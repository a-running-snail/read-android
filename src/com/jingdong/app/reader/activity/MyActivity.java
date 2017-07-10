package com.jingdong.app.reader.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import cn.jpush.android.api.JPushInterface;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.util.Log;




public class MyActivity extends Activity 
{

	private Handler handler;

	private ArrayList<DestroyListener> destroyListenerList = new ArrayList<DestroyListener>();
	private ArrayList<PauseListener> pauseListenerList = new ArrayList<PauseListener>();
	private ArrayList<ResumeListener> resumeListenerList = new ArrayList<ResumeListener>();
//	ConnectionChangeReceiver connection=new ConnectionChangeReceiver();
	@Override
	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		handler = new Handler();
	
		
//		IntentFilter filter=new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
//		registerReceiver(connection, filter);
		  //不显示程序的标题栏
//
//       getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                              WindowManager.LayoutParams.FLAG_FULLSCREEN );
//       getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//       本篇文章来源于 Linux公社网站(www.linuxidc.com)  原文链接：http://www.linuxidc.com/Linux/2011-10/44934.htm
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
	}

	public Handler getHandler() {
		return handler;
	}

	/**
	 * 统一 post 接口
	 */
	public void post(final Runnable action) {
		// Log.i("zhoubo", "handler==="+handler);
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (MyActivity.this.isFinishing()) {
					return;
				}
				action.run();
			}
		});
	}

	/**
	 * 统一 post 接口
	 */
	public void post(final Runnable action, int delayMillis) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (MyActivity.this.isFinishing()) {
					return;
				}
				action.run();
			}
		}, delayMillis);
	}

	/**
	 * 统一 post 接口
	 */
	public void post(final Runnable action, long delayMillis) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (MyActivity.this.isFinishing()) {
					return;
				}
				action.run();
			}
		}, delayMillis);
	}


	@Override
	protected void onResume() 
    {
		if (Log.D) 
        {
			Log.d("MyActivity", "onResume() -->> " + getClass().getName());
		}
		super.onResume();
		MZBookApplication.getInstance().setCurrentMyActivity(this);
		JPushInterface.onResume(this);
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		JPushInterface.onPause(this);
	}
    public interface DestroyListener {
		void onDestroy();
	}

	public interface OnReleaseHttpsetingLintenser {
		public void releaseHttpSettings();
	}

	public interface PauseListener {
		void onPause();
	}

	public interface ResumeListener {
		void onResume();
	}


	public void addPauseListener(PauseListener listener) {
		if (null != pauseListenerList) {
			pauseListenerList.add(listener);
		}
	}

	public void addResumeListener(ResumeListener listener) {
		if (null != resumeListenerList) {
			resumeListenerList.add(listener);
		}
	}

	public void addDestroyListener(DestroyListener listener) {
		if (null != destroyListenerList) {
			destroyListenerList.add(listener);
		}
	}

	public void removePauseListener(PauseListener listener) {
		if (null != pauseListenerList) {
			pauseListenerList.remove(listener);
		}
	}

	public void removeResumeListener(ResumeListener listener) {
		if (null != resumeListenerList) {
			resumeListenerList.remove(listener);
		}
	}

	public void removeDestroyListener(DestroyListener listener) {
		if (null != destroyListenerList) {
			destroyListenerList.remove(listener);
		}
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
//		unregisterReceiver(connection);
		super.onDestroy();
	}
	/**
	 * 显示遮罩层时
	 */
	public void onShowModal() {

	}

	/**
	 * 隐藏遮罩层时
	 */
	public void onHideModal() {

	}



}
