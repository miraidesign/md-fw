//------------------------------------------------------------------------
// @(#)DataSourceManager.java
//                  データソースを管理する
//                  Copyright (c) MiraiDesign 2010-13 All Rights Reserved.
//------------------------------------------------------------------------
//
package com.miraidesign.data;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;
import java.sql.Connection;
import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.servlet.ServletLog;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.SiteManager;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.IniFile;
import com.miraidesign.util.Util;
import com.miraidesign.util.Queue;


/**
    データソースを管理する
*/
public class DataSourceManager {
    static private boolean debug = (SystemConst.debug && true);  // デバッグ表示
    static private boolean debugFilename = (SystemConst.debug && true);  // デバッグ表示
    static private boolean debugConnect = (SystemConst.debug && false);  // デバッグ表示
    static private boolean debugConnection = (SystemConst.debug && false);  // デバッグ表示 false
    static private boolean debugParameter = (SystemConst.debug && true);  // デバッグ表示
    static private boolean debugPrefix    = (SystemConst.debug && false);  // デバッグ表示
    
    static public void setDebug(boolean mode) { debug = mode;}
    static public void setDebugConnect(boolean mode) { debugConnect = mode;}
    // key:   String:    SQLキーワード
    // value: String[2]  {コネクション文字列, SQLデータ}
    //                 キャッシュ情報をつけて拡張！
    
/**    
    キャッシュ関連
■条件ファイル （SQL毎）
・キャッシュサイズ （０以下でキャッシュしない）
・最長保存期間     （０以下で無限）

■キャッシュされるクラス
・テーブル
・最初の時刻（一定時間を超えたら削除）
・参照回数
・最大検索数（変更になったら削除）

■キャッシュコントローラ
・HashBinaryTreeクラスを継承
・キーは検索名
**/
    
    
    private static Hashtable<CharArray,String[]> sqlHash; 
    
    private static String defaultKey = "$$$default$$$";
    
    // key:   String  コネクション文字列
    // value: DataConnection オブジェクト
    private static Hashtable<String,DataConnection> connectionHash = 
               new Hashtable<String,DataConnection>();

    /** コネクション情報リストを返します<br>
        key:   String  コネクション文字列<br>
        value: DataConnection オブジェクト
        @return コネクション情報リスト
    **/
    public static Hashtable<String,DataConnection> getConnectionList() {
        return connectionHash;
    }

    // EJB に sql.ini の {SQLData] セクションの内容をコピーして渡す
    private static CharArrayQueue sqlQueue = new CharArrayQueue();
    // ccm.ini -------------------------------------------------------------
    static private String section = "[DataSource]";

    /** system.ini を読み込む 
        @param ini system.ini
    */
    public static void load(IniFile ini) {
        debug        &= SystemConst.debug;
        debugConnect &= SystemConst.debug;
        debugConnection &= SystemConst.debug;
        debugParameter &= SystemConst.debug;
        if (debug) System.out.println("DataSourceManager load start--");
        DataConnection.init(ini);
        if (sqlHash == null) {
            sqlHash = new Hashtable<CharArray,String[]>();
            sqlHash.put(new CharArray(defaultKey),new String[] {DataConnection.defaultConnection, ""});
            
            /**/
            CharArray chList = SystemManager.ini.get(section,"UseSourceList");
            if (chList != null) {
                CharToken token = CharToken.pop(chList);
                for (int i = 0; i < token.size(); i++) {
                    CharArray name = token.get(i).trim();
                    if (name.length()>0) {
                        sqlHash.put(new CharArray("$$$"+name+"$$$"),new String[] {name.toString(), ""});
                    }
                } // next
            }
            
            /**/

            //connectionHash = new Hashtable();
            CharArray filenames = ini.get("[Resource]","SQLData");
            if (filenames != null) {
                filenames.convertProperty();
                CharToken token = CharToken.pop(filenames);
                for (int t = 0; t < token.size(); t++) {
                    CharArray ch = token.get(t).trim();
                    if (ch.length() > 0) {
                        IniFile sqlIni = new IniFile(SystemManager.resourceDirectory+ch,
                                        "=","#","\\");
                        sqlIni.checkMultiKey(true);
                        sqlIni.setInclude("#include");
                        sqlIni.setEncoding("UTF-8");
                        sqlIni.read();
                        if (debugFilename) System.out.println(""+ch+" reading....");
                        if (sqlIni.isOK()) {
                            setSQL(sqlIni);
                        } else {
                            ++SystemManager.init_error;
                            SystemManager.log.out(ServletLog.FILE_NOT_FOUND,"SQLData:"+SystemManager.resourceDirectory+ch);
                            SystemManager.init_error_queue.enqueue("Resource not found!! SQLData:"+SystemManager.resourceDirectory+ch);
                        }
                    }
                }
                CharToken.push(token);
            }
            
        }
        if (debug) System.out.println("DataSourceManager load end--");
    }
    
