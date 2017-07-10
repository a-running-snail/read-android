package com.android.mzbook.sortview.optimized;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.util.ScreenUtils;

/**
 * 
 * 
 * @author WANGGUODONG time:2014 -12 17
 */
public class DelBookView {
	private Context mContext;
	public static final int DELETE_VIEW_WIDTH_OR_HEIGHT = 88; 
	private ViewGroup mPopupLayout;
	private DelBookViewContainer mParentView;
	private WindowManager mWinManager;
	private WindowManager.LayoutParams mParams;
	private final int ANIMALTION_TIME = 200;
	private final int HALF_ANIMALTION_TIME = 200;
	private ImageView delView = null;
	private boolean mIsOpened = false;
	private Rect mDelRect = null;
	private boolean mDismissed = false;


	private int delViewWidthOrHeight = DELETE_VIEW_WIDTH_OR_HEIGHT;

	public interface OnDelBookViewClosedListener {
		public void onClosed();
	}

	public DelBookView(Context context) {
		mContext = context;
		prepareLayout();
	}

	public void prepareLayout() {

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPopupLayout = (FrameLayout) inflater.inflate(
				R.layout.delete_book_view, null);
		mWinManager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);

		delView = (ImageView) mPopupLayout.findViewById(R.id.delView);
		delViewWidthOrHeight = ScreenUtils.dip2px(mContext,
				delViewWidthOrHeight);

		DisplayMetrics dm = new DisplayMetrics();
		mWinManager.getDefaultDisplay().getMetrics(dm);
		int screenHeight = dm.heightPixels;

		mDelRect = new Rect(0, (screenHeight - delViewWidthOrHeight) / 2,
				delViewWidthOrHeight, (screenHeight - delViewWidthOrHeight) / 2
						+ delViewWidthOrHeight);

		mParams = new WindowManager.LayoutParams();
		mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
		mParams.format = PixelFormat.RGBA_8888;
		mParams.gravity = Gravity.LEFT;
		mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		mParentView = new DelBookViewContainer(mContext);
		mParentView.setBackgroundColor(Color.TRANSPARENT);
		mParentView.addView(mPopupLayout);
		mWinManager.addView(mParentView, mParams);
		startOpenAnimation();

	}

	public void toNormalState() {
		if (delView != null)
			delView.setImageResource(R.drawable.delete_category_normal);
	}

	public void toPressedState() {
		if (delView != null)
			delView.setImageResource(R.drawable.delete_category_normal_selected);
	}

	private void startOpenAnimation() {

		Animation anim = new TranslateAnimation(-delViewWidthOrHeight, 0, 0, 0);
		anim.setDuration(HALF_ANIMALTION_TIME);
		anim.setInterpolator(new DecelerateInterpolator());
		anim.setFillAfter(true);
		anim.setAnimationListener(mOpenAnimationListener);
		delView.startAnimation(anim);
	}

	public boolean removeDelBookView() {
		if (mWinManager != null) {
			mWinManager.removeView(mParentView);
			return true;
		} else {
			return false;
		}
	}

	public void dismiss() {

		if (!mIsOpened) {
			mDismissed=true;
			return;
		}

		Animation anim = new TranslateAnimation(0, -delViewWidthOrHeight, 0, 0);
		anim.setDuration(ANIMALTION_TIME);
		anim.setInterpolator(new DecelerateInterpolator());
		anim.setFillAfter(true);
		anim.setAnimationListener(mClosedAnimationListener);
		delView.startAnimation(anim);

	}

	class DelBookViewContainer extends FrameLayout {

		long lasttime = 0;
		boolean isvalid = false;

		public DelBookViewContainer(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
					&& event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getRepeatCount() == 0) {
				dismiss();
				return true;
			}
			return super.dispatchKeyEvent(event);
		}

	}

	private Animation.AnimationListener mOpenAnimationListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mIsOpened = true;
			if(mDismissed)
				dismiss();
		}
	};

	private Animation.AnimationListener mClosedAnimationListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			removeDelBookView();
			mIsOpened = false;

		}
	};

}
