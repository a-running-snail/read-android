package com.jingdong.app.reader.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.download.test.DownloadTestActivity;
import com.jingdong.app.reader.entity.extra.Monthly;
import com.jingdong.app.reader.entity.extra.MonthlyList;
import com.jingdong.app.reader.entity.extra.ReadCardInfo;
import com.jingdong.app.reader.entity.extra.ReadingCard;
import com.jingdong.app.reader.entity.extra.UserInfo;
import com.jingdong.app.reader.extension.giftbag.GiftBagUtil;
import com.jingdong.app.reader.me.activity.FeedBackActivity;
import com.jingdong.app.reader.me.activity.IntegrationActivity;
import com.jingdong.app.reader.me.activity.MessageCenterActivity;
import com.jingdong.app.reader.me.activity.ModifyUserInfoActivity;
import com.jingdong.app.reader.me.activity.OrderActivity;
import com.jingdong.app.reader.me.activity.ReadingCardActivity;
import com.jingdong.app.reader.me.activity.ReadingDataChartActivity;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.FansActivity;
import com.jingdong.app.reader.timeline.actiivity.FocusActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ActivityUtils;
import com.jingdong.app.reader.util.DateUtil;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MeRootFragment extends CommonFragment implements TopBarViewListener {
	private TopBarView topBarView = null;
	private RoundNetworkImageView avatar;
	private RelativeLayout readingDataLayout;// 阅历
	private RelativeLayout balanceLayout;// 余额
	private RelativeLayout orderLayout;// 订单
	private String nickName;
	private String imgUrl;
	private View layout;
	private RelativeLayout user_read_left;// 喜欢
	private RelativeLayout feedback_layout;// 反馈
	private RelativeLayout changdu_card;// 畅读卡
	private RelativeLayout haveBuyLayout;// 已购列表
	private RelativeLayout changduLayout;// 畅读列表
	private List<MonthlyList> monthlyList;
	private List<ReadCardInfo> readCardList;
	private List<Date> dateList;
	private LinearLayout focus_people_layout;
	private LinearLayout fans_layout;
	private TextView focus_num;
	private TextView fans_num;
	private String following_count;
	private String follower_count;
	private String current_user_id;
	private RelativeLayout drafts_layout;// 草稿箱
	private ImageView fans_notification;
	public static Notification notification = Notification.getInstance();

	private TextView mOutOfDateInfoTv;
	private TextView mLeftMoneyInfoTv;
	private TextView changdu_tv;
	private RelativeLayout mIntegrationRL;
	private TextView mIntegrationInfoTv;
	private View mHeaderCover;
	private TextView nameTv;
	
	public MeRootFragment() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View root = inflater.inflate(R.layout.activity_root_layout, null);
		topBarView = (TopBarView) root.findViewById(R.id.topbar);
		initTopbarView();
		LinearLayout temp = (LinearLayout) root.findViewById(R.id.container);
		layout = inflater.inflate(R.layout.meroot, temp);
		mHeaderCover = layout.findViewById(R.id.header_cover);
		avatar = (RoundNetworkImageView) layout.findViewById(R.id.thumb_nail);
		readingDataLayout = (RelativeLayout) layout
				.findViewById(R.id.reading_data);
		balanceLayout = (RelativeLayout) layout.findViewById(R.id.balance);
		orderLayout = (RelativeLayout) layout.findViewById(R.id.order);
		changdu_card = (RelativeLayout) layout.findViewById(R.id.changdu_card);
		haveBuyLayout = (RelativeLayout) layout.findViewById(R.id.haveBuy_rl);
		changduLayout = (RelativeLayout) layout.findViewById(R.id.changdu_rl);
		// 初始时甚至不能点击，防止点击进入后报空指针
		balanceLayout.setClickable(false);
		balanceLayout.setFocusable(false);
		changdu_card.setClickable(false);
		changdu_card.setFocusable(false);
		mOutOfDateInfoTv = (TextView) layout
				.findViewById(R.id.meroot_out_of_date_info_tv);
		mLeftMoneyInfoTv = (TextView) layout
				.findViewById(R.id.meroot_left_money_info_tv);
		fans_notification = (ImageView) layout
				.findViewById(R.id.fans_notification);
		user_read_left = (RelativeLayout) layout
				.findViewById(R.id.user_read_left);
		feedback_layout = (RelativeLayout) layout
				.findViewById(R.id.feedback_layout);
		fans_layout = (LinearLayout) layout.findViewById(R.id.fans_layout);
		focus_people_layout = (LinearLayout) layout
				.findViewById(R.id.focus_people_layout);
		focus_num = (TextView) layout.findViewById(R.id.focus_num);
		fans_num = (TextView) layout.findViewById(R.id.fans_num);
		nameTv=(TextView) layout.findViewById(R.id.user_nickname);
		View downloadView = layout.findViewById(R.id.download_test);
		downloadView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),DownloadTestActivity.class);
				startActivity(intent);
			}
		});
		return root;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		String userpin = LoginUser.getpin();
		if (Notification.getInstance().getMessageCenterSum() == 0) {
			topBarView.clearNotificationOnMenu(true, false, false);
		} else if (Notification.getInstance().getMessageCenterSum() > 0) {
			topBarView.addNotificationOnMenu(true, false, false,"1");
		}
		if (notification.getMESum() == 0) {
			((LauncherActivity) getActivity()).clearMsg(3);
		}
		if (Notification.getInstance().getFollowersCount() > 0) {
			fans_notification.setVisibility(View.VISIBLE);
		}else {
			fans_notification.setVisibility(View.GONE);
		}

		mHandler.sendMessage(mHandler.obtainMessage(3));
		getUserInfo(userpin);
		processRedDotState();
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser){
			StatService.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_my));
		}else{
			StatService.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_my));
		}
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				getRedingCardInfo();
				getWalletInfo();
				break;

			case 1:
				Date nowTime = DateUtil.now();
				Date ouOfDateTime = dateList.get(dateList.size() - 1);
				try {
					String nowStr = (String) msg.obj;
					nowTime = DateUtil.parseDate(nowStr);
					ouOfDateTime = dateList.get(dateList.size() - 1);
					int intervalDays = DateUtil.daysBetween(nowTime,
							ouOfDateTime);
					MZLog.d("J.Beyond", "过期天数:" + intervalDays);
					if (intervalDays < 0) {
						mOutOfDateInfoTv.setVisibility(View.INVISIBLE);
					} else if (intervalDays == 0) {
						mOutOfDateInfoTv.setText("马上过期");
						mOutOfDateInfoTv.setVisibility(View.VISIBLE);
					} else {
						mOutOfDateInfoTv.setVisibility(View.VISIBLE);
						mOutOfDateInfoTv.setText("还有" + intervalDays + "天过期");
					}

					changdu_card.setClickable(true);
					changdu_card.setFocusable(true);

				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				BalanceInfo balanceInfo = (BalanceInfo) msg.obj;
				StringBuffer sb = new StringBuffer();
				if (balanceInfo.balance != 0 && balanceInfo.userScore == 0) {
					sb.append(balanceInfo.balance + "元");
				} else if (balanceInfo.balance == 0
						&& balanceInfo.userScore != 0) {
					sb.append(balanceInfo.userScore + "京豆");
				} else if (balanceInfo.balance != 0
						&& balanceInfo.userScore != 0) {
					sb.append(balanceInfo.balance + "元，"
							+ balanceInfo.userScore + "京豆");
				}
				mLeftMoneyInfoTv.setText(sb.toString());
				balanceLayout.setClickable(true);
				balanceLayout.setFocusable(true);

				break;
			case 3:
				if (mOutOfDateInfoTv != null) {
					mOutOfDateInfoTv.setVisibility(View.GONE);
				}
				getFollows();
				break;
			default:
				break;
			}
		}

	};
	

	

	protected void getRedingCardInfo() {

		if (!NetWorkUtils.isNetworkAvailable(getActivity())) {
			return;
		}
		WebRequestHelper.get(URLText.JD_BOOK_CHANGDU_URL,
				RequestParamsPool.getReadingCardParams(), true,

				new MyAsyncHttpResponseHandler(getActivity()) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// Toast.makeText(getActivity(),
						// getString(R.string.network_connect_error),
						// Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);

						MZLog.d("cj", "result=======>>" + result);
						ReadingCard readingCard = GsonUtils.fromJson(result,
								ReadingCard.class);

						if (readingCard != null
								&& Integer.parseInt(readingCard.getCode()) == 0) {
							readCardList = readingCard.getReadCardList();
							if (dateList == null) {
								dateList = new ArrayList<Date>();
							} else {
								dateList.clear();
							}
							if (readCardList.isEmpty()) {
								// mHandler.sendMessage(mHandler.obtainMessage(3));
								return;
							}
							// SimpleDateFormat sdf = new
							// SimpleDateFormat("yyyy-MM-dd");
							SimpleDateFormat dateFormat = new SimpleDateFormat(
									"yyyy-MM-dd");
							for (ReadCardInfo readCardInfo : readCardList) {
								try {
									String expiryDate = readCardInfo
											.getExpiryDate();
									String[] split = expiryDate.split(" ");

									String dateStr = split[split.length - 1];
									// System.out.println(dateStr+"::::6666666666666666666");

									dateList.add(DateUtil.string2date(dateStr,
											dateFormat));

								} catch (Exception e) {

								}

							}
							if (dateList.size() == 0) {
								return;
							}
							Collections.sort(dateList, new SortComparator());
							Message msg = Message.obtain();
							msg.obj = readingCard.getSysDate();
							msg.what = 1;
							mHandler.sendMessage(msg);
						} 

					}
				});
	}

	

	private void initView() {
		drafts_layout = (RelativeLayout) layout
				.findViewById(R.id.drafts_layout);
		mIntegrationRL = (RelativeLayout)layout.findViewById(R.id.integration_rl);
		//签到领积分
		mIntegrationInfoTv = (TextView) layout.findViewById(R.id.meroot_integration_info_tv);
		//初始时甚至不能点击，防止点击进入后报空指针
		ImageLoader.getInstance().displayImage(imgUrl, avatar,
				GlobalVarable.getDefaultAvatarDisplayOptions(false));	
		nameTv.setText(nickName);
		
		ImageView mAvatarLabel = (ImageView) layout.findViewById(R.id.avatar_label);
		if (LocalUserSetting.getIsSchoolBaiTiaoUser(getActivity())) {
			mAvatarLabel.setVisibility(View.VISIBLE);	
		} else {
			mAvatarLabel.setVisibility(View.GONE);
		}
		focus_num.setText(following_count);

		fans_num.setText(follower_count);
		avatar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(),
						ModifyUserInfoActivity.class);
				startActivity(intent);
			}
		});

		drafts_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(getActivity(), DraftsActivity.class);
				startActivity(intent);

			}
		});

		readingDataLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				TalkingDataUtil.onPersonalCenterEvent(getActivity(), "阅历");

				Intent intent = new Intent(getActivity(),
						ReadingDataChartActivity.class);
				intent.putExtra(ReadingDataChartActivity.UrlKey, imgUrl);
				intent.putExtra(ReadingDataChartActivity.NameKey, nickName);
				intent.putExtra(ReadingDataChartActivity.UserId, current_user_id);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

		balanceLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (GiftBagUtil.getInstance().IsHaveDiscount()) {
					GiftBagUtil.getInstance().isClickDisCount();
					arg0.findViewById(R.id.message_new).setVisibility(
							View.INVISIBLE);
					GiftBagUtil.getInstance().getUpdateReadMessage(
							arg0.getContext(), "2");
				}
				TalkingDataUtil.onPersonalCenterEvent(getActivity(), "钱包");

				Intent intent = new Intent(getActivity(), WebViewActivity.class);
				intent.putExtra(WebViewActivity.UrlKey,
						"http://e.m.jd.com/wallet.html");
				intent.putExtra(WebViewActivity.TopbarKey, true);
				intent.putExtra(WebViewActivity.BrowserKey, false);
				intent.putExtra(WebViewActivity.TitleKey,
						getString(R.string.extra_money));
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

		orderLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				TalkingDataUtil.onPersonalCenterEvent(getActivity(), "订单");

				Intent intent2 = new Intent(getActivity(), OrderActivity.class);
				intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent2);
			}
		});

		user_read_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				TalkingDataUtil.onPersonalCenterEvent(getActivity(), "个人主页");

				Intent intent = new Intent();
				intent.setClass(getActivity(), UserActivity.class);
				intent.putExtra("user_id", current_user_id);
				intent.putExtra(UserActivity.JD_USER_NAME, LoginUser.getpin());
				startActivity(intent);
			}
		});

		feedback_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				TalkingDataUtil.onPersonalCenterEvent(getActivity(), "意见反馈");

				Intent intent3 = new Intent(getActivity(),
						FeedBackActivity.class);
				intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent3);
			}
		});
		mIntegrationRL.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TalkingDataUtil.onPersonalCenterEvent(getActivity(), "积分");
				// 处理积分红点显示
				if (GiftBagUtil.getInstance().IsHaveIntegration()) {
					GiftBagUtil.getInstance().isClickIntegration();
					GiftBagUtil.getInstance().getUpdateReadMessage(
							v.getContext(), "3");
					v.findViewById(R.id.score_message_new).setVisibility(
							View.INVISIBLE);
				}
				Intent intent4 = new Intent(getActivity(), IntegrationActivity.class);
				intent4.putExtra(IntegrationActivity.UrlKey, imgUrl);
				intent4.putExtra(IntegrationActivity.NickName, nickName);
				intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent4);
			}
		});

		changdu_card.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				TalkingDataUtil.onPersonalCenterEvent(getActivity(), "畅读卡");

				if (GiftBagUtil.getInstance().IsHaveNewChangDuCart()) {
					GiftBagUtil.getInstance().isClickChangduCard();
					GiftBagUtil.getInstance().getUpdateReadMessage(
							arg0.getContext(), "1");
					arg0.findViewById(R.id.message_new).setVisibility(
							View.INVISIBLE);
				}
				Intent intent4 = new Intent(getActivity(),
						ReadingCardActivity.class);
				intent4.putParcelableArrayListExtra("monthCard",
						(ArrayList<MonthlyList>) monthlyList);
				intent4.putParcelableArrayListExtra("readCard",
						(ArrayList<ReadCardInfo>) readCardList);
				intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent4);
			}
		});

		focus_people_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), FocusActivity.class);
				intent.putExtra("user_id", current_user_id);
				intent.putExtra(UserActivity.JD_USER_NAME, LoginUser.getpin());
				startActivity(intent);

			}
		});

		fans_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), FansActivity.class);
				intent.putExtra("user_id", current_user_id);
				intent.putExtra(UserActivity.JD_USER_NAME, LoginUser.getpin());
				startActivity(intent);
			}
		});
		
		//已购列表
		haveBuyLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ActivityUtils.startActivity(getActivity(), new Intent(
						getActivity(), BookcaseCloudActivity.class));
				// startActivityForResult(intent, OPEN_FANS);
			}
		});
		
		// 畅读列表
		changduLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ActivityUtils.startActivity(getActivity(), new Intent(
						getActivity(), ChangDuActivity.class));
			}
		});

		getMonthly();
	}

	private void getMonthly() {
		if (!NetWorkUtils.isNetworkAvailable(getActivity())) {
			return;
		}
		WebRequestHelper.get(URLText.JD_BOOK_ORDER_URL,
				RequestParamsPool.getMonthlyParams(), true,

				new MyAsyncHttpResponseHandler(getActivity()) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);

						Monthly monthly = GsonUtils.fromJson(result,
								Monthly.class);

						if (monthly != null
								&& Integer.parseInt(monthly.getCode()) == 0) {
							monthlyList = monthly.getServerAndCardDetailList();
							mHandler.sendMessage(mHandler.obtainMessage(0));
						} 
					}
				});
	}

	private void getWalletInfo() {
		if (!NetWorkUtils.isNetworkConnected(MZBookApplication.getInstance())) {
			return;
		}
		WebRequestHelper.get(URLText.JD_BOOK_ORDER_URL,
				RequestParamsPool.getWalletParams(), true,

				new MyAsyncHttpResponseHandler(getActivity()) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);

						try {
							JSONObject obj = new JSONObject(result);
							if (obj.getInt("code") == -1) {
								return;
							} else if (obj.getInt("code") == 0) {
								String walletJson = obj.getString("wallet");
								JSONObject walletObj = new JSONObject(
										walletJson);
								double balance = walletObj.getDouble("balance");
								int userScore = walletObj.getInt("userScore");
								BalanceInfo balanceInfo = new BalanceInfo(
										balance, userScore);
								Message msg = Message.obtain();
								msg.what = 2;
								msg.obj = balanceInfo;
								mHandler.sendMessage(msg);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}

	private void getUserInfo(String pin) {
		if (!NetWorkUtils.isNetworkConnected(MZBookApplication.getInstance())) {
			return;
		}

		WebRequestHelper
				.get(URLText.JD_BOOK_STORE_URL,
						RequestParamsPool.getUserInfoParams(pin),
						true,
						new MyAsyncHttpResponseHandler(MZBookApplication.getInstance()) {

							@Override
							public void onFailure(int arg0, Header[] arg1,byte[] arg2, Throwable arg3) {
							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String result = new String(responseBody);

								UserInfo userinfo = GsonUtils.fromJson(result,
										UserInfo.class);

								if (userinfo != null
										&& userinfo.getList() != null
										&& userinfo.getList().size() > 0) {

									nickName = userinfo.getList().get(0)
											.getNickName();
									imgUrl = userinfo.getList().get(0)
											.getYunBigImageUrl();
									mHandler.sendMessage(mHandler.obtainMessage(3));
									
									//保存头像Url、用户昵称
									LocalUserSetting.saveUserHeaderUrl(getActivity(), imgUrl);
									LocalUserSetting.saveUserNickName(getActivity(), nickName);
								} 
							}

						});

	}

	private void getFollows() {
		if (!NetWorkUtils.isNetworkConnected(getActivity())) {
			return;
		}

		WebRequestHelper.get(URLText.Follows_URL,
				RequestParamsPool.getFollowParams(LoginUser.getpin()), true,
				new MyAsyncHttpResponseHandler(getActivity()) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {

					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);

						JSONObject jsonObj;
						try {
							jsonObj = new JSONObject(new String(responseBody));
							if (jsonObj != null) {
								following_count = jsonObj
										.optString("following_count");
								follower_count = jsonObj
										.optString("follower_count");
								current_user_id = jsonObj
										.optString("current_user_id");
								LocalUserSetting.saveUser_id(getActivity(),
										current_user_id);
								if (getActivity() != null && topBarView != null) {
									topBarView.setTitle(nickName);
									initView();
								}
							} 
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

	}

	
	
	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * 处理红点显示
	 */
	private void processRedDotState() {
		
		//畅读卡红点
		if (GiftBagUtil.getInstance().isCurentUserHaveNewChangDuCard()) {
			layout.findViewById(R.id.changdu_card)
					.findViewById(R.id.message_new)
					.setVisibility(View.INVISIBLE);

		} else if (GiftBagUtil.getInstance().IsHaveNewChangDuCart()) {
			layout.findViewById(R.id.changdu_card)
					.findViewById(R.id.message_new).setVisibility(View.VISIBLE);
		} else {
			layout.findViewById(R.id.changdu_card)
					.findViewById(R.id.message_new)
					.setVisibility(View.INVISIBLE);
		}
		
		//优惠券红点
		if (GiftBagUtil.getInstance().isCurentUserHaveDisCard()) {
			layout.findViewById(R.id.balance).findViewById(R.id.message_new)
					.setVisibility(View.INVISIBLE);
		} else if (GiftBagUtil.getInstance().IsHaveDiscount()) {
			layout.findViewById(R.id.balance).findViewById(R.id.message_new)
					.setVisibility(View.VISIBLE);
		} else {
			layout.findViewById(R.id.balance).findViewById(R.id.message_new)
					.setVisibility(View.INVISIBLE);
		}
		
		//积分红点
		if (GiftBagUtil.getInstance().isCurentUserHaveIntegration()) {
			layout.findViewById(R.id.integration_rl)
					.findViewById(R.id.score_message_new)
					.setVisibility(View.INVISIBLE);
		} else if (GiftBagUtil.getInstance().IsHaveIntegration()) {
			layout.findViewById(R.id.integration_rl)
					.findViewById(R.id.score_message_new)
					.setVisibility(View.VISIBLE);
		} else {
			layout.findViewById(R.id.integration_rl)
					.findViewById(R.id.score_message_new)
					.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {// 显示
			processRedDotState();
		}
	}

	public void initTopbarView() {
		if (getActivity() == null || topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.btn_bar_notifiction);
		topBarView.setRightMenuOneVisiable(true, R.drawable.btn_bar_setting,false);
		//监听器
		topBarView.setListener(this);
	}

	@Override
	public void onLeftMenuClick() {
		Intent intent = new Intent(getActivity(), MessageCenterActivity.class);
		startActivity(intent);
	}

	@Override
	public void onRightMenuOneClick() {
		Intent intent = new Intent(getActivity(), SettingActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public void onCenterMenuItemClick(int position) {

	}

	@Override
	public void onRightMenuTwoClick() {

	}
	
	class SortComparator implements Comparator<Date> {

		@Override
		public int compare(Date arg0, Date arg1) {
			return arg0.compareTo(arg1);
		}

	}

	class BalanceInfo {
		double balance;
		int userScore;

		public BalanceInfo(double balance, int userScore) {
			this.balance = balance;
			this.userScore = userScore;
		}

		public double getBalance() {
			return balance;
		}

		public void setBalance(double balance) {
			this.balance = balance;
		}

		public int getUserScore() {
			return userScore;
		}

		public void setUserScore(int userScore) {
			this.userScore = userScore;
		}
		
	}

}
