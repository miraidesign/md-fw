//------------------------------------------------------------------------
//    InputSession.java
//          システム関数  @session で呼ばれる
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content.input;

import java.util.Enumeration;
import java.util.Hashtable;
import java.net.URLEncoder;
import java.net.URLDecoder;

import com.miraidesign.system.ModuleManager;
import com.miraidesign.system.SiteMapping;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.Parameter;

/**
    システム関数（セッション情報呼び出しクラス）
*/
public class InputSession extends InputItem /* implements SessionEvent */ {
    private static boolean debug  = false;
    private static boolean debug2 = false;

    //-------------------
    // constructor
    //-------------------
    public InputSession() {
        type.set("SESSION");
    }
    
//  /* 
//      ユーザー情報クラスを生成します
//      @param id   user_id
//  */
//  public InputSession(int id) {
//      type.set("USER");
//      this.user_id = id;
//  }
    
    //-------------------
    // method
    //-------------------
    public void copy(InputSession from) {
//      this.user_id = from.user_id;
    }
    
    /** デフォルト関数 */
    public CharArray get() {
        return new CharArray();
    }
    
    public void set(CharArray ch) {
        
    }
    
    /* セッションIDを取得する */
    public int getID(SessionObject session) { 
        int id = 0;
        do {
            if (session == null) break;
            id = session.getSessionID();
        } while (false);
        return id; 
    }
    /* セッションタイムアウトを取得する */
    public int getSessionTimeOut(SessionObject session) {
        int time = 0;
        do {
            if (session == null) break;
            time = (int)(session.getSessionTimeOut()/1000);
        } while (false);
        return time;
    }
    /* アクセスタイムアウトを取得する */
    public int getAccessTimeOut(SessionObject session) {
        int time = 0;
        do {
            if (session == null) break;
            time = (int)(session.getAccessTimeOut()/1000);
        } while (false);
        return time;
    }
    
    /* 認証情報をを取得する */
    public boolean isAuthorized(SessionObject session) {
        boolean auth = false;
        do {
            if (session == null) break;
            auth = session.isAuthorized();
        } while (false);
        return auth;
    }
    /* ログインユーザー情報 */
    public String getUserID(SessionObject session) {
        String userID = "";
        do {
            if (session == null || session.userID == null) break;
            userID = session.userID;
        } while (false);
        return userID;
    }
    /* 端末ID情報 */
    public String getDeviceID(SessionObject session) {
        String deviceID = "";
        do {
            if (session == null || session.deviceID == null) break;
            deviceID = session.deviceID;
        } while (false);
        return deviceID;
    }
    
    /* PCか？ */
    public boolean isPC(SessionObject session) {
        boolean sts = false;
        do {
            if (session == null) break;
            sts = session.isPC();
        } while (false);
        return sts;
    }
    /* 携帯か？ */
    public boolean isMobile(SessionObject session) {
        boolean sts = false;
        do {
            if (session == null) break;
            sts = session.isMobile();
        } while (false);
        return sts;
    }
    /* スマートフォンか？ */
    public boolean isSmartPhone(SessionObject session) {
        boolean sts = false;
        do {
            if (session == null) break;
            sts = session.isSmartPhone();
        } while (false);
        return sts;
    }
    /* タブレット？ */
    public boolean isTablet(SessionObject session) {
        boolean sts = false;
        do {
            if (session == null) break;
            sts = session.isTablet();
        } while (false);
        return sts;
    }
    /* ロボットか？ */
    public boolean isRobot(SessionObject session) {
        boolean sts = false;
        do {
            if (session == null) break;
            sts = session.isRobot();
        } while (false);
        return sts;
    }
    
    /* cookie あり */
    public boolean hasCookie(SessionObject session) {
        boolean sts = false;
        do {
            if (session == null) break;
            sts = (session.request.getCookies() != null);
        } while (false);
        return sts;
    }
    /* cookie なしでロボットでない */
    public boolean notCookieNotRobot(SessionObject session) {
        boolean sts = false;
        do {
            if (session == null) break;
            sts = (session.request.getCookies() == null);
            if (!sts) break;
            sts = !session.isRobot();
        } while (false);
        return sts;
    }
    
