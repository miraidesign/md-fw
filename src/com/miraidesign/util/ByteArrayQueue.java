//------------------------------------------------------------------------
//    ByteArrayQueue.java
//                 ByteArray の キュー（リングバッファ使用）
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


/** ByteArray の キュー（リングバッファ使用）**/
public  class ByteArrayQueue extends Queue {
    protected ByteArray array[];   // データ本体

    //--------------------------------
    // コンストラクタ
    //--------------------------------
    /** default constructor */
    public ByteArrayQueue() { this(DEFAULT_SIZE); }
    
    /** copy constructor */
    public ByteArrayQueue(ByteArrayQueue from) { 
        this(DEFAULT_SIZE); 
        copy(from);
    }
    
    /** 
        constructer
        @param size 初期サイズ指定
    */
    public ByteArrayQueue(int size) {
        if (size <= 0) size = 4;
        type = BYTES;
        array = new ByteArray[size];
        clear();
    }
    
    /** 
        constructer
        @param size 初期サイズ指定
        @param appendMode 追加モード
    */
    public ByteArrayQueue(int size,int appendMode) {
        this(size);
        setAppendMode(appendMode);
    }
    
    /**
        データストリームから生成する
    */
    public ByteArrayQueue(DataInput in) {
        this(DEFAULT_SIZE);
        try {
            readObject(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //--------------------------------
    /**
        copy
        @param from コピー元
    */
    public ByteArrayQueue copy(ByteArrayQueue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(from.peek(i));
        }
        return this;
    }

    /**
        copy
        @param from コピー元
    */
    public ByteArrayQueue copy(Queue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(((ByteArrayQueue)from).peek(i));
        }
        return this;
    }
    //--------------------------------
    /** データフルか ？
        @return true:データフル
    */
    public boolean isFull() {  return (len >= array.length); }

    //public Enumeration elements() { return new IntQueueEnumerator(this);}

    /** 要素の追加を行う 
        @param by 追加データ
        @return true：追加成功
    */
    public boolean enqueue(byte[] by) {
        return enqueue(new ByteArray(by));
    }
    /** 要素の追加を行う
        @param object ByteArray型追加データ
        @return true：追加成功
     */
    public synchronized boolean enqueue(ByteArray object) {
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                    appendMode = DELETE_TOP;
            }
            ByteArray newarray[] = new ByteArray[size];
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
        ByteArray org = array[tail];
        if (org != null && org instanceof ByteArray) org.copy(object);
        else  array[tail] = object;
        tail = (tail + 1) % array.length;
        ++len;
        if (len == 1) notify();     // 
        return true;
    }
    
    /** 要素の挿入 
        @param index 挿入位置
        @param value 挿入データ
        @return true: 挿入成功
    */
    public synchronized boolean insert(int index, ByteArray value) { 
        if (index < 0 || index > len) return false;
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                appendMode = DELETE_TOP;
            }
            ByteArray newarray[] = new ByteArray[size];
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
                System.out.println("ByteArrayQueue insert index error");
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
    
    /** 
        データ取り出し
        @return 取得データ ：存在しない場合はnullを返す
    */
    public synchronized ByteArray dequeue() throws InterruptedException {
        while (len <= 0) { 
            if (waitMode) wait();
            else         return null;
        }
        ByteArray object = array[head];
        head = (head + 1) % array.length;
        --len;
        return object;
    }
    
    /** 
        指定要素の削除 
        @param index 削除インデックス
        @return true: 成功
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
                System.out.println("ByteArrayQueue remove index error");
            }
            
        }
        if (tail == 0) tail = array.length-1;
        else --tail;
        --len;
        return true;
    }
    
    /** 
        先頭を覗く 
        @return 参照データ
    */
    public synchronized ByteArray peek() {
        if (len <= 0) return null;
        return array[head];
    }

    /** 
        指定個所を覗く 
        @param index 指定位置
        @return 参照データ
    */
    public synchronized ByteArray peek(int index) {
        if (len <= 0 || index < 0 || index >= len) return null;
        return array[(head+index)%array.length];
    }
    /** 
        指定個所に書込 
        @param index 指定位置
        @param ch    書き込みデータ
        @return true: 書き込み成功
    */
    public synchronized boolean poke(int index, ByteArray ch) {
        if (len <= 0 || index < 0 || index >= len) {
            return false;
        } else {
            int i = (head+index)%array.length;
            ByteArray org = array[i];
            if (org != null && org instanceof ByteArray) org.set(ch);
            else             array[i] = ch;
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
            ByteArray value = peek(index1);
            poke(index1,peek(index2));
            poke(index2,value);
        }
        return true;
    }
    /**
        利用メモリサイズを取得する
    */
    public int sizeOf() {
        int size = 0;
        for (int i = 0; i < array.length; i++) {
            ByteArray by = array[i];
            if (by != null) size += by.bytes.length;
        }
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
                peek(i).writeObject(out);
            }
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            clear();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                ByteArray ch = new ByteArray();
                ch.readObject(in);
                enqueue(ch);
            }
        }
    }

}

//
//
// [end of ByteArrayQueue.java]
//

