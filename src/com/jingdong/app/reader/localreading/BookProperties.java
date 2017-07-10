package com.jingdong.app.reader.localreading;

import java.io.File;
import java.util.ArrayList;

import com.jingdong.app.reader.util.DPIUtil;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;

/*
 * is direct Reference form here a right way?
 */
public class BookProperties {

	// //////Port form BookSettingsActivity start//////////////

	public static final String KEY_THEME = "preferences_theme";
	public static final String KEY_TURNING_MODE = "preferences_turning_modev1";
	public static final String KEY_PDF_TURNING_MODE = "preferences_pdf_turning_modev1";// pdf
																						// 翻页模式，水平&垂直
	public static final String KEY_PDF_SYSBAR = "preferences_pdf_sysbarv1";
	public static final String KEY_PDF_SCREEN_ORITATION = "preferences__pdf_screen_orientationv1";
	public static final String KEY_FONT_SIZE = "preferences_font_size";
	public static final String KEY_ANIMATION_SHOW = "preferences_animation_show";

	public static final String KEY_PREPARE_VERSION_COMPLETED = "preferences_prepare_version_completed";

	public static final String KEY_CUSTOMIZE_BOOKMARK_NAME_REMEMBER_CHOICE = "preferences_customized_remember_choice";
	public static final String KEY_CUSTOMIZE_BOOKMARK_NAME_CHOICE = "preferences_customized_choice";

	public static final String KEY_CUSTOMIZE_BACKGROUND = "preferences_customized_background";

	public static final String KEY_VOLUME_ENABLE = "preferences_volume_enable";

	public static final String KEY_BRI_PERCENT = "preferences_bri_percent";

	public static final String KEY_TIMEOUT_LEVEL = "preferences_timeout_level";// 待机时间
	public static final String KEY_PDF_TIMEOUT_LEVEL = "preferences_timeout_p_v1";// 待机时间
	public static final String KEY_LINE_SPACING = "preferences_line_spacing";
	public static final String KEY_PDF_ZOOM = "preferences_pdf_zoom";
	// ///////////Port form BookSettingsActivity end////////
	/***
	 * pdf setting start
	 * **/
	public static int BOOK_PDF_TURNING_MODE = 0;
	public static int BOOK_PDF_SCREEN_ORITATION = 0;
	public static final int BOOK_PDF_TURNING_MODE_H = 0;
	public static final int BOOK_PDF_TURNING_MODE_V = 1;
	public static int BOOK_PDF_FONTSIZE = 1;
	public static final int BOOK_PDF_FONTSIZE_MIN = 1;
	public static final int BOOK_PDF_FONTSIZE_MAX = 5;
	/***
	 * pdf setting end
	 * **/
	public static boolean shouldLoad = false;
	public static boolean pdf_SysBar = false;
	public static boolean isFirstLuanch = false;
	public static int displayWidth;
	public static int displayHeight;
	public static int opWidth;
	public static int opHeight;

	public static int CONTENT_WIDTH;
	public static int CONTENT_HEIGHT;

	public static int FOLD_WIDTH;

	public static float MARGIN_RATIO;

	public static int BOOK_MARGIN_BOTTOM;
	public static int BOOK_MARGIN_BOTTOM_0;
	public static int BOOK_MARGIN_LEFT;
	public static int BOOK_MARGIN_LEFT_0;
	public static int BOOK_MARGIN_RIGHT;
	public static int BOOK_MARGIN_RIGHT_0;
	public static int BOOK_MARGIN_TOP;
	public static int BOOK_MARGIN_TOP_0;
	public static int BOOK_TITLE_MARGIN_RIGHT;
	public static int BOOK_DIVIDER_MARGIN_TOP;

