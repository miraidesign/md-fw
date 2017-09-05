//------------------------------------------------------------------------
// @(#)ItemData.java
//                 
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/** itemデータ (abstract) */
public abstract class ItemData implements ItemConstant, ItemSerializable {
    static private boolean debug = (SystemConst.debug && false);  // デバッグ表示
    protected Item item;
    public void setItem(Item item) { this.item = item; }
    public Item getItem() { return item; }
    //--------------------------------------------------------
    protected ItemData parentItemData;
    public void setParentItemData(ItemData itemdata) { this.parentItemData = itemdata;}
    public ItemData getParentItemData() { return parentItemData;}
    //--------------------------------------------------------
    protected int type;
    public void setType(int type) { this.type = type; }
    public int  getType() { return type; }
    public String getTypeName() { return Item.getTypeName(type); }
    public boolean isContainer() { return (type >= 0 && type < 20); }
    public boolean isDisplay()   { return (type >=20 && type < 40); }
    public boolean isInput()     { return (type >=40 && type < 60); }
    public boolean isButton()    { return (type >=60 && type < 80); }
    public boolean isSelect()    { return (type >=80 && type <100); }
    public boolean isMultiple()  { return (type >=100 && type <200); }
    public boolean isNonGUI()    { return (type >=500 && type <600); }
    //--------------------------------------------------------
    protected SessionObject sessionObject;
    public void setSessionObject(SessionObject obj) { sessionObject = obj; }
    public SessionObject getSessionObject() { return sessionObject; }
    
    //--------------------------------------------------------
    protected CharArrayQueue caQueue = new CharArrayQueue();
    //--------------------------------------------------------
    public abstract void setValue(String[] strs);   // パラメータ
    public CharArrayQueue getValue() {return caQueue;}
    //--------------------------------------------------------
    //public abstract void setValue(String str);
    //public abstract void setValue(CharArray ch);
    // --------------------------------------------
    protected boolean _changed = false;     // シリアライズしない

    /** 直前から変更があったか？ */
    public boolean changed() { return _changed; }

    /** 
       初期値から変更されたか？ <br>
       オーバーライドする
    */
    public boolean updated() { return false;} 
    
    //--------------------------------------------------------
    protected boolean visible = true;   // 表示するかどうか
    public void setVisible(boolean mode) { visible = mode; }
    public boolean isVisible() { return visible;}
    //--------------------------------------------------------
    protected int count = 0;   // DynamicData内等でのカウント用
    public void setCount(int count) { this.count = count; }
    public int getCount() { return count; }
    public String getName() { 
        if (parentItemData == null) {
//System.out.println("  "+getTypeName()+" parentなし count:"+count);
            if (count == 0) return "";
            return SystemConst.dataKey+count;
        } else {
//System.out.println("  "+getTypeName()+"   parent がある:"+count);
            return parentItemData.getName()+SystemConst.dataKey+count;
        }
    }
    
    //---------------------------------------------------------
    // 
    /** 
        アンカー全体を出力する (HTMLのみ)
    */
    public CharArray getTag() {
        return getTag(new CharArray());
    }
    public CharArray getTag(CharArray ch) {
        System.out.println("ItemData.getTag() オーバーライドする必要があります");
        return ch;
    }
    
    //--------------------------------------------------------
    /** コピーする */
    public void copy(ItemData from) {
//System.out.println("    ★ItemData Copy Start. "+getTypeName());
        this.item = from.item;
        this.visible = from.visible;
        this.type = from.type;
        this._changed = from._changed;
        //this.sessionObject = from.sessionObject;      //やめる！ 
        caQueue.clear();
        for (int i = 0; i < from.caQueue.size(); i++) {
            caQueue.enqueue(new CharArray(from.caQueue.peek(i)));
        }
        this.count = from.count;
        
//System.out.println("    ★ItemData Copy End.   "+getTypeName());
    }
    
    //--------------------------------------------------------
    
    public abstract CharArray draw(SessionObject session);

