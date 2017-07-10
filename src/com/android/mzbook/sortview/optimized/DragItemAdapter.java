package com.android.mzbook.sortview.optimized;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.mzbook.sortview.model.BookShelfModel;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.util.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 书架拖动布局
 * @author WANGGUODONG
 *
 */
public class DragItemAdapter extends BaseAdapter {

	// 书架通用下载状态
	public static final int DOWNLOAD_STATUS = 100;// 下载的状态
	public static final int DOWNLOAD_STATUS_FAILED = 4;// 下载失败
	public static final int DOWNLOAD_STATUS_PAUSED = 6;// 下载暂停
	public static final int DOWNLOAD_STATUS_DOWNLOADED = 0;// 下载完成
	public static final int DOWNLOAD_STATUS_DOWNLOADING = 2;// 下载中

	// 书架通用下载状态标识
	public static final String DOWNLOAD_STATUS_STRING = "下载状态";// 下载的状态
	public static final String DOWNLOAD_STATUS_FAILED_STRING = "下载失败";// 下载失败
	public static final String DOWNLOAD_STATUS_PAUSED_STRING = "下载暂停";// 下载暂停
	public static final String DOWNLOAD_STATUS_DOWNLOADED_STRING = "下载完成";// 下载完成
	public static final String DOWNLOAD_STATUS_DOWNLOADING_STRING = "下载中";// 下载中

	private Context mContext;
	private LayoutInflater mLayoutInflater = null;

	private FrameLayout.LayoutParams layoutParams;
	private LinearLayout.LayoutParams subGridparams;
	private ImageSizeUtils utils;

	private List<DragItem> dragItems = new ArrayList<DragItem>();

