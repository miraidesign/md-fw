//------------------------------------------------------------------------
//    HashParameter.java
//                 キー値を持つパラメータクラス
//
//     Copyright (c) Mirai Design Institute 2010-2011 All rights reserved.
//          update 2011-01-07   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.util.Stack;
import java.util.Vector;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.session.SessionObject;
/**
 *  キー値を持つパラメータクラス
 */
public class HashParameter extends HashVector<CharArray,CharArray> {
    private  boolean debugKey = false;
    public void setDebugKey(boolean mode) { debugKey = mode; }
    // スタック管理
    private static Stack<HashParameter> stack= new Stack<HashParameter>();
    private static int MIN_SIZE =   8;
    private static int MAX_SIZE = 256;
    
    /** デフォルトのフォーマッタ */
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static /*synchronized*/ HashParameter push(HashParameter item) {
        if (stack.size() > MAX_SIZE || item == null) {
            item = null; // for GC
            return item;
        } else {
            item.clear();
            return (HashParameter)stack.push(item);
        }
    }
    
    public static /*synchronized*/ HashParameter pop() {
        HashParameter obj;
        synchronized (stack) {
            if (stack.size() < MIN_SIZE) {
                obj = new HashParameter();
            } else {
                obj = (HashParameter)stack.pop();
            }
        }
        return obj;
    }
    public static boolean empty() {
        return stack.empty();
    }
    
    public static int getStackSize() {
        return stack.size();
    }
    // 
    private boolean display = true;
    /** 表示のためのヒントを設定する */
    public void setDisplay(boolean mode) {
        display = mode;
    }
    /** 表示のためのヒントを取得する */
    public boolean isDisplay() { return display; }

    /** constructor */
    public  HashParameter() { }

    /** copy constructor */
    public  HashParameter(HashParameter from) {
        copy(from);
    }
    
    public HashParameter copy(HashParameter from) {
        clear();
        for (int i = 0; i < from.size(); i++) {
            CharArray key   = from.keyElementAt(i);
            CharArray value = from.valueElementAt(i);
            put(new CharArray(key), new CharArray(value));
        }
        return this;
    }
    
    
    //-------------------------------------
    // 
    //-------------------------------------
    // 同一キーは１個だけとする。
    public CharArray put(CharArray key, CharArray data) {
        //super.remove(key);
        CharArray ch = super.get(key); // 2014-12-03
        if (ch != null) {
            ch.set(data);
            return ch;
        }
        return (CharArray)super.put(key,data);
    }
    
    /*
    public synchronized Object remove(Object key) {
        Object value = super.remove(key);
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
    */

    /** 指定データを削除する */
    public CharArray remove(String str) {
        CharArray ch = CharArray.pop(str);
        CharArray ret = remove(ch);
        CharArray.push(ch);
        return ret;
    }
    public HashParameter removes(String... strs) {
        CharArray ch = CharArray.pop();
        for (int i = 0; i <strs.length; i++) {
            ch.set(strs[i]);
            super.remove(ch);
        }
        CharArray.push(ch);
        return this;
    }
    
    /** 指定データを削除する */
    public CharArray remove(CharArray ch) {
        return (CharArray)super.remove(ch);
    }
    /** 複数データを削除する */
    public HashParameter removes(CharArray... chs) {
        super.remove((Object[])chs);
        return this;
    }
    
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(String key, String value) {
        clear();
        return add(key,value);
    }
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(CharArray key, String value) {
        clear();
        return add(key,value);
    }
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(String key, CharArray value) {
        clear();
        return add(key,value);
    }
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(CharArray key, CharArray value) {
        clear();
        return add(key,value);
    }
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(String key, CharSequence value) {
        clear();
        return add(key,value);
    }
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(CharArray key, CharSequence value) {
        clear();
        return add(key,value);
    }
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(String key, int value) {
        clear();
        return add(key,value);
    }
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(CharArray key, int value) {
        clear();
        return add(key,value);
    }
    
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(String key, long value) {
        clear();
        return add(key,value);
    }
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(CharArray key, long value) {
        clear();
        return add(key,value);
    }
    
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(String key, double value) {
        clear();
        return add(key,value);
    }
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(CharArray key, double value) {
        clear();
        return add(key,value);
    }
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(String key, boolean value) {
        clear();
        return add(key,value);
    }
    /** 
        データを設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter set(CharArray key, boolean value) {
        clear();
        return add(key,value);
    }
    /** Queueテーブルの先頭行を設定する */
    public HashParameter set(QueueTable table) {
        return  set(table,0);
    }
    /** Queueテーブルの指定行を設定する */
    public HashParameter set(QueueTable table, int row) {
        clear();
        return  add(table,row);
    }
    
