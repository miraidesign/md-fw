//------------------------------------------------------------------------
//    Parameter.java                                                       
//          フォーマッタに渡すパラメータを定義する
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-04   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.util.Stack;

/**  フォーマッタに渡すパラメータ（QueueElementの別名) */
public class Parameter extends QueueElement {

    //再利用のためのメソッド
    private static Stack<Parameter> stack = new Stack<Parameter>();
    private static int MIN_SIZE =  32;
    private static int MAX_SIZE = 256;
    public static /*synchronized*/ Parameter push(Parameter item) {
        if (stack.size() > MAX_SIZE) {
            item = null;    // for GC
            return item;
        } else {
            item.clear();
            return (Parameter)stack.push(item);
        }
    }
    
    public static /*synchronized*/ Parameter pop() {
        Parameter queue;
        synchronized (stack) {
            if (stack.size() < MIN_SIZE) {
                queue = new Parameter();
            } else {
                queue = (Parameter)stack.pop();
                queue.clear();
            }
        }
        return queue;
    }
    
    public Parameter() { super(); }
    /**
        @param convert タグ文字変換を行う
    */
    public Parameter(boolean convert) { super(convert); }
    public Parameter(int size) {
        super(size);
        clear();
    }
}

//
// [end of Parameter.java]
//
