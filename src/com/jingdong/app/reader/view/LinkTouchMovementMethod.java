package com.jingdong.app.reader.view;


import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import com.jingdong.app.reader.util.IMZBookClickableSpan;

public class LinkTouchMovementMethod extends LinkMovementMethod {

	private static LinkTouchMovementMethod mzInstance;

	public static LinkTouchMovementMethod getInstance() {
		if (mzInstance == null)
			mzInstance = new LinkTouchMovementMethod();

		return mzInstance;
	}
	
	private LinkTouchMovementMethod() {
	}
	
	@Override
	public boolean onTouchEvent(TextView widget, Spannable buffer,
			MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_CANCEL) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();

			x += widget.getScrollX();
			y += widget.getScrollY();

			Layout layout = widget.getLayout();
			int line = layout.getLineForVertical(y);
			int off = layout.getOffsetForHorizontal(line, x);

			int start = 0, end = 0;
			ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
			
			if (link.length > 0) {
				if (action == MotionEvent.ACTION_UP) {
					link[0].onClick(widget);
				}
				if (link[0] instanceof IMZBookClickableSpan) {
					start = ((IMZBookClickableSpan)link[0]).getStart();
					end = ((IMZBookClickableSpan)link[0]).getEnd();
				}
				
				if (widget instanceof PartClickableTextView) {
					((PartClickableTextView) widget).setLinkHit(true);
				}
			}
			
			if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
				buffer.setSpan(new BackgroundColorSpan(0x00000000), 0, buffer.length(), 
						SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
			} else if (action == MotionEvent.ACTION_DOWN) {
				buffer.setSpan(new BackgroundColorSpan(0xffe1e1e1), start, end, 
						SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			return true;
		}
		return super.onTouchEvent(widget, buffer, event);
	}
	
}
