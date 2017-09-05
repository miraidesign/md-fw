//------------------------------------------------------------------------
// @(#)HashData.java
//     HashMap型のデータの保管のみを行う（描画は行わない）
//                 Copyright (c) MiraiDesign  2012 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
//import java.util.Hashtable;
import java.util.HashMap;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.QueueTable;

/** HashMap型のデータの保管のみを行う（描画は行わない）*/
public class HashData<K,V> extends ItemData {
    private HashMap<K,V> hash = new HashMap<K,V>();  // データ保存エリア
    {
        type = HASH;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public HashData() {}

    public HashData(HashData<K,V> from, SessionObject session) {
        this.sessionObject = session;
        copy(from);
    }

    public HashData(HashMap<K,V> hash) {
        setHash(hash);
    }
    
    //---------------------------------------------------------------------
    // データ設定
    //---------------------------------------------------------------------
    /** データをクリアする */
    public void clear() { hash.clear(); }

    /** サイズを返す 
        @return  hashサイズ */
    public int  size() { return hash.size();}

    
    /** 要素の追加 （同一キーが存在した場合は後のものが削除される）
        @param key    追加するキー値
        @param value 追加する要素
    */
    public void put(K key,  V value)  { hash.put(key,value); }
    
    /** 
        要素の取得 
        @param key キーワード
        @return 取得した値
    */
    public V get(K key) { return hash.get(key); }
    
    /** 
        要素の削除
        @param key キーワード
    */
    public V remove(K key) { return hash.remove(key); }
    
    
    /**
        要素存在確認
    */
    public boolean containsKey(K key) {
        return (get(key) != null);
    }
    
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setValue(String[] values) { // 使わない
        caQueue.clear();    
        for (int i = 0; i < values.length; i++) {
            caQueue.enqueue(new CharArray(values[i])); 
        }
    }
    /** HashMapを設定する 
        あまり利用しないこと
    */  
    public boolean setHash(HashMap<K,V> hash) { 
        this.hash = hash;
        return (hash != null);
    }

    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; }  // 使わない

    /** HashMap を取得する */
    public HashMap<K,V> getHash() { return hash; }
    //---------------------------------------------------------------------
    // copy / clone
    //---------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void copy(HashData<K,V> from) { // 元オブジェクトより全データをコピー
        super.copy(from);
        hash = (HashMap<K,V>)from.hash.clone();
    }
    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする（何もしません）
        @param session SessionObject
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        if (visible) {
            // do nothing
        }
        return session.getBuffer();
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            //hash.writeObject(out);     //@@// NG
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            //hash.readObject(in);
        }
    }

}

//
// [end of HashData.java]
//

