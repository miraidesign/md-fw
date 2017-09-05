//------------------------------------------------------------------------
// @(#)ImgItem.java
//             <img  >
//             Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
//
package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.IntObject;

/** 画像アイテム */
public class ImgItem extends Item {

    String docRoot = null;

    /**
        オリジナル画像のドキュメントルートを指定する<br>
        デフォルト(null)はSystemManager.docRoot を利用する
    */
    public void setDocRoot(String docRoot) {
        this.docRoot = docRoot;
    }
    /**
        指定されたドキュメントルートを返す<br>
        デフォルトは null
    */
    public String getDocRoot() { return docRoot;}
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /** ImgItem の生成 */
    public ImgItem() { 
        super();
        init();
        itemData = new ImgData();
        itemData.setItem(this);
    }
    /** ImgItem の生成
        @param str  表示文字列
    */
    public ImgItem(String str) { 
        super();
        init();
        itemData = new ImgData(str);
        itemData.setItem(this);
    }
    /** ImgItem の生成
        @param cloneable clonaable
    */
    public ImgItem(boolean cloneable) { 
        super();
        init();
        itemData = new ImgData();
        itemData.setItem(this);
        setCloneable(cloneable);
    }
    /** ImgItem の生成
        @param str  表示文字列
        @param cloneable clonaable
    */
    public ImgItem(String str,boolean cloneable) { 
        super();
        init();
        itemData = new ImgData(str);
        itemData.setItem(this);
        setCloneable(cloneable);
    }
    /** ImgItem の生成
        @param ch  表示文字列
    */
    public ImgItem(CharArray ch) { 
        super();
        init();
        itemData = new ImgData(ch);
        itemData.setItem(this);
    }
    /** ImgItem の生成
        @param ch  表示文字列
        @param cloneable clonaable
    */
    public ImgItem(CharArray ch, boolean cloneable) { 
        super();
        init();
        itemData = new ImgData(ch);
        itemData.setItem(this);
        setCloneable(cloneable);
    }
    /** copy consutuctor */
    public ImgItem(ImgItem from) { 
        super();
        setType(IMG);
        setCloneable(from.isCloneable());
        ImgData fromdata = (ImgData)from.itemData;
        itemData = new ImgData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }
    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(IMG);
        setCloneable(false);
    }

    /** 設定をクリアする(src/alt) */
    public void clear() {
        ((ImgData)itemData).clear();
    }
    /** 設定をクリアする(src/alt) */
    public void clear(SessionObject session) {
        ((ImgData)getItemData(session)).clear();
    }

    //---------------------------------------------------------------------
    // データ設定メソッド
    //---------------------------------------------------------------------
