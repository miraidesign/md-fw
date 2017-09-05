//------------------------------------------------------------------------
// @(#)ItemContainer.java
//                 
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.renderer.Page;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.HashParameter;

/** Itemコンテナ */
public class ItemContainer extends Item {
    static private boolean debug = (SystemConst.debug && false);  // デバッグ表示
    private Vector<Item> vector = new Vector<Item>();      // Item のコンテナ
    //-----------------------------------------------
    // constructor
    //-----------------------------------------------
    public ItemContainer() {
        setType(CONTAINER);
        setCloneable(false);
    }
    public ItemContainer(ItemContainer from) {
        setType(CONTAINER);
        setCloneable(from.isCloneable());
        copy(from);
    }
    
    /** ページをセットする */
    public void setPage(Page page) {  
        //this.page = page;
        //if (itemID <= 0) super.setItemID(page.getItemID());
        super.setPage(page);
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            item.setPage(page);
        }
    }

    /** Item を追加する */
    public void addItem(Item item) {
        if (page != null) item.setPage(page);
        item.setParent(this);
        vector.addElement(item);
    }
    
    /** StringItem を追加する */
    public void addItem(String str) {
        StringItem item = new StringItem(str);
        if (page != null) item.setPage(page);
        item.setParent(this);
        vector.addElement(item);
    }
    
    /** StringItem を追加する */
    public void addItem(CharArray ch) {
        StringItem item = new StringItem(ch);
        if (page != null) item.setPage(page);
        item.setParent(this);
        vector.addElement(item);
    }
    
    /** Item配列を追加する */
    public void addItem(Item... items) {
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                addItem(items[i]);
            }
        }
    }
    /** StringItem を追加する */
    public void addItem(String... strs) {
        if (strs != null) {
            for (int i = 0; i < strs.length; i++) {
                StringItem item = new StringItem(strs[i]);
                if (page != null) item.setPage(page);
                item.setParent(this);
                vector.addElement(item);
            }
        }
    }
    
    /** StringItem を追加する */
    public void addItem(CharArray... chs) {
        if (chs != null) {
            for (int i = 0; i < chs.length; i++) {
                StringItem item = new StringItem(chs[i]);
                if (page != null) item.setPage(page);
                item.setParent(this);
                vector.addElement(item);
            }
        }
    }
    
    public void addItem(String str, Item... items) {
        addItem(str);
        addItem(items);
    }
    public void addItem(CharArray ch, Item... items) {
        addItem(ch);
        addItem(items);
    }
    public void addItem(Item item1, String... strs) {
        addItem(item1);
        addItem(strs);
    }
    public void addItem(Item item1, CharArray... chs) {
        addItem(item1);
        addItem(chs);
    }
    
    /** 指定された位置に登録されたItemを取得する*/
    public Item getItem(int index) {
        return (Item)vector.elementAt(index);
    }
    public Enumeration getItemList() {
        return vector.elements();
    }
    public Vector getItemTable() {
        return vector;
    }
    public int getItemCount() {
        return vector.size();
    }
    public void removeAllItems() {
        vector.removeAllElements();
    }
    
    public void setItemID(int itemID) {
        super.setItemID(itemID);
        if (page != null) {
            for (int i = 0; i < vector.size(); i++) {
                getItem(i).setItemID(page.getItemID());
            }
        }
    }
    /** コンテナのcopy */
    public void copy(ItemContainer from) {
        vector.removeAllElements();
        for (Enumeration e = from.getItemList(); e.hasMoreElements(); ) {
            Item item = (Item)e.nextElement();
            vector.addElement(Item.createItem(item)); // 新たにItemを生成
        }
    }
    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    public void copy(SessionObject session) {
        if (isCloneable()) {
           //今はデータを持っていない
        }
        for (int i = 0; i < vector.size(); i++) {
            getItem(i).copy(session);
        }
    }
    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    public CharArray drawContainer(SessionObject session) {
        //if (isVisible(sessionID)) {
        for (int i = 0; i < vector.size(); i++) {
            getItem(i).draw(session);
        }
        //}
        return session.getBuffer();
    }
    
    public CharArray draw(SessionObject session) {
        CharArray ch = session.getBuffer();
        //if (isVisible(sessionID)) {
            drawContainer(session);  // コンテナ内のオブジェクト群を描画する
        //}
        return ch;
    }
    
    // stream 版
    public void draw(OutputStream out) {
        //未作成
    }
    public void draw(OutputStream out, int sessionID) {
        //未作成
    }
    
    /** PageServlet#init() の後に呼ばれる */
    public void setItemInfo() {  
        super.setItemInfo();
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            item.setItemInfo();
        }
    }
    
    /** PageServletから呼ばれる <br>
        通常はユーザーログイン後に呼ばれる
    */
    public void setItemInfo(SessionObject session) {  
        super.setItemInfo(session);
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            item.setItemInfo(session);
        }
    }
    
    /** PageServletから呼ばれる
    */
    public boolean setParameter(HashParameter param, int sessionID) {
        boolean rsts = false;
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            rsts |= item.setParameter(param, sessionID);
        }
        return false;
    }
    public boolean setParameter(HashParameter param, SessionObject session) {
        boolean rsts = false;
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            rsts |= item.setParameter(param, session);
        }
        return false;
    }
    
}

//
//
// [end of ItemContainer.java]
//

