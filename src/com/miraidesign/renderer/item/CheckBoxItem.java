//------------------------------------------------------------------------
// @(#)CheckBoxItem.java
//         <input type=checkbox> のデータを保管する
//             Copyright (c) Miraidesign 2010  All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.BooleanQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.IntQueue;
import com.miraidesign.session.SessionObject;

/**
 *  チェックボックスアイテム
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class CheckBoxItem extends Item {

    CharArray key = new CharArray();  // DBカラム等

    /** DBカラムを取得する */
    public CharArray getKey() { return key; }

    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /** CheckBoxItemを生成します */
    public CheckBoxItem() { 
        super();
        init();
        itemData = new CheckBoxData();
        itemData.setItem(this);
    }

    /** CheckBoxItemを生成します
        @param key DBカラム名（マッピング用）
    */
    public CheckBoxItem(String key) { 
        super();
        this.key.set(key);
        init();
        itemData = new CheckBoxData();
        itemData.setItem(this);
    }
    /** CheckBoxItemを生成します
        @param key DBカラム名（マッピング用）
    */
    public CheckBoxItem(CharArray key) { 
        super();
        this.key.set(key);
        init();
        itemData = new CheckBoxData();
        itemData.setItem(this);
    }
    
    /** CheckBoxItemを生成します
        @param menu 表示文字列の配列
    */
    public CheckBoxItem(CharArrayQueue menu) { 
        super();
        init();
        itemData = new CheckBoxData(menu);
        itemData.setItem(this);
    }
    /** CheckBoxItemを生成します
        @param menu 表示文字列の配列
        @param option value文字列の配列(英数字を推奨)
    */
    public CheckBoxItem(CharArrayQueue menu, CharArrayQueue option) { 
        super();
        init();
        itemData = new CheckBoxData(menu,option);
        itemData.setItem(this);
    }
    /** copy constructor 
        @param from コピー元のラジオボタンアイテム
    **/
    public CheckBoxItem(CheckBoxItem from) { 
        super();
        setType(CHECKBOX);
        setCloneable(from.isCloneable());
        CheckBoxData fromdata = (CheckBoxData)from.itemData;
        itemData = new CheckBoxData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }
    
    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(CHECKBOX);
        setCloneable(true); // 選択値が必要なので
    }
    
    //---------------------------------------------------------------------
    // データ設定メソッド
    //---------------------------------------------------------------------
    /** デフォルトの選択値 */
    public void setSelectedIndex(int index,boolean b) {
        ((CheckBoxData)itemData).setSelectedIndex(index,b);
    }
    public void setSelectedIndex(int index, boolean b, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setSelectedIndex(index,b);
    }
    public void setSelectedIndex(int index, boolean b, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setSelectedIndex(index,b);
    }
    
    //-----------------------------------------------------------
    /** 指定されたものが、optionかメニューにあれば selectedにする */
    public void setSelectedItem(String str) {
        ((CheckBoxData)itemData).setSelectedItem(str);
    }
    public void setSelectedItem(String str, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setSelectedItem(str);
    }
    public void setSelectedItem(String str, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setSelectedItem(str);
    }
    public void setSelectedItem(CharArray ch) {
        ((CheckBoxData)itemData).setSelectedItem(ch);
    }
    public void setSelectedItem(CharArray ch, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setSelectedItem(ch);
    }
    public void setSelectedItem(CharArray ch, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setSelectedItem(ch);
    }
    
    //-----------------------------------------------------------
    /** 指定されたものがMenuにあればselectedにする */
    public void setSelectedMenu(String str) {
        ((CheckBoxData)itemData).setSelectedMenu(str);
    }
    public void setSelectedMenu(String str, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setSelectedMenu(str);
    }
    public void setSelectedMenu(String str, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setSelectedMenu(str);
    }
    public void setSelectedMenu(CharArray ch) {
        ((CheckBoxData)itemData).setSelectedMenu(ch);
    }
    public void setSelectedMenu(CharArray ch, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setSelectedMenu(ch);
    }
    public void setSelectedMenu(CharArray ch, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setSelectedMenu(ch);
    }
    //-----------------------------------------------------------
    /** 指定されたものがOPTIONにあればselectedにする */
    public void setSelectedOption(String str) {
        ((CheckBoxData)itemData).setSelectedOption(str);
    }
    public void setSelectedOption(String str, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setSelectedOption(str);
    }
    public void setSelectedOption(String str, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setSelectedOption(str);
    }
    public void setSelectedOption(CharArray ch) {
        ((CheckBoxData)itemData).setSelectedOption(ch);
    }
    public void setSelectedOption(CharArray ch, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setSelectedOption(ch);
    }
    public void setSelectedOption(CharArray ch, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setSelectedOption(ch);
    }
    /** 指定された文字列が、resourceと一致すれば selectedにする */
    public void setSelectedResource(String str) {
        ((CheckBoxData)itemData).setSelectedResource(str);
    }
    public void setSelectedResource(String str, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setSelectedResource(str);
    }
    public void setSelectedResource(String str, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setSelectedResource(str);
    }
    public void setSelectedResource(CharArray str) {
        ((CheckBoxData)itemData).setSelectedResource(str);
    }
    public void setSelectedResource(CharArray str, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setSelectedResource(str);
    }
    public void setSelectedResource(CharArray str, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setSelectedResource(str);
    }

    /** 選択不可にする */
    public void setDisabled(boolean mode) {
        ((CheckBoxData)itemData).setDisabled(mode);
    }
    public void setDisabled(boolean mode, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setDisabled(mode);
    }
    public void setDisabled(boolean mode, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setDisabled(mode);
    }
    
    /** 選択を全てクリアする */
    public void clearSelection() {
        ((CheckBoxData)itemData).clearSelection();
    }
    public void clearSelection(int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).clearSelection();
    }
    public void clearSelection(SessionObject session) {
        ((CheckBoxData)getItemData(session)).clearSelection();
    }
    /** 全てセットする */
    public void allSelection() {
        ((CheckBoxData)itemData).allSelection();
    }
    public void allSelection(int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).allSelection();
    }
    public void allSelection(SessionObject session) {
        ((CheckBoxData)getItemData(session)).allSelection();
    }

    //----------------------------------------------------
    public void setMenu(CharArrayQueue menu) {
        ((CheckBoxData)itemData).setMenu(menu);
    }
    public void setMenu(CharArrayQueue menu, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setMenu(menu);
    }
    public void setMenu(CharArrayQueue menu, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setMenu(menu);
    }
    //----------------------------------------------------
    public void setOption(CharArrayQueue option) {
        ((CheckBoxData)itemData).setOption(option);
    }
    public void setOption(CharArrayQueue option, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setOption(option);
    }
    public void setOption(CharArrayQueue option, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setOption(option);
    }
    //----------------------------------------------------
    public void setQueue(BooleanQueue queue) {
        ((CheckBoxData)itemData).setQueue(queue);
    }
    public void setQueue(BooleanQueue queue, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setQueue(queue);
    }
    public void setQueue(BooleanQueue queue, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setQueue(queue);
    }
    //---------------------------------------------------------------------
    /** Tag変換設定 */
    public void setTagConvert(boolean mode) {
        ((CheckBoxData)itemData).setTagConvert(mode);
    }
    public void setTagConvert(boolean mode, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).setTagConvert(mode);
    }
    public void setTagConvert(boolean mode, SessionObject session) {
        ((CheckBoxData)getItemData(session)).setTagConvert(mode);
    }

    /** @deprecated */
    public void clear() {
        ((CheckBoxData)itemData).clear();
    }
    /** チェックボックスの表示文字列をすべてクリアします。
        @param sessionID セッションＩＤ
    */
    public void clear(int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).clear();
    }
    public void clear(SessionObject session) {
        ((CheckBoxData)getItemData(session)).clear();
    }
    
    /** テーブルからセットする */
    public boolean setParameter(HashParameter param, int sessionID) {
        boolean rsts = false;
        if (key.length() > 0) {
            CharArray ch = param.get(key);
            if (ch != null) {
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
    public boolean setParameter(HashParameter param, SessionObject session) {
        boolean rsts = false;
        if (key.length() > 0) {
            CharArray ch = param.get(key);
            if (ch != null) {
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
    
    
    /** メニューを追加する */
    public void add(String menu) {add(new CharArray(menu));}
    public void add(String menu, int sessionID) {
        add(new CharArray(menu),sessionID);
    }
    public void add(String menu, SessionObject session) {
        add(new CharArray(menu),session);
    }

    /** メニューとオプションを追加する */
    public void add(String menu, String option) {
        add(new CharArray(menu), new CharArray(option));
    }
    public void add(String menu, String option, int sessionID) {
        add(new CharArray(menu),new CharArray(option),sessionID);
    }
    public void add(String menu, String option, SessionObject session) {
        add(new CharArray(menu),new CharArray(option),session);
    }
    public void add(String menu, String option, boolean selected) {
        add(new CharArray(menu), new CharArray(option), selected);
    }
    public void add(String menu, String option, int sessionID, boolean selected) {
        add(new CharArray(menu),new CharArray(option),selected,sessionID);
    }
    public void add(String menu, String option, SessionObject session, boolean selected) {
        add(new CharArray(menu),new CharArray(option),selected,session);
    }
    /** メニューとオプションとリソースを追加する */
    public void add(String menu, String option, String resource, boolean selected) {
        add(new CharArray(menu), new CharArray(option), new CharArray(resource), selected);
    }
    public void add(String menu, String option, String resource, int sessionID, boolean selected) {
        add(new CharArray(menu),new CharArray(option),new CharArray(resource),selected,sessionID);
    }
    public void add(String menu, String option, String resource, SessionObject session, boolean selected) {
        add(new CharArray(menu),new CharArray(option),new CharArray(resource),selected,session);
    }
    
    /** メニューを追加する */
    public void add(CharArray ch) {
        ((CheckBoxData)itemData).add(ch);
    }
    public void add(CharArray ch, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).add(ch);
    }
    public void add(CharArray ch, SessionObject session) {
        ((CheckBoxData)getItemData(session)).add(ch);
    }

    /** メニューとオプションを追加する */
    public void add(CharArray menu, CharArray option) {
        ((CheckBoxData)itemData).add(menu,option);
    }
    public void add(CharArray menu, CharArray option, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).add(menu, option);
    }
    public void add(CharArray menu, CharArray option, SessionObject session) {
        ((CheckBoxData)getItemData(session)).add(menu, option);
    }
    public void add(CharArray menu, CharArray option, boolean selected) {
        ((CheckBoxData)itemData).add(menu,option, selected);
    }
    public void add(CharArray menu, CharArray option, boolean selected, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).add(menu, option, selected);
    }
    public void add(CharArray menu, CharArray option, boolean selected, SessionObject session) {
        ((CheckBoxData)getItemData(session)).add(menu, option, selected);
    }
    /** メニューとオプションとリソースを追加する */
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected) {
        ((CheckBoxData)itemData).add(menu,option, resource,selected);
    }
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected, int sessionID) {
        ((CheckBoxData)getItemData(sessionID)).add(menu, option, resource,selected);
    }
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected, SessionObject session) {
        ((CheckBoxData)getItemData(session)).add(menu, option, resource,selected);
    }
    
    //---------------------------------------------------------------------
    // データ取得メソッド
    //---------------------------------------------------------------------
    /** メニュー情報を取得する*/
    public CharArrayQueue getMenu() {
        return ((CheckBoxData)itemData).getMenu();
    }
    public CharArrayQueue getMenu(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getMenu();
    }
    public CharArrayQueue getMenu(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getMenu();
    }
    /** option 情報を取得する */
    public CharArrayQueue getOption() {
        return ((CheckBoxData)itemData).getOption();
    }
    public CharArrayQueue getOption(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getOption();
    }
    public CharArrayQueue getOption(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getOption();
    }
    /** resoruce 情報を取得する */
    public CharArrayQueue getResource() {
        return ((CheckBoxData)itemData).getResource();
    }
    public CharArrayQueue getResource(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getResource();
    }
    public CharArrayQueue getResource(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getResource();
    }
    /** 選択情報を取得する */
    public BooleanQueue getQueue() {
        return ((CheckBoxData)itemData).getQueue();
    }
    public BooleanQueue getQueue(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getQueue();
    }
    public BooleanQueue getQueue(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getQueue();
    }
    /** 選択された情報をBit情報で返す */
    public int getSelectedBits() {
        return ((CheckBoxData)itemData).getSelectedBits();
    }
    public int getSelectedBits(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getSelectedBits();
    }
    public int getSelectedBits(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getSelectedBits();
    }
    
    /**
        選択されているIndexのqueueを返す
    */
    public IntQueue getSelectedIndex() {
        return ((CheckBoxData)itemData).getSelectedIndex();
    }
    public IntQueue getSelectedIndex(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getSelectedIndex();
    }
    public IntQueue getSelectedIndex(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getSelectedIndex();
    }

    /**
        選択されているアイテム（メニュー文字列）リストを返す
    */
    public CharArrayQueue getSelectedItem() {
        return ((CheckBoxData)itemData).getSelectedItem();
    }
    
    /**
        選択されているアイテム（メニュー文字列）リストを返す
            @param sessionID    セッションＩＤ
    */
    public CharArrayQueue getSelectedItem(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getSelectedItem();
    }
    public CharArrayQueue getSelectedItem(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getSelectedItem();
    }
    
    /**
        選択されているオプション（value）リストを返す
    */
    public CharArrayQueue getSelectedOption() {
        return ((CheckBoxData)itemData).getSelectedOption();
    }
    
    /**
        選択されているオプション（value）リストを返す
            @param sessionID    セッションＩＤ
    */
    public CharArrayQueue getSelectedOption(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getSelectedOption();
    }
    public CharArrayQueue getSelectedOption(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getSelectedOption();
    }
    
    /** 選択されたresource値の集合を返す */
    public CharArrayQueue getSelectedResource() {
        return ((CheckBoxData)itemData).getSelectedResource();
    }
    /** 選択されたresource値の集合を返す 
            @param sessionID    セッションＩＤ
    */
    public CharArrayQueue getSelectedResource(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getSelectedResource();
    }
    public CharArrayQueue getSelectedResource(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getSelectedResource();
    }

    //------------------------------------------------------------
    /**
        選択されているアイテム（メニュー文字列）リストをカンマ区切りで返す
    */
    public CharArray getSelectedItemList() {
        return ((CheckBoxData)itemData).getSelectedItemList();
    }
    
    /**
        選択されているアイテム（メニュー文字列）リストをカンマ区切りで返す
            @param sessionID    セッションＩＤ
    */
    public CharArray getSelectedItemList(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getSelectedItemList();
    }
    public CharArray getSelectedItemList(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getSelectedItemList();
    }
    
    /**
        選択されているオプション（value）リストをカンマ区切りで返す
    */
    public CharArray getSelectedOptionList() {
        return ((CheckBoxData)itemData).getSelectedOptionList();
    }
    
    /**
        選択されているオプション（value）リストをカンマ区切りで返す
            @param sessionID    セッションＩＤ
    */
    public CharArray getSelectedOptionList(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getSelectedOptionList();
    }
    public CharArray getSelectedOptionList(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getSelectedOptionList();
    }
    
    /** 選択されたresource値の集合をカンマ区切りで返す */
    public CharArray getSelectedResourceList() {
        return ((CheckBoxData)itemData).getSelectedResourceList();
    }
    /** 選択されたresource値の集合を返す 
            @param sessionID    セッションＩＤ
    */
    public CharArray getSelectedResourceList(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getSelectedResourceList();
    }
    public CharArray getSelectedResourceList(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getSelectedResourceList();
    }

    //--------------

    
    /** 選択不可か？ */
    public boolean isDisabled() {
        return ((CheckBoxData)itemData).isDisabled();
    }
    public boolean isDisabled(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).isDisabled();
    }
    public boolean isDisabled(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).isDisabled();
    }
    
    /**
        選択されているアイテム（メニュー文字列）リストを返す
        @deprecated
    */
    public CharArrayQueue getValue() {
        return ((CheckBoxData)itemData).getSelectedItem();
    }
    
    /**
        選択されているアイテム（メニュー文字列）リストを返す
            @param sessionID    セッションＩＤ
        @deprecated
    */
    public CharArrayQueue getValue(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getSelectedItem();
    }
    public CharArrayQueue getValue(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getSelectedItem();
    }

    /** 指定メニューのインデックスを求める */
    public int getIndex(CharArray str) {
        return ((CheckBoxData)itemData).getIndex(str);
    }
    /** 指定メニューのインデックスを求める */
    public int getIndex(CharArray str, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getIndex(str);
    }
    public int getIndex(CharArray str, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getIndex(str);
    }

    /** 指定メニューのインデックスを求める */
    public int getIndex(String str) {
        return ((CheckBoxData)itemData).getIndex(str);
    }
    /** 指定メニューのインデックスを求める */
    public int getIndex(String str, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getIndex(str);
    }
    public int getIndex(String str, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getIndex(str);
    }

    /**
        指定インデックスが選択されているか？
        @param index 0-
    */
    public boolean isSelected(int index) {
        return ((CheckBoxData)itemData).isSelected(index);
    }
    
    /**
        指定インデックスが選択されているか？
        @param index 0-
    */
    public boolean isSelected(int index, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).isSelected(index);
    }
    public boolean isSelected(int index, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).isSelected(index);
    }

    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(String str) {
        return ((CheckBoxData)itemData).isSelectedOption(str);
    }
    
    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(String str, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).isSelectedOption(str);
    }
    public boolean isSelectedOption(String str, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).isSelectedOption(str);
    }
    
    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(CharArray str) {
        return ((CheckBoxData)itemData).isSelectedOption(str);
    }
    
    /** 指定オプションが選択されているか？ */
    public boolean isSelectedOption(CharArray str, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).isSelectedOption(str);
    }
    public boolean isSelectedOption(CharArray str, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).isSelectedOption(str);
    }

    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(String str) {
        return ((CheckBoxData)itemData).isSelectedResource(str);
    }
    
    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(String str, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).isSelectedResource(str);
    }
    public boolean isSelectedResource(String str, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).isSelectedResource(str);
    }
    
    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(CharArray str) {
        return ((CheckBoxData)itemData).isSelectedResource(str);
    }
    
    /** 指定リソースが選択されているか？ */
    public boolean isSelectedResource(CharArray str, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).isSelectedResource(str);
    }
    public boolean isSelectedResource(CharArray str, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).isSelectedResource(str);
    }

    /** タグ変換情報を取得する */
    public boolean getTagConvert() {
        return ((CheckBoxData)itemData).getTagConvert();
    }
    public boolean getTagConvert(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getTagConvert();
    }
    public boolean getTagConvert(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getTagConvert();
    }

    //---------------------------------------------------------------------
    // レンダリング
    //---------------------------------------------------------------------
    public CharArray getTag(int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getTag();
    }
    public CharArray getTag(int index, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getTag(index);
    }
    public CharArray getNameTag(int index, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getNameTag(index);
    }
    public CharArray getMenuTag(int index, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getMenuTag(index);
    }
    public CharArray getHiddenTag(int index, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getHiddenTag(index);
    }
    public CharArray getNameHiddenTag(int index, int sessionID) {
        return ((CheckBoxData)getItemData(sessionID)).getNameHiddenTag(index);
    }

    public CharArray getTag(SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getTag();
    }
    public CharArray getTag(int index, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getTag(index);
    }
    public CharArray getNameTag(int index, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getNameTag(index);
    }
    public CharArray getMenuTag(int index, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getMenuTag(index);
    }
    public CharArray getHiddenTag(int index, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getHiddenTag(index);
    }
    public CharArray getNameHiddenTag(int index, SessionObject session) {
        return ((CheckBoxData)getItemData(session)).getNameHiddenTag(index);
    }

    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new CheckBoxData((CheckBoxData)itemData ,session);
            newData.setItem(this);
            session.getHashtable().put(new IntObject(getMixedID()), newData);
        }
    }
    

    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** CheckBoxItemのレンダリングを行います
        @param session セッション
        @return 描画バッファ
    */
    public CharArray draw(SessionObject session) {
        CheckBoxData data = (CheckBoxData)getItemData(session);
        return data.draw(session);
    }
    /** not ready pdeprecated */
    //public void draw(OutputStream out) {
        //未作成
    //}
    /** not ready */
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
// [end of CheckBoxItem.java]
//

