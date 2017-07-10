package com.bob.android.lib.slide;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.jingdong.app.lib.slide.R;

public class Workspace extends ViewGroup implements SlideRemoteControler,
		GestureDetector.OnGestureListener {
	private final Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	private int mScrollX = 0;
	private int mCurrentScreen = 0;

	private float mLastMotionX;
	private float mLastMotionY;
	private float mLastDistance;

	private static final String LOG_TAG = "DragableSpace";

	private static final int SNAP_VELOCITY = 600;

	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;

	private int mTouchState = TOUCH_STATE_REST;

	private int mTouchSlop = 20;
	private int mCount = 2;
	private int mBufferCount = 2;
	private int mPageWidth;
	private boolean mAnimationing = false;
	int spaceWidth = 20;
	private int mContentOffSet = 0;
	private final GestureDetector mGestureDetector;

	// int countNum = 10;
	// private Vector<Item> mItemlist = new Vector<Item>(0);
	private final Set<OnScrollListener> mListeners = new HashSet<OnScrollListener>();
	// private int lastpage =-1;
	Item[] channelButtonArray = new Item[3];// 缓冲数量
	private SlideRemoteControler remoteControler;
	private OnItemListener onItemListener = new OnItemListener() {
		@Override
		public void OnItemSelected(AdapterView<?> parent, Item item,
				int position, long id) {
		}

		@Override
		public void OnItemLoading(AdapterView<?> parent, Item item,
				int position, long id) {
		}

	};

	public void setRemoteControl(SlideRemoteControler remoteControl) {
		this.remoteControler = remoteControl;
	}

	public void setOnItemListener(OnItemListener itemListener) {
		onItemListener = itemListener;
	}

	public int getPageWidth() {
		return mPageWidth;
	}

	public int getCount() {
		return mCount;
	}

	public void setCount(int count) {
		mCount = count;
	}

	public static interface OnScrollListener {

		void onScroll(int scrollX);

		void onViewScrollFinished(int currentPage);
	}

	public void setContentOffSet(int mContentOffSet) {
		this.mContentOffSet = mContentOffSet;
	}

	public boolean isAnimationing() {
		return mAnimationing;
	}

	public void setAnimationing(boolean mAnimationing) {
		this.mAnimationing = mAnimationing;
	}

	public void setPageWidth(int mPageWidth) {
		this.mPageWidth = mPageWidth;
	}

	public void addOnScrollListener(OnScrollListener listener) {
		mListeners.add(listener);
	}

	public void removeOnScrollListener(OnScrollListener listener) {
		mListeners.remove(listener);
	}

	private static class WorkspaceOvershootInterpolator implements Interpolator {
		private static final float DEFAULT_TENSION = 1.3f;
		private float mTension;

		public WorkspaceOvershootInterpolator() {
			mTension = DEFAULT_TENSION;
		}

		@SuppressWarnings("unused")
		public void setDistance(int distance) {
			mTension = distance > 0 ? DEFAULT_TENSION / distance
					: DEFAULT_TENSION;
		}

		@SuppressWarnings("unused")
		public void disableSettle() {
			mTension = 0.f;
		}

		@Override
		public float getInterpolation(float t) {
			// _o(t) = t * t * ((tension + 1) * t + tension)
			// o(t) = _o(t - 1) + 1
			t -= 1.0f;
			return t * t * ((mTension + 1) * t + mTension) + 1.0f;
		}
	}

	// public void relayoutItem(int index){
	// Item item = mItemlist.get(index);
	// // Item item =(Item)view.getTag();
	// if(index==0||index==mItemlist.size()-1){
	// return;
	// }
	//
	// int nextnum = channelButtonArray.length/2;
	// Item nextItem = item;
	// boolean isNextNull = false;
	// int nextNum = 0;
	// while(!isNextNull){
	// if(nextItem.nextItem!=null){
	// nextItem = nextItem.nextItem;
	// }else if(nextItem.nextItem==null){
	// isNextNull = true;
	// break;
	// }
	// nextNum++;
	// }
	//
	// Item upItem = item;
	// int lastNum = 0;
	// boolean isLastNull = false;
	// while(!isLastNull){
	// if(upItem.upItem!=null){
	// upItem = upItem.upItem;
	// }else if(upItem.upItem==null){
	// isLastNull = true;
	// break;
	// }
	// lastNum++;
	// }
	//
	// if(lastNum-nextNum>1){
	// if(index+nextNum==mItemlist.size()-1){
	// return;
	// }
	// upItem.nextItem.upItem = null;//将头链掐�?
	// nextItem.nextItem = upItem;//将尾部接上头�?
	// upItem.upItem = nextItem;//将头结链到最后一个尾�?
	// upItem.nextItem = null;//将头尾部接头指控
	// RelativeLayout.LayoutParams lp =
	// ((RelativeLayout.LayoutParams)upItem.currentView.getLayoutParams());
	// lp.leftMargin=pageWidth*(index+nextNum+1);
	// Log.i("", "index+nextNum=="+(index+nextNum+1));
	// Log.i("", "pageWidth=="+pageWidth);
	// Log.i("", "lp.leftMargin=="+lp.leftMargin);
	// Log.i("", "upItem.currentView.getTag()"+upItem.currentView.getTag());
	// upItem.currentView.setLayoutParams(lp);
	// // RelativeLayout home=(RelativeLayout)getChildAt(0);
	// // home.removeView(upItem.currentView);
	// // home.addView(upItem.currentView,lp);
	// ////setItemLoad(upItem,index+nextNum+1);
	//
	// // final LayoutInflater factory = LayoutInflater.from(this.getContext());
	// // RelativeLayout cell_layout = (RelativeLayout) factory.inflate(
	// // R.layout.cell_layout, null);
	// //// b.setImageResource(R.drawable.icon);
	// // item = new Item();
	// // item.currentView = cell_layout;
	// // RelativeLayout.LayoutParams rl = new
	// RelativeLayout.LayoutParams(pageWidth,RelativeLayout.LayoutParams.FILL_PARENT);
	// // RelativeLayout home=(RelativeLayout)getChildAt(0);
	// // rl.leftMargin = 0;
	// // home.addView(cell_layout, rl);
	// // home.postInvalidate();
	//
	// }
	//
	// if(nextNum-lastNum>1){
	// if(index-lastNum==0){
	// return;
	// }
	// nextItem.upItem.nextItem = null;//将尾拿下�?
	// upItem.upItem = nextItem;//接在头部
	// nextItem.upItem = null;//接好后把尾前指针制空
	// nextItem.nextItem = upItem;//将头尾部接头指控
	// RelativeLayout.LayoutParams lp =
	// ((RelativeLayout.LayoutParams)nextItem.currentView.getLayoutParams());
	// lp.leftMargin= pageWidth*(index-lastNum-1);
	// nextItem.currentView.setLayoutParams(lp);
	// }
	// }

	int lastRelayoutItemIndex = -1;

	// int cacheNum =3;
	public void relayoutItem(int index) {

		if (index < 0) {
			index = 0;
		} else if (index >= mCount) {
			index = mCount - 1;
		}
		int itemIndex = index % mBufferCount;
		Item item = channelButtonArray[itemIndex];

		if (lastRelayoutItemIndex == index) {
			return;
		}
		lastRelayoutItemIndex = index;
		onItemListener.OnItemSelected(null, item, index, item.id);

		// if(index==0||index==mCount-1){
		// return;
		// }

		// if(mCount<mBufferCount){
		// return;
		// }

		Item nextItem = item;
		boolean isNextNull = false;
		int nextNum = 0;
		while (!isNextNull) {
			if (nextItem.nextItem != null) {
				nextItem = nextItem.nextItem;
			} else if (nextItem.nextItem == null) {
				isNextNull = true;
				break;
			}
			nextNum++;
		}

		Item upItem = item;
		int lastNum = 0;
		boolean isLastNull = false;
		while (!isLastNull) {
			if (upItem.upItem != null) {
				upItem = upItem.upItem;
			} else if (upItem.upItem == null) {
				isLastNull = true;
				break;
			}
			lastNum++;
		}

		if (lastNum - nextNum > 1) {
			if (index + nextNum == mCount - 1) {
				return;
			}
			upItem.nextItem.upItem = null;// 将头链掐�?
			nextItem.nextItem = upItem;// 将尾部接上头�?
			upItem.upItem = nextItem;// 将头结链到最后一个尾�?
			upItem.nextItem = null;// 将头尾部接头指控
			RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) upItem.currentView
					.getLayoutParams();
			int wichIndex = index + nextNum + 1;
			rl.leftMargin = getleftMargin(wichIndex);// mPageWidth*(wichIndex);
			// Log.i("zhoubo", "mPageWidth==" + mPageWidth);
			upItem.currentView.setLayoutParams(rl);
			int wichItemIndex = wichIndex % mBufferCount;
			Item wichItem = channelButtonArray[wichItemIndex];
			// setItemLoad(upItem,index+nextNum+1);
			onItemListener
					.OnItemLoading(null, wichItem, wichIndex, wichItem.id);
		}

		if (nextNum - lastNum > 1) {
			if (index - lastNum == 0) {
				return;
			}
			nextItem.upItem.nextItem = null;// 将尾拿下�?
			upItem.upItem = nextItem;// 接在头部
			nextItem.upItem = null;// 接好后把尾前指针制空
			nextItem.nextItem = upItem;// 将头尾部接头指控
			// item.upItem.upItem = null;
			// item.nextItem = tempItem;
			// tempItem.nextItem = null;
			// tempItem.upItem = item;
			RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) nextItem.currentView
					.getLayoutParams();
			int wichIndex = index - lastNum - 1;
			rl.leftMargin = getleftMargin(wichIndex);// mPageWidth*(wichIndex);
			nextItem.currentView.setLayoutParams(rl);
			int wichItemIndex = wichIndex % mBufferCount;
			Item wichItem = channelButtonArray[wichItemIndex];
			onItemListener
					.OnItemLoading(null, wichItem, wichIndex, wichItem.id);
			// setItemLoad(nextItem,index-lastNum-1);

		}
		// Log.i("zhoubo", "rrrrrrrrrr" + 123);
	}

	public void itemLoaded(int index) {
		if (index < 0 || index >= mCount) {
			return;
		}
		Item wichItem = this.getItem(index);
		onItemListener.OnItemLoading(null, wichItem, index, wichItem.id);
	}

	private void initCache() {
		// this.pageWidth = pageWidth;
		RelativeLayout home = (RelativeLayout) getChildAt(0);
		channelButtonArray = new Item[mBufferCount];
		final LayoutInflater factory = LayoutInflater.from(this.getContext());
		for (int i = 0; i < channelButtonArray.length; i++) {
			RelativeLayout cell_layout = (RelativeLayout) factory.inflate(
					R.layout.cell_layout, null);
			// b.setImageResource(R.drawable.icon);
			Item item = new Item(this.getContext());
			item.currentView = cell_layout;
			item.id = i;
			channelButtonArray[i] = item;
			// RelativeLayout.LayoutParams rl = new
			// RelativeLayout.LayoutParams(pageWidth,RelativeLayout.LayoutParams.FILL_PARENT);
			// rl.leftMargin = pageWidth*i;
			home.addView(cell_layout);
			item.currentView.setTag("" + i);
		}

		for (int i = 0; i < channelButtonArray.length; i++) {
			Item item = channelButtonArray[i];
			if (channelButtonArray.length == 0) {
				item.upItem = null;
				item.upItem = null;
			} else {
				if (i == 0) {
					item.upItem = null;
					item.nextItem = channelButtonArray[1];
				} else if (i == channelButtonArray.length - 1) {
					item.upItem = channelButtonArray[channelButtonArray.length - 2];
					item.nextItem = null;
				} else {
					item.upItem = channelButtonArray[i - 1];
					item.nextItem = channelButtonArray[i + 1];
				}
			}
		}

		// for(int i = 0; i < mCount; i++){
		// for(int j=0;j<channelButtonArray.length;j++){
		// if((i%channelButtonArray.length)==j){
		// mItemlist.add(channelButtonArray[j]);
		// }
		// }
		// }
	}

	// public void initData(){
	// int num = mBufferCount<mCount ? mBufferCount:mCount;
	// for (int i = 0; i <num; i++) {
	// //channelButtonArray[i].load(paths[i]);
	// onItemListener.OnItemLoading(null, channelButtonArray[i], i,
	// channelButtonArray[i].id);
	// }
	// //onItemListener.OnItemSelected(null, channelButtonArray[0], 0,
	// channelButtonArray[0].id);
	// }

	public void initAllItemView(View[] views) {
		int i = 0;
		for (View v : views) {
			((RelativeLayout) channelButtonArray[i].currentView).addView(v);
			i++;
		}
	}

	public Item getItem(int index) {
		int wichItemIndex = index % mBufferCount;
		// Log.i("zhoubo", "wichItemIndex===" + wichItemIndex);
		Item item = channelButtonArray[wichItemIndex];
		return item;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// mPageWidth = getMeasuredWidth();
		// RelativeLayout home=(RelativeLayout)getChildAt(0);
		// Button b = new Button(this.getContext());
		// Log.i("", "pageWidth=="+pageWidth);
		// RelativeLayout.LayoutParams rl = new
		// RelativeLayout.LayoutParams(320,RelativeLayout.LayoutParams.FILL_PARENT);
		// rl.leftMargin = 0;
		// home.addView(b, rl);
		// b = new Button(this.getContext());
		// rl = new
		// RelativeLayout.LayoutParams(320,RelativeLayout.LayoutParams.FILL_PARENT);
		// rl.leftMargin =pageWidth;
		// home.addView(b, rl);
		// b = new Button(this.getContext());
		// rl = new
		// RelativeLayout.LayoutParams(320,RelativeLayout.LayoutParams.FILL_PARENT);
		// rl.leftMargin = pageWidth*2;
		// home.addView(b, rl);

		// for(int i =0;i<mCount;i++){
		// Button b = new Button(this.getContext());
		// RelativeLayout.LayoutParams rl = new
		// RelativeLayout.LayoutParams(320,RelativeLayout.LayoutParams.FILL_PARENT);
		// rl.leftMargin = pageWidth*i;
		// home.addView(b, rl);
		// }

		@SuppressWarnings("unused")
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("error mode.");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("error mode.");
		}

		// ViewGroup child = (ViewGroup)getChildAt(0);
		// for(int j=0;j<child.getChildCount();j++){
		// RelativeLayout.LayoutParams rl =
		// (RelativeLayout.LayoutParams)child.getChildAt(j).getLayoutParams();
		// //RelativeLayout.LayoutParams rl = new
		// RelativeLayout.LayoutParams(pageWidth,RelativeLayout.LayoutParams.FILL_PARENT);
		// rl.width = pageWidth;
		// // rl.leftMargin = pageWidth*j;
		// child.getChildAt(j).setLayoutParams(rl);
		// }
		// : pageWidthSpec;
		// pageWidth = Math.min(pageWidth, getMeasuredWidth());

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			// if( i == 0){
			// getChildAt(i).measure(
			// MeasureSpec.makeMeasureSpec(mPageWidth * mCount,
			// MeasureSpec.EXACTLY), heightMeasureSpec);
			// }
			//
			if (i == 0) {
				getChildAt(i).measure(
						MeasureSpec.makeMeasureSpec(mPageWidth * mCount
								+ mContentOffSet, MeasureSpec.EXACTLY),
						heightMeasureSpec);
			} else {
				getChildAt(i).measure(
						MeasureSpec.makeMeasureSpec(mPageWidth * mCount,
								MeasureSpec.EXACTLY), heightMeasureSpec);
			}
			// ViewGroup child = (ViewGroup)getChildAt(i);
			// for(int j=0;j<child.getChildCount();j++){
			// child.getChildAt(j).measure(MeasureSpec.makeMeasureSpec(pageWidth,
			// MeasureSpec.EXACTLY),
			// heightMeasureSpec);
			// }
		}
		// Log.i("zhoubo", "%%%%%%%%%%%%%%%% ");
		// scrollTo(mCurrentScreen * width, 0);
	}
 
	@Override
	protected void dispatchDraw(Canvas canvas) {

		try {
			final long drawingTime = getDrawingTime();
			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				drawChild(canvas, getChildAt(i), drawingTime);
			}
			// int currentPage = adjustedScrollX / pageWidth;

			int scrollX = getScrollX();// + pageWidthPadding();
			// int whichScreen = scrollX/pageWidth;
			int whichScreen = (scrollX + mPageWidth / 2) / mPageWidth;
			relayoutItem(whichScreen);

			for (OnScrollListener mListener : mListeners) {
				int adjustedScrollX = getScrollX() + pageWidthPadding();
				mListener.onScroll(adjustedScrollX);
				// if((adjustedScrollX %
				// pageWidth)>(pageWidth*2)/3||(adjustedScrollX %
				// pageWidth)<(pageWidth*1)/3){
				// }
				if ((adjustedScrollX % mPageWidth) == 0) {
					mListener.onViewScrollFinished(adjustedScrollX / mPageWidth);
				}
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		// lastpage =currentPage;
	}

	int pageWidthPadding() {
		return ((getMeasuredWidth() - mPageWidth) / 2);
	}

	public Workspace(Context context) {
		super(context);
		mScroller = new Scroller(context);

		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

		this.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.FILL_PARENT));
		mGestureDetector = new GestureDetector(this);

	}

	public Workspace(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Context context = getContext();
		// WorkspaceOvershootInterpolator mScrollInterpolator = new
		// WorkspaceOvershootInterpolator();
		mScroller = new Scroller(context);
		// mScroller = new Scroller(context);

		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

		this.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.FILL_PARENT));
		mGestureDetector = new GestureDetector(this);
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.DragableSpace);
		mCurrentScreen = a.getInteger(R.styleable.DragableSpace_default_screen,
				0);

		TypedArray a1 = context.obtainStyledAttributes(attrs,
				R.styleable.Workspace);
		// pageWidthSpec =
		// a1.getDimensionPixelSize(R.styleable.Workspace_pageWidth,
		// SPEC_UNDEFINED);
		// init();
		a1.recycle();
	}

	// private void init() {
	// View view = this.findViewById(R.id.home);
	// Log.i("", "view=="+view.getId());
	// }

	// /**
	// *
	// * 包含了单双指滑动处理
	// * */
	// @Override
	// public boolean onInterceptTouchEvent(MotionEvent ev) {
	// final int action = ev.getAction();
	// if ((action == MotionEvent.ACTION_MOVE)
	// && (mTouchState != TOUCH_STATE_REST)) {
	// return true;
	// }
	//
	// final float x = ev.getX();
	// final float y = ev.getY();
	// boolean isIntercepted = false;
	// if (remoteControler != null) {
	// // Log.i("Workspace", "remoteControl ====" + remoteControler);
	// isIntercepted = remoteControler.isIntercepted();
	// }
	// // Log.i("Workspace", "isIntercepted ====" + isIntercepted);
	// if (isIntercepted) {
	// return false;
	// }
	// switch (action) {
	// case MotionEvent.ACTION_MOVE:

	public static int distance(int x1, int y1, int x2, int y2) {
		int xDistance = Math.abs(x1 - x2);
		int yDistance = Math.abs(y1 - y2);
		double twoFingerDistance = Math.sqrt(xDistance + xDistance);
		return (int) twoFingerDistance;
	}

	int twoFingerDistance = 0;
	boolean isIntercepted = false;

	// boolean
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	return false;
    }
    
	/**
	 * 
	 * 包含了单双指滑动处理
	 * */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float x = ev.getX();
		final float y = ev.getY();

		// Log.i("Workspace", "x====="+x);
		// Log.i("Workspace", "y====="+y);
		// Log.i("Workspace", "spaceWidth====="+spaceWidth);
		// Log.i("Workspace",
		// "spaceWidth+mContentOffSet====="+spaceWidth+mContentOffSet);
		// Log.i("Workspace", "mContentOffSet====="+mContentOffSet);
		if (remoteControler != null && !isIntercepted
				&& this.mCurrentScreen == 1) {
			Log.i("Workspace", "remoteControl ====" + remoteControler);
			isIntercepted = remoteControler.isIntercepted();
		}

		if (!isIntercepted && action == MotionEvent.ACTION_MOVE
				&& this.mCurrentScreen == 1) {
			if (ev.getPointerCount() > 1) {

				int distance = distance((int) ev.getX(0), (int) ev.getY(0),
						(int) ev.getX(1), (int) ev.getY(1));
				if (twoFingerDistance == 0) {
					twoFingerDistance = distance;
				}

				if (distance - twoFingerDistance > mTouchSlop / 2) {
					isIntercepted = true;
				}

			}

		}

		// Log.i("Workspace", "isIntercepted ===="+isIntercepted);
		if (isIntercepted && action != MotionEvent.ACTION_UP) {
			return false;
		}
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);
			final int yDiff = (int) Math.abs(y - mLastMotionY);

			boolean xMoved = (xDiff > mTouchSlop && xDiff > yDiff);
			//
			if (xMoved && ev.getPointerCount() > 1) // 双指滑动的处理去掉后 为单指滑动处理
			{
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			if (mCurrentScreen == 0 && ev.getPointerCount() > 1) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			if (mCurrentScreen == 0 && x > mPageWidth
					&& x < mPageWidth + mContentOffSet
					&& mTouchState != TOUCH_STATE_SCROLLING) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			break;

		case MotionEvent.ACTION_DOWN:
			// Remember location of down touch
			// adjustedScrollX = getScrollX();// + pageWidthPadding();
			// whichScreen = adjustedScrollX/pageWidth;
			// whichScreen += (adjustedScrollX%pageWidth)>(pageWidth/2)?1:0;
			// relayoutItem(whichScreen);

			// if (!mScroller.isFinished()) {
			// mScroller.abortAnimation();
			// }

			mLastMotionX = x;
			mLastMotionY = y;

			// 双指滑动的处理
			if (!mScroller.isFinished() && ev.getPointerCount() > 1
					&& isIntercepted) {
				// mTouchState = TOUCH_STATE_SCROLLING;
			} else {
				mTouchState = TOUCH_STATE_REST;
			}

			if (mCurrentScreen == 0 && x > mPageWidth
					&& x < mPageWidth + mContentOffSet
					&& mTouchState != TOUCH_STATE_SCROLLING) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			// 单指滑动的处理
			// mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST :
			// TOUCH_STATE_SCROLLING;
			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// Release the drag
			mTouchState = TOUCH_STATE_REST;
			twoFingerDistance = 0;
			isIntercepted = false;
			if(remoteControler!=null)
				remoteControler.handleTouchEventUp(ev);
			break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final float x = event.getX();

		if (mCurrentScreen == 0 && x > mPageWidth
				&& x < mPageWidth + mContentOffSet) {
			if (mGestureDetector.onTouchEvent(event))
				return true;
		}
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		final int action = event.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// Log.i(LOG_TAG, "event : down");

			if (!mScroller.isFinished()) {
				// int adjustedScrollX = getScrollX();// + pageWidthPadding();
				// int whichScreen = adjustedScrollX/pageWidth;
				// whichScreen += (adjustedScrollX%pageWidth)>(pageWidth/2)?1:0;
				// relayoutItem(whichScreen);
				// if (!mScroller.isFinished()) {
				// mScroller.abortAnimation();
				// }
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			// Log.i(LOG_TAG,"event : move");
			// if (mTouchState == TOUCH_STATE_SCROLLING) {
			// Scroll to follow the motion event
			// final int deltaX = (int) (mLastMotionX - x);
			// mLastMotionX = x;
			//
			// //Log.i(LOG_TAG, "event : move, deltaX " + deltaX + ", mScrollX "
			// + mScrollX);
			//
			// if (deltaX < 0) {
			// if (mScrollX > 0) {
			// scrollBy(Math.max(-mScrollX, deltaX), 0);
			// }
			// } else if (deltaX > 0) {
			// final int availableToScroll = getChildAt(getChildCount() - 1)
			// .getRight()
			// - mScrollX - getWidth();
			// if (availableToScroll > 0) {
			// scrollBy(Math.min(availableToScroll, deltaX), 0);
			// }
			// }

			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			int deltaX = (int) (mLastMotionX - x);
			mLastMotionX = x;
			// Log.i("Workspace", "getScrollX()========="+getScrollX());
			// Log.i("Workspace",
			// "mPageWidth-mContentOffSet========="+(mPageWidth-mContentOffSet));
			if (getScrollX() < 0 || getScrollX() > mPageWidth) {
				deltaX = 0;
			} else {
				if (getScrollX() + deltaX < 0) {
					deltaX = -getScrollX();
				} else if (getScrollX() + deltaX > mPageWidth) {
					deltaX = mPageWidth - getScrollX();
				}
			}
			// Log.i("Workspace",
			// "getScrollX()+deltaX========="+(getScrollX()+deltaX));
			// Log.i("Workspace", "mPageWidth======="+mPageWidth);
			// Log.i("Workspace", "mContentOffSet========"+mContentOffSet);

			scrollBy(deltaX, 0);
			invalidate();
			// }
			break;
		case MotionEvent.ACTION_UP:
			// Log.i(LOG_TAG, "event : up");
			if (remoteControler != null) {
				remoteControler.handleTouchEventUp(event);
			}
			handleTouchEventUp(event);
			// if (mTouchState == TOUCH_STATE_SCROLLING) {

			break;
		case MotionEvent.ACTION_CANCEL:
			// Log.i(LOG_TAG, "event : cancel");
			mTouchState = TOUCH_STATE_REST;
		}
		mScrollX = this.getScrollX();

		return true;
	}

	@Override
	public void handleTouchEventUp(MotionEvent event) {
		final VelocityTracker velocityTracker = mVelocityTracker;
		velocityTracker.computeCurrentVelocity(1000);
		int velocityX = (int) velocityTracker.getXVelocity();

		if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
			// Fling hard enough to move left
			snapToScreen(mCurrentScreen - 1);
		} else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < mCount - 1) {
			// Fling hard enough to move right
			snapToScreen(mCurrentScreen + 1);
		} else {
			snapToDestination();
		}
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
		// }
		mTouchState = TOUCH_STATE_REST;
	}

	private void snapToDestination() {
		final int screenWidth = mPageWidth;// getWidth();
		int whichScreen = (mScrollX + (screenWidth / 2)) / screenWidth;
		// Log.i(LOG_TAG, "from des");
		if (whichScreen >= mCount) {
			whichScreen = mCount - 1;
		} else if (whichScreen < 0) {
			whichScreen = 0;
		}

		snapToScreen(whichScreen);
	}

	public void snapToScreen(int whichScreen) {
		// Log.i(LOG_TAG, "snap To Screen " + whichScreen);
		// int adjustedScrollX = getScrollX() + pageWidthPadding();
		// int index = adjustedScrollX/pageWidth;
		// index += (adjustedScrollX%pageWidth)>(pageWidth/2)?1:0;
		// relayoutItem(whichScreen);
		// relayoutItem(whichScreen);
		// mCurrentScreen = whichScreen;
		// final int newX = whichScreen * mPageWidth;
		// final int delta = newX - mScrollX;
		// mScroller.startScroll(mScrollX, 0, delta, 0, Math.abs(delta) * 2);
		// invalidate();
		snapToScreen(whichScreen, 0);
	}

	public void snapToScreen(int whichScreen, int durationTimer) {
		// Log.i(LOG_TAG, "snap To Screen " + whichScreen);
		// int adjustedScrollX = getScrollX() + pageWidthPadding();
		// int index = adjustedScrollX/pageWidth;
		// index += (adjustedScrollX%pageWidth)>(pageWidth/2)?1:0;
		// relayoutItem(whichScreen);
		// relayoutItem(whichScreen);
		mCurrentScreen = whichScreen;
		final int newX = whichScreen * mPageWidth;
		final int delta = newX - mScrollX;
		mScroller.startScroll(mScrollX, 0, delta, 0, Math.abs(delta + 10) * 2
				+ durationTimer);
		invalidate();
	}

	public void relayout() {
		// mPageWidth = pageWith;
		//
		// Log.i("", "pageWith==" + mPageWidth);
		int pageWidth = mPageWidth;

		reinitX(mCurrentScreen, pageWidth);
		lastRelayoutItemIndex = -1;
		this.relayoutItem(mCurrentScreen);
		setToScreen(mCurrentScreen, mPageWidth);
		this.postInvalidate();
	}

	public void initIndex(int index) {
		mCurrentScreen = index;
		relayout();
	}

	public void reInitItemLoadData() {
		int currentPage = this.getCurrentScreen();
		int wichItemIndex = mCurrentScreen % mBufferCount;
		// Log.i("zhoubo", "wichItemIndex==" + wichItemIndex);
		Item item = channelButtonArray[wichItemIndex];
		this.itemLoaded(currentPage);
		Item nextItem = item;

		boolean isNextNull = false;
		int nextNum = 1;
		while (!isNextNull) {
			if (nextItem.nextItem != null) {
				nextItem = nextItem.nextItem;
				// rl.leftMargin = mPageWidth*(currentPage+nextNum);
				// if(currentPage+nextNum){
				//
				// }
				this.itemLoaded(currentPage + nextNum);
				// Log.i("zhoubo", "currentPage+nextNum=="
				// + (currentPage + nextNum));
				// nextItem.currentView.setLayoutParams(rl);
			} else if (nextItem.nextItem == null) {
				isNextNull = true;
				break;
			}
			nextNum++;
		}

		Item upItem = item;
		int lastNum = 1;
		boolean isLastNull = false;
		while (!isLastNull) {
			if (upItem.upItem != null) {
				upItem = upItem.upItem;
				// rl =
				// (RelativeLayout.LayoutParams)upItem.currentView.getLayoutParams();
				// rl.leftMargin = mPageWidth*(currentPage-lastNum);
				this.itemLoaded(currentPage - lastNum);
				// Log.i("zhoubo", "currentPage-lastNum=="
				// + (currentPage - lastNum));
			} else if (upItem.upItem == null) {
				isLastNull = true;
				break;
			}
			lastNum++;
		}
		// Iitem = channelButtonArray[wichItemIndex];
		// this.itemLoaded(currentPage);
	}

	public void reinitX(int currentPage, int pageWith) {

		// this.relayoutItem(mCurrentScreen);
		// Item item = mItemlist.get(currentPage);
		int wichItemIndex = mCurrentScreen % mBufferCount;
		Item item = channelButtonArray[wichItemIndex];

		RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) item.currentView
				.getLayoutParams();
		rl.leftMargin = getleftMargin(currentPage);
		rl.width = mPageWidth;
		item.currentView.setLayoutParams(rl);
		Item nextItem = item;

		boolean isNextNull = false;
		int nextNum = 1;
		while (!isNextNull) {
			if (nextItem.nextItem != null) {
				nextItem = nextItem.nextItem;
				rl = (RelativeLayout.LayoutParams) nextItem.currentView
						.getLayoutParams();
				rl.leftMargin = getleftMargin(currentPage + nextNum); // mPageWidth*(currentPage+nextNum);
				rl.width = mPageWidth;
				nextItem.currentView.setLayoutParams(rl);
			} else if (nextItem.nextItem == null) {
				isNextNull = true;
				break;
			}
			nextNum++;
		}

		Item upItem = item;
		int lastNum = 1;
		boolean isLastNull = false;
		while (!isLastNull) {
			if (upItem.upItem != null) {
				upItem = upItem.upItem;
				rl = (RelativeLayout.LayoutParams) upItem.currentView
						.getLayoutParams();
				rl.leftMargin = getleftMargin(currentPage - lastNum);
				rl.width = mPageWidth;
				upItem.currentView.setLayoutParams(rl);
			} else if (upItem.upItem == null) {
				isLastNull = true;
				break;
			}
			lastNum++;
		}

		if (currentPage == 1) {
			// item = channelButtonArray[0];
			rl = (RelativeLayout.LayoutParams) item.currentView
					.getLayoutParams();
			rl.width = mPageWidth + mContentOffSet;
			item.currentView.setLayoutParams(rl);
		} else {
			wichItemIndex = 1 % mBufferCount;
			item = channelButtonArray[wichItemIndex];
			rl = (RelativeLayout.LayoutParams) item.currentView
					.getLayoutParams();
			rl.width = mPageWidth + mContentOffSet;
			item.currentView.setLayoutParams(rl);
		}

		// item = channelButtonArray[1];
		// rl = (RelativeLayout.LayoutParams) item.currentView
		// .getLayoutParams();
		// rl.width = mPageWidth+75;
		// item.currentView.setLayoutParams(rl);

		// item = channelButtonArray[2];
		// rl = (RelativeLayout.LayoutParams) item.currentView
		// .getLayoutParams();
		// rl.width = mPageWidth+75;
		// item.currentView.setLayoutParams(rl);
	}

	public void setToScreen(int whichScreen, int pageWith) {
		// Log.i(LOG_TAG, "set To Screen " + whichScreen);

		mCurrentScreen = whichScreen;
		final int newX = whichScreen * pageWith;
		// item.currentView = item.currentView.getLayoutParams();
		// for(){

		// }
		mScroller.startScroll(newX, 0, 0, 0, 0);
		// mScroller.computeScrollOffset()
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth,
						child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}

	}

	// public void relayoutItem(int index){
	// Item item = mItemlist.get(index);
	// // Item item =(Item)view.getTag();
	// if(index==0||index==mItemlist.size()-1){
	// return;
	// }
	//
	// int nextnum = channelButtonArray.length/2;
	// Item nextItem = item;
	// boolean isNextNull = false;
	// int nextNum = 0;
	// while(!isNextNull){
	// if(nextItem.nextItem!=null){
	// nextItem = nextItem.nextItem;
	// }else if(nextItem.nextItem==null){
	// isNextNull = true;
	// break;
	// }
	// nextNum++;
	// }
	//
	// Item upItem = item;
	// int lastNum = 0;
	// boolean isLastNull = false;
	// while(!isLastNull){
	// if(upItem.upItem!=null){
	// upItem = upItem.upItem;
	// }else if(upItem.upItem==null){
	// isLastNull = true;
	// break;
	// }
	// lastNum++;
	// }
	//
	// if(lastNum-nextNum>1){
	// if(index+nextNum==mItemlist.size()-1){
	// return;
	// }
	// upItem.nextItem.upItem = null;//将头链掐�?
	// nextItem.nextItem = upItem;//将尾部接上头�?
	// upItem.upItem = nextItem;//将头结链到最后一个尾�?
	// upItem.nextItem = null;//将头尾部接头指控
	// AbsoluteLayout.LayoutParams lp =
	// ((AbsoluteLayout.LayoutParams)upItem.currentView.getLayoutParams());
	// lp.x = mOffSetXArray[index+nextNum+1];
	// upItem.currentView.setLayoutParams(lp);
	// setItemLoad(upItem,index+nextNum+1);
	// }
	//
	// if(nextNum-lastNum>1){
	// if(index-lastNum==0){
	// return;
	// }
	// nextItem.upItem.nextItem = null;//将尾拿下�?
	// upItem.upItem = nextItem;//接在头部
	// nextItem.upItem = null;//接好后把尾前指针制空
	// nextItem.nextItem = upItem;//将头尾部接头指控
	// // item.upItem.upItem = null;
	// // item.nextItem = tempItem;
	// // tempItem.nextItem = null;
	// // tempItem.upItem = item;
	// AbsoluteLayout.LayoutParams lp =
	// ((AbsoluteLayout.LayoutParams)nextItem.currentView.getLayoutParams());
	// lp.x = mOffSetXArray[index-lastNum-1];
	// nextItem.currentView.setLayoutParams(lp);
	// setItemLoad(nextItem,index-lastNum-1);
	// }
	public int getSpaceWidth() {
		return spaceWidth;
	}

	public void setSpaceWidth(int spaceWidth) {
		this.spaceWidth = spaceWidth;
	}

	public int getleftMargin(int index) {
		int x = mPageWidth * index - spaceWidth;
		return x;
	}

	// private int pageWidthSpec=-1;
	public static final int SPEC_UNDEFINED = -1;

	public int getBufferCount() {
		return mBufferCount;
	}

	public void setBufferCount(int BufferCount) {
		this.mBufferCount = BufferCount;
		initCache();
	}

	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	public void setCurrentScreen(int mCurrentScreen) {
		this.mCurrentScreen = mCurrentScreen;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			mScrollX = mScroller.getCurrX();
			scrollTo(mScrollX, 0);
			postInvalidate();
		}
	}

	// public boolean onKeyDown (int keyCode, KeyEvent event) {
	// Log.i("Workspace", "keyCode======"+keyCode);
	// return super.onKeyDown(keyCode, event);
	// }

	public void initSlideView(View menuView, View ContentView,
			OnItemListener onItemListener, int screenWidth, int off_width) {
		// WindowManager windowManager = this.getWindowManager();
		// Display display = windowManager.getDefaultDisplay();
		// int screenWidth = display.getWidth();
		// control = (Indicator) findViewById(R.id.indicator);
		this.setCount(2);
		this.setPageWidth(screenWidth + off_width);
		this.setBufferCount(3);
		this.setSpaceWidth(off_width);
		this.setOnItemListener(onItemListener);

		this.initIndex(1);
		this.reInitItemLoadData();
		this.initAllItemView(new View[] { menuView, ContentView });
	}

	//
	// public interface SlideRemoteControler {
	// public boolean isIntercepted();
	// public void handleTouchEventUp();
	// }

	@Override
	public boolean isIntercepted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		snapToScreen(1, 500);
		return true;
	}

	// @Override
	// public void handleTouchEventUp() {
	// // TODO Auto-generated method stub
	//
	// }
}