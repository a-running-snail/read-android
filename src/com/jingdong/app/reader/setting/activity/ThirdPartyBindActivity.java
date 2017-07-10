package com.jingdong.app.reader.setting.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.me.model.SocialModel;
import com.jingdong.app.reader.me.model.UserDetail;
import com.jingdong.app.reader.setting.model.ThirdPartyModel;
import com.jingdong.app.reader.setting.model.ThirdPartyModel.SocialType;
import com.jingdong.app.reader.R;

public class ThirdPartyBindActivity extends MZReadCommonActivity {

	private class UserGetter extends AsyncTask<Void, Void, UserDetail> {

		@Override
		protected UserDetail doInBackground(Void... params) {
			UserDetail userDetail = new UserDetail();
			userDetail.parseJson(ThirdPartyBindActivity.this);
			return userDetail;
		}

		@Override
		protected void onPostExecute(UserDetail result) {
			SocialModel sinaSocialModel = result.getSinalModel();
			setSwithcStatus(sinaSocialModel, sinaSwitch);
			long socialId;
			if(sinaSocialModel!=null)
				socialId=sinaSocialModel.getId();
			else
				socialId=-1;
			sinaModel = new ThirdPartyModel(socialId, ThirdPartyBindActivity.this, SocialType.Sina,
					sinaSwitch);
			sinaSwitch.setOnCheckedChangeListener(ThirdPartyBindActivity.this.sinaModel);
		}

		private void setSwithcStatus(SocialModel socialModel, CheckBox socialSwitch) {
			socialSwitch.setClickable(true);
			if (socialModel != null)
				socialSwitch.setChecked(true);
			else
				socialSwitch.setChecked(false);
		}
	}

	private CheckBox sinaSwitch;
	private ThirdPartyModel sinaModel;
	private UserGetter userGetter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bind);
		sinaSwitch = (CheckBox) findViewById(R.id.sinaSwitch);
		sinaSwitch.setClickable(false);
		findViewById(R.id.sina_weibo).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				sinaSwitch.setChecked(!sinaSwitch.isChecked());
			}
		});
		userGetter = new UserGetter();
		userGetter.execute();
	}

	@Override
	protected void onDestroy() {
		userGetter.cancel(false);
		if (sinaModel != null)
			sinaModel.stopTask();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (sinaModel != null) {
			if (!sinaModel.onActivityResult(requestCode, resultCode, data)) {
				super.onActivityResult(requestCode, resultCode, data);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
