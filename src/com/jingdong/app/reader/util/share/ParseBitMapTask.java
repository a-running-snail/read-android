package com.jingdong.app.reader.util.share;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.jingdong.app.reader.util.ImageUtils;


/**
 * 
 * @ClassName: ParseBitMapTask
 * @Description: 传入图片URL，生成BitMap
 * @author J.Beyond
 * @date 2015年6月23日 下午4:47:25
 *
 */
public class ParseBitMapTask extends AsyncTask<String, Void, Bitmap>{

	AyncTaskListener listener;
	
	public void setListener(AyncTaskListener listener) {
		this.listener = listener;
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		return ImageUtils.getBitmapByUrl(params[0]);
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (listener != null) {
			listener.onFinish(result);
		}
	}
}
