package com.jingdong.app.reader.data.db;

import java.util.ArrayList;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.entity.BookCategory;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.localreading.MyBookmark;
import com.jingdong.app.reader.util.Log;


public class DBLocalBookHelper {
	public static final int KEY_SORT_ORDERTYPE_NAME = 1;
	public static final int KEY_SORT_ORDERTYPE_AUTHOR = 2;
	public static final int KEY_SORT_ORDERTYPE_TIME = 3;

	public static final Uri uri = DataProvider.CONTENT_URI_EBOOK;
//	public static final Uri uri_pluins = DataProvider.CONTENT_URI_NAME_PLUGINS;
//	public static final Uri uri_car = DataProvider.CONTENT_URI_BOOKCART;
	public final static int FORM_PAGE_HOME = 0;
	public final static int FORM_PAGE_LOCAL_E = 1;


	public static ArrayList<LocalBook> getLocalBookList(int type,
			int orderType, String category) {
		return getLocalBookList(null, orderType, category,
				DBLocalBookHelper.FORM_PAGE_LOCAL_E, -1, type);
	}

	public static ArrayList<LocalBook> getLocalBookList(String[] formats,
			int orderType, String category) {
		return getLocalBookList(formats, orderType, category,
				DBLocalBookHelper.FORM_PAGE_LOCAL_E, -1, -1);
	}


	
	public static int getMinimumBookId() {
		int minimumBookId = -1;
		String[] projection = new String[] { "min(book_id)" };
		Cursor cur = MZBookApplication.getInstance().getContentResolver()
				.query(uri, projection, null, null, null);
		if (cur == null) {
			return minimumBookId;
		}
		while (!cur.isAfterLast()) {
			if (cur.isBeforeFirst()) {
				cur.moveToNext();
			} else {
				minimumBookId = cur.getInt(0);
				if (minimumBookId > 0)
					minimumBookId = -1;
				// --minimumBookId;
				else
					--minimumBookId;
				// minimumBookId = -1;
				break;
			}
		}
		cur.close();

		return minimumBookId;
	}

	// DBHelper.getLocalBookList(LocalBook.TYPE_EPUB,
	// DBHelper.KEY_SORT_ORDERTYPE_TIME, DBHelper.FORM_PAGE_LOCAL_E);
	public static ArrayList<LocalBook> getHomeLocalBookList() {
		return getLocalBookList(null, DBLocalBookHelper.KEY_SORT_ORDERTYPE_TIME,
				BookCategory.DEFAULT_CATEGORY, FORM_PAGE_HOME, 9,
				LocalBook.TYPE_EBOOK);
	}

	public static ArrayList<LocalBook> getLocalBookListOnlyWithIdAndVerison(
			String user_name) {
		ArrayList<LocalBook> list = new ArrayList<LocalBook>();
		String[] projection = new String[] { "book_id", "version",
				"operating_state", "order_code", "type_id" ,"note_version","note_operating_state","format"};
		String selection = "type_id" + " = " + "'" + LocalBook.TYPE_EBOOK + "'"
				+ " and " + "user_name" + " = " + "'" + user_name + "'"
				+ " and (" + "state_load" + " = " + "'"
				+ LocalBook.STATE_LOADED + "'" + " or " + "state_load" + " = "
				+ "'" + LocalBook.STATE_LOAD_READING + "')";
		Cursor cur = MZBookApplication.getInstance().getContentResolver()
				.query(uri, projection, selection, null, null);// " DESC");
		// sortOrder = null;
		if (cur == null) {
			Log.i("", "cur==null");
			return null;
		}
		// int index = 0;
		while (!cur.isAfterLast()) {
			if (cur.isBeforeFirst()) {
				cur.moveToNext();
			} else {
				LocalBook localBook = new LocalBook();
				int i = -1;
				localBook.book_id = cur.getLong(++i);
				localBook.version = cur.getLong(++i);
				localBook.operatingState = cur.getInt(++i);
				localBook.order_code = cur.getString(++i);
				localBook.type_id = cur.getInt(++i);
				localBook.bookNoteVersion=cur.getLong(++i);
				localBook.operatingNoteState=cur.getInt(++i);
				localBook.format=cur.getInt(++i);
				list.add(localBook);
				cur.moveToNext();
				// index++;
			}
		}
		Log.i("zhoubo", "list.size()" + list.size());
		cur.close();
		return list;
	}

