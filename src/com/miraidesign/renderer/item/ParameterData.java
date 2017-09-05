//------------------------------------------------------------------------
// @(#)ParameterData.java
//             HashParameter型のデータの保管のみを行う（描画は行わない）
//                 Copyright (c) MiraiDesign  2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.QueueTable;

/** HashParameter型のデータの保管のみを行う（描画は行わない）*/
public class ParameterData extends ItemData {
    private HashParameter parameter = new HashParameter();  // データ保存エリア
    {
        type = PARAMETER;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public ParameterData() {}

    public ParameterData(ParameterData from, SessionObject session) {
        this.sessionObject = session;
        copy(from);
    }

    public ParameterData(HashParameter parameter) {
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

    
    /** 要素の新設定（既存データはクリア） 
        @param key  キー値
        @param value 新しい値
    */
    public void set(String key, String value) { parameter.set(key, value); }
    public void set(CharArray key, String value) { parameter.set(key, value); }
    public void set(String key, CharArray value) { parameter.set(key, value); }
    public void set(CharArray key, CharArray value) { parameter.set(key, value); }
    public void set(String key, int value) { parameter.set(key, value); }
    public void set(CharArray key, int value) { parameter.set(key, value); }
    public void set(String key, long value) {parameter.set(key, value);}
    public void set(CharArray key, long value) {parameter.set(key, value);}
    public void set(String key, double value) {parameter.set(key, value);}
    public void set(CharArray key, double value) {parameter.set(key, value);}
    public void set(String key, boolean value) {parameter.set(key, value);}
    public void set(CharArray key, boolean value) {parameter.set(key, value);}

    /** Queueテーブルの先頭行を設定する */
    public void set(QueueTable table) { parameter.set(table,0);}
    /** Queueテーブルの指定行を設定する */
    public void set(QueueTable table, int row) { parameter.set(table, row);}

    /** 
        文字列からまとめて設定する
        @param str 文字列
        @param sep セパレータ  (= 等)
        @param connect 接続文字 (, 等)
    */
    public void set(CharArray str, String sep, String connect) {
        parameter.set(str, sep, connect);
    }
    public void set(String str, String sep, String connect) {
        parameter.set(str, sep, connect);
    }

    /** 要素の追加 （同一キーが存在した場合は後のものが削除される）
        @param key    追加するキー値
        @param value 追加する要素
    */
    public void add(String key,    String value)    { parameter.add(key,value); }
    public void add(CharArray key, String value)    { parameter.add(key,value); }
    public void add(String key,    CharArray value) { parameter.add(key,value); }
    public void add(CharArray key, CharArray value) { parameter.add(key,value); }
    public void add(String key,    int value)       { parameter.add(key,value); }
    public void add(CharArray key, int value)       { parameter.add(key,value); }
    public void add(String key,    long value)      { parameter.add(key,value); }
    public void add(CharArray key, long value)      { parameter.add(key,value); }
    public void add(String key,    double value)    { parameter.add(key,value); }
    public void add(CharArray key, double value)    { parameter.add(key,value); }
    public void add(String key,    boolean value)   { parameter.add(key,value); }
    public void add(CharArray key, boolean value)   { parameter.add(key,value); }

    /** Queueテーブルの先頭行を追加する */
    public void add(QueueTable table) { parameter.add(table,0); }
    /** Queueテーブルの指定行を追加する */
    public void add(QueueTable table, int row) { parameter.add(table,row); }

    /** 
        文字列からまとめて追加する
        @param str 文字列
        @param sep セパレータ  (= 等)
        @param connect 接続文字(, 等)
    */
    public void add(CharArray str, String sep, String connect) {
        parameter.add(str, sep, connect);
    }
    public void add(String str, String sep, String connect) {
        parameter.add(str, sep, connect);
    }

    /** キー値をパラメータ形式で取得する **/
    public CharArrayQueue getKeyParameter() { return parameter.getKeyParameter();}
    
    /** データをパラメータ形式で取得する **/
    public CharArrayQueue getDataParameter() { return parameter.getParameter();}
    
    /** 要素の取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public CharArray get(int index) { return parameter.get(index); }
    public CharArray getCharArray(int index) { return parameter.getCharArray(index); }
    public int getInt(int index) { return parameter.getInt(index); }
    public long getLong(int index) { return parameter.getLong(index); }
    public double getDouble(int index) { return parameter.getDouble(index); }
    public boolean getBoolean(int index) { return parameter.getBoolean(index); }
    
    /** 要素の取得 
        @param key キーワード
        @return 取得した値
    */
    public CharArray get(String key) { return parameter.get(key); }
    public CharArray get(CharArray key) { return parameter.get(key); }
    public CharArray getCharArray(String key) { return parameter.getCharArray(key); }
    public CharArray getCharArray(CharArray key) { return parameter.getCharArray(key); }
    public int getInt(String key) { return parameter.getInt(key); }
    public int getInt(CharArray key) { return parameter.getInt(key); }
    public long getLong(String key) { return parameter.getLong(key); }
    public long getLong(CharArray key) { return parameter.getLong(key); }
    public boolean getBoolean(String key) { return parameter.getBoolean(key); }
    public boolean getBoolean(CharArray key) { return parameter.getBoolean(key); }
    public double getDouble(String key) { return parameter.getDouble(key); }
    public double getDouble(CharArray key) { return parameter.getDouble(key); }
    
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setValue(String[] values) {
        caQueue.clear();    
        for (int i = 0; i < values.length; i++) {
            caQueue.enqueue(new CharArray(values[i]));  // うーん、、
        }
    }
    /** HashParameter を設定する */
    public boolean setParameter(HashParameter parameter) { 
        this.parameter = parameter;
        return (parameter != null);
    }

    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; }

    /** HashParameter を取得する */
    public HashParameter getParameter() { return parameter; }
    //---------------------------------------------------------------------
    // copy / clone
    //---------------------------------------------------------------------
    public void copy(ParameterData from) { // 元オブジェクトより全データをコピー
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
// [end of ParameterData.java]
//

