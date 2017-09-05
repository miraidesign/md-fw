//------------------------------------------------------------------------
// @(#)CryptPass.java
//          58進数を利用したパスワード等生成ツール：可逆変換
//          Copyright (c) Mirai Design Institute 2014 All Rights Reserved. 
//------------------------------------------------------------------------
// String encode(no):   数値をエンコードする
// long decode(str):    元の数値に戻す

package com.miraidesign.util;       // 

/**
 * 58進数変換を利用したURL短縮＆暗号化ツール。可逆変換可能。
 * @version 1.0
 * @author Toru Ishioka
 * @since  JDK1.1
 */

public class CryptPass {
    private static boolean debug = false;
    public static void setDebug(boolean mode) { debug = mode;}
    
    private static int RADIX = 58;
    public static void setRadix(int radix) { RADIX = radix;}

    private static char[] chStr = { // 62 進数変換用テーブル
      // 0   1   2   3   4   5   6   7   8   9
        'a','N','9','s','D','B','i','W','I','2',
      // A   B   C   D   E   F   G   H   I   J   K   L   M   N   O
      //10  11  12  13  14  15  16  17  18  19  20  21  22  23  24
        'q','5','C','e','E','u','U','H','8','J','z','L','c','x','w',
      // P   Q   R   S   T   U   V   W   X   Y   Z
      //25  26  27  28  29  30  31  32  33  34  35
        'Y','P','R','d','T','G','g','7','X','y','o',
      // a   b   c   d   e   f   g   h   i   j   k   l   m   n   o
      //36  37  38  39  40  41  42  43  44  45  46  47  48  49  50
        'K','b','M','S','4','f','V','h','6','j','k','Q','n','m','Z',
      // p   q   r   s   t   u   v   w   x   y   z
      //51  52  53  54  55  56  57  58  59  60  61
        'p','A','r','3','t','F','v','0','O','1','l'};

    private static int[] iVal = {
      // 0   1   2   3   4   5   6   7   8   9
        58 ,60 , 9 ,54 ,40 ,11 ,44 ,32 ,18 , 2 , 0,0,0,0,0,0,0,
      // A   B   C   D   E   F   G   H   I   J   K   L   M   N   O
        52 , 5 ,12 , 4 ,14 ,56 ,30 ,17 , 8 ,19 ,36 ,21 ,38 , 1 ,59,
      // P   Q   R   S   T   U   V   W   X   Y   Z
        26 ,47 ,27 ,39 ,29 ,16 ,42 , 7 ,33 ,25 ,50 ,0,0,0,0,0,0,
      // a   b   c   d   e   f   g   h   i   j   k   l   m   n   o
         0 ,37 ,22 ,28 ,13 ,41 ,31 ,43 , 6 ,45 ,46 ,61 ,49 ,48 ,35, 
      // p   q   r   s   t   u   v   w   x   y   z
        51 ,10 ,53 , 3 ,55 ,15 ,57 ,24 ,23 ,34 ,20};
                      

    private static char[] chars = new char[64];    // ロックして使う事
    private static char[] chBuf = new char[64];    // ロックして使う事

    private static long count = 0;
    
    private static int M1 = 7;
    private static int M2 = 137;
    private static int M3 = 1093;

    /**
     *  数値のエンコードを行う
     *  @param num  元数値
     *  @return  暗号化文字列
     *
     */
    static public CharArray encode(long num) {
        return encode(num, 2, true);
    }
    /**
     *  数値のエンコードを行う
     *  @param num  元数値
     *  @param version 1:旧分散演算 2:新分散演算
     *  @param shift_mode  シフトモード
     *  @return  暗号化文字列
     *
     */
    static public CharArray encode(long num, int version, boolean shift_mode) {
        long time = System.currentTimeMillis()+(++count*7);
        int t1 = (int)(time % RADIX);
        int t2 = (int)(((time>>4) +(count*11)) % RADIX);

        char c1 = chStr[t1];
        char c2 = chStr[t2];
        
        CharArray ch = new CharArray();
        long magic = (c1 * M1+ c2 * M2) + M3;
        if (version >= 2)  magic = (c1 * M1+ c2 * M2) * M3;
        
        //magic = 0;
        
        ch.add(c1);
        String str = _encode(num,RADIX,magic);
        if (shift_mode) {
            ch.add(CharArray.shift(str, (int)magic));
        } else {
            ch.add(str);
        }
        ch.add(c2);
        return ch;  // return chtoString();
    }
    
