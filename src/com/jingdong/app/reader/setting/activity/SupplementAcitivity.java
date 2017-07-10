package com.jingdong.app.reader.setting.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.me.model.EditInfoModel;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.request.Error;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.R;

public class SupplementAcitivity extends MZReadCommonActivity {
	
	private class Upload extends AsyncTask<String, Void, String>{
		private final static String PASSWORD="user[password]";
		private final static String NAME="user[name]";
		private final static String EMAIL="user[email]";
		private ProgressDialog dialog;
		private String jsonString;
		@Override
		protected void onPreExecute() {
			Resources resources = getResources();
			String title = resources.getString(R.string.update);
			String message = resources.getString(R.string.updating);
			dialog=ProgressDialog.show(SupplementAcitivity.this, title, message, true, false);
		}
		
		@Override
		protected String doInBackground(String... params) {
			String result=null;
			if(!isCancelled()){
				String url=getUrl();
				String body=getPostBody(params);
				result=WebRequest.postWebDataWithContext(SupplementAcitivity.this, url, body);
				jsonString=EditInfoModel.getBasicProfie(SupplementAcitivity.this);
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(dialog!=null){
				dialog.dismiss();
			}
			if(result!=null){
				if(ObservableModel.parsePostResult(result)){
					LocalUserSetting.saveTokenAndUserInfo(SupplementAcitivity.this, LocalUserSetting.getToken(SupplementAcitivity.this), jsonString);
					setResult(RESULT_OK);
					finish();
				}
				else{
					processError(result);
				}
			}
		}
		
		private String getUrl(){
			Map<String, String> paramMap=new HashMap<String, String>();
			paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(SupplementAcitivity.this));
			String url=URLBuilder.addParameter(URLText.updateRegister, paramMap);
			return url;
		}
		
		private String getPostBody(String... params){
			Map<String, String> paramMap=new HashMap<String, String>();
			paramMap.put(EMAIL, params[0]);
			paramMap.put(PASSWORD, params[1]);
			paramMap.put(NAME, params[2]);
			String body=URLBuilder.getPostTextFromMap(paramMap);
			return body;
		}
		
		private void processError(String result) {
			String msg = null;
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
				msg = jsonObject.optString(Error.ERROR);
			} catch (JSONException e) {
				MZLog.e("Bind", Log.getStackTraceString(e));
			}
			if (!TextUtils.isEmpty(msg))
				Toast.makeText(SupplementAcitivity.this, msg, Toast.LENGTH_SHORT).show();
		}
	}
	
	private final static int EMAIL_MIN=5;
	private final static int EMIAL_MAX=50;
	private final static int PWS_MIN=6;
	private final static int PWS_MAX=128;
	private final static int NAME_MAX=20;
	private EditText emailEditText;
	private EditText passwordEditText;
	private EditText nickNameEditText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_supplement);
		emailEditText=(EditText) findViewById(R.id.edit_email);
		passwordEditText=(EditText) findViewById(R.id.edit_pwd);
		nickNameEditText=(EditText)findViewById(R.id.edit_name);
		nickNameEditText.setText(LocalUserSetting.getUserName(this));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater=new MenuInflater(this);
		menuInflater.inflate(R.menu.finish, menu);
		MenuItem Item = menu.findItem(R.id.finish);
		View actionView = Item.getActionView();
		TextView view = (TextView) actionView.findViewById(R.id.finish_action);
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				upload();
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
	
	private void upload(){
		String email=emailEditText.getText().toString();
		String password=passwordEditText.getText().toString();
		String nickName=nickNameEditText.getText().toString();
		if(isEmailLegal(email)&&isPswLegal(password)&&isNameLegal(nickName)){
			Upload upload=new Upload();
			upload.execute(email,password,nickName);
		}
	}
	
	private boolean isEmailLegal(String email){
		boolean result;
		if(TextUtils.isEmpty(email)){
			Toast.makeText(this, R.string.emailEmpty, Toast.LENGTH_LONG).show();
			result=false;
		}
		else{
			if(Patterns.EMAIL_ADDRESS.matcher(email).matches()){
				if(email.length()<EMAIL_MIN&&email.length()>EMIAL_MAX){
					Toast.makeText(this, R.string.emailLength, Toast.LENGTH_LONG).show();
					result=false;
				}
				else{
					result=true;
				}
			}
			else{
				Toast.makeText(this, R.string.emailIllegal, Toast.LENGTH_LONG).show();
				result=false;
			}
		}
		return result;
	}
	
	private boolean isPswLegal(String password){
		boolean result;
		if(TextUtils.isEmpty(password)){
			Toast.makeText(this, R.string.pswEmpty, Toast.LENGTH_LONG).show();
			result=false;
		}
		else{
			if(password.length()<PWS_MIN&&password.length()>PWS_MAX){
				Toast.makeText(this, R.string.pswLength, Toast.LENGTH_LONG).show();
				result=false;
			}
			else{
				result=true;
			}
		}
		return result;
	}
	
	private boolean isNameLegal(String name){
		boolean result;
		if(TextUtils.isEmpty(name)){
			Toast.makeText(this, R.string.nameEmpty, Toast.LENGTH_LONG).show();
			result=false;
		}
		else{
			if(name.length()>NAME_MAX){
				Toast.makeText(this, R.string.nameLength, Toast.LENGTH_LONG).show();
				result=false;
			}
			else{
				result=true;
			}
		}
		return result;
	}
	
}
