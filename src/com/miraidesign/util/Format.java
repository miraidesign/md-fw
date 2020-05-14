//------------------------------------------------------------------------
//    Format.java
//              フォーマッター
//              Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.util;

/** 
    高速フォーマッター <br>
    文字列    %s %5s %+05s(右詰め)<br>
    数値(int) %d %5d %-5d(左詰め）%03d（padding by 0）<br>
        %d %i %x %X %L %l<br>
    ※漢字は2文字として計算します<br>

*/

public  class Format {

    /**
        文字列フォーマット
        @param str フォーマット定義
        @param params パラメータ
        @return フォーマット済み文字列
    */
    public static CharArray format(String str, QueueElement params) {
        return format(CharArray.pop(str), params);
    }
    /**
        文字列フォーマット
        @param str フォーマット定義
        @param params パラメータ
        @return フォーマット済み文字列
    */
    public static CharArray format(CharArray str, QueueElement params) {
        CharArray ch = CharArray.pop();
        CharArray num = CharArray.pop();
        int index = 0;
        
        int paramIndex = 0;
        while (index < str.length()) {
            int nextIndex = str.indexOf("%",index);
            if (nextIndex < 0 ||                // 見つからない
                nextIndex == str.length -1) {   // 最後の１文字
                ch.add(str.chars, index, str.length - index);
                break;
            } else if (str.chars[nextIndex+1] == '%') { // １文字とする
                ch.add(str.chars, index, nextIndex - index+1);
                index = nextIndex + 2;
            } else {
                if (nextIndex > index) {        // そこまでを処理しておく
                    ch.add(str.chars, index, nextIndex - index);
                }
                char flg = 0;
                char c = 0;
                //--------------------
                boolean tagConvert = false;  // &
                boolean urlEncode  = false;  // @
                boolean cr2Br      = false;  // $
                boolean cr2Space   = false;  // _
                    
                while (++nextIndex < str.length - 1) {
                    c = str.chars[nextIndex];
                    if (c == '&' && !tagConvert && !urlEncode) {
                        tagConvert = true;
                    } else if (c == '@' && !urlEncode && !tagConvert) {
                        urlEncode = true;
                    } else if (c == '$' && !cr2Br && !cr2Space) {
                        cr2Br = true;
                    } else if (c == '_' && !cr2Space && !cr2Br) {
                        cr2Space = true;
                    } else {
                        break;
                    }
                }
                
                //---------------------
                c = str.chars[nextIndex];
                if (c == '-' || c == '*' || c == '+' || c == ',' || 
                    c == '\\' || c == '￥') {
                    flg = c;
                    nextIndex++;
                }
                num.reset();
                while (nextIndex < str.length - 1) {
                    c = str.chars[nextIndex];
                    if (c >= '0' && c <= '9') {
                        num.add(c);
                        nextIndex++;
                    } else {
                        break;
                    }
                }
                if (nextIndex >= str.length) {
                    ch.add(num);
                    System.out.println("Format parse error!");
                    break;
                }
                int radix  = 0;
                long value = 0;
                switch (str.chars[nextIndex]) {
                    case 's':               // 文字列
                        CharArray strings = CharArray.pop(params.get(paramIndex++));
                        
                        if (tagConvert)   strings.replaceTag();
                        if (cr2Br)        strings.replace("\n","<br>");
                        if (cr2Space)     strings.replace('\n',' ');
                        if (urlEncode)    {
                            String encodeStr = strings.URLEncode("UTF-8");
                            if (encodeStr != null && encodeStr.length() > 0) strings.set(encodeStr);
                        }
                        
                        if (num.length() == 0) {
                            ch.add(strings);
                        } else {
                            int total = num.getInt();
                            int size  = strings.strlen();
                            if (total > size) {
                                if (flg == '+') {
                                    for (int i = 0; i < total-size; i++) {
                                        ch.add(' ');
                                    }
                                }
                                ch.add(strings);
                                if (flg != '+') {
                                    for (int i = 0; i < total-size; i++) {
                                        ch.add(' ');
                                    }
                                }
                            } else {
                                int length = strings.length();
                                for (int i = 1; i < length; i++) {
                                    strings.length = i;
                                    size = strings.strlen();
                                    if (size == total) break;
                                    if (size > total) {
                                        strings.length--;
                                        strings.add(' ');
                                        break;
                                    } 
                                }
                                ch.add(strings);
                                
                            }
                        }
                        CharArray.push(strings);
                        break;
                    case 'd': case 'i':     // 10進値
                    case 'l': case 'L':
                        radix = 10;
                        value = params.getLong(paramIndex++);
                        break;
                    case 'x': case 'X':     // 16進値
                        radix = 16;
                        value = params.getLong(paramIndex++);
                        break;
                    default:
                        System.out.println("Format: パラメータエラー["+str.chars[nextIndex]+"]"+str);
                        ch.add('%');
                        ch.add(num);
                        ch.add(str.chars[nextIndex]);
                        break;
                } // endcase
                if (radix > 0) {
                    if (num.length() == 0) {
                        if (flg == ',') {
                            ch.format(value, radix,-1, ',');
                        } else if (flg == '\\') {
                            ch.add('\\');
                            ch.format(value, radix,-1, ',');
                        } else if (flg == '￥') {
                            ch.add('￥');
                            ch.format(value, radix,-1, ',');
                        } else {
                            ch.format(value, radix);
                        }
                    } else {
                        c = num.chars[0];
                        int total = num.getInt();
                        if (flg == '*') {
                            ch.format(value,radix, total, '*');
                        } else if (flg == ',') {
                            ch.format(value,radix, total, ',');
                        } else if (flg == '\\') {
                            ch.add('\\');
                            ch.format(value,radix, total, ',');
                        } else if (flg == '￥') {
                            ch.add('￥');
                            ch.format(value,radix, total, ',');
                        } else if (flg == '-') {   // 左詰め
                            num.reset();
                            num.format(value,radix);
                            ch.add(num);
                            total -= num.length;
                            for (int i = 0; i < total; i++) {
                                ch.add(' ');
                            }
                        } else {    // 右詰め
                            if (c != '0') c = ' ';
                            ch.format(value,radix, total, c);
                        }
                    }
                }
                index = nextIndex + 1;
            }
            
        }
        CharArray.push(num);
        return ch;
    }
    /**
        文字列フォーマット
        @param str フォーマット定義
        @param table テーブルデータ
        @return フォーマット済み文字列
    */
    public static CharArray format(String str, QueueTable table) {
        return format(new CharArray(str), table, 0, -1,null);
    }
    public static CharArray format(String str, QueueTable table, int start, int max) {
        return format(new CharArray(str), table, start,max,null);
    }
    
