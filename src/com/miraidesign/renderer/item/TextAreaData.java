//------------------------------------------------------------------------
// @(#)TextAreaData.java
//                 テキストエリア のデータを保管する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/** テキストエリア のデータを保管する */
public class TextAreaData extends ItemData {
    private CharArray   chValue = new CharArray();  // 入力文字列データ
    private CharArray   chOriginal = new CharArray();  // 保存用 v0.684
    
    private int rows      = -1;  // 行数            -1: 指定なし
    private int cols      = -1;  // デフォルト幅    -1: 指定なし
    private int istyle    = -1;  // istyle          -1: 指定なし
    private char accessKey = (char)0;     // アクセスキー 0.932
    private boolean disabled  = false;    // 
    private boolean readonly  = false;    // 
    {
        caQueue.enqueue(chValue);
        type = TEXTAREA;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public TextAreaData() { 
    }
    public TextAreaData(TextAreaData from, SessionObject session) {
        this.sessionObject = session;
        copy(from);
    }
    public TextAreaData(String value) {
        setText(value); 
    }
    public TextAreaData(CharArray value) {
        setText(value); 
    }
    
    public TextAreaData(String value, int rows) {
        this.rows = rows;
        setText(value); 
    }
    public TextAreaData(CharArray value, int rows) {
        this.rows = rows;
        setText(value); 
    }
    public TextAreaData(String value, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        setText(value); 
    }
    public TextAreaData(CharArray value, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        setText(value); 
    }
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setValue(String[] values) {
        CharArray chTmp = CharArray.pop(chValue);
        chValue.reset();
        for (int i = 0; i < values.length; i++) {
            chValue.add(values[i]);
        }
        _changed = !chValue.equals(chTmp);
        CharArray.push(chTmp);
    }

    /** テキスト情報をセットする 
        @param value テキスト情報
    */
    public void setText(String value) {
        chValue.set(value);
        if (sessionObject == null || sessionObject.updateMode) chOriginal.set(value);
    }
    /** テキスト情報をセットする 
        @param value テキスト情報
    */
    public void setText(CharArray value) {
        chValue.set(value);
        if (sessionObject == null || sessionObject.updateMode) chOriginal.set(value);
    }

    public void setRows(int rows) { this.rows = rows; }
    public void setCols(int cols) { this.cols = cols; }

    /** istyle の設定 */
    public void setIstyle(int istyle) { this.istyle = istyle; }

    /** アクセスキー('0'～'9')を設定する **/
    public void setAccessKey(char c) { this.accessKey = c; }

    /** 入力文字をクリアする */
    public void clear() {
        chValue.clear();
        if (sessionObject == null || sessionObject.updateMode) chOriginal.clear();
    }

    public void setDisabled(boolean mode) { this.disabled = mode;}
    public void setReadonly(boolean mode) { this.readonly = mode;}

    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; } 
    public CharArray getText() { return chValue; } 
        // ※注：String で取り出すときは getValue.toString() とする
        //       ただし、Stringの多用はシステムに負担を与えるため、
        //       なるべく使用しないこと
    public int getRows()      { return rows; }
    public int getCols()      { return cols; }
    /** istyle を取得する */
    public int getIstyle() { return istyle; }

    /** アクセスキー('0'～'9')を取得します
        @return アクセスキー */
    public char    getAccessKey() { return accessKey; }

    /** 入力不可か？ **/
    public boolean isDisabled() { return disabled;}
    public boolean isReadonly() { return readonly;}

    /** オリジナルから変更されているか？ */
    public boolean updated() {
        return !chValue.equals(chOriginal);
    }

    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    public void copy(TextAreaData from) { // 元オブジェクトより全データをコピー
        super.copy(from);
        chValue.set(from.chValue);
        chOriginal.set(from.chOriginal);
        rows  = from.rows;
        cols  = from.cols;
        istyle    = from.istyle;
        accessKey = from.accessKey;
    }
    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする
        @param session セッションオブジェクト
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        if (visible) {
            session.itemRenderer.drawTextArea(session.getBuffer(),this);
        }
        return session.getBuffer();
    }
    public CharArray getTag(CharArray ch) {
        return sessionObject.itemRenderer.drawTextArea(ch,this);
    }
    public CharArray getTag() {
        return sessionObject.itemRenderer.drawTextArea(new CharArray(),this);
    }
    public CharArray getNameTag(CharArray ch) {
        return sessionObject.itemRenderer.drawTextAreaName(ch,this);
    }
    public CharArray getNameTag() {
        return getNameTag(new CharArray());
    }
    public CharArray getMenuTag(CharArray ch) {
        return sessionObject.itemRenderer.drawTextAreaMenu(ch,this);
    }
    public CharArray getMenuTag() {
        return getMenuTag(new CharArray());
    }
    public CharArray getNameMenuTag(CharArray ch) {
        return sessionObject.itemRenderer.drawTextAreaNameMenu(ch,this);
    }
    public CharArray getNameMenuTag() {
        return getNameMenuTag(new CharArray());
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            chValue.writeObject(out);
            out.writeInt(rows);
            out.writeInt(cols);
            out.writeInt(istyle);
            out.writeBoolean(disabled);
            out.writeBoolean(readonly);
            out.writeChar(accessKey);           // アクセスキー
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            chValue.readObject(in);
            rows = in.readInt();
            cols = in.readInt();
            istyle = in.readInt();
            disabled = in.readBoolean();
            readonly = in.readBoolean();
            accessKey = in.readChar();
        }
    }
}

//
//
// [end of TextAreaData.java]
//

