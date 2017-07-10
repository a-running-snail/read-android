package com.jingdong.app.reader.user;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.view.Surface;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.DraftsActivity.Draft;
import com.jingdong.app.reader.activity.DraftsActivity.Drafts;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.activity.ReadOverlayActivity;
import com.jingdong.app.reader.bookstore.sendbook.SendBookReceiveInfo;
import com.jingdong.app.reader.bookstore.sendbook.SendBookReceiveInfo.SendBookReceiveInfos;
import com.jingdong.app.reader.entity.extra.UsersList;
import com.jingdong.app.reader.extension.integration.SignModel;
import com.jingdong.app.reader.extension.jpush.JDMessageReceiver.JDMessage;
import com.jingdong.app.reader.extension.jpush.JDMessageReceiver.JDMessages;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.timeline.actiivity.TimelineSearchPeopleActivity.MentionPeopleModel;
import com.jingdong.app.reader.util.GsonUtils;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

public class LocalUserSetting {
	private static final String USERTOKEN = "usertoken";
	private static final String USERINFO = "userinfo";
	private static final String SINATOKEN = "sinatoken";
	private static final String SINATOKENEXPIR = "sinatokenexpir";
	private static final String SINAUID = "sinauid";
	private static final String BRIGHTNESS = "brightness";
	private static final String BRIGHTNESS_MODE = "brightness_mode";
	private static final String SYNC_BRIGHTNESS = "sync_brightness";
	private static final String TEXT_SIZE_LEVEL = "textsizelevel";
	private static final String LINE_SPACE_LEVEL = "lineSpacelevel";
	private static final String BLOCK_SPACE_LEVEL = "blockSpacelevel";
	private static final String PAGE_EDGE_SPACE_LEVEL = "pageEdgeSpacelevel";
	private static final String SOFTRENDER = "harderrender";
	private static final String OPDSADDRESSES = "opdsaddresses";
	private static final String READSTYLE = "readstyle";
	private static final String READSTYLE_NO_NIGHT = "readstyle_no_night";
	private static final String FIRST_TIME_USE = "first_time_use";
	private static final String SPLASH_SINCE_ID = "splash_since_id";
	private static final String SPLASH_SHOW_ID = "splash_show_id";
	private static final String BOOK_STYLE_LEVEL = "book_style_level";
	private static final String REGISTER_NEW_USER_FLAG = "register_new_user_flag";
	private static final String BOOKS_BAR_TIMELINE_NOTIFICATION = "timeline_notification";
	private static final String BOOKS_BAR_SELECTED_NOTIFICATION = "selected_notification";
	private static final String VOLUME_PAGE = "volume_page";
	private static final String VERTICAL_PAGE = "vertical_page";
	private static final String SCREEN_ORIENTATION = "screen_orientation";
	private static final String DISPLAY_ROTATION = "display_rotation";
	private static final String NOTIFICATION_SWITCH = "notification_switch";
	private static final String BAIDU_PUSH_BIND_USER_ID = "baidu_push_bind_user_id";
	private static final String USER_HEADER_URL = "user_header_url";
	private static final String USER_NICK_NAME = "user_nick_name";
	private static final String CHECK_UPDATE_FLAG = "check_update_flag";
	private static final String APPLICATION_RUNNING_FLAG = "application_running_flag";
	private static final String PAGE_ANIMATION = "page_animation";
	private static final String PUSH = "push";
	
	private static final String DATABASE_MIGRATE_FLAG = "database_migrate_flag";
	private static final String FILE_SHARE = "file_share";
	
	public static boolean useSoftRender = false;
	public static int readStyle = 0;
	public static boolean isRegisterNewUser = false;
	public static boolean isApplicationOpen = false;
	public static final int SCREEN_DONT_LOCK = 0;
	public static final int SCREEN_LANDSCAPE = 1;
	public static final int SCREEN_PORTRAIT = 2;
	public static final String AUTHTOKEN = "auth_token";
	public static final String MODEL = "Model";
	public static final String TYPE = "Type";
	public static final String HISTORY = "History";
	public static final String USER_HISTORY = "User_History";
	public static final String TimeLine_HISTORY = "Timeline_History";
	public static final String COMUUNITY_SEARCH_HISTORY = "Community_Search_History";
	public static final String BOOK_CART = "book_cart";
	public static final String BOOK_PATH = "book_path";
	public static final String TXT_FONT = "txt_font";
	public static final String CHINESE_TRADITIONAL = "chinese_traditional";
	
	private static final String SPLASH_SHOW_FIRST = "first_show_spalsh";
	
	private static final String IS_BOOKSHELF_GUID_SHOW = "isBookShelfGuidShow";
	private static final String IS_BOOKSTORE_GUID_SHOW = "isBookStoreGuidShow";
	private static final String IS_BORROWBOOK_GUID_SHOW = "isBorrowBookGuidShow";
	private static final String IS_BOOKSTORE_SEARCH_GUID_SHOW = "isBookStoreSearchGuidShow";
	private static final String IS_BOOKVIEW_GRAVITY_GUID_SHOW = "isBookViewGravityGuidShow";
	private static final String IS_SEND_BOOK_DIALOG_SHOW = "isSendBookDialogShow";
	
