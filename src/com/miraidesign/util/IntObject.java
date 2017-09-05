//------------------------------------------------------------------------
//    IntObject.java
//         再利用可能なInteger  機能限定版
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-05   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
//   Hashtableのキー等に利用可能
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.util.Stack;

/**     再利用可能なInteger  機能限定版 */
public final class IntObject /*implements Serializable*/ {

    private int value;

    /** default constructor */
    public IntObject() {}
    /* 
        consgtructor
        @param value 初期値
    */
    public IntObject(int value) {this.value = value;}
    /** 
        copy consttuctor
        @param obj コピー元
    */
    public IntObject(IntObject obj) {this.value = obj.getValue();}
    
    /** 
        値を設定する 
        @param value 設定値
    */
    public void setValue(int value) { this.value = value;}
    /** 
        値を取得する
        @return int値
    */
    public int getValue() { return value;}
    /** 
        値を取得する
        @return int値
    */
    public int intValue() { return value;}
    /** 
        hashcodeを取得する
        @return int値
    */
    public int hashCode() { return value;}
    /**
        IntObjectと比較する
        @param obj 比較オブジェクト
        @return true: 同一データ false:データまたはクラス違い 
    */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof IntObject)) {
            return value == ((IntObject)obj).getValue();
        }
        return false;
    }
    /**
        数値を文字列に変換する
        @return 文字列
    */
    public String toString() {      // あまり利用しない事
        return ""+value;
    }
    //---------------------------------
    private static Stack<IntObject> stack= new Stack<IntObject>();
    private static int MIN_SIZE =  20;
    private static int MAX_SIZE = 512;
    
    public static /*synchronized*/ IntObject push(IntObject item) {
        if (stack.size() > MAX_SIZE || item == null) {
            item = null; // for GC
            return item;
        } else {
            item.setValue(0);
            return (IntObject)stack.push(item);
        }
    }
    
    public static /*synchronized*/ IntObject pop() {
        IntObject obj;
        synchronized (stack) {
            if (stack.size() < MIN_SIZE) {
                obj = new IntObject();
            } else {
                obj = (IntObject)stack.pop();
            }
        }
        return obj;
    }
    public static IntObject pop(int i) {
        IntObject obj = pop();
        obj.setValue(i);
        return obj;
    }
    
    public static boolean empty() {
        return stack.empty();
    }
    
    public static int size() {
        return stack.size();
    }
    
}

//
// [end of IntObject.java]
//

