package com.jingdong.app.reader.album;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 相册管理类
 *
 * @author xuhongwei1
 * @date 2015年10月22日 上午11:27:04 
 *	 
 */
public class AlbumManager {
	public static final int MAX = 3;
	private volatile static AlbumManager instance=null;
	/** 存储已选中照片信息 */
	private LinkedHashMap<String, ImageData> mDataList = null;
	
	public static AlbumManager getInstance() { 
		if (null == instance){
			synchronized (AlbumManager.class) { 
				if (null == instance){
					instance = new AlbumManager();
				}
			}
		}
		return instance;  
	}
	
	public AlbumManager() {
		this.mDataList = new LinkedHashMap<String, ImageData>();
	}
	
	/**
	* @Description: 获取选中照片列表
	* @return List<ImageData> 数据列表
	* @author xuhongwei1
	* @date 2015年10月22日 上午11:27:48 
	* @throws 
	*/ 
	public LinkedHashMap<String, ImageData> getDataList() {	
		return this.mDataList;
	}
	
	public int getDataListSize() {
		return mDataList.size();
	}
	
	/**
	* @Description: 获取选中照片列表
	* @param int max 列表最大个数
	* @return List<ImageData> 数据列表
	* @author xuhongwei1
	* @date 2015年10月24日 下午2:08:55 
	* @throws 
	*/ 
	public List<ImageData> getDataList(int max) {	
		List<ImageData> mDataList = new ArrayList<ImageData>();
		LinkedHashMap<String, ImageData> selectlist = AlbumManager.getInstance().getDataList();
		Iterator iter = selectlist.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next(); 
			String key = (String)entry.getKey();
			ImageData val = (ImageData)entry.getValue();
			if(mDataList.size() < max ) {
				mDataList.add(val);
			}
		}
		
		return mDataList;
	}
	
	public ArrayList<String> getImagePathAll() {
		ArrayList<String> datalist = new ArrayList<String>();
		Iterator iter = mDataList.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next(); 
			String key = (String)entry.getKey();
			ImageData val = (ImageData)entry.getValue();
			datalist.add(val.imagePath);
		}

		return datalist;
	}
	
}
