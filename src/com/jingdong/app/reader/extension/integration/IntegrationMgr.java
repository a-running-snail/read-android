package com.jingdong.app.reader.extension.integration;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.view.dialog.DialogManager;

public class IntegrationMgr {

	public static void firstDownloadToGrand(final Context ctx) {
		
		DialogManager.showCommonDialog(ctx, "", ctx.getString(R.string.download_jdreader_to_grand_integration),
				"注册登录", "以后再说", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					Intent login = new Intent(ctx,LoginActivity.class);
					ctx.startActivity(login);
					break;
				case DialogInterface.BUTTON_NEGATIVE:

					break;
				}
				dialog.dismiss();
			}
		});
	}
}
