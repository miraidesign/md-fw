//------------------------------------------------------------------------
//@(#)ModuleManager.java
//          チャネルごとの情報とModuleの管理を行う
//          Copyright (c) MiraiDesign. 2010 All Rights Reserved.
//------------------------------------------------------------------------
package com.miraidesign.system;

import java.util.Enumeration;
import java.util.Hashtable;

import com.miraidesign.common.SystemConst;
import com.miraidesign.content.ContentMacro;
//import com.miraidesign.image.EmojiConverter;
import com.miraidesign.mail.SendMail;
import com.miraidesign.renderer.ItemInfo;
import com.miraidesign.renderer.Page;
import com.miraidesign.renderer.Module;
import com.miraidesign.renderer.item.Item;
import com.miraidesign.session.SessionObject;
import com.miraidesign.session.UserAgent;
import com.miraidesign.servlet.Loader;
import com.miraidesign.servlet.ModuleServlet;
import com.miraidesign.servlet.PageServlet;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.IniFile;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.QueueFile;
import com.miraidesign.util.Util;


/**
 *  チャネルごとのModule管理を行います。
 *
 *  @see com.miraidesign.system.SiteManager
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ModuleManager {
    static private boolean debug = (SystemConst.debug && true);  // デバッグ表示
    static private boolean debug2 = (SystemConst.debug && false);  // デバッグ表示
    static private boolean debugInfo = (SystemConst.debug && true);  // デバッグ表示 false
    static private boolean debugIP = (SystemConst.debug && false);  // デバッグ表示
    static private boolean debugMailTemplate = (SystemConst.debug && false);  // デバッグ表示

    // cookie関連
    public int pcCookieMaxHour   = 48;   // PC判別用クッキー情報の最大保持時間 
    public int cartCookieMaxHour = 48;   // ショッピングカートクッキー情報の最大保持時間 
    public boolean readCookie  = true;   // cookie 読込を行う
    public boolean writeCookie = true;   // cookie 書き込みを行う


    protected Module defaultModule;
    public void setDefaultModule(Module m) {
        defaultModule = m;
        if (debug) {
            System.out.println(siteName+"("+siteKey+":"+siteCode+")/"
                               +channelName+"("+channelCode+
                    ") に Module ID="+m.getModuleID()+
                " ("+m.getName()+") をデフォルト設定します");
        }
   }
    public Module getDefaultModule() { return defaultModule;}

    private HashVector<IntObject,Module> hashModule = 
        new HashVector<IntObject,Module>();  // Module を保管(key:ModuleID)
    private HashVector<CharArray,Module> hashName   = 
        new HashVector<CharArray,Module>();  // Module を保管(key:ModuleName)

    public HashVector<IntObject,Module> getHashID() { return hashModule;}
    public HashVector<CharArray,Module> getHashName() { return hashName;}

    /**
        'ModuleKey:PageKey' と ページタイトルのリストを管理する
    */
    private HashParameter hpTitleKeyList = new HashParameter();
    
    /** 'ModuleKey:PageKey'とページタイトルのリストを返す */
    public HashParameter getTitleKeyList() { return hpTitleKeyList;}
    

    private String siteName;         // サイト名       ex: "角川書店"
    public void setSiteName(String sitename) { this.siteName = sitename; }
    public String getSiteName() { return siteName;}

    private int siteCode;           // サイトコード
    protected void setSiteCode(int sitecode) { this.siteCode = sitecode; }
    /** サイトコードを取得する */
    public int getSiteCode() { return siteCode; }
    
    /** サイトコードを３桁の文字列で取得する */
    public String getSiteCodeString() {
        return Util.format0(siteCode,3); 
    }

    //
    private String siteKey;         // サイトキー名称    ex:  "Kadokawa"
    public void setSiteKey(String sitekey) { this.siteKey = sitekey; }
    
    /** サイトキー名称（セクションに使用する文字列）を取得する */
    public String getSiteKey() { return siteKey;}

    //
    protected String channelName;         // チャネル名称 （保管のみ）
    public void setChannelName(String channelname) { this.channelName = channelname; }
    /** チャネル名を取得する */
    public String getChannelName() { return channelName;}

    protected int channelCode;            // チャネルコード
    protected void setChannelCode(int channelcode) { this.channelCode = channelcode; }
    /** チャネルコードを取得する */
    public int getChannelCode() { return channelCode; }
    /** チャネルコードを２桁の文字列で取得する */
    public String getChannelCodeString() { 
        return Util.format0(channelCode,2); 
    }

    /** サイトコード＋チャネルコードを取得する */
    public int getSiteChannelCode() { return siteCode*100 + channelCode; }
    /** サイトコード＋チャネルコードを５桁の文字列で取得する */
    public String getSiteChannelCodeString() { 
        return Util.format0(siteCode*100 + channelCode,5); 
    }

    protected int subCode = 1;           // ハブサイトサブコード

    /** ハブサイトサブコードを取得する */
    public int getSiteSubCode() { return subCode; }
    
    //-----------------------------------------theme 関連
    /** テーマセット 情報を保管
        key:CharArray(theme) data:CharArrayQueue(themeData, themeName) */
    protected HashVector<CharArray,CharArrayQueue> hashTheme = new HashVector<CharArray,CharArrayQueue>();
    public HashVector<CharArray,CharArrayQueue> getHashTheme() { return hashTheme;}
    
    /** サイトテーマ */
    protected CharArray theme = new CharArray();
    public void setTheme(String str) { theme.set(str);}
    public void setTheme(CharArray ch) { theme.set(ch);}
    /** デフォルトのテーマを取得する */
    public CharArray getTheme() { return theme;}
    
    /** テーマ 情報を取得する */
    public CharArray getThemeData() { 
        CharArrayQueue queue = hashTheme.get(theme);
        if (queue != null && queue.size() > 0) return queue.peek();
        else return null;
    }
    /** テーマ 情報を取得する */
    public CharArray getThemeData(CharArray key) { 
        if (key == null || key.length() == 0) return getThemeData();
        return hashTheme.get(key).peek();
    }
    
    /** テーマ 情報を取得する */
    public CharArray getThemeData(String key) { 
        if (key == null || key.length() == 0) return getThemeData();
        CharArray chKey = CharArray.pop(key);
        CharArray ch = hashTheme.get(chKey).peek();
        CharArray.push(chKey);
        return ch;
    }
    
    /** テーマ 名称を取得する */
    public CharArray getThemeName() { 
        CharArrayQueue queue = hashTheme.get(theme);
        if (queue != null && queue.size() > 1) return queue.peek(1);
        else return null;
    }
    /** テーマ 名称を取得する */
    public CharArray getThemeName(CharArray key) { 
        if (key == null || key.length() == 0) return getThemeName();
        return hashTheme.get(key).peek(1);
    }
    
    /** テーマ 名称を取得する */
    public CharArray getThemeName(String key) { 
        if (key == null || key.length() == 0) return getThemeName();
        CharArray chKey = CharArray.pop(key);
        CharArray ch = hashTheme.get(chKey).peek(1);
        CharArray.push(chKey);
        return ch;
    }
    
    //------------------------------------------------Lang関連
    /** LANGセット 情報を保管
        key:CharArray(LANG) data:CharArrayQueue(langData,langName) */
    protected HashVector<CharArray, CharArrayQueue> hashLang = new HashVector<CharArray, CharArrayQueue>();
    public HashVector<CharArray, CharArrayQueue> getHashLang() { return hashLang;}
    
    /** LANG情報 **/
    protected CharArray LANG = new CharArray();
    public void setLang(String str) { LANG.set(str);}
    public void setLang(CharArray ch) { LANG.set(ch);}

    /** デフォルトのLANGを取得する */
    public CharArray getLang() { return LANG;}
    
    /** LANG 情報を取得する */
    public CharArray getLangData() { 
        CharArrayQueue queue = hashLang.get(LANG);
        if (queue != null && queue.size() > 0) return queue.peek();
        else return null;
    }
    /** LANG 情報を取得する */
    public CharArray getLangData(CharArray key) { 
        if (key == null || key.length() == 0) return getLangData();
        CharArray ch = null;
        CharArrayQueue queue = hashLang.get(key);
        if (queue != null && queue.size() > 0) ch = queue.peek();
        return ch;
        //return ((CharArrayQueue)hashLang.get(key)).peek();
    }
    /** LANG 情報を取得する */
    public CharArray getLangData(String key) { 
        if (key == null || key.length() == 0) return getLangData();
        CharArray chKey = CharArray.pop(key);
        CharArray ch = null;
        CharArrayQueue queue = hashLang.get(chKey);
        if (queue != null && queue.size() > 0) ch = queue.peek();
        CharArray.push(chKey);
        return ch;
    }
    
    // 元文字 @[M:Japanese]等は残すようにすること！
    // レンダリング時に変換されれば良い
          
    /** LANG 名称を取得する */
    public CharArray getLangName() { 
        CharArrayQueue queue = hashLang.get(LANG);
        if (queue != null && queue.size() > 1) return queue.peek(1);
        else return null;
    }
    /** LANG 名称を取得する */
    public CharArray getLangName(CharArray key) { 
        if (key == null || key.length() == 0) return getLangName();
        CharArrayQueue queue = hashLang.get(key);
        if (queue != null && queue.size() > 1) return queue.peek(1);
        else return null;
    }
    /** LANG 名称を取得する */
    public CharArray getLangName(String key) { 
        if (key == null || key.length() == 0) return getLangName();
        CharArray chKey = CharArray.pop(key);
        CharArray ch = null;
        CharArrayQueue queue = hashLang.get(chKey);
        if (queue != null && queue.size() > 1) ch = queue.peek(1);
        CharArray.push(chKey);
        return ch;
    }
    
    //---- charset 関連 ----------------------------------------------
    /** サイトデフォルト アプリでは使用しない事*/
    public String contentType = "";
    /** サイトデフォルト アプリでは使用しない事*/
    public String charSet = "";
    /** サイトデフォルト アプリでは使用しない事*/
    public String charCode = "";
    
    //-----------------------------------------------------------------
    private String header;              // ヘッダ文字
    public void setHeader(String header) { this.header = header; }
    public String getHeader() { return header;}
    //-----------------------------------------------------------------
    private String footer;              // フッター文字
    public void setFooter(String footer) { this.footer = footer; }
    public String getFooter() { return footer;}
    //-----------------------------------------------------------------
    private int searchMax = 500;        // 最大検索数 (０以下で全件)
    private boolean searchNext = false; // データが存在する時にさらに次ページ
    private boolean pageDisplay = false; // 検索結果にページを表示するか？
    private int pageMax    = -1;   // リスト表示件数のヒント
    private int pageWidth = -1;   // ページング表示幅

    private int searchMaxMobile = 500;        // 最大検索数 (０以下で全件)
    private boolean searchNextMobile = false; // データが存在する時にさらに次ページ
    private boolean pageDisplayMobile = false; // 検索結果にページを表示するか？
    private int pageMaxMobile    = -1;   // リスト表示件数のヒント
    private int pageWidthMobile = -1;   // ページング表示幅

    protected void setSearchMax(int max) { searchMax = max; }
    protected void setSearchNext(boolean mode) { searchNext = mode;}
    protected void setPageDisplay(boolean mode) { pageDisplay = mode;}
    protected void setPageMax(int max) { pageMax = max; }
    protected void setPageWidth(int width) { pageWidth = width; }
    
    protected void setSearchMaxMobile(int max) { searchMaxMobile = max; }
    protected void setSearchNextMobile(boolean mode) { searchNextMobile = mode;}
    protected void setPageDisplayMobile(boolean mode) { pageDisplayMobile = mode;}
    protected void setPageMaxMobile(int max) { pageMaxMobile = max; }
    protected void setPageWidthMobile(int width) { pageWidthMobile = width; }
    
    /** 最大検索数のヒントを返す(０以下で全件) */
    public int     getSearchMax() { return searchMax; }
    /** データが存在する時にさらに次ページ検索を行うか？*/
    public boolean getSearchNext() { return searchNext; }
    /** 検索結果にページを表示するか？*/
    public boolean getPageDisplay() { return pageDisplay; }
    /** リスト表示数のヒントを返す*/
    public int  getPageMax() { return pageMax; }
    /** ページング表示幅を返す*/
    public int  getPageWidth() { return pageWidth; }
    
    /** モバイル用：最大検索数のヒントを返す(０以下で全件) */
    public int     getSearchMaxMobile() { return searchMaxMobile; }
    /** モバイル用：データが存在する時にさらに次ページ検索を行うか？*/
    public boolean getSearchNextMobile() { return searchNextMobile; }
    /** モバイル用：検索結果にページを表示するか？*/
    public boolean getPageDisplayMobile() { return pageDisplayMobile; }
    /** モバイル用：リスト表示数のヒントを返す*/
    public int  getPageMaxMobile() { return pageMaxMobile; }
    /** モバイル用：ページング表示幅を返す*/
    public int  getPageWidthMobile() { return pageWidthMobile; }
    
    
    //-----------------------------------------------------------------
    private boolean includingTax;       // 税込みか？
    public void setIncludingTax(boolean it) { includingTax = it;}
    public boolean includingTax() { return includingTax;}

    private int maxCount   = 5;           // 最大購入個数
    private int maxAccount = 50000;       // 最大購入金額
    private int maxKind = 10;             // 最大購入種別
    protected void setMaxCount(int count) { maxCount = count;}
    protected void setMaxAccount(int count) { maxAccount = count;}
    protected void setMaxKind(int count) { maxKind = count;}
    /** 最大購入個数を返す */
    public int getMaxCount() { return maxCount;}
    /** 最大購入金額を返す */
    public int getMaxAccount() { return maxAccount;}
    /** 最大購入種別を返す */
    public int getMaxKind() { return maxKind;}

    private CharArray topPage = new CharArray();
    public void setTopPage(CharArray str) { topPage.set(str); }
    /** TopPage URL の取得 */
    public CharArray getTopPage() { return topPage;}
    
    private CharArray adminBase = new CharArray();
    public void setAdminBase(CharArray str) { adminBase.set(str); }
    /** AdminBase の取得 */
    public CharArray getAdminBase() { return adminBase;}
    
    private CharArray userBase = new CharArray();
    public void setUserBase(CharArray str) { userBase.set(str); }
    /** UserBase の取得 */
    public CharArray getUserBase() { return userBase;}
    //-------------------------------------------------------------------------
    protected boolean appendAuthID = false;
    public boolean isAppendAuthID() {return appendAuthID;}
    
    //-------------------- ロード情報のチェック
    protected boolean checkFilename = true;
    protected boolean checkSitecode = true;
    protected boolean checkVersion = true;
    protected boolean checkTimeout = true;
    protected boolean checkUserAgent = false;
    /** ロード時に ファイル名をチェックするか？ */
    public boolean checkFilename() { return checkFilename;}
    /** ロード時に サイトコードをチェックするか？ */
    public boolean checkSitecode() { return checkSitecode;}
    /** ロード時に バージョンをチェックするか？ */
    public boolean checkVersion() { return checkVersion;}
    /** ロード時に タイムアウトをチェックするか？ */
    public boolean checkTimeout() { return checkTimeout;}
    /** ロード時に ユーザーエージェントをチェックするか？ */
    public boolean checkUserAgent() { return checkUserAgent;}

    //-------------------------------------------------------------------------
    /** テンプレートリプレースキーワード */
    public HashVector<CharArray,CharArrayQueue> hashReplaceKeyword; 
    public HashVector<CharArray,CharArrayQueue> getReplaceKeyword() { return hashReplaceKeyword;}
    /** テンプレートハッシュ **/
    public HashVector<CharArray,CharArrayQueue> hashTemplate;    
    public HashVector<CharArray,CharArrayQueue> getHashTemplate() { return hashTemplate;}
    //-------------------------------------------------------------------------

    private SendMail mail = new SendMail();     // メール送信クラス
    /** メールオブジェクトを取得する **/
    public SendMail getMail() { return mail;}

    //--------------------------------------------- 
    private Hashtable<String,CharArrayQueue> mailTemplate = new Hashtable<String,CharArrayQueue>();

    protected void clearMailTemplate() {
        mailTemplate.clear();
    }
    protected void addMailTemplate(String key, CharArrayQueue file) {
if (debugMailTemplate) System.out.println("▽mail-template----");

        CharArrayQueue newFile = new CharArrayQueue();
        newFile.copy(file);
        
        for (int i = 0; i < newFile.size(); i++) {
            CharArray ch = newFile.peek(i);
            replaceMessageTemplate(ch);
        }
if (debugMailTemplate) {
        //file.dumpQueue();
        //System.out.println("▼mail-template----");
        newFile.dumpQueue();
}
        mailTemplate.put(key, newFile);
if (debugMailTemplate) System.out.println("△mail-template----");
    }
    /** メールテンプレートを取得する
        @param key  キーワード
        @return テンプレートファイル
    */
    public CharArrayQueue getMailTemplate(String key) {
        return (CharArrayQueue)mailTemplate.get(key);
    }
    //--------------------------------------------- 
    // [site.nn.File] セクションのファイル名を取り込む
    private Hashtable<String,CharArrayQueue> hashFileList = new Hashtable<String,CharArrayQueue>();

    protected void clearFileList() {
        hashFileList.clear();
    }
    protected void addFile(String key, CharArrayQueue file) {
        hashFileList.put(key, file);
    }
    /** メールテンプレートを取得する
        @param key  キーワード
        @return テンプレートファイル
    */
    public CharArrayQueue getFile(String key) {
        return (CharArrayQueue)hashFileList.get(key);
    }

    //--------------------------------------------- 
    public IniFile ini;     // sitexx.ini への参照
    public String sectionBase; // セクション基準名  ex. "Cecile.1"

    //-----------------------------------   
    /** ユーザ指定テンプレートを返す */
    public HashVector<CharArray, CharArrayQueue> getUserTemplate() { 
        return ini.getKeyTable(getSection("UserTemplate"));
    }
    //---------------------------------------------  ImageProxy
    public boolean imageProxy = false;  // デフォルトでImageProxyを有効にするか？
    public int imageWidth = -1;         // -1:幅調整なし 0:端末サイズ 1～ 指定サイズ
    public int imageRatio = 100;        // 画像表示比率  
    public int imageBitMax = 0;         // 最大変換ビット数（０で無効） 
    
    public int jan8MaxScanWidth = 200;      // 最大スキャン幅 (0.1mm単位）
    public int qrMaxScanWidth = 200;      // 最大スキャン幅 (0.1mm単位）
    //--------------------------------------------- 
    public Loader loader;   // 

    //---------------------------------------------
    public CharArray naviSeparator = new CharArray(" >> ");

    public boolean showNavigation = false;
    //public boolean showGuidance   = false;
    //public boolean showTitleImage = false;

    protected int naviID = 0;   // ナビゲーションＩＤ (for NaviNode)

    //---------------------------------------------
    public String defaultConnection = "";
    public String szSQLPrefix = "";

    /** DBのデフォルト接続を設定する */
    public void setDefaultConnection(String str) {
        defaultConnection = str.trim();
    }
    /** DBのデフォルト接続を取得する
        存在しない場合はModuleManagerのデフォルト接続を返す
     */
    public String getDefaultConnection() {
        return defaultConnection;
    }
    
    /** SQL Prefix を設定する */
    public void setSQLPrefix(String str) {
        szSQLPrefix = str;
    }
    /** SQL Prefix を取得する */
    public String getSQLPrefix() {
        return szSQLPrefix;
    }
    
    //---------------------------------------------
    // Validate 関連 未使用
