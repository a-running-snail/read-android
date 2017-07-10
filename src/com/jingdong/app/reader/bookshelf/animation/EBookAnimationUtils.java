package com.jingdong.app.reader.bookshelf.animation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.util.ScreenUtils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * 处理书架中书籍打开关闭动画
 * @author xuhongwei
 *
 */
public class EBookAnimationUtils implements Animation.AnimationListener{
	private Activity mActivity = null;
	private WindowManager mWindowManager = null;
	private WindowManager.LayoutParams mWindowLayoutParams = null;
	private DisplayMetrics mDisplayMetrics = null;
	/** 封面view的位置信息 */
	private int location[] = new int[2];
	/** 封面view宽度 */
	private int viewWidth;
	/** 封面view高度 */
	private int viewHeight;
	/** 显示图书内容的view */
	private RelativeLayout mBgView = null;
	/** 显示图书封面的view */
	private ImageView mCoverView = null;
	private RelativeLayout mRootLayout = null;
	/** 打开动画时间 */
	private final int openDuration = 900;
	/** 关闭动画时间 */
	private final int closeDuration = 700;
	/** 打开书籍标识 */
	private boolean isOpen = false;
	/** 书籍封面图片 */
	private Bitmap mCoverBitmap = null;
	/** 书籍内容图片 */
	private Bitmap mBgBitmap = null;
	/** 动画监听 */
	private OnAnimationListener mOnAnimationListener = null;
	private boolean playing = false;
	/** 屏幕高度 */
	private int mScreenHeight;
	/** 状态栏高度 */
	private int mStatusHeight;
	/** 关闭窗口显示标识 */
	private boolean isShowClose = false;
	
	public EBookAnimationUtils(Activity activity) {
		mActivity = activity;
		//显示信息
		mDisplayMetrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		mStatusHeight = ScreenUtils.getStatusHeight(mActivity);
		//当前程序界面高度
		mScreenHeight = mDisplayMetrics.heightPixels - mStatusHeight;
		
		mWindowManager = (WindowManager)mActivity.getSystemService(Context.WINDOW_SERVICE);
		
		//布局参数
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; //图片之外的其他地方透明
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowLayoutParams.x = 0;
		mWindowLayoutParams.y = 0;
		mWindowLayoutParams.alpha = 1f; //透明度
		mWindowLayoutParams.width = LayoutParams.MATCH_PARENT;  
		mWindowLayoutParams.height = LayoutParams.MATCH_PARENT;  
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		
	}
	
	/**
	 * 更新view配置信息
	 * @param view
	 */
	private void updateConfig(View view) {
		mRootLayout = new RelativeLayout(mActivity);
		mBgView = new RelativeLayout(mActivity);
		
		mCoverView = new ImageView(mActivity);
		mCoverView.setScaleType(ScaleType.FIT_XY);
		mRootLayout.setVisibility(View.GONE);
		
		if (isOpen) {
			//获取被点击图书封面的宽高
			viewWidth = view.getWidth();
			viewHeight = view.getHeight();
			////获取在当前窗口内的绝对坐标
			view.getLocationInWindow(location);
			location[1] -= mStatusHeight;

			mBgView.removeAllViews();
			float scaleX = (float)mDisplayMetrics.widthPixels/viewWidth;
			float scaleY = (float)mDisplayMetrics.heightPixels/viewHeight;

			int w = (int)(ScreenUtils.dip2px(90)/scaleX);
			int h = (int)(ScreenUtils.dip2px(90)/scaleY);
			RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(w, h);
			iconParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		}
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(viewWidth, viewHeight);
		if (isOpen) {
			params.width = viewWidth;
			params.height = viewHeight;
			params.setMargins(location[0], location[1], 0, 0);
		}else {
			params.width = mDisplayMetrics.widthPixels;
			params.height = mDisplayMetrics.heightPixels;
			params.setMargins(0, 0, 0, 0);
		}
		
		mBgView.setLayoutParams(params);
		mCoverView.setLayoutParams(params);
		
		mRootLayout.removeAllViews();
		mRootLayout.addView(mBgView, params);
		mRootLayout.addView(mCoverView, params);
		mRootLayout.setVisibility(View.VISIBLE);
		
		mWindowManager.addView(mRootLayout, mWindowLayoutParams); 
	}
	
