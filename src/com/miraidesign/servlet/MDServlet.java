//------------------------------------------------------------------------
// @(#)MDServlet.java
//              MD サーブレット
//              Copyright (c) MIraiDesign 2010-20 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Calendar;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import com.miraidesign.common.SystemConst;
import com.miraidesign.common.Const;
import com.miraidesign.common.Version;
import com.miraidesign.content.ContentParser;
import com.miraidesign.data.DataConnection;
//import com.miraidesign.image.EmojiConverter;  // 利用中止 Aug,2017
import com.miraidesign.renderer.ItemRenderer;
import com.miraidesign.renderer.Module;
import com.miraidesign.renderer.item.Item;
import com.miraidesign.renderer.item.ItemData;
import com.miraidesign.renderer.item.CheckBoxData;
import com.miraidesign.renderer.item.DynamicItemData;
import com.miraidesign.renderer.item.FileData;
import com.miraidesign.system.GCManager;
import com.miraidesign.system.SiteManager;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.SiteMapping;
import com.miraidesign.session.SessionManager;
import com.miraidesign.session.SessionObject;
import com.miraidesign.session.UserAgent;
import com.miraidesign.util.ByteArray;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.Crypt62;
import com.miraidesign.util.ExceptionWriter;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.ObjectQueue;
import com.miraidesign.util.QueueTable;
import com.miraidesign.util.UserLog;
import com.miraidesign.util.UserLogFactory;
import com.miraidesign.util.Util;
import com.miraidesign.util.Validation;


/**
 *  MDServlet
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
**/

public class MDServlet extends AbstractServlet {
    static private boolean debug = (SystemConst.debug && true); //@@false);  // デバッグ表示
    static private boolean debug2 = (SystemConst.debug && false);  // デバッグ表示 false
    static private boolean debugLock = (SystemConst.debug && false);  // デバッグ表示
    static private boolean debugUpload = (SystemConst.debug && true);  // デバッグ表示
    static private boolean debugHeader = (SystemConst.debug && false);  // ヘッダデバッグ表示
    static private boolean debugHtml = (SystemConst.debug && false);  // htmlソース表示
    static private boolean debugSessionID = (SystemConst.debug && false);  // デバッグ表示
    static private boolean debugCheckbox = (SystemConst.debug && false);  // デバッグ表示
    static private boolean debugCookie = (SystemConst.debug && true);  // デバッグ表示
    static private boolean debugChar = (SystemConst.debug && false);  // デバッグ表示
    static private boolean debugSetValue = (SystemConst.debug && false);  // デバッグ表示
    static private boolean debugReplace = (SystemConst.debug && false);   // 置換内容表示

    static private boolean debugSession = (SystemConst.debug && true);  // デバッグ表示
    static private boolean debugSessionTimeout = (SystemConst.debug && true);  // デバッグ表示
    
    static private boolean debugComplex = (SystemConst.debug && false);  // デバッグ表示  false
    static private boolean debugStreamException = (SystemConst.debug && false);  // デバッグ表示
    //static private boolean debugSystemLock = (SystemConst.debug && true);  // デバッグ表示
    static private boolean debugLang = (SystemConst.debug && false);  // デバッグ表示  false
    static private boolean debugTemplate = (SystemConst.debug && false);  // デバッグ表示 false
    static private boolean debugException = (SystemConst.debug && true);  // デバッグ表示 false

    static private boolean multiSiteSessionID = false;  // マルチサイト対応の
                                                       // セッションＩＤを生成する
    static private boolean outputContentLength = true;  // ContentLength を出力する

    static private boolean allowThreadWait     = true;  // マルチアクセス時にスレッドwaitする

    static public void setMultiSiteSessionMode(boolean mode) {
if (debug2) System.out.println("MultiSiteSessionMode = "+mode);
        multiSiteSessionID = mode;
    }
    static public boolean isMultiSiteSessionMode() {return multiSiteSessionID;}
    
    static private int initCount = 0;
    static private long startTime = 0;
    static private UserLog startup_log = null;
    
    
    private void setDebugLogMode(boolean mode) {
        debug               = (SystemConst.debug && super.debug && mode);
        debugUpload         = (SystemConst.debug && super.debug && mode);
        debugCookie         = (SystemConst.debug && super.debug && mode);
        debugSessionTimeout = (SystemConst.debug && super.debug && mode);
    }
    
    /** システム初期化処理 */
    public void init(ServletConfig config) throws ServletException {
        ++initCount;
        if (initCount == 1) {
            super.init(config);

            String name = getServletContext().getServletContextName();
            String info = getServletContext().getServerInfo();
            Const.tomcat_version = info;
if (debug) System.out.println("MDServlet ------init contextName:"+name+" serverInfo:"+info);
            
            SystemManager.getInstance(getInitParameter("config"));
            debug &= SystemConst.debug;
            debug2 &= SystemConst.debug;
            debugLock &= SystemConst.debug;
            debugUpload &= SystemConst.debug;
            debugHtml &= SystemConst.debug;
            debugSessionID &= SystemConst.debug;
            debugCheckbox &= SystemConst.debug;
            debugCookie &= SystemConst.debug;
            debugChar &= SystemConst.debug;
            debugSession &= SystemConst.debug;
            
            startTime = System.currentTimeMillis();
            startup_log = UserLogFactory.getUserLog("[StartupLog]");
            if (startup_log != null) startup_log.info("System.init");
        }
    }
    
    protected void initModules() {
        for (Enumeration e = SiteManager.getModuleManagerList(); e.hasMoreElements();) {
            ModuleManager moduleManager = (ModuleManager)e.nextElement();
            moduleManager.loader.init();
            CharArray ch = new CharArray();
            SiteMapping map = moduleManager.getSiteMapping();
            if (map != null) {
                map.getSiteParameter(ch);
System.out.println("SiteChannelCode="+moduleManager.getSiteChannelCode());
System.out.println("SiteParameter="+ch);
System.out.println("SiteString="+map.getSiteString());
System.out.println("ServerKey="+moduleManager.getServerKey());

            } else System.out.println("map は nullです");
            ch.add(SystemManager.servletPath);
            ch.replace("//","/");
System.out.println("moduleManager.init("+ch+")");
            moduleManager.init(ch.toString());
        }
        for (int i = 0; i < Version.queue.size(); i++) {
            System.out.println(Version.queue.peek(i));
        }
        if (debug) System.out.println(SystemManager.checkMemory().toString());
        
        if (SystemManager.init_error > 0) {
            System.out.println("※※--システム初期化時に "+SystemManager.init_error+ "個のエラーが発生しています");
            if (startup_log != null) startup_log.error("init error count: "+SystemManager.init_error);
            SystemManager.init_error_queue.dumpQueue();
        }
        System.out.println("◎◎--------------------------------◎◎");
        System.out.println("◎◎--システム初期化が終了しました--◎◎");
        System.out.println("◎◎--------------------------------◎◎");
        if (startup_log != null) startup_log.info("System.initModules");
    }
    
    static private int destroyCount = 0;
    
    /** システム終了時処理 */
    public void destroy() {
        if (startup_log != null) startup_log.info("System.destroy start");
        ++destroyCount;
if (debug) System.out.println("システム終了要求:"+destroyCount+" "+this);
        if (destroyCount == 1) {
            if (SystemManager.ini.getBoolean("[System]","SessionSave")) {
                if (debug2) System.out.println("------セッションを保存中です------");
                SessionManager.saveAll(".save");
            }
            
            destroyModules();
            SystemManager.destroy();
            for (int i = 0; i < Version.queue.size(); i++) {
                System.out.println(Version.queue.peek(i));
            }
            System.out.println("------システムを終了します------");
            super.destroy();
        }
        if (startup_log != null) startup_log.info("System.destroy end");
    }
    protected void destroyModules() {
        int i = 0;
        for (Enumeration e = SiteManager.getModuleManagerList(); e.hasMoreElements();) {
            ModuleManager moduleManager = (ModuleManager)e.nextElement();
            int j = 0;
            for (Enumeration e2 = moduleManager.getHashID().elements(); e2.hasMoreElements();) {
                Module module = (Module)e2.nextElement();
                module.destroy();
            }
            moduleManager.loader.destroy();     //@@// SystemManagerから消されていないようなので
        }
    }
    
    /** AbstractServletをoverrideする */
    public void doGetJob(HttpServletRequest request, HttpServletResponse response,int count) {
        doPostJob(request, response, count);
    }
    public void doPostJob(HttpServletRequest request, HttpServletResponse response, 
                          int count) {  // アクセス回数
        boolean lockedFlag = false;
        boolean closeFlag = true;
        CharArray total = null;
        IntObject paramKey = null;
        SessionObject sessionObject = null;
        String frame = "";
        ModuleManager mm = null;
        try {
            if (request.getPathInfo().indexOf("/nologs/") >= 0) setDebugLogMode(false);
            if (log == null) {
                if (debug) System.out.println(count+"|doPostJob() log=null!");
                log = ServletLog.getInstance();
            }
            Hashtable<String,UploadInfo> hashFileData = new Hashtable<String,UploadInfo>();  // ファイル情報  CharArray:name ByteArray:データ
            UploadJson jsonBody = new UploadJson();
            Hashtable<String,String[]>     hashParameter = getParameters(request,count,hashFileData,jsonBody);
            Hashtable<CharArray,CharArray> hashHeader    = getHeaders(request, hashParameter, count);
            
            
            int siteChCode  = CharArray.getInt(getParameter(SystemConst.siteKey,hashParameter));
            int sessionID = 0;
            String sessionStr1 = getParameter(SystemConst.sessionIDKey[0],hashParameter);
            String sessionStr2 = getParameter(SystemConst.sessionIDKey[1],hashParameter);
            if (sessionStr1 != null && sessionStr1.length() > 0) {
                sessionID = CharArray.getInt(sessionStr1);
            } else if (sessionStr2 != null && sessionStr2.length() > 0) {
                sessionID = (int)Crypt62.decode(sessionStr2.trim());
                if (debug) System.out.println(count+"|SessionID: "+sessionStr2+" -> "+sessionID);
            }
            int mixedID = 0;
            String mixedIDStr1 = getParameter(SystemConst.mixedIDKey[0],hashParameter);
            String mixedIDStr2 = getParameter(SystemConst.mixedIDKey[1],hashParameter);
            if (mixedIDStr1 != null && mixedIDStr1.length() > 0) {
                mixedID   = CharArray.getInt(mixedIDStr1);
            } else if (mixedIDStr2 != null && mixedIDStr2.length() > 0) {
                mixedID = (int)Crypt62.decode(mixedIDStr2.trim());
                if (debug) System.out.println(count+"|MixedID: "+mixedIDStr2+" -> "+mixedID);
            }
            int pageID    = CharArray.getInt(getParameter(SystemConst.pageIDKey,hashParameter));
            
            if (SystemManager.loader.enter(request, response, hashParameter, count, siteChCode) == false) {
                return;
            }
            boolean readCookie = true;
            boolean writeCookie = true;
            if (siteChCode > 0) mm = SiteManager.get(siteChCode);   // ModuleManager 取得
            if (mm != null) {
                if (mm.loader.enter(request, response, hashParameter, count, siteChCode) == false) return;
                readCookie  = mm.readCookie;
                writeCookie = mm.writeCookie;
            }
if (debugCookie) System.out.println(count+"|★readCookie:"+readCookie+"  writeCookie:"+writeCookie);
            // クッキー対応
            Cookie[] cookies = request.getCookies();
            int cartID = 0;
            int pcID = 0;
            
            String szCartID = SystemConst.cartIDKey+Util.format0(siteChCode,5);
            String szPCID = SystemConst.pcIDKey;
            if (cookies != null && readCookie) {
                for (int i = 0; i < cookies.length; i++) {
                    Cookie cookie = cookies[i];
                    if (debug2 || debugCookie) System.out.println(count+"|cookie["+cookie.getName()+"]"+cookie.getValue()+":"+cookie.getMaxAge());
                    if (cookie.getName().equals(szCartID)) {
                        cartID = CharArray.getInt(cookie.getValue());
                        //if (cartID > 0) sessionID = cartID;
                    } else if (cookie.getName().equals(szPCID)) {
                        pcID = CharArray.getInt(cookie.getValue());
                    } 
                }
            } else {
                if (debugSession && debugCookie) System.out.println(count+"|◆cookies not found !!");
                // 通常は利用しない処理
                if (debugSession && debugCookie) {
                    for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
                        String key = (String)e.nextElement();
                        String value = (String)request.getHeader(key);
                        System.out.println(count+"|【"+key+"】"+value);
                    }
                }
                String ck = request.getHeader("cookie");
                if (debugSession && debugCookie) System.out.println(count+"|◆"+ck);
                if (readCookie && ck != null && ck.length() > 0) {
                    HashParameter hp = HashParameter.getInstance(ck, "=", ";");
                    CharArray _ch = hp.get(szCartID);
                    if (_ch != null && _ch.isDigit()) {
                        cartID = _ch.getInt();
                    }
                    _ch = hp.get(szPCID);
                    if (_ch != null && _ch.isDigit()) {
                        pcID = _ch.getInt();
                    }
                }
            }
            
            frame   = getParameter(SystemConst.frameKey,hashParameter); 
            total = new CharArray((hashParameter.get("$$$Parameters$$$"))[0]);
            paramKey = new IntObject(total.hashCode());
            String szUA = "";
            for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
               String key = (String)e.nextElement();
               if (key.equalsIgnoreCase("user-agent")) {
                    szUA = (String)request.getHeader(key);
                    break;
                }
            }
            if (szUA == null || szUA.trim().length()==0) {
                System.out.println(count+"|UserAgentが存在しません！");
            }
            boolean new_site = (sessionID <= 0);
            
