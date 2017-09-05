//------------------------------------------------------------------------
//    ContentItemInfo.java
//                 ContentItem 情報
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//                  update: 2010-04-05 ishi
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------
// $Id$
//------------------------------------------------------------------------

package com.miraidesign.content;

import java.util.Hashtable;

import com.miraidesign.util.Queue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.Parameter;
import com.miraidesign.util.QueueTable;
import com.miraidesign.util.ObjectQueue;
import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.content.input.InputItem;

/** ContentItem 情報 */
public class ContentItemInfo extends QueueTable{
    private boolean debug = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugTemplate = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugFunc = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugParse = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugTMCount = (SystemConst.debug && false);  // デバッグ表示

    ContentParser parser = null;
    int inputCounter = 0;
    int blockCounter = 0;
    int templateCounter = 0;
    int scriptCounter = 0;
    private Hashtable<CharArray,ObjectQueue> hash = new Hashtable<CharArray,ObjectQueue>();   // CharArray key
                                                // ObjectQueue -> InputItem (配列順)
    static int TM_COUNT = 1;
    int tm_count = 0;
    
    public int getTMCount() { 
        if (debugTMCount) {
System.out.println("★★★★★★★★★★★★★★★");
System.out.println("ContentItemInfo.getTMCount() -> "+tm_count);
System.out.println("★★★★★★★★★★★★★★★");
        }
        return tm_count; 
    }
    
    protected ContentItemNode rootNode;
    public void debugNode() {
System.out.println("■□■□■□■□■□■□■□");
            if (rootNode != null) rootNode.debugNode();
System.out.println("■□■□■□■□■□■□■□");
    }
    
    // constructor
    private ContentItemInfo() {
        init();
    }
    public ContentItemInfo(ContentParser parser) {
        init();
        this.parser = parser;
        
        if (parser.parser_mode == 0) {  // 入力の時のみ
            rootNode = parser.getContentItemNode();
        }
        tm_count = TM_COUNT++;
        if (TM_COUNT > 1000000) TM_COUNT = 1;
    }
     private void init() {
if (debugParse) System.out.println("★☆ ContentItemInfo#init()");
        addColumn(Queue.INT, "LINE");      // 参照行数
        addColumn(Queue.INT, "COUNT");     // 表示回数
        addColumn(Queue.INT, "NEST");      // ネスト
    }
    // method
    
    /** 全てをクリアする */
    public void clearAll() {
if (debugParse) System.out.println("★☆ ContentItemInfo#clearAll()");
        super.clear();
        hash.clear();
        inputCounter = 0;
        blockCounter = 0;
        templateCounter = 0;
        scriptCounter = 0;
    }

    /** 行データをクリアする */
    public void clear() {
if (debugParse) System.out.println("★☆ ContentItemInfo#clear()");
        super.clear();
    }
    
    /* データを追加する */
    public void add(int line, int count, int nest) {
        int index = getRowCount();
        addRow();
        setInt(line,index,"LINE");
        setInt(count,index,"COUNT");
        setInt(nest,index,"NEST");
        ///////////////////////////////////// 新バージョン用追加 for ver 0.864
        char c = parser.getMagic(line);
        if (c == parser.i_magic) {
            Input input = (Input)parser.getContentItem(line);
            CharArray key = input.getKey();
            ObjectQueue queue = (ObjectQueue)hash.get(input.getKey());
            if (queue == null) {
                queue = new ObjectQueue();
                hash.put(key,queue);
            }
            queue.enqueue(InputItem.createInputItem(input,parser));
            inputCounter++;
        } else if (c == parser.b_magic || c == parser.e_magic) {
            blockCounter++;
        } else if (c == parser.t_magic) {
            templateCounter++;
        } else if (c == parser.s_magic) {
            scriptCounter++;
        }
        //----------------------------------
    }
    /* input の行数を返す */
    public int getInputCounter() { return inputCounter;}
    /* block の行数を返す */
    public int getBlockCounter() { return blockCounter;}
    /* template の行数を返す */
    public int getTemplateCounter() { return templateCounter;}
    /* script の行数を返す */
    public int getScriptCounter() { return scriptCounter;}
    
