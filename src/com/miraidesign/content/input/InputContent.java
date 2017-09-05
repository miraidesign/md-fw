//------------------------------------------------------------------------
//    InputContent.java
//          システム関数  @content で呼ばれる
//              Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------
//

package com.miraidesign.content.input;

//import java.text.SimpleDateFormat;

import com.miraidesign.common.SystemConst;
import com.miraidesign.content.ContentParser;
import com.miraidesign.content.ContentsManager;
import com.miraidesign.session.SessionObject;
import com.miraidesign.servlet.ModuleServlet;
import com.miraidesign.servlet.PageServlet;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.SiteMapping;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayFile;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.Crypt62;
import com.miraidesign.util.Parameter;
import com.miraidesign.util.Util;

/**
    システム関数（コンテンツ呼び出しクラス）
*/
public class InputContent extends InputItem {
    private static boolean debug = false;
    private static boolean useMobileManager = true;  // bgr はtrue

    //---------------------------------------------
    // コンテンツ情報  
    //---------------------------------------------

    private int       id    = 0;                      // コンテンツID
    private CharArray key   = new CharArray();        // コンテンツキー
    private CharArray url   = new CharArray();        // コンテンツURL
    private CharArray title = new CharArray();        // コンテンツタイトル
    private CharArray description = new CharArray();  // コンテンツ概要
    private CharArray comment = new CharArray();      // コンテンツコメント
    private CharArray bgcolor = new CharArray();      // 背景色
    private int       org_id  = 0;                    //

    private CharArrayFile template    = new CharArrayFile();  // テンプレート情報
    private CharArrayFile ini         = new CharArrayFile();  // template.ini

    // ContentParser 保管場所
    private ContentParser parser;
    public ContentParser getParser() { return parser; }

    /* コンテンツIDを設定する */
    public void setID(int id) { this.id = id;}

    /* コンテンツキーを設定する */
    public void setKey(CharArray key) { this.key.set(key);}
    public void setKey(String    key) { this.key.set(key);}

    /* コンテンツURL(ディレクトリ名）を設定する */
    public void setURL(CharArray url) { this.url.set(url);}
    public void setURL(String    url) { this.url.set(url);}

    /* コンテンツタイトルを設定する */
    public void setTitle(CharArray title) { this.title.set(title);}
    public void setTitle(String    title) { this.title.set(title);}

    /* コンテンツ概要を設定する */
    public void setDescription(CharArray description) { this.description.set(description);}
    public void setDescription(String    description) { this.description.set(description);}

    /* コンテンツコメントを設定する */
    public void setComment(CharArray comment) { this.comment.set(comment);}
    public void setComment(String    comment) { this.comment.set(comment);}

    /* 背景色を設定する */
    public void setBgColor(CharArray bgcolor) { this.bgcolor.set(bgcolor);}
    public void setBgColor(String    bgcolor) { this.bgcolor.set(bgcolor);}

    /* コンテンツIDを設定する */
    public void setOrgID(int org_id) { this.org_id = org_id;}

    /* テンプレート情報を設定する */
    public void setTemplate(CharArrayFile template) { this.template.set(template);}
    //public void setTemplate(String    template) { this.template.set(template);}

    /* template.ini情報を設定する */
    public void setIni(CharArrayFile ini) { this.ini.set(ini);}

    // getter

    /* コンテンツIDを取得する */
    public int getID() { return id; }

    /* コンテンツキーを取得する */
    public CharArray getKey() { return key; }

    /* コンテンツURL(ディレクトリ名)を取得する */
    public CharArray getURL() { return url; }

    /* コンテンツタイトルを取得する */
    public CharArray getTitle() { return title; }

    /* コンテンツ概要を取得する */
    public CharArray getDescription() { return description; }

    /* コメントを取得する */
    public CharArray getComment() { return comment; }

    /* 背景色を取得する */
    public CharArray getBgColor() { return bgcolor; }

    /* org_id を取得する */
    public int getOrgID() { return org_id;}

    /* テンプレート情報を取得する */
    public CharArrayFile getTemplate() { return template;}

    //-------------------
    // constructor
    //-------------------
    public InputContent() {
        type.set("CONTENT");
    }

