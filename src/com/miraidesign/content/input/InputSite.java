//------------------------------------------------------------------------
//    InputSite.java
//          システム関数  @site で呼ばれる
//              Copyright (c) MiraiDesign 20106 All Rights Reserved. 
//------------------------------------------------------------------------
// メソッド等再調整する
//------------------------------------------------------------------------

package com.miraidesign.content.input;

import com.miraidesign.session.SessionObject;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.system.SiteMapping;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.Parameter;

/**
    サイト関数<br>
    作成中
*/
public class InputSite extends InputItem /**implements SessionEvent**/ {
    private static boolean debug = false;

    private int site_id = -1;
    public void setSiteID(int id) { site_id = id; }
    public int getSiteID() { return site_id;}

    //-------------------
    // constructor
    //-------------------
    public InputSite() {
        type.set("SITE");
    }
    
    //-------------------
    // method
    //-------------------
    public void copy(InputSite from) {
    }
    
    /** デフォルト関数 */
    public CharArray get() {
        return new CharArray();
    }
    
    public void set(CharArray ch) {
        
    }
    
    /* OrgIDを取得する */
    public int getOrgID(SessionObject session) { 
        int id = 0;
        do {
            if (session == null) break;
            HashParameter hash = session.getUserData();
            if (hash != null) id = hash.getInt("org_id");
        } while (false);
        return id; 
    }
    /* サイトIDを取得する */
    public int getID(SessionObject session) { 
        int id = 0;
        do {
            if (session == null) break;
            HashParameter hash = session.getUserData();
            if (hash != null) id = hash.getInt("site_id");
        } while (false);
        return id; 
    }
    
    /* サイト名を取得する */
    public CharArray getName(SessionObject session) { 
        CharArray ch = null;
        do {
            if (session == null) break;
            HashParameter hash = session.getUserData();
            if (hash != null) ch = hash.getCharArray("site_name");
        } while (false);
        return ch; 
    }
    
    /* サイトキーワードを取得する */
    public CharArray getKey(SessionObject session) { 
        CharArray ch = null;
        do {
            if (session == null) break;
            HashParameter hash = session.getUserData();
            if (hash != null) ch = hash.getCharArray("site_key");
        } while (false);
        return ch; 
    }
    
    /* サイトディレクトリを取得する */
    public CharArray getDir(SessionObject session) { 
        CharArray ch = null;
        do {
            if (session == null) break;
            HashParameter hash = session.getUserData();
            if (hash != null) ch = hash.getCharArray("site_url");
        } while (false);
        return ch; 
    }
    
    /* サイトURLを取得する 
       http://servername/fw-site/site_path/enter 
    */
    public CharArray getURL(SessionObject session) { 
        CharArray ch = null;
        do {
            CharArray dir = getDir(session);
            ModuleManager mm = session.getModuleManager(); if (mm == null) break;
            SiteMapping map = mm.getSiteMapping(); if (map == null) break;
            //int org_id = getOrgID(session); if (org_id <= 0) break;
            //try {
            //    MobileSnsManager snsMgr = MobileSnsManager.getInstance(
            //        Util.format0(session.getSiteChannelCode(),5));
            //    if (snsMgr == null) break;
            //} catch (Error er) {
            //    er.printStackTrace();
            //    break;
            //} catch (Exception ex) {
            //    ex.printStackTrace();
            //    break;
            //}
            
            ch = new CharArray();
            ch.add(SystemManager.httpURL);
            
            map.getSiteParameter(ch);
            //ch.add(snsMgr.getSiteUrlPath(org_id));
            if (!ch.endsWith("/")) ch.add("/");
            if (dir == null || dir.trim().length() == 0) break;
            ch.add(dir);
            if (!ch.endsWith("/")) ch.add("/");
            //ch.add("enter");    // ログインURL
            
        } while (false);
        return ch; 
    }
    