    private static void setSQL(IniFile ini) {
        if (debug) System.out.println("----- setSQL -----------------");
        CharArrayQueue prefixQueue = new CharArrayQueue();
        CharArray ch = ini.get("[Connection]","Prefix");   // 接頭辞
        if (ch != null && ch.trim().length() > 0) {
            CharToken token = CharToken.pop();
            token.set(ch,",");
            for (int i = 0; i < token.size(); i++) {
                CharArray ca = token.get(i);
//System.out.println("●Prefix["+i+"]"+ca);  // @@
                if (ca != null && ca.trim().length() > 0) {
                    prefixQueue.enqueue(new CharArray(ca));
                }
            }
            CharToken.push(token);
        }
        if (prefixQueue.size() == 0) prefixQueue.enqueue("");
        String szDefault = ini.getString("[Connection]","Default");
//System.out.println("●Default:"+szDefault);  // @@
        for (Enumeration e = ini.getKeyList("[SQLData]"); e.hasMoreElements();) {
            CharArray key = (CharArray)e.nextElement();
            String data = ini.getString("[SQLData]",key)+" ";
                //if (debug) System.out.println("["+i+"]"+key+"="+data);
            sqlQueue.enqueue(key+"="+data);
            
            ch = ini.get("[Connection]",key);
            
            String connection = (ch != null && ch.length()>0) ? ch.toString() : szDefault;
            
            String[] strs = new String[2];
            strs[0] = connection;
            strs[1] = data;
            
            for (int i = 0; i < prefixQueue.size(); i++) {
                CharArray key2 = new CharArray();
                key2.add(prefixQueue.peek(i));
                key2.add(key);
                
                if (debug) System.out.println("●key="+key2+" connection="+connection);
            
                boolean isSQL = setConnection(key2.toString(),connection);
            
                if (isSQL) {
                    //if (debug) System.out.println("●●isSQL なので 保存します●"+key2+"●");
                    if (key.length() == 0) {
                        System.out.println("●●DataSouceManager●●キーがありません["+strs[1]+"]");
                   
                    } else if (sqlHash.containsKey(key2)) {
                        System.out.println("●●DataSouceManager●●重複キー["+key2+"]があります");
                    } else {
                        sqlHash.put(key2, strs);
                    }
                }
            }
        } // next
    }

