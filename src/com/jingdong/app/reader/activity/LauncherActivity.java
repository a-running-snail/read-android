package com.jingdong.app.reader.activity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.bookshelf.BookcaseLocalFragmentNewUI;
import com.jingdong.app.reader.bookstore.fragment.BookStoreRootFragment;
import com.jingdong.app.reader.client.ServiceProtocol;
import com.jingdong.app.reader.common.CommonFragmentActivity;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.BaseEvent;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.LoginEvent;
import com.jingdong.app.reader.entity.SignEvent;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.eventbus.de.greenrobot.event.EventBus;
import com.jingdong.app.reader.eventbus.event.MessageEvent;
import com.jingdong.app.reader.extension.giftbag.GiftBagUiReciver;
import com.jingdong.app.reader.extension.giftbag.GiftBagUtil;
import com.jingdong.app.reader.extension.giftbag.NewMessageRecivier;
import com.jingdong.app.reader.extension.integration.FloatingActionButton;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.CheckSignScoreListener;
import com.jingdong.app.reader.extension.jpush.JDPush;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.localreading.BookProperties;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.parser.BaseParserCreator;
import com.jingdong.app.reader.parser.json.TimelineJSONParser;
import com.jingdong.app.reader.parser.url.TimelineURLParser;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.JSONArrayPoxy;
import com.jingdong.app.reader.util.JSONObjectProxy;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.util.UpdateUtil;
import com.jingdong.app.reader.util.UpdateUtil.CheckNewVersionListener;
import com.jingdong.app.reader.util.UserGuiderUtil;
import com.jingdong.app.reader.util.UserGuiderUtil.GuiderCoverClickListener;
import com.jingdong.app.reader.view.BadgeView;
import com.jingdong.app.reader.view.dialog.DialogManager;

/**
 * 主框架Activity<br />
 * 1、初始化Tab<br />
 * 2、初始化Tab指向内容（Fragement）<br />
 * 3、初始化选中Tab的内容
 * 		1）初始化顶部标题栏
 * 		2）初始化中间内容区
 */
public class LauncherActivity extends CommonFragmentActivity {
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private GiftBagUiReciver giftBagUifinsh = null;
	private LinearLayout bottomMenuContent = null;
	public final static int TAB_BOOKSHELF = 1;
	public final static int TAB_BOOKSHOP = 0;
	public final static int TAB_MY_CENTER = 3;
	private static final int LOGIN_REQUEST_CODE = 1000;
	public String[] tabNames = null;
	public int[] tabImgs = null;
	public View[] menus = null;
	private int backPressTimes = 0;

	/**
	 * 底部Tab索引
	 */
	private int currentIndex = 1;//默认展示书架
	private int perMenuWidth = 0;
	private TabChangeListener mTabChangeListener;
	private boolean firstflag = true;
	private NotificationService notificationService;
	public static Notification notification = Notification.getInstance();
	private int flag = -1;

	public static final String REFRESH_FRAGMENT_CONTENT = "refresh_fragment_content";
	private static final String BOOK_STORE_TAG = "BookStore";
	private static final String BOOK_SHELF_LIST_TAG = "BookShelfList";
	private static final String BOOK_SHELF_GRID_TAG = "BookShelfGrid";
	private static final String BOOK_COMMUNITY_TAG = "BookCommunity";
	private static final String BOOK_ME_TAG = "BookMe";
	
	private Context context;
	
	private NewMessageRecivier newMessageReciver = null;
	private MZBookApplication app;
	private UserGuiderUtil userGuiderUtil = null;

	boolean isnotShow =true;
	private BookStoreRootFragment mBookStoreFragment = null;
	private BookcaseLocalFragmentListView mBookCaseNameListFragment = null;
	private BookcaseLocalFragmentListView mBookCaseTimeListFragment = null;
	private BookcaseLocalFragmentNewUI mBookCaseGirdFragment = null;
	private MeRootFragment mPersonalFragment = null;
	private CommunityFragment communityFragment = null;
	private int mBookStoreIndex;
	private int mSubPage;

	private BroadcastReceiver notificationReceiver = getBroadcastReceiver();

