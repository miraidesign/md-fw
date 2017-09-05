//------------------------------------------------------------------------
//    ContentsManager.java
//          コンテンツマネージャ
//              Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import java.util.Hashtable;

import com.miraidesign.content.input.InputContent;
//import com.miraidesign.image.EmojiConverter;
import com.miraidesign.servlet.ModuleServlet;
import com.miraidesign.servlet.PageServlet;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayFile;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.IntObject;

/**
    コンテンツマネージャ<br>
    InputContent を管理する
*/
public class ContentsManager {
    private static boolean debug = false;
    private static boolean debug2 = false;
    private static String defaultKey = "BGR3";
    private static String defaultContentsModuleKey = "ContentsManagerModule";   // debug
    private static String defaultContentsPageKey   = "PREVIEW";
    
    /* ContentsManagerのデフォルトキーワードを取得する */
    public static String getDefaultKey() { return defaultKey;}
    
    /* ContentsManagerのデフォルトキーワードを設定する */
    public static void setDefaultKey(String key) { defaultKey = key; }
    
    private static Hashtable<String, ContentsManager> hashManager = 
               new Hashtable<String, ContentsManager>();
    

    /* デフォルトの ContentsManagerを取得する */
    public static ContentsManager getInstance() {
        return getInstance(defaultKey);
    }
    
    /* 指定キーワードの ContentsManagerを取得する */
    public static ContentsManager getInstance(String key) {
        ContentsManager mgr = null;
        mgr = hashManager.get(key);
        if (mgr == null) {
            mgr = new ContentsManager();
            hashManager.put(key, mgr);
        }
        return mgr;
    }
    
    //-------- static end ------
    
    ModuleServlet contentsModule = null;
    PageServlet   contentsPage   = null;
    
    /* コンテンツ表示モジュール（ページ）を取得する */  //使用していないはず
    public ModuleServlet getContentsModule(SessionObject session) {
        if (contentsModule == null && session != null) {
            ModuleManager mm =  session.getModuleManager();
            if (mm != null) {
                contentsModule = (ModuleServlet)mm.getModule("ContentsManager");
            }
        }
        return contentsModule;
    }
    public PageServlet getContentsPage(SessionObject session) {
        if (contentsPage == null) {
            ModuleServlet module = getContentsModule(session);
            contentsPage = module.getDefaultPage();
        }
        return contentsPage;
    }
    //--------------------------
    
    private boolean ref_mode = false;        // 参照モード
    private int ref_id = 0;                  // 参照ID
    
    /* 参照モードを設定する [false] */
    public void setReferenceMode(boolean mode) {
        ref_mode = mode;
    }
    /* 参照モードを取得する [false] */
    public boolean getReferenceMode() {
        return ref_mode;
    }
    /* 参照IDを設定する [0] */
    public void setReferenceID(int id) {
        ref_id = id;
    }
    /* 参照モードを取得する [false] */
    public int getReferenceID() {
        return ref_id;
    }
    
    ModuleManager moduleManager = null;
    public void setModuleManager(ModuleManager mm) { 
        moduleManager = mm;
        if (mm != null) {
            CharArray ch = mm.ini.get(mm.getSection("Content"), "ReferenceMode");
            if (ch != null && ch.trim().length() > 0) {
                ref_mode = ch.getBoolean();
            }
if (debug) System.out.println("★ReferenceMode:"+ch+":"+ref_mode);
            ch = mm.ini.get(mm.getSection("Content"), "ReferenceID");
            if (ch != null && ch.trim().length() > 0) {
                ref_id = ch.getInt();
            }
if (debug) System.out.println("★ReferenceID:"+ch+":"+ref_id);
        }
    }
    public ModuleManager getModuleManager() { return moduleManager;}
    
    //--------------------------
    private  HashVector<IntObject,InputContent> hashID  = 
         new HashVector<IntObject,InputContent>();
    private  HashVector<CharArray,InputContent> hashKey = // key = key+"$$$"+oid
         new HashVector<CharArray,InputContent>();
    private  HashVector<CharArray,InputContent> hashURL = // key = key+"$$$"+oid
         new HashVector<CharArray,InputContent>();
    
    /** constructor */
    public ContentsManager() {
        // do nothing
    }
    
    /** コンテンツを取得する 
        @param id content_id
        @return InputContent
    */
    public InputContent getContent(int id) {
        InputContent content = null;
        do {
            if (id < 0) break;
            IntObject obj = IntObject.pop(id);
            content = hashID.get(obj);
            IntObject.push(obj);
        } while (false);
        return content;
    }
    