    protected static boolean setConnection(String key, String connection) {
        boolean rsts = true;
        //if (debug) System.out.println("DataSourceManager#setConnection("+key+","+connection+")");
        try {
            if (!connectionHash.containsKey(connection)) {
                DataConnection dc = DataConnection.getDataConnection(connection);
                if (dc != null/* && !dc.isEJB*/) {
                    if (debug) System.out.println("setSQL() new DataConnection:"+connection);
                    connectionHash.put(connection,dc);
                        
                    if (dc.protocol != null) {           // JNDI経由
                        Properties env = new Properties();
                        if (debug) {
                            System.out.println("JNDI Factory="+dc.factory);
                            System.out.println("JNDI Package="+dc.packages);
                            System.out.println("JNDI State  ="+dc.state);
                        }
                        env.put(Context.INITIAL_CONTEXT_FACTORY, dc.factory); 
                        if (dc.packages != null) env.put(Context.URL_PKG_PREFIXES, dc.packages); 
                        if (dc.state != null)    env.put(Context.STATE_FACTORIES,  dc.state); 

                        if (debug) System.out.println("JNDI URL = "+dc.protocol+dc.providerURL+
                                    ":"+dc.providerPort);
                        env.put(Context.PROVIDER_URL, dc.protocol + dc.providerURL + ":"+dc.providerPort);
                        if (dc.authUser != null) env.setProperty(Context.SECURITY_PRINCIPAL, dc.authUser);
                        if (dc.authPassword != null) env.setProperty(Context.SECURITY_CREDENTIALS, dc.authPassword);
                        if (debug) System.out.println("JNDI user = "+dc.authUser+
                                                  " password = "+dc.authPassword);
        
                        long timer = Util.Timer();
                        if (debugConnect) System.out.print("☆☆ conection start ("+key+")........");
                        Context initCtx = new InitialContext(env); 
                        if (debugConnect) System.out.println(" OK "+Util.Lapse(timer));
                        if (debug) {
                            Hashtable hash = initCtx.getEnvironment();
                            System.out.println("-------------- size = "+hash.size());
                            for (Enumeration en = hash.keys(); en.hasMoreElements();) {
                                String envkey = (String)en.nextElement();
                                Object value = hash.get(envkey);
                                System.out.println(envkey+" = "+value);
                            }
                            System.out.println("-------------- ");
                        }
                        if (debug) System.out.println("☆lookup(DataSource)☆"+dc.lookUp);
                        dc.lookupObject = /*(DataSource)*/initCtx.lookup(dc.lookUp);
                        if (debug) System.out.println("lookupObject="+(dc.lookupObject!=null));
                    } else if (dc.dataSourceClass != null) {     // DataSource使用
                        if (debug) {
                            System.out.println("DataSource lookup:"+dc.lookUp);
                        }
                        Context initCtx = new InitialContext();
                        dc.lookupObject = /*(DataSource)*/initCtx.lookup(dc.lookUp);
                    } else {    // 通常の接続
                        if (debug) {
                            System.out.println("DriverManager driver:"+dc.dbDriver);
                        }
                        DriverManager.registerDriver(
                            (Driver)Class.forName(dc.dbDriver).newInstance());
                    
                    }
                } else {
                    if (dc == null) {
                        // nullを認める
                        //SystemManager.log.out(ServletLog.DC_ERROR,connection);
                    } /*else if (dc.isEJB) rsts = false;*/
                }
            } else {
                //if (debug) System.out.println("connection:"+connection+" allready exist!!");
                rsts = true;    //!((DataConnection)connectionHash.get(connection)).isEJB;
            }
        } catch (Exception ex) {
            rsts = false;
            ex.printStackTrace();
        }
    
        return rsts;
    }

    /**
      初期化
    */
    public static void init() {
    }
    

    /** Prefix を考慮した SQLデータを返す 
        @param key SQLキー
        @param session SessionObject
        @return SQLデータ
    */
    private static String[] getPrefixSQL(String key, SessionObject session) {
        CharArray ch = CharArray.pop(key);
        String[] strs = getPrefixSQL(ch, session);
        CharArray.push(ch);
        return strs;
    }
    
    /** Prefix を考慮した SQLデータを返す 
        @param key SQLキー
        @param session SessionObject
        @return SQLデータ
    */
    private static String[] getPrefixSQL(CharArray key, SessionObject session) {
if (debugPrefix) System.out.println("getPrefixSQL:"+key+" session:"+(session!=null));
        CharArray chKey = CharArray.pop();
        String[]  strs = null;
        if (session != null) {
            if (session.szSQLPrefix.length() > 0) {
                chKey.set(session.szSQLPrefix).add(key);
if (debugPrefix) System.out.println("getPrefixSQL(session):"+chKey);
                strs = (String[])sqlHash.get(chKey);
            }
            if (strs == null) {
                ModuleManager mm = session.getModuleManager();
                if (mm != null && mm.szSQLPrefix.length() > 0) {
                    chKey.set(mm.szSQLPrefix).add(key);
if (debugPrefix) System.out.println("getPrefixSQL(module):"+chKey);
                    strs = (String[])sqlHash.get(chKey);
                }
            }
        }
        if (strs == null) {
            chKey.set(key);
            strs = ((String[])sqlHash.get(chKey));
        }
        CharArray.push(chKey);
        return strs;
    }
    
