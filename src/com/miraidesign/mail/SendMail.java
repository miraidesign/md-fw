//------------------------------------------------------------------------
//    SendMail.java
//                 メール送信プログラム
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.mail;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.miraidesign.session.UserAgent;
//import com.miraidesign.image.EmojiConverter;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.HashParameter;

/** SMTP認証用 承認クラス */
final class MyAuth extends Authenticator {
    String smtpUser="";
    String smtpPasswd="";
    public  MyAuth(String user, String pass) {
        smtpUser=user;
        smtpPasswd=pass;
    }
    protected PasswordAuthentication getPasswordAuthentication(){
        return new PasswordAuthentication(smtpUser, smtpPasswd);
    }
}  

/**
 * メール送信クラス
 *  
 *  @version 0.5 
 *  @author toru ishioka
 *  @since  JDK1.1
*/

public class SendMail {
    // static section ------------------------------------------
    private boolean debug = true;
    private boolean encodeBUG = true; // javaのエンコードバグ対策
        
    static private String szDefaultFromAddress;
    
    /** デフォルトのFromアドレスを設定する */
    static public void setDefaultFromAddress(String str) {
        szDefaultFromAddress = str;
    }
    static public String getDefaultFromAddress() { return szDefaultFromAddress;}
    //----
    static private String szDefaultSmtpHost;

    /** デフォルトのSMTPホストを設定する */
    static public void setDefaultSmtpHost(String str) {
        szDefaultSmtpHost = str;
    }
    static public String getDefaultSmtpHost() { return szDefaultSmtpHost;}
    //----
    static public String szDefaultReplyTo;

    /** デフォルトのReply-Toを設定する */
    static public void setDefaultReplyTo(String str) {
        szDefaultReplyTo = str;
    }
    
    static public String szDefaultErrorsTo;

    /** デフォルトのErrorsToを設定する */
    static public void setDefaultErrorsTo(String str) {
        szDefaultErrorsTo = str;
    }

    //----------------------------------------------------------
    private boolean debugMode = false;

    /** デバッグモードを設定する*/
    public void setDebugMode(boolean mode) { debugMode = mode; }
    
    private MimeMessage msg;
    /** MimeMessageインスタンスを取得する 
        @return 未設定の場合はnullが帰る
    */
    public MimeMessage getMimeMessage() {
        return msg;
    }
    
    private Session session; 
    private Transport transport;
    
    private String szCharSet = "iso-2022-jp";
    private String szFromAddress;
    private String szSmtpHost;
    private String szReplyTo;
    private String szErrorsTo;
    
    private CharArray chSubject = new CharArray();      // Subject
    private CharArray chSaveSubject = new CharArray();      // Subjectバックアップ
    private CharArray chMessage = new CharArray();      // 本文
    private CharArray chSaveMessage    = new CharArray();      // 本文のバックアップ

    private HashVector<String,String> header = new HashVector<String,String>();     // ヘッダ 
    
    private CharArrayQueue queueToAddress = new CharArrayQueue() {{
        setMemSavingMode(false);
    }};
    private CharArrayQueue queueCcAddress = new CharArrayQueue() {{
        setMemSavingMode(false);
    }};
    private CharArrayQueue queueBccAddress = new CharArrayQueue() {{
        setMemSavingMode(false);
    }};
    private CharArrayQueue queueAttachmentFile = new CharArrayQueue() {{
        setMemSavingMode(false);
    }};

    /** Header リストを取得する */
    public  HashVector<String,String>  getHeader() { return header; }
    
    /** To  Address リストを取得する */
    public  CharArrayQueue getToAddress() { return queueToAddress; }
    /** CC Address リストを取得する */
    public  CharArrayQueue getCcAddress() { return queueCcAddress; }
    /** BCC Address リストを取得する */
    public  CharArrayQueue getBccAddress() { return queueBccAddress; }
    /** 添付ファイルリストを取得する */
    public  CharArrayQueue getAttachmentFile() { return queueAttachmentFile; }
    
    private String encodeText = "B";

    /** 
        subjectの エンコードテキスト ( "B" "Q" "" ) を指定する 
        iso-2022-jp 時のみ有効 （デフォルトは "B")
    */
    public void setEncodeText(String str) { encodeText = str;}

    // 送信認証対応
    private boolean smtpAuth = false;
    private String  smtpUser   = "";
    private String  smtpPasswd = "";
    
