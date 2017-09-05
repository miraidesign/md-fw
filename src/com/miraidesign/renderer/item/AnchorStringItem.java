//------------------------------------------------------------------------
// @(#)AnchorStringItem.java
//         アンカー付き文字列
//             Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.IntObject;
import com.miraidesign.renderer.Page;

/**
 *  アンカー付き文字列 <br>
 *  setter/getter は通常 sessionID付きのものを使用すること
 *  @version 0.6 2010-04-10
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class AnchorStringItem extends Item {
    static private boolean debug = (SystemConst.debug && false);  // デバッグ表示

    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /** 空のAnchorStringItem を生成します */
    public AnchorStringItem() {
        super();
        init();
        itemData = new AnchorStringData();
        itemData.setItem(this);
    }
    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
    */
    public AnchorStringItem(String url) {
        this(url,"");
    }
    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
    */
    public AnchorStringItem(CharArray url) {
        this(url,"");
    }

    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
        @param str   表示する文字
    */
    public AnchorStringItem(String url,String str) {
        this(url,str,null);
    }
    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
        @param str   表示する文字
    */
    public AnchorStringItem(CharArray url,CharArray str) {
        this(url,str,null);
    }
    public AnchorStringItem(String url,CharArray str) {
        this(url,str,(char)0,null);
    }
    public AnchorStringItem(CharArray url,String str) {
        this(url,str,(char)0,null);
    }
    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
        @param str   表示する文字
        @param accessKey アクセスキー('0'～'9')
    */
    public AnchorStringItem(String url,String str,char accessKey) {
        this(url,str,accessKey,null);
    }
    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
        @param str   表示する文字
        @param accessKey アクセスキー('0'～'9')
    */
    public AnchorStringItem(CharArray url,CharArray str, char accessKey) {
        this(url,str,accessKey,null);
    }

    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
        @param str   表示する文字
        @param page     リンク先のPage
    */
    public AnchorStringItem(String url,String str,Page page) {
        super();
        init();
        itemData = new AnchorStringData(url, str,page);
        itemData.setItem(this);
    }
    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
        @param str   表示する文字
        @param page     リンク先のPage
    */
    public AnchorStringItem(CharArray url,CharArray str,Page page) {
        super();
        init();
        itemData = new AnchorStringData(url,str,page);
        itemData.setItem(this);
    }
    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
        @param str   表示する文字
        @param accessKey アクセスキー('0'～'9')
        @param page     リンク先のPage
    */
    public AnchorStringItem(String url,String str,char accessKey,Page page) {
        super();
        init();
        itemData = new AnchorStringData(url, str, accessKey, page);
        itemData.setItem(this);
    }
    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
        @param str   表示する文字
        @param accessKey アクセスキー('0'～'9')
        @param page     リンク先のPage
    */
    public AnchorStringItem(CharArray url,CharArray str,char accessKey,Page page) {
        super();
        init();
        itemData = new AnchorStringData(url,str,accessKey,page);
        itemData.setItem(this);
    }
    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
        @param str   表示する文字
        @param accessKey アクセスキー('0'～'9')
        @param page     リンク先のPage
    */
    public AnchorStringItem(String url,CharArray str,char accessKey,Page page) {
        super();
        init();
        itemData = new AnchorStringData(url,str,accessKey,page);
        itemData.setItem(this);
    }
    /** AnchorStringItem を生成します
        @param url   URLもしくはURLに追加するパラメータ
        @param str   表示する文字
        @param accessKey アクセスキー('0'～'9')
        @param page     リンク先のPage
    */
    public AnchorStringItem(CharArray url,String str,char accessKey,Page page) {
        super();
        init();
        itemData = new AnchorStringData(url,str,accessKey,page);
        itemData.setItem(this);
    }
    /** AnchorStringItem をコピー生成します
        @param from     コピー元のAnchorStringItem
    */
    public AnchorStringItem(AnchorStringItem from) {
        super();
        setType(ANCHOR_STRING);
        setCloneable(from.isCloneable());
        AnchorStringData fromdata = (AnchorStringData)from.itemData;
        itemData = new AnchorStringData(fromdata,fromdata.getSessionObject());
        itemData.setItem(this);
    }
    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(ANCHOR_STRING);
        setCloneable(true);
    }

    //---------------------------------------------------------------------
    // データ設定メソッド
    //---------------------------------------------------------------------
    /** 表示する文字列を指定する  */
    public void setText(CharArray ch) {
        ((AnchorStringData)itemData).setText(ch);
    }
    public void setText(String str) {
        ((AnchorStringData)itemData).setText(str);
    }

    /** 表示する文字列を指定する  */
    public void setText(CharArray ch, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setText(ch);
    }
    public void setText(String str, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setText(str);
    }
    /** 表示する文字列を指定する  */
    public void setText(CharArray ch, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setText(ch);
    }
    public void setText(String str, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setText(str);
    }
    /** 付加するURL文字を指定する*/
    public void setURL(CharArray ch) {
        ((AnchorStringData)itemData).setURL(ch);
    }
    /** 付加するURL文字を指定する*/
    public void setURL(String str) {
        ((AnchorStringData)itemData).setURL(str);
    }
    /** 付加するURL文字を指定する*/
    public void setURL(CharArray ch, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setURL(ch);
    }
    /** 付加するURL文字を指定する*/
    public void setURL(String str, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setURL(str);
    }
    /** 付加するURL文字を指定する*/
    public void setURL(CharArray ch, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setURL(ch);
    }
    /** 付加するURL文字を指定する*/
    public void setURL(String str, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setURL(str);
    }
    /** 付加するSSL用のURL文字を指定する*/
    public void setSSLURL(CharArray ch) {
        ((AnchorStringData)itemData).setSSLURL(ch);
    }
    /** 付加するSSL用のURL文字を指定する*/
    public void setSSLURL(String str) {
        ((AnchorStringData)itemData).setSSLURL(str);
    }
    /** 付加するSSL用のURL文字を指定する*/
    public void setSSLURL(CharArray ch, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setSSLURL(ch);
    }
    /** 付加するSSL用のURL文字を指定する*/
    public void setSSLURL(String str, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setSSLURL(str);
    }
    /** 付加するSSL用のURL文字を指定する*/
    public void setSSLURL(CharArray ch, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setSSLURL(ch);
    }
    /** 付加するSSL用のURL文字を指定する*/
    public void setSSLURL(String str, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setSSLURL(str);
    }

    /** 追加するURL文字を指定する*/
    public void addURL(CharArray ch) {
        ((AnchorStringData)itemData).addURL(ch);
    }
    /** 追加するURL文字を指定する*/
    public void addURL(String str) {
        ((AnchorStringData)itemData).addURL(str);
    }
    /** 追加するURL文字を指定する*/
    public void addURL(CharArray ch, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).addURL(ch);
    }
    /** 追加するURL文字を指定する*/
    public void addURL(String str, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).addURL(str);
    }
    /** 追加するURL文字を指定する*/
    public void addURL(CharArray ch, SessionObject session) {
        ((AnchorStringData)getItemData(session)).addURL(ch);
    }
    /** 追加するURL文字を指定する*/
    public void addURL(String str, SessionObject session) {
        ((AnchorStringData)getItemData(session)).addURL(str);
    }

    /** ターゲットフレームを指定する*/
    public void setTarget(CharArray ch) {
        ((AnchorStringData)itemData).setTarget(ch);
    }
    /** ターゲットフレームを指定する*/
    public void setTarget(CharArray ch, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setTarget(ch);
    }
    public void setTarget(CharArray ch, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setTarget(ch);
    }
    /** ターゲットフレームを指定する*/
    public void setTarget(String str) {
        ((AnchorStringData)itemData).setTarget(str);
    }
    /** ターゲットフレームを指定する*/
    public void setTarget(String str, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setTarget(str);
    }
    public void setTarget(String str, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setTarget(str);
    }

    /** アクセスキーを設定する
        @param c   アクセスキー('0'-'9') */
    public void setAccessKey(char c) {
        ((AnchorStringData)itemData).setAccessKey(c);
    }
    /** アクセスキーを設定する
        @param c   アクセスキー('0'-'9') */
    public void setAccessKey(char c, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setAccessKey(c);
    }
    public void setAccessKey(char c, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setAccessKey(c);
    }
    /** 飛び先のページを指定する */
    public void setJumpPage(Page page) {
        ((AnchorStringData)itemData).setJumpPage(page);
    }
    /** 飛び先のページを指定する */
    public void setJumpPage(Page page, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setJumpPage(page);
    }
    public void setJumpPage(Page page, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setJumpPage(page);
    }

    /** 新規セッション用のURLを生成する **/
    public void setInitialMode(boolean mode) {
        ((AnchorStringData)itemData).setInitialMode(mode);
    }
    /** 新規セッション用のURLを生成する **/
    public void setInitialMode(boolean mode, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setInitialMode(mode);
    }
    public void setInitialMode(boolean mode, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setInitialMode(mode);
    }

    /** トップページ用のURLを設定する */
    public void setTopPage(CharArray ch) {
        ((AnchorStringData)itemData).setTopPage(ch);
    }

    /** トップページ用のURLを設定する */
    public void setTopPage(CharArray ch, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setTopPage(ch);
    }
    public void setTopPage(CharArray ch, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setTopPage(ch);
    }

    /** トップページ用のURLを設定する
        @param mode セッションIDを付加するか？
    */
    public void setTopPage(CharArray ch,boolean mode) {
        ((AnchorStringData)itemData).setTopPage(ch,mode);
    }
    /** トップページ用のURLを設定する
        @param mode セッションIDを付加するか？
    */
    public void setTopPage(CharArray ch, boolean mode,int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setTopPage(ch,mode);
    }
    public void setTopPage(CharArray ch, boolean mode,SessionObject session) {
        ((AnchorStringData)getItemData(session)).setTopPage(ch,mode);
    }
    /** ラベルを指定する  */
    public void setLabel(CharArray ch) {
        ((AnchorStringData)itemData).setLabel(ch);
    }
    /** ラベルを指定する  */
    public void setLabel(CharArray ch, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setLabel(ch);
    }
    public void setLabel(CharArray ch, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setLabel(ch);
    }
    /** ラベルを指定する  */
    public void setLabel(String str) {
        ((AnchorStringData)itemData).setLabel(str);
    }
    /** ラベルを指定する  */
    public void setLabel(String str, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setLabel(str);
    }
    public void setLabel(String str, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setLabel(str);
    }

    /** align 設定 */
    public void setAlign(int align) {
        ((AnchorStringData)itemData).setAlign(align);
    }
    /** align 設定 */
    public void setAlign(int align, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setAlign(align);
    }
    public void setAlign(int align, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setAlign(align);
    }

    /** 色指定  */
    public void setColor(CharArray ch) {
        ((AnchorStringData)itemData).setColor(ch);
    }
    /** 色指定  */
    public void setColor(CharArray ch, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setColor(ch);
    }
    public void setColor(CharArray ch, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setColor(ch);
    }
    /** 色指定  */
    public void setColor(String str) {
        ((AnchorStringData)itemData).setColor(str);
    }
    /** 色指定  */
    public void setColor(String str, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setColor(str);
    }
    public void setColor(String str, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setColor(str);
    }

    /** サイズ指定  */
    public void setSize(CharArray ch) {
        ((AnchorStringData)itemData).setSize(ch);
    }
    /** サイズ指定  */
    public void setSize(CharArray ch, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setSize(ch);
    }
    public void setSize(CharArray ch, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setSize(ch);
    }
    /** サイズ指定  */
    public void setSize(String str) {
        ((AnchorStringData)itemData).setSize(str);
    }
    /** サイズ指定  */
    public void setSize(String str, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setSize(str);
    }
    public void setSize(String str, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setSize(str);
    }
    /** サイズ指定  */
    public void setSize(int size) {
        ((AnchorStringData)itemData).setSize(size);
    }
    /** サイズ指定  */
    public void setSize(int size, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setSize(size);
    }
    public void setSize(int size, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setSize(size);
    }

    /** BOLD指定  */
    public void setBold(boolean b) {
        ((AnchorStringData)itemData).setBold(b);
    }
    /** BOLD指定  */
    public void setBold(boolean b, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setBold(b);
    }
    public void setBold(boolean b, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setBold(b);
    }

    /** Italic指定  */
    public void setItalic(boolean b) {
        ((AnchorStringData)itemData).setItalic(b);
    }
    /** Italic指定  */
    public void setItalic(boolean b, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setItalic(b);
    }
    public void setItalic(boolean b, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setItalic(b);
    }

    /** UTN指定（i-modeのみ有効）  */
    public void setUtn(boolean b) {
        ((AnchorStringData)itemData).setUtn(b);
    }
    /** UTN指定  */
    public void setUtn(boolean b, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setUtn(b);
    }
    public void setUtn(boolean b, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setUtn(b);
    }

    /** タグ変換を行うか？  */
    public void setTagConvert(boolean b) {
        ((AnchorStringData)itemData).setTagConvert(b);
    }

    /** タグ変換を行うか？  */
    public void setTagConvert(boolean b, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setTagConvert(b);
    }
    public void setTagConvert(boolean b, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setTagConvert(b);
    }

    /** 等幅フォントを使用  */
    public void setTT(boolean b) {
        ((AnchorStringData)itemData).setTT(b);
    }

    /** 等幅フォントを使用  */
    public void setTT(boolean b, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).setTT(b);
    }
    public void setTT(boolean b, SessionObject session) {
        ((AnchorStringData)getItemData(session)).setTT(b);
    }

    //---------------------------------------------------------------------
    // データ取得メソッド
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() {
        return ((AnchorStringData)itemData).getValue();
    }
    public CharArrayQueue getValue(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getValue();
    }
    public CharArrayQueue getValue(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getValue();
    }
    /** 表示文字列を取得する */
    public CharArray getText() {
        return ((AnchorStringData)itemData).getText();
    }

    /** 表示文字列を取得する */
    public CharArray getText(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getText();
    }

    /** 表示文字列を取得する */
    public CharArray getText(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getText();
    }

    /** 色を取得する */
    public CharArray getColor() {
        return ((AnchorStringData)itemData).getColor();
    }

    /** 色を取得する */
    public CharArray getColor(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getColor();
    }
    public CharArray getColor(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getColor();
    }


    /** 付加されるURLを取得する */
    public CharArray getURL() {
        return ((AnchorStringData)itemData).getURL();
    }

    /** 付加されるURLを取得する */
    public CharArray getURL(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getURL();
    }
    public CharArray getURL(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getURL();
    }
    /** 付加されるSSL用のURLを取得する */
    public CharArray getSSLURL() {
        return ((AnchorStringData)itemData).getSSLURL();
    }

    /** 付加されるSSL用のURLを取得する */
    public CharArray getSSLURL(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getSSLURL();
    }
    public CharArray getSSLURL(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getSSLURL();
    }
    /** ターゲットフレームを取得する */
    public CharArray getTarget() {
        return ((AnchorStringData)itemData).getTarget();
    }

    /** ターゲットフレームを取得する */
    public CharArray getTarget(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getTarget();
    }
    public CharArray getTarget(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getTarget();
    }

    /** アクセスキーを取得する */
    public char getAccessKey() {
        return ((AnchorStringData)itemData).getAccessKey();
    }

    /** アクセスキーを取得する */
    public char getAccessKey(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getAccessKey();
    }
    public char getAccessKey(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getAccessKey();
    }

    /**  href= 以降の文字列を返します */
    public CharArray getAnchorURL() {
        return ((AnchorStringData)itemData).getAnchorURL();
    }

    /**  href= 以降の文字列を返します */
    public CharArray getAnchorURL(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getAnchorURL();
    }

    /** UTN(i-modeのみ）を取得する */
    public boolean getUtn() {
        return ((AnchorStringData)itemData).getUtn();
    }

    /** UTN（i-modeのみ）を取得する */
    public boolean getUtn(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getUtn();
    }
    public boolean getUtn(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getUtn();
    }

    /** Tag変換情報を取得する */
    public boolean getTagConvert() {
        return ((AnchorStringData)itemData).getTagConvert();
    }

    /** Tag変換情報を取得する */
    public boolean getTagConvert(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getTagConvert();
    }
    public boolean getTagConvert(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getTagConvert();
    }

    /** 新規セッション用URLを生成するか？ */
    public boolean getInitialMode() {
        return ((AnchorStringData)itemData).getInitialMode();
    }

    /** 等幅フォントか？ */
    public boolean getInitialMode(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getInitialMode();
    }
    public boolean getInitialMode(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getInitialMode();
    }

    /** 等幅フォントか？ */
    public boolean getTT() {
        return ((AnchorStringData)itemData).getTT();
    }

    /** UTN（i-modeのみ）を取得する */
    public boolean getTT(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getTT();
    }
    public boolean getTT(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getTT();
    }

    //---------------------------------------------------------------------
    // キー関連
    //---------------------------------------------------------------------
    /**
      デフォルトパラメータ用のキーリストを取得する<br>
      (存在する場合はセッションから情報を取得してパラメータ出力する)<br>
      nullは返さない
    */
    public CharArrayQueue getCheckKeys() {
        return ((AnchorStringData)getItemData()).getCheckKeys();
    }
    public CharArrayQueue getCheckKeys(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getCheckKeys();
    }
    public CharArrayQueue getCheckKeys(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getCheckKeys();
    }
    /** キーリストをクリアする */
    public void clearCheckKeys() {
        ((AnchorStringData)getItemData()).clearCheckKeys();
    }
    public void clearCheckKeys(int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).clearCheckKeys();
    }
    public void clearCheckKeys(SessionObject session) {
        ((AnchorStringData)getItemData(session)).clearCheckKeys();
    }
    /** キーリストに追加する */
    public void addCheckKeys(CharArray key) {
        ((AnchorStringData)getItemData()).addCheckKeys(key);
    }
    public void addCheckKeys(String key) {
        ((AnchorStringData)getItemData()).addCheckKeys(key);
    }
    public void addCheckKeys(String key1, String key2) {
        ((AnchorStringData)getItemData()).addCheckKeys(key1, key2);
    }
    public void addCheckKeys(String key1, String key2, String key3) {
        ((AnchorStringData)getItemData()).addCheckKeys(key1, key2, key3);
    }
    public void addCheckKeys(String key1, String key2, String key3, String key4) {
        ((AnchorStringData)getItemData()).addCheckKeys(key1, key2, key3, key4);
    }

    public void addCheckKeys(CharArray key, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).addCheckKeys(key);
    }
    public void addCheckKeys(String key, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).addCheckKeys(key);
    }
    public void addCheckKeys(String key1, String key2, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).addCheckKeys(key1, key2);
    }
    public void addCheckKeys(String key1, String key2, String key3, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).addCheckKeys(key1, key2, key3);
    }
    public void addCheckKeys(String key1, String key2, String key3, String key4, int sessionID) {
        ((AnchorStringData)getItemData(sessionID)).addCheckKeys(key1, key2, key3, key4);
    }

    public void addCheckKeys(CharArray key, SessionObject session) {
        ((AnchorStringData)getItemData(session)).addCheckKeys(key);
    }
    public void addCheckKeys(String key, SessionObject session) {
        ((AnchorStringData)getItemData(session)).addCheckKeys(key);
    }
    public void addCheckKeys(String key1, String key2, SessionObject session) {
        ((AnchorStringData)getItemData(session)).addCheckKeys(key1, key2);
    }
    public void addCheckKeys(String key1, String key2, String key3, SessionObject session) {
        ((AnchorStringData)getItemData(session)).addCheckKeys(key1, key2, key3);
    }
    public void addCheckKeys(String key1, String key2, String key3, String key4, SessionObject session) {
        ((AnchorStringData)getItemData(session)).addCheckKeys(key1, key2, key3, key4);
    }

    //---------------------------------------------------------------------
    // レンダリング
    //---------------------------------------------------------------------
    public CharArray getSrcTag(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getSrcTag();
    }
    public CharArray getMenuTag(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getMenuTag();
    }
    public CharArray getTag(int sessionID) {
        return ((AnchorStringData)getItemData(sessionID)).getTag();
    }
    public CharArray getSrcTag(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getSrcTag();
    }
    public CharArray getMenuTag(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getMenuTag();
    }
    public CharArray getTag(SessionObject session) {
        return ((AnchorStringData)getItemData(session)).getTag();
    }

    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    /** ユーザーオブジェクトをコピー生成する */
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new AnchorStringData((AnchorStringData)itemData,session);
            newData.setItem(this);
            session.getHashtable().put(new IntObject(getMixedID()),newData);
        }
    }

    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** 描画を行う */
    public CharArray draw(SessionObject session) {
        AnchorStringData data = (AnchorStringData)getItemData(session);
        //return session.itemRenderer.drawAnchorString(session, data);
        return data.draw(session);
    }


    // stream 版
    //public void draw(OutputStream out) {
    //    //未作成
    //}
    //public void draw(OutputStream out, int sessionID) {
    //    //未作成
    //}
    /**
      コンバートを行う
    */
    public void convert(SessionObject session) {
        CharArray org = getText();
        if (org.length() > 0 && langConvert) {
            CharArray ch = session.getMessage(org.toString());
            if (ch != null) setText(ch,session.getSessionID());
        }
        org = getColor();
        if (org.length() > 0 && colorConvert) {
            CharArray ch = session.getColor(org.toString());
            if (ch != null) setColor(ch,session.getSessionID());
        }
    }
}

//
// [end of AnchorStringItem.java]
//

