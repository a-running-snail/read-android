package com.jingdong.app.reader.reading;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.jingdong.app.reader.activity.BookPageViewActivity.BookPagerAdapter;

/**
 * 阅读区视图容器
 */
public class ReadViewPager extends ViewPager {

	private static final int MOVE_LIMITATION = 100;// 触发移动的像素距离
	private float lastMotionX; // 手指触碰屏幕的最后一次x坐标
	private float prevMoveX; // 手指滑动的上一次x坐标
	private boolean pagingEnabled = true;
	private boolean isGotoNext = false; // 是否前往下一页
	private boolean isGotoPrev = false; // 是否前往上一页
	private BookPagerAdapter bookPageAdapter;// 数据适配器

	public ReadViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 触摸事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (this.pagingEnabled) {
			int action = event.getAction();
			float x = event.getX();
			switch (action) {
			case MotionEvent.ACTION_DOWN:// 手指按下
				// 重置变量
				lastMotionX = x;
				prevMoveX = x;
				// 手指刚按下不需要翻页
				isGotoNext = false;
				isGotoPrev = false;
				break;
			case MotionEvent.ACTION_MOVE:// 手指按下后移动
				// 判断是左滑还是右滑
				if (prevMoveX - x > 0) {
					isGotoNext = true;
					isGotoPrev = false;
				} else {
					isGotoPrev = true;
					isGotoNext = false;
				}
				prevMoveX = x;
				break;
			case MotionEvent.ACTION_UP:// 手指按下，移动后，手指抬起
				if (isGotoNext == isGotoPrev) {
					break;
				}
				if (Math.abs(x - lastMotionX) > MOVE_LIMITATION && !bookPageAdapter.isFirstOrFinishPage()) {
					if (x > lastMotionX) {
						bookPageAdapter.goToPrev(true);// 前一页
					} else {
						bookPageAdapter.goToNext(true);// 后一页
					}
					return true;
				}
				break;
			default:
				break;
			}
			return super.onTouchEvent(event);
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (this.pagingEnabled) {
			int action = event.getAction();
			float x = event.getX();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				lastMotionX = x;
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				break;
			default:
				break;
			}
			return super.onInterceptTouchEvent(event);
		}
		return false;
	}

	public void setPagingEnabled(boolean enablePage) {
		this.pagingEnabled = enablePage;
	}

	public boolean isPagingEnabled() {
		return pagingEnabled;
	}

	@Override
	public void setAdapter(PagerAdapter adapter) {
		super.setAdapter(adapter);
		bookPageAdapter = (BookPagerAdapter) adapter;
	}

}
