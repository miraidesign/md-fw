//------------------------------------------------------------------------
//    BidirectionalList.java
//              双方向リスト＋ハッシュ
//              ハッシュキーはhashcodeを利用する
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-05   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
//             
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.util.Hashtable;

/** 双方向リスト＋ハッシュ(hashcode) */
public class BidirectionalList extends Hashtable<IntObject,BidirectionalNode>{

    protected volatile BidirectionalNode top;     // 先頭
    protected volatile BidirectionalNode bottom;  // 最新
    
    /**
    static private Thread thread;       // 削除スレッド
    **/
    
    /** consutructor */
    public BidirectionalList() {
        super();
    }
    /** 最後に追加する */
    public void append(BidirectionalNode node) {
        IntObject key =  new IntObject(node.hashCode());
        synchronized (this) {
            if (get(key) == null) {  // not found
                put(key, node);
            }
            update(node);
        }
    }
    
    /** 特定のノードを最後に付け替える */
    public void update(BidirectionalNode node) {
        synchronized (this) {
            if (node != bottom) {
                BidirectionalNode prev = node.getPrev();
                BidirectionalNode next = node.getNext();
        
                if (prev != null) prev.setNext(next);   // リンクを繋変え
                else              top = next;           // 先頭への参照を差し替え
                if (next != null) next.setPrev(prev);   //
                bottom.setNext(node);
                node.setPrev(bottom);
                bottom = node;
                node.setNext(null);
            }
        }
    }
    
    /** 指定のノードを削除する */
    public void remove(BidirectionalNode node) {
        IntObject key  =  IntObject.pop(node.hashCode());
        if (containsKey(key)) {
            synchronized (this) {
                remove(key);    // ハッシュから削除
                BidirectionalNode prev = node.getPrev();
                BidirectionalNode next = node.getNext();
                if (prev != null) prev.setNext(next);   // リンクを繋変え
                else              top = next;           // 先頭への参照を差し替え
                if (next != null) next.setPrev(prev);
                else              bottom = prev;        // 末尾への参照を差し替え
            }
        }
        IntObject.push(key);
    }
    
    /** 先頭のNodeを取り出す */
    public BidirectionalNode getTop()    { return top; }
    /** 最新のNodeを取り出す */
    public BidirectionalNode getBottom() { return bottom; }
    
    /**
        hashcodeよりNodeを取得する
        @param hashcode 検索ノードのhashcode
        @return 存在しなければ null
    */
    public BidirectionalNode get(int hashcode) {
        IntObject key  =  IntObject.pop(hashcode);
        BidirectionalNode obj = (BidirectionalNode)get(key);
        IntObject.push(key);
        return obj;
    }
}

//
// [end of BidirectionalList.java]
//

