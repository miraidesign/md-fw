//------------------------------------------------------------------------
//    CharToken.java                                                      
//                 token 分離読込みクラス                              
//
//      Copyright (c) Mirai Design Institute 2010-14 All rights reserved.
//                      update 2014-03-12   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
package com.miraidesign.util;

import java.util.Stack;

/**
    token 分離読込みクラス<br>
    対象は CharArray
**/

public class CharToken extends CharArrayQueue {
    private boolean debug = false;
    public void setDebugMode(boolean mode) {debug = mode;}
    {
        setMemSavingMode(false);
    }

    // 再利用用 ------------------------------------------------------
    static private Stack<CharToken> stack = new Stack<CharToken>();
    private static int MIN_SIZE =   8;
    private static int MAX_SIZE = 128;

    static public int getStackSize() { return stack.size(); }
    /** CharToken を stackに保管する */
    static public synchronized CharToken push(CharToken ct) {
        if (stack.size() > MAX_SIZE || ct == null) {
            ct = null; 
            return ct;
        } else {
            ct.clear();
            ct.delm.set(",");
            return (CharToken)stack.push(ct);
        }
    }
    /** 
        CharTokenを stackから取得する 
        @return CharToken インスタンス
    */
    static public /*synchronized*/ CharToken pop() {
        CharToken token;
        synchronized (stack) {
            if (stack.size() < MIN_SIZE) {
                token = new CharToken();
            } else {
                token = (CharToken)stack.pop();
            }
        }
        return token;
    }
    
    /** 
        CharTokenを stackから取得して初期値を設定する
        @param str 初期設定値
        @return CharToken インスタンス
    */
    static public CharToken pop(String str) {
        CharToken ct = pop();
        ct.set(str);
        return ct;
    }
    /** 
        CharTokenを stackから取得して初期値を設定する
        @param str 初期設定値
        @return CharToken インスタンス
    */
    public static CharToken pop(CharArray str) {
        CharToken ct = pop();
        ct.set(str);
        return ct;
    }
    /** 
        CharTokenを stackから取得して初期値、デリミタを設定する
        @param str 初期設定値
        @param delm デリミタ
        @return CharToken インスタンス
    */
    static public CharToken pop(String str, String delm) {
        CharToken ct = pop();
        ct.set(str,delm);
        return ct;
    }
    /** 
        CharTokenを stackから取得して初期値、デリミタを設定する
        @param str 初期設定値
        @param delm デリミタ
        @return CharToken インスタンス
    */
    public static CharToken pop(CharArray str, CharArray delm) {
        CharToken ct = pop();
        ct.set(str,delm);
        return ct;
    }

    //----------------------------------------------------------------
    /** デリミタ（トークン分離記号）デフォルトはカンマ */
    CharArray delm = new CharArray(",");
    
    /** コンストラクタ */
    public CharToken() { super(); }
    
    /** 
        コンストラクタ 
        @param chLine 設定データ
    */
    public CharToken(CharArray chLine) {
        super();
        set(chLine);
    }
    
    /** 
        コンストラクタ 
        @param chLine 設定データ
        @param delimiter デリミタ指定
    */
    public CharToken(CharArray chLine, CharArray delimiter) {
        super();
        delm.set(delimiter);
        set(chLine);
    }
    
    /** 
        コンストラクタ 
        @param szLine 設定データ
    */
    public CharToken(String szLine) {
        super();
        set(szLine);
    }
    
    /** 
        コンストラクタ 
        @param szLine 設定データ
        @param delimiter デリミタ指定
    */
    public CharToken(String szLine,String delimiter) {
        super();
        delm.set(delimiter);
        set(szLine);
    }
    
    /** 
        コンストラクタ 
        @param chLine 設定データ
        @param delimiter デリミタ指定
    */
    public CharToken(CharArray chLine, String delimiter) {
        super();
        delm.set(delimiter);
        set(chLine);
    }
    //---------------------------------------------------------
    // method
    //---------------------------------------------------------
    
    /** 
        データ行とデリミタの設定 
        @param chLine 設定データ
        @param delimiter デリミタ指定
        @return true:設定成功
    */
    public boolean set(CharArray chLine, CharArray delimiter) {
        if (delimiter == null || delimiter.length() <= 0) return false;
        delm.set(delimiter);
        return set(chLine);
    }
    
    /** 
        データ行とデリミタの設定 
        @param chLine 設定データ
        @param delimiter デリミタ指定
        @return true:設定成功
    */
    public boolean set(CharArray chLine, String delimiter) {
        if (delimiter == null || delimiter.length() <= 0) return false;
        delm.set(delimiter);
        return set(chLine);
    }
    
    /** 
        データ行のみ設定 
        @param ch 設定データ
        @return true:設定成功
    */
    public boolean set(CharArray ch) {
        clear();
        if (ch == null || ch.length() <= 0) return false;
        return parse(ch);
    }

