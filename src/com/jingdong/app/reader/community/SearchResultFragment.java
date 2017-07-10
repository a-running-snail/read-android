package com.jingdong.app.reader.community;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.message.model.Alert;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.parser.BaseParserCreator;
import com.jingdong.app.reader.parser.json.TimelineJSONParser;
import com.jingdong.app.reader.parser.url.TimelineSearchURLParser;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.actiivity.RecommendActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelinePostTweetActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.adapter.TimeLineAdapter;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment.TimelineFragmentRunnable;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.timeline.model.TimelineActivityModel;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.Contants;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InflateParams")
public class SearchResultFragment extends CommonFragment  implements Observer,OnItemClickListener{

	public SearchResultFragment() {
		super();
	}

	private View rootView;
	private Context context;
	private View headerView;
	private LinearLayout userContainer;
	private EmptyLayout mEmptyLayout;
	private ListView listView;
	private List<UserInfo> usersList = new ArrayList<UserInfo>();// 搜索用户结果
	private View recommendUserItem;
	private String query;
	private List<String> temp=new ArrayList<String>();
	private LinearLayout emptyLinearLayout;
	private RelativeLayout relativeLayout;
	private TimeLineAdapter adapter;
	private TimeLineModel timeLine;
	private ScheduledExecutorService executorService;
	private int currentpage = -1;
	private int pagecount = 10;
	
	private Handler myHandler;
	private int lastItemIndex;
	private Future<?> moreLoad;
	private View loading;
	private boolean noMoreData=false;
	private boolean inLoadingMoreOnSearch=false;
	private LinearLayout timelineLinearLayout;
	
	/**
	 * 该类负责异步获取并解析timeline.json
	 * 
	 * @author Alexander
	 * 
	 */
	public class TimelineFragmentRunnable implements Runnable {
		private int type;
		private String query;

		public TimelineFragmentRunnable(int type) {
			this(type, null);
		}

		public TimelineFragmentRunnable(int type, String query) {
			this.type = type;
			this.query = query;
		}

		@Override
		public void run() {
			Context context = getActivity();
			switch (type) {

			case TimeLineModel.LOAD_AS_INPUT:
				currentpage = 1;
				timeLine.search(context, type, query, currentpage, pagecount,1);
				break;
			case TimeLineModel.LOAD_MORE:
				if (query != null) {
					currentpage++;
					timeLine.searchNextPage(context, type, query, currentpage,
							pagecount,1);
				}
				break;
			case TimeLineModel.LOAD_NEW:
//				mSwipeLayout.setRefreshing(false);
				break;
			}
		}
	}
	
	/**
	 * 该类负责更新listview中的内容
	 * 
	 * @author Alexander
	 * 
	 */
	private static class TimeLineFragmentHandler extends Handler {
		WeakReference<SearchResultFragment> tWeakReference;

