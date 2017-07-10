package com.jingdong.app.reader.config;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import android.text.TextUtils;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.util.CommonUtil;


public class Configuration {

	public static Properties properties;
	private static Map<String, String> localProperties;
	public static String pluginHost = "http://app.e.360buy.com/font/";

	// 是否显示log
	// public static final boolean sPrintLog = true;
	public static final boolean sPrintClassLog = true;
	public static final boolean sWriteLog = false;
	public static final boolean isDebugMode = true;// 编译期去掉log
	// false去掉log，true保留log
	public static int readSlidelMode = 1;// 1和原来一样 2.整合一起的窗口 3.独立的应用启动，
	// Key
	public static final String UPDATEHOST = "updateHost";
	public static final String HOST = "host";// 服务端
	public static final String NOTE_HOST = "noteHost";// 图书下载服务端
	public static final String DBOOK_HOST = "dBookHost";// 图书下载服务端
	public static final String ONLINE_READ_HOST = "onlineReadHost";// 畅读服务端
	public static final String ORDER_HOST = "orderHost";// 下单服务器。
	public static final String APP_ID = "appId";// 网络请求方式
	public static final String CONNECT_TIMEOUT = "connectTimeout";// 连接超时
	public static final String READ_TIMEOUT = "readTimeout";// 读取超时
	public static final String ATTEMPTS = "attempts";// 尝试次数
	public static final String ATTEMPTS_TIME = "attemptsTime";// 尝试的间隔时间
	public static final String REQUEST_METHOD = "requestMethod";// 网络请求方式

	public static final String LOCAL_MEMORY_CACHE = "localMemoryCache";// 本地文件缓存
	public static final String LOCAL_FILE_CACHE = "localFileCache";// 本地内存缓存

	public static final String INIT_POOL_SIZE = "initPoolSize";// 最少网络线程池
	public static final String MAX_POOL_SIZE = "maxPoolSize";// 最大网络线程池

	public static final String DISCUSSUPLOADIMAGE_WIDTH = "discussUploadImageWidth";// 晒单上传图片宽度上限
	public static final String DISCUSSUPLOADIMAGE_HEIGHT = "discussUploadImageHeight";// 晒单上传图片高度上限
	public static final String DISCUSSUPLOADIMAGE_QUALITY = "discussUploadImageQuality";// 晒单上传图片质量

	public static final String ROUTINE_CHECK_DELAY_TIME = "routineCheckDelayTime";// 例行检查延迟时间

	public static final String LEAVE_TIME_GAP = "leaveTimeGap";// 例行检查延迟时间

	// public static final String PRINT_LOG = "printLog";// 日志开关
	// public static final String DEBUG_LOG = "debugLog";// 日志开关
	// public static final String VIEW_LOG = "viewLog";// 日志开关
	// public static final String ERROR_LOG = "errorLog";// 日志开关
	// public static final String INFO_LOG = "infoLog";// 日志开关
	// public static final String WARN_LOG = "warnLog";// 日志开关
	public static final String PARTNERID = "partnerID";// cpa合作者ID,电子书测试环境值为000001
	public static final String SUBPARTNERID = "subPartnerID";// cpa合作者子ID
	public static final String SOURCEPUBLICKEY = "sourcePublicKey";// CPA公钥
	public static final String DOSWITCHCPA = "doSwitchCPA";// 是否注册CPA开关
	public static final String DOSWITCHCPS = "doSwitchCPS";// 是否注册CPS开关
	public static final String VERSION_UPDATE = "versionupdate";// 是否自动更新
	public static final String DOSWITCHPOMOTIONVERSION = "doSwitchPomotionVersion";// 是否做活动
																					// 在登录中加入pomotionVersion字段
	public static final String DOSWITCHPRESENT = "doSwitchPresent"; // 赠书活动开关，在应用启动时弹出赠书对话框
	public static final String DOSAVECPS = "doSaveCPS"; // 更新后是否覆盖CPS信息
	public static final String DOSWITCHBINDCARD = "doSwitchBindCard"; // 绑定畅读卡活动开关，在应用启动时弹出赠书对话框
	public static final String SINGLEBOOK = "singleBookPack";//是否单行本

	public static final String TEST_MODE = "testMode";// 测试开关
	public static final String PREF_KEY_FIRSTRUN = "firstrun";// 是否第一次运行

	public static final String PARTNER = "partner";// 合作伙伴

	public static final String UNION_ID = "unionId";// CPS
	public static final String UNION_SITE_ID = "unionSiteId";
	public static final String SUB_UNION_ID = "subunionId";// CPS

