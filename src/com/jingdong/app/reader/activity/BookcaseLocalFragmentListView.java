package com.jingdong.app.reader.activity;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mzbook.sortview.model.BookShelfModel;
import com.android.mzbook.sortview.model.BookShelfModel.DownLoadType;
import com.android.mzbook.sortview.optimized.DragItemAdapter;
import com.android.mzbook.sortview.optimized.ImageSizeUtils;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.book.EBook;
import com.jingdong.app.reader.bookshelf.BookcaseLocalFragmentNewUI;
import com.jingdong.app.reader.bookshelf.BorrowBookMainActivity;
import com.jingdong.app.reader.bookstore.search.BookShelfSearchActivity;
import com.jingdong.app.reader.client.DownloadHelper;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadStateManager;
import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.client.DownloadThreadQueue.RefreshAble;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.BookInforEDetail;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.entity.extra.JDEBook;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.extension.integration.FloatingActionButton;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.io.StoragePath;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.activity.IntegrationActivity;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.me.model.SignSuccessionResult;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OlineDesUtils;
import com.jingdong.app.reader.onlinereading.OnlineReadManager;
import com.jingdong.app.reader.preloader.CutBitmapDisplayer;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ActivityUtils;
import com.jingdong.app.reader.util.FileGuider;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.SettingUtils;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.TopBarPopupWindow;
import com.jingdong.app.reader.view.TopBarPopupWindow.onPopupWindowItemClickListener;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.bookshelf.LongClickDeleteDialog;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class BookcaseLocalFragmentListView extends CommonFragment implements TopBarViewListener, 
						onPopupWindowItemClickListener, RefreshAble, OnPageChangeListener, OnItemClickListener {

	private static final String EBOOK_TYPE = "ebook";

	private List<BookShelfModel> models = new ArrayList<BookShelfModel>();;
	private FrameLayout recentLayout;
	private List<BookShelfModel> recentModels = new ArrayList<BookShelfModel>();

	private TopBarView topBarView = null;

	private TopBarPopupWindow leftPopupWindow = null;
	private TopBarPopupWindow rightPopupWindow = null;
	private ListView mListView = null;

	public static final String TYPE_LEFT_MENU = "left_menu";
	public static final String TYPE_RIGHT_MENU = "right_menu";
	private BookListAdapter bookListAdapter;
	private boolean isOpenBook = false;
	private int flag = -1;
	protected final static int UPDATE_UI_MESSAGE = 0;
	public final static int UPDATEDATA = 1004;

	// 顶部最近在读 滑动布局
	private ViewPager recentViewPager;
	private MyViewPagerAdapter recentViewPagerAdapter;
	private ImageView[] dots;
	private int currentIndex;
	private List<View> allPagerViews = null;
	private LinearLayout dotsArea;
	private LinearLayout recent_layout;
	private RelativeLayout viewpager_layout;
	private LinearLayout linearLayout;
	private Button empty_btn;
	
	/** 图书更新提示消息 */
	private final static int CHECK_EBOOK_UPDATE = 1001;
	/** 图书更新提示检查定时器间隔时间 */
	private final static int EBOOK_UPDATE_CHECK_TIME = 1000;
	/** 图书更新提示检查计时时间 */
	private int mCheckTime = 0;
	/** 最大检查时间 */
	private int MAX_CHECK_TIME = 60;
	private LongClickDeleteDialog mLongClickDeleteDialog;
	private int mLongPosition = -1;
	public static Handler handler;
	private ImageSizeUtils utils;
	

	private FloatingActionButton mFab;

	private Activity mContext;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mContext = activity;
	}

	class UpdateTimeComparator implements Comparator<BookShelfModel> {

		@Override
		public int compare(BookShelfModel lhs, BookShelfModel rhs) {
			if (lhs == null)
				return 1;
			if (rhs == null)
				return -1;
			double ptime1 = (double) lhs.getPercentTime();
			double mtime1 = (double) lhs.getModifiedTime();
			double ptime2 = (double) rhs.getPercentTime();
			double mtime2 = (double) rhs.getModifiedTime();
			
			double time1 = 0;
			double time2 = 0;
			if ( 0 == ptime1 ) {
				time1 = mtime1/1000;
			} else {
				time1 = ptime1;
			}
			
			if ( 0 == ptime2 ) {
				time2 = mtime2/1000;
			} else {
				time2 = ptime2;
			}
			
			// 降序排列
			if (time1 < time2)
				return 1;
			if (time1 > time2)
				return -1;
			return 0;
		}

	}

	class NameComparator implements Comparator<BookShelfModel> {

		@Override
		public int compare(BookShelfModel lhs, BookShelfModel rhs) {
			// 名称排列
			if (lhs == null || lhs.getBookName() == null)
				return 1;
			if (rhs == null || rhs.getBookName() == null)
				return -1;
			String name1 = (String) lhs.getBookName().toString();
			String name2 = (String) rhs.getBookName().toString();

			return Collator.getInstance(Locale.CHINESE).compare(name1, name2);
		}
	}

	public BookcaseLocalFragmentListView() {
		super();
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		utils = new ImageSizeUtils(getActivity());
		if (!TextUtils.isEmpty(LoginUser.getpin()) && !LocalUserSetting.getLoginSacn(getActivity()))
			LoginUser.scanDocumentToBookShelf(getActivity(), LoginUser.getpin());

		fragmentTag = "BookcaseLocalFragmentNewUI";
		String userId = LoginUser.getpin();

		IntentFilter filter = new IntentFilter();
		filter.addAction("com.mzread.action.refresh");
		filter.addAction("com.jdread.action.login");
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);

		handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (msg.what == UPDATE_UI_MESSAGE) {
					Bundle bundleData = (Bundle) msg.obj;
					int index = bundleData.getInt("index");
					int status = bundleData.getInt("status");
					int progress = bundleData.getInt("progress");
					String bookid = bundleData.getString("bookid");
					bookListAdapter.updateItemView(index + 1, status, progress, bookid);
				}else if(msg.what == CHECK_EBOOK_UPDATE) {
					checkEbookUpdateState();
				}else if(msg.what == UPDATEDATA) {
					getData();
					if(null != bookListAdapter) {
						bookListAdapter.notifyDataSetChanged();
					}
				}

			};
		};
	}

	// new 最近在读
	public void prepareRecentPager() {

		if (recentModels == null)
			return;

		if (recentModels.size() == 0) {
			recent_layout.setVisibility(View.VISIBLE);
			viewpager_layout.setVisibility(View.GONE);
		} else {
			recent_layout.setVisibility(View.GONE);
			viewpager_layout.setVisibility(View.VISIBLE);
			List<View> list = new ArrayList<View>();
			for (int i = 0; i < recentModels.size(); i++) {
				final View view = LayoutInflater.from(getActivity()).inflate(R.layout.bookcase_recent_layout_item, null);
				ImageView bookcover = (ImageView) view.findViewById(R.id.book_cover);
				TextView bookname = (TextView) view.findViewById(R.id.book_name);
				TextView bookauthor = (TextView) view.findViewById(R.id.book_author);
				TextView bookProgressAndNotes = (TextView) view.findViewById(R.id.book_progress_notes);
				TextView bookReadAt = (TextView) view.findViewById(R.id.read_at);
				ImageView imageViewLabel = (ImageView) view.findViewById(R.id.imageViewLabel);
				
				if(MZBookApplication.isPad()) {
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
				} else if (model.getBookCover().contains("http://")) {
					option = GlobalVarable.getCutBookDisplayOptions(false);
				} else {
					cover = "file://" + cover;
				}
				
				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				layoutParams.height = (int) utils.getPerItemImageHeight();
				layoutParams.width = (int) utils.getPerItemImageWidth();
				bookcover.setLayoutParams(layoutParams);
				
				ImageLoader.getInstance().displayImage(cover, bookcover, option, new ImageLoadingListener() {

					@Override
					public void onLoadingCancelled(String arg0, View arg1) {

					}

					@Override
					public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {

						Activity activity = getActivity();

						if (activity == null)
							return;

						if (arg2 != null && !arg2.isRecycled() && activity.getResources() != null) {
							arg2 = CutBitmapDisplayer.CropForExtraWidth(arg2, false);
							view.setBackgroundDrawable(ImageUtils.overlay(ImageUtils.BoxBlurFilter(arg2),
									BitmapFactory.decodeResource(activity.getResources(), R.drawable.overlay)));
						} else if (getResources() != null) {
							view.setBackgroundDrawable(ImageUtils.overlay(BitmapFactory.decodeResource(activity.getResources(), R.drawable.book_cover_default),
									BitmapFactory.decodeResource(getResources(), R.drawable.overlay)));
						}

					}

					@Override
					public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
						if (getResources() != null) {
							view.setBackgroundDrawable(ImageUtils.overlay(BitmapFactory.decodeResource(getResources(), R.drawable.book_cover_default),
									BitmapFactory.decodeResource(getResources(), R.drawable.overlay)));
						}
					}

					@Override
					public void onLoadingStarted(String arg0, View arg1) {

					}
				});
				bookname.setText(model.getBookName());
				bookauthor.setText("null".equals(model.getAuthor()) ? getString(R.string.author_unknown) : model.getAuthor());
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

				bookProgressAndNotes.setText("阅读进度" + (int) (model.getBookPercent()) + "%,阅读笔记共" + model.getNote_num() + "条");
				bookReadAt.setText("上次阅读时间 " + TimeFormat.formatTimeByMiliSecond(getResources(), (long) model.getPercentTime() * 1000));

				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						if (!isOpenBook) {

							if (model.getBookType().equals(BookShelfModel.EBOOK)) {
								isOpenBook = true;
								EBook ebook = MZBookDatabase.instance.getEBook(model.getBookid());
								OpenBookHelper.openEBook(getActivity(), ebook.bookId);

							} else {
								isOpenBook = true;
								Document doc = MZBookDatabase.instance.getDocument(model.getBookid());
								OpenBookHelper.openDocument(getActivity(), doc.documentId);
							}
						}
					}
				});

				list.add(view);
			}

			allPagerViews.clear();
			allPagerViews.addAll(list);
			intiBottomDot();
			recentViewPager.setAdapter(recentViewPagerAdapter);
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

	@Override
	public void onResume() {
		super.onResume();
		// 将悬浮窗设置为隐藏状态
		mFab.setVisibility(View.GONE);
		//处理签到按钮状态
		handleFABState();
		if (!TextUtils.isEmpty(LoginUser.getpin()) && !LocalUserSetting.getLoginSacn(getActivity()))
			LoginUser.scanDocumentToBookShelf(getActivity(), LoginUser.getpin());
		//读取数据
		getData();
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_DOCUMENT);
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.mzread.action.downloaded");
		filter.addAction("com.mzread.action.refresh");
		filter.addAction("com.jdread.action.login");
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);

		// //显示签到悬浮按钮
		// LauncherActivity containerAct = (LauncherActivity) getActivity();
		// if (containerAct != null) {
		// containerAct.showSignFloatView();
		// }else{
		// MZLog.e("J", "containerAct is null");
		// }
		System.out.println("BBBBBBBBB===========onResume======");
		if(null != bookListAdapter) {
			bookListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		// true表示该fragment被隐藏了
		if (hidden) {
			// SignFloatWinManager.getInstance().dismiss(false);
		} else {// false，表示该fragment正在显示
			DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
			DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_DOCUMENT);

			// //显示签到悬浮按钮
			// LauncherActivity containerAct = (LauncherActivity) getActivity();
			// if (containerAct != null) {
			// containerAct.showSignFloatView();
			// }else{
			// MZLog.e("J", "containerAct is null");
			// }

		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// SignFloatWinManager.getInstance().dismiss(false);
	}

	private void handleFABState() {
		LauncherActivity containerAct = (LauncherActivity) getActivity();
		if (containerAct != null) {
			containerAct.handleFloatingButtonState(mFab);
		} else {
			MZLog.e("J", "containerAct is null");
		}
	};

	public void getData() {
		recentModels.clear();
		models.clear();
		String userId = LoginUser.getpin();
		//读取数据库最近阅读数据
		recentModels = MZBookDatabase.instance.listRecentReadingBooks(userId);
		isOpenBook = false;
		//读取书架图书数据信息
		models = MZBookDatabase.instance.listBookShelf(userId, 1);

		if (models.size() == 0) {
			linearLayout.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		} else {
			for(int i=0; i<models.size(); i++) {
				boolean buyed = SettingUtils.getInstance().getBoolean("Buyed:" + models.get(i).getServerid(), false);
				if(buyed) {
					models.get(i).setDownloadType(DownLoadType.Buyed);
				}	
			}
			
			linearLayout.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
			if (flag == 0) {
				Collections.sort(models, new NameComparator());
			} else {
				Collections.sort(models, new UpdateTimeComparator());
			}
			if (bookListAdapter == null) {
				bookListAdapter = new BookListAdapter(getActivity());
				mListView.setAdapter(bookListAdapter);
			} else
				bookListAdapter.notifyDataSetChanged();
			prepareRecentPager();
		}
		
		checkEbookUpdateState();
	}
	
	/**
	* @Description: 定时检查登录状态请求图书更新数据
	* @return void
	* @author xuhongwei1
	* @date 2015年10月9日 上午11:51:52 
	* @throws 
	*/ 
	private void checkEbookUpdateState() {
		mCheckTime++;
		handler.removeMessages(CHECK_EBOOK_UPDATE);
		if(mCheckTime > MAX_CHECK_TIME) {
			mCheckTime = 0;
			return;
		}
		
		if (!LoginUser.isLogin()) {
			handler.sendEmptyMessageDelayed(CHECK_EBOOK_UPDATE, EBOOK_UPDATE_CHECK_TIME);
		}else{
			mCheckTime = 0;
			getTodayBuyedEbookOrderList();
			requestEbookUpdateData();
		}
	}
	
	
	/**
	* @Description: 获取当天订单图书列表
	* @author xuhongwei1
	* @date 2015年11月25日 下午1:56:20 
	* @throws 
	*/ 
	private void getTodayBuyedEbookOrderList() {
		if (!NetWorkUtils.isNetworkConnected(mContext)) {
			return;
		}
		
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.
				getTodayBuyedEbookOrderListParams(), new MyAsyncHttpResponseHandler(mContext) {
			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				String jsonStr = new String(responseBody);
				try {
					JSONObject json = new JSONObject(jsonStr);
					String code = json.optString("code");
					if(!code.equals("0")) {
						return;
					}
					
					JSONArray jsonarr = json.getJSONArray("resultList");
					if(null == jsonarr) {
						return;
					}
					
					boolean update = false;
					for(int i=0; i<jsonarr.length(); i++) {
						JSONObject obj = (JSONObject) jsonarr.opt(i);
						if(null != obj) {
							long serverid = obj.optInt("bookId");
							if(!checkEbookExits(serverid)) {
								SettingUtils.getInstance().putBoolean("AddBookShelf:" + serverid, true);
								SettingUtils.getInstance().putBoolean("Buyed:" + serverid, true);
								update = true;
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
					
					if(update) {
						getData();
					}
						
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
		});
	}
	
	private boolean checkEbookExits(long bookid) {
		boolean AddBookShelf = SettingUtils.getInstance().getBoolean("AddBookShelf:" + bookid, false);
		if(AddBookShelf) {
			return true;
		}
		
		if(null == models || models.size() <= 0) {
			return false;
		}
		
		for(int i=0; i<models.size(); i++) {
			BookShelfModel item = models.get(i);
			long serverid = item.getServerid();
			if(serverid == bookid) {
				return true;
			}
		}
			
		return false;
	}
	
	/**
	* @Description: 请求图书文件更新数据
	* @return void
	* @author xuhongwei1
	* @date 2015年10月9日 下午1:49:31 
	* @throws 
	*/ 
	private void requestEbookUpdateData() {
		if (!NetWorkUtils.isNetworkConnected(mContext) || models.size() <= 0) {
			return;
		}
		
		List<String> ebookids = new ArrayList<String>();
		for(BookShelfModel mBookShelfModel : models) {
			long ebookid = mBookShelfModel.getServerid();
			if(ebookid != 0 && (mBookShelfModel.getBookCoverLabel() != BookShelfModel.LABEL_TRYREAD)) {
				ebookids.add(Long.toString(ebookid));
			}
		}
		
		if(ebookids.size() <= 0) {
			return;
		}
		
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.
				getEBookUpdateParams(ebookids), new MyAsyncHttpResponseHandler(mContext) {
			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				String jsonStr = new String(responseBody);
				try {
					JSONObject json = new JSONObject(jsonStr);
					String code = json.optString("code");
					if(!code.equals("0")) {
						return;
					}
					
					JSONArray jsonarr = json.getJSONArray("resultList");
					if(null == jsonarr) {
						return;
					}
					
					for(int i=0; i<jsonarr.length(); i++) {
						JSONObject obj = (JSONObject) jsonarr.get(i);
						if(null != obj) {
							String ebookId = obj.optString("ebookId");
							boolean needUpdate = obj.optBoolean("needUpdate");
							
							if(!needUpdate) {
								needUpdate = SettingUtils.getInstance().getBoolean("file_error:" + ebookId, false);
								if(!needUpdate) {
									continue;
								}
							}
							
							for(int j=0; j<models.size(); j++) {
								BookShelfModel mBookShelfModel = models.get(j);
								String serverId = Long.toString(mBookShelfModel.getServerid());
								if(ebookId.equals(serverId)) {
									models.get(j).setDownloadType(DownLoadType.Update);
									break;
								}
							}
						}
					}		
						
					bookListAdapter.notifyDataSetChanged();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();

		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.activity_root_layout, null);
		topBarView = (TopBarView) root.findViewById(R.id.topbar);
		LinearLayout temp = (LinearLayout) root.findViewById(R.id.container);

		View rootView = inflater.inflate(R.layout.bookcase_local_listview_item, temp);

		initTopbarView();

		View headerView = inflater.inflate(R.layout.header_layout, null);

		mListView = (ListView) rootView.findViewById(R.id.mlistview);
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

		mFab.listenTo(mListView);

		linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
		empty_btn = (Button) rootView.findViewById(R.id.empty);

		empty_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(arg0.getContext(), LauncherActivity.class);
				intent.putExtra("TAB_INDEX", 0);
				startActivity(intent);
			}
		});
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
				topBarView.setLeftMenuVisiable(true, R.drawable.topbar_add);
				topBarView.setRightMenuOneVisiable(true, R.drawable.topbar_menu, false);
			}
		});
		// 初始化topbar 结束
		mListView.setOnItemClickListener(this);

		recent_layout = (LinearLayout) headerView.findViewById(R.id.recent_layout);
		LinearLayout.LayoutParams recent_Layout_Params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		recent_Layout_Params.height = (int) utils.getPerItemImageHeight() + ScreenUtils.dip2px(32);
		recent_layout.setLayoutParams(recent_Layout_Params);
		viewpager_layout = (RelativeLayout) headerView.findViewById(R.id.viewpager_layout);
		// 顶部滑动最近阅读布局开始
		allPagerViews = new ArrayList<View>();
		recentViewPager = (ViewPager) headerView.findViewById(R.id.viewpager);
		RelativeLayout.LayoutParams recentViewPagerParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		recentViewPagerParams.height = (int) utils.getPerItemImageHeight() + ScreenUtils.dip2px(32);
		recentViewPager.setLayoutParams(recentViewPagerParams);
		recentViewPager.setOnPageChangeListener(this);
		recentViewPagerAdapter = new MyViewPagerAdapter(allPagerViews);
		recentViewPager.setAdapter(recentViewPagerAdapter);
		dotsArea = (LinearLayout) headerView.findViewById(R.id.dotsArea);
		mListView.addHeaderView(headerView);
		
		// 顶部滑动最近阅读布局结束
		
		mLongClickDeleteDialog = new LongClickDeleteDialog(getActivity());
		mLongClickDeleteDialog.setOnDeleteClickListener(new LongClickDeleteDialog.OnDeleteClickListener() {
			@Override
			public void clickDelete() {
				deleteBooks(models.get(mLongPosition), mLongPosition);
				getData();
				if(null != bookListAdapter) {
					bookListAdapter.notifyDataSetChanged();
				}
				if(null != BookcaseLocalFragmentNewUI.handler) {
					BookcaseLocalFragmentNewUI.handler.sendEmptyMessage(BookcaseLocalFragmentListView.UPDATEDATA);
				}
			}
		});
		
		return root;
	}
	
	public void processSign() {
		new Handler().postDelayed(new Runnable(){  
		     public void run() {  
		    	 if(mFab!=null)
		    		 mFab.setClickable(true);
		     }  
		  }, 3000); 
		
		IntegrationAPI.signGetScore(getActivity(), false, new GrandScoreListener() {

			@Override
			public void onGrandSuccess(SignScore score) {
				// 1、隐藏悬浮窗
				mFab.setVisibility(View.GONE);

				// 2、弹出提示框
				// String scoreInfo = "恭喜你签到获得" + score + "积分";
				// CustomToast.showToast(getActivity(), scoreInfo);
				SignSuccessionResult successionResult = score.getSignSuccessionResult();
				// 连续签到活动已开启，显示弹窗
				if (successionResult != null && successionResult.isSignSuccession()) {
					String msg = null;
					if (successionResult.getSignSuccessionGiftId() > 0) {// 有奖品
						if (successionResult.isSignSuccessionGiftSuccess()) {// 获取奖品成功
							msg = "签到获得" + score.getGetScore() + "积分，连续签到奖励" + successionResult.getSignSuccessionGiftMsg() + "(京豆、优惠券可去京东主站查询)";
						} else {
							msg = "签到获得" + score.getGetScore() + "积分，抱歉" + successionResult.getSignSuccessionGiftMsg();
						}
					} else {
						msg = "签到获得" + score.getGetScore() + "积分，" + successionResult.getSignSuccessionGiftMsg() + "哦！";
					}

					DialogManager.showCommonDialog(mContext, "", msg, "去看看", "取消", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								Intent intent = new Intent(mContext, IntegrationActivity.class);
								startActivity(intent);
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								break;
							default:
								break;
							}
							dialog.dismiss();
						}

					});

				} else {
					String scoreInfo = "恭喜你签到获得" + score.getGetScore() + "积分";
					SpannableString span = new SpannableString(scoreInfo);
					int start = 7;
					int end = start + String.valueOf(score.getGetScore()).length();
					span.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					CustomToast.showToast(mContext, span);
				}

			}

			@Override
			public void onGrandFail() {
				mFab.setClickable(true);
				MZLog.e("J", "onGrandFail,code=");
			}
		});
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	// 最近在读 滑动布局
	class MyViewPagerAdapter extends PagerAdapter {

		private List<View> views;

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if(position > 0 && position < views.size())
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
			return (arg0 == arg1);
		}

	}

	private class BookListAdapter extends BaseAdapter {

		private Context context;

		class ViewHolder {
			TextView bookName;
			TextView bookAuthor;
			TextView progresstxt;
			TextView readTime;
			Button statue_button;
			ImageView bookCover;
			ImageView bookCoverLabel;
		}

		BookListAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return models.size();
		}

		@Override
		public Object getItem(int position) {
			return models.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.booklist_item, parent, false);
				holder = new ViewHolder();
				holder.bookName = (TextView) convertView.findViewById(R.id.user_book_name);
				holder.bookAuthor = (TextView) convertView.findViewById(R.id.user_book_author);
				holder.bookCover = (ImageView) convertView.findViewById(R.id.user_book_cover);
				holder.progresstxt = (TextView) convertView.findViewById(R.id.read);
				holder.readTime = (TextView) convertView.findViewById(R.id.readtime);
				holder.statue_button = (Button) convertView.findViewById(R.id.statueButton);
				holder.bookCoverLabel = (ImageView) convertView.findViewById(R.id.imageViewLabel);
				holder.bookCoverLabel.setVisibility(View.GONE);
				holder.bookAuthor.setVisibility(View.VISIBLE);
				holder.bookCover.setImageResource(R.drawable.bg_default_cover);
				
				if(MZBookApplication.isPad()) {
					holder.bookName.setTextSize(16);	
					holder.bookAuthor.setTextSize(14);	
					holder.progresstxt.setTextSize(14);	
					holder.readTime.setTextSize(14);
					
					int w = (int)(utils.getPerItemImageWidth() * 0.6);
					int h = (int)(utils.getPerItemImageHeight() * 0.6);
					FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(w, h);
					holder.bookCover.setLayoutParams(params);
				}
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String book_title = "";
			String book_author = "";
			String book_path = "";
			long book_readtime;
			int book_percent = 0;
			int book_note = 0;
			if (position < 0 || models.get(position) == null)
				return convertView;

			final BookShelfModel item = models.get(position);

			book_title = item.getBookName();
			book_author = item.getAuthor();
			book_path = item.getBookCover();
			book_percent = (int) (item.getBookPercent());
			book_readtime = (long) item.getPercentTime();
			book_note = item.getNote_num();
			if (item.getBookType().equals(EBOOK_TYPE)) {

				if (!TextUtils.isEmpty(book_path)) {
					if (book_path.startsWith("http://")) {
						ImageLoader.getInstance().displayImage(book_path, holder.bookCover, GlobalVarable.getCutBookDisplayOptions(false));
					} else {

						File file = new File(book_path);
						if (file.exists()) {
							ImageLoader.getInstance().displayImage("file://" + file.getPath(), holder.bookCover, GlobalVarable.getDefaultBookDisplayOptions());
						} else {
							ImageLoader.getInstance().displayImage("", holder.bookCover, GlobalVarable.getDefaultBookDisplayOptions());
						}
					}
				}

				int bookCoverLabel = item.getBookCoverLabel();
				if (bookCoverLabel == BookShelfModel.LABEL_TRYREAD) {
					holder.bookCoverLabel.setVisibility(View.VISIBLE);
					holder.bookCoverLabel.setImageResource(R.drawable.badge_coverlabel_trial);
				} else if (bookCoverLabel == BookShelfModel.LABEL_CHANGDU) {
					holder.bookCoverLabel.setVisibility(View.VISIBLE);
					holder.bookCoverLabel.setImageResource(R.drawable.badge_coverlabel_vip);
				} else if (bookCoverLabel == BookShelfModel.LABEL_BORROWED) {
					holder.bookCoverLabel.setVisibility(View.VISIBLE);
					holder.bookCoverLabel.setImageResource(R.drawable.badge_coverlabel_borrow);
				} else if (bookCoverLabel == BookShelfModel.LABEL_USER_BORROWED) {
					holder.bookCoverLabel.setVisibility(View.VISIBLE);
					holder.bookCoverLabel.setImageResource(R.drawable.badge_coverlabel_borrow);
				}else {
					holder.bookCoverLabel.setVisibility(View.GONE);
				}
			} else {
				if (!TextUtils.isEmpty(book_path)) {
					File file = new File(book_path);
					if (file.exists()) {
						ImageLoader.getInstance().displayImage("file://" + file.getPath(), holder.bookCover, GlobalVarable.getCutBookDisplayOptions());
					} else {
						holder.bookCover.setImageResource(R.drawable.bg_default_cover);
					}
				}
				holder.bookCoverLabel.setVisibility(View.GONE);
			}
			holder.bookName.setText(book_title);
			holder.bookAuthor.setText("null".equals(book_author) ? getString(R.string.author_unknown) : book_author);
			if (book_readtime == 0 && item.getBookPercent() == 0) {
				holder.progresstxt.setText("");
				holder.readTime.setText("未读");
				Drawable drawable = mContext.getResources().getDrawable(R.drawable.red_dot);
				holder.readTime.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			} else {
				holder.progresstxt.setText("已读到" + book_percent + "%,笔记" + book_note + "条");
				holder.readTime.setText("上次阅读时间 " + TimeFormat.formatTimeByMiliSecond(getResources(), book_readtime * 1000));
				holder.readTime.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			}
			// 暂且去掉书架中书籍的作者名称，在确定布局后再显示
			if (item.getDownload_state() == DownloadedAble.STATE_LOADED) {
				boolean needUpdate = SettingUtils.getInstance().getBoolean("file_error:" + item.getServerid(), false);
				if(needUpdate) {
					holder.statue_button.setText("更新");
				}else {
					holder.statue_button.setText("阅读");
				}
				holder.statue_button.setTextColor(getResources().getColor(R.color.highlight_color));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
			} else if (item.getDownload_state() == DownloadedAble.STATE_LOADING) {
				// updateItemData(position, false);
				int progress = 0;
				if (item.getBook_size() > 0)
					progress = (int) (item.getDownload_progress() * 100 / models.get(position).getBook_size());
				holder.statue_button.setText(progress + "%");
				holder.statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
			} else if (item.getDownload_state() == DownloadedAble.STATE_LOAD_FAILED) {
				// updateItemData(position, false);
				if(item.getDownloadType() == DownLoadType.Update) {
					holder.statue_button.setText("更新失败");
				}else {
					holder.statue_button.setText("下载失败");
				}
				holder.statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
			} else if (item.getDownload_state() == DownloadedAble.STATE_LOAD_PAUSED) {
				// updateItemData(position, false);
				holder.statue_button.setText("继续");
				holder.statue_button.setTextColor(getResources().getColor(R.color.text_main));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_black_h24);
				
				//处理更新的时候起始状态显示继续问题
				boolean startDownloading = SettingUtils.getInstance().getBoolean("startDownloading:" + item.getServerid(), false);
				if(startDownloading) {
					int progress = 0;
					if (item.getBook_size() > 0)
						progress = (int) (item.getDownload_progress() * 100 / models.get(position).getBook_size());
					
					if(0 == progress) {
						holder.statue_button.setText(progress + "%");
						holder.statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
						holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
					}
				}
			} else {
				boolean isbuy = SettingUtils.getInstance().getBoolean("Buyed:" + item.getServerid(), false);
				boolean needUpdate = SettingUtils.getInstance().getBoolean("file_error:" + item.getServerid(), false);
				if(isbuy) {
					holder.statue_button.setText("下载");	
				}else if(needUpdate) {
					holder.statue_button.setText("更新");
				}else {
					if(item.getDownloadType() == DownLoadType.Update) {
						holder.statue_button.setText("更新");
					}else {
						models.get(position).setDownloadType(DownLoadType.Normal);
						holder.statue_button.setText("阅读");
					}
				}
				
				holder.statue_button.setTextColor(getResources().getColor(R.color.highlight_color));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
			}
			holder.statue_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					responToItemClick(position, true);
				}
			});
			convertView.setTag(holder);
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					responToItemClick(position, false);
				}
			});
			convertView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if(!mLongClickDeleteDialog.isShowing()) {
						mLongPosition = position;
						mLongClickDeleteDialog.show();
					}
					return false;
				}
			});
			
			return convertView;
		}

		private void responToItemClick(int position, final boolean isBtn) {
			BookShelfModel model = models.get(position);
			if (!models.get(position).isDownloaded() && model.getDownload_state() != LocalBook.STATE_LOAD_READING 
								&& model.getDownload_state() != LocalBook.STATE_NO_LOAD) {

				DownloadedAble downloadedAble = null;

				if (model.getBookType().equals(BookShelfModel.EBOOK)) {
					downloadedAble = LocalBook.getLocalBookByIndex(model.getBookid());

				} else if (model.getBookType().equals(BookShelfModel.DOCUMENT)) {
					downloadedAble = MZBookDatabase.instance.getLocalDocument(model.getBookid());
					MZLog.d("wangguodong", "点击document");
				} else {
					return;
				}

				if (model.getDownload_state() == LocalBook.STATE_FAILED || model.getDownload_state() == LocalBook.STATE_LOAD_FAILED) {

					model.setDownload_state(LocalBook.STATE_LOADING);
					models.set(position, model);

					Bundle bundle = new Bundle();
					bundle.putInt("index", position);
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
					bundle.putInt("progress", 0);
					bundle.putString("bookid", "" + model.getBookid());
					sendMessage(bundle);
					DownloadHelper.restartDownload(getActivity(), model.getBookType(), downloadedAble);

				} else if (model.getDownload_state() == LocalBook.STATE_LOAD_PAUSED) {
					SettingUtils.getInstance().putBoolean("startDownloading:" + model.getBookid(), false);
					model.setDownload_state(LocalBook.STATE_LOADING);
					models.set(position, model);

					Bundle bundle = new Bundle();
					bundle.putInt("index", position);
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
					bundle.putInt("progress", -1);
					bundle.putString("bookid", "" + model.getBookid());
					sendMessage(bundle);
					DownloadHelper.resumeDownload(getActivity(), model.getBookType(), downloadedAble);
				} else if (model.getDownload_state() == LocalBook.STATE_LOADING) {

					model.setDownload_state(LocalBook.STATE_LOAD_PAUSED);
					models.set(position, model);

					Bundle bundle = new Bundle();
					bundle.putInt("index", position);
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_PAUSED);
					bundle.putInt("progress", -1);
					bundle.putString("bookid", "" + model.getBookid());
					sendMessage(bundle);
					DownloadHelper.stopDownload(getActivity(), model.getBookType(), downloadedAble);
				} else {
					model.setDownload_state(LocalBook.STATE_LOADED);
					models.set(position, model);
					Bundle bundle = new Bundle();
					bundle.putInt("index", position);
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED);
					bundle.putInt("progress", -1);
					bundle.putString("bookid", "" + model.getBookid());
					sendMessage(bundle);
				}
				return;
			}
			
			
			if(model.getDownloadType() == DownLoadType.Update) {
				if(isBtn) {
					SettingUtils.getInstance().putLong("change_time" + model.getServerid(), (long)model.getModifiedTime());
					SettingUtils.getInstance().putBoolean("startDownloading:" + model.getServerid(), true);
					deleteBooks(model, position);
					updateDownloadEbook(model.getServerid(), model.getBookCoverLabel(), model.getDownloadType());
					return;
				}
			}else if(model.getDownloadType() == DownLoadType.Buyed) {
				SettingUtils.getInstance().putLong("change_time" + model.getServerid(), (long)model.getModifiedTime());
				SettingUtils.getInstance().putBoolean("startDownloading:" + model.getServerid(), true);
				deleteBooks(model, position);
				updateDownloadEbook(model.getServerid(), model.getBookCoverLabel(), model.getDownloadType());
				return;
			}
			
			if (model.getBookType().equals(BookShelfModel.EBOOK)) {
				EBook ebook = MZBookDatabase.instance.getEBook(model.getBookid());
				OpenBookHelper.openEBook(getActivity(), ebook.bookId);
			} else if (model.getBookType().equals(BookShelfModel.DOCUMENT)) {
				Document doc = MZBookDatabase.instance.getDocument(model.getBookid());
				OpenBookHelper.openDocument(getActivity(), doc.documentId);
			}
		}
		

		
		/**
		* @Description: 更新下载电子书
		* @param @param ebookid 电子书id
		* @param @param bookCoverLabel 电子书类型
		* @return void
		* @author xuhongwei1
		* @date 2015年10月12日 下午4:50:23 
		* @throws 
		*/ 
		private void updateDownloadEbook(long ebookid, final int bookCoverLabel, final DownLoadType type) {
			WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getBookInfoParams(ebookid,null), 
					true, new MyAsyncHttpResponseHandler(mContext) {
				
				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					
				}

				@Override
				public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
					String result = new String(responseBody);
					try {
						JSONObject json = new JSONObject(result);
						String codeStr = json.optString("code");
						if (codeStr != null && codeStr.equals("0")) {
							JDBookInfo bookInfo = GsonUtils.fromJson(result, JDBookInfo.class);
							if(null == bookInfo) {
								return;
							}
							
							if (bookCoverLabel == BookShelfModel.LABEL_TRYREAD) {
								//试读不需要更新
							}else if (bookCoverLabel == BookShelfModel.LABEL_CHANGDU) {
								updateChangDuBook(bookInfo, type);
							} else if (bookCoverLabel == BookShelfModel.LABEL_BORROWED) {
								updateBorrowBook(bookInfo, type);
							} else if (bookCoverLabel == BookShelfModel.LABEL_USER_BORROWED) {
								updateUserBorrowBook(bookInfo, type);
							}else {
								updateBuyBook(bookInfo, type);
							}
						} else {
							Toast.makeText(mContext, mContext.getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
			});
		}
		
		/**
		* @Description: 下载更新畅读书籍
		* @param @param bookInfo
		* @return void
		* @author xuhongwei1
		* @date 2015年10月13日 下午2:09:44 
		* @throws 
		*/ 
		private void updateChangDuBook(JDBookInfo bookInfo, final DownLoadType type) {
			if(DownLoadType.Update == type) {
				SettingUtils.getInstance().putBoolean(""+bookInfo.detail.bookId, true);
			}
			BookInforEDetail bookE = new BookInforEDetail();// 下载实体
			bookE.bookid = bookInfo.detail.bookId;// bookid
			bookE.picUrl = bookInfo.detail.logo;// 小图
			bookE.largeSizeImgUrl = bookInfo.detail.largeLogo;// 大图
			bookE.bookType = LocalBook.TYPE_EBOOK;
			// 书的类型:电子书or多媒体书
			bookE.formatName = bookInfo.detail.format;// 图书格式。
			bookE.author = bookInfo.detail.author;// 作者
			bookE.bookName = bookInfo.detail.bookName;// 书名
			bookE.size = bookInfo.detail.size + "";
			OnlineReadManager.requestServer2ReadOnline(bookE, (Activity)mContext, null, false, null);			
		}
		
		/**
		* @Description: 下载更新借阅书籍
		* @param @param bookInfo 
		* @return void
		* @author xuhongwei1
		* @date 2015年10月13日 下午2:04:31 
		* @throws 
		*/ 
		private void updateBorrowBook(JDBookInfo bookInfo, final DownLoadType type) {
			if(DownLoadType.Update == type) {
				SettingUtils.getInstance().putBoolean(""+bookInfo.detail.bookId, true);
			}
			OrderEntity orderEntity = new OrderEntity();
			orderEntity.name = bookInfo.detail.bookName;
			orderEntity.bookId = bookInfo.detail.bookId;
			// orderEntity.bookType = LocalBook.TYPE_EBOOK;
			orderEntity.orderId = bookInfo.detail.bookId;
			orderEntity.formatName = bookInfo.detail.format;
			orderEntity.author = bookInfo.detail.author;
			// orderEntity.book_size = onlineBookEntity.get
			orderEntity.orderStatus = 16;
			orderEntity.price = bookInfo.detail.jdPrice;
			orderEntity.picUrl = bookInfo.detail.logo;
			orderEntity.bigPicUrl = bookInfo.detail.largeLogo;
			orderEntity.borrowEndTime = OlineDesUtils.encrypt(bookInfo.detail.userBorrowEndTime);
			
			DownloadTool.download((Activity)mContext,
					orderEntity, null, false, LocalBook.SOURCE_BORROWED_BOOK, 0,
					true, null, false);
		}
		
		/**
		* @Description: 下载更新用户借阅书籍
		* @param @param bookInfo 
		* @return void
		* @author xuhongwei1
		* @date 2015年10月13日 下午2:04:31 
		* @throws 
		*/ 
		private void updateUserBorrowBook(JDBookInfo bookInfo, final DownLoadType type) {
			if(DownLoadType.Update == type) {
				SettingUtils.getInstance().putBoolean(""+bookInfo.detail.bookId, true);
			}
			OrderEntity orderEntity = new OrderEntity();
			orderEntity.name = bookInfo.detail.bookName;
			orderEntity.bookId = bookInfo.detail.bookId;
			// orderEntity.bookType = LocalBook.TYPE_EBOOK;
			orderEntity.orderId = bookInfo.detail.bookId;
			orderEntity.formatName = bookInfo.detail.format;
			orderEntity.author = bookInfo.detail.author;
			// orderEntity.book_size = onlineBookEntity.get
			orderEntity.orderStatus = 16;
			orderEntity.price = bookInfo.detail.jdPrice;
			orderEntity.picUrl = bookInfo.detail.logo;
			orderEntity.bigPicUrl = bookInfo.detail.largeLogo;
			orderEntity.userBuyBorrowEndTime = OlineDesUtils.encrypt(bookInfo.detail.userBuyBorrowEndTime);
			
			DownloadTool.download((Activity)mContext,
					orderEntity, null, false, LocalBook.SOURCE_USER_BORROWED_BOOK, 0,
					true, null, false);
		}
		
		/**
		* @Description: 下载更新已购书籍
		* @param @param bookInfo
		* @return void
		* @author xuhongwei1
		* @date 2015年10月13日 下午2:52:59 
		* @throws 
		*/ 
		private void updateBuyBook(JDBookInfo bookInfo, final DownLoadType type) {
			if(DownLoadType.Update == type) {
				SettingUtils.getInstance().putBoolean(""+bookInfo.detail.bookId, true);
			}
			JDEBook bookEntity = OrderEntity.FromJDBooKInfo2JDEBook(bookInfo.detail);
			OrderEntity orderEntity = OrderEntity.FromJDBooK2OrderEntity(bookEntity);
			DownloadTool.download((Activity)mContext, orderEntity, null, false, LocalBook.SOURCE_BUYED_BOOK, 0, false,null,false);
		}

		public void updateItemData(int position, boolean isDownloaded) {
			if (models != null && models.size() > position && position > -1) {
				BookShelfModel item = models.get(position);
				item.setDownloaded(isDownloaded);
				models.set(position, item);
			}
		}

		public void updateItemView(final int index, int download_status, int progress, String bookid) {
			if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED) {
				SettingUtils.getInstance().putBoolean("Buyed:" + bookid, false);
				SettingUtils.getInstance().putBoolean("file_error:" + bookid, false);
				updateItemData(index, true);
			} else {
				updateItemData(index, false);
			}

			if (mListView != null) {
				int visiblePos = mListView.getFirstVisiblePosition();
				int offset = index - visiblePos;
				if (offset < 0 || index <= 0)
					return;
				View view = mListView.getChildAt(offset);
				if (view != null && getResources() != null) {
					BookShelfModel model = models.get(index - 1);
					ViewHolder holder = (ViewHolder) view.getTag();

					if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED) {
						System.out.println("BBBBBBBB=====finish===111111====");							
						holder.statue_button.setText("阅读");
						holder.statue_button.setTextColor(getResources().getColor(R.color.highlight_color));
						holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
						model.setDownload_state(LocalBook.STATE_LOAD_READING);
					} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING) {
						if(progress < 0)
							progress = 0;
						holder.statue_button.setText(progress + "%");
						holder.statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
						holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
					} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_FAILED) {
						holder.statue_button.setText("下载失败");
						holder.statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
						holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
					} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_PAUSED) {
						holder.statue_button.setText("继续");
						holder.statue_button.setTextColor(getResources().getColor(R.color.text_main));
						holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_black_h24);
					}
				}
			}
		}
	}
	
	public void deleteBooks(BookShelfModel model, int position) {
		String userId = LoginUser.getpin();
		if (model.getBookType().equals(BookShelfModel.EBOOK)) {
			EBook ebook = MZBookDatabase.instance.getEBook(model.getBookid());

			// 先暂停下载 然后继续删除
			LocalBook localBook = LocalBook.getLocalBookByIndex(model.getBookid());
			if (null != localBook && localBook.state == LocalBook.STATE_LOADING) {
				MZLog.d("wangguodong", "暂停正在下载的任务");
				DownloadHelper.stopDownload(getActivity(), BookShelfModel.EBOOK, localBook);
				SettingUtils.getInstance().putBoolean(""+localBook.book_id, false);
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
		} else if (model.getBookType().equals(BookShelfModel.DOCUMENT)) {
			Document doc = MZBookDatabase.instance.getDocument(model.getBookid());

			LocalDocument localDocument = LocalDocument.getLocalDocument(model.getBookid(), LoginUser.getpin());
			
			if (null != localDocument && localDocument.state == LocalBook.STATE_LOADING) {
				MZLog.d("wangguodong", "暂停正在下载的任务");

				// 先暂停下载 然后继续删除
				DownloadHelper.stopDownload(getActivity(), BookShelfModel.DOCUMENT, localDocument);
				SettingUtils.getInstance().putBoolean(""+localDocument.server_id, false);
				SettingUtils.getInstance().putBoolean(""+localDocument.server_id, false);
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

	public BookcaseLocalFragmentListView(int flag) {
		super();
		this.flag = flag;
	}

	// 获得指定type id 的item 的位置
	public int getPositionByTypeAndId(String type, int id) {
		if (models == null)
			return -1;
		else {
			for (int i = 0; i < models.size(); i++) {

				BookShelfModel model = models.get(i);
				if (model.getBookType().equals(type)) {
					if (model.getBookid() == id) {
						return i;
					}
				}

			}
			return -1;
		}
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {

			if (intent.getAction().equals("com.mzread.action.downloaded")) {
				getData();
			} else if (intent.getAction().equals("com.mzread.action.refresh")) {
				getData();
				Toast.makeText(getActivity(), "设备解绑，清空书城图书", Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals("com.jdread.action.login")) {
				// 自动登录成功，设置悬浮窗显示状态
				handleFABState();
			}

		};
	};

	@Override
	public void refresh(DownloadedAble DownloadAble) {
		int index = -1;

		if (DownloadAble.getType() == DownloadedAble.TYPE_BOOK) {
			LocalBook localBook = (LocalBook) DownloadAble;
			index = getPositionByTypeAndId(BookShelfModel.EBOOK, localBook._id);
			int state = DownloadStateManager.getLocalBookState(localBook);
			int progress = 0;
			if (localBook.size > 0)
				progress = (int) (localBook.progress * 100 / localBook.size);

			Bundle bundle = new Bundle();
			bundle.putInt("index", index);
			bundle.putString("bookid", "" + localBook.book_id);

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
	public void onPopupWindowItemClick(String type, int position) {
		if (type.equals(TYPE_RIGHT_MENU)) {
			switch (position) {
			case 0:// 整理
				startActivity(new Intent(getActivity(), OrderingBookCaseActivity.class));
				break;
			case 1:// 搜索
					// 搜索
				Intent intent1 = new Intent(getActivity(), BookShelfSearchActivity.class);
				intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent1);
				break;
			}
		}

		else {

			switch (position) {

			case 0:// 已购

				ActivityUtils.startActivity(getActivity(), new Intent(getActivity(), BookcaseCloudActivity.class));
				break;
			case 1:// 畅读
				ActivityUtils.startActivity(getActivity(), new Intent(getActivity(), ChangDuActivity.class));
				break;

			case 2:// 借阅
				ActivityUtils.startActivity(getActivity(), new Intent(getActivity(), BorrowBookMainActivity.class));
				break;
			case 3:// 云盘
				ActivityUtils.startActivity(getActivity(), new Intent(getActivity(), BookcaseCloudDiskActivity.class));
				break;
			case 4:// 导入本地图书
				Intent intent = new Intent(FileBrowserActivity.INTENT_ACTION_SELECT_FILE, null, getActivity(), FileBrowserActivity.class);
				String bookpath = LocalUserSetting.getBookPath(getActivity());
				intent.putExtra(FileBrowserActivity.filterExtension, new String[] { "epub", "pdf" });
				intent.putExtra(FileBrowserActivity.startDirectoryParameter, bookpath);
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(R.anim.fade, R.anim.hold);
				break;

			// case 5:// 测试
			// startActivity(new Intent(getActivity(),
			// TestActivity.class));
			case 5:// 最近在读
				startActivity(new Intent(getActivity(), BookcaseOthersReadingActivity.class));
				// Intent intent1 = new Intent(getActivity(),
				// WebViewActivity.class);
				// intent1.putExtra(WebViewActivity.UrlKey,
				// "http://item.m.jd.com/product/11615131.html");
				// intent1.putExtra(WebViewActivity.TopbarKey, "true");
				// // intent1.putExtra(WebViewActivity.UrlKey,
				// // "http://item.m.jd.com/product/11615131.html");
				// startActivity(intent1);
				break;

			}
		}

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
				break;
			case 1:// 按名称排序
				LocalUserSetting.saveBookShelfModel(getActivity(), "Name");
				((LauncherActivity) getActivity()).showMainLayoutView(1);
				if (rightPopupWindow != null) {
					rightPopupWindow.dismiss();
				}
				break;
			case 2:// 按时间排序
				LocalUserSetting.saveBookShelfModel(getActivity(), "Time");
				((LauncherActivity) getActivity()).showMainLayoutView(1);
				if (rightPopupWindow != null) {
					rightPopupWindow.dismiss();
				}
				break;
			}

		}
	}

	@Override
	public void onLeftMenuClick() {
		if (leftPopupWindow != null) {
			leftPopupWindow.show(topBarView);
			if (leftPopupWindow.isShowing()) {
				topBarView.setLeftMenuVisiable(true, R.drawable.topbar_add_selected);
				topBarView.setRightMenuOneVisiable(false, R.drawable.topbar_menu, false);
			}
			MZLog.d("wangguodong", "xxxxxxxxxxxx");
		}
	}

	@Override
	public void onRightMenuOneClick() {
		MZLog.d("wangguodong", "######@!@@@@@@@" + rightPopupWindow == null ? "null" : "not null");
		if (rightPopupWindow != null) {
			rightPopupWindow.show(topBarView);
			if (rightPopupWindow.isShowing()) {
				// 左侧按钮隐藏
				topBarView.setLeftMenuVisiable(false, R.drawable.topbar_add_selected);
				// 点击的按钮显示“X”
				topBarView.setRightMenuOneVisiable(true, R.drawable.topbar_add_selected, false);
			}
		}

	}

	@Override
	public void onRightMenuTwoClick() {
		MZLog.d("wangguodong", "2222######@!@@@@@@@");

	}

	@Override
	public void onCenterMenuItemClick(int position) {

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		//
		// if (!models.get(position - 1).isDownloaded()) {
		//
		// BookShelfModel model = models.get(position - 1);
		//
		// DownloadedAble downloadedAble = null;
		//
		// if (model.getBookType().equals(BookShelfModel.EBOOK)) {
		// downloadedAble = LocalBook.getLocalBookByIndex(model
		// .getBookid());
		//
		// } else if (model.getBookType().equals(BookShelfModel.DOCUMENT)) {
		// downloadedAble = MZBookDatabase.instance.getLocalDocument(model
		// .getBookid());
		// MZLog.d("wangguodong", "点击document");
		// } else {
		// return;
		// }
		//
		// if (model.getDownload_state() == LocalBook.STATE_FAILED
		// || model.getDownload_state() == LocalBook.STATE_LOAD_FAILED) {
		//
		// model.setDownload_state(LocalBook.STATE_LOADING);
		// models.set(position -1 , model);
		//
		// Bundle bundle = new Bundle();
		// bundle.putInt("index", position - 1);
		// bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
		// bundle.putInt("progress", 0);
		// sendMessage(bundle);
		// DownloadHelper.restartDownload(getActivity(),
		// model.getBookType(), downloadedAble);
		//
		// } else if (model.getDownload_state() == LocalBook.STATE_LOAD_PAUSED)
		// {
		//
		// model.setDownload_state(LocalBook.STATE_LOADING);
		// models.set(position - 1, model);
		//
		// Bundle bundle = new Bundle();
		// bundle.putInt("index", position - 1);
		// bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
		// bundle.putInt("progress", -1);
		// sendMessage(bundle);
		// DownloadHelper.resumeDownload(getActivity(),
		// model.getBookType(), downloadedAble);
		// } else if (model.getDownload_state() == LocalBook.STATE_LOADING) {
		//
		// model.setDownload_state(LocalBook.STATE_LOAD_PAUSED);
		// models.set(position - 1, model);
		//
		// Bundle bundle = new Bundle();
		// bundle.putInt("index", position - 1);
		// bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_PAUSED);
		// bundle.putInt("progress", -1);
		// sendMessage(bundle);
		// DownloadHelper.stopDownload(getActivity(), model.getBookType(),
		// downloadedAble);
		// } else {
		// model.setDownload_state(LocalBook.STATE_LOADED);
		// models.set(position - 1, model);
		// Bundle bundle = new Bundle();
		// bundle.putInt("index", position - 1);
		// bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED);
		// bundle.putInt("progress", -1);
		// sendMessage(bundle);
		// }
		// return;
		// }
		//
		// BookShelfModel model = models.get(position - 1);
		// MZLog.d("cj", "model========>>"+model.getBookName() + "type====>>"+
		// model.getBookType());
		// if (model.getBookType().equals(BookShelfModel.EBOOK)) {
		// EBook ebook = MZBookDatabase.instance.getEBook(model.getBookid());
		// OpenBookHelper.openEBook(getActivity(), ebook.bookId);
		//
		// } else if (model.getBookType().equals(BookShelfModel.DOCUMENT)) {
		// Document doc = MZBookDatabase.instance.getDocument(model
		// .getBookid());
		// OpenBookHelper.openDocument(getActivity(), doc.documentId);
		//
		// }
	}

	public void sendMessage(Bundle bundle) {
		Message msg = new Message();
		msg.what = UPDATE_UI_MESSAGE;
		msg.obj = bundle;
		handler.sendMessage(msg);
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

	private void setCurrentDot(int positon) {
		if (positon < 0 || positon > 4 || currentIndex == positon) {
			return;
		}
		dots[positon].setEnabled(true);
		dots[currentIndex].setEnabled(false);
		currentIndex = positon;
	}

}
