package com.jingdong.app.reader.epub.paging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookPageImageEnlargeActivity;
import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.epub.paging.Border.BorderStyle;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.notes.NotesModel;
import com.jingdong.app.reader.preloader.CutBitmapDisplayer;
import com.jingdong.app.reader.reading.BookBackCoverView;
import com.jingdong.app.reader.reading.BookMark;
import com.jingdong.app.reader.reading.CoverPage;
import com.jingdong.app.reader.reading.FinishPage;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.reading.ReadSearchData;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.DateUtil;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.StringUtil;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path.Direction;
import android.graphics.Region.Op;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

public class Kit42View extends RelativeLayout implements IPopEventHandler{

	private String tag = "kit42View";
    public static final int PAGE_ANIMATION_DURATION = 400;//毫秒级
    private static final int NOTE_ACTION_BAR_MIN_HEIGHT = 130;
    private PageContext pageContext;
    private float density;
    private int screenWidth;
    private int screenHeight;
    private Paint footHeadPaint;

    private Page page;
    private Context ctx;

    private ImageView beginIndicator;
    private ImageView endIndicator;
    private View arrowLayout;
    private View bookMarkLayout;
    private View bookMarkBgLayout;
    private View fontDownloadLayout;
    private View searchBarLayout;
    private View pageShadowLayout;
    private ImageView arrowImage;
    private TextView arrowText;
    private BookBackCoverView bookFinishView;

    private IReadFunction readFunction;
    private GestureDetectorCompat detector;
    private HashMap<ImageView, String> playControlMap = new HashMap<ImageView, String>();
    private List<ReadNote> peopleNotes = new ArrayList<ReadNote>();
    private List<ImageView> noteImageView;
    private List<View> imageViewList = new ArrayList<View>();
    private ReadPopWindow popupWindow;
    
    /**分享笔记专用*/
    private Paint sharePaint;
    private Bitmap shareCoverBitmap;
    private Bitmap shareBottomBitmap;
    private Bitmap shareTopNoteBitmap;
    private Bitmap shareTopSummaryBitmap;
    private List<String> shareNoteContentList;
    private int shareReadNoteHeight = 0;
    private int shareContentHeight = 0;
    private int shareBottomHeight = 0;
    private int shareTopHeight = 0;
    private int shareCoverHeight = 0;
    private String shareBookName = null;
    private String shareAuthor = null;
    /*分享笔记结束*/

    enum NoteState {
        MOVE_BEGIN, MOVE_END, END_NOTE
    }

    private ReadNote modifyReadNote;
    private NoteState noteState = NoteState.END_NOTE;
    private Element noteBegin = null;
    private Element noteEnd = null;
    private int noteBeginLineIndex = -1;
    private int noteEndLineIndex = -1;
    private int bookMarkMaxDistanceY = 0;
    private int bookMarkActionPointY = 0;
    private int scrollDeltaY = 0;
    private int footAscent;
    private int footDescent;
    private int searchPrevChapter = 0;
    private int searchPrevBlock = 0;
    private int searchPrevOffset = 0;
    private int searchNextChapter = 0;
    private int searchNextBlock = 0;
    private int searchNextOffset = 0;
    private float popArrowX;
    private float longPressX;
    private float longPressY;
    private float beginIndicatorX;
    private float beginIndicatorY;
    private float endIndicatorX;
    private float endIndicatorY;
    private boolean isReleased = false;
    private boolean isBookMarkScroll = false;
    private boolean isAddBookMark = false;
    private boolean isArrowUp = false;
    private boolean isCancelNote = false;
    private boolean isTopArrowShow = false;
    private boolean isBottomArrowShow = false;
    private boolean isNoteContentShowing = false;
    private boolean isReadNoteShareFlow = false;//分享笔记流程
    private boolean isLongPressSelectState = false;
    private Animation arrowRotateUp;
    private Animation arrowRotateDown;
    private RelativeLayout.LayoutParams arrowlp;
    private RelativeLayout.LayoutParams bookmarklp;
    private RelativeLayout.LayoutParams bookmarkBglp;
    private Bitmap brownPaperBg;//牛皮纸背景图
    private Bitmap texturebmp;
    private int preSelectedTextureIndex = -1;
    
    private Scroller mScroller;
    
    private int lastTouchX;
    boolean mIsFlatTurning = false; //是否平翻（非从上下角折翻）
    private GradientDrawable shadowDrawableRL;
    private GradientDrawable shadowDrawableLR;
    private long actionDownTime = 0;
    
    public Kit42View(Context context, IReadFunction readFunc) {
        this(context, readFunc, false);
    }

