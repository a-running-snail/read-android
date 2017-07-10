package com.jingdong.app.reader.activity;

import java.io.File;
import java.util.Date;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.album.PhotoViewAttacher;
import com.jingdong.app.reader.album.PhotoViewAttacher.OnPhotoTapListener;
import com.jingdong.app.reader.album.PhotoViewAttacher.OnViewTapListener;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.MZReadCommonActivityWithActionBar;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity.OnShareItemClickedListener;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.util.share.SharePopupWindow;
import com.jingdong.app.reader.util.share.ShareResultListener;
import com.jingdong.app.reader.util.share.WBShareHelper;
import com.jingdong.app.reader.util.share.WXShareHelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BookPageImageEnlargeActivity extends
		MZReadCommonActivityWithActionBar {
//	private GestureImageView image;
	private ImageView image;
	int width = 0;
	int height = 0;
	private TextView saveTv;
	private TextView shareTv;
	private Bitmap bitmap;
	private String resource;
	private String bookId,bookName;
	
	private SharePopupWindow menuWindow;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_bookpage_image_enlarge);
		resource = getIntent().getStringExtra("imageResource");
//		image = (GestureImageView) findViewById(R.id.image);
		image = (ImageView) findViewById(R.id.image);
		saveTv = (TextView) findViewById(R.id.save_image);
		shareTv = (TextView) findViewById(R.id.share_image);
		
		if(getIntent()!=null){
			bookId = getIntent().getStringExtra("bookId");
			bookName = getIntent().getStringExtra("bookName");
		}

//		image.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				
//			}
//		});

		if (UiStaticMethod.isEmpty(resource)) {
			finish();
		} else {
			DisplayMetrics metric = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metric);
			width = metric.widthPixels; // 屏幕宽度（像素）
			height = metric.heightPixels; // 屏幕高度（像素）
			bitmap = ImageUtils.getBitmapFromNamePath(resource, width,
					height);
			image.setImageBitmap(bitmap);
			
			PhotoViewAttacher attacher = new PhotoViewAttacher(image);
			attacher.setMaxScale(10);
			attacher.setOnViewTapListener(new OnViewTapListener() {
				@Override
				public void onViewTap(View view, float x, float y) {
					finish();
					overridePendingTransition(0, R.anim.alpha_out_shorttime);
				}
			});
		}
		
		saveTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String[] arr = resource.split("/");
				String fileName = "" ; 
				if(arr !=null && arr.length > 0){
					fileName = arr[arr.length-1];
				}else{
					fileName = String.valueOf(new Date().getTime())+".jpg";
				}
				String dirPath = MZBookApplication.getInstance().getCachePath()+File.separator+"downloads";
				File dir = new File(dirPath);
				if(!dir.exists()) {
					dir.mkdirs();
				}
				String filePath = dirPath + File.separator + fileName;
				boolean result = ImageUtils.saveFile(bitmap, filePath);
				if(result){
					MediaScannerConnection.scanFile(BookPageImageEnlargeActivity.this, new String[]{filePath}, null, null);
					String msgSavePic = getResources().getString(R.string.save_image_success_tip) + ": " + filePath;
					Toast.makeText(BookPageImageEnlargeActivity.this, msgSavePic, Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(BookPageImageEnlargeActivity.this, getResources().getString(R.string.save_image_fail_tip), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		shareTv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Bitmap watermark = BitmapFactory.decodeResource(getResources(), R.drawable.jdlebook_water_mark);
				
				final Bitmap shareBitmap = ImageUtils.watermarkBitmap(bitmap,watermark);
				
				
				String text = "分享了来自《%s》的图片 @京东阅读 http://e.m.jd.com/ebook/%s.html ";
				if(!TextUtils.isEmpty(bookId)&& !TextUtils.isEmpty(bookName))
					text = String.format(text, bookName, bookId);
				final String shareText =text;
				menuWindow = new SharePopupWindow(BookPageImageEnlargeActivity.this, new OnShareItemClickedListener() {

					@Override
					public void onShareItemClicked(int type, int position) {

						switch (type) {
						case 101:// 微博分享使用原图，不加水印
							WBShareHelper.getInstance().doShareByBitmap(BookPageImageEnlargeActivity.this, shareText, "", bitmap, "", "", new ShareResultListener() {
								@Override
								public void onShareRusult(int resultType) {
									switch (resultType) {
									case ShareResultListener.SHARE_SUCCESS:
										Toast.makeText(BookPageImageEnlargeActivity.this, "分享成功", Toast.LENGTH_LONG).show();
										break;
									case ShareResultListener.SHARE_CANCEL:
										Toast.makeText(BookPageImageEnlargeActivity.this, "分享取消", Toast.LENGTH_LONG).show();
										break;
									case ShareResultListener.SHARE_FAILURE:
										Toast.makeText(BookPageImageEnlargeActivity.this, "分享失败", Toast.LENGTH_LONG).show();
										break;
									}
								}
							});
							break;
						case 102:// wechat_friend
							WXShareHelper.getInstance().shareImage(BookPageImageEnlargeActivity.this, shareBitmap, 1, new ShareResultListener()  {
								
								@Override
								public void onShareRusult(int resultType) {
									switch (resultType) {
									case ShareResultListener.SHARE_SUCCESS:
										Toast.makeText(BookPageImageEnlargeActivity.this, "分享成功", Toast.LENGTH_LONG).show();
										break;
									case ShareResultListener.SHARE_CANCEL:
										Toast.makeText(BookPageImageEnlargeActivity.this, "分享取消", Toast.LENGTH_LONG).show();
										break;
									case ShareResultListener.SHARE_FAILURE:
										Toast.makeText(BookPageImageEnlargeActivity.this, "分享失败", Toast.LENGTH_LONG).show();
										break;
									}
								}
							});
							break;
						case 103:// wechat
							WXShareHelper.getInstance().shareImage(BookPageImageEnlargeActivity.this, shareBitmap, 0, new ShareResultListener() {
								
								@Override
								public void onShareRusult(int resultType) {
									switch (resultType) {
									case ShareResultListener.SHARE_SUCCESS:
										Toast.makeText(BookPageImageEnlargeActivity.this, "分享成功", Toast.LENGTH_LONG).show();
										break;
									case ShareResultListener.SHARE_CANCEL:
										Toast.makeText(BookPageImageEnlargeActivity.this, "分享取消", Toast.LENGTH_LONG).show();
										break;
									case ShareResultListener.SHARE_FAILURE:
										Toast.makeText(BookPageImageEnlargeActivity.this, "分享失败", Toast.LENGTH_LONG).show();
										break;
									}
								}
							});
							break;
						}
						menuWindow.dismiss();
					}
				},  -1);
				menuWindow.showAtLocation(BookPageImageEnlargeActivity.this.findViewById(R.id.content),
				Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			}
		});

	}

}
