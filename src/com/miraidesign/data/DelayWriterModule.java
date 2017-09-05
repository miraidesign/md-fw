//------------------------------------------------------------------------
//    DelayWriterModule.java
//              DBへの遅延書込みを実現する
//                 Copyright (c) MiraiDeign 2010 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.data;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.IntQueue;
import com.miraidesign.util.ObjectQueue;
import com.miraidesign.util.Parameter;
import com.miraidesign.util.Queue;
import com.miraidesign.util.Util;

import com.miraidesign.servlet.AbstractInitModule;
import com.miraidesign.servlet.ServletLog;
import com.miraidesign.session.SessionObject;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.ModuleManager;

/** DBへの遅延書込みを実現する */
public class DelayWriterModule extends AbstractInitModule {
    private boolean  debug = (SystemConst.debug && true);     // デバッグ文を表示するか
    private boolean  debugStatus = (SystemConst.debug && false);

    private Thread thread;
    public Thread getThread() { return thread;}

    private ServletLog log;         // ログ出力
    
    private ObjectQueue paramQueue = new ObjectQueue();          // パラメータ保管
    private CharArrayQueue sqlKeyQueue = new CharArrayQueue();   // SQL保管
    private IntQueue    siteQueue  = new IntQueue();             // siteID保管

    private CharArray sqlKey = new CharArray(); // 固定キーの時に使用する

    private IntQueue timerQueue = new IntQueue(5) {{            // タイマ保管
        setAppendMode(Queue.DELETE_TOP);    // 先頭データを捨てる
    }};

    /** パラメータスタック */
    private static int STACK_MIN = 16;
    private static int STACK_MAX = 10000;
    private Stack<Parameter> stack = new Stack<Parameter>();

    /** DelayWriter 内のスタックに保管する 
        @param param パラメータ
        @return null:保管エラー
    */
    
    public Parameter pushParameter(Parameter param) {
        if (stack.size() > STACK_MAX) {
            param = null;    // for GC
            return param;
        } else if (param != null) {
            param.clear();
            return (Parameter)stack.push(param);
        } else return null;
    }

    /** DelayWriter 内のスタックから取得する 
        @return Parameter
    */
    public Parameter popParameter() {
        Parameter param;
        synchronized (stack) {
            //if (stack.empty()) {    // スタックが空なら
            if (stack.size() < STACK_MIN) {
                param = new Parameter();
            } else {
                param = (Parameter)stack.pop();
            }
        }
        return param;
    }

    ///////////////////////////////////////
    boolean isRun = true;
    
    private SimpleDateFormat sdf;
    DataAccessObject systemDao = null;
    
    DataAccessObject userDao   = null;
    
