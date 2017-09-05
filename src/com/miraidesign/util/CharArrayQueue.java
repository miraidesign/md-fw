//------------------------------------------------------------------------
//    CharArrayQueue.java
//                 CharArray の キュー（リングバッファ使用）
//
//     Copyright (c) Mirai Design Institute 2010-2011 All rights reserved.
//          update 2011-01-07   ishiokatoru@miraidesign.com
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------


package com.miraidesign.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/** CharArray の キュー（リングバッファ使用）*/
public  class CharArrayQueue extends Queue {
    protected CharArray array[];   // データ本体

    protected boolean memSavingMode = true;
    
    /*
        メモリ節約モードを有効にする
        デフォルト：有効
    */
    public void setMemSavingMode(boolean mode) {
        memSavingMode = true;
    }
    //--------------------------------
    // コンストラクタ
    //--------------------------------
    /**
        デフォルトサイズ(32)のCharArrayQueueを生成する
    */
    public CharArrayQueue() { this(DEFAULT_SIZE); }
    
    /**
        指定サイズ(32)のCharArrayQueueを生成する
        @param size 初期サイズ
    */
    public CharArrayQueue(int size) {
        if (size <= 0) size = 4;
        type = STRING;
        array = new CharArray[size];
        clear();
    }
    /**
        @param size 初期サイズ
        @param appendMode 追加モード Queue#EXTEND_DATA/DELETE_TOP/NOT_APPEND/WAIT_DATA
    */
    public CharArrayQueue(int size,int appendMode) {
        this(size);
        setAppendMode(appendMode);
    }

