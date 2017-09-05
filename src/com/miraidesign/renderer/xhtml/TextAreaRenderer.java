//------------------------------------------------------------------------
// @(#)TextAreaRenderer.java
//                 テキストエリア
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.xhtml;

import com.miraidesign.renderer.item.TextAreaData;
import com.miraidesign.renderer.item.ItemConstant;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;

/**
 *  テキストエリアのレンダリング(XHTML)を行う
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class TextAreaRenderer /*implements ItemRenderer*/ {
    /**
        テキストエリアタグを描画する
        @param ch   出力バッファ
        @param textAreaData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch,
                                 TextAreaData textAreaData) {
        SessionObject session = textAreaData.getSessionObject();
        int rows = textAreaData.getRows();
        int cols  = textAreaData.getCols();
        int istyle = textAreaData.getIstyle();
        boolean disabled  = textAreaData.isDisabled();
        boolean readonly  = textAreaData.isReadonly();
        
        ch.add("<textarea name=\"");
        ch.add(textAreaData.getItem().getName());
        ch.add(textAreaData.getName());     // Dynamic
        ch.add("\"");
        if (rows >= 0) {
            ch.add(" rows=\"");
            ch.format(rows);
            ch.add("\"");
        }
        if (cols >= 0) {
            ch.add(" cols=\"");
            ch.format(cols);
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
        
        char c = textAreaData.getAccessKey();
        if (c > 0) {
            ch.add(" accesskey=\"");
            ch.add(c);
            ch.add("\"");
        }
        if (disabled) ch.add(" disabled=\"disabled\"");
        if (readonly) ch.add(" readonly=\"readonly\"");
        ch.add(">\n");
        ch.add(textAreaData.getText());
        ch.add("</textarea>\n");
        return ch;
    }
    static public CharArray drawNameMenu(CharArray ch, 
                                 TextAreaData textAreaData) {
        drawName(ch,textAreaData);
        ch.add(">\n");
        drawMenu(ch,textAreaData);
        ch.add("<!-- --");
        return ch;
    }
    static public CharArray drawName(CharArray ch,
                                 TextAreaData textAreaData) {
        SessionObject session = textAreaData.getSessionObject();
        int rows = textAreaData.getRows();
        int cols  = textAreaData.getCols();
        int istyle = textAreaData.getIstyle();
        boolean disabled  = textAreaData.isDisabled();
        boolean readonly  = textAreaData.isReadonly();
        
        ch.add("name=\"");
        ch.add(textAreaData.getItem().getName());
        ch.add(textAreaData.getName());     // Dynamic
        ch.add("\"");
        if (rows >= 0) {
            ch.add(" rows=\"");
            ch.format(rows);
            ch.add("\"");
        }
        if (cols >= 0) {
            ch.add(" cols=\"");
            ch.format(cols);
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
        char c = textAreaData.getAccessKey();
        if (c > 0) {
            ch.add(" accesskey=\"");
            ch.add(c);
            ch.add("\"");
        }
        if (disabled) ch.add(" disabled=\"disabled\"");
        if (readonly) ch.add(" readonly=\"readonly\"");
        return ch;
    }
    static public CharArray drawMenu(CharArray ch,
                                 TextAreaData textAreaData) {
        ch.add(textAreaData.getText());
        return ch;
    }
}

//
//
// [end of TextAreaRenderer.java]
//

