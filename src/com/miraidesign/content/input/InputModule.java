//------------------------------------------------------------------------
//    InputModule.java
//          システム関数  @module で呼ばれる
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
// １インスタンスのみ生成される
//    

package com.miraidesign.content.input;

//import java.text.SimpleDateFormat;

import com.miraidesign.content.ContentParser;
import com.miraidesign.session.SessionObject;
import com.miraidesign.servlet.ContentRenderer;
import com.miraidesign.servlet.ModuleServlet;
import com.miraidesign.servlet.PageServlet;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.ModuleManager;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.ObjectQueue;
import com.miraidesign.util.Parameter;
import com.miraidesign.util.QueueTable;

/**
    システム関数（モジュール呼び出しクラス）
*/
public class InputModule extends InputItem {
    private static boolean debug = false;
    private static boolean debugDisp = false;
    private static boolean debugPaging = false;
    //private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //private static SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    //private static SimpleDateFormat sdfJ = new SimpleDateFormat("MM年dd日");
    
    //---------------------------------------------
    // 
    //---------------------------------------------
    
    //-------------------
    // constructor
    //-------------------
    public InputModule() {
        type.set("MODULE");
    }
    
    //-------------------
    // method
    //-------------------
    public void copy(InputModule from) {
        // do nothing
    }
    
    /** デフォルト関数 */
    public CharArray get() {
        return getLink();
    }
    
    
    // 何をするんだっけ？
    public void set(CharArray ch) {
        
    }

    /** パラメータを解析して、 Contentを取り出す 
        @param param パラメータ
        @param session SessionObject
        @return null 存在しない
    */
    public static PageServlet getPage(CharArrayQueue param, SessionObject session) {
        PageServlet page = null;
        do {
            if (param == null || param.size() == 0) break;
            CharArray ch = param.peek();    if (ch.trim().length() == 0) break;
            int index = ch.indexOf(':');    if (index <= 0) break;
            
            String moduleName = ch.substring(0,index);
            String pageName   = ch.substring(index+1);
            
            if (debug) System.out.println("InputModule#getPage("+moduleName+"."+pageName+")");
            if (session == null) break;
            ModuleManager mm = session.getModuleManager();  if (mm == null) break;
            ModuleServlet module = (ModuleServlet)(mm.getModule(moduleName));
            if (module == null) break;
            page = (PageServlet)module.get(pageName);
        } while (false);
        return page;
    }

    /* リンクを求める */
    public CharArray getLink() {
if (debug) System.out.println("▼getLink() 未動作です！！");
        CharArray ch = new CharArray();
/*        
        ch.add("<a href=\"");
        ch.add("");               // コンテンツを表示するPageServletの URL(session付)
        
        ch.add("?id=");
        ch.format(id);
        ch.add("\"");
        ch.add(">");
        ch.add("リンクタイトル");
        ch.add("</a>\n");
*/
if (debug) System.out.println("▲getLink()"+ch);
        return ch;
    }
    
    CharArray noLink = new CharArray("このリンクはありません"); // これじゃ多分だめ

    public CharArray getLink(CharArrayQueue param, SessionObject session) {
if (debug) System.out.println("▼▼getLink(param)");

        PageServlet page = getPage(param, session);
        
        CharArray title = (page != null) ? page.getTitleName() : noLink;
if (debug) System.out.println("titile["+title+"]");
        CharArray ch = new CharArray();
        ch.add("<a href=\"");
        ch.add(getURL(param, session));
        ch.add("\">");
        if (title.length() > 0) ch.add(title);
        else                    ch.add("-- no title --");
        ch.add("</a>\n");
if (debug) System.out.println("▲▲getLink(param)"+ch);
        return ch;
    }
    public CharArray getURL(CharArrayQueue param, SessionObject session) {
if (debug) System.out.println("▼▼getURL(param)");
        CharArray ch = new CharArray();
        PageServlet page = getPage(param, session);
        if (page != null) {
            if (SystemManager.convertContextPath == 1) {
                //int sessionID = session.getSessionID();
                //SiteMapping map = mm.getSiteMapping();
                //map.getSiteParameter(ch);
                
                //page.getURL(session);
                ch.add(page.getURL(session));
            } else {
                ch.add(page.getURL(session));
                
            }
        } else {
            
        }
if (debug) System.out.println("url["+ch+"]");

if (debug) System.out.println("▲▲getURL(param)"+ch);
        return ch;
    }
    
