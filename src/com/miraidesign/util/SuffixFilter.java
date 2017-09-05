//------------------------------------------------------------------------
//    SuffixFilter.java
//                 
//             Copyright (c) Toru Ishioka 1997-2013 All Rights Reserved.
//                      update: 2013-01-13 ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
package com.miraidesign.util;

import java.io.*;

/** 拡張子フィルタ */
public class SuffixFilter implements FilenameFilter {
    boolean ignoreCase = false;
    String[] strs;
    
    // consructor
    public SuffixFilter() {
        //do nothing
    }
    public SuffixFilter(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    public SuffixFilter(String suffix) {
        setSuffix(suffix);
    }
    public SuffixFilter(String[] suffix) {
        setSuffix(suffix);
    }
    public SuffixFilter(CharArrayQueue suffix) {
        setSuffix(suffix);
    }
    public SuffixFilter(String suffix, boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        setSuffix(suffix);
    }
    public SuffixFilter(String[] suffix, boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        setSuffix(suffix);
    }
    public SuffixFilter(CharArrayQueue suffix, boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        setSuffix(suffix);
    }
    // set method
    public void setSuffix(String suffix) {
        if (strs == null || strs.length != 1) strs = new String[1];
        strs[0] = ignoreCase? suffix.toUpperCase() : suffix;
    }
    public void setSuffix(String[] suffixes) {
        if (suffixes != null) {
            int size = suffixes.length;
            if (strs == null || strs.length != size) strs = new String[size];
            for (int i = 0; i < size; i++) {
                strs[i] = ignoreCase? suffixes[i].toUpperCase() : suffixes[i];
            }
        }
    }
    public void setSuffix(CharArrayQueue suffixes) {
        if (suffixes != null) {
            int size = suffixes.size();
            if (strs == null || strs.length != size) strs = new String[size];
            for (int i = 0; i < size; i++) {
                strs[i] = ignoreCase? suffixes.peek(i).toUpperCase().toString() 
                                    : suffixes.peek(i).toString();
            }
        }
    }
    
    // FilenameFilter event
    public boolean accept(File dir,String name) {
        boolean sts = false;
        if (strs != null) {
            for (int i = 0; i < strs.length; i++) {
                String str = strs[i];
                if (ignoreCase && name.toUpperCase().endsWith(str)) {
                    sts = true; break;
                } else if (name.endsWith(str)) {
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
// [end of SuffixFilter.java]
//

