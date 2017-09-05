//------------------------------------------------------------------------
// @(#)DynamicRenderer.java
//                 
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.xhtml;

import com.miraidesign.renderer.item.DynamicData;
import com.miraidesign.util.CharArray;
import com.miraidesign.session.SessionObject;

/**
 *  可変データ のレンダリング（XHTML)を行う
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class DynamicRenderer /*implements ItemRenderer*/ {
    /**
        レンダリングする
        @param session SessionObject
        @param dynamicData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(SessionObject session,
                                 DynamicData dynamicData) {
        CharArray ch = session.getBuffer();
        
        
        
        return ch;
    }
}

//
//
// [end of DynamicRenderer.java]
//

