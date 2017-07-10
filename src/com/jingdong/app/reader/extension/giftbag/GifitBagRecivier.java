package com.jingdong.app.reader.extension.giftbag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.util.DisplayUtil;
import com.jingdong.app.reader.util.MZLog;

public class GifitBagRecivier extends BroadcastReceiver {
	public static final String ACTION = "com.jingdong.app.reader.giftbag.GiftBagAcitivity";
	public static final int HAVE_GIFTBAG = 0x01;
	public static final int LIVE_GIFTBAG = 0x02;
	public static final int GIFTBAG_ERR_MSG = 0x03;
	public static final String COMMOND_KEY = "GifitBagRecivier";
	private GiftBagView giftBagView = null;
	private GiftBagAcitivity acitvity;
	private String tag = "GifitBagRecivier";
	GiftBagUtil bagUtil = GiftBagUtil.getInstance();

	public GifitBagRecivier(GiftBagAcitivity acitvity) {
		// TODO Auto-generated constructor stub
		this.giftBagView = (GiftBagView) acitvity
				.findViewById(R.id.giftbagvessel);
		this.giftBagView.setLogon(acitvity);
		this.acitvity = acitvity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try{

			// TODO Auto-generated method stub
			if (intent == null) {
				return;
			}
			if (giftBagView == null) {
				return;
			}
			int commond_key = intent.getIntExtra(COMMOND_KEY, LIVE_GIFTBAG);
			String message = intent.getStringExtra(GiftBagUtil.MESSAGE_KEY);
			TextView titleText = (TextView) giftBagView.findViewById(R.id.title);
			ImageView iv = (ImageView) giftBagView.findViewById(R.id.gift_bg);
			ImageView bgImg = (ImageView) giftBagView.findViewById(R.id.gift_bg);
			int id = intent.getIntExtra("id", -1);
			// GiftBagSucessView successView=(GiftBagSucessView)
			// giftBagView.findViewById(R.id.successview);
			switch (commond_key) {
			case HAVE_GIFTBAG:
				RelativeLayout rl = new RelativeLayout(context);
				RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				rl.setLayoutParams(rlParams);
				rl.setGravity(Gravity.CENTER);
				String tem[] = message.split("\\n");
				SparseArray<String> textData = new SparseArray<>();
				SparseArray<Integer> color = new SparseArray<>();
				SparseArray<Integer> size = new SparseArray<>();
				for (int i = 0; i < tem.length; i++) {
					textData.put(i, tem[i]);
					if (i >= tem.length - 2) {
						size.put(i, 13);
					} else {

						size.put(i, 15);
					}
					color.put(i,
							context.getResources().getColor(R.color.gifitbagcolor));
				}
				GiftBagSucessView successView = new GiftBagSucessView(context);
				successView.setId(0x1000);
				GiftBagTextAdapter gtextA = new GiftBagTextAdapter(textData, color,
						size);
				successView.setTextAdapter(gtextA);
				successView.notifyData();
				rl.addView(successView, rlParams);
				acitvity.setContentView(rl);
				ImageView close = new ImageView(context);
				DisplayMetrics metrics = MZBookApplication.getInstance()
						.getResources().getDisplayMetrics();
				int widht = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 30, metrics);
				rlParams = new RelativeLayout.LayoutParams(widht, widht);
				rlParams.rightMargin = widht;
				rlParams.topMargin = DisplayUtil.getHeight() / 5;
				rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				close.setLayoutParams(rlParams);
				close.setImageResource(R.drawable.gift_close);
				close.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						MZBookApplication app = (MZBookApplication) acitvity
								.getApplication();
						Intent intent = new Intent(GiftBagUiReciver.ACTION);
						app.sendLocalBroadcastMessage(intent);
						acitvity.finish();
					}
				});
				rl.addView(close);
				successView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						MZBookApplication app = (MZBookApplication) acitvity
								.getApplication();
						Intent intent = new Intent(GiftBagUiReciver.ACTION);
						app.sendLocalBroadcastMessage(intent);
						acitvity.finish();
						return false;
					}
				});
				rl.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						MZBookApplication app = (MZBookApplication) acitvity
								.getApplication();
						Intent intent = new Intent(GiftBagUiReciver.ACTION);
						app.sendLocalBroadcastMessage(intent);
						acitvity.finish();
						return false;
					}
				});
				bagUtil.updateGiftBagStatus(id, 3);
				break;
			case LIVE_GIFTBAG:
				giftBagView.findViewById(R.id.get_gift_button).setVisibility(
						View.GONE);
				giftBagView.findViewById(R.id.books_titles).setVisibility(
						View.VISIBLE);
				titleText.setMinLines(15);
				RelativeLayout.LayoutParams rlp = (LayoutParams) titleText
						.getLayoutParams();
				if (rlp != null) {
					Drawable bgDrawable = bgImg.getDrawable();
					rlp.width = bgDrawable.getIntrinsicWidth();
					rlp.height = bgDrawable.getIntrinsicHeight() * 2;
					titleText.setLines(10);
					titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
					MZLog.d(tag, "message=" + message.replace("\\n", "<br>"));
					titleText.setText(Html.fromHtml(message.replace("\\n",
							"&nbsp<br>")));
					titleText.setGravity(Gravity.CENTER_HORIZONTAL);
					titleText.setLayoutParams(rlp);
				}
				giftBagView.findViewById(R.id.books_titles)
						.setVisibility(View.GONE);
				iv.setImageResource(R.drawable.giftbag_bg3);
				iv.setScaleType(ScaleType.FIT_CENTER);
				giftBagView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						acitvity.finish();
						return false;
					}
				});
				break;
			case GIFTBAG_ERR_MSG:
				rlp = (LayoutParams) titleText.getLayoutParams();
				titleText.setVisibility(View.VISIBLE);
				giftBagView.findViewById(R.id.get_gift_button).setVisibility(
						View.GONE);
				giftBagView.findViewById(R.id.books_titles)
						.setVisibility(View.GONE);
				titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
				titleText.setText(message);
				if (rlp != null) {
					Drawable bgDrawable = bgImg.getDrawable();
					rlp.width = LayoutParams.MATCH_PARENT;
					rlp.height = LayoutParams.MATCH_PARENT;
					rlp.topMargin = 0;
					titleText.setLines(10);
					titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
					MZLog.d(tag, "message=" + message.replace("\\n", "<br>"));
					titleText.setText(Html.fromHtml(message.replace("\\n",
							"&nbsp<br>")));
					titleText.setGravity(Gravity.CENTER);
					titleText.setLayoutParams(rlp);
				}
				giftBagView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						acitvity.finish();
						return false;
					}
				});
				break;
			}
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
