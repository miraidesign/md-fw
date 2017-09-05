//------------------------------------------------------------------------
// @(#)PasswordRenderer.java
//                 
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.html;

import com.miraidesign.renderer.item.PasswordData;
import com.miraidesign.renderer.item.ItemConstant;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;

/**
 *  パスワード入力エリア(input type=password) のレンダリング（HTML)を行う
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class PasswordRenderer /*implements ItemRenderer*/ {
    /**
        input(password)タグを描画する
        @param ch   出力バッファ
        @param passwordData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch, 
                                 PasswordData passwordData) {
        SessionObject session = passwordData.getSessionObject();
        int size = passwordData.getSize();
        int max = passwordData.getMaxLength();
        int istyle = passwordData.getIstyle();
        boolean disabled  = passwordData.isDisabled();
        boolean readonly  = passwordData.isReadonly();
        
        ch.add("<input type=\"password\" name=\"");
        ch.add(passwordData.getItem().getName());
        ch.add(passwordData.getName()); // Dynamic用
        ch.add("\" value=\"");
        ch.add(CharArray.replaceTag(passwordData.getText()));
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
        char c = passwordData.getAccessKey();
        if (c > 0) {
            ch.add(" accesskey=\"");
            ch.add(c);
            ch.add("\"");
        }
        if (disabled) ch.add(" disabled=\"disabled\"");
        if (readonly) ch.add(" readonly=\"readonly\"");
        ch.add("/>\n");
        return ch;
    }
    static public CharArray drawName(CharArray ch, 
                                 PasswordData passwordData) {
        SessionObject session = passwordData.getSessionObject();
        int size = passwordData.getSize();
        int max = passwordData.getMaxLength();
        int istyle = passwordData.getIstyle();
        boolean disabled  = passwordData.isDisabled();
        boolean readonly  = passwordData.isReadonly();
        
        ch.add("name=\"");
        ch.add(passwordData.getItem().getName());
        ch.add(passwordData.getName()); // Dynamic用
        ch.add("\" value=\"");
        ch.add(CharArray.replaceTag(passwordData.getText()));
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
        char c = passwordData.getAccessKey();
        if (c > 0) {
            ch.add(" accesskey=\"");
            ch.add(c);
            ch.add("\"");
        }
        if (disabled) ch.add(" disabled=\"disabled\"");
        if (readonly) ch.add(" readonly=\"readonly\"");
        return ch;
    }
}

//
//
// [end of PasswordRenderer.java]
//

