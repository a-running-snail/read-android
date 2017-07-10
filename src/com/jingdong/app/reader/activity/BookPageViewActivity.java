package com.jingdong.app.reader.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.book.EBook;
import com.jingdong.app.reader.bookmark.BookMarksFragment;
import com.jingdong.app.reader.bookshelf.BookcaseLocalFragmentNewUI;
import com.jingdong.app.reader.bookshelf.animation.EBookAnimationUtils;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager.AddToCartListener;
import com.jingdong.app.reader.bookstore.sendbook.SendBookFirstPageActivity;
import com.jingdong.app.reader.bookstore.sendbook.SendBookReceiveInfo;
import com.jingdong.app.reader.bookstore.sendbook.SendBookReceiveInfo.SendBookReceiveInfos;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadThreadQueue.RefreshAble;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.common.CommonActivity;
import com.jingdong.app.reader.config.ITransKey;
import com.jingdong.app.reader.data.DrmTools;
import com.jingdong.app.reader.data.db.DBHelper;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.ReadBookPage;
import com.jingdong.app.reader.entity.extra.BuyedEbook;
import com.jingdong.app.reader.entity.extra.JDEBook;
import com.jingdong.app.reader.entity.extra.SimplifiedDetail;
import com.jingdong.app.reader.epub.FilePath;
import com.jingdong.app.reader.epub.JDDecryptUtil;
import com.jingdong.app.reader.epub.UserSettingStatistics;
import com.jingdong.app.reader.epub.css.CSSCollection;
import com.jingdong.app.reader.epub.epub.ContentReader;
import com.jingdong.app.reader.epub.epub.PlayItem;
import com.jingdong.app.reader.epub.epub.Spine;
import com.jingdong.app.reader.epub.epub.TOCItem;
import com.jingdong.app.reader.epub.paging.Chapter;
import com.jingdong.app.reader.epub.paging.Element;
import com.jingdong.app.reader.epub.paging.IReadFunction;
import com.jingdong.app.reader.epub.paging.Kit42View;
import com.jingdong.app.reader.epub.paging.Page;
import com.jingdong.app.reader.epub.paging.PageCalculator;
import com.jingdong.app.reader.epub.paging.PageContext;
import com.jingdong.app.reader.epub.paging.PageLine;
import com.jingdong.app.reader.epub.paging.PagePool;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.ReadGetTimeListener;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.io.StoragePath;
import com.jingdong.app.reader.io.Unzip;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.media.MediaPlayerHelper;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.notes.NotesModel;
import com.jingdong.app.reader.plugin.FontItem;
import com.jingdong.app.reader.plugin.pdf.outline.OutlineItem;
import com.jingdong.app.reader.reading.BackCoverRecommendActivity;
import com.jingdong.app.reader.reading.BookBackCoverView;
import com.jingdong.app.reader.reading.BookBackCoverView.IBackCoverActionListener;
import com.jingdong.app.reader.reading.BookMark;
import com.jingdong.app.reader.reading.BookSearchActivity;
import com.jingdong.app.reader.reading.ChapterPageIndex;
import com.jingdong.app.reader.reading.CoverPage;
import com.jingdong.app.reader.reading.FinishPage;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.reading.ReadProgress;
import com.jingdong.app.reader.reading.ReadSearchData;
import com.jingdong.app.reader.reading.ReadViewPager;
import com.jingdong.app.reader.reading.ReadingData;
import com.jingdong.app.reader.service.MediaDownloadService;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ActivityUtils;
import com.jingdong.app.reader.util.DataIntent;
import com.jingdong.app.reader.util.FileUtils;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.KSICibaTranslate;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.SettingUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.TranslateTask;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.util.NetWorkUtils.NetworkConnectType;
import com.jingdong.app.reader.util.share.ShareResultListener;
import com.jingdong.app.reader.util.share.WXShareHelper;
import com.jingdong.app.reader.util.tts.TTSManager;
import com.jingdong.app.reader.util.tts.TTSManager.SpeechActionListener;
import com.jingdong.app.reader.util.tts.TTSUtil;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.SharePopupWindow;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.jingdong.app.reader.view.dialog.ProgressHUD;
import com.loopj.android.http.RequestParams;
import com.nineoldandroids.view.ViewHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.LruCache;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;
import net.davidashen.text.Hyphenator;
import net.davidashen.util.ErrorHandler;

/**
 * 打开图书流程<br />
 * 
 *
 */
@SuppressLint("InflateParams")
public class BookPageViewActivity extends CommonActivity implements IReadFunction, IBackCoverActionListener, ITransKey, RefreshAble {

    public static Typeface fontDefault = null;
    public static Typeface fzKai = null;//方正楷体
    public static Typeface fzLTH = null;//方正兰亭黑
    public static Typeface fzSS = null;//方正仿宋
    public static LruCache<String, Bitmap> bitmapCache = null;

    public static final float PageMarginTop = 65;
    public static final float PageMarginBottom = 50;
    public static float PageLineSpace = 0.5f;
    public static float PageBlockSpace = 1.0f;
    public static float leftClickX = 0;
    public static float rightClickX = 0;
    public static float topClickY = 0;
    public static float bottomClickY = 0;

    private static final int ReadSettingRequest = 0;
    private static final int TOCRequest = 1;
    private static final int PlayNavRequest = 2;
    private static final int CreateNoteRequest = 3;
    private static final int ModifyNoteRequest = 4;
    private static final int ShowNoteListRequest = 5;
    private static final int ConfirmLocalBookRequest = 6;
    private static final int SearchForBookRequest = 7;
    @SuppressWarnings("unused")
    private static final int ReaderSettingRequest = 8;

    public static final int RESULT_OPEN_TOC = Activity.RESULT_FIRST_USER;
    public static final int RESULT_OPEN_PLAYLIST = Activity.RESULT_FIRST_USER + 1;
    public static final int RESULT_PURCHASE_FULL_BOOK = Activity.RESULT_FIRST_USER + 2;
    public static final int RESULT_VIEW_NOTE = Activity.RESULT_FIRST_USER + 3;
    public static final int RESULT_VIEW_BACK = Activity.RESULT_FIRST_USER + 4;
    public static final int RESULT_CONFIRM_BOOK = Activity.RESULT_FIRST_USER + 5;
    public static final int RESULT_CHANGE_PAGE_NOTES = Activity.RESULT_FIRST_USER + 6;
    public static final int RESULT_SEARCH_BOOK = Activity.RESULT_FIRST_USER + 7;
    public static final int RESULT_TOGGLE_BOOK_MARK = Activity.RESULT_FIRST_USER + 8;
    public static final int RESULT_SETTING_MORE = Activity.RESULT_FIRST_USER + 9;
    public static final int RESULT_SETTING_TTS = Activity.RESULT_FIRST_USER + 10;

    public static final String ACTION_SHOW_FONT_DIALOG = "action_show_font_dialog";
    public static final String ACTION_CANCEL_BUILDING = "action_cancel_building";
    public static final String ACTION_PULL_NOTE = "action_pull_note";
    public static final String PlayListLocationKey = "PlayListLocationKey";
    public static final String ChangePageKey = "ChangePageKey";

    private static final int MIN_PAGE_COUNT = 6;
    private static float density = 0;
    private static float pageMarginLeft = 0;
    private static float pageMarginRight = 0;
    private static boolean isSystemFontFace = true;
    private static boolean isIgnoreCssTextColor = false;
    private static FontItem fzssFontItem;
    private static FontItem fzktFontItem;
    private static FontItem fzlthFontItem;
    private String downloadFontText = null;
    private int screenWidth;
    private int screenHeight;
    private float pageWidth;
    private float pageHeight;
    private Paint textPaint;
    private ReadViewPager viewPager;

    private List<NotesModel> notesModelList = new ArrayList<NotesModel>();
    private List<NotesModel> allNotesModelList = new ArrayList<NotesModel>();
    private List<Chapter> chapterList = new ArrayList<Chapter>();
    private List<TOCItem> tocList;
    private ArrayList<PlayItem> playList;
    private String audioPath;

    private BookPagerAdapter pageAdapter;
    private BuildChapterTask buildChapterTask;
    private OpenBookTask openBookTask;
    private PageCountTask pageCountTask;
    private PrepareNextPrevChapterInBackground prepareNextPrevTask;
    private boolean isPageFinished = false;
    private SearchTask searchTask;

    private ReadProgress progress = new ReadProgress();
    private ReadProgress localProgress;
    private boolean serverProgressSynced = false;
    private ReadProgress startProgress;
    private ReadProgress endProgress;
    private ReadProgress backProgress;
    private ReadProgress currentProgress;

    private EBook eBook;
    private Document document;
    private DocBind docBind;
    private long bookId;
    private int documentId;

    private static String userId;

    /**
     * 证书信息（Rights）
     */
    private String key;

    private String bookName;
    private String author;
    private String fontPath;

    private Kit42View noteKit42View;

    private PagePool pagePool;

    private Bitmap shareReadNoteBitmap;

    private int[] textSizeLevelSet;
    private int[] lineSpaceLevelSet;
    private int[] blockSpaceLevelSet;
    private int[] pageEdgeSpaceLevelSet;

    private boolean needBack = false;
    private boolean disableSyncNote = false;
    private boolean isFirstOpenBook = false;
    private boolean isExitBookPage = false;
    private boolean isBookOpenError = false;
    private boolean isShowAllNotes = false;
    private boolean isUseVolumePage = false;
    private boolean isProgressChange = false;
    private boolean isJDProgress = false;// 老版本京东的进度合并到新版本
    private boolean isGotoJDProgress = false;// 是否跳到老版本京东的进度
    private boolean isShowPurchaseButton = false;
    private boolean isShowBookCover = false;
    private boolean isShowSearchBar = false;
    private boolean isChangeFontFace = false;
    private boolean isDownloadFontFace = false;
    private boolean isDownloadFontFaceDone = false;
    private boolean isPageAnimationSlide = false;
    private boolean isPageAnimationTurning = false;
    private boolean isPageNoAnimation = false;
    private long lastNoteSyncTime = 0;
    private long pageAnimationTime = 0;

    private int bookPageCount = Integer.MAX_VALUE;

    private final int defaultPageAdapterIndex = 10000;
    private int batteryPercent = 0;
    private int localTextSize = 0;
    private int localLineSpace = 0;
    private int localBlockSpace = 0;
    private int localPageEdgeSpace = 0;
    private String localTocPageIndex = null;
    private String localChapterPageIndex = null;
    private String localChapterBlockIndex = null;
    private String searchKeywords = null;
    private String coverPath = null;
    private String dictPath = null;
    private static int userSettingBgColor = 0;
    private static int origialBgColor = 0;
    private static int mergedBgColor = 0;
    private static int origialFontColor = 0;
    private static int matchFontColor = 0;
    private static int bg_color;
    private static int text_color;
    private Context context;
    private Handler handler = new Handler();
    
    //朗读加载文字时开头需要截掉的长度
    private int cutIndex = 0;
    private Double nextPageTextLenth = 0.00;
    private int thisPageTextLenth= 0 ,totalTextLenth = 0 ;
    private boolean haveShowDictionary = false;
    
