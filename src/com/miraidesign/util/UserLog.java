//------------------------------------------------------------------------
//    UserLog.java
//                 ログ出力クラス
//                 Copyright (c) Mirai Design 2010 All Rights Reserved.
//------------------------------------------------------------------------
//
package com.miraidesign.util;

import java.util.Enumeration;
import java.util.Properties;
import java.net.URL;
import java.net.Socket;
import java.net.ServerSocket;

import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.net.SocketNode;
import org.apache.log4j.xml.DOMConfigurator;


/**
 *  ユーザー指定ログ出力クラス(log4j)
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class UserLog {
    /** ログカテゴリー */
    private LogCategory cat;
    /** ログカテゴリークラスを取得する*/
    public LogCategory getLogCategory() { return cat;}
    
    private int port = 0;   // サーバー利用時のポート

    /** サーバーポートを取得する
        @return ポート番号(0でサーバー動作なし)
    */
    public int getPort() { return port;}
    
    /** constructor 1 */
    public UserLog(String section) {
        boolean sts = false;
        do {
            //cat = LogCategory.getInstance(UserLog.class.getName());
            cat = new LogCategory(UserLog.class.getName());
            //cat.setFQCN(UserLog.class.getName());
        
            IniFile ini = com.miraidesign.system.SystemManager.ini;
            if (ini == null) break;
            Properties prop = new Properties();
            Enumeration e = ini.getKeyList(section); if (e == null) break;
            
            for (; e.hasMoreElements(); ) {
                CharArray key   = (CharArray)e.nextElement();
                //CharArray value = ini.get(section,key);
                //int n = value.replaceCount("$(catalina.home)",System.getProperty("catalina.home"));
                //if (n > 0) value.replace("\\","/");
                
                CharArray value = CharArray.convertProperty(ini.get(section,key));
                value.replace("\\","/");
                
                prop.setProperty(key.toString(), value.toString());
            }
            //PropertyConfigurator.configure(prop);
            PropertyConfigurator configure = new PropertyConfigurator();
            // for log4j 1.1.3
            //configure.doConfigure(prop,cat.hierarchy);
            // for log4j 1.2.8
            configure.doConfigure(prop,cat.getLoggerRepository());
            
            if (port > 0) {
                try {
                    cat.info("Listening on port " + port);
                    ServerSocket serverSocket = new ServerSocket(port);
                    while (true) {
                        cat.info("Waiting to accept a new client.");
                        Socket socket = serverSocket.accept();
                        cat.info("Connected to client at " + socket.getInetAddress());
                        cat.info("Starting new socket node.");
                        new Thread(new SocketNode(socket,
                            LogManager.getLoggerRepository())).start();
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            sts = true;
        } while (false);
        if (!sts) System.out.println("UserLog"+section+"が生成できません");
    }
    
    /** Constructor2 */
    public UserLog(URL url) {
        //@@//cat = new LogCategory(UserLog.class.getName()); 
        cat = new LogCategory(url.toString()); 

        if (url.getFile().endsWith(".xml")) {    // 定義がXMLの場合
            DOMConfigurator config = new DOMConfigurator();
            // for log4j 1.1.3
            //config.doConfigure(url, cat.hierarchy);
            // for log4j 1.2.8
            config.doConfigure(url, cat.getLoggerRepository());
        } else {                            // 定義がPropertiesファイルの
            PropertyConfigurator config = new PropertyConfigurator();
            // for log4j 1.1.3
            //config.doConfigure(url, cat.hierarchy);
            // for log4j 1.2.8
            config.doConfigure(url, cat.getLoggerRepository());
        }
    }
    
    public UserLog(Properties prop) {
        //cat = LogCategory.getInstance(UserLog.class.getName());
        cat = new LogCategory(UserLog.class.getName());
        
        //cat.setFQCN(UserLog.class.getName());
                
        //PropertyConfigurator.configure(prop);
        PropertyConfigurator configure = new PropertyConfigurator();
        // for log4j 1.1.3
        //configure.doConfigure(prop,cat.hierarchy);
        // for log4j 1.2.8
        configure.doConfigure(prop,cat.getLoggerRepository());
    }
    
    //-------------------------------------------
    /** メッセージを出力する Priority.INFO
        @param str  メッセージ
    */
    public void out(String str) { cat.log(Priority.INFO, str);}
    public void log(String str) { cat.log(Priority.INFO, str);}


    /** 各レベルのメッセージを出力する 
        @param str  メッセージ
    */
    public void fatal(String str) { cat.fatal(str); }
    public void error(String str) { cat.error(str); }
    public void warn(String str) { cat.warn(str); }
    public void info(String str) { cat.info(str); }
    public void debug(String str) { cat.debug(str); }
    public void trace(String str) { cat.debug(str); }

}

//
// [end of UserLog.java]
//

