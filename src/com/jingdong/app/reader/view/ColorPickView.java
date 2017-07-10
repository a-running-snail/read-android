package com.jingdong.app.reader.view;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.ScreenUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class ColorPickView extends View {
	private Context context;
	private int bigCircle; // 外圈半径
	private int rudeRadius; // 可移动小球的半径
	private int centerColor; // 可移动小球的颜色
	private Bitmap bitmapBack; // 背景图片
	// private Bitmap bitmapDot;
	private Paint mPaint; // 背景画笔
	private Paint mCenterPaint; // 可移动小球画笔
	private Point centerPoint;// 中心位置
	private Point mRockPosition;// 小球当前位置
	private OnColorChangedListener listener; // 小球移动的监听
	private int length; // 小球到中心位置的距离

	private Paint mRectPaint;// 渐变方块画笔
	private Paint mLinePaint;// 分隔线画笔
	private Shader rectShader;// 渐变方块渐变图像
	private float rectLeft;// 渐变方块左x坐标
	private float rectTop;// 渐变方块右x坐标
	private float rectRight;// 渐变方块上y坐标
	private float rectBottom;// 渐变方块下y坐标

	private int[] mCircleColors;// 渐变色环颜色
	private int[] mRectColors;// 渐变方块颜色

	private boolean downInCircle = true;// 按在渐变环上
	private boolean downInRect;// 按在渐变方块上
	private boolean highlightCenter;// 高亮
	private boolean highlightCenterLittle;// 微亮

	private Point movePoint;// 可移动滑块初始位置
	private Paint mMovePaint; // 可移动滑块画笔
	private int width;
	private int rockx;
	private int rocky;
	private int move;
	private int trockx;
	private int trocky;
	private int tmove;

	private Paint myPaint;
	private boolean is_bg;

	public ColorPickView(Context context) {
		super(context);
	}

	public ColorPickView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		init(attrs);
	}

	public ColorPickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init(attrs);
	}

	public void setOnColorChangedListener(OnColorChangedListener listener) {
		this.listener = listener;
	}

	/**
	 * @describe 初始化操作
	 * @param attrs
	 */
	private void init(AttributeSet attrs) {
		// 获取自定义组件的属性
		TypedArray types = context.obtainStyledAttributes(attrs,
				R.styleable.color_picker);
		try {
			bigCircle = types.getDimensionPixelOffset(
					R.styleable.color_picker_circle_radius, 0);
			rudeRadius = types.getDimensionPixelOffset(
					R.styleable.color_picker_center_radius, 0);
			centerColor = types.getColor(R.styleable.color_picker_center_color,
					Color.BLACK);
		} finally {
			types.recycle(); // TypeArray用完需要recycle
		}
		width = (int) ScreenUtils.getWidthJust(context);
		// 将背景图片大小设置为属性设置的直径
		bitmapBack = BitmapFactory.decodeResource(getResources(),
				R.drawable.reader_colorwheel);
		bitmapBack = Bitmap.createScaledBitmap(bitmapBack, bigCircle * 2,
				bigCircle * 2, false);
		// bitmapDot = BitmapFactory.decodeResource(getResources(),
		// R.drawable.reader_colorpicker_cross_standard);
		// bitmapDot = Bitmap.createScaledBitmap(bitmapDot, rudeRadius * 2,
		// rudeRadius * 2, false);
	
	}
	
	public void initView(boolean isbg){
		// 中心位置坐标
		myPaint = new Paint();
		is_bg = isbg;
		if (isbg) {
			rockx = LocalUserSetting.getRockPositionX(context);
			rocky = LocalUserSetting.getRockPositionY(context);
			move = LocalUserSetting.getMovePoint(context);
			if (rockx == -1 && rocky == -1) {
				mRockPosition = new Point(width / 2, bigCircle);
			} else {
				mRockPosition = new Point(rockx, rocky);
				myPaint.setColor(bitmapBack.getPixel(rockx - width / 2
						+ bigCircle, rocky));
			}
		} else {
			trockx = LocalUserSetting.getTRockPositionX(context);
			trocky = LocalUserSetting.getTRockPositionY(context);
			tmove = LocalUserSetting.getTMovePoint(context);
			if (trockx == -1 && trocky == -1) {
				mRockPosition = new Point(width / 2, bigCircle);
			} else {
				mRockPosition = new Point(trockx, trocky);
				myPaint.setColor(bitmapBack.getPixel(trockx - width / 2
						+ bigCircle, trocky));
			}
		}

		centerPoint = new Point(width / 2, bigCircle);

		// 初始化背景画笔和可移动小球的画笔
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mCenterPaint = new Paint();
		mCenterPaint.setStyle(Style.STROKE);
		mCenterPaint.setColor(centerColor);
		mCenterPaint.setAntiAlias(true);

		// 边框参数
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setColor(Color.WHITE);
		mLinePaint.setStrokeWidth(0);

		// 黑白渐变参数
		mRectColors = new int[] { 0xFFFFFFFF, myPaint.getColor(), 0xFF000000 };
		mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRectPaint.setStrokeWidth(5);
		// rectLeft = -bigCircle - mPaint.getStrokeWidth() * 0.5f;
		rectLeft = -(width / 2 - bigCircle - ScreenUtils.dip2px(32));
		rectTop = bigCircle * 2 + mLinePaint.getStrokeWidth() * 0.5f
				+ mLinePaint.getStrokeMiter() * 0.5f + ScreenUtils.dip2px(22);
		rectRight = width - ScreenUtils.dip2px(64) + rectLeft;
		rectBottom = rectTop + ScreenUtils.dip2px(8);
		if (isbg) {
			if (move == -1) {
				movePoint = new Point(width / 2, bigCircle * 2
						+ ScreenUtils.dip2px(28));
			} else {
				movePoint = new Point(move, bigCircle * 2
						+ ScreenUtils.dip2px(28));
			}
		} else {
			if (tmove == -1) {
				movePoint = new Point(width / 2, bigCircle * 2
						+ ScreenUtils.dip2px(28));
			} else {
				movePoint = new Point(tmove, bigCircle * 2
						+ ScreenUtils.dip2px(28));
			}
		}
		mMovePaint = new Paint();
		mMovePaint.setStyle(Style.STROKE);
		mMovePaint.setColor(centerColor);
		mMovePaint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 移动中心
		canvas.translate(width / 2 - bigCircle, 0);
		// 画背景图片
		canvas.drawBitmap(bitmapBack, 0, 0, mPaint);
		// 画中心小球
		canvas.drawCircle(mRockPosition.x - width / 2 + bigCircle,
				mRockPosition.y, rudeRadius, mCenterPaint);
		// canvas.drawBitmap(bitmapDot, mRockPosition.x, mRockPosition.y,
		// mCenterPaint);

		// 画黑白渐变块
		if (downInCircle) {
			mRectColors[1] = myPaint.getColor();
		}
		rectShader = new LinearGradient(rectLeft, 0, rectRight, 0, mRectColors,
				null, Shader.TileMode.MIRROR);
		mRectPaint.setShader(rectShader);
		// canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom,
		// mRectPaint);
		// 画圆角矩形
		RectF oval = new RectF(rectLeft, rectTop, rectRight, rectBottom);// 设置个新的长方形
		canvas.drawRoundRect(oval, 8, 8, mRectPaint);// 第二个参数是x半径，第三个参数是y半径
		// float offset = mLinePaint.getStrokeWidth() / 2;
		// canvas.drawLine(rectLeft - offset, rectTop - offset * 2,
		// rectLeft - offset, rectBottom + offset * 2, mLinePaint);//左
		// canvas.drawLine(rectLeft - offset * 2, rectTop - offset,
		// rectRight + offset * 2, rectTop - offset, mLinePaint);//上
		// canvas.drawLine(rectRight + offset, rectTop - offset * 2,
		// rectRight + offset, rectBottom + offset * 2, mLinePaint);//右
		// canvas.drawLine(rectLeft - offset * 2, rectBottom + offset,
		// rectRight + offset * 2, rectBottom + offset, mLinePaint);//下

		// 画可移动滑块
		canvas.drawCircle(movePoint.x - width / 2 + bigCircle, movePoint.y,
				rudeRadius, mMovePaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		boolean inRect = inRect(x, y);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: // 按下
			downInRect = inRect;
			length = getLength(event.getX(), event.getY(), centerPoint.x,
					centerPoint.y);
			if (length > bigCircle - rudeRadius) {
				return true;
			}
			break;
		case MotionEvent.ACTION_MOVE: // 移动
			length = getLength(event.getX(), event.getY(), centerPoint.x,
					centerPoint.y);
			if (length <= bigCircle - rudeRadius) {
				mRockPosition.set((int) event.getX(), (int) event.getY());
				if (mRockPosition.x > 0 && mRockPosition.y > 0) {
					myPaint.setColor(bitmapBack.getPixel(mRockPosition.x
							- width / 2 + bigCircle, mRockPosition.y));
//					movePoint.set(width / 2,
//							bigCircle * 2 + ScreenUtils.dip2px(26));
//					listener.onColorChange(myPaint.getColor());
					mRectColors[1] = myPaint.getColor();
					listener.onColorChange(interpRectColor(mRectColors, movePoint.x));
					if (is_bg) {
						LocalUserSetting.saveRockPositionX(context,
								mRockPosition.x);
						LocalUserSetting.saveRockPositionY(context,
								mRockPosition.y);
					} else {
						LocalUserSetting.saveTRockPositionX(context,
								mRockPosition.x);
						LocalUserSetting.saveTRockPositionY(context,
								mRockPosition.y);
					}
				}
			} else if (downInRect && inRect) {// down在渐变方块内, 且move也在渐变方块内
				movePoint.set((int) event.getX(),
						bigCircle * 2 + ScreenUtils.dip2px(26));
				listener.onColorChange(interpRectColor(mRectColors, movePoint.x));
				if (is_bg) {
					LocalUserSetting.saveMovePoint(context, movePoint.x);
				} else {
					LocalUserSetting.saveTMovePoint(context, movePoint.x);
				}
			}

			break;
		case MotionEvent.ACTION_UP:// 抬起
			if (downInRect) {
				downInRect = false;
			}
			break;

		default:
			break;
		}
		invalidate(); // 更新画布
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 视图大小设置为直径
		setMeasuredDimension(width, bigCircle * 2 + ScreenUtils.dip2px(44));
	}

	/**
	 * 坐标是否在渐变色中
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean inRect(float x, float y) {
		if (x <= width - ScreenUtils.dip2px(32) && x >= ScreenUtils.dip2px(32)
				&& y <= rectBottom + ScreenUtils.dip2px(15)
				&& y >= rectTop - ScreenUtils.dip2px(15)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @describe 计算两点之间的位置
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static int getLength(float x1, float y1, float x2, float y2) {
		return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	/**
	 * @describe 当触摸点超出圆的范围的时候，设置小球边缘位置
	 * @param a
	 * @param b
	 * @param cutRadius
	 * @return
	 */
	public static Point getBorderPoint(Point a, Point b, int cutRadius) {
		float radian = getRadian(a, b);
		return new Point(a.x + (int) (cutRadius * Math.cos(radian)), a.x
				+ (int) (cutRadius * Math.sin(radian)));
	}

	/**
	 * @describe 触摸点与中心点之间直线与水平方向的夹角角度
	 * @param a
	 * @param b
	 * @return
	 */
	public static float getRadian(Point a, Point b) {
		float lenA = b.x - a.x;
		float lenB = b.y - a.y;
		float lenC = (float) Math.sqrt(lenA * lenA + lenB * lenB);
		float ang = (float) Math.acos(lenA / lenC);
		ang = ang * (b.y < a.y ? -1 : 1);
		return ang;
	}

	/**
	 * 获取渐变块上颜色
	 * 
	 * @param colors
	 * @param x
	 * @return
	 */
	int a, r, g, b, c0, c1;
	float p;

	private int interpRectColor(int colors[], float mx) {
		if (mx < width / 2) {
			c0 = colors[0];
			c1 = colors[1];
			p = (mx - ScreenUtils.dip2px(32))
					/ ((width - 2 * ScreenUtils.dip2px(32)) / 2);
		} else {
			c0 = colors[1];
			c1 = colors[2];
			p = (mx - width / 2) / ((width - 2 * ScreenUtils.dip2px(32)) / 2);
		}
		a = ave(Color.alpha(c0), Color.alpha(c1), p);
		r = ave(Color.red(c0), Color.red(c1), p);
		g = ave(Color.green(c0), Color.green(c1), p);
		b = ave(Color.blue(c0), Color.blue(c1), p);
		return Color.argb(a, r, g, b);
	}

	private int ave(int s, int d, float p) {
		return s + Math.round(p * (d - s));
	}

	// 颜色发生变化的回调接口
	public interface OnColorChangedListener {
		void onColorChange(int color);
	}
}
