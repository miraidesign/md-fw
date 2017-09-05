//------------------------------------------------------------------------
//@(#)SiteManager.java
//        ModuleManagerをSite+channelごとに管理します
//
//        Copyright (c) MiraiDeign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
package com.miraidesign.system;

import java.util.Enumeration;
import java.util.Hashtable;

import com.miraidesign.common.SystemConst;
import com.miraidesign.mail.SendMail;
import com.miraidesign.renderer.Module;
import com.miraidesign.renderer.Page;
import com.miraidesign.servlet.Loader;
import com.miraidesign.servlet.ModuleServlet;
import com.miraidesign.servlet.PageServlet;
import com.miraidesign.session.SessionObject;
import com.miraidesign.session.UserAgent;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.ExceptionWriter;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.HashVector;
import com.miraidesign.util.IniFile;
import com.miraidesign.util.IntObject;
import com.miraidesign.util.IntQueue;
import com.miraidesign.util.ParameterConverter;
import com.miraidesign.util.QueueFile;

/**
 *  SiteごとのModuleManagerの管理を行います。
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public abstract class SiteManager {
    static private boolean systemdebug = true;
    static private boolean debugReading = (SystemConst.debug && true);  // デバッグ表示
    static private boolean debug = (SystemConst.debug && true);  // デバッグ表示
    /** sitecode から sitename を取得する */
    static private Hashtable<IntObject,ModuleManager> hashCode  = 
               new Hashtable<IntObject,ModuleManager>();
    /** sitename から ModuleManager を取得する */
    static private HashVector<CharArray,ModuleManager> hashName = 
               new HashVector<CharArray,ModuleManager>();
    
    static Hashtable<IntObject,ModuleManager> getModuleHashTable() { return hashCode;}
    static HashVector<CharArray,ModuleManager> getHashModuleManager() { return hashName;}
    //-------------------------------------------------------------------------
    // consuructor なし
    //-------------------------------------------------------------------------
    //public SiteManager() {  }

    // public void init();
    
    /*
        サイト定義ファイルをロードする
        エンコーディングはMS932->UTF-8
    */
    static public void load(IniFile ini) {
        String encoding = ini.getEncoding();
        if (encoding == null || encoding.length() == 0) encoding = "UTF-8"; // MS932
        load(ini, encoding);
    }
    /** 
        サイト定義ファイルをロードする
        @param ini
        @param encoding ファイルのエンコーディング 
    */
    static public void load(IniFile ini, String encoding) {
        if (ini.isOK()) {
            CharArray filenames = ini.get("[Resource]","SiteData");
            filenames.convertProperty();
            CharToken token = CharToken.pop(filenames);
            for (int t = 0; t < token.size(); t++) {
                CharArray ch = token.get(t).trim();
                if (ch.length() > 0) {
                    IniFile siteIni = new IniFile(SystemManager.resourceDirectory+ch,"=","#","\\","\\n");
                    siteIni.setInclude("#include");
                    
                    if (encoding == null || encoding.length() == 0) encoding = ini.getEncoding();
                    if (encoding == null || encoding.length() == 0) encoding = "UTF-8"; //"MS932";
                    
                    siteIni.setEncoding(encoding);
                    
                    ParameterConverter converter = new ParameterConverter();
                    converter.setWidth(2);
                    siteIni.setConverter(converter);
                    
                    siteIni.read();
                    if (debugReading) System.out.println(""+ch+"  reading.....");
                    if (siteIni.isOK()) {
                        set(siteIni);
                    } else {
                        System.out.println("SQLData:"+SystemManager.resourceDirectory+ch+"が見つかりません");
                        ++SystemManager.init_error;
                        SystemManager.init_error_queue.enqueue("SQLData:"+SystemManager.resourceDirectory+ch+"が見つかりません");
                    }
                }
            }
            CharToken.push(token);
        } else {
            ++SystemManager.init_error;
            SystemManager.init_error_queue.enqueue("ini.NG !");
        }
    }
    
    /** サイトの設定を行う */
    static public void set(IniFile ini) {
        CharArray chSite = ini.get("[Site]","UseSite");
        if (chSite != null) {
            chSite.convertProperty();
            CharToken token = CharToken.pop(chSite);
            for (int t = 0; t < token.size(); t++) {
                CharArray ch = token.get(t).trim();
                if (ch.length() > 0) {
                    setSite(ini, ch);
                }
            }
            CharToken.push(token);
        }
    }
    
    static public void setSite(IniFile ini, CharArray chSite) {
        String site = "["+chSite+"]";
        int    siteCode = ini.getInt(site,"Code");
        String siteName = ini.getString(site,"Name");

        CharArray smtpHost    = ini.get(site,"SmtpHost");
        CharArray fromAddress = ini.get(site,"FromAddress");
        CharArray replyTo     = ini.get(site,"ReplyTo");
        CharArray errorsTo     = ini.get(site,"ErrorsTo");
        
        CharArray toAddress = ini.get(site,"ToAddress");
        CharArray ccAddress = ini.get(site,"CcAddress");
        CharArray bccAddress = ini.get(site,"BccAddress");
        
        CharArray channels = ini.get(site,"Channels");
        CharToken token = CharToken.pop(channels);
        IntObject key =  IntObject.pop();
        for (int t = 0; t < token.size(); t++) {
            CharArray ch = token.get(t).trim();
            if (ch.length() > 0) {
                int channelCode = ch.getInt();      // チャネル定義
                String section = "["+chSite+"."+ch+"]";
                CharArray subCode      = ini.get(section, "SubCode");
                CharArray name   = ini.get(section, "Name");
                // 
                CharArray contentType = ini.get(section, "ContentType");
                CharArray charSet     = ini.get(section, "CharSet");
                CharArray charCode    = ini.get(section, "CharCode");
                
                CharArray header = ini.get(section, "Header");
                CharArray footer = ini.get(section, "Footer");
                CharArray smtpHost2    = ini.get(section,"SmtpHost");
                CharArray fromAddress2 = ini.get(section,"FromAddress");
                CharArray replyTo2     = ini.get(section,"ReplyTo");
                CharArray errorsTo2     = ini.get(section,"ErrorsTo");
                
                CharArray toAddress2 = ini.get(section,"ToAddress");
                CharArray ccAddress2 = ini.get(section,"CcAddress");
                CharArray bccAddress2 = ini.get(section,"BccAddress");

                CharArray searchMax    = ini.get(section, "SearchMax");
                CharArray searchNext   = ini.get(section, "SearchNext");
                CharArray pageDisplay  = ini.get(section, "PageDisplay");
                CharArray pageMax      = ini.get(section, "PageMax");
                CharArray pageWidth  = ini.get(section, "PageWidth");

                CharArray searchMaxMobile    = ini.get(section, "SearchMax.Mobile");
                CharArray searchNextMobile   = ini.get(section, "SearchNext.Mobile");
                CharArray pageDisplayMobile   = ini.get(section, "PageDisplay.Mobile");
                CharArray pageMaxMobile       = ini.get(section, "PageMax.Mobile");
                CharArray pageWidthMobile  = ini.get(section, "PageWidth.Mobile");
                
                CharArray topPage      = ini.get(section,"TopPageURL");
                CharArray adminBase    = ini.get(section,"AdminBase");
                CharArray userBase     = ini.get(section,"UserBase");

                CharArray checkFilename   = ini.get(section,"CheckFilename");
                CharArray checkSitecode   = ini.get(section,"CheckSitecode");
                CharArray checkVersion    = ini.get(section,"CheckVersion");
                CharArray checkTimeout    = ini.get(section,"CheckTimeout");
                CharArray checkUserAgent  = ini.get(section,"CheckUserAgent");
                
                CharArray imageProxy   = ini.get(section,"ImageProxy");
                CharArray imageWidth   = ini.get(section,"ImageWidth");
                CharArray imageRatio   = ini.get(section,"ImageRatio");
                CharArray imageBitMax  = ini.get(section,"ImageBitMax");

                CharArray navi          = ini.get(section,"NaviSeparator");
                CharArray defaultConnection = ini.get(section,"DefaultConnection");
                CharArray sqlPrefix          = ini.get(section,"SQLPrefix"); 
                CharArray appendAuthID       = ini.get(section,"AppendAuthID");

                if (debug) System.out.println(section+"DefaultConnection["+defaultConnection+"]");
                if (debug) System.out.println(section+"SQLPrefix["+sqlPrefix+"]");

                
                CharArray hostURL = ini.get(section,"HostURL");
                CharArray httpURL = ini.get(section,"HttpURL");
                CharArray httpsURL = ini.get(section,"HttpsURL");
                CharArray registURL = ini.get(section,"RegistURL");
                
                CharArray serverKey = ini.get(section,"ServerKey");
                CharArray serverKeyFlg = ini.get(section,"ServerKeyFlg");
                CharArray serverName = ini.get(section,"ServerName");
                
                CharArray sslRedirect = ini.get(section,"SSLRedirect");
                CharArray sslCheckPort = ini.get(section,"SSLCheckPort");
                
                if (header != null) {
                    if (header.indexOf("\\n") >= 0) {
                        CharToken tk = CharToken.pop();
                        tk.set(header,"\\n");
                        header.reset();
                        for (int n = 0; n < tk.size(); n++) {
                            header.add(tk.get(n));
                            if (n+1 < tk.size()) header.add("\n");
                        }
                        CharToken.push(tk);
                    }
                }
                if (footer != null) {
                    if (footer.indexOf("\\n") >= 0) {
                        CharToken tk = CharToken.pop();
                        tk.set(footer,"\\n");
                        footer.reset();
                        for (int n = 0; n < tk.size(); n++) {
                            footer.add(tk.get(n));
                            if (n+1 < tk.size()) footer.add("\n");
                        }
                        CharToken.push(tk);
                    }
                }
                key.setValue(siteCode*100+channelCode);
                if (hashCode.containsKey(key)) {
                    String err_msg = "Sitemanager error: "+siteName+"/"+name+
                        "("+siteCode+"/"+channelCode+
                        ")は同一コード("+key+")が存在するため追加できません!!";
                    System.out.println(err_msg);
                    ++SystemManager.init_error;
                    SystemManager.init_error_queue.enqueue(err_msg);
                } else {
                    if (debug) {
                        System.out.println("Sitemanager :"+siteName+"("+chSite+")"+
                        "/"+name+
                        "("+siteCode+"/"+channelCode+
                        ")"+"を登録します ◆");
                    }
                    ModuleManager mm = new ModuleManager();
                    mm.ini = ini;
                    if (subCode != null) mm.subCode = subCode.getInt();
                    mm.setSiteName(siteName);
                    mm.setSiteCode(siteCode);
                    mm.setSiteKey(chSite.toString());
                    mm.setChannelName(name.toString());
                    mm.setChannelCode(channelCode);

                    if (contentType != null && contentType.trim().length() > 0) {
                        mm.contentType = contentType.toString();
                    }
                    if (charSet != null && charSet.trim().length() > 0) {
                        mm.charSet = charSet.toString();
                    }
                    if (charCode != null && charCode.trim().length() > 0) {
                        mm.charCode = charCode.toString();
                    }
                    
                    if (header != null) mm.setHeader(header.toString());
                    if (footer != null) mm.setFooter(footer.toString());
                    
                    if (searchMax  != null) {
                        mm.setSearchMax(searchMax.getInt());
                        mm.setSearchMaxMobile((searchMaxMobile != null)? 
                                  searchMaxMobile.getInt(): searchMax.getInt());
                    }
                    if (searchNext != null) {
                        mm.setSearchNext(searchNext.getBoolean());
                        mm.setSearchNextMobile((searchNextMobile != null) ?
                                  searchNextMobile.getBoolean(): searchNext.getBoolean());
                    }
                    if (pageDisplay != null) {
                        mm.setPageDisplay(pageDisplay.getBoolean());
                        mm.setPageDisplayMobile((pageDisplayMobile != null) ?
                            pageDisplayMobile.getBoolean():pageDisplay.getBoolean());
                    }
                    if (pageMax != null && pageMax.trim().length()>0) {
                        mm.setPageMax(pageMax.getInt());
                        mm.setPageMaxMobile(
                            (pageMaxMobile != null && pageMaxMobile.trim().length()>0) ?
                            pageMaxMobile.getInt():pageMax.getInt());
                    }
                    if (pageWidth != null && pageWidth.trim().length()>0) {
                        mm.setPageWidth(pageWidth.getInt());
                        mm.setPageWidthMobile(
                        (pageWidthMobile != null && pageWidthMobile.trim().length()>0)?
                         pageWidthMobile.getInt() :pageWidth.getInt());
                    }
                    
                    if (topPage != null) mm.setTopPage(topPage.trim());
                    if (adminBase != null) mm.setAdminBase(adminBase.trim());
                    if (userBase != null) mm.setUserBase(userBase.trim());
                    
                    if (checkFilename != null) mm.checkFilename = checkFilename.getBoolean();
                    if (checkSitecode != null) mm.checkSitecode = checkSitecode.getBoolean();
                    if (checkVersion  != null) mm.checkVersion  = checkVersion.getBoolean();
                    if (checkTimeout  != null) mm.checkTimeout  = checkTimeout.getBoolean();
                    if (checkUserAgent != null) mm.checkUserAgent = checkUserAgent.getBoolean();
                    
                    if (imageProxy != null) mm.imageProxy = imageProxy.getBoolean();
                    if (imageWidth != null) mm.imageWidth = imageWidth.getInt();
                    if (imageRatio != null) mm.imageRatio = imageRatio.getInt();
                    if (imageBitMax != null) mm.imageBitMax = imageBitMax.getInt();

                    if (navi != null) mm.naviSeparator.set(navi);
                    if (defaultConnection != null) mm.defaultConnection= defaultConnection.trim().toString();
                    if (sqlPrefix != null) mm.szSQLPrefix= sqlPrefix.trim().toString();
                    
                    if (appendAuthID != null) {
                        mm.appendAuthID = appendAuthID.getBoolean();
                    } else {
                        mm.appendAuthID = SystemManager.appendAuthID;
                    }
                    
                    if (hostURL != null && hostURL.trim().length() > 0) mm.hostURL = hostURL.toString();
                    if (httpURL != null && httpURL.trim().length() > 0) mm.httpURL = httpURL.toString();
                    if (httpsURL != null && httpsURL.trim().length() > 0) mm.httpsURL = httpsURL.toString();
                    if (registURL != null && registURL.trim().length() > 0) mm.registURL = registURL.toString();
                    
                    if (serverKey != null && serverKey.trim().length() > 0) {
                        mm.serverKey = serverKey.toString();
                        if (SystemManager.serverKey.length() > 0) SystemManager.serverKey += ", ";
                        SystemManager.serverKey += mm.serverKey;
                    }
                    if (serverKeyFlg != null && serverKeyFlg.trim().length() > 0 && serverKeyFlg.isDigit()) mm.serverKeyFlg = serverKeyFlg.getInt();
                    if (serverName != null && serverName.trim().length() > 0) mm.serverName = serverName.toString();
                    if (sslRedirect != null && sslRedirect.trim().length() > 0) mm.SSLRedirect = sslRedirect.getBoolean();
                    if (sslCheckPort != null && sslCheckPort.trim().length() > 0) mm.SSLCheckPort = sslCheckPort.getInt();
                    
                    //----------------------------------------------------------------------------
                    hashCode.put(new IntObject(key),mm);
                    hashName.put(new CharArray(name),mm);
                    String szChannel = ch.toString();
                    // Theme読み込み ---------------------------------------------------------------
                    CharArray chSection = CharArray.pop("["+chSite+"."+szChannel+".Theme]");
                    if (ini.containsSection(chSection)) {
                        HashVector vec = ini.getKeyTable(chSection);
                        for (int i = 0; i < vec.size(); i++) {
                            CharArray ck = ((CharArray)vec.keyElementAt(i)).trim();
                            CharArray cd = ((CharArrayQueue)vec.valueElementAt(i)).peek().trim();
                            if (ck.equals("default")) {
                                mm.setTheme(new CharArray(cd));
                            } else {
                                CharToken _token = CharToken.pop();
                                _token.set(cd, ",");
                                CharArrayQueue _queue = new CharArrayQueue();
                                _queue.enqueue(new CharArray(_token.get(0)));
                                _queue.enqueue(new CharArray(_token.get(1)));
                                mm.getHashTheme().put(new CharArray(ck), _queue);
                                CharToken.push(_token);
                            }
                        }
                    }
                    CharArray theme  = ini.get(section,"Theme"); // 存在すればサイトテーマとして優先
                    if (theme != null && theme.trim().length() > 0) {
                        HashVector<CharArray,CharArrayQueue> hashTheme = mm.getHashTheme();
                        if (hashTheme != null && hashTheme.get(theme) != null) {
                            mm.setTheme(theme);
                        }
                    }
                    // LANG読み込み ---------------------------------------------------------------
                    chSection.set("["+chSite+"."+szChannel+".Lang]");
                    if (ini.containsSection(chSection)) {
                        HashVector vec = ini.getKeyTable(chSection);
                        for (int i = 0; i < vec.size(); i++) {
                            CharArray ck = ((CharArray)vec.keyElementAt(i)).trim();
                            CharArray cd = ((CharArrayQueue)vec.valueElementAt(i)).peek().trim();
                            if (ck.equals("default")) {
                                mm.setLang(new CharArray(cd));
                            } else {
                                CharToken _token = CharToken.pop();
                                _token.set(cd, ",");
                                CharArrayQueue _queue = new CharArrayQueue();
                                _queue.enqueue(new CharArray(_token.get(0)));
                                _queue.enqueue(new CharArray(_token.get(1)));
                                mm.getHashLang().put(new CharArray(ck), _queue);
                                CharToken.push(_token);
                            }
                        }
                    }
                    CharArray lang  = ini.get(section,"Lang");   // 存在すればサイトLANGとして優先
                    if (lang != null && lang.trim().length() > 0) {
                        HashVector<CharArray,CharArrayQueue> hashLang = mm.getHashLang();
                        if (hashLang != null && hashLang.get(lang) != null) {
                            mm.setLang(lang);
                        }
                    }
                    // InputReference 登録  -------------------------------------------
                    chSection.set("["+chSite+"."+szChannel+".InputRef]");
                    if (ini.containsSection(chSection)) {
                        HashVector<CharArray,CharArrayQueue> vec = ini.getKeyTable(chSection);
                        if (vec != null && vec.size() > 0) {
                            mm.refHash = new HashVector<CharArray,HashParameter>();
                            for (int i = 0; i < vec.size(); i++) {
                                CharArray ck = ((CharArray)vec.keyElementAt(i)).trim();
                                CharArray cd = ((CharArrayQueue)vec.valueElementAt(i)).peek().trim();
                                
                                HashParameter param = new HashParameter();
                                param.set(cd, ":", ";");
                                
                                mm.refHash.put(ck, param);
                            }
                        }
                    }
                    // Macro 登録  -------------------------------------------
                    chSection.set("["+chSite+"."+szChannel+".Macro]");
                    if (ini.containsSection(chSection)) {
                        HashVector<CharArray,CharArrayQueue> vec = ini.getKeyTable(chSection);
                        if (vec != null && vec.size() > 0) {
                            mm.getContentMacro(vec);
                        }
                    }
                    // ローダーの登録--------------------------------------------------------------
System.out.println("SiteManager.setSite() InitLoader 登録");
                    chSection.set("["+chSite+"."+szChannel+".Init]");
                    mm.loader = new Loader();
                    mm.loader.setModuleManager(mm);
                    mm.loader.load(ini.getKeyTable(chSection));   // 初期化モジュールのロード
                    // モジュールの登録--------------------------------------------------------------
System.out.println("SiteManager.setSite() Module 登録");
                    chSection = CharArray.pop("["+chSite+"."+szChannel+".Module]");
                    mm.sectionBase = chSite+"."+szChannel;
                    if (ini.containsSection(chSection)) {
                        IntQueue iq = new IntQueue();
                        CharToken token2 = CharToken.pop();
                        for (Enumeration e = ini.getKeyList(chSection); e.hasMoreElements();) {
                            CharArray chName = (CharArray)e.nextElement();
                            try {
                                ch = ini.get(chSection, chName);
                                if (ch != null) {
                                    token2.set(ch);
                                    if (token2.size() >=2) {
                                        int id = token2.getInt(0);
                                        if (iq.find(id)) {
                                            System.out.println("!! error !! Module ID "+id+" duprecated");
                                            ++SystemManager.init_error;
                                        }
                                        iq .enqueue(id);
                                        String className = token2.get(1).trim().toString();
                                        if (debug) System.out.println(className+"("+id+") Loading...");
                                        if (className.length() > 0) {
                                            ModuleServlet m = (ModuleServlet)Class.forName(className).newInstance();
                                            m.set(id, chName.toString());
                                            mm.add(m);
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                              ++SystemManager.init_error;
                              System.out.println("Load Error :"+chName);
                              SystemManager.init_error_queue.enqueue("Load Error :"+chName);
                              ex.printStackTrace();
                            }
                        }
                        CharToken.push(token2);
                    }
                    // ローダーのロード処理 
                    mm.loader.load();  
                    
                    // テンプレート読み込み ---------------------------
                    chSection.set("["+chSite+"."+szChannel+".ReplaceKeyword]");
                    mm.hashReplaceKeyword = ini.getKeyTable(chSection);     // テンプレートリプレースキーワードを取得
                    
                    chSection.set("["+chSite+"."+szChannel+".Template]");
                    mm.hashTemplate = ini.getKeyTable(chSection);     // 共有テンプレートを取得
                    
                    if (debug) System.out.println("Loading html..");
                    chSection.set("["+chSite+"."+szChannel+".Html]");
                    if (ini.containsSection(chSection)) {
                        CharArray showNavigation = ini.get(chSection,"ShowNavigation");

                        if (showNavigation != null && showNavigation.trim().length()>0) {
                            mm.showNavigation = showNavigation.getBoolean();
                        }
                        String titleSection= "["+chSite+"."+szChannel+".Title]";
                        CharToken token2 = CharToken.pop();
                        CharToken token3 = CharToken.pop();
                        token2.setDelimiter(".");
                        token3.setDelimiter(",");
                        
                        
                        HashParameter hashKey = mm.getTitleKeyList();
                        hashKey.clear();
                        
                        //
                        //  [*.*.Html]の読み込み
                        //
                        for (Enumeration e = ini.getKeyList(chSection); e.hasMoreElements();) {
                            CharArray chName = (CharArray)e.nextElement();  // モジュール名+PageID+PageName
                            ch = ini.get(chSection, chName);    // データを取得
                            if (ch != null) {
                                token2.set(chName);
                                if (token2.size() >=2) {
                                    CharArray moduleKey = token2.get(0);         // モジュール名
                                    int id = token2.getInt(1);  // PageID
                                    CharArray pageKey = new CharArray();         // ページキー
                                    if (token2.size() >=3) pageKey.set(token2.get(2).trim());
                                    CharArray mimeType = new CharArray();   // for Ajax 2014-03-17
                                    if (token2.size() >=4) mimeType.set(token2.get(3).trim());
                                    
                                    token3.set(ch);
                                    
                                    CharArray fileName = token3.get(0);
                                    CharArray title    = token3.get(1);
                                    CharArray titleImage = token3.get(2);
                                    CharArray param    = token3.get(3);
                                    CharArray guide    = token3.get(4);
                                    
                                    if (fileName != null && fileName.length() > 0) {
                                        boolean sts = false;
                                        if (debug) System.out.print(moduleKey+"("+id
                                                +((pageKey.length()> 0)? "."+pageKey : "")
                                                +((mimeType.length()> 0)? "."+mimeType : "")
                                                +") "+fileName+" , "+title+" Loading...");
                                        Module m = mm.getModule(moduleKey);
                                        if (m != null) {
                                            Page p = m.get(id);
                                            if (p != null) {
                                                p.setName(pageKey);
                                                ((PageServlet)p).setMimeType(mimeType); // 2014-03-17
                                                sts = ((PageServlet)p).setParser(SystemManager.resourceDirectory+fileName);
                                                if (param != null && param.trim().length() > 0) {
                                                    ((PageServlet)p).setNodeParameter(param);
                                                }
                                            }
                                        }

                                        if (debug) System.out.println(sts ? "OK" : "ERROR!");
                                        if (!sts) {
                                            ++SystemManager.init_error;
                                            SystemManager.init_error_queue.enqueue(
                                                moduleKey+"("+id
                                                +((pageKey.length()> 0)? "."+pageKey : "")
                                                +((mimeType.length()> 0)? "."+mimeType : "")
                                                +") "+fileName+" , "+title+" Loading...ERROR!");
                                        }

                                    }
                                    // 画面リスト登録
                                    do {
                                        if (moduleKey == null || moduleKey.length() ==0) break;
                                        if (pageKey == null || pageKey.length() ==0) break;
                                        if (title == null || title.length() ==0) break;
                                        
                                        hashKey.add(moduleKey+":"+pageKey, ""+title);
                                    } while (false);
                                }
                            }
                        } // next
                        CharToken.push(token3);
                        CharToken.push(token2);
                    }
                    if (debug) System.out.println("Loading html end");
                    // メールテンプレート読み込み ---------------------------
                    chSection.set("["+chSite+"."+szChannel+".Mail]");
                    if (ini.containsSection(chSection)) {
                        for (Enumeration e = ini.getKeyList(chSection); e.hasMoreElements();) {
                            CharArray chKey= (CharArray)e.nextElement();  // キーワード
                            CharArray chFile= ini.get(chSection,chKey);
                            if (chFile.trim().length() > 0) {
                                String filename = SystemManager.resourceDirectory+chFile;
                                if (chFile.chars[0]=='/' || chFile.chars[0]=='\\' || chFile.indexOf(":") > 0) {
                                    filename = chFile.toString();
                                }
                            
                                QueueFile file = new QueueFile(filename,"UTF-8");   // MS932
                                if (file.read()) {
                                    if (debugReading) System.out.println(""+chSection+chKey+"="+filename+" Loading...");
                                    mm.addMailTemplate(chKey.toString(),file);
                                }
                            }
                        }
                    }
                    // ファイル読み込み -------------------------------------
                    chSection.set("["+chSite+"."+szChannel+".File]");
                    if (ini.containsSection(chSection)) {
                        for (Enumeration e = ini.getKeyList(chSection); e.hasMoreElements();) {
                            CharArray chKey= (CharArray)e.nextElement();  // キーワード
                            CharArray chFile= ini.get(chSection,chKey);
                            if (debug) System.out.println(chKey+"="+chFile+" Loading...");
                            if (chFile.trim().length() > 0) {
                                String filename = SystemManager.resourceDirectory+chFile;
                                if (chFile.chars[0]=='/' || chFile.chars[0]=='\\' || chFile.indexOf(":") > 0) {
                                    filename = chFile.toString();
                                }
                            
                                QueueFile file = new QueueFile(filename,"UTF-8"); // MS932
                                if (file.read()) {
                                    if (debugReading) System.out.println(""+chSection+chKey+"="+filename+" Loading...");
                                    mm.addFile(chKey.toString(),file);
                                }
                            }
                        }
                    }
                    // メニュー読み込み -------------------------------------
                    chSection.set("["+chSite+"."+szChannel+".Menu]");
                    
                    for (int i = 1; i <= 3; i++) {
                        ch = ini.get(chSection.toString(),""+i);    // ASP,COMP,SHOP
                        if (ch != null && ch.trim().length() > 0) {
                            mm.add(ini,"["+chSite+"."+szChannel+".Menu."+ch+"]",ch.toString());
                        }
                    }
                    
                    //-------------------------------------------------------
                    mm.pcCookieMaxHour = SystemManager.pcCookieMaxHour;
                    chSection.set("["+chSite+"."+szChannel+".Session]");
                    ch = ini.get(chSection,"PCCookieMaxHour");
                    if (ch != null && ch.trim().length() > 0) mm.pcCookieMaxHour = ch.getInt();
                    ch = ini.get(chSection,"ReadCookie");
                    if (ch != null && ch.trim().length() > 0) mm.readCookie = ch.getBoolean();
                    ch = ini.get(chSection,"WriteCookie");
                    if (ch != null && ch.trim().length() > 0) mm.writeCookie = ch.getBoolean();
                    //-------------------------------------------------------
                    chSection.set("["+chSite+"."+szChannel+".Barcode]");
                    ch = ini.get(chSection,"JAN8.MaxScanWidth");
                    if (ch != null && ch.trim().length()>0) {
                        mm.jan8MaxScanWidth = ch.getInt();
                    }
                    ch = ini.get(chSection,"QR.MaxScanWidth");
                    if (ch != null && ch.trim().length()>0) {
                        mm.qrMaxScanWidth = ch.getInt();
                    }
                    
                    //-------------------------------------------------------
                    // Validator 読み込み
                    //mm.initValidate();
                    //-------------------------------------------------------
                    // アイテム情報読み込み
                    chSection.set("["+chSite+"."+szChannel+".Item]");
                    mm.setHashItemInfo(ini.getKeyTable(chSection));
                    //-------------------------------------------------------
                    // デフォルトスタイルシート読み込み
                    chSection.set("["+chSite+"."+szChannel+".StyleSheetURL]");
                    mm.setStyleSheetURL(ini.getKeyTable(chSection), UserAgent.PC);
                    chSection.set("["+chSite+"."+szChannel+".StyleSheetURL.PC]");
                    mm.setStyleSheetURL(ini.getKeyTable(chSection), UserAgent.PC);
                    //chSection.set("["+chSite+"."+szChannel+".StyleSheetURL.Docomo]");
                    //mm.setStyleSheetURL(ini.getKeyTable(chSection), UserAgent.DOCOMO);
                    //chSection.set("["+chSite+"."+szChannel+".StyleSheetURL.SoftBank]");
                    //mm.setStyleSheetURL(ini.getKeyTable(chSection), UserAgent.SOFTBANK);
                    //chSection.set("["+chSite+"."+szChannel+".StyleSheetURL.Au]");
                    //mm.setStyleSheetURL(ini.getKeyTable(chSection), UserAgent.AU);
                    
                    // IP登録--------------------------------------------------------------
                    mm.setIP();
                    
                    mm.getSiteMapping();
                    
                    CharArray.push(chSection);
                }
            } // endif
        } // next 次のチャネルへ
        IntObject.push(key);
        CharToken.push(token);
    }
    

    /**
        ModuleManager を取得する
        @param siteChCode sitecode*100 + channelcode
    */
    static public ModuleManager get(int siteChCode) {
        // sitecode から取得する
        IntObject key       = IntObject.pop(siteChCode);
        ModuleManager moduleManager = (ModuleManager)hashCode.get(key);
        if (moduleManager == null) {
            System.out.println("SiteManager.get("+siteChCode+
            ") ModuleManager が取得できません(system error) key:"+
            ((key==null) ? "null" : ""+key.getValue()));
            if (systemdebug) {
                if (siteChCode > 0) {
                    System.out.println("hash.size="+hashCode.size());
                    for (Enumeration e = hashCode.keys(); e.hasMoreElements();) {
                        IntObject obj = (IntObject)e.nextElement();
                        Object value = hashCode.get(obj);
                        System.out.println("key:"+obj.getValue()+
                                       " value:"+(value != null));
                    }
                }
                try {
                    throw new Exception();
                } catch (Exception ex) {
                    ExceptionWriter exw = ExceptionWriter.pop();
                    ex.printStackTrace(exw);
                    System.out.println("ModuleManager.get(site["+siteChCode+"] not found):"+exw.peek());
                    exw.dumpQueue(0,4);
                    ExceptionWriter.push(exw);
                }
                
            }
        }
        IntObject.push(key);
        return moduleManager;
    }

    /**
        ModuleManager を取得する
        @param sitename チャネルサイト名
        @return ModuleManager
    */
    static public ModuleManager get(String sitename) {
        CharArray key = CharArray.pop(sitename);
        ModuleManager moduleManager = get(key);
        CharArray.push(key);
        return moduleManager;
    }
    /**
        ModuleManager を取得する
        @param sitename サイト名
        @return ModuleManager
    */
    static public ModuleManager get(CharArray sitename) {
        ModuleManager moduleManager = (ModuleManager)hashName.get(sitename);
        if (moduleManager == null) {
            System.out.println("SiteManager.get("+sitename+
            ") ModuleManager が取得できません(system error)");
        }
        return moduleManager;
    }
    
    static public Enumeration getModuleManagerList() {
        return hashName.elements();
    }

}

//
// [end of SiteManager.java]
//
