//------------------------------------------------------------------------
//    AbstractBidirectionalNode.java
//              双方向ノードインターフェース
//              Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.util;

/** 双方向ノードインターフェース */
public class AbstractBidirectionalNode implements BidirectionalNode {
    protected BidirectionalNode prev;     // 直前ノード
    protected BidirectionalNode next;     // 次のノード

    public BidirectionalNode getPrev() {
        return prev;
    }
    public BidirectionalNode getNext() {
        return next;
    }
    public void setPrev(BidirectionalNode node) {
        prev = node;
    }
    public void setNext(BidirectionalNode node) {
        next = node;
    }
}

//
//
// [end of AbstractBidirectionalNode.java]
//

