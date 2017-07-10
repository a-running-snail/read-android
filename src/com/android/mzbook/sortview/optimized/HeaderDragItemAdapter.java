package com.android.mzbook.sortview.optimized;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mzbook.sortview.model.BookShelfModel;
import com.android.mzbook.sortview.model.BookShelfModel.DownLoadType;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.album.ImageManager;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookshelf.BookcaseLocalFragmentNewUI;
import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.BookInforEDetail;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.entity.extra.JDEBook;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OlineDesUtils;
import com.jingdong.app.reader.onlinereading.OnlineReadManager;
import com.jingdong.app.reader.preloader.CutBitmapDisplayer;
import com.jingdong.app.reader.reading.EpubCover;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.MD5Util;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.SettingUtils;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.util.ViewHolder;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

/**
 * 书架拖动布局
 * @author WANGGUODONG
 *
 */
public class HeaderDragItemAdapter extends BaseAdapter {
	// 书架通用下载状态
	public static final int DOWNLOAD_STATUS = 100;// 下载的状态
	public static final int DOWNLOAD_STATUS_FAILED = 4;// 下载失败
	public static final int DOWNLOAD_STATUS_PAUSED = 6;// 下载暂停
	public static final int DOWNLOAD_STATUS_DOWNLOADED = 0;// 下载完成
	public static final int DOWNLOAD_STATUS_DOWNLOADING = 2;// 下载中

	// 书架通用下载状态标识
	public static final String DOWNLOAD_STATUS_STRING = "下载状态";// 下载的状态
	public static final String DOWNLOAD_STATUS_FAILED_STRING = "下载失败";// 下载失败
	public static final String DOWNLOAD_STATUS_PAUSED_STRING = "已暂停";// 下载暂停
	public static final String DOWNLOAD_STATUS_DOWNLOADED_STRING = "下载完成";// 下载完成
	public static final String DOWNLOAD_STATUS_DOWNLOADING_STRING = "下载中...";// 下载中

	private Context mContext;
	private LayoutInflater mLayoutInflater = null;
	private FrameLayout.LayoutParams layoutParams;
	private RelativeLayout.LayoutParams[] folderParams = new RelativeLayout.LayoutParams[4];
	private ImageSizeUtils utils;
	private RelativeLayout.LayoutParams[] layoutParamsArray = new RelativeLayout.LayoutParams[3];
	private List<DragItem> dragItems = new ArrayList<DragItem>();
	private BookcaseLocalFragmentNewUI mBookcaseLocalFragmentNewUI;
	private int mHidePosition = -1;
	/** 图片缓存cache */
	private LruCache<String, Bitmap> mLruCache = null;

