//------------------------------------------------------------------------
//    SessionObject.java
//          ユーザーごとのデータを保管する
//          Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.session;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Stack;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.miraidesign.common.SystemConst;
import com.miraidesign.content.BlockInfo;
import com.miraidesign.content.BlockParameter;
import com.miraidesign.content.ContentParser;
import com.miraidesign.content.TemplateHashParameter;
import com.miraidesign.content.TemplateParameter;
import com.miraidesign.data.DataAccessObject;
//import com.miraidesign.image.EmojiConverter;
import com.miraidesign.renderer.ItemRenderer;
import com.miraidesign.renderer.Page;
import com.miraidesign.renderer.item.Item;
import com.miraidesign.renderer.item.ItemData;
import com.miraidesign.servlet.MDServlet;
import com.miraidesign.servlet.PageServlet;
import com.miraidesign.servlet.UploadInfo;
import com.miraidesign.servlet.UploadJson;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.system.SiteManager;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.NaviNode;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayFile;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.Crypt62;
import com.miraidesign.util.DataReadWrite;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.IntQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.LongObject;
import com.miraidesign.util.ObjectQueue;
import com.miraidesign.util.QueueElement;
import com.miraidesign.util.Util;

class SessionLocked {   // for hashLocked
    public boolean locked;
    public long    timestamp;
    public SessionLocked(boolean mode, long l) {
        locked = mode;
        timestamp = l;
    }
}


