//------------------------------------------------------------------------
//    InputSystem.java
//          システム関数  @SYSTEM で呼ばれる
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
//

package com.miraidesign.content.input;

import java.util.Calendar;
import java.text.SimpleDateFormat;

import com.miraidesign.system.SystemManager;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.Parameter;
import com.miraidesign.util.Util;

/**
    システム関数（システム）
*/
public class InputSystem extends InputItem {
    private static boolean debug = false;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    //private static SimpleDateFormat sdfJ = new SimpleDateFormat("MM年dd日");
    //-------------------
    // constructor
    //-------------------
    public InputSystem() {
        type.set("SYSTEM");
    }
    
    //-------------------
    // method
    //-------------------
    /** デフォルト関数 */
    public CharArray get() {
        CharArray ch =  new CharArray("miraidesign");
        return ch;
    }

    public void set(CharArray ch) {
    }

    public CharArray getVersion() {
        return new CharArray(com.miraidesign.common.Version.getVersionString());
    }
    public CharArray getVersionDate() {
        return new CharArray(com.miraidesign.common.Version.getVersionDate());
    }

    public CharArray getDebug() { // 試験用
        CharArray ch =  new CharArray("Powerd by http://www.miraidesign.com/");
        return ch;
    }
    
    /* 日付 平成○○年○月○日 の形で取得する */
    public CharArray getDate() {
        CharArray ch = new CharArray();
        
        Calendar cal = Calendar.getInstance();
        //long time = System.currentTimeMillis();
        //cal.setTimeInMillis(time);
        ch.add(Util.getWDate(cal));
        return ch;
    }
    /* 日付 平成○○年○○月○○日 の形で取得する */
    public CharArray getDate2() {
        CharArray ch = new CharArray();
        
        Calendar cal = Calendar.getInstance();
        //long time = System.currentTimeMillis();
        //cal.setTimeInMillis(time);
        ch.add(Util.getWDate2(cal));
        return ch;
    }
    
    
    /* 日付・時刻を yyyy/MM/dd HH:mm の形で取得する */
    public CharArray getTime() {
        CharArray ch = new CharArray();
        long time = System.currentTimeMillis();
        synchronized (sdfDateTime) {
            String str = sdfDateTime.format(new java.util.Date(time));
            ch.set(str);
        }
        return ch;
    }

    /* 日付・時刻を 指定パターンでフォーマットして取得する */
    public CharArray getTime(CharArray pattern) {
//System.out.println("★★getTime("+pattern+")");
        if (pattern == null || pattern.length() == 0) return getTime();
        CharArray ch = new CharArray();
        long time = System.currentTimeMillis();
        synchronized (sdf) {
            sdf.applyPattern(pattern.toString());
            String str = sdf.format(new java.util.Date(time));
            ch.set(str);
//System.out.println("★★str("+str+")");
        }
        return ch;
    }
    
    /* 和暦年を求める (明治以前は西暦になる) */
    public CharArray getWareki() {
       return getWareki(null);
    }
    public CharArray getWareki(CharArrayQueue param) {
        CharArray ch = new CharArray();
        int year = -1;
        if (param != null && param.size() > 0) {
            year = param.peek().getInt();
        }
        if (year < 0) {
            Calendar cal = Calendar.getInstance();
            //long time = System.currentTimeMillis();
            //cal.setTimeInMillis(time);
            year = cal.get(Calendar.YEAR);
        }
        ch.add(Util.wyear(year));
        return ch;
    }
    
    /* 元号を求める (明治以前は西暦になる) */
    public CharArray getGengo() {
        return getGengo(null);
    }
    public CharArray getGengo(CharArrayQueue param) {
        CharArray ch = new CharArray();
        int year = -1;
        if (param != null && param.size() > 0) {
            year = param.peek().getInt();
        }
        if (year < 0) {
            Calendar cal = Calendar.getInstance();
            //long time = System.currentTimeMillis();
            //cal.setTimeInMillis(time);
            year = cal.get(Calendar.YEAR);
        }
        
        ch.add(Util.gyear(year));
        return ch;
    }

