package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ReadCardInfo implements Parcelable{

	private int amountType;// 卡类型
	private int appType;// 属于电子书
	private int canReadCount;// 畅读卡可以阅读的书数量
	private String cardName;
	private String cardNo;// 卡号
	private int cardStatus;// 卡状态码
	private int ebookCount;// 读过多少本
	private String expiryDate;// 服务时间
	private String faceMoney;
	private String id;
	private String imgUrl;// 卡图片
	private String pin;
	private String serveEndTime;
	private String serveStartTime;
	private String statusDesc;// 卡状态
	private int topCount;// 卡的读书量上限

	

	public int getAmountType() {
		return amountType;
	}

	public void setAmountType(int amountType) {
		this.amountType = amountType;
	}

	public int getAppType() {
		return appType;
	}

	public void setAppType(int appType) {
		this.appType = appType;
	}

	public int getCanReadCount() {
		return canReadCount;
	}

	public void setCanReadCount(int canReadCount) {
		this.canReadCount = canReadCount;
	}

	public String getCardName() {
		return cardName;
	}

	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public int getCardStatus() {
		return cardStatus;
	}

	public void setCardStatus(int cardStatus) {
		this.cardStatus = cardStatus;
	}

	public int getEbookCount() {
		return ebookCount;
	}

	public void setEbookCount(int ebookCount) {
		this.ebookCount = ebookCount;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getFaceMoney() {
		return faceMoney;
	}

	public void setFaceMoney(String faceMoney) {
		this.faceMoney = faceMoney;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getServeEndTime() {
		return serveEndTime;
	}

	public void setServeEndTime(String serveEndTime) {
		this.serveEndTime = serveEndTime;
	}

	public String getServeStartTime() {
		return serveStartTime;
	}

	public void setServeStartTime(String serveStartTime) {
		this.serveStartTime = serveStartTime;
	}

	public String getStatusDesc() {
		return statusDesc;
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}

	public int getTopCount() {
		return topCount;
	}

	public void setTopCount(int topCount) {
		this.topCount = topCount;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 *
	private int amountType;// 卡类型
	private int appType;// 属于电子书
	private int canReadCount;// 畅读卡可以阅读的书数量
	private String cardName;
	private String cardNo;// 卡号
	private int cardStatus;// 卡状态码
	private int ebookCount;// 读过多少本
	private String expiryDate;// 服务时间
	private String faceMoney;
	private String id;
	private String imgUrl;// 卡图片
	private String pin;
	private String serveEndTime;
	private String serveStartTime;
	private String statusDesc;// 卡状态
	private int topCount;// 卡的读书量上限
	 */
	@Override
	public void writeToParcel(Parcel out, int arg1) {
		// TODO Auto-generated method stub
		out.writeInt(amountType);
		out.writeInt(appType);
		out.writeInt(canReadCount);
		out.writeInt(cardStatus);
		out.writeInt(ebookCount);
		out.writeInt(topCount);
		out.writeString(cardName);
		out.writeString(cardNo);
		out.writeString(expiryDate);
		out.writeString(faceMoney);
		out.writeString(imgUrl);
		out.writeString(pin);
		out.writeString(serveEndTime);
		out.writeString(serveStartTime);
		out.writeString(statusDesc);
	}
	
	public static final Parcelable.Creator<ReadCardInfo> CREATOR = new Creator<ReadCardInfo>(){
		@Override
		public ReadCardInfo createFromParcel(Parcel in) {
			return new ReadCardInfo(in);
		}
		@Override
		public ReadCardInfo[] newArray(int size) {
			return new ReadCardInfo[size];
		}
	};
	
	public ReadCardInfo(Parcel in) {
		amountType = in.readInt();
		appType = in.readInt();
		canReadCount = in.readInt();
		cardStatus = in.readInt();
		ebookCount = in.readInt();
		topCount = in.readInt();
		
		cardName = in.readString();
		cardNo = in.readString();
		expiryDate = in.readString();
		faceMoney = in.readString();
		imgUrl = in.readString();
		pin = in.readString();
		serveEndTime = in.readString();
		serveStartTime = in.readString();
		statusDesc = in.readString();
	}
}
