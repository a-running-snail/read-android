package com.jingdong.app.reader.timeline.fragment;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.bookstore.search.SearchActivity;
import com.jingdong.app.reader.bookstore.search.SearchActivity.SearchUserInterface;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.entity.extra.SearchKeyWord;
import com.jingdong.app.reader.me.model.UserFollower;
import com.jingdong.app.reader.net.url.QueryUrlGetter;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.actiivity.TweetListActivity;
import com.jingdong.app.reader.timeline.adapter.UserListAdapter;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.timeline.model.UserModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.Base64;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.EmptyLayout;

/**
 * UserListFragment 所在的Activity必须实现UserListCallBack。 UserListFragment
 * 所在的Activity可以通过fragment
 * .setArgument(string,boolean)方法来决定是否显示fragment中的关注按钮。如果需要根据用户输入改变显示结果
 * ，需要把当前Fragment做为OnQueryTextListener
 * 在默认情况下关注按钮不显示。具体设置可参见TimelineSearchPeopleActivity。
 * 
 * @author Alexander
 * 
 */
public class UserListFragment extends CommonFragment implements Observer, OnItemClickListener, OnScrollListener, SearchUserInterface, OnClickListener {
	private class PeopleTask implements Runnable {
		private int type;
		private String query;

		public PeopleTask(int type) {
			this.type = type;
		}

		public PeopleTask(int type, String query) {
			this.type = type;
			this.query = query;
		}

		@Override
		public void run() {
			Context context = getActivity();
			switch (type) {
			case UserModel.LOAD_FOLLOWING_PEOPLE:
				userModel.loadFollowingList();
				break;
			case UserModel.LOAD_AS_INPUT:
				currentpage = 1;
				userModel.searchUsers(context, type, query, currentpage, pagecount);
				break;
			case UserModel.LOAD_QUERY_MORE:
				if (query != null) {
					currentpage++;
					userModel.searchUsers(context, type, query, currentpage, pagecount);
				}
				break;
			case UserModel.LOAD_FOLLOWING_MORE:
				userModel.loadMoreFollowingUser();
				break;
			case UserModel.FOLLOW_ALL:
				userModel.followUsers(type);
				break;
			}

		}
	}

	private static class PeopleListHandler extends Handler {
		WeakReference<UserListFragment> reference;

