package com.jingdong.app.reader.timeline.fragment;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.community.FriendCircleFragment.TimelineFragmentRunnable;
import com.jingdong.app.reader.community.square.SquareFragment;
import com.jingdong.app.reader.community.square.entity.SquareEntity;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.message.activity.MessageActivity;
import com.jingdong.app.reader.message.adapter.MessageItemAdapter;
import com.jingdong.app.reader.message.model.Alert;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.net.url.PagedBasedUrlGetter;
import com.jingdong.app.reader.parser.ParserCreator;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelinePostTweetActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.adapter.TimeLineAdapter;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.timeline.model.TimelineActivityModel;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;

@SuppressLint("InflateParams")
public class BaseTimeLineFragment extends CommonFragment implements
		SwipeRefreshLayout.OnRefreshListener, Observer, OnScrollListener,
		OnItemClickListener {

	private class TimeLineRunnable implements Runnable {
		private int type;

		public TimeLineRunnable(int type) {
			this.type = type;
		}

		@Override
		public void run() {
			switch (type) {
			case TimelineActivityModel.POST_TWEET:
				MZLog.d("wangguodong",
						"========== TimelineActivityModel.POST_TWEET");
				model.postTweet(getActivity(), postTweetBundle);
				break;
			}
		}
	}

	@Override
	public void onDestroy() {
		model.deleteObserver(this);
		super.onDestroy();
	}

	/**
	 * 该类负责更新listview中的内容
	 * 
	 * @author Alexander
	 * 
	 */
	private static class TimeLineFragmentHandler extends Handler {
		WeakReference<BaseTimeLineFragment> tWeakReference;

		public TimeLineFragmentHandler(BaseTimeLineFragment fragment) {
			tWeakReference = new WeakReference<BaseTimeLineFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			BaseTimeLineFragment fragment = tWeakReference.get();
			if (fragment != null) {
				switch (msg.what) {
				case TimeLineModel.LOAD_INIT:
					if (fragment.loadingMore)
						fragment.listView.setOnScrollListener(fragment);
					fragment.listView.setOnItemClickListener(fragment);
					fragment.loading.setVisibility(View.GONE);
					fragment.screenLoading.setVisibility(View.GONE);
					if (fragment.pullRefresh) {
						// ActionBarPullToRefresh.from(fragment.getActivity())
						// .options(Options.create().scrollDistance(UiStaticMethod.SCROLL_DISTANCE).build())
						// .allChildrenArePullable().listener(fragment).setup(fragment.pullToRefreshLayout);
						// fragment.pullToRefreshLayout.setRefreshComplete();
						mSwipeLayout.setRefreshing(false);
						fragment.isRefreshing =false;
					}
					iniLoad(msg, fragment);
					break;
				case TimeLineModel.RELOAD:
					fragment.loading.setVisibility(View.GONE);
					fragment.screenLoading.setVisibility(View.GONE);
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
						Toast.makeText(fragment.getActivity(),
								R.string.loading_fail, Toast.LENGTH_SHORT)
								.show();
					break;
				case TimeLineModel.LOAD_MORE:
					fragment.loading.setVisibility(View.GONE);
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
							fragment.timeLine.refreshData();
							fragment.adapter.notifyDataSetChanged();
						} else {
							Toast.makeText(fragment.getActivity(),
									R.string.user_no_more, Toast.LENGTH_SHORT)
									.show();
						}
					} else
						Toast.makeText(fragment.getActivity(),
								R.string.loading_fail, Toast.LENGTH_SHORT)
								.show();
					break;
				case TimeLineModel.LOAD_NEW:
					if (fragment.pullRefresh) {
						Notification.getInstance().setReadNormalCount(
								fragment.getActivity());
						if (msg.arg1 == ObservableModel.SUCCESS_INT) {
							fragment.timeLine.refreshData();
							fragment.adapter.notifyDataSetChanged();
						}
						mSwipeLayout.setRefreshing(false);
						fragment.isRefreshing =false;
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
					fragment.adapter.notifyDataSetChanged();
					break;
				case TimeLineModel.EMPTY_INPUT:
					fragment.timeLine.clear();
					fragment.loading.setVisibility(View.GONE);
					fragment.adapter.notifyDataSetInvalidated();
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
		private void iniLoad(Message msg, BaseTimeLineFragment fragment) {
			if (msg.arg1 == ObservableModel.SUCCESS_INT) {
				Notification.getInstance().setReadNormalCount(
						fragment.getActivity());
				if (msg.arg2 == ObservableModel.SUCCESS_INT) {
					fragment.timeLine.refreshData();
					fragment.adapter.notifyDataSetChanged();
				} else {
					Activity activity = fragment.getActivity();
					if (fragment.index != DEFAULT_INDEX
							&& activity instanceof MessageActivity) {
						MessageActivity messageActivity = (MessageActivity) activity;
						if (fragment.index == messageActivity.getIndex()) {
							Toast.makeText(fragment.getActivity(),
									R.string.user_no_more, Toast.LENGTH_SHORT)
									.show();
						}

					} else{
						 fragment.relativeLayout.setVisibility(View.GONE);
						 fragment.empty_view.setVisibility(View.VISIBLE);
							if (fragment.flags == 0) {
								fragment.icon.setBackgroundResource(R.drawable.icon_notifiction);
								fragment.emptybutton.setVisibility(View.GONE);
								fragment.text.setText("暂无提到您的消息");
							} else if (fragment.flags == 1) {
								fragment.icon.setBackgroundResource(R.drawable.icon_notifiction);
								fragment.emptybutton.setVisibility(View.GONE);
								fragment.text.setText("暂无评论");
							} else if (fragment.flags == 2) {
								fragment.icon.setBackgroundResource(R.drawable.icon_essay);
								fragment.text.setText("暂无随便说说");
							} else if (fragment.flags == 3) {
								fragment.icon.setBackgroundResource(R.drawable.icon_essay);
								fragment.text.setText("还没有写过书评哦");
							} else if (fragment.flags == 4) {
								fragment.icon.setBackgroundResource(R.drawable.icon_collection);
								fragment.text.setText("暂无收藏的社区动态");
							}
					}
				}
			} else {
				Toast.makeText(fragment.getActivity(), R.string.loading_fail,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

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
			case TimeLineModel.RELOAD:
			case TimeLineModel.LOAD_INIT:
				currentpage = 1;
				if (flag == 0) {
					timeLine.loadAt_users(context, type, currentpage,
							pagecount, "", url,1);
				} else if (flag == 1) {
					timeLine.loadEntity(context, type, currentpage, pagecount,
							"", url, user_id + "",user_name,1);
				}
				break;
			case TimeLineModel.LOAD_MORE:

				// timeLine.loadEntities(context, type,
				// timeLine.getEntityAt(timeLine.getLength() - 1).getGuid(),
				// timeLine.getEntityAt(timeLine.getLength() - 1).getId());
				currentpage++;
				if (flag == 0) {
					timeLine.loadAt_users(context, type, currentpage,
							pagecount,
							timeLine.getEntityAt(timeLine.getLength() - 1)
									.getGuid(), url,1);
				} else if (flag == 1) {
					timeLine.loadEntity(context, type, currentpage, pagecount,
							timeLine.getEntityAt(timeLine.getLength() - 1)
									.getGuid(), url, user_id + "",user_name,1);
				}
				break;
			case TimeLineModel.LOAD_NEW:
				currentpage = 1;
				if (pullRefresh) {
					if (timeLine.getLength() == 0) {
						executorService.execute(new TimelineFragmentRunnable(
								TimeLineModel.LOAD_INIT));
					} else if (flag == 0) {
						timeLine.loadAt_users(context, type, currentpage,
								pagecount, "", url,1);
					} else if (flag == 1) {
						timeLine.loadEntity(context, type, currentpage,
								pagecount, "", url, user_id + "",user_name,1);
					}

					// timeLine.loadEntities(context, type,
					// timeLine.getEntityAt(0).getGuid(),
					// UiStaticMethod.ILLEGAL_INDEX);
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
	private final static int DEFAULT_INDEX = -1;
	private boolean loadingMore;
	private boolean pullRefresh;
	private boolean searchable;
	private int index;
	private int lastItemIndex;
	private int searchPage = PagedBasedUrlGetter.FIRST_PAGE;
	private String currentQuery;
	private TimeLineModel timeLine;
	private ListView listView;
	private BaseAdapter adapter;
	private Handler myHandler;
	private View loading;
	private View screenLoading;
	private SearchView searchView;
	private Future<?> initLoad;
	private Future<?> moreLoad;
	private ScheduledExecutorService executorService;
	private int currentpage = -1;
	private int pagecount = 10;
	private static SwipeRefreshLayout mSwipeLayout;
	private String url;
	private int flag = -1;
	private int flags = -1;

	public final static int POST_TWEET = 101;
	private Bundle postTweetBundle;

	private TimelineActivityModel model;
	private String user_id;
	private String user_name;
	private RelativeLayout relativeLayout;
	private RelativeLayout empty_view;
	private ImageView icon;
	private TextView text;
	private Button emptybutton;
	private boolean isRefreshing = false;

	public BaseTimeLineFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.base_timeline_fragment, null);

		fragmentTag = "TimelineFragment";

		loading = inflater.inflate(R.layout.view_loading, null, false);
		screenLoading = rootView.findViewById(R.id.screen_loading);
		listView = (ListView) rootView.findViewById(R.id.timeline_list);
		mSwipeLayout = (SwipeRefreshLayout) rootView
				.findViewById(R.id.ptr_layout);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.red_main, R.color.bg_main,
				R.color.red_sub, R.color.bg_main);
		empty_view = (RelativeLayout) rootView.findViewById(R.id.empty_view);
		emptybutton = (Button) rootView.findViewById(R.id.empty);
		icon = (ImageView) rootView.findViewById(R.id.icon);
		text = (TextView) rootView.findViewById(R.id.text);
		relativeLayout = (RelativeLayout) rootView
				.findViewById(R.id.relativeLayout);

		model = new TimelineActivityModel();
		model.addObserver(this);
		
		emptybutton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent=new Intent(arg0.getContext(), LauncherActivity.class);
				intent.putExtra("TAB_INDEX", 2);
				startActivity(intent);
			}
		});
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		boolean timeAdapter = getArguments().getBoolean(TIMELINE_ADAPTER);
		boolean hideBottom = getArguments().getBoolean(HIDE_BOTTOM);
		loadingMore = getArguments().getBoolean(ENABLE_LOADING_MORE, true);
		pullRefresh = getArguments().getBoolean(ENABLE_PULL_REFRESH);
		searchable = getArguments().getBoolean(SEARCHABLE);
		index = getArguments()
				.getInt(BaseTimeLineFragment.INDEX, DEFAULT_INDEX);
		url = getArguments().getString("url");
		flag = getArguments().getInt("flag");
		flags = getArguments().getInt("flags");
		user_id = getArguments().getString("user_id");
		user_name = getArguments().getString(UserActivity.JD_USER_NAME);
		ParserCreator parserCreator = getArguments().getParcelable(
				PARSER_CREATOR);
		
		if (flags == 0) {
			Notification.getInstance().setReadAtMeCount(getActivity());
		}else {
			Notification.getInstance().setReadCommentsCount(getActivity());
		}
		executorService = NotificationService.getExecutorService();
		listView.addFooterView(UiStaticMethod.getFooterParent(getActivity(),
				loading));
		timeLine = new TimeLineModel(timeAdapter, parserCreator);
		timeLine.addObserver(this);
		myHandler = new TimeLineFragmentHandler(this);
		if (timeAdapter) {
			adapter = new TimeLineAdapter(getActivity(), timeLine, hideBottom,this);
		} else
			adapter = new MessageItemAdapter(getActivity(), timeLine);
		listView.setAdapter(adapter);
		if (searchable) {
			if (loadingMore)
				listView.setOnScrollListener(this);
			listView.setOnItemClickListener(this);
			screenLoading.setVisibility(View.GONE);
		} else {
			screenLoading.setVisibility(View.VISIBLE);
			loading.setVisibility(View.GONE);
			isRefreshing =true;
			initLoad = executorService.submit(new TimelineFragmentRunnable(
					TimeLineModel.LOAD_INIT));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case TimelineTweetActivity.START_TWEET_FROM_QUOTE:
		case START_ACTIVITY_FROM_LIST:
			Message message = Message.obtain();
			switch (resultCode) {
			case TimelineTweetActivity.DELETE_ENTITY:
				deleteEntity(message, data);
				break;
			case TimelineTweetActivity.UPDATE_COMMENTS_NUMBER:
				message.obj = data
						.getStringExtra(TimelineTweetActivity.TWEET_GUID);
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
						message.arg1 = data.getIntExtra(
								TweetModel.COMMENT_NUMBER, commentNumber);
						message.arg2 = data.getIntExtra(
								TweetModel.RECOMMENTS_COUNT, recommendCount);
						if (message.arg1 == commentNumber
								&& message.arg2 == recommendCount)
							message.recycle();
						else
							myHandler.sendMessage(message);
					}
				}
				break;
			}

			break;

		case POST_TWEET:
			switch (resultCode) {
			case TimelinePostTweetActivity.POST_TWEET_WORDS:
				MZLog.d("wangguodong", "======post tweet data!!");
				postTweetBundle = data.getExtras();
				executorService.submit(new TimeLineRunnable(
						TimelineActivityModel.POST_TWEET));
				break;
			}
			break;
		case TimelineTweetActivity.START_COMMENT_FROM_TWEET :
			if(resultCode==getActivity().RESULT_OK){
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
				}
				else{
				}
				myHandler.sendMessage(message1);
				if(bundle.getBoolean(TimelineCommentsActivity.CHECKED))
					executorService.execute(new TimelineFragmentRunnable(TimeLineModel.LOAD_NEW));
			}
			
			break;
		}
	}

	@Override
	public void onDestroyView() {
		timeLine.deleteObserver(this);
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
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& lastItemIndex == adapter.getCount() - 1) {
			if (moreLoad == null || moreLoad.isDone()) {
				loading.setVisibility(View.VISIBLE);
				if (searchable)
					moreLoad = executorService
							.submit(new TimelineFragmentRunnable(
									TimeLineModel.LOAD_MORE, currentQuery));
				else
					moreLoad = executorService
							.submit(new TimelineFragmentRunnable(
									TimeLineModel.LOAD_MORE));
			}

		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		lastItemIndex = firstVisibleItem + visibleItemCount - 1 - 1;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position < adapter.getCount()) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Entity entity = timeLine.getEntityAt(position);
			if (entity instanceof Alert) {
				Alert alert = (Alert) entity;
				if (alert.getLinkType() == Alert.USER_LINK) {
					intent.putExtra(UserFragment.USER_ID,
							Long.parseLong(((Alert) entity).getLink()));
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
	 * 删除列表中的数据，并重新加载网络上的新数据
	 */
	// public void update() {
	// if (executorService != null) {
	// if (initLoad != null && initLoad.isDone()) {
	// timeLine.clear();
	// adapter.notifyDataSetInvalidated();
	// screenLoading.setVisibility(View.VISIBLE);
	// initLoad = executorService.submit(new
	// TimelineFragmentRunnable(TimeLineModel.RELOAD));
	// }
	// }
	//
	//
	// }

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
		intent.setClass(getActivity(), TimelineTweetActivity.class);
		startActivityForResult(intent, START_ACTIVITY_FROM_LIST);
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
		message.obj = data
				.getStringExtra(TimelineTweetActivity.DELETE_ENTITY_ID);
		myHandler.sendMessage(message);
	}

	@Override
	public void onRefresh() {
		if(!isRefreshing){
			isRefreshing =true;
			executorService.execute(new TimelineFragmentRunnable(TimeLineModel.LOAD_NEW));
		}
			
	}

}
