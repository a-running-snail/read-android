package com.jingdong.app.reader.localreading;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.data.db.DataProvider;

/**
 * @author wcx
 * @version 2011-12-2 上午11:35:02
 * 
 */
public class MyBookmark implements Serializable{
	private static final long serialVersionUID = 341289161365642392L;

	public final static Uri URI = DataProvider.CONTENT_URI_NAME_BOOKMARK;

	public final static int STATE_NO_MODIFY = 0;//Modify
	public final static int STATE_MODIFY = 1;//Modify
	public final static int STATE_ADD = 2;//Modify
	public final static int STATE_DEL = 3;//Modify
	public final static int STATE_SERVER_NORMAL = 1;//Modify
	public final static int STATE_SERVER_DEL = 0;//Modify
	
	public static final int DATATYPE_POS = 1;
	public static final int DATATYPE_LAST_POS = 0;
	
	public static final int  BOOKMARK_TYPE_EPUB = 1;
	public static final int  BOOKMARK_TYPE_PDF = 0;

	// private Long id = null;
	// private int type = -1;
	// private long book_id;
	// private long creation_time;
	// private int modification_time;
	// private int access_time;
	// private int access_counter;
	// private int paragraph_index = -1;
	// private int element_index = -1;
	// private int char_index = -1;
	// private String bookmark_title;
	// private int bookmark_from = -1;
	// private int bookmark_total = -1;
	// private int bookmark_to = -1;

	private long clientId;// 客户端书签id
	private long serverId;// 服务器书签id

	private String user;
	private long bookId;
	private long orderId;
	private int dataType;// 手动书签，系统书签
	private int bookMarkType;// 书的类型

	//	double offSet;
	private int paragraphIndex;
	private int elementIndex;
	private int charIndex;
	private String name;
	private long deviceTime;// 客户端修改时间
	private long creationTime;// 创建时间
	//long modificationTime;// 修改时间
	private int operatingState;


	private long offset;
	private long offsetTotal;
	public static String KEY_DB_OEPRATING_STATE = "operating_state";
	public static String KEY_DB_SERVERID= "server_id";
	public static String KEY_DB_DEVICETIME = "device_time";
	
//	String version;
	public void setServerId(long serverId) {
		this.serverId = serverId;
	}
	
