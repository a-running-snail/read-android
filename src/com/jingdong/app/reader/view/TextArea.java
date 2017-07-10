package com.jingdong.app.reader.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
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
import com.jingdong.app.reader.community.PreviewActivity;
import com.jingdong.app.reader.timeline.model.core.BookNoteInterface;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.timeline.model.core.Entity.RenderType;
import com.jingdong.app.reader.util.LeadingMarginClickableSpan;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.jingdong.app.reader.R;

public class TextArea extends FrameLayout {

	private RatingBar rating;
	private TextView title;
	private TextView main;
	private TextView quotation;
	private boolean truncation;
	private int titleMaxLines;
	private int contentMaxLines;
	private LinearLayout ratingLinearLayout;
	private CommunityBookInfoView bookInfoViewForBookComment;
	private CommunityBookInfoView bookInfoViewForNote;
	private LinearLayout noteLinearLayout;
	private LinearLayout bookForNoteLinearLayout;
	private TextView bookComment;
	private String headerText="";
	private Context context;
	private LinearLayout imageLayout;
	private ImageView imageView1,imageView2,imageView3;

	public TextArea(Context context) {
		this(context, null);
	}

	public TextArea(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TextArea(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context=context;
		LayoutInflater.from(context).inflate(R.layout.view_text_area, this,
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


	/**
	 * 
	 * @param dataSource
	 * @param clickable
	 */
	public void parseEntitys(Entity dataSource, boolean clickable) {
		if (dataSource.getRenderBody().isDeleted()) {
			showDeleteText();
		} else {
			double zero = 0;
			UiStaticMethod.setRatingBar(rating,ratingLinearLayout,dataSource.getRenderBody().getRating());
			initHeaderText(dataSource);
			//是否显示书评模块中的图书信息view
			if (!Double.isNaN(zero = dataSource.getRenderBody().getRating()) && zero != 0) {
				setBookCommentVisible(true);
				bookInfoViewForBookComment.parseBookInfos(context,dataSource);
				String text="";
				boolean show = !UiStaticMethod.isNullString(dataSource.getRenderBody().getContent());
				if (show) {
					text = headerText+dataSource.getRenderBody().getContent();
				} else {
					text="";
				}
				UiStaticMethod.setTextString(bookComment, text);
			} else{
				setBookCommentVisible(false);
				switch (dataSource.getRenderType()) {
				case UserTweet:
					bookForNoteLinearLayout.setVisibility(View.GONE);
					break;
				default:
					bookForNoteLinearLayout.setVisibility(View.VISIBLE);
					bookInfoViewForNote.parseBookInfos(context,dataSource);
					break;
				}
				parseBookNotes(dataSource.getRenderBody(), clickable);
			}
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
		if (dataSource.getRenderBody().isDeleted()) {
			showDeleteText();
		} else {
			double zero = 0;
			UiStaticMethod.setRatingBar(rating,ratingLinearLayout,dataSource.getRenderBody().getRating());
			//区分信息类别，初始化类别文字
			initHeaderText(dataSource);
			//是否显示书评模块中的图书信息view
			if (!Double.isNaN(zero = dataSource.getRenderBody().getRating()) && zero != 0) {
				setBookCommentVisible(true);
				boolean jumpToDetail=false;
				bookInfoViewForBookComment.parseBookInfo(context,dataSource,flag,jumpToDetail);
				String text="";
				boolean show = !UiStaticMethod.isNullString(dataSource.getRenderBody().getContent());
				if (show) {
					text = headerText+dataSource.getRenderBody().getContent();
				} else {
					text="";
				}
				UiStaticMethod.setTextString(bookComment, text);
			} else{
				setBookCommentVisible(false);
				switch (dataSource.getRenderType()) {
				case UserTweet://动态无书籍信息
					bookForNoteLinearLayout.setVisibility(View.GONE);
					break;
				default:
					bookForNoteLinearLayout.setVisibility(View.VISIBLE);
					boolean jumpToDetail=false;
					bookInfoViewForNote.parseBookInfo(context,dataSource,flag,jumpToDetail);
					break;
				}
				parseBookNote(dataSource.getRenderBody(), clickable);
			}
			//设置随便说说图片
			initImageViews(dataSource);
		}
	}
	
	/**
	 * 设置随便说说的图片
	 */
	private void initImageViews(final Entity entity){
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
	
	/**
	 * 设置书评，true为书评，false为动态，笔记，划线，以及转发评论等
	 * @param visible
	 */
	private void setBookCommentVisible(boolean visible){
		if(visible){
			bookInfoViewForBookComment.setVisibility(View.VISIBLE);
			bookInfoViewForNote.setVisibility(View.GONE);
			noteLinearLayout.setVisibility(View.GONE);
			bookForNoteLinearLayout.setVisibility(View.GONE);
		}else{
			bookInfoViewForNote.setVisibility(View.VISIBLE);
			noteLinearLayout.setVisibility(View.VISIBLE);
			bookForNoteLinearLayout.setVisibility(View.VISIBLE);
			bookInfoViewForBookComment.setVisibility(View.GONE);
		}
			
	}

	public void parseBookNotes(BookNoteInterface bookNote, boolean clickable) {
//		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//		main.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//		quotation.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
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

	/**
	 * 显示内容以及引用文本
	 * @param bookNote
	 * @param clickable
	 */
	public void parseBookNote(BookNoteInterface bookNote, boolean clickable) {
//		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//		main.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//		quotation.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		UiStaticMethod.setText(title, bookNote.getTitle());
		
		String content=bookNote.getContent();
		String quote=bookNote.getQuote();
		if(content!=null && !content.equals("")){
			content = headerText+content;
		}else{
			quote = headerText + quote;
		}
		if (clickable) {
			
			UiStaticMethod.setAtUrlClickable(getContext(), main,
					content);
			UiStaticMethod.setAtUrlClickable(getContext(), quotation,
					quote);
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
			title.setEllipsize(TruncateAt.END);
			main.setEllipsize(TruncateAt.END);
			quotation.setEllipsize(TruncateAt.END);
		} else {
			title.setEllipsize(null);
			main.setEllipsize(null);
			quotation.setEllipsize(null);
		}
	}

	private void initViews() {
		rating = (RatingBar) findViewById(R.id.rating);
		title = (TextView) findViewById(R.id.title);
		main = (TextView) findViewById(R.id.main);
		quotation = (TextView) findViewById(R.id.quotation);
		ratingLinearLayout = (LinearLayout) findViewById(R.id.ratingLinearLayout);
		bookInfoViewForBookComment = (CommunityBookInfoView) findViewById(R.id.bookForBookComment);
		bookInfoViewForNote = (CommunityBookInfoView) findViewById(R.id.bookForNote);
		noteLinearLayout =(LinearLayout) findViewById(R.id.noteLinearLayout);
		bookForNoteLinearLayout =(LinearLayout) findViewById(R.id.bookForNoteLinearLayout);
		bookComment=  (TextView) findViewById(R.id.bookComment);
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
		main.setVisibility(View.VISIBLE);
		main.setText(R.string.delete_timeline);
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
