package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.Upload;
import com.jingdong.app.reader.util.Upload.UploadStateListener;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;

public class UploadToCloudDiskActivity extends BaseActivityWithTopBar implements TopBarViewListener{

	private ListView listView;
	private List<Document> list = new ArrayList<Document>();

	private TextView msgView;
	private ProgressBar progressBar;

	private ListDocumentAdapter adapter;
	private ListUploadDocumentAdapter uploadAdapter;
	private String authToken = "";
	private String userId;
	private List<Map<String, String>> localSignList = new ArrayList<Map<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_clouddisk_upload);
		
		getTopBarView().setRightMenuOneVisiable(true, "上传", R.color.red_main, false);
		
		listView = (ListView) findViewById(R.id.upload_local_book_list);
		msgView = (TextView) findViewById(R.id.upload_msg_header);
		progressBar = (ProgressBar) findViewById(R.id.refresh_progress);

		userId = LoginUser.getpin();
		adapter = new ListDocumentAdapter();
		uploadAdapter = new ListUploadDocumentAdapter();

		ListLocalDocument task = new ListLocalDocument();
		task.execute();

	}

	public void requestSynBook() {
		localSignList = MZBookDatabase.instance.getLocalDocumentSign(UploadToCloudDiskActivity.this,userId);

		for (int i = 0; i < localSignList.size(); i++) {
			// 本地书籍如果没有server_id 需要重新绑定
			if (Integer.valueOf(localSignList.get(i).get("serverId")) == 0) {

				final String documentid = localSignList.get(i)
						.get("documentId");
				WebRequestHelper.post(URLText.synServerId, RequestParamsPool
						.bindYunPanServeridParams(
								localSignList.get(i).get("sign"), localSignList
										.get(i).get("name")), true,
						new MyAsyncHttpResponseHandler(
								UploadToCloudDiskActivity.this) {

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {
								DocBind bind = new DocBind();
								bind.userId = LoginUser.getpin();
								bind.documentId = Integer.valueOf(documentid);
								try {
									JSONObject object = new JSONObject(
											new String(responseBody));
									bind.serverId = object
											.getLong("document_id");
									MZLog.d("wangguodong", "服务器返回的serverid="
											+ bind.serverId);
									MZBookDatabase.instance.UpdateDocBind(bind);
								} catch (Exception e) {
									MZLog.e(activityTag, e.getMessage());
								}

							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								Toast.makeText(UploadToCloudDiskActivity.this,
										"书籍绑定失败", Toast.LENGTH_SHORT).show();
							}
						});

			}
		}

		JSONArray jsonArr = new JSONArray();
		try {

			for (Map<String, String> map : localSignList) {
				JSONObject obj = new JSONObject();
				obj.put("sign", map.get("sign"));
				obj.put("id", map.get("serverId"));
				jsonArr.put(obj);
			}

		} catch (Exception e) {
			MZLog.e(activityTag, e.getMessage());
		}

		WebRequestHelper.post(URLText.synCloudDiskBook, RequestParamsPool
				.isNeedUploadYunPanParams(jsonArr.toString()), true,
				new MyAsyncHttpResponseHandler(UploadToCloudDiskActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						MZLog.d("wangguodong", "请求失败了");
						if(progressBar!=null)
							progressBar.setVisibility(View.GONE);

					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// 刷新header信息
						try {
							JSONArray array = new JSONArray(new String(
									responseBody));
							int unSyncBookCount = array.length();
							if (unSyncBookCount < 1) {

								msgView.setText(R.string.upload_not_find_synbook_msg);
								listView.setAdapter(uploadAdapter);
								uploadAdapter.notifyDataSetChanged();
								progressBar.setVisibility(View.GONE);

							} else {
								msgView.setText(UploadToCloudDiskActivity.this
										.getResources()
										.getString(
												R.string.upload_find_synbook_msg,
												array.length()));

								// 刷新listview信息
								boolean isNeedUpload = false;
								for (int i = 0; i < array.length(); i++)
									for (int j = 0; j < localSignList.size(); j++) {
										if (array
												.get(i)
												.toString()
												.equals(localSignList.get(j)
														.get("serverId")))

										{
											localSignList.get(j).put(
													"isNeedUploaded", "true");
											isNeedUpload = true;

										}
									}

								Collections.sort(localSignList,
										new SignBookComparator());
								listView.setAdapter(uploadAdapter);
								uploadAdapter.notifyDataSetChanged();

								progressBar.setVisibility(View.GONE);

								for (int position = 0; isNeedUpload
										&& position < localSignList.size(); position++) {

									Map<String, String> map = localSignList
											.get(position);
									if (map.get("isNeedUploaded")
											.equals("true")) {
										map.put("position", position + "");
										MZLog.d(activityTag, "当前位置：" + position
												+ "开始上传...");
										final int index = position;
										Upload.requestUploadFile(
												UploadToCloudDiskActivity.this,
												map, new UploadStateListener() {

													@Override
													public void onUploadFinished(
															String type,
															String msg) {
														if(type.equals(Upload.TYPE_MSG)){
														if (msg.equals("ok")) {
															localSignList
																	.get(index)
																	.put("isNeedUploaded",
																			"false");
															updateView(
																	index,
																	getResources()
																			.getString(
																					R.string.uploaded));

														} else {
															MZLog.d(activityTag,
																	"上传成功 服务器绑定失败");
															updateView(index,msg);
														}
														}
													}

													
												});

									}

								}

							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});

	}

	private void updateView(int index, String msg) {
		int visiblePos = listView.getFirstVisiblePosition();
		int offset = index - visiblePos;
		if (offset < 0)
			return;
		View view = listView.getChildAt(offset);
		if (view!=null) {
			TextView uploadState = (TextView) view
					.findViewById(R.id.local_book_state);
			ProgressBar bar = (ProgressBar) view
					.findViewById(R.id.uploading_progress);
			uploadState.setText(msg);
			uploadState.setTextColor(Color.RED);
			bar.setVisibility(View.GONE);
		}

	}

	class SignBookComparator implements Comparator<Map<String, String>> {

		@Override
		public int compare(Map<String, String> lhs, Map<String, String> rhs) {
			if (lhs.get("isNeedUploaded").equals("true")
					&& rhs.get("isNeedUploaded").equals("false"))
				return -1;
			else if (lhs.get("isNeedUploaded").equals("false")
					&& rhs.get("isNeedUploaded").equals("true")) {
				return 1;
			}
			return 0;
		}

	}

	class ListLocalDocument extends AsyncTask<Void, Void, List<Document>> {

		@Override
		protected List<Document> doInBackground(Void... params) {

			progressBar.setVisibility(View.VISIBLE);
			list = MZBookDatabase.instance.listDocument(userId);
			return list;
		}

		@Override
		protected void onPostExecute(List<Document> result) {
			super.onPostExecute(result);

			if (listView.getAdapter() == null) {
				listView.setAdapter(adapter);
			}
			adapter.notifyDataSetChanged();
			progressBar.setVisibility(View.GONE);

		}

	}

	class ListUploadDocumentAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return null != localSignList ? localSignList.size() : 0;
		}

		@Override
		public Object getItem(int arg0) {
			return localSignList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup rootview) {

			View v;
			if (convertView == null) {
				v = LayoutInflater.from(UploadToCloudDiskActivity.this)
						.inflate(R.layout.item_upload_book, null, false);
			} else {
				v = convertView;
			}

			TextView textView = (TextView) v.findViewById(R.id.local_book_name);
			TextView textView2 = (TextView) v
					.findViewById(R.id.local_book_state);
			ProgressBar pro = (ProgressBar) v
					.findViewById(R.id.uploading_progress);
			textView.setText(localSignList.get(position).get("name"));

			if (localSignList.get(position).get("isNeedUploaded")
					.equals("true")) {

				textView2.setText(R.string.file_uploading);
				pro.setVisibility(View.VISIBLE);

			}

			else {
				textView2.setVisibility(View.VISIBLE);
				textView2.setText(R.string.uploaded);
				pro.setVisibility(View.GONE);
			}

			return v;
		}

	}

	class ListDocumentAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return null != list ? list.size() : 0;
		}

		@Override
		public Object getItem(int arg0) {
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup rootview) {

			View v;
			if (convertView == null) {
				v = LayoutInflater.from(UploadToCloudDiskActivity.this)
						.inflate(R.layout.item_upload_book, null, false);
			} else {
				v = convertView;
			}

			TextView textView = (TextView) v.findViewById(R.id.local_book_name);
			TextView textView2 = (TextView) v
					.findViewById(R.id.local_book_state);
			textView.setText(list.get(position).title);
			textView2.setVisibility(View.GONE);
			return v;
		}

	}


	@Override
	public void onRightMenuOneClick() {
		super.onRightMenuOneClick();
		if (NetWorkUtils
				.isNetworkConnected(UploadToCloudDiskActivity.this)) { // 查询同步数据
			progressBar.setVisibility(View.VISIBLE);
			requestSynBook();
		} else

		{
			msgView.setText(R.string.network_not_find);
		}
	}
	
//	@Override
//	protected void onDestroy() {
//		// TODO Auto-generated method stub
//		super.onDestroy();
//		Log.d("JD_Reader", "finish----------");
//		setResult(1002);
//	}



}
