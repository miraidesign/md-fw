//------------------------------------------------------------------------
// @(#)FileItem.java
//                 input type = file
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.OutputStream;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.IntObject;
import com.miraidesign.session.SessionObject;

/**
 *  ファイル入力アイテム(input type=file)
**/
public class FileItem extends Item {

    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    /**
        FileItemオブジェクトを生成する
    */
    public FileItem() { 
        super();
        init();
        itemData = new FileData();
        itemData.setItem(this);
    }
    /**
        FileItemオブジェクトを生成する
        @param value デフォルト値
    */
    public FileItem(String value) { 
        super();
        init();
        itemData = new FileData(value);
        itemData.setItem(this);
    }
    /**
        FileItemオブジェクトを生成する
        @param value デフォルト値
    */
    public FileItem(CharArray value) { 
        super();
        init();
        itemData = new FileData(value);
        itemData.setItem(this);
    }
    /**
        FileItemオブジェクトを生成する(Copy constructor)
    */
    public FileItem(FileItem from) { 
        super();
        setType(FILE);
        setCloneable(from.isCloneable());
        FileData fromdata = (FileData)from.itemData;
        itemData = new FileData(fromdata, fromdata.getSessionObject());
        itemData.setItem(this);
    }

    //---------------------------------------------------------------------
    // initializer
    //---------------------------------------------------------------------
    private void init() {
        setType(FILE);
        setCloneable(true);
    }
    
    //---------------------------------------------------------------------
    // データ設定メソッド
    //---------------------------------------------------------------------
    /** テキスト情報をセットする 
        @param str テキスト情報
    */
    public  void setText(String str) {
        ((FileData)itemData).setText(str);
    }
    public  void setText(String str, int sessionID) {
        ((FileData)getItemData(sessionID)).setText(str);
    }
    public  void setText(String str, SessionObject session) {
        ((FileData)getItemData(session)).setText(str);
    }

    /** テキスト情報をセットする 
        @param ch テキスト情報
    */
    public  void setText(CharArray ch) {
        ((FileData)itemData).setText(ch);
    }
    public  void setText(CharArray ch, int sessionID) {
        ((FileData)getItemData(sessionID)).setText(ch);
    }
    public  void setText(CharArray ch, SessionObject session) {
        ((FileData)getItemData(session)).setText(ch);
    }

    /** 表示幅を設定する 
        @param size 表示サイズ
    */
    public void setSize(int size) {
        ((FileData)itemData).setSize(size);
    }
    public  void setSize(int size, int sessionID) {
        ((FileData)getItemData(sessionID)).setSize(size);
    }
    public  void setSize(int size, SessionObject session) {
        ((FileData)getItemData(session)).setSize(size);
    }

    /** 入力最大文字数を指定する 
        @param size 最大文字数
    */
    public void setMaxLength(int size) {
        ((FileData)itemData).setMaxLength(size);
    }
    public  void setMaxLength(int size, int sessionID) {
        ((FileData)getItemData(sessionID)).setMaxLength(size);
    }
    public  void setMaxLength(int size, SessionObject session) {
        ((FileData)getItemData(session)).setMaxLength(size);
    }
    /** istyleの設定 
        @param istyle Item.HIRAGANA/KATAKANA/ALPHABET/NUMERIC
    */
    public void setIstyle(int istyle) {
        ((FileData)itemData).setIstyle(istyle);
    }
    public  void setIstyle(int istyle, int sessionID) {
        ((FileData)getItemData(sessionID)).setIstyle(istyle);
    }
    public  void setIstyle(int istyle, SessionObject session) {
        ((FileData)getItemData(session)).setIstyle(istyle);
    }

    /** データのクリア*/
    public  void clear() {
        ((FileData)itemData).clear();
    }
    public  void clear(int sessionID) {
        ((FileData)getItemData(sessionID)).clear();
    }
    public  void clear(SessionObject session) {
        ((FileData)getItemData(session)).clear();
    }

    /** ファイル出力パスを設定する
        @param path 出力パス
    */
    public  void setPath(String path) {
        ((FileData)itemData).setPath(path);
    }
    public  void setPath(String path, int sessionID) {
        ((FileData)getItemData(sessionID)).setPath(path);
    }
    public  void setPath(String path, SessionObject session) {
        ((FileData)getItemData(session)).setPath(path);
    }
    
