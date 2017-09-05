//------------------------------------------------------------------------
//    WindowControl.java                                                  
//                 ダイアログウィンドのOpen()とClose()のインタフェースを
//                 提供する
//------------------------------------------------------------------------
package com.miraidesign.event;

import java.awt.*;
import java.awt.event.*;

public interface WindowControl {     
    public void Open();         // ダイアログウィンドウをオープン
    public void Close();        // ダイアログウィンドウをクローズ
}

//
// $Author: ishi $
//
// [end of WindowControl.java]
//
