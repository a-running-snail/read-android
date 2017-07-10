package com.android.mzbook.sortview.optimized;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;

/**
 * 书架拖动布局
 * @author WANGGUODONG
 *
 */
public class DragGridLayout extends HeaderGridView {

	// 正在拖拽的position
	private int mDragPosition;
	private int holdPosition;
	private int dropPosition;
	private int startPosition;

	// 刚开始拖拽的item对应的View
	private View mStartDragItemView = null;
	// 用于拖拽的镜像
	private DragItemView mDragImageView;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mWindowLayoutParams;
	// 拖拽的item对应的Bitmap
	private Bitmap mDragBitmap;
	// 按下的点到所在item的上边缘的距离
	private int mPoint2ItemTop;
	// 按下的点到所在item的左边缘的距离
	private int mPoint2ItemLeft;
	// DragGridView距离屏幕顶部的偏移量
	private int mOffset2Top;
	// DragGridView距离屏幕左边的偏移量
	private int mOffset2Left;
	// 状态栏的高度
	private int mStatusHeight;
	// DragGridView自动向下滚动的边界值
	private int mDownScrollBorder;
	// DragGridView自动向上滚动的边界值
	private int mUpScrollBorder;
	// DragGridView自动滚动的速度
	private static final int speed = 20;
	// item发生变化回调的接口
	private OnItemDragAndDropListener onItemDragAndDropListener;

	private boolean isDrag = false;
	/**
	 * 长按触发时长
	 */
	private long dragResponseMS = 1000;
	private int moveX;
	private int moveY;
	private int moveRawX;
	private int moveRawY;
	private int mDownX;
	private int mDownY;
	private int nColumns = 3;
	private boolean inMovingAnimation = false;
	private boolean inMergeStatus = false;
	private Handler mHandler = new Handler();
	private String LastAnimationID;
	private Runnable mDelayedSwapRunnable = null;
	private Runnable mDelayedMergeRunnable = null;
	private static final int NOT_DEFINED_VALUE = -1;
	private static final int INVALID_POSITION = NOT_DEFINED_VALUE;
	private static final int TOUCH_STATE_RESTING = 0;
	private static final int TOUCH_STATE_CLICK = 1;
	private static final int TOUCH_STATE_LONG_CLICK = 2;
	private int mTouchState = TOUCH_STATE_RESTING;
	private Context   mContext                    = null;
	private boolean isDragAndDropEnable = true;
	private boolean isDragAndDropMergeEnable = true;
	
	private boolean isScaleDragView = false;// 是否缩放了拖动view
	public boolean inDelBookView=false;
    private boolean   isDragOutSupport = false;
    
	long moveStartTime =0;
	long moveEndTime =0;

	/**
	 * 构造初始化
	 * @param context
	 */
	public DragGridLayout(Context context) {
		this(context, null);
	}

