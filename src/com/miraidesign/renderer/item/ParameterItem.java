//------------------------------------------------------------------------
// @(#)ParameterItem.java
//         HashParameter型のデータの保管のみを行う（描画は行わない）
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//             データ処理は存在しない
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.QueueTable;

/** HashParameter型のデータの保管のみを行う（描画は行わない）*/
public class ParameterItem extends Item {
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public ParameterItem() { 
        super();
        setType(PARAMETER);
        setCloneable(true);
        itemData = new ParameterData();
        itemData.setItem(this);
    }

    /** copy constructor **/
    public ParameterItem(ParameterItem from) { 
        super();
        setType(PARAMETER);
        setCloneable(from.isCloneable());
        ParameterData fromdata = (ParameterData)from.itemData;
        itemData = new ParameterData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }
    
    //---------------------------------------------------------------------
    // データ設定
    //---------------------------------------------------------------------
    /** データをクリアする */
    public  void clear() {
        ((ParameterData)itemData).clear();
    }
    public  void clear(int sessionID) {
        ((ParameterData)getItemData(sessionID)).clear();
    }
    public  void clear(SessionObject session) {
        ((ParameterData)getItemData(session)).clear();
    }
    
    /** サイズを返す 
        @return  Elementサイズ */
    public  int size() {
        return ((ParameterData)itemData).size();
    }
    public  int size(int sessionID) {
        return ((ParameterData)getItemData(sessionID)).size();
    }
    public  int size(SessionObject session) {
        return ((ParameterData)getItemData(session)).size();
    }
    
    /** 要素の新設定（既存データはクリア） 
        @param key  キー値
        @param value 新しい値
    */
    public void set(String key, String value) { 
        ((ParameterData)itemData).set(key, value); 
    }
    public void set(CharArray key, String value) {
        ((ParameterData)itemData).set(key, value); 
    }
    public void set(String key, CharArray value) {
        ((ParameterData)itemData).set(key, value);
    }
    public void set(CharArray key, CharArray value) {
        ((ParameterData)itemData).set(key, value);
    }
    public void set(String key, int value) {
        ((ParameterData)itemData).set(key, value);
    }
    public void set(CharArray key, int value) {
        ((ParameterData)itemData).set(key, value);
    }
    public void set(String key, long value) {
        ((ParameterData)itemData).set(key, value);
    }
    public void set(CharArray key, long value) {
        ((ParameterData)itemData).set(key, value);
    }
    public void set(String key, double value) {
        ((ParameterData)itemData).set(key, value);
    }
    public void set(CharArray key, double value) {
        ((ParameterData)itemData).set(key, value);
    }
    public void set(String key, boolean value) {
        ((ParameterData)itemData).set(key, value);
    }
    public void set(CharArray key, boolean value) {
        ((ParameterData)itemData).set(key, value);
    }
    