    //-------------------
    // method
    //-------------------
    public void copy(InputContent from) {
        super.copy(from);
        this.id = from.id;
        this.key.set(from.key);
        this.url.set(from.url);
        this.title.set(from.title);
        this.description.set(from.description);
        this.comment.set(from.comment);
        this.bgcolor.set(from.bgcolor);
        this.org_id = org_id;
        this.template.set(from.template);
        this.ini.set(from.ini);
    }

    public boolean parse() {
        if (parser == null) {
            //content_parser = new ContentParser(file,null,session.getDefaultNamespace().toString(),moduleManager);
            parser = new ContentParser(template, ini, "");
            parser.parse();
        } else {
            parser.parse();

        }
        return true;
    }

    public boolean parse(ModuleManager mm) {
        if (parser == null) {
            //content_parser = new ContentParser(file,null,session.getDefaultNamespace().toString(),moduleManager);
            parser = new ContentParser(template, ini, "", mm);
            parser.parse();
        } else {
            parser.parse();
        }
        return true;
    }

    /* デフォルト関数 */
    public CharArray get() {
        return getLink(null);
    }


    // 
    public void set(CharArray ch) {

    }

    /** パラメータを解析して、 Contentを取り出す
        @param param パラメータ
        @param session SessionObject
        @return null 存在しない
    */
    private InputContent getContent(CharArrayQueue param, SessionObject session) {
        ContentsManager cm = ContentsManager.getInstance(
                        Util.format0(session.getSiteChannelCode(),5));
        boolean ref_mode = cm.getReferenceMode();
        int     ref_id   = cm.getReferenceID();
        InputContent content = this;
        if (param != null && param.size() > 0) {
            CharArray ch = param.peek();
if (debug) System.out.println(session.count+"|getContent("+ch+")oid["+org_id+"] ref_mode:"+ref_mode);
            if (ch.trim().length() > 0) {
                int oid = org_id;
                if (oid == 0) {
                    int i = CharArray.getInt(session.getParameter("orgID"));
                    if (i > 0) {
if (debug) System.out.println(session.count+"|getContent change oid to getParameter(orgID)->("+i+")");
                        oid = i;
                    }
                }
                if (oid == 0) {
                    if (debug) {
                        session.println("--parameter--");
                        session.debugParameters(session);
                        session.println("--userdata--");
                        session.getUserData().debugParameter(session);
                    }
                    do {
                        int i = session.getUserData().getInt("org_id");
                        if (i > 0) {
if (debug) System.out.println(session.count+"|getContent change oid to getUserData(org_id)->("+i+")");
                            oid = i;
                            break;
                        }
                        if (!ref_mode) break;
                        if (ref_id > 0) {
                            oid = ref_id;
                            break;
                        }
                        i = session.getUserData().getInt("ref_id");
                        if (i > 0) {
if (debug) System.out.println(session.count+"|getContent change oid to getUserData(ref_id)->("+i+")");
                            oid = i;
                            break;
                        }
                        i = session.getUserData().getInt("parent_id");
                        if (i > 0) {
if (debug) System.out.println(session.count+"|getContent change oid to getUserData(parent_id)->("+i+")");
                            oid = i;
                            break;
                        }
                        i = session.getUserData().getInt("asp_id");
                        if (i > 0) {
if (debug) System.out.println(session.count+"|getContent change oid to getUserData(asp_id)->("+i+")");
                            oid = i;
                            break;
                        }
if (debug) System.out.println(session.count+"|getContent change oid to 1");
                        oid = 1;

                    } while (false);
                }
                do {
                    content = cm.getContent(ch, oid, session); if (content != null)   break;
                    if (!ref_mode) break;
                    int i = ref_id;
                    if (i > 0) {
                        content = cm.getContent(ch, i, session); if (content != null) break;
                    }
                    i = session.getUserData().getInt("ref_id");
                    if (i > 0) {
                        content = cm.getContent(ch, i, session); if (content != null) break;
                    }
                    i = session.getUserData().getInt("parent_id");
                    if (i > 0) {
                        content = cm.getContent(ch, i, session); if (content != null) break;
                    }
                    i = session.getUserData().getInt("asp_id");
                    if (i > 0) {
                        content = cm.getContent(ch, i, session); if (content != null) break;
                    }
                    content = cm.getContent(ch, 1, session); //@@// ASP
                } while (false);
                //content = cm.getContent(ch, 11);
            }
        }
if (debug && content== null) System.out.println("content が null です");
        return content;
    }

