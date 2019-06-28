//------------------------------------------------------------------------
// @(#)ListItem.java
//         Parameter型のデータの保管のみを行う（描画は行わない）
//                 Copyright (c) Mirai Design 2019 All Rights Reserved. 
//------------------------------------------------------------------------
//             データ処理は存在しない
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.Parameter;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.QueueTable;

/** Parameter型のデータの保管のみを行う（描画は行わない）*/
public class ListItem extends Item {
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public ListItem() { 
        super();
        setType(LIST);
        setCloneable(true);
        itemData = new ListData();
        itemData.setItem(this);
    }

    /** copy constructor **/
    public ListItem(ListItem from) { 
        super();
        setType(PARAMETER);
        setCloneable(from.isCloneable());
        ListData fromdata = (ListData)from.itemData;
        itemData = new ListData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }
    
    //---------------------------------------------------------------------
    // データ設定
    //---------------------------------------------------------------------
    /** データをクリアする */
    public  void clear() {
        ((ListData)itemData).clear();
    }
    public  void clear(int sessionID) {
        ((ListData)getItemData(sessionID)).clear();
    }
    public  void clear(SessionObject session) {
        ((ListData)getItemData(session)).clear();
    }
    
    /** サイズを返す 
        @return  Elementサイズ */
    public  int size() {
        return ((ListData)itemData).size();
    }
    public  int size(int sessionID) {
        return ((ListData)getItemData(sessionID)).size();
    }
    public  int size(SessionObject session) {
        return ((ListData)getItemData(session)).size();
    }
    
    
    /** 要素の追加 （同一キーが存在した場合は後のものが削除される）
        @param key    追加するキー値
        @param value 追加する要素
    */
    public void add(CharSequence value) { ((ListData)itemData).add(value); }
    public void add(int value)          { ((ListData)itemData).add(value); }
    public void add(long value)         { ((ListData)itemData).add(value); }
    public void add(double value)       { ((ListData)itemData).add(value); }
    public void add(boolean value)      { ((ListData)itemData).add(value); }

    /** 要素の追加 （同一キーが存在した場合は後のものが削除される）
        @param key    追加するキー値
        @param value 追加する要素
        @param session セッション
    */
    public void add(CharSequence value, SessionObject session) { ((ListData)getItemData(session)).add(value); }
    public void add(int value, SessionObject session)          { ((ListData)getItemData(session)).add(value); }
    public void add(long value, SessionObject session)         { ((ListData)getItemData(session)).add(value); }
    public void add(double value, SessionObject session)       { ((ListData)getItemData(session)).add(value); }
    public void add(boolean value, SessionObject session)      { ((ListData)getItemData(session)).add(value); }
    
    /** 要素の取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public CharArray get(int index)          { return ((ListData)itemData).get(index); }
    public CharArray getCharArray(int index) { return ((ListData)itemData).getCharArray(index); }
    public int getInt(int index)             { return ((ListData)itemData).getInt(index); }
    public long getLong(int index)           { return ((ListData)itemData).getLong(index); }
    public double getDouble(int index)       { return ((ListData)itemData).getDouble(index); }
    public boolean getBoolean(int index)     { return ((ListData)itemData).getBoolean(index); }
    /** 要素の取得 
        @param index 取得する場所(0-
        @param session セッション
        @return 取得した値
    */
    public CharArray get(int index, SessionObject session)      { return ((ListData)getItemData(session)).get(index); }
    public CharArray getCharArray(int index, SessionObject session) { return ((ListData)getItemData(session)).getCharArray(index); }
    public int getInt(int index, SessionObject session)         { return ((ListData)getItemData(session)).getInt(index); }
    public long getLong(int index, SessionObject session)       { return ((ListData)getItemData(session)).getLong(index); }
    public double getDouble(int index, SessionObject session)   { return ((ListData)getItemData(session)).getDouble(index); }
    public boolean getBoolean(int index, SessionObject session) { return ((ListData)getItemData(session)).getBoolean(index); }
    
    /** Parameter を設定する 
        ItemのsetParameterとは無関係である事に注意すること
    */
    public boolean setParameter(Parameter parameter) {
        return ((ListData)itemData).setParameter(parameter);
    }
    public boolean setParameter(Parameter parameter, int sessionID) {
        return ((ListData)getItemData(sessionID)).setParameter(parameter);
    }
    public boolean setParameter(Parameter parameter, SessionObject session) {
        return ((ListData)getItemData(session)).setParameter(parameter);
    }
    
    /** Parameter を取得する */
    public Parameter getParameter() {
        return ((ListData)itemData).getParameter();
    }
    public Parameter getParameter(int sessionID) {
        return ((ListData)getItemData(sessionID)).getParameter();
    }
    
    public Parameter getParameter(SessionObject session) {
        return ((ListData)getItemData(session)).getParameter();
    }
    
    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new ListData((ListData)itemData, session);
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
        ListData data = (ListData)getItemData(session);
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
// [end of ListItem.java]
//