    /** 要素の新設定（既存データはクリア） 
        @param key  キー値
        @param value 新しい値
        @param sessionID セッションID
    */
    public void set(String key, String value, int sessionID) { 
        ((ParameterData)getItemData(sessionID)).set(key, value); 
    }
    public void set(CharArray key, String value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(key, value); 
    }
    public void set(String key, CharArray value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(key, value);
    }
    public void set(CharArray key, CharArray value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(key, value);
    }
    public void set(String key, int value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(key, value);
    }
    public void set(CharArray key, int value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(key, value);
    }
    public void set(String key, long value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(key, value);
    }
    public void set(CharArray key, long value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(key, value);
    }
    public void set(String key, double value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(key, value);
    }
    public void set(CharArray key, double value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(key, value);
    }
    public void set(String key, boolean value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(key, value);
    }
    public void set(CharArray key, boolean value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(key, value);
    }
    /** 要素の新設定（既存データはクリア） 
        @param key  キー値
        @param value 新しい値
        @param session セッション
    */
    public void set(String key, String value, SessionObject session) { 
        ((ParameterData)getItemData(session)).set(key, value); 
    }
    public void set(CharArray key, String value, SessionObject session) {
        ((ParameterData)getItemData(session)).set(key, value); 
    }
    public void set(String key, CharArray value, SessionObject session) {
        ((ParameterData)getItemData(session)).set(key, value);
    }
    public void set(CharArray key, CharArray value, SessionObject session) {
        ((ParameterData)getItemData(session)).set(key, value);
    }
    public void set(String key, int value, SessionObject session) {
        ((ParameterData)getItemData(session)).set(key, value);
    }
    public void set(CharArray key, int value, SessionObject session) {
        ((ParameterData)getItemData(session)).set(key, value);
    }
    public void set(String key, long value, SessionObject session) {
        ((ParameterData)getItemData(session)).set(key, value);
    }
    public void set(CharArray key, long value, SessionObject session) {
        ((ParameterData)getItemData(session)).set(key, value);
    }
    public void set(String key, double value, SessionObject session) {
        ((ParameterData)getItemData(session)).set(key, value);
    }
    public void set(CharArray key, double value, SessionObject session) {
        ((ParameterData)getItemData(session)).set(key, value);
    }
    public void set(String key, boolean value, SessionObject session) {
        ((ParameterData)getItemData(session)).set(key, value);
    }
    public void set(CharArray key, boolean value, SessionObject session) {
        ((ParameterData)getItemData(session)).set(key, value);
    }
    
    /** Queueテーブルの先頭行を設定する */
    public void set(QueueTable table) {
        ((ParameterData)itemData).set(table,0);
    }
    public void set(QueueTable table, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(table,0);
    }
    public void set(QueueTable table, SessionObject session) {
        ((ParameterData)getItemData(session)).set(table,0);
    }
    
//  /** Queueテーブルの指定行を設定する */
//  public void set(QueueTable table, int row) {
//      ((ParameterData)itemData).set(table, row);
//  }
//  public void set(QueueTable table, int row, int sessionID) {
//      ((ParameterData)getItemData(sessionID)).set(table, row);
//  }
    
    /** 
        文字列からまとめて設定する
        @param str 文字列
        @param sep セパレータ   ( = 等)
        @param connect 接続文字 ( , 等)
    */
    public void set(CharArray str, String sep, String connect) {
        ((ParameterData)itemData).set(str, sep, connect);
    }
    public void set(String str, String sep, String connect) {
        ((ParameterData)itemData).set(str, sep, connect);
    }
    public void set(CharArray str, String sep, String connect, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(str, sep, connect);
    }
    public void set(String str, String sep, String connect, int sessionID) {
        ((ParameterData)getItemData(sessionID)).set(str, sep, connect);
    }
    public void set(CharArray str, String sep, String connect, SessionObject session) {
        ((ParameterData)getItemData(session)).set(str, sep, connect);
    }
    public void set(String str, String sep, String connect, SessionObject session) {
        ((ParameterData)getItemData(session)).set(str, sep, connect);
    }
    
