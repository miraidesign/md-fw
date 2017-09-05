//------------------------------------------------------------------------
//    SessionManager.java
//             セッションを管理する
//             Copyright (c) MiraiDesign 2010-13 All Rights Reserved. 
//------------------------------------------------------------------------
package com.miraidesign.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import com.miraidesign.common.SystemConst;
import com.miraidesign.common.Version;
import com.miraidesign.renderer.item.ItemData;
import com.miraidesign.system.GCManager;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.SiteManager;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.servlet.MDServlet;
import com.miraidesign.servlet.ServletLog;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.FastOutputStream;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.Util;

/**
 *  セッションを管理します。<br>
 *  ユーザー毎のSessionObject全体を管理します。
 *
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class SessionManager {
    static private boolean debug            = (SystemConst.debug && false); // false
    static private boolean debugDisp        = (SystemConst.debug && false); // false
    static private boolean debugSession     = (SystemConst.debug && false); // 
    static private boolean debugLoad        = (SystemConst.debug && false); // 
    static private boolean debugRemove      = (SystemConst.debug && false); // 
    static private boolean debugUpdate      = (SystemConst.debug && false); // false
    static private boolean debugSessionCheck= (SystemConst.debug && false); // 
    static private boolean debugThread      = (SystemConst.debug && false); // false
    static private boolean debugCheck       = (SystemConst.debug && false); // false
    
    static private Stack<SessionObject> stack = new Stack<SessionObject>();   // SessionObjectの再利用に使用
    private static int MIN_SIZE =   0;      // for stack
    private static int MAX_SIZE =  32;      // for stack
    
    /** stack保持最小サイズを設定する */
    static public void setMinStackSize(int size) {
        if (size >= 0) MIN_SIZE = size;
    }
    
    /** stack保持最大サイズを設定する */
    static public void setMaxStackSize(int size) {
        if (size >= 0) MAX_SIZE = size;
    }
    
    /** stackを強制クリアする */
    static public void clearStack() {
        //if (debug) 
        System.out.println("SessionManager:clearStack()");
        synchronized (stack) {
            while (!stack.isEmpty()) {  // Stackになければオブジェクトを生成
                SessionObject session = (SessionObject)stack.pop();
                session = null;
            }
        }
    }
    
    static public int getStackSize() { return stack.size();}
    
    static private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static private final SimpleDateFormat sdf2 = new SimpleDateFormat("mm:ss");
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("JST"));
        sdf2.setTimeZone(TimeZone.getTimeZone("JST"));
    }
    /**
        mmm:ss 形式の文字列を返す
        @param time 経過時間(ミリ秒)
        @return 時刻文字列
    */
    static String getLapseTime(long time) {
        String flg="";
        if (time < 0) {
            flg="-"; time = -time;
        }
        int sec = (int)(time /1000);
        if (time%1000 >= 500) sec++;
        int min = sec / 60;
        sec = sec % 60;
    
        return flg+min+":"+(sec < 10 ? "0"+sec : sec);
    }
    
    //---------------------------------------------------------------------------------------
    static private Hashtable<IntObject,SessionObject> hashSessionObjects = 
               new Hashtable<IntObject,SessionObject>();  // SessionObjectを保管
    /** ロック用のオブジェクトを取得(GCManager用) */
    static public Object getHashSessionObjects() {
        return hashSessionObjects;
    }
    /** セッション数を返す */
    static public int getSessionCount() { return hashSessionObjects.size();}
    
    /** DAO使用数を返す */
    static public int getDAOCount() {
        int count = 0;
        synchronized (hashSessionObjects) { // (1) getDAOCount
            for (Enumeration e = hashSessionObjects.elements(); e.hasMoreElements();) {
                SessionObject session = (SessionObject)e.nextElement();
                count += session.getHashDAO().size();
            }
        }
        return count;
    }
    
    static private volatile SessionObject top;          // 先頭（一番古いSessionObject）
    static private volatile SessionObject bottom;       // 一番新しいSessionObject
    
    static private Thread thread;       // セッション削除スレッド
    /** セッション削除スレッド動作中フラグ */
    static public boolean isRunning;
    /** セッション削除スレッド開始  */
    static public synchronized void start() {
        //MDControl.getStatus();
        if (thread == null) {
            isRunning = true;
            thread = new Thread(
                new Runnable() {
                    long LONG_WAIT = 60 *60 * 1000;     // １時間
                    long SHORT_SLEEP =      50;         //  0.5秒
                    long LONG_SLEEP = 30 * 1000;        // 30秒
                    long REMOVE_SESSIONS = 900 * 1000;  // 15分 (バグ対策：定期的に全セッションをチェックする)
                    public void run() {
                        debug &= SystemConst.debug;
                        if (debug || debugThread) System.out.println("※※SessionThread start-------");
                        long timeout = SystemConst.sessionTimeout;
                        //timeout =  60 * 1000;    // デバッグ
                        long sleepTime = LONG_SLEEP;
                        long lastRemoveSessions = 0;
                        String szNow = "";
                        while (isRunning) {
                            try {
                                if (debug || debugThread) {
                                    synchronized (sdf) {
                                        szNow = sdf.format(new java.util.Date(System.currentTimeMillis()));
                                    }
                                }
                                if (top == null) {
                                    if (debug || debugThread) System.out.println("※※ wait "+LONG_WAIT+"\t"+szNow);
                                    synchronized (thread) {
                                        thread.wait(LONG_WAIT);
                                    }
                                    if (debug || debugThread) {
                                        synchronized (sdf) {
                                            szNow = sdf.format(new java.util.Date(System.currentTimeMillis()));
                                        }
                                        System.out.println("※※ awake \t\t"+szNow);
                                    }
                                } else {
                                    SessionObject session = top;
                                    if (session != null) {
                                        long now = System.currentTimeMillis();
                                        long l = session.lastTime;
                                        long lapse =  now - l;
                                        if (lapse >= timeout) {
                                            if (debug || debugThread) System.out.println("※※ timeout:"+lapse+"\t"+szNow);
                                            if (!GCManager.isRunning()) remove(session, "SessionManager-timeout");
                                            sleepTime = SHORT_SLEEP;
                                        } else {
                                            sleepTime = timeout - lapse;
                                        }
                                    }
                                    if (debug || debugThread) {
                                        System.out.println("※※ wait "+sleepTime+" rest:"+hashSessionObjects.size()+"\t"+szNow);
                                    }
                                    if (debugDisp) dispSession();
                                    synchronized (thread) {
                                        thread.wait(sleepTime);
                                    }
                                }
                                long lapse = System.currentTimeMillis() - lastRemoveSessions;
                                if (lapse > REMOVE_SESSIONS && !GCManager.isRunning()) {
                                    removeSessions((int)(timeout/40000));  // タイムアウト時間の1.5倍にしておく
                                    lastRemoveSessions = System.currentTimeMillis();
                                }
                                
                            } catch (InterruptedException ex) {
                                System.out.println("※※<interrupt>");
                            }
                        }
                        if (debugDisp) dispSession();
                        if (debug || debugThread) System.out.println("※※SessionThread end---------");
                        thread = null;
                    }
                }
            );
            thread.setName("SessionManager");
            thread.start();
        }
    }
    
    /** 
        debug 用 <br>
        ブラウザにアクセス中のセッションリストを表示する
    */
    static public /*synchronized*/ CharArray getSessionList(CharArray ch) {
        return getSessionList(ch, false);
    }
    
    /** 
        debug 用 
        ブラウザにアクセス中のセッションリストを表示する
    */
    static public /*synchronized*/ CharArray getSessionList(CharArray ch, boolean web_mode) {
        return getSessionList(ch, web_mode, null);
    }
    static public /*synchronized*/ CharArray getSessionList(CharArray ch, boolean web_mode, Hashtable<IntObject,String> hash) {
        if (ch == null) ch = new CharArray();
        //ch.reset();
        ch.setAppendMode(true);
        if (web_mode) {
            ch.add("<style type=\"text/css\"><!--\n");
            ch.add("td{ font-size:10pt; }\n");
            ch.add("table {\n");
            ch.add("    background:white;\n");
            ch.add("    border:4;\n");
            ch.add("    border-width: 1px 3px 3px 1px;\n");
            ch.add("}\n");
            ch.add(".TT{\n");
            ch.add("  background:#222299;\n");
            ch.add("  color:white;\n");
            ch.add("  font-weight:bold;\n");
            ch.add("}\n");
            ch.add(".RR{ background:#ffcccc; }\n");
            ch.add(".YY{ background:#ffffbb; }\n");
            ch.add(".OO{ background:#ffcc88; }\n");
            ch.add(".CC{ background:#88eeee; }\n");
            ch.add(".DD{ background:#cceecc; }\n");
            ch.add("--></style>\n");
            ch.add("<table>\n");
            
        }
        synchronized (hashSessionObjects) {
            SessionObject session = top;
            long l = System.currentTimeMillis();
            int i = 0;
            while (session != null) {
                if (web_mode && (i % 20) == 0) {
                    ch.add(" <tr>\n");
                    ch.add("   ");
                    ch.add("<td class=\"TT\">");ch.format(hashSessionObjects.size(),10,3,'0');ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("session");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("start");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("rest");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("total");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("site");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("ref");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("pv");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("user");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("item");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("carrier");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("device");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("sz");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("browser");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("uri");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("loc");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("remote");ch.add("</td>");
                    ch.add("<td class=\"TT\">");ch.add("User-Agent");ch.add("</td>\n");
                    ch.add(" </tr>\n");
                }
            
                long startTime = session.getStartTime();
                long lastTime  = session.getLastTime();
                String szStart="";
                String szRest="";
                String szTotal="";
                String szUser=session.userID;
                if (szUser == null) szUser="";
                String szUserName = session.userName;
                if (szUserName != null && szUserName.length() > 0) {
                    if (szUser.indexOf(szUserName) < 0) {
                        if (szUser.length() > 0) szUser += ":";
                        szUser += szUserName;
                    }
                }
                synchronized (sdf) {
                    szStart = sdf.format(new java.util.Date(startTime));
                }
                szRest  = getLapseTime(l-lastTime);
                szTotal = getLapseTime(l-startTime);
                
                String[] keys = {"acc_login", "user_name", "mem_name", "nickname", "mem_nick_name", "login_id", "mem_id", "user_id"};
                for (int ii = 0;  (ii < keys.length) &&(szUser== null || szUser.length()==0) ; ii++) {
                    CharArray ca = session.getUserData().get(keys[ii]);
                    if (ca != null && ca.trim().length() > 0) {
                        szUser = ca.toString();
                        break;
                    }
                }
                ++i;
                if (!web_mode && hash != null) {
                    hash.put(new IntObject(session.getSessionID()), "["+Util.format0(i,3)+"]");
                }
                ch.add(web_mode? "<td nowrap>" : "[");
                ch.format(i,10,3,'0');
                ch.add(web_mode? "</td>" : "]");
                ch.add(web_mode? "<td align=\"right\" class=\"RR\">" : " session:"); 
                ch.format(session.getSessionID(),10,11,' ');
                
                if (!web_mode) {
                    int _prev = 0;
                    int _next = 0;
                    if (session.prev != null) _prev = session.prev.sessionID;
                    if (session.next != null) _next = session.next.sessionID;
                    ch.format(_prev,10,11,' ');
                    ch.format(_next,10,11,' ');
                }
                ch.add(web_mode? "</td><td nowrap>" : " start:");ch.add(szStart);
                ch.add(web_mode? "</td><td align=\"right\" class=\"YY\">" : " rest:"); ch.add(szRest);
                ch.add(web_mode? "</td><td align=\"right\" class=\"OO\">" : " total:"); ch.add(szTotal);
                ch.add(web_mode? "</td><td align=\"right\">" : " site:"); ch.add(session.getSiteChannelCode());
                ch.add(web_mode? "</td><td align=\"right\">" : " ref:");  ch.add(session.ref_count);
                
                ch.add(web_mode? "</td><td align=\"right\" class=\"CC\">" : " pv:"); ch.add(session.access);
                if (web_mode) { 
                    ch.add("<td nowrap>"); 
                    if (szUser.length() < 22) ch.add(szUser);
                    else {ch.add(szUser, 0, 22); ch.add("--");}
                    ch.add("</td>");
                } else {
                    if (szUser != null && szUser.length() > 0) {
                        ch.add(" user:"); ch.add(szUser);
                    } else {
                        ch.add(" ---------");
                    }
                }
                if (web_mode) { 
                    ch.add("<td align=\"right\" class=\"DD\">"); ch.format(session.itemCount); ch.add("</td>");
                } else {
                    ch.add("("); ch.format(session.itemCount); ch.add(")");
                }
                ch.add(web_mode? "<td>" : "\t"); 
                ch.add(session.getCarrierName2());  // SmartPhone, Tabletを切り分ける
                ch.add(web_mode? "</td><td nowrap>":"\t"); 
                ch.add(session.getDeviceName());
                    //" prev:"+((session.prev!=null)?""+session.prev.getSessionID():"----")+ 
                    //" next:"+((session.next!= null)?""+session.next.getSessionID():"----")
                ch.add(web_mode? "</td><td nowrap>":"\t"); 
                ch.add(session.userAgent.display_size > 0 ?  ""+session.userAgent.display_size : "");
                
                ch.add(web_mode? "</td><td nowrap class=\"CC\">":"\t"); 
                ch.add(session.getBrowserName());
                    
                if (web_mode) { 
                    ch.add("</td><td nowrap class=\"DD\">"); 
                    if (session.requestURI != null) {
                        if (session.requestURI.length() < 40) ch.add(session.requestURI);
                        else {ch.add(session.requestURI, 0, 40); ch.add("--");}
                    }
                    ch.add("</td>");
                } else {
                    ch.add(" uri:"); 
                    if (session.requestURI != null) {
                        if (session.requestURI.length() < 40) ch.add(session.requestURI);
                        else {ch.add(session.requestURI, 0, 40); ch.add("--");}
                    }
                }
                String loc = session.getLang()+"-"+session.getCountryCode();
                //String remote = session.getRemoteAddr();
                String remote = session.getRemoteIP();
                if (web_mode) { 
                    ch.add("<td nowrap>"); ch.add(loc); ch.add("</td>");
                    
                    if (remote.equals("60.32.65.16")) ch.add("<td nowrap class=\"RR\">"); 
                    else                              ch.add("<td nowrap  class=\"YY\">"); 
                    ch.add(remote); ch.add("</td>");
                } else {
                    ch.add(" ");ch.add(loc); 
                    ch.add(" ");ch.add(remote); ch.add("\t");
                }
                
                if (web_mode) { 
                    ch.add("<td nowrap>"); 
                    if (session.userAgent != null) {
                        CharArray _ua = session.userAgent.getUserAgentHeader();
                        ch.add(_ua);    // 2012-01-10 全て表示するようにする
                    }
                    ch.add("</td>");
                }
                if (web_mode) { ch.add("</tr>\n");}
                session = session.next;
            } // enddo
        } // synchronized
        if (web_mode) ch.add("</table>\n");
        return ch;
    }
    
    /** 
        debug 用 
        ブラウザにアクセス中のセッションリスト数を表示する
    */
    static public /*synchronized*/ int getSessionListSize() { //
        int size = 0;
        synchronized (hashSessionObjects) {
            SessionObject session = top;
            while (session != null) {
                ++size;
                session = session.next;
            }
        }
        return size;
    }
    
    /** debug 用 */
    static public /*synchronized*/ CharArray dispSession() {
        SessionObject session = top;
        if (debug) {
            System.out.println("session---- top:"+((top==null)?"----":""+top.getSessionID())+
                    " bottom:"+((bottom==null)?"----":""+bottom.getSessionID()));
        }
        CharArray ch = new CharArray();
        getSessionList(ch);
        System.out.print(ch.toString());
        return ch;
    }
    
    
    /** セッション削除スレッドの停止  */
    static public void stop() {
        if (thread != null) {
            // スレッド停止フラグを立てる
            if (debug) System.out.println("※※SessionThread スレッド停止要求");
            isRunning = false;
            while (thread != null) {
                synchronized (thread) {
                    if (debug) System.out.println("※※notify");
                    thread.notify();
                }
                Util.Delay(100);
            }
            if (debug) System.out.println("※※SessionThread スレッド停止完了");
        }
    }
    
    /**
        セッションオブジェクトの更新<br>
        リンクの更新を行います
    */
    static public /*synchronized*/ void update(SessionObject session) { // 2013-04-17 synchronized 取る
        if (session == null) return;
        if (debug || debugUpdate) System.out.println("※※update size ="+hashSessionObjects.size());
        if (debugDisp) dispSession();
        if (session != null) session.access++;
        if (session == bottom || hashSessionObjects.size() <= 1) return;
        for (int i = 0; GCManager.isRunning() && i < 25; i++) {  // 最大5秒待つ
            Util.Delay(200);
        }
        boolean return_flg = false;
        synchronized (hashSessionObjects) { // (2) update ★要チェック
            if (session == bottom || hashSessionObjects.size() <= 1) {
                return_flg = true;
            } else {
                int sessionID = session.getSessionID();
                SessionObject prevSession = session.prev;
                SessionObject nextSession = session.next;
                if (prevSession != null) prevSession.next = nextSession;  // リンクを繋変え
                else                     top = nextSession;     // 先頭への参照を差し替え
                if (nextSession != null) nextSession.prev = prevSession;
                if (top == null) {top = session; bottom = null;}
                if (bottom != null && bottom.sessionID != session.sessionID) {
                    bottom.next = session;
                    session.prev = bottom;
                }
                bottom = session;
                session.next = null;
            }
        } // synchronized
        if (return_flg) return;
        if (debugDisp) dispSession();
        if (debug || debugUpdate) System.out.println("※※update end size ="+hashSessionObjects.size());
        if (debugSessionCheck) checkSession("update"); 
    }
    
    static public void update(int sessionID) {
        update(getSession(sessionID));
    }
    
    /**
        新規にセッションオブジェクトを取得する
        @return 新規に作成されたセッションオブジェクト 
        ## MDServletから呼ばれる (シングルサイト対応：使われていない)
    */
    static public /*synchronized*/ SessionObject getInitSessionObject(int sitecode,
                                                                  Hashtable<CharArray,CharArray> hash,
                                                                  HttpServletRequest request,
                                                                  int pcID, int count) { 

        if (debug || debugSession) {
            System.out.println(""+count+"|getInitSessionObject sitecode="+sitecode+" pcID:"+pcID);
        }
        SessionObject session = null;
        boolean notifyFlg = (top == null);
        boolean new_session = false;
        synchronized (stack) {
            if (stack.isEmpty()) {  // Stackになければオブジェクトを生成
                new_session = true;
            } else {                // あれば再利用する
                session = (SessionObject)stack.pop();
            }
        }
        if (new_session) {
            session = new SessionObject(sitecode,hash,request,count);
        } else {
            session.reset();
            session.init(sitecode,hash,request,count);
        }
        if (count > 0) session.count = count;
        if (pcID > 0) {
            if (MDServlet.isMultiSiteSessionMode()) {
                session.sessionID = (int)((int)((pcID * 37) + sitecode) & 0x7fffffff);
System.out.println(count+"|getInitSessionObject sitecode="+sitecode+" pcID:"+pcID);
System.out.println(count+"|getInitSessionObject          ="+(int)((int)((pcID * 37) + sitecode) & 0x7fffffff));
System.out.println(count+"|getInitSessionObject sessionID="+session.sessionID);
            } else {
                session.sessionID = pcID;
            }
        }
        
        if (debug || debugSession) {
            System.out.println(count+"|getInitSessionObject sessionID="+session.getSessionID()+" pcID:"+pcID);
        }
        synchronized (hashSessionObjects) { // (3) getInitSessionObject
            IntObject key = new IntObject(session.getSessionID());
            if (hashSessionObjects.containsKey(key)) {  // すでに存在する
                if (debug) System.out.println(" key already exists!! ->update");
                session = hashSessionObjects.get(key);  //@@@// 存在するセッションと入れ替える
                
                session.access++;
                SessionObject prevSession = session.prev;
                SessionObject nextSession = session.next;
                if (prevSession != null) prevSession.next = nextSession;  // リンクを繋変え
                else                     top = nextSession;     // 先頭への参照を差し替え
                if (nextSession != null) nextSession.prev = prevSession;
                if (bottom != null && bottom.sessionID != session.sessionID) {
                    bottom.next = session;
                    session.prev = bottom;
                }
                bottom = session;
                session.next = null;
                
            } else {
                session.access++;
                hashSessionObjects.put(key,session);
                
                if (top == null) {top = session; bottom = null;}
                if (bottom != null && bottom.sessionID != session.sessionID) {
                    bottom.next = session;
                    session.prev = bottom;
                }
                bottom = session;
                session.next = null;
            }
        }  // synchronized
        synchronized (thread) {
            if (debug) System.out.println("※※notify");
            thread.notify();
        }
        if (debugDisp) dispSession();
        if (debugSessionCheck) checkSession("init");  // 2012-09-27
        
        return session;
    }
    /**
        新規にセッションオブジェクトを取得する
        @return 新規に作成されたセッションオブジェクト
        ## load から呼ばれる （使われていない)
    */
    static public /*synchronized*/ SessionObject getInitSessionObject1(int sitecode,
                                                                  Hashtable<CharArray,CharArray> hash,
                                                                  HttpServletRequest request,   //2013-02-19
                                                                  int pcID, int sessionID, int count) { 
        if (debug || debugSession) {
            System.out.println(""+count+"|getInitSessionObject1 sitecode="+sitecode+" pcID:"+pcID);
        }
        
        SessionObject session = null;
        boolean notifyFlg = (top == null);
        boolean new_session = false;
        synchronized (stack) {
            if (stack.isEmpty()) {  // Stackになければオブジェクトを生成
                new_session = true;
            } else {                // あれば再利用する
                session = (SessionObject)stack.pop();
            }
        }
        if (new_session) {
            session = new SessionObject(sitecode,hash,request,count);
        } else {
            session.reset();
            session.init(sitecode,hash,request,count);
        }
        if (count > 0) session.count = count;
        if (pcID > 0 && sessionID <= 0) {
            if (MDServlet.isMultiSiteSessionMode()) {
                sessionID = (int)((int)((pcID * 37) + sitecode) & 0x7fffffff);
            } else {
                sessionID = pcID;
            }
        }
        session.sessionID = sessionID;
        if (debug || debugSession) {
            System.out.println(count+"|getInitSessionObject1 sessionID="+session.getSessionID()+" pcID:"+pcID);
        }
        
        synchronized (hashSessionObjects) { // (4) getInitSessionObject
            IntObject key = new IntObject(session.getSessionID());
            if (hashSessionObjects.containsKey(key)) {  // すでに存在する
                if (debug) System.out.println(" key already exists!! ->update");
                session = hashSessionObjects.get(key);  //@@@// 存在するセッションと入れ替える
                
                session.access++;
                SessionObject prevSession = session.prev;
                SessionObject nextSession = session.next;
                if (prevSession != null) prevSession.next = nextSession;  // リンクを繋変え
                else                     top = nextSession;     // 先頭への参照を差し替え
                if (nextSession != null) nextSession.prev = prevSession;
                if (bottom != null && bottom.sessionID != session.sessionID) {
                    bottom.next = session;
                    session.prev = bottom;
                }
                bottom = session;
                session.next = null;
                
            } else {
                session.access++;
                hashSessionObjects.put(key,session);
                if (top == null) {top = session; bottom = null;}
                if (bottom != null && bottom.sessionID != session.sessionID) {
                    bottom.next = session;
                    session.prev = bottom;
                }
                bottom = session;
                session.next = null;
            }
        } // synchronized
        
        synchronized (thread) {
            if (debug) System.out.println("※※notify");
            thread.notify();
        }
        if (debugDisp) dispSession();
        
        if (debugSessionCheck) checkSession("init1");  // 2012-09-27
        return session;
    }
    /**
        新規にセッションオブジェクトを取得する
        MDServletから呼ばれる（マルチサイト対応)
        @return 新規に作成されたセッションオブジェクト 
    */
    static public /*synchronized*/ SessionObject getInitSessionObject2(int sitecode,
                                                                  Hashtable<CharArray,CharArray> hash,
                                                                  HttpServletRequest request,   //2013-02-19
                                                                  int cartID, int count) { 
        SessionObject session = null;
        boolean notifyFlg = (top == null);
        boolean updateFlg = false;
        boolean new_session = false;
        
        for (int i = 0; GCManager.isRunning() && i < 25; i++) {  // 最大5秒待つ
            Util.Delay(200);
        }
        
        synchronized (stack) {
            if (stack.isEmpty()) {  // Stackになければオブジェクトを生成
                new_session = true;
            } else {                // あれば再利用する
                session = (SessionObject)stack.pop();
            }
        }
        if (new_session) {
            session = new SessionObject(sitecode,hash, request, count);
        } else {    // スタックを使用
            session.reset();
            session.init(sitecode,hash, request, count);
        }
        if (count > 0) session.count = count;
        
        if (cartID > 0) session.sessionID = cartID;
        else {
            cartID = (int)((int)((session.sessionID * 37) + sitecode) & 0x7fffffff);
        }
        int sessionID = session.sessionID;
        if (debug || debugSession) {
            System.out.println(""+count+"|getInitSessionObject2 sessionID:"+session.getSessionID()+"->"+sessionID+" cartID:"+cartID);
        }
        session.setSessionID(cartID);
        synchronized (hashSessionObjects) {
            IntObject key = new IntObject(sessionID);
            if (hashSessionObjects.containsKey(key)) {  // すでに存在する
                if (debug) System.out.println(" key already exists!! ->update");
                session = hashSessionObjects.get(key);  // 存在するセッションと入れ替える
                updateFlg = true;   // 2013-04-08
            } else {
                key.setValue(cartID);
                if (hashSessionObjects.containsKey(key)) {  // すでに存在する
                    if (debug) System.out.println(" key already exists!! ->update");
                    session = hashSessionObjects.get(key);  // 存在するセッションと入れ替える
                    updateFlg = true;
                } else {
                    session.access++;
                    hashSessionObjects.put(key,session);
                    if (top == null) {top = session; bottom = null;}
                    if (bottom != null && bottom.sessionID != session.sessionID) {
                        bottom.next = session;
                        session.prev = bottom;
                    }
                    bottom = session;
                    session.next = null;
                }
            }
        } // synchronized

        session.setSessionID(sessionID);
        
        synchronized (thread) {
            if (debug) System.out.println("※※notify");
            thread.notify();
        }
        if (updateFlg) update(session); // 2013-04-08
        if (debugDisp) dispSession();
        if (debugSessionCheck) checkSession("init2");  // 2012-09-27
        
        return session;
    }
    /** 
        セッションの追加 
        @return true 新規に追加 false:存在したのでリンクのみ差し替え
    */
    static public boolean add(SessionObject session) {
        boolean sts = false;
        synchronized (hashSessionObjects) {
            IntObject key = new IntObject(session.getSessionID());
            if (hashSessionObjects.containsKey(key)) {  // すでに存在する
                if (debug) System.out.println(" key already exists!! ->update");
                session.access++;
                SessionObject prevSession = session.prev;
                SessionObject nextSession = session.next;
                if (prevSession != null) prevSession.next = nextSession;  // リンクを繋変え
                else                     top = nextSession;     // 先頭への参照を差し替え
                if (nextSession != null) nextSession.prev = prevSession;
                if (top == null) {top = session; bottom = null;}
                if (bottom != null && bottom.sessionID != session.sessionID) {
                    bottom.next = session;
                    session.prev = bottom;
                }
                bottom = session;
                session.next = null;
            } else {
                session.access++;
                hashSessionObjects.put(key,session);
                if (top == null) { top = session; bottom = null;}
                if (bottom != null && bottom.sessionID != session.sessionID) {
                    bottom.next = session;
                    session.prev = bottom;
                }
                bottom = session;
                session.next = null;
                sts = true;
            }
        } // synchronized
        synchronized (thread) {
            if (debug) System.out.println("※※notify");
            thread.notify();
        }
        if (debugDisp) dispSession();
        if (debugSessionCheck) checkSession("add");  // 2012-09-27
        return sts;
    }

    /**
        ２回目以降のセッションオブジェクトの取得
        @param sessionID セッションＩＤ
        @return 取得したセッションオブジェクト
    */
    static public SessionObject getSessionObject(int sessionID) { 
        return getSessionObject(sessionID,false);
    }
    static public SessionObject getSessionObject(int sessionID, boolean updateflg) {
        return getSessionObject(sessionID, updateflg, false);
    }
