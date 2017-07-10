package com.jingdong.app.reader.pdf;

import com.jingdong.app.reader.reading.BackCoverRecommendActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.OutlineItem;

import com.baidu.mobstat.StatService;
import com.bob.android.lib.slide.Item;
import com.bob.android.lib.slide.OnItemListener;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookCartActivity;
import com.jingdong.app.reader.activity.BookCommentNewuiActivity;
import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.activity.BookcaseCloudActivity;
import com.jingdong.app.reader.activity.CatalogActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.book.EBook;
import com.jingdong.app.reader.bookmark.BookMarksFragment;
import com.jingdong.app.reader.bookshelf.BookcaseLocalFragmentNewUI;
import com.jingdong.app.reader.bookshelf.animation.EBookAnimationUtils;
import com.jingdong.app.reader.bookshelf.animation.EBookAnimationUtils.OnAnimationListener;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager.AddToCartListener;
import com.jingdong.app.reader.bookstore.sendbook.SendBookFirstPageActivity;
import com.jingdong.app.reader.bookstore.sendbook.SendBookReceiveInfo;
import com.jingdong.app.reader.bookstore.sendbook.SendBookReceiveInfo.SendBookReceiveInfos;
import com.jingdong.app.reader.common.CommonActivity;
import com.jingdong.app.reader.data.DrmTools;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.extra.BuyedEbook;
import com.jingdong.app.reader.entity.extra.JDEBook;
import com.jingdong.app.reader.entity.extra.SimplifiedDetail;
import com.jingdong.app.reader.epub.JDDecryptUtil;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.ReadGetTimeListener;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.reading.BookBackCoverView.IBackCoverActionListener;
import com.jingdong.app.reader.reading.BookMark;
import com.jingdong.app.reader.reading.ReadProgress;
import com.jingdong.app.reader.reading.ReadingData;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ActivityUtils;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.util.RC4Encrypt;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.SettingUtils;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.UnzipFile;
import com.jingdong.app.reader.util.UserGuiderUtil;
import com.jingdong.app.reader.util.UserGuiderUtil.GuiderCoverClickListener;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

public class PDFBookViewActivity extends CommonActivity implements IBackCoverActionListener {

    public static final String ACTION_PDF_REOPEN            = "com.jingdong.app.reader.pdf.ACTION_PDF_REOPEN";
    public static final String ACTION_PDF_PAGE_TURNING_MODE = "com.jingdong.app.reader.pdf.ACTION_PDF_PAGE_TURNING_MODE";
    public static final String ACTION_PDF_LOCK_SCREEN_MODE  = "com.jingdong.app.reader.pdf.ACTION_PDF_LOCK_SCREEN_MODE";

    public static final String ChangePageKey = "ChangePageKey";

    private static final int ReadSettingRequest = 0;
    private static final int TOCRequest = 1;

    public static final int RESULT_OPEN_TOC             = Activity.RESULT_FIRST_USER;
    public static final int RESULT_VIEW_BACK            = Activity.RESULT_FIRST_USER + 1;
    public static final int RESULT_PURCHASE_FULL_BOOK   = Activity.RESULT_FIRST_USER + 2;
    public static final int RESULT_SEARCH_BOOK          = Activity.RESULT_FIRST_USER + 3;
    public static final int RESULT_TOGGLE_BOOK_MARK     = Activity.RESULT_FIRST_USER + 4;

    public static int BOOK_PDF_TURNING_MODE         = 0;
    public static int BOOK_PDF_SCREEN_ORITATION     = 0;

    public static final int BOOK_PDF_TURNING_MODE_H = 0;
    public static final int BOOK_PDF_TURNING_MODE_V = 1;

    public static final int BOOK_PDF_FONTSIZE_MIN   = 1;
    public static final int BOOK_PDF_FONTSIZE_MAX   = 5;

    private MuPDFCore core              = null;
    private MuPDFReaderView pdfReader   = null;

    private final Configuration mCurConfig = new Configuration();

    private boolean isFromAnimActivity = false;// 是否来自动画的activity如果是，将取消默认的activity跳转。
    private int mLastPostion = -1;
    private int mNextPostion = -1;

    private ReadProgress progress = new ReadProgress();
    private ReadProgress localProgress;
    private ReadProgress startProgress;
    private ReadProgress endProgress;
    private boolean serverProgressSynced = false;
    private boolean isFirstOpenBook = false;
    private EBook eBook;
    private Document document;
    private DocBind docBind;
    private long bookId;
    private int documentId;

    private String bookKey;
    private String deviceUUID;
    private String random;
    private String bookPath;
    private String userId;

    private List<BookMark> bookMarkList = new ArrayList<BookMark>();
    private boolean isUseVolumePage = false;
    private boolean isExitBookPage = false;
    private boolean isShowPurchaseButton = false;
    private boolean mScreenOrientationLocked = false;
    private Context context;

    private PDFReadingReceiver receiver = new PDFReadingReceiver();

