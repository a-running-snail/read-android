package com.jingdong.app.reader.bob.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.util.ThreadUtil;
import com.jingdong.app.reader.view.JdProgressDialog;



public class ShowTools {

	public static Toast toast;
	public static int NOTIFICATION_ID = 8633170;

	public static void toast(String notice) {
		if (notice != null && notice.length() > 0) {
			if (toast == null) {
				toast = Toast.makeText(MZBookApplication.getInstance(), notice,
						Toast.LENGTH_SHORT);
			} else {
				toast.setText(notice);
			}
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}
	
	public static void toastInThread(String notice) {
		Message msg = MZBookApplication.getInstance().mHandler.obtainMessage(0, 0,
				0);
		msg.obj = notice;
		MZBookApplication.getInstance().mHandler.sendMessage(msg);
	}

	public static void toastLongInThread(String notice) {
		Message msg = MZBookApplication.getInstance().mHandler.obtainMessage(1, 0,
				0);
		msg.obj = notice;
		MZBookApplication.getInstance().mHandler.sendMessage(msg);
	}

	public static void toastLong(String notice) {
		if (notice != null && notice.length() > 0) {
			if (toast == null) {
				toast = Toast.makeText(MZBookApplication.getInstance(), notice,
						Toast.LENGTH_SHORT);
			} else {
				toast.setText(notice);
			}
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

	public static void toastInThread(int rId) {
		String message = "";
		message = MZBookApplication.getContext().getString(rId);
		toastInThread(message);
	}

	public static void toast(int rId) {
		String message = "";
		message = MZBookApplication.getContext().getString(rId);
		toast(message);
	}
	
	public static JdProgressDialog getProgressDialog(final Activity activity,
			final Runnable cancelRunnable) {
		JdProgressDialog mpDialog = new JdProgressDialog(activity);
		// mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		mpDialog.setTitle("加载插件中");// 设置标题
		// mpDialog.setIcon(R.drawable.ic_launcher);// 设置图标
		mpDialog.setMessage("请稍候，努力加载中");
		mpDialog.setMessageColor(Color.WHITE);
		mpDialog.setIndeterminate(true);// 设置进度条是否为不明确
		mpDialog.setCancelable(true);// 设置进度条是否可以按退回键取消
		mpDialog.setCanceledOnTouchOutside(false);
		mpDialog.setOnCancelListener(new JdProgressDialog.OnCancelListener() {

			@Override
			public void onCancel(JdProgressDialog dialog) {
				cancelRunnable.run();
				dialog.dismiss();
			}
		});
		return mpDialog;
	}

	public static AlertDialog.Builder getDialogBuilder(final Activity activity,
			String title, String message, final Runnable runnable,
			final Intent intent) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity,
				ProgressDialog.THEME_HOLO_LIGHT);
		builder.setTitle(title)
				.setMessage(message)
				.setPositiveButton(" 确 认 ",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								ThreadUtil.runInThread(runnable);
							}
						})
				.setNegativeButton(" 取 消  ",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface paramDialogInterface) {
						paramDialogInterface.dismiss();
					}
				}).setCancelable(true);
		return builder;
	}
}
