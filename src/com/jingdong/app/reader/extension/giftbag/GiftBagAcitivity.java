package com.jingdong.app.reader.extension.giftbag;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.util.MZLog;

public class GiftBagAcitivity extends Activity implements GiftBagLognInterface {
	GifitBagRecivier recivier;
	private String tag=getClass().getSimpleName();
	private Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_giftbag);
		recivier=new GifitBagRecivier(this);
		IntentFilter intentFilter=new IntentFilter(GifitBagRecivier.ACTION);
		registerReceiver(recivier, intentFilter);
		GiftBagView giftBagView=(GiftBagView) findViewById(R.id.giftbagvessel);
		giftBagView.setLogon(this);
		 intent=getIntent();
		giftBagView.setIntent(getIntent());
		this.findViewById(R.id.gift_close).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(intent!=null){
					int id=intent.getIntExtra("id", -1);
					GiftBagUtil.getInstance().updateGiftBagStatus(id, 1);
				}
				finish();
			}
		});
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(intent!=null){
			int id=intent.getIntExtra("id", -1);
			GiftBagUtil.getInstance().updateGiftBagStatus(id, 1);
		}
		super.onBackPressed();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(recivier);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		MZLog.d(tag, "onActivityResult..............");
		if (requestCode == GlobalVarable.REQUEST_CODE_GOTO_FIRST_PAGE) {
			GiftBagUtil.getInstance().getGiftBag(this);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	@Override
	public void giftBagLogo() {
		Intent intent = new Intent(this,
				LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent,
				GlobalVarable.REQUEST_CODE_GOTO_FIRST_PAGE);
	}

}