    class PDFReadingReceiver extends BroadcastReceiver{
    	
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_PDF_REOPEN))
            {
                if (PDFDeviceInfo.NonStandardFontsNumb == 0)
                {
                    new AlertDialog.Builder(PDFBookViewActivity.this, ProgressDialog.THEME_HOLO_LIGHT).setTitle(getString(R.string.info))
                            .setMessage(R.string.pdf_font_reopen)
                            .setPositiveButton(getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1)
                                        {
                                        	exit();
                                        }
                                    })
                            .create()
                            .show();
                }
                else
                {
                    new AlertDialog.Builder(PDFBookViewActivity.this, ProgressDialog.THEME_HOLO_LIGHT).setTitle(getString(R.string.info))
                            .setMessage("网络连接异常，请联网后重试")
                            .setPositiveButton(getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1)
                                        {
//                                                                    PDFBookViewActivity.this.finish();
                                        }
                                    })
                            .create()
                            .show();
                    PDFDeviceInfo.NonStandardFontsNumb = 0;
                }
            }
            else if (intent.getAction().equals(ACTION_PDF_LOCK_SCREEN_MODE))
            {
                setupScreenOrientation();
            }
            else if (intent.getAction().equals(ACTION_PDF_PAGE_TURNING_MODE))
            {
                int previousMode = BOOK_PDF_TURNING_MODE;

                if (LocalUserSetting.useVerticalPage(PDFBookViewActivity.this))
                {
                    BOOK_PDF_TURNING_MODE = BOOK_PDF_TURNING_MODE_V;
                }
                else
                {
                    BOOK_PDF_TURNING_MODE = BOOK_PDF_TURNING_MODE_H;
                }

                if (pdfReader != null && previousMode != BOOK_PDF_TURNING_MODE)
                {
                    pdfReader.setDisplayedViewIndex(progress.pdfPage);
                }
            }
            else if (intent.getAction().equals(PDFReadOverlayActivity.ACTION_SETTING_FONT_CHANGE))
            {
                float zoomLevel = intent.getFloatExtra(PDFReadOverlayActivity.ZoomLevelKey, BOOK_PDF_FONTSIZE_MIN);

                if (zoomLevel < BOOK_PDF_FONTSIZE_MIN)
                {
                    zoomLevel = BOOK_PDF_FONTSIZE_MIN;
                }
                else if (zoomLevel > BOOK_PDF_FONTSIZE_MAX)
                {
                    zoomLevel = BOOK_PDF_FONTSIZE_MAX;
                }

                if (pdfReader != null)
                {
                    pdfReader.setScaleX(zoomLevel);
                    pdfReader.setScaleY(zoomLevel);
                }
            }
            else if (intent.getAction().equals(PDFReadOverlayActivity.ACTION_PAGENUMBER_CHANGE))
            {
                int pageNumber = intent.getIntExtra(ChangePageKey, -1);

                if (pdfReader != null && pageNumber >= 0)
                {
                    mLastPostion = pdfReader.getDisplayedViewIndex();
                    pdfReader.setDisplayedViewIndex(pageNumber);
                    Intent in = new Intent(PDFReadOverlayActivity.ACTION_BACK_PROGRESS_DONE);
                    in.putExtra(PDFReadOverlayActivity.BookMarkStateKey, isBookMarked(pageNumber));
                    LocalBroadcastManager.getInstance(PDFBookViewActivity.this).sendBroadcast(in);
                }
            }
            else if (intent.getAction().equals(PDFReadOverlayActivity.ACTION_GO_BACK_PROGRESS))
            {
                if (pdfReader != null)
                {
                    mNextPostion = pdfReader.getDisplayedViewIndex();
                    pdfReader.setDisplayedViewIndex(mLastPostion);
                    Intent in = new Intent(PDFReadOverlayActivity.ACTION_CHANGE_PROGRESS_DONE);
                    in.putExtra(PDFReadOverlayActivity.CurrentPageIndexKey, mLastPostion);
                    in.putExtra(PDFReadOverlayActivity.BookMarkStateKey, isBookMarked(mLastPostion));
                    LocalBroadcastManager.getInstance(PDFBookViewActivity.this).sendBroadcast(in);
                }
            }
            else if (intent.getAction().equals(PDFReadOverlayActivity.ACTION_GO_FORWARD_PROGRESS))
            {
                if (pdfReader != null)
                {
                    pdfReader.setDisplayedViewIndex(mNextPostion);
                    Intent in = new Intent(PDFReadOverlayActivity.ACTION_CHANGE_PROGRESS_DONE);
                    in.putExtra(PDFReadOverlayActivity.CurrentPageIndexKey, mNextPostion);
                    in.putExtra(PDFReadOverlayActivity.BookMarkStateKey, isBookMarked(mNextPostion));
                    LocalBroadcastManager.getInstance(PDFBookViewActivity.this).sendBroadcast(in);
                }
            }
            else if (intent.getAction().equals(BookMarksFragment.ACTION_RELOAD_BOOKMARK))
            {
                loadBookMarks();
            }
            else if (intent.getAction().equals(BackCoverRecommendActivity.ACTION_PURCHASE_BOOK))
            {
                purchaseFullBook();
            }
        }
    }

    private void ShowRelatedPage ()
    {
        if (pdfReader.getDisplayedViewIndex() == core.countPages() - 1)
        {
            Intent intent = new Intent(PDFBookViewActivity.this, BackCoverRecommendActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (eBook != null)
            {
                intent.putExtra(BackCoverRecommendActivity.BOOK_ID_KEY, eBook.bookId);
                intent.putExtra(BackCoverRecommendActivity.IS_TRY_READ_KEY, JDDecryptUtil.isTryRead);
            }
            else if (document != null)
            {
                intent.putExtra(BackCoverRecommendActivity.DOC_ID_KEY, document.documentId);

                if (docBind != null)
                {
                    if (docBind.bookId != 0)
                    {
                        intent.putExtra(BackCoverRecommendActivity.BOOK_ID_KEY, docBind.bookId);
                    }

                    if (docBind.serverId != 0)
                    {
                        intent.putExtra(BackCoverRecommendActivity.DOC_SERVER_ID_KEY, docBind.serverId);
                    }
                }
            }

            startActivity(intent);

            if (PDFBookViewActivity.BOOK_PDF_TURNING_MODE == PDFBookViewActivity.BOOK_PDF_TURNING_MODE_V)
            {
                overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
            }
            else if (PDFBookViewActivity.BOOK_PDF_TURNING_MODE == PDFBookViewActivity.BOOK_PDF_TURNING_MODE_H)
            {
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        }
    }

    private void ShowNonStandardFontsDialog ()
    {
        final HashSet<String> NonStandardFonts = new HashSet<>();

        if (!new File(PDFDeviceInfo.NonStandardFontsPath).exists())
        {
            new File(PDFDeviceInfo.NonStandardFontsPath).mkdir();
        }

        if (null != PDFDeviceInfo.NonStandardFontsName) 
        {
        	if (!PDFDeviceInfo.NonStandardFontsName.isEmpty())
            {
                for (String EUFont : PDFDeviceInfo.NonStandardFontsName)
                {
                    if (!new File(PDFDeviceInfo.NonStandardFontsPath + EUFont + ".TTF").exists())
                    {
                        NonStandardFonts.add(EUFont);
                        PDFDeviceInfo.NonStandardFontsNumb += 1;
                    }
                }
            }
        }

        if (!NonStandardFonts.isEmpty())
        {
        	if(isFinishing()) {
        		return;
        	}
            DialogManager.showCommonDialog(this, "温馨提示", "需要下载字体以获得正确显示", "马上下载", "下次再说",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                case DialogInterface.BUTTON_POSITIVE:
                                    {
                                        for (final String font : NonStandardFonts)
                                        {
                                            String NSFontWeb = "http://app.e.360buy.com/font/" + font + ".jps";
                                            final String NSFontLoc = PDFDeviceInfo.NonStandardFontsPath + font;

                                            AsyncHttpClient client = new AsyncHttpClient();
                                            client.get(NSFontWeb, new BinaryHttpResponseHandler() {
                                                @Override
                                                public void onSuccess(int a, Header[] b, byte[] data) {
                                                    try {
                                                        File fontFile = new File(NSFontLoc);
                                                        fontFile.createNewFile();
                                                        FileOutputStream fontStream = new FileOutputStream(fontFile);
                                                        fontStream.write(data);
                                                        fontStream.close();

                                                        try {
                                                            RC4Encrypt.crypt(NSFontLoc, NSFontLoc + ".j");
                                                            UnzipFile.unZipFile(NSFontLoc + ".j", PDFDeviceInfo.NonStandardFontsPath);

                                                            new File(NSFontLoc).delete();
                                                            new File(NSFontLoc + ".j").delete();

                                                            PDFDeviceInfo.NonStandardFontsNumb -= 1;

                                                            if (PDFDeviceInfo.NonStandardFontsNumb == 0)
                                                            {
                                                                Intent intent = new Intent(PDFBookViewActivity.ACTION_PDF_REOPEN);
                                                                LocalBroadcastManager.getInstance(MZBookApplication.getContext()).sendBroadcast(intent);
                                                            }
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(int a, Header[] b, byte[] c, Throwable d) {
                                                    Intent intent = new Intent(PDFBookViewActivity.ACTION_PDF_REOPEN);
                                                    LocalBroadcastManager.getInstance(MZBookApplication.getContext()).sendBroadcast(intent);
                                                }
                                            });
                                        }

                                        dialog.dismiss();
                                    }
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    });
        }
    }

    private MuPDFCore openFile(){
    	
        PDFDeviceInfo.resetInfo();
        PDFDeviceInfo.KeyBook = bookKey.getBytes();
        PDFDeviceInfo.KeyUUID = deviceUUID.getBytes();
        PDFDeviceInfo.KeyRand = random.getBytes();

        try {
            core = new MuPDFCore(bookPath);
        } catch (Exception e) {
            e.printStackTrace();
            core = null;
        }

        return core;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setupScreenOrientation();

        pdfReader = new MuPDFReaderView(this){
            @Override
            protected void onMoveToChild(int i)
            {
                super.onMoveToChild(i);
            }

            @Override
            protected void onTapMainDocArea()
            {
                menuViewToggle();
            }

            @Override
            protected void onDocMotion()
            {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
            {
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

            	if(e2.getX() - e1.getX() < - metrics.widthPixels / 2)
            		ShowRelatedPage();

                return super.onFling(e1, e2, velocityX, velocityY);
            }
        };

        RelativeLayout layout = new RelativeLayout(this);
        layout.addView(pdfReader);
        layout.setBackgroundResource(com.artifex.mupdfdemo.R.drawable.tiled_background);
        setContentView(layout);

        bookId = getIntent().getLongExtra(OpenBookHelper.EBookIdKey, 0);
        documentId = getIntent().getIntExtra(OpenBookHelper.DocumentIdKey, 0);

        if (bookId > 0)
        {
            eBook = MZBookDatabase.instance.getEBookByBookId(bookId);
        }

        if (documentId > 0)
        {
            document = MZBookDatabase.instance.getDocument(documentId);
        }

        if (eBook == null && document == null)
        {
            return;
        }

        registerReceiver();

        userId = LoginUser.getpin();
        deviceUUID = DrmTools.hashDevicesInfor();

        if (eBook != null){
            LocalBook book = LocalBook.getLocalBook(eBook.bookId,LoginUser.getpin());
            random = book.random;
            bookKey = book.cert;
            bookPath = book.book_path;
            progress = MZBookDatabase.instance.getEbookReadProgress(userId, eBook.bookId);
            progress.bookType = LocalBook.FORMAT_PDF;
            isFirstOpenBook = progress.updateTime <= 0;
            localProgress = progress.clone();

            if (LocalBook.SOURCE_TRYREAD_BOOK.equals(book.source) ||
                    LocalBook.SOURCE_BORROWED_BOOK.equals(book.source) ||
                    LocalBook.SOURCE_ONLINE_BOOK.equals(book.source)){
                isShowPurchaseButton = true;
            }

            // 如果试读书已经购买，提示用户下载全本
            if (LocalBook.SOURCE_TRYREAD_BOOK.equals(book.source)){
            	
                if (NetWorkUtils.isNetworkConnected(PDFBookViewActivity.this)){
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
                                                if (book.bookId.equals(eBook.bookId)) {
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
        }
        else if (document != null){
            File file = new File(document.bookPath);

            if (!file.exists()){
                SettingUtils.getInstance().putBoolean("file_error:" + bookId, true);
                ToastUtil.showToastInThread(getString(R.string.file_dont_exist), Toast.LENGTH_SHORT);
                return;
            }

            random = "";
            bookKey = "";
            bookPath = document.bookPath;
            progress = MZBookDatabase.instance.getDocReadProgress(userId, document.documentId);
            progress.bookType = LocalBook.FORMAT_PDF;
            localProgress = progress.clone();
            docBind = MZBookDatabase.instance.getDocBind(document.documentId, userId);
        }

        if (core == null){
            core = openFile();
            pdfReader.setSelectionMode(false);
            pdfReader.setAdapter(new MuPDFPageAdapter(this, core));

            if (core != null && core.needsPassword()){
                return;
            }
        }

        if (core == null)
        {
            SettingUtils.getInstance().putBoolean("file_error:" + bookId, true);
            ToastUtil.showToastInThread(R.string.pdf_open_fail);
            exit();

            return;
        }

        ShowNonStandardFontsDialog();

        BOOK_PDF_TURNING_MODE =
                LocalUserSetting.useVerticalPage(this) ? BOOK_PDF_TURNING_MODE_V : BOOK_PDF_TURNING_MODE_H;

        loadBookMarks();

        Display d = getWindowManager().getDefaultDisplay();
        Point s = new Point();
        d.getSize(s);

        int pageCount = core.countPages();

        if (pageCount <= 0)
        {
        	exit();
            return;
        }

        pdfReader.setDisplayedViewIndex(progress.pdfPage);

        if (eBook != null)
        {
            syncReadProgressAndBookMark();
        }
        else if (document != null)
        {
            if (docBind == null || docBind.serverId == 0)
            {
                requestDocumentServerIDTask();
            }
            else
            {
                syncReadProgressAndBookMark();
            }
        }
        
		// 获赠书籍显示赠言扉页
		if (eBook != null && (TextUtils.isEmpty(eBook.source)||eBook.source.equals(LocalBook.SOURCE_BUYED_BOOK)) && LoginUser.isLogin()) {
			List<SendBookReceiveInfo> list = LocalUserSetting.getSendBookReceiveInfos(this);
			SendBookReceiveInfo info = null;
			boolean haveSendbookInfo = false;
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					info = list.get(i);
					if (!TextUtils.isEmpty(info.ebookId) && !TextUtils.isEmpty(info.userPin) && LoginUser.isLogin()
							&& info.ebookId.equals(String.valueOf(eBook.bookId)) && LoginUser.getpin().equals(info.userPin)) {
						haveSendbookInfo = true;
						break;
					}
				}
			}
			if(haveSendbookInfo ){
				checkIsFirstPage(info);
			}else{
				if(NetWorkUtils.isNetworkConnected(PDFBookViewActivity.this))
					WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool.getReceiveInfoParams(String.valueOf(eBook.bookId)),
						false, new MyAsyncHttpResponseHandler(PDFBookViewActivity.this) {

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
											List<SendBookReceiveInfo> list = LocalUserSetting.getSendBookReceiveInfos(PDFBookViewActivity.this);
											list.add(info);
											SendBookReceiveInfos infos = new SendBookReceiveInfos();
											infos.infos = list;
											LocalUserSetting.saveSendBookReceiveInfos(PDFBookViewActivity.this, infos);
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

    
	/**
	 * 检查当前进度是否该书第一页，若是，则判断是否有赠言页需要显示
	 */
	private void checkIsFirstPage(SendBookReceiveInfo info){
		if(progress !=null && progress.pdfPage == 0 && info != null){
        	Intent firstpageIntent = new Intent(this,SendBookFirstPageActivity.class);
        	firstpageIntent.putExtra("sendNickName", info.sendNickName);
			firstpageIntent.putExtra("sendMsg", info.sendMsg);
    		startActivityForResult(firstpageIntent,SendBookFirstPageActivity.SENDBOOKFIRSTPAGE);
			overridePendingTransition(R.anim.alpha_in, 0);
        }
	}
    
    private void loadBookMarks()
    {
        bookMarkList.clear();

        if (eBook != null)
        {
            bookMarkList = MZBookDatabase.instance.getAllBookMarksOfBook(userId, eBook.bookId, 0);
        }
        else if (document != null)
        {
            bookMarkList = MZBookDatabase.instance.getAllBookMarksOfBook(userId, 0, document.documentId);
        }
    }

    private void registerReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PDF_REOPEN);
        filter.addAction(ACTION_PDF_PAGE_TURNING_MODE);
        filter.addAction(ACTION_PDF_LOCK_SCREEN_MODE);
        filter.addAction(PDFReadOverlayActivity.ACTION_PAGENUMBER_CHANGE);
        filter.addAction(PDFReadOverlayActivity.ACTION_SETTING_FONT_CHANGE);
        filter.addAction(PDFReadOverlayActivity.ACTION_GO_BACK_PROGRESS);
        filter.addAction(PDFReadOverlayActivity.ACTION_GO_FORWARD_PROGRESS);
        filter.addAction(BookMarksFragment.ACTION_RELOAD_BOOKMARK);
        filter.addAction(BackCoverRecommendActivity.ACTION_PURCHASE_BOOK);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void unregisterReceiver()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private boolean isBookMarked()
    {
        if (pdfReader == null)
        {
            return false;
        }

        int pageIndex = pdfReader.getDisplayedViewIndex();
        return isBookMarked(pageIndex);
    }

    private boolean isBookMarked(int pageIndex)
    {
        for (BookMark m : bookMarkList)
        {
            if (m.pdf_page == pageIndex)
            {
                return true;
            }
        }

        return false;
    }

    private void toggleBookMark()
    {
        if (pdfReader == null)
        {
            return;
        }

        int pageIndex = pdfReader.getDisplayedViewIndex();
        boolean isAddBookMark = true;
        BookMark mark = null;

        for (BookMark m : bookMarkList)
        {
            if (m.pdf_page == pageIndex)
            {
                isAddBookMark = false;
                mark = m;
                break;
            }
        }

        if (isAddBookMark)
        {
            mark = createBookMark();

            if (mark != null)
            {
                int markId = MZBookDatabase.instance.addBookMark(mark);
                mark.id = markId;
                bookMarkList.add(mark);
                ToastUtil.showToastInThread(getString(R.string.read_toast_add_bookmark), Toast.LENGTH_SHORT);
            }
        }
        else
        {
            if (mark != null)
            {
                MZBookDatabase.instance.deleteBookMarkByUpdate(mark.id);
                bookMarkList.remove(mark);
                ToastUtil.showToastInThread(getString(R.string.read_toast_remove_bookmark), Toast.LENGTH_SHORT);
            }
        }
    }

    private BookMark createBookMark()
    {
        if (pdfReader == null)
        {
            return null;
        }

        BookMark mark = new BookMark();

        if (eBook != null)
        {
            mark.ebookid = eBook.bookId;
        }

        if (document != null)
        {
            mark.docid = document.documentId;
        }

        mark.userid = userId;
        mark.bookType = LocalBook.FORMAT_PDF;
        mark.pdf_page = pdfReader.getDisplayedViewIndex();
        
        OutlineItem[] outlineArray = core.getOutline();

        if (outlineArray == null || outlineArray.length == 0)
        {
            return mark;
        }

        int chapterIndex = 0;

        for (int i = 0; i < outlineArray.length ; i++)
        {
            if (mark.pdf_page >= outlineArray[i].page)
            {
                chapterIndex = i;
            }
            else
            {
                break;
            }
        }

        mark.chapter_title = outlineArray[chapterIndex].title;
        mark.digest = mark.chapter_title;

        return mark;
    }

    private UserGuiderUtil userGuiderUtil = null;

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus)
        {
        	EBookAnimationUtils anim = BookcaseLocalFragmentNewUI.getEBookAnimationUtils();
        	if (null != anim) {
        		anim.hideWindow();
        	}
            final WindowManager.LayoutParams layout = getWindow().getAttributes();
            float brightness = 0;

            if (LocalUserSetting.isSyncBrightness(this))
            {
                int mode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;

                try {
                    mode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
                } catch (SettingNotFoundException e) {
                    e.printStackTrace();
                }

                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
                {
                    brightness = -1 / 255f;
                }
                else
                {
                    int bright = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
                    brightness = bright / 255f;
                }
            }
            else
            {
                brightness = LocalUserSetting.getReadBrightness(this);
            }

            layout.screenBrightness = brightness;
            layout.flags = layout.flags | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            getWindow().setAttributes(layout);
        }

        //第一次打开pdf时显示新手指引：重力感应旋转
        if (!LocalUserSetting.isBookViewGravityGuidShow(this))
        {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.gravity_guide);

            userGuiderUtil = new UserGuiderUtil(this, imageView, true,false, false, true, new GuiderCoverClickListener() {
                @Override
                public void onClick(View view) {
                }
            });

            LocalUserSetting.saveBookViewGravityGuidShow(this);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode)
        {
            case RESULT_OPEN_TOC:
                OutlineItem[] outlineArray = core.getOutline();
                int pageIndex = 0;
                int chapterIndex = 0;

                if (pdfReader != null)
                {
                    pageIndex = pdfReader.getDisplayedViewIndex();
                }

                ArrayList<com.jingdong.app.reader.plugin.pdf.outline.OutlineItem> outlineList = new ArrayList<>();

                if (outlineArray != null)
                {
                    for (int i = 0; i < outlineArray.length; i++)
                    {
                        outlineList.add(
                                new com.jingdong.app.reader.plugin.pdf.outline.OutlineItem (
                                        outlineArray[i].level,
                                        outlineArray[i].title,
                                        outlineArray[i].page
                                )
                        );

                        if (pageIndex >= outlineArray[i].page)
                        {
                            chapterIndex = i;
                        }
                    }
                }

                String bookname = "";
                String author = "";

                if (eBook != null)
                {
                    bookname = eBook.title;
                    author = eBook.authorName;
                }
                else if (document != null)
                {
                    bookname = document.title;
                }

                Intent intent = new Intent(this, CatalogActivity.class);
                intent.putExtra(CatalogActivity.TOCLabelListKey, outlineList);
                intent.putExtra(CatalogActivity.BookNameKey, bookname);
                intent.putExtra(CatalogActivity.AuthorNameKey, author);
                intent.putExtra(CatalogActivity.ChapterIndexKey, chapterIndex);
                intent.putExtra(CatalogActivity.PageCalculatorFinish, true);
                intent.putExtra(CatalogActivity.EbookIdKey, eBook == null ? 0 : eBook.bookId);
                intent.putExtra(CatalogActivity.DocumentIdKey, document == null ? 0 : document.documentId);
                intent.putExtra(CatalogActivity.DOCUMENTSIGN, document == null ? "" : document.opfMD5);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, TOCRequest);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;

            case RESULT_PURCHASE_FULL_BOOK:
                purchaseFullBook();
                break;

            case RESULT_SEARCH_BOOK:
                break;

            case RESULT_TOGGLE_BOOK_MARK:
                this.toggleBookMark();
                break;

            case CatalogActivity.RESULT_CHANGE_PAGE:
                int pageNumber = 0;

                if (data != null)
                {
                    pageNumber = data.getIntExtra(CatalogActivity.PAGE_INDEX, 0);
                }

                if (pdfReader != null)
                {
                    mLastPostion = pdfReader.getDisplayedViewIndex();
                    pdfReader.setDisplayedViewIndex(pageNumber);
                }
                break;

            case RESULT_VIEW_BACK:
            	exit();
                break;
        }

        
        switch (requestCode)
        {
            case TOCRequest:
                if (data == null)
                {
                    break;
                }

                String selectedPage = data.getStringExtra(CatalogActivity.TOCSelectedPageKey);

                if (!TextUtils.isEmpty(selectedPage))
                {
                    int page = Integer.valueOf(selectedPage);

                    if (pdfReader != null)
                    {
                        mLastPostion = pdfReader.getDisplayedViewIndex();
                        pdfReader.setDisplayedViewIndex(page);
                    }
                }
                break;
            case SendBookFirstPageActivity.SENDBOOKFIRSTPAGE://从赠言扉页点击返回键直接关闭阅读页面
            	if(resultCode ==SendBookFirstPageActivity.SENDBOOKFIRSTPAGE){
            		if (!isFinishing()) {
            			exit();
        			}
            	}
            	break;	
            default:
                break;
        }
    }

    @Override
    public void purchaseFullBook()
    {
        if (eBook == null)
        {
            return;
        }

//      Intent intent = new Intent(this, BookInfoNewUIActivity.class);
//      intent.putExtra(BookInfoNewUIActivity.BookIdKey, eBook.bookId);
//      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//      startActivity(intent);
//      finish();
        
        if (LoginUser.isLogin())
        {
            SimplifiedDetail detail = new SimplifiedDetail();
            detail.bookId= eBook.bookId;

            BookCartManager.getInstance().addToShoppingCart(PDFBookViewActivity.this, detail, new AddToCartListener() {
                @Override
                public void onAddSuccess()
                {
                    Intent itIntent = new Intent(PDFBookViewActivity.this, BookCartActivity.class);
                    startActivity(itIntent);
                }

                @Override
                public void onAddFail()
                {
                    Toast.makeText(PDFBookViewActivity.this, "购买失败，请检查网络状况是否正常！", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            Intent login = new Intent(PDFBookViewActivity.this, LoginActivity.class);
            startActivity(login);
        }
    }
    
    @Override
    public void finishRating(float rating)
    {
        Intent intent = new Intent(this, BookCommentNewuiActivity.class);
        intent.putExtra(BookCommentNewuiActivity.RatingValueKey, rating);

        long bookId = 0;
        String title = "";

        if (eBook != null)
        {
            title = eBook.title;
            bookId = eBook.bookId;
        }

        if (document != null && !TextUtils.isEmpty(document.title))
        {
            title = document.title;
        }

        if (docBind != null && !TextUtils.isEmpty(docBind.serverTitle))
        {
            title = docBind.serverTitle;
            bookId = docBind.bookId;
        }

        if (bookId == 0)
        {
            return;
        }

        intent.putExtra(BookCommentNewuiActivity.TitleKey, title);
        intent.putExtra(BookCommentNewuiActivity.BookIdKey, bookId);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (isUseVolumePage)
        {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
            {
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
            {
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_MENU)
        {
            menuViewToggle();
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isUseVolumePage) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (pdfReader != null)
                    pdfReader.moveToNext();

                ShowRelatedPage();

                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            	if (pdfReader != null)
            		pdfReader.moveToPrevious();

                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
            if (pdfReader != null)
                pdfReader.moveToNext();

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PAGE_UP) {
            if (pdfReader != null)
                pdfReader.moveToPrevious();

            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	exit();
        }

        return super.onKeyDown(keyCode, event);
    }
   
    private String getChapterTitle(int pageIndex)
    {
    	String chapterName = "";
    	if(null == core) {
    		return chapterName;
    	}
    	
        OutlineItem[] outlineArray = core.getOutline();
        if (outlineArray != null)
        {
            for (int i = 0; i < outlineArray.length; i++)
            {
            	OutlineItem item = outlineArray[i];
            	if(null != item) {
            		if (pageIndex >= item.page)
                    {
                        chapterName = item.title;
                    } 
                    else
                    {
                        break;
                    }
            	}
            }
        }

        return chapterName;
    }

    void updateProgress()
    {
        if (pdfReader == null)
        {
            return;
        }

        progress.chapterItemRef = "";
        progress.chapterId = 0;
        progress.paraIndex = 0;
        progress.offsetInPara = 0;
        progress.updateTime = System.currentTimeMillis()/1000;
        progress.pdfZoom = pdfReader.getScaleX(); //pdfReader.getScale();
        progress.pdfXOffsetPercent = 0; //pdfReader.getDisplayedViewOffsetXPercent(displayIndex);
        progress.pdfYOffsetPercent = 0; //pdfReader.getDisplayedViewOffsetYPercent(displayIndex);
        progress.pdfPage = pdfReader.getDisplayedViewIndex();
        progress.chapterTitle = getChapterTitle(progress.pdfPage);
        progress.percent = 0;

        if (core != null)
        {
            int pageNumber = progress.pdfPage;
            int totalNumber = core.countPages();

            if (pageNumber != -1 && totalNumber > 0)
            {
                progress.percent = (pageNumber + 1) / (float) totalNumber;
            }

            if (pdfReader.getDisplayedViewIndex() == totalNumber - 1)
            {
                if (BOOK_PDF_TURNING_MODE == BOOK_PDF_TURNING_MODE_V)
                {
                    progress.pdfYOffsetPercent = 0;
                }
                else if (BOOK_PDF_TURNING_MODE == BOOK_PDF_TURNING_MODE_H)
                {
                    progress.pdfXOffsetPercent = 0;
                }
            }
        }

		if (eBook != null) {
			progress.pdfXOffsetPercent = 0;
		}

		if (eBook != null)
        {
            MZBookDatabase.instance.insertOrUpdateEbookReadProgress(userId, eBook.bookId, progress, false);
        }
        else if (document != null)
        {
            MZBookDatabase.instance.insertOrUpdateDocReadProgress(userId, document.documentId, progress, false);
        }
    }

    @Override
    protected void onPause()
    {
        updateProgress();
        endProgress = progress.clone();

        if (!TextUtils.isEmpty(userId))
        {
            uploadReadProgressAndBookMark();
            uploadReadingData();
        }

        StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yuedu_pdf));

        super.onPause();
    }

    @Override
    protected void onStop()
    {
        updateProgress();
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        updateProgress();
        startProgress = progress.clone();
        startProgress.updateTime = System.currentTimeMillis()/1000;
        isUseVolumePage = LocalUserSetting.useVolumePage(this);
        StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yuedu_pdf));

        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        updateProgress();
        isExitBookPage = true;

        if (core != null)
            core.onDestroy();

        core = null;
        PDFDeviceInfo.resetInfo();

        unregisterReceiver();

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        if (pdfReader == null)
        {
            return;
        }

        int lockScreenOrientation = LocalUserSetting.lockScreenOrientation(this);

        if (lockScreenOrientation == LocalUserSetting.SCREEN_DONT_LOCK)
        {
            if (mCurConfig.orientation != newConfig.orientation)
            {
                mCurConfig.orientation = newConfig.orientation;

//              if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
//                {
//              }
//
//              if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
//                {
//              }
            }
        }
    }
    
    private void setupScreenOrientation() {
    	
        int lockScreenOrientation = LocalUserSetting.lockScreenOrientation(this);

        if (lockScreenOrientation == LocalUserSetting.SCREEN_LANDSCAPE ||
                lockScreenOrientation == LocalUserSetting.SCREEN_PORTRAIT){
            lockScreenOrientation(lockScreenOrientation);
        }
        else{
            unlockScreenOrientation();
        }
    }

    private void lockScreenOrientation(int orientation)
    {
        if (!mScreenOrientationLocked)
        {
            switch (orientation)
            {
                case LocalUserSetting.SCREEN_PORTRAIT:
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO)
                    {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    else
                    {
                        int rotation = LocalUserSetting.getDisplayRotation(this);

                        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_180)
                        {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                        }
                        else
                        {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }
                    }
                break;

                case LocalUserSetting.SCREEN_LANDSCAPE:
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO)
                    {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                    else
                    {
                        int rotation = LocalUserSetting.getDisplayRotation(this);

                        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                        {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        }
                        else
                        {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        }
                    }
                    break;
            }

            mScreenOrientationLocked = true;
        }
    }

    private void unlockScreenOrientation()
    {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        mScreenOrientationLocked = false;
    }

    private void uploadReadingData()
    {
        if (startProgress.updateTime <= 0
                || endProgress.updateTime <= 0
                || startProgress.updateTime >= endProgress.updateTime)
        {
            return;
        }

        ReadingData data = new ReadingData();

        if (eBook != null)
        {
            data.setEbook_id(eBook.bookId);
        }
        else if (document != null)
        {
            data.setDocument_id(document.documentId);
        }
        else
        {
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

    private void requestUploadReadingData()
    {
        if (eBook == null && document == null)
        {
            return;
        }

        if (!LoginUser.isLogin())
        {
            return;
        }

        if (!NetWorkUtils.isNetworkConnected(this)) {
            return;
        }

        long bookId = 0;
        float percent = 0;

        if (eBook != null)
        {
            bookId = eBook.bookId;

            if (core != null)
            {
                int pageNumber = progress.pdfPage;
                int totalNumber = core.countPages();

                if (pageNumber != -1 && totalNumber > 0)
                {
                    progress.percent = (pageNumber + 1) / (float) totalNumber;
                }
            }
        }
        else
        {
            if (docBind != null && docBind.bookId != 0)
            {
                bookId = docBind.bookId;

                if (core != null)
                {
                    int pageNumber = progress.pdfPage;
                    int totalNumber = core.countPages();

                    if (pageNumber != -1 && totalNumber > 0)
                    {
                        progress.percent = (pageNumber + 1) / (float) totalNumber;
                    }
                }
            }
        }
        
		final long book_id = bookId == 0 ? documentId : bookId;
		//TODO  阅读获得积分需要的时长
		IntegrationAPI.readGetTime(this, new ReadGetTimeListener() {
			@Override
			public void onGetTimeSuccess(final int readTime) {
				long readTimeMills = endProgress.updateTime - startProgress.updateTime;
				if (readTimeMills >= readTime * 60) {
					// 请求服务器获取阅读积分
					IntegrationAPI.readTimeGetScore(PDFBookViewActivity.this, book_id, readTime,
							new GrandScoreListener() {

						@Override
						public void onGrandSuccess(SignScore score) {
							String scoreInfo = "恭喜你今日阅读超过" + readTime + "分钟，获得" + score.getGetScore() + "积分";
							SpannableString span = new SpannableString(scoreInfo);
							int start1 = 9 + String.valueOf(readTime).length() + 5;
							int end1 = start1 + String.valueOf(readTime).length();
							span.setSpan(new ForegroundColorSpan(Color.RED), start1, end1,
									Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
							span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start1, end1,
									Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
							CustomToast.showToast(PDFBookViewActivity.this, scoreInfo);
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

        List<ReadingData> list = MZBookDatabase.instance.getAllReadingData();
        RequestParams request = RequestParamsPool.getUploadBatchReadingData(list, bookId, percent);

        WebRequestHelper.post(URLText.uploadReadingDataUrl, request, true,
                new MyAsyncHttpResponseHandler(this) {

                    @Override
                    public void onResponse(int statusCode, Header[] headers,
                            byte[] responseBody) {
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
                    public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                            Throwable arg3) {

                    }
                });
    }

    void menuViewToggle()
    {
        int pageIndex = pdfReader.getDisplayedViewIndex();

        Intent intent = new Intent(PDFBookViewActivity.this, PDFReadOverlayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(PDFReadOverlayActivity.CurrentPageIndexKey, pageIndex);
        intent.putExtra(PDFReadOverlayActivity.PageCountKey, core.countPages());
        intent.putExtra(PDFReadOverlayActivity.BookMarkStateKey, isBookMarked());
        intent.putExtra(PDFReadOverlayActivity.ShowPurchaseButtonKey, isShowPurchaseButton);
        intent.putExtra(PDFReadOverlayActivity.LandMinZoomKey, 1.0f);
        intent.putExtra(PDFReadOverlayActivity.ZoomLevelKey, pdfReader.getScaleX());
        intent.putExtra(PDFReadOverlayActivity.IsBackProgressKey, mLastPostion >= 0);

        OutlineItem[] outlineArray = core.getOutline();

        if (outlineArray != null && outlineArray.length > 0)
        {
            ArrayList<com.jingdong.app.reader.plugin.pdf.outline.OutlineItem> outlineList = new ArrayList<>();

            for (OutlineItem item : outlineArray)
            {
                outlineList.add(
                        new com.jingdong.app.reader.plugin.pdf.outline.OutlineItem (
                            item.level,
                            item.title,
                            item.page
                ));
            }

            intent.putParcelableArrayListExtra(PDFReadOverlayActivity.ChapterPageIndexListKey, outlineList);
        }

        if (document != null)
        {
            intent.putExtra(PDFReadOverlayActivity.DocumentIdKey, document.documentId);
        }

        startActivityForResult(intent, ReadSettingRequest);
    }

    // 菜单设置ui
    OnItemListener onItemListener = new OnItemListener() {
        @Override
        public void OnItemSelected(AdapterView<?> parent, Item item,
                int position, long id) {
        }

        @Override
        public void OnItemLoading(AdapterView<?> parent, Item item,
                int position, long id) {

        }
    };

    private void requestDocumentServerIDTask()
    {
        if (isExitBookPage)
        {
            return;
        }

        if (!LoginUser.isLogin())
        {
            return;
        }

        if (docBind == null || docBind.serverId == 0)
        {
            if (!NetWorkUtils.isNetworkConnected(this))
            {
                return;
            }

            RequestParams request = RequestParamsPool.getDocBindParams(document.title, document.opfMD5);

            WebRequestHelper.post(URLText.synServerId, request, true,
                    new MyAsyncHttpResponseHandler(this) {

                        @Override
                        public void onResponse(int statusCode,
                                Header[] headers, byte[] responseBody) {
                            if (isExitBookPage) {
                                return;
                            }
                            try {
                                docBind = new DocBind();
                                JSONObject obj = new JSONObject(new String(
                                        responseBody));
                                docBind.documentId = document.documentId;
                                docBind.userId = userId;
                                docBind.serverId = obj
                                        .getLong("document_id");
                                docBind.bind = 0;
                                JSONObject book = obj.optJSONObject("book");
                                if (book != null) {
                                    docBind.bookId = book.optLong("id");
                                    docBind.serverTitle = book
                                            .optString("name");
                                    docBind.serverAuthor = book
                                            .optString("author_name");
                                    docBind.serverCover = book
                                            .optString("cover");
                                    // 服务器返回数据 说明服务器已经存在该书籍 所以直接绑定
                                    if (docBind.bookId != 0)
                                        docBind.bind = 1;
                                }
                                MZBookDatabase.instance
                                        .insertOrUpdateDocBind(docBind);
                                if (!serverProgressSynced) {
                                    syncReadProgressAndBookMark();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int arg0, Header[] arg1,
                                byte[] arg2, Throwable arg3) {

                        }
                    });
        }
    }

    protected void syncReadProgressAndBookMark()
    {
        if (eBook == null && document == null)
        {
            return;
        }

        if (isExitBookPage)
        {
            return;
        }

        if (!LoginUser.isLogin())
        {
            return;
        }

        if (!NetWorkUtils.isNetworkConnected(this))
        {
            return;
        }

        RequestParams request = null;

        if (eBook != null)
        {
            LocalBook book = LocalBook.getLocalBook(eBook.bookId,LoginUser.getpin());
            request = RequestParamsPool.getSyncEBookReadProgressBookMark(book);
        }
        else if (document != null)
        {
            if (docBind == null || docBind.serverId == 0)
            {
                return;
            }

            request = RequestParamsPool.getSyncDocumentReadProgressBookMark(document);
        }

        WebRequestHelper.post(URLText.JD_BOOK_READ_URL, request, true,
                new MyAsyncHttpResponseHandler(this) {

                    @Override
                    public void onResponse(int statusCode, Header[] headers,
                            byte[] responseBody) {
                        SyncServerReadProgress task = new SyncServerReadProgress();
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, responseBody);
                    }

                    @Override
                    public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                            Throwable arg3) {

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
                        } else if (data.getInt("dataType") == 1) {// 书签
                            int valid = data.getInt("valid");
                            if (valid == 0) {
                                MZBookDatabase.instance.deleteBookMark(userId,
                                        data.optInt("id"));
                            } else if (valid == 1) {
                                BookMark bookMark = BookMark.fromJSON(data);
                                bookMark.docid = documentId;
                                bookMark.isSync = 1;
                                markList.add(bookMark);
                            }
                        }
                    }
                    
                    MZBookDatabase.instance.insertOrUpdateBookMarksSyncTime(
                            userId, bookId, documentId, version);
                    for (BookMark mark : markList) {
                        MZBookDatabase.instance.addBookMark(mark);
                    }
                } else if ("3".equals(code)) {
                    // FIXME 请登录后重试  --liqiang
                } else if ("5".equals(code) || "80".equals(code)) {
                    ToastUtil.showToastInThread(obj.optString("message"), Toast.LENGTH_SHORT);
                } else {
                    // FIXME ServiceCode2String.code2StringToast
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
                if (serverProgress.inSameLocation(localProgress)
                        || serverProgress.inSameLocation(progress)) {
                    return false;
                } else {
                    return true;
                }
            }
            
        }

        @Override
        protected void onPostExecute(final ReadProgress serverProgress)
        {
            super.onPostExecute(serverProgress);

            if (isExitBookPage) {
                return;
            }
            if (serverProgress == null) {
                return;
            }
            loadBookMarks();
            serverProgressSynced = true;
            if (serverProgressAfterLocalProgress(serverProgress)) {
                String chapterName = "";
                if (TextUtils.isEmpty(serverProgress.chapterTitle)) {
                    chapterName = getChapterTitle(serverProgress.pdfPage);
                    if (chapterName == null) {
                        chapterName = "";
                    }
                    serverProgress.chapterTitle = chapterName;
                } else {
                    chapterName = serverProgress.chapterTitle;
                }

                if (isFirstOpenBook) {
                    progress = serverProgress;
                    progress.updateTime = System.currentTimeMillis() / 1000;
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(
                            PDFBookViewActivity.this,
                            AlertDialog.THEME_HOLO_LIGHT)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setTitle(
                                    getString(R.string.progress_sync_alert)
                                            + chapterName)
                            .setPositiveButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface d,
                                                int whichButton) {
                                            progress = serverProgress;
                                            progress.updateTime = System
                                                    .currentTimeMillis() / 1000;
                                            if (pdfReader != null) {
//                                              pdfReader.forceLoadProgress(progress.pdfPage, progress.pdfXOffsetPercent,
//                                                      progress.pdfYOffsetPercent, progress.pdfZoom);
                                                pdfReader.setDisplayedViewIndex(progress.pdfPage);
                                            }
                                        }
                                    })
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface d,
                                                int whichButton) {

                                        }
                                    }).setCancelable(false).create();

                    alertDialog.show();
                }
            }
        }
    }

    private void uploadReadProgressAndBookMark()
    {
        if (eBook == null && document == null) {
            return;
        }

        if (!LoginUser.isLogin()) {
            return;
        }

        if (progress.updateTime == 0) {
            return;
        }

        if (!NetWorkUtils.isNetworkConnected(this))
        {
            return;
        }

        RequestParams request = null;

        if (eBook != null)
        {
            LocalBook book = LocalBook.getLocalBook(eBook.bookId,LoginUser.getpin());
            request = RequestParamsPool.getUploadEBookReadProgressBookMark(book);
        }
        else if (document != null)
        {
            if (docBind == null || docBind.serverId == 0)
            {
                return;
            }

            request = RequestParamsPool.getUploadDocumentReadProgressBookMark(document);
        }
        else
        {
            return;
        }
        WebRequestHelper.post(URLText.JD_BOOK_READ_URL, request, true,
                new MyAsyncHttpResponseHandler(this) {

                    @Override
                    public void onResponse(int statusCode, Header[] headers,
                            byte[] responseBody) {
                        UploadServerReadProgress task = new UploadServerReadProgress();
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, responseBody);
                    }

                    @Override
                    public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                            Throwable arg3) {

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
                                MZBookDatabase.instance.deleteBookMark(userId,
                                        data.optInt("id"));
                            } else if (valid == 1) {
                                BookMark bookMark = BookMark.fromJSON(data);
                                bookMark.docid = documentId;
                                bookMark.isSync = 1;
                                markList.add(bookMark);
                            }
                        }
                    }
                    MZBookDatabase.instance.insertOrUpdateBookMarksSyncTime(
                            userId, bookId, documentId, version);
                    for (BookMark mark : markList) {
                        MZBookDatabase.instance.addBookMark(mark);
                    }
                    MZBookDatabase.instance.cleanBookMarks();
                } else if ("3".equals(code)) {
                    // FIXME 请登录后重试  --liqiang
                } else if ("5".equals(code) || "80".equals(code)) {
                    ToastUtil.showToastInThread(obj.optString("message"), Toast.LENGTH_SHORT);
                } else {
                    // FIXME ServiceCode2String.code2StringToast
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    
    /** 关闭当前页面 */
    private void exit() {
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
}
