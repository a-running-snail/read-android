package com.jingdong.app.reader.reading;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class ReadSearchData implements Parcelable{

	private String title;//章节名
	private String searchText;
	private int chapterIndex;
	private int paraIndex;
	private int startOffsetInPara;//关键词第一个字符的位置
	private int endOffsetInPara;//关键词最后一个字符的后一个字符位置
	private List<Integer> offsetList = new ArrayList<Integer>();//关键词第一个字符位置的集合
	
	public ReadSearchData() {}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public int getChapterIndex() {
		return chapterIndex;
	}

	public void setChapterIndex(int chapterIndex) {
		this.chapterIndex = chapterIndex;
	}

	public int getParaIndex() {
		return paraIndex;
	}

	public void setParaIndex(int paraIndex) {
		this.paraIndex = paraIndex;
	}

	public int getStartOffsetInPara() {
		return startOffsetInPara;
	}

	public void setStartOffsetInPara(int offsetInPara) {
		this.startOffsetInPara = offsetInPara;
	}

	public int getEndOffsetInPara() {
		return endOffsetInPara;
	}

	public void setEndOffsetInPara(int endOffsetInPara) {
		this.endOffsetInPara = endOffsetInPara;
	}
	
	public void addOffset(int offset) {
		offsetList.add(Integer.valueOf(offset));
	}
	
	public List<Integer> getOffsetList() {
		return offsetList;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(searchText);
		dest.writeInt(chapterIndex);
        dest.writeInt(paraIndex);
        dest.writeInt(startOffsetInPara);
        dest.writeInt(endOffsetInPara);
	}
	
	public static final Parcelable.Creator<ReadSearchData> CREATOR = new Parcelable.Creator<ReadSearchData>() {
        public ReadSearchData createFromParcel(Parcel in) {
            return new ReadSearchData(in);
        }

        public ReadSearchData[] newArray(int size) {
            return new ReadSearchData[size];
        }
    };

    private ReadSearchData(Parcel in) {
        title = in.readString();
        searchText = in.readString();
        chapterIndex = in.readInt();
        paraIndex = in.readInt();
        startOffsetInPara = in.readInt();
        endOffsetInPara = in.readInt();
    }
}
