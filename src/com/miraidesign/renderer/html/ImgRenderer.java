//------------------------------------------------------------------------
// @(#)ImgRenderer.java
//                 
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.html;

import com.miraidesign.renderer.item.ImgData;
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
public class ImgRenderer /*implements ItemRenderer*/ {
    /**
        input(text)タグを描画する
        @param ch   出力バッファ
        @param imgData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch,
                                 ImgData imgData) {
        SessionObject session = imgData.getSessionObject();
        CharArray alt = imgData.getAlt();
        int width = imgData.getWidth();
        int height = imgData.getHeight();
        int align = imgData.getAlign();
        int border = imgData.getBorder();

        ch.add("<img src=\"");
        ch.add(imgData.getSrc());
        ch.add("\"");
        if (alt.length()>0) {
            ch.add(" alt=\"");
            ch.add(alt);
            ch.add("\"");
        }
        if (width > 0) {
            ch.add(" width=\"");
            ch.format(width);
            ch.add("\"");
        }
        if (height > 0) {
            ch.add(" height=\"");
            ch.format(height);
            ch.add("\"");
        }
        if (align > 0) {
            //if (session.isDocomo()) {
            //    ch.add(" style=\"");
            //    ch.add(ItemConstant.xhtml_align[align]);
            //    ch.add("\"");
            //} else {
                ch.add(" align=\"");
                ch.add(ItemConstant.align[align]);
                ch.add("\"");
            //}
        }
        if (border >= 0) {
            ch.add(" border=\"");
            ch.format(border);
            ch.add("\"");
        }
        ch.add("/>\n");
        return ch;
    }
    static public CharArray drawSrc(CharArray ch,
                                 ImgData imgData) {
        SessionObject session = imgData.getSessionObject();
        CharArray alt = imgData.getAlt();
        int width = imgData.getWidth();
        int height = imgData.getHeight();
        int align = imgData.getAlign();
        int border = imgData.getBorder();

        ch.add("src=\"");
        ch.add(imgData.getSrc());
        ch.add("\"");
        if (alt.length()>0) {
            ch.add(" alt=\"");
            ch.add(alt);
            ch.add("\"");
        }
        if (width > 0) {
            ch.add(" width=\"");
            ch.format(width);
            ch.add("\"");
        }
        if (height > 0) {
            ch.add(" height=\"");
            ch.format(height);
            ch.add("\"");
        }
        if (align > 0) {
            //if (session.isDocomo()) {
            //    ch.add(" style=\"");
            //    ch.add(ItemConstant.xhtml_align[align]);
            //    ch.add("\"");
            //} else {
                ch.add(" align=\"");
                ch.add(ItemConstant.align[align]);
                ch.add("\"");
            //}
        }
        if (border >= 0) {
            ch.add(" border=\"");
            ch.format(border);
            ch.add("\"");
        }
        return ch;
    }
}

//
//
// [end of ImgRenderer.java]
//

