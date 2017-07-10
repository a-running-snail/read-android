package com.jingdong.app.reader.me.activity;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.common.MZReadCommonFragmentActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.net.url.PagedBasedUrlGetter;
import com.jingdong.app.reader.net.url.QueryUrlGetter;
import com.jingdong.app.reader.setting.activity.RecommendUsersActivity;
import com.jingdong.app.reader.timeline.actiivity.TweetListActivity;
import com.jingdong.app.reader.timeline.fragment.UserListCallBack;
import com.jingdong.app.reader.timeline.fragment.UserListFragment;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.R;

public class UserListActivity extends MZReadCommonFragmentActivity implements UserListCallBack {
	private class FollowingUrlGetter implements QueryUrlGetter {
		private final PagedBasedUrlGetter getter;

		public FollowingUrlGetter(String userId, Context context) {
			getter = new PagedBasedUrlGetter(userId, URLText.followingListUrl, context);
		}

		@Override
		public String getInitPageUrl(String baseUrl) {
			return getter.getInitPageUrl(null);
		}

		@Override
		public String getNextPageUrl(String baseUrl, String id) {
			return getter.getNextPageUrl(null, null);
		}

		@Override
		public String getQueryInitPageUrl(String query) {
			throw new UnsupportedOperationException("关注列表不具有查找功能");
		}

		@Override
		public String getQueryNextPageUrl(String query) {
			throw new UnsupportedOperationException("关注列表不具有查找功能");
		}

	}

	private class FollowerUrlGetter implements QueryUrlGetter {
		private final PagedBasedUrlGetter getter;

		public FollowerUrlGetter(String userId, Context context) {
			getter = new PagedBasedUrlGetter(userId, URLText.followerListUrl, context);
		}

		@Override
		public String getInitPageUrl(String baseUrl) {
			return getter.getInitPageUrl(null);
		}

		@Override
		public String getNextPageUrl(String baseUrl, String id) {
			return getter.getNextPageUrl(null, null);
		}

		@Override
		public String getQueryInitPageUrl(String query) {
			throw new UnsupportedOperationException("粉丝列表不具有查找功能");
		}

		@Override
		public String getQueryNextPageUrl(String query) {
			throw new UnsupportedOperationException("粉丝列表不具有查找功能");
		}

	}

	private class ReadUserList implements QueryUrlGetter {
		private int followingPage = PagedBasedUrlGetter.FIRST_PAGE;

		@Override
		public String getInitPageUrl(String baseUrl) {
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put(WebRequestHelper.AUTH_TOKEN, LocalUserSetting.getToken(UserListActivity.this));
			String url = URLBuilder.addParameter(getIntent().getStringExtra(TweetListActivity.RequestUrlKey), paramMap);
			return url;
		}

		@Override
		public String getNextPageUrl(String baseUrl, String id) {
			Map<String, String> paramMap = new HashMap<String, String>();
			followingPage++;
			paramMap.put(getIntent().getStringExtra(TweetListActivity.NextPageKey), Integer.toString(followingPage));
			paramMap.put(WebRequestHelper.AUTH_TOKEN, LocalUserSetting.getToken(UserListActivity.this));
			String url = URLBuilder.addParameter(getIntent().getStringExtra(TweetListActivity.RequestUrlKey), paramMap);
			return url;
		}

		@Override
		public String getQueryInitPageUrl(String query) {
			throw new UnsupportedOperationException("读者列表不具有查找功能");
		}

		@Override
		public String getQueryNextPageUrl(String query) {
			throw new UnsupportedOperationException("读者不具有查找功能");
		}

	}

	private class ThirdPartyFriends implements QueryUrlGetter {

		@Override
		public String getInitPageUrl(String baseUrl) {
			throw new UnsupportedOperationException("第三方用户列表，初始内容在intent中已经传过来了");
		}

		@Override
		public String getNextPageUrl(String baseUrl, String id) {
			throw new UnsupportedOperationException("第三方用户列表，初始内容在intent中已经传过来了");
		}

		@Override
		public String getQueryInitPageUrl(String query) {
			throw new UnsupportedOperationException("第三方用户列表，初始内容在intent中已经传过来了");
		}

		@Override
		public String getQueryNextPageUrl(String query) {
			throw new UnsupportedOperationException("第三方用户列表，初始内容在intent中已经传过来了");
		}

	}

