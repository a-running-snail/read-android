package com.jingdong.app.reader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;


public class BookInfoItemDetailActivity extends BaseActivityWithTopBar {

	private TextView authorTextView;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_bookinfo_author);
		Intent it=getIntent();
		String info=it.getStringExtra("info");
		Spanned title=Html.fromHtml(info);
		authorTextView=(TextView) findViewById(R.id.author_info);
		authorTextView.setText(title);
	}
}
