package com.jingdong.app.reader.client;

import android.app.Activity;

import com.android.mzbook.sortview.model.BookShelfModel;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;

public class DownloadHelper {

	public static void startDownload(Activity context, String type,
			DownloadedAble downloadedAble) {
		if (downloadedAble!=null) {
			if (type.equals(BookShelfModel.EBOOK)) {
				MZLog.d("wangguodong", "下载Helper:" + "开始下载ebook");
				LocalBook localBook = (LocalBook) downloadedAble;
				localBook.progress = 0;
				localBook.state = LocalBook.STATE_LOAD_PAUSED;
				localBook.save();
				MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), localBook.book_id, localBook.source);
				localBook.start(context);
			} else if (type.equals(BookShelfModel.DOCUMENT)) {
				MZLog.d("wangguodong", "下载Helper:" + "开始下载document");
				LocalDocument localBook = (LocalDocument) downloadedAble;
				localBook.progress = 0;
				localBook.state = LocalBook.STATE_LOAD_PAUSED;
				localBook.save();
				LocalDocument.start(context, localBook);
			}
		}
	}

	public static void stopDownload(Activity context, String type,
			DownloadedAble downloadedAble) {
		if (downloadedAble!=null) {
			if (type.equals(BookShelfModel.EBOOK)) {
				MZLog.d("wangguodong", "下载Helper:" + "停止下载ebook");
				LocalBook localBook = (LocalBook) downloadedAble;
				DownloadService.stop(localBook);
			} else if (type.equals(BookShelfModel.DOCUMENT)) {
				MZLog.d("wangguodong", "下载Helper:" + "停止下载document");
				LocalDocument localBook = (LocalDocument) downloadedAble;
				DownloadService.stop(localBook);
			}
		}
	}

	public static void restartDownload(Activity context, String type,
			DownloadedAble downloadedAble) {
			if (downloadedAble!=null) {
				if (type.equals(BookShelfModel.EBOOK)) {
					MZLog.d("wangguodong", "下载Helper:" + "重新下载ebook");
					LocalBook localBook = (LocalBook) downloadedAble;
					localBook.progress = 0;
					localBook.state = LocalBook.STATE_LOAD_PAUSED;
					localBook.save();
					MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), localBook.book_id, localBook.source);
					localBook.start(context);
				} else if (type.equals(BookShelfModel.DOCUMENT)) {
					MZLog.d("wangguodong", "下载Helper:" + "重新下载document");
					LocalDocument localBook = (LocalDocument) downloadedAble;
					localBook.progress = 0;
					localBook.state = LocalBook.STATE_LOAD_PAUSED;
					localBook.save();
					LocalDocument.start(context, localBook);
				}			
			}
	}

	// 继续下载
	public static void resumeDownload(Activity context, String type,
			DownloadedAble downloadedAble) {
		if (downloadedAble!=null) {
			if (type.equals(BookShelfModel.EBOOK)) {
				MZLog.d("wangguodong", "下载Helper:" + "继续下载ebook");
				LocalBook localBook = (LocalBook) downloadedAble;
				localBook.mod_time = System.currentTimeMillis();
				localBook.saveModTime();
				MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), localBook.book_id, localBook.source);
				localBook.start(context);
			} else if (type.equals(BookShelfModel.DOCUMENT)) {
				MZLog.d("wangguodong", "下载Helper:" + "继续下载document");
				LocalDocument localBook = (LocalDocument) downloadedAble;
				LocalDocument.start(context, localBook);
			}
		}

	}

}
