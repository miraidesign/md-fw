//------------------------------------------------------------------------
//    Version.java
//                 バージョン管理ファイル
//     Copyright (c) Mirai Design Institute 2010-2020 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.common;

import com.miraidesign.util.CharArrayQueue;

/**
    MD Framework のバージョンを保管します。<br>
*/
public class Version {
    static public int    version        =   1035;
    static public String version_string = "1.035";
    static public String version_date   = "2022-10-15 00:19 (JST)";
    //-------------------------------------------------------
    static public int getVersion() { return version;}
    static public String getVersionString() { return version_string;}
    static public String getVersionDate() { return version_date;}

    static public String szVersion = "md-fw ver."+version_string+" "+version_date;

static {
    System.out.println(szVersion);
}

    static public CharArrayQueue queue = new CharArrayQueue() {{
        enqueue(szVersion);
    }};

    /** メジャーバージョン */
    static public /*final*/ int major =  version /1000;   // 0:

    /** マイナーバージョン */
    static public /*final*/ int minor =  (version%1000)/100;

    /** セッション保存用バージョン */
    static public /*final*/ int save =  version;

    /** セッション読み込みバージョン <br>
        この内容がSessionObject#version に保管される */
    static public           int load =  0;

}

//
// [end of Version.java]
//

