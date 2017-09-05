//------------------------------------------------------------------------
// @(#)XhtmlRenderer.java
//                 XHTMLレンダラー
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
package com.miraidesign.renderer.xhtml;

import com.miraidesign.renderer.ItemRenderer;
import com.miraidesign.session.SessionObject;
import com.miraidesign.util.CharArray;

import com.miraidesign.renderer.item.AnchorStringData;
//import com.miraidesign.renderer.item.BlockData;
//import com.miraidesign.renderer.item.ButtonData;
import com.miraidesign.renderer.item.CheckBoxData;
import com.miraidesign.renderer.item.DynamicData;
import com.miraidesign.renderer.item.FileData;
import com.miraidesign.renderer.item.FormData;
//import com.miraidesign.renderer.item.HairLineData;
import com.miraidesign.renderer.item.HiddenData;
//import com.miraidesign.renderer.item.ImageData;
import com.miraidesign.renderer.item.ImgData;
//import com.miraidesign.renderer.item.LinkData;
import com.miraidesign.renderer.item.ListBoxData;
import com.miraidesign.renderer.item.PasswordData;
import com.miraidesign.renderer.item.RadioButtonData;
import com.miraidesign.renderer.item.StringData;
import com.miraidesign.renderer.item.SubmitData;
import com.miraidesign.renderer.item.TextAreaData;
import com.miraidesign.renderer.item.TextData;

/**
 *  Xhtmlレンダラー
 *
 *  @version 0.5 2010-04-06
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class XhtmlRenderer implements ItemRenderer {
    static private XhtmlRenderer instance;   // singleton
    
    /** インスタンスを取得する(singleton) 
        @return XhtmlRenderer のインスタンス
    */
    static public XhtmlRenderer getInstance() {
        if (instance == null) instance = new XhtmlRenderer();
        return instance;
    }
    
    /** constructor */
    private XhtmlRenderer() { }
    
    public CharArray drawAnchorString(CharArray ch, AnchorStringData itemData) {
        return AnchorStringRenderer.draw(ch, itemData);
    }
    public CharArray drawAnchorStringSrc(CharArray ch, AnchorStringData itemData) {
        return AnchorStringRenderer.drawSrc(ch, itemData);
    }
    public CharArray drawAnchorStringMenu(CharArray ch, AnchorStringData itemData) {
        return AnchorStringRenderer.drawMenu(ch, itemData);
    }
    
//  public CharArray drawBlock(CharArray ch, BlockData itemData) {
//      return BlockRenderer.draw(ch, itemData);
//  }
//  public CharArray drawBlockStart(CharArray ch, BlockData itemData) {
//      return BlockRenderer.draw_start(ch, itemData);
//  }
//  public CharArray drawBlockEnd(CharArray ch, BlockData itemData) {
//      return BlockRenderer.draw_end(ch, itemData);
//  }

//  public CharArray drawButton(CharArray ch, ButtonData itemData) {
//      return ButtonRenderer.draw(ch, itemData);
//  }
//  public CharArray drawButtonName(CharArray ch, ButtonData itemData) {
//      return ButtonRenderer.drawName(ch, itemData);
//  }
    
    public CharArray drawCheckBox(CharArray ch, CheckBoxData itemData) {
        return CheckBoxRenderer.draw(ch, itemData);
    }
    // index = -1 でダミーデータを描画する
    public CharArray drawCheckBox(CharArray ch, CheckBoxData itemData, int index) {
        return CheckBoxRenderer.draw(ch, itemData, index);
    }
    public CharArray drawCheckBoxName(CharArray ch, CheckBoxData itemData, int index) {
        return CheckBoxRenderer.drawName(ch, itemData, index);
    }
    public CharArray drawCheckBoxHidden(CharArray ch, CheckBoxData itemData) {
        return CheckBoxRenderer.drawHidden(ch, itemData);
    }
    public CharArray drawCheckBoxNameHidden(CharArray ch, CheckBoxData itemData, int index) {
        return CheckBoxRenderer.drawNameHidden(ch, itemData, index);
    }
    public CharArray drawCheckBoxMenu(CharArray ch, CheckBoxData itemData, int index) {
        return CheckBoxRenderer.drawMenu(ch, itemData, index);
    }
    
    public CharArray drawDynamic(SessionObject session, DynamicData itemData) {
        return DynamicRenderer.draw(session, itemData);
    }
    
    public CharArray drawFile(CharArray ch, FileData itemData) {
        return FileRenderer.draw(ch, itemData);
    }
    public CharArray drawFileName(CharArray ch, FileData itemData) {
        return FileRenderer.drawName(ch, itemData);
    }
    
    public CharArray drawForm(CharArray ch, FormData itemData) {
        return FormRenderer.draw(ch, itemData);
    }
    public CharArray drawFormStart(CharArray ch, FormData itemData) {
        return FormRenderer.draw_start(ch, itemData);
    }
    public CharArray drawFormEnd(CharArray ch, FormData itemData) {
        return FormRenderer.draw_end(ch, itemData);
    }
    public CharArray drawFormAction(CharArray ch, FormData itemData) {
        return FormRenderer.drawAction(ch, itemData);
    }
    public CharArray drawFormHidden(CharArray ch, FormData itemData) {
        return FormRenderer.drawHidden(ch, itemData);
    }
    public CharArray drawFormActionHidden(CharArray ch, FormData itemData) {
        return FormRenderer.drawActionHidden(ch, itemData);
    }
    
