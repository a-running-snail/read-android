package com.jingdong.app.reader.localreading;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.data.db.DataProvider;
import com.jingdong.app.reader.entity.LocalBook;

public class BookNote {
	private long bookNoteId;
	private long servicedId;
	private long bookId;
	private long orderId;
	private String userName;
	private int bookFormate;
	private int start_paragraph_index;//epub
	private int start_element_index;
	private int start_char_index;
	private int end_paragraph_index;
	private int end_element_index;
	private int end_char_index;
	private int startOffset;//开始的偏移量
	private int endOffset;//结束的偏移量
	private int pageNum;//pdf;
	private double startXScale;

	private double startYScale;
	private double endXScale;
	private double endYScale;
	private String content;
	private boolean hasRemark;
	private String remarkContent="";
	private int color=0xFF33bad7;
	private long lastModifyTime;//最后修改时间 //保存服务器时间，用于服务端同步。
	private long createTime;//创建时间。本地的笔记用本地时间，同步后，如果是新增的用服务端的时间。
	private long clientTime;//客户端时间
	private int modifyState;//修改状态。
	private boolean isDel;
	public ArrayList<MyBookNote> myBookNotes=new ArrayList<MyBookNote>();//与该笔记相关的MyBookNote;
 
	public final static Uri URI_NOTES = DataProvider.CONTENT_URI_NAME_MYBOOKNOTE;
	
	public final static String[] ALL_PROJECTION = new String[] {"_id","server_id","book_id","order_id",
		"userName","bookFormate","startAndEndBookMark","startIndex","endIndex","content","hasMark",
		"remark","color","createTime","lastModifyTime","clientTime","modifyState"};

	public long getBookNoteId() {
		return bookNoteId;
	}

	public void setBookNoteId(long bookNoteId) {
		this.bookNoteId = bookNoteId;
	}
 	public boolean isDel() {
		return isDel;
	}
 	
 	public void setDel(boolean isDel){
 		this.isDel=isDel;
 	}

	public long getServicedId() {
		return servicedId;
	}

	public void setServicedId(long servicedId) {
		this.servicedId = servicedId;
	}

	public long getBookId() {
		return bookId;
	}

	public void setBookId(long bookId) {
		this.bookId = bookId;
	}

	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getBookFormate() {
		return bookFormate;
	}

	public void setBookFormate(int bookFormate) {
		this.bookFormate = bookFormate;
	}

	public int getStart_paragraph_index() {
		return start_paragraph_index;
	}

	public void setStart_paragraph_index(int start_paragraph_index) {
		this.start_paragraph_index = start_paragraph_index;
	}

	public int getStart_element_index() {
		return start_element_index;
	}

	public void setStart_element_index(int start_element_index) {
		this.start_element_index = start_element_index;
	}

	public int getStart_char_index() {
		return start_char_index;
	}

	public void setStart_char_index(int start_char_index) {
		this.start_char_index = start_char_index;
	}

	public int getEnd_paragraph_index() {
		return end_paragraph_index;
	}

	public void setEnd_paragraph_index(int end_paragraph_index) {
		this.end_paragraph_index = end_paragraph_index;
	}

	public int getEnd_element_index() {
		return end_element_index;
	}

	public void setEnd_element_index(int end_element_index) {
		this.end_element_index = end_element_index;
	}

	public int getEnd_char_index() {
		return end_char_index;
	}

