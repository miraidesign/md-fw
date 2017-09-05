//------------------------------------------------------------------------
//    ContentItemNode.java
//      Block & InputItem 情報
//                 Copyright (c) Mirai Design 2010 All Rights Reserved. 
//------------------------------------------------------------------------
package com.miraidesign.content;

import javax.swing.tree.DefaultMutableTreeNode;

import com.miraidesign.util.CharArray;
import com.miraidesign.util.CharToken;
import com.miraidesign.util.IntQueue;
import com.miraidesign.content.input.InputItem;

/**
    Block / InputItem 情報
*/
public class ContentItemNode extends DefaultMutableTreeNode {
    int level = 0;
    int total = 1;
    int max = 0;
    public ContentItemNode() { 
        super();
    }
    public ContentItemNode(ContentItem item) { 
        super();
        super.setUserObject(item);
        init(item);
    }
    public ContentItemNode(ContentItem item ,boolean allowsChildren) { 
        super();
        super.setAllowsChildren(allowsChildren);
        super.setUserObject(item);
        init(item);
    }
    
    private void init(ContentItem item) {
        
        if (item.isBlock()) {
            ((Block)item).setContentItemNode(this);
            max = total = ((Block)item).getMax();
        }
        if (item.isInput()) {
            ((Input)item).setContentItemNode(this);
        }
    }
    
    public int getMax() {
        if (isBlock()) {
            max = getBlock().getMax();
        }
        return max;
    }
    
    public int getTotal() {
        int total = getMax();
        if (isBlock()) {
            ContentItemNode parent = (ContentItemNode)getParent();
            while (!parent.isRootNode()) {
                total *= parent.getMax();
                parent = (ContentItemNode)parent.getParent();
            }
        }
        return total;
    }
    
    
    /* このノードの子配列にある、指定されたインデックスの子を返します */
    public ContentItemNode getChildNodeAt(int index) {
        return (ContentItemNode)super.getChildAt(index);
    }
    ////////////////////////////////////////////////////////
    public void add(ContentItemNode node) {
        super.add(node);
        node.setLevel(level+1);
        node.total = total * node.max;
    }
    
    protected void setLevel(int level) { this.level = level;}
    /* レベルを取得する */
    public int getLevel() { return level;}
    
    /* ルートノードか？*/
    public boolean isRootNode() {
        return (level == 0);
    }
    
    public boolean isBlock() {
        ContentItem item = (ContentItem)getUserObject();
        return (item != null && item.isBlock());
    }
    public boolean isInput() {
        ContentItem item = (ContentItem)getUserObject();
        return (item != null && item.isInput());
    }
    public ContentItem getContentItem() {
        return (ContentItem)getUserObject();
    }
    public Block getBlock() {
        return isBlock() ? (Block)getUserObject() : null;
    }
    
    public Input getInput() {
        return isInput() ? (Input)getUserObject() : null;
    }
    
    public boolean isLastBlock() {
        boolean sts = false;
        if (isBlock()) {
            sts = true;
            for (int i = 0; i < getChildCount(); i++) {
                ContentItemNode node = getChildNodeAt(i);
                if (node.isBlock()) {
                    sts = false;
                    break;
                }
            }
        }
        return sts;
    }
    
    public void exchange(ContentItemInfo info, String change_list) {
        CharArray ch = CharArray.pop(change_list);
        exchange(info, ch);
        CharArray.push(ch);
    }
    public void exchange(ContentItemInfo info, CharArray change_list) {
//System.out.println("★ExChange:"+change_list);
        IntQueue queue = new IntQueue();
        IntQueue noQueue = new IntQueue();
        CharToken token = CharToken.pop();
        token.set(change_list,",");
        for (int i = 0; i < token.size(); i++) {
            queue.enqueue(token.getInt(i));
            noQueue.enqueue(i+1);
        }
        CharToken.push(token);
        for (int j = 0; j < getChildCount(); j++) {
            ContentItemNode node = getChildNodeAt(j);
            if (node.isBlock()) {
                System.out.println("CotentItemNode:exchange error!!");
                break;
            }
            for (int i = 1; i <= queue.size(); i++) {
                noQueue.poke(i-1,i);
            }
            CharArray key = node.getInput().getKey();
            
            for (int i = 1; i < queue.size(); i++) {
                int num = queue.peek(i-1);      // 新番号
                int tmp = noQueue.peek(i-1);    // 今入っているもの
                if (num != tmp) {
                    for (int k = i+1; k <= queue.size(); k++) {
                        tmp = noQueue.peek(k-1);
                        if (num == tmp) {
                            InputItem item1 = info.getInputItem(key,i);
                            InputItem item2 = info.getInputItem(key,k);
                            item1.exchange(item2);
                            noQueue.exchange(i-1,k-1);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    String szLevel="■■■■■■■■■■■■■■■■■■";
    public void debugNode() {
        CharArray ch = new CharArray();
        ch.add(szLevel.substring(0,level));
        ch.add("[");
        if (isRootNode()) {
            ch.add("[Root]");
        }
        if (isBlock()) {
            ch.add("Block:");
            ch.add(getBlock().getKey());
        }
        if (isInput()) {
            ch.add("Input:");
            ch.add(getInput().getKey());
        }
        ch.add("]");
        if (isBlock()) {
            ch.add(" max:"+getMax());
            ch.add(" total:"+getTotal());
        }
        System.out.println(ch);
        for (int j = 0; j < getChildCount(); j++) {
            ContentItemNode node = getChildNodeAt(j);
            node.debugNode();
        }
    }
    
}

//
// [end of ContentItemNode.java]
//

