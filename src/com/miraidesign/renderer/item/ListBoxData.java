//------------------------------------------------------------------------
// @(#)ListBoxData.java
//                 [select] のデータを保管する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
//
package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.BooleanQueue;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.IntQueue;

/**
 *  リストボックスデータ
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class ListBoxData extends ItemData implements ItemDataSelectable {
    private CharArrayQueue menu = new CharArrayQueue();   // メニューリスト
    private CharArrayQueue option = new CharArrayQueue();   // オプション値リスト
    private CharArrayQueue resource = new CharArrayQueue();   // ItemInfo情報
    private BooleanQueue   queue = new BooleanQueue(); // 選択フラグ
    private BooleanQueue   queueOrg = new BooleanQueue(); // 保存用 v0.684
    private int size      = -1;  // デフォルト幅    -1: 指定なし 1:でプルダウン
    private boolean multiple = false;   // 複数選択可能？
    private boolean disabled  = false;    // 
    {
        type = LIST_BOX;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public ListBoxData() { }
    public ListBoxData(int size) { 
        setSize(size); 
    }
    public ListBoxData(ListBoxData from,SessionObject session) {
        this.sessionObject = session;
        copy(from); 
    }
    public ListBoxData(CharArrayQueue menu) {
        setMenu(menu);
        for (int i = 0; i < menu.size(); i++) {
            option.enqueue(new CharArray());
            resource.enqueue("");
            queue.enqueue(false);
            queueOrg.enqueue(false);
        }
    }
    public ListBoxData(CharArrayQueue menu, CharArrayQueue option) {
        setMenu(menu);
        setOption(option);
        for (int i = 0; i < menu.size(); i++) {
            option.enqueue(new CharArray());
            resource.enqueue("");
            queue.enqueue(false);
            queueOrg.enqueue(false);
        }
    }
    public ListBoxData(CharArrayQueue menu,BooleanQueue queue) {
        setMenu(menu);
        setOption(menu);
        setQueue(queue);
        for (int i = 0; i < queue.size(); i++) {
            resource.enqueue("");
        }
    }
    public ListBoxData(CharArrayQueue menu, CharArrayQueue option, BooleanQueue queue) {
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
    /** メニュー情報を全てクリアする*/
    public void clear() { 
        menu.clear(); 
        option.clear(); 
        resource.clear(); 
        queue.clear(); 
        if (sessionObject == null || sessionObject.updateMode) queueOrg.clear(); 
    }
    public void add(CharArray ch) { 
        menu.enqueue(ch);
        option.enqueue(new CharArray());
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    public void add(String str) { 
        menu.enqueue(str);
        option.enqueue(new CharArray());
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    public void add(CharArray menu, CharArray option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    public void add(String menu, String option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    public void add(CharArray menu, String option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    public void add(String menu, CharArray option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    public void add(CharArray menu, CharArray option, boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(selected);
        queueOrg.enqueue(selected);
    }
    public void add(String menu, String option, boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        queue.enqueue(selected);
        queueOrg.enqueue(selected);
    }
    
    ///////////////////////////////////
    /** メニュー追加 
        @param menu メニュー文字列
        @param option オプション文字列(value)
        @param resource ItemInfoリソース
        @param selected 初期選択値
    */
    public void add(CharArray menu, CharArray option, CharArray resource,boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        this.resource.enqueue(resource);
        queue.enqueue(selected);
        queueOrg.enqueue(selected);
    }
    public void add(CharArray menu, CharArray option, CharArray resource) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        this.resource.enqueue(resource);
        queue.enqueue(false);
        queueOrg.enqueue(false);
    }
    /** メニュー追加 
        @param menu メニュー文字列
        @param option オプション文字列(value)
        @param resource ItemInfoリソース
        @param selected 初期選択値
    */
    public void add(String menu, String option, String resource, boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        this.resource.enqueue(resource);
        queue.enqueue(selected);
        queueOrg.enqueue(selected);
    }
    public void add(String menu, String option, String resource) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        this.resource.enqueue(resource);
        queue.enqueue(false);
        queueOrg.enqueue(false);
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
    
    public void setSize(int size) { this.size = size; }
    public void setMultiple(boolean mode) { this.multiple = mode;}
    public void setDisabled(boolean mode) { this.disabled = mode;}

    /** 指定indexをselected にする*/
    public void setSelectedIndex(int index) { 
        if (multiple == false) {
            for (int i = 0; i < queue.size(); i++) {
                queue.poke(i,false);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
            }
        }
        queue.poke(index,true);
        if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(index,true);
    }
    public void setSelectedIndex(int index, boolean b) { 
        if (multiple == false) {
            if (b) {
                for (int i = 0; i < queue.size(); i++) {
                    queue.poke(i,false);
                    if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
                }
            }
        }
        queue.poke(index,b);
        if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(index,b);
    }
    
    /** 指定されたものが、optionかメニューにあれば selectedにする */
    public void setSelectedItem(String str) {
        if (multiple == false) {
            for (int i = 0; i < queue.size(); i++) {
                queue.poke(i,false);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
            }
        }
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
    
    /** 指定されたものが、optionかメニューにあれば selectedにする */
    public void setSelectedItem(CharArray c) {
        if (multiple == false) {
            for (int i = 0; i < queue.size(); i++) {
                queue.poke(i,false);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
            }
        }
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
                break;
            }
        }
    }
    /** 指定されたものが、メニューにあれば selectedにする */
    public void setSelectedMenu(String str) {
        if (multiple == false) {
            for (int i = 0; i < queue.size(); i++) {
                queue.poke(i,false);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                break;
            }
        }
    }
    /** 指定されたものが、メニューにあれば selectedにする */
    public void setSelectedMenu(CharArray c) {
        if (multiple == false) {
            for (int i = 0; i < queue.size(); i++) {
                queue.poke(i,false);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(c)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                break;
            }
        }
    }
    /** 指定されたものが、optionにあれば selectedにする */
    public void setSelectedOption(String str) {
        if (multiple == false) {
            for (int i = 0; i < queue.size(); i++) {
                queue.poke(i,false);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
            }
        }
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                return;
            }
        }
    }
    /** 指定されたものが、optionにあれば selectedにする */
    public void setSelectedOption(CharArray c) {
        if (multiple == false) {
            for (int i = 0; i < queue.size(); i++) {
                queue.poke(i,false);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
            }
        }
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(c)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                return;
            }
        }
    }

    /** 指定された文字列が、resourceにあれば selectedにする */
    public void setSelectedResource(String str) {
        if (multiple == false) {
            for (int i = 0; i < queue.size(); i++) {
                queue.poke(i,false);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
            }
        }
        for (int i = 0; i < resource.size(); i++) {
            if (resource.peek(i).equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                return;
            }
        }
    }
    
    /** 指定された文字列が、resourceにあれば selectedにする */
    public void setSelectedResource(CharArray str) {
        if (multiple == false) {
            for (int i = 0; i < queue.size(); i++) {
                queue.poke(i,false);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
            }
        }
        for (int i = 0; i < resource.size(); i++) {
            if (resource.peek(i).equals(str)) {
                queue.poke(i,true);
                if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,true);
                return;
            }
        }
    }
    
    /**ユーザーは使用しません*/
    public void setValue(String[] strs) {
        BooleanQueue queueTmp = new BooleanQueue();
        queueTmp.copy(queue);
    
        for (int i = 0; i < queue.size(); i++) {
            queue.poke(i, false);
        }
        for (int i = 0; i < strs.length; i++) {
            //setSelectedItem(strs[i]);
            boolean flg = false;
            for (int j = 0; j < option.size(); j++) {
                CharArray ch = option.peek(j);
                if (ch != null && ch.equals(strs[i])) {
                    queue.poke(j,true);
                    flg = true;
                    break;
                }
            }
            if (!flg) {
                for (int j = 0; j < menu.size(); j++) {
                    CharArray ch = menu.peek(j);
                    if (ch != null && ch.equals(strs[i])) {
                        queue.poke(j,true);
                        break;
                    }
                }
            }
        }
        _changed = !queue.equals(queueTmp);
    }
    /** 全てクリアする */
    public void clearSelection() {
        for (int i = 0; i < queue.size(); i++) {
            queue.poke(i,false);
            if (sessionObject == null || sessionObject.updateMode) queueOrg.poke(i,false);
        }
    }
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getMenu() { return menu; } 
    public CharArray getMenu(int index) { return menu.peek(index); } 
    public CharArrayQueue getOption() { return option; } 
    public CharArray getOption(int index) { return option.peek(index); } 
    
    public CharArrayQueue getResource() { return resource; } 
    public CharArray getResource(int index) { return resource.peek(index); } 
    public BooleanQueue getQueue() { return queue; } 
    public boolean getQueue(int index) { return queue.peek(index); } 
    
    public int getSize()      { return size; }
    public boolean isMultiple() { return multiple;}
    public boolean isDisabled() { return disabled;}

    /** 選択されたインデックスの集合を返す */
    public IntQueue getSelectedIndex() {
        IntQueue iq = new IntQueue();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.peek(i)) {
                iq.enqueue(i);
            }
        }
        return iq; 
    }
    /** 選択されたアイテムの集合を返す */
    public CharArrayQueue getSelectedItem() {
        CharArrayQueue cq = new CharArrayQueue();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.peek(i)) {
                cq.enqueue(menu.peek(i));
            }
        }
        return cq;
    }
    /** 選択されたアイテムの集合を返す */
    public CharArrayQueue getSelectedMenu() {
        CharArrayQueue cq = new CharArrayQueue();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.peek(i)) {
                cq.enqueue(menu.peek(i));
            }
        }
        return cq;
    }
    /** 選択されたオプションの集合を返す */
    public CharArrayQueue getSelectedOption() {
        CharArrayQueue cq = new CharArrayQueue();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.peek(i)) {
                cq.enqueue(option.peek(i));
            }
        }
        return cq;
    }

    /** 選択されたDB出力値の集合を返す */
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
        @param str 指定オプション
    */
    public boolean isSelectedOption(String str) {
        int sts = -1;
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch != null && ch.equals(str)) return isSelected(i);
        }
        return false;
    }
    /** 指定オプションが選択されているか 
        @param str 指定オプション
    */
    public boolean isSelectedOption(CharArray str) {
        int sts = -1;
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch != null && ch.equals(str)) return isSelected(i);
        }
        return false;
    }
    /** 指定リソースが選択されているか 
        @param str 指定リソース
    */
    public boolean isSelectedResource(String str) {
        int sts = -1;
        for (int i = 0; i < resource.size(); i++) {
            CharArray ch = resource.peek(i);
            if (ch != null && ch.equals(str)) return isSelected(i);
        }
        return false;
    }
    /** 指定リソースが選択されているか 
        @param str 指定リソース
    */
    public boolean isSelectedResource(CharArray str) {
        int sts = -1;
        for (int i = 0; i < resource.size(); i++) {
            CharArray ch = resource.peek(i);
            if (ch != null && ch.equals(str)) return isSelected(i);
        }
        return false;
    }
    
    
    /** 指定メニューのインデックスを求める */
    public int getIndex(CharArray c) {
        int sts = -1;
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch != null && ch.equals(c)) {
                sts = i;
                break;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch != null && ch.equals(c)) {
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
            if (ch != null && ch.equals(c)) {
                sts = i;
                break;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch != null && ch.equals(c)) {
                sts = i;
                break;
            }
        }
        return sts;
    }

    /** オリジナルから変更されているか？ */
    public boolean updated() {
        return !queue.equals(queueOrg);
    }

    //---------------------------------------------------------------------
    // remove
    //---------------------------------------------------------------------
    /** オプションの内容の行を削除する */
    public void removeOption(CharArray str) {
        int index = -1;
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch != null && ch.equals(str)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            menu.remove(index);
            option.remove(index);
            resource.remove(index);
            queue.remove(index);
        }
    }
    
    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    public void copy(ListBoxData from) { // 元オブジェクトより全データをコピー
        menu.clear();
        for (int i = 0; i < from.menu.size(); i++) {
            CharArray ch = (CharArray)from.menu.peek(i);
            menu.enqueue(new CharArray(ch));
        }
        option.clear();
        for (int i = 0; i < from.option.size(); i++) {
            CharArray ch = (CharArray)from.option.peek(i);
            option.enqueue(new CharArray(ch));
        }
        
        resource.copy2(from.resource);
        
        queue.copy(from.queue);
        queueOrg.copy(from.queueOrg);
        size           = from.size;
        multiple       = from.multiple;
        disabled       = from.disabled;
    }

    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする
        @param session セッションオブジェクト
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        if (visible) {
            session.itemRenderer.drawListBox(session.getBuffer(),this);
        }
        return session.getBuffer();
    }
    
    public CharArray getTag(CharArray ch) {
        return sessionObject.itemRenderer.drawListBox(ch, this);
    }
    public CharArray getTag() {
        return sessionObject.itemRenderer.drawListBox(new CharArray(), this);
    }
    public CharArray getNameTag(CharArray ch) {
        return sessionObject.itemRenderer.drawListBoxName(ch, this);
    }
    public CharArray getNameTag() {
        return getNameTag(new CharArray());
    }
    
    public CharArray getMenuTag(CharArray ch) {
        return sessionObject.itemRenderer.drawListBoxMenu(ch, this);
    }
    public CharArray getMenuTag() {
        return getMenuTag(new CharArray());
    }
    public CharArray getNameMenuTag(CharArray ch) {
        return sessionObject.itemRenderer.drawListBoxNameMenu(ch, this);
    }
    public CharArray getNameMenuTag() {
        return getNameMenuTag(new CharArray());
    }
    
    public CharArray getMenuTag(CharArray ch, int index) {
        return sessionObject.itemRenderer.drawListBoxMenu(ch, this, index);
    }
    public CharArray getMenuTag(int index) {
        return getMenuTag(new CharArray(), index);
    }
    public CharArray getOptionTag(CharArray ch, int index) {
        return sessionObject.itemRenderer.drawListBoxOption(ch, this, index);
    }
    public CharArray getOptionTag(int index) {
        return getOptionTag(new CharArray(), index);
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
            out.writeInt(size);
            out.writeBoolean(multiple);
            out.writeBoolean(disabled);
            resource.writeObject(out);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            menu.readObject(in);
            option.readObject(in);
            queue.readObject(in);
            size     = in.readInt();
            multiple = in.readBoolean();
            disabled = in.readBoolean();
            resource.readObject(in);
        }
    }

}

//
// [end of ListBoxData.java]
//

