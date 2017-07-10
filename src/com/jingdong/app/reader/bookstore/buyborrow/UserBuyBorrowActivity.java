package com.jingdong.app.reader.bookstore.buyborrow;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.mzbook.sortview.model.BookShelfModel;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.SettingActivity;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadStateManager;
import com.jingdong.app.reader.client.DownloadThreadQueue.RefreshAble;
import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.BodyEncodeEntity;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OlineDesUtils;
import com.jingdong.app.reader.security.RequestSecurityKeyTask;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListCommentsActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.DesUtil;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.util.RsaEncoder;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.view.SharePopupWindow;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.SharePopupWindow.onPopupWindowItemClickListener;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.tendcloud.tenddata.TCAgent;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 用户借阅
 * @author xuhongwei1
 * @date 2015年12月3日 下午2:11:38 
 */
public class UserBuyBorrowActivity extends Activity implements TopBarViewListener, 
	OnClickListener, RefreshAble, onPopupWindowItemClickListener {
	/** 标题栏 */
	private TopBarView topBarView = null;
	/** 借阅提示图标 */
	private ImageView mTipIcon = null;
	/** 借阅提示语 */
	private TextView mTipText = null;
	/** 借阅按钮 */
	private Button mBorrowBtn = null;
	/** 试读按钮布局 */
	private RelativeLayout bottomLayout = null;
	/** 试读按钮 */
	private Button mTryReadBtn = null;
	/** 分享按钮 */
	private Button mShareBtn = null;
	/** 借阅规则 */
	private TextView mBorrowRule = null;
	/** 图书详情信息 */
	private JDBookInfo bookInfo = null;
	/** 当前借阅状态 */
	private BorrowStatus mCurBorrowStatus = BorrowStatus.NOBORROW;
	/** 借阅状态 */
	private enum BorrowStatus {
		/** 未借阅 */ 			NOBORROW, 
		/** 借阅时间过期 */		BORROWEXPIRE,
		/** 无用户可以借阅 */		NOUSERBORROW,
		/** 允许借阅 */			ALLOWBORROW,
		/** 借阅成功 */			BORROWFINISH,
		/** 借阅失败 */			BORROWFAIL
	}
	/** 允许借阅用户人数 */
	private int allowBorrowUserNum = 0;
	/** 借阅成功后扣除的积分 */
	private int score = 10;
	/** 借阅倒计时10秒 */
	private int borrowTime = 10;
	/** 请求借阅定时消息 **/
	private final int BORROWTIMER = 1001;
	/** 借阅请求中标识 */
	private boolean requestBorrowing = false;
	/** 请求借阅后返回code */
	private String requestBorrowCode = "";
	/** 试读下载中进度 */
	private ButtonAddProgressBar mTryReadProgressBtn = null;
	/** 借阅下载中进度 */
	private ButtonAddProgressBar mBorrowProgressBtn = null;
	/** 分辨率 */
	public static DisplayMetrics dm;
	/** 下载中 */
	private boolean downloading = false;
	/** 分享对话框 */
	private static SharePopupWindow sharePopupWindow = null;
	/** 分享标题 */
	private String shareTitle = "";
	/** 分享信息 */
	private String shareMsg = "";
	/** 借阅剩余天数 */
	private int days = 3;
	/** 试读下载中标识 */
	private boolean isTryReadDownloading = false;
	/** 借阅下载中标识 */
	private boolean isBorrowDownloading = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		setContentView(R.layout.activity_user_buyborrow);
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
		bookInfo = (JDBookInfo)getIntent().getSerializableExtra("bookInfo");
		shareTitle = LocalUserSetting.getUserNickName() + "喊你来免费借书看";
		shareMsg = LocalUserSetting.getUserNickName() + "刚刚成功借阅了一本《"+
				bookInfo.detail.bookName +"》，这么好的功能我只告诉好盆友，来晚了就可能借光啦！";
		days = bookInfo.detail.canBuyBorrowDays;
		
		initView();
	}
	
	private void initView() {
		topBarView = (TopBarView)findViewById(R.id.topbar);
		topBarView.setLeftMenuVisiable(true, R.drawable.topbar_add_selected);
		topBarView.setTitle("试读·借阅");
		topBarView.setListener(this);
		topBarView.updateTopBarView();
		
		mTipIcon = (ImageView)findViewById(R.id.mTipIcon);
		mTipText = (TextView)findViewById(R.id.mTipText);
		mBorrowBtn = (Button)findViewById(R.id.mBorrowBtn);
		bottomLayout = (RelativeLayout)findViewById(R.id.bottomLayout);
		mTryReadBtn = (Button)findViewById(R.id.mTryReadBtn);
		mShareBtn = (Button)findViewById(R.id.mShareBtn);
		mBorrowRule = (TextView)findViewById(R.id.buyborrow_rule);
		
		mTryReadProgressBtn = (ButtonAddProgressBar)findViewById(R.id.mTryReadProgressBtn);
		mBorrowProgressBtn = (ButtonAddProgressBar)findViewById(R.id.mBorrowProgressBtn);

		mBorrowBtn.setOnClickListener(this);
		mTryReadBtn.setOnClickListener(this);
		mShareBtn.setOnClickListener(this);
		mBorrowRule.setOnClickListener(this);
		
		initLayout();
	}
	
	private void initLayout() {
		if (bookInfo.detail.isTryRead && !TextUtils.isEmpty(bookInfo.detail.tryDownLoadUrl)) {
			bottomLayout.setVisibility(View.VISIBLE);
			if(checkTryReadIsDownload()) {
				mTryReadBtn.setText("阅读");
				mTryReadBtn.setTextColor(Color.WHITE);
				mTryReadBtn.setBackgroundColor(getResources().getColor(R.color.red_main));
			}else {
				mTryReadBtn.setText("我要试读");
				mTryReadBtn.setTextColor(getResources().getColor(R.color.red_main));
				mTryReadBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_tryread_bg));
			}
		}else {
			bottomLayout.setVisibility(View.GONE);
		}
		
		if(bookInfo.detail.isAlreadyUserBuyBorrow) { //已借阅
			mShareBtn.setVisibility(View.VISIBLE);
			mBorrowRule.setVisibility(View.GONE);
			long userBorrowStartTime = TimeFormat.formatStringTime(bookInfo.detail.currentTime);
			long userBorrowEndTime = TimeFormat.formatStringTime(bookInfo.detail.userBuyBorrowEndTime);
			if (userBorrowEndTime > userBorrowStartTime) {
				mCurBorrowStatus = BorrowStatus.BORROWFINISH;
				int days = (int)(Math.ceil((double)(userBorrowEndTime - userBorrowStartTime) / (24 * 3600 * 1000)));
				String msgStr = "";
				String dayStr = "";
				if(days > 0) {
					dayStr = "" + days;
					msgStr = "您已成功借阅该书\n借阅时间还剩" + days + "天。" + "到期后将自动归还~";
				}else {
					int hour = (int)((userBorrowEndTime - userBorrowStartTime) / (3600 * 1000));
					dayStr = "" + hour;
					msgStr = "您已成功借阅该书\n借阅时间还剩" + hour + "小时。" + "到期后将自动归还~";	
				}
				Spannable sp = new SpannableString(msgStr);  
				sp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red_main)), 15, 15 + dayStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				mTipText.setText(sp);
				mTipIcon.setBackgroundResource(R.drawable.icon_userborrow_normal);
				mBorrowBtn.setTextColor(Color.WHITE);
				mBorrowBtn.setBackgroundColor(getResources().getColor(R.color.red_main));
				if(checkBorrowIsDownload()) {
					mBorrowBtn.setText("阅读");
				}else {
					mBorrowBtn.setText("下载阅读");
				}
			}else { //借阅时间过期
				mCurBorrowStatus = BorrowStatus.BORROWEXPIRE;
				mBorrowBtn.setBackgroundColor(Color.rgb(0xbb, 0xbb, 0xbb));
				mTipIcon.setBackgroundResource(R.drawable.icon_userr_borrow_finish);
				mTipText.setText("您已经借阅过此书了，每本书只能借阅一次哦！");
				mShareBtn.setVisibility(View.GONE);
				mBorrowRule.setVisibility(View.VISIBLE);
			}
		}else { //未借阅过
			mCurBorrowStatus = BorrowStatus.NOBORROW;
			mTipIcon.setBackgroundResource(R.drawable.icon_userborrow_normal);
			mTipText.setText("正在获取可借阅人数，请稍后...");
			mBorrowBtn.setVisibility(View.GONE);
			getLendUserCount("" + bookInfo.detail.bookId);
		}
		
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		lbm.registerReceiver(new BroadcastReceiver() {  
	        @Override  
	        public void onReceive(Context context, Intent intent) {  
	        	if(null == intent) {
	        		return;
	        	}
	        	String ebookid = intent.getStringExtra("ebookid");
	        	if(null == ebookid || (null != ebookid && !ebookid.equals(bookInfo.detail.bookId + ""))) {
	        		return;
	        	}
	        	if(mTryReadProgressBtn.getVisibility() == View.VISIBLE) {
	        		downloading = false;
					mTryReadProgressBtn.setVisibility(View.GONE);
					mTryReadBtn.setVisibility(View.VISIBLE);
					mTryReadBtn.setText("出错啦，点击重试");
					mTryReadBtn.setTextColor(getResources().getColor(R.color.red_main));
					mTryReadBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_tryread_bg));
	        	}
	        	
	        	if(mBorrowProgressBtn.getVisibility() == View.VISIBLE) {
	        		mCurBorrowStatus = BorrowStatus.ALLOWBORROW;
	        		downloading = false;
					mBorrowProgressBtn.setVisibility(View.GONE);
					mBorrowBtn.setVisibility(View.VISIBLE);
					mBorrowBtn.setText("出错啦，点击重试");
					mBorrowBtn.setTextColor(getResources().getColor(R.color.red_main));
					mBorrowBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_tryread_bg));
	        	}
	        }  
	    }, new IntentFilter("download_ebook_fail")); 
	}
	
	/** 
	 * 获取可借阅人数 和借阅扣除分数
	 * @author xuhongwei1
	 * @param String ebookId 图书ID
	 * @date 2015年12月1日 下午1:39:58 
	 */
	private void getLendUserCount(String ebookId) {
		WebRequestHelper.post(URLText.JD_USER_SEARCH_URL, RequestParamsPool.getLendUserCountParams(ebookId), 
				false, new MyAsyncHttpResponseHandler(this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				mTipText.setText("当前无可借阅的人，请稍后再试~");
				mBorrowBtn.setBackgroundColor(Color.rgb(0xbb, 0xbb, 0xbb));
				mBorrowBtn.setVisibility(View.VISIBLE);
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				JSONObject jsonObj;
				try {
					jsonObj = new JSONObject(new String(responseBody));
					if (jsonObj != null) {
						String code = jsonObj.optString("code");
						if("0".equals(code)) {
							allowBorrowUserNum = jsonObj.optInt("count");
							score = jsonObj.optInt("score");
							
							if(allowBorrowUserNum > 0) {
								mCurBorrowStatus = BorrowStatus.ALLOWBORROW;
								String countStr = "" + allowBorrowUserNum;
								String msgStr = "当前有"+ allowBorrowUserNum +"位书友愿意借出本书\n借阅成功将扣"+score+"积分~";
								Spannable sp = new SpannableString(msgStr);  
								sp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red_main)), 3, 3+countStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
								mTipText.setText(sp);
								mTipIcon.setBackgroundResource(R.drawable.icon_userborrow_normal);
								mBorrowBtn.setVisibility(View.VISIBLE);
								mBorrowBtn.setText("我要借阅");
								mBorrowBtn.setTextColor(Color.WHITE);
								mBorrowBtn.setBackgroundColor(getResources().getColor(R.color.red_main));
							}else {
								mCurBorrowStatus = BorrowStatus.NOUSERBORROW;
								mBorrowBtn.setVisibility(View.VISIBLE);
								mBorrowBtn.setBackgroundColor(Color.rgb(0xbb, 0xbb, 0xbb));
								mTipIcon.setBackgroundResource(R.drawable.icon_userborrow_no_book);
								mTipText.setText("当前无可借阅的人，请稍后再试~");
							}
						}else {
							mBorrowBtn.setVisibility(View.VISIBLE);
							mCurBorrowStatus = BorrowStatus.NOUSERBORROW;
							mBorrowBtn.setBackgroundColor(Color.rgb(0xbb, 0xbb, 0xbb));
							mTipIcon.setBackgroundResource(R.drawable.icon_userborrow_no_book);
							mTipText.setText("当前无可借阅的人，请稍后再试~");
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mBorrowBtn:
			if (!LoginUser.isLogin()) {
				Intent it = new Intent(UserBuyBorrowActivity.this, LoginActivity.class);
				startActivity(it);
				return;
			}
			
			if(requestBorrowing || downloading) {
				return;
			}
			
			clickBorrowBtn();
			break;
			
		case R.id.mTryReadBtn:
			if(requestBorrowing || downloading) {
				return;
			}
			
			clickTryReadBtn();	
			break;
		case R.id.mShareBtn:
			sharePopupWindow = new SharePopupWindow(UserBuyBorrowActivity.this);
			sharePopupWindow.setListener(this);
			sharePopupWindow.show(topBarView.getSubmenurightOneImage(), true);
			break;
		case R.id.buyborrow_rule:
			String url = "http://e.m.jd.com/borrowRule.html";
			Intent intent = new Intent(UserBuyBorrowActivity.this,WebViewActivity.class);
			intent.putExtra(WebViewActivity.UrlKey, url);
			intent.putExtra(WebViewActivity.TopbarKey, true);
			intent.putExtra(WebViewActivity.BrowserKey, false);
			intent.putExtra(WebViewActivity.TitleKey, "借阅规则");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onLeftMenuClick() {
		finish();
	}

	@Override
	public void onRightMenuOneClick() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onRightMenuTwoClick() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCenterMenuItemClick(int position) {
		// TODO Auto-generated method stub
	}
	
	/**
	* @Description: 点击我要借阅按钮
	* @author xuhongwei1
	* @date 2015年12月9日 下午1:25:07 
	* @throws 
	*/ 
	private void clickBorrowBtn() {
		switch (mCurBorrowStatus) {
		case ALLOWBORROW:
			DialogManager.showCommonDialog(UserBuyBorrowActivity.this, "提示", "借阅成功后将扣除"+ score +"积分", 
					"确定", "取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						toBorrow();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						
						break;
					default:
						break;
					}
					dialog.dismiss();
				}
			});
			break;
		case BORROWFINISH:
			downloadUserBorrowBook();
			break;
		default:
			break;
		}
	}
	
	private void showSettingSwitch() {
		DialogManager.showCommonDialog(UserBuyBorrowActivity.this, "提示", "借阅功能未开启\n现在去设置中开启？", 
				"去设置", "取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					Intent intent = new Intent(UserBuyBorrowActivity.this, SettingActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
	}
	
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case BORROWTIMER:
				requestBorrowing = true;
				mHandler.removeMessages(BORROWTIMER);
				mBorrowBtn.setTextColor(Color.WHITE);
				mBorrowBtn.setBackgroundColor(Color.rgb(0xbb, 0xbb, 0xbb));
				mBorrowBtn.setText("正在为您借阅 "+ borrowTime +"秒");
				borrowTime--;
				if("141".equals(requestBorrowCode)) { //用户可用积分小于活动需扣除积分
					requestBorrowing = false;
					mCurBorrowStatus = BorrowStatus.ALLOWBORROW;
					String countStr = "" + allowBorrowUserNum;
					String msgStr = "当前有"+ allowBorrowUserNum +"位书友愿意借出本书\n借阅成功将扣"+score+"积分~";
					Spannable sp = new SpannableString(msgStr);  
					sp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red_main)), 3, 3+countStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					mTipText.setText(sp);
					mTipIcon.setBackgroundResource(R.drawable.icon_userborrow_normal);
					mBorrowBtn.setVisibility(View.VISIBLE);
					mBorrowBtn.setText("我要借阅");
					mBorrowBtn.setTextColor(Color.WHITE);
					mBorrowBtn.setBackgroundColor(getResources().getColor(R.color.red_main));
					
					Toast.makeText(UserBuyBorrowActivity.this, "您的积分不足"+score+"分\n暂时不能借阅", Toast.LENGTH_SHORT).show();
					return;
				}else if("29".equals(requestBorrowCode)){ 
					requestBorrowing = false;
					mCurBorrowStatus = BorrowStatus.ALLOWBORROW;
					showSettingSwitch();
					mBorrowBtn.setText("我要借阅");
					mBorrowBtn.setTextColor(Color.WHITE);
					mBorrowBtn.setBackgroundColor(getResources().getColor(R.color.red_main));
					return;
				}else if("-2".equals(requestBorrowCode)){ 
					mCurBorrowStatus = BorrowStatus.ALLOWBORROW;
					mTipIcon.setBackgroundResource(R.drawable.icon_userborrow_no_book);
					mTipText.setText("网络不可用，请稍后再试~");
					mBorrowBtn.setText("我要借阅");
					mBorrowBtn.setTextColor(Color.WHITE);
					mBorrowBtn.setBackgroundColor(Color.rgb(0xbb, 0xbb, 0xbb));
					return;
				}else {
					if(borrowTime < 0) {
						requestBorrowing = false;
						if("0".equals(requestBorrowCode)) {//借阅成功
							mCurBorrowStatus = BorrowStatus.BORROWFINISH;
							mTipIcon.setBackgroundResource(R.drawable.icon_userr_borrow_finish);
							
							String msgStr = "借阅成功！\n您可以免费借阅" + days + "天。到期后将自动归还~";
							Spannable sp = new SpannableString(msgStr);  
							sp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red_main)), 13, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
							mTipText.setText(sp);
							
							mBorrowBtn.setText("下载阅读");
							mBorrowBtn.setTextColor(Color.WHITE);
							mBorrowBtn.setBackgroundColor(getResources().getColor(R.color.red_main));
							mShareBtn.setVisibility(View.VISIBLE);
							mBorrowRule.setVisibility(View.GONE);
						}else if("22".equals(requestBorrowCode)) { //用户列表为空
							mCurBorrowStatus = BorrowStatus.BORROWFAIL;
							mTipIcon.setBackgroundResource(R.drawable.icon_userborrow_fail);
							mTipText.setText("本书已被其他用户抢先借走");
							mBorrowBtn.setBackgroundColor(Color.rgb(0xbb, 0xbb, 0xbb));
						}else if("23".equals(requestBorrowCode)) {
							mCurBorrowStatus = BorrowStatus.BORROWFAIL;
							mTipIcon.setBackgroundResource(R.drawable.icon_userborrow_fail);
							mTipText.setText("您已经借过这本书了");
							mBorrowBtn.setBackgroundColor(Color.rgb(0xbb, 0xbb, 0xbb));
							mBorrowBtn.setText("我要借阅");
						}else if("26".equals(requestBorrowCode)){ //已被其他用户抢先借走
							mCurBorrowStatus = BorrowStatus.BORROWFAIL;
							mTipIcon.setBackgroundResource(R.drawable.icon_userborrow_fail);
							mTipText.setText("本书已被其他用户抢先借走");
							mBorrowBtn.setBackgroundColor(Color.rgb(0xbb, 0xbb, 0xbb));
						}else {
							mCurBorrowStatus = BorrowStatus.BORROWFAIL;
							mTipIcon.setBackgroundResource(R.drawable.icon_userborrow_no_book);
							mTipText.setText("网络不可用，请稍后再试~");
							mBorrowBtn.setText("我要借阅");
							mBorrowBtn.setTextColor(Color.WHITE);
							mBorrowBtn.setBackgroundColor(Color.rgb(0xbb, 0xbb, 0xbb));
						}
						return;
					}
				}

				mHandler.sendEmptyMessageDelayed(BORROWTIMER, 1000);
				break;
			default:
				break;
			}
		};
	};
	
	/**
	* @Description: 发起借阅请求
	* @author xuhongwei1
	* @date 2015年12月3日 下午5:37:16 
	* @throws 
	*/ 
	private void toBorrow() {
		if (!NetWorkUtils.isNetworkAvailable(UserBuyBorrowActivity.this)) {
			Toast.makeText(UserBuyBorrowActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_LONG).show();
			return;
		}
		
		borrowTime = 10;
		requestBorrowCode = "-1";
		mHandler.removeMessages(BORROWTIMER);
		mHandler.sendEmptyMessage(BORROWTIMER);
		
		RequestSecurityKeyTask task = new RequestSecurityKeyTask(
				new RequestSecurityKeyTask.OnGetSessionKeyListener() {

					@Override
					public void onGetSessionKeySucceed() {
						WebRequestHelper.get(URLText.JD_USER_SEARCH_URL, RequestParamsPool.getUserBorrowBookParams(bookInfo.detail.bookId + "", bookInfo.detail.bookName), 
								true, new MyAsyncHttpResponseHandler(UserBuyBorrowActivity.this) {
							@Override
							public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
								requestBorrowCode = "-2";
							}

							@Override
							public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
								try {
									JSONObject object = new JSONObject(new String(responseBody));
									requestBorrowCode = object.getString("code");
									if("0".equals(requestBorrowCode)) {
										String encryptResult = object.optString("encryptResult");
										BodyEncodeEntity encodeEntity = RsaEncoder.getEncodeEntity();
										String desEncryptResult = DesUtil.decrypt(encryptResult, encodeEntity.desSessionKey);
										JSONObject desJson = new JSONObject(new String(desEncryptResult));
										if(null != desJson) {
											String currentTime = desJson.optString("currentTime");
											bookInfo.detail.userBuyBorrowStartTime = currentTime;
											long userBorrowStartTime = TimeFormat.formatStringTime(bookInfo.detail.userBuyBorrowStartTime);
											if(days > 0) {
												userBorrowStartTime += days * 24 * 3600 * 1000;
												SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
												Date date=new Date(userBorrowStartTime);
												String userBuyBorrowEndTime = format.format(date);
												bookInfo.detail.userBuyBorrowEndTime = userBuyBorrowEndTime;
											}
											
											String lendPin = desJson.optString("lendPin");
											if(!TextUtils.isEmpty(lendPin)) {
												Follow(lendPin);
											}
										}
									}else if("8".equals(requestBorrowCode)){//sessionKey失效的情况
										RsaEncoder.setEncodeEntity(null);
										toBorrow(); 
										return;
									}
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						});
					}

					@Override
					public void onGetSessionKeyFailed() {
					}
				});
		task.excute();

	}
	
	/**
	* @Description: 关注某人
	* @author xuhongwei1
	* @date 2015年12月9日 上午9:31:06 
	* @throws 
	*/ 
	private void Follow(String pin) {
		WebRequestHelper.post(URLText.Follow_SomeOne_URL, RequestParamsPool.getFollowPinParams(pin),
				new MyAsyncHttpResponseHandler(this) {
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}
					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
					}
		});
	}
	
	/**
	* @Description: 检查试读图书是否已经下载
	* @author xuhongwei1
	* @date 2015年12月7日 下午2:38:02 
	* @throws 
	*/ 
	private boolean checkTryReadIsDownload() {
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
		
		return isDownloaded;
	}
	
	/**
	* @Description: 检查借阅图书是否已经下载
	* @author xuhongwei1
	* @date 2015年12月9日 下午1:26:07 
	* @throws 
	*/ 
	private boolean checkBorrowIsDownload() {
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
			if (!allLocalBooks.source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK) && !allLocalBooks.source.equals(LocalBook.SOURCE_BUYED_BOOK)) {
				isFailed = true;

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
		
		return isDownloaded;
	}
	
	/**
	* @Description: 点击试读按钮
	* @author xuhongwei1
	* @date 2015年12月7日 上午10:41:41 
	* @throws 
	*/ 
	private void clickTryReadBtn() {
		isTryReadDownloading = true;
		isBorrowDownloading = false;
		TalkingDataUtil.onBookDetailEvent(UserBuyBorrowActivity.this, "试读");
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
			downloading = true;
			OrderEntity orderEntity = OrderEntity.FromJDBooKInfo2OrderEntity(bookInfo.detail);
			DownloadTool.download((Activity) UserBuyBorrowActivity.this, orderEntity, null, false, LocalBook.SOURCE_TRYREAD_BOOK, 0, true, null, false);
		} else if (isDownloaded) {
			// 打开阅读
			OpenBookHelper.openEBook(UserBuyBorrowActivity.this, bookInfo.detail.bookId);
		} else if (isFailed) {
			if (allLocalBooks.source.equals(LocalBook.SOURCE_ONLINE_BOOK) || 
					allLocalBooks.source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK)) {
				final String pathStr = pathString;
				String msgStr = "您的书架上已有畅读本，是否下载试读本覆盖它？";
				if(allLocalBooks.source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK)) {
					msgStr = "您的书架上已有借阅本，是否下载试读本覆盖它？";
				}
				DialogManager.showCommonDialog(UserBuyBorrowActivity.this, "提示", msgStr, "确定", "取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case DialogInterface.BUTTON_POSITIVE:
									if (!TextUtils.isEmpty(pathStr)) {
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
									localBook.start(UserBuyBorrowActivity.this);
									
									mBorrowBtn.setText("下载阅读");
									mBorrowBtn.setTextColor(Color.WHITE);
									mBorrowBtn.setBackgroundColor(getResources().getColor(R.color.red_main));
									break;
								case DialogInterface.BUTTON_NEGATIVE:
									
									break;
								default:
									break;
								}
								dialog.dismiss();
							}
				});
				
			}else {
				if (!TextUtils.isEmpty(pathString)) {
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
				localBook.start(UserBuyBorrowActivity.this);
			}

		} else if (isPause) {
			LocalBook localBook = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());

			localBook.mod_time = System.currentTimeMillis();
			localBook.saveModTime();
			MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, localBook.source);
			localBook.start(UserBuyBorrowActivity.this);

		} else {
			if (allLocalBooks.state == LocalBook.STATE_LOADED)
				OpenBookHelper.openEBook(UserBuyBorrowActivity.this, bookInfo.detail.bookId);
			else if (DownloadService.inDownloadQueue(allLocalBooks)) {
				int progress = 0;
				if (allLocalBooks.size > 0)
					progress = (int) (100 * (allLocalBooks.progress / (allLocalBooks.size * 1.0)));
//				sendMessage(progress);
			}
		}
	}
	
	/**
	* @Description: 下载用户借阅书籍
	* @author xuhongwei1
	* @date 2015年12月4日 下午2:12:44 
	* @throws 
	*/ 
	public void downloadUserBorrowBook() {
		isTryReadDownloading = false;
		isBorrowDownloading = true;
		TalkingDataUtil.onBookDetailEvent(UserBuyBorrowActivity.this, "用户借阅");
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
			if (!allLocalBooks.source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK) && !allLocalBooks.source.equals(LocalBook.SOURCE_BUYED_BOOK)) {
				isFailed = true;

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
			if (bookInfo != null && bookInfo.detail != null) {
				startDownloadBorrow();
			} else {
				Toast.makeText(UserBuyBorrowActivity.this, "请求出错了!", Toast.LENGTH_LONG).show();
			}
		} else if (isDownloaded) {
			// 打开阅读
			OpenBookHelper.openEBook(UserBuyBorrowActivity.this, bookInfo.detail.bookId);
		} else if (isFailed) {

			if (allLocalBooks.source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {

				final String pathstr = pathString;
				DialogManager.showCommonDialog(UserBuyBorrowActivity.this, "提示", "您的书架上已有畅读本，是否下载借阅本覆盖它？", "确定", "取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case DialogInterface.BUTTON_POSITIVE:
									if (!TextUtils.isEmpty(pathstr)) {
										IOUtil.deleteFile(new File(pathstr));
										deledeBook();
									}
									startDownloadBorrow();
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
					deledeBook();
				}
				startDownloadBorrow();
			}

		} else if (isPause) {
			LocalBook localBook = LocalBook.getLocalBook(bookInfo.detail.bookId, LoginUser.getpin());
			localBook.mod_time = System.currentTimeMillis();
			localBook.saveModTime();
			MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, localBook.source);
			localBook.start(UserBuyBorrowActivity.this);
		} else {
			MZLog.d("wangguodong", "else");
		}
	}
	
	private void deledeBook() {
		String userId = LoginUser.getpin();
		List<BookShelfModel> models = MZBookDatabase.instance.listBookShelf(userId, 0);
		for (int i = 0; i < models.size(); i++) {
			BookShelfModel item = models.get(i);
			int bookid = item.getBookid();
			long serverid = item.getServerid();
			if(serverid == bookInfo.detail.bookId) {
				Long[] ebooks = new Long[] { serverid };
				Integer[] index = new Integer[] { bookid };
				MZBookDatabase.instance.deleteEbook(userId, index, ebooks);
				return;
			}
		}
	}
	
	private void startDownloadBorrow() {
		mTryReadBtn.setText("我要试读");
		mTryReadBtn.setTextColor(getResources().getColor(R.color.red_main));
		mTryReadBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_tryread_bg));
		
		downloading = true;
		String temp = bookInfo.detail.userBuyBorrowEndTime;
		String time = OlineDesUtils.encrypt(temp);
		bookInfo.detail.userBuyBorrowEndTime = time;
		OrderEntity orderEntity = OrderEntity.FromJDBooKInfo2OrderEntity(bookInfo.detail);
		bookInfo.detail.userBuyBorrowEndTime = temp;
		bookInfo.detail.borrowEndTime = time;
		orderEntity.isBorrowBuy = true;

		DownloadTool.download((Activity) UserBuyBorrowActivity.this, orderEntity, null, false, LocalBook.SOURCE_USER_BORROWED_BOOK, 0, true, null, false);
	}

	@Override
	public void refresh(DownloadedAble downloadAble) {
		final LocalBook localBook = (LocalBook) downloadAble;
		if (localBook == null || bookInfo == null) {
			return;
		}

		if (localBook.book_id != bookInfo.detail.bookId) {
			return;
		}

		final int progress = (int) (100 * (localBook.progress / (localBook.size * 1.0)));
		
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if ( isTryReadDownloading ) {
					if(progress == 100) {
						downloading = false;
						mTryReadProgressBtn.setVisibility(View.GONE);
						mTryReadBtn.setVisibility(View.VISIBLE);
						mTryReadBtn.setText("阅读");
						mTryReadBtn.setTextColor(Color.WHITE);
						mTryReadBtn.setBackgroundColor(getResources().getColor(R.color.red_main));
					}else {
						downloading = true;
						mTryReadBtn.setVisibility(View.GONE);
						mTryReadProgressBtn.setVisibility(View.VISIBLE);
						mTryReadProgressBtn.setProgress(progress);
					}
				} 
				
				if ( isBorrowDownloading ) {
					if(progress == 100) {
						downloading = false;
						mCurBorrowStatus = BorrowStatus.BORROWFINISH;
						mBorrowProgressBtn.setVisibility(View.GONE);
						mBorrowBtn.setVisibility(View.VISIBLE);
						mBorrowBtn.setText("阅读");
						mBorrowBtn.setTextColor(Color.WHITE);
						mBorrowBtn.setBackgroundColor(getResources().getColor(R.color.red_main));
						
						MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), bookInfo.detail.bookId, LocalBook.SOURCE_USER_BORROWED_BOOK);
					}else {
						downloading = true;
						mBorrowBtn.setVisibility(View.GONE);
						mBorrowProgressBtn.setVisibility(View.VISIBLE);
						mBorrowProgressBtn.setProgress(progress);
					}
				}
				
			}
		});
		
	}

	@Override
	public int getType() {
		return 0;
	}

	@Override
	public void refreshDownloadCache() {
	}

	@Override
	public void onPopupWindowWeixinClick() {
		Bitmap shareBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.share_icon);
		sharePopupWindow.shareToWeixin(this, shareTitle, shareMsg, shareBitmap, getShareUrl(), 0, 0, bookInfo.detail.bookId+"");
		post(LoginUser.getpin(), String.valueOf(bookInfo.detail.bookId));
	}
	
	private void post(String pin, String bookid) {
		WebRequestHelper.post(URLText.SHARE_URL, RequestParamsPool.getShareParams(pin, bookid, "Book"), 
				true, new MyAsyncHttpResponseHandler(UserBuyBorrowActivity.this) {
			@Override public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {}
			@Override public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {}
		});
	}

	@Override
	public void onPopupWindowSinaClick() {
		Bitmap shareBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.share_icon);
		sharePopupWindow.shareToWeibo(UserBuyBorrowActivity.this, shareMsg, "", shareBitmap, getShareUrl() + "     ", "", 0, bookInfo.detail.bookId+"");
		post(LoginUser.getpin(), String.valueOf(bookInfo.detail.bookId));
	}

	@Override
	public void onPopupWindowWeixinFriend() {
		Bitmap shareBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.share_icon);
		sharePopupWindow.shareToWeixin(UserBuyBorrowActivity.this, shareTitle, shareMsg, shareBitmap, getShareUrl(), 1, 0, bookInfo.detail.bookId+"");
		post(LoginUser.getpin(), String.valueOf(bookInfo.detail.bookId));
	}

	@Override
	public void onPopupWindowCancel() {
		sharePopupWindow.dismiss();
	}

	@Override
	public void onPopupWindowMore() {
		sharePopupWindow.More(UserBuyBorrowActivity.this, shareTitle, shareMsg, getShareUrl());
	}
	
	@Override
	public void onPopupWindowCommuity() {
		Intent itIntent = new Intent(UserBuyBorrowActivity.this, TimelineBookListCommentsActivity.class);
		itIntent.putExtra("type", "share_to_comunity");
		itIntent.putExtra("book_id", bookInfo.detail.bookId+"");
		itIntent.putExtra("book_name", bookInfo.detail.bookName);
		itIntent.putExtra("book_author", bookInfo.detail.author);
		itIntent.putExtra("book_cover", bookInfo.detail.logo);
		
		startActivity(itIntent);
	}
	
	private String getShareUrl() {
		String share_url = "";
		try {
			String nickname = new String(LocalUserSetting.getUserNickName().getBytes(), "utf-8");
			String ebookName = new String(bookInfo.detail.bookName.getBytes(), "utf-8");
			nickname = URLEncoder.encode(nickname, "utf-8");
			ebookName = URLEncoder.encode(ebookName, "utf-8");
			share_url = "http://e.m.jd.com/share.html?nickName=" + nickname +"&ebookName=" + ebookName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return share_url;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		TCAgent.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_userborrow));
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_userborrow));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		TCAgent.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_userborrow));
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_userborrow));
		
	}

}