    /* リンクを求める */
    public CharArray getLink(SessionObject session) {
if (debug) System.out.println(session.count+"|▼getLink() id:"+id+" key:"+key);
        CharArray ch = new CharArray();

        CharArray _url = getURL(session);
        if (_url.length() > 0) {
            ch.add("<a href=\"");
            ch.add(_url);               // コンテンツを表示するPageServletの URL(session付)
            ch.add("\"");
            ch.add(">");
            if (title.length() == 0) ch.add(" --no title-- ");
            else ch.add(title);
            ch.add("</a>\n");
        }
if (debug) System.out.println(session.count+"|▲getLink()"+ch);
        return ch;
    }

    /* URLを求める */
    public CharArray getURL(SessionObject session) {
if (debug) System.out.println(session.count+"|▼getURL()");
        CharArray ch = new CharArray();
        do {
            int oid = session.getUserData().getInt("org_id");
            if (oid <= 0) oid = org_id;

            ModuleManager mm = session.getModuleManager();
            if (mm == null) break;
            ModuleServlet module = (ModuleServlet)mm.getModule("ContentsManager");
            PageServlet   page   = module.getDefaultPage();
            if (oid <= 0) break;
            if (SystemManager.convertContextPath == 1) {
                int sessionID = session.getSessionID();
                SiteMapping map = mm.getSiteMapping();
if (debug) System.out.println(session.count+"|  getURL() org_id="+oid);
                map.getSiteParameter(ch);
                if (oid > 0) {
                    //ch.add('/');
                    if (useMobileManager) {
//if (debug) System.out.println(session.count+"  ★★★★★★★★★★★");
//                      try {
//                          MobileSnsManager snsMgr = MobileSnsManager.getInstance(
//                              Util.format0(session.getSiteChannelCode(),5));
//                          if (snsMgr != null) {
//                              ch.add(snsMgr.getSiteUrlPath(oid));
//                          } else {
//                              ch.add("orgID=");
//                              ch.add(oid);
//                              ch.add('/');
//                          }
//                      } catch (Error er) {
//                          //er.printStackTrace(); // Error表示を中止 2007-09-27
//                          useMobileManager = false;
//                      } catch (Exception ex) {
//                          //ex.printStackTrace(); // Exception表示を中止 2007-09-27
                            useMobileManager = false;
//                      }
                    }
                }
                //
                //map.getPageParameter(ch, module.getModuleID()*1000+page.getPageID()); // cm をつける
                //
                if (!ch.endsWith("/")) ch.add("/");
                if (url.length() > 0) {
                    ch.add(url);
                } else {
                    ch.add("ckey=");
                    ch.add(key);
                }
if (debug) System.out.println(session.count+"|  ☆"+ch);
                //if (useMobileManager) {
                if (session.isMobile() && !session.isRobot()) { 
                    ch.add("/");
                    if (SystemConst.cryptSessionID) {
                        ch.add(SystemConst.sessionIDKey[1]); ch.add('=');
                        ch.add(Crypt62.encode(sessionID));
                    } else {
                        ch.add(SystemConst.sessionIDKey[0]); ch.add('=');
                        ch.format(sessionID);
                    }
                }
if (debug) System.out.println(session.count+"|  ★"+ch);
            } else {
                ch.add(page.getURL(session));
                if (ch.indexOf('?') >= 0) ch.add('&');
                else                      ch.add('?');
                ch.add("ckey=");
                ch.add(key);
                if (useMobileManager) {
                    if (oid > 0) {
                        ch.add("&orgID=");
                        ch.add(oid);
                    }
                }
            }

        } while (false);
if (debug) System.out.println(session.count+"|▲getURL()"+ch);
        return ch;
    }

