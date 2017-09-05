//------------------------------------------------------------------------
//    LongQueue.java
//         long の キュー（リングバッファ使用）
// 
//          Copyright (c) Mirai Design Institute 2010-14 All rights reserved.
//          update 2014-02-18   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** long の キュー（リングバッファ使用）**/
public  class LongQueue extends Queue {
    protected long array[];   // データ本体

    //--------------------------------
    // コンストラクタ
    //--------------------------------
    public LongQueue() { this(DEFAULT_SIZE); }
    
    public LongQueue(int size) {
        type = LONG;
        array = new long[size];
        clear();
    }
    
    public LongQueue(int size,int appendMode) {
        this(size);
        setAppendMode(appendMode);
    }

    public LongQueue(DataInput in) {
        this(DEFAULT_SIZE);
        try {
            readObject(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    //--------------------------------
    public LongQueue copy(LongQueue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(from.peek(i));
        }
        return this;
    }
    public LongQueue copy(Queue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(((LongQueue)from).peek(i));
        }
        return this;
    }
    //--------------------------------
    public boolean isFull() {  return (len >= array.length); }

    //public Enumeration elements() { return new IntQueueEnumerator(this);}


    /**  要素の追加 */
    public synchronized boolean enqueue(int object) {
        return enqueue((long)object);
    }
    /**  要素の追加 */
    public synchronized boolean enqueue(long object) {
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                    appendMode = DELETE_TOP;
            }
            long newarray[] = new long[size];
            System.arraycopy(array, head, newarray, 0, array.length - head);
            System.arraycopy(array,    0, newarray,array.length -head, head);
            head = 0;
            tail = array.length - 1;
            array = newarray;
        } else if (len >= array.length) {
            switch (appendMode) {
                case DELETE_TOP:        // 先頭データを捨てる
                    if (len > 0) {
                        head = (head + 1) % array.length;
                        --len; ++over_size;
                    }
                    break;
                case NOT_APPEND:        // 追加データを捨てる
                    return true;
                case WAIT_DATA:         //: 追加できるまで待つ
                    return false;       //  呼び出し側でリトライする事
            } // endcase
        }
        array[tail] = object;
        tail = (tail + 1) % array.length;
        ++len;
        if (len == 1) notify();     // 
        return true;
    }
    
    /** 要素の挿入 */
    public synchronized boolean insert(int index, long value) { 
        if (index < 0 || index > len) return false;
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                appendMode = DELETE_TOP;
            }
            long newarray[] = new long[size];
            System.arraycopy(array, head, newarray, 0, array.length - head);
            System.arraycopy(array,    0, newarray,array.length -head, head);
            head = 0;
            tail = array.length - 1;
            array = newarray;
        } else if (len >= array.length) {
            
        }
        index = (head+index) % array.length;
        if (head < tail) {
            System.arraycopy(array, index, array, index+1, tail - index);
        } else {
            if (index < tail) {
                System.arraycopy(array, index, array, index+1, tail - index-1);
            } else if (index >= head) {
                //System.out.println("insert("+index+") C");
                if (tail > 0) {
                    System.arraycopy(array, 0, array, 1, tail-1);
                }
                System.arraycopy(array, index, array, index+1, array.length - index-1);
                array[0] = array[array.length-1];
            } else {
                System.out.println("LongQueue insert index error");
            }
        }
        array[index] = value;
        
        if (len < array.length) {
            tail = (tail + 1) % array.length;
            ++len;
            if (len == 1) notify();
        }
        return true;
    }
    
    
    /** 取り出し */
    public synchronized long dequeue() throws InterruptedException {
        while (len <= 0) { 
            if (waitMode) wait();
            else         return 0;
        }
        long object = array[head];
        head = (head + 1) % array.length;
        --len;
        return object;
    }
    
    /** 指定要素の削除 */
    public synchronized boolean remove(int index) 
                    /*throws InterruptedException*/ {  // 削除
        if (len <= 0 ||index < 0 || index >= len) return false;
        index = (head+index) % array.length;
        if (head < tail) {
            System.arraycopy(array, index+1, array, index, tail - index);
        } else {
            if (index < tail) {
                System.arraycopy(array, index+1, array, index, tail - index);
            } else if (index >= head) {
                System.arraycopy(array, index+1, array, index, array.length - index-1);
                array[array.length-1] = array[0];
                if (tail > 0) System.arraycopy(array, 1, array, 0, tail-1);
            } else {
                System.out.println("LongQueue remove index error");
            }
            
        }
        if (tail == 0) tail = array.length-1;
        else --tail;
        --len;
        return true;
    }
    
    /** 先頭を覗く */
    public synchronized long peek() {
        if (len <= 0) return 0;        //null;
        return array[head];
    }

    /** 指定個所を覗く */
    public synchronized long peek(int index) {
        if (len <= 0 || index < 0 || index >= len) return 0;    //null;
        return array[(head+index)%array.length];
    }
    /** 指定個所に書込 */
    public synchronized boolean poke(int index, long l) {
        if (len <= 0 || index < 0 || index >= len) {
            return false;
        } else {
            array[(head+index)%array.length] = l;
            return true;
        }
    }
    /** 指定要素をインクリメントする 
        @param index
    */
    public synchronized boolean increment(int index) {
        if (len <= 0 || index < 0 || index >= len) return false;
        ++array[(head+index)%array.length];
        return true;
    }
    /** 指定要素をデクリメントする 
        @param index
    */
    public synchronized boolean decrement(int index) {
        if (len <= 0 || index < 0 || index >= len) return false;
        --array[(head+index)%array.length];
        return true;
    }

    /* 指定数値でクリアする */
    public synchronized void clear(long num) {
        for (int i = 0; i < size(); i++) {
            array[(head+i)%array.length] = num;
        }
    }
    /* 指定数値でクリアする */
    public synchronized void clear(int num) {
        for (int i = 0; i < size(); i++) {
            array[(head+i)%array.length] = (long)num;
        }
    }

    /** 要素の入れ替え 
        @param index1 index2と入れ替える
        @param index2 index1と入れ替える
        @return false:パラメータエラー
    */
    public synchronized boolean exchange(int index1, int index2) 
                    /*throws InterruptedException*/ {  // 削除
        if (len <= 0 || index1<0 || index1>=len || index2<0 || index2>=len) return false;
        if (index1 != index2) {
            long value = peek(index1);
            poke(index1,peek(index2));
            poke(index2,value);
        }
        return true;
    }

    /** 内容が同じか？ */
    public boolean equals(LongQueue queue) {
        if (this.size() != queue.size()) return false;
        for (int i = 0; i < this.size(); i++) {
            if (peek(i) != queue.peek(i)) return false;
        }
        return true;
    }

    /** 検索する */
    public long indexOf(long no) {
        for (int i = 0; i < this.size(); i++) {
            if (peek(i) == no) return i;
        }
        return -1;
    }
    /** 検索する */
    public boolean find(long no) {
        for (int i = 0; i < this.size(); i++) {
            if (peek(i) == no) return true;
        }
        return false;
    }
    
    /** 
        カンマ区切り文字列から追加する<br>
        先頭、最後の {} [] 等があれば削除する
    */
    public LongQueue addFromCSV(String str) {
        return addFromCSV(CharArray.pop(str));
    }
    /** 
        カンマ区切り文字列から追加する<br>
        先頭、最後の {} [] 等があれば削除する
    */
    public LongQueue addFromCSV(CharArray ch) {
        do {
            if (ch == null || ch.trim().length() == 0) break;
            while (ch.startsWith("{")) ch.remove(0,1);
            while (ch.startsWith("[")) ch.remove(0,1);
            while (ch.endsWith("}")) ch.length--;
            while (ch.endsWith("]")) ch.length--;
            if (ch.length() == 0) break;
            CharToken token = CharToken.pop();
            token.set(ch,",");
            for (int i = 0; i < token.size(); i++) {
                enqueue(CharArray.getLong(token.get(i)));
            }
            CharToken.push(token);
        } while (false);
        return this;
    }
    /**
        利用メモリサイズを取得する
    */
    public int sizeOf() {
        int size = array.length * 8;
        return size;
    }
    /**
        合計値を取得する
    */
    public long total() {
        long total = 0;
        for (int i = 0; i < len; i++) {
            total += peek(i);
        }
        return total;
    }
    
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            out.writeInt(len);
            for (int i = 0; i < len; i++) {
                out.writeLong(peek(i));
            }
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            clear();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                enqueue(in.readLong());
            }
        }
    }
    
    //----------------------
    public void debugDisp() {
        System.out.println("debugDisp===========================");
        System.out.println("head="+head+" tail="+tail+" len="+len+" array="+array.length);
        CharArray ch = new CharArray();
        ch.setAppendMode(false);
        for (int i = 0; i < array.length; i++) {
            System.out.print(ch.format(i,10,4)+":");
        }
        System.out.println("");
        for (int i = 0; i < array.length; i++) {
            System.out.print(ch.format(array[i],10,4)+":");
        }
        System.out.println("");
    }

    /** 内容をダンプする（デバッグ用） **/
    public void dumpQueue() {
        dumpQueue(0, size());
    }
    /** 内容をダンプする（デバッグ用） 
        @param index 開始位置 0-
        @param size  サイズ
    **/
    public void dumpQueue(int index, int size) {
        int n = 0;
        for (int i = index; i < size(); i++) {
            System.out.println("["+i+"]"+peek(i));
            if (++n >= size) break;
        }
    }
    /** 内容をダンプする（デバッグ用） 
        @param index 開始位置 0-
        @param size  サイズ
        @param ch    出力バッファ
    **/
    public CharArray dumpQueue(int index, int size, CharArray ch) {
        if (ch == null) ch = new CharArray();
        int n = 0;
        for (int i = index; i < size(); i++) {
            ch.add("["+i+"]"+peek(i)+"\n");
            if (++n >= size) break;
        }
        return ch;
    }
    

}


//
// [end of LongQueue.java]
//

