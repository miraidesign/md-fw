//------------------------------------------------------------------------
// @(#)Validation.java
//                  
//              Copyright (c) Mirai Design. 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

/**
 *  バリデーションメソッドを集めたクラス
 *
 * @version 0.5 
 * @author Toru Ishioka
 * @since  JDK1.0
 */

public class Validation {
    static private boolean debug = true;
    /**
        Email で使用できる文字か？
        @param address E-mailアドレス
        @return true:OK
    */
    static public boolean isEmailAddress(CharArray address) {
        if (address == null) return false;
        CharArray ch = CharArray.pop(address).trim();
        boolean sts = checkEmailAddress(ch);
        CharArray.push(ch);
        return sts;
    }
    static public boolean isEmailAddress(String address) {
        if (address == null) return false;
        CharArray ch = CharArray.pop(address);
        boolean sts = checkEmailAddress(ch);
        CharArray.push(ch);
        return sts;
        
    }
    
    static private boolean checkEmailAddress(CharArray ch) {
        if (ch.length() <= 0) return false;
        int index = ch.indexOf("@");
        if (index <= 0) return false;
        int atmark = index;
        
        if ((index+1) == ch.length())    return false;
        if (ch.indexOf("@",index+1) > 0) return false;
        
        if ((index = ch.indexOf("..",atmark)) >= 0) return false;
        if ((index = ch.indexOf(".",atmark))  <= 0) return false;
        
        String strOK = "._@-?/+%";   
        for (int i = 0; i < ch.length(); i++) {
            char c = ch.charAt(i);
            if ((strOK.indexOf(c) >= 0) ||
                Character.isDigit(c) || Character.isLowerCase(c) ||
                Character.isUpperCase(c)) {
            } else {
                return false;
            }
        }
        return true;
    }
    
    /** Mobile用のメールアドレスか？ */
    static public boolean isMobileEmailAddress(String address) {
        CharArray ch = CharArray.pop(address);
        boolean sts = isMobileEmailAddress(ch,true);
        CharArray.push(ch);
        return sts;
    }
    /** Mobile用のメールアドレスか？ */
    static public boolean isMobileEmailAddress(CharArray address) {
        return isMobileEmailAddress(address,true);
    }
    
    static String[] szMobileAddress = {
        "@docomo.ne.jp",
        "@ezweb.ne.jp",
        "vodafone.ne.jp",
        "softbank.ne.jp",
        "desney.ne.jp",
        "i.softbank.jp",
        "pdx.ne.jp",
        "emnet.ne.jp",
        "willcom.com"
    };
    
    /**
        Mobile用のメールアドレスか？
        @param address メールアドレス
        @param check   文字チェックも行う
    */
    static public boolean isMobileEmailAddress(String address, boolean check) {
        CharArray ch = CharArray.pop(address);
        boolean sts = isMobileEmailAddress(ch,check);
        CharArray.push(ch);
        return sts;
    }
    static public boolean isMobileEmailAddress(CharArray address, boolean check) {
        boolean sts = false;
        CharArray ch = CharArray.pop(address);
        do {
            ch.trim();
            if (check) {
                sts = checkEmailAddress(ch);
                if (!sts) break;
            }
            sts = false;
            ch.toLowerCase();
            for (int i = 0; i < szMobileAddress.length; i++) {
                if (ch.endsWith(szMobileAddress[i])) {
                    sts = true;
                    break;
                }
            }
        } while (false);
        CharArray.pop(ch);
        return sts;
    }
    
