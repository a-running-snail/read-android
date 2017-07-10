package com.jingdong.app.reader.me.model;

public class ScoreRecordModel {

	private String created;//创建时间
	private int id;
	private int store;//正数为获得积分
	private int storeType;
	private String storeTypeStr;//类型
	private int storePlus;
	private String giftName;
	
	
	public int getStorePlus() {
		return storePlus;
	}
	public void setStorePlus(int storePlus) {
		this.storePlus = storePlus;
	}
	public String getGiftName() {
		return giftName;
	}
	public void setGiftName(String giftName) {
		this.giftName = giftName;
	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStore() {
		return store;
	}
	public void setStore(int store) {
		this.store = store;
	}
	public int getStoreType() {
		return storeType;
	}
	public void setStoreType(int storeType) {
		this.storeType = storeType;
	}
	public String getStoreTypeStr() {
		return storeTypeStr;
	}
	public void setStoreTypeStr(String storeTypeStr) {
		this.storeTypeStr = storeTypeStr;
	}
	
	
}
