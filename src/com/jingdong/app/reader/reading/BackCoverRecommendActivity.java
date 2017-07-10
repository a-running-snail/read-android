package com.jingdong.app.reader.reading;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookCommentNewuiActivity;
import com.jingdong.app.reader.activity.BookCommentsListActivity;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.bookstore.style.controller.BookInfoStyleController;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController.OnHeaderActionClickListener;
import com.jingdong.app.reader.bookstore.style.controller.RankingListViewStyleController;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.extra.JDBook;
import com.jingdong.app.reader.entity.extra.JDBookComments;
import com.jingdong.app.reader.entity.extra.JDBookDetail;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.entity.extra.StoreBook;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListCommentsActivity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.DateUtil;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.JavaUtil;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.SharePopupWindow;
import com.jingdong.app.reader.view.SharePopupWindow.onPopupWindowItemClickListener;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BackCoverRecommendActivity extends BaseActivityWithTopBar
		implements onPopupWindowItemClickListener {

	public static final String ACTION_PURCHASE_BOOK = "com.jingdong.app.reader.reading.PurchaseBook";
	public static final String BOOK_ID_KEY = "bookId";
	public static final String DOC_ID_KEY = "docId";
	public static final String DOC_SERVER_ID_KEY = "docServerId";
	public static final String IS_TRY_READ_KEY = "isTryRead";
	
	private LinearLayout containerLayout = null;
	private LinearLayout headerView = null;
	private LinearLayout bookCommentsView = null;
	private LinearLayout bookOthersLikeView = null;
	private int currentIndex = 0;
	private JDBookInfo bookInfo = null;
	private List<JDBookComments> list = new ArrayList<JDBookComments>();
	private TextView readTime;
	private long bookId = 0;
	private long serverDocId = 0;
	private String userId = "";
	private int documentId = 0;
	private int noteCount = -1;
	private Button share_book;
//	private TextView more;
	private static SharePopupWindow sharePopupWindow = null;
	private Bitmap bitmap;
	private static final int IO_BUFFER_SIZE = 2 * 1024;
	private static final String PAPER_BOOK_SHARE_URL = URLText.JD_PAPER_BOOK_SHARE_URL+"wareId=";
	private static final String EBOOK_SHARE_URL = "http://e.m.jd.com/ebook/";
	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private JDBook ebook = null;
	private boolean isTryRead = false;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private List<JDBookDetail> jdBookList = new ArrayList<JDBookDetail>();
//	private ListViewForScrollView booklist;
//	private BookListAdapter bookListAdapter;
//	private RelativeLayout relativeLayout;
	private Button buy_book;
	private boolean isAuthorBookListViewAdded = false;
	private View mAuthorBookListView;

	class Recommend {
		String name;
		String author;
		long ebookId;
		float jdPrice;
		String imageUrl;
	}

	public void initData() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}

	private List<Recommend> recommends = new ArrayList<Recommend>();

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.bookcover_recommend);
		bookId = getIntent().getLongExtra(BOOK_ID_KEY, 0);
		documentId = getIntent().getIntExtra(DOC_ID_KEY, 0);
		serverDocId = getIntent().getLongExtra(DOC_SERVER_ID_KEY, 0);
		isTryRead = getIntent().getBooleanExtra(IS_TRY_READ_KEY, false);
		userId = LoginUser.getpin();
		List<ReadNote> noteList = null;
		if (bookId > 0) {
			noteList = MZBookDatabase.instance
					.listEBookReadNote(userId, bookId);
		} else if (documentId > 0) {
			noteList = MZBookDatabase.instance.listDocReadNote(userId,
					documentId);
		}
		if (noteList != null && noteList.size() > 0) {
			noteCount = noteList.size();
		} else {
			noteCount = 0;
		}
		containerLayout = (LinearLayout) findViewById(R.id.holder);
		headerView = BookInfoStyleController
				.getBackCoverHeaderStyleView(BackCoverRecommendActivity.this);
		bookOthersLikeView = BookInfoStyleController
				.getBookInfoOtherLikeStyleView(BackCoverRecommendActivity.this);
		bookCommentsView = BookInfoStyleController.getBackCoverStyleView(
				BackCoverRecommendActivity.this, "书评", "更多书评", "写书评");
		readTime = (TextView) headerView.findViewById(R.id.readed);
