package com.jingdong.app.reader.client;

import java.io.File;

import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.LocalBook.SubBook;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;


/**
 * @author keshuangjie
 */
public class DownloadStateManager {
	public final static int STATE_LOAD_FAIL = 0;// 未下载、下载失败
	public final static int STATE_LOADING = 1; // 下载、暂停中
	public final static int STATE_LOADED = 2; // 下载完成
	
	
	public static int getLocalDocumentState(LocalDocument book){
		final LocalDocument localBook =  book;
		if (localBook != null) {
			File file = null;
			String filePath = localBook.book_path;
			if (filePath != null && !filePath.trim().equals("")) {
				file = new File(filePath);
			}
			if (localBook.state == LocalBook.STATE_LOADED
					|| localBook.state == LocalBook.STATE_LOAD_READING) {
				if (file != null && file.exists()) {
					return STATE_LOADED;
				}
				return STATE_LOAD_FAIL;
			} else if (localBook.state == LocalBook.STATE_LOADING
					|| localBook.state == LocalBook.STATE_LOAD_PAUSED
					|| localBook.state == LocalBook.STATE_LOAD_READY) {
				return STATE_LOADING;
			}
		}
		return STATE_LOAD_FAIL;
	}
	
	public static int getLocalBookState(LocalBook book){
		final LocalBook localBook =  book;
		if (localBook != null) {
			File file = null;
			String filePath = localBook.book_path;
			if (filePath != null && !filePath.trim().equals("")) {
				file = new File(filePath);
			}
			if (localBook.state == LocalBook.STATE_LOADED
					|| localBook.state == LocalBook.STATE_LOAD_READING) {
				if (file != null && file.exists()) {
					return STATE_LOADED;
				}
				return STATE_LOAD_FAIL;
			} else if (localBook.state == LocalBook.STATE_LOADING
					|| localBook.state == LocalBook.STATE_LOAD_PAUSED
					|| localBook.state == LocalBook.STATE_LOAD_READY) {
				return STATE_LOADING;
			}
		}
		return STATE_LOAD_FAIL;
	}
	
	public static int getLocalBookState(LocalBook book, String source){
		final LocalBook localBook =  book;
		if (localBook != null && DownloadTool.sourceEquals(source, localBook.source)) {
			File file = null;
			String filePath = localBook.book_path;
			if (filePath != null && !filePath.trim().equals("")) {
				file = new File(filePath);
			}
			if (localBook.state == LocalBook.STATE_LOADED
					|| localBook.state == LocalBook.STATE_LOAD_READING) {
				if (file != null && file.exists()) {
					return STATE_LOADED;
				}
				return STATE_LOAD_FAIL;
			} else if (localBook.state == LocalBook.STATE_LOADING
					|| localBook.state == LocalBook.STATE_LOAD_PAUSED
					|| localBook.state == LocalBook.STATE_LOAD_READY) {
				return STATE_LOADING;
			}
		}
		return STATE_LOAD_FAIL;
	}
	
	public static int getLocalBookState(SubBook book) {
		final SubBook localBook = book;
		File file = null;
		String filePath = localBook.book_path;
		if (filePath != null && !filePath.trim().equals("")) {
			file = new File(filePath);
		}
		if (localBook != null) {
			if (localBook.state == LocalBook.STATE_LOADED
					|| localBook.state == LocalBook.STATE_LOAD_READING) {
				if (file != null && file.exists()) {
					return STATE_LOADED;
				}
				return STATE_LOAD_FAIL;
			} else if (localBook.state == LocalBook.STATE_LOADING
					|| localBook.state == LocalBook.STATE_LOAD_PAUSED
					|| localBook.state == LocalBook.STATE_LOAD_READY) {
				return STATE_LOADING;
			}
		}
		return STATE_LOAD_FAIL;
	}
	
	public static String getProgressPercent(long progress, long size) {
		int progressPercent = 0;
		if (size > 0) {
			progressPercent = (int) (progress * 100 / size);
		}
		return progressPercent + "%";
	}
	
	
	
	/**
	 *   @author      keshuangjie   
	 *	 @description 返回ClipDrawable的level值
	 */
	public static int getClipProgress(long progress, long size){
		int clipProgress = 0;
		if(size != 0){
			//ClipDrawable内部预设了一个最大的level值10000
			clipProgress = (int)(10000*progress/size);
		}
		return clipProgress;
	}

	public static int getProgressPercents(long progress, long size) {
		// TODO Auto-generated method stub
		int progressPercent = 0;
		if (size > 0) {
			MZLog.d("cj", "progress======>>"+(int) progress);
			MZLog.d("cj", "size======>>"+(int) size);
			int progres = (int) progress;
			int sizes = (int) size;
			progressPercent = (int) (360 * (progress / (size * 1.0)));
		}
		return progressPercent;
	}

}
