//------------------------------------------------------------------------
//    NaviNode.java
//          Navigationノード情報
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//  注：スレッドセーフではない（ルートノード等で同期させる事）
//------------------------------------------------------------------------


package com.miraidesign.system;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import com.miraidesign.common.SystemConst;
import com.miraidesign.data.DataAccessObject;
import com.miraidesign.renderer.item.AnchorStringItem;
import com.miraidesign.renderer.item.AnchorStringData;
import com.miraidesign.session.SessionObject;
import com.miraidesign.servlet.ModuleServlet;
import com.miraidesign.servlet.PageServlet;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.ObjectQueue;

/**
    ナビゲーション用ノード情報
**/

public class NaviNode extends DefaultMutableTreeNode {
    static private boolean debugList = true;
    static private boolean debugDAO = false;
    
    // ModuleManagerにもコピーしておく
    static public final int TEXT     = 0;    // テキストで表示
    static public final int DAO      = 1;    // DAOがあればアンカーで表示
    static public final int ANCHOR   = 2;    // アンカーで表示（デフォルト）
    
    private int mode = ANCHOR;
    
    private static String[] strs = {"TEXT","DAO","ANCHOR"};
    
    private PageServlet page;
    private AnchorStringItem anchor;
    private int id = 0;
    
    // for debug ---------------------
    static ObjectQueue queue;
    static {
        if (debugList) queue = new ObjectQueue();
    }
    static public void debugList() {
        if (debugList) {
            System.out.println("==========================================");
            for (int i = 0; i < queue.size(); i++) {
                NaviNode node = (NaviNode)queue.peek(i);
                if (node.getParent() == null) {
                    debugShow(node,"");
                }
            }
            System.out.println("==========================================");
        }
    }
    static public void debugShow(NaviNode node,String indent) {
        System.out.println(indent+"["+node.getID()+"] "+node.getTitle()+
            "("+strs[node.mode]+")"+
            node.page.getModule().getName()+":"+node.page.getPageID());
        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            NaviNode child = (NaviNode)node.getChildAt(i);
            debugShow(child,indent+"    ");
        }
    }
    //-----
    public NaviNode() { 
        super();
        super.setUserObject(this);
        if (debugList) queue.enqueue(this);
    }
    public NaviNode(PageServlet page) { 
        super();
        super.setUserObject(this);
        this.page = page;
        if (page != null) {
            ModuleManager mm = page.getModule().getModuleManager();
            id = ++mm.naviID;
            
            anchor = new AnchorStringItem(SystemConst.nodeKey+"="+id,"");
            anchor.setVisible(false);
            anchor.setJumpPage(page);
            page.addItem(anchor);
            
        }
        if (debugList) queue.enqueue(this);
    }
    public NaviNode(PageServlet page, CharArray title) { 
        super();
        super.setUserObject(this);
        this.page = page;
        if (page != null) {
            ModuleManager mm = page.getModule().getModuleManager();
            id = ++mm.naviID;
            
            anchor = new AnchorStringItem(SystemConst.nodeKey+"="+id,"");
            anchor.setVisible(false);
            anchor.setJumpPage(page);
            page.addItem(anchor);
            setTitle(title);
        }
        if (debugList) queue.enqueue(this);
    }
    public NaviNode(PageServlet page ,boolean allowsChildren) { 
        super();
        super.setAllowsChildren(allowsChildren);
        super.setUserObject(this);
        this.page = page;
        if (page != null) {
            ModuleManager mm = page.getModule().getModuleManager();
            id = ++mm.naviID;
            
            anchor = new AnchorStringItem(SystemConst.nodeKey+"="+id,"");
            anchor.setVisible(false);
            anchor.setJumpPage(page);
            page.addItem(anchor);
            
        }
        if (debugList) queue.enqueue(this);
    }
    // setter
    public void setPageServlet(PageServlet page) { this.page = page; }
    public void setTitle(CharArray title) {anchor.setText(title);}
    public void setTitle(String title) {anchor.setText(title);}
    public void setMode(int mode) { this.mode = mode;}
    public void setID(int id) { this.id = id;}
    
    // getter
    public PageServlet getPageServlet() { return page; }
    public AnchorStringItem getAnchor() { return anchor;}
    public CharArray getTitle() { return anchor.getText();}
    public int getMode() { return mode;}

    protected DataAccessObject checkDAO(SessionObject session) {
        return checkDAO(session, false);
    }
    protected DataAccessObject checkDAO(SessionObject session, boolean debugDisp) {
        DataAccessObject dao = session.checkDAO(page);
        if (dao == null) {
            if (debugDisp) {
                System.out.println("NaviNode#checkDAO "+page.getModule().getName()+":"+page.getPageID());
            }
            NaviNode parent = (NaviNode)getParent();
            if (parent != null) dao = parent.checkDAO(session);
        }
        return dao;
    }
    public CharArray getTag(SessionObject session) {
        CharArray ch = null;
        CharArray msg = ((ModuleServlet)page.getModule()).getMessage(
                          session, anchor.getText().toString());
        switch (mode) {
            case TEXT: 
                ch = (msg != null && msg.length() > 0) ? msg : anchor.getText();
                
                break;
            case DAO:
                if (checkDAO(session) == null) {
                    ch = (msg != null && msg.length() > 0) ? msg :anchor.getText();
                    if (debugDAO) {
                        checkDAO(session, true);
                    }
                }
                // break してはいけない
            case ANCHOR:
            default:
                if (ch == null) {
                    AnchorStringData asd = (AnchorStringData)anchor.getItemData(session);
                    if (msg != null && msg.length() > 0) asd.setText(msg);
                    asd.setURL(SystemConst.nodeKey+"="+id);
                    String s = page.getNodeParameter();
                    if (s.length() > 0) asd.addURL("&"+s);
                    ch = asd.getTag();
                }
                break;
        }
        return ch;
    }
    public int getID() { return id;}
    //---------------------------------------------
    /** このノードの子のリストを返します */
//  public Enumeration children() {
//      return super.children();
//  }
//  /** 子の数を返す */
//  public int getChildCount() { return super.getChildCount(); }
//  /** ルートまでの距離を返します */
//  public int getLevel() { return super.getLevel(); }
//  //---------------------------------------------
//  /** ルートからこのノードまでのオブジェクトパスを返します */
//  public Object[] getUserObjectPath() {
//      return super.getUserObjectPath();
//  }
//
    /** このノードの子配列にある、指定されたインデックスの子を返します */
    public NaviNode getChildNodeAt(int index) {
        return (NaviNode)super.getChildAt(index);
    }
    
    public NaviNode copy(NaviNode from) {
        return copy(from, 0, 0);
    }
    public NaviNode copy(NaviNode from, int n1, int n2) {
//System.out.println("●Node copy:"+n1+"/"+n2+":"+getTitle()+"<-"+from.getTitle());
        mode = from.mode;
        page = from.page;
        anchor = new AnchorStringItem(from.anchor);
        int count  = from.getChildCount();
        if (getChildCount() == 0) { // 自分に子供がない時
            n1++;
            for (int i = 0; i < count; i++) {
                NaviNode org = from.getChildNodeAt(i);
                NaviNode node = new NaviNode(org.page,org.page.getTitleName());
                add(node);
                node.copy(org, n1, count+1);
                org.page.getNodeQueue().enqueue(node);
            }
        }
        return this;
    }
}

//
// [end of NaviNode.java]
//

