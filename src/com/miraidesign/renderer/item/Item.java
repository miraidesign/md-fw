//------------------------------------------------------------------------
// @(#)Item.java
//                 
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.util.Hashtable;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.Crypt62;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.HashParameter;
import com.miraidesign.servlet.ServletLog;
import com.miraidesign.session.SessionObject;
import com.miraidesign.session.SessionManager;

import com.miraidesign.renderer.Page;
import com.miraidesign.renderer.Module;
import com.miraidesign.renderer.ItemInfo;

/** itemクラス(abstract) */
public abstract class Item implements ItemConstant {
    private static boolean debug = (SystemConst.debug && true);
    
    //------------------------------------------------------------------
    private static Hashtable<IntObject,String> nameHash = new Hashtable<IntObject,String>();
    static {
        // container group
        nameHash.put(new IntObject(-1),"unknown item");
        nameHash.put(new IntObject(CONTAINER),"CONTAINER");
        nameHash.put(new IntObject(FORM),"FORM");
        nameHash.put(new IntObject(LINK),"LINK");
        nameHash.put(new IntObject(HEADER),"HEADER");
        nameHash.put(new IntObject(BLOCK),"BLOCK");
        nameHash.put(new IntObject(FONT),"FONT");
        
        // display group
        nameHash.put(new IntObject(STRING),"STRING");
        nameHash.put(new IntObject(LINE_FEED),"LINE_FEED");
        nameHash.put(new IntObject(HAIR_LINE),"HAIR_LINE");
        nameHash.put(new IntObject(ANCHOR_STRING),"ANCHOR_STRING");
        nameHash.put(new IntObject(IMG),"IMG");
        nameHash.put(new IntObject(ANCHOR_IMG),"ANCHOR_IMG");
        
        // input group
        nameHash.put(new IntObject(TEXT),"TEXT");
        nameHash.put(new IntObject(HIDDEN),"HIDDEN");
        nameHash.put(new IntObject(PASSWORD),"PASSWORD");
        nameHash.put(new IntObject(CHECKBOX),"CHECKBOX");
        nameHash.put(new IntObject(RADIO),"RADIO");
        nameHash.put(new IntObject(SUBMIT),"SUBMIT");
        nameHash.put(new IntObject(RESET),"RESET");
        nameHash.put(new IntObject(BUTTON),"BUTTON");
        nameHash.put(new IntObject(IMAGE),"IMAGE");
        nameHash.put(new IntObject(FILE),"FILE");
        nameHash.put(new IntObject(TEXTAREA),"TEXTAREA");
        
        // button group
        nameHash.put(new IntObject(BTN_BUTTON),"BTN_BUTTON");
        nameHash.put(new IntObject(BTN_SUBMIT),"BTN_SUBMIT");
        nameHash.put(new IntObject(BTN_RESET),"BTN_RESET");
        
        // select group
        nameHash.put(new IntObject(LIST_BOX),"LIST_BOX");
        
        // multiple data group
        nameHash.put(new IntObject(DYNAMIC),"DYNAMIC");
        nameHash.put(new IntObject(TABLE),"TABLE");
        
        // non GUI group
        nameHash.put(new IntObject(QUEUE),"QUEUE");
        nameHash.put(new IntObject(QUEUE_TABLE),"QUEUE_TABLE");
        nameHash.put(new IntObject(ELEMENT),"ELEMENT");
        nameHash.put(new IntObject(PARAMETER),"PARAMETER");
        nameHash.put(new IntObject(HASH),"HASH");
        nameHash.put(new IntObject(LIST),"LIST");
        nameHash.put(new IntObject(EVENT),"EVENT");
    }

    /** アイテム型名を取得する */
    public synchronized String getTypeName() {
        IntObject o = IntObject.pop(type);
        String str =  (String)nameHash.get(o);
        if (str == null) {
            o.setValue(-1);
            //str =  (String)nameHash.get(o);
            str =  (String)nameHash.get(o)+"("+type+")";
        }
        IntObject.push(o);
        return str;
    }
    /** アイテムタイプに対応したアイテム型名を取得する */
    static public String getTypeName(int type) {
        IntObject o = IntObject.pop(type);
        String str =  (String)nameHash.get(o);
        if (str == null) {
            o.setValue(-1);
            //str =  (String)nameHash.get(o);
            str =  (String)nameHash.get(o)+"("+type+")";
        }
        IntObject.push(o);
        return str;
    }
    //---------------------------------------------------------------
    // ItemInfo 関連
    //---------------------------------------------------------------
    public String itemInfoKey;

