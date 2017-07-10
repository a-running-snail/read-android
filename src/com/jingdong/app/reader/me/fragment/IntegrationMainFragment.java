package com.jingdong.app.reader.me.fragment;

import java.util.Collections;

import org.apache.http.Header;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.me.activity.IntegrationActivity;
import com.jingdong.app.reader.me.adapter.ScoreIndexListAdapter;
import com.jingdong.app.reader.me.model.MyScoreIndex;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.me.model.SignSuccessionResult;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 积分页主界面Fragment
 * 
 * @author J.Beyond
 *
 */
public class IntegrationMainFragment extends BackHandledFragment implements TopBarViewListener {

	private static final int SHOW_LOADING = 0x01;
	private static final int SHOW_NORMAL = 0x02;
	private static final int SHOW_NET_ERROR = 0x03;
	private RoundNetworkImageView mHeaderView;
//	private TextView mNickName;
	private TextView mCurrentScore;
	private TextView mSignTV;
	private TextView mScoreRecordTv;
	private TextView mExchangeDescTv;
	private ListView mIntegrationLv;
	private TopBarView topBarView;
	private FragmentManager mFmg;
	private ScoreIndexListAdapter mAdapter;
	private SwitchPageListener mListener;
	private Activity mActivity;
	/** 签到中标示 */
	private boolean signing = false;

	public interface SwitchPageListener {
		void switchToRecord();
		void switchToExchangeInfo();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = activity;
		try {
			mListener = (SwitchPageListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + "must implement switchPageListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.integration_main_fragment, null);
		initView(layout);
//		initData();
		return layout;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// 设置头像
//		String imgUrl = (String) getArguments().get("HEADER_IMAGE_URL");
		String imgUrl = LocalUserSetting.getUserHeaderUrl();
		ImageLoader.getInstance().displayImage(imgUrl, mHeaderView, GlobalVarable.getDefaultAvatarDisplayOptions(false));
		initData();
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("com.jdread.action.exchange")) {
				MZLog.d("J", "onReceive action::com.jdread.action.exchange");
				initData();
			}
		};
	};
	private View mNormalContentView;
	private View mAwardLotteryView;
	private EmptyLayout mEmptyLayout;
	private TextView mNextGiftTV;
	private View mHeaderLl;
	private View mHeaderRl;

	private void initData() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.jdread.action.exchange");
		LocalBroadcastManager.getInstance(mActivity).registerReceiver(mReceiver, filter);

