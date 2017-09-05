//------------------------------------------------------------------------
// @(#)StringRenderer.java
//                 
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.renderer.html;

import com.miraidesign.renderer.item.StringData;
import com.miraidesign.renderer.item.ItemConstant;
import com.miraidesign.session.SessionObject;
import com.miraidesign.session.UserAgent;
import com.miraidesign.util.CharArray;

/**
 *  文字列 のレンダリング（HTML)を行う
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/
public class StringRenderer /*implements ItemRenderer*/ {

    /**
        文字列を描画する
        @param session SessionObject
        @param ch  データ出力先
        @param stringData データの保管元
        @return 一時出力先(CharArray)
    */
    static public CharArray draw(SessionObject session,
                                 CharArray ch, 
                                 StringData stringData) {
        boolean useFont = stringData.useFont();
        int align = stringData.getAlign();
        if (align > 0) {
            ch.add("<div");
            //if (session.isDocomo()) ch.add(" style=\"text-align:");
            ch.add(" align=\"");
            ch.add(ItemConstant.align[align]);
            ch.add("\">");
        }
        if (stringData.isBold()) ch.add("<b>");
        if (stringData.isItalic()) ch.add("<i>");
        if (useFont) {
            ch.add("<font");
            stringData.getStyleTag(ch); // スタイルシート参照を追加
            CharArray color = stringData.getColor();
            if (color.length() > 0) {
                //if (session.isDocomo()) ch.add(" style=\"color:");
                ch.add(" color=\"");
                ch.add(color);
                ch.add("\"");
            }
            CharArray size = stringData.getSize().trim();
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

        boolean marquee    = stringData.isMarquee();
        if (marquee) {
            //if (session.isDocomo()) ch.add("<div style=\"display:-wap-marquee\">");
            ch.add("<marquee>");
        }

        boolean tagConvert = stringData.getTagConvert();
        CharArray ca = tagConvert ? CharArray.replaceTag(stringData.getText()) :stringData.getText();
        boolean wrap = stringData.isWordWrap();
        int c_width = 0;
        
        if (wrap) { // c_widthはwrap以外は0
            UserAgent ua = session.userAgent;
            if (ua.isPC()) {
                c_width = stringData.getWrapWidth();
            } else {
                c_width = ua.c_width;
            }
        }
        ch.add(CharArray.wordWrap(ca, stringData.getWrapOffset(),c_width,"<br />\n"));

        if (marquee) {
            //if (session.isDocomo()) ch.add("</div>");
            ch.add("</marquee>");
        }
        if (useFont) ch.add("</font>");
        if (stringData.isItalic()) ch.add("</i>");
        if (stringData.isBold()) ch.add("</b>");
        if (align > 0) ch.add("</div>\n");
        
        //ch.replace("<hr>","<hr />");
        //ch.replace("<br>","<br />"); //
        return ch;
    }
}

//
//
// [end of StringRenderer.java]
//

