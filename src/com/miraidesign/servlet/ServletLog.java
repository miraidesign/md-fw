//------------------------------------------------------------------------
//    ServletLog.java
//         ログ出力クラス
//         Copyright (c) MiraiDesign, Inc. 2010 All Rights Reserved.
//------------------------------------------------------------------------
//
package com.miraidesign.servlet;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.IntQueue;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.IniFile;
import com.miraidesign.util.LogCategory;

/**
 *  ServletLog 
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ServletLog {
    protected boolean debug = false;         // デバッグ表示

                                    // 以下は ccm.ini と合わせる事
    // FATAL-----------------------------------------------------
    static final public int FILE_NOT_FOUND    = 5000;
    // ERROR----------------------------------------------------
    static final public int SYSTEM_ERROR      = 4000;
    static final public int SQL_EXCEPTION     = 4001;
    static final public int XML_EXCEPTION     = 4002;
    static final public int DC_ERROR          = 4003;
    //static final public int EJB_ERROR         = 4004;
    // WARN-----------------------------------------------------
    static final public int EXCEPTION         = 3000;
    static final public int NULL_POINTER      = 3001;
    static final public int SESSION_NOT_FOUND = 3002;
    static final public int PAGE_NOT_FOUND    = 3003;
    static final public int UA_NOT_FOUND      = 3004;
    static final public int SS_NOT_FOUND      = 3005;
    static final public int SITE_NOT_FOUND    = 3006;
    static final public int ID_NOT_FOUND      = 3007;
    static final public int PW_NOT_FOUND      = 3008;
    static final public int SESSION_LOAD_ERROR  = 3009;
    static final public int DS_ERROR          = 3010;
    static final public int DAO_ERROR         = 3011;
    static final public int TEMPLATE_ERROR    = 3012;
    static final public int BLOCK_ERROR       = 3013;
    static final public int CART_SAVE_ERROR   = 3014;
    static final public int CART_LOAD_ERROR   = 3015;
    static final public int SESSION_COUNT_OVER = 3016;
    // INFO-----------------------------------------------------
    // DEBUG-----------------------------------------------------
    static final public int NORMAL            = 1000;



    private LogCategory cat;
    private String   hostname = "";
    private String   identify = "C";

    private Hashtable<CharArray,IntObject> hashKey = 
        new Hashtable<CharArray,IntObject>();    // key, IntObject(ID)
    private Hashtable<IntObject,CharArray> hashMessage  = 
        new Hashtable<IntObject,CharArray>();    // IntObject(ID), message
    private Hashtable<IntObject,Priority> hashPriority = 
        new Hashtable<IntObject,Priority>();    // IntObject(ID), Priority

    private static ServletLog instance; // singleton
    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------
    private ServletLog() {
        //cat = LogCategory.getInstance(ServletLog.class.getName());
        cat = new LogCategory(ServletLog.class.getName());
        
        //cat.setFQCN(ServletLog.class.getName());
        debug &= SystemConst.debug;
    }
    
    public static ServletLog getInstance() {
        if (instance == null) {
            instance = new ServletLog();
        }
        return instance;
    }
    
    //---------------------------------------------------------
    // Setter
    //---------------------------------------------------------
    public void init(IniFile ini) {    
        // log4jコンフィグレーション
//        if (true) return;
        if (debug) System.out.println("ServletLog Init-------------------");
        Properties prop = new Properties();
        for (Enumeration e = ini.getKeyList("[Log]"); e.hasMoreElements(); ) {
            CharArray key   = (CharArray)e.nextElement();
            CharArray value = CharArray.convertProperty(ini.get("[Log]",key));
            value.replace("\\","/");
            
            if (debug) System.out.println("ServletLog:Log:"+key+"="+value);
            prop.setProperty(key.toString(), value.toString());
        }
        PropertyConfigurator configure=new PropertyConfigurator();
        // for log4j 1.2.8
        configure.doConfigure(prop, cat.getLoggerRepository());
        if (debug) System.out.println("---------------------------------");
        // Messageコンフィグレーション
        CharToken token = CharToken.pop();
        for (Enumeration e = ini.getKeyList("[Message]"); e.hasMoreElements(); ) {
            CharArray key   = (CharArray)e.nextElement();
            CharArray value = ini.get("[Message]",key);
            if (debug) System.out.println("ServletLog:Message:"+key+"="+value);
            token.set(value);
            CharArray chPriority = token.get(0);
            int id = token.getInt(1);
            CharArray mes = token.get(2);
            IntObject obj = new IntObject(id);
            Priority priority = Priority.DEBUG;
            if (chPriority.equals("FATAL")) priority = Priority.FATAL;
            else if (chPriority.equals("ERROR")) priority = Priority.ERROR;
            else if (chPriority.equals("WARN")) priority = Priority.WARN;
            else if (chPriority.equals("INFO")) priority = Priority.INFO;
            else if (chPriority.equals("DEBUG")) priority = Priority.DEBUG;
            else {
                System.out.println("Priority が違うので DEBUGに設定します:"+id+" "+mes);
            }
            hashKey.put(key,obj);
            hashMessage.put(obj,new CharArray(mes));
            hashPriority.put(obj,priority);
        }
        CharToken.push(token);
        if (debug) System.out.println("ServletLog end-----------------------");
    }
    
    
    /** ホスト名を設定する 
        @param str ホスト名
    **/
    public void setHostName(String str) {
        this.hostname = str;
    }
    
    /** 識別子を設定する 
        @param str 識別子 ( "C", "M", "I",...)
    */
    public void setIdentify(String str) {
        this.identify = str;
    }
    
    //---------------------------------------------------------
    // log
    //---------------------------------------------------------
    private Priority getMessage(CharArray ch, String key) {
        Priority priority = null;
        if (hostname.length() > 0) {
            ch.add(hostname);
            ch.add(' ');
        }
        ch.add(identify);
        IntObject obj = (IntObject)hashKey.get(key);
        if (obj == null) {
            ch.add("---- ");
        } else {
            ch.format(obj.getValue(),10,4,'0');
            ch.add(' ');
            CharArray mes = (CharArray)hashMessage.get(obj);
            if (mes != null && mes.length()>0) {
                ch.add(mes);
                ch.add(':');
            }
            priority = (Priority)hashPriority.get(obj);
        }
        return priority;
    }
    private Priority getMessage(CharArray ch) {
        return getMessage(ch, -1);
    }
    private Priority getMessage(CharArray ch, int id) {
        Priority priority = null;
        if (hostname.length() > 0) {
            ch.add(hostname);
            ch.add(' ');
        }
        ch.add(identify);
        if (id < 0) {
            ch.add("---- ");
        } else {
            IntObject obj = IntObject.pop(id);
            ch.format(id,10,4,'0');
            ch.add(' ');
            CharArray mes = (CharArray)hashMessage.get(obj);
            if (mes != null && mes.length > 0) {
                ch.add(mes);
                ch.add(':');
            }
            priority = (Priority)hashPriority.get(obj);
            IntObject.push(obj);
        }
        return priority;
    }
    
    //-------------------------------------------
    /** メッセージを出力する 
        @param key  メッセージ定義キー文字列
        @param str  メッセージ
    */
    public void log(String key, String str) {
        return;
        
        //CharArray ch = CharArray.pop();
        //Priority priority = getMessage(ch, key);
        //ch.add(str);
        //cat.log((priority != null) ? priority : Priority.DEBUG, ch.toString());
        //CharArray.push(ch);
    }
    /** メッセージを出力する 
        @param key  メッセージ定義キー文字列
    */
    public void log(String key) {
        log(key, "");
    }
    /** メッセージを出力する 
        @param key  メッセージ定義キー文字列
    */
    public void log(CharArray key) {
        log(""+key, "");
    }
    /** メッセージを出力する 
        @param id   メッセージ定義ＩＤ
        @param str  メッセージ
    */
    public void log(int id, String str) {
        if (id > 0) {
            CharArray ch = CharArray.pop();
            Priority priority = getMessage(ch, id);
            ch.add(str);
            cat.log((priority != null) ? priority : Priority.DEBUG, ch.toString());
            CharArray.push(ch);
        }
    }
    /** メッセージを出力する 
        @param id   メッセージ定義ＩＤ
    */
    public void log(int id) {
        if (id > 0) {
            log(id, "");
        }
    }
    
    /** log と一緒です */
  
    public void out(String key, String str) {
        log(key,str);
    }
    public void out(String key) {
        log(key);
    }
    public void out(CharArray key) {
        log(key);
    }
    public void out(int id, String str) {
        log(id,str);
    }
    public void out(int id) {
        if (id > 0) log(id);
    }
    
    
    //-------------------------------------------
    /** 致命的エラーメッセージを出力する 
        @param str  メッセージ
    */
    public void fatal(String str) {
        CharArray ch = CharArray.pop();
        getMessage(ch);
        ch.add(str);
        cat.fatal(ch.toString());
        CharArray.push(ch);
    }
    /** 致命的エラーメッセージを出力する 
        @param str  メッセージ
    */
    public void fatal(CharArray str) {
        CharArray ch = CharArray.pop();
        getMessage(ch);
        ch.add(str);
        cat.fatal(ch.toString());
        CharArray.push(ch);
    }
    /** 致命的エラーメッセージを出力する 
        @param key  メッセージ定義キー文字列
        @param str  メッセージ
    */
    public void fatal(String key, String str) {
        CharArray ch = CharArray.pop();
        getMessage(ch, key);
        ch.add(str);
        cat.fatal(ch.toString());
        CharArray.push(ch);
    }
    /** 致命的エラーメッセージを出力する 
        @param id   メッセージ定義ＩＤ
        @param str  メッセージ
    */
    public void fatal(int id, String str) {
        if (id > 0) {
            CharArray ch = CharArray.pop();
            getMessage(ch, id);
            ch.add(str);
            cat.fatal(ch.toString());
            CharArray.push(ch);
        }
    }
    //-------------------------------------------
    /** エラーメッセージを出力する 
        @param str  メッセージ
    */
    public void error(String str) {
        CharArray ch = CharArray.pop();
        getMessage(ch);
        ch.add(str);
        cat.error(ch.toString());
        CharArray.push(ch);
    }
    /** エラーメッセージを出力する 
        @param str  メッセージ
    */
    public void error(CharArray str) {
        CharArray ch = CharArray.pop();
        getMessage(ch);
        ch.add(str);
        cat.error(ch.toString());
        CharArray.push(ch);
    }
    /** エラーメッセージを出力する 
        @param key  メッセージ定義キー文字列
        @param str  メッセージ
    */
    public void error(String key, String str) {
        CharArray ch = CharArray.pop();
        getMessage(ch, key);
        ch.add(str);
        cat.error(ch.toString());
        CharArray.push(ch);
    }
    /** エラーメッセージを出力する 
        @param id   メッセージ定義ＩＤ
        @param str  メッセージ
    */
    public void error(int id, String str) {
        if (id > 0) {
            CharArray ch = CharArray.pop();
            getMessage(ch, id);
            ch.add(str);
            cat.error(ch.toString());
            CharArray.push(ch);
        }
    }
    //-------------------------------------------
    /** ワーニングメッセージを出力する 
        @param str  メッセージ
    */
    public void warn(String str) {
        CharArray ch = CharArray.pop();
        getMessage(ch);
        ch.add(str);
        cat.warn(ch.toString());
        CharArray.push(ch);
    }
    /** ワーニングメッセージを出力する 
        @param str  メッセージ
    */
    public void warn(CharArray str) {
        CharArray ch = CharArray.pop();
        getMessage(ch);
        ch.add(str);
        cat.warn(ch.toString());
        CharArray.push(ch);
    }
    /** ワーニングメッセージを出力する 
        @param key  メッセージ定義キー文字列
        @param str  メッセージ
    */
    public void warn(String key, String str) {
        CharArray ch = CharArray.pop();
        getMessage(ch, key);
        ch.add(str);
        cat.warn(ch.toString());
        CharArray.push(ch);
    }
    /** ワーニングメッセージを出力する 
        @param id   メッセージ定義ＩＤ
        @param str  メッセージ
    */
    public void warn(int id, String str) {
        if (id > 0) {
            CharArray ch = CharArray.pop();
            getMessage(ch, id);
            ch.add(str);
            cat.warn(ch.toString());
            CharArray.push(ch);
        }
    }
    //-------------------------------------------
    /** インフォメーションメッセージを出力する 
        @param str  メッセージ
    */
    public void info(String str) {
        CharArray ch = CharArray.pop();
        getMessage(ch);
        ch.add(str);
        cat.info(ch.toString());
        CharArray.push(ch);
    }
    /** インフォメーションメッセージを出力する 
        @param str  メッセージ
    */
    public void info(CharArray str) {
        CharArray ch = CharArray.pop();
        getMessage(ch);
        ch.add(str);
        cat.info(ch.toString());
        CharArray.push(ch);
    }
    /** インフォメーションメッセージを出力する 
        @param key  メッセージ定義キー文字列
        @param str  メッセージ
    */
    public void info(String key, String str) {
        CharArray ch = CharArray.pop();
        getMessage(ch, key);
        ch.add(str);
        cat.info(ch.toString());
        CharArray.push(ch);
    }
    /** インフォメーションメッセージを出力する 
        @param id   メッセージ定義ＩＤ
        @param str  メッセージ
    */
    public void info(int id, String str) {
        if (id > 0) {
            CharArray ch = CharArray.pop();
            getMessage(ch, id);
            ch.add(str);
            cat.info(ch.toString());
            CharArray.push(ch);
        }
    }
    //-------------------------------------------
    /** デバッグメッセージを出力する 
        @param str  メッセージ
    */
    public void debug(String str) {
        CharArray ch = CharArray.pop();
        getMessage(ch);
        ch.add(str);
        cat.debug(ch.toString());
        CharArray.push(ch);
    }
    /** デバッグメッセージを出力する 
        @param str  メッセージ
    */
    public void debug(CharArray str) {
        CharArray ch = CharArray.pop();
        getMessage(ch);
        ch.add(str);
        cat.debug(ch.toString());
        CharArray.push(ch);
    }
    /** デバッグメッセージを出力する 
        @param key  メッセージ定義キー文字列
        @param str  メッセージ
    */
    public void debug(String key, String str) {
        CharArray ch = CharArray.pop();
        getMessage(ch, key);
        ch.add(str);
        cat.debug(ch.toString());
        CharArray.push(ch);
    }
    /** デバッグメッセージを出力する 
        @param id   メッセージ定義ＩＤ
        @param str  メッセージ
    */
    public void debug(int id, String str) {
        if (id > 0) {
            CharArray ch = CharArray.pop();
            getMessage(ch, id);
            ch.add(str);
            cat.debug(ch.toString());
            CharArray.push(ch);
        }
    }
    //-------------------------------------------
    public void trace(String str) { debug(str);}
    public void trace(CharArray str) { debug(str);}
}

//
// [end of ServletLog.java]
//


