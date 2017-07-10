package com.jingdong.app.reader.request;

import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.util.MZLog;

import android.util.Log;

public class Success {
	private static final String SUCCESS = "success";
	private static final String DOWNLOAD_LINKD = "download_link";
	private boolean success;
	private String downloadLink;

	private Success() {

	}

	public static Success fromJson(String jsonString) {
		Success success = new Success();
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			success.success = jsonObject.getBoolean(SUCCESS);
			success.downloadLink = jsonObject.optString(DOWNLOAD_LINKD);
		} catch (JSONException e) {
			success = null;
			MZLog.d("success", Log.getStackTraceString(e));
		}
		return success;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getDownloadLink() {
		return downloadLink;
	}
}
