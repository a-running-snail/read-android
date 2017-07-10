package com.jingdong.app.reader.client;

import com.jingdong.app.reader.util.FileGuider;


/**
 * 是否可下载接口
 */
public interface DownloadedAble {
	
	public static final int TYPE_BOOK = 0;
	public static final int TYPE_PLUG = 1;
	public static final int TYPE_DOCUMENT = 2;//云盘书籍
	
	public static int STATE_INIT = -1;
	public final static int STATE_UNLOAD = STATE_INIT + 1;
	public final static int STATE_LOADED = STATE_INIT + 2;// 已读
	public final static int STATE_LOAD_READY = STATE_INIT + 3;
	public final static int STATE_LOADING = STATE_INIT + 4;
	public final static int STATE_LOAD_FAILED = STATE_INIT + 5;
	public final static int STATE_LOAD_PAUSED = STATE_INIT + 6;
	public final static int STATE_LOAD_READING = STATE_INIT + 7;// 未读
	
	/**
	 * 当前下载状态
	 */
	public int getDownloadStatus();
	
	/**
	 * 设置下载状态
	 * @param state
	 */
	public void setDownloadStatus(int state);
	
	public boolean saveState();
	
	public boolean save();
	
	public long getId();
	
	/**
	 * 是否尝试从本地SD卡中加载内容
	 * @return
	 */
	public boolean isTryGetContetnFromSD();
	
	public void setTryGetContetnFromSD(boolean isTryGetContetnFromSD);
	
	public void manualStop();
	
	public boolean isManualStop();
	
	public int getType();
	
	public String getUrl();
	
	public String getFilePath();
	
	public void saveLoadTime(long time);
	
	public void setRequestEntry(RequestEntry requestEntry);
	
	public void setCurrentSize(long currentSize);
	
	public void setTotalSize(long size);
	
	public long getTotalSize();
	
	public long getCurrentSize();
	
	public int getRequestCode();
	
	public void setRequestCode(int requestCode);
	
	public FileGuider creatFileGuider();
	
	public boolean isBelongPagCode(int code);
	
	public DownloadedAble getCopy();
	
	public void setCopy(DownloadedAble downloadedAble);
}
