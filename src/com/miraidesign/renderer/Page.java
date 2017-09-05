//------------------------------------------------------------------------
//    Page.java
//                 
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
package com.miraidesign.renderer;

//
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.Util;
import com.miraidesign.session.SessionObject;
import com.miraidesign.renderer.item.Item;
import com.miraidesign.renderer.item.StringItem;

/**
 *  表示する１ページのItem管理を行います。
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class Page {
    static private boolean debug = (SystemConst.debug && false);  // デバッグ表示
    static private boolean debugTimer = (SystemConst.debug && false);  // デバッグ表示    
    private Module module;          // 所属するモジュール
    private int pageID;             // page-ID
    protected Vector<Item> vector = new Vector<Item>();     // Itemを保存する
    private int itemID = 0;         // Itemが使用するIDを発版する
    private int hashCode = 0;
    
    
    private String name = "";         // ページ名
    /** ページ名（キーワード）を取得する */
    public String getName() { return name;}
    /** ページ名（キーワード）を設定する */
    public void setName(String name) {
        this.name = name;
        if (module != null) module.setPageName(name,this);
    }
    public void setName(CharArray name) {
        this.name = name.toString();
        if (module != null) module.setPageName(this.name,this);
    }
    
    /** Pageオブジェクトを生成する
    */
    public Page() {
    }
    /** Pageオブジェクトを生成する
        @param pageID pageID
    */
    public Page(int pageID) {
        setPageID(pageID);
    }
    /** Pageオブジェクトを生成する
        @param pageID pageID
        @param name ページ名（キーワード）
    */
    public Page(int pageID, String name) {
        setPageID(pageID);
        setName(name);
    }
    
    /** Module.add(Page) で使用される）*/
    protected void setModule(Module module) { 
        this.module = module;
        hashCode = 0;
    }
    /** 所属するModuleを参照する
        @return モジュール */
    public Module getModule() { return module;}
    
    /** pageID を設定する
        @param pageID pageID */
    public void setPageID(int pageID) { this.pageID = pageID; }
    /** pageID を取得する 
        @return pageID */
    public int getPageID() { return pageID; }
    
    public int getItemID() {    // itemIDを発番する
        if (debug) {
            System.out.println("Page:  itemID 発行："+(itemID+1));
        }
        return ++itemID;
    }
    
    ///** アイテム配列を追加する
    //    @param items 追加するItemの配列 */
    //public synchronized void addItem(Item[] items) {
    //    for (int i = 0; i < items.length; i++) {
    //        addItem(items[i]);
    //    }
    //}
    
    /** 複数アイテムを追加する
        @param items 追加するItem */
    public synchronized void addItem(Item... items) {
        for (int i = 0; i < items.length; i++) {
            addItem(items[i]);
        }
    }
    
    /** アイテムを追加する
        @param item 追加するItem */
    public synchronized void addItem(Item item) {
        if (debug) {
            System.out.println("Page:  addItem："+item.getTypeName()+" start------");
        }
        
        item.setPage(this);
        int id = item.getItemID();
        vector.addElement(item);
        
        if (debug) {
            System.out.println("Page:  addItem："+item.getTypeName()+" end------");
        }
    }
    /** Stringアイテムを追加する
        @param str 追加する文字列 */
    public synchronized void addItem(String str) {
        if (debug) {
            System.out.println("Page:  addItem："+str+" start------");
        }
        StringItem item = new StringItem(str);
        item.setPage(this);
        int id = item.getItemID();
        vector.addElement(item);
        
        
        if (debug) {
            System.out.println("Page:  addItem："+item.getTypeName()+" end------");
        }
    }
    public synchronized void addItem(CharArray ch) {
        if (debug) {
            System.out.println("Page:  addItem："+ch+" start------");
        }
        StringItem item = new StringItem(ch);
        item.setPage(this);
        int id = item.getItemID();
        vector.addElement(item);
        
        
        if (debug) {
            System.out.println("Page:  addItem："+item.getTypeName()+" end------");
        }
    }
    /** アイテムを追加する */
    public void addItem(Item item1, Item item2) {
        addItem(item1);
        addItem(item2);
    }
    public void addItem(String str, Item item2) {
        addItem(str);
        addItem(item2);
    }
    public void addItem(CharArray ch, Item item2) {
        addItem(ch);
        addItem(item2);
    }
    public void addItem(Item item1, String str) {
        addItem(item1);
        addItem(str);
    }
    public void addItem(Item item1, CharArray ch) {
        addItem(item1);
        addItem(ch);
    }
    
    /** アイテムを追加する */
    public void addItem(Item item1, Item item2, Item item3) {
        addItem(item1);
        addItem(item2);
        addItem(item3);
    }
    public void addItem(String str, Item item2, Item item3) {
        addItem(str);
        addItem(item2);
        addItem(item3);
    }
    /** アイテムを追加する */
    public void addItem(Item item1, Item item2, Item item3, Item item4) {
        addItem(item1);
        addItem(item2);
        addItem(item3);
        addItem(item4);
    }
    public void addItem(String str, Item item2, Item item3, Item item4) {
        addItem(str);
        addItem(item2);
        addItem(item3);
        addItem(item4);
    }
    
    /** Item をクリアする */
    public void removeAllItems() {
        clear();
    }
    /** Item をクリアする */
    public synchronized void clear() {
        vector.removeAllElements();
        itemID = 0;
    }
    
