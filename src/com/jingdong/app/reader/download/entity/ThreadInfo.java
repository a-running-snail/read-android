package com.jingdong.app.reader.download.entity;

/**
 * 线程实体类
 * 
 * @author Beyond
 *
 */
public class ThreadInfo {

	private int id;
	private String url;
	private long start;
	private long end;
	private long finished;
	private long total;
	private int state = 0;// 0：未下载完成，1：下载完成
	private int acceptRange;//0：支持多线程下载，-1：不支持
	private int retryCount=0;//重新下载次数，一个线程下载失败后默认重试3次
	public ThreadInfo() {
		// TODO Auto-generated constructor stub
	}

	public ThreadInfo(int id, String url, long start, long end, long finished, long total) {
		super();
		this.id = id;
		this.url = url;
		this.start = start;
		this.end = end;
		this.finished = finished;
		this.total = total;
	}

	@Override
	public String toString() {
		return "ThreadInfo [id=" + id + ", url=" + url + ", start=" + start + ", end=" + end + ", finished=" + finished + ", total=" + total + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getFinished() {
		return finished;
	}

	public void setFinished(long finished) {
		this.finished = finished;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getAcceptRange() {
		return acceptRange;
	}

	public void setAcceptRange(int acceptRange) {
		this.acceptRange = acceptRange;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	
	
	
	

}