	// epub设置所用
	public static final String KEY_EPUB_TURNING_MODE = "preferences_epub_turning_mode";
	public static final String KEY_EPUB_FONT_SIZE = "preferences_epub_font_size";
	public static final String KEY_EPUB_VOLUME_ENABLE = "preferences_epub_volume_enable";
	// public static final String KEY_EPUB_BRI_PERCENT =
	// "preferences_epub_bri_percent";
	public static final String KEY_EPUB_TIMEOUT_LEVEL = "preferences_epub_timeout_level";// 待机时间
	public static final String KEY_EPUB_LINE_SPACING = "preferences_epub_line_spacing";
	public static final String KEY_EPUB_ORIENTATION = "preferences_epub_screen_orientation";
	// public static final String CUSTOMIZED_BACKGROUND = "customized.png";

	public final static String KEY_EPUB_THEME_MODE = "preferences_epub_theme_mode";
	public final static String KEY_EPUB_THEME_MODE_NUM = "preferences_epub_theme_mode_num";
	public final static String KEY_EPUB_THEME_BG_PICKER = "preferences_epub_bg_color_picker";
	public final static String KEY_EPUB_THEME_FONT_PICKER = "preferences_epub_font_color_picker";
	public static final String KEY_EPUB_THEME_BG_PICKER_L = "preferences_epub_bg_color_picker_left";// 颜色选择器中背景选择的左侧按钮。
	public static final String KEY_EPUB_THEME_BG_PICKER_R = "preferences_epub_bg_color_picker_right";// 颜色选择器中背景选择的右侧按钮。
	public static final String KEY_EPUB_THEME_FONT_PICKER_L = "preferences_epub_font_color_picker_left";// 颜色选择器中背景选择的右侧按钮。
	public static final String KEY_EPUB_THEME_FONT_PICKER_R = "preferences_epub_font_color_picker_right";// 颜色选择器中背景选择的右侧按钮。
	public static final String KEY_EPUB_ANIMATION_SHOW = "preferences_epub_animation_show";
	public static final String KEY_EPUB_GIVEN_BG_COLOER = "preferences_epub_given_bg_color";// 已给背景的颜色值。
	public static final String KEY_EPUB_BATTERANDTIMER_SHOW = "preferences_epub_batteryandtime_show";// 是否显示电量和时间。
	public static final String KEY_EPUB_SYSTEM_BAR_SHOW = "preferences_epub_systembar_show";// 是否显示系统框。
	public static final String KEY_EPUB_IS_NIGHT_MODE = "preferences_epub_is_night_mode";// 是否黑夜模式
	public static final String KEY_EPUB_IS_SYSTEM_LIGHT="preferences_epub_is_follow_system_light";//是否随系统亮度。
	public static int BOOK_THEME_BG_PICKER_L_COLOR = -1;// 背景左侧色值
	public static float BOOK_THEME_BG_PICKER_R_COLOR = 0.1f;// 背景有侧色值
	public static int BOOK_THEME_FONT_PICKER_L_COLOR = -1;// 字体左侧色值
	public static float BOOK_THEME_FONT_PICKER_R_COLOR = 0.1f;// 背景有侧色值
	// external
	public static long BOOK_ID = 0;
	public static long BOOK_FORMAT = 0;
	public static String BOOK_PATH = "";// "/sdcard/test.epub";
	public static String BOOK_KEY = "";
	public static String BOOK_RANDOM = "";
	public static String BOOK_DEVICE_ID = "";
	public static String BOOK_URL = "";
	public static boolean BOOK_ONLINE = false;
	public static String BOOK_NAME = "";
	public static String BOOK_AUTHOR = "";
	public static String BOOK_TEMP_DIR="";
	public static String BOOK_USERNAME="";
	public static ArrayList<BookNote> allBookNotes=new ArrayList<BookNote>();
	public static String BOOK_PRICE="";
	public static String BOOK_OLD_PRICE="";
	public static String  BOOK_ORDERID="";
	// public static String BG_PATH_1 = "";//
	// "/sdcard/epub-sdk-demo/bg_book_1.png";
	// public static String BG_PATH_2 = "";//
	// "/sdcard/epub-sdk-demo/bg_book.png";
	// public static String BG_PATH_4 = "";
	// public static String BG_PATH_5 = "";

	public static boolean BG_CUSTOMIZED_BACKGROUND_CHANGED = false;

