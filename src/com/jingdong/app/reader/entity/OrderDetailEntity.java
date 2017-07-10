package com.jingdong.app.reader.entity;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class OrderDetailEntity {

	/*
	 *订单详情
	 *	返回参数格式
	返回参数格式
		{"code":"","orderId":"","wares":[{"wareId":"","wareCount":"","wareName":"",
		      "warePicUrl":""}],"payMethod":"","wareMoney":"","freightMoney":"",
		      "discountMoney":"","readFreight":"","readPayMoney":"","consignee":"",
		      "consigneeAddress":"","consigneePhone":"","consigneeEmail":"",
		      "defrayMethod":"","defrayAddress":"","invocieType":"","invoiceStartName":"",
		      "invoiceContent":""}

					说明：
					参数名称	是否必填	描述	示例
					orderId	Y	订单编号	8382913
					wareId	Y	商品ID

					wareCount	Y	商品数量
					wareName	Y	商品名称
					warePicUrl		商品图片地址
					payMethod	Y	付款方式
					上门自提

					wareMoney	Y	商品金额
					freightMoney	Y	运费金额
					discountMoney	Y	优惠金额
					readFreight	Y	实际运费
					readPayMoney	Y	应支付金额
					consignee	Y	收货人
					consigneeAddress		收货人地址
					consigneePhone		收货人手机
					consigneeEmail		收货人邮件
					defrayMethod		支付方式
					1，
					2，
					defrayAddress		支付地址
					invocieType		发票类型
					1，
					2，
					invoiceStartName		发票抬头
					invoiceContent		发票内容
	 */
	private final static String KEY_WARES_LIST = "wares";

	public final  String KEY_ORDERID = "orderId";



	public final  String KEY_PAYMETHOD = "payMethod";
	public final  String KEY_WAREMONEY = "wareMoney";
	public final  String KEY_FREIGHTMONEY = "freightMoney";
	public final  String KEY_DISCOUNTMONEY = "discountMoney";
	public final  String KEY_READFREIGHT= "readFreight";
	public final  String KEY_READPAYMONEY = "readPayMoney";
	public final  String KEY_CONSIGNEE = "consignee";
	public final  String KEY_CONSIGNEEADDRESS = "consigneeAddress";
	public final  String KEY_CONSIGNEEPHONE = "consigneePhone";
	public final  String KEY_CONSIGNEEEMAIL = "consigneeEmail";
	public final  String KEY_DEFRAYMETHOD = "defrayMethod";
	public final  String KEY_DEFRAYADDRESS = "defrayAddress";
	public final  String KEY_INVOCIETYPE = "invocieType";
	public final  String KEY_INVOICESTARTNAME = "invoiceStartName";
	public final  String KEY_INVOICECONTENT = "invoiceContent";


	public String orderId;



	public int payMethod;
	public String wareMoney;
	public String freightMoney;
	public String discountMoney;
	public String readFreight;
	public String readPayMoney;
	public String consignee;
	public String consigneeAddress;
	public String consigneePhone;
	public String consigneeEmail;
	public int defrayMethod;
	public String defrayAddress;
	public int invocieType;
	public String invoiceStartName;
	public String invoiceContent;

	public static List<wares> list_waresList = new ArrayList<wares>();

	public static class wares{
		public final  String KEY_WAREID = "wareId";
		public final  String KEY_WARECOUNT = "wareCount";
		public final  String KEY_WARENAME = "wareName";
		public final  String KEY_WAREPICURL = "warePicUrl";


		public String wareId;
		public int wareCount;
		public String wareName;
		public String warePicUrl;

	}



	public static final OrderDetailEntity parse(JSONObject jsonObject) {

		OrderDetailEntity objEntity = null;
		if (jsonObject != null) {
			try {
				objEntity = new OrderDetailEntity();

				objEntity.orderId =  DataParser.getString(jsonObject, objEntity.KEY_ORDERID);
				objEntity.payMethod =  DataParser.getInt(jsonObject, objEntity.KEY_PAYMETHOD);
				objEntity.wareMoney =  DataParser.getString(jsonObject, objEntity.KEY_WAREMONEY);
				objEntity.freightMoney =  DataParser.getString(jsonObject, objEntity.KEY_FREIGHTMONEY);
				objEntity.discountMoney =  DataParser.getString(jsonObject, objEntity.KEY_DISCOUNTMONEY);
				objEntity.readFreight =  DataParser.getString(jsonObject, objEntity.KEY_READFREIGHT);
				objEntity.readPayMoney =  DataParser.getString(jsonObject, objEntity.KEY_READPAYMONEY);
				objEntity.consignee =  DataParser.getString(jsonObject, objEntity.KEY_CONSIGNEE);
				objEntity.consigneeAddress =  DataParser.getString(jsonObject, objEntity.KEY_CONSIGNEEADDRESS);
				objEntity.consigneePhone =  DataParser.getString(jsonObject, objEntity.KEY_CONSIGNEEPHONE);
				objEntity.consigneeEmail =  DataParser.getString(jsonObject, objEntity.KEY_CONSIGNEEEMAIL);
				objEntity.defrayMethod =  DataParser.getInt(jsonObject, objEntity.KEY_DEFRAYMETHOD);
				objEntity.defrayAddress =  DataParser.getString(jsonObject, objEntity.KEY_DEFRAYADDRESS);
				objEntity.invocieType =  DataParser.getInt(jsonObject, objEntity.KEY_INVOCIETYPE);
				objEntity.invoiceStartName =  DataParser.getString(jsonObject, objEntity.KEY_INVOICESTARTNAME);
				objEntity.invoiceContent =  DataParser.getString(jsonObject, objEntity.KEY_INVOICECONTENT);
				JSONArray jsonList_track = jsonObject.getJSONArray(KEY_WARES_LIST);



				if(jsonList_track!=null&&jsonList_track.length()!=0){
					int length = 0;
					if (jsonList_track != null && (length = jsonList_track.length()) > 0) {
						JSONObject jsonObj = null;
						//Object object;
						for (int index = 0; index < length; index++) {
							jsonObj = jsonList_track.getJSONObject(index);
							if (jsonObj != null) {
								wares obj = new wares();
								obj.wareId = DataParser.getString(jsonObj, obj.KEY_WAREID);
								obj.wareCount = DataParser.getInt(jsonObj, obj.KEY_WARECOUNT);
								obj.wareName = DataParser.getString(jsonObj, obj.KEY_WARENAME);
								obj.warePicUrl = DataParser.getString(jsonObj, obj.KEY_WAREPICURL);
								if (obj != null) {
									list_waresList.add(obj);
								}
							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();

				e.printStackTrace();
			}
		}

		return objEntity;
	}
}