static int g_count = 0;
    static public SessionObject getSessionObject(int sessionID, boolean updateflg, boolean debugLock) {
        IntObject key  =  IntObject.pop(sessionID);
        SessionObject session = null;
        
        for (int i = 0; GCManager.isRunning() && i < 50; i++) {  // 最大10秒待つ
            Util.Delay(200);
        }
        synchronized (hashSessionObjects) {
            session = (SessionObject)hashSessionObjects.get(key);
        }
        if (session != null) {
            long l = System.currentTimeMillis();
            if (updateflg) session.setLastTime(l);
            long timer = l - session.getStartTime();
            if (timer > SystemConst.accessMaxSec) {   // タイムアウトなら
                if (debug || debugSession) {
                    System.out.println(session.count+"|SessionManager AccessTime Over !! "+timer);
                }
                remove(session, "SM-accessMax");        // セッションの削除
                IntObject.push(key);
                return null;
            }
            
            if (updateflg) { 
                update(session);  // 今は呼び出しはここだけ(AbstractServlet からは呼ばれない)
            }
        } else {
            if (debug && sessionID> 0) System.out.println("session:"+sessionID+" が見つからない !! ");
        }
        IntObject.push(key);
        
        if (debugSessionCheck) checkSession("get");  // 2012-09-27
        return session;
    }
    /** ただ取得するだけ。アクセス時刻は更新しない */
    static public SessionObject getSession(int sessionID) { 
        IntObject key  =  IntObject.pop(sessionID);
        SessionObject session = (SessionObject)hashSessionObjects.get(key);
        IntObject.push(key);
        return session;
    }

    /** セッションの存在を確認する */
    static public boolean exist(int sessionID) {
        IntObject key  =  IntObject.pop(sessionID);
        SessionObject session = (SessionObject)hashSessionObjects.get(key);
        boolean rsts = (session != null);
        IntObject.push(key);
        return rsts;
    }
    /** セッションの存在を確認する */
    static public boolean exist(SessionObject session) {
        if (session == null) return false;
        return exist(session.getSessionID());
    }

    /**
        ユーザー毎のItemDataの取得を行う
        @param sessionID セッションＩＤ
        @param mixedID   ミックスドＩＤ
        @return 取得したItemDataオブジェクト（null:エラー）
    */
    // 2012-11-14 synchronized を外す
    public static /*synchronized*/ ItemData getItemData(int sessionID, int mixedID) {
        SessionObject session = getSessionObject(sessionID);  // セッションオブジェクトの取得
        if (session == null) return null;
        return session.getItemData(mixedID);
    }
    
    public static /*synchronized*/ ItemData getItemData(SessionObject session, int mixedID) {
        if (session == null) return null;
        return session.getItemData(mixedID);
    }
    
    /**
        セッションを削除する
        @param session 削除するセッション
        @return 成功したら true を返す
    */
    public static boolean remove(SessionObject session) {
        return remove(session, true, "");
    }
    /**
        セッションを削除する
        @param session 削除するセッション
        @return 成功したら true を返す 
    */
    public static boolean remove(SessionObject session, String msg) {
        return remove(session, true, msg);
    }
    /**
        セッションを削除する
        @param session 削除するセッション
        @param mode    削除セッションを初期化するか？
        @return 成功したら true を返す
    */
    public static /*synchronized*/ boolean remove(SessionObject session, boolean mode) { // 
        return remove(session, mode, "");
    }
    public static /*synchronized*/ boolean remove(SessionObject session, boolean mode, String msg) { // 2012/11/16sync 追加
        boolean rsts = false;
        if (session == null) return false;
        
        for (int i = 0; GCManager.isRunning() && i < 150; i++) { 
            Util.Delay(200);
        }
        int sessionID = session.getSessionID();
        int _count = session.count;
        
if (debug || debugSession || debugRemove) System.out.println("▼SessionManager#remove["+msg+"] "+sessionID+" "+mode);
        
        String _userid = session.userID;
        IntObject key  =  IntObject.pop(sessionID);
        boolean found = false;
        boolean isTop = false;
       synchronized (hashSessionObjects) { // (7) remove
            SessionObject prevSession = session.prev;
            SessionObject nextSession = session.next;
            int pid= -1; if (prevSession != null) pid = prevSession.sessionID;
            int nid= -1; if (nextSession != null) nid = nextSession.sessionID;
            
            found = hashSessionObjects.containsKey(key);
            if (found) {
                hashSessionObjects.remove(key); // ハッシュから削除
                
                if (prevSession != null) prevSession.next = nextSession;  // リンクを繋変え
                else                     top = nextSession;     // 先頭への参照を差し替え
                if (nextSession != null) nextSession.prev = prevSession;
                else                     bottom = prevSession;  // 末尾への参照を差し替え
            } else {
                isTop = (top != null && top.sessionID > 0 && top.sessionID == sessionID); 
if (debugRemove) System.out.println(" not found! isTop:"+isTop);
                if (isTop) {  // hashにないが、先頭にある
                    SessionObject _session = top;
                    while (_session != null) {
                        if (_session.equals(session)) {
                            _session = _session.next;
                        } else break;
                    }
                    if (top.equals(_session)) top = null;  // 2013-02-13
                    else top = _session;
                } else {
                    // do nothing 
                }
            }
            
            // リストに存在するセッションを強制削除する 2013-04-08 コスト高いので再検討すること
            int _c = 0;
            SessionObject _session = top;
            while (_session != null) {
                SessionObject _prev = _session.prev;
                SessionObject _next = _session.next; 
                if (_session.equals(session)) {
                    System.out.println("リストから消します("+sessionID+")1");   // ここまで表示されている
                    if (_prev != null) _prev.next = _next;
                    else               top = _next;
                    if (_next != null) _next.prev = _prev;
                    else               bottom = _prev;
                    if (_c == 0) {  // top
                        top = _next;
                    }
                } else if (_session.sessionID == sessionID) {
                    System.out.println("リストから消します("+sessionID+")2");
                    if (_prev != null) _prev.next = _next;
                    else               top = _next;
                    if (_next != null) _next.prev = _prev;
                    else               bottom = _prev;
                    if (_c == 0) {  // top
                        top = _next;
                    }
                }
                if (_session == _next) break;  // 
                if (++_c > 100) break;        // 無限ループする可能性を排除する
                _session = _next;
                
            } 
            IntObject.push(key);

            if (found || isTop) {
if (debug || debugSession || debugRemove) System.out.println("SessionManager#remove1 "+_count+"("+_userid+") "+MAX_SIZE);
                boolean reset = false;
                synchronized (stack) {
                    if (stack.size() < MAX_SIZE) {
                        if (mode) reset = true;
                        stack.push(session);        // Stackで再利用k
                    } else {
                        session = null;
                    }
                }
                if (reset) session.reset();
                rsts = true;
            } else {
                if (debug || debugSession) System.out.println("SessionManager session:"+sessionID+" remove key not found ! "+_count+"("+_userid+")");
            } //endif (found || isTop)
        } // synchronized  2011-11-15 戻す
         
if (debug || debugSession || debugRemove) System.out.println("▲SessionManager#remove session:"+sessionID+" "+(rsts? "成功" : "失敗")+" "+_count+"("+_userid+")");
        if (debugSessionCheck) {
            if (!checkSession("remove:")) {  // 2012-09-27
                System.out.println("found:"+found+" isTop:"+isTop);
            }
        }
        return rsts;
    }
    public static boolean remove(int sessionID, String msg) {
        SessionObject session = getSession(sessionID);
        if (session == null) return false;
        return remove(session, msg);
    }
    public static boolean remove(int sessionID) {
        SessionObject session = getSession(sessionID);
        if (session == null) return false;
        return remove(session);
    }
    /**
        全てのセッションを保存する (拡張子 .ss)
        @return 保存したセッション数
    */
    public static int saveAll() {
        return saveAll(".ss");
    }
    /**
        全てのセッションを保存する
        @param ext  拡張子
        @return 保存したセッション数
    */
    public static int saveAll(String ext) {
        int count = 0;
        synchronized (hashSessionObjects) { // (8) saveAll
            for (Enumeration e = hashSessionObjects.elements(); e.hasMoreElements();) {
                SessionObject session = (SessionObject)e.nextElement();
                if (save(session, ext)) count++;
            }
        }
        return count;
    }
  
    /**
        全てのセッションを保存する
        @param site_channel_code  対象とするサイトチャネルコード
        @param ext  拡張子
        @return 保存したセッション数
    */
    public static int saveAll(int site_channel_code, String ext) {
        int count = 0;
        synchronized (hashSessionObjects) { // (9) saveAll
            for (Enumeration e = hashSessionObjects.elements(); e.hasMoreElements();) {
                SessionObject session = (SessionObject)e.nextElement();
                if (session.getSiteChannelCode() == site_channel_code) {
                    if (save(session, ext)) count++;
                }
            }
        }
        return count;
    }

    /**
        セッションをセッションプールに保存する (拡張子 .ss)
        @param session 保存するセッション
        @return 成功したら true を返す
    
    */
    public static boolean save(SessionObject session) {
        return save(session,".ss");
    }
    /**
        セッションをセッションプールに保存する
        @param session 保存するセッション
        @param ext     拡張子 (etc ".ss")
        @return 成功したら true を返す
    
    */
    public static /*synchronized*/ boolean save(SessionObject session, String ext) {
        boolean rsts = false;
        int sessionID = session.getSessionID();
        String filename = SystemManager.poolingDirectory+sessionID+ext;
        if (debug || debugSession) {
            System.out.println("SessionManager セッションを "+filename+" に保管します。"+session.count);
        }
        try {
            if (SystemConst.zipCompress) {
                FileOutputStream fout = new FileOutputStream(filename);
                ZipOutputStream zout = new ZipOutputStream(fout);
                zout.putNextEntry(new ZipEntry(filename));
                FastOutputStream out  = new FastOutputStream(zout);
                
                out.writeUTF(filename);                 // ファイル名を保存
                out.writeInt(session.getSiteChannelCode());    // サイト/チャネルコード
                out.writeInt(Version.save);             // バージョンの保管
                out.writeLong(System.currentTimeMillis());  // セーブ時刻の保存
                CharArray ch = session.getUserAgent().getUserAgentHeader();
                if (ch == null) ch = new CharArray();
                ch.writeObject(out);
                
                session.writeObject(out);

                zout.closeEntry();
                out.flush();
                fout.close();
            
            } else {
                FileOutputStream fout = new FileOutputStream(filename);
                FastOutputStream out  = new FastOutputStream(fout);
                
                out.writeUTF(filename);                 // ファイル名を保存
                out.writeInt(session.getSiteChannelCode());    // サイト/チャネルコード
                out.writeInt(Version.save);             // バージョンの保管
                out.writeLong(System.currentTimeMillis());  // セーブ時刻の保存
                UserAgent ua = session.getUserAgent();
                CharArray ch = null;
                if (ua != null) ch = ua.getUserAgentHeader();
                if (ch == null) ch = new CharArray();
                ch.writeObject(out);
                session.writeObject(out);
                
                out.flush();
                fout.close();
            }
            
            if (debug) {
                System.out.println("SesssionManager セッションの保管が終了しました。"+session.count);
            }
            rsts = true;
        } catch (Exception ex) {
            //if (debug) {
                System.out.println("SessionManager セッションの保管エラー "+session.count);
            //}
            System.out.println("Exception:"+ex);
            ex.printStackTrace();   // for debug
        }
        return rsts;
    }
    
    /** 保存セッションファイルが存在するか  (拡張子 .ss)
        @param sessionID セッションＩＤ
        @return 存在すればtrue
    */
    public static boolean existFile(int sessionID) {
        return existFile(sessionID,".ss");
    }
    /** 保存セッションファイルが存在するか 
        @param sessionID セッションＩＤ
        @param ext       保存時の拡張子
        @return 存在すればtrue
    */
    public static /*synchronized*/ boolean existFile(int sessionID, String ext) {
        String _f = SystemManager.poolingDirectory+sessionID+ext;
        
        boolean sts = new File(_f).exists();
        
        System.out.println("ファイル["+_f+"]が存在"+(sts? "しました" : "しません"));
        return sts;
    }
    
    /** 保存セッションファイルを削除する  (拡張子 .ss)
        @param sessionID セッションＩＤ
        @return 成功すればtrue
    */
    public static boolean deleteFile(int sessionID) {
        return deleteFile(sessionID, ".ss");
    }
    /** 保存セッションファイルを削除する 
        @param sessionID セッションＩＤ
        @param ext       拡張子
        @return 成功すればtrue
    */
    public static /*synchronized*/ boolean deleteFile(int sessionID, String ext) {
        File file = new File(SystemManager.poolingDirectory+sessionID+ext);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
    
    /** 保存セッションファイルの拡張子を変更する  (拡張子 .ss → .rr)
        @param sessionID セッションＩＤ
        @return 成功すればtrue
    */
    public static boolean renameFile(int sessionID) {
        return renameFile(sessionID, ".ss", ".rr");
    }
    /** 保存セッションファイルの拡張子を変更する 
        @param sessionID セッションＩＤ
        @param extFrom   変更元拡張子 
        @param extTo     変更後拡張子
        @return 成功すればtrue
    */
    public static /*synchronized*/ boolean renameFile(int sessionID, String extFrom, String extTo) {
        File file = new File(SystemManager.poolingDirectory+sessionID+extFrom);
        if (file.exists()) {
            deleteFile(sessionID, extTo);
            return file.renameTo(new File(SystemManager.poolingDirectory+sessionID+extTo));
        }
        return false;
    }
    
    /** プ－リングされたセッションオブジェクトをロードする 
        @param sessionID セッションＩＤ
        @param siteChCode  サイト、チャネルコード
        @param hash  ヘッダー
        @return セッションオブジェクト
    */
    public static SessionObject load(int sessionID, int siteChCode, 
                                     Hashtable<CharArray,CharArray> hash,HttpServletRequest request, int pcID) {
        return load(sessionID, siteChCode, hash, request, pcID, ".ss");
    }
    /** プ－リングされたセッションオブジェクトをロードする 
        @param sessionID セッションＩＤ
        @param siteChCode  サイト、チャネルコード
        @param hash  ヘッダー
        @param ext   拡張子
        @return セッションオブジェクト
    */
    public static /*synchronized*/ SessionObject load(int sessionID, 
                                                  int siteChCode, 
                                                  Hashtable<CharArray,CharArray> hash,
                                                  HttpServletRequest request,
                                                  int pcID,
                                                  String ext) {
        return load(sessionID, siteChCode, hash, request, pcID, ext, 0);
    }
    /** プ－リングされたセッションオブジェクトをロードする 
        @param sessionID セッションＩＤ
        @param siteChCode  サイト、チャネルコード
        @param hash  ヘッダー
        @param ext   拡張子
        @param count セッションカウント
        @return セッションオブジェクト
    */
    public static synchronized SessionObject load(int sessionID, 
                                                  int siteChCode, 
                                                  Hashtable<CharArray,CharArray> hash,
                                                  HttpServletRequest request,
                                                  int pcID,
                                                  String ext,
                                                  int count) {
        String filename = SystemManager.poolingDirectory+sessionID+ext;
        if (debug || debugLoad) {
            System.out.println(count+"|◎セッションを "+filename+" からロードします:"+sessionID+"/"+siteChCode+"/"+pcID);
        }
        SessionObject session = getInitSessionObject1(siteChCode,hash,request,pcID,sessionID, count);    //@@@//
        CharArray ch = CharArray.pop();
        if (session != null) {
            try {
                FileInputStream fin = new FileInputStream(filename);
                DataInputStream in = new DataInputStream(new BufferedInputStream(fin,16384));


                boolean checkFilename = true;
                boolean checkSitecode = true;
                boolean checkVersion = true;
                boolean checkTimeout = true;
                boolean checkUserAgent = false;
                ModuleManager mm = SiteManager.get(siteChCode);
                if (mm != null) {
                    checkUserAgent = mm.checkUserAgent();
                }
                do {
                    String s = in.readUTF();
                    if (!filename.equals(s)) {
                        SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR,"ファイル名が違います["+filename+"]["+s+"]");
                        if (checkFilename) session = null; break;
                    }
                    int site = in.readInt();
                    if (ext.equals(".save") && siteChCode ==0) {
                        session.init(site, null, request, false);
                    } else if (siteChCode != site) {
                        SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR,"サイト/チャネルコードが違います["+siteChCode+"]["+site+"]");
                        if (checkSitecode) session = null; break;
                    }
                    int iVersion = in.readInt();
                    if (iVersion > Version.save) {
                        SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR,"このバージョンのファイルは読めません["+iVersion+"/"+Version.save+"]");
                        if (checkVersion) session = null; break;
                    }
                    session.version = iVersion;
//System.out.println(" version:"+iVersion+" site="+site);

                    long saveTime = in.readLong();
                    long passedTime = System.currentTimeMillis() - saveTime;
                    long limit = SystemManager.poolingTime * 60*60*1000;
                    if (passedTime > limit) {
                        SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR,"保存期間が "+SystemManager.poolingTime+" 時間をオーバーしています("+
                                            limit + "<" + saveTime+")");
                        if (checkTimeout) session = null; break;
                    }
                    ch.readObject(in);
                    
                    if (checkUserAgent && !ch.equals(session.getUserAgent().getUserAgentHeader())) {
                        SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR,"UserAgentが違います["+ch+"/"+session.getUserAgent().getUserAgentHeader()+"]");
                        if (checkUserAgent) session = null; break;
                    }
                    session.readObject(in);
                    if (debug || debugLoad) {
                        System.out.println(count+"|◎セッションのロードに成功しました。");
                    }
                } while(false);
                
                fin.close();
            } catch (Exception ex) {
                SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR," :"+ex);
                ex.printStackTrace();   // for debug
                session = null;
          }
          if (session == null) {
              if (debug || debugLoad) System.out.println("◎セッションロードエラー");
          }
        } else {
            SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR," セッションがありません");
        }
        CharArray.push(ch);
        return session;
    }
    public static synchronized SessionObject load(SessionObject session,  String ext, HttpServletRequest request,int count) {
        int sessionID = session.getSessionID();
        String filename = SystemManager.poolingDirectory+sessionID+ext;
        if (debug || debugLoad) {
            System.out.println(count+"|◎セッションを "+filename+" から上書きロードします:"+sessionID);
        }
        CharArray ch = CharArray.pop();
        if (session != null) {
            try {
                FileInputStream fin = new FileInputStream(filename);
                DataInputStream in = new DataInputStream(new BufferedInputStream(fin,16384));
                boolean checkFilename = true;
                boolean checkSitecode = true;
                boolean checkVersion = true;
                boolean checkTimeout = true;
                boolean checkUserAgent = false;
                ModuleManager mm = session.getModuleManager();
                if (mm != null) {
                    checkUserAgent = mm.checkUserAgent();
                }
                do {
                    String s = in.readUTF();
                    if (!filename.equals(s)) {
                        SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR,"ファイル名が違います["+filename+"]["+s+"]");
                        if (checkFilename) session = null; break;
                    }
                    int site = in.readInt();
                    if (ext.equals(".save")) {
                        session.init(site, null,request, false);
                    }
                    int iVersion = in.readInt();
                    if (iVersion > Version.save) {
                        SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR,"このバージョンのファイルは読めません["+iVersion+"/"+Version.save+"]");
                        if (checkVersion) session = null; break;
                    }
                    session.version = iVersion;
                    long saveTime = in.readLong();
                    long passedTime = System.currentTimeMillis() - saveTime;
                    long limit = SystemManager.poolingTime * 60*60*1000;
                    if (passedTime > limit) {
                        SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR,"保存期間が "+SystemManager.poolingTime+" 時間をオーバーしています("+
                                            limit + "<" + saveTime+")");
                        if (checkTimeout) session = null; break;
                    }
                    ch.readObject(in);
                    
                    if (checkUserAgent && !ch.equals(session.getUserAgent().getUserAgentHeader())) {
                        SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR,"UserAgentが違います["+ch+"/"+session.getUserAgent().getUserAgentHeader()+"]");
                        if (checkUserAgent) session = null; break;
                    }
                    session.readObject(in);
                    session.request = request;
                    if (debug || debugLoad) {
                        System.out.println(count+"|◎セッションのロードに成功しました。");
                    }
                } while(false);
                
                fin.close();
            } catch (Exception ex) {
                SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR," :"+ex);
                ex.printStackTrace();   // for debug
                session = null;
          }
          if (session == null) {
              if (debug || debugLoad) System.out.println("◎セッションロードエラー");
          }
        } else {
            SystemManager.log.out(ServletLog.SESSION_LOAD_ERROR," セッションがありません");
        }
        CharArray.push(ch);
        return session;
    }

    static int size_diff = 0;   // デバッグ用サイズ差分
    static int error_count = 0;
    public static void setErrorCount(int n) {
        if (n >= 0) error_count = n;
    }
    /**
        session数不整合チェックを行う
    */
    public static /*synchronized*/ boolean checkSession(String str) {
        return checkSession(str, false);
    }
    public static /*synchronized*/ boolean checkSession(String str, boolean force) { //2013-04-17 sync取る
        boolean sts = true;
        do {
            int list_size = 0;
            int hash_size = 0;
            SessionObject session = null;
            synchronized (hashSessionObjects) { // (10) checkSession
                hash_size = hashSessionObjects.size();
                session = top;
                while (session != null) {
                    ++list_size;
                    session = session.next;
                }
            }
            int diff = hash_size - list_size;
            //if (diff > size_diff) {
            if (diff != 0) {
                size_diff = diff;
                sts = false;
                System.out.println("☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆");
                SystemManager.log.error(str+" ☆ checkSession Error: list_size="+list_size+" hash_size="+hash_size);
                System.out.println("☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆");
            }
            if (diff != 0 || force) {
                CharArray ch = new CharArray(str);
                
                ch.add(" SessionList---- top:"+((top==null)?"----":""+top.getSessionID())+
                    " bottom:"+((bottom==null)?"----":""+bottom.getSessionID())+"\n");
                
                Hashtable<IntObject, String> hash = new Hashtable<IntObject,String>();
                getSessionList(ch, false, hash);
                ch.add("\nHash id\\ttprev\t\tnext\n");
                IntObject obj = new IntObject();
                for (Enumeration e = hashSessionObjects.elements(); e.hasMoreElements();) {
                    session = (SessionObject)e.nextElement();
                    if (session == null) {
                        System.out.println("null");
                        continue;
                    }
                    int _prev = 0;
                    int _next = 0;
                    if (session.prev != null) _prev = session.prev.sessionID;
                    if (session.next != null) _next = session.next.sessionID;
                    int _id = session.sessionID;
                    obj.setValue(_id);   String str1 = hash.get(obj); if (str1==null) str1="[---]";
                    obj.setValue(_prev); String str2 = hash.get(obj); if (str2==null) str2="[---]";
                    obj.setValue(_next); String str3 = hash.get(obj); if (str3==null) str3="[---]";
                    
                    String szStart = "";
                    String szLast  = "";
                    
                    synchronized (sdf) {
                        szStart = sdf.format(new java.util.Date(session.getStartTime()));
                        szLast = sdf.format(new java.util.Date(session.getLastTime()));
                    }
                    ch.add(str1);   ch.format(_id,10,10);   ch.add("\t");
                    ch.add(str2);   ch.format(_prev,10,10); ch.add("\t");
                    ch.add(str3);   ch.format(_next,10,10); ch.add("\t");
                    ch.add("pv:"); ch.format(session.access); ch.add("\t");
                    ch.add(szStart); ch.add("\t"); ch.add(szLast); ch.add("\n");
                }  // next
                if (diff != 0) SystemManager.log.error(ch.toString());
                else           SystemManager.log.warn(ch.toString());
                if (!force) {
                    if (++error_count >2) debugSessionCheck = false;  // 2回のみ表示する
                }
            } // endif (diff)
        } while (false);
        return sts;
    }
    
    /**
        指定時間(分）以上アクセスのないセッションを削除する。
    */
    static public int removeSessions(int timeoutMin) {
        int _count = 0;
        do {
            if (timeoutMin < 0) break;
            int no = 0;
            CharArray ch = CharArray.pop();
            long now = System.currentTimeMillis();
            
            ch.add("\n▽SessionManager#removeSessions timeOut:"+timeoutMin+"\n");
            for (Enumeration e = hashSessionObjects.elements(); e.hasMoreElements();) {
                ch.format(++no,10,3,'0');
                ch.add(':');
                SessionObject session = (SessionObject)e.nextElement();
                if (session == null) {
                    ch.add(" null\n");
                    continue;
                }
                int _id = session.sessionID;
                int access = session.access;
                    
                String szStart = "";
                String szLast  = "";
                
                long start = session.getStartTime();
                long last  = session.getLastTime();
                synchronized (sdf) {
                    szStart = sdf.format(new java.util.Date(start));
                    szLast = sdf.format(new java.util.Date(last));
                }
                ch.format(_id,10,11); ch.add("\t");
                ch.add("pv:");ch.format(access,10,2); ch.add("\t");
                ch.add(szStart); ch.add("\t");
                ch.add(szLast); ch.add(' ');
                
                int lap = (int)((now - last) / 60000);
                ch.format(lap,10,3);
                if (lap >= timeoutMin) {
                    ++_count;
                    remove(session, "SM-removeSessions");
                    ch.add(" removed !!");
                } else {
                    ch.add(" not removed");
                
                }
                ch.add("\n");
            
            }
            ch.add("△SessionManager#removeSessions count:"+_count);
            if (_count > 0) SystemManager.log.warn(ch.toString());
            CharArray.push(ch);
        } while (false);
        return _count;
    }
}

//
// [end of SessionManager.java]
//

