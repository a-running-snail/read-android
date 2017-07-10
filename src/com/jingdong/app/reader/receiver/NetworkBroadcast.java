package com.jingdong.app.reader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.text.TextUtils;

import com.jingdong.app.reader.activity.MyActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.LoginHelper;
import com.jingdong.app.reader.util.LoginHelper.LoginListener;
import com.jingdong.app.reader.util.MZLog;

public class NetworkBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		MZLog.i("J","NetworkBroadcast onReceive");

		State wifiState = null;
		State mobileState = null;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		if (wifiState != null && mobileState != null && State.CONNECTED != wifiState && State.CONNECTED == mobileState) {
			// 手机网络连接成功
//			autoLogin(context);
		} else if (wifiState != null && mobileState != null && State.CONNECTED != wifiState && State.CONNECTED != mobileState) {
			// 手机没有任何的网络
			
		} else if (wifiState != null && State.CONNECTED == wifiState) {
			// 无线网络连接成功
//			autoLogin(context);
			
		}
	}
	
	private void autoLogin(final Context context) {
		MZBookApplication app = (MZBookApplication) context.getApplicationContext();
		final MyActivity myActivity = app.getCurrentMyActivity();
		if (!app.isLogin() && !(myActivity instanceof LoginActivity)) {
			String userName = LoginUser.getUserName();
			String getpsw = LoginUser.getpsw();
			if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(getpsw)) {
				LoginHelper.doLogin(context, userName, getpsw,null, true, new LoginListener() {
					
					@Override
					public void onLoginSuccess() {
						// TODO Auto-generated method stub
						MZLog.i("J","NetworkBroadcast auto login success");
						
					}
					
					@Override
					public void onLoginFail(String errCode) {
						// TODO Auto-generated method stub
						MZLog.e("J","NetworkBroadcast auto login fail");
						
					}
				});
			}
		}
	}

}
