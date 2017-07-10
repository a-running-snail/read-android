package com.jingdong.app.reader.community;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.reflect.TypeToken;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.FocusModel;
import com.jingdong.app.reader.entity.extra.Relation_with_current_user;
import com.jingdong.app.reader.entity.extra.UsersList;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.RecommendActivity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.TopBarView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tendcloud.tenddata.ad;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SearchUserListActivity extends BaseActivityWithTopBar {

	private TopBarView topBarView = null;
	private ListView mListView;
	private static int perPageCount = 10;

	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private List<SearchUser> usersLists = new ArrayList<SearchUser>(); ;
	private UserAdapter adapter;
	private Context context;
	private String query;
	private View loading;
	private EmptyLayout emptyLayout;
	

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.community_search_userlist);
		topBarView = (TopBarView) findViewById(R.id.topbar);
		context=this;
		
		query=getIntent().getExtras().getString("query");
		
		initTopbarView();
		initField();
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
						searchUsers();
						loading.setVisibility(View.VISIBLE);
					}
				}
			}
		});
		LayoutInflater  inflater=LayoutInflater.from(context);
		loading = inflater.inflate(R.layout.list_cell_footer, null, false);
		mListView.addFooterView(UiStaticMethod.getFooterParent(context,
				loading));
		loading.setVisibility(View.GONE);
		
		adapter = new UserAdapter(context, usersLists);
		mListView.setAdapter(adapter);
		searchUsers();
		
		emptyLayout=(EmptyLayout) findViewById(R.id.error_layout);
		emptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
	}

	public void initTopbarView() {

		if (topBarView == null)
			return;
		topBarView.setTitle("相关用户");
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setListener(this);
	}

	public void initField() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}

	/**
	 * 搜索用户
	 * @param query
	 * @param currentpage
	 * @param pagecount
	 */
	public void searchUsers() {
				WebRequestHelper.getWithContext(context,URLText.searchPeople,
						RequestParamsPool
								.searchPeopleParams(currentSearchPage,perSearchCount, query), true,
						new MyAsyncHttpResponseHandler(context) {

							@SuppressWarnings("deprecation")
							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
							}

							@SuppressWarnings("deprecation")
							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String jsonString = new String(responseBody);
								
								List<SearchUser> searchUsersList = GsonUtils.fromJson(jsonString,new TypeToken<List<SearchUser>>(){});
								
								if (searchUsersList != null && searchUsersList.size() > 0) {
										currentSearchPage++;

										List<SearchUser> all = new ArrayList<SearchUser>();
										for (int i = 0; i < searchUsersList.size(); i++) {
											SearchUser user = searchUsersList.get(i);
											all.add(user);
										}
										usersLists.addAll(all);
										adapter.notifyDataSetChanged();
								} else{
									noMoreBookOnSearch=true;
									Toast.makeText(context,
											getString(R.string.no_more_data),
											Toast.LENGTH_LONG).show();
								}
								inLoadingMoreOnSearch=false;
								loading.setVisibility(View.GONE);
								emptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
							}
						});
	}
	
	
	private class SearchUser {

		public String id;
		public String name;
		public int role;
		public String avatar;
		public String jd_user_name;
		public String is_v_member;
		public Relation_with_current_user relation_with_current_user;
		public String summary;
		public String sex;
		public String cover;
	}
	

	@Override
	public void onLeftMenuClick() {
		finish();
	}

	@Override
	public void onRightMenuOneClick() {
	}

	public class UserAdapter extends BaseAdapter {

		private Context context;
		private List<SearchUser> usersLists;
		private String jd_user_name;

		// flag 0:关注 1：粉丝
		public UserAdapter(Context context, List<SearchUser> usersLists) {
			this.context = context;
			this.usersLists = usersLists;
		}

		@Override
		public int getCount() {
			if (usersLists == null) {
				return 0;
			} else {
				return usersLists.size();
			}
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
		public View getView( int positions, View convertView, ViewGroup parent) {


			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.community_search_userlist_item, parent, false);}
		
			RelativeLayout relativeLayout = ViewHolder.get(convertView, R.id.relativeLayout);
			RoundNetworkImageView avatar_label = ViewHolder.get(convertView,  R.id.thumb_nail);
			TextView timeline_user_name = ViewHolder.get(convertView, R.id.timeline_user_name);
			TextView timeline_user_summary = ViewHolder.get(convertView, R.id.timeline_user_summary);
			final ImageView imagebutton = ViewHolder.get(convertView, R.id.imagebutton);
			
			final int position = positions;

			ImageLoader.getInstance().displayImage(
					usersLists.get(position).avatar, avatar_label,
					GlobalVarable.getDefaultAvatarDisplayOptions(false));
			timeline_user_name.setText(usersLists.get(position).name);
			timeline_user_summary.setText(usersLists.get(position).summary);
			initFollowModel(position,imagebutton);

			imagebutton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean following = usersLists.get(position)
							.relation_with_current_user.isFollowing();
					usersLists.get(position).relation_with_current_user
							.setFollowing(!following);
					resetButton(usersLists.get(position)
							.relation_with_current_user,imagebutton);
					if (!following) {
						Follow(usersLists.get(position).id);
					} else {
						UNFollow(usersLists.get(position).id);
					}
				}
			});
