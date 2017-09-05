//------------------------------------------------------------------------
//    ByteArray.java
//              byte[] ラッパー
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-05   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
// 
//------------------------------------------------------------------------
package com.miraidesign.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * byte[] ラッパー<br>
 * 高速データ処理目的で使用(GCも極力発生しない)<br>
 * ※スレッドセーフではないのに注意！<br>
 *  append_modeの時の引数なし getXXX() 関数では、<br>
 *  読み込み位置をシフトしながら行う
 *  
 *  @version 1.1 1999-10-25
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ByteArray {
    public byte[] bytes;
    
    /** byte[] を返す 
        return バイト配列
    */
    public byte[] getBytes() { return bytes;}
    
    /**
      新しいbyte[]を返す
    */
    public byte[] getByteArray() {
        byte[] array = new byte[length];
        System.arraycopy(bytes,0, array,0,length);
        return array;
    }
    
    /** データサイズを返す */
    public int sizeOf() {
        return bytes.length;
    }
    
    public int length = 0;
    
    private int index = 0;  // 連続値を抜き出す時の位置

    private static int ADD_SIZE = 64;
    private static boolean DOUBLE_SIZE = true;

    private boolean append_mode = true;      // データセット、取得時有効
    
    /** 連続取得モード設定 
        @param mode 連続取得モード
    */
    public ByteArray setAppendMode(boolean mode) { 
        append_mode = mode;
        return this;
    }
    
    /** コピー 
        @param from コピー元のByteArray
    */
    public ByteArray copy(ByteArray from) {
        set(from);
        append_mode = from.append_mode;
        //hash = from.hash;
        return this;
    }
    
    //-------------------------------------------------
    // コンストラクタ
    //-------------------------------------------------
    /** default constructor */
    public ByteArray() { this(null,4,true); }

    /**
        copy constructor
        @param by   コピー元データ
    */
    public ByteArray(ByteArray by) { set(by.bytes,0,by.length);}

    /** 
        coustructor
        @param by   セットするデータ
        @param size 初期バッファサイズのヒント
        @param mode 追加モード
    */
    public ByteArray(byte[] by,int size,boolean mode) {
        size = Math.abs(size);
        bytes = new byte[DOUBLE_SIZE ? (size+1) * 2 :size+ADD_SIZE];
        if (by != null) {
            length = Math.min(size,by.length);
            System.arraycopy(by,0,bytes,0,length);
        }
        setAppendMode(mode);
    }

    /** 
        coustructor
        @param by   セットするデータ
        @param size 初期バッファサイズのヒント
    */
    public ByteArray(byte[] by,int size) { this(by,size,true);}
    /** 
        coustructor
        @param by   セットするデータ
    */
    public ByteArray(byte[] by) { this(by,0,true); }
    /** 
        coustructor
        @param size 初期バッファサイズのヒント
    */
    public ByteArray(int size) { this(null,size,true); }
    /** 
        coustructor
        @param size 初期バッファサイズのヒント
        @param mode 追加モード
    */
    public ByteArray(int size,boolean mode) { this(null,size,mode); }
    //--------------------------------------
    /**
        バイト長を返す
        @return length
    */
    public int length() { return length;}
    /** 
        データをリセットする<br>
        length変更のみで内部データは変更しない。（高速)
    */
    public ByteArray reset() { length = 0; return this;}
    
    /**
        データをリセットする。
        配列データも０クリアする（遅い)
    */
    public ByteArray clear() {
        reset();
        for (int i = 0; i < bytes.length; i++) bytes[i] = 0;
        return this;
    }
    /** 
        連続取得インデックスをセットする
        @param i セットするindex値
    */
    public void resetIndex(int i) { index = i;}
    
    /** 連続取得インデックスをリセットする */
    public void resetIndex() { index = 0;}
    
    /** 
        連続取得時のindexを取得する
        @return index
    */
    public int getIndex() { return index;}

    /** データ設定
        @param by 設定データ
        @return 自分自身
    */
    public ByteArray set(ByteArray by) {
        return set(by.bytes,0,by.length);
    }
    /** データ設定
        @param by 設定データ
        @return 自分自身
    */
    public ByteArray set(byte[] by) {
        return set(by,0,by.length);
    }
    /** データ設定
        @param by 設定データ
        @param offset コピー開始位置
        @param size   コピーサイズ
        @return 自分自身
    */
    public ByteArray set(byte[] by,int offset,int size) {
        if (bytes.length < size) {
            bytes = new byte[size+2];
        }
        System.arraycopy(by,offset,bytes,0,size);
        length = size;
        return this;
    }
    
    /** 1バイト設定する
        @param b 設定データ
        @return 自分自身
    */
    public ByteArray set(byte b) {
        if (bytes.length < 1) {
            bytes = new byte[4];
        }
        bytes[0] = b;
        length = 1;
        return this;
    }
    
    /** データ追加
        @param by 追加データ
        @return 自分自身
     */
    public ByteArray add(ByteArray by) {
        return add(by.bytes,0,by.length);
    }
    /** データ追加 
        @param by 追加データ
        @return 自分自身
    */
    public ByteArray add(byte[] by) {
        return add(by,0,by.length);
    }
    /** データ追加 
        @param by 追加データ
        @param offset コピー開始位置
        @param size   コピーサイズ
        @return 自分自身
    */
    public ByteArray add(byte[] by,int offset,int size) {
        if (bytes.length < length+size) {
            byte[] new_bytes = new byte[DOUBLE_SIZE ? (length+size+1)*2 : length+size+ADD_SIZE];
            System.arraycopy(bytes,0,new_bytes,0,length);
            bytes = new_bytes;
        }
        System.arraycopy(by,offset,bytes,length,size);
        length += size;
        return this;
    }
    /** 1バイト追加 
        @param b 追加バイト
        @return 自分自身
    */
    public ByteArray add(byte b) {
        if (bytes.length < length+1) {
            byte[] new_bytes = new byte[DOUBLE_SIZE ? (length+1)*2 :length+1+ADD_SIZE];
            System.arraycopy(bytes,0,new_bytes,0,length);
            bytes = new_bytes;
        }
        bytes[length++] = b;
        return this;
    }
    
    private static byte[] byBuf = new byte[200];    // 一時バッファ
    
    /** データ挿入 
        @param index 指定位置
        @param by 挿入データ
        @return 自分自身
    */
    public ByteArray insert(int index,ByteArray by) {
        return insert(index,by.bytes,0,by.length);
    }
    /** 
        データ挿入 
        @param index 指定位置
        @param by 挿入データ
        @return 自分自身
    */
    public ByteArray insert(int index,byte[] by) {
        return insert(index,by,0,by.length);
    }
    /** 
        データ挿入 
        @param index 指定位置
        @param by 挿入データ
        @param offset コピー開始位置
        @param size   コピーサイズ
        @return 自分自身
    */
    public ByteArray insert(int index,byte[] by,int offset,int size) {
        if (bytes.length < length+size) {
            byte[] new_bytes = new byte[DOUBLE_SIZE ? (length+size+1)*2 : length+size+ADD_SIZE];
            System.arraycopy(bytes,0,new_bytes,0,length);
            bytes = new_bytes;
        }
        if (index <= length) {
            synchronized (byBuf) {
                System.arraycopy(bytes,0,byBuf,0,index);
                System.arraycopy(by,offset,byBuf,index,size);
                System.arraycopy(bytes,index,byBuf,index+size,length-index);
                System.arraycopy(byBuf,0,bytes,0,length+size);
            }
            length += size;
        }
        return this;
    }
    /** 
        1バイト挿入 
        @param index 指定位置
        @param b 挿入データ
        @return 自分自身
    */
    public ByteArray insert(int index,byte b) {
        if (bytes.length < length+1) {
            byte[] new_bytes = new byte[DOUBLE_SIZE ? (length+1)*2 :length+1+ADD_SIZE];
            System.arraycopy(bytes,0,new_bytes,0,length);
            bytes = new_bytes;
        }
        if (index <= length) {
            synchronized (byBuf) {
                System.arraycopy(bytes,0,byBuf,0,index);
                byBuf[index] = b;
                System.arraycopy(bytes,index,byBuf,index+1,length-index);
                System.arraycopy(byBuf,0,bytes,0,length+1);
            }
            ++length;
        }
        return this;
    }

    //---------------------------------------------------------
    // データ設定
    //---------------------------------------------------------
    /**
     *  現在位置に、boolean値を設定する
     *  @param b 設定値
     */
     public ByteArray setBoolean(boolean b) { return setBoolean(append_mode ? length : 0,b); }
    /**
     *  指定位置に、boolean値を設定する
     *  @param offset 指定位置
     *  @param b 設定値
     *  @return 自分自身
     */
     public ByteArray setBoolean(int offset,boolean b) {
        if (bytes.length < offset+1) {
            byte[] new_bytes = new byte[DOUBLE_SIZE ? (length+1)*2 :length+ADD_SIZE];
            System.arraycopy(bytes,0,new_bytes,0,length);
            bytes = new_bytes;
        }
        bytes[offset++] = (byte)(b ? 1 : 0);
        if (offset > length) length = offset;
        return this;
     }
    /**
     *  現在位置に、byte値を設定する
     *  @param i 設定値
     *  @return 自分自身
     */
     public ByteArray setByte(int i) { return setByte(append_mode ? length : 0,i); }
    /**
     *  指定位置に、byte値を設定する
     *  @param offset 指定位置
     *  @param i 設定値
     *  @return 自分自身
     */
     public ByteArray setByte(int offset,int i) {
        if (bytes.length < offset+1) {
            byte[] new_bytes = new byte[DOUBLE_SIZE ? (length+1) * 2 : length+ADD_SIZE];
            System.arraycopy(bytes,0,new_bytes,0,length);
            bytes = new_bytes;
        }
        bytes[offset++] = (byte)i;
        if (offset > length) length = offset;
        return this;
     }
    /**
     *  現在位置に、byte値を設定する
     *  @param i 設定値
     *  @return 自分自身
     */
     public ByteArray setShort(int i) { return setShort(append_mode ? length : 0,i); }
    /**
     *  指定位置に、short値を設定する
     *  @param offset 指定位置
     *  @param i 設定値
     *  @return 自分自身
     */
     public ByteArray setShort(int offset,int i) {
        if (bytes.length < offset+2) {
            byte[] new_bytes = new byte[DOUBLE_SIZE ? (length+2)*2 :length+2+ADD_SIZE];
            System.arraycopy(bytes,0,new_bytes,0,length);
            bytes = new_bytes;
        }
        bytes[offset++] = (byte)((i >>>  8));
        bytes[offset++] = (byte)((i >>>  0));
        if (offset > length) length = offset;
        return this;
     }
    /**
     *  現在位置に、byte値を設定する
     *  @param i 設定値
     *  @return 自分自身
     */
     public ByteArray setChar(int i) { return setChar(append_mode ? length : 0,i); }
    /**
     *  指定位置に、char値を設定する
     *  @param offset 指定位置
     *  @param i 設定値
     *  @return 自分自身
     */
     public ByteArray setChar(int offset,int i) {
        if (bytes.length < offset+2) {
//System.out.println("array copy");
            byte[] new_bytes = new byte[DOUBLE_SIZE ? (length+2)*2 :length+2+ADD_SIZE];
            System.arraycopy(bytes,0,new_bytes,0,length);
            bytes = new_bytes;
        }
        bytes[offset++] = (byte)((i >>>  8));
        bytes[offset++] = (byte)((i >>>  0));
        if (offset > length) length = offset;
        return this;
     }
    /**
     *  現在位置に、int値を設定する
     *  @param i 設定値
     *  @return 自分自身
     */
     public ByteArray setInt(int i) { return setInt(append_mode ? length : 0,i); }
    /**
     *  指定位置に、byte値を設定する
     *  @param offset 指定位置
     *  @param i 設定値
     *  @return 自分自身
     */
     public ByteArray setInt(int offset,int i) {
        if (bytes.length < offset+4) {
System.out.println("(int) array copy length:"+length+" offset:"+offset);
            byte[] new_bytes = new byte[DOUBLE_SIZE ? (length+4)*2 :length+4+ADD_SIZE];
            System.arraycopy(bytes,0,new_bytes,0,length);
            bytes = new_bytes;
        }
        bytes[offset++] = (byte)((i >>> 24));
        bytes[offset++] = (byte)((i >>> 16));
        bytes[offset++] = (byte)((i >>>  8));
        bytes[offset++] = (byte)((i >>>  0));
        if (offset > length) length = offset;
        return this;
     }
    /**
     *  現在位置に、long値を設定する
     *  @param l 設定値
     *  @return 自分自身
     */
     public ByteArray setLong(long l) { return setLong(append_mode ? length : 0,l); }
    /**
     *  指定位置に、long値を設定する
     *  @param offset 指定位置
     *  @param l 設定値
     *  @return 自分自身
     */
     public ByteArray setLong(int offset,long l) {
        if (bytes.length < offset+8) {
            byte[] new_bytes = new byte[DOUBLE_SIZE ? (length+8)*2 :length+8+ADD_SIZE];
            System.arraycopy(bytes,0,new_bytes,0,length);
            bytes = new_bytes;
        }
        bytes[offset++] = (byte)((int)(l >>> 56));
        bytes[offset++] = (byte)((int)(l >>> 48));
        bytes[offset++] = (byte)((int)(l >>> 40));
        bytes[offset++] = (byte)((int)(l >>> 32));
        bytes[offset++] = (byte)((l >>> 24));
        bytes[offset++] = (byte)((l >>> 16));
        bytes[offset++] = (byte)((l >>>  8));
        bytes[offset++] = (byte)((l >>>  0));
        if (offset > length) length = offset;
        return this;
     }
     
    /**
     *  現在位置に、double値を設定する
     *  @param d 設定値
     *  @return 自分自身
     */
     public ByteArray setDouble(double d) { return setDouble(append_mode ? length : 0,d); }
    /**
     *  指定位置に、double値を設定する
     *  @param offset 指定位置
     *  @param d 設定値
     *  @return 自分自身
     */
     public ByteArray setDouble(int offset,double d) {
        return setLong(offset,Double.doubleToLongBits(d));
     }
     //----------------------------------------------------------------
     // データ取得
     //----------------------------------------------------------------
    /**
     *  bool値を取得する
     *  @return bool値
     */
    public boolean  getBoolean()  { 
        boolean b =  getBoolean(append_mode ? index : 0); 
        if (append_mode) index++;
        return b;
    }
    /**
        指定位置のbool値を取得する
        @param offset 指定位置
        @return bool値
    */
    public boolean  getBoolean(int offset) { 
        boolean b = false;
        if (0 <= offset && offset <= length-1) {
           b = (bytes[offset] != 0);
        }
        return b;
    }
    /**
     *  byte値を取得する
     *  @return byte値
     */
    public byte  getByte()  { 
        byte b =  getByte(append_mode ? index : 0); 
        if (append_mode) index++;
        return b;
    }
    /**
     *  指定位置のbyte値を取得する
     *  @param offset 指定位置
     *  @return byte値
     */
    public byte  getByte(int offset) { 
        byte b = 0;
        if (0 <= offset && offset <= length-1) {
           b = bytes[offset];   //&0xff;
        }
        return b;
    }
    /**
       現在位置のchar値を取りだす
       @return char
    */
    public char  getChar()  { 
        char c =  getChar(append_mode ? index : 0); 
        if (append_mode) index += 2;
        return c;
    }
    /**
       指定位置のchar値を取りだす
       @param offset 指定位置
       @return char
    */
    public char  getChar(int offset) { 
        char c = 0;
        if (0 <= offset && offset <= length-2) {
           c = (char)(((bytes[offset] & 0xff)<<8) +(bytes[offset+1] &0xff));
        }
        return c;
    }
    /**
        現在位置からint値を取り出す
        @return int値
    */
    public int  getInt()  { 
        int i =  getInt(append_mode ? index : 0); 
        if (append_mode) index += 4;
        return i;
    }
    /**
        指定位置からint値を取り出す
        @param offset 指定位置
        @return int値 offsetエラーは 0を返す
    */
    public int  getInt(int offset) { 
        int i = 0;
        if (0 <= offset && offset <= length-4) {
            i =  ((bytes[offset]& 0xff)<<24) +((bytes[offset+1]&0xff)<<16)
                +((bytes[offset+2] & 0xff)<<8 ) +(bytes[offset+3] &0xff);
        }
        return i;
    }
    /**
        現在位置からlong値を取り出す
        @return long値
    */
    public long getLong() { 
        long l = getLong(append_mode ? index : 0);
        if (append_mode) index += 8;
        return l;
    }
    /**
        指定位置からlong値を取り出す
        @param offset 指定位置
        @return long値 offsetエラーは 0を返す
    */
    public long getLong(int offset) {
        long l = 0;
        if (0 <= offset && offset <= length-8) {
            l = ((0xffl &bytes[offset])<<56) +((0xffl &bytes[offset+1])<<48)
               +((0xffl &bytes[offset+2])<<40) +((0xffl &bytes[offset+3])<<32)
               +((0xffl &bytes[offset+4])<<24) +((0xffl &bytes[offset+5])<<16)
               +((0xffl &bytes[offset+6])<<8 ) + (0xffl & bytes[offset+7]);
        }
        return l;
    }
    
    /**
        現在位置からdouble値を取り出す
        @return double値
    */
    public double getDouble() { 
        double d = getDouble(append_mode ? index : 0);
        if (append_mode) index += 8;
        return d;
    }
    /**
        指定位置からdouble値を取り出す
        @param offset 指定位置
        @return double値 offsetエラーは 0を返す
    */
    public double getDouble(int offset) {
        return  Double.longBitsToDouble(getLong(offset));
    }
    
    /** データ両端の空白、コントロールコードを除去 
        @return 自分自身を変える
    */
    public ByteArray trim() { // 
        trimR();
        trimL();
        return this;
    }

    /** データ左側の空白、コントロールコードを除去 
        @return 自分自身を変える
    */
    public ByteArray trimL() {
        int i = 0;
        while (i < length) {
            if (bytes[i] > (byte)' ') break;
            i++;
        }
        if (i > 0) {
            int j = 0;
            while (i < length) {
                bytes[j++] = bytes[i++];
            }
            length = j;
        }
        return this;
    }

    /** データ右側の空白、コントロールコードを除去 
        @return 自分自身を変える
    */
    public ByteArray trimR() {
        while (length > 0) {
            if (bytes[length-1] > (byte)' ') break;
            length--;
        }
        return this;
    }

    /** データ(String)変換<br>
            ※遅くなるのであまり使用しないこと
     */
    public String toString() {
        CharArray ch = new CharArray();
        ch.add('(');
        ch.format(length,10,3,' ');
        ch.add(')');
        for (int i = 0; i < length; i++) {
            int val = bytes[i] & 0xff;
            ch.add(':');
            ch.format(val,16,2,'0');
        }
        return ch.toString();
    }

    //---------------------------------
    // ハッシュ用
    //---------------------------------
    /** Object比較
        @return 内部値が全て同一の時にtrueを返す
    */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof ByteArray)) {
            if (length == ((ByteArray)obj).length) {
                for (int i = 0; i < length; i++) {
                    if (bytes[i] != ((ByteArray)obj).bytes[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    private int hash = 0;   // ハッシュコードを保管しておく
    
    /** ハッシュコード関数 
        @return ハッシュコード
    */
    public int hashCode() { 
        int h = hash;
        if (h == 0) {
            int off = 0;
            byte val[] = bytes;
            int len = length;

            for (int i = 0; i < len; i++) {
                h = 31*h + val[off++];
            }
            hash = h;
        }
        return h;
    }
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            out.writeInt(length);
            out.write(bytes,0,length);
            out.writeBoolean(append_mode);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            length = in.readInt();
            if (bytes.length < length) {
                bytes = new byte[length+2];
            }
            in.readFully(bytes,0,length);
            append_mode = in.readBoolean();
            hash = 0;
        }
    }


}

//
// [end of ByteArray.java]
//

