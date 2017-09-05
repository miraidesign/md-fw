//------------------------------------------------------------------------
//    ReceiveThread.java
//                 メール受信スレッドプログラム
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.mail;

import javax.mail.Message;

import com.miraidesign.util.Util;
import com.miraidesign.event.EventMulticaster;

/**
 * メール受信スレッド
 *  
 *  @version 0.5 
 *  @author toru ishioka
 *  @since  JDK1.1
*/

public class ReceiveThread implements Runnable {
    // static section ------------------------------------------
    private boolean debug     = false;
    private boolean debugRun  = false;
    
    private Thread thread;          // 受信スレッド
    private boolean isRun;          // スレッド動作中フラグ
    private boolean closed;         // スレッド完了フラグ
    private ReceiveMail mail;
    private ReceiveMailListener receiveMailListener;
    private boolean closedAll = false;
    
    private int interval = 5 * 60 * 1000;      // 5 分
    private String name ="";
    
    /** ReceiveThread オブジェクトの生成と初期化を行う
        @param mail ReceiveMail インスタンス
    */
    public ReceiveThread(ReceiveMail mail) { 
        debug &= com.miraidesign.common.SystemConst.debug;
        debugRun &= com.miraidesign.common.SystemConst.debug;
        this.mail = mail;
        init();
    }

    /** ReceiveThread オブジェクトの生成と初期化を行う
        @param mail ReceiveMail インスタンス
        @param listener 受信メールリスナー
    */
    public ReceiveThread(ReceiveMail mail, ReceiveMailListener listener) { 
        debug &= com.miraidesign.common.SystemConst.debug;
        debugRun &= com.miraidesign.common.SystemConst.debug;
        this.mail = mail;
        addReceiveMailListener(listener);
        init();
    }

    /** 初期化を行う */
    public boolean init() {
        boolean rsts = false;
        
        rsts = true;
        return rsts;
    }

    public void setName(String s) {
        this.name = s;
        if (thread != null) thread.setName(s);
    }

    /** 受信インターバルを設定する（デフォルト５分）
        @param interval 受信インターバル（分）1-
    */
    public void setInterval(int interval) {
        if (interval > 0) this.interval = interval * 60 * 1000;
    }
    public void setIntervalByMinute(int interval) {
        if (interval > 0) this.interval = interval * 60 * 1000;
    }

    /** 受信インターバルを秒単位で設定する
        @param interval 受信インターバル（秒）1-
    */
    public void setIntervalBySecond(int interval) {
        if (interval > 0) this.interval = interval * 1000;
    }

    /** メール受信イベント登録
        @param listener 受信メールリスナー
    */
    public void addReceiveMailListener(ReceiveMailListener listener) {
        receiveMailListener = EventMulticaster.add(receiveMailListener, listener);
        
    }
    /** メール受信イベント削除 
        @param listener 受信メールリスナー
    */
    public void removeReceiveMailListener(ReceiveMailListener listener) {
        receiveMailListener = EventMulticaster.remove(receiveMailListener, listener);
    }

    private int firstWait = -1;  // 最初の待ち時間  -1 で
    
    /** 最初の待ち時間を指定 */
    public void start(int firstWait) {
        this.firstWait = firstWait;
        start();
    }
    
    /** 受信を開始（または再開）する */
    public void start() {
        isRun = true;
        if (thread == null) {
            thread = new Thread(this);
            if (name.length()>0) thread.setName(name);
            thread.start();
        } else {
            synchronized (thread) {
                thread.notify();
            }
        }
    }
    
    /** 受信を一時停止する */
    public void stop() {
        isRun = false;
        if (thread != null) {
            synchronized (thread) {
                thread.notify();
            }
        }
    }
    
    /** 受信スレッドを停止する */
    public void close() {
        if (debug) System.out.println("ReceiveThread : "+thread.getName()+" close() start--");
        closed = true;
        isRun = false;
        while (closedAll == false) {
            if (debug) System.out.println("ReceiveThread : "+thread.getName()+" close() notify--");
            synchronized (thread) {
                thread.notify();
            }
            if (debug) System.out.println("ReceiveThread : "+thread.getName()+" close() notify OK");
            Util.Delay(100);
        }
        if (debug) System.out.println("ReceiveThread : "+thread.getName()+" close() end--");
    }
    
    /** */
    public void run() {
        if (debugRun) System.out.println("ReceiveThread start--");
        int count = 0;
        while (!closed) {
            ++count;
            if (debug) System.out.println("run "+(count)+"  closed:"+closed+" isRun:"+isRun);
            try {
                if (isRun) {
                    if (debug) System.out.println("sleep---");
                    if (count == 1 && firstWait >= 0) {
                        //Thread.sleep(firstWait * 1000);
                        synchronized (thread) {
                            thread.wait(firstWait * 1000);
                        }
                    } else {
                        //Thread.sleep(interval);
                        synchronized (thread) {
                            thread.wait(interval);
                        }
                    }
                    if (isRun) {
                        rec();  // 受信処理
                    } else {
                       if (debug) System.out.println("closed:"+closed+" isRun:"+isRun);
                    }
                } else {
                    if (debug) System.out.println("wait---");
                    synchronized (thread) {
                        thread.wait();     // thread.wait
                    }
                }
            } catch (InterruptedException ex) {
                System.out.println("ReceiveThread "+ex);
                ex.printStackTrace();
            }
        }
        //thread = null;
        closedAll = true;
        if (debugRun) System.out.println("ReceiveThread end--");
    }


    /** メール受信処理 */
    private void rec() {
        if (mail.connect()) {
            Message[] msgs = mail.getMessages();     //  全部取得
            // これをインターフェースに渡す
            receiveMailListener.mailReceived(msgs);
            mail.close();
            receiveMailListener.mailClosed();
        } else {
            // 何か通知する
            receiveMailListener.mailReceived(null);     // 接続エラー
        }
    }
}

//
// [end of ReceiveThread.java]
//

