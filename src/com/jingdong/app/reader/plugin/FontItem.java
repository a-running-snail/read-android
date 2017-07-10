package com.jingdong.app.reader.plugin;

import java.io.File;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Environment;
import android.view.View;

import com.jingdong.app.reader.BuildConfig;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.client.DownloadedEntity;
import com.jingdong.app.reader.data.db.DBHelper;
import com.jingdong.app.reader.data.db.DataProvider;
import com.jingdong.app.reader.util.FileGuider;

/**
 * @author yfxiawei yfxiawei@360buy.com 上午11:29:09
 * 
 */
public class FontItem extends DownloadedEntity implements Comparable<FontItem> {
	
	public static final String FOUNDER_SS = "方正仿宋";
	public static final String FOUNDER_KAITI = "方正楷体";
	public static final String FOUNDER_LANTINGHEI = "方正兰亭黑";
	public static final String FOUNDER_MIAOWUHEI = "方正喵呜黑";
	
	public static final String FOUNDER_SS_FILE = "FZSS_GBK.ttf";//下载url写死
	public static final String FOUNDER_KAITI_FILE = "FZKT_GB18030.TTF";//不可修改，会影响下载url
	public static final String FOUNDER_LANTINGHEI_FILE = "fzlth_gb18030.ttf";//不可修改，会影响下载url
	public static final String FOUNDER_MIAOWUHEI_FILE = "FZMWFont.ttf";//不可修改，会影响下载url
	
	public static final int KEY_PLUGIN_FONT = 1;// 插件类型 字体
	public static final int KEY_FONT_SRC_INTERNAL = -1;// 字体来源 软件下载字体
	public static final int KEY_FONT_SRC_IMPORT = 1;// 字体来源 用户导入字体
	public static final int KEY_FONT_SRC_System = 0;// 系统字体不可删除不可下载
	public static final int KEY_PLUGIN_ENABLE = 1;// 字体状态 选中
	public static final int KEY_PLUGIN_DISABLE = 0;// 字体状态 未选中
	public FontItem copy;
	private String name = "";
	private int plugin_type, plugin_src = KEY_FONT_SRC_IMPORT,plugin_enable = KEY_PLUGIN_DISABLE;
	private long initShowTotalSize;
	public View itemView;
	public int belongPagCode;

	public long getInitShowTotalSize() {
		return initShowTotalSize;
	}

	public void setInitShowTotalSize(long initShowTotalSize) {
		this.initShowTotalSize = initShowTotalSize;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 */
	public FontItem() {
		// TODO 自动生成的构造函数存根
	}

	/**
	 * @param name
	 * @param path
	 * @param url
	 * @param size
	 * @param plugin_offset_size
	 * @param download_status
	 * @param plugin_type
	 * @param plugin_src
	 */
	public FontItem(String name, String path, String url, long size,
			long plugin_offset_size, int download_status, int plugin_type,
			int plugin_src) {
		super();
		this.name = name;
		this.filePath = path;
		this.url = url;
		this.totalSize = size;
		this.currentSize = plugin_offset_size;
		this.downloadStatus = download_status;
		this.plugin_type = plugin_type;
		this.plugin_src = plugin_src;
	}

	/**
	 * 
	 */
	public FontItem(String fName) {
		// TODO 自动生成的构造函数存根
		this.name = fName;
		downloadStatus = STATE_UNLOAD;
	}

	/**
	 * @param name
	 *            要设置的 name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return plugin_type
	 */
	public int getPlugin_type() {
		return plugin_type;
	}

	/**
	 * @param plugin_type
	 *            要设置的 plugin_type
	 */
	public void setPlugin_type(int plugin_type) {
		this.plugin_type = plugin_type;
	}

	/**
	 * @return plugin_src
	 */
	public int getPlugin_src() {
		return plugin_src;
	}

	/**
	 * @param plugin_src
	 *            要设置的 plugin_src
	 */
	public void setPlugin_src(int plugin_src) {
		this.plugin_src = plugin_src;
	}

	/*
	 * （非 Javadoc）
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (BuildConfig.DEBUG) {
			return "FontItem [name=" + name + ", filePath=" + filePath
					+ ", url=" + url + ", totalSize=" + totalSize
					+ ", currentSize=" + currentSize + ", download_status="
					+ downloadStatus + ", plugin_type=" + plugin_type
					+ ", plugin_src=" + plugin_src + "]";
		}
		return super.toString();

	}

	/**
	 * @return plugin_enable
	 */
	public int getPlugin_enable() {
		return plugin_enable;
	}

	/**
	 * @param plugin_enable
	 *            要设置的 plugin_enable
	 */
	public void setPlugin_enable(int plugin_enable) {
		this.plugin_enable = plugin_enable;
	}

	/**
	 * @return id
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            要设置的 id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * 正在下载状态 下载完成还是下载中
	 * 
	 * @author yfxiawei
	 * @see com.jingdong.app.reader.client.DownloadedAble#saveState()
	 */
	@Override
	public boolean saveState() {
		ContentValues values = new ContentValues();
		values.put("plugin_download_status", downloadStatus);

		boolean isSuccess = false;

		ContentResolver mContentResolver = MZBookApplication.getInstance().getContentResolver();
		if ((mContentResolver.update(DataProvider.CONTENT_URI_NAME_PLUGINS,values, "_id" + "=" + "'" + id + "'", null)) == 0) {
			mContentResolver.insert(DataProvider.CONTENT_URI_NAME_PLUGINS,values);
		}

		mContentResolver.notifyChange(DataProvider.CONTENT_URI_NAME_PLUGINS,null);
		return isSuccess;
	}

	/**
	 * 文件存储位置
	 * 
	 * @author yfxiawei
	 * @see com.jingdong.app.reader.client.DownloadedAble#getFilePath()
	 */
	@Override
	public String getFilePath() {

		return filePath;
	}

	public void setFilePath(String filePath) {

		this.filePath = filePath;
	}

	@Override
	public int getType() {
		return FontItem.TYPE_PLUG;
	}

	@Override
	public boolean save() {
		DBHelper.savePlugin(this);
		return true;
	}

	@Override
	public FileGuider creatFileGuider() {
		FileGuider savePath = new FileGuider(FileGuider.SPACE_PRIORITY_EXTERNAL);

		savePath.setImmutable(true);
		String subDirName = filePath.substring(0, filePath.lastIndexOf("/"));
		subDirName = subDirName.substring(subDirName.lastIndexOf("/") + 1);
		savePath.setChildDirName(subDirName);
		savePath.setFileName(filePath.substring(filePath.lastIndexOf("/") + 1));

		return savePath;
	}

	/**
	 * （非 Javadoc） 下午2:18:55
	 * 
	 * @author yfxiawei
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FontItem another) {
		if (_priority > another._priority) {
			return 1;
		} else if (_priority < another._priority) {
			return -1;
		}
		return 0;

	}

	@Override
	public boolean isBelongPagCode(int code) {
		// TODO Auto-generated method stub
		return belongPagCode==code;
	}

	@Override
	public void setCopy(DownloadedAble downloadedAble) {
		 if(downloadedAble instanceof FontItem){
			this.copy=(FontItem)downloadedAble;
		 }else {
			this.copy=null;
		}
	}

	@Override
	public DownloadedEntity getCopy() {
		return this.copy;
	}

}
