package com.jingdong.app.reader.view.bookshelf;

import com.jingdong.app.reader.R;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;

public class BookShelfEditTopBar extends PopupWindow implements OnClickListener{
	/** 取消按钮 */
	private TextView mCancleBtn = null;
	/** 全选按钮 */
	private TextView mSelectAllBtn = null;
	/** 顶部标题 */
	private TextView mEditTipTitle = null;
	private OnTopBarClickListener mOnTopBarClickListener = null;
	
	public BookShelfEditTopBar(Context context, DisplayMetrics mDisplayMetrics) {
		super(context);
		initView(context, mDisplayMetrics);
	}
	
	/**
	 * 初始化
	 * @param context
	 * @param dm
	 */
	private void initView(Context context, DisplayMetrics dm) {
		View view = LayoutInflater.from(context).inflate(R.layout.bookshelf_edittopbar, null);
		//中间标题
        mEditTipTitle = (TextView)view.findViewById(R.id.mEditTipTitle);
        //完成按钮
		mCancleBtn = (TextView)view.findViewById(R.id.mCancleBtn);
		//全选按钮
		mSelectAllBtn = (TextView)view.findViewById(R.id.mSelectAllBtn);
		mCancleBtn.setOnClickListener(this);
		mSelectAllBtn.setOnClickListener(this);
		setContentView(view);
		setWidth(dm.widthPixels);
		setHeight((int)(44*dm.density));
		//白色背景
		ColorDrawable dw = new ColorDrawable(0x00000000);  
        this.setBackgroundDrawable(dw);
        setAnimationStyle(R.style.BookShelfTopBarAnimation);
	}
	
	/** 更新顶部标题文本 */
	public void updateTitle(String title) {
		mEditTipTitle.setText(title);
	}
	
	/** 更新顶部标题文本 */
	public void selectAll(boolean enable) {
		if(enable) {
			mSelectAllBtn.setText("全不选");
		}else {
			mSelectAllBtn.setText("全选");
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mCancleBtn://完成
			mOnTopBarClickListener.clickCancleBtn();
			break;
		case R.id.mSelectAllBtn://全选
			mOnTopBarClickListener.clickSelectAllBtn();
			break;
		default:
			break;
		}
	}
	
	/** 
	 * 设置按钮事件监听 
	 */
	public void setOnTopBarClickListener(OnTopBarClickListener onTopBarClickListener) {
		this.mOnTopBarClickListener = onTopBarClickListener;
	}
	
	/**
	 * 点击事件接口
	 * @author mowen
	 *
	 */
	public interface OnTopBarClickListener {
		/** 点击取消按钮 */
		public void clickCancleBtn();
		/** 点击全选按钮 */
		public void clickSelectAllBtn();
	}
	
}
