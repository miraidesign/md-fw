//------------------------------------------------------------------------
//    IniFile.java                                                        
//    プロファイルファイル（.ini）読込みクラス
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-04   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
package com.miraidesign.util;

import java.io.InputStream;
import java.util.Enumeration;
import java.net.Socket;

/**
 *    プロファイルファイル（.ini）をHashVectorで管理する
 */
public class IniFile extends QueueFile {
    static final boolean debug = false;
    
    private String  szSection   = "[";      // セクションスタート文字列
    private String  szSeparator = "=";      // セパレーター文字列
    private String szComment    = ";";      // コメント開始文字列
    private String szTerminator = "[End]";  // この文字列のセクションで終了
    
    private boolean trimming = true;       // データをトリミングするか 
    
    private boolean isOK = true;

    private boolean checkMulti = false;  // キーの多重登録をチェックする
    public void checkMultiKey(boolean mode) { checkMulti = mode;}

    /** key:セクション名 value:データハッシュテーブル */
    private HashVector<CharArray,HashVector<CharArray,CharArrayQueue>> hashSection = 
        new HashVector<CharArray,HashVector<CharArray,CharArrayQueue>>();

    /** 指定ファイルの IniFileを生成する */
    public IniFile(String szFileName) {
        super(szFileName);
    }
    /** 指定文字列から IniFileを生成する */
    public IniFile(String[] strs) {
        super(strs);
    }

    /** 指定ソケット入力から IniFileを生成する */
    public IniFile(Socket socket) {
        super(socket);
    }
   
    /** 指定ストリーム入力から IniFileを生成する */
    public IniFile(InputStream stream) {
        super(stream);
    }

    /** 指定ファイルの IniFileを生成する */
    public IniFile(String szFileName, String szSeparator) {
        super(szFileName);
        this.szSeparator = szSeparator;
    }
    /** 指定ファイルの IniFileを生成する */
    public IniFile(String szFileName, String separator, String comment,String connector) {
        super(szFileName);
        setSeparator(separator);
        setComment(comment);
        setConnector(connector);
    }
    /** コンストラクタ
        @param szFileName   ファイル名
        @param separator    セパレータ
        @param comment      コメント文字
        @param connector    行結合文字
        @param nlconnector  \n変換＋行結合
    
    */
    public IniFile(String szFileName, String separator, String comment,String connector, String nlconnector) {
        super(szFileName);
        setSeparator(separator);
        setComment(comment);
        setConnector(connector);
        setNLConnector(nlconnector);
    }

    public IniFile(String[] strs, String szSeparator) {
        super(strs);
        this.szSeparator = szSeparator;
    }

    public IniFile(InputStream stream, String separator, String comment,String connector) {
        super(stream);
        setSeparator(separator);
        setComment(comment);
        setConnector(connector);
    }
    public IniFile(InputStream stream, String separator, String comment,String connector,String nlconnector) {
        super(stream);
        setSeparator(separator);
        setComment(comment);
        setConnector(connector);
        setNLConnector(nlconnector);
    }

    /** セパレータ文字を設定する （デフォルト:"="）**/
    public void setSeparator(String str) { 
        if (str != null && str.length() > 0) szSeparator = str;
    }
    /** コメント文字を設定する （デフォルト:";"）**/
    public void setComment(String str) { 
        if (str != null && str.length() > 0) szComment = str;
    }
    /** セクションと認識する最初の文字列を設定する （デフォルト:"["）*/
    public void setSection(String str) { 
        if (str != null && str.length() > 0) szSection = str;
    }
    /** ターミネート文字列を設定する （デフォルト:"[End]"）*/
    public void setTerminator(String str) { 
        if (str != null) szTerminator = str;
    }
    /** データをトリミングするか （デフォルト:true）*/
    public void setTrimming(boolean mode) {
        this.trimming = mode;
    }
    /** ファイルが正常に読み込めたか？ */
    public boolean isOK() { return isOK;}

    /** データ読み込みを実装するメソッドを定義する */
    public boolean read() { 
        isOK = super.read();
        if (debug) System.out.println("read status="+isOK);
        return parse();
    }
    
    /** ファイルの読み込む
        @param doParse  true パースも行う
    */
    public boolean read(boolean doParse) { 
        isOK = super.read();
        if (debug) System.out.println("read status="+isOK);
        if (doParse) return parse();
        else return isOK;
    }
    private static CharArray nullCharArray = new CharArray();   // null key用

