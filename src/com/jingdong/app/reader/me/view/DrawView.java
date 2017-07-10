package com.jingdong.app.reader.me.view;

import org.xml.sax.Attributes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class DrawView extends View {

	public DrawView(Context context) {
		super(context);
	}
	
	public DrawView(Context context,AttributeSet attr) {
		super(context,attr);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int x = this.getWidth()/2;
		int y = this.getHeight()/2;
		int r = x;
		// 创建画笔  
        Paint p = new Paint();
        p.setAntiAlias(true);// 去除画笔的锯齿效果
        p.setColor(Color.WHITE);
        canvas.drawCircle(x, y, r, p);
        p.setColor(Color.RED);// 设置红色
        canvas.drawCircle(x, y, r-4, p);
        p.setColor(Color.WHITE);// 设置白色画笔
        canvas.drawCircle(x, y, r-10, p);
 	}

}
