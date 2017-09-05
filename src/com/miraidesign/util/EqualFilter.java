//------------------------------------------------------------------------
//    EqualFilter.java
//                 
//             Copyright (c) ishioka.toru@miraidesign.com 2013 All Rights Reserved.
//------------------------------------------------------------------------
package com.miraidesign.util;

import java.io.*;

/** 文字列一致フィルタ */
public class EqualFilter implements FilenameFilter {
    boolean ignoreCase = false;
    String[] strs;

    // constructor
    public EqualFilter() {
        // do nothing
    }
    public EqualFilter(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    public EqualFilter(String name) {
        setName(name);
    }
    public EqualFilter(String name, boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        setName(name);
    }
    public EqualFilter(String[] name, boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        setName(name);
    }
    public EqualFilter(CharArrayQueue name, boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        setName(name);
    }
    
    // set method
    public void setName(String name) {
        if (strs == null || strs.length != 1) strs = new String[1];
        strs[0] = ignoreCase? name.toUpperCase() : name;
    }
    public void setName(String[] name) {
        if (name != null) {
            int size = name.length;
            if (strs == null || strs.length != size) strs = new String[size];
            for (int i = 0; i < size; i++) {
                strs[i] = ignoreCase? name[i].toUpperCase() : name[i];
            }
        }
    }
    public void setName(CharArrayQueue name) {
        if (name != null) {
            int size = name.size();
            if (strs == null || strs.length != size) strs = new String[size];
            for (int i = 0; i < size; i++) {
                strs[i] = ignoreCase? name.peek(i).toUpperCase().toString() 
                                    : name.peek(i).toString();
            }
        }
    }
    
    // FilenameFilter event
    public boolean accept(File dir,String name) {
        boolean sts = false;
        if (strs != null) {
            for (int i = 0; i < strs.length; i++) {
                String str = strs[i];
                if (ignoreCase && name.toUpperCase().equals(str)) {
                    sts = true; break;
                } else if (name.equals(str)) {
                    sts = true; break;
                } else {
                    sts = false;
                }
            } // next
        }
        return sts;
    }
}

//
// $Author: ishioka $
//
// [end of EqualFilter.java]
//

