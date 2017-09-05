//--------------------------------------------------------------------
// @(#)ByteBufferStack.java
//     ByteBufferを再利用する
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-03-11   ishioka.toru@miraidesign.com
//--------------------------------------------------------------------
package com.miraidesign.util;

import java.util.Stack;
import java.nio.ByteBuffer;

/**
 * <p>ByteBuffer を再利用する</p>
 * システム共通バッファ(static)と通常バッファ(instance毎)の2種類を持つ<br>
 * 最小サイズ:0 最大サイズ:64 をデフォルトとする。<br>
 * バッファサイズにあまりばらつきのない場合は同一のインスタンスを<br>
 * 利用すると、メモリ効率が良くなる。<br>
 * デフォルトバッファサイズは 1024*8
 * バッファ領域は allocateDirect()でヒープ外に確保する。
 *  @version 1.0 2010-03-11
 *  @author Toru Ishioka
 *  @since  JDK1.5
 * 
 */
public class ByteBufferStack {

    // -----------------------------------------------------
    //  システム共通(static)バッファ処理
    // -----------------------------------------------------
    static protected ByteBufferStack common = null;
    
    /** システム共通バッファを取得する */
    static public ByteBufferStack getInstance() {
        if (common == null) common = new ByteBufferStack();
        return common;
    }
    /** システム共通バッファを取得する */
    static public ByteBufferStack getInstance(int capacity) {
        if (common == null) common = new ByteBufferStack(capacity);
        else common.setBufferSize(capacity);
        return common;
    }
    
    // -----------------------------------------------------
    //  通常バッファ処理
    // -----------------------------------------------------
    protected int MIN_SIZE =   0;
    protected int MAX_SIZE =  64;

    protected int bufferSize = 1024*8;
    /**
       バッファサイズを指定する
    */
    public void setBufferSize(int size) { this.bufferSize = size;}
    /**
        バッファサイズを取得する
    */
    public int getBufferSize() { return bufferSize;}

    /** ByteBuffer専用スタック */
    protected static Stack<ByteBuffer> stack;

    /** constructor */
    public ByteBufferStack() {
        stack = new Stack<ByteBuffer>();
    }
    /** constructor */
    public ByteBufferStack(int buffer_size) {
        stack = new Stack<ByteBuffer>();
        setBufferSize(buffer_size);
    }
    public ByteBufferStack(int buffer_size,int max_size) {
        stack = new Stack<ByteBuffer>();
        setBufferSize(buffer_size);
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
    public ByteBufferStack clear() {
        synchronized (stack) {
            stack.clear();
        }
        return this;
    }
    
    /**
        ByteBuffer をスタックに保管する
        @param buf 保管する ByteBuffer
    */
    public ByteBuffer push(ByteBuffer buf) {
        buf.clear();
        synchronized (stack) {
            if (stack.size() >= MAX_SIZE) buf = null;
            else                      stack.push(buf);
        }
        return buf;
    }
    
    /**
        ByteBuffer をスタックより取得する
        @return 取得したByteBuffer
    */
    public ByteBuffer pop() {
        ByteBuffer buf = null;
        synchronized (stack) {
            if (stack.size() <= MIN_SIZE) buf = ByteBuffer.allocateDirect(bufferSize);
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
// end of ByteBufferStack.java
