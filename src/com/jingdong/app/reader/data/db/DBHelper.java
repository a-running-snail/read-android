package com.jingdong.app.reader.data.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.entity.BookCategory;
import com.jingdong.app.reader.entity.BookInforEDetail;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.localreading.MyBookmark;
import com.jingdong.app.reader.plugin.FontItem;
import com.jingdong.app.reader.plugin.FontParser;
import com.jingdong.app.reader.util.FileGuider;
import com.jingdong.app.reader.util.FileUtils;
import com.jingdong.app.reader.util.FontManager;
import com.jingdong.app.reader.util.Log;

public class DBHelper {
	public static final int KEY_SORT_ORDERTYPE_NAME = 1;
	public static final int KEY_SORT_ORDERTYPE_AUTHOR = 2;
	public static final int KEY_SORT_ORDERTYPE_TIME = 3;

	public static final Uri uri = DataProvider.CONTENT_URI_EBOOK;
	public static final Uri uri_pluins = DataProvider.CONTENT_URI_NAME_PLUGINS;
	public static final Uri uri_car = DataProvider.CONTENT_URI_BOOKCART;
	public final static int FORM_PAGE_HOME = 0;
	public final static int FORM_PAGE_LOCAL_E = 1;
	
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

	public static void insertBookCart(BookInforEDetail book) {
		// System.out.println(book.bookid+"-"+book.bookName);
		Log.i("insertBookCart", book.bookid + "-" + book.bookName);
		ContentValues values = new ContentValues();
		// 363687
		values.put("id", book.bookid);
		// values.put("id","111482");
		values.put("book_name", book.bookName);
		values.put("book_type", book.bookType);
		values.put("num", book.count);
		values.put("addcar_time", System.currentTimeMillis());
		String selection = "id=?";
		if ((MZBookApplication.getContext().getContentResolver().update(
				uri_car, values, selection, new String[] { book.bookid + "" })) == 0) {
			MZBookApplication.getContext().getContentResolver()
					.insert(uri_car, values);
		}
		MZBookApplication.getContext().getContentResolver()
				.notifyChange(uri_car, null);
	}

