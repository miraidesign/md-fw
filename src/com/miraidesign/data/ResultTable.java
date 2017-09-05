//------------------------------------------------------------------------
//@(#)ResultTable.java
//          検索結果保管テーブル
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------
//
//
package com.miraidesign.data;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.AbstractBidirectionalNode;
import com.miraidesign.util.QueueTable;
import com.miraidesign.system.SystemManager;

/**
 *  ResultTable 
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ResultTable extends AbstractBidirectionalNode {
    protected boolean debug = false;         // デバッグ表示

    static public int total = 0;

    //protected BidirectionalNode prev;     // 直前ノード
    //protected BidirectionalNode next;     // 次のノード
    
    public long startTime;
    public long lastTime;
    public DataAccessObject dao;
    public QueueTable table = new QueueTable();
    
    public int index = 0;   // debug
    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------
    public ResultTable() {
        index = ++total;
        startTime = lastTime = System.currentTimeMillis();
        debug &= SystemConst.debug;
        //if (SystemManager.tableManager != null) SystemManager.tableManager.append(this);
    }
    public ResultTable(DataAccessObject dao) {
        index = ++total;
        startTime = lastTime = System.currentTimeMillis();
        debug &= SystemConst.debug;
        this.dao = dao;
        //if (SystemManager.tableManager != null) SystemManager.tableManager.append(this);
    }
    public ResultTable(ResultTable from) {
        index = ++total;
        copy(from.getTable());
        startTime = lastTime = System.currentTimeMillis();
        debug &= SystemConst.debug;
        //if (SystemManager.tableManager != null) SystemManager.tableManager.append(this);
    }
    
    //---------------------------------------------------------
    //
    //---------------------------------------------------------
    public void copy(QueueTable from) {
        table.copy(from);
    }
    public void setTable(QueueTable table) {
        this.table = table;
    }
    public QueueTable getTable() { 
        return table;
    }
    /** これが呼ばれるともう使用できない */
    public void close() {   
        dao = null;
        table.reset();
        table = null;
    }
    //---------------------------------------------------------
    // 双方向ノードインターフェース
    //---------------------------------------------------------
/**
    public BidirectionalNode getPrev() {
        return prev;
    }
    public BidirectionalNode getNext() {
        return next;
    }
    public void setPrev(BidirectionalNode node) {
        prev = node;
    }
    public void setNext(BidirectionalNode node) {
        next = node;
    }
**/
    //-----------------------------------------------------------
    //
    //-----------------------------------------------------------
    public void update() {
        lastTime = System.currentTimeMillis();
        //if (SystemManager.tableManager != null) SystemManager.tableManager.update(this);
    }

}

//
// [end of ResultTable.java]
//

