package com.jingdong.app.reader.extension.integration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.Button;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.util.MZLog;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * 漂浮按钮
 *
 */
public class FloatingActionButton extends Button {

	private static final int TIME_DISMISS = 3 * 60 * 1000;
	private long mMillisLeft = TIME_DISMISS;// 已经倒计时的毫秒数
	private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
	private final Paint mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Bitmap mBitmap;
	private int mColor;
	private boolean mHidden = false;
	private Rect rect;
	private int mLeftDisplayed = -1;
	private int mRightDisplayed = -1;
	private int mTopDisplayed = -1;
	private int mBottomDisplayed = -1;
	/**
	 * The FAB button's Y position when it is displayed.
	 */
	private float mYDisplayed = -1;
	/**
	 * The FAB button's Y position when it is hidden.
	 */
	private float mYHidden = -1;
	private String mText;
	private CountDownTimer mTimer;

	public FloatingActionButton(Context context) {
		this(context, null);
	}

	public FloatingActionButton(Context context, AttributeSet attributeSet) {
		this(context, attributeSet, 0);
	}

	public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingActionButton);
		mColor = a.getColor(R.styleable.FloatingActionButton_android_color, Color.WHITE);
		mButtonPaint.setStyle(Paint.Style.FILL);
		mButtonPaint.setColor(mColor);
		float radius, dx, dy;
		radius = a.getFloat(R.styleable.FloatingActionButton_shadowRadius, 10.0f);
		dx = a.getFloat(R.styleable.FloatingActionButton_shadowDx, 0.0f);
		dy = a.getFloat(R.styleable.FloatingActionButton_shadowDy, 3.5f);
		int color = a.getInteger(R.styleable.FloatingActionButton_shadowColor, Color.argb(100, 0, 0, 0));
		mText = a.getString(R.styleable.FloatingActionButton_text);
		MZLog.d("J", "------------Text:" + mText);
		mButtonPaint.setShadowLayer(radius, dx, dy, color);

		Drawable drawable = a.getDrawable(R.styleable.FloatingActionButton_drawable);
		if (null != drawable) {
			mBitmap = ((BitmapDrawable) drawable).getBitmap();
		}
		setWillNotDraw(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = mWindowManager.getDefaultDisplay();
		Point size = new Point();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			display.getSize(size);
			mYHidden = size.y;
		} else
			mYHidden = display.getHeight();
	}

	public static int darkenColor(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.8f;
		return Color.HSVToColor(hsv);
	}

	public void setColor(int color) {
		mColor = color;
		mButtonPaint.setColor(mColor);
		invalidate();
	}

	public void setDrawable(Drawable drawable) {
		mBitmap = ((BitmapDrawable) drawable).getBitmap();
		invalidate();
	}

	public void setShadow(float radius, float dx, float dy, int color) {
		mButtonPaint.setShadowLayer(radius, dx, dy, color);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), mButtonPaint);
		if (null != mBitmap) {
			canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2, (getHeight() - mBitmap.getHeight()) / 2, mDrawablePaint);
		}
		if (TextUtils.isEmpty(mText)) return;
		
		TextPaint paint = new TextPaint();
		paint.setAntiAlias(true);
		paint.setTextSize(getTextSize());
		paint.setTextAlign(Align.CENTER);
		paint.setColor(Color.WHITE);
		int xPos = (canvas.getWidth() / 2);
		int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
		canvas.drawText(mText, xPos, yPos, paint);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// Perform the default behavior
		super.onLayout(changed, left, top, right, bottom);
		if (mLeftDisplayed == -1) {
			mLeftDisplayed = left;
			mRightDisplayed = right;
			mTopDisplayed = top;
			mBottomDisplayed = bottom;
		}

		// Store the FAB button's displayed Y position if we are not already
		// aware of it
		if (mYDisplayed == -1) {
			mYDisplayed = ViewHelper.getY(this);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int color;
		if (event.getAction() == MotionEvent.ACTION_UP) {
			color = mColor;
		} else {
			color = darkenColor(mColor);
			rect = new Rect(mLeftDisplayed, mTopDisplayed, mRightDisplayed, mBottomDisplayed);
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (!rect.contains(mLeftDisplayed + (int) event.getX(), mTopDisplayed + (int) event.getY())) {
				color = mColor;
			}
		}
		mButtonPaint.setColor(color);
		invalidate();
		return super.onTouchEvent(event);
	}

	public void hide(boolean hide) {
		// If the hidden state is being updated
		if (!hide) {
			MZLog.d("J", "floating button is first show");
			if (mMillisLeft == 0) {
				setVisibility(View.GONE);
				return;
			}
			startCountDownTimer();
		}
		if (hide && mTimer != null) {
			mTimer.cancel();
		}
		if (mHidden != hide) {
			// Store the new hidden state
			mHidden = hide;
			// Animate the FAB to it's new Y position
			ObjectAnimator animator = ObjectAnimator.ofFloat(this, "y", mHidden ? mYHidden : mYDisplayed).setDuration(500);
			animator.setInterpolator(mInterpolator);
			animator.start();
		}
	}

	void scrollChange(boolean hide) {
		// If the hidden state is being updated
		if (mHidden != hide) {
			// Store the new hidden state
			mHidden = hide;

			// Animate the FAB to it's new Y position
			ObjectAnimator animator = ObjectAnimator.ofFloat(this, "y", mHidden ? mYHidden : mYDisplayed).setDuration(500);
			animator.setInterpolator(mInterpolator);
			animator.start();
		}
	}

	public void listenTo(AbsListView listView) {
		if (null != listView) {
			listView.setOnScrollListener(new DirectionScrollListener(this));
		}
	}

	private void startCountDownTimer() {
		MZLog.d("J", "======startCountDownTimer");
		if (mTimer != null) {
			mTimer.cancel();
		}
		this.mTimer = new MyCountDownTimer(mMillisLeft, 1000);
		this.mTimer.start();
	}

	/**
	 * 
	 * @ClassName: MyCountDownTimer
	 * @Description: 倒计时器，倒计时TIME_DISMISS后,若未点击悬浮窗自动消失
	 * @author J.Beyond
	 * @date 2015年5月15日 上午10:41:26
	 *
	 */
	private class MyCountDownTimer extends CountDownTimer {

		public MyCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mMillisLeft = millisUntilFinished;
		}

		@Override
		public void onFinish() {
			mMillisLeft = 0;
			startAlphaAnimation();
			MZLog.i("J", "CountDownTimer finish,float button dismiss");
		}
	}

	private void startAlphaAnimation() {
		// 渐变动画 从显示（1.0）到隐藏（0.0）
		AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
		alphaAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				setVisibility(View.GONE);
			}
		});
		// 执行三秒
		alphaAnim.setDuration(1000);
		this.startAnimation(alphaAnim);
	}
}
