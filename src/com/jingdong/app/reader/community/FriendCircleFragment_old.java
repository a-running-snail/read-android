//package com.jingdong.app.reader.community;
//
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Observable;
//import java.util.Observer;
//import java.util.concurrent.Future;
//import java.util.concurrent.ScheduledExecutorService;
//
//import org.apache.http.Header;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.annotation.Nullable;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.AbsListView;
//import android.widget.AbsListView.OnScrollListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.BaseAdapter;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.RelativeLayout;
//import android.widget.SearchView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.jingdong.app.reader.R;
//import com.jingdong.app.reader.activity.LoginActivity;
//import com.jingdong.app.reader.activity.MZBookApplication;
//import com.jingdong.app.reader.activity.TimelineRootFragment;
//import com.jingdong.app.reader.bookstore.search.SearchActivity;
//import com.jingdong.app.reader.common.CommonFragment;
//import com.jingdong.app.reader.entity.extra.FocusModel;
//import com.jingdong.app.reader.me.activity.UserActivity;
//import com.jingdong.app.reader.me.fragment.UserFragment;
//import com.jingdong.app.reader.message.activity.MessageActivity;
//import com.jingdong.app.reader.message.adapter.MessageItemAdapter;
//import com.jingdong.app.reader.message.model.Alert;
//import com.jingdong.app.reader.message.model.Notification;
//import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
//import com.jingdong.app.reader.net.RequestParamsPool;
//import com.jingdong.app.reader.net.URLText;
//import com.jingdong.app.reader.net.WebRequestHelper;
//import com.jingdong.app.reader.net.url.PagedBasedUrlGetter;
//import com.jingdong.app.reader.parser.ParserCreator;
//import com.jingdong.app.reader.service.NotificationService;
//import com.jingdong.app.reader.timeline.actiivity.RecommendActivity;
//import com.jingdong.app.reader.timeline.actiivity.TimelineBookListActivity;
//import com.jingdong.app.reader.timeline.actiivity.TimelineBookListCommentsActivity;
//import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
//import com.jingdong.app.reader.timeline.actiivity.TimelinePostTweetActivity;
//import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
//import com.jingdong.app.reader.timeline.adapter.TimeLineAdapter;
//import com.jingdong.app.reader.timeline.model.ObservableModel;
//import com.jingdong.app.reader.timeline.model.TimeLineModel;
//import com.jingdong.app.reader.timeline.model.TimelineActivityModel;
//import com.jingdong.app.reader.timeline.model.TweetModel;
//import com.jingdong.app.reader.timeline.model.core.Entity;
//import com.jingdong.app.reader.user.LocalUserSetting;
//import com.jingdong.app.reader.user.LoginUser;
//import com.jingdong.app.reader.util.GsonUtils;
//import com.jingdong.app.reader.util.MZLog;
//import com.jingdong.app.reader.util.NetWorkUtils;
//import com.jingdong.app.reader.util.UiStaticMethod;
//import com.jingdong.app.reader.view.EmptyLayout;
//import com.jingdong.app.reader.view.TopBarPopupWindow;
//import com.jingdong.app.reader.view.TopBarView;
//import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
//
//public class FriendCircleFragment_old extends CommonFragment implements SwipeRefreshLayout.OnRefreshListener, TopBarViewListener, Observer, OnScrollListener,
//		OnItemClickListener, TopBarPopupWindow.onPopupWindowItemClickListener {
//	private static final int NET_ERROR = 404;
//
//	private class TimeLineRunnable implements Runnable {
//		private int type;
//
//		public TimeLineRunnable(int type) {
//			this.type = type;
//		}
//
//		@Override
//		public void run() {
//			switch (type) {
//			case TimelineActivityModel.POST_TWEET:
//				MZLog.d("wangguodong", "========== TimelineActivityModel.POST_TWEET");
//				model.postTweet(getActivity(), postTweetBundle);
//				break;
//			}
//		}
//	}
//
//	public FriendCircleFragment_old() {
//		super();
//		// TODO Auto-generated constructor stub
//	}
//
//	@Override
//	public void onDestroy() {
//		model.deleteObserver(this);
//
//		MZLog.d("life-cycle", "社区onDestroy");
//
//		super.onDestroy();
//	}
//
//	private void submitinit() {
//		executorService.submit(new TimelineFragmentRunnable(TimeLineModel.LOAD_NEW));
//	}
//
//	/**
//	 * 该类负责更新listview中的内容
//	 * 
//	 * @author Alexander
//	 * 
//	 */
//	private static class TimeLineFragmentHandler extends Handler {
//		WeakReference<FriendCircleFragment_old> tWeakReference;
//
//		public TimeLineFragmentHandler(FriendCircleFragment_old fragment) {
//			tWeakReference = new WeakReference<FriendCircleFragment_old>(fragment);
//		}
//
//		@Override
//		public void handleMessage(Message msg) {
//			FriendCircleFragment_old fragment = tWeakReference.get();
//			if (fragment != null) {
//				switch (msg.what) {
//				case TimeLineModel.RECOMMEND:
//					if (Integer.parseInt(fragment.unfollow_count) > 0 && Integer.parseInt(fragment.count) < 5) {
//						ishaveHeaderView = true;
//						fragment.listView.addHeaderView(fragment.headerView);
//					} else {
//						ishaveHeaderView = false;
//						fragment.listView.removeHeaderView(fragment.headerView);
//					}
//					fragment.submitinit();
//					break;
//				case TimeLineModel.LOAD_INIT:
//					if (fragment.loadingMore)
//						fragment.listView.setOnScrollListener(fragment);
//					fragment.listView.setOnItemClickListener(fragment);
//					fragment.loading.setVisibility(View.GONE);
//					// fragment.screenLoading.setVisibility(View.GONE);
//					fragment.mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
//					mSwipeLayout.setRefreshing(false);
//					if (fragment.pullRefresh) {
//						// ActionBarPullToRefresh.from(fragment.getActivity())
//						// .options(Options.create().scrollDistance(UiStaticMethod.SCROLL_DISTANCE).build())
//						// .allChildrenArePullable().listener(fragment).setup(fragment.pullToRefreshLayout);
//						// fragment.pullToRefreshLayout.setRefreshComplete();
//						mSwipeLayout.setRefreshing(false);
//					}
//					iniLoad(msg, fragment);
//					break;
//				case TimeLineModel.RELOAD:
//					fragment.loading.setVisibility(View.GONE);
//					// fragment.screenLoading.setVisibility(View.GONE);
//					fragment.mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
//					if (msg.arg1 == ObservableModel.SUCCESS_INT)
//						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
//							fragment.timeLine.refreshData();
//							fragment.adapter.notifyDataSetChanged();
//						}
//					break;
//				case TimeLineModel.LOAD_AS_INPUT:
//					fragment.loading.setVisibility(View.GONE);
//					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
//						fragment.timeLine.refreshData();
//						fragment.adapter.notifyDataSetInvalidated();
//					} else
//						Toast.makeText(fragment.getActivity(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
//					break;
//				case TimeLineModel.LOAD_MORE:
//					fragment.loading.setVisibility(View.GONE);
//					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
//						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
//							fragment.timeLine.refreshData();
//							fragment.adapter.notifyDataSetChanged();
//						} else {
//							Toast.makeText(fragment.getActivity(), R.string.user_no_more, Toast.LENGTH_SHORT).show();
//						}
//					} else
//						Toast.makeText(fragment.getActivity(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
//					break;
//				case TimeLineModel.LOAD_NEW:
//					fragment.loading.setVisibility(View.GONE);
//					// fragment.screenLoading.setVisibility(View.GONE);
//					fragment.mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
//					if (fragment.pullRefresh) {
//						Notification.getInstance().setReadNormalCount(fragment.getActivity());
//						if (msg.arg1 == ObservableModel.SUCCESS_INT) {
//							fragment.timeLine.refreshData();
//							fragment.adapter.notifyDataSetChanged();
//						}
//						mSwipeLayout.setRefreshing(false);
//					}
//					break;
//				case TimeLineModel.DELETE_ENTITY:
//					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
//						String guid = (String) msg.obj;
//						if (fragment.timeLine.delete(guid))
//							fragment.adapter.notifyDataSetChanged();
//					}
//					break;
//				case TimeLineModel.UPDATE_COMMENTS_NUMBER:
//					int index = fragment.timeLine.indexOf((String) msg.obj);
//					Entity entity = fragment.timeLine.getEntityAt(index);
//					entity.setCommentNumber(msg.arg1);
//					entity.setRecommendsCount(msg.arg2);
//					fragment.adapter.notifyDataSetChanged();
//					break;
//				case TimeLineModel.EMPTY_INPUT:
//					fragment.timeLine.clear();
//					fragment.loading.setVisibility(View.GONE);
//					fragment.adapter.notifyDataSetInvalidated();
//					break;
//				case TimelineActivityModel.POST_TWEET_NEW:
//					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
//						if (fragment.executorService != null) {
//							fragment.executorService.submit(fragment.new TimelineFragmentRunnable(TimeLineModel.LOAD_NEW));
//						}
//					}
//					break;
//				case NET_ERROR:
//					fragment.mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
//					
//					break;
//				}
//			}
//		}
//
//		/**
//		 * 初始化ListView
//		 * 
//		 * @param msg
//		 *            消息体
//		 * @param fragment
//		 *            待设置的Fragment.
//		 */
//		private void iniLoad(Message msg, FriendCircleFragment_old fragment) {
//			if (msg.arg1 == ObservableModel.SUCCESS_INT) {
//
//				if (msg.arg2 == ObservableModel.SUCCESS_INT) {
//					fragment.timeLine.refreshData();
//					fragment.adapter.notifyDataSetChanged();
//				} else {
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
//				}
//			} else {
//				Toast.makeText(fragment.getActivity(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
//			}
//		}
//	}
//
//	/**
//	 * 该类负责异步获取并解析timeline.json
//	 * 
//	 * @author Alexander
//	 * 
//	 */
//	public class TimelineFragmentRunnable implements Runnable {
//		private int type;
//		private String query;
//
//		public TimelineFragmentRunnable(int type) {
//			this(type, null);
//		}
//
//		public TimelineFragmentRunnable(int type, String query) {
//			this.type = type;
//			this.query = query;
//		}
//
//		@Override
//		public void run() {
//			Context context = getActivity();
//			switch (type) {
//			case TimeLineModel.RELOAD:
//			case TimeLineModel.LOAD_INIT:
//				currentpage = 1;
//				if (!NetWorkUtils.isNetworkConnected(getActivity())) {
//					myHandler.sendEmptyMessage(NET_ERROR);
//				} else {
//					timeLine.loadEntities(context, type, currentpage, pagecount, "", "",LocalUserSetting.getRecommend_Guid(context, LoginUser.getpin()), 0);
//				}
//				break;
//			case TimeLineModel.LOAD_MORE:
//				if (query != null) {
//				} else {
//					// timeLine.loadEntities(context, type,
//					// timeLine.getEntityAt(timeLine.getLength() - 1).getGuid(),
//					// timeLine.getEntityAt(timeLine.getLength() - 1).getId());
//					currentpage++;
//					timeLine.loadEntities(context, type, currentpage, pagecount, timeLine.getEntityAt(timeLine.getLength() - 1).getGuid(), "", "", 0);
//				}
//				break;
//			case TimeLineModel.LOAD_NEW:
//				currentpage = 1;
//				if (pullRefresh) {
//					if (timeLine.getLength() == 0) {
//						executorService.execute(new TimelineFragmentRunnable(TimeLineModel.LOAD_INIT));
//					} else {
//						if (!NetWorkUtils.isNetworkConnected(getActivity())) {
//							myHandler.sendEmptyMessage(NET_ERROR);
//						} else {
//							timeLine.loadEntities(context, type, currentpage, pagecount, "",
//									LocalUserSetting.getCommunity_Since_Guid(context, LoginUser.getpin()),
//									LocalUserSetting.getRecommend_Guid(context, LoginUser.getpin()), 0);
//						}
//					}
//					// timeLine.loadEntities(context, type,
//					// timeLine.getEntityAt(0).getGuid(),
//					// UiStaticMethod.ILLEGAL_INDEX);
//				}
//				break;
//			}
//		}
//	}
//
//	/**
//	 * 对应于一个boolean变量，默认值为false，目前只有在消息-通知中调用Timeline的时候需要把这个变量设为false。
//	 * 对应的boolean变量如果为true，表示使用TimelineAdapter；如果为false，则使用MessageItemApdater。
//	 */
//	public final static String TIMELINE_ADAPTER = "timelineAdapter";
//	/**
//	 * 对应于一个boolean变量，默认值为false。对应的boolean变量如果为true，表示隐藏Timeline中每一项下方的转发和评论按钮；
//	 * 如果为false，则显示Timeline中每一项下方的转发和评论按钮。
//	 */
//	public final static String HIDE_BOTTOM = "hide_bottom";
//	/**
//	 * 对应于一个boolean变量，默认值为false，目前只有在主页-动态中调用Timeline的时候需要把这个变量设为true。
//	 * 对应的boolean变量如果为true， 表示支持顶部下拉刷新功能；如果为false，顶部下拉刷新自动被禁用。
//	 */
//	public final static String ENABLE_PULL_REFRESH = "pullrefresh";
//	/**
//	 * 对应于一个boolean变量，默认值为true，目前只有在精选中需要把这个参数设置为false。对应的boolean变量如果为true，
//	 * 则支持底部下拉加载更多；如果为false，则禁用底部下拉加载更多。
//	 */
//	public final static String ENABLE_LOADING_MORE = "loadingMore";
//	/**
//	 * 对应于一个boolean变量,默认值为false，目前只有在SearchActivity中调用Timeline的时候需要把这个变量是为true。
//	 * 对应的变量如果为true，表示启动搜索功能，当前Fragment会自动切换到搜索模式；如果为false，则关闭搜索功能。
//	 */
//	public final static String SEARCHABLE = "search";
//	/**
//	 * 对应于一个int变量,默认值为TimelineFragment.DEFAULT_INDEX，这个变量为一个过期的变量，即将被删除，不需要传入，
//	 * 目前只有在MessageItemAdapter中被使用。这个变量表示当前Fragmet处于Adatper中的第几位，请勿提供这个参数。
//	 */
//	public final static String INDEX = "index";
//	/**
//	 * 对应于ParserCreator对象，这个参数必须传入。否则TimelineFragement无法获取URL并解析成JSON。
//	 */
//	public final static String PARSER_CREATOR = "parserCreator";
//	public final static long RELOAD_DELAY_MS = 5000;
//	public final static int START_ACTIVITY_FROM_LIST = 100;
//	private final static int DEFAULT_INDEX = -1;
//	private boolean loadingMore;
//	private boolean pullRefresh;
//	private boolean searchable;
//	private int index;
//	private int lastItemIndex;
//	private int searchPage = PagedBasedUrlGetter.FIRST_PAGE;
//	private String currentQuery;
//	private TimeLineModel timeLine;
//	private ListView listView;
//	private BaseAdapter adapter;
//	private Handler myHandler;
//	private View loading;
//	// private View screenLoading;
//	private SearchView searchView;
//	private Future<?> initLoad;
//	private Future<?> moreLoad;
//	private ScheduledExecutorService executorService;
//	private TopBarView topBarView = null;
//	private View layout;
//	private int currentpage = -1;
//	private int pagecount = 20;
//	private static SwipeRefreshLayout mSwipeLayout;
//	private TopBarPopupWindow rightPopupWindow = null;
//
//	public final static int POST_TWEET = 101;
//	private Bundle postTweetBundle;
//
//	private TimelineActivityModel model;
//	private RelativeLayout relativeLayout;
//	private LinearLayout linearLayout;
//	private ImageView icon;
//	private TextView text;
//	private Button emptybutton;
//	private static boolean isLogin = false;
//	private View headerView;
//	private ImageView clear_image;
//	private RelativeLayout recommend_layout;
//	private String unfollow_count;
//	private String count;
//	private static boolean ishaveHeaderView = false;
//	private int adapterCount = -1;
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
//		MZLog.d("life-cycle", "社区onCreateView");
//
//		View rootView = inflater.inflate(R.layout.activity_timeline, null);
//		topBarView = (TopBarView) rootView.findViewById(R.id.topbar);
//
//		LinearLayout temp = (LinearLayout) rootView.findViewById(R.id.container);
//		layout = inflater.inflate(R.layout.fragment_timeline, temp);
//		fragmentTag = "TimelineFragment";
//		count = LocalUserSetting.getRecommend_count(getActivity(), LoginUser.getpin());
//		LocalUserSetting.saveRecommend_count(getActivity(), Integer.parseInt(LocalUserSetting.getRecommend_count(getActivity(), LoginUser.getpin())) + 1 + "",
//				LoginUser.getpin());
//		loading = inflater.inflate(R.layout.list_cell_footer, null, false);
//		headerView = inflater.inflate(R.layout.recommend_header, null);
//		recommend_layout = (RelativeLayout) headerView.findViewById(R.id.recommend_layout);
////		clear_image = (ImageView) headerView.findViewById(R.id.clear_image);
//		mEmptyLayout = (EmptyLayout) layout.findViewById(R.id.error_layout);
//		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if (EmptyLayout.NETWORK_ERROR == mEmptyLayout.getErrorState()) {
//					mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
//					submitinit();
//				}
//			}
//		});
//		listView = (ListView) layout.findViewById(R.id.timeline_list);
//		mSwipeLayout = (SwipeRefreshLayout) layout.findViewById(R.id.ptr_layout);
//		mSwipeLayout.setOnRefreshListener(this);
//		mSwipeLayout.setColorScheme(R.color.red_main, R.color.bg_main, R.color.red_sub, R.color.bg_main);
//		linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
//		relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativeLayout);
//		icon = (ImageView) rootView.findViewById(R.id.icon);
//		text = (TextView) rootView.findViewById(R.id.text);
//		emptybutton = (Button) rootView.findViewById(R.id.empty);
//		List<String> rightItemSubmenu = new ArrayList<String>();
//		rightItemSubmenu.add("写书评");
//		rightItemSubmenu.add("随便说说");
//
//		rightPopupWindow = new TopBarPopupWindow(getActivity(), rightItemSubmenu, "101");
//		rightPopupWindow.setListener(this);
//
//		model = new TimelineActivityModel();
//		model.addObserver(this);
//
//		recommend_layout.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Intent intent = new Intent(getActivity(), RecommendActivity.class);
//				startActivity(intent);
//				MZBookApplication.alreadyShowRecomment = true;
//				listView.removeHeaderView(headerView);
//				ishaveHeaderView = false;
//			}
//		});
//
//		clear_image.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				MZBookApplication.alreadyShowRecomment = true;
//				listView.removeHeaderView(headerView);
//				ishaveHeaderView = false;
//			}
//		});
//		emptybutton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				if (!LoginUser.isLogin()) {
//					isLogin = true;
//					Intent intent = new Intent(getActivity(), LoginActivity.class);
//					startActivity(intent);
//				} else {
//					Intent intent = new Intent(getActivity(), RecommendActivity.class);
//					startActivity(intent);
//				}
//			}
//		});
//
//		return rootView;
//	}
//
//	public void initTopbarView() {
//		if (getActivity() == null || topBarView == null)
//			return;
//		List<String> item = new ArrayList<String>();
//		item.add("社区");
//		if (LoginUser.isLogin()) {
//			topBarView.setLeftMenuVisiable(true, R.drawable.topbar_search);
//			topBarView.setRightMenuOneVisiable(true, R.drawable.btn_bar_compose, false);
//		} else {
//			relativeLayout.setVisibility(View.GONE);
//			linearLayout.setVisibility(View.VISIBLE);
//			icon.setBackgroundResource(R.drawable.icon_empty);
//			text.setText("登录京东账号后即可查看社区精彩内容");
//			emptybutton.setVisibility(View.VISIBLE);
//			emptybutton.setText("登录京东账号");
//		}
//		topBarView.setTitleItem(item);
//		topBarView.setListener(this);
//		topBarView.updateTopBarView();
//	}
//
//	private boolean timeAdapter;
//	private boolean hideBottom;
//	private ParserCreator parserCreator;
//	private FocusModel focusmodel;
//	private EmptyLayout mEmptyLayout;
//
//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		timeAdapter = getArguments().getBoolean(TIMELINE_ADAPTER);
//		hideBottom = getArguments().getBoolean(HIDE_BOTTOM);
//		loadingMore = getArguments().getBoolean(ENABLE_LOADING_MORE, true);
//		pullRefresh = getArguments().getBoolean(ENABLE_PULL_REFRESH, true);
//		searchable = getArguments().getBoolean(SEARCHABLE);
//		index = getArguments().getInt(FriendCircleFragment_old.INDEX, DEFAULT_INDEX);
//		parserCreator = getArguments().getParcelable(PARSER_CREATOR);
//
//		executorService = NotificationService.getExecutorService();
//		listView.addFooterView(UiStaticMethod.getFooterParent(getActivity(), loading));
//		timeLine = new TimeLineModel(timeAdapter, parserCreator);
//		timeLine.addObserver(this);
//		myHandler = new TimeLineFragmentHandler(this);
//
//		if (timeAdapter) {
//			adapter = new TimeLineAdapter(getActivity(), timeLine, hideBottom);
//		} else
//			adapter = new MessageItemAdapter(getActivity(), timeLine);
//		listView.setAdapter(adapter);
//
//	}
//
//	@Override
//	public void onResume() {
//		// TODO Auto-generated method stub
//
//		MZLog.d("life-cycle", "社区onResume");
//
//		super.onResume();
//		initTopbarView();
//		if (LoginUser.isLogin()) {
//			linearLayout.setVisibility(View.GONE);
//			relativeLayout.setVisibility(View.VISIBLE);
//			if (adapter != null) {
//				adapter.notifyDataSetChanged();
//			}
//
//			if (searchable) {
//				if (loadingMore)
//					listView.setOnScrollListener(this);
//				listView.setOnItemClickListener(this);
//				// screenLoading.setVisibility(View.GONE);
//				mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
//			} else {
//				listView.removeHeaderView(headerView);
//				// screenLoading.setVisibility(View.VISIBLE);
//				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
//				loading.setVisibility(View.GONE);
//
//
//				if (Integer.parseInt(count) < 5 && !MZBookApplication.alreadyShowRecomment) {
//					if (!NetWorkUtils.isNetworkConnected(getActivity())) {
//						mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
//						
//						return;
//					}
//					WebRequestHelper.get(URLText.Recommend_URL, RequestParamsPool.getRecommendUserParams(1 + "", 10 + ""), true,
//							new MyAsyncHttpResponseHandler(getActivity()) {
//
//								@Override
//								public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//									// TODO Auto-generated method stub
//									// screenLoading.setVisibility(View.GONE);
//									mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
//									ishaveHeaderView = false;
//									submitinit();
//								}
//
//								@Override
//								public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
//									// TODO Auto-generated method stub
//
//									String result = new String(responseBody);
//
//									Log.d("cj", "result=======>>" + result);
//									focusmodel = GsonUtils.fromJson(result, FocusModel.class);
//									if (focusmodel != null) {
//										unfollow_count = focusmodel.getUnfollow_count();
//										myHandler.sendMessage(myHandler.obtainMessage(TimeLineModel.RECOMMEND));
//									}
//								}
//							});
//				} else {
//					ishaveHeaderView = false;
//					submitinit();
//				}
//			}
//		}
//	}
//
//
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		switch (requestCode) {
//		case TimelineTweetActivity.START_TWEET_FROM_QUOTE:
//		case START_ACTIVITY_FROM_LIST:
//			Message message = Message.obtain();
//			switch (resultCode) {
//			case TimelineTweetActivity.DELETE_ENTITY:
//				deleteEntity(message, data);
//				break;
//			case TimelineTweetActivity.UPDATE_COMMENTS_NUMBER:
//				message.obj = data.getStringExtra(TimelineTweetActivity.TWEET_GUID);
//				if (data.getBooleanExtra(TweetModel.IS_DELETE, false)) {
//					deleteEntity(message, data);
//				} else {
//					message.what = TimeLineModel.UPDATE_COMMENTS_NUMBER;
//					int index = timeLine.indexOf((String) message.obj);
//					if (index < 0) {
//						return;
//					} else {
//						Entity entity = timeLine.getEntityAt(index);
//						int commentNumber = entity.getCommentNumber();
//						int recommendCount = entity.getRecommendsCount();
//						message.arg1 = data.getIntExtra(TweetModel.COMMENT_NUMBER, commentNumber);
//						message.arg2 = data.getIntExtra(TweetModel.RECOMMENTS_COUNT, recommendCount);
//						if (message.arg1 == commentNumber && message.arg2 == recommendCount)
//							message.recycle();
//						else
//							myHandler.sendMessage(message);
//					}
//				}
//				break;
//			}
//
//			break;
//
//		case POST_TWEET:
//			switch (resultCode) {
//			case TimelinePostTweetActivity.POST_TWEET_WORDS:
//				MZLog.d("wangguodong", "======post tweet data!!");
//				postTweetBundle = data.getExtras();
//				executorService.submit(new TimeLineRunnable(TimelineActivityModel.POST_TWEET));
//				break;
//			}
//			break;
//		}
//	}
//
//	@Override
//	public void onDestroyView() {
//
//		MZLog.d("life-cycle", "社区onDestroyView");
//		timeLine.deleteObserver(this);
//		model.deleteObserver(this);
//		myHandler.removeCallbacksAndMessages(null);
//		super.onDestroyView();
//	}
//
//	@Override
//	public void update(Observable observable, Object data) {
//		Message message = (Message) data;
//		myHandler.sendMessage(message);
//	}
//
//	@Override
//	public void onScrollStateChanged(AbsListView view, int scrollState) {
//		if (searchView != null)
//			searchView.clearFocus();
//		if (ishaveHeaderView) {
//			adapterCount = adapter.getCount();
//		} else {
//			adapterCount = adapter.getCount() - 1;
//		}
//		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItemIndex == adapterCount) {
//			if (moreLoad == null || moreLoad.isDone()) {
//				loading.setVisibility(View.VISIBLE);
//				if (searchable)
//					moreLoad = executorService.submit(new TimelineFragmentRunnable(TimeLineModel.LOAD_MORE, currentQuery));
//				else
//					moreLoad = executorService.submit(new TimelineFragmentRunnable(TimeLineModel.LOAD_MORE));
//			}
//
//		}
//	}
//
//	@Override
//	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//		lastItemIndex = firstVisibleItem + visibleItemCount - 1 - 1;
//	}
//
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		Entity entity = null;
//		if (position <= adapter.getCount()) {
//			Intent intent = new Intent();
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			if (ishaveHeaderView) {
//				entity = timeLine.getEntityAt(position - 1);
//			} else {
//				entity = timeLine.getEntityAt(position);
//			}
//			if (entity instanceof Alert) {
//				Alert alert = (Alert) entity;
//				if (alert.getLinkType() == Alert.USER_LINK) {
//					intent.putExtra(UserFragment.USER_ID, Long.parseLong(((Alert) entity).getLink()));
//					intent.setClass(getActivity(), UserActivity.class);
//					startActivity(intent);
//				} else if (alert.getLinkType() == Alert.ENTITY_LINK) {
//					startTweetActivity(intent, entity);
//				}
//			} else {
//				startTweetActivity(intent, entity);
//			}
//
//		}
//	}
//
//	/**
//	 * 删除列表中的数据，并重新加载网络上的新数据
//	 */
//	// public void update() {
//	// if (executorService != null) {
//	// if (initLoad != null && initLoad.isDone()) {
//	// timeLine.clear();
//	// adapter.notifyDataSetInvalidated();
//	// screenLoading.setVisibility(View.VISIBLE);
//	// initLoad = executorService.submit(new
//	// TimelineFragmentRunnable(TimeLineModel.RELOAD));
//	// }
//	// }
//	//
//	//
//	// }
//
//	/**
//	 * 启动一个TweetActivity
//	 * 
//	 * @param intent
//	 *            待设置的Intent
//	 * @param entity
//	 *            数据源
//	 */
//	private void startTweetActivity(Intent intent, Entity entity) {
//		String guid = entity.getGuid();
//		if (UiStaticMethod.isNullString(guid))
//			guid = entity.getRenderBody().getEntity().getGuid();
//		intent.putExtra(TimelineTweetActivity.TWEET_GUID, guid);
//		intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, entity.getUser().getName());
//
//		intent.setClass(getActivity(), TimelineTweetActivity.class);
//		startActivityForResult(intent, START_ACTIVITY_FROM_LIST);
//	}
//
//	/**
//	 * 删除一条动态
//	 * 
//	 * @param message
//	 *            一条消息
//	 * @param data
//	 *            一个包含有待删除动态的ID的Intent
//	 */
//	private void deleteEntity(Message message, Intent data) {
//		message.what = TimeLineModel.DELETE_ENTITY;
//		message.arg1 = ObservableModel.SUCCESS_INT;
//		message.obj = data.getStringExtra(TimelineTweetActivity.DELETE_ENTITY_ID);
//		myHandler.sendMessage(message);
//	}
//
//	@Override
//	public void onLeftMenuClick() {
//		// TODO Auto-generated method stub
//		Intent intent = new Intent(getActivity(), SearchActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(intent);
//	}
//
//	@Override
//	public void onRightMenuOneClick() {
//		if (rightPopupWindow != null) {
//			rightPopupWindow.show(topBarView);
//		}
//
//	}
//
//	@Override
//	public void onCenterMenuItemClick(int position) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void onRightMenuTwoClick() {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void onRefresh() {
//		// TODO Auto-generated method stub
//		executorService.execute(new TimelineFragmentRunnable(TimeLineModel.LOAD_NEW));
//	}
//
//	@Override
//	public void onPopupWindowItemClick(String type, int position) {
//		if (type.equals("101")) {
//
//			switch (position) {
//			case 0:
//				Intent it2 = new Intent(getActivity(), TimelineBookListCommentsActivity.class);
//				it2.putExtra("type", TimelineBookListActivity.type[3]);
//				startActivity(it2);
//				break;
//			case 1:
//
//				Intent it1 = new Intent(getActivity(), TimelinePostTweetActivity.class);
//				it1.putExtra("title", getString(R.string.timeline_post_title));
//				startActivityForResult(it1, TimelineRootFragment.POST_TWEET);
//				break;
//			}
//		}
//	}
//
//	@Override
//	public void onPopupWindowSubmenuItemCheck(String type, int checkid) {
//		// TODO Auto-generated method stub
//
//	}
//}
