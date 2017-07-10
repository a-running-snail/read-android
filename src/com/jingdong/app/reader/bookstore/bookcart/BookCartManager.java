package com.jingdong.app.reader.bookstore.bookcart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.bookstore.bookcart.ServerCartDao.AddToCartInnerListener;
import com.jingdong.app.reader.bookstore.bookcart.ServerCartDao.DelFromCartInnerListener;
import com.jingdong.app.reader.bookstore.bookcart.ServerCartDao.GetBookCartItemsListener;
import com.jingdong.app.reader.bookstore.bookcart.ServerCartDao.GetBookIdsListener;
import com.jingdong.app.reader.entity.BookCardItemEntity;
import com.jingdong.app.reader.entity.extra.SimplifiedDetail;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;

/**
 * 
 * @ClassName: BookCartManager
 * @Description: 购物车管理类
 * @author J.Beyond
 * @date 2015年4月22日 下午6:47:37
 *
 */
public class BookCartManager {

	private static BookCartManager mInstance;
	private BookCartManager() {}

	public static BookCartManager getInstance() {
		if (mInstance == null) {
			mInstance = new BookCartManager();
		}
		return mInstance;
	}
	
	/**
	 * 
	 * @ClassName: GetShoppingCartInfoListener
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author J.Beyond
	 * @date 2015年4月22日 下午5:30:04
	 *
	 */
	public interface GetShoppingCartInfoListener{
		void onStart();
		void onSuccess(boolean isFromLocal,BookCardItemEntity bookCardItemEntity);
		void onFail();
	}
	
	
	/**
	 * 
	 * @ClassName: CheckExistListener
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author J.Beyond
	 * @date 2015年4月23日 下午3:13:42
	 *
	 */
	public interface CheckExistListener{
		void isExist(boolean isExist);
	}
	
