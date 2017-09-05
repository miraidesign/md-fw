//------------------------------------------------------------------------
// @(#)TextRenderer.java
//                 
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.html;

import com.miraidesign.renderer.item.TextData;
import com.miraidesign.renderer.item.ItemConstant;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;

/**
 *  文字列入力エリア(input type=text) のレンダリング（HTML)を行う
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class TextRenderer /*implements ItemRenderer*/ {
    /**
        input(text)タグを描画する
        @param ch   出力バッファ
        @param textData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch,
                                 TextData textData) {
        SessionObject session = textData.getSessionObject();
        int size = textData.getSize();
        int max = textData.getMaxLength();
        int istyle = textData.getIstyle();
        boolean disabled  = textData.isDisabled();
        boolean readonly  = textData.isReadonly();
        
        ch.add("<input type=\"text\" name=\"");
        ch.add(textData.getItem().getName());
        ch.add(textData.getName()); // Dynamic用
        ch.add("\"");
        ch.add(" value=\"");
        ch.add(CharArray.replaceTag(textData.getText()));
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
        if (istyle >= 0 && istyle <= 4) {
            //if (session.isDocomo() && session.isXHTML()) {
            //    ch.add(" style=\"");
            //    ch.add(ItemConstant.xhtml_istyle[istyle]);
            //    ch.add("\"");
            //} else {
                ch.add(" istyle=\"");
                ch.format(istyle);
                ch.add("\"");
            //}
        }
        char c = textData.getAccessKey();
        if (c > 0) {
            ch.add(" accesskey=\"");
            ch.add(c);
            ch.add("\"");
        }
        if (disabled) ch.add(" disabled=\"disabled\""); //
        if (readonly) ch.add(" readonly=\"readonly\"");
        ch.add("/>\n");
        return ch;
    }
    static public CharArray drawName(CharArray ch,
                                 TextData textData) {
        SessionObject session = textData.getSessionObject();
        int size = textData.getSize();
        int max = textData.getMaxLength();
        int istyle = textData.getIstyle();
        boolean disabled  = textData.isDisabled();
        boolean readonly  = textData.isReadonly();
        ch.add("name=\"");
        ch.add(textData.getItem().getName());
        ch.add(textData.getName()); // Dynamic用
        ch.add("\"");
        ch.add(" value=\"");
        ch.add(CharArray.replaceTag(textData.getText()));
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
        if (istyle >= 0 && istyle <= 4) {
            //if (session.isDocomo() && session.isXHTML()) {
            //    ch.add(" style=\"");
            //    ch.add(ItemConstant.xhtml_istyle[istyle]);
            //    ch.add("\"");
            //} else {
                ch.add(" istyle=\"");
                ch.format(istyle);
                ch.add("\"");
            //}
        }
        char c = textData.getAccessKey();
        if (c > 0) {
            ch.add(" accesskey=\"");
            ch.add(c);
            ch.add("\"");
        }
        if (disabled) ch.add(" disabled=\"disabled\""); //
        if (readonly) ch.add(" readonly=\"readonly\"");
        return ch;
    }
}

//
// [end of TextRenderer.java]
//