/**
 *  ユーザー毎のセッション情報を管理します。
 *
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class SessionObject implements DataReadWrite {
    static private boolean debug = (SystemConst.debug && false);    // デバッグ表示
    static private boolean debugWrite = (SystemConst.debug && false);  // 保存チェック
    static private boolean debugRead = (SystemConst.debug && false);
    static private boolean debugBlock = (SystemConst.debug && false);
    static private boolean debugTemplate = (SystemConst.debug && false);
    static private boolean debugTheme = (SystemConst.debug && false);
    static private boolean debugTimer = (SystemConst.debug && false);
    static private boolean debugSession = (SystemConst.debug && false);
    static private boolean debugParameter = (SystemConst.debug && true);
    static private boolean debugLocked = (SystemConst.debug && false);   // false
    static private boolean debugPaging = (SystemConst.debug && false);
    static private boolean debugNamespace = (SystemConst.debug && false);
    static private boolean debugLang = (SystemConst.debug && false);    // false
    static private boolean debugHeader = (SystemConst.debug && false);    // false

    protected SessionObject prev = null;
    protected SessionObject next = null;

    static private SimpleDateFormat sdf;

    public int ref_count = 0; //@@@// 参照カウント

    protected int  siteCode  = 0;  // サイトコード
    protected int  channelCode = 0; // チャネルコード
    protected int  sessionID = 0;  // ユーザーごとのセッションＩＤ
    protected int  cartID = 0;      // ショッピングカートＩＤ
    public    int  cookieID = 0;   // クッキーから取得したＩＤ
    protected int  pcID = 0;        // PCID（ユーザーを区分する）
    protected long startTime = 0;  // ユーザーがセッションを開始した時刻
    protected long lastTime  = 0;  // ユーザーが最後にアクセスした時刻

    private Hashtable<IntObject,ItemData> hashItemData = new Hashtable<IntObject,ItemData>();
    private HashParameter userData = new HashParameter(); // ユーザ指定パラメータ
    /** ユーザ指定パラメータを返す */
    public HashParameter getUserData() { return userData;}

    private HashParameter userTemplate = new HashParameter(); // ユーザ指定テンプレート
    
    /** ユーザ指定テンプレートを返す */
    public HashParameter getUserTemplate() { return userTemplate;}

    /** UserTemplate に設定する */
    public boolean setUserTemplate(String key, String value) {
        HashParameter hp = getUserTemplate();
        if (hp == null) return false;
        hp.set(key, value);
        return true;
    }
    /** UserTemplate に設定する */
    public boolean setUserTemplate(String key, CharArray value) {
        HashParameter hp = getUserTemplate();
        if (hp == null) return false;
        hp.set(key, value);
        return true;
    }
    /** UserTemplate に追加する */
    public boolean addUserTemplate(String key, String value) {
        HashParameter hp = getUserTemplate();
        if (hp == null) return false;
        hp.add(key, value);
        return true;
    }
    /** UserTemplate に追加する */
    public boolean addUserTemplate(String key, CharArray value) {
        HashParameter hp = getUserTemplate();
        if (hp == null) return false;
        hp.add(key, value);
        return true;
    }

    private HashParameter checkData = new HashParameter(); // チェック用パラメータ

    /** チェック用パラメータリストを返す */
    public HashParameter getCheckParameter() { return checkData;}

    /** チェック用パラメータの内容を返す */
    public CharArray getCheckParameter(CharArray key) { return checkData.get(key);}
    /** チェック用パラメータの内容を返す */
    public CharArray getCheckParameter(String key) { return checkData.get(key);}

    /** チェック用パラメータを全てクリアする*/
    public void clearCheckParameter() {
        checkData.clear();
    }
    /** チェック用パラメータを削除する*/
    public void removeCheckParameter(String key) {
        checkData.remove(key);
    }
    /** チェック用パラメータを削除する*/
    public void removeCheckParameter(CharArray key) {
        checkData.remove(key);
    }

    /**
        チェックパラメータを取得する
        @param keys チェック＆取得するパラメータキーリスト
        @return どれか１つでも変更があったら、falseを返す
    */
    public boolean checkParameter(CharArrayQueue keys) {
        boolean sts = true;
        for (int i = 0; i < keys.size(); i++) {
            CharArray key = keys.peek(i);
            CharArray data = checkData.get(key);
            String param = request.getParameter(key.toString());

            if (param != null) {
                String param2 = getParameter(key.toString());
                if (data == null) {
                    checkData.add(key,param2);
                    sts = false;
                } else if (!data.equals(param2)) {
                    data.set(param2);
                    sts = false;
                }
            }
        }
        return sts;
    }
    /**
        チェックパラメータを取得する
        @param keys チェック＆取得するパラメータキーリスト
        @return どれか１つでも変更があったら、falseを返す
    */
    public boolean checkParameter(String... keys) {  // 
        boolean sts = true;
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            CharArray data = checkData.get(key);
            String param = request.getParameter(key);
            if (param != null) {
                String param2 = getParameter(key.toString());
                if (data == null) {
                    checkData.add(key,param2);
                    sts = false;
                } else if (!data.equals(param2)) {
                    data.set(param2);
                    sts = false;
                }
            }
        }
        return sts;
    }

    // metaタグ情報を定義する
    private HashParameter hashMetaHttp = null;
    private HashParameter hashMetaName = null;

    /** Meta 情報(http-equiv)を取得する */
    public HashParameter getHashMetaHttp() { return hashMetaHttp;}
    /** Meta 情報(name)を取得する */
    public HashParameter getHashMetaName() { return hashMetaName;}


    /** Meta 情報(http-equiv)を設定する */
    public void setMetaHttp(CharArray key, CharArray content) {
        if (hashMetaHttp == null) hashMetaHttp = new HashParameter();
        hashMetaHttp.add(key, content);
    }
    public void setMetaHttp(String key, String content) {
        if (hashMetaHttp == null) hashMetaHttp = new HashParameter();
        hashMetaHttp.add(key, content);
    }
    /** Meta 情報(http-equiv)を追加する */
    public void addMetaHttp(CharArray key, CharArray content) {
        if (hashMetaHttp == null) hashMetaHttp = new HashParameter();
        CharArray _content = hashMetaHttp.get(key);
        if (_content == null) hashMetaHttp.add(key, content);
        else if (!_content.equals(content)) {
            _content.add(", ");
            _content.add(content);
        }
    }
    public void addMetaHttp(String key, String content) {
        if (hashMetaHttp == null) hashMetaHttp = new HashParameter();

        CharArray chKey = CharArray.pop(key);
        CharArray _content = hashMetaHttp.get(chKey);
        if (_content == null) hashMetaHttp.add(chKey, content);
        else if (!_content.equals(content)) {
            _content.add(", ");
            _content.add(content);
            CharArray.push(chKey);
        }
    }
    public void addMetaHttp(String key, CharArray content) {
        if (hashMetaHttp == null) hashMetaHttp = new HashParameter();

        CharArray chKey = CharArray.pop(key);
        CharArray _content = hashMetaHttp.get(chKey);
        if (_content == null) hashMetaHttp.add(chKey, content);
        else if (!_content.equals(content)) {
            _content.add(", ");
            _content.add(content);
            CharArray.push(chKey);
        }
    }
    /** Meta 情報(name)を設定する */
    public void setMetaName(CharArray key, CharArray content) {
        if (hashMetaName == null) hashMetaName = new HashParameter();
        hashMetaHttp.add(key, content);
    }
    public void setMetaName(String key, String content) {
        if (hashMetaName == null) hashMetaName = new HashParameter();
        hashMetaHttp.add(key, content);
    }

    /** Meta 情報(name)を追加する */
    public void addMetaName(CharArray key, CharArray content) {
        if (hashMetaName == null) hashMetaName = new HashParameter();
        hashMetaHttp.add(key, content);

        CharArray _content = hashMetaName.get(key);
        if (_content == null) hashMetaName.add(key, content);
        else if (!_content.equals(content)) {
            _content.add(", ");
            _content.add(content);
        }
    }

    public void addMetaName(String key, String content) {
        if (hashMetaName == null) hashMetaName = new HashParameter();
        CharArray chKey = CharArray.pop(key);
        CharArray _content = hashMetaName.get(chKey);
        if (_content == null) hashMetaName.add(chKey, content);
        else if (!_content.equals(content)) {
            _content.add(", ");
            _content.add(content);
            CharArray.push(chKey);
        }
    }
    public void addMetaName(String key, CharArray content) {
        if (hashMetaName == null) hashMetaName = new HashParameter();
        CharArray chKey = CharArray.pop(key);
        CharArray _content = hashMetaName.get(chKey);
        if (_content == null) hashMetaName.add(chKey, content);
        else if (!_content.equals(content)) {
            _content.add(", ");
            _content.add(content);
            CharArray.push(chKey);
        }
    }

    /** セッションメタ情報を全てクリアする */
    public void clearMetaAll() {
        if (hashMetaHttp != null) hashMetaHttp.clear();
        if (hashMetaName != null) hashMetaName.clear();
    }

    /** Header Parameter */
    private HashParameter hashHeaderParameter = null;

    /** Header置換情報を取得する */
    public HashParameter getHeaderParameter() { return hashHeaderParameter;}

    /** Header置換情報をクリアする */
    public void clearHeaderParameter() {
//System.out.println("◆clearHeaderParameter!!");
        if (hashHeaderParameter != null) hashHeaderParameter.clear();
    }
    public void debugHeaderParameter() {
//System.out.println("◆debugHeaderParameter!!");
        if (hashHeaderParameter != null) hashHeaderParameter.debugParameter();
    }
    public void debugHeaderParameter(SessionObject session) {
//System.out.println("◆debugHeaderParameter!!");
        if (hashHeaderParameter != null) hashHeaderParameter.debugParameter(session);
    }
    public void debugHeaderParameter(String str) {
//System.out.println("◆debugHeaderParameter!!");
        System.out.println(str);
        if (hashHeaderParameter != null) hashHeaderParameter.debugParameter();
    }
    public void debugHeaderParameter(String str, SessionObject session) {
//System.out.println("◆debugHeaderParameter!!");
        if (session != null) System.out.print(session.count+"|");
        System.out.println(str);
        if (hashHeaderParameter != null) hashHeaderParameter.debugParameter(session);
    }
    //-------------------------------------------------------------------

    //static private CharArray defaultRenderer    = new CharArray("XML");
    static private CharArray defaultRenderer    = new CharArray("HTML");
    static private CharArray defaultLanguage    = new CharArray("HTML");

    /** デフォルトレンダラーを設定する*/
    static public void setDefaultRenderer(String str) {
        defaultRenderer.set(str.toUpperCase());
    }
    private CharArray renderer    = new CharArray(defaultRenderer);     //レンダラー
    private CharArray language    = new CharArray(defaultLanguage);

    //-------------------------------------------------- 
    public ItemRenderer itemRenderer;
    /** レンダラーを設定する */
    public void setItemRenderer(ItemRenderer itemRenderer) {
        this.itemRenderer = itemRenderer;
    }
    /** レンダラーを返す */
    public ItemRenderer getItemRenderer() {
        return itemRenderer;
    }

    private CharArray chBuffer = null;   // レンダリング専用バッファ 
    /**
        デフォルト（表示用）バッファを取得する
    */
    public CharArray getBuffer() {
        if (chBuffer == null) chBuffer = CharArray.popBig();
        return chBuffer;
    }

    /**
        デフォルト（表示用）バッファをクリアする
    */
    public void clearBuffer() {
        if (chBuffer != null) chBuffer.clear();
    }

    // 
    Hashtable<String,CharArray> bufHash;  // String, CharArray
    /**
        名前空間ごとのバッファを取得する
    */
    public CharArray getBuffer(String nameSpace) {
        if (nameSpace == null || nameSpace.equals("")) return getBuffer();
        if (bufHash == null) bufHash = new Hashtable<String,CharArray>();
        CharArray ch = (CharArray)bufHash.get(nameSpace);
        if (ch == null) {
            ch = CharArray.popBig();
            bufHash.put(nameSpace, ch);
        }
        return ch;
    }
    /**
        名前空間ごとのバッファをクリアする
    */
    public void clearBuffer(String nameSpace) {
        if (nameSpace == null || nameSpace.equals("")) clearBuffer();
        else if (bufHash != null) {
            CharArray ch = (CharArray)bufHash.get(nameSpace);
            if (ch != null) ch.clear();
        }
    }
    public void clearBuffer(CharArray nameSpace) {
        clearBuffer(nameSpace.toString());
    }
    /**
        全ての名前空間のバッファをクリアする
    */
    public void clearAllBuffer() {
        clearBuffer();
        clearParserBuffer();
    }
    /**
        デフォルト以外のバッファをクリアする
    */
    public void clearParserBuffer() {
        if (bufHash != null) {
            for (Enumeration e = bufHash.elements(); e.hasMoreElements();) {
                CharArray ch = (CharArray)e.nextElement();
                if (ch != null) ch.clear();
            }
        }
    }

    /**
        デフォルト（表示用）バッファをリセットする
    */
    public void resetBuffer() {
        if (chBuffer != null) {
            chBuffer.clear();
            CharArray.pushBig(chBuffer);
            chBuffer = null;
        }
    }

    /**
        名前空間ごとのバッファをリセットする
    */
    public void resetBuffer(String nameSpace) {
        if (nameSpace == null || nameSpace.equals("")) resetBuffer();
        else if (bufHash != null) {
            CharArray ch = (CharArray)bufHash.get(nameSpace);
            if (ch != null) {
                ch.clear();
                CharArray.pushBig(ch);
                bufHash.remove(nameSpace);
            }
        }
    }
    /**
        全ての名前空間のバッファをリセットする
    */
    public void resetAllBuffer() {
        resetBuffer();
        resetParserBuffer();
    }

    /**
        デフォルト以外のバッファをリセットする
    */
    public void resetParserBuffer() {
        if (bufHash != null) {
            for (Enumeration e = bufHash.keys(); e.hasMoreElements();) {
                String nameSpace = (String)e.nextElement();
                CharArray ch = (CharArray)bufHash.get(nameSpace);
                if (ch != null) {
                    ch.clear();
                    CharArray.pushBig(ch);
                }
                bufHash.remove(nameSpace);
            }
        }
    }

    //--------------------------------------------------
    // SessionEvent 
    private Vector<SessionEvent> eventVector;

    /**
        SessionEvent を追加する
    */
    public void addSessionEvent(SessionEvent ev) {
        if (eventVector == null) eventVector = new Vector<SessionEvent>();
        eventVector.addElement(ev);
    }
    /**
        同一SessionEvent が存在するか？
    */
    public boolean existSessionEvent(SessionEvent ev) {
        boolean exist = false;
        if (eventVector != null && ev != null) {
            exist = eventVector.contains(ev);
        }
        return exist;
    }
    /**
        SessionEvent を削除する
    */
    public void removeSessionEvent(SessionEvent ev) {
        if (eventVector != null && ev != null) {
            if (eventVector.contains(ev)) {
                ev.reset(this);     // イベントにリセットメッセージを送る
                eventVector.removeElement(ev);
            }
        }
    }

    /**
        全てのSessionEvent を削除する
    */
    public void removeAllSessionEvent() {
        if (eventVector != null) {
            for (Enumeration e = eventVector.elements(); e.hasMoreElements();) {
                SessionEvent ev = (SessionEvent)e.nextElement();
                ev.reset(this);
                ev = null;
            }
            eventVector = null;
        }
    }

    //--------------------------------------------------
    // ここで持つべきではないかも知れないが,,しかもpublic！
    public   HttpServletRequest request;
    public void setRequest(HttpServletRequest request, int _count) {
        this.request = request;
        this.remoteAddr = request.getRemoteAddr();
        this.remoteHost = request.getRemoteHost();
        
        requestURI = request.getRequestURI();
if (debugPaging) System.out.println(_count+"|{"+count+"})"+userID+")★setRequest:uri:"+requestURI);
        tokenURI = new CharToken(); //clear();
if (debugPaging) tokenURI.setDebugMode(true);
        tokenURI.set(requestURI,"/");
if (debugPaging) tokenURI.setDebugMode(false);
        hashURI.clear();
        for (int i = 0; i < tokenURI.size(); i++) {
            CharArray ch = tokenURI.get(i);
            int index = ch.indexOf("=");
if (debugPaging) System.out.println("★tokenURI("+(i+1)+"/"+tokenURI.size()+") ch="+ch+" index="+index);
            if (index > 0 ) {
                CharArray key = CharArray.pop(ch);
                key.length = index;
                CharArray data = CharArray.pop(ch);
                data.remove(0,index+1);

if (debugPaging) print(_count+"| key:"+key+" data:"+data);
                hashURI.put(key.toString(), data.toString());
                //System.out.println(" write.");
if (debugPaging) System.out.println("  ★hashURI put:"+key+"="+data);
            }
        }
    }

    public   String requestURI;
    public   CharToken tokenURI = new CharToken();
    public   Hashtable<String,String> hashURI = new Hashtable<String,String>();


    /**
        URIをディレクトリ分割したリストを返す
    */
    public CharToken getDirectoryListFromURI() { return tokenURI; }

    /**
        URIから強制取得したパラメータリストを返す<br>
        パラメータは先頭の = で取得されたもの
    */
    public Hashtable<String,String> getParameterListFromURI() { return hashURI; }

    /** URIから強制取得したパラメータを渡す
        @param  key パラメータキーワード
    */
    public   String getParameterFromURI(String key) {
        return hashURI.get(key);
    }

    public   volatile HttpServletResponse response;
    public   Hashtable<String,String[]> hashParameter;

    /**
        FWによる変換後のパラメータリストを返す
    */
    public   Hashtable<String,String[]> getHashParameter() {
        return hashParameter;
    }

    public   Hashtable<CharArray,CharArray> hashHeader;
    public   Hashtable<String, UploadInfo> hashFileData;
    public   UploadJson jsonBody;
    public   UploadJson getJson() { return jsonBody;}
    public   MDServlet mdServlet;

    public   int       mixedID;
    public   int       pageID;
    public   UserAgent userAgent;

    public   int       count;   // セッションカウント
    public   int       access;  // アクセスカウント

    /** PCユーザーか？ */
    public boolean isPC() {
        return (userAgent != null) ? userAgent.isPC() : true;
    }
    /** モバイルか？ 
       @deprecated
    */
    public boolean isMobile() {
        return (userAgent != null) ? !userAgent.isPC() : false;
    }

    /** Robotか？ **/
    public boolean isRobot()  {
        if (userAgent == null) return false;
        return userAgent.isRobot();
    }

    /** cookieあり？ */
    public boolean hasCookie() {
        boolean sts = false;
        if (request != null) {
            sts = (request.getCookies() != null);
        }
        return sts;
    }
    /** スマートフォンか？ */
    public boolean isSmartPhone() {
        boolean sts = false;
        if (userAgent == null) return false;
        return userAgent.isSmartPhone();
    }
    /** 旧タイプのスマートフォンか？ */
    public boolean isSmartPhoneOld() {
        boolean sts = false;
        if (userAgent == null) return false;
        return userAgent.isSmartPhone() && userAgent.browser.equals("old");
    }
    /** 新タイプのスマートフォンか？ */
    public boolean isSmartPhoneNew() {
        boolean sts = false;
        if (userAgent == null) return false;
        return userAgent.isSmartPhone() && !userAgent.browser.equals("old");
    }
    /** タブレットか？ */
    public boolean isTablet() {
        boolean sts = false;
        if (userAgent == null) return false;
        return userAgent.isTablet();
    }
    /** Androidか？ */
    public boolean isAndroid() {
        boolean sts = false;
        if (userAgent == null) return false;
        return userAgent.isAndroid();
    }
    
    /** carrierID 取得 */
    public int getCarrier() {
        return (userAgent != null) ? userAgent.getCarrier() : -1;
    }
    /** carrier名 取得 */
    public String getCarrierName() {
        return (userAgent != null) ? userAgent.getCarrierName() : "";
    }
    /** carrier名 取得(SmartPhone, Tabletを区別) */
    public String getCarrierName2() {
        return (userAgent != null) ? userAgent.getCarrierName2() : "";
    }

    /** device名 取得 */
    public String getDeviceName() {
        return (userAgent != null) ? userAgent.deviceName : "";
    }
    /** browser名 取得 (暫定版) */
    public String getBrowserName() {
        return (userAgent != null) ? ""+userAgent.browser : "";
    }

    /** SSL 対応機種か? */
    public boolean isSSL() {
        return (userAgent != null) ? userAgent.isSSL() : false;
    }
    /** カラー機種か? */
    public boolean isColor() {
        return (userAgent != null) ? userAgent.isColor : true;
    }
    /** HTML表示可能機種 */
    public boolean isHTML() {
        return (userAgent != null) ? userAgent.html : true;
    }
    /** XHTML対応機種？ */
    public boolean isXHTML() {
        return (userAgent != null) ? userAgent.xhtml : false;
    }
    /** HTML5対応機種？ */
    public boolean isHTML5() {
        return (userAgent != null) ? userAgent.html5 : false;
    }
    public boolean isHtml5() {
        return (userAgent != null) ? userAgent.html5 : false;
    }

    /** ImageProxy のデフォルト設定の取得 */
    public boolean defaultImageProxy() {
        ModuleManager mm = getModuleManager();
        return (mm != null) ? mm.imageProxy : false;
    }

    /** 
        request URL を取得する
    */
    public CharArray getRequestURL() {
        CharArray ch = null;
        do {
            if (request == null) break;
            ch = new CharArray(request.getRequestURL());
            ch.replace("://",":@@");
            ch.replace("//","/");
            ch.replace(":@@","://");
        } while (false);
        return ch;
    }
    
    public String remoteAddr = "";
    public String remoteHost = "";
    /** remote address を取得する */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /** remote host を取得する */
    public String getRemoteHost() {
        return remoteHost;
    }

    public   int       version;     // ロードした時のバージョン
    //---------------------------------------------------
    public   String    userID;              // ハブサイトユーザーＩＤ
    public   String    password;            // パスワード
    public   boolean   authorized = false;  // 認証チェック済みか？
    public   String    deviceID;            // 端末ID（ログイン時に保管する)

    /** 認証ID */
    public   int       authID;              // 認証ID(

    public   int       level = 0;           // レベル
    public   long      loginTime = 0;       // ログイン時刻
    public   long      ackTime = 0;         // ACK取得時刻
    //---------------------------------------------------
    public   long      authorizedTime = 0L;  // 認証時刻
    //---------------------------------------------------
    public   int       itemCount = 0;   // アイテム数
    //---------------------------------------------------
    /** userID を取得する */
    public String getUserID() { return userID;}
    /** 端末ID を取得する */
    public String getDeviceID() { return deviceID;}
    
    /** ユーザー名 */
    public   String    userName;
    /** ユーザー名を設定する */
    public void setUserName(String name) { userName = name;}
    /** ユーザー名を取得する */
    public String getUserName() { return userName;}

    /** 認証情報をセットする */
    public void setAuthorized(String id, String pass,boolean mode) {
        userID = id;
        password = pass;
        authorized = mode;
        if (mode) { // 認証成功
            authorizedTime = System.currentTimeMillis();
        } else {    // ログイン終了
            authorizedTime = 0L;
        }
    }
    /** 認証済かどうか？ */
    public boolean isAuthorized() {
        return authorized;
    }
    /**
        UserCheckModule 等をチェックして、
    */
    public boolean checkAuthorized() {
        return authorized;
    }

    /** 認証IDをセットする */
    public void setAuthID(int id) { authID = id;}
    /** 認証IDを取得する */
    public int getAuthID() { return authID;}
    /** URL認証キーを取得する */
    public String getAuthString() {
        return SystemConst.authIDKey+"="+Crypt62.encode(authID);
    }

    public   PageServlet enterPage;             // 最初に飛んできたページ
    public   PageServlet forwardPage;           // リダイレクトされたページ
    public   PageServlet previousForwardPage;   // 前回のリダイレクトページ
    public   PageServlet previousPage;          // 前のページ
    public   ItemData    previousItemData;      // 前回クリックされたItemData
    public   ItemData    clickedItemData;       // クリックされたItemData
    public   Item        clickedItem;           // クリックされたItem
    public   boolean     updateMode = true;     // Item のupdateチェックモード

    //----------------------------------------------------
    // フレームロック関連
    //----------------------------------------------------
    public   volatile   boolean   locked = false;    // 使用中はtrueにする
                                                     // ↓に切り替え
    // BooleanObject ->SessionLocked
    private  Hashtable<IntObject,SessionLocked>  hashLocked =
         new Hashtable<IntObject,SessionLocked>(); // IntObject : SessionLocked

    private int MAX_LOCK_SEC = 30;

    /** フレームロック状態を見る FWのみ */
    public boolean isLocked(IntObject key) {
        boolean rsts = false;
        synchronized (hashLocked) {
            SessionLocked obj = (SessionLocked)hashLocked.get(key);
            if (obj != null) rsts = obj.locked;
            else             rsts = false;
        }
        return rsts;
    }

    /** フレームロックを設定する FWのみ */
    public boolean setLocked(IntObject key, boolean mode, int _count) {
        boolean sts = true;
if (debugLocked) System.out.println(_count+"|{"+count+"}setLocked("+key+","+mode+")");
        synchronized (hashLocked) {
            do {
                SessionLocked obj = (SessionLocked)hashLocked.get(key);
                if (obj == null) {
                    obj = new SessionLocked(mode, System.currentTimeMillis());
                    hashLocked.put(key, obj);
                } else if (obj.timestamp + MAX_LOCK_SEC * 1000 <= System.currentTimeMillis()) {
                    obj.locked = mode;
                    if (mode == false) hashLocked.remove(key);
                } else {
                    if (mode && obj.locked) {

                        sts = false;
                        break;
                    }
                    obj.locked = mode;
                }
                locked = mode;
            } while (false);
        }
if (debugLocked) System.out.println(_count+"|{"+count+"}setLocked("+key+","+mode+") sts="+sts);
        return sts;
    }
    /** 全てのロックを解除する FWのみ */
    public void clearAllLock(int _count) {
        locked = false;
if (debugLocked) System.out.println(_count+"|clearAllLock start");
        synchronized (hashLocked) {
            hashLocked.clear();
        }
if (debugLocked) System.out.println(_count+"|clearAllLock end");

    }

    //----------------------------------------------------
    // テンプレート関連
    //----------------------------------------------------
    private  Hashtable<String,TemplateParameter> hashTemplateParameter;   // Templateテンプレートのパラメータを保管
    private  Hashtable<String,TemplateHashParameter> hashTemplateHashParameter;   // Templateテンプレートのパラメータを保管

    // マルチセッションの時危険だが、、
    private CharArray defaultNamespace = new CharArray();
    /** デフォルトネームスペース取得 */
    public CharArray getDefaultNamespace() { return defaultNamespace;}

    /** デフォルトネームスペース設定 */
    public void setDefaultNamespace(String str) {
if (debugNamespace) System.out.println("session.setDefaultNamespace:"+str);
        defaultNamespace.set(str);
    }
    /** デフォルトネームスペース設定 */
    public void setDefaultNamespace(CharArray str) {
if (debugNamespace) System.out.println("session.setDefaultNamespace:"+str);
        defaultNamespace.set(str);
    }
    /** デフォルトネームスペースのクリア・初期化 */
    public void clearDefaultNamespace() { defaultNamespace.clear();}
                                                // String : TemplateParameter

    /** 指定ネームスペースのテンプレートパラメータを取得する
        @param  nameSpace ネームスペース
        @return TemplateParameter
    */
    public TemplateParameter getTemplateParameter(String nameSpace) {
        if (hashTemplateParameter == null) hashTemplateParameter = new Hashtable<String,TemplateParameter>();
        if (nameSpace == null) nameSpace = "";
        TemplateParameter tp = (TemplateParameter)hashTemplateParameter.get(nameSpace);
        if (tp == null) {
            tp = new TemplateParameter(nameSpace);
            hashTemplateParameter.put(nameSpace, tp);
if (debugTemplate) System.out.println("  session:("+nameSpace+")のTemplateParameterを生成します");
        }
        return tp;
    }
    public TemplateParameter getTemplateParameter(CharArray nameSpace) {
        return getTemplateParameter(nameSpace.toString());
    }
    /** 指定ネームスペースのテンプレートパラメータを取得する(hash用)
        @param  nameSpace ネームスペース
        @return TemplateParameter
    */
    public TemplateHashParameter getTemplateHashParameter(String nameSpace) {
        if (hashTemplateHashParameter == null) hashTemplateHashParameter =
            new Hashtable<String,TemplateHashParameter>();
        if (nameSpace == null) nameSpace = "";
        TemplateHashParameter thp = (TemplateHashParameter)hashTemplateHashParameter.get(nameSpace);
        if (thp == null) {
            thp = new TemplateHashParameter(nameSpace);
            hashTemplateHashParameter.put(nameSpace, thp);
if (debugTemplate) System.out.println("  session:("+nameSpace+")のTemplateHashParameterを生成します");
        }
        return thp;
    }
    public TemplateHashParameter getTemplateHashParameter(CharArray nameSpace) {
        return getTemplateHashParameter(nameSpace.toString());
    }

    /**
        テンプレートモードをセットする
        @param key
        @param mode ParametersQueue:LOOP/DEQUEUE
    */
    public void setTemplateMode(CharArray key, int mode) {
        TemplateParameter tp = getTemplateParameter(defaultNamespace);
        tp.setTemplateMode(key, mode);
        // hash側にも同じ値をセットしておく
        TemplateHashParameter thp = getTemplateHashParameter(defaultNamespace);
        thp.setTemplateMode(key, mode);
    }
    /**
        テンプレートモードをセットする
        @param key
        @param mode ParametersQueue:LOOP/DEQUEUE
    */
    public void setTemplateMode(String key, int mode) {
        CharArray ch = CharArray.pop(key);
        setTemplateMode(ch,mode);
        CharArray.push(ch);
    }

    /**
        テンプレートモードを取得する
        @param key
    */
    public int getTemplateMode(CharArray key) {
        TemplateParameter tp = getTemplateParameter(defaultNamespace);
        return tp.getTemplateMode(key);
    }
    /**
        テンプレートモードを取得する
        @param key
    */
    public int getTemplateMode(String key) {
        CharArray ch = CharArray.pop(key);
        int ret = getTemplateMode(ch);
        CharArray.push(ch);
        return ret;
    }

    /**
        テンプレートモードをセットする
        @param key
        @param mode ParametersQueue:LOOP/DEQUEUE
        @param nameSpace ネームスペース
    */
    public void setTemplateMode(CharArray key, int mode, String nameSpace) {
        TemplateParameter tp = getTemplateParameter(nameSpace);
        tp.setTemplateMode(key, mode);
        // hash側にも同じ値をセットしておく
        TemplateHashParameter thp = getTemplateHashParameter(nameSpace);
        thp.setTemplateMode(key, mode);
    }
    /**
        テンプレートモードをセットする
        @param key
        @param mode ParametersQueue:LOOP/DEQUEUE
        @param nameSpace ネームスペース
    */
    public void setTemplateMode(String key, int mode, String nameSpace) {
        CharArray ch = CharArray.pop(key);
        setTemplateMode(ch,mode, nameSpace);
        CharArray.push(ch);
    }
    /**
        テンプレートモードを取得する
        @param key
        @param nameSpace ネームスペース
    */
    public int getTemplateMode(CharArray key, String nameSpace) {
        TemplateParameter tp = getTemplateParameter(nameSpace);
        return tp.getTemplateMode(key);
    }
    /**
        テンプレートモードを取得する
        @param key
        @param nameSpace ネームスペース
    */
    public int getTemplateMode(String key, String nameSpace) {
        CharArray ch = CharArray.pop(key);
        int ret = getTemplateMode(ch, nameSpace);
        CharArray.push(ch);
        return ret;
    }

    /**
        テンプレートパラメータをセットする
    */
    public void setTemplate(String key) {
        setTemplate(key, new QueueElement());   //(QueueElement)null);
    }
    /**
        Globalテンプレートパラメータをセットする
    */
    public void setGlobalTemplate(String key) {
        setTemplate(key, new QueueElement(),0,"");   //(QueueElement)null);
    }

    /**
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param param パラメータ
    */
    public void setTemplate(String key, QueueElement param) {
        CharArray ch = CharArray.pop(key);
        setTemplate(ch,param);
        CharArray.push(ch);
    }
    /**
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param hp ハッシュパラメータ
    */
    public void setTemplate(String key, HashParameter hp) {
        CharArray ch = CharArray.pop(key);
        setTemplate(ch, hp);
        CharArray.push(ch);
    }
    /**
        テンプレートパラメータをセットする
        @param key テンプレートキー
    */
    public void setTemplate(CharArray key) {
        setTemplate(key, new QueueElement());   //(QueueElement)null);
    }
    /**
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param param パラメータ
    */
    public void setTemplate(CharArray key, QueueElement param) {
        setTemplate(key,param,0);
    }
    /**
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param hp hashパラメータ
    */
    public void setTemplate(CharArray key, HashParameter hp) {
        setTemplate(key,hp,0);
    }
    /**
        テンプレートパラメータをセットする
        @param key   テンプレートキー
        @param index セットブロック位置
    */
    public void setTemplate(String key,int index) {
        setTemplate(key,(QueueElement)null,index);
    }
    /**
        テンプレートパラメータをセットする
        @param key   テンプレートキー
        @param param パラメータ
        @param index セットブロック位置
    */
    public void setTemplate(String key, QueueElement param, int index) {
        CharArray ch = CharArray.pop(key);
        setTemplate(ch,param, index);
        CharArray.push(ch);
    }
    /**
        テンプレートパラメータをセットする
        @param key   テンプレートキー
        @param hp  ハッシュパラメータ
        @param index セットブロック位置
    */
    public void setTemplate(String key, HashParameter hp, int index) {
        CharArray ch = CharArray.pop(key);
        setTemplate(ch,hp, index);
        CharArray.push(ch);
    }
    /**
        テンプレートパラメータをセットする
        @param key   テンプレートキー
        @param index セットブロック位置
    */
    public void setTemplate(CharArray key, int index) {
        setTemplate(key, (QueueElement)null, index);
    }
    /**
        テンプレートパラメータをセットする<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void setTemplate(String key, String str) {
        setTemplate(key, new QueueElement().add(str), 0);
    }
    /**
        テンプレートパラメータをセットする<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void setTemplate(String key, CharArray str) {
        setTemplate(key, new QueueElement().add(str), 0);
    }
    /**
        テンプレートパラメータをセットする<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void setTemplate(CharArray key, String str) {
        setTemplate(key, new QueueElement().add(str), 0);
    }
    /**
        テンプレートパラメータをセットする<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void setTemplate(CharArray key, CharArray str) {
        setTemplate(key, new QueueElement().add(str), 0);
    }

    /**
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param param パラメータ
        @param index ブロックインデックス
    */
    public void setTemplate(CharArray key, QueueElement param, int index) {
if (debugTemplate) System.out.println("  session:setTemplate ("+defaultNamespace+")にデータを設定します key:"+key);
        TemplateParameter tp = getTemplateParameter(defaultNamespace);
        tp.setTemplate(key, param, index);
    }
