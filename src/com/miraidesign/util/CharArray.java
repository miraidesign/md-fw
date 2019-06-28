//------------------------------------------------------------------------
// @(#)CharArray.java
//              char[] ラッパー
//
//          Copyright (c) Mirai Design Institute 2010-15 All rights reserved.
//          update 2014-01-05   ishioka.toru@miraidesign.com
//--------------------------------------------------------------------
//       
package com.miraidesign.util;

import java.awt.Graphics;

import java.io.Serializable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

//import java.lang.StringCoding;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <b>再利用可能文字列</b> char[] <br>
 * 高速文字列処理目的でStringの替わりに使用（ＧＣも極力発生しない）<br>
 * ※スレッドセーフではないのに注意！
 *  
 *  @version 1.2 2010-02-03
 *  @author Toru Ishioka
 *  @since  JDK1.4
**/

public class CharArray implements Serializable, CharSequence {
    private static Stack<CharArray> bigStack = new Stack<CharArray>();
    private static int MIN_SIZE =  10;
    private static int MAX_SIZE = 512;
    
    /* CharArrayオブジェクトをスタック（大）に返却する */
    public static /*synchronized*/ CharArray pushBig(CharArray item) {
        if (bigStack.size() > MAX_SIZE || item == null) {
            item = null;    // for GC
            return item;
        } else {
            item.reset();
            item.setAppendMode(true);
            return (CharArray)bigStack.push(item);
        }
    }
    /* スタック（大）からCharArrayオブジェクトを取り出す */
    public static /*synchronized*/ CharArray popBig() {
        CharArray ch;
        synchronized (bigStack) {
            //if (bigStack.empty()) {    // スタックが空なら
            if (bigStack.size() < MIN_SIZE) {
                ch =  new CharArray();
            } else {
                ch = (CharArray)bigStack.pop();
            }
        }
        return ch;
    }
    /** スタック（大）からCharArrayオブジェクトを取り出す 
        @param str CharArrayを初期化する文字列
        @return CharArray
    */
    public static CharArray popBig(String str) {
        CharArray ch = popBig();
        ch.set(str);
        return ch;
    }
    /** スタック（大）からCharArrayオブジェクトを取り出す
        @param str CharArrayを初期化する文字列
        @return CharArray
    */
    public static CharArray popBig(CharArray str) {
        CharArray ch = popBig();
        ch.set(str);
        return ch;
    }
    
    /* スタック（大）に保管されている個数を取得する **/
    public static int getBigStackSize() {
        return bigStack.size();
    }
    
    
    /** 文字列格納バッファ */
    public char[] chars;
    
    /* データサイズを返す */
    public int sizeOf() {
        return chars.length * 2;
    }
    
    /** 文字列長 */
    public int length = 0;

    private static int ADD_SIZE = 64;           // バッファ拡大時の追加サイズ
    private static boolean DOUBLE_SIZE = true;  // true時は ADD_SIZE拡張を行わず、
                                                // バッファ量を倍にする拡張を行う

    private boolean isNull = false;     // NULLセットフラグ
    
    /** 型保存用 */
    public int type = Queue.STRING;
    
    /** ステータス（利用方法は自由) */
    public int status = 0;
    
    /** 
      文字列コピー 
      @param from コピー元文字列
      @return this
    */
    public CharArray copy(CharArray from) {
        set(from);
        if (from != null) {
            append_mode = from.append_mode;
            isNull = from.isNull;
            type = from.type;
            status = from.status;
            minus_top = from.minus_top;
            hash = from.hash;
        }
        return this;
    }
    
    /** 
        文字列を交換する（内容のみ交換）
        @param from 交換元文字列
    */
    public void exchange(CharArray from) {
        CharArray tmp = pop(this);
        this.set(from);
        if (from != null) from.set(tmp);
        CharArray.push(tmp);
    }
    
    //-------------------------------------------------
    // コンストラクタ
    //-------------------------------------------------
    /**
        CharArrayを生成する[default constructor]
    */
    public CharArray() { this(null,0,4); isNull = false;}
    
    /**
        CharArrayを生成する[constructor]
        @param ch コピーするchar配列
        @param offset コピー開始位置
        @param size   コピーサイズ
    */
    public CharArray(char[] ch,int offset,int size) {
        size = Math.abs(size);  // (^_^;)
        chars = new char[DOUBLE_SIZE ? size*2 : size+ADD_SIZE];
        if (ch != null) {
            length = Math.min(size,ch.length);
            System.arraycopy(ch,offset,chars,0,length);
        } else {
            isNull = true;
        }
    }
    
    /**
        CharArrayを生成する[constructor]
        @param ch コピーするchar配列
    */
    public CharArray(char[] ch) { this(ch,0,ch.length); } 

    /**
        CharArrayを生成する[constructor]
        @param size 初期バッファサイズ ※length()は 0 のまま
    **/
    public CharArray(int size) { this(null,0,size); isNull=false;}

    
    /**
        CharArrayを生成する[constructor]
        @param s 初期文字列
    */
    public CharArray(String s) {
        if (s != null) {
            int size = s.length();
            chars = new char[DOUBLE_SIZE ? size*2 :size+ADD_SIZE];
            s.getChars(0,size,chars,0);
            length = size;
        } else {
            isNull = true;
            chars = new char[4];
        }
    }
    /**
        CharArrayを生成する[constructor]
        @param s 初期文字列
    */
    public CharArray(StringBuffer s) {
        if (s != null) {
            int size = s.length();
            chars = new char[DOUBLE_SIZE ? size*2 :size+ADD_SIZE];
            s.getChars(0,size,chars,0);
            length = size;
        } else {
            isNull = true;
            chars = new char[4];
        }
    }
    /**
        CharArrayを生成する[constructor]
        @param s 初期文字列
    */
    public CharArray(CharSequence s) {
        if (s != null) {
            int size = s.length();
            chars = new char[DOUBLE_SIZE ? size*2 :size+ADD_SIZE];
            for (int i = 0; i < size; i++) {
                chars[i] = s.charAt(i);
            }
            length = size;
        } else {
            isNull = true;
            chars = new char[4];
        }
    }
    /**
        CharArrayを生成する[copy constructor]
        @param from コピー元のCharArray
    */
    public CharArray(CharArray from) {
        if (from != null) {
            chars = new char[from.length+ADD_SIZE];
            set(from.chars,0,from.length);
            append_mode = from.append_mode;
            isNull = from.isNull;
            type = from.type;
            status = from.status;
            minus_top = from.minus_top;
            hash = from.hash;
        } else {
            isNull = true;
            chars = new char[4];
        }
    }
    /**
        CharArrayを生成する[constructor]
        @param c １文字
    */
    public CharArray(char c) { 
        chars = new char[4];
        set(c);
    }
    /**
        CharArrayを生成する[constructor]
        byte[]からCharArrayを生成する(encode処理なし)
        @param bytes コピー元の byte 配列
    */
    public CharArray(byte[] bytes) {
        if (bytes != null) {
            int size = bytes.length;
            chars = new char[size/2 + 4];
        
            for (int i = 0; i < size; i += 2) {
                int hi = bytes[i] & 0xff;
                int lo = (i+1 < size) ? bytes[i+1] & 0xff: 0;
                chars[length++] = (char)(((lo << hi) + (lo << 0)));
            }
        } else {
            isNull = true;
            chars = new char[4];
        }
    }
    
    //--------------------------------------
    /** 文字列の長さを返す
        @return 文字列の長さ 
    */
    public int length() { return length;}
    
    /* 文字列をリセット（クリア）する **/
    public CharArray clear() { length = 0; hash = 0; isNull = false; type = Queue.STRING; status = 0; return this;}
    
    /* 文字列をリセット（クリア）する **/
    public CharArray reset() { length = 0; hash = 0; isNull = false; type = Queue.STRING; status = 0; return this;}
    
    /* 試験用リセット：呼び出さない事 */
    protected CharArray reset(int size) {
        if (size > 0) chars = new char[size];
        else          chars = null;
        return reset();
    }

    /* Null がセットされたか？ */
    public boolean isNull() {
        if (length > 0) isNull = false;
        return isNull;
    }
    /* 型取得 */
    public int getDataType() {
        return type;
    }
    public void setDataType(int type) {
        this.type = type;
    }

    /** 
        文字列設定 
        @param ch     コピーする文字列
        @return this
    */
    public CharArray set(CharArray ch) {
        if (ch == null) {
            clear();
            isNull = true;
            return this;
        }
        isNull = ch.isNull();
        return set(ch.chars,0,ch.length);
    }
    /** 
        文字列設定 
        @param ch     コピーする文字列
        @param offset コピー開始位置
        @return this
    */
    public CharArray set(CharArray ch, int offset) {
        if (ch == null) {
            clear();
            isNull = true;
            return this;
        }
        isNull = ch.isNull();
        return set(ch.chars,offset,ch.length-offset);
    }
    /** 
        文字列設定 
        @param ch     コピーする文字列
        @param offset コピー開始位置
        @param size   コピーするサイズ
        @return this
    */
    public CharArray set(CharArray ch, int offset, int size) {
        if (ch == null) {
            clear();
            isNull = true;
            return this;
        }
        if (size < 0) {
            size = ch.length - offset;
        }
        isNull = ch.isNull();
        return set(ch.chars,offset,size);
    }
    
    /** 
        文字列設定
        @param ch コピーするchar配列 
        @return this
     */
    public CharArray set(char[] ch) {
        if (ch == null) {
            clear();
            isNull = true;
            return this;
        }
        return set(ch,0,ch.length);
    }
    /** 
        文字列設定
        @param ch コピーするchar配列 
        @param offset コピー開始位置
        @param size   コピーするサイズ
        @return this
     */
    public CharArray set(char[] ch,int offset,int size) {
        if (ch == null || size <= 0) {
            clear();
            if (ch == null) isNull = true;
            return this;
        }
        if (chars.length < size) {
            chars = null;
            chars = new char[DOUBLE_SIZE ? size*2 :size+ADD_SIZE];
        }
        System.arraycopy(ch,offset,chars,0,size);
        length = size; hash = 0;
        isNull = false;
        return this;
    }

    /** 
        一文字設定
        @param c セットする文字
        @return this
    */
    public CharArray set(char c) {
        if (chars.length < 1) {
            chars = new char[4];
        }
        chars[0] = c;
        length = 1; hash = 0;
        isNull = false;
        return this;
    }
    /** 
        文字列設定
        @param seq コピーする文字列
        @return this
    */
    public CharArray set(CharSequence seq) {
        if (seq == null) {
            clear();
            isNull = true;
            return this;
        }
        int size = seq.length();
        if (chars.length < size) {
            chars = null;
            chars = new char[DOUBLE_SIZE ? size*2 :size+ADD_SIZE];
        }
        if (seq instanceof CharArray) {
            System.arraycopy(((CharArray)seq).chars,0,chars,0,size);
        } else if (seq instanceof String) {
            ((String)seq).getChars(0,size,chars,0);
        } else if (seq instanceof StringBuffer) {
            ((StringBuffer)seq).getChars(0,size,chars,0);
        } else if (seq instanceof StringBuilder) {
            ((StringBuilder)seq).getChars(0,size,chars,0);
        } else {
            for (int i = 0; i < size; i++) {
                chars[i] = seq.charAt(i);
            }
        }
        length = size; hash = 0;
        isNull = false;
        return this;
    }
    /** 
        文字列設定
        @param s コピーする文字列
        @return this
    */
    public CharArray set(String s) {
        if (s == null) {
            clear();
            isNull = true;
            return this;
        }
        int size = s.length();
        if (chars.length < size) {
            chars = null;
            chars = new char[DOUBLE_SIZE ? size*2 :size+ADD_SIZE];
        }
        s.getChars(0,size,chars,0);
        length = size; hash = 0;
        isNull = false;
        return this;
    }
    /** 
        文字列設定
        @param s コピーする文字列
        @return this
    */
    public CharArray set(StringBuffer s) {
        if (s == null) {
            clear();
            isNull = true;
            return this;
        }
        int size = s.length();
        if (chars.length < size) {
            chars = null;
            chars = new char[DOUBLE_SIZE ? size*2 :size+ADD_SIZE];
        }
        s.getChars(0,size,chars,0);
        length = size; hash = 0;
        isNull = false;
        return this;
    }
    
    /** 
        数値をフォーマットして設定 
        @param i セットする数値
        @return this
    */
    public CharArray set(int i) {
        clear();
        return format(i);
    }
    /** 
        数値をフォーマットして設定 
        @param l セットする数値
        @return this
    */
    public CharArray set(long l) {
        clear();
        return format(l);
    }
    /** 
        論理値設定(true,false)
        @param b セットする値
        @return this
    */
    public CharArray set(boolean b) {
        return set(b? "true": "false");
    }

    /* 文字列設定 */
    public CharArray setNotNull(String s) {
        isNull = false;
        if (s == null) {
            clear();
            return this;
        }
        int size = s.length();
        if (chars.length < size) {
            chars = new char[DOUBLE_SIZE ? size*2 :size+ADD_SIZE];
        }
        //System.arraycopy(ch,offset,chars,length,size);
        s.getChars(0,size,chars,0);
        length = size; hash = 0;
        return this;
    }

    /* 文字列設定 */
    public CharArray setNotNull(CharArray ch) {
        isNull = false;
        if (ch == null) {
            clear();
            return this;
        }
//System.out.println("(1):"+isNull);
        return set(ch.chars,0,ch.length);
    }
    /* 文字列設定 */
    public CharArray setNotNull(CharArray ch, int offset) {
        isNull = false;
        if (ch == null) {
            clear();
            return this;
        }
        return set(ch.chars,offset,ch.length-offset);
    }
    /** 文字列設定 
        @param ch     コピーする文字列
        @param offset コピー開始位置
        @param size   コピーするサイズ
        @return this
    */
    public CharArray setNotNull(CharArray ch, int offset, int size) {
        isNull = false;
        if (ch == null) {
            clear();
            return this;
        }
        if (size < 0) {
            size = ch.length - offset;
        }
        return set(ch.chars,offset,size);
    }
    /* 文字列設定 */
    public CharArray setNotNull(char[] ch) {
        isNull = false;
        if (ch == null) {
            clear();
            return this;
        }
        return set(ch,0,ch.length);
    }


