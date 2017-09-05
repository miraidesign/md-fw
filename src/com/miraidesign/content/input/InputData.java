//------------------------------------------------------------------------
//    InputData.java
//          ユーザー入力情報（TEXT)を保管する
//              Copyright (c) Miraidesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content.input;

import com.miraidesign.content.ContentParser;
import com.miraidesign.content.Input;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;

/**
    ユーザー入力情報（DATA)を保管する
*/
public class InputData extends InputItem {
    private CharArray maxlength = new CharArray();
    private CharArray mode = new CharArray();
    private CharArray min  = new CharArray();
    private CharArray max  = new CharArray();
                           
    ContentParser parser;
    //-------------------
    // constructor
    //-------------------
    /* コンストラクタ */
    public InputData(Input input, ContentParser parser) {
        super(input);
        this.parser = parser;
        type.set("DATA");
        data.setNotNull(input.getDefault());
        maxlength.setNotNull(input.getParameter("maxlength"));
        mode.setNotNull(input.getParameter("mode"));
        min.setNotNull(input.getParameter("min"));
        max.setNotNull(input.getParameter("max"));
    }
    public InputData(ContentParser parser) {  // 未使用
        this.parser = parser;
    }
    
    //-------------------
    // exchange
    //-------------------
    public void exchange(InputItem from) {
        super.exchange(from);
        InputData _from = (InputData)from;
        
        maxlength.exchange(_from.maxlength);
        mode.exchange(_from.mode);
        min.exchange(_from.min);
        max.exchange(_from.max);
    }
    //-------------------
    // method
    //-------------------
    /* maxlength を設定する */
    public void setMaxlength(int maxlength) {
        this.maxlength.set(""+maxlength);
    }
    /* maxlength を設定する */
    public void setMaxlength(CharArray maxlength) {
        this.maxlength.setNotNull(maxlength);
    }
    /* maxlength を取得する */
    public CharArray getMaxlength() {
        return maxlength;
    }
    
    /* mode を設定する 
        num/alpha/alnum/[all]
    */
    public void setMode(CharArray mode) {
        this.mode.setNotNull(mode);
    }
    /* mode を取得する */
    public CharArray getMode() {
        return mode;
    }
    
    /* 最小値を設定する(num時のみ有効） */
    public void setMin(int min) {
        this.min.set(""+min);
    }
    /* 最小値を設定する(num時のみ有効） */
    public void setMin(CharArray min) {
        this.min.setNotNull(min);
    }
    /* 最小値を取得する */
    public CharArray getMin() {
        return min;
    }
    
    /* 最大値を設定する(num時のみ有効） */
    public void setMax(int max) {
        this.max.set(""+max);
    }
    /* 最大値を設定する(num時のみ有効） */
    public void setMax(CharArray max) {
        this.max.setNotNull(max);
    }
    /* 最大値を取得する */
    public CharArray getMax() {
        return max;
    }
    
    //////////////////////////////////////////////////
    
    /* デフォルト関数 */
    public CharArray get() {
        return data;
    }
    public CharArray get(int state) {
        CharArray ch = data;
        return ch;
    }
    public void set(CharArray ch) {
        data.setNotNull(ch);
    }
    
    /**
        関数呼び出し
        @param func  関数名 
        @param param 関数パラメータ（未サポート）
        @return 関数の結果
    */
    public CharArray getFunc(CharArray func, CharArrayQueue param) {
        return getFunc(func, param, 0);
    }
    public CharArray getFunc(CharArray func, CharArrayQueue param, int state) {
        CharArray ch = null;
        if (func == null) {
            ch = get(state);
        } else {
            func.trim().toLowerCase();
            if (func.length() == 0)            ch = get(state);
            else if (func.equals("main"))      ch = get(state);
            else if (func.equals("maxlength")) ch = getMaxlength();
            else if (func.equals("min"))       ch = getMin();
            else if (func.equals("max"))       ch = getMax();
            else if (func.equals("mode"))      ch = getMode();
            else if (func.equals("default"))   ch = input.getDefault();
            else if (func.equals("key"))       ch = getKey();
            else if (func.equals("label"))     ch = getLabel();
            else if (func.equals("description")) ch = getDescription();
            else                               ch = get(state);
        }
        return ch;
    }
    
    /**
        関数設定
        @param func  関数名 
        @param param 関数パラメータ
        @return true 設定成功
    */
    public boolean setFunc(CharArray func, CharArrayQueue param) {
        boolean sts = true;
        if (func != null && param != null && param.size() > 1) {
            CharArray ch = param.peek();
            func.trim().toLowerCase();
            if (func.length() == 0)            set(ch);
            else if (func.equals("main"))      set(ch);
            else if (func.equals("maxlength")) setMaxlength(ch);
            else if (func.equals("min"))       setMin(ch);
            else if (func.equals("max"))       setMax(ch);
            else if (func.equals("mode"))      setMode(ch);
            else                               sts = false;
        } else sts = false;
        return sts;
    }
    /**
        関数設定
        @param func 関数名 
        @param ch 関数パラメータ
        @return true 設定成功
    */
    public boolean setFunc(CharArray func, CharArray ch) {
        boolean sts = true;
        if (func != null) {
            func.trim().toLowerCase();
            if (func.length() == 0)            set(ch);
            else if (func.equals("main"))      set(ch);
            else if (func.equals("maxlength")) setMaxlength(ch);
            else if (func.equals("min"))       setMin(ch);
            else if (func.equals("max"))       setMax(ch);
            else if (func.equals("mode"))      setMode(ch);
            else                               sts = false;
        } else sts = false;
        return sts;
    }
}

//
// [end of InputData.java]
//

