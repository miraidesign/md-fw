//------------------------------------------------------------------------
// @(#)StyleTag.java
//              スタイルシートへの参照を保管する
//              Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.util.CharArray;

/** スタイルシートへの参照を保管する */
public class StyleTag {
    private CharArray chStyle = new CharArray();
    private CharArray chClass = new CharArray();
    private CharArray chID    = new CharArray();

    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public StyleTag() {
    }
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    /** スタイルの設定 STYLE= */
    public void setStyle(String str) {
        chStyle.set(str);
    }
    public void setStyle(CharArray str) {
        chStyle.set(str);
    }
    
    /** クラスの設定 CLASS= */
    public void setClass(String str) {
        chClass.set(str);
    }
    public void setClass(CharArray str) {
        chClass.set(str);
    }
    
    /** ＩＤの設定 ID= */
    public void setID(String str) {
        chID.set(str);
    }
    public void setID(CharArray str) {
        chID.set(str);
    }
    
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    /** タグ情報の取得 */
    public CharArray getTag() {
        CharArray ch = new CharArray();
        return getTag(ch);
    
    }
    /** タグ情報の取得 
        @param ch 使用バッファ
    */
    public CharArray getTag(CharArray ch) {
        if (chStyle.length() > 0) {
            ch.add(" style="); ch.add("\"");ch.add(chStyle); ch.add("\"");
        }
        if (chClass.length() > 0) {
            ch.add(" class="); ch.add("\"");ch.add(chClass); ch.add("\"");
        }
        if (chID.length() > 0) {
            ch.add(" id="); ch.add("\"");ch.add(chID); ch.add("\"");
        }
        return ch;
    }
    
    /** レンダリングする情報が存在するか */
    public boolean existTag() {
        if (chStyle.length() > 0 || chClass.length() > 0 || chID.length() > 0) {
            return true;
        }
        return false;
    }
    
    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    public void copy(StyleTag from) { // 元オブジェクトより全データをコピー
        chStyle.set(from.chStyle);
        chClass.set(from.chClass);
        chID.set(from.chID);
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            chStyle.writeObject(out);
            chClass.writeObject(out);
            chID.writeObject(out);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            chStyle.readObject(in);
            chClass.readObject(in);
            chID.readObject(in);
        }
    }
}

//
// [end of StyleTag.java]
//