    public boolean parse() { 
        hashSection.clear();
        if (isOK) {
            CharArray section = CharArray.pop();
            CharArray data = CharArray.pop();
        if (debug) System.out.println("size="+size());
            for (int j = 0; j < size(); j++) {
                CharArray ch = get(j).trimL();      // １行づつ取り出す
                if (szTerminator.length() > 0 && ch.startsWith(szTerminator)) break;    // 終了
                if (ch.startsWith(szComment)) continue;  // コメントは無視
                if (ch.startsWith(szSection)) { // セクション文字列の場合
                    int index = ch.indexOf(szComment);
                    boolean convert = false;
                    while (index >= 0) {
                        if (ch.chars[index-1] == '\\') {    // エスケープ文字
                            // do nothing
                            index = ch.indexOf(szComment, index+1);
                            convert = true;
                        } else {
                            ch.length = index;    // コメントを切り取る
                            break;
                        }
                    }
                    ch.trimR();
                    if (convert) ch.replace("\\"+szComment,szComment);
                    section.set(ch);            // セクションの設定
                    
                    if (!hashSection.containsKey(section)) {    // セクションが登録されていなければ
                        // そのセクション用の HashVectorを登録しておく
                        hashSection.put(new CharArray(section), new HashVector<CharArray,CharArrayQueue>());
                    }
                } else {
                    if (section.length() > 0) { // セクションが存在すれば
                        int index = ch.indexOf(szComment);
                        boolean convert = false;
                        while (index >= 0) {
                            if (index > 0 && ch.chars[index-1] == '\\') {    // エスケープ文字
                                // do nothing
                                index = ch.indexOf(szComment, index+1);
                                convert = true;
                            } else {
                                ch.length = index;    // コメントを切り取る
                                break;
                            }
                        }
                        if (convert) ch.replace("\\"+szComment,szComment);
                        
                        index = ch.indexOf(szSeparator);
                        data.set(ch).trim();
                        if (ch.length() > 0) {
                        //if (index >= 0) {    // セパレータが存在する    
                                    // ない時は空白キーに登録する
                            //CharArray key = new CharArray(ch.chars, 0, index);
                            CharArray key = (index >= 0) ? new CharArray(ch.chars, 0, index) : nullCharArray;
                            key.trimR();
                            CharArray value = new CharArray();
                            int size = szSeparator.length();
                            value.set(ch.chars, index+size, ch.length()-index-size);
                            HashVector<CharArray,CharArrayQueue> hash = hashSection.get(section);
                            if (hash != null) {
                                if (trimming) value.trim();
                                if (debug) System.out.println("IniFile.read() "+section+" put("+key+","+value+")");
                                CharArrayQueue queue = (CharArrayQueue)hash.get(key);
                                if (queue == null) {    // キーが存在しない
                                    queue = new CharArrayQueue();
                                    hash.put(key,queue);
                                } else if (checkMulti) {
                                    System.out.println("●●IniFile●●重複キー["+key+"]があります");
                                }
                                queue.enqueue(value);
                            } else {
                                System.out.println("IniFile.read() system error !!");
                            }
                        //} else {  // ない時は空白キーに登録する
                        }
                    } else {
                        //if (debug) System.out.println("セクション"+section+"のサイズが０です");
                    }
                } // endif
            } // next
            CharArray.push(data);
            CharArray.push(section);
        }
        return isOK;
    }

    /** 
        セクション名とキーワードを指定してそれに該当するデータを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合はnull
    */
    public CharArray get(CharArray section, CharArray keyword) {
        CharArray ch = null;
        HashVector<CharArray,CharArrayQueue> hash = hashSection.get(section);
        if (hash != null) {
            CharArrayQueue queue =  (CharArrayQueue)hash.get(keyword);
            if (queue != null) ch = queue.peek();
        }
        return ch;
    }
    
