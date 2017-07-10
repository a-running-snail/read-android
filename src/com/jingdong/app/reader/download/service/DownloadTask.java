package com.jingdong.app.reader.download.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;

import com.jingdong.app.reader.download.db.ThreadDAO;
import com.jingdong.app.reader.download.db.ThreadDAOImpl;
import com.jingdong.app.reader.download.entity.DownloadFileInfo;
import com.jingdong.app.reader.download.entity.ThreadInfo;
import com.jingdong.app.reader.download.manager.CallbackManager;
import com.jingdong.app.reader.download.util.L;
import com.jingdong.app.reader.download.util.NetUtil;

/**
 * 
 *
 *
 *                #####################################################
 *                #                                                   #
 *                #                       _oo0oo_                     #
 *                #                      o8888888o                    #
 *                #                      88" . "88                    #
 *                #                      (| -_- |)                    #
 *                #                      0\  =  /0                    #
 *                #                    ___/`---'\___                  #
 *                #                  .' \\|     |# '.                 #
 *                #                 / \\|||  :  |||# \                #
 *                #                / _||||| -:- |||||- \              #
 *                #               |   | \\\  -  #/ |   |              #
 *                #               | \_|  ''\---/''  |_/ |             #
 *                #               \  .-\__  '-'  ___/-. /             #
 *                #             ___'. .'  /--.--\  `. .'___           #
 *                #          ."" '<  `.___\_<|>_/___.' >' "".         #
 *                #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 *                #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 *                #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 *                #                       `=---='                     #
 *                #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 *                #                                                   #
 *                #               佛祖保佑         永无BUG              #
 *                #                                                   #
 *                #####################################################
 *
 *
 *
 * @ClassName: DownloadTask
 * @Description: 下载任务
 * @author J.Beyond
 * @date 2015年8月7日 下午8:30:12
 *
 */
public class DownloadTask {

	private Context mContext;
	private DownloadFileInfo mFileInfo;
	// public boolean isPause = false;
	private ThreadDAO mDao;
	public long mFinished = 0;
	public long mLastFinished = 0;
	private int mThreadCount;
	// 一个ThreadInfo对应一个DownloadThread
	private Map<Integer, DownloadThread> mThreadMap = new ConcurrentHashMap<Integer, DownloadThread>();
	private boolean mAcceptRanges;
	// 线程池
	public static ExecutorService sExecutorService = Executors.newCachedThreadPool();

	public DownloadTask(Context mContext, DownloadFileInfo mFileInfo, int threadCount) {
		super();
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		this.mThreadCount = threadCount;
		this.mDao = new ThreadDAOImpl(mContext);
	}

	/**
	 * @Title: doDownload
	 * @Description: 执行下载和继续下载
	 * @param 
	 * @return void
	 * @throws
	 */
	public synchronized void doDownload() {
		mFinished = 0;
		pausedThreadCount = 0;
		// 读取数据库的线程信息
		List<ThreadInfo> threads = mDao.getThreads(mFileInfo.getUrl());
		if (threads.size() == 0) {// 首次下载
			// 获得每个线程下载的长度
			int length = (int) (mFileInfo.getLength() / mThreadCount);
			String url = mFileInfo.getUrl();
			for (int i = 0; i < mThreadCount; i++) {
				// 创建线程信息
				int start = i * length;
				int end = (i + 1) * length - 1;
				ThreadInfo threadInfo = new ThreadInfo(i, url, start, end, 0, mFileInfo.getLength());
				threadInfo.setAcceptRange(mFileInfo.isAcceptRanges()?0:-1);
				if (i == mThreadCount - 1) {
					threadInfo.setEnd(mFileInfo.getLength());
				}
				// 添加到线程集合中
				threads.add(threadInfo);
				// 向数据库中插入线程信息
				mDao.insertThreadInfo(threadInfo);
			}
		}
		for (ThreadInfo threadInfo : threads) {
			threadInfo.setRetryCount(0);
			// 文件下载的总进度
			mFinished += threadInfo.getFinished();
			mFileInfo.setAcceptRanges(threadInfo.getAcceptRange() == 0 ? true:false);
			DownloadThread thread = null;
			//判断mThreadMap中是否存在threadInfo，有则从map中获取DownloadThread，没有就创建一个Entry，放入Map中
			if (mThreadMap.containsKey(threadInfo.getId())) {
				for (Entry<Integer, DownloadThread> entry : mThreadMap.entrySet()) {
					if (threadInfo.getId()==entry.getKey()) {
						thread = entry.getValue();
						break;
					}
				}
			}else{
				//state=0表示未下载完，1表示下载完毕
				if (threadInfo.getState() == 0) {
					thread = new DownloadThread(threadInfo);
					mThreadMap.put(threadInfo.getId(), thread);
				}
			}
			if (thread != null) {
				thread.isPause = false;
				DownloadTask.sExecutorService.execute(thread);
				// 通知等待
				CallbackManager.getInstance().notifyWait(mFileInfo);
				L.d("【" + mFileInfo.getFileName() + "】开始下载--线程数[" + mThreadCount + "]");
			}
		}
	}

