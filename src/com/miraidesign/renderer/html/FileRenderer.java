//------------------------------------------------------------------------
// @(#)FileRenderer.java
//                 
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.html;

import com.miraidesign.util.CharArray;
import com.miraidesign.session.SessionObject;
import com.miraidesign.renderer.item.FileData;
import com.miraidesign.renderer.item.ItemConstant;

/**
 *  文字列入力エリア(input type=file) のレンダリング（HTML)を行う
**/
public class FileRenderer /*implements ItemRenderer*/ {
    /**
        input(file)タグを描画する
        @param ch   出力バッファ
        @param fileData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch,
                                 FileData fileData) {
        SessionObject session = fileData.getSessionObject();
        int size = fileData.getSize();
        int max = fileData.getMaxLength();
        int istyle = fileData.getIstyle();
        ch.add("<input type=\"file\" name=\"");
        ch.add(fileData.getItem().getName());
        ch.add(fileData.getName()); // Dynamic用
        ch.add("\"");
        ch.add(" value=\"");
        ch.add(CharArray.replaceTag(fileData.getText()));
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
            //if (session.isDocomo()) {
            //    ch.add(" style=\"");
            //    ch.add(ItemConstant.xhtml_istyle[istyle]);
            //    ch.add("\"");
            //} else {
            ch.add(" istyle=\"");
            ch.format(istyle);
            ch.add("\"");
            //}
        }
        ch.add("/>\n");
        return ch;
    }
    static public CharArray drawName(CharArray ch,
                                 FileData fileData) {
        SessionObject session = fileData.getSessionObject();
        int size = fileData.getSize();
        int max = fileData.getMaxLength();
        int istyle = fileData.getIstyle();
        ch.add("name=\"");
        ch.add(fileData.getItem().getName());
        ch.add(fileData.getName()); // Dynamic用
        ch.add("\"");
        ch.add(" value=\"");
        ch.add(CharArray.replaceTag(fileData.getText()));
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
            //if (session.isDocomo()) {
            //    ch.add(" style=\"");
            //    ch.add(ItemConstant.xhtml_istyle[istyle]);
            //    ch.add("\"");
            //} else {
            ch.add(" istyle=\"");
            ch.format(istyle);
            ch.add("\"");
            //}
        }
        return ch;
    }
}

//
//
// [end of FileRenderer.java]
//