    /** ファイル出力パスを設定する
        @param path 出力パス
    */
    public  void setPath(CharArray path) {
        ((FileData)itemData).setPath(path);
    }
    public  void setPath(CharArray path, int sessionID) {
        ((FileData)getItemData(sessionID)).setPath(path);
    }
    public  void setPath(CharArray path, SessionObject session) {
        ((FileData)getItemData(session)).setPath(path);
    }

    /** コンテントタイプを設定する
        @param type コンテントタイプ
    */
    public  void setContentType(String type) {
        ((FileData)itemData).setContentType(type);
    }
    public  void setContentType(String type, int sessionID) {
        ((FileData)getItemData(sessionID)).setContentType(type);
    }
    public  void setContentType(String type, SessionObject session) {
        ((FileData)getItemData(session)).setContentType(type);
    }
    
    /** コンテントタイプを設定する
        @param type コンテントタイプ
    */
    public  void setContentType(CharArray type) {
        ((FileData)itemData).setContentType(type);
    }
    public  void setContentType(CharArray type, int sessionID) {
        ((FileData)getItemData(sessionID)).setContentType(type);
    }
    public  void setContentType(CharArray type, SessionObject session) {
        ((FileData)getItemData(session)).setContentType(type);
    }

    //---------------------------------------------------------------------
    // データ取得メソッド
    //---------------------------------------------------------------------
    /** デフォルト入力文字列を取得する
        @return 入力文字列 */
    public CharArrayQueue getValue() {
        return ((FileData)itemData).getValue();
    }
    
    /** ユーザー毎の入力文字列を取得する
        @param sessionID セッションＩＤ
        @return 入力文字列 */
    public CharArrayQueue getValue(int sessionID) {
        return ((FileData)getItemData(sessionID)).getValue();
    }
    public CharArrayQueue getValue(SessionObject session) {
        return ((FileData)getItemData(session)).getValue();
    }

    /** デフォルト入力文字列を取得する
        @return 入力文字列 */
    public CharArray getText() {
        return ((FileData)itemData).getText();
    }
        // ※注：String で取り出すときは getValue().toString() 等とする
        //       ただし、Stringの多用はシステムに負担を与えるため、
        //       なるべく使用しないこと
    
    /** ユーザー毎の入力文字列を取得する
        @param sessionID セッションＩＤ
        @return 入力文字列 */
    public CharArray getText(int sessionID) {
        return ((FileData)getItemData(sessionID)).getText();
    }
    public CharArray getText(SessionObject session) {
        return ((FileData)getItemData(session)).getText();
    }

    /** 出力パス取得する
        @return 出力パス */
    public CharArray getPath() {
        return ((FileData)itemData).getPath();
    }
    public CharArray getPath(int sessionID) {
        return ((FileData)getItemData(sessionID)).getPath();
    }
    public CharArray getPath(SessionObject session) {
        return ((FileData)getItemData(session)).getPath();
    }

    /** 出力ファイル名（フルパス）を取得する
        @return 出力ファイル名（フルパス） */
    public CharArray getOutputFilename() {
        return ((FileData)itemData).getOutputFilename();
    }
    public CharArray getOutputFilename(int sessionID) {
        return ((FileData)getItemData(sessionID)).getOutputFilename();
    }
    public CharArray getOutputFilename(SessionObject session) {
        return ((FileData)getItemData(session)).getOutputFilename();
    }

    /** 出力ファイル名を取得する
        @return 出力ファイル名 */
    public CharArray getFilename() {
        return ((FileData)itemData).getFilename();
    }
    public CharArray getFilename(int sessionID) {
        return ((FileData)getItemData(sessionID)).getFilename();
    }
    public CharArray getFilename(SessionObject session) {
        return ((FileData)getItemData(session)).getFilename();
    }

