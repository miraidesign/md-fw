//------------------------------------------------------------------------
// @(#)Queue.java
//                 キュー（リングバッファ使用）(abstract) 
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-04   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** キュー（リングバッファ使用）<br>
<pre>
    データ追加時配列を超える場合のモード<br>
      0:EXTEND_DATA    配列を拡張する(デフォルト)<br>
      1:DELETE_TOP     先頭データを捨てる<br>
      2:NOT_APPEND     追加データを捨てる<br>
      3:WAIT_DATA      追加できるまで待つ<br>
</pre>
*/
public  abstract class Queue {
    protected final static int DEFAULT_SIZE = 32;
    
    static public final int MIN    = 1;
    
    static public final int INT    = 1;
    static public final int LONG   = 2;
    static public final int BOOL   = 3;
    static public final int DOUBLE = 4;
    static public final int STRING = 5;
    static public final int OBJECT = 6;
    static public final int BYTES  = 7;
    static public final int DATE   = 8;
    
    static public final int MAX    = 8;
    
    // これも認める
    static public final int BOOLEAN = 3;
    static public final int CHAR   = 5;
    static public final int CHARARRAY = 5;
    
    static protected final String[] szTypes = {" ","int","Long","bool","double","string","Object","Bytes","Date"};
    static public String getTypeName(int type) {return szTypes[type];}
    /**
        指定タイプのQueueオブジェクトを生成する
        @param type INT/LONG/BOOL/DOUBLE/STRING/OBJECT/DATE
        @return Queue
    */
    static synchronized Queue create(int type) {
        Queue queue = null;
        switch (type) {
            case INT:    queue = new IntQueue(); break;
            case LONG:   queue = new LongQueue(); break;
            case BOOL:   queue = new BooleanQueue(); break;
            case DOUBLE: queue = new DoubleQueue(); break;
            case STRING: queue = new CharArrayQueue(); break;
            case OBJECT: queue = new ObjectQueue(); break;
            case BYTES:  queue = new ByteArrayQueue(); break;
            case DATE:   queue = new LongQueue(); break;
            default: break;
        }
        return queue;
    }
    
    static synchronized Queue create(Queue from) {
        Queue queue = create(from.type);
        queue.copy(from);
        return queue;
    }
    
    //public abstract void copy(Queue from);
    public abstract Queue copy(Queue from);
    
    protected int type;
    protected int len;          // 含まれる要素数
    protected int head;         // 先頭インデックス
    protected int tail;         // 末端インデックス
    
    /** 追加データ配列オーバー時の処理モード */
    protected int appendMode;   // 
    /** 0: 配列を拡張する **/
    public final static int EXTEND_DATA = 0;
    /** 1: 先頭データを捨てる **/
    public final static int DELETE_TOP  = 1;
    /** 2: 追加データを捨てる **/
    public final static int NOT_APPEND  = 2;
    /** 3: 追加できるまで待つ **/
    public final static int WAIT_DATA   = 3;

    /** 取り出し時，なければ待つ */
    protected boolean waitMode = false ;
    
    /** EXTEND_DATA 時の最大サイズ デフォルト:-1(サイズチェックなし)*/
    protected int max_size =  -1;   // EXTEND_DATA 時のみ有効
    
    /** DELETE_TOP時に廃棄されてしまった個数 */
    protected int over_size = 0;    // DELETE_TOP  時のみ有効

    // 現在 ParametersQueue のみで使用
    public static final int LOOP    = 0;    // ブロックをループして使用
    public static final int DEQUEUE = 1;    // １回のみ使用する
    public static final int DEQUEUE2 = 2;    // １回のみ使用するが１個残す
    protected static final int MODE_MAX = 3;  //
 
    // 
    private boolean display = true;
    /* 表示のためのヒントを設定する */
    public void setDisplay(boolean mode) {
        display = mode;
    }
    /* 表示のためのヒントを取得する */
    public boolean isDisplay() { return display; }
 
    //--------------------------------
    // コンストラクタ
    //--------------------------------
    public Queue() { this(DEFAULT_SIZE); }
    
    public Queue(int size) { clear(); };
    
    public Queue(int size,int appendMode) {};
    
    /*
    public Queue(DataInput in) { 
        try {
            readObject(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    */
    //--------------------------------
    /**
        追加時のモードを設定する
        @param mode EXTEND_DATA(デフォルト)/DELETE_TOP/NOT_APPEND/WAIT_DATA
    */
    public void setAppendMode(int mode) {
        if (EXTEND_DATA <= mode && mode <= WAIT_DATA) appendMode = mode;
    }
    /**
        待ち受けモードを設定する
        @param mode trueの時は取り出せるまで待つ
    */
    public void setWaitMode(boolean mode) { waitMode = mode;}
    
    /**
        EXTEND_DATA時の最大サイズを設定する
        @param size 最大サイズ  デフォルト：-1（サイズ制限なし）
    */
    public void setMaxSize(int size) { max_size = size; }
    
