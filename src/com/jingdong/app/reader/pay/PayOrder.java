package com.jingdong.app.reader.pay;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;




/**
 * Copyright 2011 Jingdong Android Mobile Application
 * 
 * @author ThinkinBunny
 * 
 *         Time: 2011-12-15
 * 
 *         Name: 购物车列表
 * 
 *         Description: 电纸书购物车
 */
public class PayOrder 
{

	static public void toOrder(String token,Activity activity) 
    {
	    		
        StringBuilder sBuilder = new StringBuilder();
		//sBuilder.append("http://order.e.jd.com" +"/ebookorder_orderStep1.action"+ "?tokenKey=");

		sBuilder.append("http://order.e.jd.com" 
		+"/order_orderStep1.action"
				+ "?tokenKey=");


        sBuilder.append(token);
		Intent intent = new Intent(activity.getApplicationContext(),
				OnlinePayActivity.class);
		intent.putExtra("url", sBuilder.toString());
		intent.putExtra("key", OnlinePayActivity.FromEShoppingCarActivity);
		activity.startActivity(intent);
        
		//UPPayAssistEx.startPayByJAR(this, PayActivity.class, null, null,"201501141504190099172","01");
		//int ret = UPPayAssistEx.startPay(this, null, null, "201501141504190099172", "01");
	
    }
	static public void toOrder(Activity activity,String token,String json) 
	{
		
//		StringBuilder sBuilder = new StringBuilder();
//		//sBuilder.append("http://order.e.jd.com" +"/ebookorder_orderStep1.action"+ "?tokenKey=");
//		
//		sBuilder.append("http://order.e.jd.com/client.action?" 
//				+"functionId=newOrderListPay&body="
//				+ json);
		
		
//		sBuilder.append(token);
		if(null == activity || (null != activity && activity.isFinishing())) {
			return;
		}
		
		Intent intent = new Intent(activity.getApplicationContext(),
				OnlinePayActivity.class);
		intent.putExtra("url", token);
		intent.putExtra("key", OnlinePayActivity.FromEShoppingCarActivity);
		activity.startActivity(intent);
		
		//UPPayAssistEx.startPayByJAR(this, PayActivity.class, null, null,"201501141504190099172","01");
		//int ret = UPPayAssistEx.startPay(this, null, null, "201501141504190099172", "01");
		
	}
	
	
	
	
}
