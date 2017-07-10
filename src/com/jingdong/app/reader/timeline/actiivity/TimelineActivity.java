package com.jingdong.app.reader.timeline.actiivity;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ScheduledExecutorService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.MZReadCommonFragmentActivity;
import com.jingdong.app.reader.parser.BaseParserCreator;
import com.jingdong.app.reader.parser.json.TimelineJSONParser;
import com.jingdong.app.reader.parser.url.TimelineURLParser;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.timeline.model.TimelineActivityModel;
import com.jingdong.app.reader.ui.ActionBarHelper;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.FlowWindowUtils;

public class TimelineActivity extends MZReadCommonFragmentActivity implements Observer{

	private FlowWindowUtils flowUtils;
	private class TimeLineRunnable implements Runnable {
		private int type;

		public TimeLineRunnable(int type) {
			this.type = type;
		}

		@Override
		public void run() {
			switch (type) {
			case TimelineActivityModel.POST_TWEET:
				model.postTweet(TimelineActivity.this, postTweetBundle);
				break;
			}
		}
	}

	private static class TimeLineHandler extends Handler {
		WeakReference<TimelineActivity> reference;

		public TimeLineHandler(TimelineActivity activity) {
			reference = new WeakReference<TimelineActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			TimelineActivity activity = reference.get();
			if (activity != null) {
				switch (msg.what) {
				case TimelineActivityModel.POST_TWEET:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						activity.showPostTweetSuccess();
						if (activity.executorService != null && activity.fragment != null) {
							activity.executorService
									.submit(activity.fragment.new TimelineFragmentRunnable(
											TimeLineModel.LOAD_NEW));
						}
					}
					break;
				}
			}
		}
	}

	public static final String SINCE = "since_guid";
	public static final String BEFORE_GUID = "before_guid";
	public final static int POST_TWEET = 101;
	private ActionBarHelper actionBarHelper;
	public TimelineFragment fragment;
	private TimelineActivityModel model;
	private ScheduledExecutorService executorService;
	private Bundle postTweetBundle;
	private Handler myHandler;
	private int backPressTimes;


	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			MZLog.d("wangguodong", "Timeline 响应"+intent.getAction());
			if(intent.getAction().equals(FlowWindowUtils.COMPOSE_TEXT))
			{
				Intent it1 = new Intent(context, TimelinePostTweetActivity.class);
				startActivityForResult(it1, POST_TWEET);
			} else if (intent.getAction().equals(FlowWindowUtils.BOOK_COMMENT)) {
				Intent it2 = new Intent(context,
						TimelineBookListCommentsActivity.class);
				it2.putExtra("type", TimelineBookListActivity.type[3]);
				startActivity(it2);
			} else if (intent.getAction().equals(FlowWindowUtils.BOOK_LIST)) {
				Intent it3 = new Intent(context,
						TimelineBookListCommentsActivity.class);
				it3.putExtra("type", TimelineBookListActivity.type[4]);
				startActivity(it3);
			} else if (intent.getAction().equals(NotificationService.NOTIFICATION_ACTION)) {
//				refreshActionBarMenuUI();
			}
		}

	};

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(FlowWindowUtils.COMPOSE_TEXT);
		filter.addAction(FlowWindowUtils.BOOK_LIST);
		filter.addAction(FlowWindowUtils.BOOK_COMMENT);
		filter.addAction(NotificationService.NOTIFICATION_ACTION);
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
	}

	private void unregisterReceiver() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
	}

	private void refreshActionBarMenuUI() {
		if (actionBarHelper != null) {
			actionBarHelper.loadNewNotification(this);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		if (savedInstanceState == null)
			initFragment();
		executorService = NotificationService.getExecutorService();
		model = new TimelineActivityModel();
		model.addObserver(this);
		myHandler = new TimeLineHandler(this);
		flowUtils=new FlowWindowUtils();//悬浮窗口
		//if (LocalUserSetting.isFirstTimeUse(this)) {
		//	Toast.makeText(this, "点击右上角可打开菜单", Toast.LENGTH_LONG).show();
		//}

		actionBarHelper = new ActionBarHelper();
		actionBarHelper.customActionBar(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case POST_TWEET:
			switch (resultCode) {
			case TimelinePostTweetActivity.POST_TWEET_WORDS:
				postTweetBundle = data.getExtras();
				executorService.submit(new TimeLineRunnable(TimelineActivityModel.POST_TWEET));
				break;
			}
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver();
		refreshActionBarMenuUI();
	}



	@Override
	protected void onDestroy() {
		model.deleteObserver(this);
		myHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (backPressTimes > 0) {
			//exit app
			MZBookApplication.exitApplication();
			super.onBackPressed();
		} else {
			Toast.makeText(this, getString(R.string.back_exit_warning),
					Toast.LENGTH_SHORT).show();
			++backPressTimes;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.timeline, menu);
		actionBarHelper.createDropdownButton(this, menu);
		refreshActionBarMenuUI();
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		// boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver();
		flowUtils.removeView();
		backPressTimes = 0;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.dropdown_button) {
			actionBarHelper.createPopupMenu(this, this.findViewById(R.id.actionbar_overlay));
			return true;
		} else {
			switch (item.getItemId()) {
			case R.id.timeline_post_tweet:
				flowUtils.addView(TimelineActivity.this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void update(Observable observable, Object data) {
		Message message = (Message) data;
		myHandler.sendMessage(message);
	}

	/**
	 * 初始化当前Activity的Fragment
	 */
	private void initFragment() {
		fragment = new TimelineFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(TimelineFragment.ENABLE_PULL_REFRESH, true);
		bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
		bundle.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(TimelineURLParser.class,
				TimelineJSONParser.class));
		fragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().add(R.id.timeline_fragment_container, fragment).commit();
	}

	public void showPostTweetSuccess() {
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(TimelineActivity.this, getString(R.string.post_tweet_success),Toast.LENGTH_SHORT).show();
			}
		});
	}

}
