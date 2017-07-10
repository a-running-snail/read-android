package com.jingdong.app.reader.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.LeadingMarginSpan;
import android.view.View;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.book.Book;

public class LeadingMarginClickableSpan  extends ClickableSpan implements LeadingMarginSpan, ParcelableSpan, IMZBookClickableSpan {
	private final int LEAD_MARGIN;
	private final Context context;
	private LeadingMarginSpan.Standard span;
	private boolean firstItem;
	private boolean clickable;
	private final Book book;
	private int start;
	private int end;

	public LeadingMarginClickableSpan(Context context, boolean firstItem, Book book, int start, int end, boolean clickable) {
		this.context=context;
		LEAD_MARGIN=(int) context.getResources().getDimension(R.dimen.first_line_indent);
		this.firstItem = firstItem;
		this.clickable = clickable;
		this.book = book;
		this.start = start;
		this.end = end;
		if (book.getTitle().startsWith(UiStaticMethod.LEFT_QUOTE))
			this.span = new LeadingMarginSpan.Standard(LEAD_MARGIN, 0);
		else
			this.span = new LeadingMarginSpan.Standard(0, 0);
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
		if (clickable) {
//			Intent intent = new Intent(context, BookInfoActivity.class);
//			intent.putExtra(BookInfoActivity.BookIdKey, (int) book.getBookId());
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			context.startActivity(intent);
//			
			Intent intent2 = new Intent(context,
					BookInfoNewUIActivity.class);
			intent2.putExtra("bookid",  book.getBookId());
			context.startActivity(intent2);
		}
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);
		if (clickable) {
			ds.setColor(context.getResources().getColor(R.color.text_main));
		} else {
			ds.setColor(context.getResources().getColor(R.color.text_sub));
		}
		ds.setUnderlineText(false);
	}

	@Override
	public int getLeadingMargin(boolean first) {
		if (firstItem)
			return span.getLeadingMargin(first);
		else
			return 0;
	}

	@Override
	public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom,
			CharSequence text, int start, int end, boolean first, Layout layout) {
		span.drawLeadingMargin(c, p, x, dir, top, baseline, bottom, text, start, end, first, layout);
	}

	@Override
	public int describeContents() {
		return span.describeContents();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		span.writeToParcel(dest, flags);
	}

	@Override
	public int getSpanTypeId() {
		return span.getSpanTypeId();
	}
}
