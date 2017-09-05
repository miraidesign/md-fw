//------------------------------------------------------------------------
// @(#)CheckServlet.java
//              環境チェック用
//              Copyright (c) Mirai Design 2010 All Rights Reserved.
//------------------------------------------------------------------------
// 使用方法：
//
package com.miraidesign.servlet;

import java.io.PrintWriter;
import java.util.Hashtable;

//import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.miraidesign.common.SystemConst;
import com.miraidesign.common.Const;

import com.miraidesign.session.SessionManager;
import com.miraidesign.session.UserAgent;
import com.miraidesign.system.SystemManager;
import com.miraidesign.system.GCManager;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayStack;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.IntObject;

/**
    サーブレットの入り口となります
*/

public class CheckServlet extends AbstractServlet {
    static private boolean debugACK   = true;   // デバッグ表示
    static private boolean debugGC    = false;  // デバッグ表示
    static private boolean debugCheck = false;   // デバッグ表示
    {
        setAllDebugLog(false);
        super.debugLog    = false;
        /*
        super.debug       = false;
        super.debugHeader = false;
        super.debugUA     = false;
        super.debugMulti  = false;
        super.debugPath   = false;
        super.debugPath2  = false;
        super.debugSystemLock = false;  // 2011-08-21
        super.debugPict = false;
        super.debugUTF = false;
        super.debugContext = false;
        super.debugParameter = false;
        */
    }
    /** AbstractServletをoverrideする */
    public void doGetJob(HttpServletRequest request, HttpServletResponse response, int count) {
        doPostJob(request, response, count);
    }
    public void doPostJob(HttpServletRequest request, HttpServletResponse response, int count) {
        //debug &= SystemConst.debug;
        try {
            //out = response.getWriter();
            //Hashtable hashParameter = getParameters(request, -1);
            Hashtable<String,String[]> hashParameter = getParameters(request, count);

            String command = getParameter("cmd",hashParameter);

            CharArray ch = CharArray.pop();
            //--------------------------------------------------------------------
if ((debug && !debugACK) || debugCheck) System.out.println(count+"|◎check:cmd:"+command);
            if (command.equals("ack")) {
                // 廃棄または作り直し (アプレット復活の際は必須)
            } else if (command.equals("kill")) {
                // 廃棄または作り直し
            } else if (command.equals("destroy")) {
                // 廃棄または作り直し
            } else if (command.equals("logout")) {
                // 廃棄または作り直し
            } else if (command.equals("who")) {
                // 作り直す
            } else if (command.equals("mem")) {
                checkMemory(ch);
            } else if (command.equals("free")) {
                checkFree(ch);
            } else if (command.equals("session")) {
                checkSession(ch);
            } else if (command.equals("stack")) {
                checkStack(ch);
            } else if (command.equals("version")) {
                checkServer(ch);
                checkVM(ch);
                checkVersion(ch);
                ch.add("System startup at : ");
                ch.add(SystemManager.szStartupTime);
            } else if (command.equals("gc")) {
                GCManager.execute(true);
            } else if (command.equals("update")) {
                int sessionID = CharArray.getInt(getParameter("session",hashParameter));
                SessionManager.update(sessionID);
            } else if (command.equals("reload")) {
                String target = getParameter("target",hashParameter).trim();
                String file   = getParameter("file",hashParameter).trim();
                if (target.equals("useragent")) {   // ユーザーエージェントファイル更新
                    if (file.length() > 0) {
                        UserAgent.init(file);
                    } else {
                        UserAgent.init();
                    }
                } else if (target.equals("zip")) {  // 郵便番号辞書ファイル更新
                    if (file.length() > 0) {
                        
                    } else {
                        
                    }
                }
            } else if (command.equals("reset")) {
                String target = getParameter("target",hashParameter).trim();
                if (target.equals("sessionstack")) {
                    SessionManager.clearStack();
                }
            } else if (command.equals("thread")) {
                int active_count = Thread.activeCount();
                Thread ta[] = new Thread[active_count];
        
                int n = Thread.enumerate(ta);
                ch.add("checkThread:active_count="+active_count+" enumerate="+n);
                for (int i = 0; i < n; i++) {
                    ch.add("<br>\nThread "+i+" is "+ta[i].getName());
                }
                ch.add("\n");
            } else if (command.equals("sessionlist")) {
                //checkServer(ch);
                //checkVersion(ch);
                checkMemory(ch);
                ch.add("<br>\n");
                SessionManager.getSessionList(ch, true);
                //ch.replace("\n","<br>\n");
            } else if (command.equals("checksession")) {
                SessionManager.checkSession("CMD",true);
            } else if (command.equals("sessioncheck")) {
                int n = CharArray.getInt(getParameter("count",hashParameter));
                if (n > 0) SessionManager.setErrorCount(n);
                
            } else if (command.equals("remove")) {
                int id = CharArray.getInt(getParameter("id",hashParameter));
                if (id > 0) SessionManager.remove(id, "CheckServlet");
                else {
                    String strs = getParameter("timeout",hashParameter).trim();
                    if (strs.length() > 0) {
                        char c = strs.charAt(0);
                        if (c >= '0' && c <= '9') {
                        
                            SessionManager.removeSessions(CharArray.getInt(strs));
                            SessionManager.checkSession("check");
                        }
                    }
                }
            } else if (command.equals("all")) {
                checkServer(ch);
                checkVM(ch);
                checkVersion(ch);
                ch.add("System startup at : ");
                ch.add(SystemManager.szStartupTime);
                ch.add("<br>\n");
                checkMemory(ch);
                ch.add("<br>\n");
                checkStack(ch);
                ch.add("<br>\n");
                checkSession(ch);
                ch.add("<br>\n");
            }
            
            //------------------------
            if (debugCheck) {
                System.out.println("response:"+ch);
            }
            if (ch.length() > 0) {
                PrintWriter out = response.getWriter();
                ch.println(out);
                out.close();
            }
            CharArray.push(ch);
            
        } catch (Exception ex) {
            if (debug) {
                System.out.println("check:");
                ex.printStackTrace();
            }
        }
    }
    
