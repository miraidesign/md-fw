//------------------------------------------------------------------------
// @(#)RssRenderer.java
//                 
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import java.text.SimpleDateFormat;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.QueueTable;

/**
 *  RSS 1.0でのレンダリングを行います
 *  
 *  @version 0.6 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class RssRenderer implements FeedRenderer {
    private static RssRenderer instance = null;
    private static boolean convertLineFeed = true; // 改行をコンバートするか
                                                   // 通常:true PDNモード:false
    static public FeedRenderer getInstance() {
        if (instance == null) instance = new RssRenderer();
        return instance;
    }
    
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static SimpleDateFormat sdf3 = new SimpleDateFormat("mm:ss");
    
    //---------------------------
    private RssRenderer() {}
    //---------------------------

    /** RSS 描画メソッド */
    public CharArray draw(ContentFeedInformation info) {
        CharArray ch = new CharArray();
        if (info != null) {
            CharArray lang = info.getLang();
            ch.add("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"); // なくても動作する
            ch.add("<rdf:RDF\n"+
                   "  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
                   "  xmlns=\"http://purl.org/rss/1.0/\"\n"+
                   "  xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"+
                   "  xmlns:sy=\"http://purl.org/rss/1.0/modules/syndication/\"\n"+
                   "  xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"\n"+
                   "  xmlns:itunes=\"http://www.itunes.com/dtds/podcast-1.0.dtd\" version=\"2.0\"\n");


            if (lang.length() > 0) {
                ch.add("  xml:lang=\"");ch.add(lang);ch.add("\"");
            }
            ch.add(">\n");
            ch.add("\n");
            drawChannel(info, ch);
            //drawImage(info, ch);
            drawItem(info, ch);
            ch.add("\n");
            ch.add("</rdf:RDF>\n");
        }
        return ch;
    }
    
    private CharArray drawChannel(ContentFeedInformation info, CharArray ch) {
        CharArray filepath = info.getPath();
        CharArray lang = info.getLang();
        long      time = info.getPublishDate();
        CharArray rights    = info.getRights();
        CharArray publisher = info.getPublisher();
        String szDate;
        
        ch.add("  <channel rdf:about=\"");ch.add(filepath);
        if (!filepath.endsWith("/")) ch.add("/");
        ch.add(info.getFilename());
        ch.add("\">\n");
        
        ch.add("    <title>");
        ch.add(CharArray.replaceTag(info.getTitle()));
        ch.add("</title>\n");
        
        ch.add("    <link>");ch.add(info.getLink());ch.add("</link>\n");
        
        CharArray description = new CharArray(info.getDescription());
        
        if (description.indexOf("<![CDATA[") < 0) {
            description.replace("\r","\n");
            if (convertLineFeed) description.replace("\n","<br>\n");
            description.replaceTag();
        }
        ch.add("    <description>");ch.add(description);ch.add("</description>\n");
        
        // image?
        
        if (lang.length() > 0) {
            ch.add("    <dc:language>");ch.add(lang);ch.add("</dc:language>\n");
        }
        if (rights.length() > 0) {
            ch.add("    <dc:rights>");ch.add(rights);ch.add("</dc:rights>\n");
        }
        if (publisher.length() > 0) {
            ch.add("    <dc:publisher>");ch.add(publisher);ch.add("</dc:publisher>\n");
        }
        
        if (time > 0) {
            synchronized (sdf2) {
                szDate = sdf2.format(new java.util.Date(time));
            }
            ch.add("    <dc:date>");ch.add(szDate);ch.add("+09:00");ch.add("</dc:date>\n");
        }
        
        drawItems(info, ch);
        
        ch.add("  </channel>\n");
        return ch;
    }
    
    private CharArray drawItems(ContentFeedInformation info, CharArray ch) {
        QueueTable table = info.getItemTable();
        ch.add("    <items>\n");
        ch.add("      <rdf:Seq>\n");
        if (table != null) {
            for (int j = 0; j < table.getRowCount(); j++) {
                CharArray link  = table.get(j,"link");
                ch.add("        <rdf:li rdf:resource=\"");
                ch.add(link);
                ch.add("\" />\n");
            }
        }
        ch.add("      </rdf:Seq>\n");
        ch.add("    </items>\n");
        return ch;
    }
    private CharArray drawItem(ContentFeedInformation info, CharArray ch) {
        QueueTable table = info.getItemTable();
        if (table != null) {
            for (int j = 0; j < table.getRowCount(); j++) {
                CharArray title = CharArray.replaceTag(table.get(j,"title"));
                CharArray link  = table.get(j,"link");
                CharArray description = new CharArray(table.get(j,"description"));
                
                if (description.indexOf("<![CDATA[") < 0) {
                    description.replace("\r","\n");
                    if (convertLineFeed) description.replace("\n","<br>\n");
                    description.replaceTag();
                }
                CharArray content     = new CharArray(table.get(j,"content"));
                CharArray creator     = CharArray.replaceTag(table.get(j,"creator"));
                CharArray category    = CharArray.replaceTag(table.get(j,"category"));
                long      ldate       = table.getLong(j,"date");
                String    szDate;
                synchronized (sdf2) {
                    szDate = sdf2.format(new java.util.Date(ldate));
                }
                // 
                CharArray key         = CharArray.replaceTag(table.get(j,"keywords"));
                //key.replace(",","&#x2C;");  // カンマを変換
                //key.replace(" ",", ");      // スペースをカンマ区切りに変換

                long t = table.getLong(j,"duration") * 1000;
                String szDuration;
                synchronized (sdf3) {
                    szDuration =sdf3.format(new java.util.Date(t));
                }

                //ch.add("    <item>\n");
                ch.add("  <item rdf:about=\"");ch.add(link);ch.add("\">\n");
                ch.add("    <title>");ch.add(title);ch.add("</title>\n");
                ch.add("    <link>");ch.add(link);ch.add("</link>\n");
                ch.add("    <description>");ch.add(description);ch.add("</description>\n");
                if (content.length() > 0) {
                    ch.add("    <content:encoded>");ch.add(content);ch.add("</content:encoded>\n");
                }
                ch.add("    <dc:creator>");ch.add(creator);ch.add("</dc:creator>\n");
                ch.add("    <dc:subject>");ch.add(category);ch.add("</dc:subject>\n");
                ch.add("    <dc:date>");ch.add(szDate);ch.add("+09:00");ch.add("</dc:date>\n");
                if (key.length() > 0) {
                    ch.add("    <itunes:key>"); ch.add(key); ch.add("</itunes:key>\n");
                }
                if (t > 0) {
                    ch.add("    <itunes:duration>"); ch.add(szDuration); ch.add("</itunes:duration>\n");
                }
                
                ch.add("  </item>\n");
            } // next
        }
        return ch;
    }
    
    /**
        CDATA セクション以外のタグ文字列をコンバートする<br>
        オリジナルは変更しない<br>
        CDATAセクションは１個のみ対応→これでは意味がない、、
        @param org 元の文字列
        @return 変換後の文字列(
    **/
    static private CharArray replaceTag(CharArray org) {
        if (org == null || org.length==0) return org;
        CharArray ch = CharArray.pop();
        do {
            int index1 = ch.indexOf("<![CDATA[");
            if (index1 < 0) break;
            int index2 = ch.indexOf("]]>", index1);
            
            //
            // 未作成
            //
            
        } while (false);
        ch.replaceTag();
        return ch;
    }
}

//
// [RssRenderer.java]
//
