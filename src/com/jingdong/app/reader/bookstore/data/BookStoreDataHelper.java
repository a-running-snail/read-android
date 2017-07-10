package com.jingdong.app.reader.bookstore.data;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.bookshelf.data.BookShelfDataHelper;
import com.jingdong.app.reader.bookstore.StoreBook;
import com.jingdong.app.reader.bookstore.style.controller.BookStoreStyleContrller;
import com.jingdong.app.reader.cache.CacheManager;
import com.jingdong.app.reader.entity.extra.BookStoreEntity;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity;
import com.jingdong.app.reader.entity.extra.CategoryList;
import com.jingdong.app.reader.entity.extra.JDCategoryBook;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity.BookStoreList;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ToastUtil;

public class BookStoreDataHelper {
	
	public static final String BOOKSTORE_FRAMEWORK_CACHE_KEY = "bookstore_framework_cache_key";
	public static final String BOOKSTORE_CATEGORY_CACHE_KEY = "bookstore_category_cache_key";
	public static final String BOOKSTORE_MODULE_CACHE_KEY_PREFIX = "bookstore_module_cache_";
	private static final String TAG = "BookStoreDataHelper";

	/**单一实例*/
	private static BookStoreDataHelper mInstance;
	private BookStoreDataHelper() {}
	public static BookStoreDataHelper getInstance() {
		if (mInstance == null) {
			synchronized (BookStoreDataHelper.class) {
				if (mInstance == null) {
					mInstance = new BookStoreDataHelper();
				}
			}
		}
		return mInstance;
	}
	

	public interface RefreshCallback {
		void onRefreshFinish();
		void onRefreshFail();
	}

	public interface GetCacheDataListener {
		void onSuccess(Serializable serializable);
		void onSuccess(Map<String, Serializable> moduleMap);
		void onFail();
	}

	/**
	 * 
	 * @Title: getBookStoreFramework
	 * @Description: 获取书城主框架数据：本地缓存有数据从缓存中取，否则从网络获取
	 * @param @param ctx
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月19日 下午5:22:18
	 */
	public void getBookStoreFramework(Context ctx, final GetCacheDataListener listener) {
		if (listener == null) {
			return;
		}
		//是否读取缓存数据
		if (isReadCacheData(ctx, BOOKSTORE_FRAMEWORK_CACHE_KEY)) {
			MZLog.i(TAG, "************getBookStoreFramework from cache");
			new CacheTask(ctx,BOOKSTORE_FRAMEWORK_CACHE_KEY, listener).execute();
		} else {
			MZLog.i(TAG, "************getBookStoreFramework from internet");
			requestBookStoreFramework(ctx, listener);
		}
	}

