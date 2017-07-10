package com.jingdong.app.reader.timeline.actiivity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.FocusModel;
import com.jingdong.app.reader.entity.extra.UsersList;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.adapter.FocusAdapter;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.view.TopBarView;

public class RecommendActivity extends BaseActivityWithTopBar {

	private TopBarView topBarView = null;
	private ListView mListView;
	private static int perPageCount = 10;

	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private boolean inSearch = false;
	private FocusModel focusmodel;
	private List<UsersList> usersLists = new ArrayList<UsersList>();
	private FocusAdapter focusAdapter;
	private String ids;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.focus);
		topBarView = (TopBarView) findViewById(R.id.topbar);
		initTopbarView();
		initField();
		getMyWant();
		mListView = (ListView) findViewById(R.id.mlistview);
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
		focusAdapter = new FocusAdapter(RecommendActivity.this, usersLists);
		mListView.setAdapter(focusAdapter);
	}

	public void initTopbarView() {

		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, "取消", R.color.red_main);
		topBarView.setRightMenuOneVisiable(true, "一键关注", R.color.red_main,
				false);
		topBarView.setListener(this);
	}

	public void initField() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}

	private void getMyWant() {
		WebRequestHelper.get(URLText.Recommend_URL, RequestParamsPool
				.getRecommendUserParams(currentSearchPage + "", 30 + ""), true,
				new MyAsyncHttpResponseHandler(RecommendActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(RecommendActivity.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);

						Log.d("cj", "result=======>>" + result);
						focusmodel = GsonUtils.fromJson(result,
								FocusModel.class);

						if (focusmodel != null) {
							if (focusmodel.getUsers() != null) {
								currentSearchPage++;

								if (focusmodel.getUsers() != null
										&& focusmodel.getUsers().size() < perPageCount) {
									noMoreBookOnSearch = true;
								} else {
									noMoreBookOnSearch = false;
								}

								List<UsersList> all = new ArrayList<UsersList>();
								for (int i = 0; i < focusmodel.getUsers()
										.size(); i++) {
									UsersList userList = focusmodel.getUsers()
											.get(i);
									all.add(userList);
								}
								usersLists.addAll(all);
								focusAdapter.notifyDataSetChanged();
							}
						} else
							Toast.makeText(RecommendActivity.this,
									getString(R.string.network_connect_error),
									Toast.LENGTH_LONG).show();
						inLoadingMoreOnSearch = false;
					}
				});
	}

	@Override
	public void onLeftMenuClick() {
		finish();
	}

	@Override
	public void onRightMenuOneClick() {
		if (usersLists != null && usersLists.size()>0) {
			ids = usersLists.get(0).getId();
			for (int i = 0; i < usersLists.size(); i++) {
				ids += "," + usersLists.get(i).getId();
				usersLists.get(i).getRelation_with_current_user()
						.setFollowing(true);
			}
			focusAdapter.notifyDataSetChanged();
			Follow(ids);
		}
	}

	private void Follow(String id) {
		WebRequestHelper.post(URLText.FollowALL_URL,
				RequestParamsPool.getFollowSomeParams(id),
				new MyAsyncHttpResponseHandler(RecommendActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(RecommendActivity.this,
								R.string.network_connect_error,
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);
						Log.d("cj", "result=======>>" + result);
						try {
							JSONObject jsonObject = new JSONObject(result);
							String code = jsonObject.optString("code");
							String message = jsonObject.optString("message");
							if (jsonObject != null) {
								if (code.equals("0")) {
									Toast.makeText(RecommendActivity.this,
											message, 1).show();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}
}