    /** 要素の追加 （同一キーが存在した場合は後のものが削除される）
        @param key    追加するキー値
        @param value 追加する要素
    */
    public void add(String key,    String value)    {
        ((ParameterData)itemData).add(key,value);
    }
    public void add(CharArray key, String value)    {
        ((ParameterData)itemData).add(key,value);
    }
    public void add(String key,    CharArray value) {
        ((ParameterData)itemData).add(key,value);
    }
    public void add(CharArray key, CharArray value) {
        ((ParameterData)itemData).add(key,value);
    }
    public void add(String key,    int value)       {
        ((ParameterData)itemData).add(key,value);
    }
    public void add(CharArray key, int value)       {
        ((ParameterData)itemData).add(key,value);
    }
    public void add(String key,    long value)      {
        ((ParameterData)itemData).add(key,value);
    }
    public void add(CharArray key, long value)      {
        ((ParameterData)itemData).add(key,value);
    }
    public void add(String key,    double value)    {
        ((ParameterData)itemData).add(key,value);
    }
    public void add(CharArray key, double value)    {
        ((ParameterData)itemData).add(key,value);
    }
    public void add(String key,    boolean value)   {
        ((ParameterData)itemData).add(key,value);
    }
    public void add(CharArray key, boolean value)   {
        ((ParameterData)itemData).add(key,value);
    }
    /** 要素の追加 （同一キーが存在した場合は後のものが削除される）
        @param key    追加するキー値
        @param value 追加する要素
        @param sessionID セッションID
    */
    public void add(String key,    String value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }
    public void add(CharArray key, String value, int sessionID)    {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }
    public void add(String key,    CharArray value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }
    public void add(CharArray key, CharArray value, int sessionID) {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }
    public void add(String key,    int value, int sessionID)       {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }
    public void add(CharArray key, int value, int sessionID)       {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }
    public void add(String key,    long value, int sessionID)      {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }
    public void add(CharArray key, long value, int sessionID)      {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }
    public void add(String key,    double value, int sessionID)    {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }
    public void add(CharArray key, double value, int sessionID)    {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }
    public void add(String key,    boolean value, int sessionID)   {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }
    public void add(CharArray key, boolean value, int sessionID)   {
        ((ParameterData)getItemData(sessionID)).add(key,value);
    }

    /** 要素の追加 （同一キーが存在した場合は後のものが削除される）
        @param key    追加するキー値
        @param value 追加する要素
        @param session セッション
    */
    public void add(String key,    String value, SessionObject session) {
        ((ParameterData)getItemData(session)).add(key,value);
    }
    public void add(CharArray key, String value, SessionObject session)    {
        ((ParameterData)getItemData(session)).add(key,value);
    }
    public void add(String key,    CharArray value, SessionObject session) {
        ((ParameterData)getItemData(session)).add(key,value);
    }
    public void add(CharArray key, CharArray value, SessionObject session) {
        ((ParameterData)getItemData(session)).add(key,value);
    }
    public void add(String key,    int value, SessionObject session)       {
        ((ParameterData)getItemData(session)).add(key,value);
    }
    public void add(CharArray key, int value, SessionObject session)       {
        ((ParameterData)getItemData(session)).add(key,value);
    }
    public void add(String key,    long value, SessionObject session)      {
        ((ParameterData)getItemData(session)).add(key,value);
    }
    public void add(CharArray key, long value, SessionObject session)      {
        ((ParameterData)getItemData(session)).add(key,value);
    }
    public void add(String key,    double value, SessionObject session)    {
        ((ParameterData)getItemData(session)).add(key,value);
    }
    public void add(CharArray key, double value, SessionObject session)    {
        ((ParameterData)getItemData(session)).add(key,value);
    }
    public void add(String key,    boolean value, SessionObject session)   {
        ((ParameterData)getItemData(session)).add(key,value);
    }
    public void add(CharArray key, boolean value, SessionObject session)   {
        ((ParameterData)getItemData(session)).add(key,value);
    }

    /** Queueテーブルの先頭行を追加する */
    public void add(QueueTable table) {
        ((ParameterData)itemData).add(table,0);
    }
    public void add(QueueTable table, int sessionID) {
        ((ParameterData)getItemData(sessionID)).add(table,0);
    }
    public void add(QueueTable table, SessionObject session) {
        ((ParameterData)getItemData(session)).add(table,0);
    }
//    /** Queueテーブルの指定行を追加する */
//    public void add(QueueTable table, int row) {
//        ((ParameterData)itemData).add(table,row);
//   }

    /** 
        文字列からまとめて追加する
        @param str 文字列
        @param sep セパレータ   (= 等)
        @param connect 接続文字 (, 等)
    */
    public void add(CharArray str, String sep, String connect) {
        ((ParameterData)itemData).add(str, sep, connect);
    }
    public void add(String str, String sep, String connect) {
        ((ParameterData)itemData).add(str, sep, connect);
    }
    public void add(CharArray str, String sep, String connect, int sessionID) {
        ((ParameterData)getItemData(sessionID)).add(str, sep, connect);
    }
    public void add(String str, String sep, String connect, int sessionID) {
        ((ParameterData)getItemData(sessionID)).add(str, sep, connect);
    }
    public void add(CharArray str, String sep, String connect, SessionObject session) {
        ((ParameterData)getItemData(session)).add(str, sep, connect);
    }
    public void add(String str, String sep, String connect, SessionObject session) {
        ((ParameterData)getItemData(session)).add(str, sep, connect);
    }

