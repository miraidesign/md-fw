//------------------------------------------------------------------------
//    ImageProxy.java
//                 JAIを使ったイメージプロキシ
//                 メソッドの引数にnullを渡してはならない。
//                 戻り値がnullの場合はエラーが起こったことを表す。
//                 Copyright (c) MIraiDesign 2010 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.image;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import com.miraidesign.common.SystemConst;
import com.miraidesign.system.ModuleManager;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.Util;
import com.miraidesign.session.SessionObject;
import com.miraidesign.session.UserAgent;

/**
 * イメージプロキシ
 *  
 *  @version 0.5 
 *  @author toru ishioka
 *  @since  JDK1.1
*/

public class ImageProxy {
    private static boolean debug = (false && SystemConst.debug);
    
    private static int proxyMode = 0;    // 0:JAI使用(デフォルト) 1:ImageAlchemy使用
    private static int anotherMode = 0;  // 上でだめな時の代替選択枝
    private static String rootPath = "/usr/local/tomcat/webapps/";
    private static String middlePath = "/proxy/image/";
    private static String barcodePath = "/proxy/barcode/";

    public static void setMode(int mode) { 
        proxyMode = mode; 
//if (debug) {
//    System.out.println("●");
//    System.out.println("●ProxyModeを "+mode+" にセットします");
//    System.out.println("●");
//}
    }
    public static void setAnotherMode(int mode) { anotherMode = mode;}
    public static void setRootDir(String str) { rootPath = str; }
    public static void setOutputDir(String str) { middlePath = str; }
    public static void setBarcodeDir(String str) { barcodePath = str; }

    public static String getRootDir() { return rootPath;}