//		booklist = (ListViewForScrollView) headerView
//				.findViewById(R.id.book_list);
		share_book = (Button) headerView.findViewById(R.id.share_book);
		buy_book = (Button) headerView.findViewById(R.id.buy_book);
		if (isTryRead) {
			buy_book.setVisibility(View.VISIBLE);
			buy_book.setText(R.string.buy_complement);
		}else {
			buy_book.setVisibility(View.GONE);
		}
//		more = (TextView) headerView.findViewById(R.id.more);
//		relativeLayout = (RelativeLayout) headerView
//				.findViewById(R.id.relativeLayout);
		containerLayout.addView(headerView);
//		bookListAdapter = new BookListAdapter(BackCoverRecommendActivity.this);
//		booklist.setAdapter(bookListAdapter);
		queryReadData();
		if (LoginUser.isLogin() && bookId > 0) {
			getBookInfo(bookId);
		} else {
			//导入的书UI只显示阅读时间和笔记数
			share_book.setVisibility(View.GONE);
//			relativeLayout.setVisibility(View.GONE);
//			headerView.findViewById(R.id.divider).setVisibility(View.GONE);
			int timeInSec = (int) MZBookDatabase.instance
					.getBookReadingDataTime(bookId, documentId);
			if (timeInSec >= 3600) {
				int time = timeInSec / 3600;
				String timeText = String.valueOf(time);
				timeText += getResources().getString(R.string.hour);
				int second = (timeInSec - time * 3600) / 60;
				readTime.setText("共" + timeText + second + "分钟,笔记" + noteCount
						+ "条。");
			} else if (timeInSec >= 60 && timeInSec < 3600) {
				int time = timeInSec / 60;
				String timeText = String.valueOf(time);
				timeText += getResources().getString(R.string.minute);
				readTime.setText("共" + timeText + ",笔记" + noteCount + "条。");
			} else {
				String timeText = String.valueOf(timeInSec);
				timeText += getResources().getString(R.string.second);
				readTime.setText("共" + timeText + ",笔记" + noteCount + "条。");
			}
		}
		
		buy_book.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isTryRead) {
					Intent intent = new Intent(BackCoverRecommendActivity.ACTION_PURCHASE_BOOK);
				    LocalBroadcastManager.getInstance(BackCoverRecommendActivity.this).sendBroadcast(intent);
                	finish();
					return;
				}
			}
		});

		share_book.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sharePopupWindow = new SharePopupWindow(
						BackCoverRecommendActivity.this);
				sharePopupWindow.setListener(BackCoverRecommendActivity.this);
				sharePopupWindow
						.show(getTopBarView().getSubmenurightOneImage());
			}
		});

//		more.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(BackCoverRecommendActivity.this, BookStoreBookListActivity.class);
//				
//				intent.putExtra("list_type", BookStoreBookListActivity.TYPE_AUTHOR_OTHER_BOOKS);
//				intent.putExtra("keys", bookInfo.detail.author);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				startActivity(intent);
//			}
//		});

