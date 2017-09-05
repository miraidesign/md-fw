//------------------------------------------------------------------------
// @(#)RadioButtonRenderer.java
//                 
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.xhtml;

import com.miraidesign.renderer.item.RadioButtonData;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;

/**
 *  ラジオボタンのレンダリング（XHTML)を行う
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class RadioButtonRenderer /*implements ItemRenderer*/ {
    /**
        ラジオボタンを描画する<br>
        表示文字列の最後に\nが付いていればそこで改行します
        @param ch   出力バッファ
        @param radioButtonData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch,
                                 RadioButtonData radioButtonData) {
        CharArrayQueue  menu    = radioButtonData.getMenu();
        CharArrayQueue  option  = radioButtonData.getOption();
        int selectedIndex = radioButtonData.getSelectedIndex();
        boolean disabled   = radioButtonData.isDisabled();
        boolean tagConvert = radioButtonData.getTagConvert();

        for (int i = 0; i < menu.size(); i++) {
            ch.add("<input type=\"radio\" name=\"");
            ch.add(radioButtonData.getItem().getName());
            ch.add(radioButtonData.getName()); // Dynamic用
            ch.add("\" ");
            if (selectedIndex == i) {
                ch.add("checked=\"checked\" ");
            }
            if (option.peek(i).length() > 0) {
                ch.add("value=\"");
                ch.add(tagConvert ? CharArray.replaceTag(option.peek(i)) : option.peek(i));
                ch.add("\"");
            }
            if (disabled) ch.add(" disabled=\"disabled\"");
            ch.add("/>");
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
        ch.replace("<hr>","<hr />");
        ch.replace("<br>","<br />");
        return ch;
    }
    static public CharArray draw(CharArray ch,
                                 RadioButtonData radioButtonData, int index) {
        CharArray  menu   = radioButtonData.getMenu().peek(index);
        CharArray  option = radioButtonData.getOption().peek(index);
        int selectedIndex = radioButtonData.getSelectedIndex();
        boolean disabled   = radioButtonData.isDisabled();
        boolean tagConvert = radioButtonData.getTagConvert();

        ch.add("<input type=\"radio\" name=\"");
        ch.add(radioButtonData.getItem().getName());
        ch.add(radioButtonData.getName()); // Dynamic用
        ch.add("\" ");
        if (selectedIndex == index) ch.add("checked=\"checked\" ");
        if (option.length() > 0) {
            ch.add("value=\"");
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
        ch.add("\n");
        ch.replace("<hr>","<hr />");
        ch.replace("<br>","<br />");
        return ch;
    }
    static public CharArray drawName(CharArray ch,
                                 RadioButtonData radioButtonData, int index) {
        CharArray  option = radioButtonData.getOption().peek(index);
        int selectedIndex = radioButtonData.getSelectedIndex();
        boolean disabled   = radioButtonData.isDisabled();
        boolean tagConvert = radioButtonData.getTagConvert();

        ch.add("name=\"");
        ch.add(radioButtonData.getItem().getName());
        ch.add(radioButtonData.getName()); // Dynamic用
        ch.add("\" ");
        if (selectedIndex == index) ch.add("checked=\"checked\" ");
        if (option.length() > 0) {
            ch.add("value=\"");
            ch.add(tagConvert ? CharArray.replaceTag(option) : option);
            ch.add("\"");
        }
        if (disabled) ch.add(" disabled=\"disabled\"");
        return ch;
    }
    static public CharArray drawMenu(CharArray ch,
                                 RadioButtonData radioButtonData, int index) {
        boolean tagConvert = radioButtonData.getTagConvert();
        CharArray  menu   = radioButtonData.getMenu().peek(index);

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
        ch.replace("<hr>","<hr />");
        ch.replace("<br>","<br />");
        return ch;
    }
}

//
//
// [end of RadioButtonRenderer.java]
//

