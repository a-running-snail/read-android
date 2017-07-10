package com.jingdong.app.reader.data.db;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.localreading.MyBookmark;

public class DBBookMarkHelper {

	public final static Uri URI = DataProvider.CONTENT_URI_NAME_BOOKMARK;

	/**
	 * @author zhoubo
	 * @return boolean
	 * @param long bookId
	 * @param int dataTyp 手动书签还是系统书签
	 * @param int bookMarkType BOOKMARK_TYPE_EPUB BOOKMARK_TYPE_PDF
	 * @param String
	 *            name
	 * @long offset
	 * @long offsetTotal
	 * @see 接受bookid和当前阅读位置。页码，判断当前页是否加入书签
	 * **/
	public static boolean saveLastBookmark(long bookId, int dataType,
			int bookMarkType, String name, long offset, long offsetTotal) {
		String condition = "book_id=" + bookId + " AND data_type="
				+ MyBookmark.DATATYPE_LAST_POS;
		MyBookmark lastBookmark = getBookmark(condition);
		if (lastBookmark == null) {
			lastBookmark = new MyBookmark();
			lastBookmark.setBook_id(bookId);

		}
		lastBookmark.setOperatingState(MyBookmark.STATE_MODIFY);
		lastBookmark.setDataType(dataType);
		lastBookmark.setBookMarkType(bookMarkType);
		lastBookmark.setName(name);
		lastBookmark.setOffset(offset);
		lastBookmark.setOffsetTotal(offsetTotal);
		saveBookmark(lastBookmark, bookId, true);
		return false;
	}

	// @Override
	public static synchronized void deleteBookmarks(long bookId, long serverId) {
		String condition = "server_id=" + serverId + " AND " + "book_id" + "="
				+ bookId;
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		// int num = mContentResolver.delete(URI, condition, null);
		mContentResolver.delete(URI, condition, null);
	}

	// @Override
	public static synchronized void deleteBookMarks(long bookId,
			int operatingState) {
		String condition = "operating_state=" + operatingState + " AND "
				+ "book_id" + "=" + bookId;
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		// int num = mContentResolver.delete(URI, condition, null);
		mContentResolver.delete(URI, condition, null);
	}

	public synchronized static void deleteBookmarks(long book_id) {
		String condition = "book_id=" + book_id;
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		mContentResolver.delete(URI, condition, null);
	}

	public static boolean saveBookmark(MyBookmark lastBookmark, long bookId,
			boolean isMedifyTime) {
		lastBookmark.setBook_id(bookId);
		LocalBook.saveOperatingState(MyBookmark.STATE_MODIFY, bookId);
		if (isMedifyTime) {
			lastBookmark.setDeviceTime(System.currentTimeMillis() / 1000);
		}
		return lastBookmark.saveBookmark(true, true);
	}

	public static long addBookmark(MyBookmark bookmark) {
		// TODO Auto-generated method stub
		// myBookMarks.add(bookmark);
		saveBookmark(bookmark, bookmark.getBook_id(), true);
		return 1;
	}

	private static Cursor rawQuery(String condition, String[] selectionArgs) {
		ContentResolver mContentResolver =  MZBookApplication.getContext().getContentResolver();
		Cursor cur = mContentResolver.query(URI, null, condition, null, null);// " DESC");
		return cur;
	}

