//------------------------------------------------------------------------
// @(#)Input.java
//              入力パラメータ情報
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
//
package com.miraidesign.content;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;

/**
 *  Input   入力パラメーター情報
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class Input implements ContentItem {
    private static boolean debug = false;
    public static final int TEXT     = 1;    // 
    public static final int TEXTAREA = 2;    // 
    public static final int HTML     = 3;    // 
    public static final int LINK     = 4;    // 
    public static final int IMAGE    = 5;    // 
    public static final int FILE     = 6;    // 
    public static final int SELECT   = 7;    // 
    public static final int TABLE    = 8;    // 
    public static final int COMMON_TEXT  = 9;  // 
    public static final int DATA     = 10;    //  
    //headerコントロール用
    public static final int META     = 11;   // 
    public static final int BODY     = 12;   // 
    public static final int BASE     = 13;   // 
    public static final int TITLE    = 14;   //
    public static final int STYLE    = 15;   //
    public static final int SCRIPT   = 16;   // 
    
    public static final int INPUT_ITEM_MAX = 7; // 未使用
    //public static final int  = 10;    // 
    
    public static String[] typeNames = {
        "","TEXT","TEXTAREA","HTML","LINK","IMAGE","FILE","SELECT",
        "TABLE","COMMON_TEXT","DATA","META","BODY",
        "BASE","TITLE","STYLE","SCRIPT"
    };
    
    private static CharArrayQueue chTypeNames = new CharArrayQueue();
    static {
        for (int i = 0; i < typeNames.length; i++) {
            CharArray ch = new CharArray(typeNames[i]);
            chTypeNames.enqueue(ch);
        }
    }
    /** パラメータタイプ */
    public int type = 0;
    
    protected HashParameter hash;
    
    /** タグ内データ */
    protected CharArray data = new CharArray();
    //----------------------
    // constructor
    //----------------------
    /* デフォルトコンストラクタ */
    public Input() { }
    
    /* タイプ(int)を指定する */
    public Input(int type) { 
        setType(type);
    }
    /* タイプ名を指定する */
    public Input(String type_name) { 
        setType(type_name);
    }
    /* タイプ名を指定する */
    public Input(CharArray type_name) { 
        setType(type_name);
    }

    /* オリジナルアイテムを取得する */
    public ContentItem getOriginalItem() { return this; }
    
    //----------------------
    // method
    //----------------------
    /* 指定タイプ名をチェックする */
    static public int getType(CharArray type_name) {
        int _type = 0;
        if (type_name != null) {
            CharArray ch = CharArray.pop(type_name);
            ch.trim().toUpperCase();
            for (int i = 1; i < typeNames.length; i++) {
                if (ch.equals(typeNames[i])) {  // found!!
                    _type = i; 
                    break;
                }
            }
            CharArray.push(ch);
        }
        if (_type <= 0) {
            System.out.println("Input: 入力type("+type_name+")が取得できません");
        }
if (debug) System.out.println("Input:getType("+type_name+")=="+_type);
        return _type;
    }
    static public int getType(String type_name) {
        CharArray ch = CharArray.pop(type_name);
        int _type = getType(ch);
        CharArray.push(ch);
        return _type;
    }
    
    /* 入力タイプを設定する */
    public void setType(int type) {
        this.type = type;
    }
    
    /* 入力タイプ名を指定する */
    public int setType(CharArray type_name) {
        type = getType(type_name);
        return type;
    }
    public int setType(String type_name) {
        type = getType(type_name);
        return type;
    }
    
    public CharArray getType() {
        return chTypeNames.peek(type);
    }
    public CharArray getTypeName() {
        return chTypeNames.peek(type);
    }
    public static CharArray getTypeName(int type) {
        return chTypeNames.peek(type);
    }
   
    /* パラメータリストを設定する */
    public void setParameterList(HashParameter hash) {
        this.hash = hash;
    }
    
    /* パラメータ全体を取得する */
    public HashParameter getParameterList() {
        if (hash == null) hash = new HashParameter();
        return hash;
    }
    //----------------------------------------
    // 共用メソッド
    //----------------------------------------
    public boolean isBlock() {return false;}
    public boolean isInput() {return true;}
    public boolean isTemplate() {return false;}

    private ContentItemNode node = null;
    public void setContentItemNode(ContentItemNode node) {
        this.node = node;
    }
    public ContentItemNode getContentItemNode() { return node;}


    /* キーワードを取得する
        @return 存在しない場合は null
    */
    public CharArray getKey() {
        return hash.get("key");
    }
    /* ラベルを取得する 
        @return 存在しない場合は null
    */
    public CharArray getLabel() {
        return hash.get("label");
    }
    /* コメントを取得する 
        @return 存在しない場合は null
    */
    public CharArray getDescription() {
        return hash.get("description");
    }
    
    /* 表示モードを取得 (デフォルト:true) */
    public boolean isDisplay() {
        boolean sts = true;
        CharArray ch = hash.get("display");
        if (ch != null) {
            sts = ch.getBoolean();
        }
        return sts;
    }
    
    /* 省略可能か？ (デフォルト:true) */
    public boolean isNullable() {
        boolean sts = true;
        CharArray ch = hash.get("nullable");
        if (ch != null) {
            sts = ch.getBoolean();
        }
        return sts;
    }
    
    /* デフォルト文字列の取得 */
    public CharArray getDefault() {
        return hash.get("default");
    }
    
    /* 指定パラメータを取得する 
        @return 存在しない場合は null
    */
    public CharArray getParameter(String key) {
        String szKey = key.toLowerCase();
        return hash.get(szKey);
    }
    public CharArray getParameter(CharArray key) {
        String szKey = key.toString().toLowerCase();
        return hash.get(szKey);
    }
    
    /* タグ間データを取得(script/style用 */
    public CharArray getData() { return data; }
    
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    /* @deprecated */
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
        }
    }
    /* @deprecated */
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
        }
    }
}

//
// $Author$
// $Source$
// $Revision$
// $Date$
//
// $Log$
//
// [end of Input.java]
//

