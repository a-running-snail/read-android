package com.jingdong.app.reader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.R;

public class BookInfoAuthorActivity extends MZReadCommonActivity {

	private TextView authorTextView;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_bookinfo_author);
		Intent it=getIntent();
		String info=it.getStringExtra("author_info");
		
		authorTextView=(TextView) findViewById(R.id.author_info);
		authorTextView.setText(info.replace("\n", "\n\n"));
	}
}
