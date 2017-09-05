//------------------------------------------------------------------------
//    SessionKey.java
//                 セッションキーを配布する
//         Copyright (c) Mirai Design Institutes 2010 All Rights Reserved. 
//                 update: 2010-04-02 ishioka
//------------------------------------------------------------------------

package com.miraidesign.session;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.IniFile;
import com.miraidesign.util.IntQueue;
import com.miraidesign.util.Util;
import com.miraidesign.util.CryptURL;

/**
 *  セッションキーを配布する
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class SessionKey {
    private static boolean debug  = false;
    private static boolean pseudo = true; // 擬似セッションキーを使う
    private static int pseudoKey = 1;     // セッションＩＤの代わりに使うユニークキー
    
    private static int div = 0;           // セッションＩＤ分割数
    private static IntQueue queue = new IntQueue(); // 使用リスト
    
    public static void init(IniFile ini) {
        div = 0;
        queue.clear();
        CharArray data = ini.get("[System]","SessionSuffix");
        if (data != null) {
            CharToken token = CharToken.pop();
            token.set(data,"/");
            if (token.size() == 2) {
                boolean isOK = false;
                div = token.getInt(1);
                CharToken token2 = new CharToken(token.get(0),",");
                for (int i = 0; i < token2.size(); i++) {
                    int value = token2.getInt(i);
                    if (value >= 0 && value < div) {
                        queue.enqueue(value);
                        isOK = true;    // １つでもＯＫなら通す
                    } else {
                        //isOK = false;
                    }
                }
                if (!isOK) {  // 設定ミス
                    div = 0;
                    queue.clear();
                }
            }
            CharToken.push(token);
        }
    }
    
    /**  セッションキーを取り出す<br>
        pseudo = true の時は擬似セッションキーを返します
        @return セッションキー
    */
    static public int getSessionKey() {
        if (pseudo) {   // 擬似キーを発行
            return getPseudoKey();
        } else {        // セッションから取得
            
            return 0;   // not ready
        }
    }
    
    static long last_time = -1;
    
    /** 
        擬似セッションキーを取り出す
        @return セッションキー
     */
    static synchronized public int getPseudoKey() {
        long time = System.currentTimeMillis();
        int rsts = -1;
        boolean found = false;
        while (!found) {
            while (time == last_time) {
                com.miraidesign.util.Util.Delay(1);
                time = System.currentTimeMillis();
            }
            last_time = time;
            long value = time * 1000000L+ (pseudoKey++ % 1000000L);
            rsts = (int)((value ^ (value >> 32)) & 0x7fffffff);  // hashCodeルーチンを流用
            
            if (div > 0) {
                int j = rsts % div;
                for (int i = 0; i < queue.size(); i++) {
                    int ii = queue.peek(i);
                    if (ii == j) {
                        found = true;
                        break;
                    }
                }
            } else {
                found = true;
            }
        } // enddo
if (debug) System.out.println("SessionKey:"+rsts);
        return rsts;
    }
    // 短縮して使いたい時は、com.miraidesign.util.Cript62.encode(int) を呼ぶ
    
    /** 擬似キーを使用するか？
        @param mode true:擬似キーを使用する(default)
     */
    static public void setPseudoMode(boolean mode) {
        pseudo = true;
    }
}

//
//
// [end of SessionKey.java]
//

