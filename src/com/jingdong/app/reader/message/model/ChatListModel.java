package com.jingdong.app.reader.message.model;

import java.io.File;
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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jingdong.app.reader.R;

import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.data.db.MZBookDatabase;import com.jingdong.app.reader.io.StoragePath;
import com.jingdong.app.reader.me.model.UserDetail;
import com.jingdong.app.reader.message.activity.ChatActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.privateMsg.DocumentRequest;
import com.jingdong.app.reader.privateMsg.PrivateMessage;
import com.jingdong.app.reader.privateMsg.DocumentRequest.BorrowStatus;
import com.jingdong.app.reader.request.Error;
import com.jingdong.app.reader.request.Redirect;
import com.jingdong.app.reader.request.Success;
import com.jingdong.app.reader.service.OpdsBookDownloadService;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.ListInterface;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.TaskStatus;
import com.jingdong.app.reader.util.UiStaticMethod;

public class ChatListModel extends ObservableModel implements ListInterface<PrivateMessage>, ApproveBorrowInterface{
	public static enum TaskType {
		LOAD_USER_INFO, LOAD_INIT_LIST, LOAD_PREVIOUS_PAGE, SEND_TEXT, UPDATE_LIST, SCHEDULE_UPDATE, APPROVE_BORROW, DOWNLOAD_LINK, SEARCH_LOCAL_BOOK, DENY_BORROW;
	}

	private final static String MESSAGES = "messages";

	private class Task extends AsyncTask<Void, Void, String> implements TaskStatus {
		private long documentId;
		private String messageBody;
		private TaskType type;
		private Document document;
		private ProgressDialog progressDialog;
		private DocumentRequest documentRequest;
		private Context contxt;

		private final static String BEFORE_ID = "before_id";
		private final static String MESSAGE_BODY = "message[body]";
		private final static String MESSAGE_RECEIVER = "message[receiver_id]";
		private final static String DOCUMENT_REQUEST_ID = "document_request_id";
		private final static String POST_STATUS = "status";

		private Task(TaskType type,Context contxt) {
//			this(type, null, -1);
			this.type = type;
			this.messageBody = messageBody;
			this.documentId = documentId;
			this.documentRequest = null;
			this.contxt = contxt;
		}

		private Task(TaskType type, Document document) {
			this(type, document, null);
		}

		private Task(TaskType type, DocumentRequest documentRequest) {
			this(type, null, documentRequest);
		}

		private Task(TaskType type, String messageBody) {
			this(type, messageBody, -1);
		}

		private Task(TaskType type, long documentId) {
			this(type, null, documentId);
		}

		private Task(TaskType type, Document document, DocumentRequest documentRequest) {
			this.type = type;
			this.document = document;
			this.documentRequest = documentRequest;
		}

		private Task(TaskType type, String messageBody, long documentId) {
			this.type = type;
			this.messageBody = messageBody;
			this.documentId = documentId;
			this.documentRequest = null;
		}

		@Override
		protected void onPreExecute() {
			Resources resources = context.getResources();
			String title, message;
			switch (type) {
			case APPROVE_BORROW:
				title = resources.getString(R.string.borrow);
				message = resources.getString(R.string.borrowing);
				progressDialog = ProgressDialog.show(context, title, message, true, false);
				break;
			case DOWNLOAD_LINK:
				title = resources.getString(R.string.download);
				message = resources.getString(R.string.start_download);
				progressDialog = ProgressDialog.show(context, title, message, true, false);
				break;
			case SEARCH_LOCAL_BOOK:
				title = resources.getString(R.string.search);
				message = resources.getString(R.string.search_local_book);
				progressDialog = ProgressDialog.show(context, title, message, true, false);
				break;
			case DENY_BORROW:
				title = resources.getString(R.string.deny);
				message = resources.getString(R.string.denying);
				progressDialog = ProgressDialog.show(context, title, message, true, false);
				break;
			default:
				break;
			}
		}