	/**
	 * 
	 * @Title: requestBookStoreFramework
	 * @Description: 网络请求获取书城框架数据
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年5月19日 下午2:50:55
	 */
	public void requestBookStoreFramework(final Context ctx, final GetCacheDataListener listener) {
		//检查是否有网络
		if (!NetWorkUtils.isNetworkConnected(ctx)) {
			listener.onFail();
			return;
		}

		//请求书城数据
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getBookStoreFrameWorkParams(),
				new MyAsyncHttpResponseHandler(ctx) {

					/**
					 * 	请求失败
					 */
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						listener.onFail();
					}

					/**
					 * 请求成功
					 */
					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						//？
						listener.onFail();
						
						String result = new String(responseBody);
						try {
							/**
							 * 解析书城数据
							 */
							BookStoreEntity entity = GsonUtils.fromJson(result, BookStoreEntity.class);
							if (entity != null) {
								listener.onSuccess(entity);
								//缓存主框架数据
								new SaveCacheTask(ctx, entity, BOOKSTORE_FRAMEWORK_CACHE_KEY).execute();
							} else {
								listener.onFail();
							}
						} catch (Exception e) {
							e.printStackTrace();
							listener.onFail();
						}
					}
				});
	}


	/**
	 * 
	 * @Title: getModuleData
	 * @Description: 获取模块数据，优先从本地缓存中获取，本地缓存中没有从网络获取
	 * @param @param ctx
	 * @param @param mod
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月19日 下午6:00:14
	 */
	public void getModuleData(Context ctx, BookStoreEntity.Modules mod, GetCacheDataListener listener) {
		if (listener == null) {
			MZLog.e("J", "getModuleData listener is null");
			return;
		}
		// 限时特价不使用缓存数据，都需要从网络获取最新数据展示-tmj
		if (mod.moduleType == BookStoreStyleContrller.TYPE_SPECIAL_PRICE) {
			requestModule(ctx, mod, listener);
		} else {// 除限时特价外的其他模块依然采用原来的缓存逻辑，暂时不修改-tmj
			String key = BOOKSTORE_MODULE_CACHE_KEY_PREFIX + mod.id + "_" + mod.moduleType;
			if (isReadCacheData(ctx, key)) {
				CacheTask cacheTask = new CacheTask(ctx, key, listener);
				cacheTask.execute(key);
			} else {
				requestModule(ctx, mod, listener);
			}
		}
	}

	/**
	 * 
	 * @Title: requestModule
	 * @Description:从网络获取模块数据
	 * @param @param ctx
	 * @param @param mod
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月19日 下午6:01:50
	 */
	public void requestModule(final Context ctx, final BookStoreEntity.Modules mod, final GetCacheDataListener listener) {
		if (!NetWorkUtils.isNetworkConnected(ctx)) {
			listener.onFail();
			return;
		}
		final int modType = mod.moduleType;
		
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getBookStoreChildModuleParams(mod.id, modType),
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						listener.onFail();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String result = new String(responseBody);
						BookStoreModuleBookListEntity bookListEntity = GsonUtils.fromJson(result, BookStoreModuleBookListEntity.class);
						if (bookListEntity != null) {
							// 缓存数据
							String key = BOOKSTORE_MODULE_CACHE_KEY_PREFIX + mod.id + "_" + mod.moduleType;
							Map<String, Serializable> muduleMap = new HashMap<String, Serializable>();
							muduleMap.put(key, bookListEntity);
							listener.onSuccess(muduleMap);
							//缓存数据
							new SaveCacheTask(ctx, bookListEntity, key).execute();
						} else {
							listener.onFail();
						}
					}
				});
	}


	public void getBookStoreCategoryData(Context ctx, GetCacheDataListener listener) {
		if (listener == null) {
			return;
		}
//		if (isReadCacheData(ctx, BOOKSTORE_CATEGORY_CACHE_KEY)) {
//			MZLog.i(TAG, "************getBookStoreCategoryData from cache");
//			new CacheTask(ctx,BOOKSTORE_CATEGORY_CACHE_KEY, listener).execute();
//		} else {
			MZLog.i(TAG, "************getBookStoreCategoryData from cache");
			requestCategoryData(ctx, listener);
//		}
	}

	/**
	 * 
	 * @Title: requestCategoryData
	 * @Description: 网络请求书城分类数据
	 * @param @param ctx
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月21日 下午4:29:22
	 */
	public void requestCategoryData(final Context ctx, final GetCacheDataListener listener) {
		if (!NetWorkUtils.isNetworkConnected(ctx)) {
			listener.onFail();
			return;
		}
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getCategoryBookParams(1), new MyAsyncHttpResponseHandler(ctx) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//				ToastUtil.showToastInThread("网络请求出错了，请重试！", Toast.LENGTH_LONG);
				listener.onFail();
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

				String result = new String(responseBody);
				MZLog.d(TAG, "result::" + result);
				JDCategoryBook jdCategoryBook = GsonUtils.fromJson(result, JDCategoryBook.class);
				if (jdCategoryBook != null) {
					List<CategoryList> categoryList = jdCategoryBook.getCatList();
					if (categoryList == null) {
						MZLog.e("J", "服务器返回的分类数据为空");
						listener.onFail();
						return;
					}
					//回调
					listener.onSuccess(jdCategoryBook);
					//缓存数据
					new SaveCacheTask(ctx, jdCategoryBook, BOOKSTORE_CATEGORY_CACHE_KEY).execute();
				} else {
//					Toast.makeText(ctx, ctx.getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
					listener.onFail();
				}
			}
		});
	}
	
	public interface FetchRecommendDataListener{
		void onSuccess(BookStoreList bookStoreList);
		void onFailure();
	}
	
	/**
	 * 
	 * @Title: fetchRecommendData
	 * @Description: 获取下拉推荐数据
	 * @param @param ctx
	 * @param @param currentPage
	 * @param @param listener
	 * @return void
	 * @throws
	 */
	public void fetchRecommendData(final Context ctx,int currentPage, final FetchRecommendDataListener listener) {
		if (!NetWorkUtils.isNetworkConnected(ctx)) {
			listener.onFailure();
			return;
		}
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getuessYouLikeParams(currentPage), new MyAsyncHttpResponseHandler(ctx) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				listener.onFailure();
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				String result = new String(responseBody);
				Log.d("J", "%%%%%result::"+result);
				BookStoreList bookStoreList = GsonUtils.fromJson(result, BookStoreList.class);
				if (bookStoreList != null && bookStoreList.code == 0) {
					listener.onSuccess(bookStoreList);
				}else{
					listener.onFailure();
				}
			}
		});
	}

	/**
	 * 
	 * @Title: isReadCacheData
	 * @Description: 是否需要读取缓存
	 * @param @param ctx
	 * @param @param key
	 * @param @return
	 * @return boolean
	 * @throws
	 * @date 2015年5月22日 上午10:14:34
	 */
	protected boolean isReadCacheData(Context ctx, String key) {
		// 如果存在缓存
		if (CacheManager.isExistDataCache(ctx, key)) {
			// 没网络，需要读取缓存
			if (!NetWorkUtils.isNetworkConnected(ctx)) {
				MZLog.d(TAG, "========network unaviable!");
				return true;
			} else {
				//有网，判断缓存是否失效
				if (CacheManager.isCacheDataFailure(ctx, key)) {
					MZLog.d(TAG, "========缓存失效");
					return false;
				} else {
					return true;
				}
			}
		} else {
			MZLog.d(TAG, "========cache:"+key+" is not exist!");
			return false;
		}
	}

	/**
	 * 
	 * @ClassName: SaveCacheTask
	 * @Description: 异步保存缓存任务
	 * @author J.Beyond
	 * @date 2015年5月22日 上午11:13:58
	 *
	 */
	private class SaveCacheTask extends AsyncTask<Void, Void, Boolean> {
		private WeakReference<Context> mContext;
		private Serializable mSeri;
		private String mKey;

		private SaveCacheTask(Context context, Serializable seri, String key) {
			mContext = new WeakReference<Context>(context);
			this.mSeri = seri;
			this.mKey = key;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return CacheManager.saveObject(mContext.get(), mSeri, mKey);
		}
		
		
		@Override
		protected void onPostExecute(Boolean isCacheSuccess) {
			MZLog.d(TAG, "key::"+mKey+" cache sucess? "+isCacheSuccess);
		}
	}

	/**
	 * 
	 * @ClassName: CacheTask
	 * @Description:异步加载缓存数据
	 * @author J.Beyond
	 * @date 2015年5月22日 上午11:01:08
	 *
	 */
	private static class CacheTask extends AsyncTask<String, Void, Serializable> {

		private final WeakReference<Context> mContext;
		private GetCacheDataListener mListener;
		private String mKey;

		public CacheTask(Context ctx,String key, GetCacheDataListener listener) {
			this.mContext = new WeakReference<Context>(ctx);
			this.mListener = listener;
			this.mKey = key;
		}

		@Override
		protected Serializable doInBackground(String... params) {
			Serializable seri = CacheManager.readObject(mContext.get(), mKey);
			if (seri == null) {
				return null;
			} else {
				return seri;
			}
		}

		@Override
		protected void onPostExecute(Serializable seri) {
			if (seri == null) {
				mListener.onFail();
			}else{
				if (mKey!= null && mKey.contains(BOOKSTORE_MODULE_CACHE_KEY_PREFIX)) {
					Map<String, Serializable> muduleMap = new HashMap<String, Serializable>();
					muduleMap.put(mKey, (BookStoreModuleBookListEntity)seri);
					mListener.onSuccess(muduleMap);
				}else{
					mListener.onSuccess(seri);
				}
			}
		}
	}

}