    /**
        指定位置のContentItem を返す
        @param index = 0 ～
        @return ContentItem
    */
    public ContentItem getContentItem(int index) {
        ContentItem item = null;
        if (parser == null) System.out.println("ContentItemInfo#getContentItem error parser == null");
        if (getRowCount() > index && index >=0 && parser != null)  {
            int j = getInt(index, "LINE");
            item = parser.getContentItem(j);
            ///////////////////////////////////// 新バージョン用追加 for ver 0.864
            int count = getInt(index, "COUNT");
            char c = parser.getMagic(j);
            if (c == parser.i_magic) {
                do { 
                    Input input = (Input)item;
                    ObjectQueue queue = (ObjectQueue)hash.get(input.getKey());
                    if (queue == null) {
                        System.out.println("ContentItemInfo#getContentItem error queue=null");
                        break;
                    }
                    ContentItem ci = (ContentItem)queue.peek(count-1);
                    if (ci == null) {
                        System.out.println("ContentItemInfo#getContentItem error InputItem not found");
                        break;
                    }
                    item = ci;
                } while (false);
            }
            ///////////////////////////////////// 
        }
        return item;
    }
    
    /**
        表示回数を返す
        @param index = 0 ～
        @return -1: 行指定エラー
    */
    public int getActiveCount(int index) {
        int _count = -1;
        if (index < getRowCount()) {
            _count = getInt(index, "COUNT");
        }
        return _count;
    }
    /**
        ネストカウントを返す
        @param index = 0 ～
        @return -1: 行指定エラー
    */
    public int getNestCount(int index) {
        int _count = -1;
        if (index < getRowCount()) {
            _count = getInt(index, "NEST");
        }
        return _count;
    }
    
    /**
        ContentParserの参照行を返す
        @param index = 0 ～
        @return -1: 行指定エラー  0～
    */
    public int getReferenceLine(int index) {
        int _line = -1;
        if (index < getRowCount()) {
            _line = getInt(index, "LINE");
        }
        return _line;
    }
    
    /**
        指定キーワードのブロックを返します
        @param key キーワード
        @return 存在しない場合は null
    */
    public Block getBlock(String key) {
        ObjectQueue _queue = parser.getBlock(key);
        return (_queue.size() == 0) ? null : (Block)_queue.peek();
    }
    public Block getBlock(CharArray key) {
        ObjectQueue _queue = parser.getBlock(key);
        return (_queue.size() == 0) ? null : (Block)_queue.peek();
    }
    /**
        指定キーワードの入力オブジェクトを返します
        @param key キーワード
        @return 存在しない場合は null
    */
    public Input getInput(String key) {
        return parser.getInput(key);
    }
    public Input getInput(CharArray key) {
        return parser.getInput(key);
    }
    
    /** InputItem を返す
        @param key キーワード
        @param count 参照カウント
        @return InputItem
    */
    public InputItem getInputItem(CharArray key, int count) {
        InputItem item = null;
        do {
            item = parser.getSystemInputItem(key,count); // システムアイテムを取得
            if (item != null) break;
            if (hash == null) break;
            ObjectQueue queue = (ObjectQueue)hash.get(key);
            if (queue == null) break;
            item = (InputItem)queue.peek(count-1);
        
        } while (false);
        if (item == null) System.out.println("ContentItemInfo#getInputItem("+key+","+count+")が見つかりません");
        return item;
    }
    public InputItem getInputItem(String key, int count) {
        CharArray ch = CharArray.pop(key);
        InputItem item = getInputItem(ch, count);
        CharArray.push(ch);
        return  item;
    }
    
