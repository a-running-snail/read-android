package com.jingdong.app.reader.reading;

import android.os.Parcel;
import android.os.Parcelable;

public class ChapterPageIndex implements Parcelable {
    public ChapterPageIndex(String title, int start, int end) {
        this.title = title;
        pageStart = start;
        pageEnd = end;
    }

    public String title = "";
    public int pageStart = -1;
    public int pageEnd = -1;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(pageStart);
        dest.writeInt(pageEnd);
    }

    public static final Parcelable.Creator<ChapterPageIndex> CREATOR = new Parcelable.Creator<ChapterPageIndex>() {
        public ChapterPageIndex createFromParcel(Parcel in) {
            return new ChapterPageIndex(in);
        }

        public ChapterPageIndex[] newArray(int size) {
            return new ChapterPageIndex[size];
        }
    };

    private ChapterPageIndex(Parcel in) {
        title = in.readString();
        pageStart = in.readInt();
        pageEnd = in.readInt();
    }
}
