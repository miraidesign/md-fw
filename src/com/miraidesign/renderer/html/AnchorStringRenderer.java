//------------------------------------------------------------------------
// @(#)AnchorStringRenderer.java
//                 
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.html;

import com.miraidesign.renderer.item.AnchorStringData;
import com.miraidesign.renderer.item.ItemConstant;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharToken;

/**
 *  アンカー付き文字列 のレンダリング（HTML)を行う
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class AnchorStringRenderer /*implements ItemRenderer*/ {
    static private boolean useAC = false;   //true; キャッシュ対策を行う
    static private int count = 0;
    /**
        文字列を描画する
        @param ch   出力バッファ
        @param anchorStringData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(CharArray ch, 
                                 AnchorStringData anchorStringData) {
        SessionObject session = anchorStringData.getSessionObject();
        int align = anchorStringData.getAlign();
        if (align > 0) {
            ch.add("<div");
            //if (session.isDocomo()) ch.add(" style=\"text-align:");
            ch.add(" align=\"");
            ch.add(ItemConstant.align[align]);
            ch.add("\">");
        }
        ch.add("<a");
        CharArray url = anchorStringData.getAnchorURL();
        CharArray label = anchorStringData.getLabel();
        if (url.length() > 0) {
            ch.add(" href=\"");
            ch.add(url);
            if (useAC) {
                if (++count > 100000) count = 1;
                int magic = (int)(System.currentTimeMillis() & 0xffffff)+count;
                ch.add((url.indexOf('?') >= 0) ? "&" : "?");
                ch.add("AC="); ch.format(magic,36);
            }
            if (label.length() > 0) {
                ch.add('#');
                ch.add(label);
            }
            ch.add("\"");
        }
        char c = anchorStringData.getAccessKey();
        if (c > 0) {
            ch.add(" accesskey=\"");
            ch.add(c);
            ch.add("\"");
        }
        CharArray target = anchorStringData.getTarget();
        if (target.length()> 0) {
            ch.add(" target=\"");
            ch.add(target);
            ch.add("\"");
        }
        //if (anchorStringData.getUtn() && session.isDocomo()) {
        //    ch.add(" utn=\"utn\"");
        //}
        ch.add(">");
        
        boolean useFont = anchorStringData.useFont();
        if (anchorStringData.isBold()) ch.add("<b>");
        if (anchorStringData.isItalic()) ch.add("<i>");
        if (useFont) {
            ch.add("<font");
            anchorStringData.getStyleTag(ch); // スタイルシート参照を追加
            CharArray color = anchorStringData.getColor();
            if (color.length() > 0) {
                //if (session.isDocomo()) ch.add(" style=\"color:");
                ch.add(" color=\"");
                ch.add(color);
                ch.add("\"");
            }
            CharArray size = anchorStringData.getSize().trim();
            if (size.length() > 0) {
                /*
                if (session.isDocomo()) {
                    int i = size.getInt();
                    
                    ch.add(" style=\"font-size:");
                    if      (size.equals("+1")) ch.add("larger");
                    else if (size.equals("-1")) ch.add("smaller");
                    else if (i == 0)            ch.add("xx-small");
                    else if (i <= 1)            ch.add("x-small");
                    else if (i <= 2)            ch.add("small");
                    else if (i >= 6)            ch.add("xx-large");
                    else if (i >= 5)            ch.add("x-large");
                    else if (i >= 4)            ch.add("large");
                    else                        ch.add("medium");
                */
                ch.add(" size=\"");
                ch.add(size);
                ch.add("\"");
            }
            ch.add(">");
        }
        CharArray ca = anchorStringData.getText();
        if (ca.indexOf("\n") < 0) { 
            ch.add(ca);
        } else {
            CharToken token = CharToken.pop();
            token.set(ca,"\n");
            for (int i = 0; i < token.size(); i++) {
                ch.add(token.get(i));
                if (i + 1 < token.size()) {
                    ch.add("<br />\n");
                }
            }
            CharToken.push(token);
        }
        if (useFont) ch.add("</font>");
        if (anchorStringData.isItalic()) ch.add("</i>");
        if (anchorStringData.isBold()) ch.add("</b>");
        ch.add("</a>");
        if (align > 0) ch.add("</div>\n");
        //ch.replace("<hr>","<hr />");
        //ch.replace("<br>","<br />");
        return ch;
    }

    static public CharArray drawSrc(CharArray ch, 
                                 AnchorStringData anchorStringData) {
        CharArray url = anchorStringData.getAnchorURL();
        CharArray label = anchorStringData.getLabel();
        if (url.length() > 0) {
            ch.add("href=\"");
            ch.add(url);
            if (useAC) {
                if (++count > 100000) count = 1;
                int magic = (int)(System.currentTimeMillis() & 0xffffff)+count;
                ch.add((url.indexOf('?') >= 0) ? "&" : "?");
                ch.add("AC="); ch.format(magic,36);
            }
            if (label.length() > 0) {
                ch.add('#');
                ch.add(label);
            }
            ch.add("\"");
        }
        char c = anchorStringData.getAccessKey();
        if (c > 0) {
            ch.add(" accesskey=\"");
            ch.add(c);
            ch.add("\"");
        }
        CharArray target = anchorStringData.getTarget();
        if (target.length()> 0) {
            ch.add(" target=\"");
            ch.add(target);
            ch.add("\"");
        }
        //if (anchorStringData.getUtn() && anchorStringData.getSessionObject().isDocomo()) {
        //    ch.add(" utn=\"utn\"");
        //}
        return ch;
    }
    static public CharArray drawMenu(CharArray ch, 
                                 AnchorStringData anchorStringData) {
        boolean useFont = anchorStringData.useFont();
        //int align = anchorStringData.getAlign();
        //if (align > 0) {
        //    ch.add("<div");
        //    ch.add(" align=\"");
        //    ch.add(ItemConstant.align[align]);
        //    ch.add("\">");
        //}
        if (anchorStringData.isBold()) ch.add("<b>");
        if (anchorStringData.isItalic()) ch.add("<i>");
        if (useFont) {
            SessionObject session = anchorStringData.getSessionObject();
            ch.add("<font");
            anchorStringData.getStyleTag(ch); // スタイルシート参照を追加
            CharArray color = anchorStringData.getColor();
            if (color.length() > 0) {
                //if (session.isDocomo()) ch.add(" style=\"color:");
                ch.add(" color=\"");
                ch.add(color);
                ch.add("\"");
            }
            CharArray size = anchorStringData.getSize().trim();
            if (size.length() > 0) {
                /*
                if (session.isDocomo()) {
                    int i = size.getInt();
                    
                    ch.add(" style=\"font-size:");
                    if      (size.equals("+1")) ch.add("larger");
                    else if (size.equals("-1")) ch.add("smaller");
                    else if (i == 0)            ch.add("xx-small");
                    else if (i <= 1)            ch.add("x-small");
                    else if (i <= 2)            ch.add("small");
                    else if (i >= 6)            ch.add("xx-large");
                    else if (i >= 5)            ch.add("x-large");
                    else if (i >= 4)            ch.add("large");
                    else                        ch.add("medium");
                } else {
                */
                ch.add(" size=\"");
                ch.add(size);
                ch.add("\"");
            }
            ch.add(">");
        }
        CharArray ca = anchorStringData.getText();
        if (ca.indexOf("\n") < 0) { 
            ch.add(ca);
        } else {
            CharToken token = CharToken.pop();
            token.set(ca,"\n");
            for (int i = 0; i < token.size(); i++) {
                ch.add(token.get(i));
                if (i + 1 < token.size()) {
                    ch.add("<br />\n");
                }
            }
            CharToken.push(token);
        }
        if (useFont) ch.add("</font>");
        if (anchorStringData.isItalic()) ch.add("</i>");
        if (anchorStringData.isBold()) ch.add("</b>");
        //if (align > 0) ch.add("</div>\n");
        return ch;
    }
}

//
//
// [end of AnchorStringRenderer.java]
//