    /**
        SMTP認証設定
    */
    public void setSmtpAuth(boolean mode) {
        smtpAuth = mode;
    }
    /**
        SMTP認証設定
        @param mode 送信認証を行うか？
        @param user 認証ID
        @param passwd パスワード
    */
    public void setSmtpAuth(boolean mode, CharArray user, CharArray passwd) {
        smtpAuth = mode;
        if (user != null)   smtpUser = user.toString();
        if (passwd != null) smtpPasswd = passwd.toString();
    }
    /**
        SMTP認証設定
        @param mode 送信認証を行うか？
        @param user 認証ID
        @param passwd パスワード
    */
    public void setSmtpAuth(boolean mode, String user, String passwd) {
        smtpAuth = mode;
        if (user != null)   smtpUser = user;
        if (passwd != null) smtpPasswd = passwd;
    }
    /**
        SMTP認証設定
        @param user 認証ID
        @param passwd パスワード
    */
    public void setSmtpAuth(CharArray user, CharArray passwd) {
        smtpAuth = true;
        if (user != null)   smtpUser = user.toString();
        if (passwd != null) smtpPasswd = passwd.toString();
    }
    /**
        SMTP認証設定
        @param user 認証ID
        @param passwd パスワード
    */
    public void setSmtpAuth(String user, String passwd) {
        smtpAuth = true;
        if (user != null)   smtpUser = user;
        if (passwd != null) smtpPasswd = passwd;
    }
    
    //////////////////////////////////////////////////////////////////////////////////
    /** Constructor */
    public SendMail() { 
        debug &= com.miraidesign.common.SystemConst.debug;
    }
    /** Copy conbstructor */
    public SendMail(SendMail from) {
        copy(from);
    }
    /** Copy */
    public void copy(SendMail from) {
        this.szCharSet      = from.szCharSet;
        this.szFromAddress  = from.szFromAddress;
        this.szSmtpHost     = from.szSmtpHost;
        this.szReplyTo      = from.szReplyTo;
        this.szErrorsTo     = from.szErrorsTo;
    
        this.chSubject.set(from.chSubject);
        this.chSaveSubject.set(from.chSaveSubject);
        this.chMessage.set(from.chMessage);
        this.chSaveMessage.set(from.chSaveMessage);

        this.header.copy(from.header);
    
        this.queueToAddress.copy(from.queueToAddress);
        this.queueCcAddress.copy(from.queueCcAddress);
        this.queueBccAddress.copy(from.queueBccAddress);
        this.queueAttachmentFile.copy(from.queueAttachmentFile);
        this.smtpAuth   = from.smtpAuth;
        this.smtpUser   = from.smtpUser;
        this.smtpPasswd = from.smtpPasswd;
    }
    
    /** CharSet 指定
        デフォルトは "iso-2022-jp"
    */
    public void setCharSet(String str) {
        szCharSet = str;
    }
    
    /** FromAddress の設定<br>
        指定がない場合はデフォルト設定を使用する
    */
    public void setFromAddress(String str) { szFromAddress = str; change_flg = true;}
    public void setFromAddress(CharArray ch) { szFromAddress = ch.toString(); change_flg = true;}
    
    /** FromAddress の取得 */
    public String getFromAddress() {
        return (szFromAddress != null) ? szFromAddress : szDefaultFromAddress;
    }
    
    /** SmtpHost の設定<br>
        指定がない場合はデフォルト設定を使用する
    */
    public void setSmtpHost(String str) { szSmtpHost = str; change_flg = true;}
    public void setSmtpHost(CharArray ch) { szSmtpHost = ch.toString(); change_flg = true;}
    
    /** SmtpHost の取得 */
    public String getSmtpHost() {
        return (szSmtpHost != null) ? szSmtpHost : szDefaultSmtpHost;
    }
    
    /**
        メールテキストをファイルより設定する
    */
    public void set(CharArrayQueue file) {
        chSubject.clear();
        chMessage.clear();
        if (file != null) {
            chSubject.add(file.peek(0));
            for (int i = 1; i < file.size(); i++) {
                chMessage.add(file.peek(i));
                chMessage.add("\n");
            }
        }
    }
    
