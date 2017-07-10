package com.jingdong.app.reader.service.download;

import java.io.Serializable;


public class TaskInfo implements Serializable{
	
	public static final int STATE_IN_QUEUE_DOWNLOADING = 100;// 下载中
	public static final int STATE_IN_QUEUE_DOWNLOADED = 101;// 下载完成
	public static final int STATE_IN_QUEUE_WAITING = 102;// 队列等待中
	public static final int STATE_IN_QUEUE_CANCEL = 103;// 下载取消

	public static final String DOWNLOAD_FILE_TYPE_EBOOK = "ebook";// 书城书籍
	public static final String DOWNLOAD_FILE_TYPE_DOCUMENT = "document";// 第三方书籍
	public static final String DOWNLOAD_FILE_TYPE_OHTER = "other";// 其他文件类型

	private String identity;//任务唯一标示符
	private String downloadType;// 当前下载文件类型
	private String downloadUrl;// 请求的url 不一定是最终下载地址
	private String saveFilePath;// 保存文件路径  
	
	private ExtraDetail detail;//额外信息 ebook类型 需要添加book对象数据 document 需要serverid

	
	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getDownloadType() {
		return downloadType;
	}

	public void setDownloadType(String downloadType) {
		this.downloadType = downloadType;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getSaveFilePath() {
		return saveFilePath;
	}

	public void setSaveFilePath(String saveFilePath) {
		this.saveFilePath = saveFilePath;
	}

	public ExtraDetail getDetail() {
		return detail;
	}

	public void setDetail(ExtraDetail detail) {
		this.detail = detail;
	}
	
}