    /** ItemInfoキー を設定する */
    public void setItemInfo(String key) {
        itemInfoKey = key;
    }
    /** ItemInfoを設定する */
    public void setItemInfo(ItemInfo info) {
        itemInfoKey = null;
        if (info != null) {
            CharArray ch = info.getKey();
            if (ch != null) itemInfoKey = ch.toString();
        }
    }
    
    /** PageServlet#init() の後に呼ばれる */
    public void setItemInfo() {
        ItemInfo info = getItemInfo();
        if (info != null) {
            info.setInfo(this);
        }
    }
    /** PageServletから呼ばれる <br>
        通常はユーザーログイン後に呼ばれる
    */
    public void setItemInfo(SessionObject session) {
        ItemInfo info = getItemInfo();
        if (info != null) {
            info.setInfo(getItemData(session));
        }
    }
    
    /** ItemInfo を取得する */
    public ItemInfo getItemInfo(SessionObject session) {
        if (itemInfoKey == null || itemInfoKey.length() == 0) return null;
        return session.getModuleManager().getItemInfo(itemInfoKey);
    }
    /** ItemInfo を取得する */
    public ItemInfo getItemInfo() {
        if (itemInfoKey == null || itemInfoKey.length() == 0 || page == null) return null;
        return page.getModule().getModuleManager().getItemInfo(itemInfoKey);
    }
    
    /** checkInfoの前に呼ばれる 必要に応じてオーバーライドする */
    public void checkStart(SessionObject session) {}
    
    /** アイテム内容可否チェックメソッド<br>
        通常ログイン時に呼ばれる。必要に応じてオーバーライドする 
       @param session セッション
       @param param   ItemInfoテーブルの１行のデータ
       @return true: 表示する
    */
    public boolean checkInfo(SessionObject session, HashParameter param) {
        return true;
    }

    /** checkInfoの後に呼ばれる 必要に応じてオーバーライドする */
    public void checkEnd(SessionObject session) {}
    
    //------------------------------------------------------------------
    public int type = -1;     // Item type  ( see ItemConstant)
    public void setType(int type) { this.type = type; }
    public int  getType() { return this.type; }
    public boolean isContainer() { return (type >= 0 && type < 20); }
    public boolean isDisplay()   { return (type >=20 && type < 40); }
    public boolean isInput()     { return (type >=40 && type < 60); }
    public boolean isButton()    { return (type >=60 && type < 80); }
    public boolean isSelect()    { return (type >=80 && type <100); }
    public boolean isDynamic()  { return (type >=100 && type <200); }
    public boolean isNonGUI()    { return (type >=500 && type <600); }
    
    static public boolean isContainer(int type) { return (type >= 0 && type < 20); }
    static public boolean isDisplay(int type)   { return (type >=20 && type < 40); }
    static public boolean isInput(int type)     { return (type >=40 && type < 60); }
    static public boolean isButton(int type)    { return (type >=60 && type < 80); }
    static public boolean isSelect(int type)    { return (type >=80 && type <100); }
    static public boolean isDynamic(int type)  { return (type >=100 && type <200); }
    static public boolean isNonGUI(int type)    { return (type >=500 && type <600); }
    
    //-----------------------------------------------------------------
    protected Page page; 
    /** 所属するPageオブジェクトを設定する
        @param page Pageオブジェクト */
    public void setPage(Page page) { 
        if (debug) {
            if (page == null) {
                System.out.println("Item:setPage page = null "+type);
                return;
            }
        }
        if (this.page != null && isCloneable()) {
            Module m1 = this.page.getModule();
            Module m2 = page.getModule();
            ServletLog.getInstance().out(m1.getName()+" page:"+this.page.getPageID()+"に登録されている "+
            getTypeName()+"を "+m2.getName()+" page:"+page.getPageID()+"に再登録しています") ;
            
        }
        this.page = page; 
        if (itemID <= 0 && page != null) setItemID(page.getItemID());
    }
    public Page getPage() { return page;}
    //-----------------------------------------------------------------
    protected Object userObject;    // ユーザー定義オブジェクト
    /** ユーザー定義オブジェクトを設定する
        @param userObject ユーザー定義オブジェクト
    **/
    public void setUserObject(Object userObject) { this.userObject = userObject; }
    /** ユーザー定義オブジェクトを取得する
        @return ユーザー定義オブジェクト
    **/
    public Object getUserObject() { return userObject; }
    //-----------------------------------------------------------------
    protected int itemID = 0;
    protected int mixedID;
    
