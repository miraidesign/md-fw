//------------------------------------------------------------------------
// @(#)FormRenderer.java
//                 
//             Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.html;

import com.miraidesign.common.SystemConst;
import com.miraidesign.content.ContentParser;
import com.miraidesign.renderer.Module;
import com.miraidesign.renderer.Page;
import com.miraidesign.renderer.item.Item;
import com.miraidesign.renderer.item.FormData;
import com.miraidesign.renderer.item.ItemContainer;
import com.miraidesign.session.SessionObject;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.system.SiteMapping;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.Crypt62;

/**
 *  Form のレンダリング（HTML)を行う
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class FormRenderer /*implements ItemRenderer*/ {
    /**
        レンダリングする
        @param ch 出力バッファ
        @param formData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch,
                                 FormData formData) {
        draw_start(ch, formData);
        drawHidden(ch, formData);
        // コンテナの描画
        if (formData.getParentItemData() == null) {  // 親がDynamicItemDataでない場合
            Item item = formData.getItem();
            if (item.isContainer()) {
                ((ItemContainer)item).drawContainer(formData.getSessionObject());
            }
        }
        formData.drawContainer();   // getQueue()があるときのみ描画
        draw_end(ch,formData);
        return ch;
    }
    /**
        開始タグを描画する
        @param ch 一時出力先
        @param formData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw_start(CharArray ch,
                                       FormData formData) {
        SessionObject session = formData.getSessionObject();
        CharArray method = formData.getMethod();
        CharArray target = formData.getTarget();
        CharArray url    = formData.getURL();
        CharArray action = formData.getAction();
        CharArray enctype = formData.getEncType();
        boolean   utn     = formData.getUtn();
        ch.add("<form action=\"");
        boolean preview = false;
        int parser_mode = CharArray.getInt(session.getUserData().get("parser_mode"));
        CharArray dynamicURL = getMappingActionURL(formData);
        if (parser_mode == ContentParser.PREVIEW || parser_mode == ContentParser.REMOTE_PREVIEW 
            || parser_mode == ContentParser.PUBLISH_PREVIEW) {
            ch.add("#");
            preview = true;
        } else if (dynamicURL != null && dynamicURL.trim().length() > 0) {
            ch.add(dynamicURL);
        } else if (formData.changeURL) {
            ch.add(formData.getActionURL());
        } else {
            ch.add(url);
        }
        if (!formData.changeURL) {
            if (action.length()> 0) {
                if (url.length()>0) {
                    ch.add((url.indexOf('?') >= 0) ? "&amp;" : "?");
                }
                ch.add(action);
            }
        }
        ch.add("\"");
        if (preview) ch.add(" onSubmit=\"return false;\"");
        if (method.length() > 0) ch.add(" method=\""+method+"\"");
        
        if (target.length() > 0) ch.add(" target=\""+target+"\"");
        if (enctype.length() > 0) ch.add(" enctype=\""+enctype+"\"");
        //if (utn && session.isDocomo()) ch.add(" utn=\"utn\"");
        ch.add(">\n");
        return ch;
    }
    /**
        終了タグを描画する
        @param ch 一時出力先
        @param formData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw_end(CharArray ch, FormData formData) {
        ch.add("</form>\n");
        return ch;
    }
    /**
        Hiddenデータをを描画する
        @param ch 一時出力先
        @param formData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray drawHidden(CharArray ch, FormData formData) {
        return drawHidden(ch,formData,true);
    }
    static private CharArray drawHidden(CharArray ch, FormData formData,
                                       boolean mode) {  // 最後に ">"をつけるか？
//System.out.println("  ★drawHidden");
        int mixedID = formData.getItem().getMixedID();
        int sessionID = 0;
        SessionObject session = formData.getSessionObject();
        boolean ssl = false;
        boolean appendAuthID = false;
        if (session != null) {
            sessionID = session.getSessionID();
            ssl = session.isSSL();
            appendAuthID = (session.isAppendAuthID() && session.getAuthID()>0);
        }
        int jumpID = 0;
        Page page = formData.getPage();
        if (page != null) { // 飛び先が指定してあれば
            Module module = page.getModule();
            jumpID = module.getModuleID()*1000+page.getPageID();
        }
        CharArray action = formData.getAction();
        CharArray sslAction = formData.getSSLAction();
        if (ssl && sslAction.length() > 0) action = sslAction;
        if (formData.changeURL && action.length() > 0) {
            CharArray chAction = CharArray.pop();
            int index = action.indexOf('?');
                
            if (index >= 0) {
                chAction.set(action,index+1);
            } else {
                chAction.set(action);
            }
            if (chAction.length() > 0 && 
                chAction.indexOf("://") < 0 && 
                chAction.indexOf('=') >0 ) {
                CharToken token = CharToken.pop();
                CharToken token2 = CharToken.pop();
                
                token.set(chAction,"&amp;");
                if (token.size() < 2) token.set(chAction,"&");
                for (int i = 0; i < token.size(); i++) {
                    token2.set(token.get(i),"=");
                    ch.add("    <input type=\"hidden\" name=\"");
                    ch.add(token2.get(0));
                    if (token2.size() > 1) {
                        ch.add("\" value=\"");
                        ch.add(token2.get(1));
                    }
                    ch.add("\"/>\n");
                }
                CharToken.push(token2);
                CharToken.push(token);
            }
            CharArray.push(chAction);
        }
        //------------------------------------------- siteCode
        ModuleManager mm = session.getModuleManager();
        SiteMapping  map = mm.getSiteMapping();
        if (formData.getInitialMode() || mm.ini.getBoolean("["+mm.sectionBase+"]","UseSiteCode")) {
            int _site = getSiteCode(formData);
            if (_site > 0) {
                ch.add("    <input type=\"hidden\" name=\"");
                ch.add(SystemConst.siteKey); 
                ch.add("\" value=\"");
                ch.format(_site);
                ch.add("\"/>\n");
            }
        }
        //------------------------------------------- Session ID
        if (!formData.getInitialMode()) {
            int parser_mode = CharArray.getInt(session.getUserData().get("parser_mode"));
            if (parser_mode != ContentParser.PUBLISH_PREVIEW && parser_mode != ContentParser.PUBLISH) {
                ch.add("    <input type=\"hidden\" name=\"");
                ch.add(SystemConst.sessionIDKey[SystemConst.cryptSessionID ? 1 : 0]); 
                ch.add("\" value=\"");
                if (SystemConst.cryptSessionID) {
                    ch.add(Crypt62.encode(sessionID));
                } else {
                    ch.format(sessionID);
                }
                ch.add("\"/>\n");
            }
        }
        //------------------------------------------- MixedID
        ch.add("    <input type=\"hidden\" name=\"");
        ch.add(SystemConst.mixedIDKey[SystemConst.cryptMixedID ? 1 : 0]);
        ch.add("\"");
        ch.add(" value=\"");
        if (SystemConst.cryptMixedID) {
            ch.add(Crypt62.encode(mixedID));
        } else {
            ch.format(mixedID);
        }
        ch.add("\"/");
        if (mode) ch.add(">\n");
        //------------------------------------------- jump Page ID
        if (page != null) {
            CharArray mappingURL = map.getUserURL(jumpID);
            if (mappingURL != null && mappingURL.length() > 0) { // ユーザーマッピングあり
                CharToken token = CharToken.pop();
                token.set(mappingURL,"/");
                CharToken token2 = CharToken.pop();
                for (int i = 0; i < token.size(); i++) {
                    CharArray _ch = token.get(i);
                    token2.set(_ch, "=");
                    if (token2.size() == 2) {
                        CharArray _key = token2.get(0);
                        CharArray _value = token2.get(1);
                        if (!mode) ch.add(">\n");
                        ch.add("    <input type=\"hidden\" name=\"");
                        ch.add(_key);
                        ch.add("\" value=\"");
                        ch.add(_value);
                        ch.add("\"/");
                        if (mode) ch.add(">\n");
                    }
                }
                CharToken.push(token2);
                CharToken.push(token);
            } else {
                if (!mode) ch.add(">\n");
                CharArray _url = map.getContentURL(jumpID);
                ch.add("    <input type=\"hidden\" name=\"");
                ch.add((_url == null)? SystemConst.pageIDKey : "ckey");
                ch.add("\" value=\"");
                if (_url == null) ch.format(jumpID);
                else              ch.add(_url);
                ch.add("\"/");
                if (mode) ch.add(">\n");
            }
        }
        //------------------------------------------- AuthID
        if (appendAuthID) {
            if (!mode) ch.add(">\n");
            ch.add("    <input type=\"hidden\" name=\"");
            ch.add(SystemConst.authIDKey);
            ch.add("\" value=\"");
            ch.add(Crypt62.encode(session.getAuthID()));
            ch.add("\"/");
            if (mode) ch.add(">\n");
        }
        //-------------------------------------------
        CharArray target = formData.getTarget();
        if (target.length() > 0) {
            if (!mode) ch.add(">\n");
            ch.add("    <input type=\"hidden\" name=\"");
            ch.add(SystemConst.frameKey);
            ch.add("\" value=\"");
            ch.add(target);
            ch.add("\"/");
            if (mode) ch.add(">\n");
        }
        //-------------------------------------------
        CharArrayQueue checkKeys = formData.getCheckKeys();
        for (int i = 0; i < checkKeys.size(); i++) {
            CharArray key = checkKeys.peek(i);
            CharArray data = session.getCheckParameter(key);
            if (data != null) {
                if (!mode) ch.add(">\n");
                ch.add("    <input type=\"hidden\" name=\"");
                ch.add(key);
                ch.add("\" value=\"");
                ch.add(data);
                ch.add("\"/");
                if (mode) ch.add(">\n");
            }
        }
        return ch;
    }
    /**
        action部を描画する
        @param ch 一時出力先
        @param formData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray drawAction(CharArray ch,
                                       FormData formData) {
//System.out.println("  ★drawAction");
        SessionObject session = formData.getSessionObject();
        CharArray method = formData.getMethod();
        CharArray target = formData.getTarget();
        CharArray url    = formData.getURL();
        CharArray action = formData.getAction();
        CharArray enctype = formData.getEncType();
        boolean   utn     = formData.getUtn();
        ch.add("action=\"");
        
        int parser_mode = CharArray.getInt(session.getUserData().get("parser_mode"));
        boolean preview = false;
        CharArray dynamicURL = getMappingActionURL(formData);
        if (parser_mode == ContentParser.PREVIEW || parser_mode == ContentParser.REMOTE_PREVIEW 
            || parser_mode == ContentParser.PUBLISH_PREVIEW) {
            ch.add("#");
            preview = true;
        } else if (dynamicURL != null && dynamicURL.trim().length() > 0) {
            ch.add(dynamicURL);
        } else if (formData.changeURL) {
            ch.add(formData.getActionURL());
        } else {
            ch.add(url);
        }
        if (!formData.changeURL) {
            if (action.length()> 0) {
                ch.add((url.indexOf('?') >= 0) ? "&amp;" : "?");
                ch.add(action);
            }
        }
        ch.add("\"");
        if (preview) ch.add(" onSubmit=\"return false;\"");
        if (method.length() > 0) ch.add(" method=\""+method+"\"");
        if (target.length() > 0) ch.add(" target=\""+target+"\"");
        if (enctype.length() > 0) ch.add(" enctype=\""+enctype+"\"");
        //if (utn && session.isDocomo()) ch.add(" utn=\"utn\"");
        return ch;
    }
    /**
        action+Hidden部を描画する
        @param ch 一時出力先
        @param formData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray drawActionHidden(CharArray ch,
                                       FormData formData) {
//System.out.println("  ★drawActionHidden");
        drawAction(ch, formData);
        ch.add(">\n");
        drawHidden(ch,formData,false);
        return ch;
    }
    
    /** Module PageID を取得する */
    static int getJumpID(FormData formData) {
        int jumpID = 0;
        Page page = formData.getPage();
        if (page != null) { // 飛び先が指定してあれば
            Module module = page.getModule();
            jumpID = module.getModuleID()*1000+page.getPageID();
        }
        return jumpID;
    }
    
    /** */
    static CharArray getMappingActionURL(FormData formData) {
        CharArray url = null;
        do {
            SessionObject session = formData.getSessionObject();
            ModuleManager mm = session.getModuleManager();
            SiteMapping  map = mm.getSiteMapping();
            int jumpID = getJumpID(formData);
            CharArray mappingURL = map.getUserURL(jumpID);
            if (mappingURL != null && mappingURL.length() > 0) { // ユーザーマッピングあり
//System.out.println("★★jumpID:"+jumpID+ "にマッピングが存在する");
                url = mm.ini.get(mm.getSection("Mode"), "DynamicViewURL");
            }
        } while (false);
//System.out.println("★★actionURL:"+url);
        return url;
    }
    
    /** サイトコードを取得する（マッピングされてたら書き換える) */
    static int getSiteCode(FormData formData) {
        int site_code = 0;
        do {
            SessionObject session = formData.getSessionObject();
            ModuleManager mm = session.getModuleManager();
            SiteMapping  map = mm.getSiteMapping();
            int jumpID = getJumpID(formData);
            CharArray mappingURL = map.getUserURL(jumpID);
            if (mappingURL != null && mappingURL.length() > 0) { //
                site_code = mm.ini.getInt(mm.getSection("Mode"), "DynamicSiteCode");
            }
            if (site_code == 0) site_code = mm.getSiteChannelCode();
        } while (false);
        return site_code;
    }
    
}

//
// [end of FormRenderer.java]
//