    /**
        Queueをクリアする（内部ポインタのみのクリア）
    */
    public void clear() { len = head = tail = over_size = 0; display = true;}

    /* Queueのサイズを取得する */
    public final int size() { return len; }
    /* DELETE_TOP 時に あふれてしまったオブジェクトの個数を返す*/
    public final int getOverSize() { return over_size;}
    /* 空なら true */
    public boolean isEmpty() { return (len <= 0); }

    // 
    // 以下のメソッドは必要に応じてオーバーライドする

    /* 要素の追加を行う(オーバーライドされなければエラー表示） */
    public synchronized boolean enqueue(Object value) {
        System.out.println("Queue method error!!:enqueue-Object");
        return false;
    }
    public synchronized boolean enqueue(int value) {
        System.out.println("Queue method error!!:enqueue-int");
        return false;
    }
    public synchronized boolean enqueue(long value) {
        System.out.println("Queue method error!!:enqueue-long");
        return false;
    }
    public synchronized boolean enqueue(boolean value) {
        System.out.println("Queue method error!!:enqueue-boolean");
        return false;
    }
    public synchronized boolean enqueue(double value) {
        System.out.println("Queue method error!!:enqueue-double");
        return false;
    }
    public synchronized boolean enqueue(CharArray value) {
        System.out.println("Queue method error!!:enqueue-CharArray");
        return false;
    }
    public synchronized boolean enqueue(ByteArray value) {
        System.out.println("Queue method error!!:enqueue-ByteArray");
        return false;
    }
    /* 挿入 (オーバーライドされなければエラー表示）*/
    public synchronized boolean insert(int indx, Object value) {
        System.out.println("Queue method error!!:insert-Object");
        return false;
    }
    public synchronized boolean insert(int indx, int value) {
        System.out.println("Queue method error!!:insert-int");
        return false;
    }
    public synchronized boolean insert(int indx, long value) {
        System.out.println("Queue method error!!:insert-long");
        return false;
    }
    public synchronized boolean insert(int indx, boolean value) {
        System.out.println("Queue method error!!:insert-boolean");
        return false;
    }
    public synchronized boolean insert(int indx, double value) {
        System.out.println("Queue method error!!:insert-double");
        return false;
    }
    public synchronized boolean insert(int indx, CharArray value) {
        System.out.println("Queue method error!!:insert-CharArray");
        return false;
    }
    public synchronized boolean insert(int indx, ByteArray value) {
        System.out.println("Queue method error!!:insert-ByteArray");
        return false;
    }
    
//  /* 要素の取り出し */
//  public synchronized Object dequeue() throws InterruptedException {
//      return null;
//  }

    /* 指定のアイテムを削除する */
    public abstract boolean remove(int index);
    /* アイテムを入れ替える */
    public abstract boolean exchange(int index1, int index2);
    
    /* データサイズを取得する */
    public abstract int sizeOf();
//  
//  /* 先頭を覗く **/
//  public synchronized Object peek() {
//      if (len <= 0) return null;
//      return array[head];
//  }
//
//  /* 指定個所を覗く **/
//  public synchronized Object peek(int index) {
//      if (len <= 0 || index < 0 || index >= len) return null;
//      return array[(head+index)%array.length];
//  }
//
//  /* 指定個所に書き込む  **/
//     public synchronized void poke(int index,Object o) {
//      if (len <= 0 || index < 0 || index >= len) {}
//      else array[(head+index)%array.length] = o;
//  }
    /* 指定個所に書き込む (オーバーライドされなければエラー表示） */
    public synchronized boolean poke(int index, int i) {
        System.out.println("Queue method error!!:poke-int");
        return false;
    }
    public synchronized boolean poke(int index, long l) {
        System.out.println("Queue method error!!:poke-long");
        return false;
    }
    public synchronized boolean poke(int index, double d) {
        System.out.println("Queue method error!!:poke-double");
        return false;
    }
    public synchronized boolean poke(int index, boolean b) {
        System.out.println("Queue method error!!:poke-boolean");
        return false;
    }
    public synchronized boolean poke(int index, CharArray ch) {
        System.out.println("Queue method error!!:poke-CharArray");
        return false;
    }
    public synchronized boolean poke(int index, Object obj) {
        System.out.println("Queue method error!!:poke-Object");
        return false;
    }

    public void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            out.writeInt(type);
            out.writeInt(len);
            out.writeInt(head);
            out.writeInt(tail);
            out.writeInt(appendMode);
            out.writeBoolean(waitMode);
            out.writeInt(max_size);
            out.writeInt(over_size);
        }
    }
    public void readObject(DataInput in)  throws IOException {
        if (in != null) {
            type = in.readInt();
            len  = in.readInt();
            head = in.readInt();
            tail = in.readInt();
            appendMode = in.readInt();
            waitMode   = in.readBoolean();
            max_size   = in.readInt();
            over_size  = in.readInt();
        }
    }

}


//
// [end of Queue.java]
//

