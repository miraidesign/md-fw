//------------------------------------------------------------------------
// @(#)HiddenRenderer.java
//                 
//                 Copyright (c) Miraidesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.html;

import com.miraidesign.renderer.item.HiddenData;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;

/**
 *  隠し文字列(input type=hidden) のレンダリング（HTML)を行う
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class HiddenRenderer /*implements ItemRenderer*/ {
    /**
        input(hidden)タグを描画する
        @param ch   出力バッファ
        @param hiddenData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch, HiddenData hiddenData) {
        int size = hiddenData.getSize();
        int max = hiddenData.getMaxLength();
        ch.add("<input type=\"hidden\" name=\"");
        ch.add(hiddenData.getItem().getName());
        ch.add(hiddenData.getName()); // Dynamic用
        ch.add("\" value=\"");
        ch.add(CharArray.replaceTag(hiddenData.getText()));
        ch.add("\"");
        if (size >= 0) {
            ch.add(" size=\"");
            ch.format(size);
            ch.add("\"");
        }
        if (max >= 0) {
            ch.add(" maxlength=\"");
            ch.format(max);
            ch.add("\"");
        }
        ch.add("/>\n");
        return ch;
    }
    static public CharArray drawName(CharArray ch, HiddenData hiddenData) {
        int size = hiddenData.getSize();
        int max = hiddenData.getMaxLength();
        ch.add("name=\"");
        ch.add(hiddenData.getItem().getName());
        ch.add(hiddenData.getName()); // Dynamic用
        ch.add("\" value=\"");
        ch.add(CharArray.replaceTag(hiddenData.getText()));
        ch.add("\"");
        if (size >= 0) {
            ch.add(" size=\"");
            ch.format(size);
            ch.add("\"");
        }
        if (max >= 0) {
            ch.add(" maxlength=\"");
            ch.format(max);
            ch.add("\"");
        }
        return ch;
    }
}

//
//
// [end of HiddenRenderer.java]
//

