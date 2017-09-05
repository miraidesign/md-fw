//------------------------------------------------------------------------
// @(#)ItemInfo.java
//                 <br>
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//

package com.miraidesign.renderer;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.QueueTable;
import com.miraidesign.util.HashParameter;
import com.miraidesign.session.SessionObject;
import com.miraidesign.renderer.item.Item;
import com.miraidesign.renderer.item.ItemData;
import com.miraidesign.renderer.item.ItemDataSelectable;

/** アイテム情報クラス(item.txtを読み込む) */
public class ItemInfo {
    private static final boolean debug = false;
    private CharArray key = new CharArray();
    private QueueTable table = new QueueTable();
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /** constructor */
    public ItemInfo() { 
        
    }
    /**
     * 初期化メソッド (Item情報を読み込む)
     */
    public int init(CharArrayQueue queue, int start) {
        table.clearAll();
if (debug) System.out.println("--v start:"+start);
        //int sts =  table.readText(queue, ",","#",":","#",start,true,":","#");
        int sts =  table.readText(queue, ",","[","]","//",start,true,":","]");
if (debug) System.out.println("--dump--");
if (debug) table.dumpTable();
if (debug) System.out.println("--^ end:"+sts);
        return sts;
    }
    
    //---------------------------------------------------------------------
    // 
    //---------------------------------------------------------------------
    /** キー情報を取得する */
    public CharArray getKey() { return table.getTitle().trim(); }
    /** テーブル情報を取得する */
    public QueueTable getTable() { return table; }
    
    //---------------------------------------------------------------------
    // ItemDataにセット
    //---------------------------------------------------------------------
    /** Item から呼ばれる */
    public void setInfo(Item item) {
        if (item == null) return;
        ItemData itemData = item.getItemData();
        setInfo(itemData);
    }
    public void setInfo(ItemData itemData) {
        if (itemData != null && itemData instanceof ItemDataSelectable) {
if (debug) System.out.println("★setInfo["+itemData.getItem().itemInfoKey+"]");
            ((ItemDataSelectable)itemData).clear();
            SessionObject session = itemData.getSessionObject();
            if (session != null) itemData.getItem().checkStart(session);
            for (int j = 0; j < table.getRowCount(); j++) {
if (debug) System.out.println(" ("+j+")"+table.getCharArray(j,"menu"));
                if (table.getBoolean(j,"visible")) {
                    if (session == null) {
                        ((ItemDataSelectable)itemData).add(table.getCharArray(j,"menu").trim(),
                                   table.getCharArray(j,"option").trim(),
                                   table.getCharArray(j,"resource").trim(),
                                   table.getBoolean(j,"selected"));
                    } else {
                        HashParameter param = table.getHashParameter(j);
                        param.add("LINE",j);
                        param.add("END",(j == (table.getRowCount()-1)));
                        boolean status = itemData.getItem().checkInfo(session,param);
                        if (status) {
                            CharArray ch = table.getCharArray(j,"menu").trim();
                            CharArray msg = session.getMessage(ch.toString());
                            ((ItemDataSelectable)itemData).add((msg != null) ? msg : ch,
                                     table.getCharArray(j,"option").trim(),
                                     table.getCharArray(j,"resource").trim(),
                                     table.getBoolean(j,"selected"));
                        }
                    }
                }
            } // next
            if (session != null) itemData.getItem().checkEnd(session);
        }
    }
}

//
// [end of ItemInfo.java]
//

