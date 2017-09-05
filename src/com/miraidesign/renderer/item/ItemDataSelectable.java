//------------------------------------------------------------------------
// @(#)ItemDataSelectable.java
//                 選択可能なItemDataに使用する
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//             
//------------------------------------------------------------------------
// $Id$
//------------------------------------------------------------------------
//
package com.miraidesign.renderer.item;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/**
 *  選択可能ItemData インターフェース
 *
 *  @version 1.0 2010-04-07
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public interface ItemDataSelectable {
    public void clear();
    
    //public void add(CharArray ch);
    //public void add(String str);
    public void add(CharArray menu, CharArray option);
    public void add(String menu, String option);
    //public void add(CharArray menu, CharArray option, boolean selected);
    //public void add(String menu, String option, boolean selected);

    //public void add(CharArray menu, CharArray option, int number);
    //public void add(String menu, String option, int number);
    //public void add(CharArray menu, CharArray option, int number, boolean selected);
    //public void add(String menu, String option, int number, boolean selected);
    
    public void add(CharArray menu, CharArray option, CharArray resource, boolean selected);
    public void add(String menu, String option, String resource, boolean selected);
    
    public CharArrayQueue getMenu();
    public CharArrayQueue getOption();
    public CharArrayQueue getResource();
    
    public void setSelectedIndex(int index);
}

//
//
// [end of ItemDataSelectable.java]
//

