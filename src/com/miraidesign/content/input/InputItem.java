//------------------------------------------------------------------------
//    InputItem.java
//          ユーザー入力情報を保管するための abstract class
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content.input;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.Parameter;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.content.Input;
import com.miraidesign.content.ContentItem;
import com.miraidesign.content.ContentItemNode;
import com.miraidesign.content.ContentParser;

/**
    ユーザー入力情報を保管するための abstract class
*/
public abstract class InputItem implements ContentItem {
    private static boolean debug = false;
    
    /** ItemID */
    protected int id = 0;
    /** Template ID */
    protected int template_id = 0;
    /** Label ID */
    protected int label_id = 0;
    
    /** Item type */
    protected CharArray type = new CharArray();
    protected CharArray data = new CharArray();
    protected Input input;
    
    /** Item keyword */
    protected CharArray key         = new CharArray();
    /** Item label */
    protected CharArray label       = new CharArray();
    protected CharArray description = new CharArray();
    protected boolean   nullable    = true;
    protected boolean   display     = true;

    /** ブロック参照モード（デフォルト:false) */
    protected boolean reference_mode = false;

    /** アイテム編集可能か？ */
    protected boolean editable = true;

    //-------------------
    // constructor
    //-------------------
    /* コンストラクタ */
    public InputItem(Input input) {
        this.input = input;
        if (input != null) {
            key.set(input.getKey());
            label.set(input.getLabel());
            description.set(input.getDescription());
            display = input.isDisplay();
            nullable = input.isNullable();
        }
    }
    
    /** デフォルトコンストラクタ（システム関数用) */
    public InputItem() {}
    
    /**
        データをコピーする
        @param from コピー元InputItem
    */
    public void copy(InputItem from) {
        this.id = from.id;
        this.template_id = from.template_id;
        this.label_id = from.label_id;
        this.type.set(from.type);
        this.data.set(from.data);
        // this.input = from.input;  // 今回は行わない
        this.key.set(from.key);
        this.label.set(from.label);
        this.description.set(from.description);
        this.nullable = from.nullable;
        this.display = from.display;
        this.editable = from.editable;
    }
    
    //-------------------
    // exchange
    //-------------------
    /**
        InputItem の入れ替えを行う
        @param from InputItem
    */
    public void exchange(InputItem from) {
        data.exchange(from.data);
        boolean _display = this.display;
        this.display = from.display;
        from.display = _display;
        
        boolean _editable = this.editable;
        this.editable = from.editable;
        from.editable = _editable;
        
    }
    public ContentItemNode getContentItemNode() { return null;}
    
    //-------------------
    // method
    //------------------
    static public InputItem createInputItem(Input input, ContentParser parser) {
        InputItem item = null;
        /////////////////////////////////////////// 今は使わない  2010/04/01
/*
        if (input != null) {
            CharArray type = input.getType();
            if (type != null) {
                //if (type.equals("TEXT")) {
                if (type.equals(Input.typeNames[Input.TEXT])) {
                    item = new InputText(input, parser);
                // } else if (type.equals("TEXTAREA")) {
                } else if (type.equals(Input.typeNames[Input.TEXTAREA])) {
                    item = new InputTextArea(input, parser);
                //} else if (type.equals("HTML")) {
                } else if (type.equals(Input.typeNames[Input.HTML])) {
                    item = new InputHtml(input, parser);
                //} else if (type.equals("IMAGE")) {
                } else if (type.equals(Input.typeNames[Input.IMAGE])) {
                    item = new InputImage(input, parser);
                //} else if (type.equals("FILE")) {
                } else if (type.equals(Input.typeNames[Input.FILE])) {
                    item = new InputFile(input, parser);
                //} else if (type.equals("LINK")) {
                } else if (type.equals(Input.typeNames[Input.LINK])) {
                    item = new InputLink(input, parser);
                } else if (type.equals(Input.typeNames[Input.SELECT])) {
                    item = new InputSelect(input, parser);
                //} else if (type.equals("TABLE")) {
                //    item = new InputTable(input, parser);
                //} else if (type.equals("COMMON_TEXT")) {
                //    item = new InputCommonText(input, parser);
                }
            }
        }
*/
        ///////////////////////////////////////////////////////////////////
        return item;
    }
    
    static public InputItem createInputItem(CharArray type) {
        return createInputItem(type.toString());
    }
    static public InputItem createInputItem(String type) {
        InputItem item = null;
        if (type != null) {
            /* 利用中止
            if (type != null) {
                if (type.equals(Input.typeNames[Input.TEXT])) {
                    item = new InputText();
                } else if (type.equals(Input.typeNames[Input.TEXTAREA])) {
                    item = new InputTextArea();
                } else if (type.equals(Input.typeNames[Input.HTML])) {
                    item = new InputHtml();
                } else if (type.equals(Input.typeNames[Input.IMAGE])) {
                    item = new InputImage();
                } else if (type.equals(Input.typeNames[Input.FILE])) {
                    item = new InputFile();
                } else if (type.equals(Input.typeNames[Input.LINK])) {
                    item = new InputLink();
                } else if (type.equals(Input.typeNames[Input.SELECT])) {
                    item = new InputSelect();
                }
            }
            ****************************/
        }
        return item;
    }
   
    public boolean isBlock() {return false;}
    public boolean isInput() {return true;}
    public boolean isTemplate() {return false;}
    
