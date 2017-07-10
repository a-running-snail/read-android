package com.jingdong.app.reader.book;

import org.json.JSONObject;


import android.os.Parcel;
import android.os.Parcelable;

public class Document implements Parcelable {
	private final static String BOOK = "book";
	private final static String ID = "id";
	private final static String NAME = "name";
	private final static String SIGN = "sign";
	private final static String USER_ID = "user_id";
	public int documentId;
	public String title;
	public String author;
	public String coverPath;
	public String bookPath;
	public long addAt;
	public String opfMD5;
	// 最后阅读时间
	public long readAt;
	
	public long size;
	public long progress;
	public int state = -1;
	public int bookState = -1;//
	public long mod_time;
	public long access_time;
	public String packageName;
	public String dir;
	public int format = -1;// 书的格式 1pdf类型 2epub类型
	
	
	private Book book;
	private String userId;
	
	public int fromCloudDisk = 0;//0 云盘下载  1= 外部导入
	public String bookSource ;//0 云盘下载  1= 外部导入

	public Document() {

	}

	public final static Document fromJson(JSONObject jsonObject) {
		Document document = new Document();
		if (jsonObject != null) {
			document.opfMD5 = jsonObject.optString(SIGN);
			document.documentId = jsonObject.optInt(ID);
			document.title = jsonObject.optString(NAME);
			document.userId = jsonObject.optString(USER_ID);
			JSONObject bookJson = jsonObject.optJSONObject(BOOK);
			if (bookJson != null) {
				document.book = Book.fromJSON(bookJson);
			}
		}
		return document;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(documentId);
		dest.writeString(title);
		dest.writeString(author);
		dest.writeString(bookPath);
		dest.writeString(coverPath);
		dest.writeLong(addAt);
		dest.writeString(opfMD5);
		// 最后阅读时间
		dest.writeLong(readAt);
		dest.writeString(userId);
		dest.writeInt(format);
		dest.writeInt(fromCloudDisk);
		dest.writeString(bookSource);
		dest.writeByte((byte) (book != null ? 1 : 0));
		if (book != null)
			dest.writeParcelable(book, flags);
	}

	public static final Parcelable.Creator<Document> CREATOR = new Parcelable.Creator<Document>() {
		public Document createFromParcel(Parcel in) {
			return new Document(in);
		}

		public Document[] newArray(int size) {
			return new Document[size];
		}
	};

	private Document(Parcel in) {
		documentId = in.readInt();
		title = in.readString();
		author = in.readString();
		bookPath = in.readString();
		coverPath = in.readString();
		addAt = in.readLong();
		opfMD5 = in.readString();
		readAt = in.readLong();
		userId = in.readString();
		format = in.readInt();
		fromCloudDisk=in.readInt();
		bookSource=in.readString();
		if (in.readByte() == 1)
			book = in.readParcelable(Book.class.getClassLoader());
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

	public String getUserId() {
		return userId;
	}

}
