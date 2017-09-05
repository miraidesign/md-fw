//------------------------------------------------------------------------
//    Double3Point.java
//                 3次元データの保管
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
package com.miraidesign.util;

/**  3次元データの保管 */
public class Double3Point {
    public  double x;
    public  double y;
    public  double z;
    
    public  Double3Point() { }
    public  Double3Point(double x,double y,double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public  Double3Point(Double3Point p) {
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
    }
}

//
// [end of Double3Point.java]
//

