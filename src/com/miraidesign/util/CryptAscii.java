//------------------------------------------------------------------------
// @(#)CryptAscii.java
//          ASCII文字限定暗号化ツール
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//         0x20 - 0x7e と TABコードのみ変換します。（改行コードはそのまま）
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.net.URLEncoder;
import java.net.URLDecoder;

/**
 *  ASCII文字列専用暗号化ツール<br>
 *  0x20 - 0x7e と TABコードのみ変換します。（改行コードはそのまま）
 * @version 1.0 2010-04-01
 * @author Toru Ishioka
 * @since  JDK1.1
 */

public class CryptAscii {
    private static int[] magic = { // 変換テーブル
       101,509,  2,407,852, 21,611,546, 82, 63,345,
       203, 10, 26,305,  6,444, 45, 76, 71,496,155,
       648,917,998, 94, 47, 63, 49, 53,750, 12,713,
       713,815, 16,964, 65,266,265, 67,468,  8, 18,
       895, 93,689,221,483,380, 75,670,358, 99,285,
       185, 61,156,342,323, 24,425, 28,629, 50,731,
       833, 14, 34,935, 36, 22,138, 39,240,141,954,
        55,662, 51, 59,560, 97,872, 73,174,277, 78,
       179, 87, 81, 84, 20,527, 32, 37, 43, 57,586,
        88, 91,792, 96,119,567,892
    };
    private static int magicMAX = 1002;  // 0x5e * 11 - 0x20

    /**
     *  エンコード
     *  @param ch エンコード文字列
     *  @return 暗号化文字列
     */
    static public CharArray encode(CharArray ch) {
        return encode(ch, 0);
    }
    static public CharArray encode(String str) {
        CharArray ch = CharArray.pop(str);
        CharArray rsts = encode(ch);
        CharArray.push(ch);
        return rsts;
    }
    /**
     *  エンコード
     *  @param ch エンコード文字列
     *  @param key エンコードキー
     *  @return 暗号化文字列
     */
    static public CharArray encode(CharArray ch, int key) {
        CharArray ret = CharArray.pop();
        for (int i = 0; i < ch.length(); i++) {
            int MAGIC = magic[(i+key)%magic.length];
            char c = ch.chars[i];
            char d = (c == 0x09) ? (char)0x7f
                     : (char)(0x20 + (c -0x20 + MAGIC)%0x5e);
            ret.add(d);
        }
        return ret;
    }
    static public CharArray encode(String str, int key) {
        CharArray ch = CharArray.pop(str);
        CharArray rsts = encode(ch,key);
        CharArray.push(ch);
        return rsts;
    }
    /**
     *  エンコード
     *  @param ch エンコード文字列
     *  @param key エンコードキーワード
     *  @return 暗号化文字列
     */
    static public CharArray encode(CharArray ch, String key) {
        return encode(ch, key, 0);
    }
    static public CharArray encode(String str, String key) {
        CharArray ch = CharArray.pop(str);
        CharArray rsts = encode(ch,key);
        CharArray.push(ch);
        return rsts;
    }
    /**
     *  エンコード
     *  @param ch エンコード文字列
     *  @param key エンコードキーワード
     *  @param offset キーワードオフセット
     *  @return 暗号化文字列
     */
    static public CharArray encode(CharArray ch, String key, int offset) {
        CharArray ret = CharArray.pop();
        for (int i = 0; i < ch.length(); i++, offset++) {
            int k = key.charAt(offset%key.length());
            int MAGIC = magic[(offset+k)%magic.length];
            char c = ch.chars[i];
            char d = (c == 0x09) ? (char)0x7f
                     : (char)(0x20 + (c -0x20 + MAGIC)%0x5e);
            ret.add(d);
        }
        return ret;
    }
    static public CharArray encode(String str, String key, int offset) {
        CharArray ch = CharArray.pop(str);
        CharArray rsts = encode(ch,key,offset);
        CharArray.push(ch);
        return rsts;
    }

    /**
     *  デコード
     *  @param ch 暗号文字列
     *  @return 解読文字列
     */
    static public CharArray decode(CharArray ch) {
        return decode(ch, 0);
    }
    static public CharArray decode(String str) {
        CharArray chTmp = CharArray.pop(str);
        CharArray ch = decode(chTmp);
        CharArray.push(chTmp);
        return ch;
    }
    
