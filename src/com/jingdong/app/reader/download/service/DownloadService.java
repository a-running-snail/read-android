package com.jingdong.app.reader.download.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.jingdong.app.reader.download.cfg.DownloadConfiguration;
import com.jingdong.app.reader.download.entity.DownloadConstants;
import com.jingdong.app.reader.download.entity.DownloadFileInfo;
import com.jingdong.app.reader.download.manager.CallbackManager;
import com.jingdong.app.reader.download.util.DownloadUtil;
import com.jingdong.app.reader.download.util.L;
import com.jingdong.app.reader.download.util.NetUtil;
import com.jingdong.app.reader.download.util.StorageUtil;

/**
 *
 * @ClassName: DownloadService
 * @Description: 后台下载服务
 * @author J.Beyond
 * @date 2015年8月12日 下午5:51:28
 *
 */
public class DownloadService extends Service {

	public static final String ACTION_INIT = "action_init";
	public static final String ACTION_START = "action_start";
	public static final String ACTION_STOP = "action_stop";
	public static final String ACTION_PAUSE = "action_pause";
	public static final String ACTION_RESUME = "action_resume";
	public static final String ACTION_FINISH = "action_finish";
	public static final String ACTION_UPDATE = "action_update";
	public static final String ACTION_CONFIRM = "action_confirm";