    /* LANG取得 */
    public CharArray getLang(SessionObject session) {
        CharArray ch = null;
        do {
            if (session == null) break;
            ch = session.getLang();
        } while (false);
        return ch;
    }
    /* LANGデータ取得 */
    public CharArray getLangData(SessionObject session) {
        CharArray ch = null;
        do {
            if (session == null) break;
            ch = session.getLangData();
        } while (false);
        return ch;
    }
    /* LANG名称取得 */
    public CharArray getLangName(SessionObject session) {
        CharArray ch = null;
        do {
            if (session == null) break;
            ch = session.getLangName();
        } while (false);
        return ch;
    }
    
    /* Theme取得 */
    public CharArray getTheme(SessionObject session) {
        CharArray ch = null;
        do {
            if (session == null) break;
            ch = session.getTheme();
        } while (false);
        return ch;
    }
    /* Themeデータ取得 */
    public CharArray getThemeData(SessionObject session) {
        CharArray ch = null;
        do {
            if (session == null) break;
            ch = session.getThemeData();
        } while (false);
        return ch;
    }
    /* Theme名称取得 */
    public CharArray getThemeName(SessionObject session) {
        CharArray ch = null;
        do {
            if (session == null) break;
            ch = session.getThemeName();
        } while (false);
        return ch;
    }
    /* RequestURL 取得 */
    public CharArray getRequestURL(SessionObject session) {
        CharArray ch = null;
        do {
            if (session == null || session.request == null) break;
            ch = new CharArray(session.request.getRequestURL());
            ch.replace("://",":@@");
            ch.replace("//","/");
            ch.replace(":@@","://");
        } while (false);
        return ch;
    }
    /* RequestURI 取得 */
    public CharArray getRequestURI(SessionObject session) {
        CharArray ch = null;
        do {
            if (session == null || session.request == null) break;
            ch = new CharArray(session.request.getRequestURI());
            ch.replace("//","/");
        } while (false);
        return ch;
    }
    
    
    /* 
        パラメータ情報を取得する
    */
    public CharArray getParameterData(CharArrayQueue param, SessionObject session) { 
        CharArray ch = new CharArray();
        do {
            if (session == null) break;
            if (param == null || param.size() == 0) break;
            CharArray key = param.peek();
            if (key == null || key.trim().length() == 0) break;
            
            ch.set(session.getParameter(key.toString()));
if (debug2) System.out.println(session.count+"|◆@session.parameter("+key+")="+ch);
        } while (false);
        return ch; 
    }
    /* 
        ユーザデータ情報を取得する
    */
    public CharArray getUserData(CharArrayQueue param, SessionObject session) { 
        CharArray ch = new CharArray();
        do {
            if (session == null) break;
            if (param == null || param.size() == 0) break;
            CharArray key = param.peek();
            if (key == null || key.trim().length() == 0) break;
            HashParameter hash = session.getUserData();
            if (hash == null) break;
            //if (debug2) hash.debugParameter();
            ch.set( hash.get(key));
if (debug2) System.out.println(session.count+"|◆@session.user_data("+key+")="+ch);
        } while (false);
        return ch; 
    }

    /* 
        パラメータキーリストを取得する
    */
    public Parameter getParameterKeys(CharArrayQueue param, Parameter p,SessionObject session) { 
        do {
            if (session == null || p == null) break;
            Hashtable<String, String[]>  hash = session.getHashParameter();
            for (Enumeration e = hash.keys(); e.hasMoreElements();) {
                String key = (String)e.nextElement();
                if (!key.equals("$$$Parameters$$$")) p.add(key);
            }
        } while (false);
        
        
        return p; 
    }
    /* 
        パラメータデータリストを取得する
    */
    public Parameter getParameterValues(CharArrayQueue param, Parameter p,SessionObject session) { 
        do {
            if (session == null || p == null) break;
            Hashtable<String, String[]>  hash = session.getHashParameter();
            for (Enumeration e = hash.keys(); e.hasMoreElements();) {
                String key = (String)e.nextElement();
                if (!key.equals("$$$Parameters$$$")) {
                    String[] strs = (String[])hash.get(key);
                    if (strs == null || strs.length == 0) p.add();
                    else                                  p.add(strs[0]);
                }
            }
            //for (Enumeration e = hash.elements(); e.hasMoreElements();) {
            //    String[] strs = (String[])e.nextElement();
            //    if (strs == null || strs.length == 0) p.add();
            //     else                                 p.add(strs[0]);
            //} 
        } while (false);
        return p;
    }