    /**
        メール情報一括設定<br>
        サーバー、アドレス等の情報を一括設定する。
        @param  hv  iniファイルセクション情報
        @return true:設定完了
    */
    public boolean set(HashVector<CharArray,CharArrayQueue> hv) {
        if (hv == null || hv.size()==0) return false;
        
        CharArrayQueue queue = hv.get("SmtpHost");
        if (queue != null && queue.size()>0) setSmtpHost(queue.peek());
        
        boolean   smtpAuth = false;
        queue = hv.get("SmtpAuth");
        if (queue != null && queue.size()>0) smtpAuth = queue.peek().getBoolean();
        CharArray smtpUser = null;
        queue = hv.get("SmtpUser");
        if (queue != null && queue.size()>0) smtpUser = queue.peek();
        CharArray smtpPasswd = null;
        queue = hv.get("SmtpPasswd");
        if (queue != null && queue.size()>0) smtpPasswd = queue.peek();
        setSmtpAuth(smtpAuth, smtpUser, smtpPasswd);

        queue = hv.get("FromAddress");
        if (queue != null && queue.size()>0) setFromAddress(queue.peek());
        
        CharArray cc = null;
        queue = hv.get("CcAddress");
        if (queue != null && queue.size()>0) {
            cc = queue.peek();
            if (cc != null && cc.trim().length > 0) {
                setCcAddress((HashParameter.getInstance(cc)).getKeyParameter());
            }
        }
        CharArray bcc = null;
        queue = hv.get("BccAddress");
        if (queue != null && queue.size()>0) {
            bcc = queue.peek();
            if (bcc != null && bcc.trim().length > 0) {
                setBccAddress((HashParameter.getInstance(bcc)).getKeyParameter());
            }
        }
        
        queue = hv.get("ReplyTo");
        if (queue != null && queue.size()>0) {
            CharArray reply = queue.peek().trim();
            if (reply != null && reply.trim().length > 0) {
                setReplyTo(reply);
            }
        }
        
        // Errors To は止める
        queue = hv.get("ErrorsTo");
        if (queue != null && queue.size()>0) {
            CharArray errors = queue.peek().trim();
            if (errors != null && errors.trim().length > 0) {
                setErrorsTo(errors);
            }
        }
        
        return true;
    }
    
    /** 表題の設定
        @param str 表題
    */
    public void setSubject(String str) {
        chSubject.set(str);
    }
    public void setSubject(CharArray str) {
        chSubject.set(str);
    }
    /** 表題に文字列を追加
        @param str 表題
    */
    public void addSubject(String str) {
        chSubject.add(str);
    }
    public void addSubject(CharArray str) {
        chSubject.add(str);
    }
    
    /** 表題の取得 */
    public CharArray getSubject() { return chSubject;}
    
    
    /** 本文の設定
        @param str 本文
    */
    public void setMessage(String str) {
        chMessage.set(str);
    }
    public void setMessage(CharArray str) {
        chMessage.set(str);
    }
    /** 本文に文字列を追加
        @param str 本文
    */
    public void addMessage(String str) {
        chMessage.add(str);
    }
    public void addMessage(CharArray str) {
        chMessage.add(str);
    }

    /** ヘッダの追加
        @param key      キー値
        @param value    値
    */
    public void addHeader(String key, String value) {
        header.remove(key);
        header.put(key, value);
    }


    /**
     *  サブジェクトの部分文字列をコンバートします
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     */

    public CharArray replaceSubject(String from, String to) {
        chSubject.replace(from,to);
        return chSubject;
    }
    public CharArray replaceSubject(String from, CharArray to) {
        chSubject.replace(from,to);
        return chSubject;
    }
    public CharArray replaceSubject(CharArray from, CharArray to) {
        chSubject.replace(from,to);
        return chSubject;
    }
    public CharArray replaceSubject(CharArray from, String to) {
        chSubject.replace(from,to);
        return chSubject;
    }
    
    /**
     *  本文の部分文字列をコンバートします
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     */

    public CharArray replaceMessage(String from, String to) {
        chMessage.replace(from,to);
        return chMessage;
    }
    public CharArray replaceMessage(String from, CharArray to) {
        chMessage.replace(from,to);
        return chMessage;
    }
    public CharArray replaceMessage(CharArray from, CharArray to) {
        chMessage.replace(from,to);
        return chMessage;
    }
    public CharArray replaceMessage(CharArray from, String to) {
        chMessage.replace(from,to);
        return chMessage;
    }
    