//  public void setGlobalTemplate(CharArray key, QueueElement param, int index) {
//      setTemplate(key, param, index, "");
//  }
    /**
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param hp  パラメータ
        @param index ブロックインデックス
    */
    public void setTemplate(CharArray key, HashParameter hp, int index) {
if (debugTemplate) System.out.println("  session:setTemplate ("+defaultNamespace+")にデータを設定します key:"+key);
        TemplateHashParameter thp = getTemplateHashParameter(defaultNamespace);
        thp.setTemplate(key, hp, index);
    }
    /**
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param param パラメータ
        @param index ブロックインデックス
        @param nameSpace
    */
    public void setTemplate(CharArray key, QueueElement param, int index, String nameSpace) {
if (debugTemplate) System.out.println("  session:setTemplate ("+nameSpace+")にデータを設定します key:"+key);
        TemplateParameter tp = getTemplateParameter(nameSpace);
        tp.setTemplate(key, param, index);
    }
    /**
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param param パラメータ
        @param index ブロックインデックス
        @param nameSpace
    */
    public void setTemplate(String key, QueueElement param, int index, String nameSpace) {
if (debugTemplate) System.out.println("  session:setTemplate ("+nameSpace+")にデータを設定します key:"+key);
        TemplateParameter tp = getTemplateParameter(nameSpace);
        tp.setTemplate(key, param, index);
    }
    /**
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param hp パラメータ
        @param index ブロックインデックス
        @param nameSpace
    */
    public void setTemplate(CharArray key, HashParameter hp, int index, String nameSpace) {
if (debugTemplate) System.out.println("  session:setTemplate ("+nameSpace+")にデータを設定します key:"+key);
        TemplateHashParameter thp = getTemplateHashParameter(nameSpace);
        thp.setTemplate(key, hp, index);
    }
    /**
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param hp パラメータ
        @param index ブロックインデックス
        @param nameSpace
    */
    public void setTemplate(String key, HashParameter hp, int index, String nameSpace) {
if (debugTemplate) System.out.println("  session:setTemplate ("+nameSpace+")にデータを設定します key:"+key);
        TemplateHashParameter thp = getTemplateHashParameter(nameSpace);
        thp.setTemplate(key, hp, index);
    }

    /**
        テンプレートパラメータを追加する
        @param str 
    */
    public void addTemplate(String str) {
        addTemplate(str,(QueueElement)null);
    }
    /**
        テンプレートパラメータを追加する
        @param str 
        @param param
    */
    public void addTemplate(String str, QueueElement param) {
        CharArray key = CharArray.pop(str);
        addTemplate(key,param);
        CharArray.push(key);
    }
    /**
        テンプレートパラメータを追加する
        @param key 
    */
    public void addTemplate(CharArray key) {
        addTemplate(key, (QueueElement)null);
    }
    /**
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param param パラメータ
    */
    public void addTemplate(CharArray key, QueueElement param) {
        addTemplate(key, param, 0);
    }

    /**
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param hp hashパラメータ
    */
    public void addTemplate(CharArray key, HashParameter hp) {
        addTemplate(key, hp, 0);
    }
    /**
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param hp hashパラメータ
    */
    public void addTemplate(String key, HashParameter hp) {
        CharArray ch = CharArray.pop(key);
        addTemplate(ch, hp);
        CharArray.push(ch);
    }

    /**
        テンプレートパラメータを追加する
        @param key
        @param index 
    */
    public void addTemplate(String key, int index) {
        addTemplate(key, (QueueElement)null, index);
    }
    /**
        テンプレートパラメータを追加する
        @param key
        @param param
        @param index 
    */
    public void addTemplate(String key, QueueElement param, int index) {
        CharArray ch = CharArray.pop(key);
        addTemplate(ch,param, index);
        CharArray.push(ch);
    }
    /**
        テンプレートパラメータを追加する
        @param key
        @param hp
        @param index 
    */
    public void addTemplate(String key, HashParameter hp, int index) {
        CharArray ch = CharArray.pop(key);
        addTemplate(ch, hp, index);
        CharArray.push(ch);
    }
    /**
        テンプレートパラメータを追加する
        @param key
        @param index 
    */
    public void addTemplate(CharArray key, int index) {
        addTemplate(key, (QueueElement)null, index);
    }

