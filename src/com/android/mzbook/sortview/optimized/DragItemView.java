package com.android.mzbook.sortview.optimized;

import com.jingdong.app.reader.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 书架拖动布局
 * @author WANGGUODONG 
 * time:2014 -7 17
 */
public class DragItemView extends View {

	private final Context mContext;
	private final ViewGroup mPopupLayout;
	private final ViewGroup mParentView;
	private final WindowManager mWinManager;
	private final WindowManager.LayoutParams mWindowParams;
	private ImageView imageView;
	private int viewWidth;
	private int viewHeight;
	// 图片顶点在整个界面中位置
	private int viewx;
	private int viewy;

	public DragItemView(final Context context, int x, int y, int width, int height) {
		super(context);
		mContext = context;
		viewWidth = width;
		viewHeight = height;
		viewx = x;
		viewy = y;
		mWindowParams = new WindowManager.LayoutParams();
		mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowParams.x = x;
		mWindowParams.y = y;
		mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		
		mWindowParams.type =WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
		
		mWindowParams.format = PixelFormat.TRANSLUCENT;
		mWindowParams.alpha = 0.7f;
		mWinManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPopupLayout = (LinearLayout) inflater.inflate(
				R.layout.dragview_popup_layout, null);
		imageView = (ImageView) mPopupLayout.findViewById(R.id.drag_imageview);
		mParentView = new FrameLayout(mContext);
		mParentView.setVisibility(View.VISIBLE);
		mWinManager.addView(mParentView, mWindowParams);
		mParentView.addView(mPopupLayout, mWindowParams);
		mPopupLayout.setVisibility(VISIBLE);

	}



	/**
	 * 放大view
	 * 
	 * @param x
	 *            view 在屏幕中的横坐标
	 * @param y
	 *            view 在屏幕的纵坐标
	 */

	public void scaleToNormalView(int x, int y) {

		// 传过来window 绝对位置
		float xvalue = (x - viewx) / (viewWidth * 1.0f);
		float yvalue = (y - viewy) / (viewHeight * 1.0f);
		Animation animation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f,
				Animation.RELATIVE_TO_SELF, xvalue, Animation.RELATIVE_TO_SELF,
				yvalue);
		animation.setDuration(500);
		animation.setFillAfter(true);
		mPopupLayout.startAnimation(animation);

	}

	/**
	 * 缩放view
	 * 
	 * @param x
	 *            view 在屏幕中的横坐标
	 * @param y
	 *            view 在屏幕的纵坐标
	 */

	public void scaleToMiniView(int x, int y) {

		// 传过来window 绝对位置
		float xvalue = (x - viewx) / (viewWidth * 1.0f);
		float yvalue = (y - viewy) / (viewHeight * 1.0f);
		// 以触摸点为中心进行缩放，相对于自身的位置缩放
		Animation animation = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
				Animation.RELATIVE_TO_SELF, xvalue, Animation.RELATIVE_TO_SELF,
				yvalue);
		animation.setDuration(300);
		animation.setFillAfter(true);
		mPopupLayout.startAnimation(animation);

	}

	public void fadeOut() {

		Animation animation = new AlphaAnimation(1.0f, 0.0f);
		animation.setDuration(500);
		animation.setFillAfter(true);
		mPopupLayout.startAnimation(animation);

	}
	
	public void destroyDragImageView(){
		

		removeView();
		BitmapDrawable bitmapDrawable = (BitmapDrawable)getDrawable();
		if (bitmapDrawable != null) {
			Bitmap bitmap = bitmapDrawable.getBitmap();
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}
		}
		
	}

	public void setImageBackground(Bitmap bm) {
		if (imageView != null)
			imageView.setImageBitmap(bm);
	}

	public BitmapDrawable getDrawable() {
		if (imageView != null)
			return (BitmapDrawable) imageView.getDrawable();
		return null;
	}

	public void removeView() {
		if (mWinManager != null)
		{
			mParentView.setVisibility(View.INVISIBLE);
			mWinManager.removeView(mParentView);
		}
			
	}

	/**
	 * 更新view在屏幕的显示位置
	 * 
	 * @param x
	 *            view 在屏幕中的横坐标
	 * @param y
	 *            view 在屏幕的纵坐标
	 */

	public void updatePosition(int x, int y) {
		if (mWinManager != null && mWindowParams != null) {
			mWindowParams.x = x;
			mWindowParams.y = y;
			viewx = x;
			viewy = y;
			mWinManager.updateViewLayout(mParentView, mWindowParams);
		}

	}
	
	public int getViewWidth() {
		return viewWidth;
	}

	public int getViewHeight() {
		return viewHeight;
	}

}
