//------------------------------------------------------------------------
// @(#)AnchorStringData.java
//      アンカー付き文字列データ
//          Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.common.SystemConst;
import com.miraidesign.renderer.StyleTag;
import com.miraidesign.session.SessionObject;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.system.SiteManager;
import com.miraidesign.system.SiteMapping;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.Crypt62;
import com.miraidesign.renderer.Module;
import com.miraidesign.renderer.Page;

/**
 *  <b>アンカー付き文字列のデータを保管します</b><br>
 *  <blockquote>
 *  URLは以下の手順で作成されます。<br>
 *  1) 飛び先Pageが設定されていれば、その親 Module より URL を取得<br>
 *  2) ユーザー指定URLを付加<br>
 *  3) SessionIDを付加  （S=nnnn) SystemConstで暗号化ON/OFF<br>
 *  4) MixedIDを付加  （M=nnnn) SystemConstで暗号化ON/OFF<br>
 *  5) 飛び先情報(moduleID+PageID)を付加 (P=mmmppp) SystemConstで暗号化ON/OFF<br>
 *  </blockquote>
 *
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class AnchorStringData extends ItemData {
    static private boolean debug = (SystemConst.debug && false);     // デバッグ表示
    static private boolean debugURL = (SystemConst.debug && false);  // デバッグ表示
    
    private CharArray url    = new CharArray();     // URL
    private CharArray sslurl = new CharArray();     // URL(SSL用)
    private CharArray string = new CharArray();     // 文字列
    private CharArray target = new CharArray();     // ターゲットフレーム
    private char accessKey;                         // アクセスキー
    private Page page;                              // Jump先ページ
    private boolean makeInitialURL = false;         // 
    private boolean makeTopURL = false;             // 
    
    private StyleTag  style  = new StyleTag();      // スタイル参照
    private CharArray color  = new CharArray();     // フォント色
    private CharArray size   = new CharArray();     // フォントサイズ
    private CharArray topPage = new CharArray();    // TopPageURL
    private CharArray label = new CharArray();      // 文書内ラベル指定

    private boolean   bold   = false;               // ボールド
    private boolean   italic = false;               // イタリック
    private int       align  = 0;                   // 位置情報
    private boolean   utn = false;
    private boolean   tagConvert = false;           // タグ文字を変換する
    
    private boolean   tt = false;   // 等しい幅フォントで出力する
    
    /** 
      デフォルトパラメータ用のキーリスト
      存在する場合はセッションから情報を取得してパラメータ出力する
    */
    private CharArrayQueue checkKeys = new CharArrayQueue();
    /** 
      デフォルトパラメータ用のキーリストを取得する<br>
      (存在する場合はセッションから情報を取得してパラメータ出力する)<br>
      nullは返さない
    */
    public CharArrayQueue getCheckKeys() { return checkKeys; }

    /** キーリストをクリアする */
    public void clearCheckKeys() { checkKeys.clear();}
    /** キーリストに追加する */
    public void addCheckKeys(CharArray key) { checkKeys.enqueue(key);}
    public void addCheckKeys(String key) { checkKeys.enqueue(key);}
    public void addCheckKeys(String key1, String key2) { 
        checkKeys.enqueue(key1);
        checkKeys.enqueue(key2);
    }
    public void addCheckKeys(String key1, String key2, String key3) { 
        checkKeys.enqueue(key1);
        checkKeys.enqueue(key2);
        checkKeys.enqueue(key3);
    }
    public void addCheckKeys(String key1, String key2, String key3, String key4) { 
        checkKeys.enqueue(key1);
        checkKeys.enqueue(key2);
        checkKeys.enqueue(key3);
        checkKeys.enqueue(key4);
    }
    
    
    /** 新規セッション用のURLを生成する **/
    public void setInitialMode(boolean mode) {
        makeInitialURL = mode;
    }
    /** 新規セッション用のURLを生成するか？ */
    public boolean getInitialMode() { return makeInitialURL;}
    
    protected boolean useSessionID = true;    // top ページ用


    /** Topページ用のURLを生成する **/
    public void setTopPage(CharArray ch) {
        topPage.set(ch);
    }
    public void setTopPage(String ch) {
        topPage.set(ch);
    }
    public void setTopPage(CharArray ch,boolean mode) {
        topPage.set(ch);
        useSessionID = mode;
    }
    /**
        
        @param mode true: sessionIDをつける
    */
    public void setTopPage(String ch, boolean mode) {
        topPage.set(ch);
        useSessionID = mode;
    }
    
    static public boolean USE_MIXED_ID = false; //true;
    
    /** useMixedID のデフォルトを設定する */
    static public void setDefaultMixedID(boolean mode) {
        USE_MIXED_ID = mode;
    }
    
    // MixedIDを出力するか（やめると、クリックしたItemが取得できなくなる）
    protected boolean useMixedID = USE_MIXED_ID;
    
    public boolean isUseMixedID() { return useMixedID; }
    public void setUseMixedID(boolean mode) {
        useMixedID = mode;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /** 空のAnchorStringData を生成します */
    public AnchorStringData() { }
    
    /** AnchorStringData をコピー生成します
        @param from     コピー元のAnchorStringData
        @param session  対応するSessionObject
    */
    public AnchorStringData(AnchorStringData from,SessionObject session) { 
        this.sessionObject = session;
        copy(from);
        if (debug) {
            System.out.println ("  AnchorStringData copy construct session="+
                                session+" this="+this);
            if (session != null) {
                System.out.println("   sessionID="+
                session.getSessionID());
            }
        }
    }
    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
    */
    public AnchorStringData(String url) { 
        this(url,"",(char)0,null);
    }
    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
    */
    public AnchorStringData(CharArray url) { 
        this(url,"",(char)0,null);
    }
    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
        @param string   表示する文字
    */
    public AnchorStringData(String url,String string) { 
        this(url,string,(char)0,null);
    }
    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
        @param string   表示する文字
    */
    public AnchorStringData(CharArray url, CharArray string) { 
        this(url,string,(char)0,null);
    }

    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
        @param string   表示する文字
        @param page     リンク先のPage
    */
    public AnchorStringData(String url,String string, Page page) { 
        this(url,string,(char)0,page);
    }
    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
        @param string   表示する文字
        @param page     リンク先のPage
    */
    public AnchorStringData(CharArray url, CharArray string, Page page) { 
        this(url,string,(char)0,page);
    }
    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
        @param string   表示する文字
        @param page     リンク先のPage
    */
    public AnchorStringData(String url, CharArray string, Page page) { 
        this(url,string,(char)0,page);
    }
    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
        @param string   表示する文字
        @param page     リンク先のPage
    */
    public AnchorStringData(CharArray url, String string, Page page) { 
        this(url,string,(char)0,page);
    }
    
    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
        @param accessKey アクセスキー('0'～'9')
        @param string   表示する文字
        @param page     リンク先のPage
    */
    public AnchorStringData(String url,String string, char accessKey,Page page) { 
if (debug) System.out.println ("  AnchorStringData construct  this="+this);
        this.page = page;
        this.accessKey = accessKey;
        setURL(url); 
        setText(string);
    }
    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
        @param accessKey アクセスキー('0'～'9')
        @param string   表示する文字
        @param page     リンク先のPage
    */
    public AnchorStringData(CharArray url, CharArray string, char accessKey,Page page) { 
        this.page = page;
        this.accessKey = accessKey;
        setURL(url); 
        setText(string); 
    }
    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
        @param accessKey アクセスキー('0'～'9')
        @param string   表示する文字
        @param page     リンク先のPage
    */
    public AnchorStringData(String url, CharArray string, char accessKey,Page page) { 
        this.page = page;
        this.accessKey = accessKey;
        setURL(url); 
        setText(string); 
    }
    /** AnchorStringData を生成します
        @param url      URLもしくはURLに追加するパラメータ
        @param accessKey アクセスキー('0'～'9')
        @param string   表示する文字
        @param page     リンク先のPage
    */
    public AnchorStringData(CharArray url, String string, char accessKey,Page page) { 
        this.page = page;
        this.accessKey = accessKey;
        setURL(url); 
        setText(string); 
    }
    
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setValue(String[] strs) {string.set(strs[0]);}

    /** URLもしくはURLに追加するパラメータを指定します */
    public void setURL(String str) {url.set(str);}
    /** URLもしくはURLに追加するパラメータを指定します */
    public void setURL(CharArray ch) {url.set(ch);}
    /** SSL用のURLもしくはSSL用のURLに追加するパラメータを指定します */
    public void setSSLURL(String str) {sslurl.set(str);}
    /** SSL用のURLもしくはSSL用のURLに追加するパラメータを指定します */
    public void setSSLURL(CharArray ch) {sslurl.set(ch);}

    /** URLもしくはURLに追加するパラメータを指定します */
    public void addURL(String str) {url.add(str);}
    /** URLもしくはURLに追加するパラメータを指定します */
    public void addURL(CharArray ch) {url.add(ch);}

    /** 表示する文字列を設定します  */
    public void setText(String str) { string.set(str);}
    /** 表示する文字列を設定します  */
    public void setText(CharArray ch) { string.set(ch);}

    /** ターゲットフレームを指定します  */
    public void setTarget(String str) { target.set(str);}
    /** ターゲットフレームを指定します  */
    public void setTarget(CharArray ch) { target.set(ch);}

    /** ジャンプ先のPageオブジェクトを設定する */
    public void setJumpPage(Page page) { this.page = page;}
    /** アクセスキー('0'～'9')を設定する **/
    public void setAccessKey(char c) { this.accessKey = c; }
    
    /** フォント色の設定 */
    public void setColor(String str) {color.set(str);}
    /** フォント色の設定 */
    public void setColor(CharArray ch) {color.set(ch);}
    /** フォントサイズの設定 */
    public void setSize(String str) {size.set(str);}
    /** フォントサイズの設定 */
    public void setSize(CharArray ch) {size.set(ch);}
    /** フォントサイズの設定 */
    public void setSize(int i) {size.reset(); size.format(i);}
    /** bold 設定*/
    public void setBold(boolean b) { bold = b;}
    /** italic 設定 */
    public void setItalic(boolean b) { italic = b;}
    /** Style シート参照設定 */
    public void setStyle(String str)   { style.setStyle(str);}
    /** Style シート参照設定 */
    public void setStyle(CharArray ch) { style.setStyle(ch);}
    public void setClass(String str)   { style.setClass(str);}
    public void setClass(CharArray ch) { style.setClass(ch);}
    public void setID(String str)      { style.setID(str);}
    public void setID(CharArray ch)     { style.setID(ch);}

    /** align情報設定 */
    public void setAlign(int i) { 
        if (i < ItemConstant.align.length) align = i;
    }

    /** フォントタグを生成する必要があるか？ */
    public boolean useFont() {
        return (color.length() > 0 || size.length() > 0 || style.existTag());
    }
    /** html内の飛び先ラベルを指定する*/
    public void setLabel(String str)   { label.set(str);}
    /** html内の飛び先ラベルを指定する*/
    public void setLabel(CharArray ch) { label.set(ch);}
    
    public void setUtn(boolean mode) { utn = mode; }
    public void setTagConvert(boolean mode) { tagConvert = mode; }
    
    /* 等幅フォントを指定する */
    public void setTT(boolean mode) {tt = mode;}
    
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; }
    /** URL を取得する */
    public CharArray getURL() { return url; }
    /** SSL用の URL を取得する */
    public CharArray getSSLURL() { return sslurl; }
    /** 表示文字列を取得する */
    public CharArray getText() { return string; }
    /** ターゲットフレームの取得 */
    public CharArray getTarget() { return target; }
    /** ジャンプ先のPageオブジェクトを取得します */
    public Page      getJumpPage() { return page; }

    public boolean getUtn() { return utn; }
    public boolean getTagConvert() { return tagConvert; }
    
    /** アクセスキー('0'～'9')を取得します
        @return アクセスキー
    */
    public char      getAccessKey() { return accessKey; }
    
    /**  href= 以降の文字列を返します */
    public CharArray getAnchorURL() {
if (debug) System.out.println("▼getAnchorURL");
        int useDir = SystemManager.convertContextPath;  // コンテキストパス変換を行うか
        
        int mixedID = 0;
        if (page != null) {
            mixedID = item.getMixedID();
        } else {
            if (debug) {
                System.out.println("getAnchorURL page が null です item="+((item != null) ? item.getTypeName() : ""));
            }
        }
        
        String szAmp = "&amp;";
        int sessionID = 0;
        boolean ssl = false;
        boolean appendAuthID = false;
        ModuleManager mm = null;
        SiteMapping map = null;
        if (sessionObject != null) {
            sessionID = sessionObject.getSessionID();
            ssl = (sessionObject.isSSL() && sslurl.length()>0);
            if (sessionObject.isPC()) szAmp = "&";
            mm = sessionObject.getModuleManager();
            map = mm.getSiteMapping(); 
            appendAuthID = (sessionObject.isAppendAuthID() && sessionObject.getAuthID()>0);
        }
        int jumpID = 0;
        
        boolean appendContextPath = false;    // コンテキストパス情報を付加したか？
        boolean appendSiteCode = false;
        
        CharArray ch = new CharArray();
        ch.setAppendMode(true);
        if (page != null) { // 飛び先が指定してあれば
            Module module = page.getModule();
            if (topPage.length() > 0) {
                if (!topPage.equals("*")) ch.add(topPage);
                appendContextPath = true;
            } else if (!ssl && url.indexOf("://") < 0) {
                if (useDir == 1) {
                    map.getSiteParameter(sessionObject, ch);
                    appendSiteCode = true;
                } else {
                    if (module != null) ch.add(module.getServletPath());
                    else                ch.add(SystemManager.servletPath);
                }
                appendContextPath = true;
            } else if (ssl && sslurl.indexOf("://") < 0) {
                if (useDir == 1) {
                    map.getSiteParameter(sessionObject, ch);
                    appendSiteCode = true;
                } else {
                    if (module != null) ch.add(module.getServletPath());
                    else  ch.add(SystemManager.servletPath);
                }
                appendContextPath = true;
            }
            jumpID = module.getModuleID()*1000+page.getPageID();
        } else if (topPage.length() > 0) {
            if (!topPage.equals("*")) ch.add(topPage);
            appendContextPath = true;
        } else {
            CharArray chTmp = CharArray.pop(url);
            chTmp.toLowerCase();
            if (chTmp.startsWith("mailto:")) {
                ch.add(url);
                CharArray.push(chTmp);
                return ch;
            }
            CharArray.push(chTmp);
        }
        
        /**** getUserParam()に移行した
        // ユーザー定義パラメータの追加
        if (ssl) {
            if (useDir == 1) {
                CharArray ch2 = CharArray.pop(sslurl);
                ch2.replace("&amp;","&");
                CharToken token = CharToken.pop();
                token.set(ch2, "&");
                for (int i = 0; i < token.size(); i++) {
                    ch.add('/');
                    ch.add(token.get(i));
                }
                CharToken.push(token);
                CharArray.push(ch2);
            } else if (ch.length() > 0) {
                ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
                ch.add(sslurl);
            }
if (debug) System.out.println(" 4["+ch+"] ssl url["+url+"]");
        } else if (url.length() > 0) { // ユーザ指定URL
            if (useDir == 1) {
                CharArray ch2 = CharArray.pop(url);
                ch2.replace("&amp;","&");
                CharToken token = CharToken.pop();
                token.set(ch2, "&");
                for (int i = 0; i < token.size(); i++) {
                    ch.add('/');
                    ch.add(token.get(i));
                }
                CharToken.push(token);
                CharArray.push(ch2);
            } else if (ch.length() > 0) {
                ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
                ch.add(url);
            }
if (debug) System.out.println(" 4["+ch+"] not ssl url["+url+"]");
        }
        **/
        
        boolean  useSite = mm.ini.getBoolean("["+mm.sectionBase+"]","UseSiteCode");
        
        boolean useCookie = false;
        
        if (SystemManager.convertContextPath == 1 &&
           sessionObject != null && sessionObject.request != null) {
            //useCookie = sessionObject.isPC();
            if (sessionObject.isPC()) {
                useCookie = (sessionObject.request.getCookies() != null);
            }
        }
        //useCookie = false;
        
        if (topPage.length() > 0) { // トップページ用URLを設定する
            if (useSite) {  // サイトコードをつける
                if (!appendContextPath) {
                    map.getSiteParameter(sessionObject, ch);
                    appendContextPath = true;
                } else {
                    int site_code = mm.getSiteChannelCode();
                    if (useDir == 1) {
                        if (ch.chars[ch.length()-1] != '/') ch.add('/');
                        ch.add(SystemConst.siteKey); ch.add('=');
                        ch.format(site_code);
                    } else {
                        ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
                        ch.add(SystemConst.siteKey); ch.add('=');
                        ch.format(site_code);
                    }
                }
                appendSiteCode = true;
            }
            if (page != null) {
                map.getPageParameter(ch, jumpID);
                //if (useDir == 1) {
                //    if (ch.chars[ch.length()-1] != '/') ch.add('/');
                //} else {
                //    ch.add(szAmp);
                //}
                //ch.add(SystemConst.pageIDKey); ch.add('=');
                //ch.format(jumpID);
            }
            getUserParam(ch, ssl, useDir);
            // セッションＩＤをつける
            if (useSessionID) {
                if (useCookie || sessionObject.isRobot()) {
                    // do nothing
                } else {
                    if (useDir == 1) {
                        if (ch.chars[ch.length()-1] != '/') ch.add('/');
                    } else {
                        ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
                    }
                    if (SystemConst.cryptSessionID) {
                        ch.add(SystemConst.sessionIDKey[1]); ch.add('=');
                        ch.add(Crypt62.encode(sessionID));
                    } else {
                        ch.add(SystemConst.sessionIDKey[0]); ch.add('=');
                        ch.format(sessionID);
                    }
               }
            }
        } else if (makeInitialURL) { // 初期URLを設定する

if (debug) System.out.println("■makeInitialURL["+ch+"]"+appendContextPath);
            if (!appendContextPath) {
                map.getSiteParameter(sessionObject, ch);   //@@// たぶんダメ
                appendContextPath = true;
                appendSiteCode = true;
            }
            if (!appendSiteCode) {
                if (useDir == 1) {
                    if (ch.chars[ch.length()-1] != '/') ch.add('/');
                } else {
                    ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
                }
                ch.add(SystemConst.siteKey);
                ch.add('=');
                ch.format(mm.getSiteChannelCode());
                appendSiteCode = true;
            }
            if (page != null) {
                map.getPageParameter(ch, jumpID);
            }
            getUserParam(ch, ssl, useDir);
            
        } else {
            if (!appendContextPath && useDir==1) {
                map.getSiteParameter(sessionObject, ch);   //@@// たぶんダメ?
                appendContextPath = true;
                appendSiteCode= true;
            } else if (useSite && !appendSiteCode) {
                map.getSiteParameter(sessionObject, ch);   //@@// 
                appendSiteCode= true;
            
            }
            if (page != null) {
                map.getPageParameter(ch, jumpID);
            }
            getUserParam(ch, ssl, useDir);
            
            // セッションＩＤをつける
            if (useCookie || sessionObject.isRobot()) {
                // do nothing
            } else {
                if (useDir == 1) {
                    if (ch.chars[ch.length()-1] != '/') ch.add('/');
                } else {
                    ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
                }
                if (SystemConst.cryptSessionID) {
                    ch.add(SystemConst.sessionIDKey[1]); ch.add('=');
                    ch.add(Crypt62.encode(sessionID));
                } else {
                    ch.add(SystemConst.sessionIDKey[0]); ch.add('=');
                    ch.format(sessionID);
                }
            }
            // MixedID 追加
            if (useMixedID) {
                if (useDir == 1) {
                    if (ch.chars[ch.length()-1] != '/') ch.add('/');
                } else {
                    ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
                }
                if (SystemConst.cryptMixedID) {
                    ch.add(SystemConst.mixedIDKey[1]); ch.add('=');
                    ch.add(Crypt62.encode(mixedID));
                } else {
                    ch.add(SystemConst.mixedIDKey[0]); ch.add('=');
                    ch.format(mixedID);
                }
            }
            
            if (sessionObject.node != null) {    // ナビゲーションノード情報
                if (useDir == 1) {
                    if (ch.chars[ch.length()-1] != '/') ch.add('/');
                } else {
                    ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
                }
                ch.add(SystemConst.parentNodeKey);
                ch.add("=");
                ch.format(sessionObject.node.getID());
            }
        }
        if (appendAuthID) {
            if (useDir == 1) {
                if (ch.indexOf("/"+SystemConst.authIDKey+"=") < 0) {
                    if (ch.chars[ch.length()-1] != '/') ch.add('/');
                    ch.add(SystemConst.authIDKey); ch.add('=');
                    ch.add(Crypt62.encode(sessionObject.getAuthID()));   // エンコードして追加
                }
            } else {
                if (ch.indexOf(szAmp+SystemConst.authIDKey+"=") < 0) {
                    ch.add(szAmp);
                    ch.add(SystemConst.authIDKey); ch.add('=');
                    ch.add(Crypt62.encode(sessionObject.getAuthID()));   // エンコードして追加
                }
            }
        }
        if (target.length() > 0 /*&& gateway.length() ==0*/) {
            if (useDir == 1) {
                if (ch.chars[ch.length()-1] != '/') ch.add('/');
            } else {
                ch.add(szAmp);
            }
            ch.add(SystemConst.frameKey); ch.add('=');
            ch.add(target);
        }
        
        for (int i = 0; i < checkKeys.size(); i++) {
            CharArray key = checkKeys.peek(i);
            CharArray data = sessionObject.getCheckParameter(key);
            if (data != null) {
                if (useDir == 1) {
                    ch.add('/');
                } else {
                    ch.add(szAmp);
                }
                ch.add(key);
                ch.add('=');
                ch.add(data);
            }
        }
        if (debug || debugURL) {
            System.out.println("▲getAnchorURL()["+ch.toString()+"] url["+
            (ssl ? sslurl : url)+"] page["+(page!=null)+"]");
        }
        return ch;
    }
    
    // ユーザー定義パラメータの追加
    private CharArray getUserParam(CharArray ch, boolean ssl, int useDir) {
        String szAmp = "&";
        if (ssl) {
            if (useDir == 1) {
                CharArray ch2 = CharArray.pop(sslurl);
                ch2.replace("&amp;","&");
                CharToken token = CharToken.pop();
                token.set(ch2, "&");
                for (int i = 0; i < token.size(); i++) {
                    if (ch.chars[ch.length-1] != '/') ch.add('/');
                    ch.add(token.get(i));
                }
                CharToken.push(token);
                CharArray.push(ch2);
            } else if (ch.length() > 0) {
                ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
                ch.add(sslurl);
            }
        } else if (url.length() > 0) { // ユーザ指定URL
            if (useDir == 1) {
                CharArray ch2 = CharArray.pop(url);
                ch2.replace("&amp;","&");
                CharToken token = CharToken.pop();
                token.set(ch2, "&");
                for (int i = 0; i < token.size(); i++) {
                    if (ch.length > 0 && ch.chars[ch.length-1] != '/') ch.add('/');
                    ch.add(token.get(i));
                }
                CharToken.push(token);
                CharArray.push(ch2);
            } else if (ch.length() > 0) {
                ch.add((ch.indexOf('?') >= 0) ? szAmp : "?");
                ch.add(url);
            }
        }
        return ch;
    }
    
    
    /** 
        getSrcTag を使用して下さい
        @deprecated
    */
    public CharArray getHref() {
        return getHref(new CharArray());
    }
    /** 
        @deprecated
    */
    public CharArray getHref(SessionObject session) {
        return getHref(session.getBuffer());
    }
    /** 
        getSrcTag を使用して下さい
        @deprecated 
    */
    public CharArray getHref(CharArray ch) {
        ch.add(" href=\""+getAnchorURL()+"\"");
        return ch;
    }
    
    /** アンカー全体を出力する */
    public CharArray getTag(CharArray ch) {
        if (sessionObject == null) {
            System.out.println("DEBUG[このメッセージを見たら連絡->ishioka]session==null");
        } else if (sessionObject.itemRenderer == null) {
            System.out.println("DEBUG[このメッセージを見たら連絡->ishioka]itemRenderrer==null");
        }
        return sessionObject.itemRenderer.drawAnchorString(ch,this);
    }
    /** アンカー全体を出力する */
    public CharArray getTag() {
        if (sessionObject == null) {
            System.out.println("DEBUG[このメッセージを見たら連絡->ishioka]session==null");
        } else if (sessionObject.itemRenderer == null) {
            System.out.println("DEBUG[このメッセージを見たら連絡->ishioka]itemRenderrer==null");
        }
        return sessionObject.itemRenderer.drawAnchorString(new CharArray(),this);
    }
    
    /** アンカー内を出力する*/
    public CharArray getSrcTag(CharArray ch) {
// debug
        if (sessionObject == null) {
            System.out.println("DEBUG[このメッセージを見たら連絡->ishioka]session==null");
        } else if (sessionObject.itemRenderer == null) {
            System.out.println("DEBUG[このメッセージを見たら連絡->ishioka]itemRenderrer==null");
        }
        return sessionObject.itemRenderer.drawAnchorStringSrc(ch,this);
    }
    /** アンカー内を出力する*/
    public CharArray getSrcTag() {
        return getSrcTag(new CharArray());
    }
    /** 表示文字列を出力する*/
    public CharArray getMenuTag(CharArray ch) {
        if (sessionObject == null) {
            System.out.println("DEBUG[このメッセージを見たら連絡->ishioka]session==null");
        } else if (sessionObject.itemRenderer == null) {
            System.out.println("DEBUG[このメッセージを見たら連絡->ishioka]itemRenderrer==null");
        }
        return sessionObject.itemRenderer.drawAnchorStringMenu(ch,this);
    }
    /** 表示文字列を出力する*/
    public CharArray getMenuTag() {
        return getMenuTag(new CharArray());
    }
    
    /** 表示フォントの色情報を取得する */
    public CharArray getColor() { return color; }
    /** 表示フォントのサイズ情報を取得する */
    public CharArray getSize() { return size; }
    /** 表示フォントがボールドか？ */
    public boolean isBold() { return bold;}
    /** 表示フォントがイタリックか？ */
    public boolean isItalic() { return italic;}
    /** html内の飛び先ラベルを取得する */
    public CharArray getLabel() { return label; }
    
    /** スタイルシート タグ埋め込み情報を取得する */
    public CharArray getStyleTag() { return style.getTag();}
    /** スタイルシート タグ埋め込み情報を取得する */
    public CharArray getStyleTag(CharArray ch) { return style.getTag(ch);}
    /** align 情報を取得する */
    public int getAlign() { return align;}
    
    /** 等幅モードを取得する */
    public boolean getTT() { return tt;}
    
    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    /** 元オブジェクトより全データをコピー */
    public void copy(AnchorStringData from) {
        super.copy(from);
        url.set(from.url);
        sslurl.set(from.sslurl);
        string.set(from.string);
        target.set(from.target);
        this.page     = from.page;
        this.accessKey = from.accessKey;
        this.makeInitialURL = from.makeInitialURL;

        color.set(from.color);
        size.set(from.size);
        topPage.set(from.topPage);
        bold = from.bold;
        italic = from.italic;
        style.copy(from.style);
        align = from.align;
        useSessionID = from.useSessionID;
        useMixedID   = from.useMixedID;
        label.set(from.label);
        this.utn = from.utn;
        this.tagConvert = from.tagConvert;
        this.tt = from.tt;
        checkKeys.copy2(from.checkKeys); // 0.827
    }
    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** AnchorStringData をレンダリングします
        @param session セッションオブジェクト
    */
    public CharArray draw(SessionObject session) {
        if (visible) {
            session.itemRenderer.drawAnchorString(session.getBuffer(),this);
        }
        return session.getBuffer();
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    /** シリアライズデータを書き出します */
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            if (debug) {
                System.out.println("Anchor writeObject: string="+string+
                    " page:"+(page!=null)+"\n  "+getAnchorURL());
            }
            url.writeObject(out);               // URL
            string.writeObject(out);            // 文字列
            target.writeObject(out);            // ターゲットフレーム
            out.writeChar(accessKey);           // アクセスキー
            if (page != null) {
                out.writeBoolean(true);
                // Jump先ページ 指定
                Module module = page.getModule();
                ModuleManager mm = module.getModuleManager();
                out.writeInt(mm.getSiteChannelCode()); 
                out.writeInt(module.getModuleID());
                out.writeInt(page.getPageID());
            } else {
                out.writeBoolean(false);
            }
            out.writeBoolean(makeInitialURL);
            
            style.writeObject(out);
            color.writeObject(out);
            size.writeObject(out);
            topPage.writeObject(out);
            
            out.writeBoolean(bold);
            out.writeBoolean(italic);
            out.writeInt(align);
            out.writeBoolean(useSessionID);
            
            label.writeObject(out);
            sslurl.writeObject(out);               // URL
            out.writeBoolean(utn);
            out.writeBoolean(tagConvert);
            
            out.writeBoolean(tt);
            // default key
            checkKeys.writeObject(out);
            out.writeBoolean(useMixedID);
            
        }
    }
    /** シリアライズデータを読み込みます */
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            url.readObject(in);
            string.readObject(in);
            target.readObject(in);
            accessKey = in.readChar();

            if (in.readBoolean()) {
                // Jumpページの取り出し
                ModuleManager mm = SiteManager.get(in.readInt());
                Module module = mm.getModule(in.readInt());
                page = module.get(in.readInt());
            } else {
                //page = null;
            }
            makeInitialURL = in.readBoolean();
            
            style.readObject(in);
            color.readObject(in);
            size.readObject(in);
            topPage.readObject(in);
            bold = in.readBoolean();
            italic = in.readBoolean();
            align = in.readInt();
            useSessionID = in.readBoolean();
            
            label.readObject(in);
            sslurl.readObject(in);
            utn = in.readBoolean();
            tagConvert = in.readBoolean();
            tt = in.readBoolean();
            // default key
            checkKeys.readObject(in);
            useMixedID = in.readBoolean();
        }
    }
}

//
// [end of AnchorStringData.java]
//

