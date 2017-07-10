package com.jingdong.app.reader.me.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.jingdong.app.reader.me.adapter.BookNoteAdapter;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.net.url.PagedBasedUrlGetter;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.core.BookNoteInterface;
import com.jingdong.app.reader.timeline.model.core.RenderBody;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;

public class RemoteBookNoteModel extends ObservableModel implements BookNoteModelInterface {
	private static final String BOOKS = "/books/";
	private static final String DOCUMENTS = "/documents/";
	private static final String USERS = "/users/";
	private static final String NOTES = "/notes.json";
	private static final String ORDER = "order";
	private static final String CHAPTER = "chapter";
	private static final String PAGE = "page";
	private static final String KEY_NOTES = "notes";
	private static final String POST_PUBLIC = "public";
	private static final String ON = "on";
	private static final String OFF = "off";
	private Context context;
	private String userId;
	private long bookId;
	private long deleteItem;
	private List<BookNoteInterface> tempNotes;
	private List<BookNoteInterface> bookNotes;
	private List<String> chapterNames;
	private List<Object> row;
	private int currentPage;
	
	private boolean isDocument=false;
	private long documentId=-1;

	public RemoteBookNoteModel(Context context) {
		this.context = context;
		tempNotes = new LinkedList<BookNoteInterface>();
		bookNotes = new LinkedList<BookNoteInterface>();
		chapterNames = new LinkedList<String>();
		row = new LinkedList<Object>();
		currentPage = PagedBasedUrlGetter.FIRST_PAGE;
	}

	public RemoteBookNoteModel(Context context, String userId, long bookId) {
		this(context);
		this.userId = userId;
		this.bookId = bookId;
	}
	
	public RemoteBookNoteModel(Context context, String userId, long bookId,boolean isDocument) {
		this(context);
		this.userId = userId;
		this.documentId = bookId;
		this.isDocument=isDocument;
	}
	
	public RemoteBookNoteModel(Context context, String userId, long bookId,long documentId) {
		this(context);
		this.userId = userId;
		this.bookId = bookId;
		this.documentId = documentId;
	}

