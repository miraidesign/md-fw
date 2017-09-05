//------------------------------------------------------------------------
//    DoublePoint.java
//                 
//                 Copyright (c) Toru Ishioka 1997  
//                                               All Rights Reserved.     
//                                               update: 97-04-08 ishioka 
//------------------------------------------------------------------------
package com.miraidesign.util;

/** ２次元情報の保管 */
public class DoublePoint {
    public  double x;
    public  double y;
    
    public  DoublePoint() { }
    public  DoublePoint(double x,double y) {
        this.x = x;
        this.y = y;
    }
    public  DoublePoint(DoublePoint p) {
        this.x = p.x;
        this.y = p.y;
    }
}

//
// [end of DoublePoint.java]
//