    /** 
        セクション名とキーワードを指定してそれに該当するデータを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合はnull
    */
    public CharArray get(String section, String keyword) {
        CharArray chSection = CharArray.pop(section);
        CharArray chKeyword = CharArray.pop(keyword);
        
        CharArray ch = get(chSection, chKeyword);
        
        CharArray.push(chKeyword);
        CharArray.push(chSection);
        return ch;
    }
    /** 
        セクション名とキーワードを指定してそれに該当するデータを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合はnull
    */
    public CharArray get(String section, CharArray keyword) {
        CharArray chSection = CharArray.pop(section);
        CharArray ch = get(chSection, keyword);
        CharArray.push(chSection);
        return ch;
    }
    /** 
        セクション名とキーワードを指定してそれに該当するデータを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合はnull
    */
    public CharArray get(CharArray section, String keyword) {
        CharArray chKeyword = CharArray.pop(keyword);
        CharArray ch = get(section, chKeyword);
        CharArray.push(chKeyword);
        return ch;
    }
    
    
    /** セクション名とキーワードを指定してそれに該当するデータを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合は空文字 "" を返す
    */
    public String getString(CharArray section, CharArray keyword) {
        CharArray ch = get(section, keyword);
        if (ch == null) return "";
        return ch.toString();
    }
    /** セクション名とキーワードを指定してそれに該当するデータを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合は空文字 "" を返す
    */
    public String getString(String section, String keyword) {
        String str = "";
        CharArray chSection = CharArray.pop(section);
        CharArray chKeyword = CharArray.pop(keyword);
        CharArray ch = get(chSection, chKeyword);
        if (ch != null) str = ch.toString();
        CharArray.push(chKeyword);
        CharArray.push(chSection);
        return str;
    }
    /** セクション名とキーワードを指定してそれに該当するデータを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合は空文字 "" を返す
    */
    public String getString(String section, CharArray keyword) {
        String str = "";
        CharArray chSection = CharArray.pop(section);
        CharArray ch = get(chSection, keyword);
        if (ch != null) str = ch.toString();
        CharArray.push(chSection);
        return str;
    }
    /** セクション名とキーワードを指定してそれに該当するデータを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合は空文字 "" を返す
    */
    public String getString(CharArray section, String keyword) {
        String str = "";
        CharArray chKeyword = CharArray.pop(keyword);
        CharArray ch = get(section, chKeyword);
        if (ch != null) str = ch.toString();
        CharArray.push(chKeyword);
        return str;
    }
    //------------------------------------------------------------------
    // 複数取り出し
    //------------------------------------------------------------------
    /** セクション名とキーワードを指定してそれに該当するデータリストを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合はnull
    */
    public CharArrayQueue getQueue(CharArray section, CharArray keyword) {
        CharArrayQueue queue = null;
        HashVector<CharArray,CharArrayQueue> hash = hashSection.get(section);
        if (hash != null) queue =  (CharArrayQueue)hash.get(keyword);
        return queue;
    }
    
    /** セクション名とキーワードを指定してそれに該当するデータリストを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合はnull
    */
    public CharArrayQueue getQueue(String section, String keyword) {
        CharArray chSection = CharArray.pop(section);
        CharArray chKeyword = CharArray.pop(keyword);
        
        CharArrayQueue queue = getQueue(chSection, chKeyword);
        
        CharArray.push(chKeyword);
        CharArray.push(chSection);
        return queue;
    }
    /** セクション名とキーワードを指定してそれに該当するデータリストを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合はnull
    */
    public CharArrayQueue getQueue(String section, CharArray keyword) {
        CharArray chSection = CharArray.pop(section);
        CharArrayQueue queue = getQueue(chSection, keyword);
        CharArray.push(chSection);
        return queue;
    }
    /** セクション名とキーワードを指定してそれに該当するデータリストを取り出す 
        @param section セクション名
        @param keyword キーワード名
        @return 取得データ。存在しない場合はnull
    */
    public CharArrayQueue getQueue(CharArray section, String keyword) {
        CharArray chKeyword = CharArray.pop(keyword);
        CharArrayQueue queue = getQueue(section, chKeyword);
        CharArray.push(chKeyword);
        return queue;
    }
    
