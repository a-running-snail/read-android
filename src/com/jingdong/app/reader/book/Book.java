package com.jingdong.app.reader.book;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.util.UiStaticMethod;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Book implements Parcelable {
	public final static Creator<Book> CREATOR = new Creator<Book>() {

		@Override
		public Book createFromParcel(Parcel source) {
			return new Book(source);
		}

		@Override
		public Book[] newArray(int size) {
			return new Book[size];
		}
	};

	private final static String COMPOSITE = "is_composite";
	private final static String COMPOSITE_TAG = "composite_tag_id";
	private final static String WEB_ONLY_TEXT = "web_only_text";
	// 为了联系起Document和Book，添加了IS_DOCUEMTN属性。这个属性不存在于json中，只是为了在图书列表中方便判断图书类型。
	public final static String IS_DOCUMENT = "isMockDocument";
	public final static String EBOOK = "ebook";
	public final static String WEB_PRICE = "web_price";
	public final static String BORROW = "borrow";
	public final static String DOCUMENT_ID = "documentId";
	public final static String USER_RATING = "user_rating";
	public final static String HIDDEN = "hidden";
	public final static double NO_EXIST = -1.0;
	private final boolean quote;
	private boolean document;
	private boolean ebook;
	private boolean composite;
	private boolean borrowable;
	private boolean hidden;
	private long compositeTag;
	private long docuemntId;
	private double webPrice;
	private String webOnlyText;
	public int bookId;
	public int ebookId;
	public String title;
	public String authorName;
	public String cover;
	public String publisher;
	public String summary;
	private double userRating;
	public double rating;
	public double price;
	public long purchaseTime;
	public String authorSummary;
	public String name;
	public String sign;
	
	public int noteCount;
	public List<BookEntity> entityList = new ArrayList<BookEntity>();

	public Book() {
		this(true);
	}

	/**
	 * 
	 * 
	 * @param quote
	 *            是否为书名自动加引号，true表示为书名自动加引号,false表示不为书名自动加引号
	 */
	public Book(boolean quote) {
		this.quote = quote;
	}

	private Book(Parcel parcel) {
		quote = (parcel.readByte() == 0) ? false : true;
		docuemntId = parcel.readLong();
		bookId = parcel.readInt();
		title = parcel.readString();
		name = parcel.readString();
		authorName = parcel.readString();
		cover = parcel.readString();
		userRating = parcel.readDouble();
		rating = parcel.readDouble();
		publisher = parcel.readString();
		summary = parcel.readString();
		authorSummary = parcel.readString();
		noteCount=parcel.readInt();
		hidden = (parcel.readByte() == 0) ? false : true;
		document = (parcel.readByte() == 0) ? false : true;
		ebook = (parcel.readByte() == 0) ? false : true;
		borrowable = (parcel.readByte() == 0) ? false : true;
		webPrice = parcel.readDouble();
		ebookId = parcel.readInt();
		price = parcel.readDouble();
		composite = (parcel.readByte() == 0) ? false : true;
		compositeTag = parcel.readLong();
		webOnlyText = parcel.readString();
		sign = parcel.readString();
		parcel.readTypedList(entityList, BookEntity.CREATOR);
	}

	public int getEntityIdWithEdition(int edition) {
		for (BookEntity entity : entityList) {
			if (entity.edition == edition) {
				return entity.entityId;
			}
		}
		return 0;
	}

	public static Book fromJSON(JSONObject json) {
		Book book = new Book();
		parseJson(json, book);
		return book;
	}

	public void setTitle(String title) {
		this.title = title;
//		if (quote) {
//			this.title = getQuotedTitle();
//		}
	}

	public void setName(String name) {
		this.name = name;
		if (quote) {
			this.name = getQuotedName();
		}
	}

	public String getQuotedTitle() {
		
		if(TextUtils.isEmpty(title))
			return "";
		
		return UiStaticMethod.getQuotedTitle(title);
		
	}
	
	public String getQuotedName() {
		return UiStaticMethod.getQuotedTitle(name);
		
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
	
	public long getBookId() {
		return bookId;
	}

	public long getDocumentId() {
		return docuemntId;
	}

	public int getNoteCount() {
		return noteCount;
	}

	public void setNoteCount(int noteCount) {
		this.noteCount = noteCount;
	}

	public String getTitle() {
		return title;
	}

	public String getName(){
		return name;
	}
	
	public String getAuthorName() {
		return authorName;
	}

	public String getCover() {
		return cover;
	}

	public String getWebOnlyText() {
		return webOnlyText;
	}

	public double getRating() {
		return rating;
	}

	public double getUserRating() {
		return userRating;
	}
	
	public String getPublisher() {
		return publisher;
	}
	
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public double getWebPrice() {
		return webPrice;
	}

	public boolean isHidden() {
		return hidden;
	}

	public boolean isEbook() {
		return ebook;
	}

	public boolean isDocument() {
		return document;
	}

	public boolean isBorrowable() {
		return borrowable;
	}

	public long getCompositeTag() {
		return compositeTag;
	}

	public boolean isComposite() {
		return composite;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getShareURL() {
		StringBuilder builder = new StringBuilder();
		builder.append(URLText.shareBookUrl);
		builder.append(bookId);
		return builder.toString();
	}

	/***
	 * 从JSONObject中取得书籍数据，并将结果保存到Book中
	 * 
	 * @param jObject
	 *            数据源
	 * @return 解析的结果，一个Book对象
	 * @throws JSONException
	 *             解析JSON时发生异常
	 */
	public void parseJson(JSONObject jObject) throws JSONException {
		if (jObject != null) {
			parseJson(jObject, this);
			setTitle(jObject.optString("title"));
			setPublisher(jObject.optString("publisher"));
		}
	}

	@Override
	public int hashCode() {
		return bookId;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Book && ((Book) o).getBookId() == bookId;
	}

	@Override
	public String toString() {
		return title;
	}
	

	private static void parseJson(JSONObject json, Book book) {
		book.bookId = json.optInt("id");
		book.docuemntId = json.optLong(DOCUMENT_ID);
		book.title = json.optString("title");
		book.name = json.optString("name");
		book.authorName = json.optString("author");
		book.cover = json.optString("imgUrl");
		book.userRating = json.optDouble(USER_RATING);
		book.rating = json.optDouble("rating");
		book.publisher = json.optString("publisher_name");
		book.summary = json.optString("summary");
		book.authorSummary = json.optString("author_summary");
		book.noteCount=json.optInt("note_count");
		book.hidden = json.optBoolean(HIDDEN);
		book.document = json.optBoolean(IS_DOCUMENT);
		book.ebook = json.optBoolean("isEBook");
		book.webPrice = json.optDouble("price");
		book.borrowable = json.optBoolean("isBorrow");
		book.sign = json.optString("sign");
		JSONObject ebook = json.optJSONObject("ebook");
		if (ebook != null) {
			book.ebookId = ebook.optInt("id");
			book.price = ebook.optDouble("price", 0.0);
			book.composite = ebook.optBoolean(COMPOSITE);
			book.compositeTag = ebook.optLong(COMPOSITE_TAG);
			book.webOnlyText = ebook.optString("info");
			if (ebook != null) {
				JSONArray entityArray = ebook.optJSONArray("entities");
				if (entityArray != null) {
					for (int i = 0; i < entityArray.length(); ++i) {
						JSONObject o = entityArray.optJSONObject(i);
						if (o != null) {
							book.entityList.add(BookEntity.fromJSON(o));
						}
					}
				}
			}
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (quote ? 1 : 0));
		dest.writeLong(docuemntId);
		dest.writeInt(bookId);
		dest.writeString(title);
		dest.writeString(name);
		dest.writeString(authorName);
		dest.writeString(cover);
		dest.writeDouble(userRating);
		dest.writeDouble(rating);
		dest.writeString(publisher);
		dest.writeString(summary);
		dest.writeString(authorSummary);
		dest.writeInt(noteCount);
		dest.writeByte((byte) (hidden ? 1 : 0));
		dest.writeByte((byte) (document ? 1 : 0));
		dest.writeByte((byte) (ebook ? 1 : 0));
		dest.writeByte((byte) (borrowable ? 1 : 0));
		dest.writeDouble(webPrice);
		dest.writeInt(ebookId);
		dest.writeDouble(price);
		dest.writeByte((byte) (composite ? 1 : 0));
		dest.writeLong(compositeTag);
		dest.writeString(webOnlyText);
		dest.writeTypedList(entityList);
		dest.writeString(sign);
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

}
