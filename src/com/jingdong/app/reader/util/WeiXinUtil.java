package com.jingdong.app.reader.util;

import android.content.Context;
import android.widget.Toast;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.entity.WeiXinEntity;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WeiXinUtil {

	private static final String TAG = "WeiXinUtil";

	private static WeiXinEntity weiXinInfo;

	private static IWXAPI weiXinApi; // 微信api接口类

	public static void createAndRegisterWX(Context context) {
		try {
			weiXinApi = WXAPIFactory.createWXAPI(context, "wx79f9198071040f23");
            weiXinApi.registerApp("wx79f9198071040f23");
		} catch (Exception e) {
			if(Log.D){
				e.printStackTrace();
			}
		}
	}

	public static WeiXinEntity getWeiXinInfo() {
		return weiXinInfo;
	}

	public static void setWeiXinInfo(WeiXinEntity weiXinInfo) {
		WeiXinUtil.weiXinInfo = weiXinInfo;
	}

	/**
	 * 当前微信客户端是否支持微信支付
	 * 
	 * @return
	 */
	public static boolean isWeixinPaySupported() {
		
		if(weiXinApi == null){
			createAndRegisterWX(MZBookApplication.getInstance());
		}
		
		if(weiXinApi != null){
			return weiXinApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
		}
		
		return false;
		
	}

	/**
	 * 是否安装了微信客户端
	 * 
	 * @return
	 */
	public static boolean isWeiXinInstalled() {
		
		if(weiXinApi == null){
			createAndRegisterWX(MZBookApplication.getInstance());
		}
		
		if(weiXinApi != null){
			return weiXinApi.isWXAppInstalled();
		}
		
		return false;
	}

	public static IWXAPI getWeiXinApi() {
		if (weiXinApi == null) {
			weiXinApi = WXAPIFactory.createWXAPI(MZBookApplication.getInstance(), "wx79f9198071040f23");
		}
		return weiXinApi;
	}


	public static void doWeiXinPay(final WeiXinEntity weiXinEntity) {

		if (Log.D) {
			Log.d(TAG, "doWeiXinPay() -->> weiXinEntity = " + weiXinEntity);
		}

		try {
			MZBookApplication.getInstance().getCurrentMyActivity().post(new Runnable() {

				@Override
				public void run() {
					MZLog.i("JD_Reader", "WeixinUtil#doWeiXinPay::run()");
					// 是否安装了微信客户端
					if (!WeiXinUtil.isWeiXinInstalled()) { // 如果没安装，弹提示，并返回
						Toast.makeText(MZBookApplication.getInstance(), "抱歉，您尚未安装微信", Toast.LENGTH_SHORT).show();
						return;
					}

					// 当前微信客户端是否支持微信支付
					if (!WeiXinUtil.isWeixinPaySupported()) { // 如果不支持，弹提示，并返回
						Toast.makeText(MZBookApplication.getInstance(), "请升级到微信最新版本使用", Toast.LENGTH_SHORT).show();
						return;
					}

					if (weiXinEntity == null) {
						return;
					}

					// if (weiXinEntity == null) {
					// weiXinEntity = new WeiXinEntity();
					// weiXinEntity.setAppId("wxd930ea5d5a258f4f");
					// weiXinEntity.setPartnerId("1900000109");
					// weiXinEntity.setPrepayId("11010000001405123e6e5ba38498c2fb");
					// weiXinEntity.setNonceStr("7d91786c01b3931e8d94baf248608979");
					// weiXinEntity.setTimeStamp("1399927585");
					// weiXinEntity.setPackageValue("Sign=Wxpay");
					// weiXinEntity.setSign("763bead686431b28b1082c2dee1835ff62dcc3da");
					// }

					if (Log.D) {
						Log.d(TAG, "doWeiXinPay() -->> appId = " + weiXinEntity.getAppId());
						Log.d(TAG, "doWeiXinPay() -->> partnerId = " + weiXinEntity.getPartnerId());
						Log.d(TAG, "doWeiXinPay() -->> prepayId = " + weiXinEntity.getPrepayId());
						Log.d(TAG, "doWeiXinPay() -->> nonceStr = " + weiXinEntity.getNonceStr());
						Log.d(TAG, "doWeiXinPay() -->> timeStamp = " + weiXinEntity.getTimeStamp());
						Log.d(TAG, "doWeiXinPay() -->> packageValue = " + weiXinEntity.getPackageValue());
						Log.d(TAG, "doWeiXinPay() -->> sign = " + weiXinEntity.getSign());
					}

					try 
                    {
						PayReq req = new PayReq();
						req.appId = weiXinEntity.getAppId();
						req.partnerId = weiXinEntity.getPartnerId();
						req.prepayId = weiXinEntity.getPrepayId();
						req.nonceStr = weiXinEntity.getNonceStr();
						req.timeStamp = weiXinEntity.getTimeStamp();
						req.packageValue = weiXinEntity.getPackageValue();
						req.sign = weiXinEntity.getSign();
						MZLog.i("JD_Reader", "WeixinUtil#doWeiXinPay::sign="+req.sign);
						WeiXinUtil.getWeiXinApi().sendReq(req);
					} catch (Exception e) {
						if(Log.D){
							e.printStackTrace();
						}
						
					}

				}
			});
		} catch (Exception e) {
			if (Log.D) {
				e.printStackTrace();
			}
		}
	}

}
