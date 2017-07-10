package com.jingdong.app.reader.book;

import android.os.Parcel;
import android.os.Parcelable;

public class EBook implements Parcelable{
    public int ebookId;
    public long bookId;
    public String title;
    public String authorName;
    public String cover;
    public long purchaseTime;
    public int edition;
    public String source;
    public int entityId;
    public double percent;
    //最后阅读时间
    public long readAt;
    public float price;
    
    public EBook() {
        
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ebookId);
        dest.writeLong(bookId);
        dest.writeString(title);
        dest.writeString(authorName);
        dest.writeString(cover);
        dest.writeLong(purchaseTime);
        dest.writeInt(edition);
        dest.writeInt(entityId);
        dest.writeDouble(percent);
        //最后阅读时间
        dest.writeLong(readAt);
        dest.writeFloat(price);
        dest.writeString(source);
    }
    
    public static final Parcelable.Creator<EBook> CREATOR = new Parcelable.Creator<EBook>() {
        public EBook createFromParcel(Parcel in) {
            return new EBook(in);
        }

        public EBook[] newArray(int size) {
            return new EBook[size];
        }
    };
    
    private EBook(Parcel in) {
        ebookId = in.readInt();
        bookId = in.readLong();
        title = in.readString();
        authorName = in.readString();
        cover = in.readString();
        purchaseTime = in.readLong();
        edition = in.readInt();
        entityId = in.readInt();
        percent = in.readDouble();
        //最后阅读时间
        readAt = in.readInt();
        price=in.readFloat();
        source=in.readString();
    }
}
