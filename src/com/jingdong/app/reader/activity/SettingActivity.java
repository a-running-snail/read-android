package com.jingdong.app.reader.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookstore.buyborrow.BuyBorrowStatus;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.eventbus.de.greenrobot.event.EventBus;
import com.jingdong.app.reader.eventbus.event.MessageEvent;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.activity.ContactUsActivity;
import com.jingdong.app.reader.me.activity.HelpActivity;
import com.jingdong.app.reader.me.activity.ModifyUserInfoActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.BookStoreCacheManager;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.SdCardUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.util.UpdateUtil;
import com.jingdong.app.reader.view.dialog.DialogManager;

public class SettingActivity extends BaseActivityWithTopBar {
	private RelativeLayout modify_info, clean_cookie_vessel;
	private boolean ispushEnabled = false;
	private ImageView switchLine;
	private ImageView switchOnDot;
	private ImageView switchOffDot;
	private ImageView mBorrowSwitchLine;
	private ImageView mBorrowSwitchOnDot;
	private ImageView mBorrowSwitchOffDot;
	private RelativeLayout switch_dotLayout;
	private RelativeLayout borrow_switch_dotLayout;
	private RelativeLayout contact_usLayout;
	private LinearLayout changeLayout;// 切换用户
	private RelativeLayout updateLayout;
	private RelativeLayout device_bind;
	private RelativeLayout helpLayout;
	private TextView mVersionTv;
	private TextView mCopyTv;
	private Context mContext;
	private String tag = "SettingActivity";
	private TextView book_save_mkdir;
	private RelativeLayout svaeDir;
	private TextView updatTv;
	private View borrowLine;
	private RelativeLayout borrowLayout; 
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		mContext = SettingActivity.this;
		setContentView(R.layout.setting);
	}

	@Override
	protected void onResume() {
		super.onResume();
		initView();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_modify_setting));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_modify_setting));
	}

	@SuppressLint("CommitTransaction")
	private void initView() {
		modify_info = (RelativeLayout) findViewById(R.id.modify_info);
		clean_cookie_vessel = (RelativeLayout) findViewById(R.id.clean_cookie_vessel);
		device_bind = (RelativeLayout) findViewById(R.id.user_device_bind);
		switchLine = (ImageView) findViewById(R.id.switch_line);
		switchOnDot = (ImageView) findViewById(R.id.switchOn_dot);
		switchOffDot = (ImageView) findViewById(R.id.switchOff_dot);
		
		borrowLine  = (View) findViewById(R.id.borrowLine);
		borrowLayout  = (RelativeLayout) findViewById(R.id.borrowLayout);
		mBorrowSwitchLine = (ImageView) findViewById(R.id.borrow_switch_line);
		mBorrowSwitchOffDot = (ImageView) findViewById(R.id.borrow_switchOff_dot);
		mBorrowSwitchOnDot = (ImageView) findViewById(R.id.borrow_switchOn_dot);
		
		switch_dotLayout = (RelativeLayout) findViewById(R.id.switch_dot_statue);
		borrow_switch_dotLayout = (RelativeLayout) findViewById(R.id.borrow_switch_dot_statue);
		contact_usLayout = (RelativeLayout) findViewById(R.id.contact_us);
		changeLayout = (LinearLayout) findViewById(R.id.change);
		updateLayout = (RelativeLayout) findViewById(R.id.update);
		updatTv=(TextView) findViewById(R.id.update_tv);
		helpLayout = (RelativeLayout) findViewById(R.id.help);
		mVersionTv = (TextView) findViewById(R.id.version_info);
		book_save_mkdir = (TextView) findViewById(R.id.book_save_mkdir);
		svaeDir=(RelativeLayout) findViewById(R.id.save_dir);
		mVersionTv.setText("京东阅读Android V" + getVersionName());
		mCopyTv = (TextView) findViewById(R.id.copy_info);
		ispushEnabled = LocalUserSetting.getPushPage(this);
		MZLog.d(tag, "ispushEnabled=" + ispushEnabled);
		setupSwitchImage();

		if (SdCardUtils.isSecondSDcardMounted()) {
			book_save_mkdir.setEnabled(true);
			MZLog.d("wangguodong", "存在外置SDcard");
			svaeDir.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startActivity(new Intent(SettingActivity.this,SelectSaveDirActivity.class));
				}
			});
			
		} else {
			MZLog.d("wangguodong", "不存在外置SDcard");
			book_save_mkdir.setEnabled(false);
		}

		clean_cookie_vessel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View arg0) {
				DialogManager.showCommonDialog(SettingActivity.this, "提示",
						"您确定清空缓存吗？", "确定", "取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int witch) {
								switch (witch) {
								case DialogInterface.BUTTON_POSITIVE:
									MZBookApplication app = (MZBookApplication) getApplication();
									if (app != null) {
										app.getBoostoreCache().clear();

									}
									BookStoreCacheManager.getInstance()
											.clearData();
									Toast.makeText(SettingActivity.this,
											"清除缓存成功", Toast.LENGTH_SHORT)
											.show();
									// 通过事件总线发送刷新请求
									EventBus.getDefault().post(
											new MessageEvent(2));
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
		});
		switch_dotLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ispushEnabled = !ispushEnabled;
				LocalUserSetting.savePushPage(SettingActivity.this,
						ispushEnabled);
				setupSwitchImage();
				try {
					if (!ispushEnabled) {

						if (JPushInterface.isPushStopped(mContext)) {
							JPushInterface.resumePush(mContext);
						}
					} else {
						JPushInterface.stopPush(mContext);
					}
				} catch (Exception e) {
					// TODO: handle exception
					LocalUserSetting.savePushPage(SettingActivity.this,
							ispushEnabled);
				}

			}
		});
		
		borrow_switch_dotLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(MZBookApplication.getInstance().getBuyBorrowStatus().lendStatus == 1) {
					updateBuyBorrowStatus("0");
				}else {
					updateBuyBorrowStatus("1");
				}
			}
		});

		modify_info.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// Talking-Data
				TalkingDataUtil.onPersonalCenterEvent(SettingActivity.this,
						"设置_修改个人资料");

				Intent intent = new Intent(SettingActivity.this,
						ModifyUserInfoActivity.class);
				startActivity(intent);
			}
		});

		device_bind.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Talking-Data
				TalkingDataUtil.onPersonalCenterEvent(SettingActivity.this,
						"设置_解绑设备");

				Intent intent = new Intent(SettingActivity.this,
						WebViewActivity.class);
				intent.putExtra(WebViewActivity.UrlKey,
						"http://e.m.jd.com/unbundling.html");
				intent.putExtra(WebViewActivity.TopbarKey, true);
				intent.putExtra(WebViewActivity.BrowserKey, false);
				intent.putExtra(WebViewActivity.TitleKey,
						getString(R.string.out_bind));
				startActivity(intent);
			}
		});

		contact_usLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Talking-Data
				TalkingDataUtil.onPersonalCenterEvent(SettingActivity.this,
						"设置_联系我们");

				Intent intent = new Intent(SettingActivity.this,
						ContactUsActivity.class);
				startActivity(intent);
			}
		});

		changeLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				TalkingDataUtil.onPersonalCenterEvent(SettingActivity.this,
						"设置_切换用户");
				MZBookApplication.alreadyShowRecomment = false;
				Intent intent = new Intent(SettingActivity.this,
						LoginActivity.class);
				intent.putExtra("isSwitchAccount", true);
				startActivity(intent);

				// 通过事件总线发送刷新请求
				EventBus.getDefault().post(new MessageEvent(2));
			}
		});

		updateLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Talking-Data
				TalkingDataUtil.onPersonalCenterEvent(SettingActivity.this,
						"设置_版本更新");

				UpdateUtil.checksofteWareUpdated(true, SettingActivity.this,
						null);
			}
		});

		helpLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Talking-Data
				TalkingDataUtil.onPersonalCenterEvent(SettingActivity.this,
						"设置_帮助");
				Intent intent = new Intent(SettingActivity.this,
						HelpActivity.class);
				startActivity(intent);
			}
		});

		book_save_mkdir.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});
		
		
		UpdateUtil.checkUpdate(true, SettingActivity.this,
				updatTv);
	}

	private void setupSwitchImage() {
		TypedArray a = this
				.obtainStyledAttributes(new int[] {
						R.attr.read_switch_on_line_img,
						R.attr.read_switch_off_line_img });
		if (ispushEnabled) {
			switchOnDot.setVisibility(View.GONE);
			switchOffDot.setVisibility(View.VISIBLE);
			switchLine.setImageResource(a.getResourceId(1,
					R.drawable.switchoff_line_standard));
		} else {
			switchOnDot.setVisibility(View.VISIBLE);
			switchOffDot.setVisibility(View.GONE);
			switchLine.setImageResource(a.getResourceId(0,
					R.drawable.switchon_line_standard));
		}
		
		if(MZBookApplication.getInstance().getBuyBorrowStatus().status) {
			borrowLine.setVisibility(View.VISIBLE);
			borrowLayout.setVisibility(View.VISIBLE);
			if(MZBookApplication.getInstance().getBuyBorrowStatus().lendStatus == 1) {
				mBorrowSwitchOnDot.setVisibility(View.VISIBLE);
				mBorrowSwitchOffDot.setVisibility(View.GONE);
				mBorrowSwitchLine.setImageResource(a.getResourceId(0, R.drawable.switchon_line_standard));
			}else {
				mBorrowSwitchOnDot.setVisibility(View.GONE);
				mBorrowSwitchOffDot.setVisibility(View.VISIBLE);
				mBorrowSwitchLine.setImageResource(a.getResourceId(1, R.drawable.switchoff_line_standard));
			}
		}else {
			borrowLine.setVisibility(View.GONE);
			borrowLayout.setVisibility(View.GONE);
		}
		
		MZLog.d(tag, "setupSwitchImage ispushEnabled=" + ispushEnabled);
	}
	
	private String getVersionName() {
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			String version = packInfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	* @Description: 更新借阅状态
	* @param String lendStatus 0关闭借书功能；1开通借书功能
	* @author xuhongwei1
	* @date 2015年12月1日 下午1:39:58 
	* @throws 
	*/ 
	private void updateBuyBorrowStatus(final String lendStatus) {
		if (!NetWorkUtils.isWifiConnected(this)) {
			Toast.makeText(this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
			return;
		}
		
		WebRequestHelper.post(URLText.JD_USER_SEARCH_URL, RequestParamsPool.getUpdateBorrowStatusParams(lendStatus), 
				false, new MyAsyncHttpResponseHandler(this) {

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
						if("0".equals(code) || "23".equals(code)) {
							BuyBorrowStatus b = MZBookApplication.getInstance().getBuyBorrowStatus();
							b.lendStatus = jsonObj.optInt("lendStatus");
							MZBookApplication.getInstance().setBuyBorrowStatus(b);
							
							setupSwitchImage();
						}else {
							Toast.makeText(SettingActivity.this, "借阅状态更新失败，请稍后重试！", Toast.LENGTH_SHORT).show();
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
}
