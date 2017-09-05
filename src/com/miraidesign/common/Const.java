//------------------------------------------------------------------------
//    Const.java
//              共通定数
//     Copyright (c) Mirai Design Institute 2010-2011 All Rights Reserved. 
//------------------------------------------------------------------------
//             
//------------------------------------------------------------------------

package com.miraidesign.common;

/** 共通定数 */
public class Const {
    public static String os_name;
    public static String os_arch;
    public static String os_version;
    public static String java_vendor;
    public static String java_version;
    public static String java_compiler;      // 注)MSJVMはnull
    public static String java_home;          // java home
    public static String java_vm_name;
    public static String java_vm_vendor;
    public static String java_vm_version;
    public static String data_model;
    public static String file_encoding;
    public static String user_home;
    public static String user_name;
    public static String user_dir;           // ユーザーディレクトリ
    public static String user_country;
    public static String user_language;
    
    public static String catalina_home;
    public static String catalina_base;

    public static boolean isWin32 = false;
    public static boolean isWindows = false;
    public static boolean isSolaris = false;
    public static boolean isLinux   = false;
    public static boolean isMicrosoft = false;
    public static boolean isSun = false;
    public static boolean isIBM = false;

    static {
        os_name         = System.getProperty("os.name");
        os_arch         = System.getProperty("os.arch");
        os_version      = System.getProperty("os.version");
        java_vendor     = System.getProperty("java.vendor");
        java_version    = System.getProperty("java.version");
        java_compiler   = System.getProperty("java.compiler");
        java_home       = System.getProperty("java.home");
        java_vm_name    = System.getProperty("java.vm.name");
        java_vm_vendor  = System.getProperty("java.vm.vendor");
        java_vm_version = System.getProperty("java.vm.version");
        data_model      = System.getProperty("sun.arch.data.model");
        
        file_encoding   = System.getProperty("file.encoding");
        user_home       = System.getProperty("user.home");
        user_name       = System.getProperty("user.name");
        user_dir        = System.getProperty("user.dir");
        user_country    = System.getProperty("user.country");
        user_language   = System.getProperty("user.language");
        catalina_home   = System.getProperty("cataline.home");
        catalina_base   = System.getProperty("cataline.base");
        
        isWin32      = os_name.toLowerCase().startsWith("windows");
        isWindows    = os_name.toLowerCase().startsWith("windows");
        isSolaris    = os_name.toLowerCase().startsWith("solaris");
        isLinux      = os_name.toLowerCase().startsWith("linux");
        
        isMicrosoft  = java_vendor.toLowerCase().startsWith("microsoft");
        isSun        = java_vendor.toLowerCase().startsWith("sun");
        isIBM        = java_vendor.toLowerCase().startsWith("ibm");
    }
    
    public static String szMinus;
    static {
        szMinus = isSolaris ? "\u2212" : "－";
    }
    
    public static String tomcat_version = "";       // MD servletで設定する
    public static String server_name = "";          // server.name (起動環境)
    public static String host_name = "";            // hostname
    static {
        server_name     = System.getProperty("server.name");
        host_name       = System.getProperty("host.name");
    }
}

//
// [end of Const.java]
//