    /* タイトルを求める */
    CharArray noTitle = new CharArray("このタイトルはありません"); // これじゃ多分だめ
    public CharArray getTitle(CharArrayQueue param, SessionObject session) {
if (debug) System.out.println("▼▼getTitle(param)");
        PageServlet page = getPage(param, session);
        CharArray title = (page != null) ? page.getTitleName() : noTitle;
if (debug) System.out.println("titile["+title+"]");
        CharArray ch = new CharArray(title);

if (debug) System.out.println("▲▲getTitle(param)"+ch);
        return ch;
    }
    
    // ページを表示する（合成モードのときのみ有効）
    public CharArray getDisp(CharArrayQueue param, SessionObject session) {
if (debug || debugDisp) System.out.println("▼▼▼▼▼getDisp() session:"+(session != null));
        CharArray ch = new CharArray();
        PageServlet page = getPage(param, session);
        do {
            if (page == null) break;
            
            ObjectQueue queue = page.forward(session);
            if (queue == null || queue.size() == 0) break;
            
            PageServlet page0 = (PageServlet)queue.peek(0);  // 先頭ページ
            
            ContentRenderer renderer = SystemManager.contentRenderer;
            boolean isComplex =  (renderer != null && renderer.isComplexPage(queue)); // 合成モード？
            if (!isComplex) {
                if (debug) System.out.println("InputModule#getDisp() 合成モードでありません、が続行します");
            }
            
            CharArrayQueue namespaceQueue = renderer.getNamespaceQueue(queue);
            
            CharArray defaultNamespace = session.getDefaultNamespace();    // 念のため
            
            // 必要か？
            CharArray title = page0.getTitleName();
            String contentType = session.getContentType();
            String charSet = session.getCharSet();
            //-------------------  処理開始
            if (debug) System.out.println("★InputModule:合成ページ表示を行います: title="+title);
            
if (debugPaging) System.out.println("★★★★★★★★★★★★★★");
            CharArray chPagingURL = CharArray.pop();;
            CharToken tokenURI = session.getDirectoryListFromURI();
            for (int i = 0; i < tokenURI.size(); i++) {
                CharArray dir = tokenURI.get(i);
                if (!dir.startsWith("tm=") && !dir.startsWith("start=") && dir.length()>0) {
                    chPagingURL.add('/');
                    chPagingURL.add(dir);
                }
if (debugPaging) System.out.println("["+(i+1)+"/"+tokenURI.size()+"]◎dir:"+dir+"  URL:"+chPagingURL);
            }

            String pagingURL = chPagingURL.toString();
            CharArray.push(chPagingURL);
if (debugPaging) System.out.println("pagingURL:"+pagingURL);
if (debugPaging) System.out.println("★★★★★★★★★★★★★★");
            
            for (int i = 0; i < queue.size(); i++) {
                PageServlet _page = (PageServlet)queue.peek(i);
                if (_page != null) {
                    if (namespaceQueue!= null) {
                        CharArray _ns = namespaceQueue.peek(i);
                        session.setDefaultNamespace(_ns);
                        if (_ns.length()>0) {
                            session.clearBuffer(_ns);
                            session.clearAllBlock(_ns.toString());
                            session.clearTemplate(_ns);    //@@// どうしよう
                        }
                    }
                    String org = _page.getPagingURL();   //@@// debug
if (debugPaging) System.out.println("InputModule(1)");
                    _page.setPagingURL(pagingURL); //@@// debug
                    
if (debug) System.out.println("★InputModule draw i="+i+"("+namespaceQueue.peek(i)+")");
                    _page.makePage(session);             //@@// タイミングはここで大丈夫か？
                    ContentParser parser = _page.getParser();
                    if (parser != null && parser.isEnabled()) {
                        //_page.showTemplate(session); // ナビ・ガイダンス等の表示
                        if (debug) {
                            System.out.println("_parser.draw()◇◆");
                        }
                        //-----------------------------------------
                        // module.disp の時も有効にする
                        parser.setTemplate(session);
                        //-----------------------------------------
                        
                        ch.add(parser.draw(session));
                    } else {
                        if (debug) {
                            System.out.println("_page.draw()◇◆");
                            if (parser == null) {
                                System.out.println(" parser == null!!");
                            } else {
                                System.out.println(" parser.isEnabled()="+parser.isEnabled());
                                
                            }
                        }
                        _page.draw(session); //  通常はオーバーライドのみ利用される
                    }
if (debugPaging) System.out.println("InputModule(2)");
                    _page.setPagingURL(org); //@@// debug
                }
            }
            session.setDefaultNamespace(defaultNamespace);
            
            //----
        } while (false);
if (debug || debugDisp) System.out.println("▲▲▲▲▲getDisp() ch.length()="+ch.length());
        return ch;
    }
    
