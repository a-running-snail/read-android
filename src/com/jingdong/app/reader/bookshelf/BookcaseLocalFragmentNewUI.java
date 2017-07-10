package com.jingdong.app.reader.bookshelf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.mzbook.sortview.model.BookShelfModel;
import com.android.mzbook.sortview.model.BookShelfModel.DownLoadType;
import com.android.mzbook.sortview.model.Folder;
import com.android.mzbook.sortview.optimized.DragGridLayout;
import com.android.mzbook.sortview.optimized.DragGridLayout.OnItemDragAndDropListener;
import com.android.mzbook.sortview.optimized.DragItem;
import com.android.mzbook.sortview.optimized.DragItemAdapter;
import com.android.mzbook.sortview.optimized.HeaderDragItemAdapter;
import com.android.mzbook.sortview.optimized.ImageSizeUtils;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookcaseCloudActivity;
import com.jingdong.app.reader.activity.BookcaseCloudDiskActivity;
import com.jingdong.app.reader.activity.BookcaseLocalFragmentListView;
import com.jingdong.app.reader.activity.BookcaseOthersReadingActivity;
import com.jingdong.app.reader.activity.ChangDuActivity;
import com.jingdong.app.reader.activity.FileBrowserActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.activity.OrderingBookCaseActivity;
import com.jingdong.app.reader.album.ImageManager;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.book.EBook;
import com.jingdong.app.reader.bookshelf.animation.EBookAnimationUtils;
import com.jingdong.app.reader.bookstore.search.BookShelfSearchActivity;
import com.jingdong.app.reader.client.DownloadHelper;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadStateManager;
import com.jingdong.app.reader.client.DownloadThreadQueue.RefreshAble;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.extension.integration.FloatingActionButton;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.io.StoragePath;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.preloader.CutBitmapDisplayer;
import com.jingdong.app.reader.reading.EpubCover;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ActivityUtils;
import com.jingdong.app.reader.util.FileGuider;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.MD5Util;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.SettingUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.TopBarPopupWindow;
import com.jingdong.app.reader.view.TopBarPopupWindow.onPopupWindowItemClickListener;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.bookshelf.BookShelfToolBar;
import com.jingdong.app.reader.view.bookshelf.DragGridView;
import com.jingdong.app.reader.view.bookshelf.DragGridView.OnItemDragListener;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.tendcloud.tenddata.TCAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * 书架
 */
