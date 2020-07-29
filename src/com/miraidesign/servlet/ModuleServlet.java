//------------------------------------------------------------------------
//    ModuleServlet.java
//                 ModuleServlet 
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.servlet;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.UserLog;
import com.miraidesign.util.UserLogFactory;
import com.miraidesign.util.Util;
import com.miraidesign.renderer.Module;
import com.miraidesign.renderer.Page;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.session.SessionObject;

/**
 *  ModuleServlet 
 *  
 *  @version 0.5 2010-04-05
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ModuleServlet extends Module
{
    static private boolean st_debug = false; //このソースのデバッグ
    
    protected boolean debug = true;         // デバッグ表示  各モジュールが参照する
    /** デバッグモードの設定 */
    public void setDebugMode(boolean mode) { debug = mode; }
    public boolean getDebugMode() { return debug;}
    
    protected ServletLog log;                // サーブレットログ
    protected UserLog tracking;          // トラッキング
    protected UserLog operation;         // オペレーション
    
    private PageServlet defaultPageServlet;                         // デフォルトページ
    public void add(PageServlet page) { 
        if (defaultPageServlet == null) setDefaultPage(page);
        super.add((Page)page); 
    }
    
    public void setDefaultPage(PageServlet page) { defaultPageServlet = page; }
    public PageServlet getDefaultPage() { return defaultPageServlet; }

    // NaviNodeと同一
    static public final int TEXT     = 0;    // テキストで表示
    static public final int DAO      = 1;    // DAOがあればアンカーで表示
    static public final int ANCHOR   = 2;    // アンカーで表示（デフォルト）

    // ISO デバッグ用
    private int app_id = 0; // アプリケーションID
    public void setAppID(int id) { app_id = id; }
    public int getAppID() { return app_id; }

    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------
    public ModuleServlet() { }
    public ModuleServlet(int moduleID,String moduleName) {
        super(moduleID,moduleName);
    }
    //public ModuleServlet(int moduleID,String moduleName, String path) {
    //    super(moduleID,moduleName,path);
    //}
    
    //---------------------------------------------------------
    // 初期化
    //---------------------------------------------------------
    /** モジュールの初期化を行います */
    public void init() {
        init(true);
    }
    private int siteCode;
    private int channelCode;
    public void init(boolean append) /*throws ServletException*/ {
        debug &= SystemConst.debug;
        log = ServletLog.getInstance();
        tracking = UserLogFactory.getUserLog("[Tracking]");
        operation = UserLogFactory.getUserLog("[OperationLog]");
        ModuleManager mm = getModuleManager();  //
        siteCode = mm.getSiteCode();
        channelCode = mm.getChannelCode();
        
        if (append) {
            for (Enumeration e = super.getPageList(); e.hasMoreElements();) {
                PageServlet page = (PageServlet)e.nextElement();
                try {
                    page.init();
                } catch (Exception ex) {
                    System.out.println("●"+getName()+"."+page.getPageID()+" の初期化でエラーが発生しています。");
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /** シャットダウン時の処理を行う */
    public void destroy() {
        destroy(true);
    }
    public void destroy(boolean append) /*throws ServletException*/ {
        if (append) {
            for (Enumeration e = super.getPageList(); e.hasMoreElements();) {
                PageServlet page = (PageServlet)e.nextElement();
                page.destroy();
            }
        }
    }
    
    /*
    protected void tracking(String msg) {
        tracking.out(siteCode, channelCode, msg);
    }
    protected void tracking(CharArray msg) {
        tracking(msg.toString());
    }
    */
    //---------------------------------------------------------
    // パラメータ解析処理   analize
    //---------------------------------------------------------
//  protected void analizeParameter(int pageID, SessionObject sessionObject) {
//      PageServlet pageServlet = (PageServlet)super.get(pageID);
//      if (pageServlet == null) {
//          System.out.println("ModuleServlet.analizeMessage("+pageID+") が見つかりません");
//      } else {
//          System.out.println("ModuleServlet.analizeMessage("+pageID+") を表示します");
//          pageServlet.analizeParameter(sessionObject);
//      }
 // }
    public String getParameter(Hashtable<String,String[]> hash, String key) {
        String[] strs = null;
        if (hash != null) strs = (String[])hash.get(key);
        if (strs == null) return "";
        return strs[0];
    }
    
    //---------------------------------------------------------
    // ページ生成処理
    //---------------------------------------------------------
    protected void makePage(int pageID, SessionObject sessionObject) {
        PageServlet pageServlet = (PageServlet)super.get(pageID);
        if (pageServlet == null) {
            System.out.println("ModuleServlet.makePage("+pageID+") が見つかりません");
        } else {
            System.out.println("ModuleServlet.makePage("+pageID+") を表示します");
            pageServlet.makePage(sessionObject);
        }
    }
    
    //---------------------------------------------------------
    // レンダリング処理     draw
    //---------------------------------------------------------
    protected CharArray draw(SessionObject sessionObject) {    // デフォルトのページを表示する
        if (defaultPageServlet != null) {
            return draw(defaultPageServlet.getPageID(),sessionObject);
        }
        return null;        // 要チェック
    }
    protected CharArray draw(int pageID, SessionObject sessionObject) {
        PageServlet pageServlet = (PageServlet)super.get(pageID);
        if (pageServlet == null) {
            System.out.println("ModuleServlet.draw("+pageID+") が見つかりません");
            return null;        // 要チェック
        } else {
            System.out.println("ModuleServlet.draw("+pageID+") を表示します");
            return pageServlet.draw(sessionObject);
            
        }
        // return ch;
    }
    //---------------------------------------------------------
    // 追加メソッド
    //---------------------------------------------------------
    public String getSection() {
        return moduleManager.getSection();
    }
    public String getSection(String str) {
        return moduleManager.getSection(str);
    }
    /** メッセージを取得する*/
    public CharArray getMessage(String str) {
        String str2 = getName()+"."+str;
        CharArray ch = moduleManager.getMessage(str2, false);
        if (ch == null) ch = moduleManager.getMessage(str, false);
        if (ch == null && st_debug) {
            System.out.println("<>Module.getMessage["+str+"] not found");
        }
        return ch;
    }
    /** メッセージを取得する*/
    public CharArray getMessage(CharArray str) {
        String str2 = getName()+"."+str;
        CharArray ch = moduleManager.getMessage(str2, false);
        if (ch == null) ch = moduleManager.getMessage(str, false);
        if (ch == null && st_debug) {
            System.out.println("<>Module.getMessage["+str+"] not found");
        }
        return ch;
    }
    /** メッセージを取得する (@LANG対応) */
    public CharArray getMessage(SessionObject session,String str) {
        String str2 = getName()+"."+str;
        CharArray ch = moduleManager.getMessage(session, str2, false);
        if (ch == null) ch = moduleManager.getMessage(session,str, false);
        if (ch == null && st_debug) {
            System.out.println("<>Module.getMessage["+str+"] not found");
        }
        return ch;
    }
    /** メッセージを取得する (@LANG対応) */
    public CharArray getMessage(SessionObject session, CharArray str) {
        String str2 = getName()+"."+str;
        CharArray ch = moduleManager.getMessage(session, str2, false);
        if (ch == null) ch = moduleManager.getMessage(session,str, false);
        if (ch == null && st_debug) {
            System.out.println("<>Module.getMessage["+str+"] not found");
        }
        return ch;
    }
    /** 色情報を取得する*/
    public CharArray getColor(String str) {
        String str2 = getName()+"."+str;
        CharArray ch = moduleManager.getColor(str2, false);
        if (ch == null) ch = moduleManager.getColor(str, false);
        if (ch == null && st_debug) {
            System.out.println("<>Module.getColor["+str+"] not found");
        }
        return ch;
    }
    /** 色情報を取得する (@THEME対応) */
    public CharArray getColor(SessionObject session,String str) {
        String str2 = getName()+"."+str;
        CharArray ch = moduleManager.getColor(session, str2, false);
        if (ch == null) ch = moduleManager.getColor(session,str, false);
        if (ch == null && st_debug) {
            System.out.println("<>Module.getColor["+str+"] not found");
        }
        return ch;
    }
    
    /** エラーコードを取得する*/
    public CharArray getErrorCode(String str) {
        String str2 = getName()+"."+str+".Code";
        CharArray ch = moduleManager.getMessage(str2, false);
        if (ch == null) ch = moduleManager.getMessage(str+".Code", false);
        if (ch == null && st_debug) {
            System.out.println("<>Module.getMessage["+str+"] not found");
        }
        if (ch != null) {
            ch.replace("@",Util.format0(getModuleID(),3));
        }
        return ch;
    }
    
    /** エラーコードを取得する*/
    public CharArray getErrorCode(CharArray str) {
        String str2 = getName()+"."+str+".Code";
        CharArray ch = moduleManager.getMessage(str2, false);
        if (ch == null) ch = moduleManager.getMessage(str+".Code", false);
        if (ch == null && st_debug) {
            System.out.println("<>Module.getMessage["+str+"] not found");
        }
        if (ch != null) {
            ch.replace("@",Util.format0(getModuleID(),3));
        }
        return ch;
    }
    
    /**
        CSVファイルの出力
        @param session  セッション
        @param ch       出力内容
        @param filename デフォルトのファイル名
    **/
    public boolean writeCSV(SessionObject session, CharArray ch, CharArray filename) {
        //return writeCSV(session, ch, filename.toString(), "Shift_JIS", "MS932", "inline");
        return writeCSV(session, ch, filename.toString(), "UTF-8", "UTF8", "inline");
    }
    public boolean writeCSV(SessionObject session, CharArray ch, String filename) {
        //return writeCSV(session, ch, filename, "Shift_JIS", "MS932", "inline");
        return writeCSV(session, ch, filename, "UTF-8", "UTF8", "inline");
    }
    /**
        CSVファイルの出力
        @param session  セッション
        @param ch       出力内容
        @param filename デフォルトのファイル名
        @param szCharset charset デフォルト(UTF-8)
        @param szEncode  encode  デフォルト(UTF8)
        @param mode      [inline] attachment
    **/
    public boolean writeCSV(SessionObject session, CharArray ch, CharArray filename, String szCharset, String szEncode, String mode) {
        return writeCSV(session, ch, filename.toString(), szCharset, szEncode, mode);
    }
    public boolean writeCSV(SessionObject session, CharArray ch, String filename, String szCharset, String szEncode , String mode) {
        boolean rsts = false;
        try {
            if (szCharset == null || szCharset.length()==0) szCharset = "UTF-8";    //"Shift_JIS";
            if (szEncode == null || szEncode.length() == 0) szEncode  = "UTF8";     //"MS932";
            
            if (ch.indexOf("\r") < 0 && System.getProperty("line.separator").equals("\n")) {
                ch.replace("\n","\r\n");
                if (debug) System.out.println("ModuleServlet#writeTSV:改行コードを変換します");
            }
            
            session.response.setContentType("text/comma-separeted-values; charset="+szCharset);
            if (mode == null || mode.length()==0) mode="inline";
            session.response.setHeader("Content-Disposition",mode+";filename="+filename);
            java.io.OutputStream outStream = session.response.getOutputStream();
            outStream.write(ch.toString().getBytes(szEncode));
            outStream.flush();
            rsts = true;
         } catch (Exception ex) {
            ex.printStackTrace();
         }
         return rsts;
    }
    
    /**
        TSVファイルの出力
        @param session  セッション
        @param ch       出力内容
        @param filename デフォルトのファイル名
    **/
    public boolean writeTSV(SessionObject session, CharArray ch, CharArray filename) {
        //return writeTSV(session, ch, filename.toString(), "Shift_JIS", "MS932", "inline");
        return writeTSV(session, ch, filename.toString(), "UTF-9", "UTF8", "inline");
    }
    public boolean writeTSV(SessionObject session, CharArray ch, String filename) {
        //return writeTSV(session, ch, filename, "Shift_JIS", "MS932", "inline");
        return writeTSV(session, ch, filename, "UTF-8", "UTF8", "inline");
    }
    /**
        TSVファイルの出力
        @param session  セッション
        @param ch       出力内容
        @param filename デフォルトのファイル名
        @param szCharset charset デフォルト(UTF-8)
        @param szEncode  encode  デフォルト(UTF8)
        @param mode      [inline] attachment
    **/
    public boolean writeTSV(SessionObject session, CharArray ch, CharArray filename, String szCharset, String szEncode, String mode) {
        return writeTSV(session, ch, filename.toString(), szCharset, szEncode, mode);
    }
    public boolean writeTSV(SessionObject session, CharArray ch, String filename, String szCharset, String szEncode , String mode) {
        boolean rsts = false;
        try {
            if (szCharset == null || szCharset.length()==0) szCharset = "UTF-8";    //"Shift_JIS";
            if (szEncode == null || szEncode.length() == 0) szEncode  = "UTF8"; //"MS932";

            if (ch.indexOf("\r") < 0 && System.getProperty("line.separator").equals("\n")) {
                ch.replace("\n","\r\n");
                if (debug) System.out.println("ModuleServlet#writeTSV:改行コードを変換します");
            }
            session.response.setContentType("text/tab-separeted-values; charset="+szCharset);
            if (mode == null || mode.length()==0) mode="inline";
            session.response.setHeader("Content-Disposition",mode+";filename="+filename);
            OutputStream outStream = session.response.getOutputStream();
            outStream.write(ch.toString().getBytes(szEncode));
            outStream.flush();
            rsts = true;
         } catch (Exception ex) {
            ex.printStackTrace();
         }
         return rsts;
    }
    
    /**
        AxisService の取得
    */
/**  コメントアウト
    public AxisService getAxisService() {
        return moduleManager.getAxisService();
    }
**/    
    /** メールテンプレートを取得する
        @param key  キーワード
        @return テンプレートファイル
    */
    public CharArrayQueue getMailTemplate(String key) {
        return moduleManager.getMailTemplate(key);
    }
    
    /** ファイルをを取得する
        @param key  キーワード
        @return ファイル
    */
    public CharArrayQueue getFile(String key) {
        return moduleManager.getFile(key);
    }
    
}

//
// [end of ModuleServlet.java]
//

