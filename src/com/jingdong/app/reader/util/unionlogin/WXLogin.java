package com.jingdong.app.reader.util.unionlogin;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.unionlogin.UnionLoginFactory.LoginResponsesLitener;
import com.jingdong.app.reader.wxapi.WXEventCallback;
import com.jingdong.app.reader.wxapi.WXEventCallback.WXFetchAuthCodeListener;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 
 * @ClassName: WXLogin
 * @Description: 执行微信登录逻辑
 * @author J.Beyond
 * @date 2015-3-13 下午5:58:02
 *
 */
public class WXLogin {

	private static final String WXAPP_ID = "wx79f9198071040f23";
	private static final String WXAPP_SECRET = "8b1364fed802aa0987a261a1477b1b28";
	private static String FETCH_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
	private static WXLogin mInstance;
	private IWXAPI mIwxapi;
	private Activity mActivity;
	private LoginResponsesLitener mResponsesLitener;
	private WXLogin() {
		// TODO Auto-generated constructor stub
	}
	public static WXLogin getInstance() {
		if (mInstance == null) {
			mInstance = new WXLogin();
		}
		return mInstance;
	}
	
	/***
	 * 
	 * @Title: initWXApi
	 * @Description: 将应用注册到微信、判断本机是否安装微信客户端
	 * @param @param activity
	 * @param @param responsesLitener
	 * @param @return
	 * @return boolean
	 * @date 2015-3-13 下午5:55:43
	 * @throws
	 */
	public boolean initWXApi(Activity activity,LoginResponsesLitener responsesLitener) {
		this.mActivity = activity;
		this.mResponsesLitener = responsesLitener;
		mIwxapi = WXAPIFactory.createWXAPI(activity, WXAPP_ID, true);
		mIwxapi.registerApp(WXAPP_ID);
		return mIwxapi.isWXAppInstalled();
	}
	
	/**
	 * 
	 * @Title: sendAuthRequest
	 * @Description: 拉起微信登录页
	 * @param 
	 * @return void
	 * @date 2015-3-13 下午5:56:51
	 * @throws
	 */
	public void sendAuthRequest() {
		
		WXEventCallback wxEventCallback = WXEventCallback.getInstance();
		/**
		 * 监听会在WXEventCallback#onResp方法回调，获取到token
		 */
		wxEventCallback.setFetchAuthCodeListener(new WXFetchAuthCodeListener() {
			
			@Override
			public void onFetchAuthCodeSuccess(String code) {
				// TODO Auto-generated method stub
				fetchAccessToken(code);
			}
		});
		
		SendAuth.Req req = new SendAuth.Req();
		req.scope = "snsapi_userinfo";
		req.state = "wechat_sdk_demo_test";
		mIwxapi.sendReq(req);
	}
	
	/**
	 * 
	 * @Title: fetchAccessToken
	 * @Description: 利用code获取accessToken
	 * @param @param code
	 * @return void
	 * @date 2015-3-13 下午5:37:56
	 * @throws
	 */
	private void fetchAccessToken(String code) {
		// TODO Auto-generated method stub
		RequestParams params = new RequestParams();
		params.add("appid", WXAPP_ID);
		params.add("secret", WXAPP_SECRET);
		params.add("code", code);
		params.add("grant_type", "authorization_code");
		
		WebRequestHelper.get(FETCH_ACCESS_TOKEN_URL, params, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				// TODO Auto-generated method stub
				String resp = new String(arg2);
				try {
					JSONObject obj = new JSONObject(resp);
					String accessToken = obj.getString("access_token");
					long expiresIn = obj.getLong("expires_in");
					String refreshToken = obj.getString("refresh_token");
					String openid = obj.getString("openid");
					String scope = obj.getString("scope");
					
					LoginData data = new LoginData(openid, accessToken, expiresIn, refreshToken, scope);
					if (mResponsesLitener != null) {
						mResponsesLitener.onLoginSuccess(data);
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				// TODO Auto-generated method stub
				String errorStr = new String(arg2);
				try {
					JSONObject obj = new JSONObject(errorStr);
					String errmsg = obj.getString("errmsg");
					int errcode = obj.getInt("errcode");
					MZLog.d("J.Beyond", "fetchAccessToken error::errcode="+errcode+",errmsg"+errmsg);
					if (mResponsesLitener != null) {
						mResponsesLitener.onLoginFail(errmsg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	
	
	
	
}