	   public static MyBookmark loadSettingFromFile(byte[] bytes)
	    {
		    ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
	        ObjectInputStream in;
			try {
				in = new ObjectInputStream(bin);
			    Object ret = in.readObject();
		        in.close();
		        return (MyBookmark)ret;
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
	    }
	    
	    public byte[] getBytes()
	    {
	    	 try  {
	    	        // save the object to a byte array
	    	        ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    	        ObjectOutputStream out = new ObjectOutputStream(bout);
	    	        out.writeObject(this);
	    	        byte[] bytes = bout.toByteArray();
	    	        out.close();
	    			return bytes;
	    	    }
	    	    catch(Exception e)  {
	    	        return null;
	    	    }
	    }

	
	
	public MyBookmark() {
		super();
	}
	
	public long getServerId() {
		return serverId;
	}

	public int getBookType() {
		return bookMarkType;
	}

	public void setBookMarkType(int bookType) {
		this.bookMarkType = bookType;
	}

	
	public static MyBookmark getBookmarkFromCursor(Cursor cursor) {
		int i = -1;
		MyBookmark bookmark = null;
		try {
			if (!cursor.isAfterLast()&&!cursor.isBeforeFirst()) {
				bookmark = new MyBookmark();
				bookmark.clientId = cursor.getLong(++i);
				bookmark.serverId = cursor.getLong(++i);
				bookmark.user = cursor.getString(++i);
				bookmark.bookId = cursor.getLong(++i);
				bookmark.orderId = cursor.getLong(++i);
				bookmark.dataType = cursor.getInt(++i);
				bookmark.bookMarkType = cursor.getInt(++i);
//				bookmark.offSet = cursor.getLong(++i);
				bookmark.paragraphIndex = cursor.getInt(++i);
				bookmark.elementIndex = cursor.getInt(++i);
				bookmark.charIndex = cursor.getInt(++i);
				bookmark.name = cursor.getString(++i);
				bookmark.deviceTime = cursor.getLong(++i);// 客户端修改时间
				bookmark.creationTime = cursor.getLong(++i);// 创建时间
				//bookmark.modificationTime = cursor.getLong(++i);// 修改时间
				bookmark.operatingState = cursor.getInt(++i);
				bookmark.offset = cursor.getLong(++i);
				bookmark.offsetTotal = cursor.getLong(++i);
			}
		} catch (Exception e) {

		}
		return bookmark;
	}
	
	public long getDeviceTime() {
		return deviceTime;
	}

	public void setDeviceTime(long deviceTime) {
		this.deviceTime = deviceTime;
	}

	public int getOperatingState() {
		return operatingState;
	}

	public void setOperatingState(int operatingState) {
		this.operatingState = operatingState;
	}
	
	// @Override
	public static boolean saveBookmark(boolean autoUpdateOperatingState,ContentValues values,long serverId) {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
//	 	ContentValues values = toValues();
		String selection = "server_id=" + serverId;
		if(autoUpdateOperatingState){
		if(values.containsKey(KEY_DB_OEPRATING_STATE)){
			values.remove(KEY_DB_OEPRATING_STATE);
		}
		values.put(KEY_DB_OEPRATING_STATE, STATE_MODIFY);
			}
		Uri uri = null;
		
		int num = (mContentResolver.update(URI, values, selection, null));
		if (num == 0) {
		 	values.remove("client_id");
			if(autoUpdateOperatingState){
				if(values.containsKey(KEY_DB_OEPRATING_STATE)){
					values.remove(KEY_DB_OEPRATING_STATE);
				}
				values.put(KEY_DB_OEPRATING_STATE, STATE_ADD);
					}
			uri = mContentResolver.insert(URI, values);
		}
		boolean isSuccess = false;
		if (uri != null||num!=0) {
			isSuccess = true;
		} else {
			isSuccess = false;
		}
		MZBookApplication.getContext().getContentResolver()
		.notifyChange(URI, null);
		return isSuccess;
	}
	
	
	
	// 保存的时候以那个Id为唯一标识  true为ClientId， false为serverId
	public boolean saveBookmark(boolean autoUpdateOperatingState,boolean isClientId) {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		if(autoUpdateOperatingState){
		this.operatingState = STATE_MODIFY;
		}
	 	ContentValues values = toValues();
//		values.put("client_id", clientId);
////		values.put("id", id);
//		values.put("user", user);
//		values.put("book_id", bookId);
//		values.put("order_id", orderId);
//		values.put("data_type", dataType);
//		values.put("book_type", bookType);
//		values.put("paragraph_index", paragraphIndex);
//		values.put("element_index", elementIndex);
//		values.put("char_index", charIndex);
//		values.put("name", name);
//		values.put("device_time", deviceTime);
//		values.put("creation_time", creationTime);
//		//values.put("modification_time", modificationTime);
//		values.put("state", state);
//		values.put("offset", offset);
//		values.put("offset_total", offsetTotal);
		String selection = "";
       if(isClientId){
   		selection = "book_id=" + bookId + " AND client_id=" + clientId
		+ " AND data_type=" + dataType;
      }else{
  		selection = "book_id=" + bookId + " AND server_id=" + serverId
		+ " AND data_type=" + dataType;  
      }
		Uri uri = null;
		
		int num = (mContentResolver.update(URI, values, selection, null));
		if (num == 0) {
			if(autoUpdateOperatingState){
			this.operatingState = STATE_ADD;
			}
		 	values = toValues();
		 	values.remove("client_id");
			uri = mContentResolver.insert(URI, values);
		}
		boolean isSuccess = false;
		if (uri != null||num!=0) {
			isSuccess = true;
		} else {
			isSuccess = false;
		}
		MZBookApplication.getContext().getContentResolver()
		.notifyChange(URI, null);
		return isSuccess;
	}
	
	public ContentValues toValues(){
		ContentValues values = new ContentValues();
//		values.put("client_id", clientId);
    	values.put("server_id", serverId);
		values.put("user", user);
		values.put("book_id", bookId);
		values.put("order_id", orderId);
		values.put("data_type", dataType);
		values.put("book_type", bookMarkType);
		values.put("paragraph_index", paragraphIndex);
		values.put("element_index", elementIndex);
		values.put("char_index", charIndex);
		values.put("name", name);
		values.put("device_time", deviceTime);
		values.put("creation_time", creationTime);
		//values.put("modification_time", modificationTime);
		values.put("operating_state", operatingState);
		values.put("offset", offset);
		values.put("offset_total", offsetTotal);
		return values;
	}
	/*

	public MyBookmark(PageData pageDataRight, int type) {
		super();
		this.dataType = type;

		this.creationTime = System.currentTimeMillis();

		this.paragraphIndex = pageDataRight.bookMark.paragraph_index;
		this.elementIndex = pageDataRight.bookMark.element_index;
		this.charIndex = pageDataRight.bookMark.char_index;
        try {
            if (pageDataRight.bookMark != null && pageDataRight.bookMark.title != null) {
            	 
                this.name = pageDataRight.bookMark.title;//.substring(0,
                       // Math.min(50, pageDataRight.bookMark.title.length()))+"...";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

		// this.bookmarkFrom = pageDataRight.readProgress.from;
		// this.bookmarkTotal = pageDataRight.readProgress.total;
		this.offset = pageDataRight.readProgress.from;
		this.offsetTotal = pageDataRight.readProgress.total;
	}

	public void setPageData(PageData pageDataRight, int type) {
		this.dataType = type;

		this.creationTime = System.currentTimeMillis();

		this.paragraphIndex = pageDataRight.bookMark.paragraph_index;
		this.elementIndex = pageDataRight.bookMark.element_index;
		this.charIndex = pageDataRight.bookMark.char_index;
		try {
    		if (pageDataRight.bookMark != null && pageDataRight.bookMark.title != null) {
        		this.name = pageDataRight.bookMark.title.substring(0,
        				Math.min(50, pageDataRight.bookMark.title.length()));
    		}
		} catch (Exception e) {
		    e.printStackTrace();
		}

		// this.bookmarkFrom = pageDataRight.readProgress.from;
		// this.bookmarkTotal = pageDataRight.readProgress.total;
		this.offset = pageDataRight.readProgress.from;
		this.offsetTotal = pageDataRight.readProgress.total;
	}*/

	public float getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}
	
