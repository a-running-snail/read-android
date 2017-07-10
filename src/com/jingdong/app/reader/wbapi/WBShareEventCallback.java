package com.jingdong.app.reader.wbapi;

import android.app.Activity;

/**
 * WBRespActivity与SharePopupWindow之间通信接口
 * 单例
 * @author ouyanghaibing
 *
 */
public class WBShareEventCallback{
	
	public static final int WBSHARE_SUCCESS = 1;
	public static final int WBSHARE_CANCEL = 2;
	public static final int WBSHARE_FAILURE = 3;
	
	private Activity mActivity;
	private WBShareResultListener mResultListener;
	
	private static WBShareEventCallback mShareEventCallBack;
	private WBShareEventCallback() {
		// TODO Auto-generated constructor stub
	}
	public static WBShareEventCallback getInstance() {
		if (mShareEventCallBack == null) {
			mShareEventCallBack = new WBShareEventCallback();
		}
		return mShareEventCallBack;
	}
	
	public interface WBShareResultListener{
		public void onWBShareRusult(int resultType);
	}
	
	public void setmActivity(Activity mActivity) {
		this.mActivity = mActivity;
	}
	
	public void setResultListener(WBShareResultListener mResultListener) {
		this.mResultListener = mResultListener;
	}
	public WBShareResultListener getResultListener() {
		return mResultListener;
	}
	
	

}
