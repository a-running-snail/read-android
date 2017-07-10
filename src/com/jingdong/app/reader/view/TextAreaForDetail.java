package com.jingdong.app.reader.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.community.CommunityUtil;
import com.jingdong.app.reader.community.PreviewActivity;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.BookNoteInterface;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.LeadingMarginClickableSpan;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.TextArea.imageClick;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.jingdong.app.reader.R;

public class TextAreaForDetail extends FrameLayout {

	private RatingBar rating;
	private TextView bookName;
	private TextView title;
	private TextView main;
	private TextView quotation;
	private boolean truncation;
	private int titleMaxLines;
	private int contentMaxLines;
	private TextView author;
	private TextView publisher;
	private ImageView book_cover;
	private LinearLayout book_info;
	private LinearLayout ratingLinearLayout;
	private TextView addToBookShelf;
	private LinearLayout likeLinearLayout;
	private ImageView likeImage;
	private TextView likeTextView;
	private String headerText;
	private CommunityBookInfoView bookInfoView;
	private Context context;
	private JDBookInfo bookInfo;
	Boolean isWish=false;
	private LinearLayout imageLayout;
	private ImageView imageView1,imageView2,imageView3;

	public TextAreaForDetail(Context context) {
		this(context, null);
	}

	public TextAreaForDetail(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TextAreaForDetail(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context=context;
		LayoutInflater.from(context).inflate(R.layout.view_text_area_for_detail, this,
				true);
		if (!isInEditMode()) {
			initViews();
			TypedArray typedArray = context.getTheme().obtainStyledAttributes(
					attrs, R.styleable.TextArea, 0, 0);
			try {
				initAttribute(typedArray);
			} finally {
				typedArray.recycle();
			}
		}
	}
	
	private void initHeaderText(Entity dataSource){
		switch (dataSource.getRenderType()) {
		case UserTweet:
			headerText = "发表了说说 | ";
			break;
		case BookComment:
			if (!Double.isNaN(dataSource.getRenderBody().getRating()) && dataSource.getRenderBody().getRating() != 0) {
				headerText="写了书评 | ";
			}else
				headerText="分享了书籍 | ";
			break;
		case Note:
			if(dataSource.getRenderBody().getContent()!=null && !dataSource.getRenderBody().getContent().equals("")){
				headerText = "写了笔记 | ";
			}else
				headerText = "做了划线 | ";
			break;
		case EntityComment:
			headerText = "回复 | ";
			break;
		default:
			headerText="";
			break;
		}
	}

	public void parseEntitys(Entity dataSource, boolean clickable) {
		bookName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		if (dataSource.getRenderBody().isDeleted()) {
			showDeleteText();
		} else {
			initHeaderText(dataSource);
			parseBookNames(dataSource, bookName, book_cover, author,publisher,addToBookShelf, book_info);
			parseBookNotes(dataSource.getRenderBody(), clickable);
			UiStaticMethod.setRatingBar(rating,ratingLinearLayout, dataSource.getRenderBody()
					.getRating());
			//设置随便说说图片
			initImageViews(dataSource);
		}
	}

	/**
	 * 显示引用、笔记、还有图书信息区域
	 * @param dataSource
	 * @param clickable
	 */
	public void parseEntity(Entity dataSource, boolean clickable,int flag) {
		bookName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		if (dataSource.getRenderBody().isDeleted()) {
			showDeleteText();
		} else {
			initHeaderText(dataSource);
			parseBookName(dataSource, bookName, book_cover, author,publisher,addToBookShelf, book_info,flag);
			parseBookNote(dataSource.getRenderBody(), clickable);
			UiStaticMethod.setRatingBar(rating,ratingLinearLayout,dataSource.getRenderBody()
					.getRating());
			
			//设置随便说说图片
			initImageViews(dataSource);
		}
		
		
	}
	
	
	/**
	 * 设置随便说说的图片
	 */
	private void initImageViews(Entity entity){
		if(entity.getImages()!=null && entity.getImages().size()>0){
			imageLayout.setVisibility(View.VISIBLE);
			String url1 = entity.getImages().get(0) + "!q10.jpg";
			imageView1.setOnClickListener(new imageClick(0, entity.getImages()));
			ImageLoader.getInstance().displayImage(url1, imageView1,GlobalVarable.getDefaultCommunityDisplayOptions(false));
			if(entity.getImages().size()>1) {
				String url2 = entity.getImages().get(1) + "!q10.jpg";
				imageView2.setOnClickListener(new imageClick(1, entity.getImages()));
				ImageLoader.getInstance().displayImage(url2, imageView2,GlobalVarable.getDefaultCommunityDisplayOptions(false));
			}
			if(entity.getImages().size()>2) {
				String url3 = entity.getImages().get(2) + "!q10.jpg";
				imageView3.setOnClickListener(new imageClick(2, entity.getImages()));
				ImageLoader.getInstance().displayImage(url3, imageView3,GlobalVarable.getDefaultCommunityDisplayOptions(false));
			}
		}
		else{
			imageLayout.setVisibility(View.GONE);
		}
	}
	
	class imageClick implements OnClickListener {
		private int index;
		private ArrayList<String> urls;
		
		public imageClick(int index, ArrayList<String> urls){
			this.index = index;
			this.urls = urls;
		}

		@Override
		public void onClick(View arg0) {
			Intent preview = new Intent(context, PreviewActivity.class);
			preview.putExtra("index", index);
			preview.putExtra("urls", urls);
			context.startActivity(preview);
		}
		
	}

	public void parseBookNotes(BookNoteInterface bookNote, boolean clickable) {
		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		main.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		quotation.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		UiStaticMethod.setText(title, bookNote.getTitle());
		
		String content=bookNote.getContent();
		String quote=bookNote.getQuote();
		if(content!=null && !content.equals("")){
			content = headerText+content;
		}else{
			quote = headerText + quote;
		}
		if (clickable) {
			UiStaticMethod.setAtUrlClickable(getContext(), main,content);
			UiStaticMethod.setAtUrlClickable(getContext(), quotation,quote);
		} else {
			UiStaticMethod.setTextString(main, content);
			UiStaticMethod.setTextString(quotation, quote);
		}
	}

	public void parseBookNote(BookNoteInterface bookNote, boolean clickable) {
		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		main.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		quotation.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		UiStaticMethod.setText(title, bookNote.getTitle());
		
		String content=bookNote.getContent();
		String quote=bookNote.getQuote();
		if(content!=null && !content.equals("")){
			content = headerText+content;
		}else{
			quote = headerText + quote;
		}
		if (clickable) {
			UiStaticMethod.setAtUrlClickable(getContext(), main,content);
			UiStaticMethod.setAtUrlClickable(getContext(), quotation,quote);
		} else {
			UiStaticMethod.setTextString(main, content);
			UiStaticMethod.setTextString(quotation, quote);
		}
	}

	public boolean isTruncation() {
		return truncation;
	}

	public int getTitleMaxLines() {
		return titleMaxLines;
	}

	public int getContentMaxLines() {
		return contentMaxLines;
	}

	public final void setTitleMaxLines(int titleMaxLines) {
		this.titleMaxLines = titleMaxLines;
		title.setMaxLines(titleMaxLines);
	}

	public final void setContentMaxLines(int contentMaxLines) {
		this.contentMaxLines = contentMaxLines;
		main.setMaxLines(contentMaxLines);
		quotation.setMaxLines(contentMaxLines);
	}

	public final void setTruncation(boolean truncation) {
		this.truncation = truncation;
		if (truncation) {
			bookName.setEllipsize(TruncateAt.END);
			title.setEllipsize(TruncateAt.END);
			main.setEllipsize(TruncateAt.END);
			quotation.setEllipsize(TruncateAt.END);
			author.setEllipsize(TruncateAt.END);
			publisher.setEllipsize(TruncateAt.END);
		} else {
			bookName.setEllipsize(null);
			title.setEllipsize(null);
			main.setEllipsize(null);
			quotation.setEllipsize(null);
			author.setEllipsize(null);
			publisher.setEllipsize(null);
		}
	}

	private void initViews() {
		rating = (RatingBar) findViewById(R.id.rating);
		bookName = (TextView) findViewById(R.id.book_name);
		title = (TextView) findViewById(R.id.title);
		main = (TextView) findViewById(R.id.main);
		quotation = (TextView) findViewById(R.id.quotation);
		author = (TextView) findViewById(R.id.author);
		publisher = (TextView) findViewById(R.id.publisher);
		book_cover = (ImageView) findViewById(R.id.book_cover);
		book_info = (LinearLayout) findViewById(R.id.book_info);
		ratingLinearLayout = (LinearLayout) findViewById(R.id.ratingLinearLayout);
		addToBookShelf =  (TextView) findViewById(R.id.addToBookShelf);
		likeLinearLayout = (LinearLayout) findViewById(R.id.likeLinearLayout);
		likeImage =  (ImageView) findViewById(R.id.like_image);
		likeTextView = (TextView) findViewById(R.id.like_text);
		
		imageLayout = (LinearLayout) findViewById(R.id.image_layout);
		imageView1 = (ImageView) findViewById(R.id.image1);
		imageView2 = (ImageView) findViewById(R.id.image2);
		imageView3 = (ImageView) findViewById(R.id.image3);
		
		dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		int imageWidth = (int)((dm.widthPixels - dipToPx(100))/3);
//		int imageHeight = (int)(imageWidth*1.33f);
		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(imageWidth, imageWidth);
		imageParams.setMargins(0, 0, dipToPx(6), 0);
		imageView1.setLayoutParams(imageParams);
		
		LinearLayout.LayoutParams imageParams2 = new LinearLayout.LayoutParams(imageWidth, imageWidth);
		imageParams2.setMargins(0, 0, dipToPx(6), 0);
		imageView2.setLayoutParams(imageParams2);
		
		LinearLayout.LayoutParams imageParams3 = new LinearLayout.LayoutParams(imageWidth, imageWidth);
		imageView3.setLayoutParams(imageParams3);
	}
	
	DisplayMetrics dm;
	public int dipToPx(int dip) {
		return (int) (dip * dm.density + 0.5f);
	}

	private void initAttribute(TypedArray typedArray) {
		boolean truncation = typedArray.getBoolean(
				R.styleable.TextArea_truncation, false);
		setTruncation(truncation);
		int titleMaxLines = typedArray.getInt(
				R.styleable.TextArea_titleMaxLines, -1);
		if (titleMaxLines > 0)
			setTitleMaxLines(titleMaxLines);
		int contentMaxLines = typedArray.getInt(
				R.styleable.TextArea_contentMaxLines, -1);
		if (contentMaxLines > 0)
			setContentMaxLines(contentMaxLines);
	}

	private void showDeleteText() {
		title.setVisibility(View.GONE);
		quotation.setVisibility(View.GONE);
		rating.setVisibility(View.GONE);
		ratingLinearLayout.setVisibility(View.GONE);
		bookName.setVisibility(View.GONE);
		author.setVisibility(View.GONE);
		publisher.setVisibility(View.GONE);
		book_cover.setVisibility(View.GONE);
		book_info.setVisibility(View.GONE);
		main.setVisibility(View.VISIBLE);
		main.setText(R.string.delete_timeline);
	}

	/**
	 * 本方法负责填充图书信息，这个标题来自book，也可能来自books
	 * 
	 * @param dataSource
	 *            数据源
	 * @param textView
	 *            待填充区域
	 */
	private void parseBookName(Entity dataSource, TextView textView,
			ImageView book_cover, TextView author,TextView publisher,TextView addToBookShelf, LinearLayout book_info,int flag) {
		boolean clickable = true;
		CharSequence text = null;
		String imgurl = null;
		String autString = null,publisherStr = null;
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
					//初始化喜欢状态
					isWishBook(context,book.getBookId());
					//点击改变喜欢状态
					likeLinearLayout.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							isWish=!isWish;
							updateWishButton(context, isWish);
							updateServerWishStatus(context,isWish,book.getBookId());
						}
					});
					
					book_info.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent2 = new Intent(getContext(),
									BookInfoNewUIActivity.class);
							intent2.putExtra("bookid", book.getBookId());
							getContext().startActivity(intent2);
						}
					});
					//加入书架逻辑处理
					addToBookShelf.setVisibility(View.VISIBLE);
					addToBookShelf.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							CommunityUtil.addToBookcase(book, context);
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
			publisher.setVisibility(View.GONE);
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
			textView.setText(text, BufferType.SPANNABLE);
			textView.setMovementMethod(LinkTouchMovementMethod.getInstance());
			textView.setFocusable(false);
			textView.setFocusableInTouchMode(false);
			textView.setVisibility(View.VISIBLE);
		}
		UiStaticMethod.setBookInfo(textView, text, book_cover, author,publisher, imgurl,
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
	private void parseBookNames(Entity dataSource, TextView textView,
			ImageView book_cover, TextView author, TextView publisher,TextView addToBookShelf,LinearLayout book_info) {
		boolean clickable = true;
		CharSequence text = null;
		String imgurl = null;
		String autString = null,publisherStr = null;;
		
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
			publisherStr = dataSource.getBook().getPublisher() == null ?"":	dataSource.getBook().getPublisher()	;
			//收藏动态
//			final TweetModel entity = (TweetModel) dataSource;
//			if (entity.isFavourite()) {
//				likeImage.setImageResource(R.drawable.btn_toolbar_fav);
//			} else {
//				likeImage.setImageResource(R.drawable.btn_toolbar_unfav);
//			}
//			likeLinearLayout.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View arg0) {
//					new CommunityUtil().clickFavourite(getContext(), entity, likeImage);
//				}
//			});
			
			for (final Book book : books) {
				if (book != null) {
					//初始化喜欢状态
					isWishBook(context,book.getBookId());
					//点击改变喜欢状态
					likeLinearLayout.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							isWish=!isWish;
							updateWishButton(context, isWish);
							updateServerWishStatus(context,isWish,book.getBookId());
						}
					});
					
					book_cover.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent2 = new Intent(getContext(),
									BookInfoNewUIActivity.class);
							intent2.putExtra("bookid", book.getBookId());
							
							getContext().startActivity(intent2);
						}
					});
					//加入书架逻辑处理
					addToBookShelf.setVisibility(View.VISIBLE);
					addToBookShelf.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							CommunityUtil.addToBookcase(book, context);
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
			publisher.setVisibility(View.GONE);
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
		}
		if (text != null && clickable) {
			text="《"+text+"》";
			textView.setText(text, BufferType.SPANNABLE);
			textView.setMovementMethod(LinkTouchMovementMethod.getInstance());
			textView.setFocusable(false);
			textView.setFocusableInTouchMode(false);
			textView.setVisibility(View.VISIBLE);
		}
		UiStaticMethod.setBookInfos(textView, text, book_cover, author,publisher, imgurl,
				autString,publisherStr, book_info);
	}
	private void updateServerWishStatus(Context context,boolean iswish,long bookid){
		if (iswish) {
			CommunityUtil.wishBook(context, bookid);
		}else
			CommunityUtil.unWishBook(context, bookid);
	}
	
	/**
	 * 获取喜欢该书的状态
	 * @param context
	 * @param bookid
	 */
	public void isWishBook(final Context context,long bookid) {

		if (!LoginUser.isLogin())
			return;

		WebRequestHelper.post(URLText.wishStatus, RequestParamsPool.getWishBookParams(bookid), true,
				new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						try {
							JSONObject object = new JSONObject(new String(responseBody));
							boolean iswished = object.optBoolean("wish_status");
							isWish=iswished;
							if (iswished) {
								updateWishButton(context,iswished);
							} else {
								updateWishButton(context,iswished);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});
	}
	/**
	 * 更新喜欢状态
	 * @param context
	 * @param iswished
	 */
	public void updateWishButton(Context context,boolean iswished) {
		if (likeImage != null && likeTextView != null) {
			if (iswished) {
				likeImage.setImageResource(R.drawable.community_like_book_icon);
				likeTextView.setTextColor(context.getResources().getColor(R.color.red_main));
			} else {
				likeImage.setImageResource(R.drawable.community_unlike_book_icon);
				likeTextView.setTextColor(context.getResources().getColor(R.color.text_sub));
			}
		}
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