    /** 
        Imageコンバート後のURLを取得する 
        @param docRoot   オリジナル画像のドキュメントルート nullの場合はシステムより取得 
    */
    public static CharArray getURL(String docRoot, CharArray srcURLCh, SessionObject session, int width, int ratio) {
        if (srcURLCh.length == 0) {
            return null;
        }
        try {
            return getOutFileName(docRoot, srcURLCh, session.userAgent, session.getModuleManager(),width, ratio, 0, false, false);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }   // end of getURL()

    /** 
        Imageコンバート後のURLを取得する 
    */
    public static CharArray getURL(String docRoot, CharArray srcURLCh, UserAgent ua, int width, int ratio) {
        if (srcURLCh.length == 0) return null;
        try {
            return getOutFileName(docRoot, srcURLCh, ua, null, width, ratio, 0, false, false);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }   // end of getURL()
    /** 
        Imageコンバート後のURLを取得する 
    */
    public static CharArray getURL(String docRoot, CharArray srcURLCh, UserAgent ua, int width, int ratio, int rotate) {
        if (srcURLCh.length == 0) return null;
        try {
            return getOutFileName(docRoot, srcURLCh, ua, null, width, ratio, rotate, false, false);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }   // end of getURL()
    /** 
        Imageコンバート後のURLを取得する 
    */
    public static CharArray getURL(String docRoot, CharArray srcURLCh, UserAgent ua, int width, int ratio, int rotate, boolean flip, boolean flop) {
        if (srcURLCh.length == 0) return null;
        try {
            return getOutFileName(docRoot, srcURLCh, ua, null, width, ratio, rotate, flip, flop);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }   // end of getURL()

    /** BarCodeのURLを取得する
        @param docRoot   オリジナル画像のドキュメントルート nullの場合はシステムより取得 
        @param code      バーコード
        @param session   セッション
        @param mode      バーコードモード
        @param version   バージョン
    */
    public static CharArray getBarcodeURL(String docRoot, CharArray code, SessionObject session, int mode, int version) {
        try {
            CharArray ch = getBarcodeFileName(code, session, mode, version);
            return getOutFileName(docRoot, ch, session.userAgent, session.getModuleManager(), -1, 0, 0, false, false);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    /** ローカルまたはリモートホスト上の画像ファイルをキャリアーにあわせてフォーマットし、
        ローカルホストに保存する。 画像形式による分岐はこの中で行われる。
        @param docRoot   オリジナル画像のドキュメントルート nullの場合はシステムより取得 
        @param srcFileName      変更元ファイル名
        @param userAgent        ユーザーエージェント
        @param width            画像幅： 0で変更なし
        @param rotate           回転：0でなし
        @param flip             上下反転（未サポート）
        @param flop             左右反転（未サポート）
        @return                 変換後のファイル名
    */
    private static CharArray getOutFileName(String docRoot, CharArray srcFileName, UserAgent userAgent, ModuleManager mm, 
                                            int width, int ratio, int rotate, boolean flip, boolean flop) throws IOException {
        boolean convert = true;
        long timer = Util.Timer();
        //UserAgent userAgent = session.userAgent;
        if (debug) System.out.println("▼ImageProxy.getOutFileName("+srcFileName+","+userAgent.getCarrierName()+","+width+")");
        if (userAgent == null) convert = false;                             // 画像変換なし
        else if (userAgent.isPC() && userAgent.getWidth() <= 0) convert = false;
        
        CharArray ch = new CharArray(srcFileName).toLowerCase();
        boolean isGIF = ch.endsWith(".gif");
        if (width == -1 && userAgent != null && userAgent.colorDepth > 2) {   // 表示幅の変更を行わない
            if (ch.endsWith(".gif")) {
                if (userAgent.isGIF()) convert = false;
            } else if (ch.endsWith(".jpeg") || ch.endsWith(".jpg")) {
                if (userAgent.isJPEG()) convert = false;
            } else if (ch.endsWith(".bmp")) {
                if (userAgent.isBMP()) convert = false;
            } else if (ch.endsWith(".png")) {
                if (userAgent.isPNG()) convert = false;
            }
        }
        
        int proxy = proxyMode;
        CharArray sts = srcFileName;
        if (convert) {
            if (userAgent.colorDepth > 0 && userAgent.colorDepth <= 2 && proxy == 0) {  // JAIの階調変換を中止
                if (anotherMode > 0) proxy = anotherMode;
            }
            // スマートフォンのみにする
            sts = prepareOutImg(docRoot,srcFileName, ".jpg", userAgent, mm, width, ratio, proxy, rotate, flip, flop);
        } else { // Servletコンバート用
            if (userAgent == null) {
                System.out.println("★ImageProxy 画像のコンバートを開始します");
                sts = prepareOutImg(docRoot,srcFileName, ".jpg", userAgent, mm, width, ratio, proxy, rotate, flip, flop);
            }
        }
        if (debug) System.out.println("▲ImageProxy.return "+sts+" convert:"+convert+" Lapse:"+Util.Lapse(timer));
        return sts;
        
        
    }   // end of getOutFileName();

    /** バーコード画像を必要に応じて生成し、ローカルホストに保存する。
        @param code             バーコード
        @param userAgent        ユーザーエージェント
        @return                 変換後のファイル名
    */
    private static CharArray getBarcodeFileName(CharArray code, SessionObject session, int mode, int version) throws IOException {
        long timer = Util.Timer();
        if (debug) System.out.println("▼ImageProxy.getBarcodeFileName("+code+","+session.userAgent.deviceName+","+mode+","+version+")");
        
        
        CharArray filename = new CharArray();
        //filename.add(rootPath);
        filename.add(barcodePath);
        if (mode == BarCode.QR) {
            if (version == 0) { // 自動取得
                version = QRCodeGenerator.getVersion(code);
                if (debug) System.out.println(" ->version:"+version);
            }
            filename.add("QR");
            filename.format(code.hashCode());
        } else {
            filename.add(code);
        }
        
        //@@// modeによって変える必要がある
        ModuleManager mm = session.getModuleManager();
        int max_width = 0;
        int width = 1;
        if (mode == BarCode.JAN8) {
            CharArray ch = mm.get("Barcode","JAN8.MaxScanWidth");
            if (ch != null && ch.trim().length() > 0) max_width = ch.getInt();
            width = BarCodeGenerator.getWidth(session.userAgent, version, max_width);
        }
        if (mode == BarCode.QR) {
            CharArray ch = mm.get("Barcode","QR.MaxScanWidth");
            if (ch != null && ch.trim().length() > 0) max_width = ch.getInt();
            width = QRCodeGenerator.getWidth(session.userAgent, version, max_width);
        }
        if (version > 1) {
            filename.add('@');
            filename.format(version);
        }
        if (width < 1) width = 1;
        if (width > 1) {
            //filename.add('$');
            filename.add('_');
            filename.format(width);
        }
        if (mode==BarCode.QR) filename.add(".png");
        else                  filename.add(".gif");
        String szFilename = rootPath+filename;
        File outFile = new File(szFilename);
        if (debug) System.out.println("出力ファイルのパス  = " + outFile.getPath());

        if (!outFile.exists() || !outFile.isFile()) {   // ファイルがあれば出力しない
            boolean dirSuccess = true;
            // 保存先のディレクトリを作成する。
            if (!outFile.getParentFile().mkdirs()) {
                if (!outFile.getParentFile().exists()) {
                    if (debug) System.out.println("出力ディレクトリの作成失敗!");
                    dirSuccess = false;
                }
            }
            if (dirSuccess) {
                switch (mode) {
                    case 1: BarCodeGenerator.convert(code, outFile, version, width); break;
                    case 2: QRCodeGenerator.convert(code, outFile, version, width); break;
                    default: filename = null; break;
                }
            } else {
                filename = null;
            }
        } else {
            if (debug) System.out.println("既存のファイルを出力");
        }
        
        if (debug) System.out.println("▲ImageProxy.getBarcodeFileName() "+filename+" Lapse:"+Util.Lapse(timer));
        return filename;
    } 

//
//  private static CharArray prepareOutImg(CharArray srcFileName, 
//                                         String outExtType,
//                                         SessionObject session, int width, int ratio,
//                                         int proxy)       throws IOException {
//      return prepareOutImg(null,srcFileName, outExtType, session.userAgent, session.getModuleManager(), width,ratio,proxy);
//    }
    // ローカルまたはリモートホスト上の画像ファイルを指定された形式にあわせてフォーマットし、
    // ローカルホストに保存する。 戻り値は変換後のファイル名。
    private static CharArray prepareOutImg(String docRoot,      // オリジナル画像のあるDocRoot
                                           CharArray srcFileName, 
                                           String outExtType,
                                           UserAgent ua,
                                           ModuleManager mm, 
                                           int width, int ratio,
                                           int proxy,
                                           int rotate, boolean flip, boolean flop
                                           )       throws IOException {
    
        // 出力ファイルの拡張子を変更する。
        if (debug) System.out.println("prepareOutImg("+srcFileName+","+outExtType+",..,"+width+")");

        //UserAgent ua = session.userAgent;

        CharArray ch = new CharArray(srcFileName).toLowerCase();
        
        //if (ua != null && ua.isAu()) {
        //    ch.replace('$','_');    // 文字変換
        //}
        
        boolean isGIF = ch.endsWith(".gif");

        CharArray outFileName = new CharArray(srcFileName);
        int lastPeriodIdx = outFileName.toString().lastIndexOf(".");
        outFileName.remove(lastPeriodIdx, outFileName.length - lastPeriodIdx);
        
        if (width == 0) {
            width = ua.width;   // 端末の幅を使用する
            if (ratio != 100 && ratio > 0) {
                width *= ratio;
                width /= 100;
            }
        }
        if (width > 0) outFileName.add("@"+width);
        
        // 
        int colorDepth = 8;
        if (ua != null) colorDepth = ua.colorDepth;
        if (isGIF && colorDepth > 8) colorDepth = 8;
        // 階調変換ロジックを入れる

        //ModuleManager mm = session.getModuleManager();
        if (mm != null && mm.imageBitMax > 0 && mm.imageBitMax < colorDepth) {
            colorDepth = mm.imageBitMax;
        }
//if (debug) System.out.println("imageBitMax="+mm.imageBitMax+" colorDepth="+ua.colorDepth+" -> "+colorDepth);
        if (colorDepth <= 8) {
            //outFileName.add("$"+colorDepth);
            outFileName.add("_"+colorDepth);
        }
        if (rotate > 0) {
            outFileName.add("r"+rotate);
        }
        if (flip) {
            outFileName.add("_R");
        }
        if (flop) {
            outFileName.add("_r");
        }
        outFileName.add(outExtType);


        // 入力ファイルがローカルホスト上かリモートホスト上かで入力、出力ファイル名を変更する。
        // 入力ファイルがローカルホスト上の場合は出力ファイルも同じディレクトリに作成される。
        // 入力ファイルがリモートホスト上の場合は以下の規則に従う。
        // "http://aaa.bbb.ccc/ddd/eee.xxx" --> rootPath + middlePath + "aaa.bbb.ccc/ddd/eee" + outExtType
        URL inURL = null;
        File inFile = null;
        
        String outFullPath;
        
        if (srcFileName.startsWith("http://")) {   // 入力ファイルがリモートホスト上の場合
            if (proxy == 1) {   // ImageProxy を使用する
                inFile = getRemoteImg(srcFileName);
            } else {
                inURL = new URL(srcFileName.toString());
            }
            outFileName.replace("http://", middlePath);  // "http://"をmiddlePathで置換する。
            outFullPath = rootPath + outFileName;
        } else {                                  // 入力ファイルがローカルホスト上の場合
            if (proxy == 1) {   // ImageProxy を使用する
                inFile = new File(((docRoot != null) ? docRoot :rootPath) + srcFileName);
            } else {
                inURL = new File(((docRoot != null) ? docRoot :rootPath) + srcFileName).toURL();
            }
            outFullPath = ((docRoot != null) ? docRoot :rootPath) + outFileName;  
        }

        if (debug) {
            if (proxy == 0) {
               System.out.println("入力画像ファイルURL  = " + inURL);
            } else {
                System.out.println("入力画像ファイルパス = " + inFile.getPath());
            }
        }
        File outFile = new File(outFullPath);
        if (debug) System.out.println("出力ファイルのパス  = " + outFile.getPath());

        if (!outFile.exists() || !outFile.isFile()) {   // ファイルがあれば出力しない
            boolean dirSuccess = true;
            // 保存先のディレクトリを作成する。
            if (!outFile.getParentFile().mkdirs()) {
                if (!outFile.getParentFile().exists()) {
                    if (debug) System.out.println("出力ディレクトリの作成失敗!");
                    dirSuccess = false;
                }
            }
            if (dirSuccess) {
                if (proxy == 0) {
                    if (debug) System.out.println("ImageProxy use JAI:"+proxy);
                    ImageConverter.convert(inURL, outFullPath, colorDepth, width);  //, rotate, flip, flop); JAI未対応
                } else {    // ImageAlchemy
                    if (debug) System.out.println("ImageProxy use ImageAlchemy:"+proxy);
                    ImageConverter.convert(inFile, outFile, colorDepth, width, rotate, flip, flop);
                }
            } else {
                outFileName = null;
            }
        } else {    // ファイルが存在する
            // タイムスタンプをチェック
            if (outFile.lastModified() < inFile.lastModified()) {
                if (debug) System.out.println("ファイルを再生成します");
                if (proxy == 0) {
                    if (debug) System.out.println("ImageProxy use JAI:"+proxy);
                    ImageConverter.convert(inURL, outFullPath, colorDepth, width);  //, rotate, flip, flop); JAI未対応
                } else {    // ImageAlchemy
                    if (debug) System.out.println("ImageProxy use ImageAlchemy:"+proxy);
                    ImageConverter.convert(inFile, outFile, colorDepth, width, rotate, flip, flop);
                }
            } else {
                if (debug) System.out.println("既存のファイルを出力");
            }
        }
        
        return outFileName;
    }   // end of prepareOutImg()

    // リモートホスト上の画像ファイルをローカルホスト上にコピーする。
    // コピー先のディレクトリは以下の規則に従う。
    // "http://aaa.bbb.ccc/ddd/eee.xxx" --> rootPath + middlePath + "aaa.bbb.ccc/ddd/eee.xxx"
    // 戻り値はコピーされたファイル。
    // すでに存在する場合はコピーされない。
    private static File getRemoteImg(CharArray inURLCh) throws IOException {
        if (debug) System.out.println("getRemoteImg("+inURLCh+")");
        URL inURL = new URL(inURLCh.toString());
        File file = new File(rootPath + middlePath + inURL.getHost() + inURL.getPath());
        // ローカルホスト上にファイルが存在しない場合はコピーする。
        if (!file.exists() || !file.isFile()) {
            // 保存先のディレクトリを作成する。
            if (!file.getParentFile().mkdirs()) {
                if (!file.getParentFile().exists()) {
                    System.out.println("コピー先ディレクトリの作成失敗");
                    return null;
                }
            }
            //DataInputStream inStream = new DataInputStream(inURL.openStream());
            InputStream inStream = inURL.openStream();
            FileOutputStream outStream = new FileOutputStream(file);
            int b;
            while ((b = inStream.read()) != -1) {
                //System.out.print(b);
                outStream.write(b);
            }
            // byte[]として読み込むと画像が変化してしまう。
            /**
            while (inStream.available() >= 1) {
                int size = inStream.available();
                byte[] buffer = new byte[size];
                //inStream.readFully(buffer,0,size);
                inStream.read(buffer);
                outStream.write(buffer);
            }
            **/
            inStream.close();
            outStream.close();
        }
        return file;
    }   // end of getRemoteImg()
}

//
//
// [end of ImageProxy.java]
//
