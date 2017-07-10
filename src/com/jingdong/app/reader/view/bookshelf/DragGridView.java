package com.jingdong.app.reader.view.bookshelf;

import java.util.LinkedList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import com.android.mzbook.sortview.optimized.HeaderDragItemAdapter;
import com.android.mzbook.sortview.optimized.HeaderGridView;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.util.ScreenUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * 书架可拖拽布局
 * @author xuhongwei
 */
public class DragGridView extends HeaderGridView{
	/** item长按响应的时间 */
	private long dragResponseMS = 1000;
	/** 是否可以拖拽，默认不可以 */
	private boolean isDrag = false;
	/** 手指按下时X坐标 */
	private int mDownX;
	/** 手指按下时Y坐标 */
	private int mDownY;
	/** 手指移动后X坐标 */
	private int moveX;
	/** 手指移动后Y坐标 */
	private int moveY;
	/** 正在拖拽项的位置 */
	private int mDragPosition;
	/** 被合并项的位置 */
	private int mergePosition;
	/** 刚开始拖拽的item对应的View */
	private View mStartDragItemView = null;
	/** 用于存放拖拽的镜像view的布局 */
	private RelativeLayout mDragImageView;
	/** 用于显示拖拽镜像，实现放大缩小动画的view */
	private ImageView mDragImage;
	private WindowManager mWindowManager;
	/** item镜像的布局参数 */
	private WindowManager.LayoutParams mWindowLayoutParams;
	/** 我们拖拽的item对应的Bitmap */
	private Bitmap mDragBitmap;
	/** 按下的点到所在item的上边缘的距离 */
	private int mPoint2ItemTop ; 
	/** 按下的点到所在item的左边缘的距离 */
	private int mPoint2ItemLeft;
	/** DragGridView距离屏幕顶部的偏移量 */
	private int mOffset2Top;
	/** DragGridView距离屏幕左边的偏移量 */
	private int mOffset2Left;
	/** 状态栏的高度 */
	private int mStatusHeight; 
	/** DragGridView自动向下滚动的边界值 */
	private int mDownScrollBorder;
	/** DragGridView自动向上滚动的边界值 */
	private int mUpScrollBorder;
	/** DragGridView自动滚动的速度 */
	private static final int speed = 20;
	/** 动画结束标识 */
	private boolean mAnimationEnd = true;
	private HeaderDragItemAdapter mDragAdapter;
	private int mNumColumns;
	private int mColumnWidth;
	private boolean mNumColumnsSet;
	private int mHorizontalSpacing;
	/** 合并状态标识 */
	private boolean inMergeStatus = false;
	/** 是否缩放了拖动的view */
	private boolean isScaleDragView = false;
	/** item拖拽后合并监听 */
	private OnItemDragListener mOnItemDragListener = null;
	/** 禁止滑动标识 */
	private boolean mProhibitSliding = false;

	public DragGridView(Context context) {
		this(context, null);
		init(context);
	}
	
