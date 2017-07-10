package com.jingdong.app.reader.timeline.model.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.book.Document;

public class RenderBody implements Parcelable, BookNoteInterface {
	public final static Creator<RenderBody> CREATOR = new Creator<RenderBody>() {

		@Override
		public RenderBody[] newArray(int size) {
			return new RenderBody[size];
		}

		@Override
		public RenderBody createFromParcel(Parcel source) {
			return new RenderBody(source);
		}
	};
	public final static String RENDER_CONTENT = "content";
	public final static String RENDER_QUOTE = "quote_text";
	private final static String RENDER_ID = "id";
	private final static String RENDER_DELETE = "deleted";
	private final static String RENDER_BOOK = "books";
	private final static String DOCUMENT = "document";
	private final static String RENDER_TITLE = "title";
	private final static String READING_READING_DATA = "reading_data_url";
	private final static String RENDER_ENTITY = "entity";
	private final static String RENDER_RATING = "rating";
	private final static String RENDER_CHAPTER_NAME = "chapter_name";
	private final static String RENDER_PRIVATE = "is_private";
	private final static String RENDER_WRITTEN = "written_at_timestamp";
	private final static String RENDER_GUID = "entity_guid";
	private final static String USER_ID = "user_id";
	//书单相关
	private final static String BOOK_LIST_URL = "book_list_url";
	private final static String BOOK_LIST_NAME = "book_list_name";
	private final static String BOOK_LIST_ID = "book_list_id";
	private final static String BOOK_ACTION_TEXT="action_text";
	private final static String BOOK_LIST_ADD_AT="created_at_timestamp";
	private long id;
	private List<Book> bookList;
	private Document document;
	private boolean deleted;
	private boolean isPrivate;
	private long writtenTime;
	private String userID;
	private double rating;
	private String guid;
	private String title;
	private String readingData;
	private String content;
	private String quote;
	private String chapterName;
	private Entity entity;
	
	//书单相关
	private String bookListUrl;
	private String bookListName;
	private long bookListId;
	private String actionText;
	private String bookListAddAt;
	
	private RenderBody(Parcel source) {
		id = source.readLong();
		if (bookList == null)
			bookList = new ArrayList<Book>();
		source.readTypedList(bookList, Book.CREATOR);
		deleted = (source.readByte() == 0) ? false : true;
		isPrivate = (source.readByte() == 0 ? false : true);
		writtenTime = source.readLong();
		userID = source.readString();
		rating = source.readDouble();
		guid = source.readString();
		title = source.readString();
		readingData = source.readString();
		content = source.readString();
		quote = source.readString();
		chapterName = source.readString();
		entity = source.readParcelable(Entity.class.getClassLoader());
		document = source.readParcelable(Document.class.getClassLoader());
		
		//书单相关
		bookListUrl=source.readString();
		bookListName=source.readString();
		bookListId=source.readLong();
		actionText=source.readString();
		bookListAddAt=source.readString();
	}

	public RenderBody() {

	}

	/***
	 * 从JSONObject中取得数据，并将结果保存到RenderBody中。
	 * 在解析RenderBody中包含的Entity时，本方法会调用getEntityFromJson()来进行递归解析，这种递归在目前只有1层。
	 *
	 * @param jObject
	 *            数据源
	 * @param topEntity
	 *            true表示该entity为顶层实体，false表示该entit为第二层实体。
	 * @throws JSONException
	 *             解析JSON时发生异常
	 */
	public void parseJson(JSONObject jObject, boolean topEntity) throws JSONException {
		if (jObject != null) {
			JSONObject entityJson;
			setId(jObject.optInt(RENDER_ID));
			setDeleted(jObject.optInt(RENDER_DELETE) == 0 ? false : true);
			setContent(jObject.optString(RENDER_CONTENT));
			setQuote(jObject.optString(RENDER_QUOTE));
			setTitle(jObject.optString(RENDER_TITLE));
			setReadingData(jObject.optString(READING_READING_DATA));
			setBookList(getBooksFromJson(jObject.optJSONArray(RENDER_BOOK)));
			setRating(jObject.optDouble(RENDER_RATING));
			setChapterName(jObject.optString(RENDER_CHAPTER_NAME));
			setPrivate(jObject.optInt(RENDER_PRIVATE) == 0 ? false : true);
			setWrittenTime(jObject.optLong(RENDER_WRITTEN));
			setGuid(jObject.optString(RENDER_GUID));
			setUserID(jObject.optString(USER_ID));
			//书单相关
			setBookListId(jObject.optLong(BOOK_LIST_ID));
			setBookListName(jObject.optString(BOOK_LIST_NAME));
			setBookListUrl(jObject.optString(BOOK_LIST_URL));
			setActionText(jObject.optString(BOOK_ACTION_TEXT));
			setBookListAddAt(jObject.optString(BOOK_LIST_ADD_AT));
			
			setDocument(Document.fromJson(jObject.optJSONObject(DOCUMENT)));
			if (topEntity && (entityJson = jObject.optJSONObject(RENDER_ENTITY)) != null) {
				entity = new Entity();
				entity.parseJson(entityJson, false);
			}
		}
	}

