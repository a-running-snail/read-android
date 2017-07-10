package com.jingdong.app.reader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.entity.extra.JDBookInfo.Detail;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.R;

public class BookBasicInfoActivity extends BaseActivityWithTopBar {

	private TextView title;

	private TextView author;

	private TextView publisher;
	private TextView publishTime;
	private TextView publishIsbn;
	private TextView publishEdition;
	private TextView fileSize;
	private JDBookInfo.Detail bookinfo = null;
	private String publisher_tag = "";

	public void prepareLayout() {

		Intent it = getIntent();

		title = (TextView) findViewById(R.id.title_content);

		author = (TextView) findViewById(R.id.author_content);

		publisher = (TextView) findViewById(R.id.publisher_content);
		publishTime = (TextView) findViewById(R.id.publish_time_content);
		publishIsbn = (TextView) findViewById(R.id.publish_isbn_content);
		publishEdition=(TextView) findViewById(R.id.publish_edition);
		fileSize = (TextView) findViewById(R.id.publish_file_size_content);

		bookinfo = (Detail) it.getBundleExtra("bookinfo").get("bookinfo");
		
		UiStaticMethod.setTextNoGone(title, bookinfo.bookName);

		UiStaticMethod.setTextNoGone(author, bookinfo.author);
		UiStaticMethod.setTextNoGone(publishEdition, bookinfo.edition);
		
		UiStaticMethod.setTextNoGone(publisher, bookinfo.publisher);
		UiStaticMethod.setTextNoGone(publishTime,  bookinfo.publishTime);
		UiStaticMethod.setTextNoGone(publishIsbn,  bookinfo.isbn);
		UiStaticMethod.setTextNoGone(fileSize,  bookinfo.size+"MB");

	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_bookinfo_basicinfo);
		prepareLayout();
	}

}
