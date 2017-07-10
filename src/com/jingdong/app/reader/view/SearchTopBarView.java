package com.jingdong.app.reader.view;

import java.util.ArrayList;
import java.util.List;

import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class SearchTopBarView extends RelativeLayout implements OnClickListener {
	/**
	 * 自定义view点击事件回调
	 * 
	 * @author WANGGUODONG
	 * 
	 */
	public interface TopBarViewListener {
		public void onLeftMenuClick();

		public void onRightMenu_leftClick();

		public void onRightMenu_rightClick();

		public void onCenterMenuItemClick(int position);
	}

	private TopBarViewListener listener;
	private int currentPosition = 0;
	private Context mContext = null;
	private LinearLayout itemContainer = null;
	private RelativeLayout leftMenuLayout = null;
	private RelativeLayout rightMenuLayout = null;
	private RelativeLayout rightMenuLayout_left = null;
	private RelativeLayout rightMenuLayout_right = null;

	public SearchTopBarView(Context context) {
		this(context, null);
	}

	/**
	 * 返回指定title栏的topbar 默认单个没有底部红点 两个以上有红点，最多四个item
	 * 
	 * @param context
	 * @param attrs
	 * @param item
	 *            中间title的数组
	 */
	public SearchTopBarView(Context context, AttributeSet attrs) {

		super(context, attrs);
		this.mContext = context;
		initView();
	}

	public static int getTopBarHeightPix(Context context) {
		return context.getResources().getDimensionPixelSize(
				R.dimen.custom_top_bar_height);
	}

	public void initView() {
		RelativeLayout layout = (RelativeLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.search_top_bar, this, true);
		itemContainer = (LinearLayout) layout
				.findViewById(R.id.center_container);
		leftMenuLayout = (RelativeLayout) layout.findViewById(R.id.left_menu);
		rightMenuLayout = (RelativeLayout) layout.findViewById(R.id.right_menu);
		rightMenuLayout_left = (RelativeLayout) layout
				.findViewById(R.id.right_menu_left);
		rightMenuLayout_right = (RelativeLayout) layout
				.findViewById(R.id.right_menu_right);
		leftMenuLayout.setOnClickListener(this);
		rightMenuLayout_left.setOnClickListener(this);
		rightMenuLayout_right.setOnClickListener(this);
	}

	public void updateTopBarView(boolean isNone) {

		if (itemContainer == null)
			return;

		itemContainer.removeAllViews();
		// 测量左右菜单
		leftMenuLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		rightMenuLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

		// 获得左右菜单的宽度
		int leftMenuWidth = leftMenuLayout.getMeasuredWidth();
		int rightMenuWidth = rightMenuLayout.getMeasuredWidth();
		int titleWidth = 0;
		if (isNone) {
			// 获得中间标题宽度(取菜单的宽度最大值)
			titleWidth = (int) ScreenUtils.getWidthJust(mContext) - (leftMenuWidth + 32);
		}else{
			// 获得中间标题宽度(取菜单的宽度最大值)
			titleWidth = (int) ScreenUtils.getWidthJust(mContext) - (leftMenuWidth + rightMenuWidth);
		}
			

		LinearLayout view = (LinearLayout) LayoutInflater.from(mContext).inflate(
				R.layout.search_top_bar_item, null);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		params.width = titleWidth;
		params.height = getTopBarHeightPix(mContext);
		view.setLayoutParams(params);
		itemContainer.addView(view);
	}


	/**
	 * 设置左边菜单可见性,文本按钮
	 * 
	 * @param visiable
	 * @param text
	 */
	public void setLeftMenuVisiable(boolean visiable, int imgRes) {

		if (leftMenuLayout == null)
			return;

		if (visiable) {
			leftMenuLayout.setVisibility(View.VISIBLE);
			ImageView imageView = (ImageView) leftMenuLayout
					.findViewById(R.id.sub_menu_left_image);

			imageView.setVisibility(View.VISIBLE);
			imageView.setImageResource(imgRes);

		} else {
			leftMenuLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 设置右边菜单可见性
	 * 右边两个按钮
	 * @param visiable
	 * @param text
	 */
	public void setRightMenuVisiable(boolean visiable, int left_imgRes,
			int right_imgRes) {

		if (rightMenuLayout == null)
			return;

		if (visiable) {
			rightMenuLayout.setVisibility(View.VISIBLE);
			rightMenuLayout_left.setVisibility(View.VISIBLE);
			rightMenuLayout_right.setVisibility(View.VISIBLE);
			ImageView right_image_left = (ImageView) 
					findViewById(R.id.sub_menu_right_left_image);

			ImageView right_image_right = (ImageView) 
					findViewById(R.id.sub_menu_right_right_image);
			right_image_left.setVisibility(View.VISIBLE);
			right_image_left.setImageResource(left_imgRes);
			right_image_right.setVisibility(View.VISIBLE);
			right_image_right.setImageResource(right_imgRes);
		} else {
			rightMenuLayout.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * 设置右边菜单可见性
	 * 没有按钮
	 * @param visiable
	 * @param text
	 */
	public void setRightMenuVisiable(boolean visiable) {

		if (rightMenuLayout == null)
			return;

		if (visiable) {
			rightMenuLayout.setVisibility(View.GONE);
			rightMenuLayout_left.setVisibility(View.GONE);
			rightMenuLayout_right.setVisibility(View.GONE);
			ImageView right_image_left = (ImageView) 
					findViewById(R.id.sub_menu_right_left_image);

			ImageView right_image_right = (ImageView) 
					findViewById(R.id.sub_menu_right_right_image);
			right_image_left.setVisibility(View.GONE);
			right_image_right.setVisibility(View.GONE);
		} else {
			rightMenuLayout.setVisibility(View.GONE);
		}
	}


	public void onTitleItemClick(int position) {

		if (currentPosition == position) {
			return;
		} else {
			currentPosition = position;
			// dotMoveToPosition(position);
			if (listener != null)
				listener.onCenterMenuItemClick(position);
		}

	}

	public TopBarViewListener getListener() {
		return listener;
	}

	public void setListener(TopBarViewListener listener) {
		this.listener = listener;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.left_menu:

			// Toast.makeText(mContext, "左侧菜单被点击", Toast.LENGTH_SHORT).show();
			if (listener != null)
				listener.onLeftMenuClick();
			break;

		case R.id.right_menu_left:
			// Toast.makeText(mContext, "右侧菜单被点击", Toast.LENGTH_SHORT).show();
			if (listener != null)
				listener.onRightMenu_leftClick();
			break;
		case R.id.right_menu_right:
			if (listener != null) {
				listener.onRightMenu_rightClick();
			}
		}

	}
}
