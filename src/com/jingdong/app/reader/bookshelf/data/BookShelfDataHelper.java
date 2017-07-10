package com.jingdong.app.reader.bookshelf.data;

import java.util.List;

import org.apache.http.Header;

import android.content.Context;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.bookshelf.inf.FetchChangduListListener;
import com.jingdong.app.reader.client.DownloadStateManager;
import com.jingdong.app.reader.download.util.L;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.MyOnlineBookEntity;
import com.jingdong.app.reader.entity.extra.ChangduEbook;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.NetWorkUtils;

/**
 * @ClassName: BookShelfDataHelper
 * @Description: 书架数据补助类
 * @author J.Beyond
 * @date 2015年8月24日 下午4:16:10
 *
 */
public class BookShelfDataHelper {

	/**单一实例*/
	private static BookShelfDataHelper mInstance;
	private BookShelfDataHelper() {}
	public static BookShelfDataHelper getInstance() {
		if (mInstance == null) {
			synchronized (BookShelfDataHelper.class) {
				if (mInstance == null) {
					mInstance = new BookShelfDataHelper();
				}
			}
		}
		return mInstance;
	}
	

	public void searchChangduBook(final Context context,String keyword,int currentSearchPage,int perSearchCount,final FetchChangduListListener listener) {
		if (listener == null) {
			L.e("listener cannot be null");
			return;
		}
		//网络异常
		if (!NetWorkUtils.isNetworkConnected(context)) {
			Toast.makeText(context, context.getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
			listener.onFailure(context.getString(R.string.network_connect_error));
			return;
		}
		WebRequestHelper.get(URLText.JD_BOOK_CHANGDU_URL, RequestParamsPool.searchChangduEbookParams(currentSearchPage + "", perSearchCount + "", keyword), true,
				new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						listener.onFailure(context.getString(R.string.network_connect_error));
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String result = new String(responseBody);
						ChangduEbook ebook = GsonUtils.fromJson(result, ChangduEbook.class);
						if (ebook != null && ebook.code == 0) {
							listener.onSuccess(ebook);
						} else if (ebook == null){
							listener.onFailure("服务端数据异常");
						}
					}
				});
	}
	
	
	/**
	 * 
	 * @Title: fetchChangduList
	 * @Description: 获取畅读列表数据
	 * @param @param context
	 * @param @param currentPage
	 * @param @param perPageCount
	 * @param @param listener
	 * @return void
	 * @throws
	 */
	public void fetchChangduList(final Context context,final int currentPage,int perPageCount,final FetchChangduListListener listener){
		if (listener == null) {
			L.e("listener cannot be null");
			return;
		}
		//网络异常
		if (!NetWorkUtils.isNetworkConnected(context)) {
			Toast.makeText(context, context.getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
			listener.onFailure(context.getString(R.string.network_connect_error));
			return;
		}

		// MZLog.d("wangguodong", "本地书籍ebook个数:"+allLocalBooks.size()+"个");
		WebRequestHelper.post(URLText.JD_BOOK_CHANGDU_URL, RequestParamsPool.getChangduEbookParams(currentPage + "", perPageCount + ""), true,
				new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						listener.onFailure(context.getString(R.string.network_connect_error));
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

						String result = new String(responseBody);
						L.d(result);
						ChangduEbook ebook = GsonUtils.fromJson(result, ChangduEbook.class);
						if (ebook != null && ebook.code == 0) {
							listener.onSuccess(ebook);
						} else {
							listener.onFailure("服务端数据异常");
						}
					}
				});
		
	}
	
	public EBookItemHolder checkBookState(List<LocalBook> list, MyOnlineBookEntity entity) {

		EBookItemHolder holder = new EBookItemHolder();
		holder.ebook = entity;
		holder.existInLocal = false;
		holder.failed = false;
		holder.paused = false;
		holder.inWaitingQueue = false;

		if (null == list || list.size() == 0)
			return holder;// 本地没有任何数据
		for (int i = 0; i < list.size(); i++) {

			LocalBook localBook = list.get(i);
			if (localBook.book_id == entity.itemId) {

				if (localBook.source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
					int state = DownloadStateManager.getLocalBookState(localBook);
					if (state == DownloadStateManager.STATE_LOADED || holder.existInLocal) {
						holder.existInLocal = true;

					} else if (state == DownloadStateManager.STATE_LOADING) {
						if (localBook.state == LocalBook.STATE_LOADING || localBook.state == LocalBook.STATE_LOAD_READY) {
							holder.inWaitingQueue = true;
						}
						if (localBook.state == LocalBook.STATE_LOAD_PAUSED) {
							holder.paused = true;
						}
					} else {
						holder.failed = true;
					}
				} else {// 本地书籍有试读版本或借阅版本，购买版本
					holder.existInLocal = false;
					holder.failed = false;
					holder.paused = false;
					holder.inWaitingQueue = false;
				}
			}
		}
		return holder;
	}
	
	public static class EBookItemHolder {
		public MyOnlineBookEntity ebook;
		public boolean existInLocal;
		public boolean paused;
		public boolean failed;
		public boolean inWaitingQueue;
		public int progress;
	}
	
	
	
}
