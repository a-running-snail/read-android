package com.jingdong.app.reader.bookstore.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookCartActivity;
import com.jingdong.app.reader.activity.BookPageImageEnlargeActivity;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.activity.LauncherActivity.TabChangeListener;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.activity.SettingActivity;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager.GetTotalCountListener;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper.GetCacheDataListener;
import com.jingdong.app.reader.bookstore.search.BookStoreSearchActivity;
import com.jingdong.app.reader.bookstore.style.controller.BookStoreStyleContrller;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.extra.BookStoreEntity;
import com.jingdong.app.reader.entity.extra.BookStoreEntity.MainThemeList;
import com.jingdong.app.reader.pay.OnlinePayActivity;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.util.share.ShareResultListener;
import com.jingdong.app.reader.util.share.WXShareHelper;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.tendcloud.tenddata.TCAgent;

public class BookStoreRootFragment extends CommonFragment implements TopBarViewListener, TabChangeListener {

	private ViewPager mBookStoreViewPager;
	private BookstorePagerAdapter mViewPagerAdapter;
	//各个子模块
	private static final List<Fragment> mFragments = new ArrayList<Fragment>();
	private static final String LauncherActivity = null;
	protected static final int GET_TOTAL_COUNT = 0x010;
	private onBannerAttachListener mBannerAttachListener;
	private static TopBarView topBarView = null;
	private FragmentManager mFragmentManager;

	private static final String TAG = "BookStoreRootFragment";
	public static int mCurrentPos = 0;
	private LinearLayout mContainerView;
	private static BookStoreEntity sBookStoreEntity;
	private EmptyLayout mEmptyLayout;

	public BookStoreRootFragment() {
		super();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		//设置监听器
		((LauncherActivity) activity).setTabChangeListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		LinearLayout root = (LinearLayout) inflater.inflate(R.layout.bookstore_root_layout, null);
		//顶部标题栏（首页、优惠、排行、分类）
		topBarView = (TopBarView) root.findViewById(R.id.topbar);
		//加载失败或者没有书城内容时的提醒
		mEmptyLayout = (EmptyLayout) root.findViewById(R.id.error_layout);
		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//获取书城数据
				getBookStoreFrameworkData();
			}
		});
		//初始化书城顶部
		initTopbarView();
		mContainerView = (LinearLayout) root.findViewById(R.id.container);
		//设置内容（容器为ViewPager）
		View layout = inflater.inflate(R.layout.activity_bookstore, mContainerView);
		mBookStoreViewPager = (ViewPager) layout.findViewById(R.id.pager);
		if (mViewPagerAdapter == null) {
			//书城底部Tab和顶部标题栏之间是一个左右滑动的控件
			updatePagerAdapter();
		}
		getBookStoreFrameworkData();
		return root;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MZLog.d(TAG, "===========>onReume");
		if (mBookStoreViewPager != null) {
			mBookStoreViewPager.setCurrentItem(mCurrentPos);
		}
		LauncherActivity act = (LauncherActivity) getActivity();
		act.onWindowFocusChanged(true);
		updateBookCart();

		int bookStoreIndex = act.getBookStoreIndex();
		MZLog.d("J.Beyond", "BookStoreRootFragment-->onResume-->bookStoreIndex:" + bookStoreIndex);
		if (bookStoreIndex == 100) {
			mBookStoreViewPager.setCurrentItem(0);
		}
