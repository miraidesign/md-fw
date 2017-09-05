//------------------------------------------------------------------------
//    QueueFile.java                                                       
//          テキストファイル等をCharArrayQueuer管理
//
//          Copyright (c) Mirai Design Institute 2010 All rights reserved.
//          update 2010-02-04   ishioka.toru@miraidesign.com
//------------------------------------------------------------------------
//
package com.miraidesign.util;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import java.net.Socket;
import java.net.URL;

/** テキストファイルの内容等をCharArrayQueuer管理 <br>
    日本語ファイルの漢字コード自動認識をする場合は、<br>
    user.region=JP の設定が必要です
*/
public class QueueFile extends CharArrayQueue {
    static final boolean debug = false;
    protected String szFileName;
    protected String[] str;             // あとで変更する
    public boolean silentMode = false;
    protected boolean appendMode = false;
    protected int iAppendMax = 1000;    // アペンドする最大行数

    private BufferedWriter out;
    protected Socket socket;
    protected InputStream stream;
    
    protected String szDefaultEncoding = "MS932";       //SJIS"; MS932
    public String getEncoding() { return szDefaultEncoding;}
    public String getFileEncoding() { return szDefaultEncoding;}
    public void setEncoding(String s) {
        if (s != null) {
            szDefaultEncoding = s;
        }
    }
    public void setFileEncoding(String s) {
        setEncoding(s);
    }
    
    //---
    protected CharArray connector = new CharArray();    // 行を結合する文字列
    public void setConnector(CharArray ch) { connector.set(ch); }
    public void setConnector(String str) { connector.set(str); }
    
    protected CharArray NLconnector = new CharArray();    // \nに変換して行結合
    public void setNLConnector(CharArray ch) { NLconnector.set(ch); }
    public void setNLConnector(String str) { NLconnector.set(str); }

//  // --------- 暗号化 -----------------------------
//  protected String cryptKey = null;       // 暗号キー
//  protected boolean isAscii = false;      // ASCIIモードor漢字モード(default)
//  /**
//      暗号化モード設定（read/writeとも有効になる)
//      @param key 暗号化キーワード nullで無効化
//  */
//  public void setCrypt(String key) { cryptKey = key;}
//  /** 暗号化モードを設定する
//      @param mode true: ASCIIモード false:漢字対応(デフォルト)
//  */
//  public void setCryptMode(boolean mode) { isAscii = mode;}
    //------------------------------------------------------------------------
    protected String include = "";
    protected String includeEnd = "";
    /** インクルードキーワードを設定する 
        ファイル名後のスキップワードを設定したい場合は、<br>
        setInclude("#include * terminate"); のようにする
    */
    public void setInclude(String str) { 
        int index = str.indexOf('*');
        if (index < 0) {
            include = str;
        } else {
            include     = str.substring(0, index);
            includeEnd  = str.substring(index+1);
        }
    }
    /** インクルード文字列を取得する */
    public String getIncludeString() { return include; }
    //------------------------------------------------------------------------
    public Converter converter = null;
    /** コンバータを設定する  */
    public void setConverter(Converter converter) {
        this.converter = converter;
    }
    //------------------------------------------------------------------------
    
    /** コンストラクタ */
    public QueueFile() {}
    public QueueFile(String szFileName) { this.szFileName = szFileName;}
    public QueueFile(String szFileName, String encoding) { 
        this.szFileName = szFileName;
        szDefaultEncoding = encoding;
    }
    public QueueFile(String[] str) { this.str = str; }
    public QueueFile(Socket socket) { this.socket = socket;}
    public QueueFile(InputStream stream) { this.stream = stream;}
    
    public String getFilename() { return szFileName;}
    public void setAppendMode(boolean mode) { appendMode = mode;}
    public void setAppendMode(boolean mode, int size) {
        appendMode = mode; iAppendMax = size;
    }

