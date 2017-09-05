//------------------------------------------------------------------------
//    CharArrayStack.java
//              CharArrayオブジェクトを再利用する
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-02   ishioka.toru@miraidesign.com
//--------------------------------------------------------------------
package com.miraidesign.util;

import java.util.Stack;

/**
 * CharArrayオブジェクトを再利用する。<br>
 * システム共通バッファ(static)と通常バッファ(instance毎)の2種類を持つ。<br>
 * 最小サイズ:0 最大サイズ:64 をデフォルトとする。<br>
 * バッファサイズにあまりばらつきのない場合は同一のインスタンスを<br>
 * 利用すると、メモリ効率が良くなる。
 *  
 *  @version 1.2 2010-02-03
 *  @author Toru Ishioka
 *  @since  JDK1.5
**/

public class CharArrayStack {

    // -----------------------------------------------------
    //  システム共通(static)バッファ処理
    // -----------------------------------------------------
    static protected CharArrayStack common = null;
    
    /** システム共通バッファを取得する */
    static public CharArrayStack getInstance() {
        if (common == null) common = new CharArrayStack();
        return common;
    }
    /** 
        システム共通バッファを取得する 
        @param capacity 初期バッファ容量
    */
    static public CharArrayStack getInstance(int capacity) {
        if (common == null) common = new CharArrayStack();
        common.setCapacity(capacity);
        return common;
    }
    
    // -----------------------------------------------------
    //  通常バッファ処理
    // -----------------------------------------------------
    private static int MIN_SIZE =   0;
    private static int MAX_SIZE =  64;

    private int capacity = 16;

    /** CharArray 専用スタック */
    private Stack<CharArray> stack;
    
    /** constructor */
    public CharArrayStack() {
        stack = new Stack<CharArray>();
    }
    /** constructor */
    public CharArrayStack(int max_size) {
        stack = new Stack<CharArray>();
        setMaxSize(max_size);
    }
    /** constructor */
    public CharArrayStack(int max_size, int capacity) {
        stack = new Stack<CharArray>();
        setMaxSize(max_size);
        setCapacity(capacity);
    }
    
    /** 
        capacity 設定
        @param capacity (default 16)
    */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    /** 
        capacity 取得
        @return capacity (default 16)
    */
    public int getCapacity() {
        return capacity;
    }
    
    /** 
        共用stack保持最小サイズを設定する 
        @param size stack保持最小サイズ (0～)
     */
    public void setMinSize(int size) { 
        if (size >= 0) {
            synchronized (stack) {
                MIN_SIZE = size;
            }
        }
    }
    
    /** 
        stack保持最大サイズを設定する
        @param size stack保持最大サイズ (0～)
     */
    public void setMaxSize(int size) {
        if (size >= 0) {
            synchronized (stack) {
                MAX_SIZE = size;
            }
        }
    }
    /**
        スタックサイズを取得する
        @return スタック件数
    */
    public int size() {
        return stack.size();
    }
    /**
        スタックをクリアする
    */
    public CharArrayStack clear() {
        synchronized (stack) {
            stack.clear();
        }
        return this;
    }
    
    /** CharArrayオブジェクトを返却する **/
    public  CharArray push(CharArray item) {
        item.reset();
        item.setAppendMode(true);
        synchronized (stack) {
            if (stack.size() >= MAX_SIZE) item = null;
            else                         stack.push(item);
        }
        return item;
    }
    
    /** CharArrayオブジェクトを取り出す 
        @return CharArrayオブジェクト
    **/
    public CharArray pop() {
        CharArray ch = null;
        synchronized (stack) {
            if (stack.size() <= MIN_SIZE) ch =  new CharArray(capacity);
            else                         ch =  stack.pop();
        }
        return ch;
    }
    /** CharArrayオブジェクトを取り出す 
        @param str セットする初期値
        @return CharArrayオブジェクト
    **/
    public CharArray pop(String str) {
        CharArray ch = pop();
        ch.set(str);
        return ch;
    }
    /** CharArrayオブジェクトを取り出す 
        @param str セットする初期値
        @return CharArrayオブジェクト
    **/
    public CharArray pop(CharArray str) {
        CharArray ch = pop();
        ch.set(str);
        return ch;
    }
    /** CharArrayオブジェクトを取り出す 
        @param str セットする初期値
        @return CharArrayオブジェクト
    **/
    public CharArray pop(CharSequence str) {
        CharArray ch = pop();
        ch.set(str);
        return ch;
    }
    
}

// $Author:$
// $Source:$
// $Revision:$
// $Date:$
//
// $Log:$
// 
//
// [end of CharArrayStack.java]
//

