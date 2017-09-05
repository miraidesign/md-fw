//------------------------------------------------------------------------
//    GCManager.java
//          GC マネージャー
//     Copyright (c) Mirai Design Institutes 2010-13 All Rights Reserved. 
//------------------------------------------------------------------------
package com.miraidesign.system;

import java.text.SimpleDateFormat;

import com.miraidesign.session.SessionManager;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.Util;
import com.miraidesign.util.UserLog;
import com.miraidesign.util.UserLogFactory;

/**
    GCManager
**/

public class GCManager {
    private  static boolean debug = true;
    //static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)");
    static SimpleDateFormat sdfz = SystemManager.sdfz;
    
    static private int  GC    = 1;
    static private long TIMER = 0;
    
    static int gc_count = 0;
    
    static public long gc_time = 0;  // 最終GC実行日時
    static public long old_mem = 0;  // 最終GC実行直前の空きメモリサイズ
    static public long new_mem = 0;  // 最終GC実行直後の空きメモリサイズ
    
    static private UserLog tracking = null;
    
    static public void setDebug(boolean mode) {
        debug = mode;
    }
    static public void setTrackingLog(boolean mode) {
        if (mode) tracking = UserLogFactory.getUserLog("[Tracking]");
        else tracking = null;
    }
    /** カウンター設定 
        @param count 0: 行わない DAO.clearTable() n 回に一回行う
    */
    static public void setCounter(int count) {
        if (count >= 0) GC = count;
    }
    
    /** タイマー設定 
        @param timer  0: 行わない n秒に一回行う
    */
    static public void setTimer(long timer) {
        if (timer >= 0) TIMER = timer * 1000;
        gc_time = System.currentTimeMillis();
    }
    
    /** 
        GCを実行する 
        @return true:GCを行った
    */
    static public boolean execute() {
        return execute(false);
    } 
    /** GCを実行する 
        @param mode :GCを強制実行する
        @return true:GCを行った
    */
    static public boolean execute(boolean mode) {
        boolean sts = false;
        if (GC_RUN == false) {
            sts = _execute(mode);
        }
        return sts;
    }
    
    
    private static boolean GC_RUN = false;
    
    /** GC 中か？*/
    public static boolean isRunning() {
        return GC_RUN;
    }
    
    /** 
        GCを実行する 
        @return true:GCを行った
    */
    static private boolean _execute() {
        return _execute(false);
    } 
    
    /** GCを実行する 
        @param mode :GCを強制実行する
        @return true:GCを行った
    */
    static private /*synchronized*/ boolean _execute(boolean mode) {
        boolean sts = false;
        synchronized (SessionManager.getHashSessionObjects()) {  // 2013-11-19 
          GC_RUN = true;
          do {
            long now = System.currentTimeMillis();
            String str = "";
            if (debug) {
               java.util.Date date = new java.util.Date(now);
               synchronized (sdfz) {
                    str = sdfz.format(date);
               }
            }
            if (mode) {
                old_mem = Runtime.getRuntime().freeMemory();
                long timer = Util.Timer();
                if (debug) System.out.println("◆GCManager GC start(true)");
                System.gc();
                new_mem = Runtime.getRuntime().freeMemory();
                if (debug) {
                    CharArray ch = CharArray.pop();
                    ch.add("◆GCManager "); ch.add(str);
                    ch.add(" mode  :true   Lapse:"); ch.format(Util.Lapse(timer),10,4);
                    ch.add(" free:"); ch.format(old_mem,10,13,',');
                    ch.add("->");     ch.format(new_mem,10,13,',');
                    ch.add(" session:"); ch.format(SessionManager.getSessionListSize(),10,2);
                    ch.add('/');   ch.format(SessionManager.getSessionCount(),10,2);
                    
                    System.out.println(ch.toString());
                    CharArray.push(ch);
                }
                if (tracking != null) {
                    tracking.log("GC                  : Lapse:"+Util.Lapse(timer,6));
                }
                
                if (GC > 0) gc_count = 1;
                if (TIMER > 0) gc_time = System.currentTimeMillis();
                sts = true;
                break;
            }
            if (GC > 0) {
                if (gc_count == 0) {
                    old_mem = Runtime.getRuntime().freeMemory();
                    long timer = Util.Timer();
                    if (debug) System.out.println("◆GCManager GC start:"+GC);
                    System.gc();
                    new_mem = Runtime.getRuntime().freeMemory();
                    if (debug) {
                        CharArray ch = CharArray.pop();
                        ch.add("◆GCManager "); ch.add(str);
                        ch.add(" count  :"); ch.format(GC); ch.add(" Lapse:"); ch.format(Util.Lapse(timer),10,4);
                        ch.add(" free:"); ch.format(old_mem,10,13,',');
                        ch.add("->");     ch.format(new_mem,10,13,',');
                        ch.add(" session:"); ch.format(SessionManager.getSessionListSize(),10,2);
                        ch.add('/');   ch.format(SessionManager.getSessionCount(),10,2);
                        System.out.println(ch.toString());
                        CharArray.push(ch);
                        
                    }
                    if (tracking != null) {
                        tracking.log("GC(c)               : Lapse:"+Util.Lapse(timer,6));
                    }
                    
                    if (++gc_count >= GC) gc_count = 0;
                    if (TIMER > 0) gc_time = System.currentTimeMillis();
                    sts = true;
                    break;
                }
                if (++gc_count >= GC) gc_count = 0;
            }
            if (TIMER > 0) {
                if (now - gc_time > TIMER) {
                    old_mem = Runtime.getRuntime().freeMemory();
                    long timer = Util.Timer();
                    if (debug) System.out.println("◆GCManager GC start:"+TIMER);
                    System.gc();
                    new_mem = Runtime.getRuntime().freeMemory();
                    if (debug) {
                        System.out.println("◆GCManager "+str+" TIMER:"+TIMER+"   Lapse:"+Util.Lapse(timer)+" free:"+old_mem+"->"+new_mem
                                                   +" session:"+SessionManager.getSessionListSize()+"/"+SessionManager.getSessionCount());
                    }
                    if (tracking != null) {
                        tracking.log("GC(t)               : Lapse:"+Util.Lapse(timer,6));
                    }
                    
                    if (GC > 0) gc_count = 1;
                    gc_time = System.currentTimeMillis();
                    sts = true;
                    
                    break;
                }
            }
          } while (false);
          GC_RUN = false;
        }   // synchronized
        return sts;
    }
}

//
// [end of GCManager.java]
//

