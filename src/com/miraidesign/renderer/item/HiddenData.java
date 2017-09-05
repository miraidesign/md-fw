//------------------------------------------------------------------------
// @(#)HiddenData.java
//                 [input type= hidden] のデータを保管する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/** [input type= hidden] のデータを保管する */
public class HiddenData extends ItemData {
    private CharArray   chValue = new CharArray();  // 入力文字列データ
    private CharArray   chOriginal = new CharArray();  // 保存用 v0.684
    
    private int size      = -1;  // デフォルト幅    -1: 指定なし
    private int maxLength = -1;  // 最大入力文字数  -1: 指定なし
    {
        caQueue.enqueue(chValue);
        type = HIDDEN;
    }
     
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public HiddenData() { }
    public HiddenData(HiddenData from, SessionObject session) { 
        this.sessionObject = session;
        copy(from);
    }
    public HiddenData(String value) { 
        setText(value); 
    }
    public HiddenData(CharArray value) {
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
    
    public void setSize(int size) { this.size = size; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }

    public void clear() { 
        chValue.clear();
        if (sessionObject == null || sessionObject.updateMode) chOriginal.clear();
    }
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; } 
    public CharArray getText() { return chValue; } 
        // ※注：String で取り出すときは getValue.toString() とする
        //       ただし、Stringの多用はシステムに負担を与えるため、
        //       なるべく使用しないこと
    public int getSize()      { return size; }
    public int getMaxLength() { return maxLength; }

    /** オリジナルから変更されているか？ */
    public boolean updated() {
        return !chValue.equals(chOriginal);
    }

    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    public void copy(HiddenData from) { // 元オブジェクトより全データをコピー
        super.copy(from);
        chValue.set(from.chValue);
        chOriginal.set(from.chOriginal);
        size      = from.size;
        maxLength = from.maxLength;
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
            session.itemRenderer.drawHidden(session.getBuffer(),this);
        }
        return session.getBuffer();
    }
    public CharArray getTag(CharArray ch) {
        return sessionObject.itemRenderer.drawHidden(ch,this);
    }
    public CharArray getNameTag(CharArray ch) {
        return sessionObject.itemRenderer.drawHiddenName(ch,this);
    }
    public CharArray getNameTag() {
        return getNameTag(new CharArray());
    }
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            chValue.writeObject(out);
            out.writeInt(size);
            out.writeInt(maxLength);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            chValue.readObject(in);
            size = in.readInt();
            maxLength = in.readInt();
        }
    }

}

//
// [end of HiddenData.java]
//