    Hashtable<IntObject,IntObject> hashSleep = new Hashtable<IntObject,IntObject>();
    int SLEEP_MIN = 0;
    int SLEEP_MAX = 30000;
    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------
    public DelayWriterModule() {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)");
        sdf.setTimeZone(TimeZone.getTimeZone(System.getProperty("user.timezone")));
    }

    public int getSleepTime(int ave) {
        IntObject key = IntObject.pop(ave);
        IntObject obj = (IntObject)hashSleep.get(key);
        IntObject.push(key);
        if (obj == null) return SLEEP_MAX;
        else             return obj.getValue();
    }

    public void setDAO(DataAccessObject dao) {
        this.userDao = dao;
    }

    public void init() {
        super.init();
        initialize();
    }
    
    private boolean init_end = false;
    public void initialize() {
        if (init_end) return;
        init_end = true;
        debug &= SystemConst.debug;
        log = ServletLog.getInstance();
        //-----------------------------
        HashVector<CharArray,CharArrayQueue> vector = null;
        if (moduleManager != null) {
            vector = moduleManager.ini.getKeyTable(moduleManager.getSection("DelayWriter"));
        }
        if (vector == null || vector.size()==0) {
            vector = SystemManager.ini.getKeyTable("[DelayWriter]");
        }
        int last_key=0;
        int last_value=0;
        for (int i = 0; i < vector.size(); i++) {
            int key   = ((CharArray)vector.keyElementAt(i)).getInt();
            int value = ((CharArrayQueue)vector.valueElementAt(i)).peek().getInt();
            if (i == 0) SLEEP_MIN = value;  // 使っていないが
            else if (i == vector.size()-1) SLEEP_MAX = value;
            if (i > 0) {
                int key_width = key - last_key;
                int value_width = value - last_value;
                for (int j = 1; j < key_width; j++) {
                    int k  = last_key   + j;
                    int v  = last_value + (value_width * j / key_width);
                    hashSleep.put(new IntObject(k), new IntObject(v));
                }
            }
            hashSleep.put(new IntObject(key), new IntObject(value));
            last_key = key; last_value=value;
        }
        
        //-----------------------------
        if (debugStatus) {
            System.out.println("※DelayWriter."+getKey()+" MAX="+SLEEP_MAX+" ----------------------------");
            for (Enumeration  e = hashSleep.keys(); e.hasMoreElements(); ) {
                IntObject key = (IntObject)e.nextElement();
                IntObject data = (IntObject)hashSleep.get(key);
                System.out.println(""+ key.getValue()+" = "+data.getValue());
            }
            System.out.println("※DelayWriter."+getKey()+" MIN="+SLEEP_MIN+" ----------------------------");
        }
        //-----------------------------
        start();
    }

    public void destroy() {
System.out.println("※DelayWriter."+getKey()+" destroy  ");
        //super.destroy();
        stop();
    }

    /** スレッド開始  */
    public synchronized void start() {
        if (thread == null) {
            isRun = true;
            thread = new Thread(
                new Runnable() {
                    long LONG_WAIT = 30 *60 * 1000;  // 30分
                    //long SHORT_SLEEP =      100;     // 0.1秒
                    long LONG_SLEEP = 30 * 1000;     // 30秒
                    public void run() {
                        debug &= SystemConst.debug;
                        if (debug) System.out.println("※DelayWriter."+getKey()+" thread start-------------");

                        long sleepTime = LONG_SLEEP;
                        while (isRun) {
                            try {
                                if (sqlKeyQueue.size() == 0 && paramQueue.size() == 0) { // データが無い
                                    if (debug) {
                                        java.util.Date date = new java.util.Date(System.currentTimeMillis());
                                        String szDate = sdf.format(date);
                                        System.out.println("※DelayWriter."+getKey()+" wait  "+szDate);
                                    }
                                    synchronized (thread) {
                                        thread.wait(LONG_WAIT);
                                    }
                                    if (debug) {
                                        java.util.Date date = new java.util.Date(System.currentTimeMillis());
                                        String szDate = sdf.format(date);
                                        System.out.println("※DelayWriter."+getKey()+" awake "+szDate);
                                    }
                                } else {
                                    long timer = Util.Timer();
                                    write();
                                    timerQueue.enqueue((int)Util.Lapse(timer));
                                    int total = 0;
                                    int max = 0;
                                    int size = timerQueue.size();
                                    for (int i = 0; i < size; i++) {
                                        int value = timerQueue.peek(i);
                                        total += value;
                                        max = Math.max(max,value);
                                    }
                                    int ave = (size == 5) ? (total-max+timerQueue.peek(size-1))/size : total/size;
                                    //sleepTime = Util.MinMax(SHORT_SLEEP, total*5 , LONG_SLEEP);
                                    
                                    sleepTime = getSleepTime(ave);
                                    
                                    if (debug) {
                                        CharArray ch = CharArray.pop();
                                        ch.set("  (");
                                        for (int i = 0; i < timerQueue.size(); i++) {
                                            ch.add(' ');
                                            ch.format(timerQueue.peek(i));
                                        }
                                        ch.add(" )");
                                        System.out.println("※DelayWriter."+getKey()+" total:"+total+" ave:"+ave+ch);
                                        
                                        CharArray.push(ch);
                                    }
                                    if (debug) System.out.println("※DelayWriter."+getKey()+" sleep "+sleepTime);
                                    //if (debug) dispSession();
                                    //Thread.sleep(sleepTime);
                                    synchronized (thread) {
                                        thread.wait(sleepTime);
                                    }
                                }
                            } catch (InterruptedException ex) {
                                System.out.println("※DelayWriter."+getKey()+"<interrupt>");
                            }
                        }
                        if (debug) System.out.println("※DelayWriter."+getKey()+" thread end---------------");
                        thread = null;
                    }
                }
            );
            thread.setName("DelayWriterModule."+getKey());
            thread.start();
        }
    }
    
    /** スレッドの停止  */
    public void stop() {
        if (thread != null && isRun) {
            // スレッド停止フラグを立てる
            if (debug) System.out.println("※DelayWriter."+getKey()+" スレッド停止要求");
            isRun = false;
            
            while (thread != null) {
                synchronized (thread) {
                    if (debug) System.out.println("※notify");
                    thread.notify();
                }
                Util.Delay(100);
            }
            
            if (debug) System.out.println("※DelayWriter."+getKey()+" スレッド停止完了");
        }
        thread = null; isRun = false;
    }
    /////////////////////////////////////////////////////////////
    /** キーが固定のときのみ使用する
       @param key  SQLキー
    */
    public void setSqlKey(CharArray key) {
        this.sqlKey.set(key);
    }
    
    /** キーが固定のときのみ使用する
       @param key  SQLキー
    */
    public void setSqlKey(String key) {
        this.sqlKey.set(key);
    }
    
    /** データを追加する <br>
        キー固定時のみ使用する事
        @param param パラメータ
    */
    public void add(CharArrayQueue param) {
        boolean notify = false;
        synchronized (paramQueue) {
            paramQueue.enqueue(param);
            if (userDao == null) siteQueue.enqueue(0);
            notify = (paramQueue.size() == 1);
        }
//if (debug) System.out.println("※DelayWriter:paramQueue.size():"+paramQueue.size());
        if (notify) {
            synchronized (thread) {
                //if (debug) System.out.println("※DelayWriter:notify");
                thread.notify();
            }
        }
    }
    
    /** データを追加する 
        @param key キーワード
        @param param パラメータ
    */
    public void add(CharArray key, CharArrayQueue param) {
        boolean notify = false;
        synchronized (sqlKeyQueue) {
            sqlKeyQueue.enqueue(key);
            paramQueue.enqueue(param);
            if (userDao == null) siteQueue.enqueue(0);
            notify = (sqlKeyQueue.size() == 1);
        }
        if (notify) {
            synchronized (thread) {
                if (debug) System.out.println("※notify");
                thread.notify();
            }
        }
    }
    /** データを追加する 
        @param key キーワード
        @param param パラメータ
    */
    public void add(String key, CharArrayQueue param) {
        boolean notify = false;
        synchronized (sqlKeyQueue) {
            sqlKeyQueue.enqueue(key);
            paramQueue.enqueue(param);
            if (userDao == null) siteQueue.enqueue(0);
            notify = (sqlKeyQueue.size() == 1);
        }
        if (notify) {
            synchronized (thread) {
                if (debug) System.out.println("※notify");
                thread.notify();
            }
        }
    }
    /** データを追加する 
        @param key キーワード
        @param param パラメータ
        @param session SessionObject
    */
    public void add(CharArray key, CharArrayQueue param, SessionObject session) {
        boolean notify = false;
        synchronized (sqlKeyQueue) {
            sqlKeyQueue.enqueue(key);
            paramQueue.enqueue(param);
            if (userDao == null) {
                siteQueue.enqueue((session != null) ? session.getSiteChannelCode() : 0);
            }
            notify = (sqlKeyQueue.size() == 1);
        }
        if (notify) {
            synchronized (thread) {
                if (debug) System.out.println("※notify");
                thread.notify();
            }
        }
    }
    /** データを追加する 
        @param key キーワード
        @param param パラメータ
        @param session SessionObject
    */
    public void add(String key, CharArrayQueue param, SessionObject session) {
        boolean notify = false;
        synchronized (sqlKeyQueue) {
            sqlKeyQueue.enqueue(key);
            paramQueue.enqueue(param);
            if (userDao == null) {
                siteQueue.enqueue((session != null) ? session.getSiteChannelCode() : 0);
            }
            notify = (sqlKeyQueue.size() == 1);
        }
        if (notify) {
            synchronized (thread) {
                if (debug) System.out.println("※notify");
                thread.notify();
            }
        }
    }
    /**
        データがなくなるのを待つ
    */
    public void waitForDataEnd() {
        waitForDataEnd(0);
    }
    /**
        データがなくなるのを待つ
        @param waitSec 最初の待ち時間
    */
    public void waitForDataEnd(int waitSec) {
        if (debug) System.out.println("※DelayWriter."+getKey()+" データ処理終了待ち:"+paramQueue.size());
        if (waitSec > 0) Util.Delay(waitSec * 1000);
        while (sqlKeyQueue.size() > 0 || paramQueue.size() > 0) {
            Util.Delay(1000);
        }
        if (debug) System.out.println("※DelayWriter."+getKey()+" データ処理が終了しました:"+paramQueue.size());
    }


    int count = 0;
    // データを書き込む
    private int write() {
        int sts = -1;
        DataAccessObject dao = (userDao != null) ? userDao : systemDao;
        synchronized (sqlKeyQueue) {
            try {
                CharArray key = (sqlKey.length() > 0) ? sqlKey : sqlKeyQueue.dequeue();
                CharArrayQueue param = (CharArrayQueue)paramQueue.dequeue();
                if (userDao != null) {
                    if (key != null && param != null) {
                        dao.set(key, param);
                        sts = dao.executeUpdate(); log.out(sts);
                    }
                } else {
                    int site_code = siteQueue.dequeue();
                    if (key != null && param != null) {
                        if (dao == null) {
                            dao = new DataAccessObject();
                            systemDao = dao;
                        }
                        dao.setSiteCode(site_code);
                        dao.set(key, param);
                        sts = dao.executeUpdate();
                        log.out(sts);
                    }
                }
                /*if (param instanceof Parameter)*/ pushParameter((Parameter)param);
            } catch (Exception ex) {
                if (userDao == null) {
                    try { dao.close();} catch (Exception e) {}
                    dao = null;
                }
            }
            if (sqlKeyQueue.size() == 0 && paramQueue.size() == 0 && dao != null && userDao == null) {
                try { dao.close();} catch (Exception e) {}
                dao = null;
            }
        }
        if (debug) {
            System.out.println("※DelayWriter."+getKey()+" write sts="+sts+" count:"+(++count)+
                " rest:"+Math.max(sqlKeyQueue.size(),paramQueue.size()));
        }
        return sts;
    }
}

//
// [end of DelayWriterModule.java]
//
