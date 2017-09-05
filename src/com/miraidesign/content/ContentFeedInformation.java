//------------------------------------------------------------------------
// @(#)ContentFeedInformation.java
//         フィードのためのコンテント情報を保管する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.Queue;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.QueueTable;

/** (RSS)フィードのためのコンテント情報を保管する */
public class ContentFeedInformation {
    private static boolean debug = (SystemConst.debug && true);
    
    //static public int RSS_FEED    = 0;      // RSS 1.0
    //static public int ATOM_FEED   = 10;     // ATOM (未対応)
    //private int type = RSS_FEED;
    
    private FeedRenderer feedRenderer;
    private String filename = "feed.rss";
    
    private CharArray title       = new CharArray();
    private CharArray link        = new CharArray();
    private CharArray description = new CharArray();
    private CharArray image       = new CharArray();
    private CharArray path        = new CharArray();
    private CharArray lang        = new CharArray("ja");
    private long publishDate = 0;
    private CharArray rights      = new CharArray();
    private CharArray publisher    = new CharArray();
    
    /** ファイル名を設定する
        @param filename ファイル名
    */
    public void setFilename(String filename) { this.filename = filename;}
    /** ファイルまでのパスを設定する
        @param path ファイルパス
    */
    public void setPath(CharArray path) { this.path.set(path);}
    public void setPath(String path) { this.path.set(path);}
    /** チャネルタイトルを設定する
        @param title タイトル
    */
    public void setTitle(CharArray title) { this.title.set(title);}
    public void setTitle(String title) { this.title.set(title);}
    /** サイトURLを設定する
        @param link サイトURL
    */
    public void setLink(CharArray link) { this.link.set(link);}
    public void setLink(String link) { this.link.set(link);}
    /** チャネル概要を設定する
        @param description 概要
    */
    public void setDescription(CharArray description) { 
        this.description.set(description);
    }
    public void setDescription(String description) { 
        this.description.set(description);
    }
    /** 画像を設定する
        @param path 画像パス
    */
    public void setImage(CharArray path) { this.image.set(path);}
    public void setImage(String path) { this.image.set(path);}
    /** LANG設定する default:ja
        @param lang 言語
    */
    public void setLang(CharArray lang) { 
        this.lang.set(lang);
    }
    public void setLang(String lang) { 
        this.lang.set(lang);
    }
    /** 配信日時を指定する 
        @param l 配信日時
    */
    public void setPublishDate(long l) {
        publishDate = l;
    }
    
    /** Copyrightを設定する 
        @param s copyright
    */
    public void setRights(String s) { this.rights.set(s);}
    public void setRights(CharArray s) { this.rights.set(s);}
    
    /** publisherを設定する 
        @param s publisher
    */
    public void setPublisher(String s)   { this.publisher.set(s);}
    public void setPublisher(CharArray s) { this.publisher.set(s);}
    
    /** ファイル名を取得する
        @return ファイル名
    */
    public String getFilename() { return filename; }
    /** ファイルまでのパスを取得する
        @return ファイルパス
    */
    public CharArray getPath() { return path; }
    /** FEEDファイルのURLを取得する 
        @return feed URL
    */
    public CharArray getUrl() {
        CharArray ch = new CharArray(path);
        if (!ch.endsWith("/")) ch.add("/");
        ch.add(filename);
        return ch;
    }
    
    /** チャネルタイトルを取得する
        @return チャネルタイトル
    */
    public CharArray getTitle() { return title; }
    /** サイトURLを取得する
        @return サイトURL
    */
    public CharArray getLink() { return link; }
    /** チャネル概要を取得する
        @return チャネル概要
    */
    public CharArray getDescription() { return description; }
    
    /* langを取得する （default:ja) */
    public CharArray getLang() { return lang;}
    
    /* 配信日時を取得する */
    public long getPublishDate() { return publishDate;}

    /* Copyrightを取得する */
    public CharArray getRights() { return rights;}

