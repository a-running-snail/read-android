package com.jingdong.app.reader.entity.extra;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.widget.Toast;

import com.jingdong.app.reader.activity.MyActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;

/**
 * 书城购物车
 * 
 * @author WANGGUODONG
 * 
 */
public class JDBookCart {

	private List<SimplifiedDetail> bookCart = null;
	private static JDBookCart instance = null;
	private boolean isNeedChangeKeyInSP = false;

	public static JDBookCart getInstance(Context context) {

		if (instance == null) {
			instance = new JDBookCart(context);
		}
		return instance;
	}

	private JDBookCart(Context context) {
		bookCart = new ArrayList<SimplifiedDetail>();
	}

	public List<SimplifiedDetail> getBookCartList(Context context) {

		 getBookCart(context);
		
		return bookCart;
	}

	// 获得购物车书籍数目
	public int getBookCartCount(Context context) {
		if (bookCart != null) {
			// return bookCart.size();
			return getBookCartList(context).size();
		}
		return 0;

	}

	public boolean isContained(long bookid) {

		if (bookCart != null && bookCart.size() > 0) {
			for (SimplifiedDetail detail : bookCart) {
				if (detail.bookId == bookid)
					return true;
			}
		}

		return false;
	}

	// 添加一个商品
	public void addToBookCart(Context context, SimplifiedDetail book) {

		if (bookCart != null && book != null) {

			if (!isContained(book.bookId)) {
				bookCart.add(book);
				saveBookCart(context);
				Toast.makeText(context, "书籍已经添加到购物车了", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(context, "购物车里已经存在这本书了!", Toast.LENGTH_SHORT)
						.show();
			}

		}

	}

	/**
	 * 清空购物车
	 * 
	 * @param context
	 */
	public void clearBookCart(Context context) {
		if (bookCart == null || bookCart.size() == 0)
			return;
		bookCart.clear();
		saveBookCart(context);
	}

	/**
	 * 
	 * @Title: clearBookCart
	 * @Description: 删除指定bookId数组的书
	 * @param @param mActivity
	 * @param @param bookIds
	 * @return void
	 * @throws
	 * @date 2015年4月2日 下午5:46:57
	 */
	public void clearBookCart(MyActivity mActivity, String[] bookIds) {
		String bookCartJson = LocalUserSetting.getBookCart(mActivity,
				LoginUser.getpin());
		MZLog.d("JD_Reader", "result:" + bookCartJson);

		try {
			// 1.重新获取购物车
			bookCart.clear();
			JSONArray array = new JSONArray(bookCartJson);
			if (array != null && array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					bookCart.add(GsonUtils.fromJson(array.getString(i),
							SimplifiedDetail.class));
				}

			} else {
				MZLog.d("wangguodong", "bookcart is empty!");
			}
			// 2.删除bookIds
			for (int i = 0; i < bookIds.length; i++) {
				deleteFromBookCart(mActivity, Long.valueOf(bookIds[i]));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 删除一个商品
	public void deleteFromBookCart(Context context, long bookid) {

		if (bookCart == null || bookCart.size() == 0)
			return;

		int position = -1;
		for (int i = 0; i < bookCart.size(); i++) {

			if (bookCart.get(i).bookId == bookid) {
				position = i;
				break;
			}
		}
		if (position != -1) {
			bookCart.remove(position);
			saveBookCart(context);
		}

	}

	private void saveBookCart(Context context) {
		String userPin = "";
		if (LoginUser.isLogin()) {
			userPin = LoginUser.getpin();
		}
		if (bookCart != null) {
			if (bookCart.size() == 0) {
				LocalUserSetting.saveBookCart(context, userPin, "{}");
			} else {
				String bookCartJson = GsonUtils.toJson(bookCart);
				LocalUserSetting.saveBookCart(context, userPin, bookCartJson);
			}
		}
	}

	private void getBookCart(Context context) {

		try {
			// clear bookcart
			bookCart.clear();
			// get data again
			String userPin = "";
			if (LoginUser.isLogin()) {
				userPin = LoginUser.getpin();
			}
			String bookCartJson = LocalUserSetting
					.getBookCart(context, userPin);
			// 说明之前添加到购物车时未登录
			if (bookCartJson.equals("{}")) {
				bookCartJson = LocalUserSetting.getBookCart(context, "");
				isNeedChangeKeyInSP = true;
			}
			MZLog.d("wangguodong", "result:" + bookCartJson);

			JSONArray array = new JSONArray(bookCartJson);
			if (array != null && array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					bookCart.add(GsonUtils.fromJson(array.getString(i),
							SimplifiedDetail.class));
				}
				if (isNeedChangeKeyInSP) {
					// 替换sp中的key
					LocalUserSetting.saveBookCart(context, userPin,
							bookCartJson);
					LocalUserSetting.removeNullPinBookCart(context);
					isNeedChangeKeyInSP = false;
				}

			} else {
				MZLog.d("wangguodong", "bookcart is empty!");
			}
		} catch (Exception e) {
			MZLog.d("wangguodong", "get bookcart error!");
		}
	}

	public void resetBookCart(String userPin) {

	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void add2ShoppingCart(Context context, SimplifiedDetail book) {
		//1.已登录，同步到server
		if (LoginUser.isLogin()) {
			
		}
	}

}
