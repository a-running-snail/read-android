package com.jingdong.app.reader.me.model;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;

public class ReadingDataModel extends ObservableModel {
	public static enum Event {
		INIT_LOAD;
	}

	private final static String USER = "user";
	private final static String BOOK = "book";
	private final static String DOCUMENT = "document";
	private final static String READING_SECONDS = "total_time_in_seconds";
	private final static String NOTES_COUNTS = "notes_count";
	private final static String RATING = "rating";
	private long readingSeconds;
	private int noteCounts;
	private double rating;
	private Book bookDetail = new Book(false);
	private Document document;
	private UserInfo userInfo = new UserInfo();
	private Context context;

	public ReadingDataModel(Context context) {
		this.context = context;
	}

	String jsonString = null;
	public void loadReadingData(final String user_id,final String book_id,final String document_id) {
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				WebRequestHelper.get(URLText.Reading_data,
						RequestParamsPool.getReading_data(user_id, book_id, document_id), true,
						new MyAsyncHttpResponseHandler(context, true) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								// TODO Auto-generated method stub
							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {
								// TODO Auto-generated method stub

								jsonString = new String(responseBody);
								boolean connected = false;
								try {
									connected = parseJson(jsonString);
								} catch (JSONException e) {
									MZLog.e("ReadingData", Log.getStackTraceString(e));
									e.printStackTrace();
								}
								notifyDataChanged(Event.INIT_LOAD.ordinal(), connected);
							}
						});
			}
		});
	}

	public long getReadingSeconds() {
		return readingSeconds;
	}

	public int getNoteCounts() {
		return noteCounts;
	}

	public double getRating() {
		return rating;
	}

	public Book getBook() {
		if (document != null && document.getDocumentId() > 0) {
			return document.getBook();
		}else {
			return bookDetail;
		}
	}

	public String getBookCover() {
		return bookDetail.getCover();
	}

	public String getBookName() {
		return bookDetail.getTitle();
	}

	public String getBookAuthor() {
		return bookDetail.getAuthorName();
	}

	public long getBookId() {
		return bookDetail.getBookId();
	}

	public long getDocumentId() {
		if (document == null)
			return -1;
		else
			return document.getDocumentId();
	}

	public String getUserId() {
		return userInfo.getId();
	}

	public UserInfo getUser() {
		return userInfo;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	private boolean parseJson(String jsonString) throws JSONException {
		JSONObject jsonObject = new JSONObject(jsonString);
		boolean connected = UiStaticMethod.isNetWorkConnected(jsonString);
		if (connected) {
			readingSeconds = jsonObject.optLong(READING_SECONDS);
			noteCounts = jsonObject.getInt(NOTES_COUNTS);
			rating = jsonObject.optDouble(RATING);
			userInfo.parseJson(jsonObject.optJSONObject(USER));
			bookDetail.parseJson(jsonObject.optJSONObject(BOOK));
			document = Document.fromJson(jsonObject.optJSONObject(DOCUMENT));
		}
		return connected;
	}
}
