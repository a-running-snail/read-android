package com.jingdong.app.reader.data.db;

import java.util.List;

import com.android.mzbook.sortview.model.BookShelfModel;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.SecretKeyUtil;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

public class DataProvider extends ContentProvider {

	public static final String DB_NAME = "book.db";
	public static final int DB_VESION = 35;

	public static final String TABLE_NAME_BOOKINFOR = "BookInforTable";
	
	public static final String TABLE_NAME_EBOOK = "ebook";
	
	public static final String TABLE_NAME_BOOKCART = "BookCartTable";
	public static final String TABLE_NAME_RANDOM = "RandomTable";
	public static final String TABLE_NAME_BOOKMARK = "BookMarkTable";
	public static final String TABLE_NAME_BOOKCATEGORY = "BookCategoryTable";
	public static final String TABLE_NAME_PLUGIN_TABLE = "PluinsConfigTable";
	public static final String TABLE_NAME_OLINE_CARD = "OlineCardTable";
	public static final String TABLE_NAME_BOOKNOTE = "BookNoteTable";

	// 图书复制次数限制
	private static final String TABEL_NAME_COPYCOUNT = "BookCopyCountTable";

	private static final String TABLE_NAME_OLD_BOOKMARK = "Bookmarks";
	public static final int TYPE_POS_OLD = 100;// 手动书签位置老的标示
	public static final int TYPE_LAST_POS_OLD = 200;// 系统书签位置老的标示
	public static final int VERSION_OLD_BOOKMARK_1 = 3;// 更改表名
	public static final int VERSION_OLD_BOOKMARK_2 = 18;// 更改结构，增加和修改一些字段，支持云书签
	public static final int VERSION_OLD_CAR_1 = 21;// 更改购物数据库结构，去掉去掉实体体购物车数据
	public static final int VERSION_OLD_BOOKINFO = 23;// 更改图书的格式属性。
	public static final int VERSION_OLD_CATEGORY = 25;// 为分类表增加一个分类下图书数量的字段。
	public static Uri CONTENT_URI_BOOKINFOR = null;
	public static Uri CONTENT_URI_EBOOK = null;
	public static Uri CONTENT_URI_DOCUMENT = null;
	public static Uri CONTENT_URI_BOOKCART = null;
	public static Uri CONTENT_URI_NAME_RANDOM = null;
	public static Uri CONTENT_URI_NAME_BOOKMARK = null;
	public static Uri CONTENT_URI_NAME_BOOKCATEGORY = null;
	public static Uri CONTENT_URI_NAME_BOOKCOPYCOUNT = null;
	public static Uri CONTENT_URI_NAME_PLUGINS = null;
	public static Uri CONTENT_URI_NAME_OLINE_CARD = null;
	public static Uri CONTENT_URI_NAME_MYBOOKNOTE = null;
	private static final int _EBOOK = 1;
	private static final int _DOCUMENT = 2;
	private static final int BOOKCART = 3;
	private static final int RANDOM = 4;
	private static final int BOOKMARK = 5;
	private static final int BOOKCATEGORY = 6;
	private static final int BOOKCOPYCOUNT = 7;
	private static final int PLUGIN_URI_CODE = 8;
	private static final int OLINE_CARD = 9;
	private static final int BOOKNOTE = 10;
	private static UriMatcher sUriMatcher;

	// 2014-12-27 添加拇指数据库开始

	public static final String EPURCHASE = "epurchase";
	public static final String EELOCAL = "eelocal";
	public static final String EBOOK = "ebook";
	public static final String BOOKNAME = "bookname";
	public static final String _ID = "_id";
	public static final String USERID = "userid";
	public static final String EBOOKID = "ebookid";
	public static final String BOOKID = "bookid";
	public static final String PURCHASE_AT = "purchase_at";
	public static final String ENTITYID = "entityid";
	public static final String EDITION = "edition";
	public static final String ARCHIVED = "archived";
	public static final String NAME = "name";
	public static final String PRICE = "price";
	public static final String TITLE = "title";
	public static final String AUTHOR = "author";
	public static final String DESC = "desc";
	public static final String COVER_URL = "cover_url";
	public static final String PROGRESS = "progress";
	public static final String PERCENT = "percent";
	public static final String PARA_IDX = "para_idx";
	public static final String OFFSET_IN_PARA = "offset_in_para";
	public static final String UPDATE_TIME = "update_time";
	public static final String DOCUMENT_ID = "document_id";
	public static final String CHAPTER_ITEMREF = "chapter_itemref";
	public static final String DOCUMENT = "document";
	public static final String COVER_PATH = "cover_path";
	public static final String ADD_AT = "add_at";
	public static final String OPF_MD5 = "opf_md5";
	public static final String DOCBIND = "docbind";
	public static final String SERVER_ID = "server_id";
	public static final String BIND = "bind";
	public static final String EBOOKNOTE = "ebooknote";
	public static final String NOTESYNC = "notesync";
	public static final String SERVER_NOTE_ID = "server_note_id";
	public static final String CHAPTER_NAME = "chapter_name";
	public static final String START_PARA_IDX = "start_para_idx";
	public static final String START_OFFSET_IN_PARA = "start_offset_in_para";
	public static final String END_PARA_IDX = "end_para_idx";
	public static final String END_OFFSET_IN_PARA = "end_offset_in_para";
	public static final String QUOTE = "quote";
	public static final String CONTENT = "content";
	public static final String IS_PRIVATE = "is_private";
	public static final String MODIFIED = "modified";
	public static final String DELETED = "deleted";
	public static final String PDF_PAGE = "pdf_page";
	public static final String PDF_YOFFSET = "pdf_yoffset";
	public static final String PDF_XOFFSET = "pdf_xoffset";
	public static final String LAST_UPDATE_TIME = "last_update_time";
	public static final String READ_AT = "readAt";
	public static final String READINGDATA = "readingdata";
	public static final String RD_ID = "_id";
	public static final String RD_EBOOKID = "ebook_id";
	public static final String RD_DOCUMENTID = "document_id";
	public static final String RD_STARTTIME = "start_time";
	public static final String RD_CHAPTER = "start_chapter";
	public static final String RD_PARAIDX = "start_para_idx";
	public static final String RD_PDF_PAGE = "start_pdf_page";
	public static final String RD_ENDTIME = "end_time";
	public static final String RD_ENDCHAPTER = "end_chapter";
	public static final String RD_ENDPARAIDX = "end_para_idx";
	public static final String RD_END_PDF_PAGE = "end_pdf_page";
	public static final String RD_LENGTH = "length";
	public static final String RD_SIGN = "sign";
	public static final String MENTION = "mention";
	public static final String ME_ID = "_id";
	public static final String ME_BOOK_ID = "mention_book_id";
	public static final String ME_BOOK_NAME = "mention_book_name";
	public static final String ME_BOOK_TIME = "mention_time";
//	public static final String ME_BOOK_AUTHOR = "mention_book_author";
//	public static final String ME_BOOK_COVER = "mention_book_cover";
	public static final String BOOKPAGE = "bookpage";
	public static final String BOOK_CHAPTER_PAGE = "book_chapter_page";
	public static final String BOOK_CHAPTER_BLOCK = "book_chapter_block";
	public static final String BOOK_TEXT_SIZE = "book_text_size";
	public static final String PAGECONTENT = "pagecontent";
	public static final String PAGE_START_PARA = "page_start_para";
	public static final String PAGE_START_OFFSET = "page_start_offset";
	public static final String BOOK_FONT_FACE = "book_font_face";
	public static final String BOOK_LINE_SPACE = "book_line_space";
	public static final String BOOK_BLOCK_SPACE = "book_block_space";
	public static final String BOOK_PAGE_EDGE_SPACE = "book_page_edge_space";
	public static final String BOOK_SCREEN_MODE = "book_screen_mode";

