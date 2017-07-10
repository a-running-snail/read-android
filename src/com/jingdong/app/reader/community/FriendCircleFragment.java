package com.jingdong.app.reader.community;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookcaseLocalFragmentListView;
import com.jingdong.app.reader.activity.CommunityFragment;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.TimelineRootFragment;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.entity.BaseEvent;
import com.jingdong.app.reader.entity.LoginEvent;
import com.jingdong.app.reader.entity.ReLoginEvent;
import com.jingdong.app.reader.entity.SignEvent;
import com.jingdong.app.reader.entity.extra.FocusModel;
import com.jingdong.app.reader.entity.extra.Relation_with_current_user;
import com.jingdong.app.reader.entity.extra.UsersList;
import com.jingdong.app.reader.eventbus.de.greenrobot.event.EventBus;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.message.activity.MessageActivity;
import com.jingdong.app.reader.message.adapter.MessageItemAdapter;
import com.jingdong.app.reader.message.model.Alert;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.parser.ParserCreator;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.actiivity.RecommendActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelinePostTweetActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.adapter.TimeLineAdapter;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.timeline.model.TimelineActivityModel;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.TopBarPopupWindow;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tendcloud.tenddata.TCAgent;

public class FriendCircleFragment extends CommonFragment implements SwipeRefreshLayout.OnRefreshListener, Observer, OnScrollListener,
		OnItemClickListener, TopBarPopupWindow.onPopupWindowItemClickListener {
	private static final int NET_ERROR = 404;
	/** 登陆请求结果标识 */
	private final int LOGIN = 111;
	
	private class TimeLineRunnable implements Runnable {
		private int type;

		public TimeLineRunnable(int type) {
			this.type = type;
		}

		@Override
		public void run() {
			switch (type) {
			case TimelineActivityModel.POST_TWEET:
				model.postTweet(getActivity(), postTweetBundle);
				break;
			}
		}
	}

	public FriendCircleFragment() {
		super();
	}

	@Override
	public void onDestroy() {
		model.deleteObserver(this);
		MZLog.d("life-cycle", "社区onDestroy");
		super.onDestroy();
	}

	private void submitinit() {
		executorService.submit(new TimelineFragmentRunnable(TimeLineModel.LOAD_NEW));
	}

	/**
	 * 该类负责更新listview中的内容
	 * 
	 */
	private static class TimeLineFragmentHandler extends Handler {
		WeakReference<FriendCircleFragment> tWeakReference;

		public TimeLineFragmentHandler(FriendCircleFragment fragment) {
			tWeakReference = new WeakReference<FriendCircleFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			FriendCircleFragment fragment = tWeakReference.get();
			if (fragment != null) {
				switch (msg.what) {
				case TimeLineModel.RECOMMEND:
					int unfollow_count = 0;
					if(!TextUtils.isEmpty(fragment.unfollow_count)) {
						try{
							unfollow_count = Integer.parseInt(fragment.unfollow_count);
						}catch(NumberFormatException e) {
							e.printStackTrace();
						}
					}
					if (unfollow_count > 0 /*&& Integer.parseInt(fragment.count) < 5*/) {
						if(fragment.listView.getHeaderViewsCount() <= 0) {
							ishaveHeaderView = true;
							fragment.listView.addHeaderView(fragment.headerView);
						}
					} else {
						ishaveHeaderView = false;
						fragment.listView.removeHeaderView(fragment.headerView);
					}
					fragment.submitinit();
					break;
				case TimeLineModel.LOAD_INIT:
					if (fragment.loadingMore)
						fragment.listView.setOnScrollListener(fragment);
					fragment.listView.setOnItemClickListener(fragment);
					fragment.loading.setVisibility(View.GONE);
					fragment.mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
					mSwipeLayout.setRefreshing(false);
					if (fragment.pullRefresh) {
						mSwipeLayout.setRefreshing(false);
					}
					iniLoad(msg, fragment);
					break;
				case TimeLineModel.RELOAD:
					fragment.loading.setVisibility(View.GONE);
					fragment.mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
					if (msg.arg1 == ObservableModel.SUCCESS_INT)
						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
							fragment.timeLine.refreshData();
							fragment.adapter.notifyDataSetChanged();
						}
					break;
				case TimeLineModel.LOAD_AS_INPUT:
					fragment.loading.setVisibility(View.GONE);
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						fragment.timeLine.refreshData();
						fragment.adapter.notifyDataSetInvalidated();
					} else
						Toast.makeText(fragment.getActivity(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
					break;
				case TimeLineModel.LOAD_MORE:
					fragment.loading.setVisibility(View.GONE);
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
							fragment.timeLine.refreshData();
							fragment.adapter.notifyDataSetChanged();
						} else {
							Toast.makeText(fragment.getActivity(), R.string.user_no_more, Toast.LENGTH_SHORT).show();
						}
					} else
						Toast.makeText(fragment.getActivity(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
					fragment.isLoading=false;
					break;
				case TimeLineModel.LOAD_NEW:
					fragment.loading.setVisibility(View.GONE);
					fragment.mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
					if (fragment.pullRefresh) {
						Notification.getInstance().setReadNormalCount(fragment.getActivity());
						if (msg.arg1 == ObservableModel.SUCCESS_INT) {
							fragment.timeLine.refreshData();
							fragment.adapter.notifyDataSetChanged();
						}
						mSwipeLayout.setRefreshing(false);
					}
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
				case TimeLineModel.EMPTY_INPUT:
					fragment.timeLine.clear();
					fragment.loading.setVisibility(View.GONE);
					fragment.adapter.notifyDataSetInvalidated();
					break;
				case TimelineActivityModel.POST_TWEET_NEW:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						if (fragment.executorService != null) {
							fragment.executorService.submit(fragment.new TimelineFragmentRunnable(TimeLineModel.LOAD_NEW));
						}
					}
					break;
				case NET_ERROR:
					fragment.mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
					break;
				}
			}
		}

		/**
		 * 初始化ListView
		 * 
		 * @param msg
		 *            消息体
		 * @param fragment
		 *            待设置的Fragment.
		 */
		private void iniLoad(Message msg, FriendCircleFragment fragment) {
			if (msg.arg1 == ObservableModel.SUCCESS_INT) {

				if (msg.arg2 == ObservableModel.SUCCESS_INT) {
					fragment.timeLine.clear();
					fragment.timeLine.refreshData();
					fragment.adapter.notifyDataSetChanged();
				} else {
//					Activity activity = fragment.getActivity();
//					if (fragment.index != DEFAULT_INDEX && activity instanceof MessageActivity) {
//						MessageActivity messageActivity = (MessageActivity) activity;
//						if (fragment.index == messageActivity.getIndex())
//							Toast.makeText(fragment.getActivity(), R.string.user_no_more, Toast.LENGTH_SHORT).show();
//					}
//
//					fragment.relativeLayout.setVisibility(View.GONE);
//					fragment.linearLayout.setVisibility(View.VISIBLE);
//					fragment.icon.setBackgroundResource(R.drawable.icon_users);
//					fragment.text.setText("您还没有关注任何用户");
//					fragment.emptybutton.setVisibility(View.VISIBLE);
//					fragment.emptybutton.setText("查看推荐用户");
				}
			} else {
				Toast.makeText(fragment.getActivity(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 处理列表数据更新
	 * 
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
			case TimeLineModel.RELOAD:
			case TimeLineModel.LOAD_INIT:
//				listView.setOnItemClickListener(null);
				currentpage = 1;
				if (!NetWorkUtils.isNetworkConnected(getActivity())) {
					myHandler.sendEmptyMessage(NET_ERROR);
				} else {
					timeLine.loadEntities(context, type, currentpage, pagecount, "", "",LocalUserSetting.getRecommend_Guid(context, LoginUser.getpin()), 0);
				}
				break;
			case TimeLineModel.LOAD_MORE:
				if (query != null) {
				} else {
					currentpage++;
					timeLine.loadEntities(context, type, currentpage, pagecount, timeLine.getEntityAt(timeLine.getLength() - 1).getGuid(), "", "", 0);
				}
				break;
			case TimeLineModel.LOAD_NEW:
				currentpage = 1;
				if (pullRefresh) {
					if (timeLine.getLength() == 0) {
						executorService.execute(new TimelineFragmentRunnable(TimeLineModel.LOAD_INIT));
					} else {
						if (!NetWorkUtils.isNetworkConnected(getActivity())) {
							myHandler.sendEmptyMessage(NET_ERROR);
						} else {
							timeLine.loadEntities(context, type, currentpage, pagecount, "",
									LocalUserSetting.getCommunity_Since_Guid(context, LoginUser.getpin()),
									LocalUserSetting.getRecommend_Guid(context, LoginUser.getpin()), 0);
						}
					}
				}
				break;
			}
		}
	}

	/**
	 * 对应于一个boolean变量，默认值为false，目前只有在消息-通知中调用Timeline的时候需要把这个变量设为false。
	 * 对应的boolean变量如果为true，表示使用TimelineAdapter；如果为false，则使用MessageItemApdater。
	 */
	public final static String TIMELINE_ADAPTER = "timelineAdapter";
	/**
	 * 对应于一个boolean变量，默认值为false。对应的boolean变量如果为true，表示隐藏Timeline中每一项下方的转发和评论按钮；
	 * 如果为false，则显示Timeline中每一项下方的转发和评论按钮。
	 */
	public final static String HIDE_BOTTOM = "hide_bottom";
	/**
	 * 对应于一个boolean变量，默认值为false，目前只有在主页-动态中调用Timeline的时候需要把这个变量设为true。
	 * 对应的boolean变量如果为true， 表示支持顶部下拉刷新功能；如果为false，顶部下拉刷新自动被禁用。
	 */
	public final static String ENABLE_PULL_REFRESH = "pullrefresh";
	/**
	 * 对应于一个boolean变量，默认值为true，目前只有在精选中需要把这个参数设置为false。对应的boolean变量如果为true，
	 * 则支持底部下拉加载更多；如果为false，则禁用底部下拉加载更多。
	 */
	public final static String ENABLE_LOADING_MORE = "loadingMore";
	/**
	 * 对应于一个boolean变量,默认值为false，目前只有在SearchActivity中调用Timeline的时候需要把这个变量是为true。
	 * 对应的变量如果为true，表示启动搜索功能，当前Fragment会自动切换到搜索模式；如果为false，则关闭搜索功能。
	 */
	public final static String SEARCHABLE = "search";
	/**
	 * 对应于一个int变量,默认值为TimelineFragment.DEFAULT_INDEX，这个变量为一个过期的变量，即将被删除，不需要传入，
	 * 目前只有在MessageItemAdapter中被使用。这个变量表示当前Fragmet处于Adatper中的第几位，请勿提供这个参数。
	 */
	public final static String INDEX = "index";
	/**
	 * 对应于ParserCreator对象，这个参数必须传入。否则TimelineFragement无法获取URL并解析成JSON。
	 */
	public final static String PARSER_CREATOR = "parserCreator";
	public final static long RELOAD_DELAY_MS = 5000;
	public final static int START_ACTIVITY_FROM_LIST = 100;
	public final static int GOTO_RECOMMANDED_USER_ACTIVITY = 112;
	private final static int DEFAULT_INDEX = -1;
	private boolean loadingMore;
	private boolean pullRefresh;
	private boolean searchable;
	private int index;
	private int lastItemIndex;
	private String currentQuery;
	private TimeLineModel timeLine;
	private ListView listView;
	private BaseAdapter adapter;
	private Handler myHandler;
	private View loading;
	private SearchView searchView;
	private Future<?> moreLoad;
	private ScheduledExecutorService executorService;
	private View layout;
	private int currentpage = -1;
	private int pagecount = 20;
	private static SwipeRefreshLayout mSwipeLayout;
	private TopBarPopupWindow rightPopupWindow = null;

	public final static int POST_TWEET = 101;
	private Bundle postTweetBundle;

	private TimelineActivityModel model;
	private RelativeLayout relativeLayout;
	private LinearLayout linearLayout;
	private ImageView icon;
	private TextView text;
	private Button emptybutton;
	private View headerView;
	private LinearLayout recommendContainer;
	private String unfollow_count;
	private String count;
	private static boolean ishaveHeaderView = false;
	private int adapterCount = -1;
	private View recommendUserItem;
	private List<UsersList> recommendUsers = new ArrayList<UsersList>();
	private TextView recommendAll;
	private boolean isLoading=false;

	@SuppressWarnings("deprecation")
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_friendcircle, null);
		LinearLayout temp = (LinearLayout) rootView.findViewById(R.id.container);
		layout = inflater.inflate(R.layout.fragment_timeline, temp);
		fragmentTag = "TimelineFragment";
		count = LocalUserSetting.getRecommend_count(getActivity(), LoginUser.getpin());
		LocalUserSetting.saveRecommend_count(getActivity(), Integer.parseInt(LocalUserSetting.getRecommend_count(getActivity(), LoginUser.getpin())) + 1 + "",
				LoginUser.getpin());
		loading = inflater.inflate(R.layout.list_cell_footer, null, false);
		headerView = inflater.inflate(R.layout.recommend_header, null);
		recommendContainer = (LinearLayout) headerView.findViewById(R.id.recommendContainer);
		recommendAll =  (TextView) headerView.findViewById(R.id.recommendAll);
		
		mEmptyLayout = (EmptyLayout) layout.findViewById(R.id.error_layout);
		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(EmptyLayout.NETWORK_ERROR == mEmptyLayout.mErrorState) {
					if (!NetWorkUtils.isNetworkConnected(getActivity())) {
						mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
						mEmptyLayout.postDelayed(new Runnable() {
							@Override
							public void run() {
								mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
							}
						}, 500);
					}else if(LoginUser.isLogin()){
						mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
						submitinit();
						initRecommendLayout();
					}else {
						mEmptyLayout.setErrorType(EmptyLayout.NOT_LOGIN);
					}
				}else if(EmptyLayout.NOT_LOGIN == mEmptyLayout.mErrorState) {
					if (!NetWorkUtils.isNetworkConnected(getActivity())) {
						mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
					}else{
						Intent it = new Intent(getActivity(), LoginActivity.class);
						getRootFragment().startActivityForResult(it, LOGIN);
					}
				}
				
			}
		});
		listView = (ListView) layout.findViewById(R.id.timeline_list);
		mSwipeLayout = (SwipeRefreshLayout) layout.findViewById(R.id.ptr_layout);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.red_main, R.color.bg_main, R.color.red_sub, R.color.bg_main);
		linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
		relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativeLayout);
		icon = (ImageView) rootView.findViewById(R.id.icon);
		text = (TextView) rootView.findViewById(R.id.text);
		emptybutton = (Button) rootView.findViewById(R.id.empty);
		List<String> rightItemSubmenu = new ArrayList<String>();
		rightItemSubmenu.add("写书评");
		rightItemSubmenu.add("随便说说");

		rightPopupWindow = new TopBarPopupWindow(getActivity(), rightItemSubmenu, "101");
		rightPopupWindow.setListener(this);

		model = new TimelineActivityModel();
		model.addObserver(this);

		emptybutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!LoginUser.isLogin()) {
					Intent intent = new Intent(getActivity(), LoginActivity.class);
					startActivity(intent);
				} else {
					Intent intent = new Intent(getActivity(), RecommendActivity.class);
					startActivity(intent);
				}
			}
		});
		EventBus.getDefault().register(this);
		return rootView;
	}
	
	public void onEventMainThread(BaseEvent baseEvent) {
		if (baseEvent instanceof ReLoginEvent) {
			try {
				if (null != timeLine && timeLine.getLength() == 0 && mEmptyLayout != null) {
					if (!NetWorkUtils.isNetworkConnected(getActivity())) {
						mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
					} else if (LoginUser.isLogin()) {
						mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
						ishaveHeaderView = false;
						submitinit();
						initRecommendLayout();
					} else {
						mEmptyLayout.setErrorType(EmptyLayout.NOT_LOGIN);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 初始化推荐关注
	 */
	@SuppressWarnings("deprecation")
	private void initRecommendLayout() {
		WebRequestHelper.get(URLText.Recommend_URL, RequestParamsPool
				.getRecommendUserParams("1", 10 + ""), true,
				new MyAsyncHttpResponseHandler(getActivity()) {

					
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						ToastUtil.showToastInThread(getString(R.string.network_connect_error), Toast.LENGTH_SHORT);
					}

					@SuppressLint("InflateParams")
					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

						String result = new String(responseBody);
						if(getActivity() == null) 
							return ;
						LayoutInflater inflater = getActivity().getLayoutInflater();
						focusmodel = GsonUtils.fromJson(result, FocusModel.class);
						
						if (focusmodel != null) {
							if (focusmodel.getUsers() != null) {

								final List<ImageView> allImages = new ArrayList<ImageView>();
								List<UsersList> all = new ArrayList<UsersList>();
								for (int i = 0; i < focusmodel.getUsers().size(); i++) {
									UsersList user = focusmodel.getUsers().get(i);
									if(i<6){
										recommendUserItem = inflater.inflate(R.layout.recommend_header_item, null, false);
										RoundNetworkImageView avatar_label = (RoundNetworkImageView) recommendUserItem.findViewById(R.id.thumb_nail);
										TextView timeline_user_name = (TextView) recommendUserItem.findViewById(R.id.username);
										ImageView imagebutton = (ImageView) recommendUserItem.findViewById(R.id.recommendImageview);

										if(i<5){
											ImageLoader.getInstance().displayImage(user.getAvatar(), avatar_label,
													GlobalVarable.getDefaultAvatarDisplayOptions(false));
											timeline_user_name.setText(user.getName());
											final UsersList finalUser= user;
											avatar_label.setOnClickListener(new OnClickListener() {
												@Override
												public void onClick(View v) {
													Intent intent = new Intent();
													intent.setClass(getActivity(), UserActivity.class);
													intent.putExtra("user_id", finalUser.getId());
													intent.putExtra(UserActivity.JD_USER_NAME, finalUser.getJd_user_name());
													getRootFragment().startActivityForResult(intent,GOTO_RECOMMANDED_USER_ACTIVITY);
												}
											});
										}else{
											
											avatar_label.setImageResource(R.drawable.recommend_all_bt);
											timeline_user_name.setText("全部");
											imagebutton.setVisibility(View.GONE);
											avatar_label.setOnClickListener(new OnClickListener() {
												
												@Override
												public void onClick(View arg0) {
													Intent intent = new Intent(getActivity(), RecommendActivity.class);
													getRootFragment().startActivityForResult(intent,GOTO_RECOMMANDED_USER_ACTIVITY);
												}
											});
										}
										initFollowModel(user,imagebutton);
										allImages.add(imagebutton);
										recommendContainer.addView(recommendUserItem);
									}
									all.add(user);
								}
								recommendUsers.clear();
								recommendUsers.addAll(all);
								//一键关注
								recommendAll.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View arg0) {
										if (recommendUsers != null) {
											String ids = recommendUsers.get(0).getId();
											for (int i = 1; i < recommendUsers.size(); i++) {
												ids += "," + recommendUsers.get(i).getId();
											}
											Follow(ids,allImages,new UpdateStatusInterface() {
												@Override
												public void success() {
													for (int i = 0; i < allImages.size(); i++) {
														allImages.get(i).setImageResource(R.drawable.recommended_bt_bg);
														recommendUsers.get(i).getRelation_with_current_user().setFollowing(true);
													}
												}
												@Override
												public void fail() {
												}
											});
										}
									}
								});
							}
							
							unfollow_count = focusmodel.getUnfollow_count();
							myHandler.sendMessage(myHandler.obtainMessage(TimeLineModel.RECOMMEND));
						} else
							Toast.makeText(getActivity(),
									getString(R.string.network_connect_error),
									Toast.LENGTH_LONG).show();
					}
		});
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
	String jd_user_name;
	private void initFollowModel(final UsersList user,final ImageView imagebutton) {
		jd_user_name = user.getJd_user_name();
		resetButton(user.getRelation_with_current_user(),imagebutton);
		imagebutton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (null == user.getRelation_with_current_user() ) {
					return;
				}
				
				if (user.getRelation_with_current_user().isFollowing()) {
					final List<ImageView> iamges= new ArrayList<ImageView>();
					iamges.add(imagebutton);
					UNFollow(user.getId(),iamges,new UpdateStatusInterface() {
						@Override
						public void success() {
							for (int i = 0; i < iamges.size(); i++) {
								iamges.get(i).setImageResource(R.drawable.unrecommend_bt_bg);
							}
							user.getRelation_with_current_user().setFollowing(false);
						}
						@Override
						public void fail() {
						}
					});
				}else{
					final List<ImageView> iamges= new ArrayList<ImageView>();
					iamges.add(imagebutton);
					Follow(user.getId(),iamges,new UpdateStatusInterface() {
						@Override
						public void success() {
							for (int i = 0; i < iamges.size(); i++) {
								iamges.get(i).setImageResource(R.drawable.recommended_bt_bg);
							}
							user.getRelation_with_current_user().setFollowing(true);
						}
						@Override
						public void fail() {
						}
					});
				}
					
			}
		});
	}

	public void resetButton(final Relation_with_current_user focusModel,ImageView imagebutton) {
		if(focusModel==null || imagebutton == null)
			return ;

		if (focusModel.isFollowing()) {
			if(null != imagebutton) {
				if (focusModel.isFollowed()) {
					imagebutton
							.setImageResource(R.drawable.btn_bothfollowing);
				} else {
					imagebutton
							.setImageResource(R.drawable.recommended_bt_bg);
				}
			}
		} else {
			if(null != imagebutton) {
				imagebutton.setImageResource(R.drawable.unrecommend_bt_bg);
			}
		}
		
		if (LoginUser.getpin().equals(jd_user_name)) {
			if(null != imagebutton) {
				imagebutton
				.setImageResource(R.drawable.icon_arrow_right);
				imagebutton.setEnabled(false);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void Follow(String id,final List<ImageView> imagebuttons,final UpdateStatusInterface updateInterface) {
		WebRequestHelper.post(URLText.Follow_SomeOne_URL,
				RequestParamsPool.getFollowSomeParams(id),
				new MyAsyncHttpResponseHandler(getActivity()) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						ToastUtil.showToastInThread(getString(R.string.network_connect_error), Toast.LENGTH_SHORT);
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
									updateInterface.success();
									Toast.makeText(getActivity(), message, 1).show();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}
	
	private void UNFollow(String id,final List<ImageView> imagebuttons,final UpdateStatusInterface updateInterface) {
		WebRequestHelper.post(URLText.Follow_Cancle,
				RequestParamsPool.getUNFollowParams(id),
				new MyAsyncHttpResponseHandler(getActivity()) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						ToastUtil.showToastInThread(getString(R.string.network_connect_error), Toast.LENGTH_SHORT);
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
									updateInterface.success();
									Toast.makeText(getActivity(), message, 1).show();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}
	
	private interface UpdateStatusInterface{
		public void success();
		public void fail();
	}

	private boolean timeAdapter;
	private boolean hideBottom;
	private ParserCreator parserCreator;
	private FocusModel focusmodel;
	private EmptyLayout mEmptyLayout;
//	private PostTweetInterface postTweetInterface;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		timeAdapter = getArguments().getBoolean(TIMELINE_ADAPTER);
		hideBottom = getArguments().getBoolean(HIDE_BOTTOM);
		loadingMore = getArguments().getBoolean(ENABLE_LOADING_MORE, true);
		pullRefresh = getArguments().getBoolean(ENABLE_PULL_REFRESH, true);
		searchable = getArguments().getBoolean(SEARCHABLE);
		index = getArguments().getInt(FriendCircleFragment.INDEX, DEFAULT_INDEX);
		parserCreator = getArguments().getParcelable(PARSER_CREATOR);

		executorService = NotificationService.getExecutorService();
		listView.addFooterView(UiStaticMethod.getFooterParent(getActivity(), loading));
		timeLine = new TimeLineModel(timeAdapter, parserCreator);
		timeLine.addObserver(this);
		myHandler = new TimeLineFragmentHandler(this);

		if (timeAdapter) {
			adapter = new TimeLineAdapter(getActivity(), timeLine, hideBottom,getRootFragment());
		} else
			adapter = new MessageItemAdapter(getActivity(), timeLine);
		listView.setAdapter(adapter);
		
		if (!NetWorkUtils.isNetworkConnected(getActivity())) {
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
		}else if(LoginUser.isLogin()){
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
			ishaveHeaderView = false;
			submitinit();
			initRecommendLayout();
		}else {
			mEmptyLayout.setErrorType(EmptyLayout.NOT_LOGIN);
		}
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser){
			if(null != timeLine) {
				if(timeLine.getLength() == 0) {
					if (!NetWorkUtils.isNetworkConnected(getActivity())) {
						mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
					}else if(LoginUser.isLogin()){
						mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
						ishaveHeaderView = false;
						submitinit();
						initRecommendLayout();
					}else {
						mEmptyLayout.setErrorType(EmptyLayout.NOT_LOGIN);
					}
				}
				else{
					if (!NetWorkUtils.isNetworkConnected(getActivity())) {
					}else if(LoginUser.isLogin()){
						mSwipeLayout.setRefreshing(true);
						submitinit();
					}
				}
			}
			
			TCAgent.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_friendcircle));
			StatService.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_friendcircle));
		}else{
			TCAgent.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_friendcircle));
			StatService.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_friendcircle));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		//从详情页返回，更新列表数据
		case TimelineTweetActivity.START_TWEET_FROM_QUOTE:
		case START_ACTIVITY_FROM_LIST:
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
						Bundle bundle=new Bundle();
						bundle.putInt("UPDATE_VIEWREMMENDED", 1);
						bundle.putBoolean(TweetModel.VIEWERRECOMMENDED, data.getBooleanExtra(TweetModel.VIEWERRECOMMENDED, false));
						message.setData(bundle);
						if (message.arg1 == commentNumber && message.arg2 == recommendCount && data.getBooleanExtra(TweetModel.VIEWERRECOMMENDED, false) == entity.isViewerRecommended())
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
				Bundle bundle = data.getExtras();
				Message message1 = Message.obtain();
				message1.obj = bundle.getString(TimelineCommentsActivity.ENTITY_GUID);
				message1.what = TimeLineModel.UPDATE_COMMENTS_NUMBER;
				int index = timeLine.indexOf((String) message1.obj);
				Entity entity = timeLine.getEntityAt(index);
				int commentNumber = entity.getCommentNumber();
				int recommendCount = entity.getRecommendsCount();
				if (bundle.getBoolean(TimelineCommentsActivity.IS_COMMENT) || bundle.getBoolean(TimelineCommentsActivity.CHECKED)){
					message1.arg1 = commentNumber+1;
					message1.arg2 = recommendCount;
					Bundle bundle1=new Bundle();
					bundle1.putInt("UPDATE_VIEWREMMENDED", 0);
					message1.setData(bundle1);
				}
				else{
				}
				myHandler.sendMessage(message1);

				if(bundle.getBoolean(TimelineCommentsActivity.CHECKED))
					executorService.execute(new TimelineFragmentRunnable(TimeLineModel.LOAD_NEW));
				
			}
			break;
		//发布动态
		case POST_TWEET:
			executorService.execute(new TimelineFragmentRunnable(TimeLineModel.LOAD_NEW));
			break;
		case GOTO_RECOMMANDED_USER_ACTIVITY:
			recommendContainer.removeAllViews();
			initRecommendLayout();
			break;
		case LOGIN:
			if(LoginUser.isLogin()) {
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
				ishaveHeaderView = false;
				submitinit();
				initRecommendLayout();
			}
			break;
		}
	}
	
	@Override
	public void onDestroyView() {
		timeLine.deleteObserver(this);
		model.deleteObserver(this);
		myHandler.removeCallbacksAndMessages(null);
		super.onDestroyView();
	}

	@Override
	public void update(Observable observable, Object data) {
		Message message = (Message) data;
		myHandler.sendMessage(message);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (searchView != null)
			searchView.clearFocus();
		if (ishaveHeaderView) {
			adapterCount = adapter.getCount();
		} else {
			adapterCount = adapter.getCount() - 1;
		}
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItemIndex == adapterCount) {
			if ((moreLoad == null || moreLoad.isDone()) && !isLoading) {
				isLoading=true;
				loading.setVisibility(View.VISIBLE);
				if (searchable)
					moreLoad = executorService.submit(new TimelineFragmentRunnable(TimeLineModel.LOAD_MORE, currentQuery));
				else
					moreLoad = executorService.submit(new TimelineFragmentRunnable(TimeLineModel.LOAD_MORE));
			}

		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		lastItemIndex = firstVisibleItem + visibleItemCount - 1 - 1;
		if(firstVisibleItem >= 1) {
			if(listView.getHeaderViewsCount() > 0) {
				ishaveHeaderView = false;
				listView.removeHeaderView(headerView);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Entity entity = null;
		if (position <= adapter.getCount()) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			if (ishaveHeaderView) {
				entity = timeLine.getEntityAt(position - 1);
			} else {
				entity = timeLine.getEntityAt(position);
			}
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
	 * 进入动态详情页
	 */
	private void startTweetActivity(Intent intent, Entity entity) {
		String guid = entity.getGuid();
		if (UiStaticMethod.isNullString(guid))
			guid = entity.getRenderBody().getEntity().getGuid();
		intent.putExtra(TimelineTweetActivity.TWEET_GUID, guid);
		intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, entity.getUser().getName());

		intent.setClass(getActivity(), TimelineTweetActivity.class);
		getRootFragment().startActivityForResult(intent, START_ACTIVITY_FROM_LIST);
	}

	/**
	 * 删除一条动态
	 * 
	 * @param message
	 *            一条消息
	 * @param data
	 *            一个包含有待删除动态的ID的Intent
	 */
	private void deleteEntity(Message message, Intent data) {
		message.what = TimeLineModel.DELETE_ENTITY;
		message.arg1 = ObservableModel.SUCCESS_INT;
		message.obj = data.getStringExtra(TimelineTweetActivity.DELETE_ENTITY_ID);
		myHandler.sendMessage(message);
	}

	/**
	 * 下拉刷新
	 */
	@Override
	public void onRefresh() {
		executorService.execute(new TimelineFragmentRunnable(TimeLineModel.LOAD_INIT));
	}

	/**
	 * 写书评or随便说说
	 */
	@Override
	public void onPopupWindowItemClick(String type, int position) {
	}

	@Override
	public void onPopupWindowSubmenuItemCheck(String type, int checkid) {

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
	public Fragment getRootFragment() {
		  Fragment fragment = getParentFragment();
		  if(fragment!=null){
			  while (fragment.getParentFragment() != null) {
				   fragment = fragment.getParentFragment();
				  }
		  }
		  return fragment;

		 }
}
