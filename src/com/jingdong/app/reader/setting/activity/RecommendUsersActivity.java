package com.jingdong.app.reader.setting.activity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.FollowedUserReadingActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.common.MZReadCommonFragmentActivity;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.activity.UserListActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.url.QueryUrlGetter;
import com.jingdong.app.reader.oauth.SinaAuth;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.setting.model.RecommendModel;
import com.jingdong.app.reader.timeline.actiivity.TweetListActivity;
import com.jingdong.app.reader.timeline.fragment.UserListCallBack;
import com.jingdong.app.reader.timeline.fragment.UserListFragment;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.UserInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;


public class RecommendUsersActivity extends MZReadCommonFragmentActivity implements UserListCallBack, OnClickListener, Observer {
	private class UrlGetter implements QueryUrlGetter {

		@Override
		public String getInitPageUrl(String baseUrl) {
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(RecommendUsersActivity.this));
			String url = URLBuilder.addParameter(URLText.recommandUsers, paramMap);
			return url;
		}

		@Override
		public String getNextPageUrl(String baseUrl, String id) {
			throw new UnsupportedOperationException("只有一页，无法翻页");
		}

		@Override
		public String getQueryInitPageUrl(String query) {
			throw new UnsupportedOperationException("不支持搜索滴");
		}

		@Override
		public String getQueryNextPageUrl(String query) {
			throw new UnsupportedOperationException("不支持搜索滴");
		}

	}

	private class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle response) {
			Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(response);
			if (accessToken.isSessionValid()) {
				LocalUserSetting.saveSinaAccessToken(RecommendUsersActivity.this, accessToken);
				long expiresMillsec = accessToken.getExpiresTime();
				String[] sina = new String[] { accessToken.getToken(), accessToken.getUid(), String.valueOf(expiresMillsec / 1000) };
				executor.execute(new Task(RecommendModel.SINA, sina));
			} else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = response.getString("code");
                String message = getString(R.string.weibosdk_toast_auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(RecommendUsersActivity.this, message, Toast.LENGTH_LONG).show();
            }
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(RecommendUsersActivity.this, 
                    "Sina Weibo Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		@Override
		public void onCancel() {
			Toast.makeText(RecommendUsersActivity.this, 
                    R.string.weibosdk_toast_auth_canceled, Toast.LENGTH_LONG).show();
		}

	}

	private class Task implements Runnable {
		private int type;
		private String[] token;

		public Task(int type, String[] token) {
			this.type = type;
			this.token = token;
		}

		@Override
		public void run() {
			switch (type) {
			case RecommendModel.SINA:
				model.getUserJson(token);
				break;
			}
		}
	}

	private static class MyHandler extends Handler {
		private WeakReference<RecommendUsersActivity> reference;

		public MyHandler(RecommendUsersActivity activity) {
			reference = new WeakReference<RecommendUsersActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			RecommendUsersActivity activity = reference.get();
			if (activity != null) {
				switch (msg.what) {
				case RecommendModel.SINA:
					if (msg.arg1 == RecommendModel.SUCCESS_INT) {
						Intent intent = new Intent(activity, UserListActivity.class);
						intent.putExtra(UserListFragment.SHOW_FOLLOW_ALL, true);
						intent.putExtra(UserListFragment.SHOW_RIGHT_BUTTON, true);
						intent.putExtra(UserListFragment.EMPTY_INIT_PAGE, false);
						intent.putExtra(UserListFragment.SHOW_FRIENDS_NUMBER, true);
						intent.putExtra(UserListFragment.USER_JSON_STRING, (String) msg.obj);
						intent.putExtra(LOGIN_REGISTER, activity.logRegister);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						activity.startActivityForResult(intent, GlobalVarable.REQUEST_CODE_FOLLOWED_USER_BOOK);
					} else {
						LocalUserSetting.clearSina(activity);
						Toast.makeText(activity, R.string.sina_error, Toast.LENGTH_SHORT).show();
					}
					break;
				}
			}
		}
	}

	public final static String arrayKey = "users";
	public final static String LOGIN_REGISTER = "logRegister";
	private final static String itemKey = "user";
	private boolean logRegister;
	private Executor executor;
	private View sinaWeibo;
	private Handler handler;
	private SinaAuth sinaAuth;
	private RecommendModel model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_friends);
		getActionBar().setTitle(R.string.find_friends);
		if (savedInstanceState == null)
			initFragment();
		logRegister = getIntent().getBooleanExtra(LOGIN_REGISTER, false);
		executor = NotificationService.getExecutorService();
		handler = new MyHandler(this);
		model = new RecommendModel(this);
		model.addObserver(this);
		sinaWeibo = findViewById(R.id.sina_weibo);
		sinaWeibo.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		model.deleteObserver(this);
		handler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (logRegister) {
			MenuInflater inflater = new MenuInflater(this);
			inflater.inflate(R.menu.next, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.next:
			Intent intent = new Intent(RecommendUsersActivity.this, FollowedUserReadingActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(intent, GlobalVarable.REQUEST_CODE_FOLLOWED_USER_BOOK);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 32973 && sinaAuth != null) {
			sinaAuth.authCallBack(requestCode, resultCode, data);
		} else if (requestCode == GlobalVarable.REQUEST_CODE_FOLLOWED_USER_BOOK && resultCode == RESULT_OK) {
			finish();
		}
	}
	
	@Override
	public void onItemClick(UserInfo userInfo) {
		Intent intent = new Intent(this, UserActivity.class);
		intent.putExtra(UserFragment.USER_ID, userInfo.getId());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public void onItemClick(Document document) {
	}
	
	@Override
	public QueryUrlGetter getUsersUrlGetter() {
		return new UrlGetter();
	}

	@Override
	public void onClick(View v) {
		String[] sina = model.getLocalSinaToken();
		if (sina == null) {
			sinaAuth = new SinaAuth(this, new AuthDialogListener());
			sinaAuth.auth();
		} else {
			executor.execute(new Task(RecommendModel.SINA, sina));
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		Message message = (Message) data;
		handler.sendMessage(message);
	}

	private UserListFragment initFragment() {
		UserListFragment fragment = new UserListFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(UserListFragment.SHOW_RIGHT_BUTTON, true);
		bundle.putBoolean(UserListFragment.EMPTY_INIT_PAGE, false);
		bundle.putBoolean(UserListFragment.SHOW_FOLLOW_ALL, true);
		bundle.putString(UserListFragment.ITEM_KEY, itemKey);
		bundle.putString(TweetListActivity.JsonNameKey, arrayKey);
		fragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
		return fragment;
	}

}
