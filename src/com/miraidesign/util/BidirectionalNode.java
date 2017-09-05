//------------------------------------------------------------------------
//    BidirectionalNode.java
//              双方向ノードインターフェース
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-05   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
//             
//------------------------------------------------------------------------

package com.miraidesign.util;

/** 双方向ノードインターフェース */
public interface BidirectionalNode {
    public BidirectionalNode getPrev();
    public BidirectionalNode getNext();
    public void setPrev(BidirectionalNode node);
    public void setNext(BidirectionalNode node);
}

//
// [end of BidirectionalNode.java]
//