    /* 
        ユーザーデータキーリストを取得する
    */
    public Parameter getUserDataKeys(CharArrayQueue param, Parameter p,SessionObject session) { 
        do {
            if (session == null || p == null) break;
            HashParameter hp = session.getUserData();
            for (int i = 0; i < hp.size(); i++) {
                p.add(hp.keyElementAt(i));
            }
        } while (false);
        
        
        return p; 
    }
    /* 
        パラメータデータリストを取得する
    */
    public Parameter getUserDataValues(CharArrayQueue param, Parameter p,SessionObject session) { 
        do {
            if (session == null || p == null) break;
            HashParameter hp = session.getUserData();
            
            for (int i = 0; i < hp.size(); i++) {
                CharArray ch  = hp.valueElementAt(i);
                if (ch == null || ch.length == 0) p.add();
                else                              p.add(ch);
            }
        } while (false);
        return p;
    }

    /* 
        パラメータ情報を設定する
    */
    public CharArray setParameterData(CharArrayQueue param, SessionObject session) { 
        CharArray ch = new CharArray();
        do {
            if (session == null) break;
            if (param == null || param.size() < 2) break;
            CharArray key = param.peek();
            CharArray data = param.peek(1);
            if (key == null || key.trim().length() == 0) break;
            
            session.setParameter(key.toString(),data.toString());
            ch.set(data);
if (debug2) System.out.println(session.count+"|◆@session.set_parameter("+key+")="+ch);
        } while (false);
        return ch; 
    }
    /* 
        ユーザデータを設定する
    */
    public CharArray setUserData(CharArrayQueue param, SessionObject session) { 
        CharArray ch = new CharArray();
        do {
            if (session == null) break;
            if (param == null || param.size() < 2) break;
            CharArray key  = param.peek();
            CharArray data = param.peek(1);
            if (key == null || key.trim().length() == 0) break;
            HashParameter hash = session.getUserData();
            if (hash == null) break;
            hash.add(new CharArray(key), new CharArray(data));
            if (debug2) hash.debugParameter(session);
            ch.set(data);
if (debug2) System.out.println(session.count+"|◆@session.set_userdata("+key+")="+ch);
        } while (false);
        return ch; 
    }
    /* 
        ユーザテンプレートを設定する
    */
    public CharArray setUserTemplate(CharArrayQueue param, SessionObject session) { 
        CharArray ch = new CharArray();
        do {
            if (session == null) break;
            if (param == null || param.size() < 2) break;
            CharArray key  = param.peek();
            CharArray data = param.peek(1);
            if (key == null || key.trim().length() == 0) break;
            HashParameter hash = session.getUserTemplate();
            if (hash == null) break;
            hash.add(new CharArray(key), new CharArray(data));
            if (debug2) hash.debugParameter(session);
            ch.set(data);
if (debug2) System.out.println(session.count+"|◆@session.set_usertemplate("+key+")="+ch);
        } while (false);
        return ch; 
    }
    /* 
        ユーザテンプレート情報を取得する
    */
    public CharArray getUserTemplate(CharArrayQueue param, SessionObject session) { 
        CharArray ch = new CharArray();
        do {
            if (session == null) break;
            if (param == null || param.size() == 0) break;
            CharArray key = param.peek();
            if (key == null || key.trim().length() == 0) break;
            HashParameter hash = session.getUserTemplate();
            if (hash == null) break;
            //if (debug2) hash.debugParameter();
            ch.set( hash.get(key));
if (debug2) System.out.println(session.count+"|◆@session.user_template("+key+")="+ch);
        } while (false);
        return ch; 
    }
    