		if (!NetWorkUtils.isNetworkAvailable(mActivity)) {
			Toast.makeText(mActivity, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			return;
		}
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getIntegrationParams(), true, new MyAsyncHttpResponseHandler(
				mActivity) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
//				Toast.makeText(mActivity, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				String result = new String(responseBody);

				MZLog.d("J", "onResponse=======>>" + result);
				MyScoreIndex myScore = GsonUtils.fromJson(result, MyScoreIndex.class);
				
				if (myScore != null && myScore.getCode().equals("0")) {
					mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
					setupUI(myScore);
				} else {
					MZLog.e("J", "获取积分首页数据失败");
					mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
				}
			}
		});
	}

	protected void setupUI(MyScoreIndex myScore) {
		if (myScore == null) {
			MZLog.e("J", "MyScoreIndex is null");
			CustomToast.showToast(mActivity, "网络数据异常");
			return;
		}
		System.out.println("setupUI::"+GsonUtils.toJson(myScore));
		//设置当前积分
		setupScoreInfo(myScore);
		
		if (myScore.isLottery()) {
			mAwardLotteryView.setVisibility(View.VISIBLE);
			mAwardLotteryView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mActivity,WebViewActivity.class);
					intent.putExtra(WebViewActivity.UrlKey, "http://e.m.jd.com/score_lottery.html");
					intent.putExtra(WebViewActivity.BrowserKey, false);
					intent.putExtra(WebViewActivity.TitleKey,
							getString(R.string.extra_lottery));
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			});
		}else{
			mAwardLotteryView.setVisibility(View.GONE);
		}
		mAdapter = new ScoreIndexListAdapter(mActivity, myScore);
		mIntegrationLv.setAdapter(mAdapter);

	}
	

	/**
	 * 
	 * @Title: setupNextGiftLabel
	 * @Description: 设置距离下一礼包
	 * @param @param myScore
	 * @return void
	 * @throws
	 */
	private void setupScoreInfo(MyScoreIndex myScore) {
		//设置当前积分
		String currentScore = "当前积分：" + myScore.getScoreTotal();
		SpannableString scoreStr = new SpannableString(currentScore);
		int start = 5;
		int end = 5+String.valueOf(myScore.getScoreTotal()).length();
		scoreStr.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		scoreStr.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		mCurrentScore.setText(scoreStr);
		
		//设置距离下一个礼包天数、连续签到天数
		SignSuccessionResult successionResult = myScore.getSignSuccessionResult();
		if (successionResult == null) return;
		boolean isSignSuccession = successionResult.isSignSuccession();
		if (isSignSuccession) {
			mNextGiftTV.setVisibility(View.VISIBLE);
			String nextGiftInfo = "距离下一个礼包还有"+successionResult.getSignSuccessionMsg()+"天";
			SpannableString spanStr = new SpannableString(nextGiftInfo);
			int start2 = 9;
			int end2 = start2+String.valueOf(successionResult.getSignSuccessionMsg()).length();
			spanStr.setSpan(new ForegroundColorSpan(Color.RED), start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			mNextGiftTV.setText(spanStr);
		}else{
			mNextGiftTV.setVisibility(View.VISIBLE);
			mNextGiftTV.setText("连续签到送惊喜");
		}
		
		if (!myScore.isSign()) {//已签到
			if (isSignSuccession) {
				int signSuccessionTimes = successionResult.getSignSuccessionTimes();
				String signInfo = "已连续签到"+signSuccessionTimes+"天";
				SpannableString spanStr = new SpannableString(signInfo);
				spanStr.setSpan(new ForegroundColorSpan(Color.RED), 5, 5+String.valueOf(signSuccessionTimes).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				mSignTV.setText(spanStr);
			}else{
				mSignTV.setText("已签到");
			}
			mSignTV.setTextColor(mActivity.getResources().getColor(R.color.r_text_disable));
			Drawable doneDrawable =mActivity.getResources().getDrawable(R.drawable.integration_icon_points_done);  
			doneDrawable.setBounds(0, 0, doneDrawable.getMinimumWidth(), doneDrawable.getMinimumHeight());  
			mSignTV.setCompoundDrawables(null, doneDrawable, null, null);
		} else {
			if (isSignSuccession) {
//				mNextGiftTV.setText("连续签到送惊喜");
				String nextGiftInfo = "距离下一个礼包还有"+successionResult.getSignSuccessionMsg()+"天";
				SpannableString spanStr = new SpannableString(nextGiftInfo);
				int start2 = 9;
				int end2 = start2+String.valueOf(successionResult.getSignSuccessionMsg()).length();
				spanStr.setSpan(new ForegroundColorSpan(Color.RED), start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				mNextGiftTV.setText(spanStr);
			}
			mSignTV.setTextColor(mActivity.getResources().getColor(R.color.highlight_color));
			mSignTV.setText("签到领积分");
			Drawable moneyDrawable = mActivity.getResources().getDrawable(R.drawable.integration_icon_points_money);  
			moneyDrawable.setBounds(0, 0, moneyDrawable.getMinimumWidth(), moneyDrawable.getMinimumHeight());  
			mSignTV.setCompoundDrawables(null, moneyDrawable, null, null);  
			mSignTV.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					doSign();
				}
			});
		}
	}


	/**
	 * 
	 * @Title: doSign
	 * @Description: 执行签到
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年5月16日 下午12:10:58
	 */
	private void doSign() {
		if (signing) {
			return;
		}
		
		signing = true;
		IntegrationAPI.signGetScore(mActivity, true, new GrandScoreListener() {

			@Override
			public void onGrandSuccess(SignScore score) {
				System.out.println("doSign::"+GsonUtils.toJson(score));
				SignSuccessionResult successionResult = score.getSignSuccessionResult();
				if (successionResult.getSignSuccessionGiftId() >0 ) {
					String msg = null;
					if (successionResult.isSignSuccessionGiftSuccess()) {//获取奖品成功
						msg = "签到获得"+score.getGetScore()+"积分，连续签到奖励"+successionResult.getSignSuccessionGiftMsg()+"(京豆、优惠券可到钱包中查询)";
					}else{
//						msg = "签到获得"+score.getGetScore()+"积分，抱歉"+successionResult.getSignSuccessionGiftMsg();
						String scoreInfo = "恭喜你签到获得" + score.getGetScore() + "积分";
						SpannableString span = new SpannableString(scoreInfo);
						int start = 7;
						int end = start+String.valueOf(score.getGetScore()).length();
						span.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						CustomToast.showToast(mActivity, span);
						return;
					}
					
					CustomToast.showToast(mActivity, msg);
				}else{
					// 土司提示
					String scoreInfo = "恭喜你签到获得" + score.getGetScore() + "积分";
					SpannableString span = new SpannableString(scoreInfo);
					int start = 7;
					int end = start+String.valueOf(score.getGetScore()).length();
					span.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					CustomToast.showToast(mActivity, span);
				}

				// 刷新数据
				initData();
				signing = false;
			}

			@Override
			public void onGrandFail() {
				signing = false;
//				MZLog.e("J", "onGrandFail,code=" + code);
			}
		});
	}
	
	private void updateScore() {
		
	}

	private void initView(View layout) {
		topBarView = (TopBarView) layout.findViewById(R.id.topbar);
		mNormalContentView = layout.findViewById(R.id.normal_contentview);
		mHeaderView = (RoundNetworkImageView) layout.findViewById(R.id.integration_thumb_nail);
		mEmptyLayout = (EmptyLayout) layout.findViewById(R.id.error_layout);
		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				initData();
			}
		});
		mHeaderLl = layout.findViewById(R.id.header_top_ll);
		mHeaderRl = layout.findViewById(R.id.header_top_rl);
		
		mCurrentScore = (TextView) layout.findViewById(R.id.integration_current_score);
		mSignTV = (TextView) layout.findViewById(R.id.sign_to_get_scrore_tv);
		mNextGiftTV = (TextView) layout.findViewById(R.id.next_gift_time_interval);
		
		mScoreRecordTv = (TextView) layout.findViewById(R.id.integration_record_tv);
		mExchangeDescTv = (TextView) layout.findViewById(R.id.exchange_description_tv);
		mIntegrationLv = (ListView) layout.findViewById(R.id.integration_main_list);
		mAwardLotteryView = layout.findViewById(R.id.award_lottery_tv);
		mFmg = getFragmentManager();
		initTopbarView();
		initEvent();
	}

	private void initEvent() {
		// 积分记录
		mScoreRecordTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.switchToRecord();
				}
			}
		});

		// 兑换说明
		mExchangeDescTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.switchToExchangeInfo();
				}
			}
		});

	}
	

	private void initTopbarView() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setListener(this);
		topBarView.setTitle("积分");
	}

	@Override
	public void onLeftMenuClick() {
		// if (mFmg.getBackStackEntryCount() ==1) {
		// mFmg.popBackStack();
		// } else {
		// mActivity.finish();
		// }
		if (mActivity != null) {
			((IntegrationActivity) mActivity).onBackPressed();
		}

	}

	@Override
	public void onCenterMenuItemClick(int position) {

	}

	@Override
	public void onRightMenuOneClick() {

	}

	@Override
	public void onRightMenuTwoClick() {

	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

}