	public boolean hasEntity() {
		if (entity != null)
			return true;
		else
			return false;
	}

	public long getId() {
		return id;
	}

	public String getBookListAddAt() {
		return bookListAddAt;
	}

	public void setBookListAddAt(String bookListAddAt) {
		this.bookListAddAt = bookListAddAt;
	}

	public Book getBookAt(int position) {
		return bookList.get(position);
	}

	public String getBookListString() {
		StringBuffer buffer = new StringBuffer();
		for (Book book : bookList) {
			buffer.append(book.getTitle());
			buffer.append(' ');
		}
		return buffer.toString();
	}

	public List<Book> getBookList() {
		if (bookList != null)
			return Collections.unmodifiableList(bookList);
		else
			return null;
	}

	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public String getReadingData() {
		return readingData;
	}

	@Override
	public String getQuote() {
		return quote;
	}

	@Override
	public String getContent() {
		return content;
	}

	public Entity getEntity() {
		return entity;
	}

	public double getRating() {
		return rating;
	}
	
	public void setRating(float rating){
		this.rating = rating;
	}

	@Override
	public boolean isPrivate() {
		return isPrivate;
	}

	@Override
	public String getChapterName() {
		return chapterName;
	}

	@Override
	public long getWrittenTime() {
		return writtenTime;
	}

	public String getGuid() {
		return guid;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getUserID() {
		return userID;
	}

	void setUserID(String userID) {
		this.userID = userID;
	}

	void setChapterName(String chapterName) {
		this.chapterName = chapterName;
	}

	void setBookList(List<Book> bookList) {
		this.bookList = bookList;
	}

	void setWrittenTime(long writtenTime) {
		this.writtenTime = writtenTime;
	}

	void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	void setTitle(String title) {
		this.title = title;
	}

	void setReadingData(String readingData) {
		this.readingData = readingData;
	}
	
	@Override
	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public void setQuote(String quote) {
		this.quote = quote;
	}

	void setRating(double rating) {
		this.rating = rating;
	}

	/***
	 * 从JSONArray中取得书籍列表数据，并将结果保存到List中
	 *
	 * @param jsonArray
	 *            数据源
	 * @return 解析的结果，一个保存有Book的List，若返回值为空，表示书籍列表为空
	 * @throws JSONException
	 *             解析JSON时发生异常
	 */
	private List<Book> getBooksFromJson(JSONArray jsonArray) throws JSONException {
		List<Book> books = null;
		if (jsonArray != null) {
			books = new ArrayList<Book>(jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				Book book = new Book();
				book.parseJson((JSONObject) jsonArray.get(i));
				books.add(book);
			}
		}
		return books;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof RenderBody && ((RenderBody) o).getId() == id;
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public String toString() {
		StringBuilder builder=new StringBuilder();
		if(!TextUtils.isEmpty(content))
			builder.append(content);
		if(!TextUtils.isEmpty(quote)){
			builder.append('\n');
			builder.append(quote);
		}
		return builder.toString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeTypedList(bookList);
		dest.writeByte((byte) (deleted ? 1 : 0));
		dest.writeByte((byte) (isPrivate ? 1 : 0));
		dest.writeLong(writtenTime);
		dest.writeString(userID);
		dest.writeDouble(rating);
		dest.writeString(guid);
		dest.writeString(title);
		dest.writeString(readingData);
		dest.writeString(content);
		dest.writeString(quote);
		dest.writeString(chapterName);
		dest.writeParcelable(entity, flags);
		dest.writeParcelable(document, flags);
		//书单相关
		dest.writeString(bookListUrl);
		dest.writeString(bookListName);
		dest.writeLong(bookListId);
		dest.writeString(actionText);
		dest.writeString(bookListAddAt);
	}

	public String getBookListUrl() {
		return bookListUrl;
	}

	public void setBookListUrl(String bookListUrl) {
		this.bookListUrl = bookListUrl;
	}

	public String getBookListName() {
		return bookListName;
	}

	public void setBookListName(String bookListName) {
		this.bookListName = bookListName;
	}

	public long getBookListId() {
		return bookListId;
	}

	public void setBookListId(long bookListId) {
		this.bookListId = bookListId;
	}

	public String getActionText() {
		return actionText;
	}

	public void setActionText(String actionText) {
		this.actionText = actionText;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

}