	public static final String SPLASH = "splash";
	public static final String SPLASH_ID = "splash_id";
	public static final String SPLASH_PIC = "splash_pic";
	public static final String SPLASH_PERCENT = "splash_percent";
	public static final String SPLASH_SAYING = "splash_saying";
	public static final String SPLASH_PERCENT_WIDTH = "splash_percent_width";

	public static final String FOLDER = "folder";
	public static final String FOLDER_CONTAINER = "folder_container";
	public static final String BOOKSHELF = "bookshelf";

	public static final String FOLDER_NAME = "folder_name";
	public static final String FOLDER_CHANGETIME = "folder_changetime";
	public static final String FOLDER_ID = "folder_id";

	public static final String FOLDER_CONTAINER_FOLDER_ID = "folder_dir_id";

	public static final String BOOKSHELF_ID = "id";
	public static final String BOOKSHELF_FOLDER_ID = "folder_id";
	public static final String BOOKSHELF_DOCUMENT_ID = "document_id";
	public static final String BOOKSHELF_EBOOK_ID = "ebook_id";
	public static final String BOOKSHELF_CHANGE_TIME = "change_time";

	public static final String BOOKMARK_TABLE = "bookmark";
	public static final String BOOKMARK_ID = "id";
	public static final String BOOKMARK_EBOOKID = "ebookid";
	public static final String BOOKMARK_DOCID = "docid";
	public static final String BOOKMARK_CHAPTER_TITLE = "chapter_title";
	public static final String BOOKMARK_CHAPTER_ITEMREF = "chapter_itemref";
	public static final String BOOKMARK_OFFSET_IN_PARA = "offset_in_para";
	public static final String BOOKMARK_PARA_INDEX = "para_index";
	public static final String BOOKMARK_DIGEST = "digest";
	public static final String BOOKMARK_UPDATE_AT = "updated_at";
	public static final String BOOKMARK_SERVERID = "server_id";
	public static final String BOOKMARK_USERID = "userid";
	public static final String BOOKMARK_PDF_PAGE = "pdf_page";

	public static final String BOOKMARKSYNC = "bookmarksync";
	public static final String BOOKMARKSYNC_ID = "id";
	public static final String BOOKMARKSYNC_USRID = "userid";
	public static final String BOOKMARKSYNC_EBOOKID = "ebookid";
	public static final String BOOKMARKSYNC_DOCID = "docid";
	public static final String BOOKMARKSYNC_LAST_UPDATE = "last_update_dt";

	public static final String TARGET_USER_ID = "target_user_id";
	public static final String TARGET_USER_NICKNAME = "target_user_nickname";
	public static final String TARGET_USER_AVATAR = "target_user_avatar";
	public static final String TARGET_USER_ROLE = "target_user_role";
	public static final String TARGET_USER_SUMMARY = "target_user_summary";
	public static final String TARGET_USER_NOTECOUNT = "target_user_notecount";
	public static final String TARGET_USER_PIN = "target_user_pin";
	public static final String TIMELINE_GUID = "timeline_guid";
	public static final String SHOW_ALL_NOTES = "show_all_notes";
	public static final String ANCHOR_LOCATION = "anchor_location";

	public static final String BOOK_TYPE = "book_type";
	public static final String CREATION_TIME = "creation_time";
	public static final String OPERATION_STATE = "operating_state";
	public static final String CHAPTER_TITLE = "chapter_title";

	public static final String PDF_ZOOM = "pdf_zoom";
	public static final String PDF_X_OFFSET_PERCENT = "pdf_x_offset_percent";
	public static final String PDF_Y_OFFSET_PERCENT = "pdf_y_offset_percent";
	public static final String IS_SYNC = "is_sync";
	public static final String PDF_ORIENTATION = "pdf_orientation";
	
	public static final String BOOK_PATH = "book_path";
	
	public static final String _INDEX = "_index";
	
	public static final String SIZE = "size";
	public static final String DOCUMENT_PROGRESS = "progress";
	public static final String STATE_LOAD = "state_load";
	public static final String BOOK_STATE = "book_state";
	public static final String ACCESS_TIME = "access_time";
	public static final String MOD_TIME = "mod_time";
	public static final String FROM_CLOUDDISK = "fromCloudDisk";
	public static final String BOOK_SOURCE = "book_source";
	public static final String BORROW_END_TIME = "borrow_end_time";
	
	public static final String TEMP_ID = "temp_id";//用于数据迁移时id不一致转换，无其他作用
	
	
	

	// 2014-12-27 添加拇指数据库结束