//  public HashVector<String,Validate> hashValidate;
//  
//  public void initValidate() {
//      if (hashValidate == null) hashValidate = new HashVector<String,Validate>();
//      String section = getSection("Validation");
//      HashVector<CharArray,CharArrayQueue> vector = ini.getKeyTable(section);
//      if (vector != null) {
//          for (int i = 0; i < vector.size(); i++) {
//              CharArray key   = (CharArray)vector.keyElementAt(i);
//              CharArray value = ((CharArrayQueue)vector.valueElementAt(i)).peek();
//              if (key != null && key.length() > 0 && value != null && value.length() > 0) {
//                  Validate validate = new Validate();
//                  validate.init(value);
//                  hashValidate.put(key.toString(), validate);
//              }
//          }
//      }
//  }
    //---- 
    public String hostURL = "";
    public String httpURL = "";
    public String httpsURL = "";
    public String registURL = "";
    public String cacheServerURL = "";
    //---- 
    public String getHostURL() {
        String str = hostURL;
        if (str == null || str.length() == 0) str = SystemManager.hostURL;
        return str;
    }
    public String getHttpURL() {
        String str = httpURL;
        if (str == null || str.length() == 0) str = SystemManager.httpURL;
        return str;
    }
    
    public String getHttpsURL() {
        String str = httpsURL;
        if (str == null || str.length() == 0) str = SystemManager.httpsURL;
        return str;
    }
    public String getRegistURL() {
        String str = registURL;
        if (str == null || str.length() == 0) str = SystemManager.registURL;
        return str;
    }
    public String getCacheServerURL() {
        String str = cacheServerURL;
        if (str == null || str.length() == 0) str = SystemManager.cacheServerURL;
        return str;
    }

    protected HashVector<CharArray,HashParameter> refHash;   // 入力パラメータ参照
                                    // CharArray key + HashParameter
    public HashVector<CharArray,HashParameter>  getRefHash() { return refHash;}
    public HashParameter getRefParameter(CharArray key) {
        if (refHash == null) return null;
        return refHash.get(key);
    }

    protected ContentMacro contentMacro;
    public ContentMacro getContentMacro() { return contentMacro;}
    public ContentMacro getContentMacro(HashVector<CharArray,CharArrayQueue> hv) { 
        if (contentMacro == null) contentMacro = new ContentMacro();
        contentMacro.createMacro(hv);
        return contentMacro;
    }
    

    protected SiteMapping siteMapping;
    public SiteMapping getSiteMapping() {
        if (siteMapping == null) siteMapping = new SiteMapping(this);
        return siteMapping;
    }

    /** サイトパラメータを取得するex:  /ui/  */
    public CharArray getSiteParameter() {
        return siteMapping.getSiteParameter();
    }
    public CharArray getSiteParameter(SessionObject session) {
        return siteMapping.getSiteParameter(session);
    }

    /** サイト文字列を取得するex:  ui  */
    public CharArray getSiteString() {
        return siteMapping.getSiteString();
    }
    //public CharArray getSiteString(SessionObject session) {
    //    return siteMapping.getSiteString(session);
    //}

    // meta styleのデフォルト設定
    protected CharArrayQueue meta = new CharArrayQueue();
    /** デフォルトメタタグ追加 */
    public void addMeta(CharArray ch) { meta.enqueue(ch);}
    public void addMeta(String str) { meta.enqueue(str);}
    /** デフォルトメタタグ情報を取得する */
    public CharArrayQueue getMeta() { return meta;}
    //------------------------------------------------------
    protected CharArray styleSheet = new CharArray();
    /** デフォルトスタイルシート設定 */
    public void setStyleSheet(CharArray ch) { styleSheet.set(ch);}
    public void setStyleSheet(String str) { styleSheet.set(str);}
    /** デフォルトスタイルシート追加 */
    public void addStyleSheet(CharArray ch) { styleSheet.add(ch);}
    public void addStyleSheet(String str) { styleSheet.add(str);}
    /** デフォルトスタイルシート情報を取得する */
    public CharArray getStyleSheet() { return styleSheet;}
    //------------------------------------------------------
    protected CharArrayQueue[] styleSheetURL = { // キャリア別にする
        new CharArrayQueue(),new CharArrayQueue(),new CharArrayQueue(),new CharArrayQueue()
    };
    /** スタイルシートURL設定 */
    public void addStyleSheetURL(CharArray ch) { styleSheetURL[0].enqueue(ch);}
    public void addStyleSheetURL(String str) { styleSheetURL[0].enqueue(str);}

    /** キャリア別のスタイルシートURL設定 */
    public void addStyleSheetURL(CharArray ch, int carrier) { 
        if (carrier < UserAgent.PC/* || carrier > UserAgent.AU*/) carrier = 0;
        styleSheetURL[carrier].enqueue(ch);
    }
    public void addStyleSheetURL(String str, int carrier) { 
        if (carrier < UserAgent.PC/* || carrier > UserAgent.AU*/) carrier = 0;
        styleSheetURL[carrier].enqueue(str);
    }
    
    /** スタイルシートURLを取得する */
    public CharArrayQueue getStyleSheetURL() { return styleSheetURL[0];}

    /** キャリア別のスタイルシートURLを取得する */
    public CharArrayQueue getStyleSheetURL(int carrier) { 
        if (carrier < UserAgent.PC/* || carrier > UserAgent.AU*/) carrier = 0;
        return styleSheetURL[carrier];
    }
    
    //-------------------------------------------------------------------------
    /** サーバーキー情報 */
    protected String serverKey = "";  // LB振り分け等に使用する
    public void setServerKey(String key) { this.serverKey = key;}
    public String getServerKey() { return serverKey;}
    
    protected int serverKeyFlg = 0;   // 0:適用しない  1:全てに適用  2:モバイルのみ 3:モバイル＋cookie なし端末
    
    /** サーバー名 */ // serer/サーバー名/...
    protected String serverName = "";
    public void setServerName(String name) { this.serverName = name;}
    public String getServerName() { return serverName; }
    
    /** SSL リダイレクトフラグ */
    public boolean SSLRedirect = false;
    /** SSL チェックポート */
    public int SSLCheckPort = 443;
    //-------------------------------------------------------------------------
    // consuructor
    //-------------------------------------------------------------------------
    public ModuleManager() { }
    //public ModuleManager(String sitename) { this.siteName = siteName;}

    /**
        各モジュールの init() を呼び出します
        MDServlet.init() から呼ばれます。
        @see com.miraidesign.servlet.MDServlet#init()
    */
    public void init() {
        for (Enumeration e = hashModule.elements(); e.hasMoreElements();) {
            Module module = (Module)e.nextElement();
            if (debug) {
                System.out.println("Module ID="+module.getModuleID()+
                    " ("+module.getName()+") を初期化中(init)");
            }
            module.init();
            module.setItemInfo();
        }
    }
    public void init(String servletPath) {
        for (Enumeration e = hashModule.elements(); e.hasMoreElements();) {
            Module module = (Module)e.nextElement();
            if (debug) {
                System.out.println("Module ID="+module.getModuleID()+
                    " ("+module.getName()+") を初期化中(init) ");
            }
            module.setServletPath(servletPath);
            module.init();
            module.setItemInfo();
        }
    }

    /**
        SystemManagerから呼ばれる
    */
    public void destroy() {
        for (Enumeration e = hashModule.elements(); e.hasMoreElements();) {
            Module module = (Module)e.nextElement();
            ((ModuleServlet)module).destroy();
        }
    }


    /** アイテム情報をセットする */
    public void setItemInfo(SessionObject session) {
        for (Enumeration e = hashModule.elements(); e.hasMoreElements();) {
            Module module = (Module)e.nextElement();
            if (debugInfo) {
                System.out.println("Module ID="+module.getModuleID()+
                    " ("+module.getName()+") setItemInfo ");
            }
            module.setItemInfo(session);
        }
    }
    /** convert */
    public void convert(SessionObject session) {
        for (Enumeration e = hashModule.elements(); e.hasMoreElements();) {
            Module module = (Module)e.nextElement();
            module.convert(session);
        }
    }
    /** reset */
    public void reset(SessionObject session) {
        for (Enumeration e = hashModule.elements(); e.hasMoreElements();) {
            Module module = (Module)e.nextElement();
            module.reset(session);
        }
    }

    /**
        Moduleを追加する
        @param module 追加するモジュールオブジェクト
    */
    public void add(Module module) {
        hashModule.put(new IntObject(module.getModuleID()), module);
        hashName.put(new CharArray(module.getName()),module);
        module.setModuleManager(this);
        if (debug) {
            System.out.println(siteName+"("+siteKey+":"+siteCode+")/"
                               +channelName+"("+channelCode+
                    ") に Module ID="+module.getModuleID()+
                " ("+module.getName()+") を追加しました");
        }
        if (defaultModule == null) setDefaultModule(module);
    }
    /**
        Moduleを取得する
        @param moduleID  モジュールＩＤ
        @return 指定モジュールの参照を返す
    **/
    public Module getModule(int moduleID) {
        IntObject key = IntObject.pop(moduleID);
        Module module = (Module)hashModule.get(key);
        IntObject.push(key);
        if (debug2) {
            if (module == null) {
                System.out.println("ModuleManager:getModule("+moduleID+") がありません！");
            }
        }
        return module;
    }
    /**
        Moduleを取得する
        @param moduleName  モジュール名
        @return 指定モジュールの参照を返す
    **/
    public Module getModule(String moduleName) {
        CharArray key = CharArray.pop(moduleName);
        Module module = (Module)hashName.get(key);
        CharArray.push(key);
        if (debug2) {
            if (module == null) {
                System.out.println("ModuleManager:getModule("+moduleName+") がありません！");
            }
        }
        return module;
    }
    public Module getModule(CharArray key) {
        Module module = (Module)hashName.get(key);
        if (debug2) {
            if (module == null) {
                System.out.println("ModuleManager:getModule("+key+") がありません！");
            }
        }
        return module;
    }

    /**
        ユーザーハッシュテーブルの作成
        @param session セッション
        @return  ユーザー定義ハッシュテーブル
    **/
    public Hashtable createUserTable(SessionObject session) {
//System.out.println("☆createUserTable session:"+(session!=null)+" site:"+session.getSiteChannelCode());
        Hashtable userHash = session.getHashtable();
        userHash.clear();
        for (Enumeration e1 = hashModule.elements(); e1.hasMoreElements();) {
            Module module = (Module)e1.nextElement();
            for (Enumeration e2 = module.getPageList(); e2.hasMoreElements();) {
                Page page = (Page)e2.nextElement();
                for (Enumeration e3 = page.getItemList(); e3.hasMoreElements();) {
                    Item item = (Item)e3.nextElement();
                    item.copy(session);
                }
            }
        }
        return userHash;
    }

    /**
        このサイトに属するテンプレート関連ファイルを全て読み直す
        (templateタグの file と data)
    */
    public void refreshTemplate() {
        for (int j = 0; j < hashModule.size(); j++) {
            Module module = hashModule.elementAt(j);
            HashVector<IntObject,Page> hv = module.getPageTable();
            for (int i = 0; i < hv.size(); i++) {
                Page page = (Page)hv.elementAt(i);
                ((PageServlet)page).refreshTemplate();
            }
        }
    }
    /**
        このサイトのHTMLファイルとテンプレート関連ファイルを全て読み直す
    */
    public void refreshHtml() {
        for (int j = 0; j < hashModule.size(); j++) {
            Module module = (Module)hashModule.elementAt(j);
            HashVector<IntObject,Page> hv = module.getPageTable();
            for (int i = 0; i < hv.size(); i++) {
                Page page = (Page)hv.elementAt(i);
                ((PageServlet)page).refreshHtml();
            }
        }
    }

    /** site.ini セクションの取得 */
    
    public String getSection() {
        return "["+sectionBase+"]";
    }
    
    public String getSection(String str) {
        return "["+sectionBase+"."+str+"]";
    }
    public String getSection(CharArray str) {
        return "["+sectionBase+"."+str+"]";
    }

    /** site.ini からのデータ取得 */
    public CharArray get(String section, String key) {
        return ini.get(getSection(section), key);
    }
    public CharArray get(CharArray section, CharArray key) {
        return ini.get(getSection(section), key);
    }

    /** メッセージの取得 */
    public CharArray getMessage(String str) {
        return getMessage(str, false);
    }
    public CharArray getMessage(CharArray str) {
        return getMessage(str, false);
    }
    public CharArray getMessage(String str, boolean showMessage) {
        CharArray ch =  ini.get(getSection("Message"), str);
        if (ch == null && showMessage && debug) {
            System.out.println("<>ModuleManager.getMessage["+str+"] not found");
        }
        if (ch != null && (ch.indexOf("@[M:") >= 0 || ch.indexOf("@[C:") >= 0)) {
            replaceMessageTemplate(ch);
        }
        return ch;
    }
    public CharArray getMessage(CharArray str, boolean showMessage) {
        CharArray ch =  ini.get(getSection("Message"), str);
        if (ch == null && showMessage && debug) {
            System.out.println("<>ModuleManager.getMessage["+str+"] not found");
        }
        if (ch != null && (ch.indexOf("@[M:") >= 0 || ch.indexOf("@[C:") >= 0)) {
            replaceMessageTemplate(ch);
        }
        return ch;
    }

    /** 
        メッセージの取得 (@LANG対応版)
    */ 
    public CharArray getMessage(SessionObject session, String str) {
        return getMessage(session, str, false);
    }
    public CharArray getMessage(SessionObject session, CharArray str) {
        return getMessage(session, str, false);
    }
    
    public CharArray getMessage(SessionObject session, String str, boolean showMessage) {
        CharArray ch = null;
        CharArray chLang = session.getLangData();
        if (chLang != null && chLang.trim().length() > 0) {
            ch =  ini.get(getSection("Message."+chLang), str);
        }
        if (ch == null) ch = getMessage(str, showMessage);
        if (ch != null && (ch.indexOf("@[M:") >= 0 || ch.indexOf("@[C:") >= 0)) {
            session.replaceTemplate(ch);
        }
        return ch;
    }
    public CharArray getMessage(SessionObject session, CharArray str, boolean showMessage) {
        CharArray ch = null;
        CharArray chLang = session.getLangData();
        if (chLang != null && chLang.trim().length() > 0) {
            ch =  ini.get(getSection("Message."+chLang), str);
        }
        if (ch == null) ch = getMessage(str, showMessage);
        if (ch != null && (ch.indexOf("@[M:") >= 0 || ch.indexOf("@[C:") >= 0)) {
            session.replaceTemplate(ch);
        }
        return ch;
    }
    
    
    /** 色情報テキストの取得 */
    public CharArray getColor(String str) {
        return getColor(str, false);
    }
    public CharArray getColor(String str, boolean showMessage) {
        CharArray ch =  ini.get(getSection("Color"), str);
        if (ch == null && showMessage && debug) {
            System.out.println("<>ModuleManager.getColor["+str+"] not found");
        }
        return ch;
    }
    public CharArray getColor(CharArray str) {
        return getColor(str, false);
    }
    public CharArray getColor(CharArray str, boolean showMessage) {
        CharArray ch =  ini.get(getSection("Color"), str);
        if (ch == null && showMessage && debug) {
            System.out.println("<>ModuleManager.getColor["+str+"] not found");
        }
        return ch;
    }
    
    /** 
        色情報テキストの取得 (@THEME対応)
    */ 
    public CharArray getColor(SessionObject session, String str) {
        return getColor(session, str, false);
    }
    
    public CharArray getColor(SessionObject session, String str, boolean showMessage) {
        CharArray ch = null;
        CharArray chTheme = session.getTheme();
        if (chTheme != null && chTheme.trim().length() > 0) {
            ch =  ini.get(getSection("Color."+chTheme), str);
        }
        if (ch == null) ch = getColor(str, showMessage);
        return ch;
    }
    public CharArray getColor(SessionObject session, CharArray str) {
        return getColor(session, str, false);
    }
    
    public CharArray getColor(SessionObject session, CharArray str, boolean showMessage) {
        CharArray ch = null;
        CharArray chTheme = session.getTheme();
        if (chTheme != null && chTheme.trim().length() > 0) {
            ch =  ini.get(getSection("Color."+chTheme), str);
        }
        if (ch == null) ch = getColor(str, showMessage);
        return ch;
    }

    // 追加  MenuTreeを管理する
    private Hashtable<String,MenuTree> menuHash = new Hashtable<String,MenuTree>();

    protected void add(IniFile ini, String section,String key) {
        if (debug) System.out.println("ModuleManager.add("+section+")");
        MenuTree menuTree = new MenuTree();
        menuTree.init(ini,section);
        menuHash.put(key,menuTree);
    }
    
    public MenuTree getMenuTree(String key) { return (MenuTree)menuHash.get(key); }
    public Enumeration getMenuTreeKeys() { return menuHash.keys(); }
    public int getMenuTreeCount() { return menuHash.size(); }


    // webservice 関連
