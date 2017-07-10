package com.jingdong.app.reader.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

public class RightDrawableTextView extends TextView {
	private Drawable mRightDrawable;

	public RightDrawableTextView(Context context) {
		super(context);
	}

	public RightDrawableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RightDrawableTextView(Context context, AttributeSet attrs, int style) {
		super(context, attrs, style);
	}

	@Override
	// Overriden to work only with a right drawable.
	public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {
		if (right == null) {
			mRightDrawable = null;
			return;
		}
		right.setBounds(0, 0, right.getIntrinsicWidth(), right.getIntrinsicHeight());
		mRightDrawable = right;
	}

	@Override
	public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
		if (right == 0) {
			mRightDrawable = null;
			return;
		}
		Drawable drawable = getResources().getDrawable(right);
		setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// transform the canvas so we can draw both image and text at center.
		if (mRightDrawable == null) {
			super.onDraw(canvas);
		} else {
			canvas.save();
			canvas.translate(0, Math.abs(mRightDrawable.getIntrinsicHeight() - getHeight()) / 2);
			super.onDraw(canvas);
			canvas.restore();
			canvas.save();
			int widthOfText = (int) getPaint().measureText(getText().toString());
			int right = widthOfText + getCompoundDrawablePadding();
			canvas.translate(right, (getHeight() - mRightDrawable.getIntrinsicHeight()) / 2);
			mRightDrawable.draw(canvas);
			canvas.restore();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mRightDrawable != null) {
			int height = getMeasuredHeight(), width = getMeasuredWidth();
			height = Math.max(height, mRightDrawable.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom());
			width = Math.max(width, mRightDrawable.getIntrinsicWidth() + getPaddingLeft() + getPaddingRight()
					+ getCompoundDrawablePadding());
			setMeasuredDimension(width, height);
		}
	}

}
