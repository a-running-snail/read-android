package com.jingdong.app.reader.bookshelf;

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
 * 借阅activity ，分为用户借阅以及书城借阅两个fragment
 * @author tanmojie
 *
 */
public class BorrowBookMainActivity extends BaseFragmentActivityWithTopBar implements TopBarViewListener {

	private ViewPager pager;
	private BookstorePagerAdapter pagerAdapter;
	private TopBarView view = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_borrowbook);
		initTopbarView();
		pager = (ViewPager) findViewById(R.id.vPager);
		pagerAdapter = new BookstorePagerAdapter(getSupportFragmentManager());
		pager.setAdapter(pagerAdapter);
		pager.setOffscreenPageLimit(2);

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
		item.add("用户借阅");
		item.add("书城借阅");
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
			case 0: {// 用户借阅
				UserBorrowedBookFragment fragment = new UserBorrowedBookFragment();
				return fragment;
			}
			case 1: {// 书城借阅
				StoreBorrowedBookFragment fragment = new StoreBorrowedBookFragment();
				return fragment;
			}
			default:
				return new StoreBorrowedBookFragment();
			}
		}

		@Override
		public int getCount() {
			return 2;
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