    /**
     *  暗号化文字列をデコードする
     *  @param str  暗号化文字列
     *  @return     変換前数値
     */
    static public long decode(String str) {
        return decode(str, 2);
    }
    static public long decode(String str, int version) {
        long ret = 0;
        CharArray ch = CharArray.pop(str);  // 文字列クラスにコピー
        long c1 = (long)ch.chars[0];
        long c2 = (long)ch.chars[ch.length-1];
        ch.length--;
        ch.remove(0,1);
        long magic = (c1 * M1+ c2 * M2) + M3;
        if (version >= 2)  magic = (c1 * M1+ c2 * M2) * M3;
        ret = _decode(ch,RADIX,magic);
        CharArray.push(ch);                 // 文字列クラスを返却
        return ret;
    }
    static public long decode(CharArray str) {
        return decode(str, 2, true);
    }
    static public long decode(CharArray str, int version, boolean shift_mode) {
        long ret = 0;
        CharArray ch = CharArray.pop(str);  // 文字列クラスにコピー
        long c1 = (long)ch.chars[0];
        long c2 = (long)ch.chars[ch.length-1];
        ch.length--;
        ch.remove(0,1);
        
        long magic = (c1 * M1+ c2 * M2) + M3;
        if (version >= 2)  magic = (c1 * M1+ c2 * M2) * M3;
        
        if (shift_mode) ch.shift(-(int)magic);
        
        ret = _decode(ch,RADIX,magic);
        CharArray.push(ch);                 // 文字列クラスを返却
        return ret;
    }

    /**
     *  エンコード補助関数(n進数暗号化文字列変換)
     *  @param no               元数値
     *  @param radix 基数       2 - RADIX
     *  @param magic_value      暗号化係数
     *  @return 変換後文字列
     */
    static private synchronized String _encode(long no,int radix, long magic_value) {
        if (debug) System.out.println("===============================");
        int j = 0;
        long val = no;
        val += magic_value;
        if (radix >= 2 && radix <= RADIX) {
            int i = 0;
            do {
                chBuf[i++] = chStr[(int)(val % radix)];
                if (debug) {
                    int n = (int)(val % radix);
                    char c = chStr[n];
                    System.out.println("encode["+c+"]"+n);
                }
                
                val /= radix;
            } while (val > 0);
            while (i > 0) chars[j++] = chBuf[--i];  // 反転コピー
        }
        return new String(chars,0,j);
    }

    /**
     *  デコード補助関数(n進数暗号化文字列復号)
     *  @param s                暗号文字列
     *  @param radix 基数       2 - RADIX
     *  @param magic_value      暗号化係数
     *  @return                 復号数値
     */
    static private synchronized long _decode(CharArray s, int radix, long magic_value) {
        if (debug) System.out.println("-------------------------------");
        long val = 0;
        for (int i = 0; i < s.length(); i++) {
            val *= radix;
            char c = s.chars[i];
            //if (c > ' ' && c <= '~') {
            //    val += iVal[c-'!'];
            if (c >= '0' && c <= 'z') {
                val += iVal[c-'0'];
                if (debug) {
                    int n = iVal[c-'!'];
                    System.out.println("decode["+c+"]"+n);
                }
            } else {
                System.out.println("decode error!!!");
                break;
            }
        }
        val -= magic_value;
        return val;
    }
    
}





//
// [end of CryptPass.java]
//

