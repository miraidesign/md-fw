//------------------------------------------------------------------------
//    PageServlet.java
//          PageServlet (abstract)
//          Copyright (c) Mirai Design 2010-2014 All Rights Reserved.
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------

package com.miraidesign.servlet;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.miraidesign.common.SystemConst;
import com.miraidesign.content.ContentParser;
import com.miraidesign.content.ContentScript;
import com.miraidesign.content.ContentTemplate;
import com.miraidesign.data.TableDataAccess;
import com.miraidesign.event.ItemEventListener;
//import com.miraidesign.image.EmojiConverter;
import com.miraidesign.renderer.BodyTag;
import com.miraidesign.renderer.Page;
import com.miraidesign.renderer.item.AnchorStringData;
import com.miraidesign.renderer.item.DynamicData;
import com.miraidesign.renderer.item.FormContainer;
import com.miraidesign.renderer.item.Item;
import com.miraidesign.renderer.item.ItemContainer;
import com.miraidesign.renderer.item.ItemConstant;
import com.miraidesign.renderer.item.StringItem;
import com.miraidesign.renderer.item.SubmitItem;
import com.miraidesign.renderer.item.SubmitData;

import com.miraidesign.session.SessionObject;
import com.miraidesign.session.UserAgent;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.system.NaviNode;
import com.miraidesign.system.SiteMapping;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharArrayFile;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.Crypt62;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.ObjectQueue;
import com.miraidesign.util.Parameter;
import com.miraidesign.util.ParameterConverter;
import com.miraidesign.util.Queue;
import com.miraidesign.util.QueueElement;
import com.miraidesign.util.QueueTable;


/**
 *  PageServlet (abstract)
 *
 *  @version 0.5
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public abstract class PageServlet extends Page
               implements ItemEventListener, TableDataAccess {

    static private boolean debug = (SystemConst.debug && false);  // デバッグ表示
    static private boolean debugPaging = (SystemConst.debug && false);  // false
    static private boolean debugRedirect = true;  // false
    static private boolean addTM = true;    // daoチェッカをつける

/* 一時休止 ********/
    // アクセスレベル
//    private int level = 0;

    /* アクセスレベルを設定する */
//    public void setLevel(int level) { this.level = level;}

    /* アクセスレベルを取得する */
//    public int getLevel() { return this.level;}
/***********/

    private ContentParser parser;
    /** ContentScriptを取得する （存在しなければnullを返す) */
    public ContentScript getScript() {
        if (parser == null) return null;
        return parser.getContentScript();
    }
    /** ContentTemplateを取得する （存在しなければnullを返す) */
    public ContentTemplate getTemplate() {
        if (parser == null) return null;
        return parser.getContentTemplate();
    }
    /**
        テンプレートを動的に変更する
    */
    public void changeTemplate(CharArray template, CharArray ini) {
        CharArrayFile caTemplate = new CharArrayFile(template);
        CharArrayFile caIni = new CharArrayFile(ini);
        if (parser != null) {
            parser.changeTemplate(caTemplate, caIni);
        } else {
System.out.println("PageServlet#changeTemplate ERROR");
        }
    }
    /**
        HTMLファイルとテンプレート関連ファイルを再度読み直す
    */
    public void refreshHtml() {
        if (parser != null) {
            parser.refresh();
        }
    }
    /**
        テンプレート関連ファイルを再度読み直す
        (templateタグの file と data)
    */
    public void refreshTemplate() {
        ContentTemplate ct = getTemplate();
        if (ct != null) ct.refresh();
    }

    /**
        テンプレートのテーブル情報を取得する
        @param key テーブルタイトル
        @return テーブルへの参照    存在しない場合はnull
    */
    public QueueTable getTable(CharArray key) {
        QueueTable table = null;
        ContentTemplate ct = getTemplate();
        if (ct != null) {
            table = ct.getTable(key);
        }
        return table;
    }
    public QueueTable getTable(String str) {
        CharArray key = CharArray.pop(str);
        QueueTable table = getTable(key);
        CharArray.push(key);
        return table;
    }

    /**
        外部Module等への情報通知に利用する。<br>
        利用時は overrideする。
    */
    public QueueTable getTable(SessionObject session) {
        return null;
    }

    public QueueTable getTable(SessionObject session, Object message) {
        return null;
    }
    /**
        外部Module等への情報通知に利用する。<br>
        利用時は overrideする。
    */
    public HashParameter getParameter(SessionObject session) {
        return null;
    }
    public HashParameter getParameter(SessionObject session, Object message) {
        return null;
    }
    // Utility
    public String getParameter(Hashtable<String,String[]> hash, String key) {
        String[] strs = null;
        if (hash != null) strs = (String[])hash.get(key);
        if (strs == null) return "";
        return strs[0];
    }

    /**
        外部Module等からの情報通知に利用する。<br>
        利用時は overrideする。
    */
    public void setParameter(SessionObject session, CharArray key, CharArray data) {
    }

    /**
        外部からのメッセージ通知を受け入れるのに利用する
        オーバーライドして利用する
    */
    public boolean notify(HashParameter parameter, Object obj) {
        return true;
    }
    public boolean notify(HashParameter parameter) {
        return notify(parameter, null);
    }

