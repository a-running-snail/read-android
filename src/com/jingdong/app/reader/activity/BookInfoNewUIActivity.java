package com.jingdong.app.reader.activity;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager.AddToCartListener;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager.CheckExistListener;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager.DelFromCartListener;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager.GetTotalCountListener;
import com.jingdong.app.reader.bookstore.buyborrow.UserBuyBorrowActivity;
import com.jingdong.app.reader.bookstore.sendbook.SendBookOrderActivity;
import com.jingdong.app.reader.bookstore.style.controller.BookInfoStyleController;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController.OnHeaderActionClickListener;
import com.jingdong.app.reader.bookstore.style.controller.RankingListViewStyleController;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadStateManager;
import com.jingdong.app.reader.client.DownloadThreadQueue.RefreshAble;
import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.BookCardItemEntity;
import com.jingdong.app.reader.entity.BookInforEDetail;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.extra.JDBook;
import com.jingdong.app.reader.entity.extra.JDBookComments;
import com.jingdong.app.reader.entity.extra.JDBookDetail;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.entity.extra.ReadedUserInfo;
import com.jingdong.app.reader.entity.extra.SimplifiedDetail;
import com.jingdong.app.reader.entity.extra.StoreBook;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OlineDesUtils;
import com.jingdong.app.reader.onlinereading.OnlineReadManager;
import com.jingdong.app.reader.pay.OnlinePayActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListCommentsActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.DateUtil;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.JavaUtil;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.NetWorkUtils.NetworkConnectType;
import com.jingdong.app.reader.util.OnLinePayTools;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.UserGuiderUtil;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.SharePopupWindow;
import com.jingdong.app.reader.view.SharePopupWindow.onPopupWindowItemClickListener;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
@SuppressLint("InflateParams")
@SuppressWarnings("deprecation")
public class BookInfoNewUIActivity extends BaseActivityWithTopBar implements RefreshAble, onPopupWindowItemClickListener {

	public static final String BookIdKey = "bookid";
	public static final String[] CHANGDUCARD = { "30104685", "30104684", "30104683", "30120439" };

	private JDBookInfo bookInfo = null;
	private LinearLayout headerView = null;
	private LinearLayout saleInfoView = null;
	private LinearLayout basicInfoView = null;

	private LinearLayout bookCommentsView = null;
	private LinearLayout bookNotesView = null;
	private LinearLayout bookReadedView = null;//读过此书的人

	private LinearLayout bookOthersLikeView = null;

	private LinearLayout containerLayout = null;
	private long bookid = 0;
	private int currentIndex = 0;
	public static final int DEFAULT_BOOK_COMMENTS_COUNT = 3;
	private boolean isWish = false;
	private boolean isPurchasable;// 是否可购买

	private static SharePopupWindow sharePopupWindow = null;

	private Bitmap bitmap;

	private int currentOpenTime = 0;
	private static final int DEFAULT_LEFT_MARGIN = 0; // 整个View左边距
	private static final int DEFAULT_RIGHT_MARGIN = 0; // 整个View右边距

	private static final int DEFAULT_BOOK_LEFT_MARGIN = 16; // 底部bookView左边距
	private static final int DEFAULT_BOOK_RIGHT_MARGIN = 15; // 底部bookView右边距
	private static final int DEFAULT_BOTTOM_MARGIN = 0; // 整个View底部边距
	private static final int DEFAULT_TOP_MARGIN = 0; // 整个View顶部边距

	private static final int DEFAULT_FIRST_ROW_TOP_MARGIN = 0; // 第一行离顶部view的距离

	private static final int DEFAULT_HORIZONTAL_DIVIDER_WIDTH = 16; //
	// 书籍中间的水平空隙宽度
	private static final int DEFAULT_VERTICAL_DIVIDER_WIDTH = 16; // 书籍中间的垂直空隙宽度
	private static final int DEFAULT_VIEW_BACKGROUND = 0x00ffffff;

	private static final String PAPER_BOOK_SHARE_URL = URLText.JD_PAPER_BOOK_SHARE_URL+"wareId=";
	private static final String EBOOK_SHARE_URL = "http://e.m.jd.com/ebook/";
	
	PopupWindow mPopupWindow ;

	class Recommend {
		String name;
		String author;
		long ebookId;
		float jdPrice;
		String imageUrl;
	}

	private List<Recommend> recommends = new ArrayList<Recommend>();

	// 界面按钮状态开始

	boolean isShowBookCartBtn = false;
	boolean isShowBookCommentBtn = false;
	boolean isShowPaperBookBtn = false;

	// 界面底部按钮状态结束

	private LinearLayout changduBtn = null;
	private LinearLayout leftReadBtn = null;
	private TextView leftReadText = null;
	private ImageView left_icon = null;

	private LinearLayout buyBtn = null;
	private TextView centerButtonInfo = null, center_button_buy_info = null;
	private TextView centerInfo = null;

	private LinearLayout centerReadBtn = null;
	private LinearLayout centerDownloadBtn = null;
	private TextView centerDownloadinfo = null;

	private LinearLayout borrowBtn = null;
	private LinearLayout tryReadBtn = null;
	private LinearLayout rightReadBtn = null;
	private LinearLayout mUserBorrow = null;
	private TextView mUserBorrowText = null;
	private LinearLayout bottomLayout = null;

	private FrameLayout tabone = null;
	private FrameLayout tabtwo = null;
	private FrameLayout tabthree = null;

	protected final static int UPDATE_UI_MESSAGE = 0;
	protected final static int SHOW_PROGRESS_DIALOG = 1;
	protected final static int CANCEL_SHOW_PROGRESS_DIALOG = 2;

	protected final static int PROGRESS_FAILED = -1; // 下载失败的进度

	private static final int IO_BUFFER_SIZE = 2 * 1024;

	private ProgressDialog dialog = null;
	
	private View leftLine = null;
	private View rightLine = null;
	
