//------------------------------------------------------------------------
//    ContentItem.java
//                 
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------
// $Id$
//------------------------------------------------------------------------

package com.miraidesign.content;

import com.miraidesign.util.CharArray;

/** ContentItem インターフェース */
public interface ContentItem {
    public CharArray getLabel();
    public CharArray getDescription();
    public CharArray getType();
    public CharArray getKey();
    public ContentItem getOriginalItem();
    public boolean isBlock();
    public boolean isInput();
    public boolean isTemplate();
    public ContentItemNode getContentItemNode();
}

//
// [end of ContentItem.java]
//