/****
    public Item getItem(int itemID) {  // 共通アイテムを取得
        IntObject key = IntObject.pop();
        key.setValue(itemID);
        Item item = (Item)hashItem.get(key);
        IntObject.push(key);

        return item;
    }
***/
    /** Item のリストを返す */
    public Enumeration getItemList() {
        return vector.elements();
    }
    /** Item の hash を返す */
    public Vector getItemTable() {
        return vector;
    }
    
    //----------------------------------------------------------
    // Item情報が存在すればセットする
    //----------------------------------------------------------
    public void setItemInfo() {
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            item.setItemInfo();
        }
    }
    public void setItemInfo(SessionObject session) {
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            item.setItemInfo(session);
        }
    }
    // 
    public void convert(SessionObject session) {
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            item.convert(session);
        }
    }
    /** リセット要求。ユーザーアクション等により発生する
        必要に応じてオーバーライドする
    */
    public void reset(SessionObject session) {};
    
    //----------------------------------------------------------
    // DBの情報をセットする
    //----------------------------------------------------------
    /** 所属するアイテム情報に、ＤＢのカラム名と一致するものを自動設定する。
        @param param  DBから検索してきた１行
        @return 1つでも設定できればtrueを返す
    */
    public boolean setParameter(HashParameter param, int sessionID) {
        boolean rsts = false;
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            rsts |= item.setParameter(param, sessionID);
        }
        return rsts;
    }
    public boolean setParameter(HashParameter param, SessionObject session) {
        boolean rsts = false;
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            rsts |= item.setParameter(param, session);
        }
        return rsts;
    }
    
    /** session情報を再設定する */
        public void setSessionObject(SessionObject session) {
        for (int i = 0; i < vector.size(); i++) {
            Item item = (Item)vector.elementAt(i);
            (item.getItemData(session)).setSessionObject(session);  //@@@//
        }
    }
    
    //----------------------------------------------------------
    // 描画（HTML) デバッグ用
    //----------------------------------------------------------
    public CharArray draw(SessionObject session) {
        long timer = Util.Timer();
        if (debug || debugTimer) {
            System.out.println("---- Pagedraw ★★ ----▼");
        }
        try {
            for (int i = 0; i < vector.size(); i++) {
                Item item = (Item)vector.elementAt(i);
                if (debug) {
                    System.out.println("["+(i+1)+"/"+vector.size()+"]item:"+item.getTypeName());
                    if (item.getType() == Item.STRING) {
                        StringItem stringItem = (StringItem)item;
                        System.out.println(stringItem.getText()+"/"+stringItem.getText(session.getSessionID()));
                    }
                }

                item.draw(session);           // レンダリング
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (debug || debugTimer) System.out.println("---- Pagedraw ★★ ----▲ lapse:"+Util.Lapse(timer));
        return session.getBuffer();
    }

    /** ユーザーごとの描画を行う<br>通常はこれを呼ぶ 
        @param session セッション  */
    public String drawString(SessionObject session) {
        /*if (debug)*/ System.out.println("---- drawString 誰か呼んでる？");
    
        return  draw(session).toString();
    }
    
    //----------------------------------------
    // シリアライズ用（参照のみ復活）
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
        }
    }
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            //ModuleManager mm = SiteManager.get(in.readInt());
            
        
        }
    }
    
    //------------------------------------------
    public int hashCode() {
        int h = hashCode;
        if (h == 0) {
            if (module != null) {
                h = module.getModuleID()*1000 + pageID;
            }
        }
        return h;
    }
    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        //if (obj instanceof Page) {
        //    
        //    
        //}
        return false;
    }
    public int compareTo(Object obj) {
        int sts = -99999;
        if (this == obj) {
            //System.out.println("☆compareTo 0");
            return 0;
        }
        if ((obj != null)) {
            if (obj instanceof Page) {
                int hash = ((Page)obj).hashCode();
                sts =  hashCode() - hash;
            }
        }
        //System.out.print("☆compareTo "+sts);
        return sts;
    }

}

//
// [end of Page.java]
//

