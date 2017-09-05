//------------------------------------------------------------------------
// @(#)Crypt36.java
//          CUST-ID 暗号化ツール
//          Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
// String encode(str):  DNmmmnnn の形のCUST-IDを DNXXXX の形に暗号化する
// String decode(cript):  DNXXXX を DNmmmnnnの形に復元する
// int getInt(cript):  hash用のキー値を取り出す
// String getID(int):  hash値を暗号文字列に復元
// int getNo(str):  顧客IDを取り出す
// int getNo(key):  顧客IDを取り出す
// int getType(str):  顧客区分を取り出す
// int getType(key):  顧客区分を取り出す

package com.miraidesign.util;       //

/**
 *  36進数変換を利用した暗号化ツール<br>
 *  サイズも小さくなる
 * @version 1.0 
 * @author Toru Ishioka
 * @since  JDK1.1
 */

public class Crypt36 {

    private static char[] chStr = { // 36進数変換用テーブル
        '0','1','2','3','4','5','6','7','8','9',
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O',
        'P','Q','R','S','T','U','V','W','X','Y','Z'};

    private static char[] chars = new char[64];    // ロックして使う事
    private static char[] chBuf = new char[64];    // ロックして使う事
    private static final char ch = '0';  // スペーシングキャラクタ
    private static final int DECODE_MAX = 4;  // デコードサイズMAX
    private static final int ENCODE_MAX = 6;  // デコードサイズMAX
    private static final int ADD_VALUE = 3651;  // MAGIC_VALUE

    /**
     *  エンコード
     *  @param s 入力文字
     */
    static public String encode(String s) {
        return encode(s,36);
    }
    static public synchronized String encode(String s,int radix) {

        int j = 0;
        chars[j++] = s.charAt(0);   //最初の２文字をコピー
        chars[j++] = s.charAt(1);
        int val = 0;
        try {
            val = Integer.parseInt(s.substring(2));
        } catch (Exception ex) {
            System.out.println("Exeption:"+ex);
        }
        val += ADD_VALUE;
        if (radix >= 2 && radix <= 36) {
            int i = 0;
            do {
                chBuf[i++] = chStr[(int)(val % radix)];
                val /= radix;
            } while (val > 0);
            
            while (DECODE_MAX > i) chBuf[i++] = ch;
            
            while (i > 0) chars[j++] = chBuf[--i];  // 反転コピー
        }
        return new String(chars,0,j);
    }

    /**
     *  デコード
     *  @param s 暗号文字
     */
    static public String decode(String s) {
        return decode(s,36);
    }
    static public synchronized String decode(String s, int radix) {
        int j = 0;
        chars[j++] = s.charAt(0);   //最初の２文字をコピー
        chars[j++] = s.charAt(1);
        int val = 0;
        try {
            val = Integer.parseInt(s.substring(2),radix);
        } catch (Exception ex) {
            System.out.println("Exeption:"+ex);
        }
        val -= ADD_VALUE;
        int i = 0;
        do {
            chBuf[i++] = chStr[(int)(val % 10)];
            val /= 10;
        } while (val > 0);

        while (ENCODE_MAX > i) chBuf[i++] = ch;

        while (i > 0) chars[j++] = chBuf[--i];  // 反転コピー
        return new String(chars,0,j);
    }
    
    /**
     *  ハッシュ用のキーを取得
     *  @param s 暗号文字
     */
    static public synchronized int getInt(String s) {
        String str = decode(s,36);
        int c1 = (int)str.charAt(0);
        int c2 = (int)str.charAt(1);
        
        if      (c1 >= 'a') c1 = c1 - 'a' + 10;
        else if (c1 >= 'A') c1 = c1 - 'A' + 10;
        if      (c2 >= 'a') c2 = c2 - 'a' + 10;
        else if (c2 >= 'A') c2 = c2 - 'A' + 10;
        
        int val = c1 * 36000000
                + c2 *  1000000;
        try {
            val += Integer.parseInt(str.substring(2),10);
        } catch (Exception ex) {
            System.out.println("giExeption:"+ex);
        }
        return val;
    }
    // ハッシュキーより元の暗号を抽出
    static public synchronized String getID(int no) {
        int j = 0;
        int i = no / 36000000;
        chars[j++] = chStr[i];
        no -= 36000000 * i;
        chars[j++] = chStr[no/1000000];
        int val = no % 1000000;
        i = 0;
        do {
            chBuf[i++] = chStr[(int)(val % 10)];
            val /= 10;
        } while (val > 0);

        while (ENCODE_MAX > i) chBuf[i++] = ch;

        while (i > 0) chars[j++] = chBuf[--i];  // 反転コピー
        return new String(chars,0,j);
    }
    
    /**
     *  顧客Noを取得
     *  @param s 復元文字
     */
    static public synchronized int getNo(String s) {
        int val = -1;
        try {
            val = Integer.parseInt(s.substring(5,8),10);
        } catch (Exception ex) {
            System.out.println("gnExeption:"+ex);
        }
        return val;
    }

    /**
     *  顧客Noを取得
     *  @param key ハッシュキー値
     */
    static public int getNo(int key) {
        return key % 1000;
    }
    
    /**
     *  顧客区分を取得
     *  @param s 復元文字
     */
    static public synchronized int getType(String s) {
        int val = -1;
        try {
            val = Integer.parseInt(s.substring(2,5),10);
        } catch (Exception ex) {
            System.out.println("gtExeption:"+ex);
        }
        return val;
    }
    /**
     *  顧客区分を取得
     *  @param key ハッシュキー値
     */
    static public int getType(int key) {
        return (key / 1000) % 1000;
    }
}

//
//
// [end of Crypt36.java]
//

