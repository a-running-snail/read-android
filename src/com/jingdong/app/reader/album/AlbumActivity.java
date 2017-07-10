package com.jingdong.app.reader.album;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.jingdong.app.reader.R;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;

/**
 * 相册
 *
 * @author xuhongwei1
 * @date 2015年10月21日 下午11:26:11 
 *	 
 */
public class AlbumActivity extends Activity implements OnClickListener{
	/** 相册照片墙 */
	private GridView mGridView = null;
	/** 照片墙适配器 */
	private AlbumAdapter mAlbumAdapter = null;
	/** 完成按钮 */
	private Button finishBtn = null;
	/** 选中图片数据列表 */
	private LinkedHashMap<String, ImageData> selectList = new LinkedHashMap<String, ImageData>();
	/** 拍照标识 */
	public static final int TAKE_PICTURE = 0x000001;
	/** 相册数据列表信息 */
	private List<ImageData> mDataList = new ArrayList<ImageData>();
	public static Handler mHandler = null;
	/** 照片选择更新消息 */
	public static final int UPDATESELECTE = 1;
	/** 退出当前界面通知消息 */
	public static final int EXIT = 2;
	private Uri mPhotoUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album);
		initView();
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case EXIT:
					exit();
					break;
				case UPDATESELECTE:
					ImageData data = (ImageData)msg.obj;
					if(data.isSelected) {
						selectList.put(data.imagePath, data);
					}else {
						if(selectList.containsKey(data.imagePath)) {
							selectList.remove(data.imagePath);
						}
					}
					
					if(null != mAlbumAdapter) {
						for(int i=0; i<mDataList.size(); i++) {
							ImageData imgdata = mDataList.get(i);
							if(imgdata.imagePath.equals(data.imagePath)) {
								mDataList.get(i).isSelected = data.isSelected;
								break;
							}
						}
						
						mAlbumAdapter.notifyDataSetChanged();
					}
					
					updateSelectedText();
					break;

				default:
					break;
				}
				
				super.handleMessage(msg);
			}
		};
		
	}
	
	/**
	* @Description: 初始化控件
	* @author xuhongwei1
	* @date 2015年10月21日 下午5:40:07 
	* @throws 
	*/ 
	private void initView() {
		mGridView = (GridView)findViewById(R.id.mGridView);
		finishBtn = (Button)findViewById(R.id.finishBtn);
		loadData();
		initListener();
	}
	
	/**
	* @Description: 注册控件监听事件
	* @author xuhongwei1
	* @date 2015年10月21日 下午5:43:27 
	* @throws 
	*/ 
	private void initListener() {
		findViewById(R.id.backBtn).setOnClickListener(this);
		findViewById(R.id.finishBtn).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.backBtn:
			resetData();
			exit();
			break;
		case R.id.finishBtn:
			if(AlbumManager.getInstance().getDataList().size() <= 0) {
				return;
			}
			
			exit();
			break;
			
		default:
			break;
		}
	}
	
	/**
	* @Description: 恢复图片选择列表数据
	* @author xuhongwei1
	* @date 2015年10月24日 下午6:02:26 
	* @throws 
	*/ 
	private void resetData() {
		Iterator iter = selectList.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next(); 
			String key = (String)entry.getKey();
			AlbumManager.getInstance().getDataList().remove(key);
		}
	}
	
	/**
	* @Description: 关闭当前页面，释放相关资源
	* @author xuhongwei1
	* @date 2015年10月21日 下午5:42:47 
	* @throws 
	*/ 
	private void exit() {
		if(null != mAlbumAdapter) {
			mAlbumAdapter.destory();
		}
		finish();
	}
	
	/**
	* @Description: 加载相册数据
	* @author xuhongwei1
	* @date 2015年10月21日 下午5:39:43 
	* @throws 
	*/ 
	private void loadData() {
		LoadAlbumData async = new LoadAlbumData(this, new AlbumDataCallBack() {
			@Override
			public void onSuccess(List<ImageData> _mDataList) {
				mDataList = _mDataList;
				LinkedHashMap<String, ImageData> selData = AlbumManager.getInstance().getDataList();
				if(0 != selData.size()) {
					for(int i=0; i<mDataList.size(); i++) {
						if(selData.containsKey(mDataList.get(i).imagePath)) {
							mDataList.get(i).isSelected = true;
						}
					}
				}
				mAlbumAdapter = new AlbumAdapter(AlbumActivity.this, mDataList, mGridView);
				mGridView.setAdapter(mAlbumAdapter);
				updateSelectedText();
			}
		});
		async.execute("");
	}
	
	/**
	* @Description: 更新选中列表数据
	* @author xuhongwei1
	* @date 2015年10月21日 下午10:59:22 
	* @throws 
	*/ 
	public void updateSelectList(ImageData data) {
		LinkedHashMap<String, ImageData> selData = AlbumManager.getInstance().getDataList();
		if(selData.containsKey(data.imagePath)) {
			if(!data.isSelected) {
				if(selectList.containsKey(data.imagePath))
					selectList.remove(data.imagePath);
				selData.remove(data.imagePath);
			}
		}else {
			if(data.isSelected) {
				selectList.put(data.imagePath, data);
				selData.put(data.imagePath, data);
			}
		}
		
		updateSelectedText();
	}
	
	/**
	* @Description: 更新完成按钮显示文本
	* @author xuhongwei1
	* @date 2015年10月23日 上午11:29:37 
	* @throws 
	*/ 
	private void updateSelectedText() {
		LinkedHashMap<String, ImageData> selData = AlbumManager.getInstance().getDataList();
		if(0 == selData.size()) {
			finishBtn.setTextColor(Color.rgb(0x99, 0x99, 0x99));
			finishBtn.setText("完成");
		}else {
			finishBtn.setTextColor(getResources().getColor(R.color.red_main));
			finishBtn.setText("完成(" + selData.size() + "/"+ AlbumManager.MAX +")");
		}
	}
	
	/**
	* @Description: 获取选中列表数据
	* @return List<ImageData>
	* @author xuhongwei1
	* @date 2015年10月21日 下午11:25:00 
	* @throws 
	*/ 
	public LinkedHashMap<String, ImageData> getSelectList() {
		return selectList;
	}
	
	/**
	* @Description: 打开系统相机
	* @author xuhongwei1
	* @date 2015年10月22日 下午2:27:24 
	* @throws 
	*/ 
	public void openCamera() {
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		ContentValues values = new ContentValues();
		mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
		startActivityForResult(openCameraIntent, AlbumActivity.TAKE_PICTURE);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case TAKE_PICTURE:
			if (resultCode == RESULT_OK) {
				final String[] projection = {MediaStore.Images.Media.DATA};
				final Cursor cursor = managedQuery(mPhotoUri, projection, null, null, null);
				cursor.moveToFirst();
				final int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				String imagePath = cursor.getString(columnIndex);
				
				ImageData img = new ImageData();
				img.imagePath = imagePath;
				img.isSelected = true;

				AlbumManager.getInstance().getDataList().put(img.imagePath, img);
				
				exit();
			}
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			resetData();
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
