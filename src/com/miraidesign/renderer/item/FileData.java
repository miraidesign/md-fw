//------------------------------------------------------------------------
// @(#)FileData.java
//                 [input type= file] のデータを保管する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.item;

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.IntObject;
import com.miraidesign.session.SessionObject;

/**  [input type= file] のデータを保管する */
public class FileData extends ItemData {
    private CharArray   chValue = new CharArray();  // 入力文字列データ
    private CharArray   chOriginal = new CharArray();  // 保存用 v0.684
    
    private int size      = -1;  // デフォルト幅    -1: 指定なし
    private int maxLength = -1;  // 最大入力文字数  -1: 指定なし
    private int istyle    = -1;  // istyle          -1: 指定なし
    
    private CharArray path = new CharArray();   // 出力ファイルパス（絶対パス）    
    private CharArray contentType = new CharArray();
    private int rowCount    = 0;   // 読み込み行数
    
    private CharArray uploadFilename = new CharArray(); // アップロードされたファイル名
    private int       uploadStatus = 0; // アップデートステータス
                      // 0000 0001 成功で１
                      // 0000 0010 ファイル名切り捨てたら１
                      // 0000 0100 漢字ファイル名を変更したら１
    
    public void clearStatus() { uploadStatus = 0;}
    public void setStatus(int i) { uploadStatus |= i;}
    
    /**
        アップロードステータスを取得する<br>
        0000 0001 成功で１<br>
        0000 0010 ファイル名切り捨てたら１<br>
        0000 0100 漢字ファイル名を変更したら１<br>
    **/
    public int getStatus() { return uploadStatus;}
    
    /** 指定ビットをセットする*/
    public void setStatus(int i, boolean mode) {
        if (mode) {
            setStatus(i);
        } else {
            uploadStatus &= ~i;
        }
    }
    /** 指定ビットのステータスを返す*/
    public boolean getStatus(int i) {
        return ((uploadStatus & i) == i);
    }
    
    {
        caQueue.enqueue(chValue);
        type = FILE;
    }
    
