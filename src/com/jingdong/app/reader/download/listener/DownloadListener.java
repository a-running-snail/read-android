package com.jingdong.app.reader.download.listener;

import com.jingdong.app.reader.download.entity.DownloadFileInfo;
/**
 * @ClassName: DownloadListener
 * @Description: 下载状态实时监听
 * @author J.Beyond
 * @date 2015年8月12日 下午5:50:50
 *
 */
public interface DownloadListener {

	//等待中
	public void onDownloadWait(DownloadFileInfo fileInfo);
	//下载中
	public void onDownloading(DownloadFileInfo fileInfo);
	//下载暂停
	public void onDownloadPause(DownloadFileInfo fileInfo);
	//下载完成
	public void onDownloadFinished(DownloadFileInfo fileInfo);
	//下载失败
	public void onDownloadError(DownloadFileInfo fileInfo,String errorInfo);
	//运营商流量网络下载确认
	public void onMobileNetConfirm(DownloadFileInfo fileInfo);
	
}