	public static ArrayList<LocalBook> getLocalBookList(String[] formats,
			int orderType, String category, int formPage, int pageSize, int type) {
		// 排序方法 1 ：名称 2：作者3：修改时间
		ArrayList<LocalBook> list = new ArrayList<LocalBook>();
		String sortOrder = null;
		String[] projection = LocalBook.ALL_PROJECTION;
		String selection = null;
		String[] selectionArgs = null;
		Vector<String> selectionArgList = new Vector<String>(0);
		// type_id = '1' AND category=''bmmm'' ORDER BY mod_time DESC
		if (formPage == FORM_PAGE_LOCAL_E) {
			if (formats != null) {
				// selection = "type_id" + " = " + "'" + type + "'";
				// selection = "type_id" + " = ?";
				StringBuffer strs = new StringBuffer();
				strs.append("(");
				// int type = 0;

				for (int i = 0; i < formats.length; i++) {
					if (i > 0) {
						strs.append(" or ");
					}

					strs.append("format" + " = ?");
				}
				strs.append(")");
				selection = strs.toString();
				for (String format : formats) {
					selectionArgList.add(format);
				}
				// selectionArgList.types = ;
			}
		} else if (formPage == FORM_PAGE_HOME) {
			// selection = "type_id" + " = " + "'" + type + "'"
			// + " and (" + "state_load" + " = " + "'" + LocalBook.STATE_LOADED
			// + "'"
			// + " or " + "state_load" + " = " + "'" +
			// LocalBook.STATE_LOAD_READING
			// + "')";
			// selection = "type_id" + " = ?" + " and (" + "state_load" + " =?"
			// + " or " + "state_load" + " = ?)";
			if (formats != null) {
				StringBuffer strs = new StringBuffer();
				// int type = 0;
				strs.append("(");
				for (int i = 0; i < formats.length; i++) {
					if (i > 0) {
						strs.append(" or ");
					}
					strs.append("format" + " = ?");
				}
				strs.append(")");
				strs.append(" and (" + "state_load" + " =?" + " or "
						+ "state_load" + " = ?)");
				selection = strs.toString();
				for (String format : formats) {
					selectionArgList.add(format);
				}
				selectionArgList.add(String.valueOf(LocalBook.STATE_LOADED));
				selectionArgList.add(String
						.valueOf(LocalBook.STATE_LOAD_READING));
				// selectionArgs = new String[] { String.valueOf(type),
				// String.valueOf(LocalBook.STATE_LOADED),
				// String.valueOf(LocalBook.STATE_LOAD_READING) };
			}
		}

		if (!TextUtils.isEmpty(category)) {
			if (category.equals(BookCategory.DEFAULT_CATEGORY)) {
				// 全部
			} else if (category.equals(BookCategory.UNCLASSIFIED_CATEGORY)) {
				// 未分类
				if (!TextUtils.isEmpty(selection)) {
					selection += " AND category=''";
				} else {
					selection = "category=''";
				}

			} else {
				if (!TextUtils.isEmpty(selection)) {
					selection += " AND category=?";
				} else {
					selection = "category=?";
				}
				selectionArgList.add(category);
				// if (selectionArgs.length == 1) {
				// selectionArgs = new String[2];
				// selectionArgs[0] = String.valueOf(type);
				// selectionArgs[1] = category;
				// } else if (selectionArgs.length == 3) {
				// selectionArgs = new String[4];
				// //selectionArgs[0] = String.valueOf(type);
				// selectionArgs[1] = String.valueOf(LocalBook.STATE_LOADED);
				// selectionArgs[2] = String
				// .valueOf(LocalBook.STATE_LOAD_READING);
				// selectionArgs[4] = category;
				// }

			}
		}

		if (type != -1) {
			if (!TextUtils.isEmpty(selection)) {
				selection += " AND type_id=?";
			} else {
				selection = "type_id=?";
			}
			selectionArgList.add("" + type);
		}

		selectionArgs = new String[selectionArgList.size()];
		int i = 0;
		for (String selectionArg : selectionArgList) {
			selectionArgs[i] = selectionArg;
			i++;
		}
		Cursor cur;
		switch (orderType) {
		case 1:
			sortOrder = "title";
			break;
		case 2:
			sortOrder = "author";
			break;
		case 3:
			sortOrder = "mod_time DESC";
			break;
		// default:
		// sortOrder = "mod_time" + " ASC";
		// break;
		}
		cur = MZBookApplication.getInstance().getContentResolver()
				.query(uri, projection, selection, selectionArgs, sortOrder);// " DESC");
		sortOrder = null;
		if (cur == null) {
			Log.i("", "cur==null");
			return null;
		}
		int index = 0;
		while (!cur.isAfterLast()) {
			if (cur.isBeforeFirst()) {
				cur.moveToNext();
			} else {
				LocalBook localBook = LocalBook.getLocalBook(cur);
				// if(localBook!=null&&localBook.state
				// ==LocalBook.STATE_LOADING){
				// localBook.state = LocalBook.STATE_LOAD_PAUSED;
				// }
				list.add(localBook);
				cur.moveToNext();
				index++;
				if (pageSize != -1 && index >= pageSize) {
					break;
				}
			}
		}
		Log.i("zhoubo", "list.size()" + list.size());
		cur.close();
		return list;
	}

