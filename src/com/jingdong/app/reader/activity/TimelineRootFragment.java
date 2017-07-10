package com.jingdong.app.reader.activity;

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
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.parser.BaseParserCreator;
import com.jingdong.app.reader.parser.json.TimelineJSONParser;
import com.jingdong.app.reader.parser.url.TimelineURLParser;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelinePostTweetActivity;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.timeline.model.TimelineActivityModel;
import com.jingdong.app.reader.ui.ActionBarHelper;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.FlowWindowUtils;

public class TimelineRootFragment extends CommonFragment implements Observer{

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
				model.postTweet(getActivity(), postTweetBundle);
				break;
			}
		}
	}

	private static class TimeLineHandler extends Handler {
		WeakReference<TimelineRootFragment> reference;

		public TimeLineHandler(TimelineRootFragment activity) {
			reference = new WeakReference<TimelineRootFragment>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			TimelineRootFragment activity = reference.get();
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
	private TimelineFragment fragment;
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
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
	}

	private void unregisterReceiver() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
	}

	private void refreshActionBarMenuUI() {
		if (actionBarHelper != null) {
			actionBarHelper.loadNewNotification(getActivity());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout=inflater.inflate(R.layout.activity_timeline, null);
		if (savedInstanceState == null)
			initFragment();
		executorService = NotificationService.getExecutorService();
		model = new TimelineActivityModel();
		model.addObserver(this);
		myHandler = new TimeLineHandler(this);
		flowUtils=new FlowWindowUtils();//悬浮窗口
		//if (LocalUserSetting.isFirstTimeUse(getActivity())) {
		//	Toast.makeText(getActivity(), "点击右上角可打开菜单", Toast.LENGTH_LONG).show();
		//}

		actionBarHelper = new ActionBarHelper();
		actionBarHelper.customActionBar(getActivity());
		return layout;
	}
	


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
	public void onResume() {
		super.onResume();
		registerReceiver();
		refreshActionBarMenuUI();
	}



	@Override
	public void onDestroy() {
		model.deleteObserver(this);
		myHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	/*
	@Override
	public void onBackPressed() {
		if (backPressTimes > 0) {
			//exit app
			MZBookApplication.exitMZBookApp(getActivity());
			super.onBackPressed();
		} else {
			Toast.makeText(getActivity(), getString(R.string.back_exit_warning),
					Toast.LENGTH_SHORT).show();
			++backPressTimes;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.timeline, menu);
		actionBarHelper.createDropdownButton(getActivity(), menu);
		refreshActionBarMenuUI();
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() *
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		// boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}
	
	*/
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver();
		flowUtils.removeView();
		backPressTimes = 0;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.dropdown_button) {
			actionBarHelper.createPopupMenu(getActivity(), getActivity().findViewById(R.id.actionbar_overlay));
			return true;
		} else {
			switch (item.getItemId()) {
			case R.id.timeline_post_tweet:
				flowUtils.addView(getActivity());
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}

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
		//getActivity().getSupportFragmentManager().beginTransaction().add(R.id.timeline_fragment_container, fragment).commit();
	}

	private void showPostTweetSuccess() {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(getActivity(), getString(R.string.post_tweet_success),Toast.LENGTH_SHORT).show();
			}
		});
	}

}