//		booklist.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				JDBookDetail eBook = jdBookList.get(position);
//				Intent intent = new Intent(BackCoverRecommendActivity.this,
//						BookInfoNewUIActivity.class);
//				intent.putExtra("bookid", eBook.getEbookId());
//				startActivity(intent);
//			}
//		});
	}

	private void initView() {
		containerLayout.addView(bookOthersLikeView);
		containerLayout.addView(bookCommentsView);
		LinearLayout emptyLayout = (LinearLayout) LayoutInflater.from(
				BackCoverRecommendActivity.this).inflate(
				R.layout.backcover_empty, null);
		containerLayout.addView(emptyLayout);
		bookOthersLikeView.setVisibility(View.GONE);
		bookCommentsView.setVisibility(View.GONE);
	}

	public void queryReadData() {

		if (!LoginUser.isLogin()) {
			return;
		}
		if (!NetWorkUtils.isNetworkConnected(BackCoverRecommendActivity.this)) {
			return;
		}
		RequestParams request = RequestParamsPool.getReadingData(bookId,
				serverDocId);
		WebRequestHelper
				.get(URLText.bookReadingDataUrl, request, true,
						new MyAsyncHttpResponseHandler(
								BackCoverRecommendActivity.this) {

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {
								try {
									String response = new String(responseBody,
											"utf-8");
									JSONObject obj = new JSONObject(response);
									int timeInSec = obj
											.optInt("total_time_in_seconds");
									if (timeInSec >= 3600) {
										int time = timeInSec / 3600;
										String timeText = String.valueOf(time);
										timeText += getResources().getString(
												R.string.hour);
										int second = (timeInSec - time * 3600) / 60;
										readTime.setText("共" + timeText
												+ second + "分钟,笔记" + noteCount
												+ "条。");
									} else if (timeInSec >= 60
											&& timeInSec < 3600) {
										int time = timeInSec / 60;
										String timeText = String.valueOf(time);
										timeText += getResources().getString(
												R.string.minute);
										readTime.setText("共" + timeText + ",笔记"
												+ noteCount + "条。");
									} else {
										String timeText = String
												.valueOf(timeInSec);
										timeText += getResources().getString(
												R.string.second);
										readTime.setText("共" + timeText + ",笔记"
												+ noteCount + "条。");
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {

							}
						});
	}

	private void getBookInfo(long bookid) {
		WebRequestHelper
				.get(URLText.JD_BASE_URL, RequestParamsPool
						.getBookInfoParams(bookid,null), true,
						new MyAsyncHttpResponseHandler(
								BackCoverRecommendActivity.this) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {

							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String result = new String(responseBody);
								try {
									JSONObject json = new JSONObject(result);
									String codeStr = json.optString("code");
									if (codeStr != null && codeStr.equals("0")) {
										bookInfo = GsonUtils.fromJson(result,
												JDBookInfo.class);

										if (bookInfo != null
												&& bookInfo.detail != null) {
											mHandler.sendMessage(mHandler
													.obtainMessage(4));
										}
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
		}else if (authorList.contains(",")) {
			String[] authorArr = authorList.split(",");
			processMultiAuthorBookList(authorArr);
		}else if (authorList.contains(";")) {
			String[] authorArr = authorList.split(";");
			processMultiAuthorBookList(authorArr);
		}else if (authorList.contains("；")) {
			String[] authorArr = authorList.split("；");
			processMultiAuthorBookList(authorArr);
		}else {
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
				new MyAsyncHttpResponseHandler(BackCoverRecommendActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						Toast.makeText(BackCoverRecommendActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String result = new String(responseBody);
						JDBook ebook = GsonUtils.fromJson(result, JDBook.class);
						setupAuthorBookList(new String[] { author }, ebook.bookList);
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
					new MyAsyncHttpResponseHandler(BackCoverRecommendActivity.this) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							Toast.makeText(BackCoverRecommendActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
							String result = new String(responseBody);
							JDBook ebook = GsonUtils.fromJson(result, JDBook.class);
							bookList.addAll(ebook.bookList);
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
		MZLog.d("D", "before::bookList size="+bookList.size()+"\n"+GsonUtils.toJson(bookList));
		if (bookList == null || bookList.size() == 0) {
			adjustOtherLikeLayoutParams();
			mHandler.sendMessage(mHandler.obtainMessage(5));
			return;
		}
		// 1.过滤作者
		List<JDBookDetail> filterBookList = filterAuthorList(authorArr, bookList);
		MZLog.d("D", "aifer::bookList size="+filterBookList.size()+"\n"+GsonUtils.toJson(bookList));

		String moretxt = filterBookList.size() > 3 ? "更多" : "";
		List<StoreBook> storeBookList = jDBookDetail2StoreBook(filterBookList);
		if (storeBookList.size() == 0) {
			adjustOtherLikeLayoutParams();
			mHandler.sendMessage(mHandler.obtainMessage(5));
			return;
		}
		mAuthorBookListView = RankingListViewStyleController.getBookCoverListStyleView(BackCoverRecommendActivity.this, "作者的其他图书", moretxt, 3, 1, storeBookList,
				new OnHeaderActionClickListener() {

					@Override
					public void onHeaderActionClick() {
						Intent intent = new Intent(BackCoverRecommendActivity.this, BookStoreBookListActivity.class);

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
		mHandler.sendMessage(mHandler.obtainMessage(5));
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
		}else{
			//遍历当前图书的作者
			for (int i = 0; i < currentAuthors.length; i++) {
				String currentAuthor = currentAuthors[i];
				for (int j = 0; j < auhorArr.length; j++) {
					String bookAuthor = auhorArr[j];
					if (currentAuthor != null && bookAuthor != null ) {
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

			if (bookDetail.getEbookId() != bookId) {
				storeBooks.add(book);
			}
		}
		return storeBooks;
	}
	
//	private synchronized void searchBooks(final String key) {
//		if (TextUtils.isEmpty(key)){
//			relativeLayout.setVisibility(View.GONE);
//			return;
//		}
//			
//		WebRequestHelper
//				.get(URLText.JD_BASE_URL, RequestParamsPool
//						.getSearchStoreEbookParams(currentSearchPage + "",
//								perSearchCount + "", key, ""),
//						new MyAsyncHttpResponseHandler(
//								BackCoverRecommendActivity.this) {
//
//							@Override
//							public void onFailure(int arg0, Header[] arg1,
//									byte[] arg2, Throwable arg3) {
//							}
//
//							@Override
//							public void onResponse(int statusCode,
//									Header[] headers, byte[] responseBody) {
//
//								String result = new String(responseBody);
//								Log.d("cj", "result=============>>>>>>>>>>" + result);
//								ebook = GsonUtils
//										.fromJson(result, JDBook.class);
//								
//								if (ebook != null
//										&& (ebook.getCode().equals("51") || (ebook
//												.getCode().equals("0") && ebook
//												.getResultCount() <= 1))) {
//									relativeLayout.setVisibility(View.GONE);
//									mHandler.sendMessage(mHandler.obtainMessage(5));
//								} else if (ebook == null) {
//									mHandler.sendMessage(mHandler.obtainMessage(5));
//									return;
//								}
//
//								if (ebook != null
//										&& ebook.getCode().equals("0")) {
//									currentSearchPage++;
//
//									if (ebook.getBookList() != null
//											&& currentSearchPage >= ebook.totalPage) {
//										initData();
//									} else {
//										noMoreBookOnSearch = false;
//									}
//
//									List<JDBookDetail> all = new ArrayList<JDBookDetail>();
//									for (int i = 0; i < ebook.getBookList()
//											.size(); i++) {
//										if (bookInfo.detail.bookId != ebook
//												.getBookList().get(i)
//												.getEbookId()) {
//											JDBookDetail book = ebook
//													.getBookList().get(i);
//											all.add(book);
//										}
//									}
//									jdBookList.addAll(all);
//									mHandler.sendMessage(mHandler.obtainMessage(5));
//								}
//								inLoadingMoreOnSearch = false;
//							}
//						});
//	}
//
//	private class BookListAdapter extends BaseAdapter {
//
//		private Context context;
//
//		class ViewHolder {
//
//			TextView bookTitle;
//			TextView bookAuthor;
//			TextView bookDesc;
//			ImageView bookCover;
//			ImageView imageViewLabel;
//		}
//
//		BookListAdapter(Context context) {
//			this.context = context;
//		}
//
//		@Override
//		public int getCount() {
//			return jdBookList.size() > 5 ? 5 : jdBookList.size();
//		}
//
//		@Override
//		public Object getItem(int position) {
//			return position;
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(final int position, View convertView,
//				ViewGroup parent) {
//
//			final ViewHolder holder;
//
//			if (convertView == null) {
//				convertView = LayoutInflater.from(context).inflate(
//						R.layout.bookstore_search_list_item, parent, false);
//				holder = new ViewHolder();
//				holder.bookTitle = (TextView) convertView
//						.findViewById(R.id.bookstore_search_user_book_name);
//				holder.bookAuthor = (TextView) convertView
//						.findViewById(R.id.bookstore_search_user_book_author);
//				// holder.imageView = (ImageView) convertView
//				// .findViewById(R.id.action_button);
//
//				holder.bookCover = (ImageView) convertView
//						.findViewById(R.id.bookstore_search_user_book_cover);
//
//				holder.bookDesc = (TextView) convertView
//						.findViewById(R.id.bookstore_search_book_desc);
//
//				holder.imageViewLabel = (ImageView) convertView
//						.findViewById(R.id.imageViewLabel);
//				convertView.setTag(holder);
//			} else {
//				holder = (ViewHolder) convertView.getTag();
//			}
//
//			final JDBookDetail eBook = jdBookList.get(position);
//			holder.bookTitle.setText(eBook.getName());
//			holder.bookAuthor
//					.setText("null".equals(eBook.getAuthor()) ? getString(R.string.author_unknown)
//							: eBook.getAuthor());
//			String info = eBook.getInfo();
//			if (info != null) {
//				info = info.replaceAll("^[　 ]*", "");
//				info = info.replaceAll("\\s+", "");
//				holder.bookDesc.setText(info);
//			} else {
//				holder.bookDesc.setText("");
//			}
//			holder.bookDesc.setVisibility(View.VISIBLE);
//			if (!eBook.isEBook()) {
//				holder.imageViewLabel.setBackground(getResources().getDrawable(
//						R.drawable.badge_coverlabel_paper));
//			} else {
//				holder.imageViewLabel.setBackground(null);
//			}
//			ImageLoader.getInstance().displayImage(eBook.getImageUrl(),
//					holder.bookCover, GlobalVarable.getCutBookDisplayOptions());
//			return convertView;
//		}
//	}

	public void getBookOthersLike(long bookid) {
		WebRequestHelper
				.get(URLText.JD_BASE_URL, RequestParamsPool
						.getBookOthersLikeParams(bookid), true,
						new MyAsyncHttpResponseHandler(
								BackCoverRecommendActivity.this) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								updateOthersLikeView();
							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String result = new String(responseBody);
								MZLog.d("AAA", result);

								try {

									JSONObject object = new JSONObject(result);
									JSONArray recommendsObject = object
											.optJSONArray("recommend");

									if (recommendsObject != null
											&& recommendsObject.length() > 0) {
										recommends.clear();
										for (int i = 0; i < recommendsObject
												.length(); i++) {
											Recommend recommend = GsonUtils
													.fromJson(recommendsObject
															.getString(i),
															Recommend.class);
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

	public void getBookComments(final String type, long bookid, int page) {

		RequestParams params = null;

		if (type.equals("ebook"))
			params = RequestParamsPool.getBookCommentsParams("ebook", bookid,
					page + "");
		else {
			params = RequestParamsPool.getBookCommentsParams("paperBook",
					bookInfo.detail.paperBookId, page + "");
		}

		WebRequestHelper
				.get(URLText.JD_BASE_URL, params, true,
						new MyAsyncHttpResponseHandler(
								BackCoverRecommendActivity.this) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {

							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String result = new String(responseBody);
								String currentTime = null;
								try {
									JSONObject object = new JSONObject(result);

									int code = object.optInt("code");

									if (code != 0){
										updateBookCommentsView(currentTime);
										return;
									}

									currentTime = object.getString("currentTime");
									JSONObject reviews = object
											.optJSONObject("reviews");

									JSONArray array = reviews
											.optJSONArray("list");

									if (array != null && array.length() > 0) {

										for (int i = 0; i < array.length(); i++) {
											JDBookComments comments = GsonUtils.fromJson(array.getString(i),JDBookComments.class);
											list.add(comments);
										}
									}else{
										if (type.equals("ebook"))
											getBookComments("paperBook", bookInfo.detail.paperBookId, 1);
									}
									
									if(list.size() >= 3){
										updateBookCommentsView(currentTime);
									}else if(type.equals("ebook")){
										getBookComments("paperBook", bookInfo.detail.paperBookId, 1);
									}else if(type.equals("paperBook")){
										updateBookCommentsView(currentTime);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
	}

	public void updateChildOfOthersLikeView(final List<Recommend> sublist) {
		LinearLayout container = (LinearLayout) bookOthersLikeView
				.findViewById(R.id.bookHolder);

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

						TalkingDataUtil.onBookDetailEvent(
								BackCoverRecommendActivity.this, "还喜欢的书_"
										+ sublist.get(index).name);

						Intent intent2 = new Intent(
								BackCoverRecommendActivity.this,
								BookInfoNewUIActivity.class);
						intent2.putExtra("bookid", sublist.get(index).ebookId);
						startActivity(intent2);
					}
				});

				child.setVisibility(View.VISIBLE);
				ImageView bookcover = (ImageView) child
						.findViewById(R.id.bookcover);
				TextView bookname = (TextView) child
						.findViewById(R.id.bookname);
				TextView bookAuthor = (TextView) child
						.findViewById(R.id.bookauthor);

				ImageLoader.getInstance().displayImage(sublist.get(i).imageUrl,
						bookcover,
						GlobalVarable.getCutBookDisplayOptions(false));
				// MZLog.d("AAA", "url::"+sublist.get(i).imageUrl);
				bookname.setText(sublist.get(i).name);
				bookAuthor
						.setText("null".equals(sublist.get(i).author) ? getString(R.string.author_unknown)
								: sublist.get(i).author);
			} else {
				child.setVisibility(View.GONE);
			}

		}
	}

	public void updateOthersLikeView() {

		if (bookOthersLikeView != null) {
			bookOthersLikeView.setVisibility(View.VISIBLE);
			TextView title = (TextView) bookOthersLikeView
					.findViewById(R.id.title);
			final TextView action = (TextView) bookOthersLikeView
					.findViewById(R.id.action);
			// final ImageView refreshIcon = (ImageView) bookOthersLikeView
			// .findViewById(R.id.refresh_action);
			final LinearLayout llRefresh = (LinearLayout) bookOthersLikeView
					.findViewById(R.id.ll_book_detail_other_like_refresh);

			title.setText("读过此书的人还喜欢");
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
					List<Recommend> sublist = recommends.subList(currentIndex,end);
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

	protected void updateBookCommentsView(String currentTime) {
		
		if (bookCommentsView != null) {
			bookCommentsView.setVisibility(View.VISIBLE);
			FrameLayout footer_layout = (FrameLayout) bookCommentsView
					.findViewById(R.id.footer_layout);
			TextView header_right_name = (TextView) bookCommentsView
					.findViewById(R.id.header_right_name);
			if (isTryRead) {
				header_right_name.setVisibility(View.GONE);
			}
			header_right_name.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (bookInfo != null && bookInfo.detail != null) {

						TalkingDataUtil.onBookDetailEvent(
								BackCoverRecommendActivity.this, "写书评");

						Intent it2 = new Intent(BackCoverRecommendActivity.this, TimelineBookListCommentsActivity.class);
						it2.putExtra("type", "direct_comment");
						it2.putExtra("book_id", bookInfo.detail.bookId+"");
						it2.putExtra("book_name", bookInfo.detail.bookName);
						it2.putExtra("book_author", bookInfo.detail.author);
						it2.putExtra("book_cover", bookInfo.detail.logo);
						startActivity(it2);
					}
				}
			});

			TextView textView = (TextView) bookCommentsView.findViewById(R.id.header_name);
			
			LinearLayout container1 = (LinearLayout) bookCommentsView
					.findViewById(R.id.container);
			
			if (list == null || list.size() == 0) {
				textView.setText(R.string.no_comment);
				for (int i = 0; i < container1.getChildCount(); i++) {
					View child = container1.getChildAt(i);
					child.setVisibility(View.GONE);
				}
				bookCommentsView.findViewById(R.id.footer_layout)
						.setVisibility(View.GONE);
				return;
			}else{
				textView.setText("书评");
				for (int i = 0; i < container1.getChildCount(); i++) {
					View child = container1.getChildAt(i);
					child.setVisibility(View.VISIBLE);
				}
				bookCommentsView.findViewById(R.id.footer_layout).setVisibility(View.VISIBLE);
			}
			
			footer_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					TalkingDataUtil.onBookDetailEvent(
							BackCoverRecommendActivity.this, "更多书评");

					Intent it = new Intent(BackCoverRecommendActivity.this,
							BookCommentsListActivity.class);
					if (bookInfo.detail.isEBook)
						it.putExtra("booktype", "ebook");
					else {
						it.putExtra("booktype", "paperBook");
					}
					it.putExtra("paperbookid", bookInfo.detail.paperBookId);
					it.putExtra("bookid", bookInfo.detail.bookId);
					
					it.putExtra("isShowBookCommentBtn", true);
					it.putExtra("bookName", bookInfo.detail.bookName);
					it.putExtra("book_author", bookInfo.detail.author);
					it.putExtra("book_cover", bookInfo.detail.logo);
					startActivity(it);

				}
			});
			LinearLayout container = (LinearLayout) bookCommentsView
					.findViewById(R.id.container);

			for (int i = 0; i < container.getChildCount(); i++) {
				View child = container.getChildAt(i);
				if (i < list.size()) {
					TextView title = (TextView) child.findViewById(R.id.title);
					TextView content = (TextView) child
							.findViewById(R.id.content);
					RatingBar ratingBar = (RatingBar) child
							.findViewById(R.id.rating);

					title.setText(list.get(i).nickname);
					content.setText(list.get(i).contents);
					ratingBar.setRating((float) list.get(i).score);
					
					RoundNetworkImageView avatar = (RoundNetworkImageView) child.findViewById(R.id.thumb_nail);
					TextView time=(TextView) child.findViewById(R.id.time);
					//单条评论
					final JDBookComments jDBookComments=list.get(i);
					
					if(null!=jDBookComments.userHead && !jDBookComments.userHead.equals(""))
						ImageLoader.getInstance().displayImage("http://"+jDBookComments.userHead, avatar);
					title.setText(jDBookComments.nickname);
					content.setText(jDBookComments.contents);
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
							Intent intent = new Intent(BackCoverRecommendActivity.this, UserActivity.class);
							intent.putExtra(UserActivity.NAME, jDBookComments.pin);
							startActivity(intent);
						}
					});
					
					//点击名称转到个人主页
					title.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(BackCoverRecommendActivity.this, UserActivity.class);
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

	@Override
	public void onPopupWindowWeixinClick() {
		TalkingDataUtil.onBookDetailEvent(this, "分享-微信好友");
		if(null == bookInfo || (null != bookInfo && null == bookInfo.detail)) {
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
		sharePopupWindow.shareToWeixin(this, bookInfo.detail.bookName, author1 + "\n" + str1, bookInfo.detail.logo, getShareUrl(), 0);
		post(LoginUser.getpin(), String.valueOf(bookInfo.detail.bookId));
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

	@Override
	public void onPopupWindowSinaClick() {
		TalkingDataUtil.onBookDetailEvent(this, "分享-新浪微博");
		if(null == bookInfo || (null != bookInfo && null == bookInfo.detail)) {
			return;
		}
		
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

		sharePopupWindow.shareToWeibo(this, author + " - " + "\n" + bookname + "\n" + str, "" + str, bookInfo.detail.logo, getShareUrl() + "     ", "");
		post(LoginUser.getpin(), String.valueOf(bookInfo.detail.bookId));
		
	}

	@Override
	public void onPopupWindowWeixinFriend() {
		TalkingDataUtil.onBookDetailEvent(this, "分享-微信朋友圈");
		
		if(null == bookInfo) {
			return;
		}
		
		if(null == bookInfo.detail) {
			return;
		}
		
		String str2;
		if (bookInfo != null && bookInfo.detail != null && bookInfo.detail.isFree) {
			str2 = "可免费下载";
		} else {
			str2 = "书城有售";
		}
		
		sharePopupWindow.shareToWeixin(this, bookInfo.detail.bookName + "-" + str2, null, bookInfo.detail.logo, getShareUrl(), 1);
		post(LoginUser.getpin(), String.valueOf(bookInfo.detail.bookId));
	}

	@Override
	public void onPopupWindowMore() {
		TalkingDataUtil.onBookDetailEvent(this, "分享-更多");
		if(null == bookInfo || (null != bookInfo && null == bookInfo.detail)) {
			return;
		}
		
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
		sharePopupWindow.More(this, bookInfo.detail.bookName + "-" + str3, null, getShareUrl());
	}

	@Override
	public void onPopupWindowCancel() {
		TalkingDataUtil.onBookDetailEvent(this, "分享浮窗-关闭");
		sharePopupWindow.dismiss();
	}

	@Override
	public void onPopupWindowCommuity() {
		if (null == bookInfo) {
			return;
		}
		
		if (null == bookInfo.detail) {
			return;
		}
		
		Intent itIntent = new Intent(BackCoverRecommendActivity.this,
				TimelineBookListCommentsActivity.class);
		itIntent.putExtra("type", "share_to_comunity");
		itIntent.putExtra("book_id", bookInfo.detail.bookId);
		itIntent.putExtra("book_name", bookInfo.detail.bookName);
		itIntent.putExtra("book_author", bookInfo.detail.author);
		itIntent.putExtra("book_cover", bookInfo.detail.logo);
		startActivity(itIntent);
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			String share_url = null;
			if (bookInfo.detail.isEBook) {
				share_url = EBOOK_SHARE_URL + bookInfo.detail.bookId + ".html";
			} else {
				share_url = PAPER_BOOK_SHARE_URL + bookInfo.detail.bookId;
			}

			super.handleMessage(msg);
			switch (msg.what) {
			case 0:// 新浪微博
//				String author = ("null".equals(bookInfo.detail.author)
//						|| "".equals(bookInfo.detail.author) || bookInfo.detail.author == null) ? getString(R.string.author_unknown)
//						: bookInfo.detail.author;
//				String str = null;
//				if (bookInfo.detail.info != null) {
//					if (bookInfo.detail.info.length() > 30) {
//						str = Html.fromHtml(bookInfo.detail.info).toString()
//								.substring(0, 30)
//								+ "...";
//						Pattern p = Pattern.compile("\\s*|\t|\r|\n");
//						Matcher m = p.matcher(str);
//						str = m.replaceAll("");
//					} else {
//						str = Html.fromHtml(bookInfo.detail.info).toString();
//					}
//				} else {
//					str = "暂无简介";
//				}
//
//				String bookname = null;
//				bookname = bookInfo.detail.bookName;
//				if (bookname.endsWith("》")) {
//					bookname = bookInfo.detail.bookName;
//				} else {
//					bookname = "《" + bookInfo.detail.bookName + "》";
//				}
//				if (bitmap != null) {
//
//					sharePopupWindow.sina(BackCoverRecommendActivity.this,
//							author + " - " + "\n" + bookname + "\n" + str, ""
//									+ str, bookInfo.detail.logo, share_url + "     ", "");
//					BackCoverRecommendActivity.this.post(LoginUser.getpin(),
//							String.valueOf(bookInfo.detail.bookId));
//				}
				break;
			case 1:// 微信好友
//				String string = bookInfo.detail.author;
//				String author1 = ("null".equals(string) || "".equals(string) || string == null) ? getString(R.string.author_unknown)
//						: string;
//				String str1 = null;
//				if (bookInfo.detail.info != null) {
//					if (bookInfo.detail.info.length() > 30) {
//						str1 = bookInfo.detail.info.substring(0, 30);
//					} else {
//						str1 = bookInfo.detail.info;
//					}
//				} else {
//					str1 = "暂无简介";
//				}
//
//				MZLog.d("cj", "str======>>>" + str1);
//				if (bitmap != null) {
//					MZLog.d("cj", "ddd=========>>");
//					sharePopupWindow.weixin(BackCoverRecommendActivity.this,
//							bookInfo.detail.bookName, author1 + "\n" + str1,
//							bookInfo.detail.logo, share_url, 0);
//					BackCoverRecommendActivity.this.post(LoginUser.getpin(),
//							String.valueOf(bookInfo.detail.bookId));
//				}
				break;
			case 2:// 微信朋友圈
//				String str2;
//				if (bookInfo.detail.isFree) {
//					str2 = "可免费下载";
//				} else {
//					str2 = "书城有售";
//				}
//				if (bitmap != null) {
//					sharePopupWindow.weixin(BackCoverRecommendActivity.this,
//							bookInfo.detail.bookName + "-" + str2, null,
//							bookInfo.detail.logo, share_url, 1);
//					BackCoverRecommendActivity.this.post(LoginUser.getpin(),
//							String.valueOf(bookInfo.detail.bookId));
//				}
				break;
			case 3:// 更多
//				String author3 = ("null".equals(bookInfo.detail.author)
//						|| "".equals(bookInfo.detail.author) || bookInfo.detail.author == null) ? getString(R.string.author_unknown)
//						: bookInfo.detail.author;
//				String str3 = null;
//				if (bookInfo.detail.info != null) {
//					if (bookInfo.detail.info.length() > 30) {
//						str3 = bookInfo.detail.info.substring(0, 30);
//					} else {
//						str3 = bookInfo.detail.info;
//					}
//				} else {
//					str3 = "暂无简介";
//				}
//				sharePopupWindow.More(BackCoverRecommendActivity.this,
//						bookInfo.detail.bookName + "-" + str3, null,
//						share_url);
				break;
			case 4:
				if (!isAuthorBookListViewAdded && !bookInfo.detail.author.equals("佚名")) {
					getAuthorBookList(bookInfo.detail.author);
				}else {
					mHandler.sendMessage(mHandler.obtainMessage(5));
				}
//				searchBooks(bookInfo.detail.author);
				break;
			case 5:
				initView();
				getBookOthersLike(bookId);
				mHandler.sendMessage(mHandler.obtainMessage(6));
				break;
			case 6:
				if (bookInfo.detail.isEBook) {
					getBookComments("ebook",
							bookInfo.detail.bookId,
							1);
				} else {
					getBookComments(
							"paperBook",
							bookInfo.detail.paperBookId,
							1);
				}
				break;
			default:
				break;
			}
		}

	};

	/**
	 * @param urlpath
	 * @return Bitmap 根据图片url获取图片对象
	 */
	public static Bitmap getBitMBitmap(String urlpath) {
		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;
		try {
			in = new BufferedInputStream(new URL(urlpath).openStream(),
					IO_BUFFER_SIZE);
			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
			copy(in, out);
			out.flush();
			byte[] data = dataStream.toByteArray();
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			data = null;
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}

	private void post(String pin, String bookid) {
		WebRequestHelper
				.post(URLText.SHARE_URL, RequestParamsPool.getShareParams(pin,
						bookid, "Book"), true, new MyAsyncHttpResponseHandler(
						BackCoverRecommendActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);

						MZLog.d("cj", "result=======>>" + result);
						JSONObject jsonObj;
						try {
							jsonObj = new JSONObject(new String(responseBody));
							if (jsonObj != null) {
								JSONObject desJsonObj = null;
								String code = jsonObj.optString("code");
								if (Integer.parseInt(code) == 0) {
									String message = jsonObj
											.optString("message");
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
	protected void onDestroy() {
		super.onDestroy();
		isAuthorBookListViewAdded = false;
	}
}
