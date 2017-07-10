package com.jingdong.app.reader.bookstore.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.bookstore.adapter.BookStoreIndexGridViewAdapter;
import com.jingdong.app.reader.bookstore.adapter.BookStoreListAdapter;
import com.jingdong.app.reader.bookstore.adapter.HeaderGridViewAdapter;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper.FetchRecommendDataListener;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper.GetCacheDataListener;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper.RefreshCallback;
import com.jingdong.app.reader.bookstore.style.controller.BannerViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.BookStoreStyleContrller;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController.OnHeaderActionClickListener;
import com.jingdong.app.reader.bookstore.style.controller.RankingListViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.SpecialViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.TopicsViewStyleController;
import com.jingdong.app.reader.bookstore.view.DownPartScrollView;
import com.jingdong.app.reader.bookstore.view.DownPartScrollView.DownPartCallback;
import com.jingdong.app.reader.bookstore.view.InnerScrollView;
import com.jingdong.app.reader.bookstore.view.MyGridView;
import com.jingdong.app.reader.bookstore.view.PullDownViewGroup;
import com.jingdong.app.reader.bookstore.view.PullDownViewGroup.PullDownViewCallBack;
import com.jingdong.app.reader.bookstore.view.UpperPartScrollView;
import com.jingdong.app.reader.entity.extra.BookStoreEntity.MainThemeList;
import com.jingdong.app.reader.entity.extra.BookStoreEntity.Modules;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity.BookStoreList;
import com.jingdong.app.reader.entity.extra.StoreBook;
import com.jingdong.app.reader.extension.integration.FloatingActionButton;
import com.jingdong.app.reader.util.BookStoreCacheManager;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.EmptyLayout;

/**
 * 
 * @ClassName: BookStoreIndexFragment
 * @Description: 书城首页
 * @author J.Beyond
 * @date 2015年6月25日 下午5:47:05
 *
 */
@SuppressLint("InflateParams")
public class BookStoreIndexNewFragment extends BookStoreBaseFragment {

	private static final String TAG = "BookStoreIndexFragment";
	private static final String BookStoreModuleBookListEntity = null;
	/** 标志位，标志已经初始化完成 */
	private boolean isPrepared;
	/** 是否已被加载过一次，第二次就不再去请求数据了 */
	private boolean mHasLoadedOnce;
	private View mFragmentView;

	private Activity mActivity;
	private MainThemeList mainThemeList;
	private List<Map<String, Serializable>> moduleEntityList = new ArrayList<Map<String, Serializable>>();
	private int mCurrentPage = 0;
	private int mLastModuleType = 0;
	private ListView mIndexLv;

	private int mInitModuleCount = 0;

	private EmptyLayout mEmptyLayout;

	public BookStoreIndexNewFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = activity;
		mInflater = LayoutInflater.from(mActivity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mFragmentView == null) {
			mFragmentView = inflater.inflate(R.layout.bookstore_index_new_fragment, null);
			initView(mFragmentView);
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

	RefreshCallback mRefresh = new RefreshCallback() {

		@Override
		public void onRefreshFinish() {
			// mRefreshableScrollView.removeAllViews();
			setupUI();
		}

		@Override
		public void onRefreshFail() {
			// mRefreshableScrollView.onRefreshComplete();
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
		}
	};
	private int currentPage = 1;
	private List<BookStoreModuleBookListEntity> mModuleList;
	private List<BookStoreModuleBookListEntity> mHeaderMudleList;
	private void setupUI() {
		if (mModuleList == null) {
			mModuleList = new ArrayList<BookStoreModuleBookListEntity>();
		}
		if (mHeaderMudleList == null) {
			mHeaderMudleList = new ArrayList<BookStoreModuleBookListEntity>();
		}
		
		mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);

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
//							initView(key, module.moduleType, (BookStoreModuleBookListEntity) entry.getValue());
							BookStoreModuleBookListEntity mudelEntity = (BookStoreModuleBookListEntity) entry.getValue();
							if (module.moduleType == BookStoreStyleContrller.TYPE_CIRCLE_LABEL_1 || module.moduleType == BookStoreStyleContrller.TYPE_CIRCLE_LABEL_2
									|| module.moduleType == BookStoreStyleContrller.TYPE_CIRCLE_LABEL_3 || module.moduleType == BookStoreStyleContrller.TYPE_CIRCLE_LABEL_4) {
								mHeaderMudleList.add(mudelEntity);
							}else{
								if (module.moduleType == BookStoreStyleContrller.TYPE_BOOK_LIST_GRID) {
									mudelEntity.viewType = 0;
								}else if (module.moduleType == BookStoreStyleContrller.TYPE_BOOK_LIST_VERTICAL) {
									mudelEntity.viewType = 1;
								}else if (module.moduleType == BookStoreStyleContrller.TYPE_SPECIAL_THEME) {
									mudelEntity.viewType = 2;
								}
								
								mModuleList.add(mudelEntity);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		mIndexLv.addHeaderView(setupHeaderView(mHeaderMudleList));
		BookStoreListAdapter storeListAdapter = new BookStoreListAdapter(mActivity, mModuleList);
		mIndexLv.setAdapter(storeListAdapter);
		
//		if (recommendFooter == null) {
//			recommendFooter = mInflater.inflate(R.layout.bookstore_index_recommend_header, null);
//		}
//		mTopScrollView.setFooterView(recommendFooter);
//		mUpContainer.addView(recommendFooter);
//
//		setupRecommentView();

	}

	private HeaderGridViewAdapter headerGridViewAdapter;

	private GridView setupHeaderView(List<BookStoreModuleBookListEntity> headerMudleList) {
		MyGridView headerGridView = new MyGridView(mActivity);
		headerGridView.setNumColumns(4);
		headerGridView.setGravity(Gravity.CENTER);
		headerGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		headerGridView.setPadding(40, 50, 0, 50);
		headerGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		if (headerGridViewAdapter == null) {
			headerGridViewAdapter = new HeaderGridViewAdapter(mActivity, headerMudleList);
		}else{
			headerGridViewAdapter.notifyDataSetChanged();
		}
		headerGridView.setAdapter(headerGridViewAdapter);
		return headerGridView;
	}
	
	
	
	

	private LayoutInflater mInflater;

	private void initView(View view) {
		mEmptyLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
		mIndexLv = (ListView) view.findViewById(R.id.index_new_lv);
	}

	@Override
	protected void lazyLoad() {
		if (!isVisible || !isPrepared || mHasLoadedOnce) {
			return;
		}
		initPageData();
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
		// 是否是书城首页
		mainThemeList = (MainThemeList) arguments.getSerializable("data");

		if (mainThemeList == null) {
			return;
		}
		// 加载中
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		BookStoreDataHelper bookStoreDataHelper = BookStoreDataHelper.getInstance();

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
						// mRefresh.onRefreshFinish();
						mHasLoadedOnce = true;
						setupUI();
					}
				}
			});
		}
	}




}
