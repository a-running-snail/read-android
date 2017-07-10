package com.jingdong.app.reader.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.R;

public class FlowWindowUtils {

	public static final String COMPOSE_TEXT="com.mzread.flowwindow.composetext";
	public static final String BOOK_COMMENT="com.mzread.flowwindow.bookcomment";
	public static final String BOOK_LIST="com.mzread.flowwindow.booklist";
	
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mParams;
	private WindowManager.LayoutParams mBackParams;
	private LinearLayout layout;
	private LinearLayout back;
	private boolean isViewCreate = false;
	private boolean isViewRemove = false;
	
	private LinearLayout booklist;
	private LinearLayout bookcomment;
	private LinearLayout bookcompost;
	private LinearLayout emptyView;
	private Context mContext;

	public void addView(Context context) {
		
		
		mContext=context;
		
		if (!isViewCreate) {
			MZLog.d("wangguodong", "创建悬浮窗口");
			isViewCreate=true;
			isViewRemove=false;
			mParams = new WindowManager.LayoutParams();
			mWindowManager = (WindowManager) context.getSystemService("window");
			mParams.type = LayoutParams.TYPE_APPLICATION_PANEL ;
			mParams.format = PixelFormat.RGBA_8888;
			mParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
			mParams.gravity = Gravity.CENTER;
			mParams.windowAnimations=android.R.style.Animation_InputMethod;
			mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
			mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
			layout = (LinearLayout) LayoutInflater.from(context).inflate(
					R.layout.flow_window, null);			
			
			
			back=(LinearLayout) LayoutInflater.from(context).inflate(
					R.layout.background, null);	
			
			mBackParams = new WindowManager.LayoutParams();
			mBackParams.type = LayoutParams.TYPE_APPLICATION_PANEL ;
			mBackParams.format = PixelFormat.RGBA_8888;
			mBackParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
			mBackParams.width = WindowManager.LayoutParams.MATCH_PARENT;
			mBackParams.height = WindowManager.LayoutParams.MATCH_PARENT;
			
			//添加背景颜色层
			mWindowManager.addView(back, mBackParams);
			mWindowManager.addView(layout, mParams);
			
			booklist=(LinearLayout) layout.findViewById(R.id.booklist);
			bookcomment=(LinearLayout) layout.findViewById(R.id.bookcomment);
			bookcompost=(LinearLayout) layout.findViewById(R.id.bookcompost);
			emptyView=(LinearLayout) layout.findViewById(R.id.empty_view);
			emptyView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// remove window
					removeView();
				}
			});
			booklist.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(FlowWindowUtils.BOOK_LIST));
					// remove window
					MZLog.d("wangguodong", "书单被点击");
					removeView();
				}
			});
			bookcomment.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					MZLog.d("wangguodong", "书评被点击");
					LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(FlowWindowUtils.BOOK_COMMENT));
					// remove window
					removeView();
				}
			});
			bookcompost.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					MZLog.d("wangguodong", "随便说说被点击");
					
					LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(FlowWindowUtils.COMPOSE_TEXT));
					
					// remove window
					removeView();
				}
			});
		}
		else {
			MZLog.d("wangguodong", "悬浮窗口已经创建过了");
		}

	}

	public boolean removeView() {
		if (mWindowManager != null &&isViewCreate&&!isViewRemove) {
			
			mWindowManager.removeView(layout);
			mWindowManager.removeView(back);
			isViewRemove = true;
			isViewCreate=false;
			MZLog.d("wangguodong", "移除悬浮窗口完成");
			return true;
		}
		else {
			
			MZLog.d("wangguodong", "悬浮窗口已经移除过了");
			return false;
		}
	}
}
