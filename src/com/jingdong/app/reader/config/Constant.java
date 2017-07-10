/*
 * Copyright (C) 2010 Aspire
 * 
 * MIDConst.java Version 1.0
 *
 */
package com.jingdong.app.reader.config;

/**
 * 常量类
 * @author zhangjunpinng
 *
 * 2010-9-12 上午10:39:51
 *  
 * NetworkConstant
 *
 */
public class Constant {
	public final static String JD_SHARE_PREFERENCE = "jdAndroidClient";//默认shared prefernce文件名
	public final static String LASTE_MESSAGE_READ_TIME = "lasteMessageReadeTime";//站内消息最后阅读时间
	public final static String SERVICE_TO_ACTIVIATE_MESSAGE ="message";//从service传值到Acitivate的msgId;
	public final static String POST_TO_CONFIRM_SUCCESS_FLAG ="post_order_confrim_flag";
	public final static String ADD_SHORT_CUT_FLAG ="add_short_cut_flag";//标识是否第一次启动
	public final static String JD_WIDGET_DELETED_FLAG ="jd_widget_deleted";//桌面上是否还widget,用来确定是否要结束掉service进程
	
	//文件夹得名字
	public static final String DIR_ROOT_NAME = "jdreader";
	public static final String DIR_TEMP_NAME = "temp";
	public static final String FILE_TEMP_NAME_BOOKSYN = "booksyn";
	public final static String FILE_SHARE= "share.lag";
	public final static String FILE_RANDOM= "random.lag";
	public final static String SHARE_SHOW_NOTICE = "showNotice";
	public final static String DIR_IMAGE_NAME = "image";
	public final static String IMAGE_SUFFI =".image";
	public final static String EPUB_SUFFI =".jeb";
	public final static String MEIDA_SUFFI =".jeb";
	
	public static final int LOG_IN = 1;// 登录状态
	public static final int LOG_OFF = 0;// 登出状态
	public static boolean hasLogIn = false;
	public static final String LOGIN_FLAG = "login";
	public static final String REMEMBER_NAME = "uuunnn";
	public static final String REMEMBER_PASSWORD = "pppwww";
	public static final String REMEMBER_PIN = "ppp";
	public static final String REMEMBER_FLAG = "remember";
	public static final String AUTO_LOGIN = "autologin";
	public static final String LOGIN_STATE = "login_state";
	public static final String LOGIN_COOKIE = "login_cookie";
	
	//用于存取是否显示赠书对话框
	public final static String SHOW_PRESENT_BOOK_DIALOG = "presentBookDialog";
	public final static String REQUEST_BIND_PRESENT_BOOK = "requestBindPresentBook";
	//用于保存是否已经绑定月畅读卡。（用于推广）
	public final static String REQUEST_BIND_ONLINE_CARD = "requestBindOnlineCard";
	//用于保存是否已经绑定季畅读卡。（用于推广）
	public final static String REQUEST_BIND_SENSON_CARD = "requestBindSeasonCard";
	//用于显示新手引导页面
	public final static String SHOW_GUIDE_PAGE = "guidePage";
	
	public final static String SHOW_GUIDE_LOCAL_PAGE="localGuidePage";
	public final static String SHOW_GUIDE_READBOOK_PAGE="readBookGuidePage";
	//用于保持当前时间
	public final static String DOWNLOAD_PIC_TIME = "downloadPicPreTime";
	//pdf版本号
	public final static String SHARE_PDF_SO_VERSION = "pdfVersion";
	//退出程序保存多余图片的张数
	public final static int NUM_IMAGE =60;//保存最新图片60张
	public final static int NUM_TEMP =10;//保存最新temp数据10
	public final static long CHECK_INTERVAL_DEFAULT =60*60*1000;
	
	public static String REN_MIN_BI = "¥";// 人民币符号
}
