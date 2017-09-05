//------------------------------------------------------------------------
//    DoubleObject.java
//             再利用可能な Double  機能限定版
//             Copyright (c) Mirai Design  1999-2010 All Rights Reserved.    
//                 update: 1999-11-10 ishi
//------------------------------------------------------------------------

package com.miraidesign.util;

//import java.io.Serializable;
import java.util.Stack;

/** 再利用可能な Double  機能限定版 */
public final class DoubleObject /*implements Serializable*/ {

    private double value;

    public DoubleObject() {}
    public DoubleObject(double value) {this.value = value;}
    public DoubleObject(DoubleObject obj) {this.value = obj.getValue();}
    
    public void setValue(double value) { this.value = value;}
    public double getValue() { return value;}
    public int hashCode() { 
        long bits = Double.doubleToLongBits(value);
        return (int)(bits ^ (bits >> 32));
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof DoubleObject)) {
            return Double.doubleToLongBits(value) == 
                Double.doubleToLongBits(((DoubleObject)obj).getValue());
        }
        return false;
    }
    public String toString() {      // あまり利用しない事
        return ""+value;
    }
    //---------------------------------
    private static Stack<DoubleObject> stack = new Stack<DoubleObject>();
    private static int MIN_SIZE = 8;
    private static int MAX_SIZE = 512;
    
    
    public static /*synchronized*/ DoubleObject push(DoubleObject item) {
        if (stack.size() > MAX_SIZE || item == null) {
            item = null; // for GC
            return item;
        } else {
            item.setValue(0);
            return (DoubleObject)stack.push(item);
        }
    }
    
    public static /*synchronized*/ DoubleObject pop() {
        DoubleObject obj;
        synchronized (stack) {
            if (stack.size() < MIN_SIZE) {
                obj = new DoubleObject();
            } else {
                obj = (DoubleObject)stack.pop();
            }
        }
        return obj;
    }
    public static DoubleObject pop(double d) {
        DoubleObject obj = pop();
        obj.setValue(d);
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
// [end of DoubleObject.java]
//