	@Override
	protected void onCreate(Bundle arg0) {
		context=this;
		MZLog.d("life-cycle", "Launcher onCreate");
		// 修复书籍打开返回界面显示问题 开始
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		// 修复书籍打开返回界面显示问题 结束
		
		//状态栏高度
		int statusBarHeight = getStatusBarHeight();
		super.onCreate(arg0);
		app = (MZBookApplication) getApplication();
		
		//渠道礼包广播接收
		giftBagUifinsh = new GiftBagUiReciver();
		giftBagUifinsh.setLauncherActivity(this);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(GiftBagUiReciver.ACTION);
		//注册渠道礼包广播接收
		app.regeisteLocalBroadcastRecivier(giftBagUifinsh, intentFilter);
		
		newMessageReciver = new NewMessageRecivier();
		newMessageReciver.setLauncherActivity(this);

		IntentFilter intentFiler = new IntentFilter();
		intentFiler.addAction(NewMessageRecivier.ACTION_ADD);
		intentFiler.addAction(NewMessageRecivier.ACTION_REDUCE);

		app.regeisteLocalBroadcastRecivier(newMessageReciver, intentFiler);
		// 修复书籍打开返回界面显示问题 开始
		// 检查版本更新
		UpdateUtil.checksofteWareUpdated(false, LauncherActivity.this, new CheckNewVersionListener() {
			@Override
			public void onCheckResult(boolean isNewVersionAvaiable) {
				if (!isNewVersionAvaiable) {
					 //不存在新版本
					GiftBagUtil.getInstance().reqIsHaveGiftBag(getBaseContext());
				}
			}
		});
		
		View view = LayoutInflater.from(LauncherActivity.this).inflate(R.layout.navigation_tab_layout, null);
		FrameLayout content = (FrameLayout) view.findViewById(R.id.tabcontent);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
		params.topMargin = statusBarHeight;
		params.weight = 1;
		content.setLayoutParams(params);
		// 修复书籍打开返回界面显示问题 结束
		setContentView(view);
		//底部标签栏
		//从XML中读取数组
		tabNames = getResources().getStringArray(R.array.navigation_tab_menu);
		tabImgs = new int[] { 
				R.drawable.bottom_tab_bookstore_selector, //书城
				R.drawable.bottom_tab_bookshelf_selector, //书架
				R.drawable.bottom_tab_community_selector,//社区
				R.drawable.bottom_tab_me_selector //我的
				};
		fragmentManager = getSupportFragmentManager();
		bottomMenuContent = (LinearLayout) findViewById(R.id.bottom_menu);
		//计算每个标签的宽度
		perMenuWidth = (int) (ScreenUtils.getWidthJust(LauncherActivity.this) / tabNames.length);
		//初始化京东阅读底部标签栏（含初始化默认标签所指向的内容）
		setupUiOfTabsAndContentOfSelectedTab();
		// 注册事件总线
		EventBus.getDefault().register(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		prepareOnFirstLaunch();
		MZLog.d("life-cycle", "Launcher onStart");
	}
	
	@Override
	protected void onResume() {
		MZLog.d("life-cycle", "Launcher onResume");
		super.onResume();
	}
	
	/**
	 * 初始化京东阅读底部标签栏（含初始化默认标签所指向的内容）
	 */
	private void setupUiOfTabsAndContentOfSelectedTab() {
		menus = new View[tabNames.length];
		for (int i = 0; i < tabNames.length; i++) {
			//文本和图片
			View menu = viewGenerator(tabNames[i], tabImgs[i], "");
			menus[i] = menu;
			final int position = i;
			menu.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (currentIndex == position)
						return;
					if (position == TAB_MY_CENTER) /*我的*/{
						Log.d("quda", "登入状态：" + LoginUser.isLogin());
						if (LoginUser.isLogin()) {
							//展示我的 Tab内容
							showMainLayoutView(position);
						} else {
							//启动登录
							Intent login = new Intent(LauncherActivity.this, LoginActivity.class);
							startActivity(login);
						}
					} else {
						//展示Tab内容
						showMainLayoutView(position);
					}
				}
			});
			//标签加入
			bottomMenuContent.addView(menu);
		}

		int intentIndex = getIntent().getIntExtra("TAB_INDEX", -1);
		if (intentIndex != -1) {// 外部任务处理 其他界面不会处理这个
			if (intentIndex == TAB_MY_CENTER) {
				if (LoginUser.isLogin()) {
					currentIndex = intentIndex;
					//展示Tab内容
					showMainLayoutView(intentIndex);
					//设置别名
					JDPush.setAliasAndTags(this, LoginUser.getpin(), LoginUser.getpin());
				} else {
					Intent login = new Intent(LauncherActivity.this, LoginActivity.class);
					startActivity(login);
				}
			} else {
				currentIndex = intentIndex;
				//展示Tab内容
				showMainLayoutView(intentIndex);
			}
		} else {
			//处理推送消息
			handlePushNotice(getIntent());
		}
		//初始化底部标签状态
		initBottomTabState();
	}

	
	
	public void onEventMainThread(BaseEvent baseEvent) {
		MZLog.d("J", "---onEventMainThread---");
		if (baseEvent instanceof LoginEvent) {
			String url = "http://e.m.jd.com/lottery.html?source=JdReader";
			Intent intent = new Intent(this,WebViewActivity.class);
			intent.putExtra(WebViewActivity.BrowserKey, false);
			intent.putExtra(WebViewActivity.TitleKey, "一元购抽奖");
			intent.putExtra(WebViewActivity.UrlKey, url);
			this.startActivity(intent);
		}else if (baseEvent instanceof SignEvent) {
			BookcaseLocalFragmentNewUI shelfGrid = (BookcaseLocalFragmentNewUI) getSupportFragmentManager().findFragmentByTag(BOOK_SHELF_GRID_TAG);
			if (shelfGrid != null && shelfGrid.isVisible()) {
				shelfGrid.processSign();
				return;
			}
			BookcaseLocalFragmentListView shelfList =(BookcaseLocalFragmentListView) getSupportFragmentManager().findFragmentByTag(BOOK_SHELF_LIST_TAG);
			if (shelfList != null && shelfList.isVisible()) {
				shelfList.processSign();
			}
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		final Context context=this;
		if (currentIndex == 1 && !LocalUserSetting.isBookShelfGuidShow(this)) {
			isnotShow=false;
			ImageView imageView = new ImageView(this);
			imageView.setImageResource(R.drawable.bookshelf_guide);
			userGuiderUtil = new UserGuiderUtil(this, imageView, true,false, false, true, new GuiderCoverClickListener() {

				@Override
				public void onClick(View view) {
					
					ImageView addimageView = new ImageView(LauncherActivity.this);
					addimageView.setImageResource(R.drawable.bookshelf_guide_add);
					userGuiderUtil = new UserGuiderUtil(LauncherActivity.this, addimageView, false,false, false, true, new GuiderCoverClickListener() {

						@Override
						public void onClick(View view) {
							if (!LoginUser.isLogin() && currentIndex == 1) {
//								IntegrationMgr.firstDownloadToGrand(LauncherActivity.this);
								
								DialogManager.showCommonDialog(context, "", context.getString(R.string.download_jdreader_to_grand_integration),
										"注册登录", "以后再说", new android.content.DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
										case DialogInterface.BUTTON_POSITIVE:
											Intent login = new Intent(context,LoginActivity.class);
											context.startActivity(login);
											break;
										case DialogInterface.BUTTON_NEGATIVE:
											break;
										}
										isnotShow=true;
										dialog.dismiss();
									}
								});
							}
						}
					});
				}
			});
			LocalUserSetting.saveBookShelfGuidShow(this);
		}

		if (currentIndex == 0 && !LocalUserSetting.isBookStoreGuidShow(this)) {
			ImageView imageView = new ImageView(this);
			imageView.setImageResource(R.drawable.recommend_guide);
			userGuiderUtil = new UserGuiderUtil(getParent(), imageView, false,false, false, true, null);
			LocalUserSetting.saveBookStoreGuidShow(this);
		}
		if(isnotShow && currentIndex == 1 && LocalUserSetting.isBookShelfGuidShow(this)){
			isnotShow=false;
			sendBookEvent();
		}
		super.onWindowFocusChanged(hasFocus);
	}

	/**
	 * 移除新手引导相关界面
	 */
	public void removeUserGuider() {
		if (userGuiderUtil != null) {
			userGuiderUtil.removeView();
		}
	}

	public View viewGenerator(String title, int icon, String msg) {

		LayoutInflater inflater = LayoutInflater.from(LauncherActivity.this);
		View view = inflater.inflate(R.layout.navigation_tab_menu, null);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		params.width = perMenuWidth;
		view.setLayoutParams(params);

		ImageView iconImage = (ImageView) view.findViewById(R.id.navigation_menu_icon);
		TextView titleView = (TextView) view.findViewById(R.id.navigation_menu_title);
		TextView msgView = (TextView) view.findViewById(R.id.navigation_menu_msg);
		iconImage.setImageResource(icon);
		iconImage.setScaleType(ScaleType.FIT_CENTER);

		if(MZBookApplication.isPad()) {
			titleView.setTextSize(13);
		}
		titleView.setText(title);
		msgView.setText(msg);
		return view;
	}

	

	/**
	 * 
	 * @Title: handlePushNotice
	 * @Description: 处理推送消息
	 * @param
	 * @return void
	 * @throws
	 */
	private void handlePushNotice(Intent outIntent) {
		if (outIntent != null) {
			currentIndex = outIntent.getIntExtra("lx", 1);
			if (currentIndex == 0) {
				int relationType = outIntent.getIntExtra("relationType", -1);
				if (relationType != -1) {
					if (relationType == 1) {
						Intent intent = new Intent(MZBookApplication.getContext(), BookStoreBookListActivity.class);
						intent.putExtra("fid", outIntent.getIntExtra("fid", -1));
						intent.putExtra("ftype", outIntent.getIntExtra("ftype", -1));
						intent.putExtra("list_type", outIntent.getIntExtra("list_type", -1));
						intent.putExtra("relationType", relationType);
						intent.putExtra("lx", currentIndex);
						intent.putExtra("showName", outIntent.getStringExtra("showName"));
						intent.putExtra("bannerImg", outIntent.getStringExtra("bannerimg"));
						intent.putExtra("boolNew", outIntent.getBooleanExtra("boolNew", false));
						startActivity(intent);
					} else {
						if (outIntent.getStringExtra(WebViewActivity.UrlKey) != null) {
							String url = outIntent.getStringExtra(WebViewActivity.UrlKey);
							Intent intent = new Intent(this, WebViewActivity.class);
							intent.putExtra(WebViewActivity.UrlKey, url);
							intent.putExtra(WebViewActivity.TitleKey, outIntent.getStringExtra("showName"));
							intent.putExtra(WebViewActivity.TopbarKey, false);
							intent.putExtra("lx", currentIndex);
							startActivity(intent);
						}
					}
				}
			}
		} else {
			currentIndex = 1;
		}
		mSubPage = outIntent.getIntExtra("SUB_PAGE", 0);
		//展示View
		showMainLayoutView(currentIndex);
		//设置别名
		JDPush.setAliasAndTags(this, LoginUser.getpin(), LoginUser.getpin());
	}

	


	/**
	 * 本Activity为singleTask，需要处理onNewIntent
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		int intentIndex = intent.getIntExtra("TAB_INDEX", -1);
		mBookStoreIndex = intent.getIntExtra("BOOK_STORE_INDEX", 0);
		if (intentIndex != -1) {// 外部任务处理 其他界面不会处理这个
			if (intentIndex == TAB_MY_CENTER) {
				if (LoginUser.isLogin()) {
					currentIndex = intentIndex;
					showMainLayoutView(intentIndex);
					JDPush.setAliasAndTags(this, LoginUser.getpin(), LoginUser.getpin());
				} else {
					Intent login = new Intent(LauncherActivity.this, LoginActivity.class);
					startActivity(login);
				}
			} else {
				currentIndex = intentIndex;
				showMainLayoutView(intentIndex);
			}
		} else {
			handlePushNotice(intent);
		}
	}

	/**
	 * 
	 * @Title: handleFloatingButtonBState
	 * @Description: 处理签到悬浮按钮的显示状态
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年5月28日 下午2:01:45
	 */
	public void handleFloatingButtonState(final FloatingActionButton fab) {
		if (LoginUser.isLogin()) {
			IntegrationAPI.checkSignScore(this, new CheckSignScoreListener() {

				@Override
				public void onCheck(boolean isSigned) {
					fab.setVisibility(!isSigned ? View.VISIBLE : View.GONE);
					fab.hide(isSigned);
				}
			});
		} else {
			// 之前登录成功过，自动登录异步
			if (!TextUtils.isEmpty(LoginUser.getpin())) {
				fab.setVisibility(View.GONE);
			} else {
				fab.setVisibility(View.VISIBLE);
				fab.hide(false);
			}
		}

	}

	/**
	 * 初始化标签状态
	 */
	public void initBottomTabState() {
		if (menus != null) {
			for (int i = 0; i < menus.length; i++) {
				ImageView iconImage = (ImageView) menus[i].findViewById(R.id.navigation_menu_icon);
				TextView titleView = (TextView) menus[i].findViewById(R.id.navigation_menu_title);
				if (currentIndex == i) {
					iconImage.setEnabled(true);
					titleView.setEnabled(true);
				} else {
					iconImage.setEnabled(false);
					titleView.setEnabled(false);
				}
			}
		}
	}

	// 清除tab通知
	public void clearMsg(int index) {
		if (menus != null)
			for (int i = 0; i < menus.length; i++) {
				if (index == i && menus[i].getTag() != null) {
					View msgView = (View) menus[i].findViewById(R.id.vessel);
					if (menus[i].getTag() != null) {
						BadgeView bv = (BadgeView) menus[i].getTag();
						menus[i].setTag(null);
						bv.hide(true);
						menus[i].setTag(null);
					}
				}
			}
	}

	// 添加通知
	public void notifyMessage(int index, String msg) {

		if (menus != null)
			for (int i = 0; i < menus.length; i++) {
				if (index == i && menus[i].getTag() == null) {
					View msgView = (View) menus[i].findViewById(R.id.vessel);
					msgView.measure(0, 0);
					BadgeView bv = new BadgeView(getBaseContext(), msgView);
					// bv.setText("0");
					bv.setTextColor(Color.RED);
					bv.setBackgroundColor(Color.RED);
					bv.setBackgroundResource(R.drawable.jd_notify_red_dot);
					int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
					bv.setWidth(width);
					bv.setHeight(width);
					bv.setBadgePosition(BadgeView.POSITION_TOP_LEFT);
					bv.setBadgeMargin(msgView.getWidth() / 2 + width * 6 / 5, width * 2 / 5);
					bv.show(true);
					menus[i].setTag(bv);
					MZLog.d("J", "notifyMessage,BadgeView::" + bv.toString());
				}
			}

	}

	/**
	 * 隐藏全部Fragements
	 * @param transaction
	 */
	private void hideFragments(FragmentTransaction transaction) {
		if (null != mBookStoreFragment) {
			transaction.hide(mBookStoreFragment);
		}
		if (null != mBookCaseNameListFragment) {
			transaction.hide(mBookCaseNameListFragment);
		}
		if (null != mBookCaseTimeListFragment) {
			transaction.hide(mBookCaseTimeListFragment);
		}
		if (null != mBookCaseGirdFragment) {
			transaction.hide(mBookCaseGirdFragment);
		}
		if (null != mPersonalFragment) {
			transaction.hide(mPersonalFragment);
		}
		if (null != communityFragment) {
			transaction.hide(communityFragment);
		}
	}

	/**
	 * 展示底部标签对应的内容
	 * @param transaction
	 * @param index
	 */
	public void switchContent(FragmentTransaction transaction, int index) {
		switch (index) {
			//书城
			case 0:
				MZLog.d("temp", "111111111");
				if (null != mBookStoreFragment) {
					//显示书城
					BookStoreRootFragment.mCurrentPos = mSubPage;
					transaction.show(mBookStoreFragment);
				} else {
					//初始化书城
					mBookStoreFragment = new BookStoreRootFragment();
					BookStoreRootFragment.mCurrentPos = mSubPage;
					mBookStoreFragment.setRetainInstance(true);
					transaction.add(R.id.tabcontent, mBookStoreFragment,BOOK_STORE_TAG);
					MZLog.d("temp", "11111113333");
				}
	
				if (mTabChangeListener != null) {
					mTabChangeListener.onTabChange(0);
				}
				TalkingDataUtil.onTabEvent(LauncherActivity.this, 0);
				break;
			//书架
			case 1:
				//读取书架显示模式（封面模式，列表模式）
				String model = LocalUserSetting.getBookShelfModel(getApplicationContext());
				if (("FengMian").equals(model)) {//封面模式
					if (null != mBookCaseGirdFragment)
						transaction.show(mBookCaseGirdFragment);
					else {
						mBookCaseGirdFragment = new BookcaseLocalFragmentNewUI();
						transaction.add(R.id.tabcontent, mBookCaseGirdFragment,BOOK_SHELF_GRID_TAG);
					}
				} else if (("Name").equals(model)) {//按照书名排序列表模式
					if (null != mBookCaseNameListFragment)
						transaction.show(mBookCaseNameListFragment);
					else {
						mBookCaseNameListFragment = new BookcaseLocalFragmentListView(0);
						transaction.add(R.id.tabcontent, mBookCaseNameListFragment,BOOK_SHELF_LIST_TAG);
					}
				} else if (("Time").equals(model)) {//按照时间排序，列表模式
					if (null != mBookCaseTimeListFragment)
						transaction.show(mBookCaseTimeListFragment);
					else {
						mBookCaseTimeListFragment = new BookcaseLocalFragmentListView(1);
						transaction.add(R.id.tabcontent, mBookCaseTimeListFragment,BOOK_SHELF_LIST_TAG);
					}
				} else {
					//默认书架，封面模式
					if (null != mBookCaseGirdFragment) {
						transaction.show(mBookCaseGirdFragment);
					} else {
						//初始化书架
						mBookCaseGirdFragment = new BookcaseLocalFragmentNewUI();
						transaction.add(R.id.tabcontent, mBookCaseGirdFragment,BOOK_SHELF_GRID_TAG);
					}
				}
				if (!TextUtils.isEmpty(model))
					LocalUserSetting.saveBookShelfModel(getApplicationContext(), model);
	
				// ##############
				if (mTabChangeListener != null) {
					mTabChangeListener.onTabChange(1);
				}
				TalkingDataUtil.onTabEvent(LauncherActivity.this, 1);
				break;
			//社区
			case 2:
				MZLog.d("temp", "1111111113");
				if (null != communityFragment)
					transaction.show(communityFragment);
				else {
					communityFragment = new CommunityFragment();
					Bundle bundle = new Bundle();
					bundle.putBoolean(TimelineFragment.ENABLE_PULL_REFRESH, true);
					bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
					bundle.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(TimelineURLParser.class, TimelineJSONParser.class));
					communityFragment.setArguments(bundle);
					transaction.add(R.id.tabcontent, communityFragment,BOOK_COMMUNITY_TAG);
				}
				if (mTabChangeListener != null) {
					mTabChangeListener.onTabChange(2);
				}
				TalkingDataUtil.onTabEvent(LauncherActivity.this, 2);
				break;
			//我的
			case 3:
				MZLog.d("temp", "1111111113");
				if (null != mPersonalFragment)
					transaction.show(mPersonalFragment);
				else {
					mPersonalFragment = new MeRootFragment();
					transaction.add(R.id.tabcontent, mPersonalFragment,BOOK_ME_TAG);
					MZLog.d("temp", "11111111132");
				}
				if (mTabChangeListener != null) {
					mTabChangeListener.onTabChange(3);
				}
				TalkingDataUtil.onTabEvent(LauncherActivity.this, 3);
				break;
		}
		transaction.commitAllowingStateLoss();

	}

	/**
	 * 展示内容（底部标签框架对应的内容）
	 * @param index 索引
	 */
	public void showMainLayoutView(int index) {
		currentIndex = index;
		//初始化底部标签状态
		initBottomTabState();
		fragmentTransaction = fragmentManager.beginTransaction();
		hideFragments(fragmentTransaction);
		//展示内容
		switchContent(fragmentTransaction, index);
		backPressTimes = 0;
		if (index == 3) {
			clearMsg(3);
		}
	}

	/**
	 * 点击后退键处理
	 */
	@Override
	public void onBackPressed() {
		if (1 == currentIndex ) {
			if (null != mBookCaseGirdFragment) {
			 	if (!mBookCaseGirdFragment.getIsSelected()) {
			 		exitApp();
			 	} else {
			 		mBookCaseGirdFragment.hideTooBar();	
			 	}
			}
		} else {
			//退出App
			exitApp();
		}
	}
	
	private void exitApp() {
		if (backPressTimes > 0) {
			MZBookApplication.exitApplication();
			super.onBackPressed();
		} else {
			Toast.makeText(this, getString(R.string.back_exit_warning), Toast.LENGTH_SHORT).show();
			++backPressTimes;
		}
	}

	private boolean prepareOnFirstLaunch() {

		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			int currentVersion = info.versionCode;
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int lastVersion = prefs.getInt(BookProperties.KEY_PREPARE_VERSION_COMPLETED, 0);

			if (currentVersion > lastVersion) {
				MZLog.d("wangguodong", "开始拷贝内置的ebook 文件");
				BookProperties.isFirstLuanch = true;
				prefs.edit().putInt(BookProperties.KEY_PREPARE_VERSION_COMPLETED, currentVersion).commit();
				prepare();
				MZLog.d("wangguodong", "拷贝内置的ebook 文件结束");
				return true;
			}
		} catch (PackageManager.NameNotFoundException e) {
			MZLog.d("wangguodong", "拷贝内置的ebook 文件异常终止");
		}

		return false;
	}

	private void prepare() {
		ArrayList<String> assets = new ArrayList<String>();
		ArrayList<Long> bookIds = new ArrayList<Long>();
		ArrayList<String> imgPaths = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> authors = new ArrayList<String>();
		ArrayList<String> opfMd5s = new ArrayList<String>();

		InputStream asset_is = null;
		try {

			asset_is = getResources().getAssets().open("buildin/ebook_builtin");
			String ebookBuiltInString = IOUtil.readAsString(asset_is, ServiceProtocol.sCharset, null);
			try {
				JSONObjectProxy json = new JSONObjectProxy(new JSONObject(ebookBuiltInString));
				JSONArrayPoxy ebook_list = json.getJSONArray("ebook_builtin");
				if (ebook_list != null) {
					for (int index = 0; index < ebook_list.length(); ++index) {
						bookIds.add(ebook_list.getJSONObject(index).getLong("id"));
						assets.add(ebook_list.getJSONObject(index).getString("path"));
						imgPaths.add(ebook_list.getJSONObject(index).getString("bookcover"));
						names.add(ebook_list.getJSONObject(index).getString("bookName"));
						authors.add(ebook_list.getJSONObject(index).getString("bookAuthor"));
						opfMd5s.add(ebook_list.getJSONObject(index).getString("opf_md5"));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (asset_is != null)
				asset_is.close();
			asset_is = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

		for (int i = 0; i < bookIds.size(); i++) {
			Document doc = MZBookDatabase.instance.getDocumentBySource(assets.get(i));
			if (doc != null) {
				continue;
			}
			InputStream is = null;
			try {
				is = getAssets().open("buildin/" + assets.get(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
			InputStream imgIs = null;
			try {
				imgIs = getAssets().open("buildin/" + imgPaths.get(i));
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (is != null && imgIs != null)
				LocalDocument.saveBuiltInDocumemnt(is, imgIs, bookIds.get(i), imgPaths.get(i), names.get(i), authors.get(i), opfMd5s.get(i),
						LocalBook.FORMAT_EPUB, LocalBook.FORMATNAME_EPUB);
		}

		LoginUser.scanDocumentToBookShelf(LauncherActivity.this, LoginUser.getpin());

	}

	public void onEventMainThread(MessageEvent event) {

		int index = event.getRefreshItemIndex();
		MZLog.d("wangguodong", "接收到刷新事件，刷新第" + index + "个界面");
		if (index != -1) {
			switch (index) {
			case 0:
				mBookStoreFragment = null;
				break;
			case 1:
				mBookCaseNameListFragment = null;
				mBookCaseTimeListFragment = null;
				mBookCaseGirdFragment = null;
				break;
			case 2:
				communityFragment = null;
				break;
			case 3:
				mPersonalFragment = null;
				break;

			}

		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		app.unregeisteLoacalBroadcastRecivier(newMessageReciver);
		app.unregeisteLoacalBroadcastRecivier(giftBagUifinsh);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MZLog.d("J", "LauncherActivity#onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();

	}
	
	/**
	 * 好评送书活动
	 */
	private void sendBookEvent(){
		//好评送书活动
		if(LocalUserSetting.isSendBookDialogShow(context)){
			
			WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getIsExistCommentParams(), true,
					new MyAsyncHttpResponseHandler(context) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						}

						@Override
						public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

							String result = new String(responseBody);
							try {
								JSONObject json = new JSONObject(result);

								int code = json.optInt("code");
								if (code == 0) {
									if(isFinishing() || context ==null)
										return ;
									DialogManager.showCommonDialog(context, "提示", "好评送好书，壕礼免费拿！", "查看活动", "关闭",
											new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											switch (which) {
											case DialogInterface.BUTTON_POSITIVE:

												WebRequestHelper.get(URLText.JD_BASE_URL,
														RequestParamsPool.getSendBookLoginParams(), true,
														new MyAsyncHttpResponseHandler(context) {
													@Override
													public void onFailure(int arg0, Header[] arg1, byte[] arg2,
															Throwable arg3) {
													}

													@Override
													public void onResponse(int statusCode, Header[] headers,
															byte[] responseBody) {

														String result = new String(responseBody);
														try {
															JSONObject json = new JSONObject(result);

															String url = json.optString("url");
															String title = json.optString("title");
															if (url != null && !"".equals(url)) {
																url = url + "&operationSource=2";
																Intent intent = new Intent(context,WebViewActivity.class);
																intent.putExtra(WebViewActivity.UrlKey, url);
																intent.putExtra(WebViewActivity.TopbarKey, true);
																intent.putExtra(WebViewActivity.BrowserKey, false);
																intent.putExtra(WebViewActivity.TitleKey, title);
																intent.putExtra(WebViewActivity.TypeKey, WebViewActivity.TYPE_SENDBOOK);
																intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
																context.startActivity(intent);
															}
														} catch (JSONException e) {
															e.printStackTrace();
														}

													}
												});
												break;
											case DialogInterface.BUTTON_NEGATIVE:
												LocalUserSetting.saveSendBookDialogShow(context);
												break;
											default:
												break;
											}
											dialog.dismiss();
										}
									});
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FragmentManager fm = getSupportFragmentManager();
		  int index = requestCode >> 16;
		  if (index != 0) {
		   index--;
		   if (fm.getFragments() == null || index < 0 || index >= fm.getFragments().size()) {
		    return;
		   }
		   Fragment frag = fm.getFragments().get(index);
		   if (frag == null) {
		   } else {
		    handleResult(frag, requestCode, resultCode, data);
		   }
		   return;
		  }
	}
	
	 /**
	  * 递归调用，对所有子Fragement生效
	  * 
	  * @param frag
	  * @param requestCode
	  * @param resultCode
	  * @param data
	  */
	private void handleResult(Fragment frag, int requestCode, int resultCode, Intent data) {
		frag.onActivityResult(requestCode & 0xffff, resultCode, data);
		List<Fragment> frags = frag.getChildFragmentManager().getFragments();
		if (frags != null) {
			for (Fragment f : frags) {
				if (f != null)
					handleResult(f, requestCode, resultCode, data);
			}
		}
	}
	
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(NotificationService.NOTIFICATION_ACTION);
		LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver, filter);
	}

	private void unregisterReceiver() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
	}
	
	private BroadcastReceiver getBroadcastReceiver() {
		return new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(NotificationService.NOTIFICATION_ACTION)) {
					if (notification.getNormalCount() > 0) {
						notifyMessage(2, "1");
					}
					if (notification.getMESum() > 0) {
						notifyMessage(3, "1");
					}
				}
			}
		};
	}

	/**
	 * Tab点击监听，用于进入书城后点击底部Tab 做离开书城Talking Data统计
	 * 
	 * @param tabChangeListener
	 */
	public void setTabChangeListener(TabChangeListener tabChangeListener) {
		this.mTabChangeListener = tabChangeListener;
	}
	// 获取状态栏的高度
	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
	// private HomeWatcher mHomeWatcher;

	public int getBookStoreIndex() {
		return mBookStoreIndex;
	}

	public void setBookStoreIndex(int mBookStoreIndex) {
		this.mBookStoreIndex = mBookStoreIndex;
	}
	

	public interface TabChangeListener {
		public void onTabChange(int tabIndex);
	}
}
