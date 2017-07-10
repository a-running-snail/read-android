package com.jingdong.app.reader.view;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.view.dialog.CommonDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class BookCommentDialog extends Dialog implements OnClickListener {

	public interface OnCustomDialogItemClickListener {

		public void onCustomDialogItemClick(int type);
	}

	private OnCustomDialogItemClickListener listener = null;
	private Context context = null;

	public BookCommentDialog(Context context) {
		super(context);
	}

	public BookCommentDialog(Context context,
			OnCustomDialogItemClickListener listener) {
		super(context, R.style.common_dialog_style);
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.book_comment_dialog);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setCanceledOnTouchOutside(false);
		View layout = inflater.inflate(R.layout.book_comment_dialog, null);
		addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		LinearLayout button1 = (LinearLayout) findViewById(R.id.common_dialog_button1);
		LinearLayout button2 = (LinearLayout) findViewById(R.id.common_dialog_button2);
		LinearLayout button3 = (LinearLayout) findViewById(R.id.common_dialog_button3);
		LinearLayout button4 = (LinearLayout) findViewById(R.id.common_dialog_button4);

		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		button3.setOnClickListener(this);
		button4.setOnClickListener(this);

		setCanceledOnTouchOutside(false);

	}

	@Override
	public void onClick(View v) {

		if (listener == null)
			return;

		switch (v.getId()) {
		case R.id.common_dialog_button1:
			listener.onCustomDialogItemClick(101);
			dismiss();
			break;

		case R.id.common_dialog_button2:
			listener.onCustomDialogItemClick(102);
			dismiss();
			break;

		case R.id.common_dialog_button3:
			listener.onCustomDialogItemClick(103);
			dismiss();
			break;

		case R.id.common_dialog_button4:
			listener.onCustomDialogItemClick(104);
			dismiss();
			break;

		}

	}
}