package com.jingdong.app.reader.view;

import java.util.ArrayList;
import java.util.List;

import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.R;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * 顶部标题栏的弹出层
 *
 */
public class TopBarPopupWindow {

	public interface onPopupWindowItemClickListener {
		public void onPopupWindowItemClick(String type, int position);

		public void onPopupWindowSubmenuItemCheck(String type, int checkid);
	}

	public interface DismissListener {
		public void onDismiss();
	}

	public DismissListener dismissListener;

	public DismissListener getDismissListener() {
		return dismissListener;
	}

	public void setDismissListener(DismissListener dismissListener) {
		this.dismissListener = dismissListener;
	}

	public onPopupWindowItemClickListener listener;

	public onPopupWindowItemClickListener getListener() {
		return listener;
	}

	public void setListener(onPopupWindowItemClickListener listener) {
		this.listener = listener;
	}

	private Activity mActivity = null;
	private List<String> item = new ArrayList<String>();
	private List<String> submenu = new ArrayList<String>();
	private boolean isShowing = false;
	private PopupWindow mPopupWindow = null;
	private String type = "";

	public TopBarPopupWindow(Activity activity, List<String> items, String type) {
		this.mActivity = activity;
		this.item = items;
		this.type = type;// 用于标识点击事件是由谁触发的
		initView();
	}

	public TopBarPopupWindow(Activity activity, List<String> items, List<String> submenu, String type) {
		this.mActivity = activity;
		this.item = items;
		this.submenu = submenu;
		this.type = type;// 用于标识点击事件是由谁触发的
		initViewWithSubMenu();
	}

	public void initView() {

		View popupView = mActivity.getLayoutInflater().inflate(R.layout.custom_topbar_pupop_view, null);
		LinearLayout root = (LinearLayout) popupView.findViewById(R.id.root);
		//背景点击，关闭弹出框
		popupView.findViewById(R.id.menu_shadow).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		//子项菜单
		for (int i = 0; i < item.size(); i++) {
			View itemView = mActivity.getLayoutInflater().inflate(R.layout.custom_topbar_pupop_view_item, null);
			TextView textView = (TextView) itemView.findViewById(R.id.item);

			textView.setText(item.get(i));
			final int positon = i;
			View line = itemView.findViewById(R.id.line);
			if (i == item.size() - 1)
				line.setVisibility(View.GONE);
			else {
				line.setVisibility(View.VISIBLE);
			}

			itemView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					dismiss();
					if (listener != null)
						listener.onPopupWindowItemClick(getType(), positon);
				}
			});

			root.addView(itemView);
		}

		//生成对话框
		mPopupWindow = new PopupWindow(popupView, LayoutParams.MATCH_PARENT/*宽*/, LayoutParams.MATCH_PARENT/*高*/, true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		//设置背景（蒙层）
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(mActivity.getResources().getColor(R.color.bg_menu_shadow)));
		mPopupWindow.setOnDismissListener(new OnDismissListener() {

			/**
			 * 解除对话框
			 */
			@Override
			public void onDismiss() {
				if (dismissListener != null) {
					dismissListener.onDismiss();
				}
			}
		});
	}

	public void initViewWithSubMenu() {

		View popupView = mActivity.getLayoutInflater().inflate(R.layout.custom_topbar_pupop_view, null);
		LinearLayout root = (LinearLayout) popupView.findViewById(R.id.root);
		popupView.findViewById(R.id.menu_shadow).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		//子项菜单
		for (int i = 0; i < item.size(); i++) {
			View itemView = mActivity.getLayoutInflater().inflate(R.layout.custom_topbar_pupop_view_item, null);
			TextView textView = (TextView) itemView.findViewById(R.id.item);
			textView.setText(item.get(i));

			View line = itemView.findViewById(R.id.line);
			if (i == item.size() - 1)
				line.setVisibility(View.GONE);
			else {
				line.setVisibility(View.VISIBLE);
			}

			final int positon = i;
			itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
					if (listener != null)
						listener.onPopupWindowItemClick(getType(), positon);
				}
			});

			root.addView(itemView);
		}

		root.addView(getSubMenuView());

		mPopupWindow = new PopupWindow(popupView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(mActivity.getResources().getColor(R.color.bg_menu_shadow)));
		mPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				if (dismissListener != null) {
					dismissListener.onDismiss();
				}
			}
		});
	}

	public View getSubMenuView() {

		View submenuView = mActivity.getLayoutInflater().inflate(R.layout.custom_topbar_pupop_view_submenu, null);

		LinearLayout root = (LinearLayout) submenuView.findViewById(R.id.radioGroup);

		for (int i = 0; i < submenu.size(); i++) {
			View itemView = mActivity.getLayoutInflater().inflate(R.layout.custom_topbar_pupop_view_submenu_item, null);

			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params1.height = mActivity.getResources().getDimensionPixelSize(R.dimen.custom_top_bar_pupopwindow_item_height);
			params1.gravity = Gravity.CENTER;
			itemView.setLayoutParams(params1);

			View lineView = mActivity.getLayoutInflater().inflate(R.layout.line_view, null);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params.height = ScreenUtils.dip2px(0.5);
			lineView.setLayoutParams(params);

			final ImageView radioButton = (ImageView) itemView.findViewById(R.id.item_flag);
			radioButton.setId(i);
			String model = LocalUserSetting.getBookShelfModel(mActivity);
			if (("FengMian").equals(model)) {
				if (radioButton.getId() == 0) {
					radioButton.setVisibility(View.VISIBLE);
				} else {
					radioButton.setVisibility(View.GONE);
				}
			} else if (("Name").equals(model)) {
				if (radioButton.getId() == 1) {
					radioButton.setVisibility(View.VISIBLE);
				} else {
					radioButton.setVisibility(View.GONE);
				}
			} else if (("Time").equals(model)) {
				if (radioButton.getId() == 2) {
					radioButton.setVisibility(View.VISIBLE);
				} else {
					radioButton.setVisibility(View.GONE);
				}
			} else {
				if (radioButton.getId() == 0) {
					radioButton.setVisibility(View.VISIBLE);
				} else {
					radioButton.setVisibility(View.GONE);
				}
			}

			TextView titleTextView = (TextView) itemView.findViewById(R.id.item);
			titleTextView.setText(submenu.get(i));

			final int position = i;
			itemView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (listener != null)
						listener.onPopupWindowSubmenuItemCheck(getType(), position);
				}
			});
			root.addView(itemView);
			root.addView(lineView);
		}

		return submenuView;

	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void dismiss() {
		if (mPopupWindow != null)
			mPopupWindow.dismiss();
		setShowing(false);
	}

	/**
	 * 显示弹出框
	 * @param anchor
	 */
	public void show(View anchor) {
		if (mPopupWindow != null)
			mPopupWindow.showAsDropDown(anchor, 0, 0);//设置显示PopupWindow的位置位于View的左下方，x,y表示坐标偏移量
		setShowing(true);
	}

	public boolean isShowing() {
		return isShowing;
	}

	public void setShowing(boolean isShowing) {
		this.isShowing = isShowing;
	}

}
