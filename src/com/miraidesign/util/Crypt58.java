//------------------------------------------------------------------------
// @(#)Crypt58.java
//          暗号化ツール
//          Copyright (c) Mirai Design Institute 2020 All Rights Reserved. 
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

public class Crypt58 {

    private static char[] chStr = { // 62進数変換用テーブル
      // 0   1   2   3   4   5   6   7   8   9
        'a','N','9','s','D','B','i','W','I','2',
      // A   B   C   D   E   F   G   H   I   J   K   L   M   N   O
      //10  11  12  13  14  15  16  17  18  19  20  21  22  23  24
        'q','5','C','e','E','u','U','H','8','J','z','L','c','x','w',
      // P   Q   R   S   T   U   V   W   X   Y   Z
      //25  26  27  28  29  30  31  32  33  34  35
        'Y','P','R','d','T','G','g','7','X','y','o',
      // a   b   c   d   e   f   g   h   i   j   k   l   m   n   o
      //36  37  38  39  40  41  42  43  44  45  46  47  48  49  50
        'K','b','M','S','4','f','V','h','6','j','k','Q','n','m','Z',
      // p   q   r   s   t   u   v   w   x   y   z
      //51  52  53  54  55  56  57  58  59  60  61
        'p','A','r','3','t','F','v','0','O','1','l'};
    private static int[] iVal = {
      // 0   1   2   3   4   5   6   7   8   9
        58 ,60 , 9 ,54 ,40 ,11 ,44 ,32 ,18 , 2 , 0,0,0,0,0,0,0,
      // A   B   C   D   E   F   G   H   I   J   K   L   M   N   O
        52 , 5 ,12 , 4 ,14 ,56 ,30 ,17 , 8 ,19 ,36 ,21 ,38 , 1 ,59,
      // P   Q   R   S   T   U   V   W   X   Y   Z
        26 ,47 ,27 ,39 ,29 ,16 ,42 , 7 ,33 ,25 ,50 ,0,0,0,0,0,0,
      // a   b   c   d   e   f   g   h   i   j   k   l   m   n   o
         0 ,37 ,22 ,28 ,13 ,41 ,31 ,43 , 6 ,45 ,46 ,61 ,49 ,48 ,35, 
      // p   q   r   s   t   u   v   w   x   y   z
        51 ,10 ,53 , 3 ,55 ,15 ,57 ,24 ,23 ,34 ,20};


    private static char[] chars = new char[64];    // ロックして使う事
    private static char[] chBuf = new char[64];    // ロックして使う事
    private static /* final */ int ADD_VALUE = 3967;;  // MAGIC_VALUE
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
    static public CharArray encode(int no) { return encode((long)no,58); }
    static public CharArray encode(long no) { return encode(no,58); }
    static public synchronized CharArray encode(long no,int radix) {
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
        return new CharArray(chars,0,j);
    }

    /**
     *  デコード
     *  @param ch 暗号文字列
     */
    static public long decode(CharSequence s) { return decode(s, 58); }

    /**
     *  デコード
     *  @param s 暗号文字列
     *  @param radix 基数 (max 62)
     */
    static public synchronized long decode(CharSequence s, int radix) {
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
// [end of Crypt58.java]
//