    // 指定ページのDAO をクリアする
    public CharArray clearTable(CharArrayQueue param, SessionObject session) {
        CharArray ch = new CharArray();
        PageServlet page = getPage(param, session);
        if (page != null && session != null) {
            if (session.removeDAO(page)) ch.add("OK");
        }
        return ch;
    }
    //-------------------------------------------------------------------------
    // PageMax を指定する
    //-------------------------------------------------------------------------
    public CharArray setPageMax(CharArrayQueue param, SessionObject session) {
        CharArray ch = new CharArray();
        PageServlet page = getPage(param, session);
        if (page != null && session != null) {
if (debug || debugPaging) System.out.println("▼▼▼▼▼▼▼▼ SetPageMax:"+page.hashCode());
            int max = 0;
            if (param.size() > 1) max = CharArray.getInt(param.peek(1));
if (debugPaging) System.out.println("max="+max);
            if (max > 0) {
                int hashcode = page.hashCode();
                session.setParameter(""+hashcode+"_size",""+max);
                String _start = session.getParameter(""+hashcode+"_start");

                if (_start.length() == 0) {
                    com.miraidesign.data.DataAccessObject dao = session.getDAO(page);
                    if (dao.getTimeStamp() == 0) {
                        session.removeDAO(page);
                    }
                    session.setParameter(""+hashcode+"_start",""+0);
                } else {    // start パラメータが存在する（キャッシュ利用）
                    // do nothing?
                }

                page.setPageMax(max);
                page.setPageMaxMobile(max);
                
                session.setParameter("page@hash",""+hashcode);
                page.makePage(session);
                session.removeParameter("page@hash");
                session.setParameter("cache@hash",""+hashcode);
                
                // ページングフォーマット文字列変更
                String _paging = session.getParameter(""+hashcode+"_page");
                
                CharArray _prev = null;
                CharArray _next = null;
                CharArray _sepa = null;
                
                if (param.size() > 2) _prev = param.peek(2);
                if (param.size() > 3) _sepa = param.peek(3);
                if (param.size() > 4) _next = param.peek(4);
                
                if ((_prev != null && _prev.length() > 0) ||
                    (_next != null && _next.length() > 0) || 
                    (_sepa != null && _sepa.length() > 0)) {
                    CharArray _p = CharArray.pop(_paging);
                    
                    //int index = 0;        // 重複、2重変換が考慮されていない。注意！
                    if (_prev != null && _prev.length() > 0) {
                        //int index = _p.indexOf(_prev);
                        _p.replace(page.getPrevPageStr(), _prev);
                    }
                    if (_next != null && _next.length() > 0) {
                        _p.replace(page.getNextPageStr(), _next);
                    }
                    if (_sepa != null && _sepa.length() > 0) {
                        _p.replace(page.getSeparatePageStr(), _sepa);
                    }
                    
                    if (_p.length()>0 && !_p.equals(_paging)) session.setParameter(""+hashcode+"_page", ""+_p);
                    CharArray.push(_p);
if (debugPaging) System.out.println("(2)_paging="+session.getParameter(""+hashcode+"_page"));
                }
            }
            ch.format(page.getPageMax());
        } // endif
        if (debugPaging) session.debugParameters();
        if (debug || debugPaging) System.out.println("▲▲▲▲▲▲▲▲ SetPageMax:"+ch);
        return ch;
    }
    
