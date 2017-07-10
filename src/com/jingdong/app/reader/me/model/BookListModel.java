package com.jingdong.app.reader.me.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.me.fragment.BookListFragment;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.net.url.PagedBasedUrlGetter;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;

public class BookListModel extends ObservableModel {
	public final static int READ_BOOKS = 100;
	public final static int WISH_BOOKS = 200;
	public final static int BOUGHT_BOOKS = 300;
	public final static int IMPORT_BOOKS = 400;
	public final static int SEARCH_BOOKS = 500;
	public final static int SEARCH_EBOOKS = 600;
	public final static int NOTES_BOOKS = 700;
	private final static String JSON_WIHS_BOOKS = "wish_books";
	private final static String JSON_READ_BOOKS = "books";
	private final static String JSON_BOUGHT_BOOKS = "books";
	private final static String JSON_IMPORT_BOOKS = "documents";
	private final static String JSON_NOTES_BOOKS = "book_notes";

	private final static String PAGE = "page";
	private final static String BOOK_ITEM = "book";
	private final static String TYPE = "type";
	private final static String EBOOK = "ebook";
	private final static String USERS = "/users/";
	private final static String READ_JSON = "/books/read.json";
	private final static String WISH_JSON = "/books/wish.json";
	private final static String DOCUMENT_ID = "id";
	private final static String WITH_USER = "with_user";
	private final static String RATING = "star";
	private final int bookListType;
	private final Context context;
	private List<Book> books;
	private List<Book> tempBooks;
	private int page = PagedBasedUrlGetter.FIRST_PAGE;

	/**
	 * 初始化BookListModel
	 * 
	 * @param context
	 *            数据上下文
	 * @param type
	 *            待加载的数据类型
	 */
	public BookListModel(Context context, int type) {
		books = Collections.synchronizedList(new LinkedList<Book>());
		bookListType = type;
		this.context = context;
	}

	/**
	 * 加载图书列表
	 * 
	 * @param userId
	 *            用户Id
	 * @param type
	 *            加载方式，目前有LOAD_INIT和LOAD_MORE两种
	 */
	String jsonString = null;

