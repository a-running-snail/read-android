package com.jingdong.app.reader.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mikephil.charting.utils.Utils;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.oauth.AccessTokenKeeper;
import com.jingdong.app.reader.oauth.Constants;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.wbapi.WBShareEventCallback;
import com.jingdong.app.reader.wbapi.WBShareEventCallback.WBShareResultListener;
import com.jingdong.app.reader.wxapi.WXEventCallback;
import com.jingdong.app.reader.wxapi.WXEventCallback.WXShareResultListener;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class SharePopupWindowReadingData implements WXShareResultListener,WBShareResultListener {

	private static final int THUMB_SIZE = 150;
	private String titleString;
	private String discriptString;
	private String urlpathString;
	private String urlString;
	private Activity mActivity = null;
	private boolean isShowing = false;
	private PopupWindow mPopupWindow = null;
	private RelativeLayout popup_weixin;// 微信好友
	private RelativeLayout popup_sina;// 新浪微博
	/** 微博微博分享接口实例 */
	private IWeiboShareAPI mWeiboShareAPI = null;

	public static final String WXAPP_ID = "wx79f9198071040f23";
	private IWXAPI api;
	private Bitmap bitmap;
	private RelativeLayout popup_weixin_friend;// 微信朋友圈
	private LinearLayout cancelLayout;// 取消
	private int height;
	private View popupView;
	private RelativeLayout popuplayout;
	private RelativeLayout popup_more;
	private RelativeLayout popup_community;

	public interface onPopupWindowItemClickListener {
		public void onPopupWindowWeixinClick();

		public void onPopupWindowSinaClick();

		public void onPopupWindowWeixinFriend();

		public void onPopupWindowCancel();

		public void onPopupWindowMore();
		
	}

	public onPopupWindowItemClickListener listener;

	public onPopupWindowItemClickListener getListener() {
		return listener;
	}

	public void setListener(onPopupWindowItemClickListener listener) {
		this.listener = listener;
	}

	public SharePopupWindowReadingData(Activity activity) {
		this.mActivity = activity;
		initView();
	}
	
	public SharePopupWindowReadingData(Activity activity,boolean unableWindow) {//不需要弹出框
		this.mActivity = activity;
	}
	
	

	public void setWeiboShareAPI(IWeiboShareAPI weiboShareAPI) {
		this.mWeiboShareAPI = weiboShareAPI;
	}



	public void initView() {

		popupView = mActivity.getLayoutInflater().inflate(R.layout.share_popup,
				null);
		popuplayout = (RelativeLayout) popupView.findViewById(R.id.popuplayout);

		DisplayMetrics dm = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		height = dm.heightPixels;

		mPopupWindow = new PopupWindow(popupView, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(mActivity
				.getResources().getColor(R.color.bg_menu_shadow)));

		popup_weixin = (RelativeLayout) popupView
				.findViewById(R.id.popup_weixin);
		popup_sina = (RelativeLayout) popupView.findViewById(R.id.popup_sina);
		popup_weixin_friend = (RelativeLayout) popupView
				.findViewById(R.id.popup_weixin_friend);
		popup_more = (RelativeLayout) popupView.findViewById(R.id.popup_more);
		cancelLayout = (LinearLayout) popupView.findViewById(R.id.cancel);
		

		popup_weixin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (listener != null)
					listener.onPopupWindowWeixinClick();
			}

		});

		popup_sina.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (listener != null)
					listener.onPopupWindowSinaClick();
			}

		});

		popup_weixin_friend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (listener != null)
					listener.onPopupWindowWeixinFriend();
			}
		});

		popup_more.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (listener != null) {
					listener.onPopupWindowMore();
				}
			}
		});

		cancelLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				listener.onPopupWindowCancel();
			}
		});
	}

	public void More(Activity context, final String title,
			final String description, final Bitmap bitmap, final String url) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		// sendIntent.putExtra(Intent.EXTRA_TITLE, title);
		sendIntent.putExtra(Intent.EXTRA_TEXT, title + description + url);
		sendIntent.setType("text/plain");
		// sendIntent.setData(Uri.parse(url));
		// 允许启动新的Activity
		sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Intent chooserIntent = Intent.createChooser(sendIntent, "分享到");
		if (chooserIntent == null) {
			return;
		}
		try {
			context.startActivity(chooserIntent);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(context, "Can't find share component to share",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void weixin(final Activity context, final String title,
			final String description, final Bitmap bitmap, final String url,
			int flag) {
		MZLog.d("cj", "==========>>cc");
		if(!isShareWeiXin(context)){
			Toast.makeText(context, "您还未安装微信客户端", Toast.LENGTH_SHORT).show();
			return;
		}
		sendWeiXinMessage(title, description, bitmap, url, flag);
	}

	public boolean isShareWeiXin(final Activity context) {
		api = WXAPIFactory.createWXAPI(context, WXAPP_ID, true);
		api.registerApp(WXAPP_ID);
		return api.isWXAppInstalled();
	}

	private void sendWeiXinMessage(String title, String description,
			Bitmap bitmap, String url, int flag) {
		WXEventCallback wxShareEventCallBack = WXEventCallback.getInstance();
		wxShareEventCallBack.setShareResultListener(this);
		
		WXWebpageObject webpage = new WXWebpageObject();
		if (url != null) {
			webpage.webpageUrl = url;
		}
		WXMediaMessage msg = new WXMediaMessage(webpage);
		if (title != null) {
			msg.title = title;
		}
		if (description != null) {
			msg.description = description;
		}
		if (bitmap != null) {
			MZLog.d("cj", "dddddddddd");
			msg.thumbData = bmpToByteArray(bitmap, true);
		}
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = flag;
		MZLog.d("cj", "ffffffffffff");
		api.sendReq(req);
		MZLog.d("cj", "llllll");
	}

	public static byte[] bmpToByteArray(final Bitmap bmp,
			final boolean needRecycle) {
		Log.d("cj", "gggggg");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 5, output);
		int options = 50;
		while (output.toByteArray().length / 1024 > 50) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			output.reset();// 重置baos即清空baos
			options -= 10;// 每次都减少10
			Log.d("cj", "!!!!!!!!!!");
			bmp.compress(Bitmap.CompressFormat.JPEG, options, output);// 这里压缩options%，把压缩后的数据存放到baos中
		}

		if (needRecycle) {
			bmp.recycle();
		}
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void sina(Activity context, String title, String description,
			Bitmap bitmap, String url, String defaluttext) {

		// 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
		// 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
		// NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
		// 获取微博客户端相关信息，如是否安装、支持 SDK 的版本
//		MZBookApplication.getInstance().mWeiboShareAPI.registerApp();
		if (mWeiboShareAPI == null) {
			mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(mActivity,Constants.APP_KEY);
		}
		mWeiboShareAPI.registerApp();
        Log.d("JD_Reader", "sharePopupWindow---->mWeiboShareAPI:"+mWeiboShareAPI);

		Matrix mtx = new Matrix(); 
        mtx.postRotate(1); 
		sendMessage(context, title, description, bitmap, url, defaluttext);
	}

	public void dismiss() {
		if (mPopupWindow != null) {	
			AnimationSet animTop = new AnimationSet(true);
			TranslateAnimation transTop = new TranslateAnimation(0, 0, 0,
					height);
			// 设置动画效果持续的时间
			animTop.setDuration(500); // transTop
			// 将anim对象添加到AnimationSet对象中
			animTop.addAnimation(transTop);
			animTop.setFillAfter(false);
			popupView.startAnimation(animTop);
			mPopupWindow.dismiss();
			setShowing(false);
			MZLog.d("cj", "dismiss=========>>");
		}
	}

	public void show(View v) {
		if (mPopupWindow != null) {
			AnimationSet animTop = new AnimationSet(true);
			TranslateAnimation transTop = new TranslateAnimation(0, 0, height,
					0);
			// 设置动画效果持续的时间
			animTop.setDuration(500); // transTop
			// 将anim对象添加到AnimationSet对象中
			animTop.addAnimation(transTop);
			animTop.setFillAfter(true);
			popupView.startAnimation(animTop);
			mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
			setShowing(true);
			MZLog.d("cj", "show=========>>");
		}

	}

	public boolean isShowing() {
		return isShowing;
	}

	public void setShowing(boolean isShowing) {
		this.isShowing = isShowing;
	}

	private TextObject getTextObj(String text) {
		TextObject textObject = new TextObject();
		textObject.text = "ddd";
		return textObject;
	}

	/**
	 * 第三方应用发送请求消息到微博，唤起微博分享界面。
	 * 
	 * @see {@link #sendMultiMessage} 或者 {@link #sendSingleMessage}
	 */
	private void sendMessage(Activity context, String title,
			String description, Bitmap bitmap, String url, String defaluttext) {
		WBShareEventCallback wbShareEventCallback = WBShareEventCallback.getInstance();
		wbShareEventCallback.setResultListener(this);
		Log.d("JD_Reader", "sharePopupWindow---->wbShareEventCallback::"+wbShareEventCallback);
		MZLog.d("cj", "==========>>2");
		if (mWeiboShareAPI == null) {
			Log.d("JD_Reader", "mWeiboShareAPI is null");
			return;
		}
		sendMultiMessage(title, description, bitmap, url, defaluttext);
	}

	/**
	 * 创建多媒体（网页）消息对象。
	 * 
	 * @return 多媒体（网页）消息对象。
	 */
	private WebpageObject getWebpageObj(String title, String description,
			Bitmap bitmap, String url, String defaluttext) {

		WebpageObject mediaObject = new WebpageObject();
		
		mediaObject.identify = Utility.generateGUID();
		if (title != null) {
			mediaObject.title = title;
		}
		if (description != null) {
			mediaObject.description = description;
		}
		if (bitmap != null) {
			// 设置 Bitmap 类型的图片到视频对象里
			mediaObject.setThumbImage(bitmap);
		}
		if (url != null) {
			mediaObject.actionUrl = url;
		}
		if (defaluttext != null) {
			mediaObject.defaultText = defaluttext;
		}
		return mediaObject;
	}

	private void sendMultiMessage(String title, String description,
			Bitmap bitmap, String url, String defaluttext) {
		WeiboMultiMessage weiboMessage = new WeiboMultiMessage();// 初始化微博的分享消息

		TextObject tobject=new TextObject();
		tobject.text=title+url;
		tobject.description = description;
		weiboMessage.textObject=tobject;
		if (bitmap != null) {
			ImageObject ip=new ImageObject();
			ip.setImageObject(bitmap);
			weiboMessage.imageObject=ip;
		}
		SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.multiMessage = weiboMessage;
		
        AuthInfo authInfo = new AuthInfo(mActivity, Constants.APP_KEY,
				Constants.REDIRECT_URL, Constants.SCOPE);
        Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(mActivity);
        String token = "";
        if (accessToken != null) {
            token = accessToken.getToken();
        }
        mWeiboShareAPI.sendRequest(mActivity, request, authInfo, token, new WeiboAuthListener() {
            
            @Override
            public void onWeiboException( WeiboException arg0 ) {
            	MZLog.d("cj", "WeiboException=======>>>>>" );
            }
            
            @Override
            public void onComplete( Bundle bundle ) {
                // TODO Auto-generated method stub
                Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                AccessTokenKeeper.writeAccessToken(mActivity, newToken);
            }
            
            @Override
            public void onCancel() {
            	MZLog.d("cj", "WeiboException=======>>>>>onCancel");
            }
        });
//		if (mWeiboShareAPI != null) {
//			mWeiboShareAPI.sendRequest(mActivity, request); // 发送请求消息到微博,唤起微博分享界面
//		}
//		MZLog.d("cj", "==========>>5");
	}

	/**
	 * 第三方应用发送请求消息到微博，唤起微博分享界面。 当{@link IWeiboShareAPI#getWeiboAppSupportAPI()}
	 * < 10351 时，只支持分享单条消息，即 文本、图片、网页、音乐、视频中的一种，不支持Voice消息。
	 * 
	 * @param hasText
	 *            分享的内容是否有文本
	 * @param hasImage
	 *            分享的内容是否有图片
	 * @param hasWebpage
	 *            分享的内容是否有网页
	 * @param hasMusic
	 *            分享的内容是否有音乐
	 * @param hasVideo
	 *            分享的内容是否有视频
	 */
	private void sendSingleMessage(String title, String description,
			Bitmap bitmap, String url, String defaluttext) {

		// 1. 初始化微博的分享消息
		// 用户可以分享文本、图片、网页、音乐、视频中的一种
		WeiboMessage weiboMessage = new WeiboMessage();

		weiboMessage.mediaObject = getWebpageObj(title, description, bitmap,
				url, defaluttext);
		// 2. 初始化从第三方到微博的消息请求
		SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
		// 用transaction唯一标识一个请求
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.message = weiboMessage;
		Log.d("JD_Reader", request.toString());
		// 3. 发送请求消息到微博，唤起微博分享界面
		if (mWeiboShareAPI != null) {
			mWeiboShareAPI.sendRequest(mActivity, request); // 发送请求消息到微博,唤起微博分享界面
		}
		MZLog.d("cj", "==========>>6");
	}

	@Override
	public void onWXShareRusult(int resultType) {
		switch (resultType) {
		case WXEventCallback.WXSHARE_SUCCESS:
			Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();

			break;
		case WXEventCallback.WXSHARE_CANCEL:
			Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();

			break;
		case WXEventCallback.WXSHARE_FAILURE:
			Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();
			
			break;
		}
		this.dismiss();
	}


	@Override
	public void onWBShareRusult(int resultType) {
		switch (resultType) {
		case WBShareEventCallback.WBSHARE_SUCCESS:
			Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();

			break;
		case WBShareEventCallback.WBSHARE_CANCEL:
			Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();

			break;
		case WBShareEventCallback.WBSHARE_FAILURE:
			Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();
			
			break;
		}
		this.dismiss();
	}
	
	public void weixinImg(final Activity context, final Bitmap bitmap,
			int flag) {
		MZLog.d("cj", "==========>>cc");
		if(!isShareWeiXin(context)){
			Toast.makeText(context, "您还未安装微信客户端", Toast.LENGTH_SHORT).show();
			return;
		}
		sendWeiXinImgMessage(bitmap,flag);
	}

	
	private void sendWeiXinImgMessage(Bitmap bitmap, int flag) {
		WXEventCallback wxShareEventCallBack = WXEventCallback.getInstance();
		wxShareEventCallBack.setShareResultListener(this);
		
		WXImageObject imgObj = new WXImageObject(bitmap);
		
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		// 获得图片的宽高
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scaleWidth = (float) THUMB_SIZE / (float) width;
		float scaleHeight = (float) THUMB_SIZE / (float) height;
		
		float scale = (scaleWidth < scaleHeight ? scaleWidth : scaleHeight);
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, (int)(width * scale), (int)(height * scale), true);
		if (thumbBmp != null) {
			msg.thumbData = bmpToByteArrays(thumbBmp, true);
		}
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = flag;
		bitmap.recycle();
		api.sendReq(req);
	}

	public static byte[] bmpToByteArrays(final Bitmap bmp,
			final boolean needRecycle) {
		Log.d("cj", "gggggg");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);

		if (needRecycle) {
			bmp.recycle();
		}
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
