//------------------------------------------------------------------------
//    BooleanObject.java
//                 再利用可能な Boolean  機能限定版
//                 Copyright (c) Mirai Design All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.util.Stack;

/**  再利用可能な Boolaen  機能限定版 */
public final class BooleanObject /*implements Serializable*/ {

    private boolean value = false;

    public BooleanObject() {}
    public BooleanObject(boolean value) {this.value = value;}
    public BooleanObject(BooleanObject obj) {this.value = obj.getValue();}
    
    public void setValue(boolean value) { this.value = value;}
    public boolean getValue() { return value;}
    public int hashCode() { return value ? 1 : 0;}
    
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof BooleanObject)) {
            return value == ((BooleanObject)obj).getValue();
        }
        return false;
    }
    public String toString() {      // あまり利用しない事
        return ""+value;
    }
    //---------------------------------
    private static Stack<BooleanObject> stack = new Stack<BooleanObject>();
    private static int MIN_SIZE = 8;
    private static int MAX_SIZE = 512;
    
    
    public static /*synchronized*/ BooleanObject push(BooleanObject item) {
        if (stack.size() > MAX_SIZE || item == null) {
            return item;
        } else {
            item.setValue(false);
            return (BooleanObject)stack.push(item);
        }
    }
    
    public static /*synchronized*/ BooleanObject pop() {
        BooleanObject obj;
        synchronized (stack) {
            if (stack.size() < MIN_SIZE) {    // スタックが空なら
                obj = new BooleanObject();
            } else {
                obj = (BooleanObject)stack.pop();
            }
        }
        return obj;
    }
    public static BooleanObject pop(boolean b) {
        BooleanObject obj = pop();
        obj.setValue(b);
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
//
// [end of BooleanObject.java]
//

