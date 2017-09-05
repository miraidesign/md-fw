//------------------------------------------------------------------------
// @(#)ItemEventListener.java
//                 ItemEventListener
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
package com.miraidesign.event;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.QueueTable;

/**
 *  ItemEventインターフェース
 *
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public interface ItemEventListener /**extends java.util.EventListener*/ {
    /** 
        @param session セッション
        @param ch レンダリング用バッファ
        @param queue QueueTable
        @return 出力バッファ
    */
    public CharArray execute(SessionObject session, CharArray ch, QueueTable queue);
}

//
//
// [end of ItemEventListener.java]
//

