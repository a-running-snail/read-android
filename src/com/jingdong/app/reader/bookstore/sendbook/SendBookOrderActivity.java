package com.jingdong.app.reader.bookstore.sendbook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.entity.extra.JDBookInfo.Detail;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.pay.OnlinePayActivity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.OnLinePayTools;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.TopBarView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;

public class SendBookOrderActivity extends BaseActivityWithTopBar {

	private Context context;
	
	private ImageView bookCoverImg;
	private TextView bookNameTv;
	private TextView authorTv;
	private ImageView gotoPayImg;
	private TextView gotoRuleTv;
	
	
	private String bookId;
	private String bookName;
	private String author;
	private String bookCover;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_sendbook_order);
		
		context =this;
		
		initView();
		
	}

	private void initView() {
		getTopBarView().setTitle(getResources().getString(R.string.sendbook_order_title));
		
		Intent intent = getIntent();
		if (intent==null) {
			return ;
		}
		bookId = intent.getStringExtra("bookId");
		bookName = intent.getStringExtra("bookName");
		author = intent.getStringExtra("author");
		bookCover = intent.getStringExtra("bookCover");
		
		bookCoverImg = (ImageView) findViewById(R.id.book_cover);
		bookNameTv = (TextView) findViewById(R.id.book_name);
		authorTv = (TextView) findViewById(R.id.author);
		gotoPayImg = (ImageView) findViewById(R.id.goto_pay);
		gotoRuleTv = (TextView) findViewById(R.id.goto_rule);
		
		
		if(bookCover!=null){
			ImageLoader.getInstance().displayImage(bookCover, bookCoverImg, GlobalVarable.getCutBookDisplayOptions(false));
		}
		if(!TextUtils.isEmpty(bookName)){
			bookNameTv.setText(bookName);
		}
		authorTv.setText((author == null || "null".equals(author) || "".equals(author)) ? getString(R.string.author_unknown) : author);
		
		gotoPayImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!LoginUser.isLogin()) {
					Intent it = new Intent(context, LoginActivity.class);
					startActivity(it);
					return;
				}
				// 以下添加购买逻辑
				if (OnlinePayActivity.payidList == null)
					OnlinePayActivity.payidList = new ArrayList<String>();
				else
					OnlinePayActivity.payidList.clear();
				OnlinePayActivity.payidList.add(bookId);
				OnLinePayTools.gotoSendBookPay(context, null);
				finish();
			}
		});
		
		gotoRuleTv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String url = "http://e.m.jd.com/buySendRule.html";
				Intent intent = new Intent(context,WebViewActivity.class);
				intent.putExtra(WebViewActivity.UrlKey, url);
				intent.putExtra(WebViewActivity.TopbarKey, true);
				intent.putExtra(WebViewActivity.BrowserKey, false);
				intent.putExtra(WebViewActivity.TitleKey, "送书规则");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		});
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.sendbook_order_title));
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.sendbook_order_title));
	}

}
