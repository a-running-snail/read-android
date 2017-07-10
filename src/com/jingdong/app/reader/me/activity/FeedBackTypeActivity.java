package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;

public class FeedBackTypeActivity extends BaseActivityWithTopBar {

	private LinearLayout root;
	private List<String> stringlist = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.feedtype);
		root = (LinearLayout) findViewById(R.id.root);
		stringlist.add("功能意见");
		stringlist.add("界面意见");
		stringlist.add("您的新需求");
		stringlist.add("操作意见");
		stringlist.add("流量问题");
		stringlist.add("其他意见");
		init(root);
	}

	private void init(View root) {
		for (int i = 0; i < stringlist.size(); i++) {
			View itemView = FeedBackTypeActivity.this.getLayoutInflater()
					.inflate(R.layout.feedtype_list,
							null);

			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params1.height = FeedBackTypeActivity.this.getResources()
					.getDimensionPixelSize(
							R.dimen.custom_top_bar_pupopwindow_item_height);
			params1.gravity = Gravity.CENTER;
			itemView.setLayoutParams(params1);

			View lineView = FeedBackTypeActivity.this.getLayoutInflater()
					.inflate(R.layout.line_view, null);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.leftMargin= 16;
			params.rightMargin = 16;
			params.height = ScreenUtils.dip2px(0.5);
			lineView.setLayoutParams(params);

			final ImageView radioButton = (ImageView) itemView
					.findViewById(R.id.item_flag);
			radioButton.setId(i);	
			final int position =i;
			String model = LocalUserSetting.getFeedBackType(FeedBackTypeActivity.this);
			MZLog.d("cj", "model========>>" + model);
            if (stringlist.get(i).equals(model)) {
				if(radioButton.getId() == i){
					radioButton.setVisibility(View.VISIBLE);
				}else {
					radioButton.setVisibility(View.GONE);
				}
			}
			itemView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					String result = stringlist.get(position);
	                Intent intent = new Intent();
	                intent.putExtra("result", result);
	                /*
	                 * 调用setResult方法表示我将Intent对象返回给之前的那个Activity，这样就可以在onActivityResult方法中得到Intent对象，
	                 */
	                LocalUserSetting.saveFeedBackType(FeedBackTypeActivity.this,result);
	                setResult(1001, intent);
	                //    结束当前这个Activity对象的生命
	                finish();
				}
			});

			TextView titleTextView = (TextView) itemView
					.findViewById(R.id.item);
			titleTextView.setText(stringlist.get(i));
			
			((ViewGroup) root).addView(itemView);
			((ViewGroup) root).addView(lineView);
		}
	}
}