	private static final String PRE_NAME_TOKENNAME = "cpatoken";
	private static final String PRE_NAME_TOKEN = "token";
	private static final String PRE_NAME_ISCPAPUSHSUCESS = "iscpaPushSucess";
	private static final String USER_ID = "user_id";
	private static final String RECOMMEND_COUNT = "recommend_count";
	
	private static final String LATEST_MENTION_PEOPLE = "lasted_mention_people";
	
	private static final String DRAFTS_LIST = "drafts_list";
	private static final String JDMESSAGE_LIST = "jdmessages_list";//推送消息
	private static final String SENDBOOK_RECEIVE_INFOS = "sendBookReceiveInfos";//推送消息
	
	private static final String LAST_CATOLOG_POSITION = "last_catalog_position";
	private static final String SAVE_BOOK_DIR = "save_book_dir";
	private static final String SINCE_GUID = "since_guid";
	private static final String RECOMMEND_GUID = "recommend_guid";
	private static final String BG_COLOR = "bg_color";
	private static final String BG_TEXTURE = "bg_texture";
	private static final String TEXT_COLOR = "text_color";
	private static final String NIGHT_MODEL = "nigth_model";
	private static final String IGNORE_CSS_TEXT_COLOR = "ignore_css_text_color";
	private static final String IGNORE_FONT_DOWNLOAD = "ignore_font_download";
	private static final String ROCKPOSITIONX = "rockpositionx";
	private static final String ROCKPOSITIONY = "rockpositiony";
	private static final String MOVEPOINT = "movepoint";	
	private static final String TROCKPOSITIONX = "trockpositionx";
	private static final String TROCKPOSITIONY = "trockpositiony";
	private static final String TMOVEPOINT = "tmovepoint";
	
	public static Boolean saveTMovePoint(Context context, int tmovepoint){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putInt(TMOVEPOINT, tmovepoint); 
		return editor.commit(); 
	}

	public static int getTMovePoint(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getInt(TMOVEPOINT, -1);
	}
	
	public static Boolean saveTRockPositionX(Context context, int trockpositionx){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putInt(TROCKPOSITIONX, trockpositionx); 
		return editor.commit(); 
	}

	public static int getTRockPositionX(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getInt(TROCKPOSITIONX, -1);
	}
	
	public static Boolean saveTRockPositionY(Context context, int trockpositionY){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putInt(TROCKPOSITIONY, trockpositionY); 
		return editor.commit(); 
	}
	
	public static int getTRockPositionY(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getInt(TROCKPOSITIONY, -1);
	}
	
	public static Boolean saveMovePoint(Context context, int movepoint){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putInt(MOVEPOINT, movepoint); 
		return editor.commit(); 
	}

	public static int getMovePoint(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getInt(MOVEPOINT, -1);
	}
	
	public static Boolean saveRockPositionX(Context context, int rockpositionx){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putInt(ROCKPOSITIONX, rockpositionx); 
		return editor.commit(); 
	}

	public static int getRockPositionX(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getInt(ROCKPOSITIONX, -1);
	}
	
	public static Boolean saveRockPositionY(Context context, int rockpositionY){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putInt(ROCKPOSITIONY, rockpositionY); 
		return editor.commit(); 
	}

	public static int getRockPositionY(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getInt(ROCKPOSITIONY, -1);
	}
	
	public static Boolean saveIgnoreFontDownload(Context context, boolean ignore){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putBoolean(IGNORE_FONT_DOWNLOAD, ignore); 
		return editor.commit(); 
	}

	public static boolean isIgnoreFontDownload(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(IGNORE_FONT_DOWNLOAD, false);
	}
	
	public static Boolean saveReading_Night_Model(Context context, boolean nigth_model){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putBoolean(NIGHT_MODEL, nigth_model); 
		return editor.commit(); 
	}

	public static boolean getReading_Night_Model(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(NIGHT_MODEL, false);
	}
	
	public static Boolean saveIgnoreCssTextColor(Context context, boolean ignore){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putBoolean(IGNORE_CSS_TEXT_COLOR, ignore); 
		return editor.commit(); 
	}

	public static boolean isIgnoreCssTextColor(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(IGNORE_CSS_TEXT_COLOR, false);
	}
	
	public static Boolean saveReading_Background_Color(Context context, int bg_color){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putInt(BG_COLOR, bg_color); 
		return editor.commit(); 
	}

	public static int getReading_Background_Color(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getInt(BG_COLOR, 0xFFF2F2F2);
	}
	
	/**
	 * 保存阅读背景纹理，若为-1则未设置背景纹理，大于等于0则为对应位置的纹理
	 * @param context
	 * @param texture
	 * @return
	 */
	public static Boolean saveReading_Background_Texture(Context context, int texture){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putInt(BG_TEXTURE, texture); 
		return editor.commit(); 
	}

	/**
	 * 获取阅读背景纹理，若为-1则未设置背景纹理，大于等于0则为对应位置的纹理
	 * @param context
	 * @param context
	 * @return
	 */
	public static int getReading_Background_Texture(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getInt(BG_TEXTURE, -1);
	}
	
	public static Boolean saveReading_Text_Color(Context context, int text_color){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putInt(TEXT_COLOR, text_color); 
		return editor.commit(); 
	}