	List<JDBookComments> jDBookCommentslist =new ArrayList<JDBookComments>();

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (!needHandleMessage)
				return;
			if (msg.what == UPDATE_UI_MESSAGE) {
				updateBottomProgress(msg.arg2);
			}

//			if (msg.what == SHOW_PROGRESS_DIALOG) {
//				dialog = CustomProgreeDialog.instance(BookInfoNewUIActivity.this, "下载完成，正在打开书籍，稍后...");
//				if (dialog != null)
//					dialog.show();
//			}
//			if (msg.what == CANCEL_SHOW_PROGRESS_DIALOG) {
//				if (dialog != null && dialog.isShowing())
//					dialog.dismiss();
//
//			}

		};

	};
	private boolean needHandleMessage = true;
	private int perSearchCount = 10;
	private String type;

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookinfo);

		initView();
		bookid = getIntent().getLongExtra(BookIdKey, 0);
		type = getIntent().getStringExtra("type");
		if (!NetWorkUtils.isNetworkConnected(BookInfoNewUIActivity.this)) {
			// Toast.makeText(this, "网络木有连接哦，请检查后重试!",
			// Toast.LENGTH_LONG).show();
			finish();
		}
		
		// if (bookid != 0) {
		// getBookInfo(bookid);
		// getBookOthersLike(bookid);
		//
		// } else {
		// Toast.makeText(this, "抱歉,您要看的书籍不存在哦!", Toast.LENGTH_LONG).show();
		// finish();
		// }
		// needHandleMessage = true;
		// if (savedInstanceState != null && mWeiboShareAPI != null) {
		// mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
		// }

	}
	
	@SuppressWarnings("unused")
	@Override
	public void onWindowFocusChanged(boolean hasFocus) { 
		if(bookInfo!=null && bookInfo.detail!=null && bookInfo.detail.isBuyBorrow && hasFocus && mUserBorrow!=null && mUserBorrow.getVisibility()==View.VISIBLE) {
			if (!LocalUserSetting.isBorrowBookGuidShow(this)) {
				ImageView imageView = new ImageView(this);
				imageView.setImageResource(R.drawable.user_borrow_guide);
				UserGuiderUtil userGuiderUtil = new UserGuiderUtil(this, imageView, false,true, true, false, null);
				LocalUserSetting.saveBorrowBookGuidShow(this);
			}
		}
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateBookCart();
		currentOpenTime = 0;

		if (!NetWorkUtils.isNetworkConnected(BookInfoNewUIActivity.this)) {
			// Toast.makeText(this, "网络木有连接哦，请检查后重试!",
			// Toast.LENGTH_LONG).show();
			finish();
		}
		if (bookid != 0) {
			getBookInfo(bookid,type);
			getBookOthersLike(bookid);
		} else {
			Toast.makeText(this, "抱歉,您要看的书籍不存在哦!", Toast.LENGTH_LONG).show();
			finish();
		}
		needHandleMessage = true;
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_bookinfo));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_bookinfo));
	}
	
	private void getBookInfo(long bookid,String type) {
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getBookInfoParams(bookid,type), true,
				new MyAsyncHttpResponseHandler(BookInfoNewUIActivity.this) {

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
								bookInfo = GsonUtils.fromJson(result, JDBookInfo.class);
								
								if(null == bookInfo) {
									return;
								}
								
								if(null == bookInfo.detail) {
									return;
								}
								
								// isPurchasable =
								// "暂无报价".equals(bookInfo.detail.priceMessage) ?
								// false : true;
								isPurchasable = bookInfo.detail.jdPrice != 0;
								if (bookInfo != null && bookInfo.detail != null) {
									if(jDBookCommentslist!=null && jDBookCommentslist.size()>0)
										jDBookCommentslist.clear();
									if (bookInfo.detail.isEBook) {
										getBookComments("ebook", bookInfo.detail.bookId, 1);
									} else {
										getBookComments("paperBook", bookInfo.detail.paperBookId, 1);
									}
									checkAuth();
									updateHeaderView();
									updateSaleInfoView();
									updateBasicInfoView();
									//更新读过本书的读者列表
									getBookReaded();
									if (!isAuthorBookListViewAdded && bookInfo.detail.author != null && !bookInfo.detail.author.equals("佚名")) {
										getAuthorBookList(bookInfo.detail.author);
									} else {
										if (!hasAdjustOtherLikeLayoutParams) {
											adjustOtherLikeLayoutParams();
										}
									}
								}
							} else {
								Toast.makeText(BookInfoNewUIActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
	}

	private int index = 0;

	protected void getAuthorBookList(final String authorList) {
		if (TextUtils.isEmpty(authorList))
			return;
		if (authorList.contains("，")) {
			String[] authorArr = authorList.split("，");
			processMultiAuthorBookList(authorArr);
		} else if (authorList.contains(",")) {
			String[] authorArr = authorList.split(",");
			processMultiAuthorBookList(authorArr);
		} else if (authorList.contains(";")) {
			String[] authorArr = authorList.split(";");
			processMultiAuthorBookList(authorArr);
		} else if (authorList.contains("；")) {
			String[] authorArr = authorList.split("；");
			processMultiAuthorBookList(authorArr);
		} else {
			processSingleAuthor(authorList);
		}
	}

	/**
	 * 
	 * @Title: processSingleAuthor
	 * @param @param author
	 * @return void
	 * @throws
	 */
	private void processSingleAuthor(final String author) {
		if (author == null || TextUtils.isEmpty(author)) {
			return;
		}
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSearchStoreEbookParams(currentSearchPage + "", perSearchCount + "", author, ""),
				new MyAsyncHttpResponseHandler(BookInfoNewUIActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						Toast.makeText(BookInfoNewUIActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String result = new String(responseBody);
						if(!TextUtils.isEmpty(result)) {
							JDBook ebook = GsonUtils.fromJson(result, JDBook.class);
							if(null != ebook && ebook.bookList!=null) {
								setupAuthorBookList(new String[] { author }, ebook.bookList);
							}
						}
					}
				});
	}

	private void processMultiAuthorBookList(final String[] authorArr) {
		if (authorArr == null || authorArr.length == 0)
			return;
		final List<JDBookDetail> bookList = new ArrayList<JDBookDetail>();
		for (int i = 0; i < authorArr.length; i++) {
			String author = authorArr[i];
			WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSearchStoreEbookParams(currentSearchPage + "", perSearchCount + "", author, ""),
					new MyAsyncHttpResponseHandler(BookInfoNewUIActivity.this) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							Toast.makeText(BookInfoNewUIActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
							if(null == responseBody) {
								return;
							}
							
							String result = new String(responseBody);
							
							if(TextUtils.isEmpty(result)) {
								return;
							}
							
							JDBook ebook = GsonUtils.fromJson(result, JDBook.class);
							
							if(null == ebook) {
								return;
							}
							
							if(ebook != null && ebook.bookList != null && ebook.bookList.size() > 0){
								bookList.addAll(ebook.bookList);
							}
							if (index == authorArr.length - 1) {
								setupAuthorBookList(authorArr, bookList);
							}
							index++;
						}
					});
		}
	}

	/**
	 * @param authorArr
	 * 
	 * @Title: setupAuthorBookList
	 * @Description: 设置作者其他书单
	 * @param @param ebook
	 * @return void
	 * @throws
	 */
	protected void setupAuthorBookList(String[] authorArr, final List<JDBookDetail> bookList) {
		MZLog.d("D", "before::bookList size=" + bookList.size() + "\n" + GsonUtils.toJson(bookList));
		if (bookList == null || bookList.size() == 0) {
			adjustOtherLikeLayoutParams();
			return;
		}
		// 1.过滤作者
		List<JDBookDetail> filterBookList = filterAuthorList(authorArr, bookList);
		MZLog.d("D", "aifer::bookList size=" + filterBookList.size() + "\n" + GsonUtils.toJson(bookList));

		String moretxt = filterBookList.size() > 3 ? "更多" : "";
		List<StoreBook> storeBookList = jDBookDetail2StoreBook(filterBookList);
		if (storeBookList.size() == 0) {
			adjustOtherLikeLayoutParams();
			return;
		}
		mAuthorBookListView = RankingListViewStyleController.getRankingListStyleView(BookInfoNewUIActivity.this, "该作者其他作品", moretxt, 3, 1, storeBookList,
				new OnHeaderActionClickListener() {

					@Override
					public void onHeaderActionClick() {
						Intent intent = new Intent(BookInfoNewUIActivity.this, BookStoreBookListActivity.class);

						intent.putExtra("list_type", BookStoreBookListActivity.TYPE_AUTHOR_OTHER_BOOKS);
						intent.putExtra("keys", bookInfo.detail.author);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				});

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		float margin = getResources().getDimension(R.dimen.book_detail_cell_margin);
		params.setMargins(0, (int) margin, 0, (int) margin);
		mAuthorBookListView.setLayoutParams(params);
		containerLayout.addView(mAuthorBookListView, containerLayout.getChildCount() - 2);
		isAuthorBookListViewAdded = true;
		hasAdjustOtherLikeLayoutParams = true;
	}

	private List<JDBookDetail> filterAuthorList(String[] currentAuthors, List<JDBookDetail> bookList) {
		List<JDBookDetail> bookDetails = new ArrayList<JDBookDetail>();
		for (JDBookDetail jdBookDetail : bookList) {
			String author = jdBookDetail.getAuthor();
			MZLog.d("J", "author::" + author);
			if (author.contains(",")) {
				fliterProcess(",", currentAuthors, bookDetails, jdBookDetail);
			} else if (author.contains("，")) {
				fliterProcess("，", currentAuthors, bookDetails, jdBookDetail);
			} else if (author.contains(";")) {
				fliterProcess(";", currentAuthors, bookDetails, jdBookDetail);
			} else if (author.contains("；")) {
				fliterProcess("；", currentAuthors, bookDetails, jdBookDetail);
			} else {// 单个作者
				for (int i = 0; i < currentAuthors.length; i++) {
					String currentAuthor = currentAuthors[i];
					if (currentAuthor != null && currentAuthor.equals(author)) {
						bookDetails.add(jdBookDetail);
					}
				}
			}
		}
		return bookDetails;
	}

	/**
	 * 
	 * @Title: fliterProcess
	 * @Description: 处理多个作者
	 * @param @param split
	 * @param @param currentAuthors
	 * @param @param bookDetails
	 * @param @param jdBookDetail
	 * @param @param author
	 * @return void
	 * @throws
	 */
	private void fliterProcess(String split, String[] currentAuthors, List<JDBookDetail> bookDetails, JDBookDetail jdBookDetail) {
		String[] auhorArr = jdBookDetail.getAuthor().split(split);
		if (JavaUtil.isArrayEqual(currentAuthors, auhorArr)) {
			bookDetails.add(jdBookDetail);
		} else {
			// 遍历当前图书的作者
			for (int i = 0; i < currentAuthors.length; i++) {
				String currentAuthor = currentAuthors[i];
				for (int j = 0; j < auhorArr.length; j++) {
					String bookAuthor = auhorArr[j];
					if (currentAuthor != null && bookAuthor != null) {
						if (currentAuthor.equals(bookAuthor)) {
							bookDetails.add(jdBookDetail);
						}
					}
				}
			}
		}
	}

	boolean hasAdjustOtherLikeLayoutParams = false;

	private void adjustOtherLikeLayoutParams() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		float margin = getResources().getDimension(R.dimen.book_detail_cell_margin);
		params.setMargins(0, (int) margin, 0, 0);
		bookOthersLikeView.setLayoutParams(params);
		hasAdjustOtherLikeLayoutParams = true;
	}

	private List<StoreBook> jDBookDetail2StoreBook(List<JDBookDetail> bookList) {
		ArrayList<StoreBook> storeBooks = new ArrayList<StoreBook>();
		for (int i = 0; i < bookList.size(); i++) {
			JDBookDetail bookDetail = bookList.get(i);
			StoreBook book = new StoreBook();
			book.setEBook(bookDetail.isEBook());
			book.setEbookId(bookDetail.getEbookId());
			book.setPaperBookId(bookDetail.getPaperBookId());
			book.setImageUrl(bookDetail.getImageUrl());
			book.setName(bookDetail.getName());
			book.setAuthor(bookDetail.getAuthor());
			book.setInfo(bookDetail.getInfo());

			// book.setAlreadyBuy(bookDetail.isAlreadyBuy());
			// book.setBookId(bookDetail.getEbookId());
			//
			// book.setBorrow(bookDetail.isBorrow());
			// book.setBorrowEndTime(bookDetail.getBorrowEndTime());
			// book.setBorrowStartTime(bookDetail.getBorrowStartTime());
			// book.setBuy(bookDetail.isBuy());
			// book.setCategoryPath(bookDetail.getCategoryPath());
			// book.setFileSize(bookDetail.getFileSize());
			// book.setFluentRead(bookDetail.isFluentRead());
			// book.setFormat(bookDetail.getFormat());
			// book.setFree(bookDetail.isFree());
			// book.setGood(bookDetail.getGood());
			// book.setJdPrice(bookDetail.getJdPrice());
			// book.setLargeImageUrl(bookDetail.getLargeImageUrl());
			// book.setOrderId(Long.parseLong(bookDetail.getOrderId()));
			// book.setPrice(bookDetail.getPrice());
			// book.setPriceMessage(bookDetail.getPriceMessage());
			// book.setPublisher(bookDetail.getPublisher());
			// book.setStar(bookDetail.getStar());
			if (bookDetail.getEbookId() != bookid) {
				storeBooks.add(book);
			}
		}
		return storeBooks;
	}

	/**
	 * 检查授权设置按钮显示状态
	 */
	public void checkAuth() {
		if (bookInfo.detail.isEBook && ("epub".equalsIgnoreCase(bookInfo.detail.format) || "pdf".equalsIgnoreCase(bookInfo.detail.format))) {
			centerDownloadinfo.setText("");

			if (bookInfo.detail.paperBookId > 0) {
				isShowPaperBookBtn = true;// 显示购买纸质版连接
			}

			//免费书
			if (bookInfo.detail.isFree) {

				leftLine.setVisibility(View.GONE);
				rightLine.setVisibility(View.GONE);
				changduBtn.setVisibility(View.GONE);
				leftReadBtn.setVisibility(View.GONE);

				buyBtn.setVisibility(View.GONE);
				centerReadBtn.setVisibility(View.VISIBLE);
				// 按钮背景显示
				{
					centerInfo.setTextColor(getResources().getColor(R.color.bg_main));
					tabone.setBackgroundColor(getResources().getColor(R.color.red_sub));
					tabtwo.setBackgroundColor(getResources().getColor(R.color.red_sub));
					tabthree.setBackgroundColor(getResources().getColor(R.color.red_sub));
				}

				borrowBtn.setVisibility(View.GONE);
				tryReadBtn.setVisibility(View.GONE);
				rightReadBtn.setVisibility(View.GONE);
				if (bookInfo.detail.isAlreadyBuy /*tmj || !bookInfo.detail.isUserFluentReadAddToCard*/) {
					isShowBookCartBtn = false;
					isShowBookCommentBtn = true;
				}
			}
			//非免费书
			else {
				@SuppressWarnings("static-access")
				List<String> changducardList = Arrays.asList(this.CHANGDUCARD);
				//该详情是否是畅读卡
				if (changducardList.contains(String.valueOf(bookInfo.detail.ebookId))) {
					isShowBookCartBtn = true;
					isShowBookCommentBtn = false;
					buyBtn.setVisibility(View.VISIBLE);
					centerButtonInfo.setText("￥" + bookInfo.detail.jdPrice);
					centerReadBtn.setVisibility(View.GONE);
					borrowBtn.setVisibility(View.GONE);
					tryReadBtn.setVisibility(View.GONE);
					rightReadBtn.setVisibility(View.GONE);
					centerButtonInfo.setTextColor(getResources().getColor(R.color.bg_main));
					center_button_buy_info.setTextColor(getResources().getColor(R.color.bg_main));
					// tabone.setBackgroundColor(getResources().getColor(R.color.red_sub));
					tabtwo.setBackgroundColor(getResources().getColor(R.color.red_sub));
				} 
				//是否购买了该书
				else if (bookInfo.detail.isAlreadyBuy) {

					isShowBookCartBtn = false;
					isShowBookCommentBtn = true;
					leftLine.setVisibility(View.GONE);
					rightLine.setVisibility(View.GONE);
					changduBtn.setVisibility(View.GONE);
					leftReadBtn.setVisibility(View.GONE);

					buyBtn.setVisibility(View.GONE);
					// centerButtonInfo.setVisibility(View.GONE);
					centerReadBtn.setVisibility(View.VISIBLE);

					// 按钮背景显示
					{
						centerInfo.setTextColor(getResources().getColor(R.color.bg_main));
						tabone.setBackgroundColor(getResources().getColor(R.color.red_sub));
						tabtwo.setBackgroundColor(getResources().getColor(R.color.red_sub));
						tabthree.setBackgroundColor(getResources().getColor(R.color.red_sub));
					}

					borrowBtn.setVisibility(View.GONE);
					tryReadBtn.setVisibility(View.GONE);
					rightReadBtn.setVisibility(View.GONE);
					mUserBorrow.setVisibility(View.GONE);

				}
				//是否可以畅读
				else if (bookInfo.detail.isUserCanFluentRead) {
					isShowBookCommentBtn = false;
					isShowBookCartBtn = true;
					//用户是否畅读了该书
					if (bookInfo.detail.isFluentRead) {
						changduBtn.setVisibility(View.GONE);
						leftReadBtn.setVisibility(View.VISIBLE);

						// 按钮背景显示
						{
							leftReadText.setTextColor(getResources().getColor(R.color.bg_main));
							left_icon.setImageResource(R.drawable.badge_vip_white);
							// centerInfo.setTextColor(getResources().getColor(R.color.bg_main));
							centerButtonInfo.setTextColor(getResources().getColor(R.color.text_main));
							center_button_buy_info.setTextColor(getResources().getColor(R.color.text_main));
							tabone.setBackgroundColor(getResources().getColor(R.color.red_sub));
							tabtwo.setBackgroundColor(getResources().getColor(R.color.bg_main));
							// tabthree.setBackgroundColor(getResources().getColor(R.color.red_sub));
						}
					} else {
						changduBtn.setVisibility(View.GONE);
						leftReadBtn.setVisibility(View.GONE);

						// 按钮背景显示
						{
							leftReadText.setTextColor(getResources().getColor(R.color.bg_main));
							// left_icon.setImageResource(R.drawable.badge_vip_white);
							// centerInfo.setTextColor(getResources().getColor(R.color.bg_main));
							centerButtonInfo.setTextColor(getResources().getColor(R.color.bg_main));
							center_button_buy_info.setTextColor(getResources().getColor(R.color.bg_main));
							// tabone.setBackgroundColor(getResources().getColor(R.color.red_sub));
							tabtwo.setBackgroundColor(getResources().getColor(R.color.red_sub));
							// tabthree.setBackgroundColor(getResources().getColor(R.color.red_sub));
						}
					}

					buyBtn.setVisibility(View.VISIBLE);
					centerButtonInfo.setText(bookInfo.detail.priceMessage);
					centerReadBtn.setVisibility(View.GONE);

					borrowBtn.setVisibility(View.GONE);
					tryReadBtn.setVisibility(View.GONE);
					rightReadBtn.setVisibility(View.GONE);

					handleBorrowButtonStatus();
				}

				else {
					// 没有阅读权限

					isShowBookCartBtn = true;
					isShowBookCommentBtn = false;

					if (bookInfo.detail.isFluentRead) {
						changduBtn.setVisibility(View.VISIBLE);
						leftReadBtn.setVisibility(View.GONE);
					} else {
						changduBtn.setVisibility(View.GONE);
						leftReadBtn.setVisibility(View.GONE);
					}

					buyBtn.setVisibility(View.VISIBLE);
					centerButtonInfo.setText(bookInfo.detail.priceMessage);
					centerReadBtn.setVisibility(View.GONE);

					// 按钮背景显示
					// centerInfo.setTextColor(getResources().getColor(R.color.bg_main));
					if (isPurchasable) {
						centerButtonInfo.setTextColor(getResources().getColor(R.color.bg_main));
						center_button_buy_info.setTextColor(getResources().getColor(R.color.bg_main));
						// tabone.setBackgroundColor(getResources().getColor(R.color.red_sub));
						tabtwo.setBackgroundColor(getResources().getColor(R.color.red_sub));
						// tabthree.setBackgroundColor(getResources().getColor(R.color.red_sub));
					}

					borrowBtn.setVisibility(View.GONE);
					tryReadBtn.setVisibility(View.GONE);
					rightReadBtn.setVisibility(View.GONE);
					
					handleBorrowButtonStatus();
				}
			}
		} else {
			isShowBookCartBtn = false;
			isShowBookCommentBtn = false;
			isShowPaperBookBtn = true;
			changduBtn.setVisibility(View.GONE);
			leftReadBtn.setVisibility(View.GONE);
			buyBtn.setVisibility(View.GONE);
			centerReadBtn.setVisibility(View.GONE);
			borrowBtn.setVisibility(View.GONE);
			tryReadBtn.setVisibility(View.GONE);
			rightReadBtn.setVisibility(View.GONE);
			mUserBorrow.setVisibility(View.GONE);
			leftLine.setVisibility(View.GONE);
			rightLine.setVisibility(View.GONE);
		}
		
	}
	
	/**
	* @Description: 处理书城借阅·试读按钮显示状态
	* @author xuhongwei1
	* @date 2015年12月1日 下午5:56:46 
	* @throws 
	*/ 
	private void handleBorrowButtonStatus() {
		if (bookInfo.detail.isBorrow) { //是否提供书城免费借阅
			if (bookInfo.detail.isAlreadyBorrow) { //用户是否已经借阅该书
				if (!TextUtils.isEmpty(bookInfo.detail.userBorrowEndTime)) {
					if (TimeFormat.formatStringTime(bookInfo.detail.userBorrowEndTime) > TimeFormat.formatStringTime(bookInfo.detail.currentTime)) {
						borrowBtn.setVisibility(View.GONE);
						rightReadBtn.setVisibility(View.VISIBLE);
					} else {
//						borrowBtn.setVisibility(View.GONE);
//						rightReadBtn.setVisibility(View.GONE);
						handleUserBorrowButtonStatus();
					}
				}
			}else {
				int daysString = bookInfo.detail.canBorrowDays;
				borrowBtn.setVisibility(View.VISIBLE);
				TextView borrowinfo = (TextView) borrowBtn.findViewById(R.id.borrowInfo);
				borrowinfo.setText("免费借阅" + daysString + "天");
				rightReadBtn.setVisibility(View.GONE);
			}
		} else {
			handleUserBorrowButtonStatus();
		}
		
	}

	/**
	* @Description: 处理用户借阅·试读按钮状态
	* @author xuhongwei1
	* @date 2015年12月1日 下午6:32:49 
	* @throws 
	*/ 
	private void handleUserBorrowButtonStatus() {
		if(bookInfo.detail.isBuyBorrow) {
			mUserBorrow.setVisibility(View.VISIBLE);
			borrowBtn.setVisibility(View.GONE);
			rightReadBtn.setVisibility(View.GONE);
			tryReadBtn.setVisibility(View.GONE);
		}else {
			// 可以试读
			mUserBorrow.setVisibility(View.GONE);
			if (bookInfo.detail.isTryRead && !TextUtils.isEmpty(bookInfo.detail.tryDownLoadUrl)) {
				tryReadBtn.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public void getBookOthersLike(long bookid) {
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getBookOthersLikeParams(bookid), true, new MyAsyncHttpResponseHandler(
				BookInfoNewUIActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				updateOthersLikeView();
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

				String result = new String(responseBody);
				MZLog.d("AAA", result);

				try {

					JSONObject object = new JSONObject(result);
					JSONArray recommendsObject = object.optJSONArray("recommend");

					if (recommendsObject != null && recommendsObject.length() > 0) {
						recommends.clear();
						for (int i = 0; i < recommendsObject.length(); i++) {
							Recommend recommend = GsonUtils.fromJson(recommendsObject.getString(i), Recommend.class);
							recommends.add(recommend);
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				updateOthersLikeView();

			}
		});
	}

	public void updateBottomProgress(int progress) {

		if (centerButtonInfo != null) {
			leftLine.setVisibility(View.GONE);
			rightLine.setVisibility(View.GONE);
			mUserBorrow.setVisibility(View.GONE);
			changduBtn.setVisibility(View.GONE);
			leftReadBtn.setVisibility(View.GONE);
			buyBtn.setVisibility(View.GONE);
			centerReadBtn.setVisibility(View.GONE);
			centerDownloadBtn.setVisibility(View.VISIBLE);
			// 按钮背景显示
			{
				tabone.setBackgroundColor(getResources().getColor(R.color.bg_main));
				tabtwo.setBackgroundColor(getResources().getColor(R.color.bg_main));
				tabthree.setBackgroundColor(getResources().getColor(R.color.bg_main));
			}
			borrowBtn.setVisibility(View.GONE);
			tryReadBtn.setVisibility(View.GONE);
			rightReadBtn.setVisibility(View.GONE);

			if (progress < 0) {
				ToastUtil.showToastInThread("下载失败..", Toast.LENGTH_SHORT);
			} else if (progress < 100) {
				centerDownloadinfo.setText("下载中:" + progress + "%");
			} else {
				leftLine.setVisibility(View.VISIBLE);
				rightLine.setVisibility(View.VISIBLE);
				centerDownloadinfo.setText("下载完成");
				centerDownloadinfo.setVisibility(View.GONE);
				checkAuth();
			}

		}

	}

	public void initView() {

		getTopBarView().setRightMenuOneVisiable(true, R.drawable.btn_bar_share, true);
		getTopBarView().setRightMenuTwoVisiable(true, R.drawable.topbar_cart);

		containerLayout = (LinearLayout) findViewById(R.id.holder);

		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
		// 底部状态栏
		changduBtn = (LinearLayout) findViewById(R.id.changdu_btn);
		leftReadBtn = (LinearLayout) findViewById(R.id.left_read_btn);
		leftReadText = (TextView) findViewById(R.id.left_read);
		left_icon = (ImageView) findViewById(R.id.left_icon);

		buyBtn = (LinearLayout) findViewById(R.id.buy_btn);
		centerButtonInfo = (TextView) findViewById(R.id.center_button_info);
		center_button_buy_info = (TextView) findViewById(R.id.center_button_buy_info);
		centerReadBtn = (LinearLayout) findViewById(R.id.center_read_btn);
		centerInfo = (TextView) findViewById(R.id.info);

		centerDownloadBtn = (LinearLayout) findViewById(R.id.center_download);
		centerDownloadinfo = (TextView) findViewById(R.id.center_download_info);

		borrowBtn = (LinearLayout) findViewById(R.id.borrow_btn);
		tryReadBtn = (LinearLayout) findViewById(R.id.try_read_btn);
		rightReadBtn = (LinearLayout) findViewById(R.id.right_read_btn);
		mUserBorrow = (LinearLayout) findViewById(R.id.mUserBorrow);
		mUserBorrowText = (TextView) findViewById(R.id.mUserBorrowText);
		bottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);

		tabone = (FrameLayout) findViewById(R.id.tab_one);
		tabtwo = (FrameLayout) findViewById(R.id.tab_two);
		tabthree = (FrameLayout) findViewById(R.id.tab_three);
		
		leftLine = (View)findViewById(R.id.leftLine);
		rightLine = (View)findViewById(R.id.rightLine);
		
		mUserBorrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent userread = new Intent(BookInfoNewUIActivity.this, UserBuyBorrowActivity.class);
				userread.putExtra("bookInfo", bookInfo);
				startActivity(userread);
			}
		});

		bottomLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 防止后面View响应点击事件

			}
		});

		OnClickListener onClick = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isPurchasable) {
					Toast.makeText(BookInfoNewUIActivity.this, "此书暂不支持购买", Toast.LENGTH_LONG).show();
					return;
				}
				TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "购买");
				if (!LoginUser.isLogin()) {
					Intent it = new Intent(BookInfoNewUIActivity.this, LoginActivity.class);
					startActivity(it);
					return;
				}
				// 以下添加购买逻辑
				if (OnlinePayActivity.payidList == null)
					OnlinePayActivity.payidList = new ArrayList<String>();
				else
					OnlinePayActivity.payidList.clear();
				OnlinePayActivity.payidList.add("" + bookInfo.detail.bookId);
				gotoEbookPay();

			}
		};

		buyBtn.setOnClickListener(onClick);

		center_button_buy_info.setOnClickListener(onClick);
		centerButtonInfo.setOnClickListener(onClick);
		changduBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!LoginUser.isLogin()) {
					Intent it = new Intent(BookInfoNewUIActivity.this, LoginActivity.class);
					startActivity(it);
					return;
				}
				TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "畅读");
				Intent intent = new Intent(BookInfoNewUIActivity.this, WebViewActivity.class);
				intent.putExtra(WebViewActivity.UrlKey, "http://e.m.jd.com/readCard.html");
				intent.putExtra(WebViewActivity.TopbarKey, true);
				intent.putExtra(WebViewActivity.BrowserKey, false);
				intent.putExtra(WebViewActivity.TitleKey, "购买畅读卡");
				startActivity(intent);
			}
		});

		leftReadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!LoginUser.isLogin()) {
					Intent it = new Intent(BookInfoNewUIActivity.this, LoginActivity.class);
					startActivity(it);
					return;
				}
				TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "畅读");
				addToChangduList();
			}
		});

		centerReadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!LoginUser.isLogin()) {
					Intent it = new Intent(BookInfoNewUIActivity.this, LoginActivity.class);
					startActivity(it);
					return;
				}
				TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "阅读");
				readBook();
			}
		});

		tryReadBtn.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("unused")
			@Override
			public void onClick(View v) {
				TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "试读");
				boolean notExistLocal = false;
				boolean isPause = false;
				boolean isFailed = false;
				boolean inWaiting = false;
				boolean isDownloaded = false;
				String pathString = null;

				LocalBook allLocalBooks = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());
				if (null == allLocalBooks)
					notExistLocal = true;
				else {
					notExistLocal = false;
					pathString = allLocalBooks.dir;
					if (!allLocalBooks.source.equals(LocalBook.SOURCE_TRYREAD_BOOK) && !allLocalBooks.source.equals(LocalBook.SOURCE_BUYED_BOOK)) {
						isFailed = true;

					} else {
						int state = DownloadStateManager.getLocalBookState(allLocalBooks);
						if (LocalBook.SOURCE_TRYREAD_BOOK.equals(allLocalBooks.source)) {
							if (state == DownloadStateManager.STATE_LOADED && allLocalBooks.size > 0) {
								isDownloaded = true;

							} else if (state == DownloadStateManager.STATE_LOADING) {
								if (allLocalBooks.state == LocalBook.STATE_LOADING || allLocalBooks.state == LocalBook.STATE_LOAD_READY) {
									inWaiting = true;
								}
								if (allLocalBooks.state == LocalBook.STATE_LOAD_PAUSED) {
									isPause = true;
								}
							} else {
								isFailed = true;
							}
						} else {
							isFailed = true;
						}
					}

				}

				if (notExistLocal) {
					OrderEntity orderEntity = OrderEntity.FromJDBooKInfo2OrderEntity(bookInfo.detail);
					DownloadTool.download((Activity) BookInfoNewUIActivity.this, orderEntity, null, false, LocalBook.SOURCE_TRYREAD_BOOK, 0, true, null, false);
				} else if (isDownloaded) {

					MZLog.d("wangguodong", "书籍已经存在本地了，直接打开");
					// 打开阅读
					OpenBookHelper.openEBook(BookInfoNewUIActivity.this, bookInfo.detail.bookId);

				} else if (isFailed) {

					if (allLocalBooks.source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {

						final String pathStr = pathString;
						DialogManager.showCommonDialog(BookInfoNewUIActivity.this, "提示", "您的书架上已有畅读本，是否下载试读本覆盖它？", "确定", "取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
										case DialogInterface.BUTTON_POSITIVE:
											if (!TextUtils.isEmpty(pathStr)) {
												MZLog.d("wangguodong", "删除书架已经存在的版本..." + pathStr);
												IOUtil.deleteFile(new File(pathStr));
											}

											LocalBook localBook = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());
											localBook.progress = 0;
											localBook.state = LocalBook.STATE_LOAD_PAUSED;
											localBook.source = LocalBook.SOURCE_TRYREAD_BOOK;
											localBook.bookUrl = bookInfo.detail.tryDownLoadUrl;
											localBook.size = -1;
											localBook.save();
											MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, localBook.source);
											localBook.start(BookInfoNewUIActivity.this);
											break;
										case DialogInterface.BUTTON_NEGATIVE:

											break;
										default:
											break;
										}
										dialog.dismiss();
									}
								});

					}

					else {
						if (!TextUtils.isEmpty(pathString)) {
							MZLog.d("wangguodong", "删除书架已经存在的版本..." + pathString);
							IOUtil.deleteFile(new File(pathString));
						}

						LocalBook localBook = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());
						localBook.progress = 0;
						localBook.state = LocalBook.STATE_LOAD_PAUSED;
						localBook.source = LocalBook.SOURCE_TRYREAD_BOOK;
						localBook.bookUrl = bookInfo.detail.tryDownLoadUrl;
						localBook.size = -1;
						localBook.save();
						MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, localBook.source);
						localBook.start(BookInfoNewUIActivity.this);
					}

				} else if (isPause) {
					LocalBook localBook = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());

					localBook.mod_time = System.currentTimeMillis();
					localBook.saveModTime();
					MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, localBook.source);
					localBook.start(BookInfoNewUIActivity.this);

				} else {
					if (allLocalBooks.state == LocalBook.STATE_LOADED)
						OpenBookHelper.openEBook(BookInfoNewUIActivity.this, bookInfo.detail.bookId);
					else if (DownloadService.inDownloadQueue(allLocalBooks)) {
						int progress = 0;
						if (allLocalBooks.size > 0)
							progress = (int) (100 * (allLocalBooks.progress / (allLocalBooks.size * 1.0)));
						MZLog.d("wangguodong", "已经在下载队列了");
						sendMessage(progress);
					}
				}

			}
		});

		rightReadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!LoginUser.isLogin()) {
					Intent it = new Intent(BookInfoNewUIActivity.this, LoginActivity.class);
					startActivity(it);
					return;
				}

				TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "阅读");

				boolean notExistLocal = false;
				boolean isPause = false;
				boolean isFailed = false;
				boolean isDownloaded = false;

				String pathString = null;
				LocalBook allLocalBooks = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());
				if (null == allLocalBooks)
					notExistLocal = true;
				else {
					notExistLocal = false;
					pathString = allLocalBooks.dir;
					if (!allLocalBooks.source.equals(LocalBook.SOURCE_BORROWED_BOOK) && !allLocalBooks.source.equals(LocalBook.SOURCE_BUYED_BOOK)) {
						isFailed = true;

					} else {
						int state = DownloadStateManager.getLocalBookState(allLocalBooks);
						if (DownloadTool.sourceEquals(LocalBook.SOURCE_BORROWED_BOOK, allLocalBooks.source)) {
							if (state == DownloadStateManager.STATE_LOADED && allLocalBooks.size > 0) {
								isDownloaded = true;

							} else if (state == DownloadStateManager.STATE_LOADING) {
								if (allLocalBooks.state == LocalBook.STATE_LOADING || allLocalBooks.state == LocalBook.STATE_LOAD_READY) {
								}
								if (allLocalBooks.state == LocalBook.STATE_LOAD_PAUSED) {
									isPause = true;
								}
							} else {
								isFailed = true;
							}
						} else
							isFailed = true;
					}

				}

				if (notExistLocal) {
					MZLog.d("wangguodong", "本书本地不存在 开始下载");

					if (bookInfo != null && bookInfo.detail != null) {

						MZLog.d("wangguodong", "本书本地不存在 开始下载11111");
						downloadBorrowBook();

					} else {
						Toast.makeText(BookInfoNewUIActivity.this, "请求出错了!", Toast.LENGTH_LONG).show();
					}

				} else if (isDownloaded) {
					// 打开阅读

					OpenBookHelper.openEBook(BookInfoNewUIActivity.this, bookInfo.detail.bookId);

				} else if (isFailed) {

					if (allLocalBooks.source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {

						final String pathstr = pathString;
						DialogManager.showCommonDialog(BookInfoNewUIActivity.this, "提示", "您的书架上已有畅读本，是否下载借阅本覆盖它？", "确定", "取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
										case DialogInterface.BUTTON_POSITIVE:

											if (!TextUtils.isEmpty(pathstr)) {
												MZLog.d("wangguodong", "删除书架已经存在的版本..." + pathstr);
												IOUtil.deleteFile(new File(pathstr));
											}
											LocalBook localBook = LocalBook.getLocalBook(bookid, LoginUser.getpin());
											localBook.progress = 0;
											localBook.state = LocalBook.STATE_LOAD_PAUSED;
											localBook.source = LocalBook.SOURCE_BORROWED_BOOK;
											localBook.size = -1;
											localBook.borrowEndTime = OlineDesUtils.encrypt(bookInfo.detail.userBorrowEndTime);
											MZLog.d("wangguodong", "借阅书籍保存时间：" + localBook.borrowEndTime);
											localBook.save();
											MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, localBook.source);
											localBook.start(BookInfoNewUIActivity.this);
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
						if (!TextUtils.isEmpty(pathString)) {
							MZLog.d("wangguodong", "删除书架已经存在的版本..." + pathString);
							IOUtil.deleteFile(new File(pathString));
						}
						LocalBook localBook = LocalBook.getLocalBook(bookid, LoginUser.getpin());
						localBook.progress = 0;
						localBook.state = LocalBook.STATE_LOAD_PAUSED;
						localBook.source = LocalBook.SOURCE_BORROWED_BOOK;
						localBook.size = -1;
						localBook.borrowEndTime = OlineDesUtils.encrypt(bookInfo.detail.userBorrowEndTime);
						MZLog.d("wangguodong", "借阅书籍保存时间：" + localBook.borrowEndTime);
						localBook.save();
						MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, localBook.source);
						localBook.start(BookInfoNewUIActivity.this);
					}

				} else if (isPause) {
					LocalBook localBook = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());

					localBook.mod_time = System.currentTimeMillis();
					localBook.saveModTime();
					MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, localBook.source);
					localBook.start(BookInfoNewUIActivity.this);

				} else {
					MZLog.d("wangguodong", "else");
				}

			}
		});

		borrowBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!LoginUser.isLogin()) {
					Intent it = new Intent(BookInfoNewUIActivity.this, LoginActivity.class);
					startActivity(it);
					return;
				}
				TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "借阅");
				boolean notExistLocal = false;
				boolean isPause = false;
				boolean isFailed = false;
				boolean isDownloaded = false;
				String pathString = null;

				LocalBook allLocalBooks = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());
				if (null == allLocalBooks)
					notExistLocal = true;
				else {
					notExistLocal = false;
					pathString = allLocalBooks.dir;
					if (!allLocalBooks.source.equals(LocalBook.SOURCE_BORROWED_BOOK) && !allLocalBooks.source.equals(LocalBook.SOURCE_BUYED_BOOK)) {
						isFailed = true;

					} else {
						int state = DownloadStateManager.getLocalBookState(allLocalBooks);
						if (state == DownloadStateManager.STATE_LOADED && allLocalBooks.size > 0) {
							isDownloaded = true;

						} else if (state == DownloadStateManager.STATE_LOADING) {
							if (allLocalBooks.state == LocalBook.STATE_LOADING || allLocalBooks.state == LocalBook.STATE_LOAD_READY) {
							}
							if (allLocalBooks.state == LocalBook.STATE_LOAD_PAUSED) {
								isPause = true;
							}
						} else {
							isFailed = true;
						}
					}

				}

				if (notExistLocal) {
					MZLog.d("wangguodong", "本书本地不存在 开始下载");

					if (bookInfo != null && bookInfo.detail != null) {
						toBorrow();
					} else {
						Toast.makeText(BookInfoNewUIActivity.this, "请求出错了!", Toast.LENGTH_LONG).show();
					}

				} else if (isDownloaded) {
					// 打开阅读

					OpenBookHelper.openEBook(BookInfoNewUIActivity.this, bookInfo.detail.bookId);

				} else if (isFailed) {

					if (allLocalBooks.source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {

						final String pathstr = pathString;
						DialogManager.showCommonDialog(BookInfoNewUIActivity.this, "提示", "您的书架上已有畅读本，是否下载借阅本覆盖它？", "确定", "取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
										case DialogInterface.BUTTON_POSITIVE:
											if (!TextUtils.isEmpty(pathstr)) {
												MZLog.d("wangguodong", "删除书架已经存在的版本..." + pathstr);
												IOUtil.deleteFile(new File(pathstr));
											}

											toBorrowAfterChangdu(bookInfo.detail.bookId);
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
						if (!TextUtils.isEmpty(pathString)) {
							MZLog.d("wangguodong", "删除书架已经存在的版本..." + pathString);
							IOUtil.deleteFile(new File(pathString));
						}

						toBorrowAfterChangdu(bookInfo.detail.bookId);

					}

				} else if (isPause) {
					LocalBook localBook = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());

					localBook.mod_time = System.currentTimeMillis();
					localBook.saveModTime();
					MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, localBook.source);
					localBook.start(BookInfoNewUIActivity.this);

				} else {
					MZLog.d("wangguodong", "else");
				}

			}
		});

		// 底部状态栏 结束

		headerView = BookInfoStyleController.getBookInfoHeaderStyleView(BookInfoNewUIActivity.this);

		containerLayout.addView(headerView);

		saleInfoView = BookInfoStyleController.getBookSalesOrInfoStyleView(BookInfoNewUIActivity.this, "查看目录");

		containerLayout.addView(saleInfoView);

		basicInfoView = BookInfoStyleController.getBookinfoStyleView(BookInfoNewUIActivity.this, 101, "基本信息", "更多");

		containerLayout.addView(basicInfoView);

		bookCommentsView = BookInfoStyleController.getBookinfoStyleView(BookInfoNewUIActivity.this, 102, "书评", "更多书评");
		containerLayout.addView(bookCommentsView);
		// bookNotesView = BookInfoStyleController.getBookinfoStyleView(
		// BookInfoNewUIActivity.this, 103, "笔记", "更多笔记");
		bookReadedView = BookInfoStyleController.getBookinfoStyleView(BookInfoNewUIActivity.this, 106, "", "更多");
		containerLayout.addView(bookReadedView);

		// containerLayout.addView(bookNotesView);
		bookOthersLikeView = BookInfoStyleController.getBookInfoOtherLikeStyleView(BookInfoNewUIActivity.this);
		containerLayout.addView(bookOthersLikeView);

		LinearLayout emptyLayout = (LinearLayout) LayoutInflater.from(BookInfoNewUIActivity.this).inflate(R.layout.empty_layout, null);
		containerLayout.addView(emptyLayout);

	}

	private void onlineRead() {
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
		OnlineReadManager.requestServer2ReadOnline(bookE, BookInfoNewUIActivity.this, null, false, null);
	}

	// cyr add
	public void gotoEbookPay() {

		// CommonUtil.queryCartLogin(BookCartActivity.this,new
		// BrowserUrlListener()
		// {
		// @Override
		// public void onComplete()
		// {
		//
		OnLinePayTools.gotoEbookPay(BookInfoNewUIActivity.this, null);
		// }
		// @Override
		// public void onGenToken(final String token)
		// {
		//
		// }
		//
		// });
	}

	/**
	 * 获取书评数据
	 * @param type
	 * @param bookid
	 * @param page
	 */
	public void getBookComments(final String type, long bookid, int page) {

		RequestParams params = null;

		if (type.equals("ebook"))
			params = RequestParamsPool.getBookCommentsParams("ebook", bookid, page + "");
		else {
			params = RequestParamsPool.getBookCommentsParams("paperBook", bookInfo.detail.paperBookId, page + "");
		}

		WebRequestHelper.get(URLText.JD_BASE_URL, params, true, new MyAsyncHttpResponseHandler(BookInfoNewUIActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

				String result = new String(responseBody);
				String currentTime=null;
				try {
					JSONObject object = new JSONObject(result);

					int code = object.optInt("code");

					if (code == 0){
						currentTime = object.getString("currentTime");
						JSONObject reviews = object.optJSONObject("reviews");
						JSONArray array = reviews.optJSONArray("list");

						if (array != null && array.length() > 0) {
							for (int i = 0; i < array.length(); i++) {
								JDBookComments comments = GsonUtils.fromJson(array.getString(i), JDBookComments.class);
								jDBookCommentslist.add(comments);
							}
						}else{
							if(type.equals("ebook"))
								getBookComments("paperBook", bookInfo.detail.paperBookId, 1);
						}
						
						if(jDBookCommentslist.size() >= 3){
							updateBookCommentsView(currentTime);
						}else if(type.equals("ebook")){
							getBookComments("paperBook", bookInfo.detail.paperBookId, 1);
						}
						else if(type.equals("paperBook")){
							updateBookCommentsView(currentTime);
						}
					}
					else{
						updateBookCommentsView(currentTime);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 获取已读过本书的读者信息列表
	 */
	public void getBookReaded(){
		
		RequestParams params = null;

		params = RequestParamsPool.getBookReadedParams(bookid,1 + "",BookInfoStyleController.readedUserCount+"");

		WebRequestHelper.get(URLText.JD_BASE_URL, params, true, new MyAsyncHttpResponseHandler(BookInfoNewUIActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

				String result = new String(responseBody);
				List<ReadedUserInfo> list = new ArrayList<ReadedUserInfo>();
				String amount="";
				try {
					JSONObject object = new JSONObject(result);

					int code = object.optInt("code");

					if (code != 0)
						return;

					amount=object.optString("amount");
					JSONArray array = object.optJSONArray("userInfos");
					
					if (array != null && array.length() > 0) {

						for (int i = 0; i < array.length(); i++) {
							ReadedUserInfo readedUserInfo = GsonUtils.fromJson(array.getString(i), ReadedUserInfo.class);
							list.add(readedUserInfo);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				updateBookReadedView(list,amount);
			}
		});
	}
	
	/**
	 * 更新读过本书用户view
	 * @param list
	 */
	private void updateBookReadedView(List<ReadedUserInfo> list,String amount){
		if (bookReadedView != null) {

			if (list == null || list.size() == 0) {
				bookReadedView.setVisibility(View.GONE);
				return;
			}

			LinearLayout readedContainer = (LinearLayout) bookReadedView.findViewById(R.id.readedContainer);
			LinearLayout container = (LinearLayout) readedContainer.findViewById(R.id.avatarContainer);
			
			TextView headTv = (TextView) readedContainer.findViewById(R.id.header_name);
			headTv.setText("TA们都读过  (共"+amount+"人读过)");
			
			for (int i = 0; i < container.getChildCount(); i++) {
				View child = container.getChildAt(i);
				if (i < list.size()) {
					RoundNetworkImageView avatar = (RoundNetworkImageView) child.findViewById(R.id.thumb_nail);
					//
					final ReadedUserInfo readedUserInfo=list.get(i);
					
//					if(i==BookInfoStyleController.readedUserCount-1){
////						avatar.setImageDrawable(getResources().getDrawable(R.drawable.reader_btn_more));
//						//点击头像转到个人主页
//						avatar.setOnClickListener(new OnClickListener() {
//							@Override
//							public void onClick(View arg0) {
//								Toast.makeText(BookInfoNewUIActivity.this, "click", 0).show();                         
//							}
//						});
//					}else{
//						if(null!=readedUserInfo.getYunMidImageUrl() && !readedUserInfo.getYunMidImageUrl().equals(""))
//							ImageLoader.getInstance().displayImage(readedUserInfo.getYunMidImageUrl(), avatar);
//						//点击头像转到个人主页
//						avatar.setOnClickListener(new OnClickListener() {
//							@Override
//							public void onClick(View arg0) {
//								if (LoginUser.isLogin()) {
//									Intent intent = new Intent(BookInfoNewUIActivity.this, UserActivity.class);
//									intent.putExtra(UserActivity.USER_NAME, readedUserInfo.getNickName());
//									startActivity(intent);
//								} else {
//									Intent login = new Intent(BookInfoNewUIActivity.this, LoginActivity.class);
//									startActivity(login);
//								}
//							}
//						});
//					}
					
					if(null!=readedUserInfo.getYunMidImageUrl() && !readedUserInfo.getYunMidImageUrl().equals(""))
						ImageLoader.getInstance().displayImage(readedUserInfo.getYunMidImageUrl(), avatar);
					//点击头像转到个人主页
					avatar.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(BookInfoNewUIActivity.this, UserActivity.class);
							intent.putExtra(UserActivity.NAME, readedUserInfo.getPin());
							startActivity(intent);
						}
					});
				} else {
					child.setVisibility(View.GONE);
				}
			}
		}

	}
	

	public void updateChildOfOthersLikeView(final List<Recommend> sublist) {
		LinearLayout container = (LinearLayout) bookOthersLikeView.findViewById(R.id.bookHolder);

		// MZLog.d("AAA",
		// "container.getChildCount()::"+container.getChildCount());
		// MZLog.d("AAA", "sublist.size()::"+sublist.get(0).imageUrl);
		if (recommends.size() == 0) {
			bookOthersLikeView.setVisibility(View.GONE);
			return;
		}
		for (int i = 0; i < container.getChildCount(); i++) {
			View child = container.getChildAt(i);

			if (i < sublist.size()) {

				final int index = i;
				child.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "还喜欢的书_" + sublist.get(index).name);

						Intent intent2 = new Intent(BookInfoNewUIActivity.this, BookInfoNewUIActivity.class);
						intent2.putExtra("bookid", sublist.get(index).ebookId);
						startActivity(intent2);
					}
				});

				child.setVisibility(View.VISIBLE);
				ImageView bookcover = (ImageView) child.findViewById(R.id.bookcover);
				TextView bookname = (TextView) child.findViewById(R.id.bookname);
				TextView bookAuthor = (TextView) child.findViewById(R.id.bookauthor);

				ImageLoader.getInstance().displayImage(sublist.get(i).imageUrl, bookcover, GlobalVarable.getCutBookDisplayOptions(false));
				// MZLog.d("AAA", "url::"+sublist.get(i).imageUrl);
				bookname.setText(sublist.get(i).name);
				bookAuthor.setText("null".equals(sublist.get(i).author) ? getString(R.string.author_unknown) : sublist.get(i).author);
			} else {
				child.setVisibility(View.GONE);
			}

		}
	}

	public void updateOthersLikeView() {

		if (bookOthersLikeView != null) {

			TextView title = (TextView) bookOthersLikeView.findViewById(R.id.title);
			final TextView action = (TextView) bookOthersLikeView.findViewById(R.id.action);
			// final ImageView refreshIcon = (ImageView) bookOthersLikeView
			// .findViewById(R.id.refresh_action);
			final LinearLayout llRefresh = (LinearLayout) bookOthersLikeView.findViewById(R.id.ll_book_detail_other_like_refresh);

			title.setText("购买此书的人还喜欢");
			action.setText("换一换");

			List<Recommend> sublist = new ArrayList<Recommend>();

			llRefresh.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					currentIndex += 3;
					if (currentIndex >= recommends.size())
						currentIndex = 0;
					int end = currentIndex + 3;
					if (end > recommends.size())
						end = recommends.size();
					List<Recommend> sublist = recommends.subList(currentIndex, end);
					updateChildOfOthersLikeView(sublist);

				}
			});
			// MZLog.d("AAA", "sublist::"+sublist);
			int end = currentIndex + 3;
			if (end > recommends.size())
				end = recommends.size();
			if (recommends.size() < 3) {
				llRefresh.setVisibility(View.GONE);
			} else {
				llRefresh.setVisibility(View.VISIBLE);
			}
			sublist = recommends.subList(currentIndex, end);
			updateChildOfOthersLikeView(sublist);

		}

	}
	/**
	 * 更新书评区域显示
	 * @param list
	 * @param currentTime
	 */
	protected void updateBookCommentsView(String currentTime) {

		if (bookCommentsView != null) {

		bookCommentsView.setVisibility(View.VISIBLE);
			
			TextView headerRightName = (TextView) bookCommentsView.findViewById(R.id.header_right_name);
			if ((!bookInfo.detail.isUserFluentReadAddToCard) && LoginUser.isLogin() && bookInfo.detail.isEBook){
				headerRightName.setVisibility(View.VISIBLE);
				headerRightName.setTextColor(this.getResources().getColor(R.color.red_sub));
				headerRightName.setText("写书评");
				headerRightName.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						
						Intent it2 = new Intent(BookInfoNewUIActivity.this, TimelineBookListCommentsActivity.class);
						it2.putExtra("type", "direct_comment");
						it2.putExtra("book_id", bookInfo.detail.bookId+"");
						it2.putExtra("book_name", bookInfo.detail.bookName);
						it2.putExtra("book_author", bookInfo.detail.author);
						it2.putExtra("book_cover", bookInfo.detail.logo);
						startActivity(it2);
					}
				});
			}else{
				headerRightName.setVisibility(View.GONE);
			}
			
			TextView textView = (TextView) bookCommentsView.findViewById(R.id.header_name);
			LinearLayout container1 = (LinearLayout) bookCommentsView
					.findViewById(R.id.container);
			if (jDBookCommentslist == null || jDBookCommentslist.size() == 0) {
				textView.setText(R.string.no_comment);
				for (int i = 0; i < container1.getChildCount(); i++) {
					View child = container1.getChildAt(i);
					child.setVisibility(View.GONE);
				}
				bookCommentsView.findViewById(R.id.footer_layout).setVisibility(View.GONE);
				return;
			}else{
				textView.setText("书评");
				for (int i = 0; i < container1.getChildCount(); i++) {
					View child = container1.getChildAt(i);
					child.setVisibility(View.VISIBLE);
				}
				bookCommentsView.findViewById(R.id.footer_layout).setVisibility(View.VISIBLE);
			}
			

			FrameLayout footerLayout = (FrameLayout) bookCommentsView.findViewById(R.id.footer_layout);

			footerLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "更多书评");

					Intent it = new Intent(BookInfoNewUIActivity.this, BookCommentsListActivity.class);
					if (bookInfo.detail.isEBook)
						it.putExtra("booktype", "ebook");
					else {
						it.putExtra("booktype", "paperBook");
					}
					it.putExtra("paperbookid", bookInfo.detail.paperBookId);
					it.putExtra("bookid", bookid);
					if((isShowBookCommentBtn || !bookInfo.detail.isUserFluentReadAddToCard)&& LoginUser.isLogin() && bookInfo.detail.isEBook){
						it.putExtra("isShowBookCommentBtn", true);
						it.putExtra("bookName", bookInfo.detail.bookName);
						it.putExtra("book_author", bookInfo.detail.author);
						it.putExtra("book_cover", bookInfo.detail.logo);
					}
					startActivity(it);

				}
			});

			LinearLayout container = (LinearLayout) bookCommentsView.findViewById(R.id.container);

			for (int i = 0; i < container.getChildCount(); i++) {
				View child = container.getChildAt(i);
				if (i < jDBookCommentslist.size()) {
					RoundNetworkImageView avatar = (RoundNetworkImageView) child.findViewById(R.id.thumb_nail);
					TextView title = (TextView) child.findViewById(R.id.title);
					TextView content = (TextView) child.findViewById(R.id.content);
					RatingBar ratingBar = (RatingBar) child.findViewById(R.id.rating);
					TextView time=(TextView) child.findViewById(R.id.time);
					LinearLayout commentView= (LinearLayout) child.findViewById(R.id.comment_view);
					//点击书评区域进入书评详情页
					commentView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
//							Toast.makeText(BookInfoNewUIActivity.this, "click", 0).show();;
						}
					});
					
					//单条评论
					final JDBookComments jDBookComments=jDBookCommentslist.get(i);
					
					if(null!=jDBookComments.userHead && !jDBookComments.userHead.equals(""))
						ImageLoader.getInstance().displayImage("http://"+jDBookComments.userHead, avatar);
					title.setText(jDBookComments.nickname);
					content.setText(Html.fromHtml(jDBookComments.contents));
					ratingBar.setRating((float) jDBookComments.score);
					if(null != currentTime && null != jDBookComments.creationTime){
						try {
							time.setText(DateUtil.daytimeBetweenText(currentTime,jDBookComments.creationTime));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					
					
					//点击头像转到个人主页
					avatar.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
//							if (LoginUser.isLogin()) {
								Intent intent = new Intent(BookInfoNewUIActivity.this, UserActivity.class);
								intent.putExtra(UserActivity.NAME, jDBookComments.pin);
								startActivity(intent);
//							} else {
//								Intent login = new Intent(BookInfoNewUIActivity.this, LoginActivity.class);
//								startActivity(login);
//							}
						}
					});
					
					//点击名称转到个人主页
					title.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(BookInfoNewUIActivity.this, UserActivity.class);
							intent.putExtra(UserActivity.NAME, jDBookComments.pin);
							startActivity(intent);
						}
					});
					
					
				} else {
					child.setVisibility(View.GONE);
				}

			}

		}

	}

	protected void updateBasicInfoView() {
		if (basicInfoView != null) {
			LinearLayout container = (LinearLayout) basicInfoView.findViewById(R.id.container);

			FrameLayout footer = (FrameLayout) basicInfoView.findViewById(R.id.footer_layout);

			footer.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent(BookInfoNewUIActivity.this, BookBasicInfoActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("bookinfo", bookInfo.detail);
					intent.putExtra("bookinfo", bundle);
					startActivity(intent);

				}
			});

			for (int i = 0; i < container.getChildCount(); i++) {
				View child = container.getChildAt(i);
				TextView title = (TextView) child.findViewById(R.id.title);
				TextView content = (TextView) child.findViewById(R.id.content);
				ImageView arrow = (ImageView) child.findViewById(R.id.arrow);
				switch (i) {
				case 0:
					title.setText("作者");
					String author = TextUtils.isEmpty(bookInfo.detail.author) ? getString(R.string.author_unknown) : bookInfo.detail.author;
					author = "null".equals(bookInfo.detail.author) ? getString(R.string.author_unknown) : bookInfo.detail.author;
					content.setText(author);

					if (!TextUtils.isEmpty(bookInfo.detail.authorInfo)) {
						arrow.setVisibility(View.VISIBLE);
						child.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "作者");
								startDetailView(bookInfo.detail.authorInfo);
							}
						});
					} else {
						arrow.setVisibility(View.GONE);
					}
					break;
				case 1:
					title.setText("出版方");
					content.setText(bookInfo.detail.publisher);
					arrow.setVisibility(View.VISIBLE);
					if (child != null) {
						child.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent intent = new Intent(BookInfoNewUIActivity.this, BookStoreBookListActivity.class);

								intent.putExtra("list_type", BookStoreBookListActivity.TYPE_AUTHOR_OTHER_BOOKS);
								intent.putExtra("keys", bookInfo.detail.publisher);
								intent.putExtra("isPublisher", true);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							}
						});
					}
					break;
				case 2:
					if (bookInfo.detail.isEBook) {
						title.setText("文件大小");
						content.setText(bookInfo.detail.size + "MB");
						arrow.setVisibility(View.GONE);
					} else {
						child.setVisibility(View.GONE);
					}
					break;

				default:
					break;
				}

			}

		}
	}

	protected void updateSaleInfoView() {
		if (saleInfoView != null) {

			LinearLayout container = (LinearLayout) saleInfoView.findViewById(R.id.container);

			FrameLayout footerLayout = (FrameLayout) saleInfoView.findViewById(R.id.footer_layout);

			footerLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!TextUtils.isEmpty(bookInfo.detail.catalog)) {
						TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "查看目录");
						startDetailView(bookInfo.detail.catalog);
					} else {
						Toast.makeText(BookInfoNewUIActivity.this, "此书暂时没有提供目录", Toast.LENGTH_LONG).show();
					}
				}
			});

			for (int i = 0; i < container.getChildCount(); i++) {
				View child = container.getChildAt(i);
				TextView title = (TextView) child.findViewById(R.id.title);
				TextView content = (TextView) child.findViewById(R.id.content);
				ImageView ivArrow = (ImageView) child.findViewById(R.id.arrow); 
				//广告语区域
				if (i == 0) {
					if (!TextUtils.isEmpty(bookInfo.detail.adWords)) {
						child.setVisibility(View.VISIBLE);
						title.setText("推荐语");
						content.setText(bookInfo.detail.adWords);
					} else {
						child.setVisibility(View.GONE);
					}
				}else if (i == 1) {
					if (!TextUtils.isEmpty(bookInfo.detail.promotion)) {
						child.setVisibility(View.VISIBLE);
						title.setText("促销信息");
						content.setText(bookInfo.detail.promotion);
						content.setTextColor(this.getResources().getColor(R.color.red_sub));
						ivArrow.setVisibility(View.GONE);
					} else {
						child.setVisibility(View.GONE);
					}
				}
				else {
					if (!TextUtils.isEmpty(bookInfo.detail.info)) {
						child.setVisibility(View.VISIBLE);
						title.setText("内容简介");
						content.setText(Html.fromHtml(bookInfo.detail.info));
						content.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "内容简介");

								startDetailView(bookInfo.detail.info);

							}
						});
						ivArrow.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "内容简介");

								startDetailView(bookInfo.detail.info);

							}
						});

					} else {
						child.setVisibility(View.GONE);
					}

				}

			}

		}

	}

	public void startDetailView(String info) {

		if (TextUtils.isEmpty(info)) {
			Toast.makeText(BookInfoNewUIActivity.this, "没有更多信息了!", Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(BookInfoNewUIActivity.this, BookInfoItemDetailActivity.class);

		intent.putExtra("info", info);

		startActivity(intent);

	}

	public void updateBookcartArea() {

		if (headerView != null) {

			final ImageView bookCart = (ImageView) headerView.findViewById(R.id.bookCart);
//			LinearLayout paperBookLayout = (LinearLayout) headerView.findViewById(R.id.paperBookArea);
//
//			paperBookLayout.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "购买纸质书");
//					Intent intent = new Intent(BookInfoNewUIActivity.this, WebViewActivity.class);
//					intent.putExtra(WebViewActivity.UrlKey, URLText.JD_PAPER_BOOK_SHARE_URL+"wareId=" + bookInfo.detail.paperBookId);
//					intent.putExtra(WebViewActivity.TopbarKey, false);
//					startActivity(intent);
//				}
//			});
			if (!isShowBookCartBtn && !isShowBookCommentBtn)
				bookCart.setVisibility(View.GONE);
			else if (!isShowBookCartBtn && isShowBookCommentBtn) {
//				bookCart.setText("写书评");
//				bookCart.setBackgroundResource(R.drawable.border_listbtn_red_h24);
				bookCart.setImageResource(R.drawable.write_book_review);
				
				bookCart.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						if (bookInfo != null && bookInfo.detail != null) {

							TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "写书评");

							Intent it2 = new Intent(BookInfoNewUIActivity.this, TimelineBookListCommentsActivity.class);
							it2.putExtra("type", "direct_comment");
							it2.putExtra("book_id", bookInfo.detail.bookId+"");
							it2.putExtra("book_name", bookInfo.detail.bookName);
							it2.putExtra("book_author", bookInfo.detail.author);
							it2.putExtra("book_cover", bookInfo.detail.logo);
							startActivity(it2);
							
//							Intent intent = new Intent(BookInfoNewUIActivity.this, BookCommentNewuiActivity.class);
//							intent.putExtra(BookCommentNewuiActivity.BookIdKey, bookInfo.detail.bookId);
//							intent.putExtra("bookname", "《" + bookInfo.detail.bookName + "》");
//							startActivity(intent);

						}
					}
				});

			} else if (isShowBookCartBtn && !isShowBookCommentBtn) {

//				bookCart.setText("购物车");
				BookCartManager.getInstance().isExistInShoppingCart(BookInfoNewUIActivity.this, bookid + "", new CheckExistListener() {

					@Override
					public void isExist(boolean isExist) {
						MZLog.d("J", "本书是否存在购物车？" + isExist);
						if (isExist) {
							
							bookCart.setImageResource(R.drawable.already_in_shoppingcart);
//							bookCart.setBackgroundResource(R.drawable.border_cartbtn_remove_h24);
//							bookCart.setTextColor(getResources().getColor(R.color.text_main));
							bookCart.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {

									if (bookInfo != null && bookInfo.detail != null) {

										TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "购物车-删除");

										bookCart.setImageResource(R.drawable.add_shoppingcart);
										
//										bookCart.setBackgroundResource(R.drawable.border_cartbtn_add_h24);
//										bookCart.setTextColor(getResources().getColor(R.color.red_main));
										BookCartManager.getInstance().deleteFromShoppingCart(BookInfoNewUIActivity.this, false,
												new String[] { String.valueOf(bookInfo.detail.bookId) }, new DelFromCartListener() {

													@Override
													public void onDelSuccess(boolean isDelInCart, BookCardItemEntity cardItemEntity) {
														// method stub
														MZLog.d("J", "从购物车删除成功");
														updateBookCart();
														bookInfo.detail.isAddToCart = false;
														updateBookcartArea();
														bookCart.setImageResource(R.drawable.add_shoppingcart);
//														bookCart.setBackgroundResource(R.drawable.border_cartbtn_add_h24);
//														bookCart.setTextColor(getResources().getColor(R.color.red_main));
													}

													@Override
													public void onDelFail() {
														// method stub
														MZLog.d("J", "从购物车删除失败");
														updateBookCart();

														updateBookcartArea();
													}
												});

									}
								}
							});
						} else {

							bookCart.setImageResource(R.drawable.add_shoppingcart);
//							bookCart.setBackgroundResource(R.drawable.border_cartbtn_add_h24);
//							bookCart.setTextColor(getResources().getColor(R.color.red_main));

							bookCart.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// 是否可购买
									if (!isPurchasable) {
										Toast.makeText(BookInfoNewUIActivity.this, "此书暂时不能加入购物车", Toast.LENGTH_LONG).show();
										return;
									}
									if (bookInfo != null && bookInfo.detail != null) {

										TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "购物车-添加");

										bookCart.setImageResource(R.drawable.already_in_shoppingcart);
//										bookCart.setBackgroundResource(R.drawable.border_cartbtn_remove_h24);
//										bookCart.setTextColor(getResources().getColor(R.color.text_main));

										SimplifiedDetail detail = JDBookInfo.simplifyDetail(bookInfo.detail);
										BookCartManager.getInstance().addToShoppingCart(BookInfoNewUIActivity.this, detail, new AddToCartListener() {

											@Override
											public void onAddSuccess() {
												// stub
												MZLog.d("J", "添加到购物车成功");
												updateBookCart();
												updateBookcartArea();
												bookInfo.detail.isAddToCart = true;
												bookCart.setImageResource(R.drawable.already_in_shoppingcart);
												
//												bookCart.setBackgroundResource(R.drawable.border_cartbtn_remove_h24);
//												bookCart.setTextColor(getResources().getColor(R.color.text_main));
											}

											@Override
											public void onAddFail() {
												// stub
												MZLog.e("J", "添加到购物车失败");
												updateBookCart();
												updateBookcartArea();
											}
										});
									}
								}
							});
						}
					}
				});
			}

		}
	}

	// 检查借阅请求。。。
	public void toBorrow() {
		WebRequestHelper.get(URLText.JD_BOOK_DOWNLOAD_URL, RequestParamsPool.getBorrowBookParams(bookInfo.detail.bookId), true, new MyAsyncHttpResponseHandler(
				BookInfoNewUIActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

				Toast.makeText(BookInfoNewUIActivity.this, "请求借阅出错了,请重试!", Toast.LENGTH_LONG).show();

			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				MZLog.d("wangguodong", new String(responseBody));

				try {
					JSONObject object = new JSONObject(new String(responseBody));

					String endtime = object.optString("userBorrowEndTime");
					JDBookInfo.Detail detail = bookInfo.detail;
					detail.userBorrowEndTime = endtime;
					detail.isAlreadyBorrow = true;
					detail.isBorrow = true;
					bookInfo.detail = detail;
					MZLog.d("wangguodong", "@#@#@#@" + detail.isBorrow);
					downloadBorrowBook();

				} catch (Exception e) {
					Toast.makeText(BookInfoNewUIActivity.this, "服务器出错了,请重试!", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}

			}
		});

	}

	// 检查借阅请求。。。
	public void toBorrowAfterChangdu(final long bookid) {
		WebRequestHelper.get(URLText.JD_BOOK_DOWNLOAD_URL, RequestParamsPool.getBorrowBookParams(bookInfo.detail.bookId), true, new MyAsyncHttpResponseHandler(
				BookInfoNewUIActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

				Toast.makeText(BookInfoNewUIActivity.this, "请求借阅出错了,请重试!", Toast.LENGTH_LONG).show();

			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				MZLog.d("wangguodong", new String(responseBody));

				try {
					JSONObject object = new JSONObject(new String(responseBody));

					String endtime = object.optString("userBorrowEndTime");
					JDBookInfo.Detail detail = bookInfo.detail;
					detail.userBorrowEndTime = endtime;
					detail.isAlreadyBorrow = true;
					detail.isBorrow = true;
					bookInfo.detail = detail;
					MZLog.d("wangguodong", "@#@#@#@" + detail.isBorrow);

					LocalBook localBook = LocalBook.getLocalBook(bookid, LoginUser.getpin());
					localBook.progress = 0;
					localBook.state = LocalBook.STATE_LOAD_PAUSED;
					localBook.source = LocalBook.SOURCE_BORROWED_BOOK;
					localBook.size = -1;
					localBook.borrowEndTime = OlineDesUtils.encrypt(endtime);
					MZLog.d("wangguodong", "借阅书籍保存时间：" + localBook.borrowEndTime);
					localBook.save();
					MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, localBook.source);
					localBook.start(BookInfoNewUIActivity.this);

				} catch (Exception e) {
					Toast.makeText(BookInfoNewUIActivity.this, "服务器出错了,请重试!", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}

			}
		});

	}

	public void readBook() {
		// 已经畅读过
		boolean notExistLocal = false;
		boolean isPause = false;
		boolean isFailed = false;
		boolean inWaiting = false;
		boolean isDownloaded = false;
		String pathString = null;

		LocalBook allLocalBooks = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());
		if (null == allLocalBooks)
			notExistLocal = true;
		else {
			notExistLocal = false;
			pathString = allLocalBooks.dir;
			if (!allLocalBooks.source.equals(LocalBook.SOURCE_BUYED_BOOK)) {
				isFailed = true;
				if (DownloadTool.sourceEquals(LocalBook.SOURCE_BUYED_BOOK, allLocalBooks.source) && allLocalBooks.size > 0)
					isDownloaded = true;
			} else {
				int state = DownloadStateManager.getLocalBookState(allLocalBooks);
				if (state == DownloadStateManager.STATE_LOADED && allLocalBooks.size > 0) {
					isDownloaded = true;

				} else if (state == DownloadStateManager.STATE_LOADING) {
					if (allLocalBooks.state == LocalBook.STATE_LOADING || allLocalBooks.state == LocalBook.STATE_LOAD_READY) {
						inWaiting = true;
					}
					if (allLocalBooks.state == LocalBook.STATE_LOAD_PAUSED) {
						isPause = true;
					}
				} else {
					isFailed = true;
				}
			}

		}

		if (notExistLocal) {
			if (!NetWorkUtils.isNetworkAvailable(BookInfoNewUIActivity.this)) {
				Toast.makeText(BookInfoNewUIActivity.this, "网络不可用", Toast.LENGTH_LONG).show();
				return;
			}
			if (NetWorkUtils.getNetworkConnectType(BookInfoNewUIActivity.this) == NetworkConnectType.MOBILE) {
				String title = "图书大小为" + bookInfo.detail.size + "M";
				DialogManager.showCommonDialog(BookInfoNewUIActivity.this, title, "确定使用移动网络下载吗", "确定", "取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							if (!bookInfo.detail.isFree) {
								OrderEntity orderEntity = OrderEntity.FromJDBooKInfo2OrderEntityWithOrderid(bookInfo.detail);
								DownloadTool.download((Activity) BookInfoNewUIActivity.this, orderEntity, null, false, LocalBook.SOURCE_BUYED_BOOK, 0, true,
										null, false);
							} else {
								OrderEntity orderEntity = OrderEntity.FromJDBooKInfo2OrderEntityWithoutOrderid(bookInfo.detail);
								DownloadTool.download((Activity) BookInfoNewUIActivity.this, orderEntity, null, false, LocalBook.SOURCE_BUYED_BOOK, 0, true,
										null, false);
							}
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
				if (!bookInfo.detail.isFree) {
					OrderEntity orderEntity = OrderEntity.FromJDBooKInfo2OrderEntityWithOrderid(bookInfo.detail);
					DownloadTool.download((Activity) BookInfoNewUIActivity.this, orderEntity, null, false, LocalBook.SOURCE_BUYED_BOOK, 0, true, null, false);
				} else {
					OrderEntity orderEntity = OrderEntity.FromJDBooKInfo2OrderEntityWithoutOrderid(bookInfo.detail);
					DownloadTool.download((Activity) BookInfoNewUIActivity.this, orderEntity, null, false, LocalBook.SOURCE_BUYED_BOOK, 0, true, null, false);
				}
			}

		} else if (isDownloaded) {

			MZLog.d("wangguodong", "书籍已经存在本地了，直接打开");
			// 打开阅读
			if (isFailed) {
				LocalBook localBook = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());
				localBook.source = LocalBook.SOURCE_BUYED_BOOK;
				localBook.save();
			}
			OpenBookHelper.openEBook(BookInfoNewUIActivity.this, bookInfo.detail.bookId);

		} else if (isFailed) {

			if (!TextUtils.isEmpty(pathString)) {
				MZLog.d("wangguodong", "删除书架已经存在的版本...");
				IOUtil.deleteFile(new File(pathString));
			}

			LocalBook localBook = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());
			localBook.progress = 0;
			localBook.size = 0;
			localBook.state = LocalBook.STATE_LOAD_PAUSED;
			localBook.source = LocalBook.SOURCE_BUYED_BOOK;
			localBook.order_code = String.valueOf(bookInfo.detail.orderId);
			localBook.bookUrl = "";
			localBook.save();
			MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, localBook.source);
			localBook.start(BookInfoNewUIActivity.this);

		} else if (isPause) {
			LocalBook localBook = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());

			localBook.mod_time = System.currentTimeMillis();
			localBook.saveModTime();
			localBook.start(BookInfoNewUIActivity.this);

		} else {
			MZLog.d("wangguodong", "已经在下载队列了");
		}
	}

	public void addToChangduList() {
		LocalBook allLocalBooks = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());

		if (null != allLocalBooks && allLocalBooks.source.equals(LocalBook.SOURCE_BORROWED_BOOK)) {
			String endtime = OlineDesUtils.decrypt(allLocalBooks.borrowEndTime);
			if (!TextUtils.isEmpty(endtime)) {
				MZLog.d("wangguodong", "借阅截止时间" + endtime);
				long end = TimeFormat.formatStringTime(endtime);
				long cur = System.currentTimeMillis();

				if (end != -1) {

					if (end > cur) {
						// 在借阅期限内
						final String pathstr = allLocalBooks.dir;
						DialogManager.showCommonDialog(BookInfoNewUIActivity.this, "提示", "您的书架上已有借阅本，是否下载畅读本覆盖它？", "确定", "取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
										case DialogInterface.BUTTON_POSITIVE:
											onlineRead();
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
						// 借阅过期了
						onlineRead();
					}
				} else {
					Toast.makeText(BookInfoNewUIActivity.this, "借阅书籍数据异常，请重新下载！", Toast.LENGTH_SHORT).show();
				}

			}
		}else if(null != allLocalBooks && allLocalBooks.source.equals(LocalBook.SOURCE_ONLINE_BOOK)){
			OpenBookHelper.openEBook(BookInfoNewUIActivity.this, bookInfo.detail.bookId,null);
		}
		else {
			onlineRead();
		}

	}

	public void downloadBorrowBook() {

		String temp = bookInfo.detail.userBorrowEndTime;

		String time = OlineDesUtils.encrypt(temp);
		bookInfo.detail.userBorrowEndTime = time;
		OrderEntity orderEntity = OrderEntity.FromJDBooKInfo2OrderEntity(bookInfo.detail);
		bookInfo.detail.userBorrowEndTime = temp;

		DownloadTool.download((Activity) BookInfoNewUIActivity.this, orderEntity, null, false, LocalBook.SOURCE_BORROWED_BOOK, 0, true, null, false);

	}

	protected void updateHeaderView() {

		if (headerView != null) {

			ImageView view = (ImageView) headerView.findViewById(R.id.bookCover);
			ImageView imageViewLabel = (ImageView) headerView.findViewById(R.id.imageViewLabel);
			RatingBar bar = (RatingBar) headerView.findViewById(R.id.rating);

			TextView title = (TextView) headerView.findViewById(R.id.title);
			TextView author = (TextView) headerView.findViewById(R.id.bookAuthor);
			TextView ratingValue = (TextView) headerView.findViewById(R.id.ratingValue);
			
			TextView jdprice = (TextView) headerView.findViewById(R.id.new_price);
			TextView discount = (TextView) headerView.findViewById(R.id.discount);
			TextView orgjdprice = (TextView) headerView.findViewById(R.id.old_price);
			
			TextView unit = (TextView) headerView.findViewById(R.id.unit);
			
			if(bookInfo.detail.isFree){
				jdprice.setVisibility(View.GONE);
				discount.setVisibility(View.GONE);
				orgjdprice.setVisibility(View.GONE);
				unit.setVisibility(View.GONE);
			}else if(bookInfo.detail.jdPrice <= 0 ){
				jdprice.setText("暂无报价");
				discount.setVisibility(View.GONE);
				orgjdprice.setVisibility(View.GONE);
				unit.setVisibility(View.GONE);
			}else
			{
				DecimalFormat decimalFormat =new DecimalFormat("0.00");
				DecimalFormat decimalFormat1 =new DecimalFormat("0.0");
				jdprice.setText(""+decimalFormat.format(bookInfo.detail.jdPrice));
				if(bookInfo.detail.jdPrice<bookInfo.detail.orgJdPrice && bookInfo.detail.orgJdPrice!=0){
					discount.setVisibility(View.VISIBLE);
					orgjdprice.setVisibility(View.VISIBLE);
					double discountDouble=(bookInfo.detail.jdPrice/bookInfo.detail.orgJdPrice)*10;
					discount.setText(decimalFormat1.format(discountDouble)+"折");
					orgjdprice.setText("原价：￥"+decimalFormat.format(bookInfo.detail.orgJdPrice));
					orgjdprice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //删除线
				}else{
					discount.setVisibility(View.GONE);
					orgjdprice.setVisibility(View.INVISIBLE);
				}
			}
			
			//
			FrameLayout footer = (FrameLayout) headerView.findViewById(R.id.footer_layout);
			footer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(BookInfoNewUIActivity.this, WebViewActivity.class);
					intent.putExtra(WebViewActivity.UrlKey, "http://item.m.jd.com/product/" + bookInfo.detail.paperBookId + ".html");
					intent.putExtra(WebViewActivity.TopbarKey, false);
					startActivity(intent);
				}
			});
			if (!isShowPaperBookBtn)
				footer.setVisibility(View.GONE);
			else {
				footer.setVisibility(View.VISIBLE);
			}

			ImageView bookCart = (ImageView) headerView.findViewById(R.id.bookCart);
			ImageView sendBook = (ImageView) headerView.findViewById(R.id.send_book);
			
			List<String> changducardList = Arrays.asList(this.CHANGDUCARD);
			if(bookInfo.detail.isFree || !isPurchasable || !bookInfo.detail.isEBook || changducardList.contains(String.valueOf(bookInfo.detail.ebookId))){
				sendBook.setVisibility(View.GONE);
			}
			else{
				sendBook.setVisibility(View.VISIBLE);
				sendBook.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(!NetWorkUtils.isNetworkConnected(BookInfoNewUIActivity.this)){
							ToastUtil.showToastInThread(R.string.network_not_find);
							return ;
						}
							
						//按钮点击统计接口（后台统计需要）
						WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSendBookAnalysisParams(), true,
								new MyAsyncHttpResponseHandler(BookInfoNewUIActivity.this) {
									@Override
									public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
									}
									@Override
									public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
									}
								});
						
						if(!LoginUser.isLogin()){
							Intent intent = new Intent(BookInfoNewUIActivity.this,LoginActivity.class);
							startActivity(intent);
							return;
						}
						//TODO 
						WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSendableOrderListParams(String.valueOf(bookInfo.detail.bookId)), true,
								new MyAsyncHttpResponseHandler(BookInfoNewUIActivity.this) {
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
										final JSONArray orders= json.optJSONArray("orderList");
										if(orders!=null && orders.length() >0){
											if (isFinishing()) 
												return ;
											DialogManager.showCommonDialog(BookInfoNewUIActivity.this, "提示", "您还有一本该书未成功送出哦~", "再次购买", "去赠送",
													new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													switch (which) {
													case DialogInterface.BUTTON_POSITIVE:
														startSendBook();
														break;
													case DialogInterface.BUTTON_NEGATIVE:
														String orderId = null ;
														try {
															orderId = orders.get(0).toString();
														} catch (JSONException e) {
															e.printStackTrace();
														}
														if(TextUtils.isEmpty(orderId))
															return ;
														String sendbookUrl = "http://order.e.jd.com/buySendBook/buySendBook_toPage.action?client=android&send_order_id="+orderId;
														Intent sendintent = new Intent(BookInfoNewUIActivity.this,OnlinePayActivity.class);
														sendintent.putExtra("url", sendbookUrl);
														startActivity(sendintent);
														break;
													default:
														break;
													}
													dialog.dismiss();
												}
											});
										}else{
											startSendBook();
										}
									}else{
										startSendBook();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
								});
						
					}
				});
				
			}
			