	public static final int BG_COLOR_1 = 0xFF000000;// 黑色模式背景
	public static final int BG_COLOR_2 = 0xFFeee7de; // 默认背景。
	public static final int FONT_COLOR_1 = 0x66666666;// 0xFF0000黑夜模式字体
	public static final int FONT_COLOR_2 = 0x66333333;// 默认字体
	public static int BOOK_TITLE_COLOR = 0x6600001A;
	public static int BG_COLOR_SEFT = BG_COLOR_2;
	public static int FONT_COLOR_SEFT = FONT_COLOR_2;
	public static int FONT_COLOR_BG = FONT_COLOR_2;
	public static String BG_PATH = "";
	public static String BG_PATH_CUSTOMER = "customer.png";
	// public final static String FONT_PATH = "/sdcard/DroidSansFallback.TTF";
	public static String FONT_PATH = "";
	// public static final String BG_PATH = "";
	public static int FONT_COLOR = FONT_COLOR_2;// 0xFF0000
	private static float PIXEL_WIDTH = 0;
	private static float PIXEL_HEIGHT = 0; // 0x000000

	// public static boolean IS_BOOK_THEME_NIGHT=false;
	public static int BOOK_THEME = -1;
	public static int BOOK_THEME_NUM_DEFAULT = 0;
	public static int BOOK_THEME_NUM = BOOK_THEME_NUM_DEFAULT;
	public static int BOOK_THEME_DEFAULT = -1;
	public static final int BOOK_THEME_BG = 1;
	public static final int BOOK_THEME_SELF = 2;
	// public static final int BOOK_THEME_NIGHT = 3;

	public static boolean IS_BG_PICKER = true;
	public static int BOOK_FONTSIZE_DEFAULT=DPIUtil.dip2px(11.6f);
	public static int BOOK_FONTSIZE = BOOK_FONTSIZE_DEFAULT;
	public static int BOOK_FONTSIZE_TWO = DPIUtil.dip2px(1.4f);// 以2个递增。
	public static int BOOK_FONTSIZE_FOUR = DPIUtil.dip2px(2.7f);// 以4个递增。
	public static int BOOK_FONTSIZE_LINE = DPIUtil.dip2px(13.4f);// 字体在20以下，以2增加，以上以4增加。
	public static final int BOOK_FONTSIZE_MIN = DPIUtil.dip2px(6.7f);
	public static final int BOOK_FONTSIZE_MAX = DPIUtil.dip2px(27.7f);
 	public static final int BOOK_BRIGHTNESS_MIN = 10;
	public static final int BOOK_BRIGHTNESS_MAX = 225;

	// private static final String TAG = "BookProperties";
	public static int BG_COLOR = -1;
	public static boolean IS_USER_BG_COLOR=true;
	public static boolean BOOK_VOLUME_ENABLE = false;
	public static boolean IS_SYSTEM_STATUS_BAR_SHOW = false;
	public static boolean IS_TIME_AND_POW_SHOW = false;
	public static boolean IS_BOOK_ANIMATION_SHOW = false;
	public static boolean IS_ONLY_ONE_BITMAP=false;
	public static boolean IS_BOOK_NIGHT_MODE = false;
	public static boolean IS_FOLLOW_SYSTEM_LIGHT=true;
	public static int BOOK_TIMEOUT_LEVEL_DEFAULT = 10;
	public static int BOOK_TIMEOUT_LEVEL = BOOK_TIMEOUT_LEVEL_DEFAULT;

	public static int SCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

	private static int BOOK_BRI_PERCENT_DEFLUT = -1;
	public static int BOOK_BRI_PERCENT = BOOK_BRI_PERCENT_DEFLUT;
	public static int BOOK_BRI_DELTA = 5;// 每次增加的亮度值。

	public static int BOOK_LINE_SPACING_DEFAULT = 120;
	public static int BOOK_LINE_SPACING = BOOK_LINE_SPACING_DEFAULT;
	public static int BOOK_LINE_SPACING_MAX = 180;
	public static int BOOK_LINE_SPACING_MIN = 80;
	public static int BOOK_LINE_SPACING_PRO =20;// 每次行间距调整的幅度为20;


}
