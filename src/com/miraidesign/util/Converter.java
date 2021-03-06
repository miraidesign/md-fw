//------------------------------------------------------------------------
//    Converter.java
//                 コンバーターインターフェース
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-04   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------

package com.miraidesign.util;


/** 
  コンバータインターフェース
**/
public interface Converter {
    public QueueElement add(String str);
    public QueueElement add(CharArray ca);
    public QueueElement add(CharSequence cs);
    
    public CharArray convert(CharArray str);
    public String convert(String str);
    public Converter copy();
}


//
// [end of Converter.java]
//

