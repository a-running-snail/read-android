package com.jingdong.app.reader.reading;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.JDBook;
import com.jingdong.app.reader.entity.extra.JDBookDetail;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.view.xlistview.XListView;
import com.jingdong.app.reader.view.xlistview.XListView.IXListViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;

public class OtherBookList extends BaseActivityWithTopBar implements IXListViewListener{
	private XListView booklist = null;
	private int currentPage = 1;
	private int pageSize = 20;
	private boolean noMore = false;
	private BookListAdapter adapter = null;
	private int total = 10;
	private static int perPageCount = 10;

	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private List<JDBookDetail> jdBookList = new ArrayList<JDBookDetail>();
	private JDBook ebook = null;
	private int bookType = -1;
	private String author;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_booklist);
		
		author = getIntent().getStringExtra("author");
		searchBooks(author);
		booklist = (XListView) findViewById(R.id.listview);
		booklist.setPullLoadEnable(false);
		booklist.setPullRefreshEnable(false);
		booklist.setXListViewListener(this);

		adapter = new BookListAdapter(OtherBookList.this);
		booklist.setAdapter(adapter);
		
		booklist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				JDBookDetail eBook = jdBookList.get(position);
				Intent intent = new Intent(OtherBookList.this,
						BookInfoNewUIActivity.class);
				if (bookType == 0) {
					intent.putExtra("bookid", eBook.getEbookId());
				} else if (bookType == 1) {
					intent.putExtra("bookid", eBook.getPaperBookId());
				}
				startActivity(intent);
			}
		});
	}
	
	public void initData() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}
	
	private synchronized void searchBooks(final String key) {

		WebRequestHelper.get(
				URLText.JD_BASE_URL,
				RequestParamsPool.getSearchStoreEbookParams(currentSearchPage
						+ "", perSearchCount + "", key, ""),
				new MyAsyncHttpResponseHandler(OtherBookList.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						Toast.makeText(OtherBookList.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// TODO Auto-generated method stub

						String result = new String(responseBody);

						ebook = GsonUtils.fromJson(result, JDBook.class);

						if (ebook != null
								&& (ebook.getCode().equals("51") || (ebook
										.getCode().equals("0") && ebook
										.getResultCount() == 0))) {
								bookType = 1;
								initData();
								searchPaperBooks(key);
						} else if (ebook == null) {
							onLoadComplete();
							return;
						}

						if (ebook != null && ebook.getCode().equals("0")) {
							bookType = 0;
							currentSearchPage++;

							if (ebook.getBookList() != null
									&& currentSearchPage >= ebook.totalPage) {
									Toast.makeText(
											OtherBookList.this,
											"继续向上滑动搜索纸书", Toast.LENGTH_LONG)
											.show();
									booklist.setPullLoadEnable(true);
									bookType = 1;
									initData();
							
							} else {
								noMoreBookOnSearch = false;
								booklist.setPullLoadEnable(true);
							}

								List<JDBookDetail> all = new ArrayList<JDBookDetail>();
								for (int i = 0; i < ebook.getBookList().size(); i++) {
									JDBookDetail book = ebook.getBookList()
											.get(i);
									all.add(book);
								}
								jdBookList.addAll(all);
								onLoadComplete();
								adapter.notifyDataSetChanged();
						}
						inLoadingMoreOnSearch = false;
					}

				});
	}

	private synchronized void searchPaperBooks(String key) {

		WebRequestHelper.get(
				URLText.JD_BASE_URL,
				RequestParamsPool.getSearchStoreEbookParams(currentSearchPage
						+ "", perSearchCount + "", key, 1 + ""),
				new MyAsyncHttpResponseHandler(OtherBookList.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						Toast.makeText(OtherBookList.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// TODO Auto-generated method stub

						String result = new String(responseBody);
						ebook = GsonUtils.fromJson(result, JDBook.class);

						if (ebook != null && ebook.getCode().equals("0")) {

							currentSearchPage++;

							if (ebook.getBookList() != null
									&& currentSearchPage >= ebook.totalPage) {
								noMoreBookOnSearch = true;
								booklist.setPullLoadEnable(false);
							} else {
								noMoreBookOnSearch = false;
								booklist.setPullLoadEnable(true);
							}

								List<JDBookDetail> all = new ArrayList<JDBookDetail>();
								for (int i = 0; i < ebook.getBookList().size(); i++) {
									JDBookDetail book = ebook.getBookList()
											.get(i);
									all.add(book);
								}
								jdBookList.addAll(all);
								onLoadComplete();
								adapter.notifyDataSetChanged();
						}
						inLoadingMoreOnSearch = false;
					}

				});
	}
	
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		if (!noMoreBookOnSearch) {
			if (!inLoadingMoreOnSearch) {
				inLoadingMoreOnSearch = true;
				if (bookType == 0) {
					searchBooks(author);
				} else if (bookType == 1) {
					searchPaperBooks(author);
				}
			}
		} else {
			onLoadComplete();
		}
	}
	
	private void onLoadComplete() {
		booklist.setPullLoadEnable(true);
		booklist.stopRefresh();
		booklist.stopLoadMore();
	}
	
	private class BookListAdapter extends BaseAdapter {

		private Context context;

		class ViewHolder {

			TextView bookTitle;
			TextView bookAuthor;
			TextView bookDesc;
			ImageView bookCover;
			ImageView imageViewLabel;
		}

		BookListAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return jdBookList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			final ViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.bookstore_search_list_item, parent, false);
				holder = new ViewHolder();
				holder.bookTitle = (TextView) convertView
						.findViewById(R.id.bookstore_search_user_book_name);
				holder.bookAuthor = (TextView) convertView
						.findViewById(R.id.bookstore_search_user_book_author);
				// holder.imageView = (ImageView) convertView
				// .findViewById(R.id.action_button);

				holder.bookCover = (ImageView) convertView
						.findViewById(R.id.bookstore_search_user_book_cover);

				holder.bookDesc = (TextView) convertView
						.findViewById(R.id.bookstore_search_book_desc);

				holder.imageViewLabel = (ImageView) convertView
						.findViewById(R.id.imageViewLabel);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final JDBookDetail eBook = jdBookList.get(position);
			holder.bookTitle.setText(eBook.getName());
			holder.bookAuthor
					.setText("null".equals(eBook.getAuthor()) ? getString(R.string.author_unknown)
							: eBook.getAuthor());
			String info = eBook.getInfo();
			if (info != null) {
				info = info.replaceAll("^[　 ]*", "");
				info = info.replaceAll("\\s+", "");
				holder.bookDesc.setText(info);
			} else {
				holder.bookDesc.setText("");
			}
			holder.bookDesc.setVisibility(View.VISIBLE);
			if (!eBook.isEBook()) {
				holder.imageViewLabel.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.badge_coverlabel_paper));
			} else {
				holder.imageViewLabel.setBackgroundDrawable(null);
			}
			ImageLoader.getInstance().displayImage(eBook.getImageUrl(),
					holder.bookCover, GlobalVarable.getCutBookDisplayOptions());
			return convertView;
		}
	}

}
