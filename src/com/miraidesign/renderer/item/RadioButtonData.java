//------------------------------------------------------------------------
// @(#)RadioButtonData.java
//                 <input type=radio> のデータを保管する
//                 Copyright (c) MirfaiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/** ラジオボタン情報を保管する */
public class RadioButtonData extends ItemData implements ItemDataSelectable {
    private CharArrayQueue menu   = new CharArrayQueue();   // メニューリスト
    private CharArrayQueue option = new CharArrayQueue();   // 選択Value（ない時は↑と同じ）
    private CharArrayQueue resource = new CharArrayQueue(); // ItemInfo resource値リスト
    private int selectedIndex = -1; // 選択された番号(0～) -1:指定なし
    private int originalIndex = -1; // 保存用 v0.684
    private boolean disabled  = false;    // 
    private boolean tagConvert = true;   // タグ変換を行うか？
    {
        type = RADIO;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public RadioButtonData() { }
    public RadioButtonData(RadioButtonData from,SessionObject session) {
        this.sessionObject = session;
        copy(from); 
    }
    public RadioButtonData(CharArrayQueue queue) {
        setMenu(queue);
        setOption(queue);
    }
    public RadioButtonData(CharArrayQueue menu, CharArrayQueue option) {
        setMenu(menu);
        setOption(option);
    }
    public RadioButtonData(CharArrayQueue menu, CharArrayQueue option, CharArrayQueue resource) {
        setMenu(menu);
        setOption(option);
        setResource(resource);
    }
    
    //---------------------------------------------------------------------
    //
    //---------------------------------------------------------------------
    /** メニューとオプションのクリア */
    public void clear() { 
        menu.clear();
        option.clear();
        resource.clear();
        selectedIndex = -1;
        if (sessionObject == null || sessionObject.updateMode) originalIndex = -1;
    }
    /** メニューの追加（オプションも同一）*/
    public void add(CharArray ch) { 
        menu.enqueue(ch);
        if (ch.indexOf("\n") >= 0) {
            CharArray ch2 = new CharArray(ch);
            option.enqueue(ch2.replace("\n",""));
        } else {
            option.enqueue(ch);
        }
        resource.enqueue("");
    }
    /** メニューの追加（オプションも同一）*/
    public void add(String str) { 
        menu.enqueue(str);
        if (str.indexOf("\n") >= 0) {
            CharArray ch2 = new CharArray(str);
            option.enqueue(ch2.replace("\n",""));
        } else {
            option.enqueue(str);
        }
        resource.enqueue("");
    }
    /** メニューとオプションの追加 */
    public void add(CharArray menu, CharArray option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
    }
    /** メニューとオプションの追加 */
    public void add(String menu, String option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
    }
    public void add(CharArray menu, String option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
    }
    public void add(String menu, CharArray option) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
    }
    /** メニューとオプションの追加 */
    public void add(CharArray menu, CharArray option, boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        if (selected) selectedIndex = this.menu.size() - 1;
    }
    /** メニューとオプションの追加 */
    public void add(String menu, String option, boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        resource.enqueue("");
        if (selected) selectedIndex = this.menu.size() - 1;
    }
    ///////////////////////////////////
    public void add(CharArray menu, CharArray option, CharArray resource) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        this.resource.enqueue(resource);
    }
    public void add(String menu, String option, String resource) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        this.resource.enqueue(resource);
    }
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        this.resource.enqueue(resource);
        if (selected) selectedIndex = this.menu.size() - 1;
    }
    public void add(String menu, String option, String resource, boolean selected) { 
        this.menu.enqueue(menu);
        this.option.enqueue(option);
        this.resource.enqueue(resource);
        if (selected) selectedIndex = this.menu.size() - 1;
    }
        
    //public void add(String str) { vector.addElement(new CharArray(str));}
    
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    /* メニュー文字列の設定 */
    public void setMenu(CharArrayQueue menu) {
        this.menu = menu;
    }
    /** オプション文字列の設定 */
    public void setOption(CharArrayQueue option) {
        this.option = option; 
    }
    /** リソース文字列の設定 */
    public void setResource(CharArrayQueue resource) {
        this.resource = resource; 
    }
    
    /** 指定Indexのものを選択状態にする (-1で非選択に) */
    public void setSelectedIndex(int index) { 
        selectedIndex = index; 
        if (sessionObject == null || sessionObject.updateMode) originalIndex = index; 
    }
    
    /** 指定されたものが、optionかメニューにあれば selectedにする */
    public void setSelectedItem(String str) {
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(str)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                return;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(str)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                return;
            }
        }
    }
    
    /** 指定されたものが、optionかメニューにあれば selectedにする */
    public void setSelectedItem(CharArray c) {
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(c)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                return;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(c)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                break;
            }
        }
    }
    /** 指定されたものが、メニューにあれば selectedにする */
    public void setSelectedMenu(String str) {
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(str)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                break;
            }
        }
    }
    /** 指定されたものが、メニューにあれば selectedにする */
    public void setSelectedMenu(CharArray c) {
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(c)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                break;
            }
        }
    }
    
    
    /** 指定されたものが、optionにあれば selectedにする */
    public void setSelectedOption(String str) {
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(str)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                return;
            }
        }
    }
    /** 指定されたものが、optionにあれば selectedにする */
    public void setSelectedOption(CharArray c) {
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(c)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                return;
            }
        }
    }

    /** 指定された文字列が、resourceにあれば selectedにする */
    public void setSelectedResource(String str) {
        for (int i = 0; i < resource.size(); i++) {
            if (resource.peek(i).equals(str)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                return;
            }
        }
    }
    /** 指定された文字列が、resourceにあれば selectedにする */
    public void setSelectedResource(CharArray str) {
        for (int i = 0; i < resource.size(); i++) {
            if (resource.peek(i).equals(str)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                return;
            }
        }
    }
    
    public void setValue(String[] strs) {
        int iTmp = selectedIndex;
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(strs[0])) {
                selectedIndex = i;
                _changed = !(iTmp == i);
                return;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(strs[0])) {
                selectedIndex = i;
                _changed = !(iTmp == i);
                return;
            }
        }
        _changed = false;
    }
    /** パラメータを設定する */
    public void setValue(String str) {
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(str)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                return;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(str)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                return;
            }
        }
    }
    public void setValue(CharArray c) {
        for (int i = 0; i < option.size(); i++) {
            CharArray ch = option.peek(i);
            if (ch.equals(c)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                break;
            }
        }
        for (int i = 0; i < menu.size(); i++) {
            CharArray ch = menu.peek(i);
            if (ch.equals(c)) {
                selectedIndex = i;
                if (sessionObject == null || sessionObject.updateMode) originalIndex = i;
                break;
            }
        }
    }
    public void setDisabled(boolean mode) { this.disabled = mode;}

    /** Tag変換を行うか？ */
    public void setTagConvert(boolean mode) { tagConvert = mode;}
    
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    /** 表示文字列リストを返す */
    public CharArrayQueue getMenu()   { return menu; } 
    /** オプション(value)リストを返す */
    public CharArrayQueue getOption() { return option; } 
    /** リソールリストを返す */
    public CharArrayQueue getResource() { return resource; } 
    /** 選択されているインデックスを返す */
    public int getSelectedIndex() { return selectedIndex; }
    /** 選択されている文字列（メニュー）を返す */
    public CharArray getSelectedItem() {
        return menu.peek(selectedIndex);
    }
    /** 選択されている文字列（メニュー）を返す */
    public CharArray getSelectedMenu() {
        return menu.peek(selectedIndex);
    }
    /** 選択されているvalue値を返す */
    public CharArray getSelectedOption() {
        return option.peek(selectedIndex);
    }
    /** 選択されているリソース値を返す */
    public CharArray getSelectedResource() {
        return resource.peek(selectedIndex);
    }
    public CharArrayQueue getValue() {
        caQueue.clear();
        caQueue.enqueue(menu.peek(selectedIndex));
        return caQueue;
    }

    /** 指定インデックスが選択されているか 
        @param index 0-
    */
    public boolean isSelected(int index) {
        return (selectedIndex == index);
    }
    
    /** 指定オプションが選択されているか 
        @param str 指定オプション
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
        @param str 指定オプション
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
        @param str 指定リソース
    */
    public boolean isSelectedResource(String str) {
        int sts = -1;
        for (int i = 0; i < resource.size(); i++) {
            CharArray ch = resource.peek(i);
            if (ch.equals(str)) return isSelected(i);
        }
        return false;
    }
    /** 指定index 0-リソースが選択されているか 
        @param str 指定リソース
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
        int sts = selectedIndex;
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
        int sts = selectedIndex;
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
        return (selectedIndex != originalIndex);
    }

    public boolean getTagConvert() { return tagConvert;}
    
    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    public void copy(RadioButtonData from) { // 元オブジェクトより全データをコピー
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
        selectedIndex  = from.selectedIndex;
        originalIndex  = from.originalIndex;
        disabled       = from.disabled;
        tagConvert     = from.tagConvert;
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
            session.itemRenderer.drawRadioButton(session.getBuffer(),this);
        }
        return session.getBuffer();
    }
    public CharArray getTag(CharArray ch) {
        return sessionObject.itemRenderer.drawRadioButton(ch,this);
    }
    public CharArray getTag() {
        return sessionObject.itemRenderer.drawRadioButton(new CharArray(),this);
    }
    public CharArray getTag(CharArray ch, int index) {
        return sessionObject.itemRenderer.drawRadioButton(ch,this,index);
    }
    public CharArray getTag(int index) {
        return getTag(new CharArray(),index);
    }
    
    public CharArray getNameTag(CharArray ch, int index) {
        return sessionObject.itemRenderer.drawRadioButtonName(ch,this,index);
    }
    public CharArray getNameTag(int index) {
        return getNameTag(new CharArray(),index);
    }
    public CharArray getMenuTag(CharArray ch, int index) {
        return sessionObject.itemRenderer.drawRadioButtonMenu(ch,this,index);
    }
    public CharArray getMenuTag(int index) {
        return getMenuTag(new CharArray(),index);
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            menu.writeObject(out);
            option.writeObject(out);
            out.writeInt(selectedIndex);
            out.writeBoolean(disabled);
            resource.writeObject(out);
            out.writeBoolean(tagConvert);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            menu.readObject(in);
            option.readObject(in);
            selectedIndex = in.readInt();
            disabled = in.readBoolean();
            resource.readObject(in);
            tagConvert = in.readBoolean();
        }
    }
}

//
// [end of RadioButtonData.java]
//