    /** Prefix を考慮した SQLデータを返す 
        @param key SQLキー
        @param site_code サイトコード
        @return SQLデータ
    */
    private static String[] getPrefixSQL(String key, int site_code) {
        CharArray ch = CharArray.pop(key);
        String[] strs = getPrefixSQL(ch, site_code);
        CharArray.push(ch);
        return strs;
    }
    /** Prefix を考慮した SQLデータを返す 
        @param key SQLキー
        @param site_code サイトコード
        @return SQLデータ
    */
    private static String[] getPrefixSQL(CharArray key, int site_code) {
if (debugPrefix) System.out.println("getPrefixSQL:"+key+" site_code:"+site_code);
        CharArray chKey = CharArray.pop();
        String[]  strs = null;
        
        ModuleManager mm = SiteManager.get(site_code);
        
        if (mm != null && mm.szSQLPrefix.length() > 0) {
            chKey.set(mm.szSQLPrefix).add(key);
if (debugPrefix) System.out.println("getPrefixSQL(module):"+chKey);
            strs = (String[])sqlHash.get(chKey);
        }
        
        if (strs == null) {
            chKey.set(key);
            strs = ((String[])sqlHash.get(chKey));
        }
        CharArray.push(chKey);
        return strs;
    }
    


    /** DataConnection を取得する
        @param key SQLキー
        @param site_code サイトコード
        @return DataConnection
    */
    public static DataConnection getDataConnection(String key, int site_code) { 
if (debugConnection) System.out.println("Connection0【"+key+"】site_code:"+site_code); 
        if (key == null || key.length()==0) {
            key = defaultKey;
if (debugConnection) System.out.println(" ->default【"+key+"】"); //hash="+(sqlHash != null));
        }
        
        /**
        CharArray chKey = CharArray.pop(key);
        String[]  strs = ((String[])sqlHash.get(chKey));
        CharArray.push(chKey);
        **/
        String[] strs = getPrefixSQL(key, site_code);  // 20110-06-14
        
//if (debug) System.out.println("-- strs[]="+(strs != null));
if (debugConnection && strs != null) System.out.println("connect="+strs[0]);
        String connect = (strs != null) ? strs[0] : "";
        DataConnection dc = (DataConnection)connectionHash.get(connect);
if (debugConnection) System.out.println("dc = "+(dc != null)+" conection="+connect);
        if (dc == null) {
            debugConnectionHash();
            if (site_code > 0) {
                ModuleManager mm = SiteManager.get(site_code);
                if (mm != null) connect = mm.defaultConnection;
            }
            if (connect == null || connect.length()==0) connect = DataConnection.defaultConnection;
        
            dc = (DataConnection)connectionHash.get(connect);
if (debugConnection) System.out.println("dc = "+(dc != null)+" connection="+connect);
        }
        return dc;

    }

    
    /** DataConnection を取得する   
        @param key SQLキー
        @param session SessionObject
        @return DataConnection
    */
    public static DataConnection getDataConnection(String key, SessionObject session) { 
if (debugConnection) System.out.println("Connection1【"+key+"】"); //hash="+(sqlHash != null));
        if (key == null || key.length()==0) {
            key = defaultKey;
if (debugConnection) System.out.println(" ->default【"+key+"】"); //hash="+(sqlHash != null));
        }
        String[] strs = getPrefixSQL(key, session);
if (debugConnection) System.out.println("connect="+strs[0]);
        String connect = strs[0];
        DataConnection dc = (DataConnection)connectionHash.get(connect);
if (debugConnection) System.out.println("dc = "+(dc != null)+" conection="+connect);
        if (dc == null) {
            debugConnectionHash();
            if (session != null) {
                connect = session.getDefaultConnection();
            }
            if (connect == null || connect.length()==0) connect = DataConnection.defaultConnection;
        
            dc = (DataConnection)connectionHash.get(connect);
if (debugConnection) System.out.println("dc = "+(dc != null)+" connection="+connect);
        }
        return dc;
    }

    /** DataConnection を取得する   
        @param key SQLキー
        @return DataConnection
    */
    public static DataConnection getDataConnection(String key) { 
if (debugConnection) System.out.println("Connection2【"+key+"】"); //hash="+(sqlHash != null));
        if (key == null || key.length()==0) {
            key = defaultKey;
if (debugConnection) System.out.println(" ->default【"+key+"】"); //hash="+(sqlHash != null));
        }
        CharArray chKey = CharArray.pop(key);
        String[]  strs = ((String[])sqlHash.get(chKey));
        CharArray.push(chKey);
//if (debug) System.out.println("-- strs[]="+(strs != null));
        String connect = strs[0];
        DataConnection dc = (DataConnection)connectionHash.get(connect);
if (debugConnection) System.out.println("dc = "+(dc != null)+" connection="+connect);
        return dc;
    }
    
