//------------------------------------------------------------------------
// @(#)ContentScript.java
//              スクリプトパーサー＆トランスレーター
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import com.miraidesign.common.SystemConst;
import com.miraidesign.renderer.Page;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.ObjectQueue;

/**
 *  ContentScript
 *  
 *  @version 0.5
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ContentScript {
    static private boolean debug = (SystemConst.debug && false);  // デバッグ表示

    private ObjectQueue queue = new ObjectQueue();  // CharArrayQueue を保管する
    
    private Page page;
    
    public ContentScript() {
    }
    public ContentScript(Page page) {
        this.page = page;
    }
    
    public void setPage(Page page) {
        this.page = page;
    }
    
    public Page getPage() { return this.page; }
    
    public void clear() {
        queue.clear();
    }
    /*
        ファイルをパースしてスクリプトを抜き出す
    */
    public int parse(char[] chars, int offset, int size, CharArray parseErrMsg) {
        CharArray ch = CharArray.pop();
        ch.set(chars,offset,size);
        CharToken token = new CharToken(ch,";");
        queue.enqueue(token);
        CharArray.push(ch);
        return queue.size();
    }
    
    /*
    private void parseDraw(CharArray ch) {
        // 未完成
        CharArray script = new CharArray();
        script.set(magic);
        script.add("draw");
        enqueue(script);
    }
    */
    
    /*
        描画を行う    あとで再構成する事！
    */
    public CharArray draw(SessionObject session, int index) {
        return draw(session, index, "");
    }
    public CharArray draw(SessionObject session, int index, String nameSpace) {
        if (debug) System.out.println("Script draw start-----");
        CharArray ch = session.getBuffer(nameSpace);
        CharArrayQueue chQueue = (CharArrayQueue)queue.peek(index-1);
        if (chQueue != null) {
            for (int i = 0; i < chQueue.size(); i++) {
                CharArray line = chQueue.peek(i);
                /* メソッドをParseする処理が必要 */
                if (line.equals("draw()")) {
                    //if (page != null) page.draw(session);
                } else {
                    
                }
                
            }
        }
        if (debug) System.out.println("Script draw end-----");
        return ch;
        

    }
    /* デバッグ用 */
    public CharArray draw(int index) {
        return draw(new CharArray(), index);
    }
    public CharArray draw(CharArray ch, int index) {
        if (debug) System.out.println("Script draw start-----");
        
        CharArrayQueue chQueue = (CharArrayQueue)queue.peek(index-1);
        if (chQueue != null) {
            for (int i = 0; i < chQueue.size(); i++) {
                CharArray line = chQueue.peek(i);
                /* メソッドをParseする処理が必要 */
                if (line.equals("draw()")) {
                    //if (page != null) page.draw(session);
                } else {
                    
                }
                
            }
        }
        if (debug) System.out.println("Script draw end-----");
        return ch;
    }
    
}

//
// [end of ContentScript.java]
//

