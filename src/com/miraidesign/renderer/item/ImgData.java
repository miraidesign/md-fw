//------------------------------------------------------------------------
// @(#)ImgData.java
//              <img >
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//

package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.image.ImageProxy;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/** imgタグの情報を保管する */
public class ImgData extends ItemData {
    private static boolean debug = (false & SystemConst.debug);
    private CharArray src = new CharArray();     // イメージのＵＲＩ
    private CharArray alt = new CharArray();     // 代替表示文字

    private int width  = 0;        // 幅 (０以下無効）
    private int height = 0;        // 高さ
    private int align  = 0;        // 位置情報 TOP/MIDDLE/BOTTOM/LEFT/RIGHT
    private int border = -1;       // 境界 ０以上有効
    {
        caQueue.enqueue(src);
        type = IMG;
    }
    
    protected boolean proxy = false;      // ImageProxyMode
    protected int proxyWidth = -1;        // Proxy 画面幅 -1:そのまま 0:端末に合わせる 
                                          //              1～:指定幅
    protected int imageRatio = 100;       // イメージ表示比率 /QRコードのバージョン
    private boolean convert = false;      // Proxyコンバートが終了したか？
    
    private int barcode = 0;              // バーコードモード BarCode.JAN8/QR
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public ImgData() { }
    public ImgData(ImgData from, SessionObject session) { 
        this.sessionObject = session;
        copy(from);
        ModuleManager mm = session.getModuleManager();
        if (!proxy) proxy = mm.imageProxy;
        if (proxyWidth == -1) proxyWidth = mm.imageWidth;
        if (imageRatio == 100) imageRatio = mm.imageRatio;
        
        if (proxy) {
            Item item = getItem();
            if (item != null && item.getType() == Item.IMG) {
                item.setCloneable(true);
            }
        }
    }
    public ImgData(String src) {
        setSrc(src);
    }
    public ImgData(CharArray src) {
        setSrc(src);
    }
    
    public void setSessionObject(SessionObject session) {
        super.setSessionObject(session);
        ModuleManager mm = session.getModuleManager();
        if (!proxy) proxy = mm.imageProxy;
        if (proxyWidth == -1) proxyWidth = mm.imageWidth;
        if (imageRatio == 100) imageRatio = mm.imageRatio;
    }
    /** 設定をクリアする(src/alt) */
    public void clear() {
       src.clear();
       alt.clear();
    }
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setValue(String[] strs) {src.set(strs[0]);}
    /** @deprecated */
    public void setValue(String str) {src.set(str);}
    /** @deprecated */
    public void setValue(CharArray ch) {src.set(ch);}
    
    /** 文字列の設定 */
    public void setSrc(String str) {
        src.set(str);
        if (debug) System.out.println("ImgData.setSrc:"+str);
    }
    public void setSrc(CharArray ch) {
        src.set(ch);
        if (debug) System.out.println("ImgData.setSrc:"+ch);
    }
    /** 代替表示文字 */
    public void setAlt(String str) {alt.set(str);}
    public void setAlt(CharArray ch) {alt.set(ch);}
    /* width 設定 */
    public void setWidth(int size)  { this.width = size;}
    /* height 設定 */
    public void setHeight(int size) { this.height = size;}
    /* align 設定 */
    public void setAlign(int align) { this.align = align;}
    /* border 設定 */
    public void setBorder(int size) { this.border = size;}

    /** Image Proxy 設定 */
    public void setProxy(boolean mode) { proxy = mode; }
    
    /**  Proxy用 イメージ幅設定
        @param size -1:そのまま（デフォルト） 0:端末幅に合わせる 1～：指定サイズ表示
    */
    public void setProxyWidth(int size) { proxyWidth = size; }
    
