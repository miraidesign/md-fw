//------------------------------------------------------------------------
//    HashVector.java
//                 Vectorインターフェースを持つハッシュテーブル
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-04   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
 *  Vectorインターフェースを持つハッシュテーブル<br>
 *  Emurationは登録順に返します
 */
public class HashVector<K,V> extends Hashtable<K,V> {

    //----------------------------------------
    private Vector<K> keys  = new Vector<K>();
    private Vector<V> values = new Vector<V>();

    /** constructor */
    public  HashVector() { }
    /** copy constructor */
    public  HashVector(HashVector<K,V> from) {
        copy(from);
    }
    /** copy */
    public void copy(HashVector<K,V> from) {
        clear();
        int size = from.size();
        for (int i = 0; i < size; i++) {
            K key =   from.keys.get(i);
            V value = from.values.get(i);
            put(key,value);
        }
    }
    //--------------------------------------
    // Hashtable インターフェース
    //-------------------------------------
    /** データを全てクリアします */
    public synchronized void clear() {
        super.clear();      // Hashtable
        keys.clear();       // Vector
        values.clear();     // Vector
    }
    /** キー値の集合を返します
        @return キー値の集合
    */
    public synchronized Enumeration<K> keys() {
        return keys.elements();
    }
    /** データの集合を返します
        @return データ集合
    */
    public synchronized Enumeration<V> elements() {
        return values.elements();
    }
    /** 指定キーのデータを取得します
        @param key キー値
        @return データ
    */
    public synchronized V get(Object key) {
        if (key==null) return null;
        return super.get(key);
    }
    /**
        指定キーとデータのセットを保管します<br>
        同一キーがあった場合は削除されます。
        @param key キー値
        @param value データ
        @return セットされたデータ
    */
    public synchronized V put(K key, V value) {
        remove(key); 
        keys.add(key);
        values.add(value);
        return super.put(key, value);
    }
    /**
        指定キーとデータのセットを指定位置に挿入します<br>
        同一キーがあった場合は削除されます。
        @param index 挿入位置
        @param key キー値
        @param value データ
        @return セットされたデータ
    */
    public synchronized V insert(int index, K key, V value) {
        int size = size();
        if (index > size) return null;
        if (index == size) return put(key,value);
        remove(key);
        keys.add(index, key);
        values.add(index, value);
        return super.put(key, value);
    }
    
    /** 
        指定キーのデータを削除します<br>
        ※なるべく使用しないで下さい 
        理由 (1)遅い (2)重複キーの時に問題
    */
    public synchronized V remove(Object key) {
        V value = super.remove(key);
        if (value != null) {
            //keys.remove(key);
            //values.remove(value);
            
            int index = keys.indexOf(key);
            if (index >= 0) {
                keys.remove(index);
                values.remove(index);
            } else {
System.out.println("HashVector.remove corrupt index:"+index);
            }
        }
        return value;
    }
    /* 
        複数キーのデータを削除します<br>
        ※なるべく使用しないで下さい 
        理由 (1)遅い (2)重複キーの時に問題
    */
    /*
    public synchronized HashVector remove(Object... keys) {
        for (int i = 0; i < keys.length; i++) {
            Object key = keys[i];
            V value = remove(key);
        }
        return this;
    }
    */
    /**
        指定位置のデータを削除します
        @param index 削除するインデックス
        @return 削除されたキー
        
    */
    public synchronized K removeAt(int index) {
        K key = keyElementAt(index);
        if (key != null) {
            super.remove(key);
            keys.remove(index);
            values.remove(index);
        }
        return key;
    }

    
    //-------------------------------------
    // 追加インターフェース
    //-------------------------------------
        
    /** 指定位置のキーを取得する 
        @param index 指定位置(0-
        @return キー値
    */
    public K keyElementAt(int index) {
        return keys.get(index);
    }
    /** 指定位置のオブジェクトを取得する
        @param index 指定位置(0-
        @return データ
     */
    public V valueElementAt(int index) {
        return values.get(index);
    }
    /** 指定位置のオブジェクトを取得する 
        @param index 指定位置(0-
        @return データ
    */
    public V elementAt(int index) {
        return values.get(index);
    }
    /** 指定位置のオブジェクトを取得する */
    public V get(int index) {
        return values.get(index);
    }
    
    //public Vector keysVector() { return keys; } 
    //public Vector valuesVector() { return values; }
    /** キーの一覧を設定順に返す */
    public Vector<K> keysVector() { return keys; } 
    /** value一覧を設定順に返す */
    public Vector<V> valuesVector() { return values; }
    
    /** 存在すれば一番したに付け替える */
    public synchronized void refresh(K key) {
        int index = keys.indexOf(key);
        if (index >= 0) {
            K _key   = keys.remove(index);
            V _value = values.remove(index);
            keys.add(_key);
            values.add(_value);
        }
    }
}

//
//
// [end of HashVector.java]
//

