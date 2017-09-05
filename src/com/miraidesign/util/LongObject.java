//------------------------------------------------------------------------
//    LongObject.java
//             再利用可能な Long  機能限定版
//             Copyright (c) Mirar Designv 2010 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.util;

//import java.io.Serializable;
import java.util.Stack;

/**     再利用可能な Long  機能限定版 */
public final class LongObject /*implements Serializable*/ {

    private long value;

    public LongObject() {}
    public LongObject(long value) {this.value = value;}
    public LongObject(LongObject obj) {this.value = obj.getValue();}
    
    public void setValue(long value) { this.value = value;}
    public long getValue() { return value;}
    public int hashCode() { return (int)(value ^ (value >> 32));}
    
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof LongObject)) {
            return value == ((LongObject)obj).getValue();
        }
        return false;
    }
    public String toString() {      // あまり利用しない事
        return ""+value;
    }

    //---------------------------------
    private static Stack<LongObject> stack= new Stack<LongObject>();
    private static int MIN_SIZE =  20;
    private static int MAX_SIZE = 512;
    
    
    public static /*synchronized*/ LongObject push(LongObject item) {
        if (stack.size() > MAX_SIZE || item == null) {
            item = null; // for GC
            return item;
        } else {
            item.setValue(0);
            return (LongObject)stack.push(item);
        }
    }
    
    public static /*synchronized*/ LongObject pop() {
        LongObject obj;
        synchronized (stack) {
            if (stack.size() < MIN_SIZE) {
                obj = new LongObject();
            } else {
                obj = (LongObject)stack.pop();
            }
        }
        return obj;
    }
    public static LongObject pop(long l) {
        LongObject obj = pop();
        obj.setValue(l);
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
// [end of LongObject.java]
//

