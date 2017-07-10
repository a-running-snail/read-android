package com.jingdong.app.reader.view;



import com.jingdong.app.reader.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class JdOptionDialog extends Dialog implements android.view.View.OnClickListener,OnItemClickListener{
	
	public TextView mTitle,mPosBt,mMidBt,mNegBt,mDialogMessage;
	public TestViewProgressBar mProgress;
	private View mTitleView,mOptionBt,mDialogBg;
	private Object object;
	private Context mContext;
	private LinearLayout mContentView;
	
	private View mMessageView;
	private boolean isCancelable=true;
	private boolean mPosDismissbySelf=false;
	private OnCancelListener mOnCancelListener;
	private OnClickListener mPosOnClickListener,mMidOncClickListener,mNegOnClickListener,mSingleOnClickListener;
	public static final int JDOPTONDIALOG_POS_BUTTON=0;
	public static final int JDOPTONDIALOG_MID_BUTTON=1;
	public static final int JDOPTONDIALOG_NEG_BUTTON=2;
	private Display d;
	public JdOptionDialog(Context context) {
		super(context, R.style.context_menu_dialog);
		setContentView(R.layout.item_option_dialog);
		this.mContext=context;
		setCanceledOnTouchOutside(false);
		WindowManager m = ((Activity) context).getWindowManager();   
		 d = m.getDefaultDisplay();  //为获取屏幕宽、高 
		LayoutParams p = getWindow().getAttributes();  //获取对话框当前的参数值 
//		p.height = (int) (d.getHeight() * 0.65);   //高度设置为屏幕的0.6 
 		p.width = (int) (d.getWidth() * 0.95);    //宽度设置为屏幕的0.95 
 		getWindow().setAttributes(p);     //设置生效
  		getWindow().setGravity(Gravity.CENTER);
 		mTitleView=findViewById(R.id.layout_title);
 		mTitle=(TextView)findViewById(R.id.dialog_title);
 		mOptionBt=findViewById(R.id.option_Bt);
 		mProgress=(TestViewProgressBar) findViewById(R.id.app_downloadbar);
 		mDialogMessage=(TextView)findViewById(R.id.dialog_message);
 		mContentView=(LinearLayout)findViewById(R.id.addView);
 		mDialogBg=findViewById(R.id.context_dialog);
 		mMessageView=findViewById(R.id.messageLayout);
 		mPosBt=(TextView)findViewById(R.id.dialog_PosBt);
 		mMidBt=(TextView)findViewById(R.id.dialog_MidBt);
 		mNegBt=(TextView)findViewById(R.id.dialog_NegBt);
  	}
	
	public void setHeaderTitle(int resId){
		this.setHeaderTitle(mContext.getString(resId));
	}
	
	public void setHeaderTitle(String title){
		mTitleView.setVisibility(View.VISIBLE);
		mTitle.setText(title);
	}
	public void setPositiveButton(int resId,OnClickListener listener){
		this.setPositiveButton(mContext.getResources().getString(resId), listener);
	}
	public void setMidlleButton(int resId,OnClickListener listener){
		this.setMidlleButton(mContext.getResources().getString(resId), listener);
	}
	public void setNegativeButton(int resId,OnClickListener listener){
		 this.setNegativeButton(mContext.getResources().getString(resId), listener);
	}
	
	public void setPositiveButton(String content,OnClickListener listener){
		mOptionBt.setVisibility(View.VISIBLE);
		mPosBt.setVisibility(View.VISIBLE);
		mPosBt.setText(content);
		mPosBt.setOnClickListener(this);
		this.mPosOnClickListener=listener;
	}
	public void setMidlleButton(String content,OnClickListener listener){
		mOptionBt.setVisibility(View.VISIBLE);
		mMidBt.setVisibility(View.VISIBLE);
		mMidBt.setText(content);
		mMidBt.setOnClickListener(this);
		this.mMidOncClickListener=listener;
	}
	public void setNegativeButton(String content,OnClickListener listener){
		mOptionBt.setVisibility(View.VISIBLE);
		mNegBt.setVisibility(View.VISIBLE);
		mNegBt.setText(content);
		mNegBt.setOnClickListener(this);
		this.mNegOnClickListener=listener;
	}
 
	public void setOnCancelListener(OnCancelListener onCancelListener){
		this.mOnCancelListener=onCancelListener;
	}
	
	
	public void setCancelable(boolean isCancelable){
		this.isCancelable=isCancelable;
	}
	
	
	public void setMessageColorResources(int resId){
		mDialogMessage.setTextColor(mContext.getResources().getColor(resId));
	}
	
	public void setMessageColor(int color){
		mDialogMessage.setTextColor(color);
	}
	
	public void setMessage(int resId){
		this.setMessage(mContext.getResources().getString(resId));
	}
	
	public void setMessage(String message){
		mDialogMessage.setVisibility(View.VISIBLE);
		mMessageView.setVisibility(View.VISIBLE);
		mDialogMessage.setText(message);
	}
	
	public void setDelLinesMessage(SpannableString message){
		mDialogMessage.setVisibility(View.VISIBLE);
		mMessageView.setVisibility(View.VISIBLE);
		mDialogMessage.setText(message);
	}
	
	public void setBackground(Drawable drawable){
		mDialogBg.setBackgroundDrawable(drawable);
	}
	public void setBackground(int resId){
		this.setBackground(mContext.getResources().getDrawable(resId));
	}
	public void setSingleChoiceItems(String[] items,int which,OnClickListener listener){
		mOptionBt.setVisibility(View.VISIBLE);
		ListView listView=new ListView(mContext);
		SingleAdapter adapter=new SingleAdapter(mContext,items,which);
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setDivider(mContext.getResources().getDrawable(R.drawable.line_horizontal_menu));
		listView.setAdapter(adapter);
		listView.setFooterDividersEnabled(true);
		listView.setOnItemClickListener(this);
		mSingleOnClickListener=listener;
		addSelfView(listView);
	}
	public void addSelfView(View selfView){
		android.widget.LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams((int)(d.getWidth()*0.7), LinearLayout.LayoutParams.WRAP_CONTENT);
		this.addSelfView(selfView, layoutParams);
	}
	public void addSelfView(View selfView,android.widget.LinearLayout.LayoutParams layoutParams){
		mContentView.addView(selfView,layoutParams);
	}
	public void addSelfView(int selfViewResId){
		View selfView=LayoutInflater.from(mContext).inflate(selfViewResId, null, false);
		this.addSelfView(selfView);
	}
	/**
	 * 设置按钮的背景和文字颜色
	 * @param resId
	 * @param which
	 * @param textcolorId
	 */
	public void setButtonBg(int resId,int which,int textcolor){
		switch (which) {
		case JDOPTONDIALOG_POS_BUTTON:
			mPosBt.setBackgroundResource(resId);
			mPosBt.setTextColor(mContext.getResources().getColor(textcolor));
			break;
		case JDOPTONDIALOG_MID_BUTTON:
			mMidBt.setBackgroundResource(resId);
			mMidBt.setTextColor(mContext.getResources().getColor(textcolor));
			break;
		case JDOPTONDIALOG_NEG_BUTTON:
			mNegBt.setBackgroundResource(resId);
			mNegBt.setTextColor(mContext.getResources().getColor(textcolor));
			break;
		}
	}
	
	public void setPosDismissBySelf(boolean mPosDismissBySelf){
		this.mPosDismissbySelf=mPosDismissBySelf;
	}
	public final Context getJdContext(){
		return mContext;
	}
	
	public void setTag(Object tag){
		 object=tag;
	}
	
	public Object getTag(){
		return object;
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialog_PosBt:
			if(mPosOnClickListener!=null){
				if(!mPosDismissbySelf){
					JdOptionDialog.this.dismiss();
				}
				mPosOnClickListener.onClick(JdOptionDialog.this, JDOPTONDIALOG_POS_BUTTON);
			}
			break;
		case R.id.dialog_MidBt:
			if(mMidOncClickListener!=null){
				mMidOncClickListener.onClick(JdOptionDialog.this, JDOPTONDIALOG_MID_BUTTON);
			}
			break;
		case R.id.dialog_NegBt:
			if(mNegOnClickListener!=null){
				mNegOnClickListener.onClick(JdOptionDialog.this, JDOPTONDIALOG_NEG_BUTTON);
			}
			break;
		default:
			break;
		}
		
		
	  
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		 if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
			 if(isCancelable){
				 if(mOnCancelListener!=null){
					 mOnCancelListener.onCancel(JdOptionDialog.this);
					 return true;
				 }
			 }else {
				return true;
			} 
		 }
		return super.onKeyUp(keyCode, event);
	}
	public static class Builder{
		private int mTitleResId=-1,mMessageResId=-1,mPostionResId=-1,mMiddleResId=-1,mNegativeResId=-1,mBackgroundId=-1;
		private String mTitleText,mMessageText,mPostionText,mMiddleText,mNegativeText;
		private SpannableString msp;
		private OnClickListener mPostionClickListener,mMiddleClickListener,mNegativeClickListener,mSingleClickListener;
		private Context mContext;
		private String[] items ;
		private int  mWhichChoice;
		private OnKeyListener mOnKeyListener;
		private OnCancelListener mOnCancelListener;
		private View mselfView;
		private int mSelfViewResId=-1;
		private Drawable mBackgroundDrawable;
		private android.widget.LinearLayout.LayoutParams mlLayoutParams;
		private boolean isCancelable=true;
		private int mButtonBg=-1,mWhichButton=-1,mButtonTextColor=-1;
		public  JdOptionDialog jdOptionDialog;
		public JdOptionDialog getJdOptionDialog() {
			return jdOptionDialog;
		}

		private boolean mPosDismissBySelf=false;
		public Builder(Context context){
			this.mContext=context;
		}
		public Builder setTitle(int resId){
			 this.mTitleResId=resId;
			 return this;
		}
		
		public Builder setTitle(String title){
			this.mTitleText=title;
			 return this;
		}
		public Builder setPositiveButton(int resId,OnClickListener listener){
 		        this.mPostionResId=resId;
 		        this.mPostionClickListener=listener;
 		       return this;
		}
		public Builder setMidlleButton(int resId,OnClickListener listener){
			this.mMiddleResId=resId;
	        this.mMiddleClickListener=listener;
	        return this;
		}
		public Builder setNegativeButton(int resId,OnClickListener listener){
	        this.mNegativeResId=resId;
	        this.mNegativeClickListener=listener;
	        return this;
		}
		public Builder setPositiveButton(String content,OnClickListener listener){
			this.mPostionText=content;
		    this.mPostionClickListener=listener;
		    return this;
		}
		public Builder setMidlleButton(String content,OnClickListener listener){
			this.mMiddleText=content;
		    this.mMiddleClickListener=listener;
		    return this;
		}
		public Builder setNegativeButton(String content,OnClickListener listener){
			this.mNegativeText=content;
		    this.mNegativeClickListener=listener;
		    return this;
		}
		public Builder setOnKeyListener(OnKeyListener onKeyListener){
			this.mOnKeyListener=onKeyListener;
			 return this;
		}
		public Builder setButtonBg(int resId,int which,int textcolorId){
			this.mButtonBg=resId;
			this.mWhichButton=which;
			this.mButtonTextColor=textcolorId;
			return this;
		}
		public Builder setOnCancelListener(OnCancelListener onCancelListener){
			this.mOnCancelListener=onCancelListener;
			return this;
		}
		public Builder setMessage(int resId){
			this.mMessageResId=resId;
			return this;
		}
		
		public Builder setMessage(String message){
			 this.mMessageText=message;
			 return this;
		}
		public Builder setDelLinesMessage(SpannableString msp){
			 this.msp=msp;
			 return this;
		}
		
		public Builder setCancelable(boolean isCancelable){
			this.isCancelable=isCancelable;
			return this;
		}
		public Builder addSelfView(View selfView){
			this.mselfView=selfView;
			return this;
		}
		public Builder addSelfView(View selfView,android.widget.LinearLayout.LayoutParams layoutParams){
			this.mselfView=selfView;
			this.mlLayoutParams=layoutParams;
			return this;
		}
		public Builder addSelfView(int selfViewResId){
			this.mSelfViewResId=selfViewResId;
			return this;
		}
		/**
		 * 设置Pos按钮响应后，对话框消失是使用默认消失的，还是由外界自己控制。
		 * @param mPosDismissBySelf
		 * @return
		 */
		public Builder setPosDismissBySelf(boolean mPosDismissBySelf){
			this.mPosDismissBySelf=mPosDismissBySelf;
			return this;
		}
		
		public Builder setBackground(int resId){
			this.mBackgroundId=resId;
			return this;
 		}
		public Builder setBackground(Drawable mBackgroundDrawable){
			this.mBackgroundDrawable=mBackgroundDrawable;
			return this;
		}
		
		public Builder setSingleChoiceItems(String[] items,int which,OnClickListener listener){
			this.items=items;
			this.mWhichChoice=which;
			this.mSingleClickListener=listener;
			return this;
		}
		public JdOptionDialog create(){
			JdOptionDialog optionDialog=new JdOptionDialog(mContext);
			if(this.mTitleResId>0){
			    this.mTitleText=mContext.getString(mTitleResId);
			}
			if(this.mPostionResId>0){
				this.mPostionText=mContext.getString(mPostionResId);
			}
			if(this.mMiddleResId>0){
				this.mMiddleText=mContext.getString(mMiddleResId);
			}
			if(this.mNegativeResId>0){
				this.mNegativeText=mContext.getString(mNegativeResId);
			}
			if(mMessageResId>0){
				this.mMessageText=mContext.getString(mMessageResId);
			}
			if(!TextUtils.isEmpty(this.mTitleText)){
				optionDialog.setHeaderTitle(mTitleText);
			}
			if(!TextUtils.isEmpty(this.mPostionText)){
				optionDialog.setPositiveButton(mPostionText,mPostionClickListener);
			}
			if(!TextUtils.isEmpty(this.mMiddleText)){
				optionDialog.setMidlleButton(mMiddleText, mMiddleClickListener);
			}
			if(!TextUtils.isEmpty(this.mNegativeText)){
				optionDialog.setNegativeButton(mNegativeText, mNegativeClickListener);
			}
			if(!TextUtils.isEmpty(this.mMessageText)){
				optionDialog.setMessage(mMessageText);
			}
			if(msp!=null){
				optionDialog.setDelLinesMessage(msp);
			}
			if(mOnKeyListener!=null){
				optionDialog.setOnKeyListener(mOnKeyListener);
			}
			if(mOnCancelListener!=null){
				optionDialog.setOnCancelListener(mOnCancelListener);
			}
			if(mselfView!=null){
				if(mlLayoutParams!=null){
					optionDialog.addSelfView(mselfView, mlLayoutParams);
				}else {
					optionDialog.addSelfView(mselfView);
				}
			}
			if(mSelfViewResId>0){
				optionDialog.addSelfView(mSelfViewResId);
			}
			if(mBackgroundDrawable!=null){
				optionDialog.setBackground(mBackgroundDrawable);
			}
			if(mBackgroundId>0){
				optionDialog.setBackground(mBackgroundId);
			}
			if(mButtonBg!=-1 && mButtonTextColor!=-1 && mWhichButton!=-1){
				optionDialog.setButtonBg(mButtonBg, mWhichButton, mButtonTextColor);
			}
			if(items!=null && items.length>0){
				optionDialog.setSingleChoiceItems(items, mWhichChoice, mSingleClickListener);
			}
			optionDialog.setCancelable(this.isCancelable);
			optionDialog.setPosDismissBySelf(mPosDismissBySelf);
			this.jdOptionDialog=optionDialog;
			return optionDialog;
		}
		
		public JdOptionDialog show(){
			if(jdOptionDialog==null){
				jdOptionDialog=this.create();
			} 
			jdOptionDialog.show();
			return jdOptionDialog;
		}
		
		public final Context getContext(){
			return mContext;
		}
	}
  
	public interface OnClickListener{
		public  abstract void onClick(JdOptionDialog dialog,int which);
	}
	
	public interface OnCancelListener{
		public  abstract void onCancel(JdOptionDialog dialog);
	}
	
	public  static class SingleAdapter extends BaseAdapter{
		
		private Context mContext;
		private String[] items;
		private int mChoice;
		public SingleAdapter(Context mContext, String[] items,int which) {
			this.items = items;
			this.mContext = mContext;
			this.mChoice=which;
		}
		
		@Override
		public int getCount() {
			return items.length;
		}

		@Override
		public Object getItem(int arg0) {
			return items[arg0];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
        public void setChoice(int which){
        	this.mChoice=which;
        }
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.list_item_single_choice, null);
			}
			if(position==mChoice){
				((RadioButton)convertView.findViewById(R.id.radioBt)).setChecked(true);
			}else {
				((RadioButton)convertView.findViewById(R.id.radioBt)).setChecked(false);
			}
			TextView text = (TextView) convertView.findViewById(R.id.single_choice_text);
			text.setText(items[position]);
			return convertView;  
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int pos, long arg3) {
		if(adapterView.getAdapter() instanceof SingleAdapter){
			  ((SingleAdapter)adapterView.getAdapter()).setChoice(pos);
			  ((SingleAdapter)adapterView.getAdapter()).notifyDataSetChanged();
		  }
		if(mSingleOnClickListener!=null){
			  mSingleOnClickListener.onClick(this, pos);
		  }
	}
}
