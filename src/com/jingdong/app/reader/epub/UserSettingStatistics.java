package com.jingdong.app.reader.epub;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.activity.ReadOverlayActivity;
import com.jingdong.app.reader.data.db.DBHelper;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.plugin.FontItem;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.FileUtils;
import com.jingdong.app.reader.util.NetWorkUtils;
import android.content.Context;
import android.text.TextUtils;

/**
 * 用户阅读设置统计
 * @author xuhongwei
 *
 */
public class UserSettingStatistics {
	
	 /**
     * 上传用户阅读设置信息
     */
    public static void upload(Context context) {
    	if(NetWorkUtils.isNetworkConnected(context) && LoginUser.isLogin()) {
    		JSONObject body = new JSONObject();
    		try {
				body.put("1", getColorJson(context));
				body.put("2", getFontJson(context));
				body.put("3", getFlipAnimationSetting(context));
				body.put("4", getFontSize(context));
				body.put("5", getNightModel(context));
				body.put("6", getLineSpacing(context));
				body.put("7", getDuanSpacing(context));
				body.put("8", getPageSpacing(context));
				body.put("9", getReadBrightness(context));
				body.put("10", getIsSyncBrightness(context));
				body.put("11", getIsTraditional(context));
				WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSettingParams(body.toString()),
		       			 true, new MyAsyncHttpResponseHandler(context) {

		   					@Override
		   					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
		   					}
		   					
		   					@Override
		   					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
		   					}
		   					
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    }

    /**
     * 获取背景颜色和字体颜色json
     * @param context
     * @return
     */
    private static JSONObject getColorJson(Context context) {
    	String val = "";
        String desc = "";
    	JSONObject color = new JSONObject();
    	int bg_color = LocalUserSetting.getReading_Background_Color(context);// 阅读背景颜色
    	int text_color = LocalUserSetting.getReading_Text_Color(context);// 字体颜色
        int texture = LocalUserSetting.getReading_Background_Texture(context);
        String bg_color_str = String.format("%X", bg_color);
        String text_color_str = String.format("%X", text_color);
        desc = bg_color_str + ";" + text_color_str;
        if (-1 == texture) {
        	if (0xFFF2F2F2 == bg_color) { //日常
        		val = "1001";
        	} else if (0xFFFAF5ED == bg_color) { //护眼
        		val = "1002";
        	} else if (0xFFCEEBCE == bg_color) { //清新
        		val = "1003";
        	} else if (0xFFFFFFFF == bg_color) { //纯白
        		val = "1004";
        	} else if(0xFFF7E7DE == bg_color) { //米黄
        		val = "1005";
        	} else if(0xFFF7E3D6 == bg_color) { //白纸
        		val = "1006";
        	} else if(0xFFFFE7EF == bg_color) { //粉色
        		val = "1007";
        	} else if(0xFFDEC79C == bg_color) { //纸黄色
        		val = "1008";
        	} else if(0xFF08598C == bg_color) { //海军蓝
        		val = "1009";
        	} else if(0xFF104139 == bg_color) { //暗绿
        		val = "1010";
        	} else if(0xFF21495A == bg_color) { //酱色
        		val = "1011";
        	} else if(0xFF212421 == bg_color) { //深褐
        		val = "1012";
        	} else {
        		val = "-1";
        	}
        } else {
        	if(texture >= 0 && texture < 9) {
        		String wenli[] = {"1013", "1014", "1015", "1016", "1017", "1018", "1019", "1020", "1021"};
            	val = wenli[texture];
        	}else {
        		val = "1013";
        	}
        }
        
		try {
			color.put("val", val);
			color.put("desc", desc);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
        return color;
    }
    
    /**
     * 获取字体json
     * @param context
     * @return
     */
    private static JSONObject getFontJson(Context context) {
    	JSONObject font = new JSONObject();
    	String val = "2001";
    	String systemFontName="";
    	String fontNames = "";
    	String desc = "";
    	FontItem mFontItem = DBHelper.queryEnabledFontItem();
		FontItem fontFZSS = DBHelper.queryFontItemByName(FontItem.FOUNDER_SS);
		if (TextUtils.isEmpty(mFontItem.getFilePath())) {
			DBHelper.initDefautDbData();
		} else if (TextUtils.isEmpty(fontFZSS.getUrl())) {
			//XXX 如果没有方正仿宋执行初始化
			DBHelper.initDefautDbData();
		}
		
    	ArrayList<FontItem> list = DBHelper.queryPluginItemList(FontItem.KEY_PLUGIN_FONT);
		for (int i = 0; i < list.size(); i++) {
			Object obj = list.get(i);
			FontItem fontItem = (FontItem) obj;
			String fontname = fontItem.getName();
			
			if (fontItem.getPlugin_enable() == FontItem.KEY_PLUGIN_ENABLE) {
				if(fontname.contains("系统字体")) {
					val = "2001";
				} else if (FontItem.FOUNDER_SS.equals(fontname)) {
					val = "2002";
				} else if (FontItem.FOUNDER_LANTINGHEI.equals(fontname)) {
					val = "2003";
				} else if (FontItem.FOUNDER_KAITI.equals(fontname)) {
					val = "2005";
				} else if (FontItem.FOUNDER_MIAOWUHEI.equals(fontname)) {
					val = "2006";
				} else {
					val = "-2";
				}
			} 
			
			if(fontname.contains("系统字体")) {
				systemFontName = fontname;
			}
			
			if(!fontname.contains("系统字体")) {
				if (FileUtils.isExist(fontItem.getFilePath())) {
					fontNames += ";" + fontname;
				}	
			}
		}
		
		systemFontName += fontNames;
		try {
			desc = new String(systemFontName.getBytes(), "utf-8");
		} catch (UnsupportedEncodingException e1) {
			desc = systemFontName;
		}
		
		try {
			font.put("val", val);
			font.put("desc", desc);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	return font;
    }
    
    /**
     * 获取翻页动画设置
     * @return
     */
    private static String getFlipAnimationSetting(Context context) {
    	String anim = "";
    	String names [] = {"3001", "3002", "3003", "3004"};
    	int animation = LocalUserSetting.getPageAnimation(context);
    	if(animation >= 0 && animation < 4) {
    		anim = names[animation];
    	}else {
    		anim = names[3];
    	}
    	return anim;
    }
    
    /**
     * 获取字号设置
     * @return
     */
    private static String getFontSize(Context context) {
    	String fontSize = "";
    	String fontname[] = { "4001", "4002", "4003", "4004", "4005", "4006", "4007", "4008", "4009", "4010" };
    	int textSizeLevel = LocalUserSetting.getTextSizeLevel(context);
    	if(textSizeLevel >= 0 && textSizeLevel < 10) {
    		fontSize = fontname[textSizeLevel];
    	}else {
    		fontSize = fontname[4];
    	}
    	return fontSize;
    }
    
    /**
     * 获取黑夜模式设置
     * @return
     */
    private static String getNightModel(Context context) {
    	String nightModel = "";
    	boolean mode = LocalUserSetting.getReading_Night_Model(context);
    	if (mode) {
    		nightModel = "5001";
    	} else {
    		nightModel = "5002";
    	}
    	return nightModel;
    }
    
    /**
     * 获取行间距设置
     * @return
     */
    private static String getLineSpacing(Context context) {
    	String lineSpacing = "";
    	String names[] = { "6001", "6002", "6003", "6004", "6005" };
    	int level = LocalUserSetting.getLineSpaceLevel(context);
    	if(level >= 0 && level < 5) {
    		lineSpacing = names[level];
    	}else {
    		lineSpacing = names[0];
    	}
    	return lineSpacing;
    }
    
    /**
     * 获取段间距设置
     * @return
     */
    private static String getDuanSpacing(Context context) {
    	String dunaSpacing = "";
    	String names[] = { "7001", "7002", "7003", "7004", "7005" };
    	int level = LocalUserSetting.getBlockSpaceLevel(context);
    	if(level >= 0 && level < 5) {
    		dunaSpacing = names[level];
    	}else {
    		dunaSpacing = names[0];
    	}
    	return dunaSpacing;
    }
    
    /**
     * 获取页边距设置
     * @return
     */
    private static String getPageSpacing(Context context) {
    	String pageSpacing = "";
    	String names[] = { "8001", "8002", "8003" };
    	int pageEdgeSpaceLevel = LocalUserSetting.getPageEdgeSpaceLevel(context);
    	if(pageEdgeSpaceLevel >= 0 && pageEdgeSpaceLevel < 3) {
    		pageSpacing = names[pageEdgeSpaceLevel];
    	}else {
    		pageSpacing = names[1];
    	}
    	return pageSpacing;
    }
    
    /**
     * 获取阅读亮度
     * @param context
     * @return
     */
    private static String getReadBrightness(Context context) {
    	return Float.toString(LocalUserSetting.getReadBrightness(context));
    }
    
    /**
     * 获取是否使用系统亮度
     * @param context
     * @return
     */
    private static String getIsSyncBrightness(Context context) {
    	String isSyncBrightness = "";
    	if (LocalUserSetting.isSyncBrightness(context)) {
    		isSyncBrightness = "10001";
    	} else {
    		isSyncBrightness = "10002";
    	}
    	return isSyncBrightness;
    }
    
    /**
     * 获取繁简显示状态
     * @param context
     * @return
     */
    private static String getIsTraditional(Context context) {
    	String isSyncBrightness = "";
    	if (LocalUserSetting.isTraditional(context)) {
    		isSyncBrightness = "11001";
    	} else {
    		isSyncBrightness = "11002";
    	}
    	return isSyncBrightness;
    }
    
}
