//------------------------------------------------------------------------
//    SessionEvent.java
//                 セッションイベントを定義する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.session;

/**
 *  セッションイベントを定義する
 *  
 *  @version 0.5 2010-04-05
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public interface SessionEvent {
    /** セッションリセット時に呼ばれる */
    public void reset(SessionObject session);
}

//
// [end of SessionEvent.java]
//

