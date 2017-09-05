//------------------------------------------------------------------------
// @(#)QueueTableItem.java
//                 データのテーブル形式での保管を行う（描画はしない）
//                 Copyright (c) Miraidesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//             データ処理は存在しない
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.Queue;
import com.miraidesign.util.QueueElement;
import com.miraidesign.util.QueueTable;

/** データのテーブル形式での保管を行う（描画はしない）*/
public class QueueTableItem extends Item {
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public QueueTableItem() { 
        super();
        setType(QUEUE_TABLE);
        setCloneable(true);
        itemData = new QueueTableData();
        itemData.setItem(this);
    }

    /** copy constructor **/
    public QueueTableItem(QueueTableItem from) { 
        super();
        setType(QUEUE_TABLE);
        setCloneable(from.isCloneable());
        QueueTableData fromdata = (QueueTableData)from.itemData;
        itemData = new QueueTableData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }
    
    //---------------------------------------------------------------------
    // データ設定
    //---------------------------------------------------------------------
    /** 全データとカラムをクリアする*/
    public  void clearAll() {
        ((QueueTableData)itemData).clearAll();
    }
    public  void clearAll(int sessionID) {
        ((QueueTableData)getItemData(sessionID)).clearAll();
    }
    public  void clearAll(SessionObject session) {
        ((QueueTableData)getItemData(session)).clearAll();
    }
    /** 全データ（行）をクリアする (カラムはそのまま)*/
    public  void clear() {
        ((QueueTableData)itemData).clear();
    }
    public  void clear(int sessionID) {
        ((QueueTableData)getItemData(sessionID)).clear();
    }
    public  void clear(SessionObject session) {
        ((QueueTableData)getItemData(session)).clear();
    }
    
