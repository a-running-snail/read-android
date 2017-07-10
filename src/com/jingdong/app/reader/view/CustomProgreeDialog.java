package com.jingdong.app.reader.view;

import com.jingdong.app.reader.R;

import android.app.ProgressDialog;
import android.content.Context;

public class CustomProgreeDialog {

	private static ProgressDialog myDialog;

	public static ProgressDialog instace(Context context) {

		myDialog = new ProgressDialog(context);
		myDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		myDialog.setIndeterminate(false);
		myDialog.setMessage(context.getResources().getString(
				R.string.waiting_load_data));
		myDialog.setCancelable(true);
		myDialog.setCanceledOnTouchOutside(false);
		return myDialog;
	}
	
	public static ProgressDialog instance(Context context,String msg) {

		myDialog = new ProgressDialog(context);
		myDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		myDialog.setIndeterminate(false);
		myDialog.setMessage(msg);
		myDialog.setCancelable(false);
		myDialog.setCanceledOnTouchOutside(false);
		return myDialog;
	}
	
	
	

}
