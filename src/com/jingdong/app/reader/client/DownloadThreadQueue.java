package com.jingdong.app.reader.client;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.util.MZLog;

/**
 * 下载任务队列
 *
 */
public class DownloadThreadQueue extends Thread {
	private RefreshAble refreshAblerE = null;
	public DownloadThread currentDownloadThread = null;
	public Hashtable<String, DownloadedAble> hashTable = new Hashtable<String, DownloadedAble>();
	private PriorityBlockingQueue<DownloadedAble> pendingRequests = new PriorityBlockingQueue<DownloadedAble>();

	public DownloadThreadQueue() {
	}
	
	/**
	 * 线程任务<br />
	 * 1、启动循环（直到停止）<br />
	 */
	public void run() {
		System.out.println("DDDDDDDD===DownloadThreadQueue=====run=====1111===");			
		while (!isStop) {
			try {
				synchronized (this) {
					this.wait(1000);//睡眠1秒
					if (isStop) {
						break;
					}
				}
				if (currentDownloadThread == null || !currentDownloadThread.isloading()) {
					//从下载任务池里边拿任务
					downloadAbler = poll();
					if (downloadAbler == null || downloadAbler.getDownloadStatus() == DownloadedAble.STATE_LOAD_PAUSED) {
						continue;
					}
					// 判断下载文件类型，初始化下载线程
					if (downloadAbler.getType() == DownloadedAble.TYPE_BOOK) {
						currentDownloadThread = new DownloadThreadBook(this, downloadAbler);
					} else if (downloadAbler.getType() == DownloadedAble.TYPE_DOCUMENT) {
						currentDownloadThread = new DownloadThreadDocument(this, downloadAbler);
					}
					else {
						currentDownloadThread = new DownloadThread(this, downloadAbler);
					}
					//启动下载
					currentDownloadThread.start();
					currentDownloadThread.setloading(true);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 添加下载任务并启动开始下载
	 * @param downloadAbler
	 */
	public void add(DownloadedAble downloadAbler) {			
		if (downloadAbler.getDownloadStatus() == DownloadedAble.STATE_UNLOAD 
				|| downloadAbler.getDownloadStatus() == DownloadedAble.STATE_LOAD_FAILED
				|| downloadAbler.getDownloadStatus() == DownloadedAble.STATE_LOAD_PAUSED
				|| downloadAbler.getDownloadStatus() == DownloadedAble.STATE_LOAD_READY 
				|| downloadAbler.getDownloadStatus() == DownloadedAble.STATE_INIT) {
			downloadAbler.setDownloadStatus((DownloadedAble.STATE_LOADING));
		} else {
			return;
		}	
		if (getState() == Thread.State.TERMINATED) {
			try {
				join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			if (!isruning) {
				start();
				isruning = true;
			}
		}
		offer(downloadAbler);
	}

	/**
	 * 增加下载任务
	 * @param downloadAbler
	 * @param isRun
	 */
	public void add(DownloadedAble downloadAbler, boolean isRun) {
		if (downloadAbler.getDownloadStatus() == DownloadedAble.STATE_UNLOAD /*未下载*/ || 
				downloadAbler.getDownloadStatus() == DownloadedAble.STATE_LOAD_FAILED /*下载失败*/|| 
				downloadAbler.getDownloadStatus() == DownloadedAble.STATE_LOAD_PAUSED /*下载暂停*/|| 
				downloadAbler.getDownloadStatus() == DownloadedAble.STATE_LOAD_READY /*准备就绪*/|| 
				downloadAbler.getDownloadStatus() == DownloadedAble.STATE_INIT/*初始化*/) {
				downloadAbler.setDownloadStatus((DownloadedAble.STATE_LOADING)/*下载中*/);
		} else {
			return;
		}
	
		MZLog.d("wangguodong", "DownloadThreadQueue start");
		offer(downloadAbler);
	}

	public boolean stop(DownloadedAble downloadAbler) {
		long bookId = downloadAbler.getId();
		boolean isok = false;
		DownloadThread downloadThread = this.currentDownloadThread;
		if (downloadThread != null && downloadThread.getDownloadAbler().getId() == bookId
				&& downloadThread.getDownloadAbler().getDownloadStatus() == DownloadedAble.STATE_LOADING) {
			downloadThread.stopDownload();
			isok = true;
			return isok;
		}
		Object[] requestEntryList = pendingRequests.toArray();
		pendingRequests.clear();

		Vector<DownloadedAble> List = new Vector<DownloadedAble>();
		for (Object obj : requestEntryList) {
			DownloadedAble downloadAble = (DownloadedAble) obj;
			if (downloadAble.getId() == bookId) {
				downloadAble.setDownloadStatus(DownloadedAble.STATE_LOAD_PAUSED);
				downloadAble.saveState();
				refresh(downloadAble);
				isok = true;
			} else {
				List.add(downloadAble);
			}
		}
		for (Object obj : List) {
			DownloadedAble DownloadAble = (DownloadedAble) obj;
			pendingRequests.offer(DownloadAble);
		}
		if (!isok) {
			downloadAbler.setDownloadStatus(DownloadedAble.STATE_LOAD_PAUSED);
			downloadAbler.manualStop();
			downloadAbler.saveState();
			refresh(downloadAbler);
		}

		return isok;
	}

	/**
	 * 刷新下载进度
	 * @param DownloadAble 下载任务信息
	 */
	public void refresh(DownloadedAble DownloadAble) {
		MZLog.d("quda", "" + DownloadAble.hashCode());
		RefreshAble refreshAbler = refreshAblerE;
		if (DownloadAble != null && refreshAbler != null) {
			refreshAbler.refresh(DownloadAble);
		}
	};

	public void refreshAll() {
		synchronized (MZBookApplication.getInstance()) {
			for (DownloadedAble DownloadAble : pendingRequests) {
				DownloadAble.setCopy(null);
				refresh(DownloadAble);
			}
			DownloadThread downloadThread = this.currentDownloadThread;
			if (downloadThread != null)
				if (downloadThread != null && downloadThread.isloading) {
					DownloadedAble lockBook = downloadThread.getDownloadAbler();
					if (lockBook != null) {
						lockBook.setCopy(null);
						refresh(lockBook);
					}
				}

		}
	}

	/**
	 * 下载过程当中刷新信息接口
	 *
	 */
	public interface RefreshAble {
		public void refresh(DownloadedAble DownloadAble);
		public int getType();
		public void refreshDownloadCache();
	}

	public interface PregressAble {
		public void setProgress(int progress, int state);

		public void refreshSize(long size);

		public boolean verify(long id);
	}

	public void offer(DownloadedAble downloadAbler) {
		System.out.println("DDDDDDDD===DownloadThreadQueue=====offer=====1111===");			
		synchronized (MZBookApplication.getInstance()) {
			if (!pendingRequests.contains(downloadAbler) && !hashTable.containsKey(downloadAbler.getId())) {
				downloadAbler.setDownloadStatus(DownloadedAble.STATE_LOADING);
				downloadAbler.saveState();
				refresh(downloadAbler);
				System.out.println("DDDDDDDD===DownloadThreadQueue=====offer=====2222===");				
				hashTable.put("" + downloadAbler.getId(), downloadAbler);
				pendingRequests.offer(downloadAbler);
			} 
		}
	}

	/**
	 * 从下载任务池里边拿任务
	 * @return
	 */
	public DownloadedAble poll() {
		synchronized (MZBookApplication.getInstance()) {
			try {
				DownloadedAble DownloadAbler = pendingRequests.poll(100, TimeUnit.MILLISECONDS);
				if (DownloadAbler != null && hashTable.contains(DownloadAbler.getId())) {
					hashTable.remove(DownloadAbler);
					MZLog.d("wangguodong", "21221122 poll");
				}
				return DownloadAbler;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void stopAll() {
		Object[] requestEntryList = pendingRequests.toArray();
		pendingRequests.clear();
		for (Object oject : requestEntryList) {
			DownloadedAble downloadAbler = (DownloadedAble) oject;
			downloadAbler.setDownloadStatus(DownloadedAble.STATE_LOAD_PAUSED);
			refresh(downloadAbler);
			downloadAbler.saveState();
		}
		DownloadThread downloadThread = this.currentDownloadThread;
		if (downloadThread != null && downloadThread.isloading()) {
			downloadThread.stopDownload();
		}

	}

	public void closeThreadQueue() {
		isStop = true;
		stopAll();
		isruning = false;
	}

	public boolean inDownloadQueue(DownloadedAble downloadAbler) {
		return hashTable.containsKey(downloadAbler.getId()) || (this.downloadAbler != null && this.downloadAbler.getId() == downloadAbler.getId());
	}

	boolean isStop = false;
	boolean isruning = false;
	DownloadedAble downloadAbler;

	
	
	/**
	 * 设置下载回调
	 * @param refreshAblerE
	 */
	public void setRefreshAblerE(RefreshAble refreshAblerE) {
		this.refreshAblerE = refreshAblerE;
	}
}