	public static final String APPLICATION_UPGRADE = "upgrade";// 升级检查开关

	public static final String APPLICATION_SHORTCUT = "applicationShortcut";// 快捷方式开关

	public static final String COST_HINT = "costHint";// 流量提示开关

	public static final String EBOOK_SHARE_ADDRESS = "ebook_share_address";
	public static final String PBOOK_SHARE_ADDRESS = "pbook_share_address";

	static {
		localProperties = new HashMap<String, String>();

		// 内置配置
		localProperties.put(HOST, "gw.e.jd.com");// 服务端
		localProperties.put(CONNECT_TIMEOUT, "" + 10 * 1000);// 连接超时
		localProperties.put(READ_TIMEOUT, "" + 20 * 1000);// 读取超时
		localProperties.put(ATTEMPTS, "" + 2);// 尝试次数
		localProperties.put(ATTEMPTS_TIME, "" + 0);// 尝试的间隔时间
		localProperties.put(REQUEST_METHOD, "post");// 网络请求方式

		localProperties.put(LOCAL_MEMORY_CACHE, "false");// 本地文件缓存
		localProperties.put(LOCAL_FILE_CACHE, "false");// 本地内存缓存

		localProperties.put(INIT_POOL_SIZE, "" + 5);// 最少网络线程池
		localProperties.put(MAX_POOL_SIZE, "" + 5);// 最大网络线程池

		localProperties.put(DISCUSSUPLOADIMAGE_WIDTH, "" + 500);// 晒单上传图片宽度上限
		localProperties.put(DISCUSSUPLOADIMAGE_HEIGHT, "" + 500);// 晒单上传图片高度上限
		localProperties.put(DISCUSSUPLOADIMAGE_QUALITY, "" + 80);// 晒单上传图片质量

		localProperties.put(ROUTINE_CHECK_DELAY_TIME, "" + (1000 * 2));// 例行检查延迟时间

		localProperties.put(LEAVE_TIME_GAP, "" + (1000 * 60 * 60));// 用户离开时间阀值

		// localProperties.put(PRINT_LOG, "false");// 日志开关
		// localProperties.put(DEBUG_LOG, "true");// 日志开关
		// localProperties.put(VIEW_LOG, "true");// 日志开关
		// localProperties.put(ERROR_LOG, "true");// 日志开关
		// localProperties.put(INFO_LOG, "true");// 日志开关
		// localProperties.put(WARN_LOG, "true");// 日志开关

		localProperties.put(TEST_MODE, "false");// 测试开关
		localProperties.put(DBOOK_HOST, "rights.e.jd.com");// 流量提示开关
//		localProperties.put(DBOOK_HOST, "rights.e.360buy.com");// 流量提示开关
		localProperties.put(NOTE_HOST, "notes.e.jd.com");
		localProperties.put(APP_ID, "1");

		// 发布需要修改的字段
		localProperties.put(PARTNER, "jingdong");// 合作伙伴

		localProperties.put(UNION_ID, null);// CPS
		localProperties.put(SUB_UNION_ID, null);// CPS
		localProperties.put(APPLICATION_UPGRADE, "true");// 升级检查开关
		localProperties.put(APPLICATION_SHORTCUT, "false");// 快捷方式开关
		localProperties.put(COST_HINT, "false");// 流量提示开关
		localProperties.put(VERSION_UPDATE, "false");// 流量提示开关
		localProperties.put(DOSWITCHBINDCARD, "-1");// 绑定卡默认值为false;
		localProperties.put(DOSWITCHPRESENT, "false");// 赠送默认值为false;
		localProperties.put(SINGLEBOOK, "false");// 是否单本书。
		// 在线畅读服务端。
		localProperties.put(ONLINE_READ_HOST, "cread.e.jd.com");
		localProperties.put(ORDER_HOST, "order.e.jd.com");
		localProperties.put(UPDATEHOST, "gw.music.jd.com");

		try {
			InputStream inputStream = Configuration.class.getClassLoader()
					.getResourceAsStream("reader_config.properties");
			if (null != inputStream) {
				properties = new Properties();
				properties.load(inputStream);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveCPSTojdAndroidClient(Properties properties) {
		try {
			String result = null;
			result = properties.getProperty(DOSWITCHCPS);
			if (result != null) {
				CommonUtil.saveTOJdSharedPreferences(DOSWITCHCPS, result);
			}
			result = properties.getProperty(UNION_ID);
			if (result != null) {
				CommonUtil.saveTOJdSharedPreferences(UNION_ID, result);
			}
			result = properties.getProperty(UNION_SITE_ID);
			if (result != null) {
				CommonUtil.saveTOJdSharedPreferences(UNION_SITE_ID, result);
			}
			CommonUtil.saveBoolean(PREF_KEY_FIRSTRUN, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// CPA相关部分配置移动到assets文件夹下 cpa.properties 文件中（主要为了方便打包工具使用）
	public static void loadProperties4CPA() {
		InputStream asset_is = null;
		try {
			asset_is = MZBookApplication.getInstance().getAssets()
					.open("cpa.properties");
			Properties properties = new Properties();
			properties.load(asset_is);

			String result = properties.getProperty(DOSWITCHPOMOTIONVERSION);
			if (result != null)
				localProperties.put(DOSWITCHPOMOTIONVERSION, result);

			result = properties.getProperty(DOSWITCHCPA);
			if (result != null)
				localProperties.put(DOSWITCHCPA, result);

			result = properties.getProperty(SOURCEPUBLICKEY);
			if (result != null)
				localProperties.put(SOURCEPUBLICKEY, result);

			result = properties.getProperty(PARTNERID);
			if (result != null)
				localProperties.put(PARTNERID, result);

			result = properties.getProperty(SUBPARTNERID);
			if (result != null)
				localProperties.put(SUBPARTNERID, result);

			result = properties.getProperty(DOSWITCHCPS);
			if (result != null)
				localProperties.put(DOSWITCHCPS, result);

			result = properties.getProperty(UNION_ID);
			if (result != null)
				localProperties.put(UNION_ID, result);

			result = properties.getProperty(UNION_SITE_ID);
			if (result != null)
				localProperties.put(UNION_SITE_ID, result);

			result = properties.getProperty(APPLICATION_UPGRADE);
			if (result != null)
				localProperties.put(APPLICATION_UPGRADE, result);

			result = properties.getProperty(DOSWITCHPRESENT);
			if (result != null)
				localProperties.put(DOSWITCHPRESENT, result);

			result = properties.getProperty(SINGLEBOOK);
			if (result != null)
				localProperties.put(SINGLEBOOK, result);

			result = properties.getProperty(DOSWITCHBINDCARD);// 绑定畅读卡。
			if (result != null)
				localProperties.put(DOSWITCHBINDCARD, result);

			loadCPSFormPref();
			result = properties.getProperty(DOSAVECPS);
			if (!TextUtils.isEmpty(result)) {
				if (Boolean.parseBoolean(result)
						&& CommonUtil.getBoolean(PREF_KEY_FIRSTRUN)) {
					saveCPSTojdAndroidClient(properties);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (asset_is != null) {
					asset_is.close();
					asset_is = null;
				}
			} catch (Exception e2) {
			}
		}
	}

	private static void loadCPSFormPref() {

		String doswitchcps = CommonUtil.getJdSharedPreferences().getString(
				DOSWITCHCPS, "");
		String unionID = CommonUtil.getJdSharedPreferences().getString(
				UNION_ID, "");
		String unionSITID = CommonUtil.getJdSharedPreferences().getString(
				UNION_SITE_ID, "");
		if (!TextUtils.isEmpty(doswitchcps)) {
			localProperties.put(DOSWITCHCPS, doswitchcps);
		}
		if (!TextUtils.isEmpty(unionID)) {
			localProperties.put(UNION_ID, "unionID");
		}
		if (!TextUtils.isEmpty(unionSITID)) {
			localProperties.put(UNION_SITE_ID, "unionSITID");
		}
	}

	public static String getProperty(String key) {
		return getProperty(key, null);
	}

	public static String getProperty(String key, String defaultValue) {
		String result = null;
		if (null != properties) {
			result = properties.getProperty(key);
		}
		if (null == result) {
			result = localProperties.get(key);
		}
		if (null == result) {
			result = defaultValue;
		}
		return result;
	}

	public static Integer getIntegerProperty(String key) {

		return getIntegerProperty(key, null);
	}

	public static Integer getIntegerProperty(String key, Integer defaultValue) {
		String value = getProperty(key);
		if (null == value) {
			return defaultValue;
		}
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			return defaultValue;
		}

	}

	public static Boolean getBooleanProperty(String key) {
		return getBooleanProperty(key, null);
	}

	public static Boolean getBooleanProperty(String key, Boolean defaultValue) {
		String value = getProperty(key);
		if (null == value) {
			return defaultValue;
		}
		try {
			return Boolean.valueOf(value);
		} catch (Exception e) {
			return defaultValue;
		}

	}

}
