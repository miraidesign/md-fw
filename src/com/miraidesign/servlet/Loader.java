//------------------------------------------------------------------------
//    Loader.java
//                 
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------
//
package com.miraidesign.servlet;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashVector;
import com.miraidesign.session.SessionObject;
import com.miraidesign.data.DataAccessObject;

/**
 *  InitModule
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class Loader extends AbstractInitModule {
    private boolean debug = (false & SystemConst.debug);         // false
    private boolean debugLoad = (true & SystemConst.debug);         // 
    public void setDebug(boolean mode) {debug = mode; }

    public HashVector<String,InitModule> vector = new HashVector<String,InitModule>();
    
    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------
    public Loader() {
        debug &= SystemConst.debug;
        debugLoad &= SystemConst.debug;
    }
    public void load(HashVector<CharArray,CharArrayQueue> v) {
        if (debug) System.out.println("Loader.load start---");
        if (v != null) {
            for (int i = 0; i < v.size(); i++) {
                CharArray key   = (CharArray)v.keyElementAt(i);
                CharArrayQueue queue = (CharArrayQueue)v.valueElementAt(i);
                
                if (key.length() == 0) {
                    for (int j = 0; j < queue.size(); j++) {
                        CharArray data = queue.peek(j);
                        String className = data.trim().toString();
                        if (className.length() > 0) {
                            if (debug || debugLoad) System.out.println("Loader:"+className+" loading...");
                            try {
                                InitModule im = (InitModule)Class.forName(className).newInstance();
                                im.setModuleManager(getModuleManager());
                                vector.put(""+(j+1),im);
                            } catch (Exception ex) {
                                System.out.println("Loader: load Error :"+className);
                                ex.printStackTrace();
                            }
                        }
                    }
                } else {    // 
                    for (int j = 0; j < queue.size(); j++) {
                        CharArray data = queue.peek(j);
                        String className = data.trim().toString();
                        if (className.length() > 0) {
                            if (debug || debugLoad) System.out.println("Loader:"+key+"="+className+" loading...");
                            try {
                                InitModule im = (InitModule)Class.forName(className).newInstance();
                                im.setModuleManager(getModuleManager());
                                im.setKey(key);
                                vector.put(key.toString(),im);
                            } catch (Exception ex) {
                                System.out.println("Loader: load Error :"+className);
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        } else {
            if (debug) System.out.println("Loader: vector = null!! ");
        }
        if (debug) System.out.println("Loader.load end---");
    }
    //-------------------------------------------
    public InitModule getModule(String key) {
        return (InitModule)vector.get(key);
    }

    /** InitModuleのロード時に呼ばれます<br>
       必要に応じてオーバーライドします */
    public void load() {
        if (debug) System.out.println("Loader.load()");
        for (int i = 0; i < vector.size(); i++) {
            InitModule im = (InitModule)vector.elementAt(i);
            im.load();
        }
    }

    /** Servletのinit()時に呼ばれます<br>
       必要に応じてオーバーライドします */
    public void init() {
        for (int i = 0; i < vector.size(); i++) {
            InitModule im = (InitModule)vector.elementAt(i);
            im.init();
        }
    }
    /** Servletのdestroy()時に呼ばれます<br>
       ※登録順と逆順に呼ばれます<br>
       必要に応じてオーバーライドします */
    public void destroy() {
        for (int i = vector.size()-1; i >= 0; i--) {
            InitModule im = (InitModule)vector.elementAt(i);
            im.destroy();
        }
    }

    /** アクセスがあった時に呼ばれます。
        request の情報だけで処理続行可否を決定したいときに呼び出します。
        必要に応じてオーバーライドします。（FWのみ利用します）
        @return false のとき、以降の処理が中止されます
    */
    public boolean enter(HttpServletRequest request, HttpServletResponse response,
                         Hashtable<String,String[]> hashParameter,
                         int count,int site_ch) {
        boolean sts = true;
        for (int i = 0; i < vector.size(); i++) {
            InitModule im = (InitModule)vector.elementAt(i);
            sts = im.enter(request, response, hashParameter, count, site_ch);
            if (!sts) break;
        }
        return sts;
    }

    /** パラメータに異常があり、Moduleが呼び出せない時に呼び出されます<br>
        セッションタイムアウト時も呼ばれます<br>
        必要に応じてオーバーライドします。（FWのみ利用します）
        @return false のとき、以降の処理が中止されます
    */
    public boolean check(HttpServletRequest request, HttpServletResponse response,
                         Hashtable<String,String[]> hashParameter,
                         int count,int site_ch) {
        boolean sts = true;
        for (int i = 0; i < vector.size(); i++) {
            InitModule im = (InitModule)vector.elementAt(i);
            sts = im.check(request, response, hashParameter, count, site_ch);
            if (!sts) break;
        }
        return sts;
    }
    
    /** ユーザーアクセス直後のforward(session)の直前に呼ばれます<br>
        必要に応じてオーバーライドします */
    public void start(SessionObject session) {
        for (int i = 0; i < vector.size(); i++) {
            InitModule im = (InitModule)vector.elementAt(i);
            im.start(session);
        }
    }
    /** ユーザーアクセス直後のmakePage(session)の<br>
        直前に呼ばれます。必要に応じてオーバーライドします */
    public void makeStart(SessionObject session) {
        for (int i = 0; i < vector.size(); i++) {
            InitModule im = (InitModule)vector.elementAt(i);
            im.makeStart(session);
        }
    }

    /** ユーザーアクセス直後のdraw(session)の直前に呼ばれます<br>
        必要に応じてオーバーライドします */
    public void drawStart(SessionObject session) {
        for (int i = 0; i < vector.size(); i++) {
            InitModule im = (InitModule)vector.elementAt(i);
            im.drawStart(session);
        }
    }

    /** ユーザーアクセス直後のdraw(session)の直後に呼ばれます<br>
        必要に応じてオーバーライドします */
    public void drawEnd(SessionObject session) {
        for (int i = vector.size()-1; i >= 0; i--) {
            InitModule im = (InitModule)vector.elementAt(i);
            im.drawEnd(session);
        }
    }

    /** ユーザーアクセス直後のafterPage(session)()の直後に呼ばれます<br>
        必要に応じてオーバーライドします<br> 
        ※sessionはafterPageによって削除されている場合があります
    */
    public void end(SessionObject session) {
        for (int i = vector.size()-1; i >= 0; i--) {
            InitModule im = (InitModule)vector.elementAt(i);
            im.end(session);
        }
    }

    /**
        ログイン判定の直後に呼ばれます
        @param session ログインユーザーのセッション
        @param dao     ログインで使用したDAO(SQL,Parameter,QueueTableが取得可能)
        @param result  ログイン判定
        @return  ログイン判定をオーバーライドする(通常はresultをそのまま返す)
    */
    public boolean login(SessionObject session, DataAccessObject dao, boolean result) {
        for (int i = 0; i < vector.size(); i++) {
            InitModule im = (InitModule)vector.elementAt(i);
            result = im.login(session, dao, result);
        }
        return result;
    }

    /**
        ユーザー定義メソッド
        @param session セッション
        @param object  ユーザー定義オブジェクト
        @return  ユーザー定義の戻り値
    */
    public Object execute(SessionObject session, Object object) {
        Object obj = null;
        for (int i = 0; i < vector.size(); i++) {
            InitModule im = (InitModule)vector.elementAt(i);
            obj = im.execute(session, object);
        }
        return obj;
    }

}

//
// [end of Loader.java]
//