    /** 
        デリミタの設定 
        @param ch デリミタ指定
    */
    public void setDelimiter(CharArray ch) {
        delm.set(ch);
    }
    /** 
        デリミタの設定 
        @param str デリミタ指定
    */
    public void setDelimiter(String str) {
        delm.set(str);
    }
    /**
        データのパース。デリミタ分解処理
        @param ch データ
    */
    private boolean parse(CharArray ch) {
        int size = delm.length();
        int i = 0;
        while (i < ch.length()) {
            int index = ch.indexOf(delm,i);
            if (index >= 0) {   // 見つけた
                     // i to index までを保管
                enqueue(new CharArray(ch.chars,i,index-i));
                i = index + size;
                if (i >= ch.length()) {
                    enqueue(new CharArray());
                    break;
                }
            } else {
                enqueue(new CharArray(ch.chars,i,ch.length()-i));
                break;
            }
        }
        return true;
    }
    /**
        検索文字列の設定
    */
    public boolean setSearchString(String str) {
        return setSearchString(new CharArray(str));
    }
    public boolean setSearchString(CharArray ch) {
        clear();
        return addSearchString(ch);
    }
    /**
        検索文字列の追加
    */
    public boolean addSearchString(String str) {
        return addSearchString(new CharArray(str));
    }
    public boolean addSearchString(CharArray str) {
        CharArray ch = CharArray. pop(str);
        int i = 0;
        int index =0;
        int QUAT_MODE = 0;  // 1:single quot 2: double quot
        
        int start = 0;
        while (i < ch.length()) {
            while (i < ch.length()) {
                char c = ch.chars[i++];
                if (c <= ' ' || c == '　' ) {
                    if (QUAT_MODE < 0) {
                        enqueue(ch.substr(start,i-1));
                        QUAT_MODE = 0;
                        start = i; //@@
                    }
                    start = i;
                } else if (c == '\'') {
                    QUAT_MODE = 1; //start = i;
                    int idx = ch.indexOf('\'',i);
                    if (idx >= 0) {
                        ch.remove(i-1,1); i--;
                    }
                    break;
                } else if (c == '\"') {
                    QUAT_MODE = 2; //start= i;
                    int idx = ch.indexOf('\"',i);
                    if (idx >= 0) {
                        ch.remove(i-1,1); i--;
                    }
                    break;
                } else {
                    if (QUAT_MODE !=0) {
                        QUAT_MODE = 0; 
                    } else {
                        start = i-1; 
                    }
                    break;
                }
            }
            
            if (QUAT_MODE == 0) { // 通常文字検索
                while (i < ch.length()) {
                    char c = ch.chars[i++];
                    if (c <= ' ' || c == '　' ) {
                        break;    // skip
                    } else if (c == '\'') {
                        QUAT_MODE = 1; 
                        int idx = ch.indexOf('\'',i);
                        if (idx >= 0) {
                            ch.remove(i-1,1); i--;
                        }
                        break;
                    } else if (c == '\"') {
                        QUAT_MODE = 2;  
                        int idx = ch.indexOf('\"',i);
                        if (idx >= 0) {
                            ch.remove(i-1,1); i--;
                        }
                        break;
                    } else {
                        //QUAT_MODE = 0; 
                    }
                }
                
                if (i == ch.length()) {
                    i++;
                    enqueue(ch.substr(start,i-1));
                    start = i; //@@
                } else if (QUAT_MODE == 0) {
                    enqueue(ch.substr(start,i-1));
                    start = i; //@@
                }
            }
            if (QUAT_MODE == 1) {
                index = ch.indexOf('\'',i);
                if (index >= 0) { // found
                    ch.remove(index,1);
                    i = index;
                    QUAT_MODE = 0;
                    
                    while (i < ch.length()) {
                        char c = ch.chars[i++];
                        if (c <= ' ' || c == '　' ) {
                            break;    // skip
                        } else if (c == '\'') {
                            QUAT_MODE = 1; 
                            
                            int idx = ch.indexOf('\'',i);
                            if (idx >= 0) {
                                ch.remove(idx,1);
                                ch.remove(i-1,1); i--;
                            }
                            break;
                        } else if (c == '\"') {
                            QUAT_MODE = 2;  
                            
                            int idx = ch.indexOf('\"',i);
                            if (idx >= 0) {
                                ch.remove(idx,1);
                                ch.remove(i-1,1); i--;
                            }
                            break;
                        } else {
                            //QUAT_MODE = 0; 
                        }
                    }
                    if (QUAT_MODE == 0) {
                        if (i == ch.length()) i++;
                        enqueue(ch.substr(start,i-1));
                        start = i;  //@@
                     }
                } else { // not found
                    // 無視して検索を開始する
                    QUAT_MODE = -1;
                }
            } else if (QUAT_MODE == 2) {
                index = ch.indexOf('\"',i);
                if (index >= 0) { // found
                    ch.remove(index,1);
                    i = index;
                    QUAT_MODE = 0;
                    
                    while (i < ch.length()) {
                        char c = ch.chars[i++];
                        if (c <= ' ' || c == '　' ) {
                            break;    // skip
                        } else if (c == '\'') {
                            QUAT_MODE = 1; 
                            int idx = ch.indexOf('\'',i);
                            if (idx >= 0) {
                                ch.remove(idx,1);
                                ch.remove(i-1,1); i--;
                            }
                            
                            break;
                        } else if (c == '\"') {
                            QUAT_MODE = 2;  
                            int idx = ch.indexOf('\'',i);
                            if (idx >= 0) {
                                ch.remove(idx,1);
                                ch.remove(i-1,1); i--;
                            }
                            
                            break;
                        } else {
                            //QUAT_MODE = 0; 
                        }
                    }
                    if (i == ch.length()) i++;
                    enqueue(ch.substr(start,i-1));
                    start = i;  //@@
                    
                } else { // not found
                    // 無視して検索を開始する
                    QUAT_MODE = -1;
                }
            }
        }
        CharArray.push(ch);
        return true;
    }
    
