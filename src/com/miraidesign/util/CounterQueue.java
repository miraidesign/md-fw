//------------------------------------------------------------------------
//    CounterQueue.java
//         IntQueueを利用したカウンタ
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-05   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
// $Id$
//------------------------------------------------------------------------

package com.miraidesign.util;

/** 
  IntQueueを利用したカウンタ 
*/
public  class CounterQueue extends IntQueue {
    private boolean debug = false;  // デバッグ表示

    //--------------------------------
    // コンストラクタ
    //--------------------------------
    /** default constructor */
    private CounterQueue() { }

    /** constructor */
    public CounterQueue(int size) {
        super(size);
        for (int i = 0; i < size; i++) enqueue(0);
    }
    
    /** 
        全データを0でリセットする
    */
    public CounterQueue reset() {reset(0); return this;}

    /** 
        全データを指定値でリセットする
        @param no リセットする値
    */
    public CounterQueue reset(int no) {
        for (int i = 0; i < size(); i++) {
            poke(i,no);
        }
        return this;
    }
    
    /**
        指定位置の値をインクリメント(+1)します
        @param index カウンターの位置（正しく設定すること）
        @return インクリメント後の値 (範囲エラー時 -1)
    */
    public int inc(int index) {
        int num = -1;
        if (index >= 0 && index < size()) {
            num = peek(index);
            poke(index, ++num);
        } else {
            if (debug) System.out.println("CounterQueue inc("+index+") index error size="+size());
        }
        return num;
    }

    /**
        指定位置の値をデクリメント(-1)します
        @param index カウンターの位置（正しく設定すること）
        @return デクリメント後の値 (範囲エラー時 -1)
    */
    public int dec(int index) {
        int num = -1;
        if (index >= 0 && index < size()) {
            num = peek(index);
            poke(index, --num);
        } else {
            if (debug) System.out.println("CounterQueue inc("+index+") index error size="+size());
        }
        return num;
    }
    
}


//
// [end of CounterQueue.java]
//

