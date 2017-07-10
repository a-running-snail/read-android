package com.jingdong.app.reader.album;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.TimelineRootFragment;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelinePostTweetActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class FunctionPopView extends PopupWindow implements OnTouchListener, OnClickListener{
	private Activity mActivity = null;
	private Fragment mFragment;
	
	public FunctionPopView(Fragment fragment) {
		mActivity = fragment.getActivity();
		mFragment=fragment;
		View rootView = LayoutInflater.from(mActivity).inflate(R.layout.function_popview, null);
		rootView.findViewById(R.id.comment).setOnClickListener(this);
		rootView.findViewById(R.id.upload).setOnClickListener(this);
		
		this.setBackgroundDrawable(new BitmapDrawable());   
		this.setOutsideTouchable(true);
		this.setTouchInterceptor(this);
		this.setContentView(rootView);
		this.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
		this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
		this.setFocusable(true); 
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {  
			this.dismiss();  
            return true;  
        } 
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.comment:
				dismiss();
				
				Intent it2 = new Intent(mActivity, TimelineBookListCommentsActivity.class);
				it2.putExtra("type", TimelineBookListActivity.type[3]);
				mFragment.startActivityForResult(it2, TimelineRootFragment.POST_TWEET);
				break;
			case R.id.upload:
				dismiss();
				
				Intent it1 = new Intent(mActivity, TimelinePostTweetActivity.class);
				it1.putExtra("title", mActivity.getString(R.string.timeline_post_title));
				mFragment.startActivityForResult(it1, TimelineRootFragment.POST_TWEET);
				break;
			default:
				break;
		}
	}

}
