package com.jingdong.app.reader.entity.extra;

import android.R.integer;

public class Update {

	private int appId;//1. 京东阅读大众版 2. 京东阅读DELL 3. 京东阅读三星
	private int appType;
	private String created;
	private String fatherVersion;//父版本
	private String id;
	private String info;//升级说明
	private String modified;
	private int nineOneUpGrade;//是否支持91升级
	private int platform;//平台，1为Android, 2:iPhone 3:iPad
	private String publishTime;//发布时间
	private String size;//升级文件大小
	private int status;//1为版本正常
	private int strategy;//1为一般升级，2为强制升级
	private String url;//下载地址
	private String version;
	public int getAppId() {
		return appId;
	}
	public void setAppId(int appId) {
		this.appId = appId;
	}
	public int getAppType() {
		return appType;
	}
	public void setAppType(int appType) {
		this.appType = appType;
	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public String getFatherVersion() {
		return fatherVersion;
	}
	public void setFatherVersion(String fatherVersion) {
		this.fatherVersion = fatherVersion;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getModified() {
		return modified;
	}
	public void setModified(String modified) {
		this.modified = modified;
	}
	public int getNineOneUpGrade() {
		return nineOneUpGrade;
	}
	public void setNineOneUpGrade(int nineOneUpGrade) {
		this.nineOneUpGrade = nineOneUpGrade;
	}
	public int getPlatform() {
		return platform;
	}
	public void setPlatform(int platform) {
		this.platform = platform;
	}
	public String getPublishTime() {
		return publishTime;
	}
	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getStrategy() {
		return strategy;
	}
	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
}
