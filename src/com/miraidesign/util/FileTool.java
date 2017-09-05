//------------------------------------------------------------------------
//    FileTool.java
//              ファイルユーティリティ
//              Copyright (c) MiraiDesign 2010-15 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.util;

                
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/** ファイルユーティリティ */
public class FileTool {
    private static boolean debug = true;
    private static SimpleDateFormat sdfForXcopy = new SimpleDateFormat("MM-dd-yyyy");

    private static final String[][] CONVERT_SHELL_META_CHAR = new String[][] {
        {"\\", "\\\\"},
        {"(",  "\\("},
        {")",  "\\)"},
        {"!",  "\\!"},
        {"\'", "\\\'"},
        {"\"", "\\\""},
        {"`",  "\\`"},
        {"*",  "\\*"},
        {"|",  "\\|"},
        {"~",  "\\~"},
        {"?",  "\\?"},
        {"$",  "\\$"},
        {"<",  "\\<"},
        {">",  "\\>"},
        {"&",  "\\&"},
        {"[",  "\\["},
        {"]",  "\\]"},
        {"{",  "\\{"},
        {"}",  "\\}"},
        {" ",  "\\ "},
    };
    
    private static CharArray replaceShellMetaChar(CharArray ch) {
        for (int i=0; i<CONVERT_SHELL_META_CHAR.length; i++) 
            ch.replace(CONVERT_SHELL_META_CHAR[i][0], CONVERT_SHELL_META_CHAR[i][1]);
        return ch;
    }

