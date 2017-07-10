package com.jingdong.app.reader.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.OrderingBookCaseActivity;
import com.jingdong.app.reader.activity.SettingsActivity;
import com.jingdong.app.reader.activity.UploadActivity;
import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.message.activity.ChatActivity;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.request.Error;
import com.jingdong.app.reader.request.Success;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;

public final class ShareDocHelper {

	private class Task extends AsyncTask<Void, Void, String> {
		private final static String RECEIVER_ID = "receiver_id";
		private final static String DOCUMENT_ID = "document_id";
		private final static String MESSAGE = "message";
		private final String title;
		private final String message;
		private Dialog dialog;

		private Task() {
			Resources resources = activity.getResources();
			title = resources.getString(R.string.private_msg_share_book);
			message = resources.getString(R.string.private_msg_sharing);
		}

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(activity, title, message, true, true, initCancelListener());
		}

		@Override
		protected String doInBackground(Void... params) {
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put(WebRequestHelper.AUTH_TOKEN, LocalUserSetting.getToken(activity));
			String url = URLBuilder.addParameter(URLText.shareByMessage, paramMap);
			String postBody = getPostBody();
			String result = WebRequest.postWebDataWithContext(activity, url, postBody);
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			if (Error.fromJson(result) != null) {
				startUploadActivity();
			} else if (Success.fromJson(result) != null) {
				startChatActivity();
			}
		}
		
		private String getPostBody() {
			Map<String, String> postBody = new HashMap<String, String>();
			postBody.put(RECEIVER_ID, String.valueOf(userId));
			postBody.put(DOCUMENT_ID, String.valueOf(getServerId()));
			if (ShareDocHelper.this.message != null && !ShareDocHelper.this.message.equals(""))
				try {
					postBody.put(MESSAGE, URLEncoder.encode(ShareDocHelper.this.message, WebRequestHelper.CHAR_SET));
				} catch (UnsupportedEncodingException e) {
					MZLog.e("Encode", Log.getStackTraceString(e));
				}
			String postString = URLBuilder.getPostTextFromMap(postBody);
			return postString;
		}

		private long getServerId() {
			DocBind docBind = MZBookDatabase.instance.getDocBind(sharedDocument.documentId,
					LoginUser.getpin());

			long serverId = docBind.serverId;
			return serverId;
		}
	}

	private final Activity activity;
	private final String userId;
	private final Document sharedDocument;
	private String message;
	private Task task;

	public ShareDocHelper(Activity activity, String userId, Document sharedDocument, String message) {
		this.activity=activity;
		this.userId = userId;
		this.sharedDocument = sharedDocument;
		this.message = message;
	}
	

	

	public void shareDoc() {
		task = new Task();
		task.execute();
	}

	private OnCancelListener initCancelListener() {
		return new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (task != null)
					task.cancel(false);
			}
		};
	}

	private void startChatActivity() {
		Intent intent = new Intent(activity, ChatActivity.class);
		intent.putExtra(ChatActivity.USER_ID, userId);
		intent.putExtra(SettingsActivity.CHAT_ACTIONBAR_TITLE, activity.getString(R.string.private_msg_share_book));
		activity.startActivity(intent);
	}

	private void startUploadActivity() {
		Intent intent = new Intent(activity, UploadActivity.class);
		intent.putExtra("type", UploadActivity.TYPE);
		intent.putExtra("document", sharedDocument);
		activity.startActivityForResult(intent, OrderingBookCaseActivity.UPLOAD_ACTIVITY);
	}
}