// 2010-11-15 追加
    /**
        テンプレートパラメータを追加する<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void addTemplate(String key, String str) {
        addTemplate(key, new QueueElement().add(str), 0);
    }
    /**
        テンプレートパラメータを追加する<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void addTemplate(String key, CharArray str) {
        addTemplate(key, new QueueElement().add(str), 0);
    }
    /**
        テンプレートパラメータを追加する<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void addTemplate(CharArray key, String str) {
        addTemplate(key, new QueueElement().add(str), 0);
    }
    /**
        テンプレートパラメータを追加する<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void addTemplate(CharArray key, CharArray str) {
        addTemplate(key, new QueueElement().add(str), 0);
    }




    /**
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param param パラメータ
        @param index ブロックインデックス
    */
    public void addTemplate(CharArray key, QueueElement param, int index) {
if (debugTemplate) System.out.println("  session:addTemplate ("+defaultNamespace+")にデータを設定します key:"+key);
        TemplateParameter tp = getTemplateParameter(defaultNamespace);
        tp.addTemplate(key, param, index);
    }
    /**
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param param パラメータ
        @param index ブロックインデックス
        @param nameSpace ネームスペース
    */
    public void addTemplate(CharArray key, QueueElement param, int index, String nameSpace) {
if (debugTemplate) System.out.println("  session:addTemplate ("+nameSpace+")にデータを設定します key:"+key);
        TemplateParameter tp = getTemplateParameter(nameSpace);
        tp.addTemplate(key, param, index);
    }
    /**
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param hp hashパラメータ
        @param index ブロックインデックス
    */
    public void addTemplate(CharArray key, HashParameter hp, int index) {
if (debugTemplate) System.out.println("  session:addTemplate ("+defaultNamespace+")にデータを設定します key:"+key);
        TemplateHashParameter thp = getTemplateHashParameter(defaultNamespace);
        thp.addTemplate(key, hp, index);
    }
    /**
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param hp  ハッシュパラメータ
        @param index ブロックインデックス
        @param nameSpace ネームスペース
    */
    public void addTemplate(CharArray key, HashParameter hp, int index, String nameSpace) {
if (debugTemplate) System.out.println("  session:addTemplate ("+nameSpace+")にデータを設定します key:"+key);
        TemplateHashParameter thp = getTemplateHashParameter(nameSpace);
        thp.addTemplate(key, hp, index);
    }
    /**
        テンプレートパラメータ情報を取得する
        @param key テンプレートキー
    */
    public ObjectQueue getTemplate(CharArray key) {
        return getTemplate(key, defaultNamespace);
    }
    /**
        テンプレートパラメータ情報を取得する
        @param key テンプレートキー
    */
    public ObjectQueue getTemplate(String key) {
        CharArray _key = CharArray.pop(key);
        ObjectQueue queue = getTemplate(_key, defaultNamespace);
        CharArray.push(_key);
        return queue;
    }
    /**
        テンプレートパラメータ情報を取得する
        @param key テンプレートキー
        @param nameSpace ネームスペース
    */
    public ObjectQueue getTemplate(CharArray key, String nameSpace) {
        if (hashTemplateParameter == null ||
            hashTemplateParameter.get(nameSpace) == null || key == null) return null;
        TemplateParameter tp = getTemplateParameter(nameSpace);
        return tp.getTemplate(key);
    }
    /**
        テンプレートパラメータ情報を取得する
        @param key テンプレートキー
        @param nameSpace ネームスペース
    */
    public ObjectQueue getTemplate(CharArray key, CharArray nameSpace) {
        return getTemplate(key,nameSpace.toString());
    }

    /**
        テンプレートパラメータ情報を取得する
        @param key テンプレートキー
    */
    public ObjectQueue getHashTemplate(CharArray key) {
        return getHashTemplate(key, defaultNamespace);
    }
    /**
        テンプレートパラメータ情報を取得する
        @param key テンプレートキー
    */
    public ObjectQueue getHashTemplate(String key) {
        CharArray _key = CharArray.pop(key);
        ObjectQueue queue = getHashTemplate(_key, defaultNamespace);
        CharArray.push(_key);
        return queue;
    }
    /**
        テンプレートパラメータ情報を取得する
        @param key テンプレートキー
        @param nameSpace ネームスペース
    */
    public ObjectQueue getHashTemplate(CharArray key, String nameSpace) {
        if (hashTemplateHashParameter == null ||
            hashTemplateHashParameter.get(nameSpace) == null || key == null) return null;
        TemplateHashParameter thp = getTemplateHashParameter(nameSpace);
        return thp.getTemplate(key);
    }
    /**
        テンプレートパラメータ情報を取得する
        @param key テンプレートキー
        @param nameSpace ネームスペース
    */
    public ObjectQueue getHashTemplate(CharArray key, CharArray nameSpace) {
        return getHashTemplate(key,nameSpace.toString());
    }


    /**
        テンプレートのstatic化
        @param key
        @param mode true:static化する false:通常モード
    */
    public void setStaticTemplate(CharArray key, boolean mode) {
        setStaticTemplate(key, mode, "");
    }
    /**
        テンプレートのstatic化
        @param key
        @param mode true:static化する false:通常モード
    */
    public void setStaticTemplate(String key, boolean mode) {
        CharArray ch = CharArray.pop(key);
        setStaticTemplate(ch, mode, "");
        CharArray.push(ch);
    }
    /**
        テンプレートのstatic化
        @param key
        @param nameSpace
        @param mode true:static化する false:通常モード
    */
    public void setStaticTemplate(CharArray key, boolean mode, String nameSpace) {
        if (hashTemplateParameter != null && hashTemplateParameter.get(nameSpace) != null) {
            TemplateParameter tp = getTemplateParameter(nameSpace);
            tp.setStaticTemplate(key, mode);
        }
        if (hashTemplateHashParameter != null && hashTemplateHashParameter.get(nameSpace) != null) {
            TemplateHashParameter thp = getTemplateHashParameter(nameSpace);
            thp.setStaticTemplate(key, mode);
        }
    }
    /**
        テンプレートのstatic化
        @param key
        @param nameSpace
        @param mode true:static化する false:通常モード
    */
    public void setStaticTemplate(String key, boolean mode, String nameSpace) {
        CharArray ch = CharArray.pop(key);
        setStaticTemplate(ch, mode, nameSpace);
        CharArray.push(ch);
    }
    /**
        テンプレートをstatic化する
        @param key
    */
    public void setStaticTemplate(CharArray key) {
        setStaticTemplate(key,true,"");
    }
    /**
        テンプレートをstatic化する
        @param key
    */
    public void setStaticTemplate(String key) {
        setStaticTemplate(key,true,"");
    }
    /**
        テンプレートをstatic化する
        @param key
        @param nameSpace
    */
    public void setStaticTemplate(CharArray key, String nameSpace) {
        setStaticTemplate(key,true, nameSpace);
    }
    /**
        テンプレートをstatic化する
        @param key
        @param nameSpace
    */
    public void setStaticTemplate(String key, String nameSpace) {
        setStaticTemplate(key,true, nameSpace);
    }

    /** テンプレートパラメータを全て削除する （全ネームスペース）*/
    public void clearAllTemplate() {
        if (hashTemplateParameter != null) {
            for (Enumeration e = hashTemplateParameter.elements();e.hasMoreElements();) {
                TemplateParameter tp = (TemplateParameter)e.nextElement();
                if (tp != null) tp.clearAllTemplate();
            }
        }
        if (hashTemplateHashParameter != null) {
            for (Enumeration e = hashTemplateHashParameter.elements();e.hasMoreElements();) {
                TemplateHashParameter thp = (TemplateHashParameter)e.nextElement();
                if (thp != null) thp.clearAllTemplate();
            }
        }
    }
    /** 指定ネームスペースのテンプレートパラメータを全て削除する */
    //public void clearAllTemplate(String nameSpace) {
    //    if (hashTemplateParameter != null) {
    //        TemplateParameter tp = (TemplateParameter)hashTemplateParameter.get(nameSpace);
    //        if (tp != null) tp.clearAllTemplate();
    //    }
    //}
    /** 通常テンンプレートパラメータを削除する （全ネームスペース）*/
    public void clearTemplate() {
        if (hashTemplateParameter != null) {
            for (Enumeration e = hashTemplateParameter.elements();e.hasMoreElements();) {
                TemplateParameter tp = (TemplateParameter)e.nextElement();
                if (tp != null) tp.clearTemplate();
            }
        }
        if (hashTemplateHashParameter != null) {
            for (Enumeration e = hashTemplateHashParameter.elements();e.hasMoreElements();) {
                TemplateHashParameter thp = (TemplateHashParameter)e.nextElement();
                if (thp != null) thp.clearTemplate();
            }
        }
    }
    /** 指定ネームスペースの通常テンンプレートパラメータを削除する */
    // public void clearTemplate(String nameSpace) {
    //    if (hashTemplateParameter != null) {
    //        TemplateParameter tp = (TemplateParameter)hashTemplateParameter.get(nameSpace);
    //        if (tp != null) tp.clearTemplate();
    //    }
    //}
    /** staticテンンプレートパラメータを削除する （全ネームスペース）*/
    public void clearStaticTemplate() {
        if (hashTemplateParameter != null) {
            for (Enumeration e = hashTemplateParameter.elements();e.hasMoreElements();) {
                TemplateParameter tp = (TemplateParameter)e.nextElement();
                if (tp != null) tp.clearStaticTemplate();
            }
        }
        if (hashTemplateHashParameter != null) {
            for (Enumeration e = hashTemplateHashParameter.elements();e.hasMoreElements();) {
                TemplateHashParameter thp = (TemplateHashParameter)e.nextElement();
                if (thp != null) thp.clearStaticTemplate();
            }
        }
    }
    /** 指定ネームスペースのstaticテンンプレートパラメータを削除する */
    //public void clearStaticTemplate(String nameSpace) {
    //    if (hashTemplateParameter != null) {
    //        TemplateParameter tp = (TemplateParameter)hashTemplateParameter.get(nameSpace);
    //        if (tp != null) tp.clearStaticTemplate();
    //    }
    //}
    /**
        指定テンプレートパラメータを削除する
        @param key テンプレートキーワード
    */
    public void clearTemplate(CharArray key) {
        clearTemplate(key, defaultNamespace.toString());
    }
    /**
        指定テンプレートパラメータを削除する
        @param key テンプレートキーワード
    */
    public void clearTemplate(String key) {
        CharArray ch = CharArray.pop(key);
        clearTemplate(ch, defaultNamespace.toString());
        CharArray.push(ch);

    }
    /**
        指定テンプレートパラメータを削除する
        @param key テンプレートキーワード
        @param nameSpace ネームスペース
    */
    public void clearTemplate(CharArray key, String nameSpace) {
        if (hashTemplateParameter != null) {
            TemplateParameter tp = (TemplateParameter)hashTemplateParameter.get(nameSpace);
            if (tp != null) tp.clearTemplate(key);
        }
        if (hashTemplateHashParameter != null) {
            TemplateHashParameter thp = (TemplateHashParameter)hashTemplateHashParameter.get(nameSpace);
            if (thp != null) thp.clearTemplate(key);
        }
    }
    /**
        指定テンプレートパラメータを削除する
        @param key テンプレートキーワード
        @param nameSpace ネームスペース
    */
    public void clearTemplate(String key, String nameSpace) {
        CharArray ch = CharArray.pop(key);
        clearTemplate(ch, nameSpace);
        CharArray.push(ch);

    }
    /** テンプレートデバッグ表示 */
    public void debugTemplate() {
        debugTemplate(null);
    }
    /** テンプレートデバッグ表示
        @param str 追加文字
    */
    public void debugTemplate(String str) {
        debugTemplate(str, defaultNamespace.toString());
    }
    /** テンプレートデバッグ表示
        @param str 追加文字
        @param nameSpace ネームスペース
    */
    public void debugTemplate(String str, String nameSpace) {
        if (hashTemplateParameter != null) {
            TemplateParameter tp = (TemplateParameter)hashTemplateParameter.get(nameSpace);
            if (tp != null) tp.debugTemplate(str);
            //if (tp != null) tp.debugTemplate(nameSpace);
        }
        if (hashTemplateHashParameter != null) {
            TemplateHashParameter thp = (TemplateHashParameter)hashTemplateHashParameter.get(nameSpace);
            if (thp != null) thp.debugTemplate(str);
        }
    }

    /** 全てのネームスペースのテンプレートデバッグ表示 */
    public void debugAllTemplate() {
        debugAllTemplate("");
    }
    /** 全てのネームスペースのテンプレートデバッグ表示
        @param str 追加文字
    */
    public void debugAllTemplate(String str) {
        System.out.println("▽▽▽-----------debugAllTemplate");
        if (hashTemplateParameter != null) {
            for (Enumeration e = hashTemplateParameter.keys();e.hasMoreElements();) {
                String ns = (String)e.nextElement();
                System.out.println("  ▽▼ namespace("+ns+")");
                TemplateParameter tp = (TemplateParameter)hashTemplateParameter.get(ns);
                if (tp != null) tp.debugTemplate("    ○");
            }
        }
        if (hashTemplateHashParameter != null) {
            for (Enumeration e = hashTemplateHashParameter.keys();e.hasMoreElements();) {
                String ns = (String)e.nextElement();
                System.out.println("  ▼▽ namespace("+ns+")");
                TemplateHashParameter thp = (TemplateHashParameter)hashTemplateHashParameter.get(ns);
                if (thp != null) thp.debugTemplate("    ○");
            }
        }
        System.out.println("△△△-----------debugAllTemplate");
        /**
            TemplateParameter tp = (TemplateParameter)hashTemplateParameter.get(nameSpace);
            //if (tp != null) tp.debugTemplate(str);
            if (tp != null) tp.debugTemplate(nameSpace);
        **/
    }

    /** ヘッダテンプレートをセットする */
    public void setHeaderTemplate(CharArray key, CharArray str) {
        if (str != null) {
            if (hashHeaderParameter == null) hashHeaderParameter = new HashParameter();
            hashHeaderParameter.add(key, str);
        }
    }
    public void setHeaderTemplate(String key, String str) {
        if (str != null) {
            if (hashHeaderParameter == null) hashHeaderParameter = new HashParameter();
            hashHeaderParameter.add(key,str);
        }
    }
    public void setHeaderTemplate(CharArray key, String str) {
        if (str != null) {
            if (hashHeaderParameter == null) hashHeaderParameter = new HashParameter();
            hashHeaderParameter.add(key,str);
        }
    }
    public void setHeaderTemplate(String key, CharArray str) {
        if (str != null) {
            if (hashHeaderParameter == null) hashHeaderParameter = new HashParameter();
            hashHeaderParameter.add(key,str);
        }
    }
    /** ヘッダテンプレートに追加する */
    public void addHeaderTemplate(CharArray key, CharArray str) {
        if (str != null) {
            if (hashHeaderParameter == null) hashHeaderParameter = new HashParameter();
            CharArray chKey = hashHeaderParameter.get(key);
            if (chKey == null) hashHeaderParameter.add(key,str);
            else chKey.add(str);
        }
    }
    public void addHeaderTemplate(String key, String str) {
        if (str != null) {
            if (hashHeaderParameter == null) hashHeaderParameter = new HashParameter();
            CharArray chKey = CharArray.pop(key);
            CharArray chk = hashHeaderParameter.get(chKey);
            if (chk == null) hashHeaderParameter.add(key,str);
            else            chk.add(str);
            CharArray.push(chKey);
        }
    }
    public void addHeaderTemplate(CharArray key, String str) {
        if (str != null) {
            if (hashHeaderParameter == null) hashHeaderParameter = new HashParameter();
            CharArray chKey = hashHeaderParameter.get(key);
            if (chKey == null) hashHeaderParameter.add(key,str);
            else chKey.add(str);
        }
    }
    public void addHeaderTemplate(String key, CharArray str) {
        if (str != null) {
            if (hashHeaderParameter == null) hashHeaderParameter = new HashParameter();
            CharArray chKey = CharArray.pop(key);
            CharArray chk = hashHeaderParameter.get(chKey);
            if (chk == null) hashHeaderParameter.add(key,str);
            else            chk.add(str);
            CharArray.push(chKey);
        }
    }

    /** ヘッダテンプレートを取得する */
    public CharArray getHeaderTemplate(CharArray key) {
        if (hashHeaderParameter == null) return null;
        return hashHeaderParameter.get(key);
    }
    public CharArray getHeaderTemplate(String key) {
        if (hashHeaderParameter == null) return null;
        CharArray chKey = CharArray.pop(key);
        CharArray rsts = hashHeaderParameter.get(chKey);
        CharArray.push(chKey);
        return rsts;
    }
    /** 同一ヘッダテンプレートをチェックする
        @return : 同一のものが存在すれば 0以上を返す
    */
    public int checkHeaderTemplate(CharArray key, CharArray str) {
        int sts = -1;
        if (str != null && hashHeaderParameter != null) {
            CharArray ch = hashHeaderParameter.get(key);
            if (ch != null) sts = ch.indexOf(str);
        }
        return sts;
    }
    public int checkHeaderTemplate(String key, CharArray str) {
        CharArray chKey = CharArray.pop(key);
        int sts = checkHeaderTemplate(chKey, str);
        CharArray.push(chKey);
        return sts;
    }
    /***
    private CharArray getLangKey(CharArray _key) {
        CharArray key = CharArray.pop(_key);
        CharArray langData = getLangData();
        if (langData != null && langData.length() > 0) {
            key.add("."); key.add(langData);
            ContentTemplate ct = forwardPage.getTemplate();
            if (ct.getTemplate(key) == null) {  // マルチ言語対応のテンプレートが存在しない
                key.set(_key);
            }
        }
System.out.println("●getLangKey("+_key+")->"+key+"  LANG:"+LANG+" forward:"+forwardPage);
        return key;
    }
    **/

    //----------------------------------------------------
    // ブロック関連
    //----------------------------------------------------
    //private  Hashtable   hashBlock;          // ブロックカウンター
    //                                         // CharArray key : BlockInfo

    private  Hashtable<String,BlockParameter> hashBlockParameter;   // Templateテンプレートのパラメータを保管
                                             // String : BlockParameter
    /** 指定ネームスペースのブロックパラメータを取得する
        @param  nameSpace ネームスペース
        @return TemplateParameter
    */
    public BlockParameter getBlockParameter(String nameSpace) {
        if (hashBlockParameter == null) hashBlockParameter = new Hashtable<String,BlockParameter>();
        if (nameSpace == null) nameSpace = defaultNamespace.toString();
        BlockParameter bp = (BlockParameter)hashBlockParameter.get(nameSpace);
        if (bp == null) {
            bp = new BlockParameter(nameSpace);
            hashBlockParameter.put(nameSpace, bp);
        }
        return bp;
    }

    // ページスタート時にパーサー側で使う
    public Hashtable<CharArray,BlockInfo>   getBlockTable() {
        BlockParameter bp = getBlockParameter(defaultNamespace.toString());
        return bp.getBlockTable();
    }

    public Hashtable<CharArray,BlockInfo>   getBlockTable(String nameSpace) {
        BlockParameter bp = getBlockParameter(nameSpace);
        return bp.getBlockTable();
    }

    /** ブロック情報を取得する
        @param  key    ブロックキー
        @return ブロックカウント  -1: 存在しない
    */
    public int getBlock(CharArray key) {
        int sts = -1;
        if (hashBlockParameter != null && hashBlockParameter.get(defaultNamespace.toString()) != null) {
            BlockParameter bp = getBlockParameter(defaultNamespace.toString());
            return bp.getBlock(key);
        }
        return sts;
    }
    public int getBlock(String str) {
        CharArray key = CharArray.pop(str);
        int sts = getBlock(key);
        CharArray.push(key);
        return sts;
    }

    /** ブロック開始情報を取り出す  カウンターをデクリメントする
        @param  key    ブロックキー
        @return デクリメントしカウンタ -1: 存在しない
    */
    public int startBlock(CharArray key) {
        return startBlock(key, defaultNamespace.toString());
    }
    public int startBlock(String str) {
        CharArray key = CharArray.pop(str);
        int sts = startBlock(key,defaultNamespace.toString());
        CharArray.push(key);
        return sts;
    }
    /** ブロック開始情報を取り出す  カウンターをデクリメントする
        @param  key       ブロックキー
        @param  nameSpace ネームスペース
        @return デクリメントしカウンタ -1: 存在しない
    */
    public int startBlock(CharArray key, String nameSpace) {
        int sts = -1;
        if (hashBlockParameter != null && hashBlockParameter.get(nameSpace) != null) {
            BlockParameter bp = getBlockParameter(nameSpace);
            return bp.startBlock(key);
        }
        return sts;
    }

    public int startBlock(String str, String nameSpace) {
        CharArray key = CharArray.pop(str);
        int sts = startBlock(key, nameSpace);
        CharArray.push(key);
        return sts;
    }

    /** ブロック終了情報を取り出す  ０でカウンターをリセットする
        @param  key    ブロックキー
        @return ブロックカウンタ -1: 存在しない
    */
    public int endBlock(CharArray key) {
        return endBlock(key, defaultNamespace.toString());
    }

    public int endBlock(String str) {
        CharArray key = CharArray.pop(str);
        int sts = endBlock(key, defaultNamespace.toString());
        CharArray.push(key);
        return sts;
    }
    /** ブロック終了情報を取り出す  ０でカウンターをリセットする
        @param  key    ブロックキー
        @param  nameSpace ネームスペース
        @return ブロックカウンタ -1: 存在しない
    */
    public int endBlock(CharArray key, String nameSpace) {
        int sts = -1;
        if (hashBlockParameter != null && hashBlockParameter.get(nameSpace) != null) {
            BlockParameter bp = getBlockParameter(nameSpace);
            return bp.endBlock(key);
        }
        return sts;
    }
    public int endBlock(String str, String nameSpace) {
        CharArray key = CharArray.pop(str);
        int sts = endBlock(key, nameSpace);
        CharArray.push(key);
        return sts;
    }

    /** ブロックカウンタに繰返し回数をセットする <br>
        （count=0 でそのブロックの描画が行われなくなる）
        @param  key    ブロックキー
        @param count    繰返し回数
    */
    public void setBlock(CharArray key, int count) {
        setBlock(key, count, defaultNamespace.toString());
    }
    public void setBlock(String str, int count) {
        CharArray key = CharArray.pop(str);
        setBlock(key, count, defaultNamespace.toString());
        CharArray.push(key);
    }

    /** ブロックカウンタに繰返し回数をセットする <br>
        （count=0 でそのブロックの描画が行われなくなる）
        @param  key    ブロックキー
        @param count    繰返し回数
        @param nameSpace ネームスペース
    */
    public void setBlock(CharArray key, int count, String nameSpace) {
        BlockParameter bp = getBlockParameter(nameSpace);
if (debugBlock) System.out.println("★☆Session#setBlock("+key+","+count+","+nameSpace+")");
        bp.setBlock(key, count);
    }
    public void setBlock(String str, int count, String nameSpace) {
        CharArray key = CharArray.pop(str);
        setBlock(key, count, nameSpace);
        CharArray.push(key);
    }

    /** ブロックカウンタを取得する <br>
        @param  key    ブロックキー
        @param nameSpace ネームスペース
    */
    public int getBlockCount(CharArray key,String nameSpace) {
        BlockParameter bp = getBlockParameter(nameSpace);
        return bp.count(key);
    }
    public int getBlockCount(String str, String nameSpace) {
        CharArray key = CharArray.pop(str);
        int sts = getBlockCount(key, nameSpace);
        CharArray.push(key);
        return sts;
    }
    public int getBlockCount(CharArray key) {
        BlockParameter bp = getBlockParameter(defaultNamespace.toString());
        return bp.count(key);
    }
    public int getBlockCount(String str) {
        CharArray key = CharArray.pop(str);
        int sts = getBlockCount(key);
        CharArray.push(key);
        return sts;
    }

    /** ブロック最大値を取得する <br>
        @param key    ブロックキー
        @param nameSpace ネームスペース
    */
    public int getBlockMax(CharArray key,String nameSpace) {
        BlockParameter bp = getBlockParameter(nameSpace);
        return bp.count(key);
    }
    public int getBlockMax(String str, String nameSpace) {
        CharArray key = CharArray.pop(str);
        int sts = getBlockMax(key, nameSpace);
        CharArray.push(key);
        return sts;
    }
    public int getBlockMax(CharArray key) {
        BlockParameter bp = getBlockParameter(defaultNamespace.toString());
        return bp.count(key);
    }
    public int getBlockMax(String str) {
        CharArray key = CharArray.pop(str);
        int sts = getBlockMax(key);
        CharArray.push(key);
        return sts;
    }

    /**
        ブロック情報を全て削除する（全ネームスペース）<br>
        （テンプレートファイルに記述された情報を使用する事になる）
    */
    public void clearBlock() {
        if (hashBlockParameter != null) {
            for (Enumeration e = hashBlockParameter.elements(); e.hasMoreElements();) {
                BlockParameter bp = (BlockParameter)e.nextElement();
                if (bp != null) bp.clearBlock();
            }
        }
    }
    public void clearAllBlock(String nameSpace) {
        if (hashBlockParameter != null && hashBlockParameter.get(nameSpace) != null) {
            BlockParameter bp = getBlockParameter(nameSpace);
            if (bp != null) bp.clearBlock();
        }
    }
    /**
        ブロック情報を削除する<br>
        （テンプレートファイルに記述された情報を使用する事になる）
        @param  key    ブロックキー
    */
    public void clearBlock(CharArray key) {
        if (hashBlockParameter != null && hashBlockParameter.get(defaultNamespace.toString()) != null) {
            BlockParameter bp = getBlockParameter(defaultNamespace.toString());
            //bp.remove(key);
            bp.clearBlock(key);
        }
    }
    public void clearBlock(String key) {
        CharArray ch = CharArray.pop(key);
        clearBlock(ch);
        CharArray.push(ch);
    }

    //----------------------------------------------------
    // パーサー情報
    //----------------------------------------------------
    private  Hashtable<String,ContentParser> hashContentParser;  // ContentParserを保管
                                         // String(namespace) : ContentParser

    /**
        ContentParserを取得する
        存在しない場合は新規に作成し、parse(session)される
    */
    public ContentParser getParser(CharArrayFile file, String nameSpace) {
        return getParser(file, null, nameSpace);
    }
    public ContentParser getParser(CharArrayFile file, CharArrayFile ini, String nameSpace) {
        if (hashContentParser == null) hashContentParser = new Hashtable<String,ContentParser>();
        if (nameSpace == null) nameSpace = defaultNamespace.toString();
        ContentParser cp = (ContentParser)hashContentParser.get(nameSpace);
        if (cp == null) {
            cp = new ContentParser(file, ini, nameSpace, this);
            cp.parse(this);
            hashContentParser.put(nameSpace, cp);
        }
        return cp;
    }
    /**
        ContentParserを取得する
        @param mode true:強制的に新規生成する
    */
    public ContentParser getParser(CharArrayFile file, String nameSpace, boolean mode) {
        return getParser(file, null, nameSpace, mode);
    }
    public ContentParser getParser(CharArrayFile file, CharArrayFile ini, String nameSpace, boolean mode) {
        if (hashContentParser == null) hashContentParser = new Hashtable<String,ContentParser>();
        if (nameSpace == null) nameSpace = defaultNamespace.toString();
        if (mode) clearParser(nameSpace);
        return getParser(file, ini, nameSpace);
    }

    /**
        ContentParserを取得する
        存在しない場合はnullを返す
    */
    public ContentParser getParser(String nameSpace) {
        if (hashContentParser == null) hashContentParser = new Hashtable<String,ContentParser>();
        if (nameSpace == null) nameSpace = defaultNamespace.toString();
        return  (ContentParser)hashContentParser.get(nameSpace);
    }


    /**
        ContentParserを設定する。parse(session)も行う。
        既存の場合は上書きし再度parse(session);
    */
    public ContentParser setParser(CharArrayFile file, String nameSpace) {
        return setParser(file, null, nameSpace);
    }
    public ContentParser setParser(CharArrayFile file, CharArrayFile ini, String nameSpace) {
        if (hashContentParser == null) hashContentParser = new Hashtable<String,ContentParser>();
        if (nameSpace == null) nameSpace = defaultNamespace.toString();
        ContentParser cp = (ContentParser)hashContentParser.get(nameSpace);
        if (cp == null) {
            cp = new ContentParser(file, ini, nameSpace);
            cp.parse(this);
            hashContentParser.put(nameSpace, cp);
        } else {
            if (!cp.getFile().equals(file)) {
                cp.setFile(file);
                cp.parse(this);
            }
        }
        return cp;
    }

    /**
        パーサー情報を全て削除する（全ネームスペース）<br>
        バッファもクリアします
    */
    public void clearParser() {
        if (hashContentParser != null) {
            hashContentParser.clear();
            clearParserBuffer();
            //for (Enumeration e = hashContentParser.elements(); e.hasMoreElements();) {
            //    ContentParser cp = (ContentParser)e.nextElement();
            //    if (cp != null) cp.clear();
            //}
        }
    }
    /**
        指定ネームスペースのパーサー情報を全て削除する
        バッファもクリアします
    */
    public void clearParser(String nameSpace) {
        if (hashContentParser != null) {
            hashContentParser.remove(nameSpace);
            clearBuffer(nameSpace);
        }
    }

    //----------------------------------------------------
    // forward 用のqueue
    //----------------------------------------------------
    private ObjectQueue queue = new ObjectQueue();
    /** PageServlet#forward() 用のqueueを取得する */
    public ObjectQueue getQueue() {
        return queue;
    }
    /** PageServlet#forward() 用のqueueをクリアする */
    public ObjectQueue clearQueue() {
        queue.clear();
        return queue;
    }
    //----------------------------------------------------
    // DAO キャッシュ関連
    //----------------------------------------------------
    private  int DAO_MAX = 5;  // DAOの最大保管数
    public void setDaoMax(int size) { DAO_MAX = size;}

    private  HashVector<LongObject,DataAccessObject> hashDAO =
         new HashVector<LongObject,DataAccessObject>();     // DAOのテーブル

    public HashVector<LongObject,DataAccessObject> getHashDAO() {
        return hashDAO;
    }

    public void clearHashDAO() {

        // 
        for (Enumeration e = hashDAO.keys(); e.hasMoreElements();) {
            DataAccessObject dao = (DataAccessObject)e.nextElement();
            if (dao != null) {
                try {
                    dao.rollback();
                    //dao.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                dao.clearTable();
                dao.clear();
                dao = null;
            }
        } // next;

        hashDAO.clear();
    }


    /** DAO を取得する <br>
        キャッシュに存在すればそこから取得、ない場合は新規生成する。
        @param page 取得したいページ
    */
    public DataAccessObject getDAO(PageServlet page) {
        long timer = Util.Timer();
        long l = (long)sessionID * 1000000L + page.hashCode();
        LongObject lo = LongObject.pop(l);

        DataAccessObject dao = (DataAccessObject)hashDAO.get(lo);
        if (dao == null) {
            dao = new DataAccessObject(page,page);
            dao.set(this);

            hashDAO.put(new LongObject(l),dao);
            if (debug) System.out.println("Session#getDAO DAO生成しました"+page.hashCode());
            // DAO_MAX を超えていたら先頭を削除
            if (hashDAO.size() > DAO_MAX) {
                DataAccessObject dao2 = (DataAccessObject)hashDAO.elementAt(0);
                try { dao2.close(); } catch (Exception e) {}
                if (debug) System.out.println("Session#getDAO 先頭のDAOを削除します"+dao.getPage().hashCode());
                hashDAO.removeAt(0);
                // GCに送る
                dao2.clearTable();
                dao2.clear();
                dao2 = null;
            }
        } else {
            if (debug) System.out.println("Session#getDAO DAOを再利用します"+page.hashCode());
            // 付け替えて見る
            hashDAO.refresh(lo);
            dao.setDebug(dao.getDebug() && SystemConst.debug);
        }
        LongObject.push(lo);
        if (debug) System.out.println("Session#getDAO hashsize="+hashDAO.size());
        return dao;
    }
    /** DAOが存在しなければnullを返す*/
    public DataAccessObject checkDAO(PageServlet page) {
        long l = (long)sessionID * 1000000L + page.hashCode();
        LongObject lo = LongObject.pop(l);

        DataAccessObject dao = (DataAccessObject)hashDAO.get(lo);

        LongObject.push(lo);
        return dao;
    }

    /** DAO を削除する
        @param page キャッシュよりDAOを削除するページ
    */
    public boolean removeDAO(PageServlet page) {
        boolean sts = false;
        long l = (long)sessionID * 1000000L + page.hashCode();
        LongObject lo = LongObject.pop(l);
        DataAccessObject dao = (DataAccessObject)hashDAO.get(lo);
        if (dao != null) {
            if (debug) System.out.println("Session#removeDAO DAO をクローズします "+page.hashCode());
            try {
                dao.rollback();
            } catch (Exception e) {}
            if (debug) System.out.println("Session#removeDAO hashsize="+hashDAO.size());
            dao.clearTable();
            dao.clear();
            dao = null;
            sts = (hashDAO.remove(lo) != null);
        } else {
            //if (debug) System.out.println("Session#removeDAO そのDAOは存在していません "+page.hashCode());

        }
        LongObject.push(lo);
        return sts;
    }

    //------------------------------------------------------
    // DataConnection
    //------------------------------------------------------
    public String defaultConnection = "";

    /** DBのデフォルト接続を設定する */
    public void setDefaultConnection(String str) {
        defaultConnection = str.trim();
    }

    /** DBのデフォルト接続を取得する<br>
        存在しない場合はModuleManagerのデフォルト接続を返す
     */
    public String getDefaultConnection() {
        if (defaultConnection != null && defaultConnection .length() > 0) {
            return defaultConnection;
        }
        ModuleManager mm = getModuleManager();
        return  (mm != null) ? mm.defaultConnection : defaultConnection;
    }

    //------------------------------------------------------
    // ナビゲーション関連
    //------------------------------------------------------
    public Page page = null;       // カレントテンプレートページ
    public NaviNode node = null;   // カレントノード

    //------------------------------------------------------
    // ContentLength
    //------------------------------------------------------
    public boolean outputContentLength = true; // Content-Lengthを出力するか？

    //------------------------------------------------------
    // host/http/https URL関連
    //------------------------------------------------------
    public String getHostURL() {
        String str = null;
        ModuleManager mm = getModuleManager();
        if (mm != null) str = mm.hostURL;
        if (str == null || str.length() == 0) str = SystemManager.hostURL;
        return str;
    }
    public String getHttpURL() {
        String str = null;
        ModuleManager mm = getModuleManager();
        if (mm != null) str = mm.httpURL;
        if (str == null || str.length() == 0) str = SystemManager.httpURL;
        return str;
    }
    public String getHttpsURL() {
        String str = null;
        ModuleManager mm = getModuleManager();
        if (mm != null) str = mm.httpsURL;
        if (str == null || str.length() == 0) str = SystemManager.httpsURL;
        return str;
    }
    public String getRegistURL() {
        String str = null;
        ModuleManager mm = getModuleManager();
        if (mm != null) str = mm.registURL;
        if (str == null || str.length() == 0) str = SystemManager.registURL;
        return str;
    }
    public String getCacheServerURL() {
        String str = null;
        ModuleManager mm = getModuleManager();
        if (mm != null) str = mm.cacheServerURL;
        if (str == null || str.length() == 0) str = SystemManager.cacheServerURL;
        return str;
    }
    //------------------------------------------------------
    // テーマ関連
    //------------------------------------------------------
    private CharArray theme = new CharArray();
    public void setTheme(String str) {
        if (debugTheme) System.out.println("■☆■Session#setTheme("+str+")"+sessionID);
        theme.set(str);
    }
    public void setTheme(CharArray ch) {
        if (debugTheme) System.out.println("■☆■Session#setTheme("+ch+")"+sessionID);
        theme.set(ch);
    }
    public CharArray getTheme() {
        if (debugTheme) System.out.println("■☆■Session#getTheme["+theme+"]"+sessionID);
        
        if (theme.length() == 0) {
            ModuleManager mm = getModuleManager();
            if (mm != null) return mm.getTheme();
        }
        return theme;
    }
    public CharArray getThemeData() {
        ModuleManager mm = getModuleManager();
        if (mm == null) return null;
        CharArray ch = (theme.length() == 0) ? mm.getThemeData() : mm.getThemeData(theme);
        if (debugTheme) System.out.println("■☆■Session#getThemeData "+theme+":"+ch+" "+sessionID);
        return ch;
    }
    public CharArray getThemeName() {
        ModuleManager mm = getModuleManager();
        if (mm == null) return null;
        CharArray ch = (theme.length() == 0) ? mm.getThemeName() : mm.getThemeName(theme);
        if (debugTheme) System.out.println("■☆■Session#getThemeData "+theme+":"+ch+" "+sessionID);
        return ch;
    }
    //------------------------------------------------------
    // LANG関連 (ja, en,,)
    //------------------------------------------------------
    protected CharArray LANG = new CharArray();
    public void setLang(String str) { LANG.set(str);}
    public void setLang(CharArray ch) { LANG.set(ch);}

    /** LANG キーワードを取得する */
    public CharArray getLang() { 
        if (LANG.length() == 0) {
            ModuleManager mm = getModuleManager();
            if (mm != null) {
if (debugLang) System.out.println("moduleManager#getLang:"+mm.getLang());
                LANG.set(mm.getLang());  // 2012-08-09
            }
        }
if (debugLang) System.out.println("session#getLang:"+LANG);
        return LANG;
    }

    /** LANG情報を取得する */
    public CharArray getLangData() {
        ModuleManager mm = getModuleManager();
        if (mm == null) return null;
        return (LANG.length()==0) ? mm.getLangData() : mm.getLangData(LANG);
    }
    
    /** LANG名称を取得する */
    public CharArray getLangName() {
        ModuleManager mm = getModuleManager();
        if (mm == null) return null;
        return (LANG.length()==0) ? mm.getLangName() : mm.getLangName(LANG);
    }
    
    //------------------------------------------------------
    // Country Code関連 (JP, US)
    //------------------------------------------------------
    protected CharArray CC = new CharArray();
    public void setCountryCode(String str) { CC.set(str).toUpperCase();}
    public void setCountryCode(CharArray ch) { CC.set(ch).toUpperCase();}

    /** 国別コード(JP, US...) を取得する */
    public CharArray getCountryCode() { return CC;}
    
    public CharArray SC = new CharArray();  // 国指定
    
    //------------------------------------------------------
    // Role関連
    //------------------------------------------------------
    protected CharArray role = new CharArray();
    public void setRole(String str) { role.set(str);}
    public void setRole(CharArray ch) { role.set(ch);}
    public CharArray getRole() { return role;}

    //------------------------------------------------------
    // CharSet関連
    //------------------------------------------------------
    protected String contentType = "";
    protected String charSet = "";
    protected String charCode = "";

    /** コンテントタイプを設定する*/
    public void setContentType(String str) {
        if (str == null) contentType = "";
        else             contentType = str;
    }
    /** キャラクタセットを設定する*/
    public void setCharSet(String str) {
        if (str == null) charSet = "";
        else             charSet = str;
    }
    /** 出力文字コードを設定する*/
    public void setCharCode(String str) {
        if (str == null) charCode = "";
        else             charCode = str;

System.out.println("○●○session.setCharCode("+str+")->"+charCode);
    }

    /** コンテントタイプを取得する
        @param mode true:デフォルト session,site,systemの順にサーチする
    */
    public String getContentType(boolean mode) {
        if (mode) {
            if (contentType.length() > 0) return contentType;
            ModuleManager mm = getModuleManager();
            if (mm.contentType.length() > 0) return mm.contentType;
            return SystemManager.contentType;
        } else {
            return contentType;
        }
    }
    public String getContentType() { return getContentType(true); }

    /** キャラクタセットを取得する
        @param mode true:デフォルト session,site,systemの順にサーチする
    */
    public String getCharSet(boolean mode) {
        if (mode) {
            if (charSet.length() > 0) return charSet;
            ModuleManager mm = getModuleManager();
            if (mm.charSet.length() > 0) return mm.charSet;
            return SystemManager.charSet;
        } else {
            return charSet;
        }
    }
    public String getCharSet() { return getCharSet(true); }

    /** 出力文字コードを取得する
        @param mode true:デフォルト session,site,systemの順にサーチする
    */
    public String getCharCode(boolean mode) {
        if (mode) {
            if (charCode.length() > 0) return charCode;
            ModuleManager mm = getModuleManager();
            if (mm != null && mm.charCode.length() > 0) return mm.charCode;
            return SystemManager.charCode;
        } else {
            return charCode;
        }
    }
    public String getCharCode() { return getCharCode(true); }

    //------------------------------------------------------
    // Proxy デバッグ用
    //------------------------------------------------------
    private boolean proxyDebug = false;
    private CharArrayQueue proxySrc = new CharArrayQueue();
    private IntQueue       proxySize = new IntQueue();
    public boolean getProxyDebug() { return proxyDebug;}
    public CharArrayQueue getProxySrc() { return proxySrc;}
    public IntQueue       getProxySize() { return proxySize;}
    public void setProxyDebug(boolean mode) {
        proxySrc.clear();
        proxySize.clear();
        proxyDebug = mode;
    }
    public void addProxyMessage(CharArray src, int size) {
        proxySrc.enqueue(src);
        proxySize.enqueue(size);
    }

    //------------------------------------------------------
    // セッションイベント
    //------------------------------------------------------
    
    // リセット用イベント（リセット時にクリアされる）
    private Stack<SessionEvent> resetEventStack;
    
    /* リセットイベントStackを取得する */
    public Stack<SessionEvent> getResetEventStack() { return resetEventStack; }
    
    /* リセットイベントを追加する */
    public void pushResetEvent(SessionEvent event) {
        if (event != null) {
if (debugLocked) System.out.println("  ▽pushResetEvent");
            if (resetEventStack == null) resetEventStack = new Stack<SessionEvent>();
            synchronized (resetEventStack) {
//System.out.println("★リセットイベントに追加しました");
                resetEventStack.push(event);
            }
if (debugLocked) System.out.println("  △pushResetEvent");
        }
    
    }

    /**
        リセットイベントを抜き出す
    */
    public SessionEvent popResetEvent() {
        SessionEvent event = null;
        if (resetEventStack != null) {
if (debugLocked) System.out.println("  ▽popResetEvent");
            synchronized (resetEventStack) {
                if (resetEventStack.size() > 0) {
                    event = resetEventStack.pop();
                }
                if (resetEventStack.size() <=  0) resetEventStack = null;
            }
if (debugLocked) System.out.println("  △popResetEvent");
        }
        return event;
    }

    public void resetSessionEvent() { // resetから呼ばれる
if (debugLocked) System.out.println("▽resetSessionEvent");
        while (true) {
            SessionEvent event = popResetEvent();
            if (event == null) break;
            event.reset(this);
        }
if (debugLocked) System.out.println("△resetSessionEvent");
    }

    //------------------------------------------------------
    // セッション関連メソッド
    //------------------------------------------------------

    /**
        セッションオブジェクトを生成する
        @param siteCode サイトコード＋チャネルコード
    */
    protected SessionObject(int siteCode,Hashtable<CharArray,CharArray> hash) {
        init(siteCode,hash,null,0);
    }
    protected SessionObject(int siteCode,Hashtable<CharArray,CharArray> hash, HttpServletRequest request, int _count) {
        init(siteCode,hash,request, _count);
    }
    /**
        データをクリアする
    */
    protected void reset() {
if (debugSession) {
    String device=" : ";
    if (userAgent != null) {
        device = getCarrierName()+":"+getDeviceName();
    }
    System.out.println("session#reset() :"+sessionID+" ["+
        device+
        (isRobot()? "(Robot)":"")+
        "] reset！  session count="+SessionManager.getSessionCount()+" ref:"+ref_count);
}
        clearAllLock(0);
        
        resetSessionEvent();    // 2010-09-28
        contentType = "";
        charSet = "";
        charCode = "";

        prev = null;
        next = null;
        siteCode = 0;
        channelCode = 0;
        sessionID = 0;
        cookieID = 0;   // for debug
        cartID = 0;
        pcID = 0;
        startTime = 0;
        lastTime = 0;

        renderer.set(defaultRenderer);           //レンダラー
        language.set(defaultLanguage);
        request = null;
        requestURI = null;
        remoteAddr="";
        remoteHost="";
        tokenURI.clear();
        hashURI.clear();
        response = null;
        hashParameter = null;

        hashHeader = null;
        hashFileData = null;
        jsonBody = null;
        mdServlet = null;
        mixedID = pageID = 0;
        userAgent = null;
        count = 0;
        access = 0;
        version = 0;

        userID = null;
        password = null;
        deviceID = null;
        userName = null;
        authID = 0;
        authorized = false;
        level = 0;
        loginTime = 0;
        ackTime = 0;
        authorizedTime = 0L;
        itemCount = 0;

        enterPage = null;
        forwardPage = null;
        previousForwardPage = null;
        previousPage = null;      // 前のページ
        previousItemData = null;
        clickedItemData  = null;       // クリックされたItemData
        clickedItem = null;
        //DAO_MAX = 10;

        updateMode = true;
        szSQLPrefix = "";
        defaultConnection = "";
        hashItemData.clear();

        if (hashMetaHttp != null) hashMetaHttp.clear();
        if (hashMetaName != null) hashMetaName.clear();
        if (hashHeaderParameter != null) hashHeaderParameter.clear();

        clearTemplate();    // テンプレート情報のクリア
        clearBlock();       // ブロック情報のクリア
        clearParser();      // パーサー情報のクリア

        defaultNamespace.clear();
        hashTemplateParameter = null;
        hashTemplateHashParameter = null;
        hashBlockParameter = null;
        hashContentParser = null;

        page = null;
        node = null;
if (debugTheme) System.out.println("■☆■Session ["+theme+"] clear");
        theme.clear();
        LANG.clear();
        CC.clear();
        SC.clear();
        setProxyDebug(false);
if (debug) System.out.println("Session#reset DAO をクリアします");

        int n = 1;
        for (Enumeration e = hashDAO.keys(); e.hasMoreElements(); n++) {
            LongObject key = (LongObject)e.nextElement();
            DataAccessObject dao = (DataAccessObject)hashDAO.get(key);
            if (dao != null && dao.getSession().equals(this)) {
                if (debug) System.out.println(" dao page="+dao.getPage().hashCode());
                try {
                    dao.rollback();
                } catch (Exception ex) {

                }
                dao.clearTable();
                dao.clear();
                hashDAO.remove(key);
                dao = null;
            }
        }
        userData.clear();
        userTemplate.clear();
        checkData.clear();

        resetAllBuffer();  // バッファを返す
        removeAllSessionEvent();
        minimized = false;
    }
    /**
        ユーザーデータをクリアする
    */
    public void userReset() {
if (debugSession) {
    String device=" : ";
    if (userAgent != null) {
        device = getCarrierName()+":"+getDeviceName();
    }
    System.out.println("session#userReset() :"+sessionID+" ["+
        device+
        (isRobot()? "(Robot)":"")+
        "] reset！  session count="+SessionManager.getSessionCount()+" ref:"+ref_count);
}
        userID = null;
        password = null;
        deviceID = null;
        userName = null;
        authID = 0;
        authorized = false;
        level = 0;
        loginTime = 0;
        ackTime = 0;
        authorizedTime = 0L;
        itemCount = 0;
    }


    public boolean minimized = false;
    /**
        セッション情報を最小化する
    */
    public void minimize() {
        minimized = true;
if (debugSession) {
    String device=" : ";
    if (userAgent != null) {
        device = getCarrierName()+":"+getDeviceName();
    }
    System.out.println("☆☆session:(minimize)"+sessionID+" ["+
        device+
        (isRobot()? "(Robot)":"")+
        "] reset！  session count="+SessionManager.getSessionCount()+" ref:"+ref_count);
}

    if (ref_count < 1) {
        renderer.set(defaultRenderer);           //レンダラー
        language.set(defaultLanguage);
        request = null;
        requestURI = null;
        tokenURI.clear();
        hashURI.clear();
        response = null;
        hashParameter = null;

        hashHeader = null;
        hashFileData = null;
        jsonBody = null;
        mdServlet = null;
        mixedID = pageID = 0;
    }

    if (ref_count < 1) {
        enterPage = null;
        forwardPage = null;
        previousForwardPage = null;
        previousPage = null;      // 前のページ
        previousItemData = null;
        clickedItemData  = null;       // クリックされたItemData
        clickedItem = null;

        updateMode = true;
        szSQLPrefix = "";
        defaultConnection = "";

        hashItemData.clear();

        if (hashMetaHttp != null) hashMetaHttp.clear();
        if (hashMetaName != null) hashMetaName.clear();
        if (hashHeaderParameter != null) hashHeaderParameter.clear();

        clearTemplate();    // テンプレート情報のクリア
        clearBlock();       // ブロック情報のクリア
        clearParser();      // パーサー情報のクリア

        defaultNamespace.clear();
        hashTemplateParameter = null;
        hashTemplateHashParameter = null;
        hashBlockParameter = null;
        hashContentParser = null;

        page = null;
        node = null;
    }

        setProxyDebug(false);
if (debug) System.out.println("Session#reset DAO をクリアします");

    if (ref_count < 1) {
        userData.clear();
        userTemplate.clear();
        checkData.clear();

        resetAllBuffer();  // バッファを返す
    }
        removeAllSessionEvent();
    }

    /**
        データを初期化し、ユーザハッシュテーブルを作成する。
        @param siteCode サイトコード＋チャネルコード
        @param hash  リクエストヘッダ
    */
    protected void init(int siteCode, Hashtable<CharArray,CharArray> hash) {
        init(siteCode,hash,null, true,0);
    }
    protected void init(int siteCode, Hashtable<CharArray,CharArray> hash, HttpServletRequest request,int _count) {
        init(siteCode,hash,request, true, _count);
    }
    /**
        データを初期化し、ユーザハッシュテーブルを作成する。
        @param siteChCode サイトコード＋チャネルコード
        @param hash  リクエストヘッダ
        @param mode  セッションＩＤを新規に作る
    */
    protected void init(int siteChCode, Hashtable<CharArray, CharArray> hash,HttpServletRequest request,
                        boolean mode) {
        init(siteChCode, hash, request,mode, 0);
    }
    protected void init(int siteChCode, Hashtable<CharArray, CharArray> hash,HttpServletRequest request,
                        boolean mode, int _count) {
        if (_count > 0) count = _count;
        ref_count = 0;
        debug &= SystemConst.debug;
        debugWrite &= SystemConst.debug;
        debugRead &= SystemConst.debug;
        debugBlock &= SystemConst.debug;
        this.request = request;

        setSiteChannelCode(siteChCode);
        if (mode) {
            sessionID = SessionKey.getSessionKey();     // セッションＩＤを取得
            cartID = (int)((int)((sessionID * 37) + siteChCode) & 0x7fffffff);
            cookieID = 0;
        }
        startTime = lastTime = System.currentTimeMillis();     // 現在時刻を取得
        if (debug) {
            if (sdf == null) sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date date = new java.util.Date(startTime);
            synchronized (sdf) {
                System.out.println("SessionObject#init: sesssionID = "+sessionID+
                                   "time = "+ date);
            }
        }
        setHeader(hash);
        // hashItemData を作成する
        ModuleManager moduleManager = SiteManager.get(siteChCode);
        if (moduleManager == null) {
            System.out.println("SessionObject.init: そのサイトチャネルコード("+siteChCode
                +") のModuleManagerは取得できません");

        } else {
            moduleManager.createUserTable(this);
        }
    }

    /**
        このセッションの参加しているModuleManagerを取得する
        @return ModuleManager
    */
    public ModuleManager getModuleManager() {
        return  SiteManager.get(siteCode*100+channelCode);
    }
//  /**
//      サイトコードを設定する<br>
//      ※設定個所未定
//      @param siteCode サイトコード
//  */
//  public void setSiteCode(int siteCode) {
//      this.siteCode = siteCode;
//  }
    protected void setLastTime(long l) { lastTime = l; }
    /** アクセス開始時刻・最終アクセス時刻を更新する */
    public    void updateStartTime() {
        startTime = lastTime = System.currentTimeMillis();
        update();
    }
    /** 最終アクセス時刻を更新する */
    public    void updateLastTime()  {
        lastTime = System.currentTimeMillis();
        update();
    }
    /** セッションタイムアウトまでの時間(ミリ秒)を取得する */
    public long getSessionTimeOut() {
        return SystemManager.sessionTimeOutSec * 60000L
               -(System.currentTimeMillis() - lastTime);
    }
    /** アクセスタイムアウトまでの時間(ミリ秒)を取得する */
    public long getAccessTimeOut() {
        return SystemManager.accessMaxSec * 60000L
               -(System.currentTimeMillis() - startTime);
    }

    /** UserAgent をアップデート */
    public void updateUserAgent() {
        Hashtable<CharArray,CharArray> data = new Hashtable<CharArray,CharArray>();
        boolean convert = SystemManager.ini.getBoolean("[UserAgentEmurator]","convert");
        for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            String value = (String)request.getHeader(key);
            CharArray chKey   = new CharArray(key).toLowerCase('_','-').trim();
            CharArray chValue = new CharArray(value).trim();
            if (convert && chKey.equals("user-agent")) {    // デバッグ用
                String str = getParameter("emu");
                if (str != null && str.length() > 0) {
                    CharArray ch = SystemManager.ini.get("[UserAgentEmurator]",str);
                    if (ch != null) {
                        System.out.println(((count>0)? count+"|":"")+
                                "UserAgentを["+chValue+"]"+
                                           "       から["+ch+"]に変更します:"+str);
                        chValue.set(ch);
                    }
                }
            }
            data.put(chKey, chValue);
        }
        setHeader(data);
    }
    /**
     * Header 情報を設定する。ユーザーエージェント情報を取得する。
     */
    public void setHeader(Hashtable<CharArray,CharArray> hash) {
        if (debugHeader) System.out.println("setHeader("+hash+")");
        if (hash == null) return;
        this.hashHeader = hash;
        userAgent = UserAgent.createUserAgent(this, count);
        setLanguage();
    }
    public void setHeader(Hashtable<CharArray,CharArray> hash, int _count) {
        if (debugHeader) System.out.println("setHeader("+hash+","+_count+")");
        if (hash == null) return;
        this.hashHeader = hash;
        userAgent = UserAgent.createUserAgent(this, _count);
        setLanguage();
    }
    public void setLanguage() {
        if (userAgent != null) {
            setLanguage(userAgent.language);
        }
        if (language.equals("HTML")) {
            setRenderer("HTML");
            setItemRenderer(com.miraidesign.renderer.html.HtmlRenderer.getInstance());
        } else if (language.equals("XHTML")) {
            //setRenderer("XML");
            setRenderer("XHTML");
            setLanguage("XHTML");
            setItemRenderer(com.miraidesign.renderer.xhtml.XhtmlRenderer.getInstance());
        } else {
            System.out.println("UserAgent.language("+language+")が理解できません");
        }
        if (debug) {
            System.out.println("◆◆ Renderer["+renderer+
                               "] Language["+language+"]");
        }
    }

    /**
        UserAgent を取得する
     */
    public UserAgent getUserAgent() {
        if (userAgent == null && hashHeader != null) {
            userAgent = UserAgent.createUserAgent(this, count);
            setLanguage(userAgent.language);
        }
        return userAgent;
    }

    /**
        サイトコードを取得する
        @return サイトコード
    */
    public int getSiteCode() { return siteCode; }

    /**
        チャネルコードを取得する
        @return チャネルコード
    */
    public int getChannelCode() { return channelCode; }

    /**
        サイト＋チャネルコードを取得する
        @return チャネルコード
    */
    public int getSiteChannelCode() { return siteCode*100+channelCode; }

    /**
        サイト＋チャネルコードを設定する
        @param  siteChCode サイト＋チャネルコード
    */
    public void setSiteChannelCode(int siteChCode) {
        this.siteCode    = siteChCode /100;
        this.channelCode = siteChCode % 100;
//System.out.println("☆siteCh:"+siteChCode+" を設定しました");
    }

    /**
        セッションＩＤを取得する
        @return セッションＩＤ
    */
    public int getSessionID() { return sessionID; }

    public void setSessionID(int id) {
        if (this.sessionID != id) {
            this.sessionID = id;
            refreshItemData();
        }
    }

    /**
        CartIDを取得する
        @return CartID
    */
    public int getCartID() { return cartID; }
    /** CartIDを設定する */
    public void setCartID(int id) { this.cartID = id; }

    /**
        pcIDを取得する
        @return pcID
    */
    public int getPCID() { return pcID; }
    /** pcIDを設定する */
    public void setPCID(int id) { this.pcID = id; }

    /**
        S=xxx 形式のセッション文字列を取得する
    */
    public String getSessionStr() {
      if (SystemConst.cryptSessionID) {
          return SystemConst.sessionIDKey[1] + "=" + Crypt62.encode(sessionID);
      } else {
          return SystemConst.sessionIDKey[0] + "=" + sessionID;
      }
    }

    /**
        オブジェクト生成時刻（ミリ秒）を取得する
        @return 開始時刻
    */
    public long getStartTime() { return startTime; }
    public long getLastTime() { return lastTime; }

    /**
        ユーザーハッシュテーブルを返す
        @return ユーザーハッシュテーブル
    */
    public Hashtable<IntObject,ItemData> getHashItemData() { return hashItemData; }
    public Hashtable<IntObject,ItemData> getHashtable() { return hashItemData; }

    public void refreshItemData() {
        for (Enumeration e = hashItemData.elements(); e.hasMoreElements();) {
            ItemData id = (ItemData)e.nextElement();
            id.setSessionObject(this);
        }
    }

    /** パラメータを返す 存在しないときは null で返す */
    public String[] getParameters(String key) {
        String[] strs = null;
        if (hashParameter != null) {
            return (String[])hashParameter.get(key);
        }
        return null;
    }
    /** パラメータを返す 存在しないときは "" で返す */
    public String getParameter(String key) {
        String[] strs = null;
        if (hashParameter != null) {
            strs = (String[])hashParameter.get(key);
        }
        if (strs == null) return "";
        return strs[0];
    }
    /** パラメーターを設定する
        @param key キー
        @param data データ
        @return true:成功
    */
    public boolean setParameter(String key,String data) {
        String[] datas = { data };
        return setParameter(key,datas);
    }
    /** パラメーターを設定する
        @param key キー
        @param data データ
        @return true:成功
    */
    public boolean setParameter(String key,CharArray data) {
        String[] datas = { data.toString() };
        return setParameter(key,datas);
    }
    /** パラメーターを設定する
        @param key キー
        @param data データ
        @return true:成功
    */
    public boolean setParameter(CharArray key,CharArray data) {
        String[] datas = { data.toString() };
        return setParameter(key.toString(),datas);
    }
    /** パラメーターを設定する
        @param key キー
        @param data データ
        @return true:成功
    */
    public boolean setParameter(String key,int data) {
        String[] datas = { ""+data };
        return setParameter(key,datas);
    }
    /** パラメーターを設定する
        @param key キー
        @param datas データ配列
        @return true:成功
    */
    public boolean setParameter(String key,CharArrayQueue datas) {
        String[] _datas = datas.toStringArray();
        return setParameter(key,_datas);
    }
    /** パラメーターを設定する
        @param key キー
        @param datas データ配列
        @return true:成功
    */
    public boolean setParameter(String key,String[] datas) {
        if (hashParameter != null) {
            hashParameter.remove(key);
            hashParameter.put(key,datas);
            return true;
        }
        return false;
    }

    /** 指定パラメーターを削除する
        @param key キー
    */
    public void removeParameter(String key) {
        if (hashParameter != null) {
            hashParameter.remove(key);
        }
    }
    /** 保存URIに追加する
        @param key キー
    */
    public void addURI(String key) {
if (debugPaging) System.out.println("★session.URI:"+key);
        tokenURI.enqueue(key);

    }
    /** 保存URIより、URI指定パラメーターを削除する
        @param key キー
    */
    public void removeFromURI(String key) {
if (debugPaging) System.out.println("★session.removeFromURI:"+key);
        if (tokenURI != null) {
            int size = tokenURI.size();
            for (int i = 0; i < size; i++) {
                if (tokenURI.peek(i).equals(key)) {
                    tokenURI.remove(i);
                    i++; size--;
                }
            }
        }
    }
    /** 保存URIより、URI指定パラメーターを削除する
        @param key キー
    */
    public void removeURIStartsWith(String key) {
if (debugPaging) System.out.println("★session.removeURIStartsWith:"+key);
        if (tokenURI != null) {
            int size = tokenURI.size();
            for (int i = 0; i < size; i++) {
                if (tokenURI.peek(i).startsWith(key)) {
                    tokenURI.remove(i);
                    i++; size--;
                }
            }
        }
    }
    /** パラメータデバッグ表示 */
    public void debugParameters() {
        debugParameters(null);
    }
    public void debugParameters(SessionObject session) {
        if (hashParameter != null && (debug || debugParameter)) {
            String sz = (session!= null) ? session.count+"|" : "";
            for (Enumeration e = hashParameter.keys(); e.hasMoreElements();) {
                String _key = (String)e.nextElement();
                String[] _data = (String[])hashParameter.get(_key);
                System.out.println(sz+"["+_key+"]"+_data[0]);
            }
        }
    }
    /** パラメータをアイテムにリロードする */
    public void reloadParameter() {
        if (mdServlet != null) {
            mdServlet.setValueToItem(this, count);
        }
    }

    /** ヘッダを返す 存在しないときは null で返す */
    public CharArray getHeader(CharArray key) {
        CharArray ch = null;
        if (hashHeader != null) {
            ch = (CharArray)hashHeader.get(key);
        }
        return ch;
    }
    public CharArray getHeader(String str) {
        CharArray key = CharArray.pop(str);
        CharArray ch = getHeader(key);
        CharArray.push(key);
        return ch;
    }
    /** ヘッダを設定する
        @param key キー
        @param data データ
        @return true:成功
    */
    public boolean setHeader(CharArray key, CharArray data) {
        if (debugHeader) System.out.println("setHeader("+key+","+data+")"+hashHeader);
        if (hashHeader != null) {
            hashHeader.remove(key);
            hashHeader.put(key,data);
            return true;
        }
        return false;
    }

    /**
        リモートIPを取得する(nginx対応)
    */
    public String getRemoteIP() {
        CharArray ch = getHeader("x-forwarded-for");
        if (ch != null && ch.length()> 0) return ch.toString();
        ch = getHeader("x-remote-ip");
        if (ch != null && ch.length()> 0) return ch.toString();
        ch = getHeader("x-real-ip");
        if (ch != null && ch.length()> 0) return ch.toString();
        return getRemoteAddr();
    }
    public String getRealIP() {
        CharArray ch = getHeader("x-real-ip");
        if (ch != null && ch.length()> 0) return ch.toString();
        return getRemoteAddr();
    }

    public int getRemotePort() {
        CharArray ch = getHeader("x-forwarded-port");
        if (ch != null && ch.length()> 0) return ch.getInt();
        return 80;
    }

    public String getRemoteProto() {
        CharArray ch = getHeader("x-forwarded-proto");
        if (ch != null && ch.length()> 0) return ch.toString();
        return "http";
    }

    /**
        レンダラーを設定する
        @param ch レンダラー（"HTML", "XML")
    */
    public void setRenderer(CharArray ch) {
        renderer.set(ch);
        renderer.toUpperCase();
        if (renderer.equals("HTML")) {
            setItemRenderer(com.miraidesign.renderer.html.HtmlRenderer.getInstance());
        } else if (renderer.equals("XHTML")) {
            setItemRenderer(com.miraidesign.renderer.xhtml.XhtmlRenderer.getInstance());
        }
    }

    /**
        レンダラーを設定する
        @param ch レンダラー（"HTML", "XML")
    */
    public void setRenderer(String ch) {
        renderer.set(ch);
        renderer.toUpperCase();
        if (renderer.equals("HTML")) {
            setItemRenderer(com.miraidesign.renderer.html.HtmlRenderer.getInstance());
        } else if (renderer.equals("XHTML")) {
            setItemRenderer(com.miraidesign.renderer.xhtml.XhtmlRenderer.getInstance());
        }
    }

    /**
        レンダラーを取得する
        @return レンダラー ("HTML", "XML")
    */
    public CharArray getRenderer() { return renderer; }

    /** 出力言語を設定する
        @param ch "HTML", "CHTML","HDML","MML"
    */
    public void setLanguage(CharArray ch) {
        language.set(ch);
    }
    /** 出力言語を設定する
        @param str "HTML", "CHTML","HDML","MML"
    */
    public void setLanguage(String str) {
        language.set(str);
    }
    /** 出力言語を取得する
        @return "HTML", "CHTML","HDML","MML"
    */
    public CharArray getLanguage() {
        return language;
    }

    /**
        ItemDataを返す
        @param mixedID Mixed-ID
        return ItemDataオブジェクト

    */
    public ItemData getItemData(int mixedID) {  // ItemDataを返す
        IntObject key = IntObject.pop(mixedID);
        ItemData id = (ItemData)hashItemData.get(key);
        IntObject.push(key);
        return id;
    }

    /**
        端末に応じた最適表示行数のヒントを返す
    */
    public int getPageMax() {
        ModuleManager mm = getModuleManager();
        if (mm != null) {
            int _max = isPC() ? mm.getPageMax() : mm.getPageMaxMobile();
            if (_max > 0) return _max;
        }
        return isPC() ? 30 : 10;
    }

    /** セッションを保存する
        デフォルト拡張子 .ss
    */
    public void save() {
        SessionManager.save(this);
    }
    /** セッション指定拡張子でを保存する <br>
        ex)  save(".save");
    */
    public void save(String str) {
        SessionManager.save(this,str);
    }

    //----------------------------------------------------------
    // シリアライズ用 （ユーザーテーブルのみ）
    //---------------------------------------------------------
    public void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            out.writeInt(ref_count);  // 読み込まない
            
            out.writeInt(siteCode);
            out.writeInt(channelCode);
            out.writeUTF((userID != null) ? userID : "");
            out.writeUTF((password !=null) ? password : "");
            out.writeUTF((deviceID !=null) ? deviceID : "");
            out.writeBoolean(authorized);
            out.writeInt(level);

            int size = hashItemData.size();
            if (debug) {
                System.out.println("SessionObject#writeObject size="+size);
            }
            out.writeInt(size);
            int i = 0;
            for (Enumeration e = hashItemData.keys(); e.hasMoreElements();) {
                if (debugWrite) System.out.print("["+(++i)+"/"+size+"]");
                IntObject key = (IntObject)e.nextElement();
                ItemData  id  = (ItemData)hashItemData.get(key);
                if (debugWrite) System.out.print("key="+key.getValue()+" id:"+(id!=null));
                out.writeInt(key.getValue());
                id.writeObject(out);
            }
            userData.writeObject(out);
            userTemplate.writeObject(out);
            checkData.writeObject(out);
            
            if (hashMetaHttp != null && hashMetaHttp.size()>0) {
                out.writeBoolean(true);
                hashMetaHttp.writeObject(out);
            } else {
                out.writeBoolean(false);
            }
            if (hashMetaName != null && hashMetaName.size()>0) {
                out.writeBoolean(true);
                hashMetaName.writeObject(out);
            } else {
                out.writeBoolean(false);
            }
            if (hashHeaderParameter != null && hashHeaderParameter.size()>0) {
                out.writeBoolean(true);
                hashHeaderParameter.writeObject(out);
            } else {
                out.writeBoolean(false);
            }
            
            renderer.writeObject(out);
            language.writeObject(out);
            theme.writeObject(out);
            LANG.writeObject(out);
            CC.writeObject(out);
            SC.writeObject(out);
            role.writeObject(out);
            out.writeInt(access);
            
            out.writeInt(authID);
        }
    }

    public void readObject(DataInput in)  throws IOException {
        if (in != null) {
if (debugRead) System.out.println("SessionObject readObject version:"+this.version);
            in.readInt();   // ref_count 読み込まない

            siteCode = in.readInt();
            if (debugRead) System.out.println("siteCode:"+siteCode);
            channelCode = in.readInt();
            if (debugRead) System.out.println("channelCode:"+channelCode);
            userID = in.readUTF();
            password = in.readUTF();
            deviceID = in.readUTF();
            authorized = in.readBoolean();
            
            level = in.readInt();
            if (debugRead) System.out.println("level:"+level);
            
            int size = in.readInt();
            if (debug || debugRead) {
                System.out.println("SessionObject#read size="+size+" hashSize="+hashItemData.size());
            }

            boolean convert = false;

            for (int i = 0; i < size; i++) {
                if (debugRead) System.out.print("["+(i+1)+"/"+size+"]");
                int iKey = in.readInt();
                IntObject key = new IntObject(iKey);
                ItemData  id  = (ItemData)hashItemData.get(key); // デフォルトのデータに

                if (debugRead || id == null) {
                    String type = (id != null) ? id.getTypeName() : "";
                    System.out.println("key="+key.getValue()+" id:"+(id!=null)+ " "+type);
                }
                id.readObject(in);  // データを上書きする
                id.setSessionObject(this);
            } // next
            userData.readObject(in);
            userTemplate.readObject(in);
            
            checkData.readObject(in);

            if (in.readBoolean()) {
                if (hashMetaHttp == null) hashMetaHttp = new HashParameter();
                hashMetaHttp.readObject(in);
            } else if (hashMetaHttp != null) {
                hashMetaHttp.clear();
            }
            if (in.readBoolean()) {
                if (hashMetaName == null) hashMetaName = new HashParameter();
                hashMetaName.readObject(in);
            } else if (hashMetaName != null) {
                hashMetaName.clear();
            }
            if (in.readBoolean()) {
                if (hashHeaderParameter == null) hashHeaderParameter = new HashParameter();
                hashHeaderParameter.readObject(in);
            } else if (hashHeaderParameter != null) {
                hashHeaderParameter.clear();
            }

            renderer.readObject(in);
            language.readObject(in);
            theme.readObject(in);
            LANG.readObject(in);
            CC.readObject(in);
            SC.readObject(in);
            role.readObject(in);
            access = in.readInt();
            authID = in.readInt();
        }
    }
    //-------------------------------------------------------------
    public void debugHash() {
        int size = hashItemData.size();
        System.out.println("▼sessionHash-----");
        int i = 0;
        CharArray ch = CharArray.pop();
        for (Enumeration e = hashItemData.keys(); e.hasMoreElements();) {
            ch.set("{"+(++i)+"/"+size+"}");
            IntObject key = (IntObject)e.nextElement();
            ItemData  id  = (ItemData)hashItemData.get(key);

            ch.format(key.getValue(), 10, 13, ',');
            ch.add(" ");
            ch.add(id.getTypeName());
            System.out.println(ch.toString());
        }
        CharArray.push(ch);
        System.out.println("▲sessionHash-----");
    }
    //-------------------------------------------------------------
    public int hashCode() { return sessionID; }
    /**
        辞書式に比較する
        @param obj CharArray/String/StringBuffer
        @return 0:同じ -1:自分が小さい 1:大きい -99:引数エラー
    */
    public int compareTo(Object obj) {
        if (obj instanceof SessionObject) {
            return sessionID - ((SessionObject)obj).sessionID;
        }
        return -99;
    }


    static final String startStr = "@[";
    static final String endStr = "]";

    // 動的テンプレート置換を行う(Message, Color)
    public void replaceTemplate(CharArray ch) {
        long timer = Util.Timer();
if (debug || debugTimer || debugTemplate) System.out.println("◆SessionObject#replaceTemplate--------------LANG:"+LANG+" theme:"+theme);
        ModuleManager mm = getModuleManager();
        if (ch == null || mm == null) return;
        int start = 0;
        int startPos = ch.indexOf(startStr);
        if (startPos < 0) return;
        boolean changed = false;
        CharArray tmp = CharArray.pop();
        CharArray key = CharArray.pop();
        while (startPos >= 0) {
            tmp.add(ch,start, startPos-start);
            startPos += startStr.length();
            // 終了タグ検索
            int endPos = ch.indexOf(endStr, startPos);
            if (endPos < 0) break;
            //キーワード抽出
            key.set(ch, startPos, endPos-startPos);

            CharArray msg = null;
            int _size = 2;
            if (key.startsWith("Message.")) {msg = mm.getMessage(this,key.substring(8)); _size=8;}
            else if (key.startsWith("M.")) msg = mm.getMessage(this,key.substring(2));
            else if (key.startsWith("M:")) msg = mm.getMessage(this,key.substring(2));
            else if (key.startsWith("Color.")) {  msg = mm.getColor(this,key.substring(6)); _size=6;}
            else if (key.startsWith("C."))   msg = mm.getColor(this,key.substring(2));
            else if (key.startsWith("C:"))   {
                msg = mm.getColor(this,key.substring(2));
            }
            if (msg != null) {
                changed = true;
                tmp.add(msg);
if (debugTemplate) System.out.println("★SessionObject#replaceTemplate:"+key+"->"+msg);
                start = endPos + endStr.length();
            } else {
if (debugTemplate) System.out.println("★SessionObject#replaceTemplate:"+key+" not found !");
                tmp.add(startStr);  // 2010-04-26
                tmp.add(key.substring(0,_size));
                start = startPos + startStr.length();
            }
            startPos = ch.indexOf(startStr, start);
        }
        if (changed) {
            tmp.add(ch, start);
            ch.set(tmp);
        }
        CharArray.push(key);
        CharArray.push(tmp);
if (debugTimer) System.out.println("◆SessionObject#replaceTemplate Lapse:"+Util.Lapse(timer));
    }

    //-----------------------------------------------

    /** メッセージの取得 */
    public CharArray getMessage(String str) {
        return getMessage(str, false);
    }
    /** メッセージの取得 */
    public CharArray getMessage(CharArray str) {
        return getMessage(str, false);
    }
    /** メッセージの取得 */
    public CharArray getMessage(String str, boolean showMessage) {
        ModuleManager mm = getModuleManager();
        if (mm == null) return null;
        return mm.getMessage(this, str, showMessage);
    }
    /** メッセージの取得 */
    public CharArray getMessage(CharArray str, boolean showMessage) {
        ModuleManager mm = getModuleManager();
        if (mm == null) return null;
        return mm.getMessage(this, str, showMessage);
    }
    /** 色情報の取得 */
    public CharArray getColor(String str) {
        return getColor(str, false);
    }
    /** 色情報の取得 */
    public CharArray getColor(CharArray str) {
        return getColor(str, false);
    }
    /** 色情報の取得 */
    public CharArray getColor(String str, boolean showMessage) {
        ModuleManager mm = getModuleManager();
        if (mm == null) return null;
        return mm.getColor(str, showMessage);
    }
    /** 色情報の取得 */
    public CharArray getColor(CharArray str, boolean showMessage) {
        ModuleManager mm = getModuleManager();
        if (mm == null) return null;
        return mm.getColor(str, showMessage);
    }

    /** SessionManagerのupdateを呼ぶ */
    public void update() {
        SessionManager.update(this);
    }

    public String szSQLPrefix = "";
    public void setSQLPrefix(String str) { szSQLPrefix = str; }

    /** SQL Prefix を取得する <br>
        存在しない場合はModuleManager のSQL Prefixを返す
    */
    public String getSQLPrefix() {
        if (szSQLPrefix != null && szSQLPrefix.length() > 0) {
            return szSQLPrefix;
        }
        ModuleManager mm = getModuleManager();
        return  (mm != null) ? mm.szSQLPrefix : szSQLPrefix;
    }

    /** 認証ID自動追加モード取得 */
    public boolean isAppendAuthID() {
        ModuleManager mm = getModuleManager();
        if (mm == null) return SystemManager.appendAuthID;
        return mm.isAppendAuthID();
    }

    /** count,ユーザー名付きメッセージを出力する */
    public void print(CharSequence strs) {
        System.out.print(message(this, strs));
    }
    public void println(CharSequence strs) {
        System.out.println(message(this, strs));
    }
    /** count,ユーザー名付きメッセージを取得する
        session がnullの時はメッセージのみ出力
    */
    static public void print(SessionObject session, CharSequence strs) {
        System.out.print(message(session, strs));
    }
    static public void println(SessionObject session, CharSequence strs) {
        System.out.println(message(session, strs));
    }
    /** count,ユーザー名付きメッセージを取得する */
    static public String message(SessionObject session, CharSequence strs) {
        CharArray ch = CharArray.pop();
        if (session != null) {
            if (session.count > 0) {
                ch.format(session.count);
                ch.add('|');
            }
            if (session.userID != null && session.userID.length() > 0) {
                ch.add('(');
                ch.add(session.userID);
                ch.add(')');
            } else if (session.userName != null && session.userName.length() > 0) {
                ch.add('(');
                ch.add(session.userName);
                ch.add(')');
            }
        }
        ch.add(strs);
        String str = ch.toString();
        CharArray.push(ch);
        return str;
    }
}

//
// [end of SessionObject.java]
//

