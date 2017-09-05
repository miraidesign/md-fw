//------------------------------------------------------------------------
// @(#)ListBoxRenderer.java
//                 
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.xhtml;

//import com.miraidesign.image.EmojiConverter;
import com.miraidesign.renderer.item.ListBoxData;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.BooleanQueue;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/**
 *  リストボックスのレンダリング（XHTML)を行う
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ListBoxRenderer /*implements ItemRenderer*/ {

    /**
        リストボックスを描画する
        @param ch   出力バッファ
        @param listBoxData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch, 
                                 ListBoxData listBoxData) {
        CharArrayQueue  menu   = listBoxData.getMenu();
        CharArrayQueue  option = listBoxData.getOption();
        BooleanQueue queue = listBoxData.getQueue();
        int     size      = listBoxData.getSize();
        boolean multiple  = listBoxData.isMultiple();
        boolean disabled  = listBoxData.isDisabled();
        ch.add("<select name=\"");
        ch.add(listBoxData.getItem().getName());
        ch.add(listBoxData.getName());  // Dynamic
        ch.add("\"");
        if (size > 0) {
            ch.add(" size=\"");
            ch.format(size);
            ch.add("\"");
        }
        if (multiple) ch.add(" multiple=\"multiple\"");
        if (disabled) ch.add(" disabled=\"disabled\"");
        ch.add(">\n");
        for (int i = 0; i < menu.size(); i++) {
            CharArray c = menu.peek(i);
            ch.add("  <option ");
            if (queue.peek(i)) ch.add("selected=\"selected\" ");
            if (option != null && option.size() > i) {
                CharArray o = option.peek(i);
                if (o != null && o.length() > 0) {
                    ch.add(" value=\"");
                    ch.add(o);
                    ch.add("\"");
                }
            }
            ch.add(">");
            SessionObject session = listBoxData.getSessionObject();
            if (session != null && session.isPC()) ch.add(CharArray.replaceTag(c));
            //else ch.add(CharArray.replaceTag(EmojiConverter.convFromText(c)));  // 絵文字対応
            else ch.add(CharArray.replaceTag(c));
            ch.add("</option>\n");
        }
        ch.add("</select>\n");
        return ch;
    }
    static public CharArray drawNameMenu(CharArray ch, 
                                 ListBoxData listBoxData) {
        drawName(ch,listBoxData);
        ch.add(">\n");
        drawMenu(ch,listBoxData);
        ch.add("<!-- --");
        return ch;
    }
    static public CharArray drawName(CharArray ch, 
                                 ListBoxData listBoxData) {
        int     size      = listBoxData.getSize();
        boolean multiple  = listBoxData.isMultiple();
        boolean disabled  = listBoxData.isDisabled();
        ch.add("name=\"");
        ch.add(listBoxData.getItem().getName());
        ch.add(listBoxData.getName());  // Dynamic
        ch.add("\"");
        if (size > 0) {
            ch.add(" size=\"");
            ch.format(size);
            ch.add("\"");
        }
        if (multiple) ch.add(" multiple=\"multiple\"");
        if (disabled) ch.add(" disabled=\"disabled\"");
        return ch;
    }
    static public CharArray drawMenu(CharArray ch, 
                                 ListBoxData listBoxData) {
        CharArrayQueue  menu   = listBoxData.getMenu();
        CharArrayQueue  option = listBoxData.getOption();
        BooleanQueue queue = listBoxData.getQueue();
        for (int i = 0; i < menu.size(); i++) {
            CharArray c = menu.peek(i);
            ch.add("  <option ");
            if (queue.peek(i)) ch.add("selected=\"selected\" ");
            if (option != null && option.size() > i) {
                CharArray o = option.peek(i);
                if (o != null && o.length() > 0) {
                    ch.add(" value=\""); 
                    ch.add(o);
                    ch.add("\"");
                }
            }
            ch.add(">");
            
            SessionObject session = listBoxData.getSessionObject();
            if (session != null && session.isPC()) ch.add(CharArray.replaceTag(c));
            //else ch.add(CharArray.replaceTag(EmojiConverter.convFromText(c)));  // 絵文字対応
            else ch.add(CharArray.replaceTag(c));
            
            ch.add("</option>\n");
        }
        return ch;
    }
    static public CharArray drawOption(CharArray ch, 
                                 ListBoxData listBoxData, int index) {
        CharArray   option = listBoxData.getOption().peek(index);
        boolean selected   = listBoxData.getQueue().peek(index);
        if (selected) ch.add("selected=\"selected\" ");
        if (option != null && option.length() > 0) {
            ch.add("value=\""); 
            ch.add(option);
            ch.add("\"");
        }
        return ch;
    }
    static public CharArray drawMenu(CharArray ch, 
                                 ListBoxData listBoxData, int index) {
        CharArray  menu   = listBoxData.getMenu().peek(index);
        ch.add(menu);
        return ch;
    }
}

//
// [end of ListBoxRenderer.java]
//