	public DragItemAdapter(Context context, List<DragItem> plugins) {

		this.dragItems = plugins;
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		utils = new ImageSizeUtils(mContext);

		layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		layoutParams.height = (int) utils.getPerItemImageHeight();
		layoutParams.width = (int) utils.getPerItemImageWidth();

		subGridparams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		subGridparams.height = (int) utils.getPerSubGridItemImageHeight();
		subGridparams.width = (int) utils.getPerSubGridItemImageWidth();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		MZLog.d("wangguodong", "getView ---------");
		
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.dragview_item, null);
		}
		
		TextView itemDescription = ViewHolder.get(convertView, R.id.textViewDescription);
		ImageView itemIcon = ViewHolder.get(convertView, R.id.imageViewIcon);
		ImageView imageViewbg = ViewHolder.get(convertView, R.id.imageViewbg);
		GridView grid = ViewHolder.get(convertView, R.id.cover_grid);
		TextView bookSelected = ViewHolder.get(convertView, R.id.book_selected);
		ImageView bookSelectedCover = ViewHolder.get(convertView,R.id.book_selected_cover);
		TextView readProgress = ViewHolder.get(convertView, R.id.read_progress);
		TextView gifts = ViewHolder.get(convertView, R.id.gifts);
		// 添加下载封面进度
		FrameLayout downloadState = ViewHolder.get(convertView,R.id.download_state);
		TextView downloadStatus = ViewHolder.get(convertView,R.id.download_status);
		TextView downloadProgress = ViewHolder.get(convertView,R.id.download_progress);
		// 添加角标
		ImageView bookCoverLabel = ViewHolder.get(convertView,R.id.imageViewLabel);
		RelativeLayout mSelectedLayout = ViewHolder.get(convertView,R.id.mSelectedLayout);
		
		DragItem item =  dragItems.get(position);
		
		//设置尺寸
		itemIcon.setLayoutParams(layoutParams);
		grid.setLayoutParams(layoutParams);
		imageViewbg.setLayoutParams(layoutParams);
		downloadState.setLayoutParams(layoutParams);
		mSelectedLayout.setLayoutParams(layoutParams);
		
		itemDescription.setText(item.getMo().getBookName() + "\n");
		gifts.setVisibility(View.INVISIBLE);
		
		itemIcon.setImageResource(R.drawable.book_cover_default);
		imageViewbg.setImageResource(R.drawable.transparent);
		grid.setAdapter(null);
		

		String bookTypeString = item.getMo().getBookType();

		int bookCoverLabels = item.getMo().getBookCoverLabel();
		if (bookCoverLabels == BookShelfModel.LABEL_TRYREAD) {
			bookCoverLabel.setVisibility(View.VISIBLE);
			bookCoverLabel.setImageResource(R.drawable.badge_coverlabel_trial);
		} else if (bookCoverLabels == BookShelfModel.LABEL_CHANGDU) {
			bookCoverLabel.setVisibility(View.VISIBLE);
			bookCoverLabel.setImageResource(R.drawable.badge_coverlabel_vip);
		} else if (bookCoverLabels == BookShelfModel.LABEL_BORROWED) {
			bookCoverLabel.setVisibility(View.VISIBLE);
			bookCoverLabel.setImageResource(R.drawable.badge_coverlabel_borrow);
		} else {
			bookCoverLabel.setVisibility(View.GONE);
			bookCoverLabel.setImageResource(R.drawable.transparent);
		}

		if (item.isDownloaded()) {
			downloadState.setVisibility(View.GONE);

		} else {
			downloadState.setVisibility(View.VISIBLE);

			if (item.getMo().getDownload_state() == LocalBook.STATE_FAILED
					|| item.getMo().getDownload_state() == LocalBook.STATE_LOAD_FAILED) {
				downloadStatus.setText(DOWNLOAD_STATUS_FAILED_STRING);
				downloadProgress.setText("重试");
				downloadProgress.setTextColor(mContext.getResources().getColor(
						R.color.red_main));
			} else if (item.getMo().getDownload_state() == LocalBook.STATE_LOAD_PAUSED) {
				downloadStatus.setText(DOWNLOAD_STATUS_PAUSED_STRING);
				downloadProgress.setText("继续");
				downloadProgress.setTextColor(mContext.getResources().getColor(
						R.color.red_main));
			} else if (item.getMo().getDownload_state() == LocalBook.STATE_LOADING) {
				downloadStatus.setText(DOWNLOAD_STATUS_DOWNLOADING_STRING);

				if (item.getMo().getBook_size() == 0) {
					{
						downloadProgress.setText("0%");
						downloadProgress.setTextColor(mContext.getResources()
								.getColor(R.color.bg_main));
					}
				} else {
					downloadProgress.setText(item.getMo()
							.getDownload_progress()
							* 100
							/ item.getMo().getBook_size() * 1.0 + "%");
					downloadProgress.setTextColor(mContext.getResources()
							.getColor(R.color.bg_main));
				}

			} else {
				downloadStatus.setText(DOWNLOAD_STATUS_DOWNLOADED_STRING);
				downloadProgress.setText("");
			}

		}

		if ("ebook".equals(bookTypeString)) {
			itemIcon.setVisibility(View.VISIBLE);
			if (Math.round(item.getMo().getBookPercent()) == 0
					&& Math.round(item.getMo().getPercentTime()) == 0)
			{
				readProgress.setText("未读");
				Drawable drawable = mContext.getResources().getDrawable(R.drawable.red_dot);
				readProgress.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			}
			else{
				readProgress.setText(Math.round(item.getMo().getBookPercent())
						+ "%");
				readProgress.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			}
				
			String urlPath = item.getMo().getBookCover();
			
			if (!TextUtils.isEmpty(urlPath) && urlPath.startsWith("http://")) {
				ImageLoader.getInstance().displayImage(urlPath, itemIcon,GlobalVarable.getCutBookDisplayOptions(false));
			} else {
				if (urlPath == null) {
					urlPath = "";
				}
				File file = new File(urlPath);
				if (file.exists()) {
					ImageLoader.getInstance().displayImage("file://" + file.getPath(), itemIcon,
							GlobalVarable.getDefaultBookDisplayOptions());
				} else {
					ImageLoader.getInstance().displayImage("", itemIcon, GlobalVarable.getDefaultBookDisplayOptions());
				}
			}
		} else if ("document".equals(bookTypeString)) {
			itemIcon.setVisibility(View.VISIBLE);
			if (Math.round(item.getMo().getBookPercent()) == 0
					&& Math.round(item.getMo().getPercentTime()) == 0){
				readProgress.setText("未读");
				Drawable drawable = mContext.getResources().getDrawable(R.drawable.red_dot);
				readProgress.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			}
			else{
				readProgress.setText(Math.round(item.getMo().getBookPercent())
						+ "%");
				readProgress.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			}
				
			if (!UiStaticMethod.isEmpty(item.getMo().getBookCover())) {
				if (!TextUtils.isEmpty(item.getMo().getBookCover()) && item.getMo().getBookCover().startsWith("http://")) {
					ImageLoader.getInstance().displayImage(
							item.getMo().getBookCover(), itemIcon,
							GlobalVarable.getCutBookDisplayOptions(false));

				} else {
					File file = new File(item.getMo().getBookCover());
					if (file.exists()) {
						ImageLoader.getInstance().displayImage(
								"file://" + file.getPath(), itemIcon,
								GlobalVarable.getDefaultBookDisplayOptions());

					} else {
						itemIcon.setImageResource(R.drawable.book_cover_default);
					}
				}
			} else {
				itemIcon.setImageResource(R.drawable.book_cover_default);
			}
			

		} else if (item.isFolder() || "folder".equals(bookTypeString)) {
			readProgress.setText("共" + item.getMo().getDirBookCount() + "本书");
			itemIcon.setVisibility(View.INVISIBLE);
			itemIcon.setImageResource(R.drawable.transparent);
			imageViewbg.setVisibility(View.VISIBLE);
			grid.setVisibility(View.VISIBLE);
			grid.setClickable(false);
			ItemGridAdapter adapter = new ItemGridAdapter(
					MZBookDatabase.instance.getBooksInFolder(item.getMo()
							.getBookid(), 4, LoginUser.getpin()));
			grid.setAdapter(adapter);
			imageViewbg.setImageResource(R.drawable.bookshelf_folder);

		} else if (BookShelfModel.FREE_GIFTS.equals(bookTypeString)) {
			itemDescription.setText("");
			itemIcon.setVisibility(View.VISIBLE);
			gifts.setText(item.getMo().getBookName());
			itemIcon.setImageResource(R.drawable.bookshelf_gift);
			readProgress.setText("");
			grid.setVisibility(View.GONE);
			gifts.setVisibility(View.VISIBLE);
		

		} else if (BookShelfModel.MORE.equals(bookTypeString)) {
			itemDescription.setText("");
			itemIcon.setVisibility(View.VISIBLE);
			gifts.setText(item.getMo().getBookName());
			itemIcon.setImageResource(R.drawable.bookshelf_book);
			readProgress.setText("");
			grid.setVisibility(View.GONE);
			gifts.setVisibility(View.VISIBLE);

		} else {
			imageViewbg.setVisibility(View.INVISIBLE);
			itemIcon.setVisibility(View.VISIBLE);
			itemIcon.setImageResource(R.drawable.transparent);
			grid.setVisibility(View.GONE);
		
		}

		MZLog.d("wangguodong", "is selected:$$$$$"+item.isSelected()+ "is item.getSelectedBooksNum():$$$$$"+item.getSelectedBooksNum());
		if (item.isSelected() && item.getSelectedBooksNum() == -1) {
			mSelectedLayout.setVisibility(View.VISIBLE);
			bookSelectedCover.setVisibility(View.VISIBLE);
			bookSelected.setVisibility(View.INVISIBLE);
			bookSelected.setText("");
		} else if (item.isSelected() && item.getSelectedBooksNum() > 0) {
			mSelectedLayout.setVisibility(View.INVISIBLE);
			bookSelectedCover.setVisibility(View.INVISIBLE);
			bookSelected.setVisibility(View.VISIBLE);
			bookSelected.setText(item.getSelectedBooksNum() + "");
		} else {
			mSelectedLayout.setVisibility(View.INVISIBLE);
			bookSelectedCover.setVisibility(View.INVISIBLE);
			bookSelected.setVisibility(View.INVISIBLE);
			bookSelected.setText("");
		}

		return convertView;
	}

	class ItemGridAdapter extends BaseAdapter {

		private List<BookShelfModel> list = new ArrayList<BookShelfModel>();

		public ItemGridAdapter(List<BookShelfModel> li) {
			setList(li);
		}

		@Override
		public int getCount() {
			return list != null ? list.size() : 0;
		}

		@Override
		public Object getItem(int arg0) {
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View convertview, ViewGroup arg2) {
			if (convertview == null)
				convertview = LayoutInflater.from(mContext).inflate(
						R.layout.dragview_folder_grid_item, null);

			ImageView imageView = (ImageView) convertview
					.findViewById(R.id.bookcover);

			imageView.setLayoutParams(subGridparams);
			if (!UiStaticMethod.isEmpty(list.get(arg0).getBookCover())) {
				if (list.get(arg0).getBookType().equals("ebook")) {
					String urlPath = list.get(arg0).getBookCover();
					if (urlPath.startsWith("http://")) {
						ImageLoader.getInstance().displayImage(urlPath, imageView,
								GlobalVarable.getCutBookDisplayOptions(false));
						}
						else {
							
							File file = new File(urlPath);
							if (file.exists()) {
							ImageLoader.getInstance().displayImage(
									"file://" + file.getPath(),
									imageView,
									GlobalVarable.getDefaultBookDisplayOptions());
							}
							else {
								ImageLoader.getInstance().displayImage(
										"",
										imageView,
										GlobalVarable
												.getDefaultBookDisplayOptions());
							}
						}
					
					
					
				}
				if (list.get(arg0).getBookType().equals("document")) {

					if (list.get(arg0).getBookCover().startsWith("http://")) {

						ImageLoader.getInstance().displayImage(
								list.get(arg0).getBookCover(), imageView,
								GlobalVarable.getCutBookDisplayOptions(false));
						
						
					} else {
						File file = new File(list.get(arg0).getBookCover());
						if (file.exists()) {

							ImageLoader.getInstance().displayImage(
									"file://" + file.getPath(),
									imageView,
									GlobalVarable
											.getDefaultBookDisplayOptions());
						} else {
							ImageLoader.getInstance().displayImage(
									"",
									imageView,
									GlobalVarable
											.getDefaultBookDisplayOptions());
						}
					}

				}
			}

			return convertview;
		}

		public List<BookShelfModel> getList() {
			return list;
		}

		public void setList(List<BookShelfModel> list) {
			this.list = list;
		}

	}

	@Override
	public int getCount() {

		return dragItems == null ? 0 : dragItems.size();
	}

	@Override
	public Object getItem(int position) {

		return dragItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
}