	public void loadBooks(final String url,final String userId, final int type,
			final String currentpage, final String pagecount) {
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				WebRequestHelper.get(url,
						RequestParamsPool.getBook_NoteParams(currentpage,
								pagecount, "", userId), true,
						new MyAsyncHttpResponseHandler(context, true) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								// TODO Auto-generated method stub
								Log.d("cj", "false=======>>");
							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {
								// TODO Auto-generated method stub

								jsonString = new String(responseBody);
								Log.d("cj", "result=======>>" + jsonString);
								parseJSON(type, jsonString);
							}
						});
			}
		});

	}

	/**
	 * 初始化搜索图书，可能是搜索ebook，也可能是搜索book
	 * 
	 * @param type
	 *            搜索类型
	 * @param query
	 *            待搜索的字符串
	 */
	public void searchBook(int type, String query) {
		String url = getUrlFromType(query, true);
		String jsonString = WebRequest.requestWebData(url, "",
				WebRequest.httpGet);
		parseJSON(type, jsonString);

	}

	/**
	 * 搜索图书列表的第二页，可能是搜索ebook，也可能是搜索book
	 * 
	 * @param type
	 *            搜索类型
	 * @param query
	 *            待搜索的字符串
	 */
	public void searchMoreBook(int type, String query) {
		String url = getUrlFromType(query, false);
		String jsonString = WebRequest.requestWebData(url, "",
				WebRequest.httpGet);
		parseJSON(type, jsonString);
	}

	/**
	 * 取得指定位置的图书
	 * 
	 * @param postion
	 *            指定的位置
	 * @return 指定的图书对象
	 */
	public Book getBookAt(int postion) {
		return books.get(postion);
	}

	/**
	 * 查询当前图书列表的长度
	 * 
	 * @return 图书列表的大小
	 */
	public int getLength() {
		return books.size();
	}

	/**
	 * 查询当前的图书列表是否为空
	 * 
	 * @return 如果图书列表的长度为0，返会true；否则返回false。
	 */
	public boolean isEmpty() {
		return books.isEmpty();
	}

	/**
	 * 清除图书列表
	 */
	public void clear() {
		books.clear();
	}

	/**
	 * 向图书列表中添加新的数据。
	 */
	public void refreshData() {
		if (tempBooks != null) {
			books.addAll(tempBooks);
			tempBooks = null;
		}
	}

	public void changeBookVisibility(boolean hide, Book book) {
		String url = getHideUrl(hide, book.getDocumentId());
		String result = WebRequest.postWebDataWithContext(context, url, "");
		boolean connected = UiStaticMethod.isNetWorkConnected(result);
		boolean succsess = parsePostResult(result);
		notifyDataChanged(BookListFragment.CHANGE_BOOK_VISIBILITY, connected,
				succsess, book);
	}

	private String getHideUrl(boolean hide, long id) {
		String baseUrl, url;
		if (hide)
			baseUrl = String.format(Locale.US, URLText.hideDocument, id);
		else
			baseUrl = String.format(Locale.US, URLText.unhideDocument, id);
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		url = URLBuilder.addParameter(baseUrl, paramMap);
		return url;
	}

	/**
	 * @param userId
	 * @return
	 */
	private String getJsonString(String userId) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		paramMap.put(PAGE, Integer.toString(page++));
		if (bookListType == BOUGHT_BOOKS) {
			paramMap.put(TYPE, EBOOK);
		}
		String url = URLBuilder.addParameter(getUrlFromType(userId), paramMap);
		String jsonString = WebRequest.getWebDataWithContext(context, url);
		return jsonString;
	}

	/**
	 * @param type
	 * @param query
	 * @param firstPage
	 * @return
	 */
	private String getUrlFromType(String query, boolean firstPage) {
		String baseUrl = null;
		boolean inParam = true;
		switch (bookListType) {
		case SEARCH_BOOKS:
			inParam = true;
			baseUrl = URLText.searchBookUrl;
			break;
		case SEARCH_EBOOKS:
			inParam = false;
			baseUrl = URLText.searchEbookUrl;
			break;
		}
		if (firstPage)
			page = 1;
		else
			page++;
		return PagedBasedUrlGetter.getQueryUrl(baseUrl, query, page, inParam);
	}

	/**
	 * 根据jsonString解析Json数据，并把解析结果填充到数组中，之后通知观察者。
	 * 
	 * @param type
	 *            请求类型
	 * @param jsonString
	 *            待解析的字符串
	 */
	private void parseJSON(int type, String jsonString) {
		boolean connected, hasContent = false;
		if (connected = UiStaticMethod.isNetWorkConnected(jsonString)) {
			try {
				JSONArray jsonArray = getJsonArrayFromString(jsonString);
				if (type == BookListFragment.LOAD_INIT
						|| type == BookListFragment.SEARCH)
					books.clear();
				if (jsonArray.length() == 0) {
					hasContent = false;
					page--;
				} else {
					hasContent = true;
					addToBookList(jsonArray);
				}
			} catch (JSONException e) {
				MZLog.e("BookList", Log.getStackTraceString(e));
				page--;
				hasContent = false;
			}
		}
		notifyDataChanged(type, connected, hasContent);
		LocalBroadcastManager.getInstance(context).sendBroadcast(
				new Intent("com.mzread.action.dialog.canceled"));
	}

	/**
	 * 从jsonArray中取出数据，并添加到临时数组中
	 * 
	 * @param jsonArray
	 *            待处理的jsonArray
	 * @throws JSONException
	 *             json解析一场
	 */
	private void addToBookList(JSONArray jsonArray) {

		tempBooks = new ArrayList<Book>(jsonArray.length());
		Book book;
		for (int i = 0; i < jsonArray.length(); i++) {
			book = new Book(false);
			try {
				JSONObject object = getJsonObjectFromJsonArray(jsonArray, i);
				book.parseJson(object);
				tempBooks.add(book);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据图书列表的类型返回URL地址
	 * 
	 * @param userId
	 *            用户id
	 * @return 所请求的图书列表的URL地址
	 */
	private String getUrlFromType(String userId) {
		switch (bookListType) {
		case READ_BOOKS:
			return URLText.baseUrl + USERS + String.valueOf(userId) + READ_JSON;
		case WISH_BOOKS:
			return URLText.baseUrl + USERS + String.valueOf(userId) + WISH_JSON;
		case BOUGHT_BOOKS:
			return URLText.boughtBookListUrl + userId + ".json";
		case IMPORT_BOOKS:
			return URLText.importBookListUrl + userId + ".json";
		case NOTES_BOOKS:

		{
			String url = URLText.getAllNotesNewUrl.replace(":id", userId);
			return url;
		}

		}
		return null;
	}

	/**
	 * 从Json字符串中取出JSON数组
	 * 
	 * @param jsonString
	 *            包含有数组的JSON字符串
	 * @return 图书列表
	 * @throws JSONException
	 *             在解析JSON字符串的时候发生异常
	 */
	private JSONArray getJsonArrayFromString(String jsonString)
			throws JSONException {
		JSONObject jsonObject;
		switch (bookListType) {
		case READ_BOOKS:
			jsonObject = new JSONObject(jsonString);
			return jsonObject.getJSONArray(JSON_READ_BOOKS);
		case WISH_BOOKS:
			jsonObject = new JSONObject(jsonString);
			return jsonObject.getJSONArray(JSON_WIHS_BOOKS);
		case BOUGHT_BOOKS:
			jsonObject = new JSONObject(jsonString);
			return jsonObject.getJSONArray(JSON_BOUGHT_BOOKS);
		case IMPORT_BOOKS:
			jsonObject = new JSONObject(jsonString);
			return jsonObject.getJSONArray(JSON_IMPORT_BOOKS);
		case NOTES_BOOKS:
			jsonObject = new JSONObject(jsonString);
			return jsonObject.getJSONArray(JSON_NOTES_BOOKS);
		case SEARCH_BOOKS:
		case SEARCH_EBOOKS:
			return new JSONArray(jsonString);
		}
		return null;
	}

	/**
	 * 从jsonArray中获得指定的JsonObject
	 * 
	 * @param array
	 *            jsonArray
	 * @param index索引
	 * @return 指定的JsonObject
	 * @throws JSONException
	 *             解析异常
	 */
	private JSONObject getJsonObjectFromJsonArray(JSONArray array, int index)
			throws JSONException {
		JSONObject item, jsonObject;
		double rating;
		switch (bookListType) {
		case READ_BOOKS:
			jsonObject = array.getJSONObject(index);
			rating = jsonObject.optDouble(RATING);
//			item = jsonObject.getJSONObject(BOOK_ITEM);
			if (!Double.isNaN(rating))
				jsonObject.put(Book.USER_RATING, rating);
			else
				jsonObject.put(Book.USER_RATING, Book.NO_EXIST);
			return jsonObject;
		case WISH_BOOKS:
			return array.getJSONObject(index).getJSONObject(BOOK_ITEM);
		case BOUGHT_BOOKS:
			item = array.getJSONObject(index);
			jsonObject = item.optJSONObject(WITH_USER);
			if (jsonObject != null) {
				rating = jsonObject.optDouble(RATING);
				if (!Double.isNaN(rating))
					item.put(Book.USER_RATING, rating);
				else
					item.put(Book.USER_RATING, Book.NO_EXIST);
			}
			return item;
		case IMPORT_BOOKS:
			item = array.getJSONObject(index);
			if ((jsonObject = item.optJSONObject(BOOK_ITEM)) != null)
				;
			else {
				jsonObject = item;
				jsonObject.put(Book.IS_DOCUMENT, true);
			}
			jsonObject.put(Book.DOCUMENT_ID, item.optLong(DOCUMENT_ID));
			jsonObject.put(Book.BORROW, item.optBoolean(Book.BORROW));
			jsonObject.put(Book.HIDDEN, item.optString(Book.HIDDEN));
			return jsonObject;
		case NOTES_BOOKS: {
			item = array.getJSONObject(index);
			jsonObject = item.optJSONObject(BOOK_ITEM);
			if (jsonObject == null) {
				jsonObject = item.optJSONObject("document");
				if (jsonObject != null) {
					jsonObject.put(Book.IS_DOCUMENT, true);
				}
			}
			if (jsonObject != null) {
				jsonObject.put("note_count", item.optInt("note_count"));
			}
			return jsonObject;
		}
		case SEARCH_BOOKS:
		case SEARCH_EBOOKS:
			item = array.optJSONObject(index);
			jsonObject = item.optJSONObject(Book.EBOOK);
			if (jsonObject != null) {
				item.put(Book.EBOOK, true);
				double price = jsonObject.optDouble(Book.WEB_PRICE, 0);
				item.put(Book.WEB_PRICE, price);
			}
			return item;
		}
		return null;
	}
}
