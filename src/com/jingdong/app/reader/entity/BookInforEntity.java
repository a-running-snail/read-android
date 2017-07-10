package com.jingdong.app.reader.entity;

import org.json.JSONObject;

import android.text.TextUtils;

import com.jingdong.app.reader.config.Constant;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.util.MZLog;

public class BookInforEntity extends Entity{
//
	public final static String KEY_BOOKID = "bookId";
	public final static String KEY_IMGURL = "imgUrl";
	public final static String KEY_BIG_IMGURL = "largeSizeImgUrl";
	public final static String KEY_PICURL = "picUrl";
	public final static String KEY_NAME = "name";
	public final static String KEY_AUTHOR = "author";
	public final static String KEY_SIZE = "size";
	public final static String KEY_STAR= "star";
	public final static String KEY_STAR_LEVEL= "starLevel";
	public final static String KEY_ISFREE = "free";
	public final static String KEY_PRICE= "price";
	public final static String KEY_JDPRICE= "jdPrice";
	public final static String KEY_BOOK_TYPE = "bookType";
	public final static String KEY_RESULTCOUNT = "resultCount";
	public final static String KEY_BOOKTYPENAME = "bookTypeName";
	public final static String KEY_WARENAME = "warename";//搜索出来的书名
	public final static String KEY_PPRICE = "pPrice";  //搜索中的价格  替换 jdprice
	public final static String KEY_ISCANBUY = "canBuy";
	public final static String KEY_CATTYPE = "catType";
	public final static String KEY_FORMATNAME = "format";
	public final static String KEY_PRICE_MESSAGE = "priceMessage";//“京东价”区域显示的文案
	
	/*
	 * 我的收藏里面额外的字段
	 */
	public final static String KEY_WPRICE = "wprice"; //市场价
	public final static String KEY_HPRICE = "hprice"; //京东价
	public final static String KEY_HPRICE_AS_YUAN = "hpriceAsYuan"; //京东价
	public final static String KEY_PRICE_AS_YUAN="priceAsYuan";//原价
	public long bookid;
	public String picUrl;
	public String bigPicUrl;
	public String picUrl_home;
	public String name;
	public String author;
	private double size=0;
	public int star;
	protected double price;  //原价
	protected double jdPrice;  //促销价
	protected String priceMessage = "";//“京东价”显示的区域用这个字段代替jdPrice
	public int bookType;
	public String bookTypeName;
	public String formatName; //电子书格式：PDF/EPUB

	public double wprice;
	public String hprice;
	//多媒体书,电子书能否购买
	public boolean isCanBuy  = true;
	public int resultCount = 0;
	public String warename = "";
	public  boolean isPrice=true;//是否有京东价  有价为true  没价为false
	public  boolean isFree=false;
	public  int fid;//关注列表中的 fid,用于取消关注
	public int catType = -1; //用于广告下图书列表中按钮的显示
							//0：加入购物车   1：免费下载   2：在线畅读	 3：免费领取
	//public SubBook subBook;
	public LocalBook localBook;
	
//	public Item
//   class BookOtherInfor{
//		String introduction;
//		int nolikeNum;
//		int lickNum;
//		int generalNum;
//	}
//
//  class RecommendedBook{
//	     int state;
//		 int type;
//		String imgurl;
//		String inforUrl;
//  }

