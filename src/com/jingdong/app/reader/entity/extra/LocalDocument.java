package com.jingdong.app.reader.entity.extra;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpResponse;

import android.content.Context;
import android.content.Intent;

import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadThread;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.client.RequestEntry;
import com.jingdong.app.reader.config.Constant;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.FileGuider;
import com.jingdong.app.reader.util.StreamToolBox;

public class LocalDocument implements DownloadedAble, Comparable<LocalDocument> {

	public static int STATE_INIT = -1;
	public final static int STATE_NO_LOAD = STATE_INIT + 1;
	public final static int STATE_LOADED = STATE_INIT + 2;// 已读
	public final static int STATE_LOAD_READY = STATE_INIT + 3;
	public final static int STATE_LOADING = STATE_INIT + 4;
	public final static int STATE_LOAD_FAILED = STATE_INIT + 5;
	public final static int STATE_LOAD_PAUSED = STATE_INIT + 6;
	public final static int STATE_LOAD_READING = STATE_INIT + 7;// 未读

	public final static int BOOK_STATE_NO_LOADED = STATE_INIT + 0;// 未读
	public final static int BOOK_STATE_NO_INTSTALL = STATE_INIT + 1;// 未读
	public final static int BOOK_STATE_INTSTALL = STATE_INIT + 2;// 未读

	public final static int TYPE_EBOOK = 1;
	public final static int TYPE_DOCUMENT = 2;

	public final static int FORMAT_PDF = 1;
	public final static int FORMAT_EPUB = 2;

	public final static String FORMATNAME_EPUB = "epub";
	public final static String FORMATNAME_PDF = "pdf";

	public final static int DOWNING_BOOK_URL = 4;
	public final static int DOWNING_BOOK_CONTENT = 5;
	private int requestCode = -1;
	public int downingWhich = -1;

	public int _id;// 本地id

	public long size;
	public long progress;
	public String bookUrl = "";// 下载地址
	public String author = "";
	public String title = "";
	public int state = -1;
	public int bookState = -1;//
	public long mod_time;
	public long add_time;
	public long access_time;
	public String book_path = "";
	public long _priority = 0;
	private RequestEntry requestEntry;
	public DownloadThread downloadThread;
	public String localImageUrl;
	public String packageName;
	public String opf_md5;
	public String user_id;
	public String bookSource;
	// 图书绑定信息
	public long server_id;
	public long bookid;
	public int bind;// 图书是否绑定
	public String serverAuthor;
	public String serverTitle;
	public String serverImageUrl;

	public boolean isMenualStop = false;
	public int format = -1;// 书的格式 1pdf类型 2epub类型
	public boolean tryGetContetnFromSD = false; // 尝试本地sd中是否存在正在下载的文件。

	public LocalDocument copy;
	public int belongPagCode;
	
	public String bookAbsolutePath;

	public static List<LocalDocument> getLocalBookList() {
		return MZBookDatabase.instance.getLocalDocumentList(LoginUser.getpin());
	}

	public static Document toDocument(LocalDocument doc) {

		Document document = new Document();
		document.documentId = doc._id;
		document.title = doc.title;
		document.author = doc.author;
		document.format = doc.format;
		document.opfMD5 = doc.opf_md5;
		document.size = doc.size;

		return document;

	}

	public static void start(Context context, LocalDocument doc) {
		Intent intent = new Intent(context, DownloadService.class);

		intent.putExtra("key", Long.parseLong(doc._id + ""));
		intent.putExtra("type", LocalDocument.TYPE_DOCUMENT);
		context.startService(intent);
	}

	// 以下是downloable 接口中的方法

	@Override
	public int getDownloadStatus() {
		return state;
	}

	@Override
	public void setDownloadStatus(int state) {
		this.state = state;

	}

	@Override
	public boolean saveState() {
		MZBookDatabase.instance.saveDocumentState(this.state, this._id);
		return true;
	}

	public boolean saveBookState() {
		MZBookDatabase.instance.saveDocumentBookState(this.bookState, this._id);
		return true;
	}

	public static LocalDocument getLocalDocument(int id) {

		return MZBookDatabase.instance.getLocalDocument(id);
	}

	public static LocalDocument getLocalDocument(int id, String pin) {

		return MZBookDatabase.instance.getLocalDocument(id, pin);
	}

	public static LocalDocument getLocalDocumentByServerid(long serverid,
			String pin) {

		return MZBookDatabase.instance
				.getLocalDocumentByServerid(serverid, pin);
	}

	@Override
	public void manualStop() {
		isMenualStop = true;
		stop();
	}

