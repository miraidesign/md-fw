//------------------------------------------------------------------------
// @(#)FormData.java
//                 FORM のデータを保管する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------
// 未対応： enctype accept-charset
//

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.common.SystemConst;
import com.miraidesign.renderer.Module;
import com.miraidesign.renderer.Page;
import com.miraidesign.session.SessionObject;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.SiteManager;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.system.SiteMapping;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.Crypt62;

/**
 *  FormContainer のデータを保管するクラス
 *  
 *  @version 0.9 
 *  @author Toru Ishioka
**/
public class FormData extends DynamicItemData {
    static private boolean debug = (SystemConst.debug && false);  // デバッグ表示
    
    static public boolean changeURL = true;    // URL表示仕様を変更する
    
    private CharArray action  = new CharArray();     // 処理先ＵＲＩ
    private CharArray sslAction  = new CharArray();  // 処理先ＵＲＩ
    private CharArray method  = new CharArray();     // メソッド
    private CharArray target  = new CharArray();     // ターゲットフレーム
    private CharArray enctype  = new CharArray();    // MIME type ver0.662
    private boolean   utn = false;  // for i-Mode
    private Page page;                               // Jump先ページ
    public Page getPage() { return page; }

    /** 
      デフォルトパラメータ用のキーリスト
      存在する場合はセッションから情報を取得してパラメータ出力する
    */
    private CharArrayQueue checkKeys = new CharArrayQueue();
    /** 
      デフォルトパラメータ用のキーリストを取得する<br>
      (存在する場合はセッションから情報を取得してパラメータ出力する)<br>
      nullは返さない
    */
    public CharArrayQueue getCheckKeys() { return checkKeys; }

    /** キーリストをクリアする */
    public void clearCheckKeys() { checkKeys.clear();}
    
    public void addCheckKeys(CharArray... keys) {
        for (int i = 0; i < keys.length; i++) {
             checkKeys.enqueue(keys[i]);
        }
    }
    public void addCheckKeys(String... keys) {
        for (int i = 0; i < keys.length; i++) {
             checkKeys.enqueue(keys[i]);
        }
    }
    private boolean makeInitialURL = false;         // 新規セッション用
    
    {
        caQueue.enqueue(action);
        type = FORM;
    }
    
    /** 新規セッション用のURLを生成する */
    public void setInitialMode(boolean mode) {
        makeInitialURL = mode;
    }
    /** 新規セッション用のURLを生成するか？ */
    public boolean getInitialMode() { return makeInitialURL;}
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public FormData() {
    }

    /** コピーコンストラクタ */
    public FormData(FormData from, SessionObject session) {
        this.sessionObject = session;
        copy(from);
    }
    
    public FormData(String action) {
        setAction(action);
    }
    public FormData(CharArray action) {
        setAction(action);
    }
    public FormData(String action,String method) {
        setAction(action);
        setMethod(method);
    }
    public FormData(CharArray action, CharArray method) {
        setAction(action);
        setMethod(method);
    }
    public FormData(String action,String method,String target) {
        setAction(action);
        setMethod(method);
        setTarget(target);
    }
    public FormData(CharArray action, CharArray method, CharArray target) {
        setAction(action);
        setMethod(method);
        setTarget(target);
    }
    //-------------------------------------------------------
    public FormData(String action, Page page) {
        this.page = page;
        setAction(action);
    }
    public FormData(CharArray action, Page page) {
        this.page = page;
        setAction(action);
    }
    public FormData(String action,String method, Page page) {
        this.page = page;
        setAction(action);
        setMethod(method);
    }
    public FormData(CharArray action, CharArray method, Page page) {
        this.page = page;
        setAction(action);
        setMethod(method);
    }
    
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setValue(String[] strs) { action.set(strs[0]);}
    public void setValue(String text) { action.set(text);}

    public void setAction(String text) { action.set(text);}
    public void setSSLAction(String text) { sslAction.set(text);}
    public void setMethod(String text) { method.set(text);}
    public void setTarget(String text) { target.set(text);}
    public void setEncType(String text) { enctype.set(text);}

