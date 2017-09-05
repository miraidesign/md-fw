//------------------------------------------------------------------------
// @(#)ContentTemplate.java
//              テンプレートパーサー＆トランスレーター
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import java.util.Enumeration;
import java.util.Hashtable;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.servlet.ServletLog;
import com.miraidesign.renderer.Page;

import com.miraidesign.util.Queue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharArrayFile;
import com.miraidesign.util.QueueFile;
import com.miraidesign.util.IntQueue;
import com.miraidesign.util.ObjectQueue;
import com.miraidesign.util.QueueElement;
import com.miraidesign.util.QueueTable;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.HashParameter;

/**
 *  ContentTemplate
 *  
 *  @version 0.5
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ContentTemplate {
    private boolean debug          = (SystemConst.debug && false);   // デバッグ表示 false
    private boolean debugParse     = (SystemConst.debug && false);   // デバッグ表示 false
    private boolean debugMacro     = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugSearch    = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugTemplate  = (SystemConst.debug && false);  // デバッグ表示
    private boolean debugNamespace = (SystemConst.debug && false);  // デバッグ表示 false

    private ObjectQueue queue = new ObjectQueue();   // Templateを保管
    
    /* Template個数の取得 */
    public int size() {
        return queue.size();
    }
    
    /* 指定位置のテンプレートを取得 */
    public Template getTemplate(int index) {
        return (Template)queue.peek(index);
    }
    
    /** テンプレートを取得 
        @param key キー
        @return 存在しない時は null
    */
    public Template getTemplate(CharArray key) {
        for (int i = 0; i < queue.size(); i++) {
            Template template = (Template)queue.peek(i);
            CharArray ch = template.getKey();
            if (ch.equals(key)) return template;
        }
        return null;
    }
    public Template getTemplate(String str) {
        CharArray key = CharArray.pop(str);
        Template temp = getTemplate(key);
        CharArray.push(key);
        return temp;
    }
    
    private static final String szKey  = "KEY";
    private static final String szMax  = "MAX";
    private static final String szFile = "FILE";
    private static final String szMode = "MODE";
    private static final String szData = "DATA";
    private static final String szParam = "PARAM";
    private static final String szList  = "LIST";
    private static final String szBool  = "BOOL";
    
    private ModuleManager moduleManager;
    
    private HashVector<CharArray,CharArrayQueue> temp_hv;  // template.ini [Template] の参照
    private HashVector<CharArray,CharArrayQueue> site_hv;  // site.ini [site.n.Template] の参照
    
    private Hashtable<CharArray,QueueTable> hashData;     // テンプレートテーブルデータ
    public Hashtable<CharArray,QueueTable> getHashData() {
        return hashData;
    }
    
    /** デバッグ関数*/
    public void debugTemplate() {
        System.out.println("▽▽▽ContentTemplate.debugTemplate");
        for (int i = 0; i <queue.size(); i++) {
            Template _t = (Template)queue.peek(i);
            System.out.println("queue["+i+"]"+_t.getKey()+":"+_t.getType());
        }
        System.out.println("--");
        System.out.println("△△△ContentTemplate.debugTemplate");
    }
    
    
    /**
        テンプレートのテーブル情報を取得する
        @param key テーブルタイトル
        @return テーブルへの参照    存在しない場合はnull
    */
    public QueueTable getTable(CharArray key) {
        if (hashData == null) return null;
        return (QueueTable)hashData.get(key);
    }
    public QueueTable getTable(String str) {
        CharArray key = CharArray.pop(str);
        QueueTable table = getTable(key);
        CharArray.push(key);
        return table;
    }
    /** constructor */
    public ContentTemplate() {
if (debug) System.out.println("■ContentTemplate()");
    }
