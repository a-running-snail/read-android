package com.jingdong.app.reader.bookstore.view;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.util.MZLog;

/**
 *
 *
 * @ClassName: UpperPartScrollView
 * @Description: 下半部分ScrollView： 1）支持上拉加载更多；
 *               2）下拉到顶部时显示下拉提示，下拉到一定程度切换到上一个ScrollView
 * @author J.Beyond
 * @date 2015年7月7日 下午1:30:34
 */
public class DownPartScrollView extends LinearLayout {
	private Scroller mScroller;
	private Context mContext;
	// private View mHeaderView;
	private InnerScrollView mScrollView;
	private View mFooterView;
	private int mHeadViewHeight;
	private int mScrollHeight;
	private int mFootViewHeight;
	private int upIDLpostion;
	private int upRefreshPostion;
	private int footIDLPostion;
	private int footRefreshPostion;
	private VelocityTracker mVelocityTracker;
	private float mLastMotionY;
	private float mLastMotionX;
	/** 是一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件 */
	private int mTouchSlop;
	private int mMinimumVelocity;
	private int mMaximumVelocity;
	private TextView mFooterTextView;
	private ProgressBar mFooterProgressBar;
	private RotateAnimation mFlipAnimation;
	private RotateAnimation mReverseFlipAnimation;