    //---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    public FileData() { 
    }
    public FileData(FileData from, SessionObject session) {
        this.sessionObject = session;
        copy(from);
    }
    public FileData(String value) {
        setText(value); 
    }
    public FileData(CharArray value) {
        setText(value); 
    }
    
    //---------------------------------------------------------------------
    // setter
    //---------------------------------------------------------------------
    public void setValue(String[] values) {
        CharArray chTmp = CharArray.pop(chValue);
        chValue.reset();
        for (int i = 0; i < values.length; i++) {
            chValue.add(values[i]);
        }
        _changed = !chValue.equals(chTmp);
        CharArray.push(chTmp);
    }
    /** 入力文字をクリアする */
    public void clear() {
        chValue.clear();
        if (sessionObject == null || sessionObject.updateMode) chOriginal.clear();
        // 2010-12-01
        uploadFilename.clear();
        uploadStatus = 0;
        contentType.clear();
        rowCount    = 0;   // 読み込み行数
    }
    
    /** テキスト情報をセットする 
        @param value テキスト情報
    */
    public void setText(String value) {
        chValue.set(value);
        if (sessionObject == null || sessionObject.updateMode) chOriginal.set(value);
    }
    /** テキスト情報をセットする 
        @param value テキスト情報
    */
    public void setText(CharArray value) {
        chValue.set(value);
        if (sessionObject == null || sessionObject.updateMode) chOriginal.set(value);
    }
    /** 表示幅を設定する 
        @param size 表示サイズ
    */
    public void setSize(int size) { this.size = size; }
    
    /** 入力最大文字数を指定する 
        @param maxLength サイズ
    */
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    
    /** istyle の設定 
       @param istyle Item.HIRAGANA/KATAKANA/ALPHABET/NUMERIC から選択
    */
    public void setIstyle(int istyle) { this.istyle = istyle; }
    
    /** ファイルパスを設定する
        @param path ファイルパス（フルパス指定）etc: /usr/data/
    */
    public void setPath(String path) {
        this.path.set(path);
    }
    
    /** ファイルパスを設定する
        @param path ファイルパス（フルパス指定）
    */
    public void setPath(CharArray path) {
        this.path.set(path);
    }
    
    /** アップロードされたファイル名を指定 <br>
        ※アプリケーションは使用しない
    */
    public void setUploadFilename(String name) {
        uploadFilename.set(name);
    }
    /** アップロードされたファイル名を指定 <br>
        ※アプリケーションは使用しない
    */
    public void setUploadFilename(CharArray name) {
        uploadFilename.set(name);
    }
    
    public void setContentType(String type) {
        this.contentType.set(type);
    }
    public void setContentType(CharArray type) {
        this.contentType.set(type);
    }
    
    public void setRowCount(int count) {
        rowCount = count;
    }
    //---------------------------------------------------------------------
    // getter
    //---------------------------------------------------------------------
    public CharArrayQueue getValue() { return caQueue; } 

    /** 入力文字を取得する */
    public CharArray getText() { return chValue; } 
        // ※注：String で取り出すときは getValue.toString() とする
        //       ただし、Stringの多用はシステムに負担を与えるため、
        //       なるべく使用しないこと

    /** 表示幅を指定する */
    public int getSize()      { return size; }

    /** 入力最大文字数を指定する */
    public int getMaxLength() { return maxLength; }
    
    /** istyle を取得する */
    public int getIstyle() { return istyle; }

    /** ファイル転送パスを取得する */
    public CharArray getPath() {
        return path;
    }

    /** 
        オリジナルから変更されているか？ <br>
        ※FileData では通常無意味なので使用しない事
    */
    public boolean updated() {
        return !chValue.equals(chOriginal);
    }
    
    /** 出力ファイル名を取り出す 
        @return 出力ファイル名（フルパス）
    */
    public CharArray getOutputFilename() {
        CharArray ch = new CharArray(path);
        CharArray tmp = CharArray.pop(uploadFilename);
        tmp.replace('\\','/');   // パスの変換
        int index = tmp.lastIndexOf('/');
        ch.add(tmp, index+1);
        CharArray.push(tmp);
        return ch;
    }
    
    /** 出力ファイル名（オリジナル）を取り出す 
        @return 出力ファイル名（フルパス）
    */
    public CharArray getOriginalOutputFilename() {
        CharArray ch = new CharArray(path);
        CharArray tmp = CharArray.pop(chValue);
        tmp.replace('\\','/');  // パスの変換
        int index = tmp.lastIndexOf('/');
        ch.add(tmp, index+1);
        CharArray.push(tmp);
        return ch;
    }
    
    /** ファイル名を取り出す
        @return 出力ファイル名
    */
    public CharArray getFilename() {
        CharArray ch = new CharArray();
        CharArray tmp = CharArray.pop();
        tmp.set((chValue.length()<=0)? uploadFilename : chValue);
        tmp.replace('\\','/');  // パスの変換
        int index = tmp.lastIndexOf('/');
        ch.add(tmp, index+1);
        CharArray.push(tmp);
        return ch;
    }

    /** ファイル拡張子を取り出す
        @return 出力ファイル名
    */
    public CharArray getFileExt() {
        CharArray ch = new CharArray();
        int index = chValue.lastIndexOf('.');
        if (index >= 0) ch.add(chValue, index);
        return ch;
    }
    public CharArray getUploadFileExt() {
        CharArray ch = new CharArray();
        int index = uploadFilename.lastIndexOf('.');
        if (index >= 0) ch.add(uploadFilename, index);
        return ch;
    }
    public CharArray getUploadFilename() { return uploadFilename; }
    public CharArray getContentType() { return contentType;}
    public int getRowCount() { return rowCount;}
    //---------------------------------------------------------------------
    // copy / clone 
    //---------------------------------------------------------------------
    /** 元オブジェクトより全データをコピーする */
    public void copy(FileData from) {
        super.copy(from);
        chValue.set(from.chValue);
        chOriginal.set(from.chOriginal);
        size      = from.size;
        maxLength = from.maxLength;
        istyle    = from.istyle;
        path.set(from.path);
        contentType.set(from.contentType);
        rowCount = from.rowCount;
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
            session.itemRenderer.drawFile(session.getBuffer(),this);
        }
        return session.getBuffer();
    }
    /** レンダラー用のタグを取得する （全て）*/
    public CharArray getTag(CharArray ch) {
        return sessionObject.itemRenderer.drawFile(ch,this);
    }
    /** レンダラー用のタグを取得する (name以降) */
    public CharArray getNameTag(CharArray ch) {
        return sessionObject.itemRenderer.drawFileName(ch,this);
    }
    /** レンダラー用のタグを取得する (name以降) */
    public CharArray getNameTag() {
        return getNameTag(new CharArray());
    }
    
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        if (out != null) {
            chValue.writeObject(out);
            out.writeInt(size);
            out.writeInt(maxLength);
            out.writeInt(istyle);
            path.writeObject(out);
            contentType.writeObject(out);
            out.writeInt(rowCount);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        super.readObject(in);
        if (in != null) {
            chValue.readObject(in);
            size = in.readInt();
            maxLength = in.readInt();
            istyle = in.readInt();
            path.readObject(in);
            contentType.readObject(in);
            rowCount = in.readInt();
        }
    }

}

//
//
// [end of FileData.java]
//

