//------------------------------------------------------------------------
// @(#)ItemSerializable.java
//                 Item の高速保存に使用する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
package com.miraidesign.renderer.item;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *  Item高速保存、読込用インターフェース
 *
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public interface ItemSerializable {
    public  void readObject(DataInput in) throws IOException;
    public  void writeObject(DataOutput out) throws IOException;
}

//
//
// [end of ItemSerializable.java]
//

