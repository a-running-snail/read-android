package com.jingdong.app.reader.view;

import java.io.InputStream;

import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义扇形进度条
 * 
 * @author WANGGUODONG
 * 使用方法：
 *         <com.jingdong.app.reader.view.CustomPieProgress
 *           xmlns:PieProgress="http://schemas.android.com/apk/res-auto"
 *           android:id="@+id/pie_progress"
 *           android:layout_width="25dp"
 *           android:layout_height="25dp"
 *           PieProgress:foregroundColor="#ed7057"
 *           PieProgress:backgroundColor="#ed7057" 
 *    />
 *    示例用法
 *    	final Runnable indicatorRunnable = new Runnable() {
		public void run() {
			pieRunning = true;
			while (pieProgress < 361) {
				mPieProgress2.setProgress(pieProgress);
				pieProgress += 2;;
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			pieRunning = false;
		}
	};
 * 
 */
public class CustomPieProgress extends View {
	private final RectF mRect = new RectF();
	private final RectF mRectInner = new RectF();
	private final Paint mPaintForeground = new Paint();
	private final Paint mPaintBackground = new Paint();
	private final Paint mPaintErase = new Paint();
	private static final Xfermode PORTER_DUFF_CLEAR = new PorterDuffXfermode(
			PorterDuff.Mode.CLEAR);
	private int mColorForeground = Color.WHITE;
	private int mColorBackground = Color.BLACK;
	private int mProgress;
	private int borderWidth = 2;

	private Bitmap mBitmap;

	private static final float INNER_RADIUS_RATIO = 0.84f;

	public CustomPieProgress(Context context) {
		this(context, null);
	}

	public CustomPieProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		parseAttributes(context.obtainStyledAttributes(attrs,
				R.styleable.CustomPieProgress));
		mPaintForeground.setColor(mColorForeground);
		mPaintForeground.setAntiAlias(true);
		mPaintBackground.setColor(mColorBackground);
		mPaintBackground.setAntiAlias(true);
		mPaintErase.setXfermode(PORTER_DUFF_CLEAR);
		mPaintErase.setAntiAlias(true);
	}

	private void parseAttributes(TypedArray a) {
		mColorForeground = a
				.getColor(R.styleable.CustomPieProgress_foregroundColor,
						mColorForeground);
		mColorBackground = a
				.getColor(R.styleable.CustomPieProgress_backgroundColor,
						mColorBackground);
		//borderWidth = (int) a.getDimension(
		//		R.styleable.CustomPieProgress_borderWidth, borderWidth);
		a.recycle();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBitmap, getWidth() / 2 - mBitmap.getWidth() / 2,
				getHeight() / 2 - mBitmap.getHeight() / 2, null);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		float bitmapWidth = w;
		float bitmapHeight = h;
		float radius = Math.min(bitmapWidth / 2, bitmapHeight / 2);
		mRect.set(0, 0, bitmapWidth, bitmapHeight);
		radius *= INNER_RADIUS_RATIO;
		mRectInner.set(bitmapWidth / 2f - radius, bitmapHeight / 2f - radius,
				bitmapWidth / 2f + radius, bitmapHeight / 2f + radius);
		updateBitmap();
	}

	public void setForegroundColor(int color) {
		this.mColorForeground = color;
		mPaintForeground.setColor(color);
		invalidate();
	}

	public void setBackgroundColor(int color) {
		this.mColorBackground = color;
		mPaintBackground.setColor(color);
		invalidate();
	}

	public synchronized void setProgress(int progress) {
		mProgress = progress;
		if (progress > 360) {
			mProgress = 360;
		}
		updateBitmap();
	}

	public void reset() {
		mProgress = 0;
	}
	
	
	public void setDefaultImageResource(int resId)
	{
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		InputStream is = getContext().getResources().openRawResource(resId);
		mBitmap = BitmapFactory.decodeStream(is, null, opt);
	}

	private void updateBitmap() {
		if (mRect == null || mRect.width() == 0) {
			return;
		}
		

			mBitmap = Bitmap.createBitmap((int) mRect.width(),
					(int) mRect.height(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(mBitmap);
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			//绘制边框
			mPaintBackground.setStrokeWidth(borderWidth);
			mPaintBackground.setStyle(Style.STROKE);
			mPaintBackground.setAntiAlias(true);
			
			canvas.drawCircle(mRect.width() / 2, mRect.height() / 2, mRect.width()
					/ 2 - borderWidth / 2, mPaintBackground);
			//绘制扇形
			canvas.drawArc(mRect, -90, mProgress, true, mPaintForeground);
		
		postInvalidate();
	}
}