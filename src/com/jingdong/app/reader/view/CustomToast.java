package com.jingdong.app.reader.view;

import com.jingdong.app.reader.R;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 自定义Toast
 * @author J.Beyond
 *
 */
public class CustomToast {

	private static Toast mToast;
	private static View mView;
	private static TextView mContentTv;

	private CustomToast() {
	}

	private static void getToast(Context context) {
		if (mToast == null) {
			mToast = new Toast(context);
		}
		if (mView == null) {
			mView = LayoutInflater.from(context).inflate(R.layout.custom_toast_view, null);
			mContentTv = (TextView) mView.findViewById(R.id.custom_toast_content_tv);
		}
		mToast.setView(mView);
	}

	public static void showToast(final Context context, final SpannableString span) {
		try {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					getToast(context.getApplicationContext());
					if (!TextUtils.isEmpty(span)) {
						mContentTv.setText(span);
					}
					mToast.setGravity(Gravity.CENTER,0,0);
					mToast.setDuration(Toast.LENGTH_LONG);
					mToast.show();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void showToast(final Context context, final String span) {
		try {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					getToast(context.getApplicationContext());
					if (!TextUtils.isEmpty(span)) {
						mContentTv.setText(span);
					}
					mToast.setGravity(Gravity.CENTER,0,0);
					mToast.setDuration(Toast.LENGTH_LONG);
					mToast.show();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void showNetErrorToast(final Context context, final String content) {
		try {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					getToast(context.getApplicationContext());
					if (!TextUtils.isEmpty(content)) {
						mContentTv.setText(content);
					}
					mToast.setGravity(Gravity.BOTTOM,0,200);
					mToast.setDuration(Toast.LENGTH_SHORT);
					mToast.show();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