    public Kit42View(Context context, IReadFunction readFunc, boolean isReadNoteShareFlow) {
        super(context);
        this.ctx=context;
        this.readFunction = readFunc;
        setFocusable(true);
        DisplayMetrics display = getResources().getDisplayMetrics();
        density = display.density;
        screenWidth = display.widthPixels;
        screenHeight = display.heightPixels;
        footHeadPaint = new Paint();
        // footHeadPaint.setColor(Color.rgb(145, 125, 105));
        footHeadPaint.setColor(0xFFB8B8B8);
        footHeadPaint.setTextAlign(Align.RIGHT);
        footHeadPaint.setTextSize(14 * density);
        footHeadPaint.setAntiAlias(true);
        footHeadPaint.setSubpixelText(true);
        // footHeadPaint.setTextSkewX(-0.25f);
        FontMetrics metrics = footHeadPaint.getFontMetrics();
        footAscent = Math.abs((int) metrics.ascent);
        footDescent = Math.abs((int) metrics.descent);
        this.setWillNotDraw(false);

        this.isReadNoteShareFlow = isReadNoteShareFlow;
        if (isReadNoteShareFlow) {
            return;
        }
        detector = new GestureDetectorCompat(context, new GestureListener());

        beginIndicator = new ImageView(context);
        beginIndicator.setImageResource(R.drawable.icon_left_handle);
        endIndicator = new ImageView(context);
        endIndicator.setImageResource(R.drawable.icon_right_handle);
        beginIndicator.setVisibility(View.INVISIBLE);
        endIndicator.setVisibility(View.INVISIBLE);
        
        bookMarkMaxDistanceY = (int)(41 * density);
        bookMarkActionPointY = (int)(36 * density);
        
        View layout = View.inflate(ctx, R.layout.read_bookmark_arrow_layout, null);
        arrowLayout = layout.findViewById(R.id.arrowLayout);
        bookMarkLayout = layout.findViewById(R.id.bookmarkLayout);
        bookMarkBgLayout = layout.findViewById(R.id.bookmarkBgLayout);
        ((ViewGroup)layout).removeAllViews();
        
        popupWindow = createPopupWindow();
		arrowImage = (ImageView) arrowLayout.findViewById(R.id.arrow);
		arrowText = (TextView) arrowLayout.findViewById(R.id.arrowText);
		arrowText.setText(isAddBookMark ? R.string.read_remove_bookmark_1
				: R.string.read_add_bookmark_1);
		arrowRotateUp = AnimationUtils.loadAnimation(ctx, R.anim.read_bookmark_rotate_to_up);
		arrowRotateUp.setDuration(200);
		arrowRotateUp.setFillAfter(true);
		
		arrowRotateDown = AnimationUtils.loadAnimation(ctx, R.anim.read_bookmark_rotate_to_down);
		arrowRotateDown.setDuration(200);
		arrowRotateDown.setFillAfter(true);
		
		fontDownloadLayout = View.inflate(ctx, R.layout.read_font_download_bar, null);
		fontDownloadLayout.setVisibility(View.GONE);
		fontDownloadLayout.findViewById(R.id.font_download_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				readFunction.startDownloadFont();
			}
		});
		fontDownloadLayout.findViewById(R.id.font_download_close_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				readFunction.hideDownloadFontBar();
			}
		});
		
		searchBarLayout = View.inflate(ctx, R.layout.read_search_bar, null);
		searchBarLayout.setVisibility(View.GONE);
		searchBarLayout.findViewById(R.id.searchButton).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				readFunction.openSearch();
				readFunction.hideSearchBar();
			}
		});
		searchBarLayout.findViewById(R.id.searchLastButton).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (searchPrevChapter >= 0 && searchPrevBlock >= 0
						&& searchPrevOffset >= 0) {
					readFunction.gotoSearchPage(
							searchPrevChapter,
							searchPrevBlock,
							searchPrevOffset);
				}
			}
		});
		searchBarLayout.findViewById(R.id.searchNextButton).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (searchNextChapter >= 0 && searchNextBlock >= 0
						&& searchNextOffset >= 0) {
					readFunction.gotoSearchPage(
							searchNextChapter,
							searchNextBlock,
							searchNextOffset);
				}
			}
		});
		
		pageShadowLayout = View.inflate(ctx, R.layout.read_page_slide_shadow, null);
		pageShadowLayout.setVisibility(View.GONE);
		
		mScroller = new Scroller(ctx);
	}
	
	private void setSearchPrevNextButtonStatus() {
		if (searchPrevChapter >= 0 && searchPrevBlock >= 0
				&& searchPrevOffset >= 0) {
			ImageView lastBtn = (ImageView) searchBarLayout
					.findViewById(R.id.searchLastIcon);
			lastBtn.setImageResource(R.drawable.reader_btn_search_lastone);
		} else {
			ImageView lastBtn = (ImageView) searchBarLayout
					.findViewById(R.id.searchLastIcon);
			lastBtn.setImageResource(R.drawable.reader_btn_search_lastone_disable);
		}
		if (searchNextChapter >= 0 && searchNextBlock >= 0
				&& searchNextOffset >= 0) {
			ImageView nextBtn = (ImageView) searchBarLayout
					.findViewById(R.id.searchNextIcon);
			nextBtn.setImageResource(R.drawable.reader_btn_search_nextone);
		} else {
			ImageView nextBtn = (ImageView) searchBarLayout
					.findViewById(R.id.searchNextIcon);
			nextBtn.setImageResource(R.drawable.reader_btn_search_nextone_disable);
		}
	}
	
	private void preparePrevSearchPage() {
		searchPrevChapter = -1;
		searchPrevBlock = -1;
		searchPrevOffset = -1;
		if (page == null || page.getChapter() == null) {
			return;
		}
		ReadSearchData data = page.getFirstSearchData();
		if (data != null && data.getParaIndex() == page.startParaIndex) {
			if (data.getStartOffsetInPara() < page.startOffset) {
				searchPrevChapter = data.getChapterIndex();
				searchPrevBlock = data.getParaIndex();
				searchPrevOffset = data.getStartOffsetInPara();
				return;
			}
		}
		ReadSearchData prev = page.getChapter().findPrevSearchResult(data);
		if (prev == null) {
			Chapter chapter = page.getChapter();
			while (true) {
				chapter = readFunction.getPrevChapter(chapter);
				if (chapter != null) {
					prev = chapter.getLastSearchResult();
					if (prev != null) {
						break;
					}
				} else {
					break;
				}
			}
		}
		if (prev != null) {
			searchPrevChapter = prev.getChapterIndex();
			searchPrevBlock = prev.getParaIndex();
			searchPrevOffset = prev.getEndOffsetInPara() - 1;
		}
	}
	
	private void prepareNextSearchPage() {
		searchNextChapter = -1;
		searchNextBlock = -1;
		searchNextOffset = -1;
		if (page == null || page.getChapter() == null) {
			return;
		}
		Page nextPage = page.getChapter().getNextPage(page);
		ReadSearchData data = page.getLastSearchData();
		if (nextPage != null && data != null && data.getParaIndex() == nextPage.startParaIndex) {
			if (data.getStartOffsetInPara() < nextPage.startOffset
					&& data.getEndOffsetInPara() - 1 >= nextPage.startOffset) {
				searchNextChapter = data.getChapterIndex();
				searchNextBlock = data.getParaIndex();
				searchNextOffset = data.getEndOffsetInPara() - 1;
				return;
			}
		}
		ReadSearchData next = page.getChapter().findNextSearchResult(data);
		if (next == null) {
			Chapter chapter = page.getChapter();
			while (true) {
				chapter = readFunction.getNextChapter(chapter);
				if (chapter != null) {
					next = chapter.getFirstSearchResult();
					if (next != null) {
						break;
					}
				} else {
					break;
				}
			}
		}
		if (next != null) {
			searchNextChapter = next.getChapterIndex();
			searchNextBlock = next.getParaIndex();
			searchNextOffset = next.getStartOffsetInPara();
		}
	}
	
	public void release() {
		this.isReleased = true;
		setPage(null);
		releaseShare();
		releasePageBitmap();
	}
	
	private void releaseShare() {
		if (shareCoverBitmap != null && !shareCoverBitmap.isRecycled()) {
			shareCoverBitmap.recycle();
			shareCoverBitmap = null;
		}
		if (shareBottomBitmap != null && !shareBottomBitmap.isRecycled()) {
			shareBottomBitmap.recycle();
			shareBottomBitmap = null;
		}
		if (shareTopNoteBitmap != null && !shareTopNoteBitmap.isRecycled()) {
			shareTopNoteBitmap.recycle();
			shareTopNoteBitmap = null;
		}
		if (shareTopSummaryBitmap != null && !shareTopSummaryBitmap.isRecycled()) {
			shareTopSummaryBitmap.recycle();
			shareTopSummaryBitmap = null;
		}
	}
	
	public boolean isSamePage(Page p) {
		if (p == null || page == null) {
			return false;
		}
		if (p.getChapter() == null || page.getChapter() == null) {
			return false;
		}
		if (p.getChapter().getSpine().spineIdRef.equals(page.getChapter().getSpine().spineIdRef)) {
			if (p.startParaIndex == page.startParaIndex && p.startOffset == page.startOffset) {
				return true;
			}
		}
		return false;
	}

	public void createAsyncNote(ReadNote note) {
		Chapter chapter = page.getChapter();
		note.chapterName = page.getPageHead();
		note.spineIdRef = chapter.getSpine().spineIdRef;
		note.updateTime = System.currentTimeMillis();
		note.fromParaIndex = noteBegin.paraIndex;
		note.fromOffsetInPara = noteBegin.offsetInPara;
		note.toParaIndex = noteEnd.paraIndex;
		note.toOffsetInPara = noteEnd.offsetInPara + noteEnd.getCount();
		chapter.addReadNote(note);
		endNoteSelection();
		readFunction.refreshNotes();
	}
	
	public void modifyNote(String content, boolean isPrivate) {
		if (modifyReadNote != null) {
			modifyReadNote.contentText = content;
			modifyReadNote.isPrivate = isPrivate;
			modifyReadNote.modified = true;
			hideNoteActionBar();
			readFunction.refreshNotes();
		}
	}
	
	public void cancelModifyNote() {
		hideNoteActionBar();
		invalidate();
	}

	public void cancelAsyncNote() {
		endNoteSelection();
		refreshNote();
		invalidate();
	}

	public void endNoteSelection() {
		noteState = NoteState.END_NOTE;
		noteBegin = null;
		noteEnd = null;
		noteBeginLineIndex = -1;
		noteEndLineIndex = -1;
		hideNoteActionBar();
		beginIndicator.setVisibility(View.INVISIBLE);
		endIndicator.setVisibility(View.INVISIBLE);
		readFunction.endNoteSelection();
		for (PageLine line : page.getLineList()) {
			for (Element element : line.elementList) {
				element.setSelectionStatus(Element.SelectionStatus.UNSELECTION);
			}
		}
	}

	public Kit42View(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Kit42View(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void refreshPlayControl() {
		for (Map.Entry<ImageView, String> entry : playControlMap.entrySet()) {
			ImageView imageView = entry.getKey();
			String id = entry.getValue();
			int resId = com.jingdong.app.reader.R.drawable.icon_audiobooks;
			boolean isPlaying = readFunction.isPlaying(id);
			if (isPlaying) {
				resId =com.jingdong.app.reader.R.drawable.icon_audiobooks_hl03;
			}
			imageView.setImageResource(resId);
		}
	}

	public void refreshNote() {

		if (noteImageView != null) {
			for (ImageView view : noteImageView) {
				this.removeView(view);
			}
			noteImageView.clear();
		}
		if (page == null) {
			return;
		}
		
		page.buildNoteList();

		Map<ReadNote, List<Element>> noteMap = page.getNoteMap();
		if (noteMap == null || noteMap.size() == 0) {
			return;
		}

		List<String> myNotesTag = new ArrayList<String>();
		List<ImageView> imageViews = new ArrayList<ImageView>();
		Map<String, List<ReadNote>> peopleNotesMap = new HashMap<String, List<ReadNote>>();
		
		for (Map.Entry<ReadNote, List<Element>> entry : noteMap.entrySet()) {
			ReadNote note = entry.getKey();
			List<Element> elementList = entry.getValue();
			if (!TextUtils.isEmpty(note.contentText) && elementList.size() > 0 
					&& note.userId.equals(BookPageViewActivity.getUserId())) {
				Element element = elementList.get(0);
				String elementTop = String.valueOf(element.rect.top);
				myNotesTag.add(elementTop);
				ImageView imageView = new ImageView(getContext());
				imageView.setImageResource(R.drawable.icon_read_note_text_hl);
				imageView.setPadding((int) (15 * density), (int) (15 * density), 0,
						(int) (15 * density));
				LayoutParams rl = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				rl.topMargin = (int) (element.rect.top + (BookPageViewActivity.PageMarginTop - 15)
						* density);
				rl.addRule(ALIGN_PARENT_RIGHT);
				addView(imageView, rl);
				imageViews.add(imageView);
				final ReadNote nt = note;
				imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						showNoteContent(nt);
					}

				});
			}
		}

		for (Map.Entry<ReadNote, List<Element>> entry : noteMap.entrySet()) {
			ReadNote note = entry.getKey();
			if (note.userId.equals(BookPageViewActivity.getUserId())) {
				continue;
			}
			List<Element> elementList = entry.getValue();
			if (!TextUtils.isEmpty(note.contentText) && elementList.size() > 0) {
				Element element = elementList.get(0);
				float top = computeNoteIconTop(myNotesTag, element);
				String elementTop = String.valueOf(top);
				if (peopleNotesMap.containsKey(elementTop)) {
					List<ReadNote> notes = peopleNotesMap.get(elementTop);
					notes.add(note);
					peopleNotesMap.put(elementTop, notes);
					continue;
				}
				List<ReadNote> noteList = new ArrayList<ReadNote>();
				noteList.add(note);
				peopleNotesMap.put(elementTop, noteList);
				ImageView imageView = new ImageView(getContext());
				imageView.setTag(elementTop);
				imageView.setImageResource(R.drawable.icon_read_note_text_gray);
				imageView.setPadding((int) (15 * density), (int) (15 * density), 0,
						(int) (15 * density));
				LayoutParams rl = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				rl.topMargin = (int) (top + (BookPageViewActivity.PageMarginTop - 15)
						* density);
				rl.addRule(ALIGN_PARENT_RIGHT);
				addView(imageView, rl);
				imageViews.add(imageView);
			}
		}
		
		for (ImageView view : imageViews) {
			Object tag = view.getTag();
			if (tag instanceof String) {
				String imageTag = (String) tag;
				if (peopleNotesMap.containsKey(imageTag)) {
					final List<ReadNote> notes = peopleNotesMap.get(imageTag);
					view.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							showPeopleNotesContent(notes);
						}

					});
				}
			}
		}

		if (imageViews.size() > 0) {
			noteImageView = imageViews;
		}
	}
	
	private float computeNoteIconTop(List<String> myNotesTag, Element element) {
		String elementTop = String.valueOf(element.rect.top);
		float top = element.rect.top;
		if (myNotesTag.contains(elementTop)) {
			//如果我的笔记icon已经占据这行，那么查看下一行
			Element e = findNextLineElement(element);
			if (e != null) {
				return computeNoteIconTop(myNotesTag, e);
			} else {
				return element.rect.bottom + pageContext.lineSpace;
			}
		}
		return top;
	}
	
	private Element findNextLineElement(Element element) {
		float top = element.rect.top;
		List<PageLine> lineList = page.getLineList();
		for (int i = 0; i < lineList.size(); ++i) {
			List<Element> line = lineList.get(i).elementList;
			if (line != null && line.size() > 0) {
				if (top == line.get(0).rect.top) {
					if (i < lineList.size() - 1) {
						line = lineList.get(i + 1).elementList;
						if (line != null && line.size() > 0) {
							return line.get(0);
						} else {
							return null;
						}
					} else {
						return null;
					}
				}
			}
		}
		return null;
	}
	
	private ElementText findNextElementText(ElementText element) {
		boolean isFirstElement = false;
		List<PageLine> lineList = page.getLineList();
		for (int i = 0; i < lineList.size(); ++i) {
			List<Element> line = lineList.get(i).elementList;
			if (isFirstElement) {
				Element et = line.get(0);
				if (et instanceof ElementText) {
					return (ElementText) et;
				}
			}
			for (int j = 0; j < line.size(); j++) {
				Element e = line.get(j);
				if (e == element) {
					j++;
					if (j >= line.size()) {
						isFirstElement = true;
						break;
					}
					Element et = line.get(j);
					if (et instanceof ElementText) {
						return (ElementText) et;
					}
				}
			}
		}
		return null;
	}
	
	public Page getPage() {
		return this.page;
	}
	
	public void setPage(Page page) {
		this.hidePopOver();
		this.hideNoteActionBar();
		this.playControlMap.clear();
		this.removeAllViews();
		this.bookFinishView = null;
		if (noteImageView != null) {
			noteImageView.clear();
		}
		if (imageViewList != null) {
			imageViewList.clear();
		}
		this.isAddBookMark = false;
		this.modifyReadNote = null;
		this.mIsMovingPaper = false;
		
		if (this.page instanceof CoverPage) {
			CoverPage cp = (CoverPage) this.page;
			cp.release();
		}
		this.page = page;
		if (page == null) {
			View v = readFunction.getLoadingView();
			this.addView(v, new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT));
			this.invalidate();
			return;
		}

		if (page instanceof CoverPage) {
			initPaper();
			return;
		}
		if (page instanceof FinishPage) {
			//remove backcover view
			// bookFinishView = readFunction.getLastPageView();
			// this.addView(bookFinishView, new RelativeLayout.LayoutParams(
			// RelativeLayout.LayoutParams.MATCH_PARENT,
			// RelativeLayout.LayoutParams.MATCH_PARENT));
			return;
		}
		
		this.pageContext = page.getPageContext();
		if (isReadNoteShareFlow) {
			return;
		}
		this.isAddBookMark = page.getChapter().isBookMarkInPageScope(page);

		refreshNote();

		List<ElementImage> pictureList = page.getPictureList();
		layoutImage(pictureList);
		List<PageLink> linkList = page.getLinkList();
		layoutLink(linkList);

		addView(beginIndicator, new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));
		addView(endIndicator, new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));
		
		bookmarkBglp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				bookMarkMaxDistanceY);
		bookmarkBglp.topMargin = -bookMarkMaxDistanceY;
		addView(bookMarkBgLayout, bookmarkBglp);
		
		arrowlp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				bookMarkActionPointY);
		arrowlp.topMargin = -bookMarkActionPointY;
		addView(arrowLayout, arrowlp);
		
		bookmarklp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				bookMarkActionPointY);
		bookmarklp.topMargin = isAddBookMark?0:-bookMarkActionPointY;
		addView(bookMarkLayout, bookmarklp);
		
		RelativeLayout.LayoutParams bookFontLoadlp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				screenHeight);
		addView(fontDownloadLayout, bookFontLoadlp);
		refreshDownloadFontBar();
		
		RelativeLayout.LayoutParams bookSearchlp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				screenHeight);
		addView(searchBarLayout, bookSearchlp);
		refreshSearchBar();
		if (readFunction.isShowSearchBar()) {
			String keyword = readFunction.getSearchKeywords();
			String words = StringUtil.escapeExprSpecialWord(keyword);
			page.prepareSearchHighlight(keyword, words);
			preparePrevSearchPage();
			prepareNextSearchPage();
			setSearchPrevNextButtonStatus();
		}
		
		RelativeLayout.LayoutParams shadowlp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.MATCH_PARENT);
		shadowlp.leftMargin = screenWidth-4;
		addView(pageShadowLayout, shadowlp);
		pageShadowLayout.setVisibility(View.GONE);
		
		initPaper();
		invalidate();
	}
	
	public void showPageShadow() {
		pageShadowLayout.setVisibility(View.VISIBLE);
	}
	
	public void hidePageShadow() {
		pageShadowLayout.setVisibility(View.GONE);
	}
	
	/**
	 * 展示epub中的链接
	 * @param linkList
	 */
	private void layoutLink(List<PageLink> linkList) {
		for (final PageLink link : linkList) {
			View linkView = new View(getContext());
			linkView.setBackgroundResource(R.drawable.link_overlay);
			linkView.setFocusable(true);
			linkView.setClickable(true);
			RectF rect = link.getRect();
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					(int) rect.width(), (int) rect.height());
			lp.topMargin = (int) (rect.top + BookPageViewActivity.PageMarginTop
					* density);
			lp.leftMargin = (int) (rect.left + BookPageViewActivity.getPageMarginLeft()
					* density);
			addView(linkView, lp);
			linkView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (link.isJumpAction()) {
						if (!TextUtils.isEmpty(link.getFootnoteId())) {
							if (link.getFootnoteId().startsWith("#")) {
								String id = link.getFootnoteId().replace("#", "");
								readFunction.doJumpAnchorAction(id);
							}
						}
					} else if (!TextUtils.isEmpty(link.getFootnoteId())) {
						if (link.getFootnoteId().startsWith("#")) {
							String id = link.getFootnoteId().replace("#", "");
							Chapter chapter = Kit42View.this.page.getChapter();
							String text = chapter.queryFootnote(id);
							if (!TextUtils.isEmpty(text)) {
								showAnnotation(text, v.getLeft(), v.getTop());
							}
						}
					} else {
						if (!TextUtils.isEmpty(link.getUrlText())) {
							if (link.getUrlText().startsWith("#")) {
								String id = link.getUrlText().replace("#", "");
								readFunction.doJumpAnchorAction(id);
							} else {
								Uri uri = Uri.parse(link.getUrlText());
								if (uri != null) {
									if (uri.isRelative()) {
										readFunction.doJumpChapterAction(link.getUrlText(), link.getBasePath());
									} else {
										openUrl(link.getUrlText());
									}
								}
							}
						}
					}
				}

			});
		}
	}

	private void openUrl(String urlText) {
		Uri uri = Uri.parse(urlText);
		if (uri != null && !uri.isRelative()) {
			Intent intent = new Intent(getContext(), WebViewActivity.class);
			intent.putExtra(WebViewActivity.UrlKey, urlText);
			getContext().startActivity(intent);
		}
	}
	
	public void refreshDownloadFontBar() {
		String text = readFunction.getDownloadFontText();
		if (TextUtils.isEmpty(text) || LocalUserSetting.isIgnoreFontDownload(ctx)) {
			fontDownloadLayout.setVisibility(View.GONE);
		} else {
			fontDownloadLayout.setVisibility(View.VISIBLE);
			TextView textView = (TextView) fontDownloadLayout.findViewById(R.id.font_download_text);
			textView.setText(text);
		}
	}
	
	public void refreshSearchBar() {
		searchBarLayout.setVisibility(readFunction.isShowSearchBar()?View.VISIBLE:View.GONE);
	}
	
	public void refreshImageBackground() {
		if (page instanceof FinishPage) {
			if (bookFinishView != null) {
				bookFinishView.updateTheme(ctx);
			}
		}
	}

	/**
	 * 展示epub中的图片
	 * @param pictureList
	 */
	private void layoutImage(List<ElementImage> pictureList) {
		if (pictureList == null || pictureList.size() == 0) {
			return;
		}
		int padding = (int) (5 * density);
		String chapterItemRef = this.page.getChapter().getSpine().spineIdRef;
		for (final ElementImage picture : pictureList) {
			if(null == picture) {
				continue;
			}
			final ImageView imageView = new ImageView(getContext());
			picture.setImageView(imageView);
			picture.setChapterItemRef(chapterItemRef);
			if (picture.isFootnote()) {
				imageView
						.setImageResource(R.drawable.icon_note);
			} else if (picture.isPlayControl()) {
				Uri uri = Uri.parse(picture.getAudioLink());
				String id = uri.getFragment();
				int resId = R.drawable.icon_audiobooks;
				if (!TextUtils.isEmpty(id)) {
					boolean isPlaying = readFunction.isPlaying(id);
					if (isPlaying) {
						resId = R.drawable.icon_audiobooks_hl03;
					}
					playControlMap.put(imageView, id);
				}
				imageView.setImageResource(resId);
			} else {
				Bitmap bitmap = picture.getBitmap(density,ctx);
				imageView.setImageBitmap(bitmap);
				imageView.setBackgroundColor(Color.argb(0, 0, 0, 0));
				ScaleType scaleType = picture.getImageScaleType();
				if (scaleType != null) {
					imageView.setScaleType(scaleType);
				}
				bitmap = null;
			}

			imageView.setFocusable(true);
			imageView.setClickable(true);
			RelativeLayout.LayoutParams lp;
			View v = null;
			RelativeLayout.LayoutParams vlp = null;
			if (picture.isFullScreen()) {
				lp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.MATCH_PARENT);
			} else if (picture.supportEnlarge()) {
				lp = new RelativeLayout.LayoutParams(
						(int) picture.rect.width(), (int) picture.rect.height());
				lp.topMargin = (int) (picture.rect.top + BookPageViewActivity.PageMarginTop
						* density);
				lp.leftMargin = (int) (picture.rect.left + BookPageViewActivity.getPageMarginLeft()
						* density);
				v = new View(getContext());
				v.setBackgroundResource(R.drawable.shadow_black);
				vlp = new RelativeLayout.LayoutParams(
						(int) picture.rect.width() + padding * 2,
						(int) picture.rect.height() + padding);
				vlp.topMargin = (int) (picture.rect.top + BookPageViewActivity.PageMarginTop
						* density);
				vlp.leftMargin = (int) (picture.rect.left
						+ BookPageViewActivity.getPageMarginLeft() * density - padding);
			} else if (picture.isPlayControl()) {
				lp = new RelativeLayout.LayoutParams(
						(int) (picture.rect.width() * 3 / 2),
						(int) (picture.rect.height() * 3));
				lp.topMargin = (int) (picture.rect.top
						+ BookPageViewActivity.PageMarginTop * density - picture.rect
						.height());
				lp.leftMargin = (int) (picture.rect.left
						+ BookPageViewActivity.getPageMarginLeft() * density - picture.rect
						.width() / 4);
				imageView.setScaleType(ScaleType.CENTER);
			} else {
				lp = new RelativeLayout.LayoutParams(
						(int) picture.rect.width(), (int) picture.rect.height());
				lp.topMargin = (int) (picture.rect.top + BookPageViewActivity.PageMarginTop
						* density);
				lp.leftMargin = (int) (picture.rect.left + BookPageViewActivity.getPageMarginLeft()
						* density);
				
				// 所有图片支持独立层放大，除了 A.图片高<=行高+行距；B.图片外有anchor；C.全屏图片
				if (picture.isEnlargeEnable() && TextUtils.isEmpty(picture.getAudioLink())
								&& TextUtils.isEmpty(picture.getFootnoteId())
								&& !picture.isJumpAction() && !picture.isLink()) {
					picture.setSupportEnlarge(true);
					v = new View(getContext());
					v.setBackgroundResource(R.drawable.shadow_black);
					vlp = new RelativeLayout.LayoutParams(
							(int) picture.rect.width() + padding * 2,
							(int) picture.rect.height() + padding);
					vlp.topMargin = (int) (picture.rect.top + BookPageViewActivity.PageMarginTop
							* density);
					vlp.leftMargin = (int) (picture.rect.left
							+ BookPageViewActivity.getPageMarginLeft() * density - padding);
				}
			}

			if (picture.isFootnote()) {
				lp = new RelativeLayout.LayoutParams(
						(int) (picture.rect.width() * 1.5f),
						(int) (picture.rect.height() * 1.5f));
				lp.topMargin = (int) (picture.rect.top
						+ BookPageViewActivity.PageMarginTop * density - picture.rect
						.height() * 0.25f);
				lp.leftMargin = (int) (picture.rect.left + BookPageViewActivity.getPageMarginLeft()
						* density);
				imageView.setScaleType(ScaleType.CENTER);
			}
			
			/***************** Image Float Code Start *******************/
			if (picture.isFloatLeft()) {
				lp.leftMargin = (int) (picture.getMarginLeft() + BookPageViewActivity
						.getPageMarginLeft() * density);
				if (vlp != null) {
					vlp.leftMargin = (int) (picture.getMarginLeft() + BookPageViewActivity
							.getPageMarginLeft() * density - padding);
				}
			} else if (picture.isFloatRight()) {
				lp.leftMargin = (int) (screenWidth - picture.rect.width()
						- picture.getMarginRight() - BookPageViewActivity
						.getPageMarginRight() * density);
				if (vlp != null) {
					vlp.leftMargin = (int) (screenWidth - picture.rect.width()
							- picture.getMarginRight() - BookPageViewActivity
							.getPageMarginRight() * density - padding);
				}
			}
			/***************** Image Float Code End *******************/

			imageView.setTag(lp);
			if (v != null) {
				addView(v, vlp);
				imageViewList.add(v);
			}
			addView(imageView, lp);
			imageViewList.add(imageView);
			if (!TextUtils.isEmpty(picture.getFootnoteId())
					|| !TextUtils.isEmpty(picture.getAudioLink())
					|| picture.isJumpAction() || picture.isLink() 
					|| picture.supportEnlarge()) {
				
			   
				imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!TextUtils.isEmpty(picture.getFootnoteId())) {
							String text = picture.getFootnoteId();
							String id = picture.getFootnoteId().replace("#", "");
							Chapter chapter = Kit42View.this.page.getChapter();
							text = chapter.queryFootnote(id);
							if (!TextUtils.isEmpty(text)) {
								showAnnotation(text, v.getLeft(), v.getTop());
							}
						} else if (!TextUtils.isEmpty(picture.getAudioLink())) {
							Uri uri = Uri.parse(picture.getAudioLink());
							String id = uri.getFragment();
							if (!TextUtils.isEmpty(id)) {
								//boolean isPlaying = 
								readFunction.playAudio(id);
								refreshPlayControl();
								/*if (isPlaying) {
									imageView
											.setImageResource(com.jingdong.app.reader.R.drawable.icon_audiobooks_hl03);
								} else {
									imageView
											.setImageResource(com.jingdong.app.reader.R.drawable.icon_audiobooks);
								}*/
							}
						} else if (picture.isJumpAction()) {
							if (!TextUtils.isEmpty(picture.getFootnoteId())) {
								if (picture.getFootnoteId().startsWith("#")) {
									String id = picture.getFootnoteId().replace("#", "");
									readFunction.doJumpAnchorAction(id);
								}
							}
						} else if (picture.isLink()) {
							openUrl(picture.getUrlText());
						} else {
							if (!picture.isFullScreen()
									&& picture.supportEnlarge()) {
								
								if(!UiStaticMethod.isEmpty(picture.getSrc()))
								{
									long ebookId = page.getChapter().geteBookId();
							        int docId = page.getChapter().getDocId();
							        String bookId="",bookName="";
							        if (ebookId > 0) {
							            LocalBook book = LocalBook.getLocalBook(ebookId,LoginUser.getpin());
							            bookName = book.title;
							            bookId = String.valueOf(ebookId);
							        } else if (docId > 0) {
							            Document document = MZBookDatabase.instance.getDocument(docId);
							            bookName = document.title;
							            bookId = String.valueOf(ebookId);
							        }
									
									imageView.setClickable(false);
									Intent intent = new Intent(ctx,BookPageImageEnlargeActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.putExtra("imageResource", picture.getSrc());
									intent.putExtra("bookName", bookName);
									intent.putExtra("bookId", bookId);
									ctx.startActivity(intent);	
									try {
										((Activity) ctx).overridePendingTransition(R.anim.alpha_in_shorttime, 0);
									} catch (Exception e) {
										e.printStackTrace();
									}
									imageView.setClickable(true);
								}
	
								
							/*	RelativeLayout.LayoutParams lp = (LayoutParams) v
										.getLayoutParams();
								RelativeLayout.LayoutParams savedLp = (LayoutParams) v
										.getTag();
								if (lp == savedLp) {
									RelativeLayout.LayoutParams fullScreenlp = new RelativeLayout.LayoutParams(
											RelativeLayout.LayoutParams.MATCH_PARENT,
											RelativeLayout.LayoutParams.MATCH_PARENT);
									v.setLayoutParams(fullScreenlp);
									imageView.bringToFront();
								} else {
									v.setLayoutParams(savedLp);
								}
								*/
							}

						}

					}
				});
			} else {
				imageView.setClickable(false);
			}
		}
	}
	
	/**
	 * 仿真翻页专用，在动画开始时隐藏图片
	 */
	private void hideAllImageViewOnAnimationStart() {
		for (View view : imageViewList) {
			view.setVisibility(View.GONE);
		}
		if (noteImageView != null) {
			for (ImageView view : noteImageView) {
				view.setVisibility(View.GONE);
			}
		}
		bookMarkLayout.setVisibility(View.GONE);
		fontDownloadLayout.setVisibility(View.GONE);
	}
	
	/**
	 * 仿真翻页专用，在动画结束时显示图片
	 */
	private void showAllImageViewOnAnimationEnd() {
		for (View view : imageViewList) {
			view.setVisibility(View.VISIBLE);
		}
		if (noteImageView != null) {
			for (ImageView view : noteImageView) {
				view.setVisibility(View.VISIBLE);
			}
		}
		if (isAddBookMark) {
			bookMarkLayout.setVisibility(View.VISIBLE);
		}
		refreshDownloadFontBar();
	}

	private boolean hidePopOver() {
		if (isNoteContentShowing) {
			isNoteContentShowing = false;
			return true;
		} else {
			return false;
		}
	}

	private boolean hideNoteActionBar() {
		if (popupWindow != null && popupWindow.isPopShowing()) {
			popupWindow.setPopShowing(false);
			popupWindow.dismiss();
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (isReleased || page == null || page instanceof FinishPage) {
			return;
		}
		if (mIsMovingPaper && readFunction.isPageAnimationTurning() && mCurPageBitmap != null) {
			if(mIsFlatTurning){
				drawPageEffect(canvas);
			}
			else{
				onPageDraw(canvas);
			}
			
			return;
		}
		
		if (page instanceof CoverPage) {
			CoverPage cp = (CoverPage) page;
			cp.draw(canvas);
			return;
		}
		
		drawReadingPageBackgroud(canvas);

		canvas.save();
		if (!page.isContentReady()) {
			page.drawLoadingText(canvas, this.getWidth(), this.getHeight(), BookPageViewActivity.getFontColor());
			return;
		}
		
		if (isReadNoteShareFlow) {
			canvas.drawBitmap(shareTopSummaryBitmap, 0, 0, footHeadPaint);
			canvas.drawBitmap(shareBottomBitmap, 0, shareReadNoteHeight - shareBottomHeight, footHeadPaint);
			if (shareNoteContentList != null && shareNoteContentList.size() > 0) {
				float contentHeiht = shareTopHeight + sharePaint.getTextSize();
				sharePaint.setColor(0xFFF2F2F2);
				canvas.drawRect(0, shareTopHeight, screenWidth, shareTopHeight + shareContentHeight, sharePaint);
				sharePaint.setColor(0xFF333333);
				for (String line : shareNoteContentList) {
					canvas.drawText(line, BookPageViewActivity.getPageMarginLeft() * density, contentHeiht, sharePaint);
					contentHeiht += sharePaint.descent() - sharePaint.ascent();
					contentHeiht += BookPageViewActivity.PageLineSpace * sharePaint.getTextSize();
				}
			}
			canvas.translate(BookPageViewActivity.getPageMarginLeft() * density, shareTopHeight + shareContentHeight + sharePaint.getTextSize());
		} else {
			canvas.translate(BookPageViewActivity.getPageMarginLeft() * density, BookPageViewActivity.PageMarginTop * density);
		}

		//页面行
		List<PageLine> lineList = page.getLineList();
		List<Border> borderList = new ArrayList<Border>();
		Border border = null;
		int divCode = -1;
		for (PageLine line : lineList) {
			line.draw(canvas, pageContext, ctx, density, page.isReadNoteShare());//画文字行
			if (line.style != null && line.style.isDrawBorder()) {
				if (divCode >= 0) {
					if (divCode == line.getDivHashcode()) {
						float bottom = Math.max(border.getRect().bottom, line.rect.bottom + line.getPaddingBottom());
						border.resetBottom(bottom);
						continue;
					}
				}
				divCode = line.getDivHashcode();
				border = Border.parseBorder(line.style);
				border.setCssCollection(line.getCssCollection());
				border.setPaint(line.paint);
				float left = line.rect.left;
				float top = line.rect.top - line.getPaddingTop();
				float right = line.rect.left + line.bgWidth;
				float bottom = line.rect.bottom + line.getPaddingBottom();
				if (line.isTableBorder()) {
					right = line.rect.left + line.bgWidth;
					bottom = line.getTableCellYOffset();
				}
				border.setRect(new RectF(left, top, right, bottom));
				borderList.add(border);
			}
		}
		drawBorder(canvas, borderList);

		canvas.restore();
		if(getTextureFootHeadColor() != -1){
			footHeadPaint.setColor(getTextureFootHeadColor());
		}
		else{
			footHeadPaint.setColor(BookPageViewActivity.getFootHeadColor());
		}
		
		if (isReadNoteShareFlow) {
			//如果是分享笔记要绘制书籍封面书名作者
			int coverTop = shareReadNoteHeight - shareBottomHeight - shareCoverHeight;
			if (shareCoverBitmap != null) {
				sharePaint.setColor(0xFFBFBFBF);
				canvas.drawLine(0, coverTop, screenWidth, coverTop, sharePaint);
				canvas.drawBitmap(shareCoverBitmap, 16 * density, coverTop + 16 * density, sharePaint);
				if (!TextUtils.isEmpty(shareBookName)) {
					sharePaint.setColor(0xFF333333);
					canvas.drawText(shareBookName, shareCoverBitmap.getWidth() + 24 * density, coverTop + 32 * density, sharePaint);
				}
				if (!TextUtils.isEmpty(shareAuthor)) {
					sharePaint.setColor(0xFFA5A5A5);
					sharePaint.setTextSize(12 * density);
					canvas.drawText(shareAuthor, 84 * density, coverTop + (32 + 18 * 2) * density, sharePaint);
				}
			}
		} else if (page.getChapter() != null) {
			if (page.isFullScreenImage()) {
				return;
			}
			String head = page.getPageHead();
			if (!TextUtils.isEmpty(head)) {
				footHeadPaint.setTextAlign(Align.LEFT);
				int charCount = footHeadPaint.breakText(head, true, readFunction.getPageWidth(), null);
				String headText = null;
				if (charCount < head.length()) {
					headText = head.substring(0, charCount - 1) + "...";
				} else {
					headText = head;
				}
				canvas.drawText(headText, BookPageViewActivity.getPageMarginLeft() * density, 35 * density, footHeadPaint);
			}
			
			float footY = this.getHeight() - 20 * density;
			int pageNumber = page.getChapter().getPageOffset(page);
			int totalNumber = page.getChapter().getBookPageCount();
			footHeadPaint.setTextAlign(Align.RIGHT);
			if (pageNumber != -1 && totalNumber != -1) {
				totalNumber += 1;//使最后一页页数看起来是倒数第二页
				String foot = (pageNumber + 1) + "/" + totalNumber;
				canvas.drawText(
						foot,
						this.getWidth()
								- BookPageViewActivity.getPageMarginRight()
								* density, footY,
						footHeadPaint);
			}
			
			int batteryPercent = readFunction.getBatteryPercent();
			RectF rect = new RectF();
			rect.left = BookPageViewActivity.getPageMarginLeft() * density;
			rect.top = footY - footAscent + footDescent / 2;
			rect.right = rect.left + footAscent * 2;
			rect.bottom = rect.top + footAscent;
			canvas.drawRoundRect(rect, 6, 6, footHeadPaint);
			RectF rectf = new RectF();
			rectf.left = rect.right + 1;
			rectf.top = rect.top + 9;
			rectf.right = rect.right + 4;
			rectf.bottom = rect.bottom - 9;
			canvas.drawRoundRect(rectf, 2, 2, footHeadPaint);
			footHeadPaint.setColor(BookPageViewActivity.getBackgroundColor());
			rectf.left = rect.left + 2;
			rectf.top = rect.top + 2;
			rectf.right = rect.right - 2;
			rectf.bottom = rect.bottom - 2;
			canvas.drawRoundRect(rectf, 5, 5, footHeadPaint);
			if(getTextureFootHeadColor() != -1)
				footHeadPaint.setColor(getTextureFootHeadColor());
			else
				footHeadPaint.setColor(BookPageViewActivity.getFootHeadColor());
			rectf.left = rect.left + 4;
			rectf.top = rect.top + 4;
			rectf.right = rect.right - 4;
			rectf.bottom = rect.bottom - 4;
			float total = rectf.right - rectf.left;
			float real = total * batteryPercent / 100;
			rectf.right  = rectf.left + real;
			canvas.drawRoundRect(rectf, 5, 5, footHeadPaint);
			String time = DateUtil.currentTimeHM();
			canvas.drawText(time, rect.right + 20 + footHeadPaint.measureText(time) , footY, footHeadPaint);
		}
		generatePageBitmap();
	}

	/**
	 * 绘画背景（纯色背景或者纹理）
	 * @param canvas
	 */
	private void drawReadingPageBackgroud(Canvas canvas) {
		if (page.isFullScreenImage()) {//全配图片
			canvas.drawColor(0xFF000000);//黑色背景
		} else {
			//若为纹理背景图
			if(!LocalUserSetting.getReading_Night_Model(ctx) && 
					LocalUserSetting.getReading_Background_Texture(ctx) != -1 && 
					LocalUserSetting.getReading_Background_Texture(ctx) < 9){
				int[] drawables ={R.drawable.wl1,R.drawable.wl2,R.drawable.wl3,R.drawable.wl4,R.drawable.wl5,R.drawable.wl6,R.drawable.wl7,R.drawable.wl8,R.drawable.wl9};
				try {
					if(texturebmp ==null || preSelectedTextureIndex != LocalUserSetting.getReading_Background_Texture(ctx)){
						preSelectedTextureIndex = LocalUserSetting.getReading_Background_Texture(ctx);
						texturebmp = BitmapFactory.decodeResource(getResources(), drawables[LocalUserSetting.getReading_Background_Texture(ctx)]);
					}
					int count = (this.getWidth() + texturebmp.getWidth() - 1) / texturebmp.getWidth();
					int heightcount = (this.getHeight() + texturebmp.getHeight() - 1) / texturebmp.getHeight();
					for (int i = 0; i < heightcount; i++) {
						for(int idx = 0; idx < count; ++ idx){
							canvas.drawBitmap(texturebmp, idx * texturebmp.getWidth(), i*texturebmp.getHeight(), null);
						}
					}
					//牛皮纸背景需要增加背景图
					if(LocalUserSetting.getReading_Background_Texture(ctx) == 6){
						if(brownPaperBg == null){
							int width = (int)ScreenUtils.getWidthJust(ctx);
							int height = (int)ScreenUtils.getHeightJust(ctx);
							Bitmap bg= ImageUtils.getBitmapFromResource(ctx, R.drawable.wl7_bg, width, height);
							brownPaperBg = Bitmap.createScaledBitmap(bg, width, height, true);  
							if (!bg.isRecycled()) {
								bg.recycle();
							}
						}
						canvas.drawBitmap(brownPaperBg, 0 , 0, null);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else {//若为纯色背景图
				canvas.drawColor(isReadNoteShareFlow ? 0xFFFFFFFF : BookPageViewActivity.getBackgroundColor());
			}
		}
	}
	
	/**
	 * 获取纹理背景下的header和footer的文字颜色
	 * @return
	 */
	private int getTextureFootHeadColor(){
		int texture = LocalUserSetting.getReading_Background_Texture(ctx);
		int color = -1 ;
		if(texture != -1){
			switch (texture) {
			case 0:
				color = 0xFF55504C;
				break;
			case 1:
				color = 0xFF695E57;
				break;
			case 2:
				color = 0xFF5E5A57;
				break;
			case 3:
				color = 0xFF656A65;
				break;
			case 4:
				color = 0xFF64655D;
				break;
			case 5:
				color = 0xFF4E4044;
				break;
			case 6:
				color = 0xFF645C52;
				break;
			case 7:
				color = 0xFFA6ACB2;
				break;
			case 8:
				color = 0xFF5F645F;
				break;
			default:
				break;
			}
		}
		return color ;
	}
	
    void drawBorder(Canvas canvas, List<Border> borderList) {
    	for (Border border : borderList) {
    		BorderStyle borderStyle = border.getStyle();
			if (borderStyle == BorderStyle.None
					|| borderStyle == BorderStyle.Hidden) {
				continue;
			}
			RectF rect = border.getRect();
			int borderColor = border.getColor();
			float borderWidth = border.getWidth();
			Paint paintBorder = new Paint(border.getPaint());
			paintBorder.setColor(borderColor);
			if (borderStyle == BorderStyle.Solid) {
				paintBorder.setStyle(Paint.Style.FILL);
				if (border.isLeftBorder()) {
					canvas.drawRect(rect.left, rect.top, rect.left+border.getLeftWidth(), rect.bottom, paintBorder);
				}
				if (border.isTopBorder()) {
					canvas.drawRect(rect.left, rect.top, rect.right, rect.top+border.getTopWidth(), paintBorder);
				}
				if (border.isRightBorder()) {
					canvas.drawRect(rect.right-border.getRightWidth(), rect.top, rect.right, rect.bottom, paintBorder);
				}
				if (border.isBottomBorder()) {
					canvas.drawRect(rect.left, rect.bottom-border.getBottomWidth(), rect.right, rect.bottom, paintBorder);
				}
				if (border.isFullBorder()) {
					canvas.drawRect(rect.left, rect.top, rect.left+border.getLeftWidth(), rect.bottom, paintBorder);
					canvas.drawRect(rect.left, rect.top, rect.right, rect.top+border.getTopWidth(), paintBorder);
					canvas.drawRect(rect.right-border.getRightWidth(), rect.top, rect.right, rect.bottom, paintBorder);
					canvas.drawRect(rect.left, rect.bottom-border.getBottomWidth(), rect.right, rect.bottom, paintBorder);
				}
				continue;
			}
			if (borderStyle == BorderStyle.Double) {
				paintBorder.setStyle(Paint.Style.FILL);
				if (border.isLeftBorder()) {
					canvas.drawRect(rect.left, rect.top, rect.left+borderWidth/3, rect.bottom, paintBorder);
					canvas.drawRect(rect.left+borderWidth*2/3, rect.top, rect.left+borderWidth, rect.bottom, paintBorder);
				}
				if (border.isTopBorder()) {
					canvas.drawRect(rect.left, rect.top, rect.right, rect.top+borderWidth/3, paintBorder);
					canvas.drawRect(rect.left, rect.top+borderWidth*2/3, rect.right, rect.top+borderWidth, paintBorder);
				}
				if (border.isRightBorder()) {
					canvas.drawRect(rect.right-borderWidth/3, rect.top, rect.right, rect.bottom, paintBorder);
					canvas.drawRect(rect.right-borderWidth, rect.top, rect.right-borderWidth*2/3, rect.bottom, paintBorder);
				}
				if (border.isBottomBorder()) {
					canvas.drawRect(rect.left, rect.bottom-borderWidth/3, rect.right, rect.bottom, paintBorder);
					canvas.drawRect(rect.left, rect.bottom-borderWidth, rect.right, rect.bottom-borderWidth*2/3, paintBorder);
				}
				if (border.isFullBorder()) {
					float w = borderWidth;
					float w13 = borderWidth/3;
					float w23 = borderWidth*2/3;
					canvas.drawRect(rect.left, rect.top, rect.left+w13, rect.bottom, paintBorder);
					canvas.drawRect(rect.left+w23, rect.top+w23, rect.left+w, rect.bottom-w23, paintBorder);
					canvas.drawRect(rect.left, rect.top, rect.right, rect.top+w13, paintBorder);
					canvas.drawRect(rect.left+w23, rect.top+w23, rect.right-w23, rect.top+w, paintBorder);
					canvas.drawRect(rect.right-w13, rect.top, rect.right, rect.bottom, paintBorder);
					canvas.drawRect(rect.right-w, rect.top+w23, rect.right-w23, rect.bottom-w23, paintBorder);
					canvas.drawRect(rect.left, rect.bottom-w13, rect.right, rect.bottom, paintBorder);
					canvas.drawRect(rect.left+w23, rect.bottom-w, rect.right-w23, rect.bottom-w23, paintBorder);
				}
				continue;
			}
			paintBorder.setStrokeWidth(borderWidth);
			paintBorder.setStyle(Paint.Style.STROKE);
			Path path = new Path();
			if (border.isLeftBorder()) {
				path.moveTo(rect.left+borderWidth/2, rect.top);
				path.lineTo(rect.left+borderWidth/2, rect.bottom);
			}
			if (border.isTopBorder()) {
				path.moveTo(rect.left, rect.top+borderWidth/2);
				path.lineTo(rect.right, rect.top+borderWidth/2);
			}
			if (border.isRightBorder()) {
				path.moveTo(rect.right-borderWidth/2, rect.top);
				path.lineTo(rect.right-borderWidth/2, rect.bottom);
			}
			if (border.isBottomBorder()) {
				path.moveTo(rect.left, rect.bottom-borderWidth/2);
				path.lineTo(rect.right, rect.bottom-borderWidth/2);
			}
			if (border.isFullBorder()) {
				path.moveTo(rect.left+borderWidth/2, rect.top+borderWidth/2);
				path.lineTo(rect.right-borderWidth/2, rect.top+borderWidth/2);
				path.lineTo(rect.right-borderWidth/2, rect.bottom-borderWidth/2);
				path.lineTo(rect.left+borderWidth/2, rect.bottom-borderWidth/2);
				path.lineTo(rect.left+borderWidth/2, rect.top+borderWidth/2);
			}
			if (borderStyle == BorderStyle.Dotted) {
				PathEffect effects = new DashPathEffect(new float[]{2,2,2,2}, 1);
				paintBorder.setPathEffect(effects);
			} else if (borderStyle == BorderStyle.Dashed) {
				PathEffect effects = new DashPathEffect(new float[]{8,4,8,4}, 1);
				paintBorder.setPathEffect(effects);
			}
			canvas.drawPath(path, paintBorder);
    	}
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (readFunction.isShowSearchBar()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                readFunction.stopAndClearSearch();
                readFunction.hideSearchBar();
            }
            return true;
        }
        if (noteState != NoteState.END_NOTE) {

            if (event.getAction() == MotionEvent.ACTION_DOWN
                    && noteBegin != null && noteEnd != null) {
                if (noteBegin == noteEnd && noteBegin instanceof ElementImage) {
                    if (((ElementImage)noteBegin).isFullScreen()) {
                        isCancelNote = true;
                        return true;
                    }
                }
                if (!isInSelectedRegion(event.getX(), event.getY())
                        && !isBeginEndIndicator(event.getX(), event.getY())) {
                    isCancelNote = true;
                    return true;
                }
                float y = event.getY()
                        - (BookPageViewActivity.PageMarginTop + 50) * density;
                float x = event.getX() - BookPageViewActivity.getPageMarginLeft()
                        * density;
                float deltaBeginY = Math.abs(y - noteBegin.getRect().centerY());
                float deltaEndY = Math.abs(y - noteEnd.getRect().centerY());
                if (deltaBeginY < deltaEndY) {
                    noteState = NoteState.MOVE_BEGIN;
                } else if (deltaBeginY == deltaEndY) {
                    float deltaBeginX = Math.abs(x - noteBegin.getRect().left);
                    float deltaEndX = Math.abs(x - noteEnd.getRect().right);
                    if (deltaBeginX < deltaEndX) {
                        noteState = NoteState.MOVE_BEGIN;
                    } else {
                        noteState = NoteState.MOVE_END;
                    }
                } else {
                    noteState = NoteState.MOVE_END;
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (isLongPressSelectState) {
                    if (isMove(event.getX(), event.getY())) {
                        isLongPressSelectState = false;
                    }
                }
                if (!isCancelNote && !isLongPressSelectState) {
                    Element element = findSelectedElement(event.getX(),
                            event.getY() - 25 * density);
                    if (element != null) {
                        hideNoteActionBar();
                        refreshSelection(element);
                        invalidate();
                    }
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                isLongPressSelectState = false;
                if (isCancelNote) {
                    endNoteSelection();
                } else {
                    showActionBarForCreatNote();
                }
            }
            
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isBookMarkScroll && !isCancelNote) {
                if (scrollDeltaY >= bookMarkActionPointY) {
                    toggleBookMark();
                }
                showBookMark();
                return true;
            }
            actionDownTime = System.currentTimeMillis();
            if(mIsFlatTurning && !mIsTouchDown){
            	int dx,dy;
            	boolean turnLeft = true;
                dy = 0;
                if (lastTouchX < mTouch.x) {//向右滑动
                    dx = mWidth - (int)mTouch.x + 100;
                    turnLeft = false;
                }else{ //向左滑动
                    dx = -(int)mTouch.x - mWidth -100;
                    turnLeft = true;
                }
                mScroller.startScroll((int)mTouch.x,(int)mTouch.y,dx,dy, Kit42View.PAGE_ANIMATION_DURATION);
                if(turnLeft && mIsMovingNext){
                	readFunction.savePageAnimationTime();
                	readFunction.forceGotoNextPageInAnimation();
                }else if(!turnLeft && !mIsMovingNext){
                	readFunction.savePageAnimationTime();
                	readFunction.forceGotoPrevPageInAnimation();
                }else{
                	
                }
                
                postInvalidate();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
        	if(mScroller.computeScrollOffset())
        		return true;
        	
        	mIsMovingPaper =false;
        	actionDownTime = System.currentTimeMillis();
            mIsTouchDown = true;
            mTouch.x = event.getX();
            mTouch.y = event.getY();
            if(mTouch.y < mHeight/3 || mTouch.y > mHeight*2/3){
            	mIsFlatTurning = false;
            }else{
            	mIsFlatTurning = true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
        	lastTouchX = (int)mTouch.x;
        	actionDownTime = System.currentTimeMillis();
            if (!isBookMarkScroll && !mIsMovingPaper && mIsTouchDown
                    && readFunction.isPageAnimationTurning()
                    && Math.abs(mTouch.x - event.getX()) > 50) {
            	
                mIsTouchDown = false;
                long time = System.currentTimeMillis();
                if (time - readFunction.getPageAnimationTime() < PAGE_ANIMATION_DURATION) {
                    return true;
                }
                mIsMovingNext = mTouch.x - event.getX() > 0;
                if (!mIsMovingNext && readFunction.isFirstPage(page)) {
                    return true;
                }
                if (mIsMovingNext && readFunction.isFinishPage(page)) {
                    readFunction.showBackCover();
                    return true;
                }
                //准备翻页的上下两个页面的bitmap
                readFunction.preparePageTurning(mIsMovingNext);
                hideAllImageViewOnAnimationStart();
                if(mIsFlatTurning){//平翻页面
            		mIsMovingPaper = true;
                	mTouch.x = event.getX();
                	mTouch.y = event.getY();
                    postInvalidate();
                    return true;
            	}else{//折角翻页
            		 //TODO 滑动开始仿真翻页动画
                    calcCornerXY(mTouch.x, mTouch.y);
                    if (mIsMovingNext) {
                        mIsMovingPaper = true;
                    } else {
                        if (mPageAnimation != null) {
                            gotoPrevPage();
                            return true;
                        }
                    }
            	}
            }
        }
        
        if (mIsMovingPaper) {
        	long time = System.currentTimeMillis();
            if (time - readFunction.getPageAnimationTime() < PAGE_ANIMATION_DURATION) {
                return true;
            }
            onPageTouchEvent(event);
        }
        //手势识别
        return detector.onTouchEvent(event);
    }
    
    public void toggleBookMarkAndShowMe() {
        toggleBookMark();
        showBookMark();
    }
    
    /**
     * 添加或者删除书签
     */
    private void toggleBookMark() {
        isAddBookMark = !isAddBookMark;
        if (isAddBookMark) {
            BookMark mark = createBookMark();
            int markId = MZBookDatabase.instance.addBookMark(mark);
            mark.id = markId;
            page.addBookMark(mark);
            ToastUtil.showToastInThread(ctx.getString(R.string.read_toast_add_bookmark), Toast.LENGTH_SHORT);
        } else {
            BookMark mark = page.getBookMark();
            if (mark != null) {
                MZBookDatabase.instance.deleteBookMarkByUpdate(mark.id);
            }
            page.removeBookMark();
            ToastUtil.showToastInThread(ctx.getString(R.string.read_toast_remove_bookmark), Toast.LENGTH_SHORT);
        }
    }
    
    public void refreshBookMark() {
        if (page != null && page.getChapter() != null) {
            isAddBookMark = false;
            page.setBookMark(null);
            isAddBookMark = page.getChapter().isBookMarkInPageScope(page);
            showBookMark();
        }
    }
    
    private void showBookMark() {
        bookmarkBglp.topMargin = -bookMarkMaxDistanceY;
        bookMarkBgLayout.setLayoutParams(bookmarkBglp);
        arrowlp.topMargin = -bookMarkActionPointY;
        arrowLayout.setLayoutParams(arrowlp);
        bookmarklp.topMargin = isAddBookMark?0:-bookMarkActionPointY;
        bookMarkLayout.setLayoutParams(bookmarklp);
        if (!mIsMovingPaper) {
            releasePageBitmap();
        }
        invalidate();
    }
    
    void refreshNoteIndicator() {
        if (noteBegin == null || noteEnd == null) {
            beginIndicator.setVisibility(View.INVISIBLE);
            endIndicator.setVisibility(View.INVISIBLE);
            return;
        }
        
        popupWindow.setPopShowing(true);
        popupWindow.showNoteActionBar();
        
        computeCoordinates(noteBegin, noteEnd);
        
        if (isBottomArrowShow) {
            popupWindow.showBottomArrow();
            popupWindow.showAtLocation(beginIndicator, Gravity.NO_GRAVITY, 0, (int) beginIndicatorY-popupWindow.getRootViewHeight());
        } else if (isTopArrowShow) {
            popupWindow.showTopArrow();
            popupWindow.showAtLocation(endIndicator, Gravity.NO_GRAVITY, 0, (int) endIndicatorY+endIndicator.getHeight());
        } else {
            popupWindow.hideArrow();
            popupWindow.showAtLocation(endIndicator, Gravity.NO_GRAVITY, 0, (screenHeight-popupWindow.getRootViewHeight())/2);
        }
    }
    
    private void computeArrowUpDown() {
        isTopArrowShow = false;
        isBottomArrowShow = false;
        if (beginIndicatorY >= NOTE_ACTION_BAR_MIN_HEIGHT * density) {
            isBottomArrowShow = true;
        } else if (screenHeight - endIndicatorY >= NOTE_ACTION_BAR_MIN_HEIGHT * density) {
            isTopArrowShow = true;
        }
    }
    
    private void computeCoordinates(Element noteBegin, Element noteEnd) {
        beginIndicatorX = noteBegin.getRect().left
                - beginIndicator.getWidth()
                + BookPageViewActivity.getPageMarginRight() * density;
        beginIndicatorY = noteBegin.getRect().top
                + BookPageViewActivity.PageMarginTop * density;
        endIndicatorX = noteEnd.getRect().right
                + BookPageViewActivity.getPageMarginRight() * density;
        endIndicatorY = noteEnd.getRect().top
                + BookPageViewActivity.PageMarginTop * density;

        beginIndicator.setX(beginIndicatorX);
        beginIndicator.setY(beginIndicatorY);
        endIndicator.setX(endIndicatorX);
        endIndicator.setY(endIndicatorY);
        
        computeArrowUpDown();
        
        
        if (beginIndicatorY == endIndicatorY) {
            popArrowX = (beginIndicatorX + endIndicatorX) / 2;
        } else {
            if (isTopArrowShow) {
                List<PageLine> lineList = page.getLineList();
                if (noteEndLineIndex >= 0 && lineList != null && lineList.size() > 0
                        && noteEndLineIndex < lineList.size()) {
                    PageLine line = page.getLineList().get(noteEndLineIndex);
                    Element element = line.elementList.get(0);
                    popArrowX = (endIndicatorX + element.getRect().left) / 2;
                } else {
                    popArrowX = (endIndicatorX + BookPageViewActivity
                            .getPageMarginLeft() * density) / 2;
                }
            } else if (isBottomArrowShow) {
                List<PageLine> lineList = page.getLineList();
                if (noteBeginLineIndex >= 0 && lineList != null && lineList.size() > 0
                        && noteBeginLineIndex < lineList.size()) {
                    PageLine line = page.getLineList().get(noteBeginLineIndex);
                    Element element = line.elementList.get(line.elementList.size() - 1);
                    popArrowX = (beginIndicatorX + beginIndicator.getWidth() + element
                            .getRect().right) / 2;
                } else {
                    popArrowX = (beginIndicatorX + screenWidth - BookPageViewActivity
                            .getPageMarginRight() * density) / 2;
                }
            } else {
                popArrowX = screenWidth / 2;
            }
        }
        float minX = 28 * density;
        float maxX = screenWidth - 50 * density;
        if (popArrowX < minX) {
            popArrowX = minX;
        } else if (popArrowX > maxX) {
            popArrowX = maxX;
        }
        popupWindow.setArrowX(popArrowX);
    }

    void refreshSelection(Element selectedElement) {
        int i = 0;
        int beginIndex = -1;
        int endIndex = -1;
        int selectedIndex = -1;
        for (PageLine line : page.getLineList()) {
            if (line.isImageFloat()) {
                continue;
            }
            for (Element element : line.elementList) {
                if (element == selectedElement) {
                    selectedIndex = i;
                }
                if (element == noteBegin) {
                    beginIndex = i;
                }
                if (element == noteEnd) {
                    endIndex = i;
                }
                ++i;
            }
        }

        if (noteState == NoteState.MOVE_BEGIN) {
            if (selectedIndex <= endIndex) {
                beginIndex = selectedIndex;
            } else {
                noteState = NoteState.MOVE_END;
                beginIndex = endIndex + 1;
                endIndex = selectedIndex;
            }
        }

        if (noteState == NoteState.MOVE_END) {
            if (selectedIndex >= beginIndex) {
                endIndex = selectedIndex;
            } else {
                noteState = NoteState.MOVE_BEGIN;
                endIndex = beginIndex - 1;
                beginIndex = selectedIndex;
            }
        }

        if (beginIndex == -1 || endIndex == -1) {
            return;
        }

        int newi = 0;
        for (PageLine line : page.getLineList()) {
            if (line.isImageFloat()) {
                continue;
            }
            for (Element element : line.elementList) {
                if (newi == beginIndex) {
                    noteBegin = element;
                    noteBeginLineIndex = page.getLineList().indexOf(line);
                }
                if (newi == endIndex) {
                    noteEnd = element;
                    noteEndLineIndex = page.getLineList().indexOf(line);
                }
                if (newi >= beginIndex && newi <= endIndex) {
                    element.setSelectionStatus(Element.SelectionStatus.SELECTION);
                } else {
                    element.setSelectionStatus(Element.SelectionStatus.UNSELECTION);
                }
                ++newi;
            }
        }

        popupWindow.setCreateNote(true);
        refreshNoteIndicator();

    }
    
    boolean isInSelectedRegion(float x, float y) {
        if (noteState != NoteState.END_NOTE && noteBegin != null && noteEnd != null) {
            if(null == noteBegin.getRect()) {
                return false;
            }
            if (y >= noteBegin.getRect().top && y <= noteEnd.getRect().bottom) {
                return true;
            }
        }
        return false;
    }
    
    boolean isBeginEndIndicator(float x, float y) {
        if (noteState != NoteState.END_NOTE) {
            int extraRegion = (int)(20 * density);
            if (y >= beginIndicator.getY()-extraRegion && y <= (beginIndicator.getY()+beginIndicator.getHeight()+extraRegion)) {
                if (x >= beginIndicator.getX()-extraRegion && x <= (beginIndicator.getX()+beginIndicator.getWidth()+extraRegion)) {
                    return true;
                }
            }
            if (y >= endIndicator.getY()-extraRegion && y <= (endIndicator.getY()+endIndicator.getHeight()+extraRegion)) {
                if (x >= endIndicator.getX()-extraRegion && x <= (endIndicator.getX()+endIndicator.getWidth()+extraRegion)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    boolean isMove(float x, float y) {
        if (x + 20 < longPressX || x - 20 > longPressX || y + 20 < longPressY
                || y - 20 > longPressY) {
            return true;
        }
        return false;
    }

    Element findSelectedElement(float x, float y) {

        x = x - BookPageViewActivity.getPageMarginLeft() * density;
        y = y - BookPageViewActivity.PageMarginTop * density;

        List<PageLine> lineList = page.getLineList();
        List<PageLine> selectedLineList = new ArrayList<PageLine>();
        float minDistanceY = Float.MAX_VALUE;
        PageLine selectedLine = null;
        for (int i = 0; i < lineList.size(); ++i) {
            PageLine line = lineList.get(i);
            if (line.isImageFloat()) {
                continue;
            }
            float posY = line.getPosY();
            if (posY < y + line.getLineHeight() / 2 && posY > y - line.getLineHeight() / 2) {
                selectedLineList.add(line);
            }
            
            float distanceY = line.distance(y);
            if (distanceY < minDistanceY) {
                minDistanceY = distanceY;
                selectedLine = line;
                continue;
            }
            if (selectedLine == null && i == lineList.size() - 1) {
                selectedLine = lineList.get(i);
            }
        }
        if (selectedLineList.size() <= 0) {
            return findElement(selectedLine, x);
        } else {
            double distance = 0f;
            double minDistance = Double.MAX_VALUE;
            Element selectedElement = null;
            for (PageLine line : selectedLineList) {
                Element element = findElement(line, x);
                if (element != null) {
                    distance = distanceTouchPoint(x, y, element);
                    if (distance < minDistance) {
                        minDistance = distance;
                        selectedElement = element;
                    }
                }
            }
            return selectedElement;
        }
    }
    
    private Element findElement(PageLine selectedLine, float x) {
        if (selectedLine == null || selectedLine.elementList.size() <= 0) {
            return null;
        }
        List<Element> elementList = selectedLine.elementList;
        Element first = elementList.get(0);
        Element last = elementList.get(elementList.size() - 1);
        
        if (x < first.getRect().right) {
            return first;
        }
        
        if (x >= last.getRect().left) {
            return last;
        }
        
        for (Element element : selectedLine.elementList) {
            RectF rect = element.getRect();
            if (x >= rect.left && x < rect.right) {
                return element;
            }
        }
        return null;
    }
    
    private double distanceTouchPoint(float x, float y, Element element) {
        RectF rect = element.getRect();
        float dx = Math.abs(x - (rect.left + rect.width() / 2));
        float dy = Math.abs(y - (rect.top + rect.height() / 2));
        double dxy = Math.pow(dx, 2) + Math.pow(dy, 2);
        return Math.sqrt(dxy);

    }
    
    ReadPopWindow createPopupWindow() {
        ReadPopWindow popupWindow = new ReadPopWindow(ctx, this);
        popupWindow.setWidth(screenWidth);
        popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(ctx
                .getResources(), (Bitmap) null));
        
        return popupWindow;
    }

    void showActionBarForCreatNote() {
        beginIndicator.setVisibility(View.VISIBLE);
        endIndicator.setVisibility(View.VISIBLE);
        popupWindow.setCreateNote(true);

        refreshNoteIndicator();
    }

    void showActionBarForEditNote(ReadNote note) {
        beginIndicator.setVisibility(View.INVISIBLE);
        endIndicator.setVisibility(View.INVISIBLE);
        popupWindow.setCreateNote(false);
        popupWindow.setPrivateNote(note.isPrivate);
        modifyReadNote = note;

        refreshNoteIndicator();
    }
    
    private void showAnnotation(String annotation, int popArrowX, int popArrowY) {
        isNoteContentShowing = true;
        ReadPopWindow popWindow = createPopupWindow();
        popWindow.showDictionaryResult(null,annotation,true);
        beginIndicatorY = popArrowY;
        endIndicatorY = popArrowY;
        computeArrowUpDown();
        float minX = 28 * density;
        float maxX = screenWidth - 50 * density;
        if (popArrowX < minX) {
            popArrowX = (int) minX;
        } else if (popArrowX > maxX) {
            popArrowX = (int) maxX;
        }
        popWindow.setArrowX(popArrowX);
        if (isBottomArrowShow) {
            popWindow.showBottomArrow();;
            popWindow.showAtLocation(beginIndicator, Gravity.NO_GRAVITY, 0,
                    (int) beginIndicatorY - popWindow.getRootViewHeight());
        } else if (isTopArrowShow) {
            popWindow.showTopArrow();
            popWindow.showAtLocation(endIndicator, Gravity.NO_GRAVITY, 0,
                    (int) endIndicatorY + endIndicator.getHeight());
        } else {
            popWindow.hideArrow();
            popWindow.showAtLocation(endIndicator, Gravity.NO_GRAVITY, 0,
                    (screenHeight - popWindow.getRootViewHeight()) / 2);
        }
    }
    
    ReadPopWindow dictionaryPop;
    public void showDictionaryResult(String word,String result,boolean isDictionaryReady,boolean haveShowDictionary) {
    	if(dictionaryPop !=null && dictionaryPop.isShowing()){
    		dictionaryPop.dismiss();
    		dictionaryPop=null;
    	}else{
    		if(haveShowDictionary)
    			return ;
    	}
    	dictionaryPop = createPopupWindow();
        dictionaryPop.showDictionaryResult(word,result,isDictionaryReady);
        dictionaryPop.setArrowX(popupWindow.getArrowX());
        if (isBottomArrowShow) {
            dictionaryPop.showBottomArrow();
            dictionaryPop.showAtLocation(beginIndicator, Gravity.NO_GRAVITY, 0,
                    (int) beginIndicatorY - dictionaryPop.getRootViewHeight());
        } else if (isTopArrowShow) {
            dictionaryPop.showTopArrow();
            dictionaryPop.showAtLocation(endIndicator, Gravity.NO_GRAVITY, 0,
                    (int) endIndicatorY + endIndicator.getHeight());
        } else {
            dictionaryPop.hideArrow();
            dictionaryPop.showAtLocation(endIndicator, Gravity.NO_GRAVITY, 0,
                    (screenHeight - dictionaryPop.getRootViewHeight()) / 2);
        }
    }
    
    void showNoteContent(ReadNote note) {
        MZLog.e("J", "showNoteContent>>>>note:"+note.toString());
        List<Element> elementList = this.page.getNoteMap().get(note);
        if (elementList == null || elementList.size() == 0) {
            return;
        }
        modifyReadNote = note;
        isNoteContentShowing = true;
        Element noteBegin = elementList.get(0);
        Element noteEnd = elementList.get(elementList.size() - 1);
        ReadPopWindow notePop = createPopupWindow();
        if (note.userId.equals(BookPageViewActivity.getUserId())) {
            notePop.showMyNoteContent(note);
        } else {
            NotesModel model = readFunction.findMatchNoteUser(note.userId);
            notePop.showPeopleNoteContent(note, model);
        }

        computeCoordinates(noteBegin, noteEnd);
        notePop.setArrowX(popArrowX);
        if (isBottomArrowShow) {
            notePop.showBottomArrow();
            notePop.showAtLocation(beginIndicator, Gravity.NO_GRAVITY, 0,
                    (int) beginIndicatorY - notePop.getRootViewHeight());
        } else if (isTopArrowShow) {
            notePop.showTopArrow();
            notePop.showAtLocation(endIndicator, Gravity.NO_GRAVITY, 0,
                    (int) endIndicatorY + endIndicator.getHeight());
        } else {
            notePop.hideArrow();
            notePop.showAtLocation(endIndicator, Gravity.NO_GRAVITY, 0,
                    (screenHeight - notePop.getRootViewHeight()) / 2);
        }
    }
    
    void showPeopleNotesContent(List<ReadNote> noteList) {
        peopleNotes.clear();
        peopleNotes.addAll(noteList);
        isNoteContentShowing = true;
        ReadNote beginNote = findBeginNote(noteList);
        List<Element> elementList = this.page.getNoteMap().get(beginNote);
        Element noteBegin = elementList.get(0);
        ReadNote endNote = findEndNote(noteList);
        elementList = this.page.getNoteMap().get(endNote);
        Element noteEnd = elementList.get(elementList.size() - 1);
        
        ReadPopWindow notePop = createPopupWindow();
        notePop.showPeopleNoteContent(noteList);

        computeCoordinates(noteBegin, noteEnd);
        notePop.setArrowX(popArrowX);
        if (isBottomArrowShow) {
            notePop.showBottomArrow();
            notePop.showAtLocation(beginIndicator, Gravity.NO_GRAVITY, 0,
                    (int) beginIndicatorY - notePop.getRootViewHeight());
        } else if (isTopArrowShow) {
            notePop.showTopArrow();
            notePop.showAtLocation(endIndicator, Gravity.NO_GRAVITY, 0,
                    (int) endIndicatorY + endIndicator.getHeight());
        } else {
            notePop.hideArrow();
            notePop.showAtLocation(endIndicator, Gravity.NO_GRAVITY, 0,
                    (screenHeight - notePop.getRootViewHeight()) / 2);
        }
    }
    
    private ReadNote findBeginNote(List<ReadNote> noteList) {
        ReadNote beginNote = noteList.get(0);
        for (ReadNote note : noteList) {
            if (note.fromParaIndex < beginNote.fromParaIndex
                    || (note.fromParaIndex == beginNote.fromParaIndex && note.fromOffsetInPara < beginNote.fromOffsetInPara)) {
                beginNote = note;
            }
        }
        return beginNote;
    }
    
    private ReadNote findEndNote(List<ReadNote> noteList) {
        ReadNote endNote = noteList.get(0);
        for (ReadNote note : noteList) {
            if (note.toParaIndex > endNote.toParaIndex
                    || (note.toParaIndex == endNote.toParaIndex && note.toOffsetInPara > endNote.toOffsetInPara)) {
                endNote = note;
            }
        }
        return endNote;
    }
    
    private List<ReadNote> findSelectedReadNotes(float x, float y) {
        if (page == null) {
            return null;
        }
        List<PageLine> lineList = page.getLineList();
        if (lineList.size() > 0) {
            PageLine lastLine = lineList.get(lineList.size() - 1);
            if ((y - BookPageViewActivity.PageMarginTop * density) > lastLine.rect.bottom) {
                return null;
            }
        }

        Map<ReadNote, List<Element>> noteMap = page.getNoteMap();

        if (noteMap == null || noteMap.size() == 0) {
            return null;
        }

        List<ReadNote> readNoteList = new ArrayList<ReadNote>();
        Element element = findSelectedElement(x, y);

        for (Map.Entry<ReadNote, List<Element>> entry : noteMap.entrySet()) {
            for (Element e : entry.getValue()) {
                if (e == element) {
                    ReadNote note = entry.getKey();
                    if (note.userId.equals(BookPageViewActivity.getUserId())) {
                        List<Element> elementList = entry.getValue();
                        noteBegin = elementList.get(0);
                        noteEnd = elementList.get(elementList.size() - 1);
                    }
                    readNoteList.add(note);
                    continue;
                }
            }
        }
        return readNoteList;

    }
    
    private final int FLING_MIN_DISTANCE = 20;
    private final int FLING_MIN_VELOCITY = 30;

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private boolean isFirstScroll = true;
        
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
        	Log.e(tag, "action onScroll");
        	actionDownTime = System.currentTimeMillis();
        	
            if (readFunction.isShowSearchBar()) {
                return true;
            }
            if (noteState == NoteState.END_NOTE) {
                if (isCancelNote || page == null || page instanceof CoverPage || page instanceof FinishPage) {
                    return super.onScroll(e1, e2, distanceX, distanceY);
                }
                if (!isFirstScroll && !isBookMarkScroll) {
                    return super.onScroll(e1, e2, distanceX, distanceY);
                }

                if (isFirstScroll) {
                    isFirstScroll = false;
                    isBookMarkScroll = Math.abs(distanceY / 3) > 1 && Math.abs(distanceX) < Math.abs(distanceY / 3);;
                }
                if (isBookMarkScroll) {
                    int y = (int) (-distanceY / 2);
                    
                    int top = bookMarkBgLayout.getTop() + y;
                    if (top > 0) {
                        top = 0;
                    } else if( top < -bookMarkMaxDistanceY) {
                        top = -bookMarkMaxDistanceY;
                    }
                    bookmarkBglp.topMargin = top;
                    bookMarkBgLayout.setLayoutParams(bookmarkBglp);

                    
                    top = arrowLayout.getTop() + y;
                    if (top > 0) {
                        top = 0;
                    } else if( top < -bookMarkActionPointY) {
                        top = -bookMarkActionPointY;
                    }
                    arrowlp.topMargin = top;
                    arrowLayout.setLayoutParams(arrowlp);
                    
                    if (!isAddBookMark) {
                        bookmarklp.topMargin = top;
                        bookMarkLayout.setLayoutParams(bookmarklp);
                    }
                    
                    scrollDeltaY = arrowLayout.getBottom();

                    if (scrollDeltaY < bookMarkActionPointY) {
                        if (isArrowUp) {
                            isArrowUp = false;
                            arrowImage.startAnimation(arrowRotateDown);
                            arrowText.setText(isAddBookMark ? R.string.read_remove_bookmark_1
                                            : R.string.read_add_bookmark_1);
                        }
                    } else {
                        if (!isArrowUp) {
                            isArrowUp = true;
                            arrowImage.startAnimation(arrowRotateUp);
                            arrowText.setText(isAddBookMark ? R.string.read_remove_bookmark_2
                                            : R.string.read_add_bookmark_2);
                        }
                    }

                    return true;
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            // 参数解释：   
            // e1：第1个ACTION_DOWN MotionEvent   
            // e2：最后一个ACTION_MOVE MotionEvent   
            // velocityX：X轴上的移动速度，像素/秒   
            // velocityY：Y轴上的移动速度，像素/秒   
        	Log.e(tag, "action onFling");
        	actionDownTime = System.currentTimeMillis();
            if (hidePopOver()) {
                return true;
            }
            if (readFunction.isShowSearchBar()) {
                return true;
            }
            if (noteState == NoteState.END_NOTE) {
                if (isBookMarkScroll || isCancelNote || readFunction.isPageAnimationTurning()) {
                    return true;
                }
                // 触发条件 ：
                // X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒
                if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
                        && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                    // Fling left
                    readFunction.nextPage(true);
                } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
                        && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                    // Fling right
                    readFunction.prevPage(true);
                }
            }
            return true; 
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
            if (readFunction.isShowSearchBar()) {
                return;
            }
            Log.e(tag, "action longpress");
            Log.e(tag, "action longpress eventtype:"+e.getAction());
//            long currentTime = System.currentTimeMillis();
//            if(currentTime - actionDownTime < 500)
//            	return ;
            
            if (noteState == NoteState.END_NOTE) {
                longPressX = e.getX();
                longPressY = e.getY();
                Element element = findSelectedElement(e.getX(), e.getY());
                if (element != null) {
                    isLongPressSelectState = true;
                    noteBegin = element;
                    noteEnd = element;
                    noteBegin.setSelectionStatus(Element.SelectionStatus.SELECTION);
                    if (element instanceof ElementText) {
                        if (((ElementText) element).isHyphenated()) {
                            noteEnd = findNextElementText((ElementText) element);
                            noteEnd.setSelectionStatus(Element.SelectionStatus.SELECTION);
                        }
                    }
                    noteBeginLineIndex = -1;
                    noteEndLineIndex = -1;
                    computeCoordinates(noteBegin, noteEnd);
                    beginIndicator.setVisibility(View.VISIBLE);
                    endIndicator.setVisibility(View.VISIBLE);
                    readFunction.beginNoteSelection();
                    invalidate();
                    noteState = NoteState.MOVE_END;
                }
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
        	Log.e(tag, "action onSingleTapConfirmed");
        	actionDownTime = System.currentTimeMillis();
            if (hidePopOver()) {
                return true;
            }
            if (readFunction.isShowSearchBar()) {
                return true;
            }
            if (noteState == NoteState.END_NOTE) {
                if (hideNoteActionBar()) {
                    return true;
                }
                List<ReadNote> noteList = findSelectedReadNotes(e.getX(), e.getY());
                if (noteList != null && noteList.size() > 0) {
                    boolean isPeopleNotes = true;
                    for (ReadNote note:noteList){
                        if (note.userId.equals(BookPageViewActivity.getUserId())) {
                            showActionBarForEditNote(note);
                            isPeopleNotes = false;
                            break;
                        }
                    }
                    if (isPeopleNotes) {
                        showPeopleNotesContent(noteList);
                    }
                } else {
                    if (readFunction.isPageAnimationTurning()) {
                        long time = System.currentTimeMillis();
                        if (time - readFunction.getPageAnimationTime() < PAGE_ANIMATION_DURATION) {
                            return true;
                        }
                        if (e.getX() < BookPageViewActivity.leftClickX) {
                            mIsMovingNext = false;
                        } else if (e.getX() > BookPageViewActivity.rightClickX) {
                            mIsMovingNext = true;
                        } else {
                            if (e.getY() < BookPageViewActivity.topClickY) {
                                mIsMovingNext = false;
                            } else if (e.getY() > BookPageViewActivity.bottomClickY) {
                                mIsMovingNext = true;
                            } else {
                                readFunction.openSetting();
                                return true;
                            }
                        }
                        if (!mIsMovingNext && readFunction.isFirstPage(page)) {
                            return true;
                        }
                        if (mIsMovingNext && readFunction.isFinishPage(page)) {
                            readFunction.showBackCover();
                            return true;
                        }
                        
//                        readFunction.preparePageTurning(mIsMovingNext);
//                        hideAllImageViewOnAnimationStart();
//                        int dx,dy;
//                        dy = 0;
//                        if (!mIsMovingNext) {//向右滑动
//                            dx = mWidth + 100;
//                        }else{ //向左滑动
//                            dx = -mWidth -100;
//                        }
//                        mScroller.startScroll((int)mWidth-100,(int)mTouch.y,dx,dy, Kit42View.PAGE_ANIMATION_DURATION);
//                        if( mIsMovingNext){
//                        	mIsMovingPaper = true;
//                        	readFunction.forceGotoNextPageInAnimation();
//                        }else if(!mIsMovingNext){
//                        	readFunction.forceGotoPrevPageInAnimation();
//                        }
//                        postInvalidate();
                        
                        
                        //TODO 点击开始仿真翻页动画
                        readFunction.preparePageTurning(mIsMovingNext);
                        hideAllImageViewOnAnimationStart();
                        mTouch.x = screenWidth - 1;
                        mTouch.y = screenHeight - 1;
                        calcCornerXY(mTouch.x, mTouch.y);
                        if (mIsMovingNext) {
                            mIsMovingPaper = true;
//                            gotoNextPage();
                            startAnimation();
                        } else {
                            gotoPrevPage();
                        }
                    } else {
                        if (e.getX() < BookPageViewActivity.leftClickX) {
                            readFunction.prevPage(true);
                        } else if (e.getX() > BookPageViewActivity.rightClickX) {
                            readFunction.nextPage(true);
                        } else {
                            if (e.getY() < BookPageViewActivity.topClickY) {
                                readFunction.prevPage(true);
                            } else if (e.getY() > BookPageViewActivity.bottomClickY) {
                                readFunction.nextPage(true);
                            } else {
                                readFunction.openSetting();
                            }
                        }
                    }
                }
            }
            return true;
        }
        
        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            if (readFunction.isPageAnimationTurning()) {
//                return true;
//            }else{
//            	onSingleTapConfirmed(e);
//            }
        	Log.e(tag, "action onDoubleTap");
            return this.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
        	Log.e(tag, "action onDown");
            arrowImage.clearAnimation();
            arrowText.setText(isAddBookMark ? R.string.read_remove_bookmark_1
                    : R.string.read_add_bookmark_1);
            isFirstScroll = true;
            isBookMarkScroll = false;
            isArrowUp = false;
            isCancelNote = false;
            scrollDeltaY = 0;
            return true;
        }
        
        @Override
        public void onShowPress(MotionEvent e) {
        	Log.e(tag, "action onShowPress");
        	super.onShowPress(e);
        }
    }
    
    /**
     * 设置选中背景
     */
    public void setSelection(List<Element> preElementlist,List<Element> elementlist){
        if (elementlist != null) {
            Element element;
            if (preElementlist!=null) {
                for (int i = 0; i < preElementlist.size(); i++) {
                    element=preElementlist.get(i);
                    element.setSelectionStatus(Element.SelectionStatus.UNSELECTION);
                }
            }
            for (int i = 0; i < elementlist.size(); i++) {
                element=elementlist.get(i);
                element.setSelectionStatus(Element.SelectionStatus.SELECTION);
            }
            invalidate();
        }
    }

    private void copyText(String text) {
        ClipboardManager clipboard = (ClipboardManager) getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("mzread note", text);
        clipboard.setPrimaryClip(clip);
    }

    private String getQuote() {
        int beginIndex = Integer.MAX_VALUE;
        int endIndex = Integer.MAX_VALUE;
        int index = 0;
        StringBuilder sb = new StringBuilder();
        int paraIndex = 0;
        for (PageLine line : page.getLineList()) {
            for (Element element : line.elementList) {
                element.setSelectionStatus(Element.SelectionStatus.UNSELECTION);
                if (element == noteBegin) {
                    beginIndex = index;
                    paraIndex = element.paraIndex;
                }
                if (element == noteEnd) {
                    endIndex = index;
                }
                if (index >= beginIndex && index <= endIndex) {
                    if (element.paraIndex != paraIndex) {
                        sb.append('\n');
                        paraIndex = element.paraIndex;
                    }
                    if (element instanceof ElementText) {
                        if (((ElementText) element).isHyphenated()
                                && index < endIndex) {
                            continue;
                        }
                    }
                    sb.append(element.getContent());
                }

                ++index;
            }
        }
        return sb.toString();
    }
    
    private BookMark createBookMark() {
        BookMark mark = readFunction.createEmptyMark();
        Chapter chapter = page.getChapter();
        mark.para_index = page.startParaIndex;
        mark.offset_in_para = page.startOffset;
        mark.digest = page.getDigest();
        mark.updated_at =System.currentTimeMillis()/1000;
        mark.chapter_title = page.getPageHead();
        mark.chapter_itemref = chapter.getSpine().spineIdRef;
        return mark;
    }
    
    private String getQuoteAndLineElement() {
        int beginIndex = Integer.MAX_VALUE;
        int endIndex = Integer.MAX_VALUE;
        int index = 0;
        StringBuilder sb = new StringBuilder();
        int paraIndex = 0;
        for (PageLine line : page.getLineList()) {
            for (Element element : line.elementList) {
                element.setSelectionStatus(Element.SelectionStatus.UNSELECTION);
                if (element == noteBegin) {
                    beginIndex = index;
                    paraIndex = element.paraIndex;
                }
                if (element == noteEnd) {
                    endIndex = index;
                }
                if (index >= beginIndex && index <= endIndex) {
                    element.setNoteStatus(Element.NoteStatus.NOTE);
                    if (element.paraIndex != paraIndex) {
                        sb.append('\n');
                        paraIndex = element.paraIndex;
                    }
                    if (element instanceof ElementText) {
                        if (((ElementText) element).isHyphenated()
                                && index < endIndex) {
                            continue;
                        }
                    }
                    sb.append(element.getContent());
                }

                ++index;
            }
        }
        return sb.toString();
    }
    
    private void lineNote(boolean isHideNoteActionBar) {
        lineDigestNote(true, isHideNoteActionBar);
    }

    private void lineDigestNote(boolean isPrivate, boolean isHideNoteActionBar) {
        popupWindow.setCreateNote(false);
        popupWindow.setPrivateNote(isPrivate);
        noteState = NoteState.END_NOTE;
        String quote = getQuoteAndLineElement();
        ReadNote note = readFunction.createEmptyNote();

        Chapter chapter = page.getChapter();
        note.chapterName = page.getPageHead();
        note.spineIdRef = chapter.getSpine().spineIdRef;
        note.quoteText = quote;
        note.contentText = "";
        note.updateTime = System.currentTimeMillis();
        note.fromParaIndex = noteBegin.paraIndex;
        note.fromOffsetInPara = noteBegin.offsetInPara;
        note.toParaIndex = noteEnd.paraIndex;
        note.toOffsetInPara = noteEnd.offsetInPara + noteEnd.getCount();
        note.isPrivate = isPrivate;
        chapter.addReadNote(note);
        MZLog.d("Kit42View", note.quoteText);
        modifyReadNote = note;
        noteBegin = null;
        noteEnd = null;
        noteBeginLineIndex = -1;
        noteEndLineIndex = -1;
        beginIndicator.setVisibility(View.INVISIBLE);
        endIndicator.setVisibility(View.INVISIBLE);
        readFunction.endNoteSelection();
        if (isHideNoteActionBar) {
            hideNoteActionBar();
        }
        refreshNote();
        invalidate();

    }

    @Override
    public void onNoteCopy() {
        if (popupWindow.isCreateNote()) {
            String noteText = getQuote();
            if (!TextUtils.isEmpty(noteText)) {
                copyText(noteText);
            }
            endNoteSelection();
        } else {
            hideNoteActionBar();
            if (!TextUtils.isEmpty(modifyReadNote.quoteText)) {
                copyText(modifyReadNote.quoteText);
            }
        }
    }

    @Override
    public void onDigestCreate(boolean isHideNoteActionBar) {
        if (TextUtils.isEmpty(BookPageViewActivity.getUserId())) {
            ToastUtil.showToastInThread(R.string.read_note_need_login);
            Intent it = new Intent(ctx, LoginActivity.class);
            ctx.startActivity(it);
            return;
        }
        lineNote(isHideNoteActionBar);
    }

    @Override
    public void onNoteCreate() {
        if (TextUtils.isEmpty(BookPageViewActivity.getUserId())) {
            ToastUtil.showToastInThread(R.string.read_note_need_login);
            Intent it = new Intent(ctx, LoginActivity.class);
            ctx.startActivity(it);
            return;
        }
        String quote = getQuoteAndLineElement();
        readFunction.asyncRequestCreateNote(quote, Kit42View.this);
    }

    @Override
    public void onNoteModify() {
        readFunction.asyncRequestModifyNote(modifyReadNote, Kit42View.this);
    }

    @Override
    public void onNoteDictionary() {
        String quote = getQuote();
        readFunction.asyncRequestTranslate(quote, Kit42View.this);
        hideNoteActionBar();
    }

    @Override
    public void onNoteDestory() {
        hideNoteActionBar();
        modifyReadNote.deleted = true;
        modifyReadNote.modified = true;
        modifyReadNote.updateTime = System.currentTimeMillis();
        readFunction.refreshNotes();
    }
    
    @Override
    public void onNoteBaike() {
//        String words = getQuote();
//        try {
//            words=URLEncoder.encode(words, WebRequestHelper.CHAR_SET);
//        } catch (UnsupportedEncodingException e1) {
//            e1.printStackTrace();
//        }
//        String urlText = String.format(URLText.getBaikeWords, words);
//        Intent intent = new Intent(getContext(), WebViewActivity.class);
//        intent.putExtra(WebViewActivity.UrlKey, urlText);
//        getContext().startActivity(intent);
    }
    
    @Override
    public void onNoteShareCommunity() {
        if (popupWindow.isCreateNote()) {
            lineDigestNote(false, true);
            Toast.makeText(ctx, R.string.share_success, Toast.LENGTH_SHORT).show();
        } else {
            hideNoteActionBar();
            modifyReadNote.isPrivate = !modifyReadNote.isPrivate;
            modifyReadNote.modified = true;
            modifyReadNote.updateTime = System.currentTimeMillis();
            Toast.makeText(ctx, modifyReadNote.isPrivate ? R.string.cancel_share_success:R.string.share_success, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNoteShareSinaWeibo() {
        if (!LoginUser.isLogin()) {
            ToastUtil.showToastInThread(R.string.login_first);
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(ctx)) {
            ToastUtil.showToastInThread(R.string.network_not_find);
            return;
        }
        hideNoteActionBar();
        readFunction.showLoading();
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                readFunction.shareReadNoteViaSinaWeibo(modifyReadNote, noteScreenshot());
            }
        }).start();
    }

    @Override
    public void onNoteShareWeChat(final int type) {
        if (!LoginUser.isLogin()) {
            ToastUtil.showToastInThread(R.string.login_first);
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(ctx)) {
            ToastUtil.showToastInThread(R.string.network_not_find);
            return;
        }
        hideNoteActionBar();
        readFunction.showLoading();
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                Bitmap noteBitmap = null;
                if (page != null && page.getChapter().geteBookId() <= 0) {
                    MZLog.d(VIEW_LOG_TAG, ">>>>>>>>noteBitmap:"+noteBitmap);
                    noteBitmap = noteScreenshot();
                }
                readFunction.shareReadNoteViaWeixin(modifyReadNote, noteBitmap,type);
            }
        }).start();
    }
    
//  @Override
//  public void onNoteShareWXFriends() {
//      if (!LoginUser.isLogin()) {
//          ToastUtil.showToastInThread(R.string.login_first);
//          return;
//      }
//      if (!NetWorkUtils.isNetworkConnected(ctx)) {
//          ToastUtil.showToastInThread(R.string.network_not_find);
//          return;
//      }
//      hideNoteActionBar();
//      readFunction.showLoading();
//      new Thread(new Runnable() {
//          
//          @Override
//          public void run() {
//              readFunction.shareReadNoteViaWeixin(modifyReadNote, noteScreenshot());
//          }
//      }).start();
//      
//  }

    @Override
    public void onNoteComment(String guid, long noteId) {
        readFunction.commentNote(guid, noteId);
    }

    @Override
    public NotesModel findUserNotesModel(String userId) {
        return readFunction.findMatchNoteUser(userId);
    }

    @Override
    public void refreshUserNotesModel() {
        readFunction.requestAllNotesModel();
    }

    private Bitmap noteScreenshot() {
        Kit42View view = new Kit42View(ctx, readFunction, true);
        Page p = new Page();
        view.sharePaint = createSharePaint();
        Chapter chapter = page.getChapter().getClone();
        chapter.setBaseTextSize(view.sharePaint.getTextSize());
        chapter.setGlobalPaint(view.sharePaint);
        chapter.buildBlock();
        p.initialize(page.getPageContext(), chapter);
        p.setReadNoteShare(true);
        p.setStart(modifyReadNote.fromParaIndex, modifyReadNote.fromOffsetInPara);
        p.setEnd(modifyReadNote.toParaIndex, modifyReadNote.toOffsetInPara);
        if (!TextUtils.isEmpty(modifyReadNote.contentText)) {
            view.shareNoteContentList = buildShareNoteContentLines(
                    view.sharePaint, modifyReadNote.contentText);
            view.shareContentHeight += view.shareNoteContentList.size()
                    * (view.sharePaint.descent() - view.sharePaint.ascent());
            view.shareContentHeight += view.shareNoteContentList.size()
                    * BookPageViewActivity.PageLineSpace
                    * view.sharePaint.getTextSize();
        }
        p.buildPageContent();
        view.setPage(p);
        view.shareTopHeight = (int) (screenWidth / 6.4);//6.4是图片素材的宽高比
        view.shareReadNoteHeight = p.readNoteHeight
                + (int) (view.sharePaint.getTextSize() * 2)
                + view.shareTopHeight + view.shareContentHeight;
        int topResId = view.shareNoteContentList != null
                && view.shareNoteContentList.size() > 0 ? R.drawable.share_top_b95_android
                : R.drawable.share_top_b100_android;
        Bitmap bitmap = ImageUtils.getBitmapFromResource(ctx, topResId,
                screenWidth, view.shareTopHeight);
        int width = bitmap.getWidth();
        if (width != screenWidth) {
            view.shareTopSummaryBitmap = scaleBitmap(bitmap, screenWidth,
                    view.shareTopHeight);
            bitmap.recycle();
        } else {
            view.shareTopSummaryBitmap = bitmap;
        }
        bitmap = null;
        
        view.shareBottomHeight = (int) (screenWidth / 2.46);//2.46是图片素材的宽高比
        bitmap = ImageUtils.getBitmapFromResource(ctx,
                R.drawable.share_bottom_android, screenWidth,
                view.shareBottomHeight);
        view.shareReadNoteHeight += view.shareBottomHeight;
        width = bitmap.getWidth();
        if (width != screenWidth) {
            view.shareBottomBitmap = scaleBitmap(bitmap, screenWidth,
                    view.shareTopHeight);
            bitmap.recycle();
        } else {
            view.shareBottomBitmap = bitmap;
        }
        bitmap = null;
        
        String coverPath = null;
        long ebookId = page.getChapter().geteBookId();
        int docId = page.getChapter().getDocId();
        if (ebookId > 0) {
            LocalBook book = LocalBook.getLocalBook(ebookId,LoginUser.getpin());
            coverPath = book.bigImageUrl;
            view.shareBookName = book.title;
            view.shareAuthor = book.author;
        } else if (docId > 0) {
            Document document = MZBookDatabase.instance.getDocument(docId);
            coverPath = document.coverPath;
            view.shareBookName = document.title;
            view.shareAuthor = document.author;
        }
        boolean showBookCover = !TextUtils.isEmpty(coverPath);
        if (showBookCover) {
            int w = (int) (60 * density);
            int h = (int) (80 * density);
            if (coverPath.startsWith("http://")
                    || coverPath.startsWith("https://")) {
                bitmap = ImageLoader.getInstance().loadImageSync(coverPath,
                        new ImageSize(w, h));
                bitmap = CutBitmapDisplayer.CropForExtraWidth(bitmap, true);
            } else {
                bitmap = ImageUtils.getBitmapFromNamePath(coverPath, w, h);
            }
            if (bitmap != null) {
                view.shareCoverHeight = (int) (112 * density);// 封面书名作者信息高度
                view.shareReadNoteHeight += view.shareCoverHeight;
                int height = bitmap.getHeight();
                if (height != h) {
                    view.shareCoverBitmap = scaleBitmap(bitmap, w, h);
                    bitmap.recycle();
                } else {
                    view.shareCoverBitmap = bitmap;
                }
                view.shareBookName = buildBookCoverLine(view.sharePaint,
                        view.shareBookName, view.shareCoverBitmap.getWidth());
                view.shareAuthor = buildBookCoverLine(view.sharePaint,
                        view.shareAuthor, view.shareCoverBitmap.getWidth());
                bitmap = null;
            }
        }
        
        Bitmap screenshot = Bitmap.createBitmap(screenWidth, view.shareReadNoteHeight,
                Bitmap.Config.RGB_565);
        view.draw(new Canvas(screenshot));
        view.releaseShare();
        
////////test code start///////////
//      String filename = "/storage/sdcard0/temp.png";
//      if (screenshot != null) {
//          try {
//              FileOutputStream out = new FileOutputStream(filename);
//              screenshot.compress(Bitmap.CompressFormat.PNG, 100, out);
//              out.close();
//          } catch (Exception e) {
//              e.printStackTrace();
//          }
//      }
//      Intent intent=new Intent(ctx,BookPageImageEnlargeActivity.class);
//      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//      intent.putExtra("imageResource", filename);
//      intent.putExtra("imageWidth", screenWidth);
//      intent.putExtra("imageHeight", p.readNoteHeight);
//      ctx.startActivity(intent);
////////test code end///////////
        return screenshot;
    }
    
    private Bitmap scaleBitmap(Bitmap bitmap, int w, int h) {
        // 获得图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 计算缩放比例
        float scaleWidth = w*1.0f / width;
        float scaleHeight = h*1.0f / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        if (scaleWidth >= scaleHeight) {
            matrix.postScale(scaleWidth, scaleWidth);
        } else {
            matrix.postScale(scaleHeight, scaleHeight);
        }
        // 得到新的图片
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
                true);
    }
    
    private List<String> buildShareNoteContentLines(Paint paint, String text) {
        List<String> textList = new ArrayList<String>();
        float[] widths = new float[text.length()];
        paint.getTextWidths(text, widths);
        float pageWidth = page.getChapter().getPageWidth();
        float lineWidth = 0;
        for (int i = 0, start = 0, end = 0; i < widths.length; i++) {
            lineWidth += widths[i];
            if (lineWidth > pageWidth) {
                lineWidth = 0;
                end = --i;
                textList.add(text.substring(start, end));
                start = end;
            }
            if (i == widths.length - 1) {
                textList.add(text.substring(start));
            }
        }
        return textList;
    }
    
    private String buildBookCoverLine(Paint paint, String text, int coverWidth) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        float[] widths = new float[text.length()];
        paint.getTextWidths(text, widths);
        //屏幕宽－封面图宽－左右padding－书名与图片的间距
        float pageWidth = screenWidth - coverWidth - 2*16*density - 8*density;
        float lineWidth = 0;
        for (int i = 0, start = 0, end = 0; i < widths.length; i++) {
            lineWidth += widths[i];
            if (lineWidth > pageWidth) {
                lineWidth = 0;
                end = --i;
                return text.substring(start, end)+"...";
                //start = end;// 不考虑换行
            }
            if (i == widths.length - 1) {
                return text.substring(start);
            }
        }
        return null;
    }
    
    private Paint createSharePaint() {
        Paint paint = new Paint();
        paint.setTextSize(18 * density);//18*density是中等字体大小
        paint.setTypeface(Typeface.DEFAULT);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        return paint;
    }

    /**
     * 屏幕宽度
     */
    private int mWidth = 0;
    /**
     * 屏幕高度
     */
    private int mHeight = 0;
    private int mCornerX = 0;       // 拖拽点对应的页脚
    private int mCornerY = 0;
    private Path mPath0;
    private Path mPath1;
    Bitmap mCurPageBitmap = null;               // 当前页
    Bitmap mNextPageBitmap = null;
    Bitmap mPrevPageBitmap = null;

    PointF mTouch = new PointF(-1,-1);                       // 拖拽点
    PointF mBezierStart1 = new PointF();            // 贝塞尔曲线起始点
    PointF mBezierControl1 = new PointF();      // 贝塞尔曲线控制点
    PointF mBeziervertex1 = new PointF();           // 贝塞尔曲线顶点
    PointF mBezierEnd1 = new PointF();              // 贝塞尔曲线结束点

    PointF mBezierStart2 = new PointF();            // 另一条贝塞尔曲线
    PointF mBezierControl2 = new PointF();
    PointF mBeziervertex2 = new PointF();
    PointF mBezierEnd2 = new PointF();

    float mMiddleX;
    float mMiddleY;
    float mDegrees;
    float mTouchToCornerDis;
    ColorMatrixColorFilter mColorMatrixFilter;
    Matrix mMatrix;
    float[] mMatrixArray = { 0, 0, 0, 0, 0, 0, 0, 0, 1.0f };

    boolean mIsRTandLB;                 // 是否属于右上左下
    boolean mIsMovingPaper = false;
    boolean mIsMovingNext = false;
    boolean mIsTouchDown = false;
    // for test
    float mMaxLength = 0f;
    int[] mBackShadowColors;
    int[] mFrontShadowColors;
    GradientDrawable mBackShadowDrawableLR;
    GradientDrawable mBackShadowDrawableRL;
    GradientDrawable mFolderShadowDrawableLR;
    GradientDrawable mFolderShadowDrawableRL;

    GradientDrawable mFrontShadowDrawableHBT;
    GradientDrawable mFrontShadowDrawableHTB;
    GradientDrawable mFrontShadowDrawableVLR;
    GradientDrawable mFrontShadowDrawableVRL;

    Paint mPaint;
    PageAnimation mPageAnimation;

    private void initPaper() {
        if (!readFunction.isPageAnimationTurning()) {
            return;
        }
        mIsMovingPaper = false;
        mWidth = screenWidth;
        mHeight = screenHeight;
        mMaxLength = (float) Math.hypot(mWidth, mHeight);
        mPath0 = new Path();
        mPath1 = new Path();
        createDrawable();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);

        ColorMatrix cm = new ColorMatrix();
        float array[] = { 0.55f, 0, 0, 0, 80.0f, 0, 0.55f, 0, 0, 80.0f, 0, 0, 0.55f, 0, 80.0f, 0, 0, 0, 0.1f, 0 };
        cm.set(array);
        mColorMatrixFilter = new ColorMatrixColorFilter(cm);

        if (mMatrix == null) {
            mMatrix = new Matrix();
        } else {
            mMatrix.reset();
        }
        
        if (mPageAnimation == null) {
            mPageAnimation = new PageAnimation();
            mPageAnimation.setInterpolator(new DecelerateInterpolator());
        }
    }
    
    public void resetPageAnimation() {
        clearAnimation();
        hideNoteActionBar();
        showAllImageViewOnAnimationEnd();
        mIsMovingPaper = false;
        endNoteSelection();
        invalidate();
    }
    
    /**
     * 仿真翻页专用，生成页面图片
     */
    public void generatePageBitmap() {
        if (!readFunction.isPageAnimationTurning() || mWidth <= 0
                || mHeight <= 0) {
            return;
        }
        if (mCurPageBitmap == null || mCurPageBitmap.isRecycled()) {
            mCurPageBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
            this.draw(new Canvas(mCurPageBitmap));
        }
    }
    
    public Bitmap getCurPageBitmap() {
        return mCurPageBitmap;
    }

    public void setNextPageBitmap(Bitmap nextPage) {
        if (this.mNextPageBitmap != null && !this.mNextPageBitmap.isRecycled()) {
            this.mNextPageBitmap.recycle();
        }
        if(nextPage == null)
            return ;
        this.mNextPageBitmap = Bitmap.createBitmap(nextPage);
    }
    
    public void setPrevPageBitmap(Bitmap prevPage) {
        if (this.mPrevPageBitmap != null && !this.mPrevPageBitmap.isRecycled()) {
            this.mPrevPageBitmap.recycle();
        }
        if(prevPage == null)
            return ;
        this.mPrevPageBitmap = Bitmap.createBitmap(prevPage);
    }

    public void releasePageBitmap() {
        if (mIsMovingPaper) {
            return;
        }
        if (mCurPageBitmap != null) {
            if (!mCurPageBitmap.isRecycled()) {
                mCurPageBitmap.recycle();
            }
            mCurPageBitmap = null;
        }
        if (this.mNextPageBitmap != null && !this.mNextPageBitmap.isRecycled()) {
            this.mNextPageBitmap.recycle();
        }
        if (this.mPrevPageBitmap != null && !this.mPrevPageBitmap.isRecycled()) {
            this.mPrevPageBitmap.recycle();
        }
        mNextPageBitmap = null;
        mPrevPageBitmap = null;
    }

    /**  Author : hmg25 Version: 1.0 Description : 计算拖拽点对应的拖拽脚  */
    private void calcCornerXY(float x, float y) {
        mCornerX = mWidth;
        if (y <= mHeight / 2)
            mCornerY = 0;
        else
            mCornerY = mHeight;
        if ((mCornerX == 0 && mCornerY == mHeight) || (mCornerX == mWidth && mCornerY == 0))
            mIsRTandLB = true;
        else
            mIsRTandLB = false;
    }

    private boolean onPageTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mIsTouchDown = false;
            mTouch.x = event.getX();
            mTouch.y = event.getY();
            this.postInvalidate();
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mTouch.x = event.getX();
            mTouch.y = event.getY();
            calcCornerXY(mTouch.x, mTouch.y);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mIsTouchDown = false;
            if (mIsMovingNext && mPageAnimation != null) {
//                gotoNextPage();
                startAnimation();
            }
        }
        return true;
    }
    
    private void gotoPrevPage() {
        if (mPageAnimation == null) {
            return;
        }
        mPageAnimation.initPageAnimation(mIsMovingNext, (int)mTouch.x,
                (int)mTouch.y);
        mPageAnimation.setDuration(PAGE_ANIMATION_DURATION);
        mPageAnimation.setFillAfter(true);
        mPageAnimation.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation arg0) {
            	System.out.println("TTTTTTTTT======111===" + System.currentTimeMillis());
                mIsMovingPaper = true;
                readFunction.forceGotoPrevPageInAnimation();
                readFunction.savePageAnimationTime();
            }
            
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            
            @Override
            public void onAnimationEnd(Animation arg0) {
            	System.out.println("TTTTTTTTT======2222===" + System.currentTimeMillis());
            }
        });
        startAnimation(mPageAnimation);
    }
    
    boolean mAnimationPlaying = true;
    private void gotoNextPage() {
        if (mPageAnimation == null) {
            return;
        }
        mPageAnimation.initPageAnimation(mIsMovingNext, (int) mTouch.x,
                (int) mTouch.y);
        mPageAnimation.setDuration(200);
        mPageAnimation.setFillAfter(true);
        mPageAnimation.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation arg0) {
            	mAnimationPlaying = true;
                readFunction.forceGotoNextPageInAnimation();
                readFunction.savePageAnimationTime();
            }
            
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            
            @Override
            public void onAnimationEnd(Animation arg0) {
            	mAnimationPlaying = false;
            }
        });
        startAnimation(mPageAnimation);
    }

    /** Author : hmg25 Version: 1.0 Description : 求解直线P1P2和直线P3P4的交点坐标 */
    public PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
        PointF CrossP = new PointF();
        // 二元函数通式： y=ax+b
        float a1 = (P2.y - P1.y) / (P2.x - P1.x);
        float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);

        float a2 = (P4.y - P3.y) / (P4.x - P3.x);
        float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
        CrossP.x = (b2 - b1) / (a1 - a2);
        CrossP.y = a1 * CrossP.x + b1;
        return CrossP;
    }

    private void calcPoints() {
        mMiddleX = (mTouch.x + mCornerX) / 2;
        mMiddleY = (mTouch.y + mCornerY) / 2;
        mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
        mBezierControl1.y = mCornerY;
        mBezierControl2.x = mCornerX;
        mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

        mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2;
        mBezierStart1.y = mCornerY;

        // 当mBezierStart1.x < 0或者mBezierStart1.x > mWidth时
                // 如果继续翻页，会出现BUG故在此限制
        if (mTouch.x > 0 && mTouch.x < mWidth) {
        	if (mBezierStart1.x < 0 || mBezierStart1.x > mWidth) {   
                if (mBezierStart1.x < 0)         
                    mBezierStart1.x = mWidth - mBezierStart1.x;

                float f1 = Math.abs(mCornerX - mTouch.x);
                float f2 = mWidth * f1 / mBezierStart1.x;
                mTouch.x = Math.abs(mCornerX - f2);

                float f3 = Math.abs(mCornerX - mTouch.x) * Math.abs(mCornerY - mTouch.y) / f1;
                mTouch.y = Math.abs(mCornerY - f3);
                mMiddleX = (mTouch.x + mCornerX) / 2;
                mMiddleY = (mTouch.y + mCornerY) / 2;

                mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
                mBezierControl1.y = mCornerY;

                mBezierControl2.x = mCornerX;
                mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);
                mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2;
            }
        }
        

        mBezierStart2.x = mCornerX;
        mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y) / 2;

        mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX), (mTouch.y - mCornerY));

        mBezierEnd1 = getCross(mTouch, mBezierControl1, mBezierStart1, mBezierStart2);
        mBezierEnd2 = getCross(mTouch, mBezierControl2, mBezierStart1, mBezierStart2);


        /*
         * mBeziervertex1.x 推导
         * ((mBezierStart1.x+mBezierEnd1.x)/2+mBezierControl1.x)/2  化简等价于
         * (mBezierStart1.x+ 2*mBezierControl1.x+mBezierEnd1.x) / 4
         */
        mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;
        mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;
        mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;
        mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;
    }

    private void drawCurrentPageArea(Canvas canvas, Bitmap bitmap, Path path) {
        mPath0.reset();
        mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x, mBezierEnd1.y);
        mPath0.lineTo(mTouch.x, mTouch.y);
        mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x, mBezierStart2.y);
        mPath0.lineTo(mCornerX, mCornerY);
        mPath0.close();

        canvas.save();
        canvas.clipPath(path, Region.Op.XOR);
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
        canvas.restore();
    }

    private void drawNextPageAreaAndShadow(Canvas canvas, Bitmap bitmap) {
        mPath1.reset();
        mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
        mPath1.lineTo(mBeziervertex2.x, mBeziervertex2.y);
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
        mPath1.lineTo(mCornerX, mCornerY);
        mPath1.close();

        mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x - mCornerX, mBezierControl2.y - mCornerY));
        int leftx;
        int rightx;
        GradientDrawable mBackShadowDrawable;
        if (mIsRTandLB) {
            leftx = (int) (mBezierStart1.x);
            rightx = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
            mBackShadowDrawable = mBackShadowDrawableLR;
        } else {
            leftx = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
            rightx = (int) mBezierStart1.x;
            mBackShadowDrawable = mBackShadowDrawableRL;
        }
        canvas.save();
        canvas.clipPath(mPath0);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
        mBackShadowDrawable.setBounds(leftx, (int) mBezierStart1.y, rightx, (int) (mMaxLength + mBezierStart1.y));
        mBackShadowDrawable.draw(canvas);
        canvas.restore();
    }

    private void onPageDraw(Canvas canvas) {
        if (mCurPageBitmap == null || mCurPageBitmap.isRecycled()) {
            return;
        }
        calcPoints();
        if (mBezierControl1.x == Float.NaN || mBezierControl1.y == Float.NaN
                || mBezierControl2.x == Float.NaN
                || mBezierControl2.y == Float.NaN) {
            if (mCurPageBitmap != null && !mCurPageBitmap.isRecycled()) {
                canvas.drawBitmap(mCurPageBitmap, 0, 0, null);
            }
            return;
        }
        if (mBezierControl1.x == Float.NEGATIVE_INFINITY
                || mBezierControl1.y == Float.NEGATIVE_INFINITY
                || mBezierControl2.x == Float.NEGATIVE_INFINITY
                || mBezierControl2.y == Float.NEGATIVE_INFINITY) {
            if (mCurPageBitmap != null && !mCurPageBitmap.isRecycled()) {
                canvas.drawBitmap(mCurPageBitmap, 0, 0, null);
            }
            return;
        }
        if (mBezierControl1.x == Float.POSITIVE_INFINITY
                || mBezierControl1.y == Float.POSITIVE_INFINITY
                || mBezierControl2.x == Float.POSITIVE_INFINITY
                || mBezierControl2.y == Float.POSITIVE_INFINITY) {
            if (mCurPageBitmap != null && !mCurPageBitmap.isRecycled()) {
                canvas.drawBitmap(mCurPageBitmap, 0, 0, null);
            }
            return;
        }
        if (mIsMovingNext) {
            drawCurrentPageArea(canvas, mCurPageBitmap, mPath0);
            drawNextPageAreaAndShadow(canvas, mNextPageBitmap);
            drawCurrentPageShadow(canvas);
            drawCurrentBackArea(canvas, mCurPageBitmap);
        } else {
            drawCurrentPageArea(canvas, mPrevPageBitmap, mPath0);
            drawNextPageAreaAndShadow(canvas, mCurPageBitmap);
            drawCurrentPageShadow(canvas);
            drawCurrentBackArea(canvas, mPrevPageBitmap);
        }
    }

    /** Author : hmg25 Version: 1.0 Description : 创建阴影的GradientDrawable  */
    private void createDrawable() {
        int[] color = { 0x333333, 0xb0333333 };
        mFolderShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, color);
        mFolderShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFolderShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, color);
        mFolderShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowColors = new int[] { 0xff111111, 0x111111 };
        mBackShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
        mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
        mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowColors = new int[] { 0x80111111, 0x111111 };
        mFrontShadowDrawableVLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
        mFrontShadowDrawableVLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mFrontShadowDrawableVRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
        mFrontShadowDrawableVRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHTB = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
        mFrontShadowDrawableHTB.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHBT = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
        mFrontShadowDrawableHBT.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        
        int[] flatColor = { 0xb0333333 ,0x00333333};
        shadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, flatColor);
        shadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        shadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, flatColor);
        shadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }

    /** Author : hmg25 Version: 1.0 Description : 绘制翻起页的阴影 */
    public void drawCurrentPageShadow(Canvas canvas) {
        double degree;
        if (mIsRTandLB) {
            degree = Math.PI / 4 - Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x - mBezierControl1.x);
        } else {
            degree = Math.PI / 4 - Math.atan2(mTouch.y - mBezierControl1.y, mTouch.x - mBezierControl1.x);
        }
        // 翻起页阴影顶点与touch点的距离
        double d1 = (float) 25 * 1.414 * Math.cos(degree);
        double d2 = (float) 25 * 1.414 * Math.sin(degree);
        float x = (float) (mTouch.x + d1);
        float y;
        if (mIsRTandLB) {
            y = (float) (mTouch.y + d2);   
        } else {
            y = (float) (mTouch.y - d2);
        }
        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
        mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.close();
        float rotateDegrees;
        canvas.save();
        canvas.clipPath(mPath0, Region.Op.XOR);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        int leftx;
        int rightx;
        GradientDrawable mCurrentPageShadow;
        if (mIsRTandLB) {
            leftx = (int) (mBezierControl1.x);
            rightx = (int) mBezierControl1.x + 25;
            mCurrentPageShadow = mFrontShadowDrawableVLR;
        } else {
            leftx = (int) (mBezierControl1.x - 25);
            rightx = (int) mBezierControl1.x + 1;
            mCurrentPageShadow = mFrontShadowDrawableVRL;
        }

        rotateDegrees = (float) Math.toDegrees(Math.atan2(mTouch.x - mBezierControl1.x, mBezierControl1.y - mTouch.y));
        canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);
        mCurrentPageShadow.setBounds(leftx, (int) (mBezierControl1.y - mMaxLength), rightx, (int) (mBezierControl1.y));
        mCurrentPageShadow.draw(canvas);
        canvas.restore();

        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierControl2.x, mBezierControl2.y);
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
        mPath1.close();
        canvas.save();
        canvas.clipPath(mPath0, Region.Op.XOR);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        if (mIsRTandLB) {
            leftx = (int) (mBezierControl2.y);
            rightx = (int) (mBezierControl2.y + 25);
            mCurrentPageShadow = mFrontShadowDrawableHTB;
        } else {
            leftx = (int) (mBezierControl2.y - 25);
            rightx = (int) (mBezierControl2.y + 1);
            mCurrentPageShadow = mFrontShadowDrawableHBT;
        }
        rotateDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl2.y - mTouch.y, mBezierControl2.x - mTouch.x));
        canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y);
        float temp;
        if (mBezierControl2.y < 0)
            temp = mBezierControl2.y - mHeight;
        else
            temp = mBezierControl2.y;

        int hmg = (int) Math.hypot(mBezierControl2.x, temp);
        if (hmg > mMaxLength)
            mCurrentPageShadow.setBounds((int) (mBezierControl2.x - 25) - hmg, leftx, (int) (mBezierControl2.x + mMaxLength) - hmg, rightx);
        else
            mCurrentPageShadow.setBounds((int) (mBezierControl2.x - mMaxLength), leftx, (int) (mBezierControl2.x), rightx);

        mCurrentPageShadow.draw(canvas);
        canvas.restore();
    }

    /**  Author : hmg25 Version: 1.0 Description : 绘制翻起页背面  */
    private void drawCurrentBackArea(Canvas canvas, Bitmap bitmap) {
        int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
        float f1 = Math.abs(i - mBezierControl1.x);
        int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
        float f2 = Math.abs(i1 - mBezierControl2.y);
        float f3 = Math.min(f1, f2);
        mPath1.reset();
        mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
        mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath1.close();
        GradientDrawable mFolderShadowDrawable;
        int left;
        int right;
        if (mIsRTandLB) {
            left = (int) (mBezierStart1.x - 1);
            right = (int) (mBezierStart1.x + f3 + 1);
            mFolderShadowDrawable = mFolderShadowDrawableLR;
        } else {
            left= (int) (mBezierStart1.x - f3 - 1);
            right= (int) (mBezierStart1.x + 1);
            mFolderShadowDrawable = mFolderShadowDrawableRL;
        }
        canvas.save();
        canvas.clipPath(mPath0);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        canvas.drawColor(BookPageViewActivity.getBackgroundColor());

        mPaint.setColorFilter(mColorMatrixFilter);

        float dis = (float) Math.hypot(mCornerX - mBezierControl1.x, mBezierControl2.y - mCornerY);
        float f8 = (mCornerX - mBezierControl1.x) / dis;
        float f9 = (mBezierControl2.y - mCornerY) / dis;
        mMatrixArray[0] = 1 - 2 * f9 * f9;
        mMatrixArray[1] = 2 * f8 * f9;
        mMatrixArray[3] = mMatrixArray[1];
        mMatrixArray[4] = 1 - 2 * f8 * f8;
        mMatrix.reset();
        mMatrix.setValues(mMatrixArray);
        mMatrix.preTranslate(-mBezierControl1.x, -mBezierControl1.y);
        mMatrix.postTranslate(mBezierControl1.x, mBezierControl1.y);
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, mMatrix, mPaint);
        }
        // canvas.drawBitmap(bitmap, mMatrix, null);
        mPaint.setColorFilter(null);
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
        mFolderShadowDrawable.setBounds(left, (int) mBezierStart1.y, right, (int) (mBezierStart1.y + mMaxLength));
        mFolderShadowDrawable.draw(canvas);
        canvas.restore();
    }
    
    private class PageAnimation extends Animation {
        
        private final int turningX = -100;
        private int startX;
        private int startY;
        private int endX;
        private int endY;
        
        PageAnimation() {
        }
        
        protected void initPageAnimation(boolean isMovingNext, int x, int y) {
            if (isMovingNext) {
                startX = x;
                startY = y;
                endX = turningX;
                endY = mIsRTandLB ? 0 : mHeight;
            } else {
                startX = turningX;
                startY = mIsRTandLB ? 0 : mHeight;
                endX = x;
                endY = y;
            }
        }
        
        @Override
        protected void applyTransformation(float interpolatedTime,
                Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            System.out.println("TTTTTT========interpolatedTime=" + interpolatedTime);
            int curX = 0;
            int curY = 0;
            Interpolator interpolator = this.getInterpolator();
            if (null != interpolator) {
                float value = interpolator.getInterpolation(interpolatedTime);
                interpolatedTime = value;
            }
            curX = (int) (startX + (endX - startX)
                    * interpolatedTime);
            curY = (int) (startY + (endY - startY)
                    * interpolatedTime);
            mTouch.x = curX;
            mTouch.y = curY;
            invalidate();
        }
    }
    
    private void startAnimation() {
		int dx, dy;
		// dx 水平方向滑动的距离，负值会使滚动向左滚动
		// dy 垂直方向滑动的距离，负值会使滚动向上滚动
		if (mCornerX > 0) {
			dx = -(int) (mWidth + mTouch.x);
		} else {
			dx = (int) (mWidth - mTouch.x + mWidth);
		}
		if (mCornerY > 0) {
			dy = (int) (mHeight - mTouch.y);
		} else {
			dy = (int) (1 - mTouch.y); // 防止mTouch.y最终变为0
		}
		
		mScroller.startScroll((int) mTouch.x, (int) mTouch.y, dx, dy,
				Kit42View.PAGE_ANIMATION_DURATION);
		readFunction.forceGotoNextPageInAnimation();
	}

    public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			float x = mScroller.getCurrX();
			float y = mScroller.getCurrY();
			mTouch.x = x;
			mTouch.y = y;
			postInvalidate();
		}
	}
    
    /**
     * 画翻页效果
     * @param canvas
     */
    private void drawPageEffect(Canvas canvas) {
    	Paint mPaint = new Paint();
    	if(mIsMovingNext && mCurPageBitmap!=null){
    		canvas.drawBitmap(mCurPageBitmap, 0, 0, mPaint);
    	}else if(!mIsMovingNext && mPrevPageBitmap!=null){
    		canvas.drawBitmap(mPrevPageBitmap, 0, 0, mPaint);
    	}
        
    	mPaint = new Paint();
        if (mTouch.x!=-1 && mTouch.y!=-1) {
            //翻页左侧书边
            canvas.drawLine(mTouch.x, 0, mTouch.x,screenHeight, mPaint);

            //左侧书边画阴影
            shadowDrawableRL.setBounds((int)mTouch.x - 20, 0 ,(int)mTouch.x, screenHeight);
            shadowDrawableRL.draw(canvas);

            //翻页对折处
            float halfCut = mTouch.x + (screenWidth - mTouch.x)/2;
            canvas.drawLine(halfCut, 0, halfCut, screenHeight, mPaint);

            //对折处左侧画翻页页图片背面
            Rect backArea = new Rect((int)mTouch.x,0,(int)halfCut,screenHeight);
            Paint backPaint = new Paint();
            backPaint.setColor(BookPageViewActivity.getBackgroundColor());
            canvas.drawRect(backArea, backPaint);

            //将翻页图片正面进行处理水平翻转并平移到touchPt.x点
            Paint fbPaint = new Paint();
            fbPaint.setColorFilter(mColorMatrixFilter);
            Matrix matrix = new Matrix();

            matrix.preScale(-1,1);
            matrix.postTranslate(mCurPageBitmap.getWidth() + mTouch.x,0);

            canvas.save();
            canvas.clipRect(backArea);
            canvas.drawBitmap(mCurPageBitmap, matrix, fbPaint);
            canvas.restore();

            //对折处画左侧阴影
            shadowDrawableRL.setBounds((int)halfCut - 50, 0 ,(int)halfCut, screenHeight);
            shadowDrawableRL.draw(canvas);

            Path bgPath = new Path();

            //可以显示背景图的区域
            bgPath.addRect(new RectF(halfCut,0,screenWidth,screenHeight), Direction.CW);

            //对折出右侧画背景
            if (mIsMovingNext && mNextPageBitmap!=null) {
                canvas.save();
                //只在与路径相交处画图
                canvas.clipPath(bgPath,Op.INTERSECT);
                canvas.drawBitmap(mNextPageBitmap, 0, 0, mPaint);
                canvas.restore();
            }else if(!mIsMovingNext && mCurPageBitmap !=null){
            	canvas.save();
                //只在与路径相交处画图
                canvas.clipPath(bgPath,Op.INTERSECT);
                canvas.drawBitmap(mCurPageBitmap, 0, 0, mPaint);
                canvas.restore();
            }

            //对折处画右侧阴影
            shadowDrawableLR.setBounds((int)halfCut, 0 ,(int)halfCut + 50, screenHeight);
            shadowDrawableLR.draw(canvas);
        }
    }
    
}
