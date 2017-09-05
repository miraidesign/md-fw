//------------------------------------------------------------------------
//  ContentRenderer.java
//      テンプレート合成機能を持つレンダラー
//                 Copyright (c) MiraiDesign 2017 All Rights Reserved.
//------------------------------------------------------------------------
// あとで、interface Renderer のメソッドを利用するようにする

package com.miraidesign.servlet;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.ObjectQueue;

/**
 *  テンプレート合成機能を持つレンダラー
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ContentRenderer {
    private boolean debug = (false & SystemConst.debug);         // デバッグ表示
    /** デバッグ表示の有無を切り替える */
    public void setDebug(boolean mode) {debug = mode; }

    String szHtml5Header=""+
        "<!DOCTYPE html>\n"+
        "<html lang=\"@LANG\">\n"+
        "<head>\n"+
        "<meta charset=\"@CHAR_SET\" />\n"+
        "@{TITLE}"+
        "<meta http-equiv=\"Pragma\" content=\"no-cache\" />\n"+
        "<meta http-equiv=\"Cache-control\" content=\"no-cache\" />\n"+
        "<meta http-equiv=\"X-carrier\" content=\"HTML5\" />\n"+
        "@{META}"+
        "@{BASE}"+
        "@{LINK}"+
        "@{STYLE}"+
        "@{SCRIPT}"+
        "</head>\n";


    String szXhtmlHeader=""+
//IE6バグ対策        
        //"<?xml version=\"1.0\" encoding=\"@CHAR_SET\"?>\n"+
        //"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1 Transitional//EN\""+
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1 //EN\""+
        " \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\""+
        ">\n"+
        "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"ja\" lang=\"ja\">\n"+
        "<head>\n"+
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=@CHAR_SET\" />\n"+
        "@{TITLE}"+
        "<meta http-equiv=\"Pragma\" content=\"no-cache\" />\n"+
        "<meta http-equiv=\"Cache-control\" content=\"no-cache\" />\n"+
        "<meta http-equiv=\"X-carrier\" content=\"PC\" />\n"+
        "@{META}"+
        "@{BASE}"+
        "@{LINK}"+
        "@{STYLE}"+
        "@{SCRIPT}"+
        "</head>\n";

    String szHtmlHeader = ""+
        "<html>\n"+
        "<head>\n"+
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=@CHAR_SET\">\n"+
        "@{TITLE}"+
        "<meta http-equiv=\"Pragma\" content=\"no-cache\">\n"+
        "<meta http-equiv=\"Cache-control\" content=\"no-cache\">\n"+
        "<meta http-equiv=\"X-carrier\" content=\"PC\">\n"+
        "@{META}"+
        "@{BASE}"+
        "@{LINK}"+
        "@{STYLE}"+
        "</head>\n";
        

    String szBody = "<body @{BODY}>\n";
    String szFooter="</body>\n</html>\n";

    /**
        テンプレート合成モード用のデフォルトのXHTMLヘッダを設定する
    */
    public void setHtml5lHeader(String header) { szHtml5Header = header;}
    public void setDefaultXhtmlHeader(String header) { szXhtmlHeader = header;}
    /*
    public void setAuXhtmlHeader(String header) { szAuXhtmlHeader = header;}
    public void setSoftBankXhtmlHeader(String header) { szSoftBankXhtmlHeader = header;}
    public void setDocomoXhtmlHeader(String header) { szDocomoXhtmlHeader = header;}
    public void setEmobileXhtmlHeader(String header) { szEmobileXhtmlHeader = header;}
    public void setChtmlHeader(String header) { szChtmlHeader = header;}
    public void setMmlHeader(String header)   { szMmlHeader = header;}
    public void setHtmlHeader(String header)  { szHtmlHeader = header;}
    +.
    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------
    public ContentRenderer() {
        debug &= SystemConst.debug;
    }
    
    /**
        複合（合成）ページを表示するか？
        @param queue PageServlet#forward()の戻りqueue
        @return true: 合成表示を行う
    */
    public boolean isComplexPage(ObjectQueue queue) {
        boolean sts = false;
        do {
            if (queue == null || queue.size() == 0) {
                break;
            }
            PageServlet page0 = (PageServlet)queue.peek(0);
            if (page0 == null /* || page0.getName() == null*/) {
                break;
            }
            if ((page0.getName() == null || page0.getName().trim().length() == 0)
                && page0.getParser() != null) {
                break;
            }
            for (int i = 0; i < queue.size(); i++) {
                PageServlet page = (PageServlet)queue.peek(i);
                if (page != null && 
                    page.getName() != null &&
                    page.getName().length() > 0) {
                    sts = true;
                    break;
                }
            }
        } while (false);
        return sts;
    }
    
    /**
        ページごとのネームスペースを求める
        @param queue PageServlet#forward()の戻りqueue
        @return namespace queue
    */
    public CharArrayQueue getNamespaceQueue(ObjectQueue queue) {
        CharArrayQueue caQueue = new CharArrayQueue();
        if (queue.size() > 0) {
            PageServlet page = null;
            for (int i = 0; i < queue.size(); i++) {
                page = (PageServlet)queue.peek(i);
                if (page.getName().trim().length() == 0) {
                   caQueue.enqueue("");
                } else {
                   caQueue.enqueue(page.getModule().getName()+"."+page.getName());
                }
            }
            // 無指定のNameSpace()を直後のものに置き換えるモードにする
            String last = page.getName();
            for (int i = queue.size()-1; i >= 0 ; i--) {
                page = (PageServlet)queue.peek(i);
                if (page.getName().length() == 0) {
                    caQueue.poke(i, last);
                } else {
                    last = page.getName();
                }
            }
        }
        return caQueue;
    }
    
    /**
        ヘッダを取得します
    */
    public CharArray getHeader(SessionObject session) {
        return getHeader(session, null);
    }
    
    /**
        ヘッダを取得します
    */
    public CharArray getHeader(SessionObject session, CharArray buffer) {
        if (buffer == null) buffer = new CharArray();
if (debug) session.println("(H)PC["+session.isPC()+"] mobile["+session.isMobile()+
                              "]  Robot["+session.isRobot()+"]");
        buffer.add(szHtml5Header);  // 2010-08-09
        buffer.add(szBody);
        return buffer;
    }
    /**
        フッタを取得します
    */
    public CharArray getFooter(SessionObject session) {
        return getFooter(session, null);
    }
    /**
        フッタを取得します
    */
    public CharArray getFooter(SessionObject session,CharArray buffer) {
        if (buffer == null) buffer = new CharArray();
        
        buffer.add(szFooter);
        
        return buffer;
    }
}

//
// $Author$
// $Source$
// $Revision$
// $Date$
//
// $Log$
//
// [end of ContentRenderer.java]
//

