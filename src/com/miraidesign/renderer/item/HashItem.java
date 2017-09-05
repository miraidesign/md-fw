//------------------------------------------------------------------------
// @(#)HashItem.java
//         HashMap型のデータの保管のみを行う（描画は行わない）
//                 Copyright (c) Mirai Design 2012 All Rights Reserved. 
//------------------------------------------------------------------------
//             データ処理は存在しない
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;
import java.util.HashMap;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.IntObject;

/** HashMap型のデータの保管のみを行う（描画は行わない）*/
@SuppressWarnings("unchecked")
public class HashItem<K,V> extends Item {
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public HashItem() { 
        super();
        setType(HASH);
        setCloneable(true);
        itemData = new HashData<K,V>();
        itemData.setItem(this);
    }

    /** copy constructor **/
    public HashItem(HashItem<K,V> from) { 
        super();
        setType(HASH);
        setCloneable(from.isCloneable());
        HashData<K,V> fromdata = (HashData<K,V>)from.itemData;
        itemData = new HashData<K,V>(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }
    
    //---------------------------------------------------------------------
    // データ設定
    //---------------------------------------------------------------------
    /** データをクリアする */
    public  void clear() {
        ((HashData<K,V>)itemData).clear();
    }
    public  void clear(int sessionID) {
        ((HashData<K,V>)getItemData(sessionID)).clear();
    }
    public  void clear(SessionObject session) {
        ((HashData<K,V>)getItemData(session)).clear();
    }
    
    /** サイズを返す */
    public  int size() {
        return ((HashData<K,V>)itemData).size();
    }
    public  int size(int sessionID) {
        return ((HashData<K,V>)getItemData(sessionID)).size();
    }
    public  int size(SessionObject session) {
        return ((HashData<K,V>)getItemData(session)).size();
    }
    
    /** 要素の追加
        @param key  キー値
        @param value 新しい値
    */
    public void put(K key, V value) { 
        ((HashData<K,V>)itemData).put(key, value); 
    }
    
    /** 要素の追加
        @param key  キー値
        @param value 新しい値
        @param sessionID セッションID
    */
    public void put(K key, V value, int sessionID) { 
        ((HashData<K,V>)getItemData(sessionID)).put(key, value); 
    }
    /** 要素の追加
        @param key  キー値
        @param value 新しい値
        @param session セッション
    */
    public void put(K key, V value, SessionObject session) { 
        ((HashData<K,V>)getItemData(session)).put(key, value); 
    }

    /** 要素の取得 
        @param key キーワード
        @return 取得した値
    */
    public V get(K key) {
        return ((HashData<K,V>)itemData).get(key);
    }

    /** 要素の取得 
        @param key キーワード
        @param sessionID セッションID
        @return 取得した値
    */
    public V get(K key, int sessionID) {
        return ((HashData<K,V>)getItemData(sessionID)).get(key);
    }
    
    /** 要素の取得 
        @param key キーワード
        @param session セッション
        @return 取得した値
    */
    public V get(K key, SessionObject session) {
        return ((HashData<K,V>)getItemData(session)).get(key);
    }
    
    /** 要素の削除 
        @param key キーワード
        @return 削除した値
    */
    public V remove(K key) {
        return ((HashData<K,V>)itemData).remove(key);
    }

    /** 要素の削除
        @param key キーワード
        @param sessionID セッションID
        @return 削除した値
    */
    public V remove(K key, int sessionID) {
        return ((HashData<K,V>)getItemData(sessionID)).remove(key);
    }
    
    /** 要素の削除 
        @param key キーワード
        @param session セッション
        @return 削除した値
    */
    public V remove(K key, SessionObject session) {
        return ((HashData<K,V>)getItemData(session)).remove(key);
    }
    
    /**
        要素存在確認
    */
    public boolean containsKey(K key) {
        return ((HashData<K,V>)itemData).containsKey(key);
    }
    public boolean containsKey(K key, int sessionID) {
        return ((HashData<K,V>)getItemData(sessionID)).containsKey(key);
    }
    public boolean containsKey(K key, SessionObject session) {
        return ((HashData<K,V>)getItemData(session)).containsKey(key);
    }
    //---------------------------------------------------------------------
    // setter /  getter
    //---------------------------------------------------------------------
    /** HashMapを設定する (あまり利用しないこと) */
    public boolean setHash(HashMap<K,V> hash) {
        return ((HashData<K,V>)itemData).setHash(hash);
    }
    public boolean setHash(HashMap<K,V> hash, int sessionID) {
        return ((HashData<K,V>)getItemData(sessionID)).setHash(hash);
    }
    public boolean setHash(HashMap<K,V> hash, SessionObject session) {
        return ((HashData<K,V>)getItemData(session)).setHash(hash);
    }

    /** HashMap を取得する */
    public HashMap<K,V> getHash() {
        return  ((HashData<K,V>)itemData).getHash();
    }
    public HashMap<K,V> getHash(int sessionID) {
        return  ((HashData<K,V>)getItemData(sessionID)).getHash();
    }
    public HashMap<K,V> getHash(SessionObject session) {
        return  ((HashData<K,V>)getItemData(session)).getHash();
    }
    
    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new HashData<K,V>((HashData<K,V>)itemData, session);
            newData.setItem(this);
            session.getHashtable().put(new IntObject(getMixedID()),newData);
        }
    }
    
    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする
        @param session セッションオブジェクト
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        HashData<K,V> data = (HashData<K,V>)getItemData(session);
        return data.draw(session);
    }
    // stream 版
    //public void draw(OutputStream out) {
        //未作成
    //}
    public void draw(OutputStream out, int sessionID) {
        //未作成
    }
    
}

//
//
// [end of HashItem.java]
//

