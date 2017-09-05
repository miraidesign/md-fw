//------------------------------------------------------------------------
//    TableDataAccess.java
//                 
//                 Copyright (c) MiraiDesign 2010  All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.data;

import com.miraidesign.util.QueueTable;
import com.miraidesign.session.SessionObject;

/**
 * TableDataAccessインターフェース
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public interface TableDataAccess { 
    public boolean execute(SessionObject session, QueueTable table, 
                           int start, int size, boolean cursor_end, Object o);
}

//
// [end of TableDataAccess.java]
//
