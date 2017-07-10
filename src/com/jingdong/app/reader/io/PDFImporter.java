package com.jingdong.app.reader.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.text.TextUtils;

import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.pdf.PDFCover;
import com.jingdong.app.reader.user.LoginUser;


public class PDFImporter {

	static synchronized public int importBook(String bookName, File filePath,
			Activity activity, DocBind... docBinds) {
		if (bookName == null) {
			bookName = filePath.getName();
		}

		if (TextUtils.isEmpty(bookName)) {
			return EpubImporter.BOOK_IMPORT_FAIL;
		}
		
		boolean isError = false;
		boolean isExist = false;
		String md5 = null;
		DigestInputStream inputStream = null;
		try {
			inputStream = new DigestInputStream(new BufferedInputStream(
					new FileInputStream(filePath.getAbsolutePath())),
					MessageDigest.getInstance("MD5"));
			int totalBytes = 0;
			int maxLength = 1024 * 100;
			while (inputStream.read() != -1) {
				totalBytes++;
				if (totalBytes == maxLength) {
					break;
				}
			}
			inputStream.close();
			byte[] digestBytes = inputStream.getMessageDigest().digest();
			md5 = new BigInteger(1, digestBytes).toString(16);
			int id = MZBookDatabase.instance.getDocmentId(md5);
			if (id != -1) {
				isExist = true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			isError = true;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			isError = true;
		} catch (IOException e) {
			e.printStackTrace();
			isError = true;
		} finally {
			IOUtil.closeStream(inputStream);
		}
		if (isError || TextUtils.isEmpty(md5)) {
			return EpubImporter.BOOK_IMPORT_FAIL;
		}
		if (isExist) {
			return EpubImporter.BOOK_IS_EXIST;
		}

		int documentId = MZBookDatabase.instance.createDocumentRecord();

		Document doc = new Document();
		doc.documentId = documentId;
		doc.author = "";
		doc.coverPath = "";
		doc.title = "";
		doc.opfMD5 = md5;
		doc.bookPath = filePath.getAbsolutePath();
		doc.bookSource =filePath.getAbsolutePath();
		doc.state =LocalBook.STATE_LOADED;
		doc.format = LocalBook.FORMAT_PDF;
		doc.fromCloudDisk =1;//外部导入标识
		if (TextUtils.isEmpty(doc.title)) {
			if (bookName.endsWith(".pdf") || bookName.endsWith(".PDF")) {
				doc.title = bookName.substring(0, bookName.length() - 4);
			} else {
				doc.title = bookName;
			}
		}
		
		try {
			PDFCover.saveCoverFromPdf(doc, activity);
			MZBookDatabase.instance.updateDocument(doc);
			if (docBinds == null || docBinds.length <= 0 || docBinds[0] == null) {
				DocBind bind = new DocBind();
				bind.documentId = documentId;
				bind.userId = LoginUser.getpin();
				MZBookDatabase.instance.insertOrUpdateDocBind(bind);
				// 更新书架表
				MZBookDatabase.instance.saveToBookShelf(documentId,
						System.currentTimeMillis(), 1, bind.userId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MZBookDatabase.instance.deleteDocumentRecord(documentId,
					LoginUser.getpin());
			isError = true;
		}
		if (isError) {
			return EpubImporter.BOOK_IMPORT_FAIL;
		}
		
		return EpubImporter.BOOK_IMPORT_SUCCESS;
	}
	
}
