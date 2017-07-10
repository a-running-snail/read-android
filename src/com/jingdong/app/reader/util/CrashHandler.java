package com.jingdong.app.reader.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;
import java.util.TreeSet;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

/**
 * 
 * @ClassName: CrashHandler
 * @Description: 当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告
 * @author J.Beyond
 * @date 2015-3-11 下午3:54:48
 *
 */
public class CrashHandler implements UncaughtExceptionHandler{

	 /** Debug Log Tag */  
    public static final String TAG = "CrashHandler";  
    /** 是否开启日志输出, 在Debug状态下开启, 在Release状态下关闭以提升程序性能 */  
    public static final boolean DEBUG = true;  
    /** CrashHandler实例 */  
    private static CrashHandler INSTANCE;  
    /** 程序的Context对象 */  
    private Context mContext;  
    /** 系统默认的UncaughtException处理类 */  
    private Thread.UncaughtExceptionHandler mDefaultHandler;  

    /** 错误报告文件的扩展名 */  
    private static final String CRASH_REPORTER_EXTENSION = ".txt";  
    private static String path = Environment.getExternalStorageDirectory() + File.separator+ "JDReader"+File.separator+"CrashLog"+File.separator;
      
    /** 保证只有一个CrashHandler实例 */  
    private CrashHandler() {  
    }  
  
    /** 获取CrashHandler实例 ,单例模式 */  
    public static CrashHandler getInstance() {  
        if (INSTANCE == null)  
            INSTANCE = new CrashHandler();  
        return INSTANCE;  
    }  
      
    /** 
     * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器 
     *  
     * @param ctx 
     */  
    public void init(Context ctx) {  
        mContext = ctx;  
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();  
        Thread.setDefaultUncaughtExceptionHandler(this);
    }  
      

	/** 
     * 当UncaughtException发生时会转入该函数来处理 
     */  
    @Override  
    public void uncaughtException(Thread thread, Throwable ex) {  
        if (!handleException(ex) && mDefaultHandler != null) {  
            // 如果用户没有处理则让系统默认的异常处理器来处理  
            mDefaultHandler.uncaughtException(thread, ex);  
        } else {  
            // Sleep一会后结束程序  
            // 来让线程停止一会是为了显示Toast信息给用户，然后Kill程序  
            try {  
                Thread.sleep(3000);  
            } catch (InterruptedException e) {  
                Log.e(TAG, "Error:", e);  
            }
            //注意：这里需要杀进程，否则home键退到桌面，点图标，会报异常：Java.lang.IllegalThreadStateException:thread already started
            android.os.Process.killProcess(android.os.Process.myPid());  
            System.exit(10); 
        }  
    }  
  
    /** 
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑 
     *  
     * @param ex 
     * @return true:如果处理了该异常信息;否则返回false 
     */  
    private boolean handleException(Throwable ex) {  
        if (ex == null) {  
            return true;  
        }  
        final String msg = ex.getLocalizedMessage();  
        // 使用Toast来显示异常信息  
        new Thread() {  
            @Override  
            public void run() {  
                // Toast 显示需要出现在一个线程的消息队列中  
                Looper.prepare();  
//                Toast.makeText(mContext, "程序出错啦:" + msg, Toast.LENGTH_LONG).show();  
                Looper.loop();  
            }  
        }.start();  
        // 收集设备信息  
//        collectCrashDeviceInfo(mContext);  
        // 保存错误报告文件  
//        String crashFileName = saveCrashInfoToFile(ex); 
        extractLogToFile(ex);
        // 发送错误报告到服务器  
//        sendCrashReportsToServer(mContext);  
        return true;  
    }
    
	private String extractLogToFile(Throwable ex) {
		PackageManager manager = mContext.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(mContext.getPackageName(), 0);
		} catch (NameNotFoundException e2) {
		}
		String model = Build.MODEL;
		if (!model.startsWith(Build.MANUFACTURER))
			model = Build.MANUFACTURER + " " + model;

		// Make file name - file must be saved to external storage or it wont be
		// readable by
		// the email app.
		if (!hasSDCard()) {
			return null;
		}
		
		File fileDir = new File(path);
		if (!fileDir.exists()) {
			fileDir.mkdir();
		}else {
			//判断该目录下的文件是否存在并且数量是否等于3
			if (fileDir.isDirectory()) {
				File[] fs = fileDir.listFiles();
	    		if (fs.length==5) {
	    			//按修改时间排序
	    			Arrays.sort(fs, new Comparator<File>() {
	    				public int compare(File f1, File f2) {
	    					long diff = f1.lastModified() - f2.lastModified();
	    					if (diff > 0)
	    						return 1;
	    					else if (diff == 0)
	    						return 0;
	    					else
	    						return -1;
	    				}

	    				public boolean equals(Object obj) {
	    					return true;
	    				}
	    			});
	    			
	    			for (int i = 0; i <fs.length; i++) {
	    				MZLog.d("crashhandler",fs[i].getName());
	    			}
	    			File oldestFile = fs[0];
	    			oldestFile.delete();
				}
			}
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = df.format(new Date(System.currentTimeMillis()));
		String fullName = path + "/crash-" + dateStr
				+ CRASH_REPORTER_EXTENSION;
		
		// Extract to file.
		InputStreamReader reader = null;
		FileWriter writer = null;
		try {
			String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ? "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s"
					: "logcat -d -v time";

			// get input stream
			Process process = Runtime.getRuntime().exec(cmd);
			reader = new InputStreamReader(process.getInputStream());

			// write output stream
			writer = new FileWriter(fullName);
			writer.write("Android version: " + Build.VERSION.SDK_INT + "\n");
			writer.write("Device: " + model + "\n");
			writer.write("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");
			writer.write("Cause by:"+ex.getLocalizedMessage());
			char[] buffer = new char[10000];
			do {
				int n = reader.read(buffer, 0, buffer.length);
				if (n == -1)
					break;
				writer.write(buffer, 0, n);
			} while (true);

			reader.close();
			writer.close();
		} catch (IOException e) {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e1) {
				}
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e1) {
				}

			return null;
		}

		return fullName;
	}

	private boolean hasSDCard(){
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	
}
