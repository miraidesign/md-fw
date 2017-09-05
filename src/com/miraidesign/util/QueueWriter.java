//------------------------------------------------------------------------
//    QueueWriter.java
//                 CharArrayQueue出力するPrintWriter
//                 ※ PrintWrite自身は使用しない
//                      (printStackTrace のインタフェースとしてのみ使用)
//                 Copyright (c) MIrai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.io.Writer;
import java.io.PrintWriter;

/** CharArrayQueue出力するPrintWriter */
public class QueueWriter extends PrintWriter {
    protected CharArrayQueue queue;

    public QueueWriter() {
        super(new NullWriter());
        queue = new CharArrayQueue();
        
    }
    public CharArrayQueue getQueue() { return queue; }
    
    // 
    public void clear() { queue.clear(); }
    public QueueWriter copy(CharArrayQueue from) { queue.copy(from); return this;}
    public QueueWriter copy2(CharArrayQueue from) { queue.copy2(from); return this;}
    public boolean enqueue(String str) { return queue.enqueue(str); }
    public boolean enqueue(CharArray ch) { return queue.enqueue(ch); }
    public boolean insert(int index, CharArray value) { 
        return queue.insert(index, value);
    }
    public CharArray dequeue() throws InterruptedException {
        return queue.dequeue();
    }
    
    /** サイズを返す */
    public int size() { return queue.size(); }
    
    /** 先頭を覗く */
    public CharArray peek() { return queue.peek(); }

    /** 指定個所を覗く */
    public CharArray peek(int index) { return queue.peek(index); }
    /** 指定個所に書込 */
    public boolean poke(int index, CharArray ch) { return queue.poke(index,ch); }

    /** 要素の入れ替え 
        @param index1 index2と入れ替える
        @param index2 index1と入れ替える
        @return false:パラメータエラー
    */
    public boolean exchange(int index1, int index2) 
                    /*throws InterruptedException*/ {  // 削除
        return queue.exchange(index1,index2);
    }
    
    public String[] toStringArray() { return queue.toStringArray(); }
    public String[] toStringArrayToJIS() { return queue.toStringArrayToJIS(); }
    
    
    /** あとで考える */
    public void disp() {
        synchronized (lock) {
            for (int i = 0; i < queue.size(); i++) {
                System.out.println(queue.peek(i));
            }
        }
    }
    
    public void println(String str) { queue.enqueue(str);}
    public void println(CharArray ch) { queue.enqueue(ch);}
    
    /**
        Throwable.printStackTrace(PrintWriter)用のインタフェース
    */
    public void println(char[] c) {
        queue.enqueue(new CharArray(c));
    }
}

class NullWriter extends Writer { // 何もしないWriter
    public void close() {}
    public void flush() {}
    public void write(char[] cbuf, int off, int len) {}
}


//
//
// [end of QueueWriter.java]
//