    /** 
        文字列追加 
        @param ch 追加文字列
        @return this
    */
    public CharArray append(CharArray ch) {
        return add(ch);
    }
    /**
       文字列追加 
       @param s 追加文字列
        @return this
    */
    public CharArray append(String s) {
        return add(s);
    }
    /**
       文字追加 
       @param c 追加文字列
       @return this
    */
    public CharArray append(char c) {
        return add(c);
    }
    /** 
        文字列追加 
        @param ch 追加文字列
        @return this
    */
    public CharArray add(CharArray ch) {
        if (ch == null) return this;
        return add(ch.chars,0,ch.length);
    }
    /** 文字列追加 
        @param ch 追加文字列
        @param offset 開始位置
        @return this
    */
    public CharArray add(CharArray ch,int offset) {
        if (ch == null) return this;
        return add(ch.chars,offset,ch.length-offset);
    }
    /** 文字列追加 
        @param ch 追加文字列
        @param offset 開始位置
        @param size サイズ
        @return this
    */
    public CharArray add(CharArray ch,int offset, int size) {
        if (ch == null) return this;
        if (size < 0) {
            size = ch.length() - offset;
        }
        return add(ch.chars,offset,size);
    }
    /** 文字列追加 
        @param str 追加文字列
        @param offset 開始位置
        @param size サイズ
        @return this
    */
    public CharArray add(String str,int offset, int size) {
        if (str == null) return this;
        if (size < 0) {
            size = str.length() - offset;
        }
        return add(str.toCharArray(),offset,size);
    }
    /** 文字列追加 
        @param ch 追加文字列
        @return 自分自身を返す
    */
    public CharArray add(char[] ch) {
        if (ch == null) return this;
        return add(ch,0,ch.length);
    }
    /** 文字列追加 
        @param ch 追加文字列
        @param offset 開始位置
        @return 自分自身を返す
    
    */
    public CharArray add(char[] ch, int offset) {
        if (ch == null) return this;
        return add(ch,offset,ch.length-offset);
    }
    /** 文字列追加
        @param ch 追加文字列
        @param offset 開始位置
        @param size サイズ
        @return 自分自身を返す
     */
    public CharArray add(char[] ch,int offset,int size) {
        if (ch == null || size <= 0) return this;
        if (chars.length < length+size) {
            char[] new_char = new char[DOUBLE_SIZE ? (length+size)*2 :length+size+ADD_SIZE];
            System.arraycopy(chars,0,new_char,0,length);
            chars = null;
            chars = new_char;
        }
        System.arraycopy(ch,offset,chars,length,size);
        length += size;  hash = 0;
        isNull = false;
        return this;
    }
    /** 一文字追加 
        @param c 追加文字
        @return this
    */
    public CharArray add(char c) {
        if (chars.length < length+1) {
            char[] new_char = new char[DOUBLE_SIZE ? (length+1)*2 :length+1+ADD_SIZE];
            System.arraycopy(chars,0,new_char,0,length);
            chars = null;  // 
            chars = new_char;
        }
        chars[length++] = c; hash = 0;
        isNull = false;
        return this;
    }
    /** 文字列追加 
        @param s 追加文字列
        @return this
    */
    public CharArray add(String s) {
        if (s != null) {
            int size = s.length();
            if (chars.length < length+size) {
                char[] new_char = new char[DOUBLE_SIZE ? (length+size)*2 :length+size+ADD_SIZE];
                System.arraycopy(chars,0,new_char,0,length);
                chars = null;
                chars = new_char;
            }
            s.getChars(0,size,chars,length);
            length += size; hash = 0;
            isNull = false;
        }
        return this;
    }
    /** 文字列追加 
        @param seq 追加文字列
        @return this
    */
    public CharArray add(CharSequence seq) {
        if (seq != null) {
            int size = seq.length();
            if (chars.length < length+size) {
                char[] new_char = new char[DOUBLE_SIZE ? (length+size)*2 :length+size+ADD_SIZE];
                System.arraycopy(chars,0,new_char,0,length);
                chars = null;
                chars = new_char;
            }
            if (seq instanceof CharArray) {
                System.arraycopy(((CharArray)seq).chars,0,chars,length,size);
            } else if (seq instanceof String) {
                ((String)seq).getChars(0,size,chars,length);
            } else if (seq instanceof StringBuffer) {
                ((StringBuffer)seq).getChars(0,size,chars,length);
            } else if (seq instanceof StringBuilder) {
                ((StringBuilder)seq).getChars(0,size,chars,length);
            } else {
                for (int i = 0; i < size; i++) {
                    chars[length+i] = seq.charAt(i);
                }
            }
            length += size; hash = 0;
            isNull = false;
        }
        return this;
    }
    
    /** 数値を文字列にフォーマットして追加 
        @param i 追加数値
        @return this
    */
    public CharArray add(int i) { return format(i);}
    
    /** 数値を文字列にフォーマットして追加 
        @param l 追加数値
        @return this
    */
    public CharArray add(long l) { return format(l);}
    
    /** 文字列挿入
        @param index    挿入する位置
        @param ch       挿入する文字
        @return this
     */
    public CharArray insert(int index,CharArray ch) {
        if (ch == null) return this;
        return insert(index,ch.chars,0,ch.length);
    }
    /** 文字列挿入 
        @param index    挿入する位置
        @param ch       挿入する文字
        @return this
    */
    public CharArray insert(int index,char[] ch) {
        if (ch == null) return this;
        return insert(index,ch,0,ch.length);
    }
    /* 文字列挿入 */
    public CharArray insert(int index,char[] ch,int offset,int size) {
        if (ch != null) {
            if (chars.length < length+size) {
                char[] new_char = new char[DOUBLE_SIZE ? (length+size)*2 :length+size+ADD_SIZE];
                System.arraycopy(chars,0,new_char,0,length);
                chars = new_char;
            }
            if (index <= length) {
                synchronized (chBuf) {
                    if (chBuf.length < length+size) {
                        chBuf = new char[length+size+ADD_SIZE];
                    }
                    System.arraycopy(chars,0,chBuf,0,index);
                    System.arraycopy(ch,offset,chBuf,index,size);
                    System.arraycopy(chars,index,chBuf,index+size,length-index);
                    System.arraycopy(chBuf,0,chars,0,length+size);
                }
                length += size;  hash = 0;
            }
            isNull = false;
        } else if (length == 0) {
            isNull = true;
        }
        return this;
    }
    /* 一文字挿入 */
    public CharArray insert(int index,char c) {
        if (chars.length < length+1) {
            char[] new_char = new char[length+3];
            System.arraycopy(chars,0,new_char,0,length);
            chars = new_char;
        }
        if (index <= length) {
            synchronized (chBuf) {
                if (chBuf.length < length+1) {
                    chBuf = new char[length+3];
                }
                System.arraycopy(chars,0,chBuf,0,index);
                chBuf[index] = c;
                System.arraycopy(chars,index,chBuf,index+1,length-index);
                System.arraycopy(chBuf,0,chars,0,length+1);
            }
            ++length;  hash = 0;
            isNull = false;
        }
        return this;
    }
    /* 文字列挿入 */
    public CharArray insert(int index,String s) {
        if (s != null) {
            int size = s.length();
            if (chars.length < length+size) {
                char[] new_char = new char[length+size+ADD_SIZE];
                System.arraycopy(chars,0,new_char,0,length);
                chars = new_char;
            }
            if (index <= length) {
                synchronized (chBuf) {
                    if (chBuf.length < length+size) {
                        chBuf = new char[length+size+ADD_SIZE];
                    }
                    System.arraycopy(chars,0,chBuf,0,index);
                    s.getChars(0,size,chBuf,index);
                    System.arraycopy(chars,index,chBuf,index+size,length-index);
                    System.arraycopy(chBuf,0,chars,0,length+size);
                }
                length += size;  hash = 0;
                isNull = false;
            }
        } else if (length == 0) {
            isNull = true;
        }
        return this;
    }
    
    /* 先頭に文字列挿入 */
    public CharArray insert(CharArray s) {
        return insert(0,s);
    }
    /* 先頭に文字列挿入 */
    public CharArray insert(String s) {
        return insert(0,s);
    }
        
    /** 指定index以降を全て削除
        @param index 削除位置
        @return this
    */
    public CharArray remove(int index) {
        if (index >= 0 && index <length) {
            length = index;  hash = 0;
        }
        return this;
    }

    /** 指定index以降の文字列を削除 
        @param index 削除位置
        @param size  削除する長さ
        @return this
    */
    public CharArray remove(int index, int size) {
        if (index >= 0 && index+size <= length) {
            System.arraycopy(chars,index+size,chars,index,length-index-size);
            length -= size; hash = 0;
        }
        return this;
    }

    /* 文字列(String)変換 */
    public String toString() {
        return isNull ? null : String.valueOf(chars,0,length);
    }
    /** 文字列(String)変換<br>
        @param index 変換開始位置
        @return this
     */
    public String toString(int index) {
        return isNull ? null : String.valueOf(chars,index,length-index);
    }
    /** 文字列(String)変換<br>
        @param index 変換開始位置
        @param size サイズ
        @return this
     */
    public String toString(int index, int size) {
        return isNull ? null : String.valueOf(chars,index,size);
    }
    /**
        サブシーケンスを返す
        @param start    開始インデックス
        @param end      終了インデックス
        @return CharSequence
    */
    public CharSequence subSequence(int start, int end) {
        CharArray ch = CharArray.pop();
        if (start < 0 || end < 0 || end > length || start > end) {
            throw new IndexOutOfBoundsException();
        }
        if (start <length && start >=0 && start < end) {
            for (int i = start; i < end; i++) {
                ch.add(charAt(i));
            }
        }
        return ch;
    }
    
    /* 部分文字列の出力 */
    public String substring(int beginIndex) {
        if (beginIndex < 0 || beginIndex >= length) return null;
        CharArray tmp = CharArray.pop();
        tmp.set(this, beginIndex);
        String str = tmp.toString();
        CharArray.push(tmp);
        return str;
    }
    
    /* 部分文字列の出力 */
    public String substring(int beginIndex, int endIndex) {
        if (beginIndex < 0 || beginIndex >= length) return null;
        if (endIndex < beginIndex || endIndex > length) return null;
        CharArray tmp = CharArray.pop();
        tmp.set(this, beginIndex, endIndex-beginIndex);
        String str = tmp.toString();
        CharArray.push(tmp);
        return str;
    }

    /* 部分文字列の出力(新CharArray生成) 
        @param beginIndex 開始インデックス
        @return CharArray
    */
    public CharArray substr(int beginIndex) {
        if (beginIndex < 0 || beginIndex >= length) return null;
        CharArray ch = CharArray.pop();
        ch.set(this, beginIndex);
        return ch;
    }
    
    /** 部分文字列の出力(新CharArray生成) <br>
        <pre>
        自分自身を変更したい場合はこの関数を使用せずに、
            length = endIndex;
            remove(0, startIndex); とすると効率が良い
        </pre>
        @param beginIndex 開始インデックス
        @param endIndex 開始インデックス
        @return 新CharArray
    */
    public CharArray substr(int beginIndex, int endIndex) {
        if (beginIndex < 0 || beginIndex >= length) return null;
        if (endIndex < beginIndex || endIndex > length) return null;
        CharArray ch = CharArray.pop();
        ch.set(this, beginIndex, endIndex-beginIndex);
        return ch;
    }
    
    //--------------------------
    // 数値文字列変換  （注）スレッドセーフではない
    //--------------------------
    private static int MAX_RADIX = 62;  //66 にすると _-@$も使用する
    private static char[] chStr = {'0','1','2','3','4','5','6','7','8','9',
        'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o',
        'p','q','r','s','t','u','v','w','x','y','z',
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O',
        'P','Q','R','S','T','U','V','W','X','Y','Z','_','-','@','$'};
    private static char[] chBuf = new char[2048];   // テンポラリバッファ
    private boolean append_mode = true;             // format(Div)のみで有効

    /* 追加モード設定（format(Div)のみで有効） */
    public void setAppendMode(boolean mode) { 
        append_mode = mode;
    }
    /* 追加モードを取得する  */
    public boolean getAppendMode() { return append_mode; }
    
    /* 整数→テキスト変換 */
    public CharArray format(int i) { return format((long)i,10);}
    /* 整数→テキスト変換 */
    public CharArray format(int i,int radix) { return format((long)i,radix);}
    /* 整数→テキスト変換 */
    public CharArray format(long i) { return format(i,10);}

    /** 整数→テキスト変換 
        @param val 整数
        @param radix 基数 (2-36)
        @return 文字列(this)
    **/
    public CharArray format(long val,   // 整数
                       int radix) {     // 基数 (2-36)
        if (radix >= 2 && radix <= MAX_RADIX) {
            int i = 0;
            boolean sign = (val < 0);
            if (sign) val = -val;
            synchronized (chBuf) {
                do {
                    chBuf[i++] = chStr[(int)(val % (long)radix)];
                    val /= radix;
                } while (val > 0);
                int j = append_mode ? length : 0;
                if (chars.length < i+j+1) {
                    char[] new_char = new char[DOUBLE_SIZE ? (i+j)*2 :i+j+ADD_SIZE];
                    System.arraycopy(chars,0,new_char,0,length);
                    chars = new_char;
                }
                length = i+j;  hash = 0;
                if (sign) {chars[j++] = '-'; length++;}
                while (i > 0) chars[j++] = chBuf[--i];  // 反転コピー
            }
            isNull = false;
        }
        return this;
    }
    
    public CharArray format(int val,int radix,int total) {
        return format((long)val,radix,total,' ');
    }
    public CharArray format(int val,int radix,int total,char ch) {
        return format((long)val,radix,total,ch);
    }
    /** 整数→テキスト変換 
        @param val 整数
        @param radix 基数 (2-62)
        @param total 有効桁数
        @return 文字列(this)
    **/
    public CharArray format(long val,int radix,int total) {
        return format(val,radix,total,' ');
    }
    public boolean minus_top = false;   // 有効桁数指定時に true でマイナスを
                                        // スペーシング文字の前に持ってゆく

