package com.jingdong.app.reader.album;

import com.jingdong.app.reader.R;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;

public class LoadingDialog extends Dialog{
	
	public LoadingDialog(Context context) {
		super(context, R.style.CustomDialog); 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.loading_dialog);
		
		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){ 
//        	dismiss();
        	return false; 
        }
        return super.onKeyDown(keyCode, event);
    }

}