//------------------------------------------------------------------------
//    DataAccessObject.java
//
//              Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------
//
package com.miraidesign.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.Crypt62;
import com.miraidesign.util.Parameter;
import com.miraidesign.util.Queue;
import com.miraidesign.util.QueueTable;
import com.miraidesign.util.Util;
import com.miraidesign.servlet.PageServlet;
import com.miraidesign.servlet.ServletLog;
import com.miraidesign.session.SessionObject;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.GCManager;
import com.miraidesign.system.ModuleManager;

/**
 * DataAccessObject
 *
 *  @version 0.6 
 *  @author xxxx xxxx
 *  @since  JDK1.1
**/

public class DataAccessObject {
    private boolean debug = (SystemConst.debug && true);        // デバッグ表示
    private boolean debugTable = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugCode = (SystemConst.debug && false);   // デバッグ表示
    
    private boolean debugSQL = (SystemConst.debug && true);
    private boolean debugSession = (SystemConst.debug && false);
    private boolean debugCache = (SystemConst.debug && false);
    private boolean debugSystemLock = (SystemConst.debug && false);
    private boolean debugSearch = (SystemConst.debug && false);  // false

    /** SQLのコンソールデバッグ表示モード設定
        @param mode デフォルト:true
    */
    public void debugSQL(boolean mode) {
        this.debugSQL = (SystemConst.debug && mode);
    }
    
    private TableDataAccess parent;
    private SessionObject session;
    private int site_code = 0;  // サイトチャネルコード

    /** サイトチャネルコードを設定する 
        @param n サイトチャネルコード
        @deprecated
    */
    public void setSiteCode(int n) { site_code = n; }
    /* サイトチャネルコードを設定する 
        @param n サイトチャネルコード
    */
    public void setSiteChannelCode(int n) { site_code = n; }
    /** サイトチャネルコードを取得する 
        @return サイトチャネルコード
    */
    public int getSiteChannelCode() { return site_code; }
    
    private String key;         // DataSource キーワード

    /** キーワードを取得する 
        @return キーワード
    */
    public String getKey() { return key;}
    
    private CharArray sql = new CharArray();  // 指定してあればこれを使用する
    //private String[] strs;
    private Parameter parameter = new Parameter();
    private Object object;      // ユーザー定義オブジェクト
    
    private int search_max = 0;     // 1 以上で SearchMax を上書き
    private boolean search_next = false;    // SearchNext を上書き
    private boolean search_next_flg = false;    // search_next でオーバーライドするか？
    
    private PageServlet page;
    public PageServlet getPage() { return page; }
    public SessionObject getSession() { return session; }
    
    private Exception lastException = null;
    public Exception getLastException() { return lastException;}
    
    /** コンソールデバッグ表示のオンオフ 
        @param mode デバッグフラグ
    */
    public void setDebug(boolean mode) { this.debug = mode; }
    public boolean getDebug() { return debug;}
    
    private long timestamp = 0;

    /** タイムスタンプを現時刻に設定する */
    public void setTimeStamp() { timestamp = System.currentTimeMillis();}

    /** タイムスタンプを設定する 
        @param t タイムスタンプ
    */
    public void setTimeStamp(long t) { timestamp = t;}

    /** テーブルを最初にアクセスした時のタイムスタンプを取得する
        @return タイムスタンプ
    */
    public long getTimeStamp() { return timestamp;}
    
    /** 保存時刻からの経過時間(ミリ秒)を取得する
        @return 経過時間
    */
    public long getTimeLapse() { return System.currentTimeMillis() -timestamp;}
     
    /** 保存時刻からの経過時間(ミリ秒)を取得する
        @param t 保存時刻
        @return 経過時間
    */
    public long getTimeLapse(long t) { return t -timestamp;}
    
    /** DAO特定用のキーを取得する 
        @return タイムスタンプキー文字列
    */
    public String getTimeStampKey() {
        return Crypt62.encode((int)(timestamp%13975));
    }
    
    private String tmKey = SystemConst.tmKey;
    public void setTmKey(String str) {
        tmKey=str;
    }
    
    /** デバッグ表示用 
        @param str 表示文字列
    */
    public void println(String str) {
        SessionObject.println(session,str);
    }
    
    // コネクション設定用
    private String szConnectionName = null;

    /**
        特定のコネクション名を設定する
        @param name コネクション名
    */
    public void setConnectionName(String name) {
        szConnectionName = name;
    }
    /**
        特定コネクション名を取得する
        (setConnectionName()されていなければnullを返す)
        @return コネクション名
    */
    public String getConnectionName() { return szConnectionName; }
    
    /**
        キャッシュデータが利用可能か？
        @return true: キャッシュデータが存在する
    */
    public boolean cashEnabled() {
        return cashEnabled(session);
    }
    /**
        キャッシュデータが利用可能か？
        @param session SessionObject
        @return true: キャッシュデータが存在する
    */
    public boolean cashEnabled(SessionObject session) {
        boolean sts = false;
        if (cursor_end &&                                   // データが取得完了で
            table != null && table.getTable() != null) {    // そのテーブルが存在する
            String tm = null;
            if (session != null) tm = session.getParameter(tmKey);
            if (tm == null || tm.length() == 0 || getTimeStampKey().equals(tm)) {
                sts = true;     // tmパラメータが存在しない時もOKとする
            }
        }
        return sts;
    }
    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------
    /** PageServletに依存しない コンストラクタ */
    public DataAccessObject() {
        page = null;
        parent = null;
        debug &= SystemConst.debug;
    }
    /** キャッシュは利用しないが、サイト情報は利用する
        @param mm ModuleManager
     */
    public DataAccessObject(ModuleManager mm) {
        page = null;
        parent = null;
        setModuleManager(mm);
        debug &= SystemConst.debug;
    }
    /** サイト情報を設定する 
        @param mm ModuleManager
    */
    public void setModuleManager(ModuleManager mm) {
        if (mm != null) {
            site_code = mm.getSiteChannelCode();
        }
    }
    
