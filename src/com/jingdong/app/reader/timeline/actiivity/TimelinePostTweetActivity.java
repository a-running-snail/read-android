package com.jingdong.app.reader.timeline.actiivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.DraftsActivity;
import com.jingdong.app.reader.activity.DraftsActivity.Draft;
import com.jingdong.app.reader.activity.DraftsActivity.Drafts;
import com.jingdong.app.reader.album.AlbumActivity;
import com.jingdong.app.reader.album.AlbumManager;
import com.jingdong.app.reader.album.ImageData;
import com.jingdong.app.reader.album.PreviewPhotoActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.preloader.CutBitmapDisplayer;
import com.jingdong.app.reader.timeline.model.TimelineActivityModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.tendcloud.tenddata.TCAgent;

public class TimelinePostTweetActivity extends BaseActivityWithTopBar implements
		TopBarViewListener {

	public static final String TITLE = "title";
	public static final String SHOW_AT = "showAt";
	public static final String IMAGE_LISTS = "image_lists";
	public static final String TWEET_CONTENT = "user_tweet[content]";
	public static final String TWEET_BOOKS = "user_tweet[books]";
	public static final String TWEET_USERS = "user_tweet[users]";
	public static final String BOOK_LIST = "is_book_list";
	public static final String BOOK_BAR_ID = "bar_id";
	public static final int POST_TWEET_WORDS = 1000;
	public static final int SEARCH_PEOPLE = 500;
	public static final int SEARCH_BOOK = 700;
	private static final int MAX_ENTITY_TEXT = 60000;
	private static final String AT = "@";
	private static final String BLANK = " ";
	private EditText editText;
	private boolean postTweet = true;
	private TopBarView topBarView = null;

	private List<Map<String, String>> list = new ArrayList<Map<String, String>>();

	private StringBuffer bookIdBuffer;
	private StringBuffer userIdBuffer;
	
	private GridView mGridView = null;
	private GridAdapter mGridAdapter = null;
	/** 草稿箱中选择的图片路径列表 */
	private List<ImageData> photoPathList = new ArrayList<ImageData>();
	/** 保存跳转的来源 */
	private String from = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline_post_tweet);
		initField();

		from = getIntent().getStringExtra("from");
		topBarView = getTopBarView();
		initTopBar();

		bookIdBuffer = new StringBuffer();

		userIdBuffer = new StringBuffer();
		findViewById(R.id.mention_book).setOnClickListener(
				new OnClickListener() {
					// 提到某本书
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(
								TimelinePostTweetActivity.this,
								TimelineSearchBookActivity.class);
						startActivityForResult(intent, SEARCH_BOOK);
					}
				});
		editText = (EditText) findViewById(R.id.timeline_post_tweet);
		if (postTweet)
			findViewById(R.id.timeline_tweet_at).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(
									TimelinePostTweetActivity.this,
									TimelineSearchPeopleActivity.class);
							startActivityForResult(intent, SEARCH_PEOPLE);
						}
					});
		else
			findViewById(R.id.timeline_tweet_at).setVisibility(View.INVISIBLE);

		Draft draft = (Draft) getIntent().getSerializableExtra("draft");

		if (draft != null) {
			editText.setText(draft.content);
			bookIdBuffer.append(draft.bookidBuffer);
			userIdBuffer.append(draft.useridBuffer);
			for(int i=0; i<draft.photoPath.size(); i++) {
				ImageData data = draft.photoPath.get(i);
				
				String path = Environment .getExternalStorageDirectory()+"/JDReader/DraftsBox/"+File.separator+ LoginUser.getpin() +File.separator;
				String img = data.imagePath;
				String filename = img.substring(img.lastIndexOf("/") + 1);
				String newFileName = path + File.separator + filename;
				File file = new File(newFileName);
				if(file.exists()) {
					data.imagePath = newFileName;
				}
				
				AlbumManager.getInstance().getDataList().put(data.imagePath, data);
			}
		}
		
		mGridView = (GridView)findViewById(R.id.mGridView);
	}

	public void initTopBar() {
		if (topBarView == null)
			return;
		Intent intent = getIntent();
		String title = intent.getStringExtra(TITLE);

		topBarView.setTitle(title);
		topBarView.setLeftMenuVisiable(true, "取消", R.color.red_main);
		topBarView.setRightMenuOneVisiable(true, "发布", R.color.red_main, false);
		topBarView.setListener(this);
	}

	@Override
	public void onLeftMenuClick() {
		String content = editText.getText().toString();

		if (null == getIntent().getSerializableExtra("draft")
				&& (!TextUtils.isEmpty(content) || AlbumManager.getInstance().getDataListSize() > 0))

		{
			DialogManager.showCommonDialog(TimelinePostTweetActivity.this,
					"提示", "是否保存到草稿箱?", "保存草稿", "不保存",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								saveDraft(true);
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								break;
							default:
								break;
							}
							dialog.dismiss();
							exit();
						}
					});
		} else {
			exit();
		}
	}

	@Override
	public void onBackPressed() {
		String content = editText.getText().toString();
		if (null == getIntent().getSerializableExtra("draft")
				&& (!TextUtils.isEmpty(content) || AlbumManager.getInstance().getDataListSize() > 0))
		{
			DialogManager.showCommonDialog(TimelinePostTweetActivity.this,
					"提示", "是否保存到草稿箱?", "保存草稿", "不保存",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								saveDraft(true);
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								break;
							default:
								break;
							}
							dialog.dismiss();
							exit();
						}
					});
		} else {
			exit();
		}
	}

	public void saveDraft(boolean showMsg) {
		String content = editText.getText().toString();

		if(showMsg) {
			Toast.makeText(TimelinePostTweetActivity.this, "说说已经保存到草稿箱",
					Toast.LENGTH_LONG).show();
		}
		
		Draft draft = new Draft();
		draft.type = DraftsActivity.TYPE_POST_TWEET;
		draft.content = content;
		draft.time = System.currentTimeMillis();
		draft.useridBuffer = userIdBuffer.toString();
		draft.bookidBuffer = bookIdBuffer.toString();
		draft.photoPath = AlbumManager.getInstance().getDataList(AlbumManager.MAX);

		savePhoto2DrafftsBox(draft.photoPath);
		
		List<Draft> list = LocalUserSetting
				.getDraftsList(TimelinePostTweetActivity.this);
		if (list == null)
			list = new ArrayList<Draft>();
		list.add(draft);

		Drafts drafts = new Drafts();
		drafts.drafts = list;
		LocalUserSetting.saveDraftsList(TimelinePostTweetActivity.this, drafts);
		
	}
	
	/**
	* @Description: 保存照片到草稿箱
	* @param dataList 被选中照片数据列表
	* @author xuhongwei1
	* @date 2015年10月26日 上午9:19:14 
	* @throws 
	*/ 
	private void savePhoto2DrafftsBox(List<ImageData> dataList) {
		String path = Environment .getExternalStorageDirectory()+"/JDReader/DraftsBox/"+File.separator+ LoginUser.getpin() +File.separator;
		File dir = new File(path);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		for(int i=0; i<dataList.size(); i++) {
			String img = dataList.get(i).imagePath;
			String filename = img.substring(img.lastIndexOf("/") + 1);
			String newFileName = path + File.separator + filename;
			com.jingdong.app.reader.album.FileUtils.copyFile(img, newFileName);
		}
		
	}

	@Override
	public void onRightMenuOneClick() {
		if (!NetWorkUtils.isNetworkConnected(this)) {
			Toast.makeText(this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
			return;
		}
		
		Intent intent = new Intent();
		if (postTweet) {
			if (editText.getText().length() == 0) {
				Toast.makeText(TimelinePostTweetActivity.this,
						R.string.post_without_word, Toast.LENGTH_SHORT).show();
			} else if (editText.getText().length() > MAX_ENTITY_TEXT) {
				Toast.makeText(TimelinePostTweetActivity.this,
						R.string.max_entity_text, Toast.LENGTH_SHORT).show();
			} else {
				intent.putExtra(TWEET_CONTENT, buildContent(editText.getText()
						.toString()));
				intent.putExtra(TWEET_BOOKS, bookIdBuffer.toString());
				intent.putExtra(TWEET_USERS, userIdBuffer.toString());
				intent.putExtra(IMAGE_LISTS, AlbumManager.getInstance().getImagePathAll());
				setResult(POST_TWEET_WORDS, intent);
				
				Bundle postTweetBundle = intent.getExtras();
				new TimelineActivityModel().postTweet(this, postTweetBundle);
//				exit();
			}
		} else {
			String msg;
			if (editText.getText().length() == 0)
				msg = "";
			else
				msg = editText.getText().toString();
			intent.putExtra(TWEET_CONTENT, msg);
			setResult(POST_TWEET_WORDS, intent);
			intent.putExtra(IMAGE_LISTS, AlbumManager.getInstance().getImagePathAll());
			exit();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SEARCH_PEOPLE:
			if (resultCode == TimelineSearchPeopleActivity.CLICK_USER_NAME) {
				String userName = data
						.getStringExtra(TimelineSearchPeopleActivity.USER_NAME);
				String userid = data.getStringExtra("userid");
				editText.append(AT);
				editText.append(userName);
				editText.append(BLANK);
				userIdBuffer.append(userid + ",");

			}
			break;
		case SEARCH_BOOK: {
			if (resultCode == TimelineSearchBookActivity.SELECTED_BOOK) {
				String bookId = data.getStringExtra("book_id");
				String bookName = data.getStringExtra("book_name");
				bookIdBuffer.append(bookId + ",");

				Map<String, String> map = new TreeMap<String, String>();
				String key = "《" + bookName + "》";
				String url = "<a href='/books/more/" + bookId + "'>《"
						+ bookName + "》</a>";
				map.put("id", bookId);
				map.put(key, url);
				list.add(map);
				editText.append(key);
			}

		}
			break;
		}
	}

	public String buildContent(String source) {

		MZLog.d("wangguodong", "查找文本框中书籍名称");
		MZLog.d("wangguodong", "源字符串：" + source);
		Pattern p = Pattern.compile("《[^《》]+》");
		Matcher m = p.matcher(source);
		ArrayList<String> al = new ArrayList<String>();

		while (m.find()) {
			al.add(m.group(0));
		}
		for (int i = 0; i < al.size(); i++) {
			MZLog.d("wangguodong", "查找到的书籍名称：[" + i + "]"
					+ al.get(i).toString());
			for (int j = 0; j < list.size(); j++) {
				String target = list.get(j).get(al.get(i).toString());
				if (target != null) {
					source = source.replace(al.get(i).toString(), target);
				}
			}
		}
		MZLog.d("wangguodong", "转换后的目标数据：" + source);
		return source;
	}

	private void initField() {
		Intent intent = getIntent();
		postTweet = intent.getBooleanExtra(SHOW_AT, true);
		String title = intent.getStringExtra(TITLE);

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateSelectedImage();
		TCAgent.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_community_shuoshuo));
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_community_shuoshuo));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		TCAgent.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_community_shuoshuo));
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_community_shuoshuo));
	}
	
	/**
	* @Description: 更新已选中图片列表
	* @author xuhongwei1
	* @date 2015年10月24日 下午2:24:38 
	* @throws 
	*/ 
	private void updateSelectedImage() {
		photoPathList.clear();
		photoPathList = AlbumManager.getInstance().getDataList(3);
		mGridAdapter = new GridAdapter(TimelinePostTweetActivity.this, photoPathList);
		mGridView.setAdapter(mGridAdapter);
		mGridAdapter.notifyDataSetChanged();
	}
	
	public class GridAdapter extends ArrayAdapter<ImageData> {
		private LayoutInflater inflater = null;
		private List<ImageData> mDataList;
		private DisplayMetrics dm;
		/** 保存图片实际宽度 */
		private int imgWidth;

		public GridAdapter(Context context, List<ImageData> dataList) {
			super(context, 0, dataList);
			mDataList = new ArrayList<ImageData>();
			mDataList.addAll(dataList);
			this.inflater = LayoutInflater.from(context);
			dm = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
			imgWidth = (int)((dm.widthPixels - 5 * dipToPx(6))/4);
		}
		
		@Override
		public int getCount() {
			if(mDataList.size() < 3) {
				return mDataList.size() + 1;
			}else {
				return mDataList.size();
			}
		}
		
		public int dipToPx(int dip) {
			return (int) (dip * dm.density + 0.5f);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(null == convertView) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.album_item, parent, false);
				holder.image = (ImageView)convertView.findViewById(R.id.image);
				holder.choiceIcon = (ImageView)convertView.findViewById(R.id.choiceIcon);

				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(imgWidth, imgWidth);
				holder.image.setLayoutParams(params);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.choiceIcon.setVisibility(View.VISIBLE);
			holder.choiceIcon.setImageResource(R.drawable.album_delete_icon);
			
			if((position+1) == getCount()) {
				if(mDataList.size() < 3) {
					holder.choiceIcon.setVisibility(View.GONE);
					holder.image.setImageResource(R.drawable.album_add_icon);
					convertView.setOnClickListener(new onClickListener(true, false, null));
				}else {
					ImageData data = mDataList.get(position);
					loadImage(holder.image, data);
					convertView.setOnClickListener(new onClickListener(false, false, data));
					holder.choiceIcon.setOnClickListener(new onClickListener(false, true, data));
				}
			}else {
				ImageData data = mDataList.get(position);
				loadImage(holder.image, data);
				convertView.setOnClickListener(new onClickListener(false, false, data));
				holder.choiceIcon.setOnClickListener(new onClickListener(false, true, data));
			}
			
			return convertView;
		}
		
		private void loadImage(ImageView image, ImageData data) {
			String path = data.imagePath;
			ImageLoader.getInstance().displayImage("file://"+path, image, getCutBookDisplayOptions(path));
		}
		
		public DisplayImageOptions getCutBookDisplayOptions(String pathName) {
			BitmapFactory.Options decodingOptions = new BitmapFactory.Options();
			decodingOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(pathName, decodingOptions);
			if(decodingOptions.outWidth > imgWidth) {
				decodingOptions.inSampleSize = (int)(decodingOptions.outWidth/imgWidth);
			}
			decodingOptions.inJustDecodeBounds = false;
			
	        DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(new ColorDrawable(Color.rgb(0xf0, 0xf0, 0xf0))) // 加载时的图片
	        .showImageForEmptyUri(new ColorDrawable(Color.rgb(0xf0, 0xf0, 0xf0))) // uri空的时候图片
	        .showImageOnFail(new ColorDrawable(Color.rgb(0xf0, 0xf0, 0xf0))) // 加载失败的图片
	        .resetViewBeforeLoading(false) // 默认配置
	        .delayBeforeLoading(10).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
	        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
	        .cacheOnDisk(true)// 设置下载的图片是否缓存在sdcard中
	        .considerExifParams(false) // 默认配置
	        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
	        .decodingOptions(decodingOptions)
	        .displayer(new CutBitmapDisplayer(false))
	        .build();
	        return options;
	    }
		
		class onClickListener implements OnClickListener {
			private boolean openAlbum = false;
			private boolean delete = false;
			private ImageData data;
			
			public onClickListener(boolean openalbum, boolean delete, ImageData data) {
				this.openAlbum = openalbum;
				this.delete = delete;
				this.data = data;
			}

			@Override
			public void onClick(View arg0) {
				if(openAlbum) {
					Intent album = new Intent(TimelinePostTweetActivity.this, AlbumActivity.class);
					startActivity(album);	
				}else {
					if(delete) {
						AlbumManager.getInstance().getDataList().remove(data.imagePath);
						updateSelectedImage();
					}else {
						Intent preview = new Intent(TimelinePostTweetActivity.this, PreviewPhotoActivity.class);
						preview.putExtra("item", data);
						startActivity(preview);
					}
				}
			}
			
		}
		
	}
	
	static class ViewHolder {
		ImageView image;
		ImageView choiceIcon;
	}
	
	/**
	* @Description: 退出当前界面
	* @author xuhongwei1
	* @date 2015年10月24日 下午2:22:35 
	* @throws 
	*/ 
	public void exit() {
		finish();
		AlbumManager.getInstance().getDataList().clear();
	}
	

}
