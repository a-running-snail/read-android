package com.jingdong.app.reader.download.listener;

import com.jingdong.app.reader.download.entity.DownloadFileInfo;

/**
 * @ClassName: DownloadInitListener
 * @Description: 下载状态初始化监听
 * @author J.Beyond
 * @date 2015年8月12日 下午5:50:23
 *
 */
public interface DownloadInitListener {

	public void onInitSuccess(DownloadFileInfo fileInfo);
	
	public void onInitFail(DownloadFileInfo fileInfo,String errorInfo);
	
}
