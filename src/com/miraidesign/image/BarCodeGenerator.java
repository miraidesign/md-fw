//------------------------------------------------------------------------
// @(#)BarCodeGenerator.java
//          バーコード（JAN-8)の生成
//          Copyright (c) Mirai Design 2010  All Rights Reserved.
//------------------------------------------------------------------------
//  ※ツール gifbar に依存します
//------------------------------------------------------------------------
package com.miraidesign.image;

import java.io.File;
import java.io.IOException;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.UserAgent;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.StreamAbsorber;

/**
 * バーコード(JAN-8)生成
 *  
 *  @version 0.5 
 *  @author toru ishioka
 *  @since  JDK1.1
*/

public class BarCodeGenerator {
    private static boolean debug = (true & SystemConst.debug);
    private static boolean debugProcess = (true & SystemConst.debug);
    
    private static String szCommand  = "/usr/local/bartool/gifbar";

    private static int  minWidth    = 69;   // バーコード最小幅（勝手に変更不可）
    private static int  minHeight   = 32;   // バーコード最小高さ
    private static int  marginWidth     = 8;    // 水平マージン
    private static int  marginHeight     = 8;    // 水直マージン
    
    //protected int getMinWidth() { return width; }
    //protected int getHorizontalMargin() { return marginWidth;}
    
    /** 端末に合わせた倍率を求める (ImageProxy で使用する)
        @param ua USerAgent
        @param version バージョン
        @param max_width スキャン最大サイズ
        @return width
    */
    protected static int getWidth(UserAgent ua, int version, int max_width) {
        //if (ua.isPC()) return 4;
        int lcd_width = ua.getLCDWidth();
        int width = ua.width;
        if (max_width > 0 && lcd_width > 0 && max_width < lcd_width) {
            width = (int)Math.round((double)width * max_width / lcd_width);
if (debug) System.out.println("JAN8:width changed:"+ua.width+" ->"+width+" ("+lcd_width+")");
        }
        width -=  marginWidth * 2;
        width /= minWidth;
if (debug) System.out.println("BarCodeGenerator;getWidth:"+ua.width+"->"+width);
        return width;
    }
    
    private static Runtime runtime;
    
    /** コマンド設定 
      @param str コマンド
    */
    public static void setCommand(String str) { szCommand = str; }

    // SystemManager から呼び出し  
    public static void setMinWidth(int min) { minWidth = min;}
    public static void setMinHeight(int min) { minHeight = min;}
    public static void setHorizontalMargin(int min) { marginWidth = min;}
    public static void setVerticalMargin(int min) { marginHeight = min;}
    
    /** コンソールデバッグ表示オンオフ 
      @param mode true/false
    */
    public static void setDebug(boolean mode) { debug = mode; }
    
    /**
        バーコード生成
        @param code バーコード
        @param outFile 出力ファイル名
        @param version バージョン
        @param width ｎ倍出力  1,2,3.. 
        @throws IOException IOException
        @return 成功
    */
    static public boolean convert(CharArray code,      // バーコード
                                  File outFile,        // 出力ファイル名
                                  int    version,      // バージョン（未使用）
                                  int    width         // ｎ倍出力  1,2,3..  
                               ) throws IOException
    {
        if (debug) System.out.println("BarCodeGenerator.convert("+code+","+outFile.getPath()+","+width+")");
        
        //  実行

        if (runtime == null) runtime = Runtime.getRuntime();
        
        String [] strs = new String[7];
        strs[0] = szCommand;
        strs[1] = "-c:"  + code;
        strs[2] = "-o:"  + outFile.getPath();
        strs[3] = "-w:"  + width;
        strs[4] = "-h:"  + (minHeight*width);
        strs[5] = "-xp:" + (marginWidth*width);
        strs[6] = "-yp:" + (marginHeight*width);

        
        Process process = runtime.exec(strs);
        if (debugProcess) {
            for (int i = 0; i < strs.length; i++) {
                System.out.println(strs[i]);
            }
        }
        StreamAbsorber stdinAbsorber = new StreamAbsorber(process.getInputStream(),"STD:");
        StreamAbsorber errorAbsorber = new StreamAbsorber(process.getErrorStream(),"ERR:");
        stdinAbsorber.start();
        errorAbsorber.start();
        int sts = -1;
        try {
            if (debugProcess) System.out.println("プロセス終了待機中...");
            sts = process.waitFor();
            if (sts != 0) {
                //for (int i = 0; i < strs.length; i++) {
                //    System.out.print("  "+strs[i]);
                //}
                System.out.println("変換プロセス異常終了!! "+sts);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (debugProcess) System.out.println("stdinAbsorberの終了待機中...");
        while (stdinAbsorber.isAlive()) {}
        if (debugProcess) System.out.println("errorAbsorberの終了待機中...");
        while (errorAbsorber.isAlive()) {}
        
        if (sts != 0) {
            System.out.println("------STD:");
            System.out.println(stdinAbsorber.getMessage());
            System.out.println("------ERR:");
            System.out.println(errorAbsorber.getMessage());
            System.out.println("------END:");
        }
        return (sts == 0);
    }
}

//
//
// [end of BarCodeGenerator.java]
//