		public PeopleListHandler(UserListFragment activity) {
			reference = new WeakReference<UserListFragment>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			UserListFragment fragment = reference.get();
			if (fragment != null) {
//				fragment.screenLoading.setVisibility(View.GONE);
				fragment.mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
				fragment.loading.setVisibility(View.GONE);
				switch (msg.what) {
				case UserModel.LOAD_FOLLOWING_PEOPLE:
				case UserModel.LOAD_QUERY_MORE:
				case UserModel.LOAD_FOLLOWING_MORE:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						Resources resources = fragment.getResources();
						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
							fragment.userModel.refreshList(UserModel.LOAD_QUERY_MORE);
							fragment.adapter.notifyDataSetChanged();
							if (fragment.friendsNumber) {
								String friendsNumber = resources.getQuantityString(R.plurals.numberOfThirdPartyFriends, fragment.userModel.getUsers().size(),
										fragment.userModel.getUsers().size());
								fragment.friendsNumberText.setText(friendsNumber);
							}
						} else {
							Toast.makeText(fragment.getActivity(), R.string.user_no_more, Toast.LENGTH_SHORT).show();
							if (fragment.friendsNumber) {
								fragment.friendsNumberText.setText(String.valueOf(0));
							}
						}
					} else {
						Toast.makeText(fragment.getActivity(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
						if (fragment.friendsNumber) {
							fragment.friendsNumberText.setText(String.valueOf(0));
						}
					}
					break;
				case UserModel.LOAD_AS_INPUT:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
							relativeLayout.setVisibility(View.VISIBLE);
							linearLayout.setVisibility(View.GONE);
							mListView.setVisibility(View.GONE);
							fragment.userModel.refreshList(UserModel.LOAD_AS_INPUT);
							fragment.adapter.notifyDataSetChanged();
						} else if (fragment.userModel.getInput() == null || fragment.userModel.getInput().isEmpty()) {
							relativeLayout.setVisibility(View.GONE);
							linearLayout.setVisibility(View.VISIBLE);
							mListView.setVisibility(View.GONE);
							fragment.userModel.refreshList(UserModel.LOAD_AS_INPUT);
							fragment.adapter.notifyDataSetChanged();
						}
					} else {
						Toast.makeText(fragment.getActivity(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
					}
					break;
				case UserModel.FOLLOW_ALL:
					// 这个是全部关注
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						fragment.userModel.invalidateUsers();
						Toast.makeText(fragment.getActivity(), R.string.follow_all_success, Toast.LENGTH_SHORT).show();
					} else if (msg.arg1 == ObservableModel.FAIL_INT) {
						Toast.makeText(fragment.getActivity(), R.string.follow_all_fail, Toast.LENGTH_SHORT).show();
					}
					fragment.adapter.notifyDataSetChanged();
					break;
				case UserFollower.FOLLOW:
					// 这个是某一行右边的关注
					fragment.userModel.invalidateUser((String) msg.obj, msg.arg1);
					break;
				}
			}
		}
	}

	/**
	 * 该字符串为Intent中一个boolean型变量的key，该boolean变量为true表示在每行的最右边显示一个关注按钮，
	 * 该boolean变量为false则不显示关注按钮
	 */
	public static final String SHOW_RIGHT_BUTTON = "showRightButton";
	public static final String EMPTY_INIT_PAGE = "emptyPage";
	public static final String ITEM_KEY = "itemKey";
	public static final String SHOW_FOLLOW_ALL = "followAll";
	public static final String SHOW_FRIENDS_NUMBER = "friendsNumber";
	public static final String USER_JSON_STRING = "jsonString";
	public static final String NOTE = "note";
	private Executor executor;
	private String jsonString;
	private boolean friendsNumber;
	private UserModel userModel;
	private Handler myHandler;
	private UserListCallBack callBack;
	private UserListAdapter adapter;
	private ListView listView;
	private Button followAll;
	private TextView friendsNumberText;
	private View loading;
	private View followBar;
