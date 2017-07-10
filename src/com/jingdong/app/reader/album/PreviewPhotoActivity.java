package com.jingdong.app.reader.album;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jingdong.app.reader.R;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PreviewPhotoActivity extends Activity implements OnClickListener{
	private ViewPager mViewPager = null;
	/** 图片选择按钮 */
	private ImageView mChoiceIcon = null;
	/** 显示当前图片位置 */
	private TextView title = null;
	/** 完成按钮 */
	private Button finishBtn = null;
	/** 图片位置 */
	private int index = 0;
	private PreviewPhotoAdapter mPreviewPhotoAdapter = null;
	/** 图片数据列表 */
	private List<ImageData> mDataList = new ArrayList<ImageData>();
	private String from = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview_photo);
		initView();
	}
	
	/**
	* @Description: 控件初始化
	* @author xuhongwei1
	* @date 2015年10月23日 上午9:39:10 
	* @throws 
	*/ 
	@SuppressWarnings("deprecation")
	private void initView() {
		title = (TextView)findViewById(R.id.title);
		finishBtn = (Button)findViewById(R.id.finishBtn);
		mChoiceIcon = (ImageView)findViewById(R.id.mChoiceIcon);
		mViewPager = (ViewPager)findViewById(R.id.mViewPager);
		mViewPager.setOffscreenPageLimit(1);
		loadData();
		initListener();
	}
	
	/**
	* @Description: 初始化控件监听事件
	* @author xuhongwei1
	* @date 2015年10月23日 上午9:38:49 
	* @throws 
	*/ 
	@SuppressWarnings("deprecation")
	private void initListener() {
		mChoiceIcon.setOnClickListener(this);
		findViewById(R.id.backBtn).setOnClickListener(this);
		findViewById(R.id.finishBtn).setOnClickListener(this);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				index = arg0;
				int curIndex = index + 1;
				title.setText(curIndex + "/" + mDataList.size());
				if(index >= 0 && index < mDataList.size()) {
					if(mDataList.get(arg0).isSelected) {
						mChoiceIcon.setImageResource(R.drawable.album_select_icon);
					}else {
						mChoiceIcon.setImageResource(R.drawable.album_normal_icon);
					}
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
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
//				String path;
//				String from = getIntent().getStringExtra("from");
//				if(!TextUtils.isEmpty(from)) {
//					path = getIntent().getStringExtra("path");
//					ImageData item = new ImageData();
//					item.imagePath = path;
//					mDataList.add(item);
//				}
				HashMap<String, ImageData> selectList = AlbumManager.getInstance().getDataList();
				if(0 != selectList.size()) {
					for(int i=0; i<mDataList.size(); i++) {
						String imagepath = mDataList.get(i).imagePath;
						if(selectList.containsKey(imagepath)) {
							mDataList.get(i).isSelected = true;
						}
					}
				}
				
				mPreviewPhotoAdapter = new PreviewPhotoAdapter(PreviewPhotoActivity.this, mDataList);
				mViewPager.setAdapter(mPreviewPhotoAdapter);
				ImageData curData = (ImageData)getIntent().getSerializableExtra("item");
				if(null != curData) {
					for(int i=0; i<_mDataList.size(); i++) {
						ImageData item = _mDataList.get(i);
						if(item.imagePath.equals(curData.imagePath)) {
							index = i;
							break;
						}
					}
				}
				
				mViewPager.setCurrentItem(index, false);
				int curIndex = index + 1;
				title.setText(curIndex + "/" + mDataList.size());
				updateSelectedText();
			}
		});
		async.execute("");
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.backBtn:
			exit();
			break;
		case R.id.finishBtn:
			if(null != AlbumActivity.mHandler) {
				AlbumActivity.mHandler.sendEmptyMessage(AlbumActivity.EXIT);
			}
			
			exit();
			break;
		case R.id.mChoiceIcon:
			HashMap<String, ImageData> selectData = AlbumManager.getInstance().getDataList();
			ImageData data = mDataList.get(index);
			if(selectData.containsKey(data.imagePath)) {
				mDataList.get(index).isSelected = false;
				AlbumManager.getInstance().getDataList().remove(data.imagePath);
				mChoiceIcon.setImageResource(R.drawable.album_normal_icon);
			}else {
				if(selectData.size() >= AlbumManager.MAX) {
					Toast.makeText(PreviewPhotoActivity.this, "超出可选图片张数", Toast.LENGTH_SHORT).show();
					return;
				}
				
				mDataList.get(index).isSelected = true;
				AlbumManager.getInstance().getDataList().put(data.imagePath, data);
				mChoiceIcon.setImageResource(R.drawable.album_select_icon);
			}
			
			if(null != AlbumActivity.mHandler) {
				Message msg = AlbumActivity.mHandler.obtainMessage(AlbumActivity.UPDATESELECTE);
				msg.obj = mDataList.get(index);
				AlbumActivity.mHandler.sendMessage(msg);
			}
			
			updateSelectedText();
			break;

		default:
			break;
		}
	}
	
	/**
	* @Description: 更新完成按钮显示文本
	* @author xuhongwei1
	* @date 2015年10月23日 上午11:29:37 
	* @throws 
	*/ 
	private void updateSelectedText() {
		HashMap<String, ImageData> selData = AlbumManager.getInstance().getDataList();
		if(0 == selData.size()) {
			finishBtn.setTextColor(Color.rgb(0x99, 0x99, 0x99));
			finishBtn.setText("完成");
		}else {
			finishBtn.setTextColor(getResources().getColor(R.color.red_main));
			finishBtn.setText("完成(" + selData.size() + "/"+ AlbumManager.MAX +")");
		}
	}
	
	/**
	* @Description: 关闭当前页面，释放相关资源
	* @author xuhongwei1
	* @date 2015年10月21日 下午5:42:47 
	* @throws 
	*/ 
	private void exit() {
		if(null != mPreviewPhotoAdapter) {
			mPreviewPhotoAdapter.destory();
		}
		
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

}
