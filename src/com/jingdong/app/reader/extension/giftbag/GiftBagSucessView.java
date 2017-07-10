package com.jingdong.app.reader.extension.giftbag;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.util.DisplayUtil;
import com.jingdong.app.reader.util.MZLog;

public class GiftBagSucessView extends ImageView {
	Bitmap bm = null;
	Drawable mDrawable = null;
	private static int rightPadding = 265;
	private static int topPadding = 545;
	private static int leftPadding = 335;
	private GiftBagTextAdapter textAdapter = null;
	/**
	 * 是否需要缩放图片 true 代表缩放，false 不缩放
	 */
	private boolean isscale=true;
	private int width = 0;
	private int height = 0;
	private int screenWidth=0;
	private int screenHeight=0;
	private String tag="GiftBagSucessView";
	public GiftBagSucessView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {
		is = getResources().openRawResource(R.drawable.giftbag_bg2);
		 Bitmap temp= BitmapFactory.decodeStream(is);
		width = temp.getWidth();
		height = temp.getHeight();
		 rightPadding = 265;
		 topPadding = 545;
		 leftPadding = 335;
		int screen[]=getScreenWidth();
		 screenWidth=screen[0];
		 screenHeight=screen[1];
		 MZLog.d(tag, screenWidth+","+screenHeight+","+ MZBookApplication.getInstance().getResources()
					.getDisplayMetrics().densityDpi);
		float scale=width*1.0f/height;
		if(screenWidth>=1536&&screenHeight>=height){//针对宽度是1536的屏幕进行特殊处理
			screenWidth=width;
			screenHeight=height;
			isscale=false;
		}else
		if(screenWidth<=screenHeight){
			screenHeight=(int) (screenWidth/scale);
			isscale=true;
		}else{
			screenWidth=screenHeight*1/2;
			screenHeight=(int) (screenWidth/scale);
			isscale=true;
		}
		rightPadding=(int) (rightPadding*screenWidth*1.0f/width);
		leftPadding=(int)(leftPadding*screenWidth*1.0f/width);
		topPadding=(int) (topPadding*(screenHeight*1.0f/height));
		bm=big(temp,width,height,screenWidth*1.0f/width,screenHeight*1.0f/height);
		width=bm.getWidth();
		height=bm.getHeight();
	}

	public GiftBagSucessView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public GiftBagSucessView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	private InputStream is = null;
	private float x=0;
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		textheight=0;
		tempTextHeight=0;
		if(!isscale){
			
			x=screenWidth/4;
		}
		canvas.drawBitmap(bm,x, screenHeight/2, new Paint());
		setText(canvas);
		// canvas.drawText("恭喜您！", (bm.getWidth()-)/2, topPadding, paint);

	}

	private void setText(Canvas canvas) {
		if (textAdapter != null) {
			if (textAdapter.getCount() <= 0) {
				return;
			}
			Paint paint = null;
			for (int i = 0; i < textAdapter.getCount(); i++) {
				paint = getPaint(textAdapter.getTextSize(i),
						textAdapter.getTextColor(i));
				showText(canvas, textAdapter.getTextData(i), paint);
			}
		}

	}

	private double textheight = 0;
	private double tempTextHeight=0;
	private Paint getPaint(int size, int color) {
		Paint mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(5);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, MZBookApplication.getInstance().getResources()
				.getDisplayMetrics()));
		mPaint.setColor(color);
		if(tempTextHeight==0){
			FontMetrics fm = mPaint.getFontMetrics();
			tempTextHeight=Math.ceil(fm.descent - fm.ascent)+10;
		}
		
		textheight += tempTextHeight;
		return mPaint;
	}

	private void showText(Canvas canvas, String text, Paint mPaint) {
		int textLength = text.length();
		float[] widths = new float[textLength];
		mPaint.getTextWidths(text, 0, textLength, widths);
		float textWidth = getTextWidth(widths);
		int rect_width=(int) (width - rightPadding - leftPadding);
		System.out.println("rect_width="+rect_width+",textWidth="+textWidth);
		if ((rect_width-10)>100&&textWidth > (rect_width-10)) {
			mPaint.setTextSize(mPaint.getTextSize() -1);
			showText(canvas, text, mPaint);
			return;
		}
		int tempWidth=(int) (screenHeight/2);
		if(screenWidth==720&&MZBookApplication.getInstance().getResources()
				.getDisplayMetrics().densityDpi==320){
			FontMetrics fm = mPaint.getFontMetrics();
			
			tempWidth=(int) (height/2);
			System.out.println("tempTextHeight="+tempTextHeight+",tempWidth="+tempWidth+",topPadding="+topPadding);
		}
		canvas.drawText(text,
				(x+rightPadding / 2 + (width - leftPadding) / 2) * 1.0f,
				(float) (1.0f * (topPadding + textheight+(tempWidth))), mPaint);
	}

	private float getTextWidth(float[] widths) {
		float sum = 0;
		for (float s : widths) {
			sum += s;
		}
		return sum;
	}

	public GiftBagTextAdapter getTextAdapter() {
		return textAdapter;
	}

	public void setTextAdapter(GiftBagTextAdapter textAdapter) {
		this.textAdapter = textAdapter;

	}

	public void notifyData() {
		invalidate();
	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		bm.recycle();
		bm = null;
		try {
			if (is != null) {
				is.close();
				is = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int[] getScreenWidth() {
		int []screen={DisplayUtil.getWidth(),DisplayUtil.getHeight()};
		return screen;
	}
	/**缩小比例
	 * @param bitmap
	 * @return
	 */
	private static Bitmap big(Bitmap bitmap,int widh,int height,float Xscale,float yScale) {
		  Matrix matrix = new Matrix(); 
		  matrix.postScale(Xscale,yScale); //长和宽放大缩小的比例
		  Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,widh,height,matrix,true);
		  return resizeBmp;
		 }
	
}
