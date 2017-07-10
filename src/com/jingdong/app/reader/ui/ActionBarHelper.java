package com.jingdong.app.reader.ui;

import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.SettingsActivity;
import com.jingdong.app.reader.bookstore.search.SearchActivity;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.message.activity.MessageActivity;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.timeline.actiivity.TimelineActivity;
import com.jingdong.app.reader.timeline.selected.activity.BooksSelectedTimelineActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;

public class ActionBarHelper {

	private MenuItem menuItem;
	private int notificationCount = 0;
	private int followersCount = 0;
	private boolean upgradeIndicator = false;
	public static final String POP_MENU_NAME = "pop_menu_name";
	public static final String POP_MENU_ACTION = "pop_menu_action";
	public static final String POP_MENU_VISIBLE = "pop_menu_visible";
	
	
	
	
	
	
	
	
	
	

	public void customActionBar(final Activity activity) {
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.LEFT);
		View customView = activity.getLayoutInflater().inflate(
				R.layout.activity_actionbar, null);
		ActionBar actionBar = activity.getActionBar();
		

		actionBar.setCustomView(customView, lp);

		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayShowCustomEnabled(true);

		final View timelineButton = customView
				.findViewById(R.id.actionbar_timeline);
		// final View booksbarsButton = customView
		// .findViewById(R.id.actionbar_booksbars);
		final View bookcaseButton = customView
				.findViewById(R.id.actionbar_bookcase);
		final View bookstoreButton = customView
				.findViewById(R.id.actionbar_bookstore);

		// if (activity instanceof BooksBarActivity) {
		// booksbarsButton.setClickable(false);
		// booksbarsButton.setBackgroundResource(R.color.actionbar_highlight);
		// bookcaseButton
		// .setBackgroundResource(R.drawable.actionbar_custom_selector);
		// bookstoreButton
		// .setBackgroundResource(R.drawable.actionbar_custom_selector);
		// }
		if (activity instanceof TimelineActivity) {
			timelineButton.setClickable(false);
			timelineButton.setBackgroundResource(R.color.actionbar_highlight);
			bookcaseButton
					.setBackgroundResource(R.drawable.actionbar_custom_selector);
			bookstoreButton
					.setBackgroundResource(R.drawable.actionbar_custom_selector);
		} 

		timelineButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO 动态/书吧
				view.setBackgroundResource(R.color.actionbar_highlight);
				bookcaseButton
						.setBackgroundResource(R.drawable.actionbar_custom_selector);
				bookstoreButton
						.setBackgroundResource(R.drawable.actionbar_custom_selector);
				Intent intent = new Intent(activity, TimelineActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				activity.startActivity(intent);
				// activity.startActivity(new Intent(activity,
				// BooksBarActivity.class));
				activity.overridePendingTransition(R.anim.fade, R.anim.hold);
				activity.finish();
			}
		});
		bookcaseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO 书架
				/*view.setBackgroundResource(R.color.actionbar_highlight);
				timelineButton
						.setBackgroundResource(R.drawable.actionbar_custom_selector);
				bookstoreButton
						.setBackgroundResource(R.drawable.actionbar_custom_selector);
				Intent intent = new Intent(activity, BookcaseActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				activity.startActivity(intent);
				activity.overridePendingTransition(R.anim.fade, R.anim.hold);
				activity.finish();*/
			}
		});
		bookstoreButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO 书城
				/*view.setBackgroundResource(R.color.actionbar_highlight);
				timelineButton
						.setBackgroundResource(R.drawable.actionbar_custom_selector);
				bookcaseButton
						.setBackgroundResource(R.drawable.actionbar_custom_selector);
				Intent intent = new Intent(activity, BookStoreActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				activity.startActivity(intent);
				activity.overridePendingTransition(R.anim.fade, R.anim.hold);
				activity.finish();*/
			}
		});
	}

	public static void customActionBarBack(final Activity activity) {
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.LEFT);
		View customView = activity.getLayoutInflater().inflate(
				R.layout.activity_actionbar_back, null);
		ActionBar actionBar = activity.getActionBar();
		actionBar.setCustomView(customView, lp);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayShowCustomEnabled(true);

		TextView text = (TextView) customView
				.findViewById(R.id.actionbar_title);
		if (activity instanceof MessageActivity) {
			text.setText(R.string.notification);
		} else if (activity instanceof SearchActivity) {
			text.setText(R.string.search);
		} else if (activity instanceof UserActivity) {
			text.setText(R.string.my_profile);
		} else if (activity instanceof SettingsActivity) {
			text.setText(R.string.settings);
		} else if (activity instanceof BooksSelectedTimelineActivity) {
			text.setText(R.string.books_selected);
		} else if (activity instanceof TimelineActivity) {
			text.setText(R.string.timeline);
		}

		customView.findViewById(R.id.actionbar_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View view) {
						activity.overridePendingTransition(R.anim.fade,
								R.anim.hold);
						activity.finish();
					}
				});
	}

	public static void customActionBarBack(final Activity activity,
			String customTitle) {
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.LEFT);
		View customView = activity.getLayoutInflater().inflate(
				R.layout.activity_actionbar_back, null);
		ActionBar actionBar = activity.getActionBar();
		actionBar.setCustomView(customView, lp);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayShowCustomEnabled(true);
	
		TextView text = (TextView) customView
				.findViewById(R.id.actionbar_title);
		text.setText(customTitle);
		customView.findViewById(R.id.actionbar_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View view) {
						activity.overridePendingTransition(R.anim.fade,
								R.anim.hold);
						activity.finish();
					}
				});
	}

	public static void customActionBarBack(final Activity activity,
			String customTitle, OnClickListener listener) {
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.LEFT);
		View customView = activity.getLayoutInflater().inflate(
				R.layout.activity_actionbar_back, null);
		ActionBar actionBar = activity.getActionBar();
		actionBar.setCustomView(customView, lp);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayShowCustomEnabled(true);

		TextView text = (TextView) customView
				.findViewById(R.id.actionbar_title);
		text.setText(customTitle);
		customView.findViewById(R.id.actionbar_back).setOnClickListener(
				listener);
	}

	public void createDropdownButton(Activity activity, Menu menu) {
		MenuInflater inflater = activity.getMenuInflater();
		inflater.inflate(R.menu.actionbar_dropdown_button, menu);
		menuItem = menu.findItem(R.id.dropdown_button);
	}

	
	public void loadNewNotification(Activity activity) {
		Notification notification = Notification.getInstance();
		notificationCount = notification.getMessageSum();
		followersCount = notification.getFollowersCount();
		int versionCode = -1;
		try {
			versionCode = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		upgradeIndicator = LocalUserSetting.getCheckUpdateFlag(activity) > versionCode;

		if (menuItem != null) {
			if (notificationCount > 0 || followersCount > 0) {
				menuItem.setIcon(R.drawable.actionbar_dropdown_red_dot);
			} else {
				menuItem.setIcon(R.drawable.navibar_icon_overflow_new);
			}
			if (upgradeIndicator) {
				menuItem.setIcon(R.drawable.actionbar_dropdown_red_dot);
			}
		}
		
		loadTimelineNotification(activity);
	}

	public void loadTimelineNotification(Activity activity) {
		View view = activity.getActionBar().getCustomView();
		View reddotView = view.findViewById(R.id.actionbar_timeline_reddot);
		int normalCount = Notification.getInstance().getNormalCount();
		if (reddotView != null) {
			if (normalCount > 0) {
				reddotView.setVisibility(View.VISIBLE);
				if (activity instanceof TimelineActivity) {
					reddotView
							.setBackgroundResource(R.drawable.actionbar_reddot_highlight);
				} else {
					reddotView
							.setBackgroundResource(R.drawable.actionbar_reddot);
				}
			} else {
				reddotView.setVisibility(View.GONE);
			}
		}
	}

	public void createPopupMenu(final Activity activity, View view) {
		DisplayMetrics metric = activity.getResources().getDisplayMetrics();
		float scale = metric.density;
		int width = (int) (190 * scale);// 下拉菜单的宽度
		int offsetY = (int) (-4 * scale);// 下拉菜单的Y轴偏移量
		int offsetX = metric.widthPixels - width - (int) (6 * scale);// 下拉菜单的X轴偏移量
																		// ＝
																		// 屏幕宽度－自身宽度－与右边框的距离
		View popupView = activity.getLayoutInflater().inflate(
				R.layout.activity_actionbar_popupmenu, null);

		if (notificationCount > 0) {
			View notificationView = popupView
					.findViewById(R.id.pop_item_notification);
			TextView notificationText = (TextView) notificationView
					.findViewById(R.id.notification_count);
			notificationText.setText(String.valueOf(notificationCount));
		}

		if (followersCount > 0) {
			View myProfileView = popupView
					.findViewById(R.id.pop_item_my_profile);
			TextView followersText = (TextView) myProfileView
					.findViewById(R.id.follow_count);
			followersText.setText("+" + followersCount);
		}
		
		popupView.findViewById(R.id.upgrade_indicator).setVisibility(upgradeIndicator ? View.VISIBLE : View.GONE);

		final PopupWindow mPopupWindow = new PopupWindow(popupView, width,
				LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable(activity
				.getResources(), (Bitmap) null));
		
		mPopupWindow.showAsDropDown(view, offsetX, offsetY);

		popupView.findViewById(R.id.pop_item_notification).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// 消息
						Intent intent = new Intent(activity,
								MessageActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						activity.startActivity(intent);
						activity.overridePendingTransition(R.anim.fade,
								R.anim.hold);
						mPopupWindow.dismiss();
					}
				});
		popupView.findViewById(R.id.pop_item_search).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// 搜索
						Intent intent = new Intent(activity,
								SearchActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						activity.startActivity(intent);
						activity.overridePendingTransition(R.anim.fade,
								R.anim.hold);
						mPopupWindow.dismiss();
					}
				});
		popupView.findViewById(R.id.pop_item_my_profile).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// 我的主页
						Intent intent = new Intent(activity, UserActivity.class);
						intent.putExtra(UserActivity.USER_ID, LoginUser.getpin());
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						activity.startActivity(intent);
						activity.overridePendingTransition(R.anim.fade,
								R.anim.hold);
						mPopupWindow.dismiss();
					}
				});
		popupView.findViewById(R.id.pop_item_settings).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// 设置
						Intent intent = new Intent(activity,
								SettingsActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						activity.startActivity(intent);
						activity.overridePendingTransition(R.anim.fade,
								R.anim.hold);
						mPopupWindow.dismiss();
					}
				});

	}

	public interface PopupItemClickListener {
		public void onPopupItemClick(String type);
	}

	PopupItemClickListener popupItemClickListener = null;

	public void setPopupItemClickListener(
			PopupItemClickListener mPopupItemClickListener) {
		popupItemClickListener = mPopupItemClickListener;
	}

	public void createArrangePopupMenu(final Activity activity, View view) {
		DisplayMetrics metric = activity.getResources().getDisplayMetrics();
		float scale = metric.density;
		int width = (int) (190 * scale);// 下拉菜单的宽度
		int offsetY = (int) (-4 * scale);// 下拉菜单的Y轴偏移量
		int offsetX = metric.widthPixels - width - (int) (6 * scale);// 下拉菜单的X轴偏移量
																		// ＝
																		// 屏幕宽度－自身宽度－与右边框的距离
		View popupView = activity.getLayoutInflater().inflate(
				R.layout.activity_arrange_actionbar_popupmenu, null);

		final PopupWindow mPopupWindow = new PopupWindow(popupView, width,
				LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable(activity
				.getResources(), (Bitmap) null));
		mPopupWindow.showAsDropDown(view, offsetX, offsetY);

		popupView.findViewById(R.id.putintofolder).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// 放入文件夹

						popupItemClickListener
								.onPopupItemClick("put_into_folder");

						activity.overridePendingTransition(R.anim.fade,
								R.anim.hold);
						mPopupWindow.dismiss();
					}
				});
		popupView.findViewById(R.id.uptocloud).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// 上传至云盘

						popupItemClickListener.onPopupItemClick("upto_cloud");

						activity.overridePendingTransition(R.anim.fade,
								R.anim.hold);
						mPopupWindow.dismiss();
					}
				});
		popupView.findViewById(R.id.sharebymsg).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// 私信分享

						popupItemClickListener.onPopupItemClick("share_by_msg");

						activity.overridePendingTransition(R.anim.fade,
								R.anim.hold);
						mPopupWindow.dismiss();
					}
				});
		popupView.findViewById(R.id.sharebywechat).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// 微信分享

						popupItemClickListener
								.onPopupItemClick("share_by_wechat");

						activity.overridePendingTransition(R.anim.fade,
								R.anim.hold);
						mPopupWindow.dismiss();
					}
				});

	}

	public void createCustomPopupMenu(final Activity activity, View view,
			List<Map<String, String>> list, boolean isNeedShow, int[] position) {// position
																					// 指定要显示的菜单在list中的位置
																					// 不指定默认全部显示

		DisplayMetrics metric = activity.getResources().getDisplayMetrics();
		float scale = metric.density;
		int width = (int) (190 * scale);// 下拉菜单的宽度
		int offsetY = (int) (-4 * scale);// 下拉菜单的Y轴偏移量
		int offsetX = metric.widthPixels - width - (int) (6 * scale);// 下拉菜单的X轴偏移量
																		// ＝
																		// 屏幕宽度－自身宽度－与右边框的距离

		View popupView = activity.getLayoutInflater().inflate(
				R.layout.actionbar_common_popupmenu, null);
		LinearLayout contentView = (LinearLayout) popupView
				.findViewById(R.id.content);
		final PopupWindow mPopupWindow = new PopupWindow(popupView, width,
				LayoutParams.WRAP_CONTENT, true);

		if (position != null && position.length > 0) {

			for (int i = 0; i < list.size(); i++) {

				for (int j : position) {
					if (j == i) {
						View childview = LayoutInflater.from(activity).inflate(
								R.layout.actionbar_common_popupmenu_item, null);

						TextView textView = (TextView) childview
								.findViewById(R.id.pop_item_name);

						textView.setText(list.get(i).get(POP_MENU_NAME));

						final String action = list.get(i).get(POP_MENU_ACTION);

						childview.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								LocalBroadcastManager.getInstance(activity)
										.sendBroadcast(new Intent(action));
								if (mPopupWindow != null)
									mPopupWindow.dismiss();
							}
						});
						contentView.addView(childview);

						break;
					}
				}

			}

		} else {
			for (int i = 0; i < list.size(); i++) {

				View childview = LayoutInflater.from(activity).inflate(
						R.layout.actionbar_common_popupmenu_item, null);

				TextView textView = (TextView) childview
						.findViewById(R.id.pop_item_name);

				textView.setText(list.get(i).get(POP_MENU_NAME));

				final String action = list.get(i).get(POP_MENU_ACTION);

				childview.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						LocalBroadcastManager.getInstance(activity)
								.sendBroadcast(new Intent(action));
						if (mPopupWindow != null)
							mPopupWindow.dismiss();
					}
				});
				contentView.addView(childview);

			}
		}

		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable(activity
				.getResources(), (Bitmap) null));

		if (isNeedShow)
			mPopupWindow.showAsDropDown(view, offsetX, offsetY);

	}

}
