package com.jingdong.app.reader.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * 功能引导图展示工具类
 *
 */
public class UserGuiderUtil {

	private WindowManager mWinManager = null;
	private View coverView = null;
	private Context mContext = null;
	private WindowManager.LayoutParams mWindowParams = null;
	private FrameLayout layout = null;
	
	public UserGuiderUtil(final Context context, 
			final View covView, 
			boolean incenter,
			boolean inBottom, 
			boolean inRight,
			boolean isneedTitleHeight,
			final GuiderCoverClickListener listener) {

		MZLog.d("wangguodong", "打开新手引导层...");
		if (context == null || covView == null){
			//打开新手引导层,参数错误
			return;
		}

		this.mContext = context;
		this.coverView = covView;

		mWindowParams = new WindowManager.LayoutParams();
		mWindowParams.gravity = Gravity.CENTER;
		mWindowParams.x = 0;
		mWindowParams.y = 0;
		mWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		
		mWindowParams.type =WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
		mWindowParams.format = PixelFormat.RGBA_8888;
		mWinManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		//蒙层
		layout = new FrameLayout(mContext);
		layout.setBackgroundColor(Color.parseColor("#A2000000"));

		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		if(isneedTitleHeight)
			layoutParams.topMargin=	ScreenUtils.getStatusHeight(mContext);
		if (incenter)
			layoutParams.gravity = Gravity.CENTER;
		else {
			layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		}
		
		if(inRight)
			layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
		
		if(inBottom){
			layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
			if(inRight)
				layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		}
		
		coverView.setLayoutParams(layoutParams);
		layout.addView(coverView);
		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//点击后删除View
				removeView();
				if (listener != null) {
					listener.onClick(covView);
				}
			}
		});
		mWinManager.addView(layout, mWindowParams);
	}
	
	/**
	 * 删除视图内容
	 */
	public void removeView() {
		if (mWinManager == null)
			return;
		if (layout.getParent() != null)
			mWinManager.removeView(layout);
	}
	
	public interface GuiderCoverClickListener{
		void onClick(View view);
	}

}