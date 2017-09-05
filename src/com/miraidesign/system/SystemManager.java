//------------------------------------------------------------------------
//@(#)SystemManager.java
//      システム全体の管理
//                 Copyright (c) Mirai Design 2010-17 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.system;

import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.net.InetAddress;

//import javax.xml.transform.*;
//import javax.xml.transform.stream.*;

import com.miraidesign.common.Const;
import com.miraidesign.common.SystemConst;
import com.miraidesign.common.Version;

import com.miraidesign.system.GCManager;

import com.miraidesign.data.DataSourceManager;
import com.miraidesign.mail.SendMail;
import com.miraidesign.image.BarCodeGenerator;
import com.miraidesign.image.QRCodeGenerator;
import com.miraidesign.image.ImageProxy;
import com.miraidesign.image.ImageConverter;
//import com.miraidesign.image.EmojiConverter;  // Aug.2017 絵文字利用を中止する
import com.miraidesign.session.UserAgent;
import com.miraidesign.session.SessionManager;
import com.miraidesign.session.SessionKey;

import com.miraidesign.servlet.ServletLog;
import com.miraidesign.servlet.Loader;
import com.miraidesign.servlet.ContentRenderer;
import com.miraidesign.servlet.AbstractServlet;
import com.miraidesign.servlet.MDServlet;

import com.miraidesign.util.Util;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.IniFile;
import com.miraidesign.util.ExceptionWriter;


