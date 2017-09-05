//------------------------------------------------------------------------
//    DataConnection.java
//              Data接続情報を管理する
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
package com.miraidesign.data;

import java.util.Hashtable;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.IniFile;

/**
 *  Data接続情報を管理する
 *  
 *  @version 1.0 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
 
public class DataConnection {
    static private boolean debug = (SystemConst.debug && true);  // デバッグ表示
    static protected Hashtable<String, DataConnection> hash = 
                 new Hashtable<String, DataConnection>();

    public String name;         // 接続名
    //public DataSource dataSource;   // データソース
    public Object lookupObject;  // データソース
    
    public String factory;      // ContextFactory
    public String packages;     // JNDI packages
    public String state;        // JNDI state
    
    public String dataSourceClass;   // JNDI DataSource クラス
    
    public String protocol;     // JNDI protocol
    public String providerURL;  // JNDI provider URL
    public String providerPort; // JNDI provider Port
    public String authUser;     // JNDI 
    public String authPassword; // JNDI 
    
    public String lookUp;   // 
    public String dbDriver;
    public String dbURL;
    public String dbUser;
    public String dbPassword;

    /** prepared statement で特殊文字の変換を行う */
    //public boolean convertCp932toJIS = true;
    public int outputConvertMode = 2;   //  0: do nothing  1 :toCp932  [2]:toJIS
    /** DB読み込み時に特殊文字の変換を行う */
    public int inputConvertMode  = 1;   //  0: do nothing [1]:toCp932   2 :toJIS

    public void debug() {
        if (!debug) return;
        System.out.println("name="+name);
        System.out.println("lookupObject="+lookupObject);
        System.out.println("factory="+factory);
        System.out.println("packages="+packages);
        System.out.println("state="+state);
        System.out.println("dataSourceClass="+dataSourceClass);
        System.out.println("protocol="+protocol);
        System.out.println("providerURL="+providerURL);
        System.out.println("providerPort="+providerPort);
        System.out.println("authUser="+authUser);
        System.out.println("authPassword="+authPassword);
        System.out.println("lookUp="+lookUp);
        System.out.println("dbDriver="+dbDriver);
        System.out.println("dbURL="+dbURL);
        System.out.println("dbUser="+dbUser);
        System.out.println("dbPassword="+dbPassword);
    
    }

    
    private static final String sec = "[DataSource]";  // セクション
    
    protected static String defaultConnection="";
    
    protected static HashParameter dataSourceByURL = new HashParameter();
    
    /** 
        URL に設定されたDataSourceがあれば返す（無い場合は null) 
        @param url チェックするURL
        @return データソース
    */
    public static CharArray getDataSourceByURL(CharSequence url) {
        CharArray rsts = null;
        if (dataSourceByURL.size() > 0) {
            CharArray ch = CharArray.pop(url);
            ch.trim().toLowerCase();
            if (ch.length() > 0) {
                for (int i = 0; i < dataSourceByURL.size(); i++) {
                    CharArray key = dataSourceByURL.keyElementAt(i);
                    if (ch.startsWith(key)) {
                        CharArray data = dataSourceByURL.valueElementAt(i);
if (debug) {
    System.out.println("dataSourceByURL: matched !! :"+key);
    //System.out.println("dataSourceByURL: url:"+url);
    //System.out.println("dataSourceByURL: key:"+key);
    System.out.println("dataSourceByURL: DataSource :"+data);

}
                        rsts = new CharArray(data);
                        break;
                    }
                }
            }
            CharArray.push(ch);
        }
        return rsts;
    }
    
    /**
    public String getDefalutConnection(SessionObject session) {
        // 実装する
    
        return defaultConnection;
    }
    **/
    
    
    
    
    private DataConnection() {}
    
    //-------------------------------------------------------------
    /**
        データコネクションオブジェクトを取得する
        @param key データコネクション名
        @return DataConnectionオブジェクト or null
    */
    public static DataConnection getDataConnection(String key) {
        if (key == null || key.length()==0) {
            //System.out.println("●◆●◆KEY changed["+key+"->"+defaultConnection+"]");
            //key = defaultConnection;
        }
        return (DataConnection)hash.get(key);
    }
    
    public static int size() { return hash.size();}
    public static void init(IniFile ini) {
        if (ini.isOK()) {
            //String str = ini.getString(sec,"Default");
            //if (str.length() > 0) {
            //    set(ini,str);
            //}
            
            defaultConnection = ini.getString(sec,"Default"); 
            
            CharArray chList = ini.get(sec,"UseSourceList");
            if (chList != null) {
                CharToken token = CharToken.pop(chList);
                for (int i = 0; i < token.size(); i++) {
                    CharArray name = token.get(i).trim();
                    if (name.length()>0) {
                        String str = name.toString();
                        set(ini,str);
                    }
                } // next
            }

            // dataSourceByURL の設定 --------------------------------------------
            dataSourceByURL.clear();
            HashVector hv = ini.getKeyTable(sec);
            if (hv != null) {
                CharToken token = CharToken.pop();
                for (int i = 0; i < hv.size(); i++) {
                    CharArray key   = (CharArray)hv.keyElementAt(i);
                    CharArray value = ((CharArrayQueue)hv.valueElementAt(i)).peek();
                    // CharArray#appendMode をセッション自動変換フラグに使う
                    
                    if (key.startsWith("http://") || key.startsWith("https://")) {
                        token.set(value, ",");
                        CharArray ds = token.get(0).trim();
                        CharArray ca = new CharArray(token.get(0).trim());
                        ca.setAppendMode(CharArray.getBoolean(token.get(1)));
                        dataSourceByURL.add(key.toLowerCase(), ca);
                    }
                }
if (debug) {
    System.out.println("-DataSource by URL ----------------");
    dataSourceByURL.debugParameter(true);
    System.out.println("-----------------------------------");
}
                CharToken.push(token);
            }
            //--------------------------------------------------------------------
            
        }
    }
    
    private static void set(IniFile ini, String str) {
        if (!hash.containsKey(str)) {
            CharArray ch;
            DataConnection data = new DataConnection();
            data.name = str;
            ch = ini.get(sec,str+".Factory");
            if (ch != null && ch.length() > 0) data.factory = ch.toString();
            ch = ini.get(sec,str+".Packages");
            if (ch != null && ch.length() > 0) data.packages = ch.toString();
            ch = ini.get(sec,str+".State");
            if (ch != null && ch.length() > 0) data.state = ch.toString();
            ch = ini.get(sec,str+".DataSourceClass");
            if (ch != null && ch.length() > 0) data.dataSourceClass = ch.toString();
            ch = ini.get(sec,str+".Protocol");
            if (ch != null && ch.length() > 0) data.protocol= ch.toString();
            ch = ini.get(sec,str+".ProviderURL");
            if (ch != null && ch.length() > 0) data.providerURL = ch.toString();
            ch = ini.get(sec,str+".ProviderPort");
            if (ch != null && ch.length() > 0) data.providerPort = ch.toString();
            ch = ini.get(sec,str+".AuthUser");
            if (ch != null && ch.length() > 0) data.authUser = ch.toString();
            ch = ini.get(sec,str+".AuthPassword");
            if (ch != null && ch.length() > 0) data.authPassword = ch.toString();
            ch = ini.get(sec,str+".LookUp");
            if (ch != null && ch.length() > 0) data.lookUp = ch.toString();
            ch = ini.get(sec,str+".dbDriver");
            if (ch != null && ch.length() > 0) data.dbDriver = ch.toString();
            ch = ini.get(sec,str+".dbURL");
            if (ch != null && ch.length() > 0) data.dbURL = ch.toString();
            ch = ini.get(sec,str+".dbUser");
            if (ch != null && ch.length() > 0) data.dbUser = ch.toString();
            ch = ini.get(sec,str+".dbPassword");
            if (ch != null && ch.length() > 0) data.dbPassword = ch.toString();

            //ch = ini.get(sec,str+".convertCp932toJIS");
            //if (ch != null && ch.trim().length() > 0) data.convertCp932toJIS = ch.getBoolean();
            ch = ini.get(sec,str+".outputConvertMode");
            if (ch != null && ch.trim().length() > 0) data.outputConvertMode = ch.getInt();
            ch = ini.get(sec,str+".inputConvertMode");
            if (ch != null && ch.trim().length() > 0) data.inputConvertMode = ch.getInt();

            hash.put(str,data);
            if (debug) {
                System.out.println("["+str+"]");
                System.out.println("Factory     ="+data.factory);
                System.out.println("Packages    ="+data.packages);
                System.out.println("State       ="+data.state);
                System.out.println("DataSourceClass  ="+data.dataSourceClass);
                System.out.println("Protocol    ="+data.protocol);
                System.out.println("ProviderURL ="+data.providerURL);
                System.out.println("ProviderPort="+data.providerPort);
                System.out.println("AuthUser    ="+data.authUser);
                System.out.println("AuthPassword="+data.authPassword);
                System.out.println("Lookup      ="+data.lookUp);
                System.out.println("dbDriver    ="+data.dbDriver);
                System.out.println("dbURL       ="+data.dbURL);
                System.out.println("dbUser      ="+data.dbUser);
                System.out.println("dbPassword  ="+data.dbPassword);
            }
            ch = ini.get(sec,str+".loadOnStartup");
            if (ch != null && ch.length() > 0) {
                if (ch.getBoolean()) DataSourceManager.setConnection("loadOnStartup",str);
            }
        }
    }
}

//
// [end of DataConnection.java]
//

