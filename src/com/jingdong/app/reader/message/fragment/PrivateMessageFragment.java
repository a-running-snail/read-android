package com.jingdong.app.reader.message.fragment;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.me.activity.NotificationFragment;
import com.jingdong.app.reader.message.activity.ChatActivity;
import com.jingdong.app.reader.message.adapter.PrivateMessageAdapter;
import com.jingdong.app.reader.message.model.MessageListModel;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.message.model.MessageListModel.TaskType;
import com.jingdong.app.reader.privateMsg.Conversation;
import com.jingdong.app.reader.timeline.actiivity.TimelineSearchPeopleActivity;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.TaskStatus;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.R;

public class PrivateMessageFragment extends CommonFragment implements OnClickListener, Observer, OnScrollListener,
SwipeRefreshLayout.OnRefreshListener, OnItemClickListener {

	private static class MyHandler extends Handler {
		private WeakReference<PrivateMessageFragment> reference;

		public MyHandler(PrivateMessageFragment fragment) {
			reference = new WeakReference<PrivateMessageFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			PrivateMessageFragment fragment = reference.get();
			if (fragment != null) {
				TaskType[] type = TaskType.values();
				switch (type[msg.what]) {
				case LOAD_INIT_LIST:
//					ActionBarPullToRefresh.from(fragment.getActivity())
//							.options(Options.create().scrollDistance(UiStaticMethod.SCROLL_DISTANCE).build())
//							.allChildrenArePullable().listener(fragment).setup(fragment.pullToRefreshLayout);
					refreshOrShowToast(msg, fragment);
					break;
				case LOAD_PREVIOUS_PAGE:
//					fragment.pullToRefreshLayout.setRefreshComplete();
					refreshOrShowToasts(msg, fragment);
					break;
				case LOAD_NEXT_PAGE:
					refreshOrShowToasts(msg, fragment);
					break;
				case DELETE_CONVERSATION:
					fragment.progressDialog.dismiss();
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
							fragment.adapter.notifyDataSetChanged();
						}
					}
					break;
				default:
					throw new IllegalArgumentException(type[msg.what].name());
				}
			}
		}

		/**
		 * @param msg
		 * @param fragment
		 */
		private void refreshOrShowToast(Message msg, PrivateMessageFragment fragment) {
			if (msg.arg1 == ObservableModel.SUCCESS_INT) {
				if (msg.arg2 == ObservableModel.SUCCESS_INT) {
					fragment.adapter.notifyDataSetChanged();
				}else {
					fragment.relativeLayout.setVisibility(View.GONE);
					fragment.linearLayout.setVisibility(View.VISIBLE);
					fragment.textView.setText("暂无私信");
				}
			} else
				Toast.makeText(fragment.getActivity(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
			fragment.loading.setVisibility(View.GONE);
			fragment.screenLoading.setVisibility(View.GONE);
			fragment.getActivity().setProgressBarIndeterminateVisibility(false);
		}
		
		/**
		 * @param msg
		 * @param fragment
		 */
		private void refreshOrShowToasts(Message msg, PrivateMessageFragment fragment) {
			if (msg.arg1 == ObservableModel.SUCCESS_INT) {
				if (msg.arg2 == ObservableModel.SUCCESS_INT) {
					fragment.adapter.notifyDataSetChanged();
				}else {
					Toast.makeText(fragment.getActivity(), R.string.no_more_data, Toast.LENGTH_SHORT).show();
				}
			} else
				Toast.makeText(fragment.getActivity(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
			fragment.loading.setVisibility(View.GONE);
			fragment.screenLoading.setVisibility(View.GONE);
			fragment.getActivity().setProgressBarIndeterminateVisibility(false);
		}
	}
	

	private final static int PEOPLE_LIST = 101;
	private final static int CHAT_WITH_USER_IN_LIST = 102;
	private ListView listView;
	private View loading;
	private View screenLoading;
//	private PullToRefreshLayout pullToRefreshLayout;
	private ProgressDialog progressDialog;
	private MessageListModel model;
	private PrivateMessageAdapter adapter;
	private Handler handler;
	private TaskStatus initLoadStatus;
	private TaskStatus nextPageStatus;
	private TaskStatus previousPageStatus;
	private int lastItemIndex;
	private static SwipeRefreshLayout mSwipeLayout;
	private RelativeLayout relativeLayout;
	private LinearLayout linearLayout;
	private TextView textView;
	
	public PrivateMessageFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		fragmentTag = "PrivateMessageFragment";
		View root = inflater.inflate(R.layout.fragment_private_message, null);
		loading = inflater.inflate(R.layout.view_loading, null);
		screenLoading = root.findViewById(R.id.screen_loading);
		listView = (ListView) root.findViewById(R.id.list);
		mSwipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.ptr_layout);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.red_main, R.color.bg_main,
				R.color.red_sub, R.color.bg_main);
		relativeLayout = (RelativeLayout) root.findViewById(R.id.relativeLayout);
		linearLayout = (LinearLayout) root.findViewById(R.id.linearLayout);
		textView = (TextView) root.findViewById(R.id.text);
		Notification.getInstance().setReadMessagesCount(getActivity());
//		pullToRefreshLayout = (PullToRefreshLayout) root.findViewById(R.id.ptr_layout);
		registerReceiver();
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		handler = new MyHandler(this);
		model = new MessageListModel(getActivity());
		adapter = new PrivateMessageAdapter(getActivity(), model);
		listView.addFooterView(UiStaticMethod.getFooterParent(getActivity(), loading));
		listView.setRecyclerListener(adapter);
		listView.setAdapter(adapter);
		listView.setOnScrollListener(this);
		listView.setOnItemClickListener(this);
		registerForContextMenu(listView);
		model.addObserver(this);
		getActivity().setProgressBarIndeterminate(true);
		getActivity().setProgressBarIndeterminateVisibility(true);
		screenLoading.setVisibility(View.VISIBLE);
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loading.setVisibility(View.GONE);
		initLoadStatus = model.loadMessageList();
	}

	@Override
	public void onDestroyView() {
		model.deleteObserver(this);
		handler.removeCallbacksAndMessages(null);
		unregisterReceiver();
		super.onDestroyView();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.write_message, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.write_message:
			writeMessage();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = new MenuInflater(getActivity());
		inflater.inflate(R.menu.delete, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		WrapperListAdapter myAdapter = (WrapperListAdapter) listView.getAdapter();
		Conversation conversation = (Conversation) myAdapter.getItem(info.position);
		switch (item.getItemId()) {
		case R.id.delete:
			popDialog(conversation.getId());
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PEOPLE_LIST:
			if (resultCode == TimelineSearchPeopleActivity.CLICK_USER_NAME) {
				UserInfo userInfo = data.getParcelableExtra(TimelineSearchPeopleActivity.USER_PARCELABLE);
				Intent intent = new Intent(getActivity(), ChatActivity.class);
				intent.putExtra(ChatActivity.USER_INFO, userInfo);
				startActivityForResult(intent, CHAT_WITH_USER_IN_LIST);
			}
			break;
		case CHAT_WITH_USER_IN_LIST:
			if (resultCode == Activity.RESULT_OK) {
				if (initLoadStatus == null || initLoadStatus.getTaskStatus() == Status.FINISHED)
					if (nextPageStatus == null || nextPageStatus.getTaskStatus() == Status.FINISHED)
						if (previousPageStatus == null || previousPageStatus.getTaskStatus() == Status.FINISHED)
							previousPageStatus = model.loadPreviousPage();
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		handler.sendMessage((Message) data);
	}

	@Override
	public void onClick(View v) {
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(position<adapter.getCount()){
			Intent intent = new Intent(getActivity(), ChatActivity.class);
			Conversation conversation = (Conversation) parent.getAdapter().getItem(position);
			UserInfo userInfo = conversation.getUserInfo();
			intent.putExtra(ChatActivity.USER_INFO, userInfo);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(intent, CHAT_WITH_USER_IN_LIST);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItemIndex == listView.getCount()) {
			if (initLoadStatus != null && initLoadStatus.getTaskStatus() == Status.FINISHED) {
				if (nextPageStatus == null || nextPageStatus.getTaskStatus() == Status.FINISHED) {
					if (previousPageStatus == null || previousPageStatus.getTaskStatus() == Status.FINISHED) {
						loading.setVisibility(View.VISIBLE);
						getActivity().setProgressBarIndeterminateVisibility(true);
						nextPageStatus = model.loadNextPage();
					}
				}
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		lastItemIndex = firstVisibleItem + visibleItemCount;
	}

	
	private void writeMessage() {
		Intent intent = new Intent(getActivity(), TimelineSearchPeopleActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, PEOPLE_LIST);
	}

	private void popDialog(final long id) {
		UiStaticMethod
				.createConfirmDialog(getActivity(), R.string.delete_conversation, R.string.delete_conversation_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Resources resources = getResources();
								model.deleteConversation(id);
								dialog.dismiss();
								progressDialog = ProgressDialog.show(getActivity(),
										resources.getString(R.string.delete), resources.getString(R.string.deleting),
										true, false);
							}
						}).create().show();
	}

	class ReadingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ChatActivity.ACTION_READ_NEW_MESSAGE)) {
				String userId = intent.getStringExtra(ChatActivity.USER_ID);
				boolean isRead = intent.getBooleanExtra(ChatActivity.IS_READ_NEW_MESSAGE, false);
				if (TextUtils.isEmpty(userId)) {
					for (int i = 0; i < model.size(); i++) {
						if (!userId.equals(model.get(i).getUserInfo().getId())) {
							if (isRead) {
								model.get(i).setHasNew(false);
								refreshMessage();
								break;
							}
						}
					}
				}
			}
		}
	}
	
	private void refreshMessage() {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	private ReadingReceiver receiver = new ReadingReceiver();

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ChatActivity.ACTION_READ_NEW_MESSAGE);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
	}

	private void unregisterReceiver() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		loading.setVisibility(View.VISIBLE);
		getActivity().setProgressBarIndeterminateVisibility(true);
		mSwipeLayout.setRefreshing(false);
//		previousPageStatus = model.loadMessageList();
	}
}