/**
 *  システム全体の管理を行います。
 *  
 *  @version 0.5 2010-04-05
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class SystemManager extends AbstractSystemManager {
    static private boolean debug = (SystemConst.debug && true);  // デバッグ表示
    static private boolean debugSystem = (SystemConst.debug && true);  // デバッグ表示
    static private boolean debugIP = (SystemConst.debug && false);  // デバッグ表示
    
    /** システムバージョン */
    static int version = Version.version;
    /** システム起動時刻 */
    static public long startupTime = System.currentTimeMillis();
    static public String szStartupTime = "";
    static public SimpleDateFormat sdfz = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)");
    static {
        java.util.Date date = new java.util.Date(startupTime);
        szStartupTime = sdfz.format(date);
        if (szStartupTime.indexOf("(JST") < 0) {
            TimeZone timeZone = sdfz.getTimeZone();
            szStartupTime += " ";
            sdfz.setTimeZone(TimeZone.getTimeZone("JST"));
            szStartupTime += sdfz.format(date);
            sdfz.setTimeZone(timeZone);
        }
    }
    
    // システム設定ファイル  -----------------------------------------------------
    static public  IniFile ini;     // web.xml で指定
    
    static public String hostName = "";
    static public String hostURL = "";
    static public String httpURL = "";
    static public String httpsURL = "";
    static public String registURL = "";
    static public String cacheServerURL = "";   // キャッシュサーバーURL
    static public String serverKey = "";        // LB振り分け等のサーバ識別キーワード
    static public String docRoot = "";
    static public String servletPath = "";
    static public String checkerPath = "/sc/check";
    static public String contentType = "";
    static public String charSet = "";
    static public String charCode = "";
    
    static public String resourceDirectory = "";    // リソースディレクトリ
    static public String tempDirectory = "";        // テンポラリディレクトリ

    static public int maxRenderingSize = 5000;  // 最大レンダリング文字数

    // Session関連 -------------------------------------------------------------
    static public String poolingDirectory;
    static public int sessionTimeOutSec = 60;   // セッションタイムアウト(分）
    static public int accessMaxSec = 180;       // アクセス最大時間（分)
    static public int poolingTime = 48;         // セッションをプールに保管する最大時間
    static public int maxSessionCount = 0;      // 同時に存在できるセッション数(0で無制限)

    static public int pcCookieMaxHour   = 48;   // PC判別用クッキー情報の最大保持時間
    static public int cartCookieMaxHour = 48;   // カートクッキー情報の最大保持時間

    static public Loader loader = new Loader();

    public  static ServletLog log;
    private static SystemManager systemManager;     // singleton

    // IPCheck
    static private CharArrayQueue allowIP = new CharArrayQueue();
    static private CharArrayQueue denyIP = new CharArrayQueue();
    static private CharArrayQueue sessionAllowIP = new CharArrayQueue();
    static private CharArrayQueue robotIP = new CharArrayQueue();
    //-------------------------------------------------------------------------
    static public boolean allowResponseChange = true;  // 異常時等のレスポンスの差し替えを有効にする

    static public int convertContextPath = 0;   // コンテキストパスの変換を行うか？
                                                // 0:行わない
                                                // 1:変換モード１  /param=data/ 形式にする
    
    static public boolean appendAuthID = false; // 認証IDを付加するか？
    
    static public ContentRenderer contentRenderer;
    
    /** ユーザ指定テンプレートを返す */
    static public HashVector<CharArray, CharArrayQueue> getUserTemplate() { 
        return ini.getKeyTable("[UserTemplate]");
    }
    
    static public int init_error = 0;   // 起動時のエラー回数
    static public CharArrayQueue init_error_queue = new CharArrayQueue();
    //-------------------------------------------------------------------------
    // consuructor
    //-------------------------------------------------------------------------
    private SystemManager() {  
        
    }
    static public SystemManager getInstance(String config) {
        if (systemManager == null) {
            systemManager = new SystemManager();
            
            CharArray ch = CharArray.pop(config);
            ch.convertProperty(false);
            ch.replace("\\","/");
            String szConfig = ch.toString();
            String encode = null;
            CharToken token = CharToken.pop();
            token.set(ch, ",");
            if (token.size() == 2) {
                szConfig = token.get(0).toString();
                encode   = token.get(1).trim().toString();
            } else {
            }
            CharToken.push(token);
            CharArray.push(ch);
System.out.println("config:"+config+"\n -> "+szConfig);
            ini = new IniFile(szConfig,"=","#","\\");
            ini.setInclude("#include");
            if (encode != null && encode.length() > 0) ini.setEncoding(encode);
            ini.read();
            
            init(szConfig);
        }
        return systemManager;
    }
    
    
    static public void init(String config) { 
System.out.println("●config["+config+"]●");
        if (ini.isOK()) {
            try {
                CharArray ch = null;
                //SimpleDateFormat sdfz = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)");
                java.util.Date date = new java.util.Date(System.currentTimeMillis());
                if (debug) System.out.println("------SystemManager初期化開始   "+sdfz.format(date)+"------");
                if (debug) System.out.println(checkMemory().toString());
                //------------------------------------------------------- [Debug]
                SystemConst.debug = ini.getBoolean("[Debug]","SystemOut");
                debug &= SystemConst.debug;
                
                //-------ログ------------------------------------- [Log][Message]
                log = ServletLog.getInstance();
                log.init(ini);
                log.setHostName(hostName);      //@@// この時点ではhostNameが空だが、、
                //------------------------------------------------------- [System]
                if (debug) System.out.println("config="+config+" OK");
                String s = ini.getString("[System]","TimeZone");
                if (s != null) {
                    System.setProperty("user.timezone",s);
                    if (debug) {
                        sdfz.setTimeZone(TimeZone.getTimeZone("JST"));
                        System.out.println("------TimeZoneを"+s+"に変更します "+sdfz.format(date)+"------");
                    }
                }
                
                if (debugSystem || ini.getBoolean("[Debug]","DisplaySystemProperties")) {
                    System.getProperties().list(System.out); // プロパティを表示する
                    if (debug) System.out.println("-------------------------------------");
                }
                InetAddress host = null;
                String err_msg="";
                try {
                    err_msg = "InetAddress.getLocalHost()";
                    host = InetAddress.getLocalHost();
if (debugSystem) {
    System.out.println("InetAddress.getLocalHost:"+host);
                    err_msg = "host.getHostAddress()";
    System.out.println("InetAddress.getHostAddress:"+host.getHostAddress());
}
                    err_msg = "host.getHostName()";
                    hostName = host.getHostName();
                    if (debug) System.out.println("InetAddress.getHostName:"+hostName);
                    log.setHostName(hostName);      //@@//
                } catch (Exception ex) {
                    ++init_error;
                    System.out.println(ex);
                    init_error_queue.enqueue(err_msg+" exception:"+ex);
                    ex.printStackTrace();
                }
                
                hostURL = ini.getString("[System]","HostURL").trim();
                if (host != null) {
                    if (hostURL.length() == 0) hostURL = "http://"+host.getHostAddress();
                    else if (hostURL.equals("http://"))   hostURL += host.getHostAddress();
                    else if (hostURL.equals("https://"))  hostURL += host.getHostAddress();
                }
                if (debug) System.out.println("HostURL:"+hostURL);
                
                httpURL = ini.getString("[System]","HttpURL").trim();
                if (host != null) {
                    if (httpURL.length() == 0) httpURL = "http://"+host.getHostAddress();
                    else if (httpURL.equals("http://"))   httpURL += host.getHostAddress();
                    else if (httpURL.equals("https://"))  httpURL += host.getHostAddress();
                }
                if (debug) System.out.println("HttpURL:"+httpURL);
                
                httpsURL = ini.getString("[System]","HttpsURL").trim();
                if (host != null) {
                    if (httpsURL.length() == 0) httpsURL = "https://"+host.getHostAddress();
                    else if (httpsURL.equals("http://"))  httpsURL += host.getHostAddress();
                    else if (httpsURL.equals("https://")) httpsURL += host.getHostAddress();
                }
                if (debug) System.out.println("HttpsURL:"+httpsURL);
                
                registURL = ini.getString("[System]","RegistURL").trim();
                if (host != null) {
                    if (registURL.length() == 0) registURL = "https://"+host.getHostAddress();
                    else if (registURL.equals("http://"))  registURL += host.getHostAddress();
                    else if (registURL.equals("https://")) registURL += host.getHostAddress();
                }
                if (debug) System.out.println("HttpsURL:"+httpsURL);
                
                cacheServerURL = ini.getString("[System]","CacheServerURL").trim();
                if (debug) System.out.println("CacheServerURL:"+cacheServerURL);
                
                
                ch = ini.get("[System]","DocRoot");
                
                ch.convertProperty();
                ch.replace("\\","/");
                
                docRoot = ch.toString();
                if (debug) System.out.println("docRoot;"+docRoot);
                servletPath = ini.getString("[System]","ServletPath");
                
                ch = ini.get("[System]","CheckPath");
                if (ch != null && ch.trim().length() > 0) {
                    checkerPath = ch.toString();
                }
                if (debug) System.out.println("CheckerPath:"+checkerPath);
                
                contentType = ini.getString("[System]","ContentType").trim();
                charSet = ini.getString("[System]","CharSet").trim();
                charCode = ini.getString("[System]","CharCode").trim();
                if (ini.get("[System]","CryptSessionID") != null) {
                    SystemConst.cryptSessionID = ini.getBoolean("[System]","CryptSessionID");
                }
                if (ini.get("[System]","CryptMixedID") != null) {
                    SystemConst.cryptMixedID = ini.getBoolean("[System]","CryptMixedID");
                }
                //-------メールデフォルト-----------------------------
                ch = ini.get("[System]","FromAddress");
                if (ch != null && ch.trim().length() > 0) {
                    SendMail.setDefaultFromAddress(ch.toString());
                    if (debug) System.out.println("From Address: "+ch);
                }
                ch = ini.get("[System]","ReplyTo");
                if (ch != null && ch.trim().length() > 0) {
                    SendMail.setDefaultReplyTo(ch.toString());
                    if (debug) System.out.println("Reply-To: "+ch);
                }
                ch = ini.get("[System]","ErrorsTo");
                if (ch != null && ch.trim().length() > 0) {
                    SendMail.setDefaultErrorsTo(ch.toString());
                    if (debug) System.out.println("Errors-To: "+ch);
                }
                //------GCManager
                ch = ini.get("[System]","SystemGC");
                if (ch != null && ch.trim().isDigit()) GCManager.setCounter(ch.getInt());
                ch = ini.get("[System]","SystemGCTimer");
                if (ch != null && ch.trim().isDigit()) GCManager.setTimer(ch.getLong());
                
                //------MultiSiteSessionMode
                ch = ini.get("[System]","MultiSiteSessionMode");
                if (ch != null) MDServlet.setMultiSiteSessionMode(ch.getBoolean());
                //------convertCSS
                ch = ini.get("[System]","ConvertCSS");
                if (ch != null && ch.trim().length() > 0) {
                    AbstractServlet.setConvertCSS(ch.getBoolean());
                }
                if (debug) System.out.println("入力時の CSS変換は"
                    +(AbstractServlet.getConvertCSS()?"ON":"OFF")+"です");
                //-----MaxRenderingSize
                ch = ini.get("[System]","MaxRenderingSize");
                if (ch != null && ch.getInt() > 1) {
                    maxRenderingSize = ch.getInt();
                }
                ch = ini.get("[System]","AllowResponseChange");
                if (ch != null && ch.trim().length() > 0) {
                    allowResponseChange = ch.getBoolean();
                    if (debug) System.out.println("異常時等のレスポンス変換を行い"
                               +(allowResponseChange ? "ます" : "ません"));
                }
                
                ch = ini.get("[System]","ConvertContextPath");
                if (ch != null && ch.trim().length() > 0) {
                    convertContextPath = ch.getInt();
                }
                if (ini.getBoolean("[System]","SessionSave")) {
                    if (debug) System.out.println("セッションを保存モードで起動します");
                }
                
                ch = ini.get("[System]","AppendAuthID");
                if (ch != null && ch.trim().length() > 0) {
                    appendAuthID = ch.getBoolean();
                }

                //-------------------------------------------------------
                SessionKey.init(ini);   // セッションキー初期化
                
                //------------------------------------------------------- [Session]
                ch = ini.get("[Session]","Directory");
                //ch.replace("$(catalina.home)",System.getProperty("catalina.home"));
                ch.convertProperty();
                ch.replace("\\","/");
                poolingDirectory = ch.toString();

                if (ini.get("[Session]","TimeOutSec") != null) {
                    sessionTimeOutSec = ini.getInt("[Session]","TimeOutSec");
                    SystemConst.sessionTimeout = sessionTimeOutSec * 60 * 1000;
                }
                if (ini.get("[Session]","AccessMaxSec") != null) {
                    accessMaxSec = ini.getInt("[Session]","AccessMaxSec");
                    SystemConst.accessMaxSec = accessMaxSec * 60 * 1000;
                }
                
                if (ini.get("[Session]","PoolingTime") != null) {
                    poolingTime = ini.getInt("[Session]","PoolingTime");
                }
                if (ini.get("[Session]","StackMaxSize") != null) {
                    int i =  ini.getInt("[Session]","StackMaxSize");
                    if (i >= 0) SessionManager.setMaxStackSize(i);
                }
                
                if (debug) {
                    System.out.println("PoolingDirectory="+poolingDirectory);
                    System.out.println("SessionTimeOut = "+sessionTimeOutSec+
                        "sec  PoolingTime = "+poolingTime+"hours");
                }
                
                ch = ini.get("[Session]","MaxCount");
                if (ch != null && ch.trim().length() > 0) {
                    maxSessionCount = ch.getInt();
                    if (maxSessionCount > 0) {
                        loadSessionIP();
                    }
                }
                
                ch = ini.get("[Session]","PCCookieMaxHour");
                if (ch != null && ch.trim().length() > 0) pcCookieMaxHour   = ch.getInt();

                if (debug) {
                    System.out.println("(system)PCCookieMaxHour="+pcCookieMaxHour);
                }
                
                //------------------------------------------------------- [Resource]
                // リソースディレクトリの取得
                ch = ini.get("[Resource]","Directory");
                //ch.replace("$(catalina.home)",System.getProperty("catalina.home"));
                ch.convertProperty();
                ch.replace("\\","/");
                resourceDirectory = ch.toString();
                int length = resourceDirectory.length();
                if (length > 0 && resourceDirectory.charAt(length-1) != '/' ) {
                    resourceDirectory += "/";
                }

                // リソースディレクトリの設定
                //ConfigFactory.setDirectory(resourceDirectory);
            
                // テンポラリディレクトリの取得 since 0.930a
                ch = ini.get("[Resource]","Temporary");
                if (ch != null) {
                    ch.convertProperty();
                    ch.replace("\\","/");
                    tempDirectory = ch.toString();
                    length = tempDirectory.length();
                    if (length > 0 && tempDirectory.charAt(length-1) != '/' ) {
                        tempDirectory += "/";
                    }
                }
                //------------------------------------------------------- [DataBase]
System.out.println("DataSourceManager.load() start-----");
                DataSourceManager.load(ini);
System.out.println("DataSourceManager.load() end-----");
                //------------------------------------------------------- [UserAgent]
                CharArray file = ini.get("[Resource]","UserAgent");
                UserAgent.init(file);       // UserAgent情報の読み込み
                
                //------------------------------------------------------- [ImageProxy]
                ch = ini.get("[ImageProxy]","ProxyMode");
                if (ch != null && ch.length()>0) ImageProxy.setMode(ch.getInt());
                ch = ini.get("[ImageProxy]","AnotherMode");
                if (ch != null && ch.length()>0) ImageProxy.setAnotherMode(ch.getInt());
                ch = ini.get("[ImageProxy]","RootDir");
                if (ch != null && ch.trim().length()>0) {
                    ch.convertProperty();
                    ch.replace("\\","/");
                    ImageProxy.setRootDir(ch.toString());
                }
                ch = ini.get("[ImageProxy]","OutputDir");
                if (ch != null && ch.trim().length()>0) {
                    ch.convertProperty();
                    ch.replace("\\","/");
                    ImageProxy.setOutputDir(ch.toString());
                }
                    //
                ch = ini.get("[ImageProxy]",Const.isWin32 ?"CommandWin":"Command");
                if (ch != null && ch.trim().length()>0) {
                    ch.convertProperty();
                    ch.replace("\\","/");
                    ImageConverter.setCommand(ch.toString());
                }
                ch = ini.get("[ImageProxy]","OptJpeg");
                if (ch != null) ImageConverter.setJpegOption(ch.toString());
                ch = ini.get("[ImageProxy]","OptBmp");
                if (ch != null) ImageConverter.setBmpOption(ch.toString());
                ch = ini.get("[ImageProxy]","OptPng");
                if (ch != null) ImageConverter.setPngOption(ch.toString());
                ch = ini.get("[ImageProxy]","OptGif");
                if (ch != null) ImageConverter.setGifOption(ch.toString());

                ch = ini.get("[ImageProxy]","OptWidth");
                if (ch != null) ImageConverter.setWidthOption(ch.toString());
                ch = ini.get("[ImageProxy]","OptGray");
                if (ch != null) ImageConverter.setGrayOption(ch.toString());
                ch = ini.get("[ImageProxy]","OptRatate");
                if (ch != null) ImageConverter.setRotateOption(ch.toString());
                ch = ini.get("[ImageProxy]","OptFlip");
                if (ch != null) ImageConverter.setFlipOption(ch.toString());
                ch = ini.get("[ImageProxy]","OptFlop");
                if (ch != null) ImageConverter.setFlopOption(ch.toString());
                
                //------------------------------------------------------- [BarCode]
                ch = ini.get("[BarCode]","Command");
                if (ch != null && ch.trim().length()>0) {
                    ch.convertProperty();
                    ch.replace("\\","/");
                    BarCodeGenerator.setCommand(ch.toString());
                }
                ch = ini.get("[BarCode]","CommandQR");
                if (ch != null && ch.trim().length()>0) {
                    ch.convertProperty();
                    ch.replace("\\","/");
                    QRCodeGenerator.setCommand(ch.toString());
                }
                ch = ini.get("[BarCode]","OutputDir");
                if (ch != null && ch.trim().length()>0) {
                    ch.convertProperty();
                    ch.replace("\\","/");
                    ImageProxy.setBarcodeDir(ch.toString());
                }
                ch = ini.get("[BarCode]","Width");
                if (ch != null && ch.trim().length()>0) BarCodeGenerator.setMinWidth(ch.getInt());
                ch = ini.get("[BarCode]","Height");
                if (ch != null && ch.trim().length()>0) BarCodeGenerator.setMinHeight(ch.getInt());
                ch = ini.get("[BarCode]","MarginH");
                if (ch != null && ch.trim().length()>0) BarCodeGenerator.setHorizontalMargin(ch.getInt());
                ch = ini.get("[BarCode]","MarginV");
                if (ch != null && ch.trim().length()>0) BarCodeGenerator.setVerticalMargin(ch.getInt());
                //------------------------------------------------------------------------------
                contentRenderer = new ContentRenderer();
                //------------------------------------------------------------------------------
                loader.load(ini.getKeyTable("[Init]"));     // 初期化モジュールのロード
if (debug) System.out.println("☆InitLoader.load()");
                loader.load();
                // ※ siteLoader load 
                //-------------------------------------------------------------------------------
                int host_count = refreshIP(true); // 関数化
                if (host_count > 0) {
                    //if (debugIP) System.out.println("refreshIP スレッドを生成します");
                    //スレッド生成
                    //refreshIP();
                }
                loadRobotIP();  // 2013-02-19
                
                //-------------------------------------------------------------------------------
                SiteManager.load(ini);
                if (debug) System.out.println(checkMemory().toString());
                
if (debug) System.out.println("DataSourceManager.init() start-----");
                DataSourceManager.init();
if (debug) System.out.println("DataSourceManager.init() end-----");
                
                //------------------------------------------------------------------------------
                //if (debug) System.out.println(checkMemory().toString());
if (debug) System.out.println("☆InitLoader.init()");
                loader.init();          // 初期化モジュールの初期化
                
                // スレッド生成処理 ------------------------------------------------------------
                SessionManager.start();
                
                if (debug) System.out.println(checkMemory().toString());
                if (debug) System.out.println("------SystemManager初期化終了------");
                ready = true;
            } catch (Exception ex) {
                ++init_error;
                System.out.println("初期化エラー");
                init_error_queue.enqueue("初期化エラー:"+ex);
                ex.printStackTrace();
            }
            
        } else {
            ++init_error;
            System.out.println("config="+config+" NG!! システムの起動を中止します");
            init_error_queue.enqueue("config="+config+" NG!! システムの起動を中止します");
        }
    }
    static public void destroy() {
        if (debug) System.out.println("------SystemManager終了処理開始------");
        if (debug) checkThread();
        SessionManager.stop();

        loader.destroy();          // 初期化モジュールのdestroy()

        if (debug) checkThread();
        if (debug) System.out.println("------SystemManager終了処理終了------");
    }
    
    /**
        sessionAllowIP を取得する
    **/
    static public void loadSessionIP() {
        synchronized (sessionAllowIP) {
            sessionAllowIP.clear();
            if (debugIP) System.out.println("★Session.AllowIP");
            HashVector<CharArray,CharArrayQueue> v = ini.getKeyTable("[Session.AllowIP]");
            if (v != null) {
                for (int i = 0; i < v.size(); i++) {
                    CharArrayQueue queue = (CharArrayQueue)v.elementAt(i);
                    for (int j = 0; j <queue.size(); j++) {
                        CharArray ch = queue.peek(j).trim();
                        System.out.println("Session.AllowIP["+ch+"]");
                        if (ch.length() > 0) {
                            CharArray ch2 = new CharArray(ch);
                            if (!ch.isDigit(".*")) { // IPじゃないので変換が必要
                                boolean flg = ch.chars[0] == '*';
                                if (flg) ch.remove(0,1);
                                try {
                                    InetAddress address = InetAddress.getByName(ch.toString());
                                    if (address == null) continue;
                                    ch2.set(address.getHostAddress());
                                    if (!flg) ch.set(ch2);
                                } catch (Exception ex) {
                                    if (debugIP) System.out.println("     ->ホストが取得できません");
                                    continue;
                                }
                                if (debugIP) System.out.println("     ->"+ch2);
                                if (ch2.length() == 0) continue;
                            }
                            int index = ch2.indexOf('*');
                            if (index < 0) {
                                sessionAllowIP.enqueue(ch2);
                            } else if (index > 0) {
                                ch2.length = index;
                                sessionAllowIP.enqueue(ch2);
                            }
                        }
                    }
                }
            }
            if (debugIP) sessionAllowIP.dumpQueue();
        }
    }
    /**
        robotIP を取得する
    **/
    static public void loadRobotIP() {
        synchronized (robotIP) {
            robotIP.clear();
            if (debugIP) System.out.println("★RobotFilter.IP");
            HashVector<CharArray,CharArrayQueue> v = ini.getKeyTable("[RobotFilter.IP]");
            if (v != null) {
                for (int i = 0; i < v.size(); i++) {
                    CharArrayQueue queue = (CharArrayQueue)v.elementAt(i);
                    for (int j = 0; j <queue.size(); j++) {
                        CharArray ch = queue.peek(j).trim();
                        System.out.println("RobotFilter.IP["+ch+"]");
                        if (ch.length() > 0) {
                            CharArray ch2 = new CharArray(ch);
                            if (!ch.isDigit(".*/")) { // IPじゃないので変換が必要
                                boolean flg = ch.chars[0] == '*';
                                if (flg) ch.remove(0,1);
                                try {
                                    InetAddress address = InetAddress.getByName(ch.toString());
                                    if (address == null) continue;
                                    ch2.set(address.getHostAddress());
                                    if (!flg) ch.set(ch2);
                                } catch (Exception ex) {
                                    if (debugIP) System.out.println("     ->ホストが取得できません");
                                    continue;
                                }
                                if (debugIP) System.out.println("     ->"+ch2);
                                if (ch2.length() == 0) continue;
                            }
                            int index = ch2.indexOf('*');
                            if (index < 0) {
                                robotIP.enqueue(ch2);
                            } else if (index > 0) {
                                ch2.length = index;
                                robotIP.enqueue(ch2);
                            }
                        }
                    }
                }
            }
            if (debugIP) robotIP.dumpQueue();
        }
    }
    /**
        allowIP, denyIP を更新する
        @return hostname指定件数
    **/
    static public int refreshIP() {
        return refreshIP(false);
    }
    /**
        allowIP, denyIP を更新する
        @param init 初期化モード
        @return hostname指定件数
    **/
    static public int refreshIP(boolean init) {
        int _count = 0;
        synchronized (allowIP) {
            allowIP.clear();
            if (debugIP) System.out.println("★AllowIP");
            HashVector<CharArray,CharArrayQueue> v = ini.getKeyTable("[AllowIP]");
            if (v != null) {
                for (int i = 0; i < v.size(); i++) {
                    CharArrayQueue queue = (CharArrayQueue)v.elementAt(i);
                    for (int j = 0; j <queue.size(); j++) {
                        CharArray ch = queue.peek(j).trim();
                        System.out.println("AllowIP["+ch+"]");
                        if (ch.length() > 0) {
                            CharArray ch2 = new CharArray(ch);
                            if (!ch.isDigit(".*")) { // IPじゃないので変換が必要
                                boolean flg = ch.chars[0] == '*';
                                if (flg) ch.remove(0,1);
                                try {
                                    InetAddress address = InetAddress.getByName(ch.toString());
                                    if (address == null) continue;
                                    ch2.set(address.getHostAddress());
                                    if (init && !flg) ch.set(ch2);
                                } catch (Exception ex) {
                                    if (debugIP) System.out.println("     ->ホストが取得できません");
                                    continue;
                                }
                                if (debugIP) System.out.println("     ->"+ch2);
                                if (ch2.length() == 0) continue;
                                if (flg) _count++;
                            }
                            int index = ch2.indexOf('*');
                            if (index < 0) {
                                allowIP.enqueue(ch2);
                            } else if (index > 0) {
                                ch2.length = index;
                                allowIP.enqueue(ch2);
                            }
                        }
                    }
                }
            }
            if (debugIP) allowIP.dumpQueue();
        }
        synchronized (denyIP) {
            denyIP.clear();
            if (debugIP) System.out.println("★DenyIP");
            HashVector<CharArray,CharArrayQueue> v = ini.getKeyTable("[DenyIP]");
            if (v != null) {
                for (int i = 0; i < v.size(); i++) {
                    CharArrayQueue queue = (CharArrayQueue)v.elementAt(i);
                    for (int j = 0; j < queue.size(); j++) {
                        CharArray ch = queue.peek(j).trim();
                        if (debugIP) System.out.println("DenyIP["+ch+"]");
                        if (ch.length() > 0) {
                            CharArray ch2 = new CharArray(ch);
                            if (!ch.isDigit(".*")) { // IPじゃないので変換が必要
                                boolean flg = ch.chars[0] == '*';
                                if (flg) ch.remove(0,1);
                                try {
                                    InetAddress address = InetAddress.getByName(ch.toString());
                                    if (address == null) continue;
                                    ch2.set(address.getHostAddress());
                                    if (init && !flg) ch.set(ch2);
                                } catch (Exception ex) {
                                    if (debugIP) System.out.println("     ->ホストが取得できません");
                                    continue;
                                }
                                if (debugIP) System.out.println("     ->"+ch2);
                                if (ch2.length() == 0) continue;
                                if (flg) _count++;
                            }
                            int index = ch2.indexOf('*');
                            if (index < 0) {
                                denyIP.enqueue(ch2);
                            } else if (index > 0) {
                                ch2.length = index;
                                denyIP.enqueue(ch2);
                            }
                        }
                    }
                }
            }
            if (debugIP) denyIP.dumpQueue();
        }
        return _count;
    }
    
    /** session allow IP チェック 
        @return false でアクセス不可
    */
    static public boolean checkSessionAllowIP(String szIP) {
if (debugIP) System.out.println("★SystemManager:checkSessionAllowIP:"+szIP);
        boolean rsts = false;   // (sessionAllowIP.size() == 0);
        for (int i = 0; i <sessionAllowIP.size(); i++) {
            if (szIP.startsWith(sessionAllowIP.peek(i).toString())) {
                rsts = true;
if (debugIP) System.out.println("sessionAllowIP:OK:"+sessionAllowIP.peek(i));
                //break;
            } else {
if (debugIP) System.out.println("sessionAllowIP:NG:"+sessionAllowIP.peek(i));
            }
        }
if (debugIP) System.out.println("checkSessionAllowIP:"+rsts);
        return rsts;
    }
    
    /** allow IP チェック 
        @return false でアクセス不可
    */
    static public boolean checkAllowIP(String szIP) {
if (debugIP) System.out.println("SystemManager:checkAllowIP:size:"+allowIP.size());
        boolean rsts = (allowIP.size() == 0);
        for (int i = 0; i <allowIP.size(); i++) {
            if (szIP.startsWith(allowIP.peek(i).toString())) {
                rsts = true;
if (debugIP) System.out.println("allowIP:OK:"+allowIP.peek(i));
                //break;
            } else {
if (debugIP) System.out.println("allowIP:NG:"+allowIP.peek(i));
            }
        }
if (debugIP) System.out.println("checkAllowIP:"+rsts);
        return rsts;
    }
    /** deny IP チェック 
        @return false でアクセス不可
    */
    static public boolean checkDenyIP(String szIP) {
if (debugIP) System.out.println("SystemManager:checkDenyIP:size:"+denyIP.size());
        boolean rsts = true;
        for (int i = 0; i < denyIP.size(); i++) {
            if (szIP.startsWith(denyIP.peek(i).toString())) {
                rsts = false;
if (debugIP) System.out.println("denyIP:NG:"+denyIP.peek(i));
                //break;
            } else {
if (debugIP) System.out.println("denyIP:OK:"+denyIP.peek(i));
            } 
        }
if (debugIP) System.out.println("checkDenyIP:"+rsts);
        return rsts;
    }
    /** 
        robot IP チェック 
        @return true で robot
    */
    static public boolean checkRobotIP(String szIP) {
if (debugIP) System.out.println("SystemManager:checkRobotIP:size:"+robotIP.size());
        boolean rsts = false;
        for (int i = 0; i < robotIP.size(); i++) {
            CharArray chIP = robotIP.peek(i);
            if (chIP.indexOf('/') > 0) {
                rsts = UserAgent.checkIP(szIP, chIP.toString());
            } else {
                rsts = chIP.startsWith(szIP);
            } 
            if (rsts)  break;
        }
if (debugIP) System.out.println("checkRobotIP:"+rsts);
        return rsts;
    }
    
    static public void checkThread() {
        int count = Thread.activeCount();
        Thread ta[] = new Thread[count];
        
        int n = Thread.enumerate(ta);
        System.out.println("checkThread:count="+count+" enumerate="+n);
        for (int i = 0; i < n; i++) {
            System.out.println("Thread "+i+" is "+ta[i].getName());
        }
    }
    
    static public CharArray checkMemory() {
        CharArray ch = new CharArray();
        long freeMem  = Runtime.getRuntime().freeMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long useMem   = totalMem - freeMem;
        ch.add("[Memory]");
        ch.add(" used: ");
        ch.format(useMem,10,12,',');
        ch.add(" free: ");
        ch.format(freeMem,10,12,',');
        ch.add(" total: ");
        ch.format(totalMem,10,12,',');
        return ch;
    }
}

//
//
// [end of SystemManager.java]
//


