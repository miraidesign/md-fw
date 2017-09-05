//------------------------------------------------------------------------
//    Util.java
//                 ユーティリティクラス
//
//          Copyright (c) Mirai Design Institute 2010-13 All rights reserved.
//          update 2010-02-17   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.File;
import java.util.Date;
import java.util.Calendar;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** 
  ユーティリティクラス
**/
public class Util {

    //---------------------------------------------------------------------
    // 最小最大範囲に押さえる
    //---------------------------------------------------------------------
    public static int MinMax(int min,int num,int max) {  
        return Math.min(Math.max(min,num),max);
    }
    public static long MinMax(long min,long num,long max) {
        return Math.min(Math.max(min,num),max);
    }
    
    public static double MinMax(double min,double num,double max) {
        return Math.min(Math.max(min,num),max);
    }
    
    //---------------------------------------------------------------------
    // 桁位置固定フォーマット表示メソッド
    //---------------------------------------------------------------------
    private static final String[] szSpace = { "",               //  0
                                              " ",              //  1
                                              "  ",             //  2
                                              "   ",            //  3
                                              "    ",           //  4
                                              "     ",          //  5
                                              "      ",         //  6
                                              "       ",        //  7
                                              "        ",       //  8
                                              "         ",      //  9
                                              "          ",     // 10
                                              "           ",    // 11
                                              "            ",   // 12
                                              "             ",   // 13
                                              "              ",   // 14
                                              "               ",   // 15
                                              "                ",   // 16
                                              "                 ",   // 17
                                              "                  ",   // 18
                                              "                   ",   // 19
                                              "                    ",   // 20
                                             };
    private static final int MAX_SPACE = 20;
    /**
    private static StringBuffer[] szSpaceBuf = {
            new StringBuffer(""),               //  0
            new StringBuffer(" "),              //  1
            new StringBuffer("  "),             //  2
            new StringBuffer("   "),            //  3
            new StringBuffer("    "),           //  4
            new StringBuffer("     "),          //  5
            new StringBuffer("      "),         //  6
            new StringBuffer("       "),        //  7
            new StringBuffer("        "),       //  8
            new StringBuffer("         "),      //  9
            new StringBuffer("          "),     // 10
            new StringBuffer("           "),    // 11
            new StringBuffer("            "),   // 12
    };
    **/
    
    private static final DecimalFormat if0 = new DecimalFormat("#");
    private static final DecimalFormat if1 = new DecimalFormat("0");
    private static final DecimalFormat if2 = new DecimalFormat("00");
    private static final DecimalFormat if3 = new DecimalFormat("000");
    private static final DecimalFormat if4 = new DecimalFormat("0000");
    private static final DecimalFormat if5 = new DecimalFormat("00000");
    private static final DecimalFormat if6 = new DecimalFormat("000000");
    private static final DecimalFormat if7 = new DecimalFormat("0000000");
    private static final DecimalFormat if8 = new DecimalFormat("00000000");
    private static final DecimalFormat if9 = new DecimalFormat("000000000");
    private static final DecimalFormat if10= new DecimalFormat("0000000000");
    private static final DecimalFormat if11= new DecimalFormat("00000000000");
    private static final DecimalFormat if12= new DecimalFormat("000000000000");
    private static final DecimalFormat df0 = new DecimalFormat("0.");
    private static final DecimalFormat df1 = new DecimalFormat("0.0");
    private static final DecimalFormat df2 = new DecimalFormat("0.00");
    private static final DecimalFormat df3 = new DecimalFormat("0.000");
    private static final DecimalFormat df4 = new DecimalFormat("0.0000");
    private static final DecimalFormat df5 = new DecimalFormat("0.00000");
    private static final DecimalFormat df6 = new DecimalFormat("0.000000");
    private static final DecimalFormat df7 = new DecimalFormat("0.0000000");
    private static final DecimalFormat df8 = new DecimalFormat("0.00000000");
    private static final DecimalFormat df9 = new DecimalFormat("0.000000000");
    private static final DecimalFormat df10= new DecimalFormat("0.0000000000");