	private static final Interpolator sInterpolator = new Interpolator() {
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t * t * t + 1.0f;
		}
	};
	private static final float TOUCHPARAM = 0.6f;
	private static final String TAG = "PullScrollView";
	private static final int INVALID_POINTER = -1;
	private static final String CLIENT_PREFERENCES = "pull2Refresh";
	private static final String KEY_LAST_UPDATA_TIMESTAMP = "key_last_updata_timestamp";
	private static final View LinearLayout = null;

	private final int SCROLL_STATE_IDL = 0;
	private int mActivePointerId;

	private int viewState;
	private boolean isRereshingData = false;
	private static final int STATE_PULL_DOWN_PREPARE = 1000;
	private static final int STATE_PULL_DOWN_RELEASE_TO_REFRESH = 1001;
	private static final int STATE_PULL_UP_PREPARE = 1100;
	private static final int STATE_PULL_UP_RELEASE_TO_REFRESH = 1101;
	private static final int STATE_IDL = 0;

	private boolean mIsBeingDragged = false;
	private String tag = "PullScrollView";
	private float mInitialMotionX;

	private int scrollState = SCROLL_STATE_IDL;
	private int SCROLL_STATE_SMOOTH_SCROLLING = 11111;
	private boolean mIsUnableToDrag = true;
	private boolean isTimeToFildUp = false;

	public DownPartScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLayout(context);
	}

	public DownPartScrollView(Context context) {
		this(context, null);
	}

	public InnerScrollView getScrollView() {
		return mScrollView;
	}

	/**
	 * 
	 * @Title: initLayout
	 * @Description: 初始化布局、手势等参数
	 * @param @param context
	 * @return void
	 * @throws
	 */
	private void initLayout(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mScroller = new Scroller(context, sInterpolator);

		LayoutParams scrolllp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mScrollView = new InnerScrollView(mContext);
		mScrollView.setVerticalScrollBarEnabled(false);
		addView(mScrollView, scrolllp);

		LayoutParams footerlp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mFooterView = View.inflate(mContext, R.layout.refresh_footer, null);
		mFooterTextView = (TextView) mFooterView.findViewById(R.id.pull_to_load_text);
		mFooterProgressBar = (ProgressBar) mFooterView.findViewById(R.id.pull_to_load_progress);
		addView(mFooterView, footerlp);

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

	/**
	 * 
	 * @Title: addScrollViewContent
	 * @Description: 对外公开的添加内容视图到ScrollView的方法
	 * @param @param view
	 * @return void
	 * @throws
	 */
	public void addScrollViewContent(View view) {
		mScrollView.addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (isLoadingMore) {
			MZLog.d(tag, "onInterceptTouchEvent---isLoadingMore=" + isLoadingMore);
			return false;
		}
		if (scrollState == SCROLL_STATE_SMOOTH_SCROLLING) {
			MZLog.d(tag, "onInterceptTouchEvent---scrollState == SCROLL_STATE_SMOOTH_SCROLLING:" + (scrollState == SCROLL_STATE_SMOOTH_SCROLLING) + "---------"
					+ isRereshingData);
			return true;
		}

		final int action = event.getAction() & MotionEventCompat.ACTION_MASK;
		MZLog.d(tag, "down--onInterceptTouchEvent--->mIsUnableToDrag=" + mIsUnableToDrag);
		if (action == MotionEvent.ACTION_CANCEL || (action != MotionEvent.ACTION_DOWN && mIsUnableToDrag)) {
			MZLog.d(tag, "onInterceptTouchEvent- action:  " + action + "  mIsUnableToDrag:" + mIsUnableToDrag);
			endDrag();
			return false;
		}

		final float x = event.getX();
		final float y = event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			MZLog.d(tag, "onInterceptTouchEvent--MotionEvent.ACTION_DOWN:");
			int index = MotionEventCompat.getActionIndex(event);
			mActivePointerId = MotionEventCompat.getPointerId(event, index);
			if (mActivePointerId == INVALID_POINTER)
				break;
			// 获取手指按下去时的X
			mLastMotionX = MotionEventCompat.getX(event, index);
			// 获取手指按下去时的Y
			mLastMotionY = MotionEventCompat.getY(event, index);
			break;

		case MotionEvent.ACTION_MOVE:
			MZLog.d(tag, "onInterceptTouchEvent--MotionEvent.ACTION_MOVE:");
			if (!isRereshingData) {
				determing(event);
			}
			break;
		case MotionEvent.ACTION_UP:

			break;

		default:
			break;
		}

		if (!mIsBeingDragged) {
			obtainVelocityTracker(event);
		}
		MZLog.d(tag, "down--->onInterceptTouchEvent--mIsBeingDragged:" + mIsBeingDragged);

		return mIsBeingDragged;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		MZLog.d(tag, "---onTouchEvent----");
		if (scrollState == SCROLL_STATE_SMOOTH_SCROLLING) {
			MZLog.d(tag, "onTouchEvent--scrollState == SCROLL_STATE_SMOOTH_SCROLLING:" + (scrollState == SCROLL_STATE_SMOOTH_SCROLLING) + "  mIsBeingDragged:"
					+ mIsBeingDragged + "  viewState:" + viewState + " isRereshingData:" + isRereshingData);
			return true;
		}

		// if (!mIsBeingDragged) {
		// return false;
		// }

		if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
			MZLog.d(tag, "onTouchEvent---- MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0");
			return false;
		}
		obtainVelocityTracker(event);

		final int action = event.getAction() & MotionEventCompat.ACTION_MASK;

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			MZLog.d(tag, "onTouchEvent--MotionEvent.ACTION_DOWN: +mIsBeingDragged:" + mIsBeingDragged + "  viewState:" + viewState);
			int index = MotionEventCompat.getActionIndex(event);
			mActivePointerId = MotionEventCompat.getPointerId(event, index);
			mLastMotionY = event.getY();
			// MZLog.d(TAG, "ACTION_DOWN#currentScrollY:" + getScrollY() +
			// ", mLastMotionY:" + mLastMotionY);
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			// if (mParentView!= null && mParentView.isDownView()) {
			// return true;
			// }

			break;

		case MotionEvent.ACTION_MOVE:

			if (!isRereshingData) {
				MZLog.d(tag, "onTouchEvent--MotionEvent.ACTION_MOVE1:+mIsBeingDragged:" + mIsBeingDragged + "  viewState:" + viewState);
				if (!mIsBeingDragged) {
					determing(event);
					if (mIsUnableToDrag) {
						MZLog.d(tag, "onTouchEvent--MotionEvent.ACTION_MOVE3:+mIsBeingDragged:" + mIsBeingDragged + "  viewState:" + viewState);
						return false;
					}
				}
				MZLog.d(tag, "onTouchEvent--MotionEvent.ACTION_MOVE2:+mIsBeingDragged:" + mIsBeingDragged + "  viewState:" + viewState);
				if (mIsBeingDragged) {

					final int activePointerIndex = getPointerIndex(event, mActivePointerId);
					if (mActivePointerId == INVALID_POINTER)
						break;
					final float y = MotionEventCompat.getY(event, activePointerIndex);
					// Y方向上的偏移量
					float deltaY = y - mLastMotionY;
					mLastMotionY = y;

					float oldScrollY = getScrollY();
					float scrollY = oldScrollY + deltaY;
					MZLog.d(tag, "@@@@@@@@@@@@@@\n y=" + y + "\n deltaY=" + deltaY + "\n oldScrollY=" + oldScrollY + "\n scrollY=" + scrollY + "\n upIDLpostion="
							+ upIDLpostion + "\n footIDLPostion=" + footIDLPostion + "\n@@@@@@@@@@@@@@");
					if (scrollY < upIDLpostion) {
						scrollY = upIDLpostion;
					} else if (scrollY > footIDLPostion) {
						scrollY = footIDLPostion;
					}
					// Don't lose the rounded component
					mLastMotionY += scrollY - (int) scrollY;
					judgeState(deltaY);
					float scrollDp = deltaY * TOUCHPARAM;
					MZLog.d(tag, "############\nmLastMotionY=" + mLastMotionY + "\nscrollDp=" + scrollDp + "\n############");
					scrollBy(0, (int) -scrollDp);
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			MZLog.d(tag, "onTouchEvent--MotionEvent.ACTION_UP:+mIsBeingDragged:" + mIsBeingDragged + "  viewState:" + viewState);
			// final VelocityTracker velocityTracker = mVelocityTracker;
			// velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
			// int initialVelocity = (int) velocityTracker.getYVelocity();
			endDrag();
			break;
		case MotionEvent.ACTION_CANCEL:
			MZLog.d(tag, "onTouchEvent--MotionEvent.ACTION_CANCEL:+mIsBeingDragged:" + mIsBeingDragged + "  viewState:" + viewState);
			// if (mIsBeingDragged) {
			endDrag();
			// }
			break;
		default:
			MZLog.d(tag, "onTouchEvent--default:" + event.getAction() + "   mIsBeingDragged:" + mIsBeingDragged + "  viewState:" + viewState);
			// if (mIsBeingDragged) {
			endDrag();
			// }
			break;
		}

		return true;
	}

	private void determing(MotionEvent event) {
		float x = 0, dx = 0, xDiff = 0, y = 0, dy = 0, yDiff = 0;
		try {
			final int activePointerId = mActivePointerId;
			final int pointerIndex = getPointerIndex(event, activePointerId);
			if (activePointerId == INVALID_POINTER)
				return;
			x = MotionEventCompat.getX(event, pointerIndex);
			// Calculate the distance moved
			dx = x - mLastMotionX;
			xDiff = Math.abs(dx);
			y = MotionEventCompat.getY(event, pointerIndex);
			dy = y - mLastMotionY;
			yDiff = Math.abs(dy);
			MZLog.d(tag, "down#determing-------\nx=" + x + "\ndx=" + dx + "\nxDiff=" + xDiff + "\ny=" + y + "\ndy=" + dy + "\nyDiff=" + yDiff + "\nmTouchSlop="
					+ mTouchSlop + "\n------");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		// scrollView滑动的距离
		int scrollDivid = mScrollView.getScrollY();
		// scrollView根控件（LinnearLayout）的总高度
		int firstChildHeight = mScrollView.getChildAt(0).getMeasuredHeight();
		MZLog.d(tag, ">>>>>>>>>>>>>>>\n firstChildHeight=" + firstChildHeight + "\nscrollDivid=" + scrollDivid + "\n mScrollHeight + scrollDivid="
				+ (mScrollHeight + scrollDivid) + "\n<<<<<<<<<<<<<<<<");
		if (scrollDivid == 0) {// 滑动到最顶部
			// if-else处理从上部分View切换到上部分View的临界条件
			if (dy < 0) {// 向下移动
				if (firstChildHeight > mScrollHeight + scrollDivid && getScrollY() <= 0) {
					MZLog.d(tag, "---11111 mIsUnableToDrag = true");
					mIsUnableToDrag = true;
					return;
				}
			} else {
				mIsUnableToDrag = false;
				return;
			}
		} else if (scrollDivid > 0) {// ScrollView滚动了一段距离

			if (dy > 0) {// 向上滑动
				if (firstChildHeight >= mScrollHeight + scrollDivid) {
					MZLog.d(tag, "---222222  mIsUnableToDrag = true  " + scrollDivid);
					mIsUnableToDrag = true;
					return;
				}
			}
			if (dy < 0) {// 向下滑动
				if (firstChildHeight > mScrollHeight + scrollDivid) {
					MZLog.d(tag, "---33333 mIsUnableToDrag = true");
					mIsUnableToDrag = true;
					return;
				}
			}
		}
		// 当y方向上移动的距离大于x方向上的距离 并且y方向移动的距离大于有效移动的距离
		if (yDiff > xDiff && yDiff > mTouchSlop) {
			mLastMotionY = y;
			mIsBeingDragged = true;
			MZLog.d(tag, "yDiff>mTouchSlop-------------1");
		} else if (yDiff > mTouchSlop) {
			mIsUnableToDrag = true;
			MZLog.d(tag, "yDiff>mTouchSlop-------------2");
		}

		MZLog.d(tag, "dy:" + dy + "  scrollDivid:" + scrollDivid + "  viewState:" + viewState + " mIsBeingDragged " + mIsBeingDragged + "  mIsUnableToDrag:"
				+ mIsUnableToDrag);
	}

	/**
	 * 
	 * @Title: judgeState
	 * @Description: 根据手指在y轴上的移动的距离切换至不同的状态
	 * @param @param dy
	 * @return void
	 * @throws
	 */
	private void judgeState(float dy) {
		MZLog.d(tag, "judgeState*****\ndy=" + dy + "\n getScrollY()=" + getScrollY() + "\nmHeadViewHeight=" + mHeadViewHeight + "\nmFootViewHeight="
				+ mFootViewHeight + "\ngetScrollY()=" + getScrollY() + "\n*****");
		if (dy >= 0) {// 向上滚动
			if (getScrollY() < 0) {// 下拉状态
				if (Math.abs(getScrollY()) < mHeadViewHeight) {
					viewState = STATE_PULL_DOWN_PREPARE;// 显示下拉刷新Header
				} else {
					viewState = STATE_PULL_DOWN_RELEASE_TO_REFRESH;// 下拉达到一定程度，可释放
				}
				// MZLog.d(tag, "dy >= 0getScrollY() < 0  viewState:" +
				// viewState);
				// headerPrepareToRefresh();
			} else if (getScrollY() > 0) {
				if (getScrollY() >= mFootViewHeight) {

					viewState = STATE_PULL_UP_RELEASE_TO_REFRESH;
				} else {
					viewState = STATE_PULL_UP_PREPARE;
				}
				footerPrepareRefresh();
			} else {
				if (mScrollView.getScrollY() > 0) {
					viewState = STATE_IDL;
				} else if (mScrollView.getScrollY() == 0) {
					viewState = STATE_IDL;
				} else {
					viewState = STATE_IDL;
				}
				// headerPrepareToRefresh();
				footerPrepareRefresh();
			}
		} else {// 向下滚动
			if (getScrollY() < 0) {
				if (Math.abs(getScrollY()) < mHeadViewHeight) {
					viewState = STATE_PULL_DOWN_PREPARE;
				} else {
					viewState = STATE_PULL_DOWN_RELEASE_TO_REFRESH;
				}
				// headerPrepareToRefresh();
			} else if (getScrollY() > 0) {// 下拉
				if (getScrollY() >= mFootViewHeight) {
					viewState = STATE_PULL_UP_RELEASE_TO_REFRESH;
				} else {
					viewState = STATE_PULL_UP_PREPARE;
				}
				MZLog.d(tag, "%%%%%%%%%%viewState=" + viewState);
				// 显示footerView
				footerPrepareRefresh();
			} else {
				if (mScrollView.getChildAt(0).getHeight() > (mScrollView.getHeight() + mScrollView.getScrollY())) {
					viewState = STATE_IDL;
				} else if (mScrollView.getScrollY() == 0) {
					viewState = STATE_IDL;
				} else {
					viewState = STATE_IDL;
				}
				// MZLog.d(tag, "dy < 0getScrollY() = 0viewState:" + viewState);
				// headerPrepareToRefresh();
				footerPrepareRefresh();
			}
		}

	}

	/**
	 * 
	 * @Title: endDrag
	 * @Description: 处理释放拖拽时逻辑
	 * @param
	 * @return void
	 * @throws
	 */
	private void endDrag() {

		if (mIsBeingDragged) {
			if (viewState == STATE_PULL_DOWN_PREPARE) {
				MZLog.d(tag, "--->filpup()");
				filpup();
			} else if (viewState == STATE_PULL_DOWN_RELEASE_TO_REFRESH) {
				MZLog.d(tag, "--->filpupToRefresh()");
				filpupToRefresh();
			} else if (viewState == STATE_PULL_UP_RELEASE_TO_REFRESH) {
				MZLog.d(tag, "--->loadMorePrepare()");
				int offsetY = (int) (upIDLpostion - getScrollY()) + mFootViewHeight;
				loadMorePrepare(offsetY);
			} else if (viewState == STATE_PULL_UP_PREPARE) {
				MZLog.d(tag, "--->filpdown()");
				filpdown();
			}
		}

		MZLog.d(tag, "endDrag-------------1---mIsBeingDragged:" + mIsBeingDragged + "  mIsUnableToDrag:" + mIsUnableToDrag + "  viewState:" + viewState);
		mActivePointerId = INVALID_POINTER;
		mIsBeingDragged = false;
		mIsUnableToDrag = false;
		mHeaderState = 0;
		mFooterState = 0;
		releaseVelocityTracker();

	}

	private final int RELEASE_TO_REFRESH = 200;
	private final int PULL_TO_REFRESH = 2001;
	private int mHeaderState = 0;
	private int mFooterState = 0;

	private void footerPrepareRefresh() {
		if(mFooterTextView==null)
			return ;
		if (viewState == STATE_PULL_UP_RELEASE_TO_REFRESH && mFooterState != RELEASE_TO_REFRESH) {
			mFooterTextView.setText("释放加载更多");
			mFooterState = RELEASE_TO_REFRESH;
		} else if (viewState == STATE_PULL_UP_PREPARE && mFooterState != PULL_TO_REFRESH) {
			mFooterTextView.setText("继续下拉加载更多");
			mFooterState = PULL_TO_REFRESH;
		}
	}

	public interface DownPartCallback {
		public void onLoadMore();

		public void showUpperPartCallBack();
	}

	private DownPartCallback mCallback;

	public void setDownPartCallback(DownPartCallback callback) {
		this.mCallback = callback;
	}

	private void filpdown() {
		scrollState = SCROLL_STATE_SMOOTH_SCROLLING;
		mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), (int) (upIDLpostion - getScrollY()), 300);
		invalidate();
	}

	private void filpup() {
		if (mCallback != null) {
			mCallback.showUpperPartCallBack();
		}
		scrollState = SCROLL_STATE_SMOOTH_SCROLLING;
		MZLog.d(TAG, "filpup--->\ngetScrollX()=" + getScrollX() + "\ngetScrollY()=" + getScrollY() + "\n(int) (upIDLpostion - getScrollY())="
				+ (int) (upIDLpostion - getScrollY()));
		mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), (int) (upIDLpostion - getScrollY()), 300);
		invalidate();
	}

	private void filpupToRefresh() {
		// Log.d(tag, "filpupToRefresh");
		// headRefreshing();
		// isRereshingData = true;
		// if (callBack != null) {
		// callBack.refresh();
		// }
		// scrollState = SCROLL_STATE_SMOOTH_SCROLLING;
		// mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), (int)
		// (upRefreshPostion - getScrollY()), 500);
		// invalidate();
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

	private int getPointerIndex(MotionEvent ev, int id) {
		int activePointerIndex = MotionEventCompat.findPointerIndex(ev, id);
		if (activePointerIndex == -1)
			mActivePointerId = INVALID_POINTER;
		return activePointerIndex;
	}

	/**
	 * 
	 * @Title: loadMorePrepare
	 * @Description: 加载更多
	 * @param
	 * @return void
	 * @throws
	 */
	private void loadMorePrepare(int offsetY) {
		if(mFooterTextView == null )
			return ;
		mFooterTextView.setText("加载中...");
		MZLog.d(tag, "----------->loadMorePrepare");
		scrollState = SCROLL_STATE_SMOOTH_SCROLLING;
		mScroller.startScroll(getScrollX(), getScrollY(), 0, offsetY, 200);
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (!mScroller.isFinished()) {
			if (mScroller.computeScrollOffset()) {
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
		}

		completeScroll();
		if (isTriggerLoadingMore) {
			isLoadingMore = true;
			mScrollView.setScrollable(false);
			if (mCallback != null) {
				mCallback.onLoadMore();
			}
		}
		processLoadingMore();
	}

	/**
	 * 加载结束，恢复状态
	 */
	public synchronized void loadCompelte() {
		if (isTriggerLoadingMore) {
			isLoadingMore = false;
			isTriggerLoadingMore = false;
			mScrollView.setScrollable(true);
		}
//		scrollToGetMoreSpace();
		// 隐藏footer
		mScroller.startScroll(getScrollX(), getScrollY(), 0, (int) (upIDLpostion - getScrollY()));
		invalidate();
		
	}

	private void processLoadingMore() {
		if (!isLoadingMore) {
			// scrollView滑动的距离
			int scrollDivid = mScrollView.getScrollY();
			// scrollView根控件（LinnearLayout）的总高度
			int firstChildHeight = mScrollView.getChildAt(0).getMeasuredHeight();
			if (firstChildHeight == scrollDivid + mScrollHeight) {
				MZLog.d(TAG, ">>>>>>>>>>>>>>>>滑动到底,mFootViewHeight=" + mFootViewHeight);
				// 触发加载更多
				isTriggerLoadingMore = true;
				int offset = (int) (upIDLpostion - getScrollY()) + mFootViewHeight;
				loadMorePrepare(offset);
			} else {
				isTriggerLoadingMore = false;
			}
		}
	}

	private void completeScroll() {
		MZLog.d(tag, "completeScroll  " + scrollState + "  viewState:" + viewState);
		if (scrollState == SCROLL_STATE_SMOOTH_SCROLLING) {
			scrollState = SCROLL_STATE_IDL;
			mIsBeingDragged = false;
			mIsUnableToDrag = false;

			if (viewState == STATE_PULL_DOWN_PREPARE) {
				// filpup();
			} else if (viewState == STATE_PULL_DOWN_RELEASE_TO_REFRESH) {
				// filpup();
				// TODO refresh
			} else if (viewState == STATE_PULL_UP_RELEASE_TO_REFRESH) {
				// TODO refresh

			} else if (viewState == STATE_PULL_UP_PREPARE) {
				// filpdown();
			}
			viewState = STATE_IDL;
			releaseVelocityTracker();
		}
	}

	private LayoutInflater mInflater;
	private boolean isLoadingMore = false;
	private boolean isTriggerLoadingMore = false;// 是否触发加载更多

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		/**
		 * l=0 t=0 r=1080 b=1809 mHeadViewHeight=106 mFootViewHeight=132
		 * mScrollHeight=1571
		 */
		// mHeaderView.layout(l, t - mHeadViewHeight, r, t);
//		Log.d(tag, "onLayout^^^^^^^^^^\nl=" + l + "\nt=" + t + "\nr=" + r + "\nb=" + b + "\nmFootViewHeight=" + mFootViewHeight + "\nmScrollHeight="
//				+ mScrollHeight + "\n^^^^^^^^^^");
		mScrollView.layout(l, t, r, t + mScrollHeight);

		mFooterView.layout(l, t + mScrollHeight, r, t + mScrollHeight + mFootViewHeight);

		upIDLpostion = t;

		upRefreshPostion = t - mHeadViewHeight;

		footIDLPostion = t + mScrollHeight;

		footRefreshPostion = t + mScrollHeight + mFootViewHeight / 2;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measureWidth = 0;
		int measureHeight = 0;

		mScrollView.measure(widthMeasureSpec, heightMeasureSpec);
		measureWidth = mScrollView.getMeasuredWidth();
		mScrollHeight = mScrollView.getMeasuredHeight();

		measureHeight = mScrollHeight;

		mFooterView.measure(widthMeasureSpec, getChildMeasureSpec(heightMeasureSpec, 0, mFooterView.getLayoutParams().height));
		mFootViewHeight = mFooterView.getMeasuredHeight();
		measureHeight += mFootViewHeight;

		setMeasuredDimension(measureWidth, measureHeight);
	}

	public void scrollToGetMoreSpace() {
		View childView = mScrollView.getChildAt(0);
		if (childView != null) {
			int childViewHieht = childView.getMeasuredHeight();
			int offset = childViewHieht - mScrollView.getHeight();
			if (offset < 0) {
				offset = 0;
			}
			mScrollView.scrollBy(0, offset);
			// mScrollView.smoothScrollBy(0, offset);

		}
		// mScrollView.fullScroll(View.FOCUS_DOWN);
	}

}