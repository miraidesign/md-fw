//------------------------------------------------------------------------
//    InputUser.java
//          システム関数  @user で呼ばれる
//              Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------

package com.miraidesign.content.input;

import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharArrayQueue;
import com.miraidesign.util.HashParameter;
import com.miraidesign.util.Parameter;

/**
    システム関数（ユーザ情報呼び出しクラス）
*/
public class InputUser extends InputItem /* implements SessionEvent */ {
    private static boolean debug = false;
    private static boolean debug2 = false;

/////////////////////////////////////////////////////////
////// UserManager 今回はここに static で持ってしまう
//  
//                          // ユーザーID
//  private static Hashtable<IntObject, InputUser> hash;
//  
//  /* ユーザーインスタンスを取得する 
//      @param session 
//      @param user_id  ユーザーID
//  */
//  // 成りすまし対策にセッションIDを利用することも考えられるが
//  public static InputUser getInputUser(SessionObject session, int user_id) {
//      InputUser inputUser = null;
//      if (session != null) {
//          if (hash == null) hash = new Hashtable<IntObject, InputUser>();
//          IntObject obj = IntObject.pop(user_id);
//          inputUser = hash.get(obj);
//          
//          if (inputUser == null) {
//              inputUser = new InputUser(user_id);
//              hash.put(obj, inputUser);
//          } else {
//              IntObject.push(obj);
//          }
//      }
//      return inputUser;
//  }
//  
//  /* ユーザー情報を全てクリアする */
//  public static void clear() {
//      if (hash != null) hash.clear();
//  }
//  
//  
////// UserManager End
/////////////////////////////////////////////////////////
//  // SessionEvent Interface
//  
//  public void reset() {
//      if (hash != null) {
//          IntObject obj = IntObject.pop(user_id);
//          hash.remove(obj);
//          IntObject.push(obj);
//      }
//  }
//  
//
//  /////////////////////////////////////////////////////
//  // member 変数
//  public long lastUpdateTime = 0; // 現在は未使用
//  
//  private int user_id = 0;        // 現在は未使用

    //-------------------
    // constructor
    //-------------------
    public InputUser() {
        type.set("USER");
    }
    
//  /* 
//      ユーザー情報クラスを生成します
//      @param id   user_id
//  */
//  public InputUser(int id) {
//      type.set("USER");
//      this.user_id = id;
//  }
    
    //-------------------
    // method
    //-------------------
    public void copy(InputUser from) {
//      this.user_id = from.user_id;
    }
    
    /** デフォルト関数 */
    public CharArray get() {
        return new CharArray();
    }
    
    public void set(CharArray ch) {
        
    }
    
    /* ユーザIDを取得する */
    public int getID(SessionObject session) { 
        int id = 0;
        do {
            if (session == null) break;
            HashParameter hash = session.getUserData();
            if (hash != null) id = hash.getInt("user_id");
        } while (false);
        return id; 
    }
    
    /* ユーザー名を取得する */
    public CharArray getName(SessionObject session) { 
        CharArray ch = null;
        do {
            if (session == null) break;
            HashParameter hash = session.getUserData();
            if (hash != null) ch = hash.getCharArray("user_name");
        } while (false);
        return ch; 
    }
    
    /* ニックネーム取得する */
    public CharArray getNickName(SessionObject session) { 
        CharArray ch = null;
        do {
            if (session == null) break;
            HashParameter hash = session.getUserData();
            if (hash != null) ch = hash.getCharArray("nickname");
        } while (false);
        return ch; 
    }
    
    /* 端末IDを取得する */
    public CharArray getSubID(SessionObject session) { 
        CharArray ch = null;
        do {
            if (session == null) break;
            ch = session.userAgent.deviceID;
        } while (false);
        return ch; 
    }
    
    /* 会員か？ */
    public boolean isMember(SessionObject session) { 
        boolean is_member = false;
        do {
            if (session == null) break;
            HashParameter hash = session.getUserData();
            if (hash != null) is_member = hash.getBoolean("is_member");
        } while (false);
if (debug2) System.out.println(session.count+"|◆@user.is_member="+is_member);
        return is_member; 
    }
    
    /* スーパーユーザーか？ */
    public boolean isSuperUser(SessionObject session) { 
        boolean is_superuser = false;
        do {
            if (session == null) break;
            HashParameter hash = session.getUserData();
            if (hash != null) is_superuser = (hash.getInt("user_type") == 9);
        } while (false);
        return is_superuser; 
    }
    
