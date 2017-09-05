//------------------------------------------------------------------------
// @(#)CommonServlet.java
//              サーブレットの入り口となります
//              Copyright (c) Mirai Design 2010 All Rights Reserved.
//------------------------------------------------------------------------
//
package com.miraidesign.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.miraidesign.common.SystemConst;

/**
    サーブレットの入り口となります
*/

public class CommonServlet extends MDServlet /*implements com.miraidesign.data.RSDataAccess*/ {
    static private boolean debug = true;  // デバッグ表示
    static int initCount = 0;

    public void init(ServletConfig config) throws ServletException {
        ++initCount;
        debug &= SystemConst.debug;
        if (debug) System.out.println("== ccm.init(config) == "+initCount);
        super.init(config);
        if (initCount == 1) {
            debug &= SystemConst.debug;
            super.initModules();    // モジュールの初期化を行う (１回のみ)
        }
    }
    public void init() throws ServletException {
        if (debug) System.out.println("== ccm.init() == "+initCount);
        super.init();
    }
    /**
    public boolean rsExecute(SessionObject session, ResultSet rs, Object o)
                 throws SQLException {
                 return true;
    }
    */
}

//
//
// [end of CommonServlet.java]
//

