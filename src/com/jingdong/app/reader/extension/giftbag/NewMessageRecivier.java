package com.jingdong.app.reader.extension.giftbag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.util.MZLog;

/**新消息广播
 * @author ThinkPad
 *
 */
public class NewMessageRecivier extends BroadcastReceiver {
	public static final String ACTION_ADD="com.jingdong.app.reader.giftbag.NewMessageRecivier.add";
	public static final String ACTION_REDUCE="com.jingdong.app.reader.giftbag.NewMessageRecivier.reduce";
	public static final String MESSAGE_COUNT="message_count";
	public static final String PAGE_INDEX="pageindex";
	private LauncherActivity launcherActivity=null;
	public LauncherActivity getLauncherActivity() {
		return launcherActivity;
	}

	public void setLauncherActivity(LauncherActivity launcherActivity2) {
		this.launcherActivity = launcherActivity2;
	}

	public NewMessageRecivier() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent==null){
			return;
		}
		
		MZLog.d("J", "onReceive-->intent"+intent.getAction());
		
		String action=intent.getAction();
		if(ACTION_ADD.equals(action)){//增加消息
			Log.d("cj", "this==========>>>>>>");
			launcherActivity.notifyMessage(intent.getIntExtra(PAGE_INDEX, 3),"1");
			return;
		}
		if(ACTION_REDUCE.equals(action)){//解除消息
			launcherActivity.clearMsg(intent.getIntExtra(PAGE_INDEX, 3));
			return ;
		}
	}

}
