package com.jingdong.app.reader.album;

import java.io.Serializable;

public class ImageData implements Serializable {
	/** 照片原始图片路径 */
	public String imagePath;
	/** 存储照片选中状态 */
	public boolean isSelected = false;
}