	/**
	 * 
	 * @ClassName: GetTotalCountListener
	 * @Description: 获取购物车商品数量监听
	 * @author J.Beyond
	 * @date 2015年4月23日 下午5:30:57
	 *
	 */
	public interface GetTotalCountListener{
		void onResult(int num);
	}
	/**
	 * 
	 * @Title: getShoppingCartInfo
	 * @Description: 向服务端查询
	 * @param @param ctx
	 * @param @param bookCartInfoListener
	 * @return void
	 * @throws
	 * @date 2015年4月22日 下午4:46:20
	 */
	public void getShoppingCartInfos(final Context ctx,final GetShoppingCartInfoListener bookCartInfoListener) {
		/**
		 * 已登录从服务端获取购物车信息 未登录，从本地sp中获取
		 */
		if (LoginUser.isLogin()) {
			//获取Bookids
			ServerCartDao.getBookIds(ctx, new GetBookIdsListener() {
				
				@Override
				public void onGetBookIdsSuccess(String[] bookIdArr) {
					if (bookIdArr == null) {
						if (bookCartInfoListener != null) {
							bookCartInfoListener.onSuccess(false, null);
						}
					}
					List<Map<String, String>> list = new ArrayList<Map<String, String>>();
					for (String bookId : bookIdArr) {
						Map<String, String> map = new HashMap<String, String>();
						map.put("Id", bookId + "");
						map.put("num", "1");
						list.add(map);
					}
					//将bookId封装参数获取购物车列表Item模型数据
					ServerCartDao.getBookCartItems(ctx, list, new GetBookCartItemsListener() {
						
						@Override
						public void onGetBookCartItemsSuccess(BookCardItemEntity cardItemEntity) {
							// TODO Auto-generated method stub
							if (bookCartInfoListener != null) {
								bookCartInfoListener.onSuccess(false, cardItemEntity);
							}
						}
						
						@Override
						public void onGetBookCartItemsFail() {
							// TODO Auto-generated method stub
							if (bookCartInfoListener != null) {
								bookCartInfoListener.onFail();
							}
						}
					});
				}
				
				@Override
				public void onGetBookIdsStart() {
					// TODO Auto-generated method stub
					if (bookCartInfoListener != null) {
						bookCartInfoListener.onStart();
					}
				}
				
				@Override
				public void onGetBookIdsFail() {
					// TODO Auto-generated method stub
					if (bookCartInfoListener != null) {
						bookCartInfoListener.onFail();
					}
				}
			});
		} else {
			if (bookCartInfoListener != null) {
				bookCartInfoListener.onStart();
			}
			List<SimplifiedDetail> localBookInfos = LocalCartDao.getLocalBookInfosInSP(ctx);
			if (localBookInfos != null && localBookInfos.size()>0) {
				//拼接参数
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				for (SimplifiedDetail sd : localBookInfos) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("Id", sd.bookId + "");
					map.put("num", "1");
					list.add(map);
				}
				
				ServerCartDao.getBookCartItems(ctx, list, new GetBookCartItemsListener() {
					
					@Override
					public void onGetBookCartItemsSuccess(BookCardItemEntity cardItemEntity) {
						// TODO Auto-generated method stub
						if (bookCartInfoListener != null) {
							bookCartInfoListener.onSuccess(false, cardItemEntity);
						}
					}
					
					@Override
					public void onGetBookCartItemsFail() {
						if (bookCartInfoListener != null) {
							bookCartInfoListener.onFail();
						}
					}
				});
			}else{
				//本地为空
				if (bookCartInfoListener != null) {
					bookCartInfoListener.onSuccess(false, null);
				}
			}
			
		}
	}
	
	
	public interface AddToCartListener{
		void onAddSuccess();
		void onAddFail();
	}
	
	/**
	 * 
	 * @Title: addToShoppingCart
	 * @Description: 添加到购物车
	 * @param @param ctx
	 * @param @param bookids
	 * @param @param updateListener
	 * @return void
	 * @throws
	 * @date 2015年4月22日 下午6:55:00
	 */
	public void addToShoppingCart(Context ctx,SimplifiedDetail simplifiedDetail,final AddToCartListener addListener){
		//已登录，同步到服务器
		if (LoginUser.isLogin()) {
			ServerCartDao.addToCart(ctx, new String[]{String.valueOf(simplifiedDetail.bookId)}, new AddToCartInnerListener() {
				
				@Override
				public void onAddSuccess() {
					// TODO Auto-generated method stub
					if (addListener != null) {
						addListener.onAddSuccess();
					}
				}
				
				@Override
				public void onAddFail() {
					// TODO Auto-generated method stub
					if (addListener != null) {
						addListener.onAddFail();
					}
				}
			});
		}else{
			if (LocalCartDao.addToLocal(ctx, simplifiedDetail)) {
				if (addListener != null) {
					addListener.onAddSuccess();
				}
			}else{
				if (addListener != null) {
					addListener.onAddFail();
				}
			}
		}
	}
	
	public interface DelFromCartListener{
		void onDelSuccess(boolean isDelInCart,BookCardItemEntity cardItemEntity);
		void onDelFail();
	}
	
	/**
	 * 
	 * @Title: deleteFromShoppingCart
	 * @Description: 从购物车删除
	 * @param @param ctx
	 * @param @param bookid
	 * @param @param updateListener
	 * @return void
	 * @throws
	 * @date 2015年4月22日 下午6:56:18
	 */
	public void deleteFromShoppingCart(Context ctx,final boolean isDelInCart,String[] bookids,final DelFromCartListener delListener){
		MZLog.d("J", "deleteFromShoppingCart");
		StringBuilder sb = new StringBuilder();
		for (String bookid : bookids) {
			sb.append(bookid+",");
		}
		StringBuilder bookidsSB = sb.deleteCharAt(sb.length()-1);
		if (LoginUser.isLogin()) {
//			updateShoppingCart(ctx, bookidsSB.toString(), "3", updateListener);
			ServerCartDao.delFromCart(ctx, bookids, isDelInCart, new DelFromCartInnerListener() {
				
				@Override
				public void onDelSuccess(boolean isDelInCart,
						BookCardItemEntity cardItemEntity) {
					if (delListener != null) {
						delListener.onDelSuccess(isDelInCart, cardItemEntity);
					}
				}
				
				@Override
				public void onDelFail() {
					if (delListener != null) {
						delListener.onDelFail();
					}
				}
			});
		}else{
			if (LocalCartDao.delFromLocal(ctx, bookids)) {
				if (delListener != null) {
					if (isDelInCart) {
						List<SimplifiedDetail> localBookInfos = LocalCartDao.getLocalBookInfosInSP(ctx);
						if (localBookInfos == null || localBookInfos.size() == 0) {
							if (delListener != null) {
								delListener.onDelSuccess(isDelInCart, null);
								return;
							}
						}
						
						//拼接参数
						List<Map<String, String>> list = new ArrayList<Map<String, String>>();
						for (SimplifiedDetail sd : localBookInfos) {
							Map<String, String> map = new HashMap<String, String>();
							map.put("Id", sd.bookId + "");
							map.put("num", "1");
							list.add(map);
						}
						
						ServerCartDao.getBookCartItems(ctx, list, new GetBookCartItemsListener() {
							
							@Override
							public void onGetBookCartItemsSuccess(BookCardItemEntity cardItemEntity) {
								// TODO Auto-generated method stub
								if (delListener != null) {
									delListener.onDelSuccess(isDelInCart, cardItemEntity);
								}
							}
							
							@Override
							public void onGetBookCartItemsFail() {
								// TODO Auto-generated method stub
								
							}
						});
						delListener.onDelSuccess(isDelInCart, null);
					}else{
						delListener.onDelSuccess(isDelInCart, null);
					}
				}
			}else{
				if (delListener != null) {
					delListener.onDelFail();
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @Title: isExistInShoppingCart
	 * @Description: 指定bookid是否存在购物车
	 * @param @param ctx
	 * @param @param bookid
	 * @param @param checkExistListener
	 * @return void
	 * @throws
	 * @date 2015年4月23日 下午5:26:54
	 */
	public void isExistInShoppingCart(Context ctx,final String bookid,final CheckExistListener checkExistListener){
		if (checkExistListener == null) {
			return;
		}
		if (LoginUser.isLogin()) {
			ServerCartDao.getBookIds(ctx, new GetBookIdsListener() {
				
				@Override
				public void onGetBookIdsSuccess(String[] bookIdArr) {
					if (bookIdArr == null && checkExistListener != null) {
						checkExistListener.isExist(false);
						return;
					}
					boolean isExist = false;
					for (String string : bookIdArr) {
						if (string.equals(bookid)) {
							isExist = true;
							break;
						}
					}
					if (checkExistListener != null) {
						checkExistListener.isExist(isExist);
					}
				}
				
				@Override
				public void onGetBookIdsStart() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onGetBookIdsFail() {
					// TODO Auto-generated method stub
					if (checkExistListener != null) {
						checkExistListener.isExist(false);
					}
				}
			});
		}else{
			List<SimplifiedDetail> localBookInfos = LocalCartDao.getLocalBookInfosInSP(ctx);
			if (localBookInfos == null || localBookInfos.size() == 0) {
				if (checkExistListener != null) {
					checkExistListener.isExist(false);
				}
				return;
			}
			boolean isExist = false;
			for (SimplifiedDetail sd : localBookInfos) {
				if (sd.bookId == Long.parseLong(bookid)) {
					isExist = true;
					break;
				}
			}
			if (checkExistListener != null) {
				checkExistListener.isExist(isExist);
			}
		}
	}
	
	/**
	 * 
	 * @Title: getTotalCount
	 * @Description: 查询购物车中商品的数量
	 * @param @param ctx
	 * @param @param countListener
	 * @return void
	 * @throws
	 * @date 2015年4月24日 下午3:36:03
	 */
	public void getTotalCount(Context ctx,final GetTotalCountListener countListener) {
		if (countListener == null) {
			return;
		}
		if (LoginUser.isLogin()) {
			ServerCartDao.getBookIds(ctx, new GetBookIdsListener() {
							
				@Override
				public void onGetBookIdsSuccess(String[] bookIdArr) {
					// TODO Auto-generated method stub
					if (countListener != null) {
						if (bookIdArr!= null) {
							countListener.onResult(bookIdArr.length);
						}else{
							countListener.onResult(0);
						}
					}
				}
				
				@Override
				public void onGetBookIdsStart() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onGetBookIdsFail() {
					// TODO Auto-generated method stub
					
				}
		    });
		}else{
			List<SimplifiedDetail> localBookInfos = LocalCartDao.getLocalBookInfosInSP(ctx);
			if (countListener != null && localBookInfos != null) {
				countListener.onResult(localBookInfos.size());
			}
		}
	}
	
	/**
	 * 
	 * @Title: syncLocalToServer
	 * @Description: 未登录状态添加商品到购物车，登录后将本地的购物车信息同步到服务器
	 * @param @param ctx
	 * @return void
	 * @throws
	 * @date 2015年4月24日 下午3:42:00
	 */
	public void syncLocalToServer(final Context ctx) {
		//1.获取服务端购物车
		ServerCartDao.getBookIds(ctx, new GetBookIdsListener() {
			
			@Override
			public void onGetBookIdsSuccess(String[] bookIdArr) {
				String[] bookIds = null;
				//2.服务端购物车中存在商品
				if (bookIdArr != null && bookIdArr.length > 0) {
					
					//从本地获取购物车信息
					List<SimplifiedDetail> localBookInfos = LocalCartDao.getLocalBookInfosInSP(ctx);
					if (localBookInfos != null && localBookInfos.size() > 0) {
						
						//合并购物车
						List<String > bookList = new ArrayList<String>();
						for (SimplifiedDetail sd : localBookInfos) {
							bookList.add(sd.bookId+"");
						}
						for (String bookid : bookIdArr) {
							bookList.add(bookid);
						}
						
						bookIds = new String[bookList.size()];
						for (int i = 0; i < bookList.size(); i++) {
							bookIds[i] = bookList.get(i);
						}
					}
				}else{
					//服务端不存在购物车，只需将本地的购物车同步到服务端
					List<SimplifiedDetail> localBookInfos = LocalCartDao.getLocalBookInfosInSP(ctx);
					if (localBookInfos != null) {
						bookIds = new String[localBookInfos.size()];
						for (int i = 0; i < localBookInfos.size(); i++) {
							bookIds[i] = localBookInfos.get(i).bookId+"";
						}
					}
				}
				
				//同步到服务器
				ServerCartDao.addToCart(ctx, bookIds, new AddToCartInnerListener() {
					
					@Override
					public void onAddSuccess() {
						// TODO Auto-generated method stub
						MZLog.d("J", "同步到购物车成功");
						LocalUserSetting.clearBookCart(ctx);
					}
					
					@Override
					public void onAddFail() {
						// TODO Auto-generated method stub
						MZLog.e("J", "同步到购物车失败");
					}
				}); 
			}
			
			@Override
			public void onGetBookIdsStart() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onGetBookIdsFail() {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	
	
	

}
