//------------------------------------------------------------------------
// @(#)ListData.java
//             Parameter型のデータの保管のみを行う（描画は行わない）
//                 Copyright (c) MiraiDesign  2019 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.Parameter;
import com.miraidesign.util.QueueTable;

/** Parameter型のデータの保管のみを行う（描画は行わない）*/
public class ListData extends ItemData {
    private Parameter parameter = new Parameter();  // データ保存エリア
    {
        type = LIST;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public ListData() {}

    public ListData(ListData from, SessionObject session) {
        this.sessionObject = session;
        copy(from);
    }

    public ListData(Parameter parameter) {
        setParameter(parameter);
    }
    
    //---------------------------------------------------------------------
    // データ設定
    //---------------------------------------------------------------------
    /** データをクリアする */
    public void clear() { parameter.clear(); }

    /** サイズを返す 
        @return  parameterサイズ */
    public int  size() { return parameter.size();}

    
    /** 要素の追加 （同一キーが存在した場合は後のものが削除される）
        @param key    追加するキー値
        @param value 追加する要素
    */
    public void add(CharSequence value) { parameter.add(value); }
    public void add(int value)       { parameter.add(value); }
    public void add(long value)      { parameter.add(value); }
    public void add(double value)    { parameter.add(value); }
    public void add(boolean value)   { parameter.add(value); }

    /** 要素の取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public CharArray get(int index)          { return parameter.get(index); }
    public CharArray getCharArray(int index) { return parameter.getCharArray(index); }
    public int getInt(int index)             { return parameter.getInt(index); }
    public long getLong(int index)           { return parameter.getLong(index); }
    public double getDouble(int index)       { return parameter.getDouble(index); }
    public boolean getBoolean(int index)     { return parameter.getBoolean(index); }
    
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setValue(String[] values) {
        caQueue.clear();    
        for (int i = 0; i < values.length; i++) {
            caQueue.enqueue(new CharArray(values[i]));  // うーん、、
        }
    }
    /** Parameter を設定する */
    public boolean setParameter(Parameter parameter) { 
        this.parameter = parameter;
        return (parameter != null);
    }

    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; }

    /** Parameter を取得する */
    public Parameter getParameter() { return parameter; }
    //---------------------------------------------------------------------
    // copy / clone
    //---------------------------------------------------------------------
    public void copy(ListData from) { // 元オブジェクトより全データをコピー
        super.copy(from);
        parameter.copy(from.parameter);
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
            parameter.writeObject(out);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            parameter.readObject(in);
        }
    }

}

//
// [end of ListData.java]
//

