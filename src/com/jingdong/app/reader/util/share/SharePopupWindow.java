package com.jingdong.app.reader.util.share;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity.OnPopItemClickedListener;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity.OnShareItemClickedListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class SharePopupWindow extends PopupWindow {

	private View mMenuView;

	/**
	 * 长按评论回复，弹出窗口，可以复制或删除评论
	 * @param context
	 * @param itemsOnClick
	 * @param showDelete
	 * @param position
	 */
	public SharePopupWindow(Activity context,
			final OnShareItemClickedListener itemsOnClick,
			boolean showDelete, final int position) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(
				R.layout.activity_timeline_popupwindow, null);

		FrameLayout delete = (FrameLayout) mMenuView
				.findViewById(R.id.delete);
		FrameLayout copy = (FrameLayout) mMenuView.findViewById(R.id.copy);

		if (!showDelete)
			delete.setVisibility(View.GONE);

		delete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (itemsOnClick != null)
					itemsOnClick.onShareItemClicked(101, position);
				dismiss();

			}
		});

		copy.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (itemsOnClick != null)
					itemsOnClick.onShareItemClicked(102, position);
				dismiss();
			}
		});

		this.setContentView(mMenuView);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		this.setTouchable(true);
		this.setOutsideTouchable(true);
		this.setBackgroundDrawable(new ColorDrawable(MZBookApplication.getContext().getResources()
				.getColor(R.color.bg_menu_shadow)));

	}
	
	/**
	 * 写书评popupwindow
	 * @param context
	 * @param itemsOnClickcommentBt
	 * @param showDelete
	 * @param position
	 */
	String content;
	public SharePopupWindow(final Activity context,
			final OnPopItemClickedListener itemsOnClick,
			String replyUser, final int position) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(
				R.layout.edit_comment_popupwindow, null);

		
		final EditText commentEditText = (EditText) mMenuView.findViewById(R.id.commentContent);
		commentEditText.setHint("回复 "+replyUser+":");
		TextView tvBt= (TextView) mMenuView.findViewById(R.id.commentBt);
		
		tvBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (itemsOnClick != null){
					content= commentEditText.getText().toString();
					if (content!=null && content.length() == 0) {
						Toast.makeText(context,
								R.string.post_without_word, Toast.LENGTH_SHORT).show();
					} else if (content!=null && content.length() > TimelineTweetActivity.MAX_COMMENT_TEXT) {
						Toast.makeText(context,
								R.string.max_comment_text, Toast.LENGTH_SHORT).show();
					} else {
						itemsOnClick.onPopItemClicked(content, position);
						dismiss();
					}
				}
				

			}
		});
		
		commentEditText.requestFocus();
		InputMethodManager imm=  (InputMethodManager) commentEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);

		this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		
		this.setContentView(mMenuView);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		
		this.setFocusable(true);
		this.setBackgroundDrawable(new ColorDrawable(MZBookApplication.getContext().getResources()
				.getColor(R.color.bg_menu_shadow)));
		this.setOutsideTouchable(false);

	}
	
	/**
	 * 分享弹出窗口
	 * @param context
	 * @param itemsOnClick
	 * @param position
	 */
	public SharePopupWindow(Activity context,
			final OnShareItemClickedListener itemsOnClick,final int position) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(
				R.layout.activity_share_popupwindow, null);

		RelativeLayout weibo = (RelativeLayout) mMenuView
				.findViewById(R.id.popup_sina);
		RelativeLayout wechat_friend = (RelativeLayout) mMenuView.findViewById(R.id.popup_weixin_friend);
		
		RelativeLayout wechat = (RelativeLayout) mMenuView.findViewById(R.id.popup_weixin);
		LinearLayout cancel = (LinearLayout) mMenuView.findViewById(R.id.cancel);
		weibo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (itemsOnClick != null)
					itemsOnClick.onShareItemClicked(101, position);
				dismiss();
			}
		});
		
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
		
		wechat.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (itemsOnClick != null)
					itemsOnClick.onShareItemClicked(103, position);
				dismiss();
			}
		});

		wechat_friend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (itemsOnClick != null)
					itemsOnClick.onShareItemClicked(102, position);
				dismiss();
			}
		});

		this.setContentView(mMenuView);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.MATCH_PARENT);
		this.setFocusable(true);
		this.setTouchable(true);
		this.setOutsideTouchable(true);
		this.setBackgroundDrawable(new ColorDrawable(MZBookApplication.getContext().getResources()
				.getColor(R.color.bg_menu_shadow)));

	}

}