//------------------------------------------------------------------------
// @(#)ListBoxItem.java
//                 <select> のデータを保管する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.BooleanQueue;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.IntQueue;

/**
 *  リストボックス
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ListBoxItem extends Item {
    CharArray key = new CharArray();  // DBカラム等
    
    /** DBカラムを取得する */
    public CharArray getKey() { return key; }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public ListBoxItem() { 
        super();
        init();
        itemData = new ListBoxData();
        itemData.setItem(this);
    }
    public ListBoxItem(String key) { // DB キーを設定する
        super();
        this.key.set(key);
        init();
        itemData = new ListBoxData();
        itemData.setItem(this);
    }
    public ListBoxItem(CharArray key) { // DB キーを設定する
        super();
        this.key.set(key);
        init();
        itemData = new ListBoxData();
        itemData.setItem(this);
    }
    public ListBoxItem(int size) {
        super();
        init();
        itemData = new ListBoxData(size);
        itemData.setItem(this);
    }
    public ListBoxItem(CharArrayQueue menu) { 
        super();
        init();
        itemData = new ListBoxData(menu);
        itemData.setItem(this);
    }
    /** リストボックスアイテムを生成する
        @param menu   メニューの文字列
        @param option オプション文字列
    **/
    public ListBoxItem(CharArrayQueue menu, CharArrayQueue option) { 
        super();
        init();
        itemData = new ListBoxData(menu,option);
        itemData.setItem(this);
    }
    /** copy constructor 
        @param from コピー元のリストボックスアイテム
    **/
    public ListBoxItem(ListBoxItem from) { 
        super();
        setType(LIST_BOX);
        setCloneable(from.isCloneable());
        ListBoxData fromdata = (ListBoxData)from.itemData;
        itemData = new ListBoxData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }
    
    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(LIST_BOX);
        setCloneable(true); // 選択値が必要なので
    }
    
    //---------------------------------------------------------------------
    // データ設定メソッド
    //---------------------------------------------------------------------
    /** デフォルトの選択値の設定 (-1でなし） */
    public void setSelectedIndex(int index) {
        ((ListBoxData)itemData).setSelectedIndex(index);
    }
    public void setSelectedIndex(int index, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setSelectedIndex(index);
    }
    public void setSelectedIndex(int index, SessionObject session) {
        ((ListBoxData)getItemData(session)).setSelectedIndex(index);
    }
    //----------------------------------------------------
    public void setMenu(CharArrayQueue menu) {
        ((ListBoxData)itemData).setMenu(menu);
    }
    public void setMenu(CharArrayQueue menu, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setMenu(menu);
    }
    public void setMenu(CharArrayQueue menu, SessionObject session) {
        ((ListBoxData)getItemData(session)).setMenu(menu);
    }
    //----------------------------------------------------
    public void setOption(CharArrayQueue option) {
        ((ListBoxData)itemData).setOption(option);
    }
    public void setOption(CharArrayQueue option, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setOption(option);
    }
    public void setOption(CharArrayQueue option, SessionObject session) {
        ((ListBoxData)getItemData(session)).setOption(option);
    }
    //----------------------------------------------------
    public void setQueue(BooleanQueue queue) {
        ((ListBoxData)itemData).setQueue(queue);
    }
    public void setQueue(BooleanQueue queue, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setQueue(queue);
    }
    public void setQueue(BooleanQueue queue, SessionObject session) {
        ((ListBoxData)getItemData(session)).setQueue(queue);
    }
    //-----------------------------------------------------------
    /** 指定されたものがOPTIONかMENUにあればselectedにする(OPTION優先） */
    public void setSelectedItem(String str) {
        ((ListBoxData)itemData).setSelectedItem(str);
    }
    public void setSelectedItem(String str, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setSelectedItem(str);
    }
    public void setSelectedItem(String str, SessionObject session) {
        ((ListBoxData)getItemData(session)).setSelectedItem(str);
    }
    public void setSelectedItem(CharArray ch) {
        ((ListBoxData)itemData).setSelectedItem(ch);
    }
    public void setSelectedItem(CharArray ch, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setSelectedItem(ch);
    }
    public void setSelectedItem(CharArray ch, SessionObject session) {
        ((ListBoxData)getItemData(session)).setSelectedItem(ch);
    }
    //-----------------------------------------------------------
    /** 指定されたものがMENUにあればselectedにする */
    public void setSelectedMenu(String str) {
        ((ListBoxData)itemData).setSelectedMenu(str);
    }
    public void setSelectedMenu(String str, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setSelectedMenu(str);
    }
    public void setSelectedMenu(String str, SessionObject session) {
        ((ListBoxData)getItemData(session)).setSelectedMenu(str);
    }
    public void setSelectedMenu(CharArray ch) {
        ((ListBoxData)itemData).setSelectedMenu(ch);
    }
    public void setSelectedMenu(CharArray ch, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setSelectedMenu(ch);
    }
    public void setSelectedMenu(CharArray ch, SessionObject session) {
        ((ListBoxData)getItemData(session)).setSelectedMenu(ch);
    }
    //-----------------------------------------------------------
    /** 指定されたものがOPTIONにあればselectedにする */
    public void setSelectedOption(String str) {
        ((ListBoxData)itemData).setSelectedOption(str);
    }
    public void setSelectedOption(String str, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setSelectedOption(str);
    }
    public void setSelectedOption(String str, SessionObject session) {
        ((ListBoxData)getItemData(session)).setSelectedOption(str);
    }
    public void setSelectedOption(CharArray ch) {
        ((ListBoxData)itemData).setSelectedOption(ch);
    }
    public void setSelectedOption(CharArray ch, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setSelectedOption(ch);
    }
    public void setSelectedOption(CharArray ch, SessionObject session) {
        ((ListBoxData)getItemData(session)).setSelectedOption(ch);
    }
    
    /** 指定された文字列が、resourceにあれば selectedにする */
    public void setSelectedResource(String str) {
        ((ListBoxData)itemData).setSelectedResource(str);
    }
    public void setSelectedResource(String str, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setSelectedResource(str);
    }
    public void setSelectedResource(String str, SessionObject session) {
        ((ListBoxData)getItemData(session)).setSelectedResource(str);
    }
    
    public void setSelectedResource(CharArray str) {
        ((ListBoxData)itemData).setSelectedResource(str);
    }
    public void setSelectedResource(CharArray str, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setSelectedResource(str);
    }
    public void setSelectedResource(CharArray str, SessionObject session) {
        ((ListBoxData)getItemData(session)).setSelectedResource(str);
    }
    
    //---------------------------------------------------------------
    /** 選択状況を全て解除する*/
    public void clearSelection() {
        ((ListBoxData)itemData).clearSelection();
    }
    public void clearSelection(int sessionID) {
        ((ListBoxData)getItemData(sessionID)).clearSelection();
    }
    public void clearSelection(SessionObject session) {
        ((ListBoxData)getItemData(session)).clearSelection();
    }

    /** テーブルからセットする */
    public boolean setParameter(HashParameter param, int sessionID) {
        boolean rsts = false;
        if (key.length() > 0) {
            CharArray ch = param.get(key);
            if (ch != null) {
                //setSelectedItem(ch, sessionID);
                clearSelection(sessionID);
                CharToken token = CharToken.pop();
                token.set(ch,",");
                for (int i = 0; i < token.size(); i++) {
                    CharArray ch2 = token.get(i).trim();
                    if (ch2.length() > 0) {
                        setSelectedItem(ch2, sessionID);
                    }
                }
                CharToken.push(token);
                rsts = true;
            }
        }
        return rsts;
    }
    /** テーブルからセットする */
    public boolean setParameter(HashParameter param, SessionObject session) {
        boolean rsts = false;
        if (key.length() > 0) {
            CharArray ch = param.get(key);
            if (ch == null) {
                do {
                    int index = key.indexOf(':');   // 日付取得用 
                    
                    if (index <= 0) break;
                    if (index+1 >= key.length()) break;
                    String _key = key.substring(0, index);
                    String _data = key.substring(index+1);
                    ch = param.get(_key);
                    if (ch == null) break;
                    String str = param.getDateString(_key, _data);
                    if (str != null && str.length() > 0) {
                        ch = new CharArray(); // param バッファを壊さないため
                        ch.set(str);
                    } else {
                        ch = null;
                    }
                } while (false);
            }
            if (ch != null) {
                //setSelectedItem(ch, session);
                clearSelection(session);
                CharToken token = CharToken.pop();
                token.set(ch,",");
                for (int i = 0; i < token.size(); i++) {
                    CharArray ch2 = token.get(i).trim();
                    if (ch2.length() > 0) {
                        setSelectedItem(ch2, session);
                    }
                }
                CharToken.push(token);
                rsts = true;
            }
        }
        return rsts;
    }

    public void setSize(int size) {
        ((ListBoxData)itemData).setSize(size);
    }
    public void setSize(int size, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setSize(size);
    }
    public void setSize(int size, SessionObject session) {
        ((ListBoxData)getItemData(session)).setSize(size);
    }
    public void setMultiple(boolean mode) {
        ((ListBoxData)itemData).setMultiple(mode);
    }
    public void setMultiple(boolean mode, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setMultiple(mode);
    }
    public void setMultiple(boolean mode, SessionObject session) {
        ((ListBoxData)getItemData(session)).setMultiple(mode);
    }
    public void setDisabled(boolean mode) {
        ((ListBoxData)itemData).setDisabled(mode);
    }
    public void setDisabled(boolean mode, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).setDisabled(mode);
    }
    public void setDisabled(boolean mode, SessionObject session) {
        ((ListBoxData)getItemData(session)).setDisabled(mode);
    }
    /** メニュー情報を全てクリアする*/
    public void clear() {
        ((ListBoxData)itemData).clear();
    }
    /** メニュー情報を全てクリアする*/
    public void clear(int sessionID) {
        ((ListBoxData)getItemData(sessionID)).clear();
    }
    /** メニュー情報を全てクリアする*/
    public void clear(SessionObject session) {
        ((ListBoxData)getItemData(session)).clear();
    }
    /** メニューを追加する*/
    public void add(String menu) {add(new CharArray(menu));}
    public void add(String menu, int sessionID) {
        add(new CharArray(menu),sessionID);
    }
    public void add(String menu, SessionObject session) {
        add(new CharArray(menu),session);
    }

    /** メニューを追加する*/
    public void add(String menu, String option) {
        add(new CharArray(menu), new CharArray(option));
    }
    public void add(String menu, String option, int sessionID) {
        add(new CharArray(menu),new CharArray(option),sessionID);
    }
    public void add(String menu, String option, SessionObject session) {
        add(new CharArray(menu),new CharArray(option),session);
    }
    /** メニューを追加する*/
    public void add(String menu, String option, boolean selected) {
        add(new CharArray(menu), new CharArray(option), selected);
    }
    public void add(String menu, String option, int sessionID, boolean selected) {
        add(new CharArray(menu),new CharArray(option),selected,sessionID);
    }
    public void add(String menu, String option, SessionObject session, boolean selected) {
        add(new CharArray(menu),new CharArray(option),selected,session);
    }
    /** メニューを追加する*/
    public void add(CharArray menu, String option) {
        add(menu, new CharArray(option));
    }
    public void add(CharArray menu, String option, int sessionID) {
        add(menu,new CharArray(option),sessionID);
    }
    public void add(CharArray menu, String option, SessionObject session) {
        add(menu,new CharArray(option),session);
    }
    /** メニューを追加する*/
    public void add(CharArray ch) {
        ((ListBoxData)itemData).add(ch);
    }
    public void add(CharArray ch, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).add(ch);
    }
    public void add(CharArray ch, SessionObject session) {
        ((ListBoxData)getItemData(session)).add(ch);
    }
    //
    /** メニューとoptionを追加する*/
    public void add(CharArray menu, CharArray option) {
        ((ListBoxData)itemData).add(menu,option);
    }
    public void add(CharArray menu, CharArray option, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).add(menu, option);
    }
    public void add(CharArray menu, CharArray option, SessionObject session) {
        ((ListBoxData)getItemData(session)).add(menu, option);
    }
    /** メニューとoptionを追加する*/
    public void add(CharArray menu, CharArray option, boolean selected) {
        ((ListBoxData)itemData).add(menu,option, selected);
    }
    public void add(CharArray menu, CharArray option, boolean selected, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).add(menu, option, selected);
    }
    public void add(CharArray menu, CharArray option, boolean selected, SessionObject session) {
        ((ListBoxData)getItemData(session)).add(menu, option, selected);
    }
    //
    /** メニュー、option、resourceを追加する*/
    public void add(CharArray menu, CharArray option,CharArray resource) {
        ((ListBoxData)itemData).add(menu,option,resource);
    }
    public void add(CharArray menu, CharArray option, CharArray resource, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).add(menu, option, resource);
    }
    public void add(CharArray menu, CharArray option, CharArray resource, SessionObject session) {
        ((ListBoxData)getItemData(session)).add(menu, option, resource);
    }
    /** メニュー、option、resourceを追加する*/
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected) {
        ((ListBoxData)itemData).add(menu,option, resource, selected);
    }
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).add(menu, option, resource, selected);
    }
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected, SessionObject session) {
        ((ListBoxData)getItemData(session)).add(menu, option, resource, selected);
    }
    //
    /** メニュー、option、resourceを追加する*/
    public void add(String menu, String option,String resource) {
        ((ListBoxData)itemData).add(menu,option,resource);
    }
    public void add(String menu, String option, String resource, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).add(menu, option, resource);
    }
    public void add(String menu, String option, String resource, SessionObject session) {
        ((ListBoxData)getItemData(session)).add(menu, option, resource);
    }
    /** メニュー、option、resourceを追加する*/
    public void add(String menu, String option, String resource, boolean selected) {
        ((ListBoxData)itemData).add(menu,option, resource, selected);
    }
    public void add(String menu, String option, String resource, boolean selected, int sessionID) {
        ((ListBoxData)getItemData(sessionID)).add(menu, option, resource, selected);
    }
    public void add(String menu, String option, String resource, boolean selected, SessionObject session) {
        ((ListBoxData)getItemData(session)).add(menu, option, resource, selected);
    }
    
    //---------------------------------------------------------------------
    // データ取得メソッド
    //---------------------------------------------------------------------
    /**
        表示項目リストを返す
    */
    public CharArrayQueue getMenu() {
        return ((ListBoxData)itemData).getMenu();
    }
    
    public CharArrayQueue getMenu(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getMenu();
    }
    public CharArrayQueue getMenu(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getMenu();
    }
    /**
        option値を返す
    */
    public CharArrayQueue getOption() {
        return ((ListBoxData)itemData).getOption();
    }
    
    public CharArrayQueue getOption(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getOption();
    }
    public CharArrayQueue getOption(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getOption();
    }
    /**
        resource値を返す
    */
    public CharArrayQueue getResource() {
        return ((ListBoxData)itemData).getResource();
    }
    
    public CharArrayQueue getResource(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getResource();
    }
    public CharArrayQueue getResource(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getResource();
    }

    /**
       boolean値を返す
    */
    public BooleanQueue getQueue() {
        return ((ListBoxData)itemData).getQueue();
    }
    
    public BooleanQueue getQueue(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getQueue();
    }
    public BooleanQueue getQueue(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getQueue();
    }

    /**

    */
    public int getSize() {
        return ((ListBoxData)itemData).getSize();
    }
    public int getSize(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getSize();
    }
    public int getSize(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getSize();
    }

    /**
        複数選択可能か
    */
    public boolean isMultiple() {
        return ((ListBoxData)itemData).isMultiple();
    }
    public boolean isMultiple(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).isMultiple();
    }
    public boolean isMultiple(SessionObject session) {
        return ((ListBoxData)getItemData(session)).isMultiple();
    }

    /**
        
    */
    public boolean isDisabled() {
        return ((ListBoxData)itemData).isDisabled();
    }
    public boolean isDisabled(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).isDisabled();
    }
    public boolean isDisabled(SessionObject session) {
        return ((ListBoxData)getItemData(session)).isDisabled();
    }

    /**
        選択されているIndexの集合を返す（セットされている必要がある）
    */
    public IntQueue getSelectedIndex() {
        return ((ListBoxData)itemData).getSelectedIndex();
    }
    public IntQueue getSelectedIndex(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getSelectedIndex();
    }
    public IntQueue getSelectedIndex(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getSelectedIndex();
    }

    /**
        選択されているアイテム（メニュー文字列）の集合を返す
    */
    public CharArrayQueue getSelectedItem() {
        return ((ListBoxData)itemData).getSelectedItem();
    }
    public CharArrayQueue getSelectedItem(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getSelectedItem();
    }
    public CharArrayQueue getSelectedItem(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getSelectedItem();
    }

    /**
        選択されているアイテム（メニュー文字列）の集合を返す
    */
    public CharArrayQueue getSelectedMenu() {
        return ((ListBoxData)itemData).getSelectedMenu();
    }
    public CharArrayQueue getSelectedMenu(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getSelectedMenu();
    }
    public CharArrayQueue getSelectedMenu(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getSelectedMenu();
    }

    /**
        選択されているアイテム（オプション文字列）の集合を返す
    */
    public CharArrayQueue getSelectedOption() {
        return ((ListBoxData)itemData).getSelectedOption();
    }
    public CharArrayQueue getSelectedOption(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getSelectedOption();
    }
    public CharArrayQueue getSelectedOption(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getSelectedOption();
    }

    /**
        選択されているアイテムに対応したresourceの集合を返す
    */
    public CharArrayQueue getSelectedResource() {
        return ((ListBoxData)itemData).getSelectedResource();
    }
    public CharArrayQueue getSelectedResource(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getSelectedResource();
    }
    public CharArrayQueue getSelectedResource(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getSelectedResource();
    }
