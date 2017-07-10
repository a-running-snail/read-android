package com.jingdong.app.reader.util;

import com.jingdong.app.reader.application.MZBookApplication;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * @author xuhongwei1
 */
public class SettingUtils {
	private SharedPreferences.Editor editor=null;
	private SharedPreferences preferences=null;
	private volatile static SettingUtils instance=null;
	
	public static SettingUtils getInstance() { 
		if (null == instance){
			synchronized (SettingUtils.class) { 
				if (null == instance){
					instance = new SettingUtils();
				}
			}
		}
		return instance;  
	}
	
	public SettingUtils(){
		preferences = MZBookApplication.getInstance().getSharedPreferences("settings", Activity.MODE_PRIVATE);
		editor = preferences.edit();
	}
	
	public void putString(String key, String value){
		editor.putString(key, value); 
		editor.commit();
	}
	
	public String getString(String key, String value){
		return preferences.getString(key, value);
	}
	
	public String getString(String key){
		return preferences.getString(key, "");
	}
	
	public void putBoolean(String key, boolean value){
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public boolean getBoolean(String key){
		return preferences.getBoolean(key, false);
	}
	
	public boolean getBoolean(String key, boolean value){
		return preferences.getBoolean(key, value);
	}
	
	public void putInt(String key, int value){
		editor.putInt(key, value);
		editor.commit();
	}
	
	public int getInt(String key){
		return preferences.getInt(key, -1);
	}
	
	public int getInt(String key, int value){
		return preferences.getInt(key, value);
	}
	
	public void putLong(String key, long value){
		editor.putLong(key, value);
		editor.commit();
	}
	
	public long getLong(String key){
		return preferences.getLong(key, -1);
	}
	
	public long getLong(String key, int value){
		return preferences.getLong(key, value);
	}
	
}
