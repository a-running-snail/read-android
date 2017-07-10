package com.jingdong.app.reader.bookmark;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;

import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.reading.BookMark;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;

public class BookMarksSyncService extends IntentService {

	public static final String BOOKMARK_TYPE_EBOOK = "ebook";
	public static final String BOOKMARK_TYPE_DOCUMENT = "document";
	public static final String BOOKMARK_TYPE_SYNC = "sync";
	public static final String BOOKMARK_TYPE = "type";
	public static final String BOOKMARK_BOOKID = "bookid";
	public static final String BOOKMARK_DOCUMENTID = "documentid";

	public static final String BOOKMARK_DEFAULT_COUNT = "20";

	public BookMarksSyncService() {
		super("BookMarksSyncService");
	}

	public JSONObject buildActionNode(BookMark bookMark) throws JSONException {
		JSONObject obj = new JSONObject();
		if (bookMark.server_id == 0) {
			buildCreateBookMark(obj, bookMark);
		} else if (bookMark.operation_state == 3) {
			buildDeleteBookMark(obj, bookMark);
		}
		return obj;
	}

	public void buildCreateBookMark(JSONObject obj, BookMark bookMark)
			throws JSONException {
		obj.put("action", "create");
		obj.put("chapter_title", filterSpecialCharactor(bookMark.chapter_title));
		obj.put("chapter_itemref",filterSpecialCharactor(bookMark.chapter_itemref));
		obj.put("digest",filterSpecialCharactor(bookMark.digest));
		obj.put("offset_in_para", bookMark.offset_in_para);
		obj.put("para_index", bookMark.para_index);
		// TODO 完善书签的新接口逻辑
		// FIXME chapterId
		
		if (bookMark.ebookid == 0) {
			obj.put("document_id", bookMark.docid);
		} else {
			obj.put("ebook_id", bookMark.ebookid);
		}

	}

	public void buildDeleteBookMark(JSONObject obj, BookMark bookMark)
			throws JSONException {
		obj.put("action", "destroy");
		obj.put("id", bookMark.server_id);
	}

	private String filterSpecialCharactor(String note) {
		if (note == null) {
			return "";
		}
		return note.replace(";", "；").replace("%", "％").replace("&", " ");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		String type = intent.getStringExtra(BOOKMARK_TYPE);
		if(BOOKMARK_TYPE_SYNC.equals(type)) syncAllBookMarks();
		else {
			long bookid =intent.getLongExtra(BOOKMARK_BOOKID, 0);
			int documentid =intent.getIntExtra(BOOKMARK_DOCUMENTID, 0);
			if(bookid==0) return;
			
			if (type.equals(BOOKMARK_TYPE_EBOOK)) {
				pullAllBookMarksOfCurrentBook(BOOKMARK_TYPE_EBOOK, bookid);
			} else if (type.equals(BOOKMARK_TYPE_DOCUMENT)) {
				pullAllBookMarksOfCurrentBook(BOOKMARK_TYPE_DOCUMENT, bookid);
			} else {
				return;
			}
		}

	}