	/**
	 * 构造初始化
	 * @param context
	 * @param attrs
	 */
	public DragGridLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * 初始化
	 * @param context  上下文
	 * @param attrs 属性
	 * @param defStyle 
	 */
	public DragGridLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mContext =context;
	}

	/**
	 * 拖拽初始化准备操作
	 */
	private void initDragAndDropState() {
		if (isDragAndDropEnable) {
			isDrag = true;
			//隐藏被Drag的子项
			mStartDragItemView.setVisibility(View.INVISIBLE);
			//创建镜像
			createDragImage(mDragBitmap, mDownX, mDownY);
		} else {
			isDrag = false;
		}
	}

	/**
	 * 是否点击在GridView的item上面
	 * @param dragView
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isTouchInItem(View dragView, int x, int y) {
		if (dragView == null) {
			return false;
		}
		int leftOffset = dragView.getLeft();
		int topOffset = dragView.getTop();
		if (x < leftOffset || x > leftOffset + dragView.getWidth()) {
			return false;
		}
		if (y < topOffset || y > topOffset + dragView.getHeight()) {
			return false;
		}
		return true;
	}

	/**
	 * 销毁滑动的view
	 */
	private void destroyDragImageView() {
		
		MZLog.d("wangguodong", "###@@@@@@@@@destroyDragImageView");
		if (mDragImageView != null) {
			MZLog.d("wangguodong", "111111###@@@@@@@@@destroyDragImageView");	
			mDragImageView.destroyDragImageView();
			mDragImageView.setImageBackground(null);
			mDragImageView = null;
		}
	}

	/**
	 * 创建拖动的itemview
	 * @param bitmap
	 * @param downX
	 * @param downY
	 */
	private void createDragImage(Bitmap bitmap, int downX, int downY) {
		int params_x = downX - mPoint2ItemLeft + mOffset2Left;
		int params_y = downY - mPoint2ItemTop + mOffset2Top;// - mStatusHeight;
		int view_width = bitmap.getWidth();
		int view_height = bitmap.getHeight();
		mDragImageView = new DragItemView(getContext(), params_x, params_y,view_width, view_height);
		mDragImageView.setImageBackground(bitmap);
	}

	// 停止拖拽将之前隐藏的item显示出来，并将镜像移除
	private void onStopDrag(int downX, int downY,boolean tempFlag) {

		removeCallbacks(mDelayedSwapRunnable);
		removeCallbacks(mDelayedMergeRunnable);
		
		MZLog.d("wangguodong", "inDelView:=="+inDelBookView);
		// 处理合并
		dropPosition = pointToPosition(downX, downY);
		if (!tempFlag&&dropPosition != mDragPosition && isPositionAvailable(dropPosition)
				&& isPositionAvailable(mDragPosition)) {

			if (inMergeStatus && onItemDragAndDropListener != null) {

				if (!onItemDragAndDropListener.isFolder(mDragPosition)
						&& !onItemDragAndDropListener.isFolder(dropPosition))
					onItemDragAndDropListener.onItemMergeToFolder(
							mDragPosition, dropPosition);// 两个item 均为书籍
				else if (!onItemDragAndDropListener.isFolder(mDragPosition)
						&& onItemDragAndDropListener.isFolder(dropPosition)) {
					onItemDragAndDropListener.onItemMoveToFolder(mDragPosition,
							dropPosition);// 目标是文件夹 拖动的是书籍
				}
			}
		}
		View view = getChildAt(mDragPosition - getFirstVisiblePosition());
		if (view != null) {
			view.setVisibility(View.VISIBLE);
		}
		// 删除拖动view
		destroyDragImageView();
	}

	/**
	 * 交换item,并且控制item之间的显示与隐藏效果
	 * @param moveX
	 * @param moveY
	 */
	private void onSwapItem(int moveX, int moveY) {
		int tempPosition = pointToPosition(moveX, moveY);
		if (tempPosition != mDragPosition && isPositionAvailable(tempPosition)) {
	
			if (onItemDragAndDropListener != null) {
				onItemDragAndDropListener.onItemSwap(mDragPosition,
						tempPosition);
			}

			View childTempView = getChildAt(tempPosition - getFirstVisiblePosition());
			View childDragView = getChildAt(mDragPosition - getFirstVisiblePosition());
			
			if(childTempView!=null)
				childTempView.setVisibility(
					View.INVISIBLE);

			if(childDragView!=null)
				childDragView.setVisibility(View.VISIBLE);

			mDragPosition = tempPosition;

		}
	}

	public void startMovingAnimation(int position, int x, int y) {
		int TempPosition = position;
		if (TempPosition != mDragPosition && isPositionAvailable(TempPosition)) {
			startPosition = mDragPosition;
			Log.i("wangguodong", "TempPosition:" + TempPosition + "==mDragPosition:" + mDragPosition);
			int MoveNum = TempPosition - mDragPosition;

			if (MoveNum != 0) {
				int itemMoveNum = Math.abs(MoveNum);
				float Xoffset, Yoffset;
				for (int i = 0; i < itemMoveNum; i++) {
					if (MoveNum > 0) {
						holdPosition = mDragPosition + 1;
						Xoffset = (mDragPosition / nColumns == holdPosition
								/ nColumns) ? (-1) : (nColumns - 1);
						Yoffset = (mDragPosition / nColumns == holdPosition
								/ nColumns) ? 0 : (-1);
					} else {
						holdPosition = mDragPosition - 1;
						Xoffset = (mDragPosition / nColumns == holdPosition
								/ nColumns) ? 1 : (-(nColumns - 1));
						Yoffset = (mDragPosition / nColumns == holdPosition
								/ nColumns) ? 0 : 1;
					}					

					ViewGroup moveView = (ViewGroup) getChildAt(holdPosition);
					Animation animation = getMoveAnimation(Xoffset, Yoffset);
					Log.i("wangguodong", "holdPosition:#######" + holdPosition + " moveView is null: " + (moveView!=null) + "Xoffset, Yoffset:" +Xoffset+" "+ Yoffset );
					animation.setFillAfter(true);
					if (moveView!=null)
						moveView.startAnimation(animation);
					mDragPosition = holdPosition;

					LastAnimationID = animation.toString();

					animation
							.setAnimationListener(new Animation.AnimationListener() {

								@Override
								public void onAnimationStart(Animation animation) {
									// TODO Auto-generated method stub
									inMovingAnimation = true;// 是否正在执行动画
								}

								@Override
								public void onAnimationRepeat(
										Animation animation) {
									// TODO Auto-generated method stub
								}

								@Override
								public void onAnimationEnd(Animation animation) {

									String animaionID = animation.toString();
									if (animaionID
											.equalsIgnoreCase(LastAnimationID)) {

										inMovingAnimation = false;

										if (onItemDragAndDropListener != null) {

											onItemDragAndDropListener
													.onItemSwap(startPosition,
															holdPosition);

											Log.i("wangguodong",
													"startPositionL:"
															+ startPosition
															+ "==mDragPosition:"
															+ mDragPosition);

											startPosition = holdPosition;
										}
									}
								}
							});
				}
			}
		}
	}

	public Animation getMoveAnimation(float x, float y) {
		TranslateAnimation go = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, x,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, y);
		go.setFillAfter(true);
		go.setDuration(300);
		return go;
	}

	public void updateDragView(int moveX, int moveY) {
		// 更新镜像的位置
		int x = moveX - mPoint2ItemLeft + mOffset2Left;
		int y = moveY - mPoint2ItemTop + mOffset2Top;
		mDragImageView.updatePosition(x, y);

	}
	
	// 获取状态栏的高度
	public static int getStatusHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier(
				"status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

    // 判断书籍中心点是否拖动到删除view上
    protected boolean isInDelBookView(final int x, final int y,
            final int delViewWidth, final int delViewHeight,
            final int screentHeight) {

        int scroll_y=getScrollY();
        Rect mRect = new Rect(0, (screentHeight - delViewHeight) / 2, delViewWidth, (screentHeight - delViewHeight) / 2 + delViewHeight);
        if (mRect.contains(x, y -scroll_y+getStatusHeight(mContext))) {
            return true;
        } else {
            return false;
        }
    }

	

	/**
	 * 拖动item，实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
	 * @param moveX
	 * @param moveY
	 */
	private void onDragItem(int moveX, int moveY) {

		final int x = moveX;
		final int y = moveY;

		boolean isNeedChange = false;// 是否需要缩放动画
		boolean isDragFolder = false;// 是否拖拽文件夹
		boolean isTargetFolder = false;// 目标是否文件夹
		int tempPosition = AdapterView.INVALID_POSITION;

		removeCallbacks(mDelayedSwapRunnable);
		removeCallbacks(mDelayedMergeRunnable);

		tempPosition = pointToPosition(x, y);
		
        if (isDragOutSupport && y < -100 && !isPositionAvailable(tempPosition)&&onItemDragAndDropListener != null) {

        		onItemDragAndDropListener.onDragOutLayout(mDragPosition);
            MZLog.d("wangguodong", "移动到界面以外");
        		// 删除拖动view
        		destroyDragImageView();
            return;
        }

        if (onItemDragAndDropListener != null) {

            if (isInDelBookView(x, y, ScreenUtils.dip2px(mContext, DelBookView.DELETE_VIEW_WIDTH_OR_HEIGHT), ScreenUtils.dip2px(mContext, DelBookView.DELETE_VIEW_WIDTH_OR_HEIGHT), (int) ScreenUtils.getHeightJust(mContext))) {
                MZLog.d("wangguodong", "书籍拖动到删除按钮的区域");
                onItemDragAndDropListener.onDragToDelBookView();
                inDelBookView=true;
             
                
            } else {
                MZLog.d("wangguodong", "书籍离开删除按钮的区域");
                onItemDragAndDropListener.onDragOutDelBookView();
                inDelBookView=false;
            }
            
        }
		

		if ((tempPosition != mDragPosition && isPositionAvailable(tempPosition)) ) {

			if (onItemDragAndDropListener != null) {
				isDragFolder = onItemDragAndDropListener
						.isFolder(mDragPosition);
				isTargetFolder = onItemDragAndDropListener
						.isFolder(tempPosition);
			}
			Log.i("wangguodong", "isDragFolder:" + isDragFolder
					+ " isTargetFolder:" + isTargetFolder);

			if (isDragFolder||!isDragAndDropMergeEnable)// 拖动文件夹 不可以缩放
				isNeedChange = false;
			else {
				// 拖动的不是文件夹 判断是否需要缩放
				isNeedChange = isNeedChangeToFolder(tempPosition, x, y);
			}

			Log.i("wangguodong", "isNeedChange:" + isNeedChange + "");

			if (!isNeedChange) {
				// 还原item 显示状态

				inMergeStatus = false;

				if (isScaleDragView) {
					isScaleDragView = false;
					if (mDragImageView != null&&isDragAndDropMergeEnable)
						mDragImageView.scaleToNormalView(x + mOffset2Left, y
								+ mOffset2Top);
				}
			}

			if (isNeedChange) {

				mDelayedMergeRunnable = new Runnable() {

					@Override
					public void run() {

						if (mDragImageView != null) {

							inMergeStatus = true;// 处在合并状态

							if (!isScaleDragView&&isDragAndDropMergeEnable) {
								mDragImageView.scaleToMiniView(
										x + mOffset2Left, y + mOffset2Top);
								Log.i("wangguodong", "缩放view");
								isScaleDragView = true;
							}
						}
						Log.i("wangguodong", "变成文件夹");

					}
				};

				postDelayed(mDelayedMergeRunnable, 0);
			} else {

				mDelayedSwapRunnable = new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						onSwapItem(x, y);
						// GridView自动滚动
						mHandler.post(mScrollRunnable);
					}
				};

				if (isDragFolder)
					postDelayed(mDelayedSwapRunnable, 0);// 文件夹不用合并 只需要排序 直接执行
				else {
					postDelayed(mDelayedSwapRunnable, 100);
				}
			}

		}
		else if (moveY < -20 || moveY-getHeight() > 20) {
			mHandler.post(mScrollRunnable);
		}


	}

	protected boolean isNeedChangeToFolder(int draggedChild, int x, int y) {

		// 相对坐标 相当于控件的左上角
		Rect mRect = new Rect();
		// getChildAt(draggedChild).getHitRect(mRect);
		if (mDragImageView != null) { //移动的view
			int[] position = new int[2];
			getChildAt(draggedChild - getFirstVisiblePosition()).getLocationInWindow(position);
			getChildAt(draggedChild - getFirstVisiblePosition()).getHitRect(mRect);
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
			MZLog.d("performance", "center_x:"+center_x+" center_y:"+center_y+" drag_center_x:"+drag_center_x+" drag_center_y:"+drag_center_y);
			MZLog.d("performance", "distance:"+distance+" radius:"+radius);
			
			if (distance < radius) {
				// 进入目标中心区域
				return true;
			}
			return false;
		}
		else {
			getChildAt(draggedChild - getFirstVisiblePosition()).getHitRect(mRect);
			int left_position = mRect.left;
			int top_position = mRect.top;
			int width = mRect.width();
			int height = mRect.height();
			float center_x = left_position + width / 2.0f;
			float center_y = top_position + height / 2.0f;
			float radius = Math.min(width / 3, height / 3);
			// 处理滑动view 的中心点
			int pointx = x;
			int pointy = y;
			float distance = (float) Math.sqrt((Math.abs(pointx - center_x)
					* Math.abs(pointx - center_x) + Math.abs(pointy - center_y)
					* Math.abs(pointy - center_y)));
	
			if (distance < radius) {
				// 进入目标中心区域
				return true;
			}
			return false;
		}
	}

	/**
	 * 位置是否在正常范围内
	 * @param postion
	 * @return
	 */
	public boolean isPositionAvailable(int postion) {
		if (postion <0) {
			return false;
		}
		return true;
	}

	// 长按处理
	private Runnable mLongClickRunnable = null;

	private void startTouch() {
		//长按操作检查
		startLongPressCheck();
		mTouchState = TOUCH_STATE_CLICK;
	}

	/**
	 * 检查长按操作
	 */
	private void startLongPressCheck() {

		if (mLongClickRunnable == null) {
			mLongClickRunnable = new Runnable() {
				public void run() {
					if (mTouchState == TOUCH_STATE_CLICK) {
						//获取发生事件的位置
						int index = pointToPosition(mDownX, mDownY);
						if (index == startPosition) {
							initDragAndDropState();
							//长按
							mTouchState = TOUCH_STATE_LONG_CLICK;
							//调用长按事件相关的处理
							longClickChild(index);
						}
					}
				}
			};
		}
		//特定时长后，还处于按下状态，则为长按
		mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
	}

	private void clickChildAt() {

		if (isPositionAvailable(mDragPosition)
				&& mDragPosition == startPosition) {
			MZLog.d("wangguodong", "mDragPosition=" + mDragPosition
					+ "getFirstVisiblePosition=" + getFirstVisiblePosition()
					+ "===getLastVisiblePosition:" + getLastVisiblePosition());

			View itemView = getChildAt(mDragPosition
					- getFirstVisiblePosition());
			
			if (null == itemView)
				return;

			int position = mDragPosition;
			long id = getAdapter().getItemId(position);
			performItemClick(itemView, position, id);
		}
	}

	private void longClickChild(int index) {
		if (isPositionAvailable(mDragPosition) && mDragPosition == startPosition) {
			View itemView = getChildAt(mDragPosition- getFirstVisiblePosition());

			long id = getAdapter().getItemId(index);
			OnItemLongClickListener listener = getOnItemLongClickListener();
			if (null == itemView)
				return;
			if (listener != null) {
				//触发长按事件
				listener.onItemLongClick(this, itemView, index, id);
			}
		}
	}


	
	/**
	 * 触摸事件处理
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		//手指按下
		case MotionEvent.ACTION_DOWN:

			MZLog.d("wangguodong", "ACTION_DOWN");
			//坐标位置
			mDownX = (int) ev.getX();//getX()是表示Widget相对于自身左上角的x坐标
			mDownY = (int) ev.getY();
			
			//根据按下的X,Y坐标获取所点击item的position
			mDragPosition = pointToPosition(mDownX, mDownY);
			startPosition = dropPosition = mDragPosition;

			Log.i("wangguodong", "mDragPosition" + mDragPosition);

			//位置是否在正常范围内
			if (!isPositionAvailable(mDragPosition)) {
				//不在正常范围内，不拦截
				return super.dispatchTouchEvent(ev);
			}
			// 使用Handler延迟dragResponseMS执行mLongClickRunnable
			// mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
			moveStartTime=System.currentTimeMillis();
	
			startTouch();

			// 根据position获取该item所对应的View
			mStartDragItemView = getChildAt(mDragPosition - getFirstVisiblePosition());
			
			System.out.println("TTTTTTT==mDownX=" + mDownX +"==mDownY=" +mDownY+"==left="+ mStartDragItemView.getLeft() + "===top=" + mStartDragItemView.getTop());
	
			// 为创建拖拽item镜像做准备
			mPoint2ItemTop = mDownY /*事件坐标位置，垂直坐标*/ - mStartDragItemView.getTop();
			mPoint2ItemLeft = mDownX /*事件坐标位置，水平坐标*/ - mStartDragItemView.getLeft();
			mOffset2Top = (int) (ev.getRawY() - mDownY);
			mOffset2Left = (int) (ev.getRawX()/*getRawX()是表示相对于屏幕左上角的x坐标值*/ - mDownX);
			
			System.out.println("TTTTTTT===mPoint2ItemTop=" + mPoint2ItemTop + "==mPoint2ItemLeft=" + mPoint2ItemLeft + "===mOffset2Top=" + mOffset2Top +"===mOffset2Left=" + mOffset2Left);

			// 获取DragGridView自动向上滚动的偏移量，小于这个值，DragGridView向下滚动
			mDownScrollBorder = getHeight() / 4;
			// 获取DragGridView自动向下滚动的偏移量，大于这个值，DragGridView向上滚动
			mUpScrollBorder = getHeight() * 3 / 4;

			// 开启mDragItemView绘图缓存
			mStartDragItemView.setDrawingCacheEnabled(true);
			Bitmap bitmap = mStartDragItemView.getDrawingCache();
			if(null != bitmap) {
				mDragBitmap = Bitmap.createBitmap(bitmap);	
			}
			// 释放绘图缓存，避免出现重复的镜像
			mStartDragItemView.destroyDrawingCache();