	/** 获取动画打开状态 */
	public boolean getIsOpen() {
		return isOpen;
	}
	
	/**
	 * 书架书籍打开动画显示
	 * @param view
	 */
	public void showOpenEBookAnimation(View view, OnAnimationListener onAnimationListener) {
		//有打开动画就忽略
		if(playing) {
			return;
		}
		isOpen = true;
		playing = true;
		mOnAnimationListener = onAnimationListener;
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		updateConfig(view);
		mBgView.clearAnimation();
		mCoverView.clearAnimation();
		view.setDrawingCacheEnabled(true);
		//封面缓存
		mCoverBitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.destroyDrawingCache();
		
//		String filename = MZBookApplication.getInstance().getCachePath() + "/pic.jpg";
//		mBgBitmap = ImageManager.getBitmapFromCache(filename);
//		if (null == mBgBitmap) {
			mBgView.setBackgroundColor(Color.rgb(0xeb, 0xeb, 0xeb));
//		}else {
//			mBgView.setBackgroundDrawable(new BitmapDrawable(mBgBitmap));
//		}
		
		if (null != mCoverBitmap) {
			mCoverView.setImageBitmap(mCoverBitmap);
		}
		
		//开始动画
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mBgView.startAnimation(getAnimationSet());
				mCoverView.startAnimation(getOpenCoverAnimation());
			}
		}, 100);
	}

	/**
	 * 显示关闭书籍动画
	 */
	public void closeEBook() {
		isOpen = false;
		playing = false;
		mOnAnimationListener = null;
		if (isShowClose) {
			isShowClose = false;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mBgView.startAnimation(getCloseBGAnimation());
					mCoverView.startAnimation(getCloseCoverAnimation());
				}
			}, 100);
		}
	}
	
	public void showClose(Bitmap bitmap) {
		isShowClose = true;
		if(playing) {
			return;
		}
		isOpen = false;
		playing = true;

		updateConfig(null);
		mBgView.clearAnimation();
		mCoverView.clearAnimation();
		mCoverView.setImageBitmap(null);
		
		mBgBitmap = bitmap;
//		saveBitmap();
		if (null != mBgBitmap) {
			mBgView.removeAllViews();
			mBgView.setBackgroundDrawable(new BitmapDrawable(mBgBitmap));
		}
		isOpen = true;
	}
	
	/**
	 * 保存关闭书籍截图
	 */
	private void saveBitmap() {
		if(null != mBgBitmap) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					String filename = MZBookApplication.getInstance().getCachePath() + "/pic.jpg";
					File f = new File(filename);
					if (f.exists()) {
			    	   f.delete();
					}
					try {
						FileOutputStream out = new FileOutputStream(f);
						mBgBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
						out.flush();
						out.close();
					} catch (FileNotFoundException e) {	
			    	   e.printStackTrace();
					} catch (IOException e) {
			    	   e.printStackTrace();
					}
				}
			}).start();
		}
	}
	
	/**
	 * 获取动画集合
	 * @return
	 */
	private AnimationSet getAnimationSet() {
		float scaleX;
		float scaleY;
		float pivotXValue;
		float pivotYValue;
		
		if (isOpen) {
			scaleX = (float)mDisplayMetrics.widthPixels/viewWidth;
			scaleY = (float)mDisplayMetrics.heightPixels/viewHeight;
			pivotXValue = -(float)location[0]/viewWidth;
			pivotYValue = -(float)location[1]/viewHeight;
		}else {
			scaleX = (float)viewWidth/mDisplayMetrics.widthPixels;
			scaleY = (float)viewHeight/mDisplayMetrics.heightPixels;
			pivotXValue = (float)location[0]/mDisplayMetrics.widthPixels;
			pivotYValue = (float)location[1]/mDisplayMetrics.heightPixels;
		}
		
		final AnimationSet animation = new AnimationSet(true);
		animation.setDuration(openDuration); 
		animation.setFillAfter(true);
		animation.addAnimation(getScaleAnimation(scaleX, scaleY, openDuration));
		animation.addAnimation(getTranslateAnimation(pivotXValue, pivotYValue, openDuration));
		animation.setAnimationListener(this);
		return animation;
	}
	
	/**
	 * 获取放大缩小动画
	 * @param toX
	 * @param toY
	 * @return
	 */
	private ScaleAnimation getScaleAnimation(float toX, float toY, int duration) {
		ScaleAnimation mScaleAnimation = new ScaleAnimation(1, toX, 1f, toY, 
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0); 
		mScaleAnimation.setDuration(duration);
		mScaleAnimation.setFillAfter(true);
		return mScaleAnimation;
	}
	
	/**
	 * 获取移动动画
	 * @param pivotXValue
	 * @param pivotYValue
	 * @return
	 */
	private TranslateAnimation getTranslateAnimation(float pivotXValue, float pivotYValue, int duration) {
		TranslateAnimation mTranslateAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, pivotXValue,
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, pivotYValue);
		mTranslateAnimation.setDuration(duration);
		mTranslateAnimation.setFillAfter(true);
		return mTranslateAnimation;
	}
	
	/**
	 * 获取封面动画集合
	 * @return
	 */
	private AnimationSet getOpenCoverAnimation() {
		float scaleX = (float)mDisplayMetrics.widthPixels/viewWidth;
		float scaleY = (float)mDisplayMetrics.heightPixels/viewHeight;
		float pivotXValue = (float)location[0]/viewWidth;
		float pivotYValue = (float)location[1]/viewHeight;
		
		AnimationSet animation = new AnimationSet(true);
		animation.setDuration(openDuration); 
		animation.setFillAfter(true);
		animation.setAnimationListener(EBookAnimationUtils.this);
		
		Rotate3DAnimation coverAnimation = new Rotate3DAnimation(0, -180, 0, 0, 0, false);
        coverAnimation.setDuration(openDuration-150);
        coverAnimation.setFillAfter(true);
        
        ScaleAnimation mScaleAnimation = new ScaleAnimation(1, scaleX, 1f, scaleY, 
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0); 
		mScaleAnimation.setDuration(openDuration);
		mScaleAnimation.setFillAfter(true);
		
		TranslateAnimation mTranslateAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, -pivotXValue,
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, -pivotYValue);
		mTranslateAnimation.setDuration(openDuration);
		mTranslateAnimation.setFillAfter(true);
		
        animation.addAnimation(coverAnimation);
        animation.addAnimation(mScaleAnimation);
        animation.addAnimation(mTranslateAnimation);
        animation.setAnimationListener(this);
        return animation;
	}
	
	/**
	 * 获取封面关闭动画
	 * @return
	 */
	private AnimationSet getCloseCoverAnimation() {
		float scaleX = (float)viewWidth/mDisplayMetrics.widthPixels;
		float scaleY = (float)viewHeight/mScreenHeight;
		float pivotXValue = (float)location[0]/mDisplayMetrics.widthPixels;
		float pivotYValue = (float)location[1]/mScreenHeight;
		
		AnimationSet animation = new AnimationSet(true);
		animation.setDuration(closeDuration); 
		animation.setFillAfter(true);
		animation.setAnimationListener(EBookAnimationUtils.this);
		
		Rotate3DAnimation coverAnimation = new Rotate3DAnimation(-180, 0, 0, 0, -mDisplayMetrics.widthPixels, false);
        coverAnimation.setDuration(closeDuration);
        coverAnimation.setFillAfter(true);
        coverAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        
        ScaleAnimation mScaleAnimation = new ScaleAnimation(1, scaleX, 1f, scaleY, 
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0); 
		mScaleAnimation.setDuration(closeDuration);
		mScaleAnimation.setFillAfter(true);
		mScaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		
		TranslateAnimation mTranslateAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, pivotXValue,
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, pivotYValue);
		mTranslateAnimation.setDuration(closeDuration);
		mTranslateAnimation.setFillAfter(true);
		mTranslateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		
		animation.setInterpolator(new AccelerateDecelerateInterpolator());
		animation.addAnimation(coverAnimation);
        animation.addAnimation(getScaleAnimation(scaleX, scaleY, closeDuration));
        animation.addAnimation(getTranslateAnimation(pivotXValue, pivotYValue, closeDuration));
        return animation;
	}
	
	/**
	 * 获取背景内容关闭动画
	 * @return
	 */
	private AnimationSet getCloseBGAnimation() {
		float scaleX = (float)viewWidth/mDisplayMetrics.widthPixels;
		float scaleY = (float)viewHeight/mScreenHeight;
		float pivotXValue = (float)location[0]/mDisplayMetrics.widthPixels;
		float pivotYValue = (float)location[1]/mScreenHeight;
		AnimationSet animation2 = new AnimationSet(true);
        
        Rotate3DAnimation coverAnimation2 = new Rotate3DAnimation(0, 0, 0, 0, -mDisplayMetrics.widthPixels, false);
        coverAnimation2.setDuration(closeDuration);
        coverAnimation2.setFillAfter(true);
        coverAnimation2.setInterpolator(new AccelerateDecelerateInterpolator());
        
        ScaleAnimation mScaleAnimation2 = new ScaleAnimation(1, scaleX, 1f, scaleY, 
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0); 
        mScaleAnimation2.setDuration(closeDuration);
        mScaleAnimation2.setFillAfter(true);
        mScaleAnimation2.setInterpolator(new AccelerateDecelerateInterpolator());
		
		TranslateAnimation mTranslateAnimation2 = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, pivotXValue,
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, pivotYValue);
		mTranslateAnimation2.setDuration(closeDuration);
		mTranslateAnimation2.setFillAfter(true);
		mTranslateAnimation2.setInterpolator(new AccelerateDecelerateInterpolator());
        
		animation2.setInterpolator(new AccelerateDecelerateInterpolator());
        animation2.addAnimation(coverAnimation2);
        animation2.addAnimation(mScaleAnimation2);
        animation2.addAnimation(mTranslateAnimation2);
        animation2.setAnimationListener(EBookAnimationUtils.this);
        return animation2;
	}
	
	@Override
	public void onAnimationEnd(Animation animation) {
		playing = false;
		if(!isOpen) {
			mRootLayout.setVisibility(View.GONE);
			destory();
		}
		if(null != mOnAnimationListener) {
			mOnAnimationListener.AnimationEnd();
		}
		if(!isOpen) {
			try{
				if(null != mRootLayout.getParent()) {
					mWindowManager.removeView(mRootLayout);
				}	
			}catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * 关闭打开书籍后的窗口
	 */
	public void hideWindow() {
		if(isOpen && !isShowClose) {
			mRootLayout.setVisibility(View.GONE);
			try{
				if(null != mRootLayout.getParent()) {
					mWindowManager.removeView(mRootLayout);
				}	
			}catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 *  释放资源
	 */
	private void destory() {
		mCoverView.setImageBitmap(null);
		mBgView.setBackgroundDrawable(null);
		if(null != mCoverBitmap) {
			if (!mCoverBitmap.isRecycled()) {
				mCoverBitmap.recycle();
				mCoverBitmap = null;
			}
		}
		if(null != mBgBitmap) {
			if (!mBgBitmap.isRecycled()) {
				mBgBitmap.recycle();
				mBgBitmap = null;
			}
		}
	}

	@Override 
	public void onAnimationStart(Animation animation) { 
		if(null != mOnAnimationListener) {
			mOnAnimationListener.AnimationStart();
		}
		if (null != mCoverBitmap) {
			mCoverView.setImageBitmap(mCoverBitmap);
		}
	}
	
	@Override public void onAnimationRepeat(Animation animation) { }
	
	public interface OnAnimationListener {
		public void AnimationStart();
		public void AnimationEnd();
	}

}
