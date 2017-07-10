package com.jingdong.app.reader.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.download.cfg.DownloadConfiguration;
import com.jingdong.app.reader.download.db.ThreadDAOImpl;
import com.jingdong.app.reader.download.entity.DownloadFileInfo;
import com.jingdong.app.reader.download.entity.DownloadState;
import com.jingdong.app.reader.download.listener.DownloadInitListener;
import com.jingdong.app.reader.download.listener.DownloadListener;
import com.jingdong.app.reader.download.manager.DownloadManager;
import com.jingdong.app.reader.download.manager.DownloadTools;
import com.jingdong.app.reader.download.util.DownloadUtil;
import com.jingdong.app.reader.util.KSICibaTranslate;
import com.jingdong.app.reader.view.dialog.DialogManager;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DictionarySettingActivity extends BaseActivityWithTopBar {

	protected static final int ERROR = 10;
	private static final int NORMAL = 11;
	
	LinearLayout container;
	LinearLayout dictionaryLayout;
	TextView name,size,status;
	String dictPath;
	private DownloadManager mDownloadManager;
	private Context mContext;
	private List<DownloadFileInfo> mFileList = new ArrayList<DownloadFileInfo>();
	private List<DictionaryDao> dictionaryDaos = new ArrayList<DictionaryDao>();
	private List<DictionaryDao> showDictionaryDaos = new ArrayList<DictionaryDao>();
	private Map<String,TextView> statusMap = new HashMap<String,TextView>();
	ListView listview;
	DownloadFileInfo downloadFileInfo;
	DictionaryDao dictionary;
	Boolean checkExist= false;
	FileListAdapter adapter;
	boolean isMobileNetConfirm = false;
	private int totalSize = 8053221 ;//三个词典总大小
	private static String word;//当前查询的词语
	private boolean startFlag = false;
	
	private String ceUrl="http://storage.jd.com/dic/JDReaderC150C3832C00A0EA077C529FBC435873.dic";
	private String ecUrl="http://storage.jd.com/dic/JDReader8077C06EDAAB508A0B47F5D01747842B.dic";
	private String ccUrl="http://storage.jd.com/dic/JDReader02A0E9302D35F58F4479E61631FE6D0A.dic";
		
	class DictionaryDao{
		private String displayName;
		private String size;
		private String progress;
	}	
	
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_dictionary_setting);
		this.getTopBarView().setTitle("词典设置");
		mDownloadManager = DownloadManager.getInstance();
		mContext= this;
		
		if(getIntent()!=null){
			word = getIntent().getStringExtra("word");
			startFlag = getIntent().getBooleanExtra("startFlag", false);
		}
			
		initView();
	}
	
	private void initView() {
		listview = (ListView) findViewById(R.id.listview);
		
		dictPath=MZBookApplication.getInstance().getCachePath()+File.separator+"dict";
		File dirfile= new File(dictPath);
		if(!dirfile.exists()){
			dirfile.mkdir();
		}
		
		downloadFileInfo = new DownloadFileInfo(ecUrl, "ec_xiaobai.dic", dictPath);
		mFileList.add(downloadFileInfo);
		downloadFileInfo = new DownloadFileInfo(ceUrl, "ce_xiaobai.dic", dictPath);
		mFileList.add(downloadFileInfo);
		downloadFileInfo = new DownloadFileInfo(ccUrl, "cc.dic", dictPath);
		mFileList.add(downloadFileInfo);
		
		
		dictionary = new DictionaryDao();
		dictionary.displayName="汉英·英汉·汉汉词典";
		dictionary.size= "7.66M";
		dictionary.progress = "下载";
		showDictionaryDaos.add(dictionary);
		
		adapter = new FileListAdapter(mContext, showDictionaryDaos);
		listview.setAdapter(adapter);
		
		if(!isDictionaryReady()){
			if(DownloadTools.getInstance().isRunning){
				DownloadTools.getInstance().setWord(word);
				DownloadTools.getInstance().setmHandler(mHandler);
			}else{
				if(!startFlag)
					return ;
				DownloadTools.getInstance().setWord(word);
				DownloadTools.getInstance().setmFileList(mFileList);
				DownloadTools.getInstance().setmHandler(mHandler);
				DownloadTools.getInstance().startDownload();
			}
		}else{
			dictionary.progress = "已完成";
			adapter.notifyDataSetChanged();
		}
			
	}
	
	/**
	 * 判断词典是否已经全部下载
	 * @return
	 */
	private boolean isDictionaryReady() {
        if (TextUtils.isEmpty(dictPath)) {
            String appFilePath = MZBookApplication.getInstance().getCachePath();
            dictPath = appFilePath+File.separator+"dict";
        }
        if (checkIsExist("ec_xiaobai.dic")
                && checkIsExist("ce_xiaobai.dic")
                && checkIsExist("cc.dic")) {
            return true;
        }
        return false;
    }

	/**
	 * 检查文件是否存在
	 * @param fileName
	 * @return
	 */
	private Boolean checkIsExist(String fileName){
		File dirfile= new File(dictPath);
		if(!dirfile.exists()){
			dirfile.mkdir();
			return false;
		}
		File file =new File(dictPath+File.separator+fileName);
		if(file.exists())
			return true;
		else 
			return false;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
	private Handler mHandler = new Handler(){ 
        @SuppressWarnings("static-access")
		public void handleMessage(android.os.Message msg) { 
            switch (msg.what) { 
            case 0: 
                Toast.makeText(mContext, "无法获取文件大小", 0).show();
                break; 
            case -1: 
            	if(msg.obj.toString().equals("cc.dic")){
            		showDictionaryDaos.get(0).progress = "已完成";
                	adapter.notifyDataSetChanged();
            	}
            	
                break; 
            case 1: 
            	int current =Integer.parseInt(msg.obj.toString());
            	int result = Double.valueOf((current)*1.0 / totalSize * 100).intValue(); 
            	showDictionaryDaos.get(0).progress = result + "%";
            	adapter.notifyDataSetChanged();
                break; 
            case 2: 
            	Toast.makeText(mContext, "文件获取错误", 0).show();
                break; 
            } 
        }; 
    }; 
	
	public class FileListAdapter extends BaseAdapter {

		protected static final int ERROR = 10;
		private static final int NORMAL = 11;
		private Context mContext;
		private List<DictionaryDao> dictionarys;
		private LayoutInflater inflater;
		private DownloadManager mDownloadManager;

		public FileListAdapter(Context context, List<DictionaryDao> dictionarys) {
			this.mContext = context;
			this.dictionarys = dictionarys;
			this.inflater = LayoutInflater.from(context);
			mDownloadManager = DownloadManager.getInstance();
		}

		@Override
		public int getCount() {
			return dictionarys.size();
		}

		@Override
		public Object getItem(int position) {
			return dictionarys.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.dictionary_item, null);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.size = (TextView) convertView.findViewById(R.id.size);
				holder.status = (TextView) convertView.findViewById(R.id.status);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			final DictionaryDao dictionary= dictionarys.get(position);
			
			holder.name.setText(dictionary.displayName);
			holder.size.setText(dictionary.size);
			//显示下载状态
			holder.status.setText(dictionary.progress);

			holder.status.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					if(isDictionaryReady() || DownloadTools.getInstance().isRunning){
						return ; 
					}else{
						DownloadTools.getInstance().setWord(word);
						DownloadTools.getInstance().setmFileList(mFileList);
						DownloadTools.getInstance().setmHandler(mHandler);
						DownloadTools.getInstance().startDownload();
					}
					
				}
			});
			
			return convertView;
		}

		class ViewHolder {
			TextView name;
			TextView size;
			TextView status;
		}

	}
	
}
