package com.jingdong.app.reader.ui;

import com.jingdong.app.reader.R;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;

public class ViewPagerTabBarHelper {
	private Button[] buttons;
	private ImageView animationIndicator;
	private ViewPager viewPager;
	private int focuseIndex;
	private int minDistance;

	public ViewPagerTabBarHelper(FragmentActivity activity ,View root, int count,
			ViewPager viewPager) {
		View tabLayout = root.findViewById(R.id.tab_layout);
		if (count == 5) {
			buttons = new Button[count];
			buttons[0] = (Button) tabLayout.findViewById(R.id.tabButton0);
			buttons[1] = (Button) tabLayout.findViewById(R.id.tabButton1);
			buttons[2] = (Button) tabLayout.findViewById(R.id.tabButton2);
			buttons[3] = (Button) tabLayout.findViewById(R.id.tabButton3);
			buttons[4] = (Button) tabLayout.findViewById(R.id.tabButton4);
		} else if (count == 4) {
			buttons = new Button[count];
			buttons[0] = (Button) tabLayout.findViewById(R.id.tabButton0);
			buttons[1] = (Button) tabLayout.findViewById(R.id.tabButton1);
			buttons[2] = (Button) tabLayout.findViewById(R.id.tabButton2);
			buttons[3] = (Button) tabLayout.findViewById(R.id.tabButton3);
			tabLayout.findViewById(R.id.tabButton4).setVisibility(View.GONE);
		} else if (count == 3) {
			buttons = new Button[count];
			buttons[0] = (Button) tabLayout.findViewById(R.id.tabButton0);
			buttons[1] = (Button) tabLayout.findViewById(R.id.tabButton1);
			buttons[2] = (Button) tabLayout.findViewById(R.id.tabButton2);
			tabLayout.findViewById(R.id.tabButton3).setVisibility(View.GONE);
			tabLayout.findViewById(R.id.tabButton4).setVisibility(View.GONE);
		} else if (count == 2) {
			buttons = new Button[count];
			buttons[0] = (Button) tabLayout.findViewById(R.id.tabButton0);
			buttons[1] = (Button) tabLayout.findViewById(R.id.tabButton1);
			tabLayout.findViewById(R.id.tabButton2).setVisibility(View.GONE);
			tabLayout.findViewById(R.id.tabButton3).setVisibility(View.GONE);
			tabLayout.findViewById(R.id.tabButton4).setVisibility(View.GONE);
		}
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay()
				.getMetrics(metric);
		this.minDistance = metric.widthPixels / count;

		this.animationIndicator = (ImageView) tabLayout
				.findViewById(R.id.item_dot);

//		this.animationIndicator.setX(0);// 设置初始位置

		this.viewPager = viewPager;
		this.focuseIndex = 0;
		initButtons();
	}

	public ViewPagerTabBarHelper(Activity activity, int count,
			ViewPager viewPager) {
		View tabLayout = activity.findViewById(R.id.tab_layout);
		if (count == 5) {
			buttons = new Button[count];
			buttons[0] = (Button) tabLayout.findViewById(R.id.tabButton0);
			buttons[1] = (Button) tabLayout.findViewById(R.id.tabButton1);
			buttons[2] = (Button) tabLayout.findViewById(R.id.tabButton2);
			buttons[3] = (Button) tabLayout.findViewById(R.id.tabButton3);
			buttons[4] = (Button) tabLayout.findViewById(R.id.tabButton4);
		} else if (count == 4) {
			buttons = new Button[count];
			buttons[0] = (Button) tabLayout.findViewById(R.id.tabButton0);
			buttons[1] = (Button) tabLayout.findViewById(R.id.tabButton1);
			buttons[2] = (Button) tabLayout.findViewById(R.id.tabButton2);
			buttons[3] = (Button) tabLayout.findViewById(R.id.tabButton3);
			tabLayout.findViewById(R.id.tabButton4).setVisibility(View.GONE);
		} else if (count == 3) {
			buttons = new Button[count];
			buttons[0] = (Button) tabLayout.findViewById(R.id.tabButton0);
			buttons[1] = (Button) tabLayout.findViewById(R.id.tabButton1);
			buttons[2] = (Button) tabLayout.findViewById(R.id.tabButton2);
			tabLayout.findViewById(R.id.tabButton3).setVisibility(View.GONE);
			tabLayout.findViewById(R.id.tabButton4).setVisibility(View.GONE);
		} else if (count == 2) {
			buttons = new Button[count];
			buttons[0] = (Button) tabLayout.findViewById(R.id.tabButton0);
			buttons[1] = (Button) tabLayout.findViewById(R.id.tabButton1);
			tabLayout.findViewById(R.id.tabButton2).setVisibility(View.GONE);
			tabLayout.findViewById(R.id.tabButton3).setVisibility(View.GONE);
			tabLayout.findViewById(R.id.tabButton4).setVisibility(View.GONE);
		}
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		this.minDistance = metric.widthPixels / count;

		this.animationIndicator = (ImageView) tabLayout
				.findViewById(R.id.item_dot);

//		this.animationIndicator.setLayoutParams(new LinearLayout.LayoutParams(
//				metric.widthPixels / count, ScreenUtils.dip2px(activity, 2)));

		// int animationIndicatorWidth =metric.widthPixels/count;// (int)
		// (metric.density * 50);// 这里50是tabIndicator宽度dp数值

		this.animationIndicator.setX(minDistance/2);// 设置初始位置

		this.viewPager = viewPager;
		this.focuseIndex = 0;
		initButtons();
	}

	public Button[] getTabButtons() {
		return buttons;
	}

	private void initButtons() {
		for (int i = 0; i < buttons.length; ++i) {
			Button button = buttons[i];
			final int index = i;
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					viewPager.setCurrentItem(index);
				}

			});
		}
	}

	public void setSelectItem(final int position) {
		int prePosition = this.focuseIndex;
		this.focuseIndex = position;
		reInitView(position);
		TranslateAnimation animation = new TranslateAnimation(prePosition
				* minDistance, position * minDistance, 0, 0);
		animation.setDuration(0);
		animation.setFillAfter(true);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				reInitView(position);
			}
		});
		animationIndicator.startAnimation(animation);

	}

	private void reInitView(int position) {
		for (int i = 0; i < buttons.length; ++i) {
			if (i == position) {
				buttons[i].setActivated(true);
				buttons[i].setTextColor(Color.rgb(217, 65, 78));
			} else {
				buttons[i].setActivated(false);
				buttons[i].setTextColor(Color.rgb(165, 165, 165));
			}
		}
	}
}
