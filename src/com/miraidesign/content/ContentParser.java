//------------------------------------------------------------------------
// @(#)ContentParser.java
//              スクリプト＆テンプレート＆ブロックパーサー
//              Copyright (c) MiraiDesign  2010 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.content;

import java.util.Hashtable;

import com.miraidesign.common.SystemConst;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.system.SiteMapping;
import com.miraidesign.renderer.Page;
import com.miraidesign.renderer.Module;
import com.miraidesign.servlet.PageServlet;
import com.miraidesign.servlet.ModuleServlet;
import com.miraidesign.servlet.ServletLog;
import com.miraidesign.session.SessionObject;
//import com.miraidesign.image.EmojiConverter;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayFile;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.CounterQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.IniFile;
import com.miraidesign.util.IntQueue;
import com.miraidesign.util.ObjectQueue;
import com.miraidesign.util.Util;

import com.miraidesign.content.input.InputItem;
import com.miraidesign.content.input.InputContent;
import com.miraidesign.content.input.InputMobile;
import com.miraidesign.content.input.InputModule;
import com.miraidesign.content.input.InputSession;
import com.miraidesign.content.input.InputSystem;
import com.miraidesign.content.input.InputSite;
import com.miraidesign.content.input.InputUser;

/**
 *  ContentParser
 *  
 *  @version 0.5
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ContentParser extends CharArrayQueue {
    private boolean debug = (SystemConst.debug && false);  // デバッグ表示 false
    static private boolean debugParse = (SystemConst.debug && false);  // デバッグ表示 false
    private boolean debugTemplate = (SystemConst.debug && false);  // デバッグ表示 false
    private boolean debugDraw = (SystemConst.debug && false);  // デバッグ表示 false
    
    private boolean debugInfo = (SystemConst.debug && false);  // デバッグ用コード
    static private boolean debugMacro = (SystemConst.debug && false);  // デバッグ用コード
    
    private boolean debugTimer = (SystemConst.debug && false);  // デバッグ用コード
    private boolean debugDependency = (SystemConst.debug && false);  // デバッグ用コード
    private boolean debugTMCount = (SystemConst.debug && false);  // デバッグ用コード
    private boolean debugNamespace = (SystemConst.debug && false);  // デバッグ表示 true
    
    private boolean convertTemplate = true; // 共通テンプレートをコンバートするか
    
    private boolean convertMacro = true;    // マクロをコンバートするか
    
    static protected String[] systemTemplate = {   // 外出しにすること
        //"@BASE",
        //"@BODY", 
        "@CHANNEL_CODE",
        "@CHANNEL_NAME",
        "@CHARA_SET",
        "@CONTENT_TYPE",
        //"@COUNT",
        //"@GUIDE",
        //"@HN_FOOTER",
        //"@HTTPS_BASE",
        "@LANG",
        //"@LIST",
        //"@LOGOUT",
        //"@MENU",
        //"@NAVI",
        //"@NO_LOADLIST", 
        //"@PAGE", 
        "@ROLE",
        "@SESSION_THEME",
        "@SITE_CHANNEL_CODE",
        "@SITE_CODE",
        "@SITE_KEY",
        "@SITE_NAME",
        "@SITE_THEME",
        "@THEME"
        //"@TREE"
    };
    protected String[] systemMacro = {
        
    };
    private String nameSpace = "";   // ネームスペース
    private ContentItemInfo info = null;
    private boolean frameMode = false;
    public void setFrameMode(boolean mode) { frameMode = true; }
    public boolean getFrameMode() { return frameMode;}

    private boolean enabled = true; 
    // システムテンプレートによる描画を有効にする(default:true)
    public void setEnabled(boolean mode) { this.enabled = mode; }
    public boolean isEnabled() { return enabled;}
    /* ネームスペースを設定する（デフォルト""） */
    public void setNameSpace(String str) { this.nameSpace = str;}
    /* ネームスペースを取得する */
    public String getNameSpace() { return nameSpace;}
    
    private CharArrayFile caFile;
    private IniFile templateIni;
    
    protected ContentScript   cbScript;
    protected ContentTemplate cbTemplate;
    protected ContentBlock    cbBlock;
    protected ContentInput    cbInput;
    protected ContentMacro    cbMacro;
    
    public ContentScript   getContentScript() { return cbScript;}
    public ContentTemplate getContentTemplate() { return cbTemplate;}
    public ContentBlock    getContentBlock() { return cbBlock;}
    public ContentInput    getContentInput() { return cbInput;}
    public ContentMacro    getContentMacro() { return cbMacro;}
    
    
    static private final String frameStart = "@(";
    static private final String frameEnd   = ")";
    
    static public final int MAX_FRAME_STR = 32;
    static public final int MAX_BLOCK_LENGTH = 64;
    static public final int MAX_TEMPLATE_LENGTH = 256;
    static public final int MAX_INPUT_LENGTH = 256;
    
    static public final int MAX_MACRO_KEY = 32;
    static public final int MAX_MACRO_PARAM = 512;
    
    static private final String macroStart = "$";
    static private final String shortTemplateStart = "@";
    //static private final String macroMiddle = "(";
    //static private final String macroEnd   = ")";
    
    static private final String scriptStart = "<!--@";
    static private final String scriptEnd   = "-->";
    
    static private final String templateStart = "<template";
    static private final String templateEnd = "</template>";
    
    static private final String blockStart = "<block";
    static private final String blockEnd   = "</block>";
    
    static private final String inputStart = "<input:";
    static private final String inputEnd        = ">";
    static private final String inputShortEnd   = "/>";
    static private final String inputScriptStart  = "<input:SCRIPT";
    static private final String inputStyleStart  = "<input:STYLE";
    static private final String inputScriptEnd  = "</input:SCRIPT>";
    static private final String inputStyleEnd  = "</input:STYLE>";
    
    static protected final char f_magic = 0x01; // フレーム識別コード
    static protected final char m_magic = 0x02; // マクロ識別コード  //@@// あとで実装する
    
    static protected final char s_magic = 0x03; // スクリプト識別コード
    static protected final char t_magic = 0x04; // テンプレート識別コード
    static protected final char b_magic = 0x05; // ブロック識別コード
    static protected final char e_magic = 0x06; // 終了識別コード
    static protected final char i_magic = 0x07; // 入力パラメータ識別コード
    
    private Page page;                  // 所属ページ
    protected SessionObject session;      // 使用セッション
    public SessionObject getSession() { return session;}
    public void setSession(SessionObject session) {
        this.session = session;
    }
    protected ModuleManager moduleManager; // 使用モジュールマネージャー（特定時のみ)
    public ModuleManager getModuleManager() { return moduleManager;}
    public void setModuleManager(ModuleManager mm) {
        this.moduleManager = mm;
    }
    
    private int[] ia = new int[7];      // 内部バッファ [4]->[7]
    
    private CharArray parseErrMsg = new CharArray();
    private CharArray drawErrMsg = new CharArray();

    /* parseエラーメッセージ を取得する since ver.0.871 */
    public CharArray getParseErrorMessage() {
        return parseErrMsg;
    }
    /* drawエラーメッセージ を取得する since ver.0.871 */
    public CharArray getDrawErrorMessage() {
        return drawErrMsg;
    }
    
    /////////////////////////////////////////////////////////////////
    //
    protected long publishTime = 0;
    
    /* publish timeslice を実行する時間を指定する。<br>
        ページタイトル等の取得等に使用される（０で現在時刻を使用）*/
    public void setPublishTime(long time) { this.publishTime = time; }
    
    /* 設定されたpublish time を取得する */
    public long getPublishTime() { return publishTime;}
    
    /** 編集モード */
    static public final int EDIT      = 0;
    /** プレビューモード */
    static public final int PREVIEW   = 1;
    /** タイムスライスモード */
    static public final int TIMESLICE = 2;
    /** ダイナミックビューモード */
    static public final int DYNAMIC_VIEW = 3;
    /** 配信モード */
    static public final int PUBLISH   = 4;
    /** 配信プレビューモード */
    static public final int PUBLISH_PREVIEW = 5;
    /** リモートプレビューモード */
    static public final int REMOTE_PREVIEW = 6;

    
    protected int parser_mode = 0;
    /* パーサーモードを設定する */
    public void setParserMode(int mode) {
        this.parser_mode = mode;
    }

    static public final int STATIC  = 0;
    static public final int DYNAMIC = 1;

    /* パーサーモードを取得する */
    public int getParserMode() { return parser_mode;}

    /* パーサーモード文字列を取得する */
    public String getParserModeString() {
        String str = "";
        switch (parser_mode) {
            case EDIT:          str= "EDIT"; break;
            case PREVIEW:       str= "PREVIEW"; break;
            case TIMESLICE:     str= "TIMESLICE"; break;
            case DYNAMIC_VIEW:  str= "DYNAMIC_VIEW"; break;
            case PUBLISH:       str= "PUBLISH"; break;
            case PUBLISH_PREVIEW: str= "PUBLISH_PREVIEW"; break;
            case REMOTE_PREVIEW:  str= "REMOTE_PREVIEW"; break;
        }
        return str;
    }
    /* パーサーモード文字列を取得する */
    public static String getParserModeString(int _parser_mode) {
        String str = "";
        switch (_parser_mode) {
            case EDIT:          str= "EDIT"; break;
            case PREVIEW:       str= "PREVIEW"; break;
            case TIMESLICE:     str= "TIMESLICE"; break;
            case DYNAMIC_VIEW:  str= "DYNAMIC_VIEW"; break;
            case PUBLISH:       str= "PUBLISH"; break;
            case PUBLISH_PREVIEW: str= "PUBLISH_PREVIEW"; break;
            case REMOTE_PREVIEW:  str= "REMOTE_PREVIEW"; break;
        }
        return str;
    }
    
    public PageServlet dynamicPage;
    public PageServlet rssPage;
    
    /* タイムスライスのエントリーページを取得する */
    public PageServlet getDynamicPage() {
        return getDynamicPage(session);
    }
    public PageServlet getDynamicPage(SessionObject session) {
        if (dynamicPage == null) {
            ModuleManager mm = session.getModuleManager();
            if (mm != null) {
                Module module = mm.getModule("DynamicView");
                if (module != null) {
                    int page = 1;
                    if (parser_mode == TIMESLICE) page = 2;
                    if (parser_mode == REMOTE_PREVIEW) page = 3;
                    dynamicPage = (PageServlet)module.getPage(page);
                }
            }
        }
        return dynamicPage;
    }
    
    public PageServlet getRssPage(SessionObject session) {
        if (rssPage == null) {
            ModuleManager mm = session.getModuleManager();
            if (mm != null) {
                Module module = mm.getModule("DynamicView");
                if (module != null) {
                    rssPage = (PageServlet)module.getPage(50);
                }
            }
        }
        return rssPage;
    }
    
    // タイムスライス用ページURLを返す
    public CharArray getPageURL(int content_id, int language_id, CharArray url) {
        return getPageURLSub(content_id, language_id, new CharArray(url), 0);
    }
    public CharArray getPageURL(int content_id, int language_id, String url) {
        return getPageURLSub(content_id, language_id, new CharArray(url), 0);
    }
    public CharArray getPageURL(int content_id, int language_id, CharArray url, int publish_mode) {
        return getPageURLSub(content_id, language_id, new CharArray(url), publish_mode);
    }
    public CharArray getPageURL(int content_id, int language_id, String url, int publish_mode) {
        return getPageURLSub(content_id, language_id, new CharArray(url), publish_mode);
    }
    private CharArray getPageURLSub(int content_id, int language_id, CharArray ch, int publish_mode) {
if (debug) System.out.println("★★★ getPageURL("+content_id+","+ch+") ★★★"+publish_mode);

//System.out.println("■ "+content_id+" parser:"+parser_mode+" publish:"+publish_mode);

        if (parser_mode == PREVIEW || parser_mode == REMOTE_PREVIEW || parser_mode == PUBLISH_PREVIEW) {
            if (content_id > 0) {   // 内部リンクは全て #にする
                return new CharArray("#");
            }
        } else if (((  parser_mode == TIMESLICE 
                    || (parser_mode == DYNAMIC_VIEW && publish_mode == DYNAMIC)
                    || (parser_mode == PUBLISH && publish_mode == DYNAMIC)
                    )) && session != null && content_id > 0) {
            PageServlet page = getDynamicPage(session);
            if (page != null) {
                CharArray ca = page.getURL(session);
                if (ca != null && ca.length() > 0) {
                    ch.setAppendMode(true);
                    ch.set(ca);
                    
                    int index = ch.indexOf("/at=");
                    if (index >= 0) ch.length= index;     // 認証キーをカットする
                    ch.add("/c=");
                    ch.format(content_id);
                    if (language_id > 1) {
                        ch.add("/l=");
                        ch.format(language_id);
                    }
                    if (publishTime > 0 && parser_mode == TIMESLICE) {
                        ch.add("/t=");
                        ch.format(publishTime);
                    }
                    // dynamic URL コンバート
                    if (ch.indexOf("/view/") >= 0) {
                        ModuleManager mm = session.getModuleManager();
                        CharArray url = mm.ini.get(mm.getSection("Mode"), "DynamicViewURL");
                        if (url != null && url.length() > 0) {
                            CharArray ch2 = CharArray.pop();
                            SiteMapping map = mm.getSiteMapping();
                            map.getSiteParameter(ch2);
                            ch2.add("view/");
                            ch.replace(ch2, url+"/");
                        }
                    }
                    if (ch.indexOf("/timeslice/") >= 0) {
                        ModuleManager mm = session.getModuleManager();
                        CharArray url = mm.ini.get(mm.getSection("Mode"), "TimeSliceURL");
                        if (url != null && url.length() > 0) {
                            CharArray ch2 = CharArray.pop();
                            SiteMapping map = mm.getSiteMapping();
                            map.getSiteParameter(ch2);
                            ch2.add("timeslice/");
                            ch.replace(ch2, url+"/");
                        }
                    }
                }
            }
        }
if (debug) System.out.println("★★★ url:"+ch+" ★★★★★");
        return ch;
    }