//  /** テンプレートの最大繰返しサイズを返す */
//  public int getTemplateSize(CharArray key) {
//      ContentTemplate template = getTemplate();
//      if (template == null) return -1;
//      return template.getMax(key);
//  }
    protected ObjectQueue forward_queue = new ObjectQueue();
    public ObjectQueue getForwardQueue() { return forward_queue;}

    /** スクリプトの存在するファイルを設定する */
    public boolean setParser(String filename) {
        if (parser != null) {
            parser.setNameSpace(getName());
            parser.set(filename);
            parser.parse();
        } else {

            CharArrayFile file = new CharArrayFile(filename);
            file.setInclude("<include*/>");
            //file.setFileEncoding("MS932");
            file.setEncoding("UTF-8");  // <-- MS932

            ParameterConverter converter = new ParameterConverter();
            converter.setWidth(2);
            file.setConverter(converter);

            if (file.read()) {
                if (getName().trim().length() > 0) {
                    //CharArray ch = EmojiConverter.convFromText(file);
                    //file.set(ch);
                    parser = new ContentParser(file,
                                     this,getModule().getName()+"."+getName()); // add namespace
                } else {
                    parser = new ContentParser(file,this);
                }
                parser.parse();
            } else {
                ++SystemManager.init_error;
                System.out.println("PageServlet:setParser["+filename+"] cannot read");
                SystemManager.init_error_queue.enqueue("PageServlet:setParser["+filename+"] cannot read");
                parser = null;
                return false;
            }
        }
        return true;
    }
    public boolean setParser(CharArray filename) {
        return setParser(filename.toString());
    }

    public void setParser(ContentParser parser) {
        this.parser = parser;
    }
    /** ContentParserを取得する （存在しなければnullを返す) */
    public ContentParser getParser() {
        return parser;
    }
    //------------------------------------------------------
    protected CharArray mimeType = new CharArray();
    /** mimeType設定(for Ajax) */
    public void setMimeType(CharArray ch) { mimeType.set(ch);}
    public void setMimeType(String str) { mimeType.set(str);}
    /** mimeType(for Ajax)を取得する */
    public CharArray getMimeType() { return mimeType;}
    //------------------------------------------------------
    protected CharArray title = new CharArray();
    /** タイトルタグ情報の設定 */
    public void setTitle(CharArray ch) { title.set(ch);}
    public void setTitle(String str) { title.set(str);}
    /** タイトルタグ情報を取得する */
    public CharArray getTitle() { return title; }
    //------------------------------------------------------
    protected CharArrayQueue meta = new CharArrayQueue();
    /** メタタグ追加 */
    public void addMeta(CharArray ch) { meta.enqueue(ch);}
    public void addMeta(String str) { meta.enqueue(str);}
    /** メタタグ情報を取得する */
    public CharArrayQueue getMeta() { return meta;}
    //------------------------------------------------------
    protected CharArray styleSheet = new CharArray();
    /** スタイルシート設定 */
    public void setStyleSheet(CharArray ch) { styleSheet.set(ch);}
    public void setStyleSheet(String str) { styleSheet.set(str);}
    /** スタイルシート追加 */
    public void addStyleSheet(CharArray ch) { styleSheet.add(ch);}
    public void addStyleSheet(String str) { styleSheet.add(str);}
    /** スタイルシート情報を取得する */
    public CharArray getStyleSheet() { return styleSheet;}
    //------------------------------------------------------
    protected CharArrayQueue[] styleSheetURL = { // キャリア別
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
    //------------------------------------------------------
    protected BodyTag body = new BodyTag();
    /** Bodyタグ情報取得 */
    public CharArray getBodyTag() { return body.getTag();}

    /** Bodyタグ情報取得 */
    public CharArray getBodyStyleTag() { return body.getStyleTag();}
    //-------------------------------------------------------
    private CharArray titleName = new CharArray();          // デフォルトタイトル名
    protected ObjectQueue nodeQueue = new ObjectQueue();    // ナビゲーションノード情報
    public ObjectQueue getNodeQueue() { return nodeQueue;}
    /** タイトル名称情報の設定 */
    public void setTitleName(CharArray ch) { titleName.set(ch);}
    public void setTitleName(String str)   { titleName.set(str);}
    /** タイトル名称情報を取得する */
    public CharArray getTitleName() { return titleName; }
    //-----
    private CharArray titleImage = new CharArray();        // タイトルイメージ情報
    /** タイトルイメージ情報の設定 */
    public void setTitleImage(CharArray ch) { titleImage.set(ch);}
    public void setTitleImage(String str)   { titleImage.set(str);}
    /** タイトルイメージ情報を取得する */
    public CharArray getTitleImage() { return titleImage; }
    private int titleMode = 0;      // -1: 常に表示しない 0:フラグ制御 1:常に表示
    public void setTitleMode(int i) { titleMode = i;}
    public int getTitleMode() { return titleMode;}

    //-----
    /** ガイダンス情報を保管する */
    protected StringItem guidanceItem = new StringItem("",true);

    /** ガイダンス情報を設定する */
    public void setGuidance(CharSequence cs) { guidanceItem.setText(cs);}
    public void setGuidance(CharSequence cs, int sessionID) {
        guidanceItem.setText(cs, sessionID);
    }
    public void setGuidance(CharSequence cs, SessionObject session) {
        guidanceItem.setText(cs, session.getSessionID());
    }
    /** ガイダンス情報を取得する */
    public CharArray getGuidance() { return guidanceItem.getText();}
    public CharArray getGuidance(int sessionID) { return guidanceItem.getText(sessionID);}
    public CharArray getGuidance(SessionObject session) {
        return guidanceItem.getText(session.getSessionID());
    }
    protected String nodeParameter = "";
    public void setNodeParameter(CharArray ch) { nodeParameter = ch.toString(); }
    public void setNodeParameter(String str) { nodeParameter = str; }
    public String getNodeParameter() { return nodeParameter; }

    /** ページング処理の複数同時表示対応 */

    protected String pageKeyword  = "@PAGE";
    protected String countKeyword = "@COUNT";

    /** ページ数表示用のキーワード(default:@PAGE)を取得する*/
    public String getPageKeyword() { return pageKeyword;}

    /** 件数表示用のキーワード(default:@COUNT)を取得する*/
    public String getCountKeyword() { return countKeyword;}

    /** ページ数表示用のキーワード(default:@PAGE)を設定する */
    public void setPageKeyword(String keyword) { pageKeyword = keyword;}

    /** 件数表示用のキーワード(default:@COUNT)を設定する */
    public void setCountKeyword(String keyword) { countKeyword = keyword;}

    /** ページ遷移の飛び先URL */
    protected String pagingURL = "";

    /** ページ遷移の飛び先URLを設定する */
    public void setPagingURL(String url) {
if (debugPaging) System.out.println("setPagingURL("+url+")");
        if (url == null || url.trim().length()== 0) {
            this.pagingURL = "";
        } else {
            if (url.endsWith("/") || url.endsWith("&") || url.endsWith("?") || url.equals("*")) {
                // do nothing
            } else {
                if (SystemManager.convertContextPath == 1) {
                    url += "/";
                } else {
                    if (url.indexOf('?') >= 0) {
                        url += '&';
                    } else {
                        url += '?';
                    }
                }
            }
            this.pagingURL = url;
        }
if (debugPaging) System.out.print("setPagingURL("+url+")->"+this.pagingURL);
    }
    /** ページ遷移の飛び先URLを設定する */
    public String getPagingURL() { return this.pagingURL;}

    // --------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------

    /** PageServletオブジェクトを生成する
        @param pageID pageID
    */
    public PageServlet(int pageID) {
        super(pageID);
        forward_queue.enqueue(this);

        guidanceItem.setVisible(false);
        //if (SystemManager.useGuidance) {
            addItem(guidanceItem);
        //}
        //naviNode = new NaviNode(this);
    }
    // --------------------------------------------------------
    // ここからは必要に応じてオーバーライドする
    // --------------------------------------------------------

    /** 初期化
        必要に応じてオーバーライドする
    */
    public  void init() {};
   
    /**
    
    */
    public boolean forward(HttpServletRequest request,HttpServletResponse response, int count,
           Hashtable<CharArray,CharArray> hashHeader, Hashtable<String,String[]> hashParameter) {
        return false;   // true にすると、forward(SessionObject) は呼ばれない
    }
    /** リダイレクトページが存在する場合にオーバーライドする */
    public ObjectQueue forward(SessionObject session) {
        return forward_queue;
    }

//  /** パラメータ解析
//      必要に応じてオーバーライドする
//  */
//  public void analizeParameter(SessionObject session) {};

    /** アイテムの設定 表示直前に実行される
        必要に応じてオーバーライドする
    */
    public void makePage(SessionObject session) {};

    /** ページの表示後に実行される
        必要に応じてオーバーライドする
    */
    public void afterPage(SessionObject session) {};


    /** シャットダウン時の処理
        必要に応じてオーバーライドする
    */
    public  void destroy() {};

    /** ItemEventListener インターフェース **/
    public CharArray execute(SessionObject session, CharArray ch,
                             QueueTable queue) {
       ch.add("イベントが発生しました");
        return ch;
    }
    /** DataAccess インターフェース
        @deprecated
    */
    public boolean rsExecute(SessionObject session, ResultSet rs, Object o)
            throws SQLException {
        return false;
    }
    /** DataAccess インターフェース
        @param session      セッション
        @param table        検索結果テーブル
        @param start        表示開始行のヒント
        @param size         １画面の表示行数のヒント
        @param cursor_end   全件検索済みか？
        @param o            ユーザー定義オブジェクト
        @return true で成功
    */
    public boolean execute(SessionObject session, QueueTable table,
            int start, int size, boolean cursor_end, Object o) {
          return false;
    }
    /**
        テンプレートを設定する
    */
    public void setTemplate(SessionObject session) {}

    // --------------------------------------------------------
    // ここまで（必要に応じてオーバーライド）
    // --------------------------------------------------------

    //-----------------------------------------
    /** メール用のURLを取得する
        @param session セッション
        @param ch バッファ
        @return メールURL
    */
    public CharArray getMailURL(SessionObject session, CharArray ch) {
        ModuleManager mm = getModule().getModuleManager();
        if (mm == null) return ch;
        SiteMapping map = mm.getSiteMapping();
        String ss = mm.hostURL;
        if (ss != null && ss.length() > 0) {
            ch.set(ss);
        } else {
            ch.set(com.miraidesign.system.SystemManager.hostURL);
        }
        if (SystemManager.convertContextPath == 1) {
            // do nothing
        } else {
            ch.add(com.miraidesign.system.SystemManager.servletPath);
        }
        map.getSiteParameter(ch);
        map.getPageParameter(ch, getModule().getModuleID()*1000+getPageID());
        if (SystemManager.convertContextPath == 1) {
            ch.add('/');
        } else {
            ch.add('&');
        }
        if (SystemConst.cryptSessionID) {
            ch.add(SystemConst.sessionIDKey[1]);
            ch.add('=');
            ch.add(Crypt62.encode(session.getSessionID()));
        } else {
            ch.add(SystemConst.sessionIDKey[0]);
            ch.add('=');
            ch.format(session.getSessionID());
        }
        if (SystemManager.convertContextPath == 1) {
            ch.add('/');
        } else {
            ch.add('&');
        }
        ch.add("st=load");
       return ch;
    }
    public CharArray getMailURL(SessionObject session) {
        return getMailURL(session,new CharArray());
    }
    /** メール用のURLを取得する（初期モード)
        @return メールURL
    */
    public CharArray getMailURL() {
        return getMailURL(new CharArray(),(CharArray)null);
    }
    /** メール用のURLを取得する（初期モード)
        @param param  追加パラメータ
        @return メールURL
    */
    public CharArray getMailURL(String param) {
        return getMailURL(new CharArray(),param);
    }
    public CharArray getMailURL(CharArray param) {
        return getMailURL(new CharArray(),param);
    }
    public CharArray getMailURL(CharArray ch, String param) {
        ModuleManager mm = getModule().getModuleManager();
        if (mm == null) return ch;

        String ss = mm.hostURL;
        if (ss != null && ss.length() > 0) {
            ch.set(ss);
        } else {
            ch.set(com.miraidesign.system.SystemManager.hostURL);
        }
        if (SystemManager.convertContextPath == 1) {
            // do nothing
        } else {
            ch.add(com.miraidesign.system.SystemManager.servletPath);
        }
        SiteMapping map = mm.getSiteMapping();

        map.getSiteParameter(ch);

        map.getPageParameter(ch, getModule().getModuleID()*1000+getPageID());

        if (param != null) {
            if (SystemManager.convertContextPath == 1) {
                ch.add('/');
            } else {
                ch.add('&');
            }
            ch.add(param);
        }
       return ch;
    }
    public CharArray getMailURL(CharArray ch, CharArray param) {
        ModuleManager mm = getModule().getModuleManager();
        if (mm == null) return ch;
        String ss = mm.hostURL;
        SiteMapping map = mm.getSiteMapping();
        if (ss != null && ss.length() > 0) {
            ch.set(ss);
        } else {
            ch.set(com.miraidesign.system.SystemManager.hostURL);
        }
        if (SystemManager.convertContextPath == 1) {
            // do nothing
        } else {
            ch.add(com.miraidesign.system.SystemManager.servletPath);
        }
        map.getSiteParameter(ch);
        map.getPageParameter(ch, getModule().getModuleID()*1000+getPageID());
        if (param != null) {
            if (SystemManager.convertContextPath == 1) {
                ch.add('/');
            } else {
                ch.add('&');
            }
            ch.add(param);
        }
       return ch;
    }
    /** そのページのセッション付きURLを取得する
         ただし、siteMapping可能かつcookieが有効なときはセッションは出力しない
    */
    public CharArray getURL(SessionObject session) {
        return getURL(session, false);
    }
    /** そのページのセッション付きURLをに出力する
         @param mode true:セッションIDを強制出力
    */
    public CharArray getURL(SessionObject session, boolean mode) {
        int sessionID = session.getSessionID();
        CharArray ch = new CharArray();
        ModuleManager mm = getModule().getModuleManager();
        if (mm == null) return ch;
        SiteMapping map = mm.getSiteMapping();
        if (SystemManager.convertContextPath == 1) {
            map.getSiteParameter(session, ch);  //@@// 20100622 指定する
        } else {
            ch.add(com.miraidesign.system.SystemManager.servletPath);
        }
        map.getPageParameter(ch, getModule().getModuleID()*1000+getPageID());

        boolean useCookie = false;
        if (SystemManager.convertContextPath == 1 && !mode
            && session != null && session.request != null) {
            useCookie = (session.request.getCookies() != null);
        }


        int parser_mode = CharArray.getInt(session.getUserData().get("parser_mode"));
        if (useCookie || session.isRobot() || parser_mode == ContentParser.PUBLISH) {
            // do nothing
        } else {
            if (SystemManager.convertContextPath == 1) {
                ch.add('/');
            } else {
                ch.add(session.isPC()? "&" : "&amp;");
            }
            if (SystemConst.cryptSessionID) {
                ch.add(SystemConst.sessionIDKey[1]); ch.add('=');
                ch.add(Crypt62.encode(sessionID));
            } else {
                ch.add(SystemConst.sessionIDKey[0]); ch.add('=');
                ch.format(sessionID);
            }
        }
        if (session.isAppendAuthID()) {
            if (ch.indexOf("/"+SystemConst.authIDKey+"=") < 0) {
                if (!ch.endsWith("/")) ch.add("/");
                ch.add(session.getAuthString());
            }
        }
        return ch;
    }
    /** そのページの入り口のURLを取得する（サイト名、サイトIDつき、セッションなし） */
    public CharArray getURL() {
        CharArray ch = new CharArray();
        ModuleManager mm = getModule().getModuleManager();
        if (mm == null) return ch;

        SiteMapping map = mm.getSiteMapping();
        String ss = mm.httpURL;
        if (ss != null && ss.length() > 0) {
            ch.set(ss);
        } else {
            ch.set(com.miraidesign.system.SystemManager.httpURL);
        }
        if (SystemManager.convertContextPath == 1) {
            // do nothing
        } else {
            ch.add(com.miraidesign.system.SystemManager.servletPath);
        }
        map.getSiteParameter(ch);
        map.getPageParameter(ch, getModule().getModuleID()*1000+getPageID());
        return ch;
    }
    //-----------------------------------------------------------
    // リスト表示関連
    //-----------------------------------------------------------
    private int searchMax   = -1;
    private int searchNext  = -1;
    private int pageDisplay = -1;
    private int pageMax    = -1;   // リスト表示件数のヒント
    private int pageWidth = -1;   // ページング表示幅

    private int searchMaxMobile   = -1;
    private int searchNextMobile  = -1;
    private int pageDisplayMobile = -1;
    private int pageMaxMobile    = -1;   // リスト表示件数のヒント
    private int pageWidthMobile = -1;   // ページング表示幅

    public void setSearchMax(int max) {
        this.searchMax = max;
    }
    public void setSearchNext(boolean mode) {
        this.searchNext = mode ? 1 : 0;
    }
    public void setPageDisplay(boolean mode) {
        this.pageDisplay = mode ? 1 : 0;
    }
    public void setPageMax(int max) {
        this.pageMax = max;
    }
    /** ページング表示幅を設定する */
    public void setPageWidth(int width) {
        this.pageWidth = width;
    }

    /** ページング表示幅を設定する(モバイル用) */
    public void setSearchMaxMobile(int max) {
        this.searchMaxMobile = max;
    }
    public void setSearchNextMobile(boolean mode) {
        this.searchNextMobile = mode ? 1 : 0;
    }
    public void setPageDisplayMobile(boolean mode) {
        this.pageDisplayMobile = mode ? 1 : 0;
    }
    public void setPageMaxMobile(int max) {
        this.pageMaxMobile = max;
    }
    public void setPageWidthMobile(int width) {
        this.pageWidthMobile = width;
    }

    /** 最大検索数のヒントを返す*/ // 1000件とか
    public int getSearchMax() {
        if (searchMax >= 0) {
            return searchMax;
        } else {
            ModuleManager mm = getModule().getModuleManager();
            if (mm == null) return 30;
            return mm.getSearchMax();
        }
    }
    public int getSearchMaxMobile() {
        if (searchMaxMobile >= 0) {
            return searchMaxMobile;
        } else {
            ModuleManager mm = getModule().getModuleManager();
            if (mm == null) return 10;
            return mm.getSearchMaxMobile();
        }
    }
    public int getSearchMax(SessionObject session) {
        return session.isPC() ? getSearchMax() : getSearchMaxMobile();
    }
    /** データが存在する時にさらに次ページ検索を行うか？*/
    public boolean getSearchNext() {
        if (searchNext >= 0) {
            return (searchNext != 0);
        } else {
            ModuleManager mm = getModule().getModuleManager();
            if (mm == null) return false;
            return mm.getSearchNext();
        }
    }
    public boolean getSearchNextMobile() {
        if (searchNextMobile >= 0) {
            return (searchNextMobile != 0);
        } else {
            ModuleManager mm = getModule().getModuleManager();
            if (mm == null) return false;
            return mm.getSearchNextMobile();
        }
    }
    public boolean getSearchNext(SessionObject session) {
        return session.isPC() ? getSearchNext() : getSearchNextMobile();
    }
    /** 検索結果にページを表示するか？*/
    public boolean getPageDisplay() {
        if (pageDisplay >= 0) {
            return (pageDisplay != 0);
        } else {
            ModuleManager mm = getModule().getModuleManager();
            if (mm == null) return false;
            return mm.getPageDisplay();
       }
    }
    public boolean getPageDisplayMobile() {
        if (pageDisplayMobile >= 0) {
            return (pageDisplayMobile != 0);
        } else {
            ModuleManager mm = getModule().getModuleManager();
            if (mm == null) return false;
            return mm.getPageDisplayMobile();
       }
    }
    public boolean getPageDisplay(SessionObject session) {
        return session.isPC() ? getPageDisplay() : getPageDisplayMobile();
    }

    /** リスト表示件数のヒントを返す*/ // 30行とか
    public int getPageMax() {
        if (pageMax >= 0) {
            return pageMax;
        } else {
            ModuleManager mm = getModule().getModuleManager();
            if (mm == null) return 10;
            return mm.getPageMax();
        }
    }
    public int getPageMaxMobile() {
        if (pageMaxMobile >= 0) {
            return pageMaxMobile;
        } else {
            ModuleManager mm = getModule().getModuleManager();
            if (mm == null) return 5;
            return mm.getPageMaxMobile();
        }
    }
    public int getPageMax(SessionObject session) {
        int _max = session.isPC() ? getPageMax() : getPageMaxMobile();
        return (_max > 0)? _max : session.getPageMax();
    }

    /** ページング表示幅を返す*/ // <prev|1|..|5|6|7|..|9|next>
    public int getPageWidth() {
        if (pageWidth >= 0) {
            return pageWidth;
        } else {
            ModuleManager mm = getModule().getModuleManager();
            if (mm == null) return 5;
            return mm.getPageWidth();
        }
    }
    public int getPageWidthMobile() {
        if (pageWidthMobile >= 0) {
            return pageWidthMobile;
        } else {
            ModuleManager mm = getModule().getModuleManager();
            if (mm == null) return 5;
            return mm.getPageWidthMobile();
        }
    }

    public int getPageWidth(SessionObject session) {
        return session.isPC() ? getPageWidth() : getPageWidthMobile();
    }

    //-----------------------------------------------------------
    // ページ分割の仕組み
    //-----------------------------------------------------------
    private String szPrev = "前ページ";   // モバイル用
    private String szNext = "次ページ";

    public void setPrevTitle(String str) {
        szPrev = str;
        if (prevItem != null) prevItem.setText(szPrev);
    }
    public void setNextTitle(String str) {
        szNext = str;
        if (nextItem != null) nextItem.setText(szNext);
    }
    public String getPrevTitle() { return szPrev;}
    public String getNextTitle() { return szNext;}

    private FormContainer form;
    private SubmitItem prevItem;
    private SubmitItem nextItem;
    private StringItem pageItem;
    private boolean useMyForm = false;

    /**
        ページ数を表示するアイテムを追加する
    */
    public void addPageItem() {
        if (getPageDisplay()) {
            if (pageItem == null) pageItem = new StringItem("page.",true);
            addItem(pageItem);
        }
    }

    /**
        ページ分割用のアイテムを自動追加する(Formは自動生成)
        init()で呼ぶ
    */
    public void addSplitItem() {
        if (form == null) {
            useMyForm = true;
            form = new FormContainer("",this);
        }
        if (prevItem == null) prevItem = new SubmitItem(szPrev);
        if (nextItem == null) nextItem = new SubmitItem(szNext);
        addItem(form);
        form.addItem(prevItem);
        form.addItem(nextItem);
    }
    /**
        ページ分割用のアイテムを自動追加する
        init()で呼ぶ
    */
    public void addSplitItem(FormContainer form) {
        useMyForm = false;
        this.form = form;
        if (prevItem == null) prevItem = new SubmitItem("前ページ");
        if (nextItem == null) nextItem = new SubmitItem("次ページ");
        form.addItem(prevItem);
        form.addItem(nextItem);
    }

    public int getPageStart(SessionObject session) {
        int pageStart = 0;
        if (session.clickedItemData != null) {
            try {
                pageStart = ((SubmitData)session.clickedItemData).getElement().getInt(0);
            } catch (Exception ex) {
                System.out.println("PageServlet getPageStart illegal items");
            }
        } else {
            String str = null;
            int hash = CharArray.getInt(session.getParameter("page@hash")); // InputModuleからのみセットされている
            if (hash > 0 && hash == hashCode()) {    // CMSからの呼び出し時のみセットする
                str = session.getParameter(""+hash+"_start");
            }
            if (str == null || str.length()==0) {
                if (startKey.length() > 0) {
                    str = session.getParameter(startKey);
                } else {
                    str = session.getParameter("start");
                }
            }
            if (str != null) {
                pageStart = CharArray.getInt(str);
            }
        }
        if (debug) System.out.println("pageStart:"+pageStart);
        return pageStart;
    }

    public boolean showButton(boolean prevFlag, boolean nextFlag,
                           int start, int size, int sessionID) {
        boolean sts = false;
        if (form != null) {
            if (useMyForm) form.setVisible(prevFlag || nextFlag, sessionID);

            prevItem.setVisible(prevFlag, sessionID);
            nextItem.setVisible(nextFlag, sessionID);

            if (prevFlag) {
                QueueElement element = prevItem.getElement(sessionID);
                element.clear();
                element.add(Math.max(start-size,0));
            }
            if (nextFlag) {
                QueueElement element = nextItem.getElement(sessionID);
                element.clear();
                element.add(start+size);
            }
            sts = true;
        }

        return sts;
    }

    // InputModuleで取得する
    String szPrevPage = "≪前ページ";
    String szNextPage = "次ページ≫";
    String szSeparatePage = "|";
    public void setPrevPageStr(String str)  { szPrevPage = str;}
    public void settNextPageStr(String str) { szNextPage = str;}

    public String getPrevPageStr()     { return szPrevPage;}
    public String getNextPageStr()     { return szNextPage;}
    public String getSeparatePageStr() { return szSeparatePage;}

    String szAddingPageURL = "";        // 追加URL
    public void setAddingPageURL(String str) {
        szAddingPageURL = str;
    }

    String startKey = "start";   // 表示開始位置パラメータ
    public void setStartKey(String str) { startKey = str; }

    String tmKey = SystemConst.tmKey;
    public void setTmKey(String str) {
        tmKey=str;
    }

    /** ページ表示、ボタン表示用のパラメータを設定する */
    public boolean setPageValue(boolean prevFlag, boolean nextFlag,
                           int start, int size, int count, boolean cursor_end,
                           SessionObject session, String tm) {
        boolean sts = false;
        int sessionID = session.getSessionID();
        int _hash = hashCode();                               // CMS I/F

        session.setTemplateMode(pageKeyword, Queue.LOOP);
        session.setTemplateMode(countKeyword, Queue.LOOP);

        String szSpace="";
        //if (session.isPC()) szSpace=" "; // 2010-05-10 やめる
        int cache = CharArray.getInt(session.getParameter("cache@hash")); // InputModuleからのみセットされている
        if (cache > 0 && cache == _hash) return true; // InputModuleでは１回しか呼ばない

        int _max = CharArray.getInt(session.getParameter(""+_hash+"_size"));     // CMS I/F
        String szStart = (_max > 0) ? ""+_hash+"_start=" : startKey+"=";
        
        if (startKey.length() > 0) szStart = startKey+"=";
        
if (debugPaging) {
    System.out.println("▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼"+cache);
    System.out.println("setPageValue:szStart(hash:"+_hash+"):"+szStart+" max="+_max);
}

        boolean addURI = false;
        ModuleManager mm = session.getModuleManager();
        if (mm != null) addURI = mm.ini.getBoolean(mm.getSection(),"AddURI2Page");

        if (form != null) {
            if (useMyForm) form.setVisible(prevFlag || nextFlag, sessionID);

            prevItem.setVisible(prevFlag, sessionID);
            nextItem.setVisible(nextFlag, sessionID);

            if (prevFlag) {
                QueueElement element = prevItem.getElement(sessionID);
                element.clear();
                element.add(Math.max(start-size,0));
            }
            if (nextFlag) {
                QueueElement element = nextItem.getElement(sessionID);
                element.clear();
                element.add(start+size);
            }
            sts = true;
        }
        if (pageItem != null) {
            CharArray ch = CharArray.pop();    // テンポラリバッファ
            //ch.set("&lt;page.");
            ch.format(start/size + 1);
            ch.add('/');
            ch.format((count-1)/size+1);
            //ch.add("&gt; Hit.");
            ch.add(" 全");
            ch.format(count);
            ch.add("件");
            if (!cursor_end && count>0) ch.add('+');
            pageItem.setText(ch,sessionID);
            CharArray.push(ch);
        }
        //if (parser != null) { 
            int current = start/size + 1;
            int total   = (count-1)/size+1;     // 総ページ数

if (debugPaging) System.out.println("★★★ start="+start+" size="+size+" current="+current+" total="+total);

            session.clearTemplate(pageKeyword);     // @PAGE
            if (count == 0) {
                session.clearTemplate(countKeyword);    // @COUNT
            } else {
                Parameter p = new Parameter();
                p.add(count);
                p.add((!cursor_end && count>0) ? "+" : "");
                p.add(current);
                p.add(total);

                session.setTemplate(countKeyword,p);    // @COUNT
                if (_max > 0) { // CMS I/F
                    session.setParameter(""+_hash+"_count",""+count);
                    session.setParameter(""+_hash+"_current",""+current);
                    session.setParameter(""+_hash+"_total",""+total);
                    session.setParameter(""+_hash+"_next",(!cursor_end && count>0) ? "+" : "");
                }
            }

            DynamicData dd = getDynamicData(sessionID);  // ページに存在するDynamicDataを取得
            if (dd != null) dd.setSessionObject(session);
            //if (dd == null) addItem(new DynamicItem());
            if (total > 1 && dd != null) {
                String orgURL = getPagingURL();
                if (_max > 0) {
                    CharArray chPagingURL = CharArray.pop();;
                    CharToken tokenURI = session.getDirectoryListFromURI();
if (debugPaging) tokenURI.dumpQueue("<A>");
                    for (int i = 0; i < tokenURI.size(); i++) {
                        CharArray dir = tokenURI.get(i);
                        if (dir.length() > 0 &&
                            !dir.startsWith(_hash+"_tm=") && !dir.startsWith(_hash+"_start=") &&
                            !dir.startsWith("S=") &&                  // セッションをつけすぎないように
                            (chPagingURL.indexOf("/"+dir+"/") < 0) && // 同名URLをはじく
                            !chPagingURL.endsWith("/"+dir))           // 同名URLをはじく
                        {
                            chPagingURL.add('/');
                            //if (dir.isAscii()) {
                                chPagingURL.add(dir);
                            //} else {
                            //    chPagingURL.add(dir.URLEncode("UTF-8"));
                            //}
                        }
if (debugPaging) System.out.println("★token("+(i+1)+"/"+tokenURI.size()+")☆dir:"+dir+"  URL:"+chPagingURL);
                    }
                    // 検索FORM情報が含まれる場合の処理
                    CharArray servletPath = CharArray.pop(SystemManager.servletPath);
                    servletPath.trim();
                    if (!servletPath.startsWith("/")) servletPath.insert("/");
                    if (mm != null && chPagingURL.indexOf(servletPath)>=0) {  // サーブレットパスが含まれている
                        CharArray tmp = CharArray.pop();
                        SiteMapping map = mm.getSiteMapping();
                        map.getSiteParameter(session, tmp);
                        map.getPageParameter(tmp, getModule().getModuleID()*1000+getPageID());

                        if (!tmp.startsWith("/")) tmp.insert("/");
                        chPagingURL.replaceWord(servletPath,tmp);
                        CharArray.push(tmp);
                    }
                    CharArray.push(servletPath);
                    String sz = chPagingURL.toString();
                    CharArray.push(chPagingURL);
                    setPagingURL(sz); 
if (debugPaging) System.out.println("★pagingURL1:"+sz);
                } else if (addURI) {
                    CharArray chPagingURL = CharArray.pop();;
                    CharToken tokenURI = session.getDirectoryListFromURI();
if (debugPaging) tokenURI.dumpQueue("<B>");
                    for (int i = 0; i < tokenURI.size(); i++) {
                        CharArray dir = tokenURI.get(i);
                        if (dir.length() > 0 &&
                            !dir.startsWith(tmKey+"=") && !dir.startsWith(startKey+"=") &&
                            !dir.startsWith("S=") && !dir.startsWith("at=") && // セッションをつけすぎないように
                            (chPagingURL.indexOf("/"+dir+"/") < 0) && // 同名URLをはじく
                            !chPagingURL.endsWith("/"+dir))           // 同名URLをはじく
                        {
                            chPagingURL.add('/');
                            chPagingURL.add(dir);
                        }
if (debugPaging) System.out.println("★dir:"+dir+"  URL:"+chPagingURL);
                    }
                    // 検索FORM情報が含まれる場合の処理
                    CharArray servletPath = CharArray.pop(SystemManager.servletPath);
                    servletPath.trim();
                    if (!servletPath.startsWith("/")) servletPath.insert("/");
                    if (mm != null && chPagingURL.indexOf(servletPath)>=0) {  // サーブレットパスが含まれている
                        CharArray tmp = CharArray.pop();
                        SiteMapping map = mm.getSiteMapping();
                        map.getSiteParameter(session, tmp);
                        map.getPageParameter(tmp, getModule().getModuleID()*1000+getPageID());

                        if (!tmp.startsWith("/")) tmp.insert("/");
                        chPagingURL.replaceWord(servletPath,tmp);
                        CharArray.push(tmp);
                    }
                    CharArray.push(servletPath);
                    String sz = chPagingURL.toString();
                    CharArray.push(chPagingURL);
                    setPagingURL(sz);
if (debugPaging) System.out.println("★PagingURL:"+sz);
                }
                CharArray msg = ((ModuleServlet)getModule()).getMessage(session,"PrevPage");
                if (msg != null && msg.trim().length() > 0) {
                    szPrevPage = msg.toString();
                }
                msg = ((ModuleServlet)getModule()).getMessage(session,"NextPage");
                if (msg != null && msg.trim().length() > 0) {
                    szNextPage = msg.toString();
                }
                msg = ((ModuleServlet)getModule()).getMessage(session,"SeparatePage");
                if (msg != null && msg.trim().length() > 0) {
                    szSeparatePage = msg.toString();
                }
                int disp_max = getPageWidth(session);
                CharArray ch = new CharArray();
                if (prevFlag) {
                    int i = Math.max(0,start-size);
                    AnchorStringData anchor = getAnchorStringData(szStart+i+((addTM && !session.isRobot()) ? "&"+tmKey+"="+tm : ""),szPrevPage);
                    anchor.setAccessKey('*');
                    dd.add(anchor);
                    anchor.getTag(ch);
                } else {
                    ch.add(szPrevPage);
                }
                int _start = 1;
                int _end   = total;
                if (disp_max > 1 && disp_max < total) {
                    int _half = disp_max / 2;
                    if (_half >= current) {
                        _end = disp_max;
                    } else if (current + _half > total) {
                        _start = total - disp_max;
                        _end   = total;
                    } else {
                        _start = Math.max(1, current - _half);
                        _end   = Math.min(total, current + _half);
                    }
                } else if (disp_max <= 1) {  // モバイル用 2010.06.24
                    _start = _end = current;
                }
                if (_start > 1) {
                    int i = 1;
                    ch.add(szSpace);
                    ch.add("|");
                    ch.add(szSpace);
                    if (i == current) {
                        ch.add("<b>");
                        ch.format(i);
                        ch.add("</b>");
                    } else {
                        AnchorStringData anchor = getAnchorStringData(szStart+(size*i-size)+((addTM && !session.isRobot()) ? "&"+tmKey+"="+tm : ""),""+i);
                        anchor.setVisible(false);   // モバイル画面で無条件に表示されるのを避けるため
                        dd.add(anchor);
                        anchor.getTag(ch);       // DynamicData に addしてから取り出す事
                    }
                    if (_start == 2) {
                        ch.add(szSpace); ch.add(szSeparatePage); ch.add(szSpace);
                    } else {
                        ch.add(szSpace); ch.add(szSeparatePage); ch.add('…'); ch.add(szSeparatePage); ch.add(szSpace);
                    }
                }
                for (int i = _start; i <= _end; i++) {
                    if (i == _start && _start > 1) {
                    } else {
                        ch.add(szSpace); ch.add(szSeparatePage); ch.add(szSpace);
                    }
                    if (i == current) {
                        ch.add("<b>");
                        ch.format(i);
                        ch.add("</b>");
                    } else {
                        AnchorStringData anchor = getAnchorStringData(szStart+(size*i-size)+((addTM && !session.isRobot()) ? "&"+tmKey+"="+tm : ""),""+i);
                        anchor.setVisible(false);   // モバイル画面で無条件に表示されるのを避けるため
                        dd.add(anchor);
                        anchor.getTag(ch);          // DynamicData に addしてから取り出す事
                    }
                } // next

                if (_end != total) {
                    int i = total;
                    if (_end == total-1) {
                        ch.add(szSpace); ch.add(szSeparatePage); ch.add(szSpace);
                    } else {
                        ch.add(szSpace); ch.add(szSeparatePage); ch.add('…'); ch.add(szSeparatePage); ch.add(szSpace);
                    }
                    if (i == current) {
                        ch.add("<b>");
                        ch.format(i);
                        ch.add("</b>");
                    } else {
                        AnchorStringData anchor = getAnchorStringData(szStart+(size*i-size)+((addTM && !session.isRobot()) ? "&"+tmKey+"="+tm : ""),""+i);
                        anchor.setVisible(false);   // モバイル画面で無条件に表示されるのを避けるため
                        dd.add(anchor);
                        anchor.getTag(ch);          // DynamicData に addしてから取り出す事
                    }
                }

                ch.add(szSpace); ch.add(szSeparatePage); ch.add(szSpace);
                if (nextFlag) {
                    AnchorStringData anchor = getAnchorStringData(szStart+(start+size)+((addTM && !session.isRobot()) ? "&"+tmKey+"="+tm : ""),szNextPage);
                    anchor.setAccessKey('#');
                    anchor.setVisible(false);   // モバイル画面で無条件に表示されるのを避けるため
                    dd.add(anchor);
                    anchor.getTag(ch);          // DynamicData に addしてから取り出す事
                } else {
                    ch.add(szNextPage);
                }
                
                session.setTemplateMode(pageKeyword, Queue.LOOP);
                session.setTemplate(pageKeyword,new Parameter().add(ch));
if (debugPaging) System.out.println("pageKeywordx:"+pageKeyword);
if (debugPaging) System.out.println("  _max:"+_max);
if (debugPaging) System.out.println("  _page:"+ch);
                if (_max > 0) { // CMS I/F
                    //ch.replace("/start=","/"+_hash+"_start=");
                    ch.replace("/tm=","/"+_hash+"_tm=");
if (debugPaging) System.out.println("  _page:"+ch);
                    session.setParameter(""+_hash+"_page",""+ch);
if (debugPaging) System.out.println(" orgURL:"+orgURL);

                    setPagingURL(orgURL);
                }
            }
        //}//
if (debugPaging) {
    System.out.println("setPageValue:"+sts);
    System.out.println("▲△▲△▲△▲△▲△▲△▲△▲△▲△▲△▲");
}

        return sts;
    }

    private AnchorStringData getAnchorStringData(String url, String str) {
if (debugPaging) System.out.println("-(a)-url:"+url+"   str:"+str);
        AnchorStringData anchor = new AnchorStringData(url , str);
        if (pagingURL.length() == 0) { // 通常URL
            anchor.setJumpPage(this);
        } else {
            anchor.setTopPage(pagingURL);
        }
        return anchor;
    }

    /** ナビゲーション情報の設定 */
    public void setNavigation(SessionObject session) {
        //NaviNode
        session.page = this;
        session.node = null;
        if (nodeQueue.size() == 0) {
if (debug) System.out.println("★★setNavigation nodeQueue がない！");
            return;
        }
if (debug) System.out.println("★★setNavigation nodeQueue size:"+nodeQueue.size());
        NaviNode node = null;
        String szNode = session.getParameter(SystemConst.nodeKey);
if (debug) System.out.println("szNode["+szNode+"]");
        if (szNode != null && szNode.trim().length()>0) {
            int id = CharArray.getInt(szNode);
            for (int i = 0; i < nodeQueue.size(); i++) {
                NaviNode nn = (NaviNode)nodeQueue.peek(i);
                if (nn.getID() == id) {
if (debug) System.out.println("★★setNavigation found:"+i+" id:"+id);
                    node = nn;
                    break;
                }
            }
        } else {
            String szParentNode = session.getParameter(SystemConst.parentNodeKey);
if (debug) System.out.println("szParentNode["+szParentNode+"]");
            if (szParentNode != null && szParentNode.trim().length()>0) {
                int id = CharArray.getInt(szParentNode);
                for (int i = 0; i < nodeQueue.size(); i++) {
                    NaviNode nn = (NaviNode)nodeQueue.peek(i);
                    NaviNode parent = (NaviNode)nn.getParent();

if (debug) {
      if (parent == null)  System.out.println("    ["+i+"]★★ parent = null nodeID:"+nn.getID());
      else                System.out.println("    ["+i+"]★★ parent id:"+parent.getID()+" nodeID:"+nn.getID());
}
                    if (parent != null && parent.getID() == id) {
if (debug) System.out.println("★★setNavigation parent found:"+i+" parent_id:"+id+" id:"+nn.getID());
                        node = nn;
                        break;
                    }
                }
            }
        }
        if (node == null) {
if (debug) System.out.println("★not found ?");
            node = (NaviNode)nodeQueue.peek();
if (debug) System.out.println("★nod count="+nodeQueue.size()+" node="+(node != null));
        }
        session.node = node;
        if (node == null) return;
if (debug) System.out.println("★★setNavigation node-ID="+node.getID()+" "+node.getTitle());
    }

    /** ナビゲーションテンプレートの生成 */
    private void showNavigation(SessionObject session) {
        //NaviNode
        session.clearTemplate("@NAVI");
        ModuleManager mm = getModule().getModuleManager();
        if (mm == null || mm.showNavigation == false) return;
        CharArray ch = new CharArray();
        NaviNode node = session.node;
        if (node == null) {
            if (titleName.length()>0) {
                CharArray msg = ((ModuleServlet)getModule()).getMessage(session,titleName.toString());
                ch.set((msg != null && msg.length()> 0) ? msg : titleName);
            } else return;
        } else {
if (debug) System.out.println("★★showNavigation node-ID="+node.getID()+" "+node.getTitle()+" queue:"+nodeQueue.size());
            showNavigationSub(node, ch, session);   // リンク情報の表示
            CharArray org = node.getTitle();
            CharArray msg = ((ModuleServlet)getModule()).getMessage(session,org.toString());

            ch.add((msg != null && msg.length()>0) ? msg : org);    // リンクしない
        }
        session.setTemplate("@NAVI", new Parameter().add(ch));
        if (debug) System.out.println("★★★PageServlet★Navi:"+ch);
    }

    private void showNavigationSub(NaviNode node, CharArray ch, SessionObject session) {   // 再帰メソッド
        NaviNode parent = (NaviNode)node.getParent();
        if (parent != null) {
if (debug) System.out.println("★★showNavigationSub parent-ID="+parent.getID()+" "+parent.getTitle());
            showNavigationSub(parent, ch, session);   // リンク情報の表示
            if (parent.getTitle().length() > 0) {
                ch.add(parent.getTag(session));
                ModuleManager mm = getModule().getModuleManager();
                ch.add((mm != null) ? mm.naviSeparator.toString() : ">>");
            }
        }
    }
//  /** ガイダンステンプレートの生成 */
//  private void showGuidance(SessionObject session) {
//      session.clearTemplate("@GUIDE");
//      ModuleManager mm = getModule().getModuleManager();
//      if (mm == null || mm.showGuidance == false) return;
//      CharArray ch = getGuidance(session);
//      if (ch != null && ch.length() > 0) {
//          CharArray msg = ((ModuleServlet)getModule()).getMessage(session,ch.toString());
//          if (msg != null && msg.length() > 0) ch = msg;
//          session.setTemplate("@GUIDE", new Parameter().add(ch));
//      }
//      if (debug) System.out.println("★★★PageServlet★Guidance:"+ch);
//  }
//  /** タイトルイメージの生成 */
//  private void showTitleImage(SessionObject session) {
//      //session.clearTemplate("@TITLE");
//      CharArray ch = getTitleImage();
//      int mode = getTitleMode();
//      ModuleManager mm = getModule().getModuleManager();
//      if (mm != null && ch != null && ch.trim().length() > 0) {
//          if (mode == 1 || (mode == 0 && mm.showTitleImage)) {
//              session.setTemplate("@TITLE_IMAGE", new Parameter().add(ch));
//          }
//      }
//  }

    ///*
    //    テンプレートを設定する (from MDServlet)
    //*/
    //protected void setTemplate(SessionObject session) {
    //    setNavigation(session);
    //}
    /*
        テンプレートを表示する (from MDServlet)
    */
    protected void showTemplate(SessionObject session) {
        showNavigation(session);
//      showGuidance(session);
//      showTitleImage(session);
    }
    //----------------------------------------------------------------
    /** ナビゲーションノードを追加する */
    public void addNode(int mode) {
        addNode(null, titleName, mode);
    }
    public void addNode(PageServlet page) {
        addNode(page, titleName, NaviNode.ANCHOR);
    }
    public void addNode(PageServlet page, int mode) {
        addNode(page, titleName, mode);
    }
    public void addNode(PageServlet page, CharArray title, int mode) {
        if (nodeQueue.size() == 0) {
            NaviNode node = new NaviNode(this, title);       // 新規にノードを作る
            node.setMode(mode);
            nodeQueue.enqueue(node);
        }
        if (page != null) {
            // 全てのノードに対して追加する！
            for (int i = 0; i < nodeQueue.size(); i++) {
                NaviNode node = (NaviNode)nodeQueue.peek(i);
                node.setMode(mode); 
                node.add(page.getChildNode());
            }
        }
    }

    public NaviNode getChildNode() {
        NaviNode node = null;
        for (int i = 0; i < nodeQueue.size(); i++) {
            NaviNode nn = (NaviNode)nodeQueue.peek(i);
            if (nn.getParent() == null) {   // 子になる事が可能
                node = nn;
                break;
            }
        }
        if (node == null) {
            node = new NaviNode(this, titleName);       // 新規にノードを作る
            nodeQueue.enqueue(node);
            if (nodeQueue.size() > 1) {  // 他のノードをチェック
                for (int i = 0; i < nodeQueue.size(); i++) {
                    NaviNode nn = (NaviNode)nodeQueue.peek(i);
                    if (nn.getChildCount() > 0) {
                        node.copy(nn);
                        break;
                    }
                }
            }
        }
        return node;
    }

    /** ページに所属する最初のDynamicDataを取得する
        @param sessionID    セッションＩＤ
        @return DynamicData 存在しない場合は null
    */
    public DynamicData getDynamicData(int sessionID) {
        DynamicData dd = null;
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            if (item.getType() == ItemConstant.DYNAMIC) {
                dd = (DynamicData)item.getItemData(sessionID);
            } else if (item.isContainer()) {
                dd = getDDSub((ItemContainer)item,sessionID);
            }
            if (dd != null) break;
        }
        return dd;
    }
    public DynamicData getDynamicData(SessionObject session) {
        DynamicData dd = null;
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            if (item.getType() == ItemConstant.DYNAMIC) {
                dd = (DynamicData)item.getItemData(session);
            } else if (item.isContainer()) {
                dd = getDDSub((ItemContainer)item,session);
            }
            if (dd != null) break;
        }
        return dd;
    }

    private DynamicData getDDSub(ItemContainer container,int sessionID) {
        DynamicData dd = null;
        Vector vector = container.getItemTable();
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            if (item.getType() == ItemConstant.DYNAMIC) {
                dd = (DynamicData)item.getItemData(sessionID);
            } else if (item.isContainer()) {
                dd = getDDSub((ItemContainer)item,sessionID);
            }
            if (dd != null) break;
        }
        return dd;
    }

    private DynamicData getDDSub(ItemContainer container,SessionObject session) {
        DynamicData dd = null;
        Vector vector = container.getItemTable();
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            if (item.getType() == ItemConstant.DYNAMIC) {
                dd = (DynamicData)item.getItemData(session);
            } else if (item.isContainer()) {
                dd = getDDSub((ItemContainer)item,session);
            }
            if (dd != null) break;
        }
        return dd;
    }

    /**
        sendRedirectを行う<br>
        通常 forward() 内で利用する。エラー処理を行う場合はオーバーライドする事
        @param session
        @param url
        @return null を返す
     */
    public ObjectQueue redirect(SessionObject session, String url) {
        try {
if (debugRedirect) System.out.println("redirect:"+url);
            session.response.sendRedirect(url);
        } catch (Exception ex) {
            System.out.println("PageServlet#redirect(1) exception:"+ex);
            //ex.printStackTrace();
        }
        return null;
    }
    /**
        sendRedirectを行う<br>
        通常 forward() 内で利用する。エラー処理を行う場合はオーバーライドする事
        @param response
        @param url
        @return null を返す
     */
    public ObjectQueue redirect(HttpServletResponse response, String url) {
        try {
if (debugRedirect) System.out.println("redirect:"+url);
            response.sendRedirect(url);
        } catch (Exception ex) {
            System.out.println("PageServlet#redirect(2) exception:"+ex);
            //ex.printStackTrace();
        }
        return null;
    }
    /**
        sendRedirectを行う<br>
        通常 forward() 内で利用する。エラー処理を行う場合はオーバーライドする事
        @param session
        @param url
        @return null を返す
     */
    public ObjectQueue redirect(SessionObject session, CharArray url) {
        try {
if (debugRedirect) System.out.println("redirect:"+url);
            session.response.sendRedirect(url.toString());
        } catch (Exception ex) {
            System.out.println("PageServlet#redirect(3) exception:"+ex);
            //ex.printStackTrace();
        }
        return null;
    }
    /**
        sendRedirectを行う<br>
        通常 forward() 内で利用する。エラー処理を行う場合はオーバーライドする事
        @param response
        @param url
        @return null を返す
     */
    public ObjectQueue redirect(HttpServletResponse response, CharArray url) {
        try {
if (debugRedirect) System.out.println("redirect:"+url);
            response.sendRedirect(url.toString());
        } catch (Exception ex) {
            System.out.println("PageServlet#redirect(4) exception:"+ex);
            //ex.printStackTrace();
        }
        return null;
    }

    /**
        sendRedirectを行う<br>
        通常 forward() 内で利用する。エラー処理を行う場合はオーバーライドする事
        @param session
        @param page
        @return null を返す
     */
    public ObjectQueue redirect(SessionObject session, PageServlet page) {
        CharArray url = null;
        if (session != null && page != null) {
            url = page.getURL(session);
        }
        return redirect(session, url);
    }
}

//
// [end of PageServlet.java]
//