    /** 本文、サブジェクトをバックアップ **/
    public void save() { 
        chSaveSubject.set(chSubject);
        chSaveMessage.set(chMessage);
    }
    /** バックアップした本文、サブジェクトを呼び出す */
    public void load() { 
        chSubject.set(chSaveSubject);
        chMessage.set(chSaveMessage);
    }
    
    
    /**
     *  本文ワード文字列をコンバートします（オリジナルを変換します）<br>
     *  文字列の前後が半角スペース等で区切られている必要があります。
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     */
    public CharArray replaceWordMessage(String from, String to) {
        chMessage.replaceWord(from,to);
        return chMessage;
    }
    public CharArray replaceWordMessage(CharArray from, CharArray to) {
        chMessage.replaceWord(from,to);
        return chMessage;
    }
    
    /** 本文の取得 */
    public CharArray getMessage() { return chMessage;}
    
    
    /** Reply To **/
    public void setReplyTo(String str) { szReplyTo = str; change_flg = true;}
    public void setReplyTo(CharArray ch) { szReplyTo = ch.toString(); change_flg = true;}
    /** 設定されている replyTo を返す。
        @return 存在しない場合は null */
    public String getReplyTo() { return szReplyTo;}
    
    
    /** Error To **/
    public void setErrorsTo(String str) { szErrorsTo = str; change_flg = true;}
    public void setErrorsTo(CharArray ch) { szErrorsTo = ch.toString(); change_flg = true;}
    /** 設定されている errorsTo を返す。
        @return 存在しない場合は null */
    public String getErrorsTo() { return szErrorsTo;}
    
    
    /** ToAddressの設定
        @param str ToAddress
    */
    public void setToAddress(String str) {
        queueToAddress.clear();
        queueToAddress.enqueue(str);
    }
    public void setToAddress(CharArray str) {
        queueToAddress.clear();
        queueToAddress.enqueue(str);
    }
    /** ToAddressリストの設定
        @param queue ToAddress のリスト
    */
    public void setToAddress(CharArrayQueue queue) {
        queueToAddress.copy(queue);
    }
    /** ToAddressの追加
        @param str ToAddress
    */
    public void addToAddress(String str) {
        queueToAddress.enqueue(str);
    }
    public void addToAddress(CharArray str) {
        queueToAddress.enqueue(str);
    }
    
    /** CC Addressの設定
        @param str CC Address
    */
    public void setCcAddress(String str) {
        queueCcAddress.clear();
        queueCcAddress.enqueue(str);
        change_flg = true;
    }
    public void setCcAddress(CharArray str) {
        queueCcAddress.clear();
        queueCcAddress.enqueue(str);
        change_flg = true;
    }
    /** CC Addressリストの設定
        @param queue CC Address のリスト
    */
    public void setCcAddress(CharArrayQueue queue) {
        queueCcAddress.copy(queue);
        change_flg = true;
    }
    /** CC Addressの追加
        @param str CcAddress
    */
    public void addCcAddress(String str) {
        queueCcAddress.enqueue(str);
        change_flg = true;
    }
    public void addCcAddress(CharArray str) {
        queueCcAddress.enqueue(str);
        change_flg = true;
    }
    
    /** BCC Addressの設定
        @param str BCC Address
    */
    public void setBccAddress(String str) {
        queueBccAddress.clear();
        queueBccAddress.enqueue(str);
        change_flg = true;
    }
    public void setBccAddress(CharArray str) {
        queueBccAddress.clear();
        queueBccAddress.enqueue(str);
        change_flg = true;
    }
    /** BCC Addressリストの設定
        @param queue BCC Address のリスト
    */
    public void setBccAddress(CharArrayQueue queue) {
        queueBccAddress.copy(queue);
        change_flg = true;
    }
    /** BCC Addressの追加
        @param str BCC Address
    */
    public void addBccAddress(String str) {
        queueBccAddress.enqueue(str);
        change_flg = true;
    }
    public void addBccAddress(CharArray str) {
        queueBccAddress.enqueue(str);
        change_flg = true;
    }
    
    /** 添付ファイルの設定
        @param str 添付ファイル
    */
    public void setAttachmentFile(String str) {
        queueAttachmentFile.clear();
        queueAttachmentFile.enqueue(str);
        change_flg = true;
    }
    public void setAttachmentFile(CharArray str) {
        queueAttachmentFile.clear();
        queueAttachmentFile.enqueue(str);
        change_flg = true;
    }
    
