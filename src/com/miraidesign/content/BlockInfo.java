//------------------------------------------------------------------------
// @(#)BlockInfo.java
//              HTMLブロック制御情報
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

/**
 *  BlockInfo   ブロック制御情報を保管します
 *              フレームワークが使用します
 *  @version 0.5 2010-04-05
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class BlockInfo {
    public int max  = -1;
    public int count = -1;
    
    public BlockInfo(int i) { max = count = i;}

}

//
//
// [end of BlockInfo.java]
//

