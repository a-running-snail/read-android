package com.jingdong.app.reader.view.bookshelf;

import com.jingdong.app.reader.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;

public class BookShelfEditBottomBar extends PopupWindow implements OnClickListener{
	/** 删除按钮 */
	private TextView mDeleteBtn = null;
	/** 移动按钮 */
	private TextView mMoveBtn = null;
	private OnBottomBarClickListener mOnBottomBarClickListener = null;
	
	public BookShelfEditBottomBar(Context context, DisplayMetrics mDisplayMetrics) {
		super(context);
		initView(context, mDisplayMetrics);
	}
	
	private void initView(Context context, DisplayMetrics dm) {
		View view = LayoutInflater.from(context).inflate(R.layout.bookshelf_editbottombar, null);
		mDeleteBtn = (TextView)view.findViewById(R.id.mDeleteBtn);
		mMoveBtn = (TextView)view.findViewById(R.id.mMoveBtn);
		mDeleteBtn.setOnClickListener(this);
		mMoveBtn.setOnClickListener(this);
		setContentView(view);
		setWidth(dm.widthPixels);
		setHeight((int)(52*dm.density));
		ColorDrawable dw = new ColorDrawable(0x00000000);  
        this.setBackgroundDrawable(dw);
        setAnimationStyle(R.style.BookShelfBottomBarAnimation);
	}
	
	/** 
	 * 控制底部工具栏按钮的状态
	 * @param enable
	 */
	public void bottomButtonEnable(boolean enable) {
		mDeleteBtn.setEnabled(enable);
		mMoveBtn.setEnabled(enable);
		if (enable) {
			mDeleteBtn.setTextColor(Color.rgb(0x00, 0x00, 0x00));
			mMoveBtn.setTextColor(Color.rgb(0x00, 0x00, 0x00));
		}else {
			mDeleteBtn.setTextColor(Color.rgb(0xcc, 0xcc, 0xcc));
			mMoveBtn.setTextColor(Color.rgb(0xcc, 0xcc, 0xcc));
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mDeleteBtn:
			mOnBottomBarClickListener.clickDeleteBtn();
			break;
		case R.id.mMoveBtn:
			mOnBottomBarClickListener.clickMoveBtn();
			break;

		default:
			break;
		}
	}
	
	/** 
	 * 设置按钮事件监听 
	 */
	public void setOnBottomBarClickListener(OnBottomBarClickListener onBottomBarClickListener) {
		this.mOnBottomBarClickListener = onBottomBarClickListener;
	}
	
	public interface OnBottomBarClickListener {
		/** 点击删除按钮 */
		public void clickDeleteBtn();
		/** 点击移动按钮 */
		public void clickMoveBtn();
	}

}