    /** ファイルからデータを読み込む；ファイル名はコンストラクタで定義される */
    public boolean read() { return read(szFileName,0);} 
    public boolean read(int progress) { return read(szFileName,progress);} 
    /** ファイルからデータを読み込む；ファイル名を指定する */
    public boolean read(String szFileName) {
        return read(szFileName,0);
    }
    /** ファイル読み込み
        @param szFileName ファイル名(ローカル、URL指定可)
        @param progress 進行状況表示行数（０で無視）
    */
    public boolean read(String szFileName,
                        int progress) { // N行おきに進行状況表示（０で無視）
        clear();
        if (debug) {
            System.out.println("QueueFile:Read("+szFileName+")");
        }
        boolean rsts = true;
        if (str != null) {
            for (int i = 0; i < str.length; i++) {
                enqueue(new CharArray(str[i]));     //@@//
            }
        } else if (szFileName != null) {
            if (szFileName.indexOf("://") >= 0 || new File(szFileName).exists()) {
                rsts = readSub(this, szFileName, progress, szDefaultEncoding);
            } else {
                rsts = false;
            }
        
        } else if (socket != null || stream != null) {
            rsts = readSub(this, szFileName, progress, szDefaultEncoding);
        } else {
            if (debug) System.out.println("QueueFile::Read error");
            rsts = false;
        }
        return rsts;
    }
    
