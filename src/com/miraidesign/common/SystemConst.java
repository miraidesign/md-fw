//------------------------------------------------------------------------
//    SystemConst.java
//              System 定数
//              Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//             System定数等
//------------------------------------------------------------------------

package com.miraidesign.common;

/**
 *  システム定数
 *  
 *  @version 0.5 2010-04-01
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class SystemConst {
    // 出荷時にfinalをつけて全コンパイルを推奨
    
    /** デバッグコントロール */
    public /*final*/ static volatile boolean debug =  true;
    
    /** mixedIDを暗号化する */
    public /*final*/ static boolean cryptMixedID =  false;
    
    /**  sessionIDを暗号化する */
    public /*final*/ static boolean cryptSessionID = false;
    
    /**  セッションを Zip圧縮保管する */
    public /*final*/ static boolean zipCompress = false;
    
    /** セッションタイムアウト時間 */
    public /*final*/ static long sessionTimeout =  60*60*1000;  // １時間
    
    /** アクセス継続最大時間 */
    public /*final*/ static long accessMaxSec   = 3*60*60*1000;  // ３時間
    
//  /** カートオブジェクトのタイムアウト時間 */
//  public /*final*/ static long cartTimeout =   24*60*60*1000;  // 24時間
    
//  /** 検索テーブルのタイムアウト時間 */
//  public /*final*/ static long tableTimeout   = 5*60*1000;   // 5分
    
//  /** DAOタイムアウト時間 */
//  public /*final*/ static long daoTimeout   = 30*60*1000;   // 30分
    
//  /** ハブサイト会員コード **/
//  public /*final*/ static final String cdKey="hsmcd";
//  /** ハブサイト会員パスワード **/
//  public /*final*/ static final String pwKey="hsmpw";
    
    //----------------------------------------------------------
    // URL キーワード 
    //----------------------------------------------------------
    
    /** サイトコード（ハブサイト＋チャネルコード）指定用 **/
    public /*final*/ static final String siteKey="site";
    
    /** セッション-ID URL 作成時のキー文字列 **/     // 標準、暗号化キー
    public /*final*/ static final String[] sessionIDKey= {"S","s"};
    
    /** Mixed-ID  URL 作成時のキー文字列 **/        // 標準、暗号化キー
    public /*final*/ static final String[] mixedIDKey= {"M","m"};

    /** ジャンプ先ページ URL 作成時のキー文字列 ページ番号 **/
    public /*final*/ static final String pageIDKey="P";
    
    /** ジャンプ先ページ URL 作成時のキー文字列 アイテム番号 **/
    public /*final*/ static final String itemKey="N";
    
    /** ジャンプ先ページ URL 作成時のキー文字列 **/
    public /*final*/ static final String dataKey=".";
    
    /** ジャンプ先ページ URL 作成時のキー文字列 フレーム設定 **/
    public /*final*/ static final String frameKey="F";

    /** チェックボックスクリアフラグ **/
    public /*final*/ static final String clearKey="clear";
    
//  /** analyzeParameter 制御キー */
//  public /*final*/ static final String analizeParameterKey="ANP";

    /** 
        makePagePage 制御キー <br>
        falseに設定すると makePageが実行されない
    */
    public /*final*/ static final String makePageKey="MKP";
    
    /** 
        afterPage 制御キー <br>
        falseに設定すると makePageが実行されない
    */
    public /*final*/ static final String afterPageKey="AFP";
    
    /** ノードキー **/
    public /*final*/ static final String nodeKey="n";

    /** 親ノードキー **/
    public /*final*/ static final String parentNodeKey="p";
    
    /** ツリーキー **/
    public /*final*/ static final String treeKey="tr";
    
    /** タイムスタンプキー **/
    public /*final*/ static final String tmKey="tm";
    
    /** 認証IDキー **/
    public /*final*/ static final String authIDKey="auth";
    
    //----------------------------------------------------------
    // cookie キーワード 
    //----------------------------------------------------------
    
    /** PCID キー (ブラウザ別情報) **/
    public /*final*/ static final String pcIDKey="PCID";
    
    /** CART_ID プレフィクスキー （FWサイト別セッションID情報）**/ // 名称は変更する可能性がある
    public /*final*/ static final String cartIDKey="SC_";
    

}

//
// [end of SystemConst.java]
//

