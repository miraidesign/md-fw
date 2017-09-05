//------------------------------------------------------------------------
// @(#)ItemRenderer.java
//                 Item を描画する
//                 Copyright (c) MiraiDesign 2010 All Rights Reserved. 
//------------------------------------------------------------------------
//
package com.miraidesign.renderer;

import com.miraidesign.util.CharArray;
import com.miraidesign.session.SessionObject;

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
 *  Item描画用インターフェース
 *
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public interface ItemRenderer {
    public CharArray drawAnchorString(CharArray ch, AnchorStringData itemData);
    public CharArray drawAnchorStringSrc(CharArray ch, AnchorStringData itemData);
    public CharArray drawAnchorStringMenu(CharArray ch, AnchorStringData itemData);
    
    //public CharArray drawBlock(CharArray ch, BlockData itemData);
    //public CharArray drawBlockStart(CharArray ch, BlockData itemData);
    //public CharArray drawBlockEnd(CharArray ch, BlockData itemData);
    
    //public CharArray drawButton(CharArray ch, ButtonData itemData);
    //public CharArray drawButtonName(CharArray ch, ButtonData itemData);
    
    public CharArray drawCheckBox(CharArray ch, CheckBoxData itemData);
    public CharArray drawCheckBox(CharArray ch, CheckBoxData itemData,int index);
    public CharArray drawCheckBoxName(CharArray ch, CheckBoxData itemData,int index);
    public CharArray drawCheckBoxHidden(CharArray ch, CheckBoxData itemData);
    public CharArray drawCheckBoxNameHidden(CharArray ch, CheckBoxData itemData, int index);
    public CharArray drawCheckBoxMenu(CharArray ch, CheckBoxData itemData,int index);
    
    public CharArray drawDynamic(SessionObject session, DynamicData itemData);
    
    public CharArray drawFile(CharArray ch, FileData itemData);
    public CharArray drawFileName(CharArray ch, FileData itemData);
    
    public CharArray drawForm(CharArray ch, FormData itemData);
    public CharArray drawFormStart(CharArray ch, FormData itemData);
    public CharArray drawFormEnd(CharArray ch, FormData itemData);
    public CharArray drawFormAction(CharArray ch, FormData itemData);
    public CharArray drawFormHidden(CharArray ch, FormData itemData);
    public CharArray drawFormActionHidden(CharArray ch, FormData itemData);
    
    //public CharArray drawHairLine(SessionObject session, HairLineData itemData);
    
    public CharArray drawHidden(CharArray ch, HiddenData itemData);
    public CharArray drawHiddenName(CharArray ch, HiddenData itemData);
    
    //public CharArray drawImage(CharArray ch, ImageData itemData);
    //public CharArray drawImageName(CharArray ch, ImageData itemData);
    
    public CharArray drawImg(CharArray ch, ImgData itemData);
    public CharArray drawImgSrc(CharArray ch, ImgData itemData);
    
    //public CharArray drawLineFeed(SessionObject session, LineFeedData itemData);
    //public CharArray drawLink(SessionObject session, LinkData itemData);
    
    public CharArray drawListBox(CharArray ch, ListBoxData itemData);
    public CharArray drawListBoxName(CharArray ch, ListBoxData itemData);
    public CharArray drawListBoxMenu(CharArray ch, ListBoxData itemData);
    public CharArray drawListBoxNameMenu(CharArray ch, ListBoxData itemData);
    public CharArray drawListBoxOption(CharArray ch, ListBoxData itemData,int index);
    public CharArray drawListBoxMenu(CharArray ch, ListBoxData itemData,int index);
    
    public CharArray drawPassword(CharArray ch, PasswordData itemData);
    public CharArray drawPasswordName(CharArray ch, PasswordData itemData);
    
    public CharArray drawRadioButton(CharArray ch, RadioButtonData itemData);
    public CharArray drawRadioButton(CharArray ch, RadioButtonData itemData, int index);
    public CharArray drawRadioButtonName(CharArray ch, RadioButtonData itemData, int index);
    public CharArray drawRadioButtonMenu(CharArray ch, RadioButtonData itemData, int index);
    
    public CharArray drawString(SessionObject session, CharArray ch, StringData itemData);
    
    public CharArray drawSubmit(CharArray ch, SubmitData itemData);
    public CharArray drawSubmitName(CharArray ch, SubmitData itemData);
    
    public CharArray drawTextArea(CharArray ch, TextAreaData itemData);
    public CharArray drawTextAreaName(CharArray ch, TextAreaData itemData);
    public CharArray drawTextAreaMenu(CharArray ch, TextAreaData itemData);
    public CharArray drawTextAreaNameMenu(CharArray ch, TextAreaData itemData);
    
    public CharArray drawText(CharArray ch, TextData itemData);
    public CharArray drawTextName(CharArray ch, TextData itemData);
}

//
//
// [end of ItemRenderer.java]
//