    /** 整数→テキスト変換 
        @param val 整数
        @param radix 基数 (2-62)
        @param total 有効桁数
        @param ch スペーシング文字  or ','
        @return 文字列(this)
    **/
    public CharArray format(long val, // 整数
                       int radix,     // 基数 (2-36)
                       int total,     // 有効桁数
                       char ch) {     // スペーシング文字  or ','
        if (radix >= 2 && radix <= MAX_RADIX) {
            int i = 0;
            boolean sign = (val < 0);
            boolean isComma = (ch == ',');
            if (isComma) ch = ' ';
            if (sign) val = -val;
            synchronized (chBuf) {
                do {
                    if (isComma && ((i+1)%4)==0) chBuf[i++] = ',';
                    chBuf[i++] = chStr[(int)(val % (long)radix)];
                    val /= radix;
                } while (val > 0);
                if (sign) {
                    if (minus_top) {
                        --total;
                    } else {
                        chBuf[i++] = '-';
                    }
                }
                while (total > i) chBuf[i++] = ch;
                if (sign && minus_top) {chBuf[i++] = '-';}
                int j = append_mode ? length : 0;
                if (chars.length < i+j+1) {
                    char[] new_char = new char[DOUBLE_SIZE ? (i+j)*2 :i+j+ADD_SIZE];
                    System.arraycopy(chars,0,new_char,0,length);
                    chars = new_char;
                }
                length = i+j;  hash = 0;
                while (i > 0) chars[j++] = chBuf[--i];  // 反転コピー
            }
            isNull = false;
        }
        return this;
    }
    /*
     *  浮動小数点→テキスト変換
     */
    public CharArray format(double val) { return format(val,2,-1,' ');}
    public CharArray format(double val,
                       int decimal) {   // 小数以下桁数
        return format(val,decimal,-1,' ');
    }
    
    public CharArray format(double val,
                       int decimal,     // 小数以下桁数
                       int total) {     // 有効桁数
        return format(val,decimal,total,' ');
    }
    
    
    /**
     *  浮動小数点→テキスト変換
     *  @param val 浮動小数
     *  @param decimal 小数以下桁数
     *  @param total 有効桁数
     *  @param ch スペーシング文字
     *  @return this
     **/
    public CharArray format(double val, // 浮動小数
                       int decimal,     // 小数以下桁数
                       int total,       // 有効桁数
                       char ch) {       // スペーシング文字
        if (decimal >= 0 && decimal <= 20) {
            boolean sign = (val < 0.0);
            if (sign) val = -val;
            
            int i = 1;
            int j = 0;
            for (; j < decimal; j++) { i *= 10; }
            long num = Math.round(val*i);
            long num1 = num / i;
            long num2 = num % i;
            synchronized (chBuf) {
                for (i = 0; i < decimal; i++) {
                    chBuf[i] = chStr[(int)(num2 % 10)];
                    num2 /= 10;
                }
                chBuf[i++] = '.';
                do {
                    chBuf[i++] = chStr[(int)(num1 % 10)];
                    num1 /= 10;
                } while (num1 > 0);
                
                if (sign) {
                    if (minus_top) {
                        --total;
                    } else {
                        chBuf[i++] = '-';
                    }
                }
                while (total > i) chBuf[i++] = ch;
                if (sign && minus_top) {chBuf[i++] = '-';}

                j = append_mode ? length : 0;
                if (chars.length < i+j+1) {
                    char[] new_char = new char[DOUBLE_SIZE ? (i+j)*2 :i+j+ADD_SIZE];
                    System.arraycopy(chars,0,new_char,0,length);
                    chars = new_char;
                }
                length = i+j;   hash = 0;
                while (i > 0) chars[j++] = chBuf[--i];  // 反転コピー
            }
            isNull = false;
        }
        return this;
    }
    
    /**
        整数／除数→テキスト変換
        @param val 被除数
        @param div 除数
        @param decimal 小数以下桁数
        @return 文字列
     */
    public CharArray formatDiv(int val, int div, int decimal) {
        double d = (div == 0) ? 0.0 : (double)val/div;
        return format(d,decimal,-1,' ');
    }
    /**
        整数／除数→テキスト変換
        @param val 被除数
        @param div 除数
        @param decimal 小数以下桁数
        @return 文字列
     */
    public CharArray formatDiv(long val,int div,int decimal) {
        double d = (div == 0) ? 0.0 : (double)val/div;
        return format(d,decimal,-1,' ');
    }
    /**
        整数／除数→テキスト変換
        @param val 被除数
        @param div 除数
        @param decimal 小数以下桁数
        @param total 有効桁数
        @return 文字列
     */
    public CharArray formatDiv(long val,        // 被除数
                               int div,         // 除数
                               int decimal,     // 小数以下桁数
                               int total) {     // 有効桁数
        double d = (div == 0) ? 0.0 : (double)val/div;
        return format(d,decimal,total,' ');
   }
    /**
     *  val/div の結果を format(double,,) に渡す
     *  @param val 被除数
     *  @param div 被除数
     *  @param decimal 小数以下桁数
     *  @param total 有効桁数
     *  @param ch スペーシング文字
     *  @return フォーマット済み文字列
     */
    public CharArray formatDiv(long val,        // 被除数
                               int div,         // 除数
                               int decimal,     // 小数以下桁数
                               int total,     // 有効桁数
                               char ch) {     // スペーシング文字
        double d = (div == 0) ? 0.0 : (double)val/div;
        return format(d,decimal,total,ch);
    }

    /**
        文字列フォーマット
        @param str フォーマット定義
        @param params パラメータ
        @return フォーマット済み文字列
    */
    public static CharArray format(String str, QueueElement params) {
        return Format.format(new CharArray(str), params);
    }
    public static CharArray format(CharArray str, QueueElement params) {
        return Format.format(str, params);
    }

    /**
        文字列フォーマット
        @param str フォーマット定義
        @param hp パラメータ
        @return フォーマット済み文字列
    */
    public static CharArray format(String str, HashParameter hp) {
        return Format.format(new CharArray(str), hp);
    }
    public static CharArray format(CharArray str, HashParameter hp) {
        return Format.format(str, hp);
    }
    /**
        文字列フォーマット
        @param str フォーマット定義
        @param table テーブルデータ
        @return フォーマット済み文字列
    */
    public static CharArray format(String str, QueueTable table) {
        return Format.format(new CharArray(str), table);
    }
    public static CharArray format(CharArray str, QueueTable table) {
        return Format.format(str, table);
    }

    /**
        文字列フォーマット （自分自身がフォーマット定義を持っている場合）
        @param params パラメータ
        @return フォーマット済み文字列
    */
    public CharArray format(QueueElement params) {
        return Format.format(this, params);
    }

    /**
        文字列フォーマット （自分自身がフォーマット定義を持っている場合）
        @param hp パラメータ
        @return フォーマット済み文字列
    */
    public CharArray format(HashParameter hp) {
        return Format.format(this, hp);
    }

    /**
        文字列フォーマット （自分自身がフォーマット定義を持っている場合）
        @param table テーブルデータ
        @return フォーマット済み文字列
    */
    public CharArray format(QueueTable table) {
        return Format.format(this, table);
    }
    /**
        文字列フォーマット （自分自身がフォーマット定義を持っている場合）
        @param table テーブルデータ
        @param start 開始行
        @param max   最大処理行数
        @return フォーマット済み文字列
    */
    public CharArray format(QueueTable table,int start, int max) {
        return Format.format(this, table, start, max);
    }
    /**
        文字列フォーマット （自分自身がフォーマット定義を持っている場合）
        @param table テーブルデータ
        @param start 開始行
        @param max   最大処理行数
        @param position データ順指定
        @return フォーマット済み文字列
    */
    public CharArray format(QueueTable table,int start, int max, IntQueue position) {
        return Format.format(this, table, start, max, position);
    }

    /*
     *  数値変換
     */
    static public int getInt(String str) {
        CharArray ch = CharArray.pop();
        int no = ch.set(str).getInt();
        CharArray.push(ch);
        return no;
    }
    static public int getInt(String str,int offset, int radix) {
        CharArray ch = CharArray.pop();
        int no = ch.set(str).getInt(offset,radix);
        CharArray.push(ch);
        return no;
    }
    static public int getInt(CharArray str) {
        CharArray ch = CharArray.pop();
        int no = ch.set(str).getInt();
        CharArray.push(ch);
        return no;
    }
    static public int getInt(CharArray str,int offset, int radix) {
        CharArray ch = CharArray.pop();
        int no = ch.set(str).getInt(offset,radix);
        CharArray.push(ch);
        return no;
    }
    static public long getLong(String str) {
        CharArray ch = CharArray.pop();
        long no = ch.set(str).getLong();
        CharArray.push(ch);
        return no;
    }
    static public long getLong(CharArray str) {
        CharArray ch = CharArray.pop();
        long no = ch.set(str).getLong();
        CharArray.push(ch);
        return no;
    }
    static public double getDouble(String str) {
        CharArray ch = CharArray.pop();
        double no = ch.set(str).getDouble();
        CharArray.push(ch);
        return no;
    }
    static public double getDouble(CharArray str) {
        CharArray ch = CharArray.pop();
        double no = ch.set(str).getDouble();
        CharArray.push(ch);
        return no;
    }
    static public boolean getBoolean(String str) {
        CharArray ch = CharArray.pop();
        boolean flg = ch.set(str).getBoolean();
        CharArray.push(ch);
        return flg;
    }
    static public boolean getBoolean(CharArray str) {
        CharArray ch = CharArray.pop();
        boolean flg = ch.set(str).getBoolean();
        CharArray.push(ch);
        return flg;
    }
    
    public int  getInt()  { return (int)getLong(0,10);}
    public int  getInt(int radix)  { return (int)getLong(0,radix);}
    public int  getInt(int offset,int radix){return (int)getLong(offset,radix);}
    public long getLong() { return getLong(0,10);}
    public long getLong(int radix) { return getLong(0,radix);}
    /**
       long 値を取得する
       @param offset チェック開始位置
       @param radix  基数
       @return long値
    **/
    public long getLong(int offset,int radix) {
        long result = 0;
        int i = offset;
        boolean sign = false;
        while (i < length) {
            char c = chars[i];
            if (c == '-') sign = !sign;
            else if (c == '+') ;// do notiong
            else if (c > ' ') break;
            i++;
        }
        while (i < length) {
            if (chars[i] == ',') {
                i++;
                continue;
            }
            int j = Character.digit(chars[i],radix);
            if (j < 0) break;
            result *= radix;
            result += j;
            i++;
        }
        return sign ? -result : result;
    }
    
    /*
       double 値を取得する  (E形式はだめ)
    */
    public double getDouble() { return getDouble(0);}
    /**
       double 値を取得する  (E形式はだめ)
       @param offset チェック開始位置
       @return double値
    */
    public double getDouble(int offset) { // 基数
        double result = 0;
        long div = 1;
        int i = offset; int j = 0;
        boolean sign = false;
        while (i < length) {
            char c = chars[i];
            if (c == '-') sign = !sign;
            else if (c == '+') ;    // do nothing
            else if (c > ' ') break;
            i++;
        }
        while (i < length) {
            if (chars[i] == '.') {
                i++;
                break;
            }
            if (chars[i] == ',') {
                i++;
                continue;
            }
            j = Character.digit(chars[i],10);
            if (j < 0) break;
            result *= 10;
            result += j;
            i++;
        }
        while (i < length) {
            j = Character.digit(chars[i],10);
            if (j < 0) break;
            result *= 10;   div *= 10;
            result += j;
            i++;
        }
        result /= div;
        
        return sign ? -result : result;
    }

    /*
        "on", "t*" , ".t", "yes", "enabled", "0"以外 でtrueを返す
    */
    public boolean getBoolean() {
        boolean sts = false;
        CharArray ch = CharArray.pop(this).trim().toLowerCase();
        if (ch.length() > 0) {
            if (ch.startsWith("on"))        sts = true;
            else if (ch.startsWith("t")) sts = true;
            else if (ch.startsWith(".t")) sts = true;
            else if (ch.startsWith("y"))  sts = true;
            else if (ch.startsWith("enabled"))  sts = true;
            else if (ch.getInt() != 0)  sts = true;
        }
        CharArray.push(ch);
        return sts;
    }

    //----------------------------------------------------------------
    //  空白除去／文字コンバート
    //----------------------------------------------------------------
    /*
     *  文字列両端の空白、コントロールコードを除去
     */
    public CharArray trim() {
        trimR();
        trimL();
        return this;
    }
    /*
     *  文字列両端の空白、コンロールコード、指定の１文字を除去
     */
    public CharArray trim(char c) {
        trimR(c);
        trimL(c);
        return this;
    }
    /*
     *  文字列両端の空白、コンロールコード、左右指定の１文字づつを除去
     */
    public CharArray trim(char left, char right) {
        trimR(right);
        trimL(left);
        return this;
    }
    /*
     *  文字列左側の空白、コントロールコードを除去
     */
    public CharArray trimL() {
        return trimL((char)0);
    }
    /*
     *  文字列左側の空白、コントロールコード、指定の１文字を除去
     */
    public CharArray trimL(char c) {
        int i = 0;
        while (i < length) {
            if (!isSpace(chars[i]) && chars[i] != c) break;
            i++;
        }
        if (i > 0) {
            int j = 0;
            while (i < length) {
                chars[j++] = chars[i++];
            }
            length = j; hash = 0;
        }
        return this;
    }
    /*
     *  文字列右側の空白、コントロールコードを除去
     */
    public CharArray trimR() { 
        return trimR((char)0);
    }
    /*
     *  文字列右側の空白、コントロールコード、指定の１文字を除去
     */
    public CharArray trimR(char c) { 
        while (length > 0) {
            if (!isSpace(chars[length-1]) && chars[length-1] != c) break;
            length--;  hash = 0;
        }
        return this;
    }
    
