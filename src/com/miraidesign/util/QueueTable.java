//------------------------------------------------------------------------
//    QueueTable.java
//              テーブル形式のデータを保管する
//              Copyright (c) Mirai Design 2010-2015 All Rights Reserved. 
//------------------------------------------------------------------------
//      

package com.miraidesign.util;

import com.miraidesign.util.Queue;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.sql.Types;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.table.AbstractTableModel;

import com.miraidesign.session.SessionObject;

/**
 *  テーブル形式のデータを保管する<br>
 *  カラム毎に(Int/Long/CharArray/Bolean)Queueのデータとして保管される
 *  @version 0.6 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class QueueTable extends AbstractTableModel {
    private static final boolean debug = false;
    private static final boolean debugRowsQueue = false;
    private static final boolean debugSystemLock = false;
    private static final boolean debugIndex = false;
    private static final boolean debugMerge = false;
    private static final boolean debugSort = false;
    
    private boolean debugColumn = true;
    /**
        カラムが無い時のデバッグ表示設定
        @param mode true:デバッグ表示をする
        @return 設定前のモード
    */
    public boolean setDebugColumn(boolean mode) { 
        boolean ret = debugColumn;
        debugColumn = mode;
        return ret;
    }
    /**
        カラムが無い時のデバッグ表示モードを返す
        @return true:デバッグ表示をする
    */
    public boolean isDebugColumn() { return debugColumn;}
    
    public static int KEY = 1;
    public static int NAME = 2;
    public static int ALL = 3;
    
    private CharArray   title    = new CharArray();      // テーブル名
    private CharArray   comment  = new CharArray();      // コメント
    private CharArrayQueue names = new CharArrayQueue(); // カラム表示名リスト
    
    
    private IntQueue    types   = new IntQueue(16);     // カラムタイプリスト
    private ObjectQueue columns = new ObjectQueue(16);  // カラムオブジェクトリスト
    
    private Hashtable<CharArray,IntObject> hashColumn   = 
        new Hashtable<CharArray,IntObject>();     // カラム名（キー）のハッシュ
                                // key:  CharArray カラムキー名
                                // data: IntObject カラム番号(0-)
    
    private CharArrayQueue keys  = new CharArrayQueue(); // カラムキー名リスト
    private IntQueue       params = new IntQueue();      // カラム番号が入っている
    
    /*debug*/ //public Hashtable getHash() { return hash;}
    /*debug*/ //public CharArrayQueue getKeys() { return keys;}
    /*debug*/ //public IntQueue getParams() { return params;}
    //-------------------------------------------------------------------------
    // インデックス処理用
    private ObjectQueue indexQueue = new ObjectQueue(16);   //インデックスを保管する
                        // Hashtable  or null
                                                            
                                                            
    private ObjectQueue rowsQueue = new ObjectQueue();      //行番号の参照を保管する
                                                            // 中身はIntObject
    /** デフォルトのフォーマッタ */
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /** 日付フォーマッタのデフォルトを変更する 
        @param  str 変更パターン デフォルト："yyyy-MM-dd HH:mm:ss"
    */
    public void setDefaultDateFormatString(String str) {
        sdf.applyPattern(str);
    }
    // 共通データ 2012/04-23
    private HashParameter commonData = new HashParameter();

    //--------------------------------
    // コンストラクタ
    //--------------------------------
    /** QueueTableオブジェクトを生成する */
    public QueueTable() { }
    
    /** copy constructor */
    public QueueTable(QueueTable from) {
        copy(from);
    }
    /* コピー */
    public QueueTable copy(QueueTable from) {
        title.set(from.title);
        comment.set(from.comment);
        names.copy2(from.names);
        types.copy(from.types);
        
        columns.clear();

        for (int i = 0; i < from.columns.size(); i++) {
            Queue fromQueue = (Queue)from.columns.peek(i);
            Queue toQueue   =  Queue.create(fromQueue);
            columns.enqueue(toQueue);
        }
        hashColumn.clear(); keys.clear(); params.clear();
        
        for (int i = 0; i < from.hashColumn.size(); i++) {
//        for (int i = 0; i < from.columns.size(); i++) {
            //CharArray ch =  from.keys.peek(i);   やめて見る
            CharArray ch =  new CharArray(from.keys.peek(i));
            IntObject obj = new IntObject(from.params.peek(i));
            hashColumn.put(ch,obj);
            keys.enqueue(new CharArray(ch));
            params.enqueue(obj.getValue());
        }
        // インデックス関係のコピー   あやしい
        indexQueue.copy(from.indexQueue);   
        rowsQueue.copy(from.rowsQueue);
        return this;
    }
    /**
        コピー
        @param from コピー元
        @param start コピー開始行数
        @param size  コピーする行数
        @return this
    **/
    public QueueTable copy(QueueTable from, int start, int size) {
        copyStructure(from);
        for (int j = start; j < from.getRowCount() && j < start+size; j++) {
            addRow();
            for (int i = 0; i < from.columns.size(); i++) {
                CharArray ch = from.get(j,i);
                setCharArray(ch, j-start,i);
            }
        }
        return this;
    }

    /** 構造のコピー（データはコピーしない） 
        @param from コピー元
        @return this
    */
    public QueueTable copyStructure(QueueTable from) {
        title.set(from.title);
        comment.set(from.comment);
        names.copy(from.names);
        types.copy(from.types);
//System.out.println("types.size="+types.size());
        types.clear();
        columns.clear();
        for (int i = 0; i < from.columns.size(); i++) {
            addColumn(from.getColumnType(i));
        }
        hashColumn.clear(); keys.clear(); params.clear();
        for (int i = 0; i < from.hashColumn.size(); i++) {
//        for (int i = 0; i < from.columns.size(); i++) {
            CharArray ch =  from.keys.peek(i);
            IntObject obj = new IntObject(from.params.peek(i));
            hashColumn.put(ch,obj);
            keys.enqueue(ch);
            params.enqueue(obj.getValue());
        }
        return this;
    }
    /** 構造のコピー（データはコピーしない） 
        @param rs ResultSet
        @return this
        @throws SQLException 取得エラー
    */
    public QueueTable copyStructure(ResultSet rs) throws SQLException {
        clearAll();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 0; i < meta.getColumnCount(); i++) {
            String name    = meta.getColumnName(i+1);
            int type       = meta.getColumnType(i+1);
            switch (type) {
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                addColumn(Queue.DATE,name.toUpperCase());
                break;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.NUMERIC:
            case Types.DECIMAL:
                addColumn(Queue.INT,name.toUpperCase());
                break;
            case Types.BIGINT:
                addColumn(Queue.LONG,name.toUpperCase());
                break;
            case Types.BIT:
                addColumn(Queue.BOOL,name.toUpperCase());
                break;
            case Types.REAL:
            case Types.DOUBLE:
            case Types.FLOAT:
                addColumn(Queue.DOUBLE,name.toUpperCase());
                break;
            default:
                addColumn(Queue.STRING,name.toUpperCase());
                break;
            }
        }
        return this;
    }
    
    /** 構造の追加<br>
         タイトル、名称、コメント、データはコピーしない。<br>
         同一キーの場合は追加しない。同一キーで型が違う場合は、エラーにする。
        @param from 追加する構造を持つテーブル
        @return true:  成功
    */
    public boolean addStructure(QueueTable from) {
        if (from == null) return false;
        for (int i = 0; i < from.columns.size(); i++) {
            CharArray key = from.getColumnKey(i);
            int from_type = from.getColumnType(key);
            
            int type = getColumnType(key);
            if (type >= 0 && type != from_type) { //同一キーで別タイプが存在する
                System.out.println("addStructure:key["+key+"]type error! "+
                                    Queue.szTypes[type]+"<->"+Queue.szTypes[from_type]);
                
                return false;
            }
        }
        for (int i = 0; i < from.columns.size(); i++) {
            CharArray key = from.getColumnKey(i);
            int from_type = from.getColumnType(key);
            int type = getColumnType(key);
            if (type < 0) {    // 存在しない時のみ追加する
                int index = from.getColumnIndex(key);
                String name = from.getColumnName(index);  // null を追加しないため
                addColumn(from_type, key, name);
            }
        }
        return true;
    }
    
    /*
        テーブルを追加する。カラムが違う場合は、事前に追加する
        追加カラムが同一キーで型が違う場合は、エラーにする。（追加されない）
        @param from 追加するテーブル
        @return true:  成功
    */
    public boolean addTable(QueueTable from) {
        if (!addStructure(from)) return false;
        for (int row = 0; row < from.getRowCount(); row++) {
            boolean sts = addTable(from, row);
            if (!sts) System.out.println("table row:"+row+" 追加エラー");
        }
        return true;
    }
    //--------------------------------
    // 
    //--------------------------------
    /** テーブル名をセットする 
        @param ch テーブル名
    */
    public void setTitle(CharArray ch) { title.set(ch); }
    public void setTitle(String s) { title.set(s); }
    
    /* カラム名バッファを取得する */
    public CharArrayQueue getColumnNameQueue() {
        return names;
    }
    /* カラム名をクリアする */
    public void clearColumnName() {
        names.clear();
    }
    /* カラム名をセットする */
    public void addColumnName(String name) {names.enqueue(name);}
    public void addColumnName(CharArray name) {names.enqueue(name);}
    /* カラム名をセットする */
    public void setColumnName(CharArray name, String column_key) {
        int index = getColumnIndex(column_key);
        if (index >= 0) {
            while (names.size() < index + 1) names.enqueue("");
            names.peek(index).set(name);
        }
    }
    public void setColumnName(String name, String column_key) {
        int index = getColumnIndex(column_key);
        if (index >= 0) {
            while (names.size() < index + 1) names.enqueue("");
            names.peek(index).set(name);
        }
    }
    
    /* コメントを設定する */
    public void setComment(CharArray ch) { comment.set(ch); }
    public void setComment(String s) { comment.set(s); }
    public void addComment(CharArray ch) { comment.add(ch); }
    public void addComment(String s) { comment.add(s); }
    
    /* テーブル名を取得する */
    public CharArray getTitle() { return title; }
    
    /* コメントを取得する */
    public CharArray getComment() { return comment; }
    
    /* 共通データを取得する */
    public HashParameter getCommonData() { return commonData;}
    
    
    /** 全てのデータとカラムをクリアする 
        （タイトル、コメント、共通データはクリアされません)
    */
    public synchronized void clearAll() {
        names.clear();
        columns.clear();
        types.clear();
        hashColumn.clear();
        keys.clear();
        types.clear();
        indexQueue.clear(); 
        rowsQueue.clear();
    }
    /** 呼び出さない事 */
    public void reset() {
        for (int i = 0; i < columns.size(); i++) {
if (debugSystemLock) System.out.print("c1("+i+")");
            Queue queue =  (Queue)columns.peek(i);
            if (queue instanceof CharArrayQueue) {
                ((CharArrayQueue)queue).reset(0);
            }
if (debugSystemLock) System.out.print("c2("+i+")");
        }
if (debugSystemLock) System.out.print("c3");
        clearAll();
if (debugSystemLock) System.out.print("c4");
    }
    /** 全データ（行）をクリアする (カラム情報はそのまま)*/
    public synchronized void clear() {
        for (int i = 0; i < columns.size(); i++) {
            ((Queue)columns.peek(i)).clear();
        }
        clearIndex();
        setTimeStamp(0);    // 2013-05-22
    }
    
    /** 
        指定行を削除する
        @param row 削除する行
        @return false:パラメータエラー
    */
    public synchronized boolean removeRow(int row) {
        int size = getRowCount();
        if (0 < size && size > row) {
            for (int i = 0; i < columns.size(); i++) {
                ((Queue)columns.peek(i)).remove(row);
            }
            rowsQueue.remove(row);
            reindex();
            return true;
        }
        return false;
    }
    /**
        Column を追加する
        @param  type カラムタイプ (Queue.INT/LONG/BOOL/DOUBLE/STRING/BYTES/OBJECT?/DATE)
        @return false:パラメータエラー
    */
    public synchronized boolean addColumn(int type) {
        boolean rsts = true;
        if (type >= Queue.MIN && type <= Queue.MAX) {
            types.enqueue(type);
            columns.enqueue(Queue.create(type));
            int count = getRowCount();  // addRow
            if (count > 0) {
                int i = columns.size() - 1;
                Queue queue = (Queue)columns.peek(i);
                switch (getColumnType(i)) {
                  case Queue.INT:
                    for (int j = 0; j < count; j++) ((IntQueue)queue).enqueue(0);
                    break;
                  case Queue.LONG:
                    for (int j = 0; j < count; j++) ((LongQueue)queue).enqueue(0L);
                    break;
                  case Queue.BOOL:
                    for (int j = 0; j < count; j++) ((BooleanQueue)queue).enqueue(false);
                    break;
                  case Queue.DOUBLE:
                    for (int j = 0; j < count; j++) ((DoubleQueue)queue).enqueue(0.0);
                    break;
                  case Queue.STRING:
                    for (int j = 0; j < count; j++) ((CharArrayQueue)queue).enqueue("");
                    break;
                  case Queue.OBJECT:  //@@OBJE
                    for (int j = 0; j < count; j++) ((ObjectQueue)queue).enqueue((Object)null);
                    break;
                  case Queue.BYTES:
                    for (int j = 0; j < count; j++) ((ByteArrayQueue)queue).enqueue(new ByteArray());
                    break;
                  case Queue.DATE:
                    for (int j = 0; j < count; j++) ((LongQueue)queue).enqueue(0L);
                    break;
                  default:
                    break;
                }
            }
            indexQueue.enqueue((Object)null);
        } else {
            System.out.println("addColumn type="+type+"は指定できません");
            rsts = false;
        }
        return rsts;
    }
    /**
        Column を追加する
        @param type カラムタイプ (Queue.INT/LONG/BOOL/DOUBLE/STRING/OBJECT?/BYTES/DATE)
        @param key  カラムキー （カラム名など)
        @return true:成功
    */
    public synchronized boolean addColumn(int type, CharArray key) {
        boolean rsts = addColumn(type);
        //if (rsts) hash.put(name, columns.peek(columns.size()-1));
        if (rsts) {
            int i = columns.size()-1;
            IntObject value = new IntObject(i);
            hashColumn.put(key, value);
            keys.enqueue(key);
            names.enqueue("");
            params.enqueue(i);
            indexQueue.enqueue((Object)null);
        }
        return rsts;
    }
    /**
        Column を追加する
        @param type カラムタイプ (Queue.INT/LONG/BOOL/DOUBLE/STRING/OBJECT?/BYTES/DATE)
        @param key  カラムキー （カラム名など)
        @param name カラム表示名
        @return true:成功
    **/
    public synchronized boolean addColumn(int type, CharArray key, CharArray name) {
        boolean rsts = addColumn(type);
        //if (rsts) hash.put(name, columns.peek(columns.size()-1));
        if (rsts) {
            int i = columns.size()-1;
            IntObject value = new IntObject(i);
            hashColumn.put(key, value);
            keys.enqueue(key);
            names.enqueue(name);
            params.enqueue(i);
            indexQueue.enqueue((Object)null);
        }
        return rsts;
    }
    public boolean addColumn(int type, String key) {
        return addColumn(type, new CharArray(key));
    }
    public boolean addColumn(int type, String key, String name) {
        return addColumn(type, new CharArray(key), new CharArray(name));
    }
    public boolean addColumn(int type, CharArray key, String name) {
        return addColumn(type, key, new CharArray(name));
    }
    public boolean addColumn(int type, String key, CharArray name) {
        return addColumn(type, new CharArray(key), name);
    }
    /*
    public synchronized void addColumn(int type, String name, boolean isIndex) {
    }
    public synchronized void addColumn(int type, boolean isIndex) {
    }
    */
    
    /* カラムの削除 */
    public synchronized void removeColumn(int index) {
        columns.remove(index);
        types.remove(index);
        indexQueue.remove(index);
    }
    
    /**
        Column を取得する
        @param index カラム(0-
        @return Queue
    */
    public Queue getColumn(int index) {
        return (Queue)columns.peek(index);
    }
    public Queue getColumn(CharArray key) {
        IntObject obj = (IntObject)hashColumn.get(key);
        if (obj == null) {
            if (debugColumn) System.out.println("column:"+key+" not found!");
            return null;
        }
        return getColumn(obj.getValue());
    }
    
    /** カラムタイプを取得する
        @param index カラム(0-
        @return カラムタイプ
    */
    public int getColumnType(int index) {
        return types.peek(index);
    }
    
    /** カラムタイプを取得する
        @param column_key カラムキーワード
        @return カラムタイプ
    */
    public int getColumnType(CharArray column_key) {
        int index = getColumnIndex(column_key);
        if (index < 0 ) return index;
        return types.peek(index);
    }
    /** カラムタイプを取得する
        @param column_key カラムキーワード
        @return カラムタイプ
    */
    public int getColumnType(String column_key) {
        int index = getColumnIndex(column_key);
        if (index < 0 ) return index;
        return types.peek(index);
    }
    
    /** カラムタイプ名を取得する
        @param index カラム(0-
        @return カラムタイプ名
    */
    public String getColumnTypeName(int index) {
         return Queue.szTypes[types.peek(index)];
    }
    
    /* カラム名を取得する (TableModel用) */
    public String getColumnName(int index) {
        CharArray ch =  getColumnCharName(index);
        return (ch == null) ? "" : ch.toString();
    }
    
    /* カラム名を取得する (通常はこれを使ってください) 
    */
    public CharArray getColumnCharName(int index) {
        return names.peek(index);
    }
    
    /* カラムキー名（DBカラム）を取得する  */
    public CharArray getColumnKey(int index) {
        return keys.peek(index);
    }
    
    /** カラムキーワード名からカラムインデックス（０－）を取得する 
        @param column_key カラムキーワード
        @return カラムインデックス -1:なし
    */
    public int getColumnIndex(CharArray column_key) {
        int index = -1;
        IntObject obj = (IntObject)hashColumn.get(column_key);
        if (obj != null) index = obj.getValue();
        else if (debugColumn) System.out.println("column["+column_key+"] not found !");
        return index;
    }
    public int getColumnIndex(String column_key) {
        CharArray key = CharArray.pop(column_key);
        int index = -1;
        IntObject obj = (IntObject)hashColumn.get(key);
        if (obj != null) index = obj.getValue();
        else if (debugColumn) System.out.println("column["+key+"] not found !");
        CharArray.push(key);
        return index;
    }
    
    /* カラム数を取得する */
    public int getColumnCount() {
        return columns.size();
    }
    
    /* 行数を取得する */
    public int getRowCount() {
        if (columns.size() > 0) {
            return ((Queue)columns.peek(0)).size();
        }
        return 0;
    }
    
    //--------------------------------
    // 値の取得
    //--------------------------------
    /* 値を返す (TableModel用)*/
    public Object getValueAt(int row, int column) {
        Object obj = null;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    obj = new IntObject(((IntQueue)queue).peek(row));
                    break;
                case Queue.LONG:
                    obj = new LongObject(((LongQueue)queue).peek(row));
                    break;
                case Queue.BOOL:
                    obj = new Boolean(((BooleanQueue)queue).peek(row));
                    break;
                case Queue.DOUBLE:
                    obj = new Double(((DoubleQueue)queue).peek(row));
                    break;
                case Queue.STRING:
                    obj = ((CharArrayQueue)queue).peek(row);
                    break;
                case Queue.OBJECT:
                    obj = ((ObjectQueue)queue).peek(row);
                    break;
                case Queue.BYTES:
                    obj = ((ByteArrayQueue)queue).peek(row);
                    break;
                case Queue.DATE:
                    long l = ((LongQueue)queue).peek(row);
                    if (l != -1) obj = new LongObject(l);
                    break;
                default:
                    break;
            }
        }
        return obj;
    }
    
    /* 指定カラムの値を返す */
    public int getInt(int row, int column) {
        int rsts = 0;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    rsts  = ((IntQueue)queue).peek(row);
                    break;
                case Queue.LONG:
                    long l = ((LongQueue)queue).peek(row);
                    rsts = (int)l;
                    break;
                case Queue.BOOL:
                    boolean b = ((BooleanQueue)queue).peek(row);
                    rsts = b ? 1 : 0;
                    break;
                case Queue.DOUBLE:
                    double d = ((DoubleQueue)queue).peek(row);
                    rsts = (int)Math.round(d);
                    break;
                case Queue.STRING:
                    CharArray ch = ((CharArrayQueue)queue).peek(row);
                    rsts = ch.getInt();
                    break;
                case Queue.BYTES:
                    ByteArray by = ((ByteArrayQueue)queue).peek(row);
                    rsts = by.getInt();
                    break;
                case Queue.DATE:
                    long ld = ((LongQueue)queue).peek(row);
                    rsts = (int)ld;
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public int getInt(int row, CharArray column_key) {
        return getInt(row, getColumnIndex(column_key));
    }
    public int getInt(int row, String column_key) {
        return getInt(row, getColumnIndex(column_key));
    }

    /* 指定カラムの値を返す */
    public long getLong(int row, int column) {
        long rsts = 0;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    rsts  = ((IntQueue)queue).peek(row);
                    break;
                case Queue.LONG:
                    rsts = ((LongQueue)queue).peek(row);
                    break;
                case Queue.BOOL:
                    boolean b = ((BooleanQueue)queue).peek(row);
                    rsts = b ? 1 : 0;
                    break;
                case Queue.DOUBLE:
                    double d = ((DoubleQueue)queue).peek(row);
                    rsts = Math.round(d);
                    break;
                case Queue.STRING:
                    CharArray ch = ((CharArrayQueue)queue).peek(row);
                    rsts = ch.getLong();
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    ByteArray by = ((ByteArrayQueue)queue).peek(row);
                    rsts = by.getLong();
                    break;
                case Queue.DATE:
                    rsts = ((LongQueue)queue).peek(row);
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public long getLong(int row, CharArray column_key) {
        return getLong(row, getColumnIndex(column_key));
    }
    public long getLong(int row, String column_key) {
        return getLong(row, getColumnIndex(column_key));
    }

    /* 指定カラムの値（Date型）を返す 
        カラムが(LONG/DATE) の時のみ有効
    */
    public java.sql.Date getDate(int row, int column) {
        java.sql.Date date = null;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    //rsts  = ((IntQueue)queue).peek(row);
                    break;
                case Queue.LONG:
                    date = new java.sql.Date(((LongQueue)queue).peek(row));
                    break;
                case Queue.BOOL:
                    //boolean b = ((BooleanQueue)queue).peek(row);
                    //rsts = b ? 1 : 0;
                    break;
                case Queue.DOUBLE:
                    //double d = ((DoubleQueue)queue).peek(row);
                    //rsts = Math.round(d);
                    break;
                case Queue.STRING:
                    CharArray ch = ((CharArrayQueue)queue).peek(row);
                    date = java.sql.Date.valueOf(ch.toString());
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    break;
                case Queue.DATE:
                    long l = ((LongQueue)queue).peek(row);
                    if (l != -1) date = new java.sql.Date(l);
                    break;
                default:
                    break;
            }
        }
        return date;
    }
    public java.sql.Date getDate(int row, CharArray column_key) {
        return getDate(row, getColumnIndex(column_key));
    }
    public java.sql.Date getDate(int row, String column_key) {
        return getDate(row, getColumnIndex(column_key));
    }
    
    /* 指定カラムの値（Date型）を指定されたフォーマットで返す
        カラムが(LONG/DATE) の時のみ有効
        @param format フォーマット文字列
    */
    public String getDateString(int row, int column,String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        java.util.Date date = getDate(row,column);
        return (date != null) ? df.format(date) : "";
    }
    public String getDateString(int row, CharArray column_key,String format) {
        return getDateString(row, getColumnIndex(column_key), format);
    }
    public String getDateString(int row, String column_key,String format) {
        return getDateString(row, getColumnIndex(column_key), format);
    }
    /* 指定カラムの値（Date型）を指定されたフォーマットで返す
        カラムが(LONG/DATE) の時のみ有効
        @param format フォーマット文字列
        @param timezone タイムゾーン文字列
    */
    public String getDateString(int row, int column,String format, String timezone) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        java.util.Date date = getDate(row,column);
        return (date != null) ? df.format(date) : "";
    }
    public String getDateString(int row, CharArray column_key,String format, String timezone) {
        return getDateString(row, getColumnIndex(column_key), format, timezone);
    }
    public String getDateString(int row, String column_key,String format, String timezone) {
        return getDateString(row, getColumnIndex(column_key), format, timezone);
    }
    
    /* 指定カラムの値（Date型）を指定されたフォーマットで返す
        @param df 日付フォーマッター
    */
    public String getDateString(int row, int column,DateFormat df) {
        java.util.Date date = getDate(row,column);
        return (date != null) ? df.format(date) : "";
    }
    public String getDateString(int row, CharArray column_key, DateFormat df) {
        return getDateString(row, getColumnIndex(column_key), df);
    }
    public String getDateString(int row, String column_key, DateFormat df) {
        return getDateString(row, getColumnIndex(column_key), df);
    }
    
    /* 指定カラムの値（Date型）をデフォルトのフォーマットで返す
    */
    public String getDateString(int row, int column) {
        java.util.Date date = getDate(row,column);
//System.out.println("row:"+row+"column:"+column+ " date:"+(date!=null)+" L:"+getLong(row,column)+" C"+getCharArray(row,column));
        if (date == null) return "";
        String str = null;
        synchronized (sdf) {
            str = sdf.format(date);
        }
        return str;
    }
    public String getDateString(int row, CharArray column_key) {
        return getDateString(row, getColumnIndex(column_key));
    }
    public String getDateString(int row, String column_key) {
        return getDateString(row, getColumnIndex(column_key));
    }
    
    /* 指定カラムの値を返す */
    public boolean getBoolean(int row, int column) {
        boolean rsts = false;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    int i = ((IntQueue)queue).peek(row);
                    rsts = (i != 0);
                    break;
                case Queue.LONG:
                    long l = ((LongQueue)queue).peek(row);
                    rsts = (l != 0);
                    break;
                case Queue.BOOL:
                    rsts = ((BooleanQueue)queue).peek(row);
                    break;
                case Queue.DOUBLE:
                    double d = ((DoubleQueue)queue).peek(row);
                    rsts = (Math.round(d) != 0);
                    break;
                case Queue.STRING:
                    CharArray ch = ((CharArrayQueue)queue).peek(row);
                    rsts = ch.getBoolean();
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    ByteArray by = ((ByteArrayQueue)queue).peek(row);
                    rsts = by.getBoolean();
                    break;
                case Queue.DATE:
                    long ld = ((LongQueue)queue).peek(row);
                    rsts = (ld != -1);
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public boolean getBoolean(int row, CharArray column_key) {
        return getBoolean(row, getColumnIndex(column_key));
    }
    public boolean getBoolean(int row, String column_key) {
        return getBoolean(row, getColumnIndex(column_key));
    }
    
    /* 指定カラムの値を返す */
    public double getDouble(int row, int column) {
        double rsts = 0;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    rsts  = ((IntQueue)queue).peek(row);
                    break;
                case Queue.LONG:
                    rsts = ((LongQueue)queue).peek(row);
                    break;
                case Queue.BOOL:
                    boolean b = ((BooleanQueue)queue).peek(row);
                    rsts = b ? 1 : 0;
                    break;
                case Queue.DOUBLE:
                    rsts = ((DoubleQueue)queue).peek(row);
                    break;
                case Queue.STRING:
                    CharArray ch = ((CharArrayQueue)queue).peek(row);
                    rsts = ch.getDouble();
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    ByteArray by = ((ByteArrayQueue)queue).peek(row);
                    rsts = by.getDouble();
                    break;
                case Queue.DATE:
                    rsts = ((LongQueue)queue).peek(row);
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public double getDouble(int row, CharArray column_key) {
        return getDouble(row, getColumnIndex(column_key));
    }
    public double getDouble(int row, String column_key) {
        return getDouble(row, getColumnIndex(column_key));
    }
    
    /* 指定カラムの値を返す（Object） */
    public Object getObject(int row, int column) {
        Object rsts = null;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    break;
                case Queue.LONG:
                    break;
                case Queue.BOOL:
                    break;
                case Queue.DOUBLE:
                    break;
                case Queue.STRING:
                    break;
                case Queue.OBJECT:
                    rsts = ((ObjectQueue)queue).peek(row);
                    break;
                case Queue.BYTES:
                    break;
                case Queue.DATE:
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }

    public Object getObject(int row, CharArray column_key) {
        return getObject(row, getColumnIndex(column_key));
    }
    public Object getObject(int row, String column_key) {
        return getObject(row, getColumnIndex(column_key));
    }
    
    /* 指定カラムの値を返す（ByteArray） */
    public ByteArray getByteArray(int row, int column) {
        ByteArray rsts = null;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    break;
                case Queue.LONG:
                    break;
                case Queue.BOOL:
                    break;
                case Queue.DOUBLE:
                    break;
                case Queue.STRING:
                    break;
                case Queue.OBJECT:
                    break;
                case Queue.BYTES:
                    rsts = ((ByteArrayQueue)queue).peek(row);
                    break;
                case Queue.DATE:
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }

    public ByteArray getByteArray(int row, CharArray column_key) {
        return getByteArray(row, getColumnIndex(column_key));
    }
    public ByteArray getByteArray(int row, String column_key) {
        return getByteArray(row, getColumnIndex(column_key));
    }
    
    /* 指定カラムの値を返す（文字型） */
    public CharArray getCharArray(int row, int column) {
        CharArray rsts = null;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    rsts = new CharArray().format(((IntQueue)queue).peek(row));
                    break;
                case Queue.LONG:
                    rsts = new CharArray().format(((LongQueue)queue).peek(row));
                    break;
                case Queue.BOOL:
                    boolean b = ((BooleanQueue)queue).peek(row);
                    rsts = new CharArray().format(b ? 1 : 0);
                    break;
                case Queue.DOUBLE:
                    rsts = new CharArray(""+((DoubleQueue)queue).peek(row));
                    break;
                case Queue.STRING:
                    rsts = ((CharArrayQueue)queue).peek(row);
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    //rsts = ((ByteArrayQueue)queue).peek(row);
                    break;
                case Queue.DATE:
                    rsts = new CharArray();
                    long l = ((LongQueue)queue).peek(row);
                    if (l == -1) rsts.set((String)null);
                    else         rsts.format(l);
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }

    public CharArray getCharArray(int row, CharArray column_key) {
        return getCharArray(row, getColumnIndex(column_key));
    }
    public CharArray getCharArray(int row, String column_key) {
        return getCharArray(row, getColumnIndex(column_key));
    }
    public CharArray get(int row, int column) {
        return getCharArray(row, column);
    }
    public CharArray get(int row, CharArray column_key) {
        return getCharArray(row, getColumnIndex(column_key));
    }
    public CharArray get(int row, String column_key) {
        return getCharArray(row, getColumnIndex(column_key));
    }
    
    /* 指定カラムの値を返す（文字型） ※あまり使用しない事 */
    public String getString(int row, int column) {
        String rsts = null;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    rsts = ""+((IntQueue)queue).peek(row);
                    break;
                case Queue.LONG:
                    rsts = ""+((LongQueue)queue).peek(row);
                    break;
                case Queue.BOOL:
                    boolean b = ((BooleanQueue)queue).peek(row);
                    rsts = b ? "1" : "0";
                    break;
                case Queue.DOUBLE:
                    rsts = ""+((DoubleQueue)queue).peek(row);
                    break;
                case Queue.STRING:
                    rsts = ((CharArrayQueue)queue).peek(row).toString();
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    //rsts = ((ByteArrayQueue)queue).peek(row);
                    break;
                case Queue.DATE:
                    long l = ((LongQueue)queue).peek(row);
                    if (l != -1) rsts = ""+l;
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public String getString(int row, CharArray column_key) {
        return getString(row, getColumnIndex(column_key));
    }
    public String getString(int row, String column_key) {
        return getString(row, getColumnIndex(column_key));
    }

    /* 指定カラムの値を返す（int配列） */
    public IntQueue getIntQueue(int row, int column) {
        CharArray ch = get(row, column);
        IntQueue iq = new IntQueue();
        return iq.addFromCSV(ch);
    
    }
    public IntQueue getIntQueue(int row, CharArray column_key) {
        return getIntQueue(row, getColumnIndex(column_key));
    }
    public IntQueue getIntQueue(int row, String column_key) {
        return getIntQueue(row, getColumnIndex(column_key));
    }
    
    /* 指定カラムの値を返す（lonng配列） */
    public LongQueue getLongQueue(int row, int column) {
        CharArray ch = get(row, column);
        LongQueue lq = new LongQueue();
        return lq.addFromCSV(ch);
    
    }
    public LongQueue getLongQueue(int row, CharArray column_key) {
        return getLongQueue(row, getColumnIndex(column_key));
    }
    public LongQueue getLongQueue(int row, String column_key) {
        return getLongQueue(row, getColumnIndex(column_key));
    }
    
    //------------------------------------------------------------
    // 値の設定
    //------------------------------------------------------------
    /* 値を設定する (TableModel) 未実装 */
    public void setValueAt(Object value, int row, int column) {
    }
    public void setValueAt(Object value, int row, String column_key) {
        setValueAt(value, row, getColumnIndex(column_key));
    }
    public void setValueAt(Object value, int row, CharArray column_key) {
        setValueAt(value, row, getColumnIndex(column_key));
    }
    
    /* 値を設定する */
    public boolean setInt(int value, int row, int column) {
        boolean rsts = false;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    int org  = ((IntQueue)queue).peek(row);
                    rsts = ((IntQueue)queue).poke(row,value);
                    reindex(row,column,org,value);
                    break;
                case Queue.LONG:
                    long orgL  = ((LongQueue)queue).peek(row);
                    rsts = ((LongQueue)queue).poke(row,(long)value);
                    reindex(row,column,orgL,(long)value);
                    break;
                case Queue.BOOL:
                    rsts = ((BooleanQueue)queue).poke(row, (value != 0));
                    /* 未対応 */
                    break;
                case Queue.DOUBLE:
                    rsts = ((DoubleQueue)queue).poke(row, (double)value);
                    break;
                case Queue.STRING:
                    CharArray ch = ((CharArrayQueue)queue).peek(row);
                    if (ch != null) {
                        ch.reset();
                        ch.format(value);
                        rsts = true;
                    }
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    ByteArray by = ((ByteArrayQueue)queue).peek(row);
                    if (by != null) {
                        by.reset();
                        by.setInt(value);
                        rsts = true;
                    }
                    /* 未対応 */
                    break;
                case Queue.DATE:
                    long orgDT  = ((LongQueue)queue).peek(row);
                    rsts = ((LongQueue)queue).poke(row,(long)value);
                    reindex(row,column,orgDT,(long)value);
                default:
                    break;
            }
        }
        return rsts;
    }
    public boolean setInt(int value, int row, String column_key) {
        return setInt(value, row, getColumnIndex(column_key));
    }
    public boolean setInt(int value, int row, CharArray column_key) {
        return setInt(value, row, getColumnIndex(column_key));
    }
    
    /* 値を設定する */
    public boolean setLong(long value, int row, int column) {
        boolean rsts = false;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    int orgI = ((IntQueue)queue).peek(row);
                    rsts = ((IntQueue)queue).poke(row,(int)value);
                    reindex(row,column,orgI,(int)value);
                    break;
                case Queue.LONG:
                    long orgL = ((LongQueue)queue).peek(row);
                    rsts = ((LongQueue)queue).poke(row,value);
                    reindex(row,column,orgL,value);
                    break;
                case Queue.BOOL:
                    rsts = ((BooleanQueue)queue).poke(row, (value != 0));
                    break;
                case Queue.DOUBLE:
                    rsts = ((DoubleQueue)queue).poke(row, (double)value);
                    break;
                case Queue.STRING:
                    CharArray ch = ((CharArrayQueue)queue).peek(row);
                    if (ch != null) {
                        ch.reset();
                        ch.format(value);
                        rsts = true;
                    }
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    ByteArray by = ((ByteArrayQueue)queue).peek(row);
                    if (by != null) {
                        by.reset();
                        by.setLong(value);
                        rsts = true;
                    }
                    break;
                case Queue.DATE:
                    long orgDT = ((LongQueue)queue).peek(row);
                    rsts = ((LongQueue)queue).poke(row,value);
                    reindex(row,column,orgDT,value);
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public boolean setLong(long value, int row, String column_key) {
        return setLong(value, row, getColumnIndex(column_key));
    }
    public boolean setLong(long value, int row, CharArray column_key) {
        return setLong(value, row, getColumnIndex(column_key));
    }

    /* 値(Date型）を設定する 
        カラムが(LONG/DATE)でない時はエラーとする
    */
    public boolean setDate(java.sql.Date date, int row, int column) {
        boolean rsts = false;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            long value = date.getTime();
            switch (getColumnType(column)) {
                case Queue.INT:
                    //rsts = ((IntQueue)queue).poke(row,(int)value);
                    break;
                case Queue.LONG:
                    long orgL = ((LongQueue)queue).peek(row);
                    rsts = ((LongQueue)queue).poke(row,value);
                    reindex(row,column,orgL,value);
                    break;
                case Queue.BOOL:
                    //rsts = ((BooleanQueue)queue).poke(row, (value != 0));
                    break;
                case Queue.DOUBLE:
                    //rsts = ((DoubleQueue)queue).poke(row, (double)value);
                    break;
                case Queue.STRING:
                    rsts = ((CharArrayQueue)queue).poke(row,date.toString());
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    //
                    break;
                case Queue.DATE:
                    long orgDT = ((LongQueue)queue).peek(row);
                    rsts = ((LongQueue)queue).poke(row,value);
                    reindex(row,column,orgDT,value);
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public boolean setDate(java.sql.Date date, int row, String column_key) {
        return setDate(date, row, getColumnIndex(column_key));
    }
    public boolean setDate(java.sql.Date date, int row, CharArray column_key) {
        return setDate(date, row, getColumnIndex(column_key));
    }

    /* 値を設定する */
    public boolean setBoolean(boolean value, int row, int column) {
        boolean rsts = false;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    rsts = ((IntQueue)queue).poke(row,value ? 1 : 0);
                    break;
                case Queue.LONG:
                    rsts = ((LongQueue)queue).poke(row,value ? 1L : 0L);
                    break;
                case Queue.BOOL:
                    rsts = ((BooleanQueue)queue).poke(row, value);
                    break;
                case Queue.DOUBLE:
                    rsts = ((DoubleQueue)queue).poke(row, value ? 1.0 : 0.0);
                    break;
                case Queue.STRING:
                    CharArray ch = ((CharArrayQueue)queue).peek(row);
                    if (ch != null) {
                        ch.reset();
                        ch.format(value? 1 : 0);
                        rsts = true;
                    }
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    ByteArray by = ((ByteArrayQueue)queue).peek(row);
                    if (by != null) {
                        by.reset();
                        by.setBoolean(value);
                        rsts = true;
                    }
                    break;
                case Queue.DATE:
                    rsts = ((LongQueue)queue).poke(row,value ? 1L : 0L);
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public boolean setBoolean(boolean value, int row, String column_key) {
        return setBoolean(value, row, getColumnIndex(column_key));
    }
    public boolean setBoolean(boolean value, int row, CharArray column_key) {
        return setBoolean(value, row, getColumnIndex(column_key));
    }
    
    /* 値を設定する */
    public boolean setDouble(double value, int row, int column) {
        boolean rsts = false;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    rsts = ((IntQueue)queue).poke(row,(int)Math.round(value));
                    break;
                case Queue.LONG:
                    rsts = ((LongQueue)queue).poke(row, Math.round(value));
                    break;
                case Queue.BOOL:
                    rsts = ((BooleanQueue)queue).poke(row, (Math.round(value) != 0L));
                    break;
                case Queue.DOUBLE:
                    rsts = ((DoubleQueue)queue).poke(row, value);
                    break;
                case Queue.STRING:
                    CharArray ch = ((CharArrayQueue)queue).peek(row);
                    if (ch != null) {
                        ch.set(""+value);
                        rsts = true;
                    }
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    ByteArray by = ((ByteArrayQueue)queue).peek(row);
                    if (by != null) {
                        by.reset();
                        by.setDouble(value);
                        rsts = true;
                    }
                    break;
                case Queue.DATE:
                    rsts = ((LongQueue)queue).poke(row, Math.round(value));
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public boolean setDouble(double value, int row, String column_key) {
        return setDouble(value, row, getColumnIndex(column_key));
    }
    public boolean setDouble(double value, int row, CharArray column_key) {
        return setDouble(value, row, getColumnIndex(column_key));
    }

    /* 値を設定する (Object)*/
    public boolean setObject(Object value, int row, int column) {
        boolean rsts = false;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    break;
                case Queue.LONG:
                    break;
                case Queue.BOOL:
                    break;
                case Queue.DOUBLE:
                    break;
                case Queue.STRING:
                    break;
                case Queue.OBJECT:
                    rsts = ((ObjectQueue)queue).poke(row, value);
                    break;
                case Queue.BYTES:
                    break;
                case Queue.DATE:
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public boolean setObject(Object value, int row, CharArray column_key) {
        return setObject(value, row, getColumnIndex(column_key));
    }
    public boolean setObject(Object value, int row, String column_key) {
        return setObject(value, row, getColumnIndex(column_key));
    }

    /* 値を設定する (ByteArray)*/
    public boolean setByteArray(ByteArray value, int row, int column) {
        boolean rsts = false;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    break;
                case Queue.LONG:
                    break;
                case Queue.BOOL:
                    break;
                case Queue.DOUBLE:
                    break;
                case Queue.STRING:
                    break;
                case Queue.OBJECT:
                    //rsts = ((ObjectQueue)queue).poke(row, value9;
                    break;
                case Queue.BYTES:
                    if (value == null) value = new ByteArray();
                    rsts = ((ByteArrayQueue)queue).poke(row, value);
                
                    break;
                case Queue.DATE:
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public boolean setByteArray(ByteArray value, int row, CharArray column_key) {
        return setByteArray(value, row, getColumnIndex(column_key));
    }
    public boolean setByteArray(ByteArray value, int row, String column_key) {
        return setByteArray(value, row, getColumnIndex(column_key));
    }

    /* 値を設定する */
    public boolean setCharArray(CharArray value, int row, int column) {
        boolean rsts = false;
        Queue queue = getColumn(column);
        if (queue != null && queue.size() > 0 && queue.size() > row) {
            switch (getColumnType(column)) {
                case Queue.INT:
                    rsts = ((IntQueue)queue).poke(row,(value==null) ? 0 : value.getInt());
                    break;
                case Queue.LONG:
                    rsts = ((LongQueue)queue).poke(row,(value==null) ? 0L : value.getLong());
                    break;
                case Queue.BOOL:
                    rsts = ((BooleanQueue)queue).poke(row, (value==null)? false:value.getBoolean());
                    break;
                case Queue.DOUBLE:
                    rsts = ((DoubleQueue)queue).poke(row, (value==null)? 0.0 :value.getDouble());
                    break;
                case Queue.STRING:
                    CharArray ch = ((CharArrayQueue)queue).peek(row);
                    if (ch != null) {
                        reindex(row,column,ch,value);
                        ch.set(value);
                        rsts = true;
                    }
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                
                    break;
                case Queue.DATE:
                    rsts = ((LongQueue)queue).poke(row,(value==null) ? 0L : value.getLong());
                    break;
                default:
                    break;
            }
        }
        return rsts;
    }
    public boolean setCharArray(CharArray value, int row, CharArray column_key) {
        return setCharArray(value, row, getColumnIndex(column_key));
    }
    public boolean setCharArray(CharArray value, int row, String column_key) {
        return setCharArray(value, row, getColumnIndex(column_key));
    }
    
    /* 値を設定する */
    public boolean setString(String str, int row, int column) {
        CharArray ch = CharArray.pop(str);
        boolean rsts = setCharArray(ch, row, column);
        CharArray.push(ch);
        return rsts;
    }
    public boolean setString(String str, int row, CharArray column_key) {
        return setString(str, row, getColumnIndex(column_key));
    }
    public boolean setString(String str, int row, String column_key) {
        return setString(str, row, getColumnIndex(column_key));
    }
    public boolean setString(CharArray ch, int row, int column) {
        return setCharArray(ch, row, column);
    }
    public boolean setString(CharArray str, int row, CharArray column_key) {
        return setString(str, row, getColumnIndex(column_key));
    }
    public boolean setString(CharArray str, int row, String column_key) {
        return setString(str, row, getColumnIndex(column_key));
    }
    //--------------------------------
    // Element
    //--------------------------------
    /* Element（1行のデータ)を取得する */
    public QueueElement getElement(int row) {
        QueueElement element = new QueueElement();
        for (int i = 0; i < types.size(); i++) {
            Queue queue = getColumn(i);
            switch (types.peek(i)) {
                case Queue.INT:
                    element.add(((IntQueue)queue).peek(row));
                    break;
                case Queue.LONG:
                    element.add(((LongQueue)queue).peek(row));
                    break;
                case Queue.BOOL:
                    element.add(((BooleanQueue)queue).peek(row));
                    break;
                case Queue.DOUBLE:
                    element.add(((DoubleQueue)queue).peek(row));
                    break;
                case Queue.STRING:
                    element.add(((CharArrayQueue)queue).peek(row));
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    /* 未対応 */
                    break;
                case Queue.DATE:
                    element.add(((LongQueue)queue).peek(row));
                    break;
                default:
                    break;
            }
        }
        return element;
    }
    /* １行を設定する */
    public boolean setElement(int row, QueueElement element) {
        for (int i = 0; i < columns.size(); i++) {
            Queue queue = (Queue)columns.peek(i);
            switch (getColumnType(i)) {
                case Queue.INT: 
                    ((IntQueue)queue).poke(row,element.getInt(i));
                    break;
                case Queue.LONG:
                    ((LongQueue)queue).poke(row,element.getLong(i));
                    break;
                case Queue.BOOL:
                    ((BooleanQueue)queue).poke(row,element.getBoolean(i));
                    break;
                case Queue.DOUBLE:
                    ((DoubleQueue)queue).poke(row,element.getDouble(i));
                    break;
                case Queue.STRING:
                    ((CharArrayQueue)queue).poke(row,element.getCharArray(i));
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    /* 未対応 */
                    break;
                case Queue.DATE:
                    ((LongQueue)queue).poke(row,element.getLong(i));
                    break;
                default:
                    break;
            }
        }
        return true;
    }
    
    /* １行を追加する */
    public boolean addElement(QueueElement element) {
        int index = getRowCount();
        for (int i = 0; i < columns.size(); i++) {
            Queue queue = (Queue)columns.peek(i);
            switch (getColumnType(i)) {
                case Queue.INT:
                    ((IntQueue)queue).enqueue(element.getInt(i));
                    break;
                case Queue.LONG:
                    ((LongQueue)queue).enqueue(element.getLong(i));
                    break;
                case Queue.BOOL:
                    ((BooleanQueue)queue).enqueue(element.getBoolean(i));
                    break;
                case Queue.DOUBLE:
                    ((DoubleQueue)queue).enqueue(element.getDouble(i));
                    break;
                case Queue.STRING:
                    ((CharArrayQueue)queue).enqueue(element.getCharArray(i));
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    /* 未対応 */
                    break;
                case Queue.DATE:
                    ((LongQueue)queue).enqueue(element.getLong(i));
                    break;
                default:
                    break;
            }
        }
        refreshRowsQueue(index,1);
        return true;
    }
    
    /* １行を挿入する */
    public boolean insertElement(int index, QueueElement element) {
        for (int i = 0; i < columns.size(); i++) {
            Queue queue = (Queue)columns.peek(i);
            switch (getColumnType(i)) {
                case Queue.INT:
                    ((IntQueue)queue).insert(index,element.getInt(i));
                    break;
                case Queue.LONG:
                    ((LongQueue)queue).insert(index,element.getLong(i));
                    break;
                case Queue.BOOL:
                    ((BooleanQueue)queue).insert(index,element.getBoolean(i));
                    break;
                case Queue.DOUBLE:
                    ((DoubleQueue)queue).insert(index,element.getDouble(i));
                    break;
                case Queue.STRING:
                    ((CharArrayQueue)queue).insert(index,element.getCharArray(i));
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    /* 未対応 */
                    break;
                case Queue.DATE:
                    ((LongQueue)queue).insert(index,element.getLong(i));
                    break;
                default:
                    break;
            }
        }
        refreshRowsQueue(index,1);
        return true;
    }
    
    /* 空の１行を追加する */
    public boolean addElement() {
        return addRow();
    }
    public boolean addRow() {
        return addRow(1);
    }
    public boolean addRow(int count) {
        int index = getRowCount();
        for (int i = 0; i < columns.size(); i++) {
            Queue queue = (Queue)columns.peek(i);
            //Hashtable hash = (Hashtable)indexQueue.peek(i);
            switch (getColumnType(i)) {
                case Queue.INT:
                    for (int j = 0; j < count; j++) ((IntQueue)queue).enqueue(0);
                    break;
                case Queue.LONG:
                    for (int j = 0; j < count; j++) ((LongQueue)queue).enqueue(0L);
                    break;
                case Queue.BOOL:
                    for (int j = 0; j < count; j++) ((BooleanQueue)queue).enqueue(false);
                    break;
                case Queue.DOUBLE:
                    for (int j = 0; j < count; j++) ((DoubleQueue)queue).enqueue(0.0);
                    break;
                case Queue.STRING:
                    for (int j = 0; j < count; j++) ((CharArrayQueue)queue).enqueue("");
                    break;
                case Queue.OBJECT:
                    for (int j = 0; j < count; j++) ((ObjectQueue)queue).enqueue((Object)null);
                    break;
                case Queue.BYTES:
                    for (int j = 0; j < count; j++) ((ByteArrayQueue)queue).enqueue(new ByteArray());
                    break;
                case Queue.DATE:
                    for (int j = 0; j < count; j++) ((LongQueue)queue).enqueue(0L);
                    break;
                default:
                    break;
            }
        }
        refreshRowsQueue(index,count);
        return true;
    }

    /* 空の１行を挿入する */
    public boolean insertRow(int index) {
        for (int i = 0; i < columns.size(); i++) {
            Queue queue = (Queue)columns.peek(i);
            switch (getColumnType(i)) {
                case Queue.INT:
                    ((IntQueue)queue).insert(index,0);
                    break;
                case Queue.LONG:
                    ((LongQueue)queue).insert(index,0L);
                    break;
                case Queue.BOOL:
                    ((BooleanQueue)queue).insert(index,false);
                    break;
                case Queue.DOUBLE:
                    ((DoubleQueue)queue).insert(index,0.0);
                    break;
                case Queue.STRING:
                    ((CharArrayQueue)queue).insert(index,"");
                    break;
                case Queue.OBJECT:
                    ((ObjectQueue)queue).insert(index,(Object)null);
                    break;
                case Queue.BYTES:
                    ((ByteArrayQueue)queue).insert(index, new ByteArray());
                    break;
                case Queue.DATE:
                    ((LongQueue)queue).insert(index,0L);
                    break;
                default:
                    break;
            }
        }
        refreshRowsQueue(index,1);
        return true;
    }

    /* １行を追加する */
    public boolean addTable(QueueTable table, int row) {
        int index = getRowCount();
        for (int i = 0; i < columns.size(); i++) {
            Queue queue = (Queue)columns.peek(i);
            switch (getColumnType(i)) {
                case Queue.INT:
                    ((IntQueue)queue).enqueue(table.getInt(row,i));
                    break;
                case Queue.LONG:
                    ((LongQueue)queue).enqueue(table.getLong(row,i));
                    break;
                case Queue.BOOL:
                    ((BooleanQueue)queue).enqueue(table.getBoolean(row,i));
                    break;
                case Queue.DOUBLE:
                    ((DoubleQueue)queue).enqueue(table.getDouble(row,i));
                    break;
                case Queue.STRING:
                    ((CharArrayQueue)queue).enqueue(table.getCharArray(row,i));
                    break;
                case Queue.OBJECT:
                    ((ObjectQueue)queue).enqueue(table.getObject(row,i));
                    break;
                case Queue.BYTES:
                    /* 未対応 */
                    ((ByteArrayQueue)queue).enqueue(table.getByteArray(row,i));
                    break;
                case Queue.DATE:
                    ((LongQueue)queue).enqueue(table.getLong(row,i));
                    break;
                default:
                    break;
            }
        }
        refreshRowsQueue(index,1);
        return true;
    }

    /* HashParameter 形式で設定する */
    public void setHashParameter(int row, HashParameter hp) {
        for (int i = 0; i < hp.size(); i++) {
            CharArray key   = hp.keyElementAt(i);
            CharArray value = hp.valueElementAt(i);
            setCharArray(value, row, key);
        }
    }
    
    /* HashParameter 形式で追加する */
    public void addHashParameter(HashParameter hp) {
        int row = getRowCount();
        setHashParameter(row, hp);
    }
    
    /* HashParameter 形式で取得する */
    public HashParameter getHashParameter(int row) {
        HashParameter param = new HashParameter();
        param.add(this, row);
        return param;
    }


    /* １行をコピーする */
    public boolean copyTable(int index, QueueTable table, int row) {
        for (int i = 0; i < columns.size(); i++) {
            Queue queue = (Queue)columns.peek(i);
            switch (getColumnType(i)) {
                case Queue.INT:
                    ((IntQueue)queue).poke(index, table.getInt(row,i));
                    break;
                case Queue.LONG:
                    ((LongQueue)queue).poke(index, table.getLong(row,i));
                    break;
                case Queue.BOOL:
                    ((BooleanQueue)queue).poke(index, table.getBoolean(row,i));
                    break;
                case Queue.DOUBLE:
                    ((DoubleQueue)queue).poke(index, table.getDouble(row,i));
                    break;
                case Queue.STRING:
                    ((CharArrayQueue)queue).poke(index, table.getCharArray(row,i));
                    break;
                case Queue.OBJECT:
                    ((ObjectQueue)queue).poke(index, table.getObject(row,i));
                    break;
                case Queue.BYTES:
                    /* 未対応 */
                    ((ByteArrayQueue)queue).poke(index, table.getByteArray(row,i));
                    break;
                case Queue.DATE:
                    ((LongQueue)queue).poke(index, table.getLong(row,i));
                    break;
                default:
                    break;
            }
        }
        refreshRowsQueue(index,1);
        return true;
    }
    //-------------------------------------------------
    // ソート関連
    //-------------------------------------------------
    /** 指定カラムでソートする 
        @param szColumns カラム名の配列
        @return 成功
    */
    public boolean sort(String... szColumns) {
        return sort(szColumns, false);
    }
    /** 指定カラムでソートする(大文字小文字無視)
        @param szColumns カラム名の配列
        @return 成功
    */
    public boolean sorti(String... szColumns) {
        return sort(szColumns,true);
    }
    /** 指定カラムでソートする 
        @param szColumns カラム名の配列
        @param ignoreCase 大文字小文字を無視するか？
        @return 成功
    */
    public boolean sort(String[] szColumns, boolean ignoreCase) {
        if (szColumns == null || szColumns.length==0) return false;
        IntQueue iq = new IntQueue();
if (debugSort) System.out.print("sort(");
        for (int i = 0; i < szColumns.length; i++) {
if (debugSort) {
    if (i > 0) System.out.print(", ");
    System.out.print(szColumns[i]);
}
            iq.enqueue(getColumnIndex(szColumns[i]));
        }
if (debugSort) System.out.println(") ignoreCase="+ignoreCase);
        return sort(iq, ignoreCase);
    }
    
    /** 指定カラムでソートする 
        @param szColumns カラム名の配列
        @return 成功
    */
    public boolean sort(CharArray... szColumns) {
        return sort(szColumns, false);
    }
    /** 指定カラムでソートする(大文字小文字無視)
        @param szColumns カラム名の配列
        @return 成功
    */
    public boolean sorti(CharArray... szColumns) {
        return sort(szColumns,true);
    }
    /** 指定カラムでソートする 
        @param szColumns カラム名の配列
        @param ignoreCase 大文字小文字を無視するか？
        @return 成功
    */
    public boolean sort(CharArray[] szColumns, boolean ignoreCase) {
        if (szColumns == null || szColumns.length==0) return false;
        IntQueue iq = new IntQueue();
if (debugSort) System.out.print("sort(");
        for (int i = 0; i < szColumns.length; i++) {
if (debugSort) {
    if (i > 0) System.out.print(", ");
    System.out.print(szColumns[i]);
}
            iq.enqueue(getColumnIndex(szColumns[i]));
        }
if (debugSort) System.out.println(") ignoreCase="+ignoreCase);
        return sort(iq, ignoreCase);
    }
    
    /** 指定カラムで逆ソートする 
        @param szColumns カラム名の配列
        @return 成功
    */
    public boolean rsort(String... szColumns) {
        return rsort(szColumns, false);
    }
    /** 指定カラムで逆ソートする (大文字小文字無視)
        @param szColumns カラム名の配列
        @return 成功
    */
    public boolean rsorti(String... szColumns) {
        return rsort(szColumns, true);
    }
    /** 指定カラムで逆ソートする 
        @param szColumns カラム名の配列
        @param ignoreCase 大文字小文字を無視するか？
        @return 成功
    */
    public boolean rsort(String[] szColumns, boolean ignoreCase) {
        if (szColumns == null || szColumns.length==0) return false;
        IntQueue iq = new IntQueue();
        for (int i = 0; i < szColumns.length; i++) {
            iq.enqueue(getColumnIndex(szColumns[i]));
        }
        return rsort(iq, ignoreCase);
    }
    /** 指定カラムで逆ソートする 
        @param szColumns カラム名の配列
        @return 成功
    */
    public boolean rsort(CharArray... szColumns) {
        return rsort(szColumns, false);
    }
    /** 指定カラムで逆ソートする (大文字小文字無視)
        @param szColumns カラム名の配列
        @return 成功
    */
    public boolean rsorti(CharArray... szColumns) {
        return rsort(szColumns, true);
    }
    /** 指定カラムで逆ソートする 
        @param szColumns カラム名の配列
        @param ignoreCase 大文字小文字を無視するか？
        @return 成功
    */
    public boolean rsort(CharArray[] szColumns, boolean ignoreCase) {
        if (szColumns == null || szColumns.length==0) return false;
        IntQueue iq = new IntQueue();
        for (int i = 0; i < szColumns.length; i++) {
            iq.enqueue(getColumnIndex(szColumns[i]));
        }
        return rsort(iq, ignoreCase);
    }
    //--------------------------------------------------------
    /** 指定カラムでソートする 
        @param iColumns カラムインデックスの配列
        @return 成功
    */
    public boolean sort(int... iColumns) {
        return sort(iColumns, false);
    }
    /** 指定カラムでソートする (大文字小文字無視)
        @param iColumns カラムインデックスの配列
        @return 成功
    */
    public boolean sorti(int... iColumns) {
        return sort(iColumns, true);
    }
    /** 指定カラムでソートする 
        @param iColumns カラムインデックスの配列
        @param ignoreCase 大文字小文字を無視するか？
        @return 成功
    */
    public boolean sort(int[] iColumns, boolean ignoreCase) {
        IntQueue iq = new IntQueue();
        for (int i = 0; i < iColumns.length; i++) {
            iq.enqueue(iColumns[i]);
        }
        return sort(iq, ignoreCase);
    }
    /** 指定カラムでソートする 
        @param iq カラム配列
        @return 成功
    */
    public boolean sort(IntQueue iq) {
        return sort(iq, false);
    }
    /** 指定カラムでソートする (大文字小文字区別なし）)
        @param iq カラム配列
        @return 成功
    */
    public boolean sorti(IntQueue iq) {
        return sort(iq, true);
    }
    /** 指定カラムでソートする 
        @param iq カラムインデックスのリスト
        @param ignoreCase 大文字小文字を無視するか？
        @return 成功
    */
    public boolean sort(IntQueue iq, boolean ignoreCase) {
        if (iq == null || iq.size() ==0) return false;
        try {
            int column = iq.dequeue();  // 先頭カラムを抜き出す(queue が上にずれる）
            Queue queue = getColumn(column);
            if (queue != null && queue.size() > 0) {
                switch (getColumnType(column)) {
                    case Queue.INT:
                        qsort_i((IntQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    case Queue.LONG:
                        qsort_l((LongQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    case Queue.BOOL:
                        qsort_b((BooleanQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    case Queue.DOUBLE:
                        qsort_d((DoubleQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    case Queue.STRING:
                        qsort_s((CharArrayQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    case Queue.OBJECT:
                        // do nothing
                        break;
                    case Queue.BYTES:
                        /* 未対応 */
                        break;
                    case Queue.DATE:
                        qsort_l((LongQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    default:
                        break;
                }
                refreshRowsQueue(0,-1);
                return true;
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    //-- rsort
    /** 指定カラムで逆ソートする 
        @param iColumns カラムインデックスの配列
        @return 成功
    */
    public boolean rsort(int... iColumns) {
        return rsort(iColumns, false);
    }
    /** 指定カラムで逆ソートする (大文字小文字無視)
        @param iColumns カラムインデックスの配列
        @return 成功
    */
    public boolean rsorti(int... iColumns) {
        return rsort(iColumns, true);
    }
    /** 指定カラムで逆ソートする 
        @param iColumns カラムインデックスの配列
        @param ignoreCase 大文字小文字を無視するか？
        @return 成功
    */
    public boolean rsort(int[] iColumns, boolean ignoreCase) {
        IntQueue iq = new IntQueue();
        for (int i = 0; i < iColumns.length; i++) {
            iq.enqueue(iColumns[i]);
        }
        return rsort(iq, ignoreCase);
    }
    /** 指定カラムで逆ソートする 
        @param iq カラム配列
        @return 成功
    */
    public boolean rsort(IntQueue iq) {
        return rsort(iq, false);
    }
    /** 指定カラムで逆ソートする (大文字小文字区別なし）)
        @param iq カラム配列
        @return 成功
    */
    public boolean rsorti(IntQueue iq) {
        return sort(iq, true);
    }
    /** 指定カラムで逆ソートする 
        @param iq カラムインデックスの配列
        @param ignoreCase 大文字小文字を無視するか？
        @return 成功
    */
    public boolean rsort(IntQueue iq, boolean ignoreCase) {
        if (iq == null || iq.size() ==0) return false;
        try {
            int column = iq.dequeue();  // 先頭カラムを抜き出す(queue が上にずれる）
            Queue queue = getColumn(column);
            if (queue != null && queue.size() > 0) {
                switch (getColumnType(column)) {
                    case Queue.INT:
                        rqsort_i((IntQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    case Queue.LONG:
                        rqsort_l((LongQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    case Queue.BOOL:
                        rqsort_b((BooleanQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    case Queue.DOUBLE:
                        rqsort_d((DoubleQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    case Queue.STRING:
                        rqsort_s((CharArrayQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    case Queue.OBJECT:
                        // do nothing
                        break;
                    case Queue.BYTES:
                        /* 未対応 */
                        break;
                    case Queue.DATE:
                        rqsort_l((LongQueue)queue, 0, queue.size()-1, ignoreCase, iq);
                        break;
                    default:
                        break;
                }
                refreshRowsQueue(0,-1);
                return true;
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /* int のソート */
    private void qsort_i(IntQueue queue, int first, int last, boolean ignoreCase, IntQueue iq) {
        int m = (first+last)/2; // x の参照ポインタ
        int x = queue.peek(m);
        int i = first;
        int j = last;
//System.out.println("=============================================");
//System.out.println("qsort_i first:"+first+" last:"+last+" x["+m+"]="+x);
        while (true) {
//System.out.println("--while : i="+i+" j="+j+" x="+x+_get(m,iq));
//System.out.println("   i: peek("+i+")="+queue.peek(i)+_get(i,iq)+" ----------------");

            // マルチカラム対応
            while (queue.peek(i) <= x) {    // マルチカラム対応追加メソッド
                if (queue.peek(i) == x) {
                    int ret = qsort_sub(iq, i, m, ignoreCase);
                    if (ret < 0) {
                        ++i;
//System.out.println(" ++i*: peek("+i+")="+queue.peek(i)+_get(i,iq));
                    }
                    else  break;
                } else {
                    ++i;
//System.out.println(" ++i : peek("+i+")="+queue.peek(i)+_get(i,iq));
                }
            }

//System.out.println("   j: peek("+j+")="+queue.peek(j)+_get(j,iq)+" ----------------");
            while (x <= queue.peek(j)) {    // マルチカラム対応追加メソッド
                if (x == queue.peek(j)) {
                    int ret = qsort_sub(iq, m, j, ignoreCase);
                    if (ret < 0) {
                        --j;
//System.out.println(" --j*: peek("+j+")="+queue.peek(j)+_get(j,iq));
                    }
                    else   break;
                } else {
                    --j;
//System.out.println(" --j : peek("+j+")="+queue.peek(j)+_get(j,iq));
                }
            }

//String[] strs = {"IntNum", "ID"};
            if (i >= j) {
//System.out.println("  ● i["+i+"]"+queue.peek(i)+_get(i, iq)
//                   +" と j["+j+"]"+queue.peek(j)+_get(j, iq)
//                   +" を交換しない");
                break;
            }
            if (m == i) m = j; else if (m == j) m = i;  // 参照ポインタ変更
            
//System.out.println("  ○ i["+i+"]"+queue.peek(i)+_get(i, iq)
//                   +" と j["+j+"]"+queue.peek(j)+_get(j, iq)
//                   +" を交換する");
            for (int column = 0; column < columns.size(); column++) {
                getColumn(column).exchange(i,j);
            }
//dumpTable(strs, 8, i,j);
            
            i++;  j--;
        }
        if (first < i - 1) qsort_i(queue, first, i-1, ignoreCase, iq);
        if (j + 1 < last)  qsort_i(queue, j + 1, last, ignoreCase, iq);
    }
    private CharArray _get(int row, IntQueue iq) { // for debug
        CharArray ch = new CharArray();
        if (iq != null) {
            for (int i = 0; i < iq.size(); i++) {
                ch.add(":");
                ch.add(get(row, iq.peek(i)));
            }
        }
        return ch;
    }
    
    
    /*  複数カラム対応 */
    private int qsort_sub(IntQueue iq, int i, int j, boolean ignoreCase) {
        int compare = 0;
        if (iq == null) return 0;
        for (int k = 0; k < iq.size();  k++) {
            int column_index = iq.peek(k);
            if (column_index < 0) continue;
            switch (getColumnType(column_index)) {
                case Queue.INT:
                    int n1 = getInt(i, column_index);
                    int n2 = getInt(j, column_index);
                    if (n1 > n2) compare = 1;
                    else if (n1 < n2) compare = -1;
                    break;
                case Queue.LONG:
                case Queue.DATE:
                    long l1 = getLong(i, column_index);
                    long l2 = getLong(j, column_index);
                    if (l1 > l2) compare = 1;
                    else if (l1 < l2) compare = -1;
                    break;
                case Queue.BOOL:
                    int b1 = getBoolean(i, column_index) ? 1 : 0;
                    int b2 = getBoolean(j, column_index) ? 1 : 0;
                    if (b1 > b2) compare = 1;
                    else if (b1 < b2) compare = -1;
                    break;
                case Queue.DOUBLE:
                    double d1 = getDouble(i, column_index);
                    double d2 = getDouble(j, column_index);
                    if (d1 > d2) compare = 1;
                    else if (d1 < d2) compare = -1;
                    break;
                case Queue.STRING:
                    CharArray c = getCharArray(i, column_index);
                    CharArray c2 = getCharArray(j, column_index);
                    if (ignoreCase) {
                        compare = c.compareToIgnoreCase(c2);
                    } else {
                        compare = c.compareTo(c2);
                    }
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    /* 未対応 */
                    break;
                default:
                    break;
            }  // endcase
            if (compare != 0) break;
        }  // next
//System.out.println("   compare="+compare);
        return compare;
    }
    
    /* int の逆ソート */
    private void rqsort_i(IntQueue queue, int first, int last, boolean ignoreCase, IntQueue iq) {
        int m = (first+last)/2; // x の参照ポインタ
        int x = queue.peek(m);
        int i = first;
        int j = last;
//System.out.println("=============================================");
//System.out.println("rqsort_i first:"+first+" last:"+last+" x["+m+"]="+x);
        while (true) {
//System.out.println("--while : i="+i+" j="+j+" x="+x+_get(m,iq));
//System.out.println("   i: peek("+i+")="+queue.peek(i)+_get(i,iq)+" ----------------");
            while (queue.peek(i) >= x) {
                if (queue.peek(i) == x) {
                    int ret = qsort_sub(iq, i, m, ignoreCase);
                    if (ret < 0) {
                        i++;
//System.out.println(" ++i*: peek("+i+")="+queue.peek(i)+_get(i,iq));
                    } else break;
                } else {
//System.out.println(" ++i : peek("+i+")="+queue.peek(i)+_get(i,iq));
                    i++;
                }
            }
            while (x >= queue.peek(j)) {
                if (x == queue.peek(j)) {
                    int ret = qsort_sub(iq, m, j, ignoreCase);
                    if (ret < 0) {
                        j--;
//System.out.println(" --j*: peek("+j+")="+queue.peek(j)+_get(j,iq));
                    } else break;
                } else {
                    j--;
//System.out.println(" --j : peek("+j+")="+queue.peek(j)+_get(j,iq));
                }
            }
            if (i >= j) {
//System.out.println("  ● i["+i+"]"+queue.peek(i)+_get(i, iq)
//                   +" と j["+j+"]"+queue.peek(j)+_get(j, iq)
//                   +" を交換しない");
                break;
            }
            //i と ｊを交換
            if (m == i) m = j; else if (m == j) m = i;  // 参照ポインタ変更
//System.out.println("  ○ i["+i+"]"+queue.peek(i)+_get(i, iq)
//                   +" と j["+j+"]"+queue.peek(j)+_get(j, iq)
//                   +" を交換する");
            for (int column = 0; column < columns.size(); column++) {
                getColumn(column).exchange(i,j);
            }
            i++;  j--;
        }
        if (first < i - 1) rqsort_i(queue, first, i-1, ignoreCase, iq);
        if (j + 1 < last)  rqsort_i(queue, j + 1, last, ignoreCase, iq);
    }
    
    /* long のソート */
    private void qsort_l(LongQueue queue, int first, int last, boolean ignoreCase, IntQueue iq) {
        int m = (first+last)/2; // x の参照ポインタ
        long x = queue.peek(m);
        int i = first;
        int j = last;
        while (true) {
            // マルチカラム対応
            while (queue.peek(i) <= x) {
                if (queue.peek(i) == x) {
                    int ret = qsort_sub(iq, i, m, ignoreCase);
                    if (ret < 0) ++i;
                    else  break;
                } else ++i;
            }
            while (x <= queue.peek(j)) {
                if (x == queue.peek(j)) {
                    int ret = qsort_sub(iq, m, j, ignoreCase);
                    if (ret < 0) --j;
                    else   break;
                } else --j;
            }
            if (i >= j) break;
            if (m == i) m = j; else if (m == j) m = i;  // 参照ポインタ変更
            //i と ｊを交換
            for (int column = 0; column < columns.size(); column++) {
                getColumn(column).exchange(i,j);
            }
            i++;  j--;
        }
        if (first < i - 1) qsort_l(queue, first, i-1, ignoreCase, iq);
        if (j + 1 < last)  qsort_l(queue, j + 1, last, ignoreCase, iq);
    }

    /* long の逆ソート */
    private void rqsort_l(LongQueue queue, int first, int last, boolean ignoreCase, IntQueue iq)  {
        int m = (first+last)/2; // x の参照ポインタ
        long x = queue.peek(m);
        int i = first;
        int j = last;
        while (true) {
            while (queue.peek(i) >= x) {
                if (queue.peek(i) == x) {
                    int ret = qsort_sub(iq, i, m, ignoreCase);
                    if (ret < 0) i++; else break;
                } else i++;
            }
            while (x >= queue.peek(j)) {
                if (x == queue.peek(j)) {
                    int ret = qsort_sub(iq, m, j, ignoreCase);
                    if (ret < 0) j--; else break;
                } else j--;
            }
            if (i >= j) break;
            //i と ｊを交換
            if (m == i) m = j; else if (m == j) m = i;  // 参照ポインタ変更
            for (int column = 0; column < columns.size(); column++) {
                getColumn(column).exchange(i,j);
            }
            i++;  j--;
        }
        if (first < i - 1) rqsort_l(queue, first, i-1, ignoreCase, iq);
        if (j + 1 < last)  rqsort_l(queue, j + 1, last, ignoreCase, iq);
    }
    
    /* double のソート */
    private void qsort_d(DoubleQueue queue, int first, int last, boolean ignoreCase, IntQueue iq) {
        int m = (first+last)/2;
        double x = queue.peek(m);
        int i = first;
        int j = last;
        while (true) {
            // マルチカラム対応
            while (queue.peek(i) <= x) {
                if (queue.peek(i) == x) {
                    int ret = qsort_sub(iq, i, m, ignoreCase);
                    if (ret < 0) ++i;
                    else  break;
                } else ++i;
            }
            while (x <= queue.peek(j)) {
                if (x == queue.peek(j)) {
                    int ret = qsort_sub(iq, m, j, ignoreCase);
                    if (ret < 0) --j;
                    else   break;
                } else --j;
            }
            if (i >= j) break;
            if (m == i) m = j; else if (m == j) m = i;  // 参照ポインタ変更
            for (int column = 0; column < columns.size(); column++) {
                getColumn(column).exchange(i,j);
            }
            i++;  j--;
        }
        if (first < i - 1) qsort_d(queue, first, i-1, ignoreCase, iq);
        if (j + 1 < last)  qsort_d(queue, j + 1, last, ignoreCase, iq);
    }
    /* double の逆ソート */
    private void rqsort_d(DoubleQueue queue, int first, int last, boolean ignoreCase, IntQueue iq) {
        int m = (first+last)/2;
        double x = queue.peek(m);
        int i = first;
        int j = last;
        while (true) {
            while (queue.peek(i) >= x) {
                if (queue.peek(i) == x) {
                    int ret = qsort_sub(iq, i, m, ignoreCase);
                    if (ret < 0) i++; else break;
                } else i++;
            }
            while (x >= queue.peek(j)) {
                if (x == queue.peek(j)) {
                    int ret = qsort_sub(iq, m, j, ignoreCase);
                    if (ret < 0) j--; else break;
                } else j--;
            }
            if (i >= j) break;
            //i と ｊを交換
            if (m == i) m = j; else if (m == j) m = i;  // 参照ポインタ変更
            for (int column = 0; column < columns.size(); column++) {
                getColumn(column).exchange(i,j);
            }
            i++;  j--;
        }
        if (first < i - 1) rqsort_d(queue, first, i-1, ignoreCase, iq);
        if (j + 1 < last)  rqsort_d(queue, j + 1, last, ignoreCase, iq);
    }
    
    /* boolean のソート */
    private void qsort_b(BooleanQueue queue, int first, int last, boolean ignoreCase, IntQueue iq) {
        int m = (first+last)/2;
        int x = queue.getInt(m);
        int i = first;
        int j = last;
        while (true) {
            // マルチカラム対応
            while (queue.getInt(i) <= x) {
                if (queue.getInt(i) == x) {
                    int ret = qsort_sub(iq, i, m, ignoreCase);
                    if (ret < 0) ++i;
                    else  break;
                } else ++i;
            }
            while (x <= queue.getInt(j)) {
                if (x == queue.getInt(j)) {
                    int ret = qsort_sub(iq, m, j, ignoreCase);
                    if (ret < 0) --j;
                    else   break;
                } else --j;
            }
            if (i >= j) break;
            if (m == i) m = j; else if (m == j) m = i;  // 参照ポインタ変更
            for (int column = 0; column < columns.size(); column++) {
                getColumn(column).exchange(i,j);
            }
            i++;  j--;
        }
        if (first < i - 1) qsort_b(queue, first, i-1, ignoreCase, iq);
        if (j + 1 < last)  qsort_b(queue, j + 1, last, ignoreCase, iq);
    }
    
    /* boolean の逆ソート */
    private void rqsort_b(BooleanQueue queue, int first, int last, boolean ignoreCase, IntQueue iq) {
        int m = (first+last)/2;
        int x = queue.getInt(m);
        int i = first;
        int j = last;
        while (true) {
            while (queue.getInt(i) >= x) {
                if (queue.getInt(i) == x) {
                    int ret = qsort_sub(iq, i, m, ignoreCase);
                    if (ret < 0) i++; else break;
                } else i++;
            }
            while (x >= queue.getInt(j)) {
                if (x == queue.getInt(j)) {
                    int ret = qsort_sub(iq, m, j, ignoreCase);
                    if (ret < 0) j--; else break;
                } else j--;
            }
            if (i >= j) break;
            //i と ｊを交換
            if (m == i) m = j; else if (m == j) m = i;  // 参照ポインタ変更
            for (int column = 0; column < columns.size(); column++) {
                getColumn(column).exchange(i,j);
            }
            i++;  j--;
        }
        if (first < i - 1) rqsort_b(queue, first, i-1, ignoreCase, iq);
        if (j + 1 < last)  rqsort_b(queue, j + 1, last, ignoreCase, iq);
    }
    
    /* CharArray のソート */
    private void qsort_s(CharArrayQueue queue, int first, int last, boolean ignoreCase, IntQueue iq) {
        int m = (first+last)/2; // x の参照ポインタ
        CharArray x = CharArray.pop(queue.peek(m));
        int i = first;
        int j = last;
        while (true) {
            int n;
            if (ignoreCase) {
                while ((n = queue.peek(i).compareToIgnoreCase(x)) <= 0) {
                    if (n == 0) {
                        int ret = qsort_sub(iq, i, m, ignoreCase);
                        if (ret < 0) ++i; else  break;
                    } else i++;
                }
                while ((n = x.compareToIgnoreCase(queue.peek(j))) <= 0) {
                    if (n == 0) {
                        int ret = qsort_sub(iq, m, j, ignoreCase);
                        if (ret < 0) --j;  else   break;
                    } else --j;
                }
            } else {
                while ((n = queue.peek(i).compareTo(x)) <= 0) {
                    if (n == 0) {
                        int ret = qsort_sub(iq, i, m, ignoreCase);
                        if (ret < 0) ++i; else  break;
                    } else i++;
                }
                while ((n = x.compareTo(queue.peek(j))) <= 0) {
                    if (n == 0) {
                        int ret = qsort_sub(iq, m, j, ignoreCase);
                        if (ret < 0) --j;  else   break;
                    } else --j;
                }
            }
            if (i >= j) break;
            //i と ｊを交換
            if (m == i) m = j; else if (m == j) m = i;  // 参照ポインタ変更
            for (int column = 0; column < columns.size(); column++) {
                getColumn(column).exchange(i,j);
            }
            i++;  j--;
        }
        if (first < i - 1) qsort_s(queue, first, i-1, ignoreCase, iq);
        if (j + 1 < last)  qsort_s(queue, j + 1, last, ignoreCase, iq);
        CharArray.push(x);
    }
    
    /* CharArray の逆ソート */
    private void rqsort_s(CharArrayQueue queue, int first, int last, boolean ignoreCase, IntQueue iq) {
        int m = (first+last)/2; // x の参照ポインタ
        CharArray x = CharArray.pop(queue.peek(m));
        int i = first;
        int j = last;
        while (true) {
            int n;
            if (ignoreCase) {
                while ((n = queue.peek(i).compareToIgnoreCase(x)) >= 0) {
                    if (n == 0) {
                        int ret = qsort_sub(iq, i, m, ignoreCase);
                        if (ret < 0) i++; else break;
                    } else i++;
                }
                while ((n = x.compareToIgnoreCase(queue.peek(j))) >= 0) {
                    if (n == 0) {
                        int ret = qsort_sub(iq, m, j, ignoreCase);
                        if (ret < 0) j--; else break;
                    } else j--;
                }
            } else {
                while ((n = queue.peek(i).compareTo(x)) >= 0) {
                    if (n == 0) {
                        int ret = qsort_sub(iq, i, m, ignoreCase);
                        if (ret < 0) i++; else break;
                    } else i++;
                }
                while ((n = x.compareTo(queue.peek(j))) >= 0) {
                    if (n == 0) {
                        int ret = qsort_sub(iq, m, j, ignoreCase);
                        if (ret < 0) j--; else break;
                    } else j--;
                }
            }
            if (i >= j) break;
            //i と ｊを交換
            if (m == i) m = j; else if (m == j) m = i;  // 参照ポインタ変更
            for (int column = 0; column < columns.size(); column++) {
                getColumn(column).exchange(i,j);
            }
            i++;  j--;
        }
        if (first < i - 1) rqsort_s(queue, first, i-1, ignoreCase, iq);
        if (j + 1 < last)  rqsort_s(queue, j + 1, last, ignoreCase, iq);
        CharArray.push(x);
    }

    //--------------------------------
    // インデックス関連
    //--------------------------------
    
    /* 行情報をリフレッシュする */
    private void refreshRowsQueue(int index) {
        for (int i = index; i < getRowCount(); i++) {
            IntObject obj = (IntObject)rowsQueue.peek(i);
            if (obj == null) {
                rowsQueue.enqueue(new IntObject(i));
            } else {
                obj.setValue(i);
            }
        }
        if (debugRowsQueue) {
            System.out.print("  ☆rowsQueue[");
            for (int i = 0; i < rowsQueue.size(); i++) {
                if (i != 0) System.out.print(", ");
                IntObject obj = (IntObject)rowsQueue.peek(i);
                System.out.print(obj.getValue());
            }
            System.out.println("]");
        }
    }
    /** 行情報をリフレッシュ後、
        index からcount 分のインデックスを追加する。
        @param count 追加個数  -1で全て
    */
    private void refreshRowsQueue(int index, int count) {
        refreshRowsQueue(index);
        if (count == -1) count = getRowCount();
        else count = Math.min(count+index,getRowCount());
        for (int j = index; j < count ; j++) {  // 指定行のインデックス追加
            for (int i = 0; i < getColumnCount(); i++) {
                reindex(j,i);
            }
        }
    }
    // indexQueue（カラムごと）の中身
    // Hashtable  key (CharArray/IntObject/LongObject/DoubleObject) 
    //            data ObjectQueue(複数行登録可能）→行オブジェクト 
    
    /* カラムにインデックスを指定する */
    public void setIndex(int column) {
        Hashtable hash = (Hashtable)indexQueue.peek(column);
        if (hash == null) indexQueue.poke(column,new Hashtable());
        reindex(column);
    }
    public void setIndex(CharArray column_key) {
        setIndex(getColumnIndex(column_key));
    }
    public void setIndex(String column_key) {
        setIndex(getColumnIndex(column_key));
    }
    
    /* インデックス指定を解除する */
    public void removeIndex(int column) {
        indexQueue.poke(column,(Object)null);
    }
    public void removeIndex(CharArray column_key) {
        removeIndex(getColumnIndex(column_key));
    }
    public void removeIndex(String column_key) {
        removeIndex(getColumnIndex(column_key));
    }
    
    /** インデックスをクリアする */
    public void clearIndex() {
        for (int i = 0; i < indexQueue.size(); i++) {
            Hashtable hash = (Hashtable)indexQueue.peek(i); 
            if (hash != null) hash.clear();
        }
    }
    
    // 行削除の時は必ず呼ぶ
    @SuppressWarnings("unchecked")
    public void reindex(int column) {
        Hashtable<Object,ObjectQueue> hash = (Hashtable)indexQueue.peek(column);
        if (hash != null) {
            hash.clear();
            if (getRowCount() > 0) {
                switch (getColumnType(column)) {
                case Queue.INT:
                    IntQueue intQueue = (IntQueue)columns.peek(column);
                    IntObject obj = IntObject.pop(); 
                    for (int j = 0; j < getRowCount(); j++) {
                        obj.setValue(intQueue.peek(j));
                        ObjectQueue listQueue = (ObjectQueue)hash.get(obj);
                        IntObject lineObj = (IntObject)rowsQueue.peek(j);
                        if (listQueue == null) {
                            listQueue = new ObjectQueue();
                            listQueue.enqueue(lineObj);
                            hash.put(new IntObject(obj),listQueue);
                        } else {
                            listQueue.enqueue(lineObj);
                        }
                    }
                    IntObject.push(obj);
                    break;
                case Queue.LONG:
                case Queue.DATE:
                    LongQueue longQueue = (LongQueue)columns.peek(column);
                    LongObject objL = LongObject.pop(); 
                    for (int j = 0; j < getRowCount(); j++) {
                        objL.setValue(longQueue.peek(j));
                        ObjectQueue listQueue = (ObjectQueue)hash.get(objL);
                        IntObject lineObj = (IntObject)rowsQueue.peek(j);
                        if (listQueue == null) {
                            listQueue = new ObjectQueue();
                            listQueue.enqueue(lineObj);
                            hash.put(new LongObject(objL),listQueue);
                        } else {
                            listQueue.enqueue(lineObj);
                        }
                    }
                    LongObject.push(objL);
                    break;
                case Queue.BOOL:
                    BooleanQueue boolQueue = (BooleanQueue)columns.peek(column);
                    BooleanObject objB = BooleanObject.pop(); 
                    for (int j = 0; j < getRowCount(); j++) {
                        objB.setValue(boolQueue.peek(j));
                        ObjectQueue listQueue = (ObjectQueue)hash.get(objB);
                        IntObject lineObj = (IntObject)rowsQueue.peek(j);
                        if (listQueue == null) {
                            listQueue = new ObjectQueue();
                            listQueue.enqueue(lineObj);
                            hash.put(new BooleanObject(objB),listQueue);
                        } else {
                            listQueue.enqueue(lineObj);
                        }
                    }
                    BooleanObject.push(objB);
                    break;
                case Queue.DOUBLE:
                    DoubleQueue doubleQueue = (DoubleQueue)columns.peek(column);
                    DoubleObject objD = DoubleObject.pop(); 
                    for (int j = 0; j < getRowCount(); j++) {
                        objD.setValue(doubleQueue.peek(j));
                        ObjectQueue listQueue = (ObjectQueue)hash.get(objD);
                        IntObject lineObj = (IntObject)rowsQueue.peek(j);
                        if (listQueue == null) {
                            listQueue = new ObjectQueue();
                            listQueue.enqueue(lineObj);
                            hash.put(new DoubleObject(objD),listQueue);
                        } else {
                            listQueue.enqueue(lineObj);
                        }
                    }
                    DoubleObject.push(objD);
                    break;
                case Queue.STRING:
                    CharArrayQueue charQueue = (CharArrayQueue)columns.peek(column);
                    CharArray objC = CharArray.pop(); 
                    for (int j = 0; j < getRowCount(); j++) {
                        objC.set(charQueue.peek(j));
                        ObjectQueue listQueue = (ObjectQueue)hash.get(objC);
                        IntObject lineObj = (IntObject)rowsQueue.peek(j);
                        if (listQueue == null) {
                            listQueue = new ObjectQueue();
                            listQueue.enqueue(lineObj);
if (debugIndex) System.out.println("["+objC+"]");
                            hash.put(new CharArray(objC),listQueue);
                        } else {
                            listQueue.enqueue(lineObj);
                        }
                    }
                    CharArray.push(objC);
                    break;
                case Queue.OBJECT:
                    // do nothing
                    break;
                case Queue.BYTES:
                    /* 未対応 */
                    break;
                //case Queue.DATE:          // LONG と同一処理
                //    LongQueue dateQueue = (LongQueue)columns.peek(column);
                //    break;
                default:
                    break;
                } // endcase
            }
        } // if (hash != null) 
    }
    
    @SuppressWarnings("unchecked")
    public void reindex(int row, int column) {
        Hashtable<Object,ObjectQueue> hash = (Hashtable)indexQueue.peek(column);
        if (hash != null) {
            //hash.clear();
            int j = column;
            ObjectQueue listQueue = null;
            IntObject lineObj = null;
            switch (getColumnType(column)) {
                case Queue.INT:
                    IntQueue intQueue = (IntQueue)columns.peek(column);
                    IntObject obj = IntObject.pop(); 
                    //for (int j = 0; j < getRowCount(); j++) {
                        obj.setValue(intQueue.peek(j));
                        listQueue = (ObjectQueue)hash.get(obj);
                        lineObj = (IntObject)rowsQueue.peek(j);
                        if (listQueue == null) {
                            listQueue = new ObjectQueue();
                            listQueue.enqueue(lineObj);
                            hash.put(new IntObject(obj),listQueue);
                        } else {
                            listQueue.enqueue(lineObj);
                        }
                    //}
                    IntObject.push(obj);
                    break;
                case Queue.LONG:
                case Queue.DATE:
                    LongQueue longQueue = (LongQueue)columns.peek(column);
                    LongObject objL = LongObject.pop(); 
                    //for (int j = 0; j < getRowCount(); j++) {
                        objL.setValue(longQueue.peek(j));
                        listQueue = (ObjectQueue)hash.get(objL);
                        lineObj = (IntObject)rowsQueue.peek(j);
                        if (listQueue == null) {
                            listQueue = new ObjectQueue();
                            listQueue.enqueue(lineObj);
                            hash.put(new LongObject(objL),listQueue);
                        } else {
                            listQueue.enqueue(lineObj);
                        }
                    //}
                    LongObject.push(objL);
                    break;
                case Queue.BOOL:
                    BooleanQueue boolQueue = (BooleanQueue)columns.peek(column);
                    BooleanObject objB = BooleanObject.pop(); 
                    //for (int j = 0; j < getRowCount(); j++) {
                        objB.setValue(boolQueue.peek(j));
                        listQueue = (ObjectQueue)hash.get(objB);
                        lineObj = (IntObject)rowsQueue.peek(j);
                        if (listQueue == null) {
                            listQueue = new ObjectQueue();
                            listQueue.enqueue(lineObj);
                            hash.put(new BooleanObject(objB),listQueue);
                        } else {
                            listQueue.enqueue(lineObj);
                        }
                    //}
                    BooleanObject.push(objB);
                    break;
                case Queue.DOUBLE:
                    DoubleQueue doubleQueue = (DoubleQueue)columns.peek(column);
                    DoubleObject objD = DoubleObject.pop(); 
                    //for (int j = 0; j < getRowCount(); j++) {
                        objD.setValue(doubleQueue.peek(j));
                        listQueue = (ObjectQueue)hash.get(objD);
                        lineObj = (IntObject)rowsQueue.peek(j);
                        if (listQueue == null) {
                            listQueue = new ObjectQueue();
                            listQueue.enqueue(lineObj);
                            hash.put(new DoubleObject(objD),listQueue);
                        } else {
                            listQueue.enqueue(lineObj);
                        }
                    //}
                    DoubleObject.push(objD);
                    break;
                case Queue.STRING:
//System.out.println("■reindex("+row+","+column+")");
                    //dumpHash(hash);
                    CharArrayQueue charQueue = (CharArrayQueue)columns.peek(column);
                    CharArray objC = CharArray.pop(); 
                    //for (int j = 0; j < getRowCount(); j++) {
                        objC.set(charQueue.peek(j));
                        listQueue = (ObjectQueue)hash.get(objC);
                        lineObj = (IntObject)rowsQueue.peek(j);
                        if (listQueue == null) {
                            listQueue = new ObjectQueue();
                            listQueue.enqueue(lineObj);
                            hash.put(new CharArray(objC),listQueue);
                        } else {
                            boolean found = false;
                            for (int i = 0; i < listQueue.size(); i++) {
                                obj = (IntObject)listQueue.peek(i);
                                if (obj.getValue() == lineObj.getValue()) found = true;
                            }
                            if (!found) listQueue.enqueue(lineObj);
                        }
                    //}
                    CharArray.push(objC);
                    break;
                case Queue.OBJECT: 
                    // do nothing
                    break;
                case Queue.BYTES:
                    /* 未対応 */
                    break;
                //case Queue.DATE:          // LONG と同一処理
                //    LongQueue dateQueue = (LongQueue)columns.peek(column);
                //    break;
                default:
                    break;
            } // endcase
        } // if (hash != null) 
    }
    //全て
    @SuppressWarnings("unchecked")
    public void reindex() {
        for (int i = 0; i < indexQueue.size(); i++) {
            Hashtable<Object,ObjectQueue> hash = (Hashtable)indexQueue.peek(i);
            if (hash != null) {
                hash.clear();
                for (int j = 0; j < getRowCount(); i++) {
                
                
                
                }
            }
        }
    }
    
    // setInt から呼ばれる
    @SuppressWarnings("unchecked")
    public void reindex(int row, int column, int org, int value) {
        Hashtable<IntObject,ObjectQueue> hash = (Hashtable)indexQueue.peek(column);
        if (hash != null && hash.size() > 0 && (org != value)) {
            IntObject intOrg    = IntObject.pop(org);
            IntObject intValue  = IntObject.pop(value);

            // 元データを削除
            ObjectQueue queue = (ObjectQueue)hash.get(intOrg);
/**
System.out.println("reindex:"+column+":"+org+"->"+value+"  queue:"+(queue!=null)+" value="+intOrg.getValue());
System.out.println("hash.size="+hash.size()+" columnType:"+getColumnType(column));
            for (Enumeration e = hash.keys(); e.hasMoreElements(); ) {
                IntObject obj = (IntObject)e.nextElement();
                System.out.println("  intObj:"+obj.getValue());
            }
**/
            if (queue != null) {
                for (int i = 0; i < queue.size(); i++) {
                    IntObject obj = (IntObject)queue.peek(i);
                    //System.out.println("  "+i+":"+obj.getValue());
                    if (obj.getValue() == row) {
                        //System.out.println("  "+i+":found");
                        queue.remove(i);
                        if (queue.size() == 0) {
                            hash.remove(intOrg);
                        }
                        break;
                    }
                }
            } 
            // 新データを追加
            queue = (ObjectQueue)hash.get(intValue);
            if (queue == null) {
                queue = new ObjectQueue();
                hash.put(new IntObject(intValue),queue);
            }
            queue.enqueue(rowsQueue.peek(row));
            
            IntObject.push(intValue);
            IntObject.push(intOrg);
        }
    }
    // setLong から呼ばれる
    @SuppressWarnings("unchecked")
    public void reindex(int row, int column, long org, long value) {
        Hashtable<LongObject,ObjectQueue> hash = (Hashtable)indexQueue.peek(column);
        if (hash != null && hash.size() > 0 && (org != value)) {
            LongObject intOrg    = LongObject.pop(org);
            LongObject intValue  = LongObject.pop(value);

            // 元データを削除
            ObjectQueue queue = (ObjectQueue)hash.get(intOrg);
            if (queue != null) {
                for (int i = 0; i < queue.size(); i++) {
                    IntObject obj = (IntObject)queue.peek(i);
                    if (obj.getValue() == row) {
                        queue.remove(i);
                        if (queue.size() == 0) {
                            hash.remove(intOrg);
                        }
                        break;
                    }
                }
            } 
            // 新データを追加
            queue = (ObjectQueue)hash.get(intValue);
            if (queue == null) {
                queue = new ObjectQueue();
                hash.put(new LongObject(intValue),queue);
            }
            queue.enqueue(rowsQueue.peek(row));
            
            LongObject.push(intValue);
            LongObject.push(intOrg);
        }
    }
    // setCharArray から呼ばれる
    @SuppressWarnings("unchecked")
    public void reindex(int row, int column, CharArray org, CharArray value) {
        Hashtable<CharArray,ObjectQueue> hash = (Hashtable)indexQueue.peek(column);
        if (hash != null && hash.size() > 0 && (org != value)) {
            //System.out.println("  ▼=====");
            //dumpHash(hash);  /* debug */
            // 元データを削除
            ObjectQueue queue = (ObjectQueue)hash.get(org);

            if (queue != null) {
                for (int i = 0; i < queue.size(); i++) {
                    IntObject obj = (IntObject)queue.peek(i);
                    if (obj.getValue() == row) {
                        queue.remove(i);
                        if (queue.size() == 0) {
                            hash.remove(org);
                        }
                        break;
                    }
                }
            } 
            // 新データを追加
            queue = (ObjectQueue)hash.get(value);
            if (queue == null) {
                queue = new ObjectQueue();
                hash.put(new CharArray(value), queue);
            }
            queue.enqueue(rowsQueue.peek(row));
            
            //dumpHash(hash);  /* debug */
            //System.out.println("  ▲====");
        }
    }
    public void dumpHash(Hashtable hash) {
        System.out.println("  --- hash size = "+hash.size());
        for (Enumeration e = hash.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            System.out.print("    key:"+key.toString()+"  [");
            ObjectQueue queue = (ObjectQueue)hash.get(key);
            for (int i = 0; i < queue.size(); i++) {
                if (i != 0) System.out.print(", ");
                IntObject io = (IntObject)queue.peek(i);
                System.out.print(io.getValue());
            }
            System.out.println("]");
        }
        System.out.println("  --- hash end ------");
        
    }
    
    /** 数値検索 インデックスがある時はインデックスを使う 
        @param search 検索数値
        @param column カラム
        @return 検索結果
    */
    public IntQueue find(int search, int column) {
        IntQueue queue = new IntQueue();

        if (getRowCount() > 0) {
            Hashtable hash = (Hashtable)indexQueue.peek(column);
            if (hash != null) {    // index 処理
//System.out.println("<indexed>");
                ObjectQueue listQueue = null;
                if (getColumnType(column) == Queue.LONG) {
                    LongObject key = LongObject.pop(search);
                    listQueue = (ObjectQueue)hash.get(key);
                    LongObject.push(key);
                } else {
                    IntObject key = IntObject.pop(search);
                    listQueue = (ObjectQueue)hash.get(key);
                    IntObject.push(key);
                }
                
                if (listQueue != null) {
                    for (int i = 0; i < listQueue.size(); i++) {
                        IntObject obj = (IntObject)listQueue.peek(i);
                        queue.enqueue(obj.getValue());
                    }
                }
            } else {                // 通常検索
//System.out.println("<not indexed>");
                for (int j = 0; j < getRowCount(); j++) {
                    int  find = getInt(j,column);
                    if (search == find) queue.enqueue(j);
                }
            }
        }
        return queue;
    }
    public IntQueue find(int search, CharArray column_key) {
        return find(search,getColumnIndex(column_key));
    }
    public IntQueue find(int search, String column_key) {
        return find(search,getColumnIndex(column_key));
    }
    /** 数値検索 インデックスがある時はインデックスを使う 
        @param search 検索数値
        @param column カラム
        @return 検索結果
    */
    public IntQueue find(long search, int column) {
        IntQueue queue = new IntQueue();
        
        if (getRowCount() > 0) {
            Hashtable hash = (Hashtable)indexQueue.peek(column);
            if (hash != null) {    // index 処理
//System.out.println("<L:indexed>");
                LongObject key = LongObject.pop(search);
                ObjectQueue listQueue = (ObjectQueue)hash.get(key);
                LongObject.push(key);
                if (listQueue != null) {
                    for (int i = 0; i < listQueue.size(); i++) {
                        IntObject obj = (IntObject)listQueue.peek(i);
                        queue.enqueue(obj.getValue());
                    }
                }
            } else {                // 通常検索
//System.out.println("<L:not indexed>");
                for (int j = 0; j < getRowCount(); j++) {
                    long  find = getLong(j,column);
                    if (search == find) queue.enqueue(j);
                }
            }
        }
        return queue;
    }
    public IntQueue find(long search, CharArray column_key) {
        return find(search,getColumnIndex(column_key));
    }
    public IntQueue find(long search, String column_key) {
        return find(search,getColumnIndex(column_key));
    }
    
    /** 数値検索 インデックスがある時はインデックスを使う 
        @param search 検索数値
        @param column カラム
        @return 検索結果
    */
    public IntQueue find(double search, int column) {
        IntQueue queue = new IntQueue();
        
        if (getRowCount() > 0) {
            Hashtable hash = (Hashtable)indexQueue.peek(column);
            if (hash != null) {    // index 処理
                DoubleObject key = DoubleObject.pop(search);
                ObjectQueue listQueue = (ObjectQueue)hash.get(key);
                DoubleObject.push(key);
                if (listQueue != null) {
                    for (int i = 0; i < listQueue.size(); i++) {
                        IntObject obj = (IntObject)listQueue.peek(i);
                        queue.enqueue(obj.getValue());
                    }
                }
            } else {                // 通常検索
                for (int j = 0; j < getRowCount(); j++) {
                    double  find = getDouble(j,column);
                    if (search == find) queue.enqueue(j);
                }
            }
        }
        return queue;
    }
    public IntQueue find(double search, CharArray column_key) {
        return find(search,getColumnIndex(column_key));
    }
    public IntQueue find(double search, String column_key) {
        return find(search,getColumnIndex(column_key));
    }
    
    /** 文字列検索 インデックスがある時はインデックスを使う 
        @param search 検索文字列
        @param column カラム
        @return 検索結果
    */
    public IntQueue find(CharArray search, int column) {
        IntQueue queue = new IntQueue();
        if (getRowCount() > 0) {
            Hashtable hash = (Hashtable)indexQueue.peek(column);
            if (hash != null) {    // index 処理
//System.out.println("find:"+search+"<S:indexed> row:"+getRowCount()+" hash:"+hash.size());
                //dumpHash(hash);     /** debug **/
                ObjectQueue listQueue = (ObjectQueue)hash.get(search);
                if (listQueue != null) {
//System.out.println("listQueue size:"+listQueue.size());
                    for (int i = 0; i < listQueue.size(); i++) {
                        IntObject obj = (IntObject)listQueue.peek(i);
                        queue.enqueue(obj.getValue());
                    }
                } else {
//System.out.println("listQueue が null");
                }
            } else {                // 通常検索
//System.out.println("find:"+search+"<S:not indexed> row:"+getRowCount());
                for (int j = 0; j < getRowCount(); j++) {
                    CharArray find = getCharArray(j,column);
                    if (find != null && search.equals(find)) queue.enqueue(j);
                }
            }
        }
        return queue;
    }
    public IntQueue find(String search, int column) {
        CharArray ch = CharArray.pop(search);
        IntQueue queue = find(ch, column);
        CharArray.push(ch);
        return queue;
    }
    
    public IntQueue find(CharArray search, CharArray column_key) {
        return find(search,getColumnIndex(column_key));
    }
    public IntQueue find(CharArray search, String column_key) {
        return find(search,getColumnIndex(column_key));
    }
    
    public IntQueue find(String search, String column_key) {
        return find(search,getColumnIndex(column_key));
    }
    
    //-------------------------
    // 先頭検索（先頭のみ検索）
    //-------------------------
    /** 数値検索 インデックスがある時はインデックスを使う 
        @param search 検索数値
        @param column カラム
        @return 先頭インデックス:  -1で存在しない
    */
    public int indexOf(int search, int column) {
        int rsts = -1;
        if (getRowCount() > 0) {
            Hashtable hash = (Hashtable)indexQueue.peek(column);
            if (hash != null) {    // index 処理
//System.out.println("<indexed>");
                ObjectQueue listQueue = null;
                if (getColumnType(column) == Queue.LONG) {
                    LongObject key = LongObject.pop(search);
                    listQueue = (ObjectQueue)hash.get(key);
                    LongObject.push(key);
                } else {
                    IntObject key = IntObject.pop(search);
                    listQueue = (ObjectQueue)hash.get(key);
                    IntObject.push(key);
                }
                
                if (listQueue != null && listQueue.size()>0) {
                    IntObject obj = (IntObject)listQueue.peek();
                    rsts = obj.getValue();
                }
            } else {                // 通常検索
//System.out.println("<not indexed>");
                for (int j = 0; j < getRowCount(); j++) {
                    int  find = getInt(j,column);
                    if (search == find) {
                        rsts = j;
                        break;
                    }
                }
            }
        }
        return rsts;
    }
    public int indexOf(int search, CharArray column_key) {
        return indexOf(search,getColumnIndex(column_key));
    }
    public int indexOf(int search, String column_key) {
        return indexOf(search,getColumnIndex(column_key));
    }
    
    /** 数値検索 インデックスがある時はインデックスを使う 
        @param search 検索数値
        @param column カラム
        @return 先頭インデックス:  -1で存在しない
    */
    public int indexOf(long search, int column) {
        int rsts = -1;
        if (getRowCount() > 0) {
            Hashtable hash = (Hashtable)indexQueue.peek(column);
            if (hash != null) {    // index 処理
                LongObject key = LongObject.pop(search);
                ObjectQueue listQueue = (ObjectQueue)hash.get(key);
                LongObject.push(key);
                if (listQueue != null && listQueue.size() > 0) {
                    IntObject obj = (IntObject)listQueue.peek();
                    rsts = obj.getValue();
                }
            } else {                // 通常検索
                for (int j = 0; j < getRowCount(); j++) {
                    long  find = getLong(j,column);
                    if (search == find) {
                        rsts = j;
                        break;
                    }
                }
            }
        }
        return rsts;
    }
    public int indexOf(long search, CharArray column_key) {
        return indexOf(search,getColumnIndex(column_key));
    }
    public int indexOf(long search, String column_key) {
        return indexOf(search,getColumnIndex(column_key));
    }
    
    /** 数値検索 インデックスがある時はインデックスを使う 
        @param search 検索数値
        @param column カラム
        @return 先頭インデックス:  -1で存在しない
    */
    public int indexOf(double search, int column) {
        int rsts = -1;
        if (getRowCount() > 0) {
            Hashtable hash = (Hashtable)indexQueue.peek(column);
            if (hash != null) {    // index 処理
                DoubleObject key = DoubleObject.pop(search);
                ObjectQueue listQueue = (ObjectQueue)hash.get(key);
                DoubleObject.push(key);
                if (listQueue != null && listQueue.size() > 0) {
                    IntObject obj = (IntObject)listQueue.peek();
                    rsts = obj.getValue();
                }
            } else {                // 通常検索
                for (int j = 0; j < getRowCount(); j++) {
                    double  find = getDouble(j,column);
                    if (search == find) {
                        rsts = j;
                        break;
                    }
                }
            }
        }
        return rsts;
    }
    public int indexOf(double search, CharArray column_key) {
        return indexOf(search,getColumnIndex(column_key));
    }
    public int indexOf(double search, String column_key) {
        return indexOf(search,getColumnIndex(column_key));
    }
    
    /** 文字列検索 インデックスがある時はインデックスを使う 
        @param search 検索文字列
        @param column カラム
        @return 先頭インデックス:  -1で存在しない
    */
    public int indexOf(CharArray search, int column) {
        int rsts = -1;
        if (getRowCount() > 0) {
            Hashtable hash = (Hashtable)indexQueue.peek(column);
            if (hash != null) {    // index 処理
                ObjectQueue listQueue = (ObjectQueue)hash.get(search);
                if (listQueue != null && listQueue.size() > 0) {
                    IntObject obj = (IntObject)listQueue.peek();
                    rsts = obj.getValue();
                }
            } else {                // 通常検索
                for (int j = 0; j < getRowCount(); j++) {
                    CharArray find = getCharArray(j,column);
                    if (find != null && search.equals(find)) {
                        rsts = j;
                        break;
                    }
                }
            }
        }
        return rsts;
    }
    public int indexOf(String search, int column) {
        CharArray ch = CharArray.pop(search);
        int rsts = indexOf(ch, column);
        CharArray.push(ch);
        return rsts;
    }
    
    public int indexOf(CharArray search, CharArray column_key) {
        return indexOf(search,getColumnIndex(column_key));
    }
    public int indexOf(CharArray search, String column_key) {
        return indexOf(search,getColumnIndex(column_key));
    }
    
    public int indexOf(String search, String column_key) {
        return indexOf(search,getColumnIndex(column_key));
    }
    
    //--------------------------------
    // 演算
    //--------------------------------
    /*
        指定カラムの合計値を求める
        INT/LONG/STRING カラムのみ
    */
    public long total(int column) {
        long total = 0;
        if (getRowCount() > 0) {
            int type = getColumnType(column);
            Queue queue = (Queue)columns.peek(column);
            
            switch(type) {
                case Queue.INT:
                    total = ((IntQueue)queue).total();
                    break;
                case Queue.LONG:
                    total = ((LongQueue)queue).total();
                    break;
                //case Queue.DOUBLE:
                //    total = ((DoubleQueue)queue).total();
                //    break;
                case Queue.STRING:
                    total = ((CharArrayQueue)queue).total();
                    break;
                default:
                    break;
            }
            
        }
        return total;
    }
    public long total(String column) {
        return total(getColumnIndex(column));
    }
    public long total(CharArray column) {
        return total(getColumnIndex(column));
    }
    
    //--------------------------------
    // シリアライズ
    //--------------------------------
    public void writeObject(DataOutput out) throws IOException {
        title.writeObject(out);
        comment.writeObject(out);
        names.writeObject(out);
        types.writeObject(out);
        out.writeInt(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            Queue queue = (Queue)columns.peek(i);
            queue.writeObject(out);
        }
        // カラム名
        out.writeInt(hashColumn.size());
        for (int i = 0; i < hashColumn.size(); i++) {
            CharArray ch =  keys.peek(i);
            ch.writeObject(out);
            out.writeInt(params.peek(i));
        }
        //if (Version.save >= 700) {
        commonData.writeObject(out);
        //}
        
    }
    public void readObject(DataInput in)  throws IOException {
        title.readObject(in);
        comment.readObject(in);
        names.readObject(in);
        types.readObject(in);
        columns.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            Queue queue = Queue.create(types.peek(i));  // 
            queue.readObject(in);
            columns.enqueue(queue);
        }
        // カラム名
        hashColumn.clear(); keys.clear(); params.clear();
        size = in.readInt();
        for (int i = 0; i < size; i++) {
            CharArray ch = new CharArray();
            ch.readObject(in);
            int value = in.readInt();
            IntObject obj = new IntObject(value);
            hashColumn.put(ch,obj);
            keys.enqueue(ch);
            params.enqueue(value);
        }
        //if (session.version >= 700) {
        commonData.readObject(in);
        //}
        
        
    }

    //----------------------------------------------------------------
    // テキストファイルに出力
    //-----------------------------------------------------------------
    /* テキストに出力する(QueueTable形式)<br>
        タブ区切り、コメント文字列(#)
    */
    public void writeText(CharArrayQueue queue)  /*throws IOException*/ {
        writeText(queue,"\t","#",":","#",":","#");
    }
    
    /** テキストに入出力する(QueueTable形式) 
        @param queue  テキスト
        @param delim データ区切り文字
        @param startKey キーワード開始文字列
        @param endKey キーワード終了文字列
        @param comm コメント文字列
        @return 出力サイズ
    */
     
    public int writeText(CharArrayQueue queue, String delim, 
                         String startKey, String endKey, String comm) {
        return writeText(queue, delim, startKey, endKey, comm, ":", endKey);
    }
    /** テキストに入出力する(QueueTable形式) <br>
        なるべく↑のメソッドを使用する
        @param queue  テキスト
        @param delim データ区切り文字
        @param startKey キーワード開始文字列
        @param endKey キーワード終了文字列
        @param comm コメント文字列
        @param delimCol カラムタイプデリミタ
        @param endColKey カラムキーワード終了文字列（以前との互換性のためだけに存在する);
        @return 出力サイズ
    */
    public int writeText(CharArrayQueue queue, String delim, 
                         String startKey, String endKey, String comm, 
                         String delimCol, String endColKey) {
        int org_size = queue.size();
        if (title.length() > 0) {   // タイトル出力
            CharArray ch = new CharArray(startKey+"TABLE"+endKey);
            ch.add(title);
            queue.enqueue(ch);
        }
        if (comment.length() > 0) {   // メント出力
            CharArray ch = new CharArray(startKey+"COMMENT"+endKey);
            ch.add(comment); 
            queue.enqueue(ch);
        }
        // カラム情報
        CharArray chCol = new CharArray();
        for (int i = 0; i < types.size(); i++) {
            if (i == 0) {
                chCol.add(startKey+"COL"+endColKey);
            } else {
                chCol.add(delim);
            }
            int type = types.peek(i);
            chCol.add(getColumnKey(i));
            chCol.add(delimCol);
            chCol.add(Queue.szTypes[type].charAt(0));
        }
        if (chCol.length() > 0) queue.enqueue(chCol);
        CharArray tmp = CharArray.pop();
        for (int j = 0; j < getRowCount(); j++) {
            CharArray ch = new CharArray();
            for (int i = 0; i < types.size(); i++) {
                if (i != 0) ch.add(delim);
                
                tmp.set(getCharArray(j,i));
                tmp.replace("\n","\\n");         // 改行に変換
                tmp.replace("\t","\\t");         // 
                tmp.replace("\r","\\r");         // 
                ch.add(tmp);
            }
            queue.enqueue(ch);
        }
        CharArray.push(tmp);
        queue.enqueue(startKey+"END"+endKey);
        return queue.size()-org_size;
    }

    /* テキストから入力する(QueueTable形式) <br>
        タブ区切り、コメント文字列(#)
    */
    public void readText(CharArrayQueue queue)  /*throws IOException*/ {
        readText(queue, "\t","#",":","#", 0, true, ":","#");
    }
    /** テキストから入力する(QueueTable形式) 
        @param queue  テキスト
        @param delim データ区切り文字
        @param startKey キーワード開始文字列
        @param endKey キーワード終了文字列
        @param comm コメント文字列
        @param start_pos  読み込み開始行
        @return 読み込み終了行
    */
    public int readText(CharArrayQueue queue, String delim, 
                        String startKey, String endKey,
                        String comm, int start_pos) {
        return readText(queue, delim, startKey, endKey, comm, start_pos, true, ":",endKey);
    }
    /** テキストから入力する(QueueTable形式) <br>
        なるべく↑のメソッドを使用する
        @param queue  テキスト
        @param delim データ区切り文字
        @param startKey キーワード開始文字列
        @param endKey キーワード終了文字列
        @param comm コメント文字列
        @param start_pos  読み込み開始行
        @param trim_mode  文字列の場合に前後の空白をカットする
        @param delimCol カラムタイプデリミタ
        @param endColKey カラムキーワード終了文字列（以前との互換性のためだけに存在する);
        @return 読み込み終了行
    */
    public int readText(CharArrayQueue queue, String delim, 
                        String startKey, String endKey, String comm, 
                        int start_pos, boolean trim_mode,
                        String delimCol, String endColKey) {
        CharArray chTmp = CharArray.pop();
        CharToken token = CharToken.pop();
        CharToken token2 = CharToken.pop();
        token.setDelimiter(delim);
        token2.setDelimiter(delimCol);
        clearAll();
        int line = start_pos;
        CharArray ch;
        title.clear();
        comment.clear();
        boolean make_column = false;
        boolean end_flg = false;
        for (boolean HEADER = true; HEADER ; line++) {
            ch = queue.peek(line);
            if (ch == null) {
//System.out.println("break1["+ch+"]");
                break;
            } else if (ch != null) {   // カラム情報を取得
                if (!ch.startsWith(startKey) && make_column) {
//System.out.println("break2["+ch+"]");
                    break;      // ヘッダー情報終了
                }
                chTmp.set(ch).toUpperCase();
                if (chTmp.startsWith(startKey+"END")) { // 終了
                    if (debug) System.out.print(startKey+"END");
                    end_flg= true;
                    break;
                } else if (chTmp.startsWith(startKey+"TABLE"+endKey)) {   //タイトル設定
                    if (debug) System.out.println("TITLE:"+ch);
                    int start = startKey.length()+5+endKey.length();
                    int size = ch.indexOf(comm,start);
                    if (size >= 0) size -= start;
                    title.set(ch,start,size);
                    if (trim_mode || size >= 0) title.trim();
                    if (debug) System.out.println("  ->["+title+"]");
                } else if (chTmp.startsWith(startKey+"COMMENT"+endKey)) {  //コメント設定
                    if (debug) System.out.print("COMMENT:"+ch);
                    if (comment.length()>0) comment.add("\n");
                    int start = startKey.length()+7+endKey.length();
                    int size = ch.indexOf(comm,start);
                    if (size >= 0) size -= start;
                    comment.add(ch,start, size);
                    if (debug) System.out.println("->"+comment);
                //} else if (chTmp.startsWith(startKey+"NAME"+endColKey)) { //カラム名称設定
                //    if (debug) System.out.println("COL:"+ch);
                //    ch.remove(0,startKey.length()+4+endKey.length());
                //    if (debug) System.out.println("  ->["+ch+"]");
                //    token.set(ch);
                //    for (int i = 0; i < token.size(); i++) {
                //      
                //    } // next
                } else if (chTmp.startsWith(startKey+"COL"+endColKey)) {    //カラム設定
                    if (debug) System.out.println("COL:"+ch);
                    CharArray ch2 = CharArray.pop(ch);
                    ch2.remove(0,startKey.length()+3+endKey.length());
                    int index = ch2.indexOf(comm);
                    if (index >= 0) ch2.remove(index);
                    ch2.trim();
                    
                    if (debug) System.out.println("  ->["+ch+"]");
                    token.set(ch2);
                    for (int i = 0; i < token.size(); i++) {
                        CharArray col = token.get(i);
                        if (debug) System.out.println("token2.set:"+col);
                        token2.set(col);
                        CharArray chName = token2.get(0).trim();
                        CharArray chType = token2.get(1);
                        if (debug) System.out.println("  chName="+chName+ "chType="+chType);
                        int type = Queue.STRING;
                        if (chType != null) {
                            char c = chType.chars[0];
                            switch (c) {
                                case 'i': type = Queue.INT; break;
                                case 'I': type = Queue.INT; break;
                                case 'l': type = Queue.LONG; break;
                                case 'L': type = Queue.LONG; break;
                                case 'b': type = Queue.BOOL; break;
                                case 'O': type = Queue.OBJECT; break;
                                case 'B': type = Queue.BYTES; break;
                                case 'd': type = Queue.DOUBLE; break;
                                case 'D': type = Queue.DATE; break;
                            }
                        }
                        //System.out.println("column Name:"+chName);
                        if (col.length <= 0 || chName == null || chName.length() <= 0) {
                            chName = new CharArray();
                            chName.add("COL_");     // ダミーのカラム名を付加する
                            chName.format(i);
                        }
                        if (trim_mode) chName.trim();
                        if (debug) System.out.println("  chName="+chName);
                        addColumn(type, new CharArray(chName));
                    } // next
                    make_column = true;
                    CharArray.push(ch2);
                }
            } // endif
        } // next
        if (!end_flg) {
            // データの読み込み
            for ( ; line < queue.size(); line++) {
                ch = queue.peek(line);
                
                if (ch.startsWith(startKey)) {
                    chTmp.set(ch).toUpperCase();
                    if (chTmp.startsWith(startKey+"END")) break;  // 終了
                } else if (ch.startsWith(comm)) {
                    // do nothing
//if (debugIndex) System.out.println(line+"コメント["+ch+"]");
                } else if (ch.length()>0) {
//if (debugIndex) System.out.println(line+"１行追加["+ch+"]");

                    int index = ch.indexOf(comm);
                    if (index >= 0) {
                        ch.remove(index);
                        ch.trim();
                    }

                    addRow();
                    int row = getRowCount();
                    token.set(ch);
                    while (columns.size() < token.size()) {
                        addColumn(Queue.STRING);
                    }
                    if (row == 0) {
                        addRow();
                        row = getRowCount();
                    }
                    for (int i = 0; i < token.size(); i++) {
                        CharArray c1 = token.get(i);
                        if (trim_mode) c1.trim();
                        c1.replace("\\n","\n");         // 改行に変換
                        c1.replace("\\t","\t");         // 
                        c1.replace("\\r","\r");         // 
                        setCharArray(c1, row-1, i);
                         //System.out.println("      ["+i+"]set="+token.get(i)+" row="+(row-1));
                    }
                }
            }
        }
        CharToken.push(token2);
        CharToken.push(token);
        CharArray.push(chTmp);
        if (debug) dumpTable(10);
        return line;
    }

    /** CSVテキストファイル(CharArrayQueue)に出力する 
        カンマ区切り, ダブルコーテーション
        @param chqueue        出力CharArrayQueue
    */
    public void writeCSV(CharArrayQueue chqueue)  /*throws IOException*/ {
        writeCSV(chqueue,",","\"","\"",0,null);
    }
    
    /** CSVテキストファイル(CharArrayQueue)に出力する 
        カンマ区切り, ダブルコーテーション
        @param mode   0:データのみ 1:カラムキー出力 2:カラム名出力 3:両方
        @param chqueue        出力CharArrayQueue
    */
    public void writeCSV(CharArrayQueue chqueue, int mode)  /*throws IOException*/ {
        writeCSV(chqueue,",","\"","\"",mode,null);
    }
    
    /** CSVテキストファイル(CharArrayQueue)に出力する 
        カンマ区切り, ダブルコーテーション
        @param mode   0:データのみ 1:カラムキー出力 2:カラム名出力 3:両方
        @param queue        出力順をカラムキーで指定する
        @param chqueue        出力CharArrayQueue
    */
    public void writeCSV(CharArrayQueue chqueue, int mode, CharArrayQueue queue)  /*throws IOException*/ {
        writeCSV(chqueue,",","\"","\"",mode, queue);
    }
    
    /** CSVテキストファイルに出力する 
        カンマ区切り, ダブルコーテーション
        @return  出力データ
    */
    public CharArray writeCSV()  /*throws IOException*/ {
        return writeCSV(",","\"","\"",0,null);
    }
    /** CSVテキストファイルに出力する 
        カンマ区切り, ダブルコーテーション
        @param mode   0:データのみ 1:カラムキー出力 2:カラム名出力 3:両方
        @return  出力データ
    */
    public CharArray writeCSV(int mode)  /*throws IOException*/ {
        return writeCSV(",","\"","\"",mode,null);
    }
    /** CSVテキストファイルに出力する 
        カンマ区切り, ダブルコーテーション
        @param mode   0:データのみ 1:カラムキー出力 2:カラム名出力 3:両方
        @param queue        出力順をカラムキーで指定する
        @return  出力データ
    */
    public CharArray writeCSV(int mode, CharArrayQueue queue)  /*throws IOException*/ {
        return writeCSV(",","\"","\"",mode,queue);
    }
    /** 指定形式で CSVテキストファイル(CharArrayQueue))に出力する 
        @param chqueue      出力CharArrayQueue
        @param delimiter    区切り文字
        @param quotStart    開始コーテーション
        @param quotEnd      終了コーテーション
    */
    public void writeCSV(CharArrayQueue chqueue,
                         String delimiter,
                         String quotStart,
                         String quotEnd) {
        writeCSV(chqueue,delimiter, quotStart,quotEnd,0,null);
    }
    /** 指定形式で CSVテキストファイル(CharArrayQueue)に出力する 
        @param chqueue        出力先CharArrayQueue
        @param delimiter    区切り文字
        @param quotStart    開始コーテーション
        @param quotEnd      終了コーテーション
        @param mode         0:データのみ 1:カラムキー出力 2:カラム名出力 3:両方
        @param queue        出力順をカラムキーで指定する
      
    */
    public void writeCSV(CharArrayQueue chqueue,
                         String delimiter,
                         String quotStart,
                         String quotEnd,
                         int mode,
                         CharArrayQueue queue) {
        if ((mode & 2) > 0) {   // カラム名出力
            CharArray ch = new CharArray();
            if (queue == null) {
                for (int i = 0; i < types.size(); i++) {
                    if (i != 0) ch.add(delimiter);
                    ch.add(quotStart);
                    ch.add(getColumnCharName(i));
                    ch.add(quotEnd);
                }
            } else {
                for (int i = 0; i < queue.size(); i++) {
                    CharArray key = queue.peek(i);
                    int index = getColumnIndex(key);
                    if (index < 0) {
                        if (debugColumn) System.out.println("writeCSV column["+key+"] not found!");
                        continue;
                    }
                    if (i != 0) ch.add(delimiter);
                    ch.add(quotStart);
                    ch.add(getColumnCharName(index));
                    ch.add(quotEnd);
                }
            }
            chqueue.enqueue(ch);
        }
        if ((mode & 1) > 0) {
            CharArray ch = new CharArray();
            if (queue == null) {
                for (int i = 0; i < types.size(); i++) {
                    if (i != 0) ch.add(delimiter);
                    ch.add(quotStart);
                    ch.add(getColumnKey(i));
                    ch.add(quotEnd);
                }
            } else {
                for (int i = 0; i < queue.size(); i++) {
                    CharArray key = queue.peek(i);
                    int index = getColumnIndex(key);
                    if (index < 0) {
                        if (debugColumn) System.out.println("writeCSV column["+key+"] not found!");
                        continue;
                    }
                    if (i != 0) ch.add(delimiter);
                    ch.add(quotStart);
                    ch.add(getColumnKey(index));
                    ch.add(quotEnd);
                }
            }
            chqueue.enqueue(ch);
        }
        for (int j = 0; j < getRowCount(); j++) {
            CharArray ch = new CharArray();
            if (queue == null) {
                for (int i = 0; i < types.size(); i++) {
                    if (i != 0) ch.add(delimiter);
                    ch.add(quotStart);
                    if (getColumnType(i) == Queue.DATE) {   // 日付型の場合
                        ch.add(getDateString(j,i));
                    } else {
                        ch.add(getCharArray(j,i));
                    }
                    ch.add(quotEnd);
                }
            } else {
                for (int i = 0; i < queue.size(); i++) {
                    CharArray key = queue.peek(i);
                    int index = getColumnIndex(key);
                    if (index < 0) continue;
                    if (i != 0) ch.add(delimiter);
                    ch.add(quotStart);
                    if (getColumnType(index) == Queue.DATE) {   // 日付型の場合
                        ch.add(getDateString(j,index));
                    } else {
                        ch.add(getCharArray(j,index));
                    }
                    ch.add(quotStart);
                }
            }
            chqueue.enqueue(ch);
        }
    }
    /** 指定形式で CSVテキストファイルに出力する 
        @param delimiter    区切り文字
        @param quotStart    開始コーテーション
        @param quotEnd      終了コーテーション
        @param mode         0:データのみ 1:カラムキー出力 2:カラム名出力 3:両方
        @param queue        出力順をカラムキーで指定する
        @return  出力データ
    */
    public CharArray writeCSV(String delimiter,
                              String quotStart,
                              String quotEnd,
                              int mode,
                              CharArrayQueue queue) {
        CharArray ch = new CharArray();
        
        if ((mode & 2) > 0) {   // カラム名出力
            boolean flg = true;
            if (queue == null) {
                for (int i = 0; i < types.size(); i++) {
                    CharArray name = getColumnCharName(i);
                    if (name.length() == 0) continue;     // 名前のないカラムは追加しない
                    if (flg) flg = false; else ch.add(delimiter);
                    ch.add(quotStart);
                    ch.add(getColumnCharName(i));
                    ch.add(quotEnd);
                }
            } else {
                for (int i = 0; i < queue.size(); i++) {
                    CharArray key = queue.peek(i);
                    int index = getColumnIndex(key);
                    if (index < 0) {
                        if (debugColumn) System.out.println("writeCSV column["+key+"] not found!");
                        continue;
                    }
                    if (flg) flg = false; else ch.add(delimiter);
                    ch.add(quotStart);
                    ch.add(getColumnCharName(index));
                    ch.add(quotEnd);
                }
            }
            ch.add("\n");
        }
        if ((mode & 1) > 0) {
            boolean flg = true;
            if (queue == null) {
                for (int i = 0; i < types.size(); i++) {
                    if ((mode & 2) > 0) {   // カラム名も出力する場合
                        CharArray name = getColumnCharName(i);
                        if (name.length() == 0) continue;     // 名前のないカラムは追加しない
                    }
                    if (flg) flg = false; else ch.add(delimiter);
                    ch.add(quotStart);
                    ch.add(getColumnKey(i));
                    ch.add(quotEnd);
                }
            } else {
                for (int i = 0; i < queue.size(); i++) {
                    CharArray key = queue.peek(i);
                    int index = getColumnIndex(key);
                    if (index < 0) {
                        if (debugColumn) System.out.println("writeCSV column["+key+"] not found!");
                        continue;
                    }
                    if (flg) flg = false; else ch.add(delimiter);
                    ch.add(quotStart);
                    ch.add(getColumnKey(index));
                    ch.add(quotEnd);
                }
            }
            ch.add("\n");
        }
        
        for (int j = 0; j < getRowCount(); j++) {
            boolean flg = true;
            if (queue == null) {
                for (int i = 0; i < types.size(); i++) {
                    if ((mode & 2) > 0) {   // カラム名も出力する場合
                        CharArray name = getColumnCharName(i);
                        if (name.length() == 0) continue;     // 名前のないカラムは追加しない
                    }
                    if (flg) flg = false; else ch.add(delimiter);
                    ch.add(quotStart);
                    if (getColumnType(i) == Queue.DATE) {   // 日付型の場合
                        ch.add(getDateString(j,i));
                    } else {
                        ch.add(getCharArray(j,i));
                    }
                    ch.add(quotEnd);
                }
            } else {
                for (int i = 0; i < queue.size(); i++) {
                    CharArray key = queue.peek(i);
                    int index = getColumnIndex(key);
                    if (index < 0) continue;
                    if (flg) flg = false; else ch.add(delimiter);
                    ch.add(quotStart);
                    if (getColumnType(index) == Queue.DATE) {   // 日付型の場合
                        ch.add(getDateString(j,index));
                    } else {
                        ch.add(getCharArray(j,index));
                    }
                    ch.add(quotStart);
                }
            }
            ch.add("\n");   // "\r\n"?
        }
        return ch;
    }

    //--------------------------------
    //
    //--------------------------------
     
    /** テーブル内容をダンプする（デバッグ用） **/
    public void dumpTable() {
        dumpTable(-1, (SessionObject)null);
    }
    public void dumpTable(int max) {
        dumpTable(max, (SessionObject)null);
    }
    public void dumpTable(int max, SessionObject session) {
        dumpTable(max, session, "");
    }
    public void dumpTable(int max, SessionObject session, String pre) {
        String sz = "";
        if (session != null) {
            sz += session.count+"|";
            if (session.userID != null && session.userID.length() > 0) {
                sz+= "("+session.userID+")";
            }
        }
        sz += pre;
        if (title.length() > 0)   System.out.println(sz+"#TABLE:"+title);
        if (comment.length() > 0) {
            //System.out.println("#COMMENT:"+comment);
            CharToken token = CharToken.pop();
            token.setDelimiter("\n");
            token.set(comment);
            for (int i = 0; i < token.size(); i++) {
                System.out.println(sz+"#COMMENT:"+token.get(i));
            }
            CharToken.push(token);
        }
        int cols = getColumnCount();
        int rows = getRowCount();
//System.out.println(sz+"col:"+cols+" row:"+rows);
        if (max > 0) rows = Math.min(rows,max);
        if (names.size() > 0) {
            int size = Math.min(names.size(),cols);
            System.out.print(sz+"#NAME:");
            for (int i = 0; i < size; i++) {
                System.out.print(names.peek(i)+"\t");
            }
            System.out.println("");
        }
        System.out.print(sz+"#COL:");
        for (int i = 0; i < cols; i++) {
            CharArray ch = getColumnKey(i);
            System.out.print(((ch != null)? ch.toString() : "")+":"+Queue.szTypes[getColumnType(i)].charAt(0)+"\t");
        }
        System.out.println("");
        for (int j = 0; j < rows; j++) {
            System.out.print(sz);
            for (int i = 0; i < cols; i++) {
                CharArray ca = getCharArray(j,i);
                if (ca != null && ca.isNull()) {
                    System.out.print("<null>\t");
                } else {
                    switch (getColumnType(i)) {
                    case Queue.INT:
                        System.out.print(getInt(j,i)+"\t");
                        break;
                    case Queue.LONG:
                        System.out.print(getLong(j,i)+"\t");
                        break;
                    case Queue.BOOL:
                        System.out.print(getBoolean(j,i)+"\t");
                        break;
                    case Queue.DOUBLE:
                        System.out.print(getDouble(j,i)+"\t");
                        break;
                    case Queue.STRING:
                        System.out.print(getString(j,i)+"\t");
                        break;
                    case Queue.OBJECT:  //@OBJECT
                        System.out.print("<obj>\t");
                        break;
                    case Queue.BYTES:
                        /* 未対応 */
                        System.out.print("<bytes>\t");
                        break;
                    case Queue.DATE:
                        System.out.print(getDateString(j,i)+"\t");
                        break;
                    default:
                        System.out.print("<unknown>\t");
                        break;
                    }
                }
            }
            System.out.println("");
        } // next 
        System.out.println(sz+"#END");
    }
    public void dumpTable(String... szColumns) {
        dumpTable(szColumns, 0);
    }
    public void dumpTable(String[] szColumns, int line) {
        dumpTable(szColumns, line, (IntQueue)null);
    }
    public void dumpTable(String[] szColumns, int line, int... check) {
        if (check == null) {
            dumpTable(szColumns, line, (IntQueue)null);
        } else {
            IntQueue iq = new IntQueue();
            for (int i = 0; i < check.length; i++) {
                iq.enqueue(check[i]);
            }
            dumpTable(szColumns, line, iq);
        }
    }
    public void dumpTable(String[] szColumns, int line, IntQueue check) {
        if (szColumns == null || szColumns.length == 0) dumpTable();
    
        for (int i = 0; i < szColumns.length; i++) {
            System.out.print("\t");
            System.out.print(szColumns[i]);
        }
        System.out.println();
        int rows = getRowCount();
        for (int j = 0; j < rows; j++) {
            if (line > 0) {
                if (j % line == 0) System.out.println("-----------------------------------");
            }
            System.out.print(Util.format0(j,3)+"|");
            if (check != null) {

                for (int i = 0; i < check.size(); i++) {
                    if (j == check.peek(i)) {
                        System.out.print("*");
                        break;
                    }
                }
            }
            System.out.print("\t");
            for (int i = 0; i < szColumns.length; i++) {
                int index = getColumnIndex(szColumns[i]);
                CharArray ca = getCharArray(j,index);
                if (ca != null && ca.isNull()) {
                    System.out.print("<null>\t");
                } else {
                    switch (getColumnType(index)) {
                    case Queue.INT:
                        System.out.print(getInt(j,index)+"\t");
                        break;
                    case Queue.LONG:
                        System.out.print(getLong(j,index)+"\t");
                        break;
                    case Queue.BOOL:
                        System.out.print(getBoolean(j,index)+"\t");
                        break;
                    case Queue.DOUBLE:
                        System.out.print(getDouble(j,index)+"\t");
                        break;
                    case Queue.STRING:
                        System.out.print(getString(j,index)+"\t");
                        break;
                    case Queue.OBJECT:  //@OBJECT
                        System.out.print("<obj>\t");
                        break;
                    case Queue.BYTES:
                        /* 未対応 */
                        System.out.print("<bytes>\t");
                        break;
                    case Queue.DATE:
                        System.out.print(getDateString(j,index)+"\t");
                        break;
                    default:
                        System.out.print("<unknown>\t");
                        break;
                    }
                }
            }
            System.out.println("");
        } // next 
    }
    
    // 
    public CharArray dumpTable(int max, CharArray ch) {
        return dumpTable(max, ch, "");
    }
    public CharArray dumpTable(int max, CharArray ch, String pre) {
        if (ch == null) ch = new CharArray();
        if (title.length() > 0)   ch.add(pre+"#TABLE:"+title+"\n");
        if (comment.length() > 0) {
            //System.out.println("#COMMENT:"+comment);
            CharToken token = CharToken.pop();
            token.setDelimiter("\n");
            token.set(comment);
            for (int i = 0; i < token.size(); i++) {
                ch.add(pre+"#COMMENT:"+token.get(i)+"\n");
            }
            CharToken.push(token);
        }
        int cols = getColumnCount();
        int rows = getRowCount();
//System.out.println(sz+"col:"+cols+" row:"+rows);
        if (max > 0) rows = Math.min(rows,max);
        if (names.size() > 0) {
            int size = Math.min(names.size(),cols);
            System.out.print(pre+"#NAME:");
            for (int i = 0; i < size; i++) {
                ch.add(names.peek(i)+"\t");
            }
            ch.add("\n");
        }
        ch.add(pre+"#COL:");
        for (int i = 0; i < cols; i++) {
            CharArray ck = getColumnKey(i);
            ch.add(((ck != null)? ck.toString() : "")+":"+Queue.szTypes[getColumnType(i)].charAt(0)+"\t");
        }
        ch.add("\n");
        for (int j = 0; j < rows; j++) {
            ch.add(pre);
            for (int i = 0; i < cols; i++) {
                CharArray ca = getCharArray(j,i);
                if (ca != null && ca.isNull()) {
                    ch.add("<null>\t");
                } else {
                    switch (getColumnType(i)) {
                    case Queue.INT:
                        ch.add(getInt(j,i)+"\t");
                        break;
                    case Queue.LONG:
                        ch.add(getLong(j,i)+"\t");
                        break;
                    case Queue.BOOL:
                        ch.add(getBoolean(j,i)+"\t");
                        break;
                    case Queue.DOUBLE:
                        ch.add(getDouble(j,i)+"\t");
                        break;
                    case Queue.STRING:
                        ch.add(getString(j,i)+"\t");
                        break;
                    case Queue.OBJECT:
                        /* 未対応 */
                        System.out.print("<obj>\t");
                        break;
                    case Queue.BYTES:
                        /* 未対応 */
                        System.out.print("<bytes>\t");
                        break;
                    case Queue.DATE:
                        ch.add(getDateString(j,i)+"\t");
                        break;
                    default:
                        System.out.print("<unknown>\t");
                        break;
                    }
                }
            }
            ch.add("\n");
        } // next 
        ch.add(pre+"#END\n");
        return ch;
    }

    /*
        指定行情報のデバッグ表示
    */
    public void debugTable(int row) {
        debugTable(row, null);
    }
    /*
        指定行情報のデバッグ表示
    */
    public void debugTable(int row, SessionObject session) {
        String sz = "";
        if (session != null) {
            sz += session.count+"|";
            if (session.userID != null && session.userID.length() > 0) {
                sz+= "("+session.userID+")";
            }
        }
        int cols = getColumnCount();
        int rows = getRowCount();
        if (row < 0 || row >= rows) return;
        
if (debug) System.out.println("---row:"+row+"/"+rows);
        int j = row;
        for (int i = 0; i < cols; i++) {
            System.out.print(sz);
            System.out.print("["+i+"]");
            CharArray ch = getColumnKey(i);
            System.out.print(((ch != null)? ch.toString() : "")+":"
                        +Queue.szTypes[getColumnType(i)]+"\t");
            CharArray ca = getCharArray(j,i);
            if (ca != null && ca.isNull()) {
                System.out.println("<null>");
            } else {
                switch (getColumnType(i)) {
                case Queue.INT:
                    System.out.println(getInt(j,i));
                    break;
                case Queue.LONG:
                    System.out.println(getLong(j,i));
                    break;
                case Queue.BOOL:
                    System.out.println(getBoolean(j,i));
                    break;
                case Queue.DOUBLE:
                    System.out.println(getDouble(j,i));
                    break;
                case Queue.STRING:
                    System.out.println(getString(j,i));
                    break;
                case Queue.OBJECT:  //@OBJECT
                    Object obj = getObject(j,i);
                    
                    if (obj == null) {
                        System.out.println("<null>");
                    } else {
                        System.out.println("<obj>");
                        if (obj instanceof HashParameter) {
                            ((HashParameter)obj).debugParameter("  --");
                        } else if (obj instanceof CharArrayQueue) {
                            ((CharArrayQueue)obj).dumpQueue("  --");
                        } else if (obj instanceof QueueTable) {
                            ((QueueTable)obj).dumpTable(-1,(SessionObject)null,"  --");
                        }
                    }
                    break;
                case Queue.BYTES:
                    /* 未対応 */
                    System.out.println("<bytes>\t");
                    break;
                case Queue.DATE:
                    System.out.println(getDateString(j,i));
                    break;
                default:
                    System.out.println("<unknown>");
                    break;
                }
            } //endif
        } // next
    }

    
    /*
        マージする関数
    */
    public boolean merge(QueueTable from) {
        return merge(from, null);
    }
    public boolean merge(QueueTable from, String checkColumn) {
        return merge(from, checkColumn, false);
    }
    public boolean merge(QueueTable from, String checkColumn, boolean searchFlg) {
        boolean sts = false;
        int step = 0; // デバッグ用情報
        EXIT: do {
            if (from == null || from.getRowCount() == 0) break;
            if (getRowCount() != from.getRowCount()) break;
            step++; //1
            CharArrayQueue copyColumn = new CharArrayQueue();
            boolean check = false;
            if (checkColumn != null && checkColumn.length() > 0) {
                if (getColumnIndex(checkColumn) < 0) break;
                if (from.getColumnIndex(checkColumn) < 0) break;
                check = true;
            }
            step++; //2
            for (int i = 0; i < from.keys.size(); i++) {
                CharArray key = from.keys.peek(i);
                int from_type = from.getColumnType(key);
                int type = getColumnType(key);
                if (type < 0) {  // 存在しない
                    copyColumn.enqueue(key);    // 追加カラム
                } else {
                    if (debugMerge) {
                        System.out.println("merge:same column["+key+"]");
                    }
                    if (type != from_type) break EXIT;
                }
            }
            step++; // 3
            if (copyColumn.size() == 0) break;
            step++; //4 
            for (int i = 0; i < copyColumn.size(); i++) {    // カラムを追加する
                CharArray key = copyColumn.peek(i);
                int type = from.getColumnType(key);
                addColumn(type, key);
            }
            step++; // 5
            if (check && searchFlg) {
                from.setIndex(checkColumn);
            }
            step++; // 6
            int size = getRowCount();
            for (int i = 0; i < size; i++) { // データを追加する
                int row = i;
                if (check) {
                    CharArray chk1 = get(i, checkColumn);
                    if (searchFlg) { // 検索
                        IntQueue queue = from.find(chk1, checkColumn);
                        if (queue == null || queue.size() != 1) break EXIT;
                        row = queue.peek();
                    } else { // 同一行
                        CharArray chk2 = from.get(i, checkColumn);
                        if (chk1 == null || chk2 == null || !chk1.equals(chk2)) {
                            System.out.println("merge:"+checkColumn+"["+chk1+"]!=["+chk2+"]");
                            break EXIT;
                        }
                    }
                }
                for (int j = 0; j < copyColumn.size(); j++) {
                    CharArray key = copyColumn.peek(j);
                    int type = from.getColumnType(key);
                    if (type == Queue.OBJECT) {
                        Object o = from.getObject(row, key);
                        setObject(o, i, key);
                    } else {
                        CharArray ch = from.getCharArray(row, key);
                        setCharArray(ch, i, key);
                    }
                }
            }
            sts = true;
        } while (false);
        if (!sts) System.out.println("marge failed !! "+step);
        else if (debugMerge)    System.out.println("marge success !!");
        return sts;
    }
    
    // タイムスタンプ関連
    private long timestamp = 0;
    
    /** タイムスタンプを現時刻に設定する */
    public void setTimeStamp() { timestamp = System.currentTimeMillis();}

    /* タイムスタンプを指定時刻に設定する */
    public void setTimeStamp(long t) { timestamp = t;}

    /* タイムスタンプを取得する*/
    public long getTimeStamp() { return timestamp;}
    
    /* 保存時刻からの経過時間(ミリ秒)を取得する*/
    public long getTimeLapse() { return System.currentTimeMillis() -timestamp;}
    
    /* 保存時刻からの経過時間(ミリ秒)を取得する*/
    public long getTimeLapse(long t) { return t -timestamp;}
    
    /* タイムスタンプの暗号化短縮文字列を取得する(比較のみに使用する) */
    public String getTimeStampKey() {
        return Crypt62.encode((int)(timestamp%13975));
    }
    
    // テーブルの利用メモリサイズを返す
    public synchronized int sizeOf() {
        int size = 0;
        int count = getRowCount();
        
        for (int i =0; i < columns.size(); i++) {
            int type = getColumnType(i);
            Queue queue = (Queue)columns.peek(i);
            switch (type) {
              case Queue.INT:
                size += ((IntQueue)queue).sizeOf();
                break;
              case Queue.LONG:
              case Queue.DATE:
                size += ((LongQueue)queue).sizeOf();
                break;
              case Queue.BOOL:
                size += ((BooleanQueue)queue).sizeOf();
                break;
              case Queue.DOUBLE:
                size += ((DoubleQueue)queue).sizeOf();
                break;
              case Queue.STRING:
                size += ((CharArrayQueue)queue).sizeOf();
                break;
              case Queue.BYTES:
                size += ((ByteArrayQueue)queue).sizeOf();
                break;
              case Queue.OBJECT:  //@@OBJE
                size += ((ObjectQueue)queue).sizeOf();
                break;
              default:
                break;
            }
        } // next
        return size;
    }
    
}


//
//
// [end of QueueTable.java]
//