	String jsonString = "";
	public void loadBookComments(final int page) {

//		if(isDocument)
//		{
//			jsonString=WebRequest.getWebDataWithContext(context, getNotesUrl(getBaseDocumentUrl()));
//		}
//		else {
//			jsonString=WebRequest.getWebDataWithContext(context, getNotesUrl(getBaseUrl()));
//		}
		
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				WebRequestHelper.get(URLText.User_book_notes,
						RequestParamsPool.getUser_Book_NoteParams(page+"",
								"",userId,(bookId == 0 ? "" : bookId)+"",documentId+""), true,
						new MyAsyncHttpResponseHandler(context) {

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
								boolean connected = false, hasContent = false;
								if (UiStaticMethod.isNetWorkConnected(jsonString)) {
									connected = true;
									try {
										hasContent = parseJson(jsonString);
									} catch (JSONException e) {
										MZLog.e("BookNote", Log.getStackTraceString(e));
									}
								} else {
									currentPage--;
								}
								notifyDataChanged(TYPE.LOAD.ordinal(), connected, hasContent);
							}
						});
			}
		});
		

	}

	public void deleteNote(long noteId) {
		this.deleteItem = noteId;
		String url = getNotesUrl(noteId, URLText.deleteNotesUrl);
		String result = WebRequest.deleteWebDataWithContext(context, url);
		boolean success = parsePostResult(result);
		notifyDataChanged(TYPE.DELETE.ordinal(), success);
	}

	public void deleteNoteCallBack() {
		RenderBody renderBody = new RenderBody();
		renderBody.setId(deleteItem);
		bookNotes.remove(renderBody);
		BookNoteAdapter.resetChapterNames(chapterNames, bookNotes);
		BookNoteAdapter.merge(row, chapterNames, bookNotes);
		deleteItem = -1;
	}

	public void updatePrivacy(final long noteId, boolean isPrivate, final TYPE type) {
//		String url = getNotesUrl(noteId, URLText.updatePublicUrl);
		final String value;
		if (isPrivate)
			value = ON;
		else
			value = OFF;
//		String post = POST_PUBLIC + "=" + value;
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				WebRequestHelper.post(URLText.updatePublicUrl,
						RequestParamsPool.getBookNoteStatueParams(noteId+"",value), true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {
								String jsonString = new String(responseBody);
								boolean success = parsePostResult(jsonString);
								notifyDataChanged(type.ordinal(), success, noteId);
							}
						});
			}
		});
	}

	public void refreshList() {
		bookNotes.addAll(tempNotes);
		makeChapterNames();
		tempNotes.clear();
		BookNoteAdapter.merge(row, chapterNames, bookNotes);
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setBookId(long bookId) {
		this.bookId = bookId;
	}

	public BookNoteInterface getBookNoteAt(int position) {
		return bookNotes.get(position);
	}

	public int getIndexById(long id) {
		RenderBody renderBody = new RenderBody();
		renderBody.setId(id);
		return bookNotes.indexOf(renderBody);
	}

	public int getBookNoteNumber() {
		return bookNotes.size();
	}

	public String getChapterNameAt(int position) {
		return chapterNames.get(position);
	}

	public int getChapterNumber() {
		return chapterNames.size();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.jingdong.app.reader.me.model.BookNoteModelInterface#getRowNumber()
	 */
	@Override
	public int getRowNumber() {
		return row.size();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.jingdong.app.reader.me.model.BookNoteModelInterface#getRowAt(int)
	 */
	@Override
	public Object getRowAt(int position) {
		return row.get(position);
	}

	private String getBaseUrl() {
		StringBuilder builder = new StringBuilder();
		builder.append(URLText.baseUrl);
		builder.append(BOOKS);
		builder.append(String.valueOf(bookId));
		builder.append(USERS);
		builder.append(String.valueOf(userId));
		builder.append(NOTES);
		String baseUrl = builder.toString();
		return baseUrl;
	}

	private String getBaseDocumentUrl() {
		StringBuilder builder = new StringBuilder();
		builder.append(URLText.baseUrl);
		builder.append(DOCUMENTS);
		builder.append(String.valueOf(documentId));
		builder.append(USERS);
		builder.append(String.valueOf(userId));
		builder.append(NOTES);
		String baseUrl = builder.toString();
		return baseUrl;
	}
	
	
	private String getNotesUrl(String baseUrl) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		paramMap.put(ORDER, CHAPTER);
		paramMap.put(PAGE, String.valueOf(currentPage++));
		String url = URLBuilder.addParameter(baseUrl, paramMap);
		return url;
	}

	private String getNotesUrl(long noteId, String baseUrl) {
		String fullUrl = baseUrl + String.valueOf(noteId) + ".json";
		Map<String, String> parmMap = new HashMap<String, String>();
		parmMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		String url = URLBuilder.addParameter(fullUrl, parmMap);
		return url;
	}

	private boolean parseJson(String jsonString) throws JSONException {
		boolean hasContent = false;
		JSONObject jsonObject = new JSONObject(jsonString);
		JSONArray array = jsonObject.optJSONArray(KEY_NOTES);
		RenderBody entity;
		tempNotes.clear();
		if (array.length() != 0) {
			hasContent = true;
			for (int i = 0; i < array.length(); i++) {
				entity = new RenderBody();
				entity.parseJson(array.getJSONObject(i), true);
				Log.d("cj", "isdelete==========>>>>>>>" + entity.isDeleted());
				if (!entity.isDeleted()) {
					tempNotes.add(entity);
				}
			}
		}
		return hasContent;
	}

	private void makeChapterNames() {
		if (!tempNotes.isEmpty()) {
			String lastChapter = null;
			if (!chapterNames.isEmpty())
				lastChapter = chapterNames.get(chapterNames.size() - 1);
			BookNoteInterface firstChapter = tempNotes.get(0);
			String currentChapter;
			if (firstChapter.getChapterName().equals(lastChapter))
				currentChapter = lastChapter;
			else {
				currentChapter = firstChapter.getChapterName();
				chapterNames.add(currentChapter);
			}
			for (BookNoteInterface note : tempNotes) {
				if (!currentChapter.equals(note.getChapterName())) {
					currentChapter = note.getChapterName();
					chapterNames.add(currentChapter);
				}
			}
		}
	}

	private void notifyDataChanged(int type, boolean success, long notesId) {
		Message message = Message.obtain();
		message.what = type;
		message.arg1 = success ? SUCCESS_INT : FAIL_INT;
		message.arg2 = (int) notesId;
		setChanged();
		notifyObservers(message);
	}
}