    protected void setItemID(int itemID) {  // Containerの時はOverride
        this.itemID = itemID; 
    }
    public int getItemID() { return itemID; }
    public String getName() { return SystemConst.itemKey+itemID; } // name= で使用する文字列を返す
    
    //-----------------------------------------------------------------
    protected Item parentItem;
    public void setParent(Item item) { parentItem = item; }
    public Item getParent() { return parentItem; }
    //----------------------------------------------------------------
    /**  MixedID を返す
         (moduleID*1000000+pageID*1000+itemID)
        @return Mixed-ID
    */
    public int getMixedID() {
        if (debug) {
            if (page == null) {
                System.out.println("Item:getMixedID() page = null "+ type);
            }
            Module m = page.getModule();
            if (m == null) {
                System.out.println("Item:getMixedID() module = null "+ type);
            }
        }
        mixedID = ((page.getModule()).getModuleID() * 1000000 + 
                    page.getPageID() * 1000 + itemID);
        return mixedID;
    }
    
    /** 
        圧縮文字列可されたMixedIDを返す。<br>
        ただし、SystemConst.cryptMixedID が false の時は圧縮しない。
        @return Mixed-ID の圧縮文字列
    */
    public String encodeID() {
        int mid = getMixedID();
        String str = SystemConst.cryptMixedID ? Crypt62.encode(mid) : ""+mid;
        return str;
    }
    
    /** 
        MixedID圧縮文字列からMixedIDを取り出す。
        @param str 圧縮文字列
        @return Mixed-ID
    */
    public int decodeID(String str) {
        int i = 0;
        i = SystemConst.cryptMixedID ? (int)Crypt62.decode(str) : Integer.parseInt(str);
        return i;
    }
    
    //-----------------------------------------------------------------
    protected ItemData itemData;
    public ItemData getItemData() { return itemData; }

    /** ユーザーごとのItemDataを取り出す 
        @param sessionID セッションＩＤ
        @return ItemDataオブジェクト
    */
    public synchronized ItemData getItemData(int sessionID) {
        if (!cloneable) {
            return itemData;
        }
        ItemData id = SessionManager.getItemData(sessionID,getMixedID());
        if (id == null) {
            if (sessionID == 0) {
                //id = itemData;
            } else {
                if (!SessionManager.exist(sessionID)) {  // SessionManagerから remove されている
System.out.println("★★★★★★★★★★★★★★★★★★★★★★★★★");
System.out.println("getItemData: SessionManagerからremoveされています");
System.out.println("★★★★★★★★★★★★★★★★★★★★★★★★★");
                    //SessionManager.add(session);
                    //session.refreshItemData();
                }
            }
        }
        return id;
    }
    public synchronized ItemData getItemData(SessionObject session) {
        if (!cloneable || session==null) return itemData;
        ItemData id = SessionManager.getItemData(session,getMixedID());
        if (id == null) {
            int sessionID = session.getSessionID();
            if (sessionID == 0) {
                //id = itemData;
            } else {
                if (!SessionManager.exist(sessionID)) {  // SessionManagerから remove されている
System.out.println("☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆");
System.out.println("getItemData: SessionManagerに追加します");
System.out.println("☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆");
                    SessionManager.add(session);
                    id = SessionManager.getItemData(session,getMixedID());
                    //session.refreshItemData();
                    id.setSessionObject(session);
                }
            }
        }
        return id;
    }
    
    //-----------------------------------------------------------------
    public void setVisible(boolean mode) {
        itemData.setVisible(mode);
    }
    public void setVisible(boolean mode, int sessionID) {
        getItemData(sessionID).setVisible(mode);
    }
    public void setVisible(boolean mode, SessionObject session) {
        getItemData(session).setVisible(mode);
    }
    public boolean isVisible() {
        return itemData.isVisible();
    }
    public boolean isVisible(int sessionID) {
        return getItemData(sessionID).isVisible();
    }
    public boolean isVisible(SessionObject session) {
        return getItemData(session).isVisible();
    }
    // ----------------------------------------------------
    /** 直前のデータから変更されたか？ */
    public boolean changed() {
        return itemData.changed();
    }
    /** 直前のデータから変更されたか？ */
    public boolean changed(int sessionID) {
        return getItemData(sessionID).changed();
    }
    /** 直前のデータから変更されたか？ */
    public boolean changed(SessionObject session) {
        return getItemData(session).changed();
    }
    /** オリジナルのデータから変更されたか？ */
    public boolean updated() {
        return itemData.updated();
    }
    /** オリジナルのデータから変更されたか？ */
    public boolean updated(int sessionID) {
        return getItemData(sessionID).updated();
    }
    /** オリジナルのデータから変更されたか？ */
    public boolean updated(SessionObject session) {
        return getItemData(session).updated();
    }
    //--------------------------------------------
    // 自動コンバート関連
    //--------------------------------------------
    protected boolean langConvert = false;
    protected boolean colorConvert = false;  // あとで実装
    /** コンバートモードの設定 
        セットすると login の後にitemDataを@LANGコンバートする
    */
    public void setLangConvert(boolean mode) {
        langConvert = mode;
    }
    public void setColorConvert(boolean mode) {
        colorConvert = mode;
    }
    public boolean isLangConvert() { return langConvert;}
    public boolean isColorConvert() { return colorConvert;}
    
