//------------------------------------------------------------------------
//    MagicFilter.java
//                 
//             Copyright (c) Toru Ishioka 1997-2002 All Rights Reserved.
//                 update: 1998-12-09 ishioka JDK1.2対応
//------------------------------------------------------------------------
package com.miraidesign.util;

import java.io.*;

/** ファイル名フィルタ */
public class MagicFilter implements FilenameFilter {
  boolean debug = false;
  long magicNumber;
  public MagicFilter(long magic) {
    magicNumber = magic;
  }
    
  public boolean accept(File dir,String name) {
        //System.out.print(name+":");
    boolean rsts = false;
    long l = -1;
    try{
        String szDir = dir.toString();
        if (!szDir.endsWith("/") && !szDir.endsWith("\\")) {
            szDir = Util.getFilename(szDir+"/");
        }
        FileInputStream fin = new FileInputStream(szDir+name);
        DataInputStream din = new DataInputStream(fin);
        //if (magicNumber == din.readLong())
        l = din.readLong();
        if (magicNumber == l) rsts = true;
        fin.close();
    }
    catch(IOException e){ 
        if (debug) System.out.println("MagicFilter:accept:"+name + " Exception:"+e);
    }
    if (debug) System.out.println("MagicFilter:accept:"+name + " "+rsts+" no="+l);
    return rsts;
  }
}

//
// [end of MagicFilter.java]
//

