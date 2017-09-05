//------------------------------------------------------------------------
// @(#)StringItem.java
//                 
//             Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.IntObject;

/** 文字列アイテム */
public class StringItem extends Item {

    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /** StringItem の生成 */
    public StringItem() { 
        super();
        init();
        itemData = new StringData();
        itemData.setItem(this);
    }
    /** StringItem の生成
        @param str  表示文字列
    */
    public StringItem(String str) { 
        super();
        init();
        itemData = new StringData(str);
        itemData.setItem(this);
    }
    /** StringItem の生成
        @param cloneable clonaable
    */
    public StringItem(boolean cloneable) { 
        super();
        init();
        itemData = new StringData();
        itemData.setItem(this);
        setCloneable(cloneable);
    }
    /** StringItem の生成
        @param str  表示文字列
        @param cloneable clonaable
    */
    public StringItem(String str,boolean cloneable) { 
        super();
        init();
        itemData = new StringData(str);
        itemData.setItem(this);
        setCloneable(cloneable);
    }
    /** StringItem の生成
        @param ch  表示文字列
    */
    public StringItem(CharArray ch) { 
        super();
        init();
        itemData = new StringData(ch);
        itemData.setItem(this);
    }
    /** StringItem の生成
        @param ch  表示文字列
        @param cloneable clonaable
    */
    public StringItem(CharArray ch, boolean cloneable) { 
        super();
        init();
        itemData = new StringData(ch);
        itemData.setItem(this);
        setCloneable(cloneable);
    }
    /** copy consutuctor */
    public StringItem(StringItem from) { 
        super();
        setType(STRING);
        setCloneable(from.isCloneable());
        StringData fromdata = (StringData)from.itemData;
        itemData = new StringData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }
    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(STRING);
        setCloneable(false);
    }

    //---------------------------------------------------------------------
    // データ設定メソッド
    //---------------------------------------------------------------------