    public static final DecimalFormat df2_2= new DecimalFormat("00.00");
    public static final DecimalFormat df2_3= new DecimalFormat("00.000");
    public static final DecimalFormat df2_5= new DecimalFormat("00.0000");

    //private static FieldPosition fp0 = new FieldPosition(0);
    
    //--
    public static String format0(int num,int total) {
        return format0((long)num,total);
    }
    public static String format(int num) {
        return if0.format((long)num);
    }
    public static String format(long num) {
        return if0.format(num);
    }
    /**
    public static StringBuffer format(StringBuffer buf,int num) {
        return if0.format((long)num,buf,fp0);
    }
    public static StringBuffer format(StringBuffer buf,long num) {
        return if0.format(num,buf,fp0);
    }
    **/
    //-----
    /** 0サプレスフォーマット */
    public static String format0(long num,           // 数値
                                 int total) {       // 桁数 （０サプレス）
        switch (total) {
            case 1:  return if1.format(num);
            case 2:  return if2.format(num);
            case 3:  return if3.format(num);
            case 4:  return if4.format(num);
            case 5:  return if5.format(num);
            case 6:  return if6.format(num);
            case 7:  return if7.format(num);
            case 8:  return if8.format(num);
            case 9:  return if9.format(num);
            case 10: return if10.format(num);
            case 11: return if11.format(num);
            case 12: return if12.format(num);
        }
        return if0.format(num);
    }
    
    public static String format(int num,int total) {
        return format((long)num,total);
    }
    public static String format(long num,        // 数値
                                int total) {    // 桁数（空白サプレス）
        String s = if1.format(num);
        if (total <= 0) return s;
        //マイナス位置固定未処理
        return szSpace[MinMax(0,total-s.length(),MAX_SPACE)]+s;
    }
    
    public static String format(int num,int div,int total) {
        return format((long)num,div,total);
    }
    public static String format(long num,        // 数値
                                int div,        // 序数
                                int total) {   // 桁数（空白サプレス）
        return format((double)num/div,total,2);
    }
    
    
    public static String format(int num,int div,int total,int i) {
        return format((long)num,div,total,i);
    }
    public static String format(long num,        // 数値
                                int div,        // 序数
                                int total,      // 桁数（空白サプレス）
                                int    i) {     // 小数点以下桁数
        return format((double)num/div,total,i);
    }
    
    //----
    public static String format(double num) {   // 数値
        //return format(num,6,2);
        return df2.format(num);    // 99-04-02
    }
    public static String format(double num,     // 数値
                                int total) {    // トータル桁数（空白サプレス）
        return format(num,total,2);     // 小数点以下２桁にする
    }

    public static String format(double num,     // 数値
                                int total,      // トータル桁数（空白サプレス）
                                int    i) {     // 小数点以下桁数
        String s;
        switch (i) {
            case 1:  s = df1.format(num); break;
            case 2:  s = df2.format(num); break;
            case 3:  s = df3.format(num); break;
            case 4:  s = df4.format(num); break;
            case 5:  s = df5.format(num); break;
            case 6:  s = df6.format(num); break;
            case 7:  s = df7.format(num); break;
            case 8:  s = df8.format(num); break;
            case 9:  s = df9.format(num); break;
            case 10:  s = df10.format(num); break;
            default: s = df0.format(num); break;
        }
        if (total < i+2) return s;      // 99-04-02
        // for jdk1.1.6 bug --------------------
        String sc = "";
        int j = 0;
        if (s.charAt(0) == '-') {
            j++;
            sc = "-";
        }
        for (; j < s.length(); j++) {
            char c = s.charAt(j);
            if (c == '.') {
                --j; break;
            }
            if (c != '0') break;
        }
        String ss = sc+s.substring(j);
        return szSpace[MinMax(0,total-ss.length(),MAX_SPACE)]+ss;   
        //-------------------------------------
        //マイナス位置固定未処理
        //return szSpace[MinMax(0,total-s.length(),12)]+s; // これが正しい
    }
    public static String formatL(String s,      // 左側に空白追加
                                 int total) {   // トータル桁数（空白サプレス）
        return szSpace[MinMax(0,total-s.length(),MAX_SPACE)]+s;
    }
    public static String formatR(String s,      // 右側に空白追加
                                 int total) {   // トータル桁数（空白サプレス）
        return s+szSpace[MinMax(0,total-s.length(),MAX_SPACE)];
    }
    //---------------------------------------------------------------------
    // タイマーメソッド
    //---------------------------------------------------------------------
    
