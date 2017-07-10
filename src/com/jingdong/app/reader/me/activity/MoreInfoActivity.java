package com.jingdong.app.reader.me.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.me.model.UserDetail;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.util.UiStaticMethod;

public class MoreInfoActivity extends MZReadCommonActivity implements OnClickListener {
	
	public final static String USER_DETAIL = "userDetail";
	private UserDetail userDetail;
	private String male = "";
	private String female = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_more);
		Intent intent = getIntent();
		userDetail = (UserDetail) intent.getParcelableExtra(USER_DETAIL);
		userDetail.deleteObservers();
		fillViewWithInfo();
		View sinaWeibo = findViewById(R.id.sina_weibo);
		if (userDetail.getSinalModel() != null) {
			findViewById(R.id.sina_weibo).setOnClickListener(this);
			sinaWeibo.setVisibility(View.VISIBLE);
		} else {
			sinaWeibo.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, WebViewActivity.class);
		switch (v.getId()) {
		case R.id.user_blog_widget:
			TextView textView = (TextView) findViewById(R.id.user_blog);
			intent.putExtra(WebViewActivity.UrlKey, textView.getText().toString());
			startActivity(intent);
			break;
		case R.id.sina_weibo:
			intent.putExtra(WebViewActivity.UrlKey, URLText.sinaWeibo + userDetail.getSinalModel().getUid());
			startActivity(intent);
			break;
		}

	}

	private void fillViewWithInfo() {
		setSexString();
		View views[] = setViews();
		boolean show[] = setText();
		setRowsVisibility(show, views);
		int visiable = getLastVisiable(show);
		setLastBackground(visiable, views);
	}

	private void setSexString() {
		Resources resources = getResources();
		male = resources.getString(R.string.male);
		female = resources.getString(R.string.female);
	}

	private boolean[] setText() {
		boolean[] show = new boolean[5];
		show[0] = UiStaticMethod.setText((TextView) findViewById(R.id.user_nick_name), userDetail.getName());
		show[1] = UiStaticMethod.setText((TextView) findViewById(R.id.user_sex), userDetail.isFemale() ? female : male);
		show[2] = UiStaticMethod.setText((TextView) findViewById(R.id.user_email), userDetail.getContactEmail());
		show[3] = UiStaticMethod.setText((TextView) findViewById(R.id.user_blog), userDetail.getBlog());
		show[4] = UiStaticMethod.setText((TextView) findViewById(R.id.user_introduction), userDetail.getSummary());
		return show;
	}

	private View[] setViews() {
		View[] views = new View[5];
		views[0] = findViewById(R.id.user_nick_name_widget);
		views[1] = findViewById(R.id.user_sex_widget);
		views[2] = findViewById(R.id.user_email_widget);
		views[3] = findViewById(R.id.user_blog_widget);
		views[4] = findViewById(R.id.user_introduction_widget);
		views[3].setOnClickListener(this);
		return views;
	}

	private void setRowsVisibility(boolean[] visibility, View[] views) {
		for (int i = 0; i < visibility.length; i++) {
			views[i].setVisibility(visibility[i] ? View.VISIBLE : View.GONE);
		}
	}

	private int getLastVisiable(boolean show[]) {
		int visiable = -1;
		for (int i = 0; i < show.length; i++) {
			if (show[i])
				visiable = i;
		}
		return visiable;
	}

	private void setLastBackground(int visiable, View[] views) {
		if (visiable != -1) {
			if (visiable == 0) {
				views[visiable].setBackgroundResource(R.drawable.grey_rect);
			} else {
				views[visiable].setBackgroundResource(R.drawable.grey_rect_background);
			}
		}
	}

}