//			LinearLayout paperBookLayout = (LinearLayout) headerView.findViewById(R.id.paperBookArea);
//
//			paperBookLayout.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//
//					Intent intent = new Intent(BookInfoNewUIActivity.this, WebViewActivity.class);
//					intent.putExtra(WebViewActivity.UrlKey, "http://item.m.jd.com/product/" + bookInfo.detail.paperBookId + ".html");
//					intent.putExtra(WebViewActivity.TopbarKey, false);
//					startActivity(intent);
//				}
//			});
//
//			if (!isShowPaperBookBtn)
//				paperBookLayout.setVisibility(View.GONE);
//			else {
//				paperBookLayout.setVisibility(View.VISIBLE);
//			}

			if (!isShowBookCartBtn && !isShowBookCommentBtn) {
				bookCart.setVisibility(View.GONE);
//				paperBookLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			} else if (!isShowBookCartBtn && isShowBookCommentBtn) {
				bookCart.setVisibility(View.VISIBLE);
//				bookCart.setText("写书评");
//				bookCart.setBackgroundResource(R.drawable.border_listbtn_red_h24);
				bookCart.setImageResource(R.drawable.write_book_review);
//				paperBookLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
			} else if (isShowBookCartBtn && !isShowBookCommentBtn) {
				bookCart.setVisibility(View.VISIBLE);
//				bookCart.setText("购物车");
//				bookCart.setBackgroundResource(R.drawable.border_cartbtn_add_h24);
				bookCart.setImageResource(R.drawable.add_shoppingcart);
//				paperBookLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
			}

			ImageView starView = (ImageView) headerView.findViewById(R.id.star_img);
			LinearLayout likeLl = (LinearLayout) findViewById(R.id.wish_area);
			starView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (!LoginUser.isLogin()){
						Intent intent = new Intent(BookInfoNewUIActivity.this,LoginActivity.class);
						startActivity(intent);
						return;
					}
						

					if (isWish) {
						TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "不喜欢");

						MZLog.d("wangguodong", "取消想读");
						updateWishButton(false);
						isWish = false;
						unWishBook(bookInfo.detail.bookId);
					} else {
						TalkingDataUtil.onBookDetailEvent(BookInfoNewUIActivity.this, "喜欢");

						MZLog.d("wangguodong", "想读");
						updateWishButton(true);
						isWish = true;
						wishBook(bookInfo.detail.bookId);
					}

				}
			});
			if (!bookInfo.detail.isEBook) {
				imageViewLabel.setBackgroundDrawable(getResources().getDrawable(R.drawable.badge_coverlabel_paper));
			}else {
				imageViewLabel.setBackgroundDrawable(null);
			}
			ImageLoader.getInstance().displayImage(bookInfo.detail.logo, view, GlobalVarable.getCutBookDisplayOptions(false));
			
			//点击图书封面展示大图tmj
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
//					Intent intent=new Intent(BookInfoNewUIActivity.this,BookStoreBookBigViewActivity.class);
//					intent.putExtra("largeLogo", bookInfo.detail.logo);
//					startActivity(intent);
					if(mPopupWindow!=null)
						mPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
					else{
						popBigView();
						mPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
					}
				}
			});
			
			bar.setRating(bookInfo.detail.star);
			title.setText(bookInfo.detail.bookName);
			ratingValue.setText(bookInfo.detail.star + "");
			author.setText(("null".equals(bookInfo.detail.author) || "".equals(bookInfo.detail.author) || bookInfo.detail.author == null) ? getString(R.string.author_unknown)
					: bookInfo.detail.author);

			// 更新想读状态
			isWishBook(bookInfo.detail.bookId);

			updateBookcartArea();

		}
	}
	
	private void startSendBook(){
		Intent intent = new Intent();
		intent.setClass(BookInfoNewUIActivity.this, SendBookOrderActivity.class);
		intent.putExtra("bookId", String.valueOf(bookInfo.detail.bookId));
		intent.putExtra("bookName", bookInfo.detail.bookName);
		intent.putExtra("author", bookInfo.detail.author);
		intent.putExtra("bookCover", bookInfo.detail.logo);
		startActivity(intent);
	}
	
	/**
	 * 点击封面弹出大图
	 */
	public void popBigView() {

		View popupView = this.getLayoutInflater().inflate(
				R.layout.activity_bookstore_book_bigview, null);
		
		RelativeLayout layout=(RelativeLayout) popupView.findViewById(R.id.layout);
		
		final ImageView imageView=(ImageView)popupView.findViewById(R.id.img);
		DisplayImageOptions option = GlobalVarable
				.getDefaultBookDisplayOptions();
		ImageLoader.getInstance().displayImage(bookInfo.detail.largeLogo, imageView,option,new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String arg0, View arg1) {
			}
			
			@Override
			public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
				ImageLoader.getInstance().displayImage(bookInfo.detail.logo, imageView, GlobalVarable.getCutBookDisplayOptions(false));
			}
			
			@Override
			public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
			}
			
			@Override
			public void onLoadingCancelled(String arg0, View arg1) {
				
			}
		});

		mPopupWindow = new PopupWindow(popupView, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
//		mPopupWindow.setFocusable(true);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(this
				.getResources().getColor(R.color.bg_menu_shadow)));
