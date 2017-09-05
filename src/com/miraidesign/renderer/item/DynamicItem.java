//------------------------------------------------------------------------
// @(#)DynamicItem.java
//             可変アイテム
//                 Copyright (c) MirfaiDesign 2010-17 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;
import java.util.Enumeration;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.IntObject;
import com.miraidesign.session.SessionObject;
import com.miraidesign.renderer.Page;

/**
 *  可変アイテム
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
**/
public class DynamicItem extends Item {

    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /**
        DynamicItemオブジェクトを生成する
    */
    public DynamicItem() { 
        super();
        init();
        itemData = new DynamicData();
        itemData.setItem(this);
    }
    public DynamicItem(CharArray value) { 
        super();
        init();
        itemData = new DynamicData(value);
        itemData.setItem(this);
    }
    public DynamicItem(String value) { 
        super();
        init();
        itemData = new DynamicData(value);
        itemData.setItem(this);
    }
    /**
        @param page デフォルトの飛び先ページ
    */
    public DynamicItem(Page page) { 
        super();
        init();
        itemData = new DynamicData(page);
        itemData.setItem(this);
        if (page == null) {
            System.out.println("☆☆☆ DynamicItem Page が null です ");
        }
    }

    /**
        @param value ユーザー設定値
        @param page デフォルトの飛び先ページ
    */
    public DynamicItem(CharArray value,Page page) { 
        super();
        init();
        itemData = new DynamicData(value, page);
        itemData.setItem(this);
        if (page == null) {
            System.out.println("☆☆☆ DynamicItem Page が null です ");
        }
    }

    /**
        @param value ユーザー設定値
        @param page デフォルトの飛び先ページ
    */
    public DynamicItem(String value,Page page) { 
        super();
        init();
        itemData = new DynamicData(value, page);
        itemData.setItem(this);
        if (page == null) {
            System.out.println("☆☆☆ DynamicItem Page が null です ");
        }
    }

    /**
        DynamicItemオブジェクトを生成する
    */
    public DynamicItem(DynamicItem from) { 
        super();
        setType(DYNAMIC);
        setCloneable(from.isCloneable());
        DynamicData fromdata = (DynamicData)from.itemData;
        itemData = new DynamicData(fromdata,fromdata.getSessionObject());
        itemData.setItem(this);
    }

    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(DYNAMIC);
        setCloneable(true);
    }
    
    //---------------------------------------------------------------------
    // データ設定
    //---------------------------------------------------------------------
    /** Dynamic DataのItemDataをクリアする */
    public void clear() {
        ((DynamicData)itemData).clear();
    }
    public void clear(int sessionID) {
        ((DynamicData)getItemData(sessionID)).clear();
    }
    public void clear(SessionObject session) {
        ((DynamicData)getItemData(session)).clear();
    }
    /** Dynamic Data に ItemData を追加する */
    public void add(ItemData itemData) {
        //itemData.setItem(this);
        ((DynamicData)itemData).add(itemData);
    }
    public void add(ItemData itemData, int sessionID) {
        //itemData.setItem(this);
        ((DynamicData)getItemData(sessionID)).add(itemData);
    }
    public void add(ItemData itemData, SessionObject session) {
        //itemData.setItem(this);
        ((DynamicData)getItemData(session)).add(itemData);
    }

    public void addItemData(ItemData itemData) {
        //itemData.setItem(this);
        ((DynamicData)itemData).add(itemData);
    }
    public void addItemData(ItemData itemData, int sessionID) {
        //itemData.setItem(this);
        ((DynamicData)getItemData(sessionID)).add(itemData);
    }
    public void addItemData(ItemData itemData, SessionObject session) {
        //itemData.setItem(this);
        ((DynamicData)getItemData(session)).add(itemData);
    }

    //---------------------------------------------------------------------
    // データ取得メソッド
    //---------------------------------------------------------------------
    
    
    //---------------------------------------------------------------------
    // copy 
    //---------------------------------------------------------------------
    /** デフォルトのオブジェクトをコピーしてユーザーオブジェクトを作成する
        @param session セッションオブジェクト
    */
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new DynamicData((DynamicData)itemData,session);
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
        DynamicData data = (DynamicData)getItemData(session); //@@@//
        CharArray ch = session.getBuffer();
        if (data.isVisible()) {
            int sessionID = session.getSessionID();
            for (Enumeration e = data.getItemDataList(); e.hasMoreElements();) {
                ItemData itemData = (ItemData)e.nextElement();
                itemData.draw(session);
            }
        }
        return ch;
    }
    
    
    // stream 版
    //public void draw(OutputStream out) {
        //
    //}
    public void draw(OutputStream out, int sessionID) {
        //
    }

}

//
//
// [end of DynamicItem.java]
//