    public void setValue(CharArray ch) { action.set(ch);}
    public void setAction(CharArray ch) { action.set(ch);}
    public void setSSLAction(CharArray ch) { sslAction.set(ch);}
    public void setMethod(CharArray ch) { method.set(ch);}
    public void setTarget(CharArray ch) { target.set(ch);}
    public void setEncType(CharArray ch) { enctype.set(ch);}
    public void setUtn(boolean mode) { utn = mode; }
    /** ジャンプ先のPageオブジェクトを設定する */
    public void setJumpPage(Page page) { this.page = page;}

    public void setSessionObject(SessionObject session) {
        super.setSessionObjectAll(session);
    }
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    /** アクション文字列を取り出す
       @return アクション文字列 */
    public CharArrayQueue getValue() { return caQueue; }

    public CharArray getAction() { return action; }
    public CharArray getSSLAction() { return sslAction; }

    /** メソッド文字列を取り出す
       @return メソッド文字列 */
    public CharArray getMethod() { return method; }

    /** ターゲット文字列を取り出す
       @return ターゲット文字列 */
    public CharArray getTarget() { return target; }

    /** MIME typeを取り出す */
    public CharArray getEncType() { return enctype; }

    public boolean getUtn() { return utn; }
    /** ジャンプ先ページを取得する */
    public Page      getJumpPage() { return page; }

    /* フレームワーク用の内部URLを取得する */
    public CharArray getURL() {
        CharArray ch = new CharArray();
        ch.setAppendMode(true);
        if (page != null) { // 飛び先が指定してあれば
            Module module = page.getModule();
            //ch.add(module.getServletPath());
            boolean ssl = false;
            if (sessionObject != null) {
                if (changeURL) ssl = sessionObject.isSSL();
                else           ssl = (sessionObject.isSSL() && sslAction.length()>0);
            }
            if (!changeURL) {
                if (!ssl && action.indexOf("://") < 0) {
                    if (module != null) ch.add(module.getServletPath());
                    else                ch.add(SystemManager.servletPath);
                }
                if (ssl && sslAction.indexOf("://") < 0) {
                    if (module != null) ch.add(module.getServletPath());
                    else                ch.add(SystemManager.servletPath);
                }
            } else {
                CharArray chAction = action;
                if (ssl && sslAction.length() > 0) chAction = sslAction;
                if ((chAction.indexOf("://") < 0) || (chAction.length() == 0)) {
                    if (module != null && sessionObject != null) {  // 2010-20-40
                        SiteMapping map = module.getModuleManager().getSiteMapping();
                        if (map != null) {
                            map.getSiteParameter(sessionObject, ch);
                            ch.add(SystemManager.servletPath);
                            ch.replace("//","/");
                        } else {
                            ch.add(module.getServletPath());
                        }
                    } else if (module != null) ch.add(module.getServletPath());
                    else                ch.add(SystemManager.servletPath);
                }
            }
        }
        return ch;
    }
    /** &gt;a href= 以降の文字列を返します **/
    public CharArray getActionURL() {
        return getActionURL(new CharArray());
    }
    public CharArray getActionURL(CharArray chBuf) {
        CharArray ch = CharArray.pop();
        CharArray url  = getURL();
        ch.add(url);
        
        boolean ssl = false;
        if (sessionObject != null) {
            if (changeURL) ssl = sessionObject.isSSL();
            else           ssl = (sessionObject.isSSL() && sslAction.length()>0);
        }
        if (!changeURL) { // 昔の方法
            if (ssl) {
                if (url.length()>0) {
                    ch.add((url.indexOf('?') >= 0) ? "&amp;" : "?");
                }
                ch.add(sslAction);
            } else {
                if (action.length()> 0) {
                    if (url.length()>0) {
                        ch.add((url.indexOf('?') >= 0) ? "&amp;" : "?");
                    }
                    ch.add(action);
                }
            }
        } else {    // 新仕様
            CharArray chAction = action;
            if (ssl && sslAction.length() > 0) chAction = sslAction;
            if (chAction.length() > 0) {   // chActionの'?'までを追加する
                int index  = chAction.indexOf('?');
                int index2 = chAction.indexOf('=');
                
                if (index >= 0 || index2 < 0) {
                    if (url.length() > 0) ch.add('?');
                    ch.add(chAction,0,index);
                }
            }
        }
        
        chBuf.add(ch);
        CharArray.push(ch);
        return chBuf;
    }
    
