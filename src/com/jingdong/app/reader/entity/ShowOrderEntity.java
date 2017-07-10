package com.jingdong.app.reader.entity;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class ShowOrderEntity {

	/*
	 *查看订单
	 *	返回参数格式
		{"code":"","orderId":"","orderStatus":"","trackList":[{"operateTime":"","operationContent":""}],"totalPrice":"","orderTime":"","bookList":[{"bookId":"","picUrl":""}]}
			说明：
			参数名称	是否必填	描述	示例
			orderId	Y	订单编号	8382913
			orderStatus		订单状态
			未支付
			正在出库
			完成
			totalPrice		总价
			orderTime		下单时间
			operateTime		操作订单时间
			operationContent		操作订单内容
			bookId		订单内书籍ID
			picUrl		订单内书籍图片URL
	 */
	private final static String KEY_TRACKLIST = "trackList";
	private final static String KEY_BOOKLIST = "bookList";

	public final  String KEY_ORDERID = "orderId";
	public final  String KEY_ORDERSTATUS = "orderStatus";


	public final  String KEY_TOTALPRICE = "totalPrice";
	public final  String KEY_ORDERTIME = "orderTime";


	public String orderId;
	public int orderStatus;


	public String totalPrice;
	public String orderTime;

	public static List<trackList> list_trackList = new ArrayList<trackList>();
	public static List<bookList> list_bookList = new ArrayList<bookList>();

	public static class trackList{
		public final  String key_OPERATETIME = "operateTime";
		public final  String KEY_OPERATIONCONTENT = "operationContent";


		public String operateTime;
		public int operationContent;

	}
	public static  class bookList{
		public final  String KEY_BOOKID = "bookId";
		public final  String KEY_PICURL = "picUrl";


		public String bookId;
		public String picUrl;
	}


	public static final ShowOrderEntity parse(JSONObject jsonObject) {

		ShowOrderEntity objEntity = null;
		if (jsonObject != null) {
			try {
				objEntity = new ShowOrderEntity();

				objEntity.orderId =  DataParser.getString(jsonObject, objEntity.KEY_ORDERID);
				objEntity.orderStatus =  DataParser.getInt(jsonObject, objEntity.KEY_ORDERSTATUS);
				objEntity.totalPrice =  DataParser.getString(jsonObject, objEntity.KEY_TOTALPRICE);
				objEntity.orderTime =  DataParser.getString(jsonObject, objEntity.KEY_ORDERTIME);
				JSONArray jsonList_book = jsonObject.getJSONArray(KEY_BOOKLIST);
				JSONArray jsonList_track = jsonObject.getJSONArray(KEY_TRACKLIST);
				if(jsonList_book!=null&&jsonList_book.length()!=0){
					int length = 0;
					if (jsonList_book != null && (length = jsonList_book.length()) > 0) {
						JSONObject jsonObj = null;
						//Object object;
						for (int index = 0; index < length; index++) {
							jsonObj = jsonList_book.getJSONObject(index);
							if (jsonObj != null) {
								bookList obj = new bookList();
								obj.bookId = DataParser.getString(jsonObj, obj.KEY_BOOKID);
								obj.picUrl = DataParser.getString(jsonObj, obj.KEY_PICURL);
								if (obj != null) {
									list_bookList.add(obj);
								}
							}
						}
					}
				}

				if(jsonList_track!=null&&jsonList_track.length()!=0){
					int length = 0;
					if (jsonList_track != null && (length = jsonList_track.length()) > 0) {
						JSONObject jsonObj = null;
						//Object object;
						for (int index = 0; index < length; index++) {
							jsonObj = jsonList_track.getJSONObject(index);
							if (jsonObj != null) {
								trackList obj = new trackList();
								obj.operateTime = DataParser.getString(jsonObj, obj.key_OPERATETIME);
								obj.operationContent = DataParser.getInt(jsonObj, obj.KEY_OPERATIONCONTENT);
								if (obj != null) {
									list_trackList.add(obj);
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
