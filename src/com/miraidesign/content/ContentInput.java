//------------------------------------------------------------------------
// @(#)ContentInput.java
//              入力パラメータパーサー＆トランスレーター
//              Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import com.miraidesign.common.SystemConst;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.IntQueue;
import com.miraidesign.util.ObjectQueue;

/**
 *  ContentInput
 *  
 *  @version 0.5
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ContentInput {
    private boolean debug = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugParse = (SystemConst.debug && false);  // デバッグ表示
    
    //private ObjectQueue queue = new ObjectQueue();  
    private HashVector<String,Input> hashVector = new HashVector<String,Input>();   // Input を保管
    
    private HashVector<CharArray,CharArrayQueue> hashIni;
   
    // NonKeyデータを保存
    private HashVector<String,ObjectQueue> hashOther;
    
    
    /* 指定タイプのInputリストを返す
        ただし keyなしのもののみ */
    public ObjectQueue getInputListFromType(String type) {
        if (hashOther == null) return null;
        return hashOther.get(type);
    }
    
    /* 指定位置のInputを取得 */
    public Input getInput(int index) {
        //return hashVector.get(index);
        return hashVector.elementAt(index);
    }
    
    /** 指定キーワードのInputを取得
        @param key キーワード
        @return 存在しない場合はnull
    */
    public Input getInput(CharArray key) {
        return hashVector.get(key.toString());
    }
    
    public Input getInput(String key) {
        return hashVector.get(key);
    }
    /* テンプレートを取得 
        @return 存在しない時は null
    */
