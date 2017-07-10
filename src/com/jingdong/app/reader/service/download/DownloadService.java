package com.jingdong.app.reader.service.download;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.jingdong.app.reader.util.MZLog;

public class DownloadService extends Service {

	public static final String DOWNLOAD_TASK_STATE_BROADCAST = "com.mzread.download.state";
	public static final String DOWNLOAD_TASK_FINISH_BROADCAST = "com.mzread.action.downloaded";
	public static final String DOWNLOAD_ACTION_TYPE = "action_type";
	public static final String DOWNLOAD_TASK_INFO = "task_info";
	public static final String DOWNLOAD_TASK_IDENTITY = "task_identity";
	public static final String DOWNLOAD_TASK_PROGRESS = "task_progress";
	public static final String DOWNLOAD_TASK_EDITION = "task_edtion";

	public static final int TYPE_ADD = 1;
	public static final int TYPE_STOP = 2;

	public static TaskQueue mTaskQueue = new TaskQueue();
	public static List<DownloadTask> mDownloadingTasks = new ArrayList<DownloadTask>();
	public static List<DownloadTask> mPausingTasks = new ArrayList<DownloadTask>();
	private Boolean isRunning = false;
	private DownloadManager manager;

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		manager = new DownloadManager(this);
	}

	public boolean isRunning() {
		return isRunning;
	}

	public synchronized static void removeFinishedTask(String identity) {

		for (int i = 0; i < mDownloadingTasks.size(); i++) {
			if (mDownloadingTasks.get(i).getCurrentTaskInfo().getIdentity()
					.equals(identity)) {
				mDownloadingTasks.remove(i);
				Log.i("wangguodong", "移除已经完成的任务,id=" + identity + ",等待队列："
						+ mTaskQueue.size());
			}
		}

	}

	public static boolean hasTask(String identity) {

		DownloadTask task;
		// 判断downloadingqueue 是否有当前任务
		for (int i = 0; i < mDownloadingTasks.size(); i++) {
			task = mDownloadingTasks.get(i);
			if (task.getCurrentTaskInfo().getIdentity().equals(identity)) {
				return true;
			}
		}
		// 判断waitingqueue 是否有当前任务
		for (int i = 0; i < mTaskQueue.size(); i++) {
			task = mTaskQueue.get(i);
			if (task.getCurrentTaskInfo().getIdentity().equals(identity)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (manager == null)
			manager = new DownloadManager(this);
		manager.startManage();

		if (intent != null) {

			int type = intent.getIntExtra(DOWNLOAD_ACTION_TYPE, -1);
			switch (type) {

			case TYPE_ADD:
				TaskInfo info = (TaskInfo) intent
						.getSerializableExtra(DOWNLOAD_TASK_INFO);
				if (info != null)
					manager.addTask(info);
				break;

			case TYPE_STOP:
				// mDownloadManager.close();
				// mDownloadManager = null;
				break;

			}
		}

		return super.onStartCommand(intent, flags, startId);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public class DownloadManager extends Thread {

		Context mContext;

		public DownloadManager(Context context) {
			this.mContext = context;
		}

		public void startManage() {

			if (!isRunning()) {
				isRunning = true;
				this.start();
				checkUncompleteTasks();
			}
		}

		public void checkUncompleteTasks() {
			// 检查未完成的任务
		}

		public void close() {
			//isRunning = false;
			//pauseAllTask();
			//this.stop();
		}

		@Override
		public void run() {
			while (isRunning) {
				DownloadTask task = mTaskQueue.poll();
				mDownloadingTasks.add(task);
				task.start();// task.execute();
			}
		}

		public void sendBroadCast(Context context, int edition,
				String identity, double progress) {
			Intent intent = new Intent(DOWNLOAD_TASK_STATE_BROADCAST);
			intent.putExtra(DOWNLOAD_TASK_IDENTITY, identity);
			intent.putExtra(DOWNLOAD_TASK_PROGRESS, progress);
			intent.putExtra(DOWNLOAD_TASK_EDITION, edition);// document 不需要这个参数
															// ebook需要
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		}

		public void sendFinishBroadCast(Context context) {
			Intent intent = new Intent(DOWNLOAD_TASK_FINISH_BROADCAST);
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		}

		public synchronized void addTask(TaskInfo info) {

			if (!hasTask(info.getIdentity())) {
				DownloadTask task = new DownloadTask(mContext, info,
						new DownloadTaskListener() {
							@Override
							public void updateProcess(DownloadTask task) {
								//MZLog.d("wangguodong",
								//		"任务："+ task.getCurrentTaskInfo().getIdentity() + "进度："+ task.getDownloadPercent());
								if (task.getCurrentTaskInfo()
										.getDownloadType()
										.equals(TaskInfo.DOWNLOAD_FILE_TYPE_EBOOK)) {
									sendBroadCast(mContext, task
											.getCurrentTaskInfo().getDetail()
											.getEdition(),
											task.getCurrentTaskInfo()
													.getIdentity(), task
													.getDownloadPercent());
								} else
									sendBroadCast(mContext, -1,
											task.getCurrentTaskInfo()
													.getIdentity(), task
													.getDownloadPercent());
							}

							@Override
							public void finishDownload(DownloadTask task) {
								//MZLog.d("wangguodong","任务："+ task.getCurrentTaskInfo().getIdentity() + "进度："+ task.getDownloadPercent());

								if (task.getCurrentTaskInfo()
										.getDownloadType()
										.equals(TaskInfo.DOWNLOAD_FILE_TYPE_EBOOK)) {
									sendBroadCast(mContext, task
											.getCurrentTaskInfo().getDetail()
											.getEdition(),
											task.getCurrentTaskInfo()
													.getIdentity(), 1.0);
								} else
									sendBroadCast(mContext, -1,
											task.getCurrentTaskInfo()
													.getIdentity(), 1.0);
								sendFinishBroadCast(mContext);

							}

							@Override
							public void preDownload() {

							}

							@Override
							public void errorDownload(DownloadTask task,
									String error) {
								MZLog.d("wangguodong", "任务："
										+ task.getCurrentTaskInfo()
												.getIdentity() + "错误信息："
										+ error);
							}
						});
				mTaskQueue.offer(task);

				//Log.i("wangguodong", "添加任务到队列中,id=" + info.getIdentity()
				//		+ ",等待队列：" + mTaskQueue.size() + ",下载队列："
					//	+ mDownloadingTasks.size());
			}
		}

		public int getQueueTaskCount() {

			return mTaskQueue.size();
		}

		public int getDownloadingTaskCount() {

			return mDownloadingTasks.size();
		}

		public int getPausingTaskCount() {

			return mPausingTasks.size();
		}

		public int getTotalTaskCount() {

			return getQueueTaskCount() + getDownloadingTaskCount()
					+ getPausingTaskCount();
		}

/*	public synchronized void pauseTask(String identity) {

			DownloadTask task;
			for (int i = 0; i < mDownloadingTasks.size(); i++) {
				task = mDownloadingTasks.get(i);
				if (task != null
						&& task.getCurrentTaskInfo().getIdentity()
								.equals(identity)) {
					pauseTask(task);
				}
			}
		}*/
/*
		public synchronized void pauseAllTask() {

			DownloadTask task;

			for (int i = 0; i < mTaskQueue.size(); i++) {
				task = mTaskQueue.get(i);
				mTaskQueue.remove(task);
				mPausingTasks.add(task);
			}

			for (int i = 0; i < mDownloadingTasks.size(); i++) {
				task = mDownloadingTasks.get(i);
				if (task != null) {
					pauseTask(task);
				}
			}
		}
*/
		public synchronized void continueTask(String identity) {

			DownloadTask task;
			for (int i = 0; i < mPausingTasks.size(); i++) {
				task = mPausingTasks.get(i);
				if (task != null
						&& task.getCurrentTaskInfo().getIdentity()
								.equals(identity)) {
					continueTask(task);
				}

			}
		}
	/*	
		public synchronized void deleteTask(String identity) {
			  
			  DownloadTask task; for (int i = 0; i < mDownloadingTasks.size(); i++)
			  { task = mDownloadingTasks.get(i); if (task != null &&
			  task.getCurrentTaskInfo().getIdentity().equals(identity)) { // 删除下载文件
			  // .... // 取消任务 task.onCancelled(); completeTask(task); return; } }
			  for (int i = 0; i < mTaskQueue.size(); i++) { task =
			  mTaskQueue.get(i); if (task != null &&
			  task.getCurrentTaskInfo().getIdentity().equals(identity)) {
			  mTaskQueue.remove(task); } } for (int i = 0; i <
			  mPausingTasks.size(); i++) { task = mPausingTasks.get(i); if (task !=
			  null && task.getCurrentTaskInfo().getIdentity().equals(identity)) {
			  mPausingTasks.remove(task); } } }

		public synchronized void pauseTask(DownloadTask task) {

			if (task != null) {
				//task.onCancelled();
				mDownloadingTasks.remove(task);
				mPausingTasks.add(task);
			}
		}*/

		public synchronized void continueTask(DownloadTask task) {

			if (task != null) {
				mPausingTasks.remove(task);
				mTaskQueue.offer(task);
			}
		}

		public synchronized void completeTask(DownloadTask task) {

			if (mDownloadingTasks.contains(task)) {
				mDownloadingTasks.remove(task);
			}
		}

	}

}
