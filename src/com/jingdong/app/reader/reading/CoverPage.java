package com.jingdong.app.reader.reading;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.jingdong.app.reader.epub.paging.Page;
import com.jingdong.app.reader.util.ImageUtils;

public class CoverPage extends Page {

    private Page nextPage;
    private Bitmap bitmap;
    private String coverPath;
    private int screenWidth;
    private int screenHeight;
    
    public CoverPage(Page nextPage, String coverPath, int screenWidth, int screenHeight) {
        super();
        this.nextPage = nextPage;
        this.coverPath = coverPath;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    public Page getNextPage() {
        return nextPage;
    }
    
    public void release() {
    	if (bitmap != null && !bitmap.isRecycled()) {
    		bitmap.recycle();
    	}
    	bitmap = null;
    }
    
    public void buildPageContent() {
    	if (bitmap == null || bitmap.isRecycled()) {
    		bitmap = ImageUtils.getBitmapFromNamePath(coverPath, screenWidth, screenHeight);
    		if (bitmap == null) {
    			return;
    		}
            
            float newWidth = screenWidth;
            float newHeight = screenHeight;
            int imageHeight = bitmap.getHeight();
            int imageWidth = bitmap.getWidth();
            if (imageWidth < screenWidth && imageHeight < screenHeight) {
                // scale to fit screen
                float xScale = newWidth / imageWidth;
                float yScale = newHeight / imageHeight;

                Matrix matrix = new Matrix();  
                if (xScale < yScale) {
                    matrix.preScale(xScale, xScale);  
                } else {
                    matrix.preScale(yScale, yScale); 
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageHeight, matrix, true);
            }
    	}
    }
    
    public void draw(Canvas canvas) {
    	if (bitmap != null && !bitmap.isRecycled()) {
    		int x = 0;
    		int y = 0;
    		if (bitmap.getWidth() < screenWidth) {
    			x = (screenWidth - bitmap.getWidth()) / 2;
    		}
    		if (bitmap.getHeight() < screenHeight) {
    			y = (screenHeight - bitmap.getHeight()) / 2;
    		}
    		Paint coverPaint = new Paint();
    		canvas.drawColor(0xFF000000);
    		canvas.drawBitmap(bitmap, x, y, coverPaint);
    	}
    }
}
