package com.jingdong.app.reader.bookstore.search;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.bookstore.search.adapter.SearchPageAdapter;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.url.PagedBasedUrlGetter;
import com.jingdong.app.reader.net.url.QueryUrlGetter;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.timeline.fragment.UserListCallBack;
import com.jingdong.app.reader.timeline.fragment.UserListFragment;
import com.jingdong.app.reader.ui.ViewPagerTabBarHelper;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.view.CustomProgreeDialog;
import com.jingdong.app.reader.view.SearchTopBarView;
import com.jingdong.app.reader.view.SearchTopBarView.TopBarViewListener;

public class SearchActivity extends FragmentActivity implements
		UserListCallBack, TopBarViewListener {

	// 动态搜索接口
	public interface SearchTimeLineInterface {

		public void onSearchTimeline(String query, boolean isSave);

	}

	public SearchTimeLineInterface listener;

	public SearchTimeLineInterface getListener() {
		return listener;
	}

	public void setListener(SearchTimeLineInterface listener) {
		this.listener = listener;
	}

	// 用户搜索
	public interface SearchUserInterface {

		public void onSearchUser(String query, boolean isSave);

	}

	public SearchUserInterface userlistener;

	public SearchUserInterface getuserListener() {
		return userlistener;
	}

	public void setuserListener(SearchUserInterface userlistener) {
		this.userlistener = userlistener;
	}

	private static class UsersUrl implements QueryUrlGetter {
		private int queryPage = PagedBasedUrlGetter.FIRST_PAGE;

		@Override
		public String getInitPageUrl(String baseUrl) {
			throw new UnsupportedOperationException("搜索列表初始化的时候为空，不需要内容");
		}

		@Override
		public String getNextPageUrl(String baseUrl, String id) {
			throw new UnsupportedOperationException("搜索列表初始化的时候为空，不需要内容");
		}

		@Override
		public String getQueryInitPageUrl(String query) {
			return PagedBasedUrlGetter.getQueryUrl(URLText.searchUsersUrl,
					query, PagedBasedUrlGetter.FIRST_PAGE, true);
		}

		@Override
		public String getQueryNextPageUrl(String query) {
			queryPage++;
			return PagedBasedUrlGetter.getQueryUrl(URLText.searchUsersUrl,
					query, queryPage, true);
		}
	}

	private CharSequence query;
	private FragmentPagerAdapter adapter;
	private ViewPagerTabBarHelper tabbarHelper;
	private ViewPager viewPager;
	private SearchTopBarView topBarView = null;
	private ProgressDialog mypDialog;
	public static EditText edittext_serach;// 搜索关键字输入框
	private String[] tabNames;
	private int selected = 0;
	public static boolean isHistoryKey = false;
	public static boolean isHistoryListShow = false;

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {

			if (intent.getAction().equals("com.mzread.action.dialog.show")) {
				if (mypDialog != null)
					mypDialog.show();
			}
			if (intent.getAction().equals("com.mzread.action.dialog.canceled")) {
				if (mypDialog.isShowing())
					mypDialog.cancel();
			}

		};

	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		tabNames = getTabNames();
		topBarView = (SearchTopBarView) findViewById(R.id.topbar);
		initTopbarView();
		viewPager = (ViewPager) findViewById(R.id.pager);
		adapter = new SearchPageAdapter(getSupportFragmentManager(), tabNames);
		viewPager.setAdapter(adapter);
		tabbarHelper = new ViewPagerTabBarHelper(this, tabNames.length,
				viewPager);
		mypDialog = CustomProgreeDialog.instace(SearchActivity.this);
		edittext_serach = (EditText) findViewById(R.id.edittext_serach);
		edittext_serach.setCursorVisible(true);
		edittext_serach.setFocusable(true);
		edittext_serach.requestFocus();
		edittext_serach.setHint("搜索用户昵称");
		viewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@SuppressWarnings("static-access")
					@Override
					public void onPageSelected(int position) {
						tabbarHelper.setSelectItem(position);
						selected = position;
						isHistoryKey = false;
						isHistoryListShow = false;
						if (TextUtils.isEmpty(edittext_serach.getText().toString())) {
							if (selected == 0) {
								if (((UserListFragment) adapter.getItem(0))
										.getHistoryList() != null)
									((UserListFragment) adapter.getItem(0)).historyList
											.clear();
								((UserListFragment) adapter.getItem(0)).initLists();
								isHistoryListShow = true;
							} else {
								if (((TimelineFragment) adapter.getItem(1))
										.getHistoryList() != null)
									((TimelineFragment) adapter.getItem(1)).historyList
											.clear();
								isHistoryListShow = true;
								((TimelineFragment) adapter.getItem(1)).initLists();
							}
						}
						if (selected == 0) {
							edittext_serach.setHint("搜索用户昵称");
							if (userlistener != null) {
								userlistener.onSearchUser(edittext_serach.getText()
										.toString(), true);
							}
						} else {
							edittext_serach.setHint("搜索社区动态");
							if (listener != null) {
								listener.onSearchTimeline(edittext_serach.getText()
										.toString(), true);
							}
						}
					}
				});
		initButtons(tabNames);
		tabbarHelper.setSelectItem(0);

		edittext_serach.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == event.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					closeKeyBroad();
					if (selected == 0) {
						if (userlistener != null) {
							userlistener.onSearchUser(edittext_serach.getText()
									.toString(), true);
						}
					} else {
						if (listener != null) {
							listener.onSearchTimeline(edittext_serach.getText()
									.toString(), true);
						}
					}
					return true;
				}
				return false;
			}
		});

		edittext_serach.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@SuppressWarnings("static-access")
			@Override
			public void afterTextChanged(Editable s) {
				if (TextUtils.isEmpty(edittext_serach.getText().toString())) {
					if (selected == 0) {
						if (((UserListFragment) adapter.getItem(0))
								.getHistoryList() != null)
							((UserListFragment) adapter.getItem(0)).historyList
									.clear();
						isHistoryListShow = true;
						((UserListFragment) adapter.getItem(0)).initLists();
					} else {
						if (((TimelineFragment) adapter.getItem(1))
								.getHistoryList() != null)
							((TimelineFragment) adapter.getItem(1)).historyList
									.clear();
						isHistoryListShow = true;
						((TimelineFragment) adapter.getItem(1)).initLists();
					}
				}
			}
		});
	}

	private void initTopbarView() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setRightMenuVisiable(false);
		topBarView.setListener(this);
		topBarView.updateTopBarView(true);
	}

	private void initButtons(String[] tabNames) {
		Button[] buttons = tabbarHelper.getTabButtons();
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setText(tabNames[i]);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.mzread.action.dialog.canceled");
		filter.addAction("com.mzread.action.dialog.show");
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
				filter);
		// registerReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
		// unregisterReceiver();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onItemClick(UserInfo userInfo) {
		Intent intent = new Intent(this, UserActivity.class);
		intent.putExtra(UserFragment.USER_ID, userInfo.getId());
		startActivity(intent);
	}

	@Override
	public void onItemClick(Document document) {

	}

	@Override
	public QueryUrlGetter getUsersUrlGetter() {
		return new UsersUrl();
	}

	private String[] getTabNames() {
		String[] tabNames = new String[2];
		Resources resources = getResources();
		tabNames[0] = resources.getString(R.string.user);
		tabNames[1] = resources.getString(R.string.timeline);
		return tabNames;
	}

	@Override
	public void onLeftMenuClick() {
		finish();
	}

	@Override
	public void onRightMenu_leftClick() {

	}

	@Override
	public void onRightMenu_rightClick() {

	}

	@Override
	public void onCenterMenuItemClick(int position) {

	}

	public void setEditText(String str) {
		edittext_serach.setText(str);
		// 切换后将EditText光标置于末尾
		edittext_serach.postInvalidate();
		CharSequence charSequence = edittext_serach.getText();
		if (charSequence instanceof Spannable) {
			Spannable spanText = (Spannable) charSequence;
			Selection.setSelection(spanText, charSequence.length());
		}
	}

	public static EditText getEdittext_serach() {
		return edittext_serach;
	}

	public static void setEdittext_serach(EditText edittext_serach) {
		SearchActivity.edittext_serach = edittext_serach;
	}

	private void closeKeyBroad(){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();
		if (isOpen) {
			imm.hideSoftInputFromWindow(edittext_serach.getWindowToken(), 0); // 强制隐藏键盘
		}
	}
}
