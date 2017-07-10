package com.jingdong.app.reader.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity{

	 // IWXAPI 是第三方app和微信通信的openapi接口  
    private IWXAPI api;
	public static final String WXAPP_ID = "wx79f9198071040f23";
	private WXEventCallback callBack;
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        api = WXAPIFactory.createWXAPI(this, WXAPP_ID, false); 
        callBack =WXEventCallback.getInstance();
        callBack.setmActivity(this);
        api.handleIntent(getIntent(), callBack);  
    } 
    
    
    @Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if(callBack!=null){
			api.handleIntent(intent, callBack);
		}
	}
    
    
}
