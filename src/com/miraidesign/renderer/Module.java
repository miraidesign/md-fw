//------------------------------------------------------------------------
//    Module.java
//                 
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
package com.miraidesign.renderer;

import java.util.Hashtable;
import java.util.Enumeration;

import com.miraidesign.util.HashVector;
import com.miraidesign.util.IntObject;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.session.SessionObject;

/**
 *  Module Pageオブジェクトはここから取得します。
 *  
 *  @version 0.5 2010-04-07
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class Module {
    private int moduleID;

    protected ModuleManager moduleManager;
    public void setModuleManager(ModuleManager m) {
        moduleManager = m;
    }
    public ModuleManager getModuleManager() {
        return moduleManager;
    }
    
    private String name;         // モジュール名
    /** モジュール名(キーワード）を取得する */
    public String getName() { return name;}
    /** モジュール名(キーワード）を設定する */
    public void setName(String name) { this.name = name;}
    
    private HashVector<IntObject,Page> hashPage;    // Pageを保管する
    private Hashtable<String,Page>     hashPageKey;  // 
    
    private String servletPath ="";  // 

    /** サーブレットパスの設定 （試験用）**/
    public void setServletPath(String str) {
        this.servletPath = str;
    }
    public String getServletPath() {
        return servletPath;
    }
    
    /** Moduleを生成する **/
    public Module() {
        hashPage = new HashVector<IntObject,Page>();
        hashPageKey = new Hashtable<String,Page>();
    }
    /** Moduleを生成する 
        @param moduleID モジュールＩＤ 
        @param name モジュール名
    **/
    public Module(int moduleID, String name) {
        set(moduleID, name);
        hashPage = new HashVector<IntObject,Page>();
        hashPageKey = new Hashtable<String,Page>();
    }
//  /** Moduleを生成する 
//      @param moduleID モジュールＩＤ 
//      @param nama モジュール名
//      @param path サーブレットパス
//  **/
//  public Module(int moduleID,String name, String path) {
//      this.moduleID = moduleID;
//      this.name = name;
//      this.servletPath = path;
//      hashPage = new HashVector();
//  }
    
    public void set(int moduleID,String name) {
        this.moduleID = moduleID;
        this.name = name;
    }
    
    /** Page を追加する
        @param page 追加するPageオブジェクト */
    public void add(Page page) {
        page.setModule(this);
        hashPage.put(new IntObject(page.getPageID()),page);
    }
    
    /**
        Page-ID よりPageオブジェクトを取得する
        @param id Page-ID
        @return Pageオブジェクト
    */
    public Page get(int id) {
        IntObject o = IntObject.pop(id);
        Page page = (Page)hashPage.get(o);
        IntObject.push(o);
        return page;
    }
    /**
        Page-ID よりPageオブジェクトを取得する
        @param id Page-ID
        @return Pageオブジェクト
    */
    public Page getPage(int id) {
        return get(id);
    }
    /**
        Page-Name(Keyword) よりPageオブジェクトを取得する
        @param name ページ名
        @return Pageオブジェクト
    */
    public Page get(String name) {
        Page page = hashPageKey.get(name);
        return page;
    }
    /**
        Page-Name(Keyword) よりPageオブジェクトを取得する
        @param name ページ名
        @return Pageオブジェクト
    */
    public Page getPage(String name) {
        return get(name);
    }
    
    /**
        Page-Name(Keyword)を設定する
    */
    protected void setPageName(String name, Page page) {
        if (name != null && name.length() > 0) {
            Page org = get(name);
            if (org == null) {
                hashPageKey.put(name, page);
            } else if (org != page) {
                System.out.println("Module["+this.name+"]PageKey("+name+")の２重登録!");
            }
        }
    }
    
    /** モジュールＩＤを取得する
        @return モジュールＩＤ
    */
    public int getModuleID() { return moduleID; }
    
    /** ページのリストを取得する
        @return ページのEnumeration（順不動）
    */
    public Enumeration getPageList() { return hashPage.elements(); }
    public Enumeration getPageKeys() { return hashPage.keys(); }
    public HashVector<IntObject,Page> getPageTable() { return hashPage; }
    public void init() {};
    public void destroy() {};
    
    /** ItemInfo 情報をセットします**/
    public void setItemInfo() {
        for (Enumeration e = getPageList(); e.hasMoreElements();) {
            Page page = (Page)e.nextElement();
            try {
                page.setItemInfo();
            } catch (Exception ex) {
                System.out.println("●"+getName()+"."+page.getPageID()+" のsetItemInfoでエラーが発しています。");
                ex.printStackTrace();
            }
        }
    }
    /** ItemInfo 情報をセットします**/
    public void setItemInfo(SessionObject session) {
        for (Enumeration e = getPageList(); e.hasMoreElements();) {
            Page page = (Page)e.nextElement();
            try {
                page.setItemInfo(session);
            } catch (Exception ex) {
                System.out.println("●"+getName()+"."+page.getPageID()+" のsetItemInfoでエラーが発生しています。");
                ex.printStackTrace();
            }
        }
    }
    /** convert**/
    public void convert(SessionObject session) {
        for (Enumeration e = getPageList(); e.hasMoreElements();) {
            Page page = (Page)e.nextElement();
            try {
                page.convert(session);
            } catch (Exception ex) {
                System.out.println("●"+getName()+"."+page.getPageID()+" のconvertでエラーが発生しています。");
                ex.printStackTrace();
            }
        }
    }
    /** reset */
    public void reset(SessionObject session) {
        for (Enumeration e = getPageList(); e.hasMoreElements();) {
            Page page = (Page)e.nextElement();
            try {
                page.reset(session);
            } catch (Exception ex) {
                System.out.println("●"+getName()+"."+page.getPageID()+" のresetsでエラーが発生しています。");
                ex.printStackTrace();
            }
        }
    }
    
    
}

//
// [end of Module.java]
//

