package com.jingdong.app.reader.bookstore.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookstore.adapter.BookStoreIndexGridViewAdapter;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper.FetchRecommendDataListener;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper.GetCacheDataListener;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper.RefreshCallback;
import com.jingdong.app.reader.bookstore.style.controller.BannerViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.BookStoreStyleContrller;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController.ImageLoadingListenerImpl;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController.OnHeaderActionClickListener;
import com.jingdong.app.reader.bookstore.style.controller.RankingListViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.SpecialViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.TopicsViewStyleController;
import com.jingdong.app.reader.bookstore.view.MyGridView;
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
import com.jingdong.app.reader.util.SettingUtils;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.refreshview.XRefreshView;
import com.jingdong.app.reader.view.refreshview.XRefreshView.XRefreshViewListener;
import com.jingdong.app.reader.view.refreshview.XScrollView;
import com.jingdong.app.reader.view.refreshview.XScrollView.onScrllListener;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 
 * @ClassName: BookStoreIndexFragment
 * @Description: 书城首页
 * @author J.Beyond
 * @date 2015年6月25日 下午5:47:05
 *
 */
@SuppressLint("InflateParams")
public class BookStoreIndexFragment extends BookStoreBaseFragment {

	private static final String TAG = "BookStoreIndexFragment";
	/** 标志位，标志已经初始化完成 */
	private boolean isPrepared;
	/** 是否已被加载过一次，第二次就不再去请求数据了 */
	private boolean mHasLoadedOnce;
	private View mFragmentView;

	private Activity mActivity;
	private MainThemeList mainThemeList;
	private List<Map<String, Serializable>> moduleEntityList = new ArrayList<Map<String, Serializable>>();
	private int mCurrentPage = 0;
	private int mInitModuleCount = 0;

	private EmptyLayout mEmptyLayout;
	/** 限时特价定时器 */
	private Timer mSpecialPriceTimer = null;
	/** 限时特价时间倒计时 */
	private long specialPriceCountdown = 0;
	/** 小时 */
	private final int HOUR = 60*60;
	/** 分钟 */
	private final int MINUTE = 60;
	
	/**
	 * 是否显示限时特价的倒计时
	 */
	private boolean showSpecialPriceCountDown = false;
	
	private LayoutInflater mInflater;
	private View recommendFooter;
	private BookStoreIndexGridViewAdapter mGuessYouLikeAdapter;
	private FloatingActionButton mTopFab;
	
	/** 专属推荐布局 */
	private LinearLayout recommentContainer;
	/** 上拉下拉控件 */
	private XRefreshView mXRefreshView;
	/** 总布局 */
	private LinearLayout mainLayout;
	/** 加在分页数据请求中标识 */
	private boolean isLoadMoreDataing = false;
	private XScrollView mXScrollView;
	private int currentPage = 1;
	private List<StoreBook> mRecomentList = new ArrayList<StoreBook>();
	boolean isNeedToHide = true;
	boolean isTopFabClicked = false;

	//***************************************************
	//限时特价
	public static final int DEFAULT_BOOKS_ROW = 2;
	public static final int DEFAULT_BOOKS_COLUMN = 3;
	public static final int DEFAULT_LEFT_MARGIN = 0; // 整个View左边距
	public static final int DEFAULT_RIGHT_MARGIN = 0; // 整个View右边距
	public static final int DEFAULT_BOOK_LEFT_MARGIN = 16; // 底部bookView左边距
	public static final int DEFAULT_BOOK_RIGHT_MARGIN = 16; // 底部bookView右边距
	public static final int DEFAULT_BOTTOM_MARGIN = 0; // 整个View底部边距
	public static final int DEFAULT_TOP_MARGIN = 0; // 整个View顶部边距
	public static final int DEFAULT_FIRST_ROW_TOP_MARGIN = 0; // 第一行离顶部view的距离
	public static final int DEFAULT_HORIZONTAL_DIVIDER_WIDTH = 11; //
	// 书籍中间的水平空隙宽度
	public static final int DEFAULT_VERTICAL_DIVIDER_WIDTH = 16; // 书籍中间的垂直空隙宽度
	public static final int DEFAULT_VIEW_BACKGROUND = 0x00ffffff;
	private TextView lefeTimeHourTv;
	private TextView lefeTimeMinuteTv;
	private TextView lefeTimeSecondTv;
	private LinearLayout leftTimeLayout;
	private LinearLayout endLayout;
	//***************************************************
	
