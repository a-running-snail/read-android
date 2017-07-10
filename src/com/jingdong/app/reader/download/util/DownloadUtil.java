package com.jingdong.app.reader.download.util;

import java.text.DecimalFormat;

import com.jingdong.app.reader.download.entity.DownloadFileInfo;
import com.jingdong.app.reader.download.entity.DownloadState;

/**
 *
 * @ClassName: DownloadUtil
 * @Description: 下载相关工具类
 * @author J.Beyond
 * @date 2015年8月7日 下午8:39:18
 *
 */
public class DownloadUtil {

	public static String getDownloadStateDesc(DownloadFileInfo fileInfo) {
		String str = null;
		switch (fileInfo.getDownloadState()) {
		case DownloadState.DOWNLOAD_NOTSTART:
			str = "下载";
			break;
		case DownloadState.DOWNLOAD_FAILURE:
			str = "失败";
			break;
		case DownloadState.DOWNLOAD_ING:
			str = fileInfo.getProgress() + "%";
			break;
		case DownloadState.DOWNLOAD_PAUSE:
			str = "暂停";
			break;
		case DownloadState.DOWNLOAD_START:
			str = "开始";
			break;
		case DownloadState.DOWNLOAD_SUCCESS:
			str = "完成";
			break;
		case DownloadState.DOWNLOAD_WAIT:
			str = "等待";
			break;
		default:
			break;
		}

		return str;
	}

	/**
	 * 
	 * @Title: formatSpeed
	 * @Description: 格式化下载进度 为 B/s,K/s,M/s,G/s,T/s
	 * @param @param speed
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String formatSpeed(double speed) {
		DecimalFormat format = new DecimalFormat("#,##0.#");
		if (speed < 1024) {
			return format.format(speed) + " B/s";
		}

		speed /= 1024;
		if (speed < 1024) {
			return format.format(speed) + " KB/s";
		}

		speed /= 1024;
		if (speed < 1024) {
			return format.format(speed) + " MB/s";
		}

		speed /= 1024;
		if (speed < 1024) {
			return format.format(speed) + " GB/s";
		}

		speed /= 1024;
		if (speed < 1024) {
			return format.format(speed) + " TB/s";
		}

		return format.format(speed) + "B/s";
	}

	/**
	 * 
	 * @Title: getThreadCount
	 * @Description: 根据下载文件大小设置下载线程个数
	 * @param @param fileLength
	 * @param @return
	 * @return int
	 * @throws
	 */
	public static int getThreadCount(long fileLength) {
		int threadCount = 0;
		if (fileLength < 1024 * 500) {// 500k以内 1个线程下载
			threadCount = 1;
		} else if (fileLength < 1024 * 1024) {// 1M以内 2个线程下载
			threadCount = 2;
		} else if (fileLength < 1024 * 1024 * 10) {// 10M以内 4个线程下载
			threadCount = 4;
		} else if (fileLength < 1024 * 1024 * 20) {// 20M以内 6个线程下载
			threadCount = 6;
		} else if (fileLength < 1024 * 1024 * 50) {// 50M以内 8个线程下载
			threadCount = 8;
		} else if (fileLength < 1024 * 1024 * 100) {// 100M以内 10个线程下载
			threadCount = 10;
		} else if (fileLength < 1024 * 1024 * 200) {// 200M以内 15个线程下载
			threadCount = 15;
		} else {
			threadCount = 20;
		}
		return threadCount;
	}

	/**
	 * 单位换算
	 * 
	 * @param size
	 *            单位为B
	 * @param isInteger
	 *            是否返回取整的单位
	 * @return 转换后的单位
	 */
	public static String formatFileSize(long size, boolean isInteger) {
		DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
		DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");
		DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
		String fileSizeString = "0M";
		if (size < 1024 && size > 0) {
			fileSizeString = df.format((double) size) + "B";
		} else if (size < 1024 * 1024) {
			fileSizeString = df.format((double) size / 1024) + "K";
		} else if (size < 1024 * 1024 * 1024) {
			fileSizeString = df.format((double) size / (1024 * 1024)) + "M";
		} else {
			fileSizeString = df.format((double) size / (1024 * 1024 * 1024)) + "G";
		}
		return fileSizeString;
	}
}
