//------------------------------------------------------------------------
// @(#)EventMulticaster.java
//          イベントマルチ配信
//
//                  Copyright (c) Toru Ishioka 1999-2010
//                                               All Rights Reserved.     
//------------------------------------------------------------------------

package com.miraidesign.event;

import java.util.EventListener;
import javax.mail.Message;

import com.miraidesign.mail.ReceiveMailListener;

public class EventMulticaster implements java.util.EventListener,
            ReceiveMailListener {
        //RepeatListener, AdjustmentLongListener, SelectListener {

    protected final EventListener a, b;

    protected EventMulticaster(EventListener a, EventListener b) {
        this.a = a; this.b = b;
    }

    protected EventListener remove(EventListener oldl) {
        if (oldl == a)  return b;
        if (oldl == b)  return a;
        EventListener a2 = removeInternal(a, oldl);
        EventListener b2 = removeInternal(b, oldl);
        if (a2 == a && b2 == b) {
            return this;        // it's not here
        }
        return addInternal(a2, b2);
    }
    //-------------------------------------------
    // ReceiveMailListener
    //-------------------------------------------
    public void mailReceived(Message[] messages) {
        ((ReceiveMailListener)a).mailReceived(messages);
        ((ReceiveMailListener)b).mailReceived(messages);
    }
    public void mailClosed() {
        ((ReceiveMailListener)a).mailClosed();
        ((ReceiveMailListener)b).mailClosed();
    }

    public static ReceiveMailListener add(ReceiveMailListener a, ReceiveMailListener b) {
        return (ReceiveMailListener)addInternal(a, b);
    }

    public static ReceiveMailListener remove(ReceiveMailListener l, ReceiveMailListener oldl) {
        return (ReceiveMailListener)removeInternal(l, oldl);
    }
    //-------------------------------------------
    // ItemEventListener
    //-------------------------------------------
    /*
    public CharArray execute(CItemEvent e, CharArray ch) {
        ((ItemEventListener)a).execute(e,ch);
        ((ItemEventListener)b).execute(e,ch);
        return ch;
    }

    public static ItemEventListener add(ItemEventListener a, ItemEventListener b) {
        return (ItemEventListener)addInternal(a, b);
    }

    public static ItemEventListener remove(ItemEventListener l, ItemEventListener oldl) {
        return (ItemEventListener)removeInternal(l, oldl);
    }
    */
    //-------------------------------------------
    // RepeatListener
    //-------------------------------------------
    /*
    public void repeatPerformed(RepeatEvent e) {
        ((RepeatListener)a).repeatPerformed(e);
        ((RepeatListener)b).repeatPerformed(e);
    }

    public static RepeatListener add(RepeatListener a, RepeatListener b) {
        return (RepeatListener)addInternal(a, b);
    }

    public static RepeatListener remove(RepeatListener l, RepeatListener oldl) {
        return (RepeatListener)removeInternal(l, oldl);
    }
    */
    
    //-------------------------------------------
    // AdjustmentLongListener
    //-------------------------------------------
    /*
    public void adjustmentLongValueChanged(AdjustmentLongEvent e) {
        ((AdjustmentLongListener)a).adjustmentLongValueChanged(e);
        ((AdjustmentLongListener)b).adjustmentLongValueChanged(e);
    }

    public static AdjustmentLongListener add(AdjustmentLongListener a, AdjustmentLongListener b) {
        return (AdjustmentLongListener)addInternal(a, b);
    }

    public static AdjustmentLongListener remove(AdjustmentLongListener l, AdjustmentLongListener oldl) {
        return (AdjustmentLongListener)removeInternal(l, oldl);
    }
    */
    //-------------------------------------------
    // SelectListener
    //-------------------------------------------
    /*
    public void selectChanged(SelectEvent e) {
        ((SelectListener)a).selectChanged(e);
        ((SelectListener)b).selectChanged(e);
    }

    public static SelectListener add(SelectListener a, SelectListener b) {
        return (SelectListener)addInternal(a, b);
    }

    public static SelectListener remove(SelectListener l, SelectListener oldl) {
        return (SelectListener)removeInternal(l, oldl);
    }
    */
    //-------------------------------------------
    // Internal
    //-------------------------------------------
    protected static EventListener addInternal(EventListener a, EventListener b) {
        if (a == null)  return b;
        if (b == null)  return a;
        return new EventMulticaster(a, b);
    }

    protected static EventListener removeInternal(EventListener l, EventListener oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof EventMulticaster) {
            return ((EventMulticaster)l).remove(oldl);
        } else {
            return l; 
        }
    }

}
//
//
// [end of EventMulticaster.java]
//

