package com.jingdong.app.reader.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.io.EpubImporter;
import com.jingdong.app.reader.io.PDFImporter;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.FileUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.SearchFile;
import com.jingdong.app.reader.util.ViewHolder;

public class FileBrowserActivity extends BaseActivityWithTopBar {
	// Intent Action Constants
	public static final String INTENT_ACTION_SELECT_DIR = "ua.com.vassiliev.androidfilebrowser.SELECT_DIRECTORY_ACTION";
	public static final String INTENT_ACTION_SELECT_FILE = "ua.com.vassiliev.androidfilebrowser.SELECT_FILE_ACTION";

	// Intent parameters names constants
	public static final String startDirectoryParameter = "ua.com.vassiliev.androidfilebrowser.directoryPath";
	public static final String returnDirectoryParameter = "ua.com.vassiliev.androidfilebrowser.directoryPathRet";
	public static final String returnFileParameter = "ua.com.vassiliev.androidfilebrowser.filePathRet";
	public static final String showCannotReadParameter = "ua.com.vassiliev.androidfilebrowser.showCannotRead";
	public static final String filterExtension = "ua.com.vassiliev.androidfilebrowser.filterExtension";
	public static final String TITLE ="title";
	
	// Stores names of traversed directories
	ArrayList<String> pathDirsList = new ArrayList<String>();

	// Check if the first level of the directory structure is the one showing
	// private Boolean firstLvl = true;

	private static final String LOGTAG = "F_PATH";

	private List<Item> fileList = new ArrayList<Item>();
	private File path = null;
	private String chosenFile;
	// private static final int DIALOG_LOAD_FILE = 1000;

	FileAdapter adapter;
//	FileListAdapter adapter;

	private boolean showHiddenFilesAndDirs = true;

	private boolean directoryShownIsEmpty = false;

	private boolean isImportBookFile = false;

	private String[] filterFileExtension = null;

	// Action constants
	private static int currentAction = -1;
	private static final int SELECT_DIRECTORY = 1;
	private static final int SELECT_FILE = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// In case of
		// ua.com.vassiliev.androidfilebrowser.SELECT_DIRECTORY_ACTION
		// Expects com.mburman.fileexplore.directoryPath parameter to
		// point to the start folder.
		// If empty or null, will start from SDcard root.
		setContentView(R.layout.activity_filebrowser);

		// Set action for this activity
		Intent thisInt = this.getIntent();
		currentAction = SELECT_DIRECTORY;// This would be a default action in
											// case not set by intent
		String thisAction = thisInt.getAction();
		
		String titleString =thisInt.getStringExtra(FileBrowserActivity.TITLE);
		
		if(!TextUtils.isEmpty(titleString))
			getTopBarView().setTitle(titleString);
		
		if (!TextUtils.isEmpty(thisAction)
				&& thisAction.equalsIgnoreCase(INTENT_ACTION_SELECT_FILE)) {
			Log.d(LOGTAG, "SELECT ACTION - SELECT FILE");
			currentAction = SELECT_FILE;
		}

		showHiddenFilesAndDirs = thisInt.getBooleanExtra(
				showCannotReadParameter, true);

		filterFileExtension = thisInt.getStringArrayExtra(filterExtension);
		if (filterFileExtension == null || filterFileExtension.length == 0) {
			finish();
			return;
		}
		for (int i = 0; i < filterFileExtension.length; i++) {
			if (filterFileExtension[i].equalsIgnoreCase("epub")) {
				isImportBookFile = true;
				break;
			}
		}
		setInitialDirectory();

		parseDirectoryPath();
		loadFileList();
		
		adapter =new FileAdapter();
		
