package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseFragmentActivityWithTopBar;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * 通知，分为社区通知，借阅通知以及系统通知
 * @author tanmojie
 *
 */
public class NotificationMainActivity extends BaseFragmentActivityWithTopBar implements TopBarViewListener {

	private ViewPager pager;
	private BookstorePagerAdapter pagerAdapter;
	private TopBarView view = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_notification);
		initTopbarView();
		pager = (ViewPager) findViewById(R.id.vPager);
		pagerAdapter = new BookstorePagerAdapter(getSupportFragmentManager());
		pager.setAdapter(pagerAdapter);
		pager.setOffscreenPageLimit(3);

		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				view.dotMoveToPosition(position);
			}
		});
	}

	private void initTopbarView() {
		view = getTopBarView();
		if (view==null)
			return;
		List<String> item = new ArrayList<String>();
		item.add("社区");
		item.add("推荐");
		item.add("系统");
		view.setTitleItem(item);
		view.setListener(this);
		view.updateTopBarView();
	}	

	public class BookstorePagerAdapter extends FragmentPagerAdapter {

		public BookstorePagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0: {// 社区通知
				NotificationFragment fragment = new NotificationFragment();
				return fragment;
			}
			case 1: {// 推荐通知
				JDMessageFragment messagefragment = new JDMessageFragment();
				return messagefragment;
			}
			case 2: {// 系统通知（包含借阅以及赠书通知）
				BorrowBookMessageFragment fragment = new BorrowBookMessageFragment();
				return fragment;
			}
			default:
				return new NotificationFragment();
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	}

	
	@Override
	public void onLeftMenuClick() {
		finish();
	}

	@Override
	public void onRightMenuOneClick() {
		
	}

	@Override
	public void onRightMenuTwoClick() {
		
	}

	@Override
	public void onCenterMenuItemClick(int position) {
		if(pager!=null)
			pager.setCurrentItem(position);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_message_center_tongzhi));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_message_center_tongzhi));
	}
	
}