//    /** FEED 用クラスを取得 <br>
//        getFilename()
//    
//    */
//    public ContentFeedInformation getContentFeed() {
//        ContentFeedInformation info = null;
//        if (inputCategory != null) {
//            info = inputCategory.getFeedInfo();
//        }
//System.out.println("◆ContentParser#getContentFeed:"+info+" category:"+inputCategory);
//        return info;
//    }
    
    /////////////////////////////////////////////////////////////////
    // システム関数関連  
    private static InputSystem inputSystem = null;
    private static  InputSystem getInputSystem() {
        if (inputSystem == null) inputSystem = new InputSystem();
        return inputSystem;
    }
    // 
    private static InputContent inputContent = null;
    private static  InputContent getInputContent() {
        if (inputContent == null) inputContent = new InputContent();
        return inputContent;
    }
    // 
    private static InputModule inputModule = null;
    private static  InputModule getInputModule() {
        if (inputModule == null) inputModule = new InputModule();
        return inputModule;
    }
    // 
    private static InputMobile inputMobile = null;
    private static  InputMobile getInputMobile() {
        if (inputMobile == null) inputMobile = new InputMobile();
        return inputMobile;
    }
    
    // 本来はセッションに持たせたいが
    private static InputUser inputUser = null;
    private static  InputUser getInputUser() {
        if (inputUser == null) inputUser = new InputUser();
        return inputUser;
    }
    
    // 
    private static InputSite inputSite = null;
    public static  InputSite getInputSite() {
        if (inputSite == null) inputSite = new InputSite();
        return inputSite;
    }
    
    // 
    private static InputSession inputSession = null;
    public static  InputSession getInputSession() {
        if (inputSession == null) inputSession = new InputSession();
        return inputSession;
    }
    
    // 
    //private InputPage inputPage = null;
    //public InputPage getInputPage() { return getInputPage(false);}
    //public InputPage getInputPage(boolean mode) {
    //    if (inputPage == null || mode) inputPage = new InputPage(this);
    //    return inputPage;
    //}
    //public void setInputPage(InputPage ip) { this.inputPage = ip;}
    
    // 
    //private InputCategory inputCategory = null;
    //public InputCategory getInputCategory() { return getInputCategory(false);}
    //public InputCategory getInputCategory(boolean mode) {
    //    if (inputCategory == null || mode) inputCategory = new InputCategory(this);
    //    return inputCategory;
    //}
    //public void setInputCategory(InputCategory ic) { this.inputCategory = ic;}
    
    //protected InputInformation inputInfo;
    //public void setInputInformation(InputInformation inputInfo) {
    //    this.inputInfo = inputInfo;
    //}
    //public InputInformation getInputInformation() {
    //    return inputInfo;
    //}
    
    //--------------------------------------------------------------
    //--------------------------------------------------------------
    
//  /**
//      システム関数が使用する。<br>
//      ContentParserを新規生成したあとに必ずセットすること<br>
//      @info  inputInformation
//      @param page_id
//      @param ver_seq
//  **/
/*
    public void setInformation(InputInformation info, int page_id, int ver_seq) {
        setInformation(info, page_id, ver_seq, publishTime);
    }
    public void setInformation(InputInformation info, int page_id, int ver_seq, long publishTime) {
if (debugInfo) System.out.println("◇◆◇◆◇◆setInformation start ");
        this.inputInfo = info;
        this.page_id = page_id;
        this.ver_seq = ver_seq;
        do {
            if (inputInfo == null) break;
            if (page_id <= 0) break;
            HashParameter param = inputInfo.getPageInfo(session, page_id, ver_seq, parser_mode, publishTime);
            if (param == null) break;
            
            category_id = param.getInt("CATEGORY_ID");
            content_id = param.getInt("CONTENT_ID");
            if (debugInfo) System.out.println("category_id:"+category_id+ " content_id:"+content_id);
            if (category_id > 0) {
                getInputCategory(true);
                inputCategory.setCategoryInfo(category_id,
                                              param.getInt("CATEGORY_PARENT_ID"), 
                                              param.get("CATEGORY_NAME"), 
                                              param.get("CATEGORY_PATH"),
                                              param.get("CATEGORY_DESCRIPTION"),
                                              param.get("CATEGORY_KEYWORD"));
                                              //param.getInt("MASTER_PAGE_ID"));
                inputCategory.setSiteUrl(param.get("SITE_URL"));
                if (content_id > 0) {
                    getInputPage(true);
                    inputPage.setPageId(page_id);
                    inputPage.setCategoryId(category_id);
                    inputPage.setContentId(content_id);
                    inputPage.setContentVersion(ver_seq);
                    inputPage.setTitle(param.get("PAGE_TITLE"));
                    inputPage.setContentTitle(param.get("CONTENT_TITLE"));
                    inputPage.setDescription(param.get("PAGE_DESCRIPTION"));
                    inputPage.setContentDescription(param.get("CONTENT_DESCRIPTION"));
                    inputPage.setDefaultContentId(param.getInt("DEFAULT_CONTENT_ID"));
                    
                    CharArray ch = param.get("CATEGORY_PATH");
                    if (ch == null) {
                        inputPage.setUrl(param.get("PAGE_FILENAME"));
                    } else {
                        inputPage.setUrl(ch+(ch.endsWith("/")? "" : "/")+param.get("PAGE_FILENAME"));
                    }
                    inputPage.setGroupName(param.get("GROUP_NAME"));
                    inputPage.setGroupId(param.get("GROUP_ID"));
                    inputPage.setUserName(param.get("USER_NAME"));
                    inputPage.setUserId(param.get("USER_ID"));
                    inputPage.setUpdateUserName(param.get("UPDATE_USER_NAME"));
                    
                    inputPage.setCreateDateTime(param.getLong("CREATE_DATETIME"));
                    inputPage.setUpdateDateTime(param.getLong("UPDATE_DATETIME"));
                    inputPage.setLimitDateTime(param.getLong("CONTENT_DUE_DATETIME"));
                }
            }
        } while (false);
if (debugInfo) System.out.println("◇◆◇◆◇◆setInformation end");
    }
*/
    
