package com.jingdong.app.reader.util;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class MZBookClickableSpan extends ClickableSpan implements IMZBookClickableSpan {
	
	private ClickCallback callback;
	private int color;
	private int start;
	private int end;
	
	interface ClickCallback {
		public void onClick(View widget);
	}

	public MZBookClickableSpan(ClickCallback callback, int color, int start, int end) {
		this.callback = callback;
		this.color = color;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public int getStart() {
		return start;
	}
	
	@Override
	public int getEnd() {
		return end;
	}
	
	@Override
	public void onClick(View widget) {
		callback.onClick(widget);
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);
		ds.setUnderlineText(false);
		ds.setColor(color);
	}

}
