package com.jingdong.app.reader.activity;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.io.StoragePath;
import com.jingdong.app.reader.me.activity.EditInfoActivity;
import com.jingdong.app.reader.message.activity.ChatActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.setting.activity.RecommendUsersActivity;
import com.jingdong.app.reader.setting.activity.SupplementAcitivity;
import com.jingdong.app.reader.setting.activity.ThirdPartyBindActivity;
import com.jingdong.app.reader.ui.ActionBarHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;

public class SettingsActivity extends MZReadCommonActivity implements
		OnClickListener {

	public static final int SUPPLEMENT_PROFILE = 1023;
	private View supplement;
	public static final String CHAT_ACTIONBAR_TITLE = "user_feedback";
	private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		registerClickListener();
		initHardwareRender();
		initVolumePage();
		initNotification();
		initAppVersion();
		ActionBarHelper.customActionBarBack(this);
	}

	private void initHardwareRender() {
		CheckBox hardware = (CheckBox) findViewById(R.id.hardwareRenderSwitch);
		boolean enable = LocalUserSetting.useSoftRender(this);
		hardware.setChecked(enable);
		hardware.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				LocalUserSetting.saveSoftRender(SettingsActivity.this,
						isChecked);
			}

		});

		findViewById(R.id.hardware_render_switch_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View view) {
						CheckBox hardware = (CheckBox) view
								.findViewById(R.id.hardwareRenderSwitch);
						boolean isChecked = !hardware.isChecked();
						hardware.setChecked(isChecked);
					}
				});
	}

	private void initVolumePage() {
		CheckBox volume = (CheckBox) findViewById(R.id.volume_page);
		boolean enable = LocalUserSetting.useVolumePage(this);
		volume.setChecked(enable);
		volume.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				LocalUserSetting.saveVolumePage(SettingsActivity.this,
						isChecked);
			}

		});

		findViewById(R.id.volume_page_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View view) {
						CheckBox hardware = (CheckBox) view
								.findViewById(R.id.volume_page);
						boolean isChecked = !hardware.isChecked();
						hardware.setChecked(isChecked);
					}
				});
	}

	private void initNotification() {
		CheckBox volume = (CheckBox) findViewById(R.id.notification_switch);
		boolean enable = LocalUserSetting.isNotificationSwitchOpen(this);
		volume.setChecked(enable);
		volume.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				LocalUserSetting.saveNotificationSwitch(SettingsActivity.this,
						isChecked);
			}

		});

		findViewById(R.id.notification_switch_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View view) {
						CheckBox hardware = (CheckBox) view
								.findViewById(R.id.notification_switch);
						boolean isChecked = !hardware.isChecked();
						hardware.setChecked(isChecked);
					}
				});
	}

	private void registerClickListener() {
		findViewById(R.id.settingsLogout).setOnClickListener(this);
		findViewById(R.id.find_friends).setOnClickListener(this);
		findViewById(R.id.user_feedback).setOnClickListener(this);
		findViewById(R.id.clear_cache).setOnClickListener(this);
		findViewById(R.id.thirdBind).setOnClickListener(this);
		findViewById(R.id.userModify).setOnClickListener(this);
		findViewById(R.id.recommendApp).setOnClickListener(this);
		findViewById(R.id.upgrade).setOnClickListener(this);
		if (MZBookApplication.TestBuild) {
			findViewById(R.id.about_area).setVisibility(View.VISIBLE);
			findViewById(R.id.aboutPhone).setOnClickListener(this);
		} else {
			findViewById(R.id.about_area).setVisibility(View.GONE);
		}
		int versionCode = -1;
		try {
			versionCode = getPackageManager().getPackageInfo(getPackageName(),
					0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (versionCode >= 0
				&& LocalUserSetting.getCheckUpdateFlag(this) > versionCode) {
			findViewById(R.id.upgrade_indicator).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.upgrade_indicator).setVisibility(View.GONE);
		}
		supplement = findViewById(R.id.supplement);
		supplement.setOnClickListener(this);
		if (LocalUserSetting.isRegisterFromThirdParty(this))
			supplement.setVisibility(View.VISIBLE);
		else
			supplement.setVisibility(View.GONE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		// boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SUPPLEMENT_PROFILE:
			if (resultCode == RESULT_OK)
				supplement.setVisibility(View.GONE);
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}

	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.settingsLogout:
			logOut();
			break;
		case R.id.find_friends:
			intent = new Intent(this, RecommendUsersActivity.class);
			startActivity(intent);
			break;
		case R.id.user_feedback:
			intent = new Intent(SettingsActivity.this, ChatActivity.class);
			intent.putExtra(ChatActivity.USER_ID, 127l);
			intent.putExtra(CHAT_ACTIONBAR_TITLE,
					getString(R.string.user_feedback));
			startActivity(intent);
			break;
		case R.id.clear_cache:
			clearCache();
			break;
		case R.id.userModify:
			intent = new Intent(this, EditInfoActivity.class);
			startActivity(intent);
			break;
		case R.id.thirdBind:
			intent = new Intent(this, ThirdPartyBindActivity.class);
			startActivity(intent);
			break;
		case R.id.supplement:
			intent = new Intent(this, SupplementAcitivity.class);
			startActivityForResult(intent, SUPPLEMENT_PROFILE);
			break;
		case R.id.recommendApp:
			//ExchangeDataService service = new ExchangeDataService(
			//		String.valueOf(59667));
			///new ExchangeViewManager(this, service).addView(
			//		ExchangeConstants.type_list_curtain, null);
			break;
		case R.id.upgrade:
			// check upgrade
			CheckUpdateTask checkUpdate = new CheckUpdateTask(this, true, true);
			checkUpdate.execute();
			break;
		case R.id.aboutPhone:
			// for developer test
			intent = new Intent(this, AboutPhoneActivity.class);
			startActivity(intent);
			break;
		}

	}

	private void logOut() {
		/*String token = LocalUserSetting.getToken(SettingsActivity.this);
		LocalUserSetting.clearUserInfo(SettingsActivity.this);
		Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);*/
		WebRequestHelper.setCookies(null);
		LoginUser.logOut(true);
		MZBookApplication.exitApplication();
		
		//LogoutTask task = new LogoutTask();
		//task.execute(token);
	}

	private void clearCache() {
		Resources resources = getResources();
		String message = resources.getString(R.string.waiting);
		String title = resources.getString(R.string.clear_cache);
		ProgressDialog dialog = ProgressDialog.show(this, title, message, true,
				false);

		// 删除非splash缓存
		File directory = StoragePath.getImageDir(this);
		File[] files = directory.listFiles();
		for (File file : files) {
			if (!file.getName().startsWith("splash_"))
				file.delete();
		}
		// 删除1.4.1 document下载后未删除副本
		File cacheFile = getExternalCacheDir();
		File[] cacheFiles = cacheFile.listFiles();
		for (File file : cacheFiles) {
			file.delete();
		}

		dialog.dismiss();
		Toast.makeText(this, R.string.clear_cache_done, Toast.LENGTH_SHORT)
				.show();
	}

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(NotificationService.NOTIFICATION_ACTION);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				notificationReceiver, filter);

	}

	private void unregisterReceiver() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				notificationReceiver);
	}

	private void initAppVersion() {
		TextView textView = (TextView) findViewById(R.id.version_code);
		try {
			String versionName = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName, digitString = null;
			Pattern digitPattern = Pattern.compile("\\d+(\\.\\d+)*");
			Matcher matcher = digitPattern.matcher(versionName);
			while (matcher.find()) {
				digitString = matcher.group();
			}
			if (!TextUtils.isEmpty(digitString)) {
				textView.setText(getString(R.string.version_code, digitString));
				textView.setVisibility(View.VISIBLE);
			} else {
				textView.setVisibility(View.GONE);
			}
		} catch (NameNotFoundException e) {
			textView.setVisibility(View.GONE); 
		}
	}

	
	public void logOut(String params){
	    String authToken = params;
        String urlText = URLText.logoutUrl + authToken;
	    WebRequestHelper.delete(urlText, new MyAsyncHttpResponseHandler(SettingsActivity.this) {
            
            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                
            }
            
            @Override
            public void onResponse(int statusCode,
                    Header[] headers, byte[] responseBody) {
                
                String result=new String(responseBody);
                try {
                    JSONObject o = new JSONObject(result);
                    boolean successful = o.optBoolean("success");
                    if (!successful) {
                        MZLog.d("MeActivity", "logout failed!");
                        MZLog.d("MeActivity", result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
            }
        });
	}
	

}