	public static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VESION);
		}

		static final String SQL_CREATE_TABLE_BOOKINFOR = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME_BOOKINFOR
				+ " ("
				+ "id"// book id
				+ " Long PRIMARY KEY NOT NULL,"
				+ "order_code"// order id
				+ " TEXT,"
				+ "product_code"
				+ " TEXT,"
				+ "size"
				+ " Long,"
				+ "url"
				+ " TEXT,"
				+ "price"
				+ " Double,"
				+ "author"
				+ " TEXT,"
				+ "user_name"// user pin 对应拇指中的userid字段
				+ " TEXT,"
				+ "title"
				+ " TEXT,"
				+ "signature"
				+ " TEXT,"
				+ "source"
				+ " Integer,"
				+ "cert"
				+ " TEXT,"
				+ "random"
				+ " TEXT,"
				+ "type_id"
				+ " Integer,"
				+ "progress"
				+ " Long,"
				+ "state_load"
				+ " Integer,"
				+ "book_state"
				+ " Integer,"
				+ "mod_time"
				+ " Long,"
				+ "add_time"
				+ " Long,"
				+ "access_time"
				+ " Long,"
				+ "hot_exp"
				+ " Integer,"
				+ "book_path"
				+ " TEXT,"
				+ "book_marks"
				+ " Blob,"
				+ "read_progress"
				+ " Blob,"
				+ "boot"
				+ " Blob,"
				+ "gift_book_infor"
				+ " Blob,"
				+ "temp_bookmark"
				+ " Blob,"
				+ "big_image_url"
				+ " TEXT,"
				+ "small_image_url"
				+ " TEXT,"
				+ "big_image_path"
				+ " TEXT,"
				+ "small_image_path"
				+ " TEXT,"
				+ "package_name"
				+ " TEXT,"
				+ "dir"
				+ " TEXT,"
				+ "version"
				+ " LONG,"
				+ "operating_state"
				+ " TEXT,"
				+ "category"
				+ " TEXT DEFAULT '',"
				+ "format"
				+ " TEXT,"
				+ "format_name"
				+ " TEXT,"
				+ "card_num"
				+ " TEXT, "
				+ "device_id"
				+ " TEXT, "
				+ "note_version"
				+ " LONG,"
				+ "note_operating_state" + " Integer" + ");";

		private static final String SQL_CREATE_TABLE_BOOKCATEGORY = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME_BOOKCATEGORY
				+ " ("
				+ "id"
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "category"
				+ " TEXT,"
				+ "isActive" + " INTEGER," + "count" + " INTEGER" + ");";

		private static final String SQL_CREATE_TABLE_COPYCOUNT = "CREATE TABLE IF NOT EXISTS "
				+ TABEL_NAME_COPYCOUNT
				+ " ("
				+ "id"
				+ " Long PRIMARY KEY,"
				+ "copy_count" + " INTEGER" + ");";

		private static final String SQL_CREATE_TABLE_BOOKCAR = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME_BOOKCART
				+ " ("
				+ "id"
				+ " Long PRIMARY KEY,"
				+ "book_name"
				+ " TEXT,"
				+ "book_type"
				+ " Integer,"
				+ "addcar_time" + " LONG," + "num" + " Integer"

				+ ");";

		private static final String SQL_CREATE_TABLE_RANDOM = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME_RANDOM
				+ " ("
				+ "user_name"
				+ " TEXT PRIMARY KEY,"
				+ "random" + " TEXT"

				+ ");";
		private static final String SQL_CREATE_TABLE_BOOKMARK = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME_BOOKMARK
				+ " ("
				+ "client_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "server_id INTEGER,"
				+ "user TEXT,"
				+ "book_id LONG NOT NULL REFERENCES BookInforTable(id),"
				+ "order_id LONG  NOT NULL,"
				+ "data_type INTEGER  NOT NULL,"
				+ "book_type INTEGER  NOT NULL,"
				+ "paragraph_index INTEGER  NOT NULL,"
				+ "element_index INTEGER NOT NULL,"
				+ "char_index INTEGER NOT NULL,"
				+ "name TEXT NOT NULL,"
				+ "device_time LONG NOT NULL," // 客户端修改时间
				+ "creation_time LONG,"// 创建时间
				+ "operating_state INTEGER NOT NULL,"
				+ "offset LONG NOT NULL,"
				+ "offset_total LONG NOT NULL)";

		private static final String SQL_CREATE_TABLE_PLUGINS = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME_PLUGIN_TABLE
				+ " (_id integer primary key autoincrement,"
				+ "plugin_id integer,"// 插件ID
				+ "plugin_type integer,"// 插件类型
				+ "plugin_src integer,"// 插件来源
				+ "plugin_enable integer default 0,"// 插件来源
				+ "plugin_name TEXT not null,"// 插件名称
				+ "plugin_local_version text,"// 本地版本号，未使用，保留字段
				+ "plugin_server_version text,"// 同楼上
				+ "plugin_total_size long default -1,"// 插件总大小
				+ "plugin_current_size long default -1,"// 插件下载大小
				+ "plugin_download_status integer,"// 插件下载状态
				+ "plugin_filepath text UNIQUE,"// 本地路径
				+ "plugin_fileurl text,"
				+ "plugin_init_show_total_size long default -1"// 插件未下载1前 显示大小
																// 显示大小
				+ ");";

		private static final String SQL_CREATE_TABLE_ONLINE_CARD = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME_OLINE_CARD
				+ " (card_num TEXT primary key NOT NULL,"
				+ "availab integer NOT NULL,"// 插件ID
				+ "start LONG NOT NULL,"// 插件类型
				+ "end TEXT NOT NULL"// 插件来源
				+ ");";

		private static final String SQL_CREATE_TABLE_BOOKNOTE = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME_BOOKNOTE
				+ " ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "server_id INTEGER,"
				+ "userName TEXT,"
				+ "book_id LONG NOT NULL REFERENCES BookInforTable(id),"
				+ "order_id LONG  NOT NULL,"
				+ "bookFormate INTEGER  NOT NULL,"
				+ "startAndEndBookMark TEXT,"
				+ "startIndex INTEGER,"
				+ "endIndex INTEGER,"
				+ "content TEXT,"
				+ "hasMark INTEGER,"
				+ "remark TEXT," + "color INTEGER," + "createTime LONG," //
				+ "lastModifyTime LONG,"//
				+ "clientTime LONG," + "modifyState INTEGER)";

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = SQL_CREATE_TABLE_BOOKINFOR;
			db.execSQL(sql);

			String sql_cart = SQL_CREATE_TABLE_BOOKCAR;
			db.execSQL(sql_cart);

			String sql_userRandom = SQL_CREATE_TABLE_RANDOM;
			db.execSQL(sql_userRandom);

			String sql_bookMark = SQL_CREATE_TABLE_BOOKMARK;
			db.execSQL(sql_bookMark);

			String sql_bookcategory = SQL_CREATE_TABLE_BOOKCATEGORY;
			db.execSQL(sql_bookcategory);

			sql = SQL_CREATE_TABLE_COPYCOUNT;
			db.execSQL(sql);

			sql = SQL_CREATE_TABLE_PLUGINS;
			db.execSQL(sql);

			sql = SQL_CREATE_TABLE_ONLINE_CARD;
			db.execSQL(sql);

			sql = SQL_CREATE_TABLE_BOOKNOTE;
			db.execSQL(sql);

			// 从当前版本 DB_VESION = 30开始添加拇指数据库
			updateTable32(db);
			onUpgradeTo33(db);
			onUpgradeTo34(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(DB_NAME, "onUpgrade oldVersion = " + oldVersion
					+ "newVersion = " + newVersion);
			DBUpdateManager.updateTableName(db, oldVersion, newVersion,
					TABLE_NAME_OLD_BOOKMARK, TABLE_NAME_BOOKMARK);
			DBUpdateManager.updateVerify(db, oldVersion, newVersion,
					SQL_CREATE_TABLE_BOOKINFOR);

			DBUpdateManager.updateVerify(db, oldVersion, newVersion,
					SQL_CREATE_TABLE_RANDOM);
			DBUpdateManager.updateVerify(db, oldVersion, newVersion,
					SQL_CREATE_TABLE_BOOKCATEGORY);
			DBUpdateManager.updateBookMarkTable(db, oldVersion, newVersion,
					TABLE_NAME_BOOKMARK, SQL_CREATE_TABLE_BOOKMARK);
			DBUpdateManager.updateVerify(db, oldVersion, newVersion,
					SQL_CREATE_TABLE_BOOKMARK);
			DBUpdateManager.updateVerify(db, oldVersion, newVersion,
					SQL_CREATE_TABLE_COPYCOUNT);
			DBUpdateManager.updateBookCarTable(db, oldVersion, newVersion,
					TABLE_NAME_BOOKCART, SQL_CREATE_TABLE_BOOKCAR);
			DBUpdateManager.updateBookInfoTable(db, oldVersion, newVersion,
					TABLE_NAME_BOOKINFOR);
			DBUpdateManager.updatePluginsTable(db, oldVersion, newVersion,
					TABLE_NAME_PLUGIN_TABLE, SQL_CREATE_TABLE_PLUGINS);
			DBUpdateManager.updatePluginsTable(db, oldVersion, newVersion,
					TABLE_NAME_OLINE_CARD, SQL_CREATE_TABLE_ONLINE_CARD);
			DBUpdateManager.updatePluginsTable(db, oldVersion, newVersion,
					TABLE_NAME_BOOKNOTE, SQL_CREATE_TABLE_BOOKNOTE);

			// 从当前版本 DB_VESION 32 以下都要开始添加拇指数据库
			if (oldVersion < 32) {
				onUpgradeTo32(db);
			}
			if (oldVersion < 33) {
				onUpgradeTo33(db);
			}
			if (oldVersion < 34) {
				onUpgradeTo34(db);
			}
			if(oldVersion < 35 ){
				onUpgradeTo35(db);
			}
		}
		
		/**
		 * TODO 
		 * 数据库版本低于35的需要对localBook中的source进行加密处理
		 */
		private void onUpgradeTo35(SQLiteDatabase db) {
			
			Cursor cursor = db.rawQuery("select _id, source,book_id,user_name from ebook ", null);
			
			while (null!=  cursor  && cursor.moveToNext()) {
				int _id = cursor.getInt(0);
				String source = cursor.getString(1);
				String bookid = cursor.getString(2);
				String userName = cursor.getString(3);
				
				if(!TextUtils.isEmpty(source) && !TextUtils.isEmpty(bookid) && !TextUtils.isEmpty(userName) && !LocalBook.alreadyEncrypt(source)){
					try {
						source = SecretKeyUtil.encrypt(String.valueOf(bookid), userName, source);
						db.execSQL("UPDATE ebook SET source = '"+source+"' WHERE _id = '"+_id+"' ");
					} catch (UnsatisfiedLinkError e) {
						e.printStackTrace();
					}
					
				}
			}
			cursor.close();
		}

		//第一次创建数据库时 升级到31做的操作
		private void updateTable32(SQLiteDatabase db){
			
			createEbookTable(db);
			db.execSQL("DROP TABLE "+TABLE_NAME_BOOKINFOR );
			upgradeIn32Common(db);
		}
		
		public void createEbookTable(SQLiteDatabase db){
			db.execSQL("CREATE TABLE IF NOT EXISTS "
					+ EBOOK
					+ " ("
					+" _id INTEGER PRIMARY KEY AUTOINCREMENT,"//index
					+ "book_id Long NOT NULL," //书籍id
					+ "edition INTEGER DEFAULT (0),"//无用字段
					+ "order_code TEXT,"
					+ "product_code"
					+ " TEXT,"
					+ "size"
					+ " Long,"
					+ "url"
					+ " TEXT,"
					+ "price"
					+ " Double,"
					+ "author"
					+ " TEXT,"
					+ "user_name"// user pin 对应拇指中的userid字段
					+ " TEXT,"
					+ "title"
					+ " TEXT,"
					+ "signature"
					+ " TEXT,"
					+ "source"
					+ " Integer,"
					+ "cert"
					+ " TEXT,"
					+ "random"
					+ " TEXT,"
					+ "type_id"
					+ " Integer,"
					+ "progress"
					+ " Long,"
					+ "state_load"
					+ " Integer,"
					+ "book_state"
					+ " Integer,"
					+ "mod_time"
					+ " Long,"
					+ "add_time"
					+ " Long,"
					+ "access_time"
					+ " Long,"
					+ "hot_exp"
					+ " Integer,"
					+ "book_path"
					+ " TEXT,"
					+ "book_marks"
					+ " Blob,"
					+ "read_progress"
					+ " Blob,"
					+ "boot"
					+ " Blob,"
					+ "gift_book_infor"
					+ " Blob,"
					+ "temp_bookmark"
					+ " Blob,"
					+ "big_image_url"
					+ " TEXT,"
					+ "small_image_url"
					+ " TEXT,"
					+ "big_image_path"
					+ " TEXT,"
					+ "small_image_path"
					+ " TEXT,"
					+ "package_name"
					+ " TEXT,"
					+ "dir"
					+ " TEXT,"
					+ "version"
					+ " LONG,"
					+ "operating_state"
					+ " TEXT,"
					+ "category"
					+ " TEXT DEFAULT '',"
					+ "format"
					+ " TEXT,"
					+ "format_name"
					+ " TEXT,"
					+ "card_num"
					+ " TEXT, "
					+ "device_id"
					+ " TEXT, "
					+ "note_version"
					+ " LONG,"
					+ "note_operating_state Integer,"
					+ "borrow_end_time TEXT )");
		}
		
		public void upgradeIn32Common(SQLiteDatabase db){

			// 书籍进度表
			db.execSQL("CREATE TABLE "
					+ PROGRESS
					+ " ("
					+ _ID
					+ " INTEGER PRIMARY KEY  NOT NULL ,"
					+ EBOOKID
					+ " LONG NOT NULL DEFAULT (0) ,"
					+ USERID
					+ " TEXT DEFAULT '',"
					+ PERCENT
					+ " FLOAT NOT NULL  DEFAULT (0.0) ," // offset /offset_total
					+ BOOK_TYPE
					+ " INTEGER  NOT NULL,"
					+ SERVER_ID
					+ " INTEGER,"
					+ PARA_IDX
					+ " INTEGER NOT NULL  DEFAULT (0) ,"
					+ PDF_PAGE
					+ " INTEGER DEFAULT 0,"
					+ OFFSET_IN_PARA
					+ " INTEGER NOT NULL  DEFAULT (0) ,"
					+ OPERATION_STATE
					+ " INTEGER NOT NULL,"// operating_state 1修改 0无修改 2添加 3删除
					+ UPDATE_TIME
					+ " LONG NOT NULL , " // 客户端修改时间
					+ CREATION_TIME
					+ " LONG,"// 创建时间
					+ SHOW_ALL_NOTES + " INTEGER NOT NULL DEFAULT 0 , "
					+ DOCUMENT_ID + " INTEGER DEFAULT (0) ,"
					+ CHAPTER_ITEMREF + " VARCHAR(100), "
					+ PDF_ZOOM
					+ " FLOAT DEFAULT 1, "// 1 默认大小
					+ PDF_X_OFFSET_PERCENT + " FLOAT DEFAULT 0.0, "
					+ PDF_Y_OFFSET_PERCENT + " FLOAT DEFAULT 0.0, "
					+ CHAPTER_TITLE + " TEXT DEFAULT '' )");

			// 保存用户书签数据

			db.execSQL("CREATE TABLE IF NOT EXISTS "
					+ BOOKMARK_TABLE
					+ " ("

					+ BOOKMARK_ID
					+ " INTEGER PRIMARY KEY  NOT NULL UNIQUE, "// client_id
					+ BOOKMARK_SERVERID
					+ " LONG DEFAULT 0 , "// server_id
					+ BOOKMARK_USERID
					+ "  TEXT, " // =============user
					+ BOOKMARK_EBOOKID
					+ " LONG  DEFAULT 0 , " // book_id
					+ BOOKMARK_DOCID
					+ " INTEGER DEFAULT 0, " // doc_id
					+ BOOK_TYPE
					+ " INTEGER  NOT NULL,"// epub /pdf
					+ BOOKMARK_CHAPTER_TITLE
					+ " VARCHAR(400) , "
					+ BOOKMARK_CHAPTER_ITEMREF
					+ " VARCHAR(100) NOT NULL , "
					+ BOOKMARK_OFFSET_IN_PARA
					+ " INTEGER NOT NULL , " // element_index,epub only
					+ BOOKMARK_PARA_INDEX
					+ " INTEGER NOT NULL , " // paragraph_index,epub only
					+ BOOKMARK_DIGEST
					+ " TEXT , "// name, epub only
					+ BOOKMARK_UPDATE_AT
					+ " VARCHAR(30) NOT NULL, "
					+ CREATION_TIME
					+ " LONG,"// 创建时间
					+ IS_SYNC
					+ " INTEGER DEFAULT (0) ,"
					+ OPERATION_STATE
					+ " INTEGER NOT NULL,"// operating_state// 1修改 0无修改 2添加 3删除
					+ BOOKMARK_PDF_PAGE
					+ " INTEGER DEFAULT 0 )"// paragraph_index // ,pdf only
					);
			
			// 书签同步
			db.execSQL("CREATE TABLE IF NOT EXISTS " + BOOKMARKSYNC + " ("
					+ BOOKMARKSYNC_ID
					+ " INTEGER PRIMARY KEY  NOT NULL UNIQUE, "
					+ BOOKMARKSYNC_USRID + " TEXT, "
					+ BOOKMARKSYNC_EBOOKID + " LONG  DEFAULT 0 , "
					+ BOOKMARKSYNC_DOCID + " INTEGER DEFAULT 0, "
					+ BOOKMARKSYNC_LAST_UPDATE + " VARCHAR(30)) ");
   
			// 第三方或者外部导入书籍
			db.execSQL("CREATE TABLE IF NOT EXISTS " + DOCUMENT + " (" + _ID
					+ " INTEGER PRIMARY KEY  NOT NULL UNIQUE, " 
					+ TITLE+ " VARCHAR(400)," //书籍名称
					+ AUTHOR + " VARCHAR(400)," 
					+ BOOK_PATH+ " TEXT,"
					+ BOOK_SOURCE+ " TEXT,"
					+ BOOK_TYPE + " INTEGER DEFAULT (0) , "//0 EPUB 1 PDF
					+ COVER_PATH+ " VARCHAR(400), " 
					+ SIZE+ " Long,"
					+ DOCUMENT_PROGRESS+ " Long,"
					+ STATE_LOAD+ " Integer,"
					+ BOOK_STATE+ " Integer,"//书籍下载进度
					+ ADD_AT + " INTEGER NOT NULL, "
					+ ACCESS_TIME+ " INTEGER,"
					+ MOD_TIME+ " INTEGER,"
					+ TEMP_ID+ " INTEGER DEFAULT (0),"//用于数据迁移 无其他作用
					+ READ_AT + " INTEGER NOT NULL DEFAULT 100, "
					+ FROM_CLOUDDISK + " INTEGER NOT NULL DEFAULT 0, "//0 云盘下载的 1 外部导入的   
					+ OPF_MD5+ " VARCHAR(400))");
			

			// 导入书籍绑定表
			db.execSQL("CREATE TABLE IF NOT EXISTS " + DOCBIND + " (" + _ID
					+ " INTEGER PRIMARY KEY  NOT NULL UNIQUE, " + DOCUMENT_ID
					+ " INTEGER NOT NULL, " + USERID + "  TEXT NOT NULL, "
					+ SERVER_ID + " LONG DEFAULT (0), " + BIND
					+ " INTEGER DEFAULT (0), " + BOOKID + " LONG DEFAULT (0), "
					+ TITLE + " VARCHAR(400)," + AUTHOR + " VARCHAR(400),"
					+ COVER_PATH + " VARCHAR(400))");

			// 笔记表
			db.execSQL("CREATE TABLE IF NOT EXISTS " + EBOOKNOTE + " (" + _ID
					+ " INTEGER PRIMARY KEY  NOT NULL ," + SERVER_NOTE_ID
					+ " LONG NOT NULL  DEFAULT (-1) ," + EBOOKID
					+ " LONG NOT NULL  DEFAULT (0) ," + USERID
					+ "  TEXT  NOT NULL DEFAULT(0) ," + CHAPTER_NAME
					+ " TEXT ," + START_PARA_IDX + " INTEGER NOT NULL ,"
					+ START_OFFSET_IN_PARA + " INTEGER NOT NULL ,"
					+ END_PARA_IDX + " INTEGER NOT NULL ," + END_OFFSET_IN_PARA
					+ " INTEGER NOT NULL ," + QUOTE + " TEXT ," + CONTENT
					+ " TEXT ," + UPDATE_TIME + " INTEGER NOT NULL ,"
					+ TIMELINE_GUID + " NTEXT , " + IS_PRIVATE
					+ " INTEGER NOT NULL ," + MODIFIED
					+ " INTEGER NOT NULL  DEFAULT (0) ," + DELETED
					+ " INTEGER NOT NULL  DEFAULT (0) ," + DOCUMENT_ID
					+ " INTEGER NOT NULL  DEFAULT (0) ," + CHAPTER_ITEMREF
					+ " VARCHAR(100), " + PDF_PAGE + " INTEGER DEFAULT 0, "
					+ PDF_YOFFSET + " FLOAT DEFAULT 0.0, " + PDF_XOFFSET
					+ " FLOAT DEFAULT 0.0)");

			// 笔记同步表
			db.execSQL("CREATE TABLE IF NOT EXISTS " + NOTESYNC + " (" + _ID
					+ " INTEGER PRIMARY KEY  NOT NULL ," + USERID
					+ "  TEXT  NOT NULL ," + EBOOKID
					+ " LONG NOT NULL  DEFAULT (0) ," + DOCUMENT_ID
					+ " INTEGER NOT NULL  DEFAULT (0) ," + TARGET_USER_ID
					+ " TEXT  DEFAULT 0 , " + TARGET_USER_NICKNAME
					+ " NVARCHAR(100) , " + TARGET_USER_AVATAR
					+ " VARCHAR(200) , " + TARGET_USER_SUMMARY + " TEXT , "
					+ LAST_UPDATE_TIME + " INTEGER NOT NULL  DEFAULT (0))");

			// 保存离线阅历数据
			db.execSQL("CREATE TABLE IF NOT EXISTS " + READINGDATA + " ("
					+ RD_ID + " INTEGER PRIMARY KEY  NOT NULL UNIQUE, "
					+ RD_DOCUMENTID + " INTEGER NOT NULL DEFAULT 0, "
					+ RD_EBOOKID + " LONG NOT NULL DEFAULT 0, " + USERID
					+ "  TEXT  NOT NULL ," + RD_STARTTIME
					+ " LONG NOT NULL, " + RD_CHAPTER + " VARCHAR(400), "
					+ RD_PARAIDX + " INTEGER NOT NULL, " + RD_PDF_PAGE
					+ " INTEGER NOT NULL, " + RD_ENDTIME + " LONG NOT NULL,"
					+ RD_ENDCHAPTER + " VARCHAR(400)," + RD_ENDPARAIDX
					+ " INTEGER NOT NULL," + RD_END_PDF_PAGE
					+ " INTEGER NOT NULL, " + RD_LENGTH + " LONG NOT NULL)");

			// 保存最近提到的书籍信息
			db.execSQL("CREATE TABLE IF NOT EXISTS " + MENTION + " (" + ME_ID
					+ " INTEGER PRIMARY KEY  NOT NULL UNIQUE, " + ME_BOOK_ID
					+ " LONG NOT NULL, " + ME_BOOK_NAME + " VARCHAR(400), "
					+ ME_BOOK_TIME + " INTEGER NOT NULL)");

			// 保存书籍的分页信息
			db.execSQL("CREATE TABLE IF NOT EXISTS " + BOOKPAGE + " (" + _ID
					+ " INTEGER PRIMARY KEY  NOT NULL UNIQUE, " + EBOOKID
					+ " LONG NOT NULL  DEFAULT (0), " + DOCUMENT_ID
					+ " INTEGER NOT NULL  DEFAULT (0), " + BOOK_TEXT_SIZE
					+ " INTEGER NOT NULL  DEFAULT (0), " + BOOK_CHAPTER_PAGE
					+ " NTEXT, " + BOOK_CHAPTER_BLOCK + " NTEXT, "
					+ BOOK_FONT_FACE + " NTEXT, " + BOOK_LINE_SPACE
					+ " INTEGER NOT NULL  DEFAULT (0), " + BOOK_BLOCK_SPACE
					+ " INTEGER NOT NULL  DEFAULT (0), " + BOOK_SCREEN_MODE
					+ " INTEGER NOT NULL  DEFAULT (0) )" //Portrait:0   landscape:1
					);

			// 保存splash数据
			db.execSQL("CREATE TABLE IF NOT EXISTS " + SPLASH + " ("
					+ SPLASH_ID + " INTEGER PRIMARY KEY  NOT NULL UNIQUE, "
					+ SPLASH_PIC + " VARCHAR(400)," + SPLASH_SAYING
					+ " VARCHAR(400)," + SPLASH_PERCENT + " INTEGER,"+SPLASH_PERCENT_WIDTH+" INTEGER )");

			// 文件夹表
			db.execSQL("CREATE TABLE IF NOT EXISTS " + FOLDER + " ("
					+ FOLDER_ID + " INTEGER PRIMARY KEY  NOT NULL UNIQUE, "
					+ FOLDER_NAME + " VARCHAR(200)," + FOLDER_CHANGETIME
					+ " VARCHAR(100)," + USERID + " TEXT NOT NULL DEFAULT(0))");

			
			// 书架表
			db.execSQL("CREATE TABLE IF NOT EXISTS " + BOOKSHELF + " ("
					+ BOOKSHELF_ID + " INTEGER PRIMARY KEY  NOT NULL UNIQUE, "
					+ BOOKSHELF_FOLDER_ID + " INTEGER NOT NULL DEFAULT -1,"
					+ BOOKSHELF_DOCUMENT_ID + " INTEGER NOT NULL DEFAULT -1,"
					+ BOOKSHELF_EBOOK_ID + " LONG NOT NULL DEFAULT -1,"
					+ BOOKSHELF_CHANGE_TIME + " VARCHAR(100),"
					+ FOLDER_CONTAINER_FOLDER_ID + " INTEGER DEFAULT -1,"
					+ USERID + " TEXT NOT NULL DEFAULT(0))");

			// 保存章节页面内容排版信息
			db.execSQL("CREATE TABLE IF NOT EXISTS " + PAGECONTENT + " (" + _ID
					+ " INTEGER PRIMARY KEY  NOT NULL UNIQUE, " + EBOOKID
					+ " LONG NOT NULL  DEFAULT (0), " + DOCUMENT_ID
					+ " INTEGER NOT NULL  DEFAULT (0), " + BOOK_TEXT_SIZE
					+ " INTEGER NOT NULL  DEFAULT (0), " + CHAPTER_ITEMREF
					+ " VARCHAR(100), " + PAGE_START_PARA + " NTEXT, "
					+ ANCHOR_LOCATION + " NTEXT, " + PAGE_START_OFFSET
					+ " NTEXT, " + BOOK_FONT_FACE + " NTEXT, " + BOOK_LINE_SPACE
					+ " INTEGER NOT NULL  DEFAULT (0), " + BOOK_BLOCK_SPACE
					+ " INTEGER NOT NULL  DEFAULT (0), " + BOOK_SCREEN_MODE // Portrait:0   landscape:1
					+ " INTEGER NOT NULL  DEFAULT (0) )");
		}
		
		/**
		 * 以下表中ebookid 对应京东的bookinfr表中的id userid 对应bookinfr中的user_name
		 * 本地存在表bookinftable  需要更新
		 * @param db
		 */
		private void onUpgradeTo32(SQLiteDatabase db) {

			upgradeIn32Common(db);
			// 修改bookinfo表 添加书籍版本字段
			createEbookTable(db);
			migrateDataTo32(db);
			db.execSQL("DROP TABLE "+TABLE_NAME_BOOKINFOR );//删除bookinfor表
			
		}
		
		/**
		 * 同步笔记表notesync  增加role和jd_user_name字段
		 * 本地存在表notesync  需要更新
		 * @param db
		 */
		private void onUpgradeTo33(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + NOTESYNC + " ADD COLUMN "
					+ TARGET_USER_PIN + "  TEXT ");
			db.execSQL("ALTER TABLE " + NOTESYNC + " ADD COLUMN "
					+ TARGET_USER_ROLE + " INTEGER DEFAULT 0");
			db.execSQL("ALTER TABLE " + NOTESYNC + " ADD COLUMN "
					+ TARGET_USER_NOTECOUNT + " INTEGER DEFAULT 0");
		}
		
		/**
		 * 同步笔记表notesync  增加role和jd_user_name字段
		 * 本地存在表notesync  需要更新
		 * @param db
		 */
		private void onUpgradeTo34(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + BOOKPAGE + " ADD COLUMN "
					+ BOOK_PAGE_EDGE_SPACE + "  INTEGER NOT NULL  DEFAULT (0) ");
			db.execSQL("ALTER TABLE " + PAGECONTENT + " ADD COLUMN "
					+ BOOK_PAGE_EDGE_SPACE + "  INTEGER NOT NULL  DEFAULT (0) ");
		}
		

		private void migrateDataTo32(SQLiteDatabase db){
			//迁移ebook数据
			MZLog.d("wangguodong", "迁移bookinfor数据到ebook表开始");
			db.execSQL("INSERT INTO "+EBOOK+" (book_id,order_code,product_code,size,url,price,author,user_name,title,"
									+"signature,source,cert,random,type_id,progress,state_load,book_state,mod_time,"
									+"add_time,access_time,hot_exp,book_path,book_marks,read_progress,boot,gift_book_infor,"
									+"temp_bookmark,big_image_url,small_image_url,big_image_path,small_image_path,package_name,"
									+"dir,version,operating_state,category,format,format_name,card_num,device_id,note_version,note_operating_state) "
									
									+" SELECT id,order_code,product_code,size,url,price,author,user_name,title,"
									+"signature,source,cert,random,type_id,progress,state_load,book_state,mod_time,"
									+"add_time,access_time,hot_exp,book_path,book_marks,read_progress,boot,gift_book_infor,"
									+"temp_bookmark,big_image_url,small_image_url,big_image_path,small_image_path,package_name,"
									+"dir,version,operating_state,category,format,format_name,card_num,device_id,note_version,note_operating_state "
									+" FROM "+ TABLE_NAME_BOOKINFOR +" WHERE source !='' AND source !='built_in'");
			MZLog.d("wangguodong", "迁移bookinfor数据到ebook表结束");
			//迁移document数据
			MZLog.d("wangguodong", "迁移bookinfor数据到document表开始");
			db.execSQL("INSERT INTO "+DOCUMENT+" (title,author,book_source,book_type,cover_path,size,progress,state_load,book_state,add_at,access_time,mod_time,readAt,fromCloudDisk,opf_md5,temp_id ) "
					+" SELECT title,author,book_path,format,small_image_path,size,progress,state_load,book_state,add_time,access_time,mod_time,1000,0,'',id "
					+" FROM "+ TABLE_NAME_BOOKINFOR +" WHERE source ='' OR source ='built_in'");
			MZLog.d("wangguodong", "迁移bookinfor数据到document表结束");
			//将ebook数据保存到bookshelf表
			MZLog.d("wangguodong", "迁移ebook数据到bookshelf表开始");
			db.execSQL("INSERT INTO "+BOOKSHELF+" (folder_id,document_id,ebook_id,change_time,folder_dir_id,userid) "
					+" SELECT -1,-1,_id,add_time,-1,user_name  "
					+" FROM "+ EBOOK );
			MZLog.d("wangguodong", "迁移ebook数据到bookshelf表结束");
			//将document数据保存到bookshelf表
			MZLog.d("wangguodong", "迁移document数据到bookshelf表开始");
			db.execSQL("INSERT INTO "+BOOKSHELF+" (folder_id,document_id,ebook_id,change_time,folder_dir_id,userid) "
					+" SELECT -1,_id,-1,add_at,-1,''  "
					+" FROM "+ DOCUMENT );
			MZLog.d("wangguodong", "迁移document数据到bookshelf表结束");
			
			//迁移书签数据
			MZLog.d("wangguodong", "迁移书签数据开始");
			
			db.execSQL("INSERT INTO "+BOOKMARK_TABLE+" (server_id,userid,ebookid,docid,book_type,chapter_title,digest,offset_in_para,para_index,updated_at,creation_time,is_sync,operating_state,pdf_page,chapter_itemref) "
					+" SELECT server_id,user,book_id,-1,book_type,'',name,element_index,paragraph_index,device_time,creation_time,0,operating_state,paragraph_index,'' "
					+" FROM "+ TABLE_NAME_BOOKMARK +" where data_type = 1 and book_id IN (SELECT id FROM "+TABLE_NAME_BOOKINFOR+" where source !='' AND source !='built_in')");
			
			
			
			db.execSQL("INSERT INTO "+BOOKMARK_TABLE+" (server_id,userid,ebookid,docid,book_type,chapter_title,digest,offset_in_para,para_index,updated_at,creation_time,is_sync,operating_state,pdf_page,chapter_itemref) "
					+" SELECT server_id,user,-1,book_id,book_type,'',name,element_index,paragraph_index,device_time,creation_time,0,operating_state,paragraph_index,''"
					+" FROM "+ TABLE_NAME_BOOKMARK +" where data_type = 1 and book_id IN (SELECT id FROM "+TABLE_NAME_BOOKINFOR+" where source ='' OR source ='built_in')");
			
			
			
			db.execSQL("UPDATE "+BOOKMARK_TABLE+" SET docid = (SELECT _id from document WHERE bookmark.docid = document.temp_id AND bookmark.ebookid = -1)");
			
			MZLog.d("wangguodong", "迁移书签数据结束");
			
			//迁移进度数据
			MZLog.d("wangguodong", "迁移进度数据开始");
			
			db.execSQL("INSERT INTO "+PROGRESS+" (server_id,ebookid,document_id,userid,percent,book_type,para_idx,pdf_page,offset_in_para,operating_state,update_time,creation_time ) "
					+" SELECT server_id,book_id,-1,user, 0 ,book_type,  paragraph_index,paragraph_index,element_index,operating_state,device_time,creation_time  "
					+" FROM "+ TABLE_NAME_BOOKMARK+" where data_type = 0  and book_id  IN (SELECT id FROM "+TABLE_NAME_BOOKINFOR+" where source ='' AND source ='built_in')" );
			
			db.execSQL("INSERT INTO "+PROGRESS+" (server_id,ebookid,document_id,userid,percent,book_type,para_idx,pdf_page,offset_in_para,operating_state,update_time,creation_time ) "
					+" SELECT server_id,-1,book_id,user,0,book_type,  paragraph_index,paragraph_index,element_index,operating_state,device_time,creation_time  "
					+" FROM "+ TABLE_NAME_BOOKMARK+" where data_type = 0  and book_id  IN (SELECT id FROM "+TABLE_NAME_BOOKINFOR+" where source ='' OR source ='built_in')" );
			
			db.execSQL("UPDATE "+PROGRESS+" SET document_id = (SELECT _id from document WHERE progress.document_id = document.temp_id AND progress.ebookid = -1)");
			
			MZLog.d("wangguodong", "迁移进度数据结束");
		}
		
		
		
		
	}

	static DatabaseHelper mOpenHelper;

	static SQLiteDatabase db;
	
	public static SQLiteDatabase getDbInstance(Context context){
		
		if(db==null)
		{
			mOpenHelper = new DatabaseHelper(context);
			db = mOpenHelper.getReadableDatabase();
		}
		return db;
	}


	@Override
	public boolean onCreate() {

		String db_auth = getContext().getPackageName() + ".provider";
		CONTENT_URI_EBOOK = Uri.parse("content://" + db_auth + "/"
				+ EBOOK);
		CONTENT_URI_DOCUMENT = Uri.parse("content://" + db_auth + "/"
				+ DOCUMENT);
		CONTENT_URI_BOOKCART = Uri.parse("content://" + db_auth + "/"
				+ TABLE_NAME_BOOKCART);
		CONTENT_URI_NAME_RANDOM = Uri.parse("content://" + db_auth + "/"
				+ TABLE_NAME_RANDOM);
		CONTENT_URI_NAME_BOOKMARK = Uri.parse("content://" + db_auth + "/"
				+ TABLE_NAME_BOOKMARK);
		CONTENT_URI_NAME_BOOKCATEGORY = Uri.parse("content://" + db_auth + "/"
				+ TABLE_NAME_BOOKCATEGORY);
		CONTENT_URI_NAME_BOOKCOPYCOUNT = Uri.parse("content://" + db_auth + "/"
				+ TABEL_NAME_COPYCOUNT);
		CONTENT_URI_NAME_PLUGINS = Uri.parse("content://" + db_auth + "/"
				+ TABLE_NAME_PLUGIN_TABLE);
		CONTENT_URI_NAME_OLINE_CARD = Uri.parse("content://" + db_auth + "/"
				+ TABLE_NAME_OLINE_CARD);
		CONTENT_URI_NAME_MYBOOKNOTE = Uri.parse("content://" + db_auth + "/"
				+ TABLE_NAME_BOOKNOTE);

		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(db_auth, EBOOK, _EBOOK);
		sUriMatcher.addURI(db_auth, DOCUMENT, _DOCUMENT);
		sUriMatcher.addURI(db_auth, TABLE_NAME_BOOKCART, BOOKCART);
		sUriMatcher.addURI(db_auth, TABLE_NAME_RANDOM, RANDOM);
		sUriMatcher.addURI(db_auth, TABLE_NAME_BOOKMARK, BOOKMARK);
		sUriMatcher.addURI(db_auth, TABLE_NAME_BOOKCATEGORY, BOOKCATEGORY);
		sUriMatcher.addURI(db_auth, TABEL_NAME_COPYCOUNT, BOOKCOPYCOUNT);
		sUriMatcher.addURI(db_auth, TABLE_NAME_PLUGIN_TABLE, PLUGIN_URI_CODE);
		sUriMatcher.addURI(db_auth, TABLE_NAME_OLINE_CARD, OLINE_CARD);
		sUriMatcher.addURI(db_auth, TABLE_NAME_BOOKNOTE, BOOKNOTE);
		
		getDbInstance(this.getContext());
		
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int num = 0;
		String tableName = getTableName(uri);
		num = db.delete(tableName, selection, selectionArgs);
		return num;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {

		case _EBOOK:// 获取单条记录
			return "vnd.android.cursor.dir/ebook";
		default:
			throw new IllegalArgumentException("Uri IllegalArgument:" + uri);
		}
	}

	public String getTableName(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case _EBOOK:// 获取单条记录
			return TABLE_NAME_EBOOK;
		case BOOKCART:// 获取单条记录
			return TABLE_NAME_BOOKCART;
		case RANDOM:// 获取单条记录
			return TABLE_NAME_RANDOM;
		case BOOKMARK:// 获取单条记录
			return TABLE_NAME_BOOKMARK;
		case BOOKCATEGORY:
			return TABLE_NAME_BOOKCATEGORY;
		case BOOKCOPYCOUNT:
			return TABEL_NAME_COPYCOUNT;
		case PLUGIN_URI_CODE:
			return TABLE_NAME_PLUGIN_TABLE;
		case OLINE_CARD:
			return TABLE_NAME_OLINE_CARD;
		case BOOKNOTE:
			return TABLE_NAME_BOOKNOTE;
		default:
			return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long id = -1;
		String tableName = getTableName(uri);
		id = db.insert(tableName, "Content is empty", values);
		if (id == -1) {
			return null;
		}
		return Uri.parse("" + id);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cur = null;
		String tableName = getTableName(uri);
		cur = db.query(tableName, projection, selection, selectionArgs, null,
				null, sortOrder);
		return cur;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int num = 0;
		String tableName = getTableName(uri);
		num = db.update(tableName, values, selection, selectionArgs);
		return num;
	}

	public void colse() {
		db.close();
		mOpenHelper.close();
	}

}