package com.jingdong.app.reader.me.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.view.dialog.DialogManager;

public class ContactUsActivity extends BaseActivityWithTopBar {

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.contact_us);
		View mTelView = findViewById(R.id.tel_rl);
		final String telNum = "400—606—5500";
		mTelView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogManager.showCommonDialog(ContactUsActivity.this, "", telNum, "呼叫", "取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							Intent phoneIntent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + telNum));
							// 启动
							startActivity(phoneIntent);
							break;
						case DialogInterface.BUTTON_NEGATIVE:

							break;

						default:
							break;
						}
						dialog.dismiss();
					}

				});
			}
		});
	}
}
