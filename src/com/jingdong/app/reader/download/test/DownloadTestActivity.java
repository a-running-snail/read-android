package com.jingdong.app.reader.download.test;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.download.entity.DownloadFileInfo;

public class DownloadTestActivity extends Activity {

	protected static final String TAG = "MainActivity";
	private ListView mLv;
	private List<DownloadFileInfo> mFileList = null;
	private FileListAdapter mListAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_for_test);
		mLv = (ListView) findViewById(R.id.lvFile);

		// 下载路径
		String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
		// 创建下载文件的集合

		DownloadFileInfo info1 = new DownloadFileInfo(
				"http://storage.jd.com/dic/JDReaderC150C3832C00A0EA077C529FBC435873.dic",
				"ce_xiaobai.dic", storagePath);
		DownloadFileInfo info2 = new DownloadFileInfo(
				"http://apk.hiapk.com/web/api.do?qt=8051&id=608",
				"安智市场.apk", storagePath);
		DownloadFileInfo info3 = new DownloadFileInfo(
				"http://apk.hiapk.com/appdown/com.ymall.presentshop?planid=1260185&seid=c6bac802-a250-0001-33de-16815d4013dd",
				"达令全球好货.apk", storagePath);
		DownloadFileInfo info4 = new DownloadFileInfo(
				"http://apk.hiapk.com/appdown/com.jiuxianapk.ui?planid=1260186&seid=c6bac802-a250-0001-33de-16815d4013dd",
				"酒仙网.apk", storagePath);
		DownloadFileInfo info5 = new DownloadFileInfo(
				"http://apk.hiapk.com/appdown/com.cloudmoney?planid=1260187&seid=c6bac802-a250-0001-33de-16815d4013dd",
				"云钱袋理财.apk", storagePath);
		DownloadFileInfo info6 = new DownloadFileInfo(
				"http://apk.hiapk.com/appdown/com.hangzhoucaimi.financial?planid=1260188&seid=c6bac802-a250-0001-33de-16815d4013dd", 
				"挖财宝.apk",
				storagePath);
		DownloadFileInfo info7 = new DownloadFileInfo(
				"http://apk.hiapk.com/appdown/com.pinguo.edit.sdk?planid=1251064&seid=c6bac802-a250-0001-33de-16815d4013dd",
				"MIX.apk", storagePath);
		DownloadFileInfo info8 = new DownloadFileInfo(
				"http://apk.hiapk.com/appdown/com.ketchapp.balljump?planid=1251060&seid=c6bac802-a250-0001-33de-16815d4013dd",
				"球球快跳.apk", storagePath);
		DownloadFileInfo info9 = new DownloadFileInfo(
				"http://apk.hiapk.com/appdown/com.mandian.android.dongdong?planid=1251065&seid=c6bac802-a250-0001-33de-16815d4013dd",
				"动动.apk", storagePath);
		DownloadFileInfo info10 = new DownloadFileInfo(
				"http://apk.hiapk.com/appdown/se.perigee.android.seven?planid=1251066&seid=c6bac802-a250-0001-33de-16815d4013dd",
				"7分钟锻炼.apk", storagePath);

		mFileList = new ArrayList<DownloadFileInfo>();
		mFileList.add(info1);
		mFileList.add(info2);
		mFileList.add(info3);
		mFileList.add(info4);
		mFileList.add(info5);
		mFileList.add(info6);
		mFileList.add(info7);
		mFileList.add(info8);
		mFileList.add(info9);
		mFileList.add(info10);

		// 创建适配器
		mListAdapter = new FileListAdapter(this, mFileList);
		// 设置listView的适配器
		mLv.setAdapter(mListAdapter);

	}

}