    /* メール出力用のURLを求める(sessionIDなし） */
    public CharArray getMailURL(SessionObject session) {
        CharArray ch = new CharArray();
        do {
            int oid = session.getUserData().getInt("org_id");
            if (oid <= 0) oid = org_id;

            ModuleManager mm = session.getModuleManager();
            if (mm == null) break;
            ModuleServlet module = (ModuleServlet)mm.getModule("ContentsManager");
            PageServlet   page   = module.getDefaultPage();
            if (oid <= 0) break;

            String ss = mm.hostURL;

            if (ss != null && ss.length() > 0) {
                ch.set(ss);
            } else {
                ch.set(com.miraidesign.system.SystemManager.hostURL);
            }

            if (SystemManager.convertContextPath == 1) {
                //int sessionID = session.getSessionID();
                SiteMapping map = mm.getSiteMapping();
if (debug) System.out.println("  getURL() org_id="+oid);
                map.getSiteParameter(ch);
                if (oid > 0) {
                    //ch.add('/');
                    if (useMobileManager) {
//                      try {
//                          MobileSnsManager snsMgr = MobileSnsManager.getInstance(
//                              Util.format0(session.getSiteChannelCode(),5));
//                          if (snsMgr != null) {
//                              ch.add(snsMgr.getSiteUrlPath(oid));
//                          } else {
//                              ch.add("orgID=");
//                              ch.add(oid);
//                              ch.add('/');
//                          }
//                      } catch (Error er) {
//                          er.printStackTrace();
//                          useMobileManager = false;
//                      } catch (Exception ex) {
//                          ex.printStackTrace();
                            useMobileManager = false;
//                      }

                    } else {
                        //ch.add("orgID=");
                        //ch.add(org_id);
                        //ch.add('/');
                    }
                }
                //
                //map.getPageParameter(ch, module.getModuleID()*1000+page.getPageID()); // cm をつける
                //
                if (!ch.endsWith("/")) ch.add("/");
                if (url.length() > 0) {
                    ch.add(url);
                } else {
                    ch.add("ckey=");
                    ch.add(key);
                }
                /*
                ch.add("/");
                if (SystemConst.cryptSessionID) {
                    ch.add(SystemConst.sessionIDKey[1]); ch.add('=');
                    ch.add(Crypt62.encode(sessionID));
                } else {
                    ch.add(SystemConst.sessionIDKey[0]); ch.add('=');
                    ch.format(sessionID);
                }
                */
            } else {
                ch.add(page.getURL());
                if (ch.indexOf('?') >= 0) ch.add('&');
                else                      ch.add('?');
                ch.add("ckey=");
                ch.add(key);
                if (useMobileManager) {
                    if (oid > 0) {
                        ch.add("&orgID=");
                        ch.add(oid);
                    }
                }
            }

        } while (false);
        return ch;
    }