    // PageMax を取得する
    public CharArray getPageMax(CharArrayQueue param, SessionObject session) {
        CharArray ch = new CharArray();
        PageServlet page = getPage(param, session);
        if (page != null && session != null) {
            ch.format(page.getPageMax());
        }
        return ch;
    }
    
    
    // カラムリストを出力する
    public Parameter getTable(CharArrayQueue param, Parameter p, SessionObject session) {
if (debug) System.out.println("▼▼▼▼▼getTable() session:"+(session != null));
        PageServlet page = getPage(param, session);
        do {
            if (page == null || param == null || param.size() <2) break;
            
            CharArray column = param.peek(1);
            if (column == null || column.trim().length()==0) break;
            
            int total = 0;
            int start = 0;
            int size  = 0;
            if (param.size() > 2) total = CharArray.getInt(param.peek(2));
            if (param.size() > 3) start = CharArray.getInt(param.peek(3));
            if (param.size() > 4) size  = CharArray.getInt(param.peek(4));
            
            QueueTable table = page.getTable(session);
            if (table == null) break;
            
            if (param.size() <= 3) {    // paging対応追加
                int hashcode = page.hashCode();
                String _start = session.getParameter(""+hashcode+"_start");
                String _size  = session.getParameter(""+hashcode+"_size");
if (debug) System.out.println(" ページング対応を行います _start="+_start+" _size="+_size);
                if (_start != null && _start.length() > 0 &&
                    _size  != null && _size.length() > 0 && 
                    CharArray.getInt(_start) >= 0 && CharArray.getInt(_size) > 0) {
                    start = CharArray.getInt(_start);
                    size  = CharArray.getInt(_size);
                    if (debug) System.out.println(" start="+start+" size="+size);
                }
            }
            
            int MAX = table.getRowCount();
            if (total > 0 && total < MAX) MAX = total;
            int count=0;
            for (int i = start; i < MAX; i++) {
                CharArray ch = table.get(i,column);
                p.add(ch);
                if (size > 0 && ++count >= size) break;
            }
            
        } while (false);
if (debug) System.out.println("▲▲▲▲▲getTable()");
        return p;
    }
    
    // パラメータリストを出力する
    public Parameter getParam(CharArrayQueue param, Parameter p, SessionObject session) {
if (debug) System.out.println("▼▼▼▼▼getParam() session:"+(session != null));
        PageServlet page = getPage(param, session);
        do {
            if (page == null) {
                p.add("");
                break;
            }
            
            CharArray key = param.peek(1);
            if (key == null || key.trim().length()==0) {
                p.add("");
                break;
            }
            
            HashParameter hp = page.getParameter(session);
            if (hp == null) {
                p.add("");
                break;
            }
            
            CharArray ch = hp.get(key);
            p.add(ch);
            
            
        } while (false);
if (debug) System.out.println("▲▲▲▲▲getTable()");
        return p;
    }
    