    /**
        取得文字列を数値に変換
        @param section セクション名
        @param keyword キーワード名
        @return 変換数値。または0
    */
    public int getInt(CharArray section, CharArray keyword) {
        CharArray ch = get(section,keyword);
        if (ch == null) return 0;
        else            return ch.getInt();
    }
    /**
        取得文字列を数値に変換
        @param section セクション名
        @param keyword キーワード名
        @return 変換数値。または0
    */
    public int getInt(String section, String keyword) {
        CharArray ch = get(section,keyword);
        if (ch == null) return 0;
        else            return ch.getInt();
    }
    /**
        取得文字列を数値に変換
        @param section セクション名
        @param keyword キーワード名
        @return 変換数値。または0
    */
    public long getLong(CharArray section, CharArray keyword) {
        CharArray ch = get(section,keyword);
        if (ch == null) return 0;
        else            return ch.getLong();
    }
    /**
        取得文字列を数値に変換
        @param section セクション名
        @param keyword キーワード名
        @return 変換数値。または0
    */
    public long getLong(String section, String keyword) {
        CharArray ch = get(section,keyword);
        if (ch == null) return 0;
        else            return ch.getLong();
    }
    /**
        取得文字列をboolean値に変換 true:"true","yes","on","1" 等
        @param section セクション名
        @param keyword キーワード名
        @return 変換値。またはfalse
    */
    public boolean getBoolean(CharArray section, CharArray keyword) {
        CharArray ch = get(section,keyword);
        if (ch == null) return false;
        else            return ch.getBoolean();
    }
    /**
        取得文字列をboolean値に変換 true:"true","yes","on","1" 等
        @param section セクション名
        @param keyword キーワード名
        @return 変換値。またはfalse
    */
    public boolean getBoolean(String section, String keyword) {
        CharArray ch = get(section,keyword);
        if (ch == null) return false;
        else            return ch.getBoolean();
    }
    
    //----------------------------------------------
    //
    //---------------------------------------------
    /** セクションの集合（登録順）を返す */
    public Enumeration getSectionList() {
        return hashSection.keys();
    }
    
    /** 指定セクションが存在するか？
        @param section セクション名
        @return 存在するとtrue
    */
    public boolean containsSection(CharArray section) {
        return (hashSection.get(section)!= null);
    }
    /** 指定セクションが存在するか？
        @param section セクション名
        @return 存在するとtrue
    */
    public boolean containsSection(String section) {
        CharArray ch = CharArray.pop(section);
        boolean rsts = containsSection(ch);
        CharArray.push(ch);
        return rsts;
    }
    
    /** 指定セクションのキーの集合（登録順）を返す 
        @param section セクション
        @return Enumeration (CharArray)
    */
    public Enumeration getKeyList(CharArray section) {
        HashVector<CharArray,CharArrayQueue> hash = hashSection.get(section);
        if (hash == null) return null;
        return hash.keys();
    }
    /** 指定セクションのキーの集合（登録順）を返す 
        @param section セクション
        @return Enumeration (CharArray)
    */
    public Enumeration getKeyList(String section) {
        CharArray ch = CharArray.pop(section);
        Enumeration e = getKeyList(ch);
        CharArray.push(ch);
        return e;
    }
    
    /** 指定セクションのキーのHashVectorを返す <br>
        @param section セクション
        @return HashVector(CharArray/CharArrayQueue)
    */
    public HashVector<CharArray,CharArrayQueue> getKeyTable(CharArray section) {
        return  hashSection.get(section);
    }
    /** 指定セクションのキーのHashVectorを返す <br>
        @param section セクション
        @return HashVector(CharArray/CharArrayQueue)
    */
    public HashVector<CharArray,CharArrayQueue> getKeyTable(String section) {
        CharArray ch = CharArray.pop(section);
        HashVector<CharArray,CharArrayQueue> hv = getKeyTable(ch);
        CharArray.push(ch);
        return hv;
    }
    
    /** 指定セクションのデータの集合（登録順）を返す 
        @param section セクション
        @return Enumeration(CharArrayQueue)
    */
    public Enumeration getValueList(CharArray section) {
        HashVector<CharArray,CharArrayQueue> hash = hashSection.get(section);
        if (hash == null) return null;
        return hash.elements();
    }
    /** 指定セクションのデータの集合（登録順）を返す 
        @param section セクション
        @return Enumeration(CharArrayQueue)
    */
    public Enumeration getValueList(String section) {
        CharArray ch = CharArray.pop(section);
        Enumeration e = getValueList(ch);
        CharArray.push(ch);
        return e;
    }
    
    /** 指定セクションに登録されているキーの数
        @param section セクション
        @return キーの数
    */
    public int getKeyCount(CharArray section) {
        HashVector<CharArray,CharArrayQueue> hash = hashSection.get(section);
        if (hash == null) return 0;
        return hash.size();
    }
    
    /** 指定セクションに登録されているキーの数
        @param section セクション
        @return キーの数
    */
    public int getKeyCount(String section) {
        CharArray ch = CharArray.pop(section);
        int count = getKeyCount(ch);
        CharArray.push(ch);
        return count;
    }
    
}

//
// [end of IniFile.java]
//