    /* 元号+和暦年を求める (明治以前は西暦になる) */
    public CharArray getYear() {
        return getYear(null);
    }
    public CharArray getYear(CharArrayQueue param) {
        CharArray ch = new CharArray();
        int year = -1;
        if (param != null && param.size() > 0) {
            year = param.peek().getInt();
        }
        if (year < 0) {
            Calendar cal = Calendar.getInstance();
            //long time = System.currentTimeMillis();
            //cal.setTimeInMillis(time);
            year = cal.get(Calendar.YEAR);
        }
        ch.add(Util.gyear(year)+Util.wyear(year)+"年");
        return ch;
    }
    
    // test 
    public CharArray getText(CharArrayQueue param) {
        CharArray ch = new CharArray();
        if (param != null) {
            for (int i = 0; i < param.size(); i++) {
                ch.add(param.peek(i));
            }
        }
        return ch;
    }
    public Parameter getText(CharArrayQueue param, Parameter p) {
        if (param != null && param.size() > 0) {
            for (int i = 0; i < param.size(); i++) {
                p.add(param.peek(i));
            }
        }
        return p;
    }
    public Parameter getNo(CharArrayQueue param, Parameter p) {
        if (param != null && param.size() > 0) {
            String pre="";
            String post="";
            int size = param.peek().getInt();
            if (param.size() == 3) {
                pre = param.peek(0).toString();
                size = param.peek(1).getInt();
                post = param.peek(2).toString();
            }
            if (size <= 0) {   } // do nothing
            else if (size > 1000) p.add("1000 over!");
            else {
                for (int i = 1; i <= size; i++) {
                    p.add(pre+i+post);
                }
            }
        
        } else {
            p.add("");
        }
        return p;
    }
    
    /* param */
    public Parameter getNoText(CharArrayQueue param, Parameter p) {
        if (param != null && param.size() > 2) {
            int    check = param.peek().getInt();
            String text = param.peek(1).toString();
            int size = param.peek(2).getInt();
            int   limit = 0;
            if (param.size() > 3) {
                limit = param.peek(3).getInt();
            }
            
            if (size <= 0) {   } // do nothing
            else {
                for (int i = 1; i <= size; i++) {
                    if ((i % check == check) && (i != limit)) {
                        p.add(text);
                    } else {
                        p.add("");
                    }
                }
            }
        
        } else {
            p.add("");
        }
        return p;
    }
    