//		refeshViewPagerData();
		//由后台进入书城，需处理刷新逻辑
		processAutoRefresh();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * 
	 * @Title: getBookStoreFrameworkData
	 * @Description: 获取书城框架数据
	 * @param 
	 * @return void
	 * @throws
	 * @date 2015年5月25日 下午7:34:58
	 */
	private void getBookStoreFrameworkData() {
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		BookStoreDataHelper bookStoreDataHelper = BookStoreDataHelper.getInstance();
		//请求数据
		bookStoreDataHelper.getBookStoreFramework(getActivity(), new GetCacheDataListener() {

			/**
			 * 成功回调
			 */
			@Override
			public void onSuccess(Serializable seri) {
				sBookStoreEntity = (BookStoreEntity)seri;
				//设置顶部标题栏(处理中间主内容布局)
				updateTopbarView(sBookStoreEntity);
				//隐藏错误信息
				mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
			}

			@Override
			public void onFail() {
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			}

			@Override
			public void onSuccess(Map<String, Serializable> moduleMap) {
				
			}
		});
	}


	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		MZLog.d(TAG, "bookstore root fragment-----");
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		MZLog.d(TAG, "onHiddenChanged:"+hidden);
		//触发自动刷新逻辑
		if (!hidden) {
			processAutoRefresh();
		}
	}

	/**
	 * 
	 * @Title: processAutoRefresh
	 * @Description: 处理自动刷新逻辑
	 * @param 
	 * @return void
	 * @throws
	 * @date 2015年5月25日 下午5:33:30
	 */
	private void processAutoRefresh() {
		int index = mBookStoreViewPager.getCurrentItem();
		if (sBookStoreEntity != null && sBookStoreEntity.mainThemeList != null) {
			MainThemeList mainThemeList = sBookStoreEntity.mainThemeList.get(index);
			if (mFragments.size() != 0) {
				Fragment fragment = mFragments.get(index);
				if (fragment == null) {
					return;
				}
				if (fragment instanceof BookStoreChildFragment) {
					((BookStoreChildFragment) fragment).autoRefresh(mainThemeList);
				}
			}
		}
	}

	/**
	 * 
	 * @Title: updateBookCart
	 * @Description: 更新购物车数量
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年5月19日 下午2:33:30
	 */
	public void updateBookCart() {
		BookCartManager.getInstance().getTotalCount(getActivity(), new GetTotalCountListener() {

			@Override
			public void onResult(int length) {
				MZLog.d("J", "更新购物车数量..." + length);
				if (length > 0 && length < 100) {
					topBarView.addNotificationOnMenu(false, true, true, "" + length);
				} else if (length > 99) {
					topBarView.addNotificationOnMenu(false, true, true, "99+");
				} else {
					topBarView.addNotificationOnMenu(false, true, true, "");
				}
			}
		});

	}

	/**
	 * 更新
	 */
	@SuppressWarnings("deprecation")
	public void updatePagerAdapter() {
		//初始化
		mViewPagerAdapter = new BookstorePagerAdapter(getChildFragmentManager());
		mBookStoreViewPager.setAdapter(mViewPagerAdapter);
		mBookStoreViewPager.setOffscreenPageLimit(4);
		//书城选中内容切换
		mBookStoreViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				topBarView.onTitleItemClick(position);
				// 埋点
				talkingDataPageFollow(position);
			}
		});
	}

	

	/**
	 * 更新书城数据
	 * @param entity
	 */
	public void updateTopbarView(BookStoreEntity entity) {
		// 获取顶部菜单
		List<String> titleList = new ArrayList<String>();
		for (int i = 0; i < entity.mainThemeList.size(); i++) {
			titleList.add(entity.mainThemeList.get(i).showName);
		}

		//设置顶部标题栏样式
		if (topBarView == null)
			return;

		//设置标题Tab
		topBarView.setTitleItem(titleList);
		topBarView.updateTopBarView();
		// 实例化相应数目的fragment

		if (mFragments.size() > 0) {
			mFragments.clear();
		}

		//设置顶部标题栏中间内容
		for (int i = 0; i < titleList.size() && i < 4; i++) {
			// 最多4个
			Bundle bundle = new Bundle();
			bundle.putSerializable("data", entity.mainThemeList.get(i));
			bundle.putInt("page_index", i);
			if (entity.mainThemeList.get(i).modules == null)
				continue;
			
			if (entity.mainThemeList.get(i).sort == 1) {//书城首页
				BookStoreIndexFragment fragment = new BookStoreIndexFragment();
				fragment.setArguments(bundle);
				mFragments.add(fragment);
			}else if (entity.mainThemeList.get(i).sort == 4) {//书城分类
				BookStoreCategoryNewFragment fragment = new BookStoreCategoryNewFragment();
				mFragments.add(fragment);
			}else { //优惠和排行
				BookStoreChildFragment fragment = new BookStoreChildFragment();
				fragment.setArguments(bundle);
				mFragments.add(fragment);
			}
		}
		if (mViewPagerAdapter != null) {
			mViewPagerAdapter.notifyDataSetChanged();
		} else {
			updatePagerAdapter();
		}
		// 更新页面
		if (getActivity() != null) {
			TCAgent.onPageStart(getActivity(), "书城_首页");
		} else {
			TCAgent.onPageStart(MZBookApplication.getInstance(), "书城_首页");
		}
	}

	/**
	 * 初始化顶部标题栏<br />
	 * 功能：<br />
	 * 1、设置左右侧图标<br />
	 * 2、设置标题（空）<br />
	 * 3、设置点击事件处理者<br />
	 * 
	 */
	public void initTopbarView() {
		if (topBarView == null)
			return;
		//设置左侧搜索图标				
		topBarView.setLeftMenuVisiable(true, R.drawable.topbar_search);
		//购物车图标
		topBarView.setRightMenuOneVisiable(true, R.drawable.topbar_cart, false);
		//没有标题（因为中间有导航内容）
		topBarView.setTitle("");
		//绑定点击事件
		topBarView.setListener(this);
	}

	/**
	 * 书城Adapter
	 *
	 */
	public class BookstorePagerAdapter extends FragmentStatePagerAdapter {

		public BookstorePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			MZLog.d("BookstorePagerAdapter", "getItem=" + i);
			return mFragments.get(i);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

	}

	/**
	 * 顶部左侧菜单被点击，跳转至检索
	 */
	@Override
	public void onLeftMenuClick() {
		//检索
		Intent intent = new Intent();
		intent.setClass(getActivity(), BookStoreSearchActivity.class);
		startActivity(intent);
		TalkingDataUtil.onBookStoreEvent(getActivity(), "搜索", "PV");
	}

	@Override
	public void onCenterMenuItemClick(int position) {
		if (mBookStoreViewPager != null) {
			mBookStoreViewPager.setCurrentItem(position);
		}
	}

	/**
	 * 顶部右侧菜单被点击，跳转至购物车
	 */
	@Override
	public void onRightMenuOneClick() {
		//跳转购物车页面
		Intent itIntent = new Intent(getActivity(), BookCartActivity.class);
		startActivity(itIntent);
		TalkingDataUtil.onBookStoreEvent(getActivity(), "购物车", "PV");
	}

	@Override
	public void onRightMenuTwoClick() {

	}

	@Override
	public void onTabChange(int tabIndex) {
		if (getActivity() != null) {
			switch (mCurrentPos) {
			case 0:
				TCAgent.onPageEnd(getActivity(), "书城_首页");
				break;
			case 2:
				TCAgent.onPageEnd(getActivity(), "书城_排行");
				break;
			case 3:
				TCAgent.onPageEnd(getActivity(), "书城_分类");
				break;
			}
		}
	}
	
	/**
	 * talking data页面追踪
	 * 
	 * @param position
	 */
	private void talkingDataPageFollow(int position) {
		if (position == 1) {
			// 进入优惠页
			switch (mCurrentPos) {
			case 0:
				TCAgent.onPageEnd(getActivity(), "书城_首页");
				break;
			case 2:
				TCAgent.onPageEnd(getActivity(), "书城_排行");
				break;
			case 3:
				TCAgent.onPageEnd(getActivity(), "书城_分类");
				break;
			}
			TCAgent.onPageStart(getActivity(), "书城_优惠");
			if (mBannerAttachListener != null) {
				mBannerAttachListener.onAttach();
			}

		} else if (position == 2) {
			switch (mCurrentPos) {
			case 0:
				TCAgent.onPageEnd(getActivity(), "书城_首页");
				break;
			case 1:
				TCAgent.onPageEnd(getActivity(), "书城_优惠");
				if (mBannerAttachListener != null) {
					mBannerAttachListener.onDisattach();
				}
				break;
			case 3:
				TCAgent.onPageEnd(getActivity(), "书城_分类");
				break;
			}
			TCAgent.onPageStart(getActivity(), "书城_排行");

		} else if (position == 3) {
			switch (mCurrentPos) {
			case 0:
				TCAgent.onPageEnd(getActivity(), "书城_首页");
				break;
			case 1:
				TCAgent.onPageEnd(getActivity(), "书城_优惠");
				if (mBannerAttachListener != null) {
					mBannerAttachListener.onDisattach();
				}
				break;
			case 2:
				TCAgent.onPageEnd(getActivity(), "书城_排行");
				break;
			}
			TCAgent.onPageStart(getActivity(), "书城_分类");

		} else if (position == 0) {
			switch (mCurrentPos) {
			case 3:
				TCAgent.onPageEnd(getActivity(), "书城_分类");
				break;
			case 1:
				TCAgent.onPageEnd(getActivity(), "书城_优惠");
				if (mBannerAttachListener != null) {
					mBannerAttachListener.onDisattach();
				}
				break;
			case 2:
				TCAgent.onPageEnd(getActivity(), "书城_排行");
				break;
			}
			TCAgent.onPageStart(getActivity(), "书城_首页");
		}
		mCurrentPos = position;
	}
	
	public void setBannerAttachListener(onBannerAttachListener bannerAttachListener) {
		this.mBannerAttachListener = bannerAttachListener;
	}


	/**
	 * 轮播页显示/隐藏 接口
	 * 
	 * @author ouyanghaibing
	 *
	 */
	public interface onBannerAttachListener {
		/**
		 * 轮播Banner显示时回调
		 */
		public void onAttach();

		/**
		 * 轮播Banner隐藏时回调
		 */
		public void onDisattach();
	}
}