    /* MyTopMode のチェック
        パラメータと比較する
    */
    public boolean isMyTopMode(CharArrayQueue param, SessionObject session) { 
        boolean is_mytopmode = false;
        int no = -1;
        int mode = -1;
        do {
            if (session == null) break;
            if (param == null || param.size() == 0) break;
            no = CharArray.getInt(param.peek());
            //if (no == 0) break;
            
            HashParameter hash = session.getUserData();
            if (hash == null) break;
if (debug2) {
    System.out.println("user-data");
    hash.debugParameter(session);
}
            mode = hash.getInt("mytop_mode");
            is_mytopmode = (no == mode);
        } while (false);
if (debug2) System.out.println(session.count+"|◆@user.is_mytopmode("+no+")==mytop_mode("+mode+")? ="+is_mytopmode);
        return is_mytopmode; 
    }
    
    //////////////////////////////////////////////////////////////////////////////
    // 関数呼び出し元
    //////////////////////////////////////////////////////////////////////////////
    
    /*
        関数呼び出し
        @param func 関数名 
        @param param 関数パラメータ
    */
    public CharArray getFunc(CharArray func, CharArrayQueue param) {
        return getFunc(func, param, 0);
    }
    
    public CharArray getFunc(CharArray func, CharArrayQueue param, int state) {
if (debug) System.out.println("▽▽InputUser#getFunc▽▽");
        CharArray ch = null;
        if (func == null) {
            ch = get();
        } else {
            func.trim().toLowerCase();
            if (func.length() == 0)              ch = get();
            //else if (func.equals("link"))        ch = getLink(param);
            //else if (func.equals("description")) ch = getDescription(param);
            //else if (func.equals("title"))       ch = getTitle(param);
            //else if (func.equals("disp"))        ch = getDisp(param,null);
            //else if (func.equals("view"))        ch = getDisp(param,null);
            else                                 ch = get();
        }
if (debug) System.out.println("△△InputUser#getFunc△△");
        return ch;
    }
    /*
        関数呼び出し（CMSが利用するのはこちらのみ）
        @param func  関数名 
        @param param 関数パラメータ
        @param p     戻り値用
        @param session
    */
    public Parameter getParameter(CharArray func, CharArrayQueue param, 
                                    Parameter p,SessionObject session) {
if (debug) System.out.println("▽▽InputUser#getParameter▽▽:"+func);
        if (func == null) {
            p.add(get());
        } else {
            func.trim().toLowerCase();
            if (func.length() == 0)             p.add(get());
            else if (func.equals("id")) {
                int _id = getID(session);
                p.add((_id <= 0) ? "" :""+_id);
            }
            else if (func.equals("name"))          p.add(getName(session));
            else if (func.equals("nickname"))      p.add(getNickName(session));
            else if (func.equals("sub_id"))        p.add(getSubID(session));
            else if (func.equals("is_member"))     p.add(isMember(session)? "member": "");
            else if (func.equals("is_superuser"))  p.add(isSuperUser(session)? "super_user": "");

            else if (func.equals("is_mytopmode"))  {
                p.add(isMyTopMode(param, session)? "mytop_mode:"+session.getUserData().get("mytop_mode") : "");
            }
            //else if (func.equals("height"))     p.add(getHeight(param,session));
            //else if (func.equals("view"))       p.add(getView());
            else   {
                System.out.println("@user:関数が見つかりません["+func+"]");
                p.add("");
            }
        }
if (debug) System.out.println("△△InputUser#getParameter△△:"+func);
        return p;
    }

    /**
        関数設定
        @param func  関数名 
        @param param 関数パラメータ
        @return true 設定成功
    */
    public boolean setFunc(CharArray func, CharArrayQueue param) {
        boolean sts = true;
        /*
        if (func != null && param != null && param.size() > 1) {
            CharArray ch = param.peek();
            func.trim().toLowerCase();
            if (func.length() == 0)            setSrc(ch);
            else if (func.equals("main"))      setSrc(ch);
            else                               sts = false;
        } else sts = false;
        */
        return sts;
    }

    /**
        関数設定
        @param func  関数名 
        @param ch 関数パラメータ
        @return true 設定成功
    */
    public boolean setFunc(CharArray func, CharArray ch) {
        boolean sts = true;
        /*
        if (func != null) {
            func.trim().toLowerCase();
            if (func.length() == 0)            setSrc(ch);
            else if (func.equals("main"))      setSrc(ch);
            else                               sts = false;
        } else sts = false;
        */
        return sts;
    }


}

//
// [end of InputUser.java]
//

