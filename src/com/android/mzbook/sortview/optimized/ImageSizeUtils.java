package com.android.mzbook.sortview.optimized;

import android.content.Context;

import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.R;
/**
 * 书架拖动布局
 * @author WANGGUODONG 
 * time:2014 -7 17
 */
public class ImageSizeUtils {

	private Context mContext;

	private int defaultColumn = 3;// 书架默认列数
	private int width;// 屏幕宽度
	private int perItemWidth;// 按列数平分后每列宽度
	private int perItemImageWidth;// 计算后每本书的封面宽度
	private int perItemImageHeight;// 计算后每本书的封面高度
	private int perSubGridItemImageWidth;// 计算后每个文件夹中书的封面宽度
	private int perSubGridItemImageHeight;// 计算后每个文件夹中书的封面高度

	public ImageSizeUtils(Context context) {
		this.mContext = context;

		defaultColumn = mContext.getResources().getInteger(R.integer.default_books_case_column);
		width = (int) ScreenUtils.getWidthJust(mContext);
		perItemWidth = (width - ScreenUtils.dip2px(mContext,32 + (defaultColumn - 1) * 11)) / defaultColumn;
		perItemImageWidth = perItemWidth;
		perItemImageHeight = 4 * perItemImageWidth / 3;
		perSubGridItemImageHeight = (perItemImageHeight - ScreenUtils.dip2px(mContext, 32)) / 2;
		perSubGridItemImageWidth = (perItemImageWidth - ScreenUtils.dip2px(mContext, 24)) / 2;
	}

	public int getWidth() {
		return width;
	}

	public int getPerItemWidth() {
		return perItemWidth;
	}

	public int getPerItemImageWidth() {
		return perItemImageWidth;
	}

	public int getPerItemImageHeight() {
		return perItemImageHeight;
	}

	public int getPerSubGridItemImageWidth() {
		return perSubGridItemImageWidth;
	}

	public int getPerSubGridItemImageHeight() {
		return perSubGridItemImageHeight;
	}

}
