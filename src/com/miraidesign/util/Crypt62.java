//------------------------------------------------------------------------
// @(#)Crypt62.java
//          暗号化ツール
//          Copyright (c) Mirai Design Institute 2010 All Rights Reserved. 
//------------------------------------------------------------------------
// String encode(no):   数値を62進エンコードする
// long decode(str):    元の数値に戻す

package com.miraidesign.util;       // 

/**
 * 62進数変換を利用した暗号化ツール
 * @version 1.0 
 * @author Toru Ishioka
 * @since  JDK1.1
 */

public class Crypt62 {

    private static char[] chStr = { // 62進数変換用テーブル
        '0','1','2','3','4','5','6','7','8','9',
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O',
        'P','Q','R','S','T','U','V','W','X','Y','Z',
        'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o',
        'p','q','r','s','t','u','v','w','x','y','z'};
    private static int[] iVal = {
        0,1,2,3,4,5,6,7,8,9,0,0,0,0,0,0,0,
        10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,
        25,26,27,28,29,30,31,32,33,34,35,0,0,0,0,0,0,
        36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,
        51,52,53,54,55,56,57,58,59,60,61};


    private static char[] chars = new char[64];    // ロックして使う事
    private static char[] chBuf = new char[64];    // ロックして使う事
    //private static final char ch = '0';  // スペーシングキャラクタ
    //private static final int DECODE_MAX = 4;  // デコードサイズMAX
    //private static final int ENCODE_MAX = 6;  // デコードサイズMAX
    //private static final int ADD_VALUE = 0; //3651;  // MAGIC_VALUE
    
    // あとで消す事
    private static /* final */ long ADD_VALUE = 0;
    static public long getMagicValue() { return ADD_VALUE; }
    static public void setMagicValue(long value) { 
        if (value >= 0) {
            ADD_VALUE = value;
        }
    }
    

    /**
     *  エンコード
     *  @param no 
     */
    static public String encode(int no) { return encode((long)no,62); }
    static public String encode(long no) { return encode(no,62); }
    static public synchronized String encode(long no,int radix) {
        int j = 0;
        long val = no;
        val += ADD_VALUE;
        if (radix >= 2 && radix <= 62) {
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
    static public long decode(CharArray ch) { return decode(ch.toString(),62); }

    /**
     *  デコード
     *  @param s 暗号文字列
     */
    static public long decode(String s)    { return decode(s,62); }
    
    /**
     *  デコード
     *  @param ch 暗号文字列
     *  @param radix 基数 (max 62)
     */
    static public synchronized long decode(CharArray ch, int radix) {
        return decode(ch.toString(), radix);
    }
    /**
     *  デコード
     *  @param s 暗号文字列
     *  @param radix 基数 (max 62)
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
// [end of Crypt62.java]
//