    /**
     * ディレクトリをコピーします。(外部コマンドを使います。)
     * 
     * ディレクトリtoが無い場合は新しく生成されます。
     * 
     * @param from コピーするディレクトリ
     * @param to コピー先ディレクトリ (to以下にfrom以下のオブジェクトがコピーされます。)
     * @return コピーに成功した場合はtrue, from以下にファイルが無い場合も true, 失敗した場合はfalse
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean copyDir(File from, File to) 
            throws IOException, InterruptedException {
        return copyDir(from, to, (UserLog)null, true, true);
    }
    public static boolean copyDir(File from, File to, UserLog log)
            throws IOException, InterruptedException {
        return copyDir(from, to, log, true, true);
    }
    public static boolean copyDir(File from, File to, UserLog log, boolean checkDate, boolean isDirectory)
            throws IOException, InterruptedException {
        if (from == null || to == null) return false;
        if (isDirectory) {
            if(!from.isDirectory()) return false;
            if(from.list().length == 0) return true;
            if (to.exists()) {
                if(!to.isDirectory()) return false;
            } else if(!to.mkdirs()) {
                return false;
            }
        }
        CharArray fromCA = CharArray.pop();
        CharArray toCA = CharArray.pop();
        InputStream in = null;
        InputStream ein = null;
        try {
            Process process = null;
            fromCA.set(from.getAbsolutePath());
            toCA.set(to.getAbsolutePath());
            if (isDirectory) {
                fromCA.replace("\\", "\\\\"); // この置換を入れないと、\が抜けてしまう
                toCA.replace("\\", "\\\\");
            }
            if(System.getProperty("os.name").startsWith("Windows")) {
                ArrayList<String> strL = new ArrayList<String>();
                strL.add("xcopy");
                strL.add(fromCA.toString());
                strL.add(toCA.toString());
                strL.add("/s");
                strL.add("/k");
                strL.add("/r");
                strL.add("/e");
                strL.add("/h");
                strL.add("/y");
                strL.add("/q");
                if (checkDate) {
                    strL.add("/d");
                }
                String[] strs = (String[]) strL.toArray(new String[0]);
                process = Runtime.getRuntime().exec(strs);
            } else {
                if (!fromCA.endsWith(File.separator)) fromCA.add(File.separator);
                fromCA.add("*");
                String cpStr = "cp -rp";
                if (checkDate) {
                    cpStr = cpStr + "u";
                }
                cpStr = cpStr + " " + fromCA.toString() + " " + toCA.toString();
                String strs[] = { 
                        "sh",
                        "-c",
                        cpStr
                };
                process = Runtime.getRuntime().exec(strs);
            }
            
            in  = process.getInputStream();
            ein = process.getErrorStream(); 
            
            StreamAbsorber stdinAbsorber = new StreamAbsorber(in, "copyDir STD:");
            StreamAbsorber errorAbsorber = new StreamAbsorber(ein,"copyDir ERR:");
            stdinAbsorber.start();
            errorAbsorber.start();
            
            int sts = process.waitFor(); 

            while (stdinAbsorber.isAlive()) {}
            while (errorAbsorber.isAlive()) {}
            
            if(sts != 0) {
                if (log == null) {
                    System.out.println(stdinAbsorber.getMessage());
                    System.out.println(errorAbsorber.getMessage());
                } else {
                    log.error(stdinAbsorber.getMessage().toString());
                    log.error(errorAbsorber.getMessage().toString());
                }
                return false;
            }
        } finally {
            if (in != null) in.close();
            if (ein != null) ein.close();
            CharArray.push(fromCA);
            CharArray.push(toCA);
        }
        return true;
    }

    /**
     * ディレクトリをコピーします。(外部コマンドを使います。)
     * 
     * ディレクトリtoが無い場合は新しく生成されます。
     * 
     * @param from コピーするディレクトリ
     * @param to コピー先ディレクトリ (to以下にfrom以下のオブジェクトがコピーされます。)
     * @param fromDate 指定した日付以後に変更されたコピー元ファイルだけをコピーします。
     * @return コピーに成功した場合はtrue, 失敗した場合はfalse
     * @throws InterruptedException
     * @throws IOException
     */
    public static boolean copyDir(File from, File to, Date fromDate)
            throws InterruptedException, IOException {
        return copyDir(from, to, fromDate, null);
    }
    public static boolean copyDir(File from, File to, Date fromDate, UserLog log) 
            throws InterruptedException, IOException {
        if (from == null || !from.isDirectory() || to == null) return false;
        if (to.exists()) {
            if(!to.isDirectory()) return false;
        } else if (!to.mkdirs()) {
            return false;
        } else if (fromDate == null) {
            return false;
        }
        CharArray fromCA = CharArray.pop();
        CharArray toCA = CharArray.pop();
        InputStream in = null;
        InputStream ein = null;
        try {
            Process process = null;
            fromCA.set(from.getAbsolutePath());
            toCA.set(to.getAbsolutePath());
            fromCA.replace("\\", "\\\\"); // この置換を入れないと、\が抜けてしまう
            toCA.replace("\\", "\\\\");
            if(System.getProperty("os.name").startsWith("Windows")) {
                String formatDate;
                synchronized(sdfForXcopy) {
                    formatDate = sdfForXcopy.format(fromDate);
                }
                String strs[] = {
                        "xcopy",
                        fromCA.toString(),
                        toCA.toString(),
                        "/s",
                        "/k",
                        "/r",
                        "/e",
                        "/h",
                        "/y",
                        "/q",
                        "/d:" + formatDate
                };
                process = Runtime.getRuntime().exec(strs);
            } else {
                long d = (System.currentTimeMillis() - fromDate.getTime()) / 
                         (1000 * 60 * 60 * 24);
                
                fromCA.add(File.separator);
                toCA.replace("\\", "\\\\");
                String strs[] = { // http://www.gimlay.org/~javafaq/S103.html
                        "find",
                        fromCA.toString(),
                        "-mtime",
                        "-" + (d+1), // d日以内に更新されているファイルのみコピー
                        "-type",
                        "f",
                        "-exec",
                        "cp",
                        "-pu",
                        "{}",
                        toCA.toString(),
                        ";"
                };
                process = Runtime.getRuntime().exec(strs);
            }
            in  = process.getInputStream();
            ein = process.getErrorStream(); 
            
            StreamAbsorber stdinAbsorber = new StreamAbsorber(in, "copyDir STD:");
            StreamAbsorber errorAbsorber = new StreamAbsorber(ein,"copyDir ERR:");
            stdinAbsorber.start();
            errorAbsorber.start();
            
            int sts = process.waitFor(); 

            while (stdinAbsorber.isAlive()) {}
            while (errorAbsorber.isAlive()) {}
            
            if(sts != 0) {
                if (log == null) {
                    System.out.println(stdinAbsorber.getMessage());
                    System.out.println(errorAbsorber.getMessage());
                } else {
                    log.error(stdinAbsorber.getMessage().toString());
                    log.error(errorAbsorber.getMessage().toString());
                }
                return false;
            }
        } finally {
            if (in != null) in.close();
            if (ein != null) ein.close();
            CharArray.push(fromCA);
            CharArray.push(toCA);
        }
        return true;
    }