	public static synchronized boolean load(ArrayList<MyBookmark> list,
			String clause) {
		boolean found = false;
		Cursor rs = null;
		try {
			// clause = " WHERE " + clause;
			rs = rawQuery(clause, null);
			if (rs.moveToFirst()) {
				do {
					MyBookmark v = MyBookmark.getBookmarkFromCursor(rs);
					list.add(v);
					found = true;
				} while (rs.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
		}
		return found;
	}

	// @Override
	public static synchronized void markDeledBookmark(MyBookmark bookmark) {
		if (bookmark.getServerId() == 0) {
			deleteBookmark(bookmark.getClientId());
		} else {
			LocalBook.saveOperatingState(MyBookmark.STATE_MODIFY,
					bookmark.getBook_id());
			bookmark.setOperatingState(MyBookmark.STATE_DEL);
			bookmark.setDeviceTime(System.currentTimeMillis() / 1000);
			bookmark.saveBookmark(false, true);
		}
	}

	// @Override
	public static synchronized void deleteBookmark(long clientId) {
		String condition = "client_id=" + clientId;
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		// int num = mContentResolver.delete(URI, condition, null);
		mContentResolver.delete(URI, condition, null);
		MZBookApplication.getContext().getContentResolver().notifyChange(URI, null);
	}

	public synchronized static void markDeledBookmark(long book_id,
			int bookmark_from, int bookmark_to) {
		String condition = "book_id=" + book_id + " AND offset>="
				+ bookmark_from + " AND offset<" + bookmark_to
				+ " AND data_type=" + MyBookmark.DATATYPE_POS;
		// condition = " WHERE " + condition;
		// ContentResolver mContentResolver =
		// MZBookApplication.getInstance().getContentResolver();
		ArrayList<MyBookmark> list = new ArrayList<MyBookmark>();
		load(list, condition);
		LocalBook.saveOperatingState(MyBookmark.STATE_MODIFY, book_id);
		for (MyBookmark myBookmark : list) {
			if (myBookmark.getServerId() == 0) {
				deleteBookmark(myBookmark.getClientId());
			} else {
				myBookmark.setOperatingState(MyBookmark.STATE_DEL);
				myBookmark.setDeviceTime(System.currentTimeMillis() / 1000);
				myBookmark.saveBookmark(false, true);
			}
		}
	}

	/****
	 * @author ThinkinBunny yfxiawei@360buy.com
	 * @since 2013-1-5
	 * @param book_id
	 *            图书ＩＤ　必填
	 * @param paneNum
	 *            页码，必填
	 * 
	 * *****/
	public static synchronized void markDeledPdfBookmark(long book_id,
			int pageNum) {
		String condition = "book_id=" + book_id + " AND offset=" + pageNum
				+ " AND data_type=" + MyBookmark.DATATYPE_POS;
		ArrayList<MyBookmark> list = new ArrayList<MyBookmark>();
		load(list, condition);
		LocalBook.saveOperatingState(MyBookmark.STATE_MODIFY, book_id);
		for (MyBookmark myBookmark : list) {
			if (myBookmark.getServerId() == 0) {
				deleteBookmark(myBookmark.getClientId());
			} else {
				myBookmark.setOperatingState(MyBookmark.STATE_DEL);
				myBookmark.setDeviceTime(System.currentTimeMillis() / 1000);
				myBookmark.saveBookmark(false, true);
			}
		}
	}

	public static void registerContentObserver(ContentObserver cob) {
		MZBookApplication.getContext().getContentResolver()
				.registerContentObserver(URI, true, cob);
	}

	public static void unregisterContentObserver(ContentObserver cob) {
		MZBookApplication.getContext().getContentResolver()
				.unregisterContentObserver(cob);
	}

	/**
	 * @author zhoubo
	 * @return 如果存在书签返回true 不存在返回false
	 * @param long bookId, int page
	 * @see 接受bookid和当前阅读位置。页码，判断当前页是否加入书签
	 * **/
	public static boolean isExist(long bookId, int page) {

		String condition = "book_id=" + bookId + " AND data_type="
				+ MyBookmark.DATATYPE_POS + " AND offset=" + page;
		MyBookmark lastBookmark = getBookmark(condition);
		if (lastBookmark != null) {
			return true;
		} else {
			return false;
		}
	}

	public static synchronized MyBookmark getBookmark(String clause) {
		// boolean found = false;
		Cursor cur = null;
		MyBookmark myBookmark = null;
		try {
			// clause = " WHERE " + clause;
			// ContentResolver mContentResolver =
			// MZBookApplication.getInstance().getContentResolver();
			cur = rawQuery(clause, null);// " DESC");
			if (cur.moveToFirst()) {
				myBookmark = MyBookmark.getBookmarkFromCursor(cur);
				// if(myBookmark!=null){
				// found = true;
				// }
			}
		} finally {
			if (cur != null)
				cur.close();
		}
		return myBookmark;
	}

	public static MyBookmark getLastBookmark(long book_id) {
		String condition = "book_id=" + book_id + " AND data_type="
				+ MyBookmark.DATATYPE_LAST_POS;
		MyBookmark lastBookmark = getBookmark(condition);
		return lastBookmark;
	}

	// @Override
	public synchronized static boolean find(long book_id, int bookmark_from,
			int bookmark_to, boolean isCotainDel) {
		String condition = null;
		if (isCotainDel) {
			condition = "book_id=" + book_id + " AND offset>=" + bookmark_from
					+ " AND offset<" + bookmark_to + " AND offset<"
					+ bookmark_to + " AND data_type=" + MyBookmark.DATATYPE_POS;
		} else {
			condition = "book_id=" + book_id + " AND offset>=" + bookmark_from
					+ " AND offset<" + bookmark_to + " AND offset<"
					+ bookmark_to + " AND data_type=" + MyBookmark.DATATYPE_POS
					+ " AND operating_state <>" + MyBookmark.STATE_DEL;
		}
		boolean found = false;
		Cursor rs = null;
		try {
			// condition = " WHERE " + condition;
			rs = rawQuery(condition, null);
			if (rs.moveToFirst()) {
				// MyBookmark v = MyBookmark.getBookmarkFromCursor(rs);
				found = true;
			}
		} finally {
			if (rs != null)
				rs.close();
		}
		return found;
	}

}
