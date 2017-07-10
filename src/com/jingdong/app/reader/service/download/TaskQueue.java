package com.jingdong.app.reader.service.download;
import java.util.LinkedList;
import java.util.Queue;

public class TaskQueue {
	private static final int MAX_DOWNLOAD_THREAD_COUNT = 3;
	private static final int MAX_DOWNLOAD_TASK_COUNT = 50;
	private Queue<DownloadTask> taskQueue;

	public TaskQueue() {
		taskQueue = new LinkedList<DownloadTask>();
	}

	public void offer(DownloadTask task) {
		//超过最大任务队列的任务丢弃
		if(taskQueue.size()<MAX_DOWNLOAD_TASK_COUNT)
			taskQueue.offer(task);
		
	}

	public DownloadTask poll() {

		DownloadTask task = null;
		while (DownloadService.mDownloadingTasks.size() >= MAX_DOWNLOAD_THREAD_COUNT
				|| (task = taskQueue.poll()) == null) {
			try {
				Thread.sleep(1000); 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return task;
	}

	public DownloadTask get(int position) {

		if (position >= size()) {
			return null;
		}
		return ((LinkedList<DownloadTask>) taskQueue).get(position);
	}

	public int size() {

		return taskQueue.size();
	}

	public boolean remove(int position) {

		return taskQueue.remove(get(position));
	}

	public boolean remove(DownloadTask task) {
		return taskQueue.remove(task);
	}
}