	public static int delEbook(String selection, String[] selectionArgs) {
		int flag;
		flag = MZBookApplication.getInstance().getContentResolver()
				.delete(uri, selection, selectionArgs);
		MZBookApplication.getInstance().getContentResolver()
				.notifyChange(uri, null);
		return flag;
	}

	public static boolean delEbookById(String[] bookid) {
		int flag;
		flag = delEbook("book_id=?", bookid);
		if (flag == 1) {
			return true;// 全部删除则返回0

		} else {
			return false;// 返回删除失败的数量
		}
	}



	

	public static Cursor getLocalBookByName(String book_name) {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		String[] projection = LocalBook.ALL_PROJECTION;
		String[] selectionArgs = new String[] { "%%" + book_name + "%%" };
		Cursor cur;
		String selection = "title like ? ";
		String sortOrder = null;
		cur = mContentResolver.query(uri, projection, selection, selectionArgs,
				sortOrder);// " DESC");
		return cur;
	}

	/*
	 * 在某个分类中的查询图书。
	 */
	public static Cursor getLocalBookByNameInCategory(String book_name) {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		String[] projection = LocalBook.ALL_PROJECTION;
		Cursor cur;
		String selection = "";
		String[] selectionArgs = new String[] { "" };
		if (BookCategory.getActiveCategory().equals(
				BookCategory.DEFAULT_CATEGORY)) {
			selection = "title like ? ";
			selectionArgs = new String[] { "%%" + book_name + "%%" };
		} else {
			selectionArgs = new String[] { "%%" + book_name + "%%",
					BookCategory.getActiveCategory() };
			selection = "title like ? AND category=? ";
		}
		String sortOrder = null;
		cur = mContentResolver.query(uri, projection, selection, selectionArgs,
				sortOrder);// " DESC");
		return cur;
	}



	public static boolean isExistIntDB(long bookId) {
		boolean isExist = false;
		String[] projection = null;
		String selection = "book_id=?";
		;
		Cursor cur;
		String sortOrder = null;
		cur = MZBookApplication
				.getInstance()
				.getContentResolver()
				.query(uri, projection, selection,
						new String[] { bookId + "" }, sortOrder);// " DESC");
		if (cur == null) {
			Log.i("", "cur==null");
			isExist = false;
		}
		Log.i("zhoubo", "cur.getCount()" + cur.getCount());
		if (cur.getCount() > 0) {
			isExist = true;
		}
		cur.close();
		return isExist;
	}



