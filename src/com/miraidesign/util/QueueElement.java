//------------------------------------------------------------------------
//    QueueElement.java                                                       
//          複数の情報（Element）等をCharArrayQueueで管理
//
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-04   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------

package com.miraidesign.util;

/**  CharArrayQueue で情報の集合を管理 */
public class QueueElement extends CharArrayQueue {
    static final boolean debug = false;
    boolean convert = false;

    public QueueElement() {}
    /**
        @param convert タグ文字変換を行う
    */
    public QueueElement(boolean convert) {
        this.convert = convert;
    }
    public QueueElement(int size) {
        super(size);
        for (int i = 0; i < size; i++) {
            add("");
        }
    }
    
    /** 空要素の追加 */
    public QueueElement add() {
        enqueue("");
        return this;
    }
    /** 要素の追加 
        @param value 追加する要素
    */
    /*
    public QueueElement add(String value) {
        if (convert && value != null) enqueue(CharArray.replaceTag(value));
        else         enqueue(value);
        return this;
    }
    public QueueElement add(String[] values) {
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (convert) enqueue(CharArray.replaceTag(values[i]));
                else         enqueue(values[i]);
            }
        }
        return this;
    }
    public QueueElement add(CharArray value) {
        enqueue((convert && value != null)? CharArray.replaceTag(value) : new CharArray(value));
        return this;
    }
    public QueueElement add(CharArray[] values) {
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (convert) enqueue(CharArray.replaceTag(values[i]));
                else         enqueue(values[i]);
            }
        }
        return this;
    }
    */
    public QueueElement add(CharSequence value) {
        enqueue((convert && value != null)? CharArray.replaceTag(value) : new CharArray(value));
        return this;
    }
    public QueueElement add(CharSequence[] values) {
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (convert) enqueue(CharArray.replaceTag(values[i]));
                else         enqueue(values[i]);
            }
        }
        return this;
    }
    /**
        @param value 追加するデータ
        @param mode  true: タグ文字の変換を行う
    */
    public QueueElement add(CharArray value, boolean mode) {
        enqueue((convert || mode) ? CharArray.replaceTag(value) : new CharArray(value));
        return this;
    }
    
    /**
        @param value 追加するデータ
        @param mode  true: タグ文字の変換を行う
    */
    public QueueElement add(String value, boolean mode) {
        if (convert || mode) enqueue(CharArray.replaceTag(value));
        else                 enqueue(value);
        return this;
    }
    
    public QueueElement add(CharArrayQueue values) {
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                if (convert) enqueue(CharArray.replaceTag(values.peek(i)));
                else         enqueue(new CharArray(values.peek(i)));
            }
        }
        return this;
    }
    public QueueElement add(int value) {
        CharArray ch = new CharArray().format(value);
        ch.type = Queue.INT;
        enqueue(ch);
        return this;
    }
    public QueueElement add(int[] values) {
        if (values != null) {
            for (int i = 0; i < values.length; i++) add(values[i]);
        }
        return this;
    }
    public QueueElement add(long value) {
        CharArray ch = new CharArray().format(value);
        ch.type = Queue.LONG;
        enqueue(ch);
        return this;
    }
    public QueueElement add(long[] values) {
        if (values != null) {
            for (int i = 0; i < values.length; i++) add(values[i]);
        }
        return this;
    }
    public QueueElement add(double value) {
        CharArray ch = new CharArray(""+value);
        ch.type = Queue.DOUBLE;
        enqueue(ch);
        return this;
    }
    public QueueElement add(boolean value) {
        CharArray ch = new CharArray(value ? "1" : "0");
        ch.type = Queue.BOOL;
        enqueue(ch);
        return this;
    }
    
    /** 要素の変更 
        @param index 変更位置 (0-
        @param value 新しい値
        @return true で変更成功
    */
    public boolean set(int index, CharArray value) {
        CharArray ch = peek(index);
        if (ch == null) return false;
        ch.set(convert ? CharArray.replaceTag(value) : value);
        return true;
    }
    public boolean set(int index, String value) {
        CharArray ch = peek(index);
        if (ch == null) return false;
        if (convert) ch.set( CharArray.replaceTag(value));
        else         ch.set(value);
        return true;
    }
    public boolean set(int index, int value) {
        CharArray ch = peek(index);
        if (ch == null) return false;
        ch.clear();
        ch.format(value);
        return true;
    }
    public boolean set(int index, long value) {
        CharArray ch = peek(index);
        if (ch == null) return false;
        ch.clear();
        ch.format(value);
        return true;
    }
    public boolean set(int index, boolean value) {
        CharArray ch = peek(index);
        if (ch == null) return false;
        ch.set(value ? "1" : "0");
        return true;
    }
    public boolean set(int index, double value) {
        CharArray ch = peek(index);
        if (ch == null) return false;
        ch.set(""+value);
        return true;
    }
    
    /** 要素の挿入 */
    //public boolean insert(CharArray value) {
    //    return insert(0,value);
    //}
    //public boolean insert(String value) {
    //    return insert(0,value);
    //}
    public boolean insert(int index, int value) {
        CharArray ch = new CharArray();
        ch.format(value);
        return insert(index,ch);
    }
    public boolean insert(int index, long value) {
        CharArray ch = new CharArray();
        ch.format(value);
        return insert(index,ch);
    }
    public boolean insert(int index, boolean value) {
        CharArray ch = new CharArray();
        ch.set(value ? "1" : "0");
        return insert(index,ch);
    }
    public boolean insert(int index, double value) {
        CharArray ch = new CharArray();
        ch.set(""+value);
        return insert(index,ch);
    }
    
    
    /** 要素の取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public CharArray get(int index) {
        return peek(index);
    }
    /** 要素の取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public CharArray getCharArray(int index) {
        return peek(index);
    }
    /** int型要素の取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public int getInt(int index) {
        CharArray ch = peek(index);
        if (ch == null) return 0;
        return ch.getInt();
    }
    /** long型要素の取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public long getLong(int index) {
        CharArray ch = peek(index);
        if (ch == null) return 0;
        return ch.getLong();
    }
    /** boolean型要素の取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public boolean getBoolean(int index) {
        CharArray ch = peek(index);
        if (ch == null) return false;
        return ch.getBoolean();
    }
    /** double型要素の取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public double getDouble(int index) {
        CharArray ch = peek(index);
        if (ch == null) return 0;
        return ch.getDouble();
    }
    
}

//
// [end of QueueElement.java]
//
