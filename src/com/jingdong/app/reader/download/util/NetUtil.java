package com.jingdong.app.reader.download.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.jingdong.app.reader.download.entity.DownloadConstants;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 *
 * @ClassName: NetUtil
 * @Description: 网络操作工具类
 * @author J.Beyond
 * @date 2015年8月8日 下午6:04:38
 *
 */
public class NetUtil {
	/**
	 * 根据url构建HTTP链接对象
	 *
	 * @param url
	 *            url路径
	 * @return HTTP链接对象
	 * @throws IOException
	 *             链接异常时抛出
	 */
	public static HttpURLConnection buildConnection(String url) throws IOException {
		return buildConnection(url, false);
	}

	/**
	 * 根据url构建HTTP链接对象
	 *
	 * @param url
	 *            url路径
	 * @param isAlive
	 *            是否保持长连接
	 * @return HTTP链接对象
	 * @throws IOException
	 *             链接异常时抛出
	 */
	public static HttpURLConnection buildConnection(String url, boolean isAlive) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(5000);
		connection.setRequestProperty("Accept","image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, application/vnd.android.package-archive, */*");
		connection.setRequestProperty("Accept-Ranges","bytes");
//		connection.setRequestProperty("Accept-Language", "zh-CN");
		connection.setRequestProperty("Charset", "UTF-8");
		connection.setRequestProperty("connection", "close");

		if (isAlive) {
			connection.setRequestProperty("Connection", "Keep-Alive");
		}
		return connection;
	}

	/**
	 * 获取网络类型
	 *
	 * @param context
	 *            ...
	 * @return 网络类型ID {@link DownloadConstants.NetType}
	 */
	public static int getNetWorkType(Context context) {
		int type = DownloadConstants.NetType.INVALID;
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			String typeName = networkInfo.getTypeName();
			if (typeName.equalsIgnoreCase("WIFI")) {
				type = DownloadConstants.NetType.WIFI;
			} else if (typeName.equalsIgnoreCase("MOBILE")) {
				String proxyHost = android.net.Proxy.getDefaultHost();
				type = TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(context) ? DownloadConstants.NetType.G3 : DownloadConstants.NetType.G2)
						: DownloadConstants.NetType.WAP;
			}
		}
		return type;
	}

	/**
	 * 判断是否是3G+的移动网络
	 *
	 * @param context
	 *            ...
	 * @return ...
	 */
	public static boolean isFastMobileNetwork(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		switch (telephonyManager.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return false;
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return false;
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return false;
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return true;
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return true;
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return false;
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return true;
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return true;
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return true;
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return true;
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			return true;
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			return true;
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return true;
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return false;
		case TelephonyManager.NETWORK_TYPE_LTE:
			return true;
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return false;
		default:
			return false;
		}
	}
	
	
}
