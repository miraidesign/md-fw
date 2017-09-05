//------------------------------------------------------------------------
//    PrefixFilter.java
//                 
//             Copyright (c) ishioka.toru@miraidesign.com 2013 All Rights Reserved.
//------------------------------------------------------------------------
package com.miraidesign.util;

import java.io.*;

/** 前置フィルタ */
public class PrefixFilter implements FilenameFilter {
    boolean ignoreCase = false;
    String[] strs;
    
    // consructor
    public PrefixFilter() {
        // do nothing;
    }
    public PrefixFilter(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    public PrefixFilter(String prefix) {
        setPrefix(prefix);
    }
    public PrefixFilter(String[] prefix) {
        setPrefix(prefix);
    }
    public PrefixFilter(CharArrayQueue prefix) {
        setPrefix(prefix);
    }
    public PrefixFilter(String prefix, boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        setPrefix(prefix);
    }
    public PrefixFilter(String[] prefix, boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        setPrefix(prefix);
    }
    public PrefixFilter(CharArrayQueue prefix, boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        setPrefix(prefix);
    }
    
    // set method
    public void setPrefix(String prefix) {
        if (strs == null || strs.length != 1) strs = new String[1];
        strs[0] = ignoreCase? prefix.toUpperCase() : prefix;
    }
    public void setPrefix(String[] prefix) {
        if (prefix != null) {
            int size = prefix.length;
            if (strs == null || strs.length != size) strs = new String[size];
            for (int i = 0; i < size; i++) {
                strs[i] = ignoreCase? prefix[i].toUpperCase() : prefix[i];
            }
        }
    }
    public void setPrefix(CharArrayQueue prefix) {
        if (prefix != null) {
            int size = prefix.size();
            if (strs == null || strs.length != size) strs = new String[size];
            for (int i = 0; i < size; i++) {
                strs[i] = ignoreCase? prefix.peek(i).toUpperCase().toString() 
                                    : prefix.peek(i).toString();
            }
        }
    }
    // FilenameFilter event
    public boolean accept(File dir,String name) {
        boolean sts = false;
        if (strs != null) {
            for (int i = 0; i < strs.length; i++) {
                String str = strs[i];
                if (ignoreCase && name.toUpperCase().startsWith(str)) {
                    sts = true; break;
                } else if (name.startsWith(str)) {
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
// [end of PrefixFilter.java]
//

