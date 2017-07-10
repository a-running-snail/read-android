package com.jingdong.app.reader.data.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.jingdong.app.reader.data.db.DataProvider.DatabaseHelper;
import com.jingdong.app.reader.entity.BookCategory;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.localreading.MyBookmark;
import com.jingdong.app.reader.util.DatabaseUtil;
import com.jingdong.app.reader.util.Log;

public class DBUpdateManager {

	public static boolean updateVerify(SQLiteDatabase db, int oldVersion,
			int newVersion, final String createSql) {
		return DatabaseUtil.updateVerify(db, oldVersion, newVersion, createSql);
	}

	public static void updateTableName(SQLiteDatabase db, int oldVersion,
			int newVersion, String oldTableName, String newTableName) {
		if (db != null && db.isOpen() && (newVersion > oldVersion)
				&& (oldVersion <= DataProvider.VERSION_OLD_BOOKMARK_1)) {
			if (tabbleIsExist(db, oldTableName)
					&& !tabbleIsExist(db, newTableName)) {
				String updateTableNameSql = "ALTER TABLE " + oldTableName
						+ " RENAME TO " + newTableName + " ";
				db.execSQL(updateTableNameSql);
			}
		}
	}

	/**
	 * 更新书签表是
	 * 
	 * @param tabName
	 *            表名
	 * @return
	 */

	public static void updateBookMarkTable(SQLiteDatabase db, int oldVersion,
			int newVersion, String tableName, String createSql) {
		if (!tabbleIsExist(db, tableName)) {
			return;
		}
		if ((oldVersion <= DataProvider.VERSION_OLD_BOOKMARK_2)
				&& (newVersion > oldVersion)) {
			ArrayList<MyBookmark> list = getOldBookMarks(db, tableName);
			String delTableSql = "DROP TABLE IF EXISTS " + tableName;
			db.execSQL(delTableSql);
			db.execSQL(createSql);
			for (MyBookmark myBookmark : list) {
				saveBookmark(db, tableName, myBookmark);
			}
		}
	}

	/******
	 * 插件数据库表更新
	 * 
	 * @author yfxiawei 2013年2月27日11:13:11
	 * 
	 * ************/
	public static void updatePluginsTable(SQLiteDatabase db, int oldVersion,
			int newVersion, String tableName, String createSql) {
		if (!tabbleIsExist(db, tableName)) {
			db.execSQL(createSql);
			return;
		}

	}

	/**
	 * 更新书签表是
	 * 
	 * @param tabName
	 *            表名
	 * @return
	 */

	public static void updateBookCarTable(SQLiteDatabase db, int oldVersion,
			int newVersion, String tableName, String createSql) {
		if (!tabbleIsExist(db, tableName)) {
			return;
		}
		if ((oldVersion <= DataProvider.VERSION_OLD_CAR_1)
				&& (newVersion > oldVersion)) {
			String delTableSql = "DROP TABLE IF EXISTS " + tableName;
			db.execSQL(delTableSql);
			db.execSQL(createSql);
		}
	}

	public static void updateCategoryTable(SQLiteDatabase db, String tableName) {
		if (!tabbleIsExist(db, tableName)) {
			return;
		}
		updateCategory(db, tableName);

	}

	private static void updateCategory(SQLiteDatabase db, String tableName) {

		ContentValues values1 = new ContentValues();
		values1.put("category", BookCategory.DEFAULT_CATEGORY);
		values1.put("isActive", 1);
		if (db.update(tableName, values1, "category=?",
				new String[] { BookCategory.DEFAULT_CATEGORY }) == 0) {
			db.insert(tableName, "Content is empty", values1);
		}
		ContentValues values = new ContentValues();
		values.put("category", BookCategory.ONLINEREAD_CATEGORY);
		values.put("isActive", 0);
		if (db.update(tableName, values, "category=?",
				new String[] { BookCategory.ONLINEREAD_CATEGORY }) == 0) {
			db.insert(tableName, "Content is empty", values);
		}
		ContentValues values2 = new ContentValues();
		values2.put("category", BookCategory.UNCLASSIFIED_CATEGORY);
		values2.put("isActive", 0);
		if (db.update(tableName, values2, "category=?",
				new String[] { BookCategory.UNCLASSIFIED_CATEGORY }) == 0) {
			db.insert(tableName, "Content is empty", values2);
		}
	}

	/**
	 * 更新图书信息表，将数据库版本23之前的书中没有formate值，将之前的书一律format设置为2即epub类型。@time:09/19.
	 * zhangmurui
	 * 
	 * @param db
	 * @param tableName
	 */
	public static void updateBookInfoTable(SQLiteDatabase db, int oldVersion,
			int newVersion, String tableName) {
		if (!tabbleIsExist(db, tableName)) {
			return;
		}
		if ((oldVersion <= DataProvider.VERSION_OLD_BOOKINFO)
				&& (newVersion > oldVersion)) {
			updateBookFormat(db, tableName);
		}

	}
	
	