    /* コンテンツを取得する 
        @param key コンテンツキーワード
    */
    public InputContent getContent(CharArray key, int oid) {
        return getContent(key, oid, null);
    }
    public InputContent getContent(CharArray key, int oid, SessionObject session) {
        String prefix="";
if (debug && session != null) prefix = session.count+"|";
if (debug) System.out.println(prefix+"●getContent("+key+","+oid+")");
        InputContent content = null;
        CharArray _key = CharArray.pop(key+"$$$"+oid);
        if (key != null && key.trim().length() > 0) {
            content = hashKey.get(_key);
        }
if (debug && content == null) System.out.println(prefix+" not found !! "+_key);
        if (content == null && ref_mode && ref_id > 0) {    // 参照モード
            _key.set(key+"$$$"+ref_id);
            content = hashKey.get(_key);
if (debug && content == null) System.out.println(prefix+" not found !! "+_key);
        }
        CharArray.push(_key);
if (debug && content != null) System.out.println(prefix+" content:"+key+" found !");
        return content;
    }
    
    /* コンテンツを取得する 
        @param key コンテンツキーワード
    */
    public InputContent getContent(String key, int oid) {
        return getContent(key, oid, null);
    }
    public InputContent getContent(String key, int oid, SessionObject session) {
        String prefix="";
if (debug && session != null) prefix = session.count+"|";
if (debug) System.out.println(prefix+"●getContent("+key+","+oid+")");
        InputContent content = null;
        CharArray _key = CharArray.pop(key+"$$$"+oid);
        if (key != null && key.trim().length() > 0) {
            content = hashKey.get(_key);
        }
if (debug && content == null) System.out.println(" not found !! "+_key);
        if (content == null && ref_mode && ref_id > 0) {    // 参照モード
            _key.set(key+"$$$"+ref_id);
            content = hashKey.get(_key);
if (debug && content == null) System.out.println(" not found !! "+_key);
        }
       CharArray.push(_key);
        return content;
    }

    /** コンテンツを削除する 
        @param id content_id
        @return true:削除成功
    */
    public boolean removeContent(int id) {
        boolean sts = false;
if (debug) System.out.println("●removeContent("+id+")");
        do {
            if (id < 0) break;
            IntObject obj = IntObject.pop(id);
            InputContent content = hashID.get(obj);
            if (content != null) {
                int oid = content.getOrgID();
                CharArray _key = CharArray.pop(content.getKey()+"$$$"+oid);
                CharArray _url = CharArray.pop(content.getURL()+"$$$"+oid);
                hashID.remove(obj);
                hashKey.remove(_key);
                hashURL.remove(_url);
                sts = true;
                CharArray.push(_url);
                CharArray.push(_key);
            }
            IntObject.push(obj);
        } while (false);
        return sts;
    }
    
    /* コンテンツを削除する 
        @param key コンテンツキー
        @return true:削除成功
    */
    public boolean removeContent(CharArray key, int oid) {
        boolean sts = false;
if (debug) System.out.println("●removeContent("+key+","+oid+")");
        do {
            if (key == null || key.trim().length() == 0) break;
            CharArray _key = CharArray.pop(key+"$$$"+oid);
            InputContent content = hashKey.get(_key);
            if (content != null) {
                int id = content.getID();
                CharArray _url = CharArray.pop(content.getURL()+"$$$"+oid);
                IntObject obj = IntObject.pop(id);
                hashID.remove(obj);
                hashKey.remove(_key);
                hashURL.remove(_url);
                sts = true;
                IntObject.push(obj);
                CharArray.push(_url);
            }
            CharArray.push(_key);
        } while (false);
        return sts;
    }

    /* コンテンツを削除する 
        @param key コンテンツキー
        @return true:削除成功
    */
    public boolean removeContent(String key, int oid) {
        CharArray chKey = CharArray.pop(key);
        boolean sts = removeContent(chKey, oid);
        CharArray.push(chKey);
        return sts;
    }
//  /*
//      コンテンツをリフレッシュまたは追加する
//  */
//  public boolean setContent(int id, long time) {
//      boolean sts = true;
//      
//      return true;
//  }
//  /*
//      コンテンツをリフレッシュまたは追加する
//      
//  */
//  public boolean setContent(CharArray key, long time) {
//      boolean sts = true;
//      
//      return true;
//  }
    
    /*
        コンテンツを追加する
        
    */
    public boolean addContent(int id, String key, CharArrayFile file, CharArrayFile ini, CharArray url, int oid) {
        CharArray chKey = CharArray.pop(key);
        boolean rsts = addContent(id, chKey, file, ini, url, oid);
        CharArray.push(chKey);
        return rsts;
    }
    