	public BookStoreIndexFragment() {
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = activity;
		mInflater = LayoutInflater.from(mActivity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//初始化
		if (mFragmentView == null) {
			//设置布局
			mFragmentView = inflater.inflate(R.layout.bookstore_index_fragment, null);
			//设置布局内容以及各项事件处理
			initView(mFragmentView);
			isPrepared = true;
			//加载书城数据
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
	public void onResume() {
		super.onResume();
		if(showSpecialPriceCountDown)
			startSpecialPriceTimer();
	}
	
	/**
	* @Description: 启动限时特价定时器
	* @author xuhongwei1
	* @date 2015年11月18日 下午3:49:35 
	* @throws 
	*/ 
	private void startSpecialPriceTimer() {
		
		boolean getSpecialPriceTime = SettingUtils.getInstance().getBoolean("getSpecialPriceTime");
		long historySpecialPriceCountdown = SettingUtils.getInstance().getLong("SpecialPriceCountdown");
		if(historySpecialPriceCountdown <= 0) {
			return;
		}
		long specialPriceTime = SettingUtils.getInstance().getLong("SpecialPriceTime");
		if(getSpecialPriceTime) {
			specialPriceCountdown = specialPriceTime;
		}else {
			long timeMillis = SettingUtils.getInstance().getLong("SystemCurrentTimeMillis", 0);
			if(0 == timeMillis) {
				return;
			}
			long curTimeMillis = System.currentTimeMillis();
			long backstageTime = (curTimeMillis - timeMillis)/1000;	
			if(backstageTime > 0 && backstageTime < specialPriceTime) {
				specialPriceCountdown = specialPriceTime - backstageTime;
			}else {
				specialPriceCountdown = historySpecialPriceCountdown;
			}
		}
		
		closeSpecialPriceTimer();
		mSpecialPriceTimer = new Timer(true);
		mSpecialPriceTimer.schedule(new TimerTask() {  
            @Override  
            public void run() {  
            	updateSpecialPriceTime();	
            }  
        }, 0, 1000);  
	}
	
	/**
	* @Description: 关闭限时特价定时器
	* @author xuhongwei1
	* @date 2015年11月18日 下午3:49:14 
	* @throws 
	*/ 
	private void closeSpecialPriceTimer(){
		if(null != mSpecialPriceTimer) {
			mSpecialPriceTimer.cancel();
			mSpecialPriceTimer.purge();
			mSpecialPriceTimer = null;
		}
	}
	
	/**
	* @Description: 更新显示限时特价时间
	* @author xuhongwei1
	* @date 2015年11月18日 下午3:48:55 
	* @throws 
	*/ 
	private void updateSpecialPriceTime() {
		if(null == getActivity()) {
			return;
		}
		
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				specialPriceCountdown--;
				SettingUtils.getInstance().putLong("SpecialPriceCountdown", specialPriceCountdown);
				if(specialPriceCountdown <= 0) {
					specialPriceCountdown = 0;
					SettingUtils.getInstance().putLong("SystemCurrentTimeMillis", 0);
					closeSpecialPriceTimer();
					if(null != leftTimeLayout) {
						leftTimeLayout.setVisibility(View.GONE);	
					}
					if(null != endLayout) {
						endLayout.setVisibility(View.VISIBLE);
					}
					return;
				}
				
				int hour = (int)(specialPriceCountdown/HOUR);
				int minute = (int)((specialPriceCountdown - hour*HOUR)/MINUTE);
				int second = (int)(specialPriceCountdown - hour*HOUR - minute*MINUTE);
				
				if(null != lefeTimeHourTv) {
					String hourStr;
					if(hour < 10) {
						hourStr = "0" + hour;
					}else {
						hourStr = "" + hour;
					}
					lefeTimeHourTv.setText(hourStr);
				}
				if(null != lefeTimeMinuteTv) {
					String minuteStr;
					if(minute < 10) {
						minuteStr = "0" + minute;
					}else {
						minuteStr = "" + minute;
					}
					lefeTimeMinuteTv.setText(minuteStr);
				}
				if(null != lefeTimeSecondTv) {
					String secondStr;
					if(second < 10) {
						secondStr = "0" + second;
					}else {
						secondStr = "" + second;
					}
					lefeTimeSecondTv.setText(secondStr);
				}
				if(null != leftTimeLayout) {
					leftTimeLayout.setVisibility(View.VISIBLE);
				}
				if(null != endLayout) {
					endLayout.setVisibility(View.GONE);	
				}
			}
		});
	}
	

