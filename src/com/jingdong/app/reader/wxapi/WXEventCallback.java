package com.jingdong.app.reader.wxapi;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.download.util.L;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

public class WXEventCallback implements IWXAPIEventHandler{
	
	public static final int WXSHARE_SUCCESS = 1;
	public static final int WXSHARE_CANCEL = 2;
	public static final int WXSHARE_FAILURE = 3;
	
	private static WXEventCallback mShareEventCallBack;
	private WXEventCallback() {
		// TODO Auto-generated constructor stub
	}
	public static WXEventCallback getInstance() {
		if (mShareEventCallBack == null) {
			mShareEventCallBack = new WXEventCallback();
		}
		return mShareEventCallBack;
	}
	
	public interface WXShareResultListener{
		public void onWXShareRusult(int resultType);
	}
	private WXShareResultListener mShareResultListener;
	private WXFetchAuthCodeListener mFetchAuthCodeListener;
	private Activity mActivity;
	
	public void setmActivity(Activity mActivity) {
		this.mActivity = mActivity;
	}
	public void setShareResultListener(WXShareResultListener shareResultListener) {
		this.mShareResultListener = shareResultListener;
	}
	
	
	
	public void setFetchAuthCodeListener(
			WXFetchAuthCodeListener fetchAuthCodeListener) {
		this.mFetchAuthCodeListener = fetchAuthCodeListener;
	}

	public interface WXFetchAuthCodeListener{
		
		public void onFetchAuthCodeSuccess(String code);
	}

	@Override
	public void onReq(BaseReq req) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onResp(BaseResp resp) {
		L.d("onResp");

		if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
			L.d("onResp#COMMAND_SENDAUTH");
			SendAuth.Resp sendResp = (SendAuth.Resp) resp;
			String authCode = sendResp.code;
			if (mFetchAuthCodeListener != null && TextUtils.isEmpty(authCode)) {
				mFetchAuthCodeListener.onFetchAuthCodeSuccess(authCode);
			}
		}else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
			L.d("onResp#COMMAND_SENDMESSAGE_TO_WX");
			switch (resp.errCode) {  
			case BaseResp.ErrCode.ERR_OK:  
				//分享成功  
				if (mShareResultListener != null) {
					mShareResultListener.onWXShareRusult(WXSHARE_SUCCESS);
				}
				break;  
			case BaseResp.ErrCode.ERR_USER_CANCEL:  
				//分享取消  
				if (mShareResultListener != null) {
					mShareResultListener.onWXShareRusult(WXSHARE_CANCEL);
				}
				break;  
			case BaseResp.ErrCode.ERR_AUTH_DENIED:  
				//分享拒绝  
				if (mShareResultListener != null) {
					mShareResultListener.onWXShareRusult(WXSHARE_FAILURE);
				}
				break;  
			}
		}
		if (mActivity != null) {
    		mActivity.finish();
		}
	}

}