    /*
        関数呼び出し
        @param func 関数名 
        @param param 関数パラメータ（未サポート）
    */
    public CharArray getFunc(CharArray func, CharArrayQueue param) {
        return getFunc(func, param, 0);
    }
    public CharArray getFunc(CharArray func, CharArrayQueue param, int state) {
        CharArray ch = null;
        if (func == null) {
            ch = get();
        } else {
            func.trim().toLowerCase();
            func.replace("_","");
            if (func.length() == 0)             ch = get();
            else if (func.equals("version"))    ch = getVersion();
            else if (func.equals("versiondate")) ch = getVersionDate();
            else if (func.equals("versiontime")) ch = getVersionDate();
            else if (func.equals("debug"))      ch = getDebug();
            else if (func.equals("time"))  {
                if (param == null || param.size() == 0) ch = getTime();
                else  {
                    //param.dumpQueue();  // debug
                    ch = getTime(param.peek());
                }
            } 
            else if (func.equals("date"))   ch = getDate();
            else if (func.equals("date2"))  ch = getDate2();
            else if (func.equals("wareki")) ch = getWareki(param);
            else if (func.equals("gengo"))  ch = getGengo(param);
            else if (func.equals("year"))   ch = getYear(param);
            else if (func.equals("no"))     ch = getText(param);
            else if (func.equals("text"))   ch = getText(param);
            //else if (func.equals("notext")) ch = getNoText(param);
            else if (func.equals("httpurl")) ch = new CharArray(SystemManager.httpURL);
            else if (func.equals("httpsurl")) ch = new CharArray(SystemManager.httpsURL);
            else if (func.equals("cacheserversurl")) ch = new CharArray(SystemManager.cacheServerURL);
            else if (func.equals("hostname")) ch = new CharArray(SystemManager.hostName);
            else if (func.equals("hosturl")) ch = new CharArray(SystemManager.hostURL);
            else                            ch = get();
        }
        return ch;
    }
    public Parameter getParameter(CharArray func, CharArrayQueue param, Parameter p) {
        if (func == null) {
            p.add(get());
        } else {
            func.trim().toLowerCase();
            func.replace("_","");
            if (func.length() == 0)                 p.add(get());
            else if (func.equals("version"))        p.add(getVersion());
            else if (func.equals("versiondate"))   p.add(getVersionDate());
            else if (func.equals("versiontime"))   p.add(getVersionDate());
            else if (func.equals("debug"))          p.add(getDebug());
            else if (func.equals("time"))  {
                if (param == null || param.size() == 0) p.add(getTime());
                else  {
                    //param.dumpQueue();  // debug
                    p.add(getTime(param.peek()));
                }
            } 
            else if (func.equals("date"))           p.add(getDate());
            else if (func.equals("date2"))          p.add(getDate2());
            else if (func.equals("wareki"))         p.add(getWareki(param));
            else if (func.equals("gengo"))          p.add(getGengo(param));
            else if (func.equals("year"))           p.add(getYear(param));
            else if (func.equals("no"))             getNo(param,p);
            else if (func.equals("text"))           getText(param,p);
            else if (func.equals("notext"))         getNoText(param,p);
            //else if (func.equals("dir"))            p.add(getDir());
            //else if (func.equals("url"))            p.add(getURL());
            else if (func.equals("httpurl"))        p.add(SystemManager.httpURL);
            else if (func.equals("httpsurl"))       p.add(SystemManager.httpsURL);
            else if (func.equals("hostname"))       p.add(SystemManager.hostName);
            else if (func.equals("hosturl"))        p.add(SystemManager.hostURL);
            else if (func.equals("cacheserverurl")) p.add(SystemManager.cacheServerURL);
            else   p.add("");
        }
        return p;
    }
    public Parameter getParameter(CharArray func, CharArrayQueue param, Parameter p, SessionObject session) {
        if (func == null) {
            p.add(get());
        } else {
            func.trim().toLowerCase();
            func.replace("_","");
            if (func.length() == 0)                 p.add(get());
            else if (func.equals("version"))        p.add(getVersion());
            else if (func.equals("versiondate"))   p.add(getVersionDate());
            else if (func.equals("versiontime"))   p.add(getVersionDate());
            else if (func.equals("debug"))          p.add(getDebug());
            else if (func.equals("time"))  {
                if (param == null || param.size() == 0) p.add(getTime());
                else  {
                    //param.dumpQueue();  // debug
                    p.add(getTime(param.peek()));
                }
            } 
            else if (func.equals("date"))           p.add(getDate());
            else if (func.equals("date2"))          p.add(getDate2());
            else if (func.equals("wareki"))         p.add(getWareki(param));
            else if (func.equals("gengo"))          p.add(getGengo(param));
            else if (func.equals("year"))           p.add(getYear(param));
            else if (func.equals("no"))             getNo(param,p);
            else if (func.equals("text"))           getText(param,p);
            else if (func.equals("notext"))         getNoText(param,p);
            //else if (func.equals("dir"))            p.add(getDir());
            //else if (func.equals("url"))            p.add(getURL());
            else if (func.equals("httpurl"))        p.add(session.getHttpURL());
            else if (func.equals("httpsurl"))       p.add(session.getHttpsURL());
            else if (func.equals("registurl"))       p.add(session.getRegistURL());
            else if (func.equals("cacheserverurl"))    p.add(session.getCacheServerURL());
            else if (func.equals("hostname"))       p.add(SystemManager.hostName);
            else if (func.equals("hosturl"))        p.add(session.getHostURL());
            else if (func.equals("serverkey"))      p.add(session.getModuleManager().getServerKey());
            else if (func.equals("getserverkey"))      p.add(session.getModuleManager().getServerKey());
            else if (func.equals("servername"))     p.add(session.getModuleManager().getServerName());
            else if (func.equals("getservername"))     p.add(session.getModuleManager().getServerName());
            
            //else   p.add("");
            else   p.add("function[@system."+func+"] not found!");
        }
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
// [end of InputSystem.java]
//