		public TimeLineFragmentHandler(SearchResultFragment fragment) {
			tWeakReference = new WeakReference<SearchResultFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			SearchResultFragment fragment = tWeakReference.get();
			if (fragment != null) {
				switch (msg.what) {
				case TimeLineModel.LOAD_AS_INPUT:
					if (msg.arg1 == ObservableModel.SUCCESS_INT
							&& msg.arg2 == ObservableModel.SUCCESS_INT) {
						fragment.timeLine.refreshData();
						fragment.adapter.notifyDataSetInvalidated();
					} else if (msg.arg1 == ObservableModel.SUCCESS_INT
							&& msg.arg2 == ObservableModel.FAIL_INT) {
						if(fragment.listView.getHeaderViewsCount()==0){
							fragment.relativeLayout.setVisibility(View.GONE);
							fragment.emptyLinearLayout.setVisibility(View.VISIBLE);
						}else{
							fragment.timelineLinearLayout.setVisibility(View.GONE);
						}
//						fragment.relativeLayout.setVisibility(View.GONE);
//						fragment.linearLayout.setVisibility(View.VISIBLE);
//						fragment.mListView.setVisibility(View.GONE);
//						fragment.iconImageView.setBackgroundResource(R.drawable.bookstore_icon_search_null);
//						fragment.textView.setText("社区中暂无您的搜索动态");
					} else {
						Toast.makeText(fragment.getActivity(),
								R.string.loading_fail, Toast.LENGTH_SHORT)
								.show();
					}
					break;
				case TimeLineModel.LOAD_MORE:
					fragment.loading.setVisibility(View.GONE);
					fragment.inLoadingMoreOnSearch=false;
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
							fragment.timeLine.refreshData();
							fragment.adapter.notifyDataSetChanged();
						} else {
							fragment.noMoreData=true;
							Toast.makeText(fragment.getActivity(),
									R.string.user_no_more, Toast.LENGTH_SHORT)
									.show();
						}
					} else
						Toast.makeText(fragment.getActivity(),
								R.string.loading_fail, Toast.LENGTH_SHORT)
								.show();
					break;
				case TimeLineModel.DELETE_ENTITY:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						String guid = (String) msg.obj;
						if (fragment.timeLine.delete(guid))
							fragment.adapter.notifyDataSetChanged();
					}
					break;
				case TimeLineModel.UPDATE_COMMENTS_NUMBER:
					int index = fragment.timeLine.indexOf((String) msg.obj);
					Entity entity = fragment.timeLine.getEntityAt(index);
					entity.setCommentNumber(msg.arg1);
					entity.setRecommendsCount(msg.arg2);
					if(msg.getData().getInt("UPDATE_VIEWREMMENDED")==1)
						entity.setViewerRecommended(msg.getData().getBoolean(TweetModel.VIEWERRECOMMENDED));
					fragment.adapter.notifyDataSetChanged();
					break;
				}
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.community_search_result_fragment, null);
		headerView = inflater.inflate(R.layout.community_search_reslult_headerview, null);
		userContainer = (LinearLayout) headerView.findViewById(R.id.userContainer);
		emptyLinearLayout=(LinearLayout) rootView.findViewById(R.id.emptyLinearLayout);
		relativeLayout= (RelativeLayout) rootView.findViewById(R.id.relativeLayout);
		timelineLinearLayout = (LinearLayout) headerView.findViewById(R.id.timelineLinearLayout);
		
		mEmptyLayout = (EmptyLayout) rootView.findViewById(R.id.error_layout);
		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (EmptyLayout.NETWORK_ERROR == mEmptyLayout.getErrorState()) {
					mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
				}
			}
		});
		listView = (ListView) rootView.findViewById(R.id.listview);
		listView.addHeaderView(headerView);
		
