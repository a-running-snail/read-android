package com.jingdong.app.reader.download.manager;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.text.TextUtils;

import com.jingdong.app.reader.download.entity.DownloadFileInfo;
import com.jingdong.app.reader.download.entity.DownloadState;
import com.jingdong.app.reader.download.entity.SpeedInfo;
import com.jingdong.app.reader.download.listener.DownloadInitListener;
import com.jingdong.app.reader.download.listener.DownloadListener;
import com.jingdong.app.reader.download.util.DownloadUtil;
import com.jingdong.app.reader.util.MZLog;

/**
 * @ClassName: CallbackManager
 * @Description: 下载回调管理类
 * @author J.Beyond
 * @date 2015年8月6日 下午4:26:59
 *
 */
public class CallbackManager {

	private static final String TAG = "CallbackManager";
	private Map<String, List<DownloadListener>> mCallbackMap = new ConcurrentHashMap<String, List<DownloadListener>>();
	private Map<String, DownloadInitListener> mInitCallbackMap = new ConcurrentHashMap<String, DownloadInitListener>();
	private Map<String, SpeedInfo> mSpeedMap = new ConcurrentHashMap<String, SpeedInfo>();
	private static CallbackManager mManager;

	private CallbackManager() {
	}

	public static CallbackManager getInstance() {
		if (mManager == null) {
			synchronized (CallbackManager.class) {
				if (mManager == null) {
					mManager = new CallbackManager();
				}
			}
		}
		return mManager;
	}

	/**
	 * 
	 * @Title: addListener
	 * @Description: 注册一个监听
	 * @param @param fileInfo
	 * @param @param listener
	 * @return void
	 * @throws
	 */
	public void addListener(DownloadFileInfo fileInfo, DownloadListener listener) {
		if (mCallbackMap.containsKey(fileInfo.getUrl())) {
			List<DownloadListener> list = mCallbackMap.get(fileInfo.getUrl());
			if (!list.contains(listener)) {
				list.add(listener);
			}
		} else {
			List<DownloadListener> list = new ArrayList<DownloadListener>();
			list.add(listener);
			mCallbackMap.put(fileInfo.getUrl(), list);
		}
		
		if (!mSpeedMap.containsKey(fileInfo.getUrl())) {
			mSpeedMap.put(fileInfo.getUrl(), new SpeedInfo());
		}
	}

	

	/**
	 * 
	 * @Title: notifyInitFailure
	 * @Description: 回调初始化失败
	 * @param @param fileInfo
	 * @param @param errorInfo
	 * @return void
	 * @throws
	 */
	public void notifyInitFailure(DownloadFileInfo fileInfo, String errorInfo) {
		if (mInitCallbackMap.containsKey(fileInfo.getUrl())) {
			DownloadInitListener initListener = mInitCallbackMap.get(fileInfo.getUrl());
			if (initListener != null) {
				initListener.onInitFail(fileInfo, errorInfo);
			}
		}
	}

