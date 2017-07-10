package com.jingdong.app.reader.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadThread;
import com.jingdong.app.reader.client.DownloadThreadQueue.PregressAble;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.client.RequestEntry;
import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.config.Constant;
import com.jingdong.app.reader.config.ITransKey;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.data.db.DBBookMarkHelper;
import com.jingdong.app.reader.data.db.DBHelper;
import com.jingdong.app.reader.data.db.DataProvider;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.localreading.BookNote;
import com.jingdong.app.reader.localreading.MyBookNote;
import com.jingdong.app.reader.localreading.MyBookmark;
import com.jingdong.app.reader.net.HttpSetting;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OlineDesUtils;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.FileGuider;
import com.jingdong.app.reader.util.FileUtils;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NativeWrapper;
import com.jingdong.app.reader.util.SecretKeyUtil;
import com.jingdong.app.reader.util.StreamToolBox;
import com.jingdong.app.reader.util.ToastUtil;

public class LocalBook extends Entity implements Comparable<LocalBook>,
		ITransKey, DownloadedAble {

	static Uri uri = DataProvider.CONTENT_URI_EBOOK;
	static Uri URI_RANDOM = DataProvider.CONTENT_URI_NAME_RANDOM;
	public final static String[] ALL_PROJECTION = new String[] { "_id","book_id",
			"order_code", "product_code", "size", "url", "price", "author",
			"user_name", "title", "signature", "source", "cert", "random",
			"type_id", "progress", "state_load", "book_state", "mod_time",
			"add_time", "access_time", "hot_exp", "book_path", "big_image_url",
			"small_image_url", "big_image_path", "small_image_path",
			"package_name", "dir", "version", "operating_state", "book_marks",
			"read_progress", "boot", "gift_book_infor", "temp_bookmark",
			"category", "format", "format_name", "card_num", "device_id",
			"note_operating_state", "note_version","borrow_end_time" };
	public final static String TEMP_PAth_ZIP = "123.jeb";
	public final static String TEMP_PAth_APK = "578.jeb";
	public static int STATE_INIT = -1;
	public final static int STATE_NO_LOAD = STATE_INIT + 1;
	public final static int STATE_LOADED = STATE_INIT + 2;// 已读
	public final static int STATE_LOAD_READY = STATE_INIT + 3;
	public final static int STATE_LOADING = STATE_INIT + 4;
	public final static int STATE_LOAD_FAILED = STATE_INIT + 5;
	public final static int STATE_LOAD_PAUSED = STATE_INIT + 6;
	public final static int STATE_LOAD_READING = STATE_INIT + 7;// 未读

	public final static int BOOK_STATE_NO_LOADED = STATE_INIT + 0;// 未读
	public final static int BOOK_STATE_NO_INTSTALL = STATE_INIT + 1;// 未读
	public final static int BOOK_STATE_INTSTALL = STATE_INIT + 2;// 未读

	public final static String SOURCE_BULIT_IN = "built_in";
	public final static String SOURCE_ONLINE_BOOK = "online_book";
	public final static String SOURCE_BUYED_BOOK = "buyed_book";
	public final static String SOURCE_BORROWED_BOOK = "borrowed_book";
	public final static String SOURCE_TRYREAD_BOOK = "tryread_book";
	public final static String SOURCE_USER_BORROWED_BOOK = "user_borrowed_book";
	
	public final static int TYPE_BOOKDATA_MARK = 0;// 产生的数据类型，笔记或书签。
	public final static int TYPE_BOOKDATA_NOTE = 1;
	
	public final static int TYPE_EBOOK = 1;
	public final static int TYPE_M = 2;
	public final static int FORMAT_PDF = 1;
	public final static int FORMAT_EPUB = 2;

	public final static String FORMATNAME_EPUB = "epub";
	public final static String FORMATNAME_PDF = "pdf";
	public final static int DOWNING_CERT = 1;// 未读
	public final static int DOWNING_BIG_IMG = 2;
	public final static int DOWNING_SMALL_IMG = 3;
	public final static int DOWNING_BOOK_URL = 4;
	public final static int DOWNING_BOOK_CONTENT = 5;
	public final static String ENCRYPT_PREFIX = "mmm_";
	private int requestCode = -1;
	public int downingWhich = -1;

	public int _id;
	/**
	 * 图书Id
	 */
	public long book_id;
	/**
	 * 订单号
	 */
	public String order_code = "";
	public String product_code = "";
	
	/**
	 * 图书文件大小
	 */
	public long size;
	
	/**
	 * 文件下载进度
	 */
	public long progress;
	/**
	 * 图书文件下载地址
	 */
	public String bookUrl = "";
	/**
	 * 价格
	 */
	public String price = "";
	/**
	 * 作者
	 */
	public String author = "";
	/**
	 * 
	 */
	public String userName;
	public String title = "";
	public String signature = "";
	/**
	 * 来源：试读、畅读、已购、书城借阅、用户借阅、内置书
	 */
	public String source = "";
	/**
	 * DRM证书
	 */
	public String cert = "";
	public int type_id;// bookType 变量实在太乱了
	public int state = -1;
	public int bookState = -1;
	public long mod_time;
	public long add_time;
	public long access_time;
	public int hot_exp;
	public String book_path = "";
	public String category = "";
	public ReadProgress read_progress;
	
	/**
	 * Rights系统返回的引导文件信息
	 */
	public BootEntity boot;
	public LocalBook copy;
	public SubBook subCopy;
	// public static LocalBook LocalBook;
	public String random;
	public PregressAble imagePregressAbler;
	public ViewGroup parentView;
	public HttpSetting httpSetting;
	// public int position; // 对应list 中的 position
	public boolean isChecked = false;
	public long _priority = 0;
	private RequestEntry requestEntry;
	public DownloadThread downloadThread;
	public String bigImageUrl;
	public String smallImageUrl;
	public String bigImagePath;
	public String smallImagePath;
	public String packageName;
	public String dir;
	public GiftBookInfor giftBookInfor;
	public boolean isMenualStop = false;
	public long version;
	public long bookNoteVersion;
	public int operatingState = 0;
	public int operatingNoteState = 0;
	public long totalOffset = 0;// 不需要存进数据库
	public MyBookmark tempBookmark;
	public int format = -1;// 书的格式 1pdf类型 2epub类型
	public String formatName;// 书的格式名字：epub，pdf。
	public ArrayList<MyBookmark> bookMarks;
	public ArrayList<BookNote> bookNotes;// 对应的笔记
	public boolean tryGetContetnFromSD = false; // 尝试本地sd中是否存在正在下载的文件。
	public int belongPagCode;
	public boolean isOnline = false;
	public boolean isShowOlineRead = false;
	/**
	 * 该图书对应畅读卡
	 */
	public String cadrNum = "";
	public boolean isFromJdShop = false;
	public boolean needFreshImage = false;// 下载完成，通知图片缓存 刷新图片。
	/**
	 * 设备号
	 */
	public String deviceId = "";
	public String borrowEndTime = "";//借阅结束时间
	/** 用户借阅结束时间 */
	public String userBuyBorrowEndTime;
	/** 图书文件更新标识 */
	public boolean isUpdateEnable = false;
	
	@Override
	public void manualStop() {
		isMenualStop = true;
		stop();
	}

	/**
	 * 保存加密证书
	 * @return
	 */
	public boolean saveEncryptCert() {
		ContentValues values = new ContentValues();
		if (!TextUtils.isEmpty(cert) && !cert.startsWith(ENCRYPT_PREFIX)) {
			values.put("cert", ENCRYPT_PREFIX + OlineDesUtils.encrypt(cert));
		} else if (!TextUtils.isEmpty(cert)) {
			values.put("cert", cert);
		}
		boolean isSuccess = save(values, book_id);
		return isSuccess;
	}

	private void stop() {
		if (requestEntry != null) {
			requestEntry.setStop(true);
			if (requestEntry._request != null) {
				if (!requestEntry._request.isAborted()) {
					requestEntry._request.abort();
				}
			}
		}
		if (state == LocalBook.STATE_LOADING
				|| state == LocalBook.STATE_LOAD_READY) {
			state = LocalBook.STATE_LOAD_PAUSED;
		}
		DownloadService.refresh(this);
		mod_time = System.currentTimeMillis();
		MZLog.d("wangguodong", "暂停下载，保存下载状态####"+state);
		save();

		if (requestEntry != null) {

			HttpResponse _response = requestEntry._response;
			if (_response != null) {
				try {
					_response.getEntity().consumeContent();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
			InputStream inputStream = requestEntry._inputStream;
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void copyIntoIhis(LocalBook localBook) {
		progress = localBook.progress;
		size = localBook.size;
		state = localBook.state;
		bookState = localBook.bookState;
		cert = localBook.cert;
		random = localBook.random;
		book_path = localBook.book_path;
		category = localBook.category;
		bookUrl = localBook.bookUrl;
		packageName = localBook.packageName;
		smallImagePath = localBook.smallImagePath;
		bigImagePath = localBook.bigImagePath;
		downingWhich = localBook.downingWhich;
		dir = localBook.dir;
		tryGetContetnFromSD = localBook.tryGetContetnFromSD;
	}

	
	public static ArrayList<LocalBook> getLocalBookList(String[] projection,
			String selection, String[] selectionArgs) {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		ArrayList<LocalBook> list = new ArrayList<LocalBook>();
		Cursor cur;
		String sortOrder = "mod_time" + " ASC";
		cur = mContentResolver.query(uri, projection, selection, selectionArgs,
				sortOrder);// " DESC");
		if (cur == null) {
			Log.i("", "cur==null");
			return null;
		}
		for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			LocalBook localBook = getLocalBook(cur);
			list.add(localBook);
		}
		cur.close();
		return list;
	}


	public static LocalBook getLocalBook(String selection,
			String[] selectionArgs) {
		ContentResolver mContentResolver = MZBookApplication.getInstance().getContentResolver();
		LocalBook localBook = null;
		// String selection = null;
		Cursor cur;
		// if (id >= 0) {
		// selection = "id" + " = " + "'" + id + "'";
		// }
		String sortOrder = "mod_time" + " ASC";
		cur = mContentResolver.query(uri, ALL_PROJECTION, selection,
				selectionArgs, sortOrder);// " DESC");
		if (cur == null) {
			Log.i("", "cur==null");
			return null;
		}
		while (!cur.isAfterLast()) {
			if (cur.isBeforeFirst()) {
				cur.moveToNext();
			} else {
				localBook = getLocalBook(cur);
				cur.moveToNext();
				break;
			}
		}
		cur.close();
		return localBook;
	}

	public static LocalBook getLocalBook(Cursor cur) {
		LocalBook localBook = new LocalBook();
		int i = -1;
		localBook._id = cur.getInt(++i);
		localBook.book_id = cur.getLong(++i);
		localBook.order_code = cur.getString(++i);
		localBook.product_code = cur.getString(++i);
		localBook.size = cur.getLong(++i);
		localBook.bookUrl = cur.getString(++i);
		localBook.price = cur.getString(++i);
		localBook.author = cur.getString(++i);
		localBook.userName = cur.getString(++i);
		localBook.title = cur.getString(++i);
		localBook.signature = cur.getString(++i);
		localBook.source = cur.getString(++i);
		
		if(!TextUtils.isEmpty(localBook.source) && !TextUtils.isEmpty(String.valueOf(localBook.book_id)) && !TextUtils.isEmpty(localBook.userName) 
				&& alreadyEncrypt(localBook.source)){
			
			try {
				localBook.source = SecretKeyUtil.decrypt(String.valueOf(localBook.book_id), localBook.userName, localBook.source);
			} catch (UnsatisfiedLinkError e) {
				e.printStackTrace();
			}
		}
		
		String cert = cur.getString(++i);
		if (localBook.source.equals(LocalBook.SOURCE_ONLINE_BOOK)
				&& !TextUtils.isEmpty(cert) && cert.startsWith(ENCRYPT_PREFIX)) {
			cert = cert.substring(ENCRYPT_PREFIX.length());
			localBook.cert = OlineDesUtils.decrypt(cert);
		} else if (!TextUtils.isEmpty(cert)) {
			localBook.cert = cert;
		}
		localBook.random = cur.getString(++i);
		localBook.type_id = cur.getInt(++i);
		localBook.progress = cur.getLong(++i);
		localBook.state = cur.getInt(++i);
		localBook.bookState = cur.getInt(++i);
		localBook.mod_time = cur.getLong(++i);
		localBook.add_time = cur.getLong(++i);
		localBook.access_time = cur.getLong(++i);
		localBook.hot_exp = cur.getInt(++i);
		localBook.book_path = cur.getString(++i);
		localBook.bigImageUrl = cur.getString(++i);
		localBook.smallImageUrl = cur.getString(++i);
		localBook.bigImagePath = cur.getString(++i);
		localBook.smallImagePath = cur.getString(++i);
		localBook.packageName = cur.getString(++i);
		localBook.dir = cur.getString(++i);
		localBook.version = cur.getLong(++i);
		localBook.operatingState = cur.getInt(++i);
		// localBook.giftBookInfor = cur.getString(++i);
		byte[] bytes = cur.getBlob(++i);
		if (bytes != null && bytes.length > 0) {
			// getBookmarks(bytes);
		}
		bytes = cur.getBlob(++i);
		if (bytes != null && bytes.length > 0) {
			localBook.read_progress = getReadProgress(bytes);
		}
		bytes = cur.getBlob(++i);
		if (bytes != null && bytes.length > 0) {
			localBook.boot = BootEntity.loadSettingFromFile(bytes);
			// (BootEntity) StreamToolBox.getObject(bytes);
		}
		bytes = cur.getBlob(++i);
		if (bytes != null && bytes.length > 0) {
			localBook.giftBookInfor = (GiftBookInfor) StreamToolBox
					.getObject(bytes);
		}
		bytes = cur.getBlob(++i);
		if (bytes != null && bytes.length > 0) {
			localBook.tempBookmark = (MyBookmark) StreamToolBox
					.getObject(bytes);
		}
		localBook.category = cur.getString(++i);
		if (localBook.category == null || TextUtils.isEmpty(localBook.category)) {
			localBook.category = BookCategory.UNCLASSIFIED_CATEGORY;
		}
		// 增加的图书的格式字段。
		localBook.format = cur.getInt(++i);
		localBook.formatName = cur.getString(++i);
		localBook.cadrNum = cur.getString(++i);
		String deviceId = cur.getString(++i);
		if (!TextUtils.isEmpty(deviceId)) {
			try {
				localBook.deviceId = NativeWrapper.aseDencrypt(deviceId,
						localBook.book_id + "");
			} catch (UnsatisfiedLinkError e) {// 会报异常。
				localBook.deviceId = "";
			}
			// localBook.deviceId=deviceId;
		}
		localBook.operatingNoteState = cur.getInt(++i);
		localBook.bookNoteVersion = cur.getLong(++i);
		localBook.borrowEndTime = cur.getString(++i);
		return localBook;
	}



	/**
	 * 用户下的书
	 * 
	 * @param id
	 * @param userPin
	 * @return
	 */
	public static LocalBook getLocalBook(long id, String userPin) {
		LocalBook localBook = getLocalBook("book_id=?  AND user_name=?",new String[] { Long.toString(id), userPin });
		if (localBook != null) {
			localBook.random = localBook.readRandomFromDB();
		}
		return localBook;
	}

	public static LocalBook getLocalBookByPackName(String packageName) {

		// String selection = null;
		if (!TextUtils.isEmpty(packageName)) {
			// selection = "package_name" + " = " + "'" + packageName + "'";

			LocalBook localBook = getLocalBook("package_name=?",
					new String[] { packageName });
			return localBook;
		}
		return null;
	}

	/**
	 * 在分类中按图书名字查询。
	 * 
	 * @param book_name
	 * @return
	 */
	public static List<LocalBook> getLocalBookByName(String book_name) {
		List<LocalBook> books = new ArrayList<LocalBook>();

		LocalBook localBook = new LocalBook();
		Cursor cur;
		if (!TextUtils.isEmpty(book_name)) {
			cur = DBHelper.getLocalBookByNameInCategory(book_name);
		} else {
			cur = DBHelper.getLocalBookByNameInCategory("");
		}
		if (cur == null) {
			Log.i("", "cur==null");
			return null;
		}
		while (cur.moveToNext()) {
			localBook = getLocalBook(cur);
			if (localBook != null) {
				books.add(localBook);
			}
		}
		cur.close();
		return books;
	}

	public void del(boolean isDelLocalFile) {
		MZBookDatabase.instance.deleteBookShelfRecord(LoginUser.getpin(), _id);
		if (book_id <= 0) {
			DBHelper.delEbookById(new String[] { Long.toString(book_id) });
			// File file;
			if (isDelLocalFile) {
				File file = new File(book_path);
				if (file.exists())
					file.delete();

				if (!TextUtils.isEmpty(bigImagePath)) {
					file = new File(bigImagePath);
					if (file.exists())
						file.delete();
				}

				if (!TextUtils.isEmpty(smallImagePath)) {
					file = new File(smallImagePath);
					if (file.exists())
						file.delete();
				}
			}
			DBBookMarkHelper.deleteBookmarks(book_id);
		} else {
			if (state != STATE_LOADED && state != STATE_LOAD_READING)
				DownloadService.stop(this);
			// boolean result = DBHelper.delEbookById(new String[] { id + "" });
			DBHelper.delEbookById(new String[] { book_id + "" });
			// if(isInit){
			// MyBookDB.init(MZBookApplication.getContext());
			// }
			DBBookMarkHelper.deleteBookmarks(book_id);
			// if(isInit){
			// MyBookDB.uninit();
			// }
			if (isDelLocalFile) {
				if (!TextUtils.isEmpty(bigImagePath)) {
					FileUtils.delFile(bigImagePath);
				}
				if (!TextUtils.isEmpty(smallImagePath)) {
					FileUtils.delFile(smallImagePath);
				}
				if (!TextUtils.isEmpty(dir)) {
					FileUtils.delFolder(dir);
				}
			}
		}
	}

	/**
	 * @author keshuangjie
	 * @description 下载进度百分比
	 */
	public String getProgressPercent() {
		int progress = 0;
		if (this.size != 0) {
			progress = (int) (this.progress * 100 / this.size);
		}
		return progress + "%";
	}

	public void changeCategory(String category) {
		if (!this.category.equals(category)) {
			ContentValues values = new ContentValues();
			// String categoryTemp = category;
			if (category.equals(BookCategory.DEFAULT_CATEGORY)
					|| category.equals(BookCategory.UNCLASSIFIED_CATEGORY)) {
				category = "";
			}
			values.put("category", category);
			ContentResolver mContentResolver = MZBookApplication.getInstance()
					.getContentResolver();
			if ((mContentResolver.update(uri, values, "book_id" + "=" + "'"
					+ this.book_id + "'", null)) == 0) {
				mContentResolver.insert(uri, values);
			}
			this.category = category;
		}
	}

	/**
	 * 保存
	 * @param mContentResolver
	 * @param localBook
	 * @return
	 */
	public static boolean saveLocalBook(ContentResolver mContentResolver,LocalBook localBook) {
		ContentValues values = new ContentValues();
		values.put("book_id", localBook.book_id);
		values.put("order_code", localBook.order_code);
		values.put("product_code", localBook.product_code);
		values.put("size", localBook.size);
		values.put("url", localBook.bookUrl);
		values.put("price", localBook.price);
		values.put("author", localBook.author);
		values.put("user_name", localBook.userName);
		values.put("title", localBook.title);
		values.put("signature", localBook.signature);
		//将source进行加密保存
		if(!TextUtils.isEmpty(localBook.source) && !TextUtils.isEmpty(String.valueOf(localBook.book_id)) && !TextUtils.isEmpty(localBook.userName) 
				&& !alreadyEncrypt(localBook.source)){
			try {
				localBook.source = SecretKeyUtil.encrypt(String.valueOf(localBook.book_id), localBook.userName, localBook.source);
			} catch (UnsatisfiedLinkError e) {
				e.printStackTrace();
			}
			values.put("source", localBook.source);
		}else
			values.put("source", localBook.source);

		if (localBook.source.equals(LocalBook.SOURCE_ONLINE_BOOK) && !TextUtils.isEmpty(localBook.cert)  && !localBook.cert.startsWith(ENCRYPT_PREFIX)) {
			values.put("cert",ENCRYPT_PREFIX + OlineDesUtils.encrypt(localBook.cert));
		} else if (!TextUtils.isEmpty(localBook.cert)) {
			values.put("cert", localBook.cert);
		}
		if (!TextUtils.isEmpty(localBook.random)) {
			values.put("random", localBook.random);
		}
		values.put("type_id", localBook.type_id);
		values.put("progress", localBook.progress);
		values.put("state_load", localBook.state);
		values.put("book_state", localBook.bookState);
		values.put("mod_time", localBook.mod_time);
		values.put("add_time", localBook.add_time);
		values.put("access_time", localBook.access_time);
		values.put("hot_exp", localBook.hot_exp);
		values.put("book_path", localBook.book_path);
		values.put("big_image_url", localBook.bigImageUrl);
		values.put("small_image_url", localBook.bigImageUrl);
		values.put("big_image_path", localBook.bigImagePath);
		values.put("small_image_path", localBook.smallImagePath);
		values.put("package_name", localBook.packageName);
		values.put("dir", localBook.dir);
		values.put("version", localBook.version);
		values.put("operating_state", localBook.operatingState);
		values.put("note_operating_state", localBook.operatingNoteState);
		values.put("note_version", localBook.bookNoteVersion);
		values.put("borrow_end_time", localBook.borrowEndTime);
		if (localBook.bookMarks != null) {
			// values.put("book_marks",
			// StreamToolBox.getBytes(localBook.book_marks));
		}
		if (localBook.read_progress != null) {
			values.put("read_progress", localBook.read_progress.getBytes());
		}
		if (localBook.boot != null) {
			values.put("boot", localBook.boot.getBytes());
		}
		if (localBook.giftBookInfor != null) {
			values.put("gift_book_infor",StreamToolBox.getBytes(localBook.giftBookInfor));
		}
		if (localBook.tempBookmark != null) {
			values.put("temp_bookmark",StreamToolBox.getBytes(localBook.tempBookmark));
		}
		String categoryTemp = localBook.category;
		if (localBook.category != null) {
			if (localBook.category.equals(BookCategory.DEFAULT_CATEGORY)
					|| localBook.category.equals(BookCategory.UNCLASSIFIED_CATEGORY)) {
				localBook.category = "";
			}
			values.put("category", localBook.category);
		}
		// 增加的图书格式字段。
		if (localBook.format > 0) {
			values.put("format", localBook.format);
		}
		if (!TextUtils.isEmpty(localBook.formatName)) {
			values.put("format_name", localBook.formatName);
		}
		if (!TextUtils.isEmpty(localBook.cadrNum)) {
			values.put("card_num", localBook.cadrNum);
		}
		if (!TextUtils.isEmpty(localBook.deviceId)) {// 设备id的二次加密。
			String deviceId = "";
			try {
				deviceId = NativeWrapper.aseEncrypt(localBook.deviceId,localBook.book_id + "");
			} catch (UnsatisfiedLinkError e) {// 会报异常。
				deviceId = "";
			}
			// String deviceId=localBook.deviceId;
			values.put("device_id", deviceId);
		}
		if ((mContentResolver.update(uri, values, "book_id" + "=" + "'"+ localBook.book_id + "' AND user_name ='"+LoginUser.getpin()+"'", null)) == 0) {
			mContentResolver.insert(uri, values);
		}
		localBook.category = categoryTemp == null ? "" : categoryTemp;
		boolean isSuccess = false;
		if (uri != null) {
			isSuccess = true;
		} else {
			isSuccess = false;
		}
		mContentResolver.notifyChange(uri, null);
		return isSuccess;
	}

	@Override
	public boolean save() {
		return saveLocalBook(MZBookApplication.getInstance().getContentResolver(), this);
	}

	/**
	 * 保存引导文件
	 * @return
	 */
	public boolean saveBoot() {
		boolean isSuccess = false;
		ContentValues values = new ContentValues();
		if (boot != null) {
			values.put("boot", boot.getBytes());
			isSuccess = save(values, book_id);
		}
		return isSuccess;
	}

	public float getReadProgressValue() {
		if (read_progress == null) {
			return 0;
		}
		return read_progress.offSet;
	}

	public static ReadProgress getReadProgress(long id) {

		Log.i("read_progress", String.valueOf(id));
		LocalBook localBook = null;
		String[] projection = new String[] { "read_progress" };
		String selection = null;
		Cursor cur;
		// if (id >= 0) {
		selection = "id" + " = " + "'" + id + "'";
		// }
		String sortOrder = "mod_time" + " ASC";

		cur = MZBookApplication.getInstance().getContentResolver()
				.query(uri, projection, selection, null, sortOrder);// " DESC");

		if (cur == null || cur.getCount() == 0) {// 如果返回的数目为0，也return。@zhangmurui
													// 12/9/28.
			Log.i("", "cur==null");
			return null;
		}
		while (!cur.isAfterLast()) {
			if (cur.isBeforeFirst()) {
				cur.moveToNext();
			} else {
				localBook = new LocalBook();
				byte[] bytes = cur.getBlob(0);
				if (bytes != null && bytes.length > 0) {
					localBook.read_progress = getReadProgress(bytes);
					if (localBook.read_progress != null) {
						Log.i("read_progress",
								String.valueOf(localBook.read_progress.offSet));
					}
				}
				cur.moveToNext();
				break;
			}
		}
		cur.close();
		return localBook.read_progress;
	}

	public boolean saveRandom() {
		Log.i("LocalBook", "random=====" + random);
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("user_name", userName);
		values.put("random", random);
		// String uri = null;
		if ((mContentResolver.update(URI_RANDOM, values, "user_name" + "="
				+ "'" + userName + "'", null)) == 0) {
			mContentResolver.insert(URI_RANDOM, values);
		}
		boolean isSuccess = true;
		// if (uri != null) {
		// isSuccess = true;
		// } else {
		// isSuccess = false;
		// }
		// mContentResolver.notifyChange(URI_RANDOM, null);
		return isSuccess;
	}

	public String readRandomFromDB() {
		if (!TextUtils.isEmpty(this.random)) {// 如果图书表中没有存随机数，就从random表取。
			return this.random;
		}
		String[] projection = new String[] { "random" };
		String selection = null;
		Cursor cur;
		String random = null;
		Log.i("LocalBook", "userName=====" + userName);
		if (TextUtils.isEmpty(userName)) {
			random = "";
			return random;
		} else {
			selection = "user_name" + " = " + "'" + userName + "'";
		}
		cur = MZBookApplication.getInstance().getContentResolver()
				.query(URI_RANDOM, projection, selection, null, null);// " DESC");
		if (cur == null) {
			Log.i("", "cur==null");
			return null;
		}
		while (!cur.isAfterLast()) {
			if (cur.isBeforeFirst()) {
				cur.moveToNext();
			} else {
				random = cur.getString(0);
				cur.moveToNext();
				break;
			}
		}
		cur.close();
		if (random == null) {
			random = "";
		}
		Log.i("LocalBook", "random=====" + random);
		return random;
	}

	public static boolean saveCardInBook(Long bookId, String card_num) {
		ContentValues values = new ContentValues();
		values.put("card_num", card_num);
		boolean isSuccess = save(values, bookId);
		return isSuccess;
	}
	
	public static boolean saveCardInBookByIndex(int index, String card_num) {
		ContentValues values = new ContentValues();
		values.put("card_num", card_num);
		boolean isSuccess = saveByIndex(values, index);
		return isSuccess;
	}

	@Override
	public boolean saveState() {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		ContentValues values = new ContentValues();
		LocalBook localBook = this;
		values.put("state_load", localBook.state);
		if ((mContentResolver.update(uri, values,"book_id" + "=" + "'"
				+ localBook.book_id + "' AND user_name ='"+LoginUser.getpin()+"'", null)) == 0) {
			mContentResolver.insert(uri, values);
		}
		boolean isSuccess = false;
		if (uri != null) {
			isSuccess = true;
		} else {
			isSuccess = false;
		}
		mContentResolver.notifyChange(uri, null);
		return isSuccess;
	}

	public boolean saveBookState() {
		ContentValues values = new ContentValues();
		values.put("book_state", bookState);
		boolean isSuccess = save(values, book_id);
		return isSuccess;
	}

	public static boolean saveMyBookmark(MyBookmark tempBookmark, long bookId) {
		ContentValues values = new ContentValues();
		if (tempBookmark == null) {
			values.putNull("temp_bookmark");
		} else {
			values.put("temp_bookmark", tempBookmark.getBytes());
		}

		boolean isSuccess = save(values, bookId);
		return isSuccess;
	}

	public static boolean saveVersion(long version, long bookId) {
		ContentValues values = new ContentValues();
		values.put("version", version);
		boolean isSuccess = save(values, bookId);
		return isSuccess;
	}

	public static boolean saveNoteVersion(long version, long bookId) {
		ContentValues values = new ContentValues();
		values.put("note_version", version);
		boolean isSuccess = save(values, bookId);
		return isSuccess;
	}

	public boolean saveProgress() {
		ContentValues values = new ContentValues();
		values.put("progress", progress);
		boolean isSuccess = save(values, book_id);
		return isSuccess;
	}

	public static boolean saveOperatingState(int operatingState, long id) {
		ContentValues values = new ContentValues();
		values.put("operating_state", operatingState);
		boolean isSuccess = save(values, id);
		return isSuccess;
	}

	public static boolean saveNoteOperatingState(int operatingState, long id) {
		ContentValues values = new ContentValues();
		values.put("note_operating_state", operatingState);
		boolean isSuccess = save(values, id);
		return isSuccess;
	}

	public static boolean saveBookState(String packageName, int bookState) {
		ContentValues values = new ContentValues();
		values.put("book_state", bookState);
		LocalBook localBook = getLocalBookByPackName(packageName);
		boolean isSuccess = false;
		if (localBook != null) {
			localBook.bookState = bookState;
			isSuccess = localBook.saveBookState();
		}
		return isSuccess;
	}

	public static boolean save(ContentValues values, long id) {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		// LocalBook localBook = this;
		int num = -1;
		Uri mUri = null;
		if ((num = mContentResolver.update(uri, values, "book_id" + "=" + "'"
				+ id + "'", null)) == 0) {
			mUri = mContentResolver.insert(uri, values);
		}
		boolean isSuccess = false;
		if (num > 0 || mUri != null) {
			isSuccess = true;
		} else {
			isSuccess = false;
		}
		mContentResolver.notifyChange(uri, null);
		return isSuccess;
	}
	
	public static boolean saveByIndex(ContentValues values, int index) {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		int num = -1;
		Uri mUri = null;
		if ((num = mContentResolver.update(uri, values, "_id" + "=" + "'"
				+ index + "'", null)) == 0) {
			mUri = mContentResolver.insert(uri, values);
		}
		boolean isSuccess = false;
		if (num > 0 || mUri != null) {
			isSuccess = true;
		} else {
			isSuccess = false;
		}
		mContentResolver.notifyChange(uri, null);
		return isSuccess;
	}
	
	

	public boolean saveModTime() {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		ContentValues values = new ContentValues();
		LocalBook localBook = this;
		values.put("mod_time", localBook.mod_time);
		if ((mContentResolver.update(uri, values, "book_id" + "=" + "'"
				+ localBook.book_id + "'", null)) == 0) {
			mContentResolver.insert(uri, values);
		}
		boolean isSuccess = false;
		if (uri != null) {
			isSuccess = true;
		} else {
			isSuccess = false;
		}
		mContentResolver.notifyChange(uri, null);
		return isSuccess;
	}

	public boolean saveReadProgress() {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		ContentValues values = new ContentValues();
		LocalBook localBook = this;
		if (localBook.read_progress != null) {
			values.put("read_progress", localBook.read_progress.getBytes());
			// StreamToolBox.getBytes(localBook.read_progress));
		}
		Log.i("read_progress", String.valueOf(localBook.book_id));
		if ((mContentResolver.update(uri, values, "book_id" + "=" + "'"
				+ localBook.book_id + "' AND user_name ='"+LoginUser.getpin()+"'", null)) == 0) {
			mContentResolver.insert(uri, values);
		}
		boolean isSuccess = false;
		if (uri != null) {
			isSuccess = true;
		} else {
			isSuccess = false;
		}
		mContentResolver.notifyChange(uri, null);
		return isSuccess;
	}

	public void start(final Activity myActivity) {

		if (boot != null) {
			Intent intent = new Intent(myActivity, DownloadService.class);
			intent.putExtra(KEY, book_id);
			myActivity.startService(intent);
			return;
		}

		String url = "http://"
				+ Configuration.getProperty(Configuration.DBOOK_HOST).trim()
				+ "/client.action";

		boolean isborrow =source.equals(LocalBook.SOURCE_BORROWED_BOOK)?true:false;
		boolean isBorrowBuy = source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK) ? true : false;
		
		if(source.equals(LocalBook.SOURCE_TRYREAD_BOOK))
		{
			Intent intent = new Intent(myActivity,
					DownloadService.class);
			intent.putExtra(KEY, book_id);
			myActivity.startService(intent);
			return;
		}
		System.out.println("BBBBBBB====@@@@@==start==="+url);		
		WebRequestHelper.post(
				url,
				RequestParamsPool.getBookverifyParams(LoginUser.getpin(), ""
						+ book_id, "" + order_code,isborrow, isBorrowBuy), true,
				new MyAsyncHttpResponseHandler(myActivity) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(myActivity, "下载失败", Toast.LENGTH_SHORT)
								.show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						BootEntity boot = null;
						try {

							DocumentBuilderFactory factory = DocumentBuilderFactory
									.newInstance();
							DocumentBuilder builder = factory
									.newDocumentBuilder();
							byte[] bytes = responseBody;
							String xml = StreamToolBox
									.loadStringFromStream(StreamToolBox
											.getByteArrayInputStream(bytes));
							
							MZLog.d("wangguodong", "boot 文件："+xml);
							
							Document dom = builder.parse(StreamToolBox
									.getByteArrayInputStream(bytes), "UTF-8");
							Element root = dom.getDocumentElement();

							boot = BootEntity.parser(root);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (boot == null) {
					
							MZLog.d("LocalBook", "解析失败！");
							ToastUtil.showToastInThread("下载请求失败...", Toast.LENGTH_LONG);
							
						} else {
							Log.i("wangguodong", "解析成功！");
							final LocalBook localBook = new LocalBook();
							localBook.book_id = book_id;
							localBook.boot = boot;
							boolean b = localBook.saveBoot();
							if (b) {
								Log.i("wangguodong", "存储成功！");
							} else {
								Log.i("wangguodong", "存储失败！");
							}

							Intent intent = new Intent(myActivity,DownloadService.class);
							intent.putExtra(KEY, localBook.book_id);
							myActivity.startService(intent);

						}

					}

				});

	}


	@Override
	public int compareTo(LocalBook another) {
		if (_priority > another._priority) {
			return 1;
		} else if (_priority < another._priority) {
			return -1;
		}
		return 0;

	}

	public static JSONObject localBookMark2Json(LocalBook localBook,
			boolean isForced, boolean isOpenBook) {
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
						myBookmark.getOffset() + 1);// 服务端书签需要+1才行，因为服务端书签起始点是从1开始的（pc和iphone提交的数据是这样），android端是从0开始的。
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
				if (isForced) {
					bookMarkObject.put("force", "2");
				} else {
					bookMarkObject.put("force", "1");
				}

				if (!isOpenBook) {
					list.put(bookMarkObject);
				} else if (myBookmark.getDataType() != MyBookmark.DATATYPE_LAST_POS) {
					list.put(bookMarkObject);
				}
			}
			bookMarkBuildObj.put(DataParser.KEY_LIST, list);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bookMarkBuildObj;

	}

	/**
	 * 将笔记数据转为json对象。
	 * 
	 * @param localBook
	 * @param isForced
	 * @return
	 */
	public static JSONObject localBookNote2Json(LocalBook localBook,
			boolean isForced, boolean isOpenBook) {
		// 解析LocalBook实体和LocalBook的笔记为json对象
		JSONObject bookNoteBuildObj = new JSONObject();
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
			bookNoteBuildObj.put("orderId", orderid);
			bookNoteBuildObj.put("ebookId", localBook.book_id);
			bookNoteBuildObj.put("version", localBook.bookNoteVersion);
			bookNoteBuildObj.put(MyBookmark.KEY_EBOOK_TYPE, localBook.format);
			bookNoteBuildObj.put("userId", localBook.userName);
			ArrayList<BookNote> booknoteList = localBook.bookNotes;
			JSONArray list = new JSONArray();
			for (BookNote bookNote : booknoteList) {
				JSONObject bookNoteObject = new JSONObject();
				bookNoteObject.put(MyBookNote.KEY_ID, bookNote.getServicedId());
				bookNoteObject.put(MyBookmark.KEY_DATA_TYPE,
						MyBookNote.TYPE_DATA_NOTE);
				bookNoteObject.put(MyBookNote.KEY_FORMATE,
						bookNote.getBookFormate());
				bookNoteObject.put(MyBookNote.KEY_OFFSET,
						bookNote.getStartOffset());
				bookNoteObject.put(MyBookNote.KEY_TOTALOFFFSET,
						bookNote.getEndOffset());
				bookNoteObject.put(MyBookNote.KEY_X1,
						bookNote.getStart_paragraph_index());
				bookNoteObject.put(MyBookNote.KEY_Y1,
						bookNote.getStart_element_index());
				bookNoteObject.put(MyBookNote.KEY_Z1,
						bookNote.getStart_char_index());
				bookNoteObject.put(MyBookNote.KEY_X2,
						bookNote.getEnd_paragraph_index());
				bookNoteObject.put(MyBookNote.KEY_Y2,
						bookNote.getEnd_element_index());
				bookNoteObject.put(MyBookNote.KEY_Z2,
						bookNote.getEnd_char_index());
				bookNoteObject.put(MyBookNote.KEY_NOTE, bookNote.getContent());
				bookNoteObject.put(MyBookNote.KEY_REMARK,
						bookNote.getRemarkContent());
				bookNoteObject.put(MyBookNote.KEY_COLOR, bookNote.getColor());
				bookNoteObject.put(MyBookNote.KEY_DEVICE_TIME,
						bookNote.getClientTime());
				bookNoteObject.put(MyBookNote.KEY_VERSION,
						bookNote.getLastModifyTime());
				int valid = 1;
				if (bookNote.getModifyState() == MyBookNote.STATE_DEL) {
					valid = 0;
				} else {
					valid = 1;
				}
				bookNoteObject.put(MyBookmark.KEY_VALID, valid);
				// list.put(bookNoteObject);
				// if(isForced){
				bookNoteObject.put("force", "2");
				// }else {
				// bookNoteObject.put("force", "1");
				// }
				if (!isOpenBook) {// 如果是打开就不上传数据，只是获取服务端最新的数据。
					list.put(bookNoteObject);
				}
			}
			bookNoteBuildObj.put(DataParser.KEY_LIST, list);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bookNoteBuildObj;

	}

	public int getCopyCount() {
		String[] projection = new String[] { "copy_count" };
		String selection = "id=?";
		String[] selectionArgs = new String[] { Long.toString(book_id) };
		Cursor cur = MZBookApplication
				.getInstance()
				.getContentResolver()
				.query(DataProvider.CONTENT_URI_NAME_BOOKCOPYCOUNT, projection,
						selection, selectionArgs, null);
		if (cur != null && cur.moveToNext()) {
			return cur.getInt(0);
		}
		return 0;
	}

	public static String getUserPin(long bookId) {
		String[] projection = new String[] { "user_name" };
		String selection = "book_id=?";
		String[] selectionArgs = new String[] { Long.toString(bookId) };
		Cursor cur = MZBookApplication
				.getInstance()
				.getContentResolver()
				.query(DataProvider.CONTENT_URI_EBOOK, projection, selection,
						selectionArgs, null);
		if (cur != null && cur.moveToNext()) {
			return cur.getString(0);
		}
		cur.close();
		return "";
	}

	public void saveCopyCount(int copy_count) {
		ContentValues values = new ContentValues();
		values.put("copy_count", copy_count);
		String where = "id=?";
		String[] selectionArgs = new String[] { Long.toString(book_id) };
		if (MZBookApplication
				.getInstance()
				.getContentResolver()
				.update(DataProvider.CONTENT_URI_NAME_BOOKCOPYCOUNT, values,
						where, selectionArgs) == 0) {
			values.put("id", book_id);
			MZBookApplication
					.getInstance()
					.getContentResolver()
					.insert(DataProvider.CONTENT_URI_NAME_BOOKCOPYCOUNT, values);
		}
	}


	public RequestEntry getRequestEntry() {
		return requestEntry;
	}

	@Override
	public void setRequestEntry(RequestEntry requestEntry) {
		this.requestEntry = requestEntry;
		requestCode = (int) System.currentTimeMillis();
		requestEntry.setRequestCode(requestCode);
	}

	public static List<LocalBook> getLocalBookList(String selection,String[] selectionArgs) {
		return getLocalBookList(ALL_PROJECTION, selection, selectionArgs);
	}
	public static LocalBook getLocalBook(ContentResolver mContentResolver,long id) {
		return getLocalBook("book_id=?", new String[] { Long.toString(id) });
	}

	public static LocalBook getLocalBook(long id) {

		LocalBook localBook = getLocalBook("book_id=?",new String[] { Long.toString(id) });
		if (localBook != null) {
			localBook.random = localBook.readRandomFromDB();
			MZLog.d("filepath", localBook.getFilePath());
		}
		return localBook;
	}
	
	public static LocalBook getLocalBookByIndex(int id) {
		LocalBook localBook = getLocalBook("_id=?",new String[] { Long.toString(id) });
		return localBook;
	}
	
	@Override
	public int getDownloadStatus() {
		// TODO Auto-generated method stub
		return state;
	}

	@Override
	public long getId() {
		return book_id;
	}

	@Override
	public void setDownloadStatus(int state) {
		this.state = state;

	}

	@Override
	public boolean isTryGetContetnFromSD() {
		// TODO Auto-generated method stub
		return tryGetContetnFromSD;
	}

	@Override
	public void setTryGetContetnFromSD(boolean isTryGetContetnFromSD) {
		// TODO Auto-generated method stub
		this.tryGetContetnFromSD = isTryGetContetnFromSD;
	}

	@Override
	public boolean isManualStop() {
		// TODO Auto-generated method stub
		return isMenualStop;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return DownloadedAble.TYPE_BOOK;
	}

	@Override
	public String getUrl() {
		// TODO Auto-generated method stub
		return bookUrl;
	}

	@Override
	public String getFilePath() {
		// TODO Auto-generated method stub
		return bigImagePath;
	}

	@Override
	public void saveLoadTime(long time) {
		mod_time = time;
	}

	@Override
	public void setCurrentSize(long progress) {
		// TODO Auto-generated method stub
		this.progress = progress;
	}

	@Override
	public void setTotalSize(long size) {
		// TODO Auto-generated method stub
		this.size = size;
	}

	@Override
	public long getTotalSize() {
		// TODO Auto-generated method stub
		return size;
	}

	@Override
	public long getCurrentSize() {
		// TODO Auto-generated method stub
		return progress;
	}

	@Override
	public FileGuider creatFileGuider() {
		// TODO Auto-generated method stub
		return null;
	}

	//
	// @Override
	// public int compareTo(LocalBook arg0) {
	// // TODO Auto-generated method stub
	// return 0;
	// }

	@Override
	public boolean isBelongPagCode(int code) {
		return belongPagCode == code;
	}

	@Override
	public String getImageUrl() {
		// TODO Auto-generated method stub
		return bigImageUrl;
	}

	@Override
	public String getHomeImageUrl() {
		// TODO Auto-generated method stub
		return bigImageUrl;
	}

	//
	// @Override
	// public String getImagePath(){
	// return null;
	// }
	// @Override
	// public boolean save() {
	// // TODO Auto-generated method stub
	// return false;
	// }

	@Override
	public DownloadedAble getCopy() {
		return copy;
	}

	@Override
	public void setCopy(DownloadedAble downloadedAble) {
		if (downloadedAble instanceof LocalBook) {
			this.copy = (LocalBook) downloadedAble;
		} else {
			this.copy = null;
		}
	}

	public SubBook getSubBook() {
		SubBook subBook = new SubBook();
		subBook.id = this.book_id;
		subBook.state = this.state;
		subBook.progress = this.progress;
		subBook.size = this.size;
		subBook.book_path = this.book_path;
		subBook.orderId = this.order_code;
		subBook.localBook = this;
		return subBook;
	}

	public static HashMap<Long, SubBook> getSubBookList() {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		HashMap<Long, SubBook> list = new HashMap<Long, SubBook>();
		Cursor cur;
		String sortOrder = "mod_time" + " ASC";
		String[] projection = { "book_id", "state_load", "progress", "size",
				"book_path", "source", "order_code" };
		// String selection = "source=?";
		cur = mContentResolver.query(DataProvider.CONTENT_URI_EBOOK,
				projection, null, null, sortOrder);// " DESC");
		if (cur == null) {
			Log.i("", "cur==null");
			return list;
		}
		for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			SubBook subBook = new SubBook();
			int i = -1;
			subBook.id = cur.getLong(++i);
			subBook.state = cur.getInt(++i);
			subBook.progress = cur.getLong(++i);
			subBook.size = cur.getLong(++i);
			subBook.book_path = cur.getString(++i);
			subBook.source = cur.getString(++i);
			subBook.orderId = cur.getString(++i);
			list.put(subBook.id, subBook);
		}
		cur.close();
		return list;
	}

	public static SubBook getSubBook(long id) {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		Cursor cur;
		String sortOrder = "mod_time" + " ASC";
		String[] projection = { "book_id", "state_load", "progress", "size",
				"dir", "source", "order_code" };
		// String selection = "source=?";
		cur = mContentResolver.query(DataProvider.CONTENT_URI_EBOOK,
				projection, "book_id=?", new String[] { Long.toString(id) },
				sortOrder);// " DESC");
		if (cur == null || cur.getCount() <= 0) {
			Log.i("", "cur==null");
			return null;
		}
		cur.moveToFirst();
		SubBook subBook = new SubBook();
		int i = -1;
		subBook.id = cur.getLong(++i);
		subBook.state = cur.getInt(++i);
		subBook.progress = cur.getLong(++i);
		subBook.size = cur.getLong(++i);
		subBook.book_path = cur.getString(++i);
		subBook.source = cur.getString(++i);
		subBook.orderId = cur.getString(++i);
		cur.close();
		return subBook;
	}
	
	public HttpSetting getHttpSetting() {
		return httpSetting;
	}

	public void setHttpSetting(HttpSetting httpSetting) {
		this.httpSetting = httpSetting;
	}

	public byte[] getBookmarksBytes() {
		return StreamToolBox.getBytes(bookMarks);
	}

	@Override
	public int getRequestCode() {
		return requestCode;
	}

	@Override
	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}

	public static ReadProgress getReadProgress(byte[] bytes) {
		return ReadProgress.loadSettingFromFile(bytes);
	}

	public static class SubBook {
		public long id;
		public int state;
		public long progress;
		public long size;
		public String book_path = "";
		public String source = "";
		public String orderId;
		public LocalBook localBook;
	}

	@Override
	public int hashCode() {
		return (int) book_id;
	}

	
	public static boolean alreadyEncrypt(String source){
		
		if(!TextUtils.isEmpty(source) && !source.equals(LocalBook.SOURCE_BULIT_IN) && !source.equals(LocalBook.SOURCE_BORROWED_BOOK)
				 && !source.equals(LocalBook.SOURCE_BUYED_BOOK) && !source.equals(LocalBook.SOURCE_ONLINE_BOOK)
				 && !source.equals(LocalBook.SOURCE_TRYREAD_BOOK) && !source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK))
			return true;
		
		return false;
	}
}
