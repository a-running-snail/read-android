package com.jingdong.app.reader.entity.extra;

import com.jingdong.app.reader.entity.MyOnlineBookEntity;

public class JDOnlineBookEntity {


	public String author;
	public String canRead;
	public String cardNo;
	public int cardStatus;
	public String ebookName;
	public int format;
	public String formatMeaning;
	public String imgUrl;
	public String largeSizeImgUrl;
	public long itemId;
	public String lastReadDate;
	public String lastReadDateStr;
	public String logo;
	public String plat;
	public String serveEndTime;
	public String serverEndTimeStr;
	public float size;
	public String statusDesc;
	public boolean pass;
	public boolean supportCardRead;
	
	
	public MyOnlineBookEntity toMyOnlineBookEntity(JDOnlineBookEntity entity){
		
		if(entity==null) return null;
		MyOnlineBookEntity onlineBookEntity=new MyOnlineBookEntity();
		onlineBookEntity.author=entity.author;
		onlineBookEntity.canRead=entity.canRead;
		onlineBookEntity.cardNo=entity.cardNo;
		onlineBookEntity.cardStatus=entity.cardStatus;
		onlineBookEntity.ebookName=entity.ebookName;
		onlineBookEntity.format=entity.format;
		onlineBookEntity.formatMeaning=entity.formatMeaning;
		onlineBookEntity.imgUrl=entity.imgUrl;
		onlineBookEntity.largeSizeImgUrl=entity.largeSizeImgUrl;
		onlineBookEntity.itemId=entity.itemId;
		onlineBookEntity.lastReadDate=entity.lastReadDate;
		onlineBookEntity.lastReadDateStr=entity.lastReadDateStr;
		onlineBookEntity.logo=entity.logo;
		onlineBookEntity.pass=entity.pass;
		onlineBookEntity.plat=entity.plat;
		onlineBookEntity.serveEndTime=entity.serveEndTime;
		onlineBookEntity.serverEndTimeStr=entity.serverEndTimeStr;
		onlineBookEntity.size=entity.size;
		onlineBookEntity.statusDesc=entity.statusDesc;
		onlineBookEntity.supportCardRead=entity.supportCardRead;
		return onlineBookEntity;
	}
}
