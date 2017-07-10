package com.jingdong.app.reader.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.LinearLayout.LayoutParams;

import com.jingdong.app.reader.R;

public class BookShelfPopupWindowView extends PopupWindow {

	
	public interface OnBookcasePopupMenuClickListener{
		public void onBookcasePopupMenuClick(int id);
	}

		private View mMenuView;

		public BookShelfPopupWindowView(Activity context,
				final OnBookcasePopupMenuClickListener itemsOnClick) {
			super(context);
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mMenuView = inflater.inflate(
					R.layout.layout_bookcase_popupwindow, null);

			FrameLayout delete = (FrameLayout) mMenuView.findViewById(R.id.delete);
			FrameLayout undelete = (FrameLayout) mMenuView.findViewById(R.id.undelete);
			FrameLayout cancle = (FrameLayout) mMenuView.findViewById(R.id.cancle);

			delete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (itemsOnClick != null)
						itemsOnClick.onBookcasePopupMenuClick(101);
					dismiss();

				}
			});
			
			undelete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (itemsOnClick != null)
						itemsOnClick.onBookcasePopupMenuClick(102);
					dismiss();

				}
			});
			
			cancle.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (itemsOnClick != null)
						itemsOnClick.onBookcasePopupMenuClick(103);
					dismiss();

				}
			});

		
			this.setContentView(mMenuView);
			this.setWidth(LayoutParams.MATCH_PARENT);
			this.setHeight(LayoutParams.WRAP_CONTENT);
			this.setFocusable(true);
			this.setTouchable(true);
			this.setOutsideTouchable(true);
			this.setBackgroundDrawable(new ColorDrawable(context.getResources()
					.getColor(R.color.bg_menu_shadow)));

		}
		


	}
