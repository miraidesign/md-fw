//------------------------------------------------------------------------
// @(#)HiddenItem.java
//                 input type = hidden
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.IntObject;

/** hidden アイテム */
public class HiddenItem extends Item {


    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /**
        HiddenItemオブジェクトを生成する
    */
    public HiddenItem() { 
        super();
        init();
        itemData = new HiddenData();
        itemData.setItem(this);
    }
    /**
        HideenItemオブジェクトを生成する
        @param value デフォルト値
    */
    public HiddenItem(String value) { 
        super();
        init();
        itemData = new HiddenData(value);
        itemData.setItem(this);
    }
    /**
        HideenItemオブジェクトを生成する
        @param value デフォルト値
    */
    public HiddenItem(CharArray value) { 
        super();
        init();
        itemData = new HiddenData(value);
        itemData.setItem(this);
    }

    /**
        HiddenItemオブジェクトを生成する(Copy constructor)
    */
    public HiddenItem(HiddenItem from) { 
        super();
        setType(HIDDEN);
        setCloneable(from.isCloneable());
        HiddenData fromdata = (HiddenData)from.itemData;
        itemData = new HiddenData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }


    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(HIDDEN);
        setCloneable(true);
    }
    
    //---------------------------------------------------------------------
    // データ設定メソッド
    //---------------------------------------------------------------------
/**
    public  void setValue(CharArray ch) {
        ((HiddenData)itemData).setText(ch);
    }
    public  void setValue(CharArray ch, int sessionID) {
        ((HiddenData)getItemData(sessionID)).setText(ch);
    }
**/
    public  void setText(String str) {
        ((HiddenData)itemData).setText(str);
    }
    public  void setText(String str, int sessionID) {
        ((HiddenData)getItemData(sessionID)).setText(str);
    }
    public  void setText(String str, SessionObject session) {
        ((HiddenData)getItemData(session)).setText(str);
    }
    public  void setText(CharArray ch) {
        ((HiddenData)itemData).setText(ch);
    }
    public  void setText(CharArray ch, int sessionID) {
        ((HiddenData)getItemData(sessionID)).setText(ch);
    }
    public  void setText(CharArray ch, SessionObject session) {
        ((HiddenData)getItemData(session)).setText(ch);
    }
    /** データのクリア*/
    public  void clear() {
        ((HiddenData)itemData).clear();
    }
    public  void clear(int sessionID) {
        ((HiddenData)getItemData(sessionID)).clear();
    }
    public  void clear(SessionObject session) {
        ((HiddenData)getItemData(session)).clear();
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
    /** デフォルトの隠し文字列を取得する
        @return 入力文字列 */
    public  CharArrayQueue getValue() {
        return ((HiddenData)itemData).getValue();
    }
    
    /** ユーザー毎の隠し文字列を取得する
        @param sessionID セッションＩＤ
        @return 入力文字列 */
    public  CharArrayQueue getValue(int sessionID) {
        return ((HiddenData)getItemData(sessionID)).getValue();
    }
    public  CharArrayQueue getValue(SessionObject session) {
        return ((HiddenData)getItemData(session)).getValue();
    }

    /** デフォルトの隠し文字列を取得する
        @return 入力文字列 */
    public  CharArray getText() {
        return ((HiddenData)itemData).getText();
    }
    /** ユーザー毎の隠し文字列を取得する
        @param sessionID セッションＩＤ
        @return 入力文字列 */
    public  CharArray getText(int sessionID) {
        return ((HiddenData)getItemData(sessionID)).getText();
    }
    public  CharArray getText(SessionObject session) {
        return ((HiddenData)getItemData(session)).getText();
    }
    //---------------------------------------------------------------------
    // レンダリング
    //---------------------------------------------------------------------
    public CharArray getTag(int sessionID) {
        return ((HiddenData)getItemData(sessionID)).getTag();
    }
    public CharArray getTag(SessionObject session) {
        return ((HiddenData)getItemData(session)).getTag();
    }
    public CharArray getNameTag(int sessionID) {
        return ((HiddenData)getItemData(sessionID)).getNameTag();
    }
    public CharArray getNameTag(SessionObject session) {
        return ((HiddenData)getItemData(session)).getNameTag();
    }

    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new HiddenData((HiddenData)itemData ,session);
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
        HiddenData data = (HiddenData)getItemData(session);
        return data.draw(session);
    }

    // stream 版
    public void draw(OutputStream out) {
        //未作成
    }
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
// [end of HiddenItem.java]
//

