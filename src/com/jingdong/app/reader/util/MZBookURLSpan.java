package com.jingdong.app.reader.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.View;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.net.URLText;

public class MZBookURLSpan extends URLSpan implements IMZBookClickableSpan {

	private static final String BOOK_PREFIX = "/books/more/";
	private Context context;
	private int start;
	private int end;
	
	public MZBookURLSpan(String url, Context context, int start, int end) {
		super(url);
		this.context = context;
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
		if (!TextUtils.isEmpty(getURL())) {
			if (getURL().startsWith(BOOK_PREFIX)) {
				long bookId = 0;
				int index = getURL().lastIndexOf("/");
				String bookTag = getURL().substring(index + 1);
				try {
					bookId = Long.parseLong(bookTag);
				} catch (Exception e) {
					bookId = 0;
					
				}
				if (bookId != 0) {
				
					Intent intent2 = new Intent(context,
							BookInfoNewUIActivity.class);
					intent2.putExtra("bookid", bookId);
					
					MZLog.d("wangguodong","========点击的书籍id为："+ bookId);
					
					context.startActivity(intent2);
					
					
					return;
				}
			}
			Intent intent = new Intent(context, WebViewActivity.class);
			intent.putExtra(WebViewActivity.UrlKey, getFormatUrl());
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
		}
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);
		ds.setUnderlineText(false);
		if (!TextUtils.isEmpty(getURL())) {
			ds.setColor(context.getResources().getColor(R.color.timeline_book_title));
		} else {
			ds.setColor(context.getResources().getColor(R.color.dark_grey_hl));
		}
	}

	private String getFormatUrl() {
		String url = getURL(), result;
		if (url.startsWith("/")) {
			result = URLText.baseUrl + url;
		} else {
			result = url;
		}
		return result;
	}
}