	private static final int MSG_INIT = 0;
	// public static String sDownloadPath;
	public int mThreadCount = 1;
	// public static final String DOWNLOAD_PATH =
	// Environment.getExternalStorageDirectory().getAbsolutePath() +
	// "/downloads/";
	// 下载任务的集合
	private Map<String, DownloadTask> mTaskMap = new LinkedHashMap<String, DownloadTask>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return -1;
		}
		// 获取从activity中传过来的参数
		DownloadFileInfo fileInfo = (DownloadFileInfo) intent.getSerializableExtra("fileInfo");
		if (fileInfo == null) {
			return -1;
		}
		if (ACTION_INIT.equals(intent.getAction())) {
			L.i("init-->fileInfo::" + fileInfo.toString());
		} else if (ACTION_START.equals(intent.getAction())) {
			L.i("start-->fileInfo::" + fileInfo.toString());
			// DownloadService.sDownloadPath =
			// intent.getStringExtra("download_path");
			int netWorkType = NetUtil.getNetWorkType(this);
			if (netWorkType == DownloadConstants.NetType.INVALID) {
				// 网络不可用
				CallbackManager.getInstance().notifyInitFailure(fileInfo, "网络不可用");
			}else if (netWorkType != DownloadConstants.NetType.WIFI && !DownloadConfiguration.CONFIRM_DOWNLOAD_IN_MOBILE_NET) {
				CallbackManager.getInstance().notifyMobileNetConfirm(fileInfo);
			} else {
				// WiFi网络
				InitThread initThread = new InitThread(fileInfo);
				initThread.start();
			}
		} else if (ACTION_PAUSE.equals(intent.getAction())) {
			L.i("stop-->fileInfo::" + fileInfo.toString());
			// 从集合中取出下载任务
			DownloadTask downloadTask = mTaskMap.get(fileInfo.getUrl());
			if (downloadTask != null) {
				downloadTask.doPause();
			}
		} else if (ACTION_RESUME.equals(intent.getAction())) {
			L.i("resume-->fileInfo::" + fileInfo.toString());
			// 从集合中取出下载任务
			DownloadTask downloadTask = mTaskMap.get(fileInfo.getUrl());
			if (downloadTask == null) {
				// 新建下载任务
				downloadTask = new DownloadTask(DownloadService.this, fileInfo, 3);
				// 把下载任务添加到集合中
				mTaskMap.put(fileInfo.getUrl(), downloadTask);
			}
			downloadTask.doDownload();
		} else if (ACTION_CONFIRM.equals(intent.getAction())) {
			L.i("confirm-->fileInfo::" + fileInfo.toString());
			executeTask(fileInfo);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INIT:
				DownloadFileInfo fileInfo = (DownloadFileInfo) msg.obj;
				L.i("handler-->fileInfo::" + fileInfo);
//				// 非Wifi网络并且未确认在手机流量网络下载
//				int netWorkType = NetUtil.getNetWorkType(DownloadService.this);
//				if (netWorkType != DownloadConstants.NetType.WIFI && !DownloadConfiguration.CONFIRM_DOWNLOAD_IN_MOBILE_NET) {
//					CallbackManager.getInstance().notifyMobileNetConfirm(fileInfo);
//				} else {
					DownloadConfiguration.CONFIRM_DOWNLOAD_IN_MOBILE_NET=false;
					executeTask(fileInfo);
//				}
				break;

			default:
				break;
			}
		}

	};

	/**
	 * @param acceptRanges
	 * 
	 * @Title: executeTask
	 * @Description: 启动下载任务
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	private void executeTask(DownloadFileInfo fileInfo) {
		if (mThreadCount == 0) {
			// 设置下载线程个数
			mThreadCount = DownloadUtil.getThreadCount(fileInfo.getLength());
		}
		// 启动下载任务
		DownloadTask downloadTask = new DownloadTask(DownloadService.this, fileInfo, mThreadCount);
		downloadTask.doDownload();
		// 把下载任务添加到集合中
		mTaskMap.put(fileInfo.getUrl(), downloadTask);
	}
	
	/**
	 * judgeSourceResumeEnabled 函数 判断源是否支持断点续传
	 *
	 * @param
	 * @return boolean
	 */
	private boolean isAcceptRange(String urlStr)
	{
		HttpURLConnection conn = null;
		try{
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setAllowUserInteraction(true);
			conn.setRequestProperty("Range", "bytes=" + 0 + "-" + Integer.MAX_VALUE);

			// 设置连接超时时间为10000ms
			conn.setConnectTimeout(10000);

			// 设置读取数据超时时间为10000ms
			conn.setReadTimeout(10000);
			// 判断源是否支持断点续传
			if (conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL){
				L.i("该源不支持断点下载...");
				return false;
			}else{
				L.i("该源支持断点下载...");
				return true;
			}
		} catch (IOException e){
			e.printStackTrace();
			return false;
		}finally{
			if (conn!=null) {
				conn.disconnect();
				conn = null;
			}
		}
	}


	/**
	 * 通过网络请求获取要下载文件的长度，并在本地创建一样大小的文件
	 * 
	 * @author Beyond
	 *
	 */
	class InitThread extends Thread {
		private DownloadFileInfo mFileInfo;

		public InitThread(DownloadFileInfo mFileInfo) {
			super();
			this.mFileInfo = mFileInfo;
		}

		@SuppressWarnings("deprecation")
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			try {
				// 1.链接网络文件
				conn = NetUtil.buildConnection(mFileInfo.getUrl());
				int contentLength = -1;
				if (conn.getResponseCode() == HttpStatus.SC_OK) {
					contentLength = conn.getContentLength();
				}
				if (contentLength < 0) {
					CallbackManager.getInstance().notifyFailure(mFileInfo, "下载资源不存在!");
					return;
				}
				conn.disconnect();
				//存在SD Card
				L.d("【"+mFileInfo.getFileName()+"】文件大小："+DownloadUtil.formatFileSize(contentLength, false));
				if (StorageUtil.externalMemoryAvailable()) {
					//如果下载文件的大小大于SD剩余可用空间，下载无效
					L.d("SD Card剩余存储空间："+DownloadUtil.formatFileSize(StorageUtil.getAvailableExternalMemorySize(), false));
					if (contentLength > StorageUtil.getAvailableExternalMemorySize()) {
						CallbackManager.getInstance().notifyFailure(mFileInfo, "手机剩余空间不足！");
						return;
					}
				}else{
					L.d("手机内部剩余存储空间："+DownloadUtil.formatFileSize(StorageUtil.getAvailableInternalMemorySize(), false));
					if (contentLength > StorageUtil.getAvailableInternalMemorySize()) {
						CallbackManager.getInstance().notifyFailure(mFileInfo, "手机剩余空间不足！");
						return;
					}
				}
				
				//设置下载线程个数
				//若支持断点下载，则根据文件大小分配线程
				if (isAcceptRange(mFileInfo.getUrl())) {
					mThreadCount = DownloadUtil.getThreadCount(contentLength);
					mFileInfo.setAcceptRanges(true);
				}else{
					//若不支持断点下载，线程数为1
					mThreadCount = 1;
					mFileInfo.setAcceptRanges(false);
				}
				String storagePath = mFileInfo.getStoragePath();
				if (TextUtils.isEmpty(storagePath)) {
					L.e("下载路径不存在");
					CallbackManager.getInstance().notifyFailure(mFileInfo, "下载路径不存在");
					return;
				}
				File dir = new File(storagePath);
				if (!dir.exists()) {
					dir.mkdir();
				}
				// 3.在本地创建文件
				File file = new File(dir, mFileInfo.getFileName());
				// 随机访问的文件，可以在文件的任意一个位置进行IO操作
				raf = new RandomAccessFile(file, "rwd");
				// 4.设置本地文件长度
				raf.setLength(contentLength);
				mFileInfo.setLength(contentLength);

				mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
			} catch (Exception e) {
				L.e(e);
				CallbackManager.getInstance().notifyFailure(mFileInfo, "获取下载文件大小出错！");
			} finally {
				try {
					if (conn != null) {
						conn.disconnect();
						conn = null;
					}
					if (raf != null) {
						raf.close();
						raf = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