//
//  public ContentTemplate(Page page) {
//      this.page = page;
//      site_hv = page.getModule().getModuleManager().getHashTemplate();
//  }
//  public ContentTemplate(SessionObject session) {
//      this.orgSession = session;
//      site_hv = session.getModuleManager().getHashTemplate();
//  }
//
    public ContentTemplate(ModuleManager mm) {
        this.moduleManager = mm;
        site_hv = mm.getHashTemplate();
if (debug) {
    int _size = (site_hv == null) ? 0 : site_hv.size();
    System.out.println("■ContentTemplate(mm)"+_size);
}    
    }
    
    public ContentTemplate(ModuleManager mm, HashVector<CharArray,CharArrayQueue> vec) {
        this.moduleManager = mm;
        site_hv = mm.getHashTemplate();
        temp_hv = vec;
if (debug) {
    int _size = (site_hv == null) ? 0 : site_hv.size();
    System.out.println("■ContentTemplate(mm)"+_size+((temp_hv !=null) ? ""+temp_hv.size():""));
}
    }
    public ContentTemplate(HashVector<CharArray,CharArrayQueue> vec) {
        temp_hv = vec;
    }
    
    public void clear() {
        queue.clear();
    }
    /*
        ファイルをパースしてスクリプトを抜き出す
        @return indexの配列
    */
    public IntQueue parse(char[] chars, int start, int middle,int end, boolean shortTag, CharArray parseErrMsg) {
        IntQueue iq = new IntQueue();
if (debug) System.out.println("%%%% parse template start %%%%%%%% ");
        CharArray key = null;
        CharArray mode = CharArray.pop();
        CharArray file = CharArray.pop();
        CharArray data = CharArray.pop();
        CharArray position = CharArray.pop();
        CharArray max = CharArray.pop();
        CharArray param = CharArray.pop();
        CharArray bool_param = CharArray.pop();
        CharArray ch = CharArray.pop();    // タグ内容を移しておく
        if (shortTag) --middle;
        ch.set(chars, start,middle-start).toUpperCase();    // 大文字に変えておく

        int index, index2;
        // キー値取得
        for (index=0;;) {
          index = ch.searchWord(szKey, index);
          if (index >= 0) {
            index2 = ch.indexOf('=',index+szKey.length());
            if (index2 >= 0 && /*ch.isSpace(index+szKey.length(),index2-1) && */middle > index) {
                if (!ch.isSpace(index+szKey.length(),index2-1)) {
                    index += szKey.length();
                    continue;
                }
                key = new CharArray(chars,start+index2+1,middle-start-index2-1); 
                key.trim();
if (debugParse) System.out.print("key["+key+"]");
                for (int i = 0; i < key.length; i++) {
                    if (key.chars[i] <= ' ') {
                        key.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                key.replace("\"","");   // ダブルコーテーションを削除
                key.replace("\'","");   // シングルコーテーションを削除
if (debugParse) System.out.println("->["+key+"]");
            }
          }
          break;
        }
        // ＭＡＸ値取得
        for (index=0;;) {
          index = ch.searchWord(szMax, index);
          if (index >= 0) {
            index2 = ch.indexOf('=',index+szMax.length());
            if (index2 >= 0 && /*ch.isSpace(index+szMax.length(),index2-1) && */middle > index2) {
                if (!ch.isSpace(index+szMax.length(),index2-1)) {
                    index += szMax.length();
                    continue;
                }
                max.set(chars,start+index2+1,middle-start-index2-1); 
                max.trim();
                for (int i = 0; i < max.length; i++) {
                    if (max.chars[i] <= ' ') {
                        max.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                max.replace("\"","");
                max.replace("\'","");
            }
          }
          break;
        }
        // ファイル処理
        for (index=0;;) {
          index = ch.searchWord(szFile, index);
          if (index >= 0) {
            index2 = ch.indexOf('=',index+szFile.length());
            if (index2 >= 0 && /*ch.isSpace(index+szFile.length(),index2-1) && */ middle > index2) {
                if (!ch.isSpace(index+szFile.length(),index2-1)) {
                    index += szFile.length();
                    continue;
                }
                file.set(chars,start+index2+1,middle-start-index2-1); 
                file.trim();
                for (int i = 0; i < file.length; i++) {
                    if (file.chars[i] <= ' ') {
                        file.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                file.replace("\"","");
                file.replace("\'","");
            }
          }
          break;
        }
        // データファイル処理（未完成） 順番定義を可能にする→IntQueue
        for (index=0;;) {
          index = ch.searchWord(szData, index);
          if (index >= 0) {
            index2 = ch.indexOf('=',index+szData.length());
            if (index2 >= 0 && middle > index2) {
                if (!ch.isSpace(index+szData.length(),index2-1)) {
                    index += szData.length();
                    continue;
                }
                data.set(chars,start+index2+1,middle-start-index2-1); 
                data.trim();
                for (int i = 0; i < data.length; i++) {
                    if (data.chars[i] <= ' ') {
                        data.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                data.replace("\"","");
                data.replace("\'","");
                int pos = data.indexOf(":");
                if (pos > 0) {  // ポジション指定あり
                    position.set(data.chars,pos+1,data.length()-pos-1);
                    data.remove(pos);
                    if (debugParse) {
                        System.out.println("◆data:"+data);
                        System.out.println("◆position:"+position);
                    }
                }
            }
          }
          break;
        } // next
        // 表示モード取得
        for (index=0;;) {
          index = ch.searchWord(szMode, index);
          if (index > 0) {
            index2 = ch.indexOf('=',index+szMode.length());
            if (index2 >= 0 && /*ch.isSpace(index+szMode.length(),index2-1) &&*/ middle > index2) {
                if (!ch.isSpace(index+szMode.length(),index2-1)) {
                    index += szMode.length();
                    continue;
                }
                mode.set(chars,start+index2+1,middle-start-index2-1); 
                mode.trim();
if (debug) System.out.print("mode["+mode+"]");
                for (int i = 0; i < mode.length; i++) {
                    if (mode.chars[i] <= ' ') {
                        mode.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                
                //////////////////////
                boolean quot1 = false;
                boolean quot2 = false;
                for (int i = 0; i < mode.length; i++) {
                    if (i == 0) {
                        quot1 = (mode.chars[0] == '\'');
                        quot2 = (mode.chars[0] == '"');
                    } else {
                        if (quot1 && mode.chars[i] == '\'') {
                            mode.length = i;
                            break;
                        }
                        if (quot2 && mode.chars[i] == '"') {
                            mode.length = i;
                            break;
                        }
                    }
                    if (mode.chars[i] <= ' ' && !quot1 && !quot2) {
                        mode.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                
                mode.replace("\"","");
                mode.replace("\'","");
if (debug) System.out.println("->["+mode+"]");
            }
          }
          break;
        } // next;

        param.clear(); bool_param.clear();  // 必要ないはずですが
        
        int paramMode = Template.PARAM;
        //----------------------------------------------
        // 入力パラメータ取得(BOOL)
        for (index = 0;;) {
          index = ch.searchWord(szBool, index);
          if ((index > 0) && bool_param.length()==0) {
            index2 = ch.indexOf('=',index+szList.length());
            if (index2 >= 0 && middle > index2) {
                if (!ch.isSpace(index+szBool.length(),index2-1)) {
                    index += szBool.length();
                    continue;
                }
                paramMode = Template.BOOL;
                bool_param.set(chars,start+index2+1,middle-start-index2-1); 
                bool_param.trim();
                //////////////////////
                boolean quot1 = false;
                boolean quot2 = false;
                for (int i = 0; i < bool_param.length; i++) {
                    if (i == 0) {
                        quot1 = (bool_param.chars[0] == '\'');
                        quot2 = (bool_param.chars[0] == '"');
                    } else {
                        if (quot1 && bool_param.chars[i] == '\'') {
                            bool_param.length = i;
                            break;
                        }
                        if (quot2 && bool_param.chars[i] == '"') {
                            bool_param.length = i;
                            break;
                        }
                    }
                    if (bool_param.chars[i] <= ' ' && !quot1 && !quot2) {
                        bool_param.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                /////////////////////////////////////////////
                bool_param.replace("\"","");
                bool_param.replace("\'","");
            }
          }
          break;
        } // next
        
        //----------------------------------------------
        // 入力パラメータ取得(LIST)
        for (index = 0;;) {
          index = ch.searchWord(szList,index);
          if (index > 0 && param.length()==0) {
            index2 = ch.indexOf('=',index+szList.length());
            if (index2 >= 0 && /*ch.isSpace(index+szList.length(),index2-1) && */middle > index2) {
                if (!ch.isSpace(index+szList.length(),index2-1)) {
                    index += szList.length();
                    continue;
                }
                paramMode = Template.LIST;
                param.set(chars,start+index2+1,middle-start-index2-1); 
                param.trim();
                //////////////////////
                boolean quot1 = false;
                boolean quot2 = false;
                for (int i = 0; i < param.length; i++) {
                    if (i == 0) {
                        quot1 = (param.chars[0] == '\'');
                        quot2 = (param.chars[0] == '"');
                    } else {
                        if (quot1 && param.chars[i] == '\'') {
                            param.length = i;
                            break;
                        }
                        if (quot2 && param.chars[i] == '"') {
                            param.length = i;
                            break;
                        }
                    }
                    if (param.chars[i] <= ' ' && !quot1 && !quot2) {
                        param.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                /////////////////////////////////////////////
                param.replace("\"","");
                param.replace("\'","");
            }
          }
          break;
        } // next;
        
        //----------------------------------------------
        // 入力パラメータ取得(PARAM)
        for (index = 0;;) {
          index = ch.searchWord(szParam, index);    // param を探す
          if (index > 0 &&  param.length()==0) {    // 発見
            index2 = ch.indexOf('=',index+szParam.length());  // = を探す
            if (index2 >= 0 && middle > index2) {   // 発見
                if (!ch.isSpace(index+szParam.length(),index2-1)) {
                    index += szParam.length();
                    continue;
                }
            
                paramMode = Template.PARAM;
                param.set(chars,start+index2+1,middle-start-index2-1); 
                param.trim();
if (debugParse) System.out.println("★☆★param["+param+"] index");
if (debug) System.out.print("mode["+mode+"]");
                //////////////////////
                boolean quot1 = false;
                boolean quot2 = false;
                for (int i = 0; i < param.length; i++) {
                    if (i == 0) {
                        quot1 = (param.chars[0] == '\'');
                        quot2 = (param.chars[0] == '"');
                    } else {
                        if (quot1 && param.chars[i] == '\'') {
                            param.length = i;
                            break;
                        }
                        if (quot2 && param.chars[i] == '"') {
                            param.length = i;
                            break;
                        }
                    }
                    if (param.chars[i] <= ' ' && !quot1 && !quot2) {
                        param.length = i;    // スペースまでサーチ
                        break;
                    }
                }
                /////////////////////////////////////////////
                param.replace("\"","");
                param.replace("\'","");
            }
          }
          break;
        } // next;
        // 入力パラメータ取得(PARAM) 終了
        //----------------------------------------------
        
        if (paramMode == Template.BOOL) {   // BOOL設定のみ
            param.set(bool_param);
            bool_param.clear();
        }
        //------------------------------------------------
        Template template = new Template();
        template.setParamMode(paramMode);
        
        if (key == null || key.trim().length() == 0) {
            CharArray chKey = new CharArray("$tmp$");
            chKey.setAppendMode(true);
            chKey.format(queue.size(),10,3,'0');
if (debugParse) System.out.println("            ->["+chKey+"]");
            template.setKey(chKey);
            template.setQueueMode(Queue.DEQUEUE);
        } else {
            template.setKey(key);
            template.setQueueMode(Queue.DEQUEUE2);
        }
        if (max.length() > 0)  template.setMax(max.getInt());
        if (file.length() > 0) template.setFilename(file);
        if (data.length() > 0) template.setDataFile(data);
        if (position.length() > 0) template.setPosition(position);
        
        if (mode.length() > 0) {
            ch.set(mode).toUpperCase();
            if (ch.equals("AUTO")) {
                template.setMode(Template.AUTO);
            } else if (ch.getBoolean()) {
                template.setMode(Template.TRUE);
            } else {
                template.setMode(Template.FALSE);
            }
        }
        if (param.trim().length() > 0) { 
if (debug || debugTemplate) System.out.println("▼param["+param+"]");
            CharArray tmp = CharArray.pop();
            CharArray _key = CharArray.pop();
            CharArray _func = CharArray.pop();
            CharArray _param = CharArray.pop();
            CharToken token = CharToken.pop();
            CharToken token2 = CharToken.pop();
            CharArrayQueue _funcs   = new CharArrayQueue();
            CharArrayQueue _params = new CharArrayQueue();
            
            token.set(param,";");   // クラスを分離する(;)
            for (int j = 0; j < token.size(); j++) {
                tmp.set(token.get(j));
                if (tmp.trim().length() > 0) {
                    _key.clear(); _funcs.clear(); _params.clear();
                    token2.set(tmp, ".");
                    _key.set(token2.get(0));    // クラス名(@...)を_keyにセット
if (debug || debugTemplate) System.out.println("  _key:"+_key+" token2.size:"+token2.size());
                    
                    if (_key.trim().length() > 0) {
                        if (token2.size() == 1) {
                            _funcs.enqueue(new CharArray());
                            _params.enqueue(new CharArray());
                        } else {
                        for (int i = 1; i < token2.size(); i++) { //関数毎に取り出す(.)
                            _func.clear(); _param.clear();
                            CharArray tmp2 = token2.get(i).trim();  //@@//
                            CharArray _ch = _func;
                            for (int k = 0; k < tmp2.length(); k++) {
                                char c = tmp2.chars[k];
                                if (c == '(')  _ch = _param;
                                else if (c == ')')  break;
                                else _ch.add(c);
                            }
                            // new しないとだめ
                            _funcs.enqueue(new CharArray(_func.trim().toUpperCase()));
                            _params.enqueue(new CharArray(_param.trim()));
                        }
                        }
                        template.addInputFunction(_key, _funcs, _params);
                    }
                }
            }
            CharToken.push(token2);
            CharToken.push(token);
            CharArray.push(_param);
            CharArray.push(_func);
            CharArray.push(_key);
            CharArray.push(tmp);
if (debug || debugTemplate) System.out.println("▲param["+param+"]");
        }
        if (bool_param.trim().length() > 0) {    // ver 0.967以降
if (debug || debugTemplate) System.out.println("★bool_param["+bool_param+"]");
            CharArray tmp = CharArray.pop();
            CharArray _key = CharArray.pop();
            CharArray _func = CharArray.pop();
            CharArray _param = CharArray.pop();
            CharToken token = CharToken.pop();
            CharToken token2 = CharToken.pop();
            CharArrayQueue _funcs   = new CharArrayQueue();
            CharArrayQueue _params = new CharArrayQueue();
            
            token.set(bool_param,";");  // クラスを分離する(;)
            for (int j = 0; j < token.size(); j++) {
                tmp.set(token.get(j));
                if (tmp.trim().length() > 0) {
                    _key.clear(); _funcs.clear(); _params.clear();
                    token2.set(tmp, ".");
                    _key.set(token2.get(0));    // クラス名(@...)を_keyにセット
                    if (_key.trim().length() > 0) {
                        for (int i = 1; i < token2.size(); i++) { //関数毎に取り出す(.)
                            _func.clear(); _param.clear();
                            CharArray tmp2 = token2.get(i).trim();  //@@//
                            CharArray _ch = _func;
                            for (int k = 0; k < tmp2.length(); k++) {
                                char c = tmp2.chars[k];
                                if (c == '(')  _ch = _param;
                                else if (c == ')')  break;
                                else _ch.add(c);
                            }
                            // new しないとだめ
                            _funcs.enqueue(new CharArray(_func.trim().toUpperCase()));
                            _params.enqueue(new CharArray(_param.trim()));
                        }
                        template.addBoolInputFunction(_key, _funcs, _params);
                    }
                }
            }
            CharToken.push(token2);
            CharToken.push(token);
            CharArray.push(_param);
            CharArray.push(_func);
            CharArray.push(_key);
            CharArray.push(tmp);
        }

        if (data.length() > 0) {  // ファイルから読み込む
if (debug) System.out.println("データ["+SystemManager.resourceDirectory+data+"]を読み込みます");
            QueueFile qfile = new QueueFile(SystemManager.resourceDirectory+data);
            qfile.read();
            template.setData(qfile);
            QueueTable table = template.getData();
            if (table != null) {
                CharArray title = table.getTitle();
                if (title.length() > 0) {       // タイトルをキーに
                    if (hashData == null) hashData = new Hashtable<CharArray,QueueTable>();
                    hashData.put(title,table);  // テーブルデータをハッシュに登録する
                }
            }
        }
        if (shortTag) {
            template.setQueueMode(Queue.LOOP);
            if (file.length() > 0) { // ファイルから読み込む
if (debug) System.out.println("ファイル["+SystemManager.resourceDirectory+file+"]を読み込みます");
                CharArrayFile cafile = new CharArrayFile(SystemManager.resourceDirectory+file);
                cafile.read();
                template.set(cafile);
            } else {
                CharArrayQueue value = null;
                if (temp_hv != null) {
                    value = (CharArrayQueue)temp_hv.get(key);
                }
                if ((value == null || value.size()==0) && site_hv != null) {
                    value = (CharArrayQueue)site_hv.get(key); // 変更 <-CharArray
                }
                if (value != null) {
                    template.set(value.peek());
                } else {
                    //SystemManager.log.out(ServletLog.TEMPLATE_ERROR," key="+key);  //エラーじゃなくする。
                }
                //////////////////////////////////////////////////////////////////////////////////
                // 予約テンプレートのマルチ言語対応
                if (key != null) {
                CharArray ca = CharArray.pop();
                Template last = template;
                // これだとエラーになってしまう、、

                HashVector<CharArray,CharArrayQueue> hv = moduleManager.getHashLang();
                
                //if (page != null) hash = page.getModule().getModuleManager().getHashLang();
                //else if (session != null) hash = session..getModuleManager().getHashLang();
                
                for (int i = 0; i < hv.size(); i++) {
                    CharArray langData  = hv.valueElementAt(i).peek();
                    ca.set("."); ca.add(langData);
                    if (key.endsWith(ca)) continue;     // 何もしない
                    ca.insert(0,key);   // 新しいキーを作る
                    
if (debugParse) System.out.println("☆チェック "+ca);
                    CharArrayQueue newvalue = (CharArrayQueue)site_hv.get(ca);
                    if (newvalue != null) { // 予約テンプレートに存在する！
                        Template newtemplate = new Template();
                        newtemplate.copy(template);
                        //newtemplate.setKey(new CharArray(ca));
                        newtemplate.set(newvalue.peek());
                        //queue.enqueue((Object)newtemplate);
                        //iq.enqueue(queue.size()-1);
                        newtemplate.lang = langData.toString();
                        last.next = newtemplate;    // リンクポインタ
                        last = newtemplate;
if (debugParse) System.out.println("☆☆新しいテンプレート "+ca+" を追加します");
//if (debug) System.out.println(newvalue.peek());
                    }
                } // next
                
                CharArray.push(ca);
                }
                /////////////////////////////////////////////////////////////////////////////////
            }
        } else {
            template.set(chars, middle+1, end - middle-1).trim();
        }
        //template.add("\n");
        
        if (queue.enqueue((Object)template)) {  // テンプレートを追加する
            //sts = queue.size() - 1;
            iq.enqueue(queue.size()-1);
        }
        CharArray.push(ch);
        CharArray.push(bool_param);
        CharArray.push(param);
        CharArray.push(max);
        CharArray.push(position);
        CharArray.push(data);
        CharArray.push(file);
        CharArray.push(mode);
if (debug) System.out.println("%%%% parse template end  %%%%%%%% key="+key);

        return iq;
    }

    /*
        パースしてスクリプトを抜き出す（旧メソッド：現在未利用）
        @return indexの配列
    */
/*
*   public IntQueue parse(CharArray key, CharArray param, CharArray parseErrMsg) {
*       IntQueue iq = new IntQueue();
* if (debug || debugMacro) System.out.println("%%%% parse short template start %%%%% "+key+"("+param+")");
*
*       //------------------------------------------------
*       Template template = new Template();
*       template.setParamMode(Template.PARAM);
*       template.setKey(key);
*       template.setQueueMode(Queue.DEQUEUE2);
* //System.out.println("■２■"+key+" "+Queue.DEQUEUE2);
*       
*       if (param.trim().length() > 0) {
* if (debug || debugMacro) System.out.println("param["+param+"]");
*           CharArray tmp = CharArray.pop();
*           CharArray _key = CharArray.pop();
*           CharArray _func = CharArray.pop();
*           CharArray _param = CharArray.pop();
*           CharToken token = CharToken.pop();
*           token.set(param,";");
*           for (int j = 0; j < token.size(); j++) {
*               tmp.set(token.get(j));
*               if (tmp.trim().length() > 0) {
*                   _key.clear(); _func.clear(); _param.clear();
*                   CharArray _ch = _key;
*                   for (int i = 0; i < tmp.length(); i++) {
*                       char c = tmp.chars[i];
*                       if (c == '.')       _ch = _func;
*                       else if (c == '(')  _ch = _param;
*                       else if (c == ')')  break;
*                       else _ch.add(c);
*                   }
*                   if (_key.trim().length() > 0) {
*                       template.addInputFunction(_key, 
*                                                _func.trim().toUpperCase(), 
*                                                _param.trim());
*                   }
*               }
*           }
*           CharToken.push(token);
*           
*           CharArray.push(_param);
*           CharArray.push(_func);
*           CharArray.push(_key);
*           CharArray.push(tmp);
*       }
*       
*       template.setQueueMode(Queue.LOOP);  // 
*       
*       CharArrayQueue value = null;
*       if (temp_hv != null) {  // 追加
*           value = (CharArrayQueue)temp_hv.get(key);
*       }
*       if ((value == null || value.size()==0) && site_hv != null) {
*           value = (CharArrayQueue)site_hv.get(key); // 変更 <-CharArray
*       }
*       if (value != null) {
*           template.set(value.peek());
*       } else {
* System.out.println("マクロの参照するテンプレート["+key+"]がみつからない");
*           //SystemManager.log.out(ServletLog.TEMPLATE_ERROR," key="+key);  //エラーじゃなくする。
*       }
*       //////////////////////////////////////////////////////////////////////////////////
*       // 予約テンプレートのマルチ言語対応   
*
*       CharArray ca = CharArray.pop();
*       Template last = template;
*
*       Hashtable hash = moduleManager.getHashLang();
*       
*       //if (page != null) hash = page.getModule().getModuleManager().getHashLang();
*       //else if (session != null) hash = session..getModuleManager().getHashLang();
*       
*       for (Enumeration e = hash.elements(); e.hasMoreElements();) {
*           CharArray langData  = (CharArray)e.nextElement();
*           ca.set("."); ca.add(langData);
* //if (debug) System.out.println("ca:"+ca+" key["+key+"]");
*           if (key.endsWith(ca)) continue;     // 何もしない
*           ca.insert(0,key);   // 新しいキーを作る
* if (debugParse || debugMacro) System.out.println("☆チェック "+ca);
*           CharArrayQueue newvalue = (CharArrayQueue)site_hv.get(ca);
*           if (newvalue != null) { // 予約テンプレートに存在する！
*               Template newtemplate = new Template();
*               newtemplate.copy(template);
*               //newtemplate.setKey(new CharArray(ca));
*               newtemplate.set(newvalue.peek());
*               //queue.enqueue((Object)newtemplate);
*               //iq.enqueue(queue.size()-1);
*               newtemplate.lang = langData.toString();
*               last.next = newtemplate;    // リンクポインタ
*               last = newtemplate;
* if (debugParse || debugMacro) System.out.println("☆☆新しいテンプレート "+ca+" を追加します");
* //if (debug) System.out.println(newvalue.peek());
*           }
*       } // next
*       
*       CharArray.push(ca);
*
*       if (queue.enqueue((Object)template)) {  // テンプレートを追加する
*           iq.enqueue(queue.size()-1);
*       }
* if (debug || debugMacro) System.out.println("%%%% parse short template end  %%%%%%%% key="+key);
*       return iq;
*   }
*******************************************/
    
    /*
        パースしてスクリプトを抜き出す（新メソッド：short tag 専用）
        @return indexの配列
    */
    public IntQueue parse(CharArray key, CharArrayQueue paramQueue, CharArray parseErrMsg) {
        IntQueue iq = new IntQueue();
if (debug || debugMacro) {
        System.out.println("%%%% parse short template start %%%%% "+key+"(");
        for (int i = 0; i < paramQueue.size(); i++) {
            System.out.print(paramQueue.peek(i));
            if (i < paramQueue.size() - 1) System.out.print("; ");
        }
        System.out.println(")");
}
        //------------------------------------------------
        Template template = new Template();
        template.setParamMode(Template.PARAM);
        template.setKey(key);
        template.setQueueMode(Queue.DEQUEUE2);
        if (paramQueue.size() > 0) {
            CharArray tmp = CharArray.pop();
            CharArray _key = CharArray.pop();
            CharArray _func = CharArray.pop();
            CharArray _param = CharArray.pop();
            for (int j = 0; j < paramQueue.size(); j++) {
                _key.clear(); _func.clear(); _param.clear();
                tmp = paramQueue.peek(j);

                CharArray _ch = _key;
                for (int i = 0; i < tmp.length(); i++) {
                    char c = tmp.chars[i];
                    if (c == '.')       _ch = _func;
                    else if (c == '(')  _ch = _param;
                    else if (c == ')')  break;
                    else _ch.add(c);
                }
                if (_key.trim().length() > 0) { // short tag 用
                    template.addInputFunction(_key.trim(), 
                                             _func.trim().toUpperCase(), 
                                             _param.trim());
                }
            }
            CharArray.push(_param);
            CharArray.push(_func);
            CharArray.push(_key);
            CharArray.push(tmp);
        }
/*
*       if (param.trim().length() > 0) {
* if (debug || debugMacro) System.out.println("param["+param+"]");
*           CharArray tmp = CharArray.pop();
*           CharArray _key = CharArray.pop();
*           CharArray _func = CharArray.pop();
*           CharArray _param = CharArray.pop();
*           CharToken token = CharToken.pop();
*           token.set(param,";");
*           for (int j = 0; j < token.size(); j++) {
*               tmp.set(token.get(j));
*               if (tmp.trim().length() > 0) {
*                   _key.clear(); _func.clear(); _param.clear();
*                   CharArray _ch = _key;
*                   for (int i = 0; i < tmp.length(); i++) {
*                       char c = tmp.chars[i];
*                       if (c == '.')       _ch = _func;
*                       else if (c == '(')  _ch = _param;
*                       else if (c == ')')  break;
*                       else _ch.add(c);
*                   }
*                   if (_key.trim().length() > 0) {
*                       template.addInputFunction(_key, 
*                                                _func.trim().toUpperCase(), 
*                                                _param.trim());
*                   }
*               }
*           }
*           CharToken.push(token);
*           
*           CharArray.push(_param);
*           CharArray.push(_func);
*           CharArray.push(_key);
*           CharArray.push(tmp);
*       }
************/
        
        template.setQueueMode(Queue.LOOP);  // 
        
        CharArrayQueue value = null;
        if (temp_hv != null) {  // 追加
            value = (CharArrayQueue)temp_hv.get(key);
        }
        if ((value == null || value.size()==0) && site_hv != null) {
            value = (CharArrayQueue)site_hv.get(key); // 変更 <-CharArray
        }
        if (value != null) {
            template.set(value.peek());
        } else {
System.out.println("マクロの参照するテンプレート["+key+"]がみつからない");
            //SystemManager.log.out(ServletLog.TEMPLATE_ERROR," key="+key);  //エラーじゃなくする。
        }
        //////////////////////////////////////////////////////////////////////////////////
        // 予約テンプレートのマルチ言語対応   
        if (moduleManager != null) {
            CharArray ca = CharArray.pop();
            Template last = template;

            HashVector<CharArray,CharArrayQueue> hv = moduleManager.getHashLang();
        
            for (int i = 0; i < hv.size(); i++) {
                CharArray langData  = hv.valueElementAt(i).peek();
                ca.set("."); ca.add(langData);
                if (key.endsWith(ca)) continue;     // 何もしない
                ca.insert(0,key);   // 新しいキーを作る
            
if (debugParse || debugMacro) System.out.println("☆チェック "+ca);
                CharArrayQueue newvalue = (CharArrayQueue)site_hv.get(ca);
                if (newvalue != null) { // 予約テンプレートに存在する！
                    Template newtemplate = new Template();
                    newtemplate.copy(template);
                    newtemplate.set(newvalue.peek());
                    newtemplate.lang = langData.toString();
                    last.next = newtemplate;    // リンクポインタ
                    last = newtemplate;
                    last.next = null;
if (debugParse || debugMacro) System.out.println("☆☆新しいテンプレート "+ca+" を追加します");
                }
            } // next
        
            CharArray.push(ca);
        }
        
        if (queue.enqueue((Object)template)) {  // テンプレートを追加する
            iq.enqueue(queue.size()-1);
        }
if (debug || debugMacro) System.out.println("%%%% parse short template end  %%%%%%%% key="+key);
        return iq;
    }
    
//  /* 最大値を取得する */
//  public int getMax(CharArray key) {
//      int sts = -1;
//        Template template = (Template)hash.get(key);
//        if (template != null) sts = template.getMax();
//      return -1;
//  }
    
    
    /*
        描画処理
    */
    public CharArray draw(SessionObject session, int index) {
        return draw(session, index, "");
    }
    public CharArray draw(SessionObject session, int index, String nameSpace) {
        if (debug || debugNamespace) System.out.println("★☆ ContentTemplate draw start "+index+" ["+nameSpace+"]");
        CharArray ch = session.getBuffer(nameSpace);
        Template _template = (Template)queue.peek(index);
        if (_template == null) {
            SystemManager.log.out(ServletLog.TEMPLATE_ERROR," index="+index+" template not found !! size="+queue.size());
        } else {
        Template template = _template.getLangTemplate(session); // ★ループしている
        int queue_mode = template.getQueueMode();
        if (template.getMode() == template.FALSE) {                         // 表示しない
if (debug) System.out.println("☆☆template ["+template.getKey()+"] false");
            // 何も表示しない
        } else if (template.getMode() == template.TRUE) {                          // 無条件に表示する
if (debug) System.out.println("☆☆template ["+template.getKey()+"] true");

            if (queue_mode >= 0) session.setTemplateMode(template.getKey(), queue_mode, nameSpace);
            ObjectQueue param_queue = session.getTemplate(template.getKey(), nameSpace);
            ObjectQueue hash_param_queue = session.getHashTemplate(template.getKey(), nameSpace);
            if ((param_queue == null || param_queue.size() == 0) &&
                 (hash_param_queue == null || hash_param_queue.size() == 0)) {   // セッションにパラメータがない
if (debug) System.out.println("★☆ getTemplate("+template.getKey()+","+nameSpace+") not found!!");
                QueueTable table = template.getData();
                if (table != null && table.getRowCount() > 0) {     // テーブルが定義されている
                    IntQueue queue = template.getPosition();
                    CharArray tmp = template.format(table,0,template.getMax(),queue);
                    ch.add(tmp);
                } else {
                    ch.add(template);
                }
            } else if (hash_param_queue != null && hash_param_queue.size() > 0) { // hashパラメータが存在する 
                int size = hash_param_queue.size();
                if (template.getMax() > 0) {
                    size = Math.min(size,template.getMax());
                }
if (debug) System.out.println("★☆ getTemplate("+template.getKey()+","+nameSpace+") found size="+size);
                for (int i = 0; i < size; i++) {
                    HashParameter hp = (HashParameter)hash_param_queue.peek(i);
                    if (hp == null) {
                        ch.add(template);   //そのまま追加
                    } else if (!hp.isDisplay()) { 
System.out.println("★★表示不可なので出力自体を行わない(1h)");
                            // do nothing
                    } else if (hp.size() ==0 ) {
                        ch.add(template);   //そのまま追加
                    } else {
                        CharArray tmp = template.format(hp);
                        if (debug) System.out.println(tmp);
                        ch.add(tmp);        //フォーマットして追加
                    }
                }
            } else {   // セッションにパラメータが存在する
                int size = param_queue.size();
                if (template.getMax() > 0) {
                    size = Math.min(size,template.getMax());
                }
if (debug) System.out.println("★☆ getTemplate("+template.getKey()+","+nameSpace+") found size="+size);
                for (int i = 0; i < size; i++) {
                    QueueElement parameter = (QueueElement)param_queue.peek(i);
                    if (parameter == null) {
                        ch.add(template);   //そのまま追加
                    } else if (!parameter.isDisplay()) { 
System.out.println("★★表示不可なので出力自体を行わない(1)");
                    } else if (parameter.size() ==0 ) {
                        ch.add(template);   //そのまま追加
                    } else {
                        CharArray tmp = template.format(parameter);
                        if (debug) System.out.println(tmp);
                        ch.add(tmp);        //フォーマットして追加
                    }
                }
            }
        } else {                                                // auto データがあれば表示する
if (debug) System.out.println("☆☆template ["+template.getKey()+"] auto"+"☆☆");
            if (debug) System.out.println(template);
            if (queue_mode >= 0) {
                int org = session.getTemplateMode(template.getKey(), nameSpace);
                if (org <= 0) session.setTemplateMode(template.getKey(), queue_mode, nameSpace);
            }
            ObjectQueue param_queue = session.getTemplate(template.getKey(), nameSpace);
            ObjectQueue hash_param_queue = session.getHashTemplate(template.getKey(), nameSpace);
            if (param_queue == null && hash_param_queue == null) {      // パラメータがない
if (debug) System.out.println("★☆ getTemplate("+template.getKey()+","+nameSpace+") not found!!");
                QueueTable table = template.getData();
                if (table != null && table.getRowCount() > 0) {     // テーブルが定義されている
                    IntQueue queue = template.getPosition();
                    CharArray tmp = template.format(table,0,template.getMax(),queue);
                    ch.add(tmp);
                }   
            } else {                        // パラメータがある
                int size = param_queue == null ? 0 : param_queue.size();
                int size2 = hash_param_queue == null ? 0 :hash_param_queue.size();
if (debug) System.out.println("★☆ getTemplate("+template.getKey()+","+nameSpace+") found size="+size);
                if (debug) System.out.println("◆param_queue size="+size);
                if (size == 0 && size2 == 0) {    // パラメータのサイズが０
                    QueueTable table = template.getData();
                    if (table != null && table.getRowCount() > 0) {     // テーブルが定義されている
                        IntQueue queue = template.getPosition();
    
                        CharArray tmp = template.format(table,0,template.getMax(),queue);
                        ch.add(tmp);
                    }
                } else if (size2 > 0) { // hashParamete
                    if (template.getMax() > 0) size = Math.min(size,template.getMax());
if (debug) System.out.println("★☆ getTemplate("+template.getKey()+","+nameSpace+") size="+size2);
                    
                    CharArray ca = CharArray.pop();
                    boolean display = false;
                    
                    for (int i = 0; i < size2; i++) {
                        HashParameter hp = (HashParameter)hash_param_queue.peek(i);
                        if (hp != null && hp.isDisplay()) display = true;
                        if (hp == null) {
                            ca.add(template);   //そのまま追加
                        } else if (!hp.isDisplay()) {
                            // do nothing
                            //ca.add(template.format(parameter));
                        } else if (hp.size() ==0 ) {
                            ca.add(template);   //そのまま追加
                        } else {
                            CharArray tmp = template.format(hp);
                            ca.add(tmp);        //フォーマットして追加
                        }
                    }
if (debug) System.out.println("◆ display:"+display);
                    if (display) ch.add(ca);
                    
                    CharArray.push(ca);
                } else {                // Parameter
                    if (template.getMax() > 0) {
                        size = Math.min(size,template.getMax());
                    }
if (debug) System.out.println("★☆ getTemplate("+template.getKey()+","+nameSpace+") size="+size);
                    
                    CharArray ca = CharArray.pop();
                    boolean display = false;
                    
                    for (int i = 0; i < size; i++) {
                        //Parameter parameter = (Parameter)param_queue.peek(i);
                        QueueElement parameter = (QueueElement)param_queue.peek(i);
                        if (parameter != null && parameter.isDisplay()) display = true;
                        if (parameter == null) {
                            ca.add(template);   //そのまま追加
                        } else if (!parameter.isDisplay()) {
                        } else if (parameter.size() ==0 ) {
                            ca.add(template);   //そのまま追加
                        } else {
                            CharArray tmp = template.format(parameter);
                            //if (debug) System.out.println(tmp);
                            ca.add(tmp);        //フォーマットして追加
                        }
                    }
if (debug) System.out.println("◆ display:"+display);
                    if (display) ch.add(ca);
                    
                    CharArray.push(ca);
                    
                }
            }
        } // endif
        }
        if (debug) System.out.println("Template draw end-----------------");
        return ch;
    }

    /** 
        テンプレート用の file とデータが存在すれば読み直す
        (htmlは読み直さない）
    */
    public void refresh() {
if (debug) System.out.println("ContentTemlate refresh size="+queue.size());
        for (int i = 0; i < queue.size(); i++) {
            Template template = (Template)queue.peek(i);
            CharArray file = template.getFileName();
            if (file != null && file.length() > 0) {
if (debug) System.out.println("ファイル["+SystemManager.resourceDirectory+file+"]を読み込み直します");
                CharArrayFile cafile = new CharArrayFile(SystemManager.resourceDirectory+file);
                cafile.read();
                template.set(cafile);
            }
            CharArray data = template.getDataFile();
            if (data != null && data.length() > 0) {
if (debug) System.out.println("データ["+SystemManager.resourceDirectory+data+"]を読み込み直します");
                QueueFile qfile = new QueueFile(SystemManager.resourceDirectory+data);
                qfile.read();
                template.setData(qfile);
                QueueTable table = template.getData();
                if (table != null) {
                    CharArray title = table.getTitle();
                    if (title.length() > 0) {       // タイトルをキーに
                        if (hashData == null) hashData = new Hashtable<CharArray,QueueTable>();
                        hashData.remove(title);
                        hashData.put(title,table);  // テーブルデータをハッシュに登録する
                    }
                }
            }
        }
    }
    
    /* 共通テンプレートが存在するか
    */
    public boolean existCommonTemplate(CharArray key) {
        CharArrayQueue value = null;
        if (temp_hv != null) {
            value = (CharArrayQueue)temp_hv.get(key);
        }
        if ((value == null || value.size()==0) && site_hv != null) {
            value = (CharArrayQueue)site_hv.get(key);
        }
        return (value != null);
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
        @return 発見
    */
    
    public boolean existFunc(String key_name, String func_name, int mode) {
        boolean found = false;
        for (int i = 0; i < queue.size(); i++) {
            Template template = (Template)queue.peek(i);
            if (template.existFunc(key_name,func_name,mode)) {
                found = true;
                break;
            }
        }
if (debugSearch) {
        System.out.println("【検索】existFunc("+key_name+","+func_name+","+mode+")->found:"+found);
}
        return found;
    }
//    
//  /* テスト用 */
//  public CharArray draw(CharArray key) {
//      return draw(new CharArray(), key);
//  }
//  
//  /* テスト用 */
//  public CharArray draw(CharArray ch, CharArray key) {
//      if (debug) System.out.println("Template draw start "+key+"-----");
//      Template template = (Template)hash.get(key);
//      if (template == null) {
//          System.out.println("key="+key+" template = null !!");
//      } else {
//          if (debug) System.out.println(template);
//          ch.add(template);
//      }
//      
//      if (debug) System.out.println("Template draw end-----------");
//      return ch;
//  }


}

//
// [end of ContentTemplate.java]
//

