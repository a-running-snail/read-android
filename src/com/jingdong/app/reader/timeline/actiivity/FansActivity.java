package com.jingdong.app.reader.timeline.actiivity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.FocusModel;
import com.jingdong.app.reader.entity.extra.UsersList;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.adapter.FocusAdapter;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.Log;

public class FansActivity extends BaseActivityWithTopBar {

	private ListView mListView;
	private static int perPageCount = 10;

	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private FocusModel focusmodel;
	private List<UsersList> usersLists = new ArrayList<UsersList>();
	private FocusAdapter focusAdapter;
	private TextView text;
	private String user_id;
	private RelativeLayout empty_view;
	private Button empty;
	private String jd_username;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.focus);
		Intent intent = getIntent();
		user_id = intent.getStringExtra("user_id");
		jd_username = intent.getStringExtra(UserActivity.JD_USER_NAME);

		empty_view = (RelativeLayout) findViewById(R.id.empty_view);
		text = (TextView) findViewById(R.id.text);
		empty = (Button) findViewById(R.id.empty);
		mListView = (ListView) findViewById(R.id.mlistview);
		Notification.getInstance().setReadFollowersCount(FansActivity.this);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount == 0)
					return;

				if (firstVisibleItem + visibleItemCount == totalItemCount
						&& !noMoreBookOnSearch) {
					if (!inLoadingMoreOnSearch) {
						inLoadingMoreOnSearch = true;
						getMyWant();
					}
				}
			}

		});
		focusAdapter = new FocusAdapter(FansActivity.this, usersLists);
		mListView.setAdapter(focusAdapter);
		// mListView.setEmptyView(empty_view);
		if (LoginUser.getpin().equals(jd_username)) {
			text.setText("暂时还没有粉丝哦");
		} else {
			text.setText("他还没有粉丝");
		}

		empty.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(arg0.getContext(),
						LauncherActivity.class);
				intent.putExtra("TAB_INDEX", 2);
				startActivity(intent);
			}
		});
	}

	public void initField() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (usersLists != null) {
			usersLists.clear();
		}
		initField();
		getMyWant();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_fans));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_fans));
	}

	private void getMyWant() {
		WebRequestHelper.get(
				URLText.Fans_URL,
				RequestParamsPool.getFocusParams(user_id, currentSearchPage
						+ "", perSearchCount + ""),
				new MyAsyncHttpResponseHandler(FansActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						Toast.makeText(FansActivity.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// TODO Auto-generated method stub

						String result = new String(responseBody);

						Log.d("cj", "result========>>>>>>" + result);
						focusmodel = GsonUtils.fromJson(result,
								FocusModel.class);

						if (focusmodel.getUsers() != null) {
							currentSearchPage++;
							if (focusmodel.getUsers() != null
									&& focusmodel.getUsers().size() < perPageCount) {
								noMoreBookOnSearch = true;
							} else {
								noMoreBookOnSearch = false;
							}

							List<UsersList> all = new ArrayList<UsersList>();
							for (int i = 0; i < focusmodel.getUsers().size(); i++) {
								UsersList userList = focusmodel.getUsers().get(i);
								all.add(userList);
							}
							usersLists.addAll(all);
							focusAdapter.notifyDataSetChanged();
						} else if (focusmodel.getUsers() == null && currentSearchPage == 1) {
							mListView.setVisibility(View.GONE);
							empty_view.setVisibility(View.VISIBLE);
							return;
						}
						inLoadingMoreOnSearch = false;
					}
				});
	}

}
