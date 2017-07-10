package com.jingdong.app.reader.extension.giftbag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jingdong.app.reader.activity.LauncherActivity;

/**
 * 渠道礼包广播接收
 *
 */
public class GiftBagUiReciver extends BroadcastReceiver {
	private LauncherActivity launcherActivity = null;
	public static final String ACTION="com.jingdong.app.reader.giftbag.gotome";

	public GiftBagUiReciver() {
	}
	
	/**
	 * 渠道礼包广播消息接收
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent==null){
			return;
		}
		
		if(launcherActivity!=null){
			//显示社区
			launcherActivity.showMainLayoutView(2);//社区
			launcherActivity.removeUserGuider();//移除新手引导相关界面
		}
	}

	public LauncherActivity getLauncherActivity() {
		return launcherActivity;
	}

	public void setLauncherActivity(LauncherActivity launcherActivity2) {
		this.launcherActivity = launcherActivity2;
	}
}
