package com.jingdong.app.reader.util.share;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.oauth.AccessTokenKeeper;
import com.jingdong.app.reader.oauth.Constants;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.wbapi.WBShareEventCallback;
import com.jingdong.app.reader.wbapi.WBShareEventCallback.WBShareResultListener;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;

public class WBShareHelper implements WBShareResultListener {

	private static WBShareHelper mInstance;
	/** 微博微博分享接口实例 */
	private IWeiboShareAPI mWeiboShareAPI = null;
	private Activity mActivity;
	private ShareResultListener listener;

	private WBShareHelper() {
	}

	public static WBShareHelper getInstance() {
		if (mInstance == null) {
			synchronized (WBShareHelper.class) {
				if (mInstance == null) {
					mInstance = new WBShareHelper();
				}
			}
		}
		return mInstance;
	}

	/**
	 * 
	 * @Title: doShare
	 * @Description: 对外公开的分享到微博的方法
	 * @param @param activity
	 * @param @param title
	 * @param @param description
	 * @param @param imageUrl
	 * @param @param url : 分享图片传入URL
	 * @param @param defaluttext
	 * @return void
	 * @throws
	 */
	public void doShare(Activity activity, final String title, final String description, String imageUrl, final String url, final String defaluttext,ShareResultListener l) {
		this.mActivity = activity;
		this.listener = l;
//		if (!isWBInstalled()) {
//			Toast.makeText(activity, "您还未安装微博客户端", Toast.LENGTH_SHORT).show();
//			return;
//		}
		registerAPP();
		
		Matrix mtx = new Matrix();
		mtx.postRotate(1);
		if (!TextUtils.isEmpty(imageUrl)) {
			ParseBitMapTask bitMapTask = new ParseBitMapTask();
			bitMapTask.setListener(new AyncTaskListener() {
				
				@Override
				public void onFinish(Bitmap bitmap) {
					sendMessage(title, description, bitmap, url, defaluttext);
				}
			});
			bitMapTask.execute(imageUrl);
		}else{
			sendMessage(title, description, null, url, defaluttext);
		}
	}
	
	public void doShare(Activity activity, final String title, final String description, Bitmap bitmap, final String url, final String defaluttext,ShareResultListener l) {
		this.mActivity = activity;
		this.listener = l;
//		if (!isWBInstalled()) {
//			Toast.makeText(activity, "您还未安装微博客户端", Toast.LENGTH_SHORT).show();
//			return;
//		}
		
		registerAPP();
		
		Matrix mtx = new Matrix();
		mtx.postRotate(1);
		sendMessage(title, description, bitmap, url, defaluttext);
	}
	
	/**
	 * 
	 * @Title: isWBInstalled
	 * @Description: 判断手机是否安装了微博客户端
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	private boolean isWBInstalled(){
//		if (mWeiboShareAPI == null) {
//			mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(mActivity, Constants.APP_KEY);
//		}
//		Boolean flag = mWeiboShareAPI.registerApp();
		registerAPP();
		return mWeiboShareAPI.isWeiboAppInstalled();
	}
	
	/**
	 * 注册app
	 */
	private void registerAPP(){
		if (mWeiboShareAPI == null) {
			mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(mActivity, Constants.APP_KEY);
		}
		Boolean flag = mWeiboShareAPI.registerApp();
//		Toast.makeText(mActivity, "注册结果--"+flag, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * 
	 * @Title: doShareByBitmap
	 * @param @param activity
	 * @param @param title
	 * @param @param description
	 * @param @param bitmap：分享图片传入Bitmap
	 * @param @param url
	 * @param @param defaluttext
	 * @param @param l
	 * @return void
	 * @throws
	 */
	public void doShareByBitmap(Activity activity, String title, String description, Bitmap bitmap, String url, String defaluttext,ShareResultListener l) {
		this.listener = l;
		this.mActivity = activity;
//		if (mWeiboShareAPI == null) {
//			mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(mActivity, Constants.APP_KEY);
//		}
//		mWeiboShareAPI.registerApp();
		registerAPP();
		
		Matrix mtx = new Matrix();
		mtx.postRotate(1);
		sendMessage(title, description, bitmap, url, defaluttext);
	}


	/**
	 * 
	 * @Title: sendMessage
	 * @Description: 第三方应用发送请求消息到微博，唤起微博分享界面。
	 * @param @param title
	 * @param @param description
	 * @param @param bitmap
	 * @param @param url
	 * @param @param defaluttext
	 * @return void
	 * @throws
	 */
	private void sendMessage(String title, String description, Bitmap bitmap, String url, String defaluttext) {
		WBShareEventCallback wbShareEventCallback = WBShareEventCallback.getInstance();
		wbShareEventCallback.setResultListener(this);
		MZLog.d("cj", "==========>>2");
		if (mWeiboShareAPI == null) {
			Log.d("JD_Reader", "mWeiboShareAPI is null");
			Toast.makeText(mActivity, "mWeiboShareAPI is null", 0).show();
			return;
		}
		registerAPP();

		WeiboMultiMessage weiboMessage = new WeiboMultiMessage();// 初始化微博的分享消息

		TextObject tobject = new TextObject();
		tobject.text = title + url;
		tobject.description = description;
		weiboMessage.textObject = tobject;
		if (bitmap != null) {
			MZLog.d("J", "bitmap:"+bitmap.toString());
			ImageObject ip = new ImageObject();
			ip.setImageObject(bitmap);
			weiboMessage.imageObject = ip;
		}
		SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.multiMessage = weiboMessage;

		AuthInfo authInfo = new AuthInfo(mActivity, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
		Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(mActivity);
		String token = "";
		if (accessToken != null) {
			token = accessToken.getToken();
		}
		mWeiboShareAPI.sendRequest(mActivity, request, authInfo, token, new WeiboAuthListener() {

			@Override
			public void onWeiboException(WeiboException arg0) {
				MZLog.d("cj", "WeiboException=======>>>>>");
			}

			@Override
			public void onComplete(Bundle bundle) {
				Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
				AccessTokenKeeper.writeAccessToken(mActivity, newToken);
			}

			@Override
			public void onCancel() {
				MZLog.d("cj", "WeiboException=======>>>>>onCancel");
			}
		});
	}

	/**
	 * 回调函数
	 */
	@Override
	public void onWBShareRusult(int resultType) {
		if (listener != null) {
			listener.onShareRusult(resultType);
		}
	}


}
