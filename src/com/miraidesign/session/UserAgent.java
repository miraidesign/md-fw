//------------------------------------------------------------------------
//    UserAgent.java
//                 ユーザーエージェント情報を管理する
//                 Copyright (c) MiraiDesign 2010-14 All Rights Reserved.
//                 last update: 2017-08-30 携帯関連応報を削除
//------------------------------------------------------------------------
//
// メモ：
//     deviceID = -1 の時は設定できない
//     deviceID = 0 の時は取得できる可能性がある
//          Docomo utn を付ける
//          J-Phone メニューで設定
//     文字列型にする


package com.miraidesign.session;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.text.SimpleDateFormat;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.IntQueue;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.QueueFile;
import com.miraidesign.util.QueueTable;
import com.miraidesign.util.UserLog;
import com.miraidesign.util.UserLogFactory;

import com.miraidesign.servlet.ServletLog;
import com.miraidesign.system.SystemManager;

/**
 *  個々のユーザーエージェント情報を管理する
 *
 *  @version 0.5
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class UserAgent {
    static private boolean debug = (SystemConst.debug && false);  // デバッグ表示 false
    static private boolean debugTable = (SystemConst.debug && false); // false
    static private boolean debugBot = (SystemConst.debug && false); // false
    static private boolean debugCreate = (SystemConst.debug && false);

    // ユーザーエージェント情報を確認する
    static private QueueTable table = new QueueTable();
    /** ユーザーエージェント内部テーブルを取得する */
    static public QueueTable getTable() { return table; }

    // ユーザーエージェントを保管する（ユーザーエージェントからのHash）
    static private HashVector<CharArray,UserAgent> hashUserAgent =
               new HashVector<CharArray,UserAgent>();

    /** 保管UAデバッグ用 */
    static public void dumpUserAgent() {
        for (int i = 0; i < hashUserAgent.size(); i++) {
            UserAgent ua = (UserAgent)hashUserAgent.elementAt(i);
            System.out.println("["+i+"]"+ua.key+"\t"+ua.chUserAgent);
        }

    }

    static private UserLog log = null;  // ユーザーエージェントログ
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd(E) HH:mm:ss",Locale.ENGLISH);

    static private CharArray filename = new CharArray();    // オリジナルのファイル名

    // キャリアタイプ
    /** PC */
    static public final int PC     = 0; //  0;
    /** ドコモ */
    //static public final int DOCOMO = 1; // 100;
    /** ソフトバンク */
    //static public final int SOFTBANK = 2; // 200;
    /** ソフトバンク */
    //static public final int JPHONE = 2; // 200;
    /** ソフトバンク */
    //static public final int VODAFONE = 2; // 200;
    /** AU */
    //static public final int AU     = 3; // 300;
    /** AU */
    //static public final int EZWEB  = 3; // 300;
    /** アステル */
    //static public final int ASTEL  = 4;
    /** ウィルコム */
    //static public final int WILLCOM  = 5;
    //static public final int DDIPOCKET  = 5;
    //static public final int DPOCKET  = 5;
    //static public final int DDIP     = 5;
    /** イーモバイル */
    //static public final int EMOBILE  = 6;
    /** Y!mobile */
    //static public final int YMOBILE  = 7;

    // Content-Length 出力のデフォルト値
    static public boolean outputContentLength_PC       = true;
    /**
    static public boolean outputContentLength_DOCOMO   = false;
    static public boolean outputContentLength_SOFTBANK = false;
    static public boolean outputContentLength_AU       = false;
    static public boolean outputContentLength_ASTEL    = false;
    static public boolean outputContentLength_WILLCOM  = false;
    static public boolean outputContentLength_EMOBILE  = false;
    static public boolean outputContentLength_YMOBILE  = false;
    */
    
    /** キャリア （今だけpublic） */
    public int carrier;

    /** キー情報 */
    protected CharArray key = new CharArray();
    public CharArray getKey() { return key;}
    //
    //protected int defaultIndex;

    // ユーザーエージェント情報のある行(0-)
    protected int index = -1;
    public int getIndex() { return index; }

    public boolean isEmulator = false;


    //public boolean isPC() { return (carrier < 100); }
    public int getCarrier() { return carrier;}
    public boolean isPC() { return (carrier == PC); }
    /*
    public boolean isDocomo() { return (carrier == DOCOMO); }
    public boolean isJPhone() { return (carrier == JPHONE); }
    public boolean isVodafone() { return (carrier == VODAFONE); }
    public boolean isSoftBank() { return (carrier == SOFTBANK); }
    public boolean isAu()     { return (carrier == AU); }
    public boolean isAstel()  { return (carrier == ASTEL); }
    public boolean isDDIPocket()  { return (carrier == DDIP); }
    public boolean isEmobile()  { return (carrier == EMOBILE); }
    public boolean isYmobile()  { return (carrier == YMOBILE); }
    */
    public boolean isSmartPhone() {
        return (carrier == PC && deviceName.length()>0);
    }

    /** キャリア名を取得する */
    public String getCarrierName() {
        String str = "";
        switch (carrier) {
            case PC:       str = "PC";     break;
            /*
            case DOCOMO:   str = "DOCOMO"; break;
            case SOFTBANK: str = "SOFTBANK"; break;
            case AU:       str = "AU";     break;
            case ASTEL:    str = "ASTEL";  break;
            case WILLCOM:  str = "WILLCOM"; break;
            case EMOBILE:  str = "EMOBILE"; break;
            case YMOBILE:  str = "YMOBILE"; break;
            */
        }
        return str;
    }

    /** キャリア名を取得する (SmartPhone、Tabletを切り分け)*/
    public String getCarrierName2() {
        String str = getCarrierName();
        if (isSmartPhone()) {
            str = isTablet() ? "Tablet" : "Smart";
        }
        return str;
    }

    /* Content-Length出力有無のデフォルトを取得
    public boolean getDefaultOutputContentLength() {
        boolean sts = false;
        switch (carrier) {
            case PC:        sts = outputContentLength_PC;       break;
            case DOCOMO:    sts = outputContentLength_DOCOMO;   break;
            case SOFTBANK:  sts = outputContentLength_SOFTBANK; break;
            case AU:        sts = outputContentLength_AU;       break;
            case ASTEL:     sts = outputContentLength_ASTEL;    break;
            case WILLCOM:   sts = outputContentLength_WILLCOM;  break;
            case EMOBILE:   sts = outputContentLength_EMOBILE;  break;
            case YMOBILE:   sts = outputContentLength_YMOBILE;  break;
        }
        return sts;
    }
    */
    
    /** バージョン */
    public int major;
    public int minor;
    public CharArray version = new CharArray();

    /** 機種名 */
    public String deviceName="";

    /** 端末ID */
    public CharArray deviceID = new CharArray();

    /** ブラウザタイプ */
    public CharArray browser = new CharArray();

    /** true:カラー false:グレースケール */
    public boolean isColor;

    /** 色数／階調（ビット数で表す）
        [1]:１色 2:4色 3:8色 4:16色 .... 8:256色 .. 10:1024色<br>
        16:65536色 24:約1677万色
    */
    public int colorDepth = 1;

    /** マークアップラングエージタイプ */
    public String language = "HTML";

    /** アクセスメソッド */
    public String method = "POST";

    //SSL
    protected boolean ssl = false;
    protected boolean bmp = false;
    protected boolean gif = false;
    protected boolean png = false;
    protected boolean jpeg = false;
    protected boolean mng = false;
    // レンダリング
    protected boolean  html = false;
    //protected boolean chtml = false;
    protected boolean xhtml = true;
    //protected boolean   mml = false;
    //protected boolean  hdml = false;
    protected boolean html5 = false;
    /** SSL 対応機種か？ */
    public boolean isSSL() { return ssl;}
    public boolean isBMP() { return bmp;}
    public boolean isGIF() { return gif;}
    public boolean isPNG() { return png;}
    public boolean isJPG() { return jpeg;}
    public boolean isJPEG() { return jpeg;}
    public boolean isMNG() { return mng;}
    /** レンダリング対応 */
    public boolean isHTML() { return html; }
    //public boolean isCHTML() { return chtml; }
    public boolean isXHTML() { return xhtml; }
    //public boolean isMML() { return mml; }
    //public boolean isHDML() { return hdml; }
    public boolean isHTML5() { return html5; }
    public boolean isHtml5() { return html5; }

    /** ブラウザ画面幅(ドット数) */
    public int width;
    /** ブラウザ画面高さ(ドット数) */
    public int height;
    /** 液晶画面幅(ドット数) */
    public int L_width= -1;
    /** 液晶画面高さ(ドット数) */
    public int L_height= -1;
    /** 液晶画面横サイズ(0.1mm単位) */
    public int L_width_mm  = -1;
    /** ブラウザ表示キャラクタ数 */
    public int c_width= -1;
    /** ブラウザ表示ライン数 */
    public int c_height = -1;

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLCDWidth() { return L_width; }
    public int getLCDHeight() { return L_height; }
    public int getLCDWidthMM() { return L_width_mm; }
    public int getCharacterWidth() { return c_width; }
    public int getCharacterHeight() { return c_height; }

    // ディスプレイサイズ(対角インチ×10)
    public int display_size = 0;
    
    /**
        ディスプレイサイズ(対角インチ×10)を取得する
        @return 0:不明
    */
    public int getDisplaySize() {
        return display_size;
    }
    /* タブレットか？ */
    public boolean isTablet() {
        return ((display_size >= 70) || browser.indexOf("Tablet") >= 0);
    }

    /** Androidか？ */
    public boolean isAndroid() {
        return (chUserAgent.indexOf("Android") >= 0);
    }
    public boolean isiPhohe() {
        return (chUserAgent.indexOf("iPhone") >= 0);
    }

    public int filesize;
    public int getFileSize() { return filesize;};

    public int java_version = 0;
    public int getJavaVersion() { return java_version;}

    /** 世代 */
    public int generation = 20;

    /** 世代を取得する 3G=(30～39) */
    public int getGeneration() { return generation;}

    /** 3G対応か？ */
    public boolean is3G() { return (generation >= 30);}

    /** ヘッダーのユーザーエージェント情報
    */
    private CharArray chUserAgent = new CharArray();

    /** ロボットか？ */
    private boolean robot = false;

    /** ロボットか？ */
    public boolean isRobot() { return robot;}
    public void setRobot(boolean mode) { this.robot = mode;}

    /** UserAgentオブジェクトを生成する
        @param user_agent ユーザーエージェントヘッダ
    */
    private UserAgent(int carrier, CharArray ch) {
        this.carrier = carrier;
        chUserAgent.set(ch);
if (debug && debugCreate) System.out.println("★"+getCarrierName()+":"+ch);
    }

    /**
        ユーザーエージェントヘッダを返す
    */
    public CharArray getUserAgentHeader() {
        return chUserAgent;
    }

    /** デバッグ用 */
    public void changeUA(int row, SessionObject session) {
if (debug || debugCreate) session.println("★changeUA row="+row);

//if (debug) System.out.println("(1) bmp["+bmp+"] gif["+gif+"] png["+png+"] jpg["+jpeg+"]");
//if (debug) session.println("(1)html["+html+"] xhtml["+xhtml+"] chtml["+chtml+"] mml["+mml+"]");
        deviceID.set("");
        generation = 20;
        robot = false;

        bmp = gif = png = jpeg = mng = ssl = false;
        html = xhtml = false;

        if (row == 0) {
            carrier = PC;
            deviceName = "PC";
            language="HTML";

            bmp = gif = png = jpeg = mng = true;
            ssl = true;
            html = true;
            xhtml = true;
            L_width = -1;
            L_height = -1;
            L_width_mm = -1;
            c_width = -1;
            c_height = -1;
            java_version = 0;
        } else {
            synchronized (table) {
                chUserAgent.set(table.get(row-1,"HTTP_USER_AGENT"));
            }
                carrier = PC;
                deviceName = "PC";
                deviceID.set("");

                language="HTML";
                ssl = true;
                html = true;
                xhtml = true;
            index = row-1;

            createSub();
        }

        //session.outputContentLength = getDefaultOutputContentLength();
        session.println("★change carrier:"+getCarrierName());
        if (carrier != PC) {
            session.println("★change device:"+table.get(row-1,"DEVICE_NAME"));
            deviceID.set("2010_PC");   // エミュレート用キーワード
        } else {
            session.println("★change to PC");
            colorDepth = 16;
            gif = true;
            bmp = true;
            png = true;
            jpeg = true;
            mng = true;
            ssl = true;
            method = "POST";
            generation = 30;
        }
        session.setLanguage();
    }

    // テーブル情報の取得

    /** テーブル情報を取得する */
    public CharArray get(CharArray column) {
        return table.get(index, column);
    }
    /** テーブル情報を取得する */
    public CharArray get(String column) {
        return table.get(index, column);
    }
    /** テーブル情報を取得する */
    public int getInt(CharArray column) {
        return CharArray.getInt(table.get(index, column));
    }
    /** テーブル情報を取得する */
    public int getInt(String column) {
        return CharArray.getInt(table.get(index, column));
    }
    /** テーブル情報を取得する */
    public long getLong(CharArray column) {
        return CharArray.getLong(table.get(index, column));
    }
    /** テーブル情報を取得する */
    public long getLong(String column) {
        return CharArray.getLong(table.get(index, column));
    }

    /** テーブル情報を取得する */
    public boolean getBoolean(CharArray column) {
        return CharArray.getBoolean(table.get(index, column));
    }
    /** テーブル情報を取得する */
    public boolean getBoolean(String column) {
        return CharArray.getBoolean(table.get(index, column));
    }

    //------------------------------------------------------------
    // 以下 static メソッド
    //------------------------------------------------------------
    /**
        UserAgent情報を初期化する
    */
    static public void init() {
        init(filename);
    }
    static public void init(String file) {
        CharArray ch = CharArray.pop(file);
        init(ch);
        CharArray.push(ch);
    }
    static public void init(CharArray file) {
if (debug || debugCreate) {
    System.out.print("★UserAgent.init("+file+") ");
}
        /*
        if (file != null && file.trim().length() > 0) {
            if (file.indexOf("://") >= 0 ||
                file.chars[0]=='/' ||
                file.chars[0]=='\\' ||
                file.chars[1]==':') {
                createTable(file.toString());
            } else {
                createTable(SystemManager.resourceDirectory+file);
            }
            if (filename.length() == 0) filename.set(file);
        }
        */
        if (log == null) {
            log = UserLogFactory.getUserLog("[UserAgentLog]");
        }
        //-------------------------------------------
        /*
        CharArray ch = SystemManager.ini.get("[System]","OutputContentLength");
        if (ch != null && ch.trim().length() > 0) {
            CharToken token = CharToken.pop();
            token.set(ch,",");
            for (int i = 0; i < token.size(); i++) {
                CharArray ca = token.get(i);
                if (ca != null && ch.trim().length() > 0) {
                    ca.toUpperCase();
                    if (ca.equals("PC")) outputContentLength_PC = true;
                    else if (ca.equals("DOCOMO")) outputContentLength_DOCOMO = true;
                    else if (ca.equals("VODAFONE")) outputContentLength_SOFTBANK = true;
                    else if (ca.equals("JPHONE")) outputContentLength_SOFTBANK = true;
                    else if (ca.equals("SOFTBANK")) outputContentLength_SOFTBANK = true;
                    else if (ca.equals("AU")) outputContentLength_AU = true;
                    else if (ca.equals("ASTEL")) outputContentLength_ASTEL = true;
                    else if (ca.equals("DDIP")) outputContentLength_WILLCOM = true;
                    else if (ca.equals("WILLCOM")) outputContentLength_WILLCOM = true;
                    else if (ca.equals("EMOBILE")) outputContentLength_EMOBILE = true;
                    else if (ca.equals("YMOBILE")) outputContentLength_YMOBILE = true;
                }
            }
            CharToken.push(token);
        }
        */
        int count = table.getRowCount();
        
if (debug || debugCreate) {
    System.out.println(count+" 件読み込みました");
}
       /*
        if (count < 1000) {   // 件数は
            ServletLog.getInstance().error(
                "★UserAgentファイルの読み込みに失敗しました! "+count
            );
            ++SystemManager.init_error;
            SystemManager.init_error_queue.enqueue("★UserAgentファイルの読み込みに失敗しました! "+count);
        }

        if (debug) dumpUserAgent();
        */
    }


    /**
        UserAgent情報を読み込む
    */
    static protected void createTable(String filename) {
        if (debugTable) System.out.println("★createTable["+filename+"]");
        QueueFile file = new QueueFile(filename);
        file.read();
        if (file.size() > 3) {
            synchronized (table) {
                table.readText(file);
                
                table.setIndex("KEY");          // キーでインデックス
            }
            if (debugTable) {
                System.out.println("--dumpTable--");
                table.dumpTable();
            }
        }
    }

    /**
        UserAgent 情報を生成する
        @return UserAgent
        @see com.miraidesign.session.SessionObject#setHeader
    */
    static CharArray uakey = new CharArray("user-agent");
    static CharArray hostkey = new CharArray("host");
    static CharArray x_jphone_msname = new CharArray("x-jphone-msname");
    
    static public UserAgent createUserAgent(Hashtable<CharArray,CharArray> hash) {
        return createUserAgent(hash, 0);
    }
    static public UserAgent createUserAgent(Hashtable<CharArray,CharArray> hash,  int count) {
        return createUserAgent(hash, null, count);
    }
    static public UserAgent createUserAgent(SessionObject session,  int count) {
        //return createUserAgent(session.hashHeader, 
        //    session.request != null ? session.request.getRemoteAddr() : null, count);
        return createUserAgent(session.hashHeader, session.getRemoteIP(), count);
    }
    static public UserAgent createUserAgent(Hashtable<CharArray,CharArray> hash,  String szIP,  int count) {

        String ss =  (count > 0) ? ""+count+"|"  : "";

        if (debug || debugCreate) System.out.println(ss+"★ createUserAgent--------------------:"+szIP+" ;"+count);
        UserAgent userAgent = null;
        CharArray ch = (CharArray)hash.get(uakey);
        if (ch != null) {

            userAgent = (UserAgent)hashUserAgent.get(ch);
            if (userAgent == null) {
                userAgent = createPcUA(ch,hash,szIP);


                if (userAgent != null) {
                    userAgent.robot = checkRobot(ch, szIP); // 携帯もチェック
                    if (userAgent.isPC()) {
                        hashUserAgent.put(ch,userAgent);
                    } else {  // 携帯
                        //if (userAgent.isRobot() &&   
                        //    hashuserAgen.get(userAgent.key) == null) {
                        //     userAgent.key.set("robot");
                        //}
                    }
                    /****
                    if (ch.length() > 32) {
                    } else if (ch.startsWith("J-PHONE/4.0/")) {
                    } else {
                        //hashUserAgent.put(ch,userAgent);
                    }
                    ***/
                    // browser を設定する
                    
                    userAgent.setBrowser();
                    
                }

            } else {
                if (debug) System.out.println(ss+"登録済みのuser-agent["+ch+"]を利用します");
            }
        } else {
            CharArray host = (CharArray)hash.get(hostkey);
            if (host != null && host.length() > 0) {
                //if (host.equals("www.nttdocomo.co.jp")) {
                    userAgent = createPcUA(ch,hash,szIP);
                //}
            }

            if (userAgent == null) {
                System.out.println(ss+"user-agent が 取得できません");
            }
        }

        if ((debug || debugCreate) && userAgent != null) {
            CharArray ch2 = CharArray.pop();
            //ch2.add(ss+"UserAgent:"+userAgent.getUserAgentHeader()+"\n");
            ch2.add(ss+"carrier:"+userAgent.getCarrierName()+"  ");
            //ch2.add(ss+"index :"+userAgent.getIndex()+"\n");
            if (userAgent.isRobot()) ch2.add(" [Robot]");
            
            ch2.add("key:"+userAgent.getKey()+" ");
            ch2.add("deviceName:"+userAgent.deviceName+"  ");
            ch2.add("deviceID:"+userAgent.deviceID+"\n");
            
            ch2.add(ss+"browser:"+userAgent.browser+" ");
            ch2.add("html5:"+userAgent.html5+" ");
            ch2.add(" ver:"); ch2.add(userAgent.version);
            ch2.add(" major:"+userAgent.major+" minor:"+userAgent.minor+"\n");
            
            //ch2.add(ss+"language  :"+userAgent.language+"\n");
            //ch2.add(ss+"method    :"+userAgent.method+"\n");
            //ch2.add("SSL:"+userAgent.isSSL()+"\n");
            ch2.add(ss+"L_width :"+userAgent.L_width+" ");
            ch2.add("L_height :"+userAgent.L_height+" ");
            ch2.add("width:"+userAgent.width+"  ");
            ch2.add("height:"+userAgent.height+"\n");
            ch2.add(ss+"c_width:"+userAgent.c_width+"  ");
            //ch2.add(ss+"color :"+userAgent.colorDepth+"\n");
            //ch2.add(ss+"html? :"+userAgent.html+"\n");
            ch2.add("filesize:"+userAgent.filesize+"  ");
            ch2.add("generation:"+userAgent.generation);
            System.out.println(ch2.toString());
            CharArray.push(ch2);
        }

        if (debug) System.out.println(ss+"★ -----------------------------------");
        return userAgent;
    }
    
    static private void log(UserAgent userAgent, Hashtable hash) {
        String szDate="";
        java.util.Date date = new java.util.Date(System.currentTimeMillis());
        synchronized (sdf) {
            szDate = sdf.format(date);
        }
        String szRobot="";
        if (userAgent.isRobot()) szRobot="(Robot)";
        log.out(szDate+" KEY["+userAgent.key+"] carrier["+userAgent.getCarrierName()+
                       "]"+szRobot+"--------------------------------------------------");
        log.out("user-agent:"+userAgent.getUserAgentHeader());

        for (Enumeration e = hash.keys(); e.hasMoreElements();) {
            CharArray headerKey = (CharArray)e.nextElement();
            CharArray data = (CharArray)hash.get(headerKey);
            if (!headerKey.equals(uakey)) {
                log.out(headerKey+":"+data);
            }
        }
    }


    /**
        ヘッダ情報と useragent.xml の情報を合わせて
        新たに UserAgent オブジェクトを生成する（PC用）
    */
    static private UserAgent createPcUA(CharArray chUA, Hashtable hash, String szIP) {
        UserAgent ua = new UserAgent(PC, chUA);
        // ヘッダから読み取れる情報を設定

        // ここで useragent.xml からの情報をセットする
        ua.language = "HTML";
        //----------------------------------------
        ua.ssl = true;
        ua.bmp = true;
        ua.gif = true;
        ua.jpeg = true;
        ua.png = true;
        ua.mng = true;
        ua.html = true;
        ua.xhtml = true;

        ua.width  = 800;    //640;    //480;    //640;    //256;
        ua.height = 600;    //480;    //360;    //480;    //320;

        ua.L_width  = 1024;  //750;
        ua.L_height =  768;
        ua.L_width_mm = 750;
        ua.c_width = 30;
        ua.c_height = 20;
        ua.filesize = 500000;
        //ua.robot = checkRobot(chUA);

        //--------------------------------------------
        // SmartPhone Check 
        //  ToDo: あとで情報は外出しにする
        //--------------------------------------------
        do {
            if (chUA == null) break;
            //----------------------------------
            // Android系
            //----------------------------------
            if (chUA.indexOf("mPt3D_01") >= 0) { // DPF
                ua.L_width  = 800;
                ua.L_height = 600;
                ua.width  = 800;
                ua.height = 600;
                ua.c_width = 40;
                ua.deviceName = "NF-300i";
                break;
            }
            if (chUA.indexOf("Android Dev Phone 1") >= 0) {
                ua.L_width  = 480;
                ua.L_height = 320;
                ua.width  = 480;
                ua.height = 320;
                ua.c_width = 40;
                ua.deviceName = "AndroidDevPhone-1";
                break;
            }
            if (chUA.indexOf(" smartqv") >= 0) {
                ua.L_width  = 800;
                ua.L_height = 600;
                ua.width  = 480;
                ua.height = 320;
                ua.c_width = 40;
                ua.deviceName = "smartqv";
                break;
            }
            
            //----------------------------------
            // SoftBank
            //----------------------------------
            //if (chUA.indexOf("(iPod; U;") >= 0) {
            if (chUA.indexOf("iPod") >= 0) {
                ua.display_size=35;
                ua.browser.set("MobileSafari");
                ua.L_width  = 320;
                ua.L_height = 480;    // ステータスバー関連を考慮したサイズ
                ua.c_width = 34;
                // 320x356(460?396?) 480x208px
                ua.width  = 320;
                ua.height = 356;
                ua.deviceName = "iPod";
                int idx = chUA.indexOf(" iPhone OS ");
                if (idx >= 0) {
                    int idx2 = chUA.indexOf(' ', idx+11);
                    if (idx2 < idx+8) idx2 = idx+14;
                    ua.deviceName = "iPod_"+chUA.substring(idx+11,idx2).replace("_",".");
                
                
                }
                break;
            }
            //if (chUA.indexOf("(iPad; U;") >= 0) {
            if (chUA.indexOf("iPad") >= 0) {
                // iPad     768x1024 980x1208
                // iPad2    768x1024 980x1208
                // iPad3,4 1536x2048 980x1185
                ua.display_size=97;
                ua.browser.set("MobileSafari");
                int idx = chUA.indexOf(" Coast/");
                if (idx >= 0) {
                    int _ver = chUA.getInt(idx+7,10);
                    ua.browser.set("MobileSafari(Coast/"+_ver+")");
                }
                idx = chUA.indexOf(" CriOS/");
                if (idx >= 0) {
                    int _ver = chUA.getInt(idx+7,10);
                    ua.browser.set("MobileSafari(CriOS/"+_ver+")");
                }
                
                ua.L_width  = 768;
                ua.L_height = 1024;
                ua.c_width = 80;
                ua.width  = 640;
                ua.height = 960;
                ua.deviceName = "iPad";
                idx = chUA.indexOf(" CPU OS ");
                if (idx >= 0) {
                    int idx2 = chUA.indexOf(' ', idx+8);
                    if (idx2 < idx+8) idx2 = idx+11;
                    ua.deviceName = "iPad_"+chUA.substring(idx+8,idx2).replace("_",".");
                }
                break;
            }
            //if (chUA.indexOf("(iPhone; U;") >= 0) {
            if (chUA.indexOf("iPhone") >= 0) {
                ua.display_size=35;
                ua.browser.set("MobileSafari");
                int idx = chUA.indexOf(" CriOS/");
                if (idx >= 0) {
                    int _ver = chUA.getInt(idx+7,10);
                    ua.browser.set("MobileSafari(CriOS/"+_ver+")");
                }
                
                ua.L_width  = 320;
                ua.L_height = 480;
                ua.width  = 320;
                ua.height = 356;    // ステータスバー関連を考慮したサイズ(<-480)
                ua.c_width = 34;
                // 320x356(460?396?) 480x208px
                ua.deviceName = "iPhone";
                idx = chUA.indexOf(" iPhone OS ");
                if (idx >= 0) {
                    if (chUA.chars[idx+11] >= '4') { // iOS4 以降
                        ua.display_size=40;
                        ua.L_width  = 640;
                        ua.L_height = 960; 
                    }
                    int idx2 = chUA.indexOf(' ', idx+11);
                    if (idx2 < idx+8) idx2 = idx+14;
                    ua.deviceName = "iPhone_"+chUA.substring(idx+11,idx2).replace("_",".");
                }
                break;
            }
            
            //----------------------------------
            // Docomo
            //----------------------------------
            //----------------------------------
            // Au
            //----------------------------------
            //----------------------------------
            // Y!mobile
            //----------------------------------
            //----------------------------------
            // Emobile
            //----------------------------------
            //----------------------------------
            // Willcom
            //----------------------------------
            //----------------------------------
            // HTC
            //----------------------------------
            //----------------------------------
            // Google
            //----------------------------------
            //----------------------------------
            // BlackBerry
            //----------------------------------
            
            //----------------------------------
            // Nokia
            //----------------------------------
            
            //----------------------------------
            // SAMSUNG
            //----------------------------------
            
            //----------------------------------
            // Asus
            //----------------------------------
            
            //----------------------------------
            // Palm
            //----------------------------------
            //----------------------------------
            // LG
            //----------------------------------
            //----------------------------------
            // SonyEricsson
            //----------------------------------
            //----------------------------------
            // その他
            //----------------------------------
            //----------------------------------
            // Nikon
            //----------------------------------
            if (chUA.indexOf(" COOLPIX S800c ") >= 0) {   // COOLPIX S800c
                ua.display_size=35;
                ua.L_width  = 854;
                ua.L_height = 480;
                ua.width    = 480;  // 不明
                ua.height   = 320;
                ua.deviceName = "COOLPIX S800c";
                break;
            }   
            //----------------------------------
            // Motorola
            //----------------------------------
            //----------------------------------
            // アンドロイド
            //----------------------------------
            int idx = chUA.indexOf("Android");
            if (idx >=  0) {
                ua.html5 = true;
                ua.L_width  = 480;
                ua.L_height = 800;
                ua.width    = 320;
                ua.height   = 480;
                ua.c_width = 40;
                
                if (chUA.chars[idx+7] != ' ') {
                    ua.deviceName = "Android";
                } else {
                    int idx2 = chUA.indexOf(';',idx+8);
                    if (idx2 < idx) idx2 = idx+11;
                    ua.deviceName = "Android_"+chUA.substring(idx+8,idx2);
                }
                if (chUA.indexOf("Mobile Safari") >= 0) {
                    ua.browser.set("MobileSafari");
                } else if (chUA.indexOf("Safari") >= 0) {
                    if (chUA.indexOf("Mobile/") >= 0) {
                        ua.browser.set("MobileSafari");
                    } else {
                        ua.browser.set("Safari");
                    }
                }
                break;
            }
            //----------------------------------
            // そのほか
            //----------------------------------
            if (chUA.indexOf("Nintendo Wii") >= 0) {
                ua.L_width  = 720;
                ua.L_height = 480;
                ua.width  = ua.L_width;
                ua.height = ua.L_height;
                ua.deviceName = "Nintendo Wii";
                break;
                
            }
            if (chUA.indexOf("Windows Phone") >= 0) {
                ua.c_width = 40;
                ua.L_width  = ua.width  = 480;      // 1024
                ua.L_height = ua.height = 800;      // 1553
                ua.deviceName = "WindowsPhone";
                break;
            }
            if (chUA.indexOf(" KFJW") >= 0) {
                ua.display_size = 89;
                ua.L_width  = 1920;
                ua.L_height = 1200;
                ua.width    =  980;
                ua.height   =  584;
                ua.deviceName = "Kindle FileHD 8.9";
                break;
            }
            if (chUA.indexOf(" KFTHW") >= 0) {
                ua.display_size = 70;
                ua.L_width  = 1920;
                ua.L_height = 1200;
                ua.width    =  980;
                ua.height   =  506;
                ua.deviceName = "Kindle File HDX";
                break;
            }
            if (chUA.indexOf(" KFAPW") >= 0) {
                ua.display_size = 89;
                ua.L_width  = 2560;
                ua.L_height = 1600;
                ua.width    =  980;
                ua.height   =  532;
                ua.deviceName = "Kindle File HDX 8.9";
                break;
            }
            if (chUA.indexOf(" primo81 ") >= 0) {
                ua.display_size = 79;
                ua.L_width  =  768;
                ua.L_height = 1280;
                ua.width    =  980;
                ua.height   = 1220; //?
                ua.deviceName = "Primo81";
                break;
            }
            
        } while (false);
        if (ua.deviceName.length() == 0) { // PC
            ua.c_width = 80;
            ua.robot = checkRobot(chUA, szIP); // 2011-05-03
            if (!chUA.startsWith("Mozilla") && !ua.isRobot()) { // 2010-09-28 確認すること
                log(ua, hash);  // SmartPhoneかもしれないUAをログ出力する
            }
        }
        return  ua;
    }
    /* ブラウザ情報を設定する */
    private void setBrowser(CharArray str) {
        this.browser.set(str);
    }
    /* ブラウザ情報を設定する */
    private void setBrowser(String str) {
        this.browser.set(str);
    }
    /* UAからブラウザ情報を設定する */
    private void setBrowser() {
        setBrowser(false);
    }
    /* UAからブラウザ情報を設定する 
        @param mode true: 強制再設定
    */
    private void setBrowser(boolean mode) {
        CharArray chUA = chUserAgent;
        if (browser.length() == 0 || mode) { // set Browser
            do {
                int idx = 0, idx2 = 0, idx3 = 0; 
                if (chUA.indexOf("Mobile") >= 0 && chUA.indexOf("Safari") >= 0 &&
                    chUA.indexOf("Chrome") < 0) {
                    html5 = true;
                    browser.set("MobileSafari");
                    idx = chUA.indexOf(" Coast/");
                    if (idx >= 0) {
                        int _ver = chUA.getInt(idx+7,10);
                        browser.set("MobileSafari(Coast/"+_ver+")");
                    }
                    idx = chUA.indexOf(" OPR/");
                    if (idx >= 0) {
                        int _ver = chUA.getInt(idx+5,10);
                        browser.set("MobileSafari(Opera"+_ver+")");
                    }
                    
                    break;
                } 
                idx = chUA.indexOf(" Chrome/");
                if (idx >= 0) {
                    major = chUA.getInt(idx+8, 10);
                    idx2 = chUA.indexOf('.', idx+8);
                    if (idx2 >= 0) minor = chUA.getInt(idx2+1, 10);
                    idx3 = chUA.indexOf(' ', idx2+1);
                    if (idx3 >= 0) {
                        version.set(chUA, idx+8, idx3-idx-8);
                    }
                    if (major >= 4) html5 = true;
                    browser.set("Chrome"+major);
                    
                    idx = chUA.indexOf(" OPR/");
                    if (idx >= 0) {
                        int _ver = chUA.getInt(idx+5,10);
                        browser.set("Chrome"+major+"(Opera"+_ver+")");
                    }
                    
                    break;
                }
                idx = chUA.indexOf("Version/");
                idx2 = chUA.indexOf(" Safari/", idx);
                if (idx >= 0 && idx2 > idx) {
                    major = chUA.getInt(idx+8, 10);
                    idx2 = chUA.indexOf('.', idx+8);
                    if (idx2 >= 0) minor = chUA.getInt(idx2+1, 10);
                    idx3 = chUA.indexOf(' ', idx2+1);
                    if (idx3 >= 0) {
                        version.set(chUA, idx+8, idx3-idx-8);
                    }
                    if (major >= 4) html5 = true;
                    browser.set("Safari"+major);
                    break;
                }
                idx = chUA.indexOf("Firefox/");
                if (idx >= 0) {
                    major = chUA.getInt(idx+8, 10);
                    idx2 = chUA.indexOf('.', idx+8);
                    if (idx2 >= 0) minor = chUA.getInt(idx2+1, 10);
                    version.set(chUA, idx+8); 
                    if (major >= 4) html5 = true;
                    
                    if (chUA.indexOf("Tablet;") > 0) {
                        browser.set("FirefoxTablet"+major);
                    
                    } else if (chUA.indexOf("Android;") > 0) {
                        browser.set("FirefoxMobile"+major);
                    } else {
                        browser.set("Firefox"+major);
                    }
                    break;
                }
                
                idx = chUA.indexOf("Opera/");
                idx2 = chUA.indexOf("Version/");
                if (idx >= 0) {
                    if (idx2 >= 0) {  // 旧会議
                        idx = idx2;
                        major = chUA.getInt(idx+8, 10);
                        idx2 = chUA.indexOf('.', idx+8);
                        if (idx2 >= 0) minor = chUA.getInt(idx2+1, 10);
                        version.set(chUA, idx+8);
                        
                    } else {    // 旧タイプとの互換性維持のため
                        major = chUA.getInt(idx+6, 10);
                        idx2 = chUA.indexOf('.', idx+6);
                        if (idx2 >= 0) minor = chUA.getInt(idx2+1, 10);
                        idx3 = chUA.indexOf(' ', idx2+1);
                        if (idx3 >= 0) version.set(chUA, idx+6, idx3-idx-6);
                    }
                    
                    if (major >= 11) html5 = true;
                    if (major == 10 && minor >= 50) html5 = true;
                    
                    if (chUA.indexOf("Opera Mini") > 0) {
                        browser.set("OperaMini"+major);
                    } else if (chUA.indexOf("Opera Mobi") > 0) {
                        browser.set("OperaMobi"+major);
                    } else if (chUA.indexOf("Opera Tablet") > 0) {
                        browser.set("OperaTablet"+major);
                    } else {
                        browser.set("Opera"+major);
                    }
                    break;
                }
                if (chUA.indexOf("NetFront") >= 0) {
                    browser.set("NetFront");
                    break;
                }
                if (chUA.indexOf("UP.Browser") >= 0) {
                    browser.set("UP.Browser");
                    break;
                }
                if (chUA.indexOf("DoCoMo") == 0) {
                    browser.set("DoCoMo");
                    break;
                }
                if (chUA.indexOf("NetFront") >= 0) {
                    browser.set("NetFront");
                    break;
                }
                
                idx = chUA.indexOf(" MSIE ");
                if (idx >= 0) {
                    major = chUA.getInt(idx+6, 10);
                    idx2 = chUA.indexOf('.', idx+6);
                    if (idx2 >= 0) minor = chUA.getInt(idx2+1, 10);
                    idx3 = chUA.indexOf(';', idx2+1);
                    if (idx3 >= 0) version.set(chUA, idx+6, idx3-idx-6);
                    if (major >= 9) html5 = true;
                    browser.set("IE"+major);
                    break;
                }
                idx = chUA.indexOf(" Trident/");
                if (idx >= 0) {
                    idx = chUA.indexOf(" rv:");
                    if (idx >= 0) {
                        major = chUA.getInt(idx+4, 10);
                        idx2 = chUA.indexOf('.', idx+4);
                        if (idx2 >= 0) minor = chUA.getInt(idx2+1, 10);
                        idx3 = chUA.indexOf(')', idx2+1);
                        if (idx3 >= 0) version.set(chUA, idx+6, idx3-idx-6);
                        if (major >= 9) html5 = true;
                        browser.set("IE"+major);
                        break;
                    }
                }
                
                if (chUA.indexOf("Mozilla") == 0) {
                    browser.set("Mozilla");
                    break;
                }
            } while (false);
        }
        if (browser.startsWith("MobileSafari")) {  // 2012/01/06
            html5 = true;
            int idx = chUA.indexOf("Version/");
            if (idx >= 0) {
                major = chUA.getInt(idx+8, 10);
                int idx2 = chUA.indexOf('.', idx+8);
                if (idx2 >= 0) minor = chUA.getInt(idx2+1, 10);
                int idx3 = chUA.indexOf(' ', idx2+1);
                if (idx3 >= 0) version.set(chUA, idx+8, idx3-idx-8);
            }
            if (chUA.indexOf("Android") >= 0) {
                if (isTablet()) {
                    width  = 640;
                    height = 960;
                } else {
                    width  = 320;
                    height = 480;   //356;    // ステータスバー関連を考慮したサイズ
                }
            }
        }
    }


    //-------------------------------
    // checkRobot ロボットチェック
    //-------------------------------
    //static public boolean checkRobot(CharArray chUA) {
    //    return checkRobot(chUA, null);
    //}
    static public boolean checkRobot(CharArray chUA, String szIP) {
if (debugBot) System.out.println("★checkRobot:"+szIP);
        if (chUA == null || chUA.trim().length() == 0) return true; // 2013-07-16 ua無しはロボットとする
        boolean robot = false;
        HashVector<CharArray, CharArrayQueue> hv = SystemManager.ini.getKeyTable("[RobotFilter]");
        if (hv != null && hv.size() > 0) {
            CharToken token = CharToken.pop();
            CharToken keytoken = CharToken.pop();
            token.set(chUA, " ");   // スペースで切り分け
            for (int i = 0; i < hv.size(); i++) {
                int idx = 2;
                keytoken.set(hv.keyElementAt(i),".");
                if (keytoken.size() > 1) {
                    CharArray ca = keytoken.get(1);
                    if (ca != null) {
                        if (ca.trim().isDigit()) idx = ca.getInt();
                        else if (ca.chars[0] == '*') {
                            CharArray key = keytoken.get(0).trim();
                            if (chUA.indexOf(key) >= 0) {
                                robot = true;
                                if (debugBot) System.out.println("Robot("+key+") found!->"+chUA);
                                break;
                            }
                            continue;
                        }
                    }
                } else continue;
                CharArray ch = token.get(idx);
                if (ch != null && ch.trim().length() > 0) {

                    CharArray key = keytoken.get(0).trim();
                    if (debugBot) System.out.println("RobotCheck->"+ch+" "+key);
                    if (ch.startsWith(key)) { // 検索ロジックは変わる可能性があります。
                        robot = true;
                        if (debugBot) System.out.println("Robot("+key+") Found!->"+chUA);
                        break;
                    }
                }
            }
            CharToken.push(keytoken);
            CharToken.push(token);
        }
        if (!robot && szIP != null && szIP.length() > 0) {
            robot = SystemManager.checkRobotIP(szIP);
if (debugBot) System.out.println("◆checkRobot-IP robot:"+robot);
        } else {
if (debugBot) System.out.println("◇checkRobot robot:"+robot);
        
        }
        return robot;
    }

    /**
        IP が指定範囲に含まれるか？
        @param ip  チェックするIP
        @param ipWithMask 範囲マスク指定IP (ex: 12.34.0.0/20)
        @return true 含まれる
    */
    public static boolean checkIP(String ip, String ipWithMask) {
        boolean sts = false;
if (debugBot) System.out.println("▽checkIP("+ip+","+ipWithMask+")");
        CharToken token = CharToken.pop();
        CharArray chkIP = CharArray.pop();
        CharArray srcIP  =  CharArray.pop();
        
        int mask = 32;
        do {
            if (ip == null || ipWithMask == null) break; 
            chkIP.set(ipWithMask); chkIP.trim();
            while (chkIP.endsWith("/")) chkIP.length--;
            
            int idx = chkIP.indexOf("/");
            if (idx >= 0) {
                mask = chkIP.getInt(idx+1,10);
                chkIP.length = idx;
            }
            token.set(chkIP,".");
            if (token.size() != 4) break;
if (debugBot) System.out.println("  chkIP="+chkIP+" mask:"+mask);
            long l = token.getLong(0)*256*256*256+
                     token.getLong(1)*256*256+
                     token.getLong(2)*256+
                     token.getLong(3);
            chkIP.reset();
            chkIP.format(l,2,32,'0');
            token.set(ip,".");
            if (token.size() != 4) break;
            l = token.getLong(0)*256*256*256+
                token.getLong(1)*256*256+
                token.getLong(2)*256+
                token.getLong(3);
            srcIP.format(l,2,32,'0');
if (debugBot) System.out.print("  srcIP="+srcIP+"->");
            srcIP.length=mask;
if (debugBot) System.out.println(srcIP);
if (debugBot) System.out.print("  chkIP="+chkIP+"->");
            chkIP.length=mask;
if (debugBot) System.out.println(chkIP);
            
            sts = srcIP.equals(chkIP);
        } while (false);
        CharArray.push(srcIP);
        CharArray.push(chkIP);
        CharToken.push(token);
if (debugBot) System.out.println("△CheckIP "+sts);
        return sts;
    }

    private void createSub() {
        deviceName = table.getString(index, "DEVICE_NAME");
        int i = table.getInt(index, "COLOR_DEPTH");
        if (i > 1) isColor = true;
        colorDepth = i;
        if (!ssl)  ssl  = table.getBoolean(index, "SSL");
        if (!bmp)  bmp  = table.getBoolean(index, "BMP");
        if (!gif)  gif  = table.getBoolean(index, "GIF");
        if (!png)  png  = table.getBoolean(index, "PNG");
        if (!jpeg) jpeg = table.getBoolean(index, "JPEG");
        if (!mng)  mng  = table.getBoolean(index, "MNG");

        method = table.getBoolean(index, "POST") ?  "POST" : "GET";

        width  = table.getInt(index, "WIDTH");      //画像表示最大ドット数
        height  = table.getInt(index, "HEIGHT");
        //if (isDocomo()) {
            //c_width  = table.getInt(index, "SCREEN_SIZE_BY_LINES_FULLWIDTH")*2;
        //} else {
            L_width     = table.getInt(index, "LCD_WIDTH_PX");  // 液晶幅
            L_height    = table.getInt(index, "LCD_HEIGHT_PX"); // 液晶高さ
            L_width_mm  = table.getInt(index, "LCD_WIDTH_MM");  // 液晶サイズ (0.1mm)単位
            c_width  = table.getInt(index, "CHARACTERS_PER_LINE");
            c_height  = table.getInt(index, "SCREEN_SIZE_BY_LINES");
        //}
        filesize = table.getInt(index, "FILE_SIZE");
        java_version = table.getInt(index, "JAVA_VERSION");

        i = table.getInt(index,"XHTML");
        if (i > 0) {
//if (debug) System.out.println("★i:"+i+" >0 なのでXHTMLが利用できます");
            xhtml = true;
        }
        if (debug && !xhtml) System.out.println("★XHTMLは利用できません");


        boolean b = table.getBoolean(index,"HTML");
        if (b) {
            html = true;
            language = "HTML";  // OK?
        }
    }

    //-------------------------------------------------------------------------

    /**
        ユーザーエージェントヘッダの(CharArray)リストを返す
    */
    static public Enumeration keys() {
        return hashUserAgent.keys();
    }
    /**
        ユーザーエージェントオブジェクトの(UserAgent)リストを返す
    */
    static public Enumeration elements() {
        return hashUserAgent.elements();
    }

    /* メールアドレスからキャリアを取得する。*/
    /*
    static public int  getCarrierFromMailAddress(String address) {
        return getCarrierFromMailAddress(CharArray.pop(address));
    }
    static public int  getCarrierFromMailAddress(CharArray address) {
        int carrier = PC;
        do {
            if (address.endsWith("docomo.ne.jp")) {
                carrier = DOCOMO;     break;
            }
            if (address.endsWith("softbank.ne.jp")) {
                carrier = SOFTBANK;   break;
            }
            if (address.endsWith("i.softbank.jp")) {
                carrier = SOFTBANK;   break;
            }
            if (address.endsWith("disney.ne.jp")) {
                carrier = SOFTBANK;   break;
            }
            if (address.endsWith("vodafone.ne.jp")) {
                carrier = SOFTBANK;   break;
            }
            if (address.endsWith("ezweb.ne.jp")) {
                carrier = AU;         break;
            }
            if (address.endsWith("pdx.ne.jp")) {
                carrier = WILLCOM;    break;
            }
            if (address.endsWith("willcom.com")) {
                carrier = WILLCOM;    break;
            }
            if (address.endsWith("bandai.jp")) {
                carrier = WILLCOM;    break;
            }
            if (address.endsWith("emnet.ne.jp")) {
                carrier = EMOBILE;    break;
            }
            if (address.endsWith("yahoo.ne.jp")) {
                carrier = YMOBILE;    break;
            }
        } while (false);
        return carrier;
    }
    */
}

//
// [end of UserAgent.java]
//