	private void syncAllBookMarks() {

		List<BookMark> bookMarks = MZBookDatabase.instance
				.getAllUnsyncBookMarks(LoginUser.getpin());
		if (bookMarks == null || bookMarks.size() == 0)
			return;

		String token = LocalUserSetting.getToken(this);
		String param = "?auth_token=" + token;
		String url = URLText.syncBookMarks + param;

		try {
			JSONArray arr = new JSONArray();
			for (BookMark bookMark : bookMarks) {
				arr.put(buildActionNode(bookMark));
			}
			JSONObject o = new JSONObject();
			o.put("bookmarks", arr);
			String postText = "bookmarks=" + o.toString();
			
			MZLog.d("wangguodong", "posttext=="+postText);
			
			
			
			String result = WebRequest.postWebDataWithContext(this, url,
					postText);
			
			MZLog.d("wangguodong", "syncAllBookMarks result+++"+result);
			
			JSONArray resultArr = new JSONArray(result);
			for (int i = 0; i < resultArr.length(); ++i) {
				BookMark bookMark = bookMarks.get(i);

				JSONObject resultObj = resultArr.getJSONObject(i);
				JSONObject errorObj = resultObj.optJSONObject("error");
				if (errorObj != null) {
					int errorCode = errorObj.optInt("code");
					if (errorCode == 404) {
						bookMark.operation_state = 3;
						bookMark.isSync = 1;
						continue;
					}
				}

				boolean success = resultObj.optBoolean("success");
				if (success) {
					if (bookMark.server_id == 0) {
						int id = resultObj.optInt("id", 0);
						if (id != 0) {
							bookMark.server_id = id;
							bookMark.isSync = 1;
						}
					} else if (bookMark.operation_state == 3) {
						bookMark.isSync = 1;
					} else {
						bookMark.isSync = 1;
					}
				}
			}
			for (BookMark bookMark : bookMarks) {
				MZBookDatabase.instance.insertOrUpdateBookMarks(bookMark);
			}
			MZBookDatabase.instance.cleanBookMarks();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	private void pullAllBookMarksOfCurrentBook(String type, long bookid) {
		// FIXME 改为走京东的流程
//		long lastSyncTime = 0;
//		if (type.equals(BOOKMARK_TYPE_DOCUMENT)) {
//			lastSyncTime = Long.valueOf(MZBookDatabase.instance
//					.getBookMarksSyncTime(LoginUser.getpin(), 0,
//							bookid));
//		} else {
//			lastSyncTime = Long.valueOf(MZBookDatabase.instance
//					.getBookMarksSyncTime(LoginUser.getpin(),
//							bookid, 0));
//		}
//		try {
//			if (!NetWorkUtils.isNetworkConnected(this))
//				return;
//			int total = Integer.valueOf(BOOKMARK_DEFAULT_COUNT);
//			while (total >= 20) {
//				HashMap<String, String> paramsMap = new HashMap<String, String>();
//				paramsMap.put("since_updated_at", lastSyncTime/1000 + "");
//				paramsMap.put("count", BOOKMARK_DEFAULT_COUNT);
//				paramsMap.put("auth_token", LocalUserSetting.getToken(this));
//				String requestUrl = "";
//				if (type.equals(BOOKMARK_TYPE_DOCUMENT)) {
//					requestUrl = URLBuilder.addParameter(
//							URLText.getAllBookMarksOfDocumentUrl.replace(
//									":document_id", bookid + ""), paramsMap);
//				} else {
//					requestUrl = URLBuilder.addParameter(
//							URLText.getAllBookMarksOfEbookUrl.replace(
//									":ebook_id", bookid + ""), paramsMap);
//				}
//				MZLog.d("wangguodong", "requestUrl+++"+requestUrl);
//				
//				String result = WebRequest.getWebDataWithContext(this,
//						requestUrl);
//				
//				MZLog.d("wangguodong", "pullAllBookMarksOfCurrentBook+++"+result);
//				List<BookMark> bookMarks = new ArrayList<BookMark>();
//				JSONObject jsonObject = new JSONObject(result);
//				total = jsonObject.optInt("total");
//				JSONArray array = jsonObject.optJSONArray("bookmarks");
//
//				
//				if (null != array) {
//
//					for (int i = 0; i < array.length(); i++) {
//						try {
//							JSONObject bookmarkObj = array.getJSONObject(i);
//							boolean deleted = bookmarkObj.optBoolean("deleted");
//							if (deleted) {
//								MZBookDatabase.instance.deleteBookMark(
//										LoginUser.getpin(),
//										bookmarkObj.optInt("id"));
//							} else {
//								BookMark bookMark = new BookMark();
//								bookMark.server_id = bookmarkObj.optInt("id");
//								if (type.equals(BOOKMARK_TYPE_DOCUMENT)) {
//									bookMark.docid = bookmarkObj
//											.optInt("document_id");
//								} else {
//									bookMark.ebookid = bookmarkObj
//											.optInt("ebook_id");
//								}
//								bookMark.userid = LoginUser.getpin();
//								bookMark.chapter_title = bookmarkObj
//										.optString("chapter_title");
//								bookMark.chapter_itemref = bookmarkObj
//										.optString("chapter_itemref");
//								bookMark.offset_in_para = bookmarkObj
//										.optInt("offset_in_para");
//								bookMark.para_index = bookmarkObj
//										.optInt("para_index");
//								bookMark.digest = bookmarkObj
//										.optString("digest");
//								bookMark.updated_at = bookmarkObj
//										.optLong("updated_at")*1000;
//								bookMark.deleted = bookmarkObj
//										.optInt("deleted");
//								bookMark.playorder = bookmarkObj
//										.optInt("chapter_playorder");
//
//								bookMark.modified = 1;
//
//								bookMarks.add(bookMark);
//								
//							}
//							lastSyncTime = Math.max(lastSyncTime,bookmarkObj.optLong("updated_at")*1000);
//						} catch (JSONException exception) {
//							exception.printStackTrace();
//						}
//						
//					}
//				}
//				for (BookMark b : bookMarks) {
//					// 更新数据库
//					MZBookDatabase.instance.addSyncBookMark(b);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if (type.equals(BOOKMARK_TYPE_DOCUMENT)) {
//			MZBookDatabase.instance.insertOrUpdateBookMarksSyncTime(LoginUser.getpin(), 0, bookid, lastSyncTime+"");
//
//		} else {
//			MZBookDatabase.instance.insertOrUpdateBookMarksSyncTime(LoginUser.getpin(), bookid, 0, lastSyncTime+"");
//		}

	}

}
