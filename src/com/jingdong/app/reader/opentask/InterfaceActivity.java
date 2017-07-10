package com.jingdong.app.reader.opentask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.jingdong.app.reader.opentask.InterfaceBroadcastReceiver.Command;

public class InterfaceActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Command command = Command.createCommand(getIntent());
		if(command!=null){
			Intent intent = new Intent(InterfaceBroadcastReceiver.ACTION);
			Bundle bundle = command.getBundle();
			intent.putExtras(bundle);
			sendBroadcast(intent);
			finish();
		}
	}
}