            if (multiSiteSessionID) {   // マルチサイトセッションIDを有効にする
                if (debugSession) {
                    if (sessionID > 0 && cartID > 0 && sessionID != cartID) {
                        int _cartID = (int)((int)((pcID * 37) + siteChCode) & 0x7fffffff);
                        if (_cartID != sessionID) {
                            System.out.println(count+"|★★★セッション情報に矛盾があります！★★★");
                            System.out.println(count+"[S]="+sessionID+" CART_ID:"+cartID);
                            throw new Exception("Session Infomation Error!");
                        }
                    }
                }
                if (pcID > 0) {
                    cartID = (int)((int)((pcID * 37) + siteChCode) & 0x7fffffff);
                }
                if (sessionID <= 0 && cartID > 0) {
                    if (SessionManager.exist(cartID)) {
                        sessionID = cartID;
                        if (debug2 || debugSession) System.out.println(count+"| sessionID がないので、cartID:"+cartID+" を使用します");
                    } else {
                        if (debug2 || debugSession) System.out.println(count+"| cartID:"+cartID+" の セッションが存在しません");
                    }
                }
            } else {
                if (debugSession) {
                    if (sessionID > 0 && pcID > 0 && sessionID != pcID) {
                        int _cartID = (int)((int)((pcID * 37) + siteChCode) & 0x7fffffff);
                        if (_cartID != sessionID) {
                            System.out.println(count+"|★★★セッション情報に矛盾があります！★★★");
                            System.out.println(count+"[S]="+sessionID+" PC_ID:"+pcID);
                            throw new Exception("Session Infomation Error!");
                        }
                    }
                }
                if (sessionID <= 0 && pcID > 0) {
                    if (SessionManager.exist(pcID)) {
                        sessionID = pcID;
                        if (debug2 || debugSession) System.out.println(count+"| sessionID がないので、pcID:"+pcID+" を使用します");
                    } else {
                        if (debug2 || debugSession) System.out.println(count+"| pcID:"+pcID+" の セッションが存在しません");
                    }
                }
            }
            if (sessionID <= 0) {
                if (debug2 || debugSession) System.out.println(count+"|★SessionIDなし mm:"+(mm!=null));
                if (siteChCode <= 0) {
                    log.out(ServletLog.SITE_NOT_FOUND,"("+siteChCode+")");    // siteChCode が指定されていません
                    sendRedirect("SITE_NOT_FOUND",response, count);
                    return;
                } else if (mm == null) {
                    log.out(ServletLog.SITE_NOT_FOUND,"("+siteChCode+") mm=null");  // そのサイトは存在しません
                    sendRedirect("SITE_NOT_FOUND",response, count);
                    return;
                } else {
                    if (SystemManager.maxSessionCount > 0) {
                        int session_count = SessionManager.getSessionCount();
                        if (debugSession) System.out.println(count+"|◆現在のセッション数:"+session_count);
                        if (session_count >= SystemManager.maxSessionCount) {
                            String szIP = request.getRemoteAddr();
                            if (SystemManager.checkSessionAllowIP(szIP) == false) {
                                if (debugSession) System.out.println(count+"|◆セッション最大数("+SystemManager.maxSessionCount+")をオーバしました。");
                                log.out(ServletLog.SESSION_COUNT_OVER,"("+siteChCode+")SessionObjectが生成できません");
                                sendRedirect("SS_NOT_FOUND",response, mm, count);
                                return;
                            }
                        }
                    }
                    if (SystemManager.ini.getBoolean("[System]","SessionSave") || 
                        ((mm != null) && mm.loader.getModule("SessionSaveAll") != null)) {
                        if (sessionID <= 0) sessionID = cartID;
                        if (sessionID > 0) {
                            if (debug2) System.out.println(count+"|セッションプールからロードします:"+sessionID);
                            if (SessionManager.existFile(sessionID,".save")) {
                                if (debug2) System.out.println(count+"|ファイルが存在しました");
                                int pcid = pcID;
                                if (pcid == 0) { 
                                    pcid = sessionID;
                                }
                                sessionObject = SessionManager.load(sessionID, siteChCode, 
                                        hashHeader, request,pcID,".save", count);  //@@ getHeaders 1
                                if (sessionObject != null) {
                                    long passTime = System.currentTimeMillis() - sessionObject.getLastTime();
                                    if (debug2) {
                                        System.out.println(count+"|経過時刻:"+(passTime/1000)+" TIMEOUT:"+SystemConst.sessionTimeout);
                                    }
                                    if (passTime > SystemConst.sessionTimeout) {
                                        log.out(ServletLog.SS_NOT_FOUND, "セッションタイムアウトです"+sessionID);
                                        SessionManager.remove(sessionObject, "MD-SessionTimeout");
                                        SessionManager.renameFile(sessionID,".save",".loadtimeout");
                                    } else {
                                        sessionObject.setRequest(request, count);
                                        if (debug2) System.out.println(count+"|sessionID="+sessionObject.getSessionID()+" ロード完了");
                                        SessionManager.renameFile(sessionID,".save",".load");

                                        if (cartID <= 0) cartID = (int)((int)((sessionID * 37) + siteChCode) & 0x7fffffff);
                                        sessionObject.setCartID(cartID);
                                        sessionObject.cookieID = cartID;
                                    }
                                    
                                } else {
                                    SessionManager.renameFile(sessionID,".save",".loaderror");
                                }
                            } else {
                                //log.out(ServletLog.SS_NOT_FOUND, " "+sessionID); // ファイルが存在しませんでした
                                if (debug2) System.out.println(count+"|セッションプールに存在しません "+sessionID);
                            }
                        }
                    }
                    if (sessionObject == null) {
                        if (debugSession) System.out.println(count+"|◆新規にセッションを生成します");
                        // 新たにセッションオブジェクトを設定
                        if (multiSiteSessionID) {
                            sessionObject = SessionManager.getInitSessionObject2(siteChCode, hashHeader,request,cartID, count);  //@@ getHeaders
                            sessionObject.cookieID = cartID;
                        } else {
                            sessionObject = SessionManager.getInitSessionObject(siteChCode, hashHeader,request,pcID, count);   //@@ getHeaders
                            sessionObject.cookieID = pcID;
                        }
                        if (sessionObject != null) {  // 2010-04-19 追加
                            sessionID = sessionObject.getSessionID();
                            if (hashParameter != null) {
                                if (sessionID > 0 && mixedID > 0) {  // 2010-04-09 これだけじゃだめそうな
                                    setValueToItem(sessionObject,mixedID,hashParameter,hashFileData, count);  // アイテムに値を設定する
                                }
                             }
                        }
                    }
                    if (sessionObject == null) {
                        log.out(ServletLog.SESSION_COUNT_OVER,"("+siteChCode+")SessionObjectを生成しません");
                        sendRedirect("SESSION_NOT_FOUND",response, mm, count);
                        return;
                    } else {
                        sessionObject.clearAllBuffer();
                        sessionObject.clearHeaderParameter();
                        
                        if (allowThreadWait && sessionObject.locked) {  // 他フレームによるロックが終わるまで待つ
                           int tt = 100;
                           for (int i = 0; i < 200 && sessionObject.locked; i++) {
                                 if (debugLock) {
                                    if ((i % 5) == 0) {
                                        if (debug2 || debugSession) System.out.println(count+"| wait1:"+tt); 
                                    }
                                    tt+=100;
                                 }
                                 Util.Delay(100);      //最大20秒待つ
                                 if (debugLock && i == 199) {
                                    System.out.println(count+"| wait1 TimeOut !!");
                                    log.error(count+"| wait1 TimeOut !!");
                                    
                                    sessionObject.clearAllLock(count);
                                    sendRedirect("EXCEPTION",response, mm, count); //
                                    
                                    // -----------------------------------------------------
                                    if (SessionManager.exist(sessionID) && sessionObject != null) {
                                        try {
                                            //sessionObject.getModuleManager().loader.end(sessionObject);
                                            if (mm != null) mm.loader.end(sessionObject);
                                            SystemManager.loader.end(sessionObject);
                                            
                                            //if (request.getCookies()==null) {     // ロボットは無条件にsession削除
                                                if (sessionObject.isRobot()/** || sessionObject.isPC()*/) {
                                                    if (SessionManager.exist(sessionObject)) {
                                                        SessionManager.remove(sessionObject,"MD-isRobot");
                                                    }
                                                }
                                            //}
                                        } catch (Exception ex) {
                                            CharArray _mes = CharArray.pop(ex.toString());
                                            if (debug) _mes.replace("Exception","Ex");
                                            System.out.println(count+"|loader.end(1):"+_mes);
                                            if (debug) ex.printStackTrace();
                                            CharArray.push(_mes);
                                        }
                                    }
                                    // ----------------------------------------------------------------
                                    
                                    return;
                                 }
                            }
                        }
if (debugLock) System.out.println(count+"|setLocked("+paramKey+",true)");
                        if (!sessionObject.setLocked(paramKey,true, count)) {
                            if (debugLock) System.out.println(count+"|そのセッションは使用中("+sessionID+":"+total+")です(1)");
                            closeFlag = false;
                            
                            if (SystemManager.allowResponseChange) sessionObject.response = response;
                            
                        } else {
                            lockedFlag = true;
                            sessionObject.setRequest(request,count);
                            sessionObject.response = response;
                            
                            sessionID = sessionObject.getSessionID();

                            sessionObject.node = null;
                            sessionObject.page = null;
                            sessionObject.setProxyDebug(false);

                            sessionObject.mixedID   = mixedID;
                            sessionObject.pageID    = pageID;
                            sessionObject.hashParameter = hashParameter;
                            
                            sessionObject.hashFileData = hashFileData;
                            sessionObject.jsonBody = jsonBody;
                            sessionObject.mdServlet = this;
                            if (pcID <= 0) {
                                pcID = sessionID;
if (debugSession && sessionObject.isPC()) System.out.println(count+"!!alert!! pcIDが見つからないので、sessionID["+sessionID+"]を使用します!");
                            }
                            
                            sessionObject.setPCID(pcID);
                                                        
                            if (cartID <= 0) cartID = (int)((int)((pcID * 37) + siteChCode) & 0x7fffffff);
                            sessionObject.setCartID(cartID);
                            
                            if (multiSiteSessionID) {  // マルチサイトセッションIDを有効にする
                                sessionObject.setSessionID(cartID);
                            }
                        
                            // DAOキャッシュ最大値の設定
                            CharArray ch = mm.ini.get("["+mm.sectionBase+"]","DaoCashMax");
                            if (ch != null) {
                                int max = ch.getInt();
                                if (max > 0) sessionObject.setDaoMax(max);
                            }
                            
                            if (writeCookie && (sessionObject.isPC() || sessionObject.isHTML())) {         // クッキーをセットする
                                Cookie cookie = new Cookie(szCartID, ""+cartID);
                                int cartAge = SystemManager.cartCookieMaxHour;
                                if (mm != null) cartAge = mm.cartCookieMaxHour;
                                if (debug2 || debugCookie) System.out.println(count+"|addCookie("+szCartID+","+cartID+") hour:"+cartAge);
                                
                                cookie.setMaxAge((cartAge>=0) ? 60*60*cartAge : -1);   // デフォルト48時間
                                //cookie.setPath("/ccm/test");
                                cookie.setPath("/");
                                response.addCookie(cookie);
                                
                                Cookie cookie2 = new Cookie(szPCID, ""+pcID);
                                int pcAge = SystemManager.pcCookieMaxHour;
                                if (mm != null) cartAge = mm.pcCookieMaxHour;
                                if (debug2 || debugCookie) System.out.println(count+"|addCookie("+szPCID+","+pcID+") hour:"+pcAge);
                                cookie2.setMaxAge((pcAge>=0) ? 60*60*pcAge : -1);   // デフォルト48時間
                                //cookie.setPath("/ccm/test");
                                cookie2.setPath("/");
                                response.addCookie(cookie2);
                            }
                            sessionObject.count = count;
                            
                            if (pageID > 0) { // ページが指定されている
                                doFirst(request,response,sessionObject, count, hashHeader, hashParameter);
                            } else {
                                if (debug2) System.out.println(count+"|doFirst 表示ページが指定されていません:site:"+siteChCode+
                                       ":"+sessionObject.getSiteCode());
                                doFirst(request,response,sessionObject, count, hashHeader, hashParameter);
                            }
                        }
                    }
                }
            } else {    // セッションＩＤが指定されている
                if (debug2 || debugSession) System.out.println(count+"|★SessionIDあり mm:"+(mm!=null));
                if (siteChCode > 0 && SiteManager.get(siteChCode)==null) {
                    log.out(ServletLog.SITE_NOT_FOUND,"("+siteChCode+")");    // そのサイトは存在しません
                    System.out.println(count+"|そのサイトは存在しません:"+siteChCode);
                    sessionObject = SessionManager.getSessionObject(sessionID,true);
                    if (sessionObject != null) SessionManager.remove(sessionObject,"MD-SiteNotFound");
                } else {
                    sessionObject = SessionManager.getSessionObject(sessionID,true);
                    boolean loadflg = false;
                    if (sessionObject == null || sessionObject.minimized) {
                        if (sessionObject != null && sessionObject.minimized) {
                            if (debug2) System.out.println(count+"|セッションが最小化されています");
                            mm.createUserTable(sessionObject);
                            SessionManager.load(sessionObject,".save", request, count);
                            
                        } else 
                        // ロードパラメータかどうか判断
                        if (getParameter("st",hashParameter).equals("load")) {
                            loadflg = true;
                            if (debug2) System.out.println(count+"|セッションプールからロードします:"+sessionID);
                            if (SessionManager.existFile(sessionID)) {
                                if (debug2) System.out.println(count+"|ファイルが存在しました");

                                int pcid = pcID;
                                if (pcid == 0) pcid = sessionID;
                                
                                sessionObject = SessionManager.load(sessionID, siteChCode, hashHeader, request, pcid, ".ss", count);  //@@ getHeaders
                                if (sessionObject != null) {
                                    sessionObject.setRequest(request,count);
                                    SessionManager.renameFile(sessionID);   // *.ss -> *.rr
                                    if (cartID <= 0) {
                                        cartID = (int)((int)((sessionID * 37) + siteChCode) & 0x7fffffff);
                                    }
                                    sessionObject.setCartID(cartID);
                                }
                             } else {
                                log.out(ServletLog.SS_NOT_FOUND, " "+sessionID); // ファイルが存在しませんでした
                             }
                        //} else if (SystemManager.ini.getBoolean("[Session]","DiskSave")) {  // ディスク保存モード
                        } else if (SystemManager.ini.getBoolean("[System]","SessionSave")) {
                            loadflg = true;
                            if (debug2) System.out.println(count+"|セッションプールからロードします:"+sessionID);
                            if (SessionManager.existFile(sessionID,".save")) {
                                int pcid = pcID;
                                if (pcid == 0) {
                                    pcid = sessionID;
                                }
                                if (debug2) System.out.println(count+"|ファイルが存在しました pcid:"+pcid+" siteCh:"+siteChCode);

                                sessionObject = SessionManager.load(sessionID, siteChCode, hashHeader,request, pcid,".save",count);  //@@ getHeaders
                                if (sessionObject != null) {
                                    if (siteChCode == 0) siteChCode = sessionObject.getSiteChannelCode();
                                    
                                    long passTime = System.currentTimeMillis() - sessionObject.getLastTime();
                                    if (debug2) {
                                        System.out.println(count+"|経過時刻:"+(passTime/1000)+" TIMEOUT:"+SystemConst.sessionTimeout);
                                    }
                                    if (passTime > SystemConst.sessionTimeout) {
                                        log.out(ServletLog.SS_NOT_FOUND, "セッションタイムアウトです"+sessionID);
                                        SessionManager.remove(sessionObject,"MD-SessionTimeout2");
                                        SessionManager.renameFile(sessionID,".save",".loadtimeout");
                                    } else {
                                        sessionObject.setRequest(request,count);
                                        if (debug2) System.out.println(count+"|sessionID="+sessionObject.getSessionID()+" ロード完了");
                                        if (cartID <= 0) {
                                            cartID = (int)((int)((sessionID * 37) + siteChCode) & 0x7fffffff);
                                        }
                                        sessionObject.setCartID(cartID);
                                    }
                                } else {
                                    SessionManager.renameFile(sessionID,".save",".loaderror");
                                }
                             } else {
                                log.out(ServletLog.SS_NOT_FOUND, " "+sessionID); // ファイルが存在しませんでした
                             }
                        
                        } else {
                            if (debug2) System.out.println(count+"|セッションプールからロードしません");
                            
                            if (SystemManager.loader.check(request, response, hashParameter, count, siteChCode) == false) {
                                // -----------------------------------------------------
                                if (SessionManager.exist(sessionID) && sessionObject != null) {
                                    try {
                                        if (mm != null) mm.loader.end(sessionObject);
                                        SystemManager.loader.end(sessionObject);
                                        //if (request.getCookies()==null) {   // ロボットは無条件にsession削除
                                            if (sessionObject.isRobot()/** || sessionObject.isPC()*/) {
                                                if (SessionManager.exist(sessionObject)) {
                                                    SessionManager.remove(sessionObject,"MD-isRobot2");
                                                }
                                            }
                                        //}
                                    } catch (Exception ex) {
                                        CharArray _mes = CharArray.pop(ex.toString());
                                        if (debug) _mes.replace("Exception","Ex");
                                        System.out.println(count+"|loader.end(2):"+_mes);
                                        if (debug) ex.printStackTrace();
                                        CharArray.push(_mes);
                                    }
                                }
                                return;
                            }
                            if (mm != null) {
                                if (debugSessionTimeout) System.out.println(count+"|site:"+siteChCode);
                                if (mm.loader.check(request, response, hashParameter, count, siteChCode) == false) return;
                            } else {
                                if (debugSessionTimeout) System.out.println(count+"| ModuleManager not found ! site:"+siteChCode);
                            }
                        }
                    }
                    if (sessionObject == null) {
                        if (debug) System.out.println(count+"|session= null log="+(log != null));
                        log.out(ServletLog.SESSION_NOT_FOUND,"("+sessionID+")");
                        if (debug) System.out.println(count+"|loadflg="+loadflg);
                        sendRedirect(loadflg ? "SS_NOT_FOUND": "SESSION_NOT_FOUND",response, mm, count);
                        return;
                    } else {  // sessionObject != null
                        sessionObject.clearAllBuffer();
                        sessionObject.setHeader(hashHeader);     //@@ getHeaders
                        
                        mm = sessionObject.getModuleManager();
                        if (sessionObject.locked) {
                           int tt = 100;
                           for (int i = 0; i < 300 && sessionObject.locked; i++) {
                                 if (debugLock) {
                                    if ((i%5)==0) {
                                        if (debug2 || debugSession) System.out.println(count+"| wait2:"+tt); 
                                    }
                                    tt+=100;
                                 }
                                 Util.Delay(100);      //最大30秒待つ
                                 if (debugLock && i == 299) {
                                    System.out.println(count+"| wait2 TimeOut !!");
                                    log.error(count+"| wait2 TimeOut !!");

                                    sessionObject.clearAllLock(count);
                                    sendRedirect("EXCEPTION",response, mm, count); //
                                    
                                    // -----------------------------------------------------
                                    if (SessionManager.exist(sessionID) && sessionObject != null) {
                                        try {
                                            if (mm != null) mm.loader.end(sessionObject);
                                            SystemManager.loader.end(sessionObject);
                                            //if (request.getCookies()==null) {   // ロボットは無条件にsession削除
                                                if (sessionObject.isRobot()/** || sessionObject.isPC()*/) {
                                                    if (SessionManager.exist(sessionObject)) {
                                                        SessionManager.remove(sessionObject,"MD-isRobot3");
                                                    }
                                                }
                                            //}
                                        } catch (Exception ex) {
                                            CharArray _mes = CharArray.pop(ex.toString());
                                            if (debug) _mes.replace("Exception","Ex");
                                            System.out.println(count+"|loader.end(3):"+_mes);
                                            if (debug) ex.printStackTrace();
                                            CharArray.push(_mes);
                                        }
                                    }
                                    // ----------------------------------------------------------------
                                    return;
                                 }
                            }
                        }
                        
if (debugLock) System.out.println(count+"|setLocked("+paramKey+",true)★");
                        if (!sessionObject.setLocked(paramKey,true, count)) {
                            if (debugLock) System.out.println(count+"|そのセッションは使用中("+sessionID+"/"+sessionObject.userID+":"+total+")です(3)");
                            closeFlag = false;
                            if (SystemManager.allowResponseChange) sessionObject.response = response;
                        } else {
                            if (!SessionManager.exist(sessionID)) {  // SessionManagerから remove されている
                                SessionManager.add(sessionObject);
                                sessionObject.refreshItemData();
                            }
                            lockedFlag = true;
                            if (mixedID == 0) { 
                                sessionObject.previousPage = null;
                            } else              {
                                int _moduleID = mixedID /1000000;
                                int _pageID   = (mixedID % 1000000) /1000;
                                if (debug2) System.out.println(count+"|moduleID:"+_moduleID+" page:"+_pageID);

                                mm = sessionObject.getModuleManager();
                                Module m = mm.getModule(_moduleID);
                                sessionObject.previousPage = (PageServlet)m.getPage(_pageID);
                            }
                            if (multiSiteSessionID) {
                                // debug
                                String _szCartID = SystemConst.cartIDKey+Util.format0(siteChCode,5);
                                if (readCookie && cookies != null && multiSiteSessionID) {
                                    for (int i = 0; i < cookies.length; i++) {
                                        Cookie cookie = cookies[i];
                                        if (cookie.getName().equals(_szCartID)) {
                                            sessionObject.cookieID = CharArray.getInt(cookie.getValue());
                                            break;
                                        }
                                    }
                                }
                            } else {
                                sessionObject.cookieID = pcID;
                            }
                            
                            sessionObject.clickedItem = null;
                            sessionObject.clickedItemData = null;
                            sessionObject.node = null;
                            sessionObject.page = null;
                            sessionObject.setProxyDebug(false);
                            if (sessionID > 0 && mixedID > 0) {
                                setValueToItem(sessionObject,mixedID,hashParameter,hashFileData, count);  // アイテムに値を設定する
                            }
                            sessionObject.setRequest(request,count);
                            sessionObject.response = response;
                            sessionID  = sessionObject.getSessionID();
                            sessionObject.mixedID   = mixedID;
                            sessionObject.pageID    = pageID;
                            sessionObject.hashParameter = hashParameter;
                            sessionObject.hashFileData = hashFileData;
                            sessionObject.jsonBody = jsonBody;
                            sessionObject.mdServlet = this;
                            if (siteChCode > 0) {
                                int orgCode = sessionObject.getSiteChannelCode();
                                if (orgCode != siteChCode) {
                                    sessionObject.setSiteChannelCode(siteChCode);
                                    mm.createUserTable(sessionObject);
                                }
                            }
                            if (new_site) {
                                if (cartID <= 0) cartID = (int)((int)((sessionID * 37) + siteChCode) & 0x7fffffff);
                                sessionObject.setCartID(cartID);
                                if (pcID <= 0) pcID = sessionID;
                                sessionObject.setPCID(pcID);
                            
                                // DAOキャッシュ最大値の設定
                                if (mm != null) {
                                    CharArray ch = mm.ini.get("["+mm.sectionBase+"]","DaoCashMax");
                                    if (ch != null) {
                                        int max = ch.getInt();
                                        if (max > 0) sessionObject.setDaoMax(max);
                                    }
                                }
                                if (writeCookie && (sessionObject.isPC() || sessionObject.isHTML())) {         // クッキーをセットする
                                    Cookie cookie = new Cookie(szCartID, ""+cartID);
                                    int cartAge = SystemManager.cartCookieMaxHour;
                                    if (mm != null) cartAge = mm.cartCookieMaxHour;
                                    
                                    cookie.setMaxAge((cartAge>=0)?60*60*cartAge:-1);
                                    cookie.setPath("/");
                                    response.addCookie(cookie);
                                    Cookie cookie2 = new Cookie(szPCID, ""+pcID); 
                               
                                    int pcAge = SystemManager.pcCookieMaxHour;
                                    if (mm != null) pcAge = mm.pcCookieMaxHour;
                                    cookie2.setMaxAge((pcAge>=0)?60*60*pcAge:-1);
                                    cookie2.setPath("/");
                                    response.addCookie(cookie2);
                                }
                                
                                // エミュレータデバッグモード
                                if (SystemManager.ini.getBoolean("[UserAgentEmurator]","convert")) {
                                    String emu = getParameter("emu",hashParameter);
                                    if (emu != null && emu.length()>0) {
                                        CharArray ch = SystemManager.ini.get("[UserAgentEmurator]",emu);
                                        if (ch != null && ch.trim().length()>0) {
                                            CharArray tmp = CharArray.pop("user-agent");
                                            CharArray value = (CharArray)sessionObject.hashHeader.get(tmp);
                                            if (value != null) {
                                                value.set(ch);
                                                sessionObject.userAgent = UserAgent.createUserAgent(sessionObject, count);
                                            }
                                            CharArray.push(tmp);
                                        }
                                    }
                                }
                            }
                            sessionObject.count = count;
                            if (pageID > 0) {    // 表示ページが指定されている
                                doNext(request, response, sessionObject, count, hashHeader, hashParameter);
                            } else {
                                if (debug2) System.out.println(count+"|doNext 表示ページが指定されていません");
                                doNext(request, response, sessionObject, count, hashHeader, hashParameter);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (debugException) ex.printStackTrace();
        
            try {
                ExceptionWriter exw = ExceptionWriter.pop();
                ex.printStackTrace(exw);
                
                // perlに切られるメッセージ回避の一時対応
                CharArray chTmp = CharArray.pop();
                chTmp.set(ex.toString());
                chTmp.add(exw.peek(0));
                chTmp.replace("java.lang.","");
                chTmp.replace("com.miraidesign.renderer.item.","");
                chTmp.replace("com.miraidesign.renderer.","");
                chTmp.replace("com.miraidesign.servlet.","");
                chTmp.replace("com.miraidesign.system.","");
                chTmp.replace("com.miraidesign.module.","");
                chTmp.replace("com.miraidesign.","");
                String userID = null;
                if (sessionObject != null) userID = sessionObject.userID;
                if (userID != null && userID.length() > 0) {
                    log.out(ServletLog.EXCEPTION,count+"("+userID+")|CS:"+chTmp);
                } else {
                    log.out(ServletLog.EXCEPTION,count+"|CS:"+chTmp);
                }
                CharArray.push(chTmp);
                if (debug || sessionObject != null) exw.dumpQueue();
                ExceptionWriter.push(exw);
            } catch (Exception ex2) {
                System.out.println(count+"|log 出力エラー");
                ex2.printStackTrace();
                System.out.println(count+"|log 出力エラー終了");
            }

if (debugLock) System.out.println(count+"|setLocked("+paramKey+",false) session="+(sessionObject!=null));
            if (sessionObject != null) sessionObject.setLocked(paramKey,false,count);
            System.out.println(count+"|free:"+Runtime.getRuntime().freeMemory());
            sendRedirect("EXCEPTION",response, mm, count);
            
            if (sessionObject != null && SessionManager.exist(sessionObject.getSessionID())) {
                try {
                    if (mm != null) mm.loader.end(sessionObject);
                    SystemManager.loader.end(sessionObject);
                    //if (request.getCookies()==null) { // ロボットは無条件にsession削除
                        if (sessionObject.isRobot()/**|| sessionObject.isPC()**/) {
                            if (SessionManager.exist(sessionObject)) {
                                SessionManager.remove(sessionObject,"MD-isRobot4");
                            }
                        }
                    //}
                } catch (Exception ex2) {
                    CharArray _mes = CharArray.pop(ex2.toString());
                    if (debug) _mes.replace("Exception","Ex");
                    System.out.println(count+"|loader.end(4):"+_mes);
                    if (debug) ex.printStackTrace();
                    CharArray.push(_mes);
                }
            }
            return;
        }
        if (lockedFlag) {
if (debugLock) System.out.println(count+"|setLocked("+paramKey+",false) session="+(sessionObject!=null));
            if (sessionObject != null) sessionObject.setLocked(paramKey,false, count);
        } else {
if (debugLock) System.out.println(count+"|今はよばれない");
        }
        
        // -----------------------------------------------------
        if (sessionObject != null && SessionManager.exist(sessionObject.getSessionID())) {
            try {
                if (mm != null) mm.loader.end(sessionObject);
                SystemManager.loader.end(sessionObject);
                //if (request.getCookies()==null) { // ロボットは無条件にsession削除
                    if (sessionObject.isRobot()/** || sessionObject.isPC()**/) {
                        if (SessionManager.exist(sessionObject)) {
                            SessionManager.remove(sessionObject,"MD-isRobot5");
                        }
                    }
                //}
            } catch (Exception ex) {
                CharArray _mes = CharArray.pop(ex.toString());
                if (debug) _mes.replace("Exception","Ex");
                System.out.println(count+"|loader.end(5):"+_mes);
                if (debug) ex.printStackTrace();
                CharArray.push(_mes);
            }
        }
        GCManager.execute();
    }

    public void doFirst(HttpServletRequest request, HttpServletResponse response, SessionObject session, int count,
                        Hashtable<CharArray,CharArray> hashHeader, Hashtable<String,String[]> hashParameter) {
        int moduleID = session.pageID / 1000;
        int pageID   = session.pageID % 1000;
        ModuleManager moduleManager = SiteManager.get(session.getSiteChannelCode());
        ModuleServlet module = (ModuleServlet)moduleManager.getModule(moduleID);
        if (module == null) {
            if (debug2) System.out.println(count+"|module("+moduleID+") == null");
            module = (ModuleServlet)moduleManager.getDefaultModule();
            if (module != null) {
                PageServlet page = (PageServlet)module.getDefaultPage();
                if (page == null) {
                    if (debug2) System.out.println("module:"+module.getName());
                    page = module.getDefaultPage();
                    if (debug && page != null ) {
                        System.out.println(count+"|デフォルトモジュールを使用します"+
                          " site="+moduleManager.getSiteName()+"("+moduleManager.getSiteChannelCode()+")"+
                          " page="+module.getName()+"("+module.getModuleID()+"):"+page.getPageID());
                    }
                }
                if (page == null) {
                    System.out.println(count+"|ページが見つかりません M:"+moduleID+"=null P:"+pageID);
                } else {
                    if (debug2) System.out.println(count+"|ページID:"+page.getPageID());
                    if (page.forward(request, response, count, hashHeader, hashParameter)) {     //@@ 2020 
                        SessionManager.remove(session);
                    } else {
                        execute(request, response, page,session,count);
                    }
                }
            } else {
                if (debug) System.out.println(count+"|Default Module がありません");
            }
        } else {
            PageServlet page = (PageServlet)module.get(pageID);
            if (page == null) {
                page = module.getDefaultPage();
                if (debug && page != null ) {
                    System.out.println(count+"|デフォルトページを使用します"+
                        " site="+moduleManager.getSiteName()+"("+moduleManager.getSiteChannelCode()+")"+
                        " page="+module.getName()+"("+module.getModuleID()+"):"+page.getPageID());
                }
            }
            if (page == null) {
                System.out.println(count+"|ページが見つかりません M:"+moduleID+" P:"+pageID);
            } else {
                if (page.forward(request, response, count, hashHeader, hashParameter)) { //@@ 2020
                    SessionManager.remove(session);
                } else {
                    execute(request, response, page,session, count);  //@@
                }
            }
        }
    }
    
    public void doNext(HttpServletRequest request, HttpServletResponse response, SessionObject session, int count,
                        Hashtable<CharArray,CharArray> hashHeader, Hashtable<String,String[]> hashParameter) {
        int moduleID = session.pageID / 1000;
        int pageID   = session.pageID % 1000;
        
        ModuleManager moduleManager = SiteManager.get(session.getSiteChannelCode());
        
        ModuleServlet module = (ModuleServlet)moduleManager.getModule(moduleID);
        if (module == null) {
            if (debug2) System.out.println(count+"|module("+moduleID+") == null");
            module = (ModuleServlet)moduleManager.getDefaultModule();
            if (module != null) {
                PageServlet page = (PageServlet)module.getDefaultPage();
                if (page == null) {
                    page = module.getDefaultPage();
                    if (debug && page != null) {
                        System.out.println(count+"|デフォルトモジュールを使用します"+
                          " site="+moduleManager.getSiteName()+"("+moduleManager.getSiteChannelCode()+")"+
                          " page="+module.getName()+"("+module.getModuleID()+"):"+page.getPageID());
                    }
                }
                if (page == null) {
                    System.out.println(count+"|ページが見つかりません M:"+moduleID+"=null P:"+pageID);
                } else {
                    if (debug2) System.out.println(count+"|ページID:"+page.getPageID());
                    
                    if (page.forward(request, response, count, hashHeader, hashParameter)) {
                        SessionManager.remove(session);
                    } else {
                        execute(request, response, page,session,count);
                    }
                }
            }
        } else {
            PageServlet page = (PageServlet)module.get(pageID);
            if (page == null) {
                page = module.getDefaultPage();
                if (debug && page != null ) {
                    System.out.println(count+"|デフォルトページを使用します"+
                        " site="+moduleManager.getSiteName()+"("+moduleManager.getSiteChannelCode()+")"+
                        " page="+module.getName()+"("+module.getModuleID()+"):"+page.getPageID());
                }
            }
            if (page == null) {
                System.out.println(count+"|ページが見つかりません(Next) M:"+moduleID+" P:"+pageID);
            } else {
                if (page.forward(request, response, count, hashHeader, hashParameter)) {
                    SessionManager.remove(session);
                } else {
                    execute(request, response, page,session, count);  // @@
                }
            }
        }
    }
    /**
        ページサーブレットのメソッドを呼び出し、描画を行う
        @param page 呼び出しページ
        @param session セッションオブジェクト
    */
    private void execute(HttpServletRequest request, HttpServletResponse response, PageServlet page, SessionObject session, int count) {
    
        int sessionID = session.getSessionID();
        //OutputStream outStream = null;
        
        if (debugSession) {
            if (session != null) session.println(sessionID+" このページの処理を開始します ");
            else System.out.println(count+"|このページの処理を開始します ");
        }
        
        session.clearTemplate();
        session.enterPage = page;
        SystemManager.loader.start(session); 
        ModuleManager mm = session.getModuleManager();
        mm.loader.start(session);
        page = session.enterPage;

        if (page == null) {
            System.out.println(count+"|●InitModuleからの終了要求が発生しました");
            sendRedirect("SS_NOT_FOUND",response, mm, count);
            return;
        }
        
        // url-DB マッピング
        session.setDefaultConnection("");
        String requestURL = request.getRequestURL().toString(); // for debug
        CharArray chDS = DataConnection.getDataSourceByURL(request.getRequestURL());
        if (chDS != null && chDS.length() > 0 && chDS.getAppendMode()) {
            session.setDefaultConnection(chDS.toString());
        }
        session.previousForwardPage = session.forwardPage;
        session.forwardPage = page;
        execute(request, response, page.forward(session), session, count);      //@@// 現在forward を読んでいるのはここのみ
    }
    
    
    
    private void execute(HttpServletRequest request, HttpServletResponse response, ObjectQueue queue, SessionObject session, int count ) {
        if (queue == null || queue.size() == 0) {
            if (debug2) System.out.println(count+"|MDServlet: 表示ページはありません queue:"+(queue !=null));
            return;
        }
        ModuleManager mm = session.getModuleManager();
        session.clearAllBuffer();
        CharArray ch = session.getBuffer();         // 出力バッファの取得
        ch.clear();
        int sessionID = session.getSessionID();
        
        if (queue == null || queue.size() == 0) {
            if (debug2) System.out.println(count+"|MDServlet: 表示ページはありません queue:"+(queue !=null));
            return;
        }
        PageServlet page0 = (PageServlet)queue.peek(0);  // 先頭ページ
        page0.setNavigation(session); // setTemplate->setNavigation
        session.forwardPage = page0;
        ContentRenderer renderer = SystemManager.contentRenderer;
        boolean isComplex =  (
        renderer != null && renderer.isComplexPage(queue));   // 合成モードか？
        boolean isAjax = (page0.getMimeType().length() > 0);
if (debugComplex && !isComplex) System.out.println(count+"|★MDServlet:合成ページではありません!! renderer="+(renderer !=null));
        
        CharArrayQueue namespaceQueue = null;
        if (isComplex) namespaceQueue = renderer.getNamespaceQueue(queue);
        CharArray defaultNamespace = session.getDefaultNamespace();    // 念のため
        
        if (!isComplex) {
            SystemManager.loader.makeStart(session);
            mm.loader.makeStart(session);
            if (isComplex) session.setDefaultNamespace(defaultNamespace);
            if (!session.getParameter(SystemConst.makePageKey).equals("none")) {
                for (int i = 0; i < queue.size(); i++) {
                    if (namespaceQueue!= null) session.setDefaultNamespace(namespaceQueue.peek(i));
                    PageServlet page = (PageServlet)queue.peek(i);
                    page.makePage(session);
                }
            }
            SystemManager.loader.drawStart(session); 
            if (mm != null) mm.loader.drawStart(session);
        }

         
        String str = "error";
        CharArray title = page0.getTitleName();
        
        String contentType = session.getContentType(); 
        String charSet = session.getCharSet();
        
        //------------------------------- 
        if (contentType.length() == 0) { 
            setContentType(contentTypeSJIS);
            session.setContentType(contentTypeSJIS);
            contentType = contentTypeSJIS;
        }
        //-------------------------------
        if (isComplex) {
            if (debugComplex) System.out.println(count+"|★MDServlet:合成ページ表示を行います: title="+title+" titlename:"+page0.getTitleName());
            ItemRenderer orgRenderer = session.getItemRenderer();
            CharArray    orgLanguage = session.getLanguage();
            if (session.isXHTML()) { 
                session.setLanguage("XHTML");
                session.setItemRenderer(com.miraidesign.renderer.xhtml.XhtmlRenderer.getInstance());
            }
            
            // ヘッダ取得
            CharArray header = renderer.getHeader(session); // キャリアごとのヘッダを取得

            CharArray chTitle  = CharArray.pop(); // タグなし情報
            CharArray chMeta   = CharArray.pop(); // タグ付き
            CharArray chBase   = CharArray.pop(); // タグ付き
            CharArray chLink   = CharArray.pop(); // タグ付き
            CharArray chStyle  = CharArray.pop(); // タグ付き
            CharArray chScript = CharArray.pop(); // タグ付き
            CharArray chBody   = CharArray.pop(); // 

            // ページタイトルの設定
            chTitle.add(title);
            
            CharArray styleSheet0 = mm.getStyleSheet();    // スタイルシート
            CharArray styleSheet = page0.getStyleSheet();    // スタイルシート
            CharArrayQueue styleSheetURL0 = mm.getStyleSheetURL(session.getCarrier()); // スタイルシートURL
            CharArrayQueue styleSheetURL = page0.getStyleSheetURL(session.getCarrier()); // スタイルシートURL

            // スタイルシート設定
            for (int i = 0; i < styleSheetURL0.size(); i++) {
                chLink.add("<link rel=\"stylesheet\" href=\""); 
                chLink.add(styleSheetURL0.peek(i)); 
                chLink.add("\" type=\"text/css\" />\n");
            }
            for (int i = 0; i < styleSheetURL.size(); i++) {
                chLink.add("<link rel=\"stylesheet\" href=\""); 
                chLink.add(styleSheetURL.peek(i)); 
                chLink.add("\" type=\"text/css\" />\n");
            }
            if (styleSheet0.length() > 0) {
                chStyle.add("<style type=\"text/css\">\n");
                chStyle.add(styleSheet0);
                chStyle.add("  </style>\n");
            }
            if (styleSheet.length() > 0) {
                chStyle.add("<style type=\"text/css\">\n");
                chStyle.add(styleSheet);
                chStyle.add("</style>\n");
            }
                
            chBody.add(page0.getBodyTag()); // 
            
            session.clearMetaAll();         // 
            session.clearHeaderParameter(); //

            SystemManager.loader.makeStart(session);
            if (mm != null) mm.loader.makeStart(session);
            
if (debugComplex) {
            session.println("parameter:"+session.hashParameter.size());
            for (Enumeration e = session.hashParameter.keys(); e.hasMoreElements();) {
                String key = (String)e.nextElement();
                if (!key.equals("$$$Parameters$$$")) {
                    String[] value = (String[])session.hashParameter.get(key);
                    if (value != null) session.println("["+key+"]"+value[0]);
                }
            }
            HashParameter userData = session.getUserData();
            session.println("userData:"+userData.size());
            userData.debugParameter(session);
}
            SystemManager.loader.drawStart(session); 
            if (mm != null) mm.loader.drawStart(session);
            
            // ページ取得
            for (int i = 0; i < queue.size(); i++) {
                PageServlet page = (PageServlet)queue.peek(i);
                if (page != null) {
if (debugComplex) System.out.println(count+"["+(i+1)+"/"+queue.size()+"]" + "pageID:"+page.getPageID());
                    if (namespaceQueue!= null) {
                        CharArray _ns = namespaceQueue.peek(i);
if (debugComplex) System.out.println(count+"| nameSpace:"+_ns);
                        if (_ns != null) {
                            session.setDefaultNamespace(_ns);
                            if (_ns.length()>0) session.clearTemplate(_ns);
                        } else {
                            session.setDefaultNamespace("");
                        }
                    }
                    page.makePage(session);  // 
if (debugComplex) System.out.println("ページ:"+page.getModule().getName()+"."+page.getName()+":"+page.getModule().getModuleID()+":"+page.getPageID());
                    
                    ContentParser parser = page.getParser();
                    if (parser != null && parser.isEnabled()) {
                        page.showTemplate(session); // ナビ・ガイダンス等の表示
                        if (debugComplex) System.out.println(count+"| parser.setTemplate() ◆ sessionID:"+session.getSessionID());
                        // システムテンプレートでの @関数利用を可能に 
                        parser.setTemplate(session);
                        if (debugComplex) System.out.println(count+"| parser.draw() ◆ sessionID:"+session.getSessionID());
                        ch.add(parser.draw(session));
                        
                        if (debugComplex) System.out.println(count+"| parser.draw() end ◆ ");
                    } else {
                        if (debugComplex) System.out.println(count+"| page.draw()◆ sessionID:"+session.getSessionID());
                        page.draw(session); //  通常はオーバーライドのみ利用される
                        if (debugComplex) System.out.println(count+"| page.draw() end ◆ ");
                    }
                }
            }
            if (isComplex) session.setDefaultNamespace(defaultNamespace);
            //---------------------------------------------------------
            //session meta 処理 
            HashParameter hp1 = session.getHashMetaHttp();
            HashParameter hp2 = session.getHashMetaName();
            CharArrayQueue meta0 = mm.getMeta();           // メタデータ
            CharArrayQueue meta  = page0.getMeta();        // メタデータ
            if ((hp1 != null && hp1.size() > 0) || (hp2 != null && hp2.size() > 0)
                || meta0.size() > 0 || meta.size() > 0) {
                
                if (hp1 != null) { // 
                    for (int i = 0; i < hp1.size(); i++) {
                        chMeta.add("<meta http-equiv=\""); chMeta.add(hp1.keyElementAt(i));
                        chMeta.add("\" content=\""); chMeta.add(hp1.valueElementAt(i));
                        chMeta.add("\"/>\n");
                    }
                }
                if (hp2 != null) { // 
                    for (int i = 0; i < hp2.size(); i++) {
                        chMeta.add("<meta name=\""); chMeta.add(hp2.keyElementAt(i));
                        chMeta.add("\" content=\""); chMeta.add(hp2.valueElementAt(i));
                        chMeta.add("\"/>\n");
                    }
                }
                // これはまだ誰も使っていないはず、、、
                for (int i = 0; i < meta0.size(); i++) {
                    chMeta.add("  <meta "); chMeta.add(meta0.peek(i)); chMeta.add("/>\n");
                }
                for (int i = 0; i < meta.size(); i++) {
                    chMeta.add("  <meta "); chMeta.add(meta.peek(i)); chMeta.add("/>\n");
                }
            }
            ////////////////////////////////////////////////////////
            // ヘッダタグ置換
            ////////////////////////////////////////////////////////
            CharArray _meta   = session.getHeaderTemplate("$$$meta$$$");
            CharArray _title  = session.getHeaderTemplate("$$$title$$$");
            CharArray _base   = session.getHeaderTemplate("$$$base$$$");
            CharArray _link   = session.getHeaderTemplate("$$$link$$$");
            CharArray _style  = session.getHeaderTemplate("$$$style$$$");   // 未処理(ContentInput)
            CharArray _script = session.getHeaderTemplate("$$$script$$$");  // 未処理(ContentInput)
            CharArray _body   = session.getHeaderTemplate("$$$body$$$");
            if (debugHeader) {
                if (_meta != null)   session.println(_meta);
                if (_title != null)  session.println(_title);
                if (_base != null)   session.println(_base);
                if (_link != null)   session.println(_link);
                if (_style != null)  session.println(_style);
                if (_script != null) session.println(_script);
                if (_body != null)   session.println(_body);
            }
            if (_title != null && _title.length() > 0) chTitle.set(_title);
            header.replace("@{TITLE}", "<title>"+chTitle+"</title>\n");
            header.replace("@{META}",  chMeta.add(_meta));
            header.replace("@{BASE}",  chBase.add(_base));
            header.replace("@{LINK}",  chLink.add(_link));
            header.replace("@{STYLE}", chStyle.add(_style));
            header.replace("@{SCRIPT}",chScript.add(_script));
            header.replace("@{BODY}",  chBody.add(_body));
            CharArray.push(chBody);
            CharArray.push(chScript);
            CharArray.push(chStyle);
            CharArray.push(chLink);
            CharArray.push(chBase);
            CharArray.push(chMeta);
            CharArray.push(chTitle);
            
            //-----------------------------------------------
            HashParameter hp = session.getHeaderParameter();
            if (hp != null) {
                CharArray value = CharArray.pop();
                for (int i = 0; i < hp.size(); i++) {
                    CharArray key   = hp.keyElementAt(i);
                    value.set(hp.valueElementAt(i));
                    if (debug && debugReplace) session.print("★Header["+key+"]->["+value+"] ");
                    value.replace('\n',' '); value.replace('\r',' ');
                    value.replaceTag();
                    int _count = header.replaceCount(key, value);
                    if (debug && debugReplace) System.out.println(_count+" 個、置換しました");
            
                } // next
                CharArray.push(value);
            }
            
            if (!isAjax) {  // AJAXモード以外は、ヘッダフッタを追加する
                ch.insert(0, header);
                CharArray footer = renderer.getFooter(session);
                ch.add(footer);
            }
            session.setLanguage(orgLanguage);
            session.setItemRenderer(orgRenderer);
           
        } else {  // not Complex
            //if (session.isMobile() && session.userAgent != null && session.userAgent.is3G()) {
            //    session.setLanguage("XHTML");
            //    session.setRenderer("XHTML");
            //    session.setItemRenderer(com.miraidesign.renderer.xhtml.XhtmlRenderer.getInstance());
            //}
            if (session.getRenderer().equals("HTML")) {
                ContentParser parser = page0.getParser();
                if (parser != null) {   // HTMLテンプレートが存在する場合
                    if (debug2) System.out.println(count+"|★MDServlet:HTMLテンプレート表示を行います");
                    CharArray chTop = mm.getTopPage();
                    CharArray base  = CharArray.pop();
                    
                    base.set("href=\"");
                    if (mm.httpURL != null && mm.httpURL.length()>0) {
                        base.add(mm.httpURL);
                    } else {
                        base.add(SystemManager.httpURL);
                    }
                    base.add(chTop); base.add("\"");
                    session.setTemplate("@BASE", base);
                                        
                    base.set("href=\"");
                    if (mm.httpsURL != null && mm.httpsURL.length()>0) {
                        base.add(mm.httpsURL);
                    } else {
                        base.add(SystemManager.httpsURL);
                    }
                    base.add(chTop); base.add("\"");
                    session.setTemplate("@HTTPS_BASE", base);
                    
                    //-- admin
                    base.set("href=\"");
                    if (mm.httpURL != null && mm.httpURL.length()>0) {
                        base.add(mm.httpURL);
                    } else {
                        base.add(SystemManager.httpURL);
                    }
                    if (!base.endsWith("/") && !mm.getAdminBase().startsWith("/")) base.add("/");
                    base.add(mm.getAdminBase()); base.add("\"");
                    session.setTemplate("@ADMIN_BASE", base);
                                        
                    base.set("href=\"");
                    if (mm.httpsURL != null && mm.httpsURL.length()>0) {
                        base.add(mm.httpsURL);
                    } else {
                        base.add(SystemManager.httpsURL);
                    }
                    if (!base.endsWith("/") && !mm.getAdminBase().startsWith("/")) base.add("/");
                    base.add(chTop); base.add("\"");
                    session.setTemplate("@HTTPS_ADMIN_BASE", base);
                    
                    //-- usr
                    base.set("href=\"");
                    if (mm.httpURL != null && mm.httpURL.length()>0) {
                        base.add(mm.httpURL);
                    } else {
                        base.add(SystemManager.httpURL);
                    }
                    if (!base.endsWith("/") && !mm.getUserBase().startsWith("/")) base.add("/");
                    base.add(mm.getUserBase()); base.add("\"");
                    session.setTemplate("@USER_BASE", base);
                                        
                    base.set("href=\"");
                    if (mm.httpsURL != null && mm.httpsURL.length()>0) {
                        base.add(mm.httpsURL);
                    } else {
                        base.add(SystemManager.httpsURL);
                    }
                    if (!base.endsWith("/") && !mm.getUserBase().startsWith("/")) base.add("/");
                    base.add(chTop); base.add("\"");
                    session.setTemplate("@HTTPS_USER_BASE", base);
                    
                    CharArray.push(base);
                    session.setTemplate("@TITLE",title);
                    
                    if (SystemManager.httpURL.indexOf("//localhost") > 0 ||
                        SystemManager.httpURL.indexOf("//192.168.") > 0) {
                        // do notiong
                        /*if (debugSession)*/ session.setTemplate("@BODY");
                    } else {
                        session.setTemplate("@BODY");
                    }
                    
                    if (parser.isEnabled()) {
                        page0.showTemplate(session);
                        parser.setTemplate(session);
                        parser.draw(session);
                    } else {
                        for (int i = 0; i < queue.size(); i++) {
                            PageServlet page = (PageServlet)queue.peek(i);
                            page.draw(session);
                        }
                    }
                } else {  // HTMLテンプレートが存在しない場合
                    CharArrayQueue meta = page0.getMeta();           // メタデータ
                    CharArray styleSheet = page0.getStyleSheet();    // スタイルシート
                    CharArrayQueue styleSheetURL = page0.getStyleSheetURL(session.getCarrier()); // スタイルシートURL
                    ch.add("<html>\n");
                    ch.add("<head>\n");
                    if (contentType.length() > 0) {
                        ch.add("  <meta http-equiv=\"Content-Type\" content=\""+contentType+"\" />\n");
                    } else if (charSet.length() > 0) {
                        ch.add("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset="+charSet+"\" />\n");
                    } else {
                        ch.add("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=Shift_JIS\" />\n");
                    }
                    ch.add("  <meta http-equiv=\"Pragma\" content=\"no-cache\" />\n");
                    ch.add("  <meta http-equiv=\"Cache-control\" content=\"private\" />\n");
                    for (int i = 0; i < meta.size(); i++) {
                        ch.add("  <meta "); ch.add(meta.peek(i)); ch.add("/>\n");
                    }
                    if (title.length() > 0) {
                        ch.add("  <title>"); ch.add(title); ch.add("</title>\n");
                    }
                    // スタイルシート設定
                    for (int i = 0; i < styleSheetURL.size(); i++) {
                        ch.add("  <link rel=\"stylesheet\" href=\""); 
                        ch.add(styleSheetURL.peek(i)); 
                        ch.add("\" type=\"text/css\" />\n");
                    }
                
                    if (styleSheet.length() > 0) {
                        ch.add("  <style type=\"text/css\">\n");
                        ch.add(styleSheet);
                        ch.add("  </style>");
                    }
                    ch.add("</head>\n");
                    ch.add("<body");
                    ch.add(page0.getBodyTag());
                    ch.add(">\n");
                    if (debugSessionID) {
                        ch.format(sessionID);
                        ch.add("<br>\n");
                    }
                    for (int i = 0; i < queue.size(); i++) {
                        PageServlet page = (PageServlet)queue.peek(i);
                        page.draw(session);
                    }
                    ch.add("</body></html>");
                }
            } else if (session.getRenderer().equals("XML")) {
                // do nothing 2010-04-07
            } else if (session.getRenderer().equals("XHTML")) {
                if (debugComplex) System.out.println(count+"|★MDServlet:XHTMLテンプレート表示を行います");
                ContentParser parser = page0.getParser();
                if (parser != null && !parser.isEnabled()) {
                    for (int i = 0; i < queue.size(); i++) {
                        PageServlet page = (PageServlet)queue.peek(i);
                        page.draw(session);
                    }
                } else {
                    HttpServletResponse res = SystemManager.allowResponseChange ? session.response : response;
                    res.setHeader("Expires", "0");
                    res.setHeader("Pragma","no-cache");
                    res.setHeader("Cache-Control","no-cache");

                    ch.add("<?xml version=\"1.0\" encoding=\"Shift_JIS\"?>\n");
                    ch.add("<!DOCTYPE html");
                    ch.add(" PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"");
                    ch.add(" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n");
                    ch.add("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"ja\" lang=\"ja\">\n");
                    ch.add("<head>\n");
                    ch.add("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=Shift_JIS\" />\n");
                    ch.add("<meta http-equiv=\"Pragma\" content=\"no-cache\" />\n");
                    //ch.add("<meta http-equiv=\"Cache-control\" content=\"private\" />\n");
                    ch.add("<meta http-equiv=\"Cache-control\" content=\"no-cache\" />\n");
                
                    if (title.length() > 0) {
                        ch.add("<title>"); ch.add(title); ch.add("</title>\n");
                    }
                    ch.add("</head>\n");
                    
                    ch.add("<body>\n");
                    for (int i = 0; i < queue.size(); i++) {
                        PageServlet page = (PageServlet)queue.peek(i);
                        page.draw(session);  // HTML読み込みは行わない
                    }
                    ch.add("</body>\n");
                    ch.add("</html>");
                }
            } else {
                System.out.println(count+"|★MDServlet:レンダラーが不明です");
                ch.set("Unknown renderer!");
            }
        } // ! isComplex
        
        //@@// 文字化けの対応をする
        OutputStream outStream = null;
        try {
            HttpServletResponse res = SystemManager.allowResponseChange ? session.response : response;
            outStream = res.getOutputStream();
        } catch (Exception e) {
System.out.println(count+"|stream 取得エラー(2)："+e);
            System.out.println(count+"|MDServlet:can not output!!");
            outStream = null;
        }
        
        if (outStream != null && session != null) {
            HashParameter hp = session.getUserTemplate();  // ユーザー定義による置換
            if (hp != null) {
                for (int i = 0; i < hp.size(); i++) {
                    CharArray key1   = (CharArray)hp.keyElementAt(i);
                    CharArray value1 = (CharArray)hp.valueElementAt(i);
if (debugTemplate) System.out.println("replace1["+key1+"]"+value1);
                    ch.replace(key1, value1);
                }
            }
            if (mm != null) {
                HashVector hv = mm.getUserTemplate();  // site.ini 定義による置換
                if (hv != null) {
                    for (int i = 0; i < hv.size(); i++) {
                        CharArray key1   = (CharArray)hv.keyElementAt(i);
                        CharArray value1 = ((CharArrayQueue)hv.valueElementAt(i)).peek();
                        ch.replace(key1, value1);
if (debugTemplate) System.out.println("replace2["+key1+"]"+value1);
                    }
                }
            }

            HashVector hv = SystemManager.getUserTemplate();  // system.ini 定義による置換
            if (hv != null) {
                for (int i = 0; i < hv.size(); i++) {
                    CharArray key1   = (CharArray)hv.keyElementAt(i);
                    CharArray value1 = ((CharArrayQueue)hv.valueElementAt(i)).peek();
                    ch.replace(key1, value1);
if (debugTemplate) System.out.println("replace3["+key1+"]"+value1);
                }
            }

            // サイト置換を再度行う
            if (mm != null) mm.replaceTemplate(ch);

            // 動的置換処理を行う  css, gif 等の変換に使用する
            CharArray tmp = session.getThemeData();     // セッションテーマの変換
            if (tmp != null && tmp.length() > 0) {
                ch.replace("@SESSION_THEME", tmp);
                ch.replace("@THEME", tmp);
            } else if (mm != null) {
                tmp = mm.getTheme();
                if (tmp != null && tmp.length() > 0) {
                   ch.replace("@THEME",tmp);
                }
            }
            // @LANG の置換
            CharArray chLang = session.getLangData();
            if (chLang == null || chLang.length() <= 0) chLang = mm.getLangData();
            if (chLang != null && chLang.length() > 0) ch.replace("@LANG", chLang);
if (debugLang) System.out.println("@LANG="+chLang);

            //@COUNTRYの置換
            CharArray cc = session.getCountryCode();
            if (cc !=null && cc.length() > 0) ch.replace("@COUNTRY", cc);
if (debugLang) System.out.println("@COUNTRY="+cc);
            
            // @ROLEの置換
            CharArray chRole = session.getRole();               // Roleの変換
            ch.replace("@ROLE", chRole);

            // メッセージ、色情報置換
            session.replaceTemplate(ch);
            
            // コンテントタイプ変換
            if (contentType.length() > 0) ch.replace("@CONTENT_TYPE", contentType);
            if (charSet.length() > 0)     ch.replace("@CHAR_SET", charSet);
            
            // デバッグ用だが当面有効にする
            if (session != null) {
                ch.replace("@HOST_URL", session.getHostURL());
                ch.replace("@HTTP_URL", session.getHttpURL());
                ch.replace("@HTTPS_URL", session.getHttpsURL());
                ch.replace("@REGIST_URL", session.getRegistURL());
                ch.replace("@CACHE_SERVER_URL", session.getCacheServerURL());
                ch.replace("@SESSION_ID", ""+session.getSessionID());
                ch.replace("@COOKIE_ID", ""+session.cookieID);
                
                SiteMapping map = mm.getSiteMapping();
                String _str = null;
                if (map != null) {
                    _str = map.getSiteKey(session).toString();
                }
                if (_str == null) {
                    _str = mm.getServerKey();
                }
                ch.replace("@SERVER_KEY", _str);
                
                Calendar cal = Calendar.getInstance();
                int year  = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH)+1;
                int date  = cal.get(Calendar.DATE);
                
                ch.replace("@SERVER_YEAR", ""+ year);
                ch.replace("@SERVER_MONTH", (month > 9) ? ""+ month : "0"+month);
                ch.replace("@SERVER_DATE", (date > 9) ? ""+ date : "0"+date);
            }
        }
        String charCode="UTF8"; //"MS932";
        
        String s = session.getCharCode();
        if (s.length()> 0) charCode = s;
        
if (debugChar) {
    System.out.println(count+"|charCode:"+charCode);
    System.out.println(count+"|charSet:"+charSet);
}
        if (charCode.equals("MS932") || charSet.indexOf("JIS")>=0) { 
            //ch.toCp932();
        }

        if (outStream != null) {
            boolean redirect_sts = true;
            HttpServletResponse res = SystemManager.allowResponseChange ? session.response : response;
            try {
                if (contentType.length() > 0) {
                    res.setContentType(contentType);
                    res.setHeader("Content-Type",contentType);
if (debug) System.out.println(count+"|○●Content-Type:"+contentType+" charCode:"+charCode);
                }
                CharArray _mt = new CharArray(page0.getMimeType());
                if (_mt.length() > 0) {
if (debug) System.out.println(count+"|○●MimeType:"+_mt+" charCode:"+charCode);
                     _mt.add("; ");
                     _mt.add(charCode);
                     res.setContentType(_mt.toString());
                     res.setHeader("Content-Type",_mt.toString());
                }
                
                byte[] bOut = ch.getBytes(charCode);
                if (outputContentLength && session.outputContentLength) res.setContentLength(bOut.length);
            
                outStream.write(bOut);
            } catch (IOException ex0) {
                 CharArray ch3 = CharArray.pop(ex0.toString());
                 ch3.replace("Exception","Ex.");
                 log.warn(count+"|MDServlet(streamwrite):"+ch3);
                 CharArray.push(ch3);
                 redirect_sts = sendRedirect("EXCEPTION",res, mm, count); //@@//
            } catch (Exception ex) {
                CharArray _mes = CharArray.pop(ex.toString());
                if (debug) _mes.replace("Exception","Ex");
                System.out.println(count+"| MDServlet(stream write):"+_mes);
                if (debugStreamException) ex.printStackTrace();
                CharArray.push(_mes);
                redirect_sts = sendRedirect("EXCEPTION", res, mm, count); //@@//
            }
            if (redirect_sts) {
                try {
                    outStream.flush();
                    outStream.close();
                } catch (Exception ex) {
                    CharArray ch3 = CharArray.pop(ex.toString());
                    ch3.replace("Exception","Ex.");
                    log.warn(count+"|MDServlet(flush/close):"+ch3);
                    CharArray.push(ch3);
                }
            }
        }
        if (mm != null) mm.loader.drawEnd(session);
        SystemManager.loader.drawEnd(session);
        
        if (!session.getParameter(SystemConst.afterPageKey).equals("none")) {
            for (int i = 0; i < queue.size(); i++) {
                PageServlet page = (PageServlet)queue.peek(i);
                page.afterPage(session);
            }
            if (queue.size() > 0) {
                
            }
        }
    }
    
    // alled from sesionObject
    public void setValueToItem(SessionObject session, int count) {
        setValueToItem(session, session.mixedID, session.hashParameter, session.hashFileData, count);
    }
    
    // 
    protected void setValueToItem(SessionObject sessionObject, int mixedID,
                                  Hashtable hashParameter,
                                  Hashtable hashFileData, //  ファイルデータ
                                  int count) {     
        setValueToItem(sessionObject, mixedID ,hashParameter, count);
        if (hashFileData.size() <= 0) return;

        int sessionID = sessionObject.getSessionID();
        mixedID /= 1000;  mixedID *= 1000;
        if (debug) System.out.println(count+"|setFileData start----------------------------");
        CharToken token = CharToken.pop();
        for (Enumeration e = hashFileData.keys(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            if (key.startsWith(SystemConst.itemKey)) {
                ItemData itemData = null;
                int itemNo = 0;
                int subNo = 0;
                int index = key.indexOf(SystemConst.dataKey);
                if (index > 0) {    // DynamicData
                    if (debug) System.out.println(count+"|MDServlet#setValueToItem() DynamicmDataの処理開始");
                    int i = SystemConst.itemKey.length();
                    int j = SystemConst.dataKey.length();
                    String str1 = key.substring(i,index-i+1);
                    String str2 = key.substring(index+j);
                    if (debug) System.out.println(count+"|key:"+key+" str1["+str1+"] str2["+str2+"]");
                    
                    if (debug) System.out.println(count+"|  ▽MDServlet#setValueToItem() DynamicDataの処理:"+str1);
                    itemNo = CharArray.getInt(str1);
                    ItemData parent = SessionManager.getItemData(sessionObject, mixedID+itemNo);   //@@@//
                    if (parent == null) {
                        if (debug) System.out.println(count+"|  ？DynamicDataの処理:親が存在しません mixedID="+mixedID);
                        
                    } else if (parent instanceof DynamicItemData) {
                        token.set(str2, SystemConst.dataKey);
                        if (debug) {
                            for (int ii = 0; ii < token.size(); ii++) {
                                System.out.println(count+"|  token["+ii+"]"+token.get(ii));
                            }
                        }
                        ObjectQueue queue = ((DynamicItemData)parent).getQueue();
                        for (int ii = 0; ii < token.size(); ii++) {
                            subNo = token.getInt(ii);
                            if (ii + 1 < token.size()) {
                                ItemData id = (ItemData)queue.peek(subNo);
                                if (id instanceof DynamicItemData) {
                                    queue = ((DynamicItemData)id).getQueue();
                                } else {
                                    System.out.println(count+"| ? DynamicItemData ではありません! "+subNo);
                                    break;
                                }
                            } else {
                                itemData = (ItemData)queue.peek(subNo);
                            }
                        }
                    } else {
                        if (debug) System.out.println(count+"|  ？DynamicDataの処理:親がDYNAMICでありません："+parent.getTypeName());
                        itemData = parent;
                    }
                    if (debug) System.out.println(count+"|  △DynamicDataの処理終了:");
                } else {
                    itemNo = CharArray.getInt(key.substring(SystemConst.itemKey.length()));
                    itemData = SessionManager.getItemData(sessionObject,mixedID+itemNo);    //@@@//
                    if (debug && debugSetValue) System.out.println(count+"|MDServlet#setValueToItem() 通常Itemの処理:"+key);
                }
                if (itemData == null) {
                    System.out.println(count+"|*** itemNo="+itemNo+" key:"+key+" のitemDataが見つかりません");
                } else if (itemData.getType() == Item.FILE) {
                    FileData fileData = (FileData)itemData;
                    CharArray filename = fileData.getOriginalOutputFilename();
                    CharArray path = fileData.getPath();
                    UploadInfo ui  = (UploadInfo)hashFileData.get(key);
                    if (ui != null && filename.length() > 0 && !filename.endsWith("/")) {  // ファイルに書き込む
                        try {
                            fileData.setContentType(ui.contentType);
                            fileData.setRowCount(ui.rowCount);
                            fileData.setUploadFilename("");
                            fileData.clearStatus();
                            if (path.length() > 0) {    // ディレクトリがなければ作成
                                File file = new File(path.toString());
                                if (!file.exists()) file.mkdirs();
                            }
                            int MAX = 64;  // 最大ファイル名
                            
                            CharArray org = fileData.getFilename().trim();
                            if (debugUpload) System.out.println(count+"|OrgFilename:"+org);
                            if (!Validation.isAscii(org)) {    // 全角文字が交じっている！
                                CharArray ch = Validation.convertAscii(org);
                                org.set(ch);
                                fileData.setStatus(0x04,true);
                                if (debugUpload) System.out.println(count+"|AsciiFilename:"+org);
                            }
                            org.replace(" ","_");
                            int str_len = Validation.strlen(org);
                            if (str_len >  MAX) {
                                int remove_size = str_len - MAX;
                                int idx = org.lastIndexOf('.');
                                if (idx > 0) org.remove(idx-remove_size,remove_size);
                                else org.length -= remove_size;
                                fileData.setStatus(0x02,true);
                                if (debugUpload) System.out.println(count+"|NewFilename:"+org);
                            }
                            fileData.setUploadFilename(org);
                            
                            filename = ((FileData)itemData).getOutputFilename();
                            if (ui.length > 0) {
                                FileOutputStream out = new FileOutputStream(filename.toString());  // 出力先
                                out.write(ui.bytes,0,ui.length);
                                out.close();
                                fileData.setStatus(0x01,true);
                            }
                            if (debugUpload) System.out.println(count+"|Output FILE:"+filename+" write:"+ui.length+" bytes");
                        } catch (Exception ex) {
                            fileData.setStatus(0x01,false);
                            if (debugUpload) System.out.println(count+"|ERROR-FILE:"+filename+" write:"+ui.length+" bytes");
                            ex.printStackTrace();
                        }
                    }
                    
                    hashFileData.remove(key);   // クリアしておく
                    ui = null;
                }
            }
        } // next
        CharToken.push(token);
        if (debug) System.out.println(count+"|setFileData end------------------------------");
    }
    
    protected void setValueToItem(SessionObject sessionObject, int mixedID,Hashtable hashParameter, int count) {
        int sessionID = sessionObject.getSessionID();
        mixedID /= 1000;  mixedID *= 1000;
        if (debugSetValue) System.out.println(count+"| ▼setValue start------------------------------");
        CharToken token = CharToken.pop();
        // CheckBoxクリア用
if (debugCheckbox) System.out.println(count+"|  ▼ CheckBox クリア開始 ---");
        for (Enumeration e = hashParameter.keys(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            if (key.startsWith(SystemConst.clearKey+SystemConst.itemKey)) {
                String str = key.substring(SystemConst.clearKey.length()+SystemConst.itemKey.length());
                token.set(str, SystemConst.dataKey);
                ItemData itemData = SessionManager.getItemData(sessionObject,mixedID+token.getInt(0));  //@@@//
                if (itemData.getType() == Item.CHECKBOX) {
                    ((CheckBoxData)itemData).clearSelection(true);
                    if (debugSetValue || debugCheckbox) System.out.println(count+"|  >>CheckBox のデータをクリアします");
                } else if (itemData instanceof DynamicItemData) {
                    ObjectQueue queue = ((DynamicItemData)itemData).getQueue();
                    for (int ii = 1; ii < token.size(); ii++) {
                        int subNo = token.getInt(ii);
                        if (ii + 1 < token.size()) {
                            itemData = (ItemData)queue.peek(subNo);
                            if (itemData instanceof DynamicItemData) {
                                queue = ((DynamicItemData)itemData).getQueue();
                            } else {
                                System.out.println(count+"| ? DynamicItemData ではありません! "+subNo);
                                break;
                            }
                        } else {
                            itemData = (ItemData)queue.peek(subNo);
                        }
                    }
                    if (itemData != null && itemData.getType() == Item.CHECKBOX) {
                        ((CheckBoxData)itemData).clearSelection(true);
                        if (debugSetValue || debugCheckbox) System.out.println(count+"|  >>CheckBox のデータをクリアします");
                    }
                }
            }
        } // next
if (debugCheckbox) System.out.println(count+"|  ▲ CheckBox クリア終了 ---");
        int iii = 0;
        for (Enumeration e = hashParameter.keys(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            if (key.startsWith(SystemConst.itemKey)) {
                ItemData itemData = null;
                int itemNo = 0;
                int subNo = 0;
                int index = key.indexOf(SystemConst.dataKey);
                if (index > 0) {    // DynamicData
                    if (debugSetValue) System.out.println(count+"|MDServlet#setValueToItem() DynamicmDataの処理開始");
                    int i = SystemConst.itemKey.length();
                    int j = SystemConst.dataKey.length();
                    String str1 = key.substring(i,index-i+1);
                    String str2 = key.substring(index+j);
                    if (debugSetValue) System.out.println(count+"|key:"+key+" str1["+str1+"] str2["+str2+"]");
                    
                    if (debugSetValue) System.out.println(count+"|  ▼MDServlet#setValueToItem() DynamicDataの処理:"+str1);
                    itemNo = CharArray.getInt(str1);
                    ItemData parent = SessionManager.getItemData(sessionObject, mixedID+itemNo);
                    if (parent == null) {
                        if (debugSetValue) System.out.println(count+"|  ？DynamicDataの処理:親が存在しません mixedID="+mixedID);
                        
                    } else if (parent instanceof DynamicItemData) {
                        token.set(str2, SystemConst.dataKey);
                        if (debugSetValue) {
                            for (int ii = 0; ii < token.size(); ii++) {
                                System.out.println(count+"|  token["+ii+"]"+token.get(ii));
                            }
                        }
                        ObjectQueue queue = ((DynamicItemData)parent).getQueue();
                        for (int ii = 0; ii < token.size(); ii++) {
                            subNo = token.getInt(ii);
                            if (ii + 1 < token.size()) {
                                ItemData id = (ItemData)queue.peek(subNo);
                                if (id instanceof DynamicItemData) {
                                    queue = ((DynamicItemData)id).getQueue();
                                } else {
                                    System.out.println(count+"| ? DynamicItemData ではありません! "+subNo);
                                    break;
                                }
                            } else {
                                itemData = (ItemData)queue.peek(subNo);
                            }
                        }
                    } else {
                        if (debugSetValue) System.out.println(count+"|  ？DynamicDataの処理:親がDYNAMICでありません："+parent.getTypeName());
                        itemData = parent;
                    }
                    if (debugSetValue) System.out.println(count+"|  ▲DynamicDataの処理終了:");
                } else {
                    itemNo = CharArray.getInt(key.substring(SystemConst.itemKey.length()));
                    itemData = SessionManager.getItemData(sessionObject, mixedID+itemNo);
                    if (debugSetValue) System.out.println(count+"|MDServlet#setValueToItem() 通常Itemの処理:"+key);
                }
                if (itemData == null) {
                    System.out.println(count+"|*** itemNo="+itemNo+" key:"+key+" のitemDataが見つかりません");
                } else if (itemData.isInput() || itemData.isSelect()) { // setするのはisInputのみ
                    int type = itemData.getType();
                    if (type == Item.SUBMIT || type == Item.IMAGE || type == Item.BUTTON) {
                        if (debugSetValue) System.out.println(count+"|↑クリックされたItemDataです:"+itemData.getTypeName());
                        sessionObject.clickedItemData = itemData;
                        sessionObject.clickedItem     = itemData.getItem();
                    } else {
                        // clickItem のデータセットをやめる
                        String[] value = (String[])hashParameter.get(key);
                        if (debugSetValue) {
                            System.out.print(count+"|*** itemNo="+itemNo+((subNo>0)? "."+subNo:"")+
                                " "+itemData.getItem().getTypeName()+" setValue");
                            for (int i = 0; i < value.length; i++) {
                                System.out.print(":"+value[i]);
                            }
                            System.out.println("("+itemData.getTypeName()+")にデータをセットします");
                        }
                        itemData.setValue(value);
                    }
                }
            }
        }
        CharToken.push(token);
        if (debugSetValue) System.out.println(count+"| ▲setValue end------------------------------");
    }

    /** 別ページへリダイレクトする */
    public boolean sendRedirect(String key,HttpServletResponse response, int count) {
        return sendRedirect(key, response, null,count);
    }
    /** サイト指定の別ページへリダイレクトする
        @param key  リダイレクトキーワード
        @param response 
        @param mm   ModuleManager  nullの場合はデフォルトを使用
    */
    public boolean sendRedirect(String key,HttpServletResponse response, ModuleManager mm, int count) {
        boolean sts = true;
        String mes = key.equals("EXCEPTION") ? "例外発生" : key;
if (debugChar) {
    System.out.println(count+"＃ContentType:"+response.getContentType());
    System.out.println(count+"＃CharacterEncoding:"+response.getCharacterEncoding());
}
if (debug2) System.out.println(count+"|sendRedirect "+mes+((mm != null) ? " "+mm.getSiteKey()+"/"+mm.getChannelName() :""));
        CharArray ch = SystemManager.ini.get("[Redirect]",key);
        CharArray ch2 = null;
        if (mm != null) {   // モジュール
            ch2 = mm.ini.get("["+mm.sectionBase+".Redirect]",key);
            if (ch2 != null) {
                if (ch2.trim().length()>0) {
                    try {
                        mm.replaceTemplate(ch2);
                        response.sendRedirect(ch2.toString());
if (debug2) System.out.println(count+"|   -> "+ch2);
                    } catch (Exception ex) {
                        CharArray ch3 = CharArray.pop(ex.toString());
                        ch3.replace("Exception","Ex.");
                        log.warn(count+"|MDServlet(sendRedirect):"+ch3);
                        CharArray.push(ch3);
                        sts = false;
                   }
                }
            }
        }
        if (ch2 == null) {
            if (ch != null && ch.trim().length()>0) {
                try {
                    response.sendRedirect(ch.toString());
if (debug2) System.out.println(count+"|   -> "+ch);
                } catch (Exception ex) {
                    CharArray ch3 = CharArray.pop(ex.toString());
                    ch3.replace("Exception","Ex.");
                    log.warn(count+"|MDServlet(sendRedirect):"+ch3);
                    CharArray.push(ch3);
                    sts = false;
               }
            }
        }
        return sts;
    }
}

//
//
// [end of MDServlet.java]
//

