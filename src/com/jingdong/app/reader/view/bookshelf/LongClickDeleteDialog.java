package com.jingdong.app.reader.view.bookshelf;

import com.jingdong.app.reader.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * 长按删除对话框
 * @author xuhongwei
 *
 */
public class LongClickDeleteDialog extends Dialog implements View.OnClickListener{
	private Context mContext;
	private OnDeleteClickListener mOnDeleteClickListener;

	public LongClickDeleteDialog(Context context) {
		super(context, R.style.CustomDialog);
		mContext = context;
		initView();
	}
	
	private void initView() {
		RelativeLayout layout = new RelativeLayout(mContext);
		TextView view = new TextView(mContext);
		view.setText("删除书籍");
		view.setTextColor(Color.BLACK);
		view.setTextSize(16);
		view.setPadding(50, 30, 0, 30);
		view.setBackgroundResource(R.drawable.bookshelf_longclik_delete_dialog_shape);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layout.addView(view, params);
		layout.setOnClickListener(this);
		
		setContentView(layout);
	}
	
	@Override
	public void onClick(View v) {
		dismiss();
		if (null != mOnDeleteClickListener) {
			mOnDeleteClickListener.clickDelete();
		}
	}
	
	public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
		this.mOnDeleteClickListener = onDeleteClickListener;
	}
	
	public interface OnDeleteClickListener {
		public void clickDelete();
	}

}