    // 読み込みサブルーチン
    public boolean readSub(CharArrayQueue queue, String szFileName, int progress) {
        return readSub(queue, szFileName, progress, null);
    }
    public boolean readSub(CharArrayQueue queue, String szFileName, int progress, String encoding) {
        boolean rsts = true;
        BufferedReader in = null;
        FileInputStream fin;
        
        if (encoding == null || encoding.trim().length() == 0) encoding = szDefaultEncoding;
        try {
            // ファイル名に基づきバッファードリーダ・ストリームを生成する
            //日本なら文字コード自動判別する
            if (szFileName != null) {
                try {
                    //if (System.getProperty("user.region").compareTo("JP")==0){
                        if (szFileName.indexOf("://") >= 0) { // URL指定時
                            try {
                                URL url = new URL(szFileName);
                                in= new BufferedReader(new InputStreamReader(url.openStream(),encoding));
                            } catch (Exception e0) {
                                System.out.println("QueueFile:Read() 0");
                                e0.printStackTrace();
                                in = null;
                            }
                        } else {
                            fin = new FileInputStream(szFileName);
                            try {
                                in= new BufferedReader(new InputStreamReader(fin,encoding));
                            } catch(UnsupportedEncodingException e1) {
                                System.out.println("QueueFile:Read() 1");
                                e1.printStackTrace();
                                in = null;
                            }
                         }
                    //}
                } catch (Exception e2) {
                   System.out.println("QueueFile:Read() 2");
                   e2.printStackTrace();
                }
            }
            if (in == null) {
                if (socket != null) {
                    if (debug) System.out.println("QueueFile:Read input from socket");
                    try {
                      in = new BufferedReader(new InputStreamReader(socket.getInputStream(), encoding));
                    } catch (Exception e3) {
                       System.out.println("QueueFile:Read() 3");
                        e3.printStackTrace();
                    }
                } else if (stream != null) {
                    if (debug) System.out.println("QueueFile:Read input from stream");
                    try {
                      in = new BufferedReader(new InputStreamReader(stream, encoding));
                    } catch (Exception e4) {
                       System.out.println("QueueFile:Read() 4");
                        e4.printStackTrace();
                    }
                } else {
                    String region = System.getProperty("user.region");
                    if (region != null && region.compareTo("JP")==0) {
                        fin = new FileInputStream(szFileName);
                        try{
                            in = new BufferedReader(new InputStreamReader(fin, encoding));
                        } catch (UnsupportedEncodingException e5) {
                           System.out.println("QueueFile:Read() 5");
                            e5.printStackTrace();
                            in = null;
                        }
                    } else {
                        in = new BufferedReader(new FileReader(szFileName));
                    }
                }
            }
            boolean NLconnect = false;
            boolean connect = false;
            CharArray ch = CharArray.pop();
            CharArray ch2 = CharArray.pop();
            int offset = 0;
            while (true) {
                try {
                    // ファイルから１行分を読み込む
                    if (socket != null) socket.setSoTimeout(10000);
                    String s = in.readLine();
                    if (s == null) {
                        if (connect) queue.enqueue(new CharArray(ch));
                        break;
                    }
                    if (progress > 0 && size() % progress == 0) System.out.print(".");
//                  if (cryptKey != null) { // 暗号化モード
//                                          // あとでCryptTextにする
//                      CharArray to = isAscii ? CryptAscii.decode(new CharArray(s),cryptKey,offset)
//                                             : CryptText.decode(new CharArray(s),cryptKey,offset);
//                      s = to.toString();
//                      offset += s.length();
//                  }
                    if (converter != null) s = converter.convert(s);
                    
                    boolean not_include = true;
                    if (NLconnect) {
                        ch.add("\n");
                        ch.add(ch2.set(s));
                    } else if (connect) {
                        ch.add(ch2.set(s).trimL());
                    } else {
                        int inc_size = include.length();
                        int end_size = includeEnd.length();
                        if (inc_size > 0 && s.startsWith(include) &&
                            s.length() > inc_size && 
                            (s.charAt(inc_size-1) <= ' ' || s.charAt(inc_size) <= ' ')) {
                            CharArray newFile = CharArray.pop();

                            //newFile.set(s.substring(inc_size)).trim();
                            newFile.set(s.substring(inc_size));
                            if (end_size > 0) {
                                int ix = newFile.indexOf(includeEnd);
                                if (ix >= 0) newFile.remove(ix);
                            }
                            newFile.trim();
                            if (newFile.length() > 0) {
                                Converter converter_bak = converter;
                                String nextEncoding = encoding;
                                do {
                                    int index = newFile.indexOf(' ');
                                    if (index < 0) index = newFile.indexOf('\t');
                                    if (index > 0) {
                                        CharArray chParam = new CharArray();
                                        chParam.set(newFile, index+1);
                                        chParam.trim();
                                        newFile.length = index;
                                        if (chParam.length() > 0) {
                                            if (chParam.equals("--convert")) {
                                                converter = null;
                                            } else {
                                                if (converter == null) 
                                                   converter = new ParameterConverter();
                                                else 
                                                   converter = (Converter)converter.copy();
                                                CharToken token = CharToken.pop();
                                                token.set(chParam, ",");
                                                for (int i = 0; i < token.size(); i++) {
                                                    //((Parameter)converter).add(token.get(i));    
                                                  converter.add(token.get(i));
                                                }
                                                CharToken.push(token);
                                            }
                                        }
                                    }
                                    
                                    //--------------------------
                                    int idx = newFile.indexOf(','); // エンコードチェック
                                    if (idx > 0) { // found
                                        if (idx == newFile.length()-1) {
                                            while (newFile.length() > 0 && newFile.chars[idx] == ',') {
                                                newFile.length -= 1;
                                                --idx;
                                            }
                                        } else {
                                            nextEncoding = newFile.substring(idx+1);
                                            newFile.length = idx;
                                        }
                                    }
                                    newFile.trim();
                                    //--------------------------
                                    if (newFile.indexOf("://") >= 0 ||
                                        newFile.chars[0]=='/' ||
                                        newFile.chars[0]=='\\' ||
                                        newFile.chars[1]==':') {
                                        break;  // そのまま使う
                                    } 
                                    int index1 = 0;
                                    int index2 = 0;
                                    index1 = szFileName.lastIndexOf("/");
                                    index2 = szFileName.lastIndexOf("\\");
                                    if (index1 < 0 && index2 < 0) break;
                                    if (index1 > 0) {
                                        newFile.insert(0, szFileName.substring(0,index1+1));
                                    } else if (index2 > 0) {
                                        newFile.insert(0, szFileName.substring(0,index2+1));
                                    }
                                    
                                } while (false);
                                rsts = readSub(this, newFile.toString(), progress, nextEncoding);
                                not_include = false;
                                
                                converter = converter_bak;
                            }
                            CharArray.push(newFile);
                        } else {
                            ch.add(s);
                        }
                    }
                    NLconnect = ch.endsWith(NLconnector);   // connector がないときは falseを返す
                    connect = ch.endsWith(connector);   // connector がないときは falseを返す
                    if (NLconnect) {
                        ch.length -= NLconnector.length;
                    } else if (connect) {
                        ch.length -= connector.length;
                    } else {
                        if (not_include) enqueue(new CharArray(ch));
                        ch.reset();
                    }
                    if (debug  && not_include) System.out.println(queue.size()+"| "+s);
                    if (socket != null && 
                        (s.startsWith("[END]")||s.startsWith("[End]"))) {
                        if (NLconnect || connect) queue.enqueue(new CharArray(ch));
                        break;
                    }
                } catch (IOException e4) {
                    System.out.println("QueueFile:Read IOExeption:"+e4);
                    rsts = false;
                    break;
                }
            } // enddo
            CharArray.push(ch2);
            CharArray.push(ch);
        } catch (FileNotFoundException e6) {
            System.out.println("FileNotFoundException:"+e6);
            rsts = false;
        }
        
        if (in != null) {
            try {
                in.close();
            } catch (Exception e7) {
            }
            in = null;
        }
        return rsts;
    }
    
