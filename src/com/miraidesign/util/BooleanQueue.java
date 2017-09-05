//------------------------------------------------------------------------
//    BooleanQueue.java
//                 boolean の キュー（リングバッファ使用）
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-04   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** boolean データ の キュー（リングバッファ使用）**/
public  class BooleanQueue extends Queue{
    protected boolean array[];   // データ本体

    //--------------------------------
    // コンストラクタ
    //--------------------------------
    /** constructor */
    public BooleanQueue() { this(DEFAULT_SIZE); }
    
    /** constructor */
    public BooleanQueue(int size) {
        type = BOOL;
        array = new boolean[size];
        clear();
    }
    
    /** constructor */
    public BooleanQueue(int size,int appendMode) {
        this(size);
        setAppendMode(appendMode);
    }

    public BooleanQueue(DataInput in) {
        this(DEFAULT_SIZE);
        try {
            readObject(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //--------------------------------
    /** copy 
        @param from コピー元
    */
    public BooleanQueue copy(BooleanQueue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(from.peek(i));
        }
        return this;
    }
    /** copy 
        @param from コピー元
    */
    public BooleanQueue copy(Queue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(((BooleanQueue)from).peek(i));
        }
        return this;
    }
    //--------------------------------
    /** 
        バッファがいっぱいか？
        @return バッファ最大
    */
    public boolean isFull() {  return (len >= array.length); }

    //public Enumeration elements() { return new IntQueueEnumerator(this);}

    /** 要素の追加を行う 
        @param object 追加オブジェクト
        @return true
    */
    public synchronized boolean enqueue(boolean object) { 
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                    appendMode = DELETE_TOP;
            }
            boolean newarray[] = new boolean[size];
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
    
    /** 要素の挿入 
        @param index 挿入位置
        @param value 挿入する値
        @return true を返す
    */
    public synchronized boolean insert(int index, boolean value) { 
        if (index < 0 || index > len) return false;
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                appendMode = DELETE_TOP;
            }
            boolean newarray[] = new boolean[size];
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
                System.out.println("BooleanQueue insert index error");
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
    
    /** 要素の取得
        @return 取得した値
    */
    public synchronized boolean dequeue() throws InterruptedException {
        while (len <= 0) { 
            if (waitMode) wait();
            else         return false;
        }
        boolean object = array[head];
        head = (head + 1) % array.length;
        --len;
        return object;
    }
    
    /** 指定要素の削除 
        @param index 削除位置
    */
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
                System.out.println("BooleanQueue remove index error");
            }
            
        }
        if (tail == 0) tail = array.length-1;
        else --tail;
        --len;
        return true;
    }
    
    
    /** 先頭を覗く 
        @return 先頭の値
    */
    public synchronized boolean peek() {
        if (len <= 0) return false;        //null;
        return array[head];
    }

    /** 指定個所を覗く 
        @return 指定個所の値
    */
    public synchronized boolean peek(int index) {
        if (len <= 0 || index < 0 || index >= len) return false;    //null;
        return array[(head+index)%array.length];
    }
    /* 1 または 0 で返す */
    public int getInt(int index) {
        return peek(index) ? 1 : 0;
    }

    /** 指定個所に書き込み 
        @param index 書き込み位置
        @param b     書き込み値
        @return true: 書き込み成功 false:パラメータエラー
    */
    public synchronized boolean poke(int index, boolean b) {
        if (len <= 0 || index < 0 || index >= len) {
            return false;
        } else {
            array[(head+index)%array.length] = b;
            return true;
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
            boolean value = peek(index1);
            poke(index1,peek(index2));
            poke(index2,value);
        }
        return true;
    }
    
    /** 内容が同じか？ 
        @param queue 比較先の BooleanQueue
        @return true 同一データ
    */
    public boolean equals(BooleanQueue queue) {
        if (this.size() != queue.size()) return false;
        for (int i = 0; i < this.size(); i++) {
            if (peek(i) != queue.peek(i)) return false;
        }
        return true;
    }
    /**
        利用メモリサイズを取得する
    */
    public int sizeOf() {
        int size = array.length;
        return size;
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            out.writeInt(len);
            for (int i = 0; i < len; i++) {
                out.writeBoolean(peek(i));
            }
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            clear();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                enqueue(in.readBoolean());
            }
        }
    }

}


//
// [end of BooleanQueue.java]
//

