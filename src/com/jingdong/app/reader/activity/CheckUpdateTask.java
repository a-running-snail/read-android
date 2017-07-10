package com.jingdong.app.reader.activity;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;

public class CheckUpdateTask extends AsyncTask<Void, Void, String> {

	private Context mContext;
	private ProgressDialog waiting;
	private boolean forceAlert = false;

	public CheckUpdateTask(Context context) {
		this(context, false, false);
	}
	
	public CheckUpdateTask(Context context, boolean forceAlert, boolean showWaiting) {
		this.mContext = context;
		this.forceAlert=forceAlert;
		if (showWaiting) {
			waiting = new ProgressDialog(context);
			waiting.setTitle(R.string.app_name);
			waiting.setMessage(context.getString(R.string.waiting));
			waiting.setCanceledOnTouchOutside(false);
			waiting.show();
		}
	}

	@Override
	protected String doInBackground(Void... params) {
		String result = "";
		if (NetWorkUtils.isNetworkConnected(mContext)) {
			result = WebRequest.requestWebData(URLText.checkVersionUrl, null,
					WebRequest.httpGet);
		}
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (waiting != null) {
			waiting.dismiss();
		}
		if (TextUtils.isEmpty(result))
			return;

		try {
			JSONObject obj = new JSONObject(result);
			String versionName = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0).versionName;
			int versionCode = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0).versionCode;
			MZLog.d("MZBookApplication", "version code: " + versionCode
					+ " version name: " + versionName);
			String serverVersionCodeText;
			final String downloadUrl;
			if (MZBookApplication.TestBuild) {
				serverVersionCodeText = obj.getString("test_version_code");
				downloadUrl = obj.getString("test_download_url");
			} else {
				serverVersionCodeText = obj.getString("version_code");
				downloadUrl = obj.getString("download_url");
			}
			String serverVersionName = obj.getString("version_name");
			String serverVersionDesc = obj.getString("version_desc");

			final String message = mContext.getString(R.string.current_version,
					versionName) + serverVersionDesc;
			final int serverVersionCode = Integer.parseInt(serverVersionCodeText);
			if (serverVersionCode > versionCode) {
				String title = mContext.getString(R.string.find_new_version,
						serverVersionName);
				
				if(forceAlert||serverVersionCode != LocalUserSetting.getCheckUpdateFlag(mContext))
				{
					AlertDialog dialog = new AlertDialog.Builder(mContext,
							AlertDialog.THEME_HOLO_LIGHT)
							.setIconAttribute(android.R.attr.alertDialogIcon)
							.setTitle(title)
							.setMessage(message)
							.setPositiveButton(R.string.download,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int whichButton) {
											Intent browserIntent = new Intent(
													Intent.ACTION_VIEW, Uri
															.parse(downloadUrl));
											mContext.startActivity(browserIntent);
											// mContext.finish();
											LocalUserSetting.saveCheckUpdateFlag(mContext, serverVersionCode);
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int whichButton) {
											LocalUserSetting.saveCheckUpdateFlag(
													mContext, serverVersionCode);
										}
									}).setCancelable(false).create();

					dialog.show();
				}

			} else {
				if (waiting != null) {
					Toast.makeText(mContext, R.string.no_upgrade, Toast.LENGTH_SHORT).show();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}