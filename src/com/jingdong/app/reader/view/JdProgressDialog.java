package com.jingdong.app.reader.view;


import com.jingdong.app.reader.R;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class JdProgressDialog extends Dialog{
	
	private TextView mTitle ,mDialogMessage;
	private View mTitleView,mDialogBg;
	private ImageView mIndeterminateProgress;
	private Object object;
	private Context mContext;
	private LinearLayout mContentView;
	private View mMessageView;
	private boolean isCancelable=true;
	private boolean mselfbg=false;
	private OnCancelListener mOnCancelListener;
	public static final int JDOPTONDIALOG_POS_BUTTON=0;
	public static final int JDOPTONDIALOG_MID_BUTTON=1;
	public static final int JDOPTONDIALOG_NEG_BUTTON=2;
	public JdProgressDialog(Context context) {
		super(context, R.style.context_menu_dialog);
		setContentView(R.layout.item_progress_dialog);
		this.mContext=context;
		setCanceledOnTouchOutside(false);
 		mTitleView=findViewById(R.id.layout_title);
 		mTitle=(TextView)findViewById(R.id.dialog_title);
 		mIndeterminateProgress=(ImageView)findViewById(R.id.indeterminateSytle);
 		mDialogMessage=(TextView)findViewById(R.id.dialog_message);
 		mContentView=(LinearLayout)findViewById(R.id.addView);
 		mDialogBg=findViewById(R.id.context_dialog);
 		mMessageView=findViewById(R.id.messageLayout);
 	}
	
	public void setHeaderTitle(int resId){
		this.setHeaderTitle(mContext.getString(resId));
	}
	
	public void setHeaderTitle(String title){
		mTitleView.setVisibility(View.VISIBLE);
		mTitle.setText(title);
	}
	
	public void setIndeterminate(boolean isIndeterminate){
		if(isIndeterminate){
			if(!mselfbg){
				mDialogBg.setBackgroundResource(R.drawable.bg_indeterminate_progress);
			}
			mMessageView.setVisibility(View.VISIBLE);
			mIndeterminateProgress.setVisibility(View.VISIBLE);
			Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.indeterminate_progress_style);  
			LinearInterpolator lin = new LinearInterpolator();  
			animation.setInterpolator(lin);
			mIndeterminateProgress.startAnimation(animation);
		}else{
			mMessageView.setVisibility(View.GONE);
			mIndeterminateProgress.setVisibility(View.GONE);
		}
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
	public void setBackground(Drawable drawable){
		mselfbg=true;
		mDialogBg.setBackgroundDrawable(drawable);
	}
	public void setBackground(int resId){
		this.setBackground(mContext.getResources().getDrawable(resId));
	}
	public void addSelfView(View selfView){
		android.widget.LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		this.addSelfView(selfView, layoutParams);
	}
	public void addSelfView(View selfView,android.widget.LinearLayout.LayoutParams layoutParams){
		mContentView.addView(selfView,layoutParams);
	}
	public void addSelfView(int selfViewResId){
		View selfView=LayoutInflater.from(mContext).inflate(selfViewResId, null, false);
		this.addSelfView(selfView);
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
	 
	public static JdProgressDialog show(Context context,CharSequence title,CharSequence message,boolean isIndeterminate){
		JdProgressDialog progressDialog=new JdProgressDialog(context);
		if(!TextUtils.isEmpty(title))
			progressDialog.setHeaderTitle(title.toString());
		if(!TextUtils.isEmpty(message))
			progressDialog.setMessage(message.toString());
		progressDialog.setIndeterminate(isIndeterminate);
		return progressDialog;
	}
	  
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		 if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
			 if(isCancelable){
				 if(mOnCancelListener!=null){
					 mOnCancelListener.onCancel(JdProgressDialog.this);
					 return true;
				 }
			 }else {
				return true;
			} 
		 }
		return super.onKeyUp(keyCode, event);
	}
	public static class Builder{
		private int mTitleResId=-1,mMessageResId=-1,mBackgroundId=-1;
		private String mTitleText,mMessageText ;
		private Context mContext;
		private OnKeyListener mOnKeyListener;
		private OnCancelListener mOnCancelListener;
		private View mselfView;
		private int mSelfViewResId=-1;
		private Drawable mBackgroundDrawable;
		private android.widget.LinearLayout.LayoutParams mlLayoutParams;
		private boolean isCancelable=true;
		private boolean isIndeterminate=false;
		private JdProgressDialog jdOptionDialog;
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
		public Builder setOnKeyListener(OnKeyListener onKeyListener){
			this.mOnKeyListener=onKeyListener;
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
		
		public Builder setIndeterminate(boolean isIndeterminate){
			this.isIndeterminate=isIndeterminate;
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
		
		public JdProgressDialog create(){
			JdProgressDialog optionDialog=new JdProgressDialog(mContext);
			if(this.mTitleResId>0){
			    this.mTitleText=mContext.getString(mTitleResId);
			}
			if(mMessageResId>0){
				this.mMessageText=mContext.getString(mMessageResId);
			}
			if(!TextUtils.isEmpty(this.mTitleText)){
				optionDialog.setHeaderTitle(mTitleText);
			}
			if(!TextUtils.isEmpty(this.mMessageText)){
				optionDialog.setMessage(mMessageText);
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
			if(isIndeterminate){
				optionDialog.setIndeterminate(isIndeterminate);
			}
			optionDialog.setCancelable(this.isCancelable);
			this.jdOptionDialog=optionDialog;
			return optionDialog;
		}
		
		public JdProgressDialog show(){
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
		public  abstract void onClick(JdProgressDialog dialog,int which);
	}
	
	public interface OnCancelListener{
		public  abstract void onCancel(JdProgressDialog dialog);
	}
	
	 
}