	public static int getShopType(Long bookId) {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		int type = -1;
		String[] projection = new String[] { "book_type" };
		Cursor cur;
		String selection = "id=?";
		String sortOrder = null;
		cur = mContentResolver.query(uri_car, projection, selection,
				new String[] { bookId + "" }, sortOrder);// " DESC");
		if (cur == null) {
			Log.i("", "cur==null");
			return -1;
		}
		while (!cur.isAfterLast()) {
			if (cur.isBeforeFirst()) {
				cur.moveToNext();
			} else {
				int i = -1;
				type = cur.getInt(++i);
				cur.moveToNext();
				break;
			}
		}
		cur.close();
		return type;
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

	public static int getCartBookNum() {
		String[] projection = null;
		String selection = null;
		Cursor cur;
		// String sortOrder = "id" + " ASC";
		cur = MZBookApplication.getInstance().getContentResolver()
				.query(uri_car, projection, selection, null, null);// " DESC");
		if (cur == null) {
			Log.i("", "cur==null");
			return 0;
		}
		int count = cur.getCount();
		cur.close();
		return count;
	}

	public static boolean isExistIntCart(long id) {
		boolean isExist = false;
		String[] projection = null;
		String selection = "id=?";
		;
		Cursor cur;
		String sortOrder = null;
		cur = MZBookApplication
				.getInstance()
				.getContentResolver()
				.query(uri_car, projection, selection,
						new String[] { id + "" }, sortOrder);// " DESC");
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

	public static void cleanCartBookList() {
		MZBookApplication.getInstance().getContentResolver()
				.delete(uri_car, null, null);
		MZBookApplication.getContext().getContentResolver()
				.notifyChange(uri_car, null);
	}

	public static boolean delCartBookById(String[] bookid) {
		int flag;
		flag = MZBookApplication.getInstance().getContentResolver()
				.delete(uri_car, "id=?", bookid);
		MZBookApplication.getContext().getContentResolver()
				.notifyChange(uri_car, null);

		if (flag == 1) {
			return true;// 全部删除则返回0

		} else {
			return false;// 返回删除失败的数量
		}
	}

	public static void registerContentObserver(ContentObserver cob) {
		MZBookApplication.getContext().getContentResolver()
				.registerContentObserver(uri, true, cob);
	}

	public static void unregisterContentObserver(ContentObserver cob) {
		MZBookApplication.getContext().getContentResolver()
				.unregisterContentObserver(cob);
	}

	public static int getBookinforCount() {
		String category = "";
		Cursor cur;
		int count = 0;
		String mActiviyCategory = BookCategory.getActiveCategory();
		if (mActiviyCategory.equals(BookCategory.DEFAULT_CATEGORY)) {
			cur = MZBookApplication.getInstance().getContentResolver()
					.query(uri, new String[] { "count(*)" }, null, null, null);
		} else {
			if (!mActiviyCategory.equals(BookCategory.UNCLASSIFIED_CATEGORY))
				category = mActiviyCategory;
			cur = MZBookApplication
					.getInstance()
					.getContentResolver()
					.query(uri, new String[] { "count(*)" }, "category=?",
							new String[] { category }, null);
		}
		if (cur == null)
			return count;
		while (!cur.isAfterLast()) {
			if (cur.isBeforeFirst()) {
				cur.moveToNext();
			} else {
				count = cur.getInt(0);
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

	/****
	 * 插入插件信息到数据库中
	 * 
	 * @author yfxiawei
	 * @param fontItem
	 *            插件对象
	 * @since version 1.2.4
	 * 
	 * *****************/
	public static boolean savePlugin(FontItem fontItem) {
		// System.out.println(book.bookid+"-"+book.bookName);
		boolean isSave = true;
		ContentValues values = new ContentValues();
		// 363687
		values.put("plugin_name", fontItem.getName());
		// values.put("id","111482");
		values.put("plugin_fileurl", fontItem.getUrl());
		values.put("plugin_filepath", fontItem.getFilePath());
		values.put("plugin_type", fontItem.getPlugin_type());
		values.put("plugin_src", fontItem.getPlugin_src());
		values.put("plugin_total_size", fontItem.getTotalSize());
		values.put("plugin_current_size", fontItem.getCurrentSize());
		values.put("plugin_download_status", fontItem.getDownloadStatus());
		values.put("plugin_init_show_total_size",
				fontItem.getInitShowTotalSize());
		if (MZBookApplication
				.getContext()
				.getContentResolver()
				.update(uri_pluins, values,
						"_id" + "=" + "'" + fontItem.getId() + "'", null) == 0) {
			Uri uri = MZBookApplication.getContext().getContentResolver()
					.insert(uri_pluins, values);
			if (uri != null) {
				isSave = true;
			}
		} else {
			isSave = true;
		}
		MZBookApplication.getContext().getContentResolver()
				.notifyChange(uri_pluins, null);
		return isSave;
	}

	/****
	 * 获取插件信息
	 * 
	 * @author yfxiawei 2013年2月28日13:37:00
	 * @param type
	 *            接受插件类型，字体为0
	 * @return ArrayList 返回插件对象list
	 * @since version 1.2.4
	 * *****************/
	public static ArrayList<FontItem> queryPluginItemList(int type) {
		// System.out.println(book.bookid+"-"+book.bookName);
		ArrayList<FontItem> pluginArrayList = new ArrayList<FontItem>();
		String selection = "plugin_type=?";
		Cursor cur;
		boolean mediaMountStaus = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);

		cur = MZBookApplication
				.getContext()
				.getContentResolver()
				.query(uri_pluins, null, selection, new String[] { type + "" },
						null);
		while (cur.moveToNext()) {
			FontItem fontItem = new FontItem();
			fontItem.setId(cur.getInt(0));
			fontItem.setPlugin_type(cur.getInt(2));
			fontItem.setPlugin_src(cur.getInt(3));// cur.getInt(4)enable
			fontItem.setPlugin_enable(cur.getInt(4));
			fontItem.setName(cur.getString(5));
			fontItem.setTotalSize(cur.getLong(8));
			fontItem.setCurrentSize(cur.getLong(9));

			fontItem.setDownloadStatus(cur.getInt(10));
			fontItem.setFilePath(cur.getString(11));
			fontItem.setUrl(cur.getString(12));
			fontItem.setInitShowTotalSize(cur.getLong(13));

			if (mediaMountStaus) {// 只有挂载的时候操作
				if (fontItem.getFilePath()!=null && FileUtils.isExist(fontItem.getFilePath())) {
					pluginArrayList.add(fontItem);
				} else {

					if (fontItem.getPlugin_src() == FontItem.KEY_FONT_SRC_IMPORT) {
						fontDelete(fontItem.getFilePath());
					} else if (fontItem.getPlugin_src() == FontItem.KEY_FONT_SRC_INTERNAL
							&& fontItem.getDownloadStatus() == FontItem.STATE_LOADED) {
						fontItem.setDownloadStatus(FontItem.STATE_UNLOAD);
						fontItem.setPlugin_enable(FontItem.KEY_PLUGIN_DISABLE);

						updateFontStatus(fontItem);
						pluginArrayList.add(fontItem);
					} else {
						pluginArrayList.add(fontItem);
					}

				}

			} else {// 未挂载存储不做检测
				pluginArrayList.add(fontItem);
			}

		}
		cur.close();
		cur = null;
		FontItem fontItem = queryEnabledFontItem();// 如果没有找到以选中的字体则把系统字体设为激活
		if (TextUtils.isEmpty(fontItem.getFilePath())) {
			if ( pluginArrayList.size() > 0 ) {
				pluginArrayList.get(0).setPlugin_enable(FontItem.KEY_PLUGIN_ENABLE);
				updateFontStatus(pluginArrayList.get(0));
			}
		}
		MZBookApplication.getContext().getContentResolver()
				.notifyChange(uri_pluins, null);
		return pluginArrayList;
	}

	/****
	 * 检测字体是否已经导入
	 * 
	 * @author yfxiawei 2013年2月28日13:34:01
	 * @param filePath
	 *            字体觉得路径
	 * @since version 1.2.4
	 * 
	 * @return boolean 如果已存在返回true，不存在则返回false
	 * *****************/
	public static boolean fontIsImported(String filePath) {
		// System.out.println(book.bookid+"-"+book.bookName);
		int temp = 0;
		String selection = "plugin_filepath=?";
		Cursor cur;

		cur = MZBookApplication.getContext().getContentResolver()
				.query(uri_pluins, null, selection, new String[] { filePath },

				null);
		temp = cur.getCount();
		cur.close();
		cur = null;
		return temp == 1;
	}

	/****
	 * 删除字体文件
	 * 
	 * @author yfxiawei 2013年2月28日13:34:01
	 * @param filePath
	 *            字体绝对路径
	 * @since version 1.2.4
	 * 
	 * @return boolean
	 * *****************/
	public static boolean fontDelete(String filePath) {
		// System.out.println(book.bookid+"-"+book.bookName);
		int temp = 0;
		String selection = "plugin_filepath=?";

		temp = MZBookApplication.getContext().getContentResolver()
				.delete(uri_pluins, selection, new String[] { filePath });
		MZBookApplication.getContext().getContentResolver()
				.notifyChange(uri_pluins, null);
		return temp == 1;
	}

	/****
	 * 查询已经激活的字体
	 * 
	 * @author yfxiawei 2013年2月28日13:33:18
	 * @return FontItem 返回激活的字体对象
	 * @since version 1.2.4
	 * *****************/
	public static FontItem queryEnabledFontItem() {

		String selection = "plugin_type=? and plugin_enable=?";
		Cursor cur;
		FontItem fontItem = new FontItem();
		fontItem.setFilePath("");
		cur = MZBookApplication
				.getContext()
				.getContentResolver()
				.query(uri_pluins,
						null,
						selection,
						new String[] { FontItem.KEY_PLUGIN_FONT + "",
								FontItem.KEY_PLUGIN_ENABLE + "" }, null);
		while (cur.moveToNext()) {
			fontItem.setId(cur.getInt(0));
			fontItem.setPlugin_type(cur.getInt(2));
			fontItem.setPlugin_src(cur.getInt(3));// cur.getInt(4)enable
			fontItem.setPlugin_enable(cur.getInt(4));
			fontItem.setName(cur.getString(5));
			fontItem.setTotalSize(cur.getLong(8));
			fontItem.setCurrentSize(cur.getLong(9));

			fontItem.setDownloadStatus(cur.getInt(10));
			fontItem.setFilePath(cur.getString(11));
			fontItem.setUrl(cur.getString(12));
			fontItem.setInitShowTotalSize(cur.getLong(13));
		}
		cur.close();
		cur = null;
		return fontItem;
	}
	
	/****
	 * 依据字体名称查询
	 * 
	 * @author liqiang 2015年1月21日
	 * @return FontItem 返回字体对象
	 * *****************/
	public static FontItem queryFontItemByName(String fontName) {

		String selection = "plugin_type=? and plugin_name=?";
		Cursor cur;
		FontItem fontItem = new FontItem();
		fontItem.setFilePath("");
		cur = MZBookApplication
				.getContext()
				.getContentResolver()
				.query(uri_pluins,
						null,
						selection,
						new String[] { FontItem.KEY_PLUGIN_FONT + "",
						fontName }, null);
		while (cur.moveToNext()) {
			fontItem.setId(cur.getInt(0));
			fontItem.setPlugin_type(cur.getInt(2));
			fontItem.setPlugin_src(cur.getInt(3));// cur.getInt(4)enable
			fontItem.setPlugin_enable(cur.getInt(4));
			fontItem.setName(cur.getString(5));
			fontItem.setTotalSize(cur.getLong(8));
			fontItem.setCurrentSize(cur.getLong(9));

			fontItem.setDownloadStatus(cur.getInt(10));
			fontItem.setFilePath(cur.getString(11));
			fontItem.setUrl(cur.getString(12));
			fontItem.setInitShowTotalSize(cur.getLong(13));
		}
		cur.close();
		cur = null;
		return fontItem; 
	}

	/*****
	 * 
	 * 初始化默认字体数据 在oncreate 和onupgrade调用 只在创建表的时候调用 只有数据库创建完毕才可以使用
	 * 
	 * @author yfxiawei 2013年2月27日15:45:35
	 * ********/
	public static void initDefautDbData() {
		ArrayList<FontItem> fontItems = new ArrayList<FontItem>();
		FileGuider fileGuider = new FileGuider(
				FileGuider.SPACE_PRIORITY_EXTERNAL);
		HashMap< String, String > font_map = FontManager.enumerateFonts();
		
		if(null == font_map) {
			return;
		}
		
		Iterator< String> iter = font_map.keySet().iterator(); 
		while (iter.hasNext()) {
			FontItem fontSys = new FontItem();
			String key = iter.next();
			String value[] = font_map.get(key).split("===");
			fileGuider.setFileName(value[0]);
			fontSys.setFilePath(key);
			fontSys.setCurrentSize(0);
			fontSys.setName("系统字体 " + value[0]);
			fontSys.setPlugin_enable(FontItem.KEY_PLUGIN_DISABLE);
			fontSys.setPlugin_src(FontItem.KEY_FONT_SRC_System);
			fontSys.setPlugin_type(FontItem.KEY_PLUGIN_FONT);
			fontSys.setInitShowTotalSize(Integer.parseInt(value[1]));
			fontSys.setDownloadStatus(FontItem.STATE_LOADED);
			fontSys.setUrl(null);
			fontItems.add(fontSys);
		}
		if (fontItems.size() > 0) {
			fontItems.get(0).setPlugin_enable(FontItem.KEY_PLUGIN_ENABLE);
		}
		fileGuider.setChildDirName("fontExt");
		
		FontItem fontItem_FS = new FontItem(FontItem.FOUNDER_SS);
		fileGuider.setFileName(FontItem.FOUNDER_SS_FILE);
		fontItem_FS.setFilePath(fileGuider.getFilePath());
		fontItem_FS.setCurrentSize(0);
		fontItem_FS.setPlugin_src(FontItem.KEY_FONT_SRC_INTERNAL);
		fontItem_FS.setPlugin_type(FontItem.KEY_PLUGIN_FONT);
		fontItem_FS.setInitShowTotalSize(11057836);
		fontItem_FS.setDownloadStatus(FontItem.STATE_UNLOAD);
		if (fileGuider.getFilePath()!=null && FileUtils.isExist(fileGuider.getFilePath())
				&& FontParser.getInstance().parse(fileGuider.getFilePath())) {
			fontItem_FS.setDownloadStatus(FontItem.STATE_LOADED);
		}
		// FIXME 方正仿宋 下载地址写死的
		fontItem_FS.setUrl("http://jss.360buy.com/outLinkServicePoint/8922d2ca-cb3d-4ca4-90c4-3dc8658b4d0b.ttf");
//		fontItem_FS.setUrl("http://storage.jd.com/epub/_shifengquanC07A7AEDFFD111332EA3DF67C9919C61.ttf?Expires=3037483600&AccessKey=b370e4cd325c23c9791549b02aa2f09934421b86&Signature=I5uR44534ZhDN%2BXtzyXp%2FwhYuYQ%3D");
		
		fontItems.add(fontItem_FS);

		FontItem fontItem_LTH = new FontItem(FontItem.FOUNDER_LANTINGHEI);
		fileGuider.setFileName(FontItem.FOUNDER_LANTINGHEI_FILE);
		fontItem_LTH.setFilePath(fileGuider.getFilePath());
		fontItem_LTH.setCurrentSize(0);
		fontItem_LTH.setPlugin_src(FontItem.KEY_FONT_SRC_INTERNAL);
		fontItem_LTH.setPlugin_type(FontItem.KEY_PLUGIN_FONT);
		fontItem_LTH.setInitShowTotalSize(9820870);
		fontItem_LTH.setDownloadStatus(FontItem.STATE_UNLOAD);
		if (fileGuider.getFilePath()!=null && FileUtils.isExist(fileGuider.getFilePath())
				&& FontParser.getInstance().parse(fileGuider.getFilePath())) {
			fontItem_LTH.setDownloadStatus(FontItem.STATE_LOADED);
		}
		fontItem_LTH.setUrl(Configuration.pluginHost + FontItem.FOUNDER_LANTINGHEI_FILE);
//		fontItem_LTH.setUrl("http://storage.jd.com/epub/_shifengquan6277AA098ACD3CC3F61D9E919B3899EC.ttf?Expires=3037483602&AccessKey=b370e4cd325c23c9791549b02aa2f09934421b86&Signature=jaq%2BPtQZrej15vOLPpSBFT8HYB4%3D");
		fontItems.add(fontItem_LTH);

		FontItem fontItemKT = new FontItem(FontItem.FOUNDER_KAITI);
		fileGuider.setFileName(FontItem.FOUNDER_KAITI_FILE);
		fontItemKT.setFilePath(fileGuider.getFilePath());

		fontItemKT.setCurrentSize(0);
		fontItemKT.setPlugin_src(FontItem.KEY_FONT_SRC_INTERNAL);
		fontItemKT.setPlugin_type(FontItem.KEY_PLUGIN_FONT);
		fontItemKT.setInitShowTotalSize(18814112);
		fontItemKT.setDownloadStatus(FontItem.STATE_UNLOAD);
		if (fileGuider.getFilePath()!=null && FileUtils.isExist(fileGuider.getFilePath())
				&& FontParser.getInstance().parse(fileGuider.getFilePath())) {
			fontItemKT.setDownloadStatus(FontItem.STATE_LOADED);
		}
		fontItemKT.setUrl(Configuration.pluginHost + FontItem.FOUNDER_KAITI_FILE);
//		fontItemKT.setUrl("http://storage.jd.com/epub/_shifengquan91A57D71063D3ABB2E63F04FD974A448.ttf?Expires=3037483601&AccessKey=b370e4cd325c23c9791549b02aa2f09934421b86&Signature=ILhUMfbEB2QZzOI1EkAJDOPL3Y4%3D");
		fontItems.add(fontItemKT);

		FontItem fontItem_MWH = new FontItem(FontItem.FOUNDER_MIAOWUHEI);
		fileGuider.setFileName(FontItem.FOUNDER_MIAOWUHEI_FILE);
		fontItem_MWH.setFilePath(fileGuider.getFilePath());
		fontItem_MWH.setCurrentSize(0);
		fontItem_MWH.setPlugin_src(FontItem.KEY_FONT_SRC_INTERNAL);
		fontItem_MWH.setPlugin_type(FontItem.KEY_PLUGIN_FONT);
		fontItem_MWH.setInitShowTotalSize(5672024);
		fontItem_MWH.setDownloadStatus(FontItem.STATE_UNLOAD);
		if (fileGuider.getFilePath()!=null && FileUtils.isExist(fileGuider.getFilePath())
				&& FontParser.getInstance().parse(fileGuider.getFilePath())) {
			fontItem_MWH.setDownloadStatus(FontItem.STATE_LOADED);
		}
		fontItem_MWH.setUrl(Configuration.pluginHost + FontItem.FOUNDER_MIAOWUHEI_FILE);
//		fontItem_MWH.setUrl("http://storage.jd.com/epub/_shifengquan7C8BC81E05B1199531426174FE19C03D.ttf?Expires=3037483604&AccessKey=b370e4cd325c23c9791549b02aa2f09934421b86&Signature=DBZrKug0u7tTo6XCImRFygIg6a8%3D");
		fontItems.add(fontItem_MWH);

		// FontItem[] tempFontItems = new FontItem[4];
		// tempFontItems[0] = fontSys;
		// tempFontItems[1] = fontItem_LTH;
		// tempFontItems[2] = fontItemKT;
		// tempFontItems[3] = fontItem_MWH;
		ContentValues values = new ContentValues();
		for (int i = 0; i < fontItems.size(); i++) {

			// 363687
			values.put("plugin_name", fontItems.get(i).getName());
			// values.put("id","111482");
			values.put("plugin_fileurl", fontItems.get(i).getUrl());
			values.put("plugin_filepath", fontItems.get(i).getFilePath());
			values.put("plugin_type", fontItems.get(i).getPlugin_type());
			values.put("plugin_src", fontItems.get(i).getPlugin_src());
			values.put("plugin_enable", fontItems.get(i).getPlugin_enable());
			values.put("plugin_total_size", fontItems.get(i).getTotalSize());
			values.put("plugin_current_size", fontItems.get(i).getCurrentSize());
			values.put("plugin_download_status", fontItems.get(i)
					.getDownloadStatus());
			values.put("plugin_init_show_total_size", fontItems.get(i)
					.getInitShowTotalSize());
			MZBookApplication.getInstance().getContentResolver()
					.insert(DataProvider.CONTENT_URI_NAME_PLUGINS, values);
			values.clear();

		}

	}

	/****
	 * 插件状态切换【支持下载状态和激活状态】 如果首次使用enabledFont可以为null，以后不能为
	 * null，数据库字段enable只能有一行为激活状态 2013年2月28日11:20:51
	 * 
	 * @author yfxiawei
	 * @param enabledFont
	 *            已经激活的字体对象，可以为null
	 * @param tobeEnableFontItem
	 *            需要激活的字体对象，不能为null
	 * @since 1.2.4
	 * 
	 * *****************/
	public static void updateFontStatus(FontItem tobeEnableFontItem) {

		String where = "_id=?";
		ContentValues contentValues = new ContentValues();
		contentValues.put("plugin_enable", FontItem.KEY_PLUGIN_DISABLE);

		MZBookApplication.getContext().getContentResolver()
				.update(uri_pluins, contentValues, null, null);

		contentValues.clear();
		contentValues.put("plugin_enable",
				tobeEnableFontItem.getPlugin_enable());
		contentValues.put("plugin_download_status",
				tobeEnableFontItem.getDownloadStatus());
		MZBookApplication
				.getContext()
				.getContentResolver()
				.update(uri_pluins, contentValues, where,
						new String[] { tobeEnableFontItem.getId() + "" });
		MZBookApplication.getContext().getContentResolver()
				.notifyChange(uri_pluins, null);

	}
}
