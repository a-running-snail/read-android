
package com.jingdong.app.reader.entity;

import org.json.JSONObject;

import android.text.TextUtils;

import com.jingdong.app.reader.data.DataParser;


public class BookInforEDetail extends BookInforEntity {
//	public final static String KEY_SIZE = "size";
//	public final static String KEY_PRICE = "price";
	public final static String KEY_BOOKNAME = "bookName";
	public final static String KEY_PAGES = "pages";
	public final static String KEY_KB = "kb";
//	public final static String KEY_AUTHOR = "author";
//	public final static String KEY_STAR = "star";
	public final static String KEY_INFO = "info";
//	public final static String KEY_BOOKTYPE = "bookType";
	public final static String KEY_PERFECT = "perfect";
	public final static String KEY_GOOD = "good";
	public final static String KEY_WEAK = "weak";
	public final static String KEY_RELATEID = "relateId";
	public final static String KEY_RELATEPRICE = "relatePrice";
	public final static String KEY_TYPE = "type";
	public final static String KEY_SAMLLSIZEIMGURL = "Logo";
	public final static String KEY_LARGESIZEIMGURL = "largeLogo";
	public final static String KEY_CAN_READ_ONLINE="fluentRead";//畅读按钮。
//	public final static String KEY_ISFree = "free";
	public final static String KEY_MESSAGE = "message";
	public final static String KEY_LASTCOMMENT = "lastComment";
//	public final static String KEY_BOOK_FORMATE="format";//图书类型，"pdf" or "epub".
    public final static String KEY_PROMO_INFO="promotionInfo";// 促销信息
    public final static String KEY_ISBUY = "buy";
    public final static String KEY_CANFREEGET = "canFreeGet";
    public final static String KEY_TRYREAD = "tryRead";

    public String size="";
//    public double price=0.00;
    public double ePrice=0.00;
    public String bookName="";
    public String pages="";
    public String KB = "";
    public String author = "";
    public int star;
    public String info;
    public int bookType = 1;
    public int perfect = 0;
    public int good = 0;
    public int weak = 0;
    public boolean canBuy = false;
//    public boolean fluentRead = false;
    public boolean tryRead = false; //能否试读
    public boolean canFreeGet = false; //用于畅读卡免费试用的促销活动
    public boolean canReadOnline=false;//能否畅读。
    public boolean isBuy = false;  //能否加入购物车和立即购买 
//    public boolean free = false;
    //电子书对应的纸质书
    public int relateId = -1;
    public double relatePrice = 0;
    //public boolean isFree=false;
    public long cartid;//购物车也用这个对象
    public int count =0;
    public String formatName="";//图书格式：pdf or epub。
    public String largeSizeImgUrl;  //大图

    public String lastComment;

   // public String chapter_id="";//在线畅读需要。
   // public boolean isFirstLoad=false;//在线畅读需要。
	public String message = "";
    public String promoInfor="";
    

