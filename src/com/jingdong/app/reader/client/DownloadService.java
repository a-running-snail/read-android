package com.jingdong.app.reader.client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.jingdong.app.reader.client.DownloadThreadQueue.RefreshAble;
import com.jingdong.app.reader.config.ITransKey;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.DataIntent;
import com.jingdong.app.reader.util.MZLog;

/**
 * 下载服务<br />
 * 1、Service服务维护DownloadThreadQueue队列，队列中的线程由一个优先级排序的集合组成<br />
 * 2、DownloadThreadQueue队列为一个线程，此线程DownloadThreadQueue.run()功能：<br />
 * 		1）、初始化DownloadThread线程，并启动<br /> 
 * 		2）、循环若已启动的话，每隔一秒进行睡眠，非睡眠时间处理UI的刷新<br />
 * 3、DownloadThread为下载线程(有两种情况，一为下载京东电子书，二为下载自有文档)<br />
 * 		1）、下载京东电子书，初始化DownloadThreadBook，并启动<br />
 * 
 * 4、DownloadThreadBook <br />
 * 		1）、请求引导文件（包含下载证书地址以及请求下载地址的链接地址）<br />
 * 		2）、请求证书<br />
 * 		3）、请求下载地址<br />
 * 		4）、请求内容文件（DownloadThreadBook 的 getBookContent方法），下载的过程当中，调用回调方法刷新界面<br />
 * 
 */
public class DownloadService extends Service implements ITransKey {

	/**
	 * 下载队列
	 */
	static DownloadThreadQueue[] downloadThreadQueues;

	/**
	 * 启动服务
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		System.out.println("DDDDDDDD===DownloadService=====onStart========");		
		if (intent == null) {
			return;
		}
		Long bookID = intent.getLongExtra(KEY, -1);
		int type = intent.getIntExtra("type", -1);
		DownloadedAble downloadAbler = null;

		if (type == LocalDocument.TYPE_DOCUMENT) {// 2015-1-8添加下载document
			if (bookID > -1) {
				MZLog.d("wangguodong", "service bookid:" + bookID);
				downloadAbler = LocalDocument.getLocalDocument(Integer.parseInt(bookID + ""));
				if (downloadAbler != null) {
					MZLog.d("wangguodong", "service document type:" + downloadAbler.getType());
				}

			} else {
				String key = intent.getStringExtra(KEY1);
				if (key != null) {
					downloadAbler = (DownloadedAble) DataIntent.get(key);
				}
			}
		} else {
			if (bookID > -1) {
				downloadAbler = LocalBook.getLocalBook(bookID, LoginUser.getpin());
			} else {
				String key = intent.getStringExtra(KEY1);
				if (key != null) {
					downloadAbler = (DownloadedAble) DataIntent.get(key);
				}
			}
		}

		if (downloadAbler == null) {
			return;
		}
		// 尝试本地sd中是否存在正在下载的文件。
		boolean tryGetContetnFromSD = intent.getBooleanExtra(KEY2, false);
		downloadAbler.setTryGetContetnFromSD(tryGetContetnFromSD);
		creatTQueuesIfNoExist();
		if (downloadThreadQueues[downloadAbler.getType()].isAlive() || downloadThreadQueues[downloadAbler.getType()].isruning) {
			downloadThreadQueues[downloadAbler.getType()].add(downloadAbler, true);
		}else {
			//开始下载（添加了之后，线程会Start）
			downloadThreadQueues[downloadAbler.getType()].add(downloadAbler);
		}
	}

	/**
	 * 初始化队列
	 */
	public static void creatTQueuesIfNoExist() {
		if (downloadThreadQueues == null) {
			downloadThreadQueues = new DownloadThreadQueue[3];
			downloadThreadQueues[DownloadedAble.TYPE_BOOK] = new DownloadThreadQueue();
			downloadThreadQueues[DownloadedAble.TYPE_PLUG] = new DownloadThreadQueue();
			downloadThreadQueues[DownloadedAble.TYPE_DOCUMENT] = new DownloadThreadQueue();
		}
	}

	/**
	 * 设置下载服务需要刷新的对象<br />
	 * 对外的重要接口，下载过程当中需要不定时回调
	 * @param refreshAbler 需要刷新的对象
	 * @param type 类型
	 */
	public static void setRefreshAbler(RefreshAble refreshAbler, int type) {
		creatTQueuesIfNoExist();
		if (downloadThreadQueues != null) {
			downloadThreadQueues[type].setRefreshAblerE(refreshAbler);
		}
	}

	public static boolean stop(DownloadedAble DownloadAbler) {
		if (downloadThreadQueues != null) {
			return downloadThreadQueues[DownloadAbler.getType()].stop(DownloadAbler);
		}
		return true;
	}

	public static void refresh(DownloadedAble DownloadAbler) {
		if (downloadThreadQueues != null) {
			downloadThreadQueues[DownloadAbler.getType()].refresh(DownloadAbler);
		}
	}

	public static void refreshAll(int type) {
		if (downloadThreadQueues != null) {
			downloadThreadQueues[type].refreshAll();
		}
	}

	public static void closeThreads() {
		if (downloadThreadQueues != null) {
			for (DownloadThreadQueue downloadThreadQueue : downloadThreadQueues) {
				downloadThreadQueue.closeThreadQueue();
			}
		}
		if (downloadThreadQueues != null) {
			for (int i = 0; i < downloadThreadQueues.length; i++) {
				downloadThreadQueues[i] = null;
			}
			downloadThreadQueues = null;
		}
	}

	public static boolean inDownloadQueue(DownloadedAble downloadAbler) {
		if (downloadThreadQueues != null)
			return downloadThreadQueues[downloadAbler.getType()].inDownloadQueue(downloadAbler);
		else
			return false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
