package com.jingdong.app.reader.entity;

import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class MyBookEntity extends Entity {
	
	public long bookId;// ebookId 图书id
	public long orderId;// orderId 订单号
	public int bookType;// 图书类型1 电子书
	public boolean alreadyDownload = false;

	public boolean downloadAble = true;// 该图书是否可下载。
	/**
	 * 价格
	 */
	public double price;
	public String bookTypeName;
	/**
	 * 图书名
	 */
	public String name;
	/**
	 * 作者
	 */
	public String author;
	/**
	 * 图书封面
	 */
	public String picUrl;
	/**
	 * 图书封面大图
	 */
	public String bigPicUrl;
	/**
	 * 格式,PDF(1, "pdf"), EPUB(2, "epub"), EXE(3, "exe"), SWF(4, "swf"), APK(10, "apk");
	 */
	public String bookFormat;// 图书的格式。
	public boolean isReadCard = false;


	@Override
	public String getImageUrl() {
		return picUrl;
	}

	@Override
	public String getHomeImageUrl() {
		return bigPicUrl;
	}

	public boolean isDownloadAble() {
		return downloadAble;
	}

	public void setDownloadAble(boolean downloadAble) {
		this.downloadAble = downloadAble;
	}

	// public boolean isAlreadyDownload(){
	// return alreadyDownload;
	// }
	//
	// public void setAlreadyDownload(boolean alreadyDownload){
	// this.alreadyDownload = alreadyDownload;
	// }

	public static MyBookEntity parse(JSONObject jsonObject) {
		// Log.i("MyBookEntity",
		// "jsonObject.toString()===" + jsonObject.toString());
		MyBookEntity book = null;
		if (jsonObject != null) {
			try {
				book = new MyBookEntity();
				book.bookId = DataParser.getLong(jsonObject,
						BookInforEntity.KEY_BOOKID);
				book.bookTypeName = DataParser.getString(jsonObject,
						OrderEntity.KEY_BOOK_TYPE);
				book.bookType = DataParser.getInt(jsonObject,
						BookInforEntity.KEY_BOOK_TYPE);
				book.picUrl = DataParser.getString(jsonObject,
						BookInforEntity.KEY_IMGURL);
				book.bigPicUrl = DataParser.getString(jsonObject,
						BookInforEntity.KEY_BIG_IMGURL);
				book.name = DataParser.getString(jsonObject,
						BookInforEntity.KEY_NAME);
				book.author = DataParser.getString(jsonObject,
						BookInforEntity.KEY_AUTHOR);
				book.orderId = DataParser.getLong(jsonObject,
						OrderEntity.KEY_ORDERID);
				book.downloadAble = isDownloadable(DataParser.getInt(
						jsonObject, OrderEntity.KEY_FORMAT));
				book.price = DataParser.getDouble(jsonObject,
						OrderEntity.KEY_PRICE);

				book.isReadCard = DataParser.getBoolean(jsonObject,
						OrderEntity.KEY_IS_READCARD);
				book.bookFormat = DataParser.getString(jsonObject,
						OrderEntity.KEY_FORMAT_NAME);
				// Log.i("zhoubo", "order.author==" + book.author);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return book;
	}

	/**
	 * @author ThinkinBunny
	 * @see 返回当前图书是否支持下载，目前支持epub&pdf
	 * **/
	public static boolean isDownloadable(int format) {

		if (format == LocalBook.FORMAT_EPUB || format == LocalBook.FORMAT_PDF) {
			return true;
		} else {
			return false;
		}

	}
}
