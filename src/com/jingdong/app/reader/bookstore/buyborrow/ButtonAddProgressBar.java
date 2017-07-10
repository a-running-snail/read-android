package com.jingdong.app.reader.bookstore.buyborrow;

import com.jingdong.app.reader.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class ButtonAddProgressBar extends RelativeLayout {
	private int progress;
	private Context mContext;
	
	public ButtonAddProgressBar(Context context) {
		super(context);
		initView(context);
	}
	
	public ButtonAddProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	public ButtonAddProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}
	
	public ButtonAddProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initView(context);
	}
	
	private void initView(Context context) {
		this.mContext = context;
		CustomTextView layout1 = new CustomTextView(context);
		CustomTextView layout2 = new CustomTextView(context);
		layout1.setBackgroundColor(context.getResources().getColor(R.color.red_main));
		layout2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_tryread_bg));
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addView(layout1, params);
		this.addView(layout2, params);
	}
	
	public void setProgress(int progress) {
		if(progress < 0) {
			progress = 0;
		}
		
		if(progress > 100) {
			progress = 100;
		}
		
		this.progress = progress;
		
		requestLayout();
	}
	
	public void updateText(String text) {
		this.invalidate();
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int width = getWidth();
		int height = getHeight();
		float pro = width/100;
		CustomTextView view = (CustomTextView)getChildAt(0);
		CustomTextView view2 = (CustomTextView)getChildAt(1);
		
		view.layout(0, 0, (int)(progress*pro), height);
		view2.layout((int)(progress*pro), 0, width, height);

		if(progress <= 0) {
			view.setText(0, width, "下载中...", Color.WHITE);
			view2.setText(-(int)(progress*pro), width, "下载中...", mContext.getResources().getColor(R.color.red_main));
		}else {
			view.setText(0, width, progress + "%", Color.WHITE);
			view2.setText(-(int)(progress*pro), width, progress + "%", mContext.getResources().getColor(R.color.red_main));
		}
	}
	
	
	public class CustomTextView extends View{
		private String text="";
		private int x = 0;
		private int width;
		private int color;

		public CustomTextView(Context context) {
			super(context);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			Paint mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(color);
			
			int height = getHeight();
	        mPaint.setTextSize((int)(16*UserBuyBorrowActivity.dm.density));
	        
	        Rect targetRect = new Rect(0, 0, width, height);
	        FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
	        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
	        mPaint.setTextAlign(Paint.Align.CENTER);  
	        int centerX = width/2;
			canvas.drawText(text, centerX + x, baseline, mPaint);
			
		}
		
		public void setText(int x, int width, String text, int color) {
			this.x = x;
			this.width = width;
			this.text = text;
			this.color = color;
			invalidate();
		}

	}

}
