//------------------------------------------------------------------------
// @(#)ItemConstant.java
//                 Item 定数
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
package com.miraidesign.renderer.item;

/**
 *  Item 定数
 *
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public interface ItemConstant {
    // container group
    public final static int CONTAINER  = 0;
    public final static int FORM       = 1;     // <form>
    public final static int LINK       = 2;     // <A href=></a>
    public final static int HEADER     = 3;     // 
    public final static int FOOTER     = 4;     // 
    public final static int BLOCK      = 5;     // 左中右詰め
    public final static int FONT       = 6;

    // display group
    public final static int STRING     = 20;    // 文字列
    public final static int LINE_FEED  = 22;    // 改行 <br>
    public final static int HAIR_LINE  = 23;    // 線   <hr>
    public final static int ANCHOR_STRING = 24; // アンカー＋文字列
    public final static int IMG        = 25;    // IMGタグ
    public final static int ANCHOR_IMG = 26;    // アンカー＋IMG
    
    // input group
    public final static int TEXT       = 40;    // input type=text
    public final static int HIDDEN     = 41;
    public final static int PASSWORD   = 42;
    public final static int CHECKBOX   = 43;
    public final static int RADIO      = 44;
    public final static int SUBMIT     = 45;
    public final static int RESET      = 46;
    public final static int BUTTON     = 47;
    public final static int IMAGE      = 48;
    public final static int FILE      = 49;
    public final static int TEXTAREA   = 50;
    
    
    // button group
    public final static int BTN_BUTTON  = 60;
    public final static int BTN_SUBMIT  = 61;
    public final static int BTN_RESET   = 62;
    
    // select group
    public final static int LIST_BOX    = 80;
    //public final static int COMBO_BOX   = 80;
    
    // multiple data group
    public final static int DYNAMIC     = 100; 
    public final static int TABLE       = 200; 
    
    // non GUI group
    public final static int QUEUE       = 500; 
    public final static int QUEUE_TABLE = 501; 
    public final static int ELEMENT     = 502;
    public final static int PARAMETER   = 503;
    public final static int HASH        = 504;  // May 2012
    public final static int EVENT       = 510; 
    
    
    //public void setUserObject(Object userObject);
    //public Object getUserObject();
    
    //-----------------------------------------------------------------
    // align
    //-----------------------------------------------------------------
    // aline
    public final static int LEFT    = 1;    // img
    public final static int CENTER  = 2;
    public final static int RIGHT   = 3;    // img
    public final static int JUSTIFY = 4;
    public final static int CHAR    = 5;

    public final static String[] align = {"","left","center","right","justify","char",                                          "top","middle","bottom","baseline"};
    public final static String[] xhtml_align = {"",
                    "float:left","float:center","float:right","float:justify","char",                                      "vertical-align:top","vertical-align:middle",
                    "vertical-align:bottom","vertical-align:baseline"};
   // valine
    public final static int TOP      = 6;   // img
    public final static int MIDDLE   = 7;   // img
    public final static int BOTTOM   = 8;   // img
    public final static int BASELINE = 9;

    //public final static String[] valign = {"","top","middle","bottom","baseline"};
    
    //-----------------------------------------------------------------
    // istyle
    //-----------------------------------------------------------------
    public final static int HIRAGANA = 1;   // 全角ひらがな
    public final static int KATAKANA = 2;   // 半角カタカナ
    public final static int ALPHABET = 3;   // 英字
    public final static int NUMERIC  = 4;   // 数字

    public final static String[] mml_istyle = {"","hiragana","katakana","alphabet","numeric"};

    public final static String[] xhtml_istyle = {"",
                    "-wap-input-format:&quot;*&lt;ja:h&gt;&quot;",
                    "-wap-input-format:&quot;*&lt;ja:hk&gt;&quot;",
                    "-wap-input-format:&quot;*&lt;ja:en&gt;&quot;",
                    "-wap-input-format:&quot;*&lt;ja:n&gt;&quot;"
    };
}

//
// [end of ItemConstant.java]
//