    //------------------------------------------------
    // setter getter
    //------------------------------------------------
    /* item_id を設定する */
    public void setID(int id) {
        this.id = id;
    }
    /* item_id を取得する */
    public int getID() { return this.id; }

    /* template_id を設定する */
    public void setTemplateID(int id) {
        this.template_id = id;
    }
    /* template_id を取得する */
    public int getTemplateID() { return this.template_id; }
    
    /* label_id を設定する */
    public void setLabelID(int id) {
        this.label_id = id;
    }
    /* label_id を取得する */
    public int getLabelID() { return this.label_id; }
    
    /* キーワードを設定する */
    public void setKey(CharArray key) {
        this.key.set(key);
    }
    public void setKey(String key) {
        this.key.set(key);
    }
    /* キーワードを取得する 
        @return 存在しない場合は null
    */
    public CharArray getKey() { return key; }

    /* ラベルを設定する */
    public void setLabel(CharArray label) {
        this.label.set(label);
    }
    public void setLabel(String label) {
        this.label.set(label);
    }
    /** ラベルを取得する 
        @return 存在しない場合は null
    */
    public CharArray getLabel() { return label; }

    /* コメントを設定する */
    public void setDescription(CharArray description) {
        this.description.set(description);
    }
    public void setDescription(String description) {
        this.description.set(description);
    }
    /** コメントを取得する 
        @return 存在しない場合は null
    */
    public CharArray getDescription() { return description; }
    
    /* 表示モードを取得 (デフォルト:true) */
    public boolean isDisplay() { return display; }

    /* 表示モードを設定する */
    public void setDisplay(boolean mode) { this.display = mode; }

    /* オリジナルアイテムを返す */
    public ContentItem getOriginalItem() {
        return input;
    }
    
    /* 空白を認めるかどうかの設定 */
    public void setNullable(boolean b) {
        nullable = b;
    }
    
    /* 空白を認めるか？ */
    public boolean isNullable() {
        return nullable;
    }
    
    /* 入力タイプを取得する */
    public CharArray getType() { return type; }
    
    /* データを設定する */
    public void setData(CharArray data) {
//System.out.println("●setData type="+type+" key="+key+" data:"+this.data+" -> "+data);
        this.data.set(data);
    }
    public void setData(String data) {
        this.data.set(data);
    }
    /* データを取得する */
    public CharArray getData() { return data; }
    
    /* デフォルト関数する */
    public CharArray getMain() { return get();}
    
    /* デフォルト関数 */
    public abstract CharArray get();

    /* デフォルト値を取得する */
    public CharArray getDefault() { return input.getDefault();}

    public abstract void set(CharArray ch);
    public void setMain(CharArray ch) { set(ch);}

    /** アイテム参照モードを設定する
        @param mode アイテム参照モード
     */
    public void setReferenceMode(boolean mode) {
        this.reference_mode = mode;
    }
    /** アイテム参照モードを取得する 
        @return アイテム参照モード
    */
    public boolean getReferenceMode() { return reference_mode; }
    public boolean isReferenceMode() { return reference_mode; }

    /* 編集可能モードを設定する */
    public void setEditable(boolean mode) { editable = mode; }
    /* 編集可能か？ */
    public boolean isEditable() { return editable;}

    /*
        関数呼び出し
        @param func 関数名 
        @param param 関数パラメータ（未サポート）
    */
    public abstract CharArray getFunc(CharArray func, CharArrayQueue param);

    /*
        関数呼び出し （ユーザーは使用しない事）
        @param func 関数名 
        @param param 関数パラメータ（未サポート）
        @param state
    */
    public abstract CharArray getFunc(CharArray func, CharArrayQueue param, int state);
    public CharArray getFunc(CharArray func, CharArrayQueue param, int state, SessionObject session) {
if (debug) System.out.println("▽▽InputItem#getFunc()？？");
        return getFunc(func, param, state);
    } 

    /* 
         （ユーザーは使用しない事）
        @param func 関数名 
        @param param 関数パラメータ
    */
    public Parameter getParameter(CharArray func, CharArrayQueue param, Parameter p) {
if (debug) System.out.println("▽▽InputItem#getParameter(1)？？");
        p.add(getFunc(func, param, 1));
        return p;
    }
    public Parameter getParameter(CharArray func, CharArrayQueue param, Parameter p, SessionObject session) {
if (debug) System.out.println("▽▽InputItem#getParameter(2)？？");
        p.add(getFunc(func, param, 1));
        return p;
    }
    public Parameter getListParameter(CharArray func, CharArrayQueue param, Parameter p, SessionObject session) {
if (debug) System.out.println("▽▽InputItem#getListParameter(3)？？");
        return getParameter(func, param, p, session);
    }

    /*
        関数設定
        @param func 関数名 
        @param param 関数パラメータ（今のところ先頭のみ使用）
    */
    public abstract boolean setFunc(CharArray func, CharArrayQueue param);

    public boolean setFunc(String func, CharArrayQueue param) {
        CharArray ch = CharArray.pop(func);
        boolean sts = setFunc(ch, param);
        CharArray.push(ch);
        return sts;
    }
    
    /*
        関数設定
        @param func 関数名 
        @param param 関数パラメータ
    */
    public abstract boolean setFunc(CharArray func, CharArray param);
    
    public boolean setFunc(String func, CharArray param) {
        CharArray ch = CharArray.pop(func);
        boolean sts = setFunc(ch,param);
        CharArray.push(ch);
        return sts;
    }
}

//
// [end of InputItem.java]
//

