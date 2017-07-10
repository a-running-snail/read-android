package com.jingdong.app.reader.client;

import java.io.InputStream;
import java.util.Comparator;

import org.apache.http.HttpResponse;

import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.util.MZLog;


public abstract class DownloadedEntity implements DownloadedAble{
	protected int downloadStatus;
	public int id;
	protected boolean isMenualStop;
	private RequestEntry requestEntry;
	protected long mod_time;
	private int type;
	private int requestCode;
	private DownloadedEntity copy;
	protected long totalSize;
	protected long currentSize;
	protected String filePath;
	protected String url;
	public long _priority = 0;
	
	
	public DownloadedEntity getCopy() {
		return copy;
	}

	public void setCopy(DownloadedEntity copy) {
		this.copy = copy;
	}

	public void copyIntoIhis(DownloadedEntity downloadedEntity) {
		requestCode = downloadedEntity.requestCode;
		totalSize = downloadedEntity.totalSize;
		downloadStatus = downloadedEntity.downloadStatus;
		currentSize = downloadedEntity.currentSize;
		filePath = downloadedEntity.filePath;
		copy=downloadedEntity.copy;
	}
	
	 public String getFilePath(){
		return filePath; 
	 }
	 
	 public String getUrl(){
		return url;
	 }
	
	 public void setUrl(String url){
		this.url = url;
	 }
	 
	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public int getDownloadStatus(){
		return downloadStatus;
	}
	
	public void setDownloadStatus(int downloadStatus){
		this.downloadStatus = downloadStatus;
	}
	
	public long getId(){
		return id;
	}
	
	public boolean isTryGetContetnFromSD(){
		return false;
	}
	
	public void setTryGetContetnFromSD(boolean isTryGetContetnFromSD){
		
	}
	
	public int getRequestCode(){
		return requestCode;
	}
	
	public void setRequestCode(int requestCode){
		this.requestCode = requestCode;
	}
	
	
	public void setRequestEntry(RequestEntry requestEntry){
		this.requestEntry =  requestEntry;
	}

	public void setMenualStop(boolean isMenualStop) {
		this.isMenualStop = isMenualStop;
	}
	
	public void manualStop() {
		isMenualStop = true;
		stop();
	}
	
	public void saveLoadTime(long time){
		this.mod_time = time;
	}

	public void setCurrentSize(long loadProgress){
		this.currentSize = loadProgress;
	}
	
	public long getCurrentSize(){
		return currentSize;
	}
	
	private void stop() {
		if (requestEntry != null) {
			requestEntry.setStop(true);
			if (requestEntry._request != null) {
				if (!requestEntry._request.isAborted()) {
					requestEntry._request.abort();
				}
			}
		}
		if (downloadStatus == LocalBook.STATE_LOADING
				|| downloadStatus == LocalBook.STATE_LOAD_READY) {
			downloadStatus = LocalBook.STATE_LOAD_PAUSED;
		}
		DownloadService.refresh(this);
		mod_time = System.currentTimeMillis();
		this.saveState();
		if (requestEntry != null) {
			HttpResponse _response = requestEntry._response;
			if (_response != null) {
				try {
					_response.getEntity().consumeContent();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			InputStream inputStream = requestEntry._inputStream;
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
	
	}
	
	public boolean isManualStop(){
		return isMenualStop;
	}
	
	
	public int getType(){
		return type;
	}
	

}