//  public CharArray drawHairLine(SessionObject session, HairLineData itemData) {
//      return HairLineRenderer.draw(session, itemData);
//  }
    
    public CharArray drawHidden(CharArray ch, HiddenData itemData) {
        return HiddenRenderer.draw(ch, itemData);
    }
    public CharArray drawHiddenName(CharArray ch, HiddenData itemData) {
        return HiddenRenderer.drawName(ch, itemData);
    }
    
//  public CharArray drawImage(CharArray ch, ImageData itemData) {
//      return ImageRenderer.draw(ch, itemData);
//  }
//  public CharArray drawImageName(CharArray ch, ImageData itemData) {
//      return ImageRenderer.drawName(ch, itemData);
//  }

    public CharArray drawImg(CharArray ch, ImgData itemData) {
        return ImgRenderer.draw(ch, itemData);
    }
    public CharArray drawImgSrc(CharArray ch, ImgData itemData) {
        return ImgRenderer.drawSrc(ch, itemData);
    }
    
//  public CharArray drawLineFeed(SessionObject session, LineFeedData itemData) {
//      return LineFeedRenderer.draw(session, itemData);
//  }
//  public CharArray drawLink(SessionObject session, LinkData itemData) {
//      return LinkRenderer.draw(session, itemData);
//  }

    public CharArray drawListBox(CharArray ch, ListBoxData itemData) {
        return ListBoxRenderer.draw(ch, itemData);
    }
    public CharArray drawListBoxName(CharArray ch, ListBoxData itemData) {
        return ListBoxRenderer.drawName(ch, itemData);
    }
    public CharArray drawListBoxMenu(CharArray ch, ListBoxData itemData) {
        return ListBoxRenderer.drawMenu(ch, itemData);
    }
    public CharArray drawListBoxNameMenu(CharArray ch, ListBoxData itemData) {
        return ListBoxRenderer.drawNameMenu(ch, itemData);
    }
    public CharArray drawListBoxOption(CharArray ch, ListBoxData itemData, int index) {
        return ListBoxRenderer.drawOption(ch, itemData, index);
    }
    public CharArray drawListBoxMenu(CharArray ch, ListBoxData itemData, int index) {
        return ListBoxRenderer.drawMenu(ch, itemData, index);
    }
    
    public CharArray drawPassword(CharArray ch, PasswordData itemData) {
        return PasswordRenderer.draw(ch, itemData);
    }
    public CharArray drawPasswordName(CharArray ch, PasswordData itemData) {
        return PasswordRenderer.drawName(ch, itemData);
    }
    
    public CharArray drawRadioButton(CharArray ch, RadioButtonData itemData) {
        return RadioButtonRenderer.draw(ch, itemData);
    }
    public CharArray drawRadioButton(CharArray ch, RadioButtonData itemData, int index) {
        return RadioButtonRenderer.draw(ch, itemData, index);
    }
    public CharArray drawRadioButtonName(CharArray ch, RadioButtonData itemData, int index) {
        return RadioButtonRenderer.drawName(ch, itemData, index);
    }
    public CharArray drawRadioButtonMenu(CharArray ch, RadioButtonData itemData, int index) {
        return RadioButtonRenderer.drawMenu(ch, itemData, index);
    }

    public CharArray drawString(SessionObject session, CharArray ch, StringData itemData) {
        return StringRenderer.draw(session, ch, itemData);
    }
    
    public CharArray drawSubmit(CharArray ch, SubmitData itemData) {
        return SubmitRenderer.draw(ch, itemData);
    }
    public CharArray drawSubmitName(CharArray ch, SubmitData itemData) {
        return SubmitRenderer.drawName(ch, itemData);
    }
    
    public CharArray drawTextArea(CharArray ch, TextAreaData itemData) {
        return TextAreaRenderer.draw(ch, itemData);
    }
    public CharArray drawTextAreaName(CharArray ch, TextAreaData itemData) {
        return TextAreaRenderer.drawName(ch, itemData);
    }
    public CharArray drawTextAreaMenu(CharArray ch, TextAreaData itemData) {
        return TextAreaRenderer.drawMenu(ch, itemData);
    }
    public CharArray drawTextAreaNameMenu(CharArray ch, TextAreaData itemData) {
        return TextAreaRenderer.drawNameMenu(ch, itemData);
    }
    
    public CharArray drawText(CharArray ch, TextData itemData) {
        return TextRenderer.draw(ch, itemData);
    }
    public CharArray drawTextName(CharArray ch, TextData itemData) {
        return TextRenderer.drawName(ch, itemData);
    }
    
}

//
// [end of XhtmlRenderer.java]
//

