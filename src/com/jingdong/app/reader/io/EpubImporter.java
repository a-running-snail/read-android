package com.jingdong.app.reader.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.text.TextUtils;

import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.epub.epub.ContentReader;
import com.jingdong.app.reader.reading.EpubCover;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;

public class EpubImporter {

	public static final int BOOK_IMPORT_SUCCESS = 0;
	public static final int BOOK_IMPORT_FAIL = 1;
	public static final int BOOK_IS_EXIST = 2;

	static synchronized public int importBook(String bookName, File filePath,
			Context context, DocBind... docBinds) {

		if (bookName == null) {
			bookName = filePath.getName();
		}

		if (TextUtils.isEmpty(bookName)) {
			bookName = "book.epub";
		}

		boolean isError = false;
		InputStream importFileStream = null;
		InputStream importFileStream2 = null;
		OutputStream bookFileStream = null;
		InputStream opfFileStream = null;
		int documentId = 0;
		try {
			importFileStream = new FileInputStream(filePath);
			documentId = MZBookDatabase.instance.createDocumentRecord();
			File bookFileDir = new File(StoragePath.getDocumentDir(context),
					documentId + File.separator + "content");
			Unzip.unzip(importFileStream, bookFileDir.getPath());

			// don't need copy epub anymore --2015.1.6
			// copy import file to this book's directory
//			File bookFile = new File(StoragePath.getDocumentDir(context),
//					documentId + File.separator + bookName);
//			importFileStream2 = new FileInputStream(filePath);
//			bookFileStream = new FileOutputStream(bookFile);
//			IOUtil.copy(importFileStream2, bookFileStream);

			ContentReader.isNeedJDDecrypt = false;
			ContentReader reader = new ContentReader(bookFileDir.getPath());
			Document doc = new Document();
			doc.documentId = documentId;
			doc.author = reader.getAuthor();
			doc.coverPath = reader.getCoverPath();
			doc.title = reader.getTitle();
			doc.bookPath = bookFileDir.getPath();
			doc.bookSource = filePath.getPath();
			doc.format = LocalBook.FORMAT_EPUB;
			doc.fromCloudDisk =1;//外部导入标识
			
			if (TextUtils.isEmpty(doc.title)) {
				if (bookName.endsWith(".epub") || bookName.endsWith(".EPUB")) {
					doc.title = bookName.substring(0, bookName.length() - 5);
				} else {
					doc.title = bookName;
				}
			}
			if (TextUtils.isEmpty(doc.coverPath)) {
				doc.coverPath = EpubCover.generateCover(context, doc.bookPath, doc.title);
			} else {
				File cover = new File(doc.coverPath);
				if (!cover.isFile() || !cover.exists()) {
					doc.coverPath = EpubCover.generateCover(context, doc.bookPath, doc.title);
				}
			}
			MZLog.d("wangguodong", "导入书籍封面路径：" + doc.coverPath);
			MessageDigest digest = MessageDigest.getInstance("MD5");
			File opfFile = new File(reader.getOpfPath());
			opfFileStream = new FileInputStream(opfFile);
			byte[] buffer = new byte[4096];

			int numRead = 0;
			numRead = opfFileStream.read(buffer);
			digest.update(buffer, 0, numRead);
			while ((numRead = opfFileStream.read(buffer)) > 0) {
				digest.update(buffer, 0, numRead);
			}
			opfFileStream.close();
			byte[] md5Byte = digest.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < md5Byte.length; i++) {
				String h = Integer.toHexString(0xFF & md5Byte[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}

			doc.opfMD5 = hexString.toString();
			doc.state =LocalBook.STATE_LOADED;
			if (!UiStaticMethod.isEmpty(doc.opfMD5))// 防止没有opfmd5文件的导入
			{
				MZBookDatabase.instance.updateDocument(doc);

				if (docBinds == null || docBinds.length <= 0
						|| docBinds[0] == null) {
					// 下载完成必须完成docbind的生成
					DocBind bind = new DocBind();
					bind.documentId = documentId;
					bind.userId = LoginUser.getpin();
					MZBookDatabase.instance.insertOrUpdateDocBind(bind);
					// 更新书架表
					MZBookDatabase.instance.saveToBookShelf(documentId,
							System.currentTimeMillis(), 1, bind.userId);

				} else if (docBinds.length > 0) {

					docBinds[0].documentId = documentId;
					MZBookDatabase.instance.insertOrUpdateDocBind(docBinds[0]);
					MZBookDatabase.instance.saveToBookShelf(documentId,
							System.currentTimeMillis(), 1, docBinds[0].userId);
					MZLog.d("wangguodong", "书籍已经绑定，更新docbind表");

				}

			} else {
				isError = true;
			}

		} catch (IOException e) {
			e.printStackTrace();
			isError = true;
			
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			isError = true;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			isError = true;
		} finally {
			IOUtil.closeStream(importFileStream);
			IOUtil.closeStream(importFileStream2);
			IOUtil.closeStream(bookFileStream);
			IOUtil.closeStream(opfFileStream);
		}
		if (isError) {
			MZBookDatabase.instance.deleteDocumentRecord(documentId,
					LoginUser.getpin());
			return BOOK_IMPORT_FAIL;
		}
		
		return BOOK_IMPORT_SUCCESS;
	}
	
	public static boolean isInBookCase(File item, File unZipDir)
			throws NoSuchAlgorithmException, IOException,
			XmlPullParserException {
		BufferedInputStream zipInputStream = new BufferedInputStream(
				new FileInputStream(item));
		Unzip.unzip(zipInputStream, unZipDir.getPath());
		zipInputStream.close();
		ContentReader.isNeedJDDecrypt = false;
		DigestInputStream inputStream = new DigestInputStream(
				new BufferedInputStream(new FileInputStream(
						ContentReader.getOpfPath(unZipDir.getPath()))),
				MessageDigest.getInstance("MD5"));
		while (inputStream.read() != -1)
			;
		inputStream.close();
		byte[] digestBytes = inputStream.getMessageDigest().digest();
		String md5 = new BigInteger(1, digestBytes).toString(16);
		int id = MZBookDatabase.instance.getDocmentId(md5);
		return id != -1;
	}
}
