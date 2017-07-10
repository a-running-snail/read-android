package com.jingdong.app.reader.entity;

import java.lang.ref.SoftReference;

import com.jingdong.app.reader.util.MD5Calculator;

import android.graphics.Bitmap;
import android.widget.ImageView;

public abstract class Entity  implements Cloneable{
	public static final int STATE_INIT = -2;// 未加载
	public static final int STATE_FAILED = -1;
	public static final int STATE_LOADING = 0;
	public static final int STATE_LOADED = 1;
	public int loadState = STATE_INIT;// 0为0初始
	public String urlKey;
	public String pathKey;
	public int num;
	public ImageView img;
	public SoftReference<Bitmap> imgCache;

	public abstract String getImageUrl();
	
	public abstract String getHomeImageUrl();
	
	public String getFilePath(){
		return null;
	}
	
	public boolean isRequestsNEt(){
		return true;
	}
	
	public String getUrlkey() {
		if (urlKey == null) {
			urlKey = MD5Calculator.calculateMD5(getImageUrl());
		}
		return urlKey;
	}
	
	public String getPathKey() {
		if (pathKey == null) {
			pathKey = MD5Calculator.calculateMD5(getFilePath());
		}
		return pathKey;
	}
}