	public long getOffsetTotal() {
		return offsetTotal;
	}

	public void setOffsetTotal(long offsetTotal) {
		this.offsetTotal = offsetTotal;
	}
	
	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int type) {
		this.dataType = type;
	}

	public long getBook_id() {
		return bookId;
	}

	public void setBook_id(long bookId) {
		this.bookId = bookId;
	}

	public long getCreation_time() {
		return creationTime;
	}

	public void setCreation_time(long creationTime) {
		this.creationTime = creationTime;
	}

//	public long getModification_time() {
//		return modificationTime;
//	}
//
//	public void setModification_time(long modification_time) {
//		this.modificationTime = modificationTime;
//	}

	// public int getAccess_time() {
	// return access_time;
	// }
	//
	// public void setAccess_time(long access_time) {
	// this.access_time = access_time;
	// }
	//
	// public int getAccess_counter() {
	// return access_counter;
	// }
	//
	// public void setAccess_counter(int access_counter) {
	// this.access_counter = access_counter;
	// }

	public int getParagraph_index() {
		return paragraphIndex;
	}

	public void setParagraph_index(int paragraph_index) {
		this.paragraphIndex = paragraph_index;
	}

	public int getElement_index() {
		return elementIndex;
	}

	public void setElement_index(int elementIndex) {
		this.elementIndex = elementIndex;
	}

	public int getChar_index() {
		return charIndex;
	}

	public void setChar_index(int char_index) {
		this.charIndex = char_index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
    public boolean equals(Object o) {
        boolean bEqual = super.equals(o);
        
        if (!bEqual && o instanceof MyBookmark) {
            MyBookmark bookmark = (MyBookmark) o;

            bEqual = (this.charIndex == bookmark.charIndex
                    && this.elementIndex == bookmark.elementIndex
                    && this.paragraphIndex == bookmark.paragraphIndex && this.offset == bookmark.offset) ? true
                    : false;
        }
        
        return bEqual;
    }



    public static final String KEY_CLIENT_ID = "clientId";//:***,
	public static final String KEY_ID = "id";//:***,			服务器书签表主键
	public static final String KEY_DATA_TYPE = "dataType";//:***,	书签类型 0-手动 1-系统 2笔记
	public static final String KEY_EBOOK_TYPE = "ebookType";//:***,电子书类型 0-pdf 1-epub
	public static final String KEY_OFFSET = "offset";//:***,		偏移量 pdf页数 epub进度
	public static final String KEY_TOTAL_OFFSET = "totalOffset";//:***,		偏移量 pdf页数 epub进度
	public static final String KEY_X1="x1";//:***,			起始段
	public static final String KEY_Y1="y1";//:***,			起始词
	public static final String KEY_Z1= "z1";//:***,			起始字
	public static final String KEY_NOTE = "note";//:***,			书签内容字符串
	public static final String KEY_DEVICE_TIME = "deviceTime";//:***,	创建时间
	public static final String KEY_VALID = "valid";//:***,		状态 0删除 1正常  
	public static final MyBookmark parse(JSONObject jsonObject,long bookId,long orderId,long verison) {
		MyBookmark myBookmark = null;
		if (jsonObject != null) {
			try {
				myBookmark = new MyBookmark();
//				myBookmark.clientId =  DataParser.getLong(jsonObject, KEY_CLIENT_ID);
				myBookmark.serverId =  DataParser.getInt(jsonObject, KEY_ID);
				myBookmark.bookId =  bookId;
				myBookmark.dataType =  DataParser.getInt(jsonObject, KEY_DATA_TYPE);
				myBookmark.paragraphIndex =  DataParser.getInt(jsonObject, KEY_X1);
				myBookmark.elementIndex =  DataParser.getInt(jsonObject, KEY_Y1);
				myBookmark.charIndex = DataParser.getInt(jsonObject, KEY_Z1);
				myBookmark.name = DataParser.getString(jsonObject, KEY_NOTE);
				myBookmark.deviceTime = DataParser.getLong(jsonObject, KEY_DEVICE_TIME);// 客户端修改时间
			//	myBookmark.creationTime = ;// 创建时间
				//long modificationTime;// 修改时间
				myBookmark.operatingState =  DataParser.getInt(jsonObject, KEY_VALID);
				myBookmark.offset = DataParser.getLong(jsonObject, KEY_OFFSET)-1;//本地书签需要-1才行，因为服务端书签起始点是从1开始的（pc和iphone提交的数据是这样），android端是从0开始的。
				myBookmark.offsetTotal = DataParser.getLong(jsonObject, KEY_TOTAL_OFFSET); ;
				return myBookmark;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
