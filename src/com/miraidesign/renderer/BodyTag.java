//------------------------------------------------------------------------
// @(#)BodyTag.java
//              bodyタグ情報を保管する
//              Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.util.CharArray;

/** bodyタグ情報を保管する */
public class BodyTag {
    private CharArray background = new CharArray();
    private CharArray bgcolor = new CharArray();
    private CharArray color = new CharArray();
    private CharArray text = new CharArray();
    private CharArray link = new CharArray();
    private CharArray vlink = new CharArray();
    private CharArray alink = new CharArray();

    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public BodyTag() {
    }
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    /** background設定 */
    public void setBackground(String str)    { background.set(str); }
    public void setBackground(CharArray str) { background.set(str); }
    /** bgcolor設定 */
    public void setBgcolor(String str)    { bgcolor.set(str); }
    public void setBgcolor(CharArray str) { bgcolor.set(str); }
    /** color設定 */
    public void setColor(String str)    { color.set(str); }
    public void setColor(CharArray str) { color.set(str); }
    /** text設定 */
    public void setText(String str)    { text.set(str); }
    public void setText(CharArray str) { text.set(str); }
    /** link設定 */
    public void setLink(String str)    { link.set(str); }
    public void setLink(CharArray str) { link.set(str); }
    /** vlink設定 */
    public void setVlink(String str)    { vlink.set(str); }
    public void setVlink(CharArray str) { vlink.set(str); }
    /** alink設定 */
    public void setAlink(String str)    { alink.set(str); }
    public void setAlink(CharArray str) { alink.set(str); }
    
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArray getBackground() { return background; }
    public CharArray getBgcolor() { return bgcolor; }
    public CharArray getColor() { return color; }
    public CharArray getText() { return text;}
    public CharArray getLinnk() { return link; }
    public CharArray getVlink() { return vlink; }
    public CharArray getAlink() { return alink; }
    
    
    
    /** タグ情報の取得 */
    public CharArray getTag() {
        CharArray ch = new CharArray();
        return getTag(ch);
    
    }
    /** タグ情報の取得 
        @param ch 使用バッファ
    */
    public CharArray getTag(CharArray ch) {
        if (background.length() > 0) {
            ch.add(" background="); ch.add("\"");ch.add(background); ch.add("\"");
        }
        if (bgcolor.length() > 0) {
            if (background.length() == 0 && 
                    (bgcolor.endsWith(".gif") || bgcolor.endsWith(".jpg"))) {
                ch.add(" background="); ch.add("\"");ch.add(bgcolor); ch.add("\"");
            } else {
                ch.add(" bgcolor="); ch.add("\"");ch.add(bgcolor); ch.add("\"");
            }
        }
        if (text.length() > 0) {
            ch.add(" text="); ch.add("\"");ch.add(text); ch.add("\"");
        }
        if (link.length() > 0) {
            ch.add(" link="); ch.add("\"");ch.add(link); ch.add("\"");
        }
        if (vlink.length() > 0) {
            ch.add(" vlink="); ch.add("\"");ch.add(vlink); ch.add("\"");
        }
        if (alink.length() > 0) {
            ch.add(" alink="); ch.add("\"");ch.add(alink); ch.add("\"");
        }
        return ch;
    }
    
    /** タグ情報の取得 */
    public CharArray getStyleTag() {
        CharArray ch = new CharArray();
        return getStyleTag(ch);
    
    }
    /** タグ情報の取得 
        @param ch 使用バッファ
    */
    public CharArray getStyleTag(CharArray ch) {
        ch.add(" style=\"");
        if (background.length() > 0) {
            ch.add(" background-image: url("); ch.add(background); ch.add(");");
        }
        
        if (bgcolor.length() > 0) {
            if (background.length() == 0 && 
                    (bgcolor.endsWith(".gif") || bgcolor.endsWith(".jpg"))) {
                ch.add(" background-image: url("); ch.add(bgcolor); ch.add("); ");
            } else {
                ch.add("background-color:"); ch.add(bgcolor); ch.add("; ");
            }
        }
        if (color.length() > 0) {
            ch.add("color:"); ch.add(color); ch.add("; ");
        }
        ch.add("\"");
        return ch;
    }
    
    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    public void copy(BodyTag from) { // 元オブジェクトより全データをコピー
        background.set(from.background);
        bgcolor.set(from.bgcolor);
        color.set(from.color);
        text.set(from.text);
        link.set(from.link);
        vlink.set(from.vlink);
        alink.set(from.alink);
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            background.writeObject(out);
            bgcolor.writeObject(out);
            color.writeObject(out);
            text.writeObject(out);
            link.writeObject(out);
            vlink.writeObject(out);
            alink.writeObject(out);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            background.readObject(in);
            bgcolor.readObject(in);
            color.readObject(in);
            text.readObject(in);
            link.readObject(in);
            vlink.readObject(in);
            alink.readObject(in);
        }
    }
}

//
//
// [end of BodyTag.java]
//

