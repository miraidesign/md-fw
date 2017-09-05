//------------------------------------------------------------------------
// @(#)CryptURL.java
//          URL短縮＆暗号化ツール：可逆変換
//          Copyright (c) Mirai Design Institute 2011-12 All Rights Reserved. 
//------------------------------------------------------------------------
// String encode(no):   数値を短縮URLエンコードする
// long decode(str):    元の数値に戻す

package com.miraidesign.util;       // 

/**
 * 62進数変換を利用したURL短縮＆暗号化ツール。可逆変換可能。
 * @version 1.0
 * @author Toru Ishioka
 * @since  JDK1.1
 */

public class CryptURL {

    private static char[] chStr = { // 62進数変換用テーブル
        'a','N','9','s','D','B','i','W','I','2',
        'q','5','C','e','E','u','U','H','8','J','z','L','c','O','l',
        '1','P','R','d','T','G','g','7','X','y','o',
        '0','b','M','S','4','f','V','h','6','j','k','Q','n','m','Z',
        'p','A','r','3','t','F','v','K','x','Y','w'};
    private static int[] iVal = {
        36 ,25 , 9 ,54 ,40 ,11 ,44 ,32 ,18 , 2 , 0,0,0,0,0,0,0,
        52 , 5 ,12 , 4 ,14 ,56 ,30 ,17 , 8 ,19 ,58 ,21 ,38 , 1 ,23,
        26 ,47 ,27 ,39 ,29 ,16 ,42 , 7 ,33 ,60 ,50 ,0,0,0,0,0,0,
         0 ,37 ,22 ,28 ,13 ,41 ,31 ,43 , 6 ,45 ,46 ,24 ,49 ,48 ,35, 
        51 ,10 ,53 , 3 ,55 ,15 ,57 ,61 ,59 ,34 ,20};


    private static char[] chars = new char[64];    // ロックして使う事
    private static char[] chBuf = new char[64];    // ロックして使う事

    private static long count = 0;
    
    private static int M1 = 7;
    private static int M2 = 137;
    private static int M3 = 1093;

    /**
     *  数値のエンコードを行う
     *  @param num  元数値
     *  @return  暗号化文字列
     *
     */
    static public CharArray encode(long num) {
        return encode(num, 1, false);
    }
    /**
     *  数値のエンコードを行う
     *  @param num  元数値
     *  @param version 1:旧分散演算 2:新分散演算
     *  @param shift_mode  シフトモード
     *  @return  暗号化文字列
     *
     */
    static public CharArray encode(long num, int version, boolean shift_mode) {
        long time = System.currentTimeMillis()+(++count*7);
        return encode(num, version, shift_mode, time, count);
    }
    /**
     *  数値のエンコードを行う
     *  @param num  元数値
     *  @param version 1:旧分散演算 2:新分散演算
     *  @param shift_mode  シフトモード
     *  @return  暗号化文字列
     *
     */
    static public CharArray encode(long num, int version, boolean shift_mode, long time) {
        return encode(num, version, shift_mode, time, M3);
    }
    static public CharArray encode(long num, int version, boolean shift_mode, long time, long _c) {
        int t1 = (int)(time % 62);
        int t2 = (int)(((time>>4) +(_c*11)) % 62);

        char c1 = chStr[t1];
        char c2 = chStr[t2];
        
        CharArray ch = new CharArray();
        long magic = (c1 * M1+ c2 * M2) + M3;
        if (version >= 2)  magic = (c1 * M1+ c2 * M2) * M3;
        ch.add(c1);
        String str = _encode(num,62,magic);
        if (shift_mode) {
            ch.add(CharArray.shift(str, (int)magic));
        } else {
            ch.add(str);
        }
        ch.add(c2);
        return ch;  // return chtoString();
    }
    
    
    
    /**
     *  暗号化文字列をデコードする
     *  @param str  暗号化文字列
     *  @return     変換前数値
     */
    static public long decode(String str) {
        return decode(str, 1);
    }
    static public long decode(String str, int version) {
        long ret = 0;
        CharArray ch = CharArray.pop(str);  // 文字列クラスにコピー
        long c1 = (long)ch.chars[0];
        long c2 = (long)ch.chars[ch.length-1];
        ch.length--;
        ch.remove(0,1);
        long magic = (c1 * M1+ c2 * M2) + M3;
        if (version >= 2)  magic = (c1 * M1+ c2 * M2) * M3;
        ret = _decode(ch,62,magic);
        CharArray.push(ch);                 // 文字列クラスを返却
        return ret;
    }
    static public long decode(CharArray str) {
        return decode(str, 1, false);
    }
    static public long decode(CharArray str, int version, boolean shift_mode) {
        long ret = 0;
        CharArray ch = CharArray.pop(str);  // 文字列クラスにコピー
        long c1 = (long)ch.chars[0];
        long c2 = (long)ch.chars[ch.length-1];
        ch.length--;
        ch.remove(0,1);
        
        long magic = (c1 * M1+ c2 * M2) + M3;
        if (version >= 2)  magic = (c1 * M1+ c2 * M2) * M3;
        
        if (shift_mode) ch.shift(-(int)magic);
        
        ret = _decode(ch,62,magic);
        CharArray.push(ch);                 // 文字列クラスを返却
        return ret;
    }

    /**
     *  エンコード補助関数(n進数暗号化文字列変換)
     *  @param no               元数値
     *  @param radix 基数       2 - 62
     *  @param magic_value      暗号化係数
     *  @return 変換後文字列
     */
    static private synchronized String _encode(long no,int radix, long magic_value) {
        int j = 0;
        long val = no;
        val += magic_value;
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
     *  デコード補助関数(n進数暗号化文字列復号)
     *  @param s                暗号文字列
     *  @param radix 基数       2 - 62
     *  @param magic_value      暗号化係数
     *  @return                 復号数値
     */
    static private synchronized long _decode(CharArray s, int radix, long magic_value) {
        long val = 0;
        for (int i = 0; i < s.length(); i++) {
            val *= radix;
            char c = s.chars[i];
            if (c >= '0' && c <= 'z') {
                val += iVal[c-'0'];
            } else {
                System.out.println("decode error!!!");
                break;
            }
        }
        val -= magic_value;
        return val;
    }
    
}





//
// [end of CryptURL.java]
//