    /** 指定行を削除する
        @param row 削除する行 */
    public  boolean removeRow(int row) {
        return ((QueueTableData)itemData).removeRow(row);
    }
    public  boolean removeRow(int row,int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).removeRow(row);
    }
    public  boolean removeRow(int row,SessionObject session) {
        return ((QueueTableData)getItemData(session)).removeRow(row);
    }
    
    /** カラム追加  */
    public  void addColumn(int type) {
        ((QueueTableData)itemData).addColumn(type);
    }
    public  void addColumn(int type, int sessionID) {
        ((QueueTableData)getItemData(sessionID)).addColumn(type);
    }
    public  void addColumn(int type, SessionObject session) {
        ((QueueTableData)getItemData(session)).addColumn(type);
    }
    /** カラム追加  */
    public  void addColumn(int type, CharArray name) {
        ((QueueTableData)itemData).addColumn(type, name);
    }
    public  void addColumn(int type, int sessionID, CharArray name) {
        ((QueueTableData)getItemData(sessionID)).addColumn(type,name);
    }
    public  void addColumn(int type, SessionObject session, CharArray name) {
        ((QueueTableData)getItemData(session)).addColumn(type,name);
    }
    /** カラム追加  */
    public  void addColumn(int type, String name) {
        ((QueueTableData)itemData).addColumn(type, name);
    }
    public  void addColumn(int type, int sessionID, String name) {
        ((QueueTableData)getItemData(sessionID)).addColumn(type,name);
    }
    public  void addColumn(int type, SessionObject session, String name) {
        ((QueueTableData)getItemData(session)).addColumn(type,name);
    }
    /** カラムの削除  */
    public  void removeCoumn(int index) {
        ((QueueTableData)itemData).removeColumn(index);
    }
    public  void removeColumn(int index, int sessionID) {
        ((QueueTableData)getItemData(sessionID)).removeColumn(index);
    }
    public  void removeColumn(int index, SessionObject session) {
        ((QueueTableData)getItemData(session)).removeColumn(index);
    }
    
    /** カラムデータの取得 
        @param index カラムindex(0- )
        @return IntQueue/LongQueue/DoubleQueue/Boolean/CharArrayQueue のどれか
    */
    public  Queue getColumn(int index) {
        return ((QueueTableData)itemData).getColumn(index);
    }
    public  Queue getColumn(int index, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getColumn(index);
    }
    public  Queue getColumn(int index, SessionObject session) {
        return ((QueueTableData)getItemData(session)).getColumn(index);
    }
    
    /** カラムデータの取得 
        @param name カラム名
        @return IntQueue/LongQueue/DoubleQueue/Boolean/CharArrayQueue のどれか
    */
    public  Queue getColumn(CharArray name) {
        return ((QueueTableData)itemData).getColumn(name);
    }
    public  Queue getColumn(CharArray name, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getColumn(name);
    }
    public  Queue getColumn(CharArray name, SessionObject session) {
        return ((QueueTableData)getItemData(session)).getColumn(name);
    }
    /** カラムインデックスを取得する
        @param name カラム名
        @return カラムインデックス (0-)  -1:存在しない
    */
    public  int getColumnIndex(CharArray name) {
        return ((QueueTableData)itemData).getColumnIndex(name);
    }
    public  int getColumnIndex(CharArray name, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getColumnIndex(name);
    }
    public  int getColumnIndex(CharArray name, SessionObject session) {
        return ((QueueTableData)getItemData(session)).getColumnIndex(name);
    }
    
    /** カラムタイプを取得する
        @return Queue.INT/LONG/BOOL/DOUBLE/STRING
    */
    public  int getColumnType(int index) {
        return ((QueueTableData)itemData).getColumnType(index);
    }
    public  int getColumnType(int index, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getColumnType(index);
    }
    public  int getColumnType(int index, SessionObject session) {
        return ((QueueTableData)getItemData(session)).getColumnType(index);
    }

    /** カラム数を取得する  */
    public  int getColumnCount() {
        return ((QueueTableData)itemData).getColumnCount();
    }
    public  int getColumnCount(int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getColumnCount();
    }
    public  int getColumnCount(SessionObject session) {
        return ((QueueTableData)getItemData(session)).getColumnCount();
    }

    /* 行数を取得する */
    public  int getRowCount() {
        return ((QueueTableData)itemData).getRowCount();
    }
    public  int getRowCount(int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getRowCount();
    }
    public  int getRowCount(SessionObject session) {
        return ((QueueTableData)getItemData(session)).getRowCount();
    }

    /** 値を取得する（設定したカラムタイプに対応したメソッドを呼ぶ）
        @param row      行      (0- )
        @param column   カラム  (0- )
        @return 値
    */
    public  int getInt(int row, int column) {
        return ((QueueTableData)itemData).getInt(row,column);
    }
    public  int getInt(int row, int column, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getInt(row,column);
    }
    public  int getInt(int row, int column, SessionObject session) {
        return ((QueueTableData)getItemData(session)).getInt(row,column);
    }
    /**   */
    public  long getLong(int row, int column) {
        return ((QueueTableData)itemData).getLong(row,column);
    }
    public  long getLong(int row, int column, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getLong(row,column);
    }
    public  long getLong(int row, int column, SessionObject session) {
        return ((QueueTableData)getItemData(session)).getLong(row,column);
    }
    /**   */
    public  boolean getBoolean(int row, int column) {
        return ((QueueTableData)itemData).getBoolean(row,column);
    }
    public  boolean getBoolean(int row, int column, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getBoolean(row,column);
    }
    public  boolean getBoolean(int row, int column, SessionObject session) {
        return ((QueueTableData)getItemData(session)).getBoolean(row,column);
    }
    /**   */
    public  double getDouble(int row, int column) {
        return ((QueueTableData)itemData).getDouble(row,column);
    }
    public  double getDouble(int row, int column, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getDouble(row,column);
    }
    public  double getDouble(int row, int column, SessionObject session) {
        return ((QueueTableData)getItemData(session)).getDouble(row,column);
    }
    /**   */
    public  CharArray getCharArray(int row, int column) {
        return ((QueueTableData)itemData).getCharArray(row,column);
    }
    public  CharArray getCharArray(int row, int column, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getCharArray(row,column);
    }
    public  CharArray getCharArray(int row, int column, SessionObject session) {
        return ((QueueTableData)getItemData(session)).getCharArray(row,column);
    }

    /** 値を設定する（設定したカラムタイプに対応したメソッドを呼ぶ）
        @param value    設定する値
        @param row      行      (0- )
        @param column   カラム  (0- )
        @return true で成功
    */
    public  boolean setInt(int value, int row, int column) {
        return ((QueueTableData)itemData).setInt(value,row,column);
    }
    public  boolean setInt(int value, int row, int column, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).setInt(value,row,column);
    }
    public  boolean setInt(int value, int row, int column, SessionObject session) {
        return ((QueueTableData)getItemData(session)).setInt(value,row,column);
    }
    /**   */
    public  boolean setLong(long value, int row, int column) {
        return ((QueueTableData)itemData).setLong(value,row,column);
    }
    public  boolean setLong(long value, int row, int column, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).setLong(value,row,column);
    }
    public  boolean setLong(long value, int row, int column, SessionObject session) {
        return ((QueueTableData)getItemData(session)).setLong(value,row,column);
    }
    /**   */
    public  boolean setBoolean(boolean value, int row, int column) {
        return ((QueueTableData)itemData).setBoolean(value,row,column);
    }
    public  boolean setBoolean(boolean value, int row, int column, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).setBoolean(value,row,column);
    }
    public  boolean setBoolean(boolean value, int row, int column, SessionObject session) {
        return ((QueueTableData)getItemData(session)).setBoolean(value,row,column);
    }
    /**   */
    public  boolean setDouble(double value, int row, int column) {
        return ((QueueTableData)itemData).setDouble(value,row,column);
    }
    public  boolean setDouble(double value, int row, int column, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).setDouble(value,row,column);
    }
    public  boolean setDouble(double value, int row, int column, SessionObject session) {
        return ((QueueTableData)getItemData(session)).setDouble(value,row,column);
    }
    /**   */
    public  boolean setCharArray(CharArray value, int row, int column) {
        return ((QueueTableData)itemData).setCharArray(value,row,column);
    }
    public  boolean setCharArray(CharArray value, int row, int column, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).setCharArray(value,row,column);
    }
    public  boolean setCharArray(CharArray value, int row, int column, SessionObject session) {
        return ((QueueTableData)getItemData(session)).setCharArray(value,row,column);
    }

    /* Element（1行のデータ)を取得する 
        @param row  取得する行
        @return QueueElement オブジェクト
    */
    public  QueueElement getElement(int row) {
        return ((QueueTableData)itemData).getElement(row);
    }
    public  QueueElement getElement(int row, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getElement(row);
    }
    public  QueueElement getElement(int row, SessionObject session) {
        return ((QueueTableData)getItemData(session)).getElement(row);
    }

    /** １行を設定する 
        @param row  設定する行
        @param element 設定するエレメント
    */
    public  boolean setElement(int row, QueueElement element) {
        return ((QueueTableData)itemData).setElement(row, element);
    }
    public  boolean setElement(int row, QueueElement element, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).setElement(row,element);
    }
    public  boolean setElement(int row, QueueElement element, SessionObject session) {
        return ((QueueTableData)getItemData(session)).setElement(row,element);
    }

    /** １行を追加する 
        @param element 追加するエレメント
    */
    public  boolean addElement(QueueElement element) {
        return ((QueueTableData)itemData).addElement(element);
    }
    public  boolean addElement(QueueElement element, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).addElement(element);
    }
    public  boolean addElement(QueueElement element, SessionObject session) {
        return ((QueueTableData)getItemData(session)).addElement(element);
    }

    /** 空行を追加する */
    public  boolean addElement() {
        return ((QueueTableData)itemData).addElement();
    }
    public  boolean addElement(int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).addElement();
    }
    public  boolean addElement(SessionObject session) {
        return ((QueueTableData)getItemData(session)).addElement();
    }

    /** 指定カラムでソートする 
        @param column ソートするカラム
        @return true:成功
    */
    public  boolean sort(int column) {
        return ((QueueTableData)itemData).sort(column);
    }
    public  boolean sort(int column, int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).sort(column);
    }
    public  boolean sort(int column, SessionObject session) {
        return ((QueueTableData)getItemData(session)).sort(column);
    }

    //---------------------------------------------------------------------
    //
    //---------------------------------------------------------------------
    /** テーブルの置き換え */
    public  void setTable(QueueTable queue) {
        ((QueueTableData)itemData).setTable(queue);
    }
    public  void setTable(QueueTable queue, int sessionID) {
        ((QueueTableData)getItemData(sessionID)).setTable(queue);
    }
    public  void setTable(QueueTable queue, SessionObject session) {
        ((QueueTableData)getItemData(session)).setTable(queue);
    }
    public  void setQueue(QueueTable queue) {
        ((QueueTableData)itemData).setQueue(queue);
    }
    public  void setQueue(QueueTable queue, int sessionID) {
        ((QueueTableData)getItemData(sessionID)).setQueue(queue);
    }
    public  void setQueue(QueueTable queue, SessionObject session) {
        ((QueueTableData)getItemData(session)).setQueue(queue);
    }
    //---------------------------------------------------------------------
    //
    //---------------------------------------------------------------------
    /** テーブルの取得 */
    public  QueueTable getTable() {
        return ((QueueTableData)itemData).getTable();
    }
    public  QueueTable getTable(int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getTable();
    }
    public  QueueTable getTable(SessionObject session) {
        return ((QueueTableData)getItemData(session)).getTable();
    }
    public  QueueTable getQueue() {
        return ((QueueTableData)itemData).getQueue();
    }
    public  QueueTable getQueue(int sessionID) {
        return ((QueueTableData)getItemData(sessionID)).getQueue();
    }
    public  QueueTable getQueue(SessionObject session) {
        return ((QueueTableData)getItemData(session)).getQueue();
    }
    
    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new QueueTableData((QueueTableData)itemData, session);
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
        QueueTableData data = (QueueTableData)getItemData(session);
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
// [end of QueueTableItem.java]
//

