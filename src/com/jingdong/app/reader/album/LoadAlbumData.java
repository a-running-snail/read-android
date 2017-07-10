package com.jingdong.app.reader.album;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore.Images.Media;

public class LoadAlbumData extends AsyncTask<String, String, String>{
	private AlbumDataCallBack mAlbumDataCallBack = null;
	private List<ImageData> mDataList = null;
	private ContentResolver mContentResolver = null;
	
	public LoadAlbumData(Context context, AlbumDataCallBack _mAlbumDataCallBack) {
		this.mAlbumDataCallBack = _mAlbumDataCallBack;
		this.mDataList = new ArrayList<ImageData>();
		this.mContentResolver = context.getContentResolver();
	}

	@Override
	protected String doInBackground(String... arg0) {
		buildImagesBucketList();
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		try {
			Collections.reverse(mDataList); 
		}catch(UnsupportedOperationException e) {
			e.printStackTrace();
		}
		mAlbumDataCallBack.onSuccess(mDataList);
	}
	
	void buildImagesBucketList() {
		String columns[] = new String[] { Media._ID, Media.DATA };
		Cursor cur = mContentResolver.query(Media.EXTERNAL_CONTENT_URI, columns, null, null, null);
		if (cur.moveToFirst()) {
//			int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
			int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);

			do {
//				String _id = cur.getString(photoIDIndex);
				String path = cur.getString(photoPathIndex);
				File file = new File(path);
				if(file.exists()) {
					ImageData img = new ImageData();
					img.imagePath = path;
					mDataList.add(img);
				}
				
			} while (cur.moveToNext());
			
		}
	}

}
