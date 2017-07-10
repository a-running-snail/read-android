package com.jingdong.app.reader.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.Header;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.entity.ShowOrderEntity.bookList;
import com.jingdong.app.reader.entity.extra.Update;
import com.jingdong.app.reader.entity.extra.UpdateInfo;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;

/**
 * 全局初始化类（单例模式）
 */
public class UpdateUtil {

	public static UpdateInfo updateInfo;

	public static boolean isNewVerison = false;

	public interface CheckNewVersionListener {
		public void onCheckResult(boolean isNewVersionAvaiable);
	}

	/**
	 * checksofteWareUpdated 应用更新
	 * 
	 * @param bManual
	 *            true为用户手动点击检测版本更新，false表示程序后台自动检测版本更新
	 */
	public static void checksofteWareUpdated(final boolean bManual, final Context context, final CheckNewVersionListener versionListener) {

		MZLog.d("cj", "ddd=======>>>");
		try {
			// 非联网状态下 不检查更新
			if (!NetWorkUtils.isNetworkAvailable(context)) {
				return;
			}
			WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getUpdateParams(), new MyAsyncHttpResponseHandler(context) {

				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					if (versionListener != null) {
						versionListener.onCheckResult(false);
					}
				}

				@Override
				public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
					String result = new String(responseBody);
					MZLog.d("cj", "result========>>" + result);
					updateInfo = GsonUtils.fromJson(result, UpdateInfo.class);
					// dialog_loading.dismiss();
					boolean isGo91Mark = false;

					if (updateInfo != null && Integer.parseInt(updateInfo.getCode()) == 0) {
						if (updateInfo.isLatest()) {
							if (bManual) {
								Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show();
							}
							if (versionListener != null) {
								versionListener.onCheckResult(false);
							}
							isNewVerison = false;
							return;
						} else {
							Update updateResult = updateInfo.getResult();
							if(updateResult==null) return;
							
							boolean toUpgrade = furtherConfirmToUpgrade(updateResult.getVersion(),context);
							if (versionListener != null) {
								versionListener.onCheckResult(toUpgrade);
							}
							if (!toUpgrade) return;
							
							if (MZBookApplication.isXiaoLajiao) {//
								long firstme = LocalUserSetting.getFirstShowFirstShowSplashTime(context);
								// 50天
								double first = 50 * 24 * 60 * 60;
								Date firstData = new Date(firstme);
								Date today = new Date(System.currentTimeMillis());
								int day = 0;
								int tempDay = 3 * 31;
								try {
									day = daysBetween(firstData, today);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if (day < tempDay && !bManual) {
									return;
								}
							}

							String description = "";
							int strategy = updateInfo.getResult().getStrategy();
							String remoteVersion = updateInfo.getResult().getVersion();
							String downloadUrl = updateInfo.getResult().getUrl();
							isNewVerison = true;
							// 是否去91市场升级
							if (updateInfo.getResult().getNineOneUpGrade() == 0) {
								isGo91Mark = false;
							} else if (updateInfo.getResult().getNineOneUpGrade() == 1) {
								isGo91Mark = true;
							}
							description = updateInfo.getResult().getInfo();
							long size = Integer.parseInt(updateInfo.getResult().getSize());
							String finalDescription = description + "\r\n下载的安装包大小为：" + convertStorage(size * 1024);
							// 2. 尝试升级
							if (null != context) {
								if (strategy == 1) {
									boolean isShow = true;
									long cancle_time = SettingUtils.getInstance().getLong("cancle_time", 0);
									if(cancle_time > 0) {
										long curtime = System.currentTimeMillis();
										//升级提示取消三次后一周内不在提示
										if (Math.abs(curtime - cancle_time) > 7*24*60*60*1000) {
											SettingUtils.getInstance().putInt("cancle_number", 0);
											SettingUtils.getInstance().putLong("cancle_time", 0);
										}else {
											isShow = false;
										}
									}
									
									if(isShow)
										ApplicationUpgradeHelper.getInstance().tryUpgrade(context, remoteVersion, downloadUrl, finalDescription, isGo91Mark, null);
								} else if (strategy == 2) {
									SettingUtils.getInstance().putInt("cancle_number", 0);
									SettingUtils.getInstance().putLong("cancle_time", 0);
									ApplicationUpgradeHelper.getInstance().tryMustUpgrade(context, remoteVersion, downloadUrl, finalDescription, isGo91Mark,
											null);
								} else {
									Toast.makeText(context, "版本升级失败", Toast.LENGTH_SHORT).show();
								}
							}
						}
					} else if (updateInfo == null)
						Toast.makeText(context, context.getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();

				}

				
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 进入设置页面检查是否最新版
	 * 
	 */
	public static void checkUpdate(final boolean bManual, final Context context, final TextView textView) {

		try {
			// 非联网状态下 不检查更新
			if (!NetWorkUtils.isNetworkAvailable(context)) {
				return;
			}
			WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getUpdateParams(), new MyAsyncHttpResponseHandler(context) {

				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				}

				@Override
				public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
					String result = new String(responseBody);
					updateInfo = GsonUtils.fromJson(result, UpdateInfo.class);
					boolean isGo91Mark = false;
					if (updateInfo != null && Integer.parseInt(updateInfo.getCode()) == 0) {
						if (updateInfo.isLatest()) {
							if (textView != null) {
								textView.setText("当前已是最新版本");
							}
							return;
						} else {
							Update updateResult = updateInfo.getResult();
							if(updateResult==null) return;
							
							boolean toUpgrade = furtherConfirmToUpgrade(updateResult.getVersion(),context);
							if (!toUpgrade) {
								if (textView != null) {
									textView.setText("当前已是最新版本");
								}
								return;
							}
							if (textView != null) {
								textView.setText("有新版本更新");
							}
						}
					} 
				}

				
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @Title: checkUpgate
	 * @Description: 进一步确认本地版本号与远程版本号的大小
	 * @param @param remoteVersion
	 * @param @param context
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	private static boolean furtherConfirmToUpgrade(String remoteVersion,Context context) {
		if (TextUtils.isEmpty(remoteVersion)) return false;
		//获取当前版本号
		String currentVersion = getVersionName(context);
		if (TextUtils.isEmpty(currentVersion)) return false;
		
		//将版本号中的“.”移除
		currentVersion = currentVersion.replace(".", "");
		remoteVersion = remoteVersion.replace(".", "");
		
		//比较两个字符串长度的差值，将最短的字符串后面补0
		int delta = currentVersion.length() - remoteVersion.length();
		if (delta>0) {
			StringBuilder sb = new StringBuilder(remoteVersion);
			for (int i = 0; i < delta; i++) {
				sb.append("0");
			}
			remoteVersion = sb.toString();
		}else if (delta < 0) {
			StringBuilder sb = new StringBuilder(currentVersion);
			for (int i = 0; i < -delta; i++) {
				sb.append("0");
			}
			currentVersion = sb.toString();
		}
		
		int currentVersionInt = 0;
		int remoteVersionInt = 0;
		try{
			currentVersionInt = Integer.parseInt(currentVersion);
			remoteVersionInt = Integer.parseInt(remoteVersion);
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		if (currentVersionInt >= remoteVersionInt) {
			return false;
		}else{
			return true;
		}
	}

	public static int daysBetween(Date smdate, Date bdate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		smdate = sdf.parse(sdf.format(smdate));
		bdate = sdf.parse(sdf.format(bdate));
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	public static String convertStorage(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;

		if (size >= gb) {
			return String.format("%.1f GB", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
		} else
			return String.format("%d B", size);
	}
	
	private static String getVersionName(Context context) {
		// 获取packagemanager的实例
		PackageManager packageManager = context.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			String version = packInfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