	public void setEnd_char_index(int end_char_index) {
		this.end_char_index = end_char_index;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isHasRemark() {
		return hasRemark;
	}

	public void setHasRemark(boolean hasRemark) {
		this.hasRemark = hasRemark;
	}

	public String getRemarkContent() {
		return remarkContent;
	}

	public void setRemarkContent(String remarkContent) {
		this.remarkContent = remarkContent;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public long getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(long lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getClientTime() {
		return clientTime;
	}

	public void setClientTime(long clientTime) {
		this.clientTime = clientTime;
	}
	
	public int getPageNum() {
		return pageNum;
	}
	public double getStartXScale() {
		return startXScale;
	}

	public void setStartXScale(double startXScale) {
		this.startXScale = startXScale;
	}

	public double getStartYScale() {
		return startYScale;
	}

	public void setStartYScale(double startYScale) {
		this.startYScale = startYScale;
	}

	public double getEndXScale() {
		return endXScale;
	}

	public void setEndXScale(double endXScale) {
		this.endXScale = endXScale;
	}

	public double getEndYScale() {
		return endYScale;
	}

	public void setEndYScale(double endYScale) {
		this.endYScale = endYScale;
	}
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}

	public String bookMarkToJson(int formate){
		StringBuilder builder=new StringBuilder("");
		if(formate==LocalBook.FORMAT_EPUB){
			builder.append("{'startParagraph'").append(":").append(start_paragraph_index).append(",")
			.append("'startElement'").append(":").append(start_element_index).append(",")
			.append("'startChar'").append(":").append(start_char_index).append(",")
			.append("'endParagraph'").append(":").append(end_paragraph_index).append(",")
			.append("'endElement'").append(":").append(end_element_index).append(",")
			.append("endChar").append(":").append(end_char_index).append("}");
		}else if(formate==LocalBook.FORMAT_PDF){
			builder.append("{'pageNum'").append(":").append(pageNum).append(",")
			.append("'startXScale'").append(":").append(startXScale).append(",")
			.append("'startYScale'").append(":").append(startYScale).append(",")
			.append("'endXScale'").append(":").append(endXScale).append(",")
			.append("'endYScale'").append(":").append(endYScale).append(",")
			.append("}");
		}
		return builder.toString();
	}
	public void jsonToBookMark(String json,int formate){
		try {
			JSONObject jsonObject=new JSONObject(json);
			if(formate==LocalBook.FORMAT_EPUB){
				this.start_paragraph_index=jsonObject.getInt("startParagraph");
				this.start_element_index=jsonObject.getInt("startElement");
				this.start_char_index=jsonObject.getInt("startChar");
				this.end_paragraph_index=jsonObject.getInt("endParagraph");
				this.end_element_index=jsonObject.getInt("endElement");
				this.end_char_index=jsonObject.getInt("endChar");
			}else if(formate==LocalBook.FORMAT_PDF){
				this.pageNum=jsonObject.getInt("pageNum");
				this.startXScale=jsonObject.getInt("startXScale");
				this.startYScale=jsonObject.getInt("startYScale");
				this.endXScale=jsonObject.getInt("endXScale");
				this.endYScale=jsonObject.getInt("endYScale");
			}
			
		} catch (JSONException e) {
			Log.e("MyBookNote", "------------------->"+e.toString());
 			e.printStackTrace();
		}
	}
	public int getModifyState() {
		return modifyState;
	}

	public void setModifyState(int modifyState) {
		this.modifyState = modifyState;
	}
	
	public  boolean saveBookNote(boolean isClientId){
		boolean isSuccess = false;
		String selection = "";
	      if(isClientId){
	   		selection = "book_id=" + bookId + " AND _id=" + bookNoteId;
	      }else{
	  		selection = "book_id=" + bookId + " AND server_id=" + servicedId;
	      }
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("server_id",this.servicedId);
		values.put("book_id", this.bookId);
		values.put("order_id", this.orderId);
		values.put("userName", this.userName);
		values.put("bookFormate", this.bookFormate);
		values.put("startAndEndBookMark", this.bookMarkToJson(this.bookFormate));
		values.put("startIndex", this.startOffset);
		values.put("endIndex", this.endOffset);
		values.put("content", this.content);
		if(this.hasRemark){
			values.put("hasMark", 1);
		}else{
			values.put("hasMark", 0);
		}
		values.put("remark", this.remarkContent);
		values.put("color", this.color);
		values.put("createTime", this.createTime);
		values.put("lastModifyTime", this.lastModifyTime);
		values.put("clientTime", this.clientTime);
		values.put("modifyState", this.modifyState);
		Uri uri=null;
		int num = (mContentResolver.update(URI_NOTES, values, selection, null));
		if (num == 0) {
			uri = mContentResolver.insert(URI_NOTES, values);
		}
		if (uri != null|| num > 0) {
			if(uri!=null)
				this.bookNoteId=Long.valueOf(uri.toString());
 			isSuccess = true;
		} else {
			isSuccess = false;
		}
		MZBookApplication.getContext().getContentResolver()
		.notifyChange(URI_NOTES, null);
		if(isClientId && isSuccess){
			//LocalBook.saveNoteOperatingState(MyBookNote.STATE_MODIFY, bookId);
		}
		return isSuccess;
	}
	/**
	 * 获取某本书的所有笔记
	 * @return
	 */
	public static ArrayList<BookNote> getAllMyBookNotes(long bookId){
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		ArrayList<BookNote> bookNotes=new ArrayList<BookNote>();
		String selection="book_id=="+bookId+" AND "+"modifyState <>"+MyBookNote.STATE_DEL;
		Cursor cursor=null;
		cursor=mContentResolver.query(URI_NOTES, ALL_PROJECTION, selection, null, null);
		if(cursor==null) return null;
		while (!cursor.isAfterLast()){
			if(cursor.isBeforeFirst()){
				cursor.moveToNext();
			}else {
				BookNote bookNote=getMyBookNote(cursor);
				bookNotes.add(bookNote);
				cursor.moveToNext();
			}
		}
		return bookNotes;
	}
	/**
	 * 获取某一条笔记
	 * @param cur
	 * @return
	 */
	public static  BookNote  getBookNote(long bookNoteId){
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		BookNote bookNote=null;
		String selection="_id=="+bookNoteId;
		Cursor cursor=null;
		cursor=mContentResolver.query(URI_NOTES, ALL_PROJECTION, selection, null, null);
	  if(cursor!=null){
			while (!cursor.isAfterLast()){
				if(cursor.isBeforeFirst()){
					cursor.moveToNext();
				}else {
		            bookNote=getMyBookNote(cursor);
					cursor.moveToNext();
				}
			}
	   }
		return bookNote;
	}
	
	public static BookNote getMyBookNote(Cursor cur){
		BookNote bookNote=new BookNote();
		int i = -1;
		bookNote.bookNoteId=cur.getInt(++i);
		bookNote.servicedId=cur.getInt(++i);
		bookNote.bookId=cur.getLong(++i);
		bookNote.orderId=cur.getLong(++i);
		bookNote.userName=cur.getString(++i);
		bookNote.bookFormate=cur.getInt(++i);
		String startAndendBookMark=cur.getString(++i);
		if(!TextUtils.isEmpty(startAndendBookMark)){
			bookNote.jsonToBookMark(startAndendBookMark,bookNote.bookFormate);
		}
		bookNote.startOffset=cur.getInt(++i);
		bookNote.endOffset=cur.getInt(++i);
	    bookNote.content=cur.getString(++i);
 	    bookNote.hasRemark=cur.getInt(++i)==0?false:true;
	    bookNote.remarkContent=cur.getString(++i);
	    bookNote.color=cur.getInt(++i);
	    bookNote.createTime=cur.getLong(++i);
	    bookNote.lastModifyTime=cur.getLong(++i);
	    bookNote.clientTime=cur.getLong(++i);
	    bookNote.modifyState=cur.getInt(++i);
 		return bookNote;
	}

	public void copy(BookNote bookNote){
		this.bookNoteId=bookNote.bookNoteId;
		this.servicedId=bookNote.servicedId;
		this.bookId	=bookNote.bookId;
		this.orderId	=bookNote.orderId;
		this.bookFormate	=bookNote.bookFormate;
		this.userName	=bookNote.userName;
		this.end_char_index	=bookNote.end_char_index;
		this.end_element_index	=bookNote.end_element_index;
		this.end_paragraph_index	=bookNote.end_paragraph_index;
		this.endOffset	=bookNote.endOffset;
		this.hasRemark	=bookNote.hasRemark;
		this.lastModifyTime	=bookNote.lastModifyTime;
		this.modifyState	=bookNote.modifyState;
		this.startOffset	=bookNote.startOffset;
		this.pageNum	=bookNote.pageNum;
		this.remarkContent	=bookNote.remarkContent;
		this.start_paragraph_index	=bookNote.start_paragraph_index;
		this.start_element_index	=bookNote.start_element_index;
		this.start_char_index	=bookNote.start_char_index;
		this.color	=bookNote.color;
		this.createTime	=bookNote.createTime;
		this.content=bookNote.content;
		this.clientTime=bookNote.clientTime;
	}
	public BookNote copy(){
		BookNote bookNote= new BookNote();
		bookNote.bookNoteId=this.bookNoteId;
		bookNote.servicedId=this.servicedId;
		bookNote.bookId=this.bookId;
		bookNote.orderId=this.orderId;
		bookNote.bookFormate=this.bookFormate;
		bookNote.userName=this.userName;
		bookNote.bookFormate=this.bookFormate;
		bookNote.end_char_index=this.end_char_index;
		bookNote.end_element_index=this.end_element_index;
		bookNote.end_paragraph_index=this.end_paragraph_index;
		bookNote.endOffset=this.endOffset;
		bookNote.hasRemark=this.hasRemark;
		bookNote.lastModifyTime=this.lastModifyTime;
		bookNote.modifyState=this.modifyState;
		bookNote.startOffset=this.startOffset;
		bookNote.pageNum=this.pageNum;
		bookNote.remarkContent=this.remarkContent;
		bookNote.start_paragraph_index=this.start_paragraph_index;
		bookNote.start_element_index=this.start_element_index;
		bookNote.start_char_index=this.start_char_index;
		bookNote.color=this.color;
		bookNote.createTime=this.createTime;
		bookNote.content=this.content;
		bookNote.clientTime=this.clientTime;
		return bookNote;
	}
	
	
	public BookNote copyBookMark(){
		BookNote bookNote= new BookNote();
		bookNote.start_paragraph_index=this.start_paragraph_index;
		bookNote.start_element_index=this.start_element_index;
		bookNote.start_char_index=this.start_char_index;
		bookNote.end_char_index=this.end_char_index;
		bookNote.end_element_index=this.end_element_index;
		bookNote.end_paragraph_index=this.end_paragraph_index;
		return bookNote;
	}
	

	public static void registerContentObserver(ContentObserver cob) {
		MZBookApplication.getContext().getContentResolver()
				.registerContentObserver(URI_NOTES, true, cob);
	}

	public static void unregisterContentObserver(ContentObserver cob) {
		MZBookApplication.getContext().getContentResolver()
				.unregisterContentObserver(cob);
	}
}
