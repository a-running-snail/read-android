package com.jingdong.app.reader.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static DBHelper mInstance;

	private static final String DB_NAME = "download_db";
	private static final int VERSION = 3;

	private static final String SQL_CREATE = "CREATE TABLE thread_info(_id integer primary key autoincrement,"
			+ "thread_id integer,url text,start long,end long,finished long,total_length long,state integer,accept_range integer)";
	private static final String SQL_DROP = "drop table if exists thread_info";

	public static DBHelper getInstance(Context context) {
		if (mInstance == null) {
			synchronized (DBHelper.class) {
				if (mInstance == null) {
					mInstance = new DBHelper(context);
				}
			}
		}
		return mInstance;
	}

	private DBHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL(SQL_DROP);
		db.execSQL(SQL_CREATE);
	}

}