/**
    public void setValue(CharArray ch) {
        ((StringData)itemData).setValue(ch);
    }
    
    public void setValue(CharArray ch, int sessionID) {
        ((StringData)getItemData(sessionID)).setValue(ch);
    }
**/ 
    /** 文字列の設定 */
    public void setText(CharArray ch) {
        ((StringData)itemData).setText(ch);
    }
    public void setText(CharArray ch, int sessionID) {
        ((StringData)getItemData(sessionID)).setText(ch);
    }
    public void setText(CharArray ch, SessionObject session) {
        ((StringData)getItemData(session)).setText(ch);
    }
    /** 文字列の設定 */
    public void setText(String str) {
        ((StringData)itemData).setText(str);
    }
    public void setText(String str, int sessionID) {
        ((StringData)getItemData(sessionID)).setText(str);
    }
    public void setText(String str, SessionObject session) {
        ((StringData)getItemData(session)).setText(str);
    }
    
    /** 数値設定 */
    public void setInt(int num) {
        ((StringData)itemData).setInt(num);
    }
    public void setInt(int num, int sessionID) {
        ((StringData)getItemData(sessionID)).setInt(num);
    }
    public void setInt(int num, SessionObject session) {
        ((StringData)getItemData(session)).setInt(num);
    }
    
    /** 数値設定 */
    public void setLong(long num) {
        ((StringData)itemData).setLong(num);
    }
    public void setLong(long num, int sessionID) {
        ((StringData)getItemData(sessionID)).setLong(num);
    }
    public void setLong(long num, SessionObject session) {
        ((StringData)getItemData(session)).setLong(num);
    }
    
    
    /** 文字列の追加 */
    public void addText(CharArray ch) {
        ((StringData)itemData).addText(ch);
    }
    public void addText(CharArray ch, int sessionID) {
        ((StringData)getItemData(sessionID)).addText(ch);
    }
    public void addText(CharArray ch, SessionObject session) {
        ((StringData)getItemData(session)).addText(ch);
    }
    /** 文字列の追加 */
    public void addText(String str) {
        ((StringData)itemData).addText(str);
    }
    public void addText(String str, int sessionID) {
        ((StringData)getItemData(sessionID)).addText(str);
    }
    public void addText(String str, SessionObject session) {
        ((StringData)getItemData(session)).addText(str);
    }
    /** フォント色の設定 */
    public void setColor(CharArray ch) {
        ((StringData)itemData).setColor(ch);
    }
    public void setColor(CharArray ch, int sessionID) {
        ((StringData)getItemData(sessionID)).setColor(ch);
    }
    public void setColor(CharArray ch, SessionObject session) {
        ((StringData)getItemData(session)).setColor(ch);
    }
    /** フォント色の設定 */
    public void setColor(String str) {
        ((StringData)itemData).setColor(str);
    }
    public void setColor(String str, int sessionID) {
        ((StringData)getItemData(sessionID)).setColor(str);
    }
    public void setColor(String str, SessionObject session) {
        ((StringData)getItemData(session)).setColor(str);
    }
    /** フォントサイズの設定 */
    public void setSize(CharArray ch) {
        ((StringData)itemData).setSize(ch);
    }
    public void setSize(CharArray ch, int sessionID) {
        ((StringData)getItemData(sessionID)).setSize(ch);
    }
    public void setSize(CharArray ch, SessionObject session) {
        ((StringData)getItemData(session)).setSize(ch);
    }
    /** フォントサイズの設定 */
    public void setSize(String str) {
        ((StringData)itemData).setSize(str);
    }
    public void setSize(String str, int sessionID) {
        ((StringData)getItemData(sessionID)).setSize(str);
    }
    public void setSize(String str, SessionObject session) {
        ((StringData)getItemData(session)).setSize(str);
    }
    /** フォントサイズの設定 */
    public void setSize(int size) {
        ((StringData)itemData).setSize(size);
    }
    public void setSize(int size, int sessionID) {
        ((StringData)getItemData(sessionID)).setSize(size);
    }
    public void setSize(int size, SessionObject session) {
        ((StringData)getItemData(session)).setSize(size);
    }
    /** BOLD設定 */
    public void setBold(boolean mode) {
        ((StringData)itemData).setBold(mode);
    }
    public void setBold(boolean mode, int sessionID) {
        ((StringData)getItemData(sessionID)).setBold(mode);
    }
    public void setBold(boolean mode, SessionObject session) {
        ((StringData)getItemData(session)).setBold(mode);
    }
    /** Italic設定 */
    public void setItalic(boolean mode) {
        ((StringData)itemData).setItalic(mode);
    }
    public void setItalic(boolean mode, int sessionID) {
        ((StringData)getItemData(sessionID)).setItalic(mode);
    }
    public void setItalic(boolean mode, SessionObject session) {
        ((StringData)getItemData(session)).setItalic(mode);
    }
    /** WordWrap設定 */
    public void setWrap(boolean mode) {
        ((StringData)itemData).setWrap(mode);
    }
    public void setWrap(boolean mode, int sessionID) {
        ((StringData)getItemData(sessionID)).setWrap(mode);
    }
    public void setWrap(boolean mode, SessionObject session) {
        ((StringData)getItemData(session)).setWrap(mode);
    }
    /** wordwrap offset 設定 */
    public void setWrapOffset(int size) {
        ((StringData)itemData).setWrapOffset(size);
    }
    public void setWrapOffset(int size, int sessionID) {
        ((StringData)getItemData(sessionID)).setWrapOffset(size);
    }
    public void setWrapOffset(int size, SessionObject session) {
        ((StringData)getItemData(session)).setWrapOffset(size);
    }
    
    /** PC用の wordwrap 幅設定 */
    public void setWrapWidth(int size) {
        ((StringData)itemData).setWrapWidth(size);
    }
    public void setWrapWidth(int size, int sessionID) {
        ((StringData)getItemData(sessionID)).setWrapWidth(size);
    }
    public void setWrapWidth(int size, SessionObject session) {
        ((StringData)getItemData(session)).setWrapWidth(size);
    }
    
    /** Tag変換設定 */
    public void setTagConvert(boolean mode) {
        ((StringData)itemData).setTagConvert(mode);
    }
    public void setTagConvert(boolean mode, int sessionID) {
        ((StringData)getItemData(sessionID)).setTagConvert(mode);
    }
    public void setTagConvert(boolean mode, SessionObject session) {
        ((StringData)getItemData(session)).setTagConvert(mode);
    }
    
    /** Marquee表示設定 */
    public void setMarquee(boolean mode) {
        ((StringData)itemData).setMarquee(mode);
    }
    public void setMarquee(boolean mode, int sessionID) {
        ((StringData)getItemData(sessionID)).setMarquee(mode);
    }
    public void setMarquee(boolean mode, SessionObject session) {
        ((StringData)getItemData(session)).setMarquee(mode);
    }
    
    /** データのクリア*/
    public  void clear() {
        ((StringData)itemData).clear();
    }
    public  void clear(int sessionID) {
        ((StringData)getItemData(sessionID)).clear();
    }
    public  void clear(SessionObject session) {
        ((StringData)getItemData(session)).clear();
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
    
    //-------------------------
    // スタイルシート関連
    //-------------------------
    ///** style= */
    /*
    public void setStyle(CharArray ch) {
        ((StringData)itemData).setStyle(ch);
    }
    public void setStyle(CharArray ch, int sessionID) {
        ((StringData)getItemData(sessionID)).setStyle(ch);
    }
    public void setStyle(CharArray ch, SessionObject session) {
        ((StringData)getItemData(session)).setStyle(ch);
    }
    public void setStyle(String str) {
        ((StringData)itemData).setStyle(str);
    }
    public void setStyle(String str, int sessionID) {
        ((StringData)getItemData(sessionID)).setStyle(str);
    }
    public void setStyle(String str, SessionObject session) {
        ((StringData)getItemData(session)).setStyle(str);
    }
    */
    ///** class= */
    /*
    public void setClass(CharArray ch) {
        ((StringData)itemData).setClass(ch);
    }
    public void setClass(CharArray ch, int sessionID) {
        ((StringData)getItemData(sessionID)).setClass(ch);
    }
    public void setClass(CharArray ch, SessionObject session) {
        ((StringData)getItemData(session)).setClass(ch);
    }
    public void setClass(String str) {
        ((StringData)itemData).setClass(str);
    }
    public void setClass(String str, int sessionID) {
        ((StringData)getItemData(sessionID)).setClass(str);
    }
    public void setClass(String str, SessionObject session) {
        ((StringData)getItemData(session)).setClass(str);
    }
    */
    
    ///** id= */
    /*
    public void setID(CharArray ch) {
        ((StringData)itemData).setID(ch);
    }
    public void setID(CharArray ch, int sessionID) {
        ((StringData)getItemData(sessionID)).setID(ch);
    }
    public void setID(CharArray ch, SessionObject session) {
        ((StringData)getItemData(session)).setID(ch);
    }
    public void setID(String str) {
        ((StringData)itemData).setID(str);
    }
    public void setID(String str, int sessionID) {
        ((StringData)getItemData(sessionID)).setID(str);
    }
    public void setID(String str, SessionObject session) {
        ((StringData)getItemData(session)).setID(str);
    }
    */
    /** align 設定 */
    public void setAlign(int align) {
        ((StringData)itemData).setAlign(align);
    }
    public void setAlign(int align, int sessionID) {
        ((StringData)getItemData(sessionID)).setAlign(align);
    }
    public void setAlign(int align, SessionObject session) {
        ((StringData)getItemData(session)).setAlign(align);
    }
    
    //---------------------------------------------------------------------
    // データ取得メソッド
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() {
        return ((StringData)itemData).getValue();
    }
    public CharArrayQueue getValue(int sessionID) {
        return ((StringData)getItemData(sessionID)).getValue();
    }
    public CharArrayQueue getValue(SessionObject session) {
        return ((StringData)getItemData(session)).getValue();
    }
    /** テキスト情報の取得 */
    public CharArray getText() {
        return ((StringData)itemData).getText();
    }
    public CharArray getText(int sessionID) {
        StringData sd = (StringData)getItemData(sessionID);
        return (sd != null) ? sd.getText() : null;
    }
    public CharArray getText(SessionObject session) {
        StringData sd = (StringData)getItemData(session);
        return (sd != null) ? sd.getText() : null;
    }

    /** 数値情報の取得 */
    public int getInt() {
        return ((StringData)itemData).getInt();
    }
    public int getInt(int sessionID) {
        StringData sd = (StringData)getItemData(sessionID);
        return (sd != null) ? sd.getInt() : 0;
    }
    public int getInt(SessionObject session) {
        StringData sd = (StringData)getItemData(session);
        return (sd != null) ? sd.getInt() : 0;
    }
    /** 数値情報の取得 */
    public long getLong() {
        return ((StringData)itemData).getLong();
    }
    public long getLong(int sessionID) {
        StringData sd = (StringData)getItemData(sessionID);
        return (sd != null) ? sd.getLong() : 0;
    }
    public long getLong(SessionObject session) {
        StringData sd = (StringData)getItemData(session);
        return (sd != null) ? sd.getLong() : 0;
    }

    /** 色情報の取得 */
    public CharArray getColor() {
        return ((StringData)itemData).getColor();
    }
    public CharArray getColor(int sessionID) {
        return ((StringData)getItemData(sessionID)).getColor();
    }
    public CharArray getColor(SessionObject session) {
        return ((StringData)getItemData(session)).getColor();
    }
    /** タグ変換情報を取得する */
    public boolean getTagConvert() {
        return ((StringData)itemData).getTagConvert();
    }
    public boolean getTagConvert(int sessionID) {
        return ((StringData)getItemData(sessionID)).getTagConvert();
    }
    public boolean getTagConvert(SessionObject session) {
        return ((StringData)getItemData(session)).getTagConvert();
    }
    public boolean isTagConvert() {
        return ((StringData)itemData).isTagConvert();
    }
    public boolean isTagConvert(int sessionID) {
        return ((StringData)getItemData(sessionID)).isTagConvert();
    }
    public boolean isTagConvert(SessionObject session) {
        return ((StringData)getItemData(session)).isTagConvert();
    }
    /** Marquee表示モードか？*/
    public boolean isMarquee() {
        return ((StringData)itemData).isMarquee();
    }
    public boolean isMarquee(int sessionID) {
        return ((StringData)getItemData(sessionID)).isMarquee();
    }
    public boolean isMarquee(SessionObject session) {
        return ((StringData)getItemData(session)).isMarquee();
    }
    
    /** 出力情報の取得 */
    public CharArray getTag() {
        return ((StringData)itemData).getTag();
    }
    public CharArray getTag(int sessionID) {
        return ((StringData)getItemData(sessionID)).getTag();
    }
    public CharArray getTag(SessionObject session) {
        return ((StringData)getItemData(session)).getTag();
    }
 
    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new StringData((StringData)itemData, session);
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
        StringData data = (StringData)getItemData(session); 
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
// [end of StringItem.java]
//

