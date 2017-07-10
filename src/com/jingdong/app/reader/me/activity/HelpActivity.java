package com.jingdong.app.reader.me.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;

public class HelpActivity extends BaseActivityWithTopBar{

	private RelativeLayout online_readLayout;
	private RelativeLayout buy_ebooklLayout;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.help);
		online_readLayout = (RelativeLayout) findViewById(R.id.online_readlayout);
		buy_ebooklLayout = (RelativeLayout) findViewById(R.id.buy_ebooklayout);
		
		online_readLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(HelpActivity.this,OnlineReadHelpActivity.class);
				startActivity(intent);
			}
		});
		
		buy_ebooklLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(HelpActivity.this,BuyEbookHelpActivity.class);
				startActivity(intent);
			}
		});
	}
}
