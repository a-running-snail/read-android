package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity;
import com.jingdong.app.reader.entity.extra.JDBook;
import com.jingdong.app.reader.entity.extra.JDBookDetail;
import com.jingdong.app.reader.entity.extra.StoreBook;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.JavaUtil;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.xlistview.XListView;
import com.jingdong.app.reader.view.xlistview.XListView.IXListViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class BookStoreBookListActivity extends BaseActivityWithTopBar implements IXListViewListener {

	private static final int SHOW_LOADING = 0x03;
	private static final int SHOW_NORMAL = 0x04;
	private static final int SHOW_NET_ERROR = 0x05;

	private XListView mListView = null;
	private int currentPage = 1;
	private int pageSize = 20;
	private boolean noMore = false;
	private BooklistAdapter adapter = null;
	private List<StoreBook> list = new ArrayList<StoreBook>();

	private int fid = -1;
	private int ftype = -1;
	private int relationType = -1;
	private String showName = "";

	private int listtype = -1;
	private long catId = -1;
	private boolean boolNew = false;// 是否是获取新书排行，false为热销排行忙，不传递默认为热销排行
	private String bannerImage = "";

	public static final int TYPE_CATAGORY = 101;

	public static final int TYPE_BORROWING = 102;// 限时借阅
	public static final int TYPE_RECOMMEND = 103;// 智能推荐
	public static final int TYPE_PAPER_BOOK = 104;// 纸书商城
	public static final int TYPE_AUTHOR_OTHER_BOOKS = 105;// 作者其他书籍

	public static final String LIST_TYPE = "list_type";
	private View mLoadingProgress;
	private View mNetErrorView;
	private Button mRetryButton;
	private String mKey;
	private String from;
	private int position;
	private String type;
	private String showInfo;
	public String relateLink;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_booklist);

		mListView = (XListView) findViewById(R.id.listview);
		mListView.setPullLoadEnable(false);
		mListView.setPullRefreshEnable(false);
		mListView.setXListViewListener(this);
		mErrorLayout = (EmptyLayout) findViewById(R.id.error_layout);

		// listView.setOnScrollListener(this);

		adapter = new BooklistAdapter();
		mListView.setAdapter(adapter);
		// 书城更多 会有以下字段
		fid = getIntent().getIntExtra("fid", -1);
		ftype = getIntent().getIntExtra("ftype", -1);
		relationType = getIntent().getIntExtra("relationType", -1);
		showName = getIntent().getStringExtra("showName");
		showInfo = getIntent().getStringExtra("showInfo");
		relateLink = getIntent().getStringExtra("relateLink");
		
		from = getIntent().getStringExtra("from");
		position = getIntent().getIntExtra("position", 0);
		type=getIntent().getStringExtra("type");

		// 是分类列表才有以下字段
		listtype = getIntent().getIntExtra(LIST_TYPE, -1);
		String titleString = getIntent().getStringExtra("title");
		catId = getIntent().getLongExtra("catId", -1);

		// 纸书商城特别字段
		boolNew = getIntent().getBooleanExtra("boolNew", false);

		// 需要banner的list 会有bannerImg

		bannerImage = getIntent().getStringExtra("bannerImg");

		if (!TextUtils.isEmpty(bannerImage)) {
			mHeaderView = new ImageView(BookStoreBookListActivity.this);
			mHeaderView.setScaleType(ScaleType.FIT_XY);
			// 服务器不想改 那就拉大...
			int screenWidth = (int) ScreenUtils.getWidthJust(BookStoreBookListActivity.this);// px
			int image_height = (int) (0.6 * screenWidth);

			AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, image_height);
			mHeaderView.setLayoutParams(params);

			ImageLoader.getInstance().displayImage(bannerImage, mHeaderView, GlobalVarable.getDefaultBookDisplayOptions());
			mListView.addHeaderView(mHeaderView, null, false);
			hasHeaderView  = true;
			mHeaderView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!TextUtils.isEmpty(relateLink)) {
						Intent intent = new Intent(BookStoreBookListActivity.this, WebViewActivity.class);
						intent.putExtra(WebViewActivity.UrlKey, relateLink);
						intent.putExtra(WebViewActivity.TopbarKey, true);
						intent.putExtra(WebViewActivity.BrowserKey, false);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				}
			});
		}

		switch (listtype) {
		case -1:// 普通的更多 图书列表
		{
			if (!TextUtils.isEmpty(showName)) {
				getTopBarView().setTitle(showName);
			}
			if (fid == -1 || ftype == -1 || relationType == -1) {
				Toast.makeText(BookStoreBookListActivity.this, "请求参数错误，请重试!", Toast.LENGTH_LONG).show();
				return;
			}

			else {
				// switchContentView(SHOW_LOADING);
				mErrorLayout.setOnLayoutClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
						requestData(fid, ftype, relationType);
					}
				});
				requestData(fid, ftype, relationType);
			}
		}
			break;

		case TYPE_CATAGORY:// 分类界面列表

			if (!TextUtils.isEmpty(titleString)) {
				getTopBarView().setTitle(titleString);
			}
			if (catId == -1) {
				Toast.makeText(BookStoreBookListActivity.this, "请求参数错误，请重试!", Toast.LENGTH_LONG).show();
				return;
			} else {
				mErrorLayout.setOnLayoutClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
						requestCatagoryData(catId);
					}
				});
				requestCatagoryData(catId);
			}

			break;

		case TYPE_BORROWING:// 限时借阅
			if (!TextUtils.isEmpty(showName)) {
				getTopBarView().setTitle(showName);
			}
			// switchContentView(SHOW_LOADING);
			mErrorLayout.setOnLayoutClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
					requestBorrowingData();
				}
			});
			requestBorrowingData();
			break;

		case TYPE_RECOMMEND:// 智能推荐
			if (!TextUtils.isEmpty(showName)) {
				getTopBarView().setTitle(showName);
			}
			// switchContentView(SHOW_LOADING);
			mErrorLayout.setOnLayoutClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
					requestRecommendData();
				}
			});
			requestRecommendData();
			break;

		case TYPE_PAPER_BOOK:// 纸书商城
			// switchContentView(SHOW_LOADING);
			mErrorLayout.setOnLayoutClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
					requestPaperBookData(boolNew);
				}
			});
			requestPaperBookData(boolNew);
			break;
		case TYPE_AUTHOR_OTHER_BOOKS:
			mKey = getIntent().getStringExtra("keys");
			isPublisher = getIntent().getBooleanExtra("isPublisher", false);

			getTopBarView().setTitle(mKey);
			// switchContentView(SHOW_LOADING);
			mErrorLayout.setOnLayoutClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
					getAuthorBookList(mKey);
				}
			});
			getAuthorBookList(mKey);
			break;
		}
		mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);

		

	}


	private int index = 0;
	private boolean isPublisher;
	private EmptyLayout mErrorLayout;
	private boolean hasHeaderView = false;
	private ImageView mHeaderView;

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
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSearchStoreEbookParams(currentPage + "", pageSize + "", author, ""),
				new MyAsyncHttpResponseHandler(BookStoreBookListActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						Toast.makeText(BookStoreBookListActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String result = new String(responseBody);
						JDBook ebook = GsonUtils.fromJson(result, JDBook.class);
						setupAuthorBookList(new String[] { author }, ebook.bookList, ebook);
					}
				});
	}

	private void processMultiAuthorBookList(final String[] authorArr) {
		if (authorArr == null || authorArr.length == 0)
			return;
		final List<JDBookDetail> bookList = new ArrayList<JDBookDetail>();
		index=0;
		for (int i = 0; i < authorArr.length; i++) {
			String author = authorArr[i];
			WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSearchStoreEbookParams(currentPage + "", pageSize + "", author, ""),
					new MyAsyncHttpResponseHandler(BookStoreBookListActivity.this) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							Toast.makeText(BookStoreBookListActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
							String result = new String(responseBody);
							JDBook ebook = GsonUtils.fromJson(result, JDBook.class);
							if(ebook!=null && ebook.bookList!=null)
								bookList.addAll(ebook.bookList);
							else{
								
							}
							if (index == authorArr.length - 1) {
								setupAuthorBookList(authorArr, bookList, ebook);
							}
							index++;
						}
					});
		}
	}

	protected void setupAuthorBookList(String[] authorArr, final List<JDBookDetail> bookList, JDBook ebook) {
		MZLog.d("D", "before::bookList size=" + bookList.size() + "\n" + GsonUtils.toJson(bookList));
		if (bookList == null || bookList.size() == 0) {
			return;
		}
		// 1.过滤作者
		List<StoreBook> storeBookList = null;
		if (!isPublisher) {
			List<JDBookDetail> filterBookList = filterAuthorList(authorArr, bookList);
			storeBookList = jDBookDetail2StoreBook(filterBookList);
		} else {
			storeBookList = jDBookDetail2StoreBook(bookList);
		}
		if (storeBookList != null && storeBookList.size() != 0) {
			if (ebook.totalPage == 1) {
				noMore = true;
			} else {
				if (currentPage == ebook.totalPage) {
					noMore = true;
				}
			}
			list.addAll(storeBookList);
			adapter.notifyDataSetChanged();
			currentPage++;
			onLoadComplete();
			if (noMore) {
				mListView.setPullLoadEnable(false);
			}
		} else {
			onLoadComplete();
		}

		// switchContentView(SHOW_NORMAL);
		mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);

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

	public void requestPaperBookData(boolean boolNew) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getPaperBookStoreListParams(currentPage, pageSize, boolNew),
				new MyAsyncHttpResponseHandler(BookStoreBookListActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						// Toast.makeText(BookStoreBookListActivity.this,
						// "网络不可用，请稍后重试！", Toast.LENGTH_LONG).show();
						mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
						if (hasHeaderView) {
							mListView.removeHeaderView(mHeaderView);
						}
						onLoadComplete();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						processResponseData(responseBody);
					}
				});

	}

	public void requestBorrowingData() {
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getBookStoreBorrowingParams(currentPage, pageSize), new MyAsyncHttpResponseHandler(
				BookStoreBookListActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				// Toast.makeText(BookStoreBookListActivity.this, "请求出错了，请重试！",
				// Toast.LENGTH_LONG).show();
				// switchContentView(SHOW_NET_ERROR);
				mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
				if (hasHeaderView) {
					mListView.removeHeaderView(mHeaderView);
				}
				onLoadComplete();
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				processResponseData(responseBody);
			}
		});

	}

	/**
	 * 智能推荐列表获取数据方法
	 */
	public void requestRecommendData() {
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getBookStoreRecommendParams(), true, new MyAsyncHttpResponseHandler(
				BookStoreBookListActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				// Toast.makeText(BookStoreBookListActivity.this, "请求出错了，请重试！",
				// Toast.LENGTH_LONG).show();
				// switchContentView(SHOW_NET_ERROR);
				mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
				if (hasHeaderView) {
					mListView.removeHeaderView(mHeaderView);
				}
				onLoadComplete();
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				BookStoreModuleBookListEntity.BookStoreList tempBookStoreList = GsonUtils.fromJson(new String(responseBody),
						BookStoreModuleBookListEntity.BookStoreList.class);

				if (tempBookStoreList != null && tempBookStoreList.resultList != null) {
					list.addAll(tempBookStoreList.resultList);
					adapter.notifyDataSetChanged();
				}
				onLoadComplete();
				mListView.setPullLoadEnable(false);//智能推荐只有一页，没有更多了
				mListView.stopRefresh();
				mListView.stopLoadMore();
				// processResponseData(responseBody);
				mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
			}
		});

	}

	public void requestCatagoryData(long catId) {
		WebRequestHelper.post(URLText.JD_BASE_URL,
				RequestParamsPool.getCategoryBookListParams(String.valueOf(pageSize), String.valueOf(catId), String.valueOf(currentPage)),
				new MyAsyncHttpResponseHandler(BookStoreBookListActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//						Toast.makeText(BookStoreBookListActivity.this, "请求出错了，请重试！", Toast.LENGTH_LONG).show();
						mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
						if (hasHeaderView) {
							mListView.removeHeaderView(mHeaderView);
						}
						onLoadComplete();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						processResponseData(responseBody);
					}
				});

	}

	/**
	 * 
	 * @Title: processResponseData
	 * @Description: 处理服务端返回的数据
	 * @param @param responseBody
	 * @return void
	 * @throws
	 * @date 2015年4月10日 下午5:10:39
	 */
	private void processResponseData(byte[] responseBody) {
		MZLog.d("J.BEYOND", new String(responseBody));

		BookStoreModuleBookListEntity.BookStoreList tempBookStoreList = GsonUtils.fromJson(new String(responseBody),
				BookStoreModuleBookListEntity.BookStoreList.class);

		if (tempBookStoreList != null && tempBookStoreList.resultList != null) {
			if (tempBookStoreList.totalPage == 1) {
				noMore = true;
			} else {
				if (currentPage == tempBookStoreList.totalPage) {
					noMore = true;
				}
			}
//			//由于阅后即焚接口统计totalPage有问题，目前先加入这个条件验证是否有下一页
//			if(tempBookStoreList.resultList.size()<pageSize || tempBookStoreList.resultList.size()==0 )
//				noMore=true;
			
			list.addAll(tempBookStoreList.resultList);
			adapter.notifyDataSetChanged();
			currentPage++;
			onLoadComplete();

			if(from!=null && from.equals("special_price")){
				mListView.smoothScrollToPositionFromTop(position+1, 0);
			}
			
			if (noMore) {
				mListView.setPullLoadEnable(false);
			}
		} else {
			onLoadComplete();
		}

		// switchContentView(SHOW_NORMAL);
		mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);

	}

	public void requestData(final int fid, final int ftype, final int relationType) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getBookStoreRelateParams(fid, ftype, relationType, currentPage, pageSize,type),
				new MyAsyncHttpResponseHandler(BookStoreBookListActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						// Toast.makeText(BookStoreBookListActivity.this,
						// "网络不可用，请稍后重试！", Toast.LENGTH_LONG).show();
						mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
						if (hasHeaderView) {
							mListView.removeHeaderView(mHeaderView);
						}
						onLoadComplete();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						processResponseData(responseBody);
					}
				});
	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadMore() {
		if (!noMore) {
			// currentPage++;
			if (listtype == -1)
				requestData(fid, ftype, relationType);
			else if (listtype == TYPE_CATAGORY) {
				requestCatagoryData(catId);
			} else if (listtype == TYPE_BORROWING) {
				requestBorrowingData();
			} else if (listtype == TYPE_RECOMMEND) {
				onLoadComplete();
			} else if (listtype == TYPE_PAPER_BOOK) {
				requestPaperBookData(boolNew);
			} else if (listtype == TYPE_AUTHOR_OTHER_BOOKS) {
				getAuthorBookList(mKey);
			}
		}

		else {
			onLoadComplete();
			mListView.setPullLoadEnable(false);
		}
	}

	private void onLoadComplete() {
		mListView.setPullLoadEnable(true);
		mListView.stopRefresh();
		mListView.stopLoadMore();
	}

	class BooklistAdapter extends BaseAdapter {

		private ImageLoadingListenerImpl imageLoadingListenerImpl;

		public BooklistAdapter() {
			imageLoadingListenerImpl = new ImageLoadingListenerImpl();
		}
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null)
				convertView = LayoutInflater.from(BookStoreBookListActivity.this).inflate(R.layout.bookstore_style_book_list_item, null);
			ImageView bookCover = ViewHolder.get(convertView, R.id.book_cover);
			TextView bookname = ViewHolder.get(convertView, R.id.book_title);
			TextView bookAuthor = ViewHolder.get(convertView, R.id.book_author);
			TextView bookInfo = ViewHolder.get(convertView, R.id.book_info);
			ImageView imageViewLabel = ViewHolder.get(convertView, R.id.imageViewLabel);
			LinearLayout infoLayout = ViewHolder.get(convertView, R.id.book_info_Layout);
			LinearLayout specialPriceLayout = ViewHolder.get(convertView, R.id.special_price_Layout);
			TextView originalPrice = ViewHolder.get(convertView, R.id.original_price);
			TextView specialPrice = ViewHolder.get(convertView, R.id.special_price);
			TextView priceLable = ViewHolder.get(convertView, R.id.price_lable);
			TextView jdpriceLable = ViewHolder.get(convertView, R.id.jdprice_lable);
			LinearLayout priceLayout = ViewHolder.get(convertView, R.id.priceLayout);
			LinearLayout jdpriceLayout = ViewHolder.get(convertView, R.id.jdpriceLayout);
			String url = "";
			String info = "";
			long bookid = 0;

			if (listtype == TYPE_CATAGORY) {
				url = list.get(position).imageUrl;
				info = list.get(position).info;
				bookid = list.get(position).ebookId;
			} else if (listtype == TYPE_PAPER_BOOK) {
				url = list.get(position).imageUrl;
				info = list.get(position).info;
				bookid = list.get(position).paperBookId;
			} else {
				url = list.get(position).imageUrl;
				info = list.get(position).info;
				bookid = list.get(position).ebookId;
			}
			ImageLoader.getInstance().displayImage(url, bookCover, GlobalVarable.getCutBookDisplayOptions(false),imageLoadingListenerImpl);
			bookname.setText(list.get(position).name);

			bookAuthor.setText("null".equals(list.get(position).author) ? getString(R.string.author_unknown) : list.get(position).author);
			if (!list.get(position).isEBook) {
				imageViewLabel.setBackgroundDrawable(getResources().getDrawable(R.drawable.badge_coverlabel_paper));
			} else {
				imageViewLabel.setBackgroundDrawable(null);
			}
			
			boolean showPrice = false;
			if(!TextUtils.isEmpty(showInfo)){
				if(showInfo.contains("discount")){
					showPrice = true;
				}else
					showPrice = false;
			}

			if(type !=null && type.equals("specialPrice")){
				infoLayout.setVisibility(View.GONE);
				specialPriceLayout.setVisibility(View.VISIBLE);
				
				priceLable.setVisibility(View.VISIBLE);
				jdpriceLable.setVisibility(View.VISIBLE);
				originalPrice.setText("￥"+list.get(position).price);
				originalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //删除线
				specialPrice.setText("￥"+list.get(position).jdPrice);
			}else if(showPrice){
				infoLayout.setVisibility(View.GONE);
				specialPriceLayout.setVisibility(View.VISIBLE);
				
				if(showInfo.contains("price")){
					priceLable.setVisibility(View.VISIBLE);
					originalPrice.setText("￥"+list.get(position).price);
					originalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //删除线
				}else{
					priceLayout.setVisibility(View.GONE);
					priceLable.setVisibility(View.GONE);
				}
					
				if(showInfo.contains("jdPrice")){
					jdpriceLable.setVisibility(View.VISIBLE);
					specialPrice.setText("￥"+list.get(position).jdPrice);
				}else{
					jdpriceLable.setVisibility(View.GONE);
					jdpriceLayout.setVisibility(View.GONE);
				}
					
			}else{
				infoLayout.setVisibility(View.VISIBLE);
				specialPriceLayout.setVisibility(View.GONE);
				if (info != null) {
					info = info.replaceAll("^[　 ]*", "");
					info = info.replaceAll("\\s+", "");
					bookInfo.setText(Html.fromHtml(info));
				} else {
					bookInfo.setText("");
				}
			}

			final long tempBookid = bookid;
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent2 = new Intent(BookStoreBookListActivity.this, BookInfoNewUIActivity.class);
					intent2.putExtra("bookid", tempBookid);
					intent2.putExtra("type", type);
					startActivity(intent2);

				}
			});

			return convertView;
		}

	}
	
	public static class ImageLoadingListenerImpl extends SimpleImageLoadingListener {
		public static final List<String> displayedImages = 
		          Collections.synchronizedList(new LinkedList<String>());
		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {
			if (bitmap != null) {
				ImageView imageView = (ImageView) view;
				boolean isFirstDisplay = !displayedImages.contains(imageUri);
				if (isFirstDisplay) {
					// 图片的淡入效果
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
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

			book.setAlreadyBuy(bookDetail.isAlreadyBuy());
			book.setBookId(bookDetail.getEbookId());

			book.setBorrow(bookDetail.isBorrow());
			book.setBorrowEndTime(bookDetail.getBorrowEndTime());
			book.setBorrowStartTime(bookDetail.getBorrowStartTime());
			book.setBuy(bookDetail.isBuy());
			book.setCategoryPath(bookDetail.getCategoryPath());
			book.setFileSize(bookDetail.getFileSize());
			book.setFluentRead(bookDetail.isFluentRead());
			book.setFormat(bookDetail.getFormat());
			book.setFree(bookDetail.isFree());
			book.setGood(bookDetail.getGood());
			book.setJdPrice(bookDetail.getJdPrice());
			book.setLargeImageUrl(bookDetail.getLargeImageUrl());
			// book.setOrderId(Long.parseLong(bookDetail.getOrderId()));
			book.setPrice(bookDetail.getPrice());
			book.setPriceMessage(bookDetail.getPriceMessage());
			book.setPublisher(bookDetail.getPublisher());
			book.setStar(bookDetail.getStar());
			storeBooks.add(book);
		}

		return storeBooks;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_booklist) + showName);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_booklist) + showName);
	}

}
