package com.jingdong.app.reader.util;

import com.jingdong.app.reader.application.MZBookApplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

public class NetUtils {

	private static final int NO_NET = 2147483647;
	private static final int UNKNOWN = 2147483646;
	private static final int WIFI = 2147483645;
	//private static final int ROAMING = 2147483644;

	public static int getType() {

		int result = NO_NET;

		try {

			ConnectivityManager cm = (ConnectivityManager) MZBookApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();

			if (netInfo != null && netInfo.isConnected()) {
				// if (netInfo.isRoaming()) {
				// // 漫游
				// }
				if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
					if (Log.D) {
						Log.d("Temp", "netInfo.getType() == ConnectivityManager.TYPE_MOBILE -->> ");
					}
					TelephonyManager tm = (TelephonyManager) MZBookApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
					result = tm.getNetworkType();
				} else if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					if (Log.D) {
						Log.d("Temp", "netInfo.getType() == ConnectivityManager.TYPE_WIFI -->> ");
					}
					result = WIFI;
				} else {
					if (Log.D) {
						Log.d("Temp", "netInfo.getType() == ConnectivityManager.UNKNOWN -->> ");
					}
					result = UNKNOWN;
				}
			} else {
				if (Log.D) {
					Log.d("Temp", "netInfo.getType() == ConnectivityManager.NO_NET -->> ");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (Log.D) {
			Log.d("Temp", "getType() result -->> " + result);
		}
		if (Log.D) {
			Log.d("Temp", "getType() result toTypeName() -->> " + toTypeName(result));
		}
		return result;
	}
	/**判断网络是否可用
	 * @param arg0
	 */
	public static void netAvailable(Context arg0) {
		boolean success = false; 
		//获得网络连接服务 
		ConnectivityManager connManager = (ConnectivityManager)arg0. getSystemService(Context.CONNECTIVITY_SERVICE); 
		// State state = connManager.getActiveNetworkInfo().getState(); 
		NetworkInfo network=connManager.getActiveNetworkInfo();
		if(network==null||!network.isConnectedOrConnecting()){
			Toast.makeText(arg0, "您的网络连接已中断", Toast.LENGTH_LONG).show(); 
			return;
		}
		
//		} else{
//			Toast.makeText(arg0, "您的网络连接已经成功", Toast.LENGTH_LONG).show(); 
//		}
	}
	public static String toTypeName(int code) {
		switch (code) {
		case WIFI:
			return "WIFI";
		case NO_NET:
			return "NO_NET";
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "GPRS";
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "EDGE";
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "UMTS";
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "HSDPA";
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "HSUPA";
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "HSPA";
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "CDMA";
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "CDMA - EvDo rev. 0";
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "CDMA - EvDo rev. A";
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "CDMA - 1xRTT";
		default:
			return "UNKNOWN";
		}
	}
	
	public static String getHostAndPortByUrl(String url) {

		String hostAndPort = null;
		
		if(TextUtils.isEmpty(url)){
			
		}
		
	
		if (null != url) {
			int start = url.indexOf("://") + 3;
			int end = url.indexOf("/", start);
			if (start == -1) {
				return null;
			}
			if (end == -1) {
				return null;
			}
			hostAndPort = url.substring(start, end);
			return hostAndPort;
		}
		return null;

	}
	
	public static String getProxyHost() {
//		if (!NetworkHelper.isProxy()) {
//			return null;
//		}

//		if (getType() != TelephonyManager.NETWORK_TYPE_CDMA) {
//			return null;
//		}
		String defaultHost = android.net.Proxy.getDefaultHost();
		if (Log.D) {
			Log.d("Temp", "getProxyHost() -->> " + defaultHost);
		}
		return defaultHost;
	}
	


}