    /* 
     * スペース文字か？
     */
    static public boolean isSpace(char c) {
        boolean sts = true;
        do {
            if (c <= ' ' || c == '　' || c == '\u205f') break;
            if (c >= '\u2000' && c <= '\u200A') break;
            sts = false;
        } while (false);
        return sts;
    }
    
    
    /*
     *  大文字変換（オリジナルを変換します）
     *  対象は 'a' -'z' のみ。拡張Unicode文字列等には非対応
     */
    public CharArray toUpperCase() {
        for (int i = 0; i < length; i++) {
            if (chars[i] >= 'a' && chars[i] <= 'z') {
                chars[i] -= 0x20;
            }
        }
        hash = 0;
        return this;
    }
    static public CharArray toUpperCase(CharSequence from) {
        return (new CharArray(from).toUpperCase());
    }

    /*
     *  大文字変換と、その後に特定文字のリプレイスを行います
     */
    public CharArray toUpperCase(char from, char to) {
        for (int i = 0; i < length; i++) {
            if (chars[i] >= 'a' && chars[i] <= 'z') {
                chars[i] -= 0x20;
            }
            if (chars[i] == from) chars[i] = to;
        }
        hash = 0;
        return this;
    }
    
    /*
     *  小文字変換（オリジナルを変換します）
     *  対象は 'A' -'Z' のみ。拡張Unicode文字列等には非対応
     */
    public CharArray toLowerCase() {
        for (int i = 0; i < length; i++) {
            if (chars[i] >= 'A' && chars[i] <= 'Z') {
                chars[i] += 0x20;
            }
        }
        hash = 0;
        return this;
    }
    static public CharArray toLowerCase(CharSequence from) {
        return (new CharArray(from).toLowerCase());
    }

    /*
     *  小文字変換と、その後に特定文字のリプレイスを行います
     */
    public CharArray toLowerCase(char from, char to) {
        for (int i = 0; i < length; i++) {
            if (chars[i] >= 'A' && chars[i] <= 'Z') {
                chars[i] += 0x20;
            }
            if (chars[i] == from) chars[i] = to;
        }
        hash = 0;
        return this;
    }
    
    /*
     *  特定の１文字をコンバートします（オリジナルを変換します）
     *  @param from コンバートされる文字
     *  @param to コンバートする文字
     */
    public CharArray replace(char from, char to) {
        replaceCount(from, to);
        return this;
    }

    /*
     *  特定の１文字をコンバートし、個数を返します（オリジナルを変換します）
     *  @param from コンバートされる文字
     *  @param to コンバートする文字
     *  @return 置換された個数
     */
    public int  replaceCount(char from, char to) {
        int count = 0;
        for (int i = 0; i < length; i++) {
            if (chars[i] == from) {
                chars[i] = to;
                ++count;
            }
        }
        hash = 0;
        return count;
    }
    
    /**
     *  部分文字列をコンバートします（オリジナルを変換します）
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     *  @return this
     */
    public CharArray replace(String from, String to) {
        replaceCount(from, to);
        return this;
    }
    /**
     *  部分文字列をコンバートし、個数を返します（オリジナルを変換します）
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     *  @return 置換された個数
     */
    public int replaceCount(String from, String to) {
        int count = 0;
        if (length > 0 && from != null && from.length()> 0) {
            if (to == null) to = "";
            int index = 0;
            int fromsize = from.length();
            int tosize   = to.length();
            do {
                index = indexOf(from,index);
                if (index < 0) break;
                remove(index,fromsize);
                if (index == length) add(to);
                else                 insert(index,to);
                index += tosize;
                ++count;
            } while (index >= 0);
            hash = 0;
        }
        return count;
    }
    
    /**
     *  部分文字列をコンバートします（オリジナルを変換します）
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     *  @return this
     */
    public CharArray replace(CharArray from, CharArray to) {
        replaceCount(from, to);
        return this;
    }
    /**
     *  部分文字列をコンバートし、個数を返します（オリジナルを変換します）
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     *  @return 置換された個数
     */
    public int replaceCount(CharArray from, CharArray to) {
        int count = 0;
        if (length > 0 && from != null && from.length()> 0) {
            if (to == null) to = new CharArray();
            int index = 0;
            int fromsize = from.length();
            int tosize   = to.length();
            do {
                index = indexOf(from,index);
                if (index < 0) break;
                
                remove(index,fromsize);
                if (index == length) add(to); 
                else                 insert(index,to);
                index += tosize;
                ++count;
            } while (index >= 0);
            hash = 0;
        }
        return count;
    }
    /**
     *  部分文字列をコンバートします（オリジナルを変換します）
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     *  @return this
     */
    public CharArray replace(String from, CharArray to) {
        replaceCount(from, to);
        return this;
    }
    /**
     *  部分文字列をコンバートし、個数を返します（オリジナルを変換します）
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     *  @return 置換された個数
     */
    public int replaceCount(String from, CharArray to) {
        int count = 0;
        if (length > 0 && from != null && from.length()> 0) {
            if (to == null) to = new CharArray();
            int index = 0;
            int fromsize = from.length();
            int tosize   = to.length();
            do {
                index = indexOf(from,index);
                if (index < 0) break;
                
                remove(index,fromsize);
                if (index == length) add(to); 
                else                 insert(index,to);
                index += tosize;
                ++count;
            } while (index >= 0);
            hash = 0;
        }
        return count;
    }
    /**
     *  部分文字列をコンバートします（オリジナルを変換します）
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     *  @return this
     */
    public CharArray replace(CharArray from, String to) {
        replaceCount(from, to);
        return this;
    }
    
    /**
     *  部分文字列をコンバートし、個数を返します（オリジナルを変換します）
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     *  @return 置換された個数
     */
    public int replaceCount(CharArray from, String to) {
        int count = 0;
        if (length > 0 && from != null && from.length()> 0) {
            if (to == null) to = "";
            int index = 0;
            int fromsize = from.length();
            int tosize   = to.length();
            do {
                index = indexOf(from,index);
                if (index < 0) break;
                remove(index,fromsize);
                if (index == length) add(to); 
                else                 insert(index,to);
                index += tosize;
                ++count;
            } while (index >= 0);
            hash = 0;
        }
        return count;
    }
    
    /*
        前後にコーテーションがあれば削除します
    */
    public CharArray omitQuote() {
        if (length >= 2) {
            if (chars[0] == '\'' && chars[length-1] == '\'') {
                --length;
                remove(0,1);
            }
            if (chars[0] == '\"' && chars[length-1] == '\"') {
                --length;
                remove(0,1);
            }
        }
        return this;
    }
    /*
        ワード区切りに相当する文字か？
    */
    static public boolean isDelimiter(char c) {
        if (c == '\u0024') return false;    // $ 
        if (c == '\u0040') return false;    // @ 
        if (c == '\u005F') return false;    // _ 
        // ↑ 2012-12-03
        if (c <= '\u002F') return true;
        if ('\u003a' <= c && c <= '\u0040') return true;
        if ('\u005b' <= c && c <= '\u0060') return true;
        if ('\u007b' <= c && c <= '\u00a5') return true;
        return false;
    }
    
    /**
     *  ワード文字列をコンバートします（オリジナルを変換します）
     *  文字列の前後が区切られている必要があります。
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     *  @return 置換された個数
     */
    public int replaceWord(String from, String to) {
        int count = 0;
        if (from != null && to != null) {
            int index = 0;
            int fromsize = from.length();
            int tosize   = to.length();
            do {
                index = indexOf(from,index);
                if (index < 0) break;
                if ((index == 0 || isDelimiter(chars[index-1])) &&
                    (index + fromsize == length || isDelimiter(chars[index+fromsize]))) {
                    remove(index,fromsize);
                    if (index == length) add(to);
                    else                 insert(index,to);
                    index += tosize;
                    ++count;
                } else {
                    index += fromsize + 1;
                }
            } while (index >= 0);
            hash = 0;
        }
        return count;
    }

    /**
     *  ワード文字列をコンバートします（オリジナルを変換します）
     *  文字列の前後が区切られている必要があります。
     *  @param from コンバートされる文字列
     *  @param to コンバートする文字列
     *  @return 置換された個数
     */
    public int replaceWord(CharArray from, CharArray to) {
        int count = 0;
        if (from != null && to != null) {
            int index = 0;
            int fromsize = from.length();
            int tosize   = to.length();
            do {
                index = indexOf(from,index);
                if (index < 0) break;
                if ((index == 0 || isDelimiter(chars[index-1])) &&
                    (index + fromsize == length || isDelimiter(chars[index+fromsize]))) {
                    remove(index,fromsize);
                    if (index == length) add(to);
                    else                 insert(index,to);
                    index += tosize;
                    ++count;
                } else {
                    index += fromsize + 1;
                }
            } while (index >= 0);
            hash = 0;
        }
        return count;
    }
    
    /* タグ用の特殊文字列を変換します<br>
       ex)  &lt; →  &amp;lt;   &amp;  → &amp;amp;
    */
    public CharArray replaceTag() { // 
        // 高速化  
        CharArray ch = CharArray.pop();
        for (int i = 0; i < length; i++) {
            char c = chars[i];
            switch (c) {
                case '<':  ch.add("&lt;");   break;
                case '>':  ch.add("&gt;");   break;
                case '&':  ch.add("&amp;");  break;
                case '\"': ch.add("&quot;"); break;
                case '\'': ch.add("&#039;"); break;
                case '\u00a9'  : ch.add("&copy;");  break; // (C)
                case '\u00ae'  : ch.add("&reg;");   break; // (R)
                case '\u2122'  : ch.add("&trade;");  break; // TM
                case '\u20ac'  : ch.add("&euro;");  break; // ユーロ
                default:   ch.add(c);      break;
            }
        }
        set(ch);
        CharArray.push(ch);
        return this;
    }

    /* タグ用の特殊文字列を変換します<br>
       ex)  &lt; →  &amp;lt;   &amp;  → &amp;amp;<br>
       オリジナルは変更せず、新しいCharArrayで返します
    */

    static public CharArray replaceTag(CharArray org) { // 
        CharArray ch = new CharArray();
        if (org != null && !org.isNull()) {
            for (int i = 0; i < org.length(); i++) {
                char c = org.chars[i];
                switch (c) {
                    case '<':  ch.add("&lt;");   break;
                    case '>':  ch.add("&gt;");   break;
                    case '&':  ch.add("&amp;");  break;
                    case '\"': ch.add("&quot;"); break;
                    case '\'': ch.add("&#039;"); break;
                    case '\u00a9'  : ch.add("&copy;");  break; // (C)
                    case '\u00ae'  : ch.add("&reg;");   break; // (R)
                    case '\u2122'  : ch.add("&trade;");  break; // TM
                    case '\u20ac'  : ch.add("&euro;");  break; // ユーロ
                    default:   ch.add(c);      break;
                }
            }
        } else ch.set((String)null);
        return ch;
    }

/*    
    static public CharArray replaceTag(String str) {
        CharArray ch = new CharArray();
        if (str != null) {
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                switch (c) {
                    case '<':  ch.add("&lt;");   break;
                    case '>':  ch.add("&gt;");   break;
                    case '&':  ch.add("&amp;");  break;
                    case '\"': ch.add("&quot;"); break;
                    case '\'': ch.add("&#039;"); break;
                    case '\u00a9'  : ch.add("&copy;");  break; // (C)
                    case '\u00ae'  : ch.add("&reg;");   break; // (R)
                    case '\u2122'  : ch.add("&trade;");  break; // TM
                    case '\u20ac'  : ch.add("&euro;");  break; // ユーロ
                    default:   ch.add(c);      break;
                }
            }
        } else ch.set((String)null);
        return ch;
    }
*/    
    static public CharArray replaceTag(CharSequence sc) {
        CharArray ch = new CharArray();
        if (sc != null) {
            for (int i = 0; i < sc.length(); i++) {
                char c = sc.charAt(i);
                switch (c) {
                    case '<':  ch.add("&lt;");   break;
                    case '>':  ch.add("&gt;");   break;
                    case '&':  ch.add("&amp;");  break;
                    case '\"': ch.add("&quot;"); break;
                    case '\'': ch.add("&#039;"); break;
                    case '\u00a9'  : ch.add("&copy;");  break; // (C)
                    case '\u00ae'  : ch.add("&reg;");   break; // (R)
                    case '\u2122'  : ch.add("&trade;");  break; // TM
                    case '\u20ac'  : ch.add("&euro;");  break; // ユーロ
                    default:   ch.add(c);      break;
                }
            }
        } else ch.set((String)null);
        return ch;
    }
    
    
/* 利用するときに復活
    static public String replaceTag(char c) {
        switch (c) {
            case '<':  return "&lt;";
            case '>':  return "&gt;";
            case '&':  return "&amp;";
            case '\"': return "&quot;";
            case '\'': return "&#039;";
            case '\u00a9'  : return "&copy;";
            case '\u00ae'  : return "&reg;";
            case '\u2122'  : return "&trade;";
            case '\u20ac'  : return "&euro;";
            default:   return ""+c;
        }
    }
    static public CharArray replaceTag(char c, CharArray buffer) {
        if (buffer == null) buffer = new CharArray();
        switch (c) {
            case '<':  buffer.add("&lt;");   break;
            case '>':  buffer.add("&gt;");   break;
            case '&':  buffer.add("&amp;");  break;
            case '\"': buffer.add("&quot;"); break;
            case '\'': buffer.add("&#039;"); break;
            case '\u00a9'  : buffer.add("&copy;");  break; // (C)
            case '\u00ae'  : buffer.add("&reg;");   break; // (R)
            case '\u2122'  : buffer.add("&trade;");  break; // TM
            case '\u20ac'  : buffer.add("&euro;");  break; // ユーロ
            default:   buffer.add(c);      break;
        }
        return buffer;
    }
*/
    /* タグ用の特殊文字列を再生成します<br>
        オリジナル文字列を変換します
       ex)  &lt; ←  &amp;lt;   &amp;  ← &amp;amp;
    */
    public CharArray replaceOriginalTag() { // 
        replace("&lt;","<");
        replace("&gt;",">");
        replace("&quot;","\"");
        replace("&#039;","\'");
        replace("&amp;","&");
        return this;
    }
    /* タグ用の特殊文字列を再生成します<br>
       ex)  &lt; ←  &amp;lt;   &amp;  ← &amp;amp;<br>
       オリジナルは変更せず、新しいCharArrayで返します
    */
    static public CharArray replaceOriginalTag(CharArray org) { // 
        CharArray ch = new CharArray();
        if (org != null && !org.isNull()) {
            ch.set(org);
            ch.replace("&lt;","<");
            ch.replace("&gt;",">");
            ch.replace("&quot;","\"");
            ch.replace("&#039;","\'");
            ch.replace("&amp;","&");
        } else ch.set((String)null);
        return ch;
    }
    