//
			relativeLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent();
					intent.setClass(context, UserActivity.class);
					intent.putExtra("user_id", usersLists.get(position).id);
					intent.putExtra(UserActivity.JD_USER_NAME, usersLists.get(position).jd_user_name);
					context.startActivity(intent);
				}
			});

			return convertView;
		}

		/**
		 * 初始化关注按钮
		 * 
		 * @param position
		 *            选中用户的行索引
		 * @param userDetail
		 *            选中的用户
		 * @param holoder
		 *            viewHolder
		 */
		private void initFollowModel(final int position,ImageView imagebutton) {
			jd_user_name = usersLists.get(position).jd_user_name;
			resetButton(usersLists.get(position).relation_with_current_user,imagebutton);
		}

		public void resetButton(Relation_with_current_user focusModel,ImageView imagebutton) {
			if (focusModel.isFollowing()) {
				if (focusModel.isFollowed()) {
					imagebutton
							.setBackgroundResource(R.drawable.btn_bothfollowing);
				} else {
					imagebutton
							.setBackgroundResource(R.drawable.btn_following);
				}
			} else {
				imagebutton.setBackgroundResource(R.drawable.btn_follow);
			}
			if (LoginUser.getpin().equals(jd_user_name)) {
				imagebutton
				.setBackgroundResource(R.drawable.icon_arrow_right);
				imagebutton.setEnabled(false);
			}
		}

		private void Follow(String id) {
			WebRequestHelper.post(URLText.Follow_SomeOne_URL,
					RequestParamsPool.getFollowSomeParams(id),
					new MyAsyncHttpResponseHandler(context) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2,
								Throwable arg3) {
							Toast.makeText(context, R.string.network_connect_error,
									Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onResponse(int statusCode, Header[] headers,
								byte[] responseBody) {

							String result = new String(responseBody);
							try {
								JSONObject jsonObject = new JSONObject(result);
								String code = jsonObject.optString("code");
								String message = jsonObject.optString("message");
								if (jsonObject != null) {
									if (code.equals("0")) {
										Toast.makeText(context, message, 1).show();
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
		}

		private void UNFollow(String id) {
			WebRequestHelper.post(URLText.Follow_Cancle,
					RequestParamsPool.getUNFollowParams(id),
					new MyAsyncHttpResponseHandler(context) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2,
								Throwable arg3) {
							Toast.makeText(context, R.string.network_connect_error,
									Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onResponse(int statusCode, Header[] headers,
								byte[] responseBody) {
							String result = new String(responseBody);
							try {
								JSONObject jsonObject = new JSONObject(result);
								String code = jsonObject.optString("code");
								String message = jsonObject.optString("message");
								if (jsonObject != null) {
									if (code.equals("0")) {
										Toast.makeText(context, message, 1).show();
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
		}

	}

}
