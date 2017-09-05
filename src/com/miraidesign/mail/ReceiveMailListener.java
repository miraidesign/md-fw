//------------------------------------------------------------------------
//    ReceiveMailListener.java
//                 メール受信インターフェース
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.mail;

import javax.mail.Message;

/**
  メール受信インターフェース
*/
public interface ReceiveMailListener extends java.util.EventListener {
    /* メール受信イベント */
    public void mailReceived(Message[] messages);

    /*s メール受信完了イベント*/
    public void mailClosed();
}

//
// [end of ReceiveMailListener.java]
//

