package com.jingdong.app.reader.extension.giftbag;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.DisplayUtil;

public class GiftBagView extends RelativeLayout {
	private String tag="GiftBagView";
	private int width=0;
	private int height=0;
	private int imgaeWidth=0;
	GifitBagRecivier recivier=null;
	private GiftBagLognInterface logon=null;
	private Intent intent=null;
	public Intent getIntent() {
		return intent;
	}

	public void setIntent(Intent intent) {
		this.intent = intent;
	}

	public GiftBagLognInterface getLogon() {
		return logon;
	}

	public void setLogon(GiftBagLognInterface logon) {
		this.logon = logon;
	}

	public GiftBagView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public GiftBagView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public GiftBagView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}
	public void init(Context context){
		height=DisplayUtil.getHeight();
		width=DisplayUtil.getWidth();
	}
	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();

	}
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		RelativeLayout.LayoutParams lp=(RelativeLayout.LayoutParams) getLayoutParams();
		if(width>height){
			imgaeWidth=height;
		}else{
			imgaeWidth=width;
		}
		if(lp!=null){
			lp.width=imgaeWidth;
			lp.height=imgaeWidth;
		}else{
			lp=new RelativeLayout.LayoutParams(imgaeWidth,imgaeWidth);
			setLayoutParams(lp);
		}
	
	
		initGiftBagPager(lp);
	
	}

	/**礼包起始页面
	 * @param lp
	 */
	private void initGiftBagPager(RelativeLayout.LayoutParams lp) {
		TextView title=new TextView(getContext());
		title.setId(R.id.title);
		title.setLines(2);
		title.setText("恭喜您！\n礼包喜从天降！");
		title.setGravity(Gravity.CENTER|Gravity.BOTTOM);
		title.setPadding(0, 0, 0, 15);
		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.5f);
		title.setTextColor(getContext().getResources().getColor(R.color.gifitbagcolor));
		RelativeLayout.LayoutParams titleparam=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//		titleparam.addRule(RelativeLayout.ABOVE, iv.getId());
		titleparam.addRule(RelativeLayout.CENTER_HORIZONTAL);
		titleparam.topMargin=imgaeWidth/2-lp.width/6;
		title.setLayoutParams(titleparam);
		addView(title);
		ImageView iv=(ImageView)findViewById(R.id.get_gift_button);
		
		if(iv!=null){
			RelativeLayout.LayoutParams ivparam=(LayoutParams) iv.getLayoutParams();
			if(ivparam!=null){
				
				ivparam.height=lp.width/6;
				ivparam.width=lp.width/6;
//				ivparam.topMargin=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
				ivparam.addRule(BELOW,R.id.title);
				ivparam.addRule(CENTER_HORIZONTAL);
			}
		}
		TextView message=new TextView(getContext());
		message.setId(R.id.books_titles);
		if(intent!=null){
			message.setText(intent.getStringExtra(GiftBagUtil.advDesc_key));
		}
		message.setGravity(Gravity.CENTER);
		message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
		message.setTextColor(Color.WHITE);
		message.setLines(1);
		RelativeLayout.LayoutParams messageparam=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		messageparam.topMargin=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
		messageparam.addRule(RelativeLayout.BELOW, iv.getId());
		messageparam.addRule(RelativeLayout.CENTER_HORIZONTAL);
		message.setLayoutParams(messageparam);
		addView(message);
		iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(LoginUser.getInstance().isLogin()){
					GiftBagUtil.getInstance().getGiftBag(getContext());
				}else{
					logon.giftBagLogo();
				}
			}
		});
	}
}