    public static BookInforEDetail parse(JSONObject bookJsoh){
    	//BookInforEntity.parse(bookJsoh);
    	BookInforEDetail bookE = null;
    	if(bookE==null){
    		bookE = new BookInforEDetail();
    		bookE.size = DataParser.getString(bookJsoh, BookInforEDetail.KEY_SIZE)+"M";
    		bookE.price = DataParser.getDouble(bookJsoh, BookInforEDetail.KEY_PRICE);
    		bookE.relatePrice = DataParser.getDouble(bookJsoh, BookInforEDetail.KEY_RELATEPRICE);
    		bookE.bookName = DataParser.getString(bookJsoh, BookInforEDetail.KEY_BOOKNAME);
    		bookE.pages = DataParser.getString(bookJsoh, BookInforEDetail.KEY_PAGES);
    		bookE.KB = DataParser.getString(bookJsoh, BookInforEDetail.KEY_KB);
    		bookE.author = TextUtils.isEmpty(DataParser.getString(bookJsoh, BookInforEDetail.KEY_AUTHOR.trim()))?"佚名":DataParser.getString(bookJsoh, BookInforEDetail.KEY_AUTHOR);
    		bookE.star = DataParser.getInt(bookJsoh, BookInforEDetail.KEY_STAR);
    		bookE.info = DataParser.getString(bookJsoh, BookInforEDetail.KEY_INFO);

    		// booktype应该从服务器返回的json中取得吧？
    		bookE.bookType = DataParser.getInt(bookJsoh, BookInforEDetail.KEY_BOOK_TYPE);
    		bookE.perfect = DataParser.getInt(bookJsoh, BookInforEDetail.KEY_PERFECT);
    		bookE.good = DataParser.getInt(bookJsoh, BookInforEDetail.KEY_GOOD);
    		bookE.weak = DataParser.getInt(bookJsoh, BookInforEDetail.KEY_WEAK);
    		bookE.bookid = DataParser.getInt(bookJsoh, BookInforEDetail.KEY_BOOKID);
    		bookE.relateId = DataParser.getInt(bookJsoh, BookInforEDetail.KEY_RELATEID);
    		bookE.picUrl =  DataParser.getString(bookJsoh, BookInforEDetail.KEY_PICURL);
			if(TextUtils.isEmpty(bookE.picUrl)){
				bookE.picUrl =  DataParser.getString(bookJsoh, BookInforEDetail.KEY_SAMLLSIZEIMGURL);
			}
           // bookE.promoInfor="促销信息：该商品参加满减活动，购买活动商品每满88元，可减18元现金。";
			//  bookE.promoInfor="促销信息："+DataParser.getString(bookJsoh, BookInforEDetail.KEY_PROMO_INFO);  促销信息。
    		bookE.message =  DataParser.getString(bookJsoh, BookInforEDetail.KEY_MESSAGE);
    		bookE.largeSizeImgUrl = DataParser.getString(bookJsoh,BookInforEDetail.KEY_LARGESIZEIMGURL);
    		bookE.isCanBuy = DataParser.getBoolean(bookJsoh,BookInforEDetail.KEY_ISCANBUY);
    		bookE.canReadOnline=DataParser.getBoolean(bookJsoh,BookInforEDetail.KEY_CAN_READ_ONLINE);
    		bookE.isFree = DataParser.getBoolean(bookJsoh,BookInforEDetail.KEY_ISFREE);
    		bookE.isBuy = DataParser.getBoolean(bookJsoh,BookInforEDetail.KEY_ISBUY);
    		bookE.canFreeGet = DataParser.getBoolean(bookJsoh, BookInforEDetail.KEY_CANFREEGET);
    		bookE.tryRead = DataParser.getBoolean(bookJsoh, BookInforEDetail.KEY_TRYREAD);
    		//bookE.lastComment = DataParser.getString(bookJsoh, BookInforEDetail.KEY_LASTCOMMENT);
//    		bookE.jdPrice = TextUtils.isEmpty(DataParser.getString(bookJsoh,
//					BookInforEntity.KEY_JDPRICE))?"暂无报价":DataParser.getString(bookJsoh,
//							BookInforEntity.KEY_JDPRICE);
    		bookE.jdPrice = DataParser.getDouble(bookJsoh,
					BookInforEntity.KEY_JDPRICE);
			bookE.formatName=DataParser.getString(bookJsoh, BookInforEDetail.KEY_FORMATNAME);
//			if (TextUtils.isEmpty(price)) {
//				price="暂无报价";
//				bookE.isPrice=false;
//				bookE.jdPrice = price;
//			}else {
//				bookE.isPrice=true;
//				bookE.jdPrice = Contants.REN_MIN_BI+price;
//			}
			
			bookE.priceMessage = DataParser.getString(bookJsoh, KEY_PRICE_MESSAGE);

    		if(DataParser.getInt(bookJsoh,KEY_TYPE)!=-1){
    			bookE.bookType =  DataParser.getInt(bookJsoh,KEY_TYPE);
    		}

    	}
         return bookE;
    }
    @Override
	public String getImageUrl() {
//		return picUrl;
		return largeSizeImgUrl;
	}

}