    /**
        テンプレートにセットする情報を入力パラメータから
        取り出してセッションにセットしておく
        @param session SessionObjet
    */
    public void setTemplate(SessionObject session) {
        String nameSpace = parser.getNameSpace();
        if (debug || debugTemplate) session.println("◆ContentItemInfo#setTemplate row="+getRowCount()+" nameSpace:"+nameSpace);
        for (int i = 0; i < getRowCount(); i++) {
            if (debug) {
                System.out.println("----------------");
                debugParameter(i);
            }
            ContentItem ci = getContentItem(i);
            if (ci != null && ci.isTemplate()) {
                int paramMode = ((Template)ci).getParamMode();
                int count = getInt(i,"COUNT");  // 参照回数の取得
                
                CharArray tmp_key = ci.getKey();
                QueueTable table = ((Template)ci).getParamTable();  // paramテーブルを取得
                QueueTable boolTable = ((Template)ci).getBoolTable();  // paramテーブルを取得
                if (debug || debugTemplate) {
                    session.println("-- ContentItemInfo#setTemplate dump paramTable --");
                    table.dumpTable();
                }
                if (table.getRowCount() == 0) continue;
                
                Parameter p = new Parameter();  //
                boolean display = false;
                
                if ((paramMode & Template.PARAM) != 0) {
                    boolean _display = true;
                    if (boolTable.getRowCount() > 0) {
                        _display = isDisplay(boolTable , count, session);
                    }
                    if (_display) {
                        for (int j = 0; j < table.getRowCount(); j++) {
                            CharArray inp_key = table.getCharArray(j, "KEY");
                            CharArray func  = table.getCharArray(j, "FUNC");
                            CharArray param = table.getCharArray(j, "PARAM");
                            boolean   reverse = table.getBoolean(j, "REVERSE");
                    
                            InputItem item = getInputItem(inp_key, count);
                            if (item == null) {
                                session.println("(p)関数が見つかりません:"+inp_key+"."+func+"("+param+")"+reverse);
                                continue;
                            }
                            if (item.isDisplay()) { // 表示モード
if (debugFunc) session.println("(p)関数が見つかりました:"+inp_key+"."+func+"("+param+")"+(reverse?"reverse":"")+" "+item.getType());
                                display = true;
                                CharToken token = null;
                                if (param.length() > 0) token = new CharToken(param, ",");

                                //p.add(item.getFunc(func, token, 1));
                                int org_size = p.size();
                                item.getParameter(func, token, p, session);
                                
                                
                                if (org_size == p.size()) p.add(""); 
                                
                                // （連続関数の処理)
                                for (int k = j+1; k < table.getRowCount(); k++) {
                                    CharArray _key = table.getCharArray(k, "KEY");
                                    if (_key.equals(".")) {
                                        ContentSubFunction subfunc = ContentSubFunction.getInstance();
                                        // サブ関数の処理を書く！
                                        CharArray _func  = table.getCharArray(k, "FUNC");
                                        CharArray _param = table.getCharArray(k, "PARAM");
                                        boolean   _reverse = table.getBoolean(k, "REVERSE");
                                        if (debug || debugTemplate) {
                                            System.out.println("★サブ関数発見："+_func+"("+_param+")");
                                        }
                                        subfunc.call(p.peek(org_size),_func,_param, session);
                                        
                                        ++j;
                                    } else {
                                        if (debug || debugTemplate) {
                                            System.out.println("★サブ関数処理終了");
                                        }
                                        break;
                                    }
                                }
                                
                            
                            } else {
if (debugFunc) System.out.println("(p)関数が見つかりました:"+inp_key+"."+func+"("+param+")"+item.getType()+":が表示しません");
                                p.add("");
                            }
                        } // next
                    //if (display) {  // 表示可能なものがあれば表示する
                    //    session.setTemplate(tmp_key, p, count-1, nameSpace);
                    //}
                    //if (paramMode == Template.PARAM) {
if (debugFunc) {
                        System.out.println(" 表示モードを "+display+" にする");
                        for (int n = 0; n < p.size(); n++) {
                            System.out.println(" ["+(n+1)+"]"+p.peek(n));
                        }
}
                        p.setDisplay(display);
                        session.setTemplate(tmp_key, p, count-1, nameSpace);  //@@// ここか！ 
                    //}
                    } else {
if (debugFunc) System.out.println(" 非表示モードに設定する");
                        //p.add("")
                        p.setDisplay(false);
                        session.setTemplate(tmp_key, p, count-1, nameSpace);  //@@// ここか！ 
                    }
                } else if ((paramMode & Template.LIST) != 0) {
                    boolean _display = true;
                    if (boolTable.getRowCount() > 0) {
                        _display = isDisplay(boolTable , count, session);
                    }
                    if (_display) {
                        int LIST_MAX = 0;
                        ObjectQueue listQueue = new ObjectQueue();
                        for (int j = 0; j < table.getRowCount(); j++) {
                            CharArray inp_key = table.getCharArray(j, "KEY");
                            CharArray func  = table.getCharArray(j, "FUNC");
                            CharArray param = table.getCharArray(j, "PARAM");
                            boolean   reverse = table.getBoolean(j, "REVERSE");
                    
                            InputItem item = getInputItem(inp_key, count);
                            if (item == null) {
                                System.out.println("(LIST)関数が見つかりません:"+inp_key+"."+func+"("+param+")"+reverse);
                                //continue;
                                p.add("");
                            } else {
                                if (item.isDisplay()) { // 表示モード
if (debugFunc) System.out.println("(LIST)関数が見つかりました:"+inp_key+"."+func+"("+param+")"+reverse+" "+item.getType());
                                    display = true;
                                    CharToken token = null;
                                    if (param.length() > 0) token = new CharToken(param, ",");

                                    p = new Parameter();
                                    //item.getParameter(func, token, p, session);
                                    item.getListParameter(func, token, p, session);
                            
                                    // (連続関数の処理)
                                    for (int k = j+1; k < table.getRowCount(); k++) {
                                        CharArray _key = table.getCharArray(k, "KEY");
                                        if (_key.equals(".")) {
                                            ContentSubFunction subfunc = ContentSubFunction.getInstance();
                                            // サブ関数の処理を書く！
                                            CharArray _func  = table.getCharArray(k, "FUNC");
                                            CharArray _param = table.getCharArray(k, "PARAM");
                                            boolean   _reverse = table.getBoolean(k, "REVERSE");
                                            if (debug || debugTemplate) {
                                                System.out.println("★サブ関数発見："+_func+"("+_param+")");
                                            }
                                            subfunc.call(p ,_func,_param, session);
                                            
                                            ++j;
                                        } else {
                                            if (debug || debugTemplate) {
                                                System.out.println("★サブ関数処理終了");
                                            }
                                            break;
                                        }
                                    }
                            
                                } else {
if (debugFunc) System.out.println("(LIST)関数が見つかりました:"+inp_key+"."+func+"("+param+")"+reverse+" "+item.getType()+":が表示しません");
                                    p.add("");
                                }
                            }
                            LIST_MAX = Math.max(LIST_MAX, p.size());
                            listQueue.enqueue(p);
                        } // next
                        for (int k = 0; k < LIST_MAX; k++) {  // 
                            p = new Parameter();
                            for (int j = 0; j < listQueue.size(); j++) {
                                p.enqueue(((Parameter)listQueue.peek(j)).peek(k));
                            }
                            session.addTemplate(tmp_key, p, count-1, nameSpace);   //@@//
                        }
                    }
                //} else if (paramMode == Template.TABLE) {
                } else if (paramMode == Template.BOOL) {
                    display = isDisplay(table, count, session);
                    /**
                    display = false;
                    for (int j = 0; j < table.getRowCount(); j++) {
                        CharArray inp_key = table.getCharArray(j, "KEY");
                        CharArray func  = table.getCharArray(j, "FUNC");
                        CharArray param = table.getCharArray(j, "PARAM");
                    
                        InputItem item = getInputItem(inp_key, count);
                        if (item == null) {
                            //System.out.println("関数が見つかりません:"+inp_key+"."+func+"("+param+")");
                            //continue;
                            //p.add("");  // 
                            //display = false;
                        } else {
if (debugFunc) System.out.println("(BOOL)関数が見つかりました:"+inp_key+"."+func+"("+param+")"+item.getType());
                            if (item.isDisplay()) { // 表示モード
                                CharToken token = null;
                                if (param.length() > 0) token = new CharToken(param, ",");

                                p = new Parameter();
                                item.getParameter(func, token, p, session);
                                if (p.size() == 0 || p.peek().length()==0) {}
                                else display = true;
                            
                            } else {
                                //display = false;
                                //p.add("");
                            }
                        }
                    } // next
                    **/
                     Parameter p2 = new Parameter();
                     p2.setDisplay(display);    // 表示モードのみ返す
                     session.setTemplate(tmp_key, p2, count-1, nameSpace); //@@//

                }
            } // ci.isTemplate()
        } // next
    } // end of setTemplate(sesison)
    