//	private View screenLoading;
	private int lastItemIndex;
	private static RelativeLayout relativeLayout;
	private static LinearLayout linearLayout;
	private int currentpage = -1;
	private int pagecount = 10;
	public static ArrayList<String> historyList = new ArrayList<String>(10);// 搜索历史记录
	// private List<String> hotkeylist = null;// 热词
	private UserSearchAdapter userSearchAdapter = null;
	private LinearLayout.LayoutParams lp1 = null;
	private static ListView mListView = null;
	private List<SearchKeyWord> searchKeyWordslist = null;
	private int total = 10;
	private String queString;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof UserListCallBack)) {
			throw new IllegalArgumentException("UserListFragment 所在的Activity必须实现UserListCallBack接口");
		} else
			callBack = (UserListCallBack) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fragmentTag = "UserListFragment";
		View rootView = inflater.inflate(R.layout.fragment_user_list, null, false);
		relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativeLayout);
		linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
		followBar = rootView.findViewById(R.id.timeline_follow_all);
		followAll = (Button) rootView.findViewById(R.id.follow_button);
		listView = (ListView) rootView.findViewById(R.id.timeline_following);
		friendsNumberText = (TextView) rootView.findViewById(R.id.friends_number);
		loading = inflater.inflate(R.layout.list_cell_footer, null, false);

		mEmptyLayout = (EmptyLayout) rootView.findViewById(R.id.error_layout);
		mListView = (ListView) rootView.findViewById(R.id.mlistview);
		loading.setVisibility(View.GONE);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((SearchActivity) getActivity()).setuserListener(this);
		
		boolean followAll = getArguments().getBoolean(SHOW_FOLLOW_ALL);
		if (followAll) {
			followBar.setVisibility(View.VISIBLE);
			this.followAll.setOnClickListener(this);
		} else
			followBar.setVisibility(View.GONE);
		boolean showButton = getArguments().getBoolean(SHOW_RIGHT_BUTTON);
		boolean emptyInit = getArguments().getBoolean(EMPTY_INIT_PAGE);
		friendsNumber = getArguments().getBoolean(SHOW_FRIENDS_NUMBER);
		jsonString = getArguments().getString(USER_JSON_STRING);
		myHandler = new PeopleListHandler(this);
		userModel = new UserModel(callBack.getUsersUrlGetter(), this, getArguments().getString(TweetListActivity.JsonNameKey), getArguments().getString(
				ITEM_KEY), jsonString);
		userModel.setShowButton(showButton);
		userModel.addObserver(this);
		adapter = new UserListAdapter(getActivity(), userModel, getArguments().getBoolean(NOTE));
		listView.setRecyclerListener(adapter);
		listView.addFooterView(UiStaticMethod.getFooterParent(getActivity(), loading));
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setOnScrollListener(this);
		executor = NotificationService.getExecutorService();
		if (!emptyInit) {
//			screenLoading.setVisibility(View.VISIBLE);
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
			executor.execute(new PeopleTask(UserModel.LOAD_FOLLOWING_PEOPLE));
		} else
