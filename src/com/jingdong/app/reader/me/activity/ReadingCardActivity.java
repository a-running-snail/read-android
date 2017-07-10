package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.MonthlyList;
import com.jingdong.app.reader.entity.extra.ReadCardInfo;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.dialog.DialogManager;

public class ReadingCardActivity extends BaseActivityWithTopBar {

	private List<ReadCardInfo> readCardList = null;
	private RelativeLayout by_readingcard;// 购买畅读卡
	private RelativeLayout bind_readingcard;// 绑定畅读卡
	private RelativeLayout statue_layout;// 包月卡状态
	private RelativeLayout end_timelayout;// 结束时间
	private RelativeLayout renewals_layout;// 续费
	private LinearLayout contain_layout;
	private LinearLayout outtime_contain_layout;
	private TextView header;
	private LinearLayout contain_item_layout;
	private TextView card_number_statue;
	private TextView value_statue;
	private TextView lastbook_statue;
	private TextView validity_statue;
	private TextView lastbook_total;
	private TextView foot;
	private List<MonthlyList> monthlyList;
	private TextView statue;
	private TextView endtime;
	private LinearLayout monthly_layoutLayout;
	private TextView continue_time;
	private int serviceid;
	private String urlpath;
	private RelativeLayout outtime_read_card;
	private List<ReadCardInfo> overTimereadCardList = null;
	private List<ReadCardInfo> sortreadCardList = null;
	private int flag0 = 0;
	private int flag1 = 0;
	private int flag2 = 0;
	private int flag3 = 0;
	
	private TextView cardId;
	private TextView readedNum;
	private TextView valideDate;
	private ImageView cardStatus;
	private LinearLayout cardLinearLayout;
	private TextView cardName;
	private LinearLayout header_layout;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.readingcard);
		Intent intent = getIntent();
		monthlyList = intent.getParcelableArrayListExtra("monthCard");
		readCardList = intent.getParcelableArrayListExtra("readCard");
		overTimereadCardList = new ArrayList<ReadCardInfo>();
		sortreadCardList = new ArrayList<ReadCardInfo>();

		if (readCardList == null) {
			return;
		}else {
			//排序
			try {
				System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
				Collections.sort(readCardList, new SortComparator());
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = 0; i < readCardList.size(); i++) {
				if (readCardList.get(i).getCardStatus() == 2) {
					sortreadCardList.add(readCardList.get(i));
				}else if (readCardList.get(i).getCardStatus() == 1) {
					sortreadCardList.add(readCardList.get(i));
				}else if (readCardList.get(i).getCardStatus() == 0) {
					sortreadCardList.add(readCardList.get(i));
				}else if (readCardList.get(i).getCardStatus() == 3){
					sortreadCardList.add(readCardList.get(i));
				}else if (readCardList.get(i).getCardStatus() == 4) {
					overTimereadCardList.add(readCardList.get(i));
				}else if (readCardList.get(i).getCardStatus() == 5) {
					overTimereadCardList.add(readCardList.get(i));
				}else if (readCardList.get(i).getCardStatus() == 6) {
					overTimereadCardList.add(readCardList.get(i));
				}else if (readCardList.get(i).getCardStatus() == 7) {
					overTimereadCardList.add(readCardList.get(i));
				}
			}
		}
		