    public static CharArray format(CharArray str, QueueTable table) {
        return format(str, table, 0, -1,null);
    }
    public static CharArray format(CharArray str, QueueTable table, int start, int max) {
        return format(str,table,start,max,null);
    }
    /**
        文字列フォーマット
        @param str フォーマット定義
        @param table テーブルデータ
        @param start 開始行
        @param max   最大使用行数
        @param position データ順指定
        @return フォーマット済み文字列
    */
    public static CharArray format(CharArray str, QueueTable table, int start, int max, IntQueue position) {
        CharArray ch = new CharArray();
        CharArray num = CharArray.pop();
        int MAX = table.getRowCount();
        if (max > 0) MAX = Math.min(table.getRowCount(), start + max);
        
        for (int row = start; row < MAX; row++) {
            int index = 0;
            int paramIndex = 0;
            while (index < str.length()) {
                int nextIndex = str.indexOf("%",index);
                if (nextIndex < 0 ||                // 見つからない
                    nextIndex == str.length -1) {   // 最後の１文字
                    ch.add(str.chars, index, str.length - index);
                    break;
                } else if (str.chars[nextIndex+1] == '%') { // １文字とする
                    ch.add(str.chars, index, nextIndex - index+1);
                    index = nextIndex + 2;
                } else {
                    if (nextIndex > index) {        // そこまでを処理しておく
                        ch.add(str.chars, index, nextIndex - index);
                    }
                    char flg = 0;
                    char c = 0;
                    //-------------------- 
                    boolean tagConvert = false;  // &
                    boolean urlEncode  = false;  // #
                    boolean cr2Br      = false;  // $
                    boolean cr2Space   = false;  // _
                    
                    while (++nextIndex < str.length - 1) {
                        c = str.chars[nextIndex];
                        if (c == '&' && !tagConvert && !urlEncode) {
                            tagConvert = true;
                        } else if (c == '@' && !urlEncode && !tagConvert) {
                            urlEncode = true;
                        } else if (c == '$' && !cr2Br && !cr2Space) {
                            cr2Br = true;
                        } else if (c == '_' && !cr2Space && !cr2Br) {
                            cr2Space = true;
                        } else {
                            break;
                        }
                    }
                    
                    //---------------------
                    c = str.chars[nextIndex];
                    if (c == '-' || c == '*' || c == '+' || c == ',' || 
                        c == '\\' || c == '￥') {
                        flg = c;
                        nextIndex++;
                    }
                    
                    num.reset();
                    while (nextIndex < str.length - 1) {
                        c = str.chars[nextIndex];
                        if (c >= '0' && c <= '9') {
                            num.add(c);
                            nextIndex++;
                        } else {
                            break;
                        }
                    }
                    //System.out.println("num="+num);
                    if (nextIndex >= str.length) {
                        ch.add(num);
                        System.out.println("Format parse error!");
                        break;
                    }
                    int radix  = 0;
                    long value = 0;
                    switch (str.chars[nextIndex]) {
                        case 's':               // 文字列
                            CharArray strings = CharArray.pop(table.getCharArray(row,getIndex(position,paramIndex++)));
                            if (tagConvert)   strings.replaceTag();
                            if (cr2Br)        strings.replace("\n","<br>");
                            if (cr2Space)     strings.replace('\n',' ');
                            if (urlEncode)    {
                                String encodeStr = strings.URLEncode("UTF-8");
                                if (encodeStr != null && encodeStr.length() > 0) strings.set(encodeStr);
                            }
                            if (num.length() == 0) {
                                ch.add(strings);
                            } else {
                                int total = num.getInt();
                                int size  = strings.strlen();
                                if (total > size) {
                                    if (flg == '+') {   // 右詰め
                                        for (int i = 0; i < total-size; i++) {
                                            ch.add(' ');
                                        }
                                    }
                                    ch.add(strings);
                                    if (flg != '+') {   // 左詰め
                                        for (int i = 0; i < total-size; i++) {
                                            ch.add(' ');
                                        }
                                    }
                                } else {
                                    int length = strings.length();
                                    for (int i = 1; i < length; i++) {
                                        strings.length = i;
                                        size = strings.strlen();
                                        if (size == total) break;
                                        if (size > total) {
                                            strings.length--;
                                            strings.add(' ');
                                            break;
                                        } 
                                    }
                                    ch.add(strings);
                                }
                            }
                            CharArray.push(strings);
                            break;
                        case 'd': case 'i':     // 10進値
                        case 'l': case 'L':
                            radix = 10;
                            value = table.getLong(row,getIndex(position,paramIndex++)); 
                            break;
                        case 'x': case 'X':     // 16進値
                            radix = 16;
                            value = table.getLong(row,getIndex(position,paramIndex++)); 
                            break;
                        default:
                            System.out.println("Format: パラメータエラー["+str.chars[nextIndex]+"]"+str);
                            ch.add('%');
                            ch.add(num);
                            ch.add(str.chars[nextIndex]);
                            break;
                    } // endcase
                    if (radix > 0) {
                        if (num.length() == 0) {
                            if (flg == ',') {
                                ch.format(value, radix,-1, ',');
                            } else if (flg == '\\') {
                                ch.add('\\');
                                ch.format(value, radix,-1, ',');
                            } else if (flg == '￥') {
                                ch.add('￥');
                                ch.format(value, radix,-1, ',');
                            } else {
                                ch.format(value, radix);
                            }
                        } else {
                            c = num.chars[0];
                            int total = num.getInt();
                            if (flg == '*') {
                                ch.format(value,radix, total, '*');
                            } else if (flg == ',') {
                                ch.format(value,radix, total, ',');
                            } else if (flg == '\\') {
                                ch.add('\\');
                                ch.format(value,radix, total, ',');
                            } else if (flg == '￥') {
                                ch.add('￥');
                                ch.format(value,radix, total, ',');
                            } else if (flg == '-') {   // 左詰め
                                num.reset();
                                num.format(value,radix);
                                ch.add(num);
                                total -= num.length;
                                for (int i = 0; i < total; i++) {
                                    ch.add(' ');
                                }
                            } else {    // 右詰め
                                if (c != '0') c = ' ';
                                ch.format(value,radix, total, c);
                            }
                        }
                    }
                    index = nextIndex + 1;
                }
            }
        }
        CharArray.push(num);
        return ch;
    }

