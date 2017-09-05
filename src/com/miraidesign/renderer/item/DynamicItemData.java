//------------------------------------------------------------------------
// @(#)DynamicItemData.java
//             可変アイテムデータ用インターフェース
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Enumeration;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.ObjectQueue;

/**
 *  可変アイテムデータ用
 *   
 *  @version 0.9 
 *  @author Toru Ishioka
**/
public abstract class DynamicItemData extends ItemData{
    protected ObjectQueue queue = new ObjectQueue();  // データ保管エリア
    //public abstract void add(ItemData itemData);

    public void setItem(Item item) {
        super.setItem(item);
        for (int i = 0; i < queue.size(); i++) {
            ItemData id = (ItemData)queue.peek(i);
            id.setItem(item);
        }
    }

    public void setSessionObjectAll(SessionObject session) {
        super.setSessionObject(session);
        for (int i = 0; i < queue.size(); i++) {
            ItemData id = (ItemData)queue.peek(i);
            id.setSessionObject(session);
        }
    }

    /** ItemData を追加する **/
    public void add(ItemData itemData) {
        itemData.setItem(getItem());    // 親アイテムの設定
        itemData.setParentItemData(this);     // 親アイテムデータの設定
        itemData.setSessionObject(sessionObject);
        itemData.setCount(queue.size());
        queue.enqueue(itemData);
    }
    public void add(ItemData itemData,ItemData... itemDataList) {
        add(itemData);
        for (int i = 0; i < itemDataList.length; i++) {
            add(itemDataList[i]);
        }
    }


    public void add(String str) { add(new StringData(str)); }
    public void add(CharArray str) { add(new StringData(str));}

    public void add(String str,ItemData... itemDataList) {
        add(str);
        for (int i = 0; i < itemDataList.length; i++) {
            add(itemDataList[i]);
        }
    }
    public void add(CharArray str,ItemData... itemDataList) {
        add(str);
        for (int i = 0; i < itemDataList.length; i++) {
            add(itemDataList[i]);
        }
    }
    public void add(ItemData itemData1,String str) {
        add(itemData1);
        add(str);
    }
    public void add(ItemData itemData1,CharArray str) {
        add(itemData1);
        add(str);
    }
    /** ItemData をクリアする **/
    public void clear() {
        queue.clear();
    }
    
    /** ItemDataの集合を返す */
    public Enumeration getItemDataList() {
        return queue.elements();
    }
    
    /** ItemDataのObjectQUeueを返す */
    public ObjectQueue getQueue() {
        return queue;
    }
    /** 登録されているitemDataの数を返す */
    public int size() {
        return queue.size();
    }
   
    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    public void drawContainer(SessionObject session) {
        for (int i = 0; i < queue.size(); i++) {
            ItemData id = (ItemData)queue.peek(i);
            id.draw(session);
        }
    }
    public void drawContainer() {
        for (int i = 0; i < queue.size(); i++) {
            ItemData id = (ItemData)queue.peek(i);
            id.draw(sessionObject);
        }
    }
    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    public void copyAll(DynamicItemData from) { // 元オブジェクトより全データをコピー
        super.copy(from);
        queue.clear();

        for (int i = 0; i < from.queue.size(); i++) {
            ItemData itemData = ItemData.createItemData((ItemData)from.queue.peek(i),getItem(), sessionObject);

            itemData.setItem(getItem());    // 親アイテムの設定
            itemData.setParentItemData(this);     // 親アイテムデータの設定
            itemData.setSessionObject(sessionObject);   // もはや必要ないが、、
            itemData.setCount(queue.size());
            
            queue.enqueue(itemData);
        }
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------

    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            int size = queue.size();
            out.writeInt(size);
            for (int i = 0; i < size; i++) {
                ItemData itemData = (ItemData)queue.peek(i);
                out.writeInt(itemData.getType());
                itemData.writeObject(out);
            }
        }
    }
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            int size = in.readInt();
            queue.clear();
            for (int i = 0; i < size; i++) {
                int type = in.readInt();
                ItemData itemData = null;
                switch (type) {
                    case FORM:          itemData = new FormData();          break;
                    //case BLOCK:         itemData = new BlockData();         break;
                    case ANCHOR_STRING: itemData = new AnchorStringData();  break;
                    case STRING:        itemData = new StringData();        break;
                    //case LINE_FEED:     itemData = new LineFeedData();      break;
                    //case HAIR_LINE:     itemData = new HairLineData();      break;
                    case IMG:           itemData = new ImgData();           break;
                    case TEXT:          itemData = new TextData();          break;
                    case HIDDEN:        itemData = new HiddenData();        break;
                    case PASSWORD:      itemData = new PasswordData();      break;
                    case CHECKBOX:      itemData = new CheckBoxData();      break;
                    case RADIO:         itemData = new RadioButtonData();   break;
                    case SUBMIT:        itemData = new SubmitData();        break;
                    //case IMAGE:         itemData = new ImageData();         break;
                    case TEXTAREA:      itemData = new TextAreaData();      break;
                    case LIST_BOX:      itemData = new ListBoxData();       break;
                    case DYNAMIC:       itemData = new DynamicData();       break;
                    case QUEUE_TABLE:   itemData = new QueueTableData();    break;
                    //case ELEMENT:       itemData = new ElementData();       break;
                    case PARAMETER:     itemData = new ParameterData();     break;
                    //case HASH :       itemData = new HashData();       break;
                    default:
                        System.out.println("DynamicItemData#readObject type:"+type+")は生成できません");
                    break;
                }
                if (itemData != null) {
                    itemData.setItem(getItem());    // 親アイテムの設定
                    itemData.setParentItemData(this);     // 親アイテムデータの設定
                    itemData.setSessionObject(sessionObject);
                    itemData.readObject(in);
                    add(itemData);
                }
            }
        }
    }

}

//
// [end of DynamicItemData.java]
//