	public final static String IS_FOLLOWING_LIST = "follow";
	public final static String TITLE = "title";
	public final static String BOOK_ID = "bookId";
	public final static String SELECT_USER = "selectUser";
	public final static String DOCUMENT_ID = "documentId";
	private QueryUrlGetter urlGetter;
	private Intent result;
	private String title;
	private String jsonString;
	private String userId;
	private long bookId;
	private boolean note;
	private boolean selectUser;
	private boolean logRegister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_timeline);
		initField();
		getActionBar().setTitle(title);
		Bundle bundle = new Bundle();
		if (jsonString != null) {
			initThird(bundle);
		} else {
			initNormalList(bundle);
		}
		if (savedInstanceState == null)
			initFragment(bundle);
	}

	@Override
	public void onBackPressed() {
		if(getIntent().getStringExtra(TweetListActivity.RequestUrlKey) == null){
			if(!getIntent().getExtras().getBoolean(IS_FOLLOWING_LIST)){
				if(userId.equals(LoginUser.getpin()))
					setResult(RESULT_OK);
			}
		}
		super.onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (logRegister) {
			MenuInflater inflater = new MenuInflater(this);
			inflater.inflate(R.menu.finish, menu);
			MenuItem Item = menu.findItem(R.id.finish);
			View actionView = Item.getActionView();
			TextView view=(TextView) actionView.findViewById(R.id.finish_action);
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					setResult(RESULT_OK);
					finish();
				}
			});
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onItemClick(UserInfo userInfo) {
		Intent intent;
		if (selectUser) {
			result = new Intent();
			result.putExtra(UserFragment.USER_ID, userInfo.getId());
		} else if (note) {
			String url = UiStaticMethod.getReadingDataUrl(userInfo.getId(), bookId);
			String userName = userInfo.getName();
			intent = new Intent(this, ReadingDataActivity.class);
			intent.putExtra(ReadingDataActivity.READING_DATA_URL, url);
			intent.putExtra(UserFragment.USER_NAME, userName);
			startActivity(intent);
		} else {
			intent = new Intent(this, UserActivity.class);
			intent.putExtra(UserFragment.USER_ID, userInfo.getId());
			startActivity(intent);
		}
	}

	@Override
	public QueryUrlGetter getUsersUrlGetter() {
		return urlGetter;
	}

	@Override
	public void onItemClick(Document document) {
		if (selectUser) {
			result.putExtra(DOCUMENT_ID, document.getDocumentId());
			setResult(RESULT_OK, result);
			finish();
		}
	}

	private void initField() {
		String defaultName = getTitle().toString();
		Intent intent = getIntent();
		title = intent.getExtras().getString(UserListActivity.TITLE, defaultName);
		userId = intent.getStringExtra(UserFragment.USER_ID);
		bookId = intent.getIntExtra(BOOK_ID, 0);
		selectUser = intent.getBooleanExtra(SELECT_USER, false);
		note = intent.getBooleanExtra(UserListFragment.NOTE, false);
		logRegister = getIntent().getBooleanExtra(RecommendUsersActivity.LOGIN_REGISTER, false);
		jsonString = getIntent().getStringExtra(UserListFragment.USER_JSON_STRING);
	}

	/**
	 * @param bundle
	 */
	private void initNormalList(Bundle bundle) {
		if (getIntent().getStringExtra(TweetListActivity.RequestUrlKey) == null) {
			if (getIntent().getExtras().getBoolean(IS_FOLLOWING_LIST))
				urlGetter = new FollowingUrlGetter(userId, this);
			else{
				urlGetter = new FollowerUrlGetter(userId, this);
				if(userId==LocalUserSetting.getUserId(this))
					Notification.getInstance().setReadFollowersCount(this);
			}
		} else {
			urlGetter = new ReadUserList();
			bundle.putString(TweetListActivity.JsonNameKey, getIntent().getStringExtra(TweetListActivity.JsonNameKey));
			bundle.putString(UserListFragment.ITEM_KEY, getIntent().getStringExtra(UserListFragment.ITEM_KEY));
		}
	}

	/**
	 * @param bundle
	 */
	private void initThird(Bundle bundle) {
		urlGetter = new ThirdPartyFriends();
		bundle.putString(UserListFragment.USER_JSON_STRING, jsonString);
		bundle.putBoolean(UserListFragment.SHOW_FOLLOW_ALL,
				getIntent().getBooleanExtra(UserListFragment.SHOW_FOLLOW_ALL, false));
		bundle.putBoolean(UserListFragment.EMPTY_INIT_PAGE,
				getIntent().getBooleanExtra(UserListFragment.EMPTY_INIT_PAGE, false));
		bundle.putBoolean(UserListFragment.SHOW_FRIENDS_NUMBER,
				getIntent().getBooleanExtra(UserListFragment.SHOW_FRIENDS_NUMBER, false));
	}

	private void initFragment(Bundle bundle) {
		UserListFragment fragment = new UserListFragment();
		bundle.putBoolean(UserListFragment.SHOW_RIGHT_BUTTON,
				getIntent().getBooleanExtra(UserListFragment.SHOW_RIGHT_BUTTON, true));
		bundle.putBoolean(UserListFragment.NOTE, note);
		fragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().add(R.id.user_container, fragment).commit();
	}
}