    /** 
        文字列からまとめて設定する
        @param str 文字列
        @param sep セパレータ  = 等
        @param connect 接続文字 , 等
        @return 自分自身
    */
    public HashParameter set(CharArray str, String sep, String connect) {
        clear();
        return  add(str,sep,connect);
    }
    /** 
        文字列からまとめて設定する
        @param str 文字列
        @param sep セパレータ  = 等
        @param connect 接続文字 , 等
        @return 自分自身
    */
    public HashParameter set(String str, String sep, String connect) {
        clear();
        return  add(str,sep,connect);
    }
    //--
    
    /** 
        データを追加設定する 
        @param str キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(String str, String value) {
       CharArray key = new CharArray(str);
       CharArray data = new CharArray(value);
       this.put(key,data);
       return this;
    }
    /** 
        データを追加設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(CharArray key, String value) {
       CharArray data = new CharArray(value);
       this.put(key,data);
       return this;
    }
    /** 
        データを追加設定する 
        @param str キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(String str, CharArray value) {
       CharArray key = new CharArray(str);
       CharArray data = new CharArray(value);
       this.put(key,data);
       return this;
    }
    /** 
        データを追加設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(CharArray key, CharArray value) {
       CharArray data = new CharArray(value);
       this.put(key,data);
       return this;
    }
    /** 
        データを追加設定する 
        @param str キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(String str, CharSequence value) {
       CharArray key = new CharArray(str);
       CharArray data = new CharArray(value);
       this.put(key,data);
       return this;
    }
    /** 
        データを追加設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(CharArray key, CharSequence value) {
       CharArray data = new CharArray(value);
       this.put(key,data);
       return this;
    }
    /** 
        データを追加設定する 
        @param str キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(String str, int value) {
       CharArray key = new CharArray(str);
       CharArray data = new CharArray();
       data.type = Queue.INT;
       data.format(value);
       this.put(key,data);
       return this;
    }
    /** 
        データを追加設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(CharArray key, int value) {
       CharArray data = new CharArray();
       data.type = Queue.INT;
       data.format(value);
       this.put(key,data);
       return this;
    }
    
    /** 
        データを追加設定する 
        @param str キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(String str, long value) {
       CharArray key = new CharArray(str);
       CharArray data = new CharArray();
       data.type = Queue.LONG;
       data.format(value);
       this.put(key,data);
       return this;
    }
    /** 
        データを追加設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(CharArray key, long value) {
       CharArray data = new CharArray();
       data.type = Queue.LONG;
       data.format(value);
       this.put(key,data);
       return this;
    }
    
    /** 
        データを追加設定する 
        @param str キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(String str, double value) {
       CharArray key = new CharArray(str);
       CharArray data = new CharArray(""+value);
       data.type = Queue.DOUBLE;
       this.put(key,data);
       return this;
    }
    /** 
        データを追加設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(CharArray key, double value) {
       CharArray data = new CharArray(""+value);
       data.type = Queue.DOUBLE;
       this.put(key,data);
       return this;
    }
    /** 
        データを追加設定する 
        @param str キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(String str, boolean value) {
       CharArray key = new CharArray(str);
       CharArray data = new CharArray(value ? "1" : "0");
       this.put(key,data);
       return this;
    }
    /** 
        データを追加設定する 
        @param key キー値
        @param value データ値
        @return 自分自身
    */
    public HashParameter add(CharArray key, boolean value) {
       clear();
       CharArray data = new CharArray(value ? "1" : "0");
       data.type = Queue.BOOL;
       this.put(key,data);
       return this;
    }
    /** Queueテーブルの先頭行を追加する */
    public HashParameter add(QueueTable table) {
        return add(table,0);
    }
    /** Queueテーブルの指定行を追加する */
    public HashParameter add(QueueTable table, int row) {
        if (row < table.getRowCount()) {
            for (int i = 0; i < table.getColumnCount(); i++) {
                CharArray key = table.getColumnKey(i);
                CharArray data = table.getCharArray(row,i);
                data.type = table.getColumnType(i);
                this.put(new CharArray(key), new CharArray(data));
            }
        }
        return this;
    }
    