    /** DataConnection を取得する   
        @param key SQLキー
        @param session SessionObject
        @return DataConnection
    */
    public static DataConnection getDataConnection(CharArray key, SessionObject session) { 
if (debugConnection) System.out.println("Connection3【"+key+"】"); //hash="+(sqlHash != null));
        if (key == null || key.length()==0) {
            if (key==null) key = new CharArray();
            key.set(defaultKey);
if (debugConnection) System.out.println(" ->default【"+key+"】"); //hash="+(sqlHash != null));
        }
        String[]  strs = getPrefixSQL(key, session);
        
if (debug) System.out.println("connect="+strs[0]);
        String connect = strs[0];
        DataConnection dc = (DataConnection)connectionHash.get(connect);
if (debug) System.out.println("dc = "+(dc != null)+" conection="+connect);
        if (dc == null) {
            debugConnectionHash();
            if (session != null) {
                connect = session.getDefaultConnection();
if (debug) System.out.println("site コネクション = "+connect);
            }
            if (connect == null || connect.length()==0) {
                connect = DataConnection.defaultConnection;
if (debug) System.out.println("ccm コネクション = "+connect);
            }
            dc = (DataConnection)connectionHash.get(connect);
if (debug) System.out.println("dc = "+(dc != null)+" connection="+connect);
        }
        return dc;
    }
    
    /** DataConnection を取得する   
        @param key SQLキー
        @return DataConnection
    */
    public static DataConnection getDataConnection(CharArray key) { 
if (debugConnection) System.out.println("Connection4【"+key+"】"); //hash="+(sqlHash != null));
        if (key == null || key.length()==0) {
            if (key==null) key = new CharArray();
            key.set(defaultKey);
if (debugConnection) System.out.println(" ->default【"+key+"】"); //hash="+(sqlHash != null));
        }
        String[]  strs = ((String[])sqlHash.get(key));
//if (debug) System.out.println("-- strs[]="+(strs != null));
        String connect = strs[0];
        DataConnection dc = (DataConnection)connectionHash.get(connect);
if (debug) System.out.println("dc = "+(dc != null)+" connection="+connect);
        return dc;
    }
    
    
    /** SQLキーワードよりSQLを取得する 
        @param key SQLキーワード
        @return SQL
    */
    public static String getSQL(String key) {
        if (key == null) return null;
        CharArray chKey = CharArray.pop(key);
        String[] strs = (String[])sqlHash.get(chKey);
        CharArray.push(chKey);
        if (strs == null) {
            System.out.println("●●そのSQL("+key+")は存在しません！");
            return "";
        }
        return strs[1];
    }

    /** SQLキーワードよりSQLを取得する 
        @param key SQLキーワード
        @return SQL
    */
    public static String getSQL(CharArray key) {
        if (key == null) return null;
        String[] strs = (String[])sqlHash.get(key);
        if (strs == null) {
            System.out.println("strs== null!!("+key+")");
            return "";
        }
        return strs[1];
    }