    public CharArray getUrlEncode(CharArrayQueue param, SessionObject session) { 
        CharArray ch = null;
        CharArray str = new CharArray();
        if (param != null && param.size() > 0) {
            String enc = (param.size() > 1) ? param.peek(1).toString() : "";
            ch = param.peek();
            if (ch.length() > 0) {
                try {
                    str.set(URLEncoder.encode(ch.toString(), enc));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
if (debug2) System.out.println(session.count+"|◆@session.url_encode("+ch+")->["+str+"]");
        return str;
    }
    public CharArray getUrlDecode(CharArrayQueue param, SessionObject session) { 
        CharArray ch = null;
        CharArray str = new CharArray();
        if (param != null && param.size() > 0) {
            String enc = (param.size() > 1) ? param.peek(1).toString() : "";
            ch = param.peek();
            if (ch.length() > 0) {
                try {
                    str.set(URLDecoder.decode(ch.toString(), enc));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
if (debug2) System.out.println(session.count+"|◆@session.url_decode("+ch+")->["+str+"]");
        return str;
    }
    /* site.ini情報を取得する */
    public CharArray getString(CharArrayQueue param, SessionObject session) { 
        CharArray section = null;
        CharArray key = null;
        CharArray ret = CharArray.pop();
        ModuleManager mm = session.getModuleManager();
        if (param != null && param.size() > 1 && mm != null) {
            section = param.peek(0);
            key     = param.peek(1);
            ret.set(mm.ini.get(mm.getSection(section),key));
        }
if (debug2) System.out.println(session.count+"|◆@session.getString("+section+","+key+")->["+ret+"]");
        return ret;
    }
    /* site.ini情報を取得する */
    public CharArray getInt(CharArrayQueue param, SessionObject session) { 
        CharArray section = null;
        CharArray key = null;
        CharArray ret = CharArray.pop();
        ModuleManager mm = session.getModuleManager();
        if (param != null && param.size() > 1 && mm != null) {
            section = param.peek(0);
            key     = param.peek(1);
            ret.set(CharArray.getInt(mm.ini.get(mm.getSection(section),key)));
        }
if (debug2) System.out.println(session.count+"|◆@session.getInt("+section+","+key+")->["+ret+"]");
        return ret;
    }
    /* site.ini情報を取得する */
    public CharArray getLong(CharArrayQueue param, SessionObject session) { 
        CharArray section = null;
        CharArray key = null;
        CharArray ret = CharArray.pop();
        ModuleManager mm = session.getModuleManager();
        if (param != null && param.size() > 1 && mm != null) {
            section = param.peek(0);
            key     = param.peek(1);
            ret.set(CharArray.getLong(mm.ini.get(mm.getSection(section),key)));
        }
if (debug2) System.out.println(session.count+"|◆@session.getLong("+section+","+key+")->["+ret+"]");
        return ret;
    }
    /* site.ini情報を取得する */
    public CharArray getBoolean(CharArrayQueue param, SessionObject session) { 
        CharArray section = null;
        CharArray key = null;
        CharArray ret = CharArray.pop();
        ModuleManager mm = session.getModuleManager();
        if (param != null && param.size() > 1 && mm != null) {
            section = param.peek(0);
            key     = param.peek(1);
            ret.set(CharArray.getBoolean(mm.ini.get(mm.getSection(section),key)) ? "true" : "");
        }
if (debug2) System.out.println(session.count+"|◆@session.getString("+section+","+key+")->["+ret+"]");
        return ret;
    }
    
    /* Message 情報を取得する */
    public CharArray getMessage(CharArrayQueue param, SessionObject session) { 
        CharArray key = null;
        CharArray ret = CharArray.pop();
        if (param != null && param.size() > 0 && session != null) {
            key     = param.peek(0);
            ret.set(session.getMessage(key));
        }
if (debug2) System.out.println(session.count+"|◆@session.getMessage("+key+")->["+ret+"]");
        return ret;
    }
    /* Color 情報を取得する */
    public CharArray getColor(CharArrayQueue param, SessionObject session) { 
        CharArray key = null;
        CharArray ret = CharArray.pop();
        if (param != null && param.size() > 0 && session != null) {
            key     = param.peek(0);
            ret.set(session.getColor(key));
        }
if (debug2) System.out.println(session.count+"|◆@session.getColor("+key+")->["+ret+"]");
        return ret;
    }
    
    
    /* SiteParameterを取得する(開発者用) */
    public CharArray getSiteParameter(SessionObject session) { 
        CharArray ret = CharArray.pop();
        ModuleManager mm = session.getModuleManager();
        if (mm != null) {
            SiteMapping map = mm.getSiteMapping();
            if (map != null) {
                ret.set(map.getSiteParameter(session));
            }
        }
        return ret;
    }
    /* ServerKeyを取得する(開発者用) */
    public CharArray getServerKey(SessionObject session) { 
        CharArray ret = CharArray.pop();
        ModuleManager mm = session.getModuleManager();
        if (mm != null) {
            ret.set(mm.getServerKey());
        }
        return ret;
    }
    
    /* 
        ブロック情報を設定する（開発者用)
    */
    public CharArray setBlock(CharArrayQueue param, SessionObject session) { 
        CharArray ch = new CharArray();
        do {
            if (session == null) break;
            if (param == null || param.size() < 2) break;
            CharArray key = param.peek();
            CharArray data = param.peek(1);
            if (key == null || key.trim().length() == 0) break;
            if (data == null || data.trim().length() == 0) break;
            
            session.setBlock(key.toString(),data.getInt());
            ch.set(data);
if (debug2) System.out.println(session.count+"|◆@session.set_block("+key+")="+ch);
        } while (false);
        return ch; 
    }
    
    
    
    //////////////////////////////////////////////////////////////////////////////
    // 関数呼び出し元
    //////////////////////////////////////////////////////////////////////////////
    
    /*
        関数呼び出し
        @param func 関数名 
        @param param 関数パラメータ
    */
    public CharArray getFunc(CharArray func, CharArrayQueue param) {
        return getFunc(func, param, 0);
    }
    
    public CharArray getFunc(CharArray func, CharArrayQueue param, int state) {
if (debug) System.out.println("▽▽InputSession#getFunc▽▽");
        CharArray ch = null;
        if (func == null) {
            ch = get();
        } else {
            func.trim().toLowerCase();
            if (func.length() == 0)              ch = get();
            //else if (func.equals("link"))        ch = getLink(param);
            //else if (func.equals("description")) ch = getDescription(param);
            //else if (func.equals("title"))       ch = getTitle(param);
            //else if (func.equals("disp"))        ch = getDisp(param,null);
            //else if (func.equals("view"))        ch = getDisp(param,null);
            else                                 ch = get();
        }
if (debug) System.out.println("△△InputSession#getFunc△△");
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
if (debug) System.out.println(session.count+"|▽▽InputSession#getParameter▽▽ "+func);
        if (func == null) {
            p.add(get());
        } else {
            func.trim().toLowerCase().replace("_","");
            if (func.length() == 0)             p.add(get());
            else if (func.equals("id")) {
                int _id = getID(session);
                p.add((_id <= 0) ? "" :""+_id);
            }
            else if (func.equals("sessionid")) {
                int _id = getID(session);
                p.add((_id <= 0) ? "" :""+_id);
            }
            else if (func.equals("timeout")) p.add(getSessionTimeOut(session));
            else if (func.equals("gettimeout")) p.add(getSessionTimeOut(session));
            else if (func.equals("getsessiontimeout")) p.add(getSessionTimeOut(session));
            else if (func.equals("accesstimeout")) p.add(getAccessTimeOut(session));
            else if (func.equals("getaccesstimeout")) p.add(getAccessTimeOut(session));
            else if (func.equals("isauthorized")) p.add(isAuthorized(session)?"true":"");
            else if (func.equals("isauth")) p.add(isAuthorized(session)?"true":"");
            else if (func.equals("authorized")) p.add(isAuthorized(session)?"true":"");
            else if (func.equals("userid")) p.add(getUserID(session));
            else if (func.equals("getuserid")) p.add(getUserID(session));
            else if (func.equals("deviceid")) p.add(getDeviceID(session));
            else if (func.equals("getdeviceid")) p.add(getDeviceID(session));
            
            // cookie/robot 2013-06-27
            
            else if (func.equals("ispc")) p.add(isPC(session)?"true":"");
            else if (func.equals("ismobile")) p.add(isMobile(session)?"true":"");
            else if (func.equals("issmartphone")) p.add(isSmartPhone(session)?"true":"");
            else if (func.equals("istablet")) p.add(isTablet(session)?"true":"");
            else if (func.equals("isrobot")) p.add(isRobot(session)?"true":"");
            else if (func.equals("hascookie")) p.add(hasCookie(session)?"true":"");
            else if (func.equals("notcookienotrobot")) p.add(notCookieNotRobot(session)?"true":"");
            
            //Lang関連 2010-04026
            else if (func.equals("lang")) p.add(getLang(session));
            else if (func.equals("langdata")) p.add(getLangData(session));
            else if (func.equals("langname")) p.add(getLangName(session));
            else if (func.equals("getlang")) p.add(getLang(session));
            else if (func.equals("getlangdata")) p.add(getLangData(session));
            else if (func.equals("getlangname")) p.add(getLangName(session));
            
            //Theme関連 2010-04026
            else if (func.equals("theme")) p.add(getTheme(session));
            else if (func.equals("themedata")) p.add(getThemeData(session));
            else if (func.equals("themename")) p.add(getThemeName(session));
            else if (func.equals("gettheme")) p.add(getTheme(session));
            else if (func.equals("getthemedata")) p.add(getThemeData(session));
            else if (func.equals("getthemename")) p.add(getThemeName(session));
            // Rquest関連 2012-02-21
            else if (func.equals("getrequesturl")) p.add(getRequestURL(session));
            else if (func.equals("getrequesturi")) p.add(getRequestURI(session));
            
            //else if (func.equals("name"))               p.add(getName(session));
            //else if (func.equals("pd"))                   p.add(getParameterData(param, session));
            else if (func.equals("param"))                   p.add(getParameterData(param, session));
            else if (func.equals("parameter"))            p.add(getParameterData(param, session));
            else if (func.equals("getparameter"))        p.add(getParameterData(param, session));
            else if (func.equals("parameterkeys"))       getParameterKeys(param, p, session);
            else if (func.equals("parametervalues"))     getParameterValues(param, p, session);
            else if (func.equals("getparameterkeys"))   getParameterKeys(param, p, session);
            else if (func.equals("getparametervalues")) getParameterValues(param, p, session);
            
            else if (func.equals("userdata"))         p.add(getUserData(param, session));
            else if (func.equals("getuserdata"))     p.add(getUserData(param, session));
            else if (func.equals("userdatakeys"))    getUserDataKeys(param, p, session);
            else if (func.equals("userdatavalues"))  getUserDataValues(param, p, session);
            else if (func.equals("getuserdatakeys"))    getUserDataKeys(param, p, session);
            else if (func.equals("getuserdatavalues"))  getUserDataValues(param, p, session);
            
            else if (func.equals("setparameter"))  p.add(setParameterData(param, session));
            else if (func.equals("setuserdata"))  p.add(setUserData(param, session));
            else if (func.equals("setusertemplate"))  p.add(setUserTemplate(param, session));
            else if (func.equals("usertemplate"))         p.add(getUserTemplate(param, session));
            else if (func.equals("getusertemplate"))     p.add(getUserTemplate(param, session));
            else if (func.equals("urlencode"))    p.add(getUrlEncode(param,session));
            else if (func.equals("urldecode"))    p.add(getUrlDecode(param,session));
            //else if (func.equals("height"))     p.add(getHeight(param,session));
            //else if (func.equals("view"))       p.add(getView());
            //else   p.add("");
            
            else if (func.equals("getstring"))    p.add(getString(param,session));
            else if (func.equals("getint"))       p.add(getInt(param,session));
            else if (func.equals("getlong"))      p.add(getLong(param,session));
            else if (func.equals("getbool"))      p.add(getBoolean(param,session));
            else if (func.equals("getboolean"))   p.add(getBoolean(param,session));
            
            else if (func.equals("getmessage"))   p.add(getMessage(param,session));
            else if (func.equals("getcolor"))     p.add(getColor(param,session));
            
            
            else if (func.equals("getsiteparameter"))   p.add(getSiteParameter(session));
            else if (func.equals("getserverkey"))   p.add(getServerKey(session));
            else if (func.equals("setblock"))     p.add(setBlock(param, session));
            
            else   p.add("function[@session."+func+"] not found!");
        }
if (debug) System.out.println(session.count+"|△△InputSession#getParameter△△");
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
// [end of InputSession.java]
//