    static public CharArray replaceOriginalTag(String str) {
        CharArray ch = new CharArray();
        if (str != null) {
            ch.set(str);
            ch.replace("&lt;","<");
            ch.replace("&gt;",">");
            ch.replace("&quot;","\"");
            ch.replace("&#039;","\'");
            ch.replace("&amp;","&");
        } else ch.set((String)null);
        return ch;
    }
    
    /* URL用の特殊文字列を変換します
        <pre>ex)  % -&gt; &amp;25  # -&gt; %23</pre>
    */
    public CharArray replaceURL() {
        //高速化
        CharArray ch = CharArray.pop();
        for (int i = 0; i < length; i++) {
            char c = chars[i];
            switch (c) {
                case '%':  ch.add("%25");   break;
                case ' ':  ch.add("%20");   break;
                case '<':  ch.add("%3C");   break;
                case '>':  ch.add("%3E");   break;
                case '\"': ch.add("%22");   break;
                case '#':  ch.add("%23");   break;
                case '{':  ch.add("%7B");   break;
                case '}':  ch.add("%7D");   break;
                case '|':  ch.add("%7C");   break;
                case '\\': ch.add("%5C");   break;
                case '^':  ch.add("%5E");   break;
                case '~':  ch.add("%7E");   break;
                case '[':  ch.add("%5B");   break;
                case ']':  ch.add("%5D");   break;
                case '\'': ch.add( "%60");   break;
                default:   ch.add(c);      break;
            }
        }
        set(ch);
        CharArray.push(ch);
        
        return this;
    }
    /*
        クォート文字をエスケープします
    */
    static public CharArray escapeQuote(CharArray org) { // 
        CharArray ch = new CharArray();
        if (org != null && !org.isNull()) {
            ch.add(org);
            ch.replace("'", "\\'");
            ch.replace("\"", "\\\"");
        } else ch.set((String)null);
        return ch;
    }

   /*
       converts Cp932 to JIS.<br>
       ￠ ￡ のみ変換。<br>
       AbstractServlet#getParameter()で利用
    */
    public CharArray toJIS2() {
        char c;
        for (int i = 0; i < length; i++) {
            c  = chars[i];
            switch (c) {
                //case 0xff0d:    // －  FULLWIDTH HYPHEN-MINUS ->
                //    c = 0x2212; //     MINUS SIGN
                //    break;
                case 0xffe0:    // ￠  FULLWIDTH CENT SIGN ->
                    c = 0x00a2; //     CENT SIGN
                    break;
                case 0xffe1:    // ￡  FULLWIDTH POUND SIGN ->
                    c = 0x00a3; //     POUND SIGN
                    break;
            }
            chars[i] = c;
        } // next
        return this;
    }
    /*
      converts Cp932 to JIS.<br>
       ＼ ～ ∥－ ￠ ￡ ￢ を変換。<br>
    */
    public CharArray toJIS1() {
        char c;
        for (int i = 0; i < length; i++) {
            c  = chars[i];
            switch (c) {
                //case 0xff3c:    // ＼  FULLWIDTH REVERSE SOLIDUS ->
                //    c = 0x005c; // \   REVERSE SOLIDUS
                //    break;
                case 0xff5e:    // ～  FULLWIDTH TILDE ->
                    c = 0x301c; //     WAVE DASH
                    break;
                case 0x2225:    // ∥  PARALLEL TO ->
                    c = 0x2016; //     DOUBLE VERTICAL LINE
                    break;
                //case 0xff0d:    // －  FULLWIDTH HYPHEN-MINUS ->
                //    c = 0x2212; //     MINUS SIGN
                //    break;
                case 0xffe0:    // ￠  FULLWIDTH CENT SIGN ->
                    c = 0x00a2; //     CENT SIGN
                    break;
                case 0xffe1:    // ￡  FULLWIDTH POUND SIGN ->
                    c = 0x00a3; //     POUND SIGN
                    break;
                case 0xffe2:    // ￢  FULLWIDTH NOT SIGN ->
                    c = 0x00ac; //     NOT SIGN
                    break;
            }
            chars[i] = c;
        } // next
        return this;
    }
    /*
      converts Cp932 to JIS.<br>
       ＼ ～ ∥－ ￠ ￡ ￢ を変換。<br>
    */
    public CharArray toJIS() {
        char c;
        for (int i = 0; i < length; i++) {
            c  = chars[i];
            switch (c) {
                case 0xff3c:    // ＼  FULLWIDTH REVERSE SOLIDUS ->
                    c = 0x005c; // \   REVERSE SOLIDUS
                    break;
                case 0xff5e:    // ～  FULLWIDTH TILDE ->
                    c = 0x301c; //     WAVE DASH
                    break;
                case 0x2225:    // ∥  PARALLEL TO ->
                    c = 0x2016; //     DOUBLE VERTICAL LINE
                    break;
                case 0xff0d:    // －  FULLWIDTH HYPHEN-MINUS ->
                    c = 0x2212; //     MINUS SIGN
                    break;
                case 0x2015:    // ―  HORIZONTAL BAR   -> 
                    c = 0x2014; //     EM DASH
                    break;
                case 0xffe0:    // ￠  FULLWIDTH CENT SIGN ->
                    c = 0x00a2; //     CENT SIGN
                    break;
                case 0xffe1:    // ￡  FULLWIDTH POUND SIGN ->
                    c = 0x00a3; //     POUND SIGN
                    break;
                case 0xffe2:    // ￢  FULLWIDTH NOT SIGN ->
                    c = 0x00ac; //     NOT SIGN
                    break;
            }
            chars[i] = c;
        } // next
        return this;
    }

    /*
       convert JIS to Cp932.<br>
       ＼ ～ ∥－ ￠ ￡ ￢ を変換。<br>
     */
    public CharArray toCp932() {
        char c;
        for (int i = 0; i < length; i++) {
            c  = chars[i];
            switch (c) {
                case 0x005c:    //  \  REVERSE SOLIDUS ->
                    c = 0xff3c; // ＼  FULLWIDTH REVERSE SOLIDUS
                    break;
                case 0x301c:    //     WAVE DASH ->
                    c = 0xff5e; // ～  FULLWIDTH TILDE
                    break;
                case 0x2016:    //     DOUBLE VERTICAL LINE ->
                    c = 0x2225; // ∥  PARALLEL TO
                    break;
                case 0x2212:    //     MINUS SIGN ->
                    c = 0xff0d; // －  FULLWIDTH HYPHEN-MINUS
                    break;
                case 0x2014:    //     EM DASH -> 
                    c = 0x2015; // ―  HORIZONTAL BAR
                    break;
                case 0x00a2:    //     CENT SIGN ->
                    c = 0xffe0; // ￠  FULLWIDTH CENT SIGN
                    break;
                case 0x00a3:    //     POUND SIGN ->
                    c = 0xffe1; // ￡  FULLWIDTH POUND SIGN
                    break;
                case 0x00ac:    //     NOT SIGN ->
                    c = 0xffe2; // ￢  FULLWIDTH NOT SIGN
                    break;
            }
            chars[i] = c;
        }
        return this;
    }
    
    /*
       convert JIS to Cp932(2)<br>
       ￠ ￡ を変換。<br>
     */
    public CharArray toCp9322() {
        char c;
        for (int i = 0; i < length; i++) {
            c  = chars[i];
            switch (c) {
                //case 0x2212:    //     MINUS SIGN ->
                //    c = 0xff0d; // －  FULLWIDTH HYPHEN-MINUS
                //    break;
                case 0x00a2:    //     CENT SIGN ->
                    c = 0xffe0; // ￠  FULLWIDTH CENT SIGN
                    break;
                case 0x00a3:    //     POUND SIGN ->
                    c = 0xffe1; // ￡  FULLWIDTH POUND SIGN
                    break;
            }
            chars[i] = c;
        }
        return this;
    }
    
    /*
       convert JIS to Cp932.<br>
        ～ ∥－  を変換。<br>
     */
    public CharArray toCp9323() {
        char c;
        for (int i = 0; i < length; i++) {
            c  = chars[i];
            switch (c) {
                case 0x301c:    //     WAVE DASH ->
                    c = 0xff5e; // ～  FULLWIDTH TILDE
                    break;
                case 0x2016:    //     DOUBLE VERTICAL LINE ->
                    c = 0x2225; // ∥  PARALLEL TO
                    break;
                case 0x2212:    //     MINUS SIGN ->
                    c = 0xff0d; // －  FULLWIDTH HYPHEN-MINUS
                    break;
            }
            chars[i] = c;
        }
        return this;
    }
    
    /*
        URLに含まれる特殊文字をエンコードする
    */
    public CharArray encodeURLChars() {
        replace("/","@2F.").replace("=","@3D.").replace("?","@3F.").replace("&","@26.").replace("\\","@5C.").replace(" ","@20.");
        return this;
    }
    /*
        URLに含まれる特殊文字をデコードする
    */
    public CharArray decodeURLChars() {
        replace("@2F.","/").replace("@3D.","=").replace("@3F.","?").replace("@26.","&").replace("@5C.","\\").replace("@20."," ");
        return this;
    }
    
