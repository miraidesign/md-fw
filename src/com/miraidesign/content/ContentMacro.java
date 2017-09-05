//------------------------------------------------------------------------
// @(#)ContentMacro.java
//              マクロを保管する
//              Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashVector;

/**
 *  ContentMacro
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ContentMacro {
    private boolean debug = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugMacro = (SystemConst.debug && false);  // 

    private HashVector<CharArray,Macro> hashVector = 
        new HashVector<CharArray,Macro>();   // Macroを保管
                                                        // CharArray/ Macro
    
    /** constructor */
    public ContentMacro() {
    }
    public ContentMacro(HashVector<CharArray,CharArrayQueue> hashIni) {
        createMacro(hashIni);
    }

    /** Macro 情報をクリアする */
    public void clear() { hashVector.clear(); }
    
    /* マクロを生成する */
    public void createMacro(HashVector<CharArray,CharArrayQueue> hashIni) {
if (debug) System.out.println("●● マクロを生成します ●●");
        clear();
        addMacro(hashIni);
        
        if (debugMacro) debugAllMacro();
if (debug) System.out.println("●● "+hashVector.size()+"個のマクロを生成しました●●");
    }
    /* マクロを追加する */
    protected void addMacro(HashVector<CharArray,CharArrayQueue> hashIni) {
        for (int i = 0; i < hashIni.size(); i++) {
            CharArray key  = (CharArray)hashIni.keyElementAt(i);
            CharArray data = (CharArray)((CharArrayQueue)hashIni.valueElementAt(i)).peek();
            Macro macro = hashVector.get(key);
            if (macro == null) {
                macro = new Macro();
                hashVector.put(key,macro);
            } else {
                System.out.println("●●そのマクロ["+key+"]はすでに存在します。新データで置き換えます。");
                macro.clear();
            }
            parse(macro, data);
        }
    }
    /* マクロ情報をパースする */
    protected void parse(Macro macro, CharArray data) {
        CharArray ch = new CharArray();
        int index = 0;
        for (int i = 0; i < data.length(); i++) {
            int m_index = data.indexOf('$', index);
            if (m_index < 0) {  // 見つからない
                ch.add(data.chars, index, data.length-index);
                //ch.replace("\n","<br>\n");
                macro.enqueue(ch);
                break;
            }
            // 見つかった
            ch.add(data.chars, index, m_index-index);
            if (m_index == data.length-1) { //最後の文字
                ch.add('$');
                //ch.replace("\n","<br>\n");
                macro.enqueue(ch);
                break;  // 終了
            } else if (m_index == data.length-2) { //最後から２文字目
                char c1 = data.chars[m_index+1];
                if ('0' <= c1 && c1 <= '9') {
                    if (ch.length() > 0) {
                        //ch.replace("\n","<br>\n");
                        macro.enqueue(ch);
                        ch = new CharArray();
                    }
                    ch.set(Macro.m_magic);
                    ch.add((int)(c1-'0'));
                } else {
                    ch.add('$');
                    ch.add(c1);
                }
                //ch.replace("\n","<br>\n");
                macro.enqueue(ch);
                break;
            } else if (m_index == data.length-3) { //最後から３文字目
                char c1 = data.chars[m_index+1];
                char c2 = data.chars[m_index+2];
                if ('0' <= c1 && c1 <= '9') {
                    if ('0' <= c2 && c2 <= '9') {
                        if (ch.length() > 0) {
                            //ch.replace("\n","<br>\n");
                            macro.enqueue(ch);
                            ch = new CharArray();
                        }
                        ch.set(Macro.m_magic);
                        ch.add((int)((int)(c1-'0')*10+(int)(c2-'0')));
                    } else {
                        if (ch.length() > 0) {
                            //ch.replace("\n","<br>\n");
                            macro.enqueue(ch);
                            ch = new CharArray();
                        }
                        ch.set(Macro.m_magic);
                        ch.add((int)(c1-'0'));
                    }
                    //ch.replace("\n","<br>\n");
                    macro.enqueue(ch);
                    break;  // 終了
                } else {
                    ch.add('$');
                    index = m_index+1;
                }
            } else {
                char c1 = data.chars[m_index+1];
                char c2 = data.chars[m_index+2];
                char c3 = data.chars[m_index+3];
                if ('0' <= c1 && c1 <= '9') {
                    if ('0' <= c2 && c2 <= '9') {
                        if ('0' <= c3 && c3 <= '9') {   // 処理対象としない
                            ch.add('$');
                            ch.add(c1);
                            ch.add(c2);
                            ch.add(c3);
                            index = m_index + 4;
                        } else {    // 数字２個の場合
                            if (ch.length() > 0) {
                                //ch.replace("\n","<br>\n");
                                macro.enqueue(ch);
                                ch = new CharArray();
                            }
                            ch.set(Macro.m_magic);
                            ch.add((int)((int)(c1-'0')*10+(int)(c2-'0')));
                            macro.enqueue(ch);
                            ch = new CharArray();
                            index = m_index + 3;
                        }
                    } else {  //数字１個の場合
                        if (ch.length() > 0) {
                            //ch.replace("\n","<br>\n");
                            macro.enqueue(ch);
                            ch = new CharArray();
                        }
                        ch.set(Macro.m_magic);
                        ch.add((int)(c1-'0'));
                        macro.enqueue(ch);
                        ch = new CharArray();
                        index = m_index + 2;
                    }
                } else {    // 数値が存在しない場合
                    ch.add('$');
                    index = m_index+1;
                }
            }
        } // next
    }
    /** レンダリング時のコンバート処理を行う
        @param key      マクロキーワード
        @param queue    マクロパラメータリスト
        @return CharArray
    */
    public CharArray convert(CharArray key, CharArrayQueue queue) {
        Macro macro = hashVector.get(key);
        if (macro == null) return null;     // null でよいか確認すること
        return macro.convert(queue);
    }
    
    
    /** マクロのデバッグ
        @param key マクロキーワード
    */
    public void debugMacro(CharArray key) {
        Macro macro = hashVector.get(key);
        if (macro == null) {
            System.out.println("debugMacro["+key+"] not found!");
        } else {
            System.out.println("▽Macro["+key+"] "+macro.size()+" ==============");
            for (int i = 0; i < macro.size(); i++) {
                CharArray line = macro.peek(i);
                if (line.chars[0] == Macro.m_magic) {
                    System.out.println("("+i+") $"+line.getInt(1,10));
                } else {
                    System.out.println("["+i+"]"+line);
                }
            }
            System.out.println("△Macro["+key+"] end ------------");
        }
    }
    public void debugAllMacro() {
        for (int i = 0; i < hashVector.size(); i++) {
            CharArray key = hashVector.keyElementAt(i);
            debugMacro(key);
        }
    }
}

//
//
// [end of ContentMacro.java]
//

