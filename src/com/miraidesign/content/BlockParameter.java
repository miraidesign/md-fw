//------------------------------------------------------------------------
// @(#)BlockParameter.java
//              ブロック用情報を保管する
//              Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import java.util.Hashtable;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;


/**
 *  BlockParameter ブロックカウンター情報を保管する
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class BlockParameter extends Hashtable {
    static private boolean debug = (SystemConst.debug && false);    // デバッグ表示
    static private boolean debugBlock = (SystemConst.debug && false);
    //static private boolean debugTemplate = (SystemConst.debug && false);

    private String nameSpace = "";   // ネームスペース

    /** ネームスペースを設定する（デフォルト""） 
        @param str ネームスペース
    **/
    public void setNameSpace(String str) { this.nameSpace = str;}
    public String getNameSpace() { return nameSpace;}
    
    private  Hashtable<CharArray,BlockInfo> hash;          // ブロックカウンターの保管
                                        // CharArray key : BlockInfo
    
    //------------------------
    // constructor
    //------------------------
    /** デフォルトコンストラクタ */
    public BlockParameter() { super();}

    /** 指定ネームスペースで生成する 
        @param nameSpace ネームスペース
    */
    public BlockParameter(String nameSpace) {
        super();
        this.nameSpace = nameSpace;
    }
    // copy 後で実装すること（コピーコンストラクタも）
    //public void copy(BlockParameter from) {
    // 
    //}
    
    //-----------------------------------------------
    // method
    //-----------------------------------------------
    public Hashtable<CharArray,BlockInfo>   getBlockTable() { 
        if (hash == null) hash = new Hashtable<CharArray,BlockInfo>();
        return hash;
    }
    
    /** ブロック情報を取得する 
        @param  key    ブロックキー
        @return ブロックカウント  -1: 存在しない
    */
    public int getBlock(CharArray key) {
        int sts = -1;
        if (hash != null) {
            BlockInfo obj = (BlockInfo)hash.get(key);
            if (obj != null) sts = obj.count;
        }
        return sts;
    }
    public int getBlock(String str) {
        CharArray key = CharArray.pop(str);
        int sts = getBlock(key);
        CharArray.push(key); 
        return sts;
    }

    /** ブロック開始情報を取り出す  カウンターをデクリメントする
        @param  key    ブロックキー
        @return デクリメントしカウンタ -1: 存在しない
    */
    public int startBlock(CharArray key) {
        int sts = -1;
        if (hash != null) {
            BlockInfo obj = (BlockInfo)hash.get(key);
            if (obj != null) {
                sts = obj.count;
                if (sts > -1) obj.count = sts - 1;
            }
        }
        return sts;
    }
    public int startBlock(String str) {
        CharArray key = CharArray.pop(str);
        int sts = startBlock(key);
        CharArray.push(key);
        return sts;
    }
    
    /** ブロック終了情報を取り出す  ０でカウンターをリセットする
        @param  key    ブロックキー
        @return ブロックカウンタ -1: 存在しない
    */
    public int endBlock(CharArray key) {
        int sts = -1;
        if (hash != null) {
            BlockInfo obj = (BlockInfo)hash.get(key);
            if (obj != null) {
                sts = obj.count;
                if (sts == 0) obj.count = obj.max;
            }
        }
        return sts;
    }
    public int endBlock(String str) {
        CharArray key = CharArray.pop(str);
        int sts = endBlock(key);
        CharArray.push(key);
        return sts;
    }
    
    /** ブロックカウンタに繰返し回数をセットする <br>
        （count=0 でそのブロックの描画が行われなくなる）
        @param  key    ブロックキー
        @param count    繰返し回数
    */
    public void setBlock(CharArray key, int count) {
        if (hash == null) hash = new Hashtable<CharArray,BlockInfo>();
        BlockInfo obj = (BlockInfo)hash.get(key);
if (debugBlock) System.out.println(key+":"+count+" BlockInfo:"+(obj!=null));
        if (obj != null) {
            obj.max  = count;
            obj.count = count;
        } else {
            obj = new BlockInfo(count);
            hash.put(new CharArray(key),obj);
        }
    }
    public void setBlock(String str, int count) {
        CharArray key = CharArray.pop(str);
        setBlock(key, count);
        CharArray.push(key);
        
    }
    
    /**
        ブロック情報を全て削除する<br>
        （テンプレートファイルに記述された情報を使用する事になる）
    */
    public void clearBlock() {
        if (hash != null) {
            hash.clear();
        }
    }
    /**
        ブロック情報を削除する<br>
        （テンプレートファイルに記述された情報を使用する事になる）
        @param  key    ブロックキー
    */
    public void clearBlock(CharArray key) {
        if (hash != null) {
            hash.remove(key);
        }
    }
    public void clearBlock(String key) {
        CharArray ch = CharArray.pop(key);
        clearBlock(ch);
        CharArray.push(ch);
    }
    
    /** ブロックカウントをを返す
        @param  key    ブロックキー
        @return カウント -1: 存在しない
    */
    public int count(CharArray key) {
        int sts = -1;
        if (hash != null) {
            BlockInfo obj = (BlockInfo)hash.get(key);
            if (obj != null) {
                sts = obj.count;
            }
        }
        return sts;
    }
    public int count(String str) {
        CharArray key = CharArray.pop(str);
        int sts = count(key);
        CharArray.push(key);
        return sts;
    }
    
    /** ブロック最大値をを返す
        @param  key    ブロックキー
        @return 最大値 -1: 存在しない
    */
    public int max(CharArray key) {
        int sts = -1;
        if (hash != null) {
            BlockInfo obj = (BlockInfo)hash.get(key);
            if (obj != null) {
                sts = obj.max;
            }
        }
        return sts;
    }
    public int max(String str) {
        CharArray key = CharArray.pop(str);
        int sts = max(key);
        CharArray.push(key);
        return sts;
    }
    
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    /**
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            super.writeObject(out);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            super.readObject(in);
        }
    }
    **/
}

//
// [end of BlockParameter.java]
//

