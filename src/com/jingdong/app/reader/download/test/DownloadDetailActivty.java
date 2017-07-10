package com.jingdong.app.reader.download.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.download.entity.DownloadFileInfo;
import com.jingdong.app.reader.download.listener.DownloadInitListener;
import com.jingdong.app.reader.download.listener.DownloadListener;
import com.jingdong.app.reader.download.manager.DownloadManager;
import com.jingdong.app.reader.download.util.DownloadUtil;

/**
 * 
 *
 *
 *                #####################################################
 *                #                                                   #
 *                #                       _oo0oo_                     #
 *                #                      o8888888o                    #
 *                #                      88" . "88                    #
 *                #                      (| -_- |)                    #
 *                #                      0\  =  /0                    #
 *                #                    ___/`---'\___                  #
 *                #                  .' \\|     |# '.                 #
 *                #                 / \\|||  :  |||# \                #
 *                #                / _||||| -:- |||||- \              #
 *                #               |   | \\\  -  #/ |   |              #
 *                #               | \_|  ''\---/''  |_/ |             #
 *                #               \  .-\__  '-'  ___/-. /             #
 *                #             ___'. .'  /--.--\  `. .'___           #
 *                #          ."" '<  `.___\_<|>_/___.' >' "".         #
 *                #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 *                #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 *                #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 *                #                       `=---='                     #
 *                #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 *                #                                                   #
 *                #               佛祖保佑         永无BUG              #
 *                #                                                   #
 *                #####################################################
 *
 *
 *
 * @ClassName: DownloadDetailActivty
 * @Description: 下载详情页
 * @author J.Beyond
 * @date 2015年8月10日 下午5:47:33
 *
 */
public class DownloadDetailActivty extends Activity implements DownloadListener {

	
	private static final int NORMAL = 0;
	private static final int ERROR = -1;
	
	private TextView mFilenameTv;
	private Button mDownloadBtn;
	private TextView mSpeedTv;
	private ProgressBar mPb;
	private DownloadManager mDLManager;
	private DownloadFileInfo mFileInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_for_test_detail);
		initView();
		initData();
	}

	private void initData() {
		
		mDLManager = DownloadManager.getInstance();
		mFileInfo = (DownloadFileInfo) getIntent().getSerializableExtra("DownloadFileInfo");
		mDLManager.registListener(mFileInfo, this);
		if (!mFileInfo.hasInitialized()) {
			mDLManager.init(this, mFileInfo, new DownloadInitListener() {
				
				@Override
				public void onInitSuccess(DownloadFileInfo fileInfo) {
					fileInfo.setInitialized(true);
					sendMessage(fileInfo);
				}
				
				@Override
				public void onInitFail(DownloadFileInfo fileInfo, String errorInfo) {
					// TODO Auto-generated method stub
					
				}
			});
		}
		bindData(mFileInfo);
	}

	private void initView() {
		mFilenameTv = (TextView) findViewById(R.id.file_name_tv);
		mDownloadBtn = (Button) findViewById(R.id.download_btn);
		mSpeedTv = (TextView) findViewById(R.id.download_speed_tv);
		mPb = (ProgressBar) findViewById(R.id.download_pb);
		mPb.setMax(100);
	}
	
	private void bindData(DownloadFileInfo fileInfo){
		if (fileInfo != null) {
			mFilenameTv.setText(fileInfo.getFileName());
			mSpeedTv.setText(fileInfo.getRealTimeSpeed());
			mPb.setProgress(fileInfo.getProgress());
			mDownloadBtn.setText(DownloadUtil.getDownloadStateDesc(fileInfo));
		}
		
	}

	public void download(View view) {
		mDLManager.executeDownload(this, mFileInfo);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case NORMAL:
				DownloadFileInfo fileInfo = (DownloadFileInfo) msg.obj;
				bindData(fileInfo);
				break;
			case ERROR:
				
				break;
			}
		}
	};
	
	private void sendMessage(DownloadFileInfo fileInfo) {
		Message msg = Message.obtain();
		msg.what = NORMAL;
		msg.obj = fileInfo;
		mHandler.sendMessage(msg);
	}

	@Override
	public void onDownloadWait(DownloadFileInfo fileInfo) {
		sendMessage(fileInfo);
	}

	@Override
	public void onDownloading(DownloadFileInfo fileInfo) {
		// TODO Auto-generated method stub
		sendMessage(fileInfo);

	}

	@Override
	public void onDownloadPause(DownloadFileInfo fileInfo) {
		// TODO Auto-generated method stub
		sendMessage(fileInfo);

	}

	@Override
	public void onDownloadFinished(DownloadFileInfo fileInfo) {
		// TODO Auto-generated method stub
		sendMessage(fileInfo);

	}

	@Override
	public void onDownloadError(DownloadFileInfo fileInfo, String errorInfo) {
		if (!TextUtils.isEmpty(errorInfo)) {
			Message msg = Message.obtain();
			msg.what = ERROR;
			msg.obj = errorInfo;
			mHandler.sendMessage(msg);
		}else{
			sendMessage(fileInfo);
		}
	}

	@Override
	public void onMobileNetConfirm(DownloadFileInfo fileInfo) {
		// TODO Auto-generated method stub

	}

}