    /** 添付ファイルの追加
        @param str 添付ファイル
    */
    public void addAttachmentFile(String str) {
        queueAttachmentFile.enqueue(str);
        change_flg = true;
    }
    public void addAttachmentFile(CharArray str) {
        queueAttachmentFile.enqueue(str);
        change_flg = true;
    }
    
    
    
    
    /**
        新たにセッション／メッセージを設定する
        （通常は呼ばなくてもよい）
    */
    
    public boolean setMimeMessage() {
if (debug) System.out.println("setMimeMessage start !!!!!!!!!!");
        boolean rsts = true;
        try {
            String host = (szSmtpHost != null) ? szSmtpHost: szDefaultSmtpHost;
if (debug) System.out.println("host["+host+"]");
            if (host == null || host.trim().length() <= 0) {
                System.out.println("SendMail#SetMimeMessage: SMTP host が指定されていません");
                return false;
            }
            String from = (szFromAddress != null) ? szFromAddress: szDefaultFromAddress;
            if (from == null || from.trim().length() <= 0) {
                System.out.println("SendMail#SetMimeMessage: from address が指定されていません");
                return false;
            }
            String replyTo = (szReplyTo != null) ? szReplyTo: szDefaultReplyTo;
            String errorsTo = (szErrorsTo != null) ? szErrorsTo: szDefaultErrorsTo;
            
            //if (queueToAddress.size() == 0) {
            //     System.out.println("SendMail#SetMimeMessage: To address が指定されていません");
            //    return false;
            //}
            
            Properties props = new Properties();
            props.put("mail.from", from);       // Message ID ?
            props.put("mail.smtp.host", host);
            
            // 送信認証
            if (smtpAuth) {
                props.put("mail.smtp.auth", "true" );
            }
            //session = Session.getDefaultInstance(props, null); 
            session = Session.getInstance(props, smtpAuth ? new MyAuth(smtpUser,smtpPasswd) : null); 
            if (debugMode) session.setDebug(true);
            
            // チャンネルごとにSendMailオブジェクトを保管する
            
            msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(from));
            if (replyTo != null && replyTo.length() > 0) {
                InternetAddress[] address = new InternetAddress[1];
                address[0] = new InternetAddress(replyTo);
                msg.setReplyTo(address);
            }
            if (errorsTo != null && errorsTo.trim().length() > 0) {
                msg.setHeader("Errors-To",errorsTo);
            }
            //------------------------------------------------------------------------
            int size = queueCcAddress.size();
            if (size > 0) {
                InternetAddress[] address = new InternetAddress[size];
                for (int i = 0; i < size; i++) {
                    address[i] = new InternetAddress(queueCcAddress.peek(i).toString());
                }
                msg.setRecipients(Message.RecipientType.CC, address);
            }
            size = queueBccAddress.size();
            if (size > 0) {
                InternetAddress[] address = new InternetAddress[size];
                for (int i = 0; i < size; i++) {
                    address[i] = new InternetAddress(queueBccAddress.peek(i).toString());
                }
                msg.setRecipients(Message.RecipientType.BCC, address);
            }
            // ここまでは共通にできるはず。
            /****
            size = queueToAddress.size();
            if (size > 0) {
                InternetAddress[] address = new InternetAddress[size];
                for (int i = 0; i < size; i++) {
                    address[i] = new InternetAddress(queueToAddress.peek(i).toString());
                }
                msg.setRecipients(Message.RecipientType.TO, address);
            }

            //Subjectの設定
            msg.setSubject(chSubject.toString(),szCharSet);

            //時間の指定
            msg.setSentDate(new Date());

            //メッセージ本文の指定
            msg.setText(chMessage.toString(), szCharSet);
            ***/
            change_flg = false;
        } catch (Exception ex) {
            ex.printStackTrace();
            rsts = false;
        }
if (debug) System.out.println("setMimeMessage end :"+rsts);
        return rsts;
    }
    
    public boolean connect() {
        return connect(true);
    }
    
    /**
        @param mode true：setMimeMessage()を呼ぶかも
    */
    public boolean connect(boolean mode) {
        boolean rsts = true;
        if ((msg == null) || (mode && change_flg)) rsts = setMimeMessage();
        if (rsts) {
            try {
                transport = session.getTransport("smtp");
                transport.connect();
            } catch (Exception ex) {
                ex.printStackTrace();
                rsts = false;
            }
        }
if (debug) System.out.println("connect end :"+rsts);
        return rsts;
    }
    public boolean close() {
        boolean rsts = true;
        try {
           transport.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            rsts = false;
        }
        return false;
    }
    
    boolean change_flg = false;     // 変更フラグ
    
    /** メール送信 
        @return true:送信成功
    */
    public boolean send() {
if (debug) System.out.println("\nsend() start-------------------------");
        boolean rsts = true;
        if (msg == null || change_flg) rsts = setMimeMessage();
        int size = queueToAddress.size();
        InternetAddress[] address = new InternetAddress[size];
        try {
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    address[i] = new InternetAddress(queueToAddress.peek(i).toString());
                }
                msg.setRecipients(Message.RecipientType.TO, address);
            } else if (transport == null) {
                System.out.println("SendMail#send: To address が指定されていません");
                return false;
            }
            
