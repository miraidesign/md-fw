//------------------------------------------------------------------------
// @(#)CheckBoxRenderer.java
//                 
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.html;

import com.miraidesign.common.SystemConst;
import com.miraidesign.renderer.item.CheckBoxData;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.BooleanQueue;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;

/**
 *  チェックボックスのレンダリング（HTML)を行う
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class CheckBoxRenderer /*implements ItemRenderer*/ {
    /**
        チェックボックスを描画する
        @param ch   出力バッファ
        @param checkBoxData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch,
                                 CheckBoxData checkBoxData) {
        CharArrayQueue  menu   = checkBoxData.getMenu();
        CharArrayQueue  option = checkBoxData.getOption();
        BooleanQueue    queue  = checkBoxData.getQueue();
        boolean      disabled  = checkBoxData.isDisabled();
        boolean     tagConvert = checkBoxData.getTagConvert();

        for (int i = 0; i < menu.size(); i++) {
            ch.add("<input type=\"checkbox\" name=\"");
            ch.add(checkBoxData.getItem().getName());
            ch.add(checkBoxData.getName()); // Dynamic用
            ch.add("\"");
            if (queue.peek(i)) ch.add(" checked=\"checked\"");
            if (option.peek(i).length() > 0) {
                ch.add(" value=\"");
                ch.add(tagConvert ? CharArray.replaceTag(option.peek(i)) : option.peek(i));
                ch.add("\"");
            }
            if (disabled) ch.add(" disabled=\"disabled\"");
            ch.add(" />");
            CharArray ca = menu.peek(i);
            if (ca.indexOf("\n") < 0) { 
                ch.add(tagConvert ? CharArray.replaceTag(ca) : ca);
            } else {
                CharToken token = CharToken.pop();
                token.set(ca,"\n");
                for (int j = 0; j < token.size(); j++) {
                    ch.add(tagConvert ? CharArray.replaceTag(token.get(j)) : token.get(j));
                    if (j + 1 < token.size()) {
                        ch.add("<br />\n");
                    }
                }
                CharToken.push(token);
            }
            ch.add("\n");
        }
        // ダミーデータを付属
        ch.add("<input type=\"hidden\" name=\"");
        ch.add(SystemConst.clearKey);
        //ch.format(checkBoxData.getItem().getMixedID());
        ch.add(checkBoxData.getItem().getName());
        ch.add(checkBoxData.getName()); // Dynamic用
        ch.add("\" />\n");
        //ch.replace("<hr>","<hr />");
        //ch.replace("<br>","<br />");
        return ch;
    }
    static public CharArray draw(CharArray ch,
                                 CheckBoxData checkBoxData, int index) {
        CharArray menu     = checkBoxData.getMenu().peek(index);
        CharArray option   = checkBoxData.getOption().peek(index);
        boolean   checked  = checkBoxData.getQueue().peek(index);
        boolean  disabled  = checkBoxData.isDisabled();
        boolean   tagConvert = checkBoxData.getTagConvert();

        ch.add("<input type=\"checkbox\" name=\"");
        ch.add(checkBoxData.getItem().getName());
        ch.add(checkBoxData.getName()); // Dynamic用
        ch.add("\"");
        if (checked) ch.add(" checked=\"checked\"");
        if (option != null && option.length() > 0) {
            ch.add(" value=\"");
            ch.add(tagConvert ? CharArray.replaceTag(option) : option);
            ch.add("\"");
        }
        if (disabled) ch.add(" disabled=\"disabled\"");
        ch.add(" />");
        if (menu.indexOf("\n") < 0) { 
            ch.add(tagConvert ? CharArray.replaceTag(menu) : menu);
        } else {
            CharToken token = CharToken.pop();
            token.set(menu,"\n");
            for (int j = 0; j < token.size(); j++) {
                ch.add(tagConvert ? CharArray.replaceTag(token.get(j)) : token.get(j));
                if (j + 1 < token.size()) {
                    ch.add("<br />\n");
                }
            }
            CharToken.push(token);
        }
        if (index == 0) drawHidden(ch, checkBoxData);
        else            ch.add("\n");
        
        //ch.replace("<hr>","<hr />");
        //ch.replace("<br>","<br />");
        return ch;
    }
    static public CharArray drawHidden(CharArray ch,CheckBoxData checkBoxData) {
        return drawHidden(ch,checkBoxData,true);
    }
    static public CharArray drawHidden(CharArray ch, CheckBoxData checkBoxData,
            boolean mode) { // 最後に ">"をつけるか？
        //ch.add("&nbsp;");   // デザイン調整用
        ch.add("<input type=\"hidden\" name=\"");
        ch.add(SystemConst.clearKey);
        //ch.format(checkBoxData.getItem().getMixedID());
        ch.add(checkBoxData.getItem().getName());
        ch.add(checkBoxData.getName()); // Dynamic用
        ch.add("\" ");
        if (mode) ch.add("/>\n");
        return ch;
    }
    
    static public CharArray drawName(CharArray ch,
                                 CheckBoxData checkBoxData, int index) {
        CharArray option = checkBoxData.getOption().peek(index);
        boolean checked  = checkBoxData.getQueue().peek(index);
        boolean disabled = checkBoxData.isDisabled();
        boolean     tagConvert = checkBoxData.getTagConvert();

        ch.add("name=\"");
        ch.add(checkBoxData.getItem().getName());
        ch.add(checkBoxData.getName()); // Dynamic用
        ch.add("\"");
        if (checked) ch.add(" checked=\"checked\"");
        if (option != null && option.length() > 0) {
            ch.add(" value=\"");
            ch.add(tagConvert ? CharArray.replaceTag(option) : option);
            ch.add("\"");
        }
        if (disabled) ch.add(" disabled=\"disabled\"");
        return ch;
    }
    static public CharArray drawNameHidden(CharArray ch, CheckBoxData checkBoxData, int index) {
        drawName(ch,checkBoxData,index);
        if (index == 0) {   // Hiddenタグの描画
            ch.add("/>\n");
            drawHidden(ch,checkBoxData,false);
        }
        return ch;
    }
    static public CharArray drawMenu(CharArray ch,
                                 CheckBoxData checkBoxData, int index) {
        boolean     tagConvert = checkBoxData.getTagConvert();
        CharArray menu     = checkBoxData.getMenu().peek(index);

        if (menu.indexOf("\n") < 0) { 
            ch.add(tagConvert ? CharArray.replaceTag(menu) : menu);
        } else {
            CharToken token = CharToken.pop();
            token.set(menu,"\n");
            for (int j = 0; j < token.size(); j++) {
                ch.add(tagConvert ? CharArray.replaceTag(token.get(j)) : token.get(j));
                if (j + 1 < token.size()) {
                    ch.add("<br />\n");
                }
            }
            CharToken.push(token);
        }
        //ch.replace("<hr>","<hr />");
        //ch.replace("<br>","<br />");
        return ch;
    }
}

//
//
// [end of CheckBoxRenderer.java]
//

