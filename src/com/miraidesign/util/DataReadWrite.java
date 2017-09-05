//------------------------------------------------------------------------
//    DataReadWrite.java
//                 高速シリアライズインターフェース
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-05   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** 高速シリアライズインターフェース **/
public interface DataReadWrite {
    // シリアライズ用 
    public void writeObject(DataOutput out) throws IOException;
    public void readObject(DataInput in)  throws IOException;
}

//
// [end of DataReadWrite.java]
//