    /* メール出力用のURLを求める(sessionIDなし） */
    public CharArray getMailURL(ModuleManager mm, int oid) {
if (debug) System.out.println("▼getMailURL()");
        CharArray ch = new CharArray();
        do {
            // 取得できないのはまずい！！
            //int oid = session.getUserData().getInt("org_id");
            if (oid <= 0) oid = org_id;

            if (mm == null) break;
            ModuleServlet module = (ModuleServlet)mm.getModule("ContentsManager");
            PageServlet   page   = module.getDefaultPage();
            if (oid <= 0) break;

            String ss = mm.hostURL;

            if (ss != null && ss.length() > 0) {
                ch.set(ss);
            } else {
                ch.set(com.miraidesign.system.SystemManager.hostURL);
            }

            if (SystemManager.convertContextPath == 1) {
                //int sessionID = session.getSessionID();
                SiteMapping map = mm.getSiteMapping();
if (debug) System.out.println("  getURL() org_id="+oid);
                map.getSiteParameter(ch);
                if (oid > 0) {
                    //ch.add('/');
                    if (useMobileManager) {
//                      try {
//                          MobileSnsManager snsMgr = MobileSnsManager.getInstance(
//                              Util.format0(mm.getSiteChannelCode(),5));
//                          if (snsMgr != null) {
//                              ch.add(snsMgr.getSiteUrlPath(oid));
//                          } else {
//                              ch.add("orgID=");
//                              ch.add(oid);
//                              ch.add('/');
//                          }
//                      } catch (Error er) {
//                          er.printStackTrace();
//                          useMobileManager = false;
//                      } catch (Exception ex) {
//                          ex.printStackTrace();
                            useMobileManager = false;
//                      }
                    } else {
                        //ch.add("orgID=");
                        //ch.add(org_id);
                        //ch.add('/');
                    }
                }
                //
                //map.getPageParameter(ch, module.getModuleID()*1000+page.getPageID()); // cm をつける
                //
                if (!ch.endsWith("/")) ch.add("/");
                if (url.length() > 0) {
                    ch.add(url);
                } else {
                    ch.add("ckey=");
                    ch.add(key);
                }
                /*
                ch.add("/");
                if (SystemConst.cryptSessionID) {
                    ch.add(SystemConst.sessionIDKey[1]); ch.add('=');
                    ch.add(Crypt62.encode(sessionID));
                } else {
                    ch.add(SystemConst.sessionIDKey[0]); ch.add('=');
                    ch.format(sessionID);
                }
                */
            } else {
                ch.add(page.getURL());
                if (ch.indexOf('?') >= 0) ch.add('&');
                else                      ch.add('?');
                ch.add("ckey=");
                ch.add(key);
                if (useMobileManager) {
                    if (oid > 0) {
                        ch.add("&orgID=");
                        ch.add(oid);
                    }
                }
            }

        } while (false);
if (debug) System.out.println("▲getMailURL()"+ch);
        return ch;
    }

    //CharArray noLink = new CharArray("このリンクはありません"); // これじゃ多分だめ
    CharArray noLink = new CharArray(""); // これじゃ多分だめ
    public CharArray getLink(CharArrayQueue param, SessionObject session) {
if (debug) System.out.println("▼▼getLink(param)");
        InputContent content = getContent(param, session);
        CharArray ch = (content != null) ? content.getLink(session) : noLink;
if (debug) System.out.println("▲▲getLink(param)");
        return ch;
    }

    //CharArray noURL = new CharArray("このURLはありません"); // これじゃ多分だめ
    CharArray noURL = new CharArray("");
    public CharArray getURL(CharArrayQueue param, SessionObject session) {
if (debug) System.out.println("▼▼getURL(param)");
        InputContent content = getContent(param, session);
        CharArray ch = (content != null) ? content.getURL(session) : noURL;
if (debug) System.out.println("▲▲getURL(param)");
        return ch;
    }


    CharArray noData = new CharArray("- data not found -");

    /* タイトルを求める */
    //CharArray noTitle = new CharArray("このタイトルはありません"); // これじゃ多分だめ
    public CharArray getTitle(CharArrayQueue param, SessionObject session) {
        InputContent content = getContent(param, session);
        return (content != null) ? content.getTitle() : noData;
    }

    /* 概要を求める */
    //CharArray noDescription = new CharArray("概要はありません"); // これじゃ多分だめ
    public CharArray getDescription(CharArrayQueue param, SessionObject session) {
        InputContent content = getContent(param, session);
        return (content != null) ? content.getDescription() : noData;
    }

    /* コメントを求める */
    //CharArray noComment = new CharArray("コメントはありません"); // これじゃ多分だめ
    public CharArray getComment(CharArrayQueue param, SessionObject session) {
        InputContent content = getContent(param, session);
        return (content != null) ? content.getComment() : noData;
    }

    /* 背景色を取得する */
    public CharArray getBgColor(CharArrayQueue param, SessionObject session) {
        InputContent content = getContent(param, session);
        return (content != null) ? content.getBgColor() : noData;
    }


    public CharArray getOrgID(CharArrayQueue param, SessionObject session) {
        InputContent content = getContent(param, session);
        return (content != null) ? new CharArray(""+content.getOrgID()) : noData;
    }


