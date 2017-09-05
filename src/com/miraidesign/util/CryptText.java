//------------------------------------------------------------------------
// @(#)CryptText.java
//          文字列暗号化ツール
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//         漢字コードも変換します（改行コードはそのまま）
//------------------------------------------------------------------------

package com.miraidesign.util;

/**
 *  文字列暗号化ツール<br>
 *  文字列（漢字可）を変換します。（改行コードはそのまま）<br>
 *  出力はアスキー文字列。サイズは増大します。
 * @version 1.0 2010-04-01
 * @author Toru Ishioka
 * @since  JDK1.1
 */

public class CryptText {
    private static int[] magic = { // 変換テーブル
       101,509,  2,407,852, 21,611,546, 82, 63,345,
       203, 10, 26,305,  6,444, 45, 76, 71,496,155,
       648,917,928, 94, 47, 63, 49, 53,750, 12,713,
       713,815, 16,964, 65,266,265, 67,468,  8, 18,
       895, 93,689,221,483,380, 75,670,358, 99,285,
       185, 61,156,342,323, 24,425, 28,629, 50,731,
       833, 14, 34,935, 36, 22,138, 39,240,141,954,
        55,662, 51, 59,560, 97,872, 73,174,277, 78,
       179, 87, 81, 84, 20,527, 32, 37, 43, 57,586,
        88, 91,792, 96,119,567,892
    };
    private static int magicMAX = 1034;     //

    /**
     *  エンコード
     *  @param ch エンコード文字列
     *  @return 暗号化文字列
     */
    static public CharArray encode(CharArray ch) {
        return encode(ch, 0);
    }
    /**
     *  エンコード
     *  @param ch エンコード文字列
     *  @param key エンコードキー
     *  @return 暗号化文字列
     */
    static public CharArray encode(CharArray ch, int key) {
        CharArray ret = new CharArray();
        for (int i = 0; i < ch.length(); i++) {
            int MAGIC = magic[(i+key)%magic.length];
            char c = ch.chars[i];
            int c1 = (c >>> 10) & 0x3f;           // 左   6ビット
            int c2 = (c >>> 5)  & 0x1f;           // 真中 5ビット
            int c3 = c  & 0x1f;                   // 右   5ビット
            char d1 =  (char)(0x20+(c1+MAGIC)%0x5e);
            char d2 =  (char)(0x20+(c2+MAGIC)%0x5e);
            char d3 =  (char)(0x20+(c3+MAGIC)%0x5e);
            ret.add(d1);
            ret.add(d2);
            ret.add(d3);
        }
        return ret;
    }

    /**
     *  エンコード
     *  @param ch エンコード文字列
     *  @param key エンコードキーワード
     *  @return 暗号化文字列
     */
    static public CharArray encode(CharArray ch, String key) {
        return encode(ch,key,0);
    }
    /**
     *  エンコード
     *  @param ch エンコード文字列
     *  @param key エンコードキーワード
     *  @param offset キーワードオフセット
     *  @return 暗号化文字列
     */
    static public CharArray encode(CharArray ch, String key, int offset) {
        CharArray ret = new CharArray();
        for (int i = 0; i < ch.length(); i++,offset++) {
            int k = key.charAt(offset%key.length());
            int MAGIC = magic[(offset+k)%magic.length];
            char c = ch.chars[i];
            int c1 = (c >>> 10) & 0x3f;           // 左   6ビット
            int c2 = (c >>> 5)  & 0x1f;           // 真中 5ビット
            int c3 = c  & 0x1f;                   // 右   5ビット
            char d1 =  (char)(0x20+(c1+MAGIC)%0x5e);
            char d2 =  (char)(0x20+(c2+MAGIC)%0x5e);
            char d3 =  (char)(0x20+(c3+MAGIC)%0x5e);
            ret.add(d1);
            ret.add(d2);
            ret.add(d3);
        }
        return ret;
    }

    /**
     *  デコード
     *  @param ch 暗号文字列
     *  @return 解読文字列
     */
    static public CharArray decode(CharArray ch) {
        return decode(ch, 0);
    }
    /**
     *  デコード
     *  @param ch 暗号文字列
     *  @param key デコードキー
     *  @return 解読文字列
     */
    static public CharArray decode(CharArray ch, int key) {
        CharArray ret = new CharArray();
        for (int i = 0; i < ch.length(); i+=3) {
            int MAGIC = magic[((i/3)+key)%magic.length];

            int c1 = ch.chars[i]   - 0x20;
            int c2 = ch.chars[i+1] - 0x20;
            int c3 = ch.chars[i+2] - 0x20;
            
            int d1 = (c1+magicMAX-MAGIC) % 0x5e;
            int d2 = (c2+magicMAX-MAGIC) % 0x5e;
            int d3 = (c3+magicMAX-MAGIC) % 0x5e;
            
            d1 <<= 10;
            d2 <<= 5;
            char d = (char)(d1+d2+d3);
            ret.add(d);
        }
        return ret;
    }
    /**
     *  デコード
     *  @param ch 暗号文字列
     *  @param key デコードキーワード
     *  @return 解読文字列
     */
    static public CharArray decode(CharArray ch, String key) {
        return decode(ch, key, 0);
    }
    /**
     *  デコード
     *  @param ch 暗号文字列
     *  @param key デコードキーワード
     *  @param offset キーワードオフセット
     *  @return 解読文字列
     */
    static public CharArray decode(CharArray ch, String key, int offset) {
        CharArray ret = new CharArray();
        for (int i = 0; i < ch.length(); i+=3, offset++) {
            int k = key.charAt(offset%key.length());
            int MAGIC = magic[(offset+k)%magic.length];

            int c1 = ch.chars[i]   - 0x20;
            int c2 = ch.chars[i+1] - 0x20;
            int c3 = ch.chars[i+2] - 0x20;
            
            int d1 = (c1+magicMAX-MAGIC) % 0x5e;
            int d2 = (c2+magicMAX-MAGIC) % 0x5e;
            int d3 = (c3+magicMAX-MAGIC) % 0x5e;
            
            d1 <<= 10;
            d2 <<= 5;
            char d = (char)(d1+d2+d3);
            ret.add(d);
        }
        return ret;
    }
}

//
// [end of CryptText.java]
//

