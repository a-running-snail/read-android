package com.jingdong.app.reader.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.jingdong.app.reader.util.BookStoreCacheManager;
import com.jingdong.app.reader.util.MZLog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class UiCacheUpdate extends BroadcastReceiver {
	public static final String ACTION="com.jingdong.app.reader.UiCacheUpdate";
	public static final String KEY="key";
	public static final String URL="url";
	public static final String BOOKKEY="BOOKKEY";
	public static final String WIDTH="width";
	public static final String HEIGHT="height";
	public static final String GRIVATY="grivaty";
	public ImageView iv=null;
	private static final String TAG="UiCacheUpdate";
	BookStoreCacheManager cacheManager=BookStoreCacheManager.getInstance();
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		if(intent!=null){
			if(iv!=null){
				if(intent.getBooleanExtra(BOOKKEY, false)){
					ImageLoader.getInstance().displayImage(
							intent.getStringExtra(URL), iv,
							GlobalVarable.getCutBookDisplayOptions(false));

				}else{
					ImageLoader
					.getInstance()
					.displayImage(
							intent.getStringExtra(URL),
							iv,
							GlobalVarable
									.getDefaultPublisherDisplayOptions());
				
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
