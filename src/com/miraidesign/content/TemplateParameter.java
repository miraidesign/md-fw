//------------------------------------------------------------------------
// @(#)TemplateParameter.java
//              テンプレート用パラメータを保管する
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content;

import java.util.Hashtable;
import java.util.Enumeration;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.ParametersQueue;
import com.miraidesign.util.QueueElement;
import com.miraidesign.util.ObjectQueue;

/**
 *  TemplateParameter テンプレートパラメータを保管する
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class TemplateParameter extends Hashtable {
    static private boolean debug = (SystemConst.debug && false);    // デバッグ表示
    static private boolean debugBlock = (SystemConst.debug && false);
    static private boolean debugTemplate = (SystemConst.debug && false);

    private String nameSpace = "";   // ネームスペース

    /** ネームスペースを設定する（デフォルト""） 
        @param str ネームスペース
    */
    public void setNameSpace(String str) { this.nameSpace = str;}
    public String getNameSpace() { return nameSpace;}
    
    private  Hashtable<CharArray,ParametersQueue> hash; // テンプレートのパラメータを保管
    
    //------------------------
    // constructor
    //------------------------
    /** デフォルトコンストラクタ */
    public TemplateParameter() { super();}

    /** 指定ネームスペースで生成する 
        @param nameSpace ネームスペース
    */
    public TemplateParameter(String nameSpace) {
        super();
        this.nameSpace = nameSpace;
    }
    // copy 後で実装すること（コピーコンストラクタも）
    //public void copy(TemplateParameter from) {
    // 
    //}
    
    //-----------------------------------------------
    // method
    //-----------------------------------------------
    
    /** 
        テンプレートモードをセットする
        @param key キー
        @param mode ParametersQueue:LOOP/DEQUEUE
    */
    public void setTemplateMode(CharArray key, int mode) {
        if (hash == null) hash = new Hashtable<CharArray,ParametersQueue>();
        ParametersQueue queue = (ParametersQueue)hash.get(key);
        if (queue == null) {
            queue = new ParametersQueue();
            hash.put(new CharArray(key),queue);
        }
if (debugBlock) System.out.println("SessionObject#setTemplateMode("+key+","+mode+")");
        queue.setMode(mode);
    }
    public void setTemplateMode(String str, int mode) {
        CharArray key = CharArray.pop(str);
        setTemplateMode(key,mode);
        CharArray.push(key);
    }
    /** 
        テンプレートモードを取得する
        @param key キー
        @return テンプレートモード
    */
    public int getTemplateMode(CharArray key) {
        if (hash == null) return 0;
        ParametersQueue queue = (ParametersQueue)hash.get(key);
        if (queue == null) return 0;
        return queue.getMode();
    }
    public int getTemplateMode(String str) {
        CharArray key = CharArray.pop(str);
        int ret = getTemplateMode(key);
        CharArray.push(key);
        return ret;
    }
    
    /** 
        テンプレートパラメータをセットする
        @param key テンプレートキー
    */
    public void setTemplate(String key) {
        setTemplate(key,(QueueElement)null);
    }
    public void setTemplate(String str, QueueElement param) {
        CharArray key = CharArray.pop(str);
        setTemplate(key,param);
        CharArray.push(key);
    }
    public void setTemplate(CharArray key) {
        setTemplate(key, (QueueElement)null);
    }
    /** 
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param param パラメータ
    */
    public void setTemplate(CharArray key, QueueElement param) {
        setTemplate(key,param,0);
    }
    
    /** 
        テンプレートパラメータをセットする
        @param key   テンプレートキー
        @param index セットブロック位置
    */
    public void setTemplate(String key,int index) {
        setTemplate(key,(QueueElement)null,index);
    }
    public void setTemplate(String str, QueueElement param, int index) {
        CharArray key = CharArray.pop(str);
        setTemplate(key,param, index);
        CharArray.push(key);
    }
    public void setTemplate(CharArray key, int index) {
        setTemplate(key, (QueueElement)null, index);
    }
    /** 
        テンプレートパラメータをセットする<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void setTemplate(String key, String str) {
        setTemplate(key, new QueueElement().add(str), 0);
    }
    /** 
        テンプレートパラメータをセットする<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void setTemplate(String key, CharArray str) {
        setTemplate(key, new QueueElement().add(str), 0);
    }
    /** 
        テンプレートパラメータをセットする<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void setTemplate(CharArray key, String str) {
        setTemplate(key, new QueueElement().add(str), 0);
    }
    /** 
        テンプレートパラメータをセットする<br>
        (Parameterは自動生成される)
        @param key   テンプレートキー
        @param str   セットする文字列
    */
    public void setTemplate(CharArray key, CharArray str) {
        setTemplate(key, new QueueElement().add(str), 0);
    }
    
    /***
    private CharArray getLangKey(CharArray _key) {
        CharArray key = CharArray.pop(_key);
        CharArray langData = getLangData();
        if (langData != null && langData.length() > 0) {
            key.add("."); key.add(langData);
            ContentTemplate ct = forwardPage.getTemplate();
            if (ct.getTemplate(key) == null) {  // マルチ言語対応のテンプレートが存在しない
                key.set(_key);
            }
        }
        return key;
    }
    **/
    /** 
        テンプレートパラメータをセットする
        @param key テンプレートキー
        @param param パラメータ
        @param index ブロックインデックス
    */
    public void setTemplate(CharArray key, QueueElement param, int index) {
        if (index >= 0) {
            if (hash == null) hash = new Hashtable<CharArray,ParametersQueue>();
            // 予約テンプレートのマルチ言語対応
            //CharArray key = getLangKey(_key);
            ParametersQueue queue = (ParametersQueue)hash.get(key);
            if (queue == null) {
                queue = new ParametersQueue();
                //----------------------------
                //queue.setMode(Queue.DEQUEUE);
                //----------------------------
                hash.put(new CharArray(key),queue);
            }
            if (index >= queue.size()) queue.setSize(index+1);
            ObjectQueue obj = (ObjectQueue)queue.peek(index);
            if (obj != null) {
                obj.clear();
                obj.enqueue(param);
            } else {
                System.out.println("setTemplate: unknown Error!");
            }
            //CharArray.push(key);
        }
    }
    
    /** 
        テンプレートパラメータを追加する
        @param key テンプレートキー
    */
    public void addTemplate(String key) {
        addTemplate(key,null);
    }
    /** 
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param param パラメータリスト
    */
    public void addTemplate(String key, QueueElement param) {
        CharArray _key = CharArray.pop(key);
        addTemplate(_key,param);
        CharArray.push(_key);
    }
    /** 
        テンプレートパラメータを追加する
        @param key テンプレートキー
    */
    public void addTemplate(CharArray key) {
        addTemplate(key, null);
    }
    /** 
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param param パラメータ
    */
    public void addTemplate(CharArray key, QueueElement param) {
        addTemplate(key, param, 0);
    }
    /**
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param index ブロックインデックス
    */
    public void addTemplate(String key, int index) {
        addTemplate(key,null, index);
    }
    /** 
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param param パラメータ
        @param index ブロックインデックス
    */
    public void addTemplate(String key, QueueElement param, int index) {
        CharArray _key = CharArray.pop(key);
        addTemplate(_key,param, index);
        CharArray.push(_key);
    }
    /** 
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param index ブロックインデックス
    */
    public void addTemplate(CharArray key, int index) {
        addTemplate(key, null, index);
    }
    
    /** 
        テンプレートパラメータを追加する
        @param key テンプレートキー
        @param param パラメータ
        @param index ブロックインデックス
    */
    public void addTemplate(CharArray key, QueueElement param, int index) {
        if (index >= 0) {
            if (hash == null) hash = new Hashtable<CharArray,ParametersQueue>();
            // 予約テンプレートのマルチ言語対応
            //CharArray key = getLangKey(_key);  // やめ
            
            ParametersQueue queue = (ParametersQueue)hash.get(key);
            if (queue == null) {
                queue = new ParametersQueue();
                //----------------------------
                //queue.setMode(Queue.DEQUEUE);
                //----------------------------
                hash.put(new CharArray(key),queue);
            }
            if (index >= queue.size()) queue.setSize(index+1);
            ObjectQueue obj = (ObjectQueue)queue.peek(index);
            if (obj != null) {
                obj.enqueue(param);
            } else {
                System.out.println("setTemplate: unknown Error!");
            }
            //CharArray.push(key);
        }
        //if (debug) System.out.println(" set key="+key+" param="+(param != null));
    }
    /** 
        テンプレートパラメータを取得する
        @param key テンプレートキー
        @return ObjectQueue
    */
    public ObjectQueue getTemplate(CharArray key) {
        if (hash == null || key == null) return null;
        ParametersQueue queue = (ParametersQueue)hash.get(key);
        if (queue == null) return null;
        return queue.get();
    }
    
    /**
        テンプレートのstatic化
        @param key キー
        @param mode true:static化する false:通常モード
    */
    public void setStaticTemplate(CharArray key, boolean mode) {
        if (hash != null) {
            //CharArray key = getLangKey(_key);
            ParametersQueue queue = (ParametersQueue)hash.get(key);
            if (queue != null) {
                queue.setStaticMode(mode);
            }
            //CharArray.push(key);
        }
    }
    /**
        テンプレートのstatic化
        @param key キー
        @param mode true:static化する false:通常モード
    */
    public void setStaticTemplate(String key, boolean mode) {
        CharArray ch = CharArray.pop(key);
        setStaticTemplate(ch, mode);
        CharArray.push(ch);
    }
    /**
        テンプレートのstatic化
        @param key キー
    */
    public void setStaticTemplate(CharArray key) {
        setStaticTemplate(key,true);
    }
    /**
        テンプレートのstatic化
        @param key キー
    */
    public void setStaticTemplate(String key) {
        setStaticTemplate(key,true);
    }
    
    /**
        テンプレートパラメータを全て削除する
    */
    public void clearAllTemplate() {
        if (hash != null) {
            hash.clear();
        }
    }
    /**
        通常テンンプレートパラメータを削除する
    */
    public void clearTemplate() {
        if (hash != null) {
            for (Enumeration e = hash.keys();e.hasMoreElements();) {
                CharArray key = (CharArray)e.nextElement();
                ParametersQueue queue = (ParametersQueue)hash.get(key);
                if (queue == null || !queue.getStaticMode()) {
                    hash.remove(key);
                }
            }
        }
    }
    /**
        staticテンンプレートパラメータを削除する
    */
    public void clearStaticTemplate() {
        if (hash != null) {
            for (Enumeration e = hash.keys();e.hasMoreElements();) {
                CharArray key = (CharArray)e.nextElement();
                ParametersQueue queue = (ParametersQueue)hash.get(key);
                if (queue != null && queue.getStaticMode()) {
                    hash.remove(key);
                }
            }
        }
    }
    /**
        テンプレートパラメータを削除する
        @param key テンプレートキー
    */
    public void clearTemplate(CharArray key) {
        if (hash != null) {
            //CharArray key = getLangKey(_key);
            hash.remove(key);
            //CharArray.push(key);
        }
    }
    /**
        テンプレートパラメータを削除する
        @param key テンプレートキー
    */
    public void clearTemplate(String key) {
        CharArray ch = CharArray.pop(key);
        clearTemplate(ch);
        CharArray.push(ch);
    
    }
    /** デバッグ表示 */
    public void debugTemplate() {
        debugTemplate(null);
    }
    /*
    public void debugTemplate(String str) {
        if (hash != null) {
            if (str != null) System.out.print(str);
            System.out.println("--template---"+hash.size());
            for (Enumeration e=hash.keys(); e.hasMoreElements();) {
                CharArray key = (CharArray)e.nextElement();
                System.out.print("["+key+"]");
                ParametersQueue queue = (ParametersQueue)hash.get(key);
                if (queue == null) {
                    System.out.println("<null>");
                } else {
                    if (queue.getStaticMode()) System.out.print("(*)");
                    for (int i = 0; i < queue.size(); i++) {
                        ObjectQueue p = (ObjectQueue)queue.peek(i);
                        for (int j = 0; j < p.size(); j++) {
                            System.out.print(((QueueElement)p.peek(j)).peek()+":");
                        }
                        System.out.print("/");
                    }
                    System.out.println("");
                }
            }
            System.out.println("-------------");
        }
    }
    */
    /** デバッグ表示 
        @param str 付加文字列
    */
    public void debugTemplate(String str) {
        if (hash != null) {
            
            if (str == null) str = "";
            str+= "("+nameSpace+")";
            //System.out.println("--template---"+hash.size());
            for (Enumeration e=hash.keys(); e.hasMoreElements();) {
                CharArray key = (CharArray)e.nextElement();
                System.out.print(str+"["+key+"]");
                ParametersQueue queue = (ParametersQueue)hash.get(key);
                if (queue == null) {
                    System.out.println("<null>");
                } else {
                    if (queue.getStaticMode()) System.out.print("(*)");
                    for (int i = 0; i < queue.size(); i++) {
                        ObjectQueue p = (ObjectQueue)queue.peek(i);
                        for (int j = 0; j < p.size(); j++) {
                            System.out.print(((QueueElement)p.peek(j)).peek()+":");
                        }
                        System.out.print("/");
                    }
                    System.out.println("");
                }
            }
            //System.out.println("-------------");
        }
    }
    
    //----------------------------------------
    // シリアライズ用
    //----------------------------------------
    /**
    public  void writeObject(DataOutput out) throws IOException {
        if (out != null) {
            super.writeObject(out);
        }
    }
    public  void readObject(DataInput in) throws IOException {
        if (in != null) {
            super.readObject(in);
        }
    }
    **/
}

//
// [end of TemplateParameter.java]
//

