package com.jingdong.app.reader.download.test;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.session.PlaybackState.CustomAction;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.download.entity.DownloadFileInfo;
import com.jingdong.app.reader.download.entity.DownloadState;
import com.jingdong.app.reader.download.listener.DownloadInitListener;
import com.jingdong.app.reader.download.listener.DownloadListener;
import com.jingdong.app.reader.download.manager.DownloadManager;
import com.jingdong.app.reader.download.service.DownloadService;
import com.jingdong.app.reader.download.util.DownloadUtil;
import com.jingdong.app.reader.download.util.L;
import com.jingdong.app.reader.view.dialog.CommonDialog;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.sina.weibo.sdk.constant.WBConstants.Msg;

public class FileListAdapter extends BaseAdapter {

	protected static final int ERROR = 10;
	private static final int NORMAL = 11;
	protected static final int CONFIRM_DOWNLOAD_IN_MOBILE_NET = 12;
	private Context mContext;
	private List<DownloadFileInfo> mFileList;
	private LayoutInflater inflater;
	private DownloadManager mDownloadManager;

	public FileListAdapter(Context context, List<DownloadFileInfo> fileInfos) {
		this.mContext = context;
		this.mFileList = fileInfos;
		this.inflater = LayoutInflater.from(context);
		mDownloadManager = DownloadManager.getInstance();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mFileList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mFileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.download_list_item_for_test, null);
			holder.tvFileName = (TextView) convertView.findViewById(R.id.tv_filename);
			holder.tvProgress = (TextView) convertView.findViewById(R.id.tv_progress);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
			holder.btnStart = (Button) convertView.findViewById(R.id.btn_start);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final DownloadFileInfo fileInfo = mFileList.get(position);
		// 初始化进度
		if (!fileInfo.hasInitialized()) {
			mDownloadManager.init(mContext, fileInfo, new DownloadInitListener() {
				
				@Override
				public void onInitSuccess(DownloadFileInfo fileInfo) {
					sendMessage(fileInfo);
				}

				@Override
				public void onInitFail(DownloadFileInfo fileInfo, String errorInfo) {
					if (!TextUtils.isEmpty(errorInfo)) {
						Toast.makeText(mContext, errorInfo, Toast.LENGTH_LONG).show();
					}
				}
			});
		}
		
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,DownloadDetailActivty.class);
				intent.putExtra("DownloadFileInfo", fileInfo);
				mContext.startActivity(intent);
			}
		});
		holder.tvFileName.setText(fileInfo.getFileName());
		holder.progressBar.setMax(100);
		
		holder.progressBar.setProgress(fileInfo.getProgress());
		holder.tvProgress.setText(fileInfo.getRealTimeSpeed());
		//显示下载状态
		String stateDesc = DownloadUtil.getDownloadStateDesc(fileInfo);
		holder.btnStart.setText(stateDesc);
		//注册监听
		mDownloadManager.registListener(fileInfo, listener);
		holder.btnStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (fileInfo.getDownloadState()) {
				case DownloadState.DOWNLOAD_NOTSTART:// 未下载——>下载
					mDownloadManager.executeDownload(mContext, fileInfo);
					break;
				case DownloadState.DOWNLOAD_ING:// 下载中——>暂停
					mDownloadManager.executePause(mContext, fileInfo);
					break;
				case DownloadState.DOWNLOAD_PAUSE:// 暂停——>继续下载
					mDownloadManager.executeResume(mContext, fileInfo);
					break;
				case DownloadState.DOWNLOAD_SUCCESS:// 下载完成——>“阅读”
					
					break;
				case DownloadState.DOWNLOAD_FAILURE:// 下载失败——>重新下载
					mDownloadManager.executeDownload(mContext, fileInfo);
					break;

				default:
					break;
				}

			}
		});

		return convertView;
	}

	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case NORMAL:
				DownloadFileInfo fileInfo = (DownloadFileInfo) msg.obj;
				if (fileInfo == null) return;
				
				for (int i = 0; i < mFileList.size(); i++) {
					DownloadFileInfo downloadFileInfo = mFileList.get(i);
					if (downloadFileInfo.getUrl().equals(fileInfo.getUrl())) {
						downloadFileInfo.setDownloadState(fileInfo.getDownloadState());
						downloadFileInfo.setFinished(fileInfo.getFinished());
						downloadFileInfo.setProgress(fileInfo.getProgress());
						downloadFileInfo.setRealTimeSpeed(fileInfo.getRealTimeSpeed());
						downloadFileInfo.setInitialized(fileInfo.hasInitialized());
						break;
					}
				}
				notifyDataSetChanged();
				break;
			case ERROR:
				notifyDataSetChanged();
				break;
			case CONFIRM_DOWNLOAD_IN_MOBILE_NET:
				L.e("运营商流量下载确认");
				final DownloadFileInfo fileInfo2 = (DownloadFileInfo) msg.obj;
				String sizeStr = DownloadUtil.formatFileSize(fileInfo2.getLength(), false);
				String content = "您现在使用的是运营商流量，下载文件大小为："+sizeStr+",确认下载？";
				CommonDialog dialog = DialogManager.getCommonDialog(mContext, "提示", content, "确定", "取消",
						new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							mDownloadManager.confirmDownloadInMobileNet(mContext, fileInfo2);
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							
							break;

						default:
							break;
						}
						dialog.dismiss();
					}
				});
				if(!dialog.isShowing()){
					dialog.show();
				}
				break;
			default:
				break;
			}
			
			
		}
	};
	
	public DownloadListener listener = new DownloadListener() {

		@Override
		public void onDownloading(DownloadFileInfo fileInfo) {
			sendMessage(fileInfo);
		}

		@Override
		public void onDownloadPause(DownloadFileInfo fileInfo) {
			sendMessage(fileInfo);
		}

		@Override
		public void onDownloadFinished(DownloadFileInfo fileInfo) {
			sendMessage(fileInfo);
		}

		@Override
		public void onDownloadError(DownloadFileInfo fileInfo,String errorInfo) {
			sendMessage(fileInfo);
		}

		@Override
		public void onDownloadWait(DownloadFileInfo fileInfo) {
			sendMessage(fileInfo);
		}

		@Override
		public void onMobileNetConfirm(DownloadFileInfo fileInfo) {
			Message msg = Message.obtain();
			msg.what = CONFIRM_DOWNLOAD_IN_MOBILE_NET;
			msg.obj = fileInfo;
			mHandler.sendMessage(msg);
		}
	};
	
	private void sendMessage(DownloadFileInfo fileInfo) {
		Message msg = Message.obtain();
		msg.what = NORMAL;
		msg.obj = fileInfo;
		mHandler.sendMessage(msg);
	}

	
	static class ViewHolder {
		TextView tvProgress;
		TextView tvFileName;
		ProgressBar progressBar;
		Button btnStart;
	}

}
