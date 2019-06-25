//------------------------------------------------------------------------
//    UploadJson.java
//         アップロードされた情報を保管
//                 Copyright (c) MiraiDesign 2019 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.servlet;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayFile;

/**
 *  アップロードされた情報を一時保管する
*/

public class UploadJson extends CharArrayFile
{
    public String contentType;
    public CharArray fileName;
    public int rowCount;   // 行数
}

//
// [end of UploadJson.java]
//

