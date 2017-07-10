package com.jingdong.app.reader.reading;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.jingdong.app.reader.entity.LocalBook;

public class ReadProgress {
    public String chapterItemRef;
    @Deprecated
    public int chapterId;
    public int bookType = 0;// 书的格式 1-pdf类型 2-epub类型
    public int paraIndex = 0;
    public int offsetInPara = 0;
    public long updateTime = 0;
    public float percent = 0;
    public int operatingState = 0;//operating_state 1修改 0无修改 2添加 3删除
    public int pdfPage = 0;
    public float pdfZoom = 1;//默认是1
    public float pdfXOffsetPercent = 0;
    public float pdfYOffsetPercent = 0;
    public String chapterTitle;

    public ReadProgress clone() {
        ReadProgress copy = new ReadProgress();
        copy.chapterItemRef = this.chapterItemRef;
        copy.chapterId = this.chapterId;
        copy.bookType = this.bookType;
        copy.paraIndex = this.paraIndex;
        copy.offsetInPara = this.offsetInPara;
        copy.updateTime = this.updateTime;
        copy.percent = this.percent;
        copy.operatingState = this.operatingState;
        copy.pdfPage = this.pdfPage;
        copy.pdfZoom = this.pdfZoom;
        copy.pdfXOffsetPercent = this.pdfXOffsetPercent;
        copy.pdfYOffsetPercent = this.pdfYOffsetPercent;
        copy.chapterTitle = this.chapterTitle;
        return copy;
    }
    
	public boolean inSameLocation(ReadProgress other) {
		if (other == null || bookType != other.bookType) {
			return false;
		}
		if (bookType == LocalBook.FORMAT_EPUB) {
			if (chapterItemRef.equals(other.chapterItemRef)
					&& paraIndex == other.paraIndex
					&& offsetInPara == other.offsetInPara) {
				return true;
			} else {
				return false;
			}
		} else if (bookType == LocalBook.FORMAT_PDF) {
			if (pdfPage == other.pdfPage
					&& pdfXOffsetPercent == other.pdfXOffsetPercent
					&& pdfYOffsetPercent == other.pdfYOffsetPercent) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
    
    public static ReadProgress fromJSON(JSONObject json) {
        ReadProgress progress = new ReadProgress();
        progress.chapterItemRef = json.optString("epubItem");
        int type = json.optInt("ebookType");
        if (type == 1) {
        	progress.bookType = LocalBook.FORMAT_EPUB;
        } else if (type == 0) {
        	progress.bookType = LocalBook.FORMAT_PDF;
        }
        progress.paraIndex = json.optInt("x1");
        progress.offsetInPara = json.optInt("y1");
        String timeStr = String.valueOf(System.currentTimeMillis());
        String version = String.valueOf(json.optLong("version"));
		if (timeStr.length() == version.length()) {
			progress.updateTime = json.optLong("version")/1000;
		} else {
			progress.updateTime = json.optLong("version");
		}
        String percent = json.optString("percent");
        if (!TextUtils.isEmpty(percent)) {
        	if (percent.endsWith("%")) {
        		percent = percent.substring(0, percent.length()-1);
        		progress.percent = Float.parseFloat(percent);
        		progress.percent = progress.percent / 100;
        	} else {
        		progress.percent = Float.parseFloat(percent);
        	}
        }
        progress.pdfPage = json.optInt("offset");
		try {
			progress.pdfZoom = Float.parseFloat(json.optString("pdfScaling"));
			progress.pdfXOffsetPercent = Float.parseFloat(json.optString("pdfScalingLeft"));
			progress.pdfYOffsetPercent = Float.parseFloat(json.optString("pdfScalingTop"));
		} catch (Throwable t) {
			progress.pdfZoom = 1;
			progress.pdfXOffsetPercent = 0;
			progress.pdfYOffsetPercent = 0;
		}
        progress.chapterTitle = json.optString("epubChapterTitle");
        return progress;
    }
    
    public static JSONObject toJSON(ReadProgress progress) throws JSONException {
    	JSONObject json = new JSONObject();
    	json.put("note", "");
    	json.put("valid", 1);
    	json.put("epubItem", progress.chapterItemRef);
    	json.put("force", 2);// XXX force为2是强制更新最后阅读位置，新接口可以不用传
    	if (progress.bookType == LocalBook.FORMAT_EPUB) {
    		json.put("ebookType", 1);
    	} else if (progress.bookType == LocalBook.FORMAT_PDF) {
    		json.put("ebookType", 0);
    	}
    	json.put("dataType", 0);
    	json.put("x1", progress.paraIndex);
    	json.put("y1", progress.offsetInPara);
    	json.put("deviceTime", progress.updateTime);
    	json.put("percent", String.valueOf(progress.percent));
    	json.put("offset", progress.pdfPage);
    	json.put("pdfScaling", String.valueOf(progress.pdfZoom));
    	json.put("pdfScalingLeft", String.valueOf(progress.pdfXOffsetPercent));
    	json.put("pdfScalingTop", String.valueOf(progress.pdfYOffsetPercent));
    	json.put("epubChapterTitle", progress.chapterTitle);
    	return json;
    }
}