    /** 
        文字列からまとめて追加する
        @param str 文字列
        @param sep セパレータ  = 等
        @param connect 接続文字 , 等
        @return 自分自身
    */
    public HashParameter add(CharArray str, String sep, String connect) {
        CharToken token = CharToken.pop();
        CharArray key = CharArray.pop();
        CharArray data = CharArray.pop();
        token.set(str, connect);
        for (int j = 0; j < token.size(); j++) {
            CharArray ch = token.get(j);
            int index = ch.indexOf(sep);
            if (index < 0) {
                key.set(ch).trim();
                data.set("true");  // boolean 値をセット
            } else {
                key.set(ch,0,index).trim();
                data.set(ch,index+sep.length()).trim();
            }
            put(new CharArray(key),new CharArray(data));
        }
        CharArray.push(data);
        CharArray.push(key);
        CharToken.push(token);
        return this;
    }
    /** 
        文字列からまとめて追加する
        @param str 文字列
        @param sep セパレータ  = 等
        @param connect 接続文字 , 等
        @return 自分自身
    */
    public HashParameter add(String str, String sep, String connect) {
        CharArray ch = CharArray.pop(str);
        add(ch,sep,connect);
        CharArray.push(ch);
        return this;
    }
    
    //-----------------------------------------------------
    // データの取得
    //-----------------------------------------------------
    /**
        キー値をリスト形式で取得する
        @return キー値のリスト
    **/
    public CharArrayQueue getKeyParameter() {
        CharArrayQueue queue = new CharArrayQueue();
        Vector<CharArray> v = keysVector();
        for (int i = 0; i < v.size(); i++) {
            CharArray ch = (CharArray)v.get(i);
            queue.enqueue(new CharArray(ch));
        }
        return queue;
    }
    /**
        データをリスト形式で取得する
        @return データのリスト
    **/
    public CharArrayQueue getParameter() {
        CharArrayQueue queue = new CharArrayQueue();
        Vector<CharArray> v = valuesVector();
        for (int i = 0; i < v.size(); i++) {
            CharArray ch = (CharArray)v.get(i);
            queue.enqueue(new CharArray(ch));
        }
        return queue;
    }

    /** 
        要素の取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public CharArray get(int index) {
        return (CharArray)valueElementAt(index);
    }
    /** 
        要素のCharArray型での取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public CharArray getCharArray(int index) {
        return (CharArray)valueElementAt(index);
    }
    /** 
        要素のint型での取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public int getInt(int index) {
        CharArray ch = get(index);
        if (ch == null) return 0;
        return ch.getInt();
    }
    /** 
        要素のlong型での取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public long getLong(int index) {
        CharArray ch = get(index);
        if (ch == null) return 0;
        return ch.getLong();
    }
    /** 
        要素の論理型での取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public boolean getBoolean(int index) {
        CharArray ch = get(index);
        if (ch == null) return false;
        return ch.getBoolean();
    }
    /** 
        要素のdouble型での取得 
        @param index 取得する場所(0-
        @return 取得した値
    */
    public double getDouble(int index) {
        CharArray ch = get(index);
        if (ch == null) return 0;
        return ch.getDouble();
    }
    
    /** 要素の取得 
        @param key キーワード
        @return 取得した値
    */
    public CharArray get(CharArray key) {
        CharArray ch = (CharArray)super.get(key);
        if (ch == null && debugKey) System.out.println("key["+key+"] not found !");
        return ch;
    }
    /** 要素のCharArray型での取得 
        @param key キーワード
        @return 取得した値
    */
    public CharArray getCharArray(CharArray key) {
        CharArray ch = (CharArray)super.get(key);
        if (ch == null && debugKey) System.out.println("key["+key+"] not found !");
        return ch;
    }
    
