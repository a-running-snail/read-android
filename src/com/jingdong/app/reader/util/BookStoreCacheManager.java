package com.jingdong.app.reader.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.Header;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookstore.style.controller.BookStoreStyleContrller;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController.OnHeaderActionClickListener;
import com.jingdong.app.reader.bookstore.style.controller.RankingListViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.SpecialViewStyleController;
import com.jingdong.app.reader.bookstore.style.controller.TopicsViewStyleController;
import com.jingdong.app.reader.entity.extra.BookStoreEntity;
import com.jingdong.app.reader.entity.extra.BookStoreEntity.MainThemeList;
import com.jingdong.app.reader.entity.extra.BookStoreEntity.Modules;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity;
import com.jingdong.app.reader.entity.extra.CategoryList;
import com.jingdong.app.reader.entity.extra.JDCategoryBook;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
@Deprecated
public class BookStoreCacheManager {
	private static final String TAG = "BookStoreCacheManager";
	private static final BookStoreCacheManager cacheManager = new BookStoreCacheManager();
	public static final int saveTime = ACache.TIME_DAY * 360;
	private ACache cache = null;
//	public static final String CACHE_ACTION = "com.jingdong.app.reader.CacheService";
	private String[] tabText = { "首页", "优惠", "排行" };
	private HashMap<String, BookStoreModuleBookListEntity> tempCache = new HashMap<String, BookStoreModuleBookListEntity>();
	private static final ConcurrentMap<String, View> viewCache = new ConcurrentHashMap<>();
	private static final ConcurrentMap<Integer, ConcurrentMap<String, View>> pageCache = new ConcurrentHashMap<Integer, ConcurrentMap<String, View>>();

	// private
	private BookStoreCacheManager() {
		cache = MZBookApplication.getInstance().getBoostoreCache();
	}

	public static BookStoreCacheManager getInstance() {
		return cacheManager;
	}

	public void putCache(String key, Serializable data) {
		cache.put(key, data, saveTime);
	}

