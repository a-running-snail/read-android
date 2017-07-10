package com.jingdong.app.reader.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.util.ScreenUtils;

public class ProgressHUD extends Dialog {
	private static boolean isPageLoading = false;

	public ProgressHUD(Context context, int theme) {
		super(context, theme);
	}

	private static void startAnim(Dialog dialog) {
		ImageView imageView = (ImageView) dialog.findViewById(R.id.spinnerImageView);

		if (isPageLoading) {
			LinearLayout linearLayout=(LinearLayout) dialog.findViewById(R.id.ll_progress_hud);
			linearLayout.setPadding(10, 10, 10, 10);
			int width = ScreenUtils.dip2px(90);
			int height = ScreenUtils.dip2px(90);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
			imageView.setLayoutParams(params);
			imageView.setBackgroundResource(R.anim.loading_book_anim);
		}

		AnimationDrawable spinner = (AnimationDrawable) imageView.getBackground();
		spinner.start();
	}

	/**
	 * 
	 * @Title: setMessage
	 * @Description: 设置文字
	 * @param @param message
	 * @return void
	 * @throws
	 */
	public void setMessage(CharSequence message) {
		if (message != null && message.length() > 0) {
			findViewById(R.id.message).setVisibility(View.VISIBLE);
			TextView txt = (TextView) findViewById(R.id.message);
			txt.setText(message);
			txt.invalidate();
		}
	}

	/**
	 * 
	 * @Title: show
	 * @Description: 显示菊花进度条
	 * @param @param context
	 * @param @param message
	 * @param @param indeterminate
	 * @param @param cancelable
	 * @param @param cancelListener
	 * @param @return
	 * @return ProgressHUD
	 * @throws
	 */
	public static ProgressHUD show(Context context, CharSequence message, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
		isPageLoading = false;
		ProgressHUD dialog = new ProgressHUD(context, R.style.ProgressHUD);
		dialog.setTitle("");
		dialog.setContentView(R.layout.progress_hud);
		startAnim(dialog);
		if (message == null || message.length() == 0) {
			dialog.findViewById(R.id.message).setVisibility(View.GONE);
		} else {
			TextView txt = (TextView) dialog.findViewById(R.id.message);
			txt.setText(message);
		}
		dialog.setCancelable(cancelable);
		dialog.setOnCancelListener(cancelListener);
		dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.dimAmount = 0.2f;
		dialog.getWindow().setAttributes(lp);
		// dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		dialog.show();
		return dialog;
	}

	public static ProgressHUD pageLoaing(Context context) {
		isPageLoading = true;
		ProgressHUD dialog = new ProgressHUD(context, R.style.ProgressHUD);
		dialog.setTitle("");
		dialog.setContentView(R.layout.progress_hud);
		startAnim(dialog);
		
		dialog.findViewById(R.id.message).setVisibility(View.GONE);
		dialog.setCancelable(true);
		dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.dimAmount = 0.2f;
		dialog.getWindow().setAttributes(lp);
		dialog.show();
		return dialog;
	}

}