    /**
        改行変換＆コンバータ＆
        結合文字の処理をしながらコピー：未実装
    */
    public void convertCopy(CharArrayFile from) {
        clear();
        CharToken token = CharToken.pop();
        token.set(from,"\n");
        
        boolean NLconnect = false;
        boolean connect = false;
        CharArray ch = CharArray.pop();
        CharArray ch2 = CharArray.pop();
        int offset = 0;
        
        for (int i = 0; i < token.size(); i++) {
            String s = token.peek(i).toString();
//          if (cryptKey != null) { // 暗号化モード
//              // あとでCryptTextにする
//              CharArray to = isAscii ? CryptAscii.decode(new CharArray(s),cryptKey,offset)
//                                     : CryptText.decode(new CharArray(s),cryptKey,offset);
//              s = to.toString();
//              offset += s.length();
//          }
            if (converter != null) s = converter.convert(s);
            
            if (NLconnect) {
                ch.add("\n");
                ch.add(ch2.set(s));
            } else if (connect) {
                ch.add(ch2.set(s).trimL());
            } else {
                ch.add(s);
            }
            NLconnect = ch.endsWith(NLconnector);   // connector がないときは falseを返す
            connect = ch.endsWith(connector);   // connector がないときは falseを返す
            if (NLconnect) {
                ch.length -= NLconnector.length;
            } else if (connect) {
                ch.length -= connector.length;
            } else {
                /*if (not_include)*/ enqueue(new CharArray(ch));
                ch.reset();
            }
            //enqueue(new CharArray(token.peek(i)));
        }
        CharArray.push(ch2);
        CharArray.push(ch);
        CharToken.push(token);
    }
    
    
    /** ファイルにデータを書き込む<br>
        ファイル名はコンストラクタで定義されているもの **/
    public boolean write() { return write(szFileName);}

