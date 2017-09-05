//------------------------------------------------------------------------
//    UserLogFactory.java
//                 UserLog管理クラス
//                 Copyright (c) MIrai Design 2010-17 All Rights Reserved.
//------------------------------------------------------------------------
//
package com.miraidesign.util;

import java.util.Hashtable;

/** UserLog管理クラス */
public class UserLogFactory {
    static Hashtable<String,UserLog> hash = new Hashtable<String,UserLog>();

    static public UserLog getUserLog(String key) {
        if (key == null || key.trim().length()==0) return null;
        UserLog log = (UserLog)hash.get(key);
        if (log == null) {
            log = new UserLog(key);
            hash.put(key,log);
        }
        return log;
    }
}

//
// [end of UserLogFactory.java]
//