//---
    /** 指定メニューのインデックスを求める */
    public int getIndex(CharArray str) {
        return ((ListBoxData)itemData).getIndex(str);
    }
    /** 指定メニューのインデックスを求める */
    public int getIndex(CharArray str, int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getIndex(str);
    }
    public int getIndex(CharArray str, SessionObject session) {
        return ((ListBoxData)getItemData(session)).getIndex(str);
    }

    /** 指定メニューのインデックスを求める */
    public int getIndex(String str) {
        return ((ListBoxData)itemData).getIndex(str);
    }
    /** 指定メニューのインデックスを求める */
    public int getIndex(String str, int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getIndex(str);
    }
    public int getIndex(String str, SessionObject session) {
        return ((ListBoxData)getItemData(session)).getIndex(str);
    }

    /**
        指定インデックスが選択されているか？
        @param index 0-
    */
    public boolean isSelected(int index) {
        return ((ListBoxData)itemData).isSelected(index);
    }
    
    /**
        指定インデックスが選択されているか？
        @param index 0-
    */
    public boolean isSelected(int index, int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).isSelected(index);
    }
    public boolean isSelected(int index, SessionObject session) {
        return ((ListBoxData)getItemData(session)).isSelected(index);
    }

    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(String str) {
        return ((ListBoxData)itemData).isSelectedOption(str);
    }
    
    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(String str, int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).isSelectedOption(str);
    }
    public boolean isSelectedOption(String str, SessionObject session) {
        return ((ListBoxData)getItemData(session)).isSelectedOption(str);
    }
    
    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(CharArray str) {
        return ((ListBoxData)itemData).isSelectedOption(str);
    }
    
    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(CharArray str, int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).isSelectedOption(str);
    }
    public boolean isSelectedOption(CharArray str, SessionObject session) {
        return ((ListBoxData)getItemData(session)).isSelectedOption(str);
    }

    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(String str) {
        return ((ListBoxData)itemData).isSelectedResource(str);
    }
    
    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(String str, int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).isSelectedResource(str);
    }
    public boolean isSelectedResource(String str, SessionObject session) {
        return ((ListBoxData)getItemData(session)).isSelectedResource(str);
    }
    
    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(CharArray str) {
        return ((ListBoxData)itemData).isSelectedResource(str);
    }
    
    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(CharArray str, int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).isSelectedResource(str);
    }
    public boolean isSelectedResource(CharArray str, SessionObject session) {
        return ((ListBoxData)getItemData(session)).isSelectedResource(str);
    }

    //---------------------------------------------------------------------
    // レンダリング
    //---------------------------------------------------------------------
    public CharArray getMenuTag(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getMenuTag();
    }
    public CharArray getNameTag(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getNameTag();
    }
    public CharArray getNameMenuTag(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getNameMenuTag();
    }
    public CharArray getTag(int sessionID) {
        return ((ListBoxData)getItemData(sessionID)).getTag();
    }
    
    public CharArray getMenuTag(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getMenuTag();
    }
    public CharArray getNameTag(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getNameTag();
    }
    public CharArray getNameMenuTag(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getNameMenuTag();
    }
    public CharArray getTag(SessionObject session) {
        return ((ListBoxData)getItemData(session)).getTag();
    }
    
    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new ListBoxData((ListBoxData)itemData ,session);
            newData.setItem(this);
            session.getHashtable().put(new IntObject(getMixedID()),newData);
        }
    }
    

    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    public CharArray draw(SessionObject session) {
        ListBoxData data = (ListBoxData)getItemData(session);
        return data.draw(session);
    }

    // stream 版
    //public void draw(OutputStream out) {
        //未作成
    //}
    public void draw(OutputStream out, int sessionID) {
        //未作成
    }

    /**
      コンバートを行う
    */
    public void convert(SessionObject session) {
        CharArrayQueue org = getMenu();
        CharArrayQueue menu = getMenu(session.getSessionID());
        if (org.size() == menu.size()) {
            if (colorConvert || langConvert) {
                for (int i = 0; i < menu.size(); i++) {
                    CharArray ch = org.peek(i);
                    CharArray conv = null;
                    if (langConvert) conv = session.getMessage(ch.toString());
                    if (colorConvert && conv == null) conv = session.getColor(ch.toString());
                    menu.peek(i).set((conv != null) ? conv : ch);
                }
            }
        }
    }
}

//
// [end of ListBoxItem.java]
//

