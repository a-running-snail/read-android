package com.jingdong.app.reader.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.user.LoginUser;

public class ActivityUtils {

	public static String getActivityLabel(Activity activity) {
		try {
			int labelRes = activity.getPackageManager()
					.getActivityInfo(activity.getComponentName(), PackageManager.GET_META_DATA).labelRes;
			return activity.getString(labelRes);
		} catch (Exception e) {
			return activity.getString(R.string.app_name);
		}
	}
	
	/**
	 * 获取类名
	 * @param instance
	 * @return
	 */
	public static String getClassName(Object instance) {
		String activityName = instance.getClass().getSimpleName();
		if (activityName.indexOf(".") >= 0) {
			int index = activityName.lastIndexOf(".");
			activityName = activityName.substring(index + 1);
		}
		return activityName;
	}
	
	/**
	 * 启动Activity
	 * @param context
	 * @param intent
	 */
	public static void startActivity(Context context,Intent intent){
		
		if(LoginUser.isLogin()){
			context.startActivity(intent);
		}
		else {
			Intent it = new Intent(context, LoginActivity.class);
			context.startActivity(it);
		}
	}
}