		this.initializeButtons();
		this.initializeFileListView();
		updateCurrentDirectoryTextView();
		Log.d(LOGTAG, path.getAbsolutePath());
	}

	private void setInitialDirectory() {
		Intent thisInt = this.getIntent();
		String requestedStartDir = thisInt
				.getStringExtra(startDirectoryParameter);

		if (requestedStartDir != null && requestedStartDir.length() > 0) {// if(requestedStartDir!=null
			File tempFile = new File(requestedStartDir);
			if (tempFile.isDirectory())
				this.path = tempFile;
		}// if(requestedStartDir!=null

		if (this.path == null) {// No or invalid directory supplied in intent
								// parameter
			if (Environment.getExternalStorageDirectory().isDirectory()
					&& Environment.getExternalStorageDirectory().canRead())
				path = Environment.getExternalStorageDirectory();
			else
				path = new File("/");
		}// if(this.path==null) {//No or invalid directory supplied in intent
			// parameter
	}// private void setInitialDirectory() {

	private void parseDirectoryPath() {
		pathDirsList.clear();
		String pathString = path.getAbsolutePath();
		String[] parts = pathString.split("/");
		int i = 0;
		while (i < parts.length) {
			pathDirsList.add(parts[i]);
			i++;
		}
	}

	private void initializeButtons() {
		Button upDirButton = (Button) this.findViewById(R.id.upDirectoryButton);
		upDirButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(LOGTAG, "onclick for upDirButton");
				if (pathDirsList.size() == 1) {
					finish();
				} else {
					loadDirectoryUp();
					loadFileList();
					adapter.notifyDataSetChanged();
					updateCurrentDirectoryTextView();
				}
			}
		});
		final Button searchEpubButton = (Button) findViewById(R.id.searchFiles);
		searchEpubButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putStringArray(SearchFile.FILE_NAME_EXTENSION_ARRAY,
						filterFileExtension);
				bundle.putBoolean(SearchFile.FILE_ISBOOK, isImportBookFile);
				SearchFile searchFile = new SearchFile(
						FileBrowserActivity.this, searchEpubButton, bundle);
				searchFile.execute(new File(path.getAbsolutePath()));
			}
		});
	}

	private void loadDirectoryUp() {
		// present directory removed from list
		String s = pathDirsList.remove(pathDirsList.size() - 1);
		// path modified to exclude present directory
		path = new File(path.toString().substring(0,
				path.toString().lastIndexOf(s)));
		fileList.clear();
		
		if(isImportBookFile)
			LocalUserSetting.saveBookPath(
					getApplicationContext(), path.getAbsolutePath());
		else
			LocalUserSetting.saveTxtFontPath(
					getApplicationContext(), path.getAbsolutePath());
	}

	private void updateCurrentDirectoryTextView() {
		int i = 0;
		String curDirString = "文件路径:";
		while (i < pathDirsList.size()) {
			curDirString += pathDirsList.get(i) + "/";
			i++;
		}
		if (pathDirsList.size() == 0) {
			((Button) this.findViewById(R.id.upDirectoryButton))
					.setEnabled(false);
			curDirString = "/";
		} else
			((Button) this.findViewById(R.id.upDirectoryButton))
					.setEnabled(true);

		((TextView) this.findViewById(R.id.currentDirectoryTextView))
				.setText(curDirString);
	}// END private void updateCurrentDirectoryTextView() {

	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private void initializeFileListView() {
		final ListView lView = (ListView) this.findViewById(R.id.fileListView);
		LinearLayout.LayoutParams lParam = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lParam.setMargins(15, 5, 15, 5);
		lView.setAdapter(this.adapter);
		lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				chosenFile = fileList.get(position).file;
				File sel = new File(path + "/" + chosenFile);
				Log.d(LOGTAG, "Clicked:" + chosenFile);
				if (sel.isDirectory()) {
					if (sel.canRead()) {
						// Adds chosen directory to list
						pathDirsList.add(chosenFile);
						path = new File(sel + "");
						Log.d(LOGTAG, "Just reloading the list");
						loadFileList();
						adapter.notifyDataSetChanged();
						lView.setSelection(0);
						updateCurrentDirectoryTextView();
						Log.d(LOGTAG, path.getAbsolutePath());
						if(isImportBookFile)
						LocalUserSetting.saveBookPath(
								getApplicationContext(), path.getAbsolutePath());
						else LocalUserSetting.saveTxtFontPath(
								getApplicationContext(), path.getAbsolutePath());
						
					} else {
						showToast("Path does not exist or cannot be read");
					}
				} else {
					// showToast("item clicked");
					if (sel.exists()) {
						if(isImportBookFile)
						LocalUserSetting.saveBookPath(
								getApplicationContext(), sel.getParent());
						else LocalUserSetting.saveTxtFontPath(
								getApplicationContext(), sel.getParent());
						
						
						if (isImportBookFile) {
							ImportTask task = new ImportTask(
									FileBrowserActivity.this, 1);
							String filepath = sel.getAbsolutePath().substring(
									0,
									sel.getAbsolutePath().lastIndexOf("/") + 1);
							MZLog.d("cj",
									"filepath=============>>"
											+ sel.getAbsolutePath());
							MZLog.d("cj", "filepath=============>>" + filepath);
							
							task.execute(sel);
						} else {
							ArrayList<String> filePathList = new ArrayList<String>();
							filePathList.add(sel.getAbsolutePath());
							String fontpath = sel.getAbsolutePath().substring(
									0,
									sel.getAbsolutePath().lastIndexOf("/") + 1);
							Intent intent = new Intent(
									ReaderSettingActivity.ACTION_IMPORT_FONT_DONE);
							intent.putExtra(ReaderSettingActivity.FontListKey,
									filePathList);
							MZLog.d("cj", "fontpath=============>>" + fontpath);
							
							LocalBroadcastManager.getInstance(
									FileBrowserActivity.this).sendBroadcast(
									intent);
						}
					}

					// if (!directoryShownIsEmpty) {
					// Log.d(LOGTAG, "File selected:" + chosenFile);
					// returnFileFinishActivity(sel.getAbsolutePath());
					// }
				}
			}
		});
	}

	private void returnFileFinishActivity(String filePath) {
		Intent retIntent = new Intent();
		retIntent.putExtra(returnFileParameter, filePath);
		this.setResult(RESULT_OK, retIntent);
		this.finish();
	}// END private void returnDirectoryFinishActivity() {

	private void loadFileList() {
		try {
			path.mkdirs();
		} catch (SecurityException e) {
			Log.e(LOGTAG, "unable to write on the sd card ");
		}
		fileList.clear();

		if (path.exists() && path.canRead()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					boolean showReadableFile = showHiddenFilesAndDirs
							|| sel.canRead();
					// Filters based on whether the file is hidden or not
					if (currentAction == SELECT_DIRECTORY) {
						return (sel.isDirectory() && showReadableFile);
					}
					if (currentAction == SELECT_FILE) {

						// If it is a file check the extension if provided
						if (sel.isFile() && filterFileExtension != null) {
							boolean isMatchFile = false;
							String selFileName = sel.getName();
							int index = selFileName.lastIndexOf(".");
							if (index <= 0 || index >= selFileName.length() - 1) {
								return false;
							}
							String selFileExtension = selFileName
									.substring(index + 1);
							for (int i = 0; i < filterFileExtension.length; i++) {
								if (selFileExtension
										.equalsIgnoreCase(filterFileExtension[i])) {
									isMatchFile = true;
									break;
								}
							}
							return (showReadableFile && isMatchFile);
						}
						return (showReadableFile);
					}
					return true;
				}// public boolean accept(File dir, String filename) {
			};// FilenameFilter filter = new FilenameFilter() {

			String[] fList = path.list(filter);
			this.directoryShownIsEmpty = false;
			for (int i = 0; i < fList.length; i++) {
				// Convert into file path
				File sel = new File(path, fList[i]);
				Log.d(LOGTAG,
						"File:" + fList[i] + " readable:"
								+ (Boolean.valueOf(sel.canRead())).toString());
				int drawableID = R.drawable.icon_file;
				boolean canRead = sel.canRead();
				// Set drawables
				boolean isdir =false;
				if (sel.isDirectory()) {
					isdir=true;
					drawableID = R.drawable.icon_folder;
				}
				fileList.add(i, new Item(fList[i], drawableID, canRead,isdir));
			}// for (int i = 0; i < fList.length; i++) {
			if (fileList.size() == 0) {
				// Log.d(LOGTAG, "This directory is empty");
				this.directoryShownIsEmpty = true;
				fileList.add(0, new Item(getString(R.string.noEpubFile), -1,
						true,false));
			} else {// sort non empty list
				Log.d("cj", "Collections==========>>>>>>>>");
				Collections.sort(fileList, new ItemFileNameComparator());
			}
		} else {
			Log.e(LOGTAG, "path does not exist or cannot be read");
		}
		// Log.d(TAG, "loadFileList finished");
	}// private void loadFileList() {


	
 class FileAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fileList==null?0:fileList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return fileList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(convertView==null)
				convertView=LayoutInflater.from(FileBrowserActivity.this).inflate(R.layout.item_file, null);
			
			ImageView image =ViewHolder.get(convertView, R.id.image);
			TextView textView =ViewHolder.get(convertView, R.id.text1);
			ImageView arrow =ViewHolder.get(convertView, R.id.arrow);
			
			int drawableID = 0;
			if (fileList.get(position).icon != -1) {
				// If icon == -1, then directory is empty
				drawableID = fileList.get(position).icon;
			}
			if(drawableID==R.drawable.icon_folder)
				arrow.setVisibility(View.VISIBLE);
			else {
				arrow.setVisibility(View.GONE);
			}
			image.setImageResource(drawableID);
			textView.setText(fileList.get(position).file);
			
			return convertView;
		}
		
	}
	
	
	

	private class Item {
		public String file;
		public int icon;
		public boolean isDir;

		public Item(String file, Integer icon, boolean canRead,boolean isDir) {
			this.file = file;
			this.icon = icon;
			this.isDir=isDir;
		}

		@Override
		public String toString() {
			return file;
		}
	}// END private class Item {

	private class ItemFileNameComparator implements Comparator<Item> {
		public int compare(Item lhs, Item rhs) {
			
			if(lhs.isDir==true&&rhs.isDir==false)
				return -1;
			else if(lhs.isDir==false&&rhs.isDir==true)
				return 1;
			else return lhs.file.compareToIgnoreCase(rhs.file);
		
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static long getFreeSpace(String path) {
		StatFs stat = new StatFs(path);
		long availSize;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
			availSize = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
		else
			availSize = (long) stat.getAvailableBlocks()
					* (long) stat.getBlockSize();
		return availSize;
	}// END public static long getFreeSpace(String path) {

	public static String formatBytes(long bytes) {
		String retStr = "";
		// One binary gigabyte equals 1,073,741,824 bytes.
		if (bytes > 1073741824) {// Add GB
			long gbs = bytes / 1073741824;
			retStr += Long.toString(gbs).toString() + "GB ";
			bytes = bytes - (gbs * 1073741824);
		}
		// One MB - 1048576 bytes
		if (bytes > 1048576) {// Add GB
			long mbs = bytes / 1048576;
			retStr += Long.toString(mbs).toString() + "MB ";
			bytes = bytes - (mbs * 1048576);
		}
		if (bytes > 1024) {
			long kbs = bytes / 1024;
			retStr += Long.toString(kbs).toString() + "KB";
			bytes = bytes - (kbs * 1024);
		} else
			retStr += Long.toString(bytes).toString() + " bytes";
		return retStr;
	}

	class ImportTask extends AsyncTask<File, Integer, Integer> {

		private final ProgressDialog pDialog;
		private final AtomicInteger fileNumber = new AtomicInteger();
		private final int max;
		private Context mContext;

		public ImportTask(Context mContext, int max) {
			this.max = max;
			this.mContext = mContext;
			pDialog = new ProgressDialog(mContext);
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
		protected Integer doInBackground(File... params) {
			File file = params[0];
			File unZipDir = new File(mContext.getExternalCacheDir(),
					file.getName());
			int flag = EpubImporter.BOOK_IMPORT_FAIL;
			try {
				if (FileUtils.isPDF(file.getName())) {
					flag = PDFImporter.importBook(file.getName(), file,
							FileBrowserActivity.this);
				} else {
					if (EpubImporter.isInBookCase(file, unZipDir)) {
						flag = EpubImporter.BOOK_IS_EXIST;
					} else {
						flag = EpubImporter.importBook(file.getName(), file,
								mContext);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				delDirRecurse(unZipDir);
				publishProgress(fileNumber.incrementAndGet());
			}
			return flag;

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			pDialog.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			pDialog.dismiss();
			if (result == EpubImporter.BOOK_IMPORT_SUCCESS) {
				Toast.makeText(mContext,
						getString(R.string.importBookNumber, fileNumber.get()),
						Toast.LENGTH_SHORT).show();
			} else if (result == EpubImporter.BOOK_IS_EXIST) {
				Toast.makeText(mContext, getString(R.string.file_has_exist),
						Toast.LENGTH_SHORT).show();
			} else if (result == EpubImporter.BOOK_IMPORT_FAIL) {
				Toast.makeText(mContext, getString(R.string.importBookFail),
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	private class FileListAdapter<Item> extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fileList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return fileList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub

			if (convertView == null) {
				convertView = LayoutInflater.from(FileBrowserActivity.this)
						.inflate(R.layout.item_file, parent, false);
//				TextView view = (TextView) convertView.findViewById(R.id.text1);
//				ImageView button = (ImageView) convertView.findViewById(R.id.button);
//				int drawableID = 0;
//				if (fileList.get(position).icon != -1) {
//					// If icon == -1, then directory is empty
//					drawableID = fileList.get(position).icon;
//				}
//				if (fileList.get(position).file.endsWith(".epub")) {
//					button.setVisibility(view.VISIBLE);
//				}
//				view.setText(fileList.get(position).file);
//				view.setCompoundDrawablesWithIntrinsicBounds(drawableID, 0, 0,
//						0);
			}
			return convertView;

		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_import));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_import));
	}

}
