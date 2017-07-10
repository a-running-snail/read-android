package com.jingdong.app.reader.pdf;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;

import com.artifex.mupdfdemo.MuPDFCore;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.io.StoragePath;
import com.jingdong.app.reader.util.ImageTool;

public class PDFCover {

    private static final int COVER_WIDTH    = 330;
    private static final int COVER_HEIGHT   = 430;
    // (330, 430), (110, 130), (150, 170)

    public static void saveCoverFromPdf(final Document book, final Activity activity) throws Exception {

        File fileDir = new File(StoragePath.getDocumentDir(activity), book.getTitle());

        if (!fileDir.exists())
        {
            fileDir.mkdirs();
        }

        PDFDeviceInfo.resetInfo();

        PDFDeviceInfo.KeyBook = "".getBytes();
        PDFDeviceInfo.KeyUUID = "".getBytes();
        PDFDeviceInfo.KeyRand = "".getBytes();

        MuPDFCore core = new MuPDFCore(book.bookPath);
        core.countPages();
        Bitmap bitmap = core.drawPage(0, COVER_WIDTH, COVER_HEIGHT, 0, 0, COVER_WIDTH, COVER_HEIGHT);
        String big_bm_path = fileDir.getPath() + "/" + book.title + ".jpg";
        ImageTool.saveFile(bitmap, big_bm_path);
        book.coverPath = big_bm_path;
        MZBookDatabase.instance.updateDocument(book);
        bitmap.recycle();
        core.onDestroy();
        PDFDeviceInfo.resetInfo();
    }
}
