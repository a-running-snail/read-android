package com.jingdong.app.reader.timeline.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.me.model.UserDetail;
import com.jingdong.app.reader.me.model.UserFollower;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.net.url.QueryUrlGetter;
import com.jingdong.app.reader.timeline.fragment.UserListFragment;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;

public class UserModel extends ObservableModel {
	public static class Note {
		private final static String NOTE_COUNT = "note_count";
		private final static String CONTENT_SIZE = "content_size";
		private int noteCount;
		private int contentSize;

		public void paresJson(JSONObject jsonObject) {
			noteCount = jsonObject.optInt(NOTE_COUNT);
			contentSize = jsonObject.optInt(CONTENT_SIZE);
		}

		public int getNoteCount() {
			return noteCount;
		}

		public int getContentSize() {
			return contentSize;
		}
	}

	public final static int LOAD_FOLLOWING_PEOPLE = 10;
	public final static int LOAD_AS_INPUT = 11;
	public final static int LOAD_FOLLOWING_MORE = 12;
	public final static int LOAD_QUERY_MORE = 13;
	public final static int FOLLOW_ALL = 14;
	private final static int ILLEGAL = -1;
	private final static char EQUALS = '=';
	private final static char COMMAS = ',';
	private final static String IDS = "ids";
	private final static String RELATION = "relation_with_current_user";
	private final static String RELATION_FOLLOWED = "followed";
	private final static String RELATION_FOLLOWING = "following";
	private final static String DOCUMENT = "document";
	private String input;
	private Context context;
	private Observer observer;
	private List<UserFollower> followModels;
	private List<UserInfo> followingList;
	private List<UserInfo> autoCompleteUsers;
	private List<UserInfo> tempUsers;
	/**
	 * 这个List无法在folloingList之间autoCompleteUser中相互切换
	 */
	private List<Note> tempNotes;
	private List<Note> notes;
	private List<Document> documents;
	private List<Document> tempDocuments;
	private QueryUrlGetter urlGetter;
	private boolean showButton = false;
	private final String arrayKey;
	private final String itemKey;
	private String jsonString;

	public UserModel(QueryUrlGetter queryUrlGetter, UserListFragment fragment,
			String arrayKey, String itemKey, String jsonString) {
		followingList = new LinkedList<UserInfo>();
		autoCompleteUsers = new LinkedList<UserInfo>();
		tempUsers = new LinkedList<UserInfo>();
		followModels = new LinkedList<UserFollower>();
		notes = new LinkedList<Note>();
		tempNotes = new LinkedList<Note>();
		documents = new LinkedList<Document>();
		tempDocuments = new LinkedList<Document>();
		urlGetter = queryUrlGetter;
		observer = fragment;
		this.context = fragment.getActivity();
		this.arrayKey = arrayKey;
		this.itemKey = itemKey;
		this.jsonString = jsonString;
	}

	/**
	 * 获取我关注的用户列表
	 * 
	 * @param context
	 *            当前数据上下文
	 */
	public void loadFollowingList() {
		String usersJson;
		if (jsonString != null)
			usersJson = jsonString;
		else
			usersJson = WebRequest.getWebDataWithContext(context,
					urlGetter.getInitPageUrl(null));
		parseJson(LOAD_FOLLOWING_PEOPLE, usersJson, followingList);
	}

	public void loadMoreFollowingUser() {
		String userJson = null;
		try {
			userJson = WebRequest.getWebDataWithContext(context,
					urlGetter.getNextPageUrl(null, null));
		} catch (UnsupportedOperationException e) {
			notifyDataChanged(LOAD_FOLLOWING_MORE, true, false);
			return;
		}
		parseJson(LOAD_FOLLOWING_MORE, userJson, followingList);
	}

