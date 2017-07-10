package com.jingdong.app.reader.book;

import java.io.Serializable;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class SerializabelDocument implements Serializable {
	private final static String BOOK = "book";
	private final static String ID = "id";
	private final static String NAME = "name";
	private final static String SIGN = "sign";
	private final static String USER_ID = "user_id";
	public int documentId;
	public String title;
	public String author;
	public String coverPath;
	public long addAt;
	public String opfMD5;
	// 最后阅读时间
	public long readAt;
	// 没有被持久化到数据库中，也无必要
	private Book book;
	// 没有被持久化到数据库中，也无必要
	private long userId;


	public final static SerializabelDocument fromJson(JSONObject jsonObject) {
		SerializabelDocument document = new SerializabelDocument();
		if (jsonObject != null) {
			document.opfMD5 = jsonObject.optString(SIGN);
			document.documentId = jsonObject.optInt(ID);
			document.title = jsonObject.optString(NAME);
			document.userId = jsonObject.optLong(USER_ID);
			JSONObject bookJson = jsonObject.optJSONObject(BOOK);
			if (bookJson != null) {
				document.book = Book.fromJSON(bookJson);
			}
		}
		return document;
	}


	public int getDocumentId() {
		return documentId;
	}

	public String getTitle() {
		return title;
	}

	public String getOpfMD5() {
		return opfMD5;
	}

	public Book getBook() {
		return book;
	}

	public long getUserId() {
		return userId;
	}

}
