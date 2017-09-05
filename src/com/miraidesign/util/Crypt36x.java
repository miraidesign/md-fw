//------------------------------------------------------------------------
// @(#)Crypt36x.java
//          暗号化ツール
//          Copyright (c) Mirai Design Institute 2010 All Rights Reserved. 
//------------------------------------------------------------------------
// String encode(no):   数値を36進エンコードする
// long decode(str):    元の数値に戻す

package com.miraidesign.util;       // 

/**
 * 36進数変換を利用した暗号化ツール
 * @version 1.0 
 * @author Toru Ishioka
 * @since  JDK1.1
 */

public class Crypt36x {
    // 並び替える
    private static char[] chStr = { // 32進数変換用テーブル 1<->n
        'b','n','r','5','a','3','g','w','i','y',
        '4','0','c','p','e','f','6','h','8','j','o','l','k','1','z',
        'd','q','2','u','t','s','v','9','x','7','m'};
    private static int[] iVal = {
        11 , 23,27 , 5, 10 , 3 ,16 ,34 ,18 ,32 ,                                    // 0-9
        0,0,0,0,0,0,0,                                          //
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,    // A-Z
        0,0,0,0,0,0,
         4 , 0 ,12 ,25 ,14 ,15 , 6 ,17 , 8 ,19 ,22 ,21 ,35 , 1 ,20,           // a-o
        13,26, 2,30,29,28,31, 7,33, 9,24};                      // p-z


    private static char[] chars = new char[64];    // ロックして使う事
    private static char[] chBuf = new char[64];    // ロックして使う事
    private static long ADD_VALUE = 1296;

    /**
     *  エンコード
     *  @param no 
     */
    static public String encode(int no) { return encode((long)no,36); }
    static public String encode(long no) { return encode(no,36); }
    static public synchronized String encode(long no,int radix) {
        int j = 0;
        long val = no;
        val += ADD_VALUE;
        if (radix >= 2 && radix <= 36) {
            int i = 0;
            do {
                chBuf[i++] = chStr[(int)(val % radix)];
                val /= radix;
            } while (val > 0);
            //while (DECODE_MAX > i) chBuf[i++] = ch;
            while (i > 0) chars[j++] = chBuf[--i];  // 反転コピー
        }
        return new String(chars,0,j);
    }

    /**
     *  デコード
     *  @param ch 暗号文字列
     */
    static public long decode(CharArray ch) { return decode(ch.toString(),36); }

    /**
     *  デコード
     *  @param s 暗号文字列
     */
    static public long decode(String s)    { return decode(s,36); }
    
    /**
     *  デコード
     *  @param ch 暗号文字列
     *  @param radix 基数 (max 36)
     */
    static public synchronized long decode(CharArray ch, int radix) {
        return decode(ch.toString(), radix);
    }
    /**
     *  デコード
     *  @param s 暗号文字列
     *  @param radix 基数 (max 36)
     */
    static public synchronized long decode(String s, int radix) {
        long val = 0;
        for (int i = 0; i < s.length(); i++) {
            val *= radix;
            char c = s.charAt(i);
            if (c >= '0' && c <= 'z') {
                val += iVal[c-'0'];
            } else {
                System.out.println("decode error!!!");
                break;
            }
        }
        //while (ENCODE_MAX > i) chBuf[i++] = ch;
        val -= ADD_VALUE;
        return val;
    }
}

//
// [end of Crypt36x.java]
//

