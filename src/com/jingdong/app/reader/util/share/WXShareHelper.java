package com.jingdong.app.reader.util.share;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.wxapi.WXEventCallback;
import com.jingdong.app.reader.wxapi.WXEventCallback.WXShareResultListener;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 
 * @ClassName: WXShareHelper
 * @Description: 微信分享工具类
 * @author J.Beyond
 * @date 2015年6月19日 下午1:50:33
 *
 */
public class WXShareHelper implements WXShareResultListener {

	private static WXShareHelper mInstance;
	public static final String WXAPP_ID = "wx79f9198071040f23";
	private static final int THUMB_SIZE = 150;

	private static IWXAPI mWXApi;
	private Activity mActivity;
	private ShareResultListener listener;

	private WXShareHelper() {
	}

	public static WXShareHelper getInstance() {
		if (mInstance == null) {
			synchronized (WXShareHelper.class) {
				if (mInstance == null) {
					mInstance = new WXShareHelper();
				}
			}
		}
		return mInstance;
	}

	/**
	 * 
	 * @Title: isWXInstalled
	 * @Description: 判断微信是否安装
	 * @param @param context
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	private boolean isWXInstalled(Activity context) {
		if (mWXApi == null) {
			mWXApi = WXAPIFactory.createWXAPI(context, WXAPP_ID, true);
			mWXApi.registerApp(WXAPP_ID);
		}
		return mWXApi.isWXAppInstalled();
	}

	/**
	 * 
	 * @Title: doShare
	 * @Description: 对外公开的微信分享方法
	 * @param @param context
	 * @param @param title
	 * @param @param description
	 * @param @param bitmap
	 * @param @param url
	 * @param @param flag：0 微信好友；1 朋友圈
	 * @return void
	 * @throws
	 */
	public void doShare(Activity context, final String title, final String description, String imageUrl, final String url, final int flag, ShareResultListener l) {
		this.mActivity = context;
		this.listener = l;
		if (!isWXInstalled(context)) {
			ToastUtil.showToastInThread("您还未安装微信客户端", Toast.LENGTH_SHORT);
//			Toast.makeText(context, "您还未安装微信客户端", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!TextUtils.isEmpty(imageUrl)) {
			ParseBitMapTask bitMapTask = new ParseBitMapTask();
			bitMapTask.setListener(new AyncTaskListener() {
				
				@Override
				public void onFinish(Bitmap bitmap) {
					sendWeiXinMessage(title, description, bitmap, url, flag);
				}
			});
			bitMapTask.execute(imageUrl);
		}else{
			sendWeiXinMessage(title, description, null, url, flag);
		}
	}
	
	public void doShare(Activity context, final String title, final String description, Bitmap bitmap, final String url, final int flag, ShareResultListener l) {
		this.mActivity = context;
		this.listener = l;
		if (!isWXInstalled(context)) {
			ToastUtil.showToastInThread("您还未安装微信客户端", Toast.LENGTH_SHORT);
			return;
		}
		sendWeiXinMessage(title, description, bitmap, url, flag);
	}

	/**
	 * 
	 * @Title: sendWeiXinMessage
	 * @Description: 组装参数调用微信分享API
	 * @param @param title
	 * @param @param description
	 * @param @param bitmap
	 * @param @param url
	 * @param @param flag
	 * @return void
	 * @throws
	 */
	private void sendWeiXinMessage(String title, String description, Bitmap bitmap, String url, int flag) {
		MZLog.d("WXApi", "title="+title+"\ndescription="+description+"\nurl="+url+"\nflag="+flag);
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
			// 获得图片的宽高
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			float scaleWidth = (float) THUMB_SIZE / (float) width;
			float scaleHeight = (float) THUMB_SIZE / (float) height;
			float scale = (scaleWidth < scaleHeight ? scaleWidth : scaleHeight);
			Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, (int) (width * scale), (int) (height * scale), true);
			msg.thumbData = ImageUtils.bmpToByteArray(thumbBmp, true);
			MZLog.d("cj", "thumbData::"+msg.thumbData);
		}
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = flag;
		MZLog.d("cj", "ffffffffffff");
		mWXApi.sendReq(req);
		MZLog.d("cj", "llllll");
	}

	/**
	 * 带回调函数的分享图片方法
	 * @param context
	 * @param bitmap
	 * @param flag
	 * @param l
	 */
	public void shareImage(Activity context, Bitmap bitmap, int flag,ShareResultListener l) {
		this.mActivity = context;
		this.listener = l;
		if (!isWXInstalled(context)) {
			ToastUtil.showToastInThread("您还未安装微信客户端", Toast.LENGTH_SHORT);
//			ToastUtil.makeText(context, "您还未安装微信客户端", Toast.LENGTH_SHORT).show();
			return;
		}
		sendWeiXinImgMessage(bitmap, flag);
	}
	
	/**
	 * 
	 * @Title: shareImage
	 * @Description: 分享图片
	 * @param @param context
	 * @param @param bitmap
	 * @param @param flag
	 * @return void
	 * @throws
	 */
	public void shareImage(Activity context, Bitmap bitmap, int flag) {
		this.mActivity = context;
		if (!isWXInstalled(context)) {
			ToastUtil.showToastInThread("您还未安装微信客户端", Toast.LENGTH_SHORT);
			return;
		}
		sendWeiXinImgMessage(bitmap, flag);
	}
	/**
	 * 
	 * @Title: shareImage
	 * @Description: 分享图片
	 * @param @param context
	 * @param @param bitmap
	 * @param @param flag
	 * @return void
	 * @throws
	 */
	public void shareImage(Activity context, String imageUrl, final int flag) {
		this.mActivity = context;
		if (!isWXInstalled(context)) {
			ToastUtil.showToastInThread("您还未安装微信客户端", Toast.LENGTH_SHORT);
			return;
		}
		
		ParseBitMapTask bitMapTask = new ParseBitMapTask();
		bitMapTask.setListener(new AyncTaskListener() {
			
			@Override
			public void onFinish(Bitmap bitmap) {
				if (bitmap != null) {
					sendWeiXinImgMessage(bitmap, flag);
				}
			}
		});
		bitMapTask.execute(imageUrl);
		
	}
	
	

	/**
	 * 
	 * @Title: sendWeiXinImgMessage
	 * @Description: 分享图片到微信
	 * @param @param bitmap
	 * @param @param flag：0 微信好友；1 朋友圈
	 * @return void
	 * @throws
	 */
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
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, (int) (width * scale), (int) (height * scale), true);
		if (thumbBmp != null) {
			msg.thumbData = ImageUtils.bmpToByteArray(thumbBmp, true);
		}

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = flag;
		bitmap.recycle();
		mWXApi.sendReq(req);
	}


	@Override
	public void onWXShareRusult(int resultType) {
		if (listener != null) {
			listener.onShareRusult(resultType);
		}
	}

}
