package com.jingdong.app.reader.activity;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.view.bookstore.ImageTouchView;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class BookStoreBookBigViewActivity extends Activity {
	
//	ImageTouchView imageTouchView;
//	CloseBigViewInterface listener;
	ImageView imageView;

	@Override
	protected void onCreate(Bundle arg0) {

		super.onCreate(arg0);
		 //设置无标题  
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        //设置全屏  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		setContentView(R.layout.activity_bookstore_book_bigview);
		String logo= getIntent().getStringExtra("largeLogo");
		logo=logo.replace("n1", "n12");
		imageView=(ImageView)findViewById(R.id.img);
		ImageLoader.getInstance().displayImage(logo, imageView);
		
		
//		String logo= getIntent().getStringExtra("largeLogo");
//		logo=logo.replace("n1", "n12");
//		imageTouchView=(ImageTouchView)findViewById(R.id.img);
//		ImageLoader.getInstance().displayImage(logo, imageTouchView);
//		imageTouchView.setCloseListener(new CloseBigViewInterface() {
//			@Override
//			public void onclick() {
//				finish();
//			}
//		});
		
	}

	

}

