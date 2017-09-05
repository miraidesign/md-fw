//------------------------------------------------------------------------
// @(#)ContentSubFunction.java
//              コンテンツサブ関数
//              Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//              
//------------------------------------------------------------------------

package com.miraidesign.content;

import com.miraidesign.common.SystemConst;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.Parameter;

/**
 *  ContentSubFunction
 *  
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class ContentSubFunction {
    private boolean debug = (SystemConst.debug && false);  // デバッグ表示
    
    // SINGLETON
    static ContentSubFunction instance = null;
    static public ContentSubFunction getInstance() {
        if (instance == null) instance = new ContentSubFunction();
        return instance;
    }
    
    private ContentSubFunction() {}
    
    public CharArray call(CharArray input, CharArray func, CharArray params, SessionObject session) {
if (debug) {
        System.out.println("▼▼------------------------------------------------------▼▼");
        System.out.println("★ContentSubFunction:call("+input+","+func+","+params+")");
}
        CharToken token = CharToken.pop();
        token.set(params,",");
    
        if (debug) {
            CharArray msg = CharArray.pop();
            msg.add("SubFunction#call(");
            msg.add(func);
/*
            if (params != null && params.size() > 0) {
                for (int i = 0; i < params.size(); i++) {
                    if (i != 0) msg.add(", ");
                    msg.add(params.peek(i));
                }
            }
*/
            msg.add(",");
            msg.add(params);
            msg.add(") input=");
            msg.add(input);
            session.println(msg.toString());
            CharArray.push(msg);
        }
        
        if (input == null || func == null || func.length()==0) return input;
        //CharArray output = CharArray.pop(input);
        CharArray output = input;
        
        CharArray _func = CharArray.pop(func);
        _func.trim(); _func.replace("_","");
        CharArray param1 = token.get(0);
        CharArray param2 = token.get(1);

        // ---------- 関数定義----------------------------------------
        if      (_func.equals("FOO"))           ; // do nothing
        else if (_func.equals("BAR"))           ; // do nothing
        //------------------------------------------------------------
        // 文字列変換タイプ(param)
        //------------------------------------------------------------
        else if (_func.equals("URLENCODE"))     output = func_urlEncode(output, param1);
        else if (_func.equals("URLDECODE"))     output = func_urlDecode(output, param1);
        else if (_func.equals("TAGCONVERT"))    output = func_tagConvert(output);
        else if (_func.equals("CR2BR"))         output = func_cr2br(output);
        else if (_func.equals("CR2SPC"))        output = func_cr2spc(output);
        else if (_func.equals("TRIM"))          output = func_trim(output);
        else if (_func.equals("TRIMR"))         output = func_trimR(output);
        else if (_func.equals("TRIML"))         output = func_trimL(output);
        else if (_func.equals("ADD"))           output = func_add(output, param1);
        else if (_func.equals("INSERT"))        output = func_insert(output, param1);
        else if (_func.equals("REPLACE"))       output = func_replace(output, param1, param2);
        else if (_func.equals("TOUPPERCASE"))   output = func_toUpperCase(output);
        else if (_func.equals("TOLOWERCASE"))   output = func_toLowerCase(output);
        else if (_func.equals("SUBSTR"))        output = func_substring(output, param1, param2);
        else if (_func.equals("SUBSTRING"))     output = func_substring(output, param1, param2);
        
        //------------------------------------------------------------
        // 文字列演算系(bool)
        //------------------------------------------------------------
        else if (_func.equals("EQUALS"))        output = func_equals(output, param1);
        else if (_func.equals("NOTEQUALS"))     output = func_not_equals(output, param1);
        else if (_func.equals("STARTSWITH"))    output = func_startswith(output, param1);
        else if (_func.equals("NOTSTARTSWITH")) output = func_not_startswith(output, param1);
        else if (_func.equals("ENDSWITH"))      output = func_endswith(output, param1);
        else if (_func.equals("NOTENDSWITH"))   output = func_not_endswith(output, param1);

        //------------------------------------------------------------
        // 文字列条件変換演算系(param)
        //------------------------------------------------------------
        else if (_func.equals("EQUALSADD"))      output = func_equals_add(output, param1, param2);
        else if (_func.equals("NOTEQUALSADD"))   output = func_not_equals_add(output, param1, param2);
        else if (_func.equals("EQUALSINSERT") )  output = func_equals_insert(output, param1, param2);
        else if (_func.equals("NOTEQUALSINSERT"))output = func_not_equals_insert(output, param1, param2);

        else if (_func.equals("STARTSWITHADD"))      output = func_startswith_add(output, param1, param2);
        else if (_func.equals("NOTSTARTSWITHADD"))   output = func_not_startswith_add(output, param1, param2);
        else if (_func.equals("STARTSWITHINSERT") )  output = func_startswith_insert(output, param1, param2);
        else if (_func.equals("NOTSTARTSWITHINSERT"))output = func_not_startswith_insert(output, param1, param2);
        else if (_func.equals("ENDSWITHADD"))      output = func_endswith_add(output, param1, param2);
        else if (_func.equals("NOTENDSWITHADD"))   output = func_not_endswith_add(output, param1, param2);
        else if (_func.equals("ENDSWITHINSERT") )  output = func_endswith_insert(output, param1, param2);
        else if (_func.equals("NOTENDSWITHINSERT"))output = func_not_endswith_insert(output, param1, param2);
        
        //------------------------------------------------------------
        // 演算系(bool)
        //------------------------------------------------------------
        else if (_func.equals("EQUAL"))         output = func_eq(output, param1);
        else if (_func.equals("EQ"))            output = func_eq(output, param1);
        else if (_func.equals("NE"))            output = func_ne(output, param1);
        else if (_func.equals("GT"))            output = func_gt(output, param1);
        else if (_func.equals("GE"))            output = func_ge(output, param1);
        else if (_func.equals("LT"))            output = func_lt(output, param1);
        else if (_func.equals("LE"))            output = func_le(output, param1);
        
        //------------------------------------------------------------
        // 演算系(param)
        //------------------------------------------------------------
        else if (_func.equals("INDEXOF"))       output = func_indexof(output, param1);
        else if (_func.equals("LENGTH"))        output = func_length(output, param1);
        else if (_func.equals("SIZE"))          output = func_size(output, param1);
        
        else if (_func.equals("PLUS"))          output = func_plus(output, param1);
        else if (_func.equals("MINUS"))         output = func_minus(output, param1);
        else if (_func.equals("MUL"))           output = func_mul(output, param1);
        else if (_func.equals("DIV"))           output = func_div(output, param1);
        else if (_func.equals("MOD"))           output = func_mod(output, param1);
        else if (_func.equals("AND"))           output = func_and(output, param1);
        else if (_func.equals("OR"))            output = func_or(output, param1);
        else if (_func.equals("XOR"))           output = func_xor(output, param1);
        else if (_func.equals("NOT"))           output = func_not(output, param1);

        // debug
        else if (_func.equals("REV"))           output = func_rev(output, param1, session);

        //------------------------------------------------------------
        // データ保管(param)
        //------------------------------------------------------------
        else if (_func.equals("SETPARAMETER"))   output = func_setParameter(output, param1, session);
        else if (_func.equals("ADDPARAMETER"))   output = func_addParameter(output, param1, session);
        else if (_func.equals("SETUSERDATA"))    output = func_setUserData(output, param1, session);
        else if (_func.equals("ADDUSERDATA"))    output = func_addUserData(output, param1, session);
        else if (_func.equals("SETUSERTEMPLATE"))   output = func_setUserTemplate(output, param1, session);
        else if (_func.equals("ADDUSERTEMPLATE"))   output = func_addUserTemplate(output, param1, session);
        
        // -----------------------------------------------------------
        CharArray.push(_func);
        CharToken.push(token);