	private void stop() {
		if (requestEntry != null) {
			requestEntry.setStop(true);
			if (requestEntry._request != null) {
				if (!requestEntry._request.isAborted()) {
					requestEntry._request.abort();
				}
			}
		}
		if (state == LocalDocument.STATE_LOADING
				|| state == LocalDocument.STATE_LOAD_READY) {
			state = LocalDocument.STATE_LOAD_PAUSED;
		}
		DownloadService.refresh(this);
		mod_time = System.currentTimeMillis();
		save();
		if (requestEntry != null) {
			HttpResponse _response = requestEntry._response;
			if (_response != null) {
				try {
					_response.getEntity().consumeContent();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			InputStream inputStream = requestEntry._inputStream;
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public long getId() {
		return _id;
	}

	@Override
	public boolean isTryGetContetnFromSD() {
		return tryGetContetnFromSD;
	}

	@Override
	public void setTryGetContetnFromSD(boolean isTryGetContetnFromSD) {
		this.tryGetContetnFromSD = isTryGetContetnFromSD;
	}

	@Override
	public boolean isManualStop() {
		return isMenualStop;
	}

	@Override
	public int getType() {
		return DownloadedAble.TYPE_DOCUMENT;
	}

	@Override
	public String getUrl() {
		return bookUrl;
	}

	@Override
	public String getFilePath() {
		return book_path;
	}

	@Override
	public void saveLoadTime(long time) {
		mod_time = time;
	}

	@Override
	public void setRequestEntry(RequestEntry requestEntry) {
		this.requestEntry = requestEntry;
		requestCode = (int) System.currentTimeMillis();
		requestEntry.setRequestCode(requestCode);
	}

	@Override
	public void setCurrentSize(long currentSize) {
		this.progress = currentSize;

	}

	@Override
	public void setTotalSize(long size) {
		this.size = size;

	}

	@Override
	public long getTotalSize() {
		return size;
	}

	@Override
	public long getCurrentSize() {
		return progress;
	}

	@Override
	public int getRequestCode() {
		return requestCode;
	}

	@Override
	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}

	@Override
	public FileGuider creatFileGuider() {
		return null;
	}

	@Override
	public boolean isBelongPagCode(int code) {
		return belongPagCode == code;
	}

	@Override
	public DownloadedAble getCopy() {
		return copy;
	}

	@Override
	public void setCopy(DownloadedAble downloadedAble) {
		if (downloadedAble instanceof LocalDocument) {
			this.copy = (LocalDocument) downloadedAble;
		} else {
			this.copy = null;
		}

	}

	@Override
	public boolean save() {
		long index = MZBookDatabase.instance.saveLocalDocument(this);
		if (index != -1)
			this._id = (int) index;

		return index == -1 ? false : true;
	}

	public int saveDocument(LocalDocument document) {
		return (int) MZBookDatabase.instance.saveLocalDocument(document);
	}

	public void updateDocumentBookSource(LocalDocument doc) {
		MZBookDatabase.instance.updateLocalDocumentBookSource(doc);
	}

	@Override
	public int compareTo(LocalDocument another) {
		if (_priority > another._priority) {
			return 1;
		} else if (_priority < another._priority) {
			return -1;
		}
		return 0;
	}

	public static boolean saveBuiltInDocumemnt(InputStream bookIs,InputStream imgIs, long bookId,
			String imgPic, String bookName, String bookAuthor,String md5, int format,
			String formatName) {

		boolean isSave = false;
		try {
			LocalDocument localDocument = new LocalDocument();

			FileGuider savePath = new FileGuider(FileGuider.SPACE_ONLY_INTERNAL);
			savePath.setImmutable(true);
			String suffix = Constant.EPUB_SUFFI;
			savePath.setChildDirName("/epub/" + bookId);
			savePath.setFileName(bookId + suffix);

			boolean isSaveOk = StreamToolBox.saveStreamToFile(bookIs,
					savePath.getFilePath());
			if (isSaveOk) {
				localDocument.book_path = savePath.getFilePath();
				localDocument.bookSource = savePath.getFilePath();
			}
			localDocument.title = bookName;
			localDocument.author = bookAuthor;

			File file = new File(savePath.getFilePath());
			FileInputStream fis = null;
			if (file != null && file.exists()) {
				fis = new FileInputStream(file);
			}
			if (fis != null && fis.available() > 0) {
				localDocument.size = fis.available();
				localDocument.progress = fis.available();
			}
			localDocument.add_time = System.currentTimeMillis();
			localDocument.mod_time = localDocument.add_time;
			localDocument.state = LocalBook.STATE_LOADED;
			localDocument.format = format;
			localDocument.opf_md5=md5;

			savePath.setFileName(imgPic);
			
			try {
		
				byte[] imgData = IOUtil.readAsBytes(imgIs, null);
				if (imgData != null) {
					StreamToolBox.saveStreamToFile(imgData,
							savePath.getFilePath());
					localDocument.localImageUrl = savePath.getFilePath();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}

			MZBookDatabase.instance.saveDocumentOnly(localDocument);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isSave;
	}

}