    /* Publisherを取得する */
    public CharArray getPublisher() { return publisher;}
    
    //---------------------------------------------------------------
    // アイテムテーブル
    //---------------------------------------------------------------
    protected QueueTable itemTable = new QueueTable() {{
        setTitle("FEED_ITEMS");
        addColumn(Queue.STRING, "title");       // タイトル
        addColumn(Queue.STRING, "link");        // リンク
        addColumn(Queue.STRING, "description"); // 概要
        addColumn(Queue.STRING, "content");     // コンテント
        addColumn(Queue.STRING, "creator");     // 作者
        addColumn(Queue.STRING, "category");    // カテゴリ
        addColumn(Queue.DATE,   "date");        // 作成日時
                                                // 
        addColumn(Queue.STRING, "keywords");    // キーワードリスト(複数は,でつなげる)
        addColumn(Queue.INT,    "duration");    // 放映時間（秒）
    }};
    /*
        アイテムテーブルを取得する
    */
    public QueueTable getItemTable() { return itemTable; }
    
    /** itemの追加
        @param title タイトル
        @param link  リンク
        @param description 概要
        @param creator 作者
        @param category カテゴリ
        @param date    更新日時
    */
    public synchronized void addItem(CharArray title, CharArray link, 
                        CharArray description, CharArray creator,
                        CharArray category, long date) {
        int size = itemTable.getRowCount();
        itemTable.addRow();
        itemTable.setCharArray(title, size, "title");
        itemTable.setCharArray(link, size, "link");
        itemTable.setCharArray(description, size, "description");
        itemTable.setCharArray(creator, size, "creator");
        itemTable.setCharArray(category, size, "category");
        itemTable.setLong(date, size, "date");
    }
    public synchronized void addItem(String title, String link, 
                        String description, String creator,
                        String category, long date) {
        int size = itemTable.getRowCount();
        itemTable.addRow();
        itemTable.setString(title, size, "title");
        itemTable.setString(link, size, "link");
        itemTable.setString(description, size, "description");
        itemTable.setString(creator, size, "creator");
        itemTable.setString(category, size, "category");
        itemTable.setLong(date, size, "date");
    }
    /** itemの追加
        @param title タイトル
        @param link  リンク
        @param description 概要
        @param content コンテント
        @param creator 作者
        @param category カテゴリ
        @param date    更新日時
    */
    public synchronized void addItem(CharArray title, CharArray link, 
                        CharArray description, CharArray content,CharArray creator,
                        CharArray category, long date) {
        int size = itemTable.getRowCount();
        itemTable.addRow();
        itemTable.setCharArray(title, size, "title");
        itemTable.setCharArray(link, size, "link");
        itemTable.setCharArray(description, size, "description");
        itemTable.setCharArray(content, size, "content");
        itemTable.setCharArray(creator, size, "creator");
        itemTable.setCharArray(category, size, "category");
        itemTable.setLong(date, size, "date");
    }
    public synchronized void addItem(String title, String link, 
                        String description, String content, String creator,
                        String category, long date) {
        int size = itemTable.getRowCount();
        itemTable.addRow();
        itemTable.setString(title, size, "title");
        itemTable.setString(link, size, "link");
        itemTable.setString(description, size, "description");
        itemTable.setString(content, size, "content");
        itemTable.setString(creator, size, "creator");
        itemTable.setString(category, size, "category");
        itemTable.setLong(date, size, "date");
    }
    /** itemの追加
        @param title       タイトル
        @param link        リンク
        @param description 概要
        @param content  コンテント
        @param creator  作者
        @param category カテゴリ
        @param date     更新日時
        @param keys     キーワードリスト（カンマ区切り)
        @param duration 放映時間(秒指定)
    */
    public synchronized void addItem(CharArray title, CharArray link, 
                        CharArray description, CharArray content,CharArray creator,
                        CharArray category, long date,
                        CharArray keys, int duration) {
        int size = itemTable.getRowCount();
        itemTable.addRow();
        itemTable.setCharArray(title, size, "title");
        itemTable.setCharArray(link, size, "link");
        itemTable.setCharArray(description, size, "description");
        itemTable.setCharArray(content, size, "content");
        itemTable.setCharArray(creator, size, "creator");
        itemTable.setCharArray(category, size, "category");
        itemTable.setLong(date, size, "date");
        itemTable.setCharArray(keys, size, "keywords");
        itemTable.setInt(duration, size, "duration");
    }
    /** itemの追加
        @param title       タイトル
        @param link        リンク
        @param description 概要
        @param content  コンテント
        @param creator  作者
        @param category カテゴリ
        @param date     更新日時
        @param keys     キーワードリスト
        @param duration 放映時間(秒指定)
    */
    public synchronized void addItem(CharArray title, CharArray link, 
                        CharArray description, CharArray content,CharArray creator,
                        CharArray category, long date,
                        CharArrayQueue keys, int duration) {
        int size = itemTable.getRowCount();
        itemTable.addRow();
        itemTable.setCharArray(title, size, "title");
        itemTable.setCharArray(link, size, "link");
        itemTable.setCharArray(description, size, "description");
        itemTable.setCharArray(content, size, "content");
        itemTable.setCharArray(creator, size, "creator");
        itemTable.setCharArray(category, size, "category");
        itemTable.setLong(date, size, "date");
        CharArray ch = new CharArray();
        if (keys != null) {
            for (int i = 0; i < keys.size(); i++) {
                CharArray tmp = keys.peek(i);
                if (tmp == null || tmp.trim().length()==0) continue;
                if (ch.length() > 0) ch.add(" ");
                ch.add(tmp);
            }
        }
        itemTable.setCharArray(ch, size, "keywords");
        itemTable.setInt(duration, size, "duration");
    }
    public synchronized void addItem(String title, String link, 
                        String description, String content, String creator,
                        String category, long date,
                        String[] keys, int duration) {
        int size = itemTable.getRowCount();
        itemTable.addRow();
        itemTable.setString(title, size, "title");
        itemTable.setString(link, size, "link");
        itemTable.setString(description, size, "description");
        itemTable.setString(content, size, "content");
        itemTable.setString(creator, size, "creator");
        itemTable.setString(category, size, "category");
        itemTable.setLong(date, size, "date");
        CharArray ch = new CharArray();
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                String tmp = keys[i];
                if (tmp == null || tmp.trim().length()==0) continue;
                if (ch.length() > 0) ch.add(" ");
                ch.add(tmp.trim());
            }
        }
        itemTable.setCharArray(ch, size, "keywords");
        itemTable.setInt(duration, size, "duration");
    }
    
    //-----------------------------------------------------------------
    // constructor
    //-----------------------------------------------------------------
    public ContentFeedInformation() {
        feedRenderer = RssRenderer.getInstance();    // デフォルトレンダラーの設定
    }
    //-----------------------------------------------------------------
    // method
    //-----------------------------------------------------------------
    /* レンダラーをセットする default:RssRenderer */
    public void setFeedRenderer(FeedRenderer renderer) {
        feedRenderer = renderer;
    }
    
    /** コンテンツ情報をすべてクリアする */
    public void clear() {
        title.clear();
        link.clear();
        description.clear();
        image.clear();
        path.clear();
        itemTable.clear();
    }
    
    public void copy(ContentFeedInformation from) {
        this.feedRenderer = from.feedRenderer;
        this.title.set(from.title);
        this.link.set(from.link);
        this.description.set(from.description);
        this.image.set(from.image);

        this.itemTable.copy(from.itemTable);
    }

    //------------------------------------------------------------------
    // 設定
    //------------------------------------------------------------------
    //タイプ設定
    //public void setType(int type) { this.type = type; } 
    
    //------------------------------------------------------------------
    // 描画
    //------------------------------------------------------------------
    /* 描画する */
    public CharArray draw() {
        return feedRenderer.draw(this);
    }

}

//
// [ContentFeedInformation.java]
//
