//------------------------------------------------------------------------
// @(#)CryptKey.java
//          複数数値埋め込み暗号キーを生成・取得する
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.util;       // 

/**
 *  複数数値埋め込み暗号キー生成取得ツール
 * @version 1.0 
 * @author Toru Ishioka
 * @since  JDK1.1
 */

public class CryptKey {
    private boolean debug = false;

    //////////////////////////////////////////////////////////////////////
    // エンコード
    //////////////////////////////////////////////////////////////////////

    /** エンコードする 
        @param keys 入力キーリスト
        @return 生成した暗号キー
    */
    static public CharArray encode(long... keys) {
        return encode(keys, "@");
    }
    
    /** エンコードする 
        @param keys 入力キーリスト
        @return 生成した暗号キー
    */
    static public CharArray encode(int... keys) {
        long[] lkeys = new long[keys.length];
        for (int i = 0; i < keys.length; i++) {
            lkeys[i] = (long)keys[i];
        }
        return encode(lkeys, "@");
    }
    
    /** エンコードする 
        @param keys 入力キーリスト
        @param separator 情報セパレータター(デフォルト"@")
        @return 生成した暗号キー
    */
    static public CharArray encode(long[] keys, String separator) {
        CharArray ch = new CharArray();
        
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) ch.add(separator);
            String str = Crypt62.encode(keys[i]);
            ch.add(anagram(str));
        }
        return anagram(ch);
    }

    //////////////////////////////////////////////////////////////////////
    // デコード
    //////////////////////////////////////////////////////////////////////

    /**
        デコードする
        @param key 暗号キー
        @return 情報キーリスト
    */
    static public CharArrayQueue decode(String key) {
        return decode(key, "@");
    }
    /**
        デコードする
        @param key 暗号キー
        @return 情報キーリスト
    */
    static public CharArrayQueue decode(CharArray key) {
        return decode(key, "@");
    }

    /**
        デコードする
        @param key 暗号キー
        @param separator 情報セパレータター(デフォルト"@")
        @return 情報キーリスト
    */
    static public CharArrayQueue decode(CharArray key, String separator) {
        return decode(key.toString(), separator);
    }
    /**
        デコードする
        @param key 暗号キー
        @param separator 情報セパレータター(デフォルト"@")
        @return 情報キーリスト
    */
    static public CharArrayQueue decode(String key, String separator) {
        CharArrayQueue queue = new CharArrayQueue();
        CharToken token = CharToken.pop();
        token.set(anagram(key), separator);
        for (int i = 0; i < token.size(); i++) {
            CharArray ch = anagram(token.get(i));
            long l = Crypt62.decode(ch);
            queue.enqueue(""+l);
        }
        CharToken.push(token);
        return queue;
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    /** アナグラム関数 */
    public static CharArray anagram(CharArray s) {
        CharArray ch = new CharArray(s);
        for (int i = 0; i < ch.length()/4; i++) {
            char c1 = ch.chars[i*2];
            char c2 = ch.chars[ch.length-i*2-2];
            ch.chars[i*2] = c2;
            ch.chars[ch.length-i*2-2] = c1;
        }
        return ch;
    }
    public static CharArray anagram(String s) {
        CharArray ch = new CharArray(s);
        for (int i = 0; i < ch.length()/4; i++) {
            char c1 = ch.chars[i*2];
            char c2 = ch.chars[ch.length-i*2-2];
            ch.chars[i*2] = c2;
            ch.chars[ch.length-i*2-2] = c1;
        }
        return ch;
    }
}

//
// $Author:$
// $Source:$
// $Revision:$
// $Date:$
//
// $Log:$
//
//
// [end of CryptKey.java]
//

