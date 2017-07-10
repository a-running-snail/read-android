package com.jingdong.app.reader.data.db;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.android.mzbook.sortview.model.BookShelfModel;
import com.android.mzbook.sortview.model.Folder;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.book.EBook;
import com.jingdong.app.reader.book.Mention;
import com.jingdong.app.reader.book.SerializableBook;
import com.jingdong.app.reader.client.ServiceProtocol;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.ReadBookPage;
import com.jingdong.app.reader.entity.ReadPageContent;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.entity.extra.Splash;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.model.BookShelf;
import com.jingdong.app.reader.notes.NotesModel;
import com.jingdong.app.reader.reading.BookMark;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.reading.ReadProgress;
import com.jingdong.app.reader.reading.ReadingData;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.JSONArrayPoxy;
import com.jingdong.app.reader.util.JSONObjectProxy;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.SecretKeyUtil;
import com.jingdong.app.reader.util.UiStaticMethod;

public class MZBookDatabase {

	public static MZBookDatabase instance;
	private SQLiteDatabase db;
	public static boolean isStorageReady = true;

	public static void init(Context context) {
		if (instance == null)
			instance = new MZBookDatabase(context);
	}

	public MZBookDatabase(Context context) {
		db = DataProvider.getDbInstance(context);
	}

	public List<LocalBook> onlyEookByUserid() {
		List<LocalBook> list = LocalBook.getLocalBookList("user_name=?",
				new String[] { LoginUser.getpin() });
		String[] _ids = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			_ids[i] = String.valueOf(list.get(i)._id);
		}
		List<LocalBook> list1 = new ArrayList<LocalBook>();
		if (list.size() > 0)
			list1 = LocalBook.getLocalBookList("id in ("
				+ makePlaceholders(list.size()) + ")", _ids);
		return list1;
	}

	public void clearEbookDataByUserid(List<Integer> ids) {  

		String[] arg = { LoginUser.getpin() };
		String[] _ids = new String[ids.size()];
		for (int i = 0; i < ids.size(); i++) {
			_ids[i] = String.valueOf(ids.get(i));
		}
		
		String placeholder = makePlaceholders(ids.size());
		// DataProvider.USERID+" = "+LoginUser.getpin() +" AND "+ 由于索引已经和用户关联了
		db.delete(DataProvider.PROGRESS, DataProvider.USERID
				+ " =? and ebookid > 0", arg);
		db.delete(DataProvider.BOOKSHELF, DataProvider.BOOKSHELF_EBOOK_ID
				+ " IN (" + placeholder + ")", _ids);
		db.delete(DataProvider.EBOOK,
				DataProvider._ID + " IN (" + placeholder + ")", _ids);
		MZLog.d("wangguodong", "清除书架用户下载书籍完成");
	}

	public int selectBookCount(String title, String user_id) {
		int count = 0;
		String sql = "select count(*) from ebook where title like ? and user_name=?"; // sql
		String[] args = new String[] { "%" + title + "%", user_id };
		Cursor result = db.rawQuery(sql, args);
		if (result.moveToNext()) {
			count = result.getInt(0);
		}

		String sql1 = "select count(*) from docbind where title like ? and userid=? ";
		Cursor result1 = db.rawQuery(sql1, args);
		if (result1.moveToNext()) {
			count += result1.getInt(0);
		}
		String sql2 = "select count(*) from document where title like ? ";
		Cursor result2 = db.rawQuery(sql2, new String[] { "%" + title + "%" });
		if (result2.moveToNext()) {
			count += result2.getInt(0);
		}
		result.close();
		result1.close();
		result2.close();
		return count;
	}

	synchronized public List<BookShelfModel> listAllRecentReadingBooks(
			String userid ){
		
		List<BookShelfModel> recentLists = new ArrayList<BookShelfModel>();
		List<BookShelfModel> ebookList = new ArrayList<BookShelfModel>();
		List<BookShelfModel> documentList = new ArrayList<BookShelfModel>();
		HashMap<String, BookShelfModel> ebookHash = new HashMap<String, BookShelfModel>();
		HashMap<String, BookShelfModel> docHash = new HashMap<String, BookShelfModel>();

		Cursor progressCursor = db.rawQuery("select " + DataProvider.EBOOKID
				+ "," + DataProvider.DOCUMENT_ID + ","
				+ DataProvider.UPDATE_TIME + "," + DataProvider.PERCENT
				+ " from " + DataProvider.PROGRESS + " where "
				+ DataProvider.USERID + " = '" + userid + "' ORDER BY "
				+ DataProvider.UPDATE_TIME + " DESC LIMIT 30 OFFSET 0", null);
		
		while (progressCursor != null && progressCursor.moveToNext()) {
			int documentid = progressCursor.getInt(1);
			int ebookid = progressCursor.getInt(0);
			long time = progressCursor.getLong(2);
			float percent = progressCursor.getFloat(3);

			BookShelfModel model = new BookShelfModel();
			if (ebookid != 0) {
				model.setServerid(ebookid);
				model.setBookType("ebook");
				// model.setModifiedTime(time);
				model.setPercentTime(time);
				model.setBookPercent((int) (percent * 100));
				ebookHash.put(String.valueOf(ebookid), model);

			} else if (documentid != 0) {
				model.setBookid(documentid);
				model.setBookType("document");
				// model.setModifiedTime(time);
				model.setPercentTime(time);
				model.setBookPercent((int) (percent * 100));
				docHash.put(String.valueOf(documentid), model);
			}
		}
		if (progressCursor != null)
			progressCursor.close();

		// 构造ebook 数组
		if (ebookHash.keySet().size() > 0) {
			String[] book_ids = ebookHash.keySet().toArray(
					new String[ebookHash.keySet().size()]);
			StringBuffer ebookidsSB = new StringBuffer();
			for (int i = 0; i < ebookHash.keySet().size(); i++)
				ebookidsSB.append("?").append(",");
			ebookidsSB.deleteCharAt(ebookidsSB.length() - 1);

			Cursor ebookInfoCursor = db.rawQuery(
					"select _id,book_id,title,small_image_url,author,source from "
							+ DataProvider.EBOOK + " where user_name = '"
							+ userid + "' AND book_id IN ("
							+ ebookidsSB.toString() + ")", book_ids);
			while (ebookInfoCursor != null && ebookInfoCursor.moveToNext()) {
				int _id = ebookInfoCursor.getInt(0);
				long book_id = ebookInfoCursor.getLong(1);
				String title = ebookInfoCursor.getString(2);
				String cover = ebookInfoCursor.getString(3);
				String author = ebookInfoCursor.getString(4);
				String source = ebookInfoCursor.getString(5);
				int label = 0;
				if (source.equals(LocalBook.SOURCE_BORROWED_BOOK))
					label = BookShelfModel.LABEL_BORROWED;
				else if (source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
					label = BookShelfModel.LABEL_CHANGDU;
				} else if (source.equals(LocalBook.SOURCE_TRYREAD_BOOK)) {
					label = BookShelfModel.LABEL_TRYREAD;
				} else {
					label = 0;
				}
				BookShelfModel temp = ebookHash.get(String.valueOf(book_id));
				if (temp != null) {
					temp.setId(_id);
					temp.setBookid(_id);
					temp.setBookName(title);
					temp.setBookCover(cover);
					temp.setAuthor(author);
					temp.setServerid(book_id);
					temp.setBookCoverLabel(label);
					ebookHash.put(String.valueOf(book_id), temp);
				}
			}
			if (ebookInfoCursor != null)
				ebookInfoCursor.close();

			// 查询ebook笔记数
			Cursor notenumEbookCursor = db.rawQuery(
					"select count(*), ebookid from " + DataProvider.EBOOKNOTE
							+ " where " + DataProvider.USERID + " = '" + userid
							+ "' AND ebookid IN (" + ebookidsSB.toString()
							+ ") and document_id = 0 group by ebookid",
					book_ids);
			while (notenumEbookCursor != null
					&& notenumEbookCursor.moveToNext()) {
				int count = notenumEbookCursor.getInt(0);
				long bookid = notenumEbookCursor.getInt(1);
				BookShelfModel temp = ebookHash.get(String.valueOf(bookid));
				if (temp != null) {
					temp.setNote_num(count);
					ebookHash.put(String.valueOf(bookid), temp);
				}
			}
			if (notenumEbookCursor != null)
				notenumEbookCursor.close();
		}

		if (docHash.keySet().size() > 0) {

			// 构造document 数组
			String[] documentids = docHash.keySet().toArray(
					new String[docHash.keySet().size()]);

			StringBuffer documentidsSB = new StringBuffer();
			for (int i = 0; i < docHash.keySet().size(); i++)
				documentidsSB.append("?").append(",");
			documentidsSB.deleteCharAt(documentidsSB.length() - 1);

			Cursor documentInfoCursor = db.rawQuery(
					"SELECT _id,title,cover_path,author,state_load FROM document WHERE _id IN ("
							+ documentidsSB.toString() + ")", documentids);
			while (documentInfoCursor != null
					&& documentInfoCursor.moveToNext()) {

				int documentid = documentInfoCursor.getInt(0);
				String title = documentInfoCursor.getString(1);
				String cover = documentInfoCursor.getString(2);
				String author = documentInfoCursor.getString(3);
				int state = documentInfoCursor.getInt(4);
				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null) {
					temp.setBookName(title);
					temp.setBookCover(cover);
					temp.setAuthor(author);
					temp.setDownload_state(state);
					docHash.put(String.valueOf(documentid), temp);
				}

			}
			if (documentInfoCursor != null)
				documentInfoCursor.close();

			Cursor documentbindCursor = db.rawQuery(
					"SELECT document_id,title,cover_path,server_id,bookid FROM docbind WHERE document_id IN ("
							+ documentidsSB.toString() + ")", documentids);
			while (documentbindCursor != null
					&& documentbindCursor.moveToNext()) {

				int documentid = documentbindCursor.getInt(0);
				String title = documentbindCursor.getString(1);
				String cover = documentbindCursor.getString(2);
				long serverid = documentbindCursor.getLong(3);
				long bookid = documentbindCursor.getLong(4);
				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null) {
					if (!UiStaticMethod.isEmpty(title)
							&& !UiStaticMethod.isEmpty(cover)) {
						temp.setBookName(title);
						temp.setBookCover(cover);
						MZLog.d("wangguodong", "setServerid"+serverid+"$$$$$$$$$$");
					}
					
					temp.setServerid(serverid);
					if(bookid<=0) bookid=-1;
					temp.setDocument_bookId(bookid);
					
					docHash.put(String.valueOf(documentid), temp);
				}

			}
			if (documentbindCursor != null)
				documentbindCursor.close();

			// 查询document笔记数
			Cursor notenumDocumentCursor = db.rawQuery(
					"select count(*),document_id from "
							+ DataProvider.EBOOKNOTE + " where "
							+ DataProvider.USERID + " = '" + userid
							+ "' AND document_id IN ("
							+ documentidsSB.toString()
							+ ") and ebookid=0 group by document_id",
					documentids);
			while (notenumDocumentCursor != null
					&& notenumDocumentCursor.moveToNext()) {
				long documentid = notenumDocumentCursor.getLong(1);
				int count = notenumDocumentCursor.getInt(0);
				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null) {
					temp.setNote_num(count);
					docHash.put(String.valueOf(documentid), temp);
				}
			}
			if (notenumDocumentCursor != null)
				notenumDocumentCursor.close();
		}

		recentLists.addAll(docHash.values());
		recentLists.addAll(ebookHash.values());
		Collections.sort(recentLists, new Comparator<BookShelfModel>() {

			@Override
			public int compare(BookShelfModel lhs, BookShelfModel rhs) {
				if (lhs.getPercentTime() < rhs.getPercentTime())
					return 1;
				else if (lhs.getPercentTime() > rhs.getPercentTime()) {
					return -1;
				} else
					return 0;
			}
		});
		if (null != recentLists)
			MZLog.d("wanggudong", "最近在读书籍数量" + recentLists.size() + "本");

		return recentLists;
	}
	
	
	synchronized public List<BookShelfModel> listRecentReadingBooks(
			String userid) {
		List<BookShelfModel> recentLists = new ArrayList<BookShelfModel>();
		List<BookShelfModel> ebookList = new ArrayList<BookShelfModel>();
		List<BookShelfModel> documentList = new ArrayList<BookShelfModel>();
		HashMap<String, BookShelfModel> ebookHash = new HashMap<String, BookShelfModel>();
		HashMap<String, BookShelfModel> docHash = new HashMap<String, BookShelfModel>();

		Cursor progressCursor = db.rawQuery("select " + DataProvider.EBOOKID
				+ "," + DataProvider.DOCUMENT_ID + ","
				+ DataProvider.UPDATE_TIME + "," + DataProvider.PERCENT
				+ " from " + DataProvider.PROGRESS + " where "
				+ DataProvider.USERID + " = '" + userid + "' ORDER BY "
				+ DataProvider.UPDATE_TIME + " DESC LIMIT 5 OFFSET 0", null);
		while (progressCursor != null && progressCursor.moveToNext()) {
			int documentid = progressCursor.getInt(1);
			int ebookid = progressCursor.getInt(0);
			long time = progressCursor.getLong(2);
			float percent = progressCursor.getFloat(3);

			BookShelfModel model = new BookShelfModel();
			if (ebookid != 0) {
				model.setServerid(ebookid);
				model.setBookType("ebook");
				// model.setModifiedTime(time);
				model.setPercentTime(time);
				model.setBookPercent((int) (percent * 100));
				ebookHash.put(String.valueOf(ebookid), model);

			} else if (documentid != 0) {
				model.setBookid(documentid);
				model.setBookType("document");
				// model.setModifiedTime(time);
				model.setPercentTime(time);
				model.setBookPercent((int) (percent * 100));
				docHash.put(String.valueOf(documentid), model);
			}
		}
		if (progressCursor != null)
			progressCursor.close();

		// 构造ebook 数组
		if (ebookHash.keySet().size() > 0) {
			String[] book_ids = ebookHash.keySet().toArray(
					new String[ebookHash.keySet().size()]);
			String ebookSB = makePlaceholders(ebookHash.keySet().size());
			Cursor ebookInfoCursor = db.rawQuery(
					"select _id,book_id,title,small_image_url,author,source,big_image_path,add_time from "
							+ DataProvider.EBOOK + " where user_name = '"
							+ userid + "' AND book_id IN ("
							+ ebookSB + ")", book_ids);
			while (ebookInfoCursor != null && ebookInfoCursor.moveToNext()) {
				int _id = ebookInfoCursor.getInt(0);
				long book_id = ebookInfoCursor.getLong(1);
				String title = ebookInfoCursor.getString(2);
				//String cover = ebookInfoCursor.getString(3);
				
				String webcover = ebookInfoCursor.getString(3);
				String localCover = ebookInfoCursor.getString(6);
				String cover="";
				
				long addTime = ebookInfoCursor.getLong(7);
				
				if(addTime<GlobalVarable.LOAD_BOOK_COVER_BY_NEW_WAY_TIME)
					cover=webcover;
				else {
					if(TextUtils.isEmpty(localCover))
						cover=webcover;
					else {
						cover=localCover;
					}
				}
				
				String author = ebookInfoCursor.getString(4);
				String source = ebookInfoCursor.getString(5);
				int label = 0;
				if (source.equals(LocalBook.SOURCE_BORROWED_BOOK))
					label = BookShelfModel.LABEL_BORROWED;
				else if (source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
					label = BookShelfModel.LABEL_CHANGDU;
				} else if (source.equals(LocalBook.SOURCE_TRYREAD_BOOK)) {
					label = BookShelfModel.LABEL_TRYREAD;
				} else {
					label = 0;
				}
				BookShelfModel temp = ebookHash.get(String.valueOf(book_id));
				if (temp != null) {
					temp.setId(_id);
					temp.setBookid(_id);
					temp.setBookName(title);
					temp.setBookCover(cover);
					temp.setAuthor(author);
					temp.setServerid(book_id);
					temp.setBookCoverLabel(label);
					ebookHash.put(String.valueOf(book_id), temp);
				}
			}
			if (ebookInfoCursor != null)
				ebookInfoCursor.close();

			// 查询ebook笔记数
			Cursor notenumEbookCursor = db.rawQuery(
					"select count(*), ebookid from " + DataProvider.EBOOKNOTE
							+ " where " + DataProvider.USERID + " = '" + userid
							+ "' AND ebookid IN (" + ebookSB
							+ ") and document_id = 0 group by ebookid",
					book_ids);
			while (notenumEbookCursor != null
					&& notenumEbookCursor.moveToNext()) {
				int count = notenumEbookCursor.getInt(0);
				long bookid = notenumEbookCursor.getInt(1);
				BookShelfModel temp = ebookHash.get(String.valueOf(bookid));
				if (temp != null) {
					temp.setNote_num(count);
					ebookHash.put(String.valueOf(bookid), temp);
				}
			}
			if (notenumEbookCursor != null)
				notenumEbookCursor.close();
		}

		if (docHash.keySet().size() > 0) {

			// 构造document 数组
			String[] documentids = docHash.keySet().toArray(
					new String[docHash.keySet().size()]);
			String documentSB = makePlaceholders(docHash.keySet().size());
					
			Cursor documentInfoCursor = db.rawQuery(
					"SELECT _id,title,cover_path,author,state_load FROM document WHERE _id IN ("
							+ documentSB + ")", documentids);
			while (documentInfoCursor != null
					&& documentInfoCursor.moveToNext()) {

				int documentid = documentInfoCursor.getInt(0);
				String title = documentInfoCursor.getString(1);
				String cover = documentInfoCursor.getString(2);
				String author = documentInfoCursor.getString(3);
				int state = documentInfoCursor.getInt(4);
				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null) {
					temp.setBookName(title);
					temp.setBookCover(cover);
					temp.setAuthor(author);
					temp.setDownload_state(state);
					docHash.put(String.valueOf(documentid), temp);
				}

			}
			if (documentInfoCursor != null)
				documentInfoCursor.close();

			Cursor documentbindCursor = db.rawQuery(
					"SELECT document_id,title,cover_path FROM docbind WHERE document_id IN ("
							+ documentSB + ")", documentids);
			while (documentbindCursor != null
					&& documentbindCursor.moveToNext()) {

				int documentid = documentbindCursor.getInt(0);
				String title = documentbindCursor.getString(1);
				String cover = documentbindCursor.getString(2);
				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null) {
					if (!UiStaticMethod.isEmpty(title)
							&& !UiStaticMethod.isEmpty(cover)) {
						temp.setBookName(title);
						temp.setBookCover(cover);
						docHash.put(String.valueOf(documentid), temp);
					}
				}

			}
			if (documentbindCursor != null)
				documentbindCursor.close();

			// 查询document笔记数
			Cursor notenumDocumentCursor = db.rawQuery(
					"select count(*),document_id from "
							+ DataProvider.EBOOKNOTE + " where "
							+ DataProvider.USERID + " = '" + userid
							+ "' AND document_id IN ("
							+ documentSB
							+ ") and ebookid=0 group by document_id",
					documentids);
			while (notenumDocumentCursor != null
					&& notenumDocumentCursor.moveToNext()) {
				long documentid = notenumDocumentCursor.getLong(1);
				int count = notenumDocumentCursor.getInt(0);
				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null) {
					temp.setNote_num(count);
					docHash.put(String.valueOf(documentid), temp);
				}
			}
			if (notenumDocumentCursor != null)
				notenumDocumentCursor.close();
		}

		recentLists.addAll(docHash.values());
		recentLists.addAll(ebookHash.values());
		Collections.sort(recentLists, new Comparator<BookShelfModel>() {

			@Override
			public int compare(BookShelfModel lhs, BookShelfModel rhs) {
				if (lhs.getPercentTime() < rhs.getPercentTime())
					return 1;
				else if (lhs.getPercentTime() > rhs.getPercentTime()) {
					return -1;
				} else
					return 0;
			}
		});
		if (null != recentLists)
			MZLog.d("wanggudong", "最近在读书籍数量" + recentLists.size() + "本");

		return recentLists;
	}

	public List<BookShelfModel> getBookShelfBooksByTitle(String title,
			String userid) {

		List<BookShelfModel> bookShelflist = new ArrayList<BookShelfModel>();
		List<BookShelfModel> ebookList = new ArrayList<BookShelfModel>();
		List<BookShelfModel> documentList = new ArrayList<BookShelfModel>();
		BookShelfModel bookshelf = null;
		if (selectBookCount(title, userid) == 0) {
			return null;
		} else {
			String sql = "SELECT _id,title,small_image_url,author,state_load,progress,size,book_id FROM ebook WHERE title like ? and user_name = ? ";
			String[] args = new String[] { "%" + title + "%", userid };
			Cursor result = db.rawQuery(sql, args);
			for (result.moveToFirst(); !result.isAfterLast(); result
					.moveToNext()) {
				bookshelf = new BookShelfModel();
				bookshelf.setId(result.getInt(0));
				bookshelf.setBookid(result.getInt(0));
				bookshelf.setBookName(result.getString(1));
				bookshelf.setBookCover(result.getString(2));
				bookshelf.setAuthor(result.getString(3));
				bookshelf.setDownload_state(result.getInt(4));
				bookshelf.setDownload_progress(result.getLong(5));
				bookshelf.setBook_size(result.getLong(6));
				bookshelf.setBookType("ebook");
				bookshelf.setServerid(result.getLong(7));
				ebookList.add(bookshelf);
			}
			result.close();

			List<String> documentIdList = new ArrayList<String>();
			String sql2 = "select document_id from docbind where title like ? and userid=? ";
			Cursor result2 = db.rawQuery(sql2, args);
			while (result2 != null && result2.moveToNext()) {
				documentIdList.add(String.valueOf(result2.getInt(0)));
			}

			Cursor result1 = null;
			String[] documentids = new String[documentList.size()];
			if (documentList.size() > 0) {
				String documentSB = makePlaceholders(documentList.size());
				String sql1 = "SELECT _id,title,cover_path,author,state_load FROM document WHERE title like '%"
						+ title
						+ "%' or _id  IN ("
						+ documentSB
						+ ")";
				result1 = db.rawQuery(sql1, documentids);
			} else {
				String sql1 = "SELECT _id,title,cover_path,author,state_load FROM document WHERE title like ? ";
				result1 = db.rawQuery(sql1, new String[] { "%" + title + "%" });
			}

			for (result1.moveToFirst(); !result1.isAfterLast(); result1
					.moveToNext()) {
				bookshelf = new BookShelfModel();
				bookshelf.setId(result1.getInt(0));
				bookshelf.setBookid(result1.getInt(0));
				bookshelf.setBookName(result1.getString(1));
				bookshelf.setBookCover(result1.getString(2));
				bookshelf.setAuthor(result1.getString(3));
				bookshelf.setBookType("document");
				bookshelf.setDownload_state(result1.getInt(4));
				documentList.add(bookshelf);
			}
			result1.close();
		}

		if (ebookList.size() > 0) {
			// 构造ebook 数组
			String[] ebookids = new String[ebookList.size()];
			String[] book_ids = new String[ebookList.size()];
			for (int i = 0; i < ebookList.size(); i++) {
				ebookids[i] = String.valueOf(ebookList.get(i).getId());
				book_ids[i] = String.valueOf(ebookList.get(i).getServerid());
			}
			String ebookidsSB = makePlaceholders(ebookList.size());
			
			// 查询ebook进度
			Cursor progressEbookCursor = db.rawQuery(
					"select ebookid,percent,update_time from "
							+ DataProvider.PROGRESS + " where "
							+ DataProvider.USERID + " = '" + userid
							+ "' AND ebookid IN (" + ebookidsSB
							+ ")", book_ids);

			while (progressEbookCursor != null
					&& progressEbookCursor.moveToNext()) {
				int ebookid = progressEbookCursor.getInt(0);
				float percent = progressEbookCursor.getFloat(1);
				long percent_time = progressEbookCursor.getLong(2);
				for (int i = 0; i < ebookList.size(); i++) {
					BookShelfModel temp = ebookList.get(i);
					if (temp.getServerid() == ebookid) {
						temp.setBookPercent(percent);
						temp.setPercentTime(percent_time);
						ebookList.set(i, temp);
						break;
					}
				}

			}
			progressEbookCursor.close();

			// 查询ebook笔记数
			for (int i = 0; i < ebookList.size(); i++) {
				Cursor notenumEbookCursor = db.rawQuery("select count(*) from "
						+ DataProvider.EBOOKNOTE + " where "
						+ DataProvider.USERID + " = '" + userid
						+ "' AND ebookid = ?", new String[] { String
						.valueOf(ebookList.get(i).getServerid()) });
				BookShelfModel temp = ebookList.get(i);
				int count = 0;
				if (notenumEbookCursor.moveToNext()) {
					count = notenumEbookCursor.getInt(0);
				}
				if (notenumEbookCursor != null) {
					temp.setNote_num(count);
					ebookList.set(i, temp);
					notenumEbookCursor.close();
					continue;
				}
			}
		}

		if (documentList.size() > 0) {
			// 构造document 数组
			String[] documentids = new String[documentList.size()];
			for (int i = 0; i < documentList.size(); i++) {
				documentids[i] = String.valueOf(documentList.get(i).getId());
			}
			String documentSB = makePlaceholders(documentList.size());

			// 查询document进度
			Cursor progressDocumentCursor = db.rawQuery(
					"select document_id,percent,update_time from "
							+ DataProvider.PROGRESS + " where "
							+ DataProvider.USERID + " = '" + userid
							+ "' AND document_id IN ("
							+ documentSB + ")", documentids);
			while (progressDocumentCursor != null
					&& progressDocumentCursor.moveToNext()) {
				int documentid = progressDocumentCursor.getInt(0);
				float percent = progressDocumentCursor.getFloat(1);
				long percent_time = progressDocumentCursor.getLong(2);
				for (int i = 0; i < documentList.size(); i++) {
					BookShelfModel temp = documentList.get(i);
					if (temp.getId() == documentid) {
						temp.setBookPercent(percent);
						temp.setPercentTime(percent_time);
						documentList.set(i, temp);
						break;
					}
				}

			}
			progressDocumentCursor.close();

			// 查询document笔记数
			for (int i = 0; i < documentList.size(); i++) {
				Cursor notenumDocumentCursor = db.rawQuery(
						"select count(*) from " + DataProvider.EBOOKNOTE
								+ " where " + DataProvider.USERID + " = '"
								+ userid + "' AND document_id = ?",
						new String[] { String.valueOf(documentList.get(i)
								.getId()) });
				BookShelfModel temp = documentList.get(i);
				int count = 0;
				if (notenumDocumentCursor.moveToNext()) {
					count = notenumDocumentCursor.getInt(0);
				}
				if (notenumDocumentCursor != null) {
					temp.setNote_num(count);
					documentList.set(i, temp);
					notenumDocumentCursor.close();
					continue;
				}
			}
		}

		bookShelflist.addAll(ebookList);
		bookShelflist.addAll(documentList);
		return bookShelflist;
	}

	/**
	 * 读取书架数据信息<br />
	 * 功能步骤：<br />
	 * 1、读取书架表数据信息<br />
	 * 2、读取京东图书基本信息表数据（书名、作者、封面、文件大小、下载状态等）<br />
	 * 3、读取京东图书对应的阅读进度信息（进度、阅读时间等）<br />
	 * 4、读取京东图书对应的笔记条数<br />
	 * 5、读取用户自有图书的阅读进度和阅读时间<br />
	 * 6、读取用户自有图书的笔记条数<br />
	 * 7、读取用户自有图书的基本信息（图书封面、作者等）<br />
	 * 8、读取所有文件夹关联信息<br />
	 * 
	 * @param userid
	 * @param flag 是否包含文件夹 0为包含，1为不包含
	 * @return
	 */
	synchronized public List<BookShelfModel> listBookShelf(String userid,
			int flag) {

		List<BookShelfModel> bookListOfShelf = new ArrayList<BookShelfModel>();
		List<BookShelfModel> folderList = new ArrayList<BookShelfModel>();
		HashMap<String, BookShelfModel> ebookHash = new HashMap<String, BookShelfModel>();
		HashMap<String, BookShelfModel> docHash = new HashMap<String, BookShelfModel>();
		HashMap<String, String> bookHash = new HashMap<String, String>();
		HashMap<String, Map> folderHash = new HashMap<String, Map>();

		Cursor bookShelfCursor = null;
		if (flag == 0)// 查询所有非文件夹书籍
			bookShelfCursor = db.rawQuery("select * from "
					+ DataProvider.BOOKSHELF + " where "
					//非文件夹条件
					+ DataProvider.FOLDER_CONTAINER_FOLDER_ID + " =? AND "
					+ DataProvider.USERID + " = '" + userid + "'",
					new String[] { "-1" });
		else
			bookShelfCursor = db.rawQuery("select * from "
					+ DataProvider.BOOKSHELF + " where "
					+ DataProvider.USERID + " = ?",
					new String[] { userid });
		
		while (null!=  bookShelfCursor  && bookShelfCursor.moveToNext()) {

			int documentid = bookShelfCursor.getInt(2);
			int ebookid = bookShelfCursor.getInt(3);

			BookShelfModel model = new BookShelfModel();
			model.setId(bookShelfCursor.getInt(0));
			model.setModifiedTime(Double.parseDouble(bookShelfCursor.getString(4)));
			model.setBelongDirId(bookShelfCursor.getInt(5));
			if (ebookid != -1) {
				model.setBookid(ebookid);
				model.setBookType("ebook");
				ebookHash.put(String.valueOf(ebookid), model);

			} else if (documentid != -1) {
				model.setBookid(documentid);
				model.setBookType("document");
				docHash.put(String.valueOf(documentid), model);
			}
		}
		bookShelfCursor.close();

		//从数据库中取出图书信息（京东图书）
		if (ebookHash.keySet().size() > 0) {
			// 构造ebook 数组
			String[] book_ids = new String[ebookHash.keySet().size()];
			String ebookidsSB = makePlaceholders(ebookHash.keySet().size());
			// 获取bookid
			Cursor ebookInfoCursor = db.rawQuery("select _id,title,small_image_url,author,state_load,progress,size,source,book_id,big_image_path,add_time from "
					+ DataProvider.EBOOK + " where user_name = '" + userid
					+ "' AND _id IN (" + ebookidsSB + ")",  ebookHash.keySet().toArray(
		                    new String[ebookHash.keySet().size()]));
			while (ebookInfoCursor != null && ebookInfoCursor.moveToNext()) {
				
				int _id = ebookInfoCursor.getInt(0);
				String title = ebookInfoCursor.getString(1);
				String webcover = ebookInfoCursor.getString(2);
				String localCover = ebookInfoCursor.getString(9);
				String cover="";
				
				long addTime = ebookInfoCursor.getLong(10);
				
				if(addTime<GlobalVarable.LOAD_BOOK_COVER_BY_NEW_WAY_TIME)
					cover=webcover;
				else {
					if(TextUtils.isEmpty(localCover))
						cover=webcover;
					else {
						cover=localCover;
					}
				}
				
				String author = ebookInfoCursor.getString(3);
				int state = ebookInfoCursor.getInt(4);
				long progress = ebookInfoCursor.getLong(5);
				long size = ebookInfoCursor.getLong(6);
				String source = ebookInfoCursor.getString(7);
				
				long book_id = ebookInfoCursor.getLong(8);
				
				if(!TextUtils.isEmpty(source) && !TextUtils.isEmpty(String.valueOf(book_id))&& !TextUtils.isEmpty(userid) 
						&& LocalBook.alreadyEncrypt(source)){
					
					try {
						source = SecretKeyUtil.decrypt(String.valueOf(book_id), userid, source);
					} catch (UnsatisfiedLinkError e) {
						e.printStackTrace();
					}
					
				}
				
				if (book_id > 0) {
					int label = 0;
					if (source.equals(LocalBook.SOURCE_BORROWED_BOOK)){
						label = BookShelfModel.LABEL_BORROWED;
					}else if (source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK)) {
						label = BookShelfModel.LABEL_USER_BORROWED;
					}else if (source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
						label = BookShelfModel.LABEL_CHANGDU;
					} else if (source.equals(LocalBook.SOURCE_TRYREAD_BOOK)) {
						label = BookShelfModel.LABEL_TRYREAD;
					} else {
						label = 0;
					}
					BookShelfModel temp = ebookHash.get(String.valueOf(_id));
					temp.setBookName(title);
					temp.setBookCover(cover);
					temp.setAuthor(author);
					temp.setDownload_state(state);
					temp.setDownload_progress(progress);
					temp.setBook_size(size);
					temp.setBookCoverLabel(label);
					temp.setServerid(book_id);
					ebookHash.put(String.valueOf(_id), temp);
					bookHash.put(String.valueOf(book_id),String.valueOf(_id));
				}
				else {
					ebookHash.remove(String.valueOf(_id));
				}
			}
			ebookInfoCursor.close();
			
			if (bookHash.keySet().size()>0) {  //protect from invalid record in bookshelf but not in ebook
				String bookidsSB = makePlaceholders(bookHash.keySet().size());
				
	            book_ids = bookHash.keySet().toArray(new String[bookHash.keySet().size()]);
				// 查询ebook阅读进度
				Cursor progressEbookCursor = db.rawQuery(
						"select _id,ebookid,percent,update_time from "
								+ DataProvider.PROGRESS + " where "
								+ DataProvider.USERID + " = '" + userid
								+ "' AND ebookid IN (" + bookidsSB.toString()
								+ ")", book_ids);
				while (progressEbookCursor != null
						&& progressEbookCursor.moveToNext()) {
	
					int book_id = progressEbookCursor.getInt(1);
					float percent = progressEbookCursor.getFloat(2);
					double update_time = progressEbookCursor.getDouble(3);
					BookShelfModel temp = ebookHash.get(bookHash.get(String.valueOf(book_id)));
					if (temp!=null && temp.getServerid() == book_id) {
						temp.setBookPercent((int) (percent * 100));
						temp.setPercentTime(update_time);
						ebookHash.put(bookHash.get(String.valueOf(book_id)), temp);
					}
	
				}
				progressEbookCursor.close();
	
				// 查询ebook笔记数
				Cursor notenumEbookCursor = db.rawQuery("select count(*),ebookid from "
							+ DataProvider.EBOOKNOTE + " where "
							+ DataProvider.USERID + " = '" + userid
							+ "' AND ebookid IN (" + bookidsSB.toString()
								+ ")  AND document_id = 0 group by ebookid",book_ids);
				while (notenumEbookCursor != null
						&& notenumEbookCursor.moveToNext()) {
					long book_id = notenumEbookCursor.getInt(1);
					BookShelfModel temp =  ebookHash.get(bookHash.get(String.valueOf(book_id)));
					int count = notenumEbookCursor.getInt(0);
					if (temp != null) {
						temp.setNote_num(count);
						ebookHash.put(bookHash.get(String.valueOf(book_id)), temp);
					}
	
				}
				notenumEbookCursor.close();
			}
		}

		//自有图书信息（阅读进度）
		if (docHash.keySet().size() > 0) {
			// 构造document 数组
			String[] documentids = docHash.keySet().toArray(
					new String[docHash.keySet().size()]);
			String documentidsSB = makePlaceholders(docHash.keySet().size());
			// 查询document进度
			Cursor progressDocumentCursor = db.rawQuery(
					"select _id,document_id,percent,update_time from "
							+ DataProvider.PROGRESS + " where "
							+ DataProvider.USERID + " = '" + userid
							+ "' AND document_id IN ("
							+ documentidsSB + ")", documentids);
			while (null!= progressDocumentCursor 
					&& progressDocumentCursor.moveToNext()) {
				int documentid = progressDocumentCursor.getInt(1);
				float percent = progressDocumentCursor.getFloat(2);
				double update_time = progressDocumentCursor.getDouble(3);

				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null) {
					temp.setBookPercent((int) (percent * 100));
					temp.setPercentTime(update_time);
					docHash.put(String.valueOf(documentid), temp);
				}
			}

			progressDocumentCursor.close();

			// 查询document笔记数
			Cursor notenumDocumentCursor = db
					.rawQuery(
							"select count(*),document_id from "
									+ DataProvider.EBOOKNOTE
									+ " where "
									+ DataProvider.USERID
									+ " = '"
									+ userid
									+ "' AND document_id IN (" + documentidsSB
							+ ") and ebookid=0 group by document_id",
							documentids);
			while (notenumDocumentCursor != null
					&& notenumDocumentCursor.moveToNext()) {
				int documentid = notenumDocumentCursor.getInt(1);
				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				int count = notenumDocumentCursor.getInt(0);
				if (temp != null) {
					temp.setNote_num(count);
					docHash.put(String.valueOf(documentid), temp);
				}
			}
			notenumDocumentCursor.close();

			Cursor documentInfoCursor = db
					.rawQuery(
							"SELECT _id,title,cover_path,author,state_load,progress,size FROM document WHERE _id IN ("
									+ documentidsSB + ")",
							documentids);
			while (documentInfoCursor != null
					&& documentInfoCursor.moveToNext()) {

				int documentid = documentInfoCursor.getInt(0);
				String title = documentInfoCursor.getString(1);
				String cover = documentInfoCursor.getString(2);
				String author = documentInfoCursor.getString(3);

				int state = documentInfoCursor.getInt(4);
				long progress = documentInfoCursor.getLong(5);
				long size = documentInfoCursor.getLong(6);

				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null) {
					temp.setBookName(title);
					temp.setBookCover(cover);
					temp.setAuthor(author);
					temp.setDownload_state(state);
					temp.setDownload_progress(progress);
					temp.setBook_size(size);
					docHash.put(String.valueOf(documentid), temp);
				}
			}
			documentInfoCursor.close();

			Cursor documentbindCursor = db.rawQuery(
					"SELECT document_id,title,cover_path FROM docbind WHERE document_id IN ("
							+ documentidsSB + ")", documentids);
			while (null!= documentbindCursor  && documentbindCursor.moveToNext()) {


				int documentid = documentbindCursor.getInt(0);
				String title = documentbindCursor.getString(1);
				String cover = documentbindCursor.getString(2);

				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null && !UiStaticMethod.isEmpty(title)
						&& !UiStaticMethod.isEmpty(cover)) {
					temp.setBookName(title);
					temp.setBookCover(cover);
					docHash.put(String.valueOf(documentid), temp);
				}

			}
			documentbindCursor.close();
		}

		// 得到所有文件夹id以及包含书籍统计
		Cursor folderCursor = db.rawQuery(
				"select folder_dir_id,count(folder_dir_id) from "
						+ DataProvider.BOOKSHELF + " where "
						+ DataProvider.FOLDER_CONTAINER_FOLDER_ID + " !=? AND "
						+ DataProvider.USERID + " = '" + userid + "' group by "
						+ DataProvider.FOLDER_CONTAINER_FOLDER_ID,
				new String[] { "-1" });
		while (folderCursor != null && folderCursor.moveToNext()) {
			int folderid = folderCursor.getInt(0);
			int count = folderCursor.getInt(1);
			Map<String, String> map = new HashMap<String, String>();
			map.put("folderid", String.valueOf(folderid));
			map.put("count", String.valueOf(count));
			folderHash.put(String.valueOf(folderid),map);
		}
		folderCursor.close();

		// 构造folder 数组
		if (folderHash.keySet().size() > 0) {
			String[] folderids = folderHash.keySet().toArray(new String[folderHash.keySet().size()]);
			String foldersSB = makePlaceholders(folderids.length);
			
			
			// 查询文件夹信息
			Cursor folderInfoCursor = db.rawQuery("select * from  "
					+ DataProvider.FOLDER + " where " + DataProvider.FOLDER_ID
					+ " IN (" + foldersSB + ")", folderids);
			while (null!= folderInfoCursor && folderInfoCursor.moveToNext()) {
				int folderid = folderInfoCursor.getInt(0);
				String folderName = folderInfoCursor.getString(1);
				double folderTime = Double.parseDouble(folderInfoCursor.getString(2));
				BookShelfModel model = new BookShelfModel();
				if (folderHash.get(String.valueOf(folderid))!=null) {
					String count = (String)folderHash.get(String.valueOf(folderid)).get("count");
					model.setDirBookCount(Integer.parseInt(count));
				}
				model.setBookType("folder");
				model.setBookid(folderid);
				model.setModifiedTime(folderTime);
				model.setBelongDirId(-1);// 表示为文件夹
				model.setBookName(folderName);
				model.setBookCover("null");
				folderList.add(model);
			}
			folderInfoCursor.close();
		}

		if (flag == 0) {
			bookListOfShelf.addAll(ebookHash.values());
			bookListOfShelf.addAll(docHash.values());
			bookListOfShelf.addAll(folderList);
		} else {
			bookListOfShelf.addAll(ebookHash.values());
			bookListOfShelf.addAll(docHash.values());
		}
		return bookListOfShelf;
	}

	synchronized public List<BookShelfModel> listAllBookShelf(String userid) {

		List<BookShelfModel> recentLists = new ArrayList<BookShelfModel>();
		List<BookShelfModel> ebookList = new ArrayList<BookShelfModel>();
		List<BookShelfModel> documentList = new ArrayList<BookShelfModel>();

		// 查询所有非文件夹书籍
		Cursor bookShelfCursor = db.rawQuery("select * from "
				+ DataProvider.BOOKSHELF + " where " + DataProvider.USERID
				+ " = '" + userid + "'", null);
		while (null!= bookShelfCursor  && bookShelfCursor.moveToNext()) {
			int documentid = bookShelfCursor.getInt(2);
			int ebookid = bookShelfCursor.getInt(3);

			BookShelfModel model = new BookShelfModel();
			model.setId(bookShelfCursor.getInt(0));
			model.setModifiedTime(Double.parseDouble(bookShelfCursor
					.getString(4)));
			model.setBelongDirId(bookShelfCursor.getInt(5));
			if (ebookid != -1) {
				model.setBookid(ebookid);
				model.setBookType("ebook");
				ebookList.add(model);

			} else if (documentid != -1) {
				model.setBookid(documentid);
				model.setBookType("document");
				documentList.add(model);
			}
		}
		bookShelfCursor.close();

		// 构造ebook 数组
		String[] ebookids = new String[ebookList.size()];
		for (int i = 0; i < ebookList.size(); i++) {
			ebookids[i] = String.valueOf(ebookList.get(i).getBookid());
		}
		String ebookidsSB = makePlaceholders(ebookList.size());
		

		// 构造document 数组
		String[] documentids = new String[documentList.size()];
		for (int i = 0; i < documentList.size(); i++) {
			documentids[i] = String.valueOf(documentList.get(i).getBookid());
		}
		String documentidsSB =  makePlaceholders(documentList.size());
		
		Cursor ebookInfoCursor = db.rawQuery(
				"SELECT _id,title,small_image_url FROM ebook WHERE _id IN ("
						+ ebookidsSB + ")", ebookids);
		while (null!= ebookInfoCursor  && ebookInfoCursor.moveToNext()) {

			int ebookid = ebookInfoCursor.getInt(0);
			;
			String title = ebookInfoCursor.getString(1);
			String cover = ebookInfoCursor.getString(2);
			// MZLog.d("wangguodong", "ebook title=" + title + "cover=" +
			// cover);
			for (int j = 0; j < ebookList.size(); j++) {
				if (ebookList.get(j).getBookid() == ebookid) {
					BookShelfModel temp = ebookList.get(j);
					temp.setBookName(title);
					temp.setBookCover(cover);
					ebookList.set(j, temp);
					break;
				}
			}

		}

		ebookInfoCursor.close();

		Cursor documentInfoCursor = db.rawQuery(
				"SELECT _id,title,cover_path FROM document WHERE _id IN ("
						+ documentidsSB + ")", documentids);
		while (null!= documentInfoCursor  && documentInfoCursor.moveToNext()) {

			int documentid = documentInfoCursor.getInt(0);
			;
			String title = documentInfoCursor.getString(1);
			String cover = documentInfoCursor.getString(2);

			MZLog.d("wangguodong", "document title=" + title + "cover=" + cover);

			for (int j = 0; j < documentList.size(); j++) {
				if (documentList.get(j).getBookid() == documentid) {
					BookShelfModel temp = documentList.get(j);
					temp.setBookName(title);
					temp.setBookCover(cover);
					documentList.set(j, temp);
					break;
				}
			}

		}
		documentInfoCursor.close();

		Cursor documentbindCursor = db.rawQuery(
				"SELECT document_id,title,cover_path FROM docbind WHERE document_id IN ("
						+ documentidsSB + ")", documentids);
		while (null!= documentbindCursor && documentbindCursor.moveToNext()) {

			int documentid = documentbindCursor.getInt(0);
			String title = documentbindCursor.getString(1);
			String cover = documentbindCursor.getString(2);

			for (int j = 0; j < documentList.size(); j++) {
				if (documentList.get(j).getBookid() == documentid) {
					BookShelfModel temp = documentList.get(j);
					if (!UiStaticMethod.isEmpty(title)
							&& !UiStaticMethod.isEmpty(cover)) {
						temp.setBookName(title);
						temp.setBookCover(cover);
						documentList.set(j, temp);
						break;
					}

				}
			}

		}
		documentbindCursor.close();

		recentLists.addAll(ebookList);
		recentLists.addAll(documentList);

		return recentLists;
	}

	synchronized public List<Map<String, String>> getCacheUrls(Context context,String userid) {
		MZLog.d("performance", "getCacheUrls");
		List<Map<String, String>> urls = new ArrayList<Map<String, String>>();
		//京东书籍
		Cursor bookShelfCursor = db.rawQuery("select ebook.small_image_url,bookshelf.folder_dir_id from ebook,bookshelf where ebook._id=bookshelf.ebook_id and bookshelf." + DataProvider.USERID
				+ " = '" + userid + "' and document_id=-1", null);
		while (bookShelfCursor != null && bookShelfCursor.moveToNext()) {
			String cover = bookShelfCursor.getString(0);
			int folder_dir_id = bookShelfCursor.getInt(1);
			if (!UiStaticMethod.isEmpty(cover) && cover.startsWith("http://") && !urls.contains(cover)) {
				Map<String, String> map = new HashMap<String, String>();
				if (folder_dir_id==-1) {
					map.put("type", "book");
					map.put("url", cover);
				}
				else {
					map.put("type", "book");
					map.put("url", cover);
					urls.add(map);
				}
				urls.add(map);
			}
		}
		bookShelfCursor.close();
		
		//用户自导入书籍
		Cursor docBindCursor = db.rawQuery("select docbind.cover_path,bookshelf.folder_dir_id from docbind,bookshelf where docbind.document_id=bookshelf.document_id and bookshelf." + DataProvider.USERID
				+ " = '" + userid + "' and ebook_id=-1", null);
		while (docBindCursor != null && docBindCursor.moveToNext()) {
			String cover = docBindCursor.getString(0);
			int folder_dir_id = docBindCursor.getInt(1);
			if (!UiStaticMethod.isEmpty(cover) && cover.startsWith("http://") && !urls.contains(cover)) {
				Map<String, String> map = new HashMap<String, String>();
				if (folder_dir_id==-1) {
					map.put("type", "book");
					map.put("url", cover);
				}
				else {
					map.put("type", "book");
					map.put("url", cover);
					urls.add(map);
				}
				urls.add(map);
			}
		}
		docBindCursor.close();
		return urls;
	}

	synchronized public void updateBookshelfTime(BookShelfModel model) {

		if (model.getBookType().equals("folder")) {

			ContentValues values = new ContentValues();
			values.put(DataProvider.FOLDER_CHANGETIME, model.getModifiedTime());
			db.update(DataProvider.FOLDER, values, DataProvider.FOLDER_ID
					+ " = ?",
					new String[] { String.valueOf(model.getBookid()) });
			MZLog.d("wangguodong", "保存文件夹时间" + model.getModifiedTime());
		} else {
			ContentValues values = new ContentValues();
			values.put(DataProvider.BOOKSHELF_CHANGE_TIME,
					model.getModifiedTime());
			db.update(DataProvider.BOOKSHELF, values, DataProvider.BOOKSHELF_ID
					+ " = ?", new String[] { String.valueOf(model.getId()) });
			MZLog.d("wangguodong", "保存书籍时间" + model.getModifiedTime());
		}

		MZLog.d("wangguodong", "更新书架书籍修改时间");
	}

	synchronized public void updateBookshelfFolder(BookShelfModel model) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.FOLDER_CONTAINER_FOLDER_ID,
				model.getBelongDirId());
		values.put(DataProvider.BOOKSHELF_CHANGE_TIME, model.getModifiedTime());
		db.update(DataProvider.BOOKSHELF, values, DataProvider.BOOKSHELF_ID
				+ " = ?", new String[] { String.valueOf(model.getId()) });
		MZLog.d("wangguodong", "更新书籍所属文件夹");
	}
	

	
	

	// 增加书签
	synchronized public int addBookMark(BookMark mark) {
		int id = -1;
		Cursor cursor1 = db.rawQuery(
				"SELECT " + DataProvider.BOOKMARK_ID + " FROM "
						+ DataProvider.BOOKMARK_TABLE + " WHERE  "
						+ DataProvider.BOOKMARK_EBOOKID + " =? AND "
						+ DataProvider.BOOKMARK_DOCID + " =? AND "
						+ DataProvider.BOOKMARK_USERID + " =? AND "
						+ DataProvider.BOOKMARK_CHAPTER_ITEMREF + " =? AND "
						+ DataProvider.BOOKMARK_PARA_INDEX + " =? AND "
						+ DataProvider.BOOKMARK_OFFSET_IN_PARA + " =? AND "
						+ DataProvider.BOOKMARK_PDF_PAGE + " =? AND "
						+ DataProvider.BOOK_TYPE + " =? ",
				new String[] { String.valueOf(mark.ebookid),
						String.valueOf(mark.docid),
						String.valueOf(mark.userid), mark.chapter_itemref,
						String.valueOf(mark.para_index),
						String.valueOf(mark.offset_in_para),
						String.valueOf(mark.pdf_page),
						String.valueOf(mark.bookType) });

		if (cursor1 != null) {
			while (cursor1.moveToNext()) {
				id = cursor1.getInt(0);
				mark.id = id;
				updateBookMark(mark);
				break;
			}
			cursor1.close();
		}
		if (id != -1)
			return id;

		if (id == -1 || cursor1 == null) {
			ContentValues values = new ContentValues();
			values.put(DataProvider.BOOKMARK_EBOOKID, mark.ebookid);
			values.put(DataProvider.BOOKMARK_DOCID, mark.docid);
			values.put(DataProvider.BOOK_TYPE, mark.bookType);
			values.put(DataProvider.BOOKMARK_CHAPTER_TITLE, mark.chapter_title);
			values.put(DataProvider.BOOKMARK_CHAPTER_ITEMREF,
					mark.chapter_itemref);
			values.put(DataProvider.BOOKMARK_OFFSET_IN_PARA,
					mark.offset_in_para);
			values.put(DataProvider.BOOKMARK_PARA_INDEX, mark.para_index);
			values.put(DataProvider.BOOKMARK_DIGEST, mark.digest);

			values.put(DataProvider.BOOKMARK_UPDATE_AT, mark.updated_at);
			values.put(DataProvider.BOOKMARK_SERVERID, mark.server_id);
			values.put(DataProvider.BOOKMARK_USERID, mark.userid);
			values.put(DataProvider.CREATION_TIME, mark.createTime);
			values.put(DataProvider.IS_SYNC, mark.isSync);
			values.put(DataProvider.OPERATION_STATE, mark.operation_state);
			values.put(DataProvider.BOOKMARK_PDF_PAGE, mark.pdf_page);

			db.insertWithOnConflict(DataProvider.BOOKMARK_TABLE, null, values,
					SQLiteDatabase.CONFLICT_REPLACE);
			Cursor cursor = db.rawQuery("select last_insert_rowid() from "
					+ DataProvider.BOOKMARK_TABLE, null);
			cursor.moveToFirst();
			id = cursor.getInt(0);
			cursor.close();
		}

		return id;
	}

	// 增加同步书签
	@Deprecated
	synchronized public int addSyncBookMark(BookMark mark) {

		int bookmarkid = -1;
		Cursor cursor = db.rawQuery(
				"select " + DataProvider.BOOKMARK_ID + " from "
						+ DataProvider.BOOKMARK_TABLE + " WHERE "
						+ DataProvider.BOOKMARK_SERVERID + " = ? AND "
						+ DataProvider.BOOKMARK_USERID + " =? ",
				new String[] { String.valueOf(mark.server_id),
						String.valueOf(mark.userid) });

		if (cursor == null || cursor.getCount() <= 0) {
			bookmarkid = addBookMark(mark);
			MZLog.d("wangguodong", "书签同步增加成功 id=" + bookmarkid);
			return bookmarkid;
		} else {
			cursor.moveToFirst();
			bookmarkid = cursor.getInt(0);
			cursor.close();
			mark.id = bookmarkid;
			updateBookMark(mark);
			MZLog.d("wangguodong", "书签同步更新成功 id=" + bookmarkid);
		}

		return bookmarkid;
	}

	synchronized public void saveDocumentState(int state, int documentid) {

		ContentValues values = new ContentValues();
		values.put("state_load", state);

		db.update(DataProvider.DOCUMENT, values, DataProvider._ID + " = ?",
				new String[] { String.valueOf(documentid) });

	}

	synchronized public void saveDocumentBookState(int state, int documentid) {

		ContentValues values = new ContentValues();
		values.put("book_state", state);

		db.update(DataProvider.DOCUMENT, values, DataProvider._ID + " = ?",
				new String[] { String.valueOf(documentid) });

	}

	synchronized public List<LocalDocument> getLocalDocumentList(String userpin) {

		String[] selectionArgs = new String[] { userpin };
		Cursor cursor = db
				.rawQuery(
						"SELECT document._id,document.title,document.author,"
								+ "document.book_path,document.book_type,document.cover_path,document.size,"
								+ "document.progress,document.state_load,document.book_state,document.add_at,"
								+ "document.access_time,document.mod_time,"
								+ "document.opf_md5,docbind.bind,docbind.userid,docbind.server_id,docbind.bookid,"
								+ "docbind.title,docbind.author,docbind.cover_path,document.book_source FROM docbind,document WHERE document._id = docbind.document_id and docbind.userid =?",
						selectionArgs);

		List<LocalDocument> list = new ArrayList<LocalDocument>();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				LocalDocument doc = new LocalDocument();
				doc._id = cursor.getInt(0);
				doc.title = cursor.getString(1);
				doc.author = cursor.getString(2);
				doc.book_path = cursor.getString(3);
				doc.format = cursor.getInt(4);
				doc.localImageUrl = cursor.getString(5);
				doc.size = cursor.getLong(6);
				doc.progress = cursor.getLong(7);
				doc.state = cursor.getInt(8);
				doc.bookState = cursor.getInt(9);
				doc.add_time = cursor.getInt(10);
				doc.access_time = cursor.getInt(11);
				doc.mod_time = cursor.getInt(12);
				doc.opf_md5 = cursor.getString(13);
				doc.bind = cursor.getInt(14);
				doc.user_id = cursor.getString(15);
				doc.server_id = cursor.getLong(16);
				doc.bookid = cursor.getLong(17);
				doc.serverTitle = cursor.getString(18);
				doc.serverAuthor = cursor.getString(19);
				doc.serverImageUrl = cursor.getString(20);
				doc.bookAbsolutePath=cursor.getString(21);
				list.add(doc);
			}
			cursor.close();
		}

		return list;
	}

	synchronized public LocalDocument getLocalDocument(int documentid) {

		boolean isSuccess = false;

		String[] selectionArgs = new String[] { String.valueOf(documentid) };
		Cursor cursor = db
				.rawQuery(
						"SELECT document._id,document.title,document.author,"
								+ "document.book_path,document.book_type,document.cover_path,document.size,"
								+ "document.progress,document.state_load,document.book_state,document.add_at,"
								+ "document.access_time,document.mod_time,"
								+ "document.opf_md5,docbind.bind,docbind.userid,docbind.server_id,docbind.bookid,"
								+ "docbind.title,docbind.author,docbind.cover_path FROM docbind,document WHERE document._id = docbind.document_id and document._id =?",
						selectionArgs);
		LocalDocument doc = null;
		if (cursor != null && cursor.moveToFirst()) {
			doc = new LocalDocument();
			doc._id = cursor.getInt(0);
			doc.title = cursor.getString(1);
			doc.author = cursor.getString(2);
			doc.book_path = cursor.getString(3);
			doc.format = cursor.getInt(4);
			doc.localImageUrl = cursor.getString(5);
			doc.size = cursor.getLong(6);
			doc.progress = cursor.getLong(7);
			doc.state = cursor.getInt(8);
			doc.bookState = cursor.getInt(9);
			doc.add_time = cursor.getInt(10);
			doc.access_time = cursor.getInt(11);
			doc.mod_time = cursor.getInt(12);
			doc.opf_md5 = cursor.getString(13);
			doc.bind = cursor.getInt(14);
			doc.user_id = cursor.getString(15);
			doc.server_id = cursor.getLong(16);
			doc.bookid = cursor.getLong(17);
			doc.serverTitle = cursor.getString(18);
			doc.serverAuthor = cursor.getString(19);
			doc.serverImageUrl = cursor.getString(20);

		}
		if (cursor != null)
			cursor.close();

		return doc;
	}

	synchronized public LocalDocument getLocalDocumentByServerid(long serverid,
			String userpin) {

		String[] selectionArgs = new String[] { String.valueOf(serverid),
				userpin };
		Cursor cursor = db
				.rawQuery(
						"SELECT document._id,document.title,document.author,"
								+ "document.book_path,document.book_type,document.cover_path,document.size,"
								+ "document.progress,document.state_load,document.book_state,document.add_at,"
								+ "document.access_time,document.mod_time,"
								+ "document.opf_md5,docbind.bind,docbind.userid,docbind.server_id,docbind.bookid,"
								+ "docbind.title,docbind.author,docbind.cover_path FROM docbind,document WHERE document._id = docbind.document_id and docbind.server_id=? and docbind.userid = ?",
						selectionArgs);
		LocalDocument doc = null;
		if (cursor != null && cursor.moveToFirst()) {
			doc = new LocalDocument();
			doc._id = cursor.getInt(0);
			doc.title = cursor.getString(1);
			doc.author = cursor.getString(2);
			doc.book_path = cursor.getString(3);
			doc.format = cursor.getInt(4);
			doc.localImageUrl = cursor.getString(5);
			doc.size = cursor.getLong(6);
			doc.progress = cursor.getLong(7);
			doc.state = cursor.getInt(8);
			doc.bookState = cursor.getInt(9);
			doc.add_time = cursor.getInt(10);
			doc.access_time = cursor.getInt(11);
			doc.mod_time = cursor.getInt(12);
			doc.opf_md5 = cursor.getString(13);
			doc.bind = cursor.getInt(14);
			doc.user_id = cursor.getString(15);
			doc.server_id = cursor.getLong(16);
			doc.bookid = cursor.getLong(17);
			doc.serverTitle = cursor.getString(18);
			doc.serverAuthor = cursor.getString(19);
			doc.serverImageUrl = cursor.getString(20);

		}
		if (cursor != null)
			cursor.close();

		return doc;
	}
	
	synchronized public List<LocalBook> getLocalBooks(String userpin, String[] book_ids) {
		ArrayList<LocalBook> list = new ArrayList<LocalBook>();
		if (book_ids.length>0) {
			String fields = TextUtils.join(",",LocalBook.ALL_PROJECTION);
			String idSB = this.makePlaceholders(book_ids.length);
			Cursor cursor = null;
			if (userpin!=null)
				cursor = db.rawQuery("select "+fields+" from " + DataProvider.EBOOK +" where book_id in (" + idSB +") and user_name= '"+ userpin +"'",book_ids);
			else
				cursor = db.rawQuery("select "+fields+" from " + DataProvider.EBOOK +" where book_id in (" + idSB +") ",book_ids);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					list.add(LocalBook.getLocalBook(cursor));
				}
			}
			
		}
		return list;
		
	}

	synchronized public LocalDocument getLocalDocument(int documentid,
			String userpin) {

		String[] selectionArgs = new String[] { String.valueOf(documentid),
				userpin };
		Cursor cursor = db
				.rawQuery(
						"SELECT document._id,document.title,document.author,"
								+ "document.book_path,document.book_type,document.cover_path,document.size,"
								+ "document.progress,document.state_load,document.book_state,document.add_at,"
								+ "document.access_time,document.mod_time,"
								+ "document.opf_md5,docbind.bind,docbind.userid,docbind.server_id,docbind.bookid,"
								+ "docbind.title,docbind.author,docbind.cover_path FROM docbind,document WHERE document._id = docbind.document_id and document._id =? and docbind.userid = ?",
						selectionArgs);
		LocalDocument doc = null;
		if (cursor != null && cursor.moveToFirst()) {
			doc = new LocalDocument();
			doc._id = cursor.getInt(0);
			doc.title = cursor.getString(1);
			doc.author = cursor.getString(2);
			doc.book_path = cursor.getString(3);
			doc.format = cursor.getInt(4);
			doc.localImageUrl = cursor.getString(5);
			doc.size = cursor.getLong(6);
			doc.progress = cursor.getLong(7);
			doc.state = cursor.getInt(8);
			doc.bookState = cursor.getInt(9);
			doc.add_time = cursor.getInt(10);
			doc.access_time = cursor.getInt(11);
			doc.mod_time = cursor.getInt(12);
			doc.opf_md5 = cursor.getString(13);
			doc.bind = cursor.getInt(14);
			doc.user_id = cursor.getString(15);
			doc.server_id = cursor.getLong(16);
			doc.bookid = cursor.getLong(17);
			doc.serverTitle = cursor.getString(18);
			doc.serverAuthor = cursor.getString(19);
			doc.serverImageUrl = cursor.getString(20);

		}
		if (cursor != null)
			cursor.close();

		return doc;
	}

	synchronized public long saveLocalDocument(LocalDocument document) {

		long index = -1;
		db.beginTransaction();
		try {

			Document doc = new Document();
			doc.title = document.title;
			doc.author = document.author;

			MZLog.d("wangguodong", "数据库保存type" + document.format);

			doc.format = document.format;
			doc.coverPath = document.localImageUrl;
			doc.bookPath = document.book_path;
			if (!TextUtils.isEmpty(document.bookSource))
				doc.bookSource = document.bookSource;
			doc.size = document.size;
			doc.progress = document.progress;
			doc.state = document.state;
			doc.bookState = document.bookState;
			doc.addAt = document.add_time;
			doc.access_time = document.access_time;
			doc.mod_time = document.mod_time;
			doc.opfMD5 = document.opf_md5;
			index = insertToDocument(doc);

			DocBind docBind = new DocBind();
			docBind.documentId = (int) index;
			docBind.bind = document.bind;
			docBind.serverId = document.server_id;
			docBind.userId = document.user_id;
			docBind.bookId = document.bookid;
			docBind.serverAuthor = document.serverAuthor;
			docBind.serverCover = document.serverImageUrl;
			docBind.serverTitle = document.serverTitle;
			insertOrUpdateDocBind(docBind);

			db.setTransactionSuccessful();
		} catch (Exception e) {
			index = -1;
			e.printStackTrace();

		} finally {
			db.endTransaction();
		}
		return index;
	}

	synchronized public long saveDocumentOnly(LocalDocument document) {

		long index = -1;
		try {

			Document doc = new Document();
			doc.title = document.title;
			doc.author = document.author;
			doc.format = document.format;
			doc.coverPath = document.localImageUrl;
			doc.bookPath = document.book_path;
			if (!TextUtils.isEmpty(document.bookSource))
				doc.bookSource = document.bookSource;
			doc.size = document.size;
			doc.progress = document.progress;
			doc.state = document.state;
			doc.bookState = document.bookState;
			doc.addAt = document.add_time;
			doc.access_time = document.access_time;
			doc.mod_time = document.mod_time;
			doc.opfMD5 = document.opf_md5;
			index = insertToDocument(doc);

		} catch (Exception e) {
			index = -1;
			e.printStackTrace();

		}
		return index;
	}

	synchronized public void insertOrUpdateBookMarks(BookMark mark) {
		if (mark.ebookid == 0 && mark.docid == 0) {
			return;
		}
		ContentValues values = new ContentValues();
		values.put(DataProvider.BOOKMARK_EBOOKID, mark.ebookid);
		values.put(DataProvider.BOOKMARK_DOCID, mark.docid);
		values.put(DataProvider.BOOK_TYPE, mark.bookType);
		values.put(DataProvider.BOOKMARK_CHAPTER_TITLE, mark.chapter_title);
		values.put(DataProvider.BOOKMARK_CHAPTER_ITEMREF, mark.chapter_itemref);
		values.put(DataProvider.BOOKMARK_OFFSET_IN_PARA, mark.offset_in_para);
		values.put(DataProvider.BOOKMARK_PARA_INDEX, mark.para_index);
		values.put(DataProvider.BOOKMARK_DIGEST, mark.digest);
		values.put(DataProvider.BOOKMARK_UPDATE_AT, mark.updated_at);
		values.put(DataProvider.BOOKMARK_SERVERID, mark.server_id);
		values.put(DataProvider.BOOKMARK_USERID, mark.userid);
		values.put(DataProvider.CREATION_TIME, mark.createTime);
		values.put(DataProvider.IS_SYNC, mark.isSync);
		values.put(DataProvider.OPERATION_STATE, mark.operation_state);
		values.put(DataProvider.BOOKMARK_PDF_PAGE, mark.pdf_page);

		db.insertWithOnConflict(DataProvider.BOOKMARK_TABLE, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

	}

	// 使用这个方法删除书签
	synchronized public boolean deleteBookMarkByUpdate(int bookMarkId) {

		ContentValues values = new ContentValues();
		values.put(DataProvider.OPERATION_STATE, 3);
		values.put(DataProvider.IS_SYNC, 0);
		db.updateWithOnConflict(DataProvider.BOOKMARK_TABLE, values,
				DataProvider.BOOKMARK_ID + " = ?",
				new String[] { String.valueOf(bookMarkId) },
				SQLiteDatabase.CONFLICT_REPLACE);
		return true;
	}

	// 删除书签
	synchronized public void deleteBookMark(String userid, int serverId) {
		db.delete(DataProvider.BOOKMARK_TABLE, DataProvider.BOOKMARK_SERVERID
				+ " = ? AND " + DataProvider.BOOKMARK_USERID + " =? ",
				new String[] { String.valueOf(serverId), userid });
	}

	// 删除书签
	synchronized public void deleteBookMarkById(int bookMarkId) {
		db.delete(DataProvider.BOOKMARK_TABLE, DataProvider.BOOKMARK_ID
				+ " = ?", new String[] { String.valueOf(bookMarkId) });
	}

	// 清理书签
	synchronized public void cleanBookMarks() {
		db.delete(DataProvider.BOOKMARK_TABLE, DataProvider.BOOKMARK_SERVERID
				+ " = 0 AND " + DataProvider.OPERATION_STATE + " = 3", null);
		db.delete(DataProvider.BOOKMARK_TABLE, DataProvider.OPERATION_STATE
				+ " = 3 AND " + DataProvider.IS_SYNC + " = 0", null);
	}

	// 更新书签
	synchronized public boolean updateJDBookMark(BookMark mark) {

		ContentValues values = new ContentValues();
		values.put(DataProvider.BOOKMARK_EBOOKID, mark.ebookid);
		values.put(DataProvider.BOOKMARK_DOCID, mark.docid);
		values.put(DataProvider.BOOK_TYPE, mark.bookType);
		values.put(DataProvider.BOOKMARK_CHAPTER_TITLE, mark.chapter_title);
		values.put(DataProvider.BOOKMARK_CHAPTER_ITEMREF, mark.chapter_itemref);
		values.put(DataProvider.BOOKMARK_OFFSET_IN_PARA, mark.offset_in_para);
		values.put(DataProvider.BOOKMARK_PARA_INDEX, mark.para_index);
		values.put(DataProvider.BOOKMARK_DIGEST, mark.digest);
		values.put(DataProvider.BOOKMARK_UPDATE_AT, mark.updated_at);
		values.put(DataProvider.BOOKMARK_SERVERID, mark.server_id);
		values.put(DataProvider.BOOKMARK_USERID, mark.userid);
		values.put(DataProvider.CREATION_TIME, mark.createTime);
		values.put(DataProvider.IS_SYNC, mark.isSync);
		values.put(DataProvider.OPERATION_STATE, mark.operation_state);
		values.put(DataProvider.BOOKMARK_PDF_PAGE, mark.pdf_page);
		db.updateWithOnConflict(
				DataProvider.BOOKMARK_TABLE,
				values,
				DataProvider.BOOKMARK_SERVERID + " =? AND "
						+ DataProvider.BOOKMARK_EBOOKID + " =? AND "
						+ DataProvider.BOOKMARK_DOCID + " =? AND "
						+ DataProvider.BOOKMARK_USERID + " =? ",
				new String[] { String.valueOf(mark.server_id),
						String.valueOf(mark.ebookid),
						String.valueOf(mark.docid), String.valueOf(mark.userid) },
				SQLiteDatabase.CONFLICT_REPLACE);
		return true;
	}

	// 更新书签
	synchronized public boolean updateBookMark(BookMark mark) {

		ContentValues values = new ContentValues();
		values.put(DataProvider.BOOKMARK_EBOOKID, mark.ebookid);
		values.put(DataProvider.BOOKMARK_DOCID, mark.docid);
		values.put(DataProvider.BOOK_TYPE, mark.bookType);
		values.put(DataProvider.BOOKMARK_CHAPTER_TITLE, mark.chapter_title);
		values.put(DataProvider.BOOKMARK_CHAPTER_ITEMREF, mark.chapter_itemref);
		values.put(DataProvider.BOOKMARK_OFFSET_IN_PARA, mark.offset_in_para);
		values.put(DataProvider.BOOKMARK_PARA_INDEX, mark.para_index);
		values.put(DataProvider.BOOKMARK_DIGEST, mark.digest);
		values.put(DataProvider.BOOKMARK_UPDATE_AT, mark.updated_at);
		values.put(DataProvider.BOOKMARK_SERVERID, mark.server_id);
		values.put(DataProvider.BOOKMARK_USERID, mark.userid);
		values.put(DataProvider.CREATION_TIME, mark.createTime);
		values.put(DataProvider.IS_SYNC, mark.isSync);
		values.put(DataProvider.OPERATION_STATE, mark.operation_state);
		values.put(DataProvider.BOOKMARK_PDF_PAGE, mark.pdf_page);
		db.updateWithOnConflict(DataProvider.BOOKMARK_TABLE, values,
				DataProvider.BOOKMARK_ID + " = ?",
				new String[] { String.valueOf(mark.id) },
				SQLiteDatabase.CONFLICT_REPLACE);
		return true;
	}

	// 获取指定itemRef中书签
	synchronized public List<BookMark> getAllBookMarksOfChapterItemref(
			String userid, long ebookid, int docid, String itemRef) {

		List<BookMark> list = new ArrayList<BookMark>();

		Cursor cursor = db.rawQuery(
				"SELECT * FROM " + DataProvider.BOOKMARK_TABLE + " WHERE  "
						+ DataProvider.BOOKMARK_USERID + " =? AND "
						+ DataProvider.BOOKMARK_EBOOKID + " =? AND "
						+ DataProvider.BOOKMARK_DOCID + " =? AND "
						+ DataProvider.OPERATION_STATE + " <>? AND "
						+ DataProvider.BOOKMARK_CHAPTER_ITEMREF + " =? ",
				new String[] { userid, String.valueOf(ebookid),
						String.valueOf(docid), String.valueOf(3), itemRef });// OPERATION_STATE＝3表示删除的

		if (cursor != null) {
			while (cursor.moveToNext()) {
				BookMark mark = new BookMark();
				mark.id = cursor.getInt(0);
				mark.server_id = cursor.getLong(1);
				mark.userid = userid;
				mark.ebookid = ebookid;
				mark.docid = docid;
				mark.bookType = cursor.getInt(5);
				mark.chapter_title = cursor.getString(6);
				mark.chapter_itemref = itemRef;
				mark.offset_in_para = cursor.getInt(8);
				mark.para_index = cursor.getInt(9);
				mark.digest = cursor.getString(10);
				mark.updated_at = Long.parseLong(cursor.getString(11));
				mark.createTime = cursor.getLong(12);
				mark.isSync = cursor.getInt(13);
				mark.operation_state = cursor.getInt(14);
				mark.pdf_page = cursor.getInt(15);
				list.add(mark);
			}
			cursor.close();
			return list;
		}

		return null;

	}

	// 获取所有未同步的书签

	synchronized public List<BookMark> getAllUnsyncBookMarks(String userid) {
		List<BookMark> list = new ArrayList<BookMark>();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ DataProvider.BOOKMARK_TABLE + " WHERE  "
				+ DataProvider.BOOKMARK_USERID + " =? AND "
				+ DataProvider.IS_SYNC + " =? ",
				new String[] { userid, String.valueOf(0) });

		if (cursor != null) {
			while (cursor.moveToNext()) {
				BookMark mark = new BookMark();
				mark.id = cursor.getInt(0);
				mark.server_id = cursor.getLong(1);
				mark.userid = userid;
				mark.ebookid = cursor.getLong(3);
				mark.docid = cursor.getInt(4);
				mark.bookType = cursor.getInt(5);
				mark.chapter_title = cursor.getString(6);
				mark.chapter_itemref = cursor.getString(7);
				mark.offset_in_para = cursor.getInt(8);
				mark.para_index = cursor.getInt(9);
				mark.digest = cursor.getString(10);
				mark.updated_at = Long.parseLong(cursor.getString(11));
				mark.createTime = cursor.getLong(12);
				mark.isSync = cursor.getInt(13);
				mark.operation_state = cursor.getInt(14);
				mark.pdf_page = cursor.getInt(15);
				list.add(mark);
			}
			cursor.close();
			return list;
		}

		return null;
	}

	synchronized public List<BookMark> getAllUnsyncBookMarksOfBook(
			String userid, long ebookid, int docid) {
		List<BookMark> list = new ArrayList<BookMark>();
		Cursor cursor = db.rawQuery(
				"SELECT * FROM " + DataProvider.BOOKMARK_TABLE + " WHERE  "
						+ DataProvider.BOOKMARK_USERID + " =? AND "
						+ DataProvider.BOOKMARK_EBOOKID + " =? AND "
						+ DataProvider.BOOKMARK_DOCID + " =? AND "
						+ DataProvider.IS_SYNC + " =? ",
				new String[] { userid, String.valueOf(ebookid),
						String.valueOf(docid), String.valueOf(0) });

		if (cursor != null) {
			while (cursor.moveToNext()) {
				BookMark mark = new BookMark();
				mark.id = cursor.getInt(0);
				mark.server_id = cursor.getLong(1);
				mark.userid = userid;
				mark.ebookid = cursor.getLong(3);
				mark.docid = cursor.getInt(4);
				mark.bookType = cursor.getInt(5);
				mark.chapter_title = cursor.getString(6);
				mark.chapter_itemref = cursor.getString(7);
				mark.offset_in_para = cursor.getInt(8);
				mark.para_index = cursor.getInt(9);
				mark.digest = cursor.getString(10);
				mark.updated_at = Long.parseLong(cursor.getString(11));
				mark.createTime = cursor.getLong(12);
				mark.isSync = cursor.getInt(13);
				mark.operation_state = cursor.getInt(14);
				mark.pdf_page = cursor.getInt(15);
				list.add(mark);
			}
			cursor.close();
			return list;
		}

		return null;
	}

	// 获取当前书籍所有书签
	synchronized public List<BookMark> getAllBookMarksOfBook(String userid,
			long ebookid, int docid) {// ebookid =0 docid!=0 is document
		List<BookMark> list = new ArrayList<BookMark>();

		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ DataProvider.BOOKMARK_TABLE + " WHERE  "
				+ DataProvider.BOOKMARK_USERID + " =? AND "
				+ DataProvider.BOOKMARK_EBOOKID + " =? AND "
				+ DataProvider.OPERATION_STATE + " <>? AND "
				+ DataProvider.BOOKMARK_DOCID + " =?",
				new String[] { String.valueOf(userid), String.valueOf(ebookid),
						String.valueOf(3), String.valueOf(docid) });

		if (cursor != null) {
			while (cursor.moveToNext()) {
				BookMark mark = new BookMark();
				mark.id = cursor.getInt(0);
				mark.server_id = cursor.getLong(1);
				mark.userid = userid;
				mark.ebookid = ebookid;
				mark.docid = docid;
				mark.bookType = cursor.getInt(5);
				mark.chapter_title = cursor.getString(6);
				mark.chapter_itemref = cursor.getString(7);
				mark.offset_in_para = cursor.getInt(8);
				mark.para_index = cursor.getInt(9);
				mark.digest = cursor.getString(10);
				mark.updated_at = Long.parseLong(cursor.getString(11));
				mark.createTime = cursor.getLong(12);
				mark.isSync = cursor.getInt(13);
				mark.operation_state = cursor.getInt(14);
				mark.pdf_page = cursor.getInt(15);
				list.add(mark);
			}
			cursor.close();
			return list;
		}

		return null;
	}

	// 获取书签同步时间
	synchronized public long getBookMarksSyncTime(String userId, long eBookId,
			int docId) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId), String.valueOf(docId) };
		Cursor cursor = db.rawQuery("SELECT "
				+ DataProvider.BOOKMARKSYNC_LAST_UPDATE + " FROM "
				+ DataProvider.BOOKMARKSYNC + " WHERE "
				+ DataProvider.BOOKMARKSYNC_USRID + " = ? AND "
				+ DataProvider.BOOKMARKSYNC_EBOOKID + " = ? AND "
				+ DataProvider.BOOKMARKSYNC_DOCID + " = ?", selectionArgs);
		long lastSyncTime = 0;
		if (cursor != null && cursor.moveToFirst()) {
			lastSyncTime = Long.valueOf(cursor.getString(0));
		}

		if (cursor != null)
			cursor.close();

		return lastSyncTime;
	}

	// 更新书签同步表
	synchronized public void insertOrUpdateBookMarksSyncTime(String userId,
			long eBookId, int docId, long lastUpdateTime) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId), String.valueOf(docId) };
		Cursor cursor = db.rawQuery("SELECT " + DataProvider.BOOKMARKSYNC_ID
				+ " FROM " + DataProvider.BOOKMARKSYNC + " WHERE "
				+ DataProvider.BOOKMARKSYNC_USRID + " = ? AND "
				+ DataProvider.BOOKMARKSYNC_EBOOKID + " = ? AND "
				+ DataProvider.BOOKMARKSYNC_DOCID + " = ?", selectionArgs);
		ContentValues values = new ContentValues();
		values.put(DataProvider.BOOKMARKSYNC_USRID, userId);
		values.put(DataProvider.BOOKMARKSYNC_EBOOKID, eBookId);
		values.put(DataProvider.BOOKMARKSYNC_DOCID, docId);
		values.put(DataProvider.BOOKMARKSYNC_LAST_UPDATE,
				String.valueOf(lastUpdateTime));

		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider.BOOKMARKSYNC_ID, dbId);
		}
		if (cursor != null)
			cursor.close();
		db.insertWithOnConflict(DataProvider.BOOKMARKSYNC, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	@Deprecated
	synchronized public boolean isScanedByUser(String uid) {
		String sqlString = "select * from " + DataProvider.BOOKSHELF
				+ " where " + DataProvider.USERID + " = ?";
		Cursor cursor = db.rawQuery(sqlString, new String[] { uid });
		boolean isScaned = false;
		while (cursor != null && cursor.moveToNext()) {
			isScaned = true;
			break;
		}
		if (cursor != null)
			cursor.close();
		return isScaned;

	}

	synchronized public List<BookShelfModel> getBooksInFolder(int folderid,
			int count, String uid) {
		List<BookShelfModel> list = new ArrayList<BookShelfModel>();
		String sqlString = "";
		if (count != -1)
			sqlString = "select * from " + DataProvider.BOOKSHELF + " where "
					+ DataProvider.FOLDER_CONTAINER_FOLDER_ID + " = ? limit "
					+ count + " offset 0";
		else {
			sqlString = "select * from " + DataProvider.BOOKSHELF + " where "
					+ DataProvider.FOLDER_CONTAINER_FOLDER_ID + " = ? ";
		}
		Cursor allCursor = db.rawQuery(sqlString,
				new String[] { folderid + "" });
		HashMap<String, BookShelfModel> ebookHash = new HashMap<String, BookShelfModel>();
		HashMap<String, BookShelfModel> docHash = new HashMap<String, BookShelfModel>();
		HashMap<String, String> bookHash = new HashMap<String, String>();
		HashMap<String, Map> folderHash = new HashMap<String, Map>();

		while (allCursor != null && allCursor.moveToNext()) {
			BookShelfModel model = new BookShelfModel();
			model.setId(allCursor.getInt(0));
			int documentid = allCursor.getInt(2);
			int ebookid = allCursor.getInt(3);
			model.setModifiedTime(allCursor.getDouble(4));
			model.setBelongDirId(allCursor.getInt(5));

			if (documentid != -1 && ebookid == -1) {
				model.setBookType("document");
				model.setBookid(documentid);
				docHash.put(String.valueOf(documentid), model);
			}

			if (documentid == -1 && ebookid != -1) {
				model.setBookType("ebook");
				model.setBookid(ebookid);
				ebookHash.put(String.valueOf(ebookid), model);
			}
		}
		allCursor.close();

		if (ebookHash.keySet().size() > 0) {
			// 构造ebook 数组
			String ebookidsSB = makePlaceholders(ebookHash.keySet().size());
			
			Cursor ebookInfoCursor = db
					.rawQuery(
							"SELECT _id,title,small_image_url,author,state_load,progress,size,source,book_id ,big_image_path,add_time,user_name FROM ebook WHERE _id IN ("
									+ ebookidsSB + ")", ebookHash.keySet().toArray(
						                    new String[ebookHash.keySet().size()]));
			while (ebookInfoCursor != null && ebookInfoCursor.moveToNext()) {

				long ebookid = ebookInfoCursor.getLong(0);
				String title = ebookInfoCursor.getString(1);
				
				String webcover = ebookInfoCursor.getString(2);
				String localCover = ebookInfoCursor.getString(9);
				String cover="";
				
				long addTime = ebookInfoCursor.getLong(10);
				
				if(addTime<GlobalVarable.LOAD_BOOK_COVER_BY_NEW_WAY_TIME)
					cover=webcover;
				else {
					if(TextUtils.isEmpty(localCover))
						cover=webcover;
					else {
						cover=localCover;
					}
				}

				
				String author = ebookInfoCursor.getString(3);
				int state = ebookInfoCursor.getInt(4);
				long progress = ebookInfoCursor.getLong(5);
				long size = ebookInfoCursor.getLong(6);
				String source = ebookInfoCursor.getString(7);
				
				long book_id = ebookInfoCursor.getLong(8);
				
				String userName = ebookInfoCursor.getString(11);
				//TODO 
				if(!TextUtils.isEmpty(source) && !TextUtils.isEmpty(String.valueOf(book_id)) && !TextUtils.isEmpty(userName) 
						&& LocalBook.alreadyEncrypt(source)){
					
					try {
						source = SecretKeyUtil.decrypt(String.valueOf(book_id), userName, source);
					} catch (UnsatisfiedLinkError e) {
						e.printStackTrace();
					}
					
				}

				int label = 0;
				if (source.equals(LocalBook.SOURCE_BORROWED_BOOK))
					label = BookShelfModel.LABEL_BORROWED;
				else if (source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
					label = BookShelfModel.LABEL_CHANGDU;
				} else if (source.equals(LocalBook.SOURCE_TRYREAD_BOOK)) {
					label = BookShelfModel.LABEL_TRYREAD;
				} else {
					label = 0;
				}
				
				BookShelfModel temp = ebookHash.get(String.valueOf(ebookid));
				temp.setBookName(title);
				temp.setBookCover(cover);
				temp.setAuthor(author);
				temp.setDownload_state(state);
				temp.setDownload_progress(progress);
				temp.setBook_size(size);
				temp.setBookCoverLabel(label);
				temp.setServerid(book_id);
				ebookHash.put(String.valueOf(ebookid), temp);
				bookHash.put(String.valueOf(book_id),String.valueOf(ebookid));
			}

			ebookInfoCursor.close();
		
			// 查询ebook进度
			if (bookHash.keySet().size()>0) {
				ebookidsSB = makePlaceholders(bookHash.keySet().size());
				
	            String[] book_ids = new String[bookHash.keySet().size()];
	            book_ids = bookHash.keySet().toArray(new String[bookHash.keySet().size()]);
	            
				Cursor progressEbookCursor = db.rawQuery("select ebookid,percent,update_time from "
						+ DataProvider.PROGRESS + " where " + DataProvider.USERID
						+ " = '" + uid + "' AND ebookid IN (" + ebookidsSB
						+ ")", book_ids);
				while (progressEbookCursor != null && progressEbookCursor.moveToNext()) {
					int book_id = progressEbookCursor.getInt(0);
					float percent = progressEbookCursor.getFloat(1);
					double update_time = progressEbookCursor.getDouble(2);
					BookShelfModel temp = ebookHash.get(bookHash.get(String.valueOf(book_id)));
					if (temp!=null && temp.getServerid() == book_id) {
						temp.setBookPercent((int) (percent * 100));
						temp.setPercentTime(update_time);
						ebookHash.put(bookHash.get(String.valueOf(book_id)), temp);
					}
				}
				progressEbookCursor.close();
			}
		}
		
		if (docHash.keySet().size() > 0) {
			// 构造document 数组
			String[] documentids = docHash.keySet().toArray(
					new String[docHash.keySet().size()]);
			String documentidsSB = makePlaceholders(docHash.keySet().size());
			

			// 查询document进度
			Cursor progressDocumentCursor = db.rawQuery(
					"select document_id,percent,update_time from " + DataProvider.PROGRESS
							+ " where " + DataProvider.USERID + " = '" + uid
							+ "' AND document_id IN (" + documentidsSB
							+ ")", documentids);
			while (progressDocumentCursor != null
					&& progressDocumentCursor.moveToNext()) {
				int documentid = progressDocumentCursor.getInt(0);
				float percent = progressDocumentCursor.getFloat(1);
				double update_time = progressDocumentCursor.getDouble(2);

				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null) {
					temp.setBookPercent((int) (percent * 100));
					temp.setPercentTime(update_time);
					docHash.put(String.valueOf(documentid), temp);
				}
	
			}
			progressDocumentCursor.close();
			
			Cursor documentInfoCursor = db
					.rawQuery(
							"SELECT _id,title,cover_path,author,state_load,progress,size FROM document WHERE _id IN ("
									+ documentidsSB + ")",
							documentids);
			while (documentInfoCursor != null
					&& documentInfoCursor.moveToNext()) {

				int documentid = documentInfoCursor.getInt(0);
				String title = documentInfoCursor.getString(1);
				String cover = documentInfoCursor.getString(2);
				String author = documentInfoCursor.getString(3);

				int state = documentInfoCursor.getInt(4);
				long progress = documentInfoCursor.getLong(5);
				long size = documentInfoCursor.getLong(6);

				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null) {
					temp.setBookName(title);
					temp.setBookCover(cover);
					temp.setAuthor(author);
					temp.setDownload_state(state);
					temp.setDownload_progress(progress);
					temp.setBook_size(size);
					docHash.put(String.valueOf(documentid), temp);
				}
			}
			documentInfoCursor.close();

			Cursor documentbindCursor = db.rawQuery(
					"SELECT document_id,title,cover_path FROM docbind WHERE document_id IN ("
							+ documentidsSB + ")", documentids);
			while (null != documentbindCursor
					&& documentbindCursor.moveToNext()) {

				int documentid = documentbindCursor.getInt(0);
				String title = documentbindCursor.getString(1);
				String cover = documentbindCursor.getString(2);

				BookShelfModel temp = docHash.get(String.valueOf(documentid));
				if (temp != null && !UiStaticMethod.isEmpty(title)
						&& !UiStaticMethod.isEmpty(cover)) {
					temp.setBookName(title);
					temp.setBookCover(cover);
					docHash.put(String.valueOf(documentid), temp);
				}

			}
			documentbindCursor.close();
		}

		list.addAll(ebookHash.values());
		list.addAll(docHash.values());
		return list;

	}
	
	
	
	

	synchronized public Folder getFolder(int id) {
		Cursor cursor = db.rawQuery("select " + DataProvider.FOLDER_NAME + ","
				+ DataProvider.FOLDER_CHANGETIME + " from "
				+ DataProvider.FOLDER + " WHERE " + DataProvider.FOLDER_ID
				+ "=?", new String[] { String.valueOf(id) });

		if (cursor == null) {
			return null;
		}
		while (cursor.moveToNext()) {
			Folder folder = new Folder();
			folder.setFolderName(cursor.getString(0));
			folder.setChangetime(Double.parseDouble(cursor.getString(1)));
			cursor.close();
			return folder;
		}
		return null;

	}

	synchronized public List<Folder> getAllFolder(String userid) {
		Cursor cursor = db.rawQuery("select " + DataProvider.FOLDER_ID + ","
				+ DataProvider.FOLDER_NAME + " from " + DataProvider.FOLDER
				+ " WHERE " + DataProvider.USERID + " ='" + userid + "'", null);

		if (cursor == null) {
			return null;
		}
		List<Folder> folders = new ArrayList<Folder>();
		while (cursor.moveToNext()) {
			Folder folder = new Folder();
			folder.setFolderId(cursor.getInt(0));
			folder.setFolderName(cursor.getString(1));
			folders.add(folder);
		}
		cursor.close();
		return folders;

	}

	synchronized public void clearFolder() {

		db.execSQL("DELETE  FROM " + DataProvider.FOLDER + " WHERE "
				+ DataProvider.FOLDER_ID + " NOT IN (" + "select "
				+ DataProvider.FOLDER_CONTAINER_FOLDER_ID + " from "
				+ DataProvider.BOOKSHELF + " where "
				+ DataProvider.FOLDER_CONTAINER_FOLDER_ID + " !=-1 group by "
				+ DataProvider.FOLDER_CONTAINER_FOLDER_ID + ")");

	}

	synchronized public int createFolder(String name, double time, String userId) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.FOLDER_NAME, name);
		values.put(DataProvider.FOLDER_CHANGETIME, time);
		values.put(DataProvider.USERID, userId);
		db.insertWithOnConflict(DataProvider.FOLDER, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

		Cursor cursor = db.rawQuery("select last_insert_rowid() from "
				+ DataProvider.FOLDER, null);
		int id = 0;
		if (cursor != null && cursor.moveToFirst())
			id = cursor.getInt(0);

		if (cursor != null)
			cursor.close();
		MZLog.d("wangguodong", "更新书架书籍文件夹");
		return id;

	}

	synchronized public int createFolder(double time, String userId) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.FOLDER_NAME, "未命名");
		values.put(DataProvider.FOLDER_CHANGETIME, time);
		values.put(DataProvider.USERID, userId);
		db.insertWithOnConflict(DataProvider.FOLDER, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

		Cursor cursor = db.rawQuery("select last_insert_rowid() from "
				+ DataProvider.FOLDER, null);
		int id = 0;
		if (cursor != null && cursor.moveToFirst())
			id = cursor.getInt(0);

		if (cursor != null)
			cursor.close();
		MZLog.d("wangguodong", "更新书架书籍文件夹");
		return id;

	}

	synchronized public void insertOrUpdateFolder(Folder folder) {

		ContentValues values = new ContentValues();
		values.put(DataProvider.FOLDER_ID, folder.getFolderId());
		values.put(DataProvider.FOLDER_NAME, folder.getFolderName());
		values.put(DataProvider.FOLDER_CHANGETIME, folder.getChangetime());
		values.put(DataProvider.USERID, folder.getUserid());
		db.insertWithOnConflict(DataProvider.FOLDER, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

	}

	synchronized public void updateFolder(int id, String name) {

		ContentValues values = new ContentValues();
		values.put(DataProvider.FOLDER_NAME, name);
		db.updateWithOnConflict(DataProvider.FOLDER, values,
				DataProvider.FOLDER_ID + " = ?",
				new String[] { String.valueOf(id) },
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	/**
	 * 保存图书信息到书架
	 * @param bookid
	 * @param time
	 * @param username
	 */
	public void savaJDEbookToBookShelf(long bookid, String time, String username) {

		String[] selectionArgs = new String[] { username,String.valueOf(bookid) };

		Cursor cursor = db.rawQuery(
				"SELECT _id FROM ebook WHERE user_name = ? AND book_id = ?",
				selectionArgs);
		int ebookId = -1;
		if (cursor != null && cursor.moveToFirst()) {
			ebookId = cursor.getInt(0);
			MZLog.d("wangguodong", "获得书籍的id" + ebookId);
		}
		if (cursor != null)
			cursor.close();
		if (ebookId != -1) {
			ContentValues values = new ContentValues();
			values.put(DataProvider.BOOKSHELF_EBOOK_ID, ebookId);
			values.put(DataProvider.BOOKSHELF_CHANGE_TIME, time);
			values.put(DataProvider.USERID, username);
			db.insertWithOnConflict(DataProvider.BOOKSHELF, null, values,SQLiteDatabase.CONFLICT_REPLACE);
			MZLog.d("wangguodong", "书籍已经添加到书架数据库");
		} else {
			MZLog.d("wangguodong", "插入bookshelf数据出错...");
		}
	}

	// 扫描所有当前用户书架还木有添加的document
	public List<Integer> scanLocalDocument(String userid) {

		List<Integer> list = null;
		String[] selectionArgs = new String[] { userid };
		Cursor cursor = db.rawQuery(
				"SELECT _id from document where _id NOT IN (select "
						+ DataProvider.BOOKSHELF_DOCUMENT_ID + " FROM "
						+ DataProvider.BOOKSHELF + " WHERE userid = ? AND "
						+ DataProvider.BOOKSHELF_DOCUMENT_ID + " > -1) ",
				selectionArgs);
		if (cursor == null) {
			return null;
		}
		list = new ArrayList<Integer>();
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			int id = cursor.getInt(0);
			list.add(id);
			cursor.moveToNext();
		}
		cursor.close();
		return list;
	}

	public void saveToBookShelf(int id, double time, int type, String userid) {
		ContentValues values = new ContentValues();
		// 1 表示document 0 ebook -1 文件夹
		switch (type) {
		case 1:
			values.put(DataProvider.BOOKSHELF_DOCUMENT_ID, id);
			break;
		case 0:

			values.put(DataProvider.BOOKSHELF_EBOOK_ID, id);
			break;

		case -1:
			values.put(DataProvider.BOOKSHELF_FOLDER_ID, id);
			break;
		}

		values.put(DataProvider.BOOKSHELF_CHANGE_TIME, time);
		values.put(DataProvider.USERID, userid);
		db.insertWithOnConflict(DataProvider.BOOKSHELF, null, values,SQLiteDatabase.CONFLICT_REPLACE);
	}

	public void saveToBookShelf(BookShelf bookShelf) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.BOOKSHELF_DOCUMENT_ID, bookShelf.document_id);
		values.put(DataProvider.BOOKSHELF_EBOOK_ID, bookShelf.ebook_id);
		values.put(DataProvider.BOOKSHELF_FOLDER_ID, bookShelf.folderid);
		values.put(DataProvider.BOOKSHELF_CHANGE_TIME, bookShelf.changetime);
		values.put(DataProvider.USERID, bookShelf.userid);
		values.put(DataProvider.FOLDER_CONTAINER_FOLDER_ID,
				bookShelf.folder_dirid);

		db.insertWithOnConflict(DataProvider.BOOKSHELF, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

	}

	public void saveToBookShelf(List<Map<String, Object>> allBookList,
			String userid) {

		for (int i = 0; i < allBookList.size(); i++) {
			Document document = (Document) allBookList.get(i).get("document");
			EBook ebook = (EBook) allBookList.get(i).get("ebook");
			ContentValues values = new ContentValues();
			if (document != null) {
				values.put(DataProvider.BOOKSHELF_DOCUMENT_ID,
						document.getDocumentId());
				values.put(DataProvider.BOOKSHELF_CHANGE_TIME, document.addAt);
			}
			if (ebook != null) {
				values.put(DataProvider.BOOKSHELF_EBOOK_ID, ebook.ebookId);
				values.put(DataProvider.BOOKSHELF_CHANGE_TIME,
						ebook.purchaseTime);
			}

			values.put(DataProvider.USERID, userid);
			db.insertWithOnConflict(DataProvider.BOOKSHELF, null, values,
					SQLiteDatabase.CONFLICT_REPLACE);
		}

	}

	@Deprecated
	synchronized public void insertOrUpdatePurchase(String userId, int eBookId,
			int edition, long time) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT _id, edition FROM epurchase WHERE userid = ? AND ebookid = ?",
						selectionArgs);
		ContentValues values = new ContentValues();
		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			int dbEdition = cursor.getInt(1);
			if (dbEdition == edition) {
				cursor.close();
				return;
			}
			values.put(DataProvider._ID, dbId);
		}

		if (cursor != null)
			cursor.close();

		values.put(DataProvider.USERID, userId);
		values.put(DataProvider.EBOOKID, eBookId);
		values.put(DataProvider.EDITION, edition);
		values.put(DataProvider.PURCHASE_AT, time);

		db.insertWithOnConflict(DataProvider.EPURCHASE, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	synchronized public boolean isPurchaseEbook(String userId, int eBookId) {
		boolean isPurchase = false;
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT _id, edition FROM epurchase WHERE userid = ? AND ebookid = ? AND edition=0",
						selectionArgs);
		if (cursor != null && cursor.moveToFirst()) {
			isPurchase = true;
		}

		if (cursor != null)
			cursor.close();
		return isPurchase;
	}

	synchronized public void insertOrUpdateBookName(int eBookId, String name) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.EBOOKID, eBookId);
		values.put(DataProvider.NAME, name);
		db.insertWithOnConflict(DataProvider.BOOKNAME, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	synchronized public String getBookName(int eBookId) {
		String[] selectionArgs = new String[] { String.valueOf(eBookId) };
		Cursor cursor = db.rawQuery(
				"SELECT name FROM bookname WHERE ebookid = ?", selectionArgs);
		if (cursor != null && cursor.moveToFirst()) {
			String name = cursor.getString(0);
			return name;
		}

		if (cursor != null)
			cursor.close();

		return "";
	}

	synchronized public EBook getEBook(long eBookId) {
		String[] selectionArgs = new String[] { String.valueOf(eBookId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT _id,book_id,title,author,small_image_url,source,book_path,user_name FROM ebook WHERE _id = ?",
						selectionArgs);
		EBook eBook = null;
		if (cursor != null && cursor.moveToFirst()) {
			eBook = new EBook();
			eBook.ebookId = cursor.getInt(0);
			eBook.bookId = cursor.getLong(1);
			eBook.title = cursor.getString(2);
			eBook.authorName = cursor.getString(3);
			eBook.cover = cursor.getString(4);
			eBook.source = cursor.getString(5);
			String userName = cursor.getString(7);
			
			if(!TextUtils.isEmpty(eBook.source) && !TextUtils.isEmpty(String.valueOf(eBook.bookId)) && !TextUtils.isEmpty(userName) 
					&& LocalBook.alreadyEncrypt(eBook.source)){
				
				try {
					eBook.source = SecretKeyUtil.decrypt(String.valueOf(eBook.bookId), userName, eBook.source);
				} catch (UnsatisfiedLinkError e) {
					e.printStackTrace();
				}
				
			}
			cursor.close();
		}

		return eBook;
	}

//	synchronized public long getEBookIdForChangdu() {
//
//		if (!LoginUser.isLogin())
//			return 0;
//		Cursor cursor = db.rawQuery(
//				"SELECT book_id FROM ebook WHERE source = '"
//						+ LocalBook.SOURCE_ONLINE_BOOK + "' and user_name = '"
//						+ LoginUser.getpin()
//						+ "' ORDER BY add_time DESC LIMIT 1", null);
//
//		long bookid = 0;
//		if (cursor != null && cursor.moveToFirst()) {
//
//			bookid = cursor.getLong(0);
//			cursor.close();
//		}
//
//		return bookid;
//	}

	synchronized public String getChangduExpireTime(long index) {

		Cursor cursor = db
				.rawQuery(
						"SELECT end FROM ebook,OlineCardTable WHERE ebook._id = "
								+ index
								+ " and ebook.card_num = OlineCardTable.card_num",
						null);

		String expireTime = "";
		if (cursor != null && cursor.moveToFirst()) {
			expireTime = cursor.getString(0);
			cursor.close();
		}

		return expireTime;
	}

	synchronized public EBook getEBookByBookId(long bookId) {
		String[] selectionArgs = new String[] { String.valueOf(bookId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT _id,book_id,title,author,small_image_url,source,book_path,user_name FROM ebook WHERE book_id = ?",
						selectionArgs);
		EBook eBook = null;
		if (cursor != null && cursor.moveToFirst()) {
			eBook = new EBook();
			eBook.ebookId = cursor.getInt(0);
			eBook.bookId = cursor.getLong(1);
			eBook.title = cursor.getString(2);
			eBook.authorName = cursor.getString(3);
			eBook.cover = cursor.getString(4);
			eBook.source = cursor.getString(5);
			String userName = cursor.getString(7);
			
			if(!TextUtils.isEmpty(eBook.source) && !TextUtils.isEmpty(String.valueOf(eBook.bookId)) && !TextUtils.isEmpty(userName) 
					&& LocalBook.alreadyEncrypt(eBook.source)){
				
				try {
					eBook.source = SecretKeyUtil.decrypt(String.valueOf(eBook.bookId), userName, eBook.source);
				} catch (UnsatisfiedLinkError e) {
					e.printStackTrace();
				}
				
			}
			cursor.close();
		}

		return eBook;
	}

	synchronized public void insertOrUdapteLocalBook(int eBookId, int entityId,
			int edition) {
		String[] selectionArgs = new String[] { String.valueOf(eBookId),
				String.valueOf(edition) };
		Cursor cursor = db.rawQuery(
				"SELECT _id FROM eelocal WHERE ebookid = ? AND  edition = ?",
				selectionArgs);
		ContentValues values = new ContentValues();
		values.put(DataProvider.EBOOKID, eBookId);
		values.put(DataProvider.ENTITYID, entityId);
		values.put(DataProvider.EDITION, edition);
		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider._ID, dbId);
		}

		if (cursor != null)
			cursor.close();

		db.insertWithOnConflict(DataProvider.EELOCAL, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	synchronized public int getLocalEntityId(int eBookId, int edition) {
		String[] selectionArgs = new String[] { String.valueOf(eBookId),
				String.valueOf(edition) };
		Cursor cursor = db
				.rawQuery(
						"SELECT entityid FROM eelocal WHERE ebookid = ? AND  edition = ?",
						selectionArgs);
		if (cursor != null && cursor.moveToFirst()) {
			int entityId = cursor.getInt(0);
			cursor.close();
			return entityId;
		}
		if (cursor != null)
			cursor.close();
		return 0;
	}

	synchronized public void insertOrUpdateEBook(Book book) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.EBOOKID, book.ebookId);
		values.put(DataProvider.BOOKID, book.bookId);
		values.put(DataProvider.PRICE, book.price);
		values.put(DataProvider.TITLE, book.title);
		values.put(DataProvider.AUTHOR, book.authorName);
		values.put(DataProvider.COVER_URL, book.cover);

		db.insertWithOnConflict(DataProvider.EBOOK, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

	}

	synchronized public void insertOrUpdateEBook(EBook book) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.EBOOKID, book.ebookId);
		values.put(DataProvider.BOOKID, book.bookId);
		values.put(DataProvider.PRICE, book.price);
		values.put(DataProvider.TITLE, book.title);
		values.put(DataProvider.AUTHOR, book.authorName);
		values.put(DataProvider.COVER_URL, book.cover);

		db.insertWithOnConflict(DataProvider.EBOOK, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

	}

	// 更新借阅书籍的借阅截至时间
	synchronized public void insertOrUpdateBorrowEBook(int index, String endtime) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.BORROW_END_TIME, endtime);
		values.put(DataProvider._ID, index);
		db.insertWithOnConflict(DataProvider.EBOOK, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

	}

	synchronized public void insertOrUpdateEBook(SerializableBook book) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.EBOOKID, book.ebookId);
		values.put(DataProvider.BOOKID, book.bookId);
		values.put(DataProvider.PRICE, book.price);
		values.put(DataProvider.TITLE, book.title);
		values.put(DataProvider.AUTHOR, book.authorName);
		values.put(DataProvider.COVER_URL, book.cover);

		db.insertWithOnConflict(DataProvider.EBOOK, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

	}

	// 更新购买的书籍的最后阅读时间
	synchronized public void updateEBookLastReadTime(EBook book) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.READ_AT, book.readAt);
		db.update(DataProvider.EBOOK, values, DataProvider.EBOOKID + " = ?",
				new String[] { String.valueOf(book.ebookId) });
	}

	synchronized public List<String> listPurchaseEBookDontHaveKey(String userId) {
		List<String> eBookIdList = new ArrayList<String>();
		String[] selectionArgs = new String[] { String.valueOf(userId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT epurchase.ebookid FROM epurchase WHERE epurchase.userid=? EXCEPT SELECT bookname.ebookid FROM bookname",
						selectionArgs);
		if (cursor == null) {
			return eBookIdList;
		}
		while (cursor.moveToNext()) {
			eBookIdList.add(String.valueOf(cursor.getInt(0)));
		}
		cursor.close();
		return eBookIdList;
	}

	synchronized public List<EBook> listPurchaseEBook(String userId) {
		List<EBook> eBookList = new ArrayList<EBook>();
		/*
		 * String[] selectionArgs = new String[] { String.valueOf(userId) };
		 * Cursor cursor = db .rawQuery(
		 * "SELECT distinct ebook.ebookid,ebook.bookid as bookid," +
		 * "title,author,cover_url,purchase_at,epurchase.edition, eelocal.entityid "
		 * + "FROM ebook,epurchase " +
		 * "LEFT JOIN eelocal ON (epurchase.ebookid=eelocal.ebookid AND epurchase.edition=eelocal.edition) "
		 * +
		 * "WHERE epurchase.userid=? AND epurchase.archived=0 AND ebook.ebookid=epurchase.ebookid "
		 * + "ORDER BY purchase_at DESC", selectionArgs); if (cursor == null) {
		 * return eBookList; }
		 * 
		 * while (cursor.moveToNext()) { EBook book = new EBook(); book.ebookId
		 * = cursor.getInt(0); book.bookId = cursor.getInt(1); book.title =
		 * cursor.getString(2); book.authorName = cursor.getString(3);
		 * book.cover = cursor.getString(4); book.purchaseTime =
		 * cursor.getLong(5); book.edition = cursor.getInt(6); if
		 * (cursor.isNull(7)) { book.entityId = 0; } else { book.entityId =
		 * cursor.getInt(7); } eBookList.add(book); } cursor.close();
		 */
		return eBookList;
	}

	synchronized public List<EBook> listLocalEBook(String userId) {
		List<EBook> eBookList = new ArrayList<EBook>();
		/*
		 * String[] selectionArgs = new String[] { String.valueOf(userId) };
		 * Cursor cursor = db .rawQuery(
		 * "SELECT distinct ebook.ebookid,ebook.bookid as bookid," +
		 * "title,author,cover_url,purchase_at,epurchase.edition, eelocal.entityid, readat "
		 * + "FROM ebook,epurchase " +
		 * "LEFT JOIN eelocal ON (epurchase.ebookid=eelocal.ebookid AND epurchase.edition=eelocal.edition AND eelocal.entityid IS NOT NULL) "
		 * +
		 * "WHERE epurchase.userid=? AND epurchase.archived=0 AND ebook.ebookid=epurchase.ebookid "
		 * + "ORDER BY purchase_at DESC", selectionArgs); if (cursor == null) {
		 * return eBookList; }
		 * 
		 * while (cursor.moveToNext()) { EBook book = new EBook(); book.ebookId
		 * = cursor.getInt(0); book.bookId = cursor.getInt(1); book.title =
		 * cursor.getString(2); book.authorName = cursor.getString(3);
		 * book.cover = cursor.getString(4); book.purchaseTime =
		 * cursor.getLong(5);
		 * 
		 * if (book.purchaseTime <= 0) book.purchaseTime = 100;
		 * 
		 * book.edition = cursor.getInt(6); book.entityId = cursor.getInt(7);
		 * book.readAt = cursor.getLong(8);
		 * 
		 * if (book.entityId != 0) { eBookList.add(book); } String[] selection =
		 * new String[] { String.valueOf(userId), String.valueOf(book.ebookId)
		 * }; Cursor progressCursor = db.rawQuery("SELECT percent" +
		 * " FROM progress WHERE userid = ? AND  ebookid = ?", selection); if
		 * (progressCursor != null && progressCursor.moveToFirst()) {
		 * book.percent = progressCursor.getDouble(0); } progressCursor.close();
		 * 
		 * } cursor.close();
		 */
		return eBookList;
	}

	synchronized public ReadProgress getEbookReadProgress(String userId,
			long eBookId) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT chapter_itemref, para_idx, offset_in_para, update_time, percent,"
								+ " pdf_page, pdf_zoom, pdf_x_offset_percent, pdf_y_offset_percent,"
								+ " chapter_title FROM progress WHERE userid = ? AND  ebookid = ?",
						selectionArgs);
		ReadProgress progress = new ReadProgress();
		progress.operatingState = 2;
		if (cursor != null && cursor.moveToFirst()) {
			progress.chapterItemRef = cursor.getString(0);
			progress.paraIndex = cursor.getInt(1);
			progress.offsetInPara = cursor.getInt(2);
			progress.updateTime = cursor.getLong(3);
			progress.percent = cursor.getFloat(4);
			progress.pdfPage = cursor.getInt(5);
			progress.pdfZoom = cursor.getFloat(6);
			progress.pdfXOffsetPercent = cursor.getFloat(7);
			progress.pdfYOffsetPercent = cursor.getFloat(8);
			progress.chapterTitle = cursor.getString(9);
			progress.operatingState = 1;
		}

		if (cursor != null)
			cursor.close();

		return progress;
	}

	synchronized public ReadProgress getDocReadProgress(String userId, int docId) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(docId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT chapter_itemref, para_idx, offset_in_para, update_time, percent,"
								+ " pdf_page, pdf_zoom, pdf_x_offset_percent, pdf_y_offset_percent,"
								+ " chapter_title FROM progress WHERE userid = ? AND  document_id = ?",
						selectionArgs);
		ReadProgress progress = new ReadProgress();
		progress.operatingState = 2;
		if (cursor != null && cursor.moveToFirst()) {
			progress.chapterItemRef = cursor.getString(0);
			progress.paraIndex = cursor.getInt(1);
			progress.offsetInPara = cursor.getInt(2);
			progress.updateTime = cursor.getLong(3);
			progress.percent = cursor.getFloat(4);
			progress.pdfPage = cursor.getInt(5);
			progress.pdfZoom = cursor.getFloat(6);
			progress.pdfXOffsetPercent = cursor.getFloat(7);
			progress.pdfYOffsetPercent = cursor.getFloat(8);
			progress.chapterTitle = cursor.getString(9);
			progress.operatingState = 1;
		}
		if (cursor != null)
			cursor.close();

		return progress;
	}

	synchronized public void insertOrUpdateEbookReadProgress(String userId,
			long eBookId, ReadProgress progress, boolean isShowAllNotes) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId) };
		Cursor cursor = db.rawQuery(
				"SELECT _id FROM progress WHERE userid = ? AND  ebookid = ?",
				selectionArgs);
		ContentValues values = new ContentValues();
		values.put(DataProvider.CHAPTER_ITEMREF, progress.chapterItemRef);
		values.put(DataProvider.BOOK_TYPE, progress.bookType);
		values.put(DataProvider.PARA_IDX, progress.paraIndex);
		values.put(DataProvider.OFFSET_IN_PARA, progress.offsetInPara);
		values.put(DataProvider.UPDATE_TIME, progress.updateTime);
		values.put(DataProvider.OPERATION_STATE, progress.operatingState);
		values.put(DataProvider.PDF_PAGE, progress.pdfPage);
		values.put(DataProvider.PDF_ZOOM, progress.pdfZoom);
		values.put(DataProvider.PDF_X_OFFSET_PERCENT,
				progress.pdfXOffsetPercent);
		values.put(DataProvider.PDF_Y_OFFSET_PERCENT,
				progress.pdfYOffsetPercent);
		values.put(DataProvider.CHAPTER_TITLE, progress.chapterTitle);
		if (Double.isNaN(progress.percent)) {
			values.put(DataProvider.PERCENT, 0);
		} else {
			values.put(DataProvider.PERCENT, progress.percent);
		}
		values.put(DataProvider.EBOOKID, eBookId);
		values.put(DataProvider.USERID, userId);
		values.put(DataProvider.SHOW_ALL_NOTES, isShowAllNotes ? 1 : 0);
		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider._ID, dbId);
		}
		if (cursor != null)
			cursor.close();
		db.insertWithOnConflict(DataProvider.PROGRESS, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

	}

	synchronized public void insertOrUpdateDocReadProgress(String userId,
			int docId, ReadProgress progress, boolean isShowAllNotes) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(docId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT _id FROM progress WHERE userid = ? AND  document_id = ?",
						selectionArgs);
		ContentValues values = new ContentValues();
		values.put(DataProvider.CHAPTER_ITEMREF, progress.chapterItemRef);
		values.put(DataProvider.BOOK_TYPE, progress.bookType);
		values.put(DataProvider.PARA_IDX, progress.paraIndex);
		values.put(DataProvider.OFFSET_IN_PARA, progress.offsetInPara);
		values.put(DataProvider.UPDATE_TIME, progress.updateTime);
		values.put(DataProvider.OPERATION_STATE, progress.operatingState);
		values.put(DataProvider.PDF_PAGE, progress.pdfPage);
		values.put(DataProvider.PDF_ZOOM, progress.pdfZoom);
		values.put(DataProvider.PDF_X_OFFSET_PERCENT,
				progress.pdfXOffsetPercent);
		values.put(DataProvider.PDF_Y_OFFSET_PERCENT,
				progress.pdfYOffsetPercent);
		values.put(DataProvider.CHAPTER_TITLE, progress.chapterTitle);
		if (Double.isNaN(progress.percent)) {
			values.put(DataProvider.PERCENT, 0);
		} else {
			values.put(DataProvider.PERCENT, progress.percent);
		}
		values.put(DataProvider.DOCUMENT_ID, docId);
		values.put(DataProvider.USERID, userId);
		values.put(DataProvider.SHOW_ALL_NOTES, isShowAllNotes ? 1 : 0);
		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider._ID, dbId);
		}
		if (cursor != null)
			cursor.close();
		db.insertWithOnConflict(DataProvider.PROGRESS, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

	}

	synchronized public void updateShowAllNotes(String userId, long ebookId,
			int documentId, boolean isShowAllNotes) {
		if (ebookId != 0) {
			String[] selectionArgs = new String[] {
					String.valueOf(isShowAllNotes ? 1 : 0),
					String.valueOf(userId), String.valueOf(ebookId) };
			db.execSQL(
					"UPDATE progress SET show_all_notes = ? WHERE userid = ? AND  ebookid = ? ",
					selectionArgs);
		} else if (documentId != 0) {
			String[] selectionArgs = new String[] {
					String.valueOf(isShowAllNotes ? 1 : 0),
					String.valueOf(userId), String.valueOf(documentId) };
			db.execSQL(
					"UPDATE progress SET show_all_notes = ? WHERE userid = ? AND  document_id = ? ",
					selectionArgs);
		}
	}

	synchronized public boolean isShowAllNotes(String userId, long ebookId,
			int documentId) {
		Cursor cursor = null;
		if (ebookId != 0) {
			String[] selectionArgs = new String[] { String.valueOf(userId),
					String.valueOf(ebookId) };
			cursor = db
					.rawQuery(
							"SELECT show_all_notes FROM progress WHERE userid = ? AND  ebookid = ?",
							selectionArgs);
		} else if (documentId != 0) {
			String[] selectionArgs = new String[] { String.valueOf(userId),
					String.valueOf(documentId) };
			cursor = db
					.rawQuery(
							"SELECT show_all_notes FROM progress WHERE userid = ? AND  document_id = ?",
							selectionArgs);
		} else {
			return false;
		}

		int isShowAllNotes = 0;
		if (cursor != null && cursor.moveToFirst()) {
			isShowAllNotes = cursor.getInt(0);
		}
		if (cursor != null)
			cursor.close();
		return isShowAllNotes == 1;
	}

	synchronized public void UpdateDocBind(DocBind docBind) {
		String[] selectionArgs = new String[] {
				String.valueOf(docBind.documentId),
				String.valueOf(docBind.userId) };

		ContentValues values = new ContentValues();
		values.put(DataProvider.DOCUMENT_ID, docBind.documentId);
		values.put(DataProvider.USERID, docBind.userId);
		values.put(DataProvider.SERVER_ID, docBind.serverId);
		db.update(DataProvider.DOCBIND, values,
				" document_id = ? AND userid = ?", selectionArgs);
	}

	synchronized public void insertOrUpdateDocBind(DocBind docBind) {
		String[] selectionArgs = new String[] {
				String.valueOf(docBind.documentId),
				String.valueOf(docBind.userId) };
		Cursor cursor = db.rawQuery(
				"SELECT _id FROM docbind WHERE document_id = ? AND userid = ?",
				selectionArgs);
		ContentValues values = new ContentValues();
		values.put(DataProvider.DOCUMENT_ID, docBind.documentId);
		values.put(DataProvider.USERID, docBind.userId);
		values.put(DataProvider.SERVER_ID, docBind.serverId);
		values.put(DataProvider.BIND, docBind.bind);
		values.put(DataProvider.BOOKID, docBind.bookId);
		values.put(DataProvider.TITLE, docBind.serverTitle);
		values.put(DataProvider.AUTHOR, docBind.serverAuthor);
		values.put(DataProvider.COVER_PATH, docBind.serverCover);

		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider._ID, dbId);
		}
		if (cursor != null)
			cursor.close();

		db.insertWithOnConflict(DataProvider.DOCBIND, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	synchronized public DocBind getDocBind(int documentId, String userId) {
		String[] selectionArgs = new String[] { String.valueOf(documentId),
				userId };
		Cursor cursor = db.rawQuery(
				"SELECT * FROM docbind WHERE document_id = ? AND userid = ?",
				selectionArgs);
		DocBind docBind = null;
		if (cursor != null && cursor.moveToFirst()) {
			docBind = new DocBind();
			docBind.documentId = cursor.getInt(1);
			docBind.userId = cursor.getString(2);
			docBind.serverId = cursor.getLong(3);
			docBind.bind = cursor.getInt(4);
			docBind.bookId = cursor.getLong(5);
			docBind.serverTitle = cursor.getString(6);
			docBind.serverAuthor = cursor.getString(7);
			docBind.serverCover = cursor.getString(8);
			cursor.close();
		}
		return docBind;
	}

	synchronized public int getDocmentId(String sign) {
		String[] selectionArgs = new String[] { sign };
		Cursor cursor = db.rawQuery(
				"SELECT _id FROM document WHERE opf_md5 = ? ", selectionArgs);
		int id = -1;
		if (cursor != null && cursor.moveToFirst()) {

			id = cursor.getInt(0);
		}
		if (cursor != null)
			cursor.close();
		return id;
	}

	public ArrayList<String> getBuildInOpfMd5(Context context){
		
		ArrayList<String> opfMd5s = new ArrayList<String>();

		InputStream asset_is = null;
		try {
			asset_is = context.getResources().getAssets().open("buildin/ebook_builtin");
			String ebookBuiltInString = IOUtil.readAsString(asset_is, ServiceProtocol.sCharset, null);
			try {
				JSONObjectProxy json = new JSONObjectProxy(new JSONObject(ebookBuiltInString));
				JSONArrayPoxy ebook_list = json.getJSONArray("ebook_builtin");
				if (ebook_list != null) {
					for (int index = 0; index < ebook_list.length(); ++index) {
						opfMd5s.add(ebook_list.getJSONObject(index).getString("opf_md5"));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (asset_is != null)
				asset_is.close();
			asset_is = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
			return opfMd5s;
		
		
	}
	
	
	synchronized public List<Map<String, String>> getLocalDocumentSign(Context context,
			String userid) {

		
		
		ArrayList<String> opfMd5s =getBuildInOpfMd5(context);
		
		String[] selects = new String[opfMd5s.size()+1];
		
		StringBuffer opfMd5sSB = new StringBuffer();
		for (int i = 0; i <opfMd5s.size(); i++)
			opfMd5sSB.append("?").append(",");
		opfMd5sSB.deleteCharAt(opfMd5sSB.length() - 1);
		
		for(int i=0;i<selects.length;i++)
		{
			if(i==0)
				selects[i]=userid;
			else {
				selects[i]=opfMd5s.get(i-1);
			}
		}

		String sql ="SELECT docbind.server_id,document.opf_md5,document.title,"
				+ "document._id,document.book_source FROM docbind,document WHERE docbind.document_id = document._id "
				+ "and docbind.userid = ? and document._id in ( select document_id from bookshelf where document_id != -1) and document.opf_md5 NOT IN ( "+opfMd5sSB.toString()+" )";
		
		MZLog.d("wangguodong", sql);
		
		Cursor cursor = db.rawQuery(sql,selects); 
		
		if (cursor == null) {
			return null;
		}
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			Map<String, String> map = new HashMap<String, String>();

			map.put("serverId", String.valueOf(cursor.getInt(0)));
			map.put("sign", cursor.getString(1));
			map.put("name", cursor.getString(2));
			map.put("documentId", cursor.getString(3));
			map.put("sourePath", cursor.getString(4));
			map.put("isNeedUploaded", "false");
			list.add(map);
			cursor.moveToNext();
		}
		cursor.close();

		return list;
	}

	synchronized public Document getDocument(int documentId) {
		String[] selectionArgs = new String[] { String.valueOf(documentId) };
		Cursor cursor = db.rawQuery("SELECT " + DataProvider._ID + ","
				+ DataProvider.TITLE + "," + DataProvider.AUTHOR + ","
				+ DataProvider.BOOK_SOURCE + "," + DataProvider.BOOK_PATH + ","
				+ DataProvider.COVER_PATH + "," + DataProvider.ADD_AT + ","
				+ DataProvider.READ_AT + "," + DataProvider.BOOK_TYPE + ","
				+ DataProvider.OPF_MD5 + "," + DataProvider.FROM_CLOUDDISK
				+ ",state_load FROM document WHERE _id = ?", selectionArgs);
		Document doc = null;
		if (cursor != null && cursor.moveToFirst()) {

			doc = new Document();
			doc.documentId = cursor.getInt(0);
			doc.title = cursor.getString(1);
			doc.author = cursor.getString(2);
			doc.bookSource = cursor.getString(3);
			doc.bookPath = cursor.getString(4);
			doc.coverPath = cursor.getString(5);
			doc.addAt = cursor.getLong(6);
			doc.readAt = cursor.getLong(7);
			doc.format = cursor.getInt(8);
			doc.opfMD5 = cursor.getString(9);
			doc.fromCloudDisk = cursor.getInt(10);
			doc.state = cursor.getInt(11);
		}
		if (cursor != null)
			cursor.close();
		return doc;

	}

	synchronized public Document getDocument(String sign) {
		String[] selectionArgs = new String[] { sign };
		Cursor cursor = db.rawQuery("SELECT * FROM document WHERE opf_md5 = ?",
				selectionArgs);
		Document doc = null;
		if (cursor != null && cursor.moveToFirst()) {

			doc = new Document();
			doc.documentId = cursor.getInt(0);
			doc.title = cursor.getString(1);
			doc.author = cursor.getString(2);
			doc.bookPath = cursor.getString(3);
			doc.bookSource = cursor.getString(4);
			doc.format = cursor.getInt(5);
			doc.coverPath = cursor.getString(6);
			doc.size = cursor.getLong(7);
			doc.progress = cursor.getLong(8);
			doc.state = cursor.getInt(9);
			doc.bookState = cursor.getInt(10);
			doc.addAt = cursor.getLong(11);
			doc.access_time = cursor.getLong(12);
			doc.mod_time = cursor.getLong(13);
			doc.readAt = cursor.getLong(15);
			doc.fromCloudDisk = cursor.getInt(16);
			doc.opfMD5 = cursor.getString(17);
		}
		if (cursor != null)
			cursor.close();
		return doc;
	}

	synchronized public Document getDocumentBySource(String booksource) {
		Cursor cursor = db.rawQuery(
				"SELECT * FROM document WHERE book_source LIKE '%" + booksource
						+ "'", null);
		Document doc = null;
		if (cursor != null && cursor.moveToFirst()) {

			doc = new Document();
			doc.documentId = cursor.getInt(0);
			doc.title = cursor.getString(1);
			doc.author = cursor.getString(2);
			doc.bookPath = cursor.getString(3);
			doc.bookSource = cursor.getString(4);
			doc.format = cursor.getInt(5);
			doc.coverPath = cursor.getString(6);
			doc.size = cursor.getLong(7);
			doc.progress = cursor.getLong(8);
			doc.state = cursor.getInt(9);
			doc.bookState = cursor.getInt(10);
			doc.addAt = cursor.getLong(11);
			doc.access_time = cursor.getLong(12);
			doc.mod_time = cursor.getLong(13);
			doc.readAt = cursor.getLong(15);
			doc.fromCloudDisk = cursor.getInt(16);
			doc.opfMD5 = cursor.getString(17);
		}
		if (cursor != null)
			cursor.close();
		return doc;
	}

	@Deprecated
	synchronized public void deleteEbook(String userId, int eBookId, int edition) {
		db.delete(
				DataProvider.EELOCAL,
				"ebookid = ? AND edition = ?",
				new String[] { String.valueOf(eBookId), String.valueOf(edition) });
		db.delete(
				DataProvider.EPURCHASE,
				"userid = ? AND ebookid = ?",
				new String[] { String.valueOf(userId), String.valueOf(eBookId) });
		db.delete(
				DataProvider.PROGRESS,
				"userid = ? AND ebookid = ?",
				new String[] { String.valueOf(userId), String.valueOf(eBookId) });
		db.delete(
				DataProvider.NOTESYNC,
				"userid = ? AND ebookid = ?",
				new String[] { String.valueOf(userId), String.valueOf(eBookId) });
		db.delete(
				DataProvider.EBOOKNOTE,
				"userid = ? AND ebookid = ?",
				new String[] { String.valueOf(userId), String.valueOf(eBookId) });

		db.delete(DataProvider.BOOKSHELF, "ebook_id = ?",
				new String[] { String.valueOf(eBookId) });

	}

	// 书籍信息被修改 更新的时候需要删除数据
	synchronized public void deleteEbookWhenUpdate(String userId,
			Integer[] eBookId) {
		String sb = makePlaceholders(eBookId.length);
		
		db.execSQL("DELETE FROM " + DataProvider.BOOKSHELF
				+ " WHERE ebook_id IN (" + sb + ")",
				(Object[]) eBookId);
		db.execSQL("DELETE FROM " + DataProvider.EBOOK + " WHERE ebookid IN ("
				+ sb + ")", (Object[]) eBookId);
		db.execSQL("DELETE FROM " + DataProvider.BOOKPAGE
				+ " WHERE ebookid IN (" + sb + ")",
				(Object[]) eBookId);
		db.execSQL("DELETE FROM " + DataProvider.PAGECONTENT
				+ " WHERE ebookid IN (" + sb + ")",
				(Object[]) eBookId);
	}

	public boolean haveOtherDocumentUser(String uid) {

		boolean haveOtherUser = false;
		String[] selects = new String[] { String.valueOf(uid) };
		Cursor cursor = db.rawQuery("SELECT " + DataProvider.USERID + " from "
				+ DataProvider.DOCBIND + " WHERE " + DataProvider.USERID
				+ " !=? ", selects);
		if (cursor == null) {
			return haveOtherUser;
		}
		while (cursor.moveToNext()) {
			haveOtherUser = true;
			break;
		}
		cursor.close();
		return haveOtherUser;
	}

	public boolean haveOtherEbookUser(String uid) {

		boolean haveOtherUser = false;
		String[] selects = new String[] { String.valueOf(uid) };
		Cursor cursor = db.rawQuery("SELECT user_name from "
				+ DataProvider.TABLE_NAME_EBOOK + " WHERE user_name !=? ",
				selects);
		if (cursor == null) {
			return haveOtherUser;
		}
		while (cursor.moveToNext()) {
			haveOtherUser = true;
			break;
		}
		cursor.close();
		return haveOtherUser;
	}
	
	public boolean haveOtherEbookUserSource(String uid, long book_id, String book_source) {

		boolean haveOtherUser = false;
		String[] selects = new String[] { String.valueOf(uid) };
		Cursor cursor = null;
		if (LocalBook.SOURCE_TRYREAD_BOOK.equals(book_source))
				cursor = db.rawQuery("SELECT user_name from "
				+ DataProvider.TABLE_NAME_EBOOK + " WHERE book_id = " + book_id + " and user_name !=? and source='tryread_book' ",
				selects);
		else
			cursor = db.rawQuery("SELECT user_name from "
					+ DataProvider.TABLE_NAME_EBOOK + " WHERE book_id = " + book_id + " and user_name !=? and source in ('buyed_book','online_book','borrowed_book') ",
					selects); 
		if (cursor == null) {
			return haveOtherUser;
		}
		if (cursor.moveToNext()) {
			haveOtherUser = true;
		}
		cursor.close();
		return haveOtherUser;
	}
	
	public boolean haveOtherEbookUser(String uid, long book_id) {

		boolean haveOtherUser = false;
		String[] selects = new String[] { String.valueOf(uid) };
		Cursor cursor = db.rawQuery("SELECT user_name from "
				+ DataProvider.TABLE_NAME_EBOOK + " WHERE book_id = " + book_id + " and user_name !=? ",
				selects);
		if (cursor == null) {
			return haveOtherUser;
		}
		if (cursor.moveToNext()) {
			haveOtherUser = true;
		}
		cursor.close();
		return haveOtherUser;
	}
	
	public void overrideOtherEbookUser(String uid, long book_id, String book_source) {

		if (LocalBook.SOURCE_TRYREAD_BOOK.equals(book_source)) 
			db.execSQL("update "
				+ DataProvider.TABLE_NAME_EBOOK + " set state_load="+LocalBook.STATE_LOAD_FAILED+",progress=0,size=0  WHERE book_id=" + book_id + " and user_name !='"+uid+"' and source='tryread_book' ");
		else
			db.execSQL("update "
					+ DataProvider.TABLE_NAME_EBOOK + " set state_load="+LocalBook.STATE_LOAD_FAILED+",progress=0,size=0 WHERE book_id=" + book_id + " and user_name !='"+uid+"' and source in ('buyed_book','online_book','borrowed_book') "); 
		return;
	}

	synchronized public void deleteEbook(String userId, Integer[] index,
			Long[] eBookId) {

		
		
		for(int i=0;i<eBookId.length;i++) {
			
			MZLog.d("wangguodong", eBookId[i]+"=============");
			
			db.execSQL("DELETE FROM " + DataProvider.BOOKSHELF + " WHERE  "
					+ DataProvider.USERID + " = '" + userId
					+ "' AND ebook_id="+index[i]);
			db.execSQL("DELETE FROM " + DataProvider.EBOOKNOTE + " WHERE "
					+ DataProvider.USERID + " = '" + userId
					+ "' AND ebookid="+eBookId[i]);
			db.execSQL(
					"DELETE FROM " + DataProvider.BOOKMARK_TABLE
							+ " WHERE userid ='" + userId
							+ "' AND ebookid="+eBookId[i]);
			db.execSQL(
					"DELETE FROM " + DataProvider.BOOKMARKSYNC
							+ " WHERE userid ='" + userId
							+ "' AND ebookid="+eBookId[i]);
			db.execSQL(
					"DELETE FROM " + DataProvider.NOTESYNC + " WHERE userid ='"
							+ userId + "' AND ebookid="+eBookId[i]);
			db.execSQL(
					"DELETE FROM " + DataProvider.PROGRESS + " WHERE userid ='"
							+ userId + "' AND ebookid="+eBookId[i]);
			db.execSQL("DELETE FROM " + DataProvider.TABLE_NAME_EBOOK
					+ " WHERE user_name ='" + userId + "' AND _id="+index[i]);
			db.execSQL("UPDATE " + DataProvider.TABLE_NAME_EBOOK
					+ " SET state_load=4, progress=0,size=0 WHERE book_id="+eBookId[i]);
			db.execSQL("DELETE FROM " + DataProvider.BOOKPAGE
						+ " WHERE ebookid="+eBookId[i]);
			db.execSQL("DELETE FROM " + DataProvider.PAGECONTENT
						+ " WHERE ebookid="+eBookId[i]);
		}

	}
	
	synchronized public void updateOtherEbookState(String userId, long eBookId,String source) {
		if(LocalBook.SOURCE_TRYREAD_BOOK.equals(source))
			db.execSQL("UPDATE " + DataProvider.TABLE_NAME_EBOOK
					+ " SET state_load=4, progress=0,size=0 WHERE book_id="+eBookId+" and user_name!='"+userId+"' and source!='tryread_book'");
		else
			db.execSQL("UPDATE " + DataProvider.TABLE_NAME_EBOOK
					+ " SET state_load=4, progress=0,size=0 WHERE book_id="+eBookId+" and user_name!='"+userId+"' and source not in ('online_book','borrowed_book','buyed_book')");
	}

	// synchronized public int insertOrUpdateDocument(Document doc) {
	// SQLiteDatabase db = dbHelper.getWritableDatabase();
	// int id = 0;
	// Cursor cursor = db.rawQuery("SELECT _id FROM document WHERE opf_md5 = ?",
	// new String[]{doc.opfMD5});
	// ContentValues values = new ContentValues();
	// if (cursor != null && cursor.moveToFirst()) {
	// id = cursor.getInt(0);
	// values.put(_ID, id);
	// } else {
	// values.put(ADD_AT, System.currentTimeMillis());
	// }
	// values.put(TITLE, doc.title);
	// values.put(AUTHOR, doc.author);
	// values.put(COVER_PATH, doc.coverPath);
	// values.put(OPF_MD5, doc.opfMD5);
	//
	// long rowid = db.insertWithOnConflict(DOCUMENT, null, values,
	// SQLiteDatabase.CONFLICT_REPLACE);
	// Cursor cursor1 = db.rawQuery("SELECT last_insert_rowid() FROM document",
	// null);
	// if (cursor1 != null && cursor1.moveToFirst()) {
	// id = cursor1.getInt(0);
	// }
	// MZLog.d("MZBookDatabase", "rowid: " + rowid + " id: " + id);
	// db.close();
	// return id;
	// }

	synchronized public int createDocumentRecord() {
		ContentValues values = new ContentValues();
		values.put(DataProvider.TITLE, "");
		values.put(DataProvider.AUTHOR, "");
		values.put(DataProvider.COVER_PATH, "");
		values.put(DataProvider.ADD_AT, System.currentTimeMillis());
		values.put(DataProvider.OPF_MD5, "");
		values.put(DataProvider.READ_AT, System.currentTimeMillis());
		int id = 0;
		long rowid = db.insert(DataProvider.DOCUMENT, null, values);
		Cursor cursor = db.rawQuery("SELECT last_insert_rowid() FROM document",
				null);
		if (cursor != null && cursor.moveToFirst()) {
			id = cursor.getInt(0);
		}
		if (cursor != null)
			cursor.close();
		MZLog.d("MZBookDatabase", "rowid: " + rowid + " id: " + id);
		return id;
	}

	synchronized public void insertOrUpdateDocument(Document book) {
		ContentValues values = new ContentValues();
		values.put(DataProvider._ID, book.documentId);
		values.put(DataProvider.TITLE, book.title);
		values.put(DataProvider.AUTHOR, book.author);
		values.put(DataProvider.BOOK_PATH, book.bookPath);
		values.put(DataProvider.COVER_PATH, book.coverPath);
		values.put(DataProvider.BOOK_TYPE, book.format);
		values.put(DataProvider.ADD_AT, book.addAt);
		values.put(DataProvider.OPF_MD5, book.opfMD5);
		values.put(DataProvider.READ_AT, book.readAt);

		db.insertWithOnConflict(DataProvider.DOCUMENT, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

	}

	synchronized public long insertToDocument(Document book) {

		String[] selectionArgs = new String[] { book.getOpfMD5() };

		Cursor cursor = db.rawQuery("SELECT _id FROM " + DataProvider.DOCUMENT
				+ " WHERE  " + DataProvider.OPF_MD5 + " = ?", selectionArgs);

		ContentValues values = new ContentValues();
		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider._ID, dbId);
			MZLog.d("wangguodong", "111111");
		}
		if (cursor != null)
			cursor.close();

		values.put(DataProvider.TITLE, book.title);
		values.put(DataProvider.AUTHOR, book.author);
		values.put(DataProvider.COVER_PATH, book.coverPath);
		values.put(DataProvider.ADD_AT, book.addAt);
		values.put(DataProvider.OPF_MD5, book.opfMD5);
		values.put(DataProvider.READ_AT, book.readAt);
		values.put(DataProvider.BOOK_TYPE, book.format);
		values.put(DataProvider.BOOK_PATH, book.bookPath);
		values.put(DataProvider.BOOK_SOURCE, book.bookSource);
		values.put(DataProvider.DOCUMENT_PROGRESS, book.progress);
		values.put(DataProvider.SIZE, book.size);
		values.put(DataProvider.STATE_LOAD, book.state);
		values.put(DataProvider.BOOK_STATE, book.bookState);
		values.put(DataProvider.ACCESS_TIME, book.access_time);
		values.put(DataProvider.MOD_TIME, book.mod_time);
		MZLog.d("wangguodong", "222222");
		return db.insertWithOnConflict(DataProvider.DOCUMENT, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

	}

	synchronized public List<Document> listDocument() {
		List<Document> documentList = new ArrayList<Document>();
		Cursor cursor = db.rawQuery(
				"SELECT * FROM document ORDER BY add_at DESC", null);

		if (cursor == null) {
			return documentList;
		}

		while (cursor.moveToNext()) {
			Document doc = new Document();
			doc.documentId = cursor.getInt(0);
			doc.title = cursor.getString(1);
			doc.author = cursor.getString(2);
			doc.bookPath = cursor.getString(3);
			doc.bookSource = cursor.getString(4);
			doc.format = cursor.getInt(5);
			doc.coverPath = cursor.getString(6);
			doc.size = cursor.getLong(7);
			doc.progress = cursor.getLong(8);
			doc.state = cursor.getInt(9);
			doc.bookState = cursor.getInt(10);
			doc.addAt = cursor.getLong(11);
			doc.access_time = cursor.getLong(12);
			doc.mod_time = cursor.getLong(13);
			doc.readAt = cursor.getLong(15);
			doc.fromCloudDisk = cursor.getInt(16);
			doc.opfMD5 = cursor.getString(17);
			documentList.add(doc);
		}
		cursor.close();
		return documentList;

	}

	// 优化不同用户登录书架显示错误
	synchronized public List<Document> listDocument(String userid) {
		List<Document> documentList = new ArrayList<Document>();
		String[] seleStrings = new String[] { userid + "" };
		Cursor cursor = db
				.rawQuery(
						"SELECT document._id, document.title, document.author,document.book_path,document.book_type,document.cover_path,document.add_at,document.opf_md5,document.readAt,document.state_load "
								+ " FROM docbind,document WHERE document.fromCloudDisk = 1 and docbind.document_id = document._id and docbind.userid = ?",
						seleStrings);
		if (cursor == null) {
			return documentList;
		}

		while (cursor.moveToNext()) {
			Document doc = new Document();
			doc.documentId = cursor.getInt(0);
			doc.title = cursor.getString(1);
			doc.author = cursor.getString(2);
			doc.bookPath = cursor.getString(3);
			doc.format = cursor.getInt(4);
			doc.coverPath = cursor.getString(5);
			doc.addAt = cursor.getLong(6);
			doc.opfMD5 = cursor.getString(7);
			doc.readAt = cursor.getLong(8);
			doc.state = cursor.getInt(9);
			documentList.add(doc);
		}
		cursor.close();
		return documentList;

	}

	// 更新第三方导入书籍最后阅读时间
	synchronized public void updateDocumentLastReadTime(Document document) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.READ_AT, document.readAt);
		db.update(DataProvider.DOCUMENT, values, "_id = ?",
				new String[] { String.valueOf(document.documentId) });
	}

	synchronized public void deleteDocumentRecord(int id, String userId) {
		db.delete(DataProvider.DOCUMENT, "_id = ?",
				new String[] { String.valueOf(id) });
		db.delete(DataProvider.PROGRESS, "document_id = ?",
				new String[] { String.valueOf(id) });
		db.delete(DataProvider.BOOKMARK_TABLE, "docid = ?",
				new String[] { String.valueOf(id) });
		db.delete(DataProvider.BOOKMARKSYNC, "docid = ?",
				new String[] { String.valueOf(id) });
		db.delete(DataProvider.DOCBIND, "document_id = ? AND userid = ?",
				new String[] { String.valueOf(id), String.valueOf(userId) });
		db.delete(DataProvider.EBOOKNOTE, "document_id = ?",
				new String[] { String.valueOf(id) });
		db.delete(DataProvider.NOTESYNC, "document_id = ?",
				new String[] { String.valueOf(id) });
		db.delete(DataProvider.READINGDATA, "document_id = ? AND userid = ?",
				new String[] { String.valueOf(id), String.valueOf(userId) });
		db.delete(DataProvider.BOOKSHELF, "document_id = ?",
				new String[] { String.valueOf(id) });
		db.delete(DataProvider.BOOKPAGE, "document_id = ?",
				new String[] { String.valueOf(id) });
		db.delete(DataProvider.PAGECONTENT, "document_id = ?",
				new String[] { String.valueOf(id) });
	}

	synchronized public void deleteDocumentRecord(Integer[] ids, String userId) {

		String sb = makePlaceholders(ids.length);
		
		db.execSQL("DELETE FROM " + DataProvider.DOCUMENT + " WHERE _id IN ("
				+ sb + ")", (Object[]) ids);

		db.execSQL("DELETE FROM " + DataProvider.PROGRESS + " WHERE  "
				+ DataProvider.USERID + " = '" + userId
				+ "' AND document_id IN (" + sb + ")",
				(Object[]) ids);
		db.execSQL("DELETE FROM " + DataProvider.BOOKMARK_TABLE + " WHERE  "
				+ DataProvider.USERID + " = '" + userId + "' AND "
				+ DataProvider.BOOKMARK_DOCID + " IN (" + sb + ")",
				(Object[]) ids);
		db.execSQL("DELETE FROM " + DataProvider.BOOKMARKSYNC + " WHERE  "
				+ DataProvider.USERID + " = '" + userId + "' AND "
				+ DataProvider.BOOKMARK_DOCID + " IN (" + sb + ")",
				(Object[]) ids);
		db.execSQL("DELETE FROM " + DataProvider.DOCBIND + " WHERE userid = '"
				+ userId + "' AND document_id  IN (" + sb + ")",
				(Object[]) ids);

		db.execSQL("DELETE FROM " + DataProvider.EBOOKNOTE + " WHERE  "
				+ DataProvider.USERID + " = '" + userId
				+ "' AND document_id IN (" + sb + ")",
				(Object[]) ids);
		db.execSQL("DELETE FROM " + DataProvider.NOTESYNC + " WHERE  "
				+ DataProvider.USERID + " = '" + userId
				+ "' AND document_id IN (" + sb + ")",
				(Object[]) ids);
		db.execSQL("DELETE FROM " + DataProvider.READINGDATA + " WHERE  "
				+ DataProvider.USERID + " = '" + userId
				+ "' AND document_id IN (" + sb + ")",
				(Object[]) ids);
		db.execSQL("DELETE FROM " + DataProvider.BOOKSHELF
				+ " WHERE   document_id IN (" + sb + ")",
				(Object[]) ids);

		db.execSQL("DELETE FROM " + DataProvider.BOOKPAGE
				+ " WHERE document_id IN (" + sb + ")",
				(Object[]) ids);
		db.execSQL("DELETE FROM " + DataProvider.PAGECONTENT
				+ " WHERE document_id IN (" + sb + ")",
				(Object[]) ids);

	}

	synchronized public void updateDocument(Document doc) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.TITLE, doc.title);
		values.put(DataProvider.AUTHOR, doc.author);
		values.put(DataProvider.COVER_PATH, doc.coverPath);
		values.put(DataProvider.OPF_MD5, doc.opfMD5);
		values.put(DataProvider.BOOK_PATH, doc.bookPath);
		values.put(DataProvider.STATE_LOAD, doc.state);
		values.put(DataProvider.FROM_CLOUDDISK, doc.fromCloudDisk);
		values.put(DataProvider.BOOK_SOURCE, doc.bookSource);
		values.put(DataProvider.BOOK_TYPE, doc.format);

		db.update(DataProvider.DOCUMENT, values, "_id = ?",
				new String[] { String.valueOf(doc.documentId) });
	}

	synchronized public void updateLocalDocumentBookSource(LocalDocument doc) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.BOOK_SOURCE, doc.bookSource);

		int x = db.update(DataProvider.DOCUMENT, values, "_id = ?",
				new String[] { String.valueOf(doc._id) });
		MZLog.d("wangguodong", x + "=======");
	}

	synchronized public void insertMention(Mention m) {

		List<Mention> list = getAllMention();

		if(m.getMentionBookId()==0) return;
		
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getMentionBookId() == m.getMentionBookId())
				return;
		}

		ContentValues values = new ContentValues();
		values.put(DataProvider.ME_BOOK_ID, m.getMentionBookId());
		values.put(DataProvider.ME_BOOK_NAME, m.getMentionBookName());
		values.put(DataProvider.ME_BOOK_TIME, m.getMentionAt());
