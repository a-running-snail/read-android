package com.jingdong.app.reader.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.jingdong.app.reader.activity.MyActivity;
import com.jingdong.app.reader.activity.MyActivity.DestroyListener;
import com.jingdong.app.reader.util.HttpGroup.HttpError;
import com.jingdong.app.reader.util.HttpGroup.HttpResponse;
import com.jingdong.app.reader.util.HttpGroup.HttpSetting;
import com.jingdong.app.reader.util.HttpGroup.OnEndListener;
import com.jingdong.app.reader.util.HttpGroup.OnErrorListener;
import com.jingdong.app.reader.util.HttpGroup.OnStartListener;


public class DefaultEffectHttpListener implements OnStartListener, OnEndListener, OnErrorListener, DestroyListener {

	private static final String TAG = "DefaultEffectHttpListener";

	private MyActivity myActivity;

	private static final Map<MyActivity, State> stateMap = Collections.synchronizedMap(new HashMap<MyActivity, State>());// TODO 是不是要改弱引用

	private OnStartListener onStartListener;

	private OnEndListener onEndListener;

	private OnErrorListener onErrorListener;

	/**
	 * Copyright 2011 Jingdong Android Mobile Application
	 * 
	 * @author lijingzuo
	 * 
	 *         Time: 2011-1-11 下午05:16:29
	 * 
	 *         Name:
	 * 
	 *         Description: 状态
	 */
	private class State implements Runnable {

		private MyActivity myActivity;

		private ViewGroup modal;

		private ViewGroup rootFrameLayout;

		private ProgressBar progressBar;

		private static final int WAIT_TIME = 500;// 定义一般延迟撤销遮罩的时间
		private static final int WAITING = -1;// 一直等待下去
		private boolean hasThread;// 是否已经创建用于延迟撤销遮罩的线程
		private int waitTime = WAIT_TIME;// 线程运行指示器

		public State(MyActivity myActivity) {
			this.myActivity = myActivity;
		}

		// 给遮罩层添加加载图标所使用的布局参数（高、宽包裹，垂直、水平居中）
		private LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		{
			layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		}

		/**
		 * @author lijingzuo
		 * 
		 *         Time: 2011-1-12 上午09:19:52
		 * 
		 *         Name:
		 * 
		 *         Description: 由于 ProgressBar 旋转动画随着 parentView 消失一次之后就不再运作，因此 showModal 时请调用此方法来解决该问题。
		 * 
		 * 
		 */
		private void newProgressBar() {
			if (null == myActivity) {
				return;
			}			
			myActivity.post(new Runnable() {
				@Override
				public void run() {
					modal.removeView(progressBar);
					progressBar = new ProgressBar(myActivity);
					modal.addView(progressBar, layoutParams);
				}
			});
		}

		private ViewGroup getRootFrameLayout() {

			if (null != rootFrameLayout) {
				return rootFrameLayout;
			}

			rootFrameLayout = (ViewGroup) myActivity.getWindow().peekDecorView();
			if (null == rootFrameLayout) {// 可能界面还没绘制或者还没设置。
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				rootFrameLayout = getRootFrameLayout();
			}

			return rootFrameLayout;

		}

		private ViewGroup getModal() {

			if (null != modal) {
				return modal;
			}

			modal = new RelativeLayout(myActivity);
			// 禁止触屏（用这种方式实现感觉不太爽）
			modal.setOnTouchListener(new ViewGroup.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});
			ColorDrawable colorDrawable = new ColorDrawable(Color.BLACK);
			colorDrawable.setAlpha(100);
			modal.setBackgroundDrawable(colorDrawable);

			return modal;

		}

		private int missionCount;

		/**
		 * 如果是第一个加入会返回true
		 */
		public synchronized boolean addMission() {
			missionCount++;
			if (missionCount == 1) {
				firstMission();
				return true;
			}
			return false;
		}

		/**
		 * 如果是最后一个删除会返回true
		 */
		public synchronized boolean removeMission() {
			missionCount--;
			if (missionCount < 1) {
				lastMission();
				return true;
			}
			return false;
		}