	public HeaderDragItemAdapter(Context context, List<DragItem> plugins, BookcaseLocalFragmentNewUI _mBookcaseLocalFragmentNewUI) {
		this.mBookcaseLocalFragmentNewUI = _mBookcaseLocalFragmentNewUI;
		this.dragItems = plugins;
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		utils = new ImageSizeUtils(mContext);
		initViewParams();
		initCache();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mLayoutInflater.inflate(R.layout.header_dragview_item, null);
		//书名
		TextView itemDescription = ViewHolder.get(convertView, R.id.textViewDescription);
		//图书封面
		ImageView itemIcon = ViewHolder.get(convertView, R.id.imageViewIcon);
		//背景图
		ImageView imageViewbg = ViewHolder.get(convertView, R.id.imageViewbg);
		//阅读进度
		TextView readProgress = ViewHolder.get(convertView, R.id.read_progress);
		TextView gifts = ViewHolder.get(convertView, R.id.gifts);
		FrameLayout imageViewFrame = ViewHolder.get(convertView,R.id.imageViewFrame);
		// 添加下载封面进度
		final FrameLayout downloadState = ViewHolder.get(convertView,R.id.download_state);
		TextView downloadStatus = ViewHolder.get(convertView,R.id.download_status);
		TextView downloadProgress = ViewHolder.get(convertView,R.id.download_progress);
		ProgressBar progress_horizontal = ViewHolder.get(convertView,R.id.progress_horizontal);
		ImageView download_status_btn = ViewHolder.get(convertView,R.id.download_status_btn);
		RelativeLayout downloading_layout = ViewHolder.get(convertView,R.id.downloading_layout);
		LinearLayout download_finish_layout = ViewHolder.get(convertView,R.id.download_finish_layout);
		ImageView download_status_icon = ViewHolder.get(convertView,R.id.download_status_icon);
		TextView download_finish_text = ViewHolder.get(convertView, R.id.download_finish_text);
		final ImageView update_download_btn = ViewHolder.get(convertView,R.id.update_download_btn);
		final LinearLayout mBuyedLayout = ViewHolder.get(convertView,R.id.mBuyedLayout);
		RelativeLayout mFolderLayout = ViewHolder.get(convertView, R.id.mFolderLayout);
		
		//数据
		final DragItem item =  dragItems.get(position);
		//更新整理模式的编辑状态
		updateEditStatus(convertView, item); 
		
		if(MZBookApplication.isPad()) {
			itemDescription.setTextSize(14);	
			readProgress.setTextSize(14);	
		}
		
		//最近购买
		mBuyedLayout.setVisibility(View.GONE);
		//下载中
		downloading_layout.setVisibility(View.VISIBLE);
		//下载完成
		download_finish_layout.setVisibility(View.GONE);
		
		// 添加角标
		ImageView bookCoverLabel = ViewHolder.get(convertView,R.id.imageViewLabel);
		
		final int index = position;
		//下载按钮事件
		update_download_btn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					SettingUtils.getInstance().putLong("change_time" + item.getMo().getServerid(), (long)item.getMo().getModifiedTime());
					
					mBookcaseLocalFragmentNewUI.deleteBooks(item, index);
					//下载中
					dragItems.get(index).getMo().setDownload_state(LocalBook.STATE_LOADING);
					updateDownloadEbook(item.getMo().getServerid(), item.getMo().getBookCoverLabel(), item.getMo().getDownloadType());
				}
				return true;
			}
		});
		
		//设置尺寸
		int margin = 0;
		if ((position %3) == 0)  {
			margin = ScreenUtils.dip2px(16);
			itemDescription.setPadding(margin, 0, 0, 0);
			readProgress.setPadding(margin, 0, 0, 0);
		}
		else if((position %3) == 2)	{
			margin = ScreenUtils.dip2px(16);
			itemDescription.setPadding(0, 0, margin, 0);
			readProgress.setPadding(0, 0, margin, 0);
		}
		else if((position %3) == 1) {
			margin = ScreenUtils.dip2px(11);
			itemDescription.setPadding(margin, 0, margin, 0);
			readProgress.setPadding(margin, 0, margin, 0);
		}
		
		mFolderLayout.setLayoutParams(layoutParamsArray[(position %3)]);
		imageViewFrame.setLayoutParams(layoutParamsArray[(position %3)]);
		itemIcon.setLayoutParams(layoutParams);
		imageViewbg.setLayoutParams(layoutParamsArray[(position %3)]);
		downloadState.setLayoutParams(layoutParamsArray[(position %3)]);
		itemDescription.setText(item.getMo().getBookName() + "\n");
		gifts.setVisibility(View.INVISIBLE);
		
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
		} else if (bookCoverLabels == BookShelfModel.LABEL_USER_BORROWED) {
			bookCoverLabel.setVisibility(View.VISIBLE);
			bookCoverLabel.setImageResource(R.drawable.badge_coverlabel_borrow);
		} else {
			bookCoverLabel.setVisibility(View.GONE);
		}
		
		downloadState.setVisibility(View.GONE);
		update_download_btn.setVisibility(View.GONE);

		if (item.isDownloaded()) {
			downloading_layout.setVisibility(View.GONE);
			download_finish_layout.setVisibility(View.VISIBLE);
			downloadState.setVisibility(View.GONE);
			if(!item.isFolder() && (item.getMo().getBookCoverLabel() != BookShelfModel.LABEL_TRYREAD)) {
				if(item.getMo().getDownloadType() == DownLoadType.DownLoad) {
					update_download_btn.setBackgroundResource(R.drawable.icon_e);
					update_download_btn.setVisibility(View.VISIBLE);
				}else if(item.getMo().getDownloadType() == DownLoadType.Update) {
					update_download_btn.setBackgroundResource(R.drawable.icon_e);
					update_download_btn.setVisibility(View.VISIBLE);
				}else if(item.getMo().getDownloadType() == DownLoadType.Buyed) {
					mBuyedLayout.setVisibility(View.VISIBLE);
				}
			}
		} else {				
			
			imageViewbg.setImageResource(R.drawable.transparent);
			
			if (item.getMo().getDownload_state() == LocalBook.STATE_FAILED
					|| item.getMo().getDownload_state() == LocalBook.STATE_LOAD_FAILED) {
				downloadState.setVisibility(View.VISIBLE);
				downloading_layout.setVisibility(View.GONE);
				download_finish_layout.setVisibility(View.VISIBLE);
				download_status_icon.setBackgroundResource(R.drawable.icon_f);
				download_finish_text.setText("网络出错啦\n点击重试");
				download_status_btn.setBackgroundResource(R.drawable.icon_h);
			} else if (item.getMo().getDownload_state() == LocalBook.STATE_LOAD_PAUSED) {
				downloadState.setVisibility(View.VISIBLE);
				downloadStatus.setText("已暂停");
				download_status_btn.setBackgroundResource(R.drawable.icon_g);
				
				//处理更新的时候起始状态显示“暂停状态”问题
				boolean startDownloading = SettingUtils.getInstance().getBoolean("startDownloading:" + item.getMo().getServerid(), false);
				boolean isUpdate = SettingUtils.getInstance().getBoolean(""+item.getMo().getServerid());
				if(startDownloading) {
					int progress = 0;
					if (item.getMo().getBook_size() > 0) {
						progress = (int)(item.getMo().getDownload_progress()* 100/ item.getMo().getBook_size() * 1.0);
					}
					
					if(0 == progress) {
						if(isUpdate) {
							downloadStatus.setText("更新中...");
						}else {
							downloadStatus.setText("下载中...");	
						}
						download_status_btn.setBackgroundResource(R.drawable.icon_h);
					}
					
					downloadProgress.setText( progress + "%");
					downloadProgress.setTextColor(mContext.getResources().getColor(R.color.bg_main));
					progress_horizontal.setProgress(progress);
				}
			} else if (item.getMo().getDownload_state() == LocalBook.STATE_LOADING) {
				downloadState.setVisibility(View.VISIBLE);
				boolean isUpdate = SettingUtils.getInstance().getBoolean(""+item.getMo().getServerid());
				if(isUpdate) {
					downloadStatus.setText("更新中...");
				}else {
					downloadStatus.setText(DOWNLOAD_STATUS_DOWNLOADING_STRING);	
				}

				progress_horizontal.setMax(100);
				if (item.getMo().getBook_size() == 0) {
					downloadProgress.setText("0%");
					progress_horizontal.setProgress(0);
					downloadProgress.setTextColor(mContext.getResources().getColor(R.color.bg_main));
				} else {
					int progress = (int)(item.getMo().getDownload_progress()* 100/ item.getMo().getBook_size() * 1.0);
					downloadProgress.setText( progress + "%");
					downloadProgress.setTextColor(mContext.getResources().getColor(R.color.bg_main));
					progress_horizontal.setProgress(progress);
					download_status_btn.setBackgroundResource(R.drawable.icon_h);
				}

			} else {
//				downloadStatus.setText(DOWNLOAD_STATUS_DOWNLOADED_STRING);
				downloadProgress.setText("");
			}

		}

		if ("ebook".equals(bookTypeString)) {
			itemIcon.setVisibility(View.VISIBLE);
			if (Math.round(item.getMo().getBookPercent()) == 0
					&& Math.round(item.getMo().getPercentTime()) == 0) {
					readProgress.setText("未读");
					Drawable drawable = mContext.getResources().getDrawable(R.drawable.red_dot);
					readProgress.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			} else {
				readProgress.setText((int)(item.getMo().getBookPercent())+ "%");
				readProgress.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			}
			loadImage(itemIcon, item.getMo().getBookCover(), item.getMo().getBookName());
		} else if ("document".equals(bookTypeString)) {
			itemIcon.setVisibility(View.VISIBLE);
			if (Math.round(item.getMo().getBookPercent()) == 0
					&& Math.round(item.getMo().getPercentTime()) == 0){
				readProgress.setText("未读");
				Drawable drawable = mContext.getResources().getDrawable(R.drawable.red_dot);
				readProgress.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			}
			else{
				readProgress.setText(Math.round(item.getMo().getBookPercent()) + "%");
				readProgress.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			}
			loadImage(itemIcon, item.getMo().getBookCover(), item.getMo().getBookName());
		} else if (item.isFolder() || "folder".equals(bookTypeString)) {
			readProgress.setText("共" + item.getMo().getDirBookCount() + "本书");
			itemIcon.setVisibility(View.INVISIBLE);
			itemIcon.setImageResource(R.drawable.transparent);
			imageViewbg.setVisibility(View.VISIBLE);
			imageViewbg.setImageResource(R.drawable.bookshelf_folder);
			mFolderLayout.setVisibility(View.VISIBLE);
			ImageView mFolderImage1 = ViewHolder.get(convertView,R.id.mFolderImage1);
			ImageView mFolderImage2 = ViewHolder.get(convertView,R.id.mFolderImage2);
			ImageView mFolderImage3 = ViewHolder.get(convertView,R.id.mFolderImage3);
			ImageView mFolderImage4 = ViewHolder.get(convertView,R.id.mFolderImage4);
			mFolderImage1.setVisibility(View.GONE);
			mFolderImage2.setVisibility(View.GONE);
			mFolderImage3.setVisibility(View.GONE);
			mFolderImage4.setVisibility(View.GONE);
			
			List<BookShelfModel> folderdata = MZBookDatabase.instance.getBooksInFolder(item.getMo().getBookid(), 4, LoginUser.getpin());
			for(int i=0; i<folderdata.size(); i++) {
				String urlPath = folderdata.get(i).getBookCover();
				if(0 == i) {
					loadImage(mFolderImage1, urlPath, folderdata.get(i).getBookName());
					mFolderImage1.setLayoutParams(folderParams[0]);
					mFolderImage1.setVisibility(View.VISIBLE);
				}else if(1 == i) {
					loadImage(mFolderImage2, urlPath, folderdata.get(i).getBookName());
					mFolderImage2.setLayoutParams(folderParams[1]);
					mFolderImage2.setVisibility(View.VISIBLE);
				}else if(2 == i) {
					loadImage(mFolderImage3, urlPath, folderdata.get(i).getBookName());
					mFolderImage3.setLayoutParams(folderParams[2]);
					mFolderImage3.setVisibility(View.VISIBLE);
				}else {
					loadImage(mFolderImage4, urlPath, folderdata.get(i).getBookName());
					mFolderImage4.setLayoutParams(folderParams[3]);
					mFolderImage4.setVisibility(View.VISIBLE);
				}
			}

		} else if (BookShelfModel.FREE_GIFTS.equals(bookTypeString)) {
			itemDescription.setText("");
			itemIcon.setVisibility(View.VISIBLE);
			gifts.setText(item.getMo().getBookName());
			itemIcon.setImageResource(R.drawable.bookshelf_gift);
			readProgress.setText("");
			readProgress.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			gifts.setVisibility(View.VISIBLE);
			mFolderLayout.setVisibility(View.GONE);

		} else if (BookShelfModel.MORE.equals(bookTypeString)) {
			itemDescription.setText("");
			itemIcon.setVisibility(View.VISIBLE);
			gifts.setText(item.getMo().getBookName());
			itemIcon.setImageResource(R.drawable.bookshelf_book);
			readProgress.setText("");
			readProgress.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			gifts.setVisibility(View.VISIBLE);
			imageViewbg.setVisibility(View.INVISIBLE);
			mFolderLayout.setVisibility(View.GONE);
			
			if (mBookcaseLocalFragmentNewUI.getIsSelected()) {
				convertView.setVisibility(View.INVISIBLE);
			}else {
				convertView.setVisibility(View.VISIBLE);
			}
		} else {
			imageViewbg.setVisibility(View.INVISIBLE);
			itemIcon.setVisibility(View.VISIBLE);
			itemIcon.setImageResource(R.drawable.transparent);
			mFolderLayout.setVisibility(View.GONE);
		}
		
		if(position == mHidePosition){
			convertView.setVisibility(View.INVISIBLE);
		}
		
		return convertView;
	}
	
	public void setHideItem(int hidePosition) {
		this.mHidePosition = hidePosition; 
		notifyDataSetChanged();
	}
	
	/**
	 * 检查当前数据项是否被选中
	 * @param item
	 * @return
	 */
	private boolean selectedListIsExits(DragItem item) {
		boolean exits = false;
		List<DragItem> mSelectedList = mBookcaseLocalFragmentNewUI.getSelectedList();
		for (int j=0; j<mSelectedList.size(); j++) {
			DragItem sdata = mSelectedList.get(j);
			long dec_bookid = item.getMo().getServerid();
			long src_bookid = sdata.getMo().getServerid();
			if((0 == dec_bookid) && (0 == src_bookid)) {
				String dec_bookcover = item.getMo().getBookCover();
				String src_bookcover = sdata.getMo().getBookCover();
				if (!TextUtils.isEmpty(dec_bookcover) && !TextUtils.isEmpty(src_bookcover)) {
					if (dec_bookcover.equals(src_bookcover)) {
						exits = true;	
						break;
					}
				}else {
					int dec_id = item.getMo().getId();
					int src_id = sdata.getMo().getId();
					if (dec_id == src_id) {
						exits = true;	
						break;
					}
				}
			}else {
				if(dec_bookid == src_bookid) {
					exits = true;	
					break;
				}
			}
			
		}
		
		return exits;
	}
	
	/**
	 * 更新整理模式的编辑状态
	 * @param convertView
	 * @param item
	 */
	private void updateEditStatus(View convertView, DragItem item) {
		ImageView touming = ViewHolder.get(convertView, R.id.touming);
		RelativeLayout mSelectedLayout = ViewHolder.get(convertView, R.id.mSelectedLayout);
		
		//当前是否为编辑状态
		touming.setVisibility(View.GONE);
		if (mBookcaseLocalFragmentNewUI.getIsSelected()) {
			ImageView book_selected_cover = ViewHolder.get(convertView, R.id.book_selected_cover);
			TextView book_selected = ViewHolder.get(convertView, R.id.book_selected);
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ScreenUtils.dip2px(24), ScreenUtils.dip2px(24));
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			int top = utils.getPerItemImageHeight() - ScreenUtils.dip2px(34);
			params.setMargins(0, top, 0, 0);
			book_selected_cover.setLayoutParams(params);
			book_selected.setLayoutParams(params);
			
			if (item.isFolder()) {
				mSelectedLayout.setVisibility(View.GONE);
				
				String userId = LoginUser.getpin();
				List<BookShelfModel> models = MZBookDatabase.instance.getBooksInFolder(item.getMo().getBookid(), -1, userId);
				List<DragItem> subMitems = new ArrayList<DragItem>();
				Collections.sort(models, new TimeComparator());
				for (int r = 0; r < models.size(); r++) {
					if (models.get(r).getDownload_state() == LocalBook.STATE_FAILED || models.get(r).getDownload_state() == LocalBook.STATE_LOAD_PAUSED
							|| models.get(r).getDownload_state() == LocalBook.STATE_LOADING || models.get(r).getDownload_state() == LocalBook.STATE_LOAD_FAILED) {
						subMitems.add(new DragItem(models.get(r), false, false, -1, false));
					} else {
						subMitems.add(new DragItem(models.get(r), false, false, -1, true));
					}
				}
				int number=0;
				for(int i=0; i<subMitems.size(); i++) {
					if(selectedListIsExits(subMitems.get(i))) {
						number++;
					}
				}
				if(number > 0) {
					mSelectedLayout.setVisibility(View.VISIBLE);
					book_selected_cover.setVisibility(View.GONE);
					book_selected.setVisibility(View.VISIBLE);
					book_selected.setText("" + number);
				}
			}else {
				book_selected_cover.setVisibility(View.VISIBLE);
				book_selected.setVisibility(View.GONE);
				if (selectedListIsExits(item) && (item.getMo().getBookType() != BookShelfModel.MORE)) {
					mSelectedLayout.setVisibility(View.VISIBLE);
				}else {
					mSelectedLayout.setVisibility(View.GONE);
				}
			}
			
		}else {
			mSelectedLayout.setVisibility(View.GONE);
		}
	}
	
	class TimeComparator implements Comparator<BookShelfModel> {

		@Override
		public int compare(BookShelfModel lhs, BookShelfModel rhs) {
			double time1 = (double) lhs.getModifiedTime();
			double time2 = (double) rhs.getModifiedTime();
			// 降序排列
			if (time1 < time2)
				return 1;
			if (time1 > time2)
				return -1;
			return 0;
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
		return position;
	}
	
	/**
	 * 加在网络或本地图片
	 * @param image 
	 * @param url 
	 */
	private void loadImage(final ImageView image, final String url, String name) {
		image.setBackgroundResource(R.drawable.bg_default_cover);
		if (UiStaticMethod.isEmpty(url)) {
			String p = MZBookApplication.getInstance().getCachePath() + "/downloads/" + MD5Util.md5Hex(name) + "/";
			Bitmap mBitmap = getBitmap(p);
			if (null != mBitmap) {
				image.setImageBitmap(mBitmap);
			}else {
				if (mContext.getResources() != null) {
					File dir = new File(p);
					if(!dir.exists()) {
						dir.mkdirs();
					}
					Bitmap bitmap = ImageManager.getBitmapFromCache(p + "/tempCover.png");
					if (null == bitmap) {
						String path = EpubCover.generateCover(mContext, p, name);
						if (!TextUtils.isEmpty(path)) {
							Bitmap b = ImageManager.getBitmapFromCache(path);
							if (null != b) {
								putBitmap(p, b);
								image.setImageBitmap(b);	
							}
						}
					} else {
						putBitmap(p, bitmap);
						image.setImageBitmap(bitmap);
					}
				}
			}
			
			return;
		}
		
		Bitmap mBitmap = getBitmap(url);
		if (null != mBitmap) {
			image.setImageBitmap(mBitmap);
		}else {
			final String urlstr;
			if (!url.startsWith("http://")) {
				urlstr = "file://" + url;
			}else {
				urlstr = url;
			}
			ImageLoader.getInstance().displayImage(urlstr, image, GlobalVarable.getCutBookDisplayOptions(false), new ImageLoadingListener() {
				@Override public void onLoadingStarted(String arg0, View arg1) { }
				@Override public void onLoadingFailed(String arg0, View arg1, FailReason arg2) { 
					if (mContext.getResources() != null) {
						arg1.setBackgroundDrawable(ImageUtils.overlay(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.book_cover_default),
								BitmapFactory.decodeResource(mContext.getResources(), R.drawable.overlay)));
					}
				}
				@Override public void onLoadingCancelled(String arg0, View arg1) { }
				
				@Override
				public void onLoadingComplete(String arg0, View arg1, Bitmap bitmap) {
					if(null != bitmap) {
						Bitmap b = CutBitmapDisplayer.CropForExtraWidth(bitmap, false);
						putBitmap(url, b);
						image.setImageBitmap(b);
					}
				}
			});	
		}
			
	}
	
	/**
	* @Description: 更新下载电子书
	* @param @param ebookid 电子书id
	* @param @param bookCoverLabel 电子书类型
	* @return void
	* @author xuhongwei1
	* @date 2015年10月12日 下午4:50:23 
	* @throws 
	*/ 
	public void updateDownloadEbook(long ebookid, final int bookCoverLabel, final DownLoadType type) {
		//请求图书信息
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getBookInfoParams(ebookid,null), 
				true, new MyAsyncHttpResponseHandler(mContext) {
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				String result = new String(responseBody);
				try {
					JSONObject json = new JSONObject(result);
					String codeStr = json.optString("code");
					if (codeStr != null && codeStr.equals("0")) {
						//电子书信息
						JDBookInfo bookInfo = GsonUtils.fromJson(result, JDBookInfo.class);
						if(null == bookInfo) {
							return;
						}
						
						if (bookCoverLabel == BookShelfModel.LABEL_TRYREAD) {
							//试读不需要更新
						}else if (bookCoverLabel == BookShelfModel.LABEL_CHANGDU) {
							updateChangDuBook(bookInfo, type);
						} else if (bookCoverLabel == BookShelfModel.LABEL_BORROWED) {
							updateBorrowBook(bookInfo, type);
						} else if (bookCoverLabel == BookShelfModel.LABEL_USER_BORROWED) {
							updateUserBorrowBook(bookInfo, type);
						}else {
							updateBuyBook(bookInfo, type);
						}
					} else {
						Toast.makeText(mContext, mContext.getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		});
	}
	
	/**
	* @Description: 下载更新畅读书籍
	* @param @param bookInfo
	* @return void
	* @author xuhongwei1
	* @date 2015年10月13日 下午2:09:44 
	* @throws 
	*/ 
	public void updateChangDuBook(JDBookInfo bookInfo, final DownLoadType type) {
		SettingUtils.getInstance().putBoolean("startDownloading:" + bookInfo.detail.bookId, true);
		if(DownLoadType.Update == type) {
			SettingUtils.getInstance().putBoolean(""+bookInfo.detail.bookId, true);
		}
		BookInforEDetail bookE = new BookInforEDetail();// 下载实体
		bookE.bookid = bookInfo.detail.bookId;// bookid
		bookE.picUrl = bookInfo.detail.logo;// 小图
		bookE.largeSizeImgUrl = bookInfo.detail.largeLogo;// 大图
		bookE.bookType = LocalBook.TYPE_EBOOK;
		// 书的类型:电子书or多媒体书
		bookE.formatName = bookInfo.detail.format;// 图书格式。
		bookE.author = bookInfo.detail.author;// 作者
		bookE.bookName = bookInfo.detail.bookName;// 书名
		bookE.size = bookInfo.detail.size + "";
		OnlineReadManager.requestServer2ReadOnline(bookE, (Activity)mContext, null, false, null);			
	}
	
	/**
	* @Description: 下载更新借阅书籍
	* @param @param bookInfo 
	* @return void
	* @author xuhongwei1
	* @date 2015年10月13日 下午2:04:31 
	* @throws 
	*/ 
	public void updateBorrowBook(JDBookInfo bookInfo, final DownLoadType type) {
		SettingUtils.getInstance().putBoolean("startDownloading:" + bookInfo.detail.bookId, true);
		if(DownLoadType.Update == type) {
			SettingUtils.getInstance().putBoolean(""+bookInfo.detail.bookId, true);
		}
		OrderEntity orderEntity = new OrderEntity();
		orderEntity.name = bookInfo.detail.bookName;
		orderEntity.bookId = bookInfo.detail.bookId;
		orderEntity.orderId = bookInfo.detail.bookId;
		orderEntity.formatName = bookInfo.detail.format;
		orderEntity.author = bookInfo.detail.author;
		orderEntity.orderStatus = 16;
		orderEntity.price = bookInfo.detail.jdPrice;
		orderEntity.picUrl = bookInfo.detail.logo;
		orderEntity.bigPicUrl = bookInfo.detail.largeLogo;
		orderEntity.borrowEndTime = OlineDesUtils.encrypt(bookInfo.detail.userBorrowEndTime);
		
		DownloadTool.download((Activity)mContext,
				orderEntity, null, false, LocalBook.SOURCE_BORROWED_BOOK, 0,
				true, null, false);
	}
	
	/**
	* @Description: 下载更新用户借阅书籍
	* @param @param bookInfo 
	* @return void
	* @author xuhongwei1
	* @date 2015年10月13日 下午2:04:31 
	* @throws 
	*/ 
	public void updateUserBorrowBook(JDBookInfo bookInfo, final DownLoadType type) {
		SettingUtils.getInstance().putBoolean("startDownloading:" + bookInfo.detail.bookId, true);
		if(DownLoadType.Update == type) {
			SettingUtils.getInstance().putBoolean(""+bookInfo.detail.bookId, true);
		}
		OrderEntity orderEntity = new OrderEntity();
		orderEntity.name = bookInfo.detail.bookName;
		orderEntity.bookId = bookInfo.detail.bookId;
		orderEntity.orderId = bookInfo.detail.bookId;
		orderEntity.formatName = bookInfo.detail.format;
		orderEntity.author = bookInfo.detail.author;
		orderEntity.orderStatus = 16;
		orderEntity.price = bookInfo.detail.jdPrice;
		orderEntity.picUrl = bookInfo.detail.logo;
		orderEntity.bigPicUrl = bookInfo.detail.largeLogo;
		orderEntity.userBuyBorrowEndTime = OlineDesUtils.encrypt(bookInfo.detail.userBuyBorrowEndTime);
		
		DownloadTool.download((Activity)mContext,orderEntity, null, false, LocalBook.SOURCE_USER_BORROWED_BOOK, 0,true, null, false);
	}
	
	/**
	* @Description: 下载更新已购书籍
	* @param @param bookInfo
	* @return void
	* @author xuhongwei1
	* @date 2015年10月13日 下午2:52:59 
	* @throws 
	*/ 
	public void updateBuyBook(JDBookInfo bookInfo, final DownLoadType type) {
		SettingUtils.getInstance().putBoolean("startDownloading:" + bookInfo.detail.bookId, true);
		if(DownLoadType.Update == type) {
			SettingUtils.getInstance().putBoolean(""+bookInfo.detail.bookId, true);
		}
		
		//实体信息转换
		JDEBook bookEntity = OrderEntity.FromJDBooKInfo2JDEBook(bookInfo.detail);
		OrderEntity orderEntity = OrderEntity.FromJDBooK2OrderEntity(bookEntity);
		//下载
		DownloadTool.download((Activity)mContext, orderEntity, null, false, LocalBook.SOURCE_BUYED_BOOK, 0, false,null,false);
	}
	
	
	/** 
	 * 初始化相关控件参数
	 */
	private void initViewParams() {
		layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.height = (int) utils.getPerItemImageHeight();
		layoutParams.width = (int) utils.getPerItemImageWidth();
		
		RelativeLayout.LayoutParams folder_params_left_top = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		folder_params_left_top.height = (int) utils.getPerSubGridItemImageHeight();
		folder_params_left_top.width = (int) utils.getPerSubGridItemImageWidth();
		folder_params_left_top.topMargin = ScreenUtils.dip2px(16);
		folder_params_left_top.leftMargin = ScreenUtils.dip2px(8);
		folder_params_left_top.rightMargin = ScreenUtils.dip2px(8);
		folder_params_left_top.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		folder_params_left_top.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	
		RelativeLayout.LayoutParams folder_params_right_top = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		folder_params_right_top.height = (int) utils.getPerSubGridItemImageHeight();
		folder_params_right_top.width = (int) utils.getPerSubGridItemImageWidth();
		folder_params_right_top.topMargin = ScreenUtils.dip2px(16);
		folder_params_right_top.rightMargin = ScreenUtils.dip2px(8);
		folder_params_right_top.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		folder_params_right_top.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		
		RelativeLayout.LayoutParams folder_params_left_bottom = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		folder_params_left_bottom.height = (int) utils.getPerSubGridItemImageHeight();
		folder_params_left_bottom.width = (int) utils.getPerSubGridItemImageWidth();
		folder_params_left_bottom.leftMargin = ScreenUtils.dip2px(8);
		folder_params_left_bottom.rightMargin = ScreenUtils.dip2px(8);
		folder_params_left_bottom.bottomMargin = ScreenUtils.dip2px(8);
		folder_params_left_bottom.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		folder_params_left_bottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		
		RelativeLayout.LayoutParams folder_params_right_bottom = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		folder_params_right_bottom.height = (int) utils.getPerSubGridItemImageHeight();
		folder_params_right_bottom.width = (int) utils.getPerSubGridItemImageWidth();
		folder_params_right_bottom.rightMargin = ScreenUtils.dip2px(8);
		folder_params_right_bottom.bottomMargin = ScreenUtils.dip2px(8);
		folder_params_right_bottom.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		folder_params_right_bottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		
		folderParams[0] = folder_params_left_top;
		folderParams[1] = folder_params_right_top;
		folderParams[2] = folder_params_left_bottom;
		folderParams[3] = folder_params_right_bottom;
		
		RelativeLayout.LayoutParams layoutParams_left = new RelativeLayout.LayoutParams(utils.getPerItemImageWidth(), utils.getPerItemImageHeight());
		int margin = ScreenUtils.dip2px(16);
		layoutParams_left.leftMargin=margin;
		layoutParams_left.rightMargin=0;
		
		RelativeLayout.LayoutParams layoutParams_right = new RelativeLayout.LayoutParams(utils.getPerItemImageWidth(), utils.getPerItemImageHeight());
		layoutParams_right.leftMargin=0;
		layoutParams_right.rightMargin= margin;
		layoutParams_right.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		
		RelativeLayout.LayoutParams layoutParams_middle = new RelativeLayout.LayoutParams(utils.getPerItemImageWidth(), utils.getPerItemImageHeight());
		margin =ScreenUtils.dip2px(11);
		layoutParams_middle.leftMargin=margin;
		layoutParams_middle.addRule(RelativeLayout.CENTER_HORIZONTAL);
		layoutParamsArray[0] =layoutParams_left;
		layoutParamsArray[1] = layoutParams_middle;
		layoutParamsArray[2] = layoutParams_right;
	}
	
	private void initCache() {
		int maxSize = (int)(Runtime.getRuntime().maxMemory()/10);
		mLruCache = new LruCache<String, Bitmap>(maxSize){  
		    @Override  
		    protected int sizeOf(String key, Bitmap bitmap) {  
		    	if (bitmap == null) {
		    		return 0;
		    	}
		    	return bitmap.getRowBytes() * bitmap.getHeight();
		    }  
		};  
	}
	
	public Bitmap getBitmap(String filename) {
		return mLruCache.get(filename);
	}
	
	public void putBitmap(String filename, Bitmap mBitmap) {
		mLruCache.put(filename, mBitmap);
	}

}
