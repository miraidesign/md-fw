//------------------------------------------------------------------------
// @(#)Crypt62x.java
//          暗号化ツール
//          Copyright (c) Mirai Design Institute 2011 All Rights Reserved. 
//------------------------------------------------------------------------
// String encode(no):   数値を62進エンコードする
// long decode(str):    元の数値に戻す

package com.miraidesign.util;       // 

/**
 * 62進数変換を利用した暗号化ツール。短縮URL対応版
 * @version 1.0 
 * @author Toru Ishioka
 * @since  JDK1.1
 */

public class Crypt62x {

    private static char[] chStr = { // 62進数変換用テーブル
        'a','N','9','s','D','B','i','W','I','2',
        'q','5','C','e','E','u','U','H','8','J','z','L','c','O','l',
        '1','P','R','d','T','G','g','7','X','y','o',
        '0','b','M','S','4','f','V','h','6','j','k','Q','n','m','Z',
        'p','A','r','3','t','F','v','K','x','Y','w'};
    private static int[] iVal = {
        36 ,25 , 9 ,54 ,40 ,11 ,44 ,32 ,18 , 2 , 0,0,0,0,0,0,0,
        52 , 5 ,12 , 4 ,14 ,56 ,30 ,17 , 8 ,19 ,58 ,21 ,38 , 1 ,23,   // A-O
        26 ,47 ,27 ,39 ,29 ,16 ,42 , 7 ,33 ,60 ,50 ,0,0,0,0,0,0,   // P-Z
         0 ,37 ,22 ,28 ,13 ,41 ,31 ,43 , 6 ,45 ,46 ,24 ,49 ,48 ,35,   // a-o
        51 ,10 ,53 , 3 ,55 ,15 ,57 ,61 ,59 ,34 ,20};              // p-z


    private static char[] chars = new char[64];    // ロックして使う事
    private static char[] chBuf = new char[64];    // ロックして使う事
    private static /* final */ int ADD_VALUE = 3844; //3651;  // MAGIC_VALUE
    static public int  getMagicValue() { return ADD_VALUE; }
    static public void setMagicValue(int value) { 
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
        val -= ADD_VALUE;
        return val;
    }
}

//
// [end of Crypt62x.java]
//

