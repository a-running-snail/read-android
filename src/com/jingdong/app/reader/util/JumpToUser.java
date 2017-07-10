package com.jingdong.app.reader.util;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.user.UserInfo;

public class JumpToUser implements OnClickListener {
	private final String userId;
	private final Context context;
	private final String jd_user_name;

	public JumpToUser(Context context, UserInfo userInfo) {
		this.context = context;
		userId = userInfo.getId();
		jd_user_name = userInfo.getJd_user_name();
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(context, UserActivity.class);
		intent.putExtra(UserFragment.USER_ID, userId);
		intent.putExtra(UserActivity.JD_USER_NAME, jd_user_name);
		context.startActivity(intent);
	}

}