		String jsonString = null;
		@Override
		protected String doInBackground(Void... params) {
			final String url;
			String postBody;
			url = getUrl();
			switch (type) {
			case SEND_TEXT:
				WebRequestHelper.posts(url,
						RequestParamsPool
								.getSendMessageParams(messageBody,userId), true,
						new MyAsyncHttpResponseHandler(context) {

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
							}
						});
//				postBody = getPostBody();
//				jsonString = WebRequest.postWebDataWithContext(context, url, postBody);
				break;
			case LOAD_USER_INFO:
			case UPDATE_LIST:
			case LOAD_INIT_LIST:
				WebRequestHelper.gets(url,
						RequestParamsPool
								.getPrivateMessageParams("",userId,20+"",name), true,
						new MyAsyncHttpResponseHandler(context) {

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
							}
						});
				break;
			case LOAD_PREVIOUS_PAGE:
				String before_id = null;
				if (messageList.size() > 0){
					before_id = String.valueOf(messageList.get(0).getId());
				}else {
					before_id = "";
				}
				WebRequestHelper.gets(url,
						RequestParamsPool
								.getPrivateMessageParams(before_id,userId,20+"",name), true,
						new MyAsyncHttpResponseHandler(context) {

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
							}
						});
//				jsonString = WebRequest.getWebDataWithContext(context, url);
				break;
			case DENY_BORROW:
			case APPROVE_BORROW:
				postBody = getPostBody();
				jsonString = WebRequest.postWebDataWithContext(context, url, postBody);
				break;
			case DOWNLOAD_LINK:
				jsonString = WebRequest.getWebDataWithContextWithoutRedirect(context, url);
				break;
			case SEARCH_LOCAL_BOOK:
				long id = MZBookDatabase.instance.getLocalDocumentId(document.getDocumentId(), document.getOpfMD5());
				document = MZBookDatabase.instance.getDocument((int) id);
				jsonString = String.valueOf(id);
				break;
			default:
				throw new IllegalArgumentException(type.name());
			}
			return jsonString;
		}
		
		boolean isConnected, hasContent = false;
		@Override
		protected void onPostExecute(String result) {
			
			Bundle bundle;
			if (result != null) {
				isConnected = UiStaticMethod.isNetWorkConnected(result);
			}
			if (isConnected) {
				switch (type) {
				case LOAD_USER_INFO:
					hasContent = true;
					UserDetail userDetail = new UserDetail();
					try {
						userDetail.parseJson(new JSONObject(result));
						notifyDataChanged(type.ordinal(), isConnected, hasContent, userDetail);
						return;
					} catch (JSONException e) {
						hasContent = false;
					}
					break;
				case LOAD_INIT_LIST:
					hasContent = parsePrivateMessage(result, messageList);
					break;
				case LOAD_PREVIOUS_PAGE:
					hasContent = processPullTorefresh(result);
					break;
				case SEND_TEXT:
					hasContent = parsePostResult(result, POST_STATUS, SUCCESS);
					break;
				case UPDATE_LIST:
					hasContent = processUpdateList(result);
					break;
				case APPROVE_BORROW:
					if (progressDialog != null)
						progressDialog.dismiss();
					hasContent = processApproveOrDeny(result, documentRequest, type);
					bundle = new Bundle();
					bundle.putParcelable(DOCUMENT, document);
					bundle.putParcelable(DOCUMENT_REQUEST, documentRequest);
					notifyDataChanged(type.ordinal(), isConnected, hasContent, null, bundle);
					return;
				case DENY_BORROW:
					if (progressDialog != null)
						progressDialog.dismiss();
					hasContent = processApproveOrDeny(result, documentRequest, type);
					notifyDataChanged(type.ordinal(), isConnected, hasContent, tempMessage);
					return;
				case DOWNLOAD_LINK:
					if (progressDialog != null)
						progressDialog.dismiss();
					hasContent = processDownloadLink(result);
					bundle = new Bundle();
					bundle.putString(Redirect.LOCATION, tempMessage);
					bundle.putString(OpdsBookDownloadService.OpdsBookNameKey, messageBody);
					notifyDataChanged(type.ordinal(), isConnected, hasContent, null, bundle);
					return;
				case SEARCH_LOCAL_BOOK:
					if (progressDialog != null)
						progressDialog.dismiss();
					bundle = new Bundle();
					bundle.putParcelable(DOCUMENT_REQUEST, documentRequest);
					if (document != null)
						bundle.putParcelable(DOCUMENT, document);
					notifyDataChanged(type.ordinal(), isConnected, true, Long.valueOf(Long.parseLong(result)), bundle);
					return;
				default:
					throw new IllegalArgumentException(type.name());
				}
			}
			notifyDataChanged(type.ordinal(), isConnected, hasContent);
		}

		@Override
		public Status getTaskStatus() {
			return getStatus();
		}

		private String getUrl() {
			String result, baseUrl;
			Map<String, String> paramMap = new HashMap<String, String>();
			switch (type) {
			case SEND_TEXT:
//				result = getUrl(URLText.sendMessage, paramMap);
				result = URLText.Send_Message_URL;
				break;
			case LOAD_PREVIOUS_PAGE:
//				if (messageList.size() > 0)
//					paramMap.put(BEFORE_ID, String.valueOf(messageList.get(0).getId()));
			case LOAD_INIT_LIST:
			case UPDATE_LIST:
//				baseUrl = String.format(Locale.US, URLText.messageHistory, userId);
//				result = getUrl(baseUrl, paramMap);
				result = URLText.Message_URL + "/history";
				break;
			case LOAD_USER_INFO:
				paramMap.put(UserDetail.WITHOUT_ENTITY, Boolean.toString(true));
				paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
				baseUrl = URLText.usersPublicUrl + userId + ".json";
				result = URLBuilder.addParameter(baseUrl, paramMap);
				break;
			case APPROVE_BORROW:
				result = getUrl(URLText.borrowApprove, paramMap);
				break;
			case DENY_BORROW:
				result = getUrl(URLText.borrowDeny, paramMap);
				break;
			case DOWNLOAD_LINK:
				baseUrl = String.format(Locale.US, URLText.downloadDocument, documentId);
				result = getUrl(baseUrl, paramMap);
				break;
			case SEARCH_LOCAL_BOOK:
				result = "";
				break;
			default:
				throw new IllegalArgumentException(type.name());
			}
			return result;
		}

		private String getPostBody() {
			String body;
			Map<String, String> paramMap = new HashMap<String, String>();
			switch (type) {
			case SEND_TEXT:
				paramMap.put(MESSAGE_BODY, messageBody);
				paramMap.put(MESSAGE_RECEIVER, String.valueOf(userId));
				body = URLBuilder.getPostTextFromMap(paramMap);
				break;
			case DENY_BORROW:
			case APPROVE_BORROW:
				paramMap.put(DOCUMENT_REQUEST_ID, String.valueOf(documentRequest.getId()));
				body = URLBuilder.getPostTextFromMap(paramMap);
				break;
			default:
				throw new IllegalArgumentException(type.name());
			}
			return body;
		}

		/**
		 * 得到URL与auth_token，这个方法会在参数列表中添加auth_token这一项
		 * 
		 * @param baseUrl
		 *            基础URL
		 * @param paramMap
		 *            参数列表
		 * @return 一个完整的URL
		 */
		private String getUrl(String baseUrl, Map<String, String> paramMap) {
			paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
			return URLBuilder.addParameter(baseUrl, paramMap);
		}
	}

	public final static String DOCUMENT = "document";
	public final static String DOCUMENT_REQUEST = "documentRequest";
	private final static String DEBUG_TEXT = "debug:help!!!";
	private final static String FEED_BACK_EMAIL="feedback@mzread.com";
	private final static long RRRR_ID=1;
	private String userId;
	private Context context;
	private String name;
	private List<PrivateMessage> messageList = new LinkedList<PrivateMessage>();
	// 这个是在处理APPROVE_BORROW时候，用来暂时保存Error.message的值
	private String tempMessage;

	public ChatListModel(Context context, String userId) {
		this.context = context;
		this.userId = userId;
	}
	
	public ChatListModel(Context context, String userId,String name) {
		this.context = context;
		this.userId = userId;
		this.name = name;
	}

	@Override
	public int size() {
		return messageList.size();
	}

	@Override
	public PrivateMessage get(int postion) {
		return messageList.get(postion);
	}

	public TaskStatus loadUserInfo(Context context) {
		Task task = new Task(TaskType.LOAD_USER_INFO,context);
		task.execute();
		return task;
	}

	public TaskStatus loadInit(Context context) {
		Task task = new Task(TaskType.LOAD_INIT_LIST,context);
		task.execute();
		return task;
	}

	public TaskStatus loadPrevious(Context context) {
		Task task = new Task(TaskType.LOAD_PREVIOUS_PAGE,context);
		task.execute();
		return task;
	}

	public TaskStatus sendText(String message) {
		Task task = null;
		task = new Task(TaskType.SEND_TEXT, message);
		task.execute();
		return task;
	}

	/**
	 * 发送完信息后或者定时器到时间后，在这里刷新ChatList。发送网络请求，请求最新数据。
	 */
	public TaskStatus updateList(Context context) {
		Task task = new Task(TaskType.UPDATE_LIST,context);
		task.execute();
		return task;
	}

	@Override
	public TaskStatus approveBorrow(Document document, DocumentRequest docRequest) {
		Task task = new Task(TaskType.APPROVE_BORROW, document, docRequest);
		task.execute();
		return task;
	}

	public TaskStatus searchLocalBook(Document document, DocumentRequest docRequest) {
		Task task = new Task(TaskType.SEARCH_LOCAL_BOOK, document, docRequest);
		task.execute();
		return task;
	}

	public TaskStatus denyBorrowRequest(DocumentRequest documentRequest) {
		Task task = new Task(TaskType.DENY_BORROW, documentRequest);
		task.execute();
		return task;
	}

	@Override
	public TaskStatus getDownloadLink(long messageId, String bookName) {
		Task task = new Task(TaskType.DOWNLOAD_LINK, bookName, messageId);
		task.execute();
		return task;
	}

	private void processDebugText() {
		File dbFile = null;
		try {
			dbFile = getDBFile();
		} catch (NullPointerException e) {
			Toast.makeText(context, R.string.database_not_exist, Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog dialog=createAlertDialog(dbFile);
		dialog.show();
		notifyDataChanged(TaskType.SEND_TEXT.ordinal(), false);
	}

	private File getDBFile() {
		File path = StoragePath.getDatabaseDir(context);
		File dbFile = new File(path, "mzbook.db");
		if (dbFile.exists() && dbFile.isFile())
			return dbFile;
		else
			throw new NullPointerException();
	}

	private AlertDialog createAlertDialog(final File dbFile) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(R.string.bug_upload);
		dialogBuilder.setMessage(context.getResources().getString(R.string.database_info, dbFile.getAbsolutePath()));
		dialogBuilder.setPositiveButton(R.string.send_email, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent=createEmailIntent(dbFile);
				if(intent.resolveActivity(context.getPackageManager())!=null){
					context.startActivity(intent);
					dialog.dismiss();
				}
				else
					Toast.makeText(context, R.string.email_not_exit, Toast.LENGTH_SHORT).show();

			}
		});
		dialogBuilder.setNegativeButton(R.string.i_known_it, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		return dialogBuilder.create();
	}

	private Intent createEmailIntent(File file){
		Resources resources=context.getResources();
		Intent intent=new Intent(Intent.ACTION_SENDTO);
		intent.setType("text/plain");
		intent.setData(Uri.parse("mailto:" + FEED_BACK_EMAIL));
		intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.database_email_subject));
		intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.database_email_text,LocalUserSetting.getUserName(context)));
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		return intent;
	}
	
	/**
	 * 处理刷新列表请求，即向服务器请求最新的列表，然后添加到本地列表中。如果本地列表中的内容和服务器列表中的内容完全不重合，删除本地列表，加载服务器列表
	 * 。否则将服务器列表中的新内容添加到本地列表的尾部。
	 * 
	 * @param jsonString
	 *            服务器列表的jsonString
	 * @return true表示本地列表的数据进行了更新，false表示本地列表的数据未更新。
	 */
	private boolean processUpdateList(String jsonString) {
		boolean hasNew;
		List<PrivateMessage> newList = new LinkedList<PrivateMessage>();
		parsePrivateMessage(jsonString, newList);
		if (newList.isEmpty()) {
			hasNew = false;
		} else {
			hasNew = true;
			boolean modify = newList.removeAll(messageList);
			if (modify) {
				messageList.addAll(newList);
			} else {
				messageList.clear();
				messageList.addAll(newList);
			}
		}
		return hasNew;
	}

	/**
	 * 处理下拉刷新请求
	 * 
	 * @param jsonString
	 *            待构建的jsonString
	 * @return true表示含有新数据，false表示没有新数据。
	 */
	private boolean processPullTorefresh(String jsonString) {
		boolean hasNew;
		List<PrivateMessage> preivousList = new LinkedList<PrivateMessage>();
		hasNew = parsePrivateMessage(jsonString, preivousList);
		messageList.addAll(0, preivousList);
		return hasNew;
	}

	private boolean processApproveOrDeny(String jsonString, DocumentRequest documentRequest, TaskType type) {
		Success success;
		Error error;
		boolean result = false;
		if ((success = Success.fromJson(jsonString)) != null) {
			result = success.isSuccess();
			if (type == TaskType.APPROVE_BORROW)
				documentRequest.setStatus(BorrowStatus.ACCEPT);
			else if (type == TaskType.DENY_BORROW)
				documentRequest.setStatus(BorrowStatus.DENY);
		} else if ((error = Error.fromJson(jsonString)) != null) {
			tempMessage = error.getMessage();
			result = false;
		}
		return result;
	}

	private boolean processDownloadLink(String jsongString) {
		Error error;
		Redirect redirect;
		boolean result = false;
		if ((error = Error.fromJson(jsongString)) != null) {
			result = false;
			tempMessage = error.getMessage();
		} else if ((redirect = Redirect.fromJson(jsongString)) != null) {
			result = true;
			tempMessage = redirect.getLocation();
		}
		return result;
	}

	private static boolean parsePrivateMessage(String jsonString, List<PrivateMessage> messages) {
		boolean hasContent = false;
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray jsonArray = jsonObject.optJSONArray(MESSAGES);
			PrivateMessage privateMessage;
			if (jsonArray.length() != 0)
				hasContent = true;
			for (int i = 0; i < jsonArray.length(); i++) {
				privateMessage = PrivateMessage.fromJson(jsonArray.getJSONObject(i));
				messages.add(0, privateMessage);
			}
		} catch (JSONException e) {
			hasContent = false;
			MZLog.e("Chat", Log.getStackTraceString(e));
		}
		return hasContent;
	}

}
