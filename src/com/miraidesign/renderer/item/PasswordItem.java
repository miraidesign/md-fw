//------------------------------------------------------------------------
// @(#)PasswordItem.java
//                 input type = text
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.IntObject;

/**
 *  パスワード入力アイテム(input type=password)
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class PasswordItem extends Item {


    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /**
        PasswordItemオブジェクトを生成する
    */
    public PasswordItem() { 
        super();
        init();
        itemData = new PasswordData();
        itemData.setItem(this);
    }
    /**
        PasswordItemオブジェクトを生成する
        @param value デフォルト値
    */
    public PasswordItem(String value) { 
        super();
        init();
        itemData = new PasswordData(value);
        itemData.setItem(this);
    }
    /**
        PasswordItemオブジェクトを生成する
        @param value デフォルト値
    */
    public PasswordItem(CharArray value) { 
        super();
        init();
        itemData = new PasswordData(value);
        itemData.setItem(this);
    }
    /**
        PasswordItemオブジェクトを生成する (copy constructor)
    */
    public PasswordItem(PasswordItem from) { 
        super();
        setType(PASSWORD);
        setCloneable(from.isCloneable());
        PasswordData fromdata = (PasswordData)from.itemData;
        itemData = new PasswordData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }

    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(PASSWORD);
        setCloneable(true);
    }
    
    //---------------------------------------------------------------------
    // データ設定
    //---------------------------------------------------------------------
    /** テキスト情報をセットする 
        @param str テキスト情報
    */
    public  void setText(String str) {
        ((PasswordData)itemData).setText(str);
    }
    public  void setText(String str, int sessionID) {
        ((PasswordData)getItemData(sessionID)).setText(str);
    }
    public  void setText(String str, SessionObject session) {
        ((PasswordData)getItemData(session)).setText(str);
    }

    /** テキスト情報をセットする 
        @param ch テキスト情報
    */
    public  void setText(CharArray ch) {
        ((PasswordData)itemData).setText(ch);
    }
    public  void setText(CharArray ch, int sessionID) {
        ((PasswordData)getItemData(sessionID)).setText(ch);
    }
    public  void setText(CharArray ch, SessionObject session) {
        ((PasswordData)getItemData(session)).setText(ch);
    }

    /** 表示幅を設定する 
        @param size 表示サイズ
    */
    public void setSize(int size) {
        ((PasswordData)itemData).setSize(size);
    }
    public  void setSize(int size, int sessionID) {
        ((PasswordData)getItemData(sessionID)).setSize(size);
    }
    public  void setSize(int size, SessionObject session) {
        ((PasswordData)getItemData(session)).setSize(size);
    }

    /** 入力最大文字数を指定する 
        @param size  最大文字数
    */
    public void setMaxLength(int size) {
        ((PasswordData)itemData).setMaxLength(size);
    }
    public  void setMaxLength(int size, int sessionID) {
        ((PasswordData)getItemData(sessionID)).setMaxLength(size);
    }
    public  void setMaxLength(int size, SessionObject session) {
        ((PasswordData)getItemData(session)).setMaxLength(size);
    }

    /** istyleの設定 
        @param istyle Item.HIRAGANA/KATAKANA/ALPHABET/NUMERIC
    */
    public void setIstyle(int istyle) {
        ((PasswordData)itemData).setIstyle(istyle);
    }
    public  void setIstyle(int istyle, int sessionID) {
        ((PasswordData)getItemData(sessionID)).setIstyle(istyle);
    }
    public  void setIstyle(int istyle, SessionObject session) {
        ((PasswordData)getItemData(session)).setIstyle(istyle);
    }

    /** アクセスキー設定 */
    public void setAccessKey(char key) {
        ((PasswordData)itemData).setAccessKey(key);
    }
    public void setAccessKey(char key, int sessionID) {
        ((PasswordData)getItemData(sessionID)).setAccessKey(key);
    }
    public void setAccessKey(char key, SessionObject session) {
        ((PasswordData)getItemData(session)).setAccessKey(key);
    }

    /** データのクリア*/
    public  void clear() {
        ((PasswordData)itemData).clear();
    }
    public  void clear(int sessionID) {
        ((PasswordData)getItemData(sessionID)).clear();
    }
    public  void clear(SessionObject session) {
        ((PasswordData)getItemData(session)).clear();
    }

    /** 入力オブジェクトの変更と選択とPOSTを禁止する **/
    public void setDisabled(boolean mode) {
        ((PasswordData)itemData).setDisabled(mode);
    }
    public void setDisabled(boolean mode, int sessionID) {
        ((PasswordData)getItemData(sessionID)).setDisabled(mode);
    }
    public void setDisabled(boolean mode, SessionObject session) {
        ((PasswordData)getItemData(session)).setDisabled(mode);
    }

    /** 入力オブジェクトの変更を禁止する（選択は可能） **/
    public void setReadonly(boolean mode) {
        ((PasswordData)itemData).setReadonly(mode);
    }
    public void setReadonly(boolean mode, int sessionID) {
        ((PasswordData)getItemData(sessionID)).setReadonly(mode);
    }
    public void setReadonly(boolean mode, SessionObject session) {
        ((PasswordData)getItemData(session)).setReadonly(mode);
    }

    /** テーブルからセットする */
    public boolean setParameter(HashParameter param, int sessionID) {
        boolean rsts = false;
        CharArray ch = param.get(getText());
        if (ch != null) {
            setText(ch, sessionID);
            rsts = true;
        }
        return rsts;
    }
    public boolean setParameter(HashParameter param, SessionObject session) {
        boolean rsts = false;
        CharArray ch = param.get(getText());
        if (ch != null) {
            setText(ch, session);
            rsts = true;
        }
        return rsts;
    }
    
    //---------------------------------------------------------------------
    // データ取得メソッド
    //---------------------------------------------------------------------
    /** デフォルトパスワード文字列を取得する
        @return パスワード文字列 */
    public CharArrayQueue getValue() {
        return ((PasswordData)itemData).getValue();
    }
    
    /** ユーザー毎のパスワード文字列を取得する
        @param sessionID セッションＩＤ
        @return パスワード文字列 */
    public CharArrayQueue getValue(int sessionID) {
        return ((PasswordData)getItemData(sessionID)).getValue();
    }
    public CharArrayQueue getValue(SessionObject session) {
        return ((PasswordData)getItemData(session)).getValue();
    }
    /** デフォルトパスワード文字列を取得する
        @return パスワード文字列 */
    public CharArray getText() {
        return ((PasswordData)itemData).getText();
    }
    
    /** ユーザー毎のパスワード文字列を取得する
        @param sessionID セッションＩＤ
        @return パスワード文字列 */
    public CharArray getText(int sessionID) {
        return ((PasswordData)getItemData(sessionID)).getText();
    }

    /** ユーザー毎のパスワード文字列を取得する
        @param session セッション
        @return パスワード文字列 */
    public CharArray getText(SessionObject session) {
        return ((PasswordData)getItemData(session)).getText();
    }
    
    /** アクセスキー取得 */
    public char getAccessKey() {
        return ((PasswordData)itemData).getAccessKey();
    }
    public char getAccessKey(int sessionID) {
        return ((PasswordData)getItemData(sessionID)).getAccessKey();
    }
    public char getAccessKey(SessionObject session) {
        return ((PasswordData)getItemData(session)).getAccessKey();
    }
    
    /** disabled ? */
    public boolean isDisabled() {
        return ((TextData)itemData).isDisabled();
    }
    public boolean isDisabled(int sessionID) {
        return ((TextData)getItemData(sessionID)).isDisabled();
    }
    public boolean isDisabled(SessionObject session) {
        return ((TextData)getItemData(session)).isDisabled();
    }
    
    /** readonly ? */
    public boolean isReadonly() {
        return ((TextData)itemData).isReadonly();
    }
    public boolean isReadonly(int sessionID) {
        return ((TextData)getItemData(sessionID)).isReadonly();
    }
    public boolean isReadonly(SessionObject session) {
        return ((TextData)getItemData(session)).isReadonly();
    }

    //---------------------------------------------------------------------
    // レンダリング
    //---------------------------------------------------------------------
    public CharArray getTag(int sessionID) {
        return ((PasswordData)getItemData(sessionID)).getTag();
    }
    public CharArray getNameTag(int sessionID) {
        return ((PasswordData)getItemData(sessionID)).getNameTag();
    }

    public CharArray getTag(SessionObject session) {
        return ((PasswordData)getItemData(session)).getTag();
    }
    public CharArray getNameTag(SessionObject session) {
        return ((PasswordData)getItemData(session)).getNameTag();
    }

    //---------------------------------------------------------------------
    // copy 
    //---------------------------------------------------------------------
    /** デフォルトのオブジェクトをコピーしてユーザーオブジェクトを作成する
        @param session セッション
    */
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new PasswordData((PasswordData)itemData,session);
            newData.setItem(this);
            session.getHashtable().put(new IntObject(getMixedID()),newData);
        }
    }

    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする
        @param session SessionObject
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        PasswordData data = (PasswordData)getItemData(session);
        return data.draw(session);
    }
    // stream 版
    //public void draw(OutputStream out) {
        //未作成
    //}
    public void draw(OutputStream out, int sessionID) {
        //未作成
    }

    /**
      コンバートを行う
    */
    public void convert(SessionObject session) {
        CharArray org = getText();
        if (org.length() > 0) {
            CharArray ch = null;
            if (langConvert) {
                ch = session.getMessage(org.toString());
                if (ch != null) setText(ch,session.getSessionID());
            }
            if (colorConvert && ch == null) {
                ch = session.getColor(org.toString());
                if (ch != null) setText(ch,session.getSessionID());
            }
        }
    }
}

//
//
// [end of PasswordItem.java]
//

