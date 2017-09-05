//------------------------------------------------------------------------
//    ThreadQueue.java
//             Thread 連続実行用クラス
//
//          Copyright (c) Mirai Design Institute 2012 All rights reserved.
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------

package com.miraidesign.util;


/** 
  Thread連続実行用クラス
*/
public class ThreadQueue {
    private boolean debug = true;
    
    private ObjectQueue queue = new ObjectQueue();  //Thread 同期用Objectqueue
    
    /** constructor */
    public ThreadQueue() {
        // do nothing
    }
    
    /**
        Thread同期用Objectを追加する
        @param object 同期用オブジェクト
        @return true:追加成功  false:失敗またはすでに存在する
    */
    public boolean add(Object object) {
        boolean sts = false;
        if (object != null) {
            int index = queue.find(object);
            if (index >= 0) {
                if (debug) System.out.println("ThreadQueue#add ["+object+"]は登録済みです");
            } else {
                queue.enqueue(object);
                if (debug) System.out.println("ThreadQueue#add ["+object+"]を追加しました");
            }
        }
        return sts;
    }
    
    /**
        削除してnotify
        @param object 同期用オブジェクト
        @return true:削除&amp;notify成功 
    */
    public boolean remove(Object object) {
        boolean sts = false;
        if (object != null) {
            int index = queue.find(object);
            if (index >= 0) {   // objectが存在する
                queue.remove(0);
                if (debug) System.out.println("ThreadQueue#remove ["+index+":"+object+"]を削除しました");
                if (index == 0) {
                    Object next = queue.peek();
                    if (next != null) {
                        if (debug) System.out.println("ThreadQueue#remove ["+next+"]に通知します");
                        synchronized (next) {
                            next.notifyAll();
                        }
                        sts = true;
                    }
                }
            } else {  //存在しない
                if (debug) System.out.println("ThreadQueue#remove ["+object+"]をが見つかりません");
            }
        }
        return sts;
    }
    
    
}


//
// [end of ThreadQueue.java]
//