/**
    public void setValue(CharArray ch) {
        ((ImgData)itemData).setValue(ch);
    }
    
    public void setValue(CharArray ch, int sessionID) {
        ((ImgData)getItemData(sessionID)).setValue(ch);
    }
**/ 
    /** ソース設定 */
    public void setSrc(CharArray ch) {
        ((ImgData)itemData).setSrc(ch);
    }
    public void setSrc(CharArray ch, int sessionID) {
        ((ImgData)getItemData(sessionID)).setSrc(ch);
    }
    public void setSrc(CharArray ch, SessionObject session) {
        ((ImgData)getItemData(session)).setSrc(ch);
    }
    public void setSrc(String str) {
        ((ImgData)itemData).setSrc(str);
    }
    public void setSrc(String str, int sessionID) {
        ((ImgData)getItemData(sessionID)).setSrc(str);
    }
    public void setSrc(String str, SessionObject session) {
        ((ImgData)getItemData(session)).setSrc(str);
    }
    /** 代替文字設定 */
    public void setAlt(CharArray ch) {
        ((ImgData)itemData).setAlt(ch);
    }
    public void setAlt(CharArray ch, int sessionID) {
        ((ImgData)getItemData(sessionID)).setAlt(ch);
    }
    public void setAlt(CharArray ch, SessionObject session) {
        ((ImgData)getItemData(session)).setAlt(ch);
    }
    public void setAlt(String str) {
        ((ImgData)itemData).setAlt(str);
    }
    public void setAlt(String str, int sessionID) {
        ((ImgData)getItemData(sessionID)).setAlt(str);
    }
    public void setAlt(String str, SessionObject session) {
        ((ImgData)getItemData(session)).setAlt(str);
    }
    /** width設定 */
    public void setWidth(int size) {
        ((ImgData)itemData).setWidth(size);
    }
    public void setWidth(int size, int sessionID) {
        ((ImgData)getItemData(sessionID)).setWidth(size);
    }
    public void setWidth(int size, SessionObject session) {
        ((ImgData)getItemData(session)).setWidth(size);
    }
    /** height設定 */
    public void setHeight(int size) {
        ((ImgData)itemData).setHeight(size);
    }
    public void setHeight(int size, int sessionID) {
        ((ImgData)getItemData(sessionID)).setHeight(size);
    }
    public void setHeight(int size, SessionObject session) {
        ((ImgData)getItemData(session)).setHeight(size);
    }
    /** align設定*/
    public void setAlign(int size) {
        ((ImgData)itemData).setAlign(size);
    }
    public void setAlign(int size, int sessionID) {
        ((ImgData)getItemData(sessionID)).setAlign(size);
    }
    public void setAlign(int size, SessionObject session) {
        ((ImgData)getItemData(session)).setAlign(size);
    }
    /** border設定 */
    public void setBorder(int size) {
        ((ImgData)itemData).setBorder(size);
    }
    public void setBorder(int size, int sessionID) {
        ((ImgData)getItemData(sessionID)).setBorder(size);
    }
    public void setBorder(int size, SessionObject session) {
        ((ImgData)getItemData(session)).setBorder(size);
    }
    /** proxy 設定 
        true にすると cloneable=true になります
    */
    public void setProxy(boolean mode) {
        if (mode) setCloneable(true);
        ((ImgData)itemData).setProxy(mode);
    }
    public void setProxy(boolean mode, int sessionID) {
        ((ImgData)getItemData(sessionID)).setProxy(mode);
    }
    public void setProxy(boolean mode, SessionObject session) {
        ((ImgData)getItemData(session)).setProxy(mode);
    }
    /**  Proxy用 イメージ幅設定
        @param size -1:そのまま（デフォルト） 0:端末幅に合わせる 1～：指定サイズ表示
    */
    public void setProxyWidth(int size) {
        ((ImgData)itemData).setProxyWidth(size);
    }
    public void setProxyWidth(int size, int sessionID) {
        ((ImgData)getItemData(sessionID)).setProxyWidth(size);
    }
    public void setProxyWidth(int size, SessionObject session) {
        ((ImgData)getItemData(session)).setProxyWidth(size);
    }
    /**  Proxy用 イメージ比率設定
        @param ratio 0:何もしない 80:80%表示  100：端末幅に合わせる
    */
    public void setImageRatio(int ratio) {
        ((ImgData)itemData).setImageRatio(ratio);
    }
    public void setImageRatio(int ratio, int sessionID) {
        ((ImgData)getItemData(sessionID)).setImageRatio(ratio);
    }
    public void setImageRatio(int ratio, SessionObject session) {
        ((ImgData)getItemData(session)).setImageRatio(ratio);
    }
    /**  バーコードモード設定
        @param mode 0:バーコード表示しない BarCode.JAN8/QR
    */
    public void setBarCodeMode(int mode) {
        if (mode > 0) setCloneable(true);
        ((ImgData)itemData).setBarCodeMode(mode);
    }
    public void setBarCodeMode(int mode, int sessionID) {
        ((ImgData)getItemData(sessionID)).setBarCodeMode(mode);
    }
    public void setBarCodeMode(int mode, SessionObject session) {
        ((ImgData)getItemData(session)).setBarCodeMode(mode);
    }
    /**  バーコードバージョン設定(現在QRのみ）
        @param version 1- 9
    */
    public void setBarCodeVersion(int version) {
        if (version > 0) setCloneable(true);
        ((ImgData)itemData).setBarCodeVersion(version);
    }
    public void setBarCodeVersion(int version, int sessionID) {
        ((ImgData)getItemData(sessionID)).setBarCodeVersion(version);
    }
    public void setBarCodeVersion(int version, SessionObject session) {
        ((ImgData)getItemData(session)).setBarCodeVersion(version);
    }

    //---------------------------------------------------------------------
    // データ取得メソッド
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() {
        return ((ImgData)itemData).getValue();
    }
    
    public CharArrayQueue getValue(int sessionID) {
        return ((ImgData)getItemData(sessionID)).getValue();
    }
    public CharArrayQueue getValue(SessionObject session) {
        return ((ImgData)getItemData(session)).getValue();
    }

    public CharArray getSrc() {
        return ((ImgData)itemData).getSrc();
    }
    public CharArray getSrc(int sessionID) /*throws Exception*/ {
        //return ((ImgData)getItemData(sessionID)).getSrc();
        ImgData id = (ImgData)getItemData(sessionID);
        if (id == null) {
            //throw new Exception("ImgItem Exception");
        }
        return id.getSrc();
    }
    public CharArray getSrc(SessionObject session) /*throws Exception*/ {
        //return ((ImgData)getItemData(sessionID)).getSrc();
        ImgData id = (ImgData)getItemData(session);
        if (id == null) {
            //throw new Exception("ImgItem Exception");
        }
        return id.getSrc();
    }
 
    public boolean getProxy() {
        return ((ImgData)itemData).getProxy();
    }
    public boolean getProxy(int sessionID) {
        return ((ImgData)getItemData(sessionID)).getProxy();
    }
    public boolean getProxy(SessionObject session) {
        return ((ImgData)getItemData(session)).getProxy();
    }
 
    public int getProxyWidth() {
        return ((ImgData)itemData).getProxyWidth();
    }
    public int getProxyWidth(int sessionID) {
        return ((ImgData)getItemData(sessionID)).getProxyWidth();
    }
    public int getProxyWidth(SessionObject session) {
        return ((ImgData)getItemData(session)).getProxyWidth();
    }
 
    public int getBarcodeMode() {
        return ((ImgData)itemData).getBarcodeMode();
    }
    public int getBarcodeMode(int sessionID) {
        return ((ImgData)getItemData(sessionID)).getBarcodeMode();
    }
    public int getBarcodeMode(SessionObject session) {
        return ((ImgData)getItemData(session)).getBarcodeMode();
    }
 
    //---------------------------------------------------------------------
    // レンダリング
    //---------------------------------------------------------------------
    public CharArray getTag() {
        return ((ImgData)itemData).getTag();
    }
    public CharArray getTag(int sessionID) {
        return ((ImgData)getItemData(sessionID)).getTag();
    }
    public CharArray getTag(SessionObject session) {
        return ((ImgData)getItemData(session)).getTag();
    }
 
    public CharArray getSrcTag() {
        return ((ImgData)itemData).getSrcTag();
    }
    public CharArray getSrcTag(int sessionID) {
        return ((ImgData)getItemData(sessionID)).getSrcTag();
    }
    public CharArray getSrcTag(SessionObject session) {
        return ((ImgData)getItemData(session)).getSrcTag();
    }
 
    public CharArray getProxyTag() {
        return ((ImgData)itemData).getProxyTag();
    }
    public CharArray getProxyTag(int sessionID) {
        return ((ImgData)getItemData(sessionID)).getProxyTag();
    }
    public CharArray getProxyTag(SessionObject session) {
        return ((ImgData)getItemData(session)).getProxyTag();
    }
 
 
    //---------------------------------------------------------------------
    // copy ユーザーオブジェクトを作成する
    //---------------------------------------------------------------------
    public void copy(SessionObject session) {
        if (session.getModuleManager().imageProxy) {
            setCloneable(true);
        }
        if (isCloneable()) {
            ItemData newData = new ImgData((ImgData)itemData, session);
            newData.setItem(this);
            session.getHashtable().put(new IntObject(getMixedID()),newData);
        }
    }
    
    //---------------------------------------------------------------------
    // draw
    //---------------------------------------------------------------------
    /** レンダリングする
        @param session セッションオブジェクト
        @return 出力先
    */
    public CharArray draw(SessionObject session) {
        ImgData data = (ImgData)getItemData(session);
        return data.draw(session);
    }
    
    // stream 版
    //public void draw(OutputStream out) {
        //未作成
    //}
    public void draw(OutputStream out, int sessionID) {
        //未作成
    }

    /**
      コンバートを行う
    */
    public void convert(SessionObject session) {
        CharArray org = getSrc();
        if (org.length() > 0) {
            CharArray ch = null;
            if (langConvert) {
                ch = session.getMessage(org.toString());
                if (ch != null) setSrc(ch,session);
            }
            if (colorConvert && ch == null) {
                ch = session.getColor(org.toString());
                if (ch != null) setSrc(ch,session);
            }
        }
    }
}

//
//
// [end of ImgItem.java]
//

