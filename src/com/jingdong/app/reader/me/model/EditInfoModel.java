package com.jingdong.app.reader.me.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.jingdong.app.reader.me.activity.EditInfoActivity;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.MZLog;

public class EditInfoModel extends ObservableModel {
	private final static int NO_EXIST=-1;
	public final static String PROFILE="userProfile";
	public final static String NAME = "user[name]";
	public final static String SEX = "user[sex]";
	public final static String SUMMARY = "user[summary]";
	public final static String CONTECT_EMAIL = "user[contact_email]";
	public final static String AVATAR = "user[avatar]";
	public final static String BOUNDARY = UUID.randomUUID().toString();
	private final static String DELIMETER = "--";
	private final static String LINE_END = "\r\n";
	private final static String QUOTE = "\"";
	private Context context;

	public EditInfoModel(Context context) {
		this.context = context;
	}

	public void upload(String name, String summary, String contactEmail, int sex, byte[] avatar) {
		String url = initUrl();
		boolean success=false;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			addFormParts(os, name, summary, contactEmail, sex);
			if (avatar != null)
				addFilePart(os, AVATAR, avatar);
			addEndPart(os);
		} catch (Exception e) {
			MZLog.e("EditInfoModel", Log.getStackTraceString(e));
		}
		byte[] postData = os.toByteArray();
		String result="";
		if (postData != null && postData.length != 0) {
			result = WebRequest.postBytesWithContext(context, url, postData);
			success=isSuccess(result);
			MZLog.d("editInfo", url);
			MZLog.d("editInfo", os.toString());
			MZLog.d("editInfo", result);
		}
		String profile=getBasicProfie(context);
		Bundle bundle=new Bundle();
		bundle.putString(PROFILE, profile);
		notifyDataChanged(EditInfoActivity.SUBMIT, success,true,result,bundle);
	}
	
	public static String getBasicProfie(Context context){
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		String url=URLBuilder.addParameter(URLText.settingUrl, paramMap);
		String jsonString=WebRequest.getWebDataWithContext(context, url);
		return jsonString;
	}
	
	private String initUrl() {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		String url = URLBuilder.addParameter(URLText.settingUrl, paramMap);
		// url="http://193.168.0.140:3000/settings.json?auth_token=kL4vPtaL5K3xVEdggg8Q";
		return url;
	}

	private void addFormParts(ByteArrayOutputStream os, String name, String summary, String contactEmail, int sex)
			throws Exception {
		if (name != null) {
			addFormPart(os, NAME, name);
		}
		if (summary != null) {
			addFormPart(os, SUMMARY, summary);
		}
		if (contactEmail != null) {
			addFormPart(os, CONTECT_EMAIL, contactEmail);
		}
		if (sex != -1) {
			addFormPart(os, SEX, Integer.toString(sex));
		}
	}

	private void addFormPart(ByteArrayOutputStream os, String paramName, String value) throws Exception {
		os.write(DELIMETER.getBytes());
		os.write(BOUNDARY.getBytes());
		os.write(LINE_END.getBytes());
		os.write("Content-Type: text/plain".getBytes());
		os.write(LINE_END.getBytes());
		os.write("Content-Disposition: form-data; name=".getBytes());
		os.write(QUOTE.getBytes());
		os.write(paramName.getBytes());
		os.write(QUOTE.getBytes());
		os.write(LINE_END.getBytes());
		os.write(LINE_END.getBytes());
		os.write(value.getBytes());
		os.write(LINE_END.getBytes());
	}

	private void addFilePart(ByteArrayOutputStream os, String paramName, byte[] data) throws Exception {
		os.write(DELIMETER.getBytes());
		os.write(BOUNDARY.getBytes());
		os.write(LINE_END.getBytes());
		os.write("Content-Disposition: form-data; filename=\"hi.jpg\"; name=".getBytes());
		os.write(QUOTE.getBytes());
		os.write(paramName.getBytes());
		os.write(QUOTE.getBytes());
		os.write(LINE_END.getBytes());
		os.write("Content-Type: application/octet-stream".getBytes());
		os.write(LINE_END.getBytes());
		os.write("Content-Transfer-Encoding: binary".getBytes());
		os.write(LINE_END.getBytes());
		os.write(LINE_END.getBytes());
		os.write(data);
		os.write(LINE_END.getBytes());
	}

	private void addEndPart(ByteArrayOutputStream os) throws IOException {
		os.write(DELIMETER.getBytes());
		os.write(BOUNDARY.getBytes());
		os.write(DELIMETER.getBytes());
		os.write(LINE_END.getBytes());
	}

	private boolean isSuccess(String jsonString){
		boolean success=false;
		try {
			JSONObject jsonObject=new JSONObject(jsonString);
			success=jsonObject.optString(UserInfo.ID).equals(LoginUser.getpin())?true:false;
		} catch (JSONException e) {
			MZLog.e("editInfo", Log.getStackTraceString(e));
		}
		return success;
	}
}
