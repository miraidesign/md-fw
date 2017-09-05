//------------------------------------------------------------------------
//    AbstractSystemManager.java
//                 システム全体の管理
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
package com.miraidesign.system;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.Util;

/**
 *  システム全体の管理を行います。<br>
 *  実際はこれを継承したシステム毎のSystemManagerが、
 * システム固有の情報とともに管理します。
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public abstract class AbstractSystemManager {
    static private boolean debug = (SystemConst.debug && false);  // デバッグ表示
    static protected boolean ready = false; // データロード後にtrue にする
    
    //-------------------------------------------------------------------------
    // consuructor なし
    //-------------------------------------------------------------------------
    public AbstractSystemManager() {  }

    static public void init() {
        System.out.println("AbstractSystemManager init");
    }

//  /** サイトを追加する 
//      @param sitename サイト名
//      @param sitecodes サイトコードの配列
//  */
//  static public void addSite(String sitename, int[] sitecodes) {
//      SiteManager.add(sitename,sitecodes);
//  }
//  /** サイトを追加する 
//      @param sitename サイト名
//      @param sitecodes サイトコードの配列
//  */
//  static public void addSite(CharArray sitename, int[] sitecodes) {
//      SiteManager.add(sitename,sitecodes);
//  }

    /** サイトコードより ModuleManager を取得する
        @param sitecode サイトコード
        @return ModuleManager
    */
    static public ModuleManager getModuleManager(int sitecode) {
        waitInitialize();
        return SiteManager.get(sitecode);
    }
    /** サイト名より ModuleManager を取得する
        @param sitename サイト名
        @return ModuleManager
    */
    static public ModuleManager getModuleManager(CharArray sitename) {
        waitInitialize();
        return SiteManager.get(sitename);
    }
    /** サイト名より ModuleManager を取得する
        @param sitename サイト名
        @return ModuleManager
    */
    static public ModuleManager getModuleManager(String sitename) {
        waitInitialize();
        return SiteManager.get(sitename);
    }
    static public void waitInitialize() {
        int max = 50;
        for (int i = 1; i <= max && !ready; i++) {
            Thread.yield();
            Util.Delay(200);    // 100ミリ待つ
            System.out.println("Initialize wait:"+(200*i));
        }
    }
}

//
//
// [end of AbstractSystemManager.java]
//

