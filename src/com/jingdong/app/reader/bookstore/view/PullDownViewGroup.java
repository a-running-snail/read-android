package com.jingdong.app.reader.bookstore.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.util.MZLog;

/**
 *
 * ##################################################### # # # _oo0oo_ # #
 * o8888888o # # 88" . "88 # # (| -_- |) # # 0\ = /0 # # ___/`---'\___ # # .'
 * \\| |# '. # # / \\||| : |||# \ # # / _||||| -:- |||||- \ # # | | \\\ - #/ | |
 * # # | \_| ''\---/'' |_/ | # # \ .-\__ '-' ___/-. / # # ___'. .' /--.--\ `.
 * .'___ # # ."" '< `.___\_<|>_/___.' >' "". # # | | : `- \`.;`\ _ /`;.`/ - ` :
 * | | # # \ \ `_. \_ __\ /__ _/ .-` / / # # =====`-.____`.___
 * \_____/___.-`___.-'===== # # `=---=' # #
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ # # # # 佛祖保佑 永无BUG # # #
 * #####################################################
 *
 * @ClassName: PullDownViewGroup
 * @Description: 可实现上下拉翻页的ViewGroup
 * @author J.Beyond
 * @date 2015年7月13日 下午3:38:04
 */
@SuppressLint("NewApi")
public class PullDownViewGroup extends ViewGroup {

	private static final int SHOW_UP_VIEW = 0;
	private static final int SHOW_DOWN_VIEW = 1;
	private static final float TOUCHPARAM = 0.6f;
	private Context mContext;

	private ViewGroup upView;
	private View hitView;
	private ViewGroup downView;

	private int upViewHeight, hitViewHeight, downViewHeight;

	private Scroller mScroller;
	private int downTermina;
	private int upTermina;
	private int upStartPosition;
	private int mTouchSlop;
	private int mMinimumVelocity;
	private int mMaximumVelocity;
	private VelocityTracker mVelocityTracker;
	private float mLastMotionY;
	private float mLastMotionX;
	private int mTotalHeight;
	private int showState = SHOW_UP_VIEW;
	private RotateAnimation mFlipAnimation;
	private RotateAnimation mReverseFlipAnimation;

