package com.jingdong.app.reader.me.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.timeline.model.core.RenderBody;
import com.jingdong.app.reader.user.UserInfo;

public class ItemClickCallback implements OnItemClickListener {
	private UserInfo userInfo;
	private Book book = null;

	public ItemClickCallback() {
	}

	public ItemClickCallback(UserInfo user, Book book) {
		this.userInfo = user;
		this.book = book;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getItemAtPosition(position) instanceof RenderBody) {
			RenderBody renderBody = (RenderBody) parent.getItemAtPosition(position);
			if (renderBody.isPrivate()) {
				Entity entity = new Entity();
				entity.setRenderBody(renderBody);
				entity.setBook(book);	
				entity.setUser(userInfo);
				Intent intent = new Intent(view.getContext(), TimelineTweetActivity.class);
				Bundle mBundle = new Bundle();  
		        mBundle.putParcelable(TimelineTweetActivity.ENTITY, entity); 
				intent.putExtras(mBundle);
				intent.putExtra("isPrivate", true);
				view.getContext().startActivity(intent);
			} else {
				if (!"null".equals(renderBody.getGuid()) && renderBody.getGuid() != null) {
					Intent intent = new Intent(view.getContext(), TimelineTweetActivity.class);
					intent.putExtra(TimelineTweetActivity.TWEET_GUID, renderBody.getGuid());
					view.getContext().startActivity(intent);
				}
			}
		}
	}

	public void setUser(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public void setBook(Book book) {
		this.book = book;
	}
}
