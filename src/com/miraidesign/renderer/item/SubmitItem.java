//------------------------------------------------------------------------
// @(#)SubmitItem.java
//                 input type = submit
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.QueueElement;

/**
 *  SUBMITボタン(input type=submit)
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class SubmitItem extends Item {

    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /**
        SubmitItemオブジェクトを生成する
    */
    public SubmitItem() {
        super();
        init();
        itemData = new SubmitData();
        itemData.setItem(this);
    }
    /**
        SubmitItemオブジェクトを生成する
        @param value デフォルト値
    */
    public SubmitItem(String value) {
        super();
        init();
        itemData = new SubmitData(value);
        itemData.setItem(this);
    }
    /**
        SubmitItemオブジェクトを生成する
        @param value デフォルト値
    */
    public SubmitItem(CharArray value) {
        super();
        init();
        itemData = new SubmitData(value);
        itemData.setItem(this);
    }
    /**
        SubmitItemオブジェクトを生成する (copy consuructor)
    */
    public SubmitItem(SubmitItem from) {
        super();
        setType(SUBMIT);
        setCloneable(from.isCloneable());
        SubmitData fromdata = (SubmitData)from.itemData;
        itemData = new SubmitData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }

    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(SUBMIT);
        setCloneable(true);
    }


    //---------------------------------------------------------------------
    // データ設定メソッド
    //---------------------------------------------------------------------
    /** 表示文字列の設定 **/
    public void setText(CharArray ch) {
        ((SubmitData)itemData).setText(ch);
    }
    public void setText(CharArray ch, int sessionID) {
        ((SubmitData)getItemData(sessionID)).setText(ch);
    }
    public void setText(CharArray ch, SessionObject session) {
        ((SubmitData)getItemData(session)).setText(ch);
    }
    
    public void setText(String str) {
        ((SubmitData)itemData).setText(str);
    }
    public void setText(String str, int sessionID) {
        ((SubmitData)getItemData(sessionID)).setText(str);
    }
    public void setText(String str, SessionObject session) {
        ((SubmitData)getItemData(session)).setText(str);
    }
    
    /** align 設定 */
    public void setAlign(int align) {
        ((SubmitData)itemData).setAlign(align);
    }
    public void setAlign(int align, int sessionID) {
        ((SubmitData)getItemData(sessionID)).setAlign(align);
    }
    public void setAlign(int align, SessionObject session) {
        ((SubmitData)getItemData(session)).setAlign(align);
    }
    
    /** アクセスキー設定 */
    public void setAccessKey(char key) {
        ((SubmitData)itemData).setAccessKey(key);
    }
    public void setAccessKey(char key, int sessionID) {
        ((SubmitData)getItemData(sessionID)).setAccessKey(key);
    }
    public void setAccessKey(char key, SessionObject session) {
        ((SubmitData)getItemData(session)).setAccessKey(key);
    }
    
    /** disabled 設定*/
    public void setDisabled(boolean mode) {
        ((SubmitData)itemData).setDisabled(mode);
    }
    public void setDisabled(boolean mode, int sessionID) {
        ((SubmitData)getItemData(sessionID)).setDisabled(mode);
    }
    public void setDisabled(boolean mode, SessionObject session) {
        ((SubmitData)getItemData(session)).setDisabled(mode);
    }
    
    //---------------------------------------------------------------------
    // データ取得メソッド
    //---------------------------------------------------------------------
    /** デフォルト入力文字列を取得する
        @return 入力文字列 */
    public CharArrayQueue getValue() {
        return ((SubmitData)itemData).getValue();
    }
    /** ユーザー毎の入力文字列を取得する
        @param sessionID セッションＩＤ
        @return 入力文字列 */
    public CharArrayQueue getValue(int sessionID) {
        return ((SubmitData)getItemData(sessionID)).getValue();
    }
    public CharArrayQueue getValue(SessionObject session) {
        return ((SubmitData)getItemData(session)).getValue();
    }

    /** デフォルト入力文字列を取得する
        @return 入力文字列 */
    public CharArray getText() {
        return ((SubmitData)itemData).getText();
    }
    
    /** ユーザー毎の入力文字列を取得する
        @param sessionID セッションＩＤ
        @return 入力文字列 */
    public CharArray getText(int sessionID) {
        return ((SubmitData)getItemData(sessionID)).getText();
    }
    public CharArray getText(SessionObject session) {
        return ((SubmitData)getItemData(session)).getText();
    }

    /** クリックされたか？ */
    public boolean isClicked() {
        return ((SubmitData)itemData).isClicked();
    }
    public boolean isClicked(int sessionID) {
        return ((SubmitData)getItemData(sessionID)).isClicked();
    }
    public boolean isClicked(SessionObject session) {
        return ((SubmitData)getItemData(session)).isClicked();
    }

    /** QueueElementを取得する */
    public QueueElement getElement() {
        return ((SubmitData)itemData).getElement();
    }
    public QueueElement getElement(int sessionID) {
        return ((SubmitData)getItemData(sessionID)).getElement();
    }
    public QueueElement getElement(SessionObject session) {
        return ((SubmitData)getItemData(session)).getElement();
    }

    /** align 取得 */
    public int getAlign() {
        return ((SubmitData)itemData).getAlign();
    }
    public int getAlign(int sessionID) {
        return ((SubmitData)getItemData(sessionID)).getAlign();
    }
    public int getAlign(SessionObject session) {
        return ((SubmitData)getItemData(session)).getAlign();
    }
    
    /** アクセスキー取得 */
    public char getAccessKey() {
        return ((SubmitData)itemData).getAccessKey();
    }
    public char getAccessKey(int sessionID) {
        return ((SubmitData)getItemData(sessionID)).getAccessKey();
    }
    public char getAccessKey(SessionObject session) {
        return ((SubmitData)getItemData(session)).getAccessKey();
    }

    /** disabled か？ */
    public boolean isDisabled() {
        return ((SubmitData)itemData).isDisabled();
    }
    public boolean isDisabled(int sessionID) {
        return ((SubmitData)getItemData(sessionID)).isDisabled();
    }
    public boolean isDisabled(SessionObject session) {
        return ((SubmitData)getItemData(session)).isDisabled();
    }
    //---------------------------------------------------------------------
    // レンダリング
    //---------------------------------------------------------------------
    public CharArray getTag(int sessionID) {
        return ((SubmitData)getItemData(sessionID)).getTag();
    }
    public CharArray getNameTag(int sessionID) {
        return ((SubmitData)getItemData(sessionID)).getNameTag();
    }
    public CharArray getTag(SessionObject session) {
        return ((SubmitData)getItemData(session)).getTag();
    }
    public CharArray getNameTag(SessionObject session) {
        return ((SubmitData)getItemData(session)).getNameTag();
    }

    //---------------------------------------------------------------------
    // copy
    //---------------------------------------------------------------------
    /** デフォルトのオブジェクトをコピーしてユーザーオブジェクトを作成する
        @param session セッションオブジェクト
    */
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new SubmitData((SubmitData)itemData,session);
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
        SubmitData data = (SubmitData)getItemData(session);
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
// [end of SubmitItem.java]
//