    /**
        URLEncoderを使用したエンコード
        @param  encode エンコードタイプ
        @return 変換後文字列
    */
    public String URLEncode(String encode) {
        String str = null;
        try {
            str = java.net.URLEncoder.encode(toString(),encode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return str;
    }
    
    /**
        URLDecoderを使用したデコード
        @param  encode エンコードタイプ
        @return 変換後文字列
    */
    public String URLDecode(String encode) {
        String str = null;
        try {
            str = java.net.URLDecoder.decode(toString(),encode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return str;
    }
    
    /**
      ワードラップを行う 改行は\nとする
      @param offset 開始オフセット
      @param width  表示幅
      @return this
    */
    public CharArray wordWrap(int offset, int width) {
        return wordWrap(offset, width, "\n");
    }
    
    /**
      ワードラップを行う
      @param offset 開始オフセット
      @param width  表示幅
      @param sz     改行時に使用する文字列
      @return this
    */
    public CharArray wordWrap(int offset, int width, String sz) {
        CharArray ch = wordWrap(this, offset,width,sz);
        set(ch);
        return this;
    }
    /**
      ワードラップを行う 改行は\nとする
      @param org    オリジナル文字列
      @param offset 開始オフセット
      @param width  表示幅
      @return       新しいCharArray
    */
    static public CharArray wordWrap(CharArray org, int offset, int width) {
        return wordWrap(org, offset, width, "\n");
    }
    /**
      ワードラップを行う \nは szに置換される
      @param org    オリジナル文字列
      @param offset 開始オフセット
      @param width  表示幅
      @param sz     改行時に使用する文字列
      @return       新しいCharArray
    */
    static public CharArray wordWrap(CharArray org, int offset, int width, String sz) {
        CharArray ch = new CharArray();
        CharToken token = CharToken.pop();
        CharToken token2 = CharToken.pop();
        token.set(org,"\n");
        for (int i = 0; i < token.size(); i++) {
            if (width > 0) {
                token2.set(token.get(i)," ");
                for (int j = 0; j < token2.size(); j++) {
                    CharArray cc = token2.get(j);
                    int length = cc.strlen();
                    if (offset + length > width) {
                        ch.add(sz);
                        ch.add(cc);
                        offset = length % width;
                    } else {
                        ch.add(cc);
                        offset += length;
                    }
                    if (offset >= width) {
                        ch.add(sz);
                        offset = 0;
                    } else {
                        ch.add(" ");
                        offset++;
                    }
                }
            } else {
                ch.add(token.get(i));
            }
            if (i + 1 < token.size()) {
                ch.add(sz);
                offset = 0;
            }
        }
        CharToken.push(token2);
        CharToken.push(token);
        return ch;
    }
    
    /**
        ワード分割をします
        @param str  オリジナル文字列(変更されない）
        @param size 最低分割文字数
        @param sep  挿入する文字列
        @return 新しい文字列
    */
    static public CharArray separateWord(CharSequence str, int size, String sep) {
        CharArray ch = CharArray.pop();
        int n = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);;
            if (('\u0020' <= c && c <= '\u007e') || // 半角英数
                ('\uff61' <= c && c <= '\uff9f')) { // 半角カナ
                ++n;
                if (n > size) {
                    ch.add(sep);
                    n = 1;
                }
            } else {    // ２バイト文字
                n += 2;
                if (n > size) {
                    ch.add(sep);
                    n = 2;
                }
            }
            ch.add(c);
        }
        return ch;
    }
    
    //---------------------------------
    // 検索
    //---------------------------------
    /**
        指定インデックスの１文字を返す。
        インデックスエラーの場合はStringIndexOutOfBoundsException を返す
        @param  index 0-
    */
    public char charAt(int index) {
        if ((index < 0) || (index >= length)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return chars[index];
    }
    
    /** １文字検索 
            @param c 検索する文字
            @return 最初に発見したindex
    */
    public int indexOf(char c) {
        return indexOf(c, 0);
    }
    /** １文字検索 
            @param c 検索する文字
            @param fromIndex 検索開始インデックス
            @return 最初に発見したindex
    */
    public int indexOf(char c, int fromIndex) {
        int sts = -1;
        if (fromIndex >= 0) {
            for (int i = fromIndex; i < length; i++) {
                if (chars[i] == c) {
                    sts = i;
                    break;
                }
            }
        }
        return sts;
    }
 
    /** 文字列検索 
            @param str 検索する文字列
            @return 最初に発見したindex
    */
    public int indexOf(CharSequence str) {
        return indexOf(str,0);
    }
    /** 文字列検索 
            @param str 検索する文字列
            @param fromIndex 検索開始インデックス
            @return 最初に発見したindex
    */
    public int indexOf(CharSequence str, int fromIndex) {
        int sts = -1;
        if (str != null && fromIndex >= 0) {
            int size = str.length();
            for (int i = fromIndex; i < length; i++) {
                if ((i + size) > length()) break;
                sts = i;
                for (int j = 0; j < size; j++) {
                    if (chars[i+j] != str.charAt(j)) {
                        sts = -1;
                        break;
                    } 
                }
                if (sts >= 0) break;    // found
            }
        }
        return sts;
    }
    /** 文字列検索 
            @param str 検索する文字列
            @return 最初に発見したindex
    */
    public int indexOfIgnoreCase(CharSequence str) {
        return indexOfIgnoreCase(str,0);
    }
    /** 文字列検索 
            @param str 検索する文字列
            @param fromIndex 検索開始インデックス
            @return 最初に発見したindex
    */
    public int indexOfIgnoreCase(CharSequence str, int fromIndex) {
        int sts = -1;
        if (str != null && fromIndex >= 0) {
            int size = str.length();
            for (int i = fromIndex; i < length; i++) {
                if ((i + size) > length()) break;
                sts = i;
                for (int j = 0; j < size; j++) {
                    if (Character.toUpperCase(chars[i+j]) != Character.toUpperCase(str.charAt(j))) {
                        sts = -1;
                        break;
                    } 
                }
                if (sts >= 0) break;    // found
            }
        }
        return sts;
    }
    
    /** 配列検索 
        @param strs 検索する配列
        @return 最初に一致した index -1: not found
    */
    public int indexOf(String[] strs) {
        int idx = -1;
        if (strs != null) {
            for (int i = 0; i < strs.length; i++) {
                if (equals(strs[i])) {
                    idx = i;
                    break;
                }
            }
        }
        return idx;
    }
    /** 配列検索 
        @param strs 検索する配列
        @return 最初に一致した index -1: not found
    */
    public int indexOfIgnoreCase(String[] strs) {
        int idx = -1;
        if (strs != null) {
            for (int i = 0; i < strs.length; i++) {
                if (equalsIgnoreCase(strs[i])) {
                    idx = i;
                    break;
                }
            }
        }
        return idx;
    }
    
    //------------------------------------------------
    /** ワード文字列検索  (indexOf のword版です)
            @param str 検索する文字列
            @return 最初に発見したindex
    */
    public int searchWord(CharSequence str) {
        return searchWord(str,0);
    }

    /** ワード文字列検索 (indexOf のword版です)
            @param str 検索する文字列
            @param fromIndex 検索開始インデックス
            @return 最初に発見したindex
    */
    public int searchWord(CharSequence str, int fromIndex) {
        int sts = -1;
        if (str != null && fromIndex >= 0) {
            int size = str.length();
            for (int i = fromIndex; i < length; i++) {
                if ((i + size) > length()) break;
                sts = i;
                for (int j = 0; j < size; j++) {
                    if (chars[i+j] != str.charAt(j)) {
                        sts = -1;
                        break;
                    } 
                }
                if (sts >= 0) {  // found
                   if ((i == 0 || isDelimiter(chars[i-1])) &&
                       (length() == i+size-1 || isDelimiter(chars[i+size]))) {
                       break;  // word
                   }
                }
            }
        }
        return sts;
    }
    //------------------------------------------------
    /** １文字検索 （一番右側から検索する）
            @param c 検索する文字
            @return 最初に発見したindex
    */
    public int lastIndexOf(char c) {
        return lastIndexOf(c, length-1);
    }

    /** １文字検索 （一番右側から検索する）
            @param c 検索する文字
            @param fromIndex 検索開始インデックス
            @return 最初に発見したindex
    */
    public int lastIndexOf(char c, int fromIndex) {
        int sts = -1;
        //for (int i = fromIndex; i < length; i++) {
        for (int i = fromIndex; i >= 0; i--) {
            if (chars[i] == c) {
                sts = i;
                break;
            }
        }
        return sts;
    }

    /** 文字列検索(逆順) 
            @param str 検索する文字列
            @return 最初に発見したindex
    */
    public int lastIndexOf(CharSequence str) {
        return lastIndexOf(str, length-1);
    }
    /** 文字列検索(逆順) ※未実装
            @param str 検索する文字列
            @param fromIndex 検索開始インデックス
            @return 最初に発見したindex
    */
    public int lastIndexOf(CharSequence str, int fromIndex) {
        int sts = -1;
        if (str != null && fromIndex >= 0 && fromIndex < length) {
            int size = str.length();
            if (fromIndex + size > length) fromIndex = length-size;
            for (int i = fromIndex; i >= 0; i--) {
                sts = i;
                for (int j = 0; j < size; j++) {
                    if (chars[i+j] != str.charAt(j)) {
                        sts = -1;
                        break;
                    } 
                }
                if (sts >= 0) break;    // found
            }
        }
        return sts;
    }

    
    /** 文字列検索 
            @param str 検索する文字列
            @return 見つかったか？
    */
    public boolean startsWith(CharSequence str) {
        return startsWith(str,0);
    }
    /** 文字列検索 
            @param str 検索する文字列
            @param fromIndex 検索開始インデックス
            @return 見つかったか？
    */
    public boolean startsWith(CharSequence  str, int fromIndex) {
        if (str == null || (fromIndex + str.length()) > length()) return false;
        for (int i = 0; i < str.length(); i++) {
            if (chars[fromIndex+i] != str.charAt(i)) {
                return false;
            } 
        }
        return true;
    }
    /** 文字列検索 
            @param str 検索する文字列
            @return 見つかったか？
    */
    public boolean startsWithIgnoreCase(CharSequence str) {
        return startsWithIgnoreCase(str,0);
    }
    /** 文字列検索 
            @param str 検索する文字列
            @param fromIndex 検索開始インデックス
            @return 見つかったか？
    */
    public boolean startsWithIgnoreCase(CharSequence  str, int fromIndex) {
        if (str == null || (fromIndex + str.length()) > length()) return false;
        for (int i = 0; i < str.length(); i++) {
            if (Character.toUpperCase(chars[fromIndex+i]) != Character.toUpperCase(str.charAt(i))) {
                return false;
            } 
        }
        return true;
    }
    
    /** 文字列検索 
        @param str 検索する文字列
        @return 見つかったか？
    */
    public boolean endsWith(CharSequence str) {
        if (str == null || str.length() <= 0) return false;
        int index = length - str.length();
        if (index < 0) return false;
        for (int i = 0; i < str.length(); i++) {
            if (chars[index+i] != str.charAt(i)) return false;
        }
        return true;
    }

    /** 文字列検索 
        @param strs 検索する文字列
        @return 見つかったか？
    */
    public boolean endsWith(CharSequence... strs) {
        boolean sts = false;
        for (int i = 0; i < strs.length; i++) {
            sts = endsWith(strs[i]);
            if (sts) break;
        }
        return sts;
    }

    /** 文字列検索 
        @param str 検索する文字列
        @return 見つかったか？
    */
    public boolean endsWithIgnoreCase(CharSequence str) {
        if (str == null || str.length() <= 0) return false;
        int index = length - str.length();
        if (index < 0) return false;
        for (int i = 0; i < str.length(); i++) {
            if (Character.toUpperCase(chars[index+i]) != Character.toUpperCase(str.charAt(i))) return false;
        }
        return true;
    }

    /** 文字列検索 
        @param strs 検索する文字列
        @return 見つかったか？
    */
    public boolean endsWithIgnoreCase(CharSequence... strs) {
        boolean sts = false;
        for (int i = 0; i < strs.length; i++) {
            sts = endsWithIgnoreCase(strs[i]);
            if (sts) break;
        }
        return sts;
    }

    /**
        正規表現マッチング
        @param regex 正規表現文字列
        @return true: マッチする
    */
    public boolean match(String regex) {
        boolean sts = false;
        try {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(this);
            sts = m.find();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sts;
    }

    /**
        ファイル名マッチング
        @param regex ?のみ任意の1文字とする
        @return true: マッチした
    */
    public boolean nameMatch(CharSequence regex) {
        boolean sts = false;
        do {
            if (regex == null) break;
            if (regex.length() != this.length()) break;
            sts = true;
            for (int i = 0; i < regex.length(); i++) {
                char c = regex.charAt(i);
                if (c == '?') continue;
                if (c != chars[i]) {
                    sts = false;
                    break;
                }
            }
        } while (false);
        return sts;
    }

    /**
     * 全ての文字がが指定文字の集合に含まれるか？
     *  @param   set 指定文字の集合
     *  @return  全て指定文字なら true
     */
    public boolean contains(String  set) {
        if (set == null) return false;
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if (set.indexOf(chars[i]) >= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    
    /**
     * 全ての文字が指定文字の集合に含まれるか？
     *  @param   set 指定文字の集合
     *  @return  全て指定文字なら true
     */
    public boolean contains(CharArray set) {
        if (set == null) return false;
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if (set.indexOf(chars[i]) >= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 指定位置以降の文字が指定文字の集合に含まれるか？
     *  @param   set 指定文字の集合
     *  @param   index 検索開始位置
     *  @return  全て指定文字なら true
     */
    public boolean contains(CharArray set, int index) {
        if (set == null) return false;
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = index; i < length; i++) {
            if (set.indexOf(chars[i]) >= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 指定位置以降の文字が指定文字の集合に含まれるか？
     *  @param   set 指定文字の集合
     *  @param   index 検索開始位置
     *  @return  全て指定文字なら true
     */
    public boolean contains(String set, int index) {
        if (set == null) return false;
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = index; i < length; i++) {
            if (set.indexOf(chars[i]) >= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 指定範囲の文字が指定文字の集合に含まれるか？
     *  @param   set 指定文字の集合
     *  @param   index1 検索開始位置
     *  @param   index2 検索終了位置
     *  @return  全て指定文字なら true
     */
    public boolean contains(CharArray set, int index1, int index2) {
        if (set == null) return false;
        if (length == 0) return false;
        boolean rsts = true;
        int size = Math.min(length, index2+1);
        for (int i = index1; i < size; i++) {
            if (set.indexOf(chars[i]) >= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 指定範囲の文字が指定文字の集合に含まれるか？
     *  @param   set 指定文字の集合
     *  @param   index1 検索開始位置
     *  @param   index2 検索終了位置
     *  @return  全て指定文字なら true
     */
    public boolean contains(String set, int index1, int index2) {
        if (set == null) return false;
        if (length == 0) return false;
        boolean rsts = true;
        int size = Math.min(length, index2+1);
        for (int i = index1; i < size; i++) {
            if (set.indexOf(chars[i]) >= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }

    /**
     * 全て半角数字か？
     *  @return  全て半角数字なら true
     */
    public boolean isDigit() {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('0' <= chars[i] && chars[i] <= '9') {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 全て半角数字か？
     *  @param c 有効とする一文字
     *  @return  全て半角数字なら true
     */
    public boolean isDigit(char c) {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('0' <= chars[i] && chars[i] <= '9') {
                //rsts = true;
            } else if (chars[i] == c) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 全て半角数字か？
     *  @param str 有効とする文字集合
     *  @return  全て半角数字なら true
     */
    public boolean isDigit(String str) {
        if (str == null) return false;
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            char c = chars[i];
            if ('0' <= c && c <= '9') {
                //rsts = true;
            } else if (str.indexOf(c) >= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
        指定文字列がすべて半角数字か？
        Stringの場合はCharSequenceでcastすること
        @param str チェックする文字列
        @return 全て半角数字ならtrue
    */
    static public boolean isDigit(CharSequence str) {
        boolean status = false;
        do {
            if (str == null || str.length() == 0) break;
            CharArray ch = CharArray.pop(str);
            status = ch.isDigit();
            CharArray.push(ch);
        } while (false);
        return status;
    }
    
    /**
     * 全て半角英字（記号は除く）か？
     *  @return  全て半角英字なら true
     */
    public boolean isAlpha() {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('A' <= chars[i] && chars[i] <= 'Z') {
            } else if ('a' <= chars[i] && chars[i] <= 'z') {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 全て半角英字(記号は除く）か？
     *  @param c 有効とする一文字
     *  @return  全て半角英字なら true
     */
    public boolean isAlpha(char c) {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('A' <= chars[i] && chars[i] <= 'Z') {
            } else if ('a' <= chars[i] && chars[i] <= 'z') {
                //rsts = true;
            } else if (chars[i] == c) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 全て半角英字か？
     *  @param s 有効とする文字集合
     *  @return  全て半角英字なら true
     */
    public boolean isAlpha(String s) {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('A' <= chars[i] && chars[i] <= 'Z') {
            } else if ('a' <= chars[i] && chars[i] <= 'z') {
                //rsts = true;
            } else if (s.indexOf(chars[i]) >= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    
    /**
     * 全て半角英数字か？
     *  @return  全て半角英数字なら true
     */
    public boolean isAlnum() {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('A' <= chars[i] && chars[i] <= 'Z') {
            } else if ('a' <= chars[i] && chars[i] <= 'z') {
            } else if ('0' <= chars[i] && chars[i] <= '9') {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 全て半角英数字か？
     *  @param c 有効とする1文字
     *  @return  全て半角英数字なら true
     */
    public boolean isAlnum(char c) {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('A' <= chars[i] && chars[i] <= 'Z') {
            } else if ('a' <= chars[i] && chars[i] <= 'z') {
            } else if ('0' <= chars[i] && chars[i] <= '9') {
                //rsts = true;
            } else if (chars[i] == c) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 全て半角英数字か？
     *  @param s 有効とする文字集合
     *  @return  全て半角英数字なら true
     */
    public boolean isAlnum(String s) {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('A' <= chars[i] && chars[i] <= 'Z') {
            } else if ('a' <= chars[i] && chars[i] <= 'z') {
            } else if ('0' <= chars[i] && chars[i] <= '9') {
                //rsts = true;
            } else if (s.indexOf(chars[i])>= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /*
     * 全て英字大文字か？
     */
    public boolean isUpperCase() {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('A' <= chars[i] && chars[i] <= 'Z') {
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /*
     * 全て英字大文字か？
     * @param c 有効とする1文字
     *
     */
    public boolean isUpperCase(char c) {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('A' <= chars[i] && chars[i] <= 'Z') {
            } else if (chars[i] == c) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /*
     * 全て英字大文字か？
     * @param s 有効とする文字集合
     */
    public boolean isUpperCase(String s) {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('A' <= chars[i] && chars[i] <= 'Z') {
            } else if (s.indexOf(chars[i])>= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /*
     * 全て英字小文字か？
     */
    public boolean isLowerCase() {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('a' <= chars[i] && chars[i] <= 'z') {
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /*
     * 全て英字小文字か？
     */
    public boolean isLowerCase(char c) {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('a' <= chars[i] && chars[i] <= 'z') {
            } else if (chars[i] == c) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /*
     * 全て英字小文字か？
     */
    public boolean isLowerCase(String s) {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++) {
            if ('a' <= chars[i] && chars[i] <= 'z') {
            } else if (s.indexOf(chars[i])>= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    
    /*
     * 文字列が全て半角英数で書かれているかチェックします。
     * @return  文字列が半角英数ならばtrue、全角が含まれているならばfalse
     */
    public boolean isAscii() {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++){
            if ('\u0020' <= chars[i] && chars[i] <= '\u007e') { // 半角英数
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /*
     * 文字列が全て半角英数＋半角カナで書かれているかチェックします。
     * @return  文字列が半角ならばtrue、全角が含まれているならばfalse
     */
    public boolean isHankaku() {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++){
            if (('\u0020' <= chars[i] && chars[i] <= '\u007e') || // 半角英数
                ('\uff61' <= chars[i] && chars[i] <= '\uff9f')) { // 半角カナ
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 文字列が全て全角で書かれているかチェックします。
     * @return  文字列が全角ならばtrue、半角が含まれているならばfalse
     */
    public boolean isZenkaku() {
        if (length == 0) return false;
        boolean rsts = true;
        for (int i = 0; i < length; i++){
            if (('\u0020' <= chars[i] && chars[i] <= '\u007e') || // 半角英数
                ('\uff61' <= chars[i] && chars[i] <= '\uff9f')) { // 半角カナ
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 半角文字数を返す
     * @return  文字数
     */
    public int countHankaku() {
        int count = 0;
        for (int i = 0; i < length; i++){
            if (('\u0020' <= chars[i] && chars[i] <= '\u007e') || // 半角英数
                ('\uff61' <= chars[i] && chars[i] <= '\uff9f')) { // 半角カナ
                count++;
            }
        }
        return count;
    }
    /**
     * 半角カナ文字数を返す
     * @return  文字数
     */
    public int countHankakuKana() {
        int count = 0;
        for (int i = 0; i < length; i++){
            if ('\uff61' <= chars[i] && chars[i] <= '\uff9f') { // 半角カナ
                count++;
            }
        }
        return count;
    }
    /**
     * 全角文字数を返す
     * @return  文字数
     */
    public int countZenkaku() {
        int count = 0;
        for (int i = 0; i < length; i++){
            if (('\u0020' <= chars[i] && chars[i] <= '\u007e') || // 半角英数
                ('\uff61' <= chars[i] && chars[i] <= '\uff9f')) { // 半角カナ
            } else {
                count++;
            }
        }
        return count;
    }
    /**
     * 全角+半角カナ文字数を返す
     * @return  文字数
     */
    public int countZenkakuHankana() {
        int count = 0;
        for (int i = 0; i < length; i++){
            if ('\u0020' <= chars[i] && chars[i] <= '\u007e') { // 半角英数
            } else {
                count++;
            }
        }
        return count;
    }
   
    /**
     * 文字列が全て全角カタカナで書かれているかチェックします。<br>
     * 全て全角カタカナならばtrue、それ以外が含まれているならばfalseを返します。
     * 
     * @return  文字列が半角ならばtrue、全角が含まれているならばfalse
     */
    public boolean isKatakana() {
        if (length == 0) return false;
        for (int i = 0; i < length; i++){
            char c = chars[i];
            if('\u30a1' <= c && c <= '\u30fe'){
            } else if (c == ' ') {
            } else if (c == '　') { //全角スペース
            } else if (c == 'ー') { //長音
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 文字列が全て全角ひらがなで書かれているかチェックします。
     * 全て全角ひらがなならばtrue、それ以外が含まれているならばfalseを返します。
     * <p>
     * @return  文字列が半角ならばtrue、全角が含まれているならばfalse
     */
    public boolean isHiragana() {
        if (length == 0) return false;
        for (int i = 0; i < length; i++){
            char c = charAt(i);
            if('\u3041' <= c && c <= '\u309e'){
            } else if (c == ' ') {
            } else if (c == '　') { //全角スペース
            } else if (c == 'ー') { //長音
            } else {
                return false;
            }
        }
        return true;
    }
    
    
    static String spacing = " 　\t\r\n";
    
    /* 全てスペーシング文字か？ */
    public boolean isSpace() {
        return contains(spacing);
    }
    /* 指定位置以降がスペーシング文字か？ */
    public boolean isSpace(int index) {
        return contains(spacing, index);
    }
    /* 指定範囲が全てスペーシング文字か？
    */
    public boolean isSpace(int index1, int index2) {
        return contains(spacing, index1, index2);
    }
    
    /**
     * 半角を1、全角を2とした文字列長を返します
     * @return  文字列長
     */
    public int strlen() {
        int  rsts = 0;
        for (int i = 0; i < length; i++){
            if (('\u0020' <= chars[i] && chars[i] <= '\u007e') || // 半角英数
                ('\uff61' <= chars[i] && chars[i] <= '\uff9f')) { // 半角カナ
                rsts++;
            } else {
                rsts += 2;
            }
        }
        return rsts;
    }
    /**
     * 半角を1、全角を2とした文字列長を返します
     * @param s チェックする文字列
     * @return  文字列長
     */
    static public int strlen(CharSequence s) {
        int  rsts = 0;
        for (int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if (('\u0020' <= c && c <= '\u007e') || // 半角英数
                ('\uff61' <= c && c <= '\uff9f')) { // 半角カナ
                rsts++;
            } else {
                rsts += 2;
            }
        }
        return rsts;
    }
    
    //-----------------------------------
    // コンバート
    //-----------------------------------
    // 全角変換用
    static public String zenkaku2 = 
        "。「」、・ヲァィゥェォャュョッーアイウエオカキクケコサシスセソタチツテトナニヌネノ"+
        "ハヒフヘホマミムメモヤユヨラリルレロワン゛゜";

    /**
        全角数字、記号、丸付き数字、ローマ数字を半角文字に変換します<br>
        半角カタカナを全角にします。(オリジナルを変換します)
        @param tagConvert true:タグ文字をエスケープします
        @return this
    */
    public CharArray convertMachineDependCharacter(boolean tagConvert) {
        CharArray ch = CharArray.convertMachineDependCharacter(this,tagConvert);
        set(ch);
        return this;
    }
    
    /*
        全角数字、記号、丸付き数字、ローマ数字を半角文字に変換します<br>
        半角カタカナを全角にします。（オリジナルを変換します)<br>
        タグ文字をエスケープしません
    */
    public CharArray convertMachineDependCharacter() {
        CharArray ch = CharArray.convertMachineDependCharacter(this,false);
        set(ch);
        return this;
    }
    
    /**
        全角数字、記号、丸付き数字、ローマ数字、を半角文字に変換します<br>
        半角カタカナを全角にします。（オリジナルを変換します)<br>
        @param chStr  オリジナルの文字列
        @param tagConvert true:タグ文字をエスケープします
        @return 新しい文字列を返します
    */
    static public CharArray convertMachineDependCharacter(CharSequence chStr, boolean tagConvert) {
        CharArray ch = new CharArray();
        
        if (chStr != null) {
            for (int i = 0; i < chStr.length(); i++) {
                char c = chStr.charAt(i);
                
                if (tagConvert) {
                    if (c == '＆') {
                        ch.add("&amp;");    continue;
                    } else if (c == '＜') {
                        ch.add("&lt;");     continue;
                    } else if (c == '＞') {
                        ch.add("&gt;");     continue;
                    }
                }
                
                if ('０' <= c && c <= '９') {
                    ch.add((char)('0'+c-'０'));
                } else if ('ａ' <= c && c <= 'ｚ') {
                    ch.add((char)('a'+c-'ａ'));
                } else if ('Ａ' <= c && c <= 'Ｚ') {
                    ch.add((char)('A'+c-'Ａ'));
                } else if ('①' <= c && c <= '⑳') {
                    ch.format((c-'①')+1);
                    ch.add('.');
                } else if ('\u2160' <= c && c <= '\u216b') { //ローマ数字（大）
                    ch.format((c-'\u2160')+1);
                } else if ('\u2170' <= c && c <= '\u2169') { //ローマ数字（小）
                    ch.format((c-'\u2170')+1);
                } else if ('\uff01' <= c && c <= '\uff0f') { // ！ ～ ／ 
                    ch.add((char)(c-'\uff01'+'\u0021'));
                } else if ('\uff1a' <= c && c <= '\uff20') { // ： ～ ＠
                    ch.add((char)(c-'\uff1a'+'\u003a'));
                } else if ('\uff3b' <= c && c <= '\uff40') { // ［ ～ ｛
                    ch.add((char)(c-'\uff3b'+'\u005b'));
                } else if ('\uff5b' <= c && c <= '\uff5d') { // ［ ～ ｛
                    ch.add((char)(c-'\uff5b'+'\u007b'));
                //} else if (c == '\uff5e') {     // ～
                //    ch.add('\u007e');
                //} else if (c == '\u30fc') {     // 長音
                //    ch.add('\uff70');
                } else if ('\uff61' <= c && c <= '\uff9f') { // 半角カナ
                    ch.add(zenkaku2.charAt(c-'\uff61'));
                } else {
                    ch.add(c);
                }
                
            }
        }
        return ch;
    }
    /**
        全角数字、記号、丸付き数字、ローマ数字を半角文字に変換します<br>
        半角カタカナを全角にします。（オリジナルを変換します)<br>
        タグ文字をエスケープしません
        @param chStr  オリジナルの文字列
        @return 新しい文字列を返します
    */
    static public CharArray convertMachineDependCharacter(CharSequence chStr) {
        return CharArray.convertMachineDependCharacter(chStr, false);
    }
    
    /*
        $(aaa.bbb) 形式の文字列に相当するSystem.Property() に<br>
        変換します。（オリジナルを変換します)<br>
    */
    public CharArray convertProperty() {
        CharArray ch = CharArray.convertProperty(this, true);
        set(ch);
        return this;
    }
    /**
        $(aaa.bbb) 形式の文字列に相当するSystem.Property() に<br>
        変換します。（オリジナルを変換します)<br>
        @param mode true 情報がなかった変数を元のままにする false 削除する
        @return this
    */
    public CharArray convertProperty(boolean mode) {
        CharArray ch = CharArray.convertProperty(this, mode);
        set(ch);
        return this;
    }
    
    static String propStart = "$(";
    static String propEnd = ")";

    /*
        $(aaa.bbb) 形式の文字列に相当するSystem.Property() に<br>
        変換します。（新しいCharArrayを返します)
    */
    static public CharArray convertProperty(CharArray chStr) {
        return convertProperty(chStr, true);
    }
    /**
        $(aaa.bbb) 形式の文字列に相当するSystem.Property() に<br>
        変換します。（新しいCharArrayを返します)
        @param chStr 変換元文字列
        @param mode true 情報がなかった変数を元のままにする false 削除する
        @return 新CharArray
    */
    static public CharArray convertProperty(CharArray chStr, boolean mode) {
        if (chStr == null) return null;
        CharArray ch = CharArray.pop();
        
        int index = 0;
        while (index < chStr.length) {
            int idx = chStr.indexOf(propStart, index);
            
            if (idx < 0 || idx >= chStr.length) { // not found
               ch.add(chStr.chars, index, chStr.length-index);
               index = chStr.length;   // 終了にする
               break;
            } else {
                if (idx > index) {
                    ch.add(chStr.chars, index, idx-index);
                }
                int start_index = idx + propStart.length();
                int end_index = chStr.indexOf(propEnd, start_index);  // ENDを探す
                do {               
                    if (end_index < 0 || end_index >= chStr.length 
                       || end_index == start_index) {    // 見つからない
                        ch.add(chStr.chars, idx, chStr.length-idx);
                        index = chStr.length;   // 終了にする
                        break;
                    }
                    // 見つかった
                    String key = new String(chStr.chars, start_index, end_index-start_index);
                    String property = System.getProperty(key);
                    if (property == null) { // property not found
                        if (mode) ch.add(chStr.chars, idx, end_index+propEnd.length()-idx);
                    } else {    // found
                        ch.add(property);
                    }
                    index = end_index + propEnd.length();
                } while (false);
            
            }
        }
        return ch;
    }
    static public CharArray convertProperty(String str) {
        return convertProperty(CharArray.pop(str), true);
    }
    
    /* js用にエスケープする <br>
        オリジナルバッファを変換する。※複数回かけない事！
    */
    public CharArray escape() {
        replace("\\","\\\\");   // \
        replace("\"","\\\"");   // "
        replace("\'","\\\'");   // '
        replace("/","\\/");     // /
        replace("\r","\\r");    // \r
        replace("\n","\\n");    // \n
        return this;
    }
    
    /* エスケープして新しいバッファを返す */
    static public CharArray escape(CharArray org) {
        CharArray ch = CharArray.pop(org);
        return ch.escape();
    }
    static public CharArray escape(String str) {
        return escape(CharArray.pop(str));
    }
    /* json文字列をエスケープする <br>
        新しいバッファを返す
    */
    public CharArray escapeJson() {
        CharArray ch = new CharArray();
        for (int i = 0; i < length; i++) {
            char c = chars[i];
            switch (c) {
                case '\\':  // \
                    ch.add("\\\\");
                    break;
                case '\"':  // "
                    ch.add("\\\"");
                    break;
                case '\'':  // '
                    ch.add("\\\'");
                    break;
                case '/':  // /
                    ch.add("\\/");
                    break;
                case '\b':  // \b
                    ch.add("\\b");
                    break;
                case '\f':  // \f
                    ch.add("\\f");
                    break;
                case '\n':  // \n
                    ch.add("\\n");
                    break;
                case '\r':  // \r
                    ch.add("\\r");
                    break;
                case '\t':  // \t
                    ch.add("\\t");
                    break;
                    
                default:
                    ch.add(c);
            }
        }
        return ch;
    }
    /* json文字列をアンエスケープする <br>
        新しいバッファを返す
    */
    public CharArray unescapeJson() {
        CharArray ch = new CharArray(this);
        ch.replace("\\\\","\\");
        ch.replace("\\\"", "\"");
        ch.replace("\\\'", "\'");
        ch.replace("\\/",  "/");
        ch.replace("\\\b", "\b");
        ch.replace("\\\f", "\f");
        ch.replace("\\\n", "\n");
        ch.replace("\\\r", "\r");
        ch.replace("\\\t", "\t");
        return ch;
    }
    
    // ---------------------------------
    static public CharArray pop() {
        return CharArrayStack.getInstance().pop();
    }
    static public CharArray pop(String str) {
        return CharArrayStack.getInstance().pop(str);
    }
    static public CharArray pop(CharArray ch) {
        return CharArrayStack.getInstance().pop(ch);
    }
    static public CharArray pop(CharSequence ch) {
        return CharArrayStack.getInstance().pop(ch);
    }
    static public CharArray push(CharArray ch) {
        return CharArrayStack.getInstance().push(ch);
    }
    
    //-----------------------------------
    // 描画
    //-----------------------------------
    /* Graphicsに描画する */
    public void drawChars(java.awt.Graphics g, int x, int y) {
        g.drawChars(chars,0,length,x,y);
    }
    public void drawString(java.awt.Graphics g, int x, int y) {  // 互換性のため
        g.drawChars(chars,0,length,x,y);
    }
    
    /* Writer に出力 */
    public void print(java.io.Writer out) {
        if (out != null) {
            try {
                out.write(chars,0,length);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    /* Writer に出力 */
    public void println(java.io.Writer out) {
        if (out != null) {
            try {
                out.write(chars,0,length);
                out.write("\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    //---------------------------------
    // ハッシュ用
    //---------------------------------
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof CharArray) {
            if (length == ((CharArray)obj).length) {
                for (int i = 0; i < length; i++) {
                    if (chars[i] != ((CharArray)obj).chars[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
        if (obj instanceof CharSequence) {
            if (length == ((CharSequence)obj).length()) {
                for (int i = 0; i < length; i++) {
                    if (chars[i] != ((CharSequence)obj).charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    public boolean equalsIgnoreCase(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        CharArray ch2 = new CharArray(this);
        ch2.toUpperCase();
        if (obj instanceof CharArray) {
            CharArray obj2 = new CharArray((CharArray)obj);
            obj2.toUpperCase();
            if (ch2.length == obj2.length) {
                for (int i = 0; i < ch2.length; i++) {
                    if (ch2.chars[i] != obj2.chars[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
        if (obj instanceof String) {
            String obj2 = ((String)obj).toUpperCase();
            if (ch2.length == obj2.length()) {
                for (int i = 0; i < ch2.length; i++) {
                    if (ch2.chars[i] != obj2.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        if (obj instanceof StringBuffer) {
            String obj2 = ((StringBuffer)obj).toString().toUpperCase();
            if (ch2.length == obj2.length()) {
                for (int i = 0; i < length; i++) {
                    if (ch2.chars[i] != obj2.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        if (obj instanceof StringBuilder) {
            String obj2 = ((StringBuilder)obj).toString().toUpperCase();
            if (ch2.length == obj2.length()) {
                for (int i = 0; i < length; i++) {
                    if (ch2.chars[i] != obj2.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    
    public int hashCodeOld() { // jdk1.1 
        int h = 0;
        int off = 0;
        if (length < 256) {
            for (int i = length ; i > 0; i--) {
                h = (h * 37) + chars[off++];
            }
        } else {
            // only sample some characters
            int skip = length / 8;
            for (int i = length ; i > 0; i -= skip, off += skip) {
                h = (h * 39) + chars[off];
            }
        }
        return h;
    }
    
    private int hash = 0;   // ハッシュコードを保管しておく
    
    /**
     * Returns a hashcode for this string. 
     * object is computed as
     * <blockquote><pre> 
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre></blockquote>
     *
     * @return  a hash code value for this object. 
     */
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            int off = 0;        //offset;
            char val[] = chars;
            int len = length;
            for (int i = 0; i < len; i++) {
                h = 31*h + val[i];
            }
            hash = h;
        }
        return h;
    }
    
    //---------------------------------------
    /** 
        辞書式に比較する
        @param obj CharArray/String/StringBuffer
        @return 0:同じ -1:自分が小さい 1:大きい -99:引数エラー
    */
    public int compareTo(Object obj) {
        if ((obj != null)) {
            if (obj instanceof CharArray) {
                int targetLength = ((CharArray)obj).length;
                int checkSize = Math.min(targetLength,length);
                for (int i = 0; i < checkSize; i++) {
                    if (chars[i] < ((CharArray)obj).chars[i]) {
                        return -1;
                    } else if (chars[i] > ((CharArray)obj).chars[i]) {
                        return 1;
                    }
                }
                if (targetLength > length) {
                    return -1;
                }
                if (targetLength < length) {
                    return 1;
                }
                return 0;
            }
            if (obj instanceof CharSequence) {
                int targetLength = ((CharSequence)obj).length();
                int checkSize = Math.min(targetLength,length);
                for (int i = 0; i < checkSize; i++) {
                    if (chars[i] < ((CharSequence)obj).charAt(i)) {
                        return -1;
                    } else if (chars[i] < ((CharSequence)obj).charAt(i)) {
                        return 1;
                    }
                }
                if (targetLength > length) return -1;
                if (targetLength < length) return 1;
                return 0;
            }
        }
        return -99;
    }
    /* charの小文字変換 */
    public char toLowerCase(char c) {
        if (c >= 'A' && c <= 'Z') c  += 0x20;
        return c;
    }
    /* charの大文字変換 */
    public char toUpperCase(char c) {
        if (c >= 'a' && c <= 'z') c  += 0x20;
        return c;
    }
    
    /** 
        辞書式に比較する(英時の大文字小文字は無視）
        @param obj CharArray/String/StringBuffer
        @return 0:同じ -1:自分が小さい 1:大きい -99:引数エラー
    */
    public int compareToIgnoreCase(Object obj) {
        if ((obj != null)) {
            if (obj instanceof CharArray) {
                int targetLength = ((CharArray)obj).length;
                int checkSize = Math.min(targetLength,length);
                for (int i = 0; i < checkSize; i++) {
                    char c = toLowerCase(chars[i]);
                    char c_target = toLowerCase(((CharArray)obj).chars[i]);
                    if (c < c_target) {
                        return -1;
                    } else if (c > c_target) {
                        return 1;
                    }
                }
                if (targetLength > length) {
                    return -1;
                }
                if (targetLength < length) {
                    return 1;
                }
                return 0;
            }
            if (obj instanceof CharSequence) {
                int targetLength = ((CharSequence)obj).length();
                int checkSize = Math.min(targetLength,length);
                for (int i = 0; i < checkSize; i++) {
                    char c = toLowerCase(chars[i]);
                    char c_target = toLowerCase(((CharSequence)obj).charAt(i));
                    if (c < c_target) {
                        return -1;
                    } else if (c > c_target) {
                        return 1;
                    }
                }
                if (targetLength > length) return -1;
                if (targetLength < length) return 1;
                return 0;
            }
        }
        return -99;
    }
    
    /**
        文字列を指定サイズ以下にする(オリジナルを変換する)
        @param size 変更サイズ(4以上を指定)
        @return this
    */
    public CharArray resize(int size) {
        CharArray ch = resize(this, size);
        this.set(ch);
        return this;
    }
    
    /**
        文字列を指定サイズ以下にする
        @param str  文字列
        @param size 変更サイズ(4以上を指定)
        @return 新しい文字列を返す
    */
    static public CharArray resize(String str, int size) {
        return resize(CharArray.pop(str), size);
    }
    /**
        文字列を指定サイズ以下にする
        @param ch  文字列
        @param size 変更サイズ(4以上を指定)
        @return 新しい文字列を返す
    */
    static public CharArray resize(CharArray ch, int size) {
        CharArray ret = new CharArray();
        if (ch == null || size < 4) return ret;
        if (ch.strlen() <= size) return ch;
        for (int i = 0; i < ch.length(); i++) {
            if (ret.strlen() >= (size - 3)) break;
            ret.add(ch.chars[i]);
        }
        int rest = size - ret.strlen();
        for (int i = 0; i < rest; i++) {
            ret.add('.');
        }
        return ret;
    }

    /**
        ファイル名を拡張子を除いて指定サイズ以下にする(オリジナルを変換する)
        @param size 変更サイズ(4以上を指定)
        @return this
    */
    public CharArray resizeFilename(int size) {
        CharArray ch = resizeFilename(this, size);
        this.set(ch);
        return this;
    }
    
    /**
        ファイル名を拡張子を除いて指定サイズ以下にする
        @param str  ファイル名
        @param size 変更サイズ(4以上を指定)
        @return 新しい文字列を返す
    */
    static public CharArray resizeFilename(String str, int size) {
        return resizeFilename(CharArray.pop(str), size);
    }
    /**
        ファイル名を拡張子を除いて指定サイズ以下にする
        @param ch   ファイル名
        @param size 変更サイズ(4以上を指定)
        @return 新しい文字列を返す
    */
    static public CharArray resizeFilename(CharArray ch, int size) {
        CharArray ret = new CharArray();
        if (ch == null) return ret;
        if (ch.strlen() <= size) return ch;
        int index = ch.lastIndexOf(".");
        if (index < 0) return resize(ch, size);
        if (index < 4) return ch;
        
        CharArray ext = CharArray.pop(ch);
        ext.remove(0,index+1);
        CharArray name = CharArray.pop(ch);
        name.length = index;
        size -= ext.strlen();
        
        for (int i = 0; i < name.length(); i++) {
            if (ret.strlen() >= (size - 3)) break;
            ret.add(name.chars[i]);
        }
        int rest = size - ret.strlen();
        for (int i = 0; i < rest; i++) {
            ret.add('.');
        }
        ret.add(ext);
        CharArray.push(name);
        CharArray.push(ext);
        return ret;
    }

    
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            out.writeInt(length);
            for (int i = 0; i < length; i++) {
                out.writeChar(chars[i]);
            }
            out.writeBoolean(append_mode);
            out.writeBoolean(isNull);
            out.writeBoolean(minus_top);
            out.writeInt(type);
            out.writeInt(status);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            length = in.readInt();
            if (chars.length < length) {
                chars = new char[length+ADD_SIZE];
            }
            for (int i = 0; i < length; i++) {
                chars[i] = in.readChar();
            }
            append_mode = in.readBoolean();
            isNull = in.readBoolean();
            minus_top = in.readBoolean();
            type = in.readInt();
            status = in.readInt();
            hash = 0;
        }
    }
    public  void writeObject(ByteArray out) {
        if (out != null) {
            out.setInt(length);
            for (int i = 0; i < length; i++) {
                out.setChar(chars[i]);
            }
            out.setBoolean(append_mode);
            out.setBoolean(isNull);
            out.setBoolean(minus_top);
            out.setInt(type);
            out.setInt(status);
        }
    }

    public  void readObject(ByteArray in) {
        if (in != null) {
            length = in.getInt();
            if (chars.length < length) {
                chars = new char[length+ADD_SIZE];
            }
            for (int i = 0; i < length; i++) {
                chars[i] = in.getChar();
            }
            append_mode = in.getBoolean();
            isNull = in.getBoolean();
            minus_top = in.getBoolean();
            type = in.getInt();
            status = in.getInt();
            hash = 0;
        }
    }

    /*
      バイト配列を返す
      @param encoding 文字列エンコーディング
      @return バイト配列
    */
    public byte[] getBytes(String encoding) throws UnsupportedEncodingException {
        return StringCoding.encode(encoding, chars, 0, length);
    }
    
    /**
        文字列をシフトする
        @param size シフトサイズ（左シフト&gt;0&lt;右シフト） 
        @return this
    */
    public synchronized CharArray shift(int size) {
        if (length > 1) {
            if (size > 0) { // right shift
                size = size % length;
                for (int i = 0; i < size; i++) {
                    char c = chars[length-1];
                    System.arraycopy(chars,0,chars,1,length-1);
                    chars[0] = c;
                }
            } else if (size < 0) {  // left shift
                size = -size;
                size = size % length;
                for (int i = 0; i < size; i++) {
                    char c = chars[0];
                    System.arraycopy(chars,1,chars,0,length-1);
                    chars[length-1] = c;
                }
            }
        }
        return this;
    }
    
    /**
        文字列をシフトする
        @param str 元文字列(変更しない)
        @param size シフトサイズ（左シフト&gt;0&lt;右シフト）
        @return 新しい文字列
    */
    static public  CharArray shift(CharArray str, int size) {
        CharArray ch = new CharArray(str);
        return ch.shift(size);
    }
    /**
        文字列をシフトする
        @param str 元文字列(変更しない)
        @param size シフトサイズ（左シフト&gt;0&lt;右シフト） 
        @return 新しい文字列
    */
    static public  CharArray shift(String str, int size) {
        CharArray ch = new CharArray(str);
        return ch.shift(size);
    }
    
    
}

//
// [end of CharArray.java]
//