    /* PageServletのキャッシング利用可能なコンストラクタ */
    public DataAccessObject(PageServlet page,TableDataAccess parent) {
        this.page = page;
        this.parent = parent;
        debug &= SystemConst.debug;
    }
    /** ページをセットする。（通常は使わない事！) 
        @param page PageServlet
    */
    public void setPage(PageServlet page) {
        this.page = page;
    }
    /** TableDataAccess インターフェースを指定する<br> 
        null で呼ばれなくなる 
        @param parent TableDataAccess
    */
    public void setTableDataAccess(TableDataAccess parent) {
        this.parent = parent;
    }
    public TableDataAccess getTableDataAccess() { return parent; }
    
    // set 
    public void set(SessionObject session) {
        this.session = session;
    }
    
    /**
        DAOキーワードをセットする
        @param key      DAOキーワード
    */
    public void set(String key) {
        this.key = key;
        this.parameter.clear();
        this.object = null;
        sql.set(getSQL(key));
    }
    /**
        DAOキーワードをセットする
        @param key      DAOキーワード
    */
    public void set(CharArray key) {
        this.key = key.toString();
        this.parameter.clear();
        this.object = null;
        sql.set(getSQL(key));
    }
    
    /**
        DAOキーワードを追加する
        @param key      DAOキーワード
    */
    public void add(String key) {
        sql.add(getSQL(key));
    }
    /**
        DAOキーワードを追加する
        @param key      DAOキーワード
    */
    public void add(CharArray key) {
        sql.add(getSQL(key));
    }
    
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param strs     パラメータ集合
    */
    //public void set(String key, String[] strs) {
    //    this.key = key;
    //    parameter.clear(); parameter.add(strs);
    //    this.object = null;
    //    this.sql.set(getSQL(key));
    //}
    //public void set(CharArray key, String[] strs) {
    //    this.key = key.toString();
    //    parameter.clear(); parameter.add(strs);
    //    this.object = null;
    //    this.sql.set(getSQL(key));
    //}

    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param chqueue  パラメータ集合
    */
    public void set(String key, CharArrayQueue chqueue) {
        this.key = key;
        //this.strs = chqueue.toStringArrayToJIS();
        parameter.clear(); parameter.add(chqueue);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    public void set(CharArray key, CharArrayQueue chqueue) {
        this.key = key.toString();
        //this.strs = chqueue.toStringArrayToJIS();
        parameter.clear(); parameter.add(chqueue);
        this.object = null;
        this.sql.set(getSQL(key));
    }

    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(String key, CharArray... param) {
        this.key = key;
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(CharArray key, CharArray... param) {
        this.key = key.toString();
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(String key, String... param) {
        this.key = key;
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(CharArray key, String... param) {
        this.key = key.toString();
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(String key, int... param) {
        this.key = key;
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(CharArray key, int... param) {
        this.key = key.toString();
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(String key, long param) {
        this.key = key;
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(CharArray key, long param) {
        this.key = key.toString();
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(String key, double param) {
        this.key = key;
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(CharArray key, double param) {
        this.key = key.toString();
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(String key, boolean param) {
        this.key = key;
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列をセットする
        @param key      DAOキーワード
        @param param    初期パラメータ
    */
    public void set(CharArray key, boolean param) {
        this.key = key.toString();
        parameter.clear(); parameter.add(param);
        this.object = null;
        this.sql.set(getSQL(key));
    }

    /**
        DAOキーワードとパラメータを追加する
        @param key      DAOキーワード
        @param param    パラメータ
    */
    public void add(String key, CharArray... param) {
        sql.add(getSQL(key));
        parameter.add(param);
    }
    /**
        DAOキーワードとパラメータを追加する
        @param key      DAOキーワード
        @param param    パラメータ
    */
    public void add(CharArray key, CharArray... param) {
        sql.add(getSQL(key));
        parameter.add(param);
    }
    //public void add(String key, String... param) {
    //    sql.add(getSQL(key));
    //    for (int i = 0; i < param.length; i++) {
    //        parameter.add(param[i]);
    //    }
    //}
    //public void add(CharArray key, String... param) {
    //    sql.add(getSQL(key));
    //    for (int i = 0; i < param.length; i++) {
    //        parameter.add(param[i]);
    //    }
    //}
    //public void add(String key, int param) {
    //    sql.add(getSQL(key));
    //    parameter.add(param);
    //}
    /**
        DAOキーワードとパラメータを追加する
        @param key      DAOキーワード
        @param param    パラメータ
    */
    public void add(String key, int... param) {
        sql.add(getSQL(key));
        parameter.add(param);
    }
    //public void add(CharArray key, int param) {
    //    sql.add(getSQL(key));
    //    parameter.add(param);
    //}
    /**
        DAOキーワードとパラメータを追加する
        @param key      DAOキーワード
        @param param    パラメータ
    */
    public void add(CharArray key, int... param) {
        sql.add(getSQL(key));
        parameter.add(param);
    }
    /**
        DAOキーワードとパラメータを追加する
        @param key      DAOキーワード
        @param param    パラメータ
    */
    public void add(String key, long param) {
        sql.add(getSQL(key));
        parameter.add(param);
    }
    /**
        DAOキーワードとパラメータを追加する
        @param key      DAOキーワード
        @param param    パラメータ
    */
    public void add(CharArray key, long param) {
        sql.add(getSQL(key));
        parameter.add(param);
    }
    /**
        DAOキーワードとパラメータを追加する
        @param key      DAOキーワード
        @param param    パラメータ
    */
    public void add(String key, double param) {
        sql.add(getSQL(key));
        parameter.add(param);
    }
    /**
        DAOキーワードとパラメータを追加する
        @param key      DAOキーワード
        @param param    パラメータ
    */
    public void add(CharArray key, double param) {
        sql.add(getSQL(key));
        parameter.add(param);
    }
    /**
        DAOキーワードとパラメータを追加する
        @param key      DAOキーワード
        @param param    パラメータ
    */
    public void add(String key, boolean param) {
        sql.add(getSQL(key));
        parameter.add(param);
    }
    /**
        DAOキーワードとパラメータを追加する
        @param key      DAOキーワード
        @param param    パラメータ
    */
    public void add(CharArray key, boolean param) {
        sql.add(getSQL(key));
        parameter.add(param);
    }
    /**
        DAOキーワードと複数のパラメータを追加する
        @param key      DAOキーワード
        @param params   パラメータの集合
    */
    public void add(CharArray key, CharArrayQueue params) {
        sql.add(getSQL(key));
        parameter.add(params);
    }
    /**
        DAOキーワードと複数のパラメータを追加する
        @param key      DAOキーワード
        @param params   パラメータの集合
    */
    public void add(CharArray key, String... params) {
        sql.add(getSQL(key));
        parameter.add(params);
    }
    /**
        DAOキーワードと複数のパラメータを追加する
        @param key      DAOキーワード
        @param params   パラメータの集合
    */
    public void add(String key, CharArrayQueue params) {
        sql.add(getSQL(key));
        parameter.add(params);
    }
    /**
        DAOキーワードと複数のパラメータを追加する
        @param key      DAOキーワード
        @param params   パラメータの集合
    */
    public void add(String key, String... params) {
        sql.add(getSQL(key));
        parameter.add(params);
    }
    
    /**
        DAOキーワードとSQL挿入文字列、ユーザー定義オブジェクトをセットする<br>
        ユーザー定義オブジェクトは PageServlet#execute() 内で必要に応じて利用する
        @param key      DAOキーワード
        @param strs     パラメータ
        @param o        ユーザー定義オブジェクト
    */
    public void set(String key, String[] strs, Object o) {
        this.key = key;
        parameter.clear(); parameter.add(strs);
        this.object = o;
        this.sql.set(getSQL(key));
    }
    /**
        DAOキーワードとSQL挿入文字列、ユーザー定義オブジェクトをセットする<br>
        ユーザー定義オブジェクトは PageServlet#execute() 内で必要に応じて利用する
        @param key      DAOキーワード
        @param strs     パラメータ
        @param o        ユーザー定義オブジェクト
    */
    public void set(CharArray key, String[] strs, Object o) {
        this.key = key.toString();
        parameter.clear(); parameter.add(strs);
        this.object = o;
        this.sql.set(getSQL(key));
    }

    /**
        DAOキーワードとSQL挿入文字列、ユーザー定義オブジェクトをセットする<br>
        ユーザー定義オブジェクトは PageServlet#execute() 内で必要に応じて利用する
        @param key      DAOキーワード
        @param chqueue  パラメータ
        @param o        ユーザー定義オブジェクト
    */
    public void set(String key, CharArrayQueue chqueue, Object o) {
        this.key = key;
        //this.strs = chqueue.toStringArrayToJIS();
        parameter.clear(); parameter.add(chqueue);
        this.object = o;
        this.sql.set(getSQL(key));
    }
    public void set(CharArray key, CharArrayQueue chqueue, Object o) {
        this.key = key.toString();
        //this.strs = chqueue.toStringArrayToJIS();
        parameter.clear(); parameter.add(chqueue);
        this.object = o;
        this.sql.set(getSQL(key));
    }

    /** SQL文を指定する <br>
        この指定があった場合はDAOキーワードのSQLは無視され、<br>
        そのコネクション情報のみが利用される
        @param sql SQL文
    */
    public void setSQL(String sql) { 
        this.sql.set(sql);
    }
    /** SQL文を追加する 
        @param sql SQL文
    */
    public void addSQL(String sql) { 
        this.sql.add(sql);
    }
    /** SQL文を追加する 
        @param sql SQL文
        @param str 追加文字列
    */
    public void addSQL(String sql, String str) { 
        this.sql.add(sql);
        parameter.add(str);
    }
    /** SQL文を追加する 
        @param sql SQL文
        @param str 追加文字列
    */
    public void addSQL(String sql, CharArray str) { 
        this.sql.add(sql);
        parameter.add(str);
    }
    /** SQL文を追加する 
        @param sql SQL文
        @param no 追加数値
    */
    public void addSQL(String sql, int no) { 
        this.sql.add(sql);
        parameter.add(no);
    }
    /** SQL文を追加する 
        @param sql SQL文
        @param no 追加数値
    */
    public void addSQL(String sql, long no) { 
        this.sql.add(sql);
        parameter.add(no);
    }
    /** SQL文を追加する 
        @param sql SQL文
        @param no 追加数値
    */
    public void addSQL(String sql, double no) { 
        this.sql.add(sql);
        parameter.add(no);
    }
    /** SQL文を追加する 
        @param sql SQL文
        @param b 追加条件値
    */
    public void addSQL(String sql, boolean b) { 
        this.sql.add(sql);
        parameter.add(b);
    }

    /** SQL文を指定する <br>
        この指定があった場合はDAOキーワードのSQLは無視され、<br>
        そのコネクション情報のみが利用される
        @param sql        SQL文
        @param chqueue  パラメータ
    */
    public void setSQL(String sql,CharArrayQueue chqueue) { 
        this.sql.set(sql); 
        parameter.clear(); parameter.add(chqueue);
    }

    /** SQL文を指定する <br>
        この指定があった場合はDAOキーワードのSQLは無視され、<br>
        そのコネクション情報のみが利用される
        @param sql        SQL文
    */
    public void setSQL(CharArray sql) { 
        this.sql.set(sql);
    }
    /** SQL文を追加する 
        @param sql        SQL文
    */
    public void addSQL(CharArray sql) { 
        this.sql.add(sql);
    }
    /** SQL文を追加する 
        @param sql  SQL文
        @param str  追加文字列
    */
    public void addSQL(CharArray sql, String str) { 
        this.sql.add(sql);
        parameter.add(str);
    }
    /** SQL文を追加する
        @param sql  SQL文
        @param str  追加文字列
     */
    public void addSQL(CharArray sql, CharArray str) { 
        this.sql.add(sql);
        parameter.add(str);
    }
    /** SQL文を追加する 
        @param sql  SQL文
        @param no  追加数値
    */
    public void addSQL(CharArray sql, int no) { 
        this.sql.add(sql);
        parameter.add(no);
    }
    /** SQL文を追加する
        @param sql  SQL文
        @param no  追加数値
     */
    public void addSQL(CharArray sql, long no) { 
        this.sql.add(sql);
        parameter.add(no);
    }
    /** SQL文を追加する 
        @param sql  SQL文
        @param no  追加数値
    */
    public void addSQL(CharArray sql, double no) { 
        this.sql.add(sql);
        parameter.add(no);
    }
    /** SQL文を追加する 
        @param sql  SQL文
        @param b  追加条件値
    */
    public void addSQL(CharArray sql, boolean b) { 
        this.sql.add(sql);
        parameter.add(b);
    }
    
    /** SQL文を指定する <br>
        この指定があった場合はDAOキーワードのSQLは無視され、<br>
        そのコネクション情報のみが利用される
        @param sql        SQL文
        @param chqueue  パラメータ
    */
    public void setSQL(CharArray sql, CharArrayQueue chqueue) { 
        this.sql.set(sql);
        parameter.clear(); parameter.add(chqueue);
    }
    /** SQL文を指定する <br>
        この指定があった場合はDAOキーワードのSQLは無視され、<br>
        そのコネクション情報のみが利用される
        @param sql        SQL文
        @param chqueue  パラメータ
        @param o          ユーザ定義オブジェクト(executeに渡される）
    */
    public void setSQL(CharArray sql,CharArrayQueue chqueue, Object o) { 
        this.sql.set(sql);
        parameter.clear(); parameter.add(chqueue);
        this.object = o;
    }

    /** オブジェクトを設定する 
        @param o オブジェクト
    */
    public void setObject(Object o) {
        this.object = o;
    }
    /** オブジェクトを取得する 
        @return オブジェクト
    */
    public Object getObject() {
        return object;
    }

    /** 複数のパラメータを設定する 
        @param chqueue パラメータリスト
    */
    public void setParameter(CharArrayQueue chqueue) {
        parameter.clear(); parameter.add(chqueue);
    }
    /** 複数のパラメータを設定する 
        @param strs パラメータリスト
    */
    public void setParameter(String[] strs) {
        parameter.clear(); parameter.add(strs);
    }
    /** パラメータを設定する 
        @param param パラメータ文字列
    */
    public void setParameter(CharArray param) {
        parameter.clear(); parameter.add(param);
    }
    /** パラメータを設定する 
        @param param パラメータ文字列
    */
    public void setParameter(String param) {
        parameter.clear(); parameter.add(param);
    }
    /** パラメータを設定する 
        @param param パラメータ数値
    */
    public void setParameter(int param) {
        parameter.clear(); parameter.add(param);
    }
    /** パラメータを設定する 
        @param param パラメータ数値
    */
    public void setParameter(long param) {
        parameter.clear(); parameter.add(param);
    }
    /** パラメータを設定する 
        @param param パラメータ数値
    */
    public void setParameter(double param) {
        parameter.clear(); parameter.add(param);
    }
    /** パラメータを設定する 
        @param param パラメータ条件値
    */
    public void setParameter(boolean param) {
        parameter.clear(); parameter.add(param);
    }
    
    /** 複数のパラメータを追加する 
        @param chqueue パラメータリスト
    */
    public void addParameter(CharArrayQueue chqueue) {
        parameter.add(chqueue);
    }
    /** 複数のパラメータを追加する 
        @param strs パラメータリスト
    */
    public void addParameter(String[] strs) {
        parameter.add(strs);
    }
    
    /** パラメータを追加する 
        @param param パラメータ文字列
    */
    public void addParameter(CharArray param) {
        parameter.add(param);
    }
    /** パラメータを追加する 
        @param param パラメータ文字列
    */
    public void addParameter(String param) {
        parameter.add(param);
    }
    /** パラメータを追加する 
        @param param パラメータ数値
    */
    public void addParameter(int param) {
        parameter.add(param);
    }
    /** パラメータを追加する 
        @param param パラメータ数値
    */
    public void addParameter(long param) {
        parameter.add(param);
    }
    /** パラメータを追加する 
        @param param パラメータ数値
    */
    public void addParameter(double param) {
        parameter.add(param);
    }
    /** パラメータを追加する 
        @param param パラメータ条件値
    */
    public void addParameter(boolean param) {
        parameter.add(param);
    }
    /** パラメータを取得する 
        @return パラメータリスト
    */
    public Parameter getParameter() { return parameter;}
    
    /** パラメータをクリアする*/
    public void clearParameter() { parameter.clear();}

    /** SQL を取得する 
        @return SQL文字列
    */
    public CharArray getSQL() {
        return sql;
    }

    /** SQL を取得する 
        @param key SQLキーワード
        @return SQL文字列
    */
    public String getSQL(String key) {
        if (session == null && site_code > 0) {
            return DataSourceManager.getSQL(key, site_code);
        }
        return DataSourceManager.getSQL(key, session);
    }
    /** SQL を取得する 
        @param key SQLキーワード
        @return SQL文字列
    */
    public String getSQL(CharArray key) {
        if (session == null && site_code > 0) {
            return DataSourceManager.getSQL(key, site_code);
        }
        return DataSourceManager.getSQL(key, session);
    }

    /** 検索最大数(SearchMax) をオーバーライド 
        @param size 最大値
    */
    public void setMax(int size) {
        this.search_max = size;
    }
    public void setSearchMax(int size) {
        this.search_max = size;
    }
    /** 検索フラグ(SearchNext) をオーバーライド 
        @param mode 検索フラグ
    */
    public void setNext(boolean mode) {
        this.search_next = mode;
        search_next_flg = true;
    }
    public void setSearchNext(boolean mode) {
        this.search_next = mode;
        search_next_flg = true;
    }
    /** 情報を全てクリアする */
    public void clear() {
        page = null;
        parent = null;
        session = null;
        site_code = 0;
        key = null;
        sql.clear();
        orgSQL.clear();
        orgParam.clear();
        parameter.clear();
        object = null;
        cursor_end = false;
        auto_commit = true;
        search_max = 0;
        search_next = false;
        search_next_flg = false;
        lastException = null;
        timestamp = 0;
        szConnectionName = null;
    }
    
    /**
        テーブルのクリアを行う ※ close()は行わない<br>
        通常は呼ばないで下さい
    */
    public void clearTable() {
        if (table != null) {
            table.close();
            table = null;
if (debugSystemLock) System.out.print("DAO(GC)");
            GCManager.execute();
if (debugSystemLock) System.out.println("DAO(GC)end");
        }
    }

    //-----------------------------------
    private Connection        conn  = null;
    private PreparedStatement ps    = null;
    private ResultSet         rs    = null;
    private ResultSetMetaData meta  = null;
    private ResultTable       table = null;
    private int               status = -1;

    public Connection getConnection() { return conn; }
    public PreparedStatement getPreparedStatement() { return ps; }

    /** ステータスを取得する<br>
     statement#executeUpdate 等の結果も これで参照する 
     @return ステータス
     */
    public  int getStatus() { return status;}

    private DataConnection getDataConnection() {
        if (szConnectionName != null) {
            DataConnection _dc = DataSourceManager.getConnectionList().get(szConnectionName);
            if (_dc != null) return _dc;
            System.out.println("DAO:getDataConnection:"+szConnectionName+" が見つかりません");
        }
        if (key == null && session != null) {
            String str = session.getModuleManager().defaultConnection;
            if (str.length() > 0) key = "$$$"+str+"$$$";
            
//System.out.println("★★DAO moduleManager.default:connection="+key);
            //if (key.length() > 0) {
            //    DataConnection dc = DataConnection.getDataConnection(key);
            //    if (dc != null) return dc;
            //}
        }
        if (session == null && site_code > 0) {
            return DataSourceManager.getDataConnection(key, site_code);
        }
        return DataSourceManager.getDataConnection(key, session);
    }

    /**
        result set を取得する<br>
        ※これを使用した場合は 自分で rollback() close() して下さい
        @throws SQLException SQLエラー
        @return ResultSet
    */
    public ResultSet getResultSet() throws SQLException {
        return getResultSet(getDataConnection(),false);
    }

    /**
        execute updateを行う<br>
        ※これを使用した場合は 自分で rollback() close() して下さい
        @throws SQLException SQLエラー
        @return ステータス
    */
    public int psExecuteUpdate() throws SQLException {
        DataConnection dc = getDataConnection();
        if (conn == null) conn = DataSourceManager.getConnection(dc);
//if (debugSession) println("conn.setAutoCommit("+auto_commit+")");
        conn.setAutoCommit(auto_commit);
        ps   = DataSourceManager.getPreparedStatement(conn, key, sql.toString(), parameter, dc, session, debugSQL);
        status = ps.executeUpdate();
        return status;
    }

    private ResultSet getResultSet(DataConnection dc, boolean update) throws SQLException {
        if (conn == null) conn = DataSourceManager.getConnection(dc);
        
//if (debugSession) println("conn.setAutoCommit("+auto_commit+")");
        conn.setAutoCommit(auto_commit);
        ps   = DataSourceManager.getPreparedStatement(conn, key, sql.toString(), parameter, dc,session, debugSQL);
        long timer = Util.Timer();
        if (update) {
            status = ps.executeUpdate();
            rs = null;
            if (debug && debugSQL) {
                long lapse = Util.Lapse(timer);
                String str ="";
                if (lapse >= 30000)      str = "★★★";
                else if (lapse >= 20000) str = "★★";
                else if (lapse >= 10000) str = "★";
                println("DAO.executeUpdate() Lapse:"+lapse+"\t【"+key+"】"+str);
            }
        } else {
            rs =  ps.executeQuery();
            if (rs != null) {
                //if (debug) System.out.print("Fetch size="+rs.getFetchSize());
                try {
                    if (page != null) rs.setFetchSize(page.getSearchMax(session)+2);  // Postgres だとエラーに
                    //if (debug) System.out.println(" ->="+rs.getFetchSize());
                } catch (Exception ex) {
                    //if (debug) System.out.println("getResultSet: setFetchSize error:"+ex);
                    //ex.printStackTrace();
                }
            }
            if (debug && debugSQL) {
                long lapse = Util.Lapse(timer);
                String str ="";
                if (lapse >= 30000)      str = "★★★";
                else if (lapse >= 20000) str = "★★";
                else if (lapse >= 10000) str = "★";
                println("DAO.executeQuery() Lapse:"+lapse+"\t【"+key+"】"+str);
            }
        }
        return rs;
    }

    private void initTable() throws SQLException  {  // テーブルを初期化する
        long timer = Util.Timer();
        timestamp = timer;
        table = new ResultTable(this);

        ResultSetMetaData meta = rs.getMetaData();
        
        for (int i = 0; i < meta.getColumnCount(); i++) {
            String name    = meta.getColumnName(i+1);
            int type       = meta.getColumnType(i+1);
            String typename  = meta.getColumnTypeName(i+1);
            if (debugTable) {
                String catalog = meta.getCatalogName(i+1);
                String tablename       = meta.getTableName(i+1);
                println("["+i+"]"+
                    " catalog["+catalog+"]"+
                    " name["+name+"]"+
                    " type["+type+"]"+
                    " typename["+typename+"]"+
                    " tablename["+tablename+"]"
                    );
            }
            switch (type) {
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                table.getTable().addColumn(Queue.DATE,name.toUpperCase());
                break;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            //case Types.NUMERIC:   // 文字列型に変更
            //case Types.DECIMAL:   // 文字列型に変更
                table.getTable().addColumn(Queue.INT,name.toUpperCase());
                break;
            case Types.BIGINT:
                table.getTable().addColumn(Queue.LONG,name.toUpperCase());
                break;
            case Types.BIT:       // 1ビットと限らないのでやめる
//System.out.println("BIT["+typename+"]");
                if (typename.toLowerCase().startsWith("bool")) { // 
                    table.getTable().addColumn(Queue.BOOL,name.toUpperCase());
                } else {
                    table.getTable().addColumn(Queue.STRING,name.toUpperCase());
                }
                break;
            case Types.BOOLEAN:     //  since JDK 1.4
                table.getTable().addColumn(Queue.BOOL,name.toUpperCase());
                break;
            case Types.REAL:
            case Types.DOUBLE:
            case Types.FLOAT:
                table.getTable().addColumn(Queue.DOUBLE,name.toUpperCase());
                break;
            default:
                table.getTable().addColumn(Queue.STRING,name.toUpperCase());
                break;
            }
        }
//        if (debug) System.out.println("DAO.initTable() Lapse:"+Util.Lapse(timer));
    }
    
    // １行を取り出してテーブルに追加する
    private boolean  next(int j, int count) throws SQLException {
        return next(j,count,false);
    }
    
    // １行を取り出してテーブルに追加する
    private boolean  next(int j, int count, boolean flg) throws SQLException {
        int convert = 1;
        DataConnection dc = getDataConnection();
        if (dc != null) convert = dc.inputConvertMode;
        
        //long timer2 = Util.Timer();
        boolean rsts = flg ? flg : rs.next();
        //if (debug) System.out.print(" rs.next lapse:"+Util.Lapse(timer2));
        
        if (!rsts) return false;
        //if (debug) timer2 = Util.Timer();
        table.getTable().addRow();
        for (int i = 0; i < count; i++) {
            switch (getTable().getColumnType(i)) {
            case Queue.INT:
                table.getTable().setInt(rs.getInt(i+1),j,i);
                break;
            case Queue.LONG:
                table.getTable().setLong(rs.getLong(i+1),j,i);
                break;
            case Queue.BOOL:
                table.getTable().setBoolean(rs.getBoolean(i+1),j,i);
                break;
            case Queue.DOUBLE:
                table.getTable().setDouble(rs.getDouble(i+1),j,i);
                break;
            case Queue.STRING:
                String str = rs.getString(i+1);
                CharArray ch = new CharArray(str);
                if (convert == 1) {
if (debugCode) System.out.print("orginal["+ch+"]");
                    ch.toCp932();       // 文字変換
if (debugCode) System.out.println("toCp932["+ch+"]");
                } else if (convert == 2) {
if (debugCode) System.out.print("orginal["+ch+"]");
                    ch.toJIS();       // 文字変換
if (debugCode) System.out.println("toJIS["+ch+"]");
                } else if (convert == 3) {
if (debugCode) System.out.print("orginal["+ch+"]");
                    ch.toCp9322();       // 文字変換
if (debugCode) System.out.println("toCp932(2)["+ch+"]");
                } else if (convert == 4) {
if (debugCode) System.out.print("orginal["+ch+"]");
                    ch.toJIS2();       // 文字変換
if (debugCode) System.out.println("toJIS2["+ch+"]");
                }
                table.getTable().setCharArray(ch,j,i);
                /*****
                System.out.println(ch);
                CharArray tmp = CharArray.pop();
                for (int ii = 0; ii < ch.length;ii++) {
                    char c = ch.chars[ii];
                    tmp.set(c);tmp.add("[");
                    tmp.format((int)c,16);
                    tmp.add("]");
                    System.out.print(tmp);
                }
                System.out.println("");
                byte[] by = rs.getBytes(i+1);
                if (by != null) {
                for (int ii = 0; ii < by.length; ii++) {
                    int b = by[ii];
                    tmp.set(":");
                    tmp.format(b,16);
                    System.out.print(tmp);
                }
                }
                CharArray.push(tmp);
                *****/
                //ch.toCp932();
                //System.out.println("\n -> "+ch);
                break;
            case Queue.DATE:
                long l = 0;
                try {
                    Timestamp stamp = rs.getTimestamp(i+1);
                    l = (stamp != null) ? stamp.getTime() : -1; //  0 -> -1
                } catch (Exception ex) {
                    if (debug) ex.printStackTrace();
                }
                table.getTable().setLong(l,j,i);
                break;
            }
        }
        //if (debug) System.out.println("\trs.setString lapse:"+Util.Lapse(timer2));
        return true;
    }
    
    /** コネクションをクローズする 
        @throws SQLException SQLエラー
    */
    public void close() throws SQLException {
if (debugSession) println("◆DAO.close()");
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            if (ex.toString().indexOf("Already closed.") < 0) throw ex;
        } finally {
            rs = null;
            ps = null;
            conn = null;
            auto_commit = true;
            szConnectionName = null;
       }
    }

    private boolean cursor_end = false;     // 

    /** カーソルが最後まで来たか？ <br>
        注）JDBC実装によっては取得できないかも知れない
        @return true:カーソルエンド
    */
    public boolean isLast() { return cursor_end; }
    
    /** カーソル情報を強制設定する 
        @param mode カーソルエンド情報
    */
    public void setCursorEnd(boolean mode) { cursor_end = mode;}

    private boolean auto_commit = true;

    public boolean isAutoCommit() { return auto_commit; }

    /** オートコミットモードを設定する （EJB非対応）<br>
        通常は begin()/commit() を使用する事
        @param mode オートコミットモード
    */
    public void setAutoCommit(boolean mode) { auto_commit = true; }
    
    /** AutoCommitをfalseにする (EJBでも有効)*/
    public void begin() { 
if (debugSession) println("◆DAO.begin()");
        auto_commit = false;
    }
    
    /** コネクションを設定して begin() 
        @param ch コネクション
    */
    public void begin(CharArray ch) {
        set(ch);
        begin();
    }
    /** コネクションを設定して begin() 
        @param str コネクション
    */
    public void begin(String str) {
        set(str);
        begin();
    }
    
    
    /** commit を発行する (EJBでも有効) 
        @return ステータス 0:成功
    */
    public int commit() { 
if (debugSession) println("◆DAO.commit()");
        int sts = 0;
        if (conn == null) {
            ServletLog.getInstance().out("DAO commit conn not found!");
            println("DAO commit conn not found!");
            return ServletLog.SYSTEM_ERROR;
        }
        try {
            conn.commit();
            close(); 
        } catch (SQLException ex) {
            //if (!auto_commit && conn != null) conn.rollback();
            //close();
            rollback();
            sts = ServletLog.SQL_EXCEPTION;
            lastException = ex;
            ex.printStackTrace();
        }
        return sts;
    }
    
    /** ロールバックを行う<br>
        begin() 以前の状態に戻ります。DAOの情報は全てリセットされます。
        @return ステータス 0:成功
    **/
    public int rollback() {
        int sts = 0;
        try {
            if (!auto_commit && conn != null) {
                if (debugSession) println("◆DAO.rollback()");
                conn.rollback();
            }
            close();
        } catch (SQLException ex) {
            sts = ServletLog.SQL_EXCEPTION;
            ex.printStackTrace();
        }
        return sts;
    }
    
    /** 
        Table をセットする  DBは使用しない
        @param from QueueTable
    */
    public void set(QueueTable from) {
        //this.table = table;
        if (table == null) table = new ResultTable(this);
        else               table.getTable().clearAll();
        if (from != null)  table.copy(from);
        
        cursor_end = true;
    }
    /** 
        Table をセットする  DBは使用しない
        @param from QueueTable
    */
    public void setTable(QueueTable from) {
        set(from);
    }
    //----------------------------------------------------------------------
    /**
        Tableを生成し、TableDataAccess.execute() を起動する<br>
        PageServletから呼ばれる
        @return 0:成功
    */
    public int execute() {
        //if (page == null) return ServletLog.SYSTEM_ERROR;
        int start = 0; 
        int max = 1000;
        if (page != null && session != null) {
            start = page.getPageStart(session);
            max = page.getPageMax(session);
        } else if (session != null) {
            
        }
        return execute(start,max,false);
    }
    /**
        Tableを生成し、TableDataAccess.execute() を起動する
        @param size 最大表示サイズ
        @return 0:成功
    */
    public int execute(int size) {
        //if (page == null) return ServletLog.SYSTEM_ERROR;
        int start = 0; 
        if (page != null && session != null) {
            start = page.getPageStart(session);
        }
        return execute(start,size,false);
    }
    
    protected boolean callExecute = true;

    public void setCallExecute(boolean mode) { callExecute = mode;}
    public boolean isCallExecute() { return callExecute;}

    /**
        Tableを生成する、
        @param execute true の時にTableDataAccess.execute() を起動する
        @return 0:成功
    */
    public int execute(boolean execute) {
        //if (page == null) return ServletLog.SYSTEM_ERROR;
        int start = 0; 
        int max = 1000;
        if (page != null && session != null) {
            start = page.getPageStart(session);
            max = page.getPageMax(session);
        }
        boolean org = callExecute;
        callExecute = execute;
        int sts = execute(start,max,false);
        callExecute = org;
        return sts;
    }
    
    
    /**
        execute() を起動する <br>
        データの更新、削除時に使用する
        @return エラーコード
    */
    public int executeUpdate() {
        //if (page == null) return ServletLog.SYSTEM_ERROR;
        int start = 0; 
        int max = 1000;
        if (page != null && session != null) {
            start = page.getPageStart(session);
            max = page.getPageMax(session);
        }
        return execute(start, max,true);
    }
    public int executeUpdate(int size) {
        //if (page == null) return ServletLog.SYSTEM_ERROR;
        int start = 0; 
        if (page != null && session != null) {
            start = page.getPageStart(session);
        }
        return execute(start,size,true);
    }
    //----------------------------------------------------------------------
    /**
        execute() を起動する
        @param start 開始インデックス
        @param size  検索サイズ
        @return エラーコード
    */
    public int execute(int start, int size) {
        return execute(start,size,false);
    }
    /** DAOの実行
        @param start 開始インデックス
        @param size  検索サイズ
        @param update executeUpdate を呼ぶ
        @return エラーコード
    */
    public int execute(int start, int size, boolean update) {
if (debugSearch) System.out.println("◆execute("+start+","+size+")◆");
        int sts = 0;
        //if (session == null) {
        //    return ServletLog.SESSION_NOT_FOUND;
        //}
        try {
            if (checkPrevious()) {
                //if (debug) System.out.println("dao.execute: clear cash");
                //if (table != null) table.getTable().clear();
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                rs = null; ps = null;
                cursor_end = false;
            }
            int     SEARCH_MAX  = 10;
            boolean SEARCH_NEXT = false;
            if (page != null) {
                SEARCH_MAX  = page.getSearchMax(session);
                SEARCH_NEXT = page.getSearchNext(session);
            } else {
                if (SEARCH_MAX < size) SEARCH_MAX = size;
            }
            if (search_max > 0) SEARCH_MAX = search_max;
            if (search_next_flg) SEARCH_NEXT = search_next;
            if (debugSearch) {
                System.out.println("  SEARCH_MAX="+SEARCH_MAX+" SEARCH_NEXT="+SEARCH_NEXT);
            }
            if (update) {
                cursor_end = false;
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                rs = null;
                ps = null;
                //ejbObject = null;
            }
            if (cursor_end && table != null && table.getTable() != null) {
                if (debugCache) println("DAO.execute: キャッシュを使用します");
                if (!update && (parent != null) && callExecute && page != null) {
                    boolean showTemplate = true;
                    if (callExecute && parent != null) {
                        if (table != null && table.getTable() != null) {
                            int _hash = page.hashCode();

                            int cache = CharArray.getInt(session.getParameter("cache@hash")); // InputModuleからのみセットされている
                            if (cache > 0 && cache == _hash) {
                                return sts; // InputModuleでは１回しか呼ばない
                            }
                            int hash = CharArray.getInt(session.getParameter("page@hash")); // InputModuleからのみセットされている

                            String tm = session.getParameter(tmKey);
                            if (hash > 0 && hash == _hash) {
                                String _tm = session.getParameter(_hash+"_"+tmKey);
                                tm = _tm;
                                if (tm.length() ==0) {
                                    tm=getTimeStampKey();
                                }
                            }
                            if (tm == null || tm.length() == 0 || getTimeStampKey().equals(tm)) { //OK
if (debugCache) System.out.println("このキャッシュを利用してexecute。");
                                parent.execute(session, table.getTable(), start, size, cursor_end, object);
                            } else {
                                if (debug) println("このキャッシュは使用できません。");
                                parent.execute(session, null, start, size, cursor_end, object);
                                showTemplate = false;
                            }
                        }
                    }
                    int count = table.getTable().getRowCount();
                    int rows = Math.min(count, start+size-1);
                    boolean prevFlag = (start > 0);
                    //boolean nextFlag = ((!cursor_end || rows+1 < count) && rows != start && (rows+1 < page.getSearchMax()));
                    boolean nextFlag = ((!cursor_end || rows+1 < count) && rows != start && (rows+1 < SEARCH_MAX));
                    if (showTemplate) {
                        page.setPageValue(prevFlag, nextFlag, start, size, count, cursor_end, session, getTimeStampKey());
                    }
                }
            } else {
                if (debugCache) println("DAO.execute: キャッシュを使用しません");
                if ((rs == null /*&& ejbObject == null*/) ||
                    (!update && (table == null || table.getTable()==null))) {
                    DataConnection dc = getDataConnection();
                    if (dc == null) {
                        ServletLog.getInstance().out("DAO execute DataConnection not found !");
                        println("DAO execute DataConnection not found !");
                        return ServletLog.SYSTEM_ERROR;
                    }
                    //if (dc.isEJB) {
                    //    ejbObject = getEJBObject(dc);
                    //} else {
                        rs = getResultSet(dc,update);        //rs.setFetchSize(10);
                        if (!update) initTable();
                    //}
                }
                long  timer = Util.Timer();
                //if (debug) System.out.println("DAO.execute start");
                
                int max = start + size;
                if (!update) {                       // ResultSet
                    int count = table.getTable().getColumnCount();  //@@// ここで NULLPointerになる事が
                    int rows  = table.getTable().getRowCount();
                    boolean check_flg = false;
                    
                    //if (SEARCH_MAX > 0) { 
                        //if (page != null && page.getSearchNext()) {     // nextモード
                        if (page != null && SEARCH_NEXT) {     // nextモード
                            int i = SEARCH_MAX;
                            while (i < max) i += SEARCH_MAX;
                            max = i;
                        } else {
                            max = SEARCH_MAX;
                        }
                    //}
                    for (int j = rows; j < max ; j++) {
                        //System.out.print("  "+j+"\r");
                        check_flg = next(j,count,(rows != 0 && j == rows));
                        if (!check_flg) break;
                    }
                    
                    // デバッグ用 あとで取る //@@//
                    try {
                        cursor_end = rs.isAfterLast();
                    } catch (Exception ex1) {
                        System.out.println("DAO.execute rs.isAfterLast() error:"+ex1);
                    }
                              // Oracle だと isLast() が効かないので、、(;_;)
                    if (!cursor_end && check_flg) {   // 最終行かどうかチェック
                        check_flg = rs.next();
                        if (!check_flg) {
                            cursor_end = true;
                        }
                    }
                    //if (rows == 0) cursor_end = true; // 違う処理にする
                    if (debug) {
                        //System.out.println("getRow()="+rs.getRow());
                        //System.out.println("isLast()="+rs.isLast());
                    }
                }
                //-----------------------------------------------------
                if (!update && (parent != null) && callExecute && page != null) {
                    parent.execute(session, table.getTable(), start, size, cursor_end, object);
                    
                    int count = table.getTable().getRowCount();
                    int rows = Math.min(count, start+size-1);
                    boolean prevFlag = (start > 0);
                    boolean nextFlag = ((!cursor_end || rows+1 < count) && rows != start && (rows+1 < max));
                    
                    //if (page.getSearchNext() && !cursor_end && nextFlag ==false) {
                    //if (SEARCH_NEXT && !cursor_end && nextFlag ==false) {
                    if (rows > 0 && SEARCH_NEXT && !cursor_end && nextFlag ==false) {
                        nextFlag = true;
                    }
                    page.setPageValue(prevFlag, nextFlag, start, size, count, cursor_end,session, getTimeStampKey());
                }
                // カーソルが最後までいっていたらクローズしてしまう
                if (auto_commit) {
                    if (cursor_end) close();
                    else if (SEARCH_NEXT == false && !update) close();
                }
                
                if (table != null) table.update();  // 更新する
            }
        } catch (SQLException e) {
            lastException = e;
            sts = ServletLog.SQL_EXCEPTION;
            e.printStackTrace();
            rollback();
        } catch (Exception e) {
            lastException = e;
            sts = ServletLog.EXCEPTION;
            e.printStackTrace();
            rollback();
        }
        return sts;
    }

    CharArray orgSQL = new CharArray();
    CharArrayQueue orgParam = new CharArrayQueue();
    private boolean checkPrevious() {  // 以前の値と比較チェック
        boolean changed = false;
        //String szSQL = (sql.length() > 0) ? sql.toString() : DataSourceManager.getSQL(key);
        do {
            if (sql.length() == 0) {
                if (orgSQL.length() > 0) changed= true; break;
            } else if (!sql.equals(orgSQL)) { 
                changed= true; break;
            }
            if (parameter.size() != orgParam.size()) {
                changed = true;
                break;
            } else {
                for (int i = 0; i < orgParam.size(); i++) {
                    if (!orgParam.peek(i).equals(parameter.peek(i))) {
                        changed = true;
                        break;
                    }
                }
            }
        } while (false);
        if (changed) {
            orgSQL.set(sql);
            orgParam.copy2(parameter);
        }
if (debugCache) System.out.println("☆★ checkPrevious :"+changed);
        return changed; // 変更されていたら true
    }

    /** テーブルを取得する 
        @return 取得テーブル
    */
    public QueueTable getTable() { 
        //table.update();
        return table.getTable();
    }
    /** テーブル内容をダンプする（デバッグ用） **/
    public void dumpTable() {
        table.getTable().dumpTable();
    }
    /** テーブル内容をダンプする（デバッグ用） 
        @param size 指定行数のみ印字する
    */
    public void dumpTable(int size) {
        println("DAO: table dump:"+size+"/"+table.getTable().getRowCount());
        table.getTable().dumpTable(size);
    }
    
    static public boolean delay(String key, CharArrayQueue queue) {
        DelayWriterModule module = (DelayWriterModule)SystemManager.loader.getModule("DelayWriter");
        if (module != null) {
            module.add(key, queue);
            return true;
        }
        return false;
    }
    static public boolean delay(CharArray key, CharArrayQueue queue) {
        DelayWriterModule module = (DelayWriterModule)SystemManager.loader.getModule("DelayWriter");
        if (module != null) {
            module.add(key, queue);
            return true;
        }
        return false;
    }
    static public boolean delay(String key, CharArrayQueue queue, SessionObject session) {
        DelayWriterModule module = (DelayWriterModule)SystemManager.loader.getModule("DelayWriter");
        if (module != null) {
            module.add(key, queue, session);
            return true;
        }
        return false;
    }
    static public boolean delay(CharArray key, CharArrayQueue queue, SessionObject session) {
        DelayWriterModule module = (DelayWriterModule)SystemManager.loader.getModule("DelayWriter");
        if (module != null) {
            module.add(key, queue, session);
            return true;
        }
        return false;
    }

/*************
    static public boolean delaySQL(String sql, CharArrayQueue queue) {
        DelayWriterModule module = (DelayWriterModule)SystemManager.loader.getModule("DelayWriter");
        
        if (module != null) {
            module.add(sql, queue, page);
            return true;
        }
        return false;
    }

    static public boolean delaySQL(CharArray chSQL, CharArrayQueue queue) {
        String sql = chSQL.toString();
        DelayWriterModule module = (DelayWriterModule)SystemManager.loader.getModule("DelayWriter");
        
        if (module != null) {
            module.add(sql, queue, page);
            return true;
        }
        return false;
    }
************/
}

//
// [end of DataAccessObject.java]
//
