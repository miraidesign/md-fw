//------------------------------------------------------------------------
//    AbstractInitModule.java
//         Loader によってロードされる初期化モジュールのabstractクラスです
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------
// 

package com.miraidesign.servlet;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.miraidesign.system.ModuleManager;
import com.miraidesign.session.SessionObject;
import com.miraidesign.data.DataAccessObject;
import com.miraidesign.util.CharArray;

/**
 *  AbstractInitModule 
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public abstract class AbstractInitModule implements InitModule
{
    static public boolean debug = false;
    protected ModuleManager moduleManager = null;
    protected CharArray key = new CharArray();
    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------
    public AbstractInitModule() {}
    
    public void setModuleManager(ModuleManager mm) {
        this.moduleManager = mm;
    }
    
    /** ModuleManagerを取得します<br>
        ccm.iniでロードされた場合はnullを返します */
    public ModuleManager getModuleManager() {
        return moduleManager;
    }
    
    //---------------------------------------------------------
    // key
    //---------------------------------------------------------
    public void setKey(CharArray key) { 
        this.key.set(key);
    }
    public CharArray getKey() {return key;}
    
    //---------------------------------------------------------
    // オーバーライドするメソッド
    //---------------------------------------------------------
    /** InitModuleのロード後に呼ばれます<br>
       必要に応じてオーバーライドします */
    public void load() {}
    
    /** Servletのinit()時に呼ばれます<br>
       必要に応じてオーバーライドします */
    public void init() {}
    
    /** Servletのdestroy()時に呼ばれます<br>
       ※登録順と逆順に呼ばれます<br>
       必要に応じてオーバーライドします */
    public void destroy() {}
    
    /** アクセスがあった時に呼ばれます。
        request の情報だけで処理続行可否を決定したいときに呼び出します。
        必要に応じてオーバーライドします。（FWのみ利用します）
        @return false のとき、以降の処理が中止されます
    */
    public boolean enter(HttpServletRequest request, HttpServletResponse response,
                         Hashtable<String,String[]> hashParameter,
                         int count,int site_ch) {
        return true;
    }
    
    /** パラメータに異常があり、Moduleが呼び出せない時に呼び出されます<br>
        セッションタイムアウト時も呼ばれます<br>
        必要に応じてオーバーライドします。（FWのみ利用します）
        @return false のとき、以降の処理が中止されます
    */
    public boolean check(HttpServletRequest request, HttpServletResponse response,
                         Hashtable<String,String[]> hashParameter,
                         int count,int site_ch) {
        if (debug) System.out.println("AbstractInitModule:check() do nothing");
        
        return true;
    }

    /** ユーザーアクセス直後のforward(session)の直前に呼ばれます<br>
        必要に応じてオーバーライドします */
    public void start(SessionObject session) {}

    /** ユーザーアクセス直後のmakePage(session)の直前に呼ばれます<br>
        必要に応じてオーバーライドします */
    public void makeStart(SessionObject session) {}
    
    /** ユーザーアクセス直後のdraw(session)の直前に呼ばれます<br>
        必要に応じてオーバーライドします */
    public void drawStart(SessionObject session) {}

    /** ユーザーアクセス直後のdraw(session)の直後に呼ばれます<br>
        必要に応じてオーバーライドします */
    public void drawEnd(SessionObject session) {}

    /** ユーザーアクセス直後のafterPage(session)()の直後に呼ばれます<br>
        必要に応じてオーバーライドします<br> 
        ※sessionはafterPageによって削除されている場合があります
    */
    public void end(SessionObject session) {}

    /**
        ログイン判定の直後に呼ばれます
        @param session ログインユーザーのセッション
        @param dao     ログインで使用したDAO(SQL,Parameter,QueueTableが取得可能)
        @param result  ログイン判定
        @return  ログイン判定をオーバーライドする(通常はresultをそのまま返す)
    */
    public boolean login(SessionObject session, DataAccessObject dao, boolean result) {
        return result;
    }

    /**
        ユーザー定義メソッド
        @param session セッション
        @param object  ユーザー定義オブジェクト
        @return  ユーザー定義の戻り値
    */
    public Object execute(SessionObject session, Object object) {
        return null;
    }
}

//
// [end of AbstractInitModule.java]
//

