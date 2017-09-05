//------------------------------------------------------------------------
// @(#)BlockEnd.java
//              ブロック終了ダミークラス
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

public class BlockEnd implements ContentItem {
    private Block block;    // 親ブロック
    private static CharArray chType = new CharArray("BLOCK_END");
    
    public boolean isBlock() {return true;}
    public boolean isInput() {return false;}
    public boolean isTemplate() {return false;}
    
    /** 親ブロックを取得する 
        @return Block
    */ 
    public Block getBlock() {return block;}

    public ContentItemNode getContentItemNode() { return null;}
    
    //////////////////////////////////////////////////////////////////
    /** ラベルを取得する 
        @return ラベル
    */
    public CharArray getLabel() { return block.getLabel(); } 
    
    /** ラベルを取得する 
        @return ラベル
    */
    public CharArray getKey() { return block.getKey(); } 
    
    
    /** コメントを取得する 
        @return コメント
    */
    public CharArray getDescription() { return block.getDescription(); }
    
    public CharArray getType() { return chType; }
    
    /** オリジナルアイテムを取得する 
        @return ContentItem
    */
    public ContentItem getOriginalItem() { return this; }
    //////////////////////////////////////////////////////////////////
    
    /** コンストラクタ
        @param block block実体を設定
    */
    public BlockEnd(Block block) {
        this.block = block;
    }
    
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    /* @deprecated */
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
        }
    }
    /* @deprecated */
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
        }
    }

}

//
//
// [end of BlockEnd.java]
//

