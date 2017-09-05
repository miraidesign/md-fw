//------------------------------------------------------------------------
//    InputMobile.java
//          システム関数  @mobile で呼ばれる
//              Copyright (c) MiraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.content.input;

import com.miraidesign.session.SessionObject;
import com.miraidesign.session.UserAgent;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.Parameter;

/**
    システム関数（コンテンツ呼び出しクラス）
*/
public class InputMobile extends InputItem {
    private static boolean debug = false;


    //-------------------
    // constructor
    //-------------------
    public InputMobile() {
        type.set("MOBILE");
    }

    //-------------------
    // method
    //-------------------
    public void copy(InputMobile from) {
    }

    /** デフォルト関数 */
    public CharArray get() {
        return new CharArray();
    }

    // 
    public void set(CharArray ch) {

    }

    /* 端末キーワードを取得する */
    public CharArray getKey(SessionObject session) {
        CharArray ch = null;
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (ua == null) break;
            ch = ua.getKey();

        } while (false);
        return ch;
    }

    /* ユーザーエージェントを取得する */
    public CharArray getUserAgent(SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (ua == null) break;
            ch.set(ua.getUserAgentHeader());
        } while (false);
        return ch;
    }
    /* キャリア名を取得する */
    public CharArray getCarrier(SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (ua == null) break;
            ch.set(ua.getCarrierName());
        } while (false);
        return ch;
    }
    /* デバイス名を取得する */
    public CharArray getDeviceName(SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (ua == null) break;
            ch.set(ua.deviceName);
        } while (false);
        return ch;
    }
    /* サブスクライバIDを取得する */
    public CharArray getDeviceID(SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (ua == null) break;
            ch.set(ua.deviceID);
        } while (false);
        return ch;
    }

    /* 特定ブラウザ情報を取得する */
    public CharArray getBrowser(SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (ua != null) ch.set(ua.browser);
        } while (false);
        return ch;
    }

    /* PCか？*/
    public CharArray isPC(SessionObject session) {
        return isPC(session, true);
    }
    public CharArray isPC(SessionObject session, boolean mode) {
        CharArray ch = CharArray.pop();
        if (session != null && session.isPC() && mode) ch.add("PC");
        return ch;
    }

    /* Mobileか？*/
    public CharArray isMobile(SessionObject session) {
        return isMobile(session, true);
    }
    /* Mobileか？
        @param session
        @param mode  xhtml?
    */
    public CharArray isMobile(SessionObject session, boolean mode) {
        CharArray ch = CharArray.pop();
        if (session != null && !session.isPC() && mode) ch.add("Mobile");
        return ch;
    }
    
    /* Robotか？*/
    public CharArray isRobot(SessionObject session) {
        return isRobot(session, true);
    }
    public CharArray isRobot(SessionObject session, boolean mode) {
        CharArray ch = CharArray.pop();
        if (session != null && !session.isRobot() && mode) ch.add("Robot");
        return ch;
    }
    
    /* MobileかつRobotでない*/
    public CharArray isMobileAndNotRobot(SessionObject session) {
        return isMobileAndNotRobot(session, true);
    }
    public CharArray isMobileAndNotRobot(SessionObject session, boolean mode) {
        CharArray ch = CharArray.pop();
        if (session != null && session.isMobile() && !session.isRobot() && mode) ch.add("Mobile&!Robot");
        return ch;
    }

    /* HTML5対応機種か？*/
    public CharArray isHTML5(SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (ua != null && ua.isHTML5()) ch.add("HTML5");
        } while (false);
        return ch;
    }
    /* XHTML対応機種か？*/
    public CharArray isXHTML(SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (ua != null && ua.isXHTML()) ch.add("XHTML");
        } while (false);
        return ch;
    }
    /* SSL対応機種か？*/
    public CharArray isSSL(SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (ua != null && ua.isSSL()) ch.add("SSL");
        } while (false);
        return ch;
    }

    /* 画面の横ドット数を取得する */
    public CharArray getWidth(CharArrayQueue param, SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (param == null || param.size() == 0 || param.peek().trim().length() == 0) {
                ch.format(ua.getWidth());
                break;
            }
            int per = param.peek().getInt();
            if (per > 0 && per <= 100) {
                int width = (int)Math.round((double)ua.getWidth() * (double)per / 100.0);
                if (width == 0) width = 1;
                ch.format(width);
                break;
            }
            ch.format(ua.getWidth());
        } while (false);
        return ch;
    }

    /* 画面の縦ドット数を取得する */
    public CharArray getHeight(CharArrayQueue param, SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (param == null || param.size() == 0 || param.peek().trim().length() == 0) {
                ch.format(ua.getHeight());
                break;
            }
            int per = param.peek().getInt();
            if (per > 0 && per <= 100) {
                int height = (int)Math.round((double)ua.getHeight() * (double)per / 100.0);
                if (height == 0) height = 1;
                ch.format(height);
                break;
            }
            ch.format(ua.getHeight());
        } while (false);
        return ch;
    }

    /* 液晶の横ドット数を取得する */
    public CharArray getLCDWidth(CharArrayQueue param, SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (param == null || param.size() == 0 || param.peek().trim().length() == 0) {
                ch.format(ua.getLCDWidth());
                break;
            }
            int per = param.peek().getInt();
            if (per > 0 && per <= 100) {
                int width = (int)Math.round((double)ua.getLCDWidth() * (double)per / 100.0);
                if (width == 0) width = 1;
                ch.format(width);
                break;
            }
            ch.format(ua.getWidth());
        } while (false);
        return ch;
    }

    /* 液晶の縦ドット数を取得する */
    public CharArray getLCDHeight(CharArrayQueue param, SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (param == null || param.size() == 0 || param.peek().trim().length() == 0) {
                ch.format(ua.getLCDHeight());
                break;
            }
            int per = param.peek().getInt();
            if (per > 0 && per <= 100) {
                int height = (int)Math.round((double)ua.getLCDHeight() * (double)per / 100.0);
                if (height == 0) height = 1;
                ch.format(height);
                break;
            }
            ch.format(ua.getHeight());
        } while (false);
        return ch;
    }


    /* ユーザーエージェント情報を取得する */
    public CharArray get(CharArrayQueue param, SessionObject session) {
        return getCharArray(param, session);
    }
    public CharArray getString(CharArrayQueue param, SessionObject session) {
        return getCharArray(param, session);
    }
    public CharArray getCharArray(CharArrayQueue param, SessionObject session) {
        CharArray ch = CharArray.pop();
        do {
            if (session == null) break;
            UserAgent ua = session.getUserAgent();
            if (param == null || param.size() == 0 || param.peek().trim().length() == 0) {
                break;
            }
            CharArray column = param.peek();
            ch.set(ua.get(column));
        } while (false);
        return ch;
    }
    public int getInt(CharArrayQueue param, SessionObject session) {
        return CharArray.getInt(getCharArray(param, session));
    }
    public long getLong(CharArrayQueue param, SessionObject session) {
        return CharArray.getLong(getCharArray(param, session));
    }
    public boolean getBoolean(CharArrayQueue param, SessionObject session) {
        return CharArray.getBoolean(getCharArray(param, session));
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
if (debug) System.out.println("▽▽InputMobile#getFunc▽▽");
        CharArray ch = null;
        if (func == null) {
            ch = get();
        } else {
            func.trim().toLowerCase().replace("_","");
            if (func.length() == 0)              ch = get();
            //else if (func.equals("link"))        ch = getLink(param);
            //else if (func.equals("title"))       ch = getTitle(param);
            //else if (func.equals("description")) ch = getDescription(param);
            //else if (func.equals("disp"))        ch = getDisp(param,null);
            //else if (func.equals("view"))        ch = getDisp(param,null);
            else                                 ch = get();
        }
if (debug) System.out.println("△△InputMobile#getFunc△△");
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
if (debug) System.out.println("▽▽InputMobile#getParameter▽▽");
        if (func == null) {
            p.add(get());
        } else {
            func.trim().toLowerCase().replace("_","");
            if (func.length() == 0)                p.add(get());
            else if (func.equals("key"))           p.add(getKey(session));
            else if (func.equals("useragent"))     p.add(getUserAgent(session));
            else if (func.equals("carrier"))       p.add(getCarrier(session));
            else if (func.equals("deviceid"))      p.add(getDeviceID(session));
            else if (func.equals("devicename"))    p.add(getDeviceName(session));
            else if (func.equals("device"))        p.add(getDeviceName(session));
            else if (func.equals("browser"))       p.add(getBrowser(session));

            else if (func.equals("getkey"))       p.add(getKey(session));
            else if (func.equals("getuseragent"))  p.add(getUserAgent(session));
            else if (func.equals("getcarrier"))    p.add(getCarrier(session));
            else if (func.equals("getdeviceid"))   p.add(getDeviceID(session));
            else if (func.equals("getdevicename")) p.add(getDeviceName(session));
            else if (func.equals("getbrowser"))    p.add(getBrowser(session));

            else if (func.equals("ispc"))            p.add(isPC(session));
            else if (func.equals("ismobile"))        p.add(isMobile(session));
            else if (func.equals("ismobilexhtml"))   p.add(isMobile(session, session.isXHTML()));
            //else if (func.equals("isdocomo"))        p.add(isDocomo(session));
            //else if (func.equals("isdocomochtml"))   p.add(isDocomo(session, session.isCHTML()));
            //else if (func.equals("isdocomoxhtml"))   p.add(isDocomo(session, session.isXHTML()));
            //else if (func.equals("isau"))            p.add(isAu(session));
            //else if (func.equals("isvoda"))          p.add(isSoftBank(session));
            //else if (func.equals("isvodafone"))      p.add(isSoftBank(session));
            //else if (func.equals("isvodafonemml"))   p.add(isSoftBank(session, session.isMML()));
            //else if (func.equals("isvodafonexhtml")) p.add(isSoftBank(session, session.isXHTML()));
            //else if (func.equals("issoftbank"))      p.add(isSoftBank(session));
            else if (func.equals("isrobot"))         p.add(isRobot(session));
            else if (func.equals("ismobilenotrobot"))  p.add(isMobileAndNotRobot(session));
            else if (func.equals("ismobileandnotrobot"))  p.add(isMobileAndNotRobot(session));

            //else if (func.equals("issoftbankmml"))   p.add(isSoftBank(session, session.isMML()));
            //else if (func.equals("issoftbankxhtml")) p.add(isSoftBank(session, session.isXHTML()));

            else if (func.equals("ishtml5"))       p.add(isHTML5(session));
            else if (func.equals("isxhtml"))       p.add(isXHTML(session));
            else if (func.equals("isssl"))         p.add(isSSL(session));

            else if (func.equals("width"))         p.add(getWidth(param,session));
            else if (func.equals("height"))        p.add(getHeight(param,session));
            else if (func.equals("lcdwidth"))      p.add(getLCDWidth(param,session));
            else if (func.equals("lcdheight"))     p.add(getLCDHeight(param,session));

            else if (func.equals("getwidth"))      p.add(getWidth(param,session));
            else if (func.equals("getheight"))     p.add(getHeight(param,session));
            else if (func.equals("getlcdwidth"))   p.add(getLCDWidth(param,session));
            else if (func.equals("getlcdheight"))  p.add(getLCDHeight(param,session));

            else if (func.equals("get"))           p.add(getCharArray(param,session));
            else if (func.equals("getstring"))     p.add(getCharArray(param,session));
            else if (func.equals("getchararray"))  p.add(getCharArray(param,session));
            else if (func.equals("getint"))        p.add(""+getInt(param,session));
            else if (func.equals("getlong"))       p.add(""+getLong(param,session));
            else if (func.equals("getbool"))       p.add(getBoolean(param,session) ? "true" : "");
            else if (func.equals("getboolean"))    p.add(getBoolean(param,session) ? "true" : "");


            //else if (func.equals("view"))       p.add(getView());
            //else   p.add("");
            else   p.add("function[@mobile."+func+"] not found!");
        }
if (debug) System.out.println("△△InputMobile#getParameter△△");
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
//
// [end of InputMobile.java]
//