	/**
	 * 
	 * @Title: pause
	 * @Description: 暂停下载
	 * @param 
	 * @return void
	 * @throws
	 */
	public synchronized void doPause() {
		// 只有支持断点下载的文件才能暂停
		if (mFileInfo.isAcceptRanges()) {
			for (Entry<Integer, DownloadThread> entry : mThreadMap.entrySet()) {
				DownloadThread thread = entry.getValue();
				if (thread != null) {
					thread.isPause = true;
				}
			}
		}else{
			L.e("该文件不支持断点下载，因此不能中途暂停");
		}
	}

	/**
	 * 下载线程
	 * 
	 * @author Beyond
	 *
	 */
	class DownloadThread extends Thread {
		private ThreadInfo mThreadInfo;
		public boolean isFinished = false;// 标识本线程是否下载完毕
		public boolean isFailure = false;// 标识本线程是否下载失败
		public boolean isPause = false;// 标识本线程是否下载暂停
		public int callbackInterval = 300;//每个线程回调间隔

		public DownloadThread(ThreadInfo mThreadInfo) {
			super();
			this.mThreadInfo = mThreadInfo;
			this.callbackInterval = mThreadCount * 200;
		}

		@SuppressWarnings("resource")
		@Override
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream inputStream = null;
			try {
				boolean acceptRanges = mFileInfo.isAcceptRanges();
				conn = NetUtil.buildConnection(mThreadInfo.getUrl());
//				设置线程下载的起始位置
				long start = mThreadInfo.getStart() + mThreadInfo.getFinished();
				if(acceptRanges){//支持多线程下载
					conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
				}
				int statusCode = conn.getResponseCode();
				if(statusCode == 206 || (statusCode == 200 && !acceptRanges)){
					isFailure = false;
					
					// 设置文件写入位置
					File file = new File(mFileInfo.getStoragePath(), mFileInfo.getFileName());
					raf = new RandomAccessFile(file, "rwd");
					raf.seek(start);
					
					//读取网络数据流
					inputStream = conn.getInputStream();
					byte[] buffer = new byte[1024 * 4 * 2];
					int len = -1;
					long time = System.currentTimeMillis();
					while ((len = inputStream.read(buffer)) != -1) {
						// 写入文件
						raf.write(buffer, 0, len);
						// 累加整个文件完成进度
						mFinished += len;
						// 累加每个线程完成的进度
						mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
//						L.d(mThreadInfo.getId()+"--finished:"+mThreadInfo.getFinished());
						L.d("isFailure:"+isFailure);
						// 每隔1000s回调一次
						if (System.currentTimeMillis() - time > callbackInterval) {
							time = System.currentTimeMillis();
							int progress = (int) (mFinished * 100 / mThreadInfo.getTotal());
							L.d("progress:"+progress);
							mFileInfo.setFinished(mFinished);
							mFileInfo.setProgress(progress);
							CallbackManager.getInstance().notifyRefresh(mFileInfo);
						}
						// 下载暂停时，保存下载进度
						if (isPause) {
							processPause(mThreadInfo);
							return;
						}
					}
					// 标识下载完毕
					isFinished = true;
				}else{
					isFailure = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (e!=null) {
					L.e(e);
				}
				isFailure = true;
			}finally {
				try {
					//更新数据库
					mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished(),isFinished?1:0);
					
					if (isFailure) {
						processFailure(mThreadInfo);
					}
					if (isFinished) {
						processFinished(mThreadInfo);
					}
					if (conn != null) {
						conn.disconnect();
						conn = null;
					}
					if (raf != null) {
						raf.close();
						raf = null;
					}
					if (inputStream != null) {
						inputStream.close();
						inputStream = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	int pausedThreadCount=0;
	/**
	 * 
	 * @Title: processPause
	 * @Description: 处理暂停
	 * @param @param threadInfo
	 * @return void
	 * @throws
	 */
	private synchronized void processPause(ThreadInfo threadInfo) {
		//更新数据库
//		mDao.updateThread(threadInfo.getUrl(), threadInfo.getId(), threadInfo.getFinished(),0);
		pausedThreadCount++;
		if (pausedThreadCount == mThreadMap.size()) {
			int progress = (int) (mFinished * 100 / threadInfo.getTotal());
			mFileInfo.setProgress(progress);
			CallbackManager.getInstance().notifyPause(mFileInfo);
			L.d("【" + mFileInfo.getFileName() + "】下载暂停--progress[" + progress + "%]");
		}
	}
	
	/**
	 * 
	 * @Title: processFailure
	 * @Description: 处理下载失败：
	 * 		若某个线程下载失败，则重试三次，重试三次失败后则标识本线程下载失败，然后让所有下载线程暂停
	 * @param @param threadInfo
	 * @return void
	 * @throws
	 */
	private synchronized void processFailure(ThreadInfo threadInfo) {
		if (threadInfo.getRetryCount() == 3) {
			threadInfo.setRetryCount(0);
			L.e("线程["+threadInfo.getId()+"]下载失败");
			CallbackManager.getInstance().notifyFailure(mFileInfo,"线程["+threadInfo.getId()+"]下载失败");
			return;
		}
		//设置失败次数
		threadInfo.setRetryCount(threadInfo.getRetryCount()+1);
		L.d("线程["+threadInfo.getId()+"]下载失败，进入重试，重试次数："+threadInfo.getRetryCount());
		DownloadThread thread = null;
		if (mThreadMap.containsKey(threadInfo.getId())) {
			thread = mThreadMap.get(threadInfo.getId());
			if (thread == null) {
				thread = new DownloadThread(threadInfo);
			}
		}else{
			thread = new DownloadThread(threadInfo);
		}
		//加入线程池
		sExecutorService.execute(thread);
	}

	private void pauseAll() {
		// 只有支持断点下载的文件才能暂停
		if (mFileInfo.isAcceptRanges()) {
			for (Entry<Integer, DownloadThread> entry : mThreadMap.entrySet()) {
				DownloadThread thread = entry.getValue();
				if (thread != null) {
					thread.isPause = true;
				}
			}
		}else{
			L.e("该文件不支持断点下载，因此不能中途暂停");
		}
	}

	/**
	 * 
	 * @Title: processFailure
	 * @Description: 处理失败
	 * @param @param mThreadInfo
	 * @return void
	 * @throws
	 */
	private synchronized void processFailure(ThreadInfo mThreadInfo,String errorInfo) {
		boolean allFailure = true;
		for (Entry<Integer, DownloadThread> entry : mThreadMap.entrySet()) {
			DownloadThread downloadThread = entry.getValue();
			if (downloadThread.isFailure) {
				allFailure = false;
				break;
			}
		}
		if (allFailure) {
			CallbackManager.getInstance().notifyFailure(mFileInfo,errorInfo);
			L.d("【" + mFileInfo.getFileName() + "】下载失败！errorInfo:"+errorInfo);
		}
	}

	/**
	 * 判断是否所有线程都执行完毕
	 * 
	 * @param mThreadInfo
	 */
	private synchronized void processFinished(ThreadInfo mThreadInfo) {
		try {
			L.i("Thread[" + mThreadInfo.getId() + "] download complete!!");
			Iterator<Integer> iterator = mThreadMap.keySet().iterator();
			while (iterator.hasNext()) {
				Integer id = iterator.next();
				if (id == mThreadInfo.getId()) {
					iterator.remove();
				}
			}
			if (mThreadMap.size() == 0) {
				// 删除数据库中的线程信息
				// mDao.deleteThread(mFileInfo.getUrl());
				mFileInfo.setProgress(100);
				// 发送广播通知UI下载任务结束
				CallbackManager.getInstance().notifyFinished(mFileInfo);
				L.i("【" + mFileInfo.getFileName() + "】下载完成！");
			}
		} catch (Exception e) {
			L.e(e);
		}
	}

}