    /*
        BOOL タイプの結果を返す
    */
    private boolean isDisplay(QueueTable table, int count) {
        return isDisplay(table, count, null);
    }
    // こちらのみ使うようにする
    private boolean isDisplay(QueueTable table, int count, SessionObject session) {
if (debugFunc) System.out.println(" ▽display check");
        boolean display = false;
        for (int j = 0; j < table.getRowCount(); j++) {
            CharArray inp_key = table.getCharArray(j, "KEY");
            CharArray func  = table.getCharArray(j, "FUNC");
            CharArray param = table.getCharArray(j, "PARAM");
            boolean   reverse = table.getBoolean(j, "REVERSE");
        
            InputItem item = getInputItem(inp_key, count);
            if (item == null) {
                System.out.println("isDisplay:関数が見つかりません:"+inp_key+"."+func+"("+param+")"+reverse);
            } else {
if (debugFunc) System.out.println("isDisplay:(BOOL)関数が見つかりました:"+inp_key+"."+func+"("+param+")"+item.getType());
                if (item.isDisplay()) { // 表示モード
                    CharToken token = null;
                    if (param.length() > 0) token = new CharToken(param, ",");
                    Parameter p = new Parameter();
                    int org_size = p.size();
                    item.getParameter(func, token, p, session);
                    
                    // 連続関数の処理
                    for (int k = j+1; k < table.getRowCount(); k++) {
                        CharArray _key = table.getCharArray(k, "KEY");
                        if (_key.equals(".")) {
                            ContentSubFunction subfunc = ContentSubFunction.getInstance();
                            // サブ関数の処理を書く！
                            CharArray _func  = table.getCharArray(k, "FUNC");
                            CharArray _param = table.getCharArray(k, "PARAM");
                            boolean   _reverse = table.getBoolean(k, "REVERSE");
                            if (debug || debugTemplate) {
                                System.out.println("☆サブ関数発見："+_func+"("+_param+")");
                            }
                            subfunc.call(p.peek(org_size),_func,_param, session);
                            ++j;
                        } else {
                            if (debug || debugTemplate) {
                                System.out.println("☆サブ関数処理終了");
                            }
                            break;
                        }
                    } // next
                    
                    if (!reverse) {
                        if (p.size() == 0 || p.peek().length()==0) {
if (debugFunc) System.out.println(" 表示しない");
                        
                        } else {
if (debugFunc) System.out.println(" 表示する");
                            display = true;
                        }
                    } else {
                        if (p.size() == 0 || p.peek().length()==0) { 
if (debugFunc) System.out.println(" 反転しているので表示する");
                            display=true;
                        } else {
if (debugFunc) System.out.println(" 反転しているので表示しない");
                        
                        }
                    }
                } else {
if (debugFunc) System.out.println(" 表示モードでないので表示しない");
                    //display = false;
                    //p.add("");
                }
            }
        } // next
if (debugFunc) System.out.println(" △display=表示"+(display? "する":"しない"));
        return display;
    }
    
