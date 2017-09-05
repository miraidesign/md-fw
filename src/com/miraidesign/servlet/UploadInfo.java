//------------------------------------------------------------------------
//    UploadInfo.java
//         アップロードされた情報を保管
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.servlet;

import com.miraidesign.util.ByteArray;
import com.miraidesign.util.CharArray;

/**
 *  アップロードされた情報を一時保管する
*/

public class UploadInfo extends ByteArray
{
    public String contentType;
    public CharArray fileName;
    public int rowCount;   // 行数
}

//
// [end of UploadInfo.java]
//

