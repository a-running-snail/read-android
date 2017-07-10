package com.jingdong.app.reader.extension.jpush;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jingdong.app.reader.util.MZLog;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;


/**京东推送类
 * @author quda
 *
 */
public  final class JDPush {
	public static boolean isTest=false;
	/**打开推送消息
	 * @param context Activity
	 */
	public static void openPush(Context context){
		
		  JPushInterface.setDebugMode(false); 	// 设置开启日志,发布时请关闭日志
	      JPushInterface.init(context);     		// 初始化 JPush
	}
	public static void setAliasAndTags(Context context,String name,String tag){
		Set<String> tagSet=null;
		if(!isTest&&tag!=null){
			tagSet=new HashSet<String>();
			tagSet.add(tag);
		}else{
			tagSet=new HashSet<String>();
			tagSet.add("quda2013");
//			tagSet.add("jdtest");
		}
		
		JPushInterface.setAliasAndTags(context, name, tagSet,new TagAliasCallback() {
			
			@Override
			public void gotResult(int arg0, String arg1, Set<String> arg2) {
				// TODO Auto-generated method stub
				MZLog.d("jdpush", arg0+","+arg1);
				MZLog.d("jdpush", "arg2="+arg2);
			}
		});
	}
	/**关闭推送消息
	 * @param context Activity
	 */
	public static void closePush(Context context){
//		JDPushInterface.setPushEnabled(context, false);
	}
	
	   public static final String PREFS_NAME = "JPUSH_EXAMPLE";
	    public static final String PREFS_DAYS = "JPUSH_EXAMPLE_DAYS";
	    public static final String PREFS_START_TIME = "PREFS_START_TIME";
	    public static final String PREFS_END_TIME = "PREFS_END_TIME";
	    public static final String KEY_APP_KEY = "JPUSH_APPKEY";

	    public static boolean isEmpty(String s) {
	        if (null == s)
	            return true;
	        if (s.length() == 0)
	            return true;
	        if (s.trim().length() == 0)
	            return true;
	        return false;
	    }
	    
	    // 校验Tag Alias 只能是数字,英文字母和中文
	    public static boolean isValidTagAndAlias(String s) {
	        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_-]{0,}$");
	        Matcher m = p.matcher(s);
	        return m.matches();
	    }

	    // 取得AppKey
	    public static String getAppKey(Context context) {
	        Bundle metaData = null;
	        String appKey = null;
	        try {
	            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
	                    context.getPackageName(), PackageManager.GET_META_DATA);
	            if (null != ai)
	                metaData = ai.metaData;
	            if (null != metaData) {
	                appKey = metaData.getString(KEY_APP_KEY);
	                if ((null == appKey) || appKey.length() != 24) {
	                    appKey = null;
	                }
	            }
	        } catch (NameNotFoundException e) {

	        }
	        return appKey;
	    }
	    
	    // 取得版本号
	    public static String GetVersion(Context context) {
			try {
				PackageInfo manager = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0);
				return manager.versionName;
			} catch (NameNotFoundException e) {
				return "Unknown";
			}
		}
}