    /** 要素のint型での取得 
        @param key キーワード
        @return 取得した値
    */
    public int getInt(CharArray key) {
        CharArray ch = get(key);
        if (ch == null) return 0;
        return ch.getInt();
    }
    /** 要素のlong型での取得 
        @param key キーワード
        @return 取得した値
    */
    public long getLong(CharArray key) {
        CharArray ch = get(key);
        if (ch == null) return 0;
        return ch.getLong();
    }
    /** 要素の論理型での取得 
        @param key キーワード
        @return 取得した値
    */
    public boolean getBoolean(CharArray key) {
        CharArray ch = get(key);
        if (ch == null) return false;
        return ch.getBoolean();
    }
    /** 要素のdouble型での取得 
        @param key キーワード
        @return 取得した値
    */
    public double getDouble(CharArray key) {
        CharArray ch = get(key);
        if (ch == null) return 0;
        return ch.getDouble();
    }
    //---
    /** 要素の取得 
        @param key キーワード
        @return 取得した値
    */
    public CharArray get(String key) {
        CharArray chKey = CharArray.pop(key);
        CharArray ch = (CharArray)super.get(chKey);
        CharArray.push(chKey);
        return ch;
    }
    /** 要素のCharArray型での取得 
        @param key キーワード
        @return 取得した値
    */
    public CharArray getCharArray(String key) {
        return get(key);
    }
    
    /** 要素のint型での取得 
        @param key キーワード
        @return 取得した値
    */
    public int getInt(String key) {
        CharArray ch = get(key);
        if (ch == null) return 0;
        return ch.getInt();
    }
    /** 要素のlong型での取得 
        @param key キーワード
        @return 取得した値
    */
    public long getLong(String key) {
        CharArray ch = get(key);
        if (ch == null) return 0;
        return ch.getLong();
    }
    /** 要素の論理型での取得 
        @param key キーワード
        @return 取得した値
    */
    public boolean getBoolean(String key) {
        CharArray ch = get(key);
        if (ch == null) return false;
        return ch.getBoolean();
    }
    /** 要素のdouble型での取得 
        @param key キーワード
        @return 取得した値
    */
    public double getDouble(String key) {
        CharArray ch = get(key);
        if (ch == null) return 0;
        return ch.getDouble();
    }
    
    /** 要素の日付型での取得 
        @param key キーワード
        @return 取得日付文字列
    */
    public java.sql.Date getDate(String key) {
        return new java.sql.Date(getLong(key));
    }
    
    /** 指定フォーマットで日付文字列を取得する 
        @param key キーワード
        @param format フォーマット
        @return 取得日付文字列
    */
    public String getDateString(String key,String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        java.util.Date date = getDate(key);
        return df.format(date);
    }

    /** 指定フォーマットで日付文字列を取得する 
        @param key キーワード
        @param dateFormat 日付フォーマット
        @return 取得日付文字列
    */
    public String getDateString(String key, DateFormat dateFormat) {
        java.util.Date date = getDate(key);
        return dateFormat.format(date);
    }

    /** デフォルトのフォーマットで日付文字列を取得する 
        @param key キーワード
        @return 取得日付文字列
    */
    public String getDateString(String key) {
        java.util.Date date = getDate(key);
        String str = null;
        synchronized (sdf) {
            str = sdf.format(date);
        }
        return str;
    }
    
    /**
        内容のデバッグ表示
    **/
    public void debugParameter() {
        debugParameter(0, size(), null, false, null);
    }
    public void debugParameter(String[] match) {
        debugParameter(0, size(), null, false, null, null, match);
    }
    public void debugParameter(UserLog log) {
        debugParameter(0, size(), null, false, null, log);
    }
    public void debugParameter(boolean mode) {
        debugParameter(0, size(), null, mode, null);
    }
    public void debugParameter(String pre) {
        debugParameter(0, size(), null, false, pre);
    }
    public void debugParameter(String pre, String[] match) {
        debugParameter(0, size(), null, false, pre, null, match);
    }
    public void debugParameter(String pre, UserLog log) {
        debugParameter(0, size(), null, false, pre, log);
    }
    public void debugParameter(SessionObject session) {
        debugParameter(0, size(), session, false, null);
    }
    public void debugParameter(SessionObject session, boolean mode) {
        debugParameter(0, size(), session, mode, null);
    }
    public void debugParameter(int index, int size) {
        debugParameter(index, size, null, false, null);
    }
    public void debugParameter(int index, int size, boolean mode) {
        debugParameter(index, size, null, mode, null);
    }
    public void debugParameter(int index, int size, SessionObject session) {
        debugParameter(index, size, session, false, null);
    }
    public void debugParameter(int index, int size, SessionObject session, boolean mode) {
         debugParameter(index, size, session, mode, null);
    }
    public void debugParameter(int index, int size, SessionObject session, boolean mode, String pre) {
        debugParameter(index, size, session, mode, pre, null);
    }
    public void debugParameter(int index, int size, SessionObject session, boolean mode, String pre, UserLog log) {
        debugParameter(index, size, session, mode, pre, log, null);
    }
    public void debugParameter(int index, int size, SessionObject session, boolean mode, String pre, UserLog log, String[] match) {
        if (pre == null) pre = "";
        Vector<CharArray> keys = keysVector();
        int n = 0;
        String sz = pre+((session!= null) ? session.count+"|" : "");
        for (int i = index; i < size(); i++) {
            CharArray key = (CharArray)keys.get(i);
            if (match != null) {
                if (key.indexOf(match) < 0) continue;
            }
            CharArray data = get(key);
            if (mode) {
                if (log != null) log.info(sz+"["+i+"]"+key+"\t["+data+"]\t"+data.getAppendMode());
                else System.out.println(sz+"["+i+"]"+key+"\t["+data+"]\t"+data.getAppendMode());
            } else {
                if (log != null) log.info(sz+"["+i+"]"+key+"\t["+data+"]");
                else System.out.println(sz+"["+i+"]"+key+"\t["+data+"]");
            }
            
            if (++n >= size) break;
        }
    }
    
