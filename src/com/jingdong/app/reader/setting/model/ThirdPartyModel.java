package com.jingdong.app.reader.setting.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.me.model.UserDetail;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.oauth.SinaAuth;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;

public class ThirdPartyModel extends ObservableModel implements OnCheckedChangeListener {

	public class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle response) {
			Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(response);
			if (accessToken.isSessionValid()) {
				LocalUserSetting.saveSinaAccessToken(activity, accessToken);
				long expiresMillsec = accessToken.getExpiresTime();
				String url = getSinaBindUrl(accessToken.getToken(), accessToken.getUid(), String.valueOf(expiresMillsec / 1000));
				BindTask task = new BindTask(HttpMethod.GET);
				task.execute(url);
				tasks.add(task);
			} else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = response.getString("code");
                String message = activity.getString(R.string.weibosdk_toast_auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
		}
		
		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(activity, 
                    "Sina Weibo Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		@Override
		public void onCancel() {
			Toast.makeText(activity, 
                    R.string.weibosdk_toast_auth_canceled, Toast.LENGTH_LONG).show();
		}

	}

	private class BindTask extends AsyncTask<String, Void, String> {
		private HttpMethod method;
		private UserDetail userDetail;

		private BindTask(HttpMethod method) {
			this.method = method;
		}

		@Override
		protected String doInBackground(String... params) {
			switch (method) {
			case GET:
				String jsonString = WebRequest.getWebDataWithContext(activity, params[0]);
				UserDetail userDetail = new UserDetail();
				userDetail.parseJson(activity);
				this.userDetail = userDetail;
				return jsonString;
			case DELETE:
				return WebRequest.deleteWebDataWithContext(activity, params[0]);
			default:
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				boolean success = parsePostResult(result);
				if (success) {
					processSuccess();
				} else {
					processError(result);
				}
			}
		}

		private void processSuccess() {
			socialSwitch.setOnCheckedChangeListener(null);
			socialSwitch.setChecked(!socialSwitch.isChecked());
			socialSwitch.setOnCheckedChangeListener(ThirdPartyModel.this);
			switch (type) {
			case Sina:
				switch (method) {
				case GET:
					if (userDetail != null && userDetail.getSinalModel() != null)
						thirdPartyId = userDetail.getSinalModel().getId();
					break;
				case DELETE:
					LocalUserSetting.clearSina(activity);
					thirdPartyId = -1;
					break;
				}
				break;
			}
		}

		private void processError(String result) {
			String msg = null;
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
				msg = jsonObject.optString("errors");
			} catch (JSONException e) {
				MZLog.e("Bind", Log.getStackTraceString(e));
			}
			if (!TextUtils.isEmpty(msg)) {
				Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
			}
			LocalUserSetting.clearSina(activity);
		}
	}

	public static enum SocialType {
		Sina
	};

	private static enum HttpMethod {
		DELETE, GET
	};

	private final static String AGENT = "agent";
	private final static String ANDROID = "android";
	private final Activity activity;
	private final SocialType type;
	private final SinaAuth sinaAuth;
	private final CheckBox socialSwitch;
	private final List<AsyncTask<?, ?, ?>> tasks = new LinkedList<AsyncTask<?, ?, ?>>();
	private long thirdPartyId;

	public ThirdPartyModel(long socialId, Activity activity, SocialType type, CheckBox button) {
		this.thirdPartyId = socialId;
		this.activity = activity;
		this.type = type;
		this.socialSwitch = button;
		sinaAuth = new SinaAuth(activity, new AuthDialogListener());
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		buttonView.setChecked(!isChecked);
		switch (type) {
		case Sina:
			processSina(buttonView.isChecked());
			break;
		}
	}

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 32973 && sinaAuth != null) {
			sinaAuth.authCallBack(requestCode, resultCode, data);
			return true;
		} else if (requestCode == GlobalVarable.REQUEST_CODE_FOLLOWED_USER_BOOK && resultCode == Activity.RESULT_OK) {
			activity.finish();
			return true;
		} else {
			return false;
		}
	}

	public void stopTask() {
		for (AsyncTask<?, ?, ?> task : tasks)
			task.cancel(false);
	}

	private void processSina(boolean isChecked) {
		if (isChecked) {
			if (LocalUserSetting.isRegisterFromThirdParty(activity))
				Toast.makeText(activity, R.string.unableUnbind, Toast.LENGTH_SHORT).show();
			else {
				AlertDialog dialog = new AlertDialog.Builder(activity,
						AlertDialog.THEME_HOLO_LIGHT)
						.setIconAttribute(android.R.attr.alertDialogIcon)
						.setMessage(R.string.confirmUnbind)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										BindTask task = new BindTask(HttpMethod.DELETE);
										String url = getUnbindUrl();
										task.execute(url);
										tasks.add(task);
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										
									}
								}).setCancelable(false).create();

				dialog.show();
			}
		} else {
			Oauth2AccessToken accessToken = LocalUserSetting.getSinaAccessToken(activity);
			String tokenString = accessToken.getToken();
			String uid = accessToken.getUid();
			long expireTime = accessToken.getExpiresTime();
			if (!UiStaticMethod.isNullString(tokenString) && !UiStaticMethod.isNullString(uid) && expireTime != 0
					&& expireTime > System.currentTimeMillis()) {
				String url = getSinaBindUrl(tokenString, uid, String.valueOf(expireTime / 1000));
				BindTask task = new BindTask(HttpMethod.GET);
				task.execute(url);
				tasks.add(task);
			} else
				sinaAuth.auth();
		}
	}

	private String getUnbindUrl() {
		String baseUrl = String.format(Locale.US, URLText.unBind, thirdPartyId);
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(activity));
		paramMap.put(RecommendModel.SOURCE, type.toString().toLowerCase(Locale.US));
		String url = URLBuilder.addParameter(baseUrl, paramMap);
		return url;
	}

	private String getSinaBindUrl(String sinaToken, String uid, String expiresAt) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(activity));
		paramMap.put(RecommendModel.ACCESS_TOKEN, sinaToken);
		paramMap.put(RecommendModel.UID, uid);
		paramMap.put(RecommendModel.EXPIRES, expiresAt);
		paramMap.put(AGENT, ANDROID);
		String url = URLBuilder.addParameter(URLText.bindSina, paramMap);
		return url;
	}
}