	public DragGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(context);
	}

	public DragGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mStatusHeight = ScreenUtils.getStatusHeight(context); //获取状态栏的高度
		
		if(!mNumColumnsSet){
			mNumColumns = AUTO_FIT;
		}
	}
	
	private Handler mHandler = new Handler();
	/** 用来处理是否为长按的Runnable */
	private Runnable mLongClickRunnable = new Runnable() {
		@Override
		public void run() {
			if(mDragPosition < 3 || (mDragPosition-3) == (mDragAdapter.getCount() - 1)) {
				return;
			}
			mProhibitSliding = true;
			isDrag = true; //设置可以拖拽
			mStartDragItemView.setVisibility(View.INVISIBLE);//隐藏该item
			//根据我们按下的点显示item镜像
			createDragImage(mDragBitmap, mDownX, mDownY);
			//长按监听回调
			OnItemLongClickListener listener = getOnItemLongClickListener();
			if (listener != null && null != mStartDragItemView) {
				listener.onItemLongClick(DragGridView.this, mStartDragItemView, mDragPosition, 0);
			}
		}
	};
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		mDragAdapter = (HeaderDragItemAdapter)adapter;
	}

	@Override
	public void setNumColumns(int numColumns) {
		super.setNumColumns(numColumns);
		mNumColumnsSet = true;
		this.mNumColumns = numColumns;
	}

	@Override
	public void setColumnWidth(int columnWidth) {
	    super.setColumnWidth(columnWidth);
	    mColumnWidth = columnWidth;
	}
	
    @Override
	public void setHorizontalSpacing(int horizontalSpacing) {
		super.setHorizontalSpacing(horizontalSpacing);
		this.mHorizontalSpacing = horizontalSpacing;
	}

    /**
     * 若设置为AUTO_FIT，计算有多少列
     */
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mNumColumns == AUTO_FIT) {
            int numFittedColumns;
            if (mColumnWidth > 0) {
                int gridWidth = Math.max(MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight(), 0);
                numFittedColumns = gridWidth / mColumnWidth;
                if (numFittedColumns > 0) {
                	while (numFittedColumns != 1) {
                        if (numFittedColumns * mColumnWidth + (numFittedColumns - 1) * mHorizontalSpacing > gridWidth) {
                        	numFittedColumns--;
                        } else {
                        	break;
                        }
                    }
                } else {
                    numFittedColumns = 1;
                }
            } else {
                numFittedColumns = 2;
            }
            mNumColumns = numFittedColumns;
        } 

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
	
	/**
	 * 设置响应拖拽的毫秒数，默认是1000毫秒
	 * @param dragResponseMS
	 */
	public void setDragResponseMS(long dragResponseMS) {
		this.dragResponseMS = dragResponseMS;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			mDownX = (int) ev.getX();
			mDownY = (int) ev.getY();
			
			//根据按下的X,Y坐标获取所点击item的position
			mDragPosition = pointToPosition(mDownX, mDownY);
			if(mDragPosition == AdapterView.INVALID_POSITION){
				return super.dispatchTouchEvent(ev);
			}
			
			//使用Handler延迟dragResponseMS执行mLongClickRunnable
			mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
			
			//根据position获取该item所对应的View
			mStartDragItemView = getChildAt(mDragPosition - getFirstVisiblePosition());
			
			mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
			mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();
			
			mOffset2Top = (int) (ev.getRawY() - mDownY);
			mOffset2Left = (int) (ev.getRawX() - mDownX);
			
			//获取DragGridView自动向上滚动的偏移量，小于这个值，DragGridView向下滚动
			mDownScrollBorder = getHeight() / 6;
			//获取DragGridView自动向下滚动的偏移量，大于这个值，DragGridView向上滚动
			mUpScrollBorder = getHeight() * 5/6;
			
//			ImageView touming = (ImageView)mStartDragItemView.findViewById(R.id.touming);
//			if(null != touming) {
//				touming.setPressed(false);
//			}
			
			//开启mDragItemView绘图缓存
			mStartDragItemView.setDrawingCacheEnabled(true);
			mStartDragItemView.buildDrawingCache();
			//获取mDragItemView在缓存中的Bitmap对象
			Bitmap bitmap = mStartDragItemView.getDrawingCache();
			if(null != bitmap) {
				mDragBitmap = Bitmap.createBitmap(bitmap);
			}
			//这一步很关键，释放绘图缓存，避免出现重复的镜像
			mStartDragItemView.destroyDrawingCache();
			break;
		case MotionEvent.ACTION_MOVE:
			
			int moveX = (int)ev.getX();
			int moveY = (int) ev.getY();
			
			if(Math.abs(moveY - mDownY) > 50 ) {
				mProhibitSliding = false;
				mHandler.removeCallbacks(mLongClickRunnable);
			}
			
			//如果我们在按下的item上面移动，只要不超过item的边界我们就不移除mRunnable
			if(!isTouchInItem(mStartDragItemView, moveX, moveY)){
				mHandler.removeCallbacks(mLongClickRunnable);
			}
			
			break;
		case MotionEvent.ACTION_UP:
			mHandler.removeCallbacks(mLongClickRunnable);
			mHandler.removeCallbacks(mScrollRunnable);
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
	
	/**
	 * 是否点击在GridView的item上面
	 * @param itemView
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isTouchInItem(View dragView, int x, int y){
		if(dragView == null){
			return false;
		}
		int leftOffset = dragView.getLeft();
		int topOffset = dragView.getTop();
		if(x < leftOffset || x > leftOffset + dragView.getWidth()){
			return false;
		}
		
		if(y < topOffset || y > topOffset + dragView.getHeight()){
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(isDrag && mDragImageView != null){
			switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				moveX = (int) ev.getX();
				moveY = (int) ev.getY();
				
				//拖动item
				onDragItem(moveX, moveY);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				onStopDrag();
				isDrag = false;
				break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}
	
	
	/**
	 * 创建拖动的镜像
	 * @param bitmap 镜像
	 * @param downX 按下的点相对父控件的X坐标
	 * @param downY 按下的点相对父控件的X坐标
	 */
	private void createDragImage(Bitmap bitmap, int downX , int downY) {
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; //图片之外的其他地方透明
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowLayoutParams.alpha = 0.7f; //透明度
		mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;  
		mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;  
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  
	                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE ;
		
		mDragImageView = new RelativeLayout(getContext());
		mDragImage = new ImageView(getContext());  
		mDragImage.setImageBitmap(bitmap); 
		mDragImage.setScaleType(ScaleType.FIT_XY);
		mDragImageView.addView(mDragImage);
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);  
	}
	
	/**
	 * 从界面上面移动拖动镜像
	 */
	private void removeDragImage() {
		if (mDragImageView != null) {
			mDragImageView.clearAnimation();
			mDragImageView.setVisibility(View.INVISIBLE);
			if (null != mDragImage) {
				mDragImage.setVisibility(View.INVISIBLE);
				mDragImage.setImageBitmap(null);
				mDragImage.clearAnimation();
				mDragImage = null;
				if (null != mDragBitmap) {
					if (!mDragBitmap.isRecycled()) {
						mDragBitmap.recycle();
						mDragBitmap = null;
					}
				}
			}
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}
	}
	
	/**
	 * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
	 * @param x
	 * @param y
	 */
	private void onDragItem(int moveX, int moveY) {
		mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); //更新镜像的位置

		//获取我们手指移动到的那个item的position
		final int tempPosition = pointToPosition(moveX, moveY);
		//假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
		if (tempPosition > 2 && tempPosition != mDragPosition && (tempPosition-3) != (mDragAdapter.getCount() - 1) &&
				tempPosition != AdapterView.INVALID_POSITION && mAnimationEnd) {
			if(mOnItemDragListener.isFolder(mDragPosition-3)) {
				onSwapItem(moveX, moveY);
			} else {
				final View tempView = getChildAt(tempPosition - getFirstVisiblePosition());
				if (null != tempView) {
					Rect mRect = new Rect();
					int[] position = new int[2];
					tempView.getLocationInWindow(position);
					tempView.getHitRect(mRect);
					int left_position = position[0];
					int top_position = position[1];
					int width = mRect.width();
					int height = mRect.height();
					float center_x = left_position + width / 2.0f;
					float center_y = top_position + height / 2.0f;
					// 处理滑动view 的中心点
					float drag_center_x = moveX - mPoint2ItemLeft + mOffset2Left + width / 2.0f;
					float drag_center_y = moveY - mPoint2ItemTop + mOffset2Top + height / 2.0f;
					float radius = Math.min(width / 3, height / 3);
					float distance = (float) Math.sqrt((Math.abs(drag_center_x - center_x)
							* Math.abs(drag_center_x - center_x) + Math.abs(drag_center_y - center_y)
							* Math.abs(drag_center_y - center_y)));
					
					if (distance < radius) {
						// 进入目标中心区域
						if (mDragImageView != null) {
							if ( !isScaleDragView ) {
								inMergeStatus = true;// 处在合并状态
								isScaleDragView = true;
								scaleToMiniView(mDragImage, center_x + mOffset2Left, center_y + mOffset2Top );
							}
						}
					} else {
						if( isScaleDragView ) {
							inMergeStatus = false;
							scaleToNormalView(mDragImage, 0, 0);
							isScaleDragView = false;
						}
						
						float radius2 = Math.min(width / 2, height / 2);
						if (1 == Math.abs(tempPosition - mDragPosition)) {//左右移动
							if (mDownX < moveX) { //横向向右移动
								if ((center_x < moveX) && (center_x > mDownX)) {
									if (distance < radius2) {
										onSwapItem(moveX, moveY);
									}
								}
							}else {
								if ((center_x > moveX) && (center_x < mDownX)) {
									if (distance < radius2) {
										onSwapItem(moveX, moveY);
									}
								}
							}
						}else {
							if (distance < radius2) {
								onSwapItem(moveX, moveY);
							}
						}
						
					}
					
				}
			}
			
		}
		
		//GridView自动滚动
		mHandler.post(mScrollRunnable);
	}
	
	/**
	 * 缩小view
	 * @param x view 在屏幕中的横坐标
	 * @param y view 在屏幕的纵坐标
	 */
	public void scaleToMiniView(View view, float x, float y) {
		Animation animation = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
		animation.setDuration(300);
		animation.setFillAfter(true);
		view.startAnimation(animation);
	}
	
	/**
	 * 放大view
	 * @param x view 在屏幕中的横坐标
	 * @param y view 在屏幕的纵坐标
	 */
	public void scaleToNormalView(View view, int x, int y) {
		Animation animation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
		animation.setDuration(500);
		animation.setFillAfter(true);
		view.startAnimation(animation);
	}
	
	/**
	 * 当moveY的值大于向上滚动的边界值，触发GridView自动向上滚动
	 * 当moveY的值小于向下滚动的边界值，触发GridView自动向下滚动
	 * 否则不进行滚动
	 */
	private Runnable mScrollRunnable = new Runnable() {
		@Override
		public void run() {
			if ( mProhibitSliding ) {
				return;
			}
			
			int scrollY;
			if(getFirstVisiblePosition() == 0 || getLastVisiblePosition() == getCount() - 1){
				mHandler.removeCallbacks(mScrollRunnable);
			}
			
			if(moveY > mUpScrollBorder){
				 scrollY = speed;
				 mHandler.postDelayed(mScrollRunnable, 25);
			}else if(moveY < mDownScrollBorder){
				scrollY = -speed;
				 mHandler.postDelayed(mScrollRunnable, 25);
			}else{
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
			}
			
			smoothScrollBy(scrollY, 10);
		}
	};
	
	/**
	 * 交换item,并且控制item之间的显示与隐藏效果
	 * @param moveX
	 * @param moveY
	 */
	private void onSwapItem(int moveX, int moveY){
		//获取我们手指移动到的那个item的position
		final int tempPosition = pointToPosition(moveX, moveY);
		//假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
		if(tempPosition > 2 && tempPosition != mDragPosition && tempPosition != AdapterView.INVALID_POSITION && mAnimationEnd){
			mOnItemDragListener.onSwapItem(mDragPosition - 3, tempPosition - 3);
			mDragAdapter.setHideItem(tempPosition - 3);
			
			final ViewTreeObserver observer = getViewTreeObserver();
			observer.addOnPreDrawListener(new OnPreDrawListener() {

				@Override
				public boolean onPreDraw() {
					observer.removeOnPreDrawListener(this);
					animateReorder(mDragPosition, tempPosition);
					mDragPosition = tempPosition;
					return true;
				}
			} );
		}
	}
	
	/**
	 * 创建移动动画
	 * @param view
	 * @param startX
	 * @param endX
	 * @param startY
	 * @param endY
	 * @return
	 */
	private AnimatorSet createTranslationAnimations(View view, float startX,
			float endX, float startY, float endY) {
		ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX", startX, endX);
		ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY", startY, endY);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animX, animY);
		return animSetXY;
	}

	/**
	 * item的交换动画效果
	 * @param oldPosition
	 * @param newPosition
	 */
	private void animateReorder(final int oldPosition, final int newPosition) {
		boolean isForward = newPosition > oldPosition;
		List<Animator> resultList = new LinkedList<Animator>();
		if (isForward) {
			for (int pos = oldPosition; pos < newPosition; pos++) {
				View view = getChildAt(pos - getFirstVisiblePosition());
				if (null == view) {
					break;
				}
				
				if ((pos + 1) % mNumColumns == 0) {
					resultList.add(createTranslationAnimations(view,
							- view.getWidth() * (mNumColumns - 1), 0,
							view.getHeight(), 0));
				} else {
					resultList.add(createTranslationAnimations(view,
							view.getWidth(), 0, 0, 0));
				}
			}
		} else {
			for (int pos = oldPosition; pos > newPosition; pos--) {
				View view = getChildAt(pos - getFirstVisiblePosition());
				if (null == view) {
					break;
				}
				if ((pos + mNumColumns) % mNumColumns == 0) {
					resultList.add(createTranslationAnimations(view,
							view.getWidth() * (mNumColumns - 1), 0,
							-view.getHeight(), 0));
				} else {
					resultList.add(createTranslationAnimations(view,
							-view.getWidth(), 0, 0, 0));
				}
			}
		}

		AnimatorSet resultSet = new AnimatorSet();
		resultSet.playTogether(resultList);
		resultSet.setDuration(300);
		resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
		resultSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mAnimationEnd = false;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimationEnd = true;
				if (inMergeStatus && null != mOnItemDragListener) {
					inMergeStatus = false;

					if (oldPosition < mergePosition) {
						mergePosition -= 1;
					}
					
					if ( mOnItemDragListener.isFolder(mergePosition - 3) ) {
						mOnItemDragListener.onItemMoveToFolder(mDragPosition-3, mergePosition-3);
					} else {
						mOnItemDragListener.onItemMergeToFolder(mDragPosition-3, mergePosition-3);	
					}
				}
			}
		});
		resultSet.start();
	}
	
	/**
	 * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
	 */
	private void onStopDrag() {
		mDragAdapter.setHideItem(-1);
		removeDragImage();
		
		if (isScaleDragView && inMergeStatus) {
			isScaleDragView = false;
			mergeItem();
		}
		
		View view = getChildAt(mDragPosition - getFirstVisiblePosition());
		if(view != null){
			view.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 合并item成文件夹
	 */
	private void mergeItem() {
		//获取我们手指移动到的那个item的position
		final int tempPosition = getCount() -1;
		mergePosition = pointToPosition(moveX, moveY);
		//假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
		if (mergePosition > 2 && mergePosition != mDragPosition && mergePosition != AdapterView.INVALID_POSITION && mAnimationEnd) {
			mOnItemDragListener.onSwapItem(mDragPosition - 3, tempPosition - 3);
			mDragAdapter.setHideItem(tempPosition - 3);
			final ViewTreeObserver observer = getViewTreeObserver();
			observer.addOnPreDrawListener(new OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					observer.removeOnPreDrawListener(this);
					animateReorder(mDragPosition, tempPosition);
					mDragPosition = tempPosition;
					
					View view = getChildAt(tempPosition);
					if(view != null){
						view.setVisibility(View.GONE);
					}
					return true;
				}
			} );
		}
	}
	
	/**
	 * 设置拖拽事件监听
	 * @param onItemDragListener
	 */
	public void setOnItemDragListener(OnItemDragListener onItemDragListener) {
		this.mOnItemDragListener = onItemDragListener;
	}
	
	/**
	 * 拖拽事件
	 * @author xuhongwei
	 */
	public interface OnItemDragListener {
		//交换item
		public void onSwapItem(int oldPosition, int newPosition);
		// 判断指定位置是否是文件夹
		public boolean isFolder(int position);
		// 合并两个item,形成文件夹,数据修改回掉
		public void onItemMergeToFolder(int from, int to);
		// 移动item到文件夹,数据修改回掉
		public void onItemMoveToFolder(int from, int to);
	}
	
}
