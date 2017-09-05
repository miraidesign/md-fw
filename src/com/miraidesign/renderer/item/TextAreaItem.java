//------------------------------------------------------------------------
// @(#)TextAreaItem.java
//                 テキストエリア
//                 Copyright (c) Miraidesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.IntObject;

/**
 *  テキストエリア入力アイテム
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class TextAreaItem extends Item {


    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /**
        TextAreaItemオブジェクトを生成する
    */
    public TextAreaItem() { 
        super();
        init();
        itemData = new TextAreaData();
        itemData.setItem(this);
    }
    /**
        TextAreaItemオブジェクトを生成する
        @param value デフォルト値
    */
    public TextAreaItem(String value) { 
        super();
        init();
        itemData = new TextAreaData(value);
        itemData.setItem(this);
    }
    /**
        TextAreaItemオブジェクトを生成する
        @param value デフォルト値
    */
    public TextAreaItem(CharArray value) { 
        super();
        init();
        itemData = new TextAreaData(value);
        itemData.setItem(this);
    }
    /**
        TextAreaItemオブジェクトを生成する
        @param value デフォルト値
        @param rows  行数
    */
    public TextAreaItem(String value, int rows) { 
        super();
        init();
        itemData = new TextAreaData(value, rows);
        itemData.setItem(this);
    }
    /**
        TextAreaItemオブジェクトを生成する
        @param value デフォルト値
        @param rows  行数
    */
    public TextAreaItem(CharArray value, int rows) { 
        super();
        init();
        itemData = new TextAreaData(value, rows);
        itemData.setItem(this);
    }
    /**
        TextAreaItemオブジェクトを生成する
        @param value デフォルト値
        @param rows  行数
        @param cols  列数
    */
    public TextAreaItem(String value, int rows, int cols) { 
        super();
        init();
        itemData = new TextAreaData(value, rows, cols);
        itemData.setItem(this);
    }
    /**
        TextAreaItemオブジェクトを生成する
        @param value デフォルト値
        @param rows  行数
        @param cols  列数
    */
    public TextAreaItem(CharArray value, int rows, int cols) { 
        super();
        init();
        itemData = new TextAreaData(value, rows, cols);
        itemData.setItem(this);
    }
    /**
        TextAreaItemオブジェクトを生成する(Copy constructor)
    */
    public TextAreaItem(TextAreaItem from) { 
        super();
        setType(TEXTAREA);
        setCloneable(from.isCloneable());
        TextAreaData fromdata = (TextAreaData)from.itemData;
        itemData = new TextAreaData(fromdata, fromdata.getSessionObject());
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
    /** 入力文字列設定 */
    public  void setText(CharArray ch) {
        ((TextAreaData)itemData).setText(ch);
    }
    public  void setText(CharArray ch, int sessionID) {
        ((TextAreaData)getItemData(sessionID)).setText(ch);
    }
    public  void setText(CharArray ch, SessionObject session) {
        ((TextAreaData)getItemData(session)).setText(ch);
    }
    public  void setText(String str) {
        ((TextAreaData)itemData).setText(str);
    }
    public  void setText(String str, int sessionID) {
        ((TextAreaData)getItemData(sessionID)).setText(str);
    }
    public  void setText(String str, SessionObject session) {
        ((TextAreaData)getItemData(session)).setText(str);
    }

    /** 表示行数設定 
        @param rows 行数
    */
    public  void setRows(int rows) {
        ((TextAreaData)itemData).setRows(rows);
    }
    public  void setRows(int rows, int sessionID) {
        ((TextAreaData)getItemData(sessionID)).setRows(rows);
    }
    public  void setRows(int rows, SessionObject session) {
        ((TextAreaData)getItemData(session)).setRows(rows);
    }

    /** 表示桁数設定 
        @param cols 行数
    */
    public  void setCols(int cols) {
        ((TextAreaData)itemData).setCols(cols);
    }
    public  void setCols(int cols, int sessionID) {
        ((TextAreaData)getItemData(sessionID)).setCols(cols);
    }
    public  void setCols(int cols, SessionObject session) {
        ((TextAreaData)getItemData(session)).setCols(cols);
    }

    /** istyle設定 
        @param istyle スタイル
    */
    public  void setIstyle(int istyle) {
        ((TextAreaData)itemData).setIstyle(istyle);
    }
    public  void setIstyle(int istyle, int sessionID) {
        ((TextAreaData)getItemData(sessionID)).setIstyle(istyle);
    }
    public  void setIstyle(int istyle, SessionObject session) {
        ((TextAreaData)getItemData(session)).setIstyle(istyle);
    }

    /** アクセスキー設定 */
    public void setAccessKey(char key) {
        ((TextAreaData)itemData).setAccessKey(key);
    }
    public void setAccessKey(char key, int sessionID) {
        ((TextAreaData)getItemData(sessionID)).setAccessKey(key);
    }
    public void setAccessKey(char key, SessionObject session) {
        ((TextAreaData)getItemData(session)).setAccessKey(key);
    }

    /** データのクリア*/
    public  void clear() {
        ((TextAreaData)itemData).clear();
    }
    public  void clear(int sessionID) {
        ((TextAreaData)getItemData(sessionID)).clear();
    }
    public  void clear(SessionObject session) {
        ((TextAreaData)getItemData(session)).clear();
    }

    /** 入力オブジェクトの変更と選択を禁止する **/
    public void setDisabled(boolean mode) {
        ((TextAreaData)itemData).setDisabled(mode);
    }
    public void setDisabled(boolean mode, int sessionID) {
        ((TextAreaData)getItemData(sessionID)).setDisabled(mode);
    }
    public void setDisabled(boolean mode, SessionObject session) {
        ((TextAreaData)getItemData(session)).setDisabled(mode);
    }

    /** 入力オブジェクトの変更を禁止する（選択は可能） **/
    public void setReadonly(boolean mode) {
        ((TextAreaData)itemData).setReadonly(mode);
    }
    public void setReadonly(boolean mode, int sessionID) {
        ((TextAreaData)getItemData(sessionID)).setReadonly(mode);
    }
    public void setReadonly(boolean mode, SessionObject session) {
        ((TextAreaData)getItemData(session)).setReadonly(mode);
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
        return ((TextAreaData)itemData).getValue();
    }
    
    /** ユーザー毎の入力文字列を取得する
        @param sessionID セッションＩＤ
        @return 入力文字列 */
    public CharArrayQueue getValue(int sessionID) {
        return ((TextAreaData)getItemData(sessionID)).getValue();
    }
    public CharArrayQueue getValue(SessionObject session) {
        return ((TextAreaData)getItemData(session)).getValue();
    }

    /** デフォルト入力文字列を取得する
        @return 入力文字列 */
    public CharArray getText() {
        return ((TextAreaData)itemData).getText();
    }
        // ※注：String で取り出すときは getValue().toString() 等とする
        //       ただし、Stringの多用はシステムに負担を与えるため、
        //       なるべく使用しないこと
    
    /** ユーザー毎の入力文字列を取得する
        @param sessionID セッションＩＤ
        @return 入力文字列 */
    public CharArray getText(int sessionID) {
        return ((TextAreaData)getItemData(sessionID)).getText();
    }
    public CharArray getText(SessionObject session) {
        return ((TextAreaData)getItemData(session)).getText();
    }

    /** 行数を取得する
        @return 行数 */
    public int getRows() {
        return ((TextAreaData)itemData).getRows();
    }
    public int getRows(int sessionID) {
        return ((TextAreaData)getItemData(sessionID)).getRows();
    }
    public int getRows(SessionObject session) {
        return ((TextAreaData)getItemData(session)).getRows();
    }

    /** 桁数を取得する
        @return 桁数 */
    public int getCols() {
        return ((TextAreaData)itemData).getCols();
    }
    public int getCols(int sessionID) {
        return ((TextAreaData)getItemData(sessionID)).getCols();
    }
    public int getCols(SessionObject session) {
        return ((TextAreaData)getItemData(session)).getCols();
    }

    /** istyleを取得する
        @return istyle */
    public int getIstyle() {
        return ((TextAreaData)itemData).getIstyle();
    }
    public int getIstyle(int sessionID) {
        return ((TextAreaData)getItemData(sessionID)).getIstyle();
    }
    public int getIstyle(SessionObject session) {
        return ((TextAreaData)getItemData(session)).getIstyle();
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
        return ((TextAreaData)itemData).isDisabled();
    }
    public boolean isDisabled(int sessionID) {
        return ((TextAreaData)getItemData(sessionID)).isDisabled();
    }
    public boolean isDisabled(SessionObject session) {
        return ((TextAreaData)getItemData(session)).isDisabled();
    }
    /** readonlu? */
    public boolean isReadonly() {
        return ((TextAreaData)itemData).isReadonly();
    }
    public boolean isReadonly(int sessionID) {
        return ((TextAreaData)getItemData(sessionID)).isReadonly();
    }
    public boolean isReadonly(SessionObject session) {
        return ((TextAreaData)getItemData(session)).isReadonly();
    }
    //---------------------------------------------------------------------
    // レンダリング
    //---------------------------------------------------------------------
    public CharArray getTag(int sessionID) {
        return ((TextAreaData)getItemData(sessionID)).getTag();
    }
    public CharArray getMenuTag(int sessionID) {
        return ((TextAreaData)getItemData(sessionID)).getMenuTag();
    }
    public CharArray getNameTag(int sessionID) {
        return ((TextAreaData)getItemData(sessionID)).getNameTag();
    }
    public CharArray getNameMenuTag(int sessionID) {
        return ((TextAreaData)getItemData(sessionID)).getNameMenuTag();
    }
    //
    public CharArray getTag(SessionObject session) {
        return ((TextAreaData)getItemData(session)).getTag();
    }
    public CharArray getMenuTag(SessionObject session) {
        return ((TextAreaData)getItemData(session)).getMenuTag();
    }
    public CharArray getNameTag(SessionObject session) {
        return ((TextAreaData)getItemData(session)).getNameTag();
    }
    public CharArray getNameMenuTag(SessionObject session) {
        return ((TextAreaData)getItemData(session)).getNameMenuTag();
    }

    //---------------------------------------------------------------------
    // copy 
    //---------------------------------------------------------------------
    /** デフォルトのオブジェクトをコピーしてユーザーオブジェクトを作成する
        @param session セッション
    */
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new TextAreaData((TextAreaData)itemData,session);
            newData.setItem(this);
            session.getHashtable().put(new IntObject(getMixedID()),newData);
        }
    }
    

    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする
        @param session セッション
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        TextAreaData data = (TextAreaData)getItemData(session);
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
        CharArray text = getText(session.getSessionID());
        if (org.length() > 0 && (langConvert || colorConvert)) {
            text.clear();
            CharToken token = CharToken.pop();
            token.set(org, "\n");
            for (int i = 0; i < token.size(); i++) {
                CharArray ca = token.get(i);
                CharArray ch = null;
                if (langConvert)  ch = session.getMessage(ca.toString());
                if (ch == null && colorConvert) ch = session.getColor(ca.toString());
                text.add((ch != null) ? ch : ca);
                if (i < token.size()-1) text.add("\n");
            }
            CharToken.push(token);
        }
    }

 
}

//
// [end of TextAreaItem.java]
//

