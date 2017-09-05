//------------------------------------------------------------------------
// @(#)QueueTableData.java
//              データの保管のみを行う（描画はしない）
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.Queue;
import com.miraidesign.util.QueueElement;
import com.miraidesign.util.QueueTable;

/** データの保管のみを行う（描画はしない）*/
public class QueueTableData extends ItemData {
    private QueueTable queue = new QueueTable();  // データ保存エリア
    {
        type = QUEUE_TABLE;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public QueueTableData() {
    }
    public QueueTableData(QueueTableData from, SessionObject session) {
        this.sessionObject = session;
        copy(from);
    }
    public QueueTableData(QueueTable queue) {
        setQueue(queue);
    }
    
    //---------------------------------------------------------------------
    // QueueTableインターフェース
    //---------------------------------------------------------------------
    /** 全てのデータとカラムをクリアする */
    public  void clearAll() { queue.clearAll();}
    /** 全データ（行）をクリアする (カラムはそのまま)*/
    public void clear() {queue.clear();}
    
    /** 指定行を削除する
        @param row 削除する行 */
    public boolean removeRow(int row) { return queue.removeRow(row); }
    
    /** カラム追加 
        @param type Queue.INT/LONG/BOOL/DOUBLE/STRING (OBJECT は指定禁止)
    */
    public boolean addColumn(int type) { return queue.addColumn(type); }
    /** カラム追加 
        @param type Queue.INT/LONG/BOOL/DOUBLE/STRING (OBJECT は指定禁止)
        @param name カラム名
    */
    public boolean addColumn(int type, CharArray name) { 
        return queue.addColumn(type,name); 
    }
    public boolean addColumn(int type, String name) { 
        return queue.addColumn(type,name); 
    }
    /** カラムの削除 
        @param index カラムインデックス
    */
    public void removeColumn(int index) { queue.removeColumn(index); }
    
    /** カラムデータの取得 
        @param index カラムindex(0- )
        @return IntQueue/LongQueue/DoubleQueue/Boolean/CharArrayQueue のどれか
    */
    public Queue getColumn(int index) { return queue.getColumn(index);}
 
    /** カラムデータの取得 
        @param name カラム名
        @return IntQueue/LongQueue/DoubleQueue/Boolean/CharArrayQueue のどれか
    */
    public Queue getColumn(CharArray name) { return queue.getColumn(name);}
    
    /** カラムインデックスを取得する
        @param name カラム名
        @return カラムインデックス (0-)  -1:存在しない
    */
    public int getColumnIndex(CharArray name) { 
        return queue.getColumnIndex(name);
    }
    
    /** カラムタイプを取得する
        @return Queue.INT/LONG/BOOL/DOUBLE/STRING
    */
    public int getColumnType(int index) { return queue.getColumnType(index);}
    
    /** カラム数を取得する */
    public int getColumnCount() { return queue.getColumnCount(); }
    
    /* 行数を取得する */
    public int getRowCount() { return queue.getRowCount(); }
    
    /** 値を取得する
        @param row      行      (0- )
        @param column   カラム  (0- )
    */
    public int  getInt(int row, int column) { return queue.getInt(row,column); }
    public long getLong(int row, int column) { return queue.getLong(row,column); }
    public double getDouble(int row, int column) { return queue.getDouble(row,column); }
    public boolean getBoolean(int row, int column) { return queue.getBoolean(row,column); }
    public CharArray getCharArray(int row, int column) { return queue.getCharArray(row,column); }
    
    /** 値を設定する（設定したカラムタイプに対応したメソッドを呼ぶ）
        @param value    設定する値
        @param row      行      (0- )
        @param column   カラム  (0- )
        @return true で成功
    */
    public boolean setInt(int value, int row, int column) {
        return queue.setInt(value,row,column);
    }
    /** 値を設定する */
    public boolean setLong(long value, int row, int column) {
        return queue.setLong(value,row,column);
    }
    /** 値を設定する */
    public boolean setDouble(double value, int row, int column) {
        return queue.setDouble(value,row,column);
    }
    /** 値を設定する */
    public boolean setBoolean(boolean value, int row, int column) {
        return queue.setBoolean(value,row,column);
    }
    /** 値を設定する */
    public boolean setCharArray(CharArray value, int row, int column) {
        return queue.setCharArray(value,row,column);
    }
    
    /* Element（1行のデータ)を取得する 
        @param row  取得する行
        @return QueueElement オブジェクト
    */
    public QueueElement getElement(int row) {
        return queue.getElement(row);
    }
    
    /** １行を設定する 
        @param row  設定する行
        @param element 設定するエレメント
    */
    public boolean setElement(int row, QueueElement element) { 
        return queue.setElement(row, element);
    }
    /** １行を追加する 
        @param element 追加するエレメント
    */
    public boolean addElement(QueueElement element) { 
        return queue.addElement(element);
    }
    /** 空行を追加する */
    public boolean addElement() { 
        return queue.addElement();
    }
 
    /** 指定カラムでソートする 
        @param column ソートするカラム
        @return true:成功
    */
    public boolean sort(int column) {
        return queue.sort(column);
    }
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setValue(String[] values) {
        caQueue.clear();    
        for (int i = 0; i < values.length; i++) {
            caQueue.enqueue(new CharArray(values[i]));  // うーん、、
        }
    }
    
    /** 通常は使わない */
    public void setQueue(QueueTable queue) { this.queue = queue; }
    public void setTable(QueueTable queue) { this.queue = queue; }

    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; }

    public QueueTable getQueue() { return queue; }
    public QueueTable getTable() { return queue; }
    //---------------------------------------------------------------------
    // copy / clone
    //---------------------------------------------------------------------
    public void copy(QueueTableData from) { // 元オブジェクトより全データをコピー
        super.copy(from);
        queue.clear();
        queue.copy(from.queue);
    }
    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする（何もしません）
        @param session SessionObject
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        // do nothing
        return session.getBuffer();
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            queue.writeObject(out);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            queue.readObject(in);
        }
    }

}

//
//
// [end of QueueTableData.java]
//