    /** GET しかできない機種用 */
    public CharArray getAppendURL() {
        return getAppendURL(new CharArray());
    }
    public CharArray getAppendURL(CharArray chBuf) {
        CharArray ch = CharArray.pop();
        String szAmp = "&amp;";
        
        int mixedID = item.getMixedID();
        int sessionID = 0;
        if (sessionObject != null) sessionID = sessionObject.getSessionID();
        int jumpID = 0;
        if (page != null) { // 飛び先が指定してあれば
            Module module = page.getModule();
            if (module != null) {
                jumpID = module.getModuleID()*1000+page.getPageID();
            }
        }
        if (SystemConst.cryptSessionID) {
            ch.add(SystemConst.sessionIDKey[1]); ch.add('=');
            ch.add(Crypt62.encode(sessionID));
        } else {
            ch.add(SystemConst.sessionIDKey[0]); ch.add('=');
            ch.format(sessionID);
        }
        ch.add(szAmp);
        if (SystemConst.cryptMixedID) {
            ch.add(SystemConst.mixedIDKey[1]); ch.add('=');
            ch.add(Crypt62.encode(mixedID));
        } else {
            ch.add(SystemConst.mixedIDKey[0]); ch.add('=');
            ch.format(mixedID);
        }
        if (page != null && jumpID > 0) {
            ch.add(szAmp);
            ch.add(SystemConst.pageIDKey); ch.add('=');
            ch.format(jumpID);
        }
        for (int i = 0; i < checkKeys.size(); i++) {
            CharArray key = checkKeys.peek(i);
            CharArray data = sessionObject.getCheckParameter(key);
            if (data != null) {
                ch.add(szAmp);
                ch.add(key);
                ch.add('=');
                ch.add(data);
            }
        }
        chBuf.add(ch);
        CharArray.push(ch);
        return chBuf;
    }

