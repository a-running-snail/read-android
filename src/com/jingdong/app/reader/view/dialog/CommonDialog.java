package com.jingdong.app.reader.view.dialog;
 
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.util.MZLog;
 
/**
 * 
 * Create custom Dialog windows for your application
 * Custom dialogs rely on custom layouts wich allow you to 
 * create and use your own look & feel.
 * 
 * Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 * 
 * <a href="http://my.oschina.net/arthor" target="_blank" rel="nofollow">@author</a> antoine vianey
 *
 */
public class CommonDialog extends Dialog {
	private  static CommonDialog mDialog;
	private static boolean mIsToastDialog;
 
    public CommonDialog(Context context, int theme) {
        super(context, theme);
    }
 
    public CommonDialog(Context context) {
        super(context);
    }
 
    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
 
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
 
        private DialogInterface.OnClickListener 
                        positiveButtonClickListener,
                        negativeButtonClickListener;
 
        public Builder(Context context,boolean isToastDialog) {
            this.context = context;
            mIsToastDialog = isToastDialog;
        }
 
        /**
         * Set the Dialog message from String
         * @param title
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }
 
        /**
         * Set the Dialog message from resource
         * @param title
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }
 
        /**
         * Set the Dialog title from resource
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }
 
        /**
         * Set the Dialog title from String
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
 
        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }
 
        /**
         * Set the positive button resource and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }
 
        /**
         * Set the positive button text and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }
 
        /**
         * Set the negative button resource and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(int negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }
 
        /**
         * Set the negative button text and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }
 
        /**
         * Create the custom dialog
         */
        public CommonDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mDialog = new CommonDialog(context, 
            		R.style.common_dialog_style);
            mDialog.setCanceledOnTouchOutside(false);
//            mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            View layout = inflater.inflate(R.layout.common_dialog_with_title_button, null);
            mDialog.addContentView(layout, new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            if (title == null || TextUtils.isEmpty(title)) {
				((TextView) layout.findViewById(R.id.common_dialog_title_tv)).setVisibility(View.GONE);;

			}else{
				((TextView) layout.findViewById(R.id.common_dialog_title_tv)).setVisibility(View.VISIBLE);;
				((TextView) layout.findViewById(R.id.common_dialog_title_tv)).setText(title);
			}
            if (positiveButtonText == null && negativeButtonText == null) {
				layout.findViewById(R.id.common_dialog_operation_btn_ll).setVisibility(View.GONE);
				layout.findViewById(R.id.common_dialog_driver).setVisibility(View.GONE);
			}
            // set the confirm button
            if (positiveButtonText != null) {
                ((TextView) layout.findViewById(R.id.common_dialog_button1))
                        .setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((TextView) layout.findViewById(R.id.common_dialog_button1))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(
                                    		mDialog, 
                                            DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.common_dialog_button1).setVisibility(
                        View.GONE);
				layout.findViewById(R.id.common_dialog_driver).setVisibility(View.GONE);

            }
            // set the cancel button
            if (negativeButtonText != null) {
                ((TextView) layout.findViewById(R.id.common_dialog_button2))
                        .setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((TextView) layout.findViewById(R.id.common_dialog_button2))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(
                                    		mDialog, 
                                            DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.common_dialog_button2).setVisibility(
                        View.GONE);
				layout.findViewById(R.id.common_dialog_driver).setVisibility(View.GONE);

            }
            
            
            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(
                		R.id.common_dialog_content_tv)).setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content))
                        .removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content))
                        .addView(contentView, 
                                new LayoutParams(
                                        LayoutParams.WRAP_CONTENT, 
                                        LayoutParams.WRAP_CONTENT));
            }
            mDialog.setContentView(layout);
           
            return mDialog;
        }

    }
    
   
    
    /**
     * 倒计时，3s后Dialog自动消失
     */
    private static CountDownTimer timer = new CountDownTimer(3000, 1000) {
      	 
        @Override
        public void onTick(long millisUntilFinished) {
        	
        }
 
        @Override
        public void onFinish() {
        	try {
        		if (mDialog != null) {
        			if (mDialog.isShowing()) {
        				mDialog.dismiss();
        			}
				}
        		this.cancel();
			} catch (Exception e) {
				e.printStackTrace();
				MZLog.e("J", e.getMessage());
			}
        }
    };
    
    @Override
    public void show() {
    	// TODO Auto-generated method stub
    	super.show();
    	if (mIsToastDialog) {
         	timer.start();
		}
    }
    
    
    
    

 
}