//------------------------------------------------------------------------
// @(#)StringData.java
//                 
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.renderer.StyleTag;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/** 文字列データを保管する */
public class StringData extends ItemData {
    private CharArray string = new CharArray();     // 文字列データ
    //
    private StyleTag  style  = new StyleTag();      // スタイル参照
    private CharArray color  = new CharArray();     // フォント色
    private CharArray size   = new CharArray();     // フォントサイズ

    private boolean   bold   = false;               // ボールド
    private boolean   italic = false;               // イタリック
    private int       align  = 0;                   // 位置情報
    
    private boolean wordwrap = false;      // ワードラップする since
    private int     wrap_offset   = 0;          // ワードラップオフセット
    private int     wrap_width  = 0;        // ワードラップ幅PC用

    private boolean   tagConvert   = false;        // タグ変換を行うか？
    
    private boolean marquee = false;    // マーキーモード
    {
        caQueue.enqueue(string);
        type = STRING;
    }
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public StringData() { }
    public StringData(StringData from, SessionObject session) { 
        this.sessionObject = session;
        copy(from);
    }
    public StringData(String text) {
        setText(text);
    }
    public StringData(CharArray text) {
        setText(text);
    }
    
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setValue(String[] strs) {string.set(strs[0]);}
    /** @deprecated */
    public void setValue(String str) {string.set(str);}
    /** @deprecated */
    public void setValue(CharArray ch) {string.set(ch);}
    
    /** 文字列の設定 */
    public void setText(String str) {string.set(str);}
    public void setText(CharArray ch) {string.set(ch);}
    /** 文字列の追加 */
    public void addText(String str) {string.add(str);}
    public void addText(CharArray ch) {string.add(ch);}
    /** 数値設定 */
    public void setInt(int num) {string.clear(); string.format(num);}
    /** 数値設定 */
    public void setLong(long num) {string.clear(); string.format(num);}
    
    /** フォント色の設定 */
    public void setColor(String str) {color.set(str);}
    public void setColor(CharArray ch) {color.set(ch);}
    /** フォントサイズの設定 */
    public void setSize(String str) {size.set(str);}
    public void setSize(CharArray ch) {size.set(ch);}
    public void setSize(int i) {size.reset(); size.format(i);}
    /** bold 設定*/
    public void setBold(boolean b) { bold = b;}
    /** italic 設定 */
    public void setItalic(boolean b) { italic = b;}
    /** Style シート参照設定 */
    public void setStyle(String str)   { style.setStyle(str);}
    public void setStyle(CharArray ch) { style.setStyle(ch);}
    public void setClass(String str)   { style.setClass(str);}
    public void setClass(CharArray ch) { style.setClass(ch);}
    public void setID(String str)      { style.setID(str);}
    public void setID(CharArray ch) { style.setID(ch);}

    /** align情報設定 */
    public void setAlign(int i) { 
        if (i < ItemConstant.align.length) align = i;
    }

    /** フォントタグを生成する必要があるか？ */
    public boolean useFont() {
        return (color.length() > 0 || size.length() > 0 || style.existTag());
    }

    public void clear() { string.clear();}

    /** wordwrap をするか？ */
    public void setWrap(boolean mode) { wordwrap = mode;}
    
    /** wordwrap 開始オフセット */
    public void setWrapOffset(int offset) { this.wrap_offset = offset;}
    
    /** PC用のwordwrap幅を指定する (0でなし）*/
    public void setWrapWidth(int width) { this.wrap_width = width;}
    
    /** Tag変換を行うか？ */
    public void setTagConvert(boolean mode) { tagConvert = mode;}
    
    /** Marquee表示を行うか? */
    public void setMarquee(boolean mode) { marquee = mode;}
    
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; }
    
    /** 表示文字列を取得する */
    public CharArray getText() { return string; }
    /** 数値を取得する */
    public int getInt() { return string.getInt(); }
    /** 数値を取得する */
    public long getLong() { return string.getLong(); }
    
    public CharArray getColor() { return color; }
    public CharArray getSize() { return size; }
    public boolean isBold() { return bold;}
    public boolean isItalic() { return italic;}
    
    /** スタイルシート タグ埋め込み情報を取得する */
    public CharArray getStyleTag() { return style.getTag();}
    public CharArray getStyleTag(CharArray ch) { return style.getTag(ch);}
    
    public int getAlign() { return align;}
    
    /** ワードラップを行うか */
    public boolean isWordWrap() { return wordwrap;}
    /** ワードラップオフセットを取得する */
    public int     getWrapOffset() { return wrap_offset;}
    /** PC用の wrap幅を取得 */
    public int     getWrapWidth() { return wrap_width;}

    /** Tag文字変換を行うか？*/
    public boolean isTagConvert() { return tagConvert;}
    public boolean getTagConvert() { return tagConvert;}
    
    /** marquee表示モードか？*/
    public boolean isMarquee() { return marquee;}
    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    public void copy(StringData from) { // 元オブジェクトより全データをコピー
        super.copy(from);
        string.set(from.string);
        color.set(from.color);
        size.set(from.size);
        bold = from.bold;
        italic = from.italic;
        style.copy(from.style);
        align = from.align;
        wordwrap = from.wordwrap;
        wrap_offset = from.wrap_offset;
        wrap_width = from.wrap_width;
        tagConvert = from.tagConvert;
        marquee = from.marquee;
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
            session.itemRenderer.drawString(session,session.getBuffer(), this);
        }
        return session.getBuffer();
    }
    public CharArray getTag() {
        return getTag(new CharArray());
    }
    public CharArray getTag(CharArray ch) {
        return sessionObject.itemRenderer.drawString(sessionObject,ch,this);
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            string.writeObject(out);
            style.writeObject(out);
            color.writeObject(out);
            size.writeObject(out);
            out.writeBoolean(bold);
            out.writeBoolean(italic);
            out.writeInt(align);
            
            out.writeBoolean(wordwrap);
            out.writeInt(wrap_offset);
            out.writeInt(wrap_width);
            
            out.writeBoolean(tagConvert);
            out.writeBoolean(marquee);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            string.readObject(in);
            style.readObject(in);
            color.readObject(in);
            size.readObject(in);
            bold = in.readBoolean();
            italic = in.readBoolean();
            align = in.readInt();
            
            wordwrap    = in.readBoolean();
            wrap_offset = in.readInt();
            wrap_width  = in.readInt();
            
            tagConvert = in.readBoolean();
            marquee = in.readBoolean();
        }
    }
}

//
// [end of StringData.java]
//