    /**
     * ファイルをコピーします。(外部コマンドを使います。)
     * 
     * @param file コピーするファイル
     * @param dir コピー先ディレクトリ (ディレクトリtoが無い場合は新しく生成されます。)
     * @return コピーに成功した場合はtrue, 失敗した場合はfalse
     * @throws InterruptedException
     * @throws IOException
     */
    public static boolean copyFile(File file, File dir) 
            throws InterruptedException, IOException {
        return copyFile(file, dir, (UserLog)null);
    }
    public static boolean copyFile(File file, File dir, UserLog log) 
            throws InterruptedException, IOException {
        if (file == null || dir == null) return false;
        if (file.getName().indexOf("*") == -1 && !file.isFile()) return false;
        if (dir.exists()) {
            if(!dir.isDirectory()) return false;
        } else if(!dir.mkdirs()) {
            return false;
        }

        CharArray fileCA = CharArray.pop();
        CharArray dirCA = CharArray.pop();
        InputStream in = null;
        InputStream ein = null;
        try {
            Process process = null;
            fileCA.set(file.getAbsolutePath());
            dirCA.set(dir.getAbsolutePath());
            if(System.getProperty("os.name").startsWith("Windows")) {
                String strs[] = {
                        "xcopy",
                        fileCA.toString(),
                        dirCA.toString(),
                        "/q", "/d", "/k", "/r", "/h", "/y"
                };
                process = Runtime.getRuntime().exec(strs);
            } else {
                if (file.isFile()) 
                    replaceShellMetaChar(fileCA);
                String strs[] = {
                        "sh",
                        "-c",
                        "cp -pu " + fileCA.toString() + " " + dirCA.toString()
                };
                process = Runtime.getRuntime().exec(strs);
            }
            
            in  = process.getInputStream();
            ein = process.getErrorStream();

            StreamAbsorber stdinAbsorber = new StreamAbsorber(in, "copyFile STD:");
            StreamAbsorber errorAbsorber = new StreamAbsorber(ein,"copyFile ERR:");
            stdinAbsorber.start();
            errorAbsorber.start();

            int sts = process.waitFor();

            while (stdinAbsorber.isAlive()) {}
            while (errorAbsorber.isAlive()) {}

            if(sts != 0) {
                if (log == null) {
                    System.out.println(stdinAbsorber.getMessage());
                    System.out.println(errorAbsorber.getMessage());
                } else {
                    log.error(stdinAbsorber.getMessage().toString());
                    log.error(errorAbsorber.getMessage().toString());
                }
                return false;
            }
        } finally {
            if (in != null) in.close();
            if (ein != null) ein.close();
            CharArray.push(fileCA);
            CharArray.push(dirCA);
        }
        return true;
    }

    /**
     * ファイルをコピーするとともにリネームも行う。(外部コマンドを使います。)
     * 
     * @param file コピーするファイル
     * @param dir コピー先ディレクトリ (ディレクトリtoが無い場合は新しく生成されます。)
     * @param to  転送先でのファイル名
     * @return コピーに成功した場合はtrue, 失敗した場合はfalse
     * @throws InterruptedException
     * @throws IOException
     */
    public static boolean copyFile(File file, File dir, File to) 
            throws InterruptedException, IOException {
        return copyFile(file, dir, to, null);
    }
    public static boolean copyFile(File file, File dir, File to, UserLog log) 
            throws InterruptedException, IOException {
        if (file == null || dir == null || to == null) return false;
        if (file.getName().indexOf("*") == -1 && !file.isFile()) return false;
        if (dir.exists()) {
            if(!dir.isDirectory()) return false;
        } else if(!dir.mkdirs()) {
            return false;
        }

        CharArray fileCA = CharArray.pop();
        CharArray toCA = CharArray.pop();
        InputStream in = null;
        InputStream ein = null;
        try {
            Process process = null;
            fileCA.set(file.getAbsolutePath());
            toCA.set(to.getAbsolutePath());
            if (System.getProperty("os.name").startsWith("Windows")) {
                String strs[] = {
                        "cmd.exe",
                        "/C",
                        "copy /y "+fileCA+" "+toCA
                        //"/q", "/d", "/k", "/r", "/h", "/y"
                };
                process = Runtime.getRuntime().exec(strs);
            } else {
                if (file.isFile()) 
                    replaceShellMetaChar(fileCA);
                String strs[] = {
                        "sh",
                        "-c",
                        "cp -p " + fileCA.toString() + " " + toCA.toString()
                };
                process = Runtime.getRuntime().exec(strs);
            }
            in  = process.getInputStream();
            ein = process.getErrorStream();

            StreamAbsorber stdinAbsorber = new StreamAbsorber(in, "copyFile STD:");
            StreamAbsorber errorAbsorber = new StreamAbsorber(ein,"copyFile ERR:");
            stdinAbsorber.start();
            errorAbsorber.start();

            int sts = process.waitFor();

            while (stdinAbsorber.isAlive()) {}
            while (errorAbsorber.isAlive()) {}
            if (sts != 0) {
                if (log == null) {
                    System.out.println(stdinAbsorber.getMessage());
                    System.out.println(errorAbsorber.getMessage());
                } else {
                    log.error(stdinAbsorber.getMessage().toString());
                    log.error(errorAbsorber.getMessage().toString());
                }
                return false;
            }
        } finally {
            if (in != null) in.close();
            if (ein != null) ein.close();
            CharArray.push(toCA);
            CharArray.push(fileCA);
        }
        return true;
    }

