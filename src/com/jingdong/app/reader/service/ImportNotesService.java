package com.jingdong.app.reader.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.loopj.android.http.RequestParams;

public class ImportNotesService extends IntentService {

	public static final int DEFAULT_NOTES_COUNT = 20;
	public static final String IMPORT_TYPE = "type";
	public static final String IMPORT_TYPE_EBOOK = "ebook";
	public static final String IMPORT_TYPE_DOCUMENT = "document";
	public static final String IMPORT_DOCUMENT_SIGN = "documentSign";
	public static final String IMPORT_DOCUMENT_ID = "documentId";
	public static final String IMPORT_EBOOK_ID = "ebookid";
	public static final String IMPORT_USERS_ID = "usersid";
	public static final String IMPORT_NOTES_PAGE = "notesPage";
	public static final String IMPORT_NOTES_COUNT = "notesCount";
	public static final String IMPORT_NOTES_SUCCESS = "import_notes_success";

	private int total = 0;
	private int pageIndex = 0;
	private int noteCount = 0;
	private int documentId = 0;
	private long ebookId = 0;
	private long lastSyncTime = 0;
	private String usersId;
	private String documentSign;
	
	public ImportNotesService() {
		super("ImportNotesService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		usersId = intent.getStringExtra(IMPORT_USERS_ID);
		ebookId = intent.getLongExtra(IMPORT_EBOOK_ID, 0);
		documentId = intent.getIntExtra(IMPORT_DOCUMENT_ID, 0);
		documentSign = intent.getStringExtra(IMPORT_DOCUMENT_SIGN);
		noteCount = intent.getIntExtra(IMPORT_NOTES_COUNT, 0);
		pageIndex = intent.getIntExtra(IMPORT_NOTES_PAGE, 0);
		requestSyncNotes();
	}
	
	private void requestSyncNotes() {
		if (!NetWorkUtils.isNetworkConnected(this)
				|| TextUtils.isEmpty(usersId)
				|| (ebookId == 0 && TextUtils.isEmpty(documentSign)))
			return;

		String url = URLText.getSomeoneAllNotes;
		RequestParams request = RequestParamsPool.getSomeoneAllNotes(usersId,
				ebookId, documentSign, pageIndex, DEFAULT_NOTES_COUNT);
		WebRequestHelper.get(url, request, true,
				new MyAsyncHttpResponseHandler(MZBookApplication.getContext()) {

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);
						List<ReadNote> notes = new ArrayList<ReadNote>();
						try {
							JSONObject resultObject = new JSONObject(result);
							total = resultObject.optInt("total");
							JSONArray notesArr = resultObject
									.optJSONArray("notes");

							if (notesArr != null && notesArr.length() > 0) {
								for (int i = 0; i < notesArr.length(); i++) {
									JSONObject notesObject = notesArr
											.optJSONObject(i);
									ReadNote note = ReadNote.parseFromJson(
											notesObject, documentId);
									note.modified = false;
									lastSyncTime = Math.max(lastSyncTime,
											note.updateTime);
									notes.add(note);
								}

							}

						} catch (JSONException e) {
							e.printStackTrace();
							total = 0;
						}

						for (ReadNote nt : notes) {
							MZBookDatabase.instance.insertOrUpdateEbookNote(nt);
						}
						pageIndex++;
						noteCount += notes.size();
						if (ebookId != 0) {
							MZBookDatabase.instance.updateNoteSyncTime(
									LoginUser.getpin(), ebookId, 0,
									lastSyncTime, noteCount, usersId);
						} else if (!documentSign.equals("") && documentId != 0) {
							MZBookDatabase.instance.updateNoteSyncTime(
									LoginUser.getpin(), 0, documentId,
									lastSyncTime, noteCount, usersId);
						}

						if (noteCount >= total || notes.size() == 0) {
							Intent it = new Intent();
							it.setAction(IMPORT_NOTES_SUCCESS);
							it.putExtra("identity", usersId);
							LocalBroadcastManager.getInstance(
									MZBookApplication.getContext())
									.sendBroadcast(it);
						} else {
//							it.setAction(IMPORT_NOTES_GO_ON);
//							it.putExtra(IMPORT_USERS_ID, usersId);
//                            it.putExtra(IMPORT_EBOOK_ID, ebookId);
//                            it.putExtra(IMPORT_DOCUMENT_ID, documentId);
//                            it.putExtra(IMPORT_DOCUMENT_SIGN, documentSign);
//                            it.putExtra(IMPORT_NOTES_COUNT, noteCount);
//                            it.putExtra(IMPORT_NOTES_PAGE, pageIndex);
							requestSyncNotes();
						}
						
					}

				});
	}
}