//		timelineAdapter=new TimelineAdapter(context, temp);
//		listView.setAdapter(timelineAdapter);
		
		
		timeLine = new TimeLineModel(true, new BaseParserCreator(TimelineSearchURLParser.class,TimelineJSONParser.class));
		timeLine.addObserver(this);
		adapter = new TimeLineAdapter(getActivity(), timeLine, false,getRootFragment());
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		
		loading = inflater.inflate(R.layout.list_cell_footer, null, false);
		listView.addFooterView(UiStaticMethod.getFooterParent(getActivity(),
				loading));
		loading.setVisibility(View.GONE);
		
		return rootView;
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context=getActivity();
		query = getArguments().getString("query");
		
		executorService = NotificationService.getExecutorService();
		myHandler = new TimeLineFragmentHandler(this);
		
		if(query!=null){
			//搜索用户
			searchUsers(query,Contants.START_PAGE,Contants.PER_PAGE_NUMBER);
		}
		
		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount == 0)
					return;

				if (firstVisibleItem + visibleItemCount == totalItemCount-3
						&& !noMoreData) {
					if (!inLoadingMoreOnSearch) {
						inLoadingMoreOnSearch = true;
						loading.setVisibility(View.VISIBLE);
						moreLoad = executorService.submit(new TimelineFragmentRunnable(TimeLineModel.LOAD_MORE, query));
					}
				}
			}
		});
	}
	
	private void searchTimeline(final String query){
		executorService.execute(new TimelineFragmentRunnable(
				TimeLineModel.LOAD_AS_INPUT, query));
	}
	
	/**
	 * 搜索用户
	 * @param query
	 * @param currentpage
	 * @param pagecount
	 */
	public void searchUsers(final String query,final int currentpage,final int pagecount) {
				WebRequestHelper.getWithContext(context,URLText.searchPeople,
						RequestParamsPool
								.searchPeopleParams(currentpage,pagecount, query), true,
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
								pareUserList(jsonString);
								initUserContainer();
								mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
								searchTimeline(query);
							}
						});
	}

	/**
	 * 解析json字符串，并把解析结果填充到一个List中
	 * 
	 * @param json
	 *            json字符串
	 * @return User的列表
	 */
	private void pareUserList(String json) {
		try {
			JSONArray userArray = new JSONArray(json);
			if (userArray != null) {
				JSONObject jsonObject;
				usersList = new ArrayList<UserInfo>(userArray.length());
				UserInfo user;
				for (int i = 0; i < userArray.length(); i++) {
					jsonObject = userArray.getJSONObject(i);
					if (!jsonObject.getString(UserInfo.ID).equals(
							LoginUser.getpin())) {
						user = new UserInfo();
						user.parseJson(jsonObject);
						usersList.add(user);
					}
				}
			} 
		} catch (JSONException e) {
		}
	}
	
	private void initUserContainer(){
		if(usersList !=null && usersList.size()>0){
			relativeLayout.setVisibility(View.VISIBLE);
			emptyLinearLayout.setVisibility(View.GONE);
			
			LayoutInflater inflater = getActivity().getLayoutInflater();
			for (int i = 0; i < usersList.size(); i++) {
				recommendUserItem = inflater.inflate(R.layout.community_search_hearderview_useritem, null, false);
				RoundNetworkImageView avatar_label = (RoundNetworkImageView) recommendUserItem.findViewById(R.id.thumb_nail);
				TextView timeline_user_name = (TextView) recommendUserItem.findViewById(R.id.username);

				if(i < Contants.PER_PAGE_NUMBER-1){
					UserInfo user = usersList.get(i);
					ImageLoader.getInstance().displayImage(
							user.getAvatar(), avatar_label,
							GlobalVarable.getDefaultAvatarDisplayOptions(false));
					timeline_user_name.setText(user.getName());
					final UserInfo finalUser= user;
					avatar_label.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.setClass(getActivity(), UserActivity.class);
							intent.putExtra("user_id", finalUser.getId());
							intent.putExtra(UserActivity.JD_USER_NAME, finalUser.getJd_user_name());
							getActivity().startActivity(intent);
						}
					});
				}else{

					avatar_label.setImageResource(R.drawable.recommend_all_bt);
					timeline_user_name.setText("全部");
					avatar_label.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(getActivity(), SearchUserListActivity.class);
							intent.putExtra("query", query);
							startActivity(intent);
						}
					});
				}
				userContainer.addView(recommendUserItem);
			}
		}else{
			if(listView.getHeaderViewsCount()>=0)
				listView.removeHeaderView(headerView);
//			relativeLayout.setVisibility(View.GONE);
//			emptyLinearLayout.setVisibility(View.VISIBLE);
		}
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		// 从详情页返回，更新列表数据
		case TimelineTweetActivity.START_TWEET_FROM_QUOTE:
		case FriendCircleFragment.START_ACTIVITY_FROM_LIST:
			Message message = Message.obtain();
			switch (resultCode) {
			case TimelineTweetActivity.DELETE_ENTITY:
				deleteEntity(message, data);
				break;
			case TimelineTweetActivity.UPDATE_COMMENTS_NUMBER:
				message.obj = data.getStringExtra(TimelineTweetActivity.TWEET_GUID);
				if (data.getBooleanExtra(TweetModel.IS_DELETE, false)) {
					deleteEntity(message, data);
				} else {
					message.what = TimeLineModel.UPDATE_COMMENTS_NUMBER;
					int index = timeLine.indexOf((String) message.obj);
					if (index < 0) {
						return;
					} else {
						Entity entity = timeLine.getEntityAt(index);
						int commentNumber = entity.getCommentNumber();
						int recommendCount = entity.getRecommendsCount();
						message.arg1 = data.getIntExtra(TweetModel.COMMENT_NUMBER, commentNumber);
						message.arg2 = data.getIntExtra(TweetModel.RECOMMENTS_COUNT, recommendCount);
						Bundle bundle = new Bundle();
						bundle.putInt("UPDATE_VIEWREMMENDED", 1);
						bundle.putBoolean(TweetModel.VIEWERRECOMMENDED,
								data.getBooleanExtra(TweetModel.VIEWERRECOMMENDED, false));
						message.setData(bundle);
						if (message.arg1 == commentNumber && message.arg2 == recommendCount)
							message.recycle();
						else
							myHandler.sendMessage(message);
					}
				}
				break;
			}
			break;
			case TimelineTweetActivity.START_COMMENT_FROM_TWEET:
				if (resultCode == getActivity().RESULT_OK) {
					Bundle commentBundle = data.getExtras();
//				if (commentBundle
//						.getBoolean(TimelineCommentsActivity.IS_COMMENT))
//					postComment(getActivity(), commentBundle);
//				else
//					postComment(getActivity(), commentBundle);
					
					Message message1 = Message.obtain();
					message1.obj = commentBundle.getString(TimelineCommentsActivity.ENTITY_GUID);
					message1.what = TimeLineModel.UPDATE_COMMENTS_NUMBER;
					int index = timeLine.indexOf((String) message1.obj);
					Entity entity = timeLine.getEntityAt(index);
					int commentNumber = entity.getCommentNumber();
					int recommendCount = entity.getRecommendsCount();
					if (commentBundle.getBoolean(TimelineCommentsActivity.IS_COMMENT)||commentBundle.getBoolean(TimelineCommentsActivity.CHECKED)){
						message1.arg1 = commentNumber+1;
						message1.arg2 = recommendCount;
						Bundle bundle1=new Bundle();
						bundle1.putInt("UPDATE_VIEWREMMENDED", 0);
						message1.setData(bundle1);
					}
					else{
					}
					myHandler.sendMessage(message1);

					if(commentBundle.getBoolean(TimelineCommentsActivity.CHECKED))
						executorService.execute(new TimelineFragmentRunnable(TimeLineModel.LOAD_NEW));
				}
			break;
		}
	}
	
	private void deleteEntity(Message message, Intent data) {
		message.what = TimeLineModel.DELETE_ENTITY;
		message.arg1 = ObservableModel.SUCCESS_INT;
		message.obj = data.getStringExtra(TimelineTweetActivity.DELETE_ENTITY_ID);
		myHandler.sendMessage(message);
	}
	
	
	/**
	 * 向服务器发送一条评论或转发
	 * 
	 * @param context
	 *            数据上下文
	 * @param bundle
	 *            创建一条评论所需要的数据。
	 */
	public void postComment(final Context context, final Bundle bundle) {

		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				WebRequestHelper.post(URLText.commmentsPostUrl,
						RequestParamsPool
								.getEntitysCommentsOrForwordParams(bundle),
						true, new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String result = new String(responseBody);
								
								try {
									JSONObject json= new JSONObject(result);
									if(json.optInt("code")==0){
										Message message = Message.obtain();
										message.obj = bundle.getString(TimelineCommentsActivity.ENTITY_GUID);
										message.what = TimeLineModel.UPDATE_COMMENTS_NUMBER;
										int index = timeLine.indexOf((String) message.obj);
										Entity entity = timeLine.getEntityAt(index);
										int commentNumber = entity.getCommentNumber();
										int recommendCount = entity.getRecommendsCount();
										if (bundle.getBoolean(TimelineCommentsActivity.IS_COMMENT)){
											message.arg1 = commentNumber+1;
											message.arg2 = recommendCount;
											Bundle bundle1=new Bundle();
											bundle1.putInt("UPDATE_VIEWREMMENDED", 0);
											message.setData(bundle1);
										}
										else{
										}
										myHandler.sendMessage(message);

										if(bundle.getBoolean(TimelineCommentsActivity.CHECKED))
											executorService.execute(new TimelineFragmentRunnable(TimeLineModel.LOAD_NEW));
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								ToastUtil.showToastInThread("请求失败了,请重试!",
										Toast.LENGTH_SHORT);
							}
						});
			}
		});

	}
	
	/**
	  * 得到根Fragment
	  * 
	  * @return
	  */
	 private Fragment getRootFragment() {
	  Fragment fragment = getParentFragment();
	  if(fragment!=null){
		  while (fragment.getParentFragment() != null) {
			   fragment = fragment.getParentFragment();
			  }
	  }else
		  fragment=this;
	  return fragment;

	 }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Entity entity = null;
		if (position <= adapter.getCount()) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			if(listView.getHeaderViewsCount()>0)
				entity = timeLine.getEntityAt(position - 1);
			else
				entity = timeLine.getEntityAt(position);
			if (entity instanceof Alert) {
				Alert alert = (Alert) entity;
				if (alert.getLinkType() == Alert.USER_LINK) {
					intent.putExtra(UserFragment.USER_ID, Long.parseLong(((Alert) entity).getLink()));
					intent.setClass(getActivity(), UserActivity.class);
					startActivity(intent);
				} else if (alert.getLinkType() == Alert.ENTITY_LINK) {
					startTweetActivity(intent, entity);
				}
			} else {
				startTweetActivity(intent, entity);
			}

		}
	}
	/**
	 * 启动一个TweetActivity
	 * 
	 * @param intent
	 *            待设置的Intent
	 * @param entity
	 *            数据源
	 */
	private void startTweetActivity(Intent intent, Entity entity) {
		String guid = entity.getGuid();
		if (UiStaticMethod.isNullString(guid))
			guid = entity.getRenderBody().getEntity().getGuid();
		intent.putExtra(TimelineTweetActivity.TWEET_GUID, guid);
		intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, entity.getUser().getName());
		intent.setClass(getActivity(), TimelineTweetActivity.class);
//		startActivity(intent);
		getRootFragment().startActivityForResult(intent, FriendCircleFragment.START_ACTIVITY_FROM_LIST);
	}
	
	/**
	 * 观察到数据有改动
	 */
	@Override
	public void update(Observable arg0, Object data) {
		Message message = (Message) data;
		myHandler.sendMessage(message);
		
	}
	
	
	@Override
	public void onDestroy() {
		WebRequestHelper.cancleRequest(getActivity());
		timeLine.deleteObserver(this);
		myHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}
}