    private void checkMemory(CharArray ch) {
        long freeMem  = Runtime.getRuntime().freeMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long useMem   = totalMem - freeMem;
        ch.add("[Memory]");
        ch.add(" used: ");
        ch.format(useMem,10,12,',');
        ch.add(" free: ");
        ch.format(freeMem,10,12,',');
        ch.add(" total: ");
        ch.format(totalMem,10,12,',');
    }
    private void checkFree(CharArray ch) {
        long freeMem  = Runtime.getRuntime().freeMemory();
        ch.format(freeMem);
    }
    private void checkSession(CharArray ch) {
        ch.add("  [Session] count:");
        ch.format(SessionManager.getSessionCount());
        ch.add(" DAO:");
        ch.format(SessionManager.getDAOCount());
        ch.add(" stack:");
        ch.format(SessionManager.getStackSize());
    }
    private void checkStack(CharArray ch) {
        ch.add("[Stack]");
        ch.add(" char: ");
        ch.format(CharArrayStack.getInstance().size());
        ch.add(" int: ");
        ch.format(IntObject.size());
        ch.add(" token: ");
        ch.format(CharToken.getStackSize());
        ch.add(" session: ");
        ch.format(SessionManager.getStackSize());
        
        //ch.add(" table: ");
        //ch.format(com.miraidesign.system.SystemManager.tableManager.size());
    }
    private void checkVersion(CharArray ch) {
        CharArrayQueue queue = com.miraidesign.common.Version.queue;
        for (int i = 0; i < queue.size(); i++) {
            ch.add(queue.peek(i));
            ch.add("<br>\n");
        }
    }
    private void checkServer(CharArray ch) {
        String hostName = System.getProperty("host.name");
        if (hostName != null && hostName.length() > 0) {
            ch.add("host:");
            ch.add(hostName);
            ch.add("; ");
        }
        String serverName = System.getProperty("server.name");
        if (serverName != null && serverName.length() > 0) {
            ch.add("server:");
            ch.add(serverName);
            ch.add("; ");
        }
        ch.add(SystemManager.httpURL);
        ch.add(" ["); ch.add(SystemManager.serverKey); ch.add("]");
        ch.add(" cache:"); ch.add(SystemManager.cacheServerURL);
        ch.add("<br>\n");
    }
    private void checkVM(CharArray ch) {
        ch.add(Const.os_name);
        ch.add(", "); ch.add(Const.java_vm_name);
        ch.add(" "); ch.add(Const.java_version);
        ch.add(", "); ch.add(Const.tomcat_version);
        ch.add("<br>\n");
    }
}

//
// [end of CheckServlet.java]
//