    /** 
        タイマー開始 
        @return 開始時刻
    */
    public static long Timer() {
        return System.currentTimeMillis();
    }
    /**
        タイマー終了
        @param start 開始時刻
        @return 経過時間(msec)
    */
    public static long Lapse(long start) {
        return System.currentTimeMillis() - start;
    }
    public static String Lapse(long start, int total) {
        long num =  System.currentTimeMillis() - start;
        return format(num, 1000, total, 3);
    }
    /**
        ミリ秒スリープする
        @param msec スリープ時間
        @return true:正常にスリープした
    */
    public static boolean Delay(int msec) {
        try { 
            Thread.sleep(msec); 
        } catch (InterruptedException e) { 
            System.out.println("Delay interrupted");
            return false;
        }
        return true;
    }


    /** 年齢を求める **/
    static public int getAge(long birth) {
        return getAge(birth, System.currentTimeMillis());
    }
    static public int getAge(long birth, long now) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(new java.util.Date(birth));
        Calendar c2 = Calendar.getInstance();
        c2.setTime(new java.util.Date(now));
        
        int y1 = c1.get(Calendar.YEAR);
        int m1 = c1.get(Calendar.MONTH)+1;
        int d1 = c1.get(Calendar.DATE);
        
        int y2 = c2.get(Calendar.YEAR);
        int m2 = c2.get(Calendar.MONTH)+1;
        int d2 = c2.get(Calendar.DATE);
        