    /**
        シリアライズされた入力ストリームから再生成する
        @param in  入力ストリーム
    */
    public CharArrayQueue(DataInput in) {
        this(DEFAULT_SIZE);
        try {
            readObject(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* 
        CharArrayをStackに戻して
        初期状態にする
    */
    public CharArrayQueue reset() {
        for (int i = 0; i < array.length; i++) {
            CharArray ch = array[i];
            if (ch == null) break;
            CharArray.push(ch);
            array[i] = null;
        }
        clear();
        return this;
    }
    /* 呼び出さない事 */
    protected CharArrayQueue reset(int size) {
        for (int i = 0; i < array.length; i++) {
            array[i] = null;
        }
        if (size > 0) array = new CharArray[size];
        else          array = null;
        clear();
        return this;
    
    }

    //--------------------------------
    /*
       参照をコピーする
    */
    public CharArrayQueue copy(CharArrayQueue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(from.peek(i));
        }
        return this;
    }
    /*
       参照をnewしてからコピーする
    */
    public CharArrayQueue copy2(CharArrayQueue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(new CharArray(from.peek(i)));
        }
        return this;
    }

    public CharArrayQueue copy(Queue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(((CharArrayQueue)from).peek(i));
        }
        return this;
    }
    public CharArrayQueue copy2(Queue from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            enqueue(new CharArray(((CharArrayQueue)from).peek(i)));
        }
        return this;
    }
    
    /*
        改行変換してからコピー
    */
    public CharArrayQueue copy(CharArrayFile from) {
        clear();
        CharToken token = CharToken.pop();
        token.set(from,"\n");
        for (int i = 0; i < token.size(); i++) {
            enqueue(new CharArray(token.peek(i)));
        }
        CharToken.push(token);
        return this;
    }
    
    
    public void exchange(CharArrayQueue from) {
        CharArrayQueue tmp = new CharArrayQueue();
        tmp.copy(this);
        this.copy(from);
        from.clear();
        from.copy(tmp);
        tmp = null;
    }
    //--------------------------------
    public boolean isFull() {  return (len >= array.length); }

    //public Enumeration elements() { return new IntQueueEnumerator(this);}

    /** 要素の追加を行う 
        @param str 追加文字列
        @return 追加が成功したか？
    */
    public boolean enqueue(String str) {
        return enqueue(new CharArray(str));
    }
    /** 要素の追加を行う 
        @param object 追加文字列
        @return 追加が成功したか？
    */
    public synchronized boolean enqueue(CharArray object) {
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                    appendMode = DELETE_TOP;
            }
            CharArray newarray[] = new CharArray[size];
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
        CharArray org = array[tail];
        if (org != null && org instanceof CharArray) org.copy(object);
        else  {
            if (memSavingMode) {
                array[tail] = object;
            } else {
                CharArray ch = new CharArray(object);
                array[tail] = ch;
            }
        }
        tail = (tail + 1) % array.length;
        ++len;
        if (len == 1) notify();     // 
        return true;
    }
    /* 要素の追加を行う 
        参照コピーは禁止する
    */
    public synchronized boolean enqueue2(CharArray object) {
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                    appendMode = DELETE_TOP;
            }
            CharArray newarray[] = new CharArray[size];
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
        CharArray org = array[tail];
        if (org != null && org instanceof CharArray) org.copy(object);
        else  {
            if (memSavingMode) {
                array[tail] = new CharArray(object);
            } else {
                CharArray ch = new CharArray(object);
                array[tail] = ch;
            }
        }
        tail = (tail + 1) % array.length;
        ++len;
        if (len == 1) notify();     // 
        return true;
    }
    
    /* 要素の挿入 */
    public synchronized boolean insert(int index, CharArray value) { 
        if (index < 0 || index > len) return false;
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                appendMode = DELETE_TOP;
            }
            CharArray newarray[] = new CharArray[size];
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
                System.out.println("CharArrayQueue insert index error");
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
    /* 要素の挿入 
        参照コピーは行わない
    */
    public synchronized boolean insert2(int index, CharArray value) { 
        if (index < 0 || index > len) return false;
        if (len >= array.length - 1 && appendMode == EXTEND_DATA) {
            int size = array.length * 2;
            if (max_size > 0 && size > max_size) {
                size = max_size;
                appendMode = DELETE_TOP;
            }
            CharArray newarray[] = new CharArray[size];
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
                System.out.println("CharArrayQueue insert index error");
            }
        }
        array[index] = new CharArray(value);
        
        if (len < array.length) {
            tail = (tail + 1) % array.length;
            ++len;
            if (len == 1) notify();
        }
        return true;
    }
    public synchronized boolean insert(int index, String value) { 
        return insert(index, CharArray.pop(value));
    }
    
    /* 取り出し */
    public synchronized CharArray dequeue() throws InterruptedException {
        while (len <= 0) { 
            if (waitMode) wait();
            else         return null;
        }
        CharArray object = array[head];
        head = (head + 1) % array.length;
        --len;
        return object;
    }
    
    /* 指定要素の削除 */
    public synchronized boolean remove(int index) 
                    /*throws InterruptedException*/ {  // 削除
        if (len <= 0 ||index < 0 || index >= len) return false;
        index = (head+index) % array.length;
        CharArray.push(array[index]);
        array[index] = null;
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
                System.out.println("CharArrayQueue remove index error");
            }
            
        }
        if (tail == 0) tail = array.length-1;
        else --tail;
        --len;
        return true;
    }
    
    /* 先頭を覗く */
    public synchronized CharArray peek() {
        if (len <= 0) return null;
        return array[head];
    }

    /* 指定個所を覗く */
    public synchronized CharArray peek(int index) {
        if (len <= 0 || index < 0 || index >= len) return null;
        return array[(head+index)%array.length];
    }
    /* 指定個所に書込 */
    public synchronized boolean poke(int index, CharArray ch) {
        if (len <= 0 || index < 0 || index >= len) {
            return false;
        } else {
            int i = (head+index)%array.length;
            CharArray org = array[i];
            if (org != null && org instanceof CharArray) org.set(ch);
            else             array[i] = ch;
            return true;
        }
    }
    /* 指定個所に書込 
        参照書き込みは行わない
    */
    public synchronized boolean poke2(int index, CharArray ch) {
        if (len <= 0 || index < 0 || index >= len) {
            return false;
        } else {
            int i = (head+index)%array.length;
            CharArray org = array[i];
            if (org != null && org instanceof CharArray) org.set(ch);
            else             array[i] = new CharArray(ch);
            return true;
        }
    }

    /* 要素の入れ替え 
        @param index1 index2と入れ替える
        @param index2 index1と入れ替える
        @return false:パラメータエラー
    */
    public synchronized boolean exchange(int index1, int index2) 
                    /*throws InterruptedException*/ {  // 削除
        if (len <= 0 || index1<0 || index1>=len || index2<0 || index2>=len) {
            return false;
        }
        if (index1 != index2) {
            CharArray value = CharArray.pop(peek(index1));
            poke2(index1, peek(index2));
            poke2(index2, value);
            CharArray.push(value);
        }
        return true;
    }
    public synchronized boolean exchange2(int index1, int index2) 
                    /*throws InterruptedException*/ {  // 削除
        if (len <= 0 || index1<0 || index1>=len || index2<0 || index2>=len) return false;
        if (index1 != index2) {
            CharArray value = new CharArray(peek(index1));
            poke2(index1,peek(index2));
            poke2(index2,value);
        }
        return true;
    }

    /* 内容が同じか？ */
    public boolean equals(CharArrayQueue queue) {
        if (this.size() != queue.size()) return false;
        for (int i = 0; i < this.size(); i++) {
            if (!peek(i).equals(queue.peek(i))) return false;
        }
        return true;
    }

    /** 
        文字列検索 
        @param str 検索文字列
        @return -1:存在しない 0-発見したindex
    */
    public int indexOf(CharArray str) {
        int sts = -1;
        for (int i = 0; i < this.size(); i++) {
            if (peek(i).equals(str)) {
                sts = i;
                break;
            }
        }
        return sts;
    }
    /** 
        文字列検索 
        @param str 検索文字列
        @return -1:存在しない 0-発見したindex
    */
    public int indexOf(String str) {
        int sts = -1;
        for (int i = 0; i < this.size(); i++) {
            if (peek(i).equals(str)) {
                sts = i;
                break;
            }
        }
        return sts;
    }
    
    /*
      各行の前後の空白文字を削除する
    */
    public CharArrayQueue trim() {
        for (int i = 0; i < this.size(); i++) {
            peek(i).trim();
        }
        return this;
    }
    /*
      各行の前後の空白文字を削除し、空白行を削除する
    */
    public CharArrayQueue trimLine() {
        for (int i = 0; i < this.size(); ) {
            if (peek(i).trim().length() == 0) {
                remove(i);
            } else {
                i++;
            }
        }
        return this;
    }
    /**
       全て英小文字にする
    */
    public void toLowerCase() {
        for (int i = 0; i < this.size(); i++) {
            peek(i).toLowerCase();
        }
    }
    /**
       全て英大文字にする
    */
    public void toUpperCase() {
        for (int i = 0; i < this.size(); i++) {
            peek(i).toUpperCase();
        }
    }
    
    /*
      String配列に変換して出力する
    */
    public String[] toStringArray() {
        String[] strs = new String[len];
        for (int i = 0; i < len; i++) {
            CharArray ch = peek(i);
            strs[i] = (ch == null || ch.isNull()) ? null : ch.toString();
        }
        return strs;
    }
    /*
      String配列に変換して出力する
      同時にunicode の特殊文字を変換する
    */
    public String[] toStringArrayToJIS() {
        String[] strs = new String[len];
        CharArray ch = CharArray.pop();
        for (int i = 0; i < len; i++) {
            if (peek(i) == null || peek(i).isNull()) {
                strs[i] = null;
            } else {
                ch.set(peek(i));
                strs[i] = ch.toJIS().toString();
            }
        }
        CharArray.push(ch);
        return strs;
    }
    /*
      Object配列に変換して出力する
    */
    public Object[] toObjectArray() {
        Object[] objs = new Object[len];
        for (int i = 0; i < len; i++) {
            CharArray ch = peek(i);
            objs[i] = (ch == null || ch.isNull()) ? null : ch.toString();
        }
        return objs;
    }
    /*
      Object配列に変換して出力する
      同時にunicode の特殊文字を変換する
    */
    public Object[] toObjectArrayToJIS() {
        Object[] objs = new Object[len];
        CharArray ch = CharArray.pop();
        for (int i = 0; i < len; i++) {
            if (peek(i) == null) {
                objs[i] = null;
            } else {
                ch.set(peek(i));
                objs[i] = ch.toJIS().toString();
            }
        }
        CharArray.push(ch);
        return objs;
    }

    /* 
      バイト配列を返す
      @param encoding 文字列エンコーディング
      @return バイト配列
    */
    public byte[] getBytes(String encoding) throws UnsupportedEncodingException {
        CharArray ch = CharArray.pop();
        for (int i = 0; i < len; i++) {
            ch.add(peek(i));
            ch.add("\n");
        }
    
        byte[] bytes = StringCoding.encode(encoding, ch.chars, 0, ch.length);
        CharArray.push(ch);
        return bytes;
    }

    /** 内容をダンプする（デバッグ用） **/
    public void dumpQueue() {
        dumpQueue(0, size(), "");
    }
    public void dumpQueue(String pre) {
        dumpQueue(0, size(), pre);
    }
    public void dumpQueue(int index, int size) {
        dumpQueue(index, size, "");
    }
    public void dumpQueue(int index, int size, String pre) {
        int n = 0;
        if (pre == null) pre = "";
        for (int i = index; i < size(); i++) {
            System.out.println(pre+"["+i+"]"+peek(i));
            if (++n >= size) break;
        }
    }
    public CharArray dumpQueue(int index, int size, CharArray ch) {
        if (ch == null) ch = new CharArray();
        int n = 0;
        for (int i = index; i < size(); i++) {
            ch.add("["+i+"]"+peek(i)+"\n");
            if (++n >= size) break;
        }
        return ch;
    }
    /**
        利用メモリサイズを取得する
        @return オブジェクトの利用メモリサイズ
    */
    public int sizeOf() {
        int size = 0;
        for (int i = 0; i < array.length; i++) {
            CharArray ch = array[i];
            if (ch != null) size += ch.sizeOf();
        }
        return size;
    }
    /**
        合計値を取得する
        @return 合計値
    */
    public long total() {
        long total = 0;
        for (int i = 0; i < len; i++) {
            total += CharArray.getLong(peek(i));
        }
        return total;
    }

    /**
        文字列結合する
        @param sep 結合文字
        @return  新しいCharArrayを変える
    */
    public CharArray concat(char sep) {
        CharArray ch = new CharArray();
        for (int i = 0; i < len; i++) {
            if (i > 0) ch.add(sep);
            ch.add(peek(i));
        }
        return ch;
    }
    /**
        文字列結合する
        @param sep 結合文字列
        @return  新しいCharArrayを変える
    */
    public CharArray concat(String sep) {
        CharArray ch = new CharArray();
        for (int i = 0; i < len; i++) {
            if (i > 0) ch.add(sep);
            ch.add(peek(i));
        }
        return ch;
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
            reset();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                CharArray ch = CharArray.pop();
                ch.readObject(in);
                enqueue(ch);
            }
        }
    }
}

//
// [end of CharArrayQueue.java]
//