    static int[] days = {31,28,31,30,31,30,31,31,30,31,30,31};
    /**
        正しい誕生日入力か？
        @param year     年
        @param month    月
        @param day      日
        @param maxAge 有効とする最大の年齢
    */
    static public boolean isBirthDate(int year, int month, int day,int maxAge) {
        boolean sts = false;
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,day);
        // 存在チェック
        do {
            if (month < 1 || month >12) break;
            int checkDay = days[month-1];
            if (checkDay == 28 && 
                (((year % 4 == 0) && !(year % 100 == 0)) || (year % 400 == 0))) {
                checkDay = 29;
            }
            if (day < 1 || day > checkDay) break;
        
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(System.currentTimeMillis()));
            int currentYear = cal.get(Calendar.YEAR);
            if  (currentYear - year > maxAge) break;
            if  (currentYear < year) break;
            if  (currentYear > year) {sts = true; break;}
            if  (cal.get(Calendar.MONTH)+1 < month) break;
            if  (cal.get(Calendar.MONTH)+1 > month) {sts = true; break;}
            sts = (cal.get(Calendar.DATE) >= day);
        } while (false);
        return sts;
    }
    static public boolean isBirthDate(int year, int month, int day) {
        return isBirthDate(year, month, day, 150);
    }
    
    /**
        正しい日付情報か 2003/01/02 2003-01-02 20030102 のみ認める
        @param in    入力文字列
        @return 正しい時入力文字列 null:エラー
    **/
    static public CharArray checkDate(String in) {
        CharArray tmp = CharArray.pop(in);
        CharArray ch = checkDate(tmp);
        CharArray.push(tmp);
        return ch;
    }
    static public CharArray checkDate(CharArray in) {
        return checkDate(in,null);
    }
    /**
        正しい日付情報か 2003/01/02 2003-01-02 20030102 のみ認める
        @param in    入力文字列
        @param delim 正しい時に、変更する区切り文字(nullで変更しない）
        @return 正しい時、変更後の文字列 null:エラー
    **/
    static public CharArray checkDate(String in, String delim) {
        CharArray tmp = CharArray.pop(in);
        CharArray ch = checkDate(tmp, delim);
        CharArray.push(tmp);
        return ch;
    }
    
    static int MIN_YEAR = 1800;
    static int MAX_YEAR = 2200;
    static public int[] cdate = {0,31,28,31,30,31,30,31,31,30,31,30,31};
    
    static public CharArray checkDate(CharArray in, String delim) {
        CharArray ch = null;
        do {
            int length = in.length();
            if (length == 8) {
                if (in.isDigit()) {
                    int i = in.getInt();
                    int yy = i / 10000;
                    int mm = (i / 100) % 100;;
                    int dd = i % 100;
                    if (yy < MIN_YEAR || yy > MAX_YEAR) break;
                    if (mm < 1 || mm > 12) break;
                    
                    int checkDay = cdate[mm];
                    if (checkDay == 28 && 
                       (((yy % 4 == 0) && !(yy % 100 == 0)) || (yy % 400 == 0))) {
                        checkDay = 29;
                    }
                    
                    if (dd < 1 || dd > checkDay) break;
                    
                    ch = new CharArray(in);
                    if (delim != null) {
                        int len = delim.length();
                        if (len > 0) {
                            ch.insert(4, delim);
                            ch.insert(6+len,delim);
                        }
                    }
                }
            } else if (length == 10) {
                CharToken token = CharToken.pop();
                do {
                    if (in.isDigit()) {
                        ch = null;
                    } else if (in.isDigit('-')) {
                        token.set(in,"-");
                        if (token.size() != 3) break;
                        if (token.get(0).length() != 4) break;
                        if (token.get(1).length() != 2) break;
                        if (token.get(2).length() != 2) break;
                        int yy = token.getInt(0);
                        int mm = token.getInt(1);
                        int dd = token.getInt(2);
                        if (yy < MIN_YEAR || yy > MAX_YEAR) break;
                        if (mm < 1 || mm > 12) break;
                        
                        int checkDay = cdate[mm];
                        if (checkDay == 28 && 
                           (((yy % 4 == 0) && !(yy % 100 == 0)) || (yy % 400 == 0))) {
                            checkDay = 29;
                        }
                        
                        if (dd < 1 || dd > checkDay) break;
                        ch = new CharArray(in);
                        if (delim != null && !delim.equals("-")) {
                            ch.replace("-", delim);
                        }
                    } else if (in.isDigit('/')) {
                        token.set(in,"/");
                        if (token.size() != 3) break;
                        if (token.get(0).length() != 4) break;
                        if (token.get(1).length() != 2) break;
                        if (token.get(2).length() != 2) break;
                        int yy = token.getInt(0);
                        int mm = token.getInt(1);
                        int dd = token.getInt(2);
                        if (yy < MIN_YEAR || yy > MAX_YEAR) break;
                        if (mm < 1 || mm > 12) break;
                        
                        int checkDay = cdate[mm];
                        if (checkDay == 28 && 
                           (((yy % 4 == 0) && !(yy % 100 == 0)) || (yy % 400 == 0))) {
                            checkDay = 29;
                        }
                        
                        if (dd < 1 || dd > checkDay) break;
                        ch = new CharArray(in);
                        if (delim != null && !delim.equals("/")) {
                            ch.replace("/", delim);
                        }
                    }
                } while (false);
                CharToken.push(token);
            }
        } while (false);
        return ch;
    }
    /**
        年月日の妥当性チェックを行う<br>
        西暦1000年～3000年までを有効とする。
    */
    static public boolean checkDate(int year, int month, int date) {
        boolean sts = false;
        do {
            int yy = year;
            int mm = month;
            int dd = date;
            if (yy < 1000 || yy >= 3000) break;
            if (mm < 1 || mm > 12) break;
                    
            int checkDay = cdate[mm];
            if (checkDay == 28 && 
               (((yy % 4 == 0) && !(yy % 100 == 0)) || (yy % 400 == 0))) {
                checkDay = 29;
            }
                    
            if (dd < 1 || dd > checkDay) break;
            sts = true;
        } while (false);
        return sts;
    }
    
    /* start <= end の確認 */ 
    static public boolean checkPeriod(CharArray start, CharArray end) {
        return checkPeriod(start, end, "/");
    }
    static public boolean checkPeriod(CharArray start, CharArray end, String delim) {
        boolean sts = false;
        if (start != null && end != null) {
            CharToken t1 = CharToken.pop();
            CharToken t2 = CharToken.pop();
            do {
                t1.set(start,delim);
                t2.set(end,delim);
                if (t1.size() != 3) break;
                if (t1.get(0).length() != 4) break;
                if (t1.get(1).length() != 2) break;
                if (t1.get(2).length() != 2) break;
                if (t2.size() != 3) break;
                if (t2.get(0).length() != 4) break;
                if (t2.get(1).length() != 2) break;
                if (t2.get(2).length() != 2) break;
                int y1 = t1.getInt(0);
                int y2 = t2.getInt(0);
                int m1 = t1.getInt(1);
                int m2 = t2.getInt(1);
                int d1 = t1.getInt(2);
                int d2 = t2.getInt(2);
                if (y1 < MIN_YEAR || y1 > MAX_YEAR) break;
                if (y2 < MIN_YEAR || y2 > MAX_YEAR) break;
                if (m1 < 1 || m1 > 12) break;
                if (m2 < 1 || m2 > 12) break;

                int checkDay = cdate[m1];
                if (checkDay == 28 && 
                    (((y1 % 4 == 0) && !(y1 % 100 == 0)) || (y1 % 400 == 0))) {
                    checkDay = 29;
                }
                if (d1 < 1 || d1 > checkDay) break;
                
                checkDay = cdate[m2];
                if (checkDay == 28 && 
                    (((y2 % 4 == 0) && !(y2 % 100 == 0)) || (y2 % 400 == 0))) {
                    checkDay = 29;
                }
                if (d2 < 1 || d2 > checkDay) break;
                
                if (y1 > y2) { sts = false; break;}
                if (y1 < y2) { sts = true; break;}
                if (m1 > m2) { sts = false; break;}
                if (m1 < m2) { sts = true; break;}
                if (d1 > d2) { sts = false; break;}
                sts = true;
                
            } while (false);
            CharToken.push(t2);
            CharToken.push(t1);
        }
        return sts;
    }
    /**
     * 文字列が全て半角数字で書かれているかチェックします。
     * @param  ch チェックしたい文字列
     * @return  文字列が半角数字ならtrue
     */
    static public boolean isDigit(CharArray ch) {
        if (ch == null /**|| ch.length() == 0**/) return false;
        boolean rsts = true;
        for (int i = 0; i < ch.length(); i++){
            if ('0' <= ch.charAt(i) && ch.charAt(i) <= '9') {
                rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 文字列が全て半角数字で書かれているかチェックします。
     * @param  str チェックしたい文字列
     * @return  文字列が半角数字ならtrue
     */
    static public boolean isDigit(String str){
        if (str == null /**|| str.length()== 0**/) return false;
        boolean rsts = true;
        for (int i = 0; i < str.length(); i++){
            if ('0' <= str.charAt(i) && str.charAt(i) <= '9') {
                rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 文字列が全て半角数字で書かれているかチェックします。
     * １文字だけ半角数字以外に有効とする文字を指定できます。
     * @param  ch チェックしたい文字列
     * @param  c   有効にする文字
     * @return  文字列が半角数字もしくは指定文字ならtrue
     */
    static public boolean isDigit(CharArray ch, char c) {
        if (ch == null /**|| ch.length() == 0**/) return false;
        boolean rsts = true;
        for (int i = 0; i < ch.length(); i++){
            if ('0' <= ch.charAt(i) && ch.charAt(i) <= '9') {
                //rsts = true;
            } else if (ch.charAt(i) == c) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 文字列が全て半角数字で書かれているかチェックします。
     * １文字だけ半角数字以外に有効とする文字を指定できます。
     * @param  str チェックしたい文字列
     * @param  c   有効にする文字
     * @return  文字列が半角数字もしくは指定文字ならtrue
     */
    static public boolean isDigit(String str, char c){
        if (str == null /**|| str.length()== 0*/) return false;
        boolean rsts = true;
        for (int i = 0; i < str.length(); i++){
            if ('0' <= str.charAt(i) && str.charAt(i) <= '9') {
                //rsts = true;
            } else if (str.charAt(i) == c) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }

    /**
     * 文字列に含まれる文字が、全てが指定した文字の集合に含まれるかどうか
     * チェックします。
     * @see com.miraidesign.util.CharArray#contains
     * @param   str チェックしたい文字列
     * @param   set 指定文字の集合
     * @return  全て指定文字なら true
     */
    static public boolean contains(String str, String set){
        boolean rsts = true;
        for (int i = 0; i < str.length(); i++){
            if (set.indexOf(str.charAt(i)) >= 0) {
                //rsts = true;
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }

    /**
     * 文字列が全て半角英数で書かれているかチェックします。
     * 全て半角ならばtrue、全角が含まれているならばfalseを返します。
     * <p>
     * @param  ch チェックしたい文字列
     * @return  文字列が半角ならばtrue、全角が含まれているならばfalse
     */
    static public boolean isAscii(CharArray ch) {
        boolean rsts = true;
        for (int i = 0; i < ch.length(); i++){
            char c = ch.charAt(i);
            if ('\u0020' <= c && c <= '\u007e') { // 半角英数
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 文字列が全て半角英数で書かれているかチェックします。
     * 全て半角ならばtrue、全角が含まれているならばfalseを返します。
     * <p>
     * @param  str チェックしたい文字列
     * @return  文字列が半角ならばtrue、全角が含まれているならばfalse
     */
    static public boolean isAscii(String str){
        boolean rsts = true;
        for (int i = 0; i < str.length(); i++){
            char c = str.charAt(i);
            if ('\u0020' <= c && c <= '\u007e') { // 半角英数
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }

    /**
     * 文字列が全て半角英数または半角カナで書かれているかチェックします。
     * 全て半角ならばtrue、全角が含まれているならばfalseを返します。
     * <p>
     * @param  ch チェックしたい文字列
     * @return  文字列が半角ならばtrue、全角が含まれているならばfalse
     */
    static public boolean isHankaku(CharArray ch) {
        boolean rsts = true;
        for (int i = 0; i < ch.length(); i++){
            char c = ch.charAt(i);
            if (('\u0020' <= c && c <= '\u007e') || // 半角英数
                ('\uff61' <= c && c <= '\uff9f')) { // 半角カナ
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 文字列が全て半角英数または半角カナで書かれているかチェックします。
     * 全て半角ならばtrue、全角が含まれているならばfalseを返します。
     * <p>
     * @param  str チェックしたい文字列
     * @return  文字列が半角ならばtrue、全角が含まれているならばfalse
     */
    static public boolean isHankaku(String str){
        boolean rsts = true;
        for (int i = 0; i < str.length(); i++){
            char c = str.charAt(i);
            if (('\u0020' <= c && c <= '\u007e') || // 半角英数
                ('\uff61' <= c && c <= '\uff9f')) { // 半角カナ
            } else {
                rsts = false;
                break;
            }
        }
        return rsts;
    }

    /**
     * 文字列が全て全角で書かれているかチェックします。
     * @param  ch チェックしたい文字列
     * @return  文字列が全角ならばtrue、半角が含まれているならばfalse
     */
    static public boolean isZenkaku(CharArray ch) {
        boolean rsts = true;
        for (int i = 0; i < ch.length(); i++){
            char c = ch.charAt(i);
            if (('\u0020' <= c && c <= '\u007e') || // 半角英数
                ('\uff61' <= c && c <= '\uff9f')) { // 半角カナ
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    /**
     * 文字列が全て全角で書かれているかチェックします。
     * @param  str チェックしたい文字列
     * @return  文字列が全角ならばtrue、半角が含まれているならばfalse
     */
    static public boolean isZenkaku(String str){
        boolean rsts = true;
        for (int i = 0; i < str.length(); i++){
            char c = str.charAt(i);
            if (('\u0020' <= c && c <= '\u007e') || // 半角英数
                ('\uff61' <= c && c <= '\uff9f')) { // 半角カナ
                rsts = false;
                break;
            }
        }
        return rsts;
    }
    
    /**
        全て同じ文字で構成されていれば trueを返します
        @param str チェックする文字列
        @return true 全て同じ文字かチェック対象がない
    */
    static public boolean isSameChar(CharArray str) {
        boolean rsts = true;
        if (str != null && str.length() > 0) {
            char c0 = str.chars[0];
            for (int i = 1; i < str.length(); i++) {
                if (str.chars[i] != c0) {
                    rsts = false;
                    break;
                }
            }
        }
        return rsts;
    }
    
    /**
        パスワードをチェックします
        @param passwd パスワード
        @param min 最小値
        @param max 最大値
        @param hv  NGパスワードリスト
    */
    static public boolean checkPasswd(CharArray passwd, 
                                      int min, int max, 
                                      HashVector<CharArray,CharArrayQueue> hv) {
        boolean rsts = false;
        do {
            if (passwd == null) break;
            if (passwd.length() < min) break;
            if (passwd.length() > max) break;
            rsts = true;
            CharToken token = CharToken.pop();
            for (int i = 0; i < hv.size(); i++) {
                CharArray key = (CharArray)hv.keyElementAt(i);
                CharArrayQueue queue = (CharArrayQueue)hv.elementAt(i);
                if (debug) System.out.println("["+i+"]KEY["+key+"]"+queue.size());
                for (int j = 0; j < queue.size(); j++) {
                    CharArray ch = queue.peek(j).trim();
                    if (ch.length() == 0) continue;
                    token.set(ch,",");
                    for (int k = 0; k < token.size(); k++) {
                        CharArray ca = token.get(k).trim();
                        if (ca.length() == 0) continue;
                        if (ca.equals(passwd)) {
                            rsts = false;
                if (debug) System.out.println("check "+passwd +" to "+ca+" NG!");
                            break;
                        } else {
                if (debug) System.out.println("check "+passwd +" to "+ca+" OK!");
                        }
                    } // next
                    if (!rsts) break;
                } // next
                if (!rsts) break;
            } // next
            CharToken.push(token);
        } while (false);
        return rsts;
    }
    
    /** アスキー文字（半角英数字記号）か？ **/
    static public boolean isAscii(char c){
        return ('\u0020' <= c && c <= '\u007e');
    }
    static public boolean isHankaku(char c){
        return (('\u0020' <= c && c <= '\u007e')||('\uff61' <= c && c <= '\uff9f'));
    }
    static public boolean isZenkaku(char c){
        return !(('\u0020' <= c && c <= '\u007e')||('\uff61' <= c && c <= '\uff9f'));
    }
    
    /**
     * 半角を１、全角を２とした文字列長を返します
     * @param  ch チェックしたい文字列
     * @return  文字列長
     */
    static public int strlen(CharArray ch) {
        int  rsts = 0;
        for (int i = 0; i < ch.length(); i++){
            char c = ch.charAt(i);
            if (('\u0020' <= c && c <= '\u007e') || // 半角英数
                ('\uff61' <= c && c <= '\uff9f')) { // 半角カナ
                rsts++;
            } else {
                rsts += 2;
            }
        }
        return rsts;
    }
    /**
     * 半角を１、全角を２とした文字列長を返します
     * @param  str チェックしたい文字列
     * @return  文字列長
     */
    static public int strlen(String str) {
        int  rsts = 0;
        for (int i = 0; i < str.length(); i++){
            char c = str.charAt(i);
            if (('\u0020' <= c && c <= '\u007e') || // 半角英数
                ('\uff61' <= c && c <= '\uff9f')) { // 半角カナ
                rsts++;
            } else {
                rsts += 2;
            }
        }
        return rsts;
    }



    /**
     * 文字列が全て全角カタカナで書かれているかチェックします。<br>
     * 全て全角カタカナならばtrue、それ以外が含まれているならばfalseを返します。
     * 
     * @param  ch チェックしたい文字列
     * @return  文字列が半角ならばtrue、全角が含まれているならばfalse
     */
    static public boolean isKatakana(CharArray ch) {
        for (int i = 0; i < ch.length(); i++){
            char c = ch.chars[i];
            if('\u30a1' <= c && c <= '\u30fe'){
            } else if (c == ' ') { 
            } else if (c == '　') { // 全角スペース
            } else if (c == 'ー') { 
            } else {
                return false;
            }
        }
        return true;
    }
    /**
     * 文字列が全て全角カタカナで書かれているかチェックします。<br>
     * 全て全角カタカナならばtrue、それ以外が含まれているならばfalseを返します。
     * 
     * @param  str チェックしたい文字列
     * @return  文字列が半角ならばtrue、全角が含まれているならばfalse
     */
    static public boolean isKatakana(String str) {
        for (int i = 0; i < str.length(); i++){
            char c = str.charAt(i);
            if('\u30a1' <= c && c <= '\u30fe'){
            } else if (c == ' ') { 
            } else if (c == '　') { // 全角スペース
            } else if (c == 'ー') { 
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 文字列が全て全角ひらがなで書かれているかチェックします。
     * 全て全角ひらがなならばtrue、それ以外が含まれているならばfalseを返します。
     * <p>
     * @param  ch チェックしたい文字列
     * @return  文字列が半角ならばtrue、全角が含まれているならばfalse
     */
    static public boolean isHiragana(CharArray ch) {
        for (int i = 0; i < ch.length(); i++){
            char c = ch.charAt(i);
            if('\u3041' <= c && c <= '\u309e'){
            } else if (c == ' ') {
            } else if (c == '　') { //全角スペース
            } else if (c == 'ー') { 
            } else {
                return false;
            }
        }
        return true;
    }
    /**
     * 文字列が全て全角ひらがなで書かれているかチェックします。
     * 全て全角ひらがなならばtrue、それ以外が含まれているならばfalseを返します。
     * <p>
     * @param  str チェックしたい文字列
     * @return  文字列が半角ならばtrue、全角が含まれているならばfalse
     */
    static public boolean isHiragana(String str) {
        for (int i = 0; i < str.length(); i++){
            char c = str.charAt(i);
            if('\u3041' <= c && c <= '\u309e'){
            } else if (c == ' ') { 
            } else if (c == '　') { //全角スペース
            } else if (c == 'ー') { 
            } else {
                return false;
            }
        }
        return true;
    }


    //--------------------------------------------------------------------------
    //    変換用テーブル
    //--------------------------------------------------------------------------
    static private Hashtable<IntObject,CharArray> convertAscii;
    static private Hashtable<IntObject,CharArray> convertBoth;
    static private Hashtable<IntObject,CharArray> convertKana;
    static private Hashtable<IntObject,CharArray> convertKanji;
    static private Hashtable<CharArray,CharArray> convertDakuon;
    
    // 全角変換用
    static private String zenkaku1 = 
        "　！”＃＄％＆’（）＊＋，－．／０１２３４５６７８９：；＜＝＞？"+
        "＠ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ［￥］＾＿"+
        "‘ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ｛｜｝￣■";
    static public String zenkaku2 = 
        "。「」、・ヲァィゥェォャュョッーアイウエオカキクケコサシスセソタチツテトナニヌネノ"+
        "ハヒフヘホマミムメモヤユヨラリルレロワン゛゜";

                                       // 削除する文字列
    static private String omitString = 
        "!\"#$%&'()*+,./:;=<>?@\\^_`{|}~｡｢｣[]､･ﾞﾟ "+
        "・、。，．：；？！゛゜´｀¨＾￣＿ヽヾゝゞ〃仝々〆〇／＼～∥｜…‥"+
        "‘’“”（）〔〕［］｛｝〈〉《》「」『』【】＋±×÷＝≠＜＞≦≧∞∴♂♀°′″"+
        "℃￥＄￠￡％＃＆§☆★○●◎◇◆□■△▲▽▼※〒→←↑↓〓∈∋⊆⊇⊂⊃∪∩∧∨"+
        "￢⇒⇔∀∃∠⊥⌒∂∇≡≒≪≫√∽∝∵∫∬Å‰♯♭♪†‡◯　";

    static String[][] convNum = {
            {"①１Ⅰⅰ1","1"}, {"②２Ⅱⅱ2","2"},{"③３Ⅲⅲ3","3"}, {"④４Ⅳⅳ4","4"},
            {"⑤５Ⅴⅴ5","5"}, {"⑥６Ⅵⅵ6","6"},{"⑦７Ⅶⅶ7","7"}, {"⑧８Ⅷⅷ8","8"},
            {"⑨９Ⅸⅸ9","9"}, {"０0","0"},{"⑩Ⅹⅹ","10"},  
            {"⑪","11"}, {"⑫","12"}, {"⑬","13"}, {"⑭","14"},{"⑮","15"},
            {"⑯","16"},{"⑰","17"},{"⑱","18"},{"⑲","19"},{"⑳","20"},
    };

    static String  w_ascii = "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ"+
                             "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ";
    static String  ascii   = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static String  ascii2  = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@_";
/**
            －817c―815c‐815DｰB0
            ‐815d―815c─849fｰB0-
*/
    static String  minus = "－ー‐―─ｰ-";

    static String kana = "ヲァィゥェォャュョッアイウエオカキクケコサシスセソタチツテトナニヌネノ"+
                         "ハヒフヘホマミムメモヤユヨラリルレロワンヰヱ";
    static String[] hankanadaku = { "ｶﾞ","ｷﾞ","ｸﾞ","ｹﾞ","ｺﾞ","ｻﾞ","ｼﾞ","ｽﾞ","ｾﾞ","ｿﾞ",
                          "ﾀﾞ","ﾁﾞ","ﾂﾞ","ﾃﾞ","ﾄﾞ","ﾊﾞ","ﾋﾞ","ﾌﾞ","ﾍﾞ","ﾎﾞ","ﾊﾟ","ﾋﾟ","ﾌﾟ","ﾍﾟ","ﾎﾟ","ｳﾞ"};
    static String kanadaku  = "ガギグゲゴザジズゼゾダヂヅデドバビブベボパピプペポヴ";
    static String kanadaku2 = "カキクケコサシスセソタチツテトハヒフヘホハヒフヘホウ";
    static String hira = "をぁぃぅぇぉゃゅょっあいうえおかきくけこさしすせそたちつてとなにぬねの"+
                         "はひふへほまみむめもやゆよらりるれろわんゐゑ";
    static String hiradaku  = "がぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽヴ";
    static String hiradaku2 = "かきくけこさしすせそたちつてとはひふへほはひふへほウ";
    static String hankana  = "ｦｧｨｩｪｫｬｭｮｯｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝｲｴ";
    static String hankana1 = "ｦｱｲｳｴｵﾔﾕﾖﾂｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝｲｴ";
    static String hankana2 = "ｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾊﾋﾌﾍﾎﾊﾋﾌﾍﾎｳ";

    static String kana1 = "ヲアイウエオヤユヨツアイウエオカキクケコサシスセソタチツテトナニヌネノ"+
                          "ハヒフヘホマミムメモヤユヨラリルレロワンイエ";

    static String small = "ァィゥェォャュョッぁぃぅぇぉゃゅょっ";
    static String big   = "アイウエオヤユヨツあいうえおやゆよつ";

    static {
        convertAscii   = new Hashtable<IntObject,CharArray>();
        convertBoth   = new Hashtable<IntObject,CharArray>();
        convertKana   = new Hashtable<IntObject,CharArray>();
        convertKanji  = new Hashtable<IntObject,CharArray>();
        convertDakuon = new Hashtable<CharArray,CharArray>();
        // 数値コンバートテーブル
        for (int j = 0; j < convNum.length; j++) {
            String str1 = convNum[j][0];
            String str2 = convNum[j][1];
            CharArray ch2 = new CharArray(str2);
            for (int i = 0; i < str1.length(); i++) {
                char c = str1.charAt(i);
                convertBoth.put(new IntObject(c), ch2);
                convertAscii.put(new IntObject(c), ch2);
            }
        }
        // アスキー文字列コンバート
        for (int i = 0; i < w_ascii.length();i++) {
            char c1 = w_ascii.charAt(i);
            char c2 = ascii.charAt(i);
            char c3 = ascii2.charAt(i);
            convertBoth.put(new IntObject(c1), new CharArray(c2));
            convertAscii.put(new IntObject(c1), new CharArray(c3));
        }
        // 長音コンバート
        CharArray ch = new CharArray('-');
        for (int i = 0; i < minus.length(); i++) {
            char c1 = minus.charAt(i);
            convertBoth.put(new IntObject(c1), ch);
        }
        // ひらがな、カタカナコンバート（カナ用)
        for (int i = 0; i < kana.length(); i++) {
            char c1 = kana.charAt(i);
            char c2 = hira.charAt(i);
            char c3 = hankana.charAt(i);
            char c4 = hankana1.charAt(i);
            char c5 = kana1.charAt(i);
            CharArray ch4 = new CharArray(c4);
            convertKana.put(new IntObject(c1), ch4);
            convertKana.put(new IntObject(c2), ch4);
            convertKana.put(new IntObject(c3), ch4);
            
            convertKanji.put(new IntObject(c3), new CharArray(c5)); // 漢字用
        }
        for (int i = 0; i < kanadaku.length(); i++) {   // 濁音変換
            char c1 = kanadaku.charAt(i);
            char c2 = hiradaku.charAt(i);
            char c3 = hankana2.charAt(i);
            char c4 = kanadaku2.charAt(i);
            char c5 = hiradaku2.charAt(i);
            CharArray ch3 = new CharArray(c3);
            
            IntObject io1 = new IntObject(c1);
            IntObject io2 = new IntObject(c2);
            
            convertKana.put(io1, ch3);
            convertKana.put(io2, ch3);
            
            //toZenkaku用
            convertDakuon.put(new CharArray(hankanadaku[i]),new CharArray(c1));
            
            convertKanji.put(io1, new CharArray(c4));
            convertKanji.put(io2, new CharArray(c5));

        }
        // ひらがな、カタカナコンバート（漢字用)
        for (int i = 0; i < small.length(); i++) {
            char c1 = small.charAt(i);
            char c2 = big.charAt(i);
            convertKanji.put(new IntObject(c1), new CharArray(c2));
        }
    }
    


    /** アスキーコンバート **/
    static public CharArray convertAscii(CharArray str) {
        CharArray ch = new CharArray();
        IntObject obj = IntObject.pop();
        for (int i = 0; i < str.length; i++) {
            char c = str.charAt(i);
            if (('\u0020' <= c && c <= '\u007e')){  // 半角英数
                ch.add(c);
            } else {
                obj.setValue((int)c);
                CharArray tmp = (CharArray)convertAscii.get(obj);
                if (tmp != null) ch.add(tmp);
                else {
                    /** 可逆モード
                    int c1 = (c >>> 10) & 0x3f;           // 左   6ビット
                    int c2 = (c >>> 5)  & 0x1f;           // 真中 5ビット
                    int c3 = c  & 0x1f;                   // 右   5ビット
                    ch.add(ascii2.charAt(c1%0x40));
                    ch.add(ascii2.charAt(c2%0x40));
                    ch.add(ascii2.charAt(c3%0x40));
                    **/
                    int c1 = (c >>> 8) & 0xff;      // 左 8ビット
                    int c2 = (c)  & 0xff;           // 右 8ビット
                    ch.add(ascii2.charAt(c1%0x40));
                    ch.add(ascii2.charAt(c2%0x40));
                }
            }
        }
        IntObject.push(obj);
        return ch;
    }
    
    /**
        よみがなの全角→半角変換を行う。
        使用禁止文字（漢字等）が入った時は null を返す
        @param  str 検索文字列
        @return フリガナ検索で使用する文字列
    */
    static public CharArray convertFurigana(CharArray str) {
        CharArray ch = new CharArray();
        IntObject obj = IntObject.pop();
        for (int i = 0; i < str.length; i++) {
            char c = str.charAt(i);
            if (omitString.indexOf(c) >= 0) {    // 削除文字列は追加しない
                // do nothing
            //} else if ('\u0020' <= c && c <= '\u00df') { // 半角文字（半角カタカナ含む）
            //    if (c >= 'a' && c <= 'z')  c  -= 0x20;     //大文字にする
            //    ch.add(c);
            } else if (w_ascii.indexOf(c) > 0) {    // 全角の英字
                ch = null;
                break;
            } else if (c >= 'A' && c <= 'Z') {
                ch = null;
                break;
            } else if (c >= 'a' && c <= 'z') {
                ch = null;
                break;
            } else {
                obj.setValue((int)c);
                CharArray tmp = (CharArray)convertBoth.get(obj);
                if (tmp == null) {
                    tmp = (CharArray)convertKana.get(obj);
                    if (tmp == null) {
                        //System.out.print("<NG:"+c+">");
                        ch = null;
                        break;
                    }
                }
                ch.add(tmp);
            }
        }
        IntObject.push(obj);
        if (ch != null && ch.trim().length() == 0) ch = null;
        return ch;
    }

    /**
        漢字検索用の文字列変換を行う。
        @param  str 検索文字列
        @return 漢字検索で使用する文字列
    */
    static public CharArray convertKanji(CharArray str) {
        CharArray ch = new CharArray();
        CharArray chTmp = CharArray.pop();
        IntObject obj = IntObject.pop();
        for (int i = 0; i < str.length; i++) {
            char c = str.charAt(i);
            if (omitString.indexOf(c) >= 0) {    // 削除文字列は追加しない
                // do nothing
            } else if (c >= 'a' && c <= 'z') {
                c  -= 0x20;     //大文字にする
                ch.add(c);
            } else {
                obj.setValue((int)c);
                CharArray tmp = null;
                //if (i < str.length-1) {
                //    chTmp.set(c); chTmp.add(str.charAt(i+1));
                //    tmp  = (CharArray)convertDakuon.get(chTmp);
                //}
                //if (tmp == null) {
                    tmp = (CharArray)convertBoth.get(obj);
                    if (tmp == null) {
                        tmp = (CharArray)convertKanji.get(obj);
                    }
                 //}
                 if (tmp != null) ch.add(tmp);
                 else             ch.add(c);
            }
        }
        CharArray.push(chTmp);
        IntObject.push(obj);
        return ch;
    }

    /**
        半角文字を全て全角にする
        @param  str オリジナル文字列
        @return 変換後の新しい文字列
    */
    static public CharArray toZenkaku(CharArray str) {
        CharArray ch = new CharArray();
        CharArray chTmp = CharArray.pop();
        
        IntObject obj = IntObject.pop();
        for (int i = 0; i < str.length; i++) {
            char c = str.charAt(i);
            if ('\u0020' <= c && c <= '\u007f') {        // 半角英数字
                ch.add(zenkaku1.charAt(c-'\u0020'));
            } else if ('\uff61' <= c && c <= '\uff9f') { // 半角カナ
                CharArray tmp = null;
                if (i < str.length()-1) {
                    chTmp.set(c); chTmp.add(str.charAt(i+1));
                    tmp  = (CharArray)convertDakuon.get(chTmp);
                }
                if (tmp != null) {
                    i++;
                    ch.add(tmp);
                } else {
                    ch.add(zenkaku2.charAt(c-'\uff61'));
                }
            } else {
                ch.add(c);
            }
        }
        CharArray.push(chTmp);
        IntObject.push(obj);
        return ch;
    }
    /**
        半角カナを全て全角カナに置き換える
        @param  str オリジナル文字列
        @return 変換後の新しい文字列
    */
    static public CharArray kanaToZenkaku(CharArray str) {
        CharArray ch = new CharArray();
        CharArray chTmp = CharArray.pop();
        
        IntObject obj = IntObject.pop();
        for (int i = 0; i < str.length; i++) {
            char c = str.charAt(i);
            if ('\uff61' <= c && c <= '\uff9f') { // 半角カナ
                CharArray tmp = null;
                if (i < str.length()-1) {
                    chTmp.set(c); chTmp.add(str.charAt(i+1));
                    tmp  = (CharArray)convertDakuon.get(chTmp);
                }
                if (tmp != null) {
                    i++;
                    ch.add(tmp);
                } else {
                    ch.add(zenkaku2.charAt(c-'\uff61'));
                }
            } else {
                ch.add(c);
            }
        }
        CharArray.push(chTmp);
        IntObject.push(obj);
        return ch;
    }
    static public CharArray toZenkaku(String str) {
        CharArray ch = CharArray.pop(str);
        CharArray ret = toZenkaku(ch);
        CharArray.push(ch);
        return ret;
    }

    /** 全角英数記号を半角に */
    static public CharArray toHankaku(CharArray str) {
        if (str == null) return null;
        CharArray ch = new CharArray();
        for (int i = 0; i < str.length(); i++) {
            char c = str.chars[i];
            int index = zenkaku1.indexOf(c);
            if (index >= 0) {
                ch.add((char)(0x20+index));
            } else {
                ch.add(c);
            }
        }
        return ch;
    }
    
    /** チェックデジット */
    static public int checkDigit(int no) {
        //System.out.println("no="+no);
        int num = 0;
        while (no > 0) {
            num += (no % 10) * 3;   // odd
            no /= 10;
            if (no <= 0) break;
            num += (no % 10);       // even
            no /= 10;
        }
        num = 10 - (num % 10);
        num %= 10;
        //System.out.println("digit="+num);
        return num;
    }

    /**
        ワード分割をします
        @param str  オリジナル文字列(変更されない）
        @param size 最低分割文字数
        @param sep  挿入する文字列
        @return 新しい文字列
    */
    static public CharArray separateWord(CharArray str, int size, String sep) {
        CharArray ch = new CharArray();
        int n = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.chars[i];
            if (('\u0020' <= c && c <= '\u007e') || // 半角英数
                ('\uff61' <= c && c <= '\uff9f')) { // 半角カナ
                ++n;
                if (n > size) {
                    ch.add(sep);
                    n = 1;
                }
            } else {    // ２バイト文字
                n += 2;
                if (n > size) {
                    ch.add(sep);
                    n = 2;
                }
            }
            ch.add(c);
        }
        return ch;
    }
    static public CharArray separateWord(String str, int size, String sep) {
        CharArray ch = CharArray.pop(str);
        CharArray ret = separateWord(ch,size,sep);
        CharArray.push(ch);
        return ret;
    }
}

//
// [end of Validation.java]
//
