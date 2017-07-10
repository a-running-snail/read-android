package com.jingdong.app.reader.bookstore.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper.GetCacheDataListener;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper.RefreshCallback;
import com.jingdong.app.reader.bookstore.style.controller.BannerViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.BookStoreStyleContrller;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController.OnHeaderActionClickListener;
import com.jingdong.app.reader.bookstore.style.controller.RankingListViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.SpecialViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.TopicsViewStyleController;
import com.jingdong.app.reader.cache.CacheManager;
import com.jingdong.app.reader.entity.extra.BookStoreEntity.MainThemeList;
import com.jingdong.app.reader.entity.extra.BookStoreEntity.Modules;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity;
import com.jingdong.app.reader.util.BookStoreCacheManager;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.bookstore.PullToRefreshBase;
import com.jingdong.app.reader.view.bookstore.PullToRefreshBase.OnRefreshListener;
import com.jingdong.app.reader.view.bookstore.PullToRefreshBase.State;
import com.jingdong.app.reader.view.bookstore.PullToRefreshScrollView;

/**
 * 
 * @ClassName: BookStoreChildFragment
 * @Description: 书城首页、优惠、排行等页面
 * @author J.Beyond
 * @date 2015年5月21日 下午3:45:49
 *
 */
public class BookStoreChildFragment extends BookStoreBaseFragment {

	private static final String TAG = "BookStoreChildFragment";
	private LinearLayout mContainerLayout;
	private Activity mActivity;
	private MainThemeList mainThemeList;
	private String[] tabText = { "首页", "优惠", "排行" };
	private List<Map<String, Serializable>> moduleEntityList = new ArrayList<Map<String, Serializable>>();
	private int mCurrentPage = 0;
	private int mLastModuleType = 0;
	// private ProgressBar mProgressBar = null;
	public PullToRefreshScrollView mPullRefreshScrollView = null;
	/** 标志位，标志已经初始化完成 */
	private boolean isPrepared;
	/** 是否已被加载过一次，第二次就不再去请求数据了 */
	private boolean mHasLoadedOnce;
	private View mFragmentView;

	private int mInitModuleCount = 0;

	public BookStoreChildFragment() {
		super();
	}

