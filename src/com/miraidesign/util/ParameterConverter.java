//------------------------------------------------------------------------
//    ParameterConverter.java                                                       
//          パラメータを利用したコンバート機能を提供する
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-04   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------

package com.miraidesign.util;

/**  パラメータを利用したコンバート機能を提供する */
public class ParameterConverter extends Parameter implements Converter {

    private CharArray head = new CharArray("$");
    private CharArray tail = new CharArray("");

    private int width = 1;

    /** constructor */
    public ParameterConverter() { super(); }
    /** constructor
        @param convert タグ文字変換を行う */
    public ParameterConverter(boolean convert) { super(convert); }
    /** constructor
        @param size 初期サイズ */
    public ParameterConverter(int size) {
        super(size);
        clear();
    }
    /* コンバータをコピーする */
    public Converter copy() {
        ParameterConverter p = new ParameterConverter();
        p.head.set(head);
        p.tail.set(tail);
        p.width = width;
    
        return (Converter)p;
    }
    /**
    public QueueElement add(CharArray str) {
        return super.add(str);
    }
    public QueueElement add(String str) {
        return super.add(str);
    }
    **/
    //--------------------------------------------------------------
    // 設定
    //--------------------------------------------------------------
    /**
        パラメータの接頭文字列を指定する<br>
        デフォルト "$"
        @param str  接頭文字列
    */
    public void setHead(String str) {
        head.set(str);
    }
    /**
        パラメータの接尾文字列を指定する<br>
        デフォルト ""
        @param str  接尾文字列
    */
    public void setTail(String str) {
        tail.set(str);
    }
    
    /**
        パラメータ数値の最小幅を指定する<br>
        デフォルト 1  (1,2,3..) 2だと(01,02,03,...となる）
    */
    public void setWidth(int width) {
        this.width = width;
    }
    //--------------------------------------------------------------
    // コンバーター
    //--------------------------------------------------------------
    /**
       コンバートを行う
       @param str 変換元文字列
       @return 変換
    */
    public CharArray convert(CharArray str) {
        if (size() == 0 || str == null || str.length()==0) return str;
        CharArray ch = CharArray.pop();
        for (int i = 0; i < size(); i++) {
            ch.set(head);
            ch.format(i+1,10,width,'0');
            ch.add(tail);
            str.replace(ch,peek(i));
        }
        CharArray.push(ch);
        return str;
    }
    /**
       コンバートを行う
       @param str 変換元文字列
       @return 変換
    */
    public String convert(String str) {
        if (size() == 0 || str == null || str.length()==0) return str;
        CharArray ch = CharArray.pop(str);
        String ret = convert(ch).toString();
        CharArray.push(ch);
        return ret;
    }
}

//
// [end of ParameterConverter.java]
//
