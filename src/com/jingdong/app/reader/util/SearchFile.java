package com.jingdong.app.reader.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.FileResultActivity;
import com.jingdong.app.reader.activity.GlobalVarable;

public class SearchFile extends AsyncTask<File, Integer, LinkedList<File>> {

	public final static String FILE_NAME_EXTENSION_ARRAY = "fileNameExtension";
	public final static String FILE_ISBOOK = "searchFileIsBook";
	private final static int PERIOD = 20;
	private final static String TAG = "SearchFile";
	private final static String CANCEL = "Task has been canceled";
	private final Context context;
	private final Button button;
	private final Bundle bundle;
	private final String alreadSearch;
	private final HashMap<File, Set<File>> dirMap = new HashMap<File, Set<File>>();
	private AtomicInteger fileNumber = new AtomicInteger();
	private AtomicInteger matchFileNumber = new AtomicInteger();
	private ProgressDialog progressDialog;
	private boolean isImportBookFile = false;

	public SearchFile(Context context, Button button, Bundle bundle) {
		this.context = context;
		this.button = button;
		this.bundle = bundle;
		isImportBookFile = bundle.getBoolean(FILE_ISBOOK, false);
		Resources resources = context.getResources();
		alreadSearch = resources
				.getString(isImportBookFile ? R.string.alreadySearchBook
						: R.string.alreadySearchFont);
	}

	@Override
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(context, context.getResources().getString(R.string.searchFile),
				String.format(Locale.CHINA, alreadSearch, 0, 0), true, true, new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						SearchFile.this.cancel(true);
					}
				});
		button.setEnabled(false);
	}

	@Override
	protected LinkedList<File> doInBackground(File... params) {
		FilenameFilter filenameFilter = getFilterFromBundle();
		LinkedList<File> files;
		try {
			recurseDir(params[0], filenameFilter);
		} catch (InterruptedIOException e) {
			MZLog.d(TAG, Log.getStackTraceString(e));
		} finally {
			files = dirMapToFile(dirMap);
		}
		return files;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		progressDialog.setMessage(String.format(Locale.CHINA, alreadSearch, values[0], values[1]));
	}

	@Override
	protected void onCancelled(LinkedList<File> result) {
		button.setEnabled(true);
		if (result != null && result.size() > 0) {
			Intent intent = new Intent(context, FileResultActivity.class);
			intent.putExtra(FileResultActivity.FILE_MAP, result);
			intent.putExtra(FileResultActivity.FILE_ISBOOK, isImportBookFile);
			context.startActivity(intent);
		}
	}

	@Override
	protected void onPostExecute(LinkedList<File> result) {
		button.setEnabled(true);
		progressDialog.dismiss();
		if (result != null && result.size() > 0) {
			Intent intent = new Intent(context, FileResultActivity.class);
			intent.putExtra(FileResultActivity.FILE_MAP, result);
			intent.putExtra(FileResultActivity.FILE_ISBOOK, isImportBookFile);
			context.startActivity(intent);
		} else {
//			Toast.makeText(
//					context,
//					isImportBookFile ? R.string.noEpubFile
//							: R.string.noFontFile, Toast.LENGTH_SHORT).show();
		}
	}

	private void recurseDir(File root, FilenameFilter filenameFilter) throws InterruptedIOException {
		Set<File> fileSet = new HashSet<File>();
		File[] subFile = root.listFiles(filenameFilter);
		if (subFile != null) {
			for (File item : subFile) {
				if (!isCancelled()) {
					if (item.isDirectory()) {
						recurseDir(item, filenameFilter);
					} else {
						addFileToSet(fileSet, item);
					}
				} else {
					if (!fileSet.isEmpty())
						dirMap.put(root, fileSet);
					throw new InterruptedIOException(CANCEL);
				}
			}
		}
		if (!fileSet.isEmpty())
			dirMap.put(root, fileSet);
		publishProgress(fileNumber.get(), matchFileNumber.get());
	}

	private void addFileToSet(Set<File> fileSet, File item) {
		
		fileSet.add(item);
		publishProgress(fileNumber.get(), matchFileNumber.incrementAndGet());
	/*	
		File unZipDir = new File(context.getExternalCacheDir(), item.getName());
		try {
			if (!isInBookCase(item, unZipDir)) {
				fileSet.add(item);
				publishProgress(fileNumber.get(), epubNumber.incrementAndGet());
			}
		} catch (NoSuchAlgorithmException e) {
			MZLog.e(TAG, Log.getStackTraceString(e));
		} catch (IOException e) {
			MZLog.e(TAG, Log.getStackTraceString(e));
		} catch (XmlPullParserException e) {
			MZLog.d(TAG, Log.getStackTraceString(e));
		} finally {
			delDirRecurse(unZipDir);
		}
		*/
	}



	private FilenameFilter getFilterFromBundle() {
		final String[] extensions = bundle.getStringArray(FILE_NAME_EXTENSION_ARRAY);
		return new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				if (GlobalVarable.MZBOOK_PACKAGENAME.equals(filename)) {
					return false;
				}
				for (String extension : extensions) {
					int total = fileNumber.incrementAndGet();
					if (total % PERIOD == 0)
						publishProgress(total, matchFileNumber.get());
					File currFile = new File(dir, filename);
					if (currFile.isDirectory())
						return true;
					if (filename.toLowerCase(Locale.US).endsWith(extension.toLowerCase(Locale.US)))
						return true;
				}
				return false;
			}
		};
	}


	private static LinkedList<File> dirMapToFile(Map<File, Set<File>> dirMap) {
		LinkedList<File> fileList = new LinkedList<File>();
		if (dirMap != null && dirMap.size() > 0) {
			Set<Entry<File, Set<File>>> entries = dirMap.entrySet();
			for (Entry<File, Set<File>> entry : entries) {
				fileList.add(entry.getKey());
				for (File file : entry.getValue())
					fileList.add(file);
			}
		}
		return fileList;
	}
}
