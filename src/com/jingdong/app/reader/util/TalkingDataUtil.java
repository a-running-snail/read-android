package com.jingdong.app.reader.util;

import com.tendcloud.tenddata.TCAgent;

import android.content.Context;

public class TalkingDataUtil {
	private static String[] tabArray = {"Tab_书城_PV",
										"Tab_书架_PV",
										"Tab_动态V",
										"Tab_个人中心_PV"};
	private static String[] moduleArray = {"书城_子模块",
										  "书架_子模块",
										  "社区_子模块",
										  "个人中心_子模块"};
	public static final String TD_EVENT_MODULE_BOOKSTORE_ID = "书城_子模块";
	public static final String TD_EVENT_MODULE_BOOKSHELF_ID = "书架_子模块";
	public static final String TD_EVENT_MODULE_PERSONAL_CENTER_ID = "个人中心_子模块";
	public static final String TD_EVENT_MODULE_BOOK_DETAIL_ID = "图书详情_子模块";
	public static final String TD_EVENT_MODULE_SETTING = "设置";
	public static final String TD_EVENT_MODULE_READING = "阅读_子模块";
	
	public static void onBookStoreEvent(Context context,String tab,String label) {
		TCAgent.onEvent(context, TD_EVENT_MODULE_BOOKSTORE_ID, tab+"_"+label);
	}
	
	public static void onBookShelfEvent(Context context,String label) {
		TCAgent.onEvent(context, TD_EVENT_MODULE_BOOKSHELF_ID, label);
	}
	
	public static void onPersonalCenterEvent(Context context,String label) {
		TCAgent.onEvent(context, TD_EVENT_MODULE_PERSONAL_CENTER_ID, label);
	}
	
	public static void onBookDetailEvent(Context context,String label) {
		TCAgent.onEvent(context, TD_EVENT_MODULE_BOOK_DETAIL_ID, label);
	}
	
	public static void onTabEvent(Context context,int index) {
		if (index<0 || index>4) {
			return;
		}
		TCAgent.onEvent(context, moduleArray[index],tabArray[index]);
	}
	
	public static void onCommonEvent(Context context,String label) {
		TCAgent.onEvent(context, label);
	}
	
	public static void onSettingEvent(Context context,String label) {
		TCAgent.onEvent(context, TD_EVENT_MODULE_SETTING,label);
	}
	
	public static void onReadingEvent(Context context,String bookid){
		TCAgent.onEvent(context,TD_EVENT_MODULE_READING,bookid);
		
	}
	
}
