package com.jingdong.app.reader.epub.epub;

import android.os.Parcel;
import android.os.Parcelable;

public class PlayItem implements Parcelable{
    public PlayItem() {
        
    }
    public String id;
    public String title;
    public String author;
    public String navSrc;
    public String navTitle;
    public String mediaPath;
    
    
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(navSrc);
        dest.writeString(navTitle);
        dest.writeString(mediaPath);
    }
    
    public static final Parcelable.Creator<PlayItem> CREATOR = new Parcelable.Creator<PlayItem>() {
        public PlayItem createFromParcel(Parcel in) {
            return new PlayItem(in);
        }

        public PlayItem[] newArray(int size) {
            return new PlayItem[size];
        }
    };
    
    private PlayItem(Parcel in) {
        id = in.readString();
        title = in.readString();
        author = in.readString();
        navSrc = in.readString();
        navTitle = in.readString();
        mediaPath = in.readString();
    }
}
