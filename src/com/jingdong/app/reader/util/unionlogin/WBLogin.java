package com.jingdong.app.reader.util.unionlogin;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.oauth.Constants;
import com.jingdong.app.reader.util.unionlogin.UnionLoginFactory.LoginResponsesLitener;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;


/**
 * 
 * @ClassName: WBLogin
 * @Description: 执行微博登录逻辑（SSO登录，若手机安装微博客户端会拉起微博客户端登录页，否则会web登录）
 * @author J.Beyond
 * @date 2015-3-13 下午2:42:32
 *
 */
public class WBLogin {

	private static WBLogin mInstance;
	private Activity mActivity;
	private AuthInfo mAuthInfo;
	private SsoHandler mSsoHandler;
	private LoginResponsesLitener mResponsesLitener;
	
	private WBLogin() {
		// TODO Auto-generated constructor stub
	}
	public static WBLogin getInstance() {
		if (mInstance == null) {
			mInstance = new WBLogin();
		}
		
		return mInstance;
	}
	
	public void initWeiboAuth(Activity act,LoginResponsesLitener responsesLitener) {
		this.mActivity = act;
		this.mResponsesLitener = responsesLitener;
		mAuthInfo = new AuthInfo(act, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
		mSsoHandler = new SsoHandler(act, mAuthInfo);
	}
	
	public void wbLogin() {
		mSsoHandler.authorize(new WeiboAuthListener() {
			
			@Override
			public void onComplete(Bundle values) {
				// TODO Auto-generated method stub
				Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
				if (accessToken.isSessionValid()) {
					//将登录获取的token等信息回调
					if (mResponsesLitener != null) {
						LoginData data = new LoginData();
						data.setAccessToken(accessToken.getToken());
						data.setExpiresIn(accessToken.getExpiresTime());
						data.setUid(accessToken.getUid());
						
						mResponsesLitener.onLoginSuccess(data);
					}
					//保存accessToken到本地
					WBTokenKeeper.writeAccessToken(mActivity, accessToken);
				}else{
					 // 以下几种情况，您会收到 Code：
	                // 1. 当您未在平台上注册的应用程序的包名与签名时；
	                // 2. 当您注册的应用程序包名与签名不正确时；
	                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
	                String code = values.getString("code");
	                String message = "授权失败";
	                if (!TextUtils.isEmpty(code)) {
	                    message = message + "\nObtained the code: " + code;
	                }
//	                Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
	                
	                if (mResponsesLitener != null) {
						mResponsesLitener.onLoginFail(message);
					}
				}
			}

			@Override
			public void onWeiboException(WeiboException e) {
//				 Toast.makeText(mActivity, 
//		                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
				if (mResponsesLitener != null) {
					mResponsesLitener.onLoginFail("Auth exception:"+e.getMessage());
				}
			}
			
			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				Toast.makeText(mActivity, 
	                    "取消授权", Toast.LENGTH_LONG).show();
				if (mResponsesLitener != null) {
					mResponsesLitener.onLoginCancel();
				}
			}
		});
	}
	
	
	
	
}