    /** ファイルにデータを書き込む
        @param szFileName 書き込みファイル名
    **/
    public boolean write(String szFileName) {
        boolean rsts = true;
        if (szFileName != null && size() > 0) {
            try {
                // ファイル名に基づきバッファードライタ・ストリームを生成する
                if (out == null || !appendMode) {
                    out = new BufferedWriter(new FileWriter(szFileName));
                }
                int offset = 0;
                for (int i = 0; i < size(); i++) {
                    try {
                        // class Vector オブジェクトから順番に要素を取り出す
                        CharArray ch = peek(i);
                        // ライタ・ストリームへデータを書き込む
                        if (ch == null) {
                            rsts = false;
                            break;
                        }
//                      if (cryptKey != null) { // 暗号化 あとで CryptText に
//                          CharArray to = isAscii ? CryptAscii.encode(ch, cryptKey, offset)
//                                                 : CryptText.encode(ch, cryptKey, offset);
//                          offset += ch.length();
//                          out.write(to.chars,0,to.length());
//                      } else {
                            out.write(ch.chars,0,ch.length());
//                      }
                        out.newLine();
                        out.flush();
                        if (debug) {
                            System.out.println((i+1)+"| "+ch+":"+ch.length());
                        }
                        
                    } catch (IOException e) {
                        System.out.println("IOExeption");
                        rsts = false;
                        break;
                    }
                }   // next
                //out.close();
                if (!appendMode && out != null) {
                    out.close(); 
                    out = null;
                }
            } catch (IOException e) {
                System.out.println("IOException(1)");
                rsts = false;
                if (!appendMode && out != null) {
                    try { out.close(); } catch (Exception ex) {}
                    out = null;
                }
            }
            
        } else rsts = false;
        return rsts;
    }

    /** ファイルにデータを書き込む
        @param szFileName 書き込みファイル名
        @param enc        エンコーディング
    **/
    public boolean write(String szFileName, String enc) {
        if (enc == null || enc.trim().length()== 0) enc = szDefaultEncoding;
        boolean rsts = true;
        if (szFileName != null && size() > 0) {
            try {
                // ファイル名に基づきバッファードライタ・ストリームを生成する
                if (out == null || !appendMode) {
                    out = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(szFileName),enc
                        ));
                }
                int offset = 0;
                for (int i = 0; i < size(); i++) {
                    try {
                        // class Vector オブジェクトから順番に要素を取り出す
                        CharArray ch = peek(i);
                        // ライタ・ストリームへデータを書き込む
                        if (ch == null) {
                            rsts = false;
                            break;
                        }
//                      if (cryptKey != null) { // 暗号化 あとで CryptText に
//                          CharArray to = isAscii ? CryptAscii.encode(ch, cryptKey, offset)
//                                                 : CryptText.encode(ch, cryptKey, offset);
//                          offset += ch.length();
//                          out.write(to.chars,0,to.length());
//                      } else {
                            out.write(ch.chars,0,ch.length());
//                      }
                        out.newLine();
                        out.flush();
                        if (debug) {
                            System.out.println((i+1)+"| "+ch+":"+ch.length());
                        }
                        
                    } catch (IOException e) {
                        System.out.println("IOExeption");
                        rsts = false;
                        break;
                    }
                }   // next
                //out.close();
                if (!appendMode && out != null) {
                    out.close(); 
                    out = null;
                }
            } catch (IOException e) {
                System.out.println("IOException(1)");
                rsts = false;
                if (!appendMode && out != null) {
                    try { out.close(); } catch (Exception ex) {}
                    out = null;
                }
            }
            
        } else rsts = false;
        return rsts;
    }

    public void setSilentMode(boolean mode) {silentMode = mode;}
    
    /** 最終行の後に追加する */
    public boolean add(CharArray ch) {
        if (!silentMode) {
            if (appendMode && size() >= iAppendMax) {
                write();
                clear();
            }
            enqueue(ch);
        }
        return true;
    }
    public boolean add(String s) {
        return add(new CharArray(s));
    }

    /** 指定行（0～)を取り出す */
    public CharArray get(int index) {
        return peek(index);
    }

    /** 指定行を置き換える **/
    public boolean put(int index, CharArray ch) {
        return poke(index, ch);
    }
    public boolean put(int index, String s) {
        return poke(index, new CharArray(s));
    }

//  /** 挿入  **/
//  
//  public boolean insert(int line,String s) {
//      if (line < 1 || line > this.line || pVec == null) return false;
//      pVec.insertElementAt(s,line-1);
//      return true;
//  }

//  /** 削除 **/
//  public boolean delete(int line) {
//      if (line < 1 || line > this.line || pVec == null) return false;
//      pVec.removeElementAt(line-1);
//      return true;
//  }

}

//
// [end of QueueFile.java]
//
