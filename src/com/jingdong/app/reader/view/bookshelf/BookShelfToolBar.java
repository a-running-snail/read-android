package com.jingdong.app.reader.view.bookshelf;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * 书架整理模式工具栏（含了头部和底部工具栏）
 * @author xuhongwei
 */
public class BookShelfToolBar extends View {
	/** 顶部工具栏 */
	private BookShelfEditTopBar mBookShelfEditTopBar = null;
	/** 底部工具栏 */
	private BookShelfEditBottomBar mBookShelfEditBottomBar = null;
	/** 状态栏高度 */
	private int mStatusHeight;
	private OnToolBarClickListener mOnToolBarClickListener = null;
	
	public BookShelfToolBar(Context context) {
		super(context);
		initView(context);
	}
	
	public BookShelfToolBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		initView(context);
	}
	
	public BookShelfToolBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}
	
	/**
	 * 初始化
	 * @param context
	 */
	private void initView(Context context) {
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		
		//状态栏高度
		mStatusHeight = getStatusHeight(context);
		mBookShelfEditTopBar = new BookShelfEditTopBar(context, dm);
		mBookShelfEditBottomBar = new BookShelfEditBottomBar(context, dm);
		
		//顶部工具栏点击事件
		mBookShelfEditTopBar.setOnTopBarClickListener(new BookShelfEditTopBar.OnTopBarClickListener() {
			@Override
			public void clickSelectAllBtn() {
				if (null != mOnToolBarClickListener) {
					mOnToolBarClickListener.clickSelectAllBtn();
				}
			}
			
			@Override
			public void clickCancleBtn() {
				hide();
				if (null != mOnToolBarClickListener) {
					mOnToolBarClickListener.clickCancleBtn();
				}
			}
		});
		
		//底部工具栏点击事件处理
		mBookShelfEditBottomBar.setOnBottomBarClickListener(new BookShelfEditBottomBar.OnBottomBarClickListener() {
			
			@Override
			public void clickMoveBtn() {
				if (null != mOnToolBarClickListener) {
					mOnToolBarClickListener.clickMoveBtn();
				}
			}
			
			@Override
			public void clickDeleteBtn() {
				if (null != mOnToolBarClickListener) {
					mOnToolBarClickListener.clickDeleteBtn();
				}
			}
		});
	}
	
	/** 更新顶部标题文本 */
	public void updateTitle(String title) {
		mBookShelfEditTopBar.updateTitle(title);
	}
	
	/** 更新全选按钮文本 */
	public void selectAll(boolean enable) {
		mBookShelfEditTopBar.selectAll(enable);
	}
	
	/** 
	 * 控制底部工具栏按钮的状态
	 * @param enable
	 */
	public void bottomButtonEnable(boolean enable) {
		mBookShelfEditBottomBar.bottomButtonEnable(enable);
	}
	
	/** 
	 * 显示整理模式状态栏
	 * @param parent
	 */
	public void show(View parent) {
		if (!mBookShelfEditTopBar.isShowing()) {
			//显示顶部工具栏
			mBookShelfEditTopBar.showAtLocation(parent, Gravity.LEFT|Gravity.TOP/*显示位置*/, 0, mStatusHeight);
		}
		if (!mBookShelfEditBottomBar.isShowing()) {
			//显示底部工具栏
			mBookShelfEditBottomBar.showAtLocation(parent, Gravity.LEFT|Gravity.BOTTOM/*显示位置*/, 0, 0);
		}
	}
	
	/**
	 * 隐藏整理模式状态栏
	 */
	public void hide() {
		//隐藏工具栏
		if (mBookShelfEditTopBar.isShowing()) {
			mBookShelfEditTopBar.selectAll(false);
			mBookShelfEditTopBar.dismiss();
		}
		if (mBookShelfEditBottomBar.isShowing()) {
			mBookShelfEditBottomBar.dismiss();
		}
	}

	/** 
	 * 设置按钮事件监听 
	 */
	public void setOnToolBarClickListener(OnToolBarClickListener onToolBarClickListener) {
		this.mOnToolBarClickListener = onToolBarClickListener;
	}
	
	/**
	 * 事件接口
	 */
	public interface OnToolBarClickListener {
		/** 点击取消按钮 */
		public void clickCancleBtn();
		/** 点击全选按钮 */
		public void clickSelectAllBtn();
		/** 点击删除按钮 */
		public void clickDeleteBtn();
		/** 点击移动按钮 */
		public void clickMoveBtn();
	}
	
	/**
	 * 获取状态栏的高度
	 * @param context
	 * @return
	 */
	private int getStatusHeight(Context context) {
        int statusHeight = 0;
        Rect localRect = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight){
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = context.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
        return statusHeight;
	}
	
}
