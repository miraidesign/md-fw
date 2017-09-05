//------------------------------------------------------------------------
// @(#)Template.java
//              テンプレート
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.Queue;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.IntQueue;
import com.miraidesign.util.QueueTable;

/**
 *  Template フレームワークが使用します
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class Template extends CharArray implements ContentItem {
    private boolean debug = (SystemConst.debug && false);        // デバッグ表示
    private boolean debugSearch = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugLang = (SystemConst.debug && false);        // デバッグ表示

    private int max = -1;
    
    //-------------------------------------------------------
    // 変更するかもしれないので final にはしない事
    public static int PARAM = 1;
    public static int LIST  = 2;
    //public static int TABLE = 2;
    public static int BOOL  = 4;
    
    private int paramMode = PARAM;
    public void setParamMode(int mode) { this.paramMode = mode;}
    public void addParamMode(int mode) { this.paramMode |= mode;}
    public void removedParamMode(int mode) { this.paramMode ^= mode;}
    
    public int getParamMode() { return this.paramMode;}
    //-------------------------------------------------------
    public int queueMode = -1;
    public void setQueueMode(int mode) { this.queueMode = mode;}
    public int getQueueMode() { return this.queueMode;}
    //-------------------------------------------------------
    // ContentItem対応
    CharArray label       = new CharArray();  // 使用予定なし
    CharArray description = new CharArray();  // 使用予定なし
    CharArray type        = new CharArray("TEMPLATE");
    public CharArray getLabel() { return label;}
    public CharArray getDescription() { return description; }
    public CharArray getType() { return type; }
    public ContentItem getOriginalItem() { return this;}

    public boolean isBlock() {return false;}
    public boolean isInput() {return false;}
    public boolean isTemplate() {return true;}

    public ContentItemNode getContentItemNode() { return null;}
    //--------------------------
    // Param 保存用
    private QueueTable paramTable = new QueueTable() {{
        addColumn(Queue.STRING, "KEY");      // 入力キーワード
        addColumn(Queue.STRING, "FUNC");     // 関数名
        addColumn(Queue.STRING, "PARAM");    // パラメータリスト
        addColumn(Queue.BOOL,   "REVERSE");  // BOOL時に反転する
    }};
    private QueueTable boolTable = new QueueTable() {{
        addColumn(Queue.STRING, "KEY");      // 入力キーワード
        addColumn(Queue.STRING, "FUNC");     // 関数名
        addColumn(Queue.STRING, "PARAM");    // パラメータリスト
        addColumn(Queue.BOOL,   "REVERSE");  // BOOL時に反転する
    }};
    
    // default constructor
    public Template() {
        // do nothing
    }
    // copy constructor
    public Template(Template from) {
        copy(from);
    }
    
    
    /** 使用関数の有無を検索する（関数名前方一致)
        @param key_name     キー名(ユーザー定義キー、@pagなど)省略時は全て
        @param func_name    関数名(list, child_urlなど）
        @return 1個でもあればtrueをかえす
    */
    public boolean existFunc(String key_name, String func_name) {
        return existFunc(key_name, func_name, 1);
    }
    /** 使用関数の有無を検索する
        @param key_name     キー名(ユーザー定義キー、@pagなど)省略時は全て
        @param func_name    関数名(list, child_urlなど）
        @param mode  関数名が、0:完全一致 1:前方一致 2:後方一致
        @return 1個でもあればtrueをかえす
    */
    
    public boolean existFunc(String key_name, String func_name, int mode) {
        boolean found = false;
        do {
            if (func_name == null || func_name.trim().length() == 0) break;
            String upper_func_name = func_name.toUpperCase();
if (debugSearch) {
    System.out.println("【チェック】"+key_name+"."+upper_func_name);

}
            for (int i = 0; i < paramTable.getRowCount(); i++) {
                CharArray key  = paramTable.getCharArray(i, "KEY");
                CharArray func = new CharArray(paramTable.getCharArray(i, "FUNC"));
                func.toUpperCase();
if (debugSearch) {
    System.out.println("   【param】"+key+"."+func);

}
                if (key != null && func != null) {
                    if (mode == 1) {
                        if (func.startsWith(upper_func_name) &&
                            (key_name == null || key_name.length()==0 ||
                             key.equals(key_name))) found = true;
                    } else if (mode == 2) { // 後方一致
                        if (func.endsWith(upper_func_name) &&
                            (key_name == null || key_name.length()==0 ||
                             key.equals(key_name))) found = true;
                    } else {    // 完全一致
                        if (func.equals(upper_func_name) &&
                            (key_name == null || key_name.length()==0 ||
                             key.equals(key_name))) found = true;
                    }
                    if (found) break;
                }
            }
            if (found) break;
            for (int i = 0; i < boolTable.getRowCount(); i++) {
                CharArray key = boolTable.getCharArray(i, "KEY");
                CharArray func = boolTable.getCharArray(i, "FUNC");
if (debugSearch) {
    System.out.println("   【bool】"+key+"."+func);

}
                if (key != null && func != null) {
                    if (mode == 1) {
                        if (func.startsWith(upper_func_name) &&
                            (key_name == null || key_name.length()==0 ||
                             key.equals(key_name))) found = true;
                    } else if (mode == 2) { // 後方一致
                        if (func.endsWith(upper_func_name) &&
                            (key_name == null || key_name.length()==0 ||
                             key.equals(key_name))) found = true;
                    } else {    // 完全一致
                        if (func.equals(upper_func_name) &&
                            (key_name == null || key_name.length()==0 ||
                             key.equals(key_name))) found = true;
                    }
                    if (found) break;
                }
            }
        } while (false);
        return found;
    }
    
    /** 入力関数(param)を追加する 
        @param key   入力キーワード
        @param func  関数名
        @param param パラメータリスト（カンマ区切り）※未サポート
    */
    public void addInputFunction(CharArray key,CharArray func, CharArray param) {
        int index = paramTable.getRowCount();
        boolean reverse = false;
        CharArray _key = new CharArray(key);
        CharArray _func = new CharArray(func);
        reverse = checkFunc(_key,reverse);
        reverse = checkFunc(_func,reverse);
        paramTable.addRow();
        paramTable.setCharArray(_key, index,"KEY");
        paramTable.setCharArray(_func, index,"FUNC");
        paramTable.setCharArray(param, index,"PARAM");
        paramTable.setBoolean(reverse, index,"REVERSE");    // 反転モードも保存する
if (debug) System.out.println("["+key+"]addInputFunction("+key+","+func+","+param+","+reverse+")");
    }
    
    /** 入力関数(bool)を追加する 
        @param key   入力キーワード
        @param func  関数名
        @param param パラメータリスト（カンマ区切り）※未サポート
    */
    public void addBoolInputFunction(CharArray key,CharArray func, CharArray param) {
        int index = boolTable.getRowCount();
        boolean reverse = false;
        CharArray _key = new CharArray(key);
        CharArray _func = new CharArray(func);
        reverse = checkFunc(_key,reverse);
        reverse = checkFunc(_func,reverse);
        boolTable.addRow();
        boolTable.setCharArray(_key, index,"KEY");
        boolTable.setCharArray(_func, index,"FUNC");
        boolTable.setCharArray(param, index,"PARAM");
        boolTable.setBoolean(reverse, index,"REVERSE");
if (debug) System.out.println("★★["+key+"]addBoolInputFunction("+key+","+func+","+param+","+reverse+")");
    }
    
    /** 入力関数(param)+サブ関数を追加する (since ver 0.967)
        @param key   入力キーワード
        @param funcs 関数名リスト
        @param params パラメータリスト
    */
    public void addInputFunction(CharArray key,CharArrayQueue funcs, CharArrayQueue params) {
if (debug) System.out.println("★★["+key+"]addInputFunction funcs:"+funcs.size()+" params:"+params.size());
        int index = paramTable.getRowCount();
        CharArray _key = new CharArray(key);
        for (int i = 0; i < funcs.size(); i++) {
            CharArray _func  = new CharArray(funcs.peek(i));
            CharArray _param = new CharArray(params.peek(i));
            boolean reverse = false;
            if (i == funcs.size()-1) {    // 最後の演算関数時のみ、クラス名の接頭条件と掛け合わせる
                reverse = checkFunc(_key,false);
            }
            reverse = checkFunc(_func,reverse);
            paramTable.addRow();
            if (i > 0) _key.set(".");       // sub関数のキーは"." 【重要】
            paramTable.setCharArray(_key, index,"KEY");
            paramTable.setCharArray(_func, index,"FUNC");
            paramTable.setCharArray(_param, index,"PARAM");
            paramTable.setBoolean(reverse, index,"REVERSE");    // 反転モードも保存する

if (debug) System.out.println("★★["+key+"]addInputFunction("+_key+","+_func+","+_param+","+reverse+")");
            
            index++;
        } // next;
    }
    
    /** 入力関数(bool) +サブ関数を追加する  (since ver 0.967)
        @param key   入力キーワード
        @param funcs 関数名リスト
        @param params パラメータリスト
    */
    public void addBoolInputFunction(CharArray key,CharArrayQueue funcs, CharArrayQueue params) {
        int index = boolTable.getRowCount();
        CharArray _key = new CharArray(key);
        for (int i = 0; i < funcs.size(); i++) {
            CharArray _func  = new CharArray(funcs.peek(i));
            CharArray _param = new CharArray(params.peek(i));
            boolean reverse = false;
            if (i == funcs.size()-1) {    // 最後の演算関数時のみ、クラス名の接頭条件と掛け合わせる
                reverse = checkFunc(_key,false);
            }
            reverse = checkFunc(_func, reverse);
            boolTable.addRow();
            if (i > 0) _key.set(".");       // sub関数のキーは"." 【重要】
            boolTable.setCharArray(_key, index,"KEY");
            boolTable.setCharArray(_func, index,"FUNC");
            boolTable.setCharArray(_param, index,"PARAM");
            boolTable.setBoolean(reverse, index,"REVERSE");
if (debug) System.out.println("["+key+"]addBoolInputFunction("+_key+","+_func+","+_param+","+reverse+")");
            index++;
        } // next;
    }
    
    /* 反転モード等をチェックする */
    private boolean checkFunc(CharArray ch, boolean reverse) {
        if (ch == null || ch.length() == 0 || ch.chars[0] != '!') return reverse;
        while (ch.chars[0] == '!') {
            reverse = !reverse;
            ch.remove(0,1);
        }
        return reverse;
    }
    
    /* 入力パラメータ(param)テーブルを取得する */
    public QueueTable getParamTable() { return paramTable;}
    public QueueTable getBoolTable() { return boolTable;}
    //--------------------------
    public static final int AUTO    = 0;    // 自動
    public static final int FALSE   = 1;    // 非表示
    public static final int TRUE    = 2;    // 表示
    
    private int mode = AUTO;
    /* 表示モード */
    public void setMode(int i) { 
        if (debug) {
            System.out.println("★★★★★★★★★★★★★★★★★★★");
            System.out.println("Template("+key+")setMode->"+mode);
            System.out.println("★★★★★★★★★★★★★★★★★★★");
        }
        mode = i; 
    }
    public int getMode() { return mode;}
    
    /* 最大繰返し回数 (-1)でシステムのものを使用 */
    public void setMax(int max) { this.max = max;}
    public int  getMax() { return max;}
    
    private CharArray filename = null;
    /* テキストファイル設定 */
    public void setFilename(CharArray ch) { 
        if (filename == null) filename = new CharArray();
        filename.set(ch);
    }
    public void setFilename(String str) { 
        if (filename == null) filename = new CharArray();
        filename.set(str);
    }
    public CharArray getFileName() { return filename; }
    
    private CharArray dataFile = null;
    /* データファイル設定 */
    public void setDataFile(CharArray ch) { 
        if (dataFile == null) dataFile = new CharArray();
        dataFile.set(ch);
    }
    public void setDataFile(String str) { 
        if (dataFile == null) dataFile = new CharArray();
        dataFile.set(str);
    }
    
    public CharArray getDataFile() { return dataFile; }

    private IntQueue position;
    /* Position設定 */
    public void setPosition(CharArray ch) {
        if (ch != null) {
            if (position == null) position = new IntQueue();
            position.clear();
            CharToken token = CharToken.pop();
            token.set(ch);
            for (int i = 0; i < token.size(); i++) {
                position.enqueue(token.getInt(i));
            }
            CharToken.push(token);
        }
    }
    public IntQueue getPosition() { return position; }
    
    public int getPosition(int index) { 
        if (position == null || (position.size()-1) < index) {
            return index;
        }
        return position.peek(index);
    }

    
    private QueueTable dataTable = null;
    public void setData(CharArrayQueue ca) {
        if (ca != null) {
            if (dataTable == null) dataTable = new QueueTable();
            dataTable.clearAll();
            dataTable.readText(ca);
        }
    }
    public QueueTable getData() { return dataTable;}
    public void clearData() {
        if (dataTable != null) dataTable.clearAll();
    }
    
    private CharArray key = new CharArray();
    public void setKey(CharArray key) { this.key.set(key); }
    public CharArray getKey() {return key;}
    
    //---------------------------------------------------
    // 複数言語用
    // 当面、nextPointer で実現する
    // 将来： ContentTemplateで hashで持つ
    //---------------------------------------------------
    public Template next = null;
    public String lang = "";
    
    /*
       LANG に対応したTemplateがあればそれを返す
       存在しない時に無限ループしている！
    */
    public Template getLangTemplate(SessionObject session) {
        int count = 0;
        if (next == null || session == null) return this;
        CharArray langData = session.getLangData();
if (debugLang) System.out.println("---★★ getLangTemplate:"+key+" langData:"+langData);
        if (langData != null && langData.length() > 0) {
            for (Template last = next; last != null; last = last.next) {
if (debugLang) System.out.println("★★ getLangTemplate last.lang:"+last.lang);
                if (langData.equals(last.lang)) {
                    return last;
                }
                if (++count > 20) {
                    System.out.println("★★データエラー発生");
                    break;
                }
            }
        }
        return this;
    }
    
    //----------------------------------------
    // copy 
    //----------------------------------------
    public void copy(Template from) {
        super.copy(from);
        mode = from.mode;
        max = from.max;
        paramMode = from.paramMode;
        queueMode = from.queueMode;
        label.set(from.label);
        description.set(from.description);
        type.set(from.type);
        
        if (from.filename == null) filename = null;
        else {
            if (filename == null) filename = new CharArray();
            filename.set(from.filename);
        }
        if (from.dataFile == null) dataFile = null;
        else {
            if (dataFile == null) dataFile = new CharArray();
            dataFile.set(from.dataFile);
        }
        
        if (from.position == null) position = null;
        else {
            if (position == null) position = new IntQueue();
            position.copy(from.position);
        }
        
        if (from.dataTable == null) dataTable = null;
        else {
            if (dataTable == null) dataTable = new QueueTable();
            dataTable.copy(from.dataTable);
        }
        if (from.key == null) key = null;
        else {
            if (key == null) key = new CharArray();
            key.set(from.key);
        }
        
        paramTable.copy(from.paramTable);
        boolTable.copy(from.boolTable);
        
        next = from.next;
        lang = from.lang;
    }
    
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    // 
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            super.writeObject(out);
            out.writeInt(mode);
            out.writeInt(max);
            if (filename != null) {
                out.writeBoolean(true);
                filename.writeObject(out);
            } else {
                out.writeBoolean(false);
            }
            if (key != null) {
                out.writeBoolean(true);
                key.writeObject(out);
            } else {
                out.writeBoolean(false);
            }
            
        }
    }
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            super.readObject(in);
            mode = in.readInt();
            max  = in.readInt();
            boolean b = in.readBoolean();
            if (b) {
                if (filename == null) filename = new CharArray();
                filename.readObject(in);
            } else {
                filename = null;
            }
            b = in.readBoolean();
            if (b) {
                if (key == null) key = new CharArray();
                key.readObject(in);
            } else {
                key = null;
            }
        }
    }

}

//
//
// [end of Template.java]
//