    /* サイトパス情報を取得する<br>
       /fw-site/site_path/  */
    public CharArray getPath(SessionObject session) { 
        CharArray ch = null;
        do {
            CharArray dir = getDir(session);
            ModuleManager mm = session.getModuleManager(); if (mm == null) break;
            SiteMapping map = mm.getSiteMapping(); if (map == null) break;
            //int org_id = getOrgID(session); if (org_id <= 0) break;
            //try {
            //    MobileSnsManager snsMgr = MobileSnsManager.getInstance(
            //        Util.format0(session.getSiteChannelCode(),5));
            //    if (snsMgr == null) break;
            //} catch (Error er) {
            //    er.printStackTrace();
            //    break;
            //} catch (Exception ex) {
            //    ex.printStackTrace();
            //    break;
            //}
            ch = new CharArray();
            map.getSiteParameter(ch);
            //ch.add(snsMgr.getSiteUrlPath(org_id));
            if (!ch.endsWith("/")) ch.add("/");
            if (dir == null || dir.trim().length() == 0) break;
            ch.add(dir);
            if (!ch.endsWith("/")) ch.add("/");
        } while (false);
        return ch; 
    }
    
    
    //////////////////////////////////////////////////////////////////////////////
    // 関数呼び出し元
    //////////////////////////////////////////////////////////////////////////////
    
    /*
        関数呼び出し
        @param func 関数名 
        @param param 関数パラメータ
    */
    public CharArray getFunc(CharArray func, CharArrayQueue param) {
        return getFunc(func, param, 0);
    }
    public CharArray getFunc(CharArray func, CharArrayQueue param, int state) {
if (debug) System.out.println("▽▽InputSite#getFunc▽▽");
        CharArray ch = null;
        if (func == null) {
            ch = get();
        } else {
            func.trim().toLowerCase();
            if (func.length() == 0)              ch = get();
            //else if (func.equals("link"))        ch = getLink(param);
            //else if (func.equals("title"))       ch = getTitle(param);
            //else if (func.equals("description")) ch = getDescription(param);
            //else if (func.equals("disp"))        ch = getDisp(param,null);
            //else if (func.equals("view"))        ch = getDisp(param,null);
            else                                 ch = get();
        }
if (debug) System.out.println("△△InputSite#getFunc△△");
        return ch;
    }
    /*
        関数呼び出し（CMSが利用するのはこちらのみ）
        @param func  関数名 
        @param param 関数パラメータ
        @param p     戻り値用
        @param session
    */
    public Parameter getParameter(CharArray func, CharArrayQueue param, 
                                    Parameter p,SessionObject session) {
if (debug) System.out.println("▽▽InputSite#getParameter▽▽");
        if (func == null) {
            p.add(get());
        } else {
            func.trim().toLowerCase();
            if (func.length() == 0)             p.add(get());
            else if (func.equals("id")) { 
                int _id = site_id;
                if (_id < 0) _id = getID(session);
                p.add((_id <= 0) ? "" :""+_id);
            }
            else if (func.equals("name"))       p.add(getName(session));
            else if (func.equals("org_id")) {
                int _id = getOrgID(session);
                p.add((_id <= 0) ? "" :""+_id);
            }
            else if (func.equals("key"))        p.add(getKey(session));
            else if (func.equals("dir"))        p.add(getDir(session));
            else if (func.equals("url"))        p.add(getURL(session));
            else if (func.equals("path"))       p.add(getPath(session));
            
            //else if (func.equals("height"))     p.add(getHeight(param,session));
            //else if (func.equals("view"))       p.add(getView());
            else   p.add("");
        }
if (debug) System.out.println("△△InputSite#getParameter△△");
        return p;
    }

    /**
        関数設定
        @param func  関数名 
        @param param 関数パラメータ
        @return true 設定成功
    */
    public boolean setFunc(CharArray func, CharArrayQueue param) {
        boolean sts = true;
        /*
        if (func != null && param != null && param.size() > 1) {
            CharArray ch = param.peek();
            func.trim().toLowerCase();
            if (func.length() == 0)            setSrc(ch);
            else if (func.equals("main"))      setSrc(ch);
            else                               sts = false;
        } else sts = false;
        */
        return sts;
    }

    /**
        関数設定
        @param func  関数名 
        @param ch 関数パラメータ
        @return true 設定成功
    */
    public boolean setFunc(CharArray func, CharArray ch) {
        boolean sts = true;
        /*
        if (func != null) {
            func.trim().toLowerCase();
            if (func.length() == 0)            setSrc(ch);
            else if (func.equals("main"))      setSrc(ch);
            else                               sts = false;
        } else sts = false;
        */
        return sts;
    }


}

//
// [end of InputSite.java]
//

