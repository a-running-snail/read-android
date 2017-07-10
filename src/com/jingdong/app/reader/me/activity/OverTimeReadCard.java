package com.jingdong.app.reader.me.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.ReadCardInfo;

public class OverTimeReadCard extends BaseActivityWithTopBar{
	
	private List<ReadCardInfo> readCardList = null;
	private LinearLayout contain_layout;
	private TextView header;
	private LinearLayout contain_item_layout;
	private TextView card_number_statue;
	private TextView value_statue;
	private TextView lastbook_statue;
	private TextView validity_statue;
	private TextView lastbook_total;
	private TextView foot;
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
		super.onCreate(arg0);
		setContentView(R.layout.overtimereadingcard);
		Intent intent = getIntent();
		readCardList = intent.getParcelableArrayListExtra("overTimereadCard");
		
		if (readCardList == null) {
			return;
		}
		
		contain_layout = (LinearLayout) findViewById(R.id.contain_layout);
		showChangduCardForData(readCardList);
//		for (int i = 0; i < readCardList.size(); i++) {
//			LinearLayout reading_cotain = (LinearLayout) LayoutInflater.from(
//					OverTimeReadCard.this).inflate(
//					R.layout.readingcard_contain, null);
//			header = (TextView) reading_cotain.findViewById(R.id.header);
//			foot = (TextView) reading_cotain.findViewById(R.id.foot);
//			if (readCardList.get(i).getCardStatus() == 4) {
//				flag0++;
//				if (flag0 == 1) {
//					header.setVisibility(View.VISIBLE);
//					foot.setVisibility(View.GONE);
//					header.setText("延期中的畅读卡");
//				}else {
//					header.setVisibility(View.GONE);
//					foot.setVisibility(View.VISIBLE);
//				}
//			}else if (readCardList.get(i).getCardStatus() == 5) {
//				flag1++;
//				if (flag1 == 1) {
//					header.setVisibility(View.VISIBLE);
//					foot.setVisibility(View.GONE);
//					header.setText("已退款的畅读卡");
//				}else {
//					header.setVisibility(View.GONE);
//					foot.setVisibility(View.VISIBLE);
//				}
//			}else if (readCardList.get(i).getCardStatus() == 6) {
//				flag2++;
//				if(flag2 == 1){
//					header.setVisibility(View.VISIBLE);
//					foot.setVisibility(View.GONE);
//					header.setText("到期作废的畅读卡");
//				}else {
//					header.setVisibility(View.GONE);
//					foot.setVisibility(View.VISIBLE);
//				}
//			}else if (readCardList.get(i).getCardStatus() == 7) {
//				flag3++;
//				if (flag3 == 1) {
//					header.setVisibility(View.VISIBLE);
//					foot.setVisibility(View.GONE);
//					header.setText("异常作废的畅读卡");
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
//			card_number_statue.setText(readCardList.get(i).getCardNo());
//			value_statue.setText(readCardList.get(i).getFaceMoney() + "元");
//			lastbook_statue.setText(String.valueOf(readCardList.get(i)
//					.getEbookCount()));
//			lastbook_total.setText("/" + readCardList.get(i).getTopCount());
//			validity_statue.setText(readCardList.get(i).getExpiryDate());
//			contain_layout.addView(reading_cotain);
//		}
	}

	/**
	 * 展示卡片化的畅读卡信息
	 * @param list
	 */
	private void showChangduCardForData(List<ReadCardInfo> list){
		for (int i = 0; i < list.size(); i++) {
			LinearLayout reading_cotain = (LinearLayout) LayoutInflater.from(
					OverTimeReadCard.this).inflate(
					R.layout.readingcard_cardstyle, null);
			header = (TextView) reading_cotain.findViewById(R.id.header);
			foot = (TextView) reading_cotain.findViewById(R.id.foot);
			cardStatus=(ImageView) reading_cotain.findViewById(R.id.card_status);
			cardLinearLayout =  (LinearLayout) reading_cotain.findViewById(R.id.card_linearLayout);
			cardName =  (TextView) reading_cotain.findViewById(R.id.card_name);
			header_layout = (LinearLayout) reading_cotain.findViewById(R.id.header_LinearLayout);
			
			header_layout.setVisibility(View.GONE);
			foot.setVisibility(View.VISIBLE);
			
			cardLinearLayout.setBackgroundResource(R.drawable.changdu_card_timeout);
			switch (list.get(i).getAmountType()) {//卡类型：1、包季  2、半年  3、包年  4、月卡  5、七天卡
			case 1:
				cardName.setText("季卡90天");
				break;
			case 2:
				cardName.setText("半年卡180天");
				break;
			case 3:
				cardName.setText("年卡360天");
				break;
			case 4:
				cardName.setText("月卡30天");
				break;
			case 5:
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
			valideDate.setText(list.get(i).getExpiryDate());
			contain_layout.addView(reading_cotain);
			}
		}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_overtime_readingcard));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_overtime_readingcard));
	}
	
}
