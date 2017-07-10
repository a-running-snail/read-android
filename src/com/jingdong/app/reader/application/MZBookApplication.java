package com.jingdong.app.reader.application;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Vector;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;
import jd.wjlogin_sdk.common.WJLoginHelper;
import jd.wjlogin_sdk.model.ClientInfo;
import jd.wjlogin_sdk.util.ReportType;
import net.davidashen.text.Hyphenator;
import net.davidashen.util.ErrorHandler;

import com.baidu.mobstat.StatService;
import com.jd.cpa.security.CpaConfig;
import com.jd.cpa.security.CpaHelper;
import com.jd.cpa.security.OnDevRepCallback;
import com.jd.cpa.security.ResultType;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.MZBookLifecycleHandler;
import com.jingdong.app.reader.activity.MyActivity;
import com.jingdong.app.reader.bookstore.buyborrow.BuyBorrowStatus;
import com.jingdong.app.reader.bookstore.fragment.BookStoreRootFragment;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.extension.giftbag.GiftBagUtil;
import com.jingdong.app.reader.extension.jpush.JDPush;
import com.jingdong.app.reader.io.StoragePath;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.security.RequestSecurityKeyTask;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ACache;
import com.jingdong.app.reader.util.CommonUtil;
import com.jingdong.app.reader.util.CrashHandler;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.StatisticsReportUtil;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tendcloud.tenddata.TCAgent;

public class MZBookApplication extends Application {

	public static final boolean DEBUG = true;
	public static final boolean TestBuild = true;
	private static MZBookApplication instance;
	public static final String PREFERENCES_NAME = "MZBookPreferences";
	private static boolean isMZBookAppRunning = false;
	private static UsingFreqLimitedMemoryCache memoryCache = null;
	private static int memoryCacheSize = (int) (Runtime.getRuntime()
			.maxMemory() / 1024 / 4);
	private static int diskCacheSize = 100 * 1024 * 1024;
	private static final String mBookStoreCacheKey = "bookstorecache";
	private ACache mBoostoreCache = null;
	public static boolean isExiting = false;
	public static boolean firstflag = true;
	public static final String TD_APP_ID = "BF8850873C1661A325DE5BF56740B236";
	public static boolean alreadyShowRecomment=false;
	/** 应用启动后随即生成的唯一标识 */
	public static String jds = "";
	/** 软件缓存路径 */
	private String cachePath = "";
	/** 借书状态  true:允许 false:不允许*/
	private BuyBorrowStatus mBuyBorrowStatus=new BuyBorrowStatus();
	
	/**
	 * 是否是小辣椒渠道
	 */
	public static final boolean isXiaoLajiao=false;
	private LocalBroadcastManager localBroadcastManager=null;
	
	//标识是否成功登录
	public static boolean isLogin = false;
	
	public Hyphenator h;
	
	public boolean isLogin() {
		return isLogin;
	}

	public void setLogin(boolean isLogin) {
		MZBookApplication.isLogin = isLogin;
	}

	public ACache getBoostoreCache() {
		return mBoostoreCache;
	}

	private static Context context;

	// cyr add
	private MyActivity currentMyActivity;

	private static Vector<Activity> activities = new Vector<Activity>();
	private static final String giftCacheName="gift";
	private ACache giftCache=null;
	public ACache getGiftCache() {
		return giftCache;
	}

	public void setGiftCache(ACache giftCache) {
		this.giftCache = giftCache;
	}

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(getContext(), (String) msg.obj, msg.arg1).show();
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		context = getApplicationContext();
		
		jds = "" + System.currentTimeMillis();
		initCachePath();

		Configuration.loadProperties4CPA();
		MZBookDatabase.init(this);
		LocalBroadcastManager.getInstance(this);
		StoragePath.init(this);
		registerActivityLifecycleCallbacks(MZBookLifecycleHandler.getInstance());
		mBoostoreCache = ACache.get(getApplicationContext(), mBookStoreCacheKey);
		giftCache=ACache.get(getApplicationContext(), giftCacheName);
		// 添加图片缓存
		initImageLoader(this);

		// 获取加密key
		// initKeyValue();

		// 获取登录数据
		LoginUser.getInstance();
		// LoginUser.logOut(false);

		// 初始化Talking_Data SDK
		TCAgent.LOG_ON = DEBUG;
		String partnerID = CommonUtil.getPropertiesValue("partnerID");
		Log.d("JD_Reader", "partnerID:"+partnerID);
		String subPartnerID = CommonUtil.getPropertiesValue("subPartnerID");
		if (!TextUtils.isEmpty(partnerID)) {
			String partnerid = partnerID + "_" + subPartnerID;
			Log.d("J.Beyond", "partnerid:" + partnerid);
			TCAgent.init(this, TD_APP_ID, partnerid);
		} else {
			TCAgent.init(this);
		}
		TCAgent.setReportUncaughtExceptions(true);