	public static void registerContentObserver(ContentObserver cob) {
		MZBookApplication.getContext().getContentResolver() 
				.registerContentObserver(uri, true, cob);
	}

	public static void unregisterContentObserver(ContentObserver cob) {
		MZBookApplication.getContext().getContentResolver()
				.unregisterContentObserver(cob);
	}
	
	public static int getBookinforCount(){
		String category="";
		Cursor cur;
		int count=0;
		String mActiviyCategory=BookCategory.getActiveCategory();
		if(mActiviyCategory.equals(BookCategory.DEFAULT_CATEGORY)){
			 cur = MZBookApplication.getInstance().getContentResolver() 
						.query(uri, new String[]{"count(*)"}, null, null, null);
		}else{
			if(!mActiviyCategory.equals(BookCategory.UNCLASSIFIED_CATEGORY))
				category=mActiviyCategory;
			 cur = MZBookApplication.getInstance().getContentResolver()
						.query(uri, new String[]{"count(*)"}, "category=?", new String[]{category}, null);
		}
		if(cur==null) return count;
		while (!cur.isAfterLast()) {
			if (cur.isBeforeFirst()) {
				cur.moveToNext();
			} else {
				count=cur.getInt(0);
				cur.moveToNext();
			}
		}
		cur.close();
		return count;
	}


	

	

	public static JSONObject localBook2Json(LocalBook localBook) {
		// 解析LocalBook实体和LocalBook的书签为json对象
		JSONObject bookMarkBuildObj = new JSONObject();
		try {
			long orderid = 0;
			if (TextUtils.isEmpty(localBook.order_code)) {
				orderid = 0;
			} else {
				try {
					orderid = Long.parseLong(localBook.order_code);
				} catch (Exception e) {
				}
			}
			bookMarkBuildObj.put("orderId", orderid);
			bookMarkBuildObj.put("ebookId", localBook.book_id);
			bookMarkBuildObj.put("version", localBook.version);
			bookMarkBuildObj.put(MyBookmark.KEY_TOTAL_OFFSET,
					localBook.totalOffset);
			bookMarkBuildObj.put(MyBookmark.KEY_EBOOK_TYPE, localBook.type_id);
			ArrayList<MyBookmark> bookmarkList = localBook.bookMarks;
			JSONArray list = new JSONArray();
			for (MyBookmark myBookmark : bookmarkList) {
				JSONObject bookMarkObject = new JSONObject();
				bookMarkObject.put(MyBookmark.KEY_CLIENT_ID,
						myBookmark.getClientId());
				bookMarkObject.put(MyBookmark.KEY_ID, myBookmark.getServerId());
				bookMarkObject.put(MyBookmark.KEY_DATA_TYPE,
						myBookmark.getDataType());
				bookMarkObject.put(MyBookmark.KEY_OFFSET,
						myBookmark.getOffset());
				bookMarkObject.put(MyBookmark.KEY_X1,
						myBookmark.getParagraph_index());
				bookMarkObject.put(MyBookmark.KEY_Y1,
						myBookmark.getElement_index());
				bookMarkObject.put(MyBookmark.KEY_Z1,
						myBookmark.getChar_index());
				bookMarkObject.put(MyBookmark.KEY_NOTE, myBookmark.getName());
				bookMarkObject.put(MyBookmark.KEY_DEVICE_TIME,
						myBookmark.getDeviceTime());
				int valid = 1;
				if (myBookmark.getOperatingState() == MyBookmark.STATE_DEL) {
					valid = 0;
				} else {
					valid = 1;
				}
				bookMarkObject.put(MyBookmark.KEY_VALID, valid);
				list.put(bookMarkObject);
			}
			bookMarkBuildObj.put(DataParser.KEY_LIST, list);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bookMarkBuildObj;

	}

}
