//------------------------------------------------------------------------
// @(#)SubmitRenderer.java
//                 
//                 Copyright (c) Miraidesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.xhtml;

import com.miraidesign.renderer.item.SubmitData;
import com.miraidesign.renderer.item.ItemConstant;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;

/**
 *  SUBMITボタン(input type=submit) のレンダリング（XHTML)を行う
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class SubmitRenderer /*implements ItemRenderer*/ {
    /**
        input(submit)タグを描画する
        @param ch   出力バッファ
        @param submitData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch,
                                 SubmitData submitData) {
        SessionObject session = submitData.getSessionObject();
        CharArray value = submitData.getText();
        int size = submitData.getSize();
        int max = submitData.getMaxLength();
        int align = submitData.getAlign();
        boolean disabled  = submitData.isDisabled();
        
        if (align > 0) {
            ch.add("<div");
            //if (session.isDocomo()) ch.add(" style=\"text-align:");
            ch.add(" align=\"");
            ch.add(ItemConstant.align[align]);
            ch.add("\">");
        }
        ch.add("<input type=\"submit\" name=\"");
        ch.add(submitData.getItem().getName());
        ch.add(submitData.getName()); // Dynamic用
        ch.add("\"");
        if (value.length()>0) {
            ch.add(" value=\"");
            ch.add(value);
            ch.add("\"");
        }
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
        char c = submitData.getAccessKey();
        if (c > 0) {
            ch.add(" accesskey=\"");
            ch.add(c);
            ch.add("\"");
        }
        if (disabled) ch.add(" disabled=\"disabled\""); 
        ch.add("/>");
        if (align > 0) ch.add("</div>");
        ch.add("\n");
        return ch;
    }
    static public CharArray drawName(CharArray ch,
                                 SubmitData submitData) {
        int size = submitData.getSize();
        int max  = submitData.getMaxLength();
        boolean disabled  = submitData.isDisabled();
        ch.add("name=\"");
        ch.add(submitData.getItem().getName());
        ch.add(submitData.getName()); // Dynamic用
        ch.add("\"");
        ch.add(" value=\"");
        ch.add(submitData.getText());
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
        char c = submitData.getAccessKey();
        if (c > 0) {
            ch.add(" accesskey=\"");
            ch.add(c);
            ch.add("\"");
        }
        if (disabled) ch.add(" disabled=\"disabled\""); 
        
        return ch;
    }
}

//
// [end of SubmitRenderer.java]
//

