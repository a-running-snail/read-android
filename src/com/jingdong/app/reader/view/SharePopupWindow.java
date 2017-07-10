package com.jingdong.app.reader.view;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.bookstore.buyborrow.UserBuyBorrowActivity;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.share.ShareResultListener;
import com.jingdong.app.reader.util.share.WBShareHelper;
import com.jingdong.app.reader.util.share.WXShareHelper;
import com.sina.weibo.sdk.api.TextObject;

public class SharePopupWindow {

	// private static final int THUMB_SIZE = 150;
	private String titleString;
	private String discriptString;
	private String urlpathString;
	private String urlString;
	private Activity mActivity = null;
	private boolean isShowing = false;
	private PopupWindow mPopupWindow = null;
	private RelativeLayout popup_weixin;// 微信好友
	private RelativeLayout popup_sina;// 新浪微博
	/** 微博微博分享接口实例 */
	// private IWeiboShareAPI mWeiboShareAPI = null;
	//
	public static final String WXAPP_ID = "wx79f9198071040f23";
	// private IWXAPI api;
	// private Bitmap bitmap;
	private RelativeLayout popup_weixin_friend;// 微信朋友圈
	private LinearLayout cancelLayout;// 取消
	private int height;
	private View popupView;
	private RelativeLayout popuplayout;
	private RelativeLayout popup_more;
	private RelativeLayout popup_community;
	
	private LinearLayout communityLayout = null;
	private LinearLayout moreLayout = null;

	public interface onPopupWindowItemClickListener {
		public void onPopupWindowWeixinClick();

		public void onPopupWindowSinaClick();

		public void onPopupWindowWeixinFriend();

		public void onPopupWindowCancel();

		public void onPopupWindowMore();

		public void onPopupWindowCommuity();
	}

	public onPopupWindowItemClickListener listener;

	public onPopupWindowItemClickListener getListener() {
		return listener;
	}

	public void setListener(onPopupWindowItemClickListener listener) {
		this.listener = listener;
	}

	public SharePopupWindow(Activity activity) {
		this.mActivity = activity;
		initView();
	}

	public SharePopupWindow(Activity activity, boolean unableWindow) {// 不需要弹出框
		this.mActivity = activity;
	}