    /*
        ContentItemInfoのデバッグ表示を行う
    */
    public void debugParameter() {
        debugParameter((SessionObject)null);
    }
    public void debugParameter(SessionObject session) {
        String sz = (session != null) ? session.count+"|" :"";
        for (int j = 0; j < getRowCount(); j++) {
            ContentItem ci = getContentItem(j);
            if (ci == null) {
                System.out.println(sz+"["+j+"] null");
            } else {
              try {
                int line  = getInt(j,"LINE");
                int count = getInt(j,"COUNT");
                System.out.println(sz+"["+j+"]"+
                    "line("+line+")count("+count+")\t"+
                    "type["+ci.getType()+"]\tkey["+ci.getKey()+"]\tlabel["+ci.getLabel()+"]");
              } catch (Exception ex) {
                //ex.printStackTrace();
              }
            }
        }
    }
    public CharArray debugParameter(CharArray ch) {
        if (ch == null) ch = new CharArray();
        for (int j = 0; j < getRowCount(); j++) {
            ContentItem ci = getContentItem(j);
            try {
                int line  = getInt(j,"LINE");
                int count = getInt(j,"COUNT");
                ch.add("["+j+"]"+
                    "line("+line+")count("+count+")\t"+
                    "type["+ci.getType()+"]\tkey["+ci.getKey()+"]\tlabel["+ci.getLabel()+"]\n");
                if (ci.isInput() && ci instanceof InputItem) {
                    ch.add("     data["+((InputItem)ci).getData()+"]\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return ch;
    }
    /* デバッグ表示 */
    public void debugParameter(int index) {
        debugParameter(index, (SessionObject)null);
    }
    public void debugParameter(int index, SessionObject session) {
        int j = index;
        ContentItem ci = getContentItem(j);
        try {
            int line  = getInt(j,"LINE");
            int count = getInt(j,"COUNT");
            String str = ((session != null) ? session.count+"|" :"")
                +"["+j+"]"
                +"line("+line+")count("+count+")\t"
                +"type["+ci.getType()+"]\tkey["+ci.getKey()+"]\tlabel["+ci.getLabel()+"]";
                if (ci.isInput()) {
                    str += "\tdata["+((InputItem)ci).getData()+"]";
                }
            System.out.println(str);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public CharArray debugParameter(int index, CharArray ch) {
        if (ch == null) ch = new CharArray();
        int j = index;
        ContentItem ci = getContentItem(j);
        try {
            int line  = getInt(j,"LINE");
            int count = getInt(j,"COUNT");
            String str = "["+j+"]"
                +"line("+line+")count("+count+")\t"
                +"type["+ci.getType()+"]\tkey["+ci.getKey()+"]\tlabel["+ci.getLabel()+"]";
                if (ci.isInput()) {
                    str += "\tdata["+((InputItem)ci).getData()+"]";
                }
            ch.add(str); ch.add("\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ch;
    }
    /** デバッグ表示２ */
    public void debugParameter2() {
        debugParameter2(null);
    }
    public void debugParameter2(SessionObject session) {
        String sz = (session != null) ? session.count+"|" :"";
        for (int j = 0; j < getRowCount(); j++) {
            ContentItem ci = getContentItem(j);
            try {
                int max = -1;
                int limit = -1;
                CharArray type = ci.getType();
                if (type.equals("BLOCK")) {
                    max = ((Block)ci).getMax();
                    limit = ((Block)ci).getLimit();
                }
                else if (type.equals("BLOCK_END")) {
                    max = ((BlockEnd)ci).getBlock().getMax();
                    limit = ((BlockEnd)ci).getBlock().getLimit();
                }
                int line  = getInt(j,"LINE");
                int count = getInt(j,"COUNT");
                int active  = getActiveCount(j);
                int nest  = getInt(j,"NEST");
                System.out.println(sz+"["+j+"]"+
                    "line("+line+")count("+count+")\t"+
                    "active("+active+") nest("+nest+") max("+max+"/"+limit+")\t"+
                    "type["+type+"]\tkey["+ci.getKey()+"]\tlabel["+ci.getLabel()+"]");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    public void debugInputParameter() {
        debugInputParameter(null);
    }
    public void debugInputParameter(SessionObject session) {
        String sz = (session != null) ? session.count+"|" :"";
        for (int j = 0; j < getRowCount(); j++) {
            ContentItem ci = getContentItem(j);
            if (ci.isInput()) {
              try {
                int line  = getInt(j,"LINE");
                int count = getInt(j,"COUNT");
                System.out.println(sz+"["+j+"]"+
                    "line("+line+")count("+count+")\t"+
                    "type["+ci.getType()+"]\tkey["+ci.getKey()+"]"+
                    "\tdata["+((InputItem)ci).getData()+"]");
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
        }
    }
   
}

//
// [end of ContentItemInfo.java]
//

