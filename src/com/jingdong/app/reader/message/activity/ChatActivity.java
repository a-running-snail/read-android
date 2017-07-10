package com.jingdong.app.reader.message.activity;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.SettingsActivity;
import com.jingdong.app.reader.activity.UploadActivity;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.message.adapter.ChatAdapter;
import com.jingdong.app.reader.message.model.ChatListModel;
import com.jingdong.app.reader.message.model.ChatListModel.TaskType;
import com.jingdong.app.reader.privateMsg.DocumentRequest;
import com.jingdong.app.reader.request.Redirect;
import com.jingdong.app.reader.service.OpdsBookDownloadService;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.CustomProgreeDialog;

public class ChatActivity extends BaseActivityWithTopBar implements Observer, OnClickListener,SwipeRefreshLayout.OnRefreshListener{
	private static class MyHandler extends Handler {
		private WeakReference<ChatActivity> reference;

		public MyHandler(ChatActivity chatActivity) {
			reference = new WeakReference<ChatActivity>(chatActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			final ChatActivity activity = reference.get();
			if (activity != null) {
				TaskType[] type = TaskType.values();
				switch (type[msg.what]) {
				case LOAD_USER_INFO:
					onLoadUserInfo(msg, activity);
					break;
				case SEND_TEXT:
					onSendText(msg, activity);
					break;
				case LOAD_PREVIOUS_PAGE:
					activity.mSwipeLayout.setRefreshing(false);
					refreshOrShowToast(msg, activity);
					break;
				case LOAD_INIT_LIST:
					onLoadInitList(msg, activity);
					break;
				case UPDATE_LIST:
					refreshOrShowToast(msg, activity);
					if (msg.arg1 == ObservableModel.SUCCESS_INT
							&& msg.arg2 == ObservableModel.SUCCESS_INT)
						activity.listView.setSelection(activity.listView
								.getAdapter().getCount() - 1);
					break;
				case SCHEDULE_UPDATE:
					activity.model.updateList(activity);
					break;
				case APPROVE_BORROW:
					onApprove(msg, activity);
					break;
				case DENY_BORROW:
					onDeny(msg, activity);
					break;
				case DOWNLOAD_LINK:
					onDownload(msg, activity);
					break;
				case SEARCH_LOCAL_BOOK:
					onSearchLocalBook(msg, activity);
					break;
				default:
					throw new IllegalArgumentException(type[msg.what].name());
				}
			}
		}

		/**
		 * @param msg
		 * @param activity
		 */
		private void onDeny(Message msg, final ChatActivity activity) {
			if (msg.arg1 == ObservableModel.SUCCESS_INT) {
				if (msg.arg2 == ObservableModel.SUCCESS_INT) {
					activity.adapter.notifyDataSetChanged();
				} else if (msg.arg2 == ObservableModel.FAIL_INT) {
					if (msg.obj instanceof String)
						Toast.makeText(activity, (String) msg.obj,
								Toast.LENGTH_SHORT).show();
				}
			}
			activity.model.updateList(activity);
		}

		/**
		 * @param msg
		 * @param activity
		 */
		private void onLoadUserInfo(Message msg, final ChatActivity activity) {
			if (msg.arg1 == ObservableModel.SUCCESS_INT) {
				if (msg.arg2 == ObservableModel.SUCCESS_INT) {
					activity.userInfo = (UserInfo) msg.obj;
					activity.invalidateOptionsMenu();
					activity.setupAdapter();
					activity.model.loadInit(activity);
				}
			} else
				Toast.makeText(activity, R.string.loading_fail,
						Toast.LENGTH_SHORT).show();
		}

		/**
		 * @param msg
		 * @param activity
		 */
		private void onSendText(Message msg, final ChatActivity activity) {
//			activity.sendButton.setChecked(false);
			activity.sendWaiting.dismiss();
			if (msg.arg1 == ObservableModel.SUCCESS_INT
					&& msg.arg2 == ObservableModel.SUCCESS_INT) {
				activity.messsageText.setText("");
				activity.sendButton.setEnabled(true);
				activity.sendButton.setTextColor(activity.getResources().getColor(R.color.red_main));
				activity.model.updateList(activity);
			} else {
				activity.sendButton.setEnabled(true);
				activity.sendButton.setTextColor(activity.getResources().getColor(R.color.red_main));
				Toast.makeText(activity, R.string.private_message_send_fail,
						Toast.LENGTH_SHORT).show();
			}
		}

		/**
		 * @param msg
		 * @param activity
		 */
		private void onApprove(Message msg, final ChatActivity activity) {
			if (msg.arg1 == ObservableModel.SUCCESS_INT) {
				if (msg.arg2 == ObservableModel.SUCCESS_INT) {
					activity.adapter.notifyDataSetChanged();
				} else if (msg.arg2 == ObservableModel.FAIL_INT) {
					Bundle bundle = msg.getData();
					Document document = bundle
							.getParcelable(ChatListModel.DOCUMENT);
					DocumentRequest documentRequest = bundle
							.getParcelable(ChatListModel.DOCUMENT_REQUEST);
					activity.model.searchLocalBook(document, documentRequest);
				}
			}
			activity.model.updateList(activity);
		}

		/**
		 * @param msg
		 * @param activity
		 */
		private void onLoadInitList(Message msg, final ChatActivity activity) {
//			ActionBarPullToRefresh
//					.from(activity)
//					.options(
//							Options.create()
//									.scrollDistance(
//											UiStaticMethod.SCROLL_DISTANCE)
//									.build()).allChildrenArePullable()
//					.listener(activity).setup(activity.pullToRefreshLayout);
			refreshOrShowToast(msg, activity);
			activity.listView.setSelection(activity.listView.getAdapter()
					.getCount() - 1);
			activity.executor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					activity.myHandler
							.sendEmptyMessage(TaskType.SCHEDULE_UPDATE
									.ordinal());
				}
			}, DELAY, DELAY, TimeUnit.MILLISECONDS);
		}

		/**
		 * @param msg
		 * @param activity
		 */
		private void onDownload(Message msg, final ChatActivity activity) {
			if (msg.arg1 == ObservableModel.SUCCESS_INT) {
				Bundle bundle = msg.getData();
				if (msg.arg2 == ObservableModel.SUCCESS_INT) {
					Toast.makeText(
							activity,
							activity.getString(R.string.start_download)
									+ "\""
									+ bundle.getString(OpdsBookDownloadService.OpdsBookNameKey)
									+ ".epub\""
									+ activity
											.getString(R.string.to_3rd_bookcase),
							Toast.LENGTH_LONG).show();
					Intent intent = new Intent(activity,
							OpdsBookDownloadService.class);
					intent.putExtra(OpdsBookDownloadService.OpdsBookUrlPathKey,
							bundle.getString(Redirect.LOCATION));
					intent.putExtra(
							OpdsBookDownloadService.OpdsBookNameKey,
							bundle.getString(OpdsBookDownloadService.OpdsBookNameKey));
					activity.startService(intent);
				} else if (msg.arg2 == ObservableModel.FAIL_INT) {
					Toast.makeText(activity,
							bundle.getString(Redirect.LOCATION),
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		/**
		 * @param msg
		 * @param activity
		 */
		private void onSearchLocalBook(Message msg, final ChatActivity activity) {
			if (msg.obj instanceof Long) {
				Long id = (Long) msg.obj;
				if (id == -1) {
					showAlertDialog(msg, activity);
				} else {
					Document document = msg.getData().getParcelable(
							ChatListModel.DOCUMENT);
					if (document != null) {
						Intent intent = new Intent(activity,
								UploadActivity.class);
						intent.putExtra("type", UploadActivity.TYPE);
						intent.putExtra("document", document);
						activity.startActivityForResult(intent, UPLOAD_DOCUMENT);
					} else {
						showAlertDialog(msg, activity);
					}
				}
			}
		}

		private void showAlertDialog(Message msg, final ChatActivity activity) {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			final Bundle bundle = msg.getData();
			builder.setTitle(R.string.reject_borrow);
			builder.setMessage(R.string.reject_borrow_msg);
			builder.setCancelable(true);
			builder.setNegativeButton(R.string.deny,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							activity.model.denyBorrowRequest((DocumentRequest) bundle
									.getParcelable(ChatListModel.DOCUMENT_REQUEST));
							dialog.dismiss();
						}
					});
			builder.setPositiveButton(R.string.whatever,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
		}

		private void refreshOrShowToast(Message msg, ChatActivity activity) {
			if (msg.arg1 == ObservableModel.SUCCESS_INT) {
				if (msg.arg2 == ObservableModel.SUCCESS_INT) {
					activity.adapter.notifyDataSetChanged();
					Intent intent = new Intent(ACTION_READ_NEW_MESSAGE);
					intent.putExtra(USER_ID, activity.userId);
					intent.putExtra(IS_READ_NEW_MESSAGE, true);
					LocalBroadcastManager.getInstance(activity).sendBroadcast(
							intent);
				}
			} else {
				Toast.makeText(activity, R.string.loading_fail,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public final static String ACTION_READ_NEW_MESSAGE = "action_read_new_message";
	public final static String IS_READ_NEW_MESSAGE = "is_read_new_message";
	public final static String USER_INFO = "user_info";

	public final static String USER_ID = "user_id";
	private final static long DELAY = 90 * 1000;
	private final static int UPLOAD_DOCUMENT = 120;
	private UserInfo userInfo;
	private String userId;
	private ChatListModel model;
	private ListView listView;
	private EditText messsageText;
	private TextView sendButton;
//	private PullToRefreshLayout pullToRefreshLayout;
	private static SwipeRefreshLayout mSwipeLayout;
	private ChatAdapter adapter;
	private Handler myHandler;
	private ScheduledExecutorService executor;
	private String default_message = "";
	private String actionbar_title = "";
	private ProgressDialog uploadDialog;
	private ProgressDialog sendWaiting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		initField();
		sendWaiting = CustomProgreeDialog.instance(this,
				getString(R.string.private_message_sending));
		sendButton.setOnClickListener(this);
		model.addObserver(this);
		if (userInfo == null) {
			model.loadUserInfo(ChatActivity.this);
		} else {
			setupAdapter();
			model.loadInit(ChatActivity.this);
		}
	}

	private void setupAdapter() {
		adapter = new ChatAdapter(model, this, model, userInfo);
		listView.setRecyclerListener(adapter);
		listView.setAdapter(adapter);
	}

	public void showProgressDialog() {
		uploadDialog = new ProgressDialog(ChatActivity.this);
		uploadDialog.setMessage("正在上传错误日志,有助于开发人员尽快帮您解决问题。");
		uploadDialog.show();

	}
	
	public void dismissDialog(){
		if(uploadDialog!=null&&uploadDialog.isShowing())
			uploadDialog.dismiss();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (!UiStaticMethod.isEmpty(actionbar_title)
				&& actionbar_title.equals(getString(R.string.user_feedback))) {
			MenuInflater inflater = new MenuInflater(this);
			inflater.inflate(R.menu.upload_db_file, menu);
			MenuItem Item = menu.findItem(R.id.upload);
			View actionView = Item.getActionView();
			TextView view = (TextView) actionView
					.findViewById(R.id.upload_action);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showProgressDialog();
					UploadErrorDbFileTask task=new UploadErrorDbFileTask();
					task.execute();
				}
			});
		}

		return super.onCreateOptionsMenu(menu);
	}

	class UploadErrorDbFileTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params) {
			return false;//Upload.requestUploadDbFile(ChatActivity.this);
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if(result)
			{
				Toast.makeText(ChatActivity.this, "感谢亲的协助，我们会尽快帮你解决问题哦！", Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(ChatActivity.this, "亲，上传失败了，可能网络有问题或者其他原因，请重试！", Toast.LENGTH_LONG).show();
			}
			dismissDialog();
			
		}
	}
	
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_OK);
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		executor.shutdown();
		model.deleteObserver(this);
		myHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case UPLOAD_DOCUMENT:
			int resId;
			if (resultCode == RESULT_OK)
				resId = R.string.upload_success;
			else
				resId = R.string.upload_fail;
			Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		myHandler.sendMessage((Message) data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.submit:
//			sendButton.setChecked(true);
			sendButton.setTextColor(R.color.text_sub);
			sendButton.setEnabled(false);
			sendWaiting.show();
			if (!TextUtils.isEmpty(messsageText.getText().toString())) {
				model.sendText(messsageText.getText().toString());
			} else {
				sendWaiting.dismiss();
				sendButton.setEnabled(true);
				sendButton.setTextColor(getResources().getColor(R.color.red_main));
				Toast.makeText(ChatActivity.this, R.string.post_without_word, Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}

	}

	/**
	 * 初始化activity的field。
	 */
	private void initField() {
		Intent intent = getIntent();
		userId = intent.getStringExtra(USER_ID);
		userInfo = intent.getParcelableExtra(USER_INFO);
		default_message = intent.getStringExtra("default_message");
		actionbar_title = intent
				.getStringExtra(SettingsActivity.CHAT_ACTIONBAR_TITLE);
		
		if (userInfo != null) {
			userId = userInfo.getId();
			getTopBarView().setTitle(userInfo.getName());
			getTopBarView().setRightMenuOneVisiable(true, R.drawable.icon_about, false);
			model = new ChatListModel(this, userId,userInfo.getJd_user_name());
		}else {
			model = new ChatListModel(this, userId);
		}

		myHandler = new MyHandler(this);
		executor = Executors.newSingleThreadScheduledExecutor();
		listView = (ListView) findViewById(R.id.list);
		mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.ptr_layout);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.red_main, R.color.bg_main,
				R.color.red_sub, R.color.bg_main);
//		pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
		messsageText = (EditText) findViewById(R.id.message);
		if (null != default_message && !default_message.equals("")) {
			messsageText.setText(default_message);
		}
		sendButton = (TextView) findViewById(R.id.submit);
	}
	
	@Override
	public void onRightMenuOneClick() {
		// TODO Auto-generated method stub
		super.onRightMenuOneClick();
		Intent intent = new Intent();
		intent.setClass(ChatActivity.this, UserActivity.class);
		intent.putExtra("user_id", userId);
		startActivity(intent);
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		model.loadPrevious(ChatActivity.this);
	}

}
