package com.jingdong.app.reader.me.adapter;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GetGiftByScoreListener;
import com.jingdong.app.reader.me.model.MyScoreIndex;
import com.jingdong.app.reader.me.model.ScoreGift;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 积分首页适配器
 * @author J.Beyond
 *
 */
public class ScoreIndexListAdapter extends BaseAdapter {

	private Context mContext;
	private List<ScoreGift> mScoreGiftList;
	private LayoutInflater mInflater;
	private MyScoreIndex myScore;

	public ScoreIndexListAdapter(Context ctx,MyScoreIndex myScore) {
		this.mContext = ctx;
		this.mScoreGiftList = myScore.getScoreGifts();
		this.myScore = myScore;
		this.mInflater = LayoutInflater.from(ctx);
	}
	
	@Override
	public int getCount() {
		return mScoreGiftList.size();
	}

	@Override
	public Object getItem(int position) {
		return mScoreGiftList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.integration_main_list_item, null);
			holder = new ViewHolder();
			holder.logoIv = (ImageView) convertView.findViewById(R.id.listitem_award_cover);
			holder.giftNameTv = (TextView) convertView.findViewById(R.id.giftName_tv);
			holder.scoreTv = (TextView) convertView.findViewById(R.id.score_tv);
			holder.descTv = (TextView) convertView.findViewById(R.id.desc_tv);
			holder.exchangeBtn = (Button) convertView.findViewById(R.id.exchange_btn);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag(); 
		}
		
		final ScoreGift scoreGift = mScoreGiftList.get(position);
		//设置logo
		ImageLoader.getInstance().displayImage(scoreGift.getLogoAll(), holder.logoIv,
				GlobalVarable.getCutBookDisplayOptions(false));
		holder.giftNameTv.setText(scoreGift.getGiftName());
		final int giftNeedScore = scoreGift.getStore();
		holder.scoreTv.setText(giftNeedScore+"积分兑换");
		holder.descTv.setText(scoreGift.getDesc());
		holder.exchangeBtn.setText("兑换");
		if (scoreGift.isExchange()) {
			holder.exchangeBtn.setTextColor(mContext.getResources().getColor(R.color.r_text_disable));
			holder.exchangeBtn.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
		}else{
			holder.exchangeBtn.setTextColor(mContext.getResources().getColor(R.color.highlight_color));
			holder.exchangeBtn.setBackgroundResource(R.drawable.border_listbtn_red_h24);
		}
		holder.exchangeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (scoreGift.isExchange()) {
					CustomToast.showToast(mContext, "礼物已经兑换过");
				}else{
					if (myScore.getScoreTotal() < giftNeedScore) {
						CustomToast.showToast(mContext, "积分不足，快去挣积分吧！");
					}else{
						String content = "您选择兑换“"+scoreGift.getGiftName()+"”，将扣除"+scoreGift.getStore()+"积分";
						DialogManager.showCommonDialog(mContext, "提示", content, "确定", "取消", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case DialogInterface.BUTTON_POSITIVE:
									exchangeGift(scoreGift.getId());
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
				}
				
			}
		});
		return convertView;
	}
	
	protected void exchangeGift(int id) {
		IntegrationAPI.getGiftByScore(mContext, id, new GetGiftByScoreListener() {
			
			@Override
			public void onSuccess() {
				CustomToast.showToast(mContext, "兑换成功");
				LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("com.jdread.action.exchange"));
			}
			
			@Override
			public void onFail(String errorInfo) {
				CustomToast.showToast(mContext, errorInfo);
				MZLog.e("J", "兑换失败，原因："+errorInfo);
			}
		});
	}

	static class ViewHolder{
		ImageView logoIv;
		TextView giftNameTv;
		TextView scoreTv;
		TextView descTv;
		Button exchangeBtn;
		
	}
	
	

}