		/**
		 * 添加遮罩
		 */
		private void firstMission() {
			if (null == myActivity) {
				return;
			}			

			if (hasThread) {
				waitTime = WAITING;
				notify();
			} else {
				final ViewGroup rootFrameLayout = getRootFrameLayout();
				final ViewGroup modal = getModal();
				//newProgressBar();

				myActivity.post(new Runnable() {// 不能使用attemptRunOnUiThread方法，因为如果当前是UI线程那么可能在①还未执行时，此处先执行了，导致同一个modal被addView多次的异常。
							public void run() {

								// 界面修改
								if (Log.D) {
									Log.d(TAG, "state add modal -->> " + modal);
								}
								rootFrameLayout.addView(modal, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
								rootFrameLayout.invalidate();

								// 自定义生命周期
								myActivity.onShowModal();

							}
						});
			}

		}

		/**
		 * 移除遮罩
		 */
		private void lastMission() {

			if (hasThread) {
				waitTime = WAIT_TIME;
				notify();
			} else {
				new Thread(this).start();
				hasThread = true;
			}

		}

		/**
		 * 给线程运行，用于延迟撤销遮罩
		 */
		public synchronized void run() {

			// 等待
			do {
				if (waitTime == -1) {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					try {
						int temp = waitTime;
						waitTime = 0;
						wait(temp);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} while (waitTime != 0);

			if (null != myActivity) {
				// 真正撤销遮罩了
				final ViewGroup rootFrameLayout = getRootFrameLayout();
				final ViewGroup modal = getModal();
				myActivity.post(new Runnable() {// 放到任务队列，但是还未执行①
							@Override
							public void run() {

								// 界面修改
								if (Log.D) {
									Log.d(TAG, "state remove modal -->> "
											+ modal);
								}
								rootFrameLayout.removeView(modal);
								rootFrameLayout.invalidate();

								// 自定义生命周期
								myActivity.onHideModal();

							}
						});
			}

			waitTime = WAIT_TIME;
			hasThread = false;

		}

	}

	public DefaultEffectHttpListener(HttpSetting httpSetting, MyActivity myActivity) {
		if (null != httpSetting) {
			onStartListener = httpSetting.getOnStartListener();
			onEndListener = httpSetting.getOnEndListener();
			onErrorListener = httpSetting.getOnErrorListener();
		}
		this.myActivity = myActivity;
		myActivity.addDestroyListener(this);
	}

	private void missionBegins() {

		State state = null;

		synchronized (stateMap) {// 保证不会因为线程问题建了2个。

			if (null == myActivity) {
				return;
			}

			if (Log.D) {
				Log.d(TAG, "state get with -->> " + myActivity);
			}
			state = stateMap.get(myActivity);
			if (Log.D) {
				Log.d(TAG, "state get -->> " + state);
			}
			if (null == state) {
				state = new State(myActivity);
				stateMap.put(myActivity, state);
			}

		}
		if(state!=null)
			state.addMission();

	}

	private void missionComplete() {

		State state = null;
		synchronized (stateMap) {
			if (null == myActivity) {
				return;
			}

			state = stateMap.get(myActivity);

			if (null == state) {
				return;
			}
		}

		state.removeMission();

	}

	@Override
	public void onStart() {
		missionBegins();
		if (null != onStartListener) {
			onStartListener.onStart();
		}
	}

	@Override
	public void onEnd(HttpResponse httpResponse) {
		if (null != onEndListener) {
			onEndListener.onEnd(httpResponse);
		}
		missionComplete();
	}

	@Override
	public void onError(HttpError error) {
		if (null != onErrorListener) {
			onErrorListener.onError(error);
		}
		missionComplete();
	}

	@Override
	public void onDestroy() {
		synchronized (stateMap) {
			stateMap.remove(myActivity);
			myActivity = null;
			onStartListener = null;
			onErrorListener = null;
			onEndListener = null;
		}
	}

}
