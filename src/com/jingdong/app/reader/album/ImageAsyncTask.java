package com.jingdong.app.reader.album;

import com.jingdong.app.reader.album.ImageAsyncTask.ICallBack;
import com.jingdong.app.reader.preloader.CutBitmapDisplayer;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class ImageAsyncTask {
	
	public static void getBitmapForCache(String path, int imageWidth, ICallBack mICallBack) {
		DownloadAsyncTask task = new DownloadAsyncTask(imageWidth, mICallBack);
		task.execute(path);
	}
	
	public interface ICallBack {
		public void SuccessCallback(String url, Bitmap mBitmap);
	}
	
}

class DownloadAsyncTask extends AsyncTask<String, String, Bitmap>{
	private String path = null;
	private int imageWidth = 0;
	private ICallBack mICallBack = null;
	
	public DownloadAsyncTask(int imageWidth, ICallBack _mICallBack) {
		this.imageWidth = imageWidth;
		this.mICallBack = _mICallBack;
	}

	@Override
	protected Bitmap doInBackground(String... arg0) {
		path = arg0[0];
		if(0 == imageWidth) {
			return ImageManager.getBitmapFromCache(path);
		}else {
			return ImageManager.getBitmapFromCache(path, imageWidth);
		}
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		if (null != mICallBack) {
			Bitmap bitmap = CutBitmapDisplayer.CropForExtraWidth(result, false);
			if(null != bitmap) {
				mICallBack.SuccessCallback(path, bitmap);	
			}else {
				mICallBack.SuccessCallback(path, result);
			}
			
		}
	}
	 
}