    /**  Proxy用 イメージ比率設定
        @param ratio 0:何もしない  100：幅に合わせる
    */
    public void setImageRatio(int ratio) { imageRatio = ratio; }
    
    
    /** BarCode 設定 */
    public void setBarCodeMode(int mode) {
        barcode = mode;
        if (mode > 0) {
            proxy = true;
            proxyWidth = -1;
            imageRatio = 0; // version: auto
        } else {
            imageRatio = 0;
        }
    }
    /** BarCodeバージョン設定（QRのみ 0[auto]-40） */
    public void setBarCodeVersion(int version) {
        if (version >= 0 && version <= 40) {
            imageRatio = version;
        }
    }
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; }
    
    public CharArray getSrc() { return src; }
    public CharArray getAlt() { return alt; }

    public int getWidth()  { return width; }
    public int getHeight() { return height; }
    public int getAlign()  { return align; }
    public int getBorder() { return border; }
    public boolean getProxy() { return proxy; }
    public int getProxyWidth() { return proxyWidth;}
    public int getImageRatio() { return imageRatio;}
    public int getBarcodeMode() { return barcode;}
    public int getBarCodeVergion() { return imageRatio;}
    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    public void copy(ImgData from) { // 元オブジェクトより全データをコピー
        super.copy(from);
        src.set(from.src);
        alt.set(from.alt);
        this.width = from.width;
        this.height = from.height;
        this.align = from.align;
        this.border = from.border;
        this.proxy = from.proxy;
        this.proxyWidth = from.proxyWidth;
        this.imageRatio = from.imageRatio;
        this.convert = from.convert;
        this.barcode = from.barcode;
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
            CharArray tmp = CharArray.pop();
            tmp.set(src);  // 元データを保管しておく
            setProxy();
            session.itemRenderer.drawImg(session.getBuffer(),this);
            src.set(tmp);   // データを戻す
            CharArray.push(tmp);
        }
        return session.getBuffer();
    }
    public CharArray getTag() {
        return getTag(CharArray.pop());
    }
    public CharArray getTag(CharArray ch) {
        CharArray tmp = CharArray.pop();
        tmp.set(src);  // 元データを保管しておく
        setProxy();
        CharArray ret = sessionObject.itemRenderer.drawImg(ch,this);
        src.set(tmp);   // データを戻す
        CharArray.push(tmp);
        return ret;
    }
    // src=".." を返す
    public CharArray getSrcTag() {
        return getSrcTag(CharArray.pop());
    }
    public CharArray getSrcTag(CharArray ch) {
        CharArray tmp = CharArray.pop();
        tmp.set(src);  // 元データを保管しておく
        setProxy();
        CharArray ret = sessionObject.itemRenderer.drawImgSrc(ch,this);
        src.set(tmp);   // データを戻す
        CharArray.push(tmp);
        return ret;
    }
    // 2010-07-08  src の中身を返す
    public CharArray getProxyTag() {
        return getProxyTag(CharArray.pop());
    }
    public CharArray getProxyTag(CharArray ch) {
        CharArray tmp = CharArray.pop();
        tmp.set(src);  // 元データを保管しておく
        setProxy();
        CharArray ret = new CharArray(src);
        src.set(tmp);   // データを戻す
        CharArray.push(tmp);
        return ret;
    }

    private void setProxy() {
if (debug) {
    System.out.println("setProxy:proxy:"+proxy+" src:"+src+" barcode:"+barcode);
if (sessionObject != null) System.out.println("sessionID:"+sessionObject.getSessionID());
else System.out.println("session is null");
}
        if (proxy && sessionObject != null/* && !convert && !session.isPC()*/) {
            Item orgItem =  getItem();
            CharArray ch = null;
            if (orgItem != null && orgItem instanceof ImgItem) {
                ch = ((ImgItem)orgItem).getSrc();   // オリジナルのソースを取得
            }
            
            if (ch == null || ch.trim().length()==0) ch = getSrc();
            CharArray url = null;
if (debug) System.out.println("ch:"+ch);

            String docRoot = ((ImgItem)getItem()).getDocRoot();
            if (barcode > 0) {
                url = ImageProxy.getBarcodeURL(docRoot, ch,sessionObject, barcode, imageRatio);
            } else {
                url = ImageProxy.getURL(docRoot, ch,sessionObject,proxyWidth,imageRatio);
            }
if (debug) {
    System.out.println("☆★setProxy:"+ch+" -> "+url);
    System.out.println("   Cloneable:"+item.isCloneable());
    if (sessionObject != null) System.out.println("sessionID:"+sessionObject.getSessionID());
    else System.out.println("session is null");
}
            if (sessionObject.getProxyDebug() && url != null) {
                File file = new File(ImageProxy.getRootDir()+url);
                int size = 0;
                if (file.exists()) {
                    size = (int)file.length();
                }
                sessionObject.addProxyMessage(url, size);
            }
            if (url != null) src.set(url);
            convert = true;
        }
    }
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            src.writeObject(out);
            alt.writeObject(out);
            out.writeInt(width);
            out.writeInt(height);
            out.writeInt(align);
            out.writeInt(border);
            
            out.writeBoolean(proxy);
            out.writeInt(proxyWidth);
            out.writeBoolean(convert);
            
            out.writeInt(imageRatio);
            
            out.writeInt(barcode);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            src.readObject(in);
            alt.readObject(in);
            width  = in.readInt();
            height = in.readInt();
            align  = in.readInt();
            border = in.readInt();
            
            proxy = in.readBoolean();
            proxyWidth = in.readInt();
            convert = in.readBoolean();
            imageRatio = in.readInt();
            barcode = in.readInt();
        }
    }
}

//
// [end of ImgData.java]
//