    /*
        関数呼び出し
        @param func 関数名 
        @param param 関数パラメータ
    */
    public CharArray getFunc(CharArray func, CharArrayQueue param) {
        return getFunc(func, param, 0);
    }
    public CharArray getFunc(CharArray func, CharArrayQueue param, int state) {
        return getFunc(func, param, state, null);
    }
    public CharArray getFunc(CharArray func, CharArrayQueue param, int state, SessionObject session) {
if (debug) System.out.println("▽▽InputModule#getFunc▽▽");
        CharArray ch = null;
        if (func == null) {
            ch = get();
        } else {
            func.trim().toLowerCase();
            if (func.length() == 0)              ch = get();
            else if (func.equals("link"))        ch = getLink(param, session);
            else if (func.equals("url"))         ch = getURL(param, session);
            else if (func.equals("title"))       ch = getTitle(param, session);
            else if (func.equals("disp"))        ch = getDisp(param, session);
            else if (func.equals("view"))        ch = getDisp(param, session);
            else                                 ch = get();
        }
if (debug) System.out.println("△△InputModule#getFunc△△");
        return ch;
    }
    /*
        関数呼び出し（CMSが利用するのはこちらのみ）
        @param func  関数名 
        @param param 関数パラメータ
        @param p     戻り値用
        @param session
    */
    public Parameter getParameter(CharArray func, CharArrayQueue param, 
                                    Parameter p,SessionObject session) {
if (debug) System.out.println("▽▽InputModule#getParameter▽▽");
        if (func == null) {
            p.add(get());
        } else {
            func.trim().toLowerCase();
            
            String szCh = "";
            PageServlet page = getPage(param, session); //無駄に２回呼ばれる
            if (page != null) szCh = ""+page.hashCode()+"_";
//System.out.println("★szCh:"+szCh);
            if (func.length() == 0)             p.add(get());
            else if (func.equals("link"))       p.add(getLink(param,session));
            else if (func.equals("url"))        p.add(getURL(param,session));
            else if (func.equals("title"))      p.add(getTitle(param, session));
            else if (func.equals("disp"))       p.add(getDisp(param, session));
            else if (func.equals("view"))       p.add(getDisp(param, session));
            else if (func.equals("cleartable")) p.add(clearTable(param, session));
            else if (func.equals("clear_table")) p.add(clearTable(param, session));
            else if (func.equals("setpagemax")) p.add(setPageMax(param, session));
            else if (func.equals("getpagemax")) p.add(getPageMax(param, session));
            else if (func.equals("pagemax"))    p.add(getPageMax(param, session));
            else if (func.equals("page_max"))   p.add(getPageMax(param, session));
            
            else if (func.equals("count"))      p.add(session.getParameter(szCh+"count"));
            else if (func.equals("size"))       p.add(session.getParameter(szCh+"size"));
            else if (func.equals("page"))       p.add(session.getParameter(szCh+"current"));
            else if (func.equals("total"))      p.add(session.getParameter(szCh+"total"));
            else if (func.equals("next"))       p.add(session.getParameter(szCh+"next"));
            
            else if (func.equals("paging"))     p.add(session.getParameter(szCh+"page"));
            
            else if (func.equals("table"))      getTable(param, p, session);
            else if (func.equals("param"))      getParam(param, p, session);
            else if (func.equals("parameter"))  getParam(param, p, session);
            
            //else   p.add("");
            else   p.add("function[@module."+func+"] not found!");
        }
if (debug) System.out.println("△△InputModule#getParameter△△");
        return p;
    }

    /**
        関数設定
        @param func  関数名 
        @param param 関数パラメータ
        @return true 設定成功
    */
    public boolean setFunc(CharArray func, CharArrayQueue param) {
        boolean sts = true;
        /*
        if (func != null && param != null && param.size() > 1) {
            CharArray ch = param.peek();
            func.trim().toLowerCase();
            if (func.length() == 0)            setSrc(ch);
            else if (func.equals("main"))      setSrc(ch);
            else                               sts = false;
        } else sts = false;
        */
        return sts;
    }

    /**
        関数設定
        @param func  関数名 
        @param ch 関数パラメータ
        @return true 設定成功
    */
    public boolean setFunc(CharArray func, CharArray ch) {
        boolean sts = true;
        /*
        if (func != null) {
            func.trim().toLowerCase();
            if (func.length() == 0)            setSrc(ch);
            else if (func.equals("main"))      setSrc(ch);
            else                               sts = false;
        } else sts = false;
        */
        return sts;
    }
}

//
// $Author:$
// $Source:$
// $Revision:$
// $Date:$
//
// $Log:$
//
//
// [end of InputModule.java]
//

