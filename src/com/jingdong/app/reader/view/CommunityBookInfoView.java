package com.jingdong.app.reader.view;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;

import com.jingdong.app.reader.activity.BookCartActivity;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.activity.BookcaseCloudActivity;
import com.jingdong.app.reader.activity.ChangDuActivity;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager.AddToCartListener;
import com.jingdong.app.reader.client.DownloadStateManager;
import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.client.DownloadTool.DownloadConfirmListener;
import com.jingdong.app.reader.community.CommunityUtil;
import com.jingdong.app.reader.entity.BookInforEDetail;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.MyOnlineBookEntity;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.extra.BuyedEbook;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.entity.extra.JDEBook;
import com.jingdong.app.reader.entity.extra.SimplifiedDetail;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.activity.ReadingCardActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OnlineReadManager;
import com.jingdong.app.reader.timeline.model.core.BookNoteInterface;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ActivityUtils;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.LeadingMarginClickableSpan;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.jingdong.app.reader.R;

public class CommunityBookInfoView extends FrameLayout {

	private TextView bookName;
	private TextView author;
	private TextView publisher;
	private ImageView book_cover;
	private LinearLayout book_info;
	private TextView addToBookShelf;
	private boolean isBuyed=false;//是否已经购买此书
	JDBookInfo bookInfo;

	public CommunityBookInfoView(Context context) {
		this(context, null);
	}