	@Override  
    public void setUserVisibleHint(boolean isVisibleToUser) {  
        super.setUserVisibleHint(isVisibleToUser);  
        if(getUserVisibleHint()) {
        	if(showSpecialPriceCountDown)
        		startSpecialPriceTimer();
            isVisible = true;  
            onVisible();  
            StatService.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_bookstore_home));
        } else {  
        	closeSpecialPriceTimer();
            isVisible = false;  
            onInvisible();  
            StatService.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_bookstore_home));
        }  
    }  

	//刷新回调
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
	

	/**
	 * 装载UI
	 */
	private void setupUI() {
		//隐藏错误信息
		mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
		for (int i = 0; i < mainThemeList.modules.size(); i++) {
			//模块数据检查
			Modules module = mainThemeList.modules.get(i);
			if (module == null) {
				continue;
			}
			String cacheKey = BookStoreDataHelper.BOOKSTORE_MODULE_CACHE_KEY_PREFIX + module.id + "_" + module.moduleType;
			//循环数据模块
			for (int j = 0; j < moduleEntityList.size(); j++) {
				Map<String, Serializable> moduleMap = moduleEntityList.get(j);
				for (Map.Entry<String, Serializable> entry : moduleMap.entrySet()) {
					if (cacheKey.equals(entry.getKey())) {
						try {
							initView(cacheKey/*缓存Key*/, module.moduleType, (BookStoreModuleBookListEntity) entry.getValue());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		if (recommendFooter == null) {
			recommendFooter = mInflater.inflate(R.layout.bookstore_index_recommend_header, null);
		}
		LinearLayout view = (LinearLayout)recommendFooter.getParent();
		if (null != view) {
			view.removeView(recommendFooter);
		}
		setupRecommentView();
	}

	private void fetchRecommendData() {
		if (!NetWorkUtils.isNetworkConnected(mActivity)) {
			CustomToast.showNetErrorToast(mActivity, "网络不给力");
			mXRefreshView.stopLoadMore();
			return;
		}
		
		if(isLoadMoreDataing) {
			return;
		}
		
		isLoadMoreDataing = true;
		
		if (currentPage == 1) {
			mRecomentList.clear();
		}
		BookStoreDataHelper bookStoreDataHelper = BookStoreDataHelper.getInstance();
		bookStoreDataHelper.fetchRecommendData(mActivity, currentPage, new FetchRecommendDataListener() {

			@Override
			public void onSuccess(BookStoreList bookStoreList) {
				isLoadMoreDataing = false;
				List<StoreBook> storeBooks = bookStoreList.resultList;
				mRecomentList.addAll(storeBooks);
				mGuessYouLikeAdapter.notifyDataSetChanged();
				if (currentPage > 1) {
					mXRefreshView.stopLoadMore();
				}
				currentPage++;
			}

			@Override
			public void onFailure() {
				isLoadMoreDataing = false;
				mXRefreshView.stopLoadMore();
			}
		});
	}

	/**
	 * 
	 * @Title: setupRecommentView
	 * @Description: 设置猜你喜欢布局
	 * @param
	 * @return void
	 * @throws
	 */
	protected void setupRecommentView() {
		if(null == mActivity || (null != mActivity && mActivity.isFinishing())) {
			return;
		}
		// 设置布局Margin参数
		float screenWidth = ScreenUtils.getWidthJust(mActivity);// px
		int left_margin = ScreenUtils.dip2px(mActivity, BooksViewStyleController.DEFAULT_LEFT_MARGIN);
		int right_margin = ScreenUtils.dip2px(mActivity, BooksViewStyleController.DEFAULT_RIGHT_MARGIN);
		ScreenUtils.dip2px(mActivity, BooksViewStyleController.DEFAULT_TOP_MARGIN);
		int bottom_margin = ScreenUtils.dip2px(mActivity, BooksViewStyleController.DEFAULT_BOTTOM_MARGIN);

		recommentContainer = new LinearLayout(mActivity);
		recommentContainer.setVisibility(View.GONE);
		recommentContainer.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
		recommentContainer.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = left_margin;
		params.rightMargin = right_margin;
		params.bottomMargin = bottom_margin;
		recommentContainer.setLayoutParams(params);
		recommentContainer.setGravity(Gravity.CENTER);

		View header = mInflater.inflate(R.layout.guess_you_like_header, null);
		recommentContainer.addView(header);

		MyGridView gridView = new MyGridView(mActivity);
		gridView.setFocusable(false);
		gridView.setVerticalSpacing(20);
		AbsListView.LayoutParams gridParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
		gridView.setLayoutParams(gridParams);
		gridView.setNumColumns(3);
		gridView.setColumnWidth((int) (screenWidth / 3));
		gridView.setGravity(Gravity.CENTER);
		gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		gridView.setPadding(0, 30, 0, 5);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

		mGuessYouLikeAdapter = new BookStoreIndexGridViewAdapter(mActivity, mRecomentList);
		gridView.setAdapter(mGuessYouLikeAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent2 = new Intent(mActivity, BookInfoNewUIActivity.class);
				intent2.putExtra("bookid", mRecomentList.get(position).ebookId);
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mActivity.startActivity(intent2);
			}
		});
		recommentContainer.addView(gridView);
		mainLayout.addView(recommentContainer);

		// 请求猜你喜欢数据
		fetchRecommendData();
	}

	private void initView(View view) {
		mEmptyLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
		// 初始化滚动布局
		mXScrollView = (XScrollView) view.findViewById(R.id.mXScrollView);
		mXScrollView.setOnScrllListener(new onScrllListener() {
			
			/**
			 * 滚屏事件
			 */
			@Override
			public void onScrll(int scrollY) {
				//滚动距离若大于一定的高度，则显示返回“返回顶部”按钮
				if (scrollY >= 1000) {
					//回到顶部按钮状态
					if (mTopFab.getVisibility() == View.GONE && !isTopFabClicked) {
						//渐渐显示
						mTopFab.setVisibility(View.VISIBLE);
						// 第一个参数为 view对象，第二个参数为 动画改变的类型，第三，第四个参数依次是开始透明度和结束透明度。
						ObjectAnimator alpha = ObjectAnimator.ofFloat(mTopFab, "alpha", 0.0f, 1.0f);
						alpha.setDuration(500);// 设置动画时间
						alpha.start();// 启动动画。
					}
				} else {
					//隐藏返回顶部按钮
					isTopFabClicked = false;
					if (mTopFab.getVisibility() == View.VISIBLE && isNeedToHide) {
						isNeedToHide = false;
						ObjectAnimator anim = ObjectAnimator.ofFloat(mTopFab, "alpha", 1.0f, 0.0f);
						anim.setDuration(500);
						anim.addListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								mTopFab.setVisibility(View.GONE);
								isNeedToHide = true;
							}
						});
						anim.start();
					}
				}
			}
		});
		
		mXRefreshView = (XRefreshView) view.findViewById(R.id.mXRefreshView);
		mainLayout = (LinearLayout) view.findViewById(R.id.mainLayout);
		//默认不自动刷新
		mXRefreshView.setAutoRefresh(false);
		//允许下拉刷新
		mXRefreshView.setPullLoadEnable(true);
		mXRefreshView.setPinnedTime(400);
		mXRefreshView.setXRefreshViewListener(new XRefreshViewListener() {
			
			/**
			 * 更新数据
			 */
			@Override
			public void onRefresh() {
				refreshNewData();
			}
			
			@Override
			public void onLoadMore(boolean isSlience) {
				if(recommentContainer.getVisibility() == View.GONE) {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							mXRefreshView.stopLoadMore();
							recommentContainer.setVisibility(View.VISIBLE);
						}
					}, 1000);
				}else {
					fetchRecommendData();
				}
			}
			
			@Override
			public void onRelease(float direction) {}
			
			@Override
			public void onHeaderMove(double offset, int offsetY) {}
		});
		
		// 回到顶部按钮
		mTopFab = (FloatingActionButton) view.findViewById(R.id.slide_to_top_btn);
		mTopFab.setColor(mActivity.getResources().getColor(R.color.white));
		mTopFab.setOnClickListener(new OnClickListener() {

			/**
			 * 回到顶部按钮点击处理
			 */
			@Override
			public void onClick(View v) {
				mXScrollView.post(new Runnable() {
					@Override
					public void run() {
						//点击回到顶部按钮，回到顶部
						mXScrollView.fullScroll(ScrollView.FOCUS_UP);
					}
				});
				mTopFab.setVisibility(View.GONE);
				isTopFabClicked = true;
			}
		});
	}

	/**
	 * 加载书城数据
	 */
	@Override
	protected void lazyLoad() {
		if (!isVisible || !isPrepared || mHasLoadedOnce) {
			return;
		}
		initPageData();
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
			// 结束下拉刷新
			mXRefreshView.stopRefresh();
			return;
		}
		
		currentPage = 1;
		flag = 0;
		//模块数据
		mInitModuleCount = 0;
		for (int i = 0; i < mainThemeList.modules.size(); i++) {
			
			final Modules modules = mainThemeList.modules.get(i);
			//单例实例
			BookStoreDataHelper bookStoreDataHelper = BookStoreDataHelper.getInstance();
			//重新取每个模块数据
			bookStoreDataHelper.requestModule(mActivity, modules, new GetCacheDataListener() {

				@Override
				public void onSuccess(Serializable seri) {

				}

				@Override
				public void onFail() {
					mRefresh.onRefreshFail();
					mXRefreshView.stopRefresh();
				}

				@Override
				public void onSuccess(Map<String, Serializable> moduleMap) {
					//停止刷新
					mXRefreshView.stopRefresh();
					if(null == moduleEntityList) {
						return;
					}
					
					if(null == mainThemeList) {
						return;
					}
					
					if(null == mainThemeList.modules) {
						return;
					}
					
					//进行同步，避免重新多个模块
					synchronized(TAG){
						//取到的数据清空
						if (mInitModuleCount == 0) {
							moduleEntityList.clear();
						}
						
						moduleEntityList.add(moduleMap);
						mInitModuleCount++;
					}
					
					//加载数据完毕
					if (mInitModuleCount == mainThemeList.modules.size()) {
						if(null != mRecomentList) {
							//推荐数据
							mRecomentList.clear();
						}
						if(null != mGuessYouLikeAdapter) {
							//猜你喜欢
							mGuessYouLikeAdapter.notifyDataSetChanged();
						}
						
						//清空界面View数据
						if(null != mainLayout) {
							//刷新数据
							mainLayout.removeAllViews();	
						}
						if(null != mRefresh) {
							mRefresh.onRefreshFinish();
						}
						mHasLoadedOnce = true;
					}
				}
			});
		}
	}

	/**
	 * 
	 * @Title: 获取书城数据，并展示
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年5月20日 下午1:23:16
	 */
	private void initPageData() {

		Bundle arguments = getArguments();
		mCurrentPage = arguments.getInt("page_index");
		//数据通过上一次的Fragement传递过来，非本类里边请求
		mainThemeList = (MainThemeList) arguments.getSerializable("data");
		if (mainThemeList == null) {
			return;
		}
		// 加载中
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		BookStoreDataHelper bookStoreDataHelper = BookStoreDataHelper.getInstance();

		for (int i = 0; i < mainThemeList.modules.size(); i++) {
			final Modules modules = mainThemeList.modules.get(i);
			//请求各个模块的数据
			bookStoreDataHelper.getModuleData(mActivity, modules, new GetCacheDataListener() {

				@Override
				public void onSuccess(Serializable seri) {
				}

				@Override
				public void onFail() {
					mRefresh.onRefreshFail();
				}

				/**
				 * 请求模块数据成功
				 */
				@Override
				public void onSuccess(Map<String, Serializable> moduleMap) {
					if (mInitModuleCount == 0) {
						moduleEntityList.clear();
					}
					moduleEntityList.add(moduleMap);
					mInitModuleCount++;
					if (mInitModuleCount == mainThemeList.modules.size()) {
						mHasLoadedOnce = true;
						setupUI();
					}
				}
			});
		}
	}

	private int flag = 0;

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
	private void initView(String cacheKey, int modType, final BookStoreModuleBookListEntity entity) throws Exception {
		if (mActivity == null) {
			MZLog.d("wangguodong", "mActivity is " + mActivity);
		}
		if (entity == null) {
			MZLog.d("wangguodong", "entity is " + entity);
			return;
		}
		flag++;
		View view = null;
		switch (modType) {
			//顶部圆圈
			case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_1:
			case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_2:
			case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_3:
			case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_4:
				view = BookStoreCacheManager.getInstance().getView(cacheKey);
				// 书城顶部专题部分view
				if (view == null) {//缓存不存在
					view = TopicsViewStyleController.getTopicsStyleView(mActivity/*Context*/, 1/*一行*/, 4/*4列*/, entity.moduleLinkChildList/*数据*/, null);
				}
				break;
			// 限时特价
			case BookStoreStyleContrller.TYPE_SPECIAL_PRICE:
				long currentTime = entity.currentTime;
				long endTime = entity.endTime;
				long leftTime = endTime - currentTime;
				if (leftTime > 0) {
					int specialPriceTime = (int)leftTime/1000;
					SettingUtils.getInstance().putBoolean("getSpecialPriceTime", true);
					SettingUtils.getInstance().putLong("SystemCurrentTimeMillis", System.currentTimeMillis());
					SettingUtils.getInstance().putLong("SpecialPriceTime", specialPriceTime);
					SettingUtils.getInstance().putLong("SpecialPriceCountdown", specialPriceTime);
					if(!TextUtils.isEmpty(entity.moduleBookChild.showInfo) && entity.moduleBookChild.showInfo.contains("countdown")){
						showSpecialPriceCountDown = true;
						startSpecialPriceTimer();
					}else{
						showSpecialPriceCountDown = false;
						closeSpecialPriceTimer();
					}
					
					view = BookStoreCacheManager.getInstance().getView(cacheKey);
					if (view == null)
						view = getSpecialPriceView(mActivity, entity.moduleBookChild.showName,
								"更多", 1, 3, leftTime, entity, new OnHeaderActionClickListener() {
	
									/**
									 * 特价更多被点击
									 */
									@Override
									public void onHeaderActionClick() {
										Intent intent = new Intent(mActivity, BookStoreBookListActivity.class);
										intent.putExtra("fid", entity.moduleBookChild.id);
										intent.putExtra("ftype", 2);
										intent.putExtra("relationType", 1);
										intent.putExtra("showName", entity.moduleBookChild.showName);
										intent.putExtra("type", "specialPrice");
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										startActivity(intent);
									}
								});
				}
				break;
			//书籍列表
			case BookStoreStyleContrller.TYPE_BOOK_LIST: {
	
				int childType = BookStoreStyleContrller.TYPE_BOOK_LIST * 10 + entity.moduleBookChild.showType;
				int row = 0;
	
				if (childType == BookStoreStyleContrller.TYPE_BOOK_LIST_GRID) {
	
					if (entity.resultList != null)
						row = (int) Math.floor(entity.resultList.size() / 3.0);
					view = BookStoreCacheManager.getInstance().getView(cacheKey);
					if (view == null) {
						view = BooksViewStyleController.getBooksStyleView(mActivity, entity.moduleBookChild.showName,entity.moduleBookChild.showInfo, "更多", row, 3, entity.resultList,
								new OnHeaderActionClickListener() {
	
									@Override
									public void onHeaderActionClick() {
										Intent intent = new Intent(mActivity, BookStoreBookListActivity.class);
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
	
					if (flag == moduleEntityList.size()) {
					} else {
					}
				} else if (childType == BookStoreStyleContrller.TYPE_BOOK_LIST_VERTICAL) {
	
					if (entity.resultList != null)
						view = BookStoreCacheManager.getInstance().getView(cacheKey);
					row = (int) Math.floor(entity.resultList.size() / 1.0);
					if (view == null)
						view = RankingListViewStyleController.getRankingListStyleView(mActivity, entity.moduleBookChild.showName, "更多", row, 1, entity.resultList,
								new OnHeaderActionClickListener() {
	
									@Override
									public void onHeaderActionClick() {
	
										Intent intent = new Intent(mActivity, BookStoreBookListActivity.class);
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
				if (mActivity == null) {
					return;
				}
				List<BookStoreModuleBookListEntity.ModuleBookChild> childs = new ArrayList<BookStoreModuleBookListEntity.ModuleBookChild>();
				childs.add(entity.moduleBookChild);
				view = BookStoreCacheManager.getInstance().getView(cacheKey);
				if (view == null)
					view = SpecialViewStyleController.getSpecialStyleView(mActivity, childs);
	
				break;
	
			case BookStoreStyleContrller.TYPE_BOOK_BANNER:
				BannerViewStyleController bannerViewController = BannerViewStyleController.getInstance();
				BookStoreRootFragment rootFragment = (BookStoreRootFragment) this.getParentFragment();
				if (rootFragment != null) {
					rootFragment.setBannerAttachListener(bannerViewController);
				}
				MZLog.d(TAG, "banner entity:" + GsonUtils.toJson(entity));
				view = bannerViewController.getBannerView(mActivity, entity.moduleLinkChildList);
				break;
	
			case BookStoreStyleContrller.TYPE_BOOK_CATEGORY:
				break;
		}
		
		if (null != view) {
			mainLayout.addView(view);
		}
	}

	public View getLineView() {
		View view = LayoutInflater.from(mActivity).inflate(R.layout.lineview, null);
		return view;
	}

	public View getDividerView(int height) {
		View view = LayoutInflater.from(mActivity).inflate(R.layout.bookstore_divider, null);
		if (height != 8) {
			View divider = view.findViewById(R.id.divider);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.height = ScreenUtils.dip2px(mActivity, height);
			divider.setLayoutParams(params);
		}
		return view;
	}

	/**
	 * 限时特价
	 * @param context 
	 * @param titleStr
	 * @param actionStr
	 * @param row
	 * @param column
	 * @param leftTime
	 * @param entity
	 * @param listener
	 * @return
	 */
	public LinearLayout getSpecialPriceView(final Context context, String titleStr, String actionStr, int row, int column,long leftTime, 
			final BookStoreModuleBookListEntity entity ,final OnHeaderActionClickListener listener) {
		final List<StoreBook> bookList =entity.resultList;
		
		ImageLoadingListenerImpl listenerImpl = new ImageLoadingListenerImpl();
		if (bookList == null || bookList.size() == 0 || context == null)
			return null;

		final int showSize= bookList.size();
		float screenWidth = ScreenUtils.getWidthJust(context);// px
		int left_margin = ScreenUtils.dip2px(context, DEFAULT_LEFT_MARGIN);
		int right_margin = ScreenUtils.dip2px(context, DEFAULT_RIGHT_MARGIN);
		int top_margin = ScreenUtils.dip2px(context, DEFAULT_TOP_MARGIN);
		int bottom_margin = ScreenUtils.dip2px(context, DEFAULT_BOTTOM_MARGIN);
		
		int bookLeftmargin = ScreenUtils.dip2px(context, DEFAULT_BOOK_LEFT_MARGIN);
		int bookRightmargin = ScreenUtils.dip2px(context, DEFAULT_BOOK_RIGHT_MARGIN);

		int firstRowTopMargin = ScreenUtils.dip2px(context, DEFAULT_FIRST_ROW_TOP_MARGIN);
		int vertical_divider_width = ScreenUtils.dip2px(context, DEFAULT_VERTICAL_DIVIDER_WIDTH);
		int horizontal_divider_width = ScreenUtils.dip2px(context, DEFAULT_HORIZONTAL_DIVIDER_WIDTH);

		int imageWidth = (int) ((screenWidth - (bookLeftmargin + bookRightmargin + (column - 1) * horizontal_divider_width)) / column);
		int imageHeight = 4 * imageWidth / 3;
		
		//垂直布局
		LinearLayout verticalLayout = new LinearLayout(context);
		verticalLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = left_margin;
		params.rightMargin = right_margin;
		params.topMargin = top_margin;
		params.bottomMargin = bottom_margin;

		LayoutInflater li = LayoutInflater.from(context);
		//布局参数
		verticalLayout.setLayoutParams(params);
		//背景色
		verticalLayout.setBackgroundColor(context.getResources().getColor(R.color.bg_main));

		if (bookList != null) {
			//标题
			LinearLayout headerItem = (LinearLayout) li.inflate(R.layout.bookstore_style_special_price_header, null);
			TextView title = (TextView) headerItem.findViewById(R.id.title);
			LinearLayout action = (LinearLayout) headerItem.findViewById(R.id.action);
			TextView actionTv = (TextView) action.findViewById(R.id.action_info);
			lefeTimeHourTv = (TextView) headerItem.findViewById(R.id.left_time_hour);
			lefeTimeMinuteTv = (TextView) headerItem.findViewById(R.id.left_time_minute);
			lefeTimeSecondTv = (TextView) headerItem.findViewById(R.id.left_time_second);
			leftTimeLayout = (LinearLayout) headerItem.findViewById(R.id.left_time_layout);
			endLayout = (LinearLayout) headerItem.findViewById(R.id.end_layout);
			
			leftTimeLayout.setVisibility(View.GONE);
			endLayout.setVisibility(View.GONE);
			if (bookList.size() >= row) {
				action.setVisibility(View.VISIBLE);
				actionTv.setText("更多");
			} else {
				action.setVisibility(View.GONE);
			}
			action.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (listener != null) {
						listener.onHeaderActionClick();
					}

				}
			});
			
			//隐藏底部的线
			View bottomLine = headerItem.findViewById(R.id.bottomLine);
			bottomLine.setVisibility(View.GONE);
			title.setText(titleStr);
			verticalLayout.addView(headerItem);
		}
		
		LayoutParams horizontalParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		horizontalParams.gravity = Gravity.CENTER;
		horizontalParams.bottomMargin = vertical_divider_width;
		horizontalParams.leftMargin = bookLeftmargin;
		horizontalParams.rightMargin = bookRightmargin;

		LayoutParams bookParams = new LinearLayout.LayoutParams(imageWidth, LayoutParams.WRAP_CONTENT);

		int size = bookList.size();
		int tempdata = (int) Math.ceil(size / (column * 1.0f));
		for (int k = 0; k < row; k++) {

			boolean isNeedAddRow = k < tempdata ? true : false;
			if (isNeedAddRow) {
				LinearLayout horizontaLayout = new LinearLayout(context);
				horizontaLayout.setOrientation(LinearLayout.HORIZONTAL);

				if (k == 0)
					horizontalParams.topMargin = firstRowTopMargin;
				horizontaLayout.setLayoutParams(horizontalParams);

				for (int i = 0; i < showSize; i++) {

					final int position = k * column + i;
					if (position <= bookList.size() - 1) {
						LinearLayout bookItem = (LinearLayout) li.inflate(R.layout.bookstore_style_book_special_price_item, null);

						bookParams.rightMargin = horizontal_divider_width;
						bookItem.setLayoutParams(bookParams);
						
						//大于三本时点击书籍进入列表页
						if(showSize>3){
							bookItem.setOnClickListener(new OnClickListener() {

								/**
								 * 条目被点击，进入特价列表页
								 */
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(context, BookStoreBookListActivity.class);
									intent.putExtra("fid", entity.moduleBookChild.id);
									intent.putExtra("ftype", 2);
									intent.putExtra("relationType", 1);
									intent.putExtra("showName", entity.moduleBookChild.showName);
									intent.putExtra("from", "special_price");
									intent.putExtra("position", position);
									intent.putExtra("type", "specialPrice");
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(intent);
								}
							});
						}
						//小于三本时点击书籍进入书籍详情页
						else{
							bookItem.setOnClickListener(new OnClickListener() {
								/**
								 * 条目被点击，进入图书详情页
								 */
								@Override
								public void onClick(View v) {
									Intent intent2 = new Intent(context, BookInfoNewUIActivity.class);
									intent2.putExtra("bookid", bookList.get(position).ebookId);
									intent2.putExtra("type", "specialPrice");
									intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(intent2);
								}
							});
						}

						ImageView bookCover = (ImageView) bookItem.findViewById(R.id.book_cover);
						TextView bookName = (TextView) bookItem.findViewById(R.id.book_name);
						TextView SpecialPrice = (TextView) bookItem.findViewById(R.id.special_price);
						TextView OriginalPrice = (TextView) bookItem.findViewById(R.id.original_price);

						LayoutParams coverParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);
						coverParams.gravity = Gravity.CENTER;
						bookCover.setLayoutParams(coverParams);
						ImageLoader.getInstance().displayImage(bookList.get(position).imageUrl, bookCover, GlobalVarable.getCutBookDisplayOptions(false),
								listenerImpl);

						bookName.setText(bookList.get(position).name + "\n");

						TextPaint tpaint = bookName.getPaint();
						tpaint.setFakeBoldText(true);
						SpecialPrice.setText("￥"+bookList.get(position).jdPrice);
						OriginalPrice.setText("￥"+bookList.get(position).price);
						OriginalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //删除线
						horizontaLayout.addView(bookItem);
					}
				}
				//展示书本大于3本时可以滚动浏览
				if(showSize>3){
					HorizontalScrollView scrollView = new HorizontalScrollView(context);
					LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					scrollView.setLayoutParams(params1);
					scrollView.setHorizontalScrollBarEnabled(false);
					scrollView.addView(horizontaLayout);
					verticalLayout.addView(scrollView);
				}
				else
					verticalLayout.addView(horizontaLayout);
			}
		}
		return verticalLayout;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		SettingUtils.getInstance().putBoolean("getSpecialPriceTime", false);
		closeSpecialPriceTimer();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(BooksViewStyleController.myBroadCastReceiver!=null){
			mActivity.unregisterReceiver(BooksViewStyleController.myBroadCastReceiver);
		}
	}

}
