package com.jingdong.app.reader.preloader;

import android.graphics.Bitmap;

import com.jingdong.app.reader.util.MZLog;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

public class CutBitmapDisplayer implements BitmapDisplayer {

	public static final float RADIO_CROP_WIDTH = 0.70f;
	private boolean recycle = true;
	
	public CutBitmapDisplayer(boolean recycle2) {
		this.recycle = recycle2;
	}


	@Override
	public void display(Bitmap bitmap, ImageAware imageAware,
			LoadedFrom loadedFrom) {
		imageAware.setImageBitmap(CropForExtraWidth(bitmap,this.recycle));
	}

	public static Bitmap CropForExtraWidth(Bitmap source,boolean needrecycle) {
		Bitmap newBmp = null;
		try {
			if (source == null) {
				return source;
			}
			float ratio = (float) source.getWidth() / (float) source.getHeight();
			if (ratio <= RADIO_CROP_WIDTH) {
				return source;
			}
			float leftCropRatio = (ratio - RADIO_CROP_WIDTH) / 2.0f;
			// float rightCropRatio = leftCropRatio;
			int targetWidth = source.getWidth();
			int x = 0;
			targetWidth = (int) (RADIO_CROP_WIDTH * source.getHeight());
			x = (int) (leftCropRatio * source.getHeight());
			if (source != null && !source.isRecycled()) {
				newBmp = Bitmap.createBitmap(source, x, 0, targetWidth,
						source.getHeight());
				if (!source.equals(newBmp)) {
					if(needrecycle){
						source.recycle(); 
					}
				}else {
					newBmp.recycle();
					newBmp= null;
					newBmp = source;
				}
			}
		} catch (OutOfMemoryError e) {
		}
		return newBmp;
	}
}
