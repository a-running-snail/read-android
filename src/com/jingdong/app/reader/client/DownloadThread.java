package com.jingdong.app.reader.client;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.util.MZLog;


/**
 * 下载线程
 *
 */
public class DownloadThread extends Thread {
	public static String KEY_MESSAGE = "error";

	private DownloadedAble downloadAbler;
	public static boolean sIsnotice = false;
	protected boolean isloading = false;
	protected DownloadThreadQueue downloadThreadQueue;

	/**
	 * 开始下载
	 */
	@Override
	public void run() {
		handleLoad(downloadAbler);
	}
	
	public DownloadedAble getDownloadAbler() {
		return downloadAbler;
	}
	public DownloadThread(DownloadThreadQueue downloadThreadQueue,DownloadedAble downloadAbler) {
		this.downloadAbler = downloadAbler;
		this.downloadThreadQueue = downloadThreadQueue;
	}

	private void loadContent(final DownloadedAble downloadAbler) {

		boolean isSuccess = false;
		RequestEntry entry = null;
		MZLog.i("DownloadThread", "getBookContent 0000");
		try {

			if (downloadAbler == null || downloadAbler.getUrl() == null) {
				return;
			}
			HttpUriRequest request = new HttpGet(downloadAbler.getUrl());
			MZLog.d("DownloadThread", "downloadAbler.getUrl()====" + downloadAbler.getUrl());
			entry = new RequestEntry(RequestEntry.REQUEST_TYPE_SEARCH_POST_BY_JSON, request);
			downloadAbler.setRequestEntry(entry);
			entry._type = RequestEntry.TYPE_FILE;
			MZLog.d("DownloadThread", "getBookContent 111111");

			entry._downloadListener = new OnDownloadListener() {
				@Override
				public void onprogress(long progress, long max) {
					downloadAbler.setCurrentSize(progress);
					if (downloadAbler.getTotalSize() < 1) {
						downloadAbler.setTotalSize(max);
					}
					if (sIsnotice) {
						Toast.makeText(MZBookApplication.getInstance(), progress + "/" + max, Toast.LENGTH_SHORT).show();
					}
					downloadThreadQueue.refresh(downloadAbler);
				}

				@Override
				public void onDownloadCompleted(RequestEntry requestEntry) {
				}
			};
			entry.fileGuider= downloadAbler.creatFileGuider();
			File file = entry.fileGuider.getFile();
			if (downloadAbler.getTotalSize() <= 0||downloadAbler.getCurrentSize()>=downloadAbler.getTotalSize()) {
				delFile(file);
			}

			FileInputStream fis = null;
			if (file != null && file.exists()) {
				fis = new FileInputStream(file);
			}
			if (fis != null && fis.available() > 10&&fis.available()<downloadAbler.getTotalSize()) {
				entry.start = fis.available();
				downloadAbler.setCurrentSize(fis.available());
				entry.end = downloadAbler.getTotalSize();
			} else {
				delFile(file);
				downloadAbler.setCurrentSize(0);
			}
			entry.start = downloadAbler.getCurrentSize();
			entry.end = downloadAbler.getTotalSize();
			if (entry.start > 0) {
				MZLog.d("HttpGroup", "httpSetting.start======" + entry.start);
				MZLog.d("HttpGroup", "httpSetting.end======" + entry.end);
				request.addHeader("Range", "bytes=" + entry.start + "-");
			}
			MZLog.d("DownloadThread", "getBookContent 3333333333");
			RequestEntry requestEntry = ServiceClient.execute(entry);
			if (entry.getRequestCode() != downloadAbler.getRequestCode()) {
				return;
			}
			if (requestEntry._statusCode == 0) {
				isSuccess = true;
			} else {
				isSuccess = false;
				if (!TextUtils.isEmpty(requestEntry._stateNotice)) {
					Toast.makeText(MZBookApplication.getInstance(), requestEntry._stateNotice, Toast.LENGTH_SHORT).show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int state = DownloadedAble.STATE_LOAD_FAILED;
		if (downloadAbler.getDownloadStatus() == DownloadedAble.STATE_LOAD_PAUSED) {
			state = DownloadedAble.STATE_LOAD_PAUSED;
		} else if (entry.isSuccess && downloadAbler.getCurrentSize() == downloadAbler.getTotalSize() && downloadAbler.getTotalSize()>0 
				&& downloadAbler.getFilePath() != null) {
				state = DownloadedAble.STATE_LOADED;
		}

		if (state == DownloadedAble.STATE_LOAD_PAUSED) {
			MZLog.d("DownloadThread", "书籍下载暂停");
		} else if (state == DownloadedAble.STATE_LOADED) {
			MZLog.d("DownloadThread", "书籍下载成功");
		} else {
			MZLog.d("DownloadThread", "书籍下载失败");
		}
		downloadAbler.setDownloadStatus(state);
		handleLoad(downloadAbler);
	}
	
	    static void delFile(String path){
		   if(TextUtils.isEmpty(path)){
			   return;
		   }
			File file = new File(path);
			delFile(file);
		}

	   
	    static void delFile(File file){
		   if(file==null){
			   return;
		   }
			if (file.exists()) {
				file.delete();
			}
		}
	   
	public void stopDownload() {
		if (downloadAbler != null) {
			downloadAbler.manualStop();
		}
		try {
			//停止下载线程
			this.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void handleLoad(DownloadedAble downloadAbler) {
		handleLoad(downloadAbler, false, false);
	}

	public void handleLoad(DownloadedAble downloadAbler, boolean isPassBigPic,
			boolean isPassSmallPic) {
		if (downloadAbler.isManualStop()) {
			isloading = false;
			return;
		}
		//刷新状态
		downloadThreadQueue.refresh(downloadAbler);
		MZLog.d("DownloadThread", "handleLoad 99999");
		if (downloadAbler.getDownloadStatus() == LocalBook.STATE_LOAD_PAUSED
				|| downloadAbler.getDownloadStatus() == LocalBook.STATE_LOAD_FAILED
				|| downloadAbler.getDownloadStatus() == LocalBook.STATE_LOADED) {
			downloadAbler.saveLoadTime(System.currentTimeMillis());
			downloadAbler.save();
			isloading = false;
			MZLog.d("DownloadThread", "handleLoad 00000");
			return;
		} else if (TextUtils.isEmpty(downloadAbler.getFilePath()) || downloadAbler.getDownloadStatus() != LocalBook.STATE_LOADED) {
			MZLog.d("DownloadThread", "handleLoad 33333333333");
			loadContent(downloadAbler);
		}
	}


	public void setloading(boolean isloading) {
		this.isloading = isloading;
	}

	
	public boolean isloading() {
		return isloading;
	}
	
	
	
}
