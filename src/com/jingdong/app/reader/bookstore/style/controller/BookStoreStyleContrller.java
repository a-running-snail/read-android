package com.jingdong.app.reader.bookstore.style.controller;

public class BookStoreStyleContrller {

	public static final int TYPE_CIRCLE_LABEL_1 = 1;// 顶部圆圈TYPE1
	public static final int TYPE_CIRCLE_LABEL_2 = 2;// 顶部圆圈TYPE2
	public static final int TYPE_BOOK_BANNER = 7;// 滑动banner TYPE7

	public static final int TYPE_CIRCLE_LABEL_3 = 3;// 顶部圆圈TYPE3
	public static final int TYPE_CIRCLE_LABEL_4 = 4;// 顶部圆圈TYPE4
	public static final int TYPE_BOOK_LIST = 5;// 书籍列表
	
	public static final int TYPE_BOOK_LIST_GRID = 51;// 网格书籍TYPE5//1:全封面; 2:全列表;
	public static final int TYPE_BOOK_LIST_VERTICAL = 52;// 排行榜TYPE5//1:全封面; 2:全列表;
	public static final int TYPE_SPECIAL_THEME = 6;// 特辑TYPE6
	public static final int TYPE_BOOK_CATEGORY = 9;// 分类 TYPE9
	public static final int TYPE_SPECIAL_PRICE = 10;// 分类 TYPE10 限时特价

	// 注意：moduleType=9，没有子模块信息，因此不需要调用该接口接口。
	// moduleType=3或者moduleType=4返回一种类型的结果；
	// moduleType=1或者moduleType=2或者moduleType=7返回一种类型的结果；
	// moduleType=5或者moduleType=6或者moduleType=8返回一种类型的结果；
	// 具体返回结果类型参见下面的返回结果。
	// 参数含义：
	// moduleType：
	// 1:专题标签-固定
	// 2:专题标签-自定义
	// 3:分类模块
	// 4:关键字模块
	// 5:推荐模块
	// 6:特辑模块
	// 7:单条主题/广告模块
	// 8:限时免费
	// 9:分类列表页

}