	public CommunityBookInfoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CommunityBookInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.view_community_book_info, this,
				true);
		initViews();
	}

	private void initViews() {
		bookName = (TextView) findViewById(R.id.book_name);
		author = (TextView) findViewById(R.id.author);
		publisher = (TextView) findViewById(R.id.publisher);
		book_cover = (ImageView) findViewById(R.id.book_cover);
		book_info = (LinearLayout) findViewById(R.id.book_info);
		addToBookShelf =  (TextView) findViewById(R.id.addToBookShelf);
	}

	/**
	 * 本方法负责填充图书信息，这个标题来自book，也可能来自books
	 * 
	 * @param dataSource
	 *            数据源
	 * @param textView
	 *            待填充区域
	 */
	public void parseBookInfo(final Context context,Entity dataSource,int flag,boolean jumpToDetail) {
		boolean clickable = true;
		CharSequence text = null;
		String imgurl = null;
		String autString = null,publisherStr = null;
//		bookName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		if (dataSource.getBook() != null
				&& !UiStaticMethod
						.isNullString(dataSource.getBook().getTitle()) && dataSource.getBook().getBookId() > 0) {
			List<Book> books = new LinkedList<Book>();
			books.add(dataSource.getBook());
			text = initBooksClickEvent(books,
					dataSource.getBook().getTitle() + ' ', clickable);
			imgurl = dataSource.getBook().getCover();
			autString = "null".equals(dataSource.getBook().getAuthorName()) ? getResources()
					.getString(R.string.author_unknown) : dataSource.getBook()
					.getAuthorName();
			publisherStr = (dataSource.getBook().getPublisher() == null || "null".equals(dataSource.getBook().getPublisher()) ) ?"":dataSource.getBook().getPublisher()	;
			for (final Book book : books) {
				if (book != null) {
					if(jumpToDetail){
						book_info.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								Intent intent2 = new Intent(getContext(),
										BookInfoNewUIActivity.class);
								intent2.putExtra("bookid", book.getBookId());
								getContext().startActivity(intent2);
							}
						});
					}
					
					//加入书架逻辑处理
					addToBookShelf.setVisibility(View.VISIBLE);
					addToBookShelf.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							CommunityUtil.addToBookcase(book,context);
						}
					});
				}
			
			}
		} else if (dataSource.getRenderBody().getBookList() != null
				&& !dataSource.getRenderBody().getBookList().isEmpty()) {
			text = initBooksClickEvent(
					dataSource.getRenderBody().getBookList(), dataSource
							.getRenderBody().getBookListString(), clickable);
			book_cover.setVisibility(View.GONE);
			author.setVisibility(View.GONE);
			addToBookShelf.setVisibility(View.GONE);
		} else if (dataSource.getRenderBody().getDocument() != null) {
			clickable = false;// 外部导入的书不能点
			Document document = dataSource.getRenderBody().getDocument();
			text = initDocumentClickEvent(document, document.getTitle(),
					clickable);
			if (document.getBook() != null) {
				imgurl = document.getBook().getCover();
				autString = document.getBook().getAuthorName();
				publisherStr = document.getBook().getPublisher();
			} else {
				imgurl = "";
				autString = "";
				publisherStr = "";
			}
			book_cover.setClickable(false);
			book_info.setOnClickListener(null);
			addToBookShelf.setVisibility(View.GONE);
		}else {
			book_info.setOnClickListener(null);
		}
		
		
		
		if (text != null && clickable) {
			text="《"+text+"》";
			bookName.setText(text, BufferType.SPANNABLE);
			bookName.setMovementMethod(LinkTouchMovementMethod.getInstance());
			bookName.setFocusable(false);
			bookName.setFocusableInTouchMode(false);
			bookName.setVisibility(View.VISIBLE);
		}
		UiStaticMethod.setBookInfo(bookName, text, book_cover, author,publisher, imgurl,
				autString,publisherStr, book_info,flag);
	}
	
	/**
	 * 本方法负责填充图书信息，这个标题来自book，也可能来自books
	 * 
	 * @param dataSource
	 *            数据源
	 * @param textView
	 *            待填充区域
	 */
	public void parseBookInfos(final Context context,Entity dataSource) {
		boolean clickable = true;
		CharSequence text = null;
		String imgurl = null;
		String autString = null,publisherStr = null;;
		
		bookName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		if (dataSource.getBook() != null
				&& !UiStaticMethod
						.isNullString(dataSource.getBook().getTitle()) && dataSource.getBook().getBookId() > 0) {
			List<Book> books = new LinkedList<Book>();
			books.add(dataSource.getBook());
			text = initBooksClickEvent(books,
					dataSource.getBook().getTitle() + ' ', clickable);
			imgurl = dataSource.getBook().getCover();
			autString = "null".equals(dataSource.getBook().getAuthorName()) ? getResources()
					.getString(R.string.author_unknown) : dataSource.getBook()
					.getAuthorName();
			publisherStr = (dataSource.getBook().getPublisher() == null|| "null".equals(dataSource.getBook().getPublisher())) ?"":	dataSource.getBook().getPublisher()	;
			for (final Book book : books) {
				if (book != null) {
					
//					book_cover.setOnClickListener(new OnClickListener() {
//
//						@Override
//						public void onClick(View arg0) {
//							Intent intent2 = new Intent(getContext(),
//									BookInfoNewUIActivity.class);
//							intent2.putExtra("bookid", book.getBookId());
//							
//							getContext().startActivity(intent2);
//						}
//					});
					//加入书架逻辑处理
					addToBookShelf.setVisibility(View.VISIBLE);
					addToBookShelf.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							CommunityUtil.addToBookcase(book,context);
						}
					});
				}
			}
		} else if (dataSource.getRenderBody().getBookList() != null
				&& !dataSource.getRenderBody().getBookList().isEmpty()) {
			text = initBooksClickEvent(
					dataSource.getRenderBody().getBookList(), dataSource
							.getRenderBody().getBookListString(), clickable);
			addToBookShelf.setVisibility(View.GONE);
			book_cover.setVisibility(View.GONE);
			author.setVisibility(View.GONE);
			publisher.setVisibility(View.GONE);
		} else if (dataSource.getRenderBody().getDocument() != null) {
			clickable = false;// 外部导入的书不能点
			Document document = dataSource.getRenderBody().getDocument();
			text = initDocumentClickEvent(document, document.getTitle(),
					clickable);
			if (document.getBook() != null) {
				imgurl = document.getBook().getCover();
				autString = document.getBook().getAuthorName();
				publisherStr = document.getBook().getPublisher();
			} else {
				imgurl = "";
				autString = "";
				publisherStr = "";
			}
			addToBookShelf.setVisibility(View.GONE);//外部导入的书不能加入书架
			book_cover.setClickable(false);
			book_info.setOnClickListener(null);
		}
		if (text != null && clickable) {
			text="《"+text+"》";
			bookName.setText(text, BufferType.SPANNABLE);
			bookName.setMovementMethod(LinkTouchMovementMethod.getInstance());
			bookName.setFocusable(false);
			bookName.setFocusableInTouchMode(false);
			bookName.setVisibility(View.VISIBLE);
		}
		UiStaticMethod.setBookInfos(bookName, text, book_cover, author,publisher, imgurl,
				autString,publisherStr, book_info);
	}

	
	
	/**
	 * 设置一个部分可点击的文字，并为文字不同部分添加不同的事件。
	 * 
	 * @param bookList
	 *            图书列表，图书列表中的每一项都是待设置文字中的一部分。
	 * @param src
	 *            待设置文字
	 * @param clickable
	 *            可点击否
	 * @return 一个部分可点击文字，此文字已经和事件绑定
	 */
	private SpannableStringBuilder initBooksClickEvent(List<Book> bookList,
			String src, boolean clickable) {
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(
				src);
		boolean firstline = true;
		int start = 0, end = 0;
		for (Book book : bookList) {
			if (book != null) {
				start = end;
				end += book.getTitle().toString().length();
				spannableStringBuilder.setSpan(new LeadingMarginClickableSpan(
						getContext(), firstline, book, start, end, clickable),
						start, end++,
						SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
				firstline = false;
			}
		}
		return spannableStringBuilder;
	}

	/**
	 * 设置一个部分可点击的文字，并为文字不同部分添加不同的事件。
	 * 
	 * @param document
	 *            外部导入图书。
	 * @param src
	 *            待设置文字
	 * @param clickable
	 *            可点击否
	 * @return 一个部分可点击文字，此文字已经和事件绑定
	 */
	private SpannableStringBuilder initDocumentClickEvent(Document document,
			String src, boolean clickable) {
		if (UiStaticMethod.isNullString(src)) {
			return null;
		}
		Book book = new Book();
		book.setTitle(src);
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(
				book.getTitle());
		boolean firstline = true;
		int start = 0, end = 0;
		start = end;
		end += book.getTitle().toString().length();
		spannableStringBuilder.setSpan(new LeadingMarginClickableSpan(
				getContext(), firstline, book, start, end, clickable), start,
				end++, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
		firstline = false;
		return spannableStringBuilder;
	}
}
