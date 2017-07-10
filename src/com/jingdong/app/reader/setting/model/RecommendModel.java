package com.jingdong.app.reader.setting.model;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.setting.activity.RecommendUsersActivity;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

public class RecommendModel extends ObservableModel {
	public static final int SINA = 1;
	public static final String SOURCE="source";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String UID = "uid";
	public static final String EXPIRES = "expires_at";
	private static final String SINA_ACCESS = "sina";
	private static final String M_SINA = "m_sina";
	private static final String WITHOUT_RECOMMEND = "without_recommend_users";
	private final Context context;

	public RecommendModel(Context context) {
		this.context = context;
	}

	/**
	 * 取得sina微博接口的ACCESS_TOKEN，UID和EXPIRES_TIME。考虑到兼容性，在老版本中没有存储UID，如果UID为空的，
	 * 直接返回null。
	 *
	 * @return 一个String数组，内容依次是ACCESS_TOKEN，UID，EXPIRES_TIME。
	 */
	public String[] getLocalSinaToken() {
		String[] token = null;
		Oauth2AccessToken accessToken = LocalUserSetting.getSinaAccessToken(context);
		String tokenString = accessToken.getToken();
		String uid = LocalUserSetting.getSinaUID(context);
		long expireTime = accessToken.getExpiresTime();
		if (!UiStaticMethod.isNullString(tokenString) && !UiStaticMethod.isNullString(uid) && expireTime != 0
				&& expireTime > System.currentTimeMillis()) {
			token = new String[] { tokenString, uid, String.valueOf(expireTime / 1000) };
		}
		return token;
	}

	public String getUserJson(String[] sina) {
		String url = getSinaUrl(sina);
		String result = WebRequest.getWebDataWithContext(context, url);
		try {
			JSONObject jsonObject = new JSONObject(result);

			
			if (!UiStaticMethod.isNullString(jsonObject.optString(TweetModel.ERROR)))
				
				{
				JSONObject object=new JSONObject(jsonObject.optString(TweetModel.ERROR));
				
				Intent intent=new Intent();
				intent.setAction("com.sina.auth.error");
				intent.putExtra("msg", object.optString("message"));
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
				result = null;
				}
		} catch (JSONException e) {
			MZLog.e("sina", Log.getStackTraceString(e));
			result = null;
		}
		sendMessage(result);
		return result;
	}

	private String getSinaUrl(String[] sina) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		paramMap.put(M_SINA, Boolean.toString(true));
		paramMap.put(ACCESS_TOKEN, sina[0]);
		paramMap.put(SOURCE, M_SINA);
		paramMap.put(UID, sina[1]);
		paramMap.put(EXPIRES, sina[2]);
		paramMap.put(WITHOUT_RECOMMEND, Boolean.toString(true));
		String url = URLBuilder.addParameter(URLText.thirdPartyPeople, paramMap);
		return url;
	}

	private void sendMessage(String result) {
		Message message = Message.obtain();
		message.what = SINA;
		if (result != null)
			message.arg1 = SUCCESS_INT;
		else
			message.arg1 = FAIL_INT;
		try {
			JSONObject origin=new JSONObject(result);
			result=origin.getJSONObject(SINA_ACCESS).getJSONArray(RecommendUsersActivity.arrayKey).toString();
		} catch (JSONException e) {
			MZLog.e("Sina", Log.getStackTraceString(e));
		}
		message.obj = result;
		setChanged();
		notifyObservers(message);
	}

}