    /* コンテンツを表示する*/
    CharArray noContent = new CharArray("このコンテンツはありません"); // これじゃ多分だめ
//  public CharArray getDisp(CharArrayQueue param) {
//      InputContent content = getContent(param);
//      return (content != null) ? content.draw(null) : noContent;
//                              // セッションを取得しないと描画できない！！！！
//  }
    public CharArray getDisp(CharArrayQueue param, SessionObject session) {
if (debug) session.println("▼▼getDisp() id:"+id+" key:"+key+" session="+(session != null));
        InputContent content = getContent(param, session);
        CharArray ch = (content != null) ? content.draw(session) : noData;
                                // セッションを取得しないと描画できない！！！！
if (debug) session.println("▲▲getDisp() ch.length()="+ch.length());
        return ch;
    }


    public CharArray draw(SessionObject session) {
if (debug) System.out.println("▼draw() id:"+id+" key:"+key);
if (debug && session != null) System.out.println("  sessionID:"+session.getSessionID());

        parser.setNameSpace("$$CONTENT$$"+key);
        session.clearBuffer("$$CONTENT$$"+key);
        parser.setTemplate(session);
        CharArray ch = parser.draw(session);

if (debug) System.out.println("▲draw() ch.length()="+ch.length());

        return ch;
    }

    /**
        関数呼び出し
        @param func 関数名
        @param param 関数パラメータ
        @return CharArray
    */
    public CharArray getFunc(CharArray func, CharArrayQueue param) {
        return getFunc(func, param, 0);
    }
    public CharArray getFunc(CharArray func, CharArrayQueue param, int state) {
if (debug) System.out.println("▽▽InputContent#getFunc▽▽");
        CharArray ch = null;
        if (func == null) {
            ch = get();
        } else {
            func.trim().toLowerCase();
            if (func.length() == 0)              ch = get();
            else if (func.equals("link"))        ch = getLink(param,null);
            else if (func.equals("url"))         ch = getURL(param,null);
            else if (func.equals("title"))       ch = getTitle(param, null);
            else if (func.equals("description")) ch = getDescription(param,null);
            else if (func.equals("disp"))        ch = getDisp(param,null);
            else if (func.equals("view"))        ch = getDisp(param,null);
            //else if (func.equals("view"))        ch = getView();
            else                                 ch = get();
        }
if (debug) System.out.println("△△InputContent#getFunc△△");
        return ch;
    }
    /**
        関数呼び出し（CMSが利用するのはこちらのみ）
        @param func  関数名
        @param param 関数パラメータ
        @param p     戻り値用
        @param session SessionObject
        @return Parameter
    */
    public Parameter getParameter(CharArray func, CharArrayQueue param,
                                    Parameter p,SessionObject session) {
if (debug) session.println("▽▽InputContent#getParameter▽▽");
        if (func == null) {
            p.add(get());
        } else {
            func.trim().toLowerCase();
            if (func.length() == 0)             p.add(get());
            else if (func.equals("link"))       p.add(getLink(param, session));
            else if (func.equals("url"))        p.add(getURL(param, session));
            else if (func.equals("title"))      p.add(getTitle(param, session));
            else if (func.equals("description"))  p.add(getDescription(param, session));
            else if (func.equals("org_id"))     p.add(getOrgID(param, session));
            else if (func.equals("comment"))    p.add(getComment(param, session));
            else if (func.equals("bgcolor"))    p.add(getBgColor(param, session));
            else if (func.equals("disp"))       p.add(getDisp(param, session));
            else if (func.equals("view"))       p.add(getDisp(param, session));
            //else if (func.equals("view"))       p.add(getView());
            //else   p.add("");
            else   p.add("function[@content."+func+"] not found!");

        }
if (debug) session.println("△△InputContent#getParameter△△");
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
        /**
        if (func != null && param != null && param.size() > 1) {
            CharArray ch = param.peek();
            func.trim().toLowerCase();
            if (func.length() == 0)            setSrc(ch);
            else if (func.equals("main"))      setSrc(ch);
            else                               sts = false;
        } else sts = false;
        **/
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
        /**
        if (func != null) {
            func.trim().toLowerCase();
            if (func.length() == 0)            setSrc(ch);
            else if (func.equals("main"))      setSrc(ch);
            else                               sts = false;
        } else sts = false;
        **/
        return sts;
    }


}

//
// [end of InputContent.java]
//

