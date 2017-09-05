//------------------------------------------------------------------------
// @(#)FormContainer.java
//                 
//             Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.renderer.Page;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.IntObject;


/**
 *  FORM コンテナー<br>
 *  formで使用するデータとform内のItemを管理します。
 *
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class FormContainer extends ItemContainer {

    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public FormContainer() { 
        super(); init();
        itemData = new FormData("","POST");     // 2010-11-09 変更
        itemData.setItem(this);
    }

    /** FormContainer を生成する
        @param action アクション文字列 */
    public FormContainer(String action) {
        super(); init();
        itemData = new FormData(action);
        itemData.setItem(this);
    }
    public FormContainer(String action,Page page) {
        super(); init();
        itemData = new FormData(action,page);
        itemData.setItem(this);
    }
    
    /** FormContainer を生成する
        @param action アクション文字列 */
    public FormContainer(CharArray action) {
        super(); init();
        itemData = new FormData(action);
        itemData.setItem(this);
    }
    public FormContainer(CharArray action,Page page) {
        super(); init();
        itemData = new FormData(action, page);
        itemData.setItem(this);
    }
    /** FormContainer を生成する 
      @param action アクション文字列 
      @param method 出力メソッド等 */
    public FormContainer(String action, String method) {
        super(); init();
        itemData = new FormData(action, method);
        itemData.setItem(this);
    }
    /** FormContainer を生成する 
      @param action アクション文字列 
      @param method 出力メソッド等
      @param target ターゲットフレーム */
    public FormContainer(String action, String method, String target) {
        super(); init();
        itemData = new FormData(action, method, target);
        itemData.setItem(this);
    }
    /** FormContainer を生成する 
       @param action アクション文字列 
       @param method 出力メソッド */
    public FormContainer(CharArray action, CharArray method) {
        super(); init();
        itemData = new FormData(action, method);
        itemData.setItem(this);
    }
    /** FormContainer を生成する 
       @param action アクション文字列 
       @param method 出力メソッド等
       @param target ターゲットフレーム */
    public FormContainer(CharArray action, CharArray method, CharArray target) {
        super(); init();
        itemData = new FormData(action, method, target);
        itemData.setItem(this);
    }
    /** FormContainer を生成する (copy constructor)
        @param from コピー元 FormContainer */
    public FormContainer(FormContainer from) {
        super(from); 
        setType(FORM);
        setCloneable(from.isCloneable());
        FormData fromdata = (FormData)from.itemData;
        itemData = new FormData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }
    /***/
    public FormContainer(String action, String method, Page page) {
        super(); init();
        itemData = new FormData(action, method, page);
        itemData.setItem(this);
    }
    /***/
    public FormContainer(CharArray action, CharArray method, Page page) {
        super(); init();
        itemData = new FormData(action, method, page);
        itemData.setItem(this);
    }

    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(FORM);
        setCloneable(true);
    }

    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    /** デフォルトaction文字列を設定する
       @param ch 設定文字列 */