    /**
     * ファイルをコピーします。(外部コマンドを使います。)
     * 
     * @param name コピーするファイル名のリスト
     * @param fromDir コピー元ディレクトリ
     * @param toDir コピー先ディレクトリ (無い場合は新しく生成されます。)
     * @return コピーに成功した場合はtrue, 失敗した場合はfalse
     * @throws InterruptedException
     * @throws IOException
     */
    public static boolean copyFile(String[] name, File fromDir, File toDir) 
            throws InterruptedException, IOException {
        return copyFile(name, fromDir, toDir, (UserLog)null);
    }
    /**
     * ファイルをコピーします。(外部コマンドを使います。)
     * 
     * @param name コピーするファイル名のリスト
     * @param fromDir コピー元ディレクトリ
     * @param toDir コピー先ディレクトリ (無い場合は新しく生成されます。)
     * @param log
     * @return コピーに成功した場合はtrue, 失敗した場合はfalse
     * @throws InterruptedException
     * @throws IOException
     */
    public static boolean copyFile(String[] name, File fromDir, File toDir, UserLog log) 
            throws InterruptedException, IOException {
        if (name == null || fromDir == null || 
            !fromDir.isDirectory() || toDir == null) {
                if (!fromDir.isDirectory()) {
                    System.out.println("copyFile:fromDir not directory:"+fromDir);
                } else {
                    System.out.println("copyFile:parameter error");
                }
                return false;
        }
        if (toDir.exists()) {
            if (!toDir.isDirectory()) {
                System.out.println("copyFile:toDir not directory:"+toDir);
                return false;
            }
        } else if (!toDir.mkdirs()) {
                System.out.println("copyFile:cann not make directory:"+toDir);
            return false;
        }
        if (System.getProperty("os.name").startsWith("Windows")) {
            for (int i=0; i < name.length; i++) {
                if (name[i].length() == 0) continue;
                File file = new File(fromDir, name[i]);
                if (!copyFile(file, toDir, log)) {
                    if (log != null) {
                        log.error(file.getAbsolutePath() + "をコピーできません。[Win]");
                    } else {
                        System.out.println(file.getAbsolutePath() + "をコピーできません。[Win]");
                    
                    }
                    return false;
                }
            }
            return true;
        }

        CharArray filesCA = CharArray.pop();    filesCA.clear();
        CharArray toDirCA = CharArray.pop();
        InputStream in = null;
        InputStream ein = null;
        try {
            Process process = null;
            for (int i = 0; i < name.length; i++) {
                if (name[i] == null || name[i].trim().length() == 0) continue;
                File file = new File(fromDir, name[i]);
                if (file.isFile()) {
                    CharArray ch = CharArray.pop(); ch.clear();
                    ch.set(name[i]);
                    filesCA.add(" ").add(replaceShellMetaChar(ch));
                    CharArray.push(ch);
                } else {
                    if (name[i].indexOf("*") > 0) {
                        filesCA.add(" ").add(name[i]);
                    } else {
                        return false;
                    }
                }
            }
            toDirCA.set(toDir.getAbsolutePath());
            filesCA.trim(); toDirCA.trim();
            String strs[] = {
                    "sh",
                    "-c",
                    "cp -pu \"" + filesCA.toString() + "\" \"" + toDirCA.toString()+"\""
            };
if (debug) {
    System.out.println(""+fromDir+">cp -pu \"" + filesCA.toString() + "\" \"" + toDirCA.toString()+"\"");
}            
            process = Runtime.getRuntime().exec(strs, null, fromDir);

            in  = process.getInputStream();
            ein = process.getErrorStream();

            StreamAbsorber stdinAbsorber = new StreamAbsorber(in, "copyFile STD:");
            StreamAbsorber errorAbsorber = new StreamAbsorber(ein,"copyFile ERR:");
    
            stdinAbsorber.start();
            errorAbsorber.start();

            int sts = process.waitFor();

            while (stdinAbsorber.isAlive()) {}
            while (errorAbsorber.isAlive()) {}

            if (sts != 0) {
                if (log == null) {
                    System.out.println(stdinAbsorber.getMessage());
                    System.out.println(errorAbsorber.getMessage());
                } else {
                    log.error(stdinAbsorber.getMessage().toString());
                    log.error(errorAbsorber.getMessage().toString());
                }
                return false;
            }
        } finally {
            if (in  != null) in.close();
            if (ein != null) ein.close();
            CharArray.push(filesCA);
            CharArray.push(toDirCA);
        }
        return true;
    }
    