    //--------------------------------------------------
    
    /** 
        データ行とデリミタの設定 
        @param szLine 設定データ
        @param str デリミタ指定
        @return true:設定成功
    */
    public boolean set(String szLine, String str) {
        if (str == null || str.length() <= 0) return false;
        delm.set(str);
        return set(szLine);
    }
    
    /** 
        データ行のみ設定 
        @param szLine 設定データ
        @return true:設定成功
    */
    public boolean set(String szLine) {
        clear();
        if (szLine == null || szLine.length() <= 0) return false;
        CharArray ch = CharArray.pop(szLine);
        boolean sts = parse(ch);
        CharArray.push(ch);
        return sts;
    }
    
    //--------------------------------------------------
    /**
     * カンマ区切りファイルを自動認識する<br>
     * 遅くてもよい人はこれを
     * @param str データ文字列
     */
    public boolean setCSV(CharArray str) {
        clear();
        int idx1 = str.indexOf('\'');
        int idx2 = str.indexOf('\"');
        if (idx1 < 0 && idx2 < 0) { // コーテーションがない！
            return set(str,",");
        } else {
            int mode = 0;
            CharArray ch = new CharArray();
            for (int i = 0; i < str.length(); i++) {
                char c = str.chars[i];
                if (mode == 0) {
                    if (c == '\'') {
                        ch.reset();
                        mode = 1;
                    } else if (c == '\"') {
                        ch.reset();
                        mode = 2;
                    } else if (c == ',') {
                        enqueue(ch);
                        ch = new CharArray();
                    } else {
                        ch.add(c);
                    }
                } else if (mode == 1) { // '
                    if (c == '\'') {
                        enqueue(ch);
                        ch = new CharArray();
                        mode = 3;
                    } else ch.add(c);
                } else if (mode == 2) { // '
                    if (c == '\"') {
                        enqueue(ch);
                        ch = new CharArray();
                        mode = 3;
                    } else ch.add(c);
                } else if (mode == 3) {
                    if (c == ',') mode = 0;
                }
                
            }
            if (ch.length() > 0) enqueue(ch);
        }
        return true;
    }
    
    
    //--------------------------------------------------
    /**
        指定分割位置(0-)の文字列を取り出す
        @param index 指定分割位置
        @return 取得文字列
    */
    public CharArray get(int index) {
        return peek(index);
    }
    /**
        指定分割位置(0-)の文字列を取り出す
        @param index 指定分割位置
        @return 取得文字列
    */
    public String getString(int index) {
        return ""+peek(index);
    }
    //----------
    /**
        指定分割位置(0-)の文字列をint型に変換して取り出す
        @param index 指定分割位置
        @return 取得数値
    */
    public int getInt(int index) {
        return get(index).getInt();
    }
    /**
        指定分割位置(0-)の文字列をlong型に変換して取り出す
        @param index 指定分割位置
        @return 取得数値
    */
    public long getLong(int index) {
        return get(index).getLong();
    }
    
    /**
        指定分割位置(0-)の文字列をdouble型に変換して取り出す
        @param index 指定分割位置
        @return 取得数値
    */
    public double getDouble(int index) {
        return get(index).getDouble();
    }
    /**
        指定分割位置(0-)の文字列を論理型に変換して取り出す
        @param index 指定分割位置
        @return 取得データ
    */
    public boolean getBoolean(int index) {
        return get(index).getBoolean();
    }
    
}

//
// 
// [end of CharToken.java]
//

