package com.jingdong.app.reader.timeline.actiivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.FocusModel;
import com.jingdong.app.reader.entity.extra.UsersList;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.xlistview.XListView;
import com.jingdong.app.reader.view.xlistview.XListView.IXListViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TimelineSearchPeopleActivity extends BaseActivityWithTopBar {
	public final static String PAGE = "page";
	public final static String USER_NAME = "userName";
	public final static String USER_PARCELABLE = "user";
	public final static int CLICK_USER_NAME = 5000;

	private View headerView = null;
	private XListView followListView = null;

	private int followPages = 1;
	private boolean noMoreFollowed = false;
	private int defaultCount = 10;

	private List<UsersList> usersLists = new ArrayList<UsersList>();
	private FollowerAdapter adapter = null;
	private LinearLayout mentionTitle = null;

	private List<UsersList> recentMention = new ArrayList<UsersList>();
	private LinearLayout searchImage;
	private EditText serarchPeople;
	private XListView searchResult;
	
	private SearchAdapter searchAdapter=null;

	private List<UsersList> searchUsers = new ArrayList<UsersList>();
	
	private LinearLayout empty = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_timeline);

		adapter = new FollowerAdapter();

		searchImage = (LinearLayout) findViewById(R.id.searchImage);
		serarchPeople = (EditText) findViewById(R.id.search_book_name);
		headerView = LayoutInflater.from(TimelineSearchPeopleActivity.this)
				.inflate(R.layout.activity_me_timeline_header, null);
		empty =(LinearLayout) headerView.findViewById(R.id.empty);
		
		
		mentionTitle = (LinearLayout) headerView
				.findViewById(R.id.mention_title);

		LinearLayout mentionContainer = (LinearLayout) headerView
				.findViewById(R.id.recently_mention_container);

		recentMention = LocalUserSetting
				.getLastedMentionPeople(TimelineSearchPeopleActivity.this);

		if (recentMention == null || recentMention.size() == 0)
			mentionTitle.setVisibility(View.GONE);
		else {
			mentionTitle.setVisibility(View.VISIBLE);

			for (int i = 0; i < recentMention.size(); i++) {

				View convertView = LayoutInflater.from(
						TimelineSearchPeopleActivity.this).inflate(
						R.layout.focusitem, null);

				RoundNetworkImageView avatar_label = com.jingdong.app.reader.util.ViewHolder
						.get(convertView, R.id.thumb_nail);
				TextView timeline_user_name = com.jingdong.app.reader.util.ViewHolder
						.get(convertView, R.id.timeline_user_name);
				TextView timeline_user_summary = com.jingdong.app.reader.util.ViewHolder
						.get(convertView, R.id.timeline_user_summary);
				
				
				ImageLoader.getInstance().displayImage(
						recentMention.get(i).getAvatar(), avatar_label,
						GlobalVarable.getDefaultAvatarDisplayOptions(false));
				timeline_user_name.setText(recentMention.get(i).getName());
				timeline_user_summary.setText(recentMention.get(i).summary);

				final int position = i;
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.putExtra(USER_NAME, recentMention.get(position)
								.getName());

						// intent.putExtra(USER_PARCELABLE, userInfo);
						intent.putExtra("userid", recentMention.get(position)
								.getId());
						setResult(CLICK_USER_NAME, intent);
						finish();

					}
				});

				mentionContainer.addView(convertView);
			}

		}
		searchAdapter=new SearchAdapter();
		searchResult = (XListView) findViewById(R.id.search_result);
		searchResult.setPullLoadEnable(false);
		searchResult.setPullRefreshEnable(false);
		searchResult.setDivider(null);
		searchResult.setDividerHeight(0);
		searchResult.setAdapter(searchAdapter);
		searchResult.setXListViewListener(new SearchListviewListener());
		
		searchResult.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				List<UsersList> list = LocalUserSetting
						.getLastedMentionPeople(TimelineSearchPeopleActivity.this);

				if (list == null)
					list = new ArrayList<UsersList>();
				else {
					
					boolean isExist=false;
					for (int i = 0; i < list.size(); i++) {
						if(list.get(i).getId().equals(searchUsers.get(position - 1).getId())){
							isExist=true;
							break;
						}
					}
					if(!isExist)
						list.add(searchUsers.get(position - 1));
					
					Collections.reverse(list);

					if (list.size() > 5)
						list = list.subList(0, 5);
				}
				MentionPeopleModel mentionPeopleModel = new MentionPeopleModel();
				mentionPeopleModel.list = list;
				LocalUserSetting.saveLastedMentionPeople(
						TimelineSearchPeopleActivity.this, mentionPeopleModel);

				Intent intent = new Intent();
				intent.putExtra(USER_NAME, searchUsers.get(position -1)
						.name);
				intent.putExtra("userid", searchUsers.get(position -1).getId());
				
				// intent.putExtra(USER_PARCELABLE, userInfo);
				setResult(CLICK_USER_NAME, intent);
				finish();
			}
		});

		followListView = (XListView) findViewById(R.id.follow_listview);

		serarchPeople.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					searchResult.setVisibility(View.VISIBLE);
					searchImage.setVisibility(View.GONE);
					followListView.setVisibility(View.GONE);
				}

			}
		});

		serarchPeople.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {

				// /

				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					((InputMethodManager) serarchPeople.getContext()
							.getSystemService(Context.INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(
									TimelineSearchPeopleActivity.this
											.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);

					String key = serarchPeople.getText().toString();

					if (!TextUtils.isEmpty(key)) {

						searchUsers.clear();
						noSearchResult=false;
						currentSearchPage=1;
						searchAdapter.notifyDataSetChanged();
						searchPeople(key);
					}

					return true;
				}
				return false;
				// //
			}
		});

		followListView.setPullLoadEnable(true);
		followListView.setPullRefreshEnable(false);
		followListView.setXListViewListener(new FollowListviewListener());

		followListView.addHeaderView(headerView, null, false);
		followListView.setAdapter(adapter);
		followListView.setDividerHeight(0);
		followListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				List<UsersList> list = LocalUserSetting
						.getLastedMentionPeople(TimelineSearchPeopleActivity.this);

				if (list == null)
					list = new ArrayList<UsersList>();

				boolean isExist=false;
				for (int i = 0; i < list.size(); i++) {
					if(list.get(i).getId().equals(usersLists.get(position - 2).getId())){
						isExist=true;
						break;
					}
				}
				if(!isExist)
					list.add(usersLists.get(position - 2));
				Collections.reverse(list);

				if (list.size() > 5)
					list = list.subList(0, 5);
				
				MentionPeopleModel mentionPeopleModel = new MentionPeopleModel();
				mentionPeopleModel.list = list;
				LocalUserSetting.saveLastedMentionPeople(
						TimelineSearchPeopleActivity.this, mentionPeopleModel);

				Intent intent = new Intent();
				intent.putExtra(USER_NAME, usersLists.get(position - 2)
						.getName());
				intent.putExtra("userid",  usersLists.get(position - 2)
						.getId());
				// intent.putExtra(USER_PARCELABLE, userInfo);
				setResult(CLICK_USER_NAME, intent);
				finish();

			}
		});
		getFollowData();

	}

	private void onLoadComplete() {
		
		if(usersLists==null||usersLists.size()==0)
		{
			empty.setVisibility(View.VISIBLE);
			followListView.setPullLoadEnable(false);
		}
		else {
			empty.setVisibility(View.GONE);
			followListView.setPullLoadEnable(true);
		}
		
		
		followListView.stopRefresh();
		followListView.stopLoadMore();
		
		searchResult.stopLoadMore();
		searchResult.stopRefresh();
	}

	public void getFollowData() {

		WebRequestHelper.get(URLText.Focus_URL, RequestParamsPool
				.getFocusParams(followPages + "", defaultCount + ""),
				new MyAsyncHttpResponseHandler(
						TimelineSearchPeopleActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						Toast.makeText(TimelineSearchPeopleActivity.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
						onLoadComplete();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// TODO Auto-generated method stub

						String result = new String(responseBody);

						MZLog.d("wangguodong", result);

						FocusModel focusmodel = GsonUtils.fromJson(result,
								FocusModel.class);

						if (focusmodel != null && null != focusmodel.getUsers()) {

							if (focusmodel.getUsers().size() < defaultCount)
								noMoreFollowed = true;
							else {
								noMoreFollowed = false;
							}

							List<UsersList> all = new ArrayList<UsersList>();
							for (int i = 0; i < focusmodel.getUsers().size(); i++) {
								UsersList userList = focusmodel.getUsers().get(
										i);
								all.add(userList);
							}
							usersLists.addAll(all);
							adapter.notifyDataSetChanged();
						}

						onLoadComplete();
					}
				});

	}

	class FollowerAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return usersLists == null ? 0 : usersLists.size();
		}

		@Override
		public Object getItem(int position) {
			return usersLists.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			if (convertView == null) {
				convertView = LayoutInflater.from(
						TimelineSearchPeopleActivity.this).inflate(
						R.layout.focusitem, parent, false);
			}

			RelativeLayout relativeLayout = com.jingdong.app.reader.util.ViewHolder
					.get(convertView, R.id.relativeLayout);

			RoundNetworkImageView avatar_label = com.jingdong.app.reader.util.ViewHolder
					.get(convertView, R.id.thumb_nail);
			TextView timeline_user_name = com.jingdong.app.reader.util.ViewHolder
					.get(convertView, R.id.timeline_user_name);
			TextView timeline_user_summary = com.jingdong.app.reader.util.ViewHolder
					.get(convertView, R.id.timeline_user_summary);
			ImageView imagebutton = com.jingdong.app.reader.util.ViewHolder
					.get(convertView, R.id.imagebutton);

			ImageLoader.getInstance().displayImage(
					usersLists.get(position).getAvatar(), avatar_label,
					GlobalVarable.getDefaultAvatarDisplayOptions(false));
			timeline_user_name.setText(usersLists.get(position).getName());
			timeline_user_summary.setText(usersLists.get(position).summary);
			imagebutton.setVisibility(View.GONE);

			return convertView;

		}
	}
	
	class SearchAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return searchUsers == null ? 0 : searchUsers.size();
		}

		@Override
		public Object getItem(int position) {
			return searchUsers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			if (convertView == null) {
				convertView = LayoutInflater.from(
						TimelineSearchPeopleActivity.this).inflate(
						R.layout.focusitem, parent, false);
			}

			RelativeLayout relativeLayout = com.jingdong.app.reader.util.ViewHolder
					.get(convertView, R.id.relativeLayout);

			RoundNetworkImageView avatar_label = com.jingdong.app.reader.util.ViewHolder
					.get(convertView, R.id.thumb_nail);
			TextView timeline_user_name = com.jingdong.app.reader.util.ViewHolder
					.get(convertView, R.id.timeline_user_name);
			TextView timeline_user_summary = com.jingdong.app.reader.util.ViewHolder
					.get(convertView, R.id.timeline_user_summary);
			ImageView imagebutton = com.jingdong.app.reader.util.ViewHolder
					.get(convertView, R.id.imagebutton);

			ImageLoader.getInstance().displayImage(
					searchUsers.get(position).avatar, avatar_label,
					GlobalVarable.getDefaultAvatarDisplayOptions(false));
			timeline_user_name.setText(searchUsers.get(position).name);
			timeline_user_summary.setText(searchUsers.get(position).summary);
			imagebutton.setVisibility(View.GONE);

			return convertView;

		}
	}
	

	class FollowListviewListener implements IXListViewListener {

		@Override
		public void onRefresh() {

		}

		@Override
		public void onLoadMore() {

			if (!noMoreFollowed) {
				followPages++;
				getFollowData();
			}

			else {
				onLoadComplete();
			}

		}

	}
	
	private boolean noSearchResult =false;
	private int currentSearchPage =1;
	
	
	class SearchListviewListener implements IXListViewListener {

		@Override
		public void onRefresh() {

		}

		@Override
		public void onLoadMore() {

			if (!noSearchResult) {
				currentSearchPage++;
				searchPeople(serarchPeople.getText().toString());
			}

			else {
				onLoadComplete();
			}

		}

	}
	
	public void searchPeople(String nickname) {

		WebRequestHelper.get(URLText.searchPeople, RequestParamsPool
				.searchPeopleParams(currentSearchPage, defaultCount, nickname), true,
				new MyAsyncHttpResponseHandler(
						TimelineSearchPeopleActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(TimelineSearchPeopleActivity.this,
								"请求数据出错啦，请重试!", Toast.LENGTH_LONG).show();
						onLoadComplete();
						super.onFailure(arg0, arg1, arg2, arg3);
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						MZLog.d("wangguodong", new String(responseBody));

						try {
							JSONArray object = new JSONArray(new String(
									responseBody));
							
							if(object==null||object.length()<defaultCount)
							{
								noSearchResult=true;
							}
								

							if (object != null && object.length() > 0) {
								for (int i = 0; i < object.length(); i++) {

									UsersList user = new UsersList();
									JSONObject obj = object.optJSONObject(i);
									user.avatar = obj.optString("avatar");
									user.cover = obj.optString("cover");
									user.id = obj.optString("id");
									user.jd_user_name = obj
											.optString("jd_user_name");
									user.name = obj.optString("name");
									user.role = obj.optInt("role");
									user.sex = obj.optString("sex");
									user.summary = obj.optString("summary");
									searchUsers.add(user);
								}
								searchAdapter.notifyDataSetChanged();
							}

						} catch (Exception e) {
							Toast.makeText(TimelineSearchPeopleActivity.this,
									"请求数据出错啦，请重试!", Toast.LENGTH_LONG).show();
						}
						searchResult.setPullLoadEnable(true);
						onLoadComplete();
						
					}
				});

	}

	public class MentionPeopleModel {
		public List<UsersList> list;
	}


}
