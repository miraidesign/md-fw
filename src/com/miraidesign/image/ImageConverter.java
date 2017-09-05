//------------------------------------------------------------------------
// @(#)ImageConverter.java
//          JAIを利用した画像コンバーター
//          Copyright (c) Mirai design. 2010 All Rights Reserved.
//------------------------------------------------------------------------
package com.miraidesign.image;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.ImageLayout;
import javax.media.jai.RenderedOp;

import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.awt.color.ColorSpace;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.StreamAbsorber;

/**
 * 画像コンバーター(JAI)
 *  
 *  @version 0.5 
 *  @author toru ishioka
 *  @since  JDK1.1
*/

public class ImageConverter {
    private static boolean debug = (true & SystemConst.debug);
    private static boolean debugProcess = (false & SystemConst.debug);
    private static String szCommand  = "/usr/local/bin/image_proxy.sh"; // 引数の並び順の違い等はシェルで吸収する

    private static String optJpeg = "";
    private static String optBmp  = "";
    private static String optPng  = "";
    private static String optGif  = "-colors 32";

    private static String optWidth   = "-resize";   // 幅指定オプション
    private static String optGray  = "";            // グレースケール設定オプション
    private static String optRotate  = "-rotate";   // 回転指定オプション
    private static String optFlip  = "-flip";       // 左右反転指定オプション
    private static String optFlop  = "-flop";       // 上下反転指定オプション
    
    private static Runtime runtime;
    
    // コマンド設定
    public static void setCommand(String str) { szCommand = str; }
    // JPEG オプション
    public static void setJpegOption(String str) { optJpeg = str; }
    //  BMP オプション
    public static void setBmpOption(String str) { optBmp = str; }
    //  PNG オプション
    public static void setPngOption(String str) { optPng = str; }
    //  GIF オプション
    public static void setGifOption(String str) { optGif = str; }
    
    //**  Width オプション */
    public static void setWidthOption(String str) { optWidth = str; }
    //**  Gray オプション */
    public static void setGrayOption(String str) { optGray = str; }
    //**  回転オプション */
    public static void setRotateOption(String str) { optRotate = str; }
    //**  上下反転オプション */
    public static void setFlipOption(String str) { optFlip = str; }
    //**  左右反転オプション */
    public static void setFlopOption(String str) { optFlop = str; }
    
    //** コンソールデバッグ表示オンオフ */
    public static void setDebug(boolean mode) { debug = mode; }
    
    /**
        画像コンバート
        @param inURL            入力URL
        @param outFilename      出力ファイル名
        @param depth            カラービット数
        @param width            0:do nothing else:指定サイズ出力
        @throws IOException IOException
        @return true 成功
    */
    static public boolean convert(URL inURL, 
                               String outFilename,
                               int    depth,               // カラービット数
                               int    width                // 0:do nothing else:指定サイズ出力
                               ) throws IOException
    {
        return convert(inURL, outFilename, depth, width, 0, false,false);
    }
    /**
        画像コンバート
        @param inURL            入力URL
        @param outFilename      出力ファイル名
        @param depth            カラービット数
        @param width            0:do nothing else:指定サイズ出力
        @param rotate           0:do notiong else 指定角度回転（対応ツールのみ）
        @throws IOException IOException
        @return true 成功
    */
    static public boolean convert(URL inURL, 
                               String outFilename,
                               int    depth,               // カラービット数
                               int    width,                // 0:do nothing else:指定サイズ出力
                               int    rotate         // 0:do notiong else 指定角度回転（対応ツールのみ）
                               ) throws IOException
    {
        return convert(inURL, outFilename, depth, width, rotate, false,false);
    }
    /**
        画像コンバート
        @param inURL            入力URL
        @param outFilename      出力ファイル名
        @param depth            カラービット数
        @param width            0:do nothing else:指定サイズ出力
        @param rotate           0:do notiong else 指定角度回転（対応ツールのみ）
        @param flip             true:上下反転（対応ツールのみ）
        @param flop             true:左右反転（対応ツールのみ）
        @throws IOException IOException
        @return true 成功
    */
    
    static public boolean convert(URL inURL, 
                               String outFilename,
                               int    depth,               // カラービット数
                               int    width,          // 0:do nothing else:指定サイズ出力
                               int    rotate,         // 0:do notiong else 指定角度回転（対応ツールのみ）
                               boolean flip,         // true:上下反転（対応ツールのみ）
                               boolean flop          // true:左右反転（対応ツールのみ）
                               ) throws IOException
    {
        if (debug) System.out.println("ImageConverter.convert(URL)の実行");
        
        String szType =  null;
        String str = outFilename.toLowerCase();
        if (str.endsWith(".jpeg") || str.endsWith(".jpg")) szType = "JPEG";
        else if (str.endsWith(".bmp"))                     szType = "BMP";
        else if (str.endsWith(".png"))                     szType = "PNG";
        
        if (szType == null) return false;
        
        RenderedOp src = JAI.create("url", inURL);
        RenderedOp dst = src;
        
        if (depth > 0 && depth <= 4) {      // グレースケール変換
            if (debug) System.out.println("ImageConverter.convert() グレースケール変換開始");

            ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            int[] bits = {depth};
            ColorModel colorModel = 
                new ComponentColorModel(colorSpace, bits, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

            ParameterBlock pb = new ParameterBlock();
            pb.addSource(src).add(colorModel);

            ImageLayout layout = new ImageLayout();
            layout.setColorModel(colorModel);
            RenderingHints rh = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);

            dst = JAI.create("ColorConvert", pb, rh);
        }
        if (width > 0) {                    // サイズ変換
            if (debug) System.out.println("ImageConverter.convert() サイズ変換開始");
            src = dst;
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(src);
            
            RenderableImage ren = JAI.createRenderable("renderable", pb);
            int w = src.getWidth();
            int h = src.getHeight() * width / w;
            RenderedImage dst2 = ren.createScaledRendering(width, h, null);
            
            JAI.create("filestore", dst2, outFilename, szType, null); //@@\\
        } else {
            if (debug) System.out.println("ImageConverter.convert() 変換開始");
            JAI.create("filestore", dst, outFilename, szType, null);
        }
        src.removeSource(inURL);
        return true;
    }

    /**
        画像コンバート by 外部ツール （ImageMagick、ImageAlchemy等）
        @param inFile       入力ファイル
        @param outFile      出力ファイル
        @param depth        カラービット数
        @param width        幅設定：0:do nothing else:指定サイズ出力
        @throws IOException IOException
        @return true:で成功
    */
    static public boolean convert(File inFile, 
                                  File outFile,
                                  int    depth,        // カラービット数
                                  int    width         // 0:do nothing else:指定サイズ出力
                               ) throws IOException
    {
        return convert(inFile, outFile, depth, width, 0, false, false);
    }
    /**
        画像コンバート by 外部ツール （ImageMagick、ImageAlchemy等）
        @param inFile       入力ファイル
        @param outFile      出力ファイル
        @param depth        カラービット数
        @param width        幅設定：0:do nothing else:指定サイズ出力
        @param rotate       回転設定：0:do notiong else 指定角度回転（対応ツールのみ）
        @throws IOException IOException
        @return true:で成功
    */
    static public boolean convert(File inFile, 
                                  File outFile,
                                  int    depth,        // カラービット数
                                  int    width,        // 0:do nothing else:指定サイズ出力
                                  int   rotate          // 0:do notiong else 指定角度回転（対応ツールのみ）
                               ) throws IOException
    {
        return convert(inFile, outFile, depth, width, rotate, false, false);
    }
    /**
        画像コンバート by 外部ツール （ImageMagick、ImageAlchemy等）
        @param inFile       入力ファイル
        @param outFile      出力ファイル
        @param depth        カラービット数
        @param width        幅設定：0:do nothing else:指定サイズ出力
        @param rotate       回転設定：0:do notiong else 指定角度回転（対応ツールのみ）
        @param flip         true:上下反転（対応ツールのみ）
        @param flop         true:左右反転（対応ツールのみ）
        @throws IOException IOException
        @return true:で成功
    */
    static public boolean convert(File inFile, 
                                  File outFile,
                                  int    depth,        // カラービット数
                                  int    width,         // 0:do nothing else:指定サイズ出力
                                  int   rotate,         // 0:do notiong else 指定角度回転（対応ツールのみ）
                                  boolean flip,         // true:上下反転（対応ツールのみ）
                                  boolean flop          // true:左右反転（対応ツールのみ）
                               ) throws IOException
    {
        if (debug) {
            System.out.println("ImageConverter.convert("+inFile.getPath()+","+outFile.getPath()+
                                ","+depth+
                                ","+width+
                                ","+rotate+
                                ","+flip+
                                ","+flop+
                            ")");
        }
        CharArray chOption = new CharArray();
        String str = outFile.getPath().toLowerCase();
        
        if (str.endsWith(".jpeg") || str.endsWith(".jpg")) chOption.set(optJpeg);
        else if (str.endsWith(".bmp"))                     chOption.set(optBmp);
        else if (str.endsWith(".png"))                     chOption.set(optPng);
        else if (str.endsWith(".gif"))                     chOption.set(optGif);
        else return false;
        
        // グレースケール変換
        if (depth <= 4) {
            chOption.add(" "); chOption.add(optGray);
        } else if (str.endsWith(".png")) {
            //chOption.add(" -8");    // png のみ強制８ビット出力
            //                           やめる
        }
        if (flip && optFlip.length() > 0) { // 左右反転
            chOption.add(" "); chOption.add(optFlip); 
        }
        if (flop && optFlop.length() > 0) { // 上下反転
            chOption.add(" "); chOption.add(optFlop); 
        }
        if (rotate > 0 && optRotate.length()>0) { // 回転（右回りに度で設定）
            chOption.add(" "); chOption.add(optRotate); 
            chOption.add(" "); //
            chOption.format(rotate);
        }
        if (width > 0) { // サイズ変換
            chOption.add(" "); chOption.add(optWidth); 
            chOption.add(" "); //
            chOption.format(width);
        }
        
        //  実行
        if (runtime == null) runtime = Runtime.getRuntime();
        
        CharToken token = CharToken.pop();
        token.set(chOption," ");
        
        String [] strs = new String[4+token.size()];
        strs[0] = szCommand;
        strs[1] = inFile.getParentFile().getPath();
        strs[2] = inFile.getName();
        strs[3] = outFile.getName();
        for (int i = 0; i < token.size(); i++) {
            strs[4+i] = token.get(i).toString();
        }
        
        CharToken.push(token);
        
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
                if (debug) {
                    for (int i = 0; i < strs.length; i++) {
                        System.out.print("  "+strs[i]);
                    }
                }
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
// [end of ImageConverter.java]
//