    /** 使用するItemでオーバーライドする <br>
        PageServlet から呼ばれる
    */
    public void convert(SessionObject session) {
        // do noting false;
    }
    
    //-----------------------------------------------------------------
    // constructor
    //-----------------------------------------------------------------
    
    private boolean cloneable = false;
    /** クローンを作っていいかどうかを設定する
        @param mode true:クローン作成可
    **/
    public void setCloneable(boolean mode) {
        cloneable = mode;
    }
    public boolean isCloneable() { return cloneable; }

    /** ユーザーテーブルの作成時に呼ぶ  
        @param session セッション
    */
    public abstract void copy(SessionObject session); 


    //-----------------------------------------------------------------

    //public abstract CharArray draw(CharArray ch, int sessionID);
    //public void draw(OutputStream out);
    //public void draw(OutputStream out, int sessionID);

    public CharArray draw(SessionObject session) {
        System.out.println("over ride して下さい "+getTypeName());
        return session.getBuffer();
    }

    //-----------------------------------------------------------------
    // ItemContainer copyで利用されている
    static public Item createItem(Item from) {
        Item item = null;
        int type = from.getType();
        switch (type) {
            case CONTAINER:
                item = new ItemContainer((ItemContainer)from);
                break;
            case FORM:
                item = new FormContainer((FormContainer)from);
                break;
       //   case LINK:
       //       item = new LinkContainer((LinkContainer)from);
       //       break;
       //   case BLOCK:
       //       item = new BlockContainer((BlockContainer)from);
       //       break;
            case STRING:
                item = new StringItem((StringItem)from);
                break;
        //  case LINE_FEED:
        //      item = new LineFeedItem((LineFeedItem)from);
        //      break;
        //  case HAIR_LINE:
        //      item = new HairLineItem((HairLineItem)from);
        //      break;
            case ANCHOR_STRING:
                item = new AnchorStringItem((AnchorStringItem)from);
                break;
            case TEXT:
                item = new TextItem((TextItem)from);
                break;
            case HIDDEN:
                item = new HiddenItem((HiddenItem)from);
                break;
            case PASSWORD:
                item = new PasswordItem((PasswordItem)from);
                break;
                
            case CHECKBOX:
                item = new CheckBoxItem((CheckBoxItem)from);
                break;
            case RADIO:
                item = new RadioButtonItem((RadioButtonItem)from);
                break;
            case SUBMIT:
                item = new SubmitItem((SubmitItem)from);
                break;
         // case FILE:
         //     item = new FileItem((FileItem)from);
         //     break;
            case TEXTAREA:
                item = new TextAreaItem((TextAreaItem)from);
                break;
                
                // RESET
                // BUTTON
                // IMAGE
                
            case LIST:
                item = new ListItem((ListItem)from);
                break;
            case LIST_BOX:
                item = new ListBoxItem((ListBoxItem)from);
                break;
            case DYNAMIC:
                item = new DynamicItem((DynamicItem)from);
                break;
                
            case QUEUE_TABLE:
                item = new QueueTableItem((QueueTableItem)from);
                break;
         // case ELEMENT:
         //     item = new ElementItem((ElementItem)from);
        //      break;
            case PARAMETER:
                item = new ParameterItem((ParameterItem)from);
                break;
            case HASH:
                item = new HashItem((HashItem)from);
                break;
        }
        return item;
    }
    /** アイテムに、DBのカラム名と一致するものを自動設定する。(サブクラスで実装する）
        @param param  DBから検索してきた１行
        @return 設定できればtrueを返す
    */
    public boolean setParameter(HashParameter param, int sessionID) {
        return false;
    }
    public boolean setParameter(HashParameter param, SessionObject session) {
        return false;
    }
}

//
//
// [end of Item.java]
//

