package com.jingdong.app.reader.message.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.jingdong.app.reader.common.MZReadCommonFragmentActivity;
import com.jingdong.app.reader.message.adapter.MessagePageAdapter;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.ui.ActionBarHelper;
import com.jingdong.app.reader.ui.ViewPagerTabBarHelper;
import com.jingdong.app.reader.R;

public class MessageActivity extends MZReadCommonFragmentActivity {

	private Button[] buttons;
	private PagerAdapter pagerAdapter;
	private ViewPagerTabBarHelper tabbarHelper;
	private ViewPager viewPager;
	private final ImageSpan imageSpan = new ImageSpan(this, R.drawable.icon_unread_red_dot, ImageSpan.ALIGN_BASELINE);
	private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// refreshLogo();
		}

	};

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(NotificationService.NOTIFICATION_ACTION);
		LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver, filter);

	}

	private void unregisterReceiver() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_message);
		String[] tabNames = getResources().getStringArray(R.array.message_tab_names);
		viewPager = (ViewPager) findViewById(R.id.pager);
		pagerAdapter = new MessagePageAdapter(getSupportFragmentManager(), tabNames);
		viewPager.setAdapter(pagerAdapter);
		tabbarHelper = new ViewPagerTabBarHelper(this, tabNames.length, viewPager);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				onPagerSelected(position, tabbarHelper);
			}
		});
		initButtons(tabNames);
		tabbarHelper.setSelectItem(0);
		getApplicationContext().bindService(new Intent(getApplicationContext(), NotificationService.class),
				new ServiceConnection() {
					@Override
					public void onServiceDisconnected(ComponentName name) {
					}

					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
					}
				}, BIND_AUTO_CREATE);
		ActionBarHelper.customActionBarBack(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public int getIndex() {
		return viewPager.getCurrentItem();
	}

	/**
	 * 初始化tabs，在这里tabs将替代viewpagerindicator
	 * 
	 * @return
	 */
	private void initButtons(String[] tabNames) {
		buttons = tabbarHelper.getTabButtons();
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setTag(tabNames[i]);
			setButtonText(i);
		}
	}

	/**
	 * 该方法负责处理viewPager的切换事件
	 * 
	 * @param position
	 *            切换后的viewpager的索引
	 * @param helper
	 *            一个helper对象，负责更新顶部tab的状态。
	 */
	private void onPagerSelected(int position, ViewPagerTabBarHelper helper) {
		Notification notification = Notification.getInstance();
		switch (position) {
//		case 0:
//			notification.setReadAtMeCount(MessageActivity.this);
//			break;
//		case 1:
//			notification.setReadCommentsCount(MessageActivity.this);
//			break;
//		case 2:
//			notification.setReadMessagesCount(MessageActivity.this);
//			break;
//		case 3:
//			notification.setReadGeneralAlertsCount(MessageActivity.this);
//			break;
		}
		setButtonText(position);
		helper.setSelectItem(position);
		Object fragment = pagerAdapter.instantiateItem(viewPager, position);
		if (fragment instanceof TimelineFragment) {
			TimelineFragment timelineFragment = (TimelineFragment) fragment;
			if (timelineFragment != null)
				timelineFragment.update();
		}
	}

	/**
	 * 为每个button设置文本和图片
	 * 
	 * @param index
	 *            该button在button数组中的索引
	 */
	private void setButtonText(int index) {
		Button button = buttons[index];
		Notification notification = Notification.getInstance();
		int number;
		SpannableString spannableString;
		switch (index) {
		case 0:
			number = notification.getAtMeCount();
			break;
		case 1:
			number = notification.getCommentsCount();
			break;
		case 2:
			number = notification.getMessagesCount();
			break;
		case 3:
			number = notification.getAlertsCount();
			break;
		default:
			number = 0;
		}
		if (number == 0)
			spannableString = new SpannableString((CharSequence) button.getTag());
		else {
			spannableString = new SpannableString(button.getTag() + "   ");
			spannableString.setSpan(imageSpan, spannableString.length() - 1, spannableString.length(),
					SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		button.setText(spannableString);
	}
}
