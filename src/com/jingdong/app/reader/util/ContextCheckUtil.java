
package com.jingdong.app.reader.util;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.app.NotificationCompat.Action;

public final class ContextCheckUtil {

    private ContextCheckUtil() {
    }
    private static boolean isFromJdLoad=false;
    public static void checkContext(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must no be null!");
        }
        if (context instanceof Activity) {
            throw new IllegalArgumentException("I only need Context instead of Activity!");
        }
    }
    
    //进入91助手详情页的代码
	public static void enterDetail(Context context, String packageName,boolean isFromJd) {
		Uri uri = Uri.parse("91market://details?id=" + packageName);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		boolean support = queryActivity(context, intent);
		if(isFromJd||isFromJdLoad){
			if(support){
				context.startActivity(intent);
				isFromJdLoad=false;
			}else {
				//下载91市场
				uri = Uri.parse("http://dl.sj.91.com/business/91soft/91assistant_Andphone_lite163.apk");
				intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    context.startActivity(intent);
			    isFromJdLoad=true;
			}
		}
	}
	
	
	/**
	 * 查询Activity
	 *
	 * @param ctx
	 * @param packageName
	 * @return
	 */
	public static boolean queryActivity(Context context, Intent intent) {
		PackageManager pm = context.getPackageManager();
		try {
			List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
			if (apps.isEmpty())
				return false;
			else
				return true;
		} catch (Exception e) {
			return false;
		}
	}
}
