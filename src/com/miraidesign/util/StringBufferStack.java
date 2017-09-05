//--------------------------------------------------------------------
// @(#)StringBuferStack.java
//     StringBufferを再利用する
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-02   ishioka.toru@miraidesign.com
//--------------------------------------------------------------------
package com.miraidesign.util;

import java.util.Stack;

/**
 * <p>StringBuffer を再利用する</p>
 * システム共通バッファ(static)と通常バッファ(instance毎)の2種類を持つ<br>
 * 最小サイズ:0 最大サイズ:64 をデフォルトとする。<br>
 * バッファサイズにあまりばらつきのない場合は同一のインスタンスを<br>
 * 利用すると、メモリ効率が良くなる。
 *
 *  @version 1.0 2010-02-02
 *  @author Toru Ishioka
 *  @since  JDK1.5
 * 
 */
public class StringBufferStack {

    // -----------------------------------------------------
    //  システム共通(static)バッファ処理
    // -----------------------------------------------------
    static protected StringBufferStack common = null;
    
    /** システム共通バッファを取得する */
    static public StringBufferStack getInstance() {
        if (common == null) common = new StringBufferStack();
        return common;
    }
    
    /** システム共通バッファを取得する */
    static public StringBufferStack getInstance(int capacity) {
        if (common == null) common = new StringBufferStack(capacity);
        else common.setCapacity(capacity);
        return common;
    }
    
    // -----------------------------------------------------
    //  通常バッファ処理
    // -----------------------------------------------------
    protected int MIN_SIZE =   0;
    protected int MAX_SIZE =  64;

    private int capacity = 16;

    /** StringBuffer専用スタック */
    protected static Stack<StringBuffer> stack;

    /** constructor */
    public StringBufferStack() {
        stack = new Stack<StringBuffer>();
    }
    /** constructor */
    public StringBufferStack(int max_size) {
        stack = new Stack<StringBuffer>();
        setMaxSize(max_size);
    }
    /** constructor */
    public StringBufferStack(int max_size, int capacity) {
        stack = new Stack<StringBuffer>();
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
    public void clear() {
        synchronized (stack) {
            stack.clear();
        }
    }
    
    /**
        StringBuffer をスタックに保管する
        @param sb 保管する StringBuffer
    */
    public StringBuffer push(StringBuffer sb) {
        sb.setLength(0);
        synchronized (stack) {
            if (stack.size() >= MAX_SIZE) sb = null;
            else                      stack.push(sb);
        }
        return sb;
    }
    
    /**
        StringBuffer をスタックより取得する
        @return 取得したStringBuffer
    */
    public StringBuffer pop() {
        StringBuffer sb = null;
        synchronized (stack) {
            if (stack.size() <= MIN_SIZE) sb = new StringBuffer();
            else sb = stack.pop();
        }
        return sb;
    }
}

// $Author:$
// $Source:$
// $Revision:$
// $Date:$
//
// $Log:$
// 
// end of StringBufferStack.java