//			ImageView touming = (ImageView)mStartDragItemView.findViewById(R.id.touming);
//			if (null != touming) {
//				touming.setVisibility(View.VISIBLE);
//			}
			break;
		//手指移动
		case MotionEvent.ACTION_MOVE:
			moveX = (int) ev.getX();
			moveY = (int) ev.getY();
			
			moveRawX = (int) ev.getRawX();
			moveRawY = (int) ev.getRawY();
			
			moveEndTime=System.currentTimeMillis(); 
			
			MZLog.d("wangguodong", "ACTION_MOVE");
			if (!isTouchInItem(mStartDragItemView, moveX, moveY)) {
				mHandler.removeCallbacks(mLongClickRunnable);
			}
			
			if (isDrag && mDragImageView != null) {
				// 更新dragview 位置
				updateDragView(moveX, moveY);
				onDragItem(moveX, moveY);
			}
			
			break;
		//手指抬起
		case MotionEvent.ACTION_UP:
//			if (isPositionAvailable(mDragPosition) ){
//				View itemView = getChildAt(mDragPosition - getFirstVisiblePosition());
//				if (null != itemView) {
//					ImageView touming2 = (ImageView)itemView.findViewById(R.id.touming);
//					if (null != touming2) {
//						touming2.setVisibility(View.GONE);
//					}
//				}
//				
//			}
			
			int moveX1 = (int) ev.getX();
			int moveY1 = (int) ev.getY();
			
			MZLog.d("wangguodong", "ACTION_UP"+"   move time ="+(moveEndTime-moveStartTime));
			if (mTouchState == TOUCH_STATE_CLICK&&(moveEndTime-moveStartTime<200)&&(Math.abs(moveY1 - mDownY) < 5)) {
				clickChildAt();
			}
			
			moveEndTime=0;
			moveStartTime=0;
			
			mTouchState = TOUCH_STATE_RESTING;
			mHandler.removeCallbacks(mLongClickRunnable);
			mHandler.removeCallbacks(mScrollRunnable);
			
			boolean tempFlag =inDelBookView;
			
			if (null != onItemDragAndDropListener)
				onItemDragAndDropListener.onItemMoveEnd(mDragPosition);// 移动结束
			
			if (isDrag && mDragImageView != null) {
				MZLog.d("wangguodong", "=====!!!!!onStopDrag");
				onStopDrag(moveX1, moveY1,tempFlag);
				isDrag = false;
				isScaleDragView = false;// item 是否缩放标记重置
			}
			break;
		}
		return super.dispatchTouchEvent(ev);
	}


	public void setOnItemDragAndDropListener(OnItemDragAndDropListener onItemDragAndDropListener) {
		this.onItemDragAndDropListener = onItemDragAndDropListener;
	}

	public void setDragAndDropEnable(boolean state) {
		isDragAndDropEnable = state;
	}
	
	public void setDragAndDropMergeEnable (boolean state) {
		isDragAndDropMergeEnable = state;
	}

	// 设置响应拖拽的毫秒数，默认是1000毫秒
	public void setDragResponseMS(long dragResponseMS) {
		this.dragResponseMS = dragResponseMS;
	}

    // 是否支持拖动到布局以外的拖动操作 默认不支持
    public void setDragOutSupport(boolean state) {
        isDragOutSupport = state;
    }

    /**
	 * // 当moveY的值大于向上滚动的边界值，触发GridView自动向上滚动
	// 当moveY的值小于向下滚动的边界值，触犯GridView自动向下滚动否则不进行滚动
	 */
	private Runnable mScrollRunnable = new Runnable() {

		@Override
		public void run() {
			int scrollY;
			if (moveY > mUpScrollBorder) {
				scrollY = speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			} else if (moveY < mDownScrollBorder) {
				scrollY = -speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			} else {
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
			}
			// 当拖动到达GridView向上或者向下滚动的偏移量的时候，item未移动，但是DragGridView在自动的滚动
			// 需要交换item
			onSwapItem(moveX, moveY);
			// 开始滚动
			smoothScrollBy(scrollY, 10);
		}
	};

    public interface OnItemDragAndDropListener {

		// 判断指定位置是否是文件夹
		public boolean isFolder(int position);
		// 交换两个item，数据修改回掉
		public void onItemSwap(int from, int to);
		// 合并两个item,形成文件夹,数据修改回掉
		public void onItemMergeToFolder(int from, int to);
		// 移动item到文件夹,数据修改回掉
		public void onItemMoveToFolder(int from, int to);
		// 移出文件夹
		void onDragOutLayout(int from);
		// 移动到删除按钮区域
		void onDragToDelBookView();
		// 移动到删除按钮以外
		void onDragOutDelBookView();
		// 移动结束
		public void onItemMoveEnd(int from);
		// 纠正item位置
		public int getRightPosition(int position);
	}
}
