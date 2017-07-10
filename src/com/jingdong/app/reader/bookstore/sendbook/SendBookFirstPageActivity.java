package com.jingdong.app.reader.bookstore.sendbook;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.bookshelf.BookcaseLocalFragmentNewUI;
import com.jingdong.app.reader.bookshelf.animation.EBookAnimationUtils;
import com.jingdong.app.reader.common.CommonActivity;

public class SendBookFirstPageActivity extends CommonActivity {

	public static final int SENDBOOKFIRSTPAGE = 223;
	private Context context;
	private TextView sendNicknameTv;
	private TextView wishTv,wishTv2,wishTv3;
	final int RIGHT = 0;
	final int LEFT = 1;
	private GestureDetector gestureDetector;
	private GestureDetector.OnGestureListener onGestureListener;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		setContentView(R.layout.activity_sendbook_firstpage);
		
		
		context =this;
		
		initView();
		initListener();
		gestureDetector = new GestureDetector(this,onGestureListener); 
	}

	private void initView() {
		
		sendNicknameTv = (TextView) findViewById(R.id.send_nickname);
		wishTv = (TextView) findViewById(R.id.wish);
		wishTv2 = (TextView) findViewById(R.id.wish2);
		wishTv3 = (TextView) findViewById(R.id.wish3);
		
		Intent intent = getIntent();
		if(intent==null)
			return ;
		if(!TextUtils.isEmpty(intent.getStringExtra("sendNickName")))
			sendNicknameTv.setText(separateNickName(intent.getStringExtra("sendNickName"))+"赠");
		
		if(!TextUtils.isEmpty(intent.getStringExtra("sendMsg"))){
			if(intent.getStringExtra("sendMsg").equals("养心莫若寡欲，至乐无如读书")){
				wishTv.setText("养心莫若寡欲");
				wishTv2.setText("至乐无如读书");
				wishTv3.setVisibility(View.GONE);
			}else{
				List<String> strList = separateWords(intent.getStringExtra("sendMsg"));
				if(strList.size()>0)
					wishTv.setText(strList.get(0));
				else
					wishTv.setVisibility(View.GONE);
				if(strList.size()>1)
					wishTv2.setText(strList.get(1));
				else
					wishTv2.setVisibility(View.GONE);
				if(strList.size()>2)
					wishTv3.setText(strList.get(2));
				else
					wishTv3.setVisibility(View.GONE);
			}
		}
		
	}
	
	/**
	 * 分割赠言
	 * @param originString
	 * @return
	 */
	private List<String> separateWords(String originString){
		List<String> strList = new ArrayList<String>();
		int index = 0 ;
		boolean preIsChinese =true;
		StringBuffer buffer =new StringBuffer();
		char[] array = originString.toCharArray();
		for (int i = 0; i < array.length; i++) {
		    if((char)(byte)array[i]!=array[i]){
		        buffer.append(array[i]);
		        preIsChinese=true;
		    }else{
		    	String joinstring;
		    	if(preIsChinese)
		    		joinstring =array[i]+"  ";
		    	else
		    		joinstring ="  "+array[i]+"  ";
		    	buffer.append(joinstring);
		    	preIsChinese=false;
		    }
		    index = i+1;
		    if(index%10 == 0){
		    	strList.add(buffer.toString());
		    	buffer.setLength(0);
		    }
		}
		if(buffer.length()!=0)
			strList.add(buffer.toString());
		return strList;
	}
	
	/**
	 * 分割昵称
	 * @param nickName
	 * @return
	 */
	private String separateNickName(String nickName){
		StringBuffer buffer =new StringBuffer();
		boolean preIsChinese=true;
		char[] array = nickName.toCharArray();
		for (int i = 0; i < array.length; i++) {
		    if((char)(byte)array[i]!=array[i]){
		        buffer.append(array[i]);
		        preIsChinese =  true;
		    }else{
		    	String joinstring;
		    	if(preIsChinese)
		    		joinstring =array[i]+"  ";
		    	else
		    		joinstring ="  "+array[i]+"  ";
		    	buffer.append(joinstring);
		    	preIsChinese=false;
		    }
		}
		return buffer.toString();
	}

	private void initListener(){
		onGestureListener =  new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
					float velocityY) {
				float x = e2.getX() - e1.getX();
				float y = e2.getY() - e1.getY();

				if (x > 0) {
					doResult(RIGHT);
				} else if (x < 0) {
					doResult(LEFT);
				}
				return true;
			}
		};
	}

	public boolean onTouchEvent(MotionEvent event) {
		if(gestureDetector == null){
			if(onGestureListener == null){
				initListener();
			}
			gestureDetector = new GestureDetector(this,onGestureListener); 
		}
		return gestureDetector.onTouchEvent(event);
	}

	public void doResult(int action) {

		switch (action) {
		case RIGHT:
			break;
		case LEFT:
			finish();
			overridePendingTransition(0, R.anim.left_out);  
			break;

		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus){
			EBookAnimationUtils anim = BookcaseLocalFragmentNewUI.getEBookAnimationUtils();
	    	if (null != anim) {
	    		anim.hideWindow();
	    	}
		}
		super.onWindowFocusChanged(hasFocus);
	}
	
	
	@Override
	public void onBackPressed() {
		setResult(SENDBOOKFIRSTPAGE);
		finish();
		super.onBackPressed();
		
	}
	
}