    /** SQLキーワードよりSQLを取得する 
        @param key SQLキーワード
        @param session SessionObject
        @return SQL
    */
    public static String getSQL(String key, SessionObject session) {
        if (key == null) return null;
        String[] strs = getPrefixSQL(key, session);
        if (strs == null) {
            System.out.println("○●そのSQL("+key+")は存在しません！");
            try {
                throw new Exception("SQL Not Found");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            return "";
        }
        return strs[1];
    }

    /** SQLキーワードよりSQLを取得する 
        @param key SQLキーワード
        @param session SessionObject
        @return SQL
    */
    public static String getSQL(CharArray key, SessionObject session) {
        if (key == null) return null;
        String[] strs = getPrefixSQL(key, session);
        if (strs == null) {
            System.out.println("●○そのSQL("+key+")は存在しません！");
            return "";
        }
        return strs[1];
    }

    /** SQLキーワードよりSQLを取得する 
        @param key SQLキーワード
        @param site_code サイトコード
        @return SQL
    */
    public static String getSQL(String key, int site_code) {
        if (key == null) return null;
        String[] strs = getPrefixSQL(key, site_code);
        if (strs == null) {
            System.out.println("○●そのSQL("+key+")は存在しません！");
            try {
                throw new Exception("SQL Not Found");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            return "";
        }
        return strs[1];
    }

    /** SQLキーワードよりSQLを取得する 
        @param key SQLキーワード
        @param site_code サイトコード
        @return SQL
    */
    public static String getSQL(CharArray key, int site_code) {
        if (key == null) return null;
        String[] strs = getPrefixSQL(key, site_code);
        if (strs == null) {
            System.out.println("●○そのSQL("+key+")は存在しません！");
            return "";
        }
        return strs[1];
    }

    /**
        コネクションを取得する
        @param dc DataConnection
        @return Connection
    */
    public static Connection getConnection(DataConnection dc) { 
        try {
        
            long timer = Util.Timer();
            Connection conn = null;
            if (dc != null) {
                if (dc.lookupObject != null) {           // DataSource 使用
                    if (dc.dbUser != null && dc.dbPassword != null) {
                        if (debugConnection && debug) {
                            System.out.println("◆DataSource.getConnection("+dc.dbUser+","+dc.dbPassword+") "+dc.name);
                            dc.debug();
                        }
                        conn = ((DataSource)(dc.lookupObject)).getConnection(dc.dbUser, dc.dbPassword);
                    } else {
                        if (debugConnection && debug) {
                            System.out.println("◆DataSource.getConnection() "+dc.name);
                            dc.debug();
                            //debugSQLHash();
                        }
                        conn = ((DataSource)dc.lookupObject).getConnection();  // error が出る！
                    }
                } else {
                    if (dc.dbUser != null && dc.dbPassword != null) {
                        if (debugConnection) {
                            System.out.println("◆DriveManager.getConnection("+dc.dbURL+
                                ","+dc.dbUser+","+dc.dbPassword+") "+dc.name);
                            dc.debug();
                        }
                        conn = DriverManager.getConnection(dc.dbURL,dc.dbUser, dc.dbPassword);
                    } else {
                        if (debugConnection) {
                            System.out.println("◆DriveManager.getConnection("+dc.dbURL+") "+dc.name);
                            dc.debug();
                        }
                        conn = DriverManager.getConnection(dc.dbURL);
                    }
                }
                if (debugConnection) {
                    String st = (conn == null) ? " null !! " : "";
                    System.out.println("◇◆DataSource.getConnection("+dc.name+") "+st+"Lapse:"+Util.Lapse(timer));
                }
            } else {
                SystemManager.log.out(ServletLog.DC_ERROR," not found DC");
            }
            return conn;
        } catch (Exception e) {
            SystemManager.log.out(ServletLog.DC_ERROR,""+e);
            e.printStackTrace();
            return null;
        }
    }

    public static PreparedStatement getPreparedStatement(Connection conn, 
                                    String key, String sql, CharArrayQueue parameter) {
        return getPreparedStatement(conn, key, sql, parameter, null, null,true);
    }
    public static PreparedStatement getPreparedStatement(Connection conn, 
                    String key, String sql, CharArrayQueue parameter, DataConnection dc) {
        return getPreparedStatement(conn, key, sql, parameter, dc, null,true);
    }
    public static PreparedStatement getPreparedStatement(Connection conn, 
                    String key, String sql, CharArrayQueue parameter, DataConnection dc, SessionObject session) {
        return getPreparedStatement(conn, key, sql, parameter, dc, session,true);
    }
    /**
        PreparedStatement の取得  DataAccessObject より呼ばれる
        @param conn   コネクション
        @param key    SQLキー
        @param sql    SQL nullの場合はkey登録されているものを使用する
        @param parameter SQLパラメータ
        @param dc DataConnection
        @param session SessionObject
        @param debugSQL SQLのデバッグ表示をするか？
        @return PreparedStatement
    */ 
    public static PreparedStatement getPreparedStatement(Connection conn, 
                    String key, String sql, CharArrayQueue parameter, DataConnection dc, SessionObject session,
                    boolean debugSQL) {
        CharArray ch = CharArray.pop();
        try{
            long timer = Util.Timer();
            int convert = 2;
            if (dc != null) convert = dc.outputConvertMode;
            if (sql == null) {
                CharArray chKey = CharArray.pop(key);
                sql = ((String[])sqlHash.get(chKey))[1];
                CharArray.push(chKey);
            } 
            if (debugParameter && debugSQL) {
                if (session != null) {
                    //ch.add(""+session.count+"|");
                    ch.format(session.count);
                    ch.add('|');
                }
                ch.add('【');ch.add(key); ch.add('】');
                if (dc != null) ch.add(dc.name);
                ch.add("\n");
                if (session != null) {
                    //ch.add(""+session.count+"|");
                    ch.format(session.count);
                    ch.add('|');
                }
                ch.add(sql);    //\nconn:"+(conn!=null));
                ch.add("\n");
            }
            //PreparedStatement ps = conn.prepareStatement(sql);
            PreparedStatement ps = conn.prepareStatement(sql,
                        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            if (parameter != null) {
                for (int i = 1; i <= parameter.size(); i++){
                    if (debugParameter && debugSQL) {
                        if (session != null) {
                            //ch.add(""+session.count+"|");
                            ch.format(session.count);
                            ch.add('|');
                        }
                        //ch.add(" Param["+i+"/"+parameter.size()+"]"+parameter.peek(i-1)+"\n");
                        ch.add(" Param[");
                        ch.format(i);
                        ch.add('/');
                        ch.format(parameter.size());
                        ch.add(']');
                        ch.add(parameter.peek(i-1));
                        ch.add("\n");
                    }
                    CharArray ca = parameter.peek(i-1);
//if (debug) System.out.println("★★★DataSourceManager.convert="+convert+" "+ca+"["+Queue.getTypeName(ca.type)+"]");
                    if (convert == 0)    ps.setString(i,parameter.peek(i-1).toString());
                    else if (convert==1) ps.setString(i,parameter.peek(i-1).toCp932().toString());
                    else if (convert==2) ps.setString(i,parameter.peek(i-1).toJIS().toString());
                    else if (convert==3) ps.setString(i,parameter.peek(i-1).toCp9322().toString());
                    else if (convert==4) ps.setString(i,parameter.peek(i-1).toJIS2().toString());
                    else {
                        if (ca.type == Queue.INT) {
                            ps.setInt(i, ca.getInt());
                        } else if (ca.type == Queue.LONG) {
                            ps.setLong(i, ca.getLong());
                        } else if (ca.type == Queue.DOUBLE) {
                            ps.setDouble(i, ca.getDouble());
                        } else if (ca.type == Queue.BOOL) {
                            ps.setBoolean(i, ca.getBoolean());
                        } else {
                            ps.setString(i,ca.toString());
                        }
                    }
                }
            } else if (debugParameter && debugSQL) {
                if (session != null) ch.add(""+session.count+"|");
                ch.add(" Param:-none-\n");
            }
            if (debugConnect && debugSQL) {
                if (session != null) ch.add(""+session.count+"|");
                ch.add("◇-- getPreparedStatement lapse:");
                ch.format(Util.Lapse(timer));
                ch.add("\n");
            }
            if (ch.length() > 0) System.out.print(ch.toString());
            CharArray.push(ch);
            return ps;
        } catch(Exception e) {
            if (ch.length() > 0) System.out.print(ch.toString());
            CharArray.push(ch);
            SystemManager.log.out(ServletLog.DS_ERROR," getPreparedStatement:"+e);
            e.printStackTrace();
            return null;
        } finally {
        }

    }

    /** デバッグ用関数 */
    public static void debugSQLHash() {
        System.out.println("SQL hash count="+sqlHash.size()+"---------------");
        for (Enumeration e = sqlHash.keys(); e.hasMoreElements();) {
            CharArray key = (CharArray)e.nextElement();
            String[] data = (String[])sqlHash.get(key);
            System.out.println("【"+key+"】"+data[0]+" : "+data[1]);
        }
        System.out.println("SQL hash -------------------------");
    }
    /** デバッグ用関数 
        @param key キーワード
    */
    public static void debugSQLHash(CharArray key) {
        String[] data = (String[])sqlHash.get(key);
        System.out.println("【"+key+"】"+data[0]+" : "+data[1]);
    }

    /** デバッグ用関数 */
    public static void debugConnectionHash() {
        if (debugConnection) {
            System.out.println("Connection hash count="+connectionHash.size()+"---------------");
            for (Enumeration e = connectionHash.keys(); e.hasMoreElements();) {
                String key = (String)e.nextElement();
                System.out.println("【"+key+"】");
            }
            System.out.println("Connection hash -------------------------");
        }
    }
    

}

//
// [end of DataSourceManager.java]
//

