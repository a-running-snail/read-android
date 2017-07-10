package com.jingdong.app.reader.util.thread;

import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Vector;

import android.os.Process;

import com.jingdong.app.reader.util.HttpGroup.HttpGroupaAsynPool.HttpRequestRunnable;
import com.jingdong.app.reader.util.HttpGroup.HttpRequest;
import com.jingdong.app.reader.util.IPriority;
import com.jingdong.app.reader.util.PriorityCollection;

/**
 * 线程池
 * 
 * @author lijingzuo
 */
public class ThreadPool {

	protected int maxPoolSize;
	protected int initPoolSize;
	protected Vector threads = new Vector();
	protected boolean initialized = false;
	protected boolean hasIdleThread = false;
	protected PriorityQueue<IPriority> queue = new PriorityQueue<IPriority>();

	public ThreadPool(int maxPoolSize, int initPoolSize) {
		this.maxPoolSize = maxPoolSize;
		this.initPoolSize = initPoolSize;
	}

	public void init() {
		initialized = true;
		for (int i = 0; i < initPoolSize; i++) {
			PooledThread thread = new PooledThread(this);
			thread.start();
			threads.add(thread);
		}

		// 循环分配任务
		new Thread(new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
				while (true) {
					PooledThread idleThread = getIdleThread();
					Collection<Runnable> pollTasks = (Collection<Runnable>) pollTasks();
					if (null != pollTasks) {
						idleThread.putTasks(pollTasks);
						idleThread.startTasks();
					} else {
						synchronized (queue) {
							try {
								queue.wait();
							} catch (InterruptedException e) {
							}
						}
					}
				}
			}
		}).start();
	}

	public void setMaxPoolSize(int maxPoolSize) {
		// System.out.println("重设最大线程数，最大线程数=" + maxPoolSize);
		this.maxPoolSize = maxPoolSize;
		if (maxPoolSize < getPoolSize())
			setPoolSize(maxPoolSize);
	}

	/**
	 * 重设当前线程数 若需杀掉某线程，线程不会立刻杀掉，而会等到线程中的事务处理完成 但此方法会立刻从线程池中移除该线程，不会等待事务处理结束
	 * 
	 * @param size
	 */
	public void setPoolSize(int size) {
		if (!initialized) {
			initPoolSize = size;
			return;
		} else if (size > getPoolSize()) {
			for (int i = getPoolSize(); i < size && i < maxPoolSize; i++) {
				PooledThread thread = new PooledThread(this);
				thread.start();
				threads.add(thread);
			}
		} else if (size < getPoolSize()) {
			while (getPoolSize() > size) {
				PooledThread th = (PooledThread) threads.remove(0);
				th.kill();
			}
		}

		// System.out.println("重设线程数，线程数=" + threads.size());
	}

	public int getPoolSize() {
		return threads.size();
	}

	/* 给子线程调用的 */
	protected synchronized void notifyForIdleThread() {
		hasIdleThread = true;
		notify();
	}

	protected synchronized boolean waitForIdleThread() {
		hasIdleThread = false;// 标记为假，等待有线程将其改为真
		while (!hasIdleThread && getPoolSize() >= maxPoolSize) {// 如果没有空闲线程并且无法创建新线程就继续等待
			try {
				wait();
			} catch (InterruptedException e) {
				return false;
			}
		}

		return true;
	}

	public PooledThread getIdleThread() {
		while (true) {
			// 循环看有没有空闲线程，有就返回
			for (Iterator itr = threads.iterator(); itr.hasNext();) {
				PooledThread th = (PooledThread) itr.next();
				if (!th.isRunning())
					return th;
			}

			// 如果没有空闲线程，看能否创建新线程
			if (getPoolSize() < maxPoolSize) {
				PooledThread thread = new PooledThread(this);
				thread.start();
				threads.add(thread);
				return thread;
			}

			// 标记等待，并执行等待工作，如果返回假代表被打断，如果返回真从头再获取一次线程。
			if (!waitForIdleThread()) {
				return null;
			}
		}
	}

	public synchronized void offerTask(Runnable runnable, int priority) {
		PriorityCollection<Runnable> list = new PriorityCollection<Runnable>(priority);
		list.add(runnable);
		offerTasks(list);
	}

	public synchronized void offerTasks(IPriority list) {
		queue.offer(list);
		synchronized (queue) {
			queue.notify();
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void removeTask(final HttpRequest httpRequest) {
		Vector<Object> list = new Vector<Object>();
		Iterator iter = queue.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof PriorityCollection) {
				HttpRequestRunnable runable = (HttpRequestRunnable)(((PriorityCollection<Runnable>)obj).get(0));
				if (runable.getHttpRequest() != httpRequest) {
					list.add(obj);
				}
			}
		}
		queue.clear();
		for (Object obj : list) {
			queue.offer((IPriority) obj);
		}
	}

	private synchronized IPriority pollTasks() {
		return queue.poll();
	}

}