/***
    public Template getTemplate(CharArray key) {
        for (int i = 0; i < queue.size(); i++) {
            Template template = (Template)queue.peek(i);
            CharArray ch = template.getKey();
            if (ch.equals(key)) return template;
        }
        return null;
    }
    public Template getTemplate(String str) {
        CharArray key = CharArray.pop(str);
        Template temp = getTemplate(key);
        CharArray.push(key);
        return temp;
    }
**/
    
    /** constructor */
    public ContentInput() {
        
    }
    /* constructor */
    public ContentInput(HashVector<CharArray,CharArrayQueue> ini) {
        hashIni = ini;
    }
    /** Input 情報をクリアする */
    public void clear() { 
        hashVector.clear(); 
        if (hashOther != null) hashOther.clear();
    }
    /** Input個数を取得する 
        @return Input個数
    */
    public int size() {
        return hashVector.size();
    }
    
    
    /*
        ファイルをパースしてスクリプトを抜き出す
        @return indexの配列
    */
    public int parse(char[] chars, int start ,int end, int start2, int end2, CharArray parseErrMsg, SessionObject session) {
        int no = -1;
        int index = -1;
        IntQueue iq = new IntQueue();
if (debug) System.out.println("%%%% parse input start %%%%%%%% ");
        
        CharArray param = CharArray.pop();
        CharArray ch = CharArray.pop();
        ch.set(chars, start,end-start).toLowerCase();    // 大文字に変えておく
        CharArray key = null;
if (debug) System.out.println(ch);
        
        do {
            --end;
            // 行末のスペース と / の削除
            while (end > start && (chars[end] <= ' ' || chars[end] == '/')) {
                --end;
            }
if (debug) {
    for (int i = start; i <= end ; i++) System.out.print(chars[i]);
    System.out.println();
}

            if (start > end) {
                String msg = "<input: タグ情報が取得できません";
                System.out.println("★break1 start:"+start+">end:"+end+" "+msg);
                parseErrMsg.add(msg).add("\n");
                break;
            }
            // 行頭のスペース と : の削除
            while (end > start && (chars[start] <= ' ' || chars[start] == ':')) {
                ++start;
            }

            if (start > end) {
                String msg = "<input: タイプ名が取得できません";
                System.out.println("★break2 start:"+start+">end:"+end+" "+msg);
                parseErrMsg.add(msg).add("\n");
                break;
            }
            // タイプ名取得
            CharArray type = new CharArray();
            while (start <= end) {
                if (chars[start] <= ' ') {
                    break;
                } else {
                    type.add(chars[start]);
                }
                start++;
            }
            if (Input.getType(type) <= 0) {
                String msg = "<input:"+type+" 入力タイプが違います";
                System.out.println("★break3 "+msg);
                parseErrMsg.add(msg).add("\n");
                break;
            }

            // パラメータの取得
            HashParameter hash = new HashParameter();
            while (start <= end) {
                CharArray chKey = new CharArray();
                CharArray chData = new CharArray();
                // 空白除去
                while (end >= start && chars[start] <= ' ') { ++start;} 
                if (start > end) {
                    if (debugParse) System.out.println("★break4");
                    break;
                }
                // キーワード取得
                while (end >= start && chars[start] != '=') { 
                    chKey.add(chars[start++]);
                }
                chKey.trim();
                if (++start > end || chKey.length()==0) {
                    if (debugParse) System.out.println("★break5");
                    break;
                }
                // データ取得
                // 空白除去
                while (end >= start && chars[start] <= ' ') { ++start;} 
                if (start > end) {
                    if (debugParse) System.out.println("★break6");
                    break;
                }
                if (chars[start] == '\'') { // シングルクォート
                    start++;
                    while (end >= start && chars[start] != '\'') { 
                        chData.add(chars[start++]);
                    }
                } else if (chars[start] == '\"') { // ダブルクオート
                    start++;
                    while (end >= start && chars[start] != '\"') { 
                        chData.add(chars[start++]);
                    }
                } else {
                    while (end >= start && chars[start] > ' ') { 
                        chData.add(chars[start++]);
                    }
                }
                chKey.toLowerCase();
if (debugParse) System.out.println("●ContentInput:hash add ["+chKey+"]["+chData+"]");
                hash.add(chKey,chData);
                ++start;
            }
            
            if (debug) hash.debugParameter(session);
            
            // キー情報の取得
            key = hash.get("key");    // キーは小文字で保管すること
            if (key==null || key.trim().length()==0) {
                if (type.equals("META") || type.equals("LINK") || type.equals("BASE") 
                    || type.equals("TITLE")  || type.equals("BODY") 
                    || type.equals("SCRIPT") || type.equals("STYLE")   // 予約
                    ) {
                    no = -999;
                   // do noting
                } else {
                    parseErrMsg.add("<input:"+type+" 入力タイプが違います key="+key+"\n");
                    break;
                }
            }
            
            // 参照情報の取得
            CharArray ref = hash.get("ref");    // キーは小文字で保管すること
            if (ref != null) {
                HashParameter hashParam = null;  // template.ini から取得
                if (hashIni != null && hashIni.size() > 0) {
                    CharArrayQueue caq = (CharArrayQueue)hashIni.get(ref);
                    if (caq != null && caq.size() > 0) {
                        hashParam = new HashParameter();
                        hashParam.set(caq.peek(), ":", ";");
                    }
                }
                if (hashParam == null && session != null) {
                    ModuleManager mm = session.getModuleManager();
                    if (mm != null) {
                        hashParam = mm.getRefParameter(ref);
                    }
                }

                if (hashParam != null) {
                    for (int i = 0; i < hashParam.size(); i++) {
                        CharArray ck = ((CharArray)hashParam.keyElementAt(i));
                        CharArray cd = ((CharArray)hashParam.valueElementAt(i));
                        if (hash.get(ck) == null) {
                            hash.add(ck,cd);
                        } else {
                        }
                    }
                }
            }
            //------------------------------------------------
            Input input = new Input(type);
            input.setParameterList(hash);
            if (no == -999) {
                if (hashOther == null) hashOther = new HashVector<String,ObjectQueue>();
                ObjectQueue queue = hashOther.get(type.toString());
                if (queue == null) {
                    queue = new ObjectQueue();
                    hashOther.put(type.toString(), queue);
                }
                if (start2>= 0 && end2 >=start2) {
                    input.getData().set(chars, start2, end2-start2);    // STYLE, SCRIPTデータ設定
                }
                queue.enqueue(input);
                if (debug) System.out.println("<input:"+type+" を追加します");
            } else {
                hashVector.put(key.toString(), input);  // Input を追加する  null check
                no = hashVector.size()-1;
if (debug) System.out.println(" add: input("+(input != null)+":"+key+")"+no);
            }
        } while (false);
        CharArray.push(ch);
        CharArray.push(param);

if (debug) {
        if (parseErrMsg.length() > 0) System.out.println("<input> parse Error:"+parseErrMsg);
}
if (debug) System.out.println("%%%% parse input end  %%%%%%%% key="+key+" "+((no >= 0 || no == -999) ? "OK" : "NG"));

        return no;
    }
    
    /*
        描画処理
    */
    /***
    public CharArray draw(SessionObject session, int index) {
    
        if (debug) System.out.println("Input draw start "+index+"--------");
        CharArray ch = session.getBuffer();
        
        if (debug) System.out.println("Input draw end-----------------");
        return ch;
    }
    ***/
    
}

//
// $Author$
// $Source$
// $Revision$
// $Date$
//
// $Log$
//
//
// [end of ContentInput.java]
//

