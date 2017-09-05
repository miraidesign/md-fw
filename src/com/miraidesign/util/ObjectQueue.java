//------------------------------------------------------------------------
//    ObjectQueue.java
//                 キュー（リングバッファ使用）
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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/** キュー（リングバッファ使用）**/ 
//public  class ObjectQueue<T> extends Queue {
public  class ObjectQueue extends Queue {
    //protected T array[];   // データ本体
    protected Object array[];   // データ本体

    protected int max = 0;
    //--------------------------------
    // コンストラクタ
    //--------------------------------
    public ObjectQueue() { this(DEFAULT_SIZE); }
    
    public ObjectQueue(int size) {
        type = OBJECT;
        array = new Object[size];
        clear();
    }
    
    public ObjectQueue(int size,int appendMode) {
        this(size);
        setAppendMode(appendMode);
    }
    
    public ObjectQueue(DataInput in) {
        this(DEFAULT_SIZE);
        try {
            readObject(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public ObjectQueue(ObjectQueue from) {
        this();
        copy(from);
    }
    //--------------------------------
    public ObjectQueue copy(ObjectQueue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(from.peek(i));
        }
        return this;
    }
    public ObjectQueue copy(Queue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(((ObjectQueue)from).peek(i));
        }
        return this;
    }
    //--------------------------------
    public boolean isFull() {  return (len >= array.length); }

    public Enumeration elements() { return new QueueEnumerator(this);}

    /** 要素の追加を行う */
    public synchronized boolean enqueue(Object object) {
    //public synchronized boolean enqueue(T object) {
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                    appendMode = DELETE_TOP;
            }
            //Object newarray[] = new Object[size];
            Object newarray[] = new Object[size];
            System.arraycopy(array, head, newarray, 0, array.length - head);
            System.arraycopy(array,    0, newarray,array.length -head, head);
            head = 0;
            tail = array.length - 1;
            array = newarray;
        } else if (len >= array.length) {
            switch (appendMode) {
                /*
                case EXTEND_DATA:       // 配列を拡張する
                    int size = array.length * 2;
                    if (max_size > 0 && size > max_size) {
                        size = max_size;
                        appendMode = DELETE_TOP;
                    }
                    Object newarray[] = new Object[size];
                    System.arraycopy(array, head, newarray, 0, array.length - head);
                    System.arraycopy(array,    0, newarray,array.length -head, head);
                    head = 0;
                    tail = array.length - 1;
                    array = newarray;
                    break;
                */
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
        max = Math.max(max,len);
        if (len == 1) notify();     // 
        return true;
    }

    /** 要素の挿入 */
    public synchronized boolean insert(int index, Object value) { 
    //public synchronized boolean insert(int index, T value) { 
        if (index < 0 || index > len) return false;
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                appendMode = DELETE_TOP;
            }
            Object newarray[] = new Object[size];
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
                System.out.println("ObjectQueue insert index error");
            }
        }
        array[index] = value;
        
        if (len < array.length) {
            tail = (tail + 1) % array.length;
            ++len;
            if (len == 1) notify();
        }
        max = Math.max(max,len);
        return true;
    }


    
    /** 要素の取り出し **/
    public synchronized Object dequeue() throws InterruptedException {
    //public synchronized T dequeue() throws InterruptedException {
        while (len <= 0) { 
            if (waitMode) wait();
            else         return null;
        }
        Object object = array[head];
        array[head] = null;     // 2010-03-11
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
                System.out.println("ObjectQueue remove index error");
            }
            
        }
        if (tail == 0) tail = array.length-1;
        else --tail;
        --len;
        return true;
    }

    /** 先頭を覗く **/
    public synchronized Object peek() {
    //public synchronized T peek() {
        if (len <= 0) return null;
        return array[head];
    }

    /** 指定個所を覗く **/
    public synchronized Object peek(int index) {
    //public synchronized T peek(int index) {
        if (len <= 0 || index < 0 || index >= len) return null;
        return array[(head+index)%array.length];
    }

    /** 指定個所に書き込む  **/
    public synchronized boolean poke(int index,Object o) {
    //public synchronized boolean poke(int index, T o) {
        if (len <= 0 || index < 0 || index >= len) {
            return false;
        } else {
            array[(head+index)%array.length] = o;
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
            Object value = peek(index1);
            //T value = peek(index1);
            poke(index1,peek(index2));
            poke(index2,value);
        }
        return true;
    }
    
    /**
        検索(遅い)
        @param object null は検索しない
        @return 検索index：-1で存在しない
    */
    public int find(Object object) {
        int index = -1;
        if (object != null) {
            for (int i = 0; i < len; i++) {
                if (object.equals(array[i])) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
    
    /**
        利用メモリサイズを取得する
    */
    public int sizeOf() {
        int size = array.length * 4;
        for (int i = 0; i < array.length; i++) {
            Object obj = array[i];
            if (obj != null) {
                if (obj instanceof HashParameter) size += ((HashParameter)obj).sizeOf();
                else if (obj instanceof CharArrayQueue) size += ((CharArrayQueue)obj).sizeOf();
                //else size += sizeOf(obj);  未実装
            }
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
                //DataReadWrite obj = (DataReadWrite)peek(i);
                //obj.writeObject(out);
            }
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            clear();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                // うーんこれはだめ 型がわからないと、、
                //enqueue(in.readObject());
            }
        }
    }

}


final
class QueueEnumerator implements Enumeration {
    ObjectQueue queue;
    int count;

    QueueEnumerator(ObjectQueue q){
        queue = q;
        count = 0;
    }

    public boolean hasMoreElements(){
        return count < queue.len;
    }

    public Object nextElement(){
        synchronized (queue) {
            if (count < queue.len) {
                return queue.array[(queue.head+count++)%queue.array.length];
            }
        }
        throw new NoSuchElementException("QueueEnumerator");
    }
    

}


//
// [end of ObjectQueue.java]
//