    /**
     *  デコード
     *  @param ch 暗号文字列
     *  @param key デコードキー
     *  @return 解読文字列
     */
    static public CharArray decode(CharArray ch, int key) {
        CharArray ret = CharArray.pop();
        for (int i = 0; i < ch.length(); i++) {
            int MAGIC = magic[(i+key)%magic.length];
            char c = ch.chars[i];
            char d = (c == 0x7f) ? (char)0x09
                     : (char)(0x20+ (c+magicMAX-MAGIC) % 0x5e);
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
    static public CharArray decode(String str, String key) {
        CharArray ch = CharArray.pop(str);
        CharArray ret =  decode(ch, key, 0);
        CharArray.push(ch);
        return ret;
    }
    /**
     *  デコード
     *  @param ch 暗号文字列
     *  @param key デコードキーワード
     *  @param offset キーワードオフセット
     *  @return 解読文字列
     */
    static public CharArray decode(CharArray ch, String key, int offset) {
        CharArray ret = CharArray.pop();
        for (int i = 0; i < ch.length(); i++, offset++) {
            int k = key.charAt(offset%key.length());
            int MAGIC = magic[(offset+k)%magic.length];
            char c = ch.chars[i];
            char d = (c == 0x7f) ? (char)0x09
                     : (char)(0x20+ (c+magicMAX-MAGIC) % 0x5e);
            ret.add(d);
        }
        return ret;
    }
    static public CharArray decode(String str, String key, int offset) {
        CharArray ch = CharArray.pop(str);
        CharArray ret =  decode(ch, key, offset);
        CharArray.push(ch);
        return ret;
    }
    
    static String _magic = "%4D%5F";

    /**
        メールの暗号化とURLエンコードを行う
        @param org オリジナルのメールアドレス
        @return 暗号化メールアドレス('/'は出力しません)
    */
    static public CharArray mailEncode(CharArray org) {
        return mailEncode(org, null);
    }
    static public CharArray mailEncode(String org) {
        CharArray ch = CharArray.pop(org);
        CharArray rsts = mailEncode(ch, null);
        CharArray.push(ch);
        return rsts;
    }
    
    /**
        メールの暗号化とURLエンコードを行う
        @param org オリジナルのメールアドレス
        @param charset charset
        @return 暗号化メールアドレス('/'は出力しません)
    */
    static public CharArray mailEncode(CharArray org, String charset) {
        if (charset == null) charset = "Shift_JIS";
        CharArray ret = CryptAscii.encode(org);
        ret.replace('/',(char)0x7f);
        ret.replace('%',(char)0x09);
        try {
            String str = URLEncoder.encode(ret.toString(), charset);
            ret.set(_magic);
            ret.add(str);
            ret.replace("+","%08"); //@@//
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    static public CharArray mailEncode(String org, String charset) {
        CharArray ch = CharArray.pop(org);
        CharArray rsts = mailEncode(ch, charset);
        CharArray.push(ch);
        return rsts;
    }
    
    /**
        mailEncodeによって暗号化されたメールアドレスを復号する
        @param org 暗号化メールアドレス(URLデコードされていても良い）
        @return オリジナルのメールアドレス
    */
    static public CharArray mailDecode(CharArray org) {
        return mailDecode(org, null);
    }
    static public CharArray mailDecode(String org) {
        CharArray ch = CharArray.pop(org);
        CharArray rsts = mailDecode(ch, null);
        CharArray.push(ch);
        return rsts;
    }
    /**
        mailEncodeによって暗号化されたメールアドレスを復号する
        @param org 暗号化メールアドレス(URLデコードされていても良い）
        @param charset charset
        @return オリジナルのメールアドレス
    */
    static public CharArray mailDecode(CharArray org, String charset) {
        if (charset == null) charset = "Shift_JIS";
        String str = "";
        if (org.startsWith(_magic)) {
            org.remove(0,6);
            try {
                str = URLDecoder.decode(org.toString(), charset);
            } catch (Exception ex) {
                str = org.toString();
                ex.printStackTrace();
            }
        } else {
            org.remove(0,2);
            if (org.indexOf('%') >= 0) {
                try {
                    org.replace("+","%2B");
                    str = URLDecoder.decode(org.toString(),"Shift_JIS");
                } catch (Exception ex) {
                    str = org.toString();
                    ex.printStackTrace();
                }
            } else {
                str = org.toString();
            }
        }
        str = str.replace((char)0x08,' ');  //@@//
        str = str.replace((char)0x09,'%');
        str = str.replace((char)0x7f,'/');
        CharArray ret = CryptAscii.decode(str);
        return ret;
    }
    static public CharArray mailDecode(String org, String charset) {
        CharArray ch = CharArray.pop(org);
        CharArray rsts = mailDecode(ch, charset);
        CharArray.push(ch);
        return rsts;
    }
    
}

//
// [end of CryptAscii.java]
//

