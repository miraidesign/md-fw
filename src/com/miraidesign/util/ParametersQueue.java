//------------------------------------------------------------------------
//    ParametersQueue.java
//        パラメータ情報を格納する
//
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------
//   ┌─────────────────────────┐
//   │┌─────┐┌─────┐                      │１回のtemplateで
//   ││Parameter ││Parameter │ ・・・・ ObjectQueue │使用するセット
//   │└─────┘└─────┘                      │
//   ├─────────────────────────┤
//   │┌─────┐┌─────┐                      │
//   ││Parameter ││Parameter │ ・・・・ ObjectQueue │
//   │└─────┘└─────┘                      │
//   ├─────────────────────────┤
//   │                                                  │
//   │  ・・・・・・・・・                              │↓ ObjectQueue
//   │                                                  │
//   └─────────────────────────┘
//


package com.miraidesign.util;

/**  パラメータ情報を格納する */
public class ParametersQueue extends ObjectQueue {

    private int mode = DEQUEUE;     // LOOPより変更
    private boolean static_mode = false;

    /** constructor */
    public ParametersQueue() {
        super();
    }
    public ParametersQueue(int size) {
        super(size);
        setSize(size);
    }

    /**
        モードを設定する
        @param mode LOOP / DEQUEUE
    */
    public void setMode (int mode) {
        if (mode >= 0 && mode < MODE_MAX) {
//System.out.println("ParametersQueue:setMode:"+mode);
            this.mode = mode;
        }
    }
    /**
        staticモードを設定する
    */
    public void setStaticMode (boolean mode) {
        static_mode = mode;
    }

    public int getMode() {return mode;}
    public boolean getStaticMode() {return static_mode;}

    /**
        サイズを調整する
        @param size 新しいパラメータブロックサイズ
    */
    public void setSize(int size) {
        if (size > 0) {
            while (size > size()) enqueue(new ObjectQueue());
            while (size < size()) remove(size()-1);
        }
    }
    /**
        パラメータの使用すべき１セットを取り出す
        ContentTemplate から呼ばれる
        @return null で存在しない
    */
    public ObjectQueue get() {
        ObjectQueue queue = null;
        int size = size();
        if (size > 0) {
            try {
                if (mode == LOOP) {
                    if (size == 1) {
                        queue = (ObjectQueue)peek();
                    } else {
                        queue = (ObjectQueue)dequeue();
                        enqueue(queue);
                    }
                } else if (mode == DEQUEUE) {
                    queue = (ObjectQueue)dequeue();
                } else if (mode == DEQUEUE2) {
                    /** テスト用に １個残して見る **/
                    if (size == 1 && max == 1) queue = (ObjectQueue)peek();  // 試験用！！
                    else queue = (ObjectQueue)dequeue();
                    //queue = (ObjectQueue)dequeue();
//System.out.println("ParameterQueue get:(DEQUEUE) size="+size);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return queue;
    }

    /**
        パラメータを追加する
    */
    public void add(int index, QueueElement parameter) {
        if (index > 0) {
            if (index >= size()) setSize(index+1);
            ObjectQueue queue = (ObjectQueue)peek(index);
            queue.enqueue(parameter);
        }
    }

    public void add(QueueElement parameter) {
        add(0,parameter);
    }

}

//
//
// [end of ParametersQueue.java]
//
