//------------------------------------------------------------------------
// @(#)SubmitData.java
//                 [input type= submit] のデータを保管する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.QueueElement;

/** サブミットボタン情報を保管する */
public class SubmitData extends ItemData {
    private CharArray   chValue = new CharArray();  // 入力文字列データ
    private CharArray   chOriginal = new CharArray();  // 保存用 v0.684
    
    private int size      = -1;  // デフォルト幅    -1: 指定なし
    private int maxLength = -1;  // 最大入力文字数  -1: 指定なし
    private int align     = 0;   // ver 0.633 追加
    private char accessKey = (char)0;     // アクセスキー 0.932
    private boolean disabled  = false;    // 
    {
        caQueue.enqueue(chValue);
        type = SUBMIT;
    }
    
    private boolean clicked = false;
    public void setClicked(boolean clicked) { this.clicked = clicked;}

    /** クリックされたか？ */
    public boolean isClicked() { return clicked; }
    
    private QueueElement element = new QueueElement();
    /** QueueElementを取得する */
    public QueueElement getElement() {
        return element;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public SubmitData() {
    }
    public SubmitData(SubmitData from, SessionObject session) {
        this.sessionObject = session;
        copy(from);
    }
    public SubmitData(String value) {
        setText(value);
    }
    public SubmitData(CharArray value) {
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

    /** align情報設定 */
    public void setAlign(int i) { 
        if (i < ItemConstant.align.length) align = i;
    }

    /** アクセスキー('0'～'9')を設定する **/
    public void setAccessKey(char c) { this.accessKey = c; }

    /** 入力文字をクリアする */
    public void clear() {
        chValue.clear();
        if (sessionObject == null || sessionObject.updateMode) chOriginal.clear();
    }

    public void setDisabled(boolean mode) { this.disabled = mode;}

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

    public int getAlign() { return align;}

    /** アクセスキー('0'～'9')を取得します
        @return アクセスキー */
    public char    getAccessKey() { return accessKey; }

    public boolean isDisabled() { return disabled;}

    /** オリジナルから変更されているか？ */
    public boolean updated() {
        return !chValue.equals(chOriginal);
    }
    //---------------------------------------------------------------------
    // copy / clone
    //---------------------------------------------------------------------
    public void copy(SubmitData from) { // 元オブジェクトより全データをコピー
        super.copy(from);
        chValue.set(from.chValue);
        chOriginal.set(from.chOriginal);
        size      = from.size;
        maxLength = from.maxLength;
        clicked   = from.clicked;
        element.copy2(from.element);
        align = from.align;
        accessKey = from.accessKey;
        disabled       = from.disabled;
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
            session.itemRenderer.drawSubmit(session.getBuffer(),this);
        }
        return session.getBuffer();
    }
    public CharArray getTag(CharArray ch) {
        return sessionObject.itemRenderer.drawSubmit(ch,this);
    }
    public CharArray getNameTag(CharArray ch) {
        return sessionObject.itemRenderer.drawSubmitName(ch,this);
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
            out.writeBoolean(clicked);
            element.writeObject(out);
            out.writeInt(align);
            out.writeBoolean(disabled);
            out.writeChar(accessKey);           // アクセスキー
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            chValue.readObject(in);
            size = in.readInt();
            maxLength = in.readInt();
            clicked = in.readBoolean();
            element.readObject(in);
            align = in.readInt();
            disabled = in.readBoolean();
            accessKey = in.readChar();
            
        }
    }

}

//
//
// [end of SubmitData.java]
//

