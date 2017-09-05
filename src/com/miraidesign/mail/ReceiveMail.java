//------------------------------------------------------------------------
//    ReceiveMail.java
//                 メール受信プログラム
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.mail;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

import com.miraidesign.servlet.ServletLog;

/**
  メール受信クラス
*/

public class ReceiveMail {
    // static section ------------------------------------------
    private boolean debug     = true;
    private boolean debugMode = false;
    
    private Session session;
    private Store   store;
    private Folder  inbox;
    
    // ユーザー設定
    private String protocol = "pop3";
    private String host;
    private int    port = 110;
    private String user;
    private String password;

    private String folder = "INBOX";

    /** JavaMail デバッグモードを設定する*/
    public void setDebugMode(boolean mode) { debugMode = mode; }
    /** プロトコル指定（デフォルト pop3）*/
    public void setProtocol(String protocol) {
        if (protocol != null && protocol.trim().length() > 0) {
            this.protocol = protocol;
        }
    }
    /** ホスト名かアドレスを設定*/
    public void setHost(String host) { 
        if (host != null && host.trim().length() > 0) {
            this.host = host; 
        }
    }
    /** ポート指定（デフォルト110）*/
    public void setPort(int port) { 
        if (port >= 0) this.port = port; 
    }
    /** ユーザー指定 */
    public void setUser(String user) { this.user = user; }
    /** パスワード指定 */
    public void setPassword(String password) { this.password = password;}
    /** フォルダ設定 (デフォルト INBOX) */
    public void setFolder(String str) { folder = str;}

    /** Constructor (initは行わない)*/
    public ReceiveMail() { 
        debug &= com.miraidesign.common.SystemConst.debug;
    }
    /** ReceiveMail オブジェクトの生成と初期化を行う
        @param host ホスト名
        @param user ユーザ名
        @param password パスワード
    */
    public ReceiveMail(String host,String user,String password) { 
        debug &= com.miraidesign.common.SystemConst.debug;
        this.host = host;
        this.user = user;
        this.password = password;
        init();
    }
    /** ReceiveMail オブジェクトの生成と初期化を行う
        @param host ホスト名
        @param user ユーザ名
        @param password パスワード
        @param protocol プロトコル
        @param port     ポート
    */
    public ReceiveMail(String host,String user,String password,
                       String protocol, int port) { 
        debug &= com.miraidesign.common.SystemConst.debug;
        this.host = host;
        this.user = user;
        this.password = password;
        setProtocol(protocol);
        setPort(port);
        init();
    }
    /** 初期化を行う */
    public boolean init() {
        boolean rsts = false;
        session = Session.getDefaultInstance(new Properties(), null);
        try {
            store = session.getStore(protocol);
            rsts = true;
        } catch (NoSuchProviderException ex) {
            ServletLog.getInstance().log("ReceiveMail#init():"+host+":"+port+" "+user+" "+password+" "+ex);
            ex.printStackTrace();
        }
        return rsts;
    }
    
    /** 接続する */
    public boolean connect() {
        boolean rsts = false;
        try {
            store.connect(host,port,user,password);
            Folder rootFolder = store.getDefaultFolder();
            inbox = rootFolder.getFolder(folder);
            //inbox.open(Folder.READ_ONLY);
            inbox.open(Folder.READ_WRITE);
            rsts = true;
        } catch (MessagingException ex) {
            ServletLog.getInstance().log("ReceiveMail#connect():"+host+":"+port+" "+user+" "+password+" "+ex);
            System.out.println("ReceiveMail#connect():"+host+":"+port+" "+user+" "+password);
            ex.printStackTrace();
        }
        return rsts;
    }
    
    /** メッセージを取得する */
    public Message[] getMessages() {
        Message[] messages = null;
        if (inbox == null) return null;
        try {
            messages = inbox.getMessages();     //  全部取得
        } catch (MessagingException ex) {
            ServletLog.getInstance().log("ReceiveMail#getMessage():"+host+":"+port+" "+user+" "+password+" "+ex);
            System.out.println("ReceiveMail#getMessage():"+host+":"+port+" "+user+" "+password);
            ex.printStackTrace();
        }
        return messages;
    }
    /** メッセージ数を取得する */
    public int getMessageCount() {
        int count = 0;
        if (inbox == null) return -1;
        try {
            count = inbox.getMessageCount();
        } catch (MessagingException ex) {
            ServletLog.getInstance().log("ReceiveMail#getMessageCount():"+host+":"+port+" "+user+" "+password+" "+ex);
            ex.printStackTrace();
        }
        return count;
    }
    
    /**
        クローズする
        @param mode trueで読んだメールを消す
    */
    public boolean close(boolean mode) {
        boolean rsts = false;
        try {
            if (mode) {
                int count = getMessageCount();
                if (debug && count > 0) {
                    System.out.println(count+"個のメッセージを削除します");
                }
            }
            inbox.close(mode);
            store.close();
            rsts = true;
        } catch (MessagingException ex) {
            ServletLog.getInstance().log("ReceiveMail#close():"+host+":"+port+" "+user+" "+password+" "+ex);
            ex.printStackTrace();
        
        }
        inbox = null;
        return rsts;
    }
    public void close() { close(true); }
    
}

//
//
// [end of ReceiveMail.java]
//