if (debug) System.out.println("▲▲-------------------------------------------------------▲▲");
        return output;
    }
    // リスト用の処理
    public Parameter call(Parameter input, CharArray func, CharArray params, SessionObject session) {
if (debug) {
        System.out.println("▼▼------------------------------------------------------▼▼");
        System.out.println("★ContentSubFunction:call(p,"+func+","+params+")");
}
        CharToken token = CharToken.pop();
        token.set(params,",");
    
        if (debug) {
            CharArray msg = CharArray.pop();
            msg.add("SubFunction#call(p,");
            msg.add(func);
/*
            if (params != null && params.size() > 0) {
                for (int i = 0; i < params.size(); i++) {
                    if (i != 0) msg.add(", ");
                    msg.add(params.peek(i));
                }
            }
*/
            msg.add(",");
            msg.add(params);
            msg.add(")");
            session.println(msg.toString());
            CharArray.push(msg);
        }
        
        if (input == null || func == null || func.length()==0) return input;
        //CharArray output = CharArray.pop(input);
        Parameter output = input;
        
        CharArray _func = CharArray.pop(func);
        _func.trim(); _func.replace("_","");
        CharArray param1 = token.get(0);
        CharArray param2 = token.get(1);

        // ---------- 関数定義----------------------------------------
        if      (_func.equals("FOO"))           ; // do nothing
        else if (_func.equals("BAR"))           ; // do nothing
        //------------------------------------------------------------
        // 文字列変換タイプ(param)
        //------------------------------------------------------------
        else if (_func.equals("URLENCODE"))     output = func_urlEncode(output, param1);
        else if (_func.equals("URLDECODE"))     output = func_urlDecode(output, param1);
        else if (_func.equals("TAGCONVERT"))    output = func_tagConvert(output);
        else if (_func.equals("CR2BR"))         output = func_cr2br(output);
        else if (_func.equals("CR2SPC"))        output = func_cr2spc(output);
        else if (_func.equals("TRIM"))          output = func_trim(output);
        else if (_func.equals("TRIMR"))         output = func_trimR(output);
        else if (_func.equals("TRIML"))         output = func_trimL(output);
        else if (_func.equals("ADD"))           output = func_add(output, param1);
        else if (_func.equals("INSERT"))        output = func_insert(output, param1);
        else if (_func.equals("REPLACE"))       output = func_replace(output, param1, param2);
        else if (_func.equals("TOUPPERCASE"))   output = func_toUpperCase(output);
        else if (_func.equals("TOLOWERCASE"))   output = func_toLowerCase(output);
        else if (_func.equals("SUBSTR"))        output = func_substring(output, param1, param2);
        else if (_func.equals("SUBSTRING"))     output = func_substring(output, param1, param2);
        
        //------------------------------------------------------------
        // 文字列演算系(bool)
        //------------------------------------------------------------
        else if (_func.equals("EQUALS"))        output = func_equals(output, param1);
        else if (_func.equals("NOTEQUALS"))     output = func_not_equals(output, param1);
        else if (_func.equals("STARTSWITH"))    output = func_startswith(output, param1);
        else if (_func.equals("NOTSTARTSWITH")) output = func_not_startswith(output, param1);
        else if (_func.equals("ENDSWITH"))      output = func_endswith(output, param1);
        else if (_func.equals("NOTENDSWITH"))   output = func_not_endswith(output, param1);

        //------------------------------------------------------------
        // 文字列条件変換演算系(param)
        //------------------------------------------------------------
        else if (_func.equals("EQUALSADD"))      output = func_equals_add(output, param1, param2);
        else if (_func.equals("NOTEQUALSADD"))   output = func_not_equals_add(output, param1, param2);
        else if (_func.equals("EQUALSINSERT") )  output = func_equals_insert(output, param1, param2);
        else if (_func.equals("NOTEQUALSINSERT"))output = func_not_equals_insert(output, param1, param2);

        else if (_func.equals("STARTSWITHADD"))      output = func_startswith_add(output, param1, param2);
        else if (_func.equals("NOTSTARTSWITHADD"))   output = func_not_startswith_add(output, param1, param2);
        else if (_func.equals("STARTSWITHINSERT") )  output = func_startswith_insert(output, param1, param2);
        else if (_func.equals("NOTSTARTSWITHINSERT"))output = func_not_startswith_insert(output, param1, param2);
        else if (_func.equals("ENDSWITHADD"))      output = func_endswith_add(output, param1, param2);
        else if (_func.equals("NOTENDSWITHADD"))   output = func_not_endswith_add(output, param1, param2);
        else if (_func.equals("ENDSWITHINSERT") )  output = func_endswith_insert(output, param1, param2);
        else if (_func.equals("NOTENDSWITHINSERT"))output = func_not_endswith_insert(output, param1, param2);

        //------------------------------------------------------------
        // 演算系(bool)
        //------------------------------------------------------------
        else if (_func.equals("EQUAL"))         output = func_eq(output, param1);
        else if (_func.equals("EQ"))            output = func_eq(output, param1);
        else if (_func.equals("NE"))            output = func_ne(output, param1);
        else if (_func.equals("GT"))            output = func_gt(output, param1);
        else if (_func.equals("GE"))            output = func_ge(output, param1);
        else if (_func.equals("LT"))            output = func_lt(output, param1);
        else if (_func.equals("LE"))            output = func_le(output, param1);

        //------------------------------------------------------------
        // 演算系(param)
        //------------------------------------------------------------
        else if (_func.equals("INDEXOF"))       output = func_indexof(output, param1);
        else if (_func.equals("LENGTH"))        output = func_length(output, param1);

        else if (_func.equals("PLUS"))          output = func_plus(output, param1);
        else if (_func.equals("MINUS"))         output = func_minus(output, param1);
        else if (_func.equals("MUL"))           output = func_mul(output, param1);
        else if (_func.equals("DIV"))           output = func_div(output, param1);
        else if (_func.equals("MOD"))           output = func_mod(output, param1);
        else if (_func.equals("AND"))           output = func_and(output, param1);
        else if (_func.equals("OR"))            output = func_or(output, param1);
        else if (_func.equals("XOR"))           output = func_xor(output, param1);
        else if (_func.equals("NOT"))           output = func_not(output, param1);

        // debug
        else if (_func.equals("REV"))           output = func_rev(output, param1, session);

        //------------------------------------------------------------
        // データ保管(param)
        //------------------------------------------------------------
        else if (_func.equals("SETPARAMETER"))   output = func_setParameter(output, param1, session);
        else if (_func.equals("ADDPARAMETER"))   output = func_addParameter(output, param1, session);
        else if (_func.equals("SETUSERDATA"))    output = func_setUserData(output, param1, session);
        else if (_func.equals("ADDUSERDATA"))    output = func_addUserData(output, param1, session);
        else if (_func.equals("SETUSERTEMPLATE"))   output = func_setUserTemplate(output, param1, session);
        else if (_func.equals("ADDUSERTEMPLATE"))   output = func_addUserTemplate(output, param1, session);
        
        // -----------------------------------------------------------
        CharArray.push(_func);
        CharToken.push(token);
if (debug) System.out.println("▲▲-------------------------------------------------------▲▲");
        return output;
    }
    //----------------------------------------------------------
    // 文字列変換タイプ
    //----------------------------------------------------------
    //param   url_encode()        文字列をURLエンコードする
    protected CharArray func_urlEncode(CharArray input, CharArray encode) {
        try {
            String enc = (encode != null) ? encode.toString() : "";
            if (input.toString() != null) {
                String str =  java.net.URLEncoder.encode(input.toString(), enc);
                input.set(str);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return input;
    }
    protected Parameter func_urlEncode(Parameter input, CharArray encode) {
        try {
            String enc = (encode != null) ? encode.toString() : "";
            String str =  java.net.URLEncoder.encode(input.toString(), enc);
            for (int i = 0; i < input.size(); i++) {
                input.peek(i).set(str);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return input;
    }
    //param   url_decode()        文字列をURLデコードする
    protected CharArray func_urlDecode(CharArray input, CharArray encode) {
        try {
            String enc = (encode != null) ? encode.toString() : "";
            if (input.toString() != null) {
                String str =  java.net.URLDecoder.decode(input.toString(), enc);
                input.set(str);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return input;
    }
    protected Parameter func_urlDecode(Parameter input, CharArray encode) {
        try {
            String enc = (encode != null) ? encode.toString() : "";
            String str =  java.net.URLDecoder.decode(input.toString(), enc);
            for (int i = 0; i < input.size(); i++) {
                input.peek(i).set(str);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return input;
    }
    //param   tag_convert()       タグ文字をコンバートする(CSS対策)
    protected CharArray func_tagConvert(CharArray input) {
        input.replaceTag();
        return input;
    }
    protected Parameter func_tagConvert(Parameter input) {
        for (int i = 0; i < input.size(); i++) {
            input.peek(i).replaceTag();
        }
        return input;
    }
    
    //param   cr2br()             改行文字を<br>に変換する
    protected CharArray func_cr2br(CharArray input) {
        input.replace("\n","<br>");
        return input;
    }
    protected Parameter func_cr2br(Parameter input) {
        for (int i = 0; i < input.size(); i++) {
            input.peek(i).replace("\n","<br>");
        }
        return input;
    }
    
    //param   cr2spc()            改行文字を半角スペースに変換する
    protected CharArray func_cr2spc(CharArray input) {
        input.replace("\n"," ");
        return input;
    }
    protected Parameter func_cr2spc(Parameter input) {
        for (int i = 0; i < input.size(); i++) {
            input.peek(i).replace("\n"," ");
        }
        return input;
    }
    //param   trim()              前後の空白を取る
    protected CharArray func_trim(CharArray input) {
        input.trim();
        return input;
    }
    protected Parameter func_trim(Parameter input) {
        for (int i = 0; i < input.size(); i++) {
            input.peek(i).trim();
        }
        return input;
    }
    //param   trimR()              右側の空白を取る
    protected CharArray func_trimR(CharArray input) {
        input.trimR();
        return input;
    }
    protected Parameter func_trimR(Parameter input) {
        for (int i = 0; i < input.size(); i++) {
            input.peek(i).trimR();
        }
        return input;
    }
    //param   trimL()              左側の空白を取る
    protected CharArray func_trimL(CharArray input) {
        input.trimL();
        return input;
    }
    protected Parameter func_trimL(Parameter input) {
        for (int i = 0; i < input.size(); i++) {
            input.peek(i).trimL();
        }
        return input;
    }
    
    //param   add(str)             文字列を追加する
    protected CharArray func_add(CharArray input, CharArray str) {
if (debug) System.out.println("★文字追加ADD("+input+":"+str+")");
        input.add(str);
        return input;
    }
    protected Parameter func_add(Parameter input, CharArray str) {
if (debug) System.out.println("★文字追加ADD("+input+":"+str+")");
        for (int i = 0; i < input.size(); i++) {
            input.peek(i).add(str);
        }
        return input;
    }
    
    //param   insert(str)          文字列を先頭に挿入する
    protected CharArray func_insert(CharArray input, CharArray str) {
        input.insert(0,str);
        return input;
    }
    protected Parameter func_insert(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            input.peek(i).insert(0,str);
        }
        return input;
    }
    //param   replace(str1,str2)   文字列を置換する
    protected CharArray func_replace(CharArray input, CharArray str1, CharArray str2) {
        input.replace(str1, str2);
        return input;
    }
    protected Parameter func_replace(Parameter input, CharArray str1, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            input.peek(i).replace(str1, str2);
        }
        return input;
    }
    
    //param   toUpperCase()        半角英字を大文字変換します
    protected CharArray func_toUpperCase(CharArray input) {
        input.toUpperCase();
        return input;
    }
    protected Parameter func_toUpperCase(Parameter input) {
        for (int i = 0; i < input.size(); i++) {
            input.peek(i).toUpperCase();
        }
        return input;
    }
    //param   toLowerCase()        半角英字を小文字変換します
    protected CharArray func_toLowerCase(CharArray input) {
        input.toLowerCase();
        return input;
    }
    protected Parameter func_toLowerCase(Parameter input) {
        for (int i = 0; i < input.size(); i++) {
            input.peek(i).toLowerCase();
        }
        return input;
    }
    
    // 文字列の一部を取り出します
    protected CharArray func_substring(CharArray input, CharArray str1, CharArray str2) {
        if (str2 != null && str2.trim().length() > 0) {
            input.substring(CharArray.getInt(str1), str2.getInt());
        } else {
            input.substring(CharArray.getInt(str1));
        }
        return input;
    }
    protected Parameter func_substring(Parameter input, CharArray str1, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            if (str2 != null && str2.trim().length() > 0) {
                input.peek(i).substring(CharArray.getInt(str1), str2.getInt());
            } else {
                input.peek(i).substring(CharArray.getInt(str1));
            }
        }
        return input;
    }
    
    //----------------------------------------------------------
    // 文字列演算系(bool)
    //----------------------------------------------------------
    //bool    equals(str)     文字列演算  
    protected CharArray func_equals(CharArray input, CharArray str) {
        if (input.equals(str)) input.set("true");
        else                   input.set("");
        return input;
    }
    protected Parameter func_equals(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.equals(str)) ch.set("true");
            else                ch.set("");
        }
        return input;
    }
    //bool    not_equals(str)     文字列演算  
    protected CharArray func_not_equals(CharArray input, CharArray str) {
        if (input.equals(str)) input.set("");
        else                   input.set("true");
        return input;
    }
    protected Parameter func_not_equals(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.equals(str)) ch.set("");
            else                ch.set("true");
        }
        return input;
    }
    //bool    startsWith(str)     文字列演算  
    protected CharArray func_startswith(CharArray input, CharArray str) {
        if (input.startsWith(str)) input.set("true");
        else                   input.set("");
        return input;
    }
    protected Parameter func_startswith(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.startsWith(str)) ch.set("true");
            else                    ch.set("");
        }
        return input;
    }
    //bool    !startsWith(str)     文字列演算  
    protected CharArray func_not_startswith(CharArray input, CharArray str) {
        if (input.startsWith(str)) input.set("");
        else                   input.set("true");
        return input;
    }
    protected Parameter func_not_startswith(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.startsWith(str)) ch.set("");
            else                    ch.set("true");
        }
        return input;
    }
    
    //bool    endsWith(str)     文字列演算  
    protected CharArray func_endswith(CharArray input, CharArray str) {
        if (input.endsWith(str)) input.set("true");
        else                     input.set("");
        return input;
    }
    protected Parameter func_endswith(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.startsWith(str)) ch.set("true");
            else                    ch.set("");
        }
        return input;
    }
    //bool    !endsWith(str)     文字列演算  
    protected CharArray func_not_endswith(CharArray input, CharArray str) {
        if (input.endsWith(str)) input.set("");
        else                   input.set("true");
        return input;
    }
    protected Parameter func_not_endswith(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.endsWith(str)) ch.set("");
            else                  ch.set("true");
        }
        return input;
    }
    //----------------------------------------------------------
    // 文字列条件変換系(param)
    //----------------------------------------------------------
    //param    equals_add(str,str2)     文字列演算  
    protected CharArray func_equals_add(CharArray input, CharArray str, CharArray str2) {
        if (input.equals(str)) input.add(str2);
        return input;
    }
    protected Parameter func_equals_add(Parameter input, CharArray str, CharArray str2) {
//System.out.println("◇◇func_equals.add("+str+","+str2+")◇◇◇◇◇◇◇◇◇◇◇◇◇");
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.equals(str)) {
//System.out.println("◇equals.add("+ch+","+str2+")追加する");
                ch.add(str2);
            } else {
//System.out.println("◆equals.add("+ch+","+str2+")追加しない");
            
            }
        }
        return input;
    }
    //param    not_equals_add(str,str2)     文字列演算  
    protected CharArray func_not_equals_add(CharArray input, CharArray str, CharArray str2) {
        if (!input.equals(str)) input.add(str2);
        return input;
    }
    protected Parameter func_not_equals_add(Parameter input, CharArray str, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (!ch.equals(str)) ch.add(str2);
        }
        return input;
    }
    //param    equals_insert(str,str2)     文字列演算  
    protected CharArray func_equals_insert(CharArray input, CharArray str, CharArray str2) {
        if (input.equals(str)) input.insert(0,str2);
        return input;
    }
    protected Parameter func_equals_insert(Parameter input, CharArray str, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.equals(str)) ch.insert(0, str2);
        }
        return input;
    }
    //param    not_equals_insert(str,str2)     文字列演算  
    protected CharArray func_not_equals_insert(CharArray input, CharArray str, CharArray str2) {
        if (!input.equals(str)) input.insert(0,str2);
        return input;
    }
    protected Parameter func_not_equals_insert(Parameter input, CharArray str, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (!ch.equals(str)) ch.insert(0, str2);
        }
        return input;
    }
    //--------------------------------    
    //param    startsWith_add(str, str2)     文字列演算  
    protected CharArray func_startswith_add(CharArray input, CharArray str, CharArray str2) {
        if (input.startsWith(str)) input.add(str2);
        return input;
    }
    protected Parameter func_startswith_add(Parameter input, CharArray str, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.startsWith(str)) ch.add(str2);
        }
        return input;
    }
    //param    not_startsWith_add(str, str2)     文字列演算  
    protected CharArray func_not_startswith_add(CharArray input, CharArray str, CharArray str2) {
        if (!input.startsWith(str)) input.add(str2);
        return input;
    }
    protected Parameter func_not_startswith_add(Parameter input, CharArray str, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (!ch.startsWith(str)) ch.add(str2);
        }
        return input;
    }
    //--------------------------------    
    //param    startsWith_insert(str, str2)     文字列演算  
    protected CharArray func_startswith_insert(CharArray input, CharArray str, CharArray str2) {
        if (input.startsWith(str)) input.insert(0,str2);
        return input;
    }
    protected Parameter func_startswith_insert(Parameter input, CharArray str, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.startsWith(str)) ch.insert(0,str2);
        }
        return input;
    }
    //param    not_startsWith_insert(str, str2)     文字列演算  
    protected CharArray func_not_startswith_insert(CharArray input, CharArray str, CharArray str2) {
//System.out.println("☆notStartWithInsert:"+input+"["+str+"]"+str2);
        if (!input.startsWith(str)) input.insert(0,str2);
        return input;
    }
    protected Parameter func_not_startswith_insert(Parameter input, CharArray str, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
//System.out.println("★notStartWithInsert:"+ch+"["+str+"]"+str2);
            if (!ch.startsWith(str)) ch.insert(0,str2);
        }
        return input;
    }
    //--------------------------------    
    //param    endsWith_add(str, str2)     文字列演算  
    protected CharArray func_endswith_add(CharArray input, CharArray str, CharArray str2) {
        if (input.endsWith(str)) input.add(str2);
        return input;
    }
    protected Parameter func_endswith_add(Parameter input, CharArray str, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.endsWith(str)) ch.add(str2);
        }
        return input;
    }
    //param    not_endsWith_add(str, str2)     文字列演算  
    protected CharArray func_not_endswith_add(CharArray input, CharArray str, CharArray str2) {
        if (!input.endsWith(str)) input.add(str2);
        return input;
    }
    protected Parameter func_not_endswith_add(Parameter input, CharArray str, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (!ch.endsWith(str)) ch.add(str2);
        }
        return input;
    }
    //param    endsWith_insert(str, str2)     文字列演算  
    protected CharArray func_endswith_insert(CharArray input, CharArray str, CharArray str2) {
        if (input.endsWith(str)) input.insert(0,str2);
        return input;
    }
    protected Parameter func_endswith_insert(Parameter input, CharArray str, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch.endsWith(str)) ch.insert(0,str2);
        }
        return input;
    }
    //param    not_endsWith_insert(str, str2)     文字列演算  
    protected CharArray func_not_endswith_insert(CharArray input, CharArray str, CharArray str2) {
        if (!input.endsWith(str)) input.insert(0,str2);
        return input;
    }
    protected Parameter func_not_endswith_insert(Parameter input, CharArray str, CharArray str2) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (!ch.endsWith(str)) ch.insert(0,str2);
        }
        return input;
    }
    
    //----------------------------------------------------------
    // 演算系(bool)
    //----------------------------------------------------------
    //bool    eq(num)      数値演算    イコールで'true'を返す else ''
    protected CharArray func_eq(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        if (org == num && input.equals(str)) input.set("true");
        else                                 input.set("");
        return input;
    }
    protected Parameter func_eq(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            if (org == num && ch.equals(str)) ch.set("true");
            else                              ch.set("");
        }
        return input;
    }
    
    //bool    ne(num)      数値演算    ノットイコールで'true'を返す else ''
    protected CharArray func_ne(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        if (org != num) input.set("true");
        else            input.set("");
        return input;
    }
    protected Parameter func_ne(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            if (org != num) ch.set("true");
            else            ch.set("");
        }
        return input;
    }
    //bool    gt(num)         ＞
    protected CharArray func_gt(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        if (org > num ) input.set("true");
        else            input.set("");
        return input;
    }
    protected Parameter func_gt(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            if (org > num ) ch.set("true");
            else            ch.set("");
        }
        return input;
    }
    //bool    ge(num)         ＞＝
    protected CharArray func_ge(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        if (org >= num ) input.set("true");
        else             input.set("");
        return input;
    }
    protected Parameter func_ge(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            if (org >= num ) ch.set("true");
            else             ch.set("");
        }
        return input;
    }
    //bool    lt(num)         ＜
    protected CharArray func_lt(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        if (org < num )  input.set("true");
        else             input.set("");
        return input;
    }
    protected Parameter func_lt(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            if (org < num )  ch.set("true");
            else             ch.set("");
        }
        return input;
    }
    //bool    le(num)         ＜＝
    protected CharArray func_le(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
//System.out.println("org:"+org+" num:"+num);
        if (org <= num ) input.set("true");
        else             input.set("");
        return input;
    }
    protected Parameter func_le(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            if (org <= num ) ch.set("true");
            else             ch.set("");
        }
        return input;
    }
    
    //----------------------------------------------------------
    // 演算系(param)
    //----------------------------------------------------------
    /* param indexOf (数値を返す) */
    protected CharArray func_indexof(CharArray input, CharArray str) {
        int index = input.indexOf(str);
        input.set(""+index);
        return input;
    }
    protected Parameter func_indexof(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            int index = ch.indexOf(str);
            ch.set(""+index);
        }
        return input;
    }
    /* param indexOf (数値を返す) */
    protected CharArray func_indexof(CharArray input, CharArray str, CharArray fromIndex) {
        int index = input.indexOf(str, fromIndex.getInt());
        input.set(""+index);
        return input;
    }
    protected Parameter func_indexof(Parameter input, CharArray str, CharArray fromIndex) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            int index = ch.indexOf(str, fromIndex.getInt());
            ch.set(""+index);
        }
        return input;
    }
    /* param length (文字列長を返す) */
    protected CharArray func_length(CharArray input, CharArray str) {
        int size = input.strlen();
        input.set(""+size);
        return input;
    }
    protected Parameter func_length(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            int size = ch.strlen();
            ch.set(""+size);
        }
        return input;
    }
    /* param size (行数を返す) */
    protected CharArray func_size(CharArray input, CharArray str) {
        CharToken token = CharToken.pop();
        token.set(input, "\n");
        int size = token.size();
        input.set(""+size);
        CharToken.push(token);
        return input;
    }
    protected Parameter func_size(Parameter input, CharArray str) {
        CharToken token = CharToken.pop();
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            token.set(ch, "\n");
            int size = token.size();
            ch.set(""+size);
        }
        CharToken.push(token);
        return input;
    }

    //param   plus(num)       ＋
    protected CharArray func_plus(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        input.clear();
        input.format(org+num);
        return input;
    }
    protected Parameter func_plus(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            ch.clear();
            ch.format(org+num);
        }
        return input;
    }
    //param   minus(num)      －
    protected CharArray func_minus(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        input.clear();
        input.format(org-num);
        return input;
    }
    protected Parameter func_minus(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            ch.clear();
            ch.format(org-num);
        }
        return input;
    }
    //param   mul(num)        ×
    protected CharArray func_mul(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        input.clear();
        input.format(org*num);
        return input;
    }
    protected Parameter func_mul(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            ch.clear();
            ch.format(org*num);
        }
        return input;
    }
    //param   div(num)        ÷
    protected CharArray func_div(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        input.clear();
        input.format((num == 0) ? (long)0 : Math.round((double)org/num));
        return input;
    }
    protected Parameter func_div(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            ch.clear();
            ch.format((num == 0) ? (long)0 : Math.round((double)org/num));
        }
        return input;
    }

    //param   mod(num)        ％
    protected CharArray func_mod(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        input.clear();
        input.format((num == 0) ? (long)0 : org%num);
        return input;
    }
    protected Parameter func_mod(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            ch.clear();
            ch.format((num == 0) ? (long)0 : org%num);
        }
        return input;
    }
    //param   and(num)        論理積：ビット演算(AND)
    protected CharArray func_and(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        input.clear();
        input.format(org & num);
        return input;
    }
    protected Parameter func_and(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            ch.clear();
            ch.format(org & num);
        }
        return input;
    }
    //param   or(num)        論理和：ビット演算(OR)
    protected CharArray func_or(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        input.clear();
        input.format(org | num);
        return input;
    }
    protected Parameter func_or(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            ch.clear();
            ch.format(org | num);
        }
        return input;
    }
    //param   xor(num)       排他的論理和：ビット演算(XOR)
    protected CharArray func_xor(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        long num = CharArray.getLong(str);
        input.clear();
        input.format(org ^ num);
        return input;
    }
    protected Parameter func_xor(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            long num = CharArray.getLong(str);
            ch.clear();
            ch.format(org ^ num);
        }
        return input;
    }
    
    //param   not()          論理否定：ビット演算(NOT)
    protected CharArray func_not(CharArray input, CharArray str) {
        long org = CharArray.getLong(input);
        //long num = CharArray.getLong(str);
        input.clear();
        input.format(~org);
        return input;
    }
    protected Parameter func_not(Parameter input, CharArray str) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            long org = ch.getLong();
            //long num = CharArray.getLong(str);
            ch.clear();
            ch.format(~org);
        }
        return input;
    }
    
    //bool    rev()          条件反転、デバッグ用
    protected CharArray func_rev(CharArray input, CharArray str, SessionObject session) {
if (debug) System.out.print(session.count+"| ★REV("+input);
        if (input != null && input.length() > 0) input.set("");
        else                                     input.set("true");
if (debug) System.out.println("→"+input+")");
        return input;
    }
    protected Parameter func_rev(Parameter input, CharArray str, SessionObject session) {
        for (int i = 0; i < input.size(); i++) {
            CharArray ch = input.peek(i);
            if (ch != null && ch.length() > 0) ch.set("");
            else                               ch.set("true");
        }
        return input;
    }
    
    //----------------------------------------------------------
    // データ保管(param)
    //----------------------------------------------------------
    //param   setParameter(key)     内部パラメータに設定する
    protected CharArray func_setParameter(CharArray input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            session.setParameter(key.toString(), input.toString());
        }
        return input;
    }
    protected Parameter func_setParameter(Parameter input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            for (int i = 0; i < input.size(); i++) {
                session.setParameter(key.toString(), input.peek(i).toString());
            }
        }
        return input;
    }
    //param   addParameter(key)     内部パラメータに追加する
    protected CharArray func_addParameter(CharArray input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            String str = session.getParameter(key.toString());
            if (str == null) str = "";
            session.setParameter(key.toString(), str+input.toString());
        }
        return input;
    }
    protected Parameter func_addParameter(Parameter input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            for (int i = 0; i < input.size(); i++) {
                String str = session.getParameter(key.toString());
                if (str == null) str = "";
                session.setParameter(key.toString(), str+input.peek(i).toString());
            }
        }
        return input;
    }
    //param   setUserData(key)     内部データに設定する
    protected CharArray func_setUserData(CharArray input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            session.getUserData().put(new CharArray(key), input);
        }
        return input;
    }
    protected Parameter func_setUserData(Parameter input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            CharArray chKey = new CharArray(key);
            for (int i = 0; i < input.size(); i++) {
                session.getUserData().put(chKey, input.peek(i));
            }
        }
        return input;
    }
    //param   addUserData(key)     内部データに追加する
    protected CharArray func_addUserData(CharArray input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            CharArray ch = session.getUserData().get(key);
            if (ch == null) ch = input; else ch.add(input);
            session.getUserData().put(new CharArray(key), ch);
        }
        return input;
    }
    protected Parameter func_addUserData(Parameter input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            CharArray chKey = new CharArray(key);
            for (int i = 0; i < input.size(); i++) {
                CharArray ch = session.getUserData().get(key);
                if (ch == null) ch = input.peek(i); else ch.add(input.peek(i));
                session.getUserData().put(chKey, ch);
            }
        }
        return input;
    }
    //param   setUserTemplate(key)     ユーザーテンプレートに設定する
    protected CharArray func_setUserTemplate(CharArray input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            session.getUserTemplate().put(new CharArray(key), input);
        }
        return input;
    }
    protected Parameter func_setUserTemplate(Parameter input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            CharArray chKey = new CharArray(key);
            for (int i = 0; i < input.size(); i++) {
                session.getUserTemplate().put(chKey, input.peek(i));
            }
        }
        return input;
    }
    //param   addUserTemplate(key)     ユーザーテンプレートに追加する
    protected CharArray func_addUserTemplate(CharArray input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            
            CharArray ch = session.getUserTemplate().get(key);
            if (ch == null) ch = input; else ch.add(input);
            session.getUserTemplate().put(new CharArray(key), ch);
        }
        return input;
    }
    protected Parameter func_addUserTemplate(Parameter input, CharArray key, SessionObject session) {
        if (session != null && key != null && key.length()> 0) {
            CharArray chKey = new CharArray(key);
            for (int i = 0; i < input.size(); i++) {
                CharArray ch = session.getUserTemplate().get(key);
                if (ch == null) ch = input.peek(i); else ch.add(input.peek(i));
                session.getUserTemplate().put(chKey, ch);
            }
        }
        return input;
    }

}

//
// [end of ContentSubFunction.java]
//

