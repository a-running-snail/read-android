package com.jingdong.app.reader.util;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Proxy;
import android.text.TextUtils;

public class NetWorkUtils {

	public enum NetworkConnectType {
		MOBILE, WIFI
	}


	/**
	 * 判断是否有网络连接
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 判断wifi是否可用
	 * @param context
	 * @return
	 */
	public static boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public static boolean isNetworkAvailable(Context context) {

		boolean result = false;
		if(context==null){
			return false;
		}
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			final NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
			if (networkInfos != null) {
				final int length = networkInfos.length;
				for (int i = 0; i < length; i++) {
					if (networkInfos[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return result;
	}

	public static NetworkConnectType getNetworkConnectType(Context context) {
		NetworkConnectType networkConnectType = null;

		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// mobile
		State mobile = NetworkInfo.State.DISCONNECTED;
		if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null)
			mobile = connectivityManager.getNetworkInfo(
					ConnectivityManager.TYPE_MOBILE).getState();

		// wifi
		State wifi = NetworkInfo.State.DISCONNECTED;
		if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null)
			wifi = connectivityManager.getNetworkInfo(
					ConnectivityManager.TYPE_WIFI).getState();
		if (mobile == NetworkInfo.State.CONNECTED
				|| mobile == NetworkInfo.State.CONNECTING) {
			// mobile
			networkConnectType = NetworkConnectType.MOBILE;
		} else if (wifi == NetworkInfo.State.CONNECTED
				|| wifi == NetworkInfo.State.CONNECTING) {
			// wifi
			networkConnectType = NetworkConnectType.WIFI;
		}
		return networkConnectType;
	}

	public static boolean isUsingWap(Context context) {
		boolean result = false;
		if (isNetworkAvailable(context)
				&& getNetworkConnectType(context) == NetworkConnectType.MOBILE) {
			final ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo networkInfo = connectivityManager
					.getActiveNetworkInfo();
			if (networkInfo != null) {
				final String netExtraInfo = networkInfo.getExtraInfo();
				if (!TextUtils.isEmpty(netExtraInfo)
						&& netExtraInfo.toLowerCase().contains("wap")) {
					result = true;
				}
			}
		}
		return result;
	}

	public static void setupNetwork(Context context, HttpClient httpClient) {
		if (isUsingWap(context)) {

			final String host = Proxy.getDefaultHost();
			final int port = Proxy.getDefaultPort();

			if (!TextUtils.isEmpty(host) && port != -1) {
				final HttpHost proxy = new HttpHost(host, port);
				httpClient.getParams().setParameter(
						ConnRoutePNames.DEFAULT_PROXY, proxy);
			}
		}
	}
}
