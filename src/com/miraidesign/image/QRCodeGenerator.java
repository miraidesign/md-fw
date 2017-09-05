//------------------------------------------------------------------------
// @(#)QRCodeConverter.java
//          QRコード画像ファイルを出力する
//          Copyright (c) Mirai Design 2010 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.image;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.UserAgent;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.StreamAbsorber;

/**
 * バーコード(QRコード)生成
 *  
 *  @version 0.5 
 *  @author toru ishioka
 *  @since  JDK1.1
*/

public class QRCodeGenerator {
    private static boolean debug = (true & SystemConst.debug);
    private static boolean debugProcess = (true & SystemConst.debug);

    private static int[][] checkH = {/*1*/{17,10, 7},
                                     /*2*/{34,20,14},
                                     /*3*/{58,35,24},
                                     /*4*/{82,50,34},
                                     /*5*/{106, 64, 44},
                                     /*6*/{139, 84, 58},
                                     /*7*/{154, 93, 64},
                                     /*8*/{202,122, 84},
                                     /*9*/{235,143, 98}};

    private static String szCommand = "/usr/local/qr_img0.40/cgi/qrcode.bat";

    /** コンソールデバッグ表示オンオフ */
    public static void setDebug(boolean mode) { debug = mode; }
    /** コマンド設定 */
    public static void setCommand(String str) { szCommand = str; }

    private static Runtime runtime = Runtime.getRuntime();

    private static int minimumVersion = 1;  //@@// 未使用

    /** 最小バージョンを取得する */
    public static void setMinimumVersion(int ver) {
        minimumVersion = ver;
    }
    
    
    private static String errorLevel = "H";
    /** エラーレベル L/M/Q/Hを指定 **/
    public static void setErrorLever(String str) {
        errorLevel = str;
    }
    
    /** 文字列によって表示可能な最小バージョンを求める 
    **/
    protected static int getVersion(CharArray ch) {
        int version = 10;
        int n = 2;
        int len = ch.strlen()+2;    // ゆとりを持たせておく
        if (ch.isDigit()) { // 数値のみ
            n = 0;
        } else if (ch.isAlnum()) {  // 英数字
            n = 1;
        }
        for (int i = 1; i <= 9; i++) {
            int size = checkH[i-1][n];
            if (size > len) {
                version = i;
                break;
            }
        }
        return (minimumVersion > version) ? minimumVersion : version;
    }

    /** 端末に合わせた倍率を求める (ImageProxy で使用する)*/
    protected static int getWidth(UserAgent ua, int version, int max_width) {
        int size = (21+4) + (version * 4);  // 基本サイズ
        int lcd_width = ua.getLCDWidth();
        int width = Math.min(ua.width, ua.height);
        if (max_width > 0 && lcd_width > 0 && max_width < lcd_width) {
            width = (int)Math.round((double)width * max_width / lcd_width);
if (debug) System.out.println("QR:width changed:"+ua.width+" ->"+width+" ("+lcd_width+")");
        }
        width /= size;
        
if (debug) System.out.println("QRCodeGenerator;getWidth:"+ua.width+"->"+width);
        return width;
    }

    static public boolean convert(CharArray code,      // バーコード
                            File outFile,        // 出力ファイル名
                            int version,
                            int size) throws IOException {
        if (debug) System.out.println("QRCodeGenerator.generate()の実行");

        if (debug) {
            System.out.println("コード:" + code);
            System.out.println("version:" + version);
            System.out.println("エラー訂正レベル:" + errorLevel);
            System.out.println("モジュールサイズ:" + size);
            System.out.println("出力ファイル:" + outFile.getPath());
        }
        String[] strs = new String[] {
            szCommand,
            //code.kanjiToURL("MS932").toString(),   // MS932エンコードして渡す
            URLEncoder.encode(code.toString(),"MS932"),
            String.valueOf(version),
            errorLevel,
            String.valueOf(size),
            "P",
            outFile.getPath()
        };

        Process process = runtime.exec(strs);
        if (debugProcess) {
            for (int i = 0; i < strs.length; i++) {
                System.out.println(strs[i]);
            }
        }
        StreamAbsorber stdinAbsorber = new StreamAbsorber(process.getInputStream(),"STD:");  // ,System.out
        StreamAbsorber errorAbsorber = new StreamAbsorber(process.getErrorStream(),"ERR:");  // ,System.out
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
            System.out.print(stdinAbsorber.getMessage());
            System.out.println("------ERR:");
            System.out.print(errorAbsorber.getMessage());
            System.out.println("------END:");
        }
        return (sts == 0);
    }
}

//
//
// [end of QRCodeConverter.java]
//

