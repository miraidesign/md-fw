//------------------------------------------------------------------------
// @(#)Block.java
//              HTMLブロック制御情報
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.util.CharArray;

/**
 *  Block   フレームワークが使用します
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class Block implements ContentItem {
    private static CharArray chType = new CharArray("BLOCK");

    private int max = 1;
    private CharArray key;
    private int limit = 100;        // FWでは使用しない
    
    private CharArray chLabel       = new CharArray();  // FWでは使用しない
    private CharArray chDescription = new CharArray();  // FWでは使用しない
    
    /** 最大繰返し回数を設定する (デフォルト：1) 
      @param max 最大繰り返し数
    */
    public void setMax(int max) { this.max = max;}
    /** 最大繰り返し回数を返す 
      @return 最大繰り返し数
    */
    public int  getMax() { return this.max;}
    
    /** キーワードを設定する 
        @param key キーワード
    */
    public void setKey(CharArray key) { this.key = key; }
    /** キーワードを取得する 
        @return キーワード
    */
    public CharArray getKey() {return key;}
    
    private ContentItemNode node = null;
    public void setContentItemNode(ContentItemNode node) {
        this.node = node;
    }
    public ContentItemNode getContentItemNode() { return node;}
    //////////////////////////////////////////////////////////////////
    
    public boolean isBlock() {return true;}
    public boolean isInput() {return false;}
    public boolean isTemplate() {return false;}
    
    /** ＵＩでのリミット値のヒントを設定する（デフォルト：100）<br>
        FW では使用しません
        @param limit リミット値
    */
    public void setLimit(int limit) { this.limit = limit;}
    /** リミット値を返す 
        @return リミット智
    */
    public int getLimit() { return this.limit;}
    
    /** ラベル設定 (FWでは使用しない) 
        @param label ラベル
    */
    public void setLabel(String label) { chLabel.set(label);}
    public void setLabel(CharArray label) { chLabel.set(label);}
    /** ラベルを取得する 
        @return ラベル
    */
    public CharArray getLabel() { return chLabel; }
    
    /** コメント設定 (FWでは使用しない) 
        @param description コメント
    */
    public void setDescription(String description) { chDescription.set(description);}
    public void setDescription(CharArray description) { chDescription.set(description);}
    /** コメントを取得する 
        @return description コメント
    */
    public CharArray getDescription() { return chDescription; }
    
    /** タイプ文字列("BLOCK")を取得する 
      @return タイプ文字列
    */
    public CharArray getType() { return chType;}
    
    /** オリジナルアイテムを取得する 
        @return ContentItem
    */
    public ContentItem getOriginalItem() { return this; }
    
    private BlockEnd blockEnd = null;
    /**
        ダミーのブロック終端情報を取得
        @return BlockEnd
    */
    public BlockEnd getBlockEnd() {
        if (blockEnd == null) {
            blockEnd = new BlockEnd(this);
        }
        return blockEnd;
    }
    //////////////////////////////////////////////////////////////////
    
    /** 開始位置 */
    public int start = -1;
    public void setStart(int i) { start = i; }
    public int getStart() { return start; }
    
    /** 終了位置 */
    public int end = -1;
    public void setEnd(int i) { end = i; }
    public int getEnd() { return end; }
    
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    //@@ 足りない ！
    /* @deprecated */
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            out.writeInt(max);
            if (key != null) {
                out.writeBoolean(true);
                key.writeObject(out);
            } else {
                out.writeBoolean(false);
            }
        }
    }
    /* @deprecated */
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            max  = in.readInt();
            boolean b = in.readBoolean();
            if (b) {
                if (key == null) key = new CharArray();
                key.readObject(in);
            } else {
                key = null;
            }
        }
    }

}

//
//
// [end of Block.java]
//

