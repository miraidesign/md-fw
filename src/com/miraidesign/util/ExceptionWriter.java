//------------------------------------------------------------------------
//    ExceptionWriter.java
//         Exception表示に特化したQueueWriter
//                 Copyright (c) MIrai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.util.Stack;

/** Exception表示に特化したQueueWriter */
public class ExceptionWriter extends QueueWriter {
    private static Stack<ExceptionWriter> stack = new Stack<ExceptionWriter>();
    private static int MIN_SIZE =  1;
    private static int MAX_SIZE = 256;
    /** 再利用スタックに保存 */
    public static /*synchronized*/ ExceptionWriter push(ExceptionWriter item) {
        if (stack.size() > MAX_SIZE) {
            item = null;    // for GC
            return item;
        } else {
            item.clear();
            return (ExceptionWriter)stack.push(item);
        }
    }
    
    public static /*synchronized*/ ExceptionWriter pop() {
        ExceptionWriter writer;
        synchronized (stack) {
            if (stack.size() < MIN_SIZE) {
                writer = new ExceptionWriter();
            } else {
                writer = (ExceptionWriter)stack.pop();
            }
        }
        return writer;
    }
    
    //-------------------------------------
    /**
     constructor
    */
    public ExceptionWriter() {
        super();
    }

    /**
        結合行の出力
    */
    public CharArray concat(int index, int size) {
        CharArray ch = CharArray.pop();
        int n = 0;
        for (int i = index; i < size(); i++) {
            ch.add(peek(i));
            if (++n >= size) break;
        }
        return ch;
    }
    /** 指定行を System.out.出力 
        @param index 0-
        @param size  出力行数 1-  
    */
    public void dumpQueue(int index, int size) {
        synchronized (lock) {
            int n = 0;
            for (int i = index; i < size(); i++) {
                System.out.println(peek(i));
                if (++n >= size) break;
            }
        }
    }
    /** 全ての行をSystem.out出力 */
    public void dumpQueue() {
        dumpQueue(0, size());
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
// [end of ExceptionWriter.java]
//