	private static void updateBookFormat(SQLiteDatabase db, String tableName) {

		ContentValues values1 = new ContentValues();
		values1.put("format", LocalBook.FORMAT_EPUB);
		values1.put("format_name", LocalBook.FORMATNAME_EPUB);
		if (db.update(tableName, values1, null, null) == 0) {
			db.insert(tableName, "Content is empty", values1);
		}

	}

	public static void updateCategoryTable(SQLiteDatabase db, int oldVersion,
			int newVersion, String categoryTable, String booktable) {
		if (!tabbleIsExist(db, categoryTable) || !tabbleIsExist(db, booktable)) {
			return;
		}
		if ((oldVersion <= DataProvider.VERSION_OLD_CATEGORY)
				&& (newVersion > oldVersion)) {
			updateCategory(db, categoryTable, booktable);
		}
	}

	private static void updateCategorySort(SQLiteDatabase db,
			String categoryTable) {
		Cursor cursor = db.query(categoryTable, new String[] { "category" },
				null, null, null, null, null);
		if (cursor == null) {
			Log.i("", "cur==null");
			return;
		}
		ArrayList<String> list = new ArrayList<String>();
		while (cursor.moveToNext()) {
			String name = cursor.getString(0);
			list.add(name);
		}
		cursor.close();
		cursor = null;
		db.delete(categoryTable, "category!=?", new String[] { "" });// 将表清空，
		ContentValues values1 = new ContentValues();
		values1.put("count", 0);// 为了保证全部--我的畅读---未分类--三者的显示的前后顺序。所以先对全部--在线畅读分类进行插入一次。
		values1.put("category", BookCategory.DEFAULT_CATEGORY);// 全部书
		values1.put("isActive", 1);
		db.insert(categoryTable, "empty", values1);
		// }
		values1.put("count", 0);// 为了保证全部--我的畅读---未分类--三者的显示的前后顺序。所以先对全部--在线畅读分类进行插入一次。
		values1.put("category", BookCategory.MYBUYED_CATEGORY);// 已购
		values1.put("isActive", 0);
		db.insert(categoryTable, "empty", values1);
		// }
		values1.put("count", 0);
		values1.put("category", BookCategory.ONLINEREAD_CATEGORY);// 畅读书，第一次安装应该数量为零。
		values1.put("isActive", 0);
		db.insert(categoryTable, "empty", values1);
		// }

		values1.put("count", 0);
		values1.put("category", BookCategory.UNCLASSIFIED_CATEGORY);// 畅读书，第一次安装应该数量为零。
		values1.put("isActive", 0);
		db.insert(categoryTable, "empty", values1);
		// }
		for (String name : list) {
			if (!name.equals(BookCategory.DEFAULT_CATEGORY)
					&& !name.equals(BookCategory.MYBUYED_CATEGORY)
					&& !name.equals(BookCategory.ONLINEREAD_CATEGORY)
					&& !name.equals(BookCategory.UNCLASSIFIED_CATEGORY)) {
				values1.put("count", 0);
				values1.put("category", name);
				values1.put("isActive", 0);
				db.insert(categoryTable, "empty", values1);
			}
		}

	}

	private static void updateCategory(SQLiteDatabase db, String categoryTable,
			String booktable) {
		// 为了使排列顺序一致。
		updateCategorySort(db, categoryTable);
		ContentValues values1 = new ContentValues();
		Cursor cur = db.query(booktable, new String[] { "category,count(*)" },
				null, null, "category", null, null);
		if (cur == null) {
			Log.i("", "cur==null");
			return;
		}
		int sum = 0;
		while (cur.moveToNext()) {
			String name = cur.getString(0);
			int count = cur.getInt(1);
			if (name.equals("")) {
				name = BookCategory.UNCLASSIFIED_CATEGORY;
			}
			values1.put("count", count);
			values1.put("category", name);
			values1.put("isActive", 0);
			if (db.update(categoryTable, values1, "category=?",
					new String[] { name }) == 0) {
				db.insert(categoryTable, "empty", values1);
			}
			sum += count;
		}
		values1.put("count", sum);
		values1.put("category", BookCategory.DEFAULT_CATEGORY);// 全部书
		values1.put("isActive", 1);
		if (db.update(categoryTable, values1, "category=?",
				new String[] { BookCategory.DEFAULT_CATEGORY }) == 0) {
			db.insert(categoryTable, "empty", values1);
		}
		cur.close();
		cur = null;
	}

	// @Override
	private static boolean saveBookmark(SQLiteDatabase db, String tableName,
			MyBookmark myBookmark) {
		if (myBookmark.getDataType() == DataProvider.TYPE_LAST_POS_OLD) {
			myBookmark.setDataType(MyBookmark.DATATYPE_LAST_POS);
		} else if (myBookmark.getDataType() == DataProvider.TYPE_POS_OLD) {
			myBookmark.setDataType(MyBookmark.DATATYPE_POS);
		}
		myBookmark.setOperatingState(MyBookmark.STATE_MODIFY);
		ContentValues values = myBookmark.toValues();
		String selection = "book_id=" + myBookmark.getBook_id()
				+ " AND client_id=" + myBookmark.getClientId()
				+ " AND data_type=" + myBookmark.getDataType();
		// Uri uri = null;
		long id = -1;
		if ((db.update(tableName, values, selection, null)) == 0) {
			values.remove("client_id");
			myBookmark.setOperatingState(MyBookmark.STATE_ADD);
			id = db.insert(tableName, "Content is empty", values);
		}
		boolean isSuccess = false;
		if (id == -1) {
			isSuccess = false;
		} else {
			isSuccess = true;
		}
		return isSuccess;
	}

