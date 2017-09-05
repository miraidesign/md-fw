//------------------------------------------------------------------------
// @(#)DynamicData.java
//         可変データを保管する
//                 Copyright (c) MiraiDesign 2010-17 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Enumeration;

import com.miraidesign.session.SessionObject;
import com.miraidesign.renderer.Page;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/**
 *  可変データ(ItemData)を保管する
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class DynamicData extends DynamicItemData {
    private CharArray value = new CharArray();  // ユーザー定義値
    private Page page;
    {
        caQueue.enqueue(value);
        type = DYNAMIC;
    }
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public DynamicData() {
    }
    public DynamicData(CharArray ch) {
        value.set(ch);
    }
    public DynamicData(String str) {
        value.set(str);
    }
    public DynamicData(Page page) {
        this.page = page;
    }
    /**
        @param ch     ユーザー定義値(value)
        @param page   Jump Page
    */
    public DynamicData(CharArray ch, Page page) {
        this.page = page;
        value.set(ch);
    }
    /**
        @param str    ユーザー定義値(value)
        @param page   Jump Page
    */
    public DynamicData(String str, Page page) {
        this.page = page;
        value.set(str);
    }
    public DynamicData(DynamicData from, SessionObject session) {
        this.sessionObject = session;
        copy(from); 
    }
    //---------------------------------------------------------------------
    
    /** ItemData を追加する **/
    /**
    public void add(ItemData itemData) {
        //if (itemData.getItem() != getItem()) {
        //    System.out.println("DynamicData に追加する "+itemData.getTypeName()+
        //    "に設定されている親アイテムが違います");
        //}
        itemData.setItem(getItem());    // 親アイテムの設定
        itemData.setSessionObject(sessionObject);
        itemData.setCount(queue.size());
        queue.enqueue(itemData);
    }
    **/

    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setJumpPage(Page page) {
        this.page = page;
    }
    public void setValue(CharArray ch) { value.set(ch); }
    public void setValue(String str) { value.set(str); }
    public void setValue(String[] strs) { value.set(strs[0]); }

    public void setSessionObject(SessionObject session) {
        super.setSessionObjectAll(session);
    }
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; }
    public CharArray getData() { return value; }
    
    public Page getJumpPage() {
        if (page == null) {
            System.out.println("☆☆☆ DynamicData.getJumpPage Page が null です ☆☆");
        }
        return page;
    }
    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    public void copy(DynamicData from) { // 元オブジェクトより全データをコピー
        super.copyAll(from);
        /**
        queue.clear();
        for (Enumeration e = from.queue.elements(); e.hasMoreElements();) {
            ItemData item = ItemData.createItemData((ItemData)e.nextElement(),getItem(), sessionObject);
            queue.enqueue(item);
        }
        **/
        this.page = from.page;
        value.set(from.value);
    }

    //---------------------------------------------------------------------
    //
    //---------------------------------------------------------------------
    /** レンダリングする
        @param session セッション
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        CharArray ch = session.getBuffer();
        if (visible) {
            for (Enumeration e = queue.elements(); e.hasMoreElements();) {
                ItemData itemData = (ItemData)e.nextElement();
                itemData.draw(session);
            }
        }
        return ch;
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            super.writeObject(out);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            super.readObject(in);
        }
    }
    
}

//
//
// [end of DynamicData.java]
//

