package com.jingdong.app.reader.wbapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.jingdong.app.reader.oauth.Constants;
import com.jingdong.app.reader.wbapi.WBShareEventCallback.WBShareResultListener;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;

public class WBRespActivity extends Activity implements IWeiboHandler.Response{

	private IWeiboShareAPI mWeiboShareAPI= null;
	private WBShareEventCallback mShareEventCallback;
	@Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this,Constants.APP_KEY);
        mWeiboShareAPI.registerApp();
        mShareEventCallback = WBShareEventCallback.getInstance();
        mShareEventCallback.setmActivity(this);
//        Log.d("JD_Reader", "WBRespActivity---->onCreate");
//        Log.d("JD_Reader", "WBRespActivity---->mWeiboShareAPI:"+mWeiboShareAPI);
//		Log.d("JD_Reader", "WBRespActivity---->wbShareEventCallback::"+mShareEventCallback);
        mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("JD_Reader", "WBRespActivity---->onNewIntent");
        Log.d("JD_Reader", "mWeiboShareAPI:"+mWeiboShareAPI);
        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
        if (mWeiboShareAPI != null) {
        	mWeiboShareAPI.handleWeiboResponse(intent, this);
		}
    }

	@Override
	public void onResponse(BaseResponse baseResp) {
		// TODO Auto-generated method stub
		Log.d("JD_Reader", "weibo callback::"+baseResp.toString());
		WBShareResultListener shareResultListener = WBShareEventCallback.getInstance().getResultListener();
		if (shareResultListener == null) {
			Log.d("JD_Reader", "shareResultListener is null");
			return;
			
		}
		switch (baseResp.errCode) {
		case WBConstants.ErrorCode.ERR_OK:
			shareResultListener.onWBShareRusult(WBShareEventCallback.WBSHARE_SUCCESS);
			break;
		case WBConstants.ErrorCode.ERR_CANCEL:
			shareResultListener.onWBShareRusult(WBShareEventCallback.WBSHARE_CANCEL);
			break;
		case WBConstants.ErrorCode.ERR_FAIL:
			shareResultListener.onWBShareRusult(WBShareEventCallback.WBSHARE_FAILURE);
			break;
		}
		
		this.finish();
	}
        
}