//		Collections.sort(readCardList, new SortComparator());
		try {
			initView();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initView() throws Exception {
		by_readingcard = (RelativeLayout) findViewById(R.id.by_readingcard);
		by_readingcard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ReadingCardActivity.this,
						WebViewActivity.class);
				intent.putExtra(WebViewActivity.UrlKey,
						"http://e.m.jd.com/readCard.html");
				intent.putExtra(WebViewActivity.TopbarKey, true);
				intent.putExtra(WebViewActivity.BrowserKey, false);
				intent.putExtra(WebViewActivity.TitleKey, "购买畅读卡");
				startActivity(intent);
			}
		});
		bind_readingcard = (RelativeLayout) findViewById(R.id.bind_readingcard);
		statue_layout = (RelativeLayout) findViewById(R.id.statue_layout);
		end_timelayout = (RelativeLayout) findViewById(R.id.end_timelayout);
		renewals_layout = (RelativeLayout) findViewById(R.id.renewals_layout);
		monthly_layoutLayout = (LinearLayout) findViewById(R.id.monthly_layout);
		continue_time = (TextView) findViewById(R.id.continue_time);
		outtime_read_card = (RelativeLayout) findViewById(R.id.outtime_read_card);

		outtime_read_card.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ReadingCardActivity.this,
						OverTimeReadCard.class);
				intent.putParcelableArrayListExtra("overTimereadCard",
						(ArrayList<ReadCardInfo>) overTimereadCardList);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

		bind_readingcard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ReadingCardActivity.this,
						BindReadingCardActivity.class);
				startActivity(intent);
			}
		});

		if (monthlyList != null) {
			monthly_layoutLayout.setVisibility(View.VISIBLE);
			serviceid = monthlyList.get(0).getServerId();
			statue = (TextView) findViewById(R.id.statue);
			endtime = (TextView) findViewById(R.id.endtime);
			statue.setText(monthlyList.get(0).getStatusDesc());
			endtime.setText(monthlyList.get(0).getServerDate());
			if (Integer.parseInt(monthlyList.get(0).getServerStatusDesc()) == 1) {
				continue_time.setText("取消");
				renewals_layout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						String message="您是否要取消畅读包月功能,取消后将不能免费阅读畅读卡类图书，如果您是首次购买的包月服务取消后将不能获得前两个月的免费月卡。";
						DialogManager.showCommonDialog(ReadingCardActivity.this, "提示", message, "确定", "再考虑一下",
								new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case DialogInterface.BUTTON_POSITIVE:
									CancleService((Integer) serviceid);
									break;
								case DialogInterface.BUTTON_NEGATIVE:
									break;
								default:
									break;
								}
								dialog.dismiss();
							}
						});
					}
				});

			} else {
				continue_time.setText("购买");
				renewals_layout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						ContinueMoney();
					}
				});
			}
		} else {
			monthly_layoutLayout.setVisibility(View.GONE);
		}

		contain_layout = (LinearLayout) findViewById(R.id.contain_layout);
		showChangduCardForData(sortreadCardList);
		
		outtime_contain_layout = (LinearLayout) findViewById(R.id.outtime_contain_layout);
		outtime_contain_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

			}
		});

