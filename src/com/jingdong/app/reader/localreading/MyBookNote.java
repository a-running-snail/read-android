package com.jingdong.app.reader.localreading;

import java.util.ArrayList;

import org.json.JSONObject;


import com.jingdong.app.reader.data.DataParser;

import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.user.LoginUser;


import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.widget.Toast;

public class MyBookNote {
    public BookNote bookNote;
    public ArrayList<MyBookNote> relateBookNotes=new ArrayList<MyBookNote>();//画笔记时，被再次覆盖了笔记区域。
    public ArrayList<RectF> rectFs=new ArrayList<RectF>();
    public boolean isChoiced=false;
 // 复制的最大字符限制
 	private static final int MAXIMUM_COPY_TEXT_COUNT = 200;
    private static final String SHARE_BOOK_URL="http://e.m.jd.com/ebook/";
 	private static final int MAXIMUM_COPY_COUNT = 50;
 	

	public final static int TYPE_DATA_NOTE=2;//数据类型
	public static final String KEY_ID = "id";//:***,			服务器书签表主键
	public static final String KEY_OFFSET = "offset";//笔记的startCharIndex.
	public static final String KEY_TOTALOFFFSET="totalOffset";//笔记的endCharIndex;
	public static final String KEY_X1="x1";//:***,			起始段
	public static final String KEY_Y1="y1";//:***,			起始词
	public static final String KEY_Z1= "z1";//:***,			起始字
	public static final String KEY_X2="x2";//:***,			结束段
	public static final String KEY_Y2="y2";//:***,			结束词
	public static final String KEY_Z2= "z2";//:***,			结束字
	public static final String KEY_FORMATE="ebookType";//格式
	public static final String KEY_NOTE = "note";//:***,			书签内容字符串
	public static final String KEY_DEVICE_TIME = "deviceTime";//:***,	创建时间
	public static final String KEY_VALID = "valid";//:***,		状态 0删除 1正常  
	public static final String KEY_REMARK="remark";
	public static final String KEY_COLOR="color";
	public static final String KEY_VERSION="version";
	public static final String KEY_USERNAME="userId";

	public final static int STATE_NO_MODIFY = 0;//Modify 同步完后状态修改这个状态。
	public final static int STATE_MODIFY = 1;//Modify 修改完后状态改变。
	public final static int STATE_ADD = 2;//Modify 添加笔记
	public final static int STATE_DEL = 3;//Modify 删除笔记
	
	public final static int STATE_SERVER_NORMAL = 1;//Modify
	public final static int STATE_SERVER_DEL = 0;//Modify
    
	
	public static final BookNote parse(JSONObject jsonObject,long bookId,long orderId,int bookFormate,String userName) {
		BookNote bookNote = null;
		if (jsonObject != null) {
			try {
				bookNote=new BookNote();
				bookNote.setBookId(bookId);
				bookNote.setOrderId(bookId);
				bookNote.setBookFormate(bookFormate);
				if(!TextUtils.isEmpty(userName)){
					bookNote.setUserName(userName);
				}else{
					bookNote.setUserName(LoginUser.getpin());
				}
				long serviceId =  DataParser.getLong(jsonObject, KEY_ID);
				if(serviceId>0){
					bookNote.setServicedId(serviceId);
				}
				String content=DataParser.getString(jsonObject, KEY_NOTE);
				bookNote.setContent(content);
				String remark=DataParser.getString(jsonObject, KEY_REMARK);
				if(TextUtils.isEmpty(remark)){
					bookNote.setHasRemark(false);
				}else{
					bookNote.setHasRemark(true);
					bookNote.setRemarkContent(remark);
				}
				//颜色暂屏蔽
//				int color=DataParser.getInt(jsonObject, KEY_COLOR);
//				if(color==-1){
					bookNote.setColor(0xFF33bad7);
//				}else{
//					bookNote.setColor(color);
//				}
				int startCharIndex=DataParser.getInt(jsonObject, KEY_OFFSET);
				int endCharIndex=DataParser.getInt(jsonObject, KEY_TOTALOFFFSET);
				double x1=DataParser.getDouble(jsonObject, KEY_X1);
				double y1=DataParser.getDouble(jsonObject, KEY_Y1);
				double z1=DataParser.getDouble(jsonObject, KEY_Z1);
				double x2=DataParser.getDouble(jsonObject, KEY_X2);
				double y2=DataParser.getDouble(jsonObject, KEY_Y2);
				double z2=DataParser.getDouble(jsonObject, KEY_Z2);
				if (bookFormate == LocalBook.FORMAT_EPUB) {
					if (startCharIndex > 0) {
						bookNote.setStartOffset(startCharIndex);
					}
					if (endCharIndex > 0) {
						bookNote.setEndOffset(endCharIndex);
					}
					if(x1>=0){
						bookNote.setStart_paragraph_index((int)x1);
					}
					if(y1>=0){
						bookNote.setStart_element_index((int)y1);
					}
					if(z1>=0){
						bookNote.setStart_char_index((int)z1);
					}
					if(x2>=0){
						bookNote.setEnd_paragraph_index((int)x2);
					}
					if(y2>=0){
						bookNote.setEnd_element_index((int)y2);
					}
					if(z2>=0){
						bookNote.setEnd_char_index((int)z2);
					}
					
				}else if(bookFormate==LocalBook.FORMAT_PDF){
					if (startCharIndex > 0) {
						bookNote.setPageNum(startCharIndex);
					}
					if(x1>0){
						bookNote.setStartXScale(x1);
					}
					if(y1>0){
						bookNote.setStartYScale(y1);
					}
					if(x2>0){
						bookNote.setEndXScale(x2);
					}
					if(y2>0){
						bookNote.setEndYScale(y2);
					}
				}
				
				long deviceTime=DataParser.getLong(jsonObject, KEY_DEVICE_TIME);
				long version=DataParser.getLong(jsonObject, KEY_VERSION);
				if(deviceTime>0){
					bookNote.setClientTime(deviceTime);
				}
				if(version>0){
					bookNote.setLastModifyTime(version);
				}
				
				int valid=DataParser.getInt(jsonObject, KEY_VALID);
				if(valid>-1){
					bookNote.setModifyState(valid);
				}
				return bookNote;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
    
    /**
     * 修改笔记颜色
     * @param pageBookNote
     * @param color
     */
    public static void modifyColor(final MyBookNote pageBookNote,final int color){
    	pageBookNote.bookNote.setColor(color);
    	//DBBookNoteHelper.updateColor( pageBookNote.bookNote.getBookNoteId(), color,pageBookNote.bookNote.getBookId());
    }

    
    
}