	private static final Interpolator sInterpolator = new Interpolator() {
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t * t * t + 1.0f;
		}
	};
	private static final String TAG = "PullDownViewGroup";

	public PullDownViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLayout(context);
	}

	public PullDownViewGroup(Context context) {
		this(context, null);
	}

	/**
	 * 
	 * @Title: initLayout
	 * @Description: 初始化控件、手势参数
	 * @param @param context
	 * @return void
	 * @throws
	 */
	private void initLayout(Context context) {
		mContext = context;
		mScroller = new Scroller(context);
		LayoutParams uplp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		upView = new RelativeLayout(mContext);
		addView(upView, uplp);

		LayoutParams htlp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		hitView = View.inflate(mContext, R.layout.bookstore_index_recommend_header, null);
		addView(hitView, htlp);
		imageViewIndex = (ImageView) hitView.findViewById(R.id.bookstore_index_recomment_header_iv);
		tvHitViewTip = (TextView) hitView.findViewById(R.id.bookstore_index_recomment_header_tv);

		downView = new RelativeLayout(mContext);
		LayoutParams downlp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addView(downView, downlp);

		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		initAnim();
	}

	/**
	 * 
	 * @Title: initAnim
	 * @Description: 初始化动画
	 * @param
	 * @return void
	 * @throws
	 */
	private void initAnim() {
		mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mFlipAnimation.setInterpolator(new LinearInterpolator());
		mFlipAnimation.setDuration(250);
		mFlipAnimation.setFillAfter(true);

		mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
		mReverseFlipAnimation.setDuration(250);
		mReverseFlipAnimation.setFillAfter(true);
	}

	private boolean mIsInEdge;

	/**
	 * 
	 *
	 * @ClassName: PullDownViewCallBack
	 * @Description: 回调接口，处理与外界交互
	 * @author J.Beyond
	 * @date 2015年7月13日 下午3:34:21
	 * 
	 */
	public interface PullDownViewCallBack {
		public void showUpPartCallBack();
	}

	private PullDownViewCallBack callBack;

	public void setPullDownViewCallBack(PullDownViewCallBack callBack) {
		this.callBack = callBack;
	}

	private String Tag = "PullDownViewGroup";
	private int INVALID_POINTER = -1;
	private int mActivePointerId = INVALID_POINTER;

	private boolean mIsUnableToDrag;
	private boolean mIsBeingDragged = false;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		MZLog.d(Tag, "viewgroup--onInterceptTouchEvent--->mIsUnableToDrag=" + mIsUnableToDrag);

		if (showState == SHOW_DOWN_VIEW) {
			final int action = event.getAction() & MotionEventCompat.ACTION_MASK;
			if (action == MotionEvent.ACTION_CANCEL || (action != MotionEvent.ACTION_DOWN && mIsUnableToDrag)) {
				MZLog.d(Tag, "onInterceptTouchEvent--action-MotionEvent.ACTION_CANCEL:action:" + action + " mIsUnableToDrag:" + mIsUnableToDrag);
				endDrag();
				return false;
			}
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				MZLog.d(Tag, "onInterceptTouchEvent--MotionEvent.ACTION_DOWN:");
				int index = MotionEventCompat.getActionIndex(event);
				mActivePointerId = MotionEventCompat.getPointerId(event, index);
				if (mActivePointerId == INVALID_POINTER)
					break;
				mLastMotionX = MotionEventCompat.getX(event, index);
				mLastMotionY = MotionEventCompat.getY(event, index);

				break;

			case MotionEvent.ACTION_MOVE:
				MZLog.d(Tag, "onInterceptTouchEvent--MotionEvent.ACTION_MOVE:");
				determing(event);
				break;
			case MotionEvent.ACTION_UP:

				break;

			default:
				break;
			}

			if (!mIsBeingDragged) {
				obtainVelocityTracker(event);
			}
			MZLog.d(Tag, "onInterceptTouchEvent--mIsBeingDragged:" + mIsBeingDragged);
		}

		return mIsBeingDragged;
	}

	/**
	 * 
	 * @Title: determing
	 * @Description: 处理事件拦截
	 * @param @param event
	 * @return void
	 * @throws
	 */
	private void determing(MotionEvent event) {
		final int activePointerId = mActivePointerId;
		final int pointerIndex = getPointerIndex(event, activePointerId);
		if (activePointerId == INVALID_POINTER)
			return;
		final float x = MotionEventCompat.getX(event, pointerIndex);
		final float dx = x - mLastMotionX;
		final float xDiff = Math.abs(dx);
		final float y = MotionEventCompat.getY(event, pointerIndex);
		final float dy = y - mLastMotionY;
		final float yDiff = Math.abs(dy);
		// ViewParent parent = getParent();

		MZLog.d(TAG, "viewGroup#determing-------\nx=" + x + "\ndx=" + dx + "\nxDiff=" + xDiff + "\ny=" + y + "\ndy=" + dy + "\nyDiff=" + yDiff
				+ "\nmTouchSlop=" + mTouchSlop);
		DownPartScrollView downPartScrollView = (DownPartScrollView) downView.getChildAt(0);
		InnerScrollView scrollView = downPartScrollView.getScrollView();
		int scrollDivid = scrollView.getScrollY();
		int mScrollViewHeight = scrollView.getMeasuredHeight();
		int childHeight = scrollView.getChildAt(0).getMeasuredHeight();
		MZLog.d(Tag, ">>>>>>>>>>>\nscrollDivid=" + scrollDivid + "\nmScrollViewHeight=" + mScrollViewHeight + "\nchildHeight=" + childHeight
				+ "\nscrollDivid+mScrollViewHeight=" + (scrollDivid + mScrollViewHeight) + "\n<<<<<<<<<<<<");
		if (showState == SHOW_DOWN_VIEW) {
			MZLog.d(Tag, "showState ==SHOW_DOWN_VIEW\ndy=" + dy);
			if (dy > 0) {// 下拉
				if (scrollDivid == 0) {
					mIsBeingDragged = true;
					mLastMotionX = x;
					mLastMotionY = y;
					return;
				} else {
					MZLog.d(Tag, "showState ==SHOW_DOWN_VIEW---11111");
					mIsUnableToDrag = true;
					return;
				}

			}
			if (dy < 0) {// 上滑
				if (childHeight > scrollDivid + mScrollViewHeight) {
					MZLog.d(Tag, "showState ==SHOW_DOWN_VIEW---22222");
					mIsUnableToDrag = true;
					return;
				} else {
					mIsUnableToDrag = false;
					return;
				}
			}
		} else if (showState == SHOW_UP_VIEW) {
			mIsUnableToDrag = true;
			return;
		}

		if (yDiff > mTouchSlop && yDiff > xDiff) {
			mIsBeingDragged = true;
			mLastMotionX = x;
			mLastMotionY = y;
		} else if (yDiff > mTouchSlop) {
			MZLog.d(Tag, "mIsUnableToDrag---xdiff is big");
			mIsUnableToDrag = true;
		}
		MZLog.d(Tag, "dy:" + dy + "   showState " + showState + "  mIsBeingDragged:" + mIsBeingDragged + "   mIsUnableToDrag:" + mIsUnableToDrag);
	}

	private void endDrag() {
		mActivePointerId = INVALID_POINTER;
		bottomTipState = 0;
		mIsBeingDragged = false;
		mIsUnableToDrag = false;
		releaseVelocityTracker();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (showState == SHOW_DOWN_VIEW) {
			if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
				return false;
			}
			if (scrollState == SCROLL_STATE_SCROLLING) {
				return false;
			}

			if (!mIsBeingDragged) {
				return false;
			}

			obtainVelocityTracker(event);

			final int action = event.getAction() & MotionEventCompat.ACTION_MASK;

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				MZLog.d(TAG, "ACTION_DOWN#currentScrollY:" + getScrollY() + ", mLastMotionY:" + mLastMotionY);
				int index = MotionEventCompat.getActionIndex(event);
				mActivePointerId = MotionEventCompat.getPointerId(event, index);
				mLastMotionY = event.getY();
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
				mLastMotionY = event.getY();
				break;

			case MotionEvent.ACTION_MOVE:

				MZLog.d(Tag, "onTouchEvent--MotionEvent.ACTION_MOVE:");
				if (!mIsBeingDragged) {
					determing(event);
					if (mIsUnableToDrag)
						return false;
				}
				if (mIsBeingDragged) {
					final int activePointerIndex = getPointerIndex(event, mActivePointerId);
					if (mActivePointerId == INVALID_POINTER)
						break;
					final float y = MotionEventCompat.getY(event, activePointerIndex);
					float deltaY = y - mLastMotionY;
					mLastMotionY = y;
					float scrollDip = deltaY * TOUCHPARAM;
					judgeState();
					if (deltaY < 0) {
						if (getScrollY() > 0) {
							scrollBy(0, (int) -scrollDip);
						}
					} else if (deltaY > 0) {
						if (showState == SHOW_DOWN_VIEW) {
							mIsInEdge = getScrollY() <= upViewHeight + hitViewHeight;
							if (mIsInEdge) {
								scrollBy(0, (int) -scrollDip);
							}
						}
					}
				}
				break;

			case MotionEvent.ACTION_UP:

				if (mIsBeingDragged) {
					if (showState == SHOW_DOWN_VIEW) {
						if (getScrollY() >= upStartPosition) {
							showDownView();
						} else {
							showUpView(300);
						}
					}
					endDrag();
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				if (mIsBeingDragged) {
					endDrag();
				}
				break;
			}

			return true;
		} else {
			return false;
		}
	}

	private int bottomTipState = 0;
	private final int RELEASE_TO_REFRESH = 200;
	private final int PULL_TO_REFRESH = 2001;

	private void judgeState() {
		if (showState == SHOW_DOWN_VIEW) {
			if (getScrollY() >= upStartPosition && bottomTipState != PULL_TO_REFRESH) {
				// 提示 继续下拉回首页
				tvHitViewTip.setText("继续下拉回首页");
				imageViewIndex.clearAnimation();
				imageViewIndex.startAnimation(mReverseFlipAnimation);
				bottomTipState = PULL_TO_REFRESH;
			} else if (getScrollY() < upStartPosition && bottomTipState != RELEASE_TO_REFRESH) {
				// 提示 松开回首页
				tvHitViewTip.setText("松开回首页");
				imageViewIndex.clearAnimation();
				imageViewIndex.startAnimation(mFlipAnimation);
				bottomTipState = RELEASE_TO_REFRESH;
			}
		}
	}

	public void fling(int velocityY) {
		if (getChildCount() > 0) {
			mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, 0, mTotalHeight);
			final boolean movingDown = velocityY > 0;
			awakenScrollBars(mScroller.getDuration());
			invalidate();
		}
	}

	private void obtainVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	public void addUpViewContent(View view) {
		upView.addView(view, new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
	}

	public void addDownViewContent(View view) {
		downView.addView(view, new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
	}

	private final int SCROLL_STATE_SCROLLING = 1;
	private final int SCROLL_STATE_IDL = 0;
	private int scrollState = SCROLL_STATE_IDL;
	private ImageView imageViewIndex;
	private TextView tvHitViewTip;

	public void showDownView() {
		showState = SHOW_DOWN_VIEW;
		mIsUnableToDrag = false;
		scrollState = SCROLL_STATE_SCROLLING;
		// MZLog.d(TAG,
		// "&&&&&&&showDownView-->getScrollX()="+getScrollX()+"\ngetScrollY()="
		// +getScrollY()+"\ndownTermina="+downTermina+"\n(upIDLpostion - getScrollY()="+(int)
		// (downTermina - getScrollY()));
		mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), (int) (downTermina - getScrollY()), 300);
		invalidate();
	}

	public void showUpView(int during) {
		showState = SHOW_UP_VIEW;
		scrollState = SCROLL_STATE_SCROLLING;
		if (callBack != null) {
			callBack.showUpPartCallBack();
		}
		mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), (int) (upTermina - getScrollY()), during);
		invalidate();
	}

	boolean isSlidingToTopFromBottom = false;

	/**
	 * 
	 * @Title: slideToTop
	 * @Description: 滑动到顶部
	 * @param
	 * @return void
	 * @throws
	 */
	public void slideToTop() {
		if (isDownView()) {
			isSlidingToTopFromBottom = true;
			downpartSlideToTop();
			showUpView(10);
		} else {
			upperPartSlideToTop();
		}
	}

	/**
	 * 
	 * @Title: downpartSlideToTop
	 * @Description: 下半部分滑动到顶部
	 * @param
	 * @return void
	 * @throws
	 */
	private void upperPartSlideToTop() {
		UpperPartScrollView upperViewRoot = (UpperPartScrollView) upView.getChildAt(0);
		if (upperViewRoot != null) {
			final InnerScrollView upScrollView = upperViewRoot.getScrollView();
			if (upScrollView == null)
				return;
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					upScrollView.setScrollable(true);
					upScrollView.fullScroll(ScrollView.FOCUS_UP);
				}
			});
		}
	}

	/**
	 * 
	 * @Title: upperPartSlideToTop
	 * @Description: 上半部分滑动到顶部
	 * @param
	 * @return void
	 * @throws
	 */
	private void downpartSlideToTop() {
		DownPartScrollView downViewRoot = (DownPartScrollView) downView.getChildAt(0);
		if (downViewRoot == null)
			return;
		final InnerScrollView downScrollView = downViewRoot.getScrollView();
		if (downScrollView == null)
			return;
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				downScrollView.setScrollable(true);
				downScrollView.fullScroll(ScrollView.FOCUS_UP);
			}
		});
	}

	Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);
		}
	};

	@Override
	public void computeScroll() {
		if (!mScroller.isFinished()) {
			if (mScroller.computeScrollOffset()) {
				Log.d(TAG, "viewgroup---mScroller.computeScrollOffset()=" + mScroller.computeScrollOffset());
				int oldX = getScrollX();
				int oldY = getScrollY();
				int x = mScroller.getCurrX();
				int y = mScroller.getCurrY();

				if (oldX != x || oldY != y) {
					scrollTo(x, y);
				}
				invalidate();
				return;
			}
		} else {
			scrollState = SCROLL_STATE_IDL;
			if (isSlidingToTopFromBottom) {
				isSlidingToTopFromBottom = false;
				MZLog.d(TAG, "computeScroll-->upperPartSlideToTop");
				upperPartSlideToTop();
			}
		}
	}

	private int getPointerIndex(MotionEvent ev, int id) {
		int activePointerIndex = MotionEventCompat.findPointerIndex(ev, id);
		if (activePointerIndex == -1)
			mActivePointerId = INVALID_POINTER;
		return activePointerIndex;
	}

	public boolean isDownView() {
		if (showState == SHOW_DOWN_VIEW) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		Log.e(TAG, "onLayout^^^^^^^^^^\nl=" + l + "\nt=" + t + "\nr=" + r + "\nb=" + b + "\nupViewHeight=" + upViewHeight + "\nhitViewHeight=" + hitViewHeight
//				+ "\ndownViewHeight=" + downViewHeight);
		upView.layout(l, t, r, upViewHeight);
		hitView.layout(l, t + upViewHeight, r, t + upViewHeight + hitViewHeight);
		downView.layout(l, t + upViewHeight + hitViewHeight, r, t + upViewHeight + hitViewHeight + downViewHeight);
		upTermina = t;
		downTermina = t + upViewHeight + hitViewHeight;
		upStartPosition = t + upViewHeight;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measureWidth = 0;
		int measureHeight = 0;
		upView.measure(widthMeasureSpec, getChildMeasureSpec(heightMeasureSpec, 0, upView.getLayoutParams().height));
		upViewHeight = upView.getMeasuredHeight();
		measureHeight = upViewHeight;

		hitView.measure(widthMeasureSpec, getChildMeasureSpec(heightMeasureSpec, 0, hitView.getLayoutParams().height));
		measureHeight += hitView.getMeasuredHeight();
		hitViewHeight = hitView.getMeasuredHeight();
		downView.measure(widthMeasureSpec, getChildMeasureSpec(heightMeasureSpec, 0, downView.getLayoutParams().height));
		downViewHeight = downView.getMeasuredHeight();
		measureHeight += downViewHeight;
		measureWidth = upView.getMeasuredWidth();
		mTotalHeight = measureHeight;
		setMeasuredDimension(measureWidth, measureHeight);
	}

}
