//------------------------------------------------------------------------
//    MenuTree.java
//          メニューツリー情報
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
package com.miraidesign.system;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.IniFile;

/**
    メニューシステム用ツリー情報
**/

public class MenuTree {
    private final boolean debug = false;
    private MenuNode rootNode = new MenuNode();
    /** ルートノードを返す **/
    public MenuNode getRootNode() { return rootNode;}
    
    private IniFile ini;
    private String section;
    private String loginParameter = "";
    public String getLoginParameter() { return loginParameter;}
    //-----
    public MenuTree() { }
    
    /** IniFileより読み込む */
    public void init(IniFile ini, String section) {
        this.ini = ini;
        this.section = section;
        CharArray ch = ini.get(section,"loginParameter");
        if (ch != null && ch.trim().length() > 0) loginParameter= ch.toString();
        sub(rootNode,"");
    }
    
    private void sub(MenuNode parent, String _key) {
        CharToken token = CharToken.pop();
        CharToken token2 = CharToken.pop();
        for (int i = 1; ; i++) {
            String key = _key+i;
            CharArray data = ini.get(section,key);
            if (data == null) break;
//if (debug) System.out.println(data);
            token.set(data.trim());
            // MenuNode の作成
            MenuNode node = new MenuNode();
            parent.add(node);
            node.setTitle(token.get(0));
            node.setWidth(token.get(1));
            
            token2.set(token.get(2),"/");
            node.setColor(token2.get(0));
            if (token2.get(1) != null) node.setBgColor(token2.get(1));
            
            token2.set(token.get(3),"/");
            node.setColor2(token2.get(0));
            if (token2.get(1) != null) node.setBgColor2(token2.get(1));
            
            node.setBlankColor(token.get(4));
            node.setModuleName(token.get(5));
            CharArray ch = token.get(6);
            if (ch == null || ch.trim().length()==0) {
                node.setPageNo(-1);
            } else {
                node.setPageNo(ch.getInt());
            }
            node.setParameter(token.get(7));
            
if (debug) System.out.println(key+"◆"+node.getTitle());
            // 以下のキーの作成
            sub(node,key+"-");
        }
        CharToken.push(token2);
        CharToken.push(token);
    }
    
}

//
// [end of MenuNode.java]
//

