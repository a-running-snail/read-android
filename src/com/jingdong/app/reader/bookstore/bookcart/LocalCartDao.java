package com.jingdong.app.reader.bookstore.bookcart;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;

import com.jingdong.app.reader.entity.extra.SimplifiedDetail;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;

/**
 * 
 * @ClassName: LocalCartDao
 * @Description: 本地SP购物车CRUD工具类
 * @author J.Beyond
 * @date 2015年4月24日 下午5:27:02
 *
 */
public class LocalCartDao {

	/**
	 * 
	 * @Title: getLocalBookInfosInSP
	 * @Description: 查询本地SP中所有的商品信息，封装为一个SimplifiedDetail的集合返回
	 * @param @param ctx
	 * @param @return
	 * @return List<SimplifiedDetail>
	 * @throws
	 * @date 2015年4月24日 下午3:17:34
	 */
	public static List<SimplifiedDetail> getLocalBookInfosInSP(Context ctx) {
		try {
			//获取购物车信息
			List<SimplifiedDetail> bookCart = new ArrayList<SimplifiedDetail>();
			String bookCartJson = LocalUserSetting.getBookCartInfos(ctx);
			MZLog.d("J", "LocalCartDao-->购物车信息::"+bookCartJson);
			if (bookCartJson.equals("{}")) {
				return null;
			}
			
			JSONArray array = new JSONArray(bookCartJson);
			if (array != null && array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					bookCart.add(GsonUtils.fromJson(array.getString(i),
							SimplifiedDetail.class));
				}
			} else {
				MZLog.d("J", "bookcart is empty!");
			}
			
			return bookCart;
			
		} catch (Exception e) {
			MZLog.e("J", e.getMessage());
			return null;
		}
	}
	
	/**
	 * 
	 * @Title: saveBookToLocal
	 * @Description: 将一本书保存到本地
	 * @param @param ctx
	 * @param @param detail
	 * @param @return
	 * @return boolean
	 * @throws
	 * @date 2015年4月24日 下午3:18:30
	 */
	public static boolean addToLocal(Context ctx,SimplifiedDetail detail) {
		List<SimplifiedDetail> localBookInfos = getLocalBookInfosInSP(ctx);
		if (localBookInfos == null) {
			localBookInfos = new ArrayList<SimplifiedDetail>();
		}
		localBookInfos.add(0, detail);
		String bookCartJsonFinal = GsonUtils.toJson(localBookInfos);
		return LocalUserSetting.saveToBookCart(ctx, bookCartJsonFinal);
	}
	
	/**
	 * 
	 * @Title: delFromLocal
	 * @Description: 从本地购物车删除一组商品
	 * @param @param ctx
	 * @param @param bookids
	 * @param @return
	 * @return boolean
	 * @throws
	 * @date 2015年4月24日 下午3:24:46
	 */
	public static boolean delFromLocal(Context ctx,String[] bookids) {
		List<SimplifiedDetail> localBookInfos = getLocalBookInfosInSP(ctx);
		if (localBookInfos == null) {
			return false;
		}
		List<SimplifiedDetail> temp = new ArrayList<SimplifiedDetail>();
		for (SimplifiedDetail sd : localBookInfos) {
			for (String bookid : bookids) {
				if (sd.bookId == Long.parseLong(bookid)) {
					temp.add(sd);
				}
			}
		}
		localBookInfos.removeAll(temp);
		//保存更新
		String bookCartJsonFinal = GsonUtils.toJson(localBookInfos);
		return LocalUserSetting.saveToBookCart(ctx, bookCartJsonFinal);
	}
}
