//------------------------------------------------------------------------
//    AbstractServlet.java
//             (abstract)
//             Copyright (c) MiraiDesign 2010-19 All Rights Reserved.
//------------------------------------------------------------------------
//      
//------------------------------------------------------------------------
// 

package com.miraidesign.servlet;

import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.miraidesign.common.SystemConst;
//import com.miraidesign.image.EmojiConverter;
import com.miraidesign.session.SessionManager;
import com.miraidesign.session.SessionObject;
import com.miraidesign.session.UserAgent;
import com.miraidesign.system.GCManager;
import com.miraidesign.system.SiteManager;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.Crypt62;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.Util;

/**
 *  AbstructServlet
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public abstract class AbstractServlet extends HttpServlet 
            /*implements SingleThreadModel */
{
    /*static*/ protected boolean debug = (SystemConst.debug && true);       // 
    /*static*/ protected boolean debugUA = (SystemConst.debug && false);     // 
    /*static*/ protected boolean debugHeader = (SystemConst.debug && false);  // false
    /*static*/ protected boolean debugMulti = (SystemConst.debug && false); // false
    /*static*/ protected boolean debugPath  = (SystemConst.debug && true); 
    /*static*/ protected boolean debugPath2  = (SystemConst.debug && false); // false
    /*static*/ protected boolean debugUTF  = (SystemConst.debug && false);
    /*static*/ protected boolean debugContext  = (SystemConst.debug && false);   // false

    /*static*/ protected boolean debugLog        = (SystemConst.debug && true);
    
    /** デバッグログ表示を切り替える */
    public void setAllDebugLog(boolean mode) {
        debug           = mode;
        debugHeader     = mode;
        debugUA         = mode;
        debugMulti      = mode;
        debugPath       = mode;
        debugPath2      = mode;
        debugUTF        = mode;
        debugContext    = mode;
        //debugLog        = mode;
    }
    /** デバッグログ表示を切り替える */
    public void setDebugLog(boolean mode) {
        debug           = mode;
        debugHeader     = mode;
        debugUA         = mode;
        //debugMulti      = mode;
        debugPath       = mode;
        //debugPath2      = mode;
        //debugUTF        = mode;
        //debugContext    = mode;
        //debugLog        = mode;
    }
    
    static private boolean localDebug = false;
    static private boolean convertCSS = true;  // 
    
    static public void setConvertCSS(boolean mode) { convertCSS = mode;}
    static public boolean getConvertCSS() { return convertCSS; }
    
    static volatile int accessCount = 0;
    
    protected SimpleDateFormat sdf;
    protected SimpleDateFormat sdf1;
    
    //protected Hashtable hashParameter  = new Hashtable();
    
    protected String contentType0 = "text/html; charset=Shift_JIS";
    protected String contentTypeSJIS = "text/html; charset=Shift_JIS";
    protected String contentTypeUTF = "text/html; charset=utf-8";
    protected String contentTypeSJISXML = "application/xhtml+xml; charset=Shift_JIS";
    protected ServletLog log;                // 
    
    public void init()  throws ServletException {
        super.init();
        log = ServletLog.getInstance();
        debug &= SystemConst.debug;
        debugHeader &= SystemConst.debug;
    }
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        log = ServletLog.getInstance();
        debug &= SystemConst.debug;
        debugHeader &= SystemConst.debug;
    }
    
    /***
    public void destory() {
        super.destroy();
    }
    **/
    /**
        GET
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        int count = 0;
        synchronized (log) {
            count = ++accessCount;
        }
        response.setContentType(contentTypeSJIS);
        boolean display = debug && true;
        if (log == null) {
            if (display) System.out.println(count+"|doGet() log=null!");
            log = ServletLog.getInstance();
        }
        try {
            if (debugLog && request.getRequestURI().indexOf("/nologs/") >= 0) {
                setDebugLog(false); 
                display = debug && true;
            }
            if (sdf == null) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)");
                sdf.setTimeZone(TimeZone.getTimeZone(System.getProperty("user.timezone")));
            }
            if (sdf1 == null) {
                sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf1.setTimeZone(TimeZone.getTimeZone(System.getProperty("user.timezone")));
            }
            if (display) {
                java.util.Date date = new java.util.Date(System.currentTimeMillis());
                synchronized (sdf) {
                    System.out.println(count+"|━━━━━━━━━━━━━━━━ "+sdf.format(date)+ " ━━━━▽get");
                }
            }
            if (debugHeader) debugHeaders(request,count);
            //Hashtable hashParameter = getParameters(request);
            doGetJob(request, response, count);
            //GCManager.execute();
            if (display) {
                java.util.Date date = new java.util.Date(System.currentTimeMillis());
                synchronized (sdf) {
                    System.out.println(count+"|──────────────── "+sdf.format(date)+ " ────△get");
                }
            }
            if (debugLog && request.getRequestURI().indexOf("/nologs/") >= 0) setDebugLog(true); // 2013-09-25 test
            
        } catch (Exception ex) {
            System.out.println(count+"|AbstractServlet#doGet() Error! ");
            ex.printStackTrace();
        }
    }
    /**
        POST
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        int count = 0;
        synchronized (log) {
            count = ++accessCount;
        }
        boolean display = debug && true;
        response.setContentType(contentTypeSJIS);
        if (log == null) {
            if (debug) System.out.println(count+"|doPost() log=null!");
            log = ServletLog.getInstance();
        }
        try {
            if (debugLog && request.getRequestURI().indexOf("/nologs/") >= 0) {
                setDebugLog(false); 
                display = debug && true;
            }
            if (sdf == null) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)");
                sdf.setTimeZone(TimeZone.getTimeZone(System.getProperty("user.timezone")));
            }
            if (sdf1 == null) {
                sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf1.setTimeZone(TimeZone.getTimeZone(System.getProperty("user.timezone")));
            }
            if (display) {
                java.util.Date date = new java.util.Date(System.currentTimeMillis());
                synchronized (sdf) {
                    System.out.println(count+"|━━━━━━━━━━━━━━━━ "+sdf.format(date)+" ━━━━▽post");
                }
            }
            if (debugHeader) debugHeaders(request,count);
            //Hashtable hashParameter = getParameters(request);
            doPostJob(request, response,count);
            //GCManager.execute();
            if (display) {
                java.util.Date date = new java.util.Date(System.currentTimeMillis());
                synchronized (sdf) {
                    System.out.println(count+"|──────────────── "+sdf.format(date)+" ────△post");
                }
            }
        } catch (Exception ex) {
            System.out.println(count+"|AbstractServlet#doPost() Error! ");
            ex.printStackTrace();
        }
    }
    /**
        PUT         2014-07-07
    */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        int count = 0;
        synchronized (log) {
            count = ++accessCount;
        }
        boolean display = debug && true;
        response.setContentType(contentTypeSJIS);
        if (log == null) {
            if (debug) System.out.println(count+"|doPut() log=null!");
            log = ServletLog.getInstance();
        }
        try {
            if (debugLog && request.getRequestURI().indexOf("/nologs/") >= 0) setDebugLog(false); 
            if (sdf == null) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)");
                sdf.setTimeZone(TimeZone.getTimeZone(System.getProperty("user.timezone")));
            }
            if (sdf1 == null) {
                sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf1.setTimeZone(TimeZone.getTimeZone(System.getProperty("user.timezone")));
            }
            if (display) {
                java.util.Date date = new java.util.Date(System.currentTimeMillis());
                synchronized (sdf) {
                    System.out.println(count+"|━━━━━━━━━━━━━━━━ "+sdf.format(date)+" ━━━━▽put");
                }
            }
            if (debugHeader) debugHeaders(request,count);
            //Hashtable hashParameter = getParameters(request);
            doPutJob(request, response,count);
            //GCManager.execute();
            if (display) {
                java.util.Date date = new java.util.Date(System.currentTimeMillis());
                synchronized (sdf) {
                    System.out.println(count+"|──────────────── "+sdf.format(date)+" ────△put");
                }
            }
        } catch (Exception ex) {
            System.out.println(count+"|AbstractServlet#doPut() Error! ");
            ex.printStackTrace();
        }
    }
    /**
        DELETE         2014-07-07
    */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        int count = 0;
        synchronized (log) {
            count = ++accessCount;
        }
        boolean display = debug && true;
        response.setContentType(contentTypeSJIS);
        if (log == null) {
            if (debug) System.out.println(count+"|doDelete() log=null!");
            log = ServletLog.getInstance();
        }
        try {
            if (debugLog && request.getRequestURI().indexOf("/nologs/") >= 0) setDebugLog(false); 
            if (sdf == null) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)");
                sdf.setTimeZone(TimeZone.getTimeZone(System.getProperty("user.timezone")));
            }
            if (sdf1 == null) {
                sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf1.setTimeZone(TimeZone.getTimeZone(System.getProperty("user.timezone")));
            }
            if (display) {
                java.util.Date date = new java.util.Date(System.currentTimeMillis());
                synchronized (sdf) {
                    System.out.println(count+"|━━━━━━━━━━━━━━━━ "+sdf.format(date)+" ━━━━▽delete");
                }
            }
            if (debugHeader) debugHeaders(request,count);
            //Hashtable hashParameter = getParameters(request);
            doDeleteJob(request, response,count);
            //GCManager.execute();
            if (display) {
                java.util.Date date = new java.util.Date(System.currentTimeMillis());
                synchronized (sdf) {
                    System.out.println(count+"|──────────────── "+sdf.format(date)+" ────△delete");
                }
            }
        } catch (Exception ex) {
            System.out.println(count+"|AbstractServlet#doDelete() Error! ");
            ex.printStackTrace();
        }
    }
    
    
    /** サブクラスがオーバーロードする */
    public abstract void doGetJob(HttpServletRequest request, HttpServletResponse response, int count);
    /** サブクラスがオーバーロードする */
    public abstract void doPostJob(HttpServletRequest request, HttpServletResponse response, int count);
    
    /** サブクラスが必要に応じてオーバーライドする */
    public /*abstract*/ void doPutJob(HttpServletRequest request, HttpServletResponse response, int count) {
        // do nothing
    }
    /** サブクラスが必要に応じてオーバーライドする */
    public /*abstract*/ void doDeleteJob(HttpServletRequest request, HttpServletResponse response, int count) {
        // do nothing
    }
    
    //--------------------------------------------------------------------------------
    protected String getParameter(String key, Hashtable hashParameter) {
        String[] str = (String[])hashParameter.get(key);
        if (str ==null) return "";
        else return str[0];
    }
    // 
    private SessionObject getSessionObject(HttpServletRequest request) {
        SessionObject session = null;
        do {
            int sessionID = 0;
            String value = request.getParameter(SystemConst.sessionIDKey[0]);
            if (value != null) {
                sessionID = CharArray.getInt(value);
            } else { // 暗号化の場合
                value = request.getParameter(SystemConst.sessionIDKey[0]);
                if (value != null) {
                    sessionID = (int)Crypt62.decode(value.trim());
                }
            }
            if (sessionID >= 0) {
                session = SessionManager.getSessionObject(sessionID, false, debug);
            }
        } while (false);
        if (session != null) {
            session.remoteAddr = request.getRemoteAddr();
            session.remoteHost = request.getRemoteHost();
        }
        return session;
    }

    /**
        パラメータの取得。 漢字コードはMS932とみなした変換を行っています。
    */
    protected Hashtable<String,String[]> getParameters(HttpServletRequest request) throws IOException {
        return getParameters(request,0);
    }
    
    protected Hashtable<String,String[]> getParameters(HttpServletRequest request,int count,
                        Hashtable<String,UploadInfo> hashFileData, UploadJson jsonBody) throws IOException {
        String method = request.getMethod();
        String type = request.getContentType();
        //System.out.println("★★contentType:"+type);
        int size = request.getContentLength();
        if (method == null || type == null || !method.equals("POST") 
                || (!type.startsWith("multipart/form-data")
                && !type.startsWith("application/json"))
           ) return getParameters(request,count);

        
        if (type.startsWith("application/json")) {
            return getParametersJSON(request,count,jsonBody);
        }
        
        // 以降multiform処理
        if (debug) {
            System.out.println(((count>0)? count+"|":"")+"▼ MultipartParameter get===========================:"+size);
        }
        Hashtable<String,String[]> hash = new Hashtable<String,String[]>();
        if (SystemManager.convertContextPath > 0) {  // 2014-06-04 追加
            getContextParameters(request, hash, count);
        }

        boolean isUTF = true;
        //String _enc = request.getCharacterEncoding();
        //if (debug) System.out.println("encoding:"+_enc);
        //if (_enc != null &&  _enc.equals("UTF-8")) isUTF = true;
        
        //String ua = request.getHeader("user-agent");
        //if (ua.indexOf("UP.Browser") >= 0) {
        //    isUTF = (ua.indexOf("UP.Link") < 0) && request.getMethod().equals("POST");
        //}

        String boundary = "";
        int pos = type.indexOf("boundary=");
        if (pos >= 0) {
            boundary = type.substring(pos+9);
            boundary.trim();
        }
        String szBoundary = "--" + boundary;
        
        String charCode= isUTF ? "UTF-8" : "MS932";
        SessionObject session = getSessionObject(request);
        if (session != null && session.isPC()) {
            String s = session.getCharCode();
            if (s.length()> 0) charCode = s;
        }
        if (debugMulti) println(session, count, "★charCode"+charCode+" session:"+session);
        ServletInputStream in = request.getInputStream();
        byte[] buff = new byte[64*1024];
        CharToken token = CharToken.pop();
        CharToken token2 = CharToken.pop();
        do {
        
            try {
                int len = readLine(in, buff, 0, buff.length);
                if (len < 0) break;
                String szLine = (len == 0) ? "" : new String(buff, 0, len, charCode);
                if (!szLine.startsWith(szBoundary)) {
                    if (debug) System.out.println(count+"|boundary missing:"+szLine+":"+szLine.length()); 
                    break;
                }
                if (debugMulti) System.out.println(count+"|"+szLine+":"+szLine.length());
                boolean reading = true; // データ読み込み中
                while (reading) {
                    //ByteArrayOutputStream content = new ByteArrayOutputStream();
                
                    len = readLine(in, buff, 0, buff.length);
                    if (len < 0) break;
                    szLine = (len == 0) ? "" : new String(buff, 0, len, charCode);
                    if (debugMulti) System.out.println(count+"|"+szLine+":"+szLine.length()); 
                    
                    if (!szLine.toLowerCase().startsWith("content-disposition:")) continue;
                    token.set(szLine.substring(20),";");
                    if (token.size() < 2) {
                        System.out.println(count+"|ERROR:token size:"+token.size());
                        break;
                    }
                    if (!token.get(0).trim().equals("form-data")) {
                        System.out.println(count+"|ERROR: not form-data");
                        break;
                    }
                    CharArray nameKey = new CharArray();
                    CharArray fileKey = new CharArray();   // length 0なら存在しない
                    CharArray name =    parseKeyAndValue(token.get(1), nameKey);
                    CharArray filename = parseKeyAndValue(token.get(2), fileKey);
                    len = readLine(in, buff, 0, buff.length);
                    String szName = name.toString();
                    if (fileKey.length() == 0) { // 通常データ
                        if (len < 0) {
                            System.out.println(count+"|ERROR:data not found (terminated)");
                            break;
                        }
                        CharArray data = new CharArray();
                        for (int i = 0; ; i++) {    // 無限ループ
                            len = readLine(in, buff, 0, buff.length);
                            if (len < -1) break;
                            szLine = (len == 0) ? "" : new String(buff, 0, len, charCode);
                            if (szLine.startsWith(szBoundary)) {
                                break;
                            }
                            if (i > 0) data.add("\n");  // テキストエリア用？
                            data.add(szLine);
                        }
                        //@@// アイテムにセットする
                        String[] strOrg = (String[])hash.get(szName);
                        if (strOrg == null) {
                            hash.put(szName, new String[] { data.toString()} );
                        } else {
                            String[] strNew = new String[strOrg.length+1];  // 効率悪い
                            System.arraycopy(strOrg,0,strNew,0,strOrg.length);
                            strNew[strOrg.length] = data.toString();
                            hash.remove(szName);    // 必要か
                            hash.put(szName, strNew);
                        }
                        if (name.equals(SystemConst.sessionIDKey[0])) {
                            int sessionID = data.getInt();
                            session = SessionManager.getSessionObject(sessionID);
                            if (session != null && session.isPC()) {
                                String s = session.getCharCode();
                                if (s.length()> 0) charCode = s;
                            }
                        } else if (name.equals(SystemConst.sessionIDKey[1])) {  // 
                            int sessionID = (int)Crypt62.decode(data.toString());
                            session = SessionManager.getSessionObject(sessionID);
                            if (session != null && session.isPC()) {
                                String s = session.getCharCode();
                                if (s.length()> 0) charCode = s;
                            }
                        }
                        
                        if (debugMulti) {
                            System.out.print(count+"|-----");
                            System.out.println(count+"|name:"+name+"  "+data+":length:"+data.length());
                        }
                    } else {                  // ファイル
                        hash.put(szName, new String[] {filename.toString()});     // 
                        if (len < 0) {
                            System.out.println(count+"|ERROR:content type not found (terminated)");
                            break;
                        }
                        szLine = (len == 0) ? "" : new String(buff, 0, len, charCode);
                        if (debugMulti) System.out.println(count+"["+szLine+"]"+len+":"+szLine.length());
                        String contentType="";
                        if (len != 0) {
                            if (!szLine.toLowerCase().startsWith("content-type")) {
                                System.out.println(count+"|ERROR:content-type not found(1)");
                                break;
                            }
                            int index = szLine.indexOf(":");
                            if (index < 0) {
                                System.out.println(count+"|ERROR:content-type not found(2)");
                                break;
                            }
                            contentType = szLine.substring(index+1).trim();
                            if (debug) System.out.println(count+"|contentType:"+contentType);
                            
                            len = readLine(in, buff, 0, buff.length);
                            if (len != 0) {
                                System.out.println(count+"|ERROR: len != 0 "+len);
                                break;
                            }
                        }
                        UploadInfo ui = new UploadInfo(); // 
                        ui.contentType = contentType;
                        ui.fileName = filename;
                        int line = 0;
                        for (int i = 0; ; i++) {    // 
                            len = readNext(in, buff, 0, buff.length);
                            if (len < 0) break; // -1 -> 0
                            szLine = (len == 0) ? "" : new String(buff, 0, szBoundary.length(), charCode);
                            if (szLine.startsWith(szBoundary)) { // 
                                if (debugMulti) System.out.println(count+"|"+szLine+":"+len);
                                break;
                            }
                            
                            if (debugMulti) System.out.println(count+"[len]"+len);
                            ui.add(buff,0,len);
                            if (buff[len-1] == 0x0A) line++;
                        }
                        if (ui.length < 2) {
                            System.out.println(count+"|Error: size="+len);
                            break;
                        }
                        ui.length -= 2;
                        ui.rowCount = line;
                        
                        szName += Util.format0(hashFileData.size(),3);
                        hashFileData.put(szName, ui);  // データをメモリに保管
                        
                        if (debug) System.out.println(count+"|"+filename+"-> ["+szName+":"+ui.length+"]");
                        
                        if (debugMulti) {
                            if (localDebug && filename.length() > 0) {
                                File file =  new File(filename.toString());
                                int orgsize = (int)file.length();
                                System.out.println(count+"|"+((ui.length==orgsize)?"OK":"NG")+
                                       " write:"+szName+":"+ui.length+"  org:"+filename+":"+orgsize);
                            } else {
                                System.out.println(count+"|line:"+ui.rowCount+" size:"+ui.length);
                            }
                        }
                    }   // endif 
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        } while (false);
        CharToken.push(token2);
        CharToken.push(token);
        in.close();
        
        CharArray total = new CharArray();
        for (Enumeration e = hash.keys(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            String[] strs = (String[])hash.get(key);
            if (total.length() >0) total.add('&');
            total.add(key);
            total.add('=');
            for (int i = 0; i < strs.length; i++) {
                if (i > 0) total.add(';');
                total.add(strs[i]);
            }
        }
        
        hash.put("$$$Parameters$$$",new String[] {total.toString()});
        
        if (debug) {
            System.out.println(((count>0)? count+"|":"")+"▲ MultipartParameter end===========================");
        }
        
        return hash;
    }
    protected Hashtable<String,String[]> getParametersJSON(HttpServletRequest request,int count,
                        UploadJson jsonBody) throws IOException {
        String method = request.getMethod();
        String type = request.getContentType();

        int size = request.getContentLength();
        if (debug) {
            System.out.println(((count>0)? count+"|":"")+"▼ JSON get===========================:"+size);
        }
        Hashtable<String,String[]> hash = new Hashtable<String,String[]>();
        if (SystemManager.convertContextPath > 0) { 
            getContextParameters(request, hash, count);
        }

        //if (debugMulti) println(session, count, "★charCode"+charCode+" session:"+session);
        ServletInputStream in = request.getInputStream();
        do {
            try {
                jsonBody.setStream(in);
                jsonBody.read();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        } while (false);
        in.close();
        
        if (debugMulti) System.out.println(jsonBody);
        
        hash.put("$$$Parameters$$$",new String[] {""});
        
        if (debug) {
            System.out.println(((count>0)? count+"|":"")+"▲ JSON end===========================");
        }
        
        return hash;
    }
    

    protected Hashtable<String,String[]> getParameters(HttpServletRequest request,int count) throws IOException {
        Hashtable<String,String[]> data = new Hashtable<String,String[]>();
        boolean display = debug && count >= 0;
        String ua = request.getHeader("user-agent");

        /** コンテキストパラメータを取得する*/
        if (SystemManager.convertContextPath > 0) getContextParameters(request, data, count);

        //SessionObject session = getSessionObject(request);

       if (display) {
            System.out.println(((count>0)? count+"|":"")+"▼ Parameter get==");
       }
       
        boolean isUTF = true;
        int carrier = -1;

        String _enc = request.getCharacterEncoding();
        if (ua != null) {
            if (_enc != null &&  _enc.equals("UTF-8")) isUTF = true;
        }
        
        //boolean convert = (carrier >= 0 && SystemManager.convertEmoji);
        
        if (display) {
            if (isUTF) request.setCharacterEncoding("UTF-8");   //8859_1");   //"MS932");   // 
        }
        
        CharArray total = new CharArray();

        String charCode="MS932";
        SessionObject session = getSessionObject(request);

        if (session != null) {
            if (session.isPC()) {
                String s = session.getCharCode();
                if (s.length()> 0) charCode = s;
            }
        } else {  // セッションがまだ生成されていない
            if (carrier == -1) {
                String[] str = data.get(SystemConst.siteKey);
                if (str != null && str.length > 0) {
                    int site_id = CharArray.getInt(str[0]);
                    if (site_id > 0) {
                        ModuleManager mm = SiteManager.get(site_id);
                        if (mm != null && mm.charCode != null && mm.charCode.length() > 0) {
                            charCode = mm.charCode;
                        } else if (SystemManager.charCode != null && SystemManager.charCode.length() > 0) {
                            charCode = SystemManager.charCode;
                        }
                    }
                }
            }
        }
        
        if (debugUA) {
            String szDate;
            java.util.Date date = new java.util.Date(System.currentTimeMillis());
            synchronized (sdf1) {
                szDate = sdf1.format(date);
            }
            CharArray chUA = CharArray.pop(ua);
            CharArray ch = CharArray.pop();
            
            if (count > 0) { 
                ch.format(count); ch.add('|');
            }
            if (session != null && data != null) {
                String s_id = getParameter(SystemConst.siteKey,data);
                if (s_id != null) ch.add(s_id);
                if (session.userID != null && session.userID.length() > 0) {
                    ch.add('(');ch.add(session.userID);ch.add(')');
                } else if (session.userName != null && session.userName.length() > 0) {
                    ch.add('(');ch.add(session.userName);ch.add(')');
                }
                long l = (System.currentTimeMillis() - session.getStartTime())/1000L;
                ch.add(l/60);
                ch.add(":");
                ch.format(l%60,10,2,'0');
                ch.add(" ");
            }
            
            ch.add("user-agent:");
            ch.add(ua);
            if (session == null) {
                if (UserAgent.checkRobot(chUA, request.getRemoteAddr())) ch.add(" [Robot]");
            } else {
                if (UserAgent.checkRobot(chUA, session.getRemoteIP())) ch.add(" [Robot]");
            }
            ch.add(" session:");
            ch.format(SessionManager.getSessionCount());
            ch.add(" ");
            ch.add(szDate);
            System.out.println(ch.toString());
            
            CharArray.push(ch);
            CharArray.push(chUA);
        }
        
        int no = 1;
        for (Enumeration e = request.getParameterNames(); e.hasMoreElements(); no++) {
            String key = (String)e.nextElement();
            String[] values = request.getParameterValues(key);
            String[] strs = new String[values.length];

            if (total.length() >0) total.add('&');
            total.add(key);
            total.add('=');
            
            for (int i = 0; i < values.length; i++) {
                //strs[i] = new String(values[i].getBytes("8859_1"), "MS932");
                
                // クロスサイト・スクリプティング対応
                String str;
                
                // for debug
                String str1= values[i];    // not convert
                String str2="";    // FigCharConvert
                
                if (isUTF) {       // 
                    
                    byte[] bytes = values[i].getBytes("8859_1");
                    if (debugUTF) {
                        byte[] bytes2 = values[i].getBytes("Shift_JIS");
                        byte[] bytes3 = values[i].getBytes("UTF-8");
                        System.out.println("-------------------------------------");
                        System.out.printf("[%02d] bytes(8859_1):",no);
                        for (int ii = 0; ii < bytes.length; ii++) {
                            System.out.printf(" %02x",(int)bytes[ii]);
                        }
                        
                        System.out.print(" (Shift_JIS):");
                        for (int ii = 0; ii < bytes2.length; ii++) {
                            System.out.printf(" %02x",(int)bytes2[ii]);
                        }
                        
                        System.out.print(" (UTF-8):");
                        for (int ii = 0; ii < bytes3.length; ii++) {
                            System.out.printf(" %02x",(int)bytes3[ii]);
                        }
                        
                        System.out.print("\n              values:");
                        for (int ii = 0; ii < values[i].length(); ii++) {
                            System.out.printf(" %04x",(int)values[i].charAt(ii));
                        }
                        
                    }
                    
                    if (convertCSS) str = CharArray.replaceTag(values[i]).toString();
                    else            str = values[i];
                    
                } else { // ! isUTF
                    byte[] bytes = values[i].getBytes("8859_1");
                    
                    if (debugUTF) {
                        System.out.println("-------------------------------------");
                        System.out.printf("[%02d] bytes(8859_1):",no);
                        for (int ii = 0; ii < bytes.length; ii++) {
                            System.out.printf(" %02x",(int)bytes[ii]);
                        }
                        
                        System.out.print("\n       values(");
                        for (int ii = 0; ii < values[i].length(); ii++) {
                            System.out.printf(" %04x",(int)values[i].charAt(ii));
                        }
                        
                        System.out.println(")");
                    }

                    if (convertCSS) str = CharArray.replaceTag(new String(bytes, charCode)).toString();
                    else            str = new String(bytes, charCode);
                    if (debugUTF) str2 = values[i];
                }
                CharArray ch = CharArray.pop(str);
                strs[i] = ch.toString();
                CharArray.push(ch);

                if (i > 0) total.add(';');
                total.add(strs[i]);
                if (debugUTF) {
                    System.out.print("["+key+":u]"+str1+"(");
                    for (int ii = 0; ii < str1.length(); ii++) {
                        char c = str1.charAt(ii);
                        if (isUTF) {
                            System.out.printf("%04x ", (int)c);
                        } else {
                            System.out.printf("%02x ", (byte)c);
                        }
                    }
                    System.out.println(")"+str1.length()+":"+str2+";");
                }
            } // next;
            data.put(key, strs);
            
            if (display) {
                CharArray ch = CharArray.pop();
                if (count>0) ch.format(count);
                ch.add('['); ch.add(key); ch.add(']');
                for (int i = 0; i < values.length; i++) {
                    ch.add(values[i]); 
                    if (i+1 < values.length) ch.add(':');
                }
                ch.add('/');
                for (int i = 0; i < strs.length; i++) {
                    ch.add(strs[i]); 
                    if (i+1 < strs.length) ch.add(':');
                }
                System.out.println(ch);
                CharArray.push(ch);
            }
        }
        //data.put("$$$Parameters$$$",total);
        data.put("$$$Parameters$$$",new String[] {total.toString()});
        
        // debug
        if (display) {
            System.out.println(((count>0)? count+"|":"")+"▲ Parameter end==");
        }
        return data;
    }

    /**
        ヘッダーの取得。サイト来訪時に一回だけ呼ばれます。<br>
        keyは小文字変換とアンダースコア→ハイフン変換をして格納されます。
    */
    protected Hashtable<CharArray,CharArray> getHeaders(HttpServletRequest request,Hashtable hashParameter) throws IOException {
        return getHeaders(request, hashParameter,0);
    
    }
    protected Hashtable<CharArray,CharArray> getHeaders(HttpServletRequest request,
                                  Hashtable hashParameter,
                                  int count) throws IOException {
        Hashtable<CharArray,CharArray> data = new Hashtable<CharArray,CharArray>();
        boolean display = false;    //debug && count >= 0 && !debugHeader;
        if (display) {
            System.out.println(((count>0)? count+"|":"")+
                    "▼ Header get====================================");
        }
        boolean convert = SystemManager.ini.getBoolean("[UserAgentEmurator]","convert");
        for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            String value = (String)request.getHeader(key);
            CharArray chKey   = new CharArray(key).toLowerCase('_','-').trim();
            CharArray chValue = new CharArray(value).trim();
            if (convert && chKey.equals("user-agent")) {    // 
                String str = getParameter("emu",hashParameter);
                if (str != null && str.length()>0) {
                    CharArray ch = SystemManager.ini.get("[UserAgentEmurator]",str);
                    if (ch != null) {
                        System.out.println(((count>0)? count+"|":"")+
                                "UserAgentを["+chValue+"]"+
                                           "       から["+ch+"]に変更します");
                        chValue.set(ch);
                    }
                }
            }
            data.put(chKey, chValue);
            if (display) {
                CharArray ch = CharArray.pop();
                if (count > 0) ch.format(count);
                ch.add('['); ch.add(key); ch.add(']');
                ch.add(value); 
                //ch.add(':'); //ch.add(str);
                System.out.println(ch);
                CharArray.push(ch);
            }
        }
        if (display) {
            System.out.println(((count>0)? count+"|":"")+
                "▲ Header end====================================");
        }
        
        return data;
    }

    protected void debugHeaders(HttpServletRequest request) throws IOException {
        debugHeaders(request, 0);
    }
    protected void debugHeaders(HttpServletRequest request, int count) throws IOException {
        if (debug) {
            System.out.println(((count>0)? count+"|":"")+
                    "▼ debug Header start====================================");
            CharArray ch = CharArray.pop();
            for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
                ch.reset();
                String key = (String)e.nextElement();
                String value = (String)request.getHeader(key);
                if (count>0) ch.format(count);
                ch.add('['); ch.add(key); ch.add(']');ch.add(value); 
                //ch.add(':'); //ch.add(str);
                System.out.println(ch);
            }
            CharArray.push(ch);
            System.out.println(((count>0)? count+"|":"")+
                    "▲ debug Header end====================================");
        }
        //getContextParameters(request, count);  //@@// 
        
    }

    /** コンテキストパス変換メソッド <br>
        ASCII文字のみ有効とする
    */
    protected Hashtable getContextParameters(HttpServletRequest request,int count) throws IOException {
        return getContextParameters(request, (Hashtable<String, String[]>)null, count);
    }
    /** コンテキストパス変換メソッド <br>
        ASCII文字のみ有効とする
        @param request      
        @param hashParameter 存在すればこれに追加する
        @param count         リクエストカウント（デバッグ表示用）
        
    */
    protected Hashtable getContextParameters(HttpServletRequest request,
                                             Hashtable<String, String[]> hashParameter,
                                             int count) throws IOException {
        Hashtable<String,String[]> data = (hashParameter != null) ? hashParameter : 
            new Hashtable<String, String[]>();
        boolean display = debug && count >= 0;
        debugPath &= debug;

        if (display) {
            System.out.println(((count>0)? count+"|":"")+
                "▼ ContextParameter get====================================");
        }

        String contextPath = request.getContextPath();                      // /sc
if (debugPath2) System.out.println(count+"|ContextPath:"+contextPath);
        String pathInfo = request.getPathInfo();                            // /param
if (debugPath2) System.out.println(count+"|PathInfo:"+pathInfo);
        String requestURI = request.getRequestURI();
        String requestURL = request.getRequestURL().toString(); // for debug
        String queryString = request.getQueryString(); // for debug

        if (debugPath) System.out.println(count+"|RequestURL:"+requestURL);
if (debugPath2) System.out.println(count+"|RequestURI:"+requestURI);
if (debugPath2) System.out.println(count+"|ServletPath:"+request.getServletPath());
        if (debugPath && queryString != null) System.out.println(count+"|QueryString:"+((queryString != null)?queryString:""));


if (debugPath2) System.out.println("ThreadID:"+Thread.currentThread().getName());


        //if (pathInfo != null && pathInfo.length() > 0) {
        if (requestURI != null && requestURI.length() > 0) {
            // サイトマッピング
            CharArray ch = CharArray.pop();
            //@@//ch.set(contextPath).add(pathInfo);
            ch.set(requestURI);
            
            ModuleManager mm = null;
            HashVector<CharArray,CharArrayQueue> hashSite = SystemManager.ini.getKeyTable("[SiteMapping]"); 
            if (hashSite != null) {
                for (int i = 0; i < hashSite.size(); i++) {
                    CharArray _key = (CharArray)hashSite.keyElementAt(i);
                    if (_key != null && _key.length() >=contextPath.length()) {
                        if (ch.startsWith(_key)) {
                            CharArrayQueue queue = (CharArrayQueue)hashSite.valueElementAt(i);
                            if (queue != null && queue.size() > 0) {
                                int site_id = CharArray.getInt(queue.peek());
                                if (site_id > 0) {
                                    mm = SiteManager.get(site_id);
                                    if (mm != null) {
                                        if (display) System.out.println(count+"[site]"+site_id);
                                        data.put(SystemConst.siteKey,new String[] {""+site_id});
                                        break;
                                    } else {
                                        System.out.println(count+"[site]"+site_id+" not found!!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            CharArray.push(ch);
        
            // ディレクトリマッピング
            HashVector<CharArray,CharArrayQueue> hashDir  = SystemManager.ini.getKeyTable("[DirectoryMapping]");
            HashVector<CharArray,CharArrayQueue> hashPage = SystemManager.ini.getKeyTable("[PageMapping]");
            
            CharToken token = CharToken.pop();
            CharToken token1 = CharToken.pop();
            CharToken token2 = CharToken.pop();
            CharToken token3 = CharToken.pop();
            
            token.set(pathInfo,"/");
            // 一時処理
            //CharArray _path = CharArray.pop(requestURI);
            //String _sz = _path.URLDecode("UTF-8");  // これが固定になっているのが問題
            //token.set(_sz,"/");
            
            CharArray context = new CharArray();
            for (int j = 0; j < token.size(); j++) {    // スラッシュ分割
                CharArray dir_path = token.get(j);
                if (j > 1) context.add("/");
                if (j > 0) context.add(dir_path);
//System.out.println("---GetContext["+pathInfo+"]["+j+"]"+dir_path+" context:"+context);
                int idx = dir_path.indexOf('=');
                if (idx >= 0) { // パラメータディレクトリ
                    idx = dir_path.indexOf('=', idx+1);
                    if (idx < 0) {
                        CharArray dir = dir_path;
                        token2.set(dir,"=");
                        if (token2.size() == 2) {
                            // パラメータ分割
                            CharArray key  = token2.get(0);
                            CharArray param = token2.get(1);
                            if (display) System.out.println(count+"["+key+"]"+param);
                            data.put(key.toString(),new String[] {param.toString()});
                        } else {
                            int page_id = -1;
                            if (mm != null) page_id = mm.getSiteMapping().getPageID(dir);
                            CharArrayQueue queue = (CharArrayQueue)hashPage.get(dir);
                            if (page_id > 0) {
                                //ページマッピング
                                if (debugPath) System.out.println(count+"(PageMapping)/"+dir+" -> "+page_id);
                                String szPage = ""+page_id;
                                if (display) System.out.println(count+"["+SystemConst.pageIDKey+"]"+szPage);
                                data.put(SystemConst.pageIDKey,new String[] { szPage });
                            } else if (queue != null && queue.size() > 0) {
                                //ページマッピング
                                if (debugPath) System.out.println(count+"(PageMapping)/"+dir+" -> "+queue.peek());
                                String szPage = queue.peek().toString();
                                if (display) System.out.println(count+"["+SystemConst.pageIDKey+"]"+szPage);
                            
                                data.put(SystemConst.pageIDKey,new String[] { szPage });
                            } else {
                                CharArray chDir = null;
                                if (mm != null) chDir = mm.getSiteMapping().getDirectoryMap(dir);
                                queue = (CharArrayQueue)hashDir.get(dir);
                                if (chDir != null || (queue != null && queue.size() > 0)) {
                                    //ディレクトリマッピング
                                    if (chDir == null) chDir = queue.peek();
                                    if (debugPath) System.out.println(count+"(DirectoryMapping)/"+dir+" -> "+chDir);
                                    
                                    token3.set(chDir,"=");
                                    if (token3.size()==2) {
                                        CharArray key   = token3.get(0).trim();
                                        CharArray param = token3.get(1).trim();
                                        if (display) System.out.println(count+"["+key+"]"+param);
                                        if (key.length() > 0) {
                                            data.put(key.toString(),new String[] {param.toString()});
                                        }
                                    } else {
                                    }
                                }
                            }
                        }
                        
                        continue;
                    }
                }
                
                token1.set(token.get(j),"&");
                for (int i = 0; i < token1.size(); i++) {    // ＆分割
                   //System.out.println("["+i+"/"+token.size()+"]"+token.get(i));
                    CharArray dir = token1.get(i);
                    token2.set(dir,"=");
                    if (token2.size() == 2) {
                        // パラメータ分割
                        CharArray key  = token2.get(0);
                        CharArray param = token2.get(1);
                        if (display) System.out.println(count+"["+key+"]"+param);
                        data.put(key.toString(),new String[] {param.toString()});
                    } else {
                        int page_id = -1;
                        if (mm != null) page_id = mm.getSiteMapping().getPageID(dir);
                        CharArrayQueue queue = (CharArrayQueue)hashPage.get(dir);
                        if (page_id > 0) {
                            //ページマッピング
                            if (debugPath) System.out.println(count+"(PageMapping)/"+dir+" -> "+page_id);
                            String szPage = ""+page_id;
                            if (display) System.out.println(count+"["+SystemConst.pageIDKey+"]"+szPage);
                            data.put(SystemConst.pageIDKey,new String[] { szPage });
                        } else if (queue != null && queue.size() > 0) {
                            //ページマッピング
                            if (debugPath) System.out.println(count+"(PageMapping)/"+dir+" -> "+queue.peek());
                            String szPage = queue.peek().toString();
                            if (display) System.out.println(count+"["+SystemConst.pageIDKey+"]"+szPage);
                            
                            data.put(SystemConst.pageIDKey,new String[] { szPage });
                        } else {
                            CharArray chDir = null;
                            if (mm != null) chDir = mm.getSiteMapping().getDirectoryMap(dir);
                            queue = (CharArrayQueue)hashDir.get(dir);
                            if (chDir != null || (queue != null && queue.size() > 0)) {
                                //ディレクトリマッピング
                                if (chDir == null) chDir = queue.peek();
                                if (debugPath) System.out.println(count+"(DirectoryMapping)/"+dir+" -> "+chDir);
                                token3.set(chDir,"=");
                                if (token3.size()==2) {
                                    CharArray key   = token3.get(0).trim();
                                    CharArray param = token3.get(1).trim();
                                    if (display) System.out.println(count+"["+key+"]"+param);
                                    if (key.length() > 0) {
                                        data.put(key.toString(),new String[] {param.toString()});
                                    }
                                } else {
                                    //String key = dir_path.substring(0,idx1)
                                }
                            }
                        }
                    }
                } // next i
                
                if (j > 1 && context.length() > 0) {    // 拡張マッピング対応
                    int page_id = -1;
                    if (mm != null) page_id = mm.getSiteMapping().getPageID(context);
//System.out.println("  --checkContext["+context+"]->"+page_id);
                    CharArrayQueue queue = (CharArrayQueue)hashPage.get(context);
                    if (page_id > 0) {
                        //ページマッピング
                        if (debugPath) System.out.println(count+"(*PageMapping)/"+context+" -> "+page_id);
                        String szPage = ""+page_id;
                        if (display) System.out.println(count+"["+SystemConst.pageIDKey+"]"+szPage);
                        data.put(SystemConst.pageIDKey,new String[] { szPage });
                    } else if (queue != null && queue.size() > 0) {
                        //ページマッピング
                        if (debugPath) System.out.println(count+"(*PageMapping)/"+context+" -> "+queue.peek());
                        String szPage = queue.peek().toString();
                        if (display) System.out.println(count+"["+SystemConst.pageIDKey+"]"+szPage);
                        data.put(SystemConst.pageIDKey,new String[] { szPage });
                    } else {
                        CharArray chDir = null;
                        if (mm != null) chDir = mm.getSiteMapping().getDirectoryMap(context);
                        queue = (CharArrayQueue)hashDir.get(context);
                        if (chDir != null || (queue != null && queue.size() > 0)) {
                            //ディレクトリマッピング
                            if (chDir == null) chDir = queue.peek();
                            if (debugPath) System.out.println(count+"(*DirectoryMapping)/"+context+" -> "+chDir);
                            token3.set(chDir,"=");
                            if (token3.size()==2) {
                                CharArray key   = token3.get(0).trim();
                                CharArray param = token3.get(1).trim();
                                if (display) System.out.println(count+"["+key+"]"+param);
                                if (key.length() > 0) {
                                    data.put(key.toString(),new String[] {param.toString()});
                                }
                            } else {
                                 //String key = dir_path.substring(0,idx1)
                            }
                        }
                    }
                }
            }
            CharToken.push(token3);
            CharToken.push(token2);
            CharToken.push(token1);
            CharToken.push(token);
        } // next j
        
        if (display) {
            System.out.println(((count>0)? count+"|":"")+
                "▲ ContextParameter end====================================");
        }
        return data;
    }

    /** ContentType 設定 */
    public void setContentType(String str) {
        contentType0 = str;
    }
    /** ContentType 取得 */
    public String getContentType() { return contentType0; }

    // multipart/formdata 
    private int readLine(ServletInputStream in, 
               byte b[], int offset, int length) throws IOException {
        if (length <= 0) return 0;
        int count = -1, c, prev = -1;
        try {
            while((c = in.read()) != -1) {
                if (count == -1) count = 0;
                b[offset++] = (byte)c;
                count++;
                if (prev == 0x0D && c == 0x0A) {
                    count -= 2;
                    break;
                }
                if (length == count) break;
                prev = c;
            }
        } catch(Exception e) {
            throw new IOException(e.getMessage()) ;
        }
        return count;
    }

    private int readNext(ServletInputStream in, 
               byte b[], int offset, int length) throws IOException {
        if (length <= 0) return 0;
        int count = -1, c, prev = -1;
        try {
            while((c = in.read()) != -1) {
                if (count == -1) count = 0;
                b[offset++] = (byte)c;
                count++;
                if (prev == 0x0D && c == 0x0A) {
                    break;
                }
                if (length == count) break;
                prev = c;
            }
        } catch(Exception e) {
            throw new IOException(e.getMessage()) ;
        }
        return count;
    }
    
    private CharArray parseKeyAndValue(CharArray org) {
        return parseKeyAndValue(org, new CharArray(),"="); // key 名は不定でも良しとする
    }
    private CharArray parseKeyAndValue(CharArray org, String sep) {
        return parseKeyAndValue(org, new CharArray(),sep); // key 名は不定でも良しとする
    }
    private CharArray parseKeyAndValue(CharArray org, CharArray key) {
        return parseKeyAndValue(org, key, "="); // key 名は不定でも良しとする
    }
    private CharArray parseKeyAndValue(CharArray org, CharArray key, String sep) {
        CharArray ch = new CharArray();
        if (org != null) {
            CharToken token = CharToken.pop();
            token.set(org,sep);
            if (token.size()==2) {
                key.set(token.get(0).trim());
                ch.set(token.get(1).trim());
                ch.replace("\"","");    // コーテーション外す
            }
            CharToken.push(token);
        }
        return ch;
    }
    
    public void println(SessionObject session, int count, String str) {
        if (session != null) {
            session.println(str);
        } else {
            System.out.println(((count>0)? count+"|":"")+str);
        }
    }
    
}

//
// [end of AbstractServlet.java]
//