	public void initCache() {
		try {

			// if
			// (NetWorkUtils.isNetworkConnected(MZBookApplication.getContext()))
			// {
			WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getBookStoreFrameWorkParams(),
					new MyAsyncHttpResponseHandler(MZBookApplication.getContext()) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							Toast.makeText(MZBookApplication.getContext(), "网络不可用", Toast.LENGTH_LONG).show();
						}

						@Override
						public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

							String result = new String(responseBody);
							try {
								BookStoreEntity entity = GsonUtils.fromJson(result, BookStoreEntity.class);
								if (entity != null) {
									cache.put("top", entity, saveTime);
									if (entity.mainThemeList != null) {
										int size = entity.mainThemeList.size();
										for (int i = 0; i < size && i < 4; i++) {
											for (int n = 0; n < entity.mainThemeList.get(i).modules.size(); n++) {
												requestModule(entity.mainThemeList.get(i).modules.get(n), i);
											}
										}
									}
								} else {
									MZLog.d(TAG, "服务器没有返沪数据。。。。。。");
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
					});
			// }else{
			// MZLog.d(TAG, "更新缓存数据失败，没有可用网络");
			// }

		} catch (Exception e) {
			e.printStackTrace();
			MZLog.d(TAG, "更新缓存数据失败，没有可用网络");
		}
	}

	public void initCache(final int pageIndex) {
		try {

			if (NetWorkUtils.isNetworkConnected(MZBookApplication.getContext())) {
				WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getBookStoreFrameWorkParams(),
						new MyAsyncHttpResponseHandler(MZBookApplication.getContext()) {

							@Override
							public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							}

							@Override
							public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

								String result = new String(responseBody);
								try {
									BookStoreEntity entity = GsonUtils.fromJson(result, BookStoreEntity.class);
									if (entity != null) {
										cache.put("top", entity, saveTime);
										if (entity.mainThemeList != null) {
											for (int n = 0; n < entity.mainThemeList.get(pageIndex).modules.size(); n++) {
												requestModule(entity.mainThemeList.get(pageIndex).modules.get(n), pageIndex);
											}
										}
									} else {
										MZLog.d(TAG, "服务器没有返沪数据。。。。。。");
									}
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						});
			} else {
				MZLog.d(TAG, "更新缓存数据失败，没有可用网络");
			}

		} catch (Exception e) {
			e.printStackTrace();
			MZLog.d(TAG, "更新缓存数据失败，没有可用网络");
		}
	}

	public void rfeshCache(final int pageIndex, final Rfesh rf) {
		if (rf == null) {
			return;
		}
		try {
			// viewCache.clear();

			if (NetWorkUtils.isNetworkConnected(MZBookApplication.getContext())) {
				WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getBookStoreFrameWorkParams(),
						new MyAsyncHttpResponseHandler(MZBookApplication.getContext()) {

							@Override
							public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

								rf.fail("请求失败，请下拉刷新");
							}

							@Override
							public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

								String result = new String(responseBody);
								// 获取书城架构
								try {

									BookStoreEntity entity = GsonUtils.fromJson(result, BookStoreEntity.class);

									if (entity != null) {
										cache.put("top", entity, saveTime);
										if (entity.mainThemeList != null) {
											for (int n = 0; n < entity.mainThemeList.get(pageIndex).modules.size(); n++) {
												requestModule(entity.mainThemeList.get(pageIndex).modules.get(n), pageIndex, rf,
														n == entity.mainThemeList.get(pageIndex).modules.size() - 1);
											}
										}
									} else {
										MZLog.d(TAG, "服务器没有返沪数据。。。。。。");
										rf.fail();
									}
								} catch (Exception e) {
									e.printStackTrace();
									rf.fail("更新失败，请下拉刷新");
								}

							}
						});
			} else {
				MZLog.d(TAG, "更新缓存数据失败，没有可用网络");
				rf.fail("您的网络当前不可用");
			}

		} catch (Exception e) {
			e.printStackTrace();
			MZLog.d(TAG, "更新缓存数据失败，没有可用网络.....");
			rf.fail("更新失败，请下拉刷新");
		}
	}

	public boolean initView() {
		boolean flag = true;
		if (cache == null) {
			return false;
		}
		final BookStoreEntity entity = (BookStoreEntity) cache.getAsObject("top");
		if (entity == null) {
			return false;
		}

		// TODO Auto-generated method stub
		int size = entity.mainThemeList.size();
		Modules md = null;
		for (int i = 0; i < size && i < 4; i++) {
			for (int n = 0; n < entity.mainThemeList.get(i).modules.size(); n++) {
				requestModule(entity.mainThemeList.get(i).modules.get(n), i);
				md = entity.mainThemeList.get(i).modules.get(n);
				String name = md.id + "" + md.moduleType;
				if (cache.getAsObject(name) != null) {
					try {
						initViewFormCache(i, name, md.moduleType, (BookStoreModuleBookListEntity) cache.getAsObject(name));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					return false;
				}
			}
		}
		return flag;
	}

	private void requestModule(final BookStoreEntity.Modules mod, final int pageIndex) {
		try {

			final int modType = mod.moduleType;
			WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getBookStoreChildModuleParams(mod.id, modType),
					new MyAsyncHttpResponseHandler(MZBookApplication.getContext()) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							// isCurrentRequestOver =true;
							MZLog.d(TAG, "更新模块失败：" + mod.showName);
						}

						@Override
						public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

							String result = new String(responseBody);
							MZLog.d(TAG, "服务器返回数据 ：" + mod.showName + "=" + result);
							final BookStoreModuleBookListEntity entity = GsonUtils.fromJson(result, BookStoreModuleBookListEntity.class);
							if (entity != null) {
								String key = String.valueOf(mod.id + "" + mod.moduleType);
								cache.put(key, entity, saveTime);
								tempCache.put(key, entity);
								try {
									initViewFormCache(pageIndex, key, modType, entity);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								MZLog.d(TAG, "更新模块成功");
							} else {
								MZLog.d(TAG, "更新模块失败:" + mod.showName);

							}
						}
					});

		} catch (Exception e) {

		}
	}

	public int isHaveCache(int pageIndex) {
		MZLog.d("isHaveCache", "cache.getAsObject(\"top\")=" + cache.getAsObject("top"));
		if (cache.getAsObject("top") == null) {
			return 0;
		}
		BookStoreEntity entity = (BookStoreEntity) cache.getAsObject("top");
		ArrayList<Modules> modulesList = entity.mainThemeList.get(pageIndex).modules;
		int size = modulesList.size();
		MZLog.d("isHaveCache", "viewCache.size()<size=" + (viewCache.size() < size) + ",viewCache.size()=" + viewCache.size() + ",size="
				+ size);
		// if(viewCache.size()<size){
		// return false;
		// }
		// String key=null;
		// for(Modules modules:modulesList){
		// key=modules.id+""+modules.moduleType;
		// if(getView(key)==null){
		// MZLog.d("isHaveCache", "getView(key) is null");
		// return false;
		// }else{
		// continue;
		// }
		// }
		return 0;
	}

	private void requestModule(final BookStoreEntity.Modules mod, final int pageIndex, final Rfesh rf, final boolean finsh)
			throws Exception {
		try {

			final int modType = mod.moduleType;
			WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getBookStoreChildModuleParams(mod.id, modType),
					new MyAsyncHttpResponseHandler(MZBookApplication.getContext()) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							// isCurrentRequestOver =true;
							MZLog.d(TAG, "更新模块失败：" + mod.showName);
							if (finsh) {
								if (rf != null) {
									rf.rfeshFinsh();
								}
							}
						}

						@Override
						public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

							String result = new String(responseBody);
							MZLog.d(TAG, mod.showName + "=" + result);
							final BookStoreModuleBookListEntity entity = GsonUtils.fromJson(result, BookStoreModuleBookListEntity.class);
							if (entity != null) {
								String key = String.valueOf(mod.id + "" + mod.moduleType);
								cache.put(key, entity, saveTime);
								tempCache.put(key, entity);
								try {
									initViewFormCache(pageIndex, key, modType, entity);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								MZLog.d(TAG, "更新模块成功");
							} else {
								MZLog.d(TAG, "更新模块失败:" + mod.showName);

							}
							if (finsh) {
								rf.rfeshFinsh();
							}
						}
					});

		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public MainThemeList getBookStoreEntityFromIndex(int index) {
		try {

			BookStoreEntity entity = (BookStoreEntity) cache.getAsObject("top");
			int size = entity.mainThemeList.size();
			if (size == 0 || index > size || index < 0) {
				return null;
			}
			return entity.mainThemeList.get(index);

		} catch (Exception e) {
			return null;
		}
	}

	public BookStoreModuleBookListEntity getBookStoreModuleBookListEntity(String name) {
		try {

			BookStoreModuleBookListEntity enit = null;
			if (tempCache.containsKey(name)) {
				enit = tempCache.get(name);
			}
			if (enit == null) {
				enit = (BookStoreModuleBookListEntity) cache.getAsObject(name);
			}
			return enit;

		} catch (Exception e) {
			return null;
		}
	}

	private ConcurrentHashMap<String, ImageView> imgViewCache = new ConcurrentHashMap<String, ImageView>();

	public void addImageViewToCache(String key, ImageView view) {
		imgViewCache.put(key, view);
	}

	public ImageView getImageViewFromCache(String key) {
		if (imgViewCache == null || key == null) {
			MZLog.i(TAG, " getImageViewFromCache key=" + key + ", imgViewCache=" + imgViewCache.size());
			return null;
		}
		return imgViewCache.get(key);
	}

	public void removeImageViewFromCache(String key) {
		if (imgViewCache.containsKey(key)) {
			imgViewCache.remove(key);
		}
	}

	private void initViewFormCache(final int currentpage, String name, int modType, final BookStoreModuleBookListEntity entity)
			throws Exception {
		int n = 0;
		View view = null;
		switch (modType) {

		case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_1:
		case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_2:
		case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_3:
		case BookStoreStyleContrller.TYPE_CIRCLE_LABEL_4:
			// 书城顶部专题部分view
			view = TopicsViewStyleController.getTopicsStyleView(MZBookApplication.getContext(), 1, 4, entity.moduleLinkChildList, null);
			break;
		case BookStoreStyleContrller.TYPE_BOOK_LIST: {
			int childType = BookStoreStyleContrller.TYPE_BOOK_LIST * 10 + entity.moduleBookChild.showType;
			int row = 0;
			if (childType == BookStoreStyleContrller.TYPE_BOOK_LIST_GRID) {
				if (entity.resultList != null)
					row = (int) Math.floor(entity.resultList.size() / 3.0);
				view = BooksViewStyleController.getBooksStyleView(MZBookApplication.getContext(), entity.moduleBookChild.showName,entity.moduleBookChild.showInfo, "更多",
						row, 3, entity.resultList, new OnHeaderActionClickListener() {
							@Override
							public void onHeaderActionClick() {
								TalkingDataUtil.onBookStoreEvent(MZBookApplication.getContext(), tabText[currentpage],
										entity.moduleBookChild.showName);
								Intent intent = new Intent(MZBookApplication.getContext(), BookStoreBookListActivity.class);
								intent.putExtra("fid", entity.moduleBookChild.id);
								intent.putExtra("ftype", 2);
								intent.putExtra("relationType", 1);
								intent.putExtra("showName", entity.moduleBookChild.showName);
								intent.putExtra("showInfo", entity.moduleBookChild.showInfo);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								MZBookApplication.getContext().startActivity(intent);
							}
						});
			} else if (childType == BookStoreStyleContrller.TYPE_BOOK_LIST_VERTICAL) {
				if (entity.resultList != null)
					row = (int) Math.floor(entity.resultList.size() / 1.0);
				view = RankingListViewStyleController.getRankingListStyleView(MZBookApplication.getContext(),
						entity.moduleBookChild.showName, "更多", row, 1, entity.resultList, new OnHeaderActionClickListener() {
							@Override
							public void onHeaderActionClick() {
								// TODO Talking-Data
								TalkingDataUtil.onBookStoreEvent(MZBookApplication.getContext(), tabText[currentpage],
										entity.moduleBookChild.showName);

								Intent intent = new Intent(MZBookApplication.getContext(), BookStoreBookListActivity.class);
								intent.putExtra("fid", entity.moduleBookChild.id);
								intent.putExtra("ftype", 2);
								intent.putExtra("relationType", 1);
								intent.putExtra("showName", entity.moduleBookChild.showName);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								MZBookApplication.getContext().startActivity(intent);
							}
						});
			}
		}
			break;
		case BookStoreStyleContrller.TYPE_SPECIAL_THEME:
			List<BookStoreModuleBookListEntity.ModuleBookChild> childs = new ArrayList<BookStoreModuleBookListEntity.ModuleBookChild>();
			childs.add(entity.moduleBookChild);
			view = SpecialViewStyleController.getSpecialStyleView(MZBookApplication.getContext(), childs);
			break;
		case BookStoreStyleContrller.TYPE_BOOK_BANNER:
			// BannerViewStyleController bannerViewController =
			// BannerViewStyleController.getInstance();
			//
			// view =
			// bannerViewController.getBannerView(MZBookApplication.getContext(),entity.moduleLinkChildList);
			// view.setTag("banner");
			break;
		case BookStoreStyleContrller.TYPE_BOOK_CATEGORY:
			break;

		}
		if (view != null) {
			view.setTag(name);
			viewCache.put(name, view);
		} else {
		}

	}

	public View getView(String name) {
		return null;
	}

	public interface Rfesh {
		public void rFeshView(ViewGroup vc);

		public void rfeshFinsh();

		public void fail();

		public void fail(String error);
	}

	public void reqCategoryListData() {
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getCategoryBookParams(1), new MyAsyncHttpResponseHandler(
				MZBookApplication.getContext()) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

				String result = new String(responseBody);
				JDCategoryBook jdCategoryBook = GsonUtils.fromJson(result, JDCategoryBook.class);
				if (jdCategoryBook != null) {
					ArrayList<CategoryList> categoryList = (ArrayList<CategoryList>) jdCategoryBook.getCatList();
					cache.put("CategoryList", categoryList, saveTime);
				}
				// else
				// Toast.makeText(getActivity(),
				// getString(R.string.network_connect_error),
				// Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void clearData() {
		cache.clear();
		viewCache.clear();
	}

	public List<CategoryList> getCategoryList() {
		List<CategoryList> list = (ArrayList<CategoryList>) cache.getAsObject("CategoryList");
		return list;
	}
}