	public static BookInforEntity parse(JSONObject bookJsoh) {
		BookInforEntity bookInfor = null;
		if (bookJsoh != null) {
			try {
				long bookId = DataParser.getLong(bookJsoh, BookInforEntity.KEY_BOOKID);
			//	if (bookId != -1) {
					bookInfor = new BookInforEntity();
					bookInfor.bookid = bookId;
					bookInfor.picUrl =  DataParser.getString(bookJsoh, BookInforEntity.KEY_PICURL);
					if(TextUtils.isEmpty(bookInfor.picUrl)){
					bookInfor.picUrl =  DataParser.getString(bookJsoh, BookInforEntity.KEY_IMGURL);
					}
					bookInfor.bigPicUrl = DataParser.getString(bookJsoh, BookInforEntity.KEY_BIG_IMGURL);
					bookInfor.picUrl_home =  DataParser.getString(bookJsoh, BookInforEntity.KEY_PICURL);
					bookInfor.name =  DataParser.getString(bookJsoh, BookInforEntity.KEY_NAME);
					bookInfor.author =  TextUtils.isEmpty(DataParser.getString(bookJsoh, BookInforEntity.KEY_AUTHOR).trim())?"佚名":DataParser.getString(bookJsoh, BookInforEntity.KEY_AUTHOR);
					bookInfor.bookTypeName =  DataParser.getString(bookJsoh, BookInforEntity.KEY_BOOKTYPENAME);
					bookInfor.setSize(DataParser.getDouble(bookJsoh, BookInforEntity.KEY_SIZE));
					bookInfor.star =  DataParser.getInt(bookJsoh, BookInforEntity.KEY_STAR);
					if (bookInfor.star < 0)
						bookInfor.star =  DataParser.getInt(bookJsoh, BookInforEntity.KEY_STAR_LEVEL);
					bookInfor.isCanBuy=DataParser.getBoolean(bookJsoh,BookInforEDetail.KEY_ISCANBUY);
					bookInfor.isFree=DataParser.getBoolean(bookJsoh, BookInforEntity.KEY_ISFREE);
					bookInfor.catType = DataParser.getInt(bookJsoh, BookInforEntity.KEY_CATTYPE);
					if(bookInfor.catType == -1){
						bookInfor.catType = 0;
					}
					bookInfor.formatName = DataParser.getString(bookJsoh, BookInforEntity.KEY_FORMATNAME);
					
					//价格相关
					bookInfor.price =  DataParser.getDouble(bookJsoh, BookInforEntity.KEY_PRICE);
					bookInfor.jdPrice = DataParser.getDouble(bookJsoh,BookInforEntity.KEY_JDPRICE);
					bookInfor.priceMessage = DataParser.getString(bookJsoh, KEY_PRICE_MESSAGE);

					bookInfor.bookType =  DataParser.getInt(bookJsoh,
							BookInforEntity.KEY_BOOK_TYPE);

					bookInfor.wprice =  DataParser.getDouble(bookJsoh,
							BookInforEntity.KEY_WPRICE);
					bookInfor.hprice =  DataParser.getString(bookJsoh,
							BookInforEntity.KEY_HPRICE);
					if (TextUtils.isEmpty(bookInfor.hprice)) {
						bookInfor.hprice=" 暂无报价";
					}else {
						bookInfor.hprice=" "+Constant.REN_MIN_BI+" "+bookInfor.hprice;
					}
//					if (!TextUtils.isEmpty(bookInfor.price)) {
//						bookInfor.price=Contants.REN_MIN_BI+bookInfor.price;
//					}
					
					bookInfor.fid = DataParser.getInt(bookJsoh, "fid");

				//}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bookInfor;
	}

	public static BookInforEntity parseSearch(JSONObject bookJsoh) {
		BookInforEntity bookInfor = null;
		if (bookJsoh != null) {
			try {
				bookInfor = BookInforEntity.parse(bookJsoh);
				MZLog.d("BookInforEntity", "bookInfor.....==="+bookInfor.picUrl);
				bookInfor.name =  DataParser.getString(bookJsoh, BookInforEntity.KEY_WARENAME);
				bookInfor.price=DataParser.getDouble(bookJsoh,BookInforEntity.KEY_PRICE_AS_YUAN);//搜索页返回的是促销价pPrice
				bookInfor.jdPrice = DataParser.getDouble(bookJsoh,
						BookInforEntity.KEY_PPRICE);//搜索页返回的是促销价pPrice
				bookInfor.priceMessage = DataParser.getString(bookJsoh, KEY_PRICE_MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bookInfor;
	}
	
	/**
	 * 显示原价，如果原价大于等于京东价（促销价），不显示
	 * @return
	 */
	public String getPrice(){
		if(price <= jdPrice){
			return "";
		}
		return Constant.REN_MIN_BI + price;
	}
	
	/**
	 * 限时免费 划线的价格显示jdprice,其它划线的价格显示price
	 * @return
	 */
	public String getJdPrice(){
		if(jdPrice<=0){
			return "";
		}
		return Constant.REN_MIN_BI + jdPrice;
	}
	
	/**
	 * 要显示京东价的地方都要这个方法（除了购物车页面外）
	 * @return
	 */
	public String getPriceMessage(){
		if(TextUtils.isEmpty(priceMessage)){
			return Constant.REN_MIN_BI + jdPrice;
		}
		return this.priceMessage;
	}
	
	
	public void setPriceMessage(String message){
		this.priceMessage = message;
	}
	
	
	@Override
	public String getImageUrl() {
		// TODO Auto-generated method stub
		return picUrl;
	}
	
	public void setSize(double size) {
		this.size = size;
	}

	public double getSize() {
		return size;
	}

	@Override
	public String getHomeImageUrl() {
		// TODO Auto-generated method stub
		return picUrl_home;
	}

	@Override
	public String getFilePath() {
		// TODO Auto-generated method stub
		return null;
	}

}