    private static int getIndex(IntQueue position, int index) {
        if (position == null || (position.size()-1) < index) {
            return index;
        }
        return position.peek(index);
    }

    /**
        文字列フォーマット(キーワード対応）<br>
        %s:key1 %02d:key2 のように設定します
        @param str フォーマット定義
        @param hp パラメータ
        @return フォーマット済み文字列
    */
    public static CharArray format(String str, HashParameter hp) {
        return format(CharArray.pop(str), hp);
    }
    /**
        文字列フォーマット(キーワード対応）<br>
        %s:key1 %02d:key2 のように設定します
        @param str フォーマット定義
        @param hp パラメータ
        @return フォーマット済み文字列
    */
    public static CharArray format(CharArray str, HashParameter hp) {
        CharArray ch = CharArray.pop();
        CharArray num = CharArray.pop();
        int index = 0;
        
        int paramIndex = 0;
        while (index < str.length()) {
            int nextIndex = str.indexOf("%",index);
            if (nextIndex < 0 ||                // 見つからない
                nextIndex == str.length -1) {   // 最後の１文字
                ch.add(str.chars, index, str.length - index);
                break;
            } else if (str.chars[nextIndex+1] == '%') { // １文字とする
                ch.add(str.chars, index, nextIndex - index+1);
                index = nextIndex + 2;
            } else {
                if (nextIndex > index) {        // そこまでを処理しておく
                    ch.add(str.chars, index, nextIndex - index);
                }
                char flg = 0;
                char c = 0;
                //-------------------- 
                boolean tagConvert = false;  // &
                boolean urlEncode  = false;  // @
                boolean cr2Br      = false;  // $
                boolean cr2Space   = false;  // _
                    
                while (++nextIndex < str.length - 1) {
                    c = str.chars[nextIndex];
                    if (c == '&' && !tagConvert && !urlEncode) {
                        tagConvert = true;
                    } else if (c == '@' && !urlEncode && !tagConvert) {
                        urlEncode = true;
                    } else if (c == '$' && !cr2Br && !cr2Space) {
                        cr2Br = true;
                    } else if (c == '_' && !cr2Space && !cr2Br) {
                        cr2Space = true;
                    } else {
                        break;
                    }
                }
                
                //---------------------
                c = str.chars[nextIndex];
                if (c == '-' || c == '*' || c == '+' || c == ',' || 
                    c == '\\' || c == '￥') {
                    flg = c;
                    nextIndex++;
                }
                num.reset();
                while (nextIndex < str.length - 1) {
                    c = str.chars[nextIndex];
                    if (c >= '0' && c <= '9') {
                        num.add(c);
                        nextIndex++;
                    } else {
                        break;
                    }
                }
                //System.out.println("num="+num);
                if (nextIndex >= str.length) {
                    ch.add(num);
                    System.out.println("Format parse error!");
                    break;
                }
                
                char unit = str.chars[nextIndex++];
                
                if (nextIndex >= str.length || str.chars[nextIndex] != ':') {
                    System.out.println("Format: パースエラー"+ch);
                    ch.add('%');
                    ch.add(num);
                    ch.add(unit);
                    break;
                }
                nextIndex++;
                CharArray _key   = null;
                CharArray _value = null;
                for (int i = 0; i < hp.size(); i++) {
                    CharArray key = hp.keyElementAt(i);
                    int idx = str.searchWord(key, nextIndex);
                    if (idx == nextIndex) {
                        _key   = key;
                        _value = hp.valueElementAt(i);
                        nextIndex += _key.length();
                        break;
                    }
                }
                if (_key == null) {
                    System.out.println("Format: パースエラー(key not found!!)"+ch);
                    ch.add('%');
                    ch.add(num);
                    ch.add(unit);
                    ch.add(":");
                    break;
                }
                int radix  = 0;
                long value = 0;
                switch (unit) {
                    case 's':               // 文字列
                        CharArray strings = CharArray.pop(_value);
                        
                        if (tagConvert)   strings.replaceTag();
                        if (cr2Br)        strings.replace("\n","<br>");
                        if (cr2Space)     strings.replace('\n',' ');
                        if (urlEncode)    {
                            String encodeStr = strings.URLEncode("UTF-8");
                            if (encodeStr != null && encodeStr.length() > 0) strings.set(encodeStr);
                        }
                        
                        if (num.length() == 0) {
                            ch.add(strings);
                        } else {
                            int total = num.getInt();
                            int size  = strings.strlen();
                            if (total > size) {
                                if (flg == '+') {
                                    for (int i = 0; i < total-size; i++) {
                                        ch.add(' ');
                                    }
                                }
                                ch.add(strings);
                                if (flg != '+') {
                                    for (int i = 0; i < total-size; i++) {
                                        ch.add(' ');
                                    }
                                }
                            } else {
                                int length = strings.length();
                                for (int i = 1; i < length; i++) {
                                    strings.length = i;
                                    size = strings.strlen();
                                    if (size == total) break;
                                    if (size > total) {
                                        strings.length--;
                                        strings.add(' ');
                                        break;
                                    } 
                                }
                                ch.add(strings);
                            }
                        }
                        CharArray.push(strings);
                        break;
                    case 'd': case 'i':     // 10進値
                    case 'l': case 'L':
                        radix = 10;
                        value = _value.getLong();
                        break;
                    case 'x': case 'X':     // 16進値
                        radix = 16;
                        value = _value.getLong();
                        break;
                    default:
                        System.out.println("Format: パラメータエラー["+unit+"]"+str);
                        ch.add('%');
                        ch.add(num);
                        ch.add(unit);
                        ch.add(_key);
                        break;
                } // endcase
                
                if (radix > 0) {
                    if (num.length() == 0) {
                        if (flg == ',') {
                            ch.format(value, radix,-1, ',');
                        } else if (flg == '\\') {
                            ch.add('\\');
                            ch.format(value, radix,-1, ',');
                        } else if (flg == '￥') {
                            ch.add('￥');
                            ch.format(value, radix,-1, ',');
                        } else {
                            ch.format(value, radix);
                        }
                    } else {
                        c = num.chars[0];
                        int total = num.getInt();
                        if (flg == '*') {
                            ch.format(value,radix, total, '*');
                        } else if (flg == ',') {
                            ch.format(value,radix, total, ',');
                        } else if (flg == '\\') {
                            ch.add('\\');
                            ch.format(value,radix, total, ',');
                        } else if (flg == '￥') {
                            ch.add('￥');
                            ch.format(value,radix, total, ',');
                        } else if (flg == '-') {   // 左詰め
                            num.reset();
                            num.format(value,radix);
                            ch.add(num);
                            total -= num.length;
                            for (int i = 0; i < total; i++) {
                                ch.add(' ');
                            }
                        } else {    // 右詰め
                            if (c != '0') c = ' ';
                            ch.format(value,radix, total, c);
                        }
                    }
                } // endif
                index = nextIndex;  // + 1;
            }
            
        } // enddo
        CharArray.push(num);
        return ch;
    }
}
//
//
// [end of Format.java]
//

