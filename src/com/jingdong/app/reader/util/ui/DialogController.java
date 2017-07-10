package com.jingdong.app.reader.util.ui;

import com.jingdong.app.reader.util.ui.view.JdOptionDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

/**
 * 对话框统一控制类
 * 
 * TODO 没有很好地解决同一个操作，有可能要放UI线程，有可能不用放UI线程。UI线程和非UI线程之间的协调。
 */
public class DialogController implements JdOptionDialog.OnClickListener{

	private Context context;
	protected JdOptionDialog.Builder builder;
	protected JdOptionDialog alertDialog;

	private boolean canBack = false;// 默认不允许后退

	private CharSequence initTitle;
	private CharSequence initMessage;
	private CharSequence initPositiveButton;
	private CharSequence initNeutralButton;
	private CharSequence initNegativeButton;
	private View view;

	/**
	 * 初始化，应该定制后调用（非UI线程）
	 */
	public void init(Context context) {
		this.context = context;
		builder = new JdOptionDialog.Builder(context);
		initContent();
		initButton();
	}

	/**
	 * 初始化内容
	 */
	protected void initContent() {

		// 标题
		if (TextUtils.isEmpty(initTitle)) {
			// builder.setTitle("京东");// 默认值
		} else {
			builder.setTitle(initTitle.toString());
		}

		// 信息
		if (TextUtils.isEmpty(initMessage)) {
			// 默认值
		} else {
			builder.setMessage(initMessage.toString());
		}

		// VIEW
		if (null != view) {
			builder.addSelfView(view);
		}

		// 按键事件
		builder.setCancelable(canBack);
	}

	/**
	 * 初始化按钮
	 */
	protected void initButton() {

		// （左边的按钮）重试
		if (!TextUtils.isEmpty(initPositiveButton)) {
			builder.setPositiveButton(initPositiveButton.toString(), this);
		}

		// （中间的按钮）
		if (!TextUtils.isEmpty(initNeutralButton)) {
			builder.setMidlleButton(initNeutralButton.toString(), this);
		}

		// （右边的按钮）取消或退出
		if (!TextUtils.isEmpty(initNegativeButton)) {
			builder.setNegativeButton(initNegativeButton.toString(), this);
		}

	}

	/**
	 * 显示（UI线程）
	 */
	public void show() {
		if (null != alertDialog) {
			alertDialog.show();
		} else if (null != builder) {
			alertDialog = builder.show();
		} else {
			throw new RuntimeException("builder is null, need init this controller");
		}
	}

	@Override
	public void onClick(JdOptionDialog dialog, int which) {
	}

	/**
	 * 标题
	 */
	public void setTitle(CharSequence title) {
		if (null != alertDialog) {
			alertDialog.setTitle(title);
		} else if (null != builder) {
			builder.setTitle(title.toString());
		} else {
			initTitle = title;
		}
	}

	/**
	 * 内容
	 */
	public void setMessage(CharSequence message) {
		if (null != alertDialog) {
			alertDialog.setMessage(message.toString());
		} else if (null != builder) {
			builder.setMessage(message.toString());
		} else {
			initMessage = message;
		}
	}

	/**
	 * 左按钮（如果字符串给null或""就隐藏）
	 */
	public void setPositiveButton(CharSequence text) {
		if (null != alertDialog) {
			if (!TextUtils.isEmpty(text)) { 
				builder.setPositiveButton(text.toString(), this);
			}
		} else if (null != builder) {
			builder.setPositiveButton(text.toString(), this);
		} else {
			initPositiveButton = text;
		}
	}

	/**
	 * 中按钮（如果字符串给null或""就隐藏）
	 */
	public void setNeutralButton(CharSequence text) {
		if (null != alertDialog) {
			if (!TextUtils.isEmpty(text)) { 
			builder.setMidlleButton(text.toString(), this);
			}
		} else if (null != builder) {
			builder.setMidlleButton(initNeutralButton.toString(), this);
		} else {
			initNeutralButton = text;
		}
	}

	/**
	 * 右按钮（如果字符串给null或""就隐藏）
	 */
	public void setNegativeButton(CharSequence text) {
		if (null != alertDialog) {
			if (TextUtils.isEmpty(text)) {// 隐藏
				alertDialog.setNegativeButton(text.toString(), this);
			}
		} else if (null != builder) {
			builder.setNegativeButton(initNegativeButton.toString(), this);
		} else {
			initNegativeButton = text;
		}
	}

	/**
	 * 自定义VIEW
	 */
	public void setView(View view) {
		if (null != alertDialog) {
			alertDialog.addSelfView(view);
		} else if (null != builder) {
			builder.addSelfView(view);
		} else {
			this.view = view;
		}
	}

	public boolean isCanBack() {
		return canBack;
	}

	/**
	 * 设置后退键是否有效，默认不允许后退
	 */
	public void setCanBack(boolean canBack) {
		this.canBack = canBack;
		if (null != alertDialog) {
			alertDialog.setCancelable(canBack);
		} else if (null != builder) {
			builder.setCancelable(canBack);
		}  
	}


}
