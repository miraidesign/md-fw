//------------------------------------------------------------------------
//    CloseWinodwListener.java
//                 共通のウィンドウのイベントの処理
//                 Copyright (c) K&I Software Development co. ltd. 1997  
//                                               All Rights Reserved.     
//                                               update: 97-10-24  ishioka
//------------------------------------------------------------------------
// $Header$
//------------------------------------------------------------------------
package com.miraidesign.event;

import java.awt.*;
import java.awt.event.*;

public class CloseWindowListener extends WindowAdapter {
    private  WindowControl  obj;
    
    public   CloseWindowListener(WindowControl oo) { 
        obj = oo;  
    }
    
    //  ウィンドウのイベント処理を行う
    public void windowClosing(WindowEvent e) {
        obj.Close();
    }
}

//
// $Author$
// $Source$
// $Revision$
// $Date$
//
// $Log$
//
//
// [end of CloseWindowListener.java]
//

