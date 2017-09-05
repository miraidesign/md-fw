//------------------------------------------------------------------------
//    FastOutputStream
//                 高速 DataOutputStream
//                  Copyright (c) MIraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//             ObjectOutputStream と同様のバッファリングにより高速化
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.io.OutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *  高速 DataOutputStream<br>
 *  ObjectOutputStream と同様のバッファリングにより高速化
 *  
 * @version 1.0 98-02-24
 * @author Toru Ishioka
 * @since  JDK1.0
 */

public class FastOutputStream extends OutputStream implements DataOutput 
{ 
    private byte[] buf;
    private int count;
    private OutputStream out;
    
    //------------------
    // constructor
    //------------------
    /** デフォルトのバッファリングサイズ(2048)で FastOutputStream を生成する
        @param out  出力先ストリーム
    **/
    public FastOutputStream(OutputStream out) throws IOException {
        this(out,2048);
    }
    /** 指定バッファリングサイズで FastOutputStream を生成する
        @param out  出力先ストリーム
        @param size バッファリングサイズ
    **/
    public FastOutputStream(OutputStream out,int size) throws IOException {
        this.out = out;
        dos = new DataOutputStream(this);
        buf = new byte[size];
    }
    
    /** 
    */
    public void write(int data) throws IOException {
        if (count >= buf.length) drain();
        buf[count++] = (byte)data;
    }
    /** 
    */
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }
    /** 
    */
    public void write(byte b[], int off, int len) throws IOException {
        if (len < 0) throw new IndexOutOfBoundsException();
        int avail = buf.length - count;
        if (len <= avail) {
            System.arraycopy(b, off, buf, count, len);
            count += len;
        } else {
            drain();
            out.write(b, off, len);
        }
    }

    /** 
    */
    public void flush() throws IOException {
        drain();
        out.flush();
    }

    /** 
    */
    protected void drain() throws IOException {
        if (count == 0) return;
        out.write(buf, 0, count);
        count = 0;
    }

    /** 
    */
    public void close() throws IOException {
        flush();
        out.close();
    }

    /** 
    */
    public void writeBoolean(boolean data) throws IOException {
        if (count >= buf.length) drain();
        buf[count++] = (byte)(data ? 1 : 0);
    }

    /** 
    */
    public void writeByte(int data) throws IOException  {
        if (count >= buf.length) drain();
        buf[count++] = (byte)data;
    }

    /** 
    */
    public void writeShort(int data)  throws IOException {
        if (count + 2 > buf.length) drain();
        buf[count++] = (byte)((data >>>  8));
        buf[count++] = (byte)((data >>>  0));
    }

    /** 
    */
    public void writeChar(int data)  throws IOException {
        if (count + 2 > buf.length) drain();
        buf[count++] = (byte)((data >>>  8));
        buf[count++] = (byte)((data >>>  0));
    }

    /** 
    */
    public void writeInt(int data)  throws IOException {
        if (count + 4 > buf.length) drain();
        buf[count++] = (byte)((data >>> 24));
        buf[count++] = (byte)((data >>> 16));
        buf[count++] = (byte)((data >>>  8));
        buf[count++] = (byte)((data >>>  0));
    }

    /** 
    */
    public void writeLong(long data)  throws IOException {
        if (count + 8 > buf.length) drain();
        buf[count++] = (byte)((int)(data >>> 56));
        buf[count++] = (byte)((int)(data >>> 48));
        buf[count++] = (byte)((int)(data >>> 40));
        buf[count++] = (byte)((int)(data >>> 32));
        buf[count++] = (byte)((data >>> 24));
        buf[count++] = (byte)((data >>> 16));
        buf[count++] = (byte)((data >>>  8));
        buf[count++] = (byte)((data >>>  0));
    }

    /** 
    */
    public void writeFloat(float data) throws IOException {
        int value = Float.floatToIntBits(data);
        if (count + 4 > buf.length) drain();
        buf[count++] = (byte)((value >>> 24));
        buf[count++] = (byte)((value >>> 16));
        buf[count++] = (byte)((value >>>  8));
        buf[count++] = (byte)((value >>>  0));
    }

    /** 
    */
    public void writeDouble(double data) throws IOException {
        long value = Double.doubleToLongBits(data);
        if (count + 8 > buf.length) drain();
        buf[count++] = (byte)((int)(value >>> 56));
        buf[count++] = (byte)((int)(value >>> 48));
        buf[count++] = (byte)((int)(value >>> 40));
        buf[count++] = (byte)((int)(value >>> 32));
        buf[count++] = (byte)((value >>> 24));
        buf[count++] = (byte)((value >>> 16));
        buf[count++] = (byte)((value >>>  8));
        buf[count++] = (byte)((value >>>  0));
    }

    private DataOutputStream dos;
    /** 
    */
    public void writeBytes(String data) throws IOException {
        dos.writeBytes(data);
    }

    /** 
    */
    public void writeChars(String data) throws IOException {
        dos.writeChars(data);
    }

    /** 
    */
    public void writeUTF(String data) throws IOException {
        dos.writeUTF(data);
    }
}

//
//
// [end of FastOutputStream.java]
//

