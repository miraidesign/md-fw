//------------------------------------------------------------------------
//    ObjectStack.java
//              指定オブジェクトを再利用する
//
//          Copyright (c) Mirai Design Institute 2011 All rights reserved.
//          update 2011-08-30   ishioka.toru@miraidesign.com
//--------------------------------------------------------------------
package com.miraidesign.util;

import java.util.Stack;

/**
 * 指定オブジェクトを再利用する。<br>
 * システム共通バッファ(static)と通常バッファ(instance毎)の2種類を持つ。<br>
 * 最小サイズ:0 最大サイズ:64 をデフォルトとする。<br>
 * バッファサイズにあまりばらつきのない場合は同一のインスタンスを<br>
 * 利用すると、メモリ効率が良くなる。
 *  
 *  @version 1.2 2011-08-30
 *  @author Toru Ishioka
 *  @since  JDK1.5
**/

public class ObjectStack<T> {
    // -----------------------------------------------------
    //  通常バッファ処理
    // -----------------------------------------------------
    private static int MIN_SIZE =   0;
    private static int MAX_SIZE =  64;

    /** CharArray 専用スタック */
    private Stack<T> stack;
    
    /** constructor */
    public ObjectStack() {
        stack = new Stack<T>();
    }
    /** constructor */
    public ObjectStack(int max_size) {
        stack = new Stack<T>();
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
    public ObjectStack clear() {
        synchronized (stack) {
            stack.clear();
        }
        return this;
    }
    
    /** オブジェクトを返却する 
        クリア等は自分で行う事
    **/
    public  T push(T item) {
        synchronized (stack) {
            if (stack.size() >= MAX_SIZE) item = null;
            else                          stack.push(item);
        }
        return item;
    }
    
    /** 保管したオブジェクトを取り出す <br>
        nullが返った場合は、自分でnew したものを使う事
    
    **/
    public T pop() {
        T obj = null;
        synchronized (stack) {
            if (stack.size() <= MIN_SIZE) {
                //if (stack.size() > 0) {
                    //try {
                    //    obj =  ((Class<T>)stack.peek()).newInstance();
                    //} catch (Exception ex) {
                    //    ex.printStackTrace();
                    //}
                //}
            } else {
                obj=  stack.pop();
            }
        }
        return obj;
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
// [end of ObjectStack.java]
//