/**  コメントアウト
    public AxisService getAxisService() {
        AxisService service = null;
        AxisServiceModule module = (AxisServiceModule)getModule("AxisService");
        if (module != null) service = module.getAxisService();
        return service;
    }
**/    
    
    // テンプレート置換
    public int replaceTemplate(CharArray ch) {
        int count = 0;
        if (ch != null) {
            count += ch.replaceCount("@SITE_KEY", getSiteKey());
            count += ch.replaceCount("@SITE_NAME", getSiteName());
            count += ch.replaceCount("@SITE_CODE", getSiteCodeString());
            count += ch.replaceCount("@CHANNEL_NAME", getChannelName());
            count += ch.replaceCount("@CHANNEL_CODE", getChannelCodeString());
            count += ch.replaceCount("@SITE_CHANNEL_CODE", getSiteChannelCodeString());
            if (getTheme().length() > 0) {
                count += ch.replaceCount("@SITE_THEME", getTheme());
            }
        }
//System.out.println("ModuleManager#replaceTemplate() "+count+" 個置換しました");
        return count;
    }
    
    static final String startStr = "@[";
    static final String endStr = "]";
    
    // 動的テンプレート置換を行う(Message, Color)
    // session.replaceTemplate と等価
    public void replaceMessageTemplate(CharArray ch) {
        if (ch == null) return;
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
            if (key.startsWith("Message."))    msg = getMessage(key.substring(8));
            else if (key.startsWith("M."))     msg = getMessage(key.substring(2));
            else if (key.startsWith("M:"))     msg = getMessage(key.substring(2));
            else if (key.startsWith("Color.")) msg = getColor(key.substring(6));
            else if (key.startsWith("C."))     msg = getColor(key.substring(2));
            else if (key.startsWith("C:"))     msg = getColor(key.substring(2));
            if (msg != null) {
                changed = true;
                tmp.add(msg);
                start = endPos + endStr.length();
            } else {
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
    }
    
    // 
    private Hashtable<String,ItemInfo> hashItemInfo = new Hashtable<String,ItemInfo>();   // String:ItemInfo
    
    public void setHashItemInfo(HashVector hv) {
        if (hv == null) return;
if (debugInfo) System.out.println("▼setHashItemInfo:start----------------------");

        for (int i = 0; i < hv.size(); i++) {
            CharArray key = (CharArray)hv.keyElementAt(i);
            CharArrayQueue queue = (CharArrayQueue)hv.elementAt(i);
            for (int j = 0; j < queue.size(); j++) {
                if (queue.peek(j).trim().length() > 0) {
                    String filename = SystemManager.resourceDirectory + queue.peek(j);
                    QueueFile qfile = new QueueFile(filename,  "UTF-8");
                    qfile.setInclude("#include");
                    if (qfile.read()) {
                        if (debugInfo) System.out.println(filename+" を読み込みました");
                        for (int pos = 0; pos < qfile.size();) {
                            ItemInfo info = new ItemInfo();
                            int newpos = info.init(qfile, pos);
                            if (newpos <= pos) break;
                            CharArray ckey = info.getKey();
                            if (ckey != null && ckey.trim().length() > 0) {
                                hashItemInfo.put(ckey.toString(), info);
                            } else {
                                //if (debugInfo) System.out.println("setItemInfo keyが取得できない :"+ckey);
                            }
                            pos = newpos+1;
                        }
                    } else {
                        System.out.println(filename+" が読めません");
                    }
                }
            } // next
        }
if (debugInfo) System.out.println("▲setHashItemInfo:end----------------------");
    }
    
    /** ItemInfoを取得する */
    public ItemInfo getItemInfo(String key) {
        return (ItemInfo)hashItemInfo.get(key);
    }
    
    public void setStyleSheetURL(HashVector hv) {
        setStyleSheetURL(hv, UserAgent.PC);
    }
    public void setStyleSheetURL(HashVector hv, int carrier) {
        if (hv == null) return;
        for (int i = 0; i < hv.size(); i++) {
            CharArray key = (CharArray)hv.keyElementAt(i);
            CharArrayQueue queue = (CharArrayQueue)hv.elementAt(i);
            System.out.println("["+i+"]KEY["+key+"]"+queue.size());
            for (int j = 0; j < queue.size(); j++) {
                CharArray ch = queue.peek(j).trim();
                if (ch.length() > 0) {
                    addStyleSheetURL(ch, carrier);
                }
            }
        }
    }
    //-----------------------------------------------------------
    // 
    //-----------------------------------------------------------
    public CharArrayQueue allowIP = new CharArrayQueue();
    public CharArrayQueue denyIP = new CharArrayQueue();
    
    protected void setIP() {
if (debugIP) System.out.println("★★setIP :"+getSection("AllowIP"));
        HashVector<CharArray,CharArrayQueue> v = ini.getKeyTable(getSection("AllowIP"));
        if (v != null) {
            for (int i = 0; i < v.size(); i++) {
                CharArrayQueue queue = (CharArrayQueue)v.elementAt(i);
                for (int j = 0; j <queue.size(); j++) {
                    CharArray ch = queue.peek(j).trim();
                    System.out.println("AllowIP["+ch+"]");
                    if (ch.length() > 0) {
                        int index = ch.indexOf('*');
                        if (index < 0) {
                            allowIP.enqueue(new CharArray(ch));
                        } else if (index > 0) {
                            allowIP.enqueue(new CharArray().set(ch,0,index));
                        }
                    }
                }
            }
        }
        allowIP.dumpQueue();
        
if (debugIP) System.out.println("★★setIP :"+getSection("DenyIP"));
        v = ini.getKeyTable(getSection("DenyIP"));
        if (v != null) {
            for (int i = 0; i < v.size(); i++) {
                CharArrayQueue queue = (CharArrayQueue)v.elementAt(i);
                for (int j = 0; j < queue.size(); j++) {
                    CharArray ch = queue.peek(j).trim();
                    System.out.println("DenyIP["+ch+"]");
                    if (ch.length() > 0) {
                        if (!ch.isDigit(".*")) { // IPじゃないので変換が必要
                            
                            
                            
                        }
                        int index = ch.indexOf('*');
                        if (index < 0) {
                            denyIP.enqueue(new CharArray(ch));
                        } else if (index > 0) {
                            denyIP.enqueue(new CharArray().set(ch,0,index));
                        }
                    }
                }
            }
        }
        denyIP.dumpQueue();
    }
    
    /** allow IP チェック 
        @return false でアクセス不可
    */
    public boolean checkAllowIP(String szIP) {
if (debugIP) System.out.println("checkAllowIP:size:"+allowIP.size());
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
    public boolean checkDenyIP(String szIP) {
if (debugIP) System.out.println("checkDenyIP:size:"+denyIP.size());
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
}

//
// [end of ModuleManager.java]
//


