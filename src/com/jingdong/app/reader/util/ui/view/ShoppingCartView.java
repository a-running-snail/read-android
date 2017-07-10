package com.jingdong.app.reader.util.ui.view;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.data.db.DBHelper;
import com.jingdong.app.reader.data.db.DataProvider;

/**
 * @author keshuangjie
 * @description 标题栏上的购物车
 */
public class ShoppingCartView extends FrameLayout {
	
	private TextView cartNumber;
	private Context context;
	
	public ShoppingCartView(Context context){
		super(context);
		this.context = context;
	}

	public ShoppingCartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}
	
	public void initCartNumber(){
		cartNumber = (TextView) this.findViewById(R.id.textview_carNum);
		context.getContentResolver().registerContentObserver(DataProvider.CONTENT_URI_BOOKCART,
				true, cob);
		setCarNum();
	}
	
	private ContentObserver cob = new ContentObserver(new Handler()) {
		@Override
		public boolean deliverSelfNotifications() {
			return super.deliverSelfNotifications();
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			setCarNum();
		}

	};
	
	private void setCarNum(){
		int count = DBHelper.getCartBookNum();
		cartNumber.setText(String.valueOf(count)+"+");
		if(count>0){
			cartNumber.setVisibility(View.VISIBLE);
		}else{
			cartNumber.setVisibility(View.GONE);
		}
	}
	
	public void unRegisterContentObserver(){
		context.getContentResolver().unregisterContentObserver(cob);
	}
}