    public static boolean copyFileNio(String from, String to) /*throws IOException*/ {
         return copyFileNio(new File(from), new File(to));
    }
    
   /**
     * New I/O を使ってファイルをコピーします。
     * 
     * @param from コピー元(ディレクトリ、ファイルいずれも可)
     * @param to  コピー先(ディレクトリ、ファイルいずれも可)
     * @see FileChannel
     */
    public static boolean copyFileNio(File from, File to) /*throws IOException*/ {
        //if (from == null) throw new IOException("コピー元がnullです。");
        //if (to == null) throw new IOException("コピー先がnullです。");
        boolean status = false;
        if (from == null || !from.exists()) {
            String str = from == null ? "(null)" : from.getAbsolutePath();
            //throw new FileNotFoundException ("コピー元 " + str +" が存在しません。");
            System.out.println("コピー元 " + str + " が存在しません。");
            return false;
        } else if (from.isDirectory()) {
            if (to == null || (!to.isDirectory() && !to.mkdirs())) {
                String str = (to == null) ? "(null)" : to.getAbsolutePath();
                //throw new IOException("コピー先 " + str+" のディレクトリを作成できません。");
                System.out.println("コピー先 " + str+" のディレクトリを作成できません。");
                return false;
            }
            File[] fromList = from.listFiles();
            for (int i=0; i<fromList.length; i++) {
                File destFile = new File(to, fromList[i].getName());
                status = copyFileNio (fromList[i], destFile);
                if (!status) break;
            }
            return status;
        } else if (to.isDirectory()) {
            // from は普通のファイル
            File destFile = new File(to, from.getName());
            status = copyFileNio (from, destFile);
            return status;
        } else if (!to.exists()) {
            File destParent = to.getParentFile();
            if (!destParent.isDirectory() && !destParent.mkdirs()) {
                //throw new IOException("コピー先 " + destParent.getAbsolutePath()+" のディレクトリを作成できません。");
                System.out.println("コピー先 " + destParent.getAbsolutePath()+" のディレクトリを作成できません。");
                return false;
            }
        }
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            // チャネルのトランスファ
            // 読みこみのためのチャネルの出力を
            // 書きこみのためのチャネルの入力にトランスファする
            fromChannel = new FileInputStream(from).getChannel();
            toChannel = new FileOutputStream(to).getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
            if (to != null && to.exists() && to.length() > 0) {
                status = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status = false;
        } finally {
            try {
                if (fromChannel != null) fromChannel.close();
                if (toChannel   != null) toChannel.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return status;
    }

    static final int BUF_SIZE = 8192;
    public static boolean copyFile(URL url, File to)  {
        boolean status = false;
        do {
            if (url == null || to == null) break;
            
            if (!to.exists()) {
                File destParent = to.getParentFile();
                if (!destParent.isDirectory() && !destParent.mkdirs()) {
                    System.out.println("コピー先 " + destParent.getAbsolutePath()+" のディレクトリを作成できません。");
                    break;
                }
            }
            
            BufferedInputStream  in  = null;
            FileOutputStream     out = null;
            try {
                URLConnection conn = url.openConnection();
                conn.setDoInput(true);
                in  = new BufferedInputStream(conn.getInputStream());
                out = new FileOutputStream(to);
                byte[] b = new byte[BUF_SIZE];
                do {
                    int size = in.read(b,0,BUF_SIZE);
                    if (size < 0) break;
                    out.write(b,0,size);
                } while (true);
                status = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (in  != null) in.close();
                    if (out != null) out.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } while (false);
        return status;
    }

    /** 
        ファイルまたはディレクトリの存在チェック
        @param str   ファイル名またはディレクトリ名
        @return true: 成功
    */
    static public boolean exists(String str) {
        File file = new File(str);
        boolean sts = file.exists();
        //System.out.println("can't found:"+str);
        return sts;
    }
    /** 
        ディレクトリを作成する 
        @param dir   ディレクトリ名
        @return true: 成功
    */
    static public boolean mkdirs(String dir) {
        boolean sts = false;
        try {
            File file = new File(dir);
            file.mkdirs();
            sts = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println("can't make directory:"+dir);
        return sts;
    }
    /** 
        ファイル名を含むフルパスからディレクトリを作成する 
        @param path   ファイル名を含むフルパス
        @return true: 成功
    */
    static public boolean mkdirsFromFilename(String path) {
        CharArray ch = CharArray.pop(path);
        ch.replace('\\', '/');
        int index = ch.lastIndexOf('/');
        if (index > 0) ch.length = 4;
        String dir = ch.toString();
        CharArray.push(ch);
        return mkdirs(dir);
    }
    
    
    
    
    
    /**
      ファイル比較ツール<br>
      コンソールに最大32個までの違いを表示する
      @param filename1   比較元ファイル名
      @param filename2   比較対象ファイル名
      @return true: 同一
    */
    static public boolean fileCheck(String filename1, String filename2) {
        boolean sts = false;
        do {
            try {
                FileChannel src  = new FileInputStream(filename1).getChannel();
                int size = (int)src.size();
                if (size <= 0) break;
                ByteBuffer buf1 = ByteBuffer.allocateDirect(size);
                size = src.read(buf1);
                if (size <= 0) break;
            
                src  = new FileInputStream(filename2).getChannel();
                size = (int)src.size();
                if (size <= 0) break;
                ByteBuffer buf2 = ByteBuffer.allocateDirect(size);
                size = src.read(buf2);
                if (size <= 0) break;
                sts = fileCheck(buf1, buf2);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } while (false);
        return sts;
    }
    /**
      ファイル比較ツール<br>
      コンソールに最大32個までの違いを表示する
      @param buf1   比較元ファイルを読み込んだバッファ
      @param buf2   比較対象ファイルを読み込んだバッファ
      @return true: 同一
    */
    static public boolean fileCheck(ByteBuffer buf1, ByteBuffer buf2) {
        boolean sts = false;
        do {
            if (buf1 == null || buf2 == null) {
                break;
            }
            long size1 = buf1.position();
            long size2 = buf2.position();
            if (size1 != size2) {
                System.out.println("ファイルサイズが違います:"+size1+"/"+size2+"   ");
            } else {
                System.out.println("ファイルサイズ::"+size1+"/"+size2+"   ");
            }
            long size = Math.min(size1, size2);
            int ERROR_MAX = 32;
            buf1.position(0);
            buf2.position(0);
            CharArray ch = new CharArray();
            int count = 0;
            for (long l = 0; l < size; l++) {
                int i1 = buf1.get() & 0xff;
                int i2 = buf2.get() & 0xff;
                if (i1 != i2) {
                    ch.set("["); ch.format(l,10,6,'0'); ch.add("] ");
                    ch.format(i1, 16,2,'0'); ch.add(" ");
                    ch.format(i2, 16,2,'0');
                    System.out.println(ch);
                    if (++count >= ERROR_MAX) {
                        System.out.println("-- dump 中断 --");
                        break;
                    }
                }
            }
            if (count == 0 && size1 == size2) sts = true;
        } while (false);
        return sts;
    }
}

//
//
// [end of FileTool.java]
//

