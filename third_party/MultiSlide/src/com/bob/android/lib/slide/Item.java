package com.bob.android.lib.slide;

import java.io.BufferedOutputStream;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Item {
    public String ADDING = "-2";
    public String LOADING = "0";
    public String FAILED = "-1";
	public int id;
	public Item upItem;
	public Item nextItem;
	public View currentView;
	ImageView mImageView;
	public View rootView;
	private Context mContext;
	Bitmap bitmap;
	private boolean isLoaded = false;

	public boolean isLoaded() {
		return isLoaded;
	}

	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	public Item(Context context) {
		mContext = context;
	}

	public void load(String path){
		setView(path);
	}

	public void load(){
		setView("");
	}

	public void setView(String path){

	}

	public void saveImageToLocal(){

	}

//	public void setOffWidth(){
//
//	}

    public void upDatePhoto(String path){


	}

	public void setSelected() {
//		ImageView imageView = (ImageView) currentView
//				.findViewById(R.id.imageView);
//		imageView.requestFocus();
	}

}