		JDPush.openPush(this);
		
		String appChannel = "10005";
		if (TextUtils.isEmpty(partnerID) && TextUtils.isEmpty(subPartnerID)) {
			appChannel = "10005";
		} else {
			if(!TextUtils.isEmpty(partnerID)) {
				if(!TextUtils.isEmpty(subPartnerID)) {
					appChannel = partnerID + "_" + subPartnerID;;
				}else {
					appChannel = partnerID;
				}
			}else {
				if(!TextUtils.isEmpty(subPartnerID)) {
					appChannel = subPartnerID;;
				}
			}
		}
		StatService.setAppChannel(this, appChannel, true);
		
		if (!DEBUG) {
			CrashHandler crashHandler = CrashHandler.getInstance();
			// 注册crashHandler
			crashHandler.init(getApplicationContext());
		}

		localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
	}
	public void sendLocalBroadcastMessage(Intent intent){
		localBroadcastManager.sendBroadcast(intent);
	}
	public void regeisteLocalBroadcastRecivier(BroadcastReceiver receiver,IntentFilter filter){
		localBroadcastManager.registerReceiver(receiver, filter);
	}
	public void unregeisteLoacalBroadcastRecivier(BroadcastReceiver receiver){
		localBroadcastManager.unregisterReceiver(receiver);
	}
	public void initKeyValue() {
		RequestSecurityKeyTask task = new RequestSecurityKeyTask(null);
		task.excute();
	}

	public static synchronized void addToActivityStack(Activity activity) {
		if (activities == null)
			activities = new Vector<Activity>();
		if (!isExiting)
			activities.add(activity);
	}

	public static synchronized void removeFormActivityStack(Activity activity) {
		if (activities != null && !isExiting)
			activities.remove(activity);
	}

	// 退出应用
	public static void exitApplication() {
		context.stopService(new Intent(context, NotificationService.class));
		isExiting = true;
		isMZBookAppRunning = false;
		isLogin = false;
		toDoOnExit();
		if (activities != null) {

			MZLog.d("wangguodong", "当前activity堆栈大小:" + activities.size());
			for (Activity activity : activities) {
				activity.finish();
			}
		}
		GiftBagUtil.getInstance().clearCache();
		
		
		//统一平台设置状态为退出app
//		helper.reportAppStatus(ReportType.Exit);

		// 杀掉进程
//		 System.exit(0);
//		 android.os.Process.killProcess(android.os.Process.myPid());
	}

	private static void toDoOnExit() {
		DownloadService.closeThreads();
		WebRequestHelper.setCookies(null);
		// 用户登出修改书架是否扫描的标记
		LocalUserSetting.saveLoginScan(getContext(), false);
		LoginUser.logOut(false);
		BookStoreRootFragment.mCurrentPos = 0;
		GiftBagUtil.isHaveShow=false;
	}
	

	public static Context getContext() {
		return context;
	}

	public static boolean isMZBookAppRunning() {
		return isMZBookAppRunning;
	}

	public static void startMZBookApp() {
		isMZBookAppRunning = true;
	}

	public static MZBookApplication getInstance() {
		return instance;
	}

	public static UsingFreqLimitedMemoryCache getMemoryCache() {
		if (memoryCache == null)
			memoryCache = new UsingFreqLimitedMemoryCache(memoryCacheSize);
		return memoryCache;

	}

	public static void initImageLoader(Context context) {
		File cacheDir = StoragePath.getCachesDir(context, true);
		LruDiscCache discCache = null;
		try {
			discCache = new LruDiscCache(cacheDir, new Md5FileNameGenerator(),
					diskCacheSize);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).memoryCacheExtraOptions(480, 800)
				// 不配置使用默认手机尺寸
				.threadPoolSize(3)
				// 线程池大小3
				.threadPriority(Thread.NORM_PRIORITY - 2)
				// 默认配置
				.tasksProcessingOrder(QueueProcessingType.FIFO)
				// 默认配置
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(getMemoryCache())// 如果缓存的图片总量超过限定值，先删除使用频率最小的bitmap
				.memoryCacheSize(memoryCacheSize)
				.diskCache(
						discCache == null ? new UnlimitedDiscCache(cacheDir)
								: discCache).diskCacheSize(diskCacheSize)
				// .diskCacheFileCount(300)// 缓存的文件数量
				.diskCacheFileNameGenerator(new Md5FileNameGenerator()) // 将保存的时候的URI名称用MD5加密
				// .writeDebugLogs()// 正式版本要移除此处
				.build();
		ImageLoader.getInstance().init(config);
	}

	// cyr add
	public MyActivity getCurrentMyActivity() {
		return currentMyActivity;
	}

	public void setCurrentMyActivity(MyActivity activity) {
		this.currentMyActivity = activity;

	}
	
	/**
	* @Description: 初始化缓存根目录
	* @author xuhongwei1
	* @date 2015年11月4日 下午6:27:02 
	* @throws 
	*/ 
	private void initCachePath() {
		cachePath = android.os.Environment.getExternalStorageState(); 
		if ((cachePath!=null)&&(cachePath.equals(android.os.Environment.MEDIA_MOUNTED))) {  
			cachePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath(); 
		}else{ 
			cachePath = Environment.getDataDirectory().getAbsolutePath()+File.separator+"data"+File.separator+getPackageName();
		}
		
		cachePath += File.separator + "JDReader";
	}
	
	/**
	* @Description: 获取软件缓存根目录
	* @author xuhongwei1
	* @date 2015年11月4日 下午6:26:39 
	* @throws 
	*/ 
	public String getCachePath() {
		File dir = new File(cachePath);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		return cachePath;
	}
	
	/**
	* @Description: 更新借书状态
	* @param boolean status true:允许借书
	* @author xuhongwei1
	* @date 2015年12月1日 下午1:28:03 
	* @throws 
	*/ 
	public void setBuyBorrowStatus(BuyBorrowStatus status) {
		this.mBuyBorrowStatus = status;
	}
	
	/**
	* @Description: 获取借书状态
	* @return boolean
	* @author xuhongwei1
	* @date 2015年12月1日 下午1:28:38 
	* @throws 
	*/ 
	public BuyBorrowStatus getBuyBorrowStatus() {
		return this.mBuyBorrowStatus;
	}
	
	/**
	 * 格式化字符串
	 * @param str
	 * @return
	 */
	private static String getFormatString(String str) {
		if(TextUtils.isEmpty(str)) {
			str = "";
			return str;
		}
		
		if(str.length() > 12) {
			str = str.substring(0, 11);	
		}
		str.replaceAll(" ", "");
		return str;
	}

	/**
	 * 设置统一登录平台初始化参数
	 * @return
	 */
	public static ClientInfo getClientInfo() {
		if (clientInfo == null) {
			clientInfo = new ClientInfo();
			clientInfo.setDwAppID((short) 103);
			clientInfo.setAppName(instance.getResources().getString(R.string.app_name));
			clientInfo.setClientType("android");
			clientInfo.setUuid(StatisticsReportUtil.readDeviceUUID());
			clientInfo.setArea("SHA");
			clientInfo.setDwGetSig(1);
			clientInfo.setDwAppClientVer(StatisticsReportUtil.getSoftwareVersionName());
			clientInfo.setOsVer(URLEncoder.encode(Build.VERSION.RELEASE));
			Display display = ((WindowManager)instance.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			if(null != display) {
				String screen = display.getWidth() + "*" + display.getHeight();
				clientInfo.setScreen(screen);
			}
			clientInfo.setDeviceBrand(getFormatString(Build.MANUFACTURER));
			clientInfo.setDeviceModel(getFormatString(Build.MODEL));
			clientInfo.setDeviceName(getFormatString(Build.PRODUCT));
			clientInfo.setReserve(Build.VERSION.RELEASE);
		}

		return clientInfo;
	}

	/** 统一登录平台客户端信息 */
	private static ClientInfo clientInfo = null;
	/** 登录注册辅助功能类  统一登录平台helper */
	private static WJLoginHelper mWJLoginHelper = null;
	public static WJLoginHelper getWJLoginHelper() {
		if (null == mWJLoginHelper) {
			synchronized (WJLoginHelper.class) {
				if (null == mWJLoginHelper) {
					mWJLoginHelper = new WJLoginHelper(instance, getClientInfo());
					mWJLoginHelper.createGuid();
					//设置开发模式，上线后改成false
					mWJLoginHelper.SetDevleop(false);
				}
			}
		}
		
		return mWJLoginHelper;
	}
	
	/**
	 * 判断当前设备是否是平板
	 * @return
	 */
	public static boolean isPad() {
		float mScreenWidth = ScreenUtils.getWidthJust(instance);
		float mScreenHeight = ScreenUtils.getHeightJust(instance);
		if((800 == mScreenWidth && 1216 == mScreenHeight) || 
				(1080 == mScreenWidth && 1824 == mScreenHeight)) {
			return true;	
		}
		
		return false;
	}
	
	public Hyphenator getHyphen() {
		if(h!=null)
			return h;
        h = new Hyphenator();
        h.setErrorHandler(new ErrorHandler() {

            @Override
            public void debug(String arg0, String arg1) {

            }

            @Override
            public void error(String arg0) {

            }

            @Override
            public void exception(String arg0, Exception arg1) {

            }

            @Override
            public void info(String arg0) {

            }

            @Override
            public boolean isDebugged(String arg0) {
                return false;
            }

            @Override
            public void warning(String arg0) {

            }
        });
        try {
            h.loadTable(new BufferedInputStream(getResources().getAssets().open("hyphen/ushyph.tex")));
            return h;
        } catch (Exception e) {
            return null;
        }

    }
	
}