	RefreshCallback mRefresh = new RefreshCallback() {

		@Override
		public void onRefreshFinish() {
			mContainerLayout.removeAllViews();
			for (int i = 0; i < mainThemeList.modules.size(); i++) {
				Modules module = mainThemeList.modules.get(i);
				if (module == null) {
					continue;
				}
				// mCurrentPageModules.offer(module);
				// modType=m.moduleType;
				String key = BookStoreDataHelper.BOOKSTORE_MODULE_CACHE_KEY_PREFIX + module.id + "_" + module.moduleType;
				for (int j = 0; j < moduleEntityList.size(); j++) {
					Map<String, Serializable> moduleMap = moduleEntityList.get(j);
					for (Map.Entry<String, Serializable> entry : moduleMap.entrySet()) {
						if (key.equals(entry.getKey())) {
							try {
								initView(key, module.moduleType, (BookStoreModuleBookListEntity) entry.getValue());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			mPullRefreshScrollView.onRefreshComplete();
			mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
		}

		@Override
		public void onRefreshFail() {
			mPullRefreshScrollView.onRefreshComplete();
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
		}
	};
	private EmptyLayout mEmptyLayout;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mFragmentView == null) {
			mFragmentView = inflater.inflate(R.layout.bookstore_fragment_one, null);
			mContainerLayout = (LinearLayout) mFragmentView.findViewById(R.id.holder);
			mEmptyLayout = (EmptyLayout) mFragmentView.findViewById(R.id.error_layout);
			mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					lazyLoad();
				}
			});
			mPullRefreshScrollView = (PullToRefreshScrollView) mFragmentView.findViewById(R.id.scrollvessel);
			/**
			 * 上拉监听函数
			 */
			mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

				@Override
				public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
					MZLog.d(TAG, "onRefresh");
					refreshNewData();
				}
			});
			isPrepared = true;
			lazyLoad();

		}
		// 因为共用一个Fragment视图，所以当前这个视图已被加载到Activity中，必须先清除后再加入Activity
		ViewGroup parent = (ViewGroup) mFragmentView.getParent();
		if (parent != null) {
			parent.removeView(mFragmentView);
		}

		return mFragmentView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		MZLog.i(TAG, "page:" + mCurrentPage + " isVisibleToUser:" + isVisibleToUser);
		boolean isNetworkConnected = NetWorkUtils.isNetworkConnected(mActivity);
		if (isVisibleToUser && isPrepared && mHasLoadedOnce && isNetworkConnected) {
			autoRefresh(mainThemeList);
		}

		if(isVisibleToUser){
			StatService.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_bookstore_discount) + tabText[mCurrentPage]);
		}else{
			StatService.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_bookstore_discount) + tabText[mCurrentPage]);
		}
	}

	/**
	 * 
	 * @Title: autoRefresh
	 * @Description: 执行自动刷新
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年5月25日 下午5:00:25
	 */
	public void autoRefresh(MainThemeList themeList) {
		if (themeList != null && themeList.modules != null) {
			Modules module = themeList.modules.get(0);
			if (module != null) {
				String cacheKey = BookStoreDataHelper.BOOKSTORE_MODULE_CACHE_KEY_PREFIX + module.id + "_" + module.moduleType;
				if (CacheManager.isCacheDataFailure(mActivity, cacheKey)) {
					SystemClock.sleep(300);
					mPullRefreshScrollView.setState(State.REFRESHING, true);
					new Handler().post(new Runnable() {

						@Override
						public void run() {
							// 将ScrollView移动到顶部
							ScrollView scrollView = mPullRefreshScrollView.getRefreshableView();
							if (scrollView != null) {
								scrollView.fullScroll(ScrollView.FOCUS_UP);
							}
						}
					});
				}
			}
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		MZLog.d(TAG, "----->onResume");
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		MZLog.d(TAG, "----->onHiddenChanged:" + hidden);
	}

	/**
	 * 
	 * @Title: initPageData
	 * @Description: 获取书城数据
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年5月20日 下午1:23:16
	 */
	private void initPageData() {

		Bundle arguments = getArguments();
		mCurrentPage = arguments.getInt("page_index");

		mainThemeList = (MainThemeList) arguments.getSerializable("data");

		if (mainThemeList == null) {
			return;
		}
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		BookStoreDataHelper bookStoreDataHelper = BookStoreDataHelper.getInstance();

		// mPullRefreshScrollView.setState(State.REFRESHING, true);
		// mProgressBar.setVisibility(View.VISIBLE);
		if (null != mainThemeList.modules) {
			for (int i = 0; i < mainThemeList.modules.size(); i++) {
				final Modules modules = mainThemeList.modules.get(i);
				bookStoreDataHelper.getModuleData(mActivity, modules, new GetCacheDataListener() {

					@Override
					public void onSuccess(Serializable seri) {

					}

					@Override
					public void onFail() {
						mRefresh.onRefreshFail();

					}

					@Override
					public void onSuccess(Map<String, Serializable> moduleMap) {
						if (mInitModuleCount == 0) {
							moduleEntityList.clear();
						}
						moduleEntityList.add(moduleMap);
						mInitModuleCount++;
						if (mInitModuleCount == mainThemeList.modules.size()) {
							mRefresh.onRefreshFinish();
							mHasLoadedOnce = true;
						}
					}
				});
			}
		}
		
	}

	/**
	 * 
	 * @Title: refreshNewData
	 * @Description: 下拉刷新加载最新数据
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年5月20日 下午1:34:42
	 */
	public synchronized void refreshNewData() {
		if (!NetWorkUtils.isNetworkConnected(mActivity)) {
			CustomToast.showNetErrorToast(mActivity, "网络不给力");
			mPullRefreshScrollView.onRefreshComplete();
			return;
		}
		
		mInitModuleCount = 0;
		if (null != mainThemeList.modules) {
			for (int i = 0; i < mainThemeList.modules.size(); i++) {
				final Modules modules = mainThemeList.modules.get(i);
				BookStoreDataHelper bookStoreDataHelper = BookStoreDataHelper.getInstance();
				bookStoreDataHelper.requestModule(mActivity, modules, new GetCacheDataListener() {

					@Override
					public void onSuccess(Serializable seri) {

					}

					@Override
					public void onFail() {
						mRefresh.onRefreshFail();
					}

					@Override
					public void onSuccess(Map<String, Serializable> moduleMap) {
						if (mInitModuleCount == 0) {
							moduleEntityList.clear();
						}
						moduleEntityList.add(moduleMap);
						mInitModuleCount++;
						if (mInitModuleCount == mainThemeList.modules.size()) {
							mRefresh.onRefreshFinish();
							mHasLoadedOnce = true;
						}
					}
				});
			}
		}
		
	}

	/**
	 * Fragment显示的时候调用
	 */
	@Override
	protected void lazyLoad() {
		if (!isVisible || !isPrepared || mHasLoadedOnce) {
			return;
		}
		initPageData();
	}
//	
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		this.mHasLoadedOnce = false;
//		this.mFragmentView = null;
//	}

	/**
	 * 
	 * @Title: initView
	 * @Description: 使用实体数据初始化界面
	 * @param @param name
	 * @param @param modType
	 * @param @param entity
	 * @param @throws Exception
	 * @return void
	 * @throws
	 * @date 2015年5月21日 下午3:52:16
	 */
	private void initView(String name, int modType, final BookStoreModuleBookListEntity entity) throws Exception {
		int n = 0;
		boolean isNeedBottomLine = false;
		boolean isNeedBottomDivider = false;
		boolean isNeedTop24Divider = false;
		boolean isNeedBottom24Divider = false;
		boolean isNeedSpecialTopLine = false;
		if (getActivity() == null) {
			MZLog.d("wangguodong", "getActivity() is " + getActivity());
		}
		if (entity == null) {
			MZLog.d("wangguodong", "entity is " + entity);
			return;
		}
		View view = null;
		switch (modType) {

		case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_1:
		case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_2:
		case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_3:
		case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_4:
			view = BookStoreCacheManager.getInstance().getView(name);
			// 书城顶部专题部分view
			if (view == null) {
				view = TopicsViewStyleController.getTopicsStyleView(getActivity(), 1, 4, entity.moduleLinkChildList, null);
			}

			if (modType == BookStoreStyleContrller.TYPE_CIRCLE_LABEL_1 || modType == BookStoreStyleContrller.TYPE_CIRCLE_LABEL_3) {
				isNeedTop24Divider = true;
			} else {
				isNeedBottom24Divider = true;
			}
			break;

		case BookStoreStyleContrller.TYPE_BOOK_LIST: {

			int childType = BookStoreStyleContrller.TYPE_BOOK_LIST * 10 + entity.moduleBookChild.showType;
			int row = 0;

			if (childType == BookStoreStyleContrller.TYPE_BOOK_LIST_GRID) {

				if (entity.resultList != null)
					row = (int) Math.floor(entity.resultList.size() / 3.0);
				view = BookStoreCacheManager.getInstance().getView(name);
				if (view == null) {
					view = BooksViewStyleController.getBooksStyleView(getActivity(), entity.moduleBookChild.showName,entity.moduleBookChild.showInfo, "更多", row, 3, entity.resultList,
							new OnHeaderActionClickListener() {

								@Override
								public void onHeaderActionClick() {
									TalkingDataUtil.onBookStoreEvent(getActivity(), tabText[mCurrentPage], entity.moduleBookChild.showName);
									Intent intent = new Intent(getActivity(), BookStoreBookListActivity.class);
									intent.putExtra("fid", entity.moduleBookChild.id);
									intent.putExtra("ftype", 2);
									intent.putExtra("relationType", 1);
									intent.putExtra("showName", entity.moduleBookChild.showName);
									intent.putExtra("showInfo", entity.moduleBookChild.showInfo);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								}
							});
				}

				isNeedBottomLine = true;
				isNeedBottomDivider = true;
			} else if (childType == BookStoreStyleContrller.TYPE_BOOK_LIST_VERTICAL) {

				if (entity.resultList != null)
					view = BookStoreCacheManager.getInstance().getView(name);
				row = (int) Math.floor(entity.resultList.size() / 1.0);
				if (view == null)
					view = RankingListViewStyleController.getRankingListStyleView(getActivity(), entity.moduleBookChild.showName, "更多", row, 1,
							entity.resultList, new OnHeaderActionClickListener() {

								@Override
								public void onHeaderActionClick() {
									TalkingDataUtil.onBookStoreEvent(getActivity(), tabText[mCurrentPage], entity.moduleBookChild.showName);

									Intent intent = new Intent(getActivity(), BookStoreBookListActivity.class);
									intent.putExtra("fid", entity.moduleBookChild.id);
									intent.putExtra("ftype", 2);
									intent.putExtra("relationType", 1);
									intent.putExtra("showName", entity.moduleBookChild.showName);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								}
							});

			}
		}
			break;
		case BookStoreStyleContrller.TYPE_SPECIAL_THEME:
			if (getActivity() == null) {
				return;
			}
			List<BookStoreModuleBookListEntity.ModuleBookChild> childs = new ArrayList<BookStoreModuleBookListEntity.ModuleBookChild>();
			childs.add(entity.moduleBookChild);
			view = BookStoreCacheManager.getInstance().getView(name);
			if (view == null)
				view = SpecialViewStyleController.getSpecialStyleView(getActivity(), childs);

			break;

		case BookStoreStyleContrller.TYPE_BOOK_BANNER:
			BannerViewStyleController bannerViewController = BannerViewStyleController.getInstance();
			BookStoreRootFragment rootFragment = (BookStoreRootFragment) this.getParentFragment();
			if (rootFragment != null) {
				rootFragment.setBannerAttachListener(bannerViewController);
			}
			MZLog.d(TAG, "banner entity:" + GsonUtils.toJson(entity));
			view = bannerViewController.getBannerView(getActivity(), entity.moduleLinkChildList);
			break;

		case BookStoreStyleContrller.TYPE_BOOK_CATEGORY:

			break;

		}
		if (null != view) {

			if (isNeedTop24Divider)
				mContainerLayout.addView(getDividerView(24));
			if (isNeedSpecialTopLine)
				mContainerLayout.addView(getLineView());
			if (modType != BookStoreStyleContrller.TYPE_SPECIAL_THEME && mLastModuleType == BookStoreStyleContrller.TYPE_SPECIAL_THEME) {
				mContainerLayout.addView(getDividerView(8));
			}
			if (modType == BookStoreStyleContrller.TYPE_SPECIAL_THEME && mLastModuleType != BookStoreStyleContrller.TYPE_SPECIAL_THEME) {
				mContainerLayout.addView(getLineView());
			}

			mContainerLayout.addView(view);
			mLastModuleType = modType;// 标记上次module类型

			if (isNeedBottom24Divider)
				mContainerLayout.addView(getDividerView(24));

			if (isNeedBottomLine)
				mContainerLayout.addView(getLineView());
			if (isNeedBottomDivider)
				mContainerLayout.addView(getDividerView(8));
		}
		// isCurrentRequestOver = true;
	}

	public View getLineView() {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.lineview, null);
		return view;
	}

	public View getDividerView(int height) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.bookstore_divider, null);
		if (height != 8) {
			View divider = view.findViewById(R.id.divider);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.height = ScreenUtils.dip2px(getActivity(), height);
			divider.setLayoutParams(params);
		}
		return view;
	}

}