    //public abstract void copy(ItemData from); // 元オブジェクトより全データをコピー
    
    static public ItemData createItemData(ItemData from, 
                                          Item parent, 
                                          SessionObject session) {
        ItemData itemData= null;
        int type = from.getType();
        switch (type) {
            //case CONTAINER:
            //    break;
            case FORM:
                itemData = new FormData((FormData)from, session);
                itemData.setItem(parent);
                break;
            //case BLOCK:
            //    itemData = new BlockData((BlockData)from, session);
            //    itemData.setItem(parent);
            //    break;
            
            case ANCHOR_STRING:
                itemData = new AnchorStringData((AnchorStringData)from, session);
                itemData.setItem(parent);
                break;
            case STRING:
                itemData = new StringData((StringData)from,session);
                itemData.setItem(parent);
                break;
            //case LINE_FEED:
            //    itemData = new LineFeedData((LineFeedData)from, session);
            //    itemData.setItem(parent);
            //    break;
            //case HAIR_LINE:
            //    itemData = new HairLineData((HairLineData)from, session);
            //    itemData.setItem(parent);
            //    break;
            case IMG:
                itemData = new ImgData((ImgData)from, session);
                itemData.setItem(parent);
                break;
            //case ANCHOR_IMG:
            //    itemData = new AnchorImgData((AnchorImgData)from, session);
            //    itemData.setItem(parent);
            //    break;
            case TEXT:
                itemData = new TextData((TextData)from, session);
                itemData.setItem(parent);
                break;
            case HIDDEN:
                itemData = new HiddenData((HiddenData)from, session);
                itemData.setItem(parent);
                break;
            case PASSWORD:
                itemData = new PasswordData((PasswordData)from, session);
                itemData.setItem(parent);
                break;
            case CHECKBOX:
                itemData = new CheckBoxData((CheckBoxData)from, session);
                itemData.setItem(parent);
                break;
            case RADIO:
                itemData = new RadioButtonData((RadioButtonData)from, session);
                itemData.setItem(parent);
                break;
            case SUBMIT:
                itemData = new SubmitData((SubmitData)from, session);
                itemData.setItem(parent);
                break;
            //case RESET:
            //    itemData = new ResetData((ResetData)from, session);
            //    itemData.setItem(parent);
            //    break;
            //case BUTTON:
            //    itemData = new ButtonData((ButtonData)from, session);
            //    itemData.setItem(parent);
            //    break;
            //case IMAGE:
            //   itemData = new ImageData((ImageData)from, session);
            //    itemData.setItem(parent);
            //    break;
            case TEXTAREA:
                itemData = new TextAreaData((TextAreaData)from, session);
                itemData.setItem(parent);
                break;
            case LIST_BOX:
                itemData = new ListBoxData((ListBoxData)from, session);
                itemData.setItem(parent);
                break;
                
            case DYNAMIC: 
                itemData = new DynamicData((DynamicData)from, session);
                itemData.setItem(parent);
                break;
                
            case QUEUE_TABLE:
                itemData = new QueueTableData((QueueTableData)from, session);
                itemData.setItem(parent);
                break;
            //case ELEMENT:
            //    itemData = new ElementData((ElementData)from, session);
            //    itemData.setItem(parent);
            //    break;
            case PARAMETER:
                itemData = new ParameterData((ParameterData)from, session);
                itemData.setItem(parent);
                break;
            default:
                System.out.println("ItemData#createItemData("+from.getTypeName()+")は生成できません");
                break;
        }
        return itemData;
    }
    
    
    
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    //protected Item item;
    //protected int type;
    //protected SessionObject sessionObject;
    //protected CharArrayQueue caQueue = new CharArrayQueue();
    //--------------------------------------------------------
    //protected boolean visible = true;   // 表示するかどうか

    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            if (debug) System.out.println("ItemData;writeObject "+getTypeName());
            out.writeInt(type);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            type = in.readInt();
            if (debug) System.out.println("ItemData;readObject "+getTypeName());
        }
    }
}

//
// [end of ItemData.java]
//