    //public int getSessionID() {
    //    if (sessionObject == null) return 0;
    //    return sessionObject.getSessionID();
    //}
    //------------------------------------------------
    /**
        getActionTag() を使用してください
        @deprecated
    */
    public CharArray getAction(CharArray chBuf) {
        return getActionTag(chBuf);
    }
/******
*   public CharArray getActionTag() { 
*       return getActionTag(new CharArray());
*   }
*   public CharArray getActionTag(CharArray chBuf) {
*       CharArray ch = CharArray.pop();
*       ch.add("action=\"");
*       CharArray url = getURL();
*       ch.add(url);
*       if (action.length()> 0) {
*           ch.add((url.indexOf('?') >= 0) ? "&amp;" : "?");
*           ch.add(action);
*       }
*       ch.add("\"");
*       if (method.length() > 0) ch.add(" method=\""+method+"\"");
*       if (target.length() > 0) ch.add(" target=\""+target+"\"");
*       chBuf.add(ch);
*       CharArray.push(ch);
*       return chBuf;
*   }
*   
*   public CharArray getTag(CharArray ch) {
*       ch.add("<form action=\"");
*       CharArray url = getURL();
*       ch.add(url);
*       if (action.length()> 0) {
*           ch.add((url.indexOf('?') >= 0) ? "&amp;" : "?");
*           ch.add(action);
*       }
*       ch.add("\"");
*       if (method.length() > 0) ch.add(" method=\""+method+"\"");
*       if (target.length() > 0) ch.add(" target=\""+target+"\"");
*       ch.add(">\n");
*       getHiddenData(ch);
*       return ch;
*   }
*   //----------------------------------------------
*   public CharArray getHiddenData() {
*       return getHiddenData(new CharArray());
*   }
*   public CharArray getHiddenData(CharArray ch) {
*       int mixedID = item.getMixedID();
*       int sessionID = 0;
*       if (sessionObject != null) sessionID = sessionObject.getSessionID();
*       int jumpID = 0;
*
*       if (page != null) { // 飛び先が指定してあれば
*           Module module = page.getModule();
*           jumpID = module.getModuleID()*1000+page.getPageID();
*       }
*       
*       if (action.length() > 0) {
*           // &分割して追加する
*           
*           //ch.add(action);
*       }
*       
*       //-------------------------------------------
*       ch.add("<input type=hidden name=\"");
*       ch.add(SystemConst.sessionIDKey[SystemConst.cryptSessionID ? 1 : 0]); 
*       ch.add("\"");
*       ch.add(" value=\"");
*       if (SystemConst.cryptSessionID) {
*           ch.add(Crypt62.encode(sessionID));
*       } else {
*           ch.format(sessionID);
*       }
*       ch.add("\">\n");
*       //-------------------------------------------
*       ch.add("<input type=hidden name=\"");
*       ch.add(SystemConst.mixedIDKey[SystemConst.cryptMixedID ? 1 : 0]);
*       ch.add("\"");
*       ch.add(" value=\"");
*       if (SystemConst.cryptMixedID) {
*           ch.add(Crypt62.encode(mixedID));
*       } else {
*           ch.format(mixedID);
*       }
*       ch.add("\">\n");
*       //-------------------------------------------
*       if (page != null) {
*           ch.add("<input type=hidden name=\"");
*           ch.add(SystemConst.pageIDKey);
*           ch.add("\"");
*           ch.add(" value=\"");
*           ch.format(jumpID);
*           ch.add("\">\n");
*       }
*       return ch;
*   }
*   
*******/

    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    /**
        元オブジェクトより全データをコピーする
        @param from コピー元オブジェクト
    */
    public void copy(FormData from) {
        super.copyAll(from);    // DynamicItemDataのメソッド
        action.set(from.action);
        sslAction.set(from.sslAction);
        method.set(from.method);
        target.set(from.target);
        enctype.set(from.enctype);
        utn = from.utn;
        this.page = from.page;
        this.makeInitialURL = from.makeInitialURL;
        checkKeys.copy(from.checkKeys); // 0.827
    }

    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする
        @param session セッションオブジェクト
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        if (visible) {
            session.itemRenderer.drawForm(session.getBuffer(),this);
        }
        return session.getBuffer();
    }


    public CharArray getTag(CharArray ch) {
        sessionObject.itemRenderer.drawFormStart(ch,this);
        return sessionObject.itemRenderer.drawFormHidden(ch,this);
    }
    public CharArray getTag() { return getTag(new CharArray());}
    
    public CharArray getTagStart(CharArray ch) {
        return sessionObject.itemRenderer.drawFormStart(ch,this);
    }
    public CharArray getTagStart() { return getTagStart(new CharArray());}
    
    public CharArray getTagEnd(CharArray ch) {
        return sessionObject.itemRenderer.drawFormEnd(ch,this);
    }
    public CharArray getTagEnd() { return getTagEnd(new CharArray());}

    public CharArray getActionTag() {
        return getActionTag(new CharArray());
    }
    public CharArray getActionTag(CharArray ch) {
        return sessionObject.itemRenderer.drawFormAction(ch,this);
    }
    public CharArray getHiddenTag() {
        return getHiddenTag(new CharArray());
    }
    public CharArray getHiddenTag(CharArray ch) {
        return sessionObject.itemRenderer.drawFormHidden(ch,this);
    }
    public CharArray getActionHiddenTag() {
        return getActionHiddenTag(new CharArray());
    }
    public CharArray getActionHiddenTag(CharArray ch) {
        return sessionObject.itemRenderer.drawFormActionHidden(ch,this);
    }

    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            action.writeObject(out);
            method.writeObject(out);
            target.writeObject(out);
            
            if (page != null) {
                out.writeBoolean(true);
                // Jump先ページ 指定
                Module module = page.getModule();
                ModuleManager mm = module.getModuleManager();
                out.writeInt(mm.getSiteChannelCode()); 
                out.writeInt(module.getModuleID());
                out.writeInt(page.getPageID());
            } else {
                out.writeBoolean(false);
            }
            sslAction.writeObject(out);
            enctype.writeObject(out);
            out.writeBoolean(utn);

            out.writeBoolean(makeInitialURL);
            // default key
            checkKeys.writeObject(out);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            action.readObject(in);
            method.readObject(in);
            target.readObject(in);
            
            if (in.readBoolean()) {
                // Jumpページの取り出し
                ModuleManager mm = SiteManager.get(in.readInt());
                Module module = mm.getModule(in.readInt());
                page = module.get(in.readInt());
            } else {
                //page = null;
            }
            sslAction.readObject(in);
            enctype.readObject(in);
            utn = in.readBoolean();
            
            makeInitialURL = in.readBoolean();
            // default key
            checkKeys.readObject(in);
        }
    }
}

//
// [end of FormData.java]
//

