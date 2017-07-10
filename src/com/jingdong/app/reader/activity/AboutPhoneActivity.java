package com.jingdong.app.reader.activity;

import java.io.File;

import android.R.integer;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.format.Formatter;  

import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.ui.ActionBarHelper;
import com.jingdong.app.reader.R;

public class AboutPhoneActivity extends MZReadCommonActivity {

	private TextView release;
	private TextView sdk;
	private TextView resolutionRatio;
	private TextView dpi;
	private TextView resFolder;
	private TextView ram;
	private TextView sdcard;
	LinearLayout testLayout;
	
	@Override
	protected void onCreate(Bundle arg0) {

		super.onCreate(arg0);
		setContentView(R.layout.activity_about_phone);
		initField();
		
	}

	public void initField() {

		release = (TextView) findViewById(R.id.version);
		sdk = (TextView) findViewById(R.id.sdk);
		resolutionRatio = (TextView) findViewById(R.id.resolution_ratio);
		dpi = (TextView) findViewById(R.id.dpi);
		resFolder = (TextView) findViewById(R.id.res_folder);
		ram=(TextView) findViewById(R.id.ram);
		sdcard=(TextView) findViewById(R.id.sdcard);
		testLayout=(LinearLayout) findViewById(R.id.test);
		release.setText(android.os.Build.VERSION.RELEASE);
		sdk.setText(android.os.Build.MODEL);

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);

		resolutionRatio.setText(metric.heightPixels + "x" + metric.widthPixels);
		int dpiString = metric.densityDpi;
		dpi.setText(metric.densityDpi + "");

		if (dpiString < 150)
			resFolder.setText("ldpi");
		else if (dpiString < 190) {
			resFolder.setText("mdpi");
		} else if (dpiString < 260) {
			resFolder.setText("hdpi");
		} else if (dpiString < 360) {
			resFolder.setText("xhdpi");
		} else if (dpiString < 520) {
			resFolder.setText("xxhdpi");
		} else if (dpiString > 600) {
			resFolder.setText("xxxhdpi");
		} else

			resFolder.setText("unknown");
		
		ram.setText(getSystemAvaialbeMemorySize());
		sdcard.setText(readSDCard());
		
		testLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				
			}
		});
		
		
		
	}
	

	
	private String getSystemAvaialbeMemorySize(){  
        MemoryInfo memoryInfo = new MemoryInfo() ;  
        ActivityManager manager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);  
        manager.getMemoryInfo(memoryInfo) ;  
        long memSize = memoryInfo.availMem ;  
        String availMemStr = Formatter.formatFileSize(this, memSize);
        return availMemStr ;  
    }  
	
	private String readSDCard() { 
        String state = Environment.getExternalStorageState(); 
        if(Environment.MEDIA_MOUNTED.equals(state)) { 
            File sdcardDir = Environment.getExternalStorageDirectory(); 
            StatFs sf = new StatFs(sdcardDir.getPath()); 
            long blockSize = sf.getBlockSize(); 
            long blockCount = sf.getBlockCount(); 
            long availCount = sf.getAvailableBlocks(); 
            long sdcardSize=availCount*blockSize;
            return Formatter.formatFileSize(this, sdcardSize); 
        }    
        return "sdcard unknown error";
    } 

}
