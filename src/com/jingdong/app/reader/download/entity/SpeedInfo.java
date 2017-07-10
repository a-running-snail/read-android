package com.jingdong.app.reader.download.entity;

/**
 *
 * @ClassName: SpeedInfo
 * @Description: 下载速度，保存上一时刻已完成的下载量和时间
 * @author J.Beyond
 * @date 2015年8月12日 下午5:48:26
 *
 */
public class SpeedInfo {

	private long lastTimeMillis=0;
	private long lastFinished=0;
	
	public long getLastTimeMillis() {
		return lastTimeMillis;
	}
	public void setLastTimeMillis(long lastTimeMillis) {
		this.lastTimeMillis = lastTimeMillis;
	}
	public long getLastFinished() {
		return lastFinished;
	}
	public void setLastFinished(long lastFinished) {
		this.lastFinished = lastFinished;
	}
	
	
	public void	reset() {
		this.lastTimeMillis = 0;
		this.lastFinished = 0;
	}
	
}
