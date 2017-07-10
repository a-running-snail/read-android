package com.jingdong.app.reader.activity;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.Bundle;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;

public class SelectSaveDirActivity extends BaseActivityWithTopBar {

	private LinearLayout container=null;
	private View [] allChildViews=null;
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);

		setContentView(R.layout.activity_select_save_dir);
		container=(LinearLayout) findViewById(R.id.container);
		String[] paths = getVolumnPaths(SelectSaveDirActivity.this);
		
		
		if(null==paths||paths.length==0)
			finish();
		
		allChildViews=new View[paths.length];
		
		for(int i=0;i<paths.length;i++)
		{
			
			View views =LayoutInflater.from(SelectSaveDirActivity.this).inflate(R.layout.item_select_save_dir, null);
			TextView title =(TextView) views.findViewById(R.id.title);
			final ImageView imageView =(ImageView) views.findViewById(R.id.image);
			View line=views.findViewById(R.id.line);
			
			if(TextUtils.isEmpty(LocalUserSetting.getSaveBookDir(SelectSaveDirActivity.this))&&i==0)
			{
				imageView.setVisibility(View.VISIBLE);
			}
			else if(LocalUserSetting.getSaveBookDir(SelectSaveDirActivity.this).equals(paths[i])){
				imageView.setVisibility(View.VISIBLE);
			}
			
			else {
				imageView.setVisibility(View.GONE);
			}
			
			if(i==paths.length-1)
			{
				line.setVisibility(View.GONE);
			}
			else {
				line.setVisibility(View.VISIBLE);
			}
			
			title.setText(paths[i].replace("/storage/", "")+getSizeString(paths[i]));
			
			final String path =paths[i];
			final int position =i;
			
			views.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					for(int i=0;i<allChildViews.length;i++)
					{
						
						if(allChildViews[i]==null)
							continue;
						
						 ImageView imageViewss =(ImageView) allChildViews[i].findViewById(R.id.image);
						 imageViewss.setVisibility(View.GONE);
					}
					
					if(imageView.getVisibility()==View.GONE)
					 {
						imageView.setVisibility(View.VISIBLE);
					
						if(position!=0)
						    LocalUserSetting.saveBookDir(SelectSaveDirActivity.this, path);
						else {
							LocalUserSetting.saveBookDir(SelectSaveDirActivity.this, "");
						}
					 }
					else {
						 imageView.setVisibility(View.GONE);
					}
				}
			});
			
			MZLog.d("wangguodong", "#################"+paths[i]);
			
			if(isAvaliable(paths[i]))
			{
				allChildViews[i]=views;
				container.addView(views);
			}
			
			
		}
		
		
	}
	
	public boolean isAvaliable(String path){
		StatFs stat = new StatFs(path);
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		long availableBlocks = stat.getAvailableBlocks();

		long totalSize = totalBlocks * blockSize;
		long availSize = availableBlocks * blockSize;

		if(availSize>0)
		{
			return true;
		}
		
		return false;
		
	}
	
	
	public String getSizeString(String path){
		StatFs stat = new StatFs(path);
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		long availableBlocks = stat.getAvailableBlocks();

		long totalSize = totalBlocks * blockSize;
		long availSize = availableBlocks * blockSize;

		String totalStr = Formatter.formatFileSize(this, totalSize);
		String availStr = Formatter.formatFileSize(this, availSize);
		
		return "("+availStr+"可用，共"+totalStr+")";
		
	}

	public String[] getVolumnPaths(Context context) {

		String[] paths = null;
		try {
			StorageManager mStorageManager = (StorageManager) context
					.getSystemService(Context.STORAGE_SERVICE);
			Method mMethodGetPaths = mStorageManager.getClass().getMethod(
					"getVolumePaths");

			paths = (String[]) mMethodGetPaths.invoke(mStorageManager);// 调用该方法
			MZLog.d("wangguodong", "Storage'paths[0]:" + paths[0]);
			MZLog.d("wangguodong", "Storage'paths[1]:" + paths[1]);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return paths;
	}

}