	public void searchUsers(final Context context, final int type,
			final String query, final int currentpage, final int pagecount) {
		// String jsonUsers =
		// WebRequest.requestWebData(urlGetter.getQueryInitPageUrl(input), "",
		// WebRequest.httpGet);
		// parseJson(LOAD_AS_INPUT, jsonUsers, autoCompleteUsers);
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				WebRequestHelper.get(URLText.searchPeople,
						RequestParamsPool
								.searchPeopleParams(currentpage,pagecount, query), true,
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
								Log.d("cj", "resultsssssssssss=======>>"
										+ jsonString);
								parseJson(type, jsonString, autoCompleteUsers);
							}
						});
			}
		});
	}

	public void loadMoreSearchedUses() {
		String jsonString = WebRequest.requestWebData(
				urlGetter.getQueryNextPageUrl(input), "", WebRequest.httpGet);
		parseJson(LOAD_QUERY_MORE, jsonString, autoCompleteUsers);
	}

	/**
	 * 根据用户的输入，搜索对应的用户。
	 * 
	 * @return 如果用户的输入为空，返回当前用户的关注列表。否则返回从服务器搜索到的用户列表。
	 */
	public List<UserInfo> getUsers() {
		if (input == null || input.isEmpty())
			return followingList;
		else
			return autoCompleteUsers;
	}

	public List<Note> getNotes() {
		return notes;
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void refreshList(int type) {
		if (type == LOAD_FOLLOWING_PEOPLE || type == LOAD_AS_INPUT){
			getUsers().clear();
		}
		getUsers().addAll(tempUsers);
		notes.addAll(tempNotes);
		documents.addAll(tempDocuments);
		tempDocuments.clear();
		tempUsers.clear();
		tempNotes.clear();
	}

	public int getIndexById(String userId) {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(userId);
		int index = getUsers().indexOf(userInfo);
		return index;
	}

	/**
	 * 设置搜索关键词
	 * 
	 * @param input
	 *            关键词
	 */
	public void setInput(String input) {
		this.input = input;
	}

	/**
	 * 返回当前的搜索关键字
	 * 
	 * @return
	 */
	public String getInput() {
		return input;
	}

	public boolean isShowButton() {
		return showButton;
	}

	public void setShowButton(boolean showButton) {
		this.showButton = showButton;
		if (isShowButton()) {
			addFollowModelToNewUser(followingList);
		} else {
			for (UserFollower follower : followModels)
				follower.deleteObservers();
			followModels.clear();
		}
	}

	public UserFollower getFollowModel(int index) {
		return followModels.get(index);
	}

	public void followUsers(int type) {
		boolean success;
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		String url = URLBuilder.addParameter(URLText.batchFollow, paramMap);
		String postText = generatePostParam();
		String result = WebRequest.postWebDataWithContext(context, url,
				postText);
		if (parsePostResult(result)) {
			success = true;
		} else
			success = false;
		notifyDataChanged(type, success);
	}

	public void invalidateUsers() {
		List<UserInfo> userInfos = getUsers();
		UserDetail userDetail;
		UserFollower follower;
		for (int i = 0; i < userInfos.size(); i++) {
			userDetail = (UserDetail) userInfos.get(i);
			follower = getFollowModel(getIndexById(userDetail.getId()));
			follower.setFollowing(true);
			userDetail.setFollowedByCurrentUser(true);
		}
	}

	public void invalidateUser(String id, int success) {
		int index = getIndexById(id);
		UserFollower follower = getFollowModel(index);
		UserInfo userInfo = getUsers().get(index);
		boolean following = follower.isFollowing();
		if (success == TimeLineModel.FAIL_INT)
			follower.setFollowing(!following);
		else {
			if (userInfo instanceof UserDetail)
				((UserDetail) userInfo).setFollowedByCurrentUser(following);
		}
	}

	@Override
	public synchronized void deleteObserver(Observer observer) {
		super.deleteObserver(observer);
		for (UserFollower follower : followModels)
			follower.deleteObservers();
	}

	@Override
	public synchronized void deleteObservers() {
		super.deleteObservers();
		for (UserFollower follower : followModels)
			follower.deleteObservers();
	}

	private String generatePostParam() {
		StringBuilder builder = new StringBuilder();
		builder.append(IDS);
		builder.append(EQUALS);
		List<UserInfo> users = getUsers();
		for (int i = 0; i < users.size() - 1; i++) {
			builder.append(users.get(i).getId());
			builder.append(COMMAS);
		}
		builder.append(users.get(users.size() - 1).getId());
		return builder.toString();
	}

	/**
	 * 根据JSONString，填充用户列表
	 * 
	 * @param type
	 *            类型可能为LOAD_FOLLOWING_PEOPLE，LOAD_AS_INPUT，LOAD_QUERY_MORE，
	 *            LOAD_FOLLOWING_MORE
	 * @param jsonString
	 *            待解析的jsonString
	 * @param list
	 *            需要填充的列表
	 */
	private void parseJson(int type, String jsonString, List<UserInfo> list) {
		boolean connected, hasContent = false;
		if (connected = UiStaticMethod.isNetWorkConnected(jsonString)) {
			List<UserInfo> newUsers = parseJson(jsonString);
			if (newUsers != null && !newUsers.equals(followingList)
					&& !newUsers.isEmpty()) {
				hasContent = true;
				if (type == LOAD_FOLLOWING_PEOPLE || type == LOAD_AS_INPUT){
					list.clear();
				}
				addFollowModelToNewUser(newUsers);
				tempUsers.clear();
				tempUsers.addAll(newUsers);
			} else {
				hasContent = false;
			}
		}

		LocalBroadcastManager.getInstance(context).sendBroadcast(
				new Intent("com.mzread.action.dialog.canceled"));
		notifyDataChanged(type, connected, hasContent);
	}

	/**
	 * 解析json字符串，并把解析结果填充到一个List中
	 * 
	 * @param json
	 *            json字符串
	 * @return User的列表
	 */
	private List<UserInfo> parseJson(String json) {
		try {
			JSONArray userArray;
			if (arrayKey == null)
				userArray = new JSONArray(json);
			else
				userArray = new JSONObject(json).getJSONArray(arrayKey);
			if (userArray != null) {
				JSONObject jsonObject, userDocumentJsonObject;
				Note note;
				Document document;
				tempNotes.clear();
				tempDocuments.clear();
				List<UserInfo> usersList = new ArrayList<UserInfo>(
						userArray.length());
				for (int i = 0; i < userArray.length(); i++) {
					jsonObject = userArray.getJSONObject(i);
					userDocumentJsonObject = refactorJson(jsonObject);
					if (!userDocumentJsonObject.getString(UserInfo.ID).equals(
							LoginUser.getpin())) {
						note = new Note();
						document = Document.fromJson(jsonObject
								.optJSONObject(DOCUMENT));
						note.paresJson(userDocumentJsonObject);
						tempDocuments.add(document);
						tempNotes.add(note);
						usersList.add(getUser(userDocumentJsonObject));
					}
				}
				return usersList;
			} else {
				return null;
			}
		} catch (JSONException e) {
			MZLog.e("following", json.length() + json);
			MZLog.e("following", Log.getStackTraceString(e));
			return null;
		}
	}

	/**
	 * 重构JSON对象，并添加RecommendText属性，如果存在的话
	 * 
	 * @param jsonObject
	 *            待添加的JsonObject
	 * @throws JSONException
	 *             解析异常
	 */
	private JSONObject refactorJson(JSONObject jsonObject) throws JSONException {
		JSONObject result;
		if (itemKey != null)
			result = jsonObject.getJSONObject(itemKey);
		else
			result = jsonObject;
		initSuperField(jsonObject, result);
		return result;
	}

	/**
	 * @param jsonObject
	 * @param result
	 * @throws JSONException
	 */
	private void initSuperField(JSONObject jsonObject, JSONObject result)
			throws JSONException {
		String recommentText = jsonObject.optString(UserInfo.RECOMMENT_TEXT);
		int noteCount = jsonObject.optInt(Note.NOTE_COUNT, ILLEGAL);
		int contentSize = jsonObject.optInt(Note.CONTENT_SIZE, ILLEGAL);
		if (!UiStaticMethod.isNullString(recommentText))
			result.put(UserInfo.RECOMMENT_TEXT, recommentText);
		if (noteCount != ILLEGAL)
			result.put(Note.NOTE_COUNT, noteCount);
		if (contentSize != ILLEGAL)
			result.put(Note.CONTENT_SIZE, contentSize);
	}

	/**
	 * 从JsonObjcet中得到一个UserInfo实体
	 * 
	 * @param jsonObject
	 *            待解析的JsonObject对象
	 * @return 可能为UserInfo，也可能为UserDetail
	 * @throws JSONException
	 *             解析异常
	 */
	private UserInfo getUser(JSONObject jsonObject) throws JSONException {
		UserInfo user;
		if (showButton) {
			user = new UserDetail();
			JSONObject object = jsonObject.optJSONObject(RELATION);
			if (object != null) {
				jsonObject.put(UserDetail.CURRENT_USER_FOLLOWING,
						object.getBoolean(RELATION_FOLLOWING));
				jsonObject.put(UserDetail.CURRENT_USER_FOLLOWED_BY,
						object.getBoolean(RELATION_FOLLOWED));
			}
		} else {
			user = new UserInfo();
		}
		user.parseJson(jsonObject);
		return user;
	}

	/**
	 * 为list中的用户添加对应的FollowModel
	 * 
	 * @param list
	 *            用户列表
	 */
	private void addFollowModelToNewUser(List<UserInfo> list) {
		if (isShowButton()) {
			UserFollower userFollower;
			for (UserInfo userInfo : list) {
				userFollower = new UserFollower(context,
						((UserDetail) userInfo).isFollowedByCurrentUser(),
						((UserDetail) userInfo).isFollowingCurrentUser());
				userFollower.addObserver(observer);
				followModels.add(userFollower);
			}
		}
	}
}
