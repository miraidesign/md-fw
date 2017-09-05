//------------------------------------------------------------------------
//    BidirectionalHashList.java
//              双方向リスト＋ハッシュ
//              指定ハッシュキーを使用する
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-05   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.util.Hashtable;

/** 双方向リスト＋ハッシュ(指定キー) */
public class BidirectionalHashList extends Hashtable<Object,BidirectionalNode>{
    static private final boolean debug = false;

    protected volatile BidirectionalNode top;     // 先頭
    protected volatile BidirectionalNode bottom;  // 最新
    
    /**
    static private Thread thread;       // 削除スレッド
    **/
    
    /** consutructor */
    public BidirectionalHashList() {
        super();
    }
    /** 
        最後に追加する 
        @param key 指定キー
        @param node 追加ノード
    */
    public void append(Object key, BidirectionalNode node) {
        synchronized (this) {
            if (get(key) == null) {  // not found
                put(key, node);
            }
            //update(node);
            if (top == null) {
                top = bottom = node;
            } else {
                bottom.setNext(node);
                node.setPrev(bottom);
                bottom = node;
            }
        }
    }
    
    /** 特定のノードを最後に付け替える 
        @param key ノードを特定するキー情報
    */
    public void update(Object key) {
        BidirectionalNode node = (BidirectionalNode)get(key);
        if (node != null) update(node);
    }
    /** 特定のノードを最後に付け替える 
        @param node 指定ノード
    */
    public void update(BidirectionalNode node) {
        synchronized (this) {
            if (bottom != null && node == bottom) {
                // do nothing
            } else {
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
    public BidirectionalNode remove(Object key) {
        BidirectionalNode obj = null;
        synchronized (this) {
            if (containsKey(key)) {
if (debug) System.out.println("remove key["+key+"]");
                obj = super.remove(key);    // ハッシュから削除
                BidirectionalNode prev = ((BidirectionalNode)obj).getPrev();
                BidirectionalNode next = ((BidirectionalNode)obj).getNext();
                if (prev != null) prev.setNext(next);   // リンクを繋変え
                else              top = next;           // 先頭への参照を差し替え
                if (next != null) next.setPrev(prev);
                else              bottom = prev;        // 末尾への参照を差し替え
            } else {
if (debug) System.out.println("remove key["+key+"] not found");
            }
        }
        return obj;
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
// [end of BidirectionalHashList.java]
//