        int age = y2 - y1 - 1;
        do {
            if (m2 > m1) {
                age++;
                break;
            }
            if (m2 < m1) break;
            if (d2 >= d1) age++;
        } while (false);
        return age;
    }

    /** 西暦年から和暦年をもとめる */
    static public int wyear(int year) {
        if (year >= 1989)      year -= 1988;
        else if (year >= 1926) year -= 1925;
        else if (year >= 1912) year -= 1911;
        else if (year >= 1868) year -= 1867;
        return year;
    }
    
    /** 西暦年から元号を求める 
        @return str 明治以前は西暦で返す
    */
    static public String gyear(int year) {
        String str;
        if (year >= 1989)      str = "平成";
        else if (year >= 1926) str = "昭和";
        else if (year >= 1912) str = "大正";
        else if (year >= 1868) str = "明治";
        else                str = "西暦";
        return str;
    }
    
    /** 元号＋年を取得する */
    static public String getWYear(Calendar cal) {
        if (cal == null) return "";
        int year  = cal.get(Calendar.YEAR);
        return Util.gyear(year)+Util.wyear(year)+"年";
    }
    
    
    /** 日付を 平成○○年○月○日 の形で取得する */
    static public String getWDate(Calendar cal) {
        if (cal == null) return "";
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int date  = cal.get(Calendar.DATE);
        return Util.gyear(year)+Util.wyear(year)+"年"+month+"月"+date+"日";
    }
    /** 日付を 平成○○年○○月○○日 の形で取得する <br>
        3月→03月
    */
    static public String getWDate2(Calendar cal) {
        if (cal == null) return "";
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int date  = cal.get(Calendar.DATE);
        return Util.gyear(year)+Util.wyear(year)+"年"+
               ((month < 10)? "0"+month : ""+month)+"月"+
               ((date < 10)? "0"+date : ""+date)+"日";
    }
    
    /** メールアドレスを分からないように変換する @ 以外は x に置き換えられる。
        @param from
    */
    static public CharArray hiddenAddress(CharArray from) {
        CharArray to = new CharArray();
        for (int i = 0; i < from.length(); i++) {
            char c = from.chars[i];
            if (c != '@') c = 'x';
            to.add(c);
        }
        return to;
    }
    /**
        degree から度分秒の文字列に変換する。
    */
    static public CharArray convertDegree(double degree) {
        return convertDegree(degree, 3);
    }
    /**
        degree から度分秒の文字列に変換する。
        @param degree
        @param num 小数点以下桁数
    */
    static public CharArray convertDegree(double degree, int num) {
        int deg  = (int)degree;
        int min  = (int)((degree - deg) * 60.0);
        if (min < 0) min = 0;
        double sec  = (degree - deg - min / 60.0) * 3600.0;
        if (sec < 0) sec = 0;
        
        CharArray ch = new CharArray();
        ch.format(deg, 10, 3, ' ');
        ch.add("\u00b0");
        ch.format(min, 10,2,'0');
        ch.add("\u0027");
        ch.format(sec,num, num+2,'0');
        ch.add("\"");
        return ch;
    }
    static public CharArray convertDegree(String degree) {
        return convertDegree(Double.parseDouble(degree));
    }
    static public CharArray convertDegree(CharArray degree) {
        return convertDegree(degree.getDouble());
    }
    static public CharArray convertDegree(String degree, int num) {
        return convertDegree(Double.parseDouble(degree),num);
    }
    static public CharArray convertDegree(CharArray degree, int num) {
        return convertDegree(degree.getDouble(),num);
    }
    /*
        緯度のdegreeからの変換を行う
    */
    static public CharArray convertLatitude(double degree) {
        return convertLatitude(degree, "N", "S");
    }
    static public CharArray convertLatitude(double degree, int num) {
        return convertLatitude(degree, "N", "S", num);
    }
    static public CharArray convertLatitude(double degree, String szN, String szS) {
        return convertLatitude(degree, szN, szS, 3);
    }
    static public CharArray convertLatitude(double degree, String szN, String szS, int num) {
        while (degree < -180.0) degree += 360.0;
        while (degree > 180.0) degree -= 360.0;
        if (degree > 90.0) degree = 180.0 - degree;
        if (degree < -90.0) degree = -180.0 - degree;
        String unit = (degree >= 0) ? szN : szS;
        if (degree < 0) degree = -degree;
        CharArray ch = convertDegree(degree, num);
        ch.add(unit);
        return ch;
    }
    static public CharArray convertLatitude(String degree) {
        return convertLatitude(Double.parseDouble(degree));
    }
    static public CharArray convertLatitude(CharArray degree) {
        return convertLatitude(degree.getDouble());
    }
    static public CharArray convertLatitude(String degree, String szN, String szS) {
        return convertLatitude(Double.parseDouble(degree), szN, szS);
    }
    static public CharArray convertLatitude(CharArray degree, String szN, String szS) {
        return convertLatitude(degree.getDouble(), szN, szS);
    }
    static public CharArray convertLatitude(String degree, int num) {
        return convertLatitude(Double.parseDouble(degree),num);
    }
    static public CharArray convertLatitude(CharArray degree, int num) {
        return convertLatitude(degree.getDouble(), num);
    }
    static public CharArray convertLatitude(String degree, String szN, String szS, int num) {
        return convertLatitude(Double.parseDouble(degree), szN, szS,num);
    }
    static public CharArray convertLatitude(CharArray degree, String szN, String szS, int num) {
        return convertLatitude(degree.getDouble(), szN, szS, num);
    }
    
    /*
        経度のdegreeからの変換を行う
    */
    static public CharArray convertLongitude(double degree) {
        return convertLongitude(degree, "E", "W");
    }
    static public CharArray convertLongitude(double degree, int num) {
        return convertLongitude(degree, "E", "W", num);
    }
    static public CharArray convertLongitude(double degree, String szE, String szW) {
        return convertLongitude(degree, szE, szW, 3);
    }
    static public CharArray convertLongitude(double degree, String szE, String szW, int num) {
        while (degree < -180.0) degree += 360.0;
        while (degree > 180.0) degree -= 360.0;
        String unit = (degree >= 0) ? szE : szW;
        if (degree < 0) degree = -degree;
        CharArray ch = convertDegree(degree, num);
        ch.add(unit);
        return ch;
    }
    static public CharArray convertLongitude(String degree) {
        return convertLongitude(Double.parseDouble(degree));
    }
    static public CharArray convertLongitude(CharArray degree) {
        return convertLongitude(degree.getDouble());
    }
    static public CharArray convertLongitude(String degree, String szE, String szW) {
        return convertLongitude(Double.parseDouble(degree), szE, szW);
    }
    static public CharArray convertLongitude(CharArray degree, String szE, String szW) {
        return convertLongitude(degree.getDouble(), szE, szW);
    }
    static public CharArray convertLongitude(String degree, int num) {
        return convertLongitude(Double.parseDouble(degree), num);
    }
    static public CharArray convertLongitude(CharArray degree, int num) {
        return convertLongitude(degree.getDouble(), num);
    }
    static public CharArray convertLongitude(String degree, String szE, String szW, int num) {
        return convertLongitude(Double.parseDouble(degree), szE, szW, num);
    }
    static public CharArray convertLongitude(CharArray degree, String szE, String szW, int num) {
        return convertLongitude(degree.getDouble(), szE, szW,num);
    }
    
    /**
      距離の単位付き変換を行う<br>
    */
    static public CharArray convertDistance(CharArray distance) {
        CharArray ch = new CharArray();
        double d = distance.getDouble();
        /**
        String unit ="km";
        if (d < 1.0) {
            d *= 1000.0;
            unit="m";
        }
        **/
        String unit ="m";
        if (d >= 1000.0) {
            d /= 1000.0;
            unit="km";
        }
        ch.format(d);
        ch.add(unit);
        return ch;
    }
    
    
    static private SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static private SimpleDateFormat sdfz = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)");
    /**
        時刻文字列の取得
    */
    
    static public String getDateString() {
        return getDateString(sdf, System.currentTimeMillis());
    }
    static public String getDateStringz() {
        return getDateString(sdfz, System.currentTimeMillis());
    }
    static public String getDateString(long time) {
        return getDateString(sdf, time);
    }
    static public String getDateStringz(long time) {
        return getDateString(sdfz, time);
    }
    static public String getDateString(java.text.Format format) {
        return getDateString(format, System.currentTimeMillis());
    }
    
    static public String getDateString(java.text.Format format, long time) {
        if (format == null || time < 0) return "format error: time:"+time;
        String str = null;
        java.util.Date date = new java.util.Date(time);
        synchronized (format) {
            str = format.format(date);
        }
        return str;
    }

    // 旧メソッドを一時復活
    // システムに依存したファイル名を返す
    public static String getFilename(String s) {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return getFilename(s,0);
        } else {
            return getFilename(s,1);
        }
    }
    
    public static String getFilename(String s,
                                     int mode) { // 0:win 1:unix    
        StringBuffer d = new StringBuffer();
        int start =0;
        //JDK BUG?
        //a:\\ のようにドライブ名の直後に\\が
        //あるとファイルのリストがとれない
        //修正  040198
        //if(s.length()>= 3 && s.charAt(1) == ':' &&
        //    (s.charAt(2) == '\\' || s.charAt(2) == '/')){
        //    d.append(s.charAt(0));
        //    d.append(s.charAt(1));
        //    start = 3;
        //}
        if (mode == 0) {  // windows
            for (int i = start; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '/') c = '\\';
                d.append(c);
            }
        } else {
            for (int i = start; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '\\') c = '/';
                d.append(c);
            }
        }
        if (false) System.out.println("Org Name="+s+" OS Name="+d);
        return d.toString();
    }
    //------------
    static public void setBasic(Component component) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.basic.BasicLookAndFeel");
            SwingUtilities.updateComponentTreeUI(component);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    static public void setWindows(Component component) {
        try {
            Properties prop = System.getProperties();
            String orgOS = (String)prop.put("os.name","Windows 95");
            System.setProperties(prop);
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            SwingUtilities.updateComponentTreeUI(component);
            prop.put("os.name",orgOS);
            System.setProperties(prop);
        } catch (Exception e) {
            System.out.print("setWindows:"+e);
        }
    }
    static public void setMac(Component component) {
        try {
            Properties prop = System.getProperties();
            String orgOS = (String)prop.put("os.name","Mac");
            System.setProperties(prop);

            UIManager.setLookAndFeel("com.sun.java.swing.plaf.mac.MacLookAndFeel");
            SwingUtilities.updateComponentTreeUI(component);
            prop.put("os.name",orgOS);
            System.setProperties(prop);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static public void setMotif(Component component) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            SwingUtilities.updateComponentTreeUI(component);
        } catch (Exception e) {
            System.out.print("setMotif:"+e);
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                SwingUtilities.updateComponentTreeUI(component);
            } catch (Exception ex) {
                System.out.print("setMotif:"+ex);
            }
        }
    }
    */
    
    static public void setMetal(Component component) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            SwingUtilities.updateComponentTreeUI(component);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static public void setMulti(Component component) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.multi.MultiLookAndFeel");
            SwingUtilities.updateComponentTreeUI(component);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static public void setSynth(Component component) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.synth.SynthLookAndFeel");
            SwingUtilities.updateComponentTreeUI(component);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    static public void setOrganic(Component component) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.organic.OrganicLookAndFeel");
            SwingUtilities.updateComponentTreeUI(component);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void setJLF(Component component) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.jlf.JLFLookAndFeel");
            SwingUtilities.updateComponentTreeUI(component);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
    //---------------------------------------------------------------------
    // ボタン描画メソッド
    //---------------------------------------------------------------------
    public final static int OFF = 0;
    public final static int ON = 1;
    public final static int SINK = 2;
    
    public static void button(Graphics g,int x1,int y1,int x2,int y2,
                int h,int flg,Color c1,Color c2,Color c3) {
        int ww = x2-x1+1;
        int hh = y2-y1+1;
        if (flg == SINK) {    // SINK
            g.setColor(Color.black);
            g.drawRect(x1,y1,ww,hh);
            if (h == 1) {
                g.setColor(c2);
                g.fillRect(x1+1,y1+1,ww-2,hh-2);
            } else {
                g.setColor(c3);
                g.drawRect(x1+1,y1+1,ww-1,hh-1);
                g.fillRect(x1+1,y1+1,ww-1,h-1);
                g.fillRect(x1+1,y1+1,h-1,hh-1);
                g.setColor(c2);
                g.fillRect(x1+h,y1+h,ww-h-1,hh-h-1);
            }
        } else {                           // OFF/ON
            if (flg == ON) {
                Color tmp = c1;
                c1 = c3;
                c3 = tmp;
            }
            g.setColor(c3);
            g.fillRect(x1,y2-h+1,ww,h);
            g.fillRect(x2-h+1,y1,h,hh);
            //-----------------
            int xx1 = x1;
            int yy1 = y1;
            int xx2 = x2;
            int yy2 = y2;
            for (int i = h; i != 0; i--) {
                g.setColor(c1);
                g.drawLine(xx1,yy1,xx2,yy1);
                g.drawLine(xx1,yy1,xx1,yy2);
                g.setColor(Color.lightGray);
                g.drawLine(xx2,yy1,xx2,yy1);    // Dot
                g.drawLine(xx1,yy2,xx1,yy2);    // Dot
                xx1++;yy1++;xx2--;yy2--;
            }
            g.setColor(c2);
            g.fillRect(x1+h,y1+h,ww-h-h,hh-h-h);
        }
    }
    public static void button(Graphics g,int x1,int y1,int x2,int y2,int h,
                              int flg,Color c1,Color c2) {
        button(g,x1,y1,x2,y2,h,flg,c1,c2,Color.gray);
    }
    public static void button(Graphics g,int x1,int y1,int x2,int y2,int h,
                              int flg,Color c1) {
         button(g,x1,y1,x2,y2,h,flg,c1,Color.lightGray,Color.gray);
    }
    public static void button(Graphics g,int x1,int y1,int x2,int y2,int h,
                              int flg) {
         button(g,x1,y1,x2,y2,h,flg,Color.white,Color.lightGray,Color.gray);
    }
    public static void button(Graphics g,int x1,int y1,int x2,int y2,int h) {
         button(g,x1,y1,x2,y2,h,OFF,Color.white,Color.lightGray,Color.gray);
    }
    public static void button(Graphics g,int x1,int y1,int x2,int y2) {
         button(g,x1,y1,x2,y2,1,OFF,Color.white,Color.lightGray,Color.gray);
    }

    //---------------------------------------------------------------------
    // データ取得メソッド
    //---------------------------------------------------------------------
    public static double getDouble(String s) {
        double num = 0.0;
        if (s != null && s.trim().length()>0) {
            try {
                Double d = Double.valueOf(s.trim());
                num = d.doubleValue();
            } catch (NumberFormatException e) {
                System.out.println("Util.getDouble("+s+"):"+e);
            }
        }
        
        return num;
    }
    public static boolean getSign(String s) { //符号取得
        return (s.trim().charAt(0) == '-');  // マイナスでtrue
    }

    public static int getInt(String s) {
       return getInt(s,10);
    }
    public static int getInt(String s,int reg) {
        int num = 0;
        if (s != null && s.length()>0) {
            try {
                int i = s.indexOf('.');
                if (i == -1) {  // 見つからない
                    num = Integer.parseInt(s.trim(),reg);
                } else {    // ダブルで入っている
                    num = (int)Math.round(getDouble(s));
                }
            } catch (NumberFormatException e) {
                System.out.println("Util.getInt("+s+"):"+e);
                //throw  new NumberFormatException("format_error");
            }
        }
        return num;
    }

    public static int getInt2(String s) {  // '*' は－１を返す
        int num = 0;
        if (s != null && s.length()>0) {
            try {
                int i = s.indexOf('.');
                if (i == -1) {  // 見つからない
                    i = s.indexOf('*');
                    num = (i == -1) ? Integer.parseInt(s.trim()) : -1;
                } else {    // ダブルで入っている
                    num = (int)Math.round(getDouble(s));
                }
            } catch (NumberFormatException e) {
                System.out.println("Util.getInt("+s+"):"+e);
                //throw  new NumberFormatException("format_error");
            }
        }
        return num;
    }

    public static long getLong(String s) {
        long num = 0;
        if (s != null && s.length()>0) {
            try {
                int i = s.indexOf('.');
                if (i == -1) {  // 見つからない
                    num = Long.parseLong(s.trim());
                } else {    // ダブルで入っている
                    num = (long)Math.round(getDouble(s));
                }
            } catch (NumberFormatException e) {
                System.out.println("Util.getLong("+s+"):"+e);
                //throw  new NumberFormatException("format_error");
            }
        }
        return num;
    }

    public static boolean getBoolean(String s) {
        if (s != null) {
            String str = (s.trim()).toLowerCase();
            if (str.startsWith("on"))   return true;
            if (str.startsWith("true")) return true;
            if (str.startsWith("yes"))  return true;
            if (getInt(str) != 0) return true;
        }
        return false;
    }
    public static String getString(String s) {
        return (s == null)?  "" : s; 
    }
    //----------------------------------
    public static String doublebits2HexString(double d) {
        return Long.toHexString(Double.doubleToLongBits(d));
    }

    /**
        正規表現マッチング
        @param regex 正規表現
        @param target 検索対象文字列
        @return true マッチングする
    */
    public static boolean match(String regex, CharSequence target) {
        boolean sts = false;
        try {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(target);
            sts = m.find();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sts;
    }

    /**
        ファイル名マッチング
        @param regex ?のみ任意の1文字とする
        @param target 検索ターゲット文字列
        @return true: マッチした
    */
    public static boolean nameMatch(CharSequence regex, CharSequence target) {
        boolean sts = false;
        do {
            if (regex == null || target == null) break;
            if (regex.length() != target.length()) break;
            sts = true;
            for (int i = 0; i < regex.length(); i++) {
                char c = regex.charAt(i);
                if (c == '?') continue;
                if (c != target.charAt(i)) {
                    sts = false;
                    break;
                }
            }
        } while (false);
        return sts;
    }

    /**
        ファイルまでのディレクトリがなければ作成する
        @param filename ファイル名
        @return true 成功
    */
    public static boolean mkdirs(String filename) {
        boolean status = false;
        do {
            try {
                File file = new File(filename);
                File parent = file.getParentFile();
                if (parent.exists()) {
                    status = true;
                } else {
                    status = parent.mkdirs();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } while (false);
        return status;
    }

}


//
// [end of Util.java]
//

