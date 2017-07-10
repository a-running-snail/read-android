package com.jingdong.app.reader.reading;

import java.io.FileOutputStream;

import com.android.mzbook.sortview.optimized.ImageSizeUtils;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.StringUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Align;
import android.text.TextUtils;
import android.widget.LinearLayout;

public class EpubCover extends LinearLayout{

	int width = 0;
	int height = 0;
	int bgColorIndex = 0;
	float textHeight = 0;
	String bookName = null;
	String dividerCode = null;
	Paint paint = null;
	Bitmap bitmap = null;
	StringBuffer buffer = new StringBuffer();
	
	public EpubCover(Context context) {
		super(context);
		byte[] byteArray = StringUtil.hexStringToBytes("c2ad");
		dividerCode = new String(byteArray);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int bgColor = 0xffded5c3;
		switch (bgColorIndex) {
		case 1:
			bgColor = 0xffe4773b;
			break;
		case 2:
			bgColor = 0xff68b0b4;
			break;
		case 3:
			bgColor = 0xff68b0b4;
			break;
		case 4:
			bgColor = 0xff5595af;
			break;
		default:
			bgColor = 0xffded5c3;
			break;
		}
		canvas.drawColor(bgColor);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		float x = 0;
		float y = textHeight + textHeight / 2;
		String[] text = buffer.toString().split(dividerCode);
		for (int i = 0; i < text.length; i++) {
			x = (width - paint.measureText(text[i])) / 2;
			canvas.drawText(text[i], x, y, paint);
			y += textHeight;
		}
	}
	
	private void textBuilder() {
		float[] widths = new float[bookName.length()];
		paint.getTextWidths(bookName, widths);
		FontMetrics fm = paint.getFontMetrics(); 
		float textWidth = 0;
		textHeight = fm.descent - fm.ascent;
		for (int i = 0, n = 0; i < widths.length; i++) {
			if (n >= 3) {//只显示三行
				break;
			}
			textWidth += widths[i];
			if (i+1 < widths.length && textWidth + widths[i+1] > width) {
				i --;
				n ++;
				textWidth = 0;
				buffer.append(dividerCode);
				continue;
			}
			buffer.append(bookName.substring(i, i+1));
		}
	}
	
	public static String generateCover(Context context, String path, String name) {
		if (TextUtils.isEmpty(path)) {
			return "";
		}
		ImageSizeUtils utils = new ImageSizeUtils(context);
		int width = (int) utils.getPerItemImageWidth();
		int height = (int) utils.getPerItemImageHeight();
		EpubCover cover = new EpubCover(context);
		cover.bgColorIndex = ((int)System.currentTimeMillis() % 5);
		cover.width = width;
		cover.height = height;
		cover.bookName = name;
		cover.paint = new Paint();
		cover.paint.setColor(0xFF000000);
		cover.paint.setTextAlign(Align.LEFT);
		cover.paint.setTextSize(ScreenUtils.dip2pxf(14));
		cover.paint.setAntiAlias(true);
		cover.paint.setSubpixelText(true);
		cover.textBuilder();
		Bitmap src = ImageUtils.getBitmapFromResource(context,
				R.drawable.book_cover_default_list, width, height,
				Bitmap.Config.ARGB_8888);
		cover.bitmap = Bitmap.createScaledBitmap(src, width, height, true);
		
		Bitmap screenshot = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		cover.draw(new Canvas(screenshot));
		
		String filename = path + "/tempCover.png";
		if (screenshot != null) {
			try {
				FileOutputStream out = new FileOutputStream(filename);
				screenshot.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
				filename = "";
			} finally {
				screenshot.recycle();
			}
		}
		return filename;
	}
}
