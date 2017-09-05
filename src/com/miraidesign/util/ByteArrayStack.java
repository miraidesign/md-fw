//--------------------------------------------------------------------
// @(#)ByteArrayStack.java
//     ByteArrayを再利用する
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-03-11   ishioka.toru@miraidesign.com
//--------------------------------------------------------------------
package com.miraidesign.util;

import java.util.Stack;

/**
 * <p>ByteArray を再利用する</p>
 * システム共通バッファ(static)と通常バッファ(instance毎)の2種類を持つ<br>
 * 最小サイズ:0 最大サイズ:64 をデフォルトとする。<br>
 * バッファサイズにあまりばらつきのない場合は同一のインスタンスを<br>
 * 利用すると、メモリ効率が良くなる。
 *
 *  @version 1.0 2010-03-11
 *  @author Toru Ishioka
 *  @since  JDK1.5
 * 
 */
public class ByteArrayStack {

    // -----------------------------------------------------
    //  システム共通(static)バッファ処理
    // -----------------------------------------------------
    static protected ByteArrayStack common = null;
    
    /** システム共通バッファを取得する */
    static public ByteArrayStack getInstance() {
        if (common == null) common = new ByteArrayStack();
        return common;
    }
    
    // -----------------------------------------------------
    //  通常バッファ処理
    // -----------------------------------------------------
    protected int MIN_SIZE =   0;
    protected int MAX_SIZE =  64;

    /** ByteArray専用スタック */
    protected static Stack<ByteArray> stack;

    /** constructor */
    public ByteArrayStack() {
        stack = new Stack<ByteArray>();
    }
    /** constructor */
    public ByteArrayStack(int max_size) {
        stack = new Stack<ByteArray>();
        setMaxSize(max_size);
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
    public ByteArrayStack clear() {
        synchronized (stack) {
            stack.clear();
        }
        return this;
    }
    
    /**
        ByteArray をスタックに保管する
        @param buf 保管する ByteArray
    */
    public ByteArray push(ByteArray buf) {
        buf.clear();
        synchronized (stack) {
            if (stack.size() >= MAX_SIZE) buf = null;
            else                      stack.push(buf);
        }
        return buf;
    }
    
    /**
        ByteArray をスタックより取得する
        @return 取得したByteArray
    */
    public ByteArray pop() {
        ByteArray buf = null;
        synchronized (stack) {
            if (stack.size() <= MIN_SIZE) buf = new ByteArray();
            else buf = stack.pop();
        }
        return buf;
    }
}

// $Author:$
// $Source:$
// $Revision:$
// $Date:$
//
// $Log:$
// 
// end of ByteArrayStack.java
