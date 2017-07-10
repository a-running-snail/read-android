package com.jingdong.app.reader.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.xmlpull.v1.XmlPullParserException;

import android.R.integer;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.common.MZReadCommonListActivity;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.epub.epub.ContentReader;
import com.jingdong.app.reader.io.EpubImporter;
import com.jingdong.app.reader.io.PDFImporter;
import com.jingdong.app.reader.io.Unzip;
import com.jingdong.app.reader.tuple.TwoTuple;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.FileUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ViewHolder;

public class FileResultActivity extends BaseActivityWithTopBar {
	public final static String FILE_MAP = "fileMap";
	public final static String FILE_ISBOOK = "fileIsBook";
	private List<File> fileList;
	private List<Boolean> importList;
	private boolean isImportBookFile;
	public ListView listView = null;

	public MyAdapter adapter = null;
	private LinearLayout selectAllButton = null;
	private Button importButton = null;
	private ImageView allcheckbox = null;

	private boolean isAllSelected = false;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.import_book_result_layout);

		listView = (ListView) findViewById(R.id.fileListView);

		selectAllButton = (LinearLayout) findViewById(R.id.selectall);
		importButton = (Button) findViewById(R.id.imports);
		allcheckbox = (ImageView) findViewById(R.id.allcheckbox);
		isImportBookFile = getIntent().getBooleanExtra(FILE_ISBOOK, true);
		fileList = Collections.synchronizedList((List<File>) getIntent()
				.getSerializableExtra(FILE_MAP));
		importList = Collections.synchronizedList(new ArrayList<Boolean>(
				fileList.size()));
		
		
		if(!isImportBookFile)
			importButton.setText("导入字体");

		
		List<LocalDocument> documents = MZBookDatabase.instance
				.getLocalDocumentList(LoginUser.getpin());

		if (documents == null || documents.size() == 0) {
			for (int i = 0; i < fileList.size(); i++) {
				importList.add(false);
			}
		}

		else {

			for (int i = 0; i < fileList.size(); i++) {
				
				boolean isImport =false;
				for (int j = 0; j < documents.size(); j++) {
					if (null != documents.get(j).bookAbsolutePath
							&& documents.get(j).bookAbsolutePath
									.equals(fileList.get(i).getAbsolutePath())) {
						isImport=true;
						break;
					} 
				}
				
				if(isImport)
				{
					importList.add(true);
				}
				else {
					importList.add(false);
				}
			}

		}

		adapter = new MyAdapter();

		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		selectAllButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!isAllSelected) {
					for (int i = 0; i < adapter.getCount(); i++)
						if (adapter.getItemViewType(i) == fileType.FILE
								.ordinal()&&!adapter.getItem(i).getB())
							listView.setItemChecked(i, true);

				} else {
					for (int i = 0; i < adapter.getCount(); i++)
						if (adapter.getItemViewType(i) == fileType.FILE
								.ordinal()&&!adapter.getItem(i).getB())
							listView.setItemChecked(i, false);

				}

				updateImportButton();
			}
		});

		importButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				importFile(listView, adapter);

			}
		});
	}

	void updateImportButton() {

		
		for (int i = 0; i < adapter.getCount(); i++)
			if (adapter.getItem(i).getB())
				listView.setItemChecked(i, false);
		
		int count = listView.getCheckedItemCount();
		
		if (count <= 0) {
			importButton
					.setTextColor(getResources().getColor(R.color.text_sub));
		} else {
			importButton
					.setTextColor(getResources().getColor(R.color.red_main));
		}

		int selectedCount = 0;
		for (int i = 0; i < adapter.getCount(); i++)
			if (adapter.getItemViewType(i) == fileType.FILE.ordinal()&&!adapter.getItem(i).getB())
				++selectedCount;

		if (count == selectedCount) {
			isAllSelected = true;
			allcheckbox.setImageResource(R.drawable.icon_list_selected);
		} else {
			isAllSelected = false;
			allcheckbox.setImageResource(R.drawable.icon_list_unselected);
		}

		if(isImportBookFile)
			importButton.setText("导入书架(" + count + ")");
		else {
			importButton.setText("导入字体(" + count + ")");
		}
	}

	private enum fileType {
		DIR, FILE;
	}

	private void importFile(ListView listView, MyAdapter adapter) {
		
		List<Integer> positionList = new ArrayList<Integer>(
				listView.getCheckedItemCount());
		
		
		for (long id : listView.getCheckedItemIds())
			if (adapter.getItemViewType((int) id) == fileType.FILE.ordinal()&&!adapter.getItem((int)id).getB())
				positionList.add((int) id);
		
		
		if (positionList.size() == 0) {
			Toast.makeText(
					FileResultActivity.this,
					isImportBookFile ? R.string.selectAtLeastOneBook
							: R.string.selectAtLeastOneFont, Toast.LENGTH_SHORT)
					.show();
		} else {
			if (isImportBookFile) {
				ImportBookTask task = new ImportBookTask(positionList.size());
				task.execute(positionList);
			} else {
				File file;
				ArrayList<String> filePathList = new ArrayList<String>();
				for (Integer position : positionList) {
					file = fileList.get(position);
					filePathList.add(file.getAbsolutePath());
				}
				Intent intent = new Intent(
						ReaderSettingActivity.ACTION_IMPORT_FONT_DONE);
				intent.putExtra(ReaderSettingActivity.FontListKey, filePathList);
				LocalBroadcastManager.getInstance(FileResultActivity.this)
						.sendBroadcast(intent);
			}
		}
	}

	//
	// }

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return fileList.size();
		}

		@Override
		public TwoTuple<File, Boolean> getItem(int position) {
			return new TwoTuple<File, Boolean>(fileList.get(position),
					importList.get(position));
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getViewTypeCount() {
			return fileType.values().length;
		}

		@Override
		public int getItemViewType(int position) {
			File item = getItem(position).getA();
			if (item.isDirectory())
				return fileType.DIR.ordinal();
			else
				return fileType.FILE.ordinal();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			int type = getItemViewType(position);
			if (convertView == null) {
				convertView = initConvertView();
			}
			initView(position, convertView, type);
			return convertView;
		}

		private void initView(final int position, View convertView, int type) {

			TextView alreadyImport = ViewHolder.get(convertView,
					R.id.alreadImport);
			TextView file = ViewHolder.get(convertView, R.id.file);
			TextView folderText = ViewHolder.get(convertView, R.id.folderText);
			CheckBox tick = ViewHolder.get(convertView, R.id.tick);
			ImageView image = ViewHolder.get(convertView, R.id.image);
			ImageView tick_img = ViewHolder.get(convertView, R.id.tick_img);

			LinearLayout folderLayout = ViewHolder
					.get(convertView, R.id.folder);
			LinearLayout fileLayout = ViewHolder.get(convertView,
					R.id.fileLayout);

			TwoTuple<File, Boolean> item = getItem(position);
			if (type == fileType.FILE.ordinal()) {
				fileLayout.setVisibility(View.VISIBLE);
				folderLayout.setVisibility(View.GONE);
				initFileView(position, tick_img, tick, image, alreadyImport,
						file, item);
			} else {

				fileLayout.setVisibility(View.GONE);
				folderLayout.setVisibility(View.VISIBLE);
				initDirView(folderText, item);
			}
		}

		private void initFileView(final int position, ImageView tick_img,
				CheckBox tick, ImageView image, TextView alreadImport,
				TextView file, TwoTuple<File, Boolean> item) {
			if (item.getB()) {
				
				MZLog.d("wangguodong", "书籍已经导入。。。");
				tick.setOnCheckedChangeListener(null);
				tick.setChecked(false);
				tick.setVisibility(View.GONE);
				alreadImport.setVisibility(View.GONE);
				tick_img.setVisibility(View.VISIBLE);
			} else {
				tick_img.setVisibility(View.GONE);
				tick.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						
						listView.setItemChecked(position, isChecked);
						updateImportButton();
					}
				});
				tick.setChecked(listView.isItemChecked(position));

				tick.setVisibility(View.VISIBLE);
				alreadImport.setVisibility(View.GONE);
			}
			file.setText(item.getA().getName());
			image.setImageResource(R.drawable.icon_file);
		}

		private void initDirView(TextView tip, TwoTuple<File, Boolean> item) {

			tip.setText("文件路径:" + item.getA().getPath());

		}

		private View initConvertView() {
			View convertView;
			convertView = View.inflate(FileResultActivity.this,
					R.layout.item_selected_file, null);
			return convertView;
		}
	}

	private class ImportBookTask extends
			AsyncTask<List<Integer>, Integer, List<Integer>> {

		private final ProgressDialog pDialog = new ProgressDialog(
				FileResultActivity.this);
		private final AtomicInteger fileNumber = new AtomicInteger();
		private final int max;

		public ImportBookTask(int max) {
			this.max = max;
		}

		@Override
		protected void onPreExecute() {
			Resources resources = getResources();
			pDialog.setTitle(R.string.importBookTitle);
			pDialog.setMessage(resources.getString(R.string.importBookMsg));
			pDialog.setIndeterminate(false);
			pDialog.setMax(max);
			pDialog.setProgress(0);
			pDialog.setCancelable(true);
			pDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(false);
				}
			});
			pDialog.show();
		}

		@Override
		protected List<Integer> doInBackground(List<Integer>... params) {
			File file;
			List<Integer> alreadyImport = new ArrayList<Integer>(
					params[0].size());
			for (Integer position : params[0]) {
				if (isCancelled())
					break;
				else {
					file = fileList.get(position);
					File unZipDir = new File(
							FileResultActivity.this.getExternalCacheDir(),
							file.getName());
					try {
						if (FileUtils.isPDF(file.getName())) {
							MZLog.d("JD_Reader", "importBook ...");
							int flag = PDFImporter.importBook(file.getName(),
									file, FileResultActivity.this);
							if (flag == EpubImporter.BOOK_IMPORT_SUCCESS) {
								MZLog.d("JD_Reader", "importBook success!");
								alreadyImport.add(position);
								publishProgress(fileNumber.incrementAndGet());
							}
						} else {
							if (!EpubImporter.isInBookCase(file, unZipDir)) {
								EpubImporter.importBook(file.getName(), file,
										FileResultActivity.this);
								alreadyImport.add(position);
								publishProgress(fileNumber.incrementAndGet());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						delDirRecurse(unZipDir);
					}
				}
			}
			return alreadyImport;
		}

		private void delDirRecurse(File dir) {
			File[] subFiles = dir.listFiles();
			if (subFiles != null) {
				for (File item : subFiles) {
					if (item.isDirectory())
						delDirRecurse(item);
					else
						item.delete();
				}
			}
			dir.delete();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			pDialog.setProgress(values[0]);
		}

		@Override
		protected void onCancelled(List<Integer> result) {
			onPost(result);
		}

		@Override
		protected void onPostExecute(List<Integer> result) {
			onPost(result);
			for (long position : listView.getCheckedItemIds())
				listView.setItemChecked((int) position, false);
			updateImportButton();
			pDialog.dismiss();
		}

		private void onPost(List<Integer> result) {
			// result is null when click import twice
			if (result != null && result.size() != 0) {
				for (Integer index : result) {
					importList.set(index, true);
					listView.setItemChecked(index, false);
				}
				if (adapter instanceof BaseAdapter)
					((BaseAdapter) adapter).notifyDataSetChanged();
				Toast.makeText(FileResultActivity.this,
						getString(R.string.importBookNumber, fileNumber.get()),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
