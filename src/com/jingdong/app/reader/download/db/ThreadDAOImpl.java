package com.jingdong.app.reader.download.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jingdong.app.reader.download.entity.ThreadInfo;
import com.jingdong.app.reader.download.util.L;

/**
 * 数据访问接口实现
 * 
 * @author Beyond
 *
 */
public class ThreadDAOImpl implements ThreadDAO {

	private static final String TAG = "ThreadDAOImpl";
	private DBHelper mDBHelper;

	public ThreadDAOImpl(Context context) {
		mDBHelper = DBHelper.getInstance(context);
	}

	@Override
	public synchronized void insertThreadInfo(ThreadInfo threadInfo) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		String sql = "insert into thread_info(thread_id,url,start,end,finished,total_length,state,accept_range) values(?,?,?,?,?,?,?,?)";
		db.execSQL(sql, new Object[] { threadInfo.getId(), threadInfo.getUrl(), threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinished(),
				threadInfo.getTotal(), threadInfo.getState(), threadInfo.getAcceptRange() });
		db.close();
	}

	@Override
	public synchronized void deleteThread(String url) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		String sql = "delete from thread_info where url=?";
		db.execSQL(sql, new Object[] { url });
		L.i( "delete success");
		db.close();

	}

	@Override
	public synchronized void updateThread(String url, int thread_id, long finished, int state) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		String sql = "update thread_info set finished=?,state=? where url=? and thread_id=?";
		db.execSQL(sql, new Object[] { finished, state, url, thread_id });
		// db.close();
	}

	@Override
	public synchronized List<ThreadInfo> getThreads(String url) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[] { url });
		List<ThreadInfo> list = new ArrayList<ThreadInfo>();
		while (cursor.moveToNext()) {
			ThreadInfo info = new ThreadInfo();
			info.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
			info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			info.setStart(cursor.getLong(cursor.getColumnIndex("start")));
			info.setEnd(cursor.getLong(cursor.getColumnIndex("end")));
			info.setFinished(cursor.getLong(cursor.getColumnIndex("finished")));
			info.setTotal(cursor.getLong(cursor.getColumnIndex("total_length")));
			info.setState(cursor.getInt(cursor.getColumnIndex("state")));
			info.setAcceptRange(cursor.getInt(cursor.getColumnIndex("accept_range")));
			list.add(info);
		}
		cursor.close();
		// db.close();
		return list;
	}

	@Override
	public boolean isExists(String url, int thread_id) {
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?", new String[] { url, thread_id + "'" });
		boolean exists = cursor.moveToNext();
		cursor.close();
		db.close();
		return exists;
	}

	@Override
	public ThreadInfo getThread(int thread_id) {
		ThreadInfo info=null;
		try {
			SQLiteDatabase db = mDBHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery("select * from thread_info where thread_id = ?", new String[] { thread_id + "" });
			info = new ThreadInfo();
			info.setAcceptRange(cursor.getInt(cursor.getColumnIndex("accept_range")));
			info.setEnd(cursor.getLong(cursor.getColumnIndex("end")));
			info.setStart(cursor.getLong(cursor.getColumnIndex("start")));
			info.setFinished(cursor.getLong(cursor.getColumnIndex("finished")));
			info.setTotal(cursor.getLong(cursor.getColumnIndex("total_length")));
			info.setState(cursor.getInt(cursor.getColumnIndex("state")));
			info.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
			info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			cursor.close();
			db.close();
			return info;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
