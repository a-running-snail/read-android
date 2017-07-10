package com.jingdong.app.reader.community.square;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import com.jingdong.app.reader.community.square.entity.BookInfoEntity;
import com.jingdong.app.reader.community.square.entity.SquareEntity;
import com.jingdong.app.reader.community.square.entity.UserBaseInfoEntity;
import com.jingdong.app.reader.community.square.entity.UsersEntity;

public class JsonParseUtils {
	
	/**
	* @Description: 解析社区->广场数据
	* @author xuhongwei1
	* @date 2015年11月2日 上午11:04:15 
	* @throws 
	*/ 
	public static ArrayList<SquareEntity> parseSquareEntity(JSONObject jsonObject) {
		ArrayList<SquareEntity> mSquareList = new ArrayList<SquareEntity>();
		
		String codeStr = jsonObject.optString("code");
		if("0".equals(codeStr)) {
			JSONArray resultList = jsonObject.optJSONArray("resultList");
			if(null != resultList) {
				for(int i=0; i<resultList.length(); i++) {
					SquareEntity mSquareEntity = new SquareEntity();
					JSONObject json = resultList.optJSONObject(i);
					if(null != json) {
						mSquareEntity.bookId = json.optLong("bookId");
						mSquareEntity.commentCount = json.optInt("commentCount");
						mSquareEntity.commentdate = json.optString("commentdate");
						mSquareEntity.content = json.optString("content");
						mSquareEntity.guid = json.optString("guid");
						mSquareEntity.id = json.optLong("id");
						JSONObject newBookItemVo = json.optJSONObject("newBookItemVo");
						if(null != newBookItemVo) {
							BookInfoEntity bookinfo = new BookInfoEntity();
							bookinfo.author = newBookItemVo.optString("author");
							bookinfo.borrowEndTime = newBookItemVo.optString("borrowEndTime");
							bookinfo.borrowStartTime = newBookItemVo.optString("borrowStartTime");
							bookinfo.currentTime = newBookItemVo.optString("currentTime");
							bookinfo.ebookId = newBookItemVo.optLong("ebookId");
							bookinfo.fileSize = newBookItemVo.optDouble("fileSize");
							bookinfo.format = newBookItemVo.optString("format");
							bookinfo.good = newBookItemVo.optInt("good");
							bookinfo.imageUrl = newBookItemVo.optString("imageUrl");
							bookinfo.info = newBookItemVo.optString("info");
							bookinfo.isAlreadyBorrow = newBookItemVo.optBoolean("isAlreadyBorrow");
							bookinfo.isAlreadyBuy = newBookItemVo.optBoolean("isAlreadyBuy");
							bookinfo.isBorrow = newBookItemVo.optBoolean("isBorrow");
							bookinfo.isBuy = newBookItemVo.optBoolean("isBuy");
							bookinfo.isEBook = newBookItemVo.optBoolean("isEBook");
							bookinfo.isFluentRead = newBookItemVo.optBoolean("isFluentRead");
							bookinfo.isFree = newBookItemVo.optBoolean("isFree");
							bookinfo.isUserCanFluentRead = newBookItemVo.optBoolean("isUserCanFluentRead");
							bookinfo.isUserFluentReadAddToCard = newBookItemVo.optBoolean("isUserFluentReadAddToCard");
							bookinfo.jdPrice = newBookItemVo.optDouble("jdPrice");
							bookinfo.largeImageUrl = newBookItemVo.optString("largeImageUrl");
							bookinfo.name = newBookItemVo.optString("name");
							bookinfo.paperBookId = newBookItemVo.optLong("paperBookId");
							bookinfo.price = newBookItemVo.optDouble("price");
							bookinfo.priceMessage = newBookItemVo.optString("priceMessage");
							bookinfo.publisher = newBookItemVo.optString("publisher");
							bookinfo.star = newBookItemVo.optInt("star");
							bookinfo.userBorrowEndTime = newBookItemVo.optString("userBorrowEndTime");
							bookinfo.userBorrowStartTime = newBookItemVo.optString("userBorrowStartTime");
							
							mSquareEntity.mBookInfoEntity = bookinfo;
						}
						else{
							mSquareEntity.mBookInfoEntity= new BookInfoEntity(); 
						}
						
						mSquareEntity.rating = json.optInt("rating");
						mSquareEntity.recommend = json.optBoolean("recommend");
						mSquareEntity.recommendsCount = json.optInt("recommendsCount");
						mSquareEntity.renderId = json.optLong("renderId");
						mSquareEntity.renderType = json.optString("renderType");
						
						JSONObject userBaseInfo = json.optJSONObject("userBaseInfo");
						if(null != userBaseInfo) {
							UserBaseInfoEntity userinfo = new UserBaseInfoEntity();
							userinfo.email = userBaseInfo.optString("email");
							userinfo.nickName = userBaseInfo.optString("nickName");
							userinfo.pin = userBaseInfo.optString("pin");
							userinfo.regTime = userBaseInfo.optString("regTime");
							userinfo.usex = userBaseInfo.optString("usex");
							userinfo.yunBigImageUrl = userBaseInfo.optString("yunBigImageUrl");
							userinfo.yunMidImageUrl = userBaseInfo.optString("yunMidImageUrl");
							userinfo.yunSmaImageUrl = userBaseInfo.optString("yunSmaImageUrl");
							
							mSquareEntity.mUserInfoEntity = userinfo;
						}else{
							UserBaseInfoEntity userinfo = new UserBaseInfoEntity();
							mSquareEntity.mUserInfoEntity = userinfo;
						}
						
						mSquareEntity.userId = json.optLong("userId");
						
						JSONObject users = json.optJSONObject("users");
						if(null != users) {
							UsersEntity ue = new UsersEntity();
							ue.avatar = users.optString("avatar");
							ue.createdAt = users.optString("createdAt");
							ue.deviceId = users.optString("deviceId");
							ue.id = users.optLong("id");
							ue.jdUserName = users.optString("jdUserName");
							ue.lastReadEntityId = users.optLong("lastReadEntityId");
							ue.name = users.optString("name");
							ue.role = users.optLong("role");
							ue.updatedAt = users.optString("updatedAt");
							ue.vMember = users.optInt("vMember");
							
							mSquareEntity.mUsersEntity = ue;
						}else
						{
							UsersEntity ue = new UsersEntity();
							mSquareEntity.mUsersEntity = ue;
						}
						
					}
					
					mSquareList.add(mSquareEntity);
				}
			}
		}
		
		return mSquareList;
	}

}
