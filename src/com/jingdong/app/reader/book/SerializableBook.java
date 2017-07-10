package com.jingdong.app.reader.book;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.util.UiStaticMethod;



public class SerializableBook implements Serializable {


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
	
	public boolean document;
	public boolean ebook;
	public boolean composite;
	public boolean borrowable;
	public boolean hidden;
	public long compositeTag;
	public long docuemntId;
	public double webPrice;
	public String webOnlyText;
	public int bookId;
	public int ebookId;
	public String title;
	public String authorName;
	public String cover;
	public String publisher;
	public String summary;
	public double userRating;
	public double rating;
	public double price;
	public long purchaseTime;
	public String authorSummary;
	public SerializabelDocument SimpleDocument;
	public int noteCount;
	public List<SerializableBookEntity> entityList = new ArrayList<SerializableBookEntity>();

	
	

	public int getEntityIdWithEdition(int edition) {
		for (SerializableBookEntity entity : entityList) {
			if (entity.edition == edition) {
				return entity.entityId;
			}
		}
		return 0;
	}

	public static SerializableBook fromJSON(JSONObject json) {
		SerializableBook book = new SerializableBook();
		parseJson(json, book);
		return book;
	}

	public static SerializableBook fromJSONNew(JSONObject json) {
		SerializableBook book = new SerializableBook();
		parseJsonNew(json, book);
		return book;
	}
	
	public void setTitle(String title) {
		this.title = title;

	}

	public SerializabelDocument getSimpleDocument() {
		return SimpleDocument;
	}

	public void setSimpleDocument(SerializabelDocument simpleDocument) {
		SimpleDocument = simpleDocument;
	}

	public String getQuotedTitle() {
		return UiStaticMethod.getQuotedTitle(title);
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
			setTitle(jObject.optString("name"));
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

	private static  void parseJson(JSONObject json, SerializableBook book) {
		book.bookId = json.optInt("id");
		book.docuemntId = json.optLong(DOCUMENT_ID);
		book.title = json.optString("name");
		book.authorName = json.optString("author_name");
		book.cover = json.optString("cover");
		book.userRating = json.optDouble(USER_RATING);
		book.rating = json.optDouble("rating");
		book.publisher = json.optString("publisher_name");
		book.summary = json.optString("summary");
		book.authorSummary = json.optString("author_summary");
		book.noteCount=json.optInt("note_count");
		book.hidden = json.optBoolean(HIDDEN);
		book.document = json.optBoolean(IS_DOCUMENT);
		book.ebook = json.optBoolean(EBOOK);
		book.webPrice = json.optDouble(WEB_PRICE);
		book.borrowable = json.optBoolean(BORROW);
		JSONObject ebook = json.optJSONObject("ebook");
		if (ebook != null) {
			book.ebookId = ebook.optInt("id");
			book.price = ebook.optDouble("web_price", 0.0);
			book.composite = ebook.optBoolean(COMPOSITE);
			book.compositeTag = ebook.optLong(COMPOSITE_TAG);
			book.webOnlyText = ebook.optString(WEB_ONLY_TEXT);
			if (ebook != null) {
				JSONArray entityArray = ebook.optJSONArray("entities");
				if (entityArray != null) {
					for (int i = 0; i < entityArray.length(); ++i) {
						JSONObject o = entityArray.optJSONObject(i);
						if (o != null) {
							book.entityList.add(SerializableBookEntity.fromJSON(o));
						}
					}
				}
			}
		}
		JSONObject document = json.optJSONObject("document");
		book.SimpleDocument=SerializabelDocument.fromJson(document);
		
	}

	
	private static  void parseJsonNew(JSONObject json, SerializableBook book) {
		book.bookId = json.optInt("bookId");
		book.docuemntId = json.optLong(DOCUMENT_ID);
		book.title = json.optString("title");
		book.authorName = json.optString("author");
		book.cover = json.optString("imgUrl");
		book.userRating = json.optDouble(USER_RATING);
		book.rating = json.optDouble("star");
		book.publisher = json.optString("publisher_name");
		book.summary = json.optString("info");
		book.authorSummary = json.optString("author_summary");
		book.noteCount=json.optInt("note_count");
		book.hidden = json.optBoolean(HIDDEN);
		book.document = json.optBoolean(IS_DOCUMENT);
		book.ebook = json.optBoolean(EBOOK);
		book.webPrice = json.optDouble("price");
		book.borrowable = json.optBoolean(BORROW);
		JSONObject ebook = json.optJSONObject("ebook");
		if (ebook != null) {
			book.ebookId = ebook.optInt("id");
			book.price = ebook.optDouble("web_price", 0.0);
			book.composite = ebook.optBoolean(COMPOSITE);
			book.compositeTag = ebook.optLong(COMPOSITE_TAG);
			book.webOnlyText = ebook.optString(WEB_ONLY_TEXT);
			if (ebook != null) {
				JSONArray entityArray = ebook.optJSONArray("entities");
				if (entityArray != null) {
					for (int i = 0; i < entityArray.length(); ++i) {
						JSONObject o = entityArray.optJSONObject(i);
						if (o != null) {
							book.entityList.add(SerializableBookEntity.fromJSON(o));
						}
					}
				}
			}
		}
		JSONObject document = json.optJSONObject("document");
		book.SimpleDocument=SerializabelDocument.fromJson(document);
		
	}
	
}