	/**
	 * 
	 * @Title: notifyRefresh
	 * @Description: 通知下载更新（下载中...）
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	public synchronized void notifyRefresh(DownloadFileInfo fileInfo) {
		if (fileInfo == null) {
			return;
		}
		String url = fileInfo.getUrl();
		if (TextUtils.isEmpty(url)) {
			return;
		}
		if (mCallbackMap.containsKey(url)) {
			List<DownloadListener> listenerList = mCallbackMap.get(url);
			if (listenerList == null || listenerList.size() == 0) {
				return;
			}
			if (mSpeedMap.containsKey(url)) {
				long currentTimeMillis = System.currentTimeMillis();
				long currentFinished = fileInfo.getFinished();
				SpeedInfo speedInfo = mSpeedMap.get(url);
				double realTimeSpeed = (currentFinished - speedInfo.getLastFinished()) * 1.0 / 
						((currentTimeMillis - speedInfo.getLastTimeMillis()) / 1000.0);
				fileInfo.setRealTimeSpeed(DownloadUtil.formatSpeed(realTimeSpeed));
				speedInfo.setLastFinished(currentFinished);
				speedInfo.setLastTimeMillis(currentTimeMillis);
			}
//			for (DownloadListener listener : listenerList) {
//				fileInfo.setDownloadState(DownloadState.DOWNLOAD_ING);
//				listener.onDownloading(fileInfo);
//			}
			for (int i = 0; i < listenerList.size(); i++) {
				fileInfo.setDownloadState(DownloadState.DOWNLOAD_ING);
				DownloadListener listener = listenerList.get(i);
				if (listener != null) {
					listener.onDownloading(fileInfo);
				}
			}
		}
	}
	
	/**
	 * 
	 * @Title: notifyMobileNetConfirm
	 * @Description: 移动流量网络下载确认
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	public void notifyMobileNetConfirm(DownloadFileInfo fileInfo) {
		String url = fileInfo.getUrl();
		if (TextUtils.isEmpty(url)) {
			return;
		}
		if (mCallbackMap.containsKey(url)) {
			List<DownloadListener> listenerList = mCallbackMap.get(url);
			if (listenerList == null || listenerList.size() == 0) {
				return;
			}
			for (DownloadListener listener : listenerList) {
				if (listener != null) {
					listener.onMobileNetConfirm(fileInfo);
				}
			}
			
		}
	}

	

	/**
	 * 
	 * @Title: notifyPause
	 * @Description: 通知下载暂停
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	public void notifyPause(DownloadFileInfo fileInfo) {
		if (fileInfo == null) {
			return;
		}
		MZLog.d(TAG, "notifyPause::fileInfo=" + fileInfo.toString());
		if (mSpeedMap.containsKey(fileInfo.getUrl())) {
			SpeedInfo speedInfo = mSpeedMap.get(fileInfo.getUrl());
			speedInfo.reset();
		}
		fileInfo.setDownloadState(DownloadState.DOWNLOAD_PAUSE);
		notifyChange(fileInfo, null);
	}

	/**
	 * 
	 * @Title: notifyPause
	 * @Description: 通知下载暂停
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	public synchronized void notifyFailure(DownloadFileInfo fileInfo, String errorInfo) {
		if (fileInfo == null) {
			return;
		}
		MZLog.d(TAG, "notifyFailure::fileInfo=" + fileInfo.toString());
		if (mSpeedMap.containsKey(fileInfo.getUrl())) {
			SpeedInfo speedInfo = mSpeedMap.get(fileInfo.getUrl());
			speedInfo.reset();
		}
		fileInfo.setDownloadState(DownloadState.DOWNLOAD_FAILURE);
		notifyChange(fileInfo, errorInfo);
	}

	/**
	 * 
	 * @Title: notifyFinished
	 * @Description: 通知下载完成
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	public void notifyFinished(DownloadFileInfo fileInfo) {
		if (fileInfo == null) {
			return;
		}
		MZLog.d(TAG, "notifyFinished::fileInfo=" + fileInfo.toString());
		if (mSpeedMap.containsKey(fileInfo.getUrl())) {
			mSpeedMap.remove(fileInfo.getUrl());
		}
		fileInfo.setDownloadState(DownloadState.DOWNLOAD_SUCCESS);
		notifyChange(fileInfo, null);
	}

	/**
	 * 
	 * @Title: notifyWait
	 * @Description: 通知等待
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	public synchronized void notifyWait(DownloadFileInfo fileInfo) {
		if (fileInfo == null) {
			return;
		}
		MZLog.d(TAG, "notifyWait::fileInfo=" + fileInfo.toString());
		if (mSpeedMap.containsKey(fileInfo.getUrl())) {
			SpeedInfo speedInfo = mSpeedMap.get(fileInfo.getUrl());
			speedInfo.reset();
		}
		fileInfo.setDownloadState(DownloadState.DOWNLOAD_WAIT);
		notifyChange(fileInfo, null);
	}

	/**
	 * @param errorInfo
	 * 
	 * @Title: notifyChange
	 * @Description: 通用的回调出口
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	private void notifyChange(DownloadFileInfo fileInfo, String errorInfo) {
		String url = fileInfo.getUrl();
		if (TextUtils.isEmpty(url)) {
			return;
		}
		if (mCallbackMap.containsKey(url)) {
			List<DownloadListener> listenerList = mCallbackMap.get(url);
			if (listenerList == null || listenerList.size() == 0) {
				return;
			}
			for (int i = 0; i < listenerList.size(); i++) {
				DownloadListener listener = listenerList.get(i);
				switch (fileInfo.getDownloadState()) {
				case DownloadState.DOWNLOAD_FAILURE:
					fileInfo.setRealTimeSpeed("");
					listener.onDownloadError(fileInfo, errorInfo);
					break;
//				case DownloadState.DOWNLOAD_ING:
//					fileInfo.setDownloadState(DownloadState.DOWNLOAD_ING);
//					listener.onDownloading(fileInfo);
//					break;
				case DownloadState.DOWNLOAD_PAUSE:
					fileInfo.setRealTimeSpeed("");
					listener.onDownloadPause(fileInfo);
					;
					break;
				case DownloadState.DOWNLOAD_SUCCESS:
					fileInfo.setRealTimeSpeed("");
					listener.onDownloadFinished(fileInfo);
					;
					break;
				case DownloadState.DOWNLOAD_WAIT:
					fileInfo.setRealTimeSpeed("");
					listener.onDownloadWait(fileInfo);
					break;

				default:
					break;
				}
			}
		}
	}

}