    private long fontTotalSize = 0;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookpage_view);
        fzssFontItem = null;
        fzktFontItem = null;
        fzlthFontItem = null;
        fzSS = null;
        fzLTH = null;
        fzKai = null;
        context = this;
        isIgnoreCssTextColor = LocalUserSetting.isIgnoreCssTextColor(this);
        if (LocalUserSetting.getReading_Night_Model(BookPageViewActivity.this)) {
            bg_color = 0xFF000000;
            text_color = ReadOverlayActivity.NIGHT_STYLE_FONT;
        } else {
            bg_color = LocalUserSetting.getReading_Background_Color(BookPageViewActivity.this);// 阅读背景颜色
            text_color = LocalUserSetting.getReading_Text_Color(BookPageViewActivity.this);// 字体颜色
        }

        // 动态获取字体大小数组
        textSizeLevelSet = getResources().getIntArray(R.array.bookPageViewTextSizeLevel);
        //行间距区间
        lineSpaceLevelSet = getResources().getIntArray(R.array.bookPageViewLineSpaceLevel);
        //段间距区间
        blockSpaceLevelSet = getResources().getIntArray(R.array.bookPageViewBlockSpaceLevel);
        pageEdgeSpaceLevelSet = getResources().getIntArray(R.array.bookPageViewPageEdgeSpaceLevel);

        Intent intent = getIntent();
        pagePool = new PagePool();

        bookId = intent.getLongExtra(OpenBookHelper.EBookIdKey, 0);
        documentId = intent.getIntExtra(OpenBookHelper.DocumentIdKey, 0);

        if (bookId > 0) {
            eBook = MZBookDatabase.instance.getEBookByBookId(bookId);
        }

        if (documentId > 0) {
            document = MZBookDatabase.instance.getDocument(documentId);
        }

        if (eBook == null && document == null) {
            return;
        }

        ReadBookPage bookPage = null;
        userId = LoginUser.getpin();

        isSystemFontFace = true;
        FontItem fontItem = DBHelper.queryEnabledFontItem();
        if (!TextUtils.isEmpty(fontItem.getFilePath()) && FileUtils.isExist(fontItem.getFilePath())) {
            try {
                fontDefault = Typeface.createFromFile(fontItem.getFilePath());//默认字体
                if (fontItem.getPlugin_src() != FontItem.KEY_FONT_SRC_System) {
                    isSystemFontFace = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                fontDefault = Typeface.DEFAULT;
                ToastUtil.showToastInThread(R.string.font_destory);
            }
        } else {
            fontDefault = Typeface.DEFAULT;
        }

        fontPath = fontItem.getFilePath();
        if (fzKai == null) {
            fontItem = DBHelper.queryFontItemByName(FontItem.FOUNDER_KAITI);
            if (!TextUtils.isEmpty(fontItem.getFilePath()) && FileUtils.isExist(fontItem.getFilePath())) {
                try {
                    fzKai = Typeface.createFromFile(fontItem.getFilePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (eBook != null) {
            LocalBook book = LocalBook.getLocalBook(eBook.bookId, LoginUser.getpin());
            JDDecryptUtil.isTryRead = LocalBook.SOURCE_TRYREAD_BOOK.equals(book.source);
            JDDecryptUtil.deviceUUID = DrmTools.hashDevicesInfor();
            JDDecryptUtil.random = book.random;
            JDDecryptUtil.key = book.cert;
            progress = MZBookDatabase.instance.getEbookReadProgress(userId, eBook.bookId);// 阅读进度
            progress.bookType = LocalBook.FORMAT_EPUB;
            //isFirstOpenBook = progress.updateTime <= 0; //史风全要求第一次打开可以弹出“同步阅读进度”提示 --Removed by liqiang
            localProgress = progress.clone();
            key = book.cert;
            bookName = eBook.title;
            author = eBook.authorName;
            path = book.dir + "/content";
            bookPage = MZBookDatabase.instance.getBookPage(eBook.bookId, 0);
            if (LocalBook.SOURCE_TRYREAD_BOOK.equals(book.source) || LocalBook.SOURCE_BORROWED_BOOK.equals(book.source)
                    || LocalBook.SOURCE_ONLINE_BOOK.equals(book.source)) {
                isShowPurchaseButton = true;
            }

            // 如果试读书已经购买，提示用户下载全本
            if (LocalBook.SOURCE_TRYREAD_BOOK.equals(book.source)) {
                getBuydBookToRead();
            }    
        }

        //自有图书
        if (document != null) {
            progress = MZBookDatabase.instance.getDocReadProgress(userId, document.documentId);
            progress.bookType = LocalBook.FORMAT_EPUB;
            localProgress = progress.clone();
            bookName = document.title;
            author = document.author;

            File fileDir = new File(StoragePath.getDocumentDir(this), document.documentId + File.separator + "content");
            path = fileDir.getPath();
            docBind = MZBookDatabase.instance.getDocBind(document.documentId, userId);
            // 更新最后阅读时间
            bookPage = MZBookDatabase.instance.getBookPage(0, document.documentId);

            // 确认docbind的serverid是否存在
            requestDocumentServerIDTask();
        }
        
        if (bookPage != null) {
            // 需要完善横竖屏判断逻辑 --liqiang
            if (bookPage.getFontFace().equals(fontPath)) {
                localTextSize = bookPage.getTextSize();
                localLineSpace = bookPage.getLineSpace();
                localBlockSpace = bookPage.getBlockSpace();
                localPageEdgeSpace = bookPage.getPageEdgeSpace();
                int index = bookPage.getChapterPage().indexOf("|");
                if (index > 0) {
                    localTocPageIndex = bookPage.getChapterPage().substring(0, index);
                    localChapterPageIndex = bookPage.getChapterPage().substring(index + 1);
                }
                localChapterBlockIndex = bookPage.getChapterBlockCount();
            }
        }
        if (TextUtils.isEmpty(progress.chapterItemRef) && progress.paraIndex > 0) {
            isJDProgress = true;
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        leftClickX = screenWidth * 0.33f;
        rightClickX = screenWidth * 0.67f;
        topClickY = screenHeight * 0.33f;
        bottomClickY = screenHeight * 0.67f;
        density = getResources().getDisplayMetrics().density;
        setupReadSpaceSetting();

        int textSizeLevel = LocalUserSetting.getTextSizeLevel(this);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        float pixel = textSizeLevelSet[textSizeLevel] * density;
        textPaint.setTextSize((int) (pixel + 0.5f));
        textPaint.setSubpixelText(true);
        textPaint.setTypeface(fontDefault);

        //核心:异步任务，解析章节数据,并展示
        buildChapterTask = new BuildChapterTask();
        buildChapterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

        LocalUserSetting.getReadStyle(this);
        LocalUserSetting.useSoftRender(this);
        viewPager = (ReadViewPager) findViewById(R.id.pager);
        viewPager.setBackgroundColor(getBackgroundColor());
        registerBatteryMonitor();
        registerReceiver();
        
//        //更新字体下载地址之后第一次进入后刷新字体库数据库内容
//        if(!LocalUserSetting.getUpdateFontUrl(BookPageViewActivity.this)){
//        	DBHelper.initDefautDbData();
//        	LocalUserSetting.saveUpdateFontUrl(BookPageViewActivity.this);
//        }
       
    }

    /**
     * 提示用户下载全本
     */
	private void getBuydBookToRead() {
		if (NetWorkUtils.isNetworkConnected(BookPageViewActivity.this) && LoginUser.isLogin()) {
		    WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSearchBuyedEbookParams(eBook.title, "0", "100"),
		            true, new MyAsyncHttpResponseHandler(context) {

		                @Override
		                public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
		                }

		                @Override
		                public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

		                    String result = new String(responseBody);

		                    BuyedEbook ebook = GsonUtils.fromJson(result, BuyedEbook.class);
		                    if (ebook != null && ebook.code == 0) {
		                        if (ebook.resultList != null && ebook.resultList.size() > 0) {
		                            for (JDEBook book : ebook.resultList) {
		                                if (book.bookId.equals(eBook.bookId+"")) {
		                                    String message = "您已购买此书，可以去已购列表下载完整版。";
		                                    if(isFinishing())
		                                        return ;
		                                    DialogManager.showCommonDialog(context, "温馨提示", message, "马上下载", "下次再说",
		                                            new DialogInterface.OnClickListener() {

		                                        @Override
		                                        public void onClick(DialogInterface dialog, int which) {
		                                            switch (which) {
		                                            case DialogInterface.BUTTON_POSITIVE:
		                                                ActivityUtils.startActivity(context,
		                                                        new Intent(context, BookcaseCloudActivity.class));
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
		                            }
		                        }
		                    }
		                }
		            });
		}
	}
    
    /**
     * 展示图书流程<br />
     * 1、reloadPageForAnimation 方法：启动异步任务GoToProgressTask
     */
    @Override
    protected void onResume() {
        super.onResume();
        int animation = LocalUserSetting.getPageAnimation(this);
        isPageAnimationSlide = animation == 1;
        isPageAnimationTurning = animation == 2;
        isPageNoAnimation = animation == 3;
        if (null != viewPager) {
            viewPager.setPagingEnabled(!isPageAnimationTurning && !isPageNoAnimation);
        }
        if (isPageAnimationTurning && pageAdapter != null) {
        	//核心方法：加载图书展示
            reloadPageForAnimation();
        }
        userId = LoginUser.getpin();
        DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_PLUG);
        isUseVolumePage = LocalUserSetting.useVolumePage(this);
        //是否显示全部人的笔记
        isShowAllNotes = MZBookDatabase.instance.isShowAllNotes(userId, eBook == null ? 0 : eBook.bookId, document == null ? 0 : document.documentId);
        loadCurrentChapterAllNotes();//加载笔记
        startProgress = progress.clone();
        startProgress.updateTime = System.currentTimeMillis() / 1000;
        if (!disableSyncNote) {
            refreshNote();
            disableSyncNote = false;
        }
        if (needBack) {
        	exit();
        }
        if (isChangeFontFace) {
            changeFontFace();
        }
        if (isDownloadFontFaceDone) {
            changeFontFace();
        }
        StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yuedu_epub));
    }
    
    private SpeechActionListener speechActionListener = new SpeechActionListener() {
        
        @Override
        public void switchAndSpeechNextPage() {
            pageAdapter.goToNext(true);
            List<Element> pageElement = getPageElementList("next");

            if (pageElement != null) {
                TTSManager.getInstance().initFirstPageSpeech(pageElement,nextPageTextLenth,pageAdapter.getPrimaryItem());
            }
        }

        @Override
        public void switchNextPage() {
            pageAdapter.goToNext(true);
        }

        @Override
        public void speechNextPage() {
            List<Element> pageElement = getPageElementList("next");

            if (pageElement != null) {
                TTSManager.getInstance().initFirstPageSpeech(pageElement,nextPageTextLenth,pageAdapter.getPrimaryItem());
            }
        }
    };

    
    /**
     * 观察者，在chapter类执行doPage方法后刷新阅读页
     */
    private Observer pageLoadObserver = new Observer() {

        @Override
        public void update(Observable observable, Object data) {
            if (data instanceof Object[]) {
                Object[] object = (Object[]) data;
                String position = (String) object[0];
                Page page = (Page) object[1];

                if (pageAdapter != null) {
                    pageAdapter.refreshPage(position, page);
                }
            } else {
                isBookOpenError = true;

                if (pageAdapter != null) {
                    pageAdapter.loadErrorPage();
                }
            }
        }

    };

    public static int getPageMarginLeft() {
        return (int) pageMarginLeft;
    }

    public static int getPageMarginRight() {
        return (int) pageMarginRight;
    }

    public static float getPageLineSpace() {
        return PageLineSpace;
    }

    public static float getPageBlockSpace() {
        return PageBlockSpace;
    }

    public static String getUserId() {
        return userId;
    }

    public static float getDensity() {
        return density;
    }

    public static LruCache<String, Bitmap> getBitmapCache() {
        if (bitmapCache == null) {
            int totalMemory = (int) (Runtime.getRuntime().totalMemory() / 1024);
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 10;

            if (cacheSize >= (maxMemory - totalMemory) / 2) {
                return null;
            }

            bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
        return bitmapCache;
    }

    public static void removeBitmapCache(String key) {
        if (!TextUtils.isEmpty(key) && bitmapCache != null) {
            Bitmap bitmap = bitmapCache.remove(key);
        }
    }

    
    /**
     * 行间距、段间距
     */
    private void setupReadSpaceSetting() {
        int level = LocalUserSetting.getPageEdgeSpaceLevel(this);
        float factor = pageEdgeSpaceLevelSet[level] / 100.0f;
        pageMarginLeft = getResources().getInteger(R.integer.default_bookpageview_margin_left) * factor;
        pageMarginRight = getResources().getInteger(R.integer.default_bookpageview_margin_right) * factor;
        //页面宽度（减去屏幕的空白）
        pageWidth = screenWidth - getPageMarginLeft() * density - getPageMarginRight() * density;
        //页面高度（减去屏幕的空白）
        pageHeight = screenHeight - PageMarginTop * density - PageMarginBottom * density;

        //行间距倍数
        level = LocalUserSetting.getLineSpaceLevel(this);
        PageLineSpace = lineSpaceLevelSet[level] / 100.0f;

        level = LocalUserSetting.getBlockSpaceLevel(this);
        PageBlockSpace = blockSpaceLevelSet[level] / 100.0f;
    }
    
    
    
    private synchronized void changeFontFace() {
        showLoading();
        isChangeFontFace = false;
        isDownloadFontFaceDone = false;

        if (bitmapCache != null) {
            bitmapCache.evictAll();
        }

        localTextSize = 0;
        localLineSpace = 0;
        localBlockSpace = 0;
        localPageEdgeSpace = 0;
        localTocPageIndex = null;
        localChapterPageIndex = null;

        if(null == pageAdapter) {
            return;
        }

        pageAdapter.movingNextItemRef = null;
        pageAdapter.movingPrevItemRef = null;
        deletePageContent();
        updateProgress(pageAdapter.currentPage);

        if (pageCountTask != null) {
            pageCountTask.cancel(true);
        }

        openBookTask = new OpenBookTask();
        openBookTask.isChangedFontFace = true;
        openBookTask.execute();
        BookPageViewActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (pageAdapter != null) {
                    pageAdapter.notifyDataSetChanged();
                    pageAdapter.invalidatePage();
                }
            }
        });
    }
    
    private void reloadPageForAnimation() {
        GoToProgressTask task = new GoToProgressTask();
        task.execute("");
    }

    private void loadCurrentChapterAllNotes() {
        if (chapterList != null) {
            for (Chapter chapter : chapterList) {
                chapter.setShowAllNotes(isShowAllNotes);
            }
        }
        if (!isShowAllNotes) {
            return;
        }

        Chapter currentChapter = null;
        if (pageAdapter != null && pageAdapter.currentPage != null) {
            currentChapter = pageAdapter.currentPage.getChapter();
        }
        if (currentChapter != null) {
            loadChapterAllNotes(currentChapter);
        }
    }

    private synchronized void refreshNote() {
        if (chapterList == null || chapterList.size() == 0) {
            return;
        }
        for (Chapter chapter : chapterList) {
            List<ReadNote> noteList = new ArrayList<ReadNote>();
            if (eBook != null) {
                noteList = MZBookDatabase.instance.listChapterReadNote(eBook.bookId, 0, chapter.getSpine().spineIdRef);
            }
            if (document != null) {
                noteList = MZBookDatabase.instance.listChapterReadNote(0, document.documentId, chapter.getSpine().spineIdRef);
            }
            List<ReadNote> myNoteList = new ArrayList<ReadNote>();
            List<ReadNote> peopleNoteList = new ArrayList<ReadNote>();
            for (ReadNote note : noteList) {
                if (note.userId.equals(userId)) {
                    myNoteList.add(note);
                } else {
                    peopleNoteList.add(note);
                }
            }
            chapter.refreshReadNote(myNoteList);
            chapter.addReadNote(peopleNoteList);
            if (isExitBookPage) {
                return;
            }
        }

        List<NotesModel> list = null;
        if (eBook != null) {
            list = MZBookDatabase.instance.listAllNotesModel(userId, eBook.bookId, 0);
        }
        if (document != null) {
            list = MZBookDatabase.instance.listAllNotesModel(userId, 0, document.documentId);
        }
        if (list != null) {
            notesModelList.clear();
            notesModelList.addAll(list);
        }

        BookPageViewActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (pageAdapter != null) {
                    pageAdapter.refreshNote();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pageAdapter != null && !isJDProgress) {
            updateProgress(pageAdapter.currentPage);
        }
        endProgress = progress.clone();
        syncReadNote();
        if (!TextUtils.isEmpty(userId)) {
            uploadReadProgressAndBookMark();
            uploadReadingData();
            uploadReadNote();
        }
        StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yuedu_epub));
    }

    class BatteryMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                // 当前剩余电量
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                // 电量最大值
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                // 电量百分比
                batteryPercent = level * 100 / scale;
                // ToastUtil.showToastInThread("电量百分比:"+batteryPercent,
                // Toast.LENGTH_LONG);
                if (pageAdapter != null) {
                    //刷新频率较高，不清理仿真页面截图
                    pageAdapter.invalidatePage(false);
                }
            }
        }

    }

    class ReadingReceiver extends BroadcastReceiver {
        String downloadUrl = "";

		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_PULL_NOTE)) {
				requestSyncNote();
			} else if (intent.getAction().equals(ACTION_SHOW_FONT_DIALOG)) {
				prepareShowDownloadFontBar();
			} else if (intent.getAction().equals(MediaDownloadService.ACTION_MEDIA_DOWNLOAD)) {
				String url = intent.getStringExtra(MediaDownloadService.MediaUrlPathKey);
				if (TextUtils.isEmpty(url)) {
					return;
				}
				if (downloadUrl.equals(url)) {
					playAudioWithPath(url);
					if(null != pageAdapter) {
						pageAdapter.refreshPlayContrl();
					}
				}
			} else if (intent.getAction().equals(ReadOverlayActivity.ACTION_READSTYLE_CHANGE)) {
				// read color from user setting
				isIgnoreCssTextColor = LocalUserSetting.isIgnoreCssTextColor(BookPageViewActivity.this);
				if (LocalUserSetting.getReading_Night_Model(BookPageViewActivity.this)) {
					bg_color = 0xFF000000;
					text_color = ReadOverlayActivity.NIGHT_STYLE_FONT;
				} else {
					bg_color = LocalUserSetting.getReading_Background_Color(BookPageViewActivity.this);
					text_color = LocalUserSetting.getReading_Text_Color(BookPageViewActivity.this);
				}
				
				viewPager.setBackgroundColor(getBackgroundColor());
				
				if(null != pageAdapter) {
					pageAdapter.refreshPageContent();
				}
			} else if (intent.getAction().equals(ReadOverlayActivity.ACTION_PAGENUMBER_CHANGE)) {
				changePage(intent);
			} else if (intent.getAction().equals(ReadOverlayActivity.ACTION_SETTING_FONT_CHANGE)) {
				int settingFontSize = intent.getIntExtra(ReadOverlayActivity.PageFontSizeKey, -1);
				ChangeFontSizeTask task = new ChangeFontSizeTask();
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, settingFontSize);
			} else if (intent.getAction().equals(ReadOverlayActivity.ACTION_CHANGE_READ_SPACE)) {
				setupReadSpaceSetting();
				ChangeReadSpaceTask task = new ChangeReadSpaceTask();
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else if (intent.getAction().equals(ReadOverlayActivity.ACTION_READ_SPACE_DONE)) {
				localTextSize = 0;
				localLineSpace = 0;
				localBlockSpace = 0;
				localPageEdgeSpace = 0;
				localTocPageIndex = null;
				localChapterPageIndex = null;
				if(null != pageAdapter) {
					pageAdapter.movingNextItemRef = null;
					pageAdapter.movingPrevItemRef = null;
					updateProgress(pageAdapter.currentPage);
				}
				if (pageCountTask != null) {
					pageCountTask.cancel(true);
				}
				openBookTask = new OpenBookTask();
				openBookTask.isChangedReadSpace = true;
				openBookTask.execute();
			} else if (intent.getAction().equals(ReadOverlayActivity.ACTION_GO_BACK_PROGRESS)) {
				isProgressChange = true;
				currentProgress = progress.clone();
				progress = backProgress.clone();
				GoToProgressTask task = new GoToProgressTask();
				task.execute("");
			} else if (intent.getAction().equals(ReadOverlayActivity.ACTION_GO_FORWARD_PROGRESS)) {
				isProgressChange = true;
				progress = currentProgress.clone();
				GoToProgressTask task = new GoToProgressTask();
				task.execute("");
			} else if (intent.getAction().equals(ReadOverlayActivity.ACTION_READFONT_CHANGE_DONE)) {
				localTextSize = 0;
				localLineSpace = 0;
				localBlockSpace = 0;
				localPageEdgeSpace = 0;
				localTocPageIndex = null;
				localChapterPageIndex = null;
				if(null != pageAdapter) {
					pageAdapter.movingNextItemRef = null;
					pageAdapter.movingPrevItemRef = null;
					updateProgress(pageAdapter.currentPage);
				}
				if (pageCountTask != null) {
					pageCountTask.cancel(true);
				}
				openBookTask = new OpenBookTask();
				openBookTask.isCurrentChapterChangedFont = true;
				openBookTask.execute();
			} else if (intent.getAction().equals(ReadOverlayActivity.ACTION_SIMPLIFIED_TO_TRADITIONAL)) {
				if(null != pageAdapter) {
					updateProgress(pageAdapter.currentPage);
				}
				if (pageCountTask != null) {
					pageCountTask.cancel(true);
				}
				// 若上一个task正在运行，则不响应当前的task
				if(openBookTask!=null){
					if(openBookTask.getStatus()==AsyncTask.Status.RUNNING){
						return ;
					}
				}
				openBookTask = new OpenBookTask();
				openBookTask.execute();
				BookPageViewActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pageAdapter != null) {
                            pageAdapter.notifyDataSetChanged();
                            pageAdapter.invalidatePage();
                        }
                    }
                });
            } else if (intent.getAction().equals(ReaderSettingActivity.ACTION_CHANGE_FONTFACE)) {
                String path = intent.getStringExtra(ReaderSettingActivity.FontPathKey);
                if (!TextUtils.isEmpty(path) && !path.equals(fontPath)) {
                    try {
                        fontDefault = Typeface.createFromFile(path);
                        isSystemFontFace = intent.getBooleanExtra(ReaderSettingActivity.SystemFontKey, false);
                        isChangeFontFace = true;
                        fontPath = path;
                    } catch (Exception e) {
                        e.printStackTrace();
                        fontDefault = Typeface.DEFAULT;
                        ToastUtil.showToastInThread(R.string.font_destory);
                    }
                }
            } else if (intent.getAction().equals(ReaderSettingActivity.ACTION_DOWNLOAD_FONT_DONE)) {
                if (!isChangeFontFace && isDownloadFontFace) {
                    isDownloadFontFaceDone = true;
                }
            } else if (intent.getAction().equals(BookMarksFragment.ACTION_RELOAD_BOOKMARK)) {
                ArrayList<String> itemRefList = intent.getStringArrayListExtra(BookMarksFragment.CHAPTER_ITEM_REF);
                if (itemRefList != null && itemRefList.size() > 0 && chapterList != null) {
                    for (Chapter chapter : chapterList) {
                        if (chapter == null)
                            continue;
                        String itemRef = chapter.getSpine().spineIdRef;
                        if (itemRefList.contains(itemRef)) {
                            chapter.loadBookMark(userId);
                        }
                    }

                    if (pageAdapter != null) {
                        pageAdapter.refreshBookMark();
                    }
                }
            } else if (intent.getAction().equals(BookNoteForMe.ACTION_RELOAD_READNOTE)) {
                disableSyncNote = false;
            } else if (intent.getAction().equals(TranslateTask.TRANSLATION_QUERY_RESULT_OK)) {
            	//TODO 翻译结果返回，准备显示
                final String result = intent.getStringExtra(TranslateTask.TRANSLATION_QUERY_RESULT);
                final String word = intent.getStringExtra(TranslateTask.TRANSLATION_QUERY_WORD);
                BookPageViewActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        noteKit42View.showDictionaryResult(word,result,true,haveShowDictionary);
                    }
                });
            } else if (intent.getAction().equals(BackCoverRecommendActivity.ACTION_PURCHASE_BOOK)) {
                purchaseFullBook();
            } else if (intent.getAction().equals(BookSearchActivity.ACTION_SEARCH)) {
                for (int i = 0, n = chapterList.size(); i < n; i++) {
                    Chapter chapter = chapterList.get(i);
                    chapter.setCancelSearchPage(false);
                }
                searchKeywords = intent.getStringExtra(BookSearchActivity.SEARCH_KEYWORDS);
                boolean isLoadData = intent.getBooleanExtra(BookSearchActivity.LOAD_SEARCH_DATA, false);
                if (isLoadData) {
                    loadReadSearchData();
                } else {
                    searchTask = new SearchTask();
                    searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            } else if (intent.getAction().equals(BookSearchActivity.ACTION_SEARCH_CANCEL)) {
                boolean isStopAndClearSearch = intent.getBooleanExtra(BookSearchActivity.STOP_AND_CLEAR_SEARCH, true);
                if (isStopAndClearSearch) {
                    stopAndClearSearch();
                    if (pageAdapter != null) {
                        pageAdapter.invalidatePage();
                    }
                }
            }
        }
    }

    private ReadingReceiver receiver = new ReadingReceiver();
    private BatteryMonitor batteryMonitor = new BatteryMonitor();
    // private LoadingDialog mLoadingDailog;
    private String path;

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PULL_NOTE);
        filter.addAction(ACTION_SHOW_FONT_DIALOG);
        filter.addAction(MediaDownloadService.ACTION_MEDIA_DOWNLOAD);
        filter.addAction(ReadOverlayActivity.ACTION_READFONT_CHANGE_DONE);
        filter.addAction(ReadOverlayActivity.ACTION_SIMPLIFIED_TO_TRADITIONAL);
        filter.addAction(ReadOverlayActivity.ACTION_READSTYLE_CHANGE);
        filter.addAction(ReadOverlayActivity.ACTION_PAGENUMBER_CHANGE);
        filter.addAction(ReadOverlayActivity.ACTION_SETTING_FONT_CHANGE);
        filter.addAction(ReadOverlayActivity.ACTION_GO_BACK_PROGRESS);
        filter.addAction(ReadOverlayActivity.ACTION_GO_FORWARD_PROGRESS);
        filter.addAction(ReadOverlayActivity.ACTION_CHANGE_READ_SPACE);
        filter.addAction(ReadOverlayActivity.ACTION_READ_SPACE_DONE);
        filter.addAction(ReaderSettingActivity.ACTION_CHANGE_FONTFACE);
        filter.addAction(ReaderSettingActivity.ACTION_DOWNLOAD_FONT_DONE);
        filter.addAction(BookNoteForMe.ACTION_RELOAD_READNOTE);
        filter.addAction(BookMarksFragment.ACTION_RELOAD_BOOKMARK);
        filter.addAction(TranslateTask.TRANSLATION_QUERY_RESULT_OK);
        filter.addAction(BackCoverRecommendActivity.ACTION_PURCHASE_BOOK);
        filter.addAction(BookSearchActivity.ACTION_SEARCH_CANCEL);
        filter.addAction(BookSearchActivity.ACTION_SEARCH);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void registerBatteryMonitor() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(batteryMonitor, ifilter);
        // 当前剩余电量
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        // 电量最大值
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        // 电量百分比
        batteryPercent = level * 100 / scale;
        // ToastUtil.showToastInThread("电量百分比:"+batteryPercent,
        // Toast.LENGTH_LONG);
    }

    private void uploadReadingData() {
        if (startProgress.updateTime <= 0 || endProgress.updateTime <= 0 || startProgress.updateTime >= endProgress.updateTime) {
            return;
        }

        ReadingData data = new ReadingData();
        if (eBook != null) {
            data.setEbook_id(eBook.bookId);
        } else if (document != null) {
            data.setDocument_id(document.documentId);
        } else {
            return;
        }

        data.setUserId(userId);
        data.setStart_time(startProgress.updateTime);
        data.setStart_chapter(startProgress.chapterItemRef);
        data.setStart_para_idx(startProgress.paraIndex);
        data.setStart_pdf_page(startProgress.pdfPage);
        data.setEnd_time(endProgress.updateTime);
        data.setEnd_chapter(endProgress.chapterItemRef);
        data.setEnd_para_idx(endProgress.paraIndex);
        data.setEnd_pdf_page(endProgress.pdfPage);
        data.setLength((endProgress.updateTime - startProgress.updateTime));

        MZBookDatabase.instance.insertReadingData(data);

        requestUploadReadingData();
    }

    private void requestUploadReadingData() {
        if (eBook == null && document == null) {
            return;
        }
        if (!LoginUser.isLogin()) {
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(BookPageViewActivity.this)) {
            return;
        }
        List<ReadingData> list = MZBookDatabase.instance.getAllReadingData();
        long bookId = 0;
        float percent = 0;
        if (eBook != null && !JDDecryptUtil.isTryRead) {
            bookId = eBook.bookId;
            Page page = pageAdapter != null ? pageAdapter.currentPage : null;
            if (page instanceof FinishPage) {
                page = ((FinishPage) page).getPrevPage();
            }
            if (page != null && page.getChapter() != null) {
                int pageNumber = page.getChapter().getPageOffset(page);
                int totalNumber = page.getChapter().getBookPageCount();
                if (pageNumber != -1 && totalNumber != -1) {
                    percent = (pageNumber + 1) / (float) totalNumber;

                } else {
                    float index = chapterList.indexOf(page.getChapter());
                    percent = index / (float) chapterList.size();
                }
            }
        } else if (document != null) {
            if (docBind != null && docBind.bookId != 0) {
                bookId = docBind.bookId;
                Page page = pageAdapter != null ? pageAdapter.currentPage : null;
                if (page instanceof FinishPage) {
                    page = ((FinishPage) page).getPrevPage();
                }
                if (page != null && page.getChapter() != null) {
                    int pageNumber = page.getChapter().getPageOffset(page);
                    int totalNumber = page.getChapter().getBookPageCount();
                    if (pageNumber != -1 && totalNumber != -1) {
                        percent = (pageNumber + 1) / (float) totalNumber;

                    } else {
                        float index = chapterList.indexOf(page.getChapter());
                        percent = index / (float) chapterList.size();
                    }
                }
            }
        }

        // if (percent >= 0.95F) {
        final long book_id = bookId == 0 ? documentId : bookId;
        
        IntegrationAPI.readGetTime(this, new ReadGetTimeListener() {

            @Override
            public void onGetTimeSuccess(final int readTime) {
                long readTimeMills = endProgress.updateTime - startProgress.updateTime;
                if (readTimeMills >= readTime * 60) {
                    // 请求服务器获取阅读积分
                    IntegrationAPI.readTimeGetScore(BookPageViewActivity.this, book_id, readTime, new GrandScoreListener() {

                        @Override
                        public void onGrandSuccess(SignScore score) {
//                          CustomToast.showToast(BookPageViewActivity.this, "恭喜你今日阅读超过" + readTime + "分钟，获得" + score + "积分");
                            String scoreInfo = "恭喜你今日阅读超过" +readTime + "分钟，获得"+score.getGetScore()+"积分";
                            SpannableString span = new SpannableString(scoreInfo);
                            int start1 = 9+String.valueOf(readTime).length()+5;
                            int end1 = start1 + String.valueOf(readTime).length();
                            span.setSpan(new ForegroundColorSpan(Color.RED), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            CustomToast.showToast(BookPageViewActivity.this, scoreInfo);
                        }

                        @Override
                        public void onGrandFail() {
                            MZLog.d("J", "阅读时长达到获取积分失败");
                        }
                    });
                }
            }

            @Override
            public void onGetTimeFail() {

            }
        });
        // }

        RequestParams request = RequestParamsPool.getUploadBatchReadingData(list, bookId, percent);
        WebRequestHelper.post(URLText.uploadReadingDataUrl, request, true, new MyAsyncHttpResponseHandler(BookPageViewActivity.this) {

            @Override
            public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
                String result;
                try {
                    result = new String(responseBody, "utf-8");
                    JSONObject obj = new JSONObject(result);
                    String code = obj.optString("code");
                    if ("0".equals(code)) {
                        MZBookDatabase.instance.deleteReadingData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            }
        });
    }

    private void uploadReadProgressAndBookMark() {
        if (eBook == null && document == null) {
            return;
        }
        if (!LoginUser.isLogin()) {
            return;
        }
        if (progress.updateTime == 0) {
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(BookPageViewActivity.this)) {
            return;
        }
        RequestParams request = null;
        if (eBook != null) {
            LocalBook book = LocalBook.getLocalBook(eBook.bookId, LoginUser.getpin());
            request = RequestParamsPool.getUploadEBookReadProgressBookMark(book);
        } else if (document != null) {
            if (docBind == null || docBind.serverId == 0) {
                return;
            }
            request = RequestParamsPool.getUploadDocumentReadProgressBookMark(document);
        } else {
            return;
        }
        WebRequestHelper.post(URLText.JD_BOOK_READ_URL, request, true, new MyAsyncHttpResponseHandler(BookPageViewActivity.this) {

            @Override
            public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
                UploadServerReadProgress task = new UploadServerReadProgress();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, responseBody);
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            }
        });
    }

    private void uploadReadNote() {
        if (eBook == null && document == null) {
            return;
        }
        if (!LoginUser.isLogin()) {
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(BookPageViewActivity.this)) {
            return;
        }
        List<ReadNote> noteList = MZBookDatabase.instance.listAllUnsyncReadNote(userId);
        requestUploadReadNote(noteList, false, false, 0);
    }

    private void requestUploadReadNote(final List<ReadNote> noteList, final boolean isShareSinaWeiboFlow, final boolean isShareWeixinFlow, final int type) {
        if (noteList == null || noteList.size() == 0) {
            return;
        }
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                RequestParams request = RequestParamsPool.getUploadBookReadNote(noteList);
                WebRequestHelper.post(URLText.pushNotesUrl, request, true, new MyAsyncHttpResponseHandler(BookPageViewActivity.this) {

                    @Override
                    public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
                        boolean isRefreshNote = false;
                        try {
                            String result = new String(responseBody, "utf-8");
                            JSONArray resultArr = new JSONArray(result);
                            for (int i = 0; i < resultArr.length(); ++i) {
                                ReadNote note = noteList.get(i);

                                JSONObject resultObj = resultArr.getJSONObject(i);

                                boolean isSuccess = resultObj.optBoolean("success");
                                if (isSuccess) {
                                    if (note.serverId == -1) {
                                        long id = resultObj.optLong("id", -1);
                                        if (id != -1) {
                                            note.serverId = id;
                                            note.modified = false;
                                            isRefreshNote = true;
                                        }
                                    }
                                }
                                String code = resultObj.optString("code");
                                if ("0".equals(code)) {
                                    if (note.deleted) {
                                        note.modified = false;
                                    } else {
                                        note.modified = false;
                                    }
                                } else {
                                    note.modified = false;
                                }
                                String guid = resultObj.optString("entity_guid");
                                if (!UiStaticMethod.isNullString(guid)) {
                                    note.guid = guid;
                                    isRefreshNote = true;
                                }
                            }
                            for (ReadNote note : noteList) {
                                MZBookDatabase.instance.insertOrUpdateEbookNote(note);
                            }
                            MZBookDatabase.instance.cleanReadNote();
                            if (isShareSinaWeiboFlow) {
                                ReadNote note = noteList.get(0);
                                startShareBitmapViaSinaWeibo(note.serverId, shareReadNoteBitmap);
                            } else if (isShareWeixinFlow) {
                                ReadNote note = noteList.get(0);
                                startShareBitmapViaWeixin(note, shareReadNoteBitmap, type);
                            }
                        } catch (Exception e) {
                            if (isShareSinaWeiboFlow || isShareWeixinFlow) {
                                dismissHUD();
                            }
                        }
                        if (isRefreshNote) {
                            refreshNote();
                        }
                    }

                    

                    @Override
                    public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                        if (isShareSinaWeiboFlow || isShareWeixinFlow) {
                            dismissHUD();
                        }
                    }
                });
            }
        });
    }

    // 上传进度和书签结果处理
    private class UploadServerReadProgress extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... params) {
            try {
                String result = null;
                try {
                    result = new String(params[0], "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (TextUtils.isEmpty(result)) {
                    return null;
                }
                JSONObject obj = new JSONObject(result);
                String code = obj.optString("code");
                if ("0".equals(code)) {
                    JSONArray array = obj.getJSONArray("bookList");
                    JSONObject book = array.getJSONObject(0);
                    JSONArray list = book.getJSONArray("list");
                    long version = book.getLong("version");
                    List<BookMark> markList = new ArrayList<BookMark>();
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject data = list.getJSONObject(i);
                        if (data.getInt("dataType") == 0) {// 阅读进度
                        } else if (data.getInt("dataType") == 1) {// 书签
                            int valid = data.getInt("valid");
                            if (valid == 0) {
                                MZBookDatabase.instance.deleteBookMark(userId, data.optInt("id"));
                            } else if (valid == 1) {
                                BookMark bookMark = BookMark.fromJSON(data);
                                bookMark.docid = documentId;
                                bookMark.isSync = 1;
                                markList.add(bookMark);
                            }
                        }
                    }
                    MZBookDatabase.instance.insertOrUpdateBookMarksSyncTime(userId, bookId, documentId, version);
                    for (BookMark mark : markList) {
                        MZBookDatabase.instance.addBookMark(mark);
                    }
                    MZBookDatabase.instance.cleanBookMarks();
                } else if ("3".equals(code)) {
                } else if ("5".equals(code) || "80".equals(code)) {
                    ToastUtil.showToastInThread(obj.optString("message"), Toast.LENGTH_SHORT);
                } else {
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void syncReadNote() {
        // bug Caused by: java.lang.NullPointerException
        // at com.jingdong.app.reader.BookPageViewActivity.void
        // syncReadNote()(Unknown
        // Source)
        if (null == chapterList)
            return;

        for (Chapter chapter : chapterList) {
            for (ReadNote note : chapter.getNoteList()) {
                if (note.modified && note.userId.equals(userId)) {
                    MZBookDatabase.instance.insertOrUpdateEbookNote(note);
                    note.modified = false;
                } else {
                    note.modified = false;
                }
            }
        }
        MZBookDatabase.instance.cleanReadNote();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
        	EBookAnimationUtils anim = BookcaseLocalFragmentNewUI.getEBookAnimationUtils();
        	if (null != anim) {
        		anim.hideWindow();
        	}
        	
            final WindowManager.LayoutParams layout = getWindow().getAttributes();
            float brightness = 0;
            if (LocalUserSetting.isSyncBrightness(this)) {
                int mode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                try {
                    mode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
                } catch (SettingNotFoundException e) {
                    e.printStackTrace();
                }
                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    brightness = -1 / 255f;
                } else {
                    int bright = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
                    brightness = bright / 255f;
                }
            } else {
                brightness = LocalUserSetting.getReadBrightness(this);
            }
            layout.screenBrightness = brightness;
            layout.flags = layout.flags | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            getWindow().setAttributes(layout);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ReadSettingRequest && resultCode == RESULT_OPEN_TOC) {
            if (tocList.size() > 0) {
                int chapterIndex = 0;
                int pageIndex = -1;
                Spine spine = null;
                String tocId = null;
                String chapterName = null;
                if (pageAdapter != null && pageAdapter.currentPage != null) {
                    Page page = pageAdapter.currentPage;
                    if (page instanceof CoverPage) {
                        page = ((CoverPage) page).getNextPage();
                    }
                    if (page instanceof FinishPage) {
                        page = ((FinishPage) page).getPrevPage();
                    }
                    tocId = page.getTocId();
                    if (page.getChapter() != null) {
                        pageIndex = page.getChapter().getPageOffset(page);
                        spine = page.getChapter().getSpine();
                    } else {
                        chapterName = page.getPageHead();
                    }
                }

                int tocIndex = -1;
                int tocPageIndex = -1;
                ArrayList<OutlineItem> outlineList = new ArrayList<OutlineItem>();
                for (int i = 0; i < tocList.size(); ++i) {
                    TOCItem item = tocList.get(i);
                    OutlineItem outlineItem = new OutlineItem(item.level, item.navLabel, item.pageNumber);
                    outlineList.add(outlineItem);
                    if (tocId != null && item.contentSrc.contains(tocId)) {
                        tocIndex = i;
                    }
                    if (isPageFinished && pageIndex >= 0 && item.pageNumber <= pageIndex + 1) {
                        tocPageIndex = i;
                    }
                    if (spine != null && spine.spinePath != null && item.contentSrc.contains(spine.spinePath)) {
                        chapterIndex = i;
                    } else if (chapterName != null && chapterName.equals(item.navLabel)) {
                        chapterIndex = i;
                    }
                }
                if (tocIndex >= 0) {
                    chapterIndex = tocIndex;
                }
                if (tocPageIndex >= 0) {
                    chapterIndex = tocPageIndex;
                }

                Intent intent = new Intent(this, CatalogActivity.class);
                intent.putExtra(CatalogActivity.TOCLabelListKey, outlineList);
                intent.putExtra(CatalogActivity.BookNameKey, bookName);
                intent.putExtra(CatalogActivity.AuthorNameKey, author);
                intent.putExtra(CatalogActivity.ChapterIndexKey, chapterIndex);
                intent.putExtra(CatalogActivity.EbookIdKey, eBook == null ? 0 : eBook.bookId);
                intent.putExtra(CatalogActivity.DocumentIdKey, document == null ? 0 : document.documentId);
                intent.putExtra(CatalogActivity.DOCUMENTSIGN, document == null ? "" : document.opfMD5);
                intent.putExtra(CatalogActivity.PageCalculatorFinish, isPageFinished);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, TOCRequest);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);

            }
        } else if (requestCode == ConfirmLocalBookRequest && resultCode == RESULT_OK) {

        } else if (requestCode == SearchForBookRequest && resultCode == RESULT_OK) {
            int chapterIndex = data.getIntExtra(BookSearchActivity.CHAPTER_INDEX, 0);
            int paraIndex = data.getIntExtra(BookSearchActivity.PARA_INDEX, 0);
            int offsetInPara = data.getIntExtra(BookSearchActivity.OFFSET_IN_PARA, 0);
            gotoSearchPage(chapterIndex, paraIndex, offsetInPara);
        } else if (requestCode == ReadSettingRequest && resultCode == RESULT_OPEN_PLAYLIST) {
            Intent intent = new Intent(this, BookPlayListActivity.class);
            intent.putParcelableArrayListExtra(BookPlayListActivity.PlayListKey, playList);
            intent.putExtra(BookPlayListActivity.AudioPathKey, audioPath);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, PlayNavRequest);
        } else if (requestCode == ReadSettingRequest && resultCode == RESULT_PURCHASE_FULL_BOOK) {
            purchaseFullBook();
        } else if (requestCode == ReadSettingRequest && resultCode == RESULT_SEARCH_BOOK) {
            openSearch();
        } else if (requestCode == ReadSettingRequest && resultCode == RESULT_TOGGLE_BOOK_MARK) {
            if (pageAdapter != null) {
                pageAdapter.toggleBookMark(pageAdapter.currentPage);
            }
        } else if (requestCode == ReadSettingRequest && resultCode == RESULT_VIEW_BACK) {
        	needBack = true;
        } else if (requestCode == ReadSettingRequest && resultCode == RESULT_VIEW_NOTE) {
            Intent intent = new Intent(this, BookNoteActivity.class);
            ArrayList<OutlineItem> outlineList = new ArrayList<OutlineItem>();
            for (int i = 0; i < tocList.size(); ++i) {
                TOCItem item = tocList.get(i);
                OutlineItem outlineItem = new OutlineItem(item.level, item.navLabel, item.pageNumber);
                outlineList.add(outlineItem);
            }
            intent.putExtra(BookNoteActivity.TOCLabelListKey, outlineList);
            intent.putExtra(BookNoteActivity.USER_ID, userId);
            if (eBook != null && !TextUtils.isEmpty(userId)) {
                intent.putExtra(BookNoteActivity.BOOK_NAME, eBook.title);
                intent.putExtra(BookNoteActivity.EBOOK_ID, eBook.bookId);
            } else if (document != null && !TextUtils.isEmpty(userId)) {
                intent.putExtra(BookNoteActivity.BOOK_NAME, document.title);
                intent.putExtra(BookNoteActivity.DOCUMENT_ID, document.documentId);
                intent.putExtra(BookNoteActivity.DOCUMENT_SIGN, document.opfMD5);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, ShowNoteListRequest);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        } else if (requestCode == ReadSettingRequest && resultCode == RESULT_SETTING_MORE) {

        }else if (requestCode == ReadSettingRequest && resultCode == RESULT_SETTING_TTS) {//tts 朗读
            
            List<Element> pageElement = getPageElementList("first");
            if(pageElement!=null){
                TTSManager.getInstance().init();
                TTSManager.getInstance().setSpeechActionListener(speechActionListener);
                TTSManager.getInstance().initFirstPageSpeech(pageElement,nextPageTextLenth,this.pageAdapter.getPrimaryItem());
            }
            
//          String textBuff = getPageTextFromPage(this.pageAdapter.currentPage,"first");
//          if(textBuff!=null){
//              TTSManager.getInstance().init();
//              TTSManager.getInstance().setSpeechActionListener(speechActionListener);
//              TTSManager.getInstance().initFirstPageSpeech(textBuff.toString(),nextPageTextLenth);
//          }
//          
            
            
//          Block b=this.pageAdapter.currentPage.getChapter().getBlock(this.pageAdapter.currentPage.getParaIndex());
//          int cell=b.getTableCellNumber();
//          int row = b.getTableRowNumber();
//          this.pageAdapter.currentPage.getOffsetInPara();
//          Element element = b.getElementList().get(this.pageAdapter.currentPage.getOffsetInPara());
//          String str = element.getContent();
//          Toast.makeText(this, str, 0).show();
//          Toast.makeText(this, "index="+this.pageAdapter.currentPage.getOffsetInPara(), 0).show();
            
            
        }else if (requestCode == PlayNavRequest) {

            pageAdapter.refreshPlayContrl();
            if (resultCode == RESULT_OK) {
                String navSrc = data.getStringExtra(PlayListLocationKey);
                if (!TextUtils.isEmpty(navSrc)) {
                    try {
                        navSrc = URLDecoder.decode(navSrc, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    for (Chapter chapter : chapterList) {
                        Spine spine = chapter.getSpine();
                        if (navSrc.contains(spine.spinePath)) {
                            backProgress = progress.clone();
                            progress.paraIndex = 0;
                            progress.offsetInPara = 0;
                            progress.chapterItemRef = chapter.getSpine().spineIdRef;
                            Uri uri = Uri.parse(navSrc);
                            String id = uri.getFragment();
                            GoToProgressTask task = new GoToProgressTask();
                            task.execute(id);
                            break;
                        }
                    }
                }
            }

        } else if (requestCode == TOCRequest && resultCode == RESULT_OK) {
            int selectIndex = data.getIntExtra(CatalogActivity.TOCSelectedIndexKey, -1);
            boolean isGotoLastPage = JDDecryptUtil.isTryRead;
            if (selectIndex >= 0 && selectIndex < tocList.size()) {
                TOCItem item = tocList.get(selectIndex);
                for (int i = 0; i < chapterList.size(); ++i) {
                    Chapter chapter = chapterList.get(i);
                    Spine spine = chapter.getSpine();
                    if (item.contentSrc.contains(spine.spinePath)) {
                        backProgress = progress.clone();
                        int pageNumber = chapter.getChapterPageOffset();
                        int totalNumber = chapter.getBookPageCount();
                        if (pageNumber != -1 && totalNumber != -1) {
                            progress.percent = (pageNumber + 1) / (float) totalNumber;

                        } else {
                            progress.percent = i / (float) chapterList.size();
                        }
                        progress.chapterItemRef = spine.spineIdRef;
                        progress.paraIndex = 0;
                        progress.offsetInPara = 0;
                        isGotoLastPage = false;
                        Uri uri = Uri.parse(item.contentSrc);
                        String id = uri.getFragment();
                        GoToProgressTask task = new GoToProgressTask();
                        task.execute(id);
                        break;
                    }
                }
            }
            if (isGotoLastPage) {
                Chapter chapter = chapterList.get(chapterList.size() - 1);
                GoToLastPageTask task = new GoToLastPageTask(chapter);
                task.execute();
                ToastUtil.showToastInThread(R.string.read_book_try_over_chapter);
            }
        } else if (requestCode == CreateNoteRequest) {
            if (resultCode == RESULT_OK) {
                String quote = data.getStringExtra(BookReadNoteActivity.BookNoteQuoteKey);
                String content = data.getStringExtra(BookReadNoteActivity.BookNoteContentKey);
                boolean isPrivate = data.getBooleanExtra(BookReadNoteActivity.BookNoteIsPrivateKey, true);
                ReadNote note = createEmptyNote();
                if (quote != null) {
                    note.quoteText = quote;
                }
                if (content != null) {
                    note.contentText = content;
                }
                note.isPrivate = isPrivate;
                noteKit42View.createAsyncNote(note);
            } else {
                noteKit42View.cancelAsyncNote();
            }
        } else if (requestCode == ModifyNoteRequest) {
            if (resultCode == RESULT_OK) {
                String content = data.getStringExtra(BookReadNoteActivity.BookNoteContentKey);
                boolean isPrivate = data.getBooleanExtra(BookReadNoteActivity.BookNoteIsPrivateKey, true);
                noteKit42View.modifyNote(content, isPrivate);
            } else {
                noteKit42View.cancelModifyNote();
            }
        } else if (requestCode == ShowNoteListRequest && resultCode == RESULT_OK) {
            String itemref = data.getStringExtra(BookNoteActivity.CHAPTER_ITEM_REF);
            int paraIndex = data.getIntExtra(BookNoteActivity.PARA_INDEX, 0);
            int offsetInPara = data.getIntExtra(BookNoteActivity.OFFSET_IN_PARA, 0);

            for (int i = 0; i < chapterList.size(); ++i) {
                Chapter chapter = chapterList.get(i);
                Spine spine = chapter.getSpine();
                if (itemref.equals(spine.spineIdRef)) {
                    backProgress = progress.clone();
                    int pageNumber = chapter.getChapterPageOffset();
                    int totalNumber = chapter.getBookPageCount();
                    if (pageNumber != -1 && totalNumber != -1) {
                        progress.percent = (pageNumber + 1) / (float) totalNumber;

                    } else {
                        progress.percent = i / (float) chapterList.size();
                    }
                    progress.chapterItemRef = spine.spineIdRef;
                    progress.paraIndex = paraIndex;
                    progress.offsetInPara = offsetInPara;
                    GoToProgressTask task = new GoToProgressTask();
                    task.execute("");
                    break;
                }
            }
        } else if (requestCode == TOCRequest && resultCode == CatalogActivity.RESULT_CHANGE_PAGE) {

            String itemref = data.getStringExtra(BookNoteActivity.CHAPTER_ITEM_REF);
            int paraIndex = data.getIntExtra(BookNoteActivity.PARA_INDEX, 0);
            int offsetInPara = data.getIntExtra(BookNoteActivity.OFFSET_IN_PARA, 0);

            for (int i = 0; i < chapterList.size(); ++i) {
                Chapter chapter = chapterList.get(i);
                Spine spine = chapter.getSpine();
                if (itemref.equals(spine.spineIdRef)) {
                    backProgress = progress.clone();
                    int pageNumber = chapter.getChapterPageOffset();
                    int totalNumber = chapter.getBookPageCount();
                    if (pageNumber != -1 && totalNumber != -1) {
                        progress.percent = (pageNumber + 1) / (float) totalNumber;

                    } else {
                        progress.percent = i / (float) chapterList.size();
                    }
                    progress.chapterItemRef = spine.spineIdRef;
                    progress.paraIndex = paraIndex;
                    progress.offsetInPara = offsetInPara;
                    GoToProgressTask task = new GoToProgressTask();
                    task.execute("");
                    break;
                }
            }

        } else if (requestCode == TOCRequest && resultCode == RESULT_CHANGE_PAGE_NOTES) {

            String itemref = data.getStringExtra(BookNoteActivity.CHAPTER_ITEM_REF);
            int paraIndex = data.getIntExtra(BookNoteActivity.PARA_INDEX, 0);
            int offsetInPara = data.getIntExtra(BookNoteActivity.OFFSET_IN_PARA, 0);

            for (int i = 0; i < chapterList.size(); ++i) {
                Chapter chapter = chapterList.get(i);
                Spine spine = chapter.getSpine();
                if (itemref.equals(spine.spineIdRef)) {
                    backProgress = progress.clone();
                    int pageNumber = chapter.getChapterPageOffset();
                    int totalNumber = chapter.getBookPageCount();
                    if (pageNumber != -1 && totalNumber != -1) {
                        progress.percent = (pageNumber + 1) / (float) totalNumber;

                    } else {
                        progress.percent = i / (float) chapterList.size();
                    }
                    progress.chapterItemRef = spine.spineIdRef;
                    progress.paraIndex = paraIndex;
                    progress.offsetInPara = offsetInPara;
                    GoToProgressTask task = new GoToProgressTask();
                    task.execute("");
                    break;
                }
            }
        }else if(requestCode == SendBookFirstPageActivity.SENDBOOKFIRSTPAGE && resultCode == SendBookFirstPageActivity.SENDBOOKFIRSTPAGE){//从赠言扉页点击返回键直接关闭阅读页面
            if (!isFinishing()) {
            	exit();
            }
        }
    }
    
    @SuppressWarnings("unused")
    private List<Element> getPageElementList(String type){
        List<PageLine> pageLinelist = this.pageAdapter.currentPage.getLineList();
        List<Element> elementList;
        List<Element> pageElementList = new ArrayList<Element>();
        StringBuffer textBuff= new StringBuffer();
        for (int i = 0; i < pageLinelist.size(); i++) {
            elementList = pageLinelist.get(i).getElementList();
            pageElementList.addAll(elementList);
        }
        if(type.equals("next") && pageElementList.size()>cutIndex)
            pageElementList = pageElementList.subList(cutIndex, pageElementList.size());
        
        cutIndex = 0;
        thisPageTextLenth= pageElementList.size();
        totalTextLenth = thisPageTextLenth;
        if(pageElementList.size()>0){
            Element lastElement=pageElementList.get(pageElementList.size()-1);
            Boolean endSentence=TTSUtil.isEndSentence(lastElement.getContent());
            if(!endSentence){
                Page nextpage = this.pageAdapter.getNextPage();
                if (nextpage != null) {
                    List<PageLine> nextPageLinelist = nextpage.getLineList();
                    pageElementList=getCompleteSentenceElementList(nextPageLinelist,pageElementList);
                }
            }
        }
        nextPageTextLenth = (double) (totalTextLenth-thisPageTextLenth);
        
        return pageElementList;
    }
    
    private List<Element> getCompleteSentenceElementList(List<PageLine> nextPageLinelist, List<Element> pageElementList){
        List<Element> elementList;
        Element current;
        for (int i = 0; i < nextPageLinelist.size(); i++) {
            elementList = nextPageLinelist.get(i).getElementList();
            for (int j = 0; j < elementList.size(); j++) {
                current = elementList.get(j);
                pageElementList.add(current);
                cutIndex++;
                if(TTSUtil.isEndSentence(current.getContent())){
                    totalTextLenth = pageElementList.size();
                    return pageElementList;
                }
                    
            }
        }
        return pageElementList;
    }
    
    /**
     * 从page中获取文字
     * @param page
     * @return
     */
    @SuppressWarnings("unused")
    private String getPageTextFromPage(Page page,String type){
        if(page==null)
            return null;
        List<PageLine> pageLinelist = page.getLineList();
        List<Element> elementList;
        List<Element> pageElementList = new ArrayList<>();
        StringBuffer textBuff= new StringBuffer();
        Element current = null;
        int currentIndex = 0;
        Double elementCountOflastSentence=0.00;
        for (int i = 0; i < pageLinelist.size(); i++) {
            elementList = pageLinelist.get(i).getElementList();
            elementCountOflastSentence = 0.00;
            for (int j = 0; j < elementList.size(); j++) {
                currentIndex++;
                elementCountOflastSentence++;
                if(type.equals("next") && cutIndex >= currentIndex)
                    continue;
                current = elementList.get(j);
                textBuff.append(current.getContent());
            }
        }
        cutIndex = 0;
        thisPageTextLenth= textBuff.length();
        totalTextLenth = thisPageTextLenth;
        if(textBuff.length()>0){
            Boolean endSentence=TTSUtil.isEndSentence(current.getContent());
            if(!endSentence){
                Page nextpage = this.pageAdapter.getNextPage();
                if (nextpage != null) {
                    List<PageLine> nextPageLinelist = nextpage.getLineList();
                    textBuff=getCompleteStringBuffer(nextPageLinelist,textBuff);
                }
            }
        }
        nextPageTextLenth = (double) (totalTextLenth-thisPageTextLenth);
        return textBuff.toString();
    }
    
    
    /**
     * 获取完整句子结尾的一页text
     * @param nextPageLinelist
     * @param textBuff
     * @param matcher
     * @return
     */
    private StringBuffer getCompleteStringBuffer(List<PageLine> nextPageLinelist, StringBuffer textBuff){
        List<Element> elementList;
        Element current;
        for (int i = 0; i < nextPageLinelist.size(); i++) {
            elementList = nextPageLinelist.get(i).getElementList();
            for (int j = 0; j < elementList.size(); j++) {
                current = elementList.get(j);
                textBuff.append(current.getContent());
                cutIndex++;
                if(TTSUtil.isEndSentence(current.getContent())){
                    totalTextLenth = textBuff.length();
                    return textBuff;
                }
                    
            }
        }
        return textBuff;
    }

    private void changePage(Intent data) {
        int pageNumber = data.getIntExtra(ChangePageKey, -1);
        MZLog.d("qli", "===qli===  go to page: " + pageNumber);
        if (pageNumber != -1) {
            Chapter currentChapter = null;
            for (Chapter chapter : chapterList) {
                if (chapter.isChapterPage(pageNumber)) {
                    currentChapter = chapter;
                    break;
                }
            }
            if (currentChapter != null) {
                GoToPageTask task = new GoToPageTask(pageNumber, currentChapter);
                task.execute();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
        unregisterReceiver();
        unregisterReceiver(batteryMonitor);
        // if (mTimer!=null) {
        // mTimer.cancel();
        // mTimer = null;
        // }
    }

    private void release() {
        isExitBookPage = true;
        dismissHUD();
        // 防止内存
        
        if (buildChapterTask != null) {
            buildChapterTask.cancel(true);
        }
        if (pageCountTask != null) {
            pageCountTask.cancel(true);
        }
        if (prepareNextPrevTask != null) {
            prepareNextPrevTask.cancel(true);
        }
        if (bitmapCache != null) {
            bitmapCache.evictAll();
        }
        CancelChapterBuildPageTask task = new CancelChapterBuildPageTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Intent intent = new Intent(ACTION_CANCEL_BUILDING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        if (shareReadNoteBitmap != null && !shareReadNoteBitmap.isRecycled()) {
            shareReadNoteBitmap.recycle();
            shareReadNoteBitmap = null;
        }
        if (pageAdapter != null) {
            pageAdapter.release();
        }
        notesModelList.clear();
        allNotesModelList.clear();
        fontDefault = null;
        bitmapCache = null;
        fzssFontItem = null;
        fzktFontItem = null;
        fzlthFontItem = null;
        fzSS = null;
        fzLTH = null;
        fzKai = null;
        MediaPlayerHelper.release();
    }

    public class CancelChapterBuildPageTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (chapterList != null) {
                for (Chapter chapter : chapterList) {
                    chapter.setCancelBuildPage(true);
                    chapter.deleteObservers();
                    chapter.release();
                }
                chapterList.clear();
            }
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    void updateProgress(Page page) {
        if (page == null) {
            return;
        }
        if (page instanceof CoverPage) {
            Page nextPage = ((CoverPage) page).getNextPage();
            progress.percent = 0;
            if(nextPage == null )
                return ;
            progress.chapterItemRef = nextPage.getChapter().getSpine().spineIdRef;
            progress.chapterId = nextPage.getPlayOrder();
            progress.paraIndex = nextPage.getParaIndex();
            progress.offsetInPara = nextPage.getOffsetInPara();
            progress.chapterTitle = nextPage.getPageHead();
            progress.updateTime = System.currentTimeMillis() / 1000;
            if (eBook != null) {
                MZBookDatabase.instance.insertOrUpdateEbookReadProgress(userId, eBook.bookId, progress, isShowAllNotes);
            } else if (document != null) {
                MZBookDatabase.instance.insertOrUpdateDocReadProgress(userId, document.documentId, progress, isShowAllNotes);
            }
            return;
        }
        if (page instanceof FinishPage) {
            Page prevPage = ((FinishPage) page).getPrevPage();
            progress.percent = 1;
            if(prevPage == null )
                return ;
            progress.chapterItemRef = prevPage.getChapter().getSpine().spineIdRef;
            progress.chapterId = prevPage.getPlayOrder();
            progress.paraIndex = prevPage.getParaIndex();
            progress.offsetInPara = prevPage.getOffsetInPara();
            progress.chapterTitle = prevPage.getPageHead();
            progress.updateTime = System.currentTimeMillis() / 1000;
            if (eBook != null) {
                MZBookDatabase.instance.insertOrUpdateEbookReadProgress(userId, eBook.bookId, progress, isShowAllNotes);
            } else if (document != null) {
                MZBookDatabase.instance.insertOrUpdateDocReadProgress(userId, document.documentId, progress, isShowAllNotes);
            }
            return;
        }
        progress.chapterItemRef = page.getChapter().getSpine().spineIdRef;
        progress.chapterId = page.getPlayOrder();
        progress.paraIndex = page.getParaIndex();
        progress.offsetInPara = page.getOffsetInPara();
        progress.chapterTitle = page.getPageHead();
        progress.updateTime = System.currentTimeMillis() / 1000;

        int pageNumber = page.getChapter().getPageOffset(page);
        int totalNumber = page.getChapter().getBookPageCount();
        if (pageNumber != -1 && totalNumber != -1) {
            progress.percent = (pageNumber + 1) / (float) totalNumber;

        } else {
            float index = chapterList.indexOf(page.getChapter());
            progress.percent = index / (float) chapterList.size();
        }
        if (eBook != null) {
            MZBookDatabase.instance.insertOrUpdateEbookReadProgress(userId, eBook.bookId, progress, isShowAllNotes);
        } else if (document != null) {
            MZBookDatabase.instance.insertOrUpdateDocReadProgress(userId, document.documentId, progress, isShowAllNotes);
        }
    }

    private Page getNextPage(Page page) {
        if (page == null || page instanceof FinishPage) {
            return null;
        }

        if (page instanceof CoverPage) {
            CoverPage p = (CoverPage) page;
            return p.getNextPage();
        }

        Page next = null;
        Chapter chapter = page.getChapter();
        if (chapter != null && chapter.isChapterPageReady()) {
            next = chapter.getNextPage(page);
            if (next == null) {
                int index = chapterList.indexOf(chapter);
                if (index != chapterList.size() - 1) {
                    Chapter nextChapter = chapterList.get(index + 1);
                    next = nextChapter.getFirstPage();
                    if (next == null) {
                        nextChapter.setObservable(pageAdapter.currentIndex + 1, true);
                    }
                } else {
                    if (eBook != null || docBind != null) {
                        next = new FinishPage(page);
                    }
                }
            }
        }

        return next;
    }

    private Page getPrevPage(Page page) {
        if (page == null || page instanceof CoverPage) {
            return null;
        }

        if (page instanceof FinishPage) {
            FinishPage p = (FinishPage) page;
            return p.getPrevPage();
        }

        Page prev = null;
        Chapter chapter = page.getChapter();
        if (chapter != null && chapter.isChapterPageReady()) {
            prev = chapter.getPrevPage(page);
            if (prev == null) {
                int index = chapterList.indexOf(chapter);
                if (index != 0) {
                    Chapter prevChapter = chapterList.get(index - 1);
                    prev = prevChapter.getLastPage();
                    if (prev == null) {
                        prevChapter.setObservable(pageAdapter.currentIndex - 1, false);
                    }
                } else {
                    prev = new CoverPage(page, coverPath, screenWidth, screenHeight);
                }
            }
        }
        return prev;
    }

    private void jumpPageToProgress(Chapter currentChapter) {
        jumpPageToProgress(currentChapter, false);
    }

    /**
     * 跳转到特定进度
     * @param currentChapter
     * @param isLoadCover
     */
    private void jumpPageToProgress(Chapter currentChapter, boolean isLoadCover) {
        if (currentChapter == null) {
            dismissHUD();
            return;
        }
        if (!currentChapter.isChapterPageReady()) {
            // 不应该出现这种情况，这里为了避免出现空页面 --liqiang
            currentChapter.doPage();
        }

        //当前页面
        int currentPageIndex = getCurrentPageIndex(currentChapter);
        //页面内容
        Page page = currentChapter.getPage(currentPageIndex);

        if (isShowBookCover && isLoadCover) {
        	//封面
            page = new CoverPage(page, coverPath, screenWidth, screenHeight);
        }

        if (pageAdapter == null) {
            pageAdapter = new BookPagerAdapter();
            viewPager.setAdapter(pageAdapter);
        }

        if (isShowBookCover) {
            if (page instanceof CoverPage) {
                pageAdapter.isReachStartBoundary = true;
            }
        } else {
            if (currentChapter.isFirstChapter() && currentChapter.isFirstPage(page)) {
                pageAdapter.isReachStartBoundary = true;
            }
        }

        //当前Page
        pageAdapter.currentPage = page != null ? page : currentChapter.getLastPage();
        if (pageAdapter.currentPage != null) {
            pageAdapter.currentPage.buildPageContent();// 提前准备页面内容 --liqiang
        }
        if (pageAdapter.currentIndex <= 0) {
            pageAdapter.currentIndex = defaultPageAdapterIndex;// viewpage起始位置设置比较大是为了可以向前翻页
                                                                // --liqiang
        } else {
            // 这里加一个数值是为避免ViewPaper使用同一个的view来渲染，因为渲染速度很慢而且阻塞UI线程，这是系统的处理逻辑--liqiang
            pageAdapter.currentIndex += 100;
        }
        pageAdapter.pageMap.clear();
        pageAdapter.emptyViewMap.clear();
        viewPager.setCurrentItem(pageAdapter.currentIndex, false);
        if (isProgressChange) {
            isProgressChange = false;
            int pageNumber = currentChapter.getPageOffset(pageAdapter.currentPage);
            Intent intent = new Intent(ReadOverlayActivity.ACTION_CHANGE_PROGRESS_DONE);
            if (pageAdapter != null && pageAdapter.currentPage != null) {
                intent.putExtra(ReadOverlayActivity.BookMarkStateKey, pageAdapter.currentPage.getBookMark() != null);
            }
            intent.putExtra(ReadOverlayActivity.PageNumberKey, pageNumber);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        loadChapterAllNotes(currentChapter);
        if (bookId != 0) {
            TalkingDataUtil.onReadingEvent(this, "bookid:" + bookId);
        } else {
            TalkingDataUtil.onReadingEvent(this, "bookname:" + bookName);
        }

        dismissHUD();
    }

    /**
     * 根据progress标记找到上次看到的页面，先找匹配的，找不到再找最接近的
     * 
     * @param currentChapter
     *            当前章节
     * @return 页面在章节的第几页
     */
    private int getCurrentPageIndex(Chapter currentChapter) {
        Page page = null;
        for (int i = 0; i < currentChapter.getPageCount(); ++i) {
            page = currentChapter.getPage(i);
            if (page != null) {
                if (page.pageMatchTheIndex(progress.paraIndex, progress.offsetInPara)) {
                    return i;
                } else if (page.pageAfterTheIndex(progress.paraIndex, progress.offsetInPara)) {
                    if (i > 0) {
                        return i - 1;
                    } else {
                        return i;
                    }
                }
            }
        }
        return currentChapter.getPageCount() - 1;
    }

    /**
     * 核心：展示排版后的内容View的适配器
     */
    public class BookPagerAdapter extends PagerAdapter {
        Stack<Kit42View> viewsContainer = new Stack<Kit42View>();
        List<Kit42View> viewArray = new ArrayList<Kit42View>();
        HashMap<String, Page> pageMap = new HashMap<String, Page>();
        HashMap<String, Kit42View> emptyViewMap = new HashMap<String, Kit42View>();
        HashMap<String, Kit42View> pageViewMap = new HashMap<String, Kit42View>();
        Page currentPage = null;
        int currentIndex = 0;
        String movingNextItemRef = null;
        String movingPrevItemRef = null;
        boolean isReachStartBoundary = false;
        boolean isReachEndBoundary = false;
        boolean isMovingNext;
        Kit42View currentView;

        public BookPagerAdapter() {
            super();
            viewsContainer.push(createItemView());
            viewsContainer.push(createItemView());
            viewsContainer.push(createItemView());
            viewPager.setOnPageChangeListener(pageChangerListener);
        }
        
        OnPageChangeListener pageChangerListener = new OnPageChangeListener() {
            
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    for (Kit42View view : viewArray) {
                        view.hidePageShadow();
                        ViewHelper.setTranslationX(view, 0);
                    }
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (!isPageAnimationSlide) {
                    return;
                }
                Kit42View left = getViewByPosition(position);
                Kit42View right = getViewByPosition(position+1);
                if (right != null && positionOffsetPixels!= 0) {
                    ViewHelper.setTranslationX(right, -screenWidth+positionOffsetPixels);
                }
                if (left != null) {
                    if (positionOffsetPixels != 0) {
                        left.showPageShadow();
                    }
                    left.bringToFront();
                   
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (isReachStartBoundary && position < currentIndex) {
                    viewPager.setCurrentItem(currentIndex);
                    Toast.makeText(BookPageViewActivity.this, getString(R.string.reach_book_start_page), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isReachEndBoundary && position > currentIndex) {
                    viewPager.setCurrentItem(currentIndex);
                    showBackCover();
                    return;
                }
                Page page = pageMap.get(String.valueOf(position));
                if (isShowBookCover && page instanceof CoverPage) {
                    isReachStartBoundary = true;
                    currentIndex = position;
                    currentPage = page;
                    return;
                }
                if (page == null || page.getChapter() == null) {
                    viewPager.setCurrentItem(currentIndex);
                    return;
                }
                currentPage = page;
                currentIndex = position;

                isReachStartBoundary = false;
                isReachEndBoundary = false;
                Chapter currentChapter = currentPage.getChapter();
                if (!isShowBookCover) {
                    if (currentChapter.isFirstChapter()) {
                        isReachStartBoundary = currentChapter.isFirstPage(currentPage);
                    }
                }
                if (currentChapter.isLastChapter()) {
                    isReachEndBoundary = currentChapter.isLastPage(currentPage);
                }

                loadChapterAllNotes(currentChapter);

                // 朝着一个方向快速翻动时，减少重复的task执行，提高效率
                if (isMovingNext) {
                    if (!currentChapter.getSpine().spineIdRef.equals(movingNextItemRef)) {
                        movingPrevItemRef = null;
                        movingNextItemRef = currentChapter.getSpine().spineIdRef;
                        PrepareNextAndPrevChapterTask prepareTask = new PrepareNextAndPrevChapterTask(isMovingNext);
                        prepareTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentChapter);
                    }
                } else {
                    if (!currentChapter.getSpine().spineIdRef.equals(movingPrevItemRef)) {
                        movingNextItemRef = null;
                        movingPrevItemRef = currentChapter.getSpine().spineIdRef;
                        PrepareNextAndPrevChapterTask prepareTask = new PrepareNextAndPrevChapterTask(isMovingNext);
                        prepareTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentChapter);
                    }
                }

            }
        };

        /**
         * 创建页面内容
         * @return
         */
        private Kit42View createItemView() {
            Kit42View view = new Kit42View(BookPageViewActivity.this, BookPageViewActivity.this);
            if (LocalUserSetting.useSoftRender) {
                view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            viewArray.add(view);
            return view;
        }
        
        private void release() {
            for (Kit42View v : viewArray) {
                v.release();
            }
        }
        
        private void invalidatePage() {
            invalidatePage(true);
        }

        private void invalidatePage(boolean releasePageBitmap) {
            for (Kit42View v : viewArray) {
                v.refreshDownloadFontBar();
                if (releasePageBitmap) {
                	v.releasePageBitmap();
                }
                v.refreshSearchBar();
                v.invalidate();
            }
        }

        private void refreshPageContent() {
            for (Kit42View v : viewArray) {
                v.refreshImageBackground();
                v.refreshDownloadFontBar();
                v.releasePageBitmap();
                v.refreshSearchBar();
                v.invalidate();
            }
        }

        private void refreshPlayContrl() {
            for (Kit42View v : viewArray) {
                v.refreshPlayControl();
            }
        }

        public void refreshNote() {
            for (Kit42View v : viewArray) {
                v.releasePageBitmap();
                v.refreshNote();
                v.invalidate();
            }
        }

        public void refreshBookMark() {
            for (Kit42View v : viewArray) {
                v.releasePageBitmap();
                v.refreshBookMark();
                v.invalidate();
            }
        }

        public void toggleBookMark(Page page) {
            for (Kit42View v : viewArray) {
                if (v.isSamePage(page)) {
                    v.toggleBookMarkAndShowMe();
                    break;
                }
            }
        }

        public void loadErrorPage() {
            if (emptyViewMap.isEmpty()) {
                return;
            }
            BookPageViewActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Iterator<Kit42View> iterator = emptyViewMap.values().iterator();
                    while (iterator.hasNext()) {
                        iterator.next().setPage(null);
                    }
                }
            });
        }

        private void refreshPage(final String position, final Page page) {
            if (page == null) {
                return;
            }
            BookPageViewActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Kit42View view = emptyViewMap.get(position);
                    if (view != null) {
                        pageMap.put(String.valueOf(position), page);
                        page.buildPageContent();
                        view.setPage(page);
                    }
                }
            });
        }
        
        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            currentView = (Kit42View) object;
        }

        public Kit42View getPrimaryItem() {
            return currentView;
        }

        /**
         * 获取下一页的page
         * 
         * @return
         */
        public Page getNextPage(){
            Page page = pageMap.get(String.valueOf(currentIndex + 1));
            return page;
        }
        
        public void goToNext(boolean smoothScroll) {
            if (isReachEndBoundary) {
                showBackCover();
                return;
            }
            Page page = pageMap.get(String.valueOf(currentIndex + 1));
            if (page == null) {
                return;
            }
            if (isPageNoAnimation) {
                smoothScroll = false;
            }
            currentPage = page;
            if (currentPage instanceof FinishPage) {
                isReachEndBoundary = true;
            }
            currentIndex++;
            viewPager.setCurrentItem(currentIndex, smoothScroll);
        }

        public boolean isFirstOrFinishPage() {
            if (currentPage instanceof CoverPage || currentPage instanceof FinishPage) {
                return true;
            }
            if (currentPage != null && currentPage.getChapter() != null) {
                if (currentPage.getChapter().isFirstChapter()) {
                    return currentPage.getChapter().isFirstPage(currentPage);
                }
                if (currentPage.getChapter().isLastChapter()) {
                    return currentPage.getChapter().isLastPage(currentPage);
                }
            }
            return false;
        }

        public void goToPrev(boolean smoothScroll) {
            if (isReachStartBoundary) {
                Toast.makeText(BookPageViewActivity.this, getString(R.string.reach_book_start_page), Toast.LENGTH_SHORT).show();
                return;
            }
            Page page = pageMap.get(String.valueOf(currentIndex - 1));
            if (page == null) {
                return;
            }
            if (isPageNoAnimation) {
                smoothScroll = false;
            }
            currentPage = page;
            if (isShowBookCover) {
                if (currentPage instanceof CoverPage) {
                    isReachStartBoundary = true;
                }
            } else {
                if (!(currentPage instanceof FinishPage)) {
                    if (currentPage.getChapter().getPageOffset(currentPage) == 0) {
                        isReachStartBoundary = true;
                    }
                }
            }
            currentIndex--;
            viewPager.setCurrentItem(currentIndex, smoothScroll);
        }
        
        public Kit42View getViewByPosition(int position) {
            if (pageViewMap != null) {
                return pageViewMap.get(String.valueOf(position));
            }
            return null;
        }

        /**
         * 滑动切换的时候销毁当前的组件
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
            ((Kit42View) object).releasePageBitmap();
            viewsContainer.push((Kit42View) object);
            pageMap.remove(String.valueOf(position));
            pageViewMap.remove(String.valueOf(position));
            emptyViewMap.remove(String.valueOf(position));
        }

        /**
         * 每次滑动的时候生成的组件
         * 核心：生成页面内容
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Kit42View view = null;
            if (viewsContainer.isEmpty()) {
                view = createItemView();
            } else {
                view = viewsContainer.pop();
            }
            if (view.getParent() != null) {
                ((ViewPager) view.getParent()).removeView(view);
            }
            
            //加载页面
            Page page = loadBookPage(currentPage, currentIndex, position);
            pageMap.put(String.valueOf(position), page);
            pageViewMap.put(String.valueOf(position), view);
            if (page == null) {
                emptyViewMap.put(String.valueOf(position), view);
            } else {
                page.buildPageContent();//核心
            }
            view.setPage(page);
            ((ViewPager) container).addView(view);
            return view;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        /**
         * 页码总数，也是ViewPage的总数
         */
        @Override
        public int getCount() {
            return bookPageCount;//页码总数
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

    }

    /**
     * 加载页面内容
     * @param currentPage
     * @param currentIndex
     * @param bookPageIndex
     * @return
     */
    private Page loadBookPage(Page currentPage, int currentIndex, int bookPageIndex) {
        MZLog.d("qli", "===qli===  loadBookPage ");
        if (bookPageIndex - currentIndex > 0) {
            pageAdapter.isMovingNext = true;
            if (bookPageIndex - currentIndex > 1) {
                currentPage = loadBookPage(currentPage, currentIndex, bookPageIndex - 1);
            }
            //下一页
            return this.getNextPage(currentPage);
        }
        if (bookPageIndex - currentIndex < 0) {
            pageAdapter.isMovingNext = false;
            if (bookPageIndex - currentIndex < -1) {
                currentPage = loadBookPage(currentPage, currentIndex, bookPageIndex + 1);
            }
            //上一页
            return this.getPrevPage(currentPage);
        }
        return currentPage;
    }

    private void loadNextChapter(Chapter current) {
        Chapter nextChapter = getNextChapter(current);
        if (nextChapter != null) {
            Chapter prevChapter = getPrevChapter(current);
            if (prevChapter != null) {
                Chapter prevPrevChapter = getPrevChapter(prevChapter);
                if (prevPrevChapter != null) {
                    if (prevChapter.getPageCount() >= MIN_PAGE_COUNT) {
                        prevPrevChapter.clearPage();// 清理上上一章节的页面
                    }
                    Chapter ppprevChapter = getPrevChapter(prevPrevChapter);
                    if (ppprevChapter != null) {
                        ppprevChapter.clearPage();// 清理上上上一章节的页面
                    }
                }
            }

            Page page = nextChapter.getFirstPage();
            if (page == null) {
                nextChapter.doPage();// 加载下一章节的页面
            }
            Chapter nextNextChapter = getNextChapter(nextChapter);
            if (nextNextChapter != null) {
                if (nextChapter.getPageCount() < MIN_PAGE_COUNT) {
                    page = nextNextChapter.getFirstPage();
                    if (page == null) {
                        nextNextChapter.doPage();// 加载下下一章节的页面
                    }
                }
            }
        }
    }

    private void loadPrevChapter(Chapter current) {
        Chapter prevChapter = getPrevChapter(current);
        if (prevChapter != null) {
            Chapter nextChapter = getNextChapter(current);
            if (nextChapter != null) {
                Chapter nextNextChapter = getNextChapter(nextChapter);
                if (nextNextChapter != null) {
                    if (nextChapter.getPageCount() >= MIN_PAGE_COUNT) {
                        nextNextChapter.clearPage();// 清理下下一章节的页面
                    }
                    Chapter nnnextChapter = getNextChapter(nextNextChapter);
                    if (nnnextChapter != null) {
                        nnnextChapter.clearPage();// 清理下下下一章节的页面
                    }
                }
            }

            Page page = prevChapter.getFirstPage();
            if (page == null) {
                prevChapter.doPage();// 加载上一章节的页面
            }
            Chapter prevPrevChapter = getPrevChapter(prevChapter);
            if (prevPrevChapter != null) {
                if (prevChapter.getPageCount() < MIN_PAGE_COUNT) {
                    page = prevPrevChapter.getFirstPage();
                    if (page == null) {
                        prevPrevChapter.doPage();// 加载上上一章节的页面
                    }
                }
            }
        }
    }

    public class PrepareNextAndPrevChapterTask extends AsyncTask<Chapter, Void, Void> {

        boolean isMovingNext = false;

        PrepareNextAndPrevChapterTask(boolean isMovingNext) {
            this.isMovingNext = isMovingNext;
        }

        @Override
        protected Void doInBackground(Chapter... paths) {
            if (isMovingNext) {
                loadNextChapter(paths[0]);
            } else {
                loadPrevChapter(paths[0]);
            }
            return null;
        }
    }

    private void confirmCoverPage(String path) {
        if (!TextUtils.isEmpty(path) && eBook == null) {
            Bitmap bitmap = ImageUtils.getBitmapFromNamePath(path, screenWidth, screenHeight);
            if (bitmap != null && !bitmap.isRecycled()) {
                isShowBookCover = true;
                coverPath = path;
                MZLog.d("J", coverPath);
                bitmap.recycle();
            }
        }
    }

    private String getBookCoverPath() {
        ContentReader reader = null;
        try {
            if (!TextUtils.isEmpty(path)) {
                reader = new ContentReader(path);
                return reader.getCoverPath();
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 1、加载中动画<br />
     * 2、ContentReader解析epub内容
     */
    public class BuildChapterTask extends AsyncTask<String, Void, List<Chapter>> {

    	/**
    	 * 加载中动画
    	 */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();//显示加载动画
        }

        /**
         * 解析章节信息<br />
         * 1、解析ePub文件信息（若没有解压，则进行解压）<br />
         * 2、ContentReader解析ePub文件内容<br />
         * 3、组织ePub相关的通用CSS信息<br />
         * 4、将解析得到的数据放到当前实例变量中<br />
         * 5、组装章节列表<br />
         * 6、调用onPostExecute<br />
         * return 返回章节信息列表
         */
        @Override
        protected List<Chapter> doInBackground(String... paths) {
            String epubDir = paths[0];
            //章节列表
            ArrayList<Chapter> chapters = new ArrayList<Chapter>();
            try {
            	//检查epub文件是否存在，不存在则解压
                if(!checkEpubFile(epubDir)){
                	return chapters;
                }

                //核心：调用，解析ePub文件
                ContentReader.isNeedJDDecrypt = eBook != null;
                ContentReader reader = new ContentReader(epubDir);
                confirmCoverPage(reader.getCoverPath());
                //处理样式信息
                CSSCollection globalCSS = processGlobalCSS(reader);

                //将解析得到的数据放到当前实例变量中
                tocList = reader.getTOCList();
                playList = reader.getPlayList();
                audioPath = reader.getAutioPath();
                //组装章节列表
                chapters = buildChapterList(reader, globalCSS);
                //进入异步任务的onPostExecute
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } 
            return chapters;
        }

		/**
		 * 异步任务扫尾<br />
		 * 1、检查章节列表信息是否正确（结果来源于doInBackground）<br />
		 * 2、获赠书籍显示赠言扉页<br />
		 * 3、打开书异步任务<br />
		 * 加载下一章节异步任务<br />
		 */
        @Override
        protected void onPostExecute(List<Chapter> result) {
            super.onPostExecute(result);

            if (isCancelled() || isExitBookPage) {
                return;
            }

            chapterList = result;
            //检查章节列表信息是否正确（结果来源于doInBackground）
            if (chapterList == null || chapterList.isEmpty()) {
                SettingUtils.getInstance().putBoolean("file_error:" + bookId, true);
                ToastUtil.showToastInThread(getString(R.string.file_read_fail), Toast.LENGTH_SHORT);
                EBookAnimationUtils anim = BookcaseLocalFragmentNewUI.getEBookAnimationUtils();
            	if (null != anim) {
            		anim.hideWindow();
            	}
            	finish();
//                exit();
                return;
            }

            boolean isLoadCover = false;
            if (progress != null && TextUtils.isEmpty(progress.chapterItemRef) && chapterList.get(0) !=null && chapterList.get(0).getSpine() != null) {
                isLoadCover = true;
                progress.chapterItemRef = chapterList.get(0).getSpine().spineIdRef;
            }

            //获赠书籍显示赠言扉页
            getReceiveBookInfos();

            //打开书异步任务
            openBookTask = new OpenBookTask();
            openBookTask.isLoadCoverPage = isLoadCover;
            openBookTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            //加载笔记信息异步任务
            PrepareChapterDataTask prepareTask = new PrepareChapterDataTask();
            prepareTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            //加载笔记广播
            Intent intent = new Intent(ACTION_PULL_NOTE);
            LocalBroadcastManager.getInstance(BookPageViewActivity.this).sendBroadcast(intent);
        }
        
        /**
         * 检查epub文件是否存在，不存在则解压
         * @param epubDir epub文件所在目录
         * @return
         * @throws FileNotFoundException
         * @throws IOException
         */
		private boolean checkEpubFile(String epubDir)
				throws FileNotFoundException, IOException {
			File file = new File(epubDir + "/META-INF/container.xml");
			if (!file.exists()) {
			    String unZipDir = new File(epubDir).getParentFile().getAbsolutePath() + "/content";
			    BufferedInputStream zipInputStream = null;
			    if (eBook == null && document != null) {
			        zipInputStream = new BufferedInputStream(new FileInputStream(new File(document.bookSource)));
			        document.bookPath = unZipDir;
			        MZBookDatabase.instance.updateDocument(document);
			    } else if (eBook != null && document == null) {
			        LocalBook book = LocalBook.getLocalBook(eBook.bookId, LoginUser.getpin());
			        zipInputStream = new BufferedInputStream(new FileInputStream(new File(book.book_path)));
			    } else {
			        zipInputStream = null;
			    }

			    if (null != zipInputStream) {
			        Unzip.unzip(zipInputStream, unZipDir);
			    } else{
			    	return false;
			    }
			}
			return true;
		}

        /**
         * 处理全局CSS信息
         * @param reader
         * @return
         */
		private CSSCollection processGlobalCSS(ContentReader reader) {
			List<String> cssList = new ArrayList<String>();
			CSSCollection globalCSS;//之所以叫global原因是，epub中很多章节，每个章节都是一个HTML，所以这个是全局的样式
			
			boolean isLoadMZBookCSS = false;
			for (String path : reader.getCssList()) {
			    if (path.contains("mzread~iphone.css")) {
			        cssList.add(path);
			        isLoadMZBookCSS = true;
			    } else if (path.contains("main~iphone.css")) {
			        cssList.add(path);
			    }
			}

			if (cssList.size() == 0) {
			    cssList.addAll(reader.getCssList());
			}

			if (isLoadMZBookCSS) {
			    String cssFile = "css/main~android.css";
			    if (eBook == null && document != null) {
			        cssFile = "css/external~android.css";
			    }
			    InputStream cssis = null;
			    try{
			    	cssis = getResources().getAssets().open(cssFile);                    	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }finally{
			    	IOUtil.closeStream(cssis);
			    }
			    
			    globalCSS = new CSSCollection(reader.getCssPath(), cssis);
			} else {
			    globalCSS = new CSSCollection(reader.getCssPath(), "");
			}

			InputStream coloris = null;
			try{
				 coloris = getResources().getAssets().open("css/css-color");
			     globalCSS.setColorJson(coloris);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
			    IOUtil.closeStream(coloris);
			}
            
			for (String path : cssList) {//CSS列表
				InputStream bookCssIs = null;
				try{
					 path = URLDecoder.decode(path, "GB2312");
			         if (eBook != null) {
			             bookCssIs = JDDecryptUtil.decryptFile(path);
			         } else if (document != null) {
			             bookCssIs = new FileInputStream(path);
			         } else {
			             break;
			         }

			         CSSCollection bookCSS = new CSSCollection(globalCSS.getCssPath(), bookCssIs);
			         globalCSS.merge(bookCSS);
			         IOUtil.closeStream(bookCssIs);
				}catch(Exception e){
					e.printStackTrace();
				}finally {
			        IOUtil.closeStream(bookCssIs);
			    }
			   
			}
			globalCSS.findAllFont();//所有涉及的字体
			return globalCSS;
		}

        /**
         * 使用SpineList组装章节列表
         * @param reader ePub内容解析器
         * @param globalCSS ePub全局样式
         * @return 章节列表
         */
		private ArrayList<Chapter> buildChapterList(ContentReader reader, CSSCollection globalCSS) {
			ArrayList<Chapter> chapters = new ArrayList<Chapter>();
			//文字等级设置信息
			int textSizeLevel = LocalUserSetting.getTextSizeLevel(BookPageViewActivity.this);
            float pixel = textSizeLevelSet[textSizeLevel] * density;
            //行间距，在此处设置
            float lineSpace = pixel * PageLineSpace;
            //段间距
            float blockSpace = pixel * PageBlockSpace;
            long bookId = 0;
            int docId = 0;
            if (eBook != null) {
                bookId = eBook.bookId;
            }

            if (document != null) {
                docId = document.documentId;
            }
			PageContext pageContext = new PageContext(BookPageViewActivity.this, pixel, lineSpace, blockSpace, MZBookApplication.getInstance().getHyphen());
			Chapter prev = null;
			
			//通过OPF文件Spine节点信息来组织章节列表信息
			for (Spine spineItem : reader.getSpineList()) {
			    if (isCancelled()) {
			        break;
			    }
			    //排版计算机
			    PageCalculator cal = new PageCalculator(pageWidth, pageHeight, pageContext, globalCSS, textPaint);
			    Chapter chapter = new Chapter(cal, spineItem, key, pagePool, tocList);
			    //当前章节所在顺序
			    chapter.setChapterIndex(chapters.size());
			    chapter.addObserver(pageLoadObserver);
			    chapter.setBookId(bookId, docId);
			    //是否展示全部笔记
			    chapter.setShowAllNotes(isShowAllNotes);
			    chapter.setPrevChapter(prev);
			    chapters.add(chapter);
			    prev = chapter;
			}

			if (chapters.size() > 0) {
			    chapters.get(0).setFirstChapter(true);
			    chapters.get(chapters.size() - 1).setLastChapter(true);
			}
			return chapters;
		}

        /**
         * 获赠书籍显示赠言扉页
         */
		private void getReceiveBookInfos() {
			if(isFinishing()){
				return;
			}
			if(eBook!=null && (TextUtils.isEmpty(eBook.source)||eBook.source.equals(LocalBook.SOURCE_BUYED_BOOK))  && LoginUser.isLogin()){
                List<SendBookReceiveInfo> list = LocalUserSetting.getSendBookReceiveInfos(BookPageViewActivity.this);
                SendBookReceiveInfo info= null;
                boolean haveSendbookInfo = false;
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        info = list.get(i);
                        if (info!= null && !TextUtils.isEmpty(info.ebookId) && !TextUtils.isEmpty(info.userPin) && LoginUser.isLogin()
                                && info.ebookId.equals(String.valueOf(eBook.bookId)) && LoginUser.getpin().equals(info.userPin)) {
                            haveSendbookInfo =true;
                            break;
                        }
                    }
                }
                if(haveSendbookInfo){
                    checkIsFirstPage(info);
                }
                else{
                    if(NetWorkUtils.isNetworkConnected(BookPageViewActivity.this)){
                        WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool.getReceiveInfoParams(String.valueOf(eBook.bookId)),
                                false, new MyAsyncHttpResponseHandler(BookPageViewActivity.this) {

                                    @Override
                                    public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                                    }

                                    @Override
                                    public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
                                        JSONObject jsonObj;
                                        try {
                                            jsonObj = new JSONObject(new String(responseBody));
                                            if (jsonObj != null) {
                                                String code = jsonObj.optString("code");
                                                if ("0".equals(code)) {
                                                    SendBookReceiveInfo info = GsonUtils.fromJson(jsonObj.toString(),
                                                            SendBookReceiveInfo.class);
                                                    if (info != null) {
                                                        info.ebookId = String.valueOf(eBook.bookId);
                                                        info.userPin = LoginUser.getpin();
                                                    }
                                                    if (isFinishing() || info==null)
                                                        return;
                                                    List<SendBookReceiveInfo> list = LocalUserSetting.getSendBookReceiveInfos(BookPageViewActivity.this);
                                                    list.add(info);
                                                    SendBookReceiveInfos infos = new SendBookReceiveInfos();
                                                    infos.infos = list;
                                                    LocalUserSetting.saveSendBookReceiveInfos(BookPageViewActivity.this, infos);
                                                    checkIsFirstPage(info);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                }
            }
		}
    }

    /**
     * 检查当前进度是否该书第一页，若是，则判断是否有赠言页需要显示
     */
    private void checkIsFirstPage(SendBookReceiveInfo info){
        String firstChapterTitle = null,nvaLable = null;
        if(tocList!=null && tocList.size() > 0 ){
            String firstChapter = tocList.get(0).contentSrc;
            nvaLable = tocList.get(0).navLabel;
            if(!TextUtils.isEmpty(firstChapter)){
                String[] arr = firstChapter.split("/");
                firstChapterTitle= arr[arr.length-1];
            }
        }
        
        if(!TextUtils.isEmpty(firstChapterTitle)){
            String[] arra= firstChapterTitle.split("\\.");
            firstChapterTitle = arra[0];
        }
        
        if(progress!=null && progress.paraIndex == 0 && info !=null){
            String itemRef = progress.chapterItemRef;
            if(!TextUtils.isEmpty(itemRef)){
                String[] arra= itemRef.split("\\.");
                itemRef = arra[0];
            }
            if(itemRef != null && nvaLable !=null && (!itemRef.equals(firstChapterTitle) && !nvaLable.equals(progress.chapterTitle))){
            }else{
                Intent firstpageIntent = new Intent(BookPageViewActivity.this,SendBookFirstPageActivity.class);
                firstpageIntent.putExtra("sendNickName", info.sendNickName);
                firstpageIntent.putExtra("sendMsg", info.sendMsg);
                startActivityForResult(firstpageIntent,SendBookFirstPageActivity.SENDBOOKFIRSTPAGE);
                overridePendingTransition(R.anim.alpha_in, 0);
            }
        }
    }
    
    @Override
    public Chapter getNextChapter(Chapter chapter) {
        if (chapter == null) {
            return null;
        }

        int index = chapterList.indexOf(chapter);
        if (index == chapterList.size() - 1 || index == -1) {
            return null;
        }

        return chapterList.get(index + 1);
    }

    @Override
    public Chapter getPrevChapter(Chapter chapter) {
        if (chapter == null) {
            return null;
        }

        int index = chapterList.indexOf(chapter);
        if (index == 0 || index == -1) {
            return null;
        }

        return chapterList.get(index - 1);
    }

    private void showAllJDBookMark() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                List<BookMark> bookMarks = null;
                if (eBook != null) {
                    bookMarks = MZBookDatabase.instance.getAllBookMarksOfBook(userId, eBook.bookId, 0);
                } else if (document != null) {
                    bookMarks = MZBookDatabase.instance.getAllBookMarksOfBook(userId, 0, document.documentId);
                }
                ArrayList<String> markChapterList = new ArrayList<String>();
                for (BookMark mark : bookMarks) {
                    String chapterRef = setupJDBookMarkChapterRef(mark);
                    if (!TextUtils.isEmpty(chapterRef)) {
                        MZBookDatabase.instance.updateJDBookMark(mark);
                        markChapterList.add(chapterRef);
                    } else {
                        markChapterList.add(mark.chapter_itemref);
                    }
                }
                Intent intent = new Intent(BookMarksFragment.ACTION_RELOAD_BOOKMARK);
                intent.putExtra(BookMarksFragment.CHAPTER_ITEM_REF, markChapterList);
                LocalBroadcastManager.getInstance(BookPageViewActivity.this).sendBroadcast(intent);
            }
        }).start();

    }

    private String setupJDBookMarkChapterRef(BookMark mark) {
        if (TextUtils.isEmpty(mark.chapter_itemref)) {
            int paraIndex = mark.para_index;
            for (Chapter chapter : chapterList) {
                if (paraIndex < chapter.getBlockCount()) {
                    mark.chapter_itemref = chapter.getSpine().spineIdRef;
                    mark.chapter_title = chapter.getPageHead("");
                    mark.para_index = paraIndex;
                    mark.isSync = 0;
                    mark.operation_state = 1;
                    return mark.chapter_itemref;
                } else {
                    paraIndex -= chapter.getBlockCount();
                }
            }
            Chapter lastChapter = chapterList.get(chapterList.size() - 1);
            mark.chapter_itemref = lastChapter.getSpine().spineIdRef;
            mark.chapter_title = lastChapter.getPageHead("");
            mark.para_index = lastChapter.getBlockCount() - 1;
            mark.offset_in_para = 0;
            isJDProgress = false;
            isGotoJDProgress = false;
            return mark.chapter_itemref;
        }
        return null;
    }

    private void setupJDProgressChapterRef(ReadProgress progress) {
        if (TextUtils.isEmpty(progress.chapterItemRef)) {
            int paraIndex = progress.paraIndex;
            for (Chapter chapter : chapterList) {
                if (paraIndex < chapter.getBlockCount()) {
                    progress.chapterItemRef = chapter.getSpine().spineIdRef;
                    progress.paraIndex = paraIndex;
                    isJDProgress = false;
                    isGotoJDProgress = false;
                    return;
                } else {
                    paraIndex -= chapter.getBlockCount();
                }
            }
            Chapter lastChapter = chapterList.get(chapterList.size() - 1);
            progress.chapterItemRef = lastChapter.getSpine().spineIdRef;
            progress.paraIndex = lastChapter.getBlockCount() - 1;
            progress.offsetInPara = 0;
            isJDProgress = false;
            isGotoJDProgress = false;
        }
    }

    private void gotoJDProgressPage() {
        // 处理京东老版本的进度
        setupJDProgressChapterRef(progress);
        GoToProgressTask task = new GoToProgressTask();
        task.execute("");
    }

    public class OpenBookTask extends AsyncTask<Void, Void, Void> {

        private boolean isCurrentChapterChangedFont = false;// 修改字体大小的特殊逻辑，true表示不再对当前章节做变字体大小相关逻辑，否则false
        private boolean isChangedFontFace = false;// 修改字体
        private boolean isChangedReadSpace = false;// 修复间距
        private boolean isLoadCoverPage = false;
        private Chapter currentChapter;

        public OpenBookTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prepareTask();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (isExitBookPage) {
                return null;
            }

            //若有PageCountTask正在运行的话，需要等待其完成
            checkPageCountTask();

            int changedFontIndex = -1;
            if (isCurrentChapterChangedFont || isChangedReadSpace) {
                changedFontIndex = chapterList.indexOf(currentChapter);
            }

            boolean isPageEdgeWidthChanged = localPageEdgeSpace != getPageMarginLeft();
            //文字大小设置
            int textSizeLevel = LocalUserSetting.getTextSizeLevel(BookPageViewActivity.this);
            //最终显示文字字号大小
            int textSize = textSizeLevelSet[textSizeLevel];
            float pixel = textSize * getResources().getDisplayMetrics().density;
            //行间距？
            int lineSpace = (int) (pixel * getPageLineSpace());
            boolean isLineSpaceChanged = localLineSpace != lineSpace;
            //段间距?
            int blockSpace = (int) (pixel * getPageBlockSpace());
            boolean isBlockSpaceChanged = localBlockSpace != blockSpace;
            chapterIni(changedFontIndex, textSizeLevel, pixel);
            
            //处理缓存的分页信息
            checkPagedCach(isPageEdgeWidthChanged, textSize, isLineSpaceChanged, isBlockSpaceChanged);

            //如果分页未完成，pagecontent里的数据都不能用，会导致页面排版混乱，而且影响目录页数的计算
            if (!isPageFinished) {
                deletePageContent();
                for (TOCItem tocItem : tocList) {
                    tocItem.pageNumber = 0;
                }
            }

            if (isExitBookPage) {
                return null;
            }
            //核心：执行分页
            currentChapter.doPage();
            if (prepareNextPrevTask != null) {
                prepareNextPrevTask.cancel(true);
            }
            int currentPageIndex = getCurrentPageIndex(currentChapter);
            //准备下一个章节内容
            prepareNextPrevTask = new PrepareNextPrevChapterInBackground(currentPageIndex, currentChapter);
            prepareNextPrevTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return null;
        }

		
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (isExitBookPage) {
                return;
            }
            if (isCancelled()) {
                // mLoadingDailog.dismiss();
                dismissHUD();
                return;
            }

            MZLog.d("qli", "===qli=== OpenBookTask: ");
            jumpPageToProgress(currentChapter, isLoadCoverPage);

            if (eBook != null && !serverProgressSynced) {
                syncReadProgressAndBookMark();
            }
            if (document != null) {
                if (docBind != null) {
                    if (!serverProgressSynced) {
                        syncReadProgressAndBookMark();
                    }
                }
            }

            if (!isBookOpenError && !isPageFinished) {
                pageCountTask = new PageCountTask();
                pageCountTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            if (isJDProgress) {
                DialogManager.showCommonDialog(BookPageViewActivity.this, "提示", getString(R.string.progress_continue_alert), "确定", "取消",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    showLoading();
                                    isGotoJDProgress = true;
                                    if (!TextUtils.isEmpty(localChapterBlockIndex)) {
                                        gotoJDProgressPage();
                                    }
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    isJDProgress = false;
                                    isGotoJDProgress = false;
                                    break;
                                default:
                                    break;
                                }
                                dialog.dismiss();
                            }
                        });
            }
        }

        private void chapterIni(int changedFontIndex, int textSizeLevel, float pixel) {
			for (int i = 0, n = chapterList.size(); i < n; i++) {
                Chapter chapter = chapterList.get(i);
                if (changedFontIndex == i) {
                    chapter.setChapterPageOffset(-1);
                    chapter.setBookPageCount(-1);
                } else {
                    chapter.reset();
                }
                //字体路径
                chapter.setFontPath(fontPath);
                if (isChangedFontFace) {
                    chapter.setFontFace(fontDefault);
                }
                if (isChangedReadSpace) {
                    chapter.setPageWidth(pageWidth);
                    chapter.setPageHeight(pageHeight);
                }
                chapter.setBaseTextSize(pixel);
                chapter.setTextSizeLevel(textSizeLevel);
            }
		}

		private void checkPagedCach(boolean isPageEdgeWidthChanged, int textSize, boolean isLineSpaceChanged,
				boolean isBlockSpaceChanged) {
			if (localTextSize == textSize && !isLineSpaceChanged && !isBlockSpaceChanged && !isPageEdgeWidthChanged
                    && !TextUtils.isEmpty(localChapterPageIndex) && !TextUtils.isEmpty(localTocPageIndex)) {
                String[] chapterPage = localChapterPageIndex.split(";");
                String[] chapterBlock = localChapterBlockIndex.split(";");
                if (chapterPage.length == chapterList.size()) {
                    int chapterIndex = 0;
                    int chapterOffset = 0;
                    for (Chapter chapter : chapterList) {
                        int pageCount = Integer.parseInt(chapterPage[chapterIndex]);
                        int blockCount = Integer.parseInt(chapterBlock[chapterIndex]);
                        chapter.setPageCount(pageCount);
                        chapter.setChapterPageOffset(chapterOffset);
                        chapter.setBlockCount(blockCount);
                        chapterOffset += pageCount;
                        chapterIndex++;
                    }
                    if (chapterOffset > 0) {
                        for (Chapter chapter : chapterList) {
                            chapter.setBookPageCount(chapterOffset);
                        }
                        isPageFinished = true;
                    } else {
                        for (Chapter chapter : chapterList) {
                            chapter.reset();
                        }
                    }
                }

                if (isPageFinished) {
                    String[] tocPage = localTocPageIndex.split(";");
                    if (tocPage.length == tocList.size()) {
                        int tocPageIndex = 0;
                        for (TOCItem tocItem : tocList) {
                            if(tocPageIndex >= tocList.size())
                                break;
                            try {
                                if(tocPageIndex < tocPage.length) {
                                    tocItem.pageNumber = Integer.valueOf(tocPage[tocPageIndex]);
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            tocPageIndex++;
                        }
                    }
                }
                showAllJDBookMark();
            } else if (!TextUtils.isEmpty(localChapterBlockIndex)) {
                int chapterIndex = 0;
                String[] chapterBlock = localChapterBlockIndex.split(";");
                for (Chapter chapter : chapterList) {
                    if(chapterIndex >= chapterList.size())
                        break;
                    int blockCount = 0;
                    try {
                        blockCount = Integer.parseInt(chapterBlock[chapterIndex]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    chapter.setBlockCount(blockCount);
                    chapterIndex++;
                }
                showAllJDBookMark();
            }
		}
        
        private void prepareTask() {
			isPageFinished = false;

            if (!isCurrentChapterChangedFont) {
                showLoading();//显示加载动画
            }

            if (pageCountTask != null) {
                pageCountTask.cancel(true);
            }

            if(chapterList.size() > 0) {
                currentChapter = chapterList.get(0);
            }

            for (Chapter chapter : chapterList) {
            	//找出当前章节
                if (chapter.getSpine().spineIdRef.equals(progress.chapterItemRef)) {
                    currentChapter = chapter;
                    break;
                }
            }
		}

        /**
         * 检查异步任务PageCountTask是否在运行，若在运行则需要等待其执行完毕
         */
		private void checkPageCountTask() {
			if (pageCountTask != null) {
                while (pageCountTask.getStatus() != AsyncTask.Status.FINISHED) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
		}
    }

    private void deletePageContent() {
        long bookId = 0;
        int docId = 0;
        if (eBook != null) {
            bookId = eBook.bookId;
        }
        if (document != null) {
            docId = document.documentId;
        }
        MZBookDatabase.instance.deletePageContent(bookId, docId);
    }

    private void requestDocumentServerIDTask() {
        if (isExitBookPage) {
            return;
        }
        if (!LoginUser.isLogin()) {
            return;
        }
        if (docBind == null || docBind.serverId == 0) {
            if (!NetWorkUtils.isNetworkConnected(BookPageViewActivity.this)) {
                return;
            }
            RequestParams request = RequestParamsPool.getDocBindParams(document.title, document.opfMD5);
            WebRequestHelper.post(URLText.synServerId, request, true, new MyAsyncHttpResponseHandler(BookPageViewActivity.this) {

                @Override
                public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
                    if (isExitBookPage) {
                        return;
                    }
                    try {
                        docBind = new DocBind();
                        JSONObject obj = new JSONObject(new String(responseBody));
                        docBind.documentId = document.documentId;
                        docBind.userId = userId;
                        docBind.serverId = obj.getLong("document_id");
                        docBind.bind = 0;
                        JSONObject book = obj.optJSONObject("book");
                        if (book != null) {
                            docBind.bookId = book.optLong("id");
                            docBind.serverTitle = book.optString("name");
                            docBind.serverAuthor = book.optString("author_name");
                            docBind.serverCover = book.optString("cover");
                            // 服务器返回数据 说明服务器已经存在该书籍 所以直接绑定
                            if (docBind.bookId != 0)
                                docBind.bind = 1;
                        }
                        MZBookDatabase.instance.insertOrUpdateDocBind(docBind);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

                }
            });
        }
    }

    public class ChangeFontSizeTask extends AsyncTask<Integer, Void, Void> {

        private Chapter currentChapter;

        @Override
        protected void onPreExecute() {
            try {
                super.onPreExecute();
                showLoading();
                currentChapter = chapterList.get(0);

                for (Chapter chapter : chapterList) {
                    if (chapter.getSpine().spineIdRef.equals(progress.chapterItemRef)) {
                        currentChapter = chapter;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Integer... param) {
            try {
                int fontSize = param[0];
                int userFontSize = LocalUserSetting.getTextSizeLevel(BookPageViewActivity.this);

                if (fontSize == -1) {
                    fontSize = userFontSize;
                }

                int textSize = textSizeLevelSet[fontSize];
                float pixel = textSize * getResources().getDisplayMetrics().density;
                currentChapter.clearPage();
                currentChapter.setBaseTextSize(pixel);
                currentChapter.setTextSizeLevel(fontSize);
                currentChapter.doPage();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                super.onPostExecute(result);
                jumpPageToProgress(currentChapter);

                Intent intent = new Intent(ReadOverlayActivity.ACTION_FONT_CHANGE_DONE);
                LocalBroadcastManager.getInstance(BookPageViewActivity.this).sendBroadcast(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public class ChangeReadSpaceTask extends AsyncTask<Void, Void, Void> {

        private Chapter currentChapter;

        @Override
        protected void onPreExecute() {
            try {
                super.onPreExecute();
                showLoading();
                currentChapter = chapterList.get(0);

                for (Chapter chapter : chapterList) {
                    if (chapter.getSpine().spineIdRef.equals(progress.chapterItemRef)) {
                        currentChapter = chapter;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... param) {
            try {
                int userFontSize = LocalUserSetting.getTextSizeLevel(BookPageViewActivity.this);
                int textSize = textSizeLevelSet[userFontSize];
                float pixel = textSize * getResources().getDisplayMetrics().density;
                currentChapter.clearPage();
                currentChapter.setBaseTextSize(pixel);
                currentChapter.setPageWidth(pageWidth);
                currentChapter.setPageHeight(pageHeight);
                currentChapter.doPage();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                super.onPostExecute(result);
                jumpPageToProgress(currentChapter);
                Intent intent = new Intent(ReadOverlayActivity.ACTION_CHANGE_READ_SPACE_DONE);
                LocalBroadcastManager.getInstance(BookPageViewActivity.this).sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 打开图书流程<br />
     * 1、展示加载进度（翻书动画）
     * 2、获取出当前章节<br />
     * 3、对当前章节进行排版分页<br />
     * 3、启动异步任务PrepareNextPrevChapterInBackground
     *
     */
    public class GoToProgressTask extends AsyncTask<String, Void, Void> {

        private Chapter currentChapter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            BookPageViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLoading();
                }
            });

            currentChapter = chapterList.get(0);
            for (Chapter chapter : chapterList) {
                if (chapter.getSpine().spineIdRef.equals(progress.chapterItemRef)) {
                    currentChapter = chapter;
                    break;
                }
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            String id = params[0];
            //核心方法调用分页排版
            currentChapter.doPage();
            if (!TextUtils.isEmpty(id)) {
                progress.paraIndex = currentChapter.getIdLocation(id);
            }
            if (prepareNextPrevTask != null) {
                prepareNextPrevTask.cancel(true);
            }
            //准备加载下一个章节
            int currentPageIndex = getCurrentPageIndex(currentChapter);
            prepareNextPrevTask = new PrepareNextPrevChapterInBackground(currentPageIndex, currentChapter);
            prepareNextPrevTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (isExitBookPage) {
                return;
            }
            if (isCancelled()) {
                dismissHUD();
                return;
            }
            jumpPageToProgress(currentChapter);
        }
    }

    public class GoToPageTask extends AsyncTask<Void, Void, Void> {

        private Chapter currentChapter;
        private int pageNumber;

        public GoToPageTask(int pageNumber, Chapter currentChapter) {
            this.pageNumber = pageNumber;
            this.currentChapter = currentChapter;
            backProgress = progress.clone();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }

        @Override
        protected Void doInBackground(Void... params) {
            currentChapter.doPage();
            int pageIndex = pageNumber - currentChapter.getChapterPageOffset();
            if (prepareNextPrevTask != null) {
                prepareNextPrevTask.cancel(true);
            }
            prepareNextPrevTask = new PrepareNextPrevChapterInBackground(pageIndex, currentChapter);
            prepareNextPrevTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dismissHUD();
            if (isExitBookPage) {
                return;
            }
            if (isCancelled()) {
                return;
            }
            
            if(null == currentChapter) {
                return;
            }

            Page page = currentChapter.getPage(pageNumber - currentChapter.getChapterPageOffset());
            // 这里极端情况是从最后一页拖到第一页会使用同一个View来渲染，渲染速度很慢而且阻塞UI线程，这是系统的处理逻辑
            // 所以加一个数值是为避免ViewPaper使用同一个的view来渲染--liqiang
            int currentIndex = pageAdapter.currentIndex + 100 + currentChapter.getPageOffset(page);
            pageAdapter.currentPage = page != null ? page : currentChapter.getFirstPage();
            pageAdapter.currentIndex = currentIndex;

            if (pageNumber == 0) {
                pageAdapter.isReachStartBoundary = true;
            } else {
                pageAdapter.isReachStartBoundary = false;
            }
            if (isShowBookCover) {
                pageAdapter.isReachStartBoundary = false;
            }

            pageAdapter.isReachEndBoundary = false;
            pageAdapter.pageMap.clear();
            pageAdapter.emptyViewMap.clear();
            viewPager.setCurrentItem(currentIndex, false);

            int pageNumber = currentChapter.getChapterPageOffset();
            int totalNumber = currentChapter.getBookPageCount();
            if (pageNumber != -1 && totalNumber != -1) {
                progress.percent = (pageNumber + 1) / (float) totalNumber;
            }
            progress.chapterItemRef = currentChapter.getSpine().spineIdRef;
            if(null == pageAdapter.currentPage) {
                return;
            }
            progress.paraIndex = pageAdapter.currentPage.getParaIndex();
            progress.offsetInPara = pageAdapter.currentPage.getOffsetInPara();

            Intent it = new Intent(ReadOverlayActivity.ACTION_BACK_PROGRESS_DONE);
            if (pageAdapter != null && pageAdapter.currentPage != null) {
                it.putExtra(ReadOverlayActivity.BookMarkStateKey, pageAdapter.currentPage.getBookMark() != null);
            }
            LocalBroadcastManager.getInstance(BookPageViewActivity.this).sendBroadcast(it);

            Intent intent = new Intent(ReadOverlayActivity.ACTION_PAGENUMBER_CHANGE_DONE);
            if (pageAdapter != null && pageAdapter.currentPage != null) {
                intent.putExtra(ReadOverlayActivity.BookMarkStateKey, pageAdapter.currentPage.getBookMark() != null);
            }
            LocalBroadcastManager.getInstance(BookPageViewActivity.this).sendBroadcast(intent);
        }
    }

    public class PrepareNextPrevChapterInBackground extends AsyncTask<Void, Void, Void> {

        int currentPageIndex;
        Chapter currentChapter;

        PrepareNextPrevChapterInBackground(int currentPageIndex, Chapter currentChapter) {
            this.currentPageIndex = currentPageIndex;
            this.currentChapter = currentChapter;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            loadNextPrevChapter(currentPageIndex, currentChapter);
            clearAllPageExceptWillBeUsed(currentChapter);
            return null;
        }

        private void loadNextPrevChapter(int currentPageIndex, Chapter currentChapter) {
            if (isCancelled()) {
                return;
            }
            if (currentPageIndex >= 0) {
                // 如果当前页面是该章节的最后两页时，准备下一章节
                if (currentPageIndex >= currentChapter.getPageCount() - 2) {
                    Chapter nextChapter = getNextChapter(currentChapter);
                    if (nextChapter != null) {
                        nextChapter.doPage();
                    }
                }
                if (isCancelled()) {
                    return;
                }
                // 如果当前页面是该章节的第一二页时，准备上一章节
                if (currentPageIndex <= 1) {
                    Chapter prevChapter = getPrevChapter(currentChapter);
                    if (prevChapter != null) {
                        prevChapter.doPage();
                    }
                }
            }
        }

        private void clearAllPageExceptWillBeUsed(Chapter currentChapter) {
            if (currentChapter != null) {
                int chapterIndex = chapterList.indexOf(currentChapter);
                if (chapterIndex >= 0) {
                    for (int i = 0; i < chapterList.size(); i++) {
                        if (isCancelled()) {
                            return;
                        }
                        if (i == chapterIndex - 1 || i == chapterIndex || i == chapterIndex + 1) {
                            continue;
                        }
                        Chapter chapter = chapterList.get(i);
                        if (progress != null && progress.chapterItemRef != null) {
                            if (progress.chapterItemRef.equals(chapter.getSpine().spineIdRef)) {
                                continue;
                            }
                        }
                        if (backProgress != null && backProgress.chapterItemRef != null) {
                            if (backProgress.chapterItemRef.equals(chapter.getSpine().spineIdRef)) {
                                continue;
                            }
                        }
                        if (currentProgress != null && currentProgress.chapterItemRef != null) {
                            if (currentProgress.chapterItemRef.equals(chapter.getSpine().spineIdRef)) {
                                continue;
                            }
                        }
                        chapter.clearPage();
                    }
                } else {
                    clearAllPage();
                }
            } else {
                clearAllPage();
            }
        }

        private void clearAllPage() {
            for (Chapter chapter : chapterList) {
                if (isCancelled()) {
                    return;
                }
                chapter.clearPage();
            }
        }
    }

    public class GoToLastPageTask extends AsyncTask<Void, Void, Void> {

        private Chapter currentChapter;

        public GoToLastPageTask(Chapter currentChapter) {
            this.currentChapter = currentChapter;
            backProgress = progress.clone();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }

        @Override
        protected Void doInBackground(Void... params) {
            currentChapter.doPage();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // mLoadingDailog.dismiss();
            dismissHUD();
            if (isExitBookPage) {
                return;
            }
            
            if (isCancelled()) {
                return;
            }
            
            if(null == currentChapter) {
                return;
            }

            Page page = currentChapter.getLastPage();
            // 这里极端情况是从最后一页拖到第一页会使用同一个View来渲染，渲染速度很慢而且阻塞UI线程，这是系统的处理逻辑
            // 所以加一个数值是为避免ViewPaper使用同一个的view来渲染--liqiang
            int currentIndex = pageAdapter.currentIndex + 100 + currentChapter.getPageOffset(page);
            pageAdapter.currentPage = page != null ? page : currentChapter.getFirstPage();
            pageAdapter.currentIndex = currentIndex;
            pageAdapter.isReachStartBoundary = false;
            pageAdapter.isReachEndBoundary = false;
            pageAdapter.pageMap.clear();
            pageAdapter.emptyViewMap.clear();
            viewPager.setCurrentItem(currentIndex, false);

            int pageNumber = currentChapter.getChapterPageOffset();
            int totalNumber = currentChapter.getBookPageCount();
            if(null == progress) {
                return;
            }
            if (pageNumber != -1 && totalNumber != -1) {
                progress.percent = (pageNumber + 1) / (float) totalNumber;
            }
            progress.chapterItemRef = currentChapter.getSpine().spineIdRef;
            if(null != pageAdapter.currentPage) {
                progress.paraIndex = pageAdapter.currentPage.getParaIndex();
                progress.offsetInPara = pageAdapter.currentPage.getOffsetInPara();

                LocalBroadcastManager.getInstance(BookPageViewActivity.this).sendBroadcast(new Intent(ReadOverlayActivity.ACTION_BACK_PROGRESS_DONE));
                pageAdapter.goToNext(false);
            }
        }
    }

    public class PageCountTask extends AsyncTask<Void, Void, String> {

        private StringBuffer chapterBlockCount = new StringBuffer();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            StringBuffer chapterPageIndex = new StringBuffer();
            int chapterPageOffset = 0;
            for (Chapter chapter : chapterList) {
                if (isCancelled()) {
                    return null;
                }
                if (chapter.isCancelBuildPage()) {
                    return null;
                }
                if (serverProgressSynced && !TextUtils.isEmpty(progress.chapterItemRef)) {
                    prepareServerProgressChapter();
                    serverProgressSynced = false;
                }
                int pageCount = chapter.calculatePageCount();
                chapter.setChapterPageOffset(chapterPageOffset);
                chapterPageOffset += pageCount;
                chapterPageIndex.append(chapter.getPageCount());
                chapterPageIndex.append(";");
            }
            for (Chapter chapter : chapterList) {
                chapterBlockCount.append(chapter.getBlockCount());
                chapterBlockCount.append(";");
                chapter.setBookPageCount(chapterPageOffset);
                chapter.tocPageNumberDone();
            }

            if (pageAdapter.currentPage != null) {
                Intent intent = new Intent(ReadOverlayActivity.ACTION_PAGECOUNT_DONE);
                intent.putExtra(ReadOverlayActivity.PageCountKey, chapterPageOffset);
                preparePageIndexForSeekBar(intent);
                LocalBroadcastManager.getInstance(BookPageViewActivity.this).sendBroadcast(intent);
            }
            if (serverProgressSynced) {
                gotoJDProgressPage();
            }
            return chapterPageIndex.toString();
        }

        @Override
        protected void onPostExecute(String chapterPageIndex) {
            if (chapterPageIndex != null) {
                isPageFinished = true;
                MZLog.d("qli", "===qli=== PageCountTask: ");
                if (pageAdapter != null) {
                    if (isExitBookPage) {
                        return;
                    }
                    StringBuffer tocPageIndex = new StringBuffer();
                    for (TOCItem item : tocList) {
                        tocPageIndex.append(item.pageNumber);
                        tocPageIndex.append(";");
                    }
                    tocPageIndex.append("|");
                    tocPageIndex.append(chapterPageIndex);
                    if (isCancelled()) {
                        return;
                    }
                    final String bookPageIndex = tocPageIndex.toString();
                    BookPageViewActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (isExitBookPage) {
                                return;
                            }
                            int textSizeLevel = LocalUserSetting.getTextSizeLevel(BookPageViewActivity.this);
                            int textSize = textSizeLevelSet[textSizeLevel];
                            float pixel = textSize * getResources().getDisplayMetrics().density;
                            int lineSpace = (int) (pixel * getPageLineSpace());
                            int blockSpace = (int) (pixel * getPageBlockSpace());
                            ReadBookPage bookPage = new ReadBookPage();
                            bookPage.setTextSize(textSize);
                            bookPage.setChapterPage(bookPageIndex);
                            bookPage.setFontFace(fontPath);
                            bookPage.setLineSpace(lineSpace);
                            bookPage.setBlockSpace(blockSpace);
                            bookPage.setPageEdgeSpace(getPageMarginLeft());
                            bookPage.setChapterBlockCount(chapterBlockCount.toString());
                            //需要完善横竖屏保存逻辑 --liqiang
                            if (eBook != null) {
                                MZBookDatabase.instance.insertOrUpdateBookPage(eBook.bookId, 0, bookPage);
                            }
                            if (document != null) {
                                MZBookDatabase.instance.insertOrUpdateBookPage(0, document.documentId, bookPage);
                            }
                            if (isGotoJDProgress) {
                                gotoJDProgressPage();
                            } else {
                                pageAdapter.notifyDataSetChanged();
                                pageAdapter.invalidatePage();
                            }
                        }
                    });
                    showAllJDBookMark();
                    if (isDownloadFontFace) {
                        downloadFont(fzssFontItem);
                        downloadFont(fzktFontItem);
                        downloadFont(fzlthFontItem);
                    }
                }
            }
        }
    }

    /**
     * PageCount线程专用 准备后台同步进度的章节数据
     */
    private void prepareServerProgressChapter() {
        for (Chapter chapter : chapterList) {
            if (chapter.getSpine().spineIdRef.equals(progress.chapterItemRef)) {
                chapter.doPage();// 为了PageCount线程让CPU资源给同步进度的doPage，不要删除即使GoToProgressTask已经有doPage
                GoToProgressTask task = new GoToProgressTask();
                task.execute("");
                break;
            }
        }
    }

    protected void syncReadProgressAndBookMark() {
        if (eBook == null && document == null) {
            return;
        }
        if (isExitBookPage) {
            return;
        }
        if (!LoginUser.isLogin()) {
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(BookPageViewActivity.this)) {
            return;
        }
        RequestParams request = null;
        if (eBook != null) {
            LocalBook book = LocalBook.getLocalBook(eBook.bookId, LoginUser.getpin());
            request = RequestParamsPool.getSyncEBookReadProgressBookMark(book);
        } else if (document != null) {
            if (docBind == null || docBind.serverId == 0) {
                return;
            }
            request = RequestParamsPool.getSyncDocumentReadProgressBookMark(document);
        }
        WebRequestHelper.post(URLText.JD_BOOK_READ_URL, request, true, new MyAsyncHttpResponseHandler(BookPageViewActivity.this) {

            @Override
            public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
                SyncServerReadProgress task = new SyncServerReadProgress();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, responseBody);
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

            }
        });
    }

    // 同步服务器进度和书签的结果处理
    private class SyncServerReadProgress extends AsyncTask<byte[], Void, ReadProgress> {

        @Override
        protected ReadProgress doInBackground(byte[]... params) {
            ReadProgress readprogress = null;
            try {
                String result = null;
                try {
                    result = new String(params[0], "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (TextUtils.isEmpty(result)) {
                    return null;
                }
                JSONObject obj = new JSONObject(result);
                String code = obj.optString("code");
                if ("0".equals(code)) {
                    JSONArray array = obj.getJSONArray("bookList");
                    JSONObject book = array.getJSONObject(0);
                    JSONArray list = book.getJSONArray("list");
                    long version = book.getLong("version");
                    List<BookMark> markList = new ArrayList<BookMark>();
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject data = list.getJSONObject(i);
                        if (data.getInt("dataType") == 0) {// 阅读进度
                            readprogress = ReadProgress.fromJSON(data);
                            if (!TextUtils.isEmpty(localChapterBlockIndex)) {
                                if ((bookId != 0 && bookId == data.getLong("ebookId")) || // filter
                                                                                            // wrong
                                                                                            // progress
                                        (docBind != null && String.valueOf(docBind.serverId).equals(data.getString("importBookId"))))
                                    setupJDProgressChapterRef(readprogress);
                                else
                                    readprogress = null;
                            }
                        } else if (data.getInt("dataType") == 1) {// 书签
                            int valid = data.getInt("valid");
                            if (valid == 0) {
                                MZBookDatabase.instance.deleteBookMark(userId, data.optInt("id"));
                            } else if (valid == 1) {
                                BookMark bookMark = BookMark.fromJSON(data);
                                bookMark.docid = documentId;
                                bookMark.isSync = 1;
                                if ((bookMark.ebookid != 0 && bookId == bookMark.ebookid) || // filter
                                                                                                // wrong
                                                                                                // bookmark
                                        (docBind != null && String.valueOf(docBind.serverId).equals(data.getString("importBookId"))))
                                    markList.add(bookMark);
                            }
                        }
                    }

                    MZBookDatabase.instance.insertOrUpdateBookMarksSyncTime(userId, bookId, documentId, version);
                    for (BookMark mark : markList) {
                        MZBookDatabase.instance.addBookMark(mark);
                    }
                } else if ("3".equals(code)) {
                    //请登录后重试 --liqiang
                } else if ("5".equals(code) || "80".equals(code)) {
                    ToastUtil.showToastInThread(obj.optString("message"), Toast.LENGTH_SHORT);
                } else {
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return readprogress;
        }

        boolean serverProgressAfterLocalProgress(ReadProgress serverProgress) {

            if (serverProgress.updateTime <= localProgress.updateTime) {
                return false;
            } else {
                if (serverProgress.inSameLocation(localProgress) || serverProgress.inSameLocation(progress)) {
                    return false;
                } else {
                    return true;
                }
            }

        }

        @Override
        protected void onPostExecute(final ReadProgress serverProgress) {
            super.onPostExecute(serverProgress);

            if (isExitBookPage) {
                return;
            }
            if (serverProgress == null || TextUtils.isEmpty(serverProgress.chapterItemRef)) {
                return;
            }
            showAllJDBookMark();
            if (serverProgressAfterLocalProgress(serverProgress)) {
                String chapterName = "";
                for (Chapter chapter : chapterList) {
                    if (chapter.getSpine().spineIdRef.equals(serverProgress.chapterItemRef)) {
                        chapterName = chapter.getPageHead("");
                        break;
                    }
                }
                if (chapterName == null) {
                    chapterName = "";
                }

                if (isFirstOpenBook) {
                    progress = serverProgress;
                    progress.updateTime = System.currentTimeMillis() / 1000;
                    startProgress = progress.clone();
                    serverProgressSynced = true;
                    if (isPageFinished) {
                        gotoJDProgressPage();
                    } else if (!TextUtils.isEmpty(serverProgress.chapterItemRef)) {
                        // prepare chapter data will be execute in PageCountTask
                        showLoading();
                    } else {
                        isGotoJDProgress = true;
                        showLoading();
                    }
                } else {
                    // AlertDialog alertDialog = new
                    // AlertDialog.Builder(BookPageViewActivity.this,
                    // AlertDialog.THEME_HOLO_LIGHT)
                    // .setIconAttribute(android.R.attr.alertDialogIcon).setTitle(getString(R.string.progress_sync_alert)
                    // + chapterName)
                    // .setPositiveButton(R.string.ok, new
                    // DialogInterface.OnClickListener() {
                    // public void onClick(DialogInterface d, int whichButton) {
                    // progress = serverProgress;
                    // progress.updateTime = System.currentTimeMillis() / 1000;
                    // startProgress = progress.clone();
                    // if (isPageFinished) {
                    // gotoJDProgressPage();
                    // } else if
                    // (!TextUtils.isEmpty(serverProgress.chapterItemRef)) {
                    // GoToProgressTask task = new GoToProgressTask();
                    // task.execute("");
                    // } else {
                    // isGotoJDProgress = true;
                    // showLoading();
                    // }
                    // }
                    // }).setNegativeButton(R.string.cancel, new
                    // DialogInterface.OnClickListener() {
                    // public void onClick(DialogInterface d, int whichButton) {
                    // isJDProgress = false;
                    // isGotoJDProgress = false;
                    // }
                    // }).setCancelable(false).create();
                    // alertDialog.show();

                    DialogManager.showCommonDialog(BookPageViewActivity.this, "继续阅读", getString(R.string.progress_sync_alert) + chapterName, "确定", "取消",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        progress = serverProgress;
                                        progress.updateTime = System.currentTimeMillis() / 1000;
                                        startProgress = progress.clone();
                                        if (isPageFinished) {
                                            gotoJDProgressPage();
                                        } else if (!TextUtils.isEmpty(serverProgress.chapterItemRef)) {
                                            GoToProgressTask task = new GoToProgressTask();
                                            task.execute("");
                                        } else {
                                            isGotoJDProgress = true;
                                            showLoading();
                                        }
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        isJDProgress = false;
                                        isGotoJDProgress = false;
                                        break;
                                    default:
                                        break;
                                    }
                                    dialog.dismiss();
                                }
                            });
                }
            }
        }
    }

    public class PrepareChapterDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            refreshNote();
            return null;
        }
    }

    private void requestSyncNote() {
        if (!LoginUser.isLogin()) {
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(BookPageViewActivity.this)) {
            return;
        }
        if (eBook == null && document == null) {
            return;
        }
        RequestParams request = null;
        if (eBook != null && !TextUtils.isEmpty(userId)) {
            lastNoteSyncTime = MZBookDatabase.instance.getNoteSyncTime(userId, eBook.bookId, 0);
            int millisecond = String.valueOf(System.currentTimeMillis()).length();
            if (String.valueOf(lastNoteSyncTime).length() == millisecond) {
                // lastNoteSyncTime 毫秒改成秒
                lastNoteSyncTime = lastNoteSyncTime / 1000;
            }
            request = RequestParamsPool.getEBookReadNoteSyncParams(eBook.bookId, lastNoteSyncTime);
        } else if (document != null && !TextUtils.isEmpty(userId)) {
            if (docBind == null || docBind.serverId == 0) {
                return;
            }
            lastNoteSyncTime = MZBookDatabase.instance.getNoteSyncTime(userId, 0, document.documentId);
            int millisecond = String.valueOf(System.currentTimeMillis()).length();
            if (String.valueOf(lastNoteSyncTime).length() == millisecond) {
                // lastNoteSyncTime 毫秒改成秒
                lastNoteSyncTime = lastNoteSyncTime / 1000;
            }
            request = RequestParamsPool.getDocumentReadNoteSyncParams(docBind.serverId, lastNoteSyncTime);
        }

        WebRequestHelper.get(URLText.pullNotesUrl, request, true, new MyAsyncHttpResponseHandler(BookPageViewActivity.this) {

            @Override
            public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
                int total = 0;
                try {
                    String result = new String(responseBody, "utf-8");
                    JSONObject obj = new JSONObject(result);
                    total = obj.optInt("total");
                    JSONArray noteArray = obj.optJSONArray("notes");
                    if (noteArray != null) {
                        for (int i = 0; i < noteArray.length(); ++i) {
                            JSONObject noteObj = noteArray.getJSONObject(i);
                            int deletedint = noteObj.optInt("deleted");
                            if (deletedint == 1) {
                                MZBookDatabase.instance.deleteReadNote(noteObj.optInt("id"));
                            } else {
                                try {
                                    ReadNote note = ReadNote.parseMyNoteFromJson(noteObj, document == null ? 0 : document.documentId);
                                    note.modified = false;
                                    MZLog.d("wangguodong", "网络时间lastSyncTime:(秒)" + noteObj.getLong("updated_at_timestamp"));

                                    MZBookDatabase.instance.insertOrUpdateEbookNote(note);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                            lastNoteSyncTime = Math.max(lastNoteSyncTime, noteObj.getLong("updated_at_timestamp"));
                        }
                    }

                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if (isExitBookPage) {
                    return;
                }
                if (eBook != null) {
                    MZBookDatabase.instance.insertOrUpdateNoteSyncTime(userId, eBook.bookId, 0, lastNoteSyncTime);
                } else if (document != null) {
                    MZBookDatabase.instance.insertOrUpdateNoteSyncTime(userId, 0, document.documentId, lastNoteSyncTime);
                }
                if (total >= 20) {
                    Intent intent = new Intent(ACTION_PULL_NOTE);
                    LocalBroadcastManager.getInstance(BookPageViewActivity.this).sendBroadcast(intent);
                } else {
                    PrepareChapterDataTask prepareTask = new PrepareChapterDataTask();
                    prepareTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            }

        });
    }
    
    //下载字体提示框
    private void prepareShowDownloadFontBar() {
        long totalSize = 0;
        if (fzssFontItem != null && fzssFontItem.getDownloadStatus() != FontItem.STATE_LOADED) {
            totalSize += fzssFontItem.getInitShowTotalSize();
        }
        if (fzlthFontItem != null && fzlthFontItem.getDownloadStatus() != FontItem.STATE_LOADED) {
            totalSize += fzlthFontItem.getInitShowTotalSize();
        }
        if (fzktFontItem != null && fzktFontItem.getDownloadStatus() != FontItem.STATE_LOADED) {
            totalSize += fzktFontItem.getInitShowTotalSize();
        }
        String text = "";
        if (totalSize > 0) {
        	this.fontTotalSize = totalSize;
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            text = decimalFormat.format((float)totalSize / (1024 * 1024)) + " MB";
        } else {
            downloadFontText = null;
            return;
        }
        downloadFontText = "为提高阅读质量，请下载字体文件 "+ text +"，点击继续";
        if (pageAdapter != null) {
            pageAdapter.invalidatePage();
        }
    }
    
    private void downloadFont(FontItem fontItem) {
        if (fontItem == null || TextUtils.isEmpty(fontItem.getUrl())) {
            return;
        }
        if (fontItem.getDownloadStatus() == FontItem.STATE_LOADED) {
            // 已下载的
            return;
        }
        if (DownloadService.inDownloadQueue(fontItem)) {
            // 已在下载队列中的
            return;
        }
        if (fontItem.getDownloadStatus() == FontItem.STATE_UNLOAD
                || fontItem.getDownloadStatus() == FontItem.STATE_LOAD_FAILED
                || fontItem.getDownloadStatus() == FontItem.STATE_LOAD_PAUSED
                || fontItem.getDownloadStatus() == FontItem.STATE_LOAD_READY) {
            if (!NetWorkUtils.isNetworkAvailable(BookPageViewActivity.this)) {
                ToastUtil.showToastInThread(R.string.network_not_find);
                return;
            }
            Intent intent = new Intent(this, DownloadService.class);
            String dataKey = DataIntent.creatKey();
            fontItem.setMenualStop(false);
            fontItem.setTotalSize(0);
            DataIntent.put(dataKey, fontItem);
            intent.putExtra(KEY1, dataKey);
            this.startService(intent);
        }
    }

    @Override
    public boolean playAudio(String id) {
        if (playList == null) {
            return false;
        }
        for (final PlayItem item : playList) {
            if (item.id.equals(id)) {
                return playAudioWithPath(item.mediaPath);
            }
        }
        return false;
    }

    @Override
    public boolean isPlaying(String id) {
        if (playList == null) {
            return false;
        }
        for (PlayItem item : playList) {
            if (item.id.equals(id)) {
                return MediaPlayerHelper.isPlaying(MediaPlayerHelper.getLocalAudioPath(audioPath, item.mediaPath));
            }
        }
        return false;
    }

    private boolean playAudioWithPath(final String path) {
        if (path.toLowerCase(Locale.getDefault()).startsWith("http") && !TextUtils.isEmpty(audioPath)) {
            String fileName = URLUtil.guessFileName(path, null, null);
            File file = new File(audioPath, fileName);
            if (!file.exists()) {
                DialogManager.showCommonDialog(BookPageViewActivity.this, getString(R.string.download_notify_title), getString(R.string.download_notify),
                        getString(R.string.download), "取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    if (!MediaDownloadService.mediaDownloadingQueue.contains(path)) {
                                        Intent intent = new Intent(BookPageViewActivity.this, MediaDownloadService.class);
                                        intent.putExtra(MediaDownloadService.MediaUrlPathKey, path);
                                        intent.putExtra(MediaDownloadService.MediaSavePathKey, audioPath);
                                        MediaDownloadService.addDownloadUrl(path);
                                        receiver.downloadUrl = path;
                                        startService(intent);
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
                return MediaPlayerHelper.play(file.getPath());
            }
        } else {
            return MediaPlayerHelper.play(path);
        }
        return false;
    }
    
    /** 关闭当前页面 */
    private void exit() {
    	UserSettingStatistics.upload(this);
    	
    	EBookAnimationUtils anim = BookcaseLocalFragmentNewUI.getEBookAnimationUtils();
    	if (null != anim) {
    		if (anim.getIsOpen()) {
    			int mStatusHeight = ScreenUtils.getStatusHeight(this);
    			View view = getWindow().getDecorView();
            	view.setDrawingCacheEnabled(true);
            	Bitmap cache = view.getDrawingCache();
        		Bitmap mBgBitmap = Bitmap.createBitmap(cache, 0, mStatusHeight, cache.getWidth(), (cache.getHeight() - mStatusHeight));
        		view.destroyDrawingCache();
        		anim.showClose(mBgBitmap);
        		finish();
            	overridePendingTransition(0, 0);
    		}else {
    			finish();	
    		}
    	}else {
    		finish();	
    	}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            openSetting();
            return true;
        }
        
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	exit();
            return true;
        }

        if (isUseVolumePage) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
                nextPage(false);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_PAGE_UP) {
                prevPage(false);
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else {
            if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
                nextPage(false);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_PAGE_UP) {
                prevPage(false);
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isUseVolumePage) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                return true;
            } else {
                return super.onKeyUp(keyCode, event);
            }
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public void nextPage(boolean smoothScroll) {
        if(null == viewPager) {
            return;
        }
        
        if (viewPager.isPagingEnabled() || isPageNoAnimation) {
            if(null == pageAdapter) {
                return;
            }
            pageAdapter.goToNext(smoothScroll);
        }
    }

    @Override
    public void prevPage(boolean smoothScroll) {
        if(null == viewPager || null == pageAdapter) {
            return;
        }
        if (viewPager.isPagingEnabled() || isPageNoAnimation) {
            pageAdapter.goToPrev(smoothScroll);
        }
    }
    
    @Override
    public void forceGotoNextPageInAnimation() {
        handler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                Kit42View view = pageAdapter.getViewByPosition(pageAdapter.currentIndex);
                if (view != null) {
                    view.resetPageAnimation();
                }
                pageAdapter.goToNext(false);
            }
        },Kit42View.PAGE_ANIMATION_DURATION);
    }

    @Override
    public void forceGotoPrevPageInAnimation() {
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Kit42View view = pageAdapter.getViewByPosition(pageAdapter.currentIndex);
                if (view != null) {
                    view.resetPageAnimation();
                }
                pageAdapter.goToPrev(false);
            }
        }, Kit42View.PAGE_ANIMATION_DURATION -100);
    }
    
    @Override
    public void savePageAnimationTime() {
        pageAnimationTime = System.currentTimeMillis();
    }
    
    @Override
    public long getPageAnimationTime() {
        return pageAnimationTime;
    }
    
    /**
     * 生成仿真动画的上下页的bitmap
     */
    @Override
    public void preparePageTurning(boolean isNext) {
        Kit42View currentView = pageAdapter.getViewByPosition(pageAdapter.currentIndex);
        if (isNext) {
            Kit42View nextView = pageAdapter.getViewByPosition(pageAdapter.currentIndex + 1);
            if (nextView != null) {
                nextView.generatePageBitmap();
                currentView.setNextPageBitmap(nextView.getCurPageBitmap());
            }
        } else {
            Kit42View prevView = pageAdapter.getViewByPosition(pageAdapter.currentIndex - 1);
            if (prevView != null) {
                prevView.generatePageBitmap();
                currentView.setPrevPageBitmap(prevView.getCurPageBitmap());
            }
        }
    }

    @Override
    public void openSetting() {
        Intent intent = new Intent(BookPageViewActivity.this, ReadOverlayActivity.class);
        intent.putExtra(ReadOverlayActivity.ShowPurchaseButtonKey, isShowPurchaseButton);
        if (pageAdapter != null && pageAdapter.currentPage != null) {
            intent.putExtra(ReadOverlayActivity.BookMarkStateKey, pageAdapter.currentPage.getBookMark() != null);
        }
        if (backProgress != null) {
            intent.putExtra(ReadOverlayActivity.IsBackProgressKey, true);
        }
        if (isPageFinished) {
            preparePageIndexForSeekBar(intent);
        }
        if (playList != null && playList.size() > 0) {
            intent.putExtra(ReadOverlayActivity.ShowPlayListButtonKey, true);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, ReadSettingRequest);
    }
    
    @Override
    public void openSearch() {
        Intent intent = new Intent(this, BookSearchActivity.class);
        if (!isShowSearchBar) {
            stopAndClearSearch();
        } else {
            if (pageAdapter.currentPage != null) {
                ReadSearchData data = pageAdapter.currentPage.getFirstSearchData();
                intent.putExtra(BookSearchActivity.PARA_INDEX, data.getParaIndex());
                intent.putExtra(BookSearchActivity.OFFSET_IN_PARA, data.getStartOffsetInPara());
            }
        }
        intent.putExtra(BookSearchActivity.SEARCH_KEYWORDS, searchKeywords);
        intent.putExtra(BookSearchActivity.LOAD_SEARCH_DATA, isShowSearchBar);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, SearchForBookRequest);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
    
    @Override
    public String getSearchKeywords() {
        return searchKeywords;
    }
    
    @Override
    public boolean isShowSearchBar() {
        return isShowSearchBar;
    }
    
    @Override
    public void hideSearchBar() {
        isShowSearchBar = false;
        viewPager.setPagingEnabled(!isPageAnimationTurning);
        if (pageAdapter != null) {
            pageAdapter.invalidatePage();
        }
    }
    
    @Override
    public void stopAndClearSearch() {
        if (searchTask != null) {
            searchTask.setCancel(true);
            searchTask.cancel(true);
        }
        if (pageAdapter != null && pageAdapter.currentPage != null) {
            Page page = pageAdapter.currentPage;
            page.clearCloneHightlight();
            if (page.getChapter() != null) {
                page.getChapter().clearKeywordsHighlight();
            }
        }
        for (int i = 0, n = chapterList.size(); i < n; i++) {
            Chapter chapter = chapterList.get(i);
            chapter.setCancelSearchPage(true);
            chapter.clearKeywordsHighlight();
        }
    }
    
    @Override
    public void startDownloadFont() {
        if (NetWorkUtils.isNetworkAvailable(BookPageViewActivity.this)) {
        	//若为手机移动网络，若超过3M则提示是否继续下载
    		if (NetWorkUtils.getNetworkConnectType(BookPageViewActivity.this) == NetworkConnectType.MOBILE) {
    			if (((double)fontTotalSize/ (1024 * 1024)) < 3) {//没有超过3M
    				startDownloadFonts();
    			} else {//超过3M
    				DialogManager.showCommonDialog(BookPageViewActivity.this, "提示","您的WIFI未连接，是否继续下载？", "是", "否",
    						new DialogInterface.OnClickListener() {

    							@Override
    							public void onClick(DialogInterface dialog,
    									int which) {
    								dialog.dismiss();
    								switch (which) {
    									case DialogInterface.BUTTON_POSITIVE:
    										startDownloadFonts();
    										break;
    									case DialogInterface.BUTTON_NEGATIVE:
    										break;
    									default:
    										break;
    								}
    							}
    						});
    			}
    		}else{
    			startDownloadFonts();
    		}
        } else {
            ToastUtil.showToastInThread(R.string.network_not_find);
        }
    }
    /**
     * 开始下载字体
     */
    private void startDownloadFonts(){
    	isDownloadFontFace = true;
        downloadFont(fzssFontItem);
        downloadFont(fzlthFontItem);
        downloadFont(fzktFontItem);
        downloadFontText = null;
        if (pageAdapter != null) {
            pageAdapter.invalidatePage();
        }
        ToastUtil.showToastInThread(R.string.read_font_download_start);
        Intent intent = new Intent(BookPageViewActivity.this, ReaderSettingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
    
    @Override
    public String getDownloadFontText() {
        return downloadFontText;
    }
    
    @Override
    public void hideDownloadFontBar() {
        isDownloadFontFace = false;
        downloadFontText = null;
        if (pageAdapter != null) {
            pageAdapter.invalidatePage();
        }
        ToastUtil.showToastInThread(R.string.read_font_download_cancel);
        DialogManager.showCommonDialog(BookPageViewActivity.this, "下载字体", "可在ePub阅读界面－轻按中央－Aa－高级阅读设置，重新下载推荐字体。", "关闭", "下次不再提醒",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            LocalUserSetting.saveIgnoreFontDownload(context, true);
                            break;
                        default:
                            break;
                        }
                        dialog.dismiss();
                    }
                });
    }

    private void preparePageIndexForSeekBar(Intent intent) {
        ArrayList<ChapterPageIndex> chapterPageIndexList = new ArrayList<ChapterPageIndex>();
        if(null == chapterList) {
            return;
        }
        for (Chapter chapter : chapterList) {
            if(null == chapter) {
                continue;
            }
            ChapterPageIndex pageIndex = new ChapterPageIndex(chapter.getPageHead(""), chapter.getChapterPageOffset(), chapter.getChapterPageOffset()
                    + chapter.getPageCount());
            chapterPageIndexList.add(pageIndex);
        }
        intent.putExtra(ReadOverlayActivity.ChapterPageIndexListKey, chapterPageIndexList);
        if (viewPager != null && pageAdapter != null && pageAdapter.currentPage != null) {
            int currentPosition = 0;
            if (pageAdapter.currentPage instanceof CoverPage) {
                // do nothing
            } else if (pageAdapter.currentPage instanceof FinishPage) {
                currentPosition = chapterList.get(0).getBookPageCount() - 1;
            } else {
                Chapter currentChapter = pageAdapter.currentPage.getChapter();
                currentPosition = currentChapter.getPageOffset(pageAdapter.currentPage);
            }
            intent.putExtra(ReadOverlayActivity.CurrentPageIndexKey, currentPosition);
        }
    }

    @Override
    public void beginNoteSelection() {
        viewPager.setPagingEnabled(false);
    }

    @Override
    public void endNoteSelection() {
        viewPager.setPagingEnabled(!isPageAnimationTurning);
    }

    @Override
    public void finishRating(float rating) {
        Intent intent = new Intent(this, BookCommentNewuiActivity.class);
        intent.putExtra(BookCommentNewuiActivity.RatingValueKey, rating);
        long bookId = 0;
        String title = "";
        if (eBook != null) {
            title = eBook.title;
            bookId = eBook.bookId;
        }
        if (document != null && !TextUtils.isEmpty(document.title)) {
            title = document.title;
        }

        if (docBind != null) {
            bookId = docBind.bookId;
            if (!TextUtils.isEmpty(docBind.serverTitle)) {
                title = docBind.serverTitle;
            }
        }

        if (bookId == 0) {
            return;
        }

        intent.putExtra(BookCommentNewuiActivity.TitleKey, title);
        intent.putExtra(BookCommentNewuiActivity.BookIdKey, bookId);
        startActivity(intent);
    }

    @Override
    public BookBackCoverView getLastPageView() {
        BookBackCoverView lastView = null;
        if (eBook != null) {
            BookBackCoverView view = new BookBackCoverView(this);
            view.setBackCoverActionListener(this);
            view.showPurchase(JDDecryptUtil.isTryRead);
            view.setBookId(eBook.bookId);
            view.setUserId(userId);
            lastView = view;
        } else if (document != null) {
            BookBackCoverView view = new BookBackCoverView(this);
            view.setBackCoverActionListener(this);
            view.setUserId(userId);
            view.setDocumentId(document.documentId);
            view.showRatingBar(false);
            if (docBind != null) {
                if (docBind.bookId != 0) {
                    view.setBookId(docBind.bookId);
                    view.showRatingBar(true);
                }
                if (docBind.serverId != 0) {
                    view.setServerDocId(docBind.serverId);
                }
                lastView = view;
            }
        }
        return lastView;
    }

    @Override
    public View getLoadingView() {
        View view = this.getLayoutInflater().inflate(R.layout.loading_book_page, null);
        TextView textview = (TextView) view.findViewById(R.id.textview);
        textview.setTextColor(getFontColor());
        if (isBookOpenError) {
            view.findViewById(R.id.progress).setVisibility(View.GONE);
            textview.setText(R.string.book_load_error);
        } else {
            if (pageAdapter != null) {
                if (pageAdapter.isReachStartBoundary || pageAdapter.isReachEndBoundary) {
                    view.setVisibility(View.GONE);
                }
            }
        }
        return view;
    }

    /**
     * ebook点击购买直接进入购物车tmj
     */
    @Override
    public void purchaseFullBook() {
        // Intent intent = new Intent(this, BookInfoNewUIActivity.class);
        // intent.putExtra(BookInfoNewUIActivity.BookIdKey, eBook.bookId);
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // startActivity(intent);
        // finish();

        if (LoginUser.isLogin()) {
            SimplifiedDetail detail = new SimplifiedDetail();
            detail.bookId = eBook.bookId;
            BookCartManager.getInstance().addToShoppingCart(BookPageViewActivity.this, detail, new AddToCartListener() {

                @Override
                public void onAddSuccess() {
                    Intent itIntent = new Intent(BookPageViewActivity.this, BookCartActivity.class);
                    startActivity(itIntent);
                }

                @Override
                public void onAddFail() {
                    Toast.makeText(BookPageViewActivity.this, "购买失败，请检查网络状况是否正常！", 0).show();
                }
            });
        } else {
            Intent login = new Intent(BookPageViewActivity.this, LoginActivity.class);
            startActivity(login);
        }
    }

    @Override
    public ReadNote createEmptyNote() {
        ReadNote note = new ReadNote();
        note.userId = userId;
        if (eBook != null) {
            note.ebookId = eBook.bookId;
        } else if (document != null) {
            note.documentId = document.documentId;
        }
        return note;
    }

    @Override
    public void asyncRequestCreateNote(String quote, Kit42View kit42View) {
        Intent intent = new Intent(this, BookReadNoteActivity.class);
        if (quote != null) {
            intent.putExtra(BookReadNoteActivity.BookNoteQuoteKey, quote);
        }
        if (bookId == 0 && documentId != 0) {
            intent.putExtra("documentId", documentId);
        } else if (bookId != 0 && documentId == 0) {
            intent.putExtra("bookId", bookId);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, CreateNoteRequest);
        this.noteKit42View = kit42View;
        disableSyncNote = true;
    }

    @Override
    public void asyncRequestModifyNote(ReadNote note, Kit42View kit42View) {
        if (note == null) {
            return;
        }
        String quote = note.quoteText;
        String content = note.contentText;
        boolean isPrivate = note.isPrivate;
        Intent intent = new Intent(this, BookReadNoteActivity.class);
        if (quote != null) {
            intent.putExtra(BookReadNoteActivity.BookNoteQuoteKey, quote);
        }
        if (content != null) {
            intent.putExtra(BookReadNoteActivity.BookNoteContentKey, content);
        }
        intent.putExtra(BookReadNoteActivity.BookNoteIsPrivateKey, isPrivate);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, ModifyNoteRequest);
        this.noteKit42View = kit42View;
        disableSyncNote = true;
    }

    @SuppressWarnings("static-access")
	@Override
    public void asyncRequestTranslate(String words, Kit42View kit42View) {
        this.noteKit42View = kit42View;
//      TranslateTask.requestTranslate(this, words);
        if (isDictionaryReady()) {
        	haveShowDictionary =false;
            KSICibaTranslate.getInstance().getTranslateResult(words);
        } else {
        	//TODO 无词典
        	noteKit42View.showDictionaryResult(words,null,false,false);
        	haveShowDictionary = true;
        	
//            kit42View.endNoteSelection();
//            DialogManager.showCommonDialog(BookPageViewActivity.this, "提示", "词典还没下载，请先下载词典。", "下载词典(7.6M)", "取消",
//                    new DialogInterface.OnClickListener() {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            switch (which) {
//                            case DialogInterface.BUTTON_POSITIVE:
//                                Intent intent= new Intent(BookPageViewActivity.this, DictionarySettingActivity.class);
//                                BookPageViewActivity.this.startActivity(intent);
//                                break;
//                            case DialogInterface.BUTTON_NEGATIVE:
//                                break;
//                            default:
//                                break;
//                            }
//                            dialog.dismiss();
//                        }
//                    });
        }
    }
    
    private boolean isDictionaryReady() {
        if (TextUtils.isEmpty(dictPath)) {
            String appFilePath = MZBookApplication.getInstance().getCachePath();
            dictPath = appFilePath+File.separator+"dict";
        }
        if (checkDicIsExist("ec_xiaobai.dic")
                && checkDicIsExist("ce_xiaobai.dic")
                && checkDicIsExist("cc.dic")) {
            return true;
        }
        return false;
    }
    
    /**
     * 检查文件是否存在
     * @param fileName
     * @return
     */
    private boolean checkDicIsExist(String fileName){
        File dirfile= new File(dictPath);
        if(!dirfile.exists()){
            dirfile.mkdir();
            return false;
        }
        File file =new File(dictPath+"/"+fileName);
        if(file.exists())
            return true;
        else 
            return false;
    }

    @Override
    public float getPageWidth() {
        return this.pageWidth;
    }

    public static Typeface getDefaultFont() {
        if (BookPageViewActivity.fontDefault != null) {
            return BookPageViewActivity.fontDefault;
        }
        return Typeface.DEFAULT;
    }
    
    public static void initFZSSFont() {
        if (fzssFontItem == null) {
            fzssFontItem = DBHelper.queryFontItemByName(FontItem.FOUNDER_SS);
        }
        if (TextUtils.isEmpty(fzssFontItem.getUrl())) {
            DBHelper.initDefautDbData();
            fzssFontItem = DBHelper.queryFontItemByName(FontItem.FOUNDER_SS);
        }
    }
    
    public synchronized static Typeface getFZSSFont() {
        if (!isSystemFontFace) {
            return getDefaultFont();
        }
        if (fzSS != null) {
            return fzSS;
        }
        initFZSSFont();
        if (fzssFontItem != null
                && fzssFontItem.getDownloadStatus() == FontItem.STATE_LOADED
                && !TextUtils.isEmpty(fzssFontItem.getFilePath())
                && FileUtils.isExist(fzssFontItem.getFilePath())) {
            try {
                fzSS = Typeface.createFromFile(fzssFontItem.getFilePath());
                return fzSS;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (BookPageViewActivity.fontDefault != null) {
            return BookPageViewActivity.fontDefault;
        }
        return Typeface.DEFAULT;
    }
    
    public static void initFZLTHFont() {
        if (fzlthFontItem == null) {
            fzlthFontItem = DBHelper.queryFontItemByName(FontItem.FOUNDER_LANTINGHEI);
        }
        if (TextUtils.isEmpty(fzlthFontItem.getUrl())) {
            DBHelper.initDefautDbData();
            fzlthFontItem = DBHelper.queryFontItemByName(FontItem.FOUNDER_LANTINGHEI);
        }
    }
    
    public synchronized static Typeface getFZLTHFont() {
        if (!isSystemFontFace) {
            return getDefaultFont();
        }
        if (fzLTH != null) {
            return fzLTH;
        }
        initFZLTHFont();
        if (fzlthFontItem != null
                && fzlthFontItem.getDownloadStatus() == FontItem.STATE_LOADED
                && !TextUtils.isEmpty(fzlthFontItem.getFilePath())
                && FileUtils.isExist(fzlthFontItem.getFilePath())) {
            try {
                fzLTH = Typeface.createFromFile(fzlthFontItem.getFilePath());
                return fzLTH;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (BookPageViewActivity.fontDefault != null) {
            return BookPageViewActivity.fontDefault;
        }
        return Typeface.DEFAULT;
    }
    
    public static void initFZKTFont() {
        if (fzktFontItem == null) {
            fzktFontItem = DBHelper.queryFontItemByName(FontItem.FOUNDER_KAITI);
        }
        if (TextUtils.isEmpty(fzktFontItem.getUrl())) {
            DBHelper.initDefautDbData();
            fzktFontItem = DBHelper.queryFontItemByName(FontItem.FOUNDER_KAITI);
        }
    }
    
    public synchronized static Typeface getFZKTFont() {
        if (!isSystemFontFace) {
            return getDefaultFont();
        }
        if (fzKai != null) {
            return fzKai;
        }
        initFZKTFont();
        if (fzktFontItem != null
                && fzktFontItem.getDownloadStatus() == FontItem.STATE_LOADED
                && !TextUtils.isEmpty(fzktFontItem.getFilePath())
                && FileUtils.isExist(fzktFontItem.getFilePath())) {
            try {
                fzKai = Typeface.createFromFile(fzktFontItem.getFilePath());
                return fzKai;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (BookPageViewActivity.fontDefault != null) {
            return BookPageViewActivity.fontDefault;
        }
        return Typeface.DEFAULT;
    }

    public static int getBackgroundColor() {
        return bg_color;
    }

    public static int getFontColor() {
        return text_color;
    }

    public static int getLinkColor() {
        if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_NIGHT) {
            return 0xFFFFFFFF - 0x004972A3;
        } else {
            return 0xFF4972A3;
        }
    }

    public static int getFootHeadColor() {
        switch (text_color) {
        case ReadOverlayActivity.WHITE_STYLE_FONT:
            return 0xFFA5A5A5;
        case ReadOverlayActivity.SOFT_STYLE_FONT:
            return 0xFFA69F95;
        case ReadOverlayActivity.MINT_STYLE_FONT:
            return 0xFFABB2A1;
        case ReadOverlayActivity.NIGHT_STYLE_FONT:
            return 0xFF595959;
        default:
            return getPureBgFootHeadColor();
        }
        
    }
    
    /**
     * 纯色背景的foot和head的色值
     * @return
     */
    private static int getPureBgFootHeadColor(){
		int color = -1;
		switch (text_color) {
		case 0xFF000000:
			color = 0xFF737373;
			break;
		case 0xFF313031:
			color = 0xFF696969;
			break;
		case 0xFF312C29:
			color = 0xFF7A6D64;
			break;
		case 0xFF312C2A:
			color = 0xFF6C6154;
			break;
		case 0xFF311400:
			color = 0xFF6E5746;
			break;
		case 0xFFCEE7F7:
			color = 0xFF87B0CB;
			break;
		case 0xFFEFEBD6:
			color = 0xFFEBE3BC;
			break;
		case 0xFFEFE3E7:
			color = 0xFFC0BBBD;
			break;
		case 0xFFEFE3E8:
			color = 0xFFA69EA1;
			break;
		default:
			color = text_color;
			break;
		}
		return color;
	}

    public static int getMatchFontColor(int origialColor) {
        if (LocalUserSetting.readStyle != ReadOverlayActivity.READ_STYLE_NIGHT) {
            if (isIgnoreCssTextColor) {
                return getFontColor();
            }
            return origialColor;
        }
        if (origialFontColor == origialColor) {
            return matchFontColor;
        }
        origialFontColor = origialColor;
        String hexColor = Integer.toHexString(origialColor);
        if (hexColor.length() < 8)
            return origialColor;
        String a = hexColor.substring(0, 2);
        String r = hexColor.substring(2, 4);
        String g = hexColor.substring(4, 6);
        String b = hexColor.substring(6);
        int alpha = Integer.parseInt(a, 16);
        int red = Integer.parseInt(r, 16);
        int green = Integer.parseInt(g, 16);
        int blue = Integer.parseInt(b, 16);
        if (red < 36 && green < 36 && blue < 36) {// 36是参考iOS的数值
            matchFontColor = getFontColor();
        } else {
            matchFontColor = Color.argb(alpha, 255 - red, 255 - green, 255 - blue);
        }
        return matchFontColor;
    }

    @SuppressWarnings("unused")
    public static int getMergedBgColor(int origial) {
        if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_WHITE) {
            return origial;
        }
        int settingColor = BookPageViewActivity.getBackgroundColor();
        if (origialBgColor == origial && userSettingBgColor == settingColor) {
            return mergedBgColor;
        }
        origialBgColor = origial;
        userSettingBgColor = settingColor;
        String hexColor = Integer.toHexString(origial);
        if (hexColor.length() < 8)
            return origial;
        String a = hexColor.substring(0, 2);
        String r = hexColor.substring(2, 4);
        String g = hexColor.substring(4, 6);
        String b = hexColor.substring(6);
        int red1 = Integer.parseInt(r, 16);
        int green1 = Integer.parseInt(g, 16);
        int blue1 = Integer.parseInt(b, 16);
        red1 = (int) (red1 * 0.7);
        green1 = (int) (green1 * 0.7);
        blue1 = (int) (blue1 * 0.7);

        hexColor = Integer.toHexString(settingColor);
        MZLog.d("performance", "sdafdf " + hexColor);
        a = hexColor.substring(0, 2);
        r = hexColor.substring(2, 4);
        g = hexColor.substring(4, 6);
        b = hexColor.substring(6);
        int red2 = Integer.parseInt(r, 16);
        int green2 = Integer.parseInt(g, 16);
        int blue2 = Integer.parseInt(b, 16);
        red2 = (int) (red2 * 0.3);
        green2 = (int) (green2 * 0.3);
        blue2 = (int) (blue2 * 0.3);

        mergedBgColor = Color.argb(255, red1 + red2, green1 + green2, blue1 + blue2);
        return mergedBgColor;
    }

    @Override
    public void refreshNotes() {
        if (pageAdapter != null) {
            pageAdapter.refreshNote();
        }
    }

    @Override
    public BookMark createEmptyMark() {
        BookMark mark = new BookMark();
        if (eBook != null) {
            mark.ebookid = eBook.bookId;
        }
        if (document != null) {
            mark.docid = document.documentId;
        }
        mark.userid = userId;
        mark.bookType = LocalBook.FORMAT_EPUB;
        return mark;
    }

    @Override
    public NotesModel findMatchNoteUser(String userId) {
        // 导入笔记的那些人中间查找
        for (NotesModel model : notesModelList) {
            if (model.userid.equals(userId)) {
                return model;
            }
        }
        // 所有做笔记的人中间查找
        synchronized (this) {
            for (NotesModel model : allNotesModelList) {
                if (model.userid.equals(userId)) {
                    return model;
                }
            }
        }
        return null;
    }

    private synchronized void addAllNotesModel(List<NotesModel> models) {
        allNotesModelList.clear();
        allNotesModelList.addAll(models);
    }

    @Override
    public void commentNote(String guid, long noteId) {
        Intent intent = new Intent();
        if (UiStaticMethod.isNullString(guid)) {
            intent.putExtra(TimelineTweetActivity.NOTE_ID, noteId);
        } else {
            intent.putExtra(TimelineTweetActivity.TWEET_GUID, guid);
        }
        intent.setClass(BookPageViewActivity.this, TimelineTweetActivity.class);
        startActivity(intent);
    }

    @Override
    public void requestAllNotesModel() {
        LoadAllUsersTask loadUserTask = new LoadAllUsersTask();
        loadUserTask.requestAllUsers();
    }

    private void loadChapterAllNotes(Chapter chapter) {
        if (chapter.isShowAllNotes() && NetWorkUtils.isNetworkConnected(BookPageViewActivity.this) && !chapter.isLoadAllNotesDone()) {
            LoadChapterAllNotesTask loadTask = new LoadChapterAllNotesTask(chapter);
            loadTask.requestAllNotesForChapter();
        }
    }

    private class LoadChapterAllNotesTask {

        private Chapter taskChapter;

        LoadChapterAllNotesTask(Chapter chapter) {
            taskChapter = chapter;
        }

        protected void requestAllNotesForChapter() {
            taskChapter.setLoadAllNotesDone(true);
            String url = URLText.getChapterAllNotes;
            String itemref = "";
            long bookId = 0;
            try {
                itemref = URLEncoder.encode(taskChapter.getSpine().spineIdRef, "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            if (TextUtils.isEmpty(itemref)) {
                return;
            }
            String documentSign = document == null ? null : document.opfMD5;
            if (eBook != null && eBook.bookId != 0) {
                bookId = eBook.bookId;
            } else if (!TextUtils.isEmpty(documentSign)) {
            } else {
                return;
            }
            RequestParams request = RequestParamsPool.getChapterAllNotes(bookId, documentSign, itemref, LoginUser.getpin());
            WebRequestHelper.get(url, request, true, new MyAsyncHttpResponseHandler(BookPageViewActivity.this) {

                @Override
                public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
                    String result = new String(responseBody);
                    JSONObject resultObject = null;
                    try {
                        resultObject = new JSONObject(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (resultObject != null) {
                        JSONArray notesArr = resultObject.optJSONArray("notes");
                        List<ReadNote> notes = new ArrayList<ReadNote>();
                        if (notesArr != null && notesArr.length() > 0) {
                            for (int i = 0; i < notesArr.length(); i++) {
                                JSONObject notesObject = notesArr.optJSONObject(i);
                                ReadNote note = ReadNote.parseFromJson(notesObject, document == null ? 0 : document.documentId);
                                note.modified = false;
                                notes.add(note);
                            }
                        }
                        taskChapter.addAllPeopleNote(notes);
                    } else {
                        taskChapter.setLoadAllNotesDone(false);
                    }

                    if (pageAdapter != null) {
                        pageAdapter.refreshNote();
                    }
                }

            });

            return;
        }
    }

    private class LoadAllUsersTask {

        protected void requestAllUsers() {
            long book_id = 0;
            String baseUrl = URLText.getAllNotesAuthorsOfBook;
            String documentSign = document == null ? null : document.opfMD5;
            if (eBook != null && eBook.bookId != 0) {
                book_id = eBook.bookId;
            } else if (!TextUtils.isEmpty(documentSign)) {
            } else {
                return;
            }
            RequestParams request = RequestParamsPool.getAllNotesAuthors(book_id, documentSign);
            WebRequestHelper.get(baseUrl, request, true, new MyAsyncHttpResponseHandler(BookPageViewActivity.this) {

                @Override
                public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
                    String result = new String(responseBody);
                    List<NotesModel> models = new ArrayList<NotesModel>();
                    try {
                        JSONArray array = new JSONArray(result);
                        if (array == null || array.length() == 0)
                            return;

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            NotesModel model = NotesModel.parseFromJson(object);
                            models.add(model);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    addAllNotesModel(models);
                }

            });
            return;
        }
    }

    @Override
    public void doJumpAnchorAction(String id) {
        if (TextUtils.isEmpty(id)) {
            return;
        }

        String anchor = null;
        Chapter chapter = null;

        if (pageAdapter != null && pageAdapter.currentPage != null) {
            chapter = pageAdapter.currentPage.getChapter();
            if (chapter != null) {
                anchor = chapter.getAnchor(id);
            }
        }

        if (TextUtils.isEmpty(anchor)) {
            for (Chapter c : chapterList) {
                anchor = c.getAnchor(id);
                if (!TextUtils.isEmpty(anchor)) {
                    chapter = c;
                    break;
                }
            }
        }

        if (!TextUtils.isEmpty(anchor)) {
            String[] param = anchor.split(",");
            backProgress = progress.clone();
            progress.chapterItemRef = chapter.getSpine().spineIdRef;
            progress.paraIndex = Integer.valueOf(param[0]);
            progress.offsetInPara = Integer.valueOf(param[1]);
            GoToProgressTask task = new GoToProgressTask();
            task.execute("");
        }
    }

    @Override
    public void doJumpChapterAction(String src, String basePath) {
        if (TextUtils.isEmpty(src)) {
            return;
        }

        Uri uri = Uri.parse(src);
        String relPath = uri.getPath();
        String path = FilePath.resolveRelativePath(basePath, relPath);
        String id = uri.getFragment();

        for (Chapter chapter : chapterList) {
            if (chapter.getSpine().spinePath.endsWith(path)) {
                backProgress = progress.clone();
                progress.paraIndex = 0;
                progress.offsetInPara = 0;
                progress.chapterItemRef = chapter.getSpine().spineIdRef;
                GoToProgressTask task = new GoToProgressTask();
                task.execute(id);
                break;
            }
        }
    }


    @Override
    public void shareReadNoteViaSinaWeibo(ReadNote note, Bitmap bitmap) {
        if (note == null || bitmap == null) {
            dismissHUD();
            return;
        }

        if (note.serverId <= 0) {
            List<ReadNote> noteList = new ArrayList<ReadNote>();
            noteList.add(note);

            if (shareReadNoteBitmap != null && !shareReadNoteBitmap.isRecycled()) {
                shareReadNoteBitmap.recycle();
            }

            shareReadNoteBitmap = bitmap;
            requestUploadReadNote(noteList, true, false, 0);
        } else {
            startShareBitmapViaSinaWeibo(note.serverId, bitmap);
        }
    }

    private void startShareBitmapViaSinaWeibo(long noteServerId, Bitmap bitmap) {
        if (bitmap != null) {
            SharePopupWindow sharePopupWindow = new SharePopupWindow(this);
            sharePopupWindow.shareToWeiboByBitmap(this, "《" + bookName + "》来自@京东阅读", "", bitmap, "http://e.m.jd.com/edm.html", "");
            post(LoginUser.getpin(), String.valueOf(noteServerId));

            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        dismissHUD();
    }

    @Override
    public void shareReadNoteViaWeixin(ReadNote note, Bitmap bitmap, int type) {
        if (note == null) {
            dismissHUD();
            return;
        }

        if (bitmap != null) {// 说明是本地内置书籍
            startShareBitmapViaWeixin(note, bitmap, type);
            return;
        }

        if (note.serverId <= 0) {
            List<ReadNote> noteList = new ArrayList<ReadNote>();
            noteList.add(note);

            if (shareReadNoteBitmap != null && !shareReadNoteBitmap.isRecycled()) {
                shareReadNoteBitmap.recycle();
            }

            shareReadNoteBitmap = bitmap;
            requestUploadReadNote(noteList, false, true, type);
        } else {
            startShareBitmapViaWeixin(note, bitmap, type);
        }
    }

    private void startShareBitmapViaWeixin(ReadNote note, Bitmap bitmap, int type) {
        if (bitmap != null) {
            WXShareHelper.getInstance().shareImage(this, bitmap, type);

            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        } else {
            String coverImgPath;

            if (!TextUtils.isEmpty(coverPath)) {
                coverImgPath = coverPath;
            }else{
                coverImgPath = getBookCoverPath();
            }

            Bitmap coverBitmap = ImageUtils.getBitmapFromNamePath(coverImgPath, 200,200);
            String url = "http://e.m.jd.com/ebook_ebookNoteDetail.action?noteId=" + note.serverId;
            String noteText = note.contentText;
            String sourceText = note.quoteText;
            String desc;

            if (TextUtils.isEmpty(noteText)) {
                desc = "【原文】"+sourceText;
            }else{
                desc = "【笔记】"+noteText+"\n"+"【原文】"+sourceText;
            }

            WXShareHelper.getInstance().doShare(BookPageViewActivity.this, bookName, desc, coverBitmap, url, type, new ShareResultListener() {

                @Override
                public void onShareRusult(int resultType) {
                    switch (resultType) {
                    case ShareResultListener.SHARE_SUCCESS:
                        CustomToast.showToast(BookPageViewActivity.this, "分享成功");
                        break;
                    case ShareResultListener.SHARE_CANCEL:
                        CustomToast.showToast(BookPageViewActivity.this, "分享取消");
                        break;
                    case ShareResultListener.SHARE_FAILURE:
                        CustomToast.showToast(BookPageViewActivity.this, "分享失败");
                        break;
                    }
                }
            });
        }

        post(LoginUser.getpin(), String.valueOf(note));
        dismissHUD();
    }

    private void post(final String pin, final String noteServerId) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                WebRequestHelper.post(URLText.SHARE_URL, RequestParamsPool.getShareParams(pin, noteServerId, "Note"), true, new MyAsyncHttpResponseHandler(
                        BookPageViewActivity.this) {

                    @Override
                    public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
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
                                    // String message =
                                    // jsonObj.optString("message");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private ProgressHUD mLoainHUD;

    @Override
    public void showLoading() {
        if (mLoainHUD != null && mLoainHUD.isShowing()) {
            return;
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    mLoainHUD = ProgressHUD.pageLoaing(BookPageViewActivity.this);
                }
            }
        });
    }

    private void dismissHUD() {
        if (mLoainHUD != null && mLoainHUD.isShowing()) {
            mLoainHUD.dismiss();
        }
    }
    
    @Override
    public void showBackCover() {
        Intent intent = new Intent(this, BackCoverRecommendActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (eBook != null) {
            intent.putExtra(BackCoverRecommendActivity.BOOK_ID_KEY, eBook.bookId);
            intent.putExtra(BackCoverRecommendActivity.IS_TRY_READ_KEY, JDDecryptUtil.isTryRead);
        } else if (document != null) {
            intent.putExtra(BackCoverRecommendActivity.DOC_ID_KEY, document.documentId);
            if (docBind != null) {
                if (docBind.bookId != 0) {
                    intent.putExtra(BackCoverRecommendActivity.BOOK_ID_KEY, docBind.bookId);
                }
                if (docBind.serverId != 0) {
                    intent.putExtra(BackCoverRecommendActivity.DOC_SERVER_ID_KEY, docBind.serverId);
                }
            }
        }
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
    
    private void loadReadSearchData() {
        ArrayList<ReadSearchData> searchList = new ArrayList<ReadSearchData>();
        for (int i = 0, n = chapterList.size(); i < n; i++) {
            if (isExitBookPage) {
                break;
            }
            Chapter chapter = chapterList.get(i);
            List<ReadSearchData> result = chapter.getReadSearchList();
            searchList.addAll(result);
        }
        Intent it = new Intent();
        it.setAction(BookSearchActivity.ACTION_LOAD_SEARCH_DATA);
        it.putExtra(BookSearchActivity.SEARCH_KEYWORDS, searchKeywords);
        it.putParcelableArrayListExtra(BookSearchActivity.SEARCH_RESULT, searchList);
        LocalBroadcastManager.getInstance(BookPageViewActivity.this).sendBroadcast(it);
    }

    private class SearchTask extends AsyncTask<Void, Void, Void> {

        private boolean isCancelSearch = false;

        void setCancel(boolean cancel) {
            this.isCancelSearch = cancel;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String word = new String(searchKeywords);
            for (int i = 0, n = chapterList.size(); i < n; i++) {
                if (isExitBookPage) {
                    break;
                }
                if (isCancelSearch || isCancelled()) {
                    break;
                }
                Chapter chapter = chapterList.get(i);
                ArrayList<ReadSearchData> result = chapter.pageSearch(word);
                Intent it = new Intent();
                it.setAction(BookSearchActivity.ACTION_SEARCH_DONE);
                it.putExtra(BookSearchActivity.SEARCH_KEYWORDS, word);
                it.putExtra(BookSearchActivity.SEARCH_DONE, i == n - 1);
                it.putParcelableArrayListExtra(BookSearchActivity.SEARCH_RESULT, result);
                LocalBroadcastManager.getInstance(BookPageViewActivity.this).sendBroadcast(it);
            }
            return null;
        }

    }

    @Override
    public int getBatteryPercent() {
        return batteryPercent;
    }

    @Override
    public void refresh(DownloadedAble downloadAble) {
        // RefreshAble interface
        if(null == downloadAble) {
            return;
        }
        
        if (downloadAble instanceof FontItem) {
            FontItem fontItem = (FontItem) downloadAble;
            if(null == fontItem) {
                return;
            }
            if (fontItem.getDownloadStatus() == FontItem.STATE_LOADED) {
                DBHelper.updateFontStatus(fontItem);
                if(null == fzssFontItem || fzktFontItem ==null || fzlthFontItem == null ||fontItem==null || fontItem.getName() ==null) {
                    return;
                }
                if (FontItem.FOUNDER_SS.equals(fontItem.getName())) {
                    fzssFontItem.setDownloadStatus(FontItem.STATE_LOADED);
                } else if (FontItem.FOUNDER_KAITI.equals(fontItem.getName())) {
                    if(fzktFontItem != null)
                        fzktFontItem.setDownloadStatus(FontItem.STATE_LOADED);
                } else if (FontItem.FOUNDER_LANTINGHEI.equals(fontItem.getName())) {
                    if(fzlthFontItem != null)
                        fzlthFontItem.setDownloadStatus(FontItem.STATE_LOADED);
                }
                changeFontFace();
            }
        }
    }

    @Override
    public int getType() {
        // RefreshAble interface
        return 0;
    }

    @Override
    public void refreshDownloadCache() {
        // RefreshAble interface
    }

    @Override
    public void gotoSearchPage(int chapterIndex, int paraIndex, int offsetInPara) {
        isShowSearchBar = true;
        viewPager.setPagingEnabled(false);
        Chapter chapter = chapterList.get(chapterIndex);
        backProgress = progress.clone();
        int pageNumber = chapter.getChapterPageOffset();
        int totalNumber = chapter.getBookPageCount();
        if (pageNumber != -1 && totalNumber != -1) {
            progress.percent = (pageNumber + 1) / (float) totalNumber;

        } else {
            progress.percent = chapterIndex / (float) chapterList.size();
        }
        Spine spine = chapter.getSpine();
        progress.chapterItemRef = spine.spineIdRef;
        progress.paraIndex = paraIndex;
        progress.offsetInPara = offsetInPara;
        GoToProgressTask task = new GoToProgressTask();
        task.execute("");
    }
    
    @Override
    public boolean isFirstPage(Page page) {
        if (isShowBookCover) {
            if (page instanceof CoverPage) {
                return true;
            }
        } else {
            if (page != null && page.getChapter() != null) {
                if (page.getChapter().isFirstChapter()) {
                    return page.getChapter().isFirstPage(page);
                }
                return false;
            }
        }
        return false;
    }
    
    @Override
    public boolean isFinishPage(Page page) {
        if (page instanceof FinishPage) {
            return true;
        }
        if (page != null && page.getChapter() != null) {
            if (page.getChapter().isLastChapter()) {
                return page.getChapter().isLastPage(page);
            }
            return false;
        }
        return false;
    }
    
    @Override
    public boolean isPageAnimationTurning() {
        return isPageAnimationTurning;
    }
    
    @Override
    public boolean isPageAnimationSlide() {
        return isPageAnimationSlide;
    }
}