	public void initView() {

		popupView = mActivity.getLayoutInflater().inflate(R.layout.share_popup_book, null);
		popuplayout = (RelativeLayout) popupView.findViewById(R.id.popuplayout);

		DisplayMetrics dm = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		height = dm.heightPixels;

		mPopupWindow = new PopupWindow(popupView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(mActivity.getResources().getColor(R.color.bg_menu_shadow)));

		popup_weixin = (RelativeLayout) popupView.findViewById(R.id.popup_weixin);
		popup_sina = (RelativeLayout) popupView.findViewById(R.id.popup_sina);
		popup_weixin_friend = (RelativeLayout) popupView.findViewById(R.id.popup_weixin_friend);
		popup_community = (RelativeLayout) popupView.findViewById(R.id.popup_community);
		popup_more = (RelativeLayout) popupView.findViewById(R.id.popup_more);
		cancelLayout = (LinearLayout) popupView.findViewById(R.id.cancel);
		
		communityLayout = (LinearLayout) popupView.findViewById(R.id.communityLayout);
		moreLayout = (LinearLayout) popupView.findViewById(R.id.moreLayout);

		popup_community.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (listener != null)
					listener.onPopupWindowCommuity();
			}
		});

		popup_weixin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (listener != null)
					listener.onPopupWindowWeixinClick();
			}

		});

		popup_sina.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (listener != null)
					listener.onPopupWindowSinaClick();
			}

		});

		popup_weixin_friend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (listener != null)
					listener.onPopupWindowWeixinFriend();
			}
		});

		popup_more.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (listener != null) {
					listener.onPopupWindowMore();
				}
			}
		});

		cancelLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				listener.onPopupWindowCancel();
			}
		});
	}

	public void More(Activity context, final String title, final String description, final String url) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		// sendIntent.putExtra(Intent.EXTRA_TITLE, title);
		sendIntent.putExtra(Intent.EXTRA_TEXT, title + description + url);
		sendIntent.setType("text/plain");
		// sendIntent.setData(Uri.parse(url));
		// 允许启动新的Activity
		sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Intent chooserIntent = Intent.createChooser(sendIntent, "分享到");
		if (chooserIntent == null) {
			return;
		}
		try {
			context.startActivity(chooserIntent);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(context, "Can't find share component to share", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 
	 * @Title: shareToWeixin
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @param context
	 * @param @param title
	 * @param @param description
	 * @param @param imageUrl
	 * @param @param url
	 * @param @param flag
	 * @return void
	 * @throws
	 */
	public void shareToWeixin(Activity context, String title, String description, String imageUrl, String url, int flag) {
		MZLog.d("J", "shareToWeixin-->title::" + title + "\n description::" + description + "\n url::" + url + "\n flag::" + flag);
		WXShareHelper.getInstance().doShare(context, title, description, imageUrl, url, flag, new ShareResultListener() {

			@Override
			public void onShareRusult(int resultType) {
				switch (resultType) {
				case ShareResultListener.SHARE_SUCCESS:
					Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();
					shareToGetScore();
					break;
				case ShareResultListener.SHARE_CANCEL:
					Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();

					break;
				case ShareResultListener.SHARE_FAILURE:
					Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();

					break;
				}
				dismiss();

			}
		});
	}
	
	/**
	 * 
	 * @Title: shareToWeixin
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @param context
	 * @param @param title
	 * @param @param description
	 * @param @param imageUrl
	 * @param @param url
	 * @param @param flag
	 * @param @param from 来源
	 * @return void
	 * @throws
	 */
	public void shareToWeixin(Activity context, String title, String description, String imageUrl, String url, int flag, final int from, final String ebookid) {
		MZLog.d("J", "shareToWeixin-->title::" + title + "\n description::" + description + "\n url::" + url + "\n flag::" + flag);
		WXShareHelper.getInstance().doShare(context, title, description, imageUrl, url, flag, new ShareResultListener() {

			@Override
			public void onShareRusult(int resultType) {
				switch (resultType) {
				case ShareResultListener.SHARE_SUCCESS:
					if(0 == from) { //用户借阅分享
						shareBorrowGetScore(ebookid);
					}else {
						Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();
						shareToGetScore();
					}
					break;
				case ShareResultListener.SHARE_CANCEL:
					Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();

					break;
				case ShareResultListener.SHARE_FAILURE:
					Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();

					break;
				}
				dismiss();

			}
		});

	}
	
	public void shareToWeixin(Activity context, String title, String description, Bitmap bitmap, String url, int flag, final int from, final String ebookid) {
		MZLog.d("J", "shareToWeixin-->title::" + title + "\n description::" + description + "\n url::" + url + "\n flag::" + flag);
		WXShareHelper.getInstance().doShare(context, title, description, bitmap, url, flag, new ShareResultListener() {

			@Override
			public void onShareRusult(int resultType) {
				switch (resultType) {
				case ShareResultListener.SHARE_SUCCESS:
					if(0 == from) { //用户借阅分享
						shareBorrowGetScore(ebookid);
					}else {
						Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();
						shareToGetScore();
					}
					break;
				case ShareResultListener.SHARE_CANCEL:
					Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();

					break;
				case ShareResultListener.SHARE_FAILURE:
					Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();

					break;
				}
				dismiss();

			}
		});

	}

	public void shareToWeiboByBitmap(Activity context, String title, String description, Bitmap bitmap, String url, String defaluttext) {
		WBShareHelper.getInstance().doShareByBitmap(context, title, description, bitmap, url, defaluttext, new ShareResultListener() {

			@Override
			public void onShareRusult(int resultType) {
				switch (resultType) {
				case ShareResultListener.SHARE_SUCCESS:
					Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();
					shareToGetScore();
					break;
				case ShareResultListener.SHARE_CANCEL:
					Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();

					break;
				case ShareResultListener.SHARE_FAILURE:
					Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();
					break;
				}
				dismiss();
			}
		});
	}

	/**
	 * 
	 * @Title: shareToWeibo
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @param context
	 * @param @param title:微博正文
	 * @param @param description：微博描述（可不传）
	 * @param @param imageUrl：图片URL
	 * @param @param url：链接
	 * @param @param defaluttext
	 * @return void
	 * @throws
	 */
	public void shareToWeibo(Activity context, String title, String description, String imageUrl, String url, String defaluttext) {
		MZLog.d("J", "shareToWeibo -->title::" + title + "\n description::" + description + "\n url::" + url + "\n defaluttext::" + defaluttext);

		WBShareHelper.getInstance().doShare(context, title, description, imageUrl, url, defaluttext, new ShareResultListener() {

			@Override
			public void onShareRusult(int resultType) {
				switch (resultType) {
				case ShareResultListener.SHARE_SUCCESS:
					Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();
					shareToGetScore();
					break;
				case ShareResultListener.SHARE_CANCEL:
					Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();

					break;
				case ShareResultListener.SHARE_FAILURE:
					Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();
					break;
				}
				dismiss();
			}
		});

	}
	
	public void shareToWeibo(Activity context, String title, String description, String imageUrl, String url, String defaluttext, final int from, final String ebookid) {
		MZLog.d("J", "shareToWeibo -->title::" + title + "\n description::" + description + "\n url::" + url + "\n defaluttext::" + defaluttext);

		WBShareHelper.getInstance().doShare(context, title, description, imageUrl, url, defaluttext, new ShareResultListener() {

			@Override
			public void onShareRusult(int resultType) {
				switch (resultType) {
				case ShareResultListener.SHARE_SUCCESS:
					if(0 == from) { //用户借阅分享
						shareBorrowGetScore(ebookid);
					}else {
						Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();
						shareToGetScore();
					}
					break;
				case ShareResultListener.SHARE_CANCEL:
					Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();

					break;
				case ShareResultListener.SHARE_FAILURE:
					Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();
					break;
				}
				dismiss();
			}
		});

	}
	
	public void shareToWeibo(Activity context, String title, String description, Bitmap bitmap, String url, String defaluttext, final int from, final String ebookid) {
		MZLog.d("J", "shareToWeibo -->title::" + title + "\n description::" + description + "\n url::" + url + "\n defaluttext::" + defaluttext);

		WBShareHelper.getInstance().doShare(context, title, description, bitmap, url, defaluttext, new ShareResultListener() {

			@Override
			public void onShareRusult(int resultType) {
				switch (resultType) {
				case ShareResultListener.SHARE_SUCCESS:
					if(0 == from) { //用户借阅分享
						shareBorrowGetScore(ebookid);
					}else {
						Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();
						shareToGetScore();
					}
					break;
				case ShareResultListener.SHARE_CANCEL:
					Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();

					break;
				case ShareResultListener.SHARE_FAILURE:
					Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();
					break;
				}
				dismiss();
			}
		});

	}

	public void dismiss() {
		if (mPopupWindow != null) {
			AnimationSet animTop = new AnimationSet(true);
			TranslateAnimation transTop = new TranslateAnimation(0, 0, 0, height);
			// 设置动画效果持续的时间
			animTop.setDuration(500); // transTop
			// 将anim对象添加到AnimationSet对象中
			animTop.addAnimation(transTop);
			animTop.setFillAfter(false);
			popupView.startAnimation(animTop);
			mPopupWindow.dismiss();
			setShowing(false);
			MZLog.d("cj", "dismiss=========>>");
		}
	}

	public void show(View v) {
		if (mPopupWindow != null) {
			AnimationSet animTop = new AnimationSet(true);
			TranslateAnimation transTop = new TranslateAnimation(0, 0, height, 0);
			// 设置动画效果持续的时间
			animTop.setDuration(500); // transTop
			// 将anim对象添加到AnimationSet对象中
			animTop.addAnimation(transTop);
			animTop.setFillAfter(true);
			popupView.startAnimation(animTop);
			mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
			setShowing(true);
			MZLog.d("cj", "show=========>>");
		}

	}
	
	public void show(View v, boolean hideMore) {
		if (mPopupWindow != null) {
			AnimationSet animTop = new AnimationSet(true);
			TranslateAnimation transTop = new TranslateAnimation(0, 0, height, 0);
			// 设置动画效果持续的时间
			animTop.setDuration(500); // transTop
			// 将anim对象添加到AnimationSet对象中
			animTop.addAnimation(transTop);
			animTop.setFillAfter(true);
			popupView.startAnimation(animTop);
			mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
			setShowing(true);
			MZLog.d("cj", "show=========>>");
		}
		
		if (hideMore) {
			communityLayout.setVisibility(View.GONE);
			moreLayout.setVisibility(View.GONE);
		}

	}

	public boolean isShowing() {
		return isShowing;
	}

	public void setShowing(boolean isShowing) {
		this.isShowing = isShowing;
	}

	private TextObject getTextObj(String text) {
		TextObject textObject = new TextObject();
		textObject.text = "ddd";
		return textObject;
	}

	// 分享成功赠送积分
	private void shareToGetScore() {
		IntegrationAPI.shareGetScore(mActivity, new GrandScoreListener() {

			@Override
			public void onGrandSuccess(SignScore score) {
				// String content = "分享成功，恭喜您获得" + score + "积分";
				// CustomToast.showToast(mActivity, content);
				String scoreInfo = "分享成功，恭喜您获得" + score.getGetScore() + "积分";
				SpannableString span = new SpannableString(scoreInfo);
				int start1 = 10;
				int end1 = start1 + String.valueOf(score.getGetScore()).length();
				span.setSpan(new ForegroundColorSpan(Color.RED), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				CustomToast.showToast(mActivity, span);
			}

			@Override
			public void onGrandFail() {
			}
		});
	}
	
	/**
	* @Description: 分享借阅获取积分
	* @author xuhongwei1
	* @date 2015年12月9日 下午5:16:30 
	* @throws 
	*/ 
	private void shareBorrowGetScore(String ebookid) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, 
				RequestParamsPool.getBorrowShareGetScoreParams(ebookid), 
				true, new MyAsyncHttpResponseHandler(mActivity) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				try {
					JSONObject json = new JSONObject(new String(responseBody));
					if(null != json) {
						if("0".equals(json.optString("code"))) {
							int getScore = json.optInt("getScore");
							if(getScore > 0) {
								String scoreInfo = "分享成功，恭喜您获得" + getScore + "积分";
								SpannableString span = new SpannableString(scoreInfo);
								int start1 = 10;
								int end1 = start1 + String.valueOf(getScore).length();
								span.setSpan(new ForegroundColorSpan(Color.RED), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
								span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
								CustomToast.showToast(mActivity, span);
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
