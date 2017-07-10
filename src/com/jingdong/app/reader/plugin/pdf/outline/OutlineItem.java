package com.jingdong.app.reader.plugin.pdf.outline;

import android.os.Parcel;
import android.os.Parcelable;

public class OutlineItem implements Parcelable {
	public final int level;
	public final String title;
	public final int page;//epub是段 pdf是页
	public  int outLineId;
	public boolean hasContent=true;//是否有对应的内存存在。默认为true；

	public OutlineItem(int _level, String _title, int _page) {
		level = _level;
		title = _title;
		page = _page;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(level);
		dest.writeString(title);
        dest.writeInt(page);
	}
	
    public static final Parcelable.Creator<OutlineItem> CREATOR = new Parcelable.Creator<OutlineItem>() {
        public OutlineItem createFromParcel(Parcel in) {
            return new OutlineItem(in);
        }

        public OutlineItem[] newArray(int size) {
            return new OutlineItem[size];
        }
    };

    private OutlineItem(Parcel in) {
    	level = in.readInt();
        title = in.readString();
        page = in.readInt();
    }
}
