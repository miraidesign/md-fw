//------------------------------------------------------------------------
// @(#)ContentBlock.java
//              HTML ブロック管理
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import java.util.Hashtable;
import java.util.Stack;

import com.miraidesign.common.SystemConst;
import com.miraidesign.renderer.Page;
import com.miraidesign.system.SystemManager;
import com.miraidesign.servlet.ServletLog;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.ObjectQueue;
import com.miraidesign.util.IntObject;

/**
 *  ContentBlock
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ContentBlock {
    private boolean debug = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugParam = (SystemConst.debug && false);  // パラメータのデバッグ

    private ObjectQueue queue = new ObjectQueue();  // Blockを保管
    
    /** 指定位置のブロックを取得 
         @param index 指定位置
        @return Block
    */
    public Block getBlock(int index) {
        return (Block)queue.peek(index);
    }
    /** 指定キーワードのブロックリストを取得 
        @param key キーワード
        @return BLOCK リスト 存在しない場合は size()==0
    */
    public ObjectQueue getBlock(CharArray key) {
        ObjectQueue _queue = new ObjectQueue();
        for (int i = 0; i < queue.size(); i++) {
            Block block = (Block)queue.peek(i);
            if (block.getKey().equals(key)) _queue.enqueue(block);
        }
        return _queue;
    }
    
    public ObjectQueue getBlock(String key) {
        CharArray ch = CharArray.pop(key);
        ObjectQueue _queue = getBlock(ch);
        CharArray.push(ch);
        return _queue;
    }
    //--------------------------------------------
    private Stack<IntObject> stack = new Stack<IntObject>();              // ネスト管理用
    
    private static final String szKey   = "KEY";
    private static final String szMax   = "MAX";
    private static final String szMode  = "MODE";
    private static final String szLimit = "LIMIT";
    private static final String szLabel = "LABEL";
    private static final String szDescription = "DESCRIPTION";
    
    private Page page;          // 使用しないかも
    
    /** constructor */
    public ContentBlock() {
        debug &= SystemConst.debug;
    }
    public ContentBlock(Page page) {
        this.page = page;
        debug &= SystemConst.debug;
    }
    public void clear() {
        queue.clear();
        stack.clear();
    }
    /**
        ファイルをパースしてスクリプトを抜き出す
        @param chars 文字配列
        @param start 開始位置
        @param middle 中間
        @param size  サイズ
        @param parseErrMsg エラーメッセージバッファ
        @return index
    */
    public int parse(char[] chars, int start, int middle, int size, CharArray parseErrMsg) {
        int sts = -1;
        if (debug) System.out.println("%%%% parse block start %%%%%%%%");
        CharArray key = new CharArray(); key.setAppendMode(true);
        CharArray max = CharArray.pop();
        CharArray limit = CharArray.pop();
        CharArray label = CharArray.pop();
        CharArray description = CharArray.pop();
        
        CharArray ch = CharArray.pop();     // タグ内容を移しておく
        ch.set(chars, start,middle-start).toUpperCase();    // 大文字に変えておく

        // キー値取得
        int index = ch.indexOf(szKey);
        if (index >= 0) {
            index = ch.indexOf('=',index+szKey.length());
            if (index >= 0 && middle > index) {
                key.set(chars,start+index+1,middle-start-index-1); 
                key.trim();
if (debug) System.out.print("key["+key+"]");
                for (int i = 0; i < key.length; i++) {
                    if (key.chars[i] <= ' ') {
                        key.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                key.replace("\"","");   // ダブルコーテーションを削除
                key.replace("\'","");   // シングルコーテーションを削除
if (debug) System.out.println("->["+key+"]");
            }
        }
        if (key.length() == 0) {
            key.set("$blk$");
            key.format(queue.size(),10,3,'0');
if (debug) System.out.println("            ->["+key+"]");
        }
        
        // ＭＡＸ値取得
        index = ch.indexOf(szMax);
        if (index >= 0) {
            index = ch.indexOf('=',index+szMax.length());
            if (index >= 0 && middle > index) {
                max.set(chars,start+index+1,middle-start-index-1); 
                max.trim();
if (debugParam) System.out.print("max["+max+"]");
                for (int i = 0; i < max.length; i++) {
                    if (max.chars[i] <= ' ') {
                        max.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                max.replace("\"","");
                max.replace("\'","");
if (debugParam) System.out.println("->["+max+"]");
            }
        }
        // Limit値取得 
        index = ch.indexOf(szLimit);
        if (index >= 0) {
            index = ch.indexOf('=',index+szLimit.length());
            if (index >= 0 && middle > index) {
                limit.set(chars,start+index+1,middle-start-index-1); 
                limit.trim();
if (debugParam) System.out.print("limit["+limit+"]");
                for (int i = 0; i < limit.length; i++) {
                    if (limit.chars[i] <= ' ') {
                        limit.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                limit.replace("\"","");
                limit.replace("\'","");
if (debugParam) System.out.println("->["+limit+"]");
            }
        }
        // label取得 
        index = ch.indexOf(szLabel);
        if (index >= 0) {
            index = ch.indexOf('=',index+szLabel.length());
            if (index >= 0 && middle > index) {
                label.set(chars,start+index+1,middle-start-index-1); 
                label.trim();
if (debugParam) System.out.print("label["+label+"]");
                for (int i = 0; i < label.length; i++) {
                    if (label.chars[i] <= ' ') {
                        label.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                label.replace("\"","");
                label.replace("\'","");
if (debugParam) System.out.println("->["+label+"]");
            }
        }
        // description取得 
        index = ch.indexOf(szDescription);
        if (index >= 0) {
            index = ch.indexOf('=',index+szDescription.length());
            if (index >= 0 && middle > index) {
                description.set(chars,start+index+1,middle-start-index-1); 
                description.trim();
if (debugParam) System.out.print("description["+description+"]");
                for (int i = 0; i < description.length; i++) {
                    if (description.chars[i] <= ' ') {
                        description.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                description.replace("\"","");
                description.replace("\'","");
if (debugParam) System.out.println("->["+description+"]");
            }
        }
        
        //------------------------------------------------
        Block block = new Block();
        block.setKey(key);
        block.setStart(size);
        if (max.length() > 0)  block.setMax(max.getInt());
        if (limit.length() > 0) block.setLimit(limit.getInt());
        if (label.length() > 0) block.setLabel(label.toString());
        if (description.length() > 0) block.setDescription(description.toString());

        if (queue.enqueue((Object)block)) {  // テンプレートを追加する
            sts = queue.size() - 1;
            stack.push(new IntObject(sts));
        }
        CharArray.push(ch);
        CharArray.push(description);
        CharArray.push(label);
        CharArray.push(limit);
        CharArray.push(max);
if (debug) System.out.println("%%%% parse block end  %%%%%%%% key="+key+" sts = "+sts);
        return sts;     // 登録されたBlockの位置を返す
    }
    
    public int parseEnd(int size, CharArray parseErrMsg) {
if (debug) System.out.println("%%%% parseEnd block start %%%%%%%%");
        int sts = -1;
        CharArray key = null;
        if (stack.size() == 0) {
            parseErrMsg.add("</block> 対応する開始ブロックが存在しません\n");
        
        } else {
            IntObject obj = (IntObject)stack.pop();
            if (obj != null) {
                int index = obj.getValue();
                Block block = (Block)queue.peek(index);
                if (block == null) {
                    SystemManager.log.out(ServletLog.BLOCK_ERROR," index="+index+" block not found !! size="+queue.size());
                    parseErrMsg.add("</block> 対応する開始ブロックが見つかりません\n");
                } else {
                    key = block.getKey();
                    block.setEnd(size);
                    sts = index;
                }
            }
        }
if (debug) System.out.println("%%%% parseEnd block end  %%%%%%%% key="+key+" sts = "+sts);
        return sts;
    }
    
    /**
        セッションに初期化処理 <br>
        BlockParameter の情報に追加する
        @param session SessionObject
    */
    public void init(SessionObject session) { // namespace対応
        init(session, "");
    }
    public void init(SessionObject session, String nameSpace) { // namespace対応
        if (debug) System.out.println("ContentBlock init(session)--");
        Hashtable<CharArray,BlockInfo> hash = session.getBlockTable(nameSpace);
        
        //for (Enumeration e = hash.keys(); e.hasMoreElements();) {
        //    CharArray key = (CharArray)e.nextElement();
        //    System.out.println("hash has ["+key+"]");
        //}
        
        for (int i = 0; i < queue.size(); i++) {
            Block block = (Block)queue.peek(i);
            CharArray key = block.getKey();
            
            BlockInfo obj = (BlockInfo)hash.get(key);
            if (obj == null) {  // 登録されていない
                if (debug) System.out.println("key:"+key+" 登録します max="+block.getMax());
                hash.put(key, new BlockInfo(block.getMax()));
            } else {
                int count = obj.max;
                if (count < 0) {
                    obj.max = obj.count = block.getMax();
                }
                if (debug) System.out.println("key:"+key+" 登録されています max="+count+"->"+obj.max);
            }
        }
        if (debug) System.out.println("ContentBlock init end--------");
    }
    
    /*
        描画処理
        
    */
    public int drawStart(SessionObject session, int index) {
        return drawStart(session, index, "");
    }
    public int drawStart(SessionObject session, int index, String nameSpace) {
        int sts = -1;
        Block block = (Block)queue.peek(index);
        if (block == null) {
            SystemManager.log.out(ServletLog.BLOCK_ERROR," index="+index+" block not found !! size="+queue.size());
        } else {
            CharArray key = block.getKey();
            int start = block.getStart();
            int end = block.getEnd();
            int max = block.getMax();
            int count = session.startBlock(key, nameSpace);
            
            if (debug) System.out.println("Block drawStart "+key+" start:"+start+" end:"+end+" count="+count);
            if (count <= 0 || max == 0) {  // 表示ブロックを抜ける
                sts = end + 1;
            }
        }
        return sts;
    }
    
    public int drawEnd(SessionObject session, int index) {
        return drawEnd(session, index, "");
    }
    public int drawEnd(SessionObject session, int index, String nameSpace) {
        int sts = -1;
        Block block = (Block)queue.peek(index);
        if (block == null) {
            SystemManager.log.out(ServletLog.BLOCK_ERROR," index="+index+" block not found !! size="+queue.size());
        } else {
            CharArray key = block.getKey();
            int start = block.getStart();
            int end = block.getEnd();
            int count = session.endBlock(key, nameSpace);
            if (debug) System.out.println("Block drawEnd   "+key+" start:"+start+" end:"+end+" count="+count);
            if (count > 0) {    // まだ繰り返す
                sts = start;
            }
        }
        
        return sts;
    }

}

//
// [end of ContentBlock.java]
//