    /**
      文字列（カンマ区切り等）からインスタンスを生成する
      @param str 文字列
      @return HashParameterインスタンス
    **/
    static public HashParameter getInstance(CharArray str) {
        return getInstance(str, "=", ",");
    }
    /**
      文字列（カンマ区切り等）からインスタンスを生成する
      @param str 文字列
      @return HashParameterインスタンス
    **/
    static public HashParameter getInstance(String str) {
        return getInstance(str, "=", ",");
    }
    /**
      文字列（カンマ区切り等）からインスタンスを生成する
      @param str 文字列
      @param sep セパレータ（デフォルト=)
      @param connect コネクタ（デフォルト,)
    **/
    static public HashParameter getInstance(String str, String sep, String connect) {
        CharArray ch = CharArray.pop(str);
        HashParameter param = getInstance(ch, sep, connect);
        CharArray.push(ch);
        return param;
    }
    /**
      文字列（カンマ区切り等）からインスタンスを生成する
      @param str 文字列
      @param sep セパレータ（デフォルト=)
      @param connect コネクタ（デフォルト,)
    **/
    static public HashParameter getInstance(CharArray str, String sep, String connect) {
        HashParameter param = new HashParameter();
        param.set(str,sep,connect);
        return param;
    }
    
    private int hash = 0;   // ハッシュコードを保管しておく
    
    /** ハッシュコードを返す 
        @return ハッシュコード
    */
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            CharArray ch = CharArray.pop("");
            for (int i = 0; i < size(); i++) {
                CharArray key   = keyElementAt(i);
                CharArray value = valueElementAt(i);
                ch.add(key);
                ch.add('=');
                ch.add(value);
                ch.add(';');
            }
            h = ch.hashCode();
            hash = h;
            CharArray.push(ch);
        }
        return h;
    }
    
    /**
        利用メモリサイズを取得する
    */
    public int sizeOf() {
        int size = 0;
        for (int i = 0; i < size(); i++) {
            CharArray key   = keyElementAt(i);
            CharArray value = valueElementAt(i);
            if (key != null)  size += key.sizeOf();
            if (value != null) size += value.sizeOf();
        }
        return size;
    }
    
    
    public HashMap<String,String> getHashMap() {
        HashMap<String,String> hm = new HashMap<String,String>();
        for (int i = 0; i < size(); i++) {
            CharArray key   = keyElementAt(i);
            CharArray value = valueElementAt(i);
            hm.put(key.toString(),value.toString());
        }
        return hm;
    }
    
    
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        //super.writeObject(out);
        if (out != null) {
            Vector<CharArray> keys = keysVector();
            int size = size();
            out.writeInt(size);
            for (int i = 0; i < size; i++) {
                CharArray key = (CharArray)keys.get(i);
                CharArray data = get(key);
                key.writeObject(out);
                data.writeObject(out);
            }
        }
    }
    public  void readObject(DataInput in) throws IOException {
        //super.readObject(in);
        if (in != null) {
            clear();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                CharArray key = new CharArray();
                CharArray data = new CharArray();
                key.readObject(in);
                data.readObject(in);
                put(key, data);
            }
        }
    }
    
}

//
// [end of HashParameter.java]
//