//		contain_layout = (LinearLayout) findViewById(R.id.contain_layout);
//		for (int i = 0; i < sortreadCardList.size(); i++) {
//			LinearLayout reading_cotain = (LinearLayout) LayoutInflater.from(
//					ReadingCardActivity.this).inflate(
//					R.layout.readingcard_contain, null);
//			header = (TextView) reading_cotain.findViewById(R.id.header);
//			foot = (TextView) reading_cotain.findViewById(R.id.foot);
//			if (sortreadCardList.get(i).getCardStatus() == 0) {
//				flag0++;
//				if (flag0 == 1) {
//					header.setVisibility(View.VISIBLE);
//					foot.setVisibility(View.GONE);
//					header.setText("新购入的畅读卡");
//				}else{
//					header.setVisibility(View.GONE);
//					foot.setVisibility(View.VISIBLE);
//				}
//			} else if (sortreadCardList.get(i).getCardStatus() == 1) {
//				flag1++;
//				if (flag1 == 1) {
//					header.setVisibility(View.VISIBLE);
//					foot.setVisibility(View.GONE);
//					header.setText("已经激活的畅读卡");
//				}else {
//					header.setVisibility(View.GONE);
//					foot.setVisibility(View.VISIBLE);	
//				}
//			} else if(sortreadCardList.get(i).getCardStatus() == 2) {
//				flag2++;
//				if (flag2 == 1) {
//					header.setVisibility(View.VISIBLE);
//					foot.setVisibility(View.GONE);
//					header.setText("使用中的畅读卡");
//				}else {
//					header.setVisibility(View.GONE);
//					foot.setVisibility(View.VISIBLE);	
//				}
//			} else if(sortreadCardList.get(i).getCardStatus() == 3){
//				flag3++;
//				if (flag3 == 1) {
//					header.setVisibility(View.VISIBLE);
//					foot.setVisibility(View.GONE);
//					header.setText("已达上限的畅读卡");
//				}else {
//					header.setVisibility(View.GONE);
//					foot.setVisibility(View.VISIBLE);
//				}
//			}
//			
//			card_number_statue = (TextView) reading_cotain
//					.findViewById(R.id.card_number_statue);
//			value_statue = (TextView) reading_cotain
//					.findViewById(R.id.value_statue);
//			lastbook_statue = (TextView) reading_cotain
//					.findViewById(R.id.lastbook_statue);
//			validity_statue = (TextView) reading_cotain
//					.findViewById(R.id.validity_statue);
//			lastbook_total = (TextView) reading_cotain
//					.findViewById(R.id.lastbook_total);
//
//			card_number_statue.setText(sortreadCardList.get(i).getCardNo());
//			value_statue.setText(sortreadCardList.get(i).getFaceMoney() + "元");
//			lastbook_statue.setText(String.valueOf(sortreadCardList.get(i)
//					.getEbookCount()));
//			lastbook_total.setText("/" + sortreadCardList.get(i).getTopCount());
//			validity_statue.setText(sortreadCardList.get(i).getExpiryDate());
//			contain_layout.addView(reading_cotain);
	}
	
	/**
	 * 展示卡片化的畅读卡信息
	 * @param list
	 */
	private void showChangduCardForData(List<ReadCardInfo> list){
		for (int i = 0; i < list.size(); i++) {
			LinearLayout reading_cotain = (LinearLayout) LayoutInflater.from(
					ReadingCardActivity.this).inflate(
					R.layout.readingcard_cardstyle, null);
			header = (TextView) reading_cotain.findViewById(R.id.header);
			foot = (TextView) reading_cotain.findViewById(R.id.foot);
			cardStatus=(ImageView) reading_cotain.findViewById(R.id.card_status);
			cardLinearLayout =  (LinearLayout) reading_cotain.findViewById(R.id.card_linearLayout);
			cardName =  (TextView) reading_cotain.findViewById(R.id.card_name);
			header_layout = (LinearLayout) reading_cotain.findViewById(R.id.header_LinearLayout);
			
			if (list.get(i).getCardStatus() == 0 ||list.get(i).getCardStatus() == 1) {
				flag0++;
				if (flag0 == 1) {
					header_layout.setVisibility(View.VISIBLE);
					foot.setVisibility(View.GONE);
					header.setText("待使用的畅读卡");
				}else{
					header_layout.setVisibility(View.GONE);
					foot.setVisibility(View.VISIBLE);
				}
				cardStatus.setImageResource(R.drawable.readingcard_unused_tip);
			}else if(list.get(i).getCardStatus() == 2 || list.get(i).getCardStatus() == 3) {
				flag2++;
				if (flag2 == 1) {
					header_layout.setVisibility(View.VISIBLE);
					foot.setVisibility(View.GONE);
					header.setText("使用中的畅读卡");
					
					View line=reading_cotain.findViewById(R.id.topline);
					line.setVisibility(View.GONE);
				}else {
					header_layout.setVisibility(View.GONE);
					foot.setVisibility(View.VISIBLE);	
				}
				cardStatus.setImageResource(R.drawable.readingcard_using_tip);
			}
			
			switch (list.get(i).getAmountType()) {//卡类型：1、包季  2、半年  3、包年  4、月卡  5、七天卡
			case 1:
				cardLinearLayout.setBackgroundResource(R.drawable.changdu_card_90day);
				cardName.setText("季卡90天");
				break;
			case 2:
				cardLinearLayout.setBackgroundResource(R.drawable.changdu_card_180day);
				cardName.setText("半年卡180天");
				break;
			case 3:
				cardLinearLayout.setBackgroundResource(R.drawable.changdu_card_360day);
				cardName.setText("年卡360天");
				break;
			case 4:
				cardLinearLayout.setBackgroundResource(R.drawable.changdu_card_30day);
				cardName.setText("月卡30天");
				break;
			case 5:
				cardLinearLayout.setBackgroundResource(R.drawable.changdu_card_7day);
				cardName.setText("体验卡7天");
				break;
			default:
				break;
			}
			
			cardId = (TextView) reading_cotain
					.findViewById(R.id.card_id);
			readedNum = (TextView) reading_cotain
					.findViewById(R.id.readed_num);
			valideDate = (TextView) reading_cotain
					.findViewById(R.id.valide_date);

			cardId.setText(list.get(i).getCardNo());
			readedNum.setText(String.valueOf(list.get(i)
					.getEbookCount())+"/" + list.get(i).getTopCount());
			if(list.get(i).getCardStatus() == 0)//0为未激活的卡，需要激活才能使用
				valideDate.setText("需要激活使用");
			else
				valideDate.setText(list.get(i).getExpiryDate());
			contain_layout.addView(reading_cotain);
			}
		}
	

	class SortComparator implements Comparator<ReadCardInfo> {

		@Override
		public int compare(ReadCardInfo lhs, ReadCardInfo rhs) {
			int code1 = lhs.getCardStatus();
			int code2 = rhs.getCardStatus();
			MZLog.d("sort", code1 + ":" + code2);
			// 降序排列
			if (code1 == 1) {
				switch (code2) {
				case 1:
					return 0;
				case 2:
					return 1;
				case 6:
					return -1;
				default:
					return -1;
				}
			} else if (code1 == 2) {
				switch (code2) {
				case 1:
					return -1;
				case 2:
					return 0;
				case 6:
					return -1;
				default:
					return -1;
				}
			} else {
				switch (code2) {
				case 1:
					return 1;
				case 2:
					return 1;
				case 6:
					return 0;
				default:
					return -1;
				}
			}
			// return 1;
		}
	}

	private void ContinueMoney() {
		WebRequestHelper.get(URLText.JD_BOOK_ORDER_URL,
				RequestParamsPool.getContinueMoneyParams(), true,
				new MyAsyncHttpResponseHandler(ReadingCardActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						Toast.makeText(ReadingCardActivity.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// TODO Auto-generated method stub

						String result = new String(responseBody);

						MZLog.d("cj", "Monthlyresult=======>>" + result);
						JSONObject jsonObj;
						try {
							jsonObj = new JSONObject(new String(responseBody));
							if (jsonObj != null) {
								JSONObject desJsonObj = null;
								String code = jsonObj.optString("code");
								if (Integer.parseInt(code) == 0) {
									String tokenKey = jsonObj
											.optString("tokenKey");
									String url = jsonObj.optString("url");
									urlpath = url + "?tokenKey=" + tokenKey;
									mHandler.sendMessage(mHandler
											.obtainMessage(2));
								} else if (Integer.parseInt(code) == 53) {
									Toast.makeText(getApplicationContext(),
											"已购买服务,不需要购买!", 1).show();
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
	}

	private void CancleService(Integer serviceid) {
		WebRequestHelper.get(URLText.JD_BOOK_ORDER_URL,
				RequestParamsPool.getCancleServiceParams(serviceid), true,
				new MyAsyncHttpResponseHandler(ReadingCardActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						Toast.makeText(ReadingCardActivity.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// TODO Auto-generated method stub

						String result = new String(responseBody);

						MZLog.d("cj", "Monthlyresult=======>>" + result);
						JSONObject jsonObj;
						try {
							jsonObj = new JSONObject(new String(responseBody));
							if (jsonObj != null) {
								JSONObject desJsonObj = null;
								String code = jsonObj.optString("code");
								if (Integer.parseInt(code) == 0) {
									Toast.makeText(getApplicationContext(),
											"成功", 1).show();
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				// getRedingCardInfo();
				break;
			case 1:
				// try {
				// initView();
				// } catch (Exception e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				break;
			case 2:
				Intent intent = new Intent(ReadingCardActivity.this,
						WebViewActivity.class);
				intent.putExtra(WebViewActivity.UrlKey, urlpath);
				intent.putExtra(WebViewActivity.TopbarKey, true);
				intent.putExtra(WebViewActivity.BrowserKey, false);
				intent.putExtra(WebViewActivity.TitleKey, "购买");
				startActivity(intent);
				break;
			default:
				break;
			}
		}

	};

	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_readingcard));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_readingcard));
	}
	
}
