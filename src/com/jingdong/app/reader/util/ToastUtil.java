package com.jingdong.app.reader.util;

import com.jingdong.app.reader.application.MZBookApplication;

import android.content.Intent;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class ToastUtil {

	public static void showToastInThread(int resId) {
		Message msg = MZBookApplication.getInstance().mHandler.obtainMessage(0,
				0, 0);
		msg.arg1 = Toast.LENGTH_SHORT;
		msg.obj = MZBookApplication.getContext().getString(resId);
		MZBookApplication.getInstance().mHandler.sendMessage(msg);
	}
	
	public static void showToastInThread(String str, int time) {
		Message msg = MZBookApplication.getInstance().mHandler.obtainMessage(0,
				0, 0);
		msg.arg1 = time;
		msg.obj = str;
		MZBookApplication.getInstance().mHandler.sendMessage(msg);
	}

	public static void showToastInThread(String str, int time, String ebookid) {
		Message msg = MZBookApplication.getInstance().mHandler.obtainMessage(0,
				0, 0);
		msg.arg1 = time;
		msg.obj = str;
		MZBookApplication.getInstance().mHandler.sendMessage(msg);
		
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MZBookApplication.getInstance());
		Intent i = new Intent("download_ebook_fail");
		i.putExtra("ebookid", ebookid);
		lbm.sendBroadcast(i); 
	}

}
