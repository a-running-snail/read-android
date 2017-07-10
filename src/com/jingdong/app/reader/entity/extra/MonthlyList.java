package com.jingdong.app.reader.entity.extra;

import android.os.Parcel;
import android.os.Parcelable;

public class MonthlyList implements Parcelable{

	private String cardNo;
	private int ebookCount;
	private String expiryDate;
	private String faceMoney;
	private String imgUrl;
	private String serverDate;
	private int serverId;
	private String serverStatusDesc;
	private String statusDesc;
	private int topCount;

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
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

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getServerDate() {
		return serverDate;
	}

	public void setServerDate(String serverDate) {
		this.serverDate = serverDate;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getServerStatusDesc() {
		return serverStatusDesc;
	}

	public void setServerStatusDesc(String serverStatusDesc) {
		this.serverStatusDesc = serverStatusDesc;
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

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(cardNo);
		out.writeString(expiryDate);
		out.writeString(faceMoney);
		out.writeString(imgUrl);
		out.writeString(serverDate);
		out.writeString(serverStatusDesc);
		out.writeString(statusDesc);
		out.writeInt(ebookCount);
		out.writeInt(serverId);
		out.writeInt(topCount);
	}
	
	public static final Parcelable.Creator<MonthlyList> CREATOR = new Creator<MonthlyList>(){
		@Override
		public MonthlyList createFromParcel(Parcel in) {
			return new MonthlyList(in);
		}
		@Override
		public MonthlyList[] newArray(int size) {
			return new MonthlyList[size];
		}
	};
	
	public MonthlyList(Parcel in) {
		cardNo = in.readString();
		expiryDate = in.readString();
		faceMoney = in.readString();
		imgUrl = in.readString();
		serverDate = in.readString();
		serverStatusDesc = in.readString();
		statusDesc = in.readString();
		ebookCount = in.readInt();
		serverId = in.readInt();
		topCount = in.readInt();
	}
	
}
