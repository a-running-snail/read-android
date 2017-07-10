package com.jingdong.app.reader.message.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.entity.extra.ConversationItem;
import com.jingdong.app.reader.me.activity.PrivateMessageActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.net.url.PagedBasedUrlGetter;
import com.jingdong.app.reader.privateMsg.Conversation;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.ListInterface;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.TaskStatus;
import com.jingdong.app.reader.util.UiStaticMethod;

public class MessageListModel extends ObservableModel implements ListInterface<Conversation> {
	private static final String CONVERSATIONS = "conversations";

	public static enum TaskType {
		LOAD_INIT_LIST, LOAD_NEXT_PAGE, LOAD_PREVIOUS_PAGE, DELETE_CONVERSATION
	}

	private class Task extends AsyncTask<TaskType, Void, String> implements TaskStatus {

		private final static String PAGE = "page";
		private final static String ID = "id";
		private final long conversationId;
		private TaskType type;

		public Task() {
			this(0);
		}

		public Task(long userId) {
			this.conversationId = userId;
		}

		String jsonString = null;
		@Override
		protected String doInBackground(TaskType... params) {
			this.type = params[0];

			switch (params[0]) {
			case LOAD_INIT_LIST:
				WebRequestHelper.gets(URLText.Message_URL, RequestParamsPool
						.getAlarmParams(10 + "", ""), true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onFailure(int arg0, Header[] arg1, byte[] arg2,
									Throwable arg3) {
								// TODO Auto-generated method stub
//								Toast.makeText(context,
//										R.string.network_connect_error,
//										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onResponse(int statusCode, Header[] headers,
									byte[] responseBody) {
								// TODO Auto-generated method stub

								jsonString = new String(responseBody);

								Log.d("cj", "result=======>>" + jsonString);

							}

						});
				break;
			case LOAD_NEXT_PAGE:
				WebRequestHelper.gets(URLText.Message_URL, RequestParamsPool
						.getAlarmParams(10 + "", conversations.get(conversations.size() - 1).getId() + ""), true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onFailure(int arg0, Header[] arg1, byte[] arg2,
									Throwable arg3) {
								// TODO Auto-generated method stub
								Toast.makeText(context,
										R.string.network_connect_error,
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onResponse(int statusCode, Header[] headers,
									byte[] responseBody) {
								// TODO Auto-generated method stub

								jsonString = new String(responseBody);

								Log.d("cj", "result=======>>" + jsonString);

							}

						});
				break;
			case LOAD_PREVIOUS_PAGE:
//				jsonString = WebRequest.getWebDataWithContext(context, url);
				WebRequestHelper.gets(URLText.Message_URL, RequestParamsPool
						.getAlarmParams(10 + conversations.get(0).getId() + "",  ""), true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onFailure(int arg0, Header[] arg1, byte[] arg2,
									Throwable arg3) {
								// TODO Auto-generated method stub
								Toast.makeText(context,
										R.string.network_connect_error,
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onResponse(int statusCode, Header[] headers,
									byte[] responseBody) {
								// TODO Auto-generated method stub

								jsonString = new String(responseBody);

								Log.d("cj", "result=======>>" + jsonString);

							}

						});
				break;
			case DELETE_CONVERSATION:
//				jsonString = WebRequest.postWebDataWithContext(context, url, getPostBody());
				WebRequestHelper.deletes(context,URLText.Delete_Message_URL + conversationId, RequestParamsPool
						.getDeleteMessageParams(), true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onFailure(int arg0, Header[] arg1, byte[] arg2,
									Throwable arg3) {
								// TODO Auto-generated method stub
								Toast.makeText(context,
										R.string.network_connect_error,
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onResponse(int statusCode, Header[] headers,
									byte[] responseBody) {
								// TODO Auto-generated method stub

								jsonString = new String(responseBody);

								Log.d("cj", "result=======>>" + jsonString);

							}

						});
				break;
			default:
				throw new IllegalArgumentException(params[0].name());
			}
			return jsonString;
		}

		@Override
		protected void onPostExecute(String result) {
			boolean isConnected, hasContent = false;
			isConnected = UiStaticMethod.isNetWorkConnected(result);
			if (isConnected) {
				switch (type) {
				case LOAD_INIT_LIST:
					hasContent = parseConversationList(0,result, conversations);
					break;
				case LOAD_NEXT_PAGE:
					if (hasContent)
						page++;
					hasContent = parseConversationList(1,result, conversations);
					break;
				case LOAD_PREVIOUS_PAGE:
					hasContent = processPullToRefresh(result);
					if (hasContent)
						page++;
					break;
				case DELETE_CONVERSATION:
					hasContent = parsePostResult(result);
					if (hasContent)
						deleteItem(conversationId);
					break;
				default:
					throw new IllegalArgumentException(type.name());
				}
			}
			notifyDataChanged(type.ordinal(), isConnected, hasContent);
		}

		private String getUrl(TaskType type) {
			Map<String, String> paramMap = new HashMap<String, String>();
			String baseUrl = null, result;
			switch (type) {
			case LOAD_NEXT_PAGE:
				paramMap.put(PAGE, String.valueOf(page));
			case LOAD_INIT_LIST:
			case LOAD_PREVIOUS_PAGE:
				baseUrl = URLText.privateMessageList;
				paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
				break;
			case DELETE_CONVERSATION:
				baseUrl = URLText.deleteConversation;
				paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
				break;
			}
			result = URLBuilder.addParameter(baseUrl, paramMap);
			return result;
		}

		private String getPostBody() {
			String body;
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put(ID, String.valueOf(conversationId));
			body = URLBuilder.getPostTextFromMap(paramMap);
			return body;
		}

		@Override
		public Status getTaskStatus() {
			return getStatus();
		}
	}

	private List<Conversation> conversations = new LinkedList<Conversation>();
	private Context context;
	private int page = PagedBasedUrlGetter.FIRST_PAGE;

	public MessageListModel(Context context) {
		this.context = context;
	}

	public TaskStatus loadMessageList() {
		Task task = new Task();
		task.execute(TaskType.LOAD_INIT_LIST);
		return task;
	}

	public TaskStatus loadNextPage() {
		Task task = new Task();
		task.execute(TaskType.LOAD_NEXT_PAGE);
		return task;
	}

	public TaskStatus loadPreviousPage() {
		Task task = new Task();
		task.execute(TaskType.LOAD_PREVIOUS_PAGE);
		return task;
	}

	public TaskStatus deleteConversation(long converstionId) {
		Task task = new Task(converstionId);
		task.execute(TaskType.DELETE_CONVERSATION);
		return task;
	}

	@Override
	public int size() {
		return conversations.size();
	}

	@Override
	public Conversation get(int postion) {
		return conversations.get(postion);
	}

	private void deleteItem(long conversationId) {
		Conversation conversation = new Conversation(conversationId);
		conversations.remove(conversation);
	}

	private boolean processPullToRefresh(String result) {
		boolean hasNew;
		List<Conversation> headConversations = new LinkedList<Conversation>();
		hasNew = parseConversationList(1,result, headConversations);
		if (hasNew) {
			conversations.clear();
			conversations.addAll(headConversations);
		}
		return hasNew;
	}

	private static boolean parseConversationList(int type,String result, List<Conversation> conversations) {
		boolean hasContent = false;
		try {
			JSONArray jsonArray = new JSONObject(result).getJSONArray(CONVERSATIONS);
			JSONObject jsonObject;
			Conversation conversation;
			if (type == 0) {
				conversations.clear();
			}
			for (int i = 0; i < jsonArray.length(); i++) {
				hasContent = true;
				jsonObject = jsonArray.getJSONObject(i);
				conversation = Conversation.fromJson(jsonObject);
				conversations.add(conversation);
			}
		} catch (JSONException e) {
			MZLog.e("PrivateMessage", Log.getStackTraceString(e));
			hasContent = false;
		}
		return hasContent;
	}
}
