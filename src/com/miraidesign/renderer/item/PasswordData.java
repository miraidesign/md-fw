//------------------------------------------------------------------------
// @(#)PasswordData.java
//                 [input type= password] のデータを保管する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/**
 *  [input type= password] のデータを保管する
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class PasswordData extends ItemData {
    private CharArray   chValue = new CharArray();  // 入力文字列データ
    private CharArray   chOriginal = new CharArray();  // 保存用 v0.684
    
    private int size      = -1;  // デフォルト幅    -1: 指定なし
    private int maxLength = -1;  // 最大入力文字数  -1: 指定なし
    private int istyle    = -1;  // istyle          -1: 指定なし
    private char accessKey = (char)0;     // アクセスキー 0.932
    private boolean disabled  = false;    // 
    private boolean readonly  = false;    // 
    {
        caQueue.enqueue(chValue);
        type = PASSWORD;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /** PasswordData オブジェクトを生成する
     */
    public PasswordData() {
    }
    /** PasswordData オブジェクトを生成する（コピーコンストラクタ）
        @param from コピー元オブジェクト
        @param session セッッションオブジェクト
    */
    public PasswordData(PasswordData from, SessionObject session) { 
        this.sessionObject = session;
        copy(from);
    }
    
    /** PasswordData オブジェクトを生成する 
        @param value デフォルトパスワード値
    */
    public PasswordData(String value) {
        setValue(value); 
    }

    /** PasswordData オブジェクトを生成する
        @param value デフォルトパスワード値
     */
    public PasswordData(CharArray value) {
        setValue(value); 
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
    public void setValue(String value) {
        chValue.set(value);
        chOriginal.set(value);
    }
    public void setValue(CharArray value) {
        chValue.set(value);
        chOriginal.set(value);
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
    
    /** 表示幅を設定する 
        @param size 表示サイズ
    */
    public void setSize(int size) { this.size = size; }
    /** 入力最大文字数を指定する 
        @param maxLength サイズ
    */
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }

    /** istyle の設定 
       @param istyle Item.HIRAGANA/KATAKANA/ALPHABET/NUMERIC から選択
    */
    public void setIstyle(int istyle) { this.istyle = istyle; }

    /** アクセスキー('0'～'9')を設定する **/
    public void setAccessKey(char c) { this.accessKey = c; }

    
    /** 入力文字をクリアする */
    public void clear() {
        chValue.clear();
        if (sessionObject == null || sessionObject.updateMode) chOriginal.clear();
    }

    /** 更新、Focus,POSTを禁止する */
    public void setDisabled(boolean mode) { this.disabled = mode;}
    /** 更新を禁止する */
    public void setReadonly(boolean mode) { this.readonly = mode;}
    
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; }

    /** 入力文字を取得する */
    public CharArray getText() { return chValue; }
        // ※注：String で取り出すときは getValue.toString() とする
        //       ただし、Stringの多用はシステムに負担を与えるため、
        //       なるべく使用しないこと

    /** 表示幅を指定する */
    public int getSize()      { return size; }

    /** 入力最大文字数を指定する */
    public int getMaxLength() { return maxLength; }
    
    /** istyle を取得する */
    public int getIstyle() { return istyle; }

    /** アクセスキー('0'～'9')を取得します
        @return アクセスキー */
    public char    getAccessKey() { return accessKey; }

    /** 入力、POST 不可か？ **/
    public boolean isDisabled() { return disabled;}
    /** 入力不可か？ **/
    public boolean isReadonly() { return readonly;}
    
    /** オリジナルから変更されているか？ */
    public boolean updated() {
        return !chValue.equals(chOriginal);
    }

    //---------------------------------------------------------------------
    // copy / clone
    //---------------------------------------------------------------------
    public void copy(PasswordData from) { // 元オブジェクトより全データをコピー
        super.copy(from);
        chValue.set(from.chValue);
        chOriginal.set(from.chOriginal);
        size      = from.size;
        maxLength = from.maxLength;
        istyle    = from.istyle;
        accessKey = from.accessKey;
        disabled  = from.disabled;
        readonly  = from.readonly;
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
            session.itemRenderer.drawPassword(session.getBuffer(),this);
        }
        return session.getBuffer();
    }
    
    /** レンダラー用のタグを取得する （全て）*/
    public CharArray getTag(CharArray ch) {
        return sessionObject.itemRenderer.drawPassword(ch,this);
    }
    /** レンダラー用のタグを取得する (name以降) */
    public CharArray getNameTag(CharArray ch) {
        return sessionObject.itemRenderer.drawPasswordName(ch,this);
    }
    /** レンダラー用のタグを取得する (name以降) */
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
            out.writeInt(istyle);
            out.writeChar(accessKey);           // アクセスキー
            
            out.writeBoolean(disabled);
            out.writeBoolean(readonly);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            chValue.readObject(in);
            size = in.readInt();
            maxLength = in.readInt();
            istyle = in.readInt();
            accessKey = in.readChar();
            disabled = in.readBoolean();
            readonly = in.readBoolean();
        }
    }

}

//
// [end of PasswordData.java]
//