//		values.put(DataProvider.ME_BOOK_AUTHOR, m.getAuthor());
//		values.put(DataProvider.ME_BOOK_COVER, m.getBookCover());
		db.insert(DataProvider.MENTION, null, values);
	}

	synchronized public void clearMention() {
		// 优化
		db.execSQL("delete from mention where _id not in (select _id from mention order by _id desc limit 0,5)");
	}

	synchronized public List<Mention> getAllMention() {
		List<Mention> dataList = new ArrayList<Mention>();
		Cursor cursor = db.rawQuery("SELECT * FROM " + DataProvider.MENTION
				+ " ORDER BY " + DataProvider.ME_BOOK_TIME + " DESC LIMIT 5",
				null);
		if (cursor == null) {
			return null;
		}

		while (cursor.moveToNext()) {

			Mention mention = new Mention();
			mention.set_id(cursor.getInt(0));
			mention.setMentionBookId(cursor.getInt(1));
			mention.setMentionBookName(cursor.getString(2));
			mention.setMentionAt(cursor.getInt(3));

			dataList.add(mention);
		}

		cursor.close();
		return dataList;
	}

	synchronized public void insertReadingData(ReadingData data) {

		if (data.getEbook_id() == 0 && data.getDocument_id() == 0)
			return;
		ContentValues values = new ContentValues();
		values.put(DataProvider.RD_EBOOKID, data.getEbook_id());
		values.put(DataProvider.RD_DOCUMENTID, data.getDocument_id());
		values.put(DataProvider.RD_STARTTIME, data.getStart_time());
		values.put(DataProvider.RD_CHAPTER, data.getStart_chapter());
		values.put(DataProvider.RD_PARAIDX, data.getStart_para_idx());
		values.put(DataProvider.RD_PDF_PAGE, data.getStart_pdf_page());
		values.put(DataProvider.RD_ENDTIME, data.getEnd_time());
		values.put(DataProvider.RD_ENDCHAPTER, data.getEnd_chapter());
		values.put(DataProvider.RD_ENDPARAIDX, data.getEnd_para_idx());
		values.put(DataProvider.RD_END_PDF_PAGE, data.getEnd_pdf_page());
		values.put(DataProvider.RD_LENGTH, data.getLength());
		values.put(DataProvider.USERID, data.getUserId());
		db.insert(DataProvider.READINGDATA, null, values);
	}

	synchronized public long getBookReadingDataTime(long bookId, int docId) {
		long time = 0;
		String[] selectionArgs = new String[] { String.valueOf(bookId),  String.valueOf(docId)};
		Cursor cursor = db.rawQuery("SELECT * FROM readingdata WHERE ebook_id = ? and document_id = ?", selectionArgs);
		if (cursor == null) {
			return -1;
		}
		
		while (cursor.moveToNext()) {
			ReadingData data = new ReadingData();
			data.set_id(cursor.getInt(0));
			data.setDocument_id(cursor.getInt(1));
			data.setEbook_id(cursor.getLong(2));
			data.setUserId(cursor.getString(3));
			data.setStart_time(cursor.getLong(4));
			data.setStart_chapter(cursor.getString(5));
			data.setStart_para_idx(cursor.getInt(6));
			data.setStart_pdf_page(cursor.getInt(7));
			data.setEnd_time(cursor.getLong(8));
			data.setEnd_chapter(cursor.getString(9));
			data.setEnd_para_idx(cursor.getInt(10));
			data.setEnd_pdf_page(cursor.getInt(11));
			data.setLength(cursor.getLong(12));
			time += data.getEnd_time() - data.getStart_time();
		}
		cursor.close();
		return time;
	}
	
	synchronized public List<ReadingData> getAllReadingData() {
		List<ReadingData> dataList = new ArrayList<ReadingData>();
		Cursor cursor = db.rawQuery("SELECT * FROM readingdata", null);
		if (cursor == null) {
			return null;
		}

		while (cursor.moveToNext()) {
			ReadingData data = new ReadingData();
			data.set_id(cursor.getInt(0));
			data.setDocument_id(cursor.getInt(1));
			data.setEbook_id(cursor.getLong(2));
			data.setUserId(cursor.getString(3));
			data.setStart_time(cursor.getLong(4));
			data.setStart_chapter(cursor.getString(5));
			data.setStart_para_idx(cursor.getInt(6));
			data.setStart_pdf_page(cursor.getInt(7));
			data.setEnd_time(cursor.getLong(8));
			data.setEnd_chapter(cursor.getString(9));
			data.setEnd_para_idx(cursor.getInt(10));
			data.setEnd_pdf_page(cursor.getInt(11));
			data.setLength(cursor.getLong(12));
			if (data.getDocument_id() != 0) {
				DocBind b = getDocBind(data.getDocument_id(), data.getUserId());
				// 如果serverid==0的话不要保存note
				if (b != null && b.serverId != 0) {
					data.setDocBindId(b.serverId);
					dataList.add(data);
				}
			} else if (data.getEbook_id() != 0) {
				dataList.add(data);
			}
		}
		cursor.close();
		return dataList;
	}

	synchronized public void deleteReadingData(int id) {

		db.delete(DataProvider.READINGDATA, "_id = ?",
				new String[] { String.valueOf(id) });
	}

	synchronized public void deleteReadingData() {
		Cursor cursor = db.rawQuery("SELECT * FROM readingdata", null);
		if (cursor == null) {
			return;
		}

		while (cursor.moveToNext()) {
			ReadingData data = new ReadingData();
			data.set_id(cursor.getInt(0));
			data.setDocument_id(cursor.getInt(1));
			data.setEbook_id(cursor.getLong(2));
			data.setUserId(cursor.getString(3));
			if (data.getDocument_id() != 0) {
				DocBind b = getDocBind(data.getDocument_id(), data.getUserId());
				// 如果serverid==0的话不要保存note
				if (b != null && b.serverId != 0) {
					deleteReadingData(data.get_id());
				}
			} else if (data.getEbook_id() != 0) {
				deleteReadingData(data.get_id());
			}
		}
		cursor.close();
	}

	synchronized public void insertOrUpdateEbookNote(ReadNote note) {
		if (note.ebookId == 0 && note.documentId == 0) {
			return;
		}
		ContentValues values = new ContentValues();
		if (note.id != 0) {
			values.put(DataProvider._ID, note.id);
		}

		if (note.serverId != -1) {
			Cursor cursor = db.rawQuery(
					"SELECT _id FROM ebooknote WHERE server_note_id = ?",
					new String[] { String.valueOf(note.serverId) });
			if (cursor != null && cursor.moveToFirst()) {
				note.id = cursor.getInt(0);
				values.put(DataProvider._ID, note.id);
				cursor.close();
			}
		}

		values.put(DataProvider.SERVER_NOTE_ID, note.serverId);
		values.put(DataProvider.EBOOKID, note.ebookId);
		values.put(DataProvider.USERID, note.userId);
		values.put(DataProvider.CHAPTER_NAME, note.chapterName);
		values.put(DataProvider.START_PARA_IDX, note.fromParaIndex);
		values.put(DataProvider.START_OFFSET_IN_PARA, note.fromOffsetInPara);
		values.put(DataProvider.END_PARA_IDX, note.toParaIndex);
		values.put(DataProvider.END_OFFSET_IN_PARA, note.toOffsetInPara);
		values.put(DataProvider.QUOTE, note.quoteText);
		values.put(DataProvider.CONTENT, note.contentText);
		values.put(DataProvider.UPDATE_TIME, note.updateTime);
		values.put(DataProvider.IS_PRIVATE, note.isPrivate);
		values.put(DataProvider.MODIFIED, note.modified);
		values.put(DataProvider.DELETED, note.deleted);
		values.put(DataProvider.DOCUMENT_ID, note.documentId);
		values.put(DataProvider.CHAPTER_ITEMREF, note.spineIdRef);
		values.put(DataProvider.TIMELINE_GUID, note.guid);
		long id = db.insertWithOnConflict(DataProvider.EBOOKNOTE, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
		if (id != -1) {
			note.id = (int) id;
		}
	}

	synchronized public void cleanReadNote() {
		db.delete(DataProvider.EBOOKNOTE,
				"server_note_id = -1 AND deleted = 1", null);
		db.delete(DataProvider.EBOOKNOTE, "deleted = 1 AND modified = 0", null);
	}

	synchronized public void deleteReadNote(int serverId) {
		db.delete(DataProvider.EBOOKNOTE, "server_note_id = ?",
				new String[] { String.valueOf(serverId) });
	}

	synchronized public List<ReadNote> listAllUnsyncReadNote(String userId) {
		List<ReadNote> noteList = new ArrayList<ReadNote>();
		String[] selectionArgs = new String[] { userId };
		Cursor cursor = db.rawQuery(
				"SELECT * FROM ebooknote WHERE userid = ? and modified = 1",
				selectionArgs);
		if (cursor == null) {
			return noteList;
		}

		while (cursor.moveToNext()) {
			ReadNote note = new ReadNote();
			note.id = cursor.getInt(0);
			note.serverId = cursor.getLong(1);
			note.ebookId = cursor.getLong(2);
			note.userId = cursor.getString(3);
			note.chapterName = cursor.getString(4);
			note.fromParaIndex = cursor.getInt(5);
			note.fromOffsetInPara = cursor.getInt(6);
			note.toParaIndex = cursor.getInt(7);
			note.toOffsetInPara = cursor.getInt(8);
			note.quoteText = cursor.getString(9);
			note.contentText = cursor.getString(10);
			note.updateTime = cursor.getLong(11);
			note.guid = cursor.getString(12);
			note.isPrivate = (cursor.getInt(13) == 0) ? false : true;
			note.modified = (cursor.getInt(14) == 0) ? false : true;
			note.deleted = (cursor.getInt(15) == 0) ? false : true;
			note.documentId = cursor.getInt(16);
			note.spineIdRef = cursor.getString(17);
			note.pdfPage = cursor.getInt(18);
			note.pdf_yoffset = cursor.getFloat(19);
			note.pdf_xoffset = cursor.getFloat(20);
			if (note.documentId != 0) {
				DocBind b = getDocBind(note.documentId, userId);
				// 如果serverid==0的话不要保存note
				if (b != null && b.serverId != 0) {
					note.docBindId = b.serverId;
					noteList.add(note);
				}
			} else if (note.ebookId != 0) {
				noteList.add(note);
			}
		}
		cursor.close();
		return noteList;

	}

	synchronized public List<ReadNote> listEBookReadNote(String userId,
			long eBookId) {
		List<ReadNote> noteList = new ArrayList<ReadNote>();
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT * FROM ebooknote WHERE userid = ? and ebookid = ? and deleted = 0",
						selectionArgs);
		if (cursor == null) {
			return noteList;
		}

		while (cursor.moveToNext()) {
			ReadNote note = new ReadNote();
			note.id = cursor.getInt(0);
			note.serverId = cursor.getLong(1);
			note.ebookId = cursor.getLong(2);
			note.userId = cursor.getString(3);
			note.chapterName = cursor.getString(4);
			note.fromParaIndex = cursor.getInt(5);
			note.fromOffsetInPara = cursor.getInt(6);
			note.toParaIndex = cursor.getInt(7);
			note.toOffsetInPara = cursor.getInt(8);
			note.quoteText = cursor.getString(9);
			note.contentText = cursor.getString(10);
			note.updateTime = cursor.getLong(11);
			note.guid = cursor.getString(12);
			note.isPrivate = (cursor.getInt(13) == 0) ? false : true;
			note.modified = (cursor.getInt(14) == 0) ? false : true;
			note.deleted = (cursor.getInt(15) == 0) ? false : true;
			note.documentId = cursor.getInt(16);
			note.spineIdRef = cursor.getString(17);
			note.pdfPage = cursor.getInt(18);
			note.pdf_yoffset = cursor.getFloat(19);
			note.pdf_xoffset = cursor.getFloat(20);
			noteList.add(note);
		}
		cursor.close();
		return noteList;

	}

	synchronized public List<ReadNote> listDocReadNote(String userId,
			int documentId) {
		List<ReadNote> noteList = new ArrayList<ReadNote>();
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(documentId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT * FROM ebooknote WHERE userid = ? and document_id = ? and deleted = 0",
						selectionArgs);
		if (cursor == null) {
			return noteList;
		}

		while (cursor.moveToNext()) {
			ReadNote note = new ReadNote();
			note.id = cursor.getInt(0);
			note.serverId = cursor.getLong(1);
			note.ebookId = cursor.getLong(2);
			note.userId = cursor.getString(3);
			note.chapterName = cursor.getString(4);
			note.fromParaIndex = cursor.getInt(5);
			note.fromOffsetInPara = cursor.getInt(6);
			note.toParaIndex = cursor.getInt(7);
			note.toOffsetInPara = cursor.getInt(8);
			note.quoteText = cursor.getString(9);
			note.contentText = cursor.getString(10);
			note.updateTime = cursor.getLong(11);
			note.guid = cursor.getString(12);
			note.isPrivate = (cursor.getInt(13) == 0) ? false : true;
			note.modified = (cursor.getInt(14) == 0) ? false : true;
			note.deleted = (cursor.getInt(15) == 0) ? false : true;
			note.documentId = cursor.getInt(16);
			note.spineIdRef = cursor.getString(17);
			note.pdfPage = cursor.getInt(18);
			note.pdf_yoffset = cursor.getFloat(19);
			note.pdf_xoffset = cursor.getFloat(20);
			noteList.add(note);
		}
		cursor.close();
		return noteList;

	}

	synchronized public List<ReadNote> listChapterReadNote(long eBookId,
			int documentId, String chapterItemRef) {
		List<ReadNote> noteList = new ArrayList<ReadNote>();
		Cursor cursor = null;
		if (eBookId != 0) {
			String[] selectionArgs = new String[] { chapterItemRef,
					String.valueOf(eBookId) };
			cursor = db
					.rawQuery(
							"SELECT * FROM ebooknote WHERE chapter_itemref = ? AND ebookid = ?",
							selectionArgs);
		} else if (documentId != 0) {
			String[] selectionArgs = new String[] { chapterItemRef,
					String.valueOf(documentId) };
			cursor = db
					.rawQuery(
							"SELECT * FROM ebooknote WHERE chapter_itemref = ? AND document_id = ?",
							selectionArgs);
		}
		if (cursor == null) {
			return noteList;
		}

		while (cursor.moveToNext()) {
			ReadNote note = new ReadNote();
			note.id = cursor.getInt(0);
			note.serverId = cursor.getLong(1);
			note.ebookId = cursor.getLong(2);
			note.userId = cursor.getString(3);
			note.chapterName = cursor.getString(4);
			note.fromParaIndex = cursor.getInt(5);
			note.fromOffsetInPara = cursor.getInt(6);
			note.toParaIndex = cursor.getInt(7);
			note.toOffsetInPara = cursor.getInt(8);
			note.quoteText = cursor.getString(9);
			note.contentText = cursor.getString(10);
			note.updateTime = cursor.getLong(11);
			note.guid = cursor.getString(12);
			note.isPrivate = (cursor.getInt(13) == 0) ? false : true;
			note.modified = (cursor.getInt(14) == 0) ? false : true;
			note.deleted = (cursor.getInt(15) == 0) ? false : true;
			note.documentId = cursor.getInt(16);
			note.spineIdRef = cursor.getString(17);
			note.pdfPage = cursor.getInt(18);
			note.pdf_yoffset = cursor.getFloat(19);
			note.pdf_xoffset = cursor.getFloat(20);
			noteList.add(note);
		}
		cursor.close();
		return noteList;
	}

	synchronized public List<ReadNote> listChapterReadNote(String userId,
			int eBookId, int documentId, String chapterItemRef) {
		List<ReadNote> noteList = new ArrayList<ReadNote>();
		Cursor cursor = null;
		if (eBookId != 0) {
			String[] selectionArgs = new String[] { chapterItemRef,
					String.valueOf(userId), String.valueOf(eBookId) };
			cursor = db
					.rawQuery(
							"SELECT * FROM ebooknote WHERE chapter_itemref = ? AND userid = ? AND ebookid = ?",
							selectionArgs);
		} else if (documentId != 0) {
			String[] selectionArgs = new String[] { chapterItemRef,
					String.valueOf(userId), String.valueOf(documentId) };
			cursor = db
					.rawQuery(
							"SELECT * FROM ebooknote WHERE chapter_itemref = ? AND userid = ? AND document_id = ?",
							selectionArgs);
		}
		if (cursor == null) {
			return noteList;
		}

		while (cursor.moveToNext()) {
			ReadNote note = new ReadNote();
			note.id = cursor.getInt(0);
			note.serverId = cursor.getLong(1);
			note.ebookId = cursor.getLong(2);
			note.userId = cursor.getString(3);
			note.chapterName = cursor.getString(4);
			note.fromParaIndex = cursor.getInt(5);
			note.fromOffsetInPara = cursor.getInt(6);
			note.toParaIndex = cursor.getInt(7);
			note.toOffsetInPara = cursor.getInt(8);
			note.quoteText = cursor.getString(9);
			note.contentText = cursor.getString(10);
			note.updateTime = cursor.getLong(11);
			note.guid = cursor.getString(12);
			note.isPrivate = (cursor.getInt(13) == 0) ? false : true;
			note.modified = (cursor.getInt(14) == 0) ? false : true;
			note.deleted = (cursor.getInt(15) == 0) ? false : true;
			note.documentId = cursor.getInt(16);
			note.spineIdRef = cursor.getString(17);
			note.pdfPage = cursor.getInt(18);
			note.pdf_yoffset = cursor.getFloat(19);
			note.pdf_xoffset = cursor.getFloat(20);
			noteList.add(note);
		}
		cursor.close();
		return noteList;
	}

	synchronized public long getNoteSyncTime(String userId, long eBookId,
			int docId) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId), String.valueOf(docId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT last_update_time FROM notesync WHERE userid = ? AND ebookid = ? AND document_id = ?",
						selectionArgs);
		long lastSyncTime = 0;
		if (cursor != null && cursor.moveToFirst()) {
			lastSyncTime = cursor.getLong(0);
		}
		if (cursor != null)
			cursor.close();
		return lastSyncTime;
	}

	synchronized public void insertOrUpdateNoteSyncTime(String userId,
			long eBookId, int docId, long lastUpdateTime) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId), String.valueOf(docId) };
		Cursor cursor = db
				.rawQuery(
						"SELECT _id FROM notesync WHERE userid = ? AND ebookid = ? AND document_id = ?",
						selectionArgs);
		ContentValues values = new ContentValues();
		values.put(DataProvider.USERID, userId);
		values.put(DataProvider.EBOOKID, eBookId);
		values.put(DataProvider.DOCUMENT_ID, docId);
		values.put(DataProvider.LAST_UPDATE_TIME, lastUpdateTime);

		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider._ID, dbId);
		}
		if (cursor != null)
			cursor.close();
		db.insertWithOnConflict(DataProvider.NOTESYNC, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	synchronized public void updateNoteSyncTime(String userId, long eBookId,
			int docId, long lastUpdateTime, int noteCount, String targetid) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId), String.valueOf(docId),
				String.valueOf(targetid) };
		Cursor cursor = db
				.rawQuery(
						"SELECT _id FROM notesync WHERE userid = ? AND ebookid = ? AND document_id = ? AND target_user_id =?",
						selectionArgs);

		ContentValues values = new ContentValues();
		values.put(DataProvider.TARGET_USER_NOTECOUNT, noteCount);
		values.put(DataProvider.LAST_UPDATE_TIME, lastUpdateTime);
		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider._ID, dbId);
			db.updateWithOnConflict(DataProvider.NOTESYNC, values,
					DataProvider._ID + " =?", new String[] { dbId + "" },
					SQLiteDatabase.CONFLICT_REPLACE);
		}
		if (cursor != null)
			cursor.close();

	}
	
	synchronized public void updateNoteCount(String userId, long eBookId,
			int docId, int noteCount, String targetid) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId), String.valueOf(docId),
				String.valueOf(targetid) };
		Cursor cursor = db
				.rawQuery(
						"SELECT _id FROM notesync WHERE userid = ? AND ebookid = ? AND document_id = ? AND target_user_id =?",
						selectionArgs);

		ContentValues values = new ContentValues();
		values.put(DataProvider.TARGET_USER_NOTECOUNT, noteCount);
		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider._ID, dbId);
			db.updateWithOnConflict(DataProvider.NOTESYNC, values,
					DataProvider._ID + " =?", new String[] { dbId + "" },
					SQLiteDatabase.CONFLICT_REPLACE);
		}
		if (cursor != null)
			cursor.close();

	}

	synchronized public boolean isNotesImported(String userId, long eBookId,
			int docId, String targetid) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId), String.valueOf(docId),
				String.valueOf(targetid) };
		Cursor cursor = db
				.rawQuery(
						"SELECT _id FROM notesync WHERE userid = ? AND ebookid = ? AND document_id = ? AND target_user_id =?",
						selectionArgs);
		int dbId = -1;
		if (cursor != null && cursor.moveToFirst()) {
			dbId = cursor.getInt(0);
		}
		if (cursor != null)
			cursor.close();
		if (dbId != -1)
			return true;
		return false;
	}

	synchronized public List<NotesModel> listAllNotesModel(String userId,
			long eBookId, int docId) {

		List<NotesModel> allList = new ArrayList<NotesModel>();

		String targetUserSql = "SELECT target_user_id,target_user_nickname,target_user_avatar,target_user_role,target_user_pin,target_user_notecount FROM "
				+ DataProvider.NOTESYNC
				+ " WHERE "
				+ DataProvider.EBOOKID
				+ " =? AND "
				+ DataProvider.DOCUMENT_ID
				+ " =? AND "
				+ DataProvider.USERID
				+ " =? "
				+ " AND "
				+ DataProvider.TARGET_USER_ID + " !=0";

		Cursor targetUserCursor = db.rawQuery(targetUserSql,
				new String[] { String.valueOf(eBookId), String.valueOf(docId),
						String.valueOf(userId) });

		if (targetUserCursor == null)
			return null;

		while (targetUserCursor.moveToNext()) {

			NotesModel model = new NotesModel();

			model.userid = targetUserCursor.getString(0);

			model.userName = targetUserCursor.getString(1);

			model.avatarUrl = targetUserCursor.getString(2);

			model.role = targetUserCursor.getInt(3);

			model.jd_user_name = targetUserCursor.getString(4);
			
			model.noteCount = targetUserCursor.getInt(5);
			
			model.state = NotesModel.UNSELECTED;

			allList.add(model);
		}
		targetUserCursor.close();

		// 书友笔记数量已存数据库了，因此不用这段逻辑
//		for (int i = 0; i < allList.size(); i++) {
//
//			NotesModel model = allList.get(i);
//
//			model.state = NotesModel.UNSELECTED;
//
//			String sql = "SELECT COUNT(*) FROM " + DataProvider.EBOOKNOTE
//					+ " WHERE " + DataProvider.EBOOKID + " =? AND "
//					+ DataProvider.DOCUMENT_ID + " =? AND "
//					+ DataProvider.USERID + " =?";
//
//			Cursor temCursor = db
//					.rawQuery(
//							sql,
//							new String[] { String.valueOf(eBookId),
//									String.valueOf(docId),
//									String.valueOf(model.userid) });
//
//			if (temCursor != null && temCursor.moveToFirst())
//				model.noteCount = temCursor.getInt(0);
//			else
//				model.noteCount = 0;
//
//			temCursor.close();
//			allList.set(i, model);
//		}

		return allList;

	}

	synchronized public void deletImportNotes(String userId, long eBookId,
			int docId, String targetid) {

		db.delete(DataProvider.NOTESYNC, DataProvider.USERID + " =? AND "
				+ DataProvider.EBOOKID + " =? AND " + DataProvider.DOCUMENT_ID
				+ " =? AND " + DataProvider.TARGET_USER_ID + " =?",
				new String[] { String.valueOf(userId), String.valueOf(eBookId),
						String.valueOf(docId), String.valueOf(targetid) });

		db.delete(
				DataProvider.EBOOKNOTE,
				DataProvider.USERID + " =? AND " + DataProvider.EBOOKID
						+ " =? AND " + DataProvider.DOCUMENT_ID + " =? ",
				new String[] { String.valueOf(targetid),
						String.valueOf(eBookId), String.valueOf(docId) });

	}

	synchronized public long insertOrUpdateNoteSyncTime(String userId,
			long eBookId, int docId, NotesModel target) {
		String[] selectionArgs = new String[] { String.valueOf(userId),
				String.valueOf(eBookId), String.valueOf(docId),
				String.valueOf(target.userid) };
		Cursor cursor = db
				.rawQuery(
						"SELECT _id FROM "
								+ DataProvider.NOTESYNC
								+ " WHERE userid = ? AND ebookid = ? AND document_id = ? AND target_user_id =?",
						selectionArgs);
		ContentValues values = new ContentValues();
		values.put(DataProvider.USERID, userId);
		values.put(DataProvider.EBOOKID, eBookId);
		values.put(DataProvider.DOCUMENT_ID, docId);

		values.put(DataProvider.TARGET_USER_ID, target.userid);
		values.put(DataProvider.TARGET_USER_PIN, target.jd_user_name);
		values.put(DataProvider.TARGET_USER_NICKNAME, target.userName);
		values.put(DataProvider.TARGET_USER_AVATAR, target.avatarUrl);
		values.put(DataProvider.TARGET_USER_NOTECOUNT, target.noteCount);
		values.put(DataProvider.TARGET_USER_ROLE, target.role);
		values.put(DataProvider.TARGET_USER_SUMMARY, "");

		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider._ID, dbId);
		}
		if (cursor != null)
			cursor.close();
		long key = db.insertWithOnConflict(DataProvider.NOTESYNC, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
		return key;
	}

	synchronized public long getLocalDocumentId(long documentId, String sign) {
		long localDocumentId = -1;
		String[] bindSelction = new String[] { sign };
		String[] docSelection = new String[] { String.valueOf(documentId) };
		String bindRaw = "SELECT " + DataProvider._ID + " FROM "
				+ DataProvider.DOCUMENT + " WHERE " + DataProvider.OPF_MD5
				+ " = ?";
		String docRaw = "SELECT " + DataProvider.DOCUMENT_ID + " FROM "
				+ DataProvider.DOCBIND + " WHERE " + DataProvider.SERVER_ID
				+ " = ?";
		Cursor cursor = db.rawQuery(bindRaw, bindSelction);
		if (cursor != null && cursor.moveToFirst())
			localDocumentId = cursor.getLong(0);
		cursor.close();
		if (localDocumentId == -1) {
			cursor = db.rawQuery(docRaw, docSelection);
			if (cursor != null && cursor.moveToFirst()) {
				localDocumentId = cursor.getLong(0);
			}
			if (cursor != null)
				cursor.close();
		}
		return localDocumentId;
	}

	synchronized public void insertOrUpdateBookPage(long eBookId, int docId,
			ReadBookPage bookPage) {
		String[] selectionArgs = new String[] { String.valueOf(eBookId),
				String.valueOf(docId) };
		Cursor cursor = db.rawQuery("SELECT _id FROM " + DataProvider.BOOKPAGE
				+ " WHERE " + DataProvider.EBOOKID + " = ? AND "
				+ DataProvider.DOCUMENT_ID + " = ?", selectionArgs);
		ContentValues values = new ContentValues();
		values.put(DataProvider.EBOOKID, eBookId);
		values.put(DataProvider.DOCUMENT_ID, docId);
		values.put(DataProvider.BOOK_TEXT_SIZE, bookPage.getTextSize());
		values.put(DataProvider.BOOK_CHAPTER_PAGE, bookPage.getChapterPage());
		values.put(DataProvider.BOOK_CHAPTER_BLOCK,
				bookPage.getChapterBlockCount());
		values.put(DataProvider.BOOK_FONT_FACE, bookPage.getFontFace());
		values.put(DataProvider.BOOK_LINE_SPACE, bookPage.getLineSpace());
		values.put(DataProvider.BOOK_BLOCK_SPACE, bookPage.getBlockSpace());
		values.put(DataProvider.BOOK_PAGE_EDGE_SPACE, bookPage.getPageEdgeSpace());
		values.put(DataProvider.BOOK_SCREEN_MODE, bookPage.getScreenMode());

		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider._ID, dbId);
		}
		if (cursor != null)
			cursor.close();
		db.insertWithOnConflict(DataProvider.BOOKPAGE, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	synchronized public ReadBookPage getBookPage(long eBookId, int docId) {
		ReadBookPage bookPage = null;
		String[] selectionArgs = new String[] { String.valueOf(eBookId),String.valueOf(docId) };
		Cursor cursor = db.rawQuery("SELECT " + DataProvider.BOOK_TEXT_SIZE
				+ ", " + DataProvider.BOOK_CHAPTER_PAGE + ", "
				+ DataProvider.BOOK_CHAPTER_BLOCK + ", "
				+ DataProvider.BOOK_FONT_FACE + ", "
				+ DataProvider.BOOK_LINE_SPACE + ", "
				+ DataProvider.BOOK_BLOCK_SPACE + ", "
				+ DataProvider.BOOK_PAGE_EDGE_SPACE + ", "
				+ DataProvider.BOOK_SCREEN_MODE + " FROM "
				+ DataProvider.BOOKPAGE + " WHERE " + DataProvider.EBOOKID
				+ " = ? AND " + DataProvider.DOCUMENT_ID + " = ?",
				selectionArgs);
		if (cursor != null && cursor.moveToFirst()) {
			bookPage = new ReadBookPage();
			//文字大小
			bookPage.setTextSize(cursor.getInt(cursor.getColumnIndex(DataProvider.BOOK_TEXT_SIZE)));
			//
			bookPage.setChapterPage(cursor.getString(cursor.getColumnIndex(DataProvider.BOOK_CHAPTER_PAGE)));
			//段间距
			bookPage.setChapterBlockCount(cursor.getString(cursor.getColumnIndex(DataProvider.BOOK_CHAPTER_BLOCK)));
			//字体
			bookPage.setFontFace(cursor.getString(cursor.getColumnIndex(DataProvider.BOOK_FONT_FACE)));
			//
			bookPage.setLineSpace(cursor.getInt(cursor.getColumnIndex(DataProvider.BOOK_LINE_SPACE)));
			//
			bookPage.setBlockSpace(cursor.getInt(cursor.getColumnIndex(DataProvider.BOOK_BLOCK_SPACE)));
			bookPage.setPageEdgeSpace(cursor.getInt(cursor.getColumnIndex(DataProvider.BOOK_PAGE_EDGE_SPACE)));
			bookPage.setScreenMode(cursor.getInt(cursor.getColumnIndex(DataProvider.BOOK_SCREEN_MODE)));
		}
		if (cursor != null)
			cursor.close();
		return bookPage;
	}

	synchronized public void insertOrUpdatePageContent(long eBookId, int docId,
			ReadPageContent pageContent) {

		String[] selectionArgs = new String[] { String.valueOf(eBookId),
				String.valueOf(docId), pageContent.getChapterItemRef() };
		Cursor cursor = db.rawQuery("SELECT _id FROM "
				+ DataProvider.PAGECONTENT + " WHERE " + DataProvider.EBOOKID
				+ " = ? AND " + DataProvider.DOCUMENT_ID + " = ? AND "
				+ DataProvider.CHAPTER_ITEMREF + " = ?", selectionArgs);

		ContentValues values = new ContentValues();
		values.put(DataProvider.EBOOKID, eBookId);
		values.put(DataProvider.DOCUMENT_ID, docId);
		values.put(DataProvider.BOOK_TEXT_SIZE, pageContent.getTextSize());
		values.put(DataProvider.CHAPTER_ITEMREF,
				pageContent.getChapterItemRef());
		values.put(DataProvider.PAGE_START_PARA, pageContent.getPageStartPara());
		values.put(DataProvider.PAGE_START_OFFSET,
				pageContent.getPageStartOffset());
		values.put(DataProvider.ANCHOR_LOCATION,
				pageContent.getAnchorLocation());
		values.put(DataProvider.BOOK_FONT_FACE, pageContent.getFontFace());
		values.put(DataProvider.BOOK_LINE_SPACE, pageContent.getLineSpace());
		values.put(DataProvider.BOOK_BLOCK_SPACE, pageContent.getBlockSpace());
		values.put(DataProvider.BOOK_PAGE_EDGE_SPACE, pageContent.getPageEdgeSpace());
		values.put(DataProvider.BOOK_SCREEN_MODE, pageContent.getScreenMode());

		if (cursor != null && cursor.moveToFirst()) {
			int dbId = cursor.getInt(0);
			values.put(DataProvider._ID, dbId);
		}
		if (cursor != null)
			cursor.close();
		db.insertWithOnConflict(DataProvider.PAGECONTENT, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	synchronized public void deleteBookPage(int eBookId, int docId) {
		if (eBookId > 0) {
			db.delete(DataProvider.BOOKPAGE, DataProvider.EBOOKID + " = ?",
					new String[] { String.valueOf(eBookId) });
		}
		if (docId > 0) {
			db.delete(DataProvider.BOOKPAGE, DataProvider.DOCUMENT_ID + " = ?",
					new String[] { String.valueOf(docId) });
		}
	}

	synchronized public void deleteBookPage(Integer[] eBookId, Integer[] docId) {

		if (eBookId.length > 0) {
			String sb = makePlaceholders(eBookId.length);
			
			db.execSQL("DELETE FROM " + DataProvider.BOOKPAGE + " WHERE "
					+ DataProvider.EBOOKID + " IN (" + sb + ")",
					(Object[]) eBookId);
		}
		if (docId.length > 0) {
			String sb = makePlaceholders(docId.length);
			
			db.execSQL("DELETE FROM " + DataProvider.BOOKPAGE + " WHERE "
					+ DataProvider.EBOOKID + " IN (" + sb + ")",
					(Object[]) docId);
		}

	}

	synchronized public void deletePageContent(long eBookId, int docId) {
		if (eBookId > 0) {
			db.delete(DataProvider.PAGECONTENT, DataProvider.EBOOKID + " = ?",
					new String[] { String.valueOf(eBookId) });
		}
		if (docId > 0) {
			db.delete(DataProvider.PAGECONTENT, DataProvider.DOCUMENT_ID
					+ " = ?", new String[] { String.valueOf(docId) });
		}
	}

	synchronized public void deletePageContent(Integer[] eBookId,
			Integer[] docId) {

		if (eBookId.length > 0) {
			String sb = makePlaceholders(eBookId.length);
			
			db.execSQL("DELETE FROM " + DataProvider.PAGECONTENT + " WHERE "
					+ DataProvider.EBOOKID + " IN (" + sb + ")",
					(Object[]) eBookId);
		}
		if (docId.length > 0) {
			String sb = makePlaceholders(docId.length);
			db.execSQL("DELETE FROM " + DataProvider.PAGECONTENT + " WHERE "
					+ DataProvider.DOCUMENT_ID + " IN (" + sb + ")",
					(Object[]) docId);
		}

	}

	synchronized public ReadPageContent getPageContent(long eBookId, int docId,
			String chapterItemRef) {
		ReadPageContent pageContent = null;
		String[] selectionArgs = new String[] { String.valueOf(eBookId),String.valueOf(docId), chapterItemRef };
		Cursor cursor = db.rawQuery("SELECT " + DataProvider.BOOK_TEXT_SIZE
				+ ", " + DataProvider.PAGE_START_PARA + ", "
				+ DataProvider.PAGE_START_OFFSET + ", "
				+ DataProvider.ANCHOR_LOCATION + ", "
				+ DataProvider.BOOK_FONT_FACE + ", "
				+ DataProvider.BOOK_LINE_SPACE + ", "
				+ DataProvider.BOOK_BLOCK_SPACE + ", "
				+ DataProvider.BOOK_PAGE_EDGE_SPACE + ", "
				+ DataProvider.BOOK_SCREEN_MODE + " FROM "
				+ DataProvider.PAGECONTENT + " WHERE " + DataProvider.EBOOKID
				+ " = ? AND " + DataProvider.DOCUMENT_ID + " = ? AND "
				+ DataProvider.CHAPTER_ITEMREF + " = ?", selectionArgs);
		if (cursor != null && cursor.moveToFirst()) {
			pageContent = new ReadPageContent();
			pageContent.setTextSize(cursor.getInt(cursor.getColumnIndex(DataProvider.BOOK_TEXT_SIZE)));
			pageContent.setPageStartPara(cursor.getString(cursor.getColumnIndex(DataProvider.PAGE_START_PARA)));
			pageContent.setPageStartOffset(cursor.getString(cursor.getColumnIndex(DataProvider.PAGE_START_OFFSET)));
			pageContent.setAnchorLocation(cursor.getString(cursor.getColumnIndex(DataProvider.ANCHOR_LOCATION)));
			pageContent.setFontFace(cursor.getString(cursor.getColumnIndex(DataProvider.BOOK_FONT_FACE)));
			pageContent.setLineSpace(cursor.getInt(cursor.getColumnIndex(DataProvider.BOOK_LINE_SPACE)));
			pageContent.setBlockSpace(cursor.getInt(cursor.getColumnIndex(DataProvider.BOOK_BLOCK_SPACE)));
			pageContent.setPageEdgeSpace(cursor.getInt(cursor.getColumnIndex(DataProvider.BOOK_PAGE_EDGE_SPACE)));
			pageContent.setScreenMode(cursor.getInt(cursor.getColumnIndex(DataProvider.BOOK_SCREEN_MODE)));
		}
		if (cursor != null)
			cursor.close();
		return pageContent;
	}

	/**
	 * 删除数据
	 */
	synchronized public void clearBookPageContent() {
		db.execSQL("delete from bookpage");
		db.execSQL("delete from pagecontent");
	}

	synchronized public void insertOrUpdateSplash(Splash model) {
		ContentValues values = new ContentValues();
		values.put(DataProvider.SPLASH_ID, model.id);
		values.put(DataProvider.SPLASH_PIC, model.url);
		values.put(DataProvider.SPLASH_SAYING, model.adText);
		values.put(DataProvider.SPLASH_PERCENT, model.adTextTopPercent);
		values.put(DataProvider.SPLASH_PERCENT_WIDTH, model.adTextLeftPercent);
		db.insertWithOnConflict(DataProvider.SPLASH, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
		MZLog.d("wangguodong", "保存spalsh描述信息");
	}

	synchronized public Splash findSplashById(long id) {
		String[] selectionArgs = new String[] { String.valueOf(id) };
		Cursor cursor = db.rawQuery("SELECT * FROM " + DataProvider.SPLASH
				+ " WHERE " + DataProvider.SPLASH_ID + " = ? ", selectionArgs);

		Splash model = null;
		if (cursor != null && cursor.moveToFirst()) {
			model = new Splash();
			model.id = cursor.getInt(0);
			model.url = cursor.getString(1);
			model.adText = cursor.getString(2);
			model.adTextTopPercent = cursor.getInt(3);
			model.adTextLeftPercent = cursor.getInt(4);
		}
		if (cursor != null)
			cursor.close();

		return model;
	}

	synchronized public List<Splash> findSplashs() {
		Cursor cursor = db.rawQuery("SELECT * FROM " + DataProvider.SPLASH,null);
		List<Splash> dataList = new ArrayList<Splash>();
		while (cursor != null && cursor.moveToNext()) {
			Splash model = new Splash();
			model.id = cursor.getInt(0);
			model.url = cursor.getString(1);
			model.adText = cursor.getString(2);
			model.adTextTopPercent = cursor.getInt(3);
			model.adTextLeftPercent = cursor.getInt(4);
			dataList.add(model);
		}

		if (cursor != null)
			cursor.close();

		return dataList;
	}
	
	synchronized public void deleteBookShelfRecord(String userId,long eBookId) {
		MZLog.d("wangguodong", "删除书架记录 :ebookid="+eBookId);
		db.execSQL("DELETE FROM " + DataProvider.BOOKSHELF+ " WHERE ebook_id = "+eBookId+" and userid='"+userId+"'");
	}
	
	synchronized public String makePlaceholders(int len) {
	    if (len < 1) {
	        throw new RuntimeException("No placeholders");
	    } else {
	        StringBuilder sb = new StringBuilder(len * 2 - 1);
	        sb.append("?");
	        for (int i = 1; i < len; i++) {
	            sb.append(",?");
	        }
	        return sb.toString();
	    }
	}
	
	synchronized public int getEbookId(long bookid, String username) {
		int ebookid = -1;
		
		String[] selectionArgs = new String[] { username, String.valueOf(bookid) };

		Cursor cursor = db.rawQuery(
				"SELECT _id FROM ebook WHERE user_name = ? AND book_id = ?",
				selectionArgs);
		int ebookId = -1;
		if (cursor != null && cursor.moveToFirst()) {
			ebookId = cursor.getInt(0);
			MZLog.d("wangguodong", "获得书籍的id" + ebookId);
		}
		if (cursor != null)
			cursor.close();
		
		return ebookid;
	}
			

}
