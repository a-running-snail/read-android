package com.jingdong.app.reader.bookshelf;

import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.me.activity.IntegrationActivity;
import com.jingdong.app.reader.me.activity.ReadingDataChartActivity;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.me.model.SignSuccessionResult;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.dialog.DialogManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

/**
 * 签到提示弹出对话框
 * @author xuhongwei
 *
 */
public class SignTipDialog {
	private Context mContext;
	private SignScore mSignScore;
	
	public SignTipDialog(Context context, SignScore score) {
		this.mContext = context;
		this.mSignScore = score;
		if(null != score.getSignTypeData()) {
			DialogManager.showCommonDialog(mContext, "签到成功！", score.getSignTypeData().getMsg(), score.getSignTypeData().getButtonText(), "取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						jump();
						break;
					case DialogInterface.BUTTON_NEGATIVE:

						break;
					default:
						break;
					}
					dialog.dismiss();
				}

			});
		}else {
			SignSuccessionResult successionResult = score.getSignSuccessionResult();
			//连续签到活动已开启，显示弹窗
			if (successionResult!=null && successionResult.isSignSuccession()) {
				String msg = null;
				if (successionResult.getSignSuccessionGiftId() > 0) {//有奖品
					if (successionResult.isSignSuccessionGiftSuccess()) {//获取奖品成功
						msg = "签到获得"+score.getGetScore()+"积分，连续签到奖励"+successionResult.getSignSuccessionGiftMsg()+"(京豆、优惠券可去京东主站查询)";
					}else{
						msg = "签到获得"+score.getGetScore()+"积分，抱歉"+successionResult.getSignSuccessionGiftMsg();
					}
				}else{
					msg = "签到获得"+score.getGetScore()+"积分，连续签到送惊喜！";
				}
				
				if (mContext instanceof Activity) {
					Activity a = (Activity)mContext;
					if(a.isFinishing()) {
						return;
					}
				}
				
				DialogManager.showCommonDialog(mContext, "签到成功！", msg, "去看看", "取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							Intent intent = new Intent(mContext,IntegrationActivity.class);
							((Activity)mContext).startActivity(intent);
							break;
						case DialogInterface.BUTTON_NEGATIVE:

							break;
						default:
							break;
						}
						dialog.dismiss();
					}

				});
				
			}else{
				String scoreInfo = "恭喜你签到获得" + score.getGetScore() + "积分";
				SpannableString span = new SpannableString(scoreInfo);
				int start = 7;
				int end = start+String.valueOf(score.getGetScore()).length();
				span.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				CustomToast.showToast(mContext, span);
			}
		}
		
	}
	
	/**
	 * 页面跳转
	 */
	private void jump() {
		int targetVCType = mSignScore.getSignTypeData().getTargetVCType();
		int targetSubType = mSignScore.getSignTypeData().getTargetSubType();
		if(1 == targetVCType) { //书城
			if(5 == targetSubType) { //书城子模块
				Intent intent = new Intent(mContext, BookStoreBookListActivity.class);
				intent.putExtra("fid", mSignScore.getSignTypeData().getTargetSubParams().getId());
				intent.putExtra("ftype", 2);
				intent.putExtra("relationType", 1);
				intent.putExtra("relateLink", mSignScore.getSignTypeData().getTargetSubParams().getRelateLink());
				intent.putExtra("showName", mSignScore.getSignTypeData().getTargetSubParams().getShowName());
				intent.putExtra("bannerImg",mSignScore.getSignTypeData().getTargetSubParams().getPicAddressAll());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				((Activity)mContext).startActivity(intent);
			}else { //书城
				Intent intent = new Intent(mContext, LauncherActivity.class);
				intent.putExtra("TAB_INDEX", 0);
				intent.putExtra("BOOK_STORE_INDEX", 100);
				((Activity)mContext).startActivity(intent);
			}
		}else if(2 == targetVCType) { //社区
			Intent intent = new Intent(mContext, LauncherActivity.class);
			intent.putExtra("TAB_INDEX", 2);
			((Activity)mContext).startActivity(intent);
		}else if(3 == targetVCType) { //我
			if(1 == targetSubType) { //积分
				Intent intent = new Intent(mContext, IntegrationActivity.class);
				((Activity)mContext).startActivity(intent);
			}else if(2 == targetSubType) { //阅历
				Intent intent = new Intent(mContext, ReadingDataChartActivity.class);
				((Activity)mContext).startActivity(intent);
			}else { //我
				Intent intent = new Intent(mContext, LauncherActivity.class);
				intent.putExtra("TAB_INDEX", 3);
				((Activity)mContext).startActivity(intent);
			}
		}else if(4 == targetVCType) { //url
			Intent intent = new Intent(mContext, WebViewActivity.class);
			String webUrl = mSignScore.getSignTypeData().getUrl();
			intent.putExtra(WebViewActivity.UrlKey, webUrl);
			intent.putExtra(WebViewActivity.TopbarKey, true);
			intent.putExtra(WebViewActivity.BrowserKey, false);
			intent.putExtra(WebViewActivity.TitleKey, mSignScore.getSignTypeData().getTargetSubParams().getShowName());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			((Activity)mContext).startActivity(intent);
		}else {
			Intent intent = new Intent(mContext,IntegrationActivity.class);
			((Activity)mContext).startActivity(intent);	
		}
	}

}