//		mPopupWindow.setAnimationStyle(R.style.AnimationPreview);  
//		tmj;
		imageView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return true;
			}
		});
		
		layout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (mPopupWindow != null && mPopupWindow.isShowing()) {
					mPopupWindow.dismiss();
//					mPopupWindow = null;
				}
				return false;
			}
		});
		
//		mPopupWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
	}

	public void updateWishButton(boolean iswished) {
		if (headerView != null) {

			ImageView starView = (ImageView) headerView.findViewById(R.id.star_img);
			TextView starText = (TextView) headerView.findViewById(R.id.star_txt);

			if (iswished) {
//				starView.setImageResource(R.drawable.btn_book_fav_hl);
				starView.setImageResource(R.drawable.community_like_book_icon);
				starText.setTextColor(getResources().getColor(R.color.red_main));
			} else {
//				starView.setImageResource(R.drawable.btn_book_fav);
				starView.setImageResource(R.drawable.community_unlike_book_icon);
				starText.setTextColor(getResources().getColor(R.color.text_sub));
			}

		}

	}

	@Override
	public void onRightMenuTwoClick() {
		TalkingDataUtil.onBookDetailEvent(this, "进入购物车");

		Intent itIntent = new Intent(BookInfoNewUIActivity.this, BookCartActivity.class);
		startActivity(itIntent);
	}

	public void isWishBook(long bookid) {

		if (!LoginUser.isLogin())
			return;

		WebRequestHelper.post(URLText.wishStatus, RequestParamsPool.getWishBookParams(bookid), true,
				new MyAsyncHttpResponseHandler(BookInfoNewUIActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						try {
							JSONObject object = new JSONObject(new String(responseBody));
							boolean iswished = object.optBoolean("wish_status");
							if (iswished) {
								isWish = true;
								updateWishButton(iswished);
							} else {
								isWish = false;
								updateWishButton(iswished);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});
	}

	public void unWishBook(long bookid) {
		WebRequestHelper.post(URLText.unwishBook, RequestParamsPool.unWishBookParams(bookid), true, new MyAsyncHttpResponseHandler(BookInfoNewUIActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

			}
		});
	}

	public void wishBook(long bookid) {
		WebRequestHelper.post(URLText.wishBook, RequestParamsPool.wishBookParams(bookid), true, new MyAsyncHttpResponseHandler(BookInfoNewUIActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

			}
		});
	}

	@Override
	public void refresh(final DownloadedAble downloadAble) {
		final LocalBook localBook = (LocalBook) downloadAble;
		if (localBook == null || bookInfo == null) {
			return;
		}

		if (localBook.book_id != bookInfo.detail.bookId) {
			return;
		}

		int progress = (int) (100 * (localBook.progress / (localBook.size * 1.0)));

		int state = DownloadStateManager.getLocalBookState(localBook);

		if (state == DownloadStateManager.STATE_LOADED) {

			currentOpenTime++;

			if (currentOpenTime == 1) {
//				sendProgressDialogMessage();
//				new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//
//						try {
//							Thread.sleep(1500);
//							OpenBookHelper.openEBook(BookInfoNewUIActivity.this, localBook.book_id);
//							cancelProgressDialogMessage();
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//
//					}
//				}).start();
			}

		} else if (state == DownloadStateManager.STATE_LOADING) {

			if (localBook.state == LocalBook.STATE_LOADING || localBook.state == LocalBook.STATE_LOAD_READY) {
				sendMessage(progress);

			}
		} else {// 下载失败
			sendMessage(PROGRESS_FAILED);
		}
	}

	public void sendMessage(int progress) {
		Message msg = new Message();
		msg.what = UPDATE_UI_MESSAGE;
		msg.arg2 = progress;
		handler.sendMessage(msg);
	}

	public void sendProgressDialogMessage() {
		Message msg = new Message();
		msg.what = SHOW_PROGRESS_DIALOG;
		handler.sendMessage(msg);
	}

	public void cancelProgressDialogMessage() {
		Message msg = new Message();
		msg.what = CANCEL_SHOW_PROGRESS_DIALOG;
		handler.sendMessage(msg);
	}

	@Override
	public int getType() {
		return 0;
	}

	@Override
	public void refreshDownloadCache() {

	}

	public void updateBookCart() {

		// int length = JDBookCart.getInstance(BookInfoNewUIActivity.this)
		// .getBookCartCount(BookInfoNewUIActivity.this);
		BookCartManager.getInstance().getTotalCount(this, new GetTotalCountListener() {

			@Override
			public void onResult(int length) {
				MZLog.d("wangguodong", "更新购物车数量..." + length);
				if (length > 0 && length < 100) {
					getTopBarView().addNotificationOnMenu(false, false, true, "" + length);
				} else if (length > 99) {
					getTopBarView().addNotificationOnMenu(false, false, true, "99+");
				} else {
					getTopBarView().addNotificationOnMenu(false, false, true, "");
				}
			}
		});

	}

	@Override
	public void onRightMenuOneClick() {
		super.onRightMenuOneClick();
		TalkingDataUtil.onBookDetailEvent(this, "分享浮窗-打开");
		sharePopupWindow = new SharePopupWindow(BookInfoNewUIActivity.this);
		sharePopupWindow.setListener(this);
		sharePopupWindow.show(getTopBarView().getSubmenurightOneImage());
	}

	@Override
	public void onPopupWindowMore() {
		TalkingDataUtil.onBookDetailEvent(this, "分享-更多");
		
		String author3 = ("null".equals(bookInfo.detail.author) || "".equals(bookInfo.detail.author) || bookInfo.detail.author == null) ? getString(R.string.author_unknown)
				: bookInfo.detail.author;
		String str3 = null;
		if (bookInfo.detail.info != null) {
			if (bookInfo.detail.info.length() > 30) {
				str3 = bookInfo.detail.info.substring(0, 30);
			} else {
				str3 = bookInfo.detail.info;
			}
		} else {
			str3 = "暂无简介";
		}
		sharePopupWindow.More(BookInfoNewUIActivity.this, bookInfo.detail.bookName + "-" + str3, "", getShareUrl());
	}

	@Override
	public void onPopupWindowWeixinClick() {
		TalkingDataUtil.onBookDetailEvent(this, "分享-微信好友");
		if (null == bookInfo) {
			return;
		}
		
		if (null == bookInfo.detail) {
			return;
		}
		
		String string = bookInfo.detail.author;
		String author1 = ("null".equals(string) || "".equals(string) || string == null) ? getString(R.string.author_unknown) : string;
		String str1 = null;
		if (bookInfo.detail.info != null) {
			if (bookInfo.detail.info.length() > 30) {
				str1 = bookInfo.detail.info.substring(0, 30);
			} else {
				str1 = bookInfo.detail.info;
			}
		} else {
			str1 = "暂无简介";
		}

		MZLog.d("cj", "str======>>>" + str1);
		sharePopupWindow.shareToWeixin(this, bookInfo.detail.bookName, author1 + "\n" + Html.fromHtml(str1), bookInfo.detail.logo, getShareUrl(), 0);
		post(LoginUser.getpin(), String.valueOf(bookInfo.detail.bookId));

	}

	@Override
	public void onPopupWindowSinaClick() {
		TalkingDataUtil.onBookDetailEvent(this, "分享-新浪微博");
		
		String author = ("null".equals(bookInfo.detail.author) || "".equals(bookInfo.detail.author) || bookInfo.detail.author == null) ? getString(R.string.author_unknown)
				: bookInfo.detail.author;
		String str = null;
		if (bookInfo.detail.info != null) {
			if (bookInfo.detail.info.length() > 30) {
				str = Html.fromHtml(bookInfo.detail.info).toString().substring(0, 30) + "...";
				Pattern p = Pattern.compile("\\s*|\t|\r|\n");
				Matcher m = p.matcher(str);
				str = m.replaceAll("");
			} else {
				str = Html.fromHtml(bookInfo.detail.info).toString();
			}
		} else {
			str = "暂无简介";
		}

		String bookname = null;
		bookname = bookInfo.detail.bookName;
		if (bookname.endsWith("》")) {
			bookname = bookInfo.detail.bookName;
		} else {
			bookname = "《" + bookInfo.detail.bookName + "》";
		}

		sharePopupWindow.shareToWeibo(BookInfoNewUIActivity.this, author + " - " + "\n" + bookname + "\n" + str, "" + str, bookInfo.detail.logo, getShareUrl() + "     ", "");
		post(LoginUser.getpin(), String.valueOf(bookInfo.detail.bookId));
		
	}

	@Override
	public void onPopupWindowWeixinFriend() {
		TalkingDataUtil.onBookDetailEvent(this, "分享-微信朋友圈");
		
		if (null == bookInfo) {
			return;
		}
		
		if (null == bookInfo.detail) {
			return;
		}
		
		String str2;
		if (bookInfo.detail.isFree) {
			str2 = "可免费下载";
		} else {
			str2 = "书城有售";
		}
		
		sharePopupWindow.shareToWeixin(BookInfoNewUIActivity.this, bookInfo.detail.bookName + "-" + str2, null, bookInfo.detail.logo, getShareUrl(), 1);
		post(LoginUser.getpin(), String.valueOf(bookInfo.detail.bookId));
	}

	@Override
	public void onPopupWindowCancel() {
		TalkingDataUtil.onBookDetailEvent(this, "分享浮窗-关闭");
		sharePopupWindow.dismiss();
	}


	private String getShareUrl() {
		String share_url = null;
		if (bookInfo.detail.isEBook) {
			share_url = EBOOK_SHARE_URL + bookInfo.detail.bookId + ".html";
		} else {
			share_url = PAPER_BOOK_SHARE_URL + bookInfo.detail.bookId;
		}
		return share_url;
	}

	private int currentSearchPage;
	private View mAuthorBookListView;
	private boolean isAuthorBookListViewAdded = false;


	private void post(String pin, String bookid) {
		WebRequestHelper.post(URLText.SHARE_URL, RequestParamsPool.getShareParams(pin, bookid, "Book"), true, new MyAsyncHttpResponseHandler(
				BookInfoNewUIActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				// Toast.makeText(BookInfoNewUIActivity.this,
				// getString(R.string.network_connect_error),
				// Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

				String result = new String(responseBody);

				JSONObject jsonObj;
				try {
					jsonObj = new JSONObject(new String(responseBody));
					if (jsonObj != null) {
						JSONObject desJsonObj = null;
						String code = jsonObj.optString("code");
						if (Integer.parseInt(code) == 0) {
							String message = jsonObj.optString("message");
							// Toast.makeText(getApplicationContext(),
							// message, 1).show();
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (getIntent() != null && getIntent().getIntExtra("lx", -1) == 4) {
			if(type!=null && type.equals("jdmessage")){
				super.onBackPressed();
				return ;
			}
			Intent intent = new Intent(this, LauncherActivity.class);
			intent.putExtra("LX", 1);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			this.finish();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onPopupWindowCommuity() {
		Intent itIntent = new Intent(BookInfoNewUIActivity.this, TimelineBookListCommentsActivity.class);
		itIntent.putExtra("type", "share_to_comunity");
		itIntent.putExtra("book_id", bookInfo.detail.bookId+"");
		itIntent.putExtra("book_name", bookInfo.detail.bookName);
		itIntent.putExtra("book_author", bookInfo.detail.author);
		itIntent.putExtra("book_cover", bookInfo.detail.logo);
		
//		// 捎带主要信息
//		Intent in = new Intent();
//		in.putExtra("book_id", mention.getMentionBookId() + "");
//		in.putExtra("book_name", mention.getMentionBookName());
//		in.putExtra("book_author", mention.getAuthor());
//		in.putExtra("book_cover", mention.getBookCover());
		
		startActivity(itIntent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isAuthorBookListViewAdded = false;
	}

}
