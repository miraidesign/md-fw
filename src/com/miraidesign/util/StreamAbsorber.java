//------------------------------------------------------------------------
// @(#)StreamAbsorber.java
//                 InputStreamから入力を読み込む。
//                 InputStreamをcloseすることでスレッドは終了する。
//                 Copyright (c) Mirai Design 2010 All Rights Reserved.
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import com.miraidesign.common.SystemConst;
import com.miraidesign.util.CharArray;

/** InputStreamから入力を読み込む。*/
public class StreamAbsorber extends Thread {
    private static boolean debug = (false && SystemConst.debug);

    private static int MAX_MESSAGE_LENGTH = 500_000;

    private OutputStream outStream = null;
    private InputStream inStream;
    private String head="";
    private CharArray msg = new CharArray();
    private UserLog log = null;
    
    public StreamAbsorber(InputStream inStream) {
        this.inStream = inStream;
    }
    public StreamAbsorber(InputStream inStream, int max_message_length) {
        this.inStream = inStream;
        MAX_MESSAGE_LENGTH = max_message_length;
    }
    public StreamAbsorber(InputStream inStream, String str) {
        this.inStream = inStream;
        head = str;
    }
    public StreamAbsorber(InputStream inStream, String str, int max_message_length) {
        this.inStream = inStream;
        head = str;
        MAX_MESSAGE_LENGTH = max_message_length;
    }

    public StreamAbsorber(InputStream inStream, OutputStream outStream) {
        this.inStream = inStream;
        this.outStream = outStream;
    }
    public StreamAbsorber(InputStream inStream, OutputStream outStream, int max_message_length) {
        this.inStream = inStream;
        this.outStream = outStream;
        MAX_MESSAGE_LENGTH = max_message_length;
    }


    public void setMaxMessageLength(int length) {
        MAX_MESSAGE_LENGTH = length;
    }
    /** ログを指定します<br>
        ログが指定されていた場合は、getMessage() の出力はありません.
    */
    public void setUserLog(UserLog log) {
        this.log = log;
    }
    /* コマンド出力メッセージを取得します */
    public CharArray getMessage() { return msg; }
    public void run() {
        try {
            int b;
            int last = 0x0A;
            while ((b = inStream.read()) != -1) {
                if (log != null) {
                    if (b == 0x0A) {
                        log.out(head+msg);
                        msg.clear();
                    } else if (b != 0x0D) {
                        if (MAX_MESSAGE_LENGTH <= 0 || msg.length() <= MAX_MESSAGE_LENGTH) {
                            msg.add((char)b);
                        } else {
                            msg.add("-snip-");
                            log.out(head+msg);
                            msg.clear();
                        }
                    }
                } else {
                    if (MAX_MESSAGE_LENGTH <= 0 || msg.length() <= MAX_MESSAGE_LENGTH) {
                        if (head.length() > 0) {
                            if (last == 0x0A) msg.add(head);
                            msg.add((char)b);
                            last = b;
                        } else {
                            msg.add((char)b);
                        }
                    }
                }
                if (outStream != null) {
                    try {
                        outStream.write(b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) { 
            e.printStackTrace();
        }
        if (log != null && msg.length() > 0) {
            log.out(head+msg);
            msg.clear();
        }
        if (inStream != null) {
            try {
                inStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (outStream != null) {
            try {
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        inStream  = null;
        outStream = null;
        if (debug) System.out.println("StreamAbsorber.run()の終了");
    }
}

//
//
// [end of StreamAbsorber.java]
//
