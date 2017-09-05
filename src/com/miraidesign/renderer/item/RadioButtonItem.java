//------------------------------------------------------------------------
// @(#)RadioButtonItem.java
//                 <input type=radio> のデータを保管する
//                 Copyright (c) Miraidesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.IntObject;

/**
 *  ラジオボタン
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class RadioButtonItem extends Item {
    CharArray key = new CharArray();  // DBカラム等

    /** DBカラム名を取得する */
    public CharArray getKey() { return key; }

    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /** 
        ラジオボタンアイテムを生成する
    */
    public RadioButtonItem() { 
        super();
        init();
        itemData = new RadioButtonData();
        itemData.setItem(this);
    }
    
    /** 
        ラジオボタンアイテムを生成する
        @param key DBカラム名（マッピング用）
    */
    public RadioButtonItem(String key) { 
        super();
        this.key.set(key);
        init();
        itemData = new RadioButtonData();
        itemData.setItem(this);
    }
    /** 
        ラジオボタンアイテムを生成する
        @param key DBカラム名（マッピング用）
    */
    public RadioButtonItem(CharArray key) { 
        super();
        this.key.set(key);
        init();
        itemData = new RadioButtonData();
        itemData.setItem(this);
    }
    /** 
        ラジオボタンアイテムを生成する
        @param menu メニュー文字列リスト
    */
    public RadioButtonItem(CharArrayQueue menu) { 
        super();
        init();
        itemData = new RadioButtonData(menu);
        itemData.setItem(this);
    }
    /** 
        ラジオボタンアイテムを生成する
        @param menu メニュー文字列リスト
        @param option オプション文字列リスト
    */
    public RadioButtonItem(CharArrayQueue menu, CharArrayQueue option) { 
        super();
        init();
        itemData = new RadioButtonData(menu,option);
        itemData.setItem(this);
    }
    /** 
        ラジオボタンアイテムを生成する
        @param menu メニュー文字列リスト
        @param option オプション文字列リスト
        @param resource リソース文字列リスト
    */
    public RadioButtonItem(CharArrayQueue menu, CharArrayQueue option,CharArrayQueue resource) { 
        super();
        init();
        itemData = new RadioButtonData(menu,option, resource);
        itemData.setItem(this);
    }
    /** copy constructor 
        @param from コピー元のラジオボタンアイテム
    **/
    public RadioButtonItem(RadioButtonItem from) { 
        super();
        setType(RADIO);
        setCloneable(from.isCloneable());
        RadioButtonData fromdata = (RadioButtonData)from.itemData;
        itemData = new RadioButtonData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }
    
    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(RADIO);
        setCloneable(true); // 選択値が必要なので
    }
    
    //---------------------------------------------------------------------
    // データ設定メソッド
    //---------------------------------------------------------------------
    /** デフォルトの選択値 */
    public void setSelectedIndex(int index) {
        ((RadioButtonData)itemData).setSelectedIndex(index);
    }
    public void setSelectedIndex(int index, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).setSelectedIndex(index);
    }
    public void setSelectedIndex(int index, SessionObject session) {
        ((RadioButtonData)getItemData(session)).setSelectedIndex(index);
    }

    //-----------------------------------------------------------
    /** 指定されたものがOPTIONかMENUにあればselectedにする(OPTION優先） */
    public void setSelectedItem(String str) {
        ((RadioButtonData)itemData).setSelectedItem(str);
    }
    public void setSelectedItem(String str, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).setSelectedItem(str);
    }
    public void setSelectedItem(String str, SessionObject session) {
        ((RadioButtonData)getItemData(session)).setSelectedItem(str);
    }
    public void setSelectedItem(CharArray ch) {
        ((RadioButtonData)itemData).setSelectedItem(ch);
    }
    public void setSelectedItem(CharArray ch, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).setSelectedItem(ch);
    }
    public void setSelectedItem(CharArray ch, SessionObject session) {
        ((RadioButtonData)getItemData(session)).setSelectedItem(ch);
    }
    //-----------------------------------------------------------
    /** 指定されたものがMENUにあればselectedにする */
    public void setSelectedMenu(String str) {
        ((RadioButtonData)itemData).setSelectedMenu(str);
    }
    public void setSelectedMenu(String str, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).setSelectedMenu(str);
    }
    public void setSelectedMenu(String str, SessionObject session) {
        ((RadioButtonData)getItemData(session)).setSelectedMenu(str);
    }
    public void setSelectedMenu(CharArray ch) {
        ((RadioButtonData)itemData).setSelectedMenu(ch);
    }
    public void setSelectedMenu(CharArray ch, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).setSelectedMenu(ch);
    }
    public void setSelectedMenu(CharArray ch, SessionObject session) {
        ((RadioButtonData)getItemData(session)).setSelectedMenu(ch);
    }
    //-----------------------------------------------------------
    /** 指定されたものがOPTIONにあればselectedにする */
    public void setSelectedOption(String str) {
        ((RadioButtonData)itemData).setSelectedOption(str);
    }
    public void setSelectedOption(String str, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).setSelectedOption(str);
    }
    public void setSelectedOption(String str, SessionObject session) {
        ((RadioButtonData)getItemData(session)).setSelectedOption(str);
    }
    public void setSelectedOption(CharArray ch) {
        ((RadioButtonData)itemData).setSelectedOption(ch);
    }
    public void setSelectedOption(CharArray ch, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).setSelectedOption(ch);
    }
    public void setSelectedOption(CharArray ch, SessionObject session) {
        ((RadioButtonData)getItemData(session)).setSelectedOption(ch);
    }

    /** 指定されたものがresourceにあればselectedにする */
    public void setSelectedResource(String str) {
        ((RadioButtonData)itemData).setSelectedResource(str);
    }
    public void setSelectedResource(String str, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).setSelectedResource(str);
    }
    public void setSelectedResource(String str, SessionObject session) {
        ((RadioButtonData)getItemData(session)).setSelectedResource(str);
    }
    public void setSelectedResource(CharArray str) {
        ((RadioButtonData)itemData).setSelectedResource(str);
    }
    public void setSelectedResource(CharArray str, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).setSelectedResource(str);
    }
    public void setSelectedResource(CharArray str, SessionObject session) {
        ((RadioButtonData)getItemData(session)).setSelectedResource(str);
    }

    /** 選択不可にする */
    public void setDisabled(boolean mode) {
        ((RadioButtonData)itemData).setDisabled(mode);
    }
    public void setDisabled(boolean mode, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).setDisabled(mode);
    }
    public void setDisabled(boolean mode, SessionObject session) {
        ((RadioButtonData)getItemData(session)).setDisabled(mode);
    }
    
    /** Tag変換設定 */
    public void setTagConvert(boolean mode) {
        ((RadioButtonData)itemData).setTagConvert(mode);
    }
    public void setTagConvert(boolean mode, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).setTagConvert(mode);
    }
    public void setTagConvert(boolean mode, SessionObject session) {
        ((RadioButtonData)getItemData(session)).setTagConvert(mode);
    }
    
    /** データのクリア */
    public void clear() {
        ((RadioButtonData)itemData).clear();
    }
    public void clear(int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).clear();
    }
    public void clear(SessionObject session) {
        ((RadioButtonData)getItemData(session)).clear();
    }
    
    /** テーブルからセットする */
    public boolean setParameter(HashParameter param, int sessionID) {
        boolean rsts = false;
        if (key.length() > 0) {
            CharArray ch = param.get(key);
            if (ch != null) {
                setSelectedItem(ch, sessionID);
                rsts = true;
            }
        }
        return rsts;
    }
    public boolean setParameter(HashParameter param, SessionObject session) {
        boolean rsts = false;
        if (key.length() > 0) {
            CharArray ch = param.get(key);
            if (ch != null) {
                setSelectedItem(ch, session);
                rsts = true;
            }
        }
        return rsts;
    }
    
    public void add(String menu) {add(new CharArray(menu));}
    public void add(String menu,String option) {
        add(new CharArray(menu),new CharArray(option));
    }
    public void add(String menu, int sessionID) {
        add(new CharArray(menu),sessionID);
    }
    public void add(String menu, SessionObject session) {
        add(new CharArray(menu),session);
    }
    public void add(String menu, String option,int sessionID) {
        add(new CharArray(menu),new CharArray(option),sessionID);
    }
    public void add(String menu, String option,SessionObject session) {
        add(new CharArray(menu),new CharArray(option),session);
    }
    /** メニューを追加する */
    public void add(CharArray menu) {
        ((RadioButtonData)itemData).add(menu);
    }
    public void add(CharArray ch, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).add(ch);
    }
    public void add(CharArray ch, SessionObject session) {
        ((RadioButtonData)getItemData(session)).add(ch);
    }
    /** メニューとオプションを追加する */
    public void add(CharArray menu, CharArray option) {
        ((RadioButtonData)itemData).add(menu, option);
    }
    public void add(CharArray menu, CharArray option, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).add(menu, option);
    }
    public void add(CharArray menu, CharArray option, SessionObject session) {
        ((RadioButtonData)getItemData(session)).add(menu, option);
    }
    /** メニューとオプションを追加する */
    public void add(CharArray menu, CharArray option, boolean selected) {
        ((RadioButtonData)itemData).add(menu, option, selected);
    }
    public void add(CharArray menu, CharArray option, boolean selected, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).add(menu, option, selected);
    }
    public void add(CharArray menu, CharArray option, boolean selected, SessionObject session) {
        ((RadioButtonData)getItemData(session)).add(menu, option, selected);
    }
    public void add(String menu, String option, boolean selected) {
        ((RadioButtonData)itemData).add(menu, option, selected);
    }
    public void add(String menu, String option, boolean selected, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).add(menu, option, selected);
    }
    public void add(String menu, String option, boolean selected, SessionObject session) {
        ((RadioButtonData)getItemData(session)).add(menu, option, selected);
    }
    /** メニューとオプションとリソースを追加する */
    public void add(CharArray menu, CharArray option, CharArray resource) {
        ((RadioButtonData)itemData).add(menu, option, resource);
    }
    public void add(CharArray menu, CharArray option, CharArray resource, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).add(menu, option, resource);
    }
    public void add(CharArray menu, CharArray option, CharArray resource, SessionObject session) {
        ((RadioButtonData)getItemData(session)).add(menu, option, resource);
    }
    public void add(String menu, String option, String resource) {
        ((RadioButtonData)itemData).add(menu, option, resource);
    }
    public void add(String menu, String option, String resource, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).add(menu, option, resource);
    }
    public void add(String menu, String option, String resource, SessionObject session) {
        ((RadioButtonData)getItemData(session)).add(menu, option, resource);
    }
    /** メニューとオプションとリソースを追加する */
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected) {
        ((RadioButtonData)itemData).add(menu, option, resource, selected);
    }
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).add(menu, option, resource, selected);
    }
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected, SessionObject session) {
        ((RadioButtonData)getItemData(session)).add(menu, option, resource, selected);
    }
    public void add(String menu, String option, String resource, boolean selected) {
        ((RadioButtonData)itemData).add(menu, option, resource, selected);
    }
    public void add(String menu, String option, String resource, boolean selected, int sessionID) {
        ((RadioButtonData)getItemData(sessionID)).add(menu, option, resource, selected);
    }
    public void add(String menu, String option, String resource, boolean selected, SessionObject session) {
        ((RadioButtonData)getItemData(session)).add(menu, option, resource, selected);
    }
    
    //---------------------------------------------------------------------
    // データ取得メソッド
    //---------------------------------------------------------------------
    /** メニュー情報を取得する*/
    public CharArrayQueue getMenu() {
        return ((RadioButtonData)itemData).getMenu();
    }
    public CharArrayQueue getMenu(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getMenu();
    }
    public CharArrayQueue getMenu(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getMenu();
    }
    /** option 情報を取得する */
    public CharArrayQueue getOption() {
        return ((RadioButtonData)itemData).getOption();
    }
    public CharArrayQueue getOption(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getOption();
    }
    public CharArrayQueue getOption(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getOption();
    }
    /** resource 情報を取得する */
    public CharArrayQueue getResource() {
        return ((RadioButtonData)itemData).getResource();
    }
    public CharArrayQueue getResource(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getResource();
    }
    public CharArrayQueue getResource(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getResource();
    }
    /** 選択情報を取得する */
    //public BooleanQueue getQueue() {
    //    return ((RadioButtonData)itemData).getQueue();
    //}
    //public BooleanQueue getQueue(int sessionID) {
    //    return ((RadioButtonData)getItemData(sessionID)).getQueue();
    //}
    
    /**
        選択されているIndexを返す（セットされている必要がある）
    */
    public int getSelectedIndex() {
        return ((RadioButtonData)itemData).getSelectedIndex();
    }
    public int getSelectedIndex(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getSelectedIndex();
    }
    public int getSelectedIndex(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getSelectedIndex();
    }

    /**
        選択されているアイテム（メニュー文字列）を返す
    */
    public CharArray getSelectedItem() {
        return ((RadioButtonData)itemData).getSelectedItem();
    }
    public CharArray getSelectedItem(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getSelectedItem();
    }
    public CharArray getSelectedItem(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getSelectedItem();
    }
    /**
        選択されているアイテム（メニュー文字列）を返す
    */
    public CharArray getSelectedMenu() {
        return ((RadioButtonData)itemData).getSelectedMenu();
    }
    public CharArray getSelectedMenu(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getSelectedMenu();
    }
    public CharArray getSelectedMenu(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getSelectedMenu();
    }
    
    /**
        選択されているオプション（value）を返す
    */
    public CharArray getSelectedOption() {
        return ((RadioButtonData)itemData).getSelectedOption();
    }
    public CharArray getSelectedOption(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getSelectedOption();
    }
    public CharArray getSelectedOption(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getSelectedOption();
    }
    
    /**
        選択されているresource値を返す
    */
    public CharArray getSelectedResource() {
        return ((RadioButtonData)itemData).getSelectedResource();
    }
    public CharArray getSelectedResource(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getSelectedResource();
    }
    public CharArray getSelectedResource(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getSelectedResource();
    }
    
    /** 選択不可か？ */
    public boolean isDisabled() {
        return ((RadioButtonData)itemData).isDisabled();
    }
    public boolean isDisabled(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).isDisabled();
    }
    public boolean isDisabled(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).isDisabled();
    }
    
    /**
        選択されているアイテム（メニュー文字列）リストを返す
        @deprecated
    */
    public CharArray getValue() {
        return ((RadioButtonData)itemData).getSelectedItem();
    }
    
    /**
        選択されているアイテム（メニュー文字列）リストを返す
            @param sessionID    セッションＩＤ
        @deprecated
    */
    public CharArray getValue(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getSelectedItem();
    }
    public CharArray getValue(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getSelectedItem();
    }

    /** 指定メニューのインデックスを求める */
    public int getIndex(CharArray str) {
        return ((RadioButtonData)itemData).getIndex(str);
    }
    /** 指定メニューのインデックスを求める */
    public int getIndex(CharArray str, int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getIndex(str);
    }
    public int getIndex(CharArray str, SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getIndex(str);
    }

    /** 指定メニューのインデックスを求める */
    public int getIndex(String str) {
        return ((RadioButtonData)itemData).getIndex(str);
    }
    /** 指定メニューのインデックスを求める */
    public int getIndex(String str, int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getIndex(str);
    }
    public int getIndex(String str, SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getIndex(str);
    }
    
    /**
        指定インデックスが選択されているか？
        @param index 0-
    */
    public boolean isSelected(int index) {
        return ((RadioButtonData)itemData).isSelected(index);
    }
    
    /**
        指定インデックスが選択されているか？
        @param index 0-
    */
    public boolean isSelected(int index, int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).isSelected(index);
    }
    public boolean isSelected(int index, SessionObject session) {
        return ((RadioButtonData)getItemData(session)).isSelected(index);
    }

    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(String str) {
        return ((RadioButtonData)itemData).isSelectedOption(str);
    }
    
    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(String str, int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).isSelectedOption(str);
    }
    public boolean isSelectedOption(String str, SessionObject session) {
        return ((RadioButtonData)getItemData(session)).isSelectedOption(str);
    }
    
    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(CharArray str) {
        return ((RadioButtonData)itemData).isSelectedOption(str);
    }
    
    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(CharArray str, int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).isSelectedOption(str);
    }
    public boolean isSelectedOption(CharArray str, SessionObject session) {
        return ((RadioButtonData)getItemData(session)).isSelectedOption(str);
    }

    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(String str) {
        return ((RadioButtonData)itemData).isSelectedResource(str);
    }
    
    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(String str, int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).isSelectedResource(str);
    }
    public boolean isSelectedResource(String str, SessionObject session) {
        return ((RadioButtonData)getItemData(session)).isSelectedResource(str);
    }
    
    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(CharArray str) {
        return ((RadioButtonData)itemData).isSelectedResource(str);
    }
    
    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(CharArray str, int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).isSelectedResource(str);
    }
    public boolean isSelectedResource(CharArray str, SessionObject session) {
        return ((RadioButtonData)getItemData(session)).isSelectedResource(str);
    }
    
    /** タグ変換情報を取得する */
    public boolean getTagConvert() {
        return ((RadioButtonData)itemData).getTagConvert();
    }
    public boolean getTagConvert(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getTagConvert();
    }
    public boolean getTagConvert(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getTagConvert();
    }
    
    //---------------------------------------------------------------------
    // レンダリング
    //---------------------------------------------------------------------
    public CharArray getTag(int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getTag();
    }
    public CharArray getTag(int index, int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getTag(index);
    }
    public CharArray getNameTag(int index, int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getNameTag(index);
    }
    public CharArray getMenuTag(int index, int sessionID) {
        return ((RadioButtonData)getItemData(sessionID)).getMenuTag(index);
    }

    public CharArray getTag(SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getTag();
    }
    public CharArray getTag(int index, SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getTag(index);
    }
    public CharArray getNameTag(int index, SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getNameTag(index);
    }
    public CharArray getMenuTag(int index, SessionObject session) {
        return ((RadioButtonData)getItemData(session)).getMenuTag(index);
    }

    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new RadioButtonData((RadioButtonData)itemData ,session);
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
        RadioButtonData data = (RadioButtonData)getItemData(session);
        return data.draw(session);
    }

    // stream 版
    public void draw(OutputStream out) {
        //未作成
    }
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
// [end of RadioButtonItem.java]
//

