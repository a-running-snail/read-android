package com.jingdong.app.reader.me.activity;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.Header;

import com.android.mzbook.photoview.GestureImageView;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.view.CustomProgreeDialog;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

public class UserPhotoActivity extends MZReadCommonActivity {

	private String url;
	private GestureImageView image;
	private ProgressDialog dialog;
	private int width;
	private int height;
	private Bitmap bitmap;
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {

			if (msg.what == 1) {
				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();
				}
				bitmap = ImageUtils.getUserPhotoBitmap(UserPhotoActivity.this, width, height);
				image.setImageBitmap(bitmap);
				Animation animation = new AlphaAnimation(0.5f, 1.0f);
				animation.setDuration(2000);
				image.setAnimation(animation);
				image.startAnimation(animation);

			}

		};
	
		
	};

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_photo_pre);
		DisplayMetrics metric = this.getResources().getDisplayMetrics();
		width = metric.widthPixels;
		height = metric.heightPixels;
		url = getIntent().getStringExtra("url");
		image = (GestureImageView) findViewById(R.id.image);
		dialog = CustomProgreeDialog.instace(this);
		dialog.show();
		requestPhoto();
	}

	public void  requestPhoto (){


			 WebRequestHelper.get(url, new FileAsyncHttpResponseHandler(UserPhotoActivity.this) {
                 

                 @Override
                 public void onFailure(int arg0,
                         Header[] arg1, Throwable arg2,
                         File arg3) {
                     Toast.makeText(UserPhotoActivity.this, "图片加载失败",
                             Toast.LENGTH_LONG).show();
                     finish();
                     
                 }

                 @Override
                 public void onSuccess(int arg0,
                         Header[] arg1, File response) {
   
                     try {
                         FileInputStream stream=new FileInputStream(response);
                         Bitmap bitmap=BitmapFactory.decodeStream(stream);
                         ImageUtils.saveBitmap(UserPhotoActivity.this,
                                 bitmap, "photo_preview");
                         Message msMessage=new Message();
                         msMessage.what=1;
                         handler.sendMessage(msMessage);
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                     
                 }
             });

		


	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bitmap != null) {
			bitmap.recycle();
		}
	}
}
