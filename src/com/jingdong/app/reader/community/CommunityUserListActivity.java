package com.jingdong.app.reader.community;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.FocusModel;
import com.jingdong.app.reader.entity.extra.Relation_with_current_user;
import com.jingdong.app.reader.entity.extra.UsersList;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Comment;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.TopBarView;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
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

/**
 * 喜欢的人，显示点赞的用户列表，可以关注
 * @author tanmojie
 *
 */

public class CommunityUserListActivity extends BaseActivityWithTopBar {

	private TopBarView topBarView = null;
	private ListView mListView;
	private static int perPageCount = 10;
	public static final int UPDATE_FOLLOWED_STATUS = 865;

	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private boolean inSearch = false;
	private FocusModel focusmodel;
	private List<UserInfo> usersLists = new ArrayList<UserInfo>();
	private List<Comment> comments = new ArrayList<Comment>();
	private List<Comment> tempComments = new ArrayList<Comment>();
	private FocusAdapter focusAdapter;
	private String ids;
	private String guid;
	private Context context;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.community_userlist);
		context=this;
		topBarView = (TopBarView) findViewById(R.id.topbar);
		initTopbarView();
		initField();
		
		guid = getIntent().getStringExtra("guid");
		loadRecommendUsers(this,guid,TweetModel.LOAD_RECOMMEND);
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
						loadRecommendUsers(context,guid,TweetModel.LOAD_NEXT_RECOMMEND);
					}
				}
			}

		});
		focusAdapter = new FocusAdapter(CommunityUserListActivity.this, usersLists);
		mListView.setAdapter(focusAdapter);
	}

	public void initTopbarView() {

		if (topBarView == null)
			return;
		topBarView.setTitle("喜欢的人");
//		topBarView.setLeftMenuVisiable(true, "取消", R.color.red_main);
//		topBarView.setRightMenuOneVisiable(true, "一键关注", R.color.red_main,
//				false);
//		topBarView.setListener(this);
	}

	public void initField() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}

	private void getMyWant() {
		WebRequestHelper.get(URLText.Recommend_URL, RequestParamsPool
				.getRecommendUserParams(currentSearchPage + "", 30 + ""), true,
				new MyAsyncHttpResponseHandler(CommunityUserListActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(CommunityUserListActivity.this,
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
//								usersLists.addAll(all);
								focusAdapter.notifyDataSetChanged();
							}
						} else
							Toast.makeText(CommunityUserListActivity.this,
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
		if (usersLists != null) {
			ids = usersLists.get(0).getId();
			for (int i = 0; i < usersLists.size(); i++) {
				ids += "," + usersLists.get(i).getId();
//				usersLists.get(i).getRelation_with_current_user()
//						.setFollowing(true);
			}
			focusAdapter.notifyDataSetChanged();
		}
	}

	
	
	/**
	 * 重新解析Json数据，如果有数据，则通知观察者数据变化。
	 * 
	 * @param guid
	 *            当前动态的guid
	 * @param type
	 *            以何种方式解析数据
	 */
	public void loadRecommendUsers(final Context context, String guid,int type) {
		final String url = getUrl(context, guid, type);
		
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				WebRequestHelper.get(url, RequestParamsPool.getEmptyParams(),
						true, new MyAsyncHttpResponseHandler(context) {
							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								try {
									String jsonString = new String(responseBody);

									JSONObject jObject = new JSONObject(
											jsonString);
									JSONArray array = jObject
											.getJSONArray("recommends");
									int totalCount = jObject.optInt("total_recommends_count");
									if (array.length() != 0) {
										tempComments.clear();
										fillComments(array, tempComments);
									}
									
									if (tempComments!= null) {
											currentSearchPage++;
											
											List<UserInfo> all = new ArrayList<UserInfo>();
											for (int i = 0; i < tempComments.size(); i++) {
												UserInfo userInfo = tempComments.get(i).getUser();
												all.add(userInfo);
											}
											usersLists.addAll(all);
											focusAdapter.notifyDataSetChanged();
											if(usersLists.size()<totalCount){
												noMoreBookOnSearch = false;
											}else
												noMoreBookOnSearch = true;
									} else
										Toast.makeText(CommunityUserListActivity.this,
												getString(R.string.network_connect_error),
												Toast.LENGTH_LONG).show();
									inLoadingMoreOnSearch = false;
								} catch (Exception e) {
								}
							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								MZLog.d("wangguodong", "请求出错了");
							}
						});
			}
		});
	}
	
	/**
	 * 根据JsonArray,初始化评论列表
	 * 
	 * @param jsonArray
	 *            数据源
	 * @param comments
	 *            待初始化的评论列别
	 * @throws JSONException
	 */
	private void fillComments(JSONArray jsonArray, List<Comment> tempComments)
			throws JSONException {
		JSONObject jsonObject;
		for (int i = 0; i < jsonArray.length(); i++) {
			jsonObject = jsonArray.getJSONObject(i);
			Comment comment = new Comment();
			comment.parseJson(jsonObject);
			tempComments.add(comment);
		}
		comments.addAll(tempComments);
	}
	
	private String getUrl(Context context, String guid, int type) {
		String baseUrl = null, url = null;
		long lastId = -1;
		switch (type) {
		case TweetModel.LOAD_NEXT_RECOMMEND:
			if (!comments.isEmpty())
				lastId = comments.get(comments.size() - 1).getId();
		case TweetModel.LOAD_RECOMMEND:
			baseUrl = URLText.recommandListUrl;
			break;
		}
		if (baseUrl != null) {
			url = baseUrl;
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("jd_user_name", LoginUser.getpin());
			paramMap.put(TweetModel.ENTITY_ID, guid);
			if (lastId != -1) {
				paramMap.put(TweetModel.BEFORE_ID, Long.toString(lastId));
			}
			url = URLBuilder.addParameter(baseUrl, paramMap);
		}
		return url;
	}
	
	
	class FocusAdapter extends BaseAdapter {

		private Context context;
		private List<UserInfo> usersLists;
		private String jd_user_name;

		// flag 0:关注 1：粉丝
		public FocusAdapter(Context context, List<UserInfo> usersLists) {
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
						R.layout.focusitem, parent, false);}
		
			RelativeLayout relativeLayout = ViewHolder.get(convertView, R.id.relativeLayout);
			RoundNetworkImageView avatar_label = ViewHolder.get(convertView,  R.id.thumb_nail);
			TextView timeline_user_name = ViewHolder.get(convertView, R.id.timeline_user_name);
			TextView timeline_user_summary = ViewHolder.get(convertView, R.id.timeline_user_summary);
			final ImageView imagebutton = ViewHolder.get(convertView, R.id.imagebutton);
			
			final int position = positions;
			ImageLoader.getInstance().displayImage(
					usersLists.get(position).getAvatar(), avatar_label,
					GlobalVarable.getDefaultAvatarDisplayOptions(false));
			timeline_user_name.setText(usersLists.get(position).getName());
			timeline_user_summary.setText(usersLists.get(position).getSummary());
			initFollowModel(position,imagebutton);

			imagebutton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean following = usersLists.get(position)
							.getRelation_with_current_user().isFollowing();
					usersLists.get(position).getRelation_with_current_user()
							.setFollowing(!following);
					resetButton(usersLists.get(position)
							.getRelation_with_current_user(),imagebutton);
					if (!following) {
						Follow(usersLists.get(position).getId());
					} else {
						UNFollow(usersLists.get(position).getId());
					}
				}
			});

			relativeLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent();
					intent.setClass(context, UserActivity.class);
					intent.putExtra("user_id", usersLists.get(position).getId());
					intent.putExtra(UserActivity.JD_USER_NAME, usersLists.get(position).getJd_user_name());
					intent.putExtra("position", position);
					startActivityForResult(intent, UPDATE_FOLLOWED_STATUS);
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
			jd_user_name = usersLists.get(position).getJd_user_name();
			resetButton(usersLists.get(position).getRelation_with_current_user(),imagebutton);
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
			}else{
				imagebutton.setEnabled(true);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case UPDATE_FOLLOWED_STATUS:
			if(resultCode==RESULT_OK){
				int position = data.getIntExtra("position", -1);
				if(position>=0){
					boolean status= data.getBooleanExtra("status", false);
					usersLists.get(position).getRelation_with_current_user().setFollowing(status);
					focusAdapter.notifyDataSetChanged();
				}
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