    public boolean addContent(int id, CharArray key, CharArrayFile file, CharArrayFile ini, CharArray url, int oid) {
if (debug) System.out.println("●addContent cid="+id+" key:"+key+" url:"+url+" oid:"+oid);
        boolean sts = true;
        InputContent content = new InputContent();
        content.setID(id);
        content.setKey(key);
        content.setDescription(""); // for debug
        content.setComment("");     // for debug
        content.setOrgID(oid);
        //CharArray ch = EmojiConverter.convFromText(file);
        //file.set(ch);
        //CharArray.push(ch);
        content.setTemplate(file);
        content.setIni(ini);
        content.setURL(url);
        
        content.parse(moduleManager);
        
        boolean parsed = false;
        
        IntObject obj = IntObject.pop(id);
        InputContent ic = hashID.get(obj);
        if (ic == null) {
            hashID.put(obj, content);
        } else {
            ic.copy(content);
            ic.parse();
            parsed = true;
            IntObject.push(obj);
        }
        
        CharArray _key = CharArray.pop(key+"$$$"+oid);
        ic = hashKey.get(_key);
        if (ic == null) {
            hashKey.put(_key, content);
        } else if (!parsed) {
            ic.copy(content);
            ic.parse();
            parsed = true;
            CharArray.push(_key);
        }
        
        CharArray _url = CharArray.pop(url+"$$$"+oid);
        ic = hashURL.get(_url);
        if (ic == null) {
            hashURL.put(_url, content);
        } else if (!parsed) {
            ic.copy(content);
            ic.parse();
            parsed = true;
            CharArray.push(_url);
        }
        
        return true;
    }
    
    /**
        コンテンツを全てクリア追加する
    */
    public void clearContent() {
        hashID.clear();
        hashKey.clear();
        hashURL.clear();
    }
    
    /*
        コンテンツURLに対応したコンテンツを返す
    */
    public InputContent getContentFromURL(CharArray url, int oid) {
        return getContentFromURL(url, oid, null);
    }
    public InputContent getContentFromURL(CharArray url, int oid, SessionObject session) {
        String prefix="";
if (debug && session != null) prefix = session.count+"|";
if (debug) System.out.println(prefix+"●getContentFromURL("+url+","+oid+")");
        CharArray _url = CharArray.pop(url+"$$$"+oid);
        InputContent content = hashURL.get(_url);
if (debug && content == null) System.out.println(" -- not found!! "+_url);
        if (content == null && ref_mode && ref_id > 0) {    // 参照モード
            _url.set(url+"$$$"+ref_id);
            content = hashURL.get(_url);
if (debug && content == null) System.out.println(" -- not found!! "+_url);
        }
        CharArray.push(_url);
        return content;
    }
    
    /*
        コンテンツURLに対応したコンテンツを返す
    */
    public InputContent getContentFromURL(String url, int oid) {
        return getContentFromURL(url, oid, null);
    }
    public InputContent getContentFromURL(String url, int oid, SessionObject session) {
        String prefix="";
if (debug && session != null) prefix = session.count+"|";
if (debug) System.out.println(prefix+"●getContentFromURL("+url+","+oid+")");
        CharArray _url = CharArray.pop(url+"$$$"+oid);
        InputContent content = hashURL.get(_url);
if (debug && content == null) System.out.println(" -- not found!! "+_url);
        if (content == null && ref_mode && ref_id > 0) {    // 参照モード
            _url.set(url+"$$$"+ref_id);
            content = hashURL.get(_url);
if (debug && content == null) System.out.println(" -- not found!! "+_url);
        }
        CharArray.push(_url);
        return content;
    }

    /*
        そのURLのコンテンツが存在するか？
    */
    public boolean existURL(String url, int oid) {
        CharArray _url = CharArray.pop(url+"$$$"+oid);
        InputContent content = hashURL.get(_url);
        CharArray.push(_url);
        return (content != null);
    }

    /*
        そのURLのコンテンツが存在するか？
    */
    public boolean existURL(CharArray url, int oid) {
        CharArray _url = CharArray.pop(url+"$$$"+oid);
        InputContent content = hashURL.get(_url);
        CharArray.push(_url);
        return (content != null);
    }


    /*
        URL ハッシュを取得する
    */
    public HashVector<CharArray,InputContent> getHashURL() {
        return hashURL;
    }

}

//
// [end of ContentsManager.java]
//