	public static int getReading_Text_Color(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getInt(TEXT_COLOR, ReadOverlayActivity.WHITE_STYLE_FONT);
	}
	
	public static Boolean saveCommunity_Since_Guid(Context context, String guid,String username){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(username + SINCE_GUID, guid); 
		return editor.commit(); 
	}

	public static String getCommunity_Since_Guid(Context context,String username){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(username + SINCE_GUID, "");
	}
	
	public static Boolean saveRecommend_Guid(Context context, String guid,String username){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(username + RECOMMEND_GUID, guid); 
		return editor.commit(); 
	}

	public static String getRecommend_Guid(Context context,String username){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(username + RECOMMEND_GUID, "");
	}

	public static Boolean saveBookDir(Context context, String path){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(SAVE_BOOK_DIR, path);
		return editor.commit(); 
	}
	
	public static String getSaveBookDir(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		String dir =settings.getString(SAVE_BOOK_DIR, "");
		return dir;
	}
	
	
	
	public static Boolean saveLastCatalogPosition(Context context, int position){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putInt(LAST_CATOLOG_POSITION, position); 
		return editor.commit(); 
	}

	public static int getLastCatalogPosition(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getInt(LAST_CATOLOG_POSITION, 0);
	}
	
	
	
	public static Boolean saveRecommend_count(Context context, String count,String username){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(username + RECOMMEND_COUNT, count); 
		return editor.commit(); 
	}

	public static String getRecommend_count(Context context,String username){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(username + RECOMMEND_COUNT, "0");
	}
	
	public static Boolean saveDraftsList(Context context, Drafts mo){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(DRAFTS_LIST, GsonUtils.toJson(mo));
		return editor.commit(); 
	}
	
	public static List<Draft> getDraftsList(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		String json =settings.getString(DRAFTS_LIST, null);
		
		if(TextUtils.isEmpty(json))
			return null;
		else {
			Drafts model= GsonUtils.fromJson(json, Drafts.class);
			if(model==null)
				return null;
			else {
				return model.drafts;
			}
		}
	}
	
	/**
	 * 保存推送消息
	 * @param context
	 * @param message
	 * @return
	 */
	public static Boolean saveJDMessageList(Context context, JDMessages messages){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(JDMESSAGE_LIST, GsonUtils.toJson(messages));
		return editor.commit(); 
	}
	
	/**
	 * 获取推送消息列表
	 * @param context
	 * @return
	 */
	public static List<JDMessage> getJDMessageList(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		String json =settings.getString(JDMESSAGE_LIST, null);
		
		if(TextUtils.isEmpty(json))
			return new ArrayList<JDMessage>();
		else {
			JDMessages model= GsonUtils.fromJson(json, JDMessages.class);
			if(model==null)
				return new ArrayList<JDMessage>();
			else {
				return model.messages;
			}
		}
	}
	
	/**
	 * 保存赠书赠言信息
	 * @param context
	 * @param message
	 * @return
	 */
	public static Boolean saveSendBookReceiveInfos(Context context, SendBookReceiveInfos infos){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(SENDBOOK_RECEIVE_INFOS, GsonUtils.toJson(infos));
		return editor.commit(); 
	}
	
	/**
	 * 获取赠书赠言信息列表
	 * @param context
	 * @return
	 */
	public static List<SendBookReceiveInfo> getSendBookReceiveInfos(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		String json =settings.getString(SENDBOOK_RECEIVE_INFOS, null);
		
		if(TextUtils.isEmpty(json))
			return new ArrayList<SendBookReceiveInfo>();
		else {
			SendBookReceiveInfos model= GsonUtils.fromJson(json, SendBookReceiveInfos.class);
			if(model==null)
				return new ArrayList<SendBookReceiveInfo>();
			else {
				if(model.infos == null)
					return new ArrayList<SendBookReceiveInfo>();
				return model.infos;
			}
		}
	}
	
	
	public static Boolean saveLastedMentionPeople(Context context, MentionPeopleModel mo){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(LATEST_MENTION_PEOPLE, GsonUtils.toJson(mo));
		return editor.commit(); 
	}
	
	public static List<UsersList> getLastedMentionPeople(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		String json =settings.getString(LATEST_MENTION_PEOPLE, null);
		
		if(TextUtils.isEmpty(json))
			return null;
		else {
			MentionPeopleModel model= GsonUtils.fromJson(json, MentionPeopleModel.class);
			if(model==null)
				return null;
			else {
				return model.list;
			}
		}
	}
	
	public static Boolean saveUser_id(Context context, String user_id){
		if(TextUtils.isEmpty(user_id) || null == context) {
			return false;
		}
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    if (null == settings) {
	    	return false;
	    }
		Editor editor = settings.edit();
		if (null == editor) {
	    	return false;
	    }
        editor.putString(USER_ID, user_id); 
		return editor.commit(); 
	}
	
	public static String getUser_id(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(USER_ID, null);
	}
	
	public static Boolean saveBookStoreSearchGuidShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putBoolean(IS_BOOKSTORE_SEARCH_GUID_SHOW, true); 
		return editor.commit(); 
	}
	
	public static boolean isBookStoreSearchGuidShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(IS_BOOKSTORE_SEARCH_GUID_SHOW, false);
	}
	public static Boolean saveBookStoreGuidShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putBoolean(IS_BOOKSTORE_GUID_SHOW, true); 
		return editor.commit(); 
	}
	
	public static boolean isBookStoreGuidShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(IS_BOOKSTORE_GUID_SHOW, false);
	}
	
	/**
	 * 设置借阅新手引导记录为true
	 * @param context
	 * @return
	 */
	public static Boolean saveBorrowBookGuidShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putBoolean(IS_BORROWBOOK_GUID_SHOW, true); 
		return editor.commit(); 
	}
	/**
	 * 是否显示过借阅新手引导页
	 * @param context
	 * @return
	 */
	public static boolean isBorrowBookGuidShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(IS_BORROWBOOK_GUID_SHOW, false);
	}
	
	/**
	 * 保存好评送书dialog已显示过,保存一次增加一次计数
	 * @param context
	 * @return
	 */
	public static Boolean saveSendBookDialogShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		int current = settings.getInt(IS_SEND_BOOK_DIALOG_SHOW, 0);
	    Editor editor = settings.edit();
        editor.putInt(IS_SEND_BOOK_DIALOG_SHOW, current+1); 
		return editor.commit(); 
	}
	
	/**
	 * 获取是否显示过好评送书dialog，三次以上不显示dialog
	 * @param context
	 * @return
	 */
	public static boolean isSendBookDialogShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getInt(IS_SEND_BOOK_DIALOG_SHOW, 0) < 3;
	}
	
	/**
	 * 保存重力感应旋转提示已经展示
	 * @param context
	 * @return
	 */
	public static Boolean saveBookViewGravityGuidShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putBoolean(IS_BOOKVIEW_GRAVITY_GUID_SHOW, true); 
		return editor.commit(); 
	}
	/**
	 * 判断重力感应旋转是否展示过
	 * @param context
	 * @return
	 */
	public static boolean isBookViewGravityGuidShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(IS_BOOKVIEW_GRAVITY_GUID_SHOW, false);
	}
	
	public static Boolean saveBookShelfGuidShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putBoolean(IS_BOOKSHELF_GUID_SHOW, true); 
		return editor.commit(); 
	}
	public static Boolean saveBookShelfGuidShow(Context context,boolean flag){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putBoolean(IS_BOOKSHELF_GUID_SHOW, flag); 
		return editor.commit(); 
	}
	
	public static boolean isBookShelfGuidShow(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(IS_BOOKSHELF_GUID_SHOW, false);
	}
	
	
	public static Boolean saveFirstShowSplash(Context context, boolean isfirst){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putBoolean(SPLASH_SHOW_FIRST, isfirst); 
        saveFirstShowSplashTime(editor);
		return editor.commit(); 
	}
	
	public static boolean getFirstShowSplash(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(SPLASH_SHOW_FIRST, true);//第一次启动应用
	}
	
	private static void saveFirstShowSplashTime( Editor editor){
		editor.putLong("firsttime", System.currentTimeMillis());
	}
	public static long getFirstShowFirstShowSplashTime(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getLong("firsttime", System.currentTimeMillis());
	}
	public static Boolean saveBookPath(Context context, String bookpath){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(BOOK_PATH, bookpath); 
		return editor.commit(); 
	}
	
	public static String getBookPath(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(BOOK_PATH, null);
	}
	
	public static Boolean saveTxtFontPath(Context context, String txtfont){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(TXT_FONT, txtfont); 
		return editor.commit(); 
	}
	
	public static String getTextFontPath(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(TXT_FONT, null);
	}
	
	public static Boolean saveBookCart(Context context,String userPin, String bookcart){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(BOOK_CART+"_"+userPin, bookcart); 
		return editor.commit(); 
	}
	
	public static Boolean saveToBookCart(Context context, String bookcart){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putString(BOOK_CART, bookcart); 
		return editor.commit(); 
	}
	
	public static String getBookCart(Context context,String userPin){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(BOOK_CART+"_"+userPin, "{}");
	}
	
	public static String getBookCartInfos(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(BOOK_CART, "{}");
	}
	
	public static boolean removeNullPinBookCart(Context context) {
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		 Editor editor = settings.edit();
		 editor.remove(BOOK_CART+"_");
		 return editor.commit();
	}
	
	public static boolean clearBookCart(Context context) {
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.remove(BOOK_CART);
		return editor.commit();
	}
	
	
	public static Boolean saveHistory(Context context, String history){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
//	    if (LoginUser.isLogin()) {
//	    	editor.putString(HISTORY+"_"+LoginUser.getpin(), history); 
//		}else{
//			editor.putString(HISTORY, history);
//		}
	    editor.putString(HISTORY, history);
		return editor.commit(); 
	}
	
	
	public static String getHistory(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
//		if (LoginUser.isLogin()) {
//			return settings.getString(HISTORY+"_"+LoginUser.getpin(), "");
//		}else{
//			return settings.getString(HISTORY, "");
//		}
		return settings.getString(HISTORY, "");
	}
	
	
	/**
	 * 保存社区搜索历史
	 * @param context
	 * @param history
	 * @return
	 */
	public static Boolean saveCommunitySearchHistory(Context context, String history){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
	    editor.putString(COMUUNITY_SEARCH_HISTORY, history);
		return editor.commit(); 
	}
	
	/**
	 * 获取社区搜索历史
	 * @param context
	 * @return
	 */
	public static String getCommunitySearchHistory(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(COMUUNITY_SEARCH_HISTORY, "");
	}
	
	
	public static Boolean saveUserHistory(Context context, String history){
		if (null == context || TextUtils.isEmpty(history)) {
			return false;
		}
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    if (null == settings) {
	    	return false;
	    }
	    Editor editor = settings.edit();
		if (null == editor) {
	    	return false;
	    }
        editor.putString(USER_HISTORY, history); 
		return editor.commit(); 
	}
	
	public static String getUserHistory(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(USER_HISTORY, "");
	}
	
	public static Boolean saveTimelineHistory(Context context, String history){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(TimeLine_HISTORY, history); 
		return editor.commit(); 
	}
	
	public static String getTimelineHistory(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(TimeLine_HISTORY, "");
	}
	
	private static final String LOGIN_SCAN = "login_scan";
	private static final String SIGN_DATE = "sign_date";
	private static final String SIGN_TIMER = "sign_timer";
	private static final String IS_SIGNED = "is_signed";
	private static final String IS_SHOWN_3MIN = "is_shown_3min";
	private static final String USERPIN = "userpin";
	
	
	public static void saveLoginScan(Context context, boolean isscan){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(LOGIN_SCAN, isscan);
		editor.commit();
	}
	
	public static boolean getLoginSacn(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getBoolean(LOGIN_SCAN, false);
	}
	
	public static Boolean saveFeedBackType(Context context, String type){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(TYPE, type); 
		return editor.commit(); 
	}
	
	public static String getFeedBackType(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(TYPE, "");
	}
	
	
	public static Boolean saveBookShelfModel(Context context, String model){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
        editor.putString(MODEL, model); 
		return editor.commit(); 
	}
	
	public static String getBookShelfModel(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(MODEL, "");
	}
	
	public static void saveApplicationOpenFlag(Context context, boolean isApplicationOpen){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(APPLICATION_RUNNING_FLAG, isApplicationOpen);
		editor.commit();
	}
	
	public static boolean getApplicationOpenFlag(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getBoolean(APPLICATION_RUNNING_FLAG, false);
	}

	public static void saveDatabaseMigrateFlag(Context context, boolean flag){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(DATABASE_MIGRATE_FLAG, flag);
		editor.commit();
	}
	
	public static boolean getDatabaseMigrateFlag(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getBoolean(DATABASE_MIGRATE_FLAG, false);
	}

	
	public static void saveCheckUpdateFlag(Context context, int version){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(CHECK_UPDATE_FLAG, version);
		editor.commit();
	}
	
	public static int getCheckUpdateFlag(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getInt(CHECK_UPDATE_FLAG, -1);
	}

	public static void saveRegisterNewUserFlag(Context context, boolean isRegisterNewUser){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(REGISTER_NEW_USER_FLAG, isRegisterNewUser);
		editor.commit();
	}
	
	public static boolean getRegisterNewUserFlag(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getBoolean(REGISTER_NEW_USER_FLAG, false);
	}
	

	public static void saveBookStyleLevel(Context context, String style){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(BOOK_STYLE_LEVEL, style);
		editor.commit();
	}
	
	public static String getBookStyleLevel(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getString(BOOK_STYLE_LEVEL, null);
	}
	
	public static void saveSplashShowId(Context context ,long id){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(SPLASH_SHOW_ID, id);
		editor.commit();
	}
	
	public static long getSplashShowId(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		long sinceid = settings.getLong(SPLASH_SHOW_ID, -1);
		return sinceid;
	}
	
	
	public static void saveSplashSinceId(Context context ,long id){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(SPLASH_SINCE_ID, id);
		editor.commit();
	}
	public static void saveFirstVistBookStore(Context context ,boolean flag){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("firstvistbookstore", flag);
		editor.commit();
	}
	public static  boolean getFirstVistBookStore(Context context){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		
		return settings.getBoolean("firstvistbookstore", true);
	}
	public static void saveBookshelfScanFlag(Context context,String userid,boolean state){
		if(null == context) {
			return;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(userid+"", state);
		editor.commit();
	}
	
	public static boolean getBookshelfScanFlag(Context context,String userid){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		boolean state= settings.getBoolean(userid+"", false);
		return state;
	}
	public static void saveFirstScanBookcase(Context context,boolean state){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("isFirstScanBookcase", state);
		editor.commit();
	}
	
	public static boolean isFirstScanBookcase(Context context){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		boolean state= settings.getBoolean("isFirstScanBookcase", true);
		return state;
	}
	
	public static long getSplashSinceId(Context context){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
		long sinceid = settings.getLong(SPLASH_SINCE_ID, -1);
		return sinceid;
	}
	

	public static void savaTimelineNotification(Context context,int key) {
		if(null == context) {
			return;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(BOOKS_BAR_TIMELINE_NOTIFICATION, key);
		editor.commit();
	}
	public static void savaBarsStateNotification(Context context,String key,int value) {
		if(null == context) {
			return;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public static void removeBarsStateNotification(Context context,String key) {

		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(key);
		editor.commit();
	}

	public static int getBarsStateNotification(Context context,String key) {

		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		int noti = settings.getInt(key, 1);
		return noti;
	}
	
	
	public static void savaSelectedNotification(Context context,int key) {
		if(null == context) {
			return;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(BOOKS_BAR_SELECTED_NOTIFICATION, key);
		editor.commit();
	}

	public static int getSelectedNotification(Context context) {

		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		int noti = settings.getInt(BOOKS_BAR_SELECTED_NOTIFICATION, 0);
		return noti;
	}

	public static int getTimelineNotification(Context context) {

		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		int noti = settings.getInt(BOOKS_BAR_TIMELINE_NOTIFICATION, 0);
		return noti;
	}

	public static String getUserId(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		String userInfoText = settings.getString(USERINFO, "");
		if (TextUtils.isEmpty(userInfoText)) {
			return null;
		}
		UserInfo userInfo = new UserInfo(userInfoText);
		return  userInfo.id;
	}

	public static String getUserName(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		String userInfoText = settings.getString(USERINFO, "");
		if (TextUtils.isEmpty(userInfoText)) {
			return "";
		}
		UserInfo userInfo = new UserInfo(userInfoText);
		return userInfo.name;
	}
	
	public static UserInfo getUserInfo(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		String userInfoText = settings.getString(USERINFO, "");
		if (TextUtils.isEmpty(userInfoText)) {
			return null;
		}
		return new UserInfo(userInfoText);
	}

	public static boolean isRegisterFromThirdParty(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		String userInfoText = settings.getString(USERINFO, "");
		if (TextUtils.isEmpty(userInfoText))
			return false;
		UserInfo userInfo = new UserInfo(userInfoText);
		return userInfo.isRegisterFromThirdParty();
	}

	public static String getToken(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getString(USERTOKEN, "");
	}

	public static void saveTokenAndUserInfo(Context context, String token,
			String userInfo) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(USERTOKEN, token);
		editor.putString(USERINFO, userInfo);
		editor.commit();
	}

	public static void clearUserInfo(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(USERINFO, "");
		editor.putString(USERTOKEN, "");
		editor.putString(SINATOKEN, "");
		editor.putLong(SINATOKENEXPIR, 0);
		editor.commit();
		Notification.getInstance().clear();
		clearCookie(context);
	}

	public static void clearSina(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(SINATOKEN, "");
		editor.putLong(SINATOKENEXPIR, 0);
		editor.putString(SINAUID, "");
		editor.commit();
		clearCookie(context);
	}

	public static void saveUserInfo(Context context, String userInfo) {
		if (null == context || TextUtils.isEmpty(userInfo)) {
			return;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		if (null == settings) {
	    	return;
	    }
		SharedPreferences.Editor editor = settings.edit();
		if (null == editor) {
	    	return;
	    }
		editor.putString(USERINFO, userInfo);
		editor.commit();
	}

	public static void saveSinaAccessToken(Context context,
			Oauth2AccessToken sinaToken) {
		if (null == context || null == sinaToken) {
			return;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		if (null == settings) {
	    	return;
	    }
		SharedPreferences.Editor editor = settings.edit();
		if (null == editor) {
	    	return;
	    }
		editor.putString(SINAUID, sinaToken.getUid());
		editor.putString(SINATOKEN, sinaToken.getToken());
		editor.putLong(SINATOKENEXPIR, sinaToken.getExpiresTime());
		editor.commit();
	}

	public static Oauth2AccessToken getSinaAccessToken(Context context) {
		Oauth2AccessToken token = new Oauth2AccessToken();
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		String uid = settings.getString(SINAUID, "");
		String tokenText = settings.getString(SINATOKEN, "");
		long expiresTime = settings.getLong(SINATOKENEXPIR, 0);
		token.setUid(uid);
		token.setToken(tokenText);
		token.setExpiresTime(expiresTime);
		return token;
	}

	public static String getSinaUID(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getString(SINAUID, "");
	}

	public static void saveReadBrightness(Context context, float brightness) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat(BRIGHTNESS, brightness);
		editor.commit();
	}

	public static float getReadBrightness(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getFloat(BRIGHTNESS, 0.6f);
	}
    
    public static void saveReadBrightnessMode(Context context, int mode) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(BRIGHTNESS_MODE, mode);
		editor.commit();
	}
    
	public static int getReadBrightnessMode(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getInt(BRIGHTNESS_MODE, -1);
	}
	
	public static void saveSyncBrightness(Context context, boolean isSync) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(SYNC_BRIGHTNESS, isSync);
		editor.commit();
	}
	
	public static boolean isSyncBrightness(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getBoolean(SYNC_BRIGHTNESS, true);
	}

	public static void saveOpdsAddresses(Context context, String opds) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(OPDSADDRESSES, opds);
		editor.commit();
	}

	public static String getOpdsAddresses(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getString(OPDSADDRESSES, "");
	}

	public static void saveTextSizeLevel(Context context, int level) {
		if (level < 0) {
			level = 0;
		}
		if (level > 9) {
			level = 9;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(TEXT_SIZE_LEVEL, level);
		editor.commit();
	}
	
	public static void saveLineSpaceLevel(Context context, int level) {
		if (level < 0) {
			level = 0;
		}
		if (level > 4) {
			level = 4;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(LINE_SPACE_LEVEL, level);
		editor.commit();
	}
	
	public static void saveBlockSpaceLevel(Context context, int level) {
		if (level < 0) {
			level = 0;
		}
		if (level > 4) {
			level = 4;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(BLOCK_SPACE_LEVEL, level);
		editor.commit();
	}
	
	public static void savePageEdgeSpaceLevel(Context context, int level) {
		if (level < 0) {
			level = 0;
		}
		if (level > 2) {
			level = 2;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PAGE_EDGE_SPACE_LEVEL, level);
		editor.commit();
	}

	public static int getReadStyle(Context context) {
		// 为了增加用户自定义阅读背景和字体颜色，因此样式固定是白色的
		// SharedPreferences settings = context.getSharedPreferences(
		// MZBookApplication.PREFERENCES_NAME, 0);
		// LocalUserSetting.readStyle = settings.getInt(READSTYLE, 0);
		// return LocalUserSetting.readStyle;
		return ReadOverlayActivity.READ_STYLE_WHITE;
	}

	public static void saveReadStyle(Context context, int style) {
		// 为了增加用户自定义阅读背景和字体颜色，因此样式固定是白色的
		// if (style < 0) {
		// style = 0;
		// }
		// if (style > 3) {
		// style = 3;
		// }
		// SharedPreferences settings = context.getSharedPreferences(
		// MZBookApplication.PREFERENCES_NAME, 0);
		// SharedPreferences.Editor editor = settings.edit();
		// editor.putInt(READSTYLE, style);
		// editor.commit();
		// LocalUserSetting.readStyle = style;
		LocalUserSetting.readStyle = ReadOverlayActivity.READ_STYLE_WHITE;
	}
	
	public static void saveStyleNoNight(Context context, int style) {
		if (style < 0) {
			style = 0;
		}
		if (style > 2) {
			style = 2;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(READSTYLE_NO_NIGHT, style);
		editor.commit();
	}
	
	public static int getReadStyleNoNight(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getInt(READSTYLE_NO_NIGHT, 0);
	}
	
	public static boolean isFirstTimeUse(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		boolean isFirstTimeUse = settings.getBoolean(FIRST_TIME_USE, true);
		if (isFirstTimeUse) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(FIRST_TIME_USE, false);
			editor.commit();
		}
		return isFirstTimeUse;
	}

    public static int getTextSizeLevel(Context context) {
        SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
        return settings.getInt(TEXT_SIZE_LEVEL, context.getResources().getInteger(R.integer.default_textsize_level));
    }
    
    public static int getLineSpaceLevel(Context context) {
        SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
        return settings.getInt(LINE_SPACE_LEVEL, context.getResources().getInteger(R.integer.default_linespace_level));
    }
    
    public static int getBlockSpaceLevel(Context context) {
        SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
        return settings.getInt(BLOCK_SPACE_LEVEL, context.getResources().getInteger(R.integer.default_blockspace_level));
    }
    
    public static int getPageEdgeSpaceLevel(Context context) {
        SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, 0);
        return settings.getInt(PAGE_EDGE_SPACE_LEVEL, context.getResources().getInteger(R.integer.default_pageedgespace_level));
    }

	public static boolean useSoftRender(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		LocalUserSetting.useSoftRender = settings.getBoolean(SOFTRENDER, true);
		return LocalUserSetting.useSoftRender;
	}

	public static void saveSoftRender(Context context, boolean enable) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(SOFTRENDER, enable);
		editor.commit();
		LocalUserSetting.useSoftRender = enable;
	}

	public static void saveVolumePage(Context context,boolean enable){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(VOLUME_PAGE, enable);
		editor.commit();
	}
	
	public static boolean useVolumePage(Context context){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getBoolean(VOLUME_PAGE, true);
	}
	
	public static void savePushPage(Context context,boolean enable){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PUSH, enable);
		editor.commit();
	}
	
	public static boolean getPushPage(Context context){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getBoolean(PUSH, false);
	}
	
	public static void saveVerticalPage(Context context,boolean enable){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(VERTICAL_PAGE, enable);
		editor.commit();
	}
	
	public static boolean useVerticalPage(Context context){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getBoolean(VERTICAL_PAGE, false);
	}
	
	public static void saveLockScreenOrientation(Context context, int orientation){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(SCREEN_ORIENTATION, orientation);
		editor.commit();
	}
	
	public static int lockScreenOrientation(Context context){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getInt(SCREEN_ORIENTATION, SCREEN_DONT_LOCK);
	}
	
	public static void saveDisplayRotation(Context context, int rotation){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(DISPLAY_ROTATION, rotation);
		editor.commit();
	}
	
	public static int getDisplayRotation(Context context){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getInt(DISPLAY_ROTATION, Surface.ROTATION_0);
	}
	
	public static void saveNotificationSwitch(Context context, boolean enable) {
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(NOTIFICATION_SWITCH, enable);
		editor.commit();
	}
	
	public static boolean isNotificationSwitchOpen(Context context){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getBoolean(NOTIFICATION_SWITCH, true);
	}
	
	private static void clearCookie(Context context) {
		@SuppressWarnings("unused")
		CookieSyncManager cookieSyncManager = CookieSyncManager
				.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
	}
	
	public static String getBindUserId(Context context) {
		SharedPreferences sp = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return sp.getString(BAIDU_PUSH_BIND_USER_ID, null);
	}

	public static void setBindUserId(Context context, String baiduPushUserId) {
		if(null == context || TextUtils.isEmpty(baiduPushUserId)) {
			return;
		}
		SharedPreferences sp = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(BAIDU_PUSH_BIND_USER_ID, baiduPushUserId);
		editor.commit();
	}
	public static void saveUserHeaderUrl(Context context, String url) {
		if(null == context || TextUtils.isEmpty(url)) {
			return;
		}
		SharedPreferences sp = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		if (null == sp) {
	    	return;
	    }
		SharedPreferences.Editor editor = sp.edit();
		if (null == editor) {
	    	return;
	    }
		editor.putString(USER_HEADER_URL, url);
		editor.commit();
	}
	
	public static void saveUserNickName(Context context, String nickname) {
		if(null == context || TextUtils.isEmpty(nickname)) {
			return;
		}
		SharedPreferences sp = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		if (null == sp) {
	    	return;
	    }
		SharedPreferences.Editor editor = sp.edit();
		if (null == editor) {
	    	return;
	    }
		editor.putString(USER_NICK_NAME, nickname);
		editor.commit();
	}
	
	public static String getUserHeaderUrl() {
		SharedPreferences pref = MZBookApplication.getInstance().getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
    	return pref.getString(USER_HEADER_URL, "");
	}
	public static String getUserNickName() {
		SharedPreferences pref = MZBookApplication.getInstance().getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return pref.getString(USER_NICK_NAME, "");
	}
	
	
    public static String getString(String name){
    	SharedPreferences pref = MZBookApplication.getInstance().getSharedPreferences(FILE_SHARE, Context.MODE_PRIVATE);
    	return pref.getString(name, "");
    }
    
    public static void savecpaPushState(String token, boolean iscpaPushSucess) {
		SharedPreferences sp = MZBookApplication.getInstance().getSharedPreferences(
				PRE_NAME_TOKENNAME, Context.MODE_PRIVATE);
		Editor ed = sp.edit();
		ed.clear();
		ed.putString(PRE_NAME_TOKEN, token);
		ed.putBoolean(PRE_NAME_ISCPAPUSHSUCESS, iscpaPushSucess);
		ed.commit();
	}
    
    public static void saveSignModel(Context context,SignModel model) {
    	if(null == context) {
			return;
		}
    	SharedPreferences sp = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(SIGN_DATE, model.getDate());
		editor.putBoolean(IS_SIGNED, model.isSigned());
		editor.putBoolean(IS_SHOWN_3MIN, model.isShown3min());
		editor.putString(USERPIN, model.getUserpin());
		editor.commit();
	}
    
    public static SignModel getSignModel(Context context) {
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		String date = settings.getString(SIGN_DATE, "");
		boolean isSign = settings.getBoolean(IS_SIGNED, false);
		boolean isShow3min = settings.getBoolean(IS_SHOWN_3MIN, false);
		String userpin = settings.getString(USERPIN, "");
		return new SignModel(date,isSign,isShow3min,userpin);
    }
    
    public static void saveTraditional(Context context, boolean isTraditional) {
    	if(null == context) {
			return;
		}
    	SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(CHINESE_TRADITIONAL, isTraditional);
		editor.commit();
    }
    
    public static boolean isTraditional(Context context) {
    	SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(CHINESE_TRADITIONAL, false);
    }
    
	public static void savePageAnimation(Context context, int animation){
		if(null == context) {
			return;
		}
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PAGE_ANIMATION, animation);
		editor.commit();
	}
	
	public static int getPageAnimation(Context context){
		SharedPreferences settings = context.getSharedPreferences(
				MZBookApplication.PREFERENCES_NAME, 0);
		return settings.getInt(PAGE_ANIMATION, 0);
	}

	/**
	 * 根据key保存字符串
	 * @param context
	 * @param key
	 * @param history
	 * @return
	 */
	public static Boolean saveStringValueByKey(Context context, String key,String str){
		if(null == context || TextUtils.isEmpty(key) || TextUtils.isEmpty(str)) {
			return false;
		}
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    Editor editor = settings.edit();
	    editor.putString(key, str);
		return editor.commit(); 
	}
	
	/**
	 * 根据key获取字符串
	 * @param context
	 * @param key
	 * @return
	 */
	public static String getStringValueByKey(Context context,String key){
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getString(key, "");
	}
	
	public static void saveIsSchoolBaiTiaoUser(Context context, boolean isSchoolBaiTiaoUser) {
    	if(null == context) {
			return;
		}
    	SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("IsSchBTU", isSchoolBaiTiaoUser);
		editor.commit();
    }
	
	public static boolean getIsSchoolBaiTiaoUser(Context context){
		if(null == context) {
			return false;
		}
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean("IsSchBTU", false);
	}
	
	
	public static void saveUpdateFontUrl(Context context) {
    	if(null == context) {
			return;
		}
    	SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("UpdateFontUrl", true);
		editor.commit();
    }
	
	public static boolean getUpdateFontUrl(Context context){
		if(null == context) {
			return false;
		}
		SharedPreferences settings = context.getSharedPreferences(MZBookApplication.PREFERENCES_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean("UpdateFontUrl", false);
	}
    
}