	public static void initOrUpdateDB(Context context) {
		DatabaseHelper mOpenHelper = new DatabaseHelper(context);
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		updateCategory(db, DataProvider.TABLE_NAME_BOOKCATEGORY,
				DataProvider.TABLE_NAME_BOOKINFOR);
		db.close();
		mOpenHelper.close();
	}

	// private static final String SQL_CREATE_TABLE_BOOKMARK =
	// "CREATE TABLE IF NOT EXISTS "+TABLE_NAME_BOOKMARK+" ("
	// + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
	// + "type INTEGER NOT NULL,"
	// + "book_id INTEGER NOT NULL REFERENCES BookInforTable(id),"
	// + "creation_time LONG DEFAULT 0,"
	// + "modification_time INTEGER DEFAULT 0,"
	// + "access_time INTEGER DEFAULT 0,"
	// + "access_counter INTEGER DEFAULT 0,"
	// + "paragraph_index INTEGER NOT NULL,"
	// + "element_index INTEGER NOT NULL,"
	// + "char_index INTEGER NOT NULL," + "bookmark_title TEXT,"
	// + "bookmark_from INTEGER NOT NULL,"
	// + "bookmark_total INTEGER NOT NULL,"
	// + "bookmark_to INTEGER NOT NULL)";
	/**
	 * 读取1.1.1的数据以原来以毫秒为单位，1.1.2以后改成秒为单位，所有的书签毫秒需要除以1000改成秒
	 * 
	 * */
	private static void loadOldBookmarkFromCursor(MyBookmark bookmark,
			Cursor cursor) {
		int i = -1;
		bookmark.setClientId(cursor.getLong(++i));
		bookmark.setDataType((int) cursor.getLong(++i));
		bookmark.setBook_id((int) cursor.getLong(++i));
		bookmark.setCreation_time(cursor.getLong(++i));
		long modefy_time = cursor.getLong(++i);// 还有一个字段是修改时间，
		bookmark.setDeviceTime(bookmark.getCreation_time());
		if (bookmark.getDeviceTime() > 0) {
			int year = (int) (bookmark.getDeviceTime() / 60 / 60 / 24 / 365);
			Log.i("zhoubo", "year==" + year);
			if (year > 1000) {
				bookmark.setDeviceTime(bookmark.getDeviceTime() / 1000);
				Log.i("zhoubo",
						"====bookmark.setDeviceTime(bookmark.getDeviceTime()/1000)");
			}
		} else {
			bookmark.setDeviceTime(System.currentTimeMillis() / 1000);
			Log.i("zhoubo",
					"=== bookmark.setDeviceTime(System.currentTimeMillis()/1000);");
		}
		long access_time = cursor.getLong(++i);
		long access_counter = cursor.getLong(++i);
		bookmark.setParagraph_index((int) cursor.getLong(++i));
		bookmark.setElement_index((int) cursor.getLong(++i));
		bookmark.setChar_index((int) cursor.getLong(++i));
		bookmark.setName(cursor.getString(++i));
		bookmark.setOffset(cursor.getLong(++i));
		bookmark.setOffsetTotal((int) cursor.getLong(++i));
		// bookmark.setOffset((int)cursor.getLong(++i));
		// bookmark.setProgress((float)(bookMark_from*100)/bookmark.getBookmark_total());
	}

	// private ArrayList getOldBookmarkList(){
	// ArrayList list = new ArrayList();
	//
	// return list;
	// }

	// @Override
	// public boolean getOldBookMarks() {
	// // TODO Auto-generated method stub
	// return false;
	// }

	public synchronized static ArrayList<MyBookmark> getOldBookMarks(
			SQLiteDatabase db, String tableName) {
		boolean found = false;
		Cursor rs = null;
		ArrayList<MyBookmark> bookmarks = new ArrayList<MyBookmark>();
		try {
			// clause = " WHERE " + clause;
			rs = db.query(tableName, null, null, null, null, null, null);
			if (rs.moveToFirst()) {
				do {
					MyBookmark v = new MyBookmark();
					loadOldBookmarkFromCursor(v, rs);
					bookmarks.add(v);
					found = true;
				} while (rs.moveToNext());
			}
		} finally {
			if (rs != null)
				rs.close();
		}
		return bookmarks;
	}

	/**
	 * 判断某张表是否存在
	 * 
	 * @param tabName
	 *            表名
	 * @return
	 */
	public static boolean tabbleIsExist(SQLiteDatabase db, String tableName) {
		boolean result = false;
		tableName = tableName.trim();
		if (TextUtils.isEmpty(tableName)) {
			return false;
		}
		// SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			// db = this.getReadableDatabase();
			String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='"
					+ tableName + "' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}
			cursor.close(); // 关闭游标。
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}
}