    /** キー値をパラメータ形式で取得する **/
    public CharArrayQueue getKeyParameter() {
        return ((ParameterData)itemData).getKeyParameter();
    }
    public CharArrayQueue getKeyParameter(int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getKeyParameter();
    }
    public CharArrayQueue getKeyParameter(SessionObject session) {
        return ((ParameterData)getItemData(session)).getKeyParameter();
    }
    
    /** データをパラメータ形式で取得する **/
    public CharArrayQueue getDataParameter() {
        return ((ParameterData)itemData).getDataParameter();
    }
    public CharArrayQueue getDataParameter(int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getDataParameter();
    }
    public CharArrayQueue getDataParameter(SessionObject session) {
        return ((ParameterData)getItemData(session)).getDataParameter();
    }
    
    /** 要素の取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public CharArray get(int index) {
        return ((ParameterData)itemData).get(index);
    }
    public CharArray getCharArray(int index) {
        return ((ParameterData)itemData).getCharArray(index);
    }
    public int getInt(int index) {
        return ((ParameterData)itemData).getInt(index);
    }
    public long getLong(int index) {
        return ((ParameterData)itemData).getLong(index);
    }
    public double getDouble(int index) {
        return ((ParameterData)itemData).getDouble(index);
    }
    public boolean getBoolean(int index) {
        return ((ParameterData)itemData).getBoolean(index);
    }

    /** 要素の取得 
        @param index 取得する場所(0-
        @param sessionID セッションID
        @return 取得した値
    */
    public CharArray get(int index, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).get(index);
    }
    public CharArray getCharArray(int index, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getCharArray(index);
    }
    public int getInt(int index, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getInt(index);
    }
    public long getLong(int index, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getLong(index);
    }
    public double getDouble(int index, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getDouble(index);
    }
    public boolean getBoolean(int index, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getBoolean(index);
    }
    
    /** 要素の取得 
        @param index 取得する場所(0-
        @param session セッション
        @return 取得した値
    */
    public CharArray get(int index, SessionObject session) {
        return ((ParameterData)getItemData(session)).get(index);
    }
    public CharArray getCharArray(int index, SessionObject session) {
        return ((ParameterData)getItemData(session)).getCharArray(index);
    }
    public int getInt(int index, SessionObject session) {
        return ((ParameterData)getItemData(session)).getInt(index);
    }
    public long getLong(int index, SessionObject session) {
        return ((ParameterData)getItemData(session)).getLong(index);
    }
    public double getDouble(int index, SessionObject session) {
        return ((ParameterData)getItemData(session)).getDouble(index);
    }
    public boolean getBoolean(int index, SessionObject session) {
        return ((ParameterData)getItemData(session)).getBoolean(index);
    }
    
    /** 要素の取得 
        @param key キーワード
        @return 取得した値
    */
    public CharArray get(String key) {
        return ((ParameterData)itemData).get(key);
    }
    public CharArray get(CharArray key) {
        return ((ParameterData)itemData).get(key);
    }
    public CharArray getCharArray(String key) {
        return ((ParameterData)itemData).getCharArray(key);
    }
    public CharArray getCharArray(CharArray key) {
        return ((ParameterData)itemData).getCharArray(key);
    }
    public int getInt(String key) {
        return ((ParameterData)itemData).getInt(key);
    }
    public int getInt(CharArray key) {
        return ((ParameterData)itemData).getInt(key);
    }
    public long getLong(String key) {
        return ((ParameterData)itemData).getLong(key);
    }
    public long getLong(CharArray key) {
        return ((ParameterData)itemData).getLong(key);
    }
    public boolean getBoolean(String key) {
        return ((ParameterData)itemData).getBoolean(key);
    }
    public boolean getBoolean(CharArray key) {
        return ((ParameterData)itemData).getBoolean(key);
    }
    public double getDouble(String key) {
        return ((ParameterData)itemData).getDouble(key);
    }
    public double getDouble(CharArray key) {
        return ((ParameterData)itemData).getDouble(key);
    }
    
    /** 要素の取得 
        @param key キーワード
        @param sessionID セッションID
        @return 取得した値
    */
    public CharArray get(String key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).get(key);
    }
    public CharArray get(CharArray key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).get(key);
    }
    public CharArray getCharArray(String key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getCharArray(key);
    }
    public CharArray getCharArray(CharArray key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getCharArray(key);
    }
    public int getInt(String key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getInt(key);
    }
    public int getInt(CharArray key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getInt(key);
    }
    public long getLong(String key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getLong(key);
    }
    public long getLong(CharArray key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getLong(key);
    }
    public boolean getBoolean(String key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getBoolean(key);
    }
    public boolean getBoolean(CharArray key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getBoolean(key);
    }
    public double getDouble(String key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getDouble(key);
    }
    public double getDouble(CharArray key, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getDouble(key);
    }
    
    /** 要素の取得 
        @param key キーワード
        @param session セッション
        @return 取得した値
    */
    public CharArray get(String key, SessionObject session) {
        return ((ParameterData)getItemData(session)).get(key);
    }
    public CharArray get(CharArray key, SessionObject session) {
        return ((ParameterData)getItemData(session)).get(key);
    }
    public CharArray getCharArray(String key, SessionObject session) {
        return ((ParameterData)getItemData(session)).getCharArray(key);
    }
    public CharArray getCharArray(CharArray key, SessionObject session) {
        return ((ParameterData)getItemData(session)).getCharArray(key);
    }
    public int getInt(String key, SessionObject session) {
        return ((ParameterData)getItemData(session)).getInt(key);
    }
    public int getInt(CharArray key, SessionObject session) {
        return ((ParameterData)getItemData(session)).getInt(key);
    }
    public long getLong(String key, SessionObject session) {
        return ((ParameterData)getItemData(session)).getLong(key);
    }
    public long getLong(CharArray key, SessionObject session) {
        return ((ParameterData)getItemData(session)).getLong(key);
    }
    public boolean getBoolean(String key, SessionObject session) {
        return ((ParameterData)getItemData(session)).getBoolean(key);
    }
    public boolean getBoolean(CharArray key, SessionObject session) {
        return ((ParameterData)getItemData(session)).getBoolean(key);
    }
    public double getDouble(String key, SessionObject session) {
        return ((ParameterData)getItemData(session)).getDouble(key);
    }
    public double getDouble(CharArray key, SessionObject session) {
        return ((ParameterData)getItemData(session)).getDouble(key);
    }
    
    /** HashParameter を設定する 
        ItemのsetParameterとは無関係である事に注意すること
    */
    public boolean setParameter(HashParameter parameter) {
        return ((ParameterData)itemData).setParameter(parameter);
    }
    public boolean setParameter(HashParameter parameter, int sessionID) {
        return ((ParameterData)getItemData(sessionID)).setParameter(parameter);
    }
    public boolean setParameter(HashParameter parameter, SessionObject session) {
        return ((ParameterData)getItemData(session)).setParameter(parameter);
    }
    
    /** HashParameter を取得する */
    public HashParameter getParameter() {
        return ((ParameterData)itemData).getParameter();
    }
    public HashParameter getParameter(int sessionID) {
        return ((ParameterData)getItemData(sessionID)).getParameter();
    }
    
    public HashParameter getParameter(SessionObject session) {
        return ((ParameterData)getItemData(session)).getParameter();
    }
    
    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new ParameterData((ParameterData)itemData, session);
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
        ParameterData data = (ParameterData)getItemData(session);
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
// [end of ParameterItem.java]
//

