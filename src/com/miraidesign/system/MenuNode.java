//------------------------------------------------------------------------
//    MenuNode.java
//          メニューノード情報
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//  注：スレッドセーフではない（ルートノード等で同期させる事）
//------------------------------------------------------------------------
package com.miraidesign.system;

import javax.swing.tree.DefaultMutableTreeNode;
import com.miraidesign.util.CharArray;

/**
    メニューシステム用ノード情報
**/

public class MenuNode extends DefaultMutableTreeNode {
    private CharArray title = new CharArray();      // リンク名称
    private CharArray module_name = new CharArray();     // モジュール名
    private int page_no = -1;  // ページ番号 (-1) でデフォルト
    private CharArray width = new CharArray();      // 表示幅情報
    
    private CharArray color = new CharArray();      // 文字色
    private CharArray bgcolor = new CharArray();    // 背景色

    private CharArray color2   = new CharArray();    // 文字色２
    private CharArray bgcolor2 = new CharArray();    // 背景色２
    
    private CharArray blankcolor = new CharArray();    // ブランクの色
    
    private CharArray parameter = new CharArray();      // 追加パラメータ
    //-----
    public MenuNode() { 
        super();
        super.setUserObject(this); 
    }
    public MenuNode(String title) { 
        super();
        super.setUserObject(this);
        setTitle(title);
    }
    public MenuNode(String title ,boolean allowsChildren) { 
        super();
        super.setAllowsChildren(allowsChildren);
        super.setUserObject(this);
        setTitle(title);
    }
    // setter
    public void setTitle(String str) { title.set(str).trim(); }
    public void setTitle(CharArray str) { title.set(str).trim(); }
    public void setWidth(String str) { width.set(str).trim(); }
    public void setWidth(CharArray str) { width.set(str).trim(); }
    public void setBgColor(String str) { bgcolor.set(str).trim(); }
    public void setBgColor(CharArray str) { bgcolor.set(str).trim(); }
    public void setColor(String str) { color.set(str).trim(); }
    public void setColor(CharArray str) { color.set(str).trim(); }
    public void setBgColor2(String str) { bgcolor2.set(str).trim(); }
    public void setBgColor2(CharArray str) { bgcolor2.set(str).trim(); }
    public void setColor2(String str) { color2.set(str).trim(); }
    public void setColor2(CharArray str) { color2.set(str).trim(); }
    public void setBlankColor(String str) { blankcolor.set(str).trim(); }
    public void setBlankColor(CharArray str) { blankcolor.set(str).trim(); }
    
    
    public void setModuleName(String str) { module_name.set(str).trim(); }
    public void setModuleName(CharArray str) { module_name.set(str).trim(); }
    public void setPageNo(int no) { this.page_no = no; }
    public void setParameter(String str) { parameter.set(str).trim(); }
    public void setParameter(CharArray str) { parameter.set(str).trim(); }
    
    // getter
    public CharArray getTitle() { return title; }
    public CharArray getWidth() { return width; }
    public CharArray getBgColor() { return bgcolor; }
    public CharArray getColor() { return color; }
    public CharArray getBgColor2() { return bgcolor2; }
    public CharArray getColor2() { return color2; }
    public CharArray getBlankColor() { return blankcolor; }
    
    public CharArray getModuleName() { return module_name; }
    public int getPageNo() { return page_no;}
    public CharArray getParameter() { return parameter; }
    
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
    public MenuNode getChildNodeAt(int index) {
        return (MenuNode)super.getChildAt(index);
    }
    
}

//
//
// [end of MenuNode.java]
//

