package com.jingdong.app.reader.client;

public abstract class OnDownloadListener {
	public void onDownloadStart(RequestEntry requestEntry){};
	public abstract void onprogress(long progress,long max);
    public  abstract void onDownloadCompleted(RequestEntry requestEntry);
}