//			screenLoading.setVisibility(View.GONE);
			mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
		if (historyList != null) {
			historyList.clear();
		}
		initLists();
	}

	public void initLists() {
		searchKeyWordslist = new ArrayList<SearchKeyWord>();
		historyList = readSearchHistory();
		mHandler.sendMessage(mHandler.obtainMessage(0));
		// getHotKeyWords();
	}

	private void initSearchKeyWorddapter() {
		lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) ScreenUtils.getWidthJust(getActivity()) * 2 / 19);
		android.view.ViewGroup.LayoutParams params = mListView.getLayoutParams();
		params.height = (int) ScreenUtils.getWidthJust(getActivity()) * 2 / 19 * 4;
		mListView.setLayoutParams(params);
		// 判断是否隐藏删除控件
		if (historyList != null && historyList.size() > 0) {
			userSearchAdapter = new UserSearchAdapter(getActivity(), searchKeyWordslist, lp1, true);
		} else {
			userSearchAdapter = new UserSearchAdapter(getActivity(), searchKeyWordslist, lp1, false);
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
	// // TODO Auto-generated method stub
	// Toast.makeText(getActivity(),
	// getString(R.string.network_connect_error),
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// @Override
	// public void onResponse(int statusCode, Header[] headers,
	// byte[] responseBody) {
	// // TODO Auto-generated method stub
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

		String history = LocalUserSetting.getUserHistory(getActivity());

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
			historyString = historyString + " " + Base64.encodeBytes(historyList.get(i).getBytes());
		}
		LocalUserSetting.saveUserHistory(getActivity(), historyString);

		if (historyList.isEmpty()) {
			mListView.setVisibility(View.GONE);
		}
		if (userSearchAdapter != null) {
			userSearchAdapter.notifyDataSetChanged();// 更新完数据通知数据变化
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void update(Observable observable, Object data) {
		Message message = (Message) data;
		myHandler.sendMessage(message);
	}

	@Override
	public void onDestroyView() {
		userModel.deleteObserver(this);
		myHandler.removeCallbacksAndMessages(null);
		super.onDestroyView();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position < adapter.getCount()) {
			callBack.onItemClick((UserInfo) parent.getAdapter().getItem(position));
			callBack.onItemClick(userModel.getDocuments().get(position));
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItemIndex == adapter.getCount() - 1) {
			loading.setVisibility(View.VISIBLE);
//			if (userModel.getInput() == null || userModel.getInput().isEmpty())
//				executor.execute(new PeopleTask(UserModel.LOAD_FOLLOWING_MORE));
//			else
				executor.execute(new PeopleTask(UserModel.LOAD_QUERY_MORE, queString));
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		lastItemIndex = firstVisibleItem + visibleItemCount - 1 - 1;
	}

	/**
	 * 关注用户列表中的所有用户
	 */
	@Override
	public void onClick(View v) {
		executor.execute(new PeopleTask(UserModel.FOLLOW_ALL));
	}

	@Override
	public void onSearchUser(String query, boolean isSave) {
		// TODO Auto-generated method stub
		if (query == null || query.equals("")) {
			Message message = Message.obtain();
			message.what = TimeLineModel.EMPTY_INPUT;
			myHandler.sendMessage(message);
		} else {
			search(query, isSave);
//			if (!query.equals(historyList.get(0))) {
//				search(query, isSave);
//			}else {
//				if (SearchActivity.isHistoryKey || SearchActivity.isHistoryListShow) {
//					search(query, isSave);
//				}
//			}
		}
	}
	
	private void search(String query, boolean isSave){
		if (isSave) {
			storeHistory(query, false);
		}
		queString = query;
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent("com.mzread.action.dialog.show"));
		executor.execute(new PeopleTask(UserModel.LOAD_AS_INPUT, query));
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
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

		public UserSearchAdapter(FragmentActivity fragmentActivity, List<SearchKeyWord> list, LinearLayout.LayoutParams lp1, boolean flag) {
			this.context = fragmentActivity;
			listContainer = LayoutInflater.from(fragmentActivity); // 创建视图容器并设置上下文
			this.list = list;
			this.lp1 = lp1;
			this.flag = flag;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (list == null || list.size() == 0) {
				return 0;
			} else {
				return list.size();
			}
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = listContainer.inflate(R.layout.search_key_word, null);
				viewHolder.title = (TextView) convertView.findViewById(R.id.title);
				viewHolder.listitem = (ListView) convertView.findViewById(R.id.listitem);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) ScreenUtils.getWidthJust(context)
					* (list.get(position).getData().size()) * 2 / 19);

			viewHolder.listitem.setLayoutParams(lp2);

			viewHolder.title.setText(list.get(position).getTitle());
			searchKeyWordItemAdapter = new UserSearchItemAdapter(context, position, list, lp1, flag);
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

		public UserSearchItemAdapter(FragmentActivity context2, int num, List<SearchKeyWord> list, LinearLayout.LayoutParams lp, boolean flag) {
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
			// TODO Auto-generated method stub
			if (listitem == null || listitem.size() == 0) {
				return 0;
			} else {
				return listitem.size();
			}
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = listContainer.inflate(R.layout.search_key_word_item, null);
				viewHolder.search_info = (TextView) convertView.findViewById(R.id.search_info);
				viewHolder.delete = (ImageView) convertView.findViewById(R.id.delete_img);
				viewHolder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.relativeLayout);
				viewHolder.linearLayout = (LinearLayout) convertView.findViewById(R.id.linearLayout);
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
					// TODO Auto-generated method stub
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
					// TODO Auto-generated method stub
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					boolean isOpen = imm.isActive();
					if (isOpen) {
						imm.hideSoftInputFromWindow(((SearchActivity) getActivity()).getEdittext_serach().getWindowToken(), 0); // 强制隐藏键盘
					}
					SearchActivity.isHistoryKey = true;
					((SearchActivity) getActivity()).setEditText(listitem.get(position));
					onSearchUser(listitem.get(position), true);
				}
			});
			return convertView;
		}
	}

	public static ArrayList<String> getHistoryList() {
		return historyList;
	}

	public static void setHistoryList(ArrayList<String> historyList) {
		UserListFragment.historyList = historyList;
	}

}
