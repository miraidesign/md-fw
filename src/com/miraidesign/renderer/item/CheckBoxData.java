//------------------------------------------------------------------------
// @(#)CheckBoxData.java
//         <input type=checkbox> のデータを保管する
//             Copyright (c) MIraiDesign 2010  All Rights Reserved. 
//------------------------------------------------------------------------
//
package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

//import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.BooleanQueue;
import com.miraidesign.util.IntQueue;

/** チェックボックスのデータを保管する */
public class CheckBoxData extends ItemData implements ItemDataSelectable {
    private final static boolean debug = false;
    private CharArrayQueue menu   = new CharArrayQueue();   // メニューリスト
    private CharArrayQueue option = new CharArrayQueue();   // 選択Value（ない時は↑と同じ）
    private CharArrayQueue resource = new CharArrayQueue();   // ItemInfo リソース

    private BooleanQueue   queue = new BooleanQueue(); // 選択フラグ
    private BooleanQueue   queueOrg = new BooleanQueue(); // 保存用 v0.684
    private BooleanQueue   queueLast = new BooleanQueue(); // 直前の値 v0.684
    
    private boolean disabled  = false;    // 
    private boolean tagConvert = true;   // タグ変換を行うか？
    {
        type = CHECKBOX;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public CheckBoxData() { }

    public CheckBoxData(CheckBoxData from,SessionObject session) {
        this.sessionObject = session;
        copy(from); 
    }
    public CheckBoxData(CharArrayQueue menu) {
        setMenu(menu);
        setOption(menu);
        for (int i = 0; i < menu.size(); i++) {
            queue.enqueue(false);
            queueOrg.enqueue(false);
            resource.enqueue("");
        }
    }
    public CheckBoxData(CharArrayQueue menu, CharArrayQueue option) {
        setMenu(menu);
        setOption(option);
        for (int i = 0; i < menu.size(); i++) {
            queue.enqueue(false);
            queueOrg.enqueue(false);
            resource.enqueue("");
        }
    }
    public CheckBoxData(CharArrayQueue menu,BooleanQueue queue) {
        setMenu(menu);
        setOption(menu);
        setQueue(queue);
        for (int i = 0; i < queue.size(); i++) {
            resource.enqueue("");
        }
    }
    public CheckBoxData(CharArrayQueue menu, CharArrayQueue option, BooleanQueue queue) {
        setMenu(menu);
        setOption(option);
        setQueue(queue);
        for (int i = 0; i < queue.size(); i++) {
            resource.enqueue("");
        }
    }
    
    //---------------------------------------------------------------------
    //
    //---------------------------------------------------------------------
    /** 全て削除する */
    public void clear() { 
        menu.clear(); 
        option.clear(); 
        resource.clear();
        queue.clear(); 
        if (sessionObject == null || sessionObject.updateMode) queueOrg.clear(); 
    }
    /** 表示文字列を追加する */
    public void add(CharArray ch) { 
        menu.enqueue(ch);
        if (ch.indexOf("\n") >= 0) {
            CharArray ch2 = new CharArray(ch);
            option.enqueue(ch2.replace("\n",""));
        } else {
            option.enqueue(ch);
        }
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    /** 表示文字列を追加する */
    public void add(String str) { 
        menu.enqueue(str);
        if (str.indexOf("\n") >= 0) {
            CharArray ch2 = new CharArray(str);
            option.enqueue(ch2.replace("\n",""));
        } else {
            option.enqueue(str);
        }
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    /** 表示・オプション文字列を追加する */
    public void add(CharArray menu, CharArray option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    /** 表示・オプション文字列を追加する */
    public void add(String menu, String option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    /** 表示・オプション文字列を追加する */
    public void add(String menu, CharArray option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    /** 表示・オプション文字列を追加する */
    public void add(CharArray menu, String option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    /** 表示・オプション文字列とデフォルト選択値を追加する */
    public void add(CharArray menu, CharArray option, boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(selected);
        queueOrg.enqueue(selected);
    }
    /** 表示・オプション文字列とデフォルト選択値を追加する */
    public void add(String menu, String option, boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(selected);
        queueOrg.enqueue(selected);
    }
    
    ///////////////////////////////////
    /** 表示・オプション・リソース文字列とデフォルト選択値を追加する */
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        this.resource.enqueue(resource);
        queue.enqueue(selected);
        queueOrg.enqueue(selected);
    }
    /** 表示・オプション・リソース文字列とデフォルト選択値を追加する */
    public void add(String menu, String option, String resource, boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        this.resource.enqueue(resource);
        queue.enqueue(selected);
        queueOrg.enqueue(selected);
    }
    
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setMenu(CharArrayQueue menu) {
        this.menu = menu; 
        while (menu.size() > queue.size()) queue.enqueue(false);
        while (menu.size() > queueOrg.size()) queueOrg.enqueue(false);
    }
    public void setOption(CharArrayQueue option) {
        this.option = option; 
        while (option.size() > queue.size()) queue.enqueue(false);
        while (option.size() > queueOrg.size()) queueOrg.enqueue(false);
    }
    public void setQueue(BooleanQueue queue) {
        this.queue = queue; 
        while (menu.size() > queue.size()) queue.enqueue(false);
        while (option.size() > queue.size()) queue.enqueue(false);
        queueOrg.copy(queue);
    }
    
    /** 指定indexをselected にする*/
    public void setSelectedIndex(int index) { 
if (debug) System.out.println("setSelectedIndex("+index+")");
        queue.poke(index,true);
        if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(index,true);
    }
    /** 指定indexのOnOff*/
    public void setSelectedIndex(int index, boolean b) { 
if (debug) System.out.println("setSelectedIndex("+index+","+b+")");
        queue.poke(index,b);
        if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(index,b);
    }
    /** 指定されたものが、resource, optionかメニューにあれば selectedにする */
    public void setSelectedItem(String str) {
if (debug) System.out.println("setSelectedItem("+str+")");
        for (int i = 0; i < resource.size(); i++) {
            CharArray ch = resource.peek(i);
            if (ch.equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
if (debug) System.out.println("  resource.poke "+i);
                return;
            }
        }
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
if (debug) System.out.println("  option.poke "+i);
                return;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
if (debug) System.out.println("  menu.poke "+i);
                return;
            }
        }
    }
    /** 指定されたものが、resource, optionかメニューにあれば selectedにする */
    public void setSelectedItem(CharArray str) {
if (debug) System.out.println("setSelectedItem("+str+")");
        for (int i = 0; i < resource.size(); i++) {
            CharArray ch = resource.peek(i);
            if (ch.equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
if (debug) System.out.println("  resource.poke "+i);
                return;
            }
        }
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
if (debug) System.out.println("  option.poke "+i);
                return;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
if (debug) System.out.println("  menu.poke "+i);
                return;
            }
        }
    }
    /** 指定されたものが、メニューにあれば selectedにする */
    public void setSelectedMenu(CharArray ch) { 
        for (int i = 0; i < menu.size(); i++) {
            if (menu.peek(i).equals(ch)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                break;
            }
        }
    }
    /** 指定されたものが、メニューにあれば selectedにする */
    public void setSelectedMenu(String str) { 
        for (int i = 0; i < menu.size(); i++) {
            if (menu.peek(i).equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                break;
            }
        }
    }
    
    /** 指定されたものが、optionにあれば selectedにする */
    public void setSelectedOption(CharArray ch) { 
        for (int i = 0; i < option.size(); i++) {
            if (option.peek(i).equals(ch)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                break;
            }
        }
    }
    /** 指定されたものが、optionにあれば selectedにする */
    public void setSelectedOption(String ch) { 
        for (int i = 0; i < option.size(); i++) {
            if (option.peek(i).equals(ch)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                break;
            }
        }
    }

    /** 指定された文字列が、resouceと一致すれば selectedにする */
    public void setSelectedResource(CharArray ch) {
        for (int i = 0; i < resource.size(); i++) {
            if (resource.peek(i).equals(ch)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                return;
            }
        }
    }
    
    /** 指定された文字列が、resouceと一致すれば selectedにする */
    public void setSelectedResource(String str) {
        for (int i = 0; i < resource.size(); i++) {
            if (resource.peek(i).equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                return;
            }
        }
    }
    
    /** パラメータを設定する */
    public void setValue(String str) {
        if (debug) System.out.println("CheckBoxData#setValue("+str+") option:"+option.size()+" menu:"+menu.size()+" queue:"+queue.size());
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                return;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                return;
            }
        }
    }
    public void setValue(CharArray c) {
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(c)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                return;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(c)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                return;
            }
        }
    }
    
    public void setValue(String[] strs) {
        clearSelection(false);
        for (int i = 0; i < strs.length; i++) {
            //setValue(strs[i]);
            boolean flg = false;
            for (int j = 0; j < option.size(); j++) {
                CharArray ch = option.peek(j);
                if (ch.equals(strs[i])) {
                    queue.poke(j,true);
                    flg = true;
                    break;
                }
            }
            if (!flg) {
                for (int j = 0; j < menu.size(); j++) {
                    CharArray ch = menu.peek(j);
                    if (ch.equals(strs[i])) {
                        queue.poke(j,true);
                        break;
                    }
                }
            }
        }
        _changed = !queue.equals(queueLast);
    }
    
    /** 全てクリアする */
    public void clearSelection() {
        for (int i = 0; i < queue.size(); i++) {
            queue.poke(i,false);
            if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
        }
    }
    /** 全てクリアする (FWで使用する)
        ただし、初期データはクリアしない
    */
    public void clearSelection(boolean mode) {
        if (mode) saveLastValue();
        for (int i = 0; i < queue.size(); i++) {
            queue.poke(i,false);
        }
    }
    
    /** 全てセットする */
    public void setAllSelection(boolean sts) {
        for (int i = 0; i < queue.size(); i++) {
            queue.poke(i,sts);
            if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,sts);
        }
    }
    /** 全てセットする */
    public void allSelection() {
       setAllSelection(true);
    }
    
    /** 直前値を保存する（FWで使用する) */
    private void saveLastValue() {
        queueLast.copy(queue);
        _changed = false;
    }
    
    public void setDisabled(boolean mode) { this.disabled = mode;}

    /** Tag変換を行うか？ */
    public void setTagConvert(boolean mode) { tagConvert = mode;}
    
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getMenu() { return menu; } 
    public CharArrayQueue getOption() { return option; } 
    public BooleanQueue getQueue() { return queue; } 
    public CharArrayQueue getResource() { return resource; } 
    
    
    /** 選択された情報をBit情報で返す */
    public int getSelectedBits() {
        int bit = 0;
        int b = 1;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.peek(i)) {
                bit |= b;
            }
            b <<= 1;
        }
        return bit; 
    }
    
    /** 選択されたインデックスリストを返す */
    public IntQueue getSelectedIndex() {
        IntQueue iq = new IntQueue();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.peek(i)) {
                iq.enqueue(i);
            }
        }
        return iq; 
    }
    /** 選択されたメニューリストを返す */
    public CharArrayQueue getSelectedItem() {
        CharArrayQueue caq = new CharArrayQueue();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.peek(i)) {
                caq.enqueue(menu.peek(i));
            }
        }
        return caq;
    }
    /** 選択された valueリストを返す */
    public CharArrayQueue getSelectedOption() {
        CharArrayQueue caq = new CharArrayQueue();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.peek(i)) {
                caq.enqueue(option.peek(i));
            }
        }
        return caq;
    }
    
    /** 選択されたresourceの集合を返す */
    public CharArrayQueue getSelectedResource() {
        CharArrayQueue caq = new CharArrayQueue();
        //ItemInfo info = getItem().getItemInfo();
        //if (info != null) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.peek(i)) {
                    caq.enqueue(resource.peek(i));
                }
            } // next
        //}
        return caq;
    }
    /** 選択されたメニューリストをカンマ区切りで返す */
    public CharArray getSelectedItemList() {
        CharArray ch = new CharArray();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.peek(i)) {
                if (ch.length() > 0) ch.add(',');
                ch.add(menu.peek(i));
            }
        }
        return ch;
    }
    /** 選択された valueリストをカンマ区切りで返す */
    public CharArray getSelectedOptionList() {
        CharArray ch = new CharArray();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.peek(i)) {
                if (ch.length() > 0) ch.add(',');
                ch.add(option.peek(i));
            }
        }
        return ch;
    }
    
    /** 選択されたresourceの集合をカンマ区切り返す */
    public CharArray getSelectedResourceList() {
        CharArray ch = new CharArray();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.peek(i)) {
                if (ch.length() > 0) ch.add(',');
                ch.add(resource.peek(i));
            }
        } // next
        return ch;
    }
    
    public CharArrayQueue getValue() {
        return getSelectedItem();
    }
    
    /** 指定インデックスが選択されているか 
        @param index 0-
    */
    public boolean isSelected(int index) {
        return queue.peek(index);
    }
    /** 指定オプションが選択されているか 
        @param str オプション名
    */
    public boolean isSelectedOption(String str) {
        int sts = -1;
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(str)) return isSelected(i);
        }
        return false;
    }
    /** 指定オプションが選択されているか 
        @param str オプション名
    */
    public boolean isSelectedOption(CharArray str) {
        int sts = -1;
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(str)) return isSelected(i);
        }
        return false;
    }
    /** 指定リソースが選択されているか 
        @param str 指定リソース名
    */
    public boolean isSelectedResource(String str) {
        int sts = -1;
        for (int i = 0; i < resource.size(); i++) {
            CharArray ch = resource.peek(i);
            if (ch.equals(str)) return isSelected(i);
        }
        return false;
    }
    /** 指定リソースが選択されているか 
        @param str 指定リソース名
    */
    public boolean isSelectedResource(CharArray str) {
        int sts = -1;
        for (int i = 0; i < resource.size(); i++) {
            CharArray ch = resource.peek(i);
            if (ch.equals(str)) return isSelected(i);
        }
        return false;
    }
    
    /** 指定メニューのインデックスを求める */
    public int getIndex(CharArray c) {
        int sts = -1;
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(c)) {
                sts = i;
                break;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(c)) {
                sts = i;
                break;
            }
        }
        return sts;
    }
    /** 指定メニューのインデックスを求める */
    public int getIndex(String c) {
        int sts = -1;
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(c)) {
                sts = i;
                break;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(c)) {
                sts = i;
                break;
            }
        }
        return sts;
    }

    /** 入力不可か？ **/
    public boolean isDisabled() { return disabled;}

    /** オリジナルから変更されているか？ */
    public boolean updated() {
        if (debug) {
            boolean status = !queue.equals(queueOrg);
            System.out.println("★CheckBoxData updated :"+status);
            System.out.println("  -- org --:"+queueOrg.size());
            for (int i = 0; i < queueOrg.size(); i++) {
                if (queueOrg.peek(i)) {
                    System.out.println("  ["+i+"]"+menu.peek(i));
                }
            }
            System.out.println("  -- now --:"+queue.size());
            for (int i = 0; i < queue.size(); i++) {
                if (queue.peek(i)) {
                    System.out.println("  ["+i+"]"+menu.peek(i));
                }
            }
            return status;
        }
        return !queue.equals(queueOrg);
    }

    public boolean getTagConvert() { return tagConvert;}
    
    //---------------------------------------------------------------------
    // タグ出力
    //---------------------------------------------------------------------
    /** 
        タグ全体を出力する
    */
    public CharArray getTag(CharArray ch) {
        return sessionObject.itemRenderer.drawCheckBox(ch,this);
    }
    public CharArray getTag() {
        return sessionObject.itemRenderer.drawCheckBox(new CharArray(),this);
    }
    /** 
        指定タグを出力する
        @param index 0-   -1 でダミーデータを出力
    */
    public CharArray getTag(CharArray ch, int index) {
        return sessionObject.itemRenderer.drawCheckBox(ch,this,index);
    }
    public CharArray getTag(int index) {
        return getTag(new CharArray(),index);
    }
    
    /** 
        指定タグを出力する
        @param index 0-
    */
    public CharArray getNameTag(CharArray ch, int index) {
        return sessionObject.itemRenderer.drawCheckBoxName(ch,this,index);
    }
    public CharArray getNameTag(int index) {
        return getNameTag(new CharArray(),index);
    }
    
    /** Hiddenタグを出力 
        index 0 の時のみ実際に出力される
    */
    public CharArray getHiddenTag(CharArray ch, int index) {
        if (index == 0) sessionObject.itemRenderer.drawCheckBoxHidden(ch,this);
        return ch;
    }
    public CharArray getHiddenTag(int index) {
        return getHiddenTag(new CharArray(),index);
    }
    /** Nameタグ＋Hiddenタグを出力 
        Hidden はindex 0 の時のみ実際に出力される
    */
    public CharArray getNameHiddenTag(CharArray ch, int index) {
        sessionObject.itemRenderer.drawCheckBoxNameHidden(ch,this,index);
        return ch;
    }
    public CharArray getNameHiddenTag(int index) {
        return getNameHiddenTag(new CharArray(),index);
    }
    /** 
        メニュー文字を出力する
        @param index 0-
    */
    public CharArray getMenuTag(CharArray ch, int index) {
        return sessionObject.itemRenderer.drawCheckBoxMenu(ch,this,index);
    }
    public CharArray getMenuTag(int index) {
        return getMenuTag(new CharArray(),index);
    }
    
    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    public void copy(CheckBoxData from) { // 元オブジェクトより全データをコピー
        super.copy(from);
        menu.clear();
        for (int i = 0; i < from.menu.size(); i++) {
            CharArray ch = from.menu.peek(i);
            menu.enqueue(new CharArray(ch));
        }
        option.clear();
        for (int i = 0; i < from.option.size(); i++) {
            CharArray ch = from.option.peek(i);
            option.enqueue(new CharArray(ch));
        }
        resource.copy2(from.resource);
        queue.copy(from.queue);
        queueOrg.copy(from.queueOrg);
        disabled       = from.disabled;
        tagConvert     = from.tagConvert;
    }

    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする
        @param session SessionObject
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        if (visible) {
            session.itemRenderer.drawCheckBox(session.getBuffer(),this);
        }
        return session.getBuffer();
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            menu.writeObject(out);
            option.writeObject(out);
            queue.writeObject(out);
            resource.writeObject(out);
            out.writeBoolean(disabled);
            out.writeBoolean(tagConvert);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            menu.readObject(in);
            option.readObject(in);
            queue.readObject(in);
            resource.readObject(in);
            disabled = in.readBoolean();
            tagConvert = in.readBoolean();
        }
    }
}

//
// [end of CheckBoxData.java]
//

