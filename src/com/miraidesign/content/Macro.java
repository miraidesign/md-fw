//------------------------------------------------------------------------
// @(#)Macro.java
//              マクロ
//              Copyright (c) Mirai Dewsign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//      実体はContentMacro によって生成される
//------------------------------------------------------------------------

package com.miraidesign.content;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/**
 *  Macro フレームワークが使用します
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class Macro extends CharArrayQueue  {
    private static boolean debug = false;
    
    static public final char m_magic = 0x02; // マクロ識別コード
    
    private CharArray key = new CharArray();
    public void setKey(CharArray key) { this.key.set(key); }
    public CharArray getKey() {return key;}
    
    //---------------------------------------------------
    // constructor
    //---------------------------------------------------
    public Macro() {
        // do nothing 
    }
    //---------------------------------------------------
    // 
    //---------------------------------------------------
    /** マクロパラメータをコンバートする 
        @param queue 置換パラメータリスト
        @return コンバート後のテキスト
    */
    public CharArray convert(CharArrayQueue queue) {
        CharArray ch = CharArray.pop();
        do {
            for (int i = 0; i  < size(); i++) {
                CharArray line = peek(i);
                if (line.chars[0] == m_magic) { // パラメーターなら
                    if (queue != null) {
                        int no = line.getInt(1,10);
                        if (no > 0 && no < 100 && no <= queue.size()) {
                            ch.add(queue.peek(no-1));
                        }
                    }
                } else {
                    ch.add(line);
                }
            }
        } while (false);
        return ch;
    }
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
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

}

//
//
// [end of Macro.java]
//

