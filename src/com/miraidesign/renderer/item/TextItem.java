//------------------------------------------------------------------------
// @(#)TextItem.java
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
 *  テキスト入力アイテム(input type=text)
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class TextItem extends Item {

    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /**
        TextItemオブジェクトを生成する
    */
    public TextItem() { 
        super();
        init();
        itemData = new TextData();
        itemData.setItem(this);
    }
    /**
        TextItemオブジェクトを生成する
        @param value デフォルト値
    */
    public TextItem(String value) { 
        super();
        init();
        itemData = new TextData(value);
        itemData.setItem(this);
    }
    /**
        TextItemオブジェクトを生成する
        @param value デフォルト値
    */
    public TextItem(CharArray value) { 
        super();
        init();
        itemData = new TextData(value);
        itemData.setItem(this);
    }
    /**
        TextItemオブジェクトを生成する(Copy constructor)
    */
    public TextItem(TextItem from) { 
        super();
        setType(TEXT);
        setCloneable(from.isCloneable());
        TextData fromdata = (TextData)from.itemData;
        itemData = new TextData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }

    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(TEXT);
        setCloneable(true);
    }
    
    //---------------------------------------------------------------------
    // データ設定メソッド
    //---------------------------------------------------------------------
    /** テキスト情報をセットする 
        @param str テキスト情報
    */
    public  void setText(String str) {
        ((TextData)itemData).setText(str);
    }
    public  void setText(String str, int sessionID) {
        ((TextData)getItemData(sessionID)).setText(str);
    }
    public  void setText(String str, SessionObject session) {
        ((TextData)getItemData(session)).setText(str);
    }

    /** テキスト情報をセットする 
        @param ch テキスト情報
    */
    public  void setText(CharArray ch) {
        ((TextData)itemData).setText(ch);
    }
    public  void setText(CharArray ch, int sessionID) {
        ((TextData)getItemData(sessionID)).setText(ch);
    }
    public  void setText(CharArray ch, SessionObject session) {
        ((TextData)getItemData(session)).setText(ch);
    }

    /** 数値情報をセットする 
        @param num 数値
    */
    public  void setInt(int num) {
        ((TextData)itemData).setInt(num);
    }
    public  void setInt(int num, int sessionID) {
        ((TextData)getItemData(sessionID)).setInt(num);
    }
    public  void setInt(int num, SessionObject session) {
        ((TextData)getItemData(session)).setInt(num);
    }
    /** 数値情報をセットする 
        @param num 数値
    */
    public  void setLong(long num) {
        ((TextData)itemData).setLong(num);
    }
    public  void setLong(long num, int sessionID) {
        ((TextData)getItemData(sessionID)).setLong(num);
    }
    public  void setLong(long num, SessionObject session) {
        ((TextData)getItemData(session)).setLong(num);
    }

    /** 表示幅を設定する 
        @param size 表示サイズ
    */
    public void setSize(int size) {
        ((TextData)itemData).setSize(size);
    }
    public  void setSize(int size, int sessionID) {
        ((TextData)getItemData(sessionID)).setSize(size);
    }
    public  void setSize(int size, SessionObject session) {
        ((TextData)getItemData(session)).setSize(size);
    }

    /** 入力最大文字数を指定する 
        @param size 最大文字数
    */
    public void setMaxLength(int size) {
        ((TextData)itemData).setMaxLength(size);
    }
    public  void setMaxLength(int size, int sessionID) {
        ((TextData)getItemData(sessionID)).setMaxLength(size);
    }
    public  void setMaxLength(int size, SessionObject session) {
        ((TextData)getItemData(session)).setMaxLength(size);
    }

    /** istyleの設定 
        @param istyle Item.HIRAGANA/KATAKANA/ALPHABET/NUMERIC
    */
    public void setIstyle(int istyle) {
        ((TextData)itemData).setIstyle(istyle);
    }
    public  void setIstyle(int istyle, int sessionID) {
        ((TextData)getItemData(sessionID)).setIstyle(istyle);
    }
    public  void setIstyle(int istyle, SessionObject session) {
        ((TextData)getItemData(session)).setIstyle(istyle);
    }

    /** アクセスキー設定 */
    public void setAccessKey(char key) {
        ((TextData)itemData).setAccessKey(key);
    }
    public void setAccessKey(char key, int sessionID) {
        ((TextData)getItemData(sessionID)).setAccessKey(key);
    }
    public void setAccessKey(char key, SessionObject session) {
        ((TextData)getItemData(session)).setAccessKey(key);
    }

    /** データのクリア*/
    public  void clear() {
        ((TextData)itemData).clear();
    }
    public  void clear(int sessionID) {
        ((TextData)getItemData(sessionID)).clear();
    }
    public  void clear(SessionObject session) {
        ((TextData)getItemData(session)).clear();
    }

    /** 入力オブジェクトの変更と選択とPOSTを禁止する **/
    public void setDisabled(boolean mode) {
        ((TextData)itemData).setDisabled(mode);
    }
    public void setDisabled(boolean mode, int sessionID) {
        ((TextData)getItemData(sessionID)).setDisabled(mode);
    }
    public void setDisabled(boolean mode, SessionObject session) {
        ((TextData)getItemData(session)).setDisabled(mode);
    }

    /** 入力オブジェクトの変更を禁止する（選択は可能） **/
    public void setReadonly(boolean mode) {
        ((TextData)itemData).setReadonly(mode);
    }
    
    public void setReadonly(boolean mode, int sessionID) {
        ((TextData)getItemData(sessionID)).setReadonly(mode);
    }
    public void setReadonly(boolean mode, SessionObject session) {
        ((TextData)getItemData(session)).setReadonly(mode);
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
    /** デフォルト入力文字列を取得する
        @return 入力文字列 */
    public CharArrayQueue getValue() {
        return ((TextData)itemData).getValue();
    }
    
    /** ユーザー毎の入力文字列を取得する
        @param sessionID セッションＩＤ
        @return 入力文字列 */
    public CharArrayQueue getValue(int sessionID) {
        return ((TextData)getItemData(sessionID)).getValue();
    }
    public CharArrayQueue getValue(SessionObject session) {
        return ((TextData)getItemData(session)).getValue();
    }

    /** デフォルト入力文字列を取得する
        @return 入力文字列 */
    public CharArray getText() {
        return ((TextData)itemData).getText();
    }
        // ※注：String で取り出すときは getValue().toString() 等とする
        //       ただし、Stringの多用はシステムに負担を与えるため、
        //       なるべく使用しないこと
    
    /** ユーザー毎の入力文字列を取得する
        @param sessionID セッションＩＤ
        @return 入力文字列 */
    public CharArray getText(int sessionID) {
        return ((TextData)getItemData(sessionID)).getText();
    }
    public CharArray getText(SessionObject session) {
        return ((TextData)getItemData(session)).getText();
    }

    /** 入力値を取得する */
    public int getInt() {
        return ((TextData)itemData).getInt();
    }
    public int getInt(int sessionID) {
        return ((TextData)getItemData(sessionID)).getInt();
    }
    public int getInt(SessionObject session) {
        return ((TextData)getItemData(session)).getInt();
    }
    /** 入力値を取得する */
    public long getLong() {
        return ((TextData)itemData).getLong();
    }
    public long getLong(int sessionID) {
        return ((TextData)getItemData(sessionID)).getLong();
    }
    public long getLong(SessionObject session) {
        return ((TextData)getItemData(session)).getLong();
    }

    /** アクセスキー取得 */
    public char getAccessKey() {
        return ((TextData)itemData).getAccessKey();
    }
    public char getAccessKey(int sessionID) {
        return ((TextData)getItemData(sessionID)).getAccessKey();
    }
    public char getAccessKey(SessionObject session) {
        return ((TextData)getItemData(session)).getAccessKey();
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
    
    /** readonly? */
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
        return ((TextData)getItemData(sessionID)).getTag();
    }
    public CharArray getNameTag(int sessionID) {
        return ((TextData)getItemData(sessionID)).getNameTag();
    }
    public CharArray getTag(SessionObject session) {
        return ((TextData)getItemData(session)).getTag();
    }
    public CharArray getNameTag(SessionObject session) {
        return ((TextData)getItemData(session)).getNameTag();
    }

    //---------------------------------------------------------------------
    // copy 
    //---------------------------------------------------------------------
    /** デフォルトのオブジェクトをコピーしてユーザーオブジェクトを作成する
        @param session セッションオブジェクト
    */
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new TextData((TextData)itemData,session);
            newData.setItem(this);
            session.getHashtable().put(new IntObject(getMixedID()),newData);
        }
    }

    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする
        @param session セッションオブジェクト
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        TextData data = (TextData)getItemData(session);
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
                if (ch != null) setText(ch,session);
            }
            if (colorConvert && ch == null) {
                ch = session.getColor(org.toString());
                if (ch != null) setText(ch,session);
            }
        }
    }
}

//
// [end of TextItem.java]
//

