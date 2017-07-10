package com.jingdong.app.reader.entity;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.util.JSONArrayPoxy;
import com.jingdong.app.reader.util.JSONObjectProxy;
import com.jingdong.app.reader.util.MZLog;


public class Product implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6454309392186382154L;

	//private static final String TAG = "Product";

	public static final int CRAZY = 0;
	public static final int SEARCH_CATEGORY = 1;
	public static final int PRODUCT_DETAIL = 3;
	public static final int RECOMAND_PRODECT = 4;
	public static final int USER_INFO = 5;
	public static final int ORDER_LIST = 6;
	public static final int COLLECT_LIST = 7;
	public static final int MESSAGE_LIST = 8;
	public static final int CART_LIST = 9;
	public static final int PROMOTION = 10;
	public static final int WARE_ID_BY_BAR_CODE_LIST = 11;
	public static final int EASY_LIST = 12;
	public static final int PACKS_LIST = 13;
	//private int nIndex = 0;

	public Product() {

	}

	/**
	 * 常规用于遍历
	 */
	public Product(JSONObjectProxy jsonObject, int functionId) {
		this(jsonObject, null, functionId);
	}

	/**
	 * 常规用于遍历（可变参数）
	 */
	public Product(JSONObjectProxy jsonObject, int functionId, Object[] varargs) {
		this(jsonObject, null, functionId, varargs);
	}

	/**
	 * 用于带多个图片的单个产品
	 */
	public Product(JSONObjectProxy jsonObject, JSONArray jsonImageArray, int functionId) {
		this(jsonObject, jsonImageArray, functionId, null);
	}

	/**
	 * 全功能
	 */
	private Product(JSONObjectProxy jsonObject, JSONArray jsonImageArray, int functionId, Object[] varargs) {
		update(jsonObject, jsonImageArray, functionId, varargs);
	}

	/**
	 * 更新
	 */
	public void update(JSONObjectProxy jsonObject, JSONArray jsonImageArray, int functionId) {
		update(jsonObject, jsonImageArray, functionId, null);
	}

	/**
	 * 更新
	 */
	public void update(JSONObjectProxy jsonObject, JSONArray jsonImageArray, int functionId, Object[] varargs) {

		switch (functionId) {

		case CRAZY:
			setId(jsonObject.getLongOrNull("wareId"));// ID
			setName(jsonObject.getStringOrNull("wname"));// 名称
			setImage(jsonObject.getStringOrNull("imageurl"), null);// 默认图片
			setJdPrice(jsonObject.getStringOrNull("jdPrice"));// 京东价
			setImgPrice(jsonObject.getStringOrNull("wmaprice"));// 京东价，图片形式
			setMarketPrice(jsonObject.getStringOrNull("marketPrice"));// 市场价
			if (isFiledExist(jsonObject, "adword")) {
				setAdWord(jsonObject.getStringOrNull("adword"));// 广告词
			}
			break;

		case SEARCH_CATEGORY:
			setId(jsonObject.getLongOrNull("wareId"));// ID
			setImage(jsonObject.getStringOrNull("imageurl"), null);// 默认图片
			setName(jsonObject.getStringOrNull("wname"));// 名称
			setAdWord(jsonObject.getStringOrNull("adword"));// 广告词
			setMarketPrice(jsonObject.getStringOrNull("martPrice"));// 市场价
			setJdPrice(jsonObject.getStringOrNull("jdPrice"));// 京东价
			break;
		case PACKS_LIST:
			setId(jsonObject.getLongOrNull("SkuId"));// ID
			if (varargs[0] != null) {
				// 测试数据
				// setImage((String)varargs[0]+"n5/"+jsonObject.getStringOrNull("SkuPicUrl"),
				// null);
				setImage((String) varargs[0] + jsonObject.getStringOrNull("SkuPicUrl"), null);// 默认图片,连接domain和图片名称，组成图片URL
			} else {
				setImage(jsonObject.getStringOrNull("SkuPicUrl"), null);// 默认图片
			}
			setName(jsonObject.getStringOrNull("SkuName"));// 名称
			// setAdWord(jsonObject.getStringOrNull("adword"));// 广告词
			// setMarketPrice(jsonObject.getStringOrNull("martPrice"));// 市场价
			// setJdPrice(jsonObject.getStringOrNull("jdPrice"));// 京东价
			break;
		case COLLECT_LIST:
			setId(jsonObject.getLongOrNull("wareId"));// ID
			setImage(jsonObject.getStringOrNull("imageurl"), null);// 默认图片
			setName(jsonObject.getStringOrNull("wname"));// 名称
			setAdWord(jsonObject.getStringOrNull("adword"));// 广告词
			setMarketPrice(jsonObject.getStringOrNull("martPrice"));// 市场价
			setJdPrice(jsonObject.getStringOrNull("jdPrice"));// 京东价
			setOrderId(jsonObject.getStringOrNull("orderId"));// 订单号
			setBook(jsonObject.getBooleanOrNull("book"));// 是否图书
			fid = jsonObject.getStringOrNull("fid");// 收藏项ID
			break;
		case PRODUCT_DETAIL:
			setId(jsonObject.getLongOrNull("wareId"));// ID
			setShowId(getId());
			setName(jsonObject.getStringOrNull("wname"));// 名称
			setAdWord(jsonObject.getStringOrNull("adword"));// 广告词
			setMarketPrice(jsonObject.getStringOrNull("marketPrice"));// 市场价
			setJdPrice(jsonObject.getStringOrNull("jdPrice"));// 京东价
			setBook(jsonObject.getBooleanOrNull("isbook"));// 是否图书
			setPromotion(jsonObject.getBooleanOrNull("promotion"));// 是否促销
			setUserPriceLabel(jsonObject.getStringOrNull("userLevel"));// 会员价Label
			setUserPriceContent(jsonObject.getStringOrNull("userLevelPrice"));// 会员价Content

			// 是否分区库存
			Boolean directShow = jsonObject.getBooleanOrNull("directShow");
			if (null != directShow && directShow) {
				setProvinceStockMode(1);
			}

			try {
				imageList.clear();
				for (int i = 0; i < jsonImageArray.length(); i++) {
					JSONObject jsonImage = jsonImageArray.getJSONObject(i);
					imageList.add(new Image(jsonImage, Image.PRODUCTDETAIL));
				}
			} catch (JSONException e) {
				MZLog.d(Product.class.getName(), e.getMessage());
			}

			break;
		case RECOMAND_PRODECT:
			setId(jsonObject.getLongOrNull("wareId"));// ID
			setName(jsonObject.getStringOrNull("wname"));// 名称
			if (isFiledExist(jsonObject, "adword")) {
				setAdWord(jsonObject.getStringOrNull("adword"));// 广告词
			}

			setMarketPrice(jsonObject.getStringOrNull("martPrice"));// 市场价
			setJdPrice(jsonObject.getStringOrNull("jdPrice"));// 京东价
			setImage(jsonObject.getStringOrNull("imageurl"), null);// 默认图片
			// try {
			// appendImgUrl(jsonObject.getString("imageurl"),nIndex++);
			// } catch (JSONException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			break;
		case USER_INFO:
			setUsername(jsonObject.getStringOrNull("unickName"));
			setImage(jsonObject.getStringOrNull("imgUrl"), null);
			setUserClass(jsonObject.getStringOrNull("uclass"));
			break;
		case ORDER_LIST:
			// String sOrderId,sTotalPrice,sOrderStatus,sSubmitTime;
			setOrderId(jsonObject.getStringOrNull("orderId"));
			setOrderStatus(jsonObject.getStringOrNull("orderStatus"));
			setOrderPrice(jsonObject.getStringOrNull("price"));
			setOrderSubtime(jsonObject.getStringOrNull("dataSubmit"));
			setNum(jsonObject.getIntOrNull("num"));
			setId(jsonObject.getLongOrNull("wareId"));// ID
			setName(jsonObject.getStringOrNull("wname"));// 名称
			setImage(jsonObject.getStringOrNull("imageurl"), null);// 默认图片
			setmPaymentType(jsonObject.getStringOrNull("paymentType"));
			setSubOrderFlag(jsonObject.getBooleanOrNull("subOrder"));
			break;
		case MESSAGE_LIST:
			setMessageFlag(jsonObject.getStringOrNull("msgFlag"));
			setMessageType(jsonObject.getStringOrNull("msgType"));
			setMessageTime(jsonObject.getStringOrNull("createTime"));
			setMessageBody(jsonObject.getStringOrNull("msgBody"));
			setImage(jsonObject.getStringOrNull("imgUrl"), null);
			setOrderId(jsonObject.getStringOrNull("ordId"));
			setId(jsonObject.getLongOrNull("proId"));
			setsMsgUpdateTime(jsonObject.getStringOrNull("updateTime"));
			setsMsgId(jsonObject.getStringOrNull("msgId"));
			setMsgTypeName(jsonObject.getStringOrNull("msgName"));

			break;
		case CART_LIST:
			setId(jsonObject.getLongOrNull("Id"));// ID
			setName(jsonObject.getStringOrNull("Name"));// 名称
			setJdPrice(jsonObject.getStringOrNull("Price"));// 京东价
			setJdDixcount(jsonObject.getStringOrNull("Discount"));
			setImgPrice(jsonObject.getStringOrNull("PriceImg"));// 京东价，图片形式
			setItemCount(jsonObject.getIntOrNull("Num"));
			// setId(jsonObject.getLongOrNull("wareId"));// 商品编号
			break;
		case PROMOTION:
			setId(jsonObject.getLongOrNull("wareId"));
			setAdWord(jsonObject.getStringOrNull("adword"));
			setBook(jsonObject.getBooleanOrNull("book"));
			setName(jsonObject.getStringOrNull("wname"));
			setNum(jsonObject.getIntOrNull("num"));
			break;
		case WARE_ID_BY_BAR_CODE_LIST:
			setAdWord(jsonObject.getStringOrNull("adword"));// 广告词
			setBook(jsonObject.getBooleanOrNull("book"));// 是否图书
			setImage(jsonObject.getStringOrNull("imageurl"), null);// 默认图片
			setMarketPrice(jsonObject.getStringOrNull("martPrice"));// 市场价
			setId(jsonObject.getLongOrNull("wareId"));// 商品编号
			setName(jsonObject.getStringOrNull("wname"));// 商品名称
			break;
		case EASY_LIST:
			setName(jsonObject.getStringOrNull("Name"));
			break;
		default:
			break;

		}

	}

	// 判断某个字段是否在返回的json object中
	// added by hesong 20101226
	public boolean isFiledExist(JSONObjectProxy jsonObject, String fieldName) {

		return ((jsonObject.toString().contains(fieldName)) ? true : false);
	}

	// public boolean isFiledListExist(JSONObjectProxy jsonObject,LinkedList
	// fieldNameList)
	// {
	//
	// return ((jsonObject.toString().contains(fieldName))? true : false);
	// }

	public static ArrayList<Product> toList(JSONArrayPoxy jsonArray, int functionId) {
		return toList(jsonArray, functionId, null);
	}

	public static ArrayList<Product> toList(JSONArrayPoxy jsonArray, int functionId, Object[] varargs) {

		if (null == jsonArray) {
			return null;
		}

		ArrayList<Product> list = null;

		try {

			list = new ArrayList<Product>();
			for (int i = 0; i < jsonArray.length(); i++) {
				Product ware = new Product(jsonArray.getJSONObject(i), functionId, varargs);
				list.add(ware);
			}

		} catch (JSONException e) {
			MZLog.d("Ware", e.getMessage());
		}

		return list;
	}

	private Long id;
	private Long showId;
	private List<Image> imageList = new LinkedList<Image>();// 相关图片
	private String name;// 名称
	private String adWord;// 广告词
	private String marketPrice;// 市场价
	private String jdPrice;// 京东价
	private String discount;// 折扣
	private String imgPrice;// 京东价，图片形式
	private Boolean isBook;// 是否图书
	private Boolean isPromotion;// 是否含有赠品
	private Integer num;// 数量
	private String provinceName;// 地区Name
	private String provinceID;// 地区ID
	private Boolean provinceStockFlag;// 库存标识
	private String provinceStockContent;// 库存内容（服务器返回的原始数据）
	//private Integer provinceStockCode;// 库存代号（用于内部识别和处理）

	private Integer provinceStockMode = 0;// 是否分区库存（地区库存模式）

	private ArrayList<ProvinceMode1> provinceMode1List;// 省市（分区库存模式）
	private HashMap<Integer, Integer> provinceMode1Map;// 辅助以ID确定index
	private HashMap<Long, CityMode1> cityMode1Map;// 辅助以sku id确定city id和province id

	private Integer provinceIdMode1;// 本商品最终选择的省份（分区库存模式）
	private Integer cityIdMode1;// 本商品最终选择的城市（分区库存模式）

	private String userPriceLabel;// 会员价Label
	private String userPriceContent;// 会员价Content

	private ArrayList<Product> giftList;
	private ArrayList<Coupon> couponList;

	// added by hesong
	private String sUserName;// 用户名
	private String sUserClass;// 用户等级
	private String sUerScore;// 用户积分
	private String sUserBalance;// 用户余额

	//private String sSkuName;// 购物车中商品名
	//private String sSkuID;
	//private String sPriceShow;
	private int nItemCount;
	//private String sItemPrice;
	private String fid;

	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	private String sOrderId, sTotalPrice, sOrderStatus, sSubmitTime, mPaymentType;
	private boolean subOrderFlag;
	private String sMsgFlag, sMsgType, sMsgBody, sMsgTime, sMsgUpdateTime, sMsgId, msgTypeName;

	public String getMsgTypeName() {
		return msgTypeName;
	}

	public void setMsgTypeName(String msgTypeName) {
		this.msgTypeName = msgTypeName;
	}

	public String getmPaymentType() {
		return mPaymentType;
	}

	public void setmPaymentType(String mPaymentType) {
		this.mPaymentType = mPaymentType;
	}

	public String getsMsgId() {
		return sMsgId;
	}

	public String getJdDixcount() {
		Float f = null;
		try {
			if (null != discount) {
				f = Float.valueOf(discount);
			}
		} catch (NumberFormatException e) {
		}
		return null == f || f < 0f ? "暂无折扣" : new DecimalFormat("0.00").format(f);
	}

	public void setJdDixcount(String discount) {
		this.discount = discount;
	}

	public void setsMsgId(String sMsgId) {
		this.sMsgId = sMsgId;
	}

	public String getsMsgUpdateTime() {
		return sMsgUpdateTime;
	}

	public void setsMsgUpdateTime(String sMsgUpdateTime) {
		this.sMsgUpdateTime = sMsgUpdateTime;
	}

	private List<String> sImgUrlList = new LinkedList<String>();

	public void setItemCount(int count) {
		this.nItemCount = count;
	}

	public int geItemCount() {
		if (this.nItemCount <= 0) {
			return Integer.valueOf("1");
		} else {
			return this.nItemCount;
		}
	}

	public void setMessageTime(String msgTime) {
		this.sMsgTime = msgTime;
	}

	public String getMsgTime() {
		if (this.sMsgTime.length() <= 0) {
			return String.valueOf(" ");
		} else {
			return this.sMsgTime;
		}
	}

	public void setMessageBody(String msgBody) {
		this.sMsgBody = msgBody;
	}

	public String getMsgBody() {
		if (this.sMsgBody.length() <= 0) {
			return String.valueOf(" ");
		} else {
			return this.sMsgBody;
		}
	}

	public void setMessageType(String stype) {
		this.sMsgType = stype;
	}

	public String getMsgType() {
		if (this.sMsgType.length() <= 0) {
			return String.valueOf(" ");
		} else {
			return this.sMsgType;
		}
	}

	public void setMessageFlag(String flag) {
		this.sMsgFlag = flag;
	}

	public String getMsgFlag() {
		if (this.sMsgFlag.length() <= 0) {
			return String.valueOf(" ");
		} else {
			return this.sMsgFlag;
		}
	}

	public void setOrderSubtime(String orderSubtime) {
		this.sSubmitTime = orderSubtime;
	}

	public String getOrderSubtime() {
		if (this.sSubmitTime.length() <= 0) {
			return String.valueOf(" ");
		} else {
			return this.sSubmitTime;
		}
	}

	public void setOrderPrice(String orderPrice) {
		this.sTotalPrice = orderPrice;
	}

	public String getOrderPrice() {
		if (this.sTotalPrice.length() <= 0) {
			return String.valueOf(" ");
		} else {
			return this.sTotalPrice;
		}
	}

	public void setOrderStatus(String orderStatus) {
		MZLog.d("product","set++++++" + orderStatus);
		this.sOrderStatus = orderStatus;
	}

	public String getOrderStatus() {
		MZLog.d("product","get++++++" + sOrderStatus);
		if (this.sOrderStatus.length() <= 0) {
			return String.valueOf(" ");
		} else {
			return this.sOrderStatus;
		}
	}

	public void setOrderId(String orderId) {
		this.sOrderId = orderId;
	}

	public String getOrderId() {
		if (this.sOrderId.length() <= 0) {
			return String.valueOf(" ");
		} else {
			return this.sOrderId;
		}
	}

	public void setUsername(String name) {
		this.sUserName = name;
	}

	public String getUsername() {
		if (this.sUserName.length() <= 0) {
			return String.valueOf("Customer");
		} else {
			return this.sUserName;
		}
	}

	public void setUserScore(String score) {
		this.sUerScore = score;
	}

	public String getUserScore() {
		return this.sUerScore.length() > 0 ? this.sUerScore : "0";
	}

	public void setUserClass(String userClass) {
		this.sUserClass = userClass;
	}

	public String getUserClass() {
		return this.sUserClass == null ? "" : this.sUserClass;
	}

	public void setUserBalance(String userBalance) {
		this.sUserBalance = userBalance;
	}

	public String getUserBalance() {
		return this.sUserBalance == null ? "" : this.sUserBalance;
	}

	public Image getImage() {
		if (imageList.size() > 0)
			return imageList.get(0);
		else
			return null;
	}

	public void setImage(String small, String big) {
		this.imageList.add(new Image(small, big));
	}

	public void appendImgUrl(String url, int index) {
		if (index < 0) {
			sImgUrlList.add(url.toString());
		} else {
			sImgUrlList.add(index, url.toString());
		}

	}

	public String popImgUrl(int index) {
		if (index > -1 && sImgUrlList.size() > 0 && index < sImgUrlList.size()) {
			return sImgUrlList.get(index).toString();
		} else {
			return null;
		}
	}

	/**
	 * @return 默认缩略图
	 */
	public String getImageUrl() {
		if (imageList.size() > 0)
			return imageList.get(0).getSmall();
		else
			return null;
	}

	public String getJdPrice() {
		Float f = null;
		try {
			if (null != jdPrice) {
				f = Float.valueOf(jdPrice);
			}
		} catch (NumberFormatException e) {
		}
		return null == f || f <= 0f ? "暂无报价" : new DecimalFormat("0.00").format(f);
	}

	public void setJdPrice(String jdPrice) {
		this.jdPrice = jdPrice;
	}

	public String getAdWord() {
		return adWord == null ? "" : adWord;
	}

	public void setAdWord(String adword) {
		this.adWord = adword;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long wareId) {
		this.id = wareId;
	}

	public String getName() {
		return null != name ? name : "暂无名称";
	}

	public void setName(String name) {

		if (null == name) {
			this.name = name;
			return;
		}

		try {
			Pattern pattern = Pattern.compile("([^a-zA-Z0-9（）\\(\\) ])([a-zA-Z（\\(])|" + // 中文后面跟英文或者括号
					"([^ ])([（\\(])|" + // 括号前加空格
					"([（\\(])([^ ])|" + // 括号后加空格
					"([A-Z0-9])(\\-)|" + // 型号后面跟-
					"(\\-)([A-Z0-9])|" + // -后面跟型号
					"([0-9]*[A-Z]+[0-9]*)([^a-zA-Z0-9（）\\(\\) ])");// 型号后面跟中文
			Matcher matcher = pattern.matcher(name);

			StringBuffer stringBuffer = new StringBuffer();

			while (matcher.find()) {

				StringBuffer sb = new StringBuffer();

				for (int i = 1; i <= matcher.groupCount(); i++) {
					if (matcher.group(i) != null) {
						// 替换
						sb.append(matcher.group(i)).append(" ").append(matcher.group(i + 1));
						break;
					}
				}

				MZLog.d("Temp", "name -->> " + name);
					MZLog.d("Temp", "stringBuffer.toString() -->> " + stringBuffer.toString());
					MZLog.d("Temp", "sb.toString() -->> " + sb.toString());
				matcher.appendReplacement(stringBuffer, sb.toString());
			}
			matcher.appendTail(stringBuffer);
			stringBuffer.append(" ");
			String result = stringBuffer.toString();
			this.name = result;
		} catch (Exception e) {
			e.printStackTrace();
			this.name = name;
		}
	}

	public Boolean isBook() {
		return null != isBook ? isBook : false;
	}

	public void setBook(Boolean isBook) {
		this.isBook = isBook;
	}

	public String getMarketPrice() {
		Float f = null;
		try {
			if (null != marketPrice) {
				f = Float.valueOf(marketPrice);
			}
		} catch (NumberFormatException e) {
		}
		return null == f || f <= 0f ? "暂无报价" : new DecimalFormat("0.00").format(f);
	}

	public void setMarketPrice(String marketPrice) {
		this.marketPrice = marketPrice;
	}

	public Boolean isPromotion() {
		return null != isPromotion ? isPromotion : false;
	}

	public void setPromotion(Boolean isPromotion) {
		this.isPromotion = isPromotion;
	}

	public String getImgPrice() {
		return imgPrice;
	}

	public void setImgPrice(String imgPrice) {
		this.imgPrice = imgPrice;
	}

	public List<Image> getImageList() {
		return imageList;
	}

	public void setImageList(List<Image> imagesList) {
		this.imageList = imagesList;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public ArrayList<Product> getGiftList() {
		return giftList;
	}

	public void setGiftList(ArrayList<Product> giftList) {
		this.giftList = giftList;
	}

	public ArrayList<Coupon> getCouponList() {
		return couponList;
	}

	public void setCouponList(ArrayList<Coupon> couponList) {
		this.couponList = couponList;
	}

	public String getUserPriceLabel() {
		return userPriceLabel;
	}

	public void setUserPriceLabel(String userPriceLabel) {
		this.userPriceLabel = userPriceLabel;
	}

	public String getUserPriceContent() {
		return userPriceContent;
	}

	public void setUserPriceContent(String userPriceContent) {
		this.userPriceContent = userPriceContent;
	}

	public void setSubOrderFlag(boolean subOrderFlag) {
		this.subOrderFlag = subOrderFlag;
	}

	public boolean getSubOrderFlag() {
		return subOrderFlag;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getProvinceID() {
		return provinceID;
	}

	public void setProvinceID(String provinceID) {
		this.provinceID = provinceID;
	}

	public String getProvinceStockContent() {
		return provinceStockContent;
	}

	public void setProvinceStockContent(String provinceStockContent) {
		this.provinceStockContent = provinceStockContent;
	}

	public Boolean getProvinceStockFlag() {
		return provinceStockFlag;
	}

	public void setProvinceStockFlag(Boolean provinceStockFlag) {
		this.provinceStockFlag = provinceStockFlag;
	}

	public Integer getProvinceStockMode() {
		return provinceStockMode;
	}

	public void setProvinceStockMode(Integer provinceStockMode) {
		this.provinceStockMode = provinceStockMode;
	}

	public ArrayList<ProvinceMode1> getProvinceMode1List() {
		return provinceMode1List;
	}

	public void setProvinceMode1List(ArrayList<ProvinceMode1> provinceMode1List) {
		this.provinceMode1List = provinceMode1List;
		// 使用HashMap，方便以ID确定index
		provinceMode1Map = new HashMap<Integer, Integer>();
		for (int i = 0; i < provinceMode1List.size(); i++) {
			ProvinceMode1 province = provinceMode1List.get(i);
			provinceMode1Map.put(province.getId(), i);
		}
	}

	/**
	 * 辅助以ID确定index
	 */
	public Integer getProvinceMode1IndexById(int id) {
		return provinceMode1Map.get(id);
	}

	public Integer getProvinceIdMode1() {
		return provinceIdMode1;
	}

	public void setProvinceIdMode1(Integer provinceIdMode1) {
		this.provinceIdMode1 = provinceIdMode1;
	}

	public Integer getCityIdMode1() {
		return cityIdMode1;
	}

	public void setCityIdMode1(Integer cityIdMode1) {
		this.cityIdMode1 = cityIdMode1;
	}

	/**
	 * 仅包内可用，用于city遍历时把对象添加到此以助管理
	 */
	protected void putInCityMode1Map(Long skuId, CityMode1 city) {
		if (null == cityMode1Map) {
			cityMode1Map = new HashMap<Long, CityMode1>();
		}
		if (!cityMode1Map.containsKey(skuId)) {
			cityMode1Map.put(skuId, city);
		}
	}

	/**
	 * 辅助以sku id确定city id和province id
	 */
	public CityMode1 getCityMode1BySkuId(Long skuId) {
		return cityMode1Map.get(skuId);
	}

	public Long getShowId() {
		if(null == showId){
			return getId();
		}
		return showId;
	}

	public void setShowId(Long showId) {
		this.showId = showId;
	}

}
