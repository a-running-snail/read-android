package com.jingdong.app.reader.util.unionlogin;

import android.app.Activity;
import android.widget.Toast;

/**
 * 
 * @ClassName: UnionLoginTool
 * @Description: 联合登录工具类（管理所有第三方登录）
 * @author J.Beyond
 * @date 2015-3-13 下午2:48:46
 *
 */
public class UnionLoginFactory {

	/**
	 * 
	 * @ClassName: LoginResponsesLitener
	 * @Description: 登录结果回调接口
	 * @author J.Beyond
	 * @date 2015-3-13 下午2:49:48
	 *
	 */
	public interface LoginResponsesLitener{
		void onLoginSuccess(LoginData data);
		void onLoginCancel();
		void onLoginFail(String errorInfo);
	}
	
	/**
	 * 
	 * @Title: weiboLogin
	 * @Description: 微博登录入口
	 * @param @param act
	 * @param @param loginResponsesLitener
	 * @return void
	 * @date 2015-3-13 下午2:50:17
	 * @throws
	 */
	public static void weiboLogin(Activity act,LoginResponsesLitener loginResponsesLitener) {
		WBLogin wbLogin = WBLogin.getInstance();
		wbLogin.initWeiboAuth(act, loginResponsesLitener);
		wbLogin.wbLogin();
	}
	
	
	/**
	 * 
	 * @Title: wxlogin
	 * @Description: 微信登录入口
	 * @param @param act
	 * @param @param loginResponsesLitener
	 * @return void
	 * @date 2015-3-13 下午5:59:26
	 * @throws
	 */
	public static void wxlogin(Activity act,LoginResponsesLitener loginResponsesLitener) {
		WXLogin wxLogin = WXLogin.getInstance();
		if (wxLogin.initWXApi(act, loginResponsesLitener)) {
			wxLogin.sendAuthRequest();
		}else {
			//未安装微信客户端
			Toast.makeText(act, "您还未安装微信客户端！", Toast.LENGTH_LONG).show();
		}
	}
	
	
	
}