    /** ファイル拡張子を取得する
        @return 拡張子 */
    public CharArray getFileExt() {
        return ((FileData)itemData).getFileExt();
    }
    public CharArray getFileExt(int sessionID) {
        return ((FileData)getItemData(sessionID)).getFileExt();
    }
    public CharArray getFileExt(SessionObject session) {
        return ((FileData)getItemData(session)).getFileExt();
    }

    /** ファイル拡張子を取得する
        @return 拡張子 */
    public CharArray getUploadFileExt() {
        return ((FileData)itemData).getUploadFileExt();
    }
    public CharArray getUploadFileExt(int sessionID) {
        return ((FileData)getItemData(sessionID)).getUploadFileExt();
    }
    public CharArray getUploadFileExt(SessionObject session) {
        return ((FileData)getItemData(session)).getUploadFileExt();
    }

    /** コンテントタイプを取得する
        @return コンテントタイプ */
    public CharArray getContentType() {
        return ((FileData)itemData).getContentType();
    }
    public CharArray getContentType(int sessionID) {
        return ((FileData)getItemData(sessionID)).getContentType();
    }
    public CharArray getContentType(SessionObject session) {
        return ((FileData)getItemData(session)).getContentType();
    }

    /** アップロードされたファイル名を取得する
        @return アップロードファイル名 */
    public CharArray getUploadFilename() {
        return ((FileData)itemData).getUploadFilename();
    }
    public CharArray getUploadFilename(int sessionID) {
        return ((FileData)getItemData(sessionID)).getUploadFilename();
    }
    public CharArray getUploadFilename(SessionObject session) {
        return ((FileData)getItemData(session)).getUploadFilename();
    }

    /** 取得行数を取得する
        @return 行数 */
    public int getRowCount() {
        return ((FileData)itemData).getRowCount();
    }
    public int getRowCount(int sessionID) {
        return ((FileData)getItemData(sessionID)).getRowCount();
    }
    public int getRowCount(SessionObject session) {
        return ((FileData)getItemData(session)).getRowCount();
    }
    /** アップロードステータスを取得する<br>
        0000 0001 成功で１<br>
        0000 0010 ファイル名切り捨てたら１<br>
        0000 0100 漢字ファイル名を変更したら１<br>
        @return ステータス */
    public int getStatus() {
        return ((FileData)itemData).getStatus();
    }
    public int getStatus(int sessionID) {
        return ((FileData)getItemData(sessionID)).getStatus();
    }
    public int getStatus(SessionObject session) {
        return ((FileData)getItemData(session)).getStatus();
    }

    /** 指定ビットのアップロードステータスを返す*/
    public boolean getStatus(int bit, int sessionID) {
        return ((FileData)getItemData(sessionID)).getStatus(bit);
    }
    public boolean getStatus(int bit, SessionObject session) {
        return ((FileData)getItemData(session)).getStatus(bit);
    }

    //---------------------------------------------------------------------
    // copy 
    //---------------------------------------------------------------------
    /** デフォルトのオブジェクトをコピーしてユーザーオブジェクトを作成する
        @param session セッションオブジェクト
    */
    public void copy(SessionObject session) {
        if (isCloneable()) {
            ItemData newData = new FileData((FileData)itemData,session);
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
        FileData data = (FileData)getItemData(session);
        return data.draw(session);
    }
    // stream 版
    //public void draw(OutputStream out) {
        //未作成
    //}
    public void draw(OutputStream out, int sessionID) {
        //未作成
    }

    /** レンダラー用のタグを取得する (name以降) */
    public CharArray getNameTag(int sessionID) {
        return ((FileData)getItemData(sessionID)).getNameTag();
    }
    /** レンダラー用のタグを取得する (name以降) */
    public CharArray getNameTag(SessionObject session) {
        return ((FileData)getItemData(session)).getNameTag();
    }

    /**
      コンバートを行う
    */
    public void convert(SessionObject session) {
        CharArray org = getText();
        if (org.length() > 0) {
            CharArray ch = null;
            if (langConvert) {
                ch = session.getMessage(org.toString());
                if (ch != null) setText(ch,session.getSessionID());
            }
            if (colorConvert && ch == null) {
                ch = session.getColor(org.toString());
                if (ch != null) setText(ch,session.getSessionID());
            }
        }
    }

}

//
//
// [end of FileItem.java]
//