//  //--------------------------------------------------------------
    /* モジュールマッピング情報を取得する */

//    public QueueTable getModuleMapping(int site_id, int language_id) {
//        return inputInfo.getModuleMapping(session, site_id, language_id, parser_mode, publishTime);
//    }
//  //--------------------------------------------------------------
    public InputItem getSystemInputItem(CharArray key, int count) {
//System.out.println("☆★ContentParser#getSystemInputItem:"+key);
        CharArray _key = CharArray.pop(key);
        _key.toLowerCase();
        InputItem item = null;
        if      (_key.equals("@system"))      item = getInputSystem();
        else if (_key.equals("@content"))     item = getInputContent();
        else if (_key.equals("@module"))      item = getInputModule();
        else if (_key.equals("@mobile"))      item = getInputMobile();
        else if (_key.equals("@user"))        item = getInputUser();
        else if (_key.equals("@site"))        item = getInputSite();
        else if (_key.equals("@session"))     item = getInputSession();
        
        //else if (_key.equals("@page"))        item = getInputPage();
        //else if (_key.equals("@category"))    item = getInputCategory();
        //else if (_key.equals("@common_text")) item = getInputCommonText();
        CharArray.push(_key);
        return item;
    }
    
    /////////////////////////////////////////////////////////////////
    
    /** Constructor 
        @param file  読み込むテンプレートファイル
    */
    public ContentParser(CharArrayFile file) {
        this.caFile = file;
        if (caFile.size() <= 0) caFile.read();
        cbScript   = new ContentScript();
        cbTemplate = new ContentTemplate();
        cbBlock    = new ContentBlock();
        cbInput    = new ContentInput();
    }

    /** Constructor 
        @param file  読み込むテンプレートファイル
        @param page  設定されているページ
    */
    public ContentParser(CharArrayFile file, Page page) {
        this.caFile = file;
        this.page = page;
        if (caFile.size() <= 0) caFile.read();
        cbScript = new ContentScript(page);
        cbTemplate = new ContentTemplate(page.getModule().getModuleManager());
        cbBlock    = new ContentBlock();
        cbInput    = new ContentInput();
    }

    /** Constructor 
        @param file  読み込むテンプレートファイル
        @param page  設定されているページ
        @param nameSpace ネームスペース
    */
    public ContentParser(CharArrayFile file, Page page, String nameSpace) {
        this.caFile = file;
        this.page = page;
        this.nameSpace = nameSpace;
        if (caFile.size() <= 0) caFile.read();
        cbScript = new ContentScript(page);
        cbTemplate = new ContentTemplate(page.getModule().getModuleManager());
        cbBlock    = new ContentBlock();
        cbInput    = new ContentInput();
    }

    /** Constructor 
        @param file  読み込むテンプレートファイル
        @param nameSpace ネームスペース
    */
    public ContentParser(CharArrayFile file, String nameSpace) {
        this.caFile = file;
        this.nameSpace = nameSpace;
        if (caFile.size() <= 0) caFile.read();
        cbScript   = new ContentScript();
        cbTemplate = new ContentTemplate();
        cbBlock    = new ContentBlock();
        cbInput    = new ContentInput();
    }
    /** Constructor 
        @param file  読み込むテンプレートファイル
        @param caIni iniファイル情報
        @param nameSpace ネームスペース
    */
    public ContentParser(CharArrayFile file, CharArrayFile caIni, String nameSpace) {
        this.caFile = file;
        this.nameSpace = nameSpace;
        if (caFile.size() <= 0) caFile.read();
        HashVector<CharArray,CharArrayQueue> inputHV = null; 
        HashVector<CharArray,CharArrayQueue> templateHV = null;
        HashVector<CharArray,CharArrayQueue> macroHV = null;
        if (caIni != null) {
            if (caIni.size() <= 0) caIni.read();
            if (caIni.size() > 0) {
if (debug) System.out.println("★☆★ ContentParser template.ini を読み込みます。★☆★ size:"+caIni.size());
                templateIni = new IniFile("", "=", "#", "\\", "\\n");
                //templateIni.copy(caIni);
                
                //
                CharToken token = CharToken.pop();
                caIni.replace("\r\n","\n");
                token.set(caIni,"\n");
                CharArray ch = CharArray.pop();
                for (int i = 0; i < token.size(); i++) { // IniFile#copyにしたほうがいい
                    CharArray ca = token.peek(i);
                    if (ca.endsWith("\\n")) {
                        ca.length -= 2;
                        ch.add(ca);
                        ch.add("\n");
                    } else if (ca.endsWith("\\")) {
                        ca.length -= 1;
                        ch.add(ca);
                    } else {
                        ch.add(ca);
                        templateIni.enqueue(new CharArray(ch));
                        ch.reset();
                    }
                }
                if (ch.length() > 0) {
                    templateIni.enqueue(new CharArray(ch));
                }
                CharArray.push(ch);
                CharToken.push(token);
                
                templateIni.parse();
if (debug) System.out.println("★☆★ ContentParser template.ini を読み込みました。★☆★");
                inputHV  = templateIni.getKeyTable("[InputRef]");
                templateHV = templateIni.getKeyTable("[Template]");
                macroHV    = templateIni.getKeyTable("[Macro]");
            }
        }
        cbScript   = new ContentScript();
        cbTemplate = new ContentTemplate(templateHV);
        cbBlock    = new ContentBlock();
        cbInput    = new ContentInput(inputHV);
        if (macroHV != null) cbMacro = new ContentMacro(macroHV);
    }
    
    /** Constructor 
        @param file  読み込むテンプレートファイル
        @param nameSpace ネームスペース
        @param session セッションオブジェクト
    */
    public ContentParser(CharArrayFile file, String nameSpace, SessionObject session) {
        this.caFile = file;
        this.nameSpace = nameSpace;
        this.session = session;
        if (caFile.size() <= 0) caFile.read();
        cbScript   = new ContentScript();
        cbTemplate = new ContentTemplate(session.getModuleManager());
        cbBlock    = new ContentBlock();
        cbInput    = new ContentInput();
    }
    /** Constructor 
        @param file  読み込むテンプレートファイル
        @param caIni   読み込むinifile
        @param nameSpace ネームスペース
        @param session SessionObject
    */
    public ContentParser(CharArrayFile file, CharArrayFile caIni,
                       String nameSpace, SessionObject session) {
        this.caFile = file;
        this.nameSpace = nameSpace;
        this.session = session;
        if (caFile.size() <= 0) caFile.read();
        HashVector<CharArray,CharArrayQueue> inputHV = null;
        HashVector<CharArray,CharArrayQueue> templateHV = null;
        HashVector<CharArray,CharArrayQueue> macroHV = null;
        if (caIni != null) {
            if (caIni.size() <= 0) caIni.read();
            if (caIni.size() > 0) {
                templateIni = new IniFile("", "=", "#", "\\", "\\n");
                //templateIni.copy(caIni);
                CharToken token = CharToken.pop();
                caIni.replace("\r\n","\n");
                token.set(caIni,"\n");
                CharArray ch = CharArray.pop();
                for (int i = 0; i < token.size(); i++) { // IniFile#copyにしたほうがいい
                    CharArray ca = token.peek(i);
                    if (ca.endsWith("\\n")) {
                        ca.length -= 2;
                        ch.add(ca);
                        ch.add("\n");
                    } else if (ca.endsWith("\\")) {
                        ca.length -= 1;
                        ch.add(ca);
                    } else {
                        ch.add(ca);
                        templateIni.enqueue(new CharArray(ch));
                        ch.reset();
                    }
                }
                if (ch.length() > 0) templateIni.enqueue(new CharArray(ch));
                CharArray.push(ch);
                CharToken.push(token);
                templateIni.parse();
if (debug || debugParse) {
    System.out.println("★★★ ContentParser template.ini を読み込みました。★★★");
    templateIni.dumpQueue();
}
                inputHV    = templateIni.getKeyTable("[InputRef]");
                templateHV = templateIni.getKeyTable("[Template]");
                macroHV    = templateIni.getKeyTable("[Macro]");
            }
        }
        cbScript   = new ContentScript();
        cbTemplate = new ContentTemplate(session.getModuleManager(),templateHV);
        cbBlock    = new ContentBlock();
        cbInput    = new ContentInput(inputHV);
        
        if (macroHV != null) cbMacro = new ContentMacro(macroHV);
        
    }
    //////////////////////////////////////////////////////
    // BGR用
    //////////////////////////////////////////////////////
    
    public ContentParser(CharArrayFile file, CharArrayFile caIni,
                       String nameSpace, ModuleManager mm) {
        this.caFile = file;
        this.nameSpace = nameSpace;
        //this.session = session;
        
        this.moduleManager = mm;
        if (caFile.size() <= 0) caFile.read();
        HashVector<CharArray,CharArrayQueue> inputHV = null;
        HashVector<CharArray,CharArrayQueue> templateHV = null;
        HashVector<CharArray,CharArrayQueue> macroHV = null;

        if (caIni != null) {
            if (caIni.size() <= 0) caIni.read();
            if (caIni.size() > 0) {
                templateIni = new IniFile("", "=", "#", "\\", "\\n");
                //templateIni.copy(caIni);
                CharToken token = CharToken.pop();
                caIni.replace("\r\n","\n");
                token.set(caIni,"\n");
                CharArray ch = CharArray.pop();
                for (int i = 0; i < token.size(); i++) { // IniFile#copyにしたほうがいい
                    CharArray ca = token.peek(i);
                    if (ca.endsWith("\\n")) {
                        ca.length -= 2;
                        ch.add(ca);
                        ch.add("\n");
                    } else if (ca.endsWith("\\")) {
                        ca.length -= 1;
                        ch.add(ca);
                    } else {
                        ch.add(ca);
                        templateIni.enqueue(new CharArray(ch));
                        ch.reset();
                    }
                }
                if (ch.length() > 0) templateIni.enqueue(new CharArray(ch));
                CharArray.push(ch);
                CharToken.push(token);
                templateIni.parse();
if (debug || debugParse) {
    System.out.println("☆★★ ContentParser template.ini を読み込みました。★★☆");
    //templateIni.dumpQueue();
}
                inputHV    = templateIni.getKeyTable("[InputRef]");
                templateHV = templateIni.getKeyTable("[Template]");
                macroHV    = templateIni.getKeyTable("[Macro]");
            }
        }
        cbScript   = new ContentScript();
        cbTemplate = new ContentTemplate(mm, templateHV);
        cbBlock    = new ContentBlock();
        cbInput    = new ContentInput(inputHV);
        
        if (macroHV != null) cbMacro = new ContentMacro(macroHV);
        
    }
    
    /*
        テンプレートを変更する
    */
    public void changeTemplate(CharArrayFile file, CharArrayFile caIni) {
        this.caFile = file;
        HashVector<CharArray,CharArrayQueue> inputHV = null;
        HashVector<CharArray,CharArrayQueue> templateHV = null;
        HashVector<CharArray,CharArrayQueue> macroHV = null;
        if (caIni != null) {
            if (caIni.size() <= 0) caIni.read();
            if (caIni.size() > 0) {
                templateIni = new IniFile("", "=", "#", "\\", "\\n");
                CharToken token = CharToken.pop();
                caIni.replace("\r\n","\n");
                token.set(caIni,"\n");
                CharArray ch = CharArray.pop();
                for (int i = 0; i < token.size(); i++) { // IniFile#copyにしたほうがいい
                    CharArray ca = token.peek(i);
                    if (ca.endsWith("\\n")) {
                        ca.length -= 2;
                        ch.add(ca);
                        ch.add("\n");
                    } else if (ca.endsWith("\\")) {
                        ca.length -= 1;
                        ch.add(ca);
                    } else {
                        ch.add(ca);
                        templateIni.enqueue(new CharArray(ch));
                        ch.reset();
                    }
                }
                if (ch.length() > 0) templateIni.enqueue(new CharArray(ch));
                CharArray.push(ch);
                CharToken.push(token);
                templateIni.parse();
                inputHV    = templateIni.getKeyTable("[InputRef]");
                templateHV = templateIni.getKeyTable("[Template]");
                macroHV    = templateIni.getKeyTable("[Macro]");
            }
        }
        cbScript   = new ContentScript();
        cbTemplate = new ContentTemplate(moduleManager, templateHV);
        cbBlock    = new ContentBlock();
        cbInput    = new ContentInput(inputHV);
        if (macroHV != null) cbMacro = new ContentMacro(macroHV);
        
        parse();
    }
    
    /* ファイル名を設定する<br>
        同時にファイル内容が読み込まれる
    */
    public void set(String filename) {
        if (caFile == null) caFile = new CharArrayFile();
        caFile.setFilename(filename);
        caFile.read();
    }
    
    /* ページを設定する */
    public void setPage(Page page) { this.page = page;}
    /* ページを取得する */
    public Page getPage() { return this.page; }
    
    /** 再度読み直す */
    public void refresh() {
        caFile.read();
        
        // 2010-05-27 絵文字コンバート追加
        //CharArray ch = EmojiConverter.convFromText(caFile);
        //caFile.set(ch);
        
        parse();
    }
    
    /* ファイルを指定する*/ // 使わないはず
    public void setFile(CharArrayFile file) { this.caFile = file; }

    /* ファイルを参照する */
    public CharArrayFile getFile() { return this.caFile; }
    
    /* Iniファイルを参照する */
    public IniFile getIni() { return this.templateIni; }
    
    /** ファイルをパースしてスクリプトを抜き出す */
    public void parse() { parse(null);}
    
    /* セッション情報を利用してファイルをパースする (0.860) */
    public void parse(SessionObject session) {
        parseErrMsg.clear();    //@@// 全ユーザー参照になるのでよろしくない
                                
        if (debug || debugParse) System.out.println("★☆ContentParser parse :"+caFile.getFilename()+" session:"+(session != null));

        ModuleManager mm = null;
        if (page != null) {
            ModuleServlet module = (ModuleServlet)page.getModule();
            if (module != null) mm = module.getModuleManager();
        }
        if (mm == null) {
            if (session == null) session = this.session;
            if (session != null) mm = session.getModuleManager();
        }

        CharArray caF = caFile;
        //if (convertMacro && nameSpace.length() > 0) caF = convertMacro(caFile, mm);   // マクロのコンバート
        if (convertMacro) {
            if (debug || debugParse) System.out.println("session:"+(session != null)+" mm:"+(mm != null));
            if (mm == null) {
                mm = moduleManager;
            }
            caF = convertMacro(caFile, mm);   // マクロのコンバート
        }
        
        if (convertTemplate) {  // 共通テンプレートのコンバート
            if (mm != null) {
                HashVector<CharArray,CharArrayQueue> hv = mm.getReplaceKeyword();
                if (hv != null) {
                    for (int i = 0; i < hv.size(); i++) {
                        CharArray key = (CharArray)hv.keyElementAt(i);
                        CharArrayQueue queue = (CharArrayQueue)hv.elementAt(i);
                        if (key != null && queue != null && queue.size() > 0 && 
                            key.trim().length() > 0) {
                            CharArray data = queue.peek();
                            if (data != null) {
                                caF.replace(key, data.trim());
if (debug) System.out.println("◆◆置換します["+key+"]→["+data+"]");
                            }
                        }
                    }
                }
                mm.replaceTemplate(caF);
                /*
                caF.replace("@SITE_KEY", mm.getSiteKey());
                caF.replace("@SITE_NAME", mm.getSiteName());
                caF.replace("@SITE_CODE", mm.getSiteCodeString());
                caF.replace("@CHANNEL_NAME", mm.getChannelName());
                caF.replace("@CHANNEL_CODE", mm.getChannelCodeString());
                caF.replace("@SITE_CHANNEL_CODE", mm.getSiteChannelCodeString());
                caF.replace("@SITE_THEME", mm.getTheme());
                */
            }
        }
        super.clear();
        int index   =  0;       // 
        int f_index = -1;       // @(           フレームインデックス
        
        int s_index = -1;       // <!--@        スクリプトインデックス
        int t_index = -1;       // <template    テンプレートインデックス
        int b_index = -1;       // <block       BlockIndex
        int e_index = -1;       // </block>
        int i_index = -1;       // <input:      入力パラメータインデックス
        int st_index = -1;      // @            短縮テンプレートインデックス
        
        //HashVector hv = page.getModule().getModuleManager().getHashTemplate();
        cbScript.clear();
        cbTemplate.clear();
        cbInput.clear();
        cbBlock.clear();
        
        CharArray chAdding = CharArray.pop();
        while (index < caF.length) {
            f_index = caF.indexOf(frameStart, index);
            s_index = caF.indexOf(scriptStart, index);
            t_index = caF.indexOf(templateStart, index);
            i_index = caF.indexOf(inputStart, index);      //@@//
            b_index = caF.indexOf(blockStart, index);
            e_index = caF.indexOf(blockEnd, index);
            st_index = caF.indexOf(shortTemplateStart, index);
            while (b_index >= 0) {
                char c = caF.chars[b_index+blockStart.length()];
                if (c == '/' || c == ' ' || c == '>') break;
                if (debugParse) System.out.println("<block"+c+" :ブロックじゃない");
                b_index = caF.indexOf(blockStart, b_index+blockStart.length());
            }
            if (f_index < 0 && s_index < 0 && t_index < 0 && b_index < 0 && e_index < 0 && i_index < 0 && st_index < 0) {
                //以降を保存して終了
                //CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                CharArray ch = new CharArray(chAdding);
                ch.add(caF.chars,index,caF.length-index);
                enqueue(ch);
                chAdding.clear();
                if (debug) System.out.println("parse break (script or template not found)");
                break;
            } else {
                if (f_index == st_index) st_index = -1;
                
                // 一番近いデータを取り出し他には-1を入れる
                ia[0] = s_index; ia[1] = t_index; ia[2] = b_index;  ia[3] = e_index; 
                ia[4] = i_index; ia[5] = f_index; ia[6] = st_index;
                int min = Integer.MAX_VALUE;
                for (int i = 0; i < ia.length; i++) {
                    if (ia[i] >= 0) {
                        if (ia[i] < min) {
                            min = ia[i];
                            for (int j = 0; j < i; j++) ia[j] = -1;
                        } else {
                            ia[i] = -1;
                        }
                    }
                }
                s_index = ia[0]; t_index = ia[1]; b_index  = ia[2]; e_index = ia[3]; 
                i_index = ia[4]; f_index = ia[5]; st_index = ia[6];
                //そこまでのデータを保存しておく
                if (st_index >= 0) {
                    chAdding.add(caF.chars,index,min-index);
                } else {
                    CharArray chTmp = new CharArray(chAdding);
                    chTmp.add(caF.chars,index,min-index);
                    enqueue(chTmp);
                    chAdding.clear();
                }
                if (f_index >= 0) {         // フレーム処理
                    if (debug) System.out.println("   parse frame index="+f_index);
                    int start_index = f_index + frameStart.length();
                    int end_index = caF.indexOf(frameEnd, f_index);    // スクリプトENDを探す
                    
                    boolean done = false;
                    do {
                        if (end_index < 0) {
                            if (debug) System.out.println("   { parse error!} can't found frame end");
                            enqueue(frameStart);
                            index = start_index;
                            done = true;    // 一応ＯＫにしておく
                            break;
                        }
                        if (end_index - start_index > MAX_FRAME_STR) {
                            String msg = "フレーム文字列が長すぎます";
                            System.out.println(msg);
                            parseErrMsg.add(msg).add("\n");
                            break;
                        }
                        
                        // 途中にタグが入っていたらエラーにする
                        int _x1 = caF.indexOf("<", start_index);
                        int _x2 = caF.indexOf(">", start_index);
                        if ((_x1 >= 0 && _x1 < end_index) || (_x2 >= 0 && _x2 < end_index)) {
                            
                            CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                            enqueue(ch);
                            parseErrMsg.add("フレーム情報が正しくありません\n");
                            index = caF.length;
                            break;
                        }
                        
                        CharArray chFrame = new CharArray(caF.chars, start_index, end_index-start_index);
                        if (!chFrame.isUpperCase("$_")) {
                            String msg = "@("+chFrame+")フレーム文字は英大文字を使用してください";
                            System.out.println(msg);
                            parseErrMsg.add(msg).add("\n");
                            
                            index = end_index + frameEnd.length();
                            CharArray ch = new CharArray(caF.chars,f_index,index-f_index);
                            enqueue(ch);
                            done = true;    // 一応ＯＫにしておく
                            break;
                        }
                        
                        CharArray ch = new CharArray(f_magic);
                        ch.add(chFrame);
                        enqueue(ch);
                        index = end_index + frameEnd.length();
                        
                        done = true;
                    } while (false);
                    if (!done) {
                        if (debug) System.out.println("   { parse error!} frame error");
                        CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                        enqueue(ch);
                        index = caF.length;  // ファイルよみこみ終了
                    }
                } else if (s_index >= 0) {         // スクリプト処理
                    if (debug) System.out.println("   parse script index="+s_index);
                    int start_index = s_index + scriptStart.length();
                    int end_index = caF.indexOf(scriptEnd, start_index);    // スクリプトENDを探す
                    if (end_index >= 0) {  // found !!
                    
                        // 途中にタグが入っていたらエラーにする
                        int _x1 = caF.indexOf("<", start_index);
                        int _x2 = caF.indexOf(">", start_index);
                        if ((_x1 >= 0 && _x1 < end_index) || (_x2 >= 0 && _x2 < end_index)) {
                            
                            CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                            enqueue(ch);
                            parseErrMsg.add("<--@ タグが正しくありません\n");
                            index = caF.length;
                            break;
                        }
                    
                        int i = cbScript.parse(caF.chars, start_index, end_index - start_index, parseErrMsg);
                        CharArray ch = new CharArray(s_magic);
                        ch.format(i);
                        enqueue(ch);
                        index = end_index + scriptEnd.length();
                    } else {
                        if (debug) System.out.println("   { parse error!} script end not found !!");
                        CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                        enqueue(ch);
                        parseErrMsg.add("<!--@ スクリプト（コメント）タグが閉じられていません\n");
                        index = caF.length;
                        break;
                    }
                } else if (st_index >= 0) { // ショートテンプレート処理
                    if (debug) System.out.println("▼parse short template index="+st_index);
                    int start_index = st_index + shortTemplateStart.length();
                    CharArray key  = new CharArray();
                    //CharArray param = new CharArray();
                              // 配列        開始位置     サイズ
                    key.add(shortTemplateStart);
                    //param.clear();
                    CharArrayQueue paramQueue = new CharArrayQueue();
                    int end_index = parseMacro(caF.chars, start_index, caF.length,
                                           key, paramQueue, parseErrMsg, ';', cbTemplate);
                    if (end_index != -1) {
                        //if (debug) System.out.println("short template :"+key+"["+param+"]");
                        IntQueue iq = cbTemplate.parse(key, paramQueue, parseErrMsg);
                        if (iq.size() > 0) { // OK
                            if (chAdding.length() > 0) {
                                enqueue(new CharArray(chAdding));
                                chAdding.clear();
                            }
                            
                            index = end_index;
                            for (int i = 0; i < iq.size(); i++) {
if (debug) System.out.println("【"+key+"】をマクロ登録します:"+iq.peek(i));
                                CharArray ch = new CharArray();
                                ch.add(t_magic);
                                ch.format(iq.peek(i));
                                enqueue(ch);
                            }
                        } else {
                            System.out.println("ContentParser#ここは通らないはず:short template");
                            index = start_index;
                            chAdding.add(shortTemplateStart);
                        }
                    } else {    // 文字列として処理する
                        //System.out.println("文字列として処理します");
                        index = start_index;
                        chAdding.add(shortTemplateStart);
                    }
                    //CharArray.push(param);
                    //CharArray.push(key);
                } else if (t_index >= 0) {  // テンプレート処理
                    if (debug) System.out.println("   parse template index="+t_index);
                    int start_index = t_index + templateStart.length();
                    int header_index = caF.indexOf('>',start_index);
                    int end_index    = caF.indexOf(templateEnd, start_index);
                    boolean shortTag = false;
                    if (header_index > 0) shortTag = caF.chars[header_index-1] == '/';   // 短縮
                    if (header_index > 0 && (shortTag || end_index > header_index)) { // found
                        //int no = cbTemplate.parse(caF.chars, start_index, header_index, end_index, shortTag);

                        if (!shortTag) { // 途中に余計なタグが入っていたらエラーにする
                            int _x1 = caF.indexOf(scriptStart, start_index);
                            int _x2 = caF.indexOf(templateStart, start_index);
                            int _x3 = caF.indexOf(blockStart, start_index);
                            int _x4 = caF.indexOf(blockEnd, start_index);
                            int _x5 = caF.indexOf(inputStart, start_index);
                            if ((_x1 >= 0 && _x1 < end_index) || 
                                (_x2 >= 0 && _x2 < end_index) ||
                                (_x3 >= 0 && _x3 < end_index) ||
                                (_x4 >= 0 && _x4 < end_index) ||
                                (_x5 >= 0 && _x5 < end_index) ) {
                                
                                CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                                enqueue(ch);
                                parseErrMsg.add("<template>タグ中に問題のあるタグが存在します\n");
                                index = caF.length;
                                break;
                            }
                        }
                        
                        // 一応 Queueインタフェースのままにしておく（今は１個のみ）
                        IntQueue iq = cbTemplate.parse(caF.chars, start_index, header_index, end_index, shortTag, parseErrMsg);
                        
                        if (iq.size() > 0) {
                            for (int i = 0; i < iq.size(); i++) {
                                CharArray ch = new CharArray();
                                ch.add(t_magic);
                                ch.format(iq.peek(i));
                                enqueue(ch);
                            }
                            if (shortTag) {
                                index = header_index + 1;
                            } else {
                                index = end_index + templateEnd.length();
                            }
                        } else {
                            CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                            enqueue(ch);
                            parseErrMsg.add("<template> タグに何かの問題があります\n");
                            index = caF.length;
                            break;
                        }
                    } else {
                        if (debug) System.out.println("   { parse error!} template end not found !!");
                        CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                        enqueue(ch);
                        parseErrMsg.add("<template> タグが閉じられていません\n");
                        break;
                    }
                    //if (debug) System.out.println("parse debug break");
                } else if (i_index >= 0) {             // 入力パラメータ処理
//System.out.println("★★入力パラメータ発見");
                    int no = -1;
                    if (debug) System.out.println("   parse input: index="+i_index);
                    int start_index = i_index + inputStart.length();
                    int end_index    = caF.indexOf(inputEnd, start_index);
                    if (debugParse) System.out.println("start_index="+start_index+" end_index="+end_index);
                    if (end_index > 0) { // found
                        // 途中にタグが入っていたらエラーにする
                        int _x1 = caF.indexOf('<', start_index);
                        if (_x1 >= 0 && _x1 < end_index) {
                            CharArray ch = new CharArray(chAdding);
                            ch.set(caF.chars,index,caF.length-index);
                            enqueue(ch);
                            parseErrMsg.add("<input: タグが正しくありません\n");
                            index = caF.length;
                            break;
                        }
                        // 2010-08-17
                        boolean shortTag = true;
                        boolean isScript = false;
                        boolean isStyle  = false;
                        int start_index2 = -1;
                        int end_index2 = -1;
                        int short_index = caF.indexOf(inputShortEnd, start_index);
                        if (caF.chars[end_index-1] != '/') {
                            if (caF.startsWith("STYLE", start_index)) {
                                isStyle = true;
                                end_index2 = caF.indexOf(inputStyleEnd, end_index+inputEnd.length());
                                if (end_index2 >= 0) {
                                    //System.out.println("  "+inputStyleEnd+" 発見");
                                    int idx = caF.indexOf(inputStyleStart, end_index+inputEnd.length());
                                    if (idx >= 0 && idx < end_index2) {
                                        end_index2 = -1;
                                    } else {
                                        shortTag = false;
                                    }
                                }
                            } else if (caF.startsWith("SCRIPT", start_index)) {
                                isScript = true;
                                end_index2 = caF.indexOf(inputScriptEnd, end_index+inputEnd.length());
                                if (end_index2 >= 0) {
                                    //System.out.println("  "+inputScriptEnd+" 発見");
                                    int idx = caF.indexOf(inputScriptStart, end_index+inputEnd.length());
                                    if (idx >= 0 && idx < end_index2) {
                                        end_index2 = -1;
                                    } else {
                                        shortTag = false;
                                    }
                                }
                            }
                        }
                        if (!shortTag) {
                            if (isStyle)       start_index2 = end_index+inputEnd.length();
                            else if (isScript) start_index2 = end_index+inputEnd.length();
                            _x1 = caF.indexOf(scriptStart, start_index2);
                            int _x2 = caF.indexOf(templateStart, start_index2);
                            int _x3 = caF.indexOf(templateEnd, start_index2);
                            int _x4 = caF.indexOf(blockStart, start_index2);
                            int _x5 = caF.indexOf(blockEnd, start_index2);
                            int _x6 = caF.indexOf(inputStart, start_index2);
                            if ((_x1 >= 0 && _x1 < end_index2) || 
                                (_x2 >= 0 && _x2 < end_index2) ||
                                (_x3 >= 0 && _x3 < end_index2) ||
                                (_x4 >= 0 && _x4 < end_index2) ||
                                (_x5 >= 0 && _x5 < end_index2) ||
                                (_x6 >= 0 && _x6 < end_index2) ) {
                                
                                CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                                enqueue(ch);
                                parseErrMsg.add("<input: タグ中に問題のあるタグが存在します\n");
                                index = caF.length;
                                break;
                            }
                            
                            no = cbInput.parse(caF.chars, start_index, end_index, start_index2, end_index2, parseErrMsg, session);
                            if (no >= 0 ) {
                                CharArray ch = new CharArray();
                                ch.add(i_magic);
                                ch.format(no);
                                enqueue(ch);
                            }
                            
                            if (isStyle) index = end_index2+inputStyleEnd.length();
                            else if (isScript) index = end_index2+inputScriptEnd.length();
                        } else {
                            no = cbInput.parse(caF.chars, start_index, end_index, start_index2, end_index2,parseErrMsg, session);
                            if (no >= 0 ) {
                                CharArray ch = new CharArray();
                                ch.add(i_magic);
                                ch.format(no);
                                enqueue(ch);
                            }
                            index = end_index+inputEnd.length();
                       }
                    } else {
                        if (debug) System.out.println("   { parse error!} input end not found !!");
                        parseErrMsg.add("<input: タグが閉じられていません\n");
                    }
                    if (no == -1) {
                        //CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                        CharArray ch = new CharArray(chAdding);
                        ch.add(caF.chars,index,caF.length-index);
                        enqueue(ch);
                        index = caF.length;
                        chAdding.clear();
                        break;
                    }
                } else if (b_index >= 0) {             // ブロック開始処理
                    if (debug) System.out.println("   parse block start index="+b_index);
                    int start_index  = b_index + blockStart.length();
                    int header_index = caF.indexOf('>',start_index);
                    if (header_index < 0) {
                        // 途中にタグが入っていたらエラーにする
                        int _x1 = caF.indexOf('<', start_index);
                        if (_x1 >= 0 && _x1 < header_index) {
                            CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                            enqueue(ch);
                            parseErrMsg.add("<block タグが正しくありません\n");
                            index = caF.length;
                            break;
                        }
                    
                        if (debug) System.out.println("   { parse error!} <block end not found !!");
                        parseErrMsg.add("<block タグが閉じられていません\n");
                        CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                        enqueue(ch);
                        break;
                    }
                    CharArray ch = new CharArray();
                    ch.add(b_magic);
                    
                    int no = cbBlock.parse(caF.chars, start_index, header_index, size(), parseErrMsg);
                    ch.format(no);
                    if (debug) System.out.println("   parse block start no="+no);
                    
                    enqueue(ch);
                    index = header_index+1;
                } else if (e_index >= 0) {             // ブロック終了処理
                    if (debug) System.out.println("   parse block end index="+e_index);
                    int no = cbBlock.parseEnd(size(), parseErrMsg);
                    if (no == -1) {
                        if (debug) System.out.println("   { parse error!} block_end");
                        CharArray ch = new CharArray(caF.chars,index,caF.length-index);
                        enqueue(ch);
                        break;
                    }
                    CharArray ch = new CharArray();
                    ch.add(e_magic);
                    
                    ch.format(no);
                    if (debug) System.out.println("   parse block end no="+no);
                    
                    enqueue(ch);
                    index = e_index + blockEnd.length();
                }
            } //endif
        } // end while
        if (parseErrMsg.length()==0) {
            // ブロック整合性のチェック
            int b_count = 0;
            for (int i = 0; i < size(); i++) {
                CharArray line = peek(i);
                char c = line.chars[0];
                if (c == b_magic) ++b_count;
                else if (c == e_magic) --b_count;
            }
            if (b_count > 0) parseErrMsg.add("</block>タグが足りません\n");
            else if (b_count < 0) parseErrMsg.add("</block>タグが多すぎます\n");
        }
        CharArray.push(chAdding);
        if (parseErrMsg.length()>0) {
            ServletLog.getInstance().error(caFile.getFilename()+":"+
                new CharArray(parseErrMsg).replace("\n"," "));
            System.out.println("★parseエラーが発生しています!★"+caFile.getFilename());
            System.out.println(parseErrMsg.toString());
        }
        if (debug || debugParse) System.out.println("ContentParser parse end  ==========================================");
    }
    
    /**
        短縮テンプレートのパースを行う（マクロは検討中）
        @param chars 文字配列
        @param start_index 開始インデックス
        @param end_index  検査終了最大インデックス
        @param key  キーを入れる
        @param paramQueue パラメータ文字列配列
        @param parseErrMsg エラーメッセージ用バッファ
        @param separator セパレータ文字列
        @param cbTemplate ContentTemplate
        @return index
    */
    static protected int parseMacro(char[] chars, int start_index, int end_index, 
                         CharArray key, CharArrayQueue paramQueue, CharArray parseErrMsg, char separator,
                         ContentTemplate cbTemplate) {
        int index = -1;
        
        do {
            int key_length = key.length();
            index = start_index;
            
            boolean param_mode = false;
            // スペーシング文字または
            for (int i = index;  i < end_index; i++) {
                char c = chars[i];
                if ('A' <= c && c <= 'Z') {
                    key.add(c);
                } else if ('a' <= c && c <= 'z') {
                    key.add(c);
                } else if ('0' <= c && c <= '9') {
                    key.add(c);
                } else if (c == '_') {
                    key.add(c);
                } else if (c == '(') {
                    param_mode = true;
                    index = i + 1;
                    break;
                } else if (c == ';') {  // 取り込んで終了
                    index = i + 1;
                    break;
                } else if (c < '0') {
                    index = i;
                    break;
                } else if (c >= ':' && c <= '@') {
                    index = i;
                    break;
                } else {
                    index = -1;
                    break;
                }
                if (key.length > MAX_MACRO_KEY) {
                    System.out.println("キーワードが長すぎます:"+key);
                    index = -1;
                    break;
                }
            }
            if (index == -1) {
                break;
            }
            if (key.length <= key_length) {
                if (debugParse) System.out.println("キーワードが取得できません:"+key);
                index = -1;
                break;
            }
            if (key.chars[0] == '@') {
                for (int i = 0; i < systemTemplate.length; i++) {
                    if (key.equals(systemTemplate[i])) {
                        if (debugParse) System.out.println("システム予約テンプレートです:"+key);
                        index = -1;
                        break;
                    }
                }
                // 存在チェックに変更
                if (cbTemplate != null && cbTemplate.existCommonTemplate(key)) {
                    if (debugParse) System.out.println("共通テンプレートです:"+key);
                } else {
                    if (debugParse) System.out.println("共通テンプレートではありません:"+key);
                    index = -1;
                    break;
                }
            }
            if (index == -1) {
                break;
            }
            
            if (param_mode) {   // 関数チェック
                boolean hasSeparator= false;
                CharArray param = new CharArray();
                int nest = 1;   // () のネストチェックを行う
                for (int i = index;  i < end_index; i++) {
                    char c = chars[i];
                    if (c == '(') {
                        param .add(c);
                        nest++;
                    } else if (c == ')') {
                        if (--nest == 0) {
                            index = i + 1;
                            if (param.length() > 0 || hasSeparator) paramQueue.enqueue(param);
                            break;
                        } else {
                            param .add(c);
                        }
                    } else if (c == separator) {
                        hasSeparator = true;
                        paramQueue.enqueue(new CharArray(param));
                        param.clear();
                    } else {
                        param .add(c);
                    }
                    if (param.length > MAX_MACRO_PARAM) {
                        System.out.println("パラメータが長すぎます:"+key+":"+param);
                        index = -1;
                        break;
                    }
                    if (i == end_index -1) {    // 関数終了が見つからない
                        System.out.println("エラー２:"+key+"("+param+")");
                        index = -1;
                        break;
                    }
                } // next
                if (index == -1) break;
                if (chars[index] == ';') index++;
            }   // endif (param_mode)
        } while (false);

        return index;
    }
    
    
    /*
        描画を行う(セッションバッファ使用)
    */
    public CharArray draw(SessionObject session) {
        return draw(session,null, false);
    }
    /**
        指定バッファに描画を行う
        @param session SessionObject
        @param ch      描画バッファ(null でセッションバッファを使用する)
        @return 描画バッファ
    */
    public CharArray draw(SessionObject session, CharArray ch) {
        return draw(session, ch, false);
    }
    /**
        指定バッファに描画を行う
        @param session SessionObject
        @param frame_mode  フレームテンプレートとして扱う
        @return 描画バッファ
    */
    public CharArray draw(SessionObject session, boolean frame_mode) {
        return draw(session,null, frame_mode);
    }
    /**
        指定バッファに描画を行う
        @param session SessionObject
        @param ch      描画バッファ(null でセッションバッファを使用する)
        @param frame_mode  フレームテンプレートとして扱う
        @return 描画バッファ
    */
    public CharArray draw(SessionObject session, CharArray ch, boolean frame_mode) {
        long timer = Util.Timer();
        if (debug || debugDraw || debugTimer|| debugNamespace) System.out.println("★☆ContentParser ["+nameSpace+"] draw start------------- ");
        
        drawErrMsg.clear();
        if (ch == null) {
if (debug) System.out.println("※Sessionから "+nameSpace+" のバッファを取得します");
            ch = session.getBuffer(nameSpace);
        }
        frame_mode |= frameMode;    // どちらかが立っていればフレームモードで動作する
        
        Hashtable<CharArray,CharArray> hash = null;  // フレームモードの時の一時記憶領域
                                // key: CharArray nameSpace
                                // param: CharArray (出力バッファ）
        if (frame_mode) hash = new Hashtable<CharArray,CharArray>();
        
        CharArray tmp = CharArray.pop();
        cbBlock.init(session, nameSpace);    // セッション情報の初期化
        for (int i = 0; i < size(); i++) {
if (debugDraw || debugNamespace) System.out.println("draw["+i+"/"+size()+"]"+nameSpace+":"+ch.length());
            CharArray line = peek(i);
            char c = line.chars[0];
            switch (c) {
              case f_magic:                // フレーム
                if (frame_mode) {
                    tmp.set(line,1);
                    CharArray data = (CharArray)hash.get(tmp);
                    if (data != null) {
                        ch.add(data);
                    } else {
                        ContentParser parser = session.getParser(tmp.toString());
                        if (parser != null) {
                            parser.clearBuffer(session);
                            data = parser.draw(session);
                            hash.put(new CharArray(tmp), data);
                            ch.add(data);
                        } else {
                            System.out.println("★ContentParser("+nameSpace+")draw:"+
                                               "parser("+tmp+")が見つかりません");
                        }
                    }
                } else {
                    System.out.println("★ContentParser("+nameSpace+")draw: frame mode ではありません");
                }
                break;
              case m_magic:                // マクロ
                //@@// 処理未定
                break;
              case t_magic:                // テンプレート
                if (debug) System.out.println(""+i+"[template]:"+line.toString(1));
                cbTemplate.draw(session, line.getInt(1,10), nameSpace);
                if (debug) System.out.println(""+i+"[template]: end");
                break;
              case s_magic:                 // スクリプト
                if (debug) System.out.println(""+i+"[script]:"+line.toString(1));
                cbScript.draw(session,line.getInt(1,10), nameSpace);
                break;
              case b_magic:                 // ブロック開始
                if (debug) System.out.println(""+i+"[block start]:"+line.toString(1));
                int next = cbBlock.drawStart(session, line.getInt(1,10), nameSpace);
                if (next >= 0) i = next - 1;
                break;
              case e_magic:                 // ブロック終了
                if (debug) System.out.println(""+i+"[block end]:"+line.toString(1));
                int prev = cbBlock.drawEnd(session, line.getInt(1,10), nameSpace);
                if (prev >= 0) i = prev - 1;
                break;
              case i_magic:                 // 入力パラメータ
                if (debug) System.out.println(""+i+"[input]:"+line.toString(1));
                // do nothing
                break;
              default:                      // 通常文字列、タグ等
                if (debug) System.out.println(""+i+"[normal]:"+line);
                
                //2012-06-26 空白行のみの出力を中止する※
                //line.trimR();
                //if (line.length() > 0) ch.add(line);
                if (!line.isSpace()) ch.add(line);
                break;
            }
            if (ch.length() > SystemManager.maxRenderingSize * 1024) {
                drawErrMsg.add("HTMLサイズが"+SystemManager.maxRenderingSize+"Kをオーバーしたので中断します\n");
                break;
            }
        }   // next
        CharArray.push(tmp);
        if (/*debugDraw &&*/drawErrMsg.length() > 0) {
            System.out.println("★☆ContentParser draw error");
            System.out.println(drawErrMsg.toString());
            ch.add("<br><hr>\n");
            ch.add(drawErrMsg);
        }
if (debug) System.out.println("★☆ch["+ch+"]");
        if (debug || debugDraw || debugTimer) System.out.println("★☆ContentParser ["+nameSpace+"] draw end lapse:"+Util.Lapse(timer)+" size:"+ch.length()+"("+ch.strlen()+") --------- ");
        return ch;
    }
    
    //-------------------------------------------------------------------
    // 入力関連メソッド
    //-------------------------------------------------------------------
    public void sessionBlockInit(SessionObject session) {
        cbBlock.init(session, nameSpace);    // セッション情報の初期化
    }
    
    /*
        ＵＩ用のContentItem情報を返します
        かならずキャッシュから取得します（新たに生成しない）
        存在しない場合はnullを返します。
    */
    public ContentItemInfo getContentItemInfo() {
//if (parser_mode == 0) info.debugNode();
        return info;
    }
    /**
        ＵＩ用のContentItem情報を返します
        @param session セッション
        @return ContentItemInfo
    */
    public ContentItemInfo getContentItemInfo(SessionObject session) {
        return getContentItemInfo(session,true);
    }
    /**
        ＵＩ用のContentItem情報を返します
        @param session セッション
        @param mode    true 新規に生成 false:存在すれば利用する
        @return ContentItemInfo
    */
    public ContentItemInfo getContentItemInfo(SessionObject session, boolean mode) {
if (debugParse) System.out.println("★☆ mode:"+mode+" info:"+(info !=null)+" size:"+size());
        if (mode || info == null) {
            info = new ContentItemInfo(this);
if (debugParse || debugTMCount) {
if (debugTMCount) session.println("★★★★★★★★★★★★★★★★★★★★★★");
session.println("★☆ contentItemInfo を新規に生成します ☆★"+nameSpace+" parser_mode:"+parser_mode+" tm_count:"+info.getTMCount());
if (debugTMCount) session.println("★★★★★★★★★★★★★★★★★★★★★★");
}

        } else {
if (debugParse||debugTMCount) session.println("★☆ 既存の contentItemInfo を利用します ★☆"+nameSpace+" tm_count:"+info.getTMCount());
            info.clear();
        }
        CounterQueue counterQueue = new CounterQueue(size());
        int counter = 0;

        int nest = 0;
        cbBlock.init(session, nameSpace);    // セッション情報の初期化
        for (int i = 0; i < size(); i++) {
            CharArray line = peek(i);
            char c = line.chars[0];
            switch (c) {
              case f_magic:                // フレーム
              case m_magic:                // マクロ
              case s_magic:                // スクリプト
                // do noting
                break;
              case b_magic:                 // ブロック開始
                counter = counterQueue.inc(i);
                info.add(i,counter,++nest);
                
                if (debug) System.out.println("[block start]("+i+":"+counter+")"+line.toString(1));
                int next = cbBlock.drawStart(session, line.getInt(1,10), nameSpace);
                
                //getContentItem
                Block inputBlock = (Block)getContentItem(i);
                
                //int max = inputBlock.getMax();
                
                // 異常データ対応
                int max = Util.MinMax(0, inputBlock.getMax(), inputBlock.getLimit());
                if (max != inputBlock.getMax()) {
                    System.out.println("block["+inputBlock.getLabel()+"] max:"+inputBlock.getMax()+"->"+max);
                    inputBlock.setMax(max);
                }
                
                if (max == 0) nest--;       //@@//
                if (next >= 0) i = next - 1;
                
                break;
              case e_magic:                 // ブロック終了
                counter = counterQueue.inc(i);
                info.add(i,counter,nest--);
              
                inputBlock = ((BlockEnd)getContentItem(i)).getBlock();
                max = inputBlock.getMax();
                if (max == 0) nest++;       //@@//
                
                if (debug) System.out.println("[block end]("+i+":"+counter+")"+line.toString(1));
                int prev = cbBlock.drawEnd(session, line.getInt(1,10), nameSpace);
                if (prev >= 0) i = prev - 1;
                break;
              case i_magic:                 // 入力パラメータ
                counter = counterQueue.inc(i);
                info.add(i,counter, nest);
                if (debug) System.out.println("[input]("+i+":"+counter+")"+line.toString(1));
                break;
              case t_magic:                // テンプレート 
                counter = counterQueue.inc(i);
                info.add(i,counter,nest);
              
                if (debug) System.out.println("[template]("+i+":"+counter+")"+line.toString(1));
              
                break;
              default:                      // 通常文字列、タグ等
                // do notiong
                break;
            }
        }   // next

        if (debug) System.out.println("ContentParser getContenItemInfo end------------------");
        return info;
    }
    
    /** 
        ContentItemNodeを生成して返します 
        @return ルートノード
    */
    public ContentItemNode getContentItemNode() {
        ContentItemNode rootNode = new ContentItemNode();
        
        ContentItemNode parent = rootNode;
        for (int i = 0; i < size(); i++) {
            CharArray line = peek(i);
            char c = line.chars[0];
            switch (c) {
              case f_magic:                // フレーム
              case m_magic:                // マクロ
              case s_magic:                // スクリプト
                // do noting
                break;
              case b_magic:                 // ブロック開始
                //getContentItem
                Block block = (Block)getContentItem(i);
                int max = block.getMax();
                ContentItemNode node = new ContentItemNode(block);
                parent.add(node);
                parent = node;
                break;
              case e_magic:                 // ブロック終了
                Block block2 = ((BlockEnd)getContentItem(i)).getBlock();
                ContentItemNode parent_new = (ContentItemNode)parent.getParent();
                parent = parent_new;
                break;
              case i_magic:                 // 入力パラメータ
                Input input = (Input)getContentItem(i);
                ContentItemNode node_i = new ContentItemNode(input);
                parent.add(node_i);
                break;
              case t_magic:                // テンプレート  
                break;
              default:                      // 通常文字列、タグ等
                // do notiong
                break;
            }
        }   // next
        return rootNode;
    }
    /**
        指定位置のContentItemを返します
        @param index 行指定 (0-
        @return ContentItem (Block || BlockEnd || Input) 違うタイプの場合は null
    */
    public ContentItem getContentItem(int index) {
        ContentItem _item = null;
        CharArray line = peek(index);
        if (line != null && line.length() >= 2) {
            char c = line.chars[0];
            int no = line.getInt(1,10);
            switch (c) {
              case b_magic:                 // ブロック開始
                _item = (ContentItem)cbBlock.getBlock(no);
                break;
              case e_magic:                 // ブロック終了
                Block _block = cbBlock.getBlock(no);
                if (_block != null) _item = (ContentItem)_block.getBlockEnd();
                break;
              case i_magic:
                _item = (ContentItem)cbInput.getInput(no);
                break;
              case t_magic:
                _item = (ContentItem)cbTemplate.getTemplate(no);
                break;
              default:
                // do nothing
                break;
            }
        }
        return _item;
    }
    /* magic を返す */
    protected char getMagic(int index) {
        char c = 0;
        CharArray line = peek(index);
        if (line != null && line.length() >=2) {
            c = line.chars[0];
        }
        return c;
    }
    
    /**
        指定キーワードのブロックオブジェクトリストを返します。
        @param key キーワード
        @return 存在しない場合は null
    */
    public ObjectQueue getBlock(String key) {
        return cbBlock.getBlock(key);
    }
    public ObjectQueue getBlock(CharArray key) {
        return cbBlock.getBlock(key);
    }
    
    /**
        指定キーワードの入力オブジェクトを返します。
        @param key キーワード
        @return 存在しない場合は null
    */
    public Input getInput(String key) {
        return cbInput.getInput(key);
    }
    public Input getInput(CharArray key) {
        return cbInput.getInput(key);
    }
    
    /**
        指定タイプの入力オブジェクトリストを返します。
        @param type タイプ
        @return 存在しない場合は null
    */
    public ObjectQueue getInputListFromType(String type) {
        return cbInput.getInputListFromType(type);
    }
    
    //-------------------------------------------------------------------
    // 入力アイテムの情報をParameterItemに設定する
    //-------------------------------------------------------------------
    public void setTemplate(SessionObject session) {
        if (debug || debugTemplate) System.out.println("◆ContentParser#setTemplate start---------------------");
        //--------------------------------------------------------------
        setHeaderTemplate(session);
        
        ContentItemInfo info = getContentItemInfo();
        if (info == null) {
            info = getContentItemInfo(session, false);
            
        }
        info.setTemplate(session);
        if (debug || debugTemplate) System.out.println("◆ContentParser#setTemplate end--------------------");
        if (debug) info.debugParameter(session);
    }
    //-------------------------------------------------------------------
    // 入力アイテムの情報をヘッダタグに設定する
    //-------------------------------------------------------------------
    private static String[] szHeaderTypes = { "meta", "link", "base", "script", "body", "style"};  // style (style) 
    protected void setHeaderTemplate(SessionObject session) {
        if (debug || debugTemplate) System.out.println("◆ContentParser#setHeaderTemplate start---------------------");
        if (cbInput != null) {
            CharArray ch = CharArray.pop();
            CharArray ch2 = CharArray.pop();
            CharArray _value = CharArray.pop();
            
            Hashtable<CharArray,CharArray> _hash = new Hashtable<CharArray,CharArray>();  // 同一のものは追加しない
            for (int type = 0; type < szHeaderTypes.length; type++) {
                String szType = szHeaderTypes[type];
                String szKey  = "$$$"+szType+"$$$";
if (debug && debugTemplate) System.out.println(szKey+"------------------");
                ObjectQueue queue = cbInput.getInputListFromType(szType.toUpperCase());
                if (queue != null) {
if (debug && debugTemplate) System.out.println("  queue found! size="+queue.size());
                    for (int i = 0; i < queue.size(); i++) {
                        ch2.clear();
                        Input input = (Input)queue.peek(i);
                        HashParameter hp = input.getParameterList();
                        if (hp == null || hp.size()==0) continue;
                        if (!szType.equals("body")) {
                            ch2.add('<');
                            ch2.add(szType);
                        }
                        for (int j = 0; j < hp.size(); j++) {
                            CharArray _name=hp.keyElementAt(j);
                            _value.set(hp.valueElementAt(j));
                            _value.replace('\n',' ');_value.replace('\r',' ');
                            _value.replaceTag();
                            
                            ch2.add(' ');
                            ch2.add(_name);
                            ch2.add("=\"");
                            ch2.add(_value);
                            ch2.add("\"");
                        }
                        if      (szType.equals("body"))   ch2.add(" ");
                        else if (szType.equals("script")) {
                            CharArray data = input.getData();
                            if (data.length() == 0) {
                                ch2.add("></script>\n");
                            } else {
                                ch2.add(">");
                                ch2.add(data);
                                ch2.add("</script>\n");
                            }
                            
                        } else if (szType.equals("style"))  {
                            CharArray data = input.getData();
                            if (data.length() == 0) {
                                ch2.add("></style>\n");
                            } else {
                                ch2.add(">"); 
                                ch2.add(input.getData());
                                ch2.add("</style>\n");
                            }
                        }
                        else                              ch2.add("/>\n");

                        int _check = session.checkHeaderTemplate(szKey, ch2); 

                        if (_hash.get(ch2)==null && _check < 0) {   // 同一のものは追加しない
                            _hash.put(ch2,ch2);
                            ch.add(ch2);
                        } else {
                        }
                        ch2.clear();
                    } // next
                    if (ch.length() > 0) {
                        session.addHeaderTemplate(szKey,ch);
                    }
                    if (debug && debugTemplate) System.out.println(ch);
                    ch.clear();
                }
            } // next
            //------------------------------------------------------
            // title 処理
            ObjectQueue queue = cbInput.getInputListFromType("TITLE");
            if (queue != null && queue.size()> 0) {
                //ch.add("<title>");
                for (int i = 0; i < queue.size(); i++) {
                    Input input = (Input)queue.peek(i);
                    HashParameter hp = input.getParameterList();
                    if (hp == null || hp.size()==0) continue;
                    for (int j = 0; j < hp.size(); j++) {
                        CharArray _name=hp.keyElementAt(j);
                        _value.set(hp.valueElementAt(j));
                        _value.replace('\n',' ');_value.replace('\r',' ');
                        _value.replaceTag();
                        if (i != 0 && j != 0) ch.add(' ');
                        ch.add(_value);
                    }
                } // next
                //ch.add("</title>\n");
                session.addHeaderTemplate("$$$title$$$", ch);
            }
            CharArray.push(_value);
            CharArray.push(ch);
        } else System.out.println("  cbInput not found!");
        if (debug || debugTemplate) System.out.println("◆ContentParser#setHeaderTemplate end--------------------");
    }
    /* セッションの対応namespaceのバッファをクリアする */
    public void clearBuffer(SessionObject session) {
        session.clearBuffer(nameSpace);
    }


    /* マクロのコンバート */
    private CharArray convertMacro(CharArray from, ModuleManager mm) {
if (debugMacro) System.out.println("◆convertMacro("+from+") start!");
        ContentMacro commonMacro = null;
        if (mm != null) commonMacro = mm.getContentMacro();
if (debugMacro) System.out.println("mm="+(mm!=null)+" commonMacro = "+(commonMacro != null));
        return convertMacro(from, commonMacro, cbMacro, cbTemplate, parseErrMsg);
        
    }
    
    /* マクロのコンバート */
    static public CharArray convertMacro(CharArray from, ContentMacro commonMacro) {
        return convertMacro(from, commonMacro, null, null, null);
    }
    /* マクロのコンバート */ // cbTemplate は共通テンプレート変換する際に必要
    static private CharArray convertMacro(CharArray from, ContentMacro commonMacro, ContentMacro cbMacro, ContentTemplate cbTemplate, CharArray parseErrMsg) {
        if (cbMacro == null && commonMacro == null) return from;
        //------------------------------------------------------
        CharArray ch = CharArray.popBig();
        CharArray key = CharArray.pop();
        
        int index   =  0;
        int m_index = -1;
        CharArray chAdding = CharArray.pop();
        while (index < from.length) {
            m_index = from.indexOf(macroStart, index);
            if (m_index < 0) {
                ch.add(chAdding);
                ch.add(from.chars,index,from.length-index);
                chAdding.clear();
                break;  // 終了
            }
            int start_index = m_index + macroStart.length();
if (debugMacro) System.out.println(""+from.chars[start_index]+from.chars[start_index+1]+from.chars[start_index+2]);
            //CharArray key  = new CharArray();
            //key.add(macroStart);
            key.set(macroStart);
            
            CharArrayQueue paramQueue = new CharArrayQueue();
            int end_index = parseMacro(from.chars, start_index, from.length,
                                        key, paramQueue, parseErrMsg, ',', cbTemplate);

            if (end_index != -1) {
                CharArray macroText = null;
                if (cbMacro != null) macroText = cbMacro.convert(key, paramQueue);
                if (macroText == null && commonMacro != null) {
if (debugMacro) System.out.println("マクロ("+key+")が見つからないので共通マクロもサーチします");
if (debugMacro) {
    commonMacro.debugAllMacro();
}


                    macroText = commonMacro.convert(key, paramQueue);
                }
                if (macroText != null) {    // マクロ発見！
if (debugMacro) System.out.println("マクロ("+key+")が見つかりました:endindex="+end_index);
                    ch.add(from.chars,index,m_index-index);
                    ch.add(macroText);
                    CharArray.push(macroText);
                } else {
if (debugMacro) System.out.println("マクロ("+key+")ではありません:endindex="+end_index);
                    ch.add(from.chars,index,end_index-index);
                }
                index = end_index;
                
            } else {    // 文字列として処理する
if (debugMacro) System.out.println("文字列として登録");
                ch.add(from.chars,index,start_index-index);
                index = start_index;
            }
        }
        
        CharArray.push(chAdding);
        
        CharArray.push(key);
        //return ch;
        from.set(ch);
        
        CharArray.pushBig(ch);
        return from;
    }
    
    ////////////////////////////////////////////////////////////////////
}

//
// [end of ContentParser.java]
//

