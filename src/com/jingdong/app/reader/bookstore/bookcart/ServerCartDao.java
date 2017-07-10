package com.jingdong.app.reader.bookstore.bookcart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.jingdong.app.reader.entity.BookCardItemEntity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;

/**
 * 
 * @ClassName: ServerCartDao
 * @Description: 服务器购物车CRUD工具类
 * @author J.Beyond
 * @date 2015年4月24日 下午5:28:21
 *
 */
public class ServerCartDao {
	private static final String CART_DAO_TYPE_ADD = "2";
	private static final String CART_DAO_TYPE_DEL = "3";

	public interface GetBookIdsListener{
		void onGetBookIdsStart();
		void onGetBookIdsSuccess(String[] bookIdArr);
		void onGetBookIdsFail();
	}
	
	public interface GetBookCartItemsListener{
		void onGetBookCartItemsSuccess(BookCardItemEntity cardItemEntity);
		void onGetBookCartItemsFail();
	}

	
	/**
	 * 
	 * @Title: getBookIds
	 * @Description: 查询服务端购物车的BookIds
	 * @param @param ctx
	 * @param @param bookIdsListener
	 * @return void
	 * @throws
	 * @date 2015年4月24日 下午1:47:33
	 */
	public static void getBookIds(Context ctx,final GetBookIdsListener bookIdsListener) {
		
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getShopingCartParams("","1"), true,
				new MyAsyncHttpResponseHandler(ctx) {
					@Override
					public void onStart() {
						super.onStart();
						if (bookIdsListener != null) {
							bookIdsListener.onGetBookIdsStart();
						}
					}

					@Override
					public void onFailure(int arg0, Header[] arg1,
							byte[] arg2, Throwable arg3) {
						if (bookIdsListener != null) {
							bookIdsListener.onGetBookIdsFail();
						}
					}

					@Override
					public void onResponse(int statusCode,
							Header[] headers, byte[] responseBody) {

						try {
							String result = new String(responseBody);
							MZLog.d("J", "shoppingCart result::"+result);
							JSONObject object = new JSONObject(result);
							JSONObject resultObj = object.optJSONObject("result");
							if (resultObj == null) {
								if (bookIdsListener != null) {
									bookIdsListener.onGetBookIdsFail();
								}
								return;
							}
							String[] bookIdArr= null;
							String bookList = resultObj.getString("bookList");
							if (TextUtils.isEmpty(bookList)) {
								if (bookIdsListener != null) {
									bookIdsListener.onGetBookIdsSuccess(null);
								}
								return;
							}
							MZLog.d("J", "shoppingCart bookList::"+bookList);
							if (bookList.contains(",")) {
								bookIdArr = bookList.split(",");
							}else{
								bookIdArr = new String[]{bookList};
							}
							if (bookIdsListener != null) {
								bookIdsListener.onGetBookIdsSuccess(bookIdArr);
							}
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	}
	
	/**
	 * 
	 * @Title: getBookCartItems
	 * @Description: 通过bookids，获取购物车Item实体
	 * @param @param ctx
	 * @param @param list
	 * @param @param cartItemsListener
	 * @return void
	 * @throws
	 * @date 2015年4月24日 下午2:26:38
	 */
	public static void getBookCartItems(Context ctx,List<Map<String, String>> list,final GetBookCartItemsListener cartItemsListener){
		WebRequestHelper.get(URLText.JD_BOOK_ORDER_URL,
				RequestParamsPool.getBookCartParams(list), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						
						if (cartItemsListener != null) {
							cartItemsListener.onGetBookCartItemsFail();;
						}
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						try {
							String result = new String(responseBody);
							MZLog.d("wangguodong", result);
							JSONObject object = new JSONObject(result);
							int code = object.optInt("code");
							if (code == 0) {
								JSONObject cartResult = object
										.optJSONObject("cartResult");
								
								BookCardItemEntity itemEntity = GsonUtils
										.fromJson(cartResult.toString(),
												BookCardItemEntity.class);
								if (cartItemsListener != null) {
									cartItemsListener.onGetBookCartItemsSuccess(itemEntity);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							MZLog.e("J", e.getMessage());
							if (cartItemsListener != null) {
								cartItemsListener.onGetBookCartItemsFail();
							}
						}
					}

				});
	}
	
	/**
	 * 
	 * @ClassName: AddToCartListener
	 * @Description: 添加到购物车的监听
	 * @author J.Beyond
	 * @date 2015年4月24日 下午2:26:14
	 *
	 */
	public interface AddToCartInnerListener{
		void onAddSuccess();
		void onAddFail();
	}
	
	/**
	 * 
	 * @Title: addToCart
	 * @Description: 添加一组书到购物车
	 * @param @param ctx
	 * @param @param bookId
	 * @param @param addListener
	 * @return void
	 * @throws
	 * @date 2015年4月24日 下午2:03:38
	 */
	public static void addToCart(Context ctx,String[] bookIds,final AddToCartInnerListener addListener){
		if (bookIds == null) {
			return;
		}
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getShopingCartParams(array2String(bookIds), CART_DAO_TYPE_ADD), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						if (addListener != null) {
							addListener.onAddFail();
						}
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						try {
							String result = new String(responseBody);
							MZLog.d("wangguodong", result);
							JSONObject object = new JSONObject(result);
							int code = object.optInt("code");
							if (code == 0) {
								if (addListener != null) {
									addListener.onAddSuccess();
								}
							}else{
								if (addListener != null) {
									addListener.onAddFail();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							MZLog.e("J", e.getMessage());
							if (addListener != null) {
								addListener.onAddFail();
							}
						}
					}

				});
	}
	
	/**
	 * 
	 * @ClassName: 删除的监听
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author J.Beyond
	 * @date 2015年4月24日 下午2:25:54
	 *
	 */
	public interface DelFromCartInnerListener{
		void onDelSuccess(boolean isDelInCart,BookCardItemEntity cardItemEntity);
		void onDelFail();
	}
	
	/**
	 * 
	 * @Title: delFromCart
	 * @Description: 从购物车删除图书
	 * @param @param ctx
	 * @param @param bookids 包含bookid的数组
	 * @param @param isDelInCart 从购物车删除传true，从图书主页删除传false
	 * @param @param delListener
	 * @return void
	 * @throws
	 * @date 2015年4月24日 下午2:23:53
	 */
	public static void delFromCart(final Context ctx,String[] bookids,final boolean isDelInCart,final DelFromCartInnerListener delListener) {
		if (bookids == null) {
			return;
		}
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getShopingCartParams(array2String(bookids), CART_DAO_TYPE_DEL), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						
						if (delListener != null) {
							delListener.onDelFail();;
						}
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						try {
							String result = new String(responseBody);
							MZLog.d("wangguodong", result);
							JSONObject object = new JSONObject(result);
							int code = object.optInt("code");
							if (code == 0) {
								JSONObject resultObj = object.optJSONObject("result");
								String bookList = resultObj.getString("bookList");
								MZLog.d("J", "shoppingCart bookList::"+bookList);
								
								//如果是从购物车删除，需要返回购物车中剩余的商品
								if (isDelInCart) {
									if (TextUtils.isEmpty(bookList)) {
										if (delListener != null) {
											delListener.onDelSuccess(isDelInCart, null);
										}
									}
									//拼接参数
									List<Map<String, String>> list = new ArrayList<Map<String, String>>();
									if (bookList.contains(",")) {
										String[] bookIdArr = bookList.split(",");
										for (String bookId : bookIdArr) {
											Map<String, String> map = new HashMap<String, String>();
											map.put("Id", bookId + "");
											map.put("num", "1");
											list.add(map);
										}
									}else{
										Map<String, String> map = new HashMap<String, String>();
										map.put("Id", bookList + "");
										map.put("num", "1");
										list.add(map);
									}
									
									getBookCartItems(ctx, list, new GetBookCartItemsListener() {
										
										@Override
										public void onGetBookCartItemsSuccess(BookCardItemEntity cardItemEntity) {
											if (delListener != null) {
												delListener.onDelSuccess(isDelInCart, cardItemEntity);
											}
										}
										
										@Override
										public void onGetBookCartItemsFail() {
											// TODO Auto-generated method stub
											if (delListener != null) {
												delListener.onDelFail();
											}
										}
									});
									
								}else{
									delListener.onDelSuccess(isDelInCart, null);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});
	}
	
	
	
	public static String array2String(String[] arr){
		
		StringBuilder sb = new StringBuilder();
		for (String bookid : arr) {
			sb.append(bookid+",");
		}
		return sb.deleteCharAt(sb.length()-1).toString();
	}
	
	
	
}