public class BookcaseLocalFragmentNewUI extends CommonFragment implements onPopupWindowItemClickListener,
		TopBarViewListener, RefreshAble, OnPageChangeListener, OnItemDragListener {
	private static final String EBOOK_TYPE = "ebook";
	private List<Map<String, Object>> allBookList = new ArrayList<Map<String, Object>>(); // 第三方导入与本地书库合并
	private List<BookShelfModel> models;
	private static List<DragItem> mItems = Collections.synchronizedList(new LinkedList<DragItem>());
	private HeaderDragItemAdapter mItemAdapter;
	private LinearLayout recentLayout;
	private static boolean isOpenBook = false;
	private ImageSizeUtils utils;
	private List<BookShelfModel> recentModels = new ArrayList<BookShelfModel>();
	private TopBarView topBarView = null;
	private MyFolderView foldView = null;
	private TopBarPopupWindow leftPopupWindow = null;
	private TopBarPopupWindow rightPopupWindow = null;
	public static final String TYPE_LEFT_MENU = "left_menu";
	public static final String TYPE_RIGHT_MENU = "right_menu";
	protected final static int UPDATE_UI_MESSAGE = 0;
	protected final static int SHOW_USER_GUIDER = 1;
	private RelativeLayout viewpager_layout;
	private LinearLayout recent_Layout;
	// 顶部最近在读 滑动布局
	private ViewPager recentViewPager;
	private MyViewPagerAdapter recentViewPagerAdapter;
	private TextView no_recent_info;
	private ImageView[] dots;
	private int currentIndex;
	private List<View> allPagerViews = null;
	private LinearLayout dotsArea;
	private RelativeLayout root = null;
	private Context mContext;
	public static Handler handler;

	/** 图书更新提示消息 */
	private final static int CHECK_EBOOK_UPDATE = 1001;
	/** 图书更新提示检查定时器间隔时间 */
	private final static int EBOOK_UPDATE_CHECK_TIME = 1000;
	/** 图书更新提示检查计时时间 */
	private int mCheckTime = 0;
	/** 最大检查时间 */
	private int MAX_CHECK_TIME = 60;

	/** 书架整理模式布局 */
	private DragGridView mDragGridView = null;
	/** 书架整理模式工具栏 */
	private BookShelfToolBar mBookShelfToolBar = null;
	/** 是否处于整理模式 */
	private boolean isSelected = false;
	/** 整理模式被选中列表数据 */
	private List<DragItem> mSelectedList = new ArrayList<DragItem>();
	/** 整理模式全选标识 */
	private boolean mSelectedAll = false;
	/** 书架文件夹信息 */
	private List<Folder> folders = new ArrayList<Folder>();
	/** 整理模式文件移动对话框 */
	private Dialog mFileMoveDialog = null;
	/** 处理书架中书籍打开关闭动画 */
	private static EBookAnimationUtils mEBookAnimationUtils = null;

	public BookcaseLocalFragmentNewUI() {
		super();
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {
			MZLog.d("J", "onReceive action:" + intent.getAction());
			// 更新数据
			if (intent.getAction().equals("com.mzread.action.downloaded")) {
				getData();
				mItemAdapter.notifyDataSetChanged();
			} else if (intent.getAction().equals("com.mzread.action.refresh")) {
				getData();
				mItemAdapter.notifyDataSetChanged();
				prepareRecentPager();
				Toast.makeText(getActivity(), "设备解绑，清空书城图书", Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals("com.jdread.action.login")) {
				// 自动登录成功，设置悬浮窗显示状态
				handleFABState();
			}
		}

	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mContext = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// 根布局
		root = (RelativeLayout) inflater.inflate(R.layout.activity_root_layout, null);
		// 顶部工具栏
		topBarView = (TopBarView) root.findViewById(R.id.topbar);
		// 核心内容容器（最近阅读和书架）
		LinearLayout containerLayout = (LinearLayout) root.findViewById(R.id.container);
		// 加载书架布局
		View rootView = inflater.inflate(R.layout.bookcase_local_item, containerLayout/* 根节点 */);

		// 初始化topbar 开始
		List<String> rightItems = new ArrayList<String>();
		rightItems.add("整理");
		rightItems.add("搜索");

		List<String> rightItemSubmenu = new ArrayList<String>();
		rightItemSubmenu.add("封面模式");
		rightItemSubmenu.add("按名称排序");
		rightItemSubmenu.add("按时间排序");

		List<String> leftItems = new ArrayList<String>();
		leftItems.add("已购");
		leftItems.add("畅读");
		leftItems.add("借阅");
		leftItems.add("云盘");
		leftItems.add("导入本地图书");
		leftItems.add("大家最近在读");
		// leftItems.add("分类");

		// 初始化topbar 开始
		initTopbarView();

		rightPopupWindow = new TopBarPopupWindow(getActivity(), rightItems, rightItemSubmenu, TYPE_RIGHT_MENU);
		rightPopupWindow.setListener(this);
		rightPopupWindow.setDismissListener(new TopBarPopupWindow.DismissListener() {

			@Override
			public void onDismiss() {
				topBarView.setLeftMenuVisiable(true, R.drawable.topbar_add);
				topBarView.setRightMenuOneVisiable(true, R.drawable.topbar_menu, false);
			}
		});

		leftPopupWindow = new TopBarPopupWindow(getActivity(), leftItems, TYPE_LEFT_MENU);
		leftPopupWindow.setListener(this);
		leftPopupWindow.setDismissListener(new TopBarPopupWindow.DismissListener() {

			@Override
			public void onDismiss() {
				// 隐藏左侧按钮
				topBarView.setLeftMenuVisiable(true, R.drawable.topbar_add);
				// 显示右侧按钮
				topBarView.setRightMenuOneVisiable(true, R.drawable.topbar_menu, false);
			}
		});
		// 初始化topbar 结束

		// 顶部滑动最近阅读布局开始
		allPagerViews = new ArrayList<View>();

		// ############# 最近阅读，直接读取布局填充进容器
		recentLayout = new LinearLayout(getActivity());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		recentLayout.setOrientation(LinearLayout.VERTICAL);
		recentLayout.setLayoutParams(layoutParams);

		// 最近阅读根视图
		View layout = inflater.inflate(R.layout.bookcase_recent_layout, null);
		viewpager_layout = (RelativeLayout) layout.findViewById(R.id.viewpager_layout);
		recent_Layout = (LinearLayout) layout.findViewById(R.id.recent_layout);
		LinearLayout.LayoutParams recent_Layout_Params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		recent_Layout_Params.height = (int) utils.getPerItemImageHeight() + ScreenUtils.dip2px(32);
		recent_Layout.setLayoutParams(recent_Layout_Params);

		recentViewPager = (ViewPager) layout.findViewById(R.id.viewpager);
		RelativeLayout.LayoutParams recentViewPagerParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		recentViewPagerParams.height = (int) utils.getPerItemImageHeight() + ScreenUtils.dip2px(32);
		recentViewPager.setLayoutParams(recentViewPagerParams);

		recentViewPager.setOnPageChangeListener(this);
		recentViewPagerAdapter = new MyViewPagerAdapter(allPagerViews);
		recentViewPager.setAdapter(recentViewPagerAdapter);
		dotsArea = (LinearLayout) layout.findViewById(R.id.dotsArea);
		recentLayout.addView(layout);
		// 顶部滑动最近阅读布局结束

		mDragGridView = (DragGridView) rootView.findViewById(R.id.mDragGridView);
		mDragGridView.setOnItemDragListener(this);
		mBookShelfToolBar = new BookShelfToolBar(getActivity());
		mFab = (FloatingActionButton) rootView.findViewById(R.id.fabbutton);
		mFab.setColor(getResources().getColor(R.color.red_sub));
		mFab.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 未登录跳转到登录
				if (!LoginUser.isLogin()) {
					Intent intent = new Intent(getActivity(), LoginActivity.class);
					intent.putExtra("signNeedCallback", true);
					startActivity(intent);
				} else {
					mFab.setClickable(false);
					processSign();
				}
			}
		});

		mFab.listenTo(mDragGridView);
		mItemAdapter = new HeaderDragItemAdapter(getActivity(), mItems, this);
		mDragGridView.addHeaderView(recentLayout, null, false);
		mDragGridView.setAdapter(mItemAdapter);
		mBookShelfToolBar.setOnToolBarClickListener(new BookShelfToolBar.OnToolBarClickListener() {

			@Override
			public void clickSelectAllBtn() {
				mSelectedList.clear();
				if (mSelectedAll) {
					mSelectedAll = false;
				} else {
					mSelectedAll = true;
					List<DragItem> temp = new ArrayList<DragItem>();
					for (int i = 0; i < (mItems.size() - 1); i++) {
						DragItem data = mItems.get(i);
						if (data.isFolder()) {
							String userId = LoginUser.getpin();
							List<BookShelfModel> models = MZBookDatabase.instance
									.getBooksInFolder(data.getMo().getBookid(), -1, userId);
							Collections.sort(models, new TimeComparator());
							for (int r = 0; r < models.size(); r++) {
								if (models.get(r).getDownload_state() == LocalBook.STATE_FAILED
										|| models.get(r).getDownload_state() == LocalBook.STATE_LOAD_PAUSED
										|| models.get(r).getDownload_state() == LocalBook.STATE_LOADING
										|| models.get(r).getDownload_state() == LocalBook.STATE_LOAD_FAILED) {
									temp.add(new DragItem(models.get(r), false, false, -1, false));
								} else {
									temp.add(new DragItem(models.get(r), false, false, -1, true));
								}
							}
						} else {
							temp.add(data);
						}
					}
					mSelectedList.addAll(temp);
				}

				updateToolBarStatus();
				mBookShelfToolBar.selectAll(mSelectedAll);
				mItemAdapter.notifyDataSetChanged();
			}

			@Override
			public void clickMoveBtn() {
				moveFile2Folder();
			}

			@Override
			public void clickDeleteBtn() {
				DialogManager.showCommonDialog(mContext, getString(R.string.warings),
						"从书架上删除这" + mSelectedList.size() + "本书吗？外部导入的书和试读本删除后不可恢复", getString(R.string.delete),
						getString(R.string.cancel), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							// 删除书籍并更新界面
							for (int i = 0; i < mSelectedList.size(); i++) {
								deleteBooks(mSelectedList.get(i), -1);
							}

							hideTooBar();
							updateBookCase();
							if (null != BookcaseLocalFragmentListView.handler) {
								BookcaseLocalFragmentListView.handler
										.sendEmptyMessage(BookcaseLocalFragmentListView.UPDATEDATA);
							}
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							break;
						default:
							break;
						}

					}
				});

			}

			@Override
			public void clickCancleBtn() {
				hideTooBar();
			}
		});

		mDragGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				position -= 3;
				if (position < 0)
					return false;

				if (position >= (mItems.size() - 1)) {
					return false;
				}

				if (isSelected) {
					return false;
				} 
				
				final ImageView touming = (ImageView)view.findViewById(R.id.touming);
				touming.setVisibility(View.VISIBLE);
				touming.setBackgroundColor(getResources().getColor(R.color.bookshelf_item_selected_bg));
				touming.postDelayed(new Runnable() {
					public void run() {
						touming.setVisibility(View.GONE);
					}
				}, 500);

				isSelected = true;
				mSelectedList.clear();
				mBookShelfToolBar.show(root);
				mBookShelfToolBar.bottomButtonEnable(true);
				if (mItems.get(position).isFolder()) {
					DragItem data = mItems.get(position);
					String userId = LoginUser.getpin();
					List<BookShelfModel> models = MZBookDatabase.instance.getBooksInFolder(data.getMo().getBookid(), -1,
							userId);
					Collections.sort(models, new TimeComparator());
					for (int r = 0; r < models.size(); r++) {
						if (models.get(r).getDownload_state() == LocalBook.STATE_FAILED
								|| models.get(r).getDownload_state() == LocalBook.STATE_LOAD_PAUSED
								|| models.get(r).getDownload_state() == LocalBook.STATE_LOADING
								|| models.get(r).getDownload_state() == LocalBook.STATE_LOAD_FAILED) {
							mSelectedList.add(new DragItem(models.get(r), false, false, -1, false));
						} else {
							mSelectedList.add(new DragItem(models.get(r), false, false, -1, true));
						}
					}
				} else {
					mSelectedList.add(mItems.get(position));
				}

				updateToolBarStatus();
				return false;
			}
		});

		mDragGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				position -= 3;
				if (position < 0) {
					return;
				}

				if (position == (mItems.size() - 1)) {
					if (leftPopupWindow != null) {
						leftPopupWindow.show(topBarView);
					}
					return;
				}

				if (isSelected) {
					DragItem data = mItems.get(position);
					if (data.isFolder()) {
						int actionbarHeight = TopBarView.getTopBarHeightPix(getActivity());
						foldView = new MyFolderView(actionbarHeight, view, mItems.get(position).getMo().getBookid(),
								getActivity(), getActivity().getWindow().getDecorView(), true);
						return;
					}
					boolean exits = false;
					int index = 0;
					for (int j = 0; j < mSelectedList.size(); j++) {
						index = j;
						DragItem sdata = mSelectedList.get(j);
						long dec_bookid = data.getMo().getServerid();
						long src_bookid = sdata.getMo().getServerid();
						if ((0 == dec_bookid) && (0 == src_bookid)) {
							String dec_bookcover = data.getMo().getBookCover();
							String src_bookcover = sdata.getMo().getBookCover();
							if (!TextUtils.isEmpty(dec_bookcover) && !TextUtils.isEmpty(src_bookcover)) {
								if (dec_bookcover.equals(src_bookcover)) {
									exits = true;
									break;
								}
							} else {
								int dec_id = data.getMo().getId();
								int src_id = sdata.getMo().getId();
								if (dec_id == src_id) {
									exits = true;
									break;
								}
							}
						} else {
							if (dec_bookid == src_bookid) {
								exits = true;
								break;
							}
						}
					}

					if (exits) {
						mSelectedList.remove(index);
						RelativeLayout mSelectedLayout = (RelativeLayout) view.findViewById(R.id.mSelectedLayout);
						if (null != mSelectedLayout) {
							mSelectedLayout.setVisibility(View.GONE);
						}
					} else {
						mSelectedList.add(data);
						RelativeLayout mSelectedLayout = (RelativeLayout) view.findViewById(R.id.mSelectedLayout);
						if (null != mSelectedLayout) {
							mSelectedLayout.setVisibility(View.VISIBLE);
						}
					}

					updateToolBarStatus();
				} else {
					clickEbook(position, view);
				}

			}
		});

		TCAgent.onPageStart(getActivity(), "书架");

		return root;
	}

	/**
	 * 点击封面模式图书
	 * 
	 * @param position
	 * @param view
	 */
	private void clickEbook(int position, View view) {
		final ImageView touming = (ImageView)view.findViewById(R.id.touming);
		touming.setVisibility(View.VISIBLE);
		touming.setBackgroundColor(getResources().getColor(R.color.bookshelf_item_selected_bg));
		touming.postDelayed(new Runnable() {
			public void run() {
				touming.setVisibility(View.GONE);
			}
		}, 100);
		
		if (!mItems.get(position).isDownloaded()) {
			DragItem item = mItems.get(position);
			BookShelfModel model = item.getMo();
			DownloadedAble downloadedAble = null;

			// 读取图书信息
			if (model.getBookType().equals(BookShelfModel.EBOOK)) {
				downloadedAble = LocalBook.getLocalBookByIndex(model.getBookid());
			} else if (model.getBookType().equals(BookShelfModel.DOCUMENT)) {
				downloadedAble = MZBookDatabase.instance.getLocalDocument(model.getBookid());
				MZLog.d("wangguodong", "点击document");
			} else {
				return;
			}

			// 图书的下载状态进行判断
			if (model.getDownload_state() == LocalBook.STATE_FAILED // 下载失败
					|| model.getDownload_state() == LocalBook.STATE_LOAD_FAILED/* 下载失败 */) {
				MZLog.d("wangguodong", "重新开始下载书籍...");
				model.startDownload = true;
				model.setDownload_state(LocalBook.STATE_LOADING);
				item.setMo(model);
				mItems.set(position, item);
				Bundle bundle = new Bundle();
				bundle.putInt("index", position);
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
				bundle.putInt("progress", 0);
				bundle.putString("bookid", "" + model.getServerid());
				sendMessage(bundle);
				DownloadHelper.restartDownload(getActivity(), model.getBookType(), downloadedAble);

			} else if (model.getDownload_state() == LocalBook.STATE_LOAD_PAUSED) {
				Bundle bundle = new Bundle();
				bundle.putInt("index", position);
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
				bundle.putInt("progress", -1);
				bundle.putString("bookid", "" + model.getServerid());
				sendMessage(bundle);

				model.startDownload = true;
				model.setDownload_state(LocalBook.STATE_LOADING);
				item.setMo(model);
				mItems.set(position, item);
				DownloadHelper.resumeDownload(getActivity(), model.getBookType(), downloadedAble);
			} else if (model.getDownload_state() == LocalBook.STATE_LOADING) {
				SettingUtils.getInstance().putBoolean("startDownloading:" + model.getServerid(), false);
				MZLog.d("wangguodong", "暂停下载书籍...");
				Bundle bundle = new Bundle();
				bundle.putInt("index", position);
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_PAUSED);
				bundle.putInt("progress", -1);
				bundle.putString("bookid", "" + model.getServerid());
				sendMessage(bundle);

				model.startDownload = false;
				model.setDownload_state(LocalBook.STATE_LOAD_PAUSED);
				item.setMo(model);
				mItems.set(position, item);

				DownloadHelper.stopDownload(getActivity(), model.getBookType(), downloadedAble);
			} else if (model.getDownload_state() == LocalBook.STATE_LOADED
					|| model.getDownload_state() == LocalBook.STATE_LOAD_READING) {
				model.setDownload_state(LocalBook.STATE_LOADED);
				item.setMo(model);
				mItems.set(position, item);

				Bundle bundle = new Bundle();
				bundle.putInt("index", position);
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED);
				bundle.putInt("progress", -1);
				bundle.putString("bookid", "" + model.getServerid());
				sendMessage(bundle);
			} else {
				Toast.makeText(getActivity(), "无法继续下载，书籍有问题...", Toast.LENGTH_LONG).show();
			}
			return;
		}

		// 点击的是非文件夹
		if (!mItems.get(position).isFolder()) {// 点击的是单本书
			// 获取图书信息
			BookShelfModel model = mItems.get(position).getMo();
			boolean buyed = SettingUtils.getInstance().getBoolean("Buyed:" + model.getServerid(), false);
			if (buyed) {
				SettingUtils.getInstance().putLong("change_time" + model.getServerid(), (long) model.getModifiedTime());
				deleteBooks(mItems.get(position), position);
				mItemAdapter.updateDownloadEbook(model.getServerid(), model.getBookCoverLabel(),model.getDownloadType());
				return;
			}

			if (model.getBookType().equals(BookShelfModel.EBOOK)) {
				final EBook ebook = MZBookDatabase.instance.getEBook(model.getBookid());
				if (ebook != null) {
					if (!isOpenBook) {
						isOpenBook = true;
						ImageView itemIcon = (ImageView) view.findViewById(R.id.imageViewIcon);
						OpenBookHelper.openEBook(getActivity(), ebook.bookId, itemIcon);
					}
				}
			} else if (model.getBookType().equals(BookShelfModel.DOCUMENT)) {
				final Document doc = MZBookDatabase.instance.getDocument(model.getBookid());
				if (doc != null) {
					if (!isOpenBook) {
						isOpenBook = true;
						ImageView itemIcon = (ImageView) view.findViewById(R.id.imageViewIcon);
						OpenBookHelper.openDocument(getActivity(), doc.documentId, itemIcon);
					}
				}
			} else if (model.getBookType().equals(BookShelfModel.MORE)) {
				if (leftPopupWindow != null) {
					leftPopupWindow.show(topBarView);
				}
			}

		} else {// 点击的是文件夹
			int actionbarHeight = TopBarView.getTopBarHeightPix(getActivity());
			if (mItems.get(position).isFolder()) {
				foldView = new MyFolderView(actionbarHeight, view, mItems.get(position).getMo().getBookid(),
						getActivity(), getActivity().getWindow().getDecorView(), true);

			}
		}
	}

	/**
	 * 更新整理模式标题栏状态
	 */
	private void updateToolBarStatus() {
		if (0 == mSelectedList.size()) {
			mBookShelfToolBar.bottomButtonEnable(false);
			mBookShelfToolBar.updateTitle("请选择图书");
		} else {
			mBookShelfToolBar.bottomButtonEnable(true);
			mBookShelfToolBar.updateTitle("已选择" + mSelectedList.size() + "本图书");
		}
	}

	/**
	 * 检查当前数据项是否被选中
	 * 
	 * @param item
	 * @return
	 */
	private boolean selectedListIsExits(DragItem item) {
		boolean exits = false;
		for (int j = 0; j < mSelectedList.size(); j++) {
			DragItem sdata = mSelectedList.get(j);
			long dec_bookid = item.getMo().getServerid();
			long src_bookid = sdata.getMo().getServerid();
			if ((0 == dec_bookid) && (0 == src_bookid)) {
				String dec_bookcover = item.getMo().getBookCover();
				String src_bookcover = sdata.getMo().getBookCover();
				if (!TextUtils.isEmpty(dec_bookcover) && !TextUtils.isEmpty(src_bookcover)) {
					if (dec_bookcover.equals(src_bookcover)) {
						exits = true;
						break;
					}
				}
			} else {
				if (dec_bookid == src_bookid) {
					exits = true;
					break;
				}
			}
		}

		return exits;
	}

	public void processSign() {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				if (mFab != null)
					mFab.setClickable(true);
			}
		}, 3000);

		IntegrationAPI.signGetScore(getActivity(), false, new GrandScoreListener() {

			@Override
			public void onGrandSuccess(SignScore score) {
				// 1、隐藏悬浮窗
				mFab.setVisibility(View.GONE);
				new SignTipDialog(mContext, score);
			}

			@Override
			public void onGrandFail() {
				mFab.setClickable(true);
			}
		});
	}

	private FloatingActionButton mFab;

	private void handleFABState() {
		LauncherActivity containerAct = (LauncherActivity) getActivity();
		if (containerAct != null) {
			containerAct.handleFloatingButtonState(mFab);
		} else {
			MZLog.e("J", "containerAct is null");
		}
	};

	// 最近在读 滑动布局
	class MyViewPagerAdapter extends PagerAdapter {

		private List<View> views;

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if (position < views.size())
				((ViewPager) container).removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ViewGroup parent = (ViewGroup)views.get(position).getParent();
			if ( null != parent ) {
				parent.removeAllViews();
			}
			
			((ViewPager) container).addView(views.get(position), 0);
			return views.get(position);
		}

		public MyViewPagerAdapter(List<View> views) {
			this.views = views;
		}

		@Override
		public int getCount() {
			if (views != null) {
				return views.size();
			}
			return 0;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return (arg0 == (View) arg1);
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

	public void sendMessage(Bundle bundle) {
		Message msg = new Message();
		msg.what = UPDATE_UI_MESSAGE;
		msg.obj = bundle;
		handler.sendMessage(msg);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MZLog.d("X", "onCreate");
		fragmentTag = "BookcaseLocalFragmentNewUI";
		MZLog.d("life-cycle", "书架onCreate");

		MZLog.d("wangguodong", "清理最近提到的数据表");
		MZBookDatabase.instance.clearMention();
		MZLog.d("wangguodong", "清理无用的空文件夹");
		MZBookDatabase.instance.clearFolder();
		String userId = LoginUser.getpin();
		utils = new ImageSizeUtils(getActivity());
		MZLog.d("书籍封面高度", utils.getPerItemImageHeight() + "===");

		IntentFilter filter = new IntentFilter();
		filter.addAction("com.mzread.action.refresh");
		filter.addAction("com.jdread.action.login");
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
		// 打开书籍动画工具类初始化
		mEBookAnimationUtils = new EBookAnimationUtils(getActivity());

		handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (msg.what == UPDATE_UI_MESSAGE) {
					Bundle bundleData = (Bundle) msg.obj;
					int index = bundleData.getInt("index");
					int status = bundleData.getInt("status");
					int progress = bundleData.getInt("progress");
					String bookid = bundleData.getString("bookid");
					updateItemView(index, status, progress, bookid);
				} else if (msg.what == CHECK_EBOOK_UPDATE) {
					checkEbookUpdateState();
				} else if (msg.what == BookcaseLocalFragmentListView.UPDATEDATA) {
					updateBookCase();
				}
			};
		};
	}

	/**
	 * @Description: 定时检查登录状态请求图书更新数据 @return void @author xuhongwei1 @date
	 * 2015年10月9日 上午11:51:52 @throws
	 */
	private void checkEbookUpdateState() {
		mCheckTime++;
		handler.removeMessages(CHECK_EBOOK_UPDATE);
		if (mCheckTime > MAX_CHECK_TIME) {
			mCheckTime = 0;
			return;
		}

		if (!LoginUser.isLogin()) {
			handler.sendEmptyMessageDelayed(CHECK_EBOOK_UPDATE, EBOOK_UPDATE_CHECK_TIME);
		} else {
			mCheckTime = 0;
			getTodayBuyedEbookOrderList();
			requestEbookUpdateData();
		}
	}

	/**
	 * @Description: 获取当天订单图书列表 @author xuhongwei1 @date 2015年11月25日
	 * 下午1:56:20 @throws
	 */
	private void getTodayBuyedEbookOrderList() {
		if (!NetWorkUtils.isNetworkConnected(mContext)) {
			return;
		}

		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getTodayBuyedEbookOrderListParams(),
				new MyAsyncHttpResponseHandler(mContext) {
					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String jsonStr = new String(responseBody);
						try {
							JSONObject json = new JSONObject(jsonStr);
							String code = json.optString("code");
							if (!code.equals("0")) {
								return;
							}

							JSONArray jsonarr = json.getJSONArray("resultList");
							if (null == jsonarr) {
								return;
							}

							boolean update = false;
							for (int i = 0; i < jsonarr.length(); i++) {
								JSONObject obj = (JSONObject) jsonarr.opt(i);
								if (null != obj) {
									long serverid = obj.optLong("bookId");
									if (!checkEbookExits(serverid)) {
										update = true;
										SettingUtils.getInstance().putBoolean("AddBookShelf:" + serverid, true);
										SettingUtils.getInstance().putBoolean("Buyed:" + serverid, true);
										final LocalBook localBook = new LocalBook();
										localBook.book_id = serverid;
										localBook.bigImageUrl = obj.optString("largeSizeImgUrl");
										localBook.smallImageUrl = obj.optString("imgUrl");
										localBook.order_code = String.valueOf(obj.optLong("orderId"));
										localBook.title = obj.optString("name");
										localBook.author = obj.optString("author");
										localBook.state = LocalBook.STATE_NO_LOAD;
										localBook.userName = LoginUser.getpin();
										localBook.add_time = System.currentTimeMillis();
										localBook.mod_time = System.currentTimeMillis();

										int format = obj.optInt("format");
										String formatName = obj.optString("formatMeaning");
										if (!TextUtils.isEmpty(formatName)) {
											if (formatName.equals(LocalBook.FORMATNAME_PDF)) {
												localBook.format = LocalBook.FORMAT_PDF;
												localBook.formatName = LocalBook.FORMATNAME_PDF;
											}
											if (formatName.equals(LocalBook.FORMATNAME_EPUB)) {
												localBook.format = LocalBook.FORMAT_EPUB;
												localBook.formatName = LocalBook.FORMATNAME_EPUB;
											}
										} else if (format != -1) {
											if (format == LocalBook.FORMAT_PDF) {
												localBook.format = LocalBook.FORMAT_PDF;
												localBook.formatName = LocalBook.FORMATNAME_PDF;
											} else if (format == LocalBook.FORMAT_EPUB) {
												localBook.format = LocalBook.FORMAT_EPUB;
												localBook.formatName = LocalBook.FORMATNAME_EPUB;
											}
										} else {
											continue;
										}

										boolean isave = localBook.save();
										if (isave) {
											MZBookDatabase.instance.savaJDEbookToBookShelf(serverid,
													String.valueOf(System.currentTimeMillis()), LoginUser.getpin());
										}

									}
								}
							}

							if (update) {
								updateUI();
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
	}

	/**
	 * @Description: 检查列表中是否存在 @param long bookid 图书id @author xuhongwei1 @date
	 * 2015年11月25日 下午7:28:17 @throws
	 */
	private boolean checkEbookExits(long bookid) {
		boolean AddBookShelf = SettingUtils.getInstance().getBoolean("AddBookShelf:" + bookid, false);
		if (AddBookShelf) {
			return true;
		}

		if (null == mItems || mItems.size() <= 0) {
			return false;
		}

		for (int i = 0; i < mItems.size(); i++) {
			DragItem item = mItems.get(i);
			if (item.isFolder()) {
				List<BookShelfModel> models = MZBookDatabase.instance.getBooksInFolder(item.getMo().getBookid(), -1,
						LoginUser.getpin());
				if (null != models && models.size() > 0) {
					for (int r = 0; r < models.size(); r++) {
						long subid = models.get(r).getServerid();
						if (subid == bookid) {
							return true;
						}
					}
				}
			} else {
				long serverid = item.getMo().getServerid();
				if (serverid == bookid) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @Description: 请求图书文件更新数据 @return void @author xuhongwei1 @date 2015年10月9日
	 * 下午1:49:31 @throws
	 */
	private void requestEbookUpdateData() {
		if (!NetWorkUtils.isNetworkConnected(mContext) || mItems.size() <= 0) {
			return;
		}

		List<String> ebookids = new ArrayList<String>();
		for (DragItem mDragItem : mItems) {
			long ebookid = mDragItem.getMo().getServerid();
			if (ebookid != 0 && (mDragItem.getMo().getBookCoverLabel() != BookShelfModel.LABEL_TRYREAD)) {
				ebookids.add(Long.toString(ebookid));
			}
		}

		if (ebookids.size() <= 0) {
			return;
		}

		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getEBookUpdateParams(ebookids),
				new MyAsyncHttpResponseHandler(mContext) {
					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String jsonStr = new String(responseBody);
						try {
							JSONObject json = new JSONObject(jsonStr);
							String code = json.optString("code");
							if (!code.equals("0")) {
								return;
							}

							JSONArray jsonarr = json.getJSONArray("resultList");
							if (null == jsonarr) {
								return;
							}

							for (int i = 0; i < jsonarr.length(); i++) {
								JSONObject obj = (JSONObject) jsonarr.get(i);
								if (null != obj) {
									String ebookId = obj.optString("ebookId");
									boolean needUpdate = obj.optBoolean("needUpdate");

									if (!needUpdate) {
										needUpdate = SettingUtils.getInstance().getBoolean("file_error:" + ebookId,
												false);
										if (!needUpdate) {
											continue;
										}
									}

									for (int j = 0; j < mItems.size(); j++) {
										DragItem item = mItems.get(j);
										String serverId = Long.toString(item.getMo().getServerid());
										if (ebookId.equals(serverId)) {
											mItems.get(j).getMo().setDownloadType(DownLoadType.Update);
											break;
										}
									}
								}
							}

							mItemAdapter.notifyDataSetChanged();
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
	}

	@Override
	public void onResume() {
		super.onResume();

		MZLog.d("X", "OnResume");
		// 将悬浮窗设置为隐藏状态
		mFab.setVisibility(View.GONE);
		handleFABState();

		if (!TextUtils.isEmpty(LoginUser.getpin()) && !LocalUserSetting.getLoginSacn(getActivity()))
			LoginUser.scanDocumentToBookShelf(getActivity(), LoginUser.getpin());

		isOpenBook = false;
		// 设置下载回调
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_DOCUMENT);
		updateBookCase();
		if (foldView != null && foldView.mIsOpened)
			foldView.updateSubUI();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.mzread.action.downloaded");
		filter.addAction("com.mzread.action.refresh");
		filter.addAction("com.jdread.action.login");

		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);

		EBookAnimationUtils anim = BookcaseLocalFragmentNewUI.getEBookAnimationUtils();
		if (null != anim) {
			if (anim.getIsOpen()) {
				anim.closeEBook();
			}
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			StatService.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_bookshelf));
		} else {
			StatService.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_bookshelf));
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		MZLog.d("X", "onHiddenChanged->hidden::" + hidden);
		super.onHiddenChanged(hidden);
		// true表示该fragment被隐藏了
		if (hidden) {
			// SignFloatWinManager.getInstance().dismiss(false);
		} else {// false，表示该fragment正在显示
			DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
			DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_DOCUMENT);

			// LauncherActivity containerAct = (LauncherActivity) getActivity();
			// if (containerAct != null) {
			// containerAct.showSignFloatView();
			// }else{
			// MZLog.e("J", "containerAct is null");
			// }

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MZLog.d("J", "BookcaseLocalFragmentNewUI#onDestroy");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		MZLog.d("J", "BookcaseLocalFragmentNewUI#onDestroyView");
		TCAgent.onPageEnd(getActivity(), "书架");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		MZLog.d("J", "BookcaseLocalFragmentNewUI#onDetach");

	}

	@Override
	public void onStop() {
		MZLog.d("life-cycle", "书架onStop");
		super.onStop();
	}

	// new 最近在读
	public void prepareRecentPager() {
		if (recentModels == null)
			return;

		if (recentModels.size() == 0) {
			MZLog.d("wanggudong", "未发现最近在读书籍...");
			recent_Layout.setVisibility(View.VISIBLE);
			viewpager_layout.setVisibility(View.GONE);
		} else {
			MZLog.d("wanggudong", "查询到最近在读书籍...");
			recent_Layout.setVisibility(View.GONE);
			viewpager_layout.setVisibility(View.VISIBLE);
			List<View> list = new ArrayList<View>();
			for (int i = 0; i < recentModels.size(); i++) {
				// 最近阅读子布局
				final View view = LayoutInflater.from(getActivity()).inflate(R.layout.bookcase_recent_layout_item,
						null);
				final ImageView bookcover = (ImageView) view.findViewById(R.id.book_cover);
				TextView bookname = (TextView) view.findViewById(R.id.book_name);
				TextView bookauthor = (TextView) view.findViewById(R.id.book_author);
				TextView bookProgressAndNotes = (TextView) view.findViewById(R.id.book_progress_notes);
				TextView bookReadAt = (TextView) view.findViewById(R.id.read_at);
				ImageView imageViewLabel = (ImageView) view.findViewById(R.id.imageViewLabel);

				if (MZBookApplication.isPad()) {
					bookname.setTextSize(16);
					bookauthor.setTextSize(14);
					bookProgressAndNotes.setTextSize(14);
					bookReadAt.setTextSize(14);
				}

				final BookShelfModel model = recentModels.get(i);
				DisplayImageOptions option = GlobalVarable.getDefaultBookDisplayOptions();
				String cover = model.getBookCover();

				if (model.getBookType().equals(BookShelfModel.EBOOK)) {
					option = GlobalVarable.getCutBookDisplayOptions(false);
					if (cover != null && !cover.contains("http://")) {
						cover = "file://" + cover;
					}

				} else if (cover != null && cover.contains("http://")) {
					option = GlobalVarable.getCutBookDisplayOptions(false);
				} else {
					cover = "file://" + cover;
				}

				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT);
				layoutParams.height = (int) utils.getPerItemImageHeight();
				layoutParams.width = (int) utils.getPerItemImageWidth();
				bookcover.setLayoutParams(layoutParams);

				if (TextUtils.isEmpty(model.getBookCover())) {
					String dir = "";
					if ( !TextUtils.isEmpty( model.getBookName() ) ) {
						dir = MZBookApplication.getInstance().getCachePath() + "/downloads/"
								+ MD5Util.md5Hex(model.getBookName()) + "/";
					} else {
						dir = MZBookApplication.getInstance().getCachePath() + "/downloads/temp/";
					}
					
					Bitmap bitmap = ImageManager.getBitmapFromCache(dir + "/tempCover.png");
					if (null != bitmap) {
						bookcover.setImageBitmap(bitmap);
					} else {
						String path = EpubCover.generateCover(mContext, dir, model.getBookName());
						if (!TextUtils.isEmpty(path)) {
							Bitmap b = ImageManager.getBitmapFromCache(path);
							if (null != b) {
								bookcover.setImageBitmap(b);
							}
						}
					}
				} else {
					ImageLoader.getInstance().displayImage(cover, bookcover, option, new ImageLoadingListener() {
						@Override
						public void onLoadingCancelled(String arg0, View arg1) {

						}

						@Override
						public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
							if (getActivity() != null && getResources() != null) {
								if (arg2 != null && !arg2.isRecycled()) {
									arg2 = CutBitmapDisplayer.CropForExtraWidth(arg2, false);
									view.setBackgroundDrawable(ImageUtils.overlay(ImageUtils.BoxBlurFilter(arg2),
											BitmapFactory.decodeResource(getResources(), R.drawable.overlay)));
								} else {
									view.setBackgroundDrawable(ImageUtils.overlay(
											BitmapFactory.decodeResource(getResources(), R.drawable.book_cover_default),
											BitmapFactory.decodeResource(getResources(), R.drawable.overlay)));
								}
							}

						}

						@Override
						public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
							if (getActivity() != null && getResources() != null) {
								view.setBackgroundDrawable(ImageUtils.overlay(
										BitmapFactory.decodeResource(getResources(), R.drawable.book_cover_default),
										BitmapFactory.decodeResource(getResources(), R.drawable.overlay)));
							}
						}

						@Override
						public void onLoadingStarted(String arg0, View arg1) {

						}
					});
				}

				bookname.setText(model.getBookName());
				bookauthor.setText(
						"null".equals(model.getAuthor()) ? getString(R.string.author_unknown) : model.getAuthor());

				int bookCoverLabel = model.getBookCoverLabel();
				if (bookCoverLabel == BookShelfModel.LABEL_TRYREAD) {
					imageViewLabel.setVisibility(View.VISIBLE);
					imageViewLabel.setImageResource(R.drawable.badge_coverlabel_trial);
				} else if (bookCoverLabel == BookShelfModel.LABEL_CHANGDU) {
					imageViewLabel.setVisibility(View.VISIBLE);
					imageViewLabel.setImageResource(R.drawable.badge_coverlabel_vip);
				} else if (bookCoverLabel == BookShelfModel.LABEL_BORROWED) {
					imageViewLabel.setVisibility(View.VISIBLE);
					imageViewLabel.setImageResource(R.drawable.badge_coverlabel_borrow);
				} else {
					imageViewLabel.setVisibility(View.GONE);
				}

				bookProgressAndNotes
						.setText("阅读进度" + Math.round(model.getBookPercent()) + "%,阅读笔记共" + model.getNote_num() + "条");
				bookReadAt.setText("上次阅读时间 "
						+ TimeFormat.formatTimeByMiliSecond(getResources(), (long) model.getPercentTime() * 1000));

				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!isOpenBook) {
							isOpenBook = true;
							if (model.getBookType().equals(BookShelfModel.EBOOK)) {
								EBook ebook = MZBookDatabase.instance.getEBook(model.getBookid());
								if (ebook != null) {
									OpenBookHelper.openEBook(getActivity(), ebook.bookId, bookcover);
								}
							} else {
								Document doc = MZBookDatabase.instance.getDocument(model.getBookid());
								if (doc != null) {
									OpenBookHelper.openDocument(getActivity(), doc.documentId, bookcover);
								}
							}
						}
					}
				});

				list.add(view);
			}

			allPagerViews.clear();
			if (list.size() > 0) {
				allPagerViews.addAll(list);
				recentViewPager.setAdapter(recentViewPagerAdapter);
			} else
				no_recent_info.setVisibility(View.VISIBLE);

			intiBottomDot();
		}

	}

	// 初始化底部红点
	public void intiBottomDot() {

		dotsArea.removeAllViews();
		dots = new ImageView[recentModels.size()];

		for (int i = 0; i < recentModels.size(); i++) {
			ImageView view = (ImageView) LayoutInflater.from(getActivity()).inflate(R.layout.recent_layout_dot, null);
			dotsArea.addView(view);
		}

		for (int i = 0; i < recentModels.size(); i++) {
			dots[i] = (ImageView) dotsArea.getChildAt(i);
			dots[i].setEnabled(false);// 都设为灰色
			dots[i].setTag(i);
		}
		currentIndex = 0;
		if (recentModels.size() > 0)
			dots[currentIndex].setEnabled(true);

	}

	private void setCurrentView(int position) {

		if (position < 0 || position > 4) {
			return;
		}
		recentViewPager.setCurrentItem(position);

	}

	private void setCurrentDot(int positon)

	{

		if (positon < 0 || positon > 4 || currentIndex == positon) {
			return;
		}

		dots[positon].setEnabled(true);
		dots[currentIndex].setEnabled(false);
		currentIndex = positon;

	}

	// 判断是否是外部导入书籍
	public boolean isEbookType(Map<String, Object> map) {
		if (null != map.get(EBOOK_TYPE)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取数据
	 */
	public void getData() {
		MZLog.d("wangguodong", "重新获取书架书籍数据");
		mItems.clear();
		recentModels.clear();
		String userId = LoginUser.getpin();
		models = MZBookDatabase.instance.listBookShelf(userId, 0);
		// 最近阅读数据
		recentModels = MZBookDatabase.instance.listRecentReadingBooks(userId);

		MZLog.d("wangguodong", "获取书架最近书籍数据" + models == null ? "0" : models.size() + "条");
		MZLog.d("wangguodong", "获取书架最近在读数据" + recentModels == null ? "0" : recentModels.size() + "条");

		// 添加更多
		BookShelfModel more = new BookShelfModel();
		more.setBelongDirId(-1);
		more.setBookName("");
		more.setBookCover("");
		more.setBookType(BookShelfModel.MORE);

		Collections.sort(models, new TimeComparator());
		models.add(more);
		for (int r = 0; r < models.size(); r++) {
			double time1 = (double) models.get(r).getModifiedTime();

			if (models.get(r).getBookName() == null && !models.get(r).getBookType().equals("folder")) {
				continue;
			}

			if (models.get(r).getBookType().equals("folder"))
				mItems.add(new DragItem(models.get(r), true, false, -1, true));
			else {
				boolean buyed = SettingUtils.getInstance().getBoolean("Buyed:" + models.get(r).getServerid(), false);
				if (buyed) {
					models.get(r).setDownloadType(DownLoadType.Buyed);
				}

				if (models.get(r).getDownload_state() == LocalBook.STATE_FAILED
						|| models.get(r).getDownload_state() == LocalBook.STATE_LOAD_PAUSED
						|| models.get(r).getDownload_state() == LocalBook.STATE_LOADING
						|| models.get(r).getDownload_state() == LocalBook.STATE_LOAD_FAILED) {
					mItems.add(new DragItem(models.get(r), false, false, -1, false));
				} else {
					mItems.add(new DragItem(models.get(r), false, false, -1, true));
				}

			}
		}

		checkEbookUpdateState();
	}

	public int getExtraItemCount() {

		if (LocalUserSetting.getRegisterNewUserFlag(getActivity())) {
			return 1;
		}
		return 2;
	}

	// 更新书架书籍
	public void updateBookCase() {

		allBookList.clear();
		getData();
		mItemAdapter.notifyDataSetChanged();
		prepareRecentPager();
	}

	// 更新最近在读界面
	public void updateRecentLayout() {
		recentModels.clear();
		String userId = LoginUser.getpin();
		recentModels = MZBookDatabase.instance.listRecentReadingBooks(userId);
		prepareRecentPager();
	}

	// 获得指定type id 的item 的位置
	public int getPositionByTypeAndId(String type, int id) {
		if (mItems == null)
			return -1;
		else {
			try {
				for (int i = 0; i < mItems.size(); i++) {
					if (mItems.size() <= i || mItems.get(i) == null)
						continue;
					BookShelfModel model = mItems.get(i).getMo();

					if (model.getBookType().equals(type)) {
						if (model.getBookid() == id) {
							return i;
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return -1;
		}
	}

	public void updateItemData(int position, boolean isDownloaded, int progress) {
		if (mItems != null && mItems.size() > position && position > -1) {
			DragItem item = mItems.get(position);
			item.setDownloaded(isDownloaded);
			if (isDownloaded) {
				item.getMo().setDownloadType(DownLoadType.Normal);
			}
			mItems.set(position, item);
		}
	}

	public void updateItemView(final int index, int download_status, int progress, final String bookid) {// progress
		// 是百分比整数
		// 50%
		// ==50

		if (index < 0)
			return;
		if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED) {
			SettingUtils.getInstance().putBoolean("Buyed:" + bookid, false);
			SettingUtils.getInstance().putBoolean("file_error:" + bookid, false);
			updateItemData(index, true, progress);
		} else {
			updateItemData(index, false, progress);
		}

		if (mDragGridView != null) {
			int visiblePos = mDragGridView.getFirstVisiblePosition() - 3;// 由于header原因
			// 这里要减去列数
			int offset = index - visiblePos;
			if (offset < 0)
				return;
			View view = mDragGridView.getChildAt(offset);
			if (view != null) {

				final FrameLayout downloadState = ViewHolder.get(view, R.id.download_state);
				final TextView downloadStatus = ViewHolder.get(view, R.id.download_status);
				final TextView downloadProgress = ViewHolder.get(view, R.id.download_progress);
				ProgressBar progress_horizontal = ViewHolder.get(view, R.id.progress_horizontal);
				ImageView download_status_btn = ViewHolder.get(view, R.id.download_status_btn);
				RelativeLayout downloading_layout = ViewHolder.get(view, R.id.downloading_layout);
				LinearLayout download_finish_layout = ViewHolder.get(view, R.id.download_finish_layout);
				ImageView download_status_icon = ViewHolder.get(view, R.id.download_status_icon);
				TextView download_finish_text = ViewHolder.get(view, R.id.download_finish_text);

				MZLog.d("wangguodong", "downloadState==null??" + (downloadState == null ? "null" : "not null"));
				boolean isUpdate = SettingUtils.getInstance().getBoolean(bookid);
				if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED) {
					downloading_layout.setVisibility(View.GONE);
					download_finish_layout.setVisibility(View.VISIBLE);
					download_status_icon.setBackgroundResource(R.drawable.icon_d);
					if (isUpdate) {
						download_finish_text.setText("更新成功");
					} else {
						download_finish_text.setText("下载成功");
					}
					downloadState.postDelayed(new Runnable() {
						@Override
						public void run() {
							SettingUtils.getInstance().putBoolean(bookid, false);
							downloadState.setVisibility(View.GONE);
							downloadStatus.setText("");
							downloadProgress.setText("");
						}
					}, 1000);
				} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING) {
					downloading_layout.setVisibility(View.VISIBLE);
					download_finish_layout.setVisibility(View.GONE);
					downloadState.setVisibility(View.VISIBLE);
					if (isUpdate) {
						downloadStatus.setText("更新中...");
					} else {
						downloadStatus.setText("下载中...");
					}
					if (progress < 0)
						progress = 0;
					downloadProgress.setText(progress + "%");
					progress_horizontal.setProgress(progress);
					progress_horizontal.setMax(100);
					download_status_btn.setBackgroundResource(R.drawable.icon_h);
				} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_FAILED) {
					downloading_layout.setVisibility(View.GONE);
					download_finish_layout.setVisibility(View.VISIBLE);
					download_status_icon.setBackgroundResource(R.drawable.icon_f);
					download_finish_text.setText("网络出错啦\n点击重试");
				} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_PAUSED) {
					downloading_layout.setVisibility(View.VISIBLE);
					download_finish_layout.setVisibility(View.GONE);
					downloadState.setVisibility(View.VISIBLE);
					downloadStatus.setText("已暂停");
					if (progress < 0)
						progress = 0;
					downloadProgress.setText(progress + "%");
					download_status_btn.setBackgroundResource(R.drawable.icon_g);
				}
			}

		}

	}

	@Override
	public void onPause() {
		super.onPause();
		MZLog.d("life-cycle", "书架onPause");
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
	}

	public void initTopbarView() {
		if (getActivity() == null || topBarView == null)
			return;
		List<String> item = new ArrayList<String>();
		item.add("书架");
		topBarView.setLeftMenuVisiable(true, R.drawable.topbar_add);
		topBarView.setRightMenuOneVisiable(true, R.drawable.topbar_menu, false);
		topBarView.setTitleItem(item);
		topBarView.setListener(this);
		topBarView.updateTopBarView();
	}

	class TimeComparator implements Comparator<BookShelfModel> {

		@Override
		public int compare(BookShelfModel lhs, BookShelfModel rhs) {
			double time1 = (double) lhs.getModifiedTime();
			double time2 = (double) rhs.getModifiedTime();
			// 降序排列
			if (time1 < time2)
				return 1;
			if (time1 > time2)
				return -1;
			return 0;
		}

	}

	public void updateUI() {
		getData();
		mItemAdapter.notifyDataSetChanged();
	}

	// 删除书架书籍
	public void deleteBooks(DragItem item, int position) {
		if (null == item) {
			return;
		}
		String userId = LoginUser.getpin();
		if (null == item.getMo()) {
			return;
		}
		if (TextUtils.isEmpty(item.getMo().getBookType())) {
			return;
		}
		if (item.getMo().getBookType().equals(BookShelfModel.EBOOK)) {
			EBook ebook = MZBookDatabase.instance.getEBook(item.getMo().getBookid());
			if (null == ebook) {
				return;
			}

			// 先暂停下载 然后继续删除
			LocalBook localBook = LocalBook.getLocalBookByIndex(item.getMo().getBookid());
			if (null != localBook) {
				SettingUtils.getInstance().putBoolean("" + localBook.book_id, false);
			}
			if (null != localBook && localBook.state == LocalBook.STATE_LOADING) {
				MZLog.d("wangguodong", "暂停正在下载的任务");
				DownloadHelper.stopDownload(getActivity(), BookShelfModel.EBOOK, localBook);
			}
			try {
				FileGuider savePath = new FileGuider(FileGuider.SPACE_PRIORITY_EXTERNAL);
				savePath.setImmutable(true);
				savePath.setChildDirName("/epub/" + ebook.bookId);

				File fileDir = new File(savePath.getParentPath());
				MZLog.d("wangguodong", fileDir.getAbsolutePath());

				if (fileDir.exists()) {
					IOUtil.deleteFile(fileDir);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Long[] ebooks = new Long[] { ebook.bookId };
			Integer[] index = new Integer[] { ebook.ebookId };

			MZBookDatabase.instance.deleteEbook(userId, index, ebooks);
			MZLog.d("wangguodong", "删除ebook 成功");
		} else if (item.getMo().getBookType().equals(BookShelfModel.DOCUMENT)) {
			Document doc = MZBookDatabase.instance.getDocument(item.getMo().getBookid());

			LocalDocument localDocument = LocalDocument.getLocalDocument(item.getMo().getBookid(), LoginUser.getpin());
			if (null != localDocument && localDocument.state == LocalBook.STATE_LOADING) {
				MZLog.d("wangguodong", "暂停正在下载的任务");

				// 先暂停下载 然后继续删除
				DownloadHelper.stopDownload(getActivity(), BookShelfModel.DOCUMENT, localDocument);
				SettingUtils.getInstance().putLong("change_time" + localDocument.server_id, -1);
				SettingUtils.getInstance().putBoolean("" + localDocument.server_id, false);
			}
			File fileDir = new File(StoragePath.getDocumentDir(getActivity()), String.valueOf(doc.documentId));
			if (fileDir.exists()) {
				IOUtil.deleteFile(fileDir);
			}

			Integer[] documents = new Integer[] { doc.getDocumentId() };
			MZBookDatabase.instance.deleteDocumentRecord(documents, userId);
			MZLog.d("wangguodong", "删除document 成功");
		} else {
			MZLog.d("wangguodong", "书籍未删除");
		}

	}

	class DelBookTask extends AsyncTask<Integer, Void, Void> {

		private DragItem mItem = null;

		public DelBookTask(DragItem item) {
			mItem = item;
		}

		@Override
		protected Void doInBackground(Integer... params) {

			deleteBooks(mItem, params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			updateRecentLayout();// 更新最近在读
			super.onPostExecute(result);
		}

	}

	class MyFolderView extends View implements OnItemDragAndDropListener, RefreshAble {

		private Context mContext;
		private ViewGroup mPopupLayout;
		private FolderViewContainer mParentView;
		private WindowManager mWinManager;
		private WindowManager.LayoutParams mParams;
		private final int ANIMALTION_TIME = 500;
		private final int HALF_ANIMALTION_TIME = 500;
		private int mSrceenwidth;
		private int mSrceenheigh;

		private ImageView barView;
		private ImageView topView;
		private ImageView bottomView;
		private View mBackgroundView;
		private ImageView barTopView;
		private ImageView topbgView;
		private EditText foldername;
		private LinearLayout editContent;

		private ImageView cover_image;

		private LinearLayout middleview;

		private int offsety = 0;
		private int actionbarHeight = 0;
		private int statusBarHeight = 0;
		private int DEFAULT_FOLDER_HEIGHT = 0; // 文件夹高度为屏幕的2/3
		private boolean mIsOpened = false;
		private int dismissCount = 0;

		private int[] childViewLocation = new int[2];
		private int folderIds;
		private List<BookShelfModel> models;
		// 以下处理拖拽view
		private DragGridLayout mDragGridLayout;
		private ScrollView mDragGridLayoutScrollView;
		private List<DragItem> subMitems = new LinkedList<DragItem>();
		private DragItemAdapter mSubItemAdapter;

		private boolean isNeedSorted = true;

		private Handler mhandler = new Handler() {
			public void handleMessage(android.os.Message msg) {

				if (msg.what == UPDATE_UI_MESSAGE) {
					Bundle bundleData = (Bundle) msg.obj;
					int index = bundleData.getInt("index");
					int status = bundleData.getInt("status");
					int progress = bundleData.getInt("progress");
					updateSubItemView(index, status, progress);
				}

			};
		};

		public void updateSubItemData(int position, boolean isDownloaded) {
			if (subMitems != null && subMitems.size() > position && position > -1) {
				// MZLog.d("wangguodong", "item size:" + mItems.size() +
				// "==positon:"
				// + position);
				DragItem item = subMitems.get(position);
				item.setDownloaded(isDownloaded);
				subMitems.set(position, item);
			}
		}

		public void updateSubItemView(final int index, int download_status, int progress) {// progress
																							// 是百分比整数
																							// 50%
																							// ==50

			if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED) {
				updateSubItemData(index, true);
			} else {
				updateSubItemData(index, false);
			}

			if (mDragGridLayout != null) {
				int visiblePos = mDragGridLayout.getFirstVisiblePosition();
				int offset = index - visiblePos;
				if (offset < 0)
					return;
				View view = mDragGridLayout.getChildAt(offset);
				if (view != null) {
					// 添加下载封面进度
					FrameLayout downloadState = ViewHolder.get(view, R.id.download_state);
					TextView downloadStatus = ViewHolder.get(view, R.id.download_status);
					TextView downloadProgress = ViewHolder.get(view, R.id.download_progress);

					if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED) {
						downloadState.setVisibility(View.GONE);
						downloadStatus.setText("");
						downloadProgress.setText("");
					} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING) {
						downloadState.setVisibility(View.VISIBLE);
						downloadStatus.setText("下载中...");
						// downloadProgress.setVisibility(view.VISIBLE);
						downloadProgress.setText(progress + "%");
					} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_FAILED) {
						downloadState.setVisibility(View.VISIBLE);
						downloadStatus.setText("下载失败");
						downloadProgress.setVisibility(view.GONE);
					} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_PAUSED) {
						downloadState.setVisibility(View.VISIBLE);
						downloadStatus.setText("已暂停");
						// downloadProgress.setVisibility(view.VISIBLE);
						downloadProgress.setText(progress + "%");
					}
				}
			}
		}

		public MyFolderView(int actionbarheight, View childView, int folderid, Context context, View backgroundView,
				boolean isneedSorted) {
			super(context);
			// mParentView.removeAllViews();
			mContext = context;
			mSrceenwidth = backgroundView.getWidth();
			mSrceenheigh = backgroundView.getHeight();
			mBackgroundView = backgroundView;
			folderIds = folderid;
			childView.getLocationInWindow(childViewLocation);
			isNeedSorted = isneedSorted;
			// offsety = childView.getHeight() + childViewLocation[1];//
			// childViewLocation[1]表示子view的左上角顶点y坐标
			offsety = mSrceenheigh / 3;
			MZLog.d("wangguodong", "+++++++++++++当前child点击的下部坐标" + offsety);
			actionbarHeight = actionbarheight;
			statusBarHeight = getStatusBarHeight();
			DEFAULT_FOLDER_HEIGHT = 2 * mSrceenheigh / 3;
			prepareLayout(backgroundView);

		}

		public void updateSubUI() {
			subMitems.clear();
			String userId = LoginUser.getpin();
			models = MZBookDatabase.instance.getBooksInFolder(folderIds, -1, userId);
			Collections.sort(models, new TimeComparator());
			for (int r = 0; r < models.size(); r++) {

				if (models.get(r).getDownload_state() == LocalBook.STATE_FAILED
						|| models.get(r).getDownload_state() == LocalBook.STATE_LOAD_PAUSED
						|| models.get(r).getDownload_state() == LocalBook.STATE_LOADING
						|| models.get(r).getDownload_state() == LocalBook.STATE_LOAD_FAILED) {
					DragItem item = new DragItem(models.get(r), false, false, -1, false);
					item.setSelected(selectedListIsExits(item));
					subMitems.add(item);
				} else {
					DragItem item = new DragItem(models.get(r), false, false, -1, true);
					item.setSelected(selectedListIsExits(item));
					subMitems.add(item);
				}

			}
			mSubItemAdapter.notifyDataSetChanged();
		}

		public void prepareLayout(View backgroundView) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mPopupLayout = (LinearLayout) inflater.inflate(R.layout.dragview_folder_grid, null);
			mWinManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
			// 以下处理拖拽view
			subMitems.clear();
			String userId = LoginUser.getpin();
			models = MZBookDatabase.instance.getBooksInFolder(folderIds, -1, userId);
			Collections.sort(models, new TimeComparator());

			for (int r = 0; r < models.size(); r++) {

				if (models.get(r).getDownload_state() == LocalBook.STATE_FAILED
						|| models.get(r).getDownload_state() == LocalBook.STATE_LOAD_PAUSED
						|| models.get(r).getDownload_state() == LocalBook.STATE_LOADING
						|| models.get(r).getDownload_state() == LocalBook.STATE_LOAD_FAILED) {

					DragItem item = new DragItem(models.get(r), false, false, -1, false);
					item.setSelected(selectedListIsExits(item));
					subMitems.add(item);
				} else {

					DragItem item = new DragItem(models.get(r), false, false, -1, true);
					item.setSelected(selectedListIsExits(item));
					subMitems.add(item);
				}

			}

			mDragGridLayout = (DragGridLayout) mPopupLayout.findViewById(R.id.folderview);
			mDragGridLayout.setDragOutSupport(true);
			mDragGridLayout.setOnItemDragAndDropListener(this);
			mDragGridLayout.setDragAndDropMergeEnable(false);
			mSubItemAdapter = new DragItemAdapter(mContext, subMitems);
			mDragGridLayout.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
					if (isSelected) {
						DragItem data = subMitems.get(position);
						boolean exits = false;
						int index = 0;
						for (int j = 0; j < mSelectedList.size(); j++) {
							index = j;
							DragItem sdata = mSelectedList.get(j);
							long dec_bookid = data.getMo().getServerid();
							long src_bookid = sdata.getMo().getServerid();
							if ((0 == dec_bookid) && (0 == src_bookid)) {
								String dec_bookcover = data.getMo().getBookCover();
								String src_bookcover = sdata.getMo().getBookCover();
								if (!TextUtils.isEmpty(dec_bookcover) && !TextUtils.isEmpty(src_bookcover)) {
									if (dec_bookcover.equals(src_bookcover)) {
										exits = true;
										break;
									}
								}
							} else {
								if (dec_bookid == src_bookid) {
									exits = true;
									break;
								}
							}
						}

						RelativeLayout mSelectedLayout = (RelativeLayout) view.findViewById(R.id.mSelectedLayout);
						ImageView book_selected_cover = (ImageView) view.findViewById(R.id.book_selected_cover);
						if (exits) {
							mSelectedList.remove(index);
							if (null != mSelectedLayout) {
								mSelectedLayout.setVisibility(View.GONE);
								book_selected_cover.setVisibility(View.GONE);
							}
						} else {
							mSelectedList.add(data);
							if (null != mSelectedLayout) {
								mSelectedLayout.setVisibility(View.VISIBLE);
								book_selected_cover.setVisibility(View.VISIBLE);
							}
						}

						updateToolBarStatus();
						return;
					}

					if (!subMitems.get(position).isDownloaded()) {

						DragItem item = subMitems.get(position);
						BookShelfModel model = item.getMo();

						DownloadedAble downloadedAble = null;

						if (model.getBookType().equals(BookShelfModel.EBOOK)) {
							downloadedAble = LocalBook.getLocalBookByIndex(model.getBookid());

						} else if (model.getBookType().equals(BookShelfModel.DOCUMENT)) {
							downloadedAble = MZBookDatabase.instance.getLocalDocument(model.getBookid());
							MZLog.d("wangguodong", "点击document");
						} else {
							return;
						}

						if (model.getDownload_state() == LocalBook.STATE_FAILED
								|| model.getDownload_state() == LocalBook.STATE_LOAD_FAILED) {

							MZLog.d("wangguodong", "重新开始下载书籍...");
							model.setDownload_state(LocalBook.STATE_LOADING);
							item.setMo(model);
							subMitems.set(position, item);

							Bundle bundle = new Bundle();
							bundle.putInt("index", position);
							bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
							bundle.putInt("progress", 0);
							sendSubPageMessage(bundle);
							DownloadHelper.restartDownload(getActivity(), model.getBookType(), downloadedAble);

						} else if (model.getDownload_state() == LocalBook.STATE_LOAD_PAUSED) {

							MZLog.d("wangguodong", "继续下载书籍...");
							model.setDownload_state(LocalBook.STATE_LOADING);
							item.setMo(model);
							subMitems.set(position, item);

							Bundle bundle = new Bundle();
							bundle.putInt("index", position);
							bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
							bundle.putInt("progress", -1);
							sendSubPageMessage(bundle);
							DownloadHelper.resumeDownload(getActivity(), model.getBookType(), downloadedAble);
						} else if (model.getDownload_state() == LocalBook.STATE_LOADING) {

							MZLog.d("wangguodong", "暂停下载书籍...");
							model.setDownload_state(LocalBook.STATE_LOAD_PAUSED);
							item.setMo(model);
							subMitems.set(position, item);

							Bundle bundle = new Bundle();
							bundle.putInt("index", position);
							bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_PAUSED);
							bundle.putInt("progress", -1);
							sendSubPageMessage(bundle);
							DownloadHelper.stopDownload(getActivity(), model.getBookType(), downloadedAble);
						} else if (model.getDownload_state() == LocalBook.STATE_LOADED
								|| model.getDownload_state() == LocalBook.STATE_LOAD_READING) {

							model.setDownload_state(LocalBook.STATE_LOADED);
							item.setMo(model);
							subMitems.set(position, item);

							Bundle bundle = new Bundle();
							bundle.putInt("index", position);
							bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED);
							bundle.putInt("progress", -1);
							sendSubPageMessage(bundle);
						} else {
							Toast.makeText(getActivity(), "无法继续下载，书籍有问题...", Toast.LENGTH_LONG).show();
						}

						return;
					}

					if (!subMitems.get(position).isFolder()) {
						if (!isOpenBook) {
							isOpenBook = true;
							BookShelfModel model = subMitems.get(position).getMo();
							if (model.getBookType().equals("ebook")) {
								EBook ebook = MZBookDatabase.instance.getEBook(model.getBookid());
								OpenBookHelper.openEBook(getActivity(), ebook.bookId);

							} else if (model.getBookType().equals(BookShelfModel.DOCUMENT)) {
								Document doc = MZBookDatabase.instance.getDocument(model.getBookid());
								OpenBookHelper.openDocument(getActivity(), doc.documentId);

							}
						}
					}
				}

			});

			mDragGridLayout.setAdapter(mSubItemAdapter);

			// 以上处理拖拽view

			barView = (ImageView) mPopupLayout.findViewById(R.id.barview);
			topView = (ImageView) mPopupLayout.findViewById(R.id.topview);
			bottomView = (ImageView) mPopupLayout.findViewById(R.id.bottomview);
			// middleview=(LinearLayout)
			// mPopupLayout.findViewById(R.id.middleView);
			foldername = (EditText) mPopupLayout.findViewById(R.id.foldername);
			cover_image = (ImageView) mPopupLayout.findViewById(R.id.cover_image);
			editContent = (LinearLayout) mPopupLayout.findViewById(R.id.edit_content);

			String folderNameString = MZBookDatabase.instance.getFolder(folderIds).getFolderName();
			foldername.setText(folderNameString);
			foldername.setSelection(folderNameString.length());

			foldername.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					if (!hasFocus) {
						saveFolderName(folderIds, foldername.getText().toString());
					}

				}
			});

			foldername.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
					if (arg1 == EditorInfo.IME_ACTION_DONE) {
						saveFolderName(folderIds, foldername.getText().toString());
						return true;
					}

					return false;
				}
			});

			// 截当前view背景图
			backgroundView.setDrawingCacheEnabled(true);
			Bitmap srceen = backgroundView.getDrawingCache();

			// 截图控件状态栏和actionbar
			Bitmap bar = Bitmap.createBitmap(srceen, 0, statusBarHeight, mSrceenwidth, actionbarHeight);
			barView.setImageBitmap(bar);

			// top
			Bitmap top = Bitmap.createBitmap(srceen, 0, statusBarHeight + actionbarHeight, mSrceenwidth,
					offsety - statusBarHeight - actionbarHeight);
			MZLog.d("wangguodong",
					"+++++++++++++当前上半部分图片高度statusBarHeight:" + statusBarHeight + "actionbarHeight:" + actionbarHeight);
			topView.setImageBitmap(top);
			// 截图控件以下部分

			if (UiStaticMethod.hasSmartBar()) {
				Bitmap bottom = Bitmap.createBitmap(srceen, 0, offsety, mSrceenwidth,
						mSrceenheigh - offsety - dip2px(mContext, 48));
				bottomView.setImageBitmap(bottom);
			} else {
				Bitmap bottom = Bitmap.createBitmap(srceen, 0, offsety, mSrceenwidth, mSrceenheigh - offsety);
				bottomView.setImageBitmap(bottom);
			}

			mParams = new WindowManager.LayoutParams();
			mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
			mParams.format = PixelFormat.RGBA_8888;
			// mParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
			mParams.gravity = Gravity.CENTER;
			mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
			mParams.height = WindowManager.LayoutParams.MATCH_PARENT;

			// 最底层的top部分
			mParentView = new FolderViewContainer(mContext);
			mParentView.setBackgroundColor(Color.WHITE);
			mParentView.addView(mPopupLayout);
			mWinManager.addView(mParentView, mParams);

			// 顶部ActionBar层
			barTopView = new ImageView(mContext);
			barTopView.setImageBitmap(bar);
			// addNewBarView();

			startOpenAnimation();

		}

		public void saveFolderName(int id, String name) {
			MZBookDatabase.instance.updateFolder(id, name);

		}

		public void addNewBarView() {
			WindowManager.LayoutParams params = new WindowManager.LayoutParams();
			params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
			params.format = PixelFormat.RGBA_8888;
			params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			params.gravity = Gravity.TOP;
			params.width = WindowManager.LayoutParams.MATCH_PARENT;
			params.height = WindowManager.LayoutParams.WRAP_CONTENT;
			mWinManager.addView(barTopView, params);

		}

		private void startOpenAnimation() {

			Animation fadein = new AlphaAnimation(0, 1.0f);
			fadein.setDuration(HALF_ANIMALTION_TIME);
			fadein.setInterpolator(new DecelerateInterpolator());
			fadein.setFillAfter(true);
			cover_image.startAnimation(fadein);
			editContent.startAnimation(fadein);

			Animation anim = new TranslateAnimation(0, 0, 0, mSrceenheigh - offsety + statusBarHeight);
			anim.setDuration(HALF_ANIMALTION_TIME);
			anim.setInterpolator(new DecelerateInterpolator());
			anim.setFillAfter(true);
			anim.setAnimationListener(mOpenAnimationListener);
			bottomView.startAnimation(anim);
		}

		// 获得状态栏高度
		public int getStatusBarHeight() {
			Class<?> c = null;
			Object obj = null;
			java.lang.reflect.Field field = null;
			int x = 0;
			int statusBarHeight = 0;
			try {
				c = Class.forName("com.android.internal.R$dimen");
				obj = c.newInstance();
				field = c.getField("status_bar_height");
				x = Integer.parseInt(field.get(obj).toString());
				statusBarHeight = mContext.getResources().getDimensionPixelSize(x);
				return statusBarHeight;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return statusBarHeight;
		}

		// dip to px
		public int dip2px(Context context, float dipValue) {
			final float scale = context.getResources().getDisplayMetrics().density;
			return (int) (dipValue * scale + 0.5f);
		}

		public boolean removeFloderView() {
			if (mWinManager != null) {

				mWinManager.removeView(mParentView);
				return true;
			} else {
				return false;
			}
		}

		public void dismiss() {

			if (!mIsOpened) {
				return;
			}
			foldername.setFocusable(false);

			Animation fadeout = new AlphaAnimation(1.0f, 0);
			fadeout.setDuration(HALF_ANIMALTION_TIME);
			fadeout.setInterpolator(new DecelerateInterpolator());
			fadeout.setFillAfter(true);
			cover_image.startAnimation(fadeout);
			editContent.startAnimation(fadeout);

			Animation anim = new TranslateAnimation(0, 0, mSrceenheigh - offsety, 0);
			anim.setDuration(ANIMALTION_TIME);
			anim.setInterpolator(new DecelerateInterpolator());
			anim.setFillAfter(true);
			anim.setAnimationListener(mClosedAnimationListener);
			bottomView.startAnimation(anim);
		}

		class FolderViewContainer extends FrameLayout {

			long lasttime = 0;
			boolean isvalid = false;

			public FolderViewContainer(Context context) {
				super(context);
			}

			@Override
			public boolean dispatchKeyEvent(KeyEvent event) {

				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getRepeatCount() == 0) {
					dismiss();
					return true;
				}
				return super.dispatchKeyEvent(event);
			}

			@Override
			public boolean onTouchEvent(MotionEvent event) {

				final int y = (int) event.getY();

				if (System.currentTimeMillis() - lasttime > ANIMALTION_TIME) {
					isvalid = true;
				} else {
					isvalid = false;
				}
				lasttime = System.currentTimeMillis();
				if ((event.getAction() == MotionEvent.ACTION_DOWN) && isvalid && y < offsety) {
					dismiss();
					return true;
				} else {
					return super.onTouchEvent(event);
				}

			}

		}

		private Animation.AnimationListener mOpenAnimationListener = new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mIsOpened = true;
				dismissCount = 0;
				DownloadService.setRefreshAbler(MyFolderView.this, DownloadedAble.TYPE_BOOK);
				DownloadService.setRefreshAbler(MyFolderView.this, DownloadedAble.TYPE_DOCUMENT);
			}
		};

		private Animation.AnimationListener mClosedAnimationListener = new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// 更新fragment
				updateUI();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {

				removeFloderView();
				mBackgroundView.setDrawingCacheEnabled(false);
				mIsOpened = false;

			}
		};

		class TimeComparator implements Comparator<BookShelfModel> {

			@Override
			public int compare(BookShelfModel lhs, BookShelfModel rhs) {
				double time1 = (double) lhs.getModifiedTime();
				double time2 = (double) rhs.getModifiedTime();
				// 降序排列
				if (time1 < time2)
					return 1;
				if (time1 > time2)
					return -1;
				return 0;
			}

		}

		public void sendSubPageMessage(Bundle bundle) {
			Message msg = new Message();
			msg.what = UPDATE_UI_MESSAGE;
			msg.obj = bundle;
			mhandler.sendMessage(msg);
		}

		// 获得指定type id 的item 的位置
		public int getSubPagePositionByTypeAndId(String type, int id) {
			if (subMitems == null)
				return -1;
			else {
				for (int i = 0; i < subMitems.size(); i++) {

					BookShelfModel model = subMitems.get(i).getMo();

					if (model.getBookType().equals(type)) {
						if (model.getBookid() == id) {
							return i;
						}
					}

				}
				return -1;
			}
		}

		@Override
		public void refresh(DownloadedAble DownloadAble) {
			int index = -1;
			if (DownloadAble.getType() == DownloadedAble.TYPE_BOOK) {
				LocalBook localBook = (LocalBook) DownloadAble;

				index = getSubPagePositionByTypeAndId(BookShelfModel.EBOOK, localBook._id);
				int state = DownloadStateManager.getLocalBookState(localBook);
				int progress = 0;
				if (localBook.size > 0)
					progress = (int) (localBook.progress * 100 / localBook.size);

				Bundle bundle = new Bundle();
				bundle.putInt("index", index);

				if (state == DownloadStateManager.STATE_LOADED) {

					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED);
					bundle.putInt("progress", -1);
					sendSubPageMessage(bundle);

				} else if (state == DownloadStateManager.STATE_LOADING) {
					if (localBook.state == LocalBook.STATE_LOADING || localBook.state == LocalBook.STATE_LOAD_READY) {
						bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
						bundle.putInt("progress", progress);
						sendSubPageMessage(bundle);
					}
					if (localBook.state == LocalBook.STATE_LOAD_PAUSED) {

						bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_PAUSED);
						bundle.putInt("progress", progress);
						sendSubPageMessage(bundle);
					}
				} else {
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_FAILED);
					bundle.putInt("progress", 0);
					sendSubPageMessage(bundle);
				}

			} else if (DownloadAble.getType() == DownloadedAble.TYPE_DOCUMENT) {
				LocalDocument localBook = (LocalDocument) DownloadAble;
				index = getPositionByTypeAndId(BookShelfModel.DOCUMENT, localBook._id);
				int state = DownloadStateManager.getLocalDocumentState(localBook);

				Bundle bundle = new Bundle();
				bundle.putInt("index", index);

				int progress = 0;
				if (localBook.size > 0)
					progress = (int) (localBook.progress * 100 / localBook.size);

				if (state == DownloadStateManager.STATE_LOADED) {
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED);
					bundle.putInt("progress", -1);
					sendSubPageMessage(bundle);

				} else if (state == DownloadStateManager.STATE_LOADING) {
					if (localBook.state == LocalBook.STATE_LOADING || localBook.state == LocalBook.STATE_LOAD_READY) {
						bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
						bundle.putInt("progress", progress);
						sendSubPageMessage(bundle);
					}
					if (localBook.state == LocalBook.STATE_LOAD_PAUSED) {
						bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_PAUSED);
						bundle.putInt("progress", progress);
						sendSubPageMessage(bundle);
					}
				} else {
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_FAILED);
					bundle.putInt("progress", 0);
					sendSubPageMessage(bundle);
				}
			}
		}

		@Override
		public int getType() {
			return 0;
		}

		@Override
		public void refreshDownloadCache() {

		}

		@Override
		public boolean isFolder(int position) {
			return false;
		}

		@Override
		public void onItemSwap(int from, int to) {

			if (from != to) {
				MZLog.d("wangguodong", "拖动的是文件夹，或者被排序了 移动位置");

				DragItem from_item = subMitems.get(from);
				DragItem target_item = subMitems.get(to);

				if (to == 0) {
					BookShelfModel froModel = from_item.getMo();
					froModel.setModifiedTime(target_item.getMo().getModifiedTime() + 1);
					models.set(from, froModel);
					Collections.sort(models, new TimeComparator());
					MZBookDatabase.instance.updateBookshelfTime(froModel);
				} else if (to == subMitems.size() - 1) {

					BookShelfModel froModel = from_item.getMo();
					froModel.setModifiedTime(target_item.getMo().getModifiedTime() - 1);
					models.set(from, froModel);
					Collections.sort(models, new TimeComparator());
					MZBookDatabase.instance.updateBookshelfTime(froModel);
				} else {

					DragItem befour_item = subMitems.get(to - 1);
					DragItem after_item = subMitems.get(to);

					BookShelfModel targetBefour = befour_item.getMo();
					BookShelfModel targetAfter = after_item.getMo();

					double centerTime = (targetBefour.getModifiedTime() + targetAfter.getModifiedTime()) / 2.0f;

					MZLog.d("wangguodong", "排序后的中间时间" + centerTime);

					BookShelfModel froModel = from_item.getMo();
					froModel.setModifiedTime(centerTime);

					models.set(from, froModel);
					Collections.sort(models, new TimeComparator());
					MZBookDatabase.instance.updateBookshelfTime(froModel);
				}
				subMitems.add(to, subMitems.remove(from));
			}
			mSubItemAdapter.notifyDataSetChanged();

		}

		@Override
		public void onItemMergeToFolder(int from, int to) {

		}

		@Override
		public void onItemMoveToFolder(int from, int to) {

		}

		@Override
		public int getRightPosition(int position) {
			return 0;
		}

		@Override
		public void onItemMoveEnd(int from) {

		}

		@Override
		public void onDragOutLayout(int from) {
			dismissCount++;
			if (dismissCount < 2) {
				dismiss();
				MZLog.d("wangguodong", "移出文件，重新添加到书架");
				BookShelfModel fromModel = models.get(from);
				fromModel.setBelongDirId(-1);
				fromModel.setModifiedTime(System.currentTimeMillis());
				MZBookDatabase.instance.updateBookshelfFolder(fromModel);

			}
		}

		@Override
		public void onDragToDelBookView() {

		}

		@Override
		public void onDragOutDelBookView() {

		}
	}

	/**
	 * 书架顶部标题栏弹出框里边的菜单被点击
	 */
	@Override
	public void onPopupWindowItemClick(String type, int position) {
		// 标题栏右侧菜单
		if (type.equals(TYPE_RIGHT_MENU)) {
			switch (position) {
			case 0:// 整理
				startActivity(new Intent(getActivity(), OrderingBookCaseActivity.class));
				TalkingDataUtil.onBookShelfEvent(getActivity(), "整理");
				break;
			case 1:// 搜索
					// 搜索
				Intent intent1 = new Intent(getActivity(), BookShelfSearchActivity.class);
				intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent1);
				TalkingDataUtil.onBookShelfEvent(getActivity(), "搜索");
				break;
			}
		} /* 左侧菜单 */else {
			switch (position) {
			case 0:// 已购
				TalkingDataUtil.onBookShelfEvent(getActivity(), "已购");
				ActivityUtils.startActivity(getActivity(), new Intent(getActivity(), BookcaseCloudActivity.class));
				break;
			case 1:// 畅读
				TalkingDataUtil.onBookShelfEvent(getActivity(), "畅读");
				ActivityUtils.startActivity(getActivity(), new Intent(getActivity(), ChangDuActivity.class));
				break;
			case 2:// 借阅
				TalkingDataUtil.onBookShelfEvent(getActivity(), "借阅");
				ActivityUtils.startActivity(getActivity(), new Intent(getActivity(), BorrowBookMainActivity.class));
				break;
			case 3:// 云盘
				TalkingDataUtil.onBookShelfEvent(getActivity(), "云盘");
				ActivityUtils.startActivity(getActivity(), new Intent(getActivity(), BookcaseCloudDiskActivity.class));
				break;
			case 4:// 导入本地图书
				TalkingDataUtil.onBookShelfEvent(getActivity(), "导入本地图书");
				Intent intent = new Intent(FileBrowserActivity.INTENT_ACTION_SELECT_FILE, null, getActivity(),
						FileBrowserActivity.class);
				String bookpath = LocalUserSetting.getBookPath(getActivity());
				intent.putExtra(FileBrowserActivity.filterExtension, new String[] { "epub", "pdf" });
				intent.putExtra(FileBrowserActivity.startDirectoryParameter, bookpath);
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(R.anim.fade, R.anim.hold);
				break;
			case 5:// 最近在读
				startActivity(new Intent(getActivity(), BookcaseOthersReadingActivity.class));
				break;
			}
		}
	}

	/**
	 * 标题栏左侧按钮被点击
	 */
	@Override
	public void onLeftMenuClick() {
		if (leftPopupWindow != null) {
			leftPopupWindow.show(topBarView);
			MZLog.d("wangguodong", "xxxxxxxxxxxx");
			if (leftPopupWindow.isShowing()) {
				// 更换左侧图标为关闭
				topBarView.setLeftMenuVisiable(true, R.drawable.topbar_add_selected);
				// 隐藏右侧图标
				topBarView.setRightMenuOneVisiable(false, R.drawable.topbar_menu, false);
			}
			TalkingDataUtil.onBookShelfEvent(getActivity(), "添加_PV");
		}
	}

	/**
	 * 标题栏右侧第一个按钮被点击
	 */
	@Override
	public void onRightMenuOneClick() {
		if (rightPopupWindow != null) {
			rightPopupWindow.show(topBarView);
			if (rightPopupWindow.isShowing()) {
				// 左侧按钮隐藏
				topBarView.setLeftMenuVisiable(false, R.drawable.topbar_add_selected);
				// 点击的按钮显示“X”
				topBarView.setRightMenuOneVisiable(true, R.drawable.topbar_add_selected, false);
			}
			TalkingDataUtil.onBookShelfEvent(getActivity(), "整理_PV");
		}

	}

	@Override
	public void onRightMenuTwoClick() {

	}

	@Override
	public void onCenterMenuItemClick(int position) {

	}

	/**
	 * 下载更新回调
	 */
	@Override
	public void refresh(DownloadedAble DownloadAble) {

		int index = -1;
		// 下载的是否是电子书
		if (DownloadAble.getType() == DownloadedAble.TYPE_BOOK) {
			LocalBook localBook = (LocalBook) DownloadAble;
			// 获取下载的电子书的位置
			index = getPositionByTypeAndId(BookShelfModel.EBOOK, localBook._id);
			int state = DownloadStateManager.getLocalBookState(localBook);
			int progress = 0;
			if (localBook.size > 0)// 计算下载进度百分比
				progress = (int) (localBook.progress * 100 / localBook.size);

			Bundle bundle = new Bundle();
			bundle.putInt("index", index);
			bundle.putString("bookid", "" + localBook.book_id);

			// 下载完成
			if (state == DownloadStateManager.STATE_LOADED) {
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED);
				bundle.putInt("progress", -1);
				// 更新UI
				sendMessage(bundle);

			} else if (state == DownloadStateManager.STATE_LOADING) {// 下载中
				if (localBook.state == LocalBook.STATE_LOADING || localBook.state == LocalBook.STATE_LOAD_READY) {
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
					bundle.putInt("progress", progress);
					// 更新UI
					sendMessage(bundle);
				}
				if (localBook.state == LocalBook.STATE_LOAD_PAUSED) {
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_PAUSED);
					bundle.putInt("progress", progress);
					// 更新UI
					sendMessage(bundle);
				}
			} else {
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_FAILED);
				bundle.putInt("progress", 0);
				// 更新UI
				sendMessage(bundle);
			}

		} else if (DownloadAble.getType() == DownloadedAble.TYPE_DOCUMENT) {
			LocalDocument localBook = (LocalDocument) DownloadAble;
			index = getPositionByTypeAndId(BookShelfModel.DOCUMENT, localBook._id);
			int state = DownloadStateManager.getLocalDocumentState(localBook);

			Bundle bundle = new Bundle();
			bundle.putInt("index", index);
			bundle.putString("bookid", "" + localBook.server_id);

			int progress = 0;
			if (localBook.size > 0)
				progress = (int) (localBook.progress * 100 / localBook.size);

			if (state == DownloadStateManager.STATE_LOADED) {
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED);
				bundle.putInt("progress", -1);
				sendMessage(bundle);

			} else if (state == DownloadStateManager.STATE_LOADING) {
				if (localBook.state == LocalBook.STATE_LOADING || localBook.state == LocalBook.STATE_LOAD_READY) {
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
					bundle.putInt("progress", progress);
					sendMessage(bundle);
				}
				if (localBook.state == LocalBook.STATE_LOAD_PAUSED) {
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_PAUSED);
					bundle.putInt("progress", progress);
					sendMessage(bundle);
				}
			} else {
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_FAILED);
				bundle.putInt("progress", 0);
				sendMessage(bundle);
			}
		}
	}

	@Override
	public int getType() {
		return 0;
	}

	@Override
	public void refreshDownloadCache() {

	}

	@Override
	public void onPopupWindowSubmenuItemCheck(String type, int checkid) {

		if (type.equals(TYPE_RIGHT_MENU)) {

			switch (checkid) {

			case 0:// 封面模式

				LocalUserSetting.saveBookShelfModel(getActivity(), "FengMian");

				((LauncherActivity) getActivity()).showMainLayoutView(1);
				if (rightPopupWindow != null) {
					rightPopupWindow.dismiss();
				}

				TalkingDataUtil.onBookShelfEvent(getActivity(), "封面模式");
				break;
			case 1:// 按名称排序

				LocalUserSetting.saveBookShelfModel(getActivity(), "Name");
				((LauncherActivity) getActivity()).showMainLayoutView(1);
				if (rightPopupWindow != null) {
					rightPopupWindow.dismiss();
				}

				TalkingDataUtil.onBookShelfEvent(getActivity(), "按名称排序");

				break;
			case 2:// 按时间排序
				LocalUserSetting.saveBookShelfModel(getActivity(), "Time");

				((LauncherActivity) getActivity()).showMainLayoutView(1);

				if (rightPopupWindow != null) {
					rightPopupWindow.dismiss();
				}

				TalkingDataUtil.onBookShelfEvent(getActivity(), "按时间排序");
				break;
			}

		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {

		setCurrentDot(arg0);

	}

	@Override
	public boolean isFolder(int position) {
		if (position < 0 || position > (mItems.size() - 1))
			return false;
		DragItem item = mItems.get(position);
		if (item.isFolder())
			return true;
		return false;
	}

	@Override
	public void onSwapItem(int from, int to) {
		if (from < 0 || to < 0 || from > mItems.size() - 1 || to > mItems.size() - 1)
			return;

		DragItem temp = mItems.get(from);
		if (from < to) {
			for (int i = from; i < to; i++) {
				double fromTime = mItems.get(i).getMo().getModifiedTime();
				double toTime = mItems.get(i + 1).getMo().getModifiedTime();
				mItems.get(i).getMo().setModifiedTime(toTime);
				mItems.get(i + 1).getMo().setModifiedTime(fromTime);
				MZBookDatabase.instance.updateBookshelfTime(mItems.get(i).getMo());
				MZBookDatabase.instance.updateBookshelfTime(mItems.get(i + 1).getMo());

				Collections.swap(mItems, i, i + 1);
			}
		} else if (from > to) {
			for (int i = from; i > to; i--) {
				double fromTime = mItems.get(i).getMo().getModifiedTime();
				double toTime = mItems.get(i - 1).getMo().getModifiedTime();
				mItems.get(i).getMo().setModifiedTime(toTime);
				mItems.get(i - 1).getMo().setModifiedTime(fromTime);
				MZBookDatabase.instance.updateBookshelfTime(mItems.get(i).getMo());
				MZBookDatabase.instance.updateBookshelfTime(mItems.get(i - 1).getMo());

				Collections.swap(mItems, i, i - 1);
			}
		}

		mItems.set(to, temp);
	}

	@Override
	public void onItemMergeToFolder(int from, int to) {
		if (from < 0 || to < 0 || from > mItems.size() - 1 || to > mItems.size() - 1)
			return;

		int folderid = MZBookDatabase.instance.createFolder(mItems.get(to).getMo().getModifiedTime(),
				LoginUser.getpin());
		BookShelfModel fromModel = mItems.get(from).getMo();
		fromModel.setBelongDirId(folderid);
		MZBookDatabase.instance.updateBookshelfFolder(fromModel);

		BookShelfModel toModel = mItems.get(to).getMo();
		toModel.setBelongDirId(folderid);
		MZBookDatabase.instance.updateBookshelfFolder(toModel);
		mItems.remove(from);

		getData();
		mItemAdapter.setHideItem(-1);
	}

	@Override
	public void onItemMoveToFolder(int from, int to) {
		if (from < 0 || to < 0 || from > (mItems.size() - 1) || to > (mItems.size() - 1))
			return;

		if (mItems.get(to).isFolder()) {
			BookShelfModel fromModel = mItems.get(from).getMo();
			int folderid = mItems.get(to).getMo().getBookid();
			mItems.remove(from);

			if (folderid == 0)
				return;
			fromModel.setBelongDirId(folderid);
			MZBookDatabase.instance.updateBookshelfFolder(fromModel);

			getData();
			mItemAdapter.setHideItem(-1);
		}

	}

	public void deleteFoler(boolean isDelete, int folderid) {

		String userId = LoginUser.getpin();
		List<BookShelfModel> list = MZBookDatabase.instance.getBooksInFolder(folderid, -1, LoginUser.getpin());
		List<EBook> ebookidList = new ArrayList<EBook>();
		List<Integer> documentidList = new ArrayList<Integer>();

		if (!isDelete)// 只删除文件夹 事情已经完成
		{
			for (int i = 0; i < list.size(); i++) {
				BookShelfModel model = list.get(i);
				if (model.getBelongDirId() != -1) {
					model.setBelongDirId(-1);
					model.setModifiedTime(System.currentTimeMillis());
					MZBookDatabase.instance.updateBookshelfFolder(model);
				}
			}

			updateBookCase();

			return;
		}

		// 以下 删除 书籍记录和 文件

		for (int itemPosition = 0; itemPosition < list.size(); itemPosition++) {
			if (list.get(itemPosition).getBookType().equals("ebook")) {

				EBook ebook = MZBookDatabase.instance.getEBook(list.get(itemPosition).getBookid());

				// 先暂停下载 然后继续删除
				LocalBook localBook = LocalBook.getLocalBookByIndex(list.get(itemPosition).getBookid());
				if (null != localBook && localBook.state == LocalBook.STATE_LOADING) {
					MZLog.d("wangguodong", "暂停正在下载的任务");
					DownloadHelper.stopDownload(getActivity(), BookShelfModel.EBOOK, localBook);
				}

				ebookidList.add(ebook);
				try {
					FileGuider savePath = new FileGuider(FileGuider.SPACE_PRIORITY_EXTERNAL);
					savePath.setImmutable(true);
					savePath.setChildDirName("/epub/" + ebook.bookId);

					File fileDir = new File(savePath.getParentPath());

					MZLog.d("wangguodong", fileDir.getAbsolutePath());

					if (fileDir.exists()) {
						IOUtil.deleteFile(fileDir);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (list.get(itemPosition).getBookType().equals("document")) {
				Document doc = MZBookDatabase.instance.getDocument(list.get(itemPosition).getBookid());

				LocalDocument localDocument = LocalDocument.getLocalDocument(list.get(itemPosition).getBookid(),
						LoginUser.getpin());

				if (null != localDocument && localDocument.state == LocalBook.STATE_LOADING) {
					MZLog.d("wangguodong", "暂停正在下载的任务");

					// 先暂停下载 然后继续删除
					DownloadHelper.stopDownload(getActivity(), BookShelfModel.DOCUMENT, localDocument);

				}

				documentidList.add(doc.documentId);

				File fileDir = new File(StoragePath.getDocumentDir(getActivity()), String.valueOf(doc.documentId));
				if (fileDir.exists()) {
					IOUtil.deleteFile(fileDir);
				}

			}

		}

		if (ebookidList.size() > 0) {
			Long[] ebooks = new Long[ebookidList.size()];
			for (int i = 0; i < ebookidList.size(); i++)

			{
				if (ebookidList.get(i) != null)
					ebooks[i] = ebookidList.get(i).bookId;
			}

			Integer[] index = new Integer[ebookidList.size()];
			for (int i = 0; i < ebookidList.size(); i++)

			{
				if (ebookidList.get(i) != null)
					index[i] = ebookidList.get(i).ebookId;
			}

			MZLog.d("wangguodong", "删除ebook++++++++++");

			MZBookDatabase.instance.deleteEbook(userId, index, ebooks);

		}

		if (documentidList.size() > 0) {
			Integer[] documents = new Integer[documentidList.size()];
			for (int i = 0; i < documentidList.size(); i++)

			{
				documents[i] = Integer.parseInt(documentidList.get(i).toString());
			}

			MZLog.d("wangguodong", "删除document++++++++++");
			MZBookDatabase.instance.deleteDocumentRecord(documents, userId);

		}
		updateBookCase();
	}

	/**
	 * 获取当前编辑状态
	 * 
	 * @return
	 */
	public boolean getIsSelected() {
		return isSelected;
	}

	/**
	 * 隐藏整理模式状态栏
	 */
	public void hideTooBar() {
		if (isSelected) {
			isSelected = false;
			mSelectedList.clear();
			mBookShelfToolBar.hide();
			mItemAdapter.notifyDataSetChanged();
			if (null != foldView) {
				if (View.VISIBLE == foldView.getVisibility()) {
					foldView.dismiss();
				}
			}
		}
	}

	/**
	 * 获取整理模式被选择的图书列表信息
	 * 
	 * @return
	 */
	public List<DragItem> getSelectedList() {
		return mSelectedList;
	}

	/**
	 * 移动文件到文件夹
	 */
	public void moveFile2Folder() {
		folders.clear();
		folders = MZBookDatabase.instance.getAllFolder(LoginUser.getpin());
		FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.bookshelf_order_dialog,
				null);
		ScrollView scrollView = (ScrollView) layout.findViewById(R.id.scrollView);
		if (folders.size() >= 4) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			params.height = ScreenUtils.dip2px(getActivity(), 300);
			scrollView.setLayoutParams(params);
		}

		LinearLayout contentLayout = (LinearLayout) layout.findViewById(R.id.view_content);
		for (int i = 0; i < folders.size() + 2; i++) {
			LinearLayout childlayout = (LinearLayout) LayoutInflater.from(getActivity())
					.inflate(R.layout.item_bookshelf_ordering, null);
			ImageView view = (ImageView) childlayout.findViewById(R.id.item_icon);
			TextView nameTextView = (TextView) childlayout.findViewById(R.id.item_name);
			if (i == 0) {
				view.setImageResource(R.drawable.bookshelf_arrangebook_bookshelf);
				nameTextView.setText(getString(R.string.bookshelf_ordering_move_to_desktop));
				childlayout.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mSelectedList != null && mSelectedList.size() > 0) {
							for (int i = 0; i < mSelectedList.size(); i++) {
								BookShelfModel model = mSelectedList.get(i).getMo();
								if (model.getBelongDirId() != -1) {
									model.setBelongDirId(-1);
									model.setModifiedTime(System.currentTimeMillis());
									MZBookDatabase.instance.updateBookshelfFolder(model);
								}
							}
						}

						mSelectedList.clear();
						getData();
						mItemAdapter.notifyDataSetChanged();
						mFileMoveDialog.dismiss();
						hideTooBar();
					}
				});

			} else if (i == 1) {
				view.setImageResource(R.drawable.bookshelf_arrangebook_addfolder);
				nameTextView.setText(getString(R.string.bookshelf_ordering_move_to_new_folder));

				childlayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final LinearLayout layout = (LinearLayout) LayoutInflater.from(getActivity())
								.inflate(R.layout.dialog_inner_view, null);
						final EditText text = (EditText) layout.findViewById(R.id.confirm_book_name);
						text.setText(getString(R.string.bookshelf_folder_default_name));

						text.setOnClickListener(
								(android.view.View.OnClickListener) new android.view.View.OnClickListener() {

							@Override
							public void onClick(android.view.View arg0) {
								text.setText("");
							}
						});
						new AlertDialog.Builder(getActivity()).setView(layout)
								.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								mFileMoveDialog.dismiss();
							}
						}).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

								String foldername = text.getText().toString();
								if (foldername.replace(" ", "").equals("")) {
									Toast.makeText(getActivity(), getString(R.string.string_input_error),
											Toast.LENGTH_LONG).show();
									return;
								}
								int id = MZBookDatabase.instance.createFolder(foldername, System.currentTimeMillis(),
										LoginUser.getpin());

								for (int i = 0; i < mSelectedList.size(); i++) {
									BookShelfModel model = mSelectedList.get(i).getMo();
									if (model.getBelongDirId() != id) {
										model.setBelongDirId(id);
										model.setModifiedTime(System.currentTimeMillis());
										MZBookDatabase.instance.updateBookshelfFolder(model);
									}
								}

								mSelectedList.clear();
								getData();
								mItemAdapter.notifyDataSetChanged();
								mFileMoveDialog.dismiss();
								hideTooBar();
							}
						}).setTitle("新文件夹名称:").create().show();

					}
				});

			} else {
				final int pos = i;
				view.setImageResource(R.drawable.bookshelf_arrangebook_folder);
				nameTextView.setText(folders.get(i - 2).getFolderName());
				childlayout.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						int id = folders.get(pos - 2).getFolderId();
						for (int i = 0; i < mSelectedList.size(); i++) {
							BookShelfModel model = mSelectedList.get(i).getMo();
							if (model.getBelongDirId() != id) {
								model.setBelongDirId(id);
								model.setModifiedTime(System.currentTimeMillis());
								MZBookDatabase.instance.updateBookshelfFolder(model);
							}
						}

						mSelectedList.clear();
						getData();
						mItemAdapter.notifyDataSetChanged();
						mFileMoveDialog.dismiss();
						hideTooBar();
					}

				});

			}

			if (i == 0) {
				boolean justAbook = true;
				if (mSelectedList != null && mSelectedList.size() > 0) {
					for (int k = 0; k < mSelectedList.size(); k++) {
						BookShelfModel model = mSelectedList.get(i).getMo();
						if (model.getBelongDirId() != -1) {
							justAbook = false;
							break;
						}
					}
				}

				if (!justAbook)
					contentLayout.addView(childlayout);

			} else {
				contentLayout.addView(childlayout);
			}

		}

		mFileMoveDialog = new AlertDialog.Builder(getActivity()).setView(layout).create();
		mFileMoveDialog.show();
	}

	public static EBookAnimationUtils getEBookAnimationUtils() {
		return mEBookAnimationUtils;
	}

	public static void resetClick() {
		isOpenBook = false;
	}

}
