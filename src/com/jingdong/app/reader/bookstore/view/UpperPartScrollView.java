package com.jingdong.app.reader.bookstore.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.util.MZLog;

/**
 *
 * @ClassName: UpperPartScrollView
 * @Description: 上部分ScrollView：
 * 				 1）支持下拉刷新；
 *               2）滑动到底部时，显示下拉提示，下拉到一定程度切换到下一个ScrollView
 * @author J.Beyond
 * @date 2015年7月7日 下午1:30:34
 */
public class UpperPartScrollView extends LinearLayout {
	private Scroller mScroller;
	private Context mContext;
	private View mHeaderView;
	private InnerScrollView mScrollView;
//	private View mFooterView;
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
	private ImageView mHeaderImageView;
	private TextView mHeaderTextView;
	private TextView mHeaderUpdateTextView;
	private ProgressBar mHeaderProgressBar;
	private ImageView mFooterImageView;
	private TextView mFooterTextView;
	// private ProgressBar mFooterProgressBar;
	private RotateAnimation mFlipAnimation;
	private RotateAnimation mReverseFlipAnimation;
	private List<View> mIgnoredViews = new ArrayList<View>();
	private SharedPreferences preferences;

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
	private boolean mIsUnableToDrag = false;

	public UpperPartScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLayout(context);
	}

	public UpperPartScrollView(Context context) {
		this(context, null);
	}
	
	
	
	public InnerScrollView getScrollView() {
		return mScrollView;
	}

	public void setFooterView(View view){
		mFootViewHeight = view.getMeasuredHeight();
		MZLog.d(TAG, "setFooterView--->mFootViewHeight="+mFootViewHeight);
		mFooterImageView = (ImageView) view.findViewById(R.id.bookstore_index_recomment_header_iv);
		mFooterTextView = (TextView) view.findViewById(R.id.bookstore_index_recomment_header_tv);
		mFooterTextView.setText(mContext.getString(R.string.pull_down_to_next_page));
	}

	/**
	 * 
	 * @Title: initLayout
	 * @Description: 初始化布局、手势参数
	 * @param @param context
	 * @return void
	 * @throws
	 */
	private void initLayout(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		preferences = mContext.getSharedPreferences(CLIENT_PREFERENCES, Context.MODE_PRIVATE);
		mScroller = new Scroller(context);
		
		//Header布局
		LayoutParams headlp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mHeaderView = mInflater.inflate(R.layout.pull_to_refresh_header_index, null);
		mHeaderImageView = (ImageView) mHeaderView.findViewById(R.id.pull_to_refresh_image);
		mHeaderTextView = (TextView) mHeaderView.findViewById(R.id.pull_to_refresh_text);
		mHeaderUpdateTextView = (TextView) mHeaderView.findViewById(R.id.pull_to_refresh_sub_text);
		mHeaderUpdateTextView.setVisibility(View.GONE);
		mHeaderProgressBar = (ProgressBar) mHeaderView.findViewById(R.id.pull_to_refresh_progress);
//		lastUpdateTime = preferences.getLong(KEY_LAST_UPDATA_TIMESTAMP, -1);
//		refreshUpdatedAtValue();
		addView(mHeaderView, headlp);
		
		//中间内容布局
		LayoutParams scrolllp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mScrollView = new InnerScrollView(mContext);
		mScrollView.setVerticalScrollBarEnabled(false);
		addView(mScrollView, scrolllp);

		//手势参数
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		
		//初始化动画
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
		mScrollView.addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	/**
	 * 
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (viewConficked(event)) {
			MZLog.d(tag, "onInterceptTouchEvent---viewConficked");
			return false;
		}
		if (scrollState == SCROLL_STATE_SMOOTH_SCROLLING) {
			MZLog.d(tag, "onInterceptTouchEvent---scrollState == SCROLL_STATE_SMOOTH_SCROLLING:" + (scrollState == SCROLL_STATE_SMOOTH_SCROLLING) + "---------"
					+ isRereshingData);
			return true;
		}

		final int action = event.getAction() & MotionEventCompat.ACTION_MASK;

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
			Log.v(tag, "onInterceptTouchEvent--MotionEvent.ACTION_MOVE:");
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
		Log.v(tag, "upper--->onInterceptTouchEvent--mIsBeingDragged:" + mIsBeingDragged);

		return mIsBeingDragged;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		MZLog.d("J", "---onTouchEvent----");
		if (viewConficked(event)) {
			Log.v(tag, "onTouchEvent--viewConficked ");
			return false;
		}

		if (scrollState == SCROLL_STATE_SMOOTH_SCROLLING) {
			Log.v(tag, "onTouchEvent--scrollState == SCROLL_STATE_SMOOTH_SCROLLING:" + (scrollState == SCROLL_STATE_SMOOTH_SCROLLING) + "  mIsBeingDragged:"
					+ mIsBeingDragged + "  viewState:" + viewState + " isRereshingData:" + isRereshingData);
			return true;
		}

		// if (!mIsBeingDragged) {
		// return false;
		// }

		if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
			MZLog.d("J", "onTouchEvent---- MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0");
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
			// Log.v(TAG, "ACTION_DOWN#currentScrollY:" + getScrollY() +
			// ", mLastMotionY:" + mLastMotionY);
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
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
//					MZLog.d("J", "@@@@@@@@@@@@@@\n y=" + y + "\n deltaY=" + deltaY + "\n oldScrollY=" + oldScrollY + "\n scrollY=" + scrollY + "\n upIDLpostion="
//							+ upIDLpostion + "\n footIDLPostion=" + footIDLPostion + "\n@@@@@@@@@@@@@@");
					if (scrollY < upIDLpostion) {
						scrollY = upIDLpostion;
					} else if (scrollY > footIDLPostion) {
						scrollY = footIDLPostion;
					}
					// Don't lose the rounded component
					mLastMotionY += scrollY - (int) scrollY;
					judgeState(deltaY);
					float scrollDp = deltaY * TOUCHPARAM;
					MZLog.d("J", "############\nmLastMotionY=" + mLastMotionY + "\nscrollDp=" + scrollDp + "\n############");
					scrollBy(0, (int) -scrollDp);
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			Log.v(tag, "onTouchEvent--MotionEvent.ACTION_UP:+mIsBeingDragged:" + mIsBeingDragged + "  viewState:" + viewState);
			// final VelocityTracker velocityTracker = mVelocityTracker;
			// velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
			// int initialVelocity = (int) velocityTracker.getYVelocity();
			endDrag();
			break;
		case MotionEvent.ACTION_CANCEL:
			Log.v(tag, "onTouchEvent--MotionEvent.ACTION_CANCEL:+mIsBeingDragged:" + mIsBeingDragged + "  viewState:" + viewState);
			// if (mIsBeingDragged) {
			endDrag();
			// }
			break;
		default:
			Log.v(tag, "onTouchEvent--default:" + event.getAction() + "   mIsBeingDragged:" + mIsBeingDragged + "  viewState:" + viewState);
			// if (mIsBeingDragged) {
			endDrag();
			// }
			break;
		}

		return true;
	}

	/**
	 * 
	 * @Title: determing
	 * @Description: 处理是否对touch事件做拦截
	 * @param @param event
	 * @return void
	 * @throws
	 */
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
			MZLog.d("J", "up#determing-------\nx=" + x + "\ndx=" + dx + "\nxDiff=" + xDiff + "\ny=" + y 
					+ "\ndy=" + dy + "\nyDiff=" + yDiff + "\nmTouchSlop=" + mTouchSlop+"\n--------");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		// scrollView滑动的距离
		int scrollDivid = mScrollView.getScrollY();
		// scrollView根控件（LinnearLayout）的总高度
		int firstChildHeight = mScrollView.getChildAt(0).getMeasuredHeight() ;
		MZLog.d("J", "up-------\n firstChildHeight=" + firstChildHeight + "\nmScrollHeight="+mScrollHeight+"\nscrollDivid="+
				scrollDivid+"\nmScrollHeight + scrollDivid=" + (mScrollHeight + scrollDivid));
		if (scrollDivid == 0) {// 滑动到最顶部
			if (dy < 0) {// 向下移动
				if (firstChildHeight > mScrollHeight + scrollDivid && getScrollY() <= 0) {
					MZLog.d(tag, "---11111 mIsUnableToDrag = true");
					mIsUnableToDrag = true;
					return;
				}
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
		MZLog.d("J", "judgeState*****\ndy=" + dy + "\n getScrollY()=" + getScrollY() + "\nmHeadViewHeight=" + mHeadViewHeight + "\nmFootViewHeight="
				+ mFootViewHeight + "\n*****");
		if (dy >= 0) {// 向上滚动
			if (getScrollY() < 0) {// 下拉状态
				if (Math.abs(getScrollY()) < mHeadViewHeight) {
					viewState = STATE_PULL_DOWN_PREPARE;// 显示下拉刷新Header
				} else {
					viewState = STATE_PULL_DOWN_RELEASE_TO_REFRESH;// 下拉达到一定程度，可释放
				}
				// MZLog.d(tag, "dy >= 0getScrollY() < 0  viewState:" +
				// viewState);
				headerPrepareToRefresh();
			} else if (getScrollY() > 0) {
				if (getScrollY() >= 121) {
					viewState = STATE_PULL_UP_RELEASE_TO_REFRESH;
				} else {
					viewState = STATE_PULL_UP_PREPARE;
				}
				footerPrepareRefresh();
				// MZLog.d(tag, "dy >= 0getScrollY() > 0  viewState:" +
				// viewState);
			} else {//getScrollY()=0 滑动到顶部
				if (mScrollView.getScrollY() > 0) {
					viewState = STATE_IDL;
				} else if (mScrollView.getScrollY() == 0) {
					viewState = STATE_IDL;
				} else {
					viewState = STATE_IDL;
				}
				// MZLog.d(tag, "dy >= 0getScrollY() = 0  viewState:" + viewState
				// + "  mScrollView.getScrollY()" + mScrollView.getScrollY());
				headerPrepareToRefresh();
//				footerPrepareRefresh();
			}
		} else {// 向下滚动
			if (getScrollY() < 0) {
				if (Math.abs(getScrollY()) < mHeadViewHeight) {
					viewState = STATE_PULL_DOWN_PREPARE;
				} else {
					viewState = STATE_PULL_DOWN_RELEASE_TO_REFRESH;
				}
				headerPrepareToRefresh();
			} else if (getScrollY() > 0) {// 下拉
				// if (getScrollY() >= mFootViewHeight) {
				if (getScrollY() >= 121) {
					viewState = STATE_PULL_UP_RELEASE_TO_REFRESH;
				} else {
					viewState = STATE_PULL_UP_PREPARE;
				}
				MZLog.d("J", "%%%%%%%%%%viewState=" + viewState);
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
				headerPrepareToRefresh();
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
		MZLog.d(tag, "endDrag-------------1---mIsBeingDragged:" + mIsBeingDragged +
				"  mIsUnableToDrag:" + mIsUnableToDrag + "  viewState:" + viewState);

		if (mIsBeingDragged) {
			if (viewState == STATE_PULL_DOWN_PREPARE) {
				MZLog.d(tag, "--->filpup()");
				filpup();
			} else if (viewState == STATE_PULL_DOWN_RELEASE_TO_REFRESH) {
				MZLog.d(tag, "--->filpupToRefresh()");
				filpupToRefresh();
			} else if (viewState == STATE_PULL_UP_RELEASE_TO_REFRESH) {
				MZLog.d(tag, "--->filpDownPage()");
				filpDownPage();
			} else if (viewState == STATE_PULL_UP_PREPARE) {
				MZLog.d(tag, "--->filpdown()");
				filpdown();
			}
		}

		mActivePointerId = INVALID_POINTER;
		mIsBeingDragged = false;
		mIsUnableToDrag = false;
		mHeaderState = 0;
		mFooterState = 0;
		releaseVelocityTracker();

	}

	public void addIgnoredView(View view) {
		mIgnoredViews.add(view);
	}

//	public void addScrollViewIgnoredView(View view) {
//		mScrollView.addIgnoredView(view);
//	}

	private boolean viewConficked(MotionEvent ev) {
		Rect rect = new Rect();
		for (View v : mIgnoredViews) {
			v.getHitRect(rect);
			if (rect.contains((int) ev.getX(), (int) ev.getY()))
				return true;
		}
		return false;
	}

	private final int RELEASE_TO_REFRESH = 200;
	private final int PULL_TO_REFRESH = 2001;
	private int mHeaderState = 0;
	private int mFooterState = 0;

	/**
	 * 
	 * @Title: headerPrepareToRefresh
	 * @Description: 处理HeaderView的状态
	 * @param
	 * @return void
	 * @throws
	 */
	private void headerPrepareToRefresh() {
		Log.d(tag, "headerPrepareToRefresh,viewState="+viewState+",mHeaderState="+mHeaderState+",isRereshingData="+isRereshingData);
		// 下拉释放状态并且不处于下拉状态
		// STATE_PULL_DOWN_RELEASE_TO_REFRESH = 1001
		// RELEASE_TO_REFRESH=200
		if (viewState == STATE_PULL_DOWN_RELEASE_TO_REFRESH && mHeaderState != RELEASE_TO_REFRESH) {
			Log.d(tag, "headerPrepareToRefresh  STATE_PULL_DOWN_RELEASE_TO_REFRESH");
			if (isRereshingData) {
				mHeaderTextView.setText(R.string.pull_to_refresh_pull_label);
			} else {
				mHeaderTextView.setText(R.string.pull_to_refresh_release_label);
			}
			mHeaderProgressBar.setVisibility(View.GONE);
			mHeaderImageView.setVisibility(View.VISIBLE);
			mHeaderImageView.clearAnimation();
			mHeaderImageView.startAnimation(mFlipAnimation);
			mHeaderState = RELEASE_TO_REFRESH;
//			refreshUpdatedAtValue();
		} else if (viewState == STATE_PULL_DOWN_PREPARE && mHeaderState != PULL_TO_REFRESH) {
			Log.d(tag, "headerPrepareToRefresh  STATE_PULL_DOWN_PREPARE");
			mHeaderImageView.setVisibility(View.VISIBLE);
			mHeaderProgressBar.setVisibility(View.GONE);
			mHeaderImageView.clearAnimation();
			mHeaderImageView.startAnimation(mReverseFlipAnimation);
			if (isRereshingData) {
				mHeaderTextView.setText(R.string.pull_to_refresh_pull_label);
			} else {
				mHeaderTextView.setText(R.string.pull_to_refresh_pull_label);
			}
			// refreshUpdatedAtValue();
			mHeaderState = PULL_TO_REFRESH;
		}
	}

	

	private void headRefreshing() {
		if (viewState == STATE_PULL_DOWN_RELEASE_TO_REFRESH) {
			Log.d(tag, "headRefreshing");
			mHeaderTextView.setText(R.string.pull_to_refresh_pull_label);
			mHeaderImageView.clearAnimation();
			mHeaderImageView.setVisibility(View.GONE);
			mHeaderProgressBar.setVisibility(View.VISIBLE);
		}
	}

	private void footerPrepareRefresh() {
		if (viewState == STATE_PULL_UP_RELEASE_TO_REFRESH && mFooterState != RELEASE_TO_REFRESH) {
			if(null != mFooterTextView) {
				mFooterTextView.setText("释放到专属推荐页");
			}
//			mFooterImageView.clearAnimation();
//			mFooterImageView.startAnimation(mReverseFlipAnimation);
			mFooterState = RELEASE_TO_REFRESH;
		} else if (viewState == STATE_PULL_UP_PREPARE && mFooterState != PULL_TO_REFRESH) {
//			mFooterImageView.clearAnimation();
//			mFooterImageView.startAnimation(mFlipAnimation);
			if(mFooterTextView!=null){
				mFooterTextView.setText("上拉到专属推荐页");
			}
			mFooterState = PULL_TO_REFRESH;
		}
	}

	public interface ScrollCallBack {
		public void showDownPartCallBack();

		public void refresh();
	}

	public ScrollCallBack callBack;

	public void setCallBack(ScrollCallBack callBack) {
		this.callBack = callBack;
	}

	private void filpdown() {
		scrollState = SCROLL_STATE_SMOOTH_SCROLLING;
		mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), (int) (upIDLpostion - getScrollY()), 300);
		invalidate();
	}

	/**
	 * 
	 * @Title: filpDownPage
	 * @Description: 切换到下一页
	 * @param
	 * @return void
	 * @throws
	 */
	private void filpDownPage() {
		if (null != mFooterTextView) {
			mFooterTextView.setText("上拉到专属推荐页");
		}
		if (callBack != null) {
			callBack.showDownPartCallBack();
		}
		scrollState = SCROLL_STATE_SMOOTH_SCROLLING;
//		MZLog.d(TAG, "&&&&&&&filpDownPage-->getScrollX()="+getScrollX()+"\ngetScrollY()="
//				+getScrollY()+"\nupIDLpostion="+upIDLpostion+"\n(upIDLpostion - getScrollY()="+(int) (upIDLpostion - getScrollY()));
		if (null != mScroller) {
			mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), (int) (upIDLpostion - getScrollY()));
		}
		invalidate();
	}

	private void filpup() {
		scrollState = SCROLL_STATE_SMOOTH_SCROLLING;
		mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), (int) (upIDLpostion - getScrollY()), 300);
		invalidate();
	}

	private void filpupToRefresh() {
		Log.d(tag, "filpupToRefresh");
		headRefreshing();
		isRereshingData = true;
//		if (callBack != null) {
//			callBack.refresh();
//		}
		mScrollView.setScrollable(false);
		scrollState = SCROLL_STATE_SMOOTH_SCROLLING;
		int offset = (int) (upRefreshPostion - getScrollY());
		MZLog.d(TAG, "LLLLLLLLLLLLLLL offset="+offset+",mHeadViewHeight="+mHeadViewHeight);
		mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), offset, 500);
		invalidate();
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
	
	public void finishRefresh() {
		MZLog.d(TAG, "##########finishRefresh##########\nisRereshingData="+isRereshingData);
		if (isRereshingData) {
			lastUpdateTime = System.currentTimeMillis();
			preferences.edit().putLong(KEY_LAST_UPDATA_TIMESTAMP, lastUpdateTime).commit();
			isRereshingData = false;
			mScrollView.setScrollable(true);
			int offset = (int) (upIDLpostion - getScrollY())-mHeadViewHeight;
			MZLog.d(TAG, "getScrollY()="+getScrollY()+"\noffset="+offset);
			mScroller.startScroll(getScrollX(), getScrollY(), 0, mHeadViewHeight, 500);
			invalidate();
		}

	}

	@Override
	public void computeScroll() {
		if (!mScroller.isFinished()) {//当上下两部分视图切换的时候进入
			Log.d(tag, "upper---mScroller.isFinished()="+mScroller.isFinished());
			if (mScroller.computeScrollOffset()) {
				Log.d(tag, "upper---mScroller.computeScrollOffset()="+mScroller.computeScrollOffset());
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
		if (isRereshingData && callBack != null) {
			callBack.refresh();
		}
	}

	private void completeScroll() {
		Log.v(tag, "completeScroll  " + scrollState + "  viewState:" + viewState);
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

	/** 一分钟的毫秒值，用于判断上次的更新时间 */
	public static final long ONE_MINUTE = 60 * 1000;

	/** 一小时的毫秒值，用于判断上次的更新时间 */
	public static final long ONE_HOUR = 60 * ONE_MINUTE;

	/** 一天的毫秒值，用于判断上次的更新时间 */
	public static final long ONE_DAY = 24 * ONE_HOUR;

	/*** 一月的毫秒值，用于判断上次的更新时间 */
	public static final long ONE_MONTH = 30 * ONE_DAY;

	/*** 一年的毫秒值，用于判断上次的更新时间 */
	public static final long ONE_YEAR = 12 * ONE_MONTH;
	private long lastUpdateTime;
	private String lastUpdateTopString = "";
	private LayoutInflater mInflater;

	private void refreshUpdatedAtValue() {
		long currentTime = System.currentTimeMillis();
		long timePassed = currentTime - lastUpdateTime;
		long timeIntoFormat;
		String updateAtValue;
		if (lastUpdateTime == -1) {
			updateAtValue = "您还没有刷新过哦";
		} else if (timePassed < 0) {
			updateAtValue = "您还没有刷新过哦";
		} else if (timePassed < ONE_MINUTE) {
			updateAtValue = "上次刷新：刚刚";
		} else if (timePassed < ONE_HOUR) {
			timeIntoFormat = timePassed / ONE_MINUTE;
			String value = timeIntoFormat + "分钟";
			updateAtValue = "上次刷新：" + value + "前";
		} else if (timePassed < ONE_DAY) {
			timeIntoFormat = timePassed / ONE_HOUR;
			long remainder = timePassed % ONE_HOUR;
			long tempTime = remainder / ONE_MINUTE;
			String value = timeIntoFormat + "小时" + tempTime + "分钟";
			updateAtValue = "上次刷新：" + value + "前";
		} else if (timePassed < ONE_MONTH) {
			timeIntoFormat = timePassed / ONE_DAY;
			long remainder = timePassed % ONE_DAY;
			long tempTime = remainder / ONE_HOUR;
			String value = timeIntoFormat + "天" + tempTime + "小时";
			updateAtValue = "上次刷新：" + value + "前";
		} else if (timePassed < ONE_YEAR) {
			timeIntoFormat = timePassed / ONE_MONTH;
			long remainder = timePassed % ONE_MONTH;
			long tempTime = remainder / ONE_DAY;
			String value = timeIntoFormat + "个月" + tempTime + "天";
			updateAtValue = "上次刷新：" + value + "前";
		} else {
			timeIntoFormat = timePassed / ONE_YEAR;
			long remainder = timePassed % ONE_YEAR;
			long tempTime = remainder / ONE_MONTH;
			String value = timeIntoFormat + "年" + tempTime + "月";
			updateAtValue = "上次刷新：" + value + "前";
		}
		if (!lastUpdateTopString.equals(updateAtValue)) {
			mHeaderUpdateTextView.setText(updateAtValue);
		} else {
			lastUpdateTopString = updateAtValue;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		MZLog.d("J", "onLayout=======\nl=" + l + "\nt=" + t + "\nr=" + r + "\nb=" + b + "\nmScrollHeight=" + mScrollHeight);
		/**
		 * l=0 t=0 r=1080 b=1809 mHeadViewHeight=106 mFootViewHeight=132
		 * mScrollHeight=1571
		 */
		mHeaderView.layout(l, t - mHeadViewHeight, r, t);

		mScrollView.layout(l, t, r, t + mScrollHeight);

//		mFooterView.layout(l, t + mScrollHeight, r, t + mScrollHeight + mFootViewHeight);

		upIDLpostion = t;

		upRefreshPostion = t - mHeadViewHeight;

		footIDLPostion = t + mScrollHeight;

		footRefreshPostion = t + mScrollHeight + mFootViewHeight / 2;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measureWidth = 0;
		int measureHeight = 0;

		mHeaderView.measure(widthMeasureSpec, getChildMeasureSpec(heightMeasureSpec, 0, mHeaderView.getLayoutParams().height));
		mHeadViewHeight = mHeaderView.getMeasuredHeight();
		measureHeight = mHeadViewHeight;
		measureWidth = mHeaderView.getMeasuredWidth();

		mScrollView.measure(widthMeasureSpec, heightMeasureSpec);

		mScrollHeight = mScrollView.getMeasuredHeight();
		// MZLog.d("J", "-----mScrollHeight="+mScrollHeight);
		measureHeight += mScrollHeight;

//		mFooterView.measure(widthMeasureSpec, getChildMeasureSpec(heightMeasureSpec, 0, mFooterView.getLayoutParams().height));
//		mFootViewHeight = mFooterView.getMeasuredHeight();
//		measureHeight += mFootViewHeight;
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