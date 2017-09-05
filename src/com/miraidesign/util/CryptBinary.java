//------------------------------------------------------------------------
// @(#)CryptBinary.java
//          バイナリデータ暗号化ツール
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.util;

/**
 *  バイナリデータ暗号化ツール
 * @version 1.0 2010-04-01
 * @author Toru Ishioka
 * @since  JDK1.1
 */

public class CryptBinary {
    private static int[] magic = { // 変換テーブル
       201,509,992,407,852, 21,513,546, 17, 63,345,
       303, 10,926,305,  6,444, 45, 76, 71,496,155,
       748,917,898, 94,248, 63, 49, 53,750, 12,713,
       813,815,916,964, 65,266,265, 67,467,  8, 18,
       995, 93,589,221,483,380, 75,670,358, 99,285,
       285, 61, 56,342,323, 24,425, 28,629, 50,731,
       933, 14,934,935, 36, 22,138, 39,240,143,952,
       155,662,951, 59,560, 97,872, 73,174,277, 78,
       279, 87,981, 84, 20,527, 32, 37, 43, 57,586,
       188, 91,692, 96,119,567,892
    };
    private static int magicMAX = 1024;  // 256 * 4

    /**
     *  エンコード
     *  @param by エンコード対象データ
     *  @return 暗号化データ
     */
    static public ByteArray encode(ByteArray by) {
        return encode(by, 0);
    }
    /**
     *  エンコード
     *  @param by エンコード対象データ
     *  @param key エンコードキー
     *  @return 暗号化データ
     */
    static public ByteArray encode(ByteArray by, int key) {
        ByteArray ret = new ByteArray();
        for (int i = 0; i < by.length(); i++) {
            int MAGIC = magic[(i+key)%magic.length];
            byte c = by.bytes[i];
            byte d = (byte)((c + MAGIC) & 0xff);
            ret.add(d);
        }
        return ret;
    }

    /**
     *  エンコード
     *  @param by エンコード文字列
     *  @param key エンコードキーワード
     *  @return 暗号化文字列
     */
    static public ByteArray encode(ByteArray by, String key) {
        ByteArray ret = new ByteArray();
        for (int i = 0; i < by.length(); i++) {
            int k = key.charAt(i%key.length());
            int MAGIC = magic[(i+k)%magic.length];
            byte c = by.bytes[i];
            byte d = (byte)((c + MAGIC) & 0xff);
            ret.add(d);
        }
        return ret;
    }
    /**
     *  デコード
     *  @param by 暗号文字列
     *  @return 解読文字列
     */
    static public ByteArray decode(ByteArray by) {
        return decode(by, 0);
    }
    /**
     *  デコード
     *  @param by 暗号データ
     *  @param key デコードキー
     *  @return 解読データ
     */
    static public ByteArray decode(ByteArray by, int key) {
        ByteArray ret = new ByteArray();
        for (int i = 0; i < by.length(); i++) {
            int MAGIC = magic[(i+key)%magic.length];
            byte c = by.bytes[i];
            byte d = (byte)((c+magicMAX-MAGIC) & 0xff);
            ret.add(d);
        }
        return ret;
    }
    /**
     *  デコード
     *  @param by 暗号データ
     *  @param key デコードキーワード
     *  @return 解読データ
     */
    static public ByteArray decode(ByteArray by, String key) {
        ByteArray ret = new ByteArray();
        for (int i = 0; i < by.length(); i++) {
            int k = key.charAt(i%key.length());
            int MAGIC = magic[(i+k)%magic.length];
            byte c = by.bytes[i];
            byte d = (byte)((c+magicMAX-MAGIC) & 0xff);
            ret.add(d);
        }
        return ret;
    }
}

//
// [end of CryptBinary.java]
//