//  public void setValue(CharArray ch) {
//      ((FormData)itemData).setValue(ch);
//  }
//  
//  /** ユーザー毎のaction文字列を設定する
//     @param sessionID セッションＩＤ
//     @param ch 設定文字列 */
//  public void setValue(CharArray ch, int sessionID) {
//      ((FormData)getItemData(sessionID)).setValue(ch);
//  }
    
    /** action文字列を設定する
       @param ch 設定文字列 */
    public void setAction(CharArray ch) {
        ((FormData)itemData).setAction(ch);
    }
    public void setAction(CharArray ch, int sessionID) {
        ((FormData)getItemData(sessionID)).setAction(ch);
    }
    public void setAction(CharArray ch, SessionObject session) {
        ((FormData)getItemData(session)).setAction(ch);
    }
    /** action文字列を設定する
       @param str 設定文字列 */
    public void setAction(String str) {
        ((FormData)itemData).setAction(str);
    }
    public void setAction(String str, int sessionID) {
        ((FormData)getItemData(sessionID)).setAction(str);
    }
    public void setAction(String str, SessionObject session) {
        ((FormData)getItemData(session)).setAction(str);
    }
    /** SSL 用のaction文字列を設定する
       @param ch 設定文字列 */
    public void setSSLAction(CharArray ch) {
        ((FormData)itemData).setSSLAction(ch);
    }
    public void setSSLAction(CharArray ch, int sessionID) {
        ((FormData)getItemData(sessionID)).setSSLAction(ch);
    }
    public void setSSLAction(CharArray ch, SessionObject session) {
        ((FormData)getItemData(session)).setSSLAction(ch);
    }
    /** SSL 用のaction文字列を設定する
       @param str 設定文字列 */
    public void setSSLAction(String str) {
        ((FormData)itemData).setSSLAction(str);
    }
    public void setSSLAction(String str, int sessionID) {
        ((FormData)getItemData(sessionID)).setSSLAction(str);
    }
    public void setSSLAction(String str, SessionObject session) {
        ((FormData)getItemData(session)).setSSLAction(str);
    }
    /** method 文字列を設定する*/
    public void setMethod(CharArray ch) {
        ((FormData)itemData).setMethod(ch);
    }
    public void setMethod(String str) {
        ((FormData)itemData).setMethod(str);
    }
    public void setMethod(CharArray ch, int sessionID) {
        ((FormData)getItemData(sessionID)).setMethod(ch);
    }
    public void setMethod(String str, int sessionID) {
        ((FormData)getItemData(sessionID)).setMethod(str);
    }
    public void setMethod(CharArray ch, SessionObject session) {
        ((FormData)getItemData(session)).setMethod(ch);
    }
    public void setMethod(String str, SessionObject session) {
        ((FormData)getItemData(session)).setMethod(str);
    }
    /** ターゲットフレームを設定する*/
    public void setTarget(CharArray ch) {
        ((FormData)itemData).setTarget(ch);
    }
    public void setTarget(String str) {
        ((FormData)itemData).setTarget(str);
    }
    public void setTarget(CharArray ch, int sessionID) {
        ((FormData)getItemData(sessionID)).setTarget(ch);
    }
    public void setTarget(String str, int sessionID) {
        ((FormData)getItemData(sessionID)).setTarget(str);
    }
    public void setTarget(CharArray ch, SessionObject session) {
        ((FormData)getItemData(session)).setTarget(ch);
    }
    public void setTarget(String str, SessionObject session) {
        ((FormData)getItemData(session)).setTarget(str);
    }
    /** MIME タイプを設定する*/
    public void setEncType(CharArray ch) {
        ((FormData)itemData).setEncType(ch);
    }
    public void setEncType(String str) {
        ((FormData)itemData).setEncType(str);
    }
    public void setEncType(CharArray ch, int sessionID) {
        ((FormData)getItemData(sessionID)).setEncType(ch);
    }
    public void setEncType(String str, int sessionID) {
        ((FormData)getItemData(sessionID)).setEncType(str);
    }
    public void setEncType(CharArray ch, SessionObject session) {
        ((FormData)getItemData(session)).setEncType(ch);
    }
    public void setEncType(String str, SessionObject session) {
        ((FormData)getItemData(session)).setEncType(str);
    }
    /** 飛び先のページを指定する */
    public void setJumpPage(Page page) {
        ((FormData)itemData).setJumpPage(page);
    }
    public void setJumpPage(Page page, int sessionID) {
        ((FormData)getItemData(sessionID)).setJumpPage(page);
    }
    public void setJumpPage(Page page, SessionObject session) {
        ((FormData)getItemData(session)).setJumpPage(page);
    }
    
    /** UTN指定（i-modeのみ有効）  */
    public void setUtn(boolean b) {
        ((FormData)itemData).setUtn(b);
    }
    /** UTN指定  */
    public void setUtn(boolean b, int sessionID) {
        ((FormData)getItemData(sessionID)).setUtn(b);
    }
    public void setUtn(boolean b, SessionObject session) {
        ((FormData)getItemData(session)).setUtn(b);
    }

    /** 新規セッション用のURLを生成する **/
    public void setInitialMode(boolean mode) {
        ((FormData)itemData).setInitialMode(mode);
    }
    
    /** 新規セッション用のURLを生成する **/
    public void setInitialMode(boolean mode, int sessionID) {
        ((FormData)getItemData(sessionID)).setInitialMode(mode);
    }
    public void setInitialMode(boolean mode, SessionObject session) {
        ((FormData)getItemData(session)).setInitialMode(mode);
    }
    
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    /** デフォルトアクション文字列を取得する
        @return アクション文字列 */
    public CharArrayQueue getValue() {
        return ((FormData)itemData).getValue();
    }
    /** ユーザー毎のアクション文字列を取得する
        @param sessionID セッションＩＤ
        @return アクション文字列 */
    public CharArrayQueue getValue(int sessionID) {
        return ((FormData)getItemData(sessionID)).getValue();
    } 
    public CharArrayQueue getValue(SessionObject session) {
        return ((FormData)getItemData(session)).getValue();
    } 
    /** アクション文字列を取得する */
    public CharArray getAction() {
        return ((FormData)itemData).getAction();
    }
    public CharArray getAction(int sessionID) {
        return ((FormData)getItemData(sessionID)).getAction();
    }
    public CharArray getAction(SessionObject session) {
        return ((FormData)getItemData(session)).getAction();
    }
    /** SSL用のアクション文字列を取得する */
    public CharArray getSSLAction() {
        return ((FormData)itemData).getSSLAction();
    }
    public CharArray getSSLAction(int sessionID) {
        return ((FormData)getItemData(sessionID)).getSSLAction();
    }
    public CharArray getSSLAction(SessionObject session) {
        return ((FormData)getItemData(session)).getSSLAction();
    }
    
    /** メソッド文字列を取り出す */
    public CharArray getMethod() {
        return ((FormData)itemData).getMethod();
    }
    public CharArray getMethod(int sessionID) {
        return ((FormData)getItemData(sessionID)).getMethod();
    }
    public CharArray getMethod(SessionObject session) {
        return ((FormData)getItemData(session)).getMethod();
    }
    
    /** ターゲット文字列を取り出す */
    public CharArray getTarget() {
        return ((FormData)itemData).getTarget();
    }
    public CharArray getTarget(int sessionID) {
        return ((FormData)getItemData(sessionID)).getTarget();
    }
    public CharArray getTarget(SessionObject session) {
        return ((FormData)getItemData(session)).getTarget();
    }

    /** MIMEタイプを取得する */
    public CharArray getEncType() {
        return ((FormData)itemData).getEncType();
    }
    public CharArray getEncType(int sessionID) {
        return ((FormData)getItemData(sessionID)).getEncType();
    }
    public CharArray getEncType(SessionObject session) {
        return ((FormData)getItemData(session)).getEncType();
    }
    
    /** UTN(i-modeのみ）を取得する */
    public boolean getUtn() {
        return ((FormData)itemData).getUtn();
    }
    
    /** UTN（i-modeのみ）を取得する */
    public boolean getUtn(int sessionID) {
        return ((FormData)getItemData(sessionID)).getUtn();
    }
    public boolean getUtn(SessionObject session) {
        return ((FormData)getItemData(session)).getUtn();
    }
    
    /** 新規セッション用URLを生成するか？ */
    public boolean getInitialMode() {
        return ((FormData)itemData).getInitialMode();
    }
    
    /** 新規セッション用URLを生成するか？ */
    public boolean getInitialMode(int sessionID) {
        return ((FormData)getItemData(sessionID)).getInitialMode();
    }
    public boolean getInitialMode(SessionObject session) {
        return ((FormData)getItemData(session)).getInitialMode();
    }
    //---------------------------------------------------------------------
    // キー関連
    //---------------------------------------------------------------------
    /** 
      デフォルトパラメータ用のキーリストを取得する<br>
      (存在する場合はセッションから情報を取得してパラメータ出力する)<br>
      nullは返さない
    */
    public CharArrayQueue getCheckKeys() {
        return ((FormData)getItemData()).getCheckKeys();
    }
    public CharArrayQueue getCheckKeys(int sessionID) {
        return ((FormData)getItemData(sessionID)).getCheckKeys();
    }
    public CharArrayQueue getCheckKeys(SessionObject session) {
        return ((FormData)getItemData(session)).getCheckKeys();
    }
    /** キーリストをクリアする */
    public void clearCheckKeys() {
        ((FormData)getItemData()).clearCheckKeys();
    }
    public void clearCheckKeys(int sessionID) {
        ((FormData)getItemData(sessionID)).clearCheckKeys();
    }
    public void clearCheckKeys(SessionObject session) {
        ((FormData)getItemData(session)).clearCheckKeys();
    }
    /** キーリストに追加する */
    public void addCheckKeys(CharArray key) {
        ((FormData)getItemData()).addCheckKeys(key);
    }
    public void addCheckKeys(String key) {
        ((FormData)getItemData()).addCheckKeys(key);
    }
    public void addCheckKeys(String key1, String key2) {
        ((FormData)getItemData()).addCheckKeys(key1, key2);
    }
    public void addCheckKeys(String key1, String key2, String key3) {
        ((FormData)getItemData()).addCheckKeys(key1, key2, key3);
    }
    public void addCheckKeys(String key1, String key2, String key3, String key4) {
        ((FormData)getItemData()).addCheckKeys(key1, key2, key3, key4);
    }
    //--
    public void addCheckKeys(CharArray key, int sessionID) {
        ((FormData)getItemData(sessionID)).addCheckKeys(key);
    }
    public void addCheckKeys(String key, int sessionID) {
        ((FormData)getItemData(sessionID)).addCheckKeys(key);
    }
    public void addCheckKeys(String key1, String key2, int sessionID) {
        ((FormData)getItemData(sessionID)).addCheckKeys(key1, key2);
    }
    public void addCheckKeys(String key1, String key2, String key3, int sessionID) {
        ((FormData)getItemData(sessionID)).addCheckKeys(key1, key2, key3);
    }
    public void addCheckKeys(String key1, String key2, String key3, String key4, int sessionID) {
        ((FormData)getItemData(sessionID)).addCheckKeys(key1, key2, key3, key4);
    }
    //--
    public void addCheckKeys(CharArray key, SessionObject session) {
        ((FormData)getItemData(session)).addCheckKeys(key);
    }
    public void addCheckKeys(String key, SessionObject session) {
        ((FormData)getItemData(session)).addCheckKeys(key);
    }
    public void addCheckKeys(String key1, String key2, SessionObject session) {
        ((FormData)getItemData(session)).addCheckKeys(key1, key2);
    }
    public void addCheckKeys(String key1, String key2, String key3, SessionObject session) {
        ((FormData)getItemData(session)).addCheckKeys(key1, key2, key3);
    }
    public void addCheckKeys(String key1, String key2, String key3, String key4, SessionObject session) {
        ((FormData)getItemData(session)).addCheckKeys(key1, key2, key3, key4);
    }
    
    //---------------------------------------------------------------------
    // レンダリング
    //---------------------------------------------------------------------
    public CharArray getActionHiddenTag(int sessionID) {
        return ((FormData)getItemData(sessionID)).getActionHiddenTag();
    }
    public CharArray getActionHiddenTag(SessionObject session) {
        return ((FormData)getItemData(session)).getActionHiddenTag();
    }
    //---------------------------------------------------------------------
    // copy 
    //---------------------------------------------------------------------
    /** デフォルトのオブジェクトをコピーしてユーザーオブジェクトを作成する
        @param session セッション
    */
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new FormData((FormData)itemData ,session);
            newData.setItem(this);
            session.getHashtable().put(new IntObject(getMixedID()), newData);
            
            ((FormData)newData).setMethod((session.userAgent != null) ?
                                            session.userAgent.method : "POST");
        }
        super.copy(session);
    }
    
    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする
        @param session セッション
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        FormData data = (FormData)getItemData(session);
        return data.draw(session);
    }
    // stream 版
    //public void draw(OutputStream out) {
        //未作成
    //}
    public void draw(OutputStream out, int sessionID) {
        //未作成
    }
     
    
}

//
// [end of FormContainer.java]
//

