//------------------------------------------------------------------------
//    ExitWindowListener.java
//                 共通のウィンドウのイベントの処理
//------------------------------------------------------------------------
package com.miraidesign.event;

import java.awt.event.*;

public class ExitWindowListener extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }
}

//
// $Author: ishi $
//
// [end of ExitWindowListener.java]
//

