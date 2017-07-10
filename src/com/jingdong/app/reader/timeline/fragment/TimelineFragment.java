package com.jingdong.app.reader.timeline.fragment;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import android.app.Activity;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.bookstore.search.SearchActivity;
import com.jingdong.app.reader.bookstore.search.SearchActivity.SearchTimeLineInterface;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.entity.extra.SearchKeyWord;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.message.activity.MessageActivity;
import com.jingdong.app.reader.message.adapter.MessageItemAdapter;
import com.jingdong.app.reader.message.model.Alert;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.net.url.PagedBasedUrlGetter;
import com.jingdong.app.reader.parser.ParserCreator;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.adapter.TimeLineAdapter;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.Base64;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.EmptyLayout;

/**
 * @author Alexander
 * 
 */
public class TimelineFragment extends CommonFragment implements Observer,
		OnScrollListener, OnItemClickListener,
		android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener,
		SearchTimeLineInterface {

	/**
	 * 该类负责更新listview中的内容
	 * 
	 * @author Alexander
	 * 
	 */
	private static class TimeLineFragmentHandler extends Handler {
		WeakReference<TimelineFragment> tWeakReference;

		public TimeLineFragmentHandler(TimelineFragment fragment) {
			tWeakReference = new WeakReference<TimelineFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			TimelineFragment fragment = tWeakReference.get();
			if (fragment != null) {
				switch (msg.what) {
				case TimeLineModel.LOAD_INIT:
					if (fragment.loadingMore)
						fragment.listView.setOnScrollListener(fragment);
					fragment.listView.setOnItemClickListener(fragment);
					fragment.loading.setVisibility(View.GONE);
//					fragment.screenLoading.setVisibility(View.GONE);
					fragment.mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
					if (fragment.pullRefresh) {
						// ActionBarPullToRefresh
						// .from(fragment.getActivity())
						// .options(
						// Options.create()
						// .scrollDistance(
						// UiStaticMethod.SCROLL_DISTANCE)
						// .build())
						// .allChildrenArePullable().listener(fragment)
						// .setup(fragment.pullToRefreshLayout);
						// fragment.pullToRefreshLayout.setRefreshComplete();
					}
					iniLoad(msg, fragment);
					break;
				case TimeLineModel.RELOAD:
					fragment.loading.setVisibility(View.GONE);
//					fragment.screenLoading.setVisibility(View.GONE);
					fragment.mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
					if (msg.arg1 == ObservableModel.SUCCESS_INT)
						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
							fragment.timeLine.refreshData();
							fragment.adapter.notifyDataSetChanged();
						}
					break;
				case TimeLineModel.LOAD_AS_INPUT:
					fragment.loading.setVisibility(View.GONE);
					if (msg.arg1 == ObservableModel.SUCCESS_INT
							&& msg.arg2 == ObservableModel.SUCCESS_INT) {
						fragment.relativeLayout.setVisibility(View.VISIBLE);
						fragment.linearLayout.setVisibility(View.GONE);
						fragment.mListView.setVisibility(View.GONE);
						fragment.timeLine.refreshData();
						fragment.adapter.notifyDataSetInvalidated();
					} else if (msg.arg1 == ObservableModel.SUCCESS_INT
							&& msg.arg2 == ObservableModel.FAIL_INT) {
						fragment.relativeLayout.setVisibility(View.GONE);
						fragment.linearLayout.setVisibility(View.VISIBLE);
						fragment.mListView.setVisibility(View.GONE);
						fragment.iconImageView.setBackgroundResource(R.drawable.bookstore_icon_search_null);
						fragment.textView.setText("社区中暂无您的搜索动态");
					} else {
						Toast.makeText(fragment.getActivity(),
								R.string.loading_fail, Toast.LENGTH_SHORT)
								.show();
					}
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
						// fragment.pullToRefreshLayout.setRefreshComplete();
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
		private void iniLoad(Message msg, TimelineFragment fragment) {
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
						if (fragment.index == messageActivity.getIndex())
							Toast.makeText(fragment.getActivity(),
									R.string.user_no_more, Toast.LENGTH_SHORT)
									.show();
					} else
						Toast.makeText(fragment.getActivity(),
								R.string.user_no_more, Toast.LENGTH_SHORT)
								.show();
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
				mSwipeLayout.setRefreshing(false);
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
	// private PullToRefreshLayout pullToRefreshLayout;
	private TimeLineModel timeLine;
	private ListView listView;
	private BaseAdapter adapter;
	private Handler myHandler;
	private View loading;
//	private View screenLoading;
	private SearchView searchView;
	private Future<?> initLoad;
	private Future<?> moreLoad;
	private ScheduledExecutorService executorService;
	private int currentpage = -1;
	private int pagecount = 10;
	private static RelativeLayout relativeLayout;
	private static LinearLayout linearLayout;
	public static ArrayList<String> historyList = new ArrayList<String>(10);// 搜索历史记录
	// private List<String> hotkeylist = null;// 热词
	private UserSearchAdapter userSearchAdapter = null;
	private LinearLayout.LayoutParams lp1 = null;
	private static ListView mListView = null;
	private List<SearchKeyWord> searchKeyWordslist = null;
	private int total = 10;
	private static SwipeRefreshLayout mSwipeLayout;
	private ImageView iconImageView;
	private TextView textView;

	public TimelineFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentTag = "TimelineFragment";
		View rootView = inflater.inflate(R.layout.fragment_timeline, null,
				false);
		relativeLayout = (RelativeLayout) rootView
				.findViewById(R.id.relativeLayout);
		linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
		iconImageView = (ImageView) rootView.findViewById(R.id.icon);
		textView = (TextView) rootView.findViewById(R.id.text);

		loading = inflater.inflate(R.layout.list_cell_footer, null, false);
		mEmptyLayout = (EmptyLayout) rootView.findViewById(R.id.error_layout); 
		listView = (ListView) relativeLayout.findViewById(R.id.timeline_list);
		// pullToRefreshLayout = (PullToRefreshLayout) relativeLayout
		// .findViewById(R.id.ptr_layout);
		mListView = (ListView) rootView.findViewById(R.id.mlistview);
		mSwipeLayout = (SwipeRefreshLayout) rootView
				.findViewById(R.id.ptr_layout);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.red_main, R.color.bg_main,
				R.color.red_sub, R.color.bg_main);
		loading.setVisibility(View.GONE);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((SearchActivity) getActivity()).setListener(this);

		boolean timeAdapter = getArguments().getBoolean(TIMELINE_ADAPTER);
		boolean hideBottom = getArguments().getBoolean(HIDE_BOTTOM);
		loadingMore = getArguments().getBoolean(ENABLE_LOADING_MORE, true);
		pullRefresh = getArguments().getBoolean(ENABLE_PULL_REFRESH);
		searchable = getArguments().getBoolean(SEARCHABLE);
		index = getArguments().getInt(TimelineFragment.INDEX, DEFAULT_INDEX);
		ParserCreator parserCreator = getArguments().getParcelable(
				PARSER_CREATOR);
		executorService = NotificationService.getExecutorService();
		listView.addFooterView(UiStaticMethod.getFooterParent(getActivity(),
				loading));
		timeLine = new TimeLineModel(timeAdapter, parserCreator);
		timeLine.addObserver(this);
		myHandler = new TimeLineFragmentHandler(this);
		if (timeAdapter) {
			adapter = new TimeLineAdapter(getActivity(), timeLine, hideBottom,null);
		} else
			adapter = new MessageItemAdapter(getActivity(), timeLine);
		listView.setAdapter(adapter);
		if (searchable) {
			if (loadingMore)
				listView.setOnScrollListener(this);
			listView.setOnItemClickListener(this);
//			screenLoading.setVisibility(View.GONE);
			mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
		} else {
//			screenLoading.setVisibility(View.VISIBLE);
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
			initLoad = executorService.submit(new TimelineFragmentRunnable(
					TimeLineModel.LOAD_INIT));
		}
		if (historyList != null) {
			historyList.clear();
		}
		initLists();
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
		}
	}

	public void initLists() {
		searchKeyWordslist = new ArrayList<SearchKeyWord>();
		historyList = readSearchHistory();
		mHandler.sendMessage(mHandler.obtainMessage(0));
		// getHotKeyWords();
	}

	private void initSearchKeyWorddapter() {
		lp1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				(int) ScreenUtils.getWidthJust(getActivity()) * 2 / 19);
		android.view.ViewGroup.LayoutParams params = mListView.getLayoutParams(); 
		params.height = (int) ScreenUtils.getWidthJust(getActivity()) * 2 / 19 * 4;
		mListView.setLayoutParams(params);
		// 判断是否隐藏删除控件
		if (historyList != null && historyList.size() > 0) {
			userSearchAdapter = new UserSearchAdapter(getActivity(),
					searchKeyWordslist, lp1, true);
		} else {
			userSearchAdapter = new UserSearchAdapter(getActivity(),
					searchKeyWordslist, lp1, false);
		}
		mListView.setAdapter(userSearchAdapter);
	}

	// // 获取搜索热词
	// private void getHotKeyWords() {
	// WebRequestHelper.post(URLText.JD_BASE_URL,
	// RequestParamsPool.getHotKeywordParams(String.valueOf(total)),
	// new MyAsyncHttpResponseHandler(getActivity()) {
	//
	// @Override
	// public void onFailure(int arg0, Header[] arg1, byte[] arg2,
	// Throwable arg3) {
	// Toast.makeText(getActivity(),
	// getString(R.string.network_connect_error),
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// @Override
	// public void onResponse(int statusCode, Header[] headers,
	// byte[] responseBody) {
	// String result = new String(responseBody);
	//
	// try {
	// JSONObject jsonObj = new JSONObject(result);
	// JSONArray array = null;
	// if (jsonObj != null) {
	// String code = jsonObj.optString("code");
	// if (code.equals("0")) {
	// array = jsonObj.getJSONArray("keywords");
	// hotkeylist = new ArrayList<String>();
	// for (int i = 0; i < array.length(); i++) {
	// hotkeylist.add(array.getString(i));
	// }
	// mHandler.sendMessage(mHandler
	// .obtainMessage(0));
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	//
	// }

	/*******
	 * @author ThinkinBunny
	 * @since 2013年6月9日16:53:44
	 * @return historyList 读取搜索历史，在加载页面的第一次读取
	 * ********/
	private ArrayList<String> readSearchHistory() {

		String history = LocalUserSetting
				.getTimelineHistory(getActivity());

		Log.d("cj", "history=========>>>" + history);
		String[] historyArray = history.split(" ");
		for (int i = 0; i < historyArray.length; i++) {
			try {
				String string = new String(Base64.decode(historyArray[i]));
				if (!TextUtils.isEmpty(string)) {
					historyList.add(string);
				}

			} catch (IOException e) {
			}
		}
		return historyList;
	};

	/******
	 * 保存历史搜索，按照先进先出的原则保存10条
	 * 
	 * @author ThinkinBunny
	 * @since 2013年6月9日17:53:51
	 * @param keyWord可以为null
	 *            ，为null是强制保存
	 * ********/
	public void storeHistory(String keyWord, boolean isRemoveKW) {
		historyList.trimToSize();
		if (isRemoveKW) {
			historyList.remove(keyWord);
		} else {
			if (historyList.contains(keyWord)) {
				String tmpKeyWordString = keyWord;
				historyList.remove(keyWord);
				historyList.add(0, tmpKeyWordString);
			} else if (historyList.size() < 10) {
				historyList.add(0, keyWord);

			} else {
				historyList.remove(historyList.size() - 1);
				historyList.add(0, keyWord);
			}
		}

		String historyString = "";
		for (int i = 0; i < historyList.size(); i++) {
			historyString = historyString + " "
					+ Base64.encodeBytes(historyList.get(i).getBytes());
		}
		LocalUserSetting.saveTimelineHistory(getActivity(), historyString);

		if (historyList.isEmpty()) {
			mListView.setVisibility(View.GONE);
		}
		if (userSearchAdapter != null) {
			userSearchAdapter.notifyDataSetChanged();// 更新完数据通知数据变化
		}
	}

	@Override
	public void onResume() {
		super.onResume();
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
				} else if (alert.getLinkType() == Alert.ENTITY_LINK)
					startTweetActivity(intent, entity);
			} else
				startTweetActivity(intent, entity);
		}
	}

	/**
	 * 删除列表中的数据，并重新加载网络上的新数据
	 */
	public void update() {
		if (executorService != null) {
			if (initLoad != null && initLoad.isDone()) {
				timeLine.clear();
				adapter.notifyDataSetInvalidated();
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
				initLoad = executorService.submit(new TimelineFragmentRunnable(
						TimeLineModel.RELOAD));
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
	public void onSearchTimeline(String newText, boolean isSave) {
		currentQuery = newText;
		if (newText == null || newText.equals("")) {
			Message message = Message.obtain();
			message.what = TimeLineModel.EMPTY_INPUT;
			myHandler.sendMessage(message);
		} else {
			search(newText,isSave);
//			if (!newText.equals(historyList.get(0))) {
//				search(newText,isSave);
//			}else {
//				if (SearchActivity.isHistoryKey || SearchActivity.isHistoryListShow) {
//					search(newText,isSave);
//				}
//			}
		}
	}
	
	private void search(String newText, boolean isSave){
		if (isSave) {
			storeHistory(newText, false);
		}
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
				new Intent("com.mzread.action.dialog.show"));
		executorService.execute(new TimelineFragmentRunnable(
				TimeLineModel.LOAD_AS_INPUT, newText));

	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				if (historyList != null && historyList.size() > 0) {
					SearchKeyWord searchKeyWord1 = new SearchKeyWord();
					searchKeyWord1.setTitle("历史搜索");
					searchKeyWord1.setData(historyList);
					searchKeyWordslist.add(searchKeyWord1);
					// SearchKeyWord searchKeyWord2 = new SearchKeyWord();
					// searchKeyWord2.setTitle("热门搜索");
					// searchKeyWord2.setData(hotkeylist);
					// searchKeyWordslist.add(searchKeyWord2);

					mListView.setVisibility(View.VISIBLE);
					linearLayout.setVisibility(View.GONE);
					relativeLayout.setVisibility(View.GONE);

					initSearchKeyWorddapter();
				}

				break;
			}
		}
	};
	private EmptyLayout mEmptyLayout;

	public class UserSearchAdapter extends BaseAdapter {

		private List<SearchKeyWord> list = null;
		private FragmentActivity context = null;
		private UserSearchItemAdapter searchKeyWordItemAdapter = null;
		private LayoutInflater listContainer; // 视图容器
		private LinearLayout.LayoutParams lp1 = null, lp2 = null;
		private boolean flag = false;

		class ViewHolder {
			private TextView title = null;
			private ListView listitem = null;
		}

		public UserSearchAdapter(FragmentActivity fragmentActivity,
				List<SearchKeyWord> list, LinearLayout.LayoutParams lp1,
				boolean flag) {
			this.context = fragmentActivity;
			listContainer = LayoutInflater.from(fragmentActivity); // 创建视图容器并设置上下文
			this.list = list;
			this.lp1 = lp1;
			this.flag = flag;
		}

		@Override
		public int getCount() {
			if (list == null || list.size() == 0) {
				return 0;
			} else {
				return list.size();
			}
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = listContainer.inflate(R.layout.search_key_word,
						null);
				viewHolder.title = (TextView) convertView
						.findViewById(R.id.title);
				viewHolder.listitem = (ListView) convertView
						.findViewById(R.id.listitem);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			lp2 = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					(int) ScreenUtils.getWidthJust(context)
							* (list.get(position).getData().size()) * 2 / 19);

			viewHolder.listitem.setLayoutParams(lp2);

			viewHolder.title.setText(list.get(position).getTitle());
			searchKeyWordItemAdapter = new UserSearchItemAdapter(context,
					position, list, lp1, flag);
			viewHolder.listitem.setAdapter(searchKeyWordItemAdapter);
			return convertView;
		}
	}

	public class UserSearchItemAdapter extends BaseAdapter {

		private List<SearchKeyWord> list = null;
		private FragmentActivity context = null;
		private int num = 0;
		private LayoutInflater listContainer; // 视图容器
		private List<String> listitem = null;
		private LinearLayout.LayoutParams lp = null;
		private boolean flag = false;

		class ViewHolder {
			private RelativeLayout relativeLayout;
			private LinearLayout linearLayout;
			private TextView search_info = null;
			private ImageView delete = null;
		}

		public UserSearchItemAdapter(FragmentActivity context2, int num,
				List<SearchKeyWord> list, LinearLayout.LayoutParams lp,
				boolean flag) {
			this.context = context2;
			this.list = list;
			this.num = num;
			this.lp = lp;
			listContainer = LayoutInflater.from(context2); // 创建视图容器并设置上下文
			listitem = list.get(num).getData();
			this.flag = flag;
		}

		@Override
		public int getCount() {
			if (listitem == null || listitem.size() == 0) {
				return 0;
			} else {
				return listitem.size();
			}
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = listContainer.inflate(
						R.layout.search_key_word_item, null);
				viewHolder.search_info = (TextView) convertView
						.findViewById(R.id.search_info);
				viewHolder.delete = (ImageView) convertView
						.findViewById(R.id.delete_img);
				viewHolder.relativeLayout = (RelativeLayout) convertView
						.findViewById(R.id.relativeLayout);
				viewHolder.linearLayout = (LinearLayout) convertView
						.findViewById(R.id.linearLayout);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.relativeLayout.setLayoutParams(lp);
			viewHolder.relativeLayout.setGravity(Gravity.CENTER_VERTICAL);
			viewHolder.linearLayout.setGravity(Gravity.CENTER_VERTICAL);
			viewHolder.search_info.setText(listitem.get(position));
			if (num == 0 && flag) {
				viewHolder.delete.setVisibility(View.VISIBLE);
			}
			viewHolder.linearLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String query = list.get(0).getData().get(position);
					list.get(0).getData().remove(position);
					notifyDataSetChanged();
					if (list.get(0).getData().isEmpty()) {
						initLists();
					}
					storeHistory(query, true);
				}
			});

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					InputMethodManager imm = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					boolean isOpen = imm.isActive();
					if (isOpen) {
						imm.hideSoftInputFromWindow(
								((SearchActivity) getActivity())
										.getEdittext_serach().getWindowToken(),
								0); // 强制隐藏键盘
					}
					SearchActivity.isHistoryKey = true;
					((SearchActivity) getActivity()).setEditText(listitem
							.get(position));
					onSearchTimeline(listitem.get(position), true);
				}
			});
			return convertView;
		}
	}

	@Override
	public void onRefresh() {
		executorService.execute(new TimelineFragmentRunnable(
				TimeLineModel.LOAD_NEW));
	}

	public static ArrayList<String> getHistoryList() {
		return historyList;
	}

	public static void setHistoryList(ArrayList<String> historyList) {
		TimelineFragment.historyList = historyList;
	}

}