if (debug) System.out.println("send() header設定--");
            // Header 設定
            for (int i = 0; i < header.size(); i++) {
                String key   = (String)header.keyElementAt(i);
                String value = (String)header.valueElementAt(i);
                msg.setHeader(key,value);
            }
if (debug) System.out.println("send() header設定 end--");
 
 
            CharArray subject = CharArray.pop(chSubject);
            CharArray message = CharArray.pop(chMessage);
            
            // 改行コードの変換
            //subject.replace("\n","\r\n");
            //message.replace("\n","\r\n");
            
            //Subjectの設定
            CharArray ch = CharArray.pop(szCharSet);
            ch.toLowerCase();
            if (ch.equals("iso-2022-jp")) {
                if (encodeBUG) {    // javaのエンコードバグ対策
                    if (subject.chars[subject.length-1] > '\u007e') subject.add(' ');
                    if (message.chars[message.length-1] > '\u007e') message.add('\n');
                }
                subject.toJIS();
                message.toJIS();
                if (encodeText != null && encodeText.length() > 0) {
                    msg.setSubject(MimeUtility.encodeText(subject.toString(),
                                   szCharSet,encodeText));
                } else {
                    msg.setSubject(subject.toString(),szCharSet);
                }
                //msg.setHeader("Content-Type","text/plain; charset=\"iso-2022-jp\"");
                msg.setHeader("Content-Type","text/plain");
            } else {
                msg.setSubject(subject.toString(),szCharSet);
                msg.setHeader("Content-Type","text/plain");
            }
            //時間の指定
            msg.setSentDate(new Date());
            //メッセージ本文の指定
            
            if (queueAttachmentFile.size()==0) {    // 添付ファイルなし
                //int carrier = UserAgent.getCarrierFromMailAddress(getToAddress().peek());
                msg.setText(message.toString(), szCharSet);
            } else {                                // 添付ファイルあり
                MimeMultipart content = new MimeMultipart();
                // テキスト部作成
                MimeBodyPart text = new MimeBodyPart();
                text.setText(message.toString(), szCharSet);
                content.addBodyPart(text);
                // 添付ファイル部作成
                CharArray tmp = CharArray.pop();
                for (int i=0; i < queueAttachmentFile.size(); i++) {
                    tmp.set(queueAttachmentFile.peek(i));
                    MimeBodyPart attachmentFile = new MimeBodyPart();
                    attachmentFile.setDataHandler(
                        new DataHandler(new FileDataSource(queueAttachmentFile.peek(i).toString())));

                    tmp.replace('\\','/');  // パスの変換
                    int index = tmp.lastIndexOf('/');
                    if (index >= 0) {
                        tmp.remove(0, index+1);
                    }
                    attachmentFile.setFileName(tmp.toString());
                    content.addBodyPart(attachmentFile);
                }
                CharArray.push(tmp);
                // メールにマルチパートメッセージをセット
                msg.setContent(content);
            }
            CharArray.push(message);
            CharArray.push(subject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        //rsts = false;  // for debug
        
        if (rsts) {
            try {
if (debug) System.out.println("send() transport! "+(transport != null));
                if (transport != null) {
                    transport.sendMessage(msg,address);
                } else {
                    Transport.send(msg);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (debug) {
                    System.out.println("SMTP:"+getSmtpHost()+
                        " from:"+getFromAddress()+
                        " to:"+address[0]);
                
                }
                rsts = false;
            }
        }
if (debug) System.out.println("send() end:-----------------------------\n"+rsts);
        return rsts;
    }
    

}



//
// [end of SendMail.java]
//

