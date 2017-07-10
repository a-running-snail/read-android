package com.jingdong.app.reader.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.jingdong.app.reader.entity.AccountEntity;
import com.jingdong.app.reader.entity.AdEntity;
import com.jingdong.app.reader.entity.Bean;
import com.jingdong.app.reader.entity.BookInforEDetail;
import com.jingdong.app.reader.entity.BookInforEntity;
import com.jingdong.app.reader.entity.BookIntroduceEntity;
import com.jingdong.app.reader.entity.BookShortMsgEntity;
import com.jingdong.app.reader.entity.CateEntity;
import com.jingdong.app.reader.entity.CommentEntity;
import com.jingdong.app.reader.entity.MyBookEntity;
import com.jingdong.app.reader.entity.MyJDCountEntity;
import com.jingdong.app.reader.entity.MyJdECardEntity;
import com.jingdong.app.reader.entity.MyOnlineReadCardEntity;
import com.jingdong.app.reader.entity.MyOrderListEntity;
import com.jingdong.app.reader.entity.OnlineReadCardEntity;
import com.jingdong.app.reader.entity.OrderDetailEntity;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.PageEntity;
import com.jingdong.app.reader.entity.RecommendBookDetailEntity;
import com.jingdong.app.reader.entity.ReturnStatus;
import com.jingdong.app.reader.entity.ShowOrderEntity;
import com.jingdong.app.reader.entity.TryReadCatalogEntity;
import com.jingdong.app.reader.plugin.FontItem;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.StreamToolBox;

public class DataParser {
	public final static String KEY_MESSAGE = "message";
	public final static String KEY_RESULTLIST = "resultList";
	public final static String KEY_RESULT = "result";
	public final static String KEY_ONLINE_LIST = "readbookList";
	public final static String KEY_LIST = "list";
	public final static String KEY_BOOK_LIST = "bookList";
	public final static String KEY_CODE = "code";

	private final static String KEY_CURRENT_PAGE = "currentPage";
	private final static String KEY_TOTAL_PAGE = "totalPage";
	// private final static String KEY_TOTAL_COUNT = "totalCount";
	private final static String KEY_RESULT_COUNT = "resultCount";
	private final static String KEY_BOOKLIST = "bookList";
	// private final static String KEY_BOOKDETAILLIST = "bookDetailList";
	private final static String KEY_BOOKINFO = "bookInfo";
	private final static String KEY_CATELIST = "catList";
	private final static String KEY_PICLIST = "picList";
	// private final static String KEY_ORDERLIST = "orderList";
	private final static String KEY_CARDLIST = "cardList";
	private final static String KEY_READLIST = "catalogList";
	private final static String KEY_COMMENTLIST = "commentList";
	// private final static String KEY_ECARDLIST = "eCardList";
	private final static String KEY_RECOMMEND = "recommend";
	public final static String KEY_ERRO = "error";

	// private final static int PAGE_COUNT = 20;

	/**
	 * 将输入流转化为字符串输出
	 * 
	 * @param is
	 * @return
	 */
	public static final String getStringFromInputStream(InputStream is) {
		if (is != null) {
			BufferedReader br;
			StringBuffer sjson = new StringBuffer();
			try {
				br = new BufferedReader(new InputStreamReader(is, "utf-8"));
				String rs = "";
				while ((rs = br.readLine()) != null) {
					sjson.append(rs);
				}
				br.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sjson.toString();
		}
		return "";
	}

	public static String getErro(String josn) {
		String erro = "";
		try {
			JSONObject objosn = new JSONObject(josn);
			erro = DataParser.getString(objosn, KEY_ERRO);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return erro;
	}

	public static ReturnStatus parserReturnStatus(String josn) {
		// String erro = "";
		ReturnStatus returnStatus = null;
		returnStatus = new ReturnStatus();
		if(!TextUtils.isEmpty(josn)){
		try {
			JSONObject objosn = new JSONObject(josn);
			returnStatus.code = DataParser.getInt(objosn, KEY_CODE);
			returnStatus.massage = DataParser.getString(objosn, KEY_MESSAGE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return returnStatus;
	}

	/**
	 * 解析获取列表时的数据，
	 * 
	 * @param parseBookInforList
	 *            解析赠送图书的tab数据
	 * 
	 */
	public static final PageEntity parseBookInforList(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return BookInforEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_BOOKLIST, ParserHanlde);
	}

	/**
	 * 解析获取列表时的数据，搜索的价格字段有区别
	 * 
	 * @return
	 */
	public static final PageEntity parseSearchBookInforList(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return BookInforEntity.parseSearch(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_BOOKLIST, ParserHanlde);
	}

	/**
	 * 解析搜索列表数据，
	 * 
	 * @return
	 * @SuppressWarnings("unchecked") public static final PageEntity
	 *                                parseSearchBookInforList(JSONObject
	 *                                jsonObj) { ParserObjectHanlde ParserHanlde
	 *                                =new ParserObjectHanlde(){
	 * @Override public Object praserObject(JSONObject jSONObject) { return
	 *           BookInforEntity.parse(jSONObject); } }; return
	 *           parsePageEntity(jsonObj, KEY_BOOKDETAILLIST, ParserHanlde); }
	 */
	/**
	 * 解析获取详情时候的数据，
	 * 
	 * @return
	 */
	public static final PageEntity parseBookdetailList(JSONObject jsonObj) {
		PageEntity pageEntity = null;
		// ArrayList list = null;
		JSONObject jsonList = null;
		pageEntity = new PageEntity();
		try {
			jsonList = jsonObj.getJSONObject(KEY_BOOKINFO);
			pageEntity.code = getString(jsonObj, KEY_CODE);
			if (pageEntity.code.equals("0")) {
				pageEntity.obj = BookInforEDetail.parse(jsonList);
				pageEntity.isSuccess = true;
				pageEntity.total = 1;
				pageEntity.currentPage = 0;
			} else {
				pageEntity.isSuccess = false;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pageEntity;
	}

	/**
	 * 解析收藏获取列表时的数据，
	 * 
	 * @return
	 */
	public static final PageEntity parseBookInforListCollect(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return BookInforEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_RESULTLIST, ParserHanlde);
	}

	/**
	 * 解析我的畅读列表。
	 * 
	 */
/*
	public static final PageEntity parseMyOnlineReadBook(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return MyOnlineBookEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_RESULTLIST, ParserHanlde);
	}

	/**
	 * 解析我购买的的畅读卡列表。
	 * 
	 */
	private static final String KEY_NOLINE_READ_CARD = "readCardList";

	public static final PageEntity parseMyOnlineReadCard(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return MyOnlineReadCardEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_NOLINE_READ_CARD, ParserHanlde);
	}

	/**
	 * 解析销售的畅读卡列表。
	 */
	private static final String KEY_NOLINE_CARD = "cardList";

	public static final PageEntity parseOnlineReadCard(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return OnlineReadCardEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_NOLINE_CARD, ParserHanlde);
	}

	/**
	 * 解析商品介绍列表，
	 * 
	 * @return
	 */
	public static final PageEntity parsebookIntroDetail(JSONObject jsonObj) {
		PageEntity pageEntity = null;
		JSONObject jsonList = null;
		pageEntity = new PageEntity();
		try {
			jsonList = jsonObj.getJSONObject(KEY_BOOKINFO);
			pageEntity.code = getString(jsonObj, KEY_CODE);
			if (pageEntity.code.equals("0")) {
				pageEntity.list = BookIntroduceEntity.parse(jsonList).key2valueList;
				pageEntity.isSuccess = true;
				pageEntity.total = 1;
			} else {
				pageEntity.isSuccess = false;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pageEntity;
	}

	/**
	 * 解析商品介绍详情，请求类型 1：内容摘要 2：作者简介 3：图书目录
	 * 
	 * @return
	 */
	
	public static final PageEntity parsebookIntroInfo(JSONObject jsonObj) {
		PageEntity pageEntity = null;
		ArrayList<String> list = null;
		pageEntity = new PageEntity();
		pageEntity.code = getString(jsonObj, KEY_CODE);
		if (pageEntity.code.equals("0")) {
			String info = getString(jsonObj, KEY_BOOKINFO);
			list = new ArrayList<String>();
			list.add(info);
			pageEntity.list = list;
			pageEntity.isSuccess = true;
			pageEntity.total = 1;
		} else {
			pageEntity.isSuccess = false;
		}
		return pageEntity;
	}

	/**
	 * 解析获取列表时的数据，
	 * 
	 * @return
	 */
	public static final PageEntity parseAdEntityList(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return AdEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_PICLIST, ParserHanlde);
	}

	/**
	 * 解析图书详情-- 推荐列表，
	 * 
	 * @return
	 */
	public static final PageEntity parseRecommendBookDetailEntityList(
			JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return RecommendBookDetailEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_RECOMMEND, ParserHanlde);
	}

	/**
	 * 解析图书详情-- 库存状态，
	 * 
	 * @return
	 * 
	 *         <<<<<<< .mine
	 * @SuppressWarnings("unchecked") public static final PageEntity
	 *                                parsebookStockEntityList( JSONObject
	 *                                jsonObject) { PageEntity pageEntity =
	 *                                null; // ArrayList<Object> list = null;
	 *                                pageEntity = new PageEntity();
	 *                                pageEntity.code = getString(jsonObject,
	 *                                KEY_CODE); if
	 *                                (pageEntity.code.equals("0")) { //
	 *                                list=new ArrayList<Object>();
	 *                                BookStocksEntity stock =
	 *                                BookStocksEntity.parse(jsonObject);
	 *                                pageEntity.currentPage = 0;
	 *                                pageEntity.total = 1; pageEntity.obj =
	 *                                stock; // list.add(stock);
	 *                                pageEntity.isSuccess = true; } else {
	 *                                pageEntity.isSuccess = false; } return
	 *                                pageEntity; } ======= //
	 *                                @SuppressWarnings("unchecked") // public
	 *                                static final PageEntity
	 *                                parsebookStockEntityList( // JSONObject
	 *                                jsonObject) { // PageEntity pageEntity =
	 *                                null; // // ArrayList<Object> list = null;
	 *                                // pageEntity = new PageEntity(); //
	 *                                pageEntity.code = getString(jsonObject,
	 *                                KEY_CODE); // if
	 *                                (pageEntity.code.equals("0")) { // //
	 *                                list=new ArrayList<Object>(); // //
	 *                                pageEntity.currentPage = 0; //
	 *                                pageEntity.total = 1; // // //
	 *                                list.add(stock); // pageEntity.isSuccess =
	 *                                true; // } else { // pageEntity.isSuccess
	 *                                = false; // } // return pageEntity; // }
	 *                                >>>>>>> .r2221
	 */
	/**
	 * 
	 * 解析我的优惠券的数据
	 * 
	 * @return
	 */
	public static final PageEntity parseCouponEntityList(JSONObject jsonObject) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return MyJdECardEntity.parse(jSONObject);
			}
		};

		PageEntity pageEntity = parsePageEntity(jsonObject, KEY_RESULTLIST,
				ParserHanlde);
		if (pageEntity.isSuccess) {
			pageEntity.currentPage = 0;
			pageEntity.total = 1;
		}
		return pageEntity;

	}

	/**
	 * 解析我的余额的数据，。
	 * 
	 * @param jsonObject
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final PageEntity parseMyAccountEntityList(
			JSONObject jsonObject) {
		PageEntity pageEntity = new PageEntity();
		AccountEntity obj = AccountEntity.parse(jsonObject);
		pageEntity.list = new ArrayList();
		if (obj != null) {
			pageEntity.isSuccess = true;
			pageEntity.currentPage = 0;
			pageEntity.total = 1;
			pageEntity.code = obj.code;
			pageEntity.list.add(obj);
		} else {
			pageEntity = null;
		}
		return pageEntity;
	}

	/**
	 * 解析用户评论的数据，
	 * 
	 * @return
	 */
	public static final PageEntity parsecommentList(JSONObject jsonObject) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return CommentEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObject, KEY_COMMENTLIST, ParserHanlde);
	}

	/**
	 * 解析我的京东数量的数据，
	 * 
	 * @return
	 */
	public static final MyJDCountEntity parsemyJdCount(JSONObject jsonObject) {
		MyJDCountEntity myjdCount = null;
		myjdCount = MyJDCountEntity.parse(jsonObject);
		// }
		return myjdCount;
	}

	/**
	 * 解析我的订单列表的数据，
	 * 
	 * @return
	 */
	public static final PageEntity parsemyOrderList(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return MyOrderListEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_RESULTLIST, ParserHanlde);
	}

	/**
	 * 解析获取列表时的数据，
	 * 
	 * @return
	 */
	public static final PageEntity parseorderList(JSONObject jsonObj) {
		MZLog.d("zhoubo", "jsonObj===" + jsonObj.toString());
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return OrderEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_RESULTLIST, ParserHanlde);
	}

	/**
	 * 解析获取我的详细订单中的图书列表
	 * 
	 * @return
	 */
	public static final PageEntity parseBookList(JSONObject jsonObj) {
		MZLog.d("zhoubo", "jsonObj===" + jsonObj.toString());
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return MyBookEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_RESULTLIST, ParserHanlde);
	}

	/**
	 * 我的京东E卡列表，
	 * 
	 * @return
	 */
	public static final PageEntity parsemyjdECardList(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return MyJdECardEntity.parse(jSONObject);
			}
		};

		PageEntity pageEntity = parsePageEntity(jsonObj, KEY_CARDLIST,
				ParserHanlde);
		if (pageEntity.isSuccess) {
			pageEntity.currentPage = 0;
			pageEntity.total = 1;
		}
		return pageEntity;
	}

	/**
	 * 查看订单的数据，
	 * 
	 * @return
	 */
	public static final ShowOrderEntity parseshowOrder(JSONObject jsonObj) {
		ShowOrderEntity obj = null;
		// final String result = getStringFromInputStream(is);
		if (jsonObj != null) {
			// JSONObject jsonObject;
			// jsonObject = new JSONObject(result);
			obj = ShowOrderEntity.parse(jsonObj);
		}
		return obj;
	}

	/**
	 * 图书资讯数据，
	 * 
	 * @return
	 */
	public static final PageEntity parseBookShortMsgList(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return BookShortMsgEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_CATELIST, ParserHanlde);
	}

	/**
	 * 订单详情数据，
	 * 
	 * @return
	 */
	public static final OrderDetailEntity parseorderDetail(JSONObject jsonObj) {
		OrderDetailEntity obj = null;
		// final String result = getStringFromInputStream(is);
		if (jsonObj != null) {
			// jsonObject = new JSONObject(result);
			obj = OrderDetailEntity.parse(jsonObj);
		}
		return obj;
	}

	/**
	 * 我试读的图书列表数据
	 * 
	 * @return
	 */
	public static final PageEntity parseprobationBookList(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return BookInforEntity.parse(jSONObject);
			}
		};
		return parsePageEntity(jsonObj, KEY_RESULTLIST, ParserHanlde);
	}

	/**
	 * 试读的图书title 列表
	 * 
	 * @return
	 */
	public static final PageEntity parsereadEpub(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return TryReadCatalogEntity.parse(jSONObject);
			}
		};
		PageEntity pageEntity = parsePageEntity(jsonObj, KEY_READLIST,
				ParserHanlde);
		if (pageEntity.isSuccess) {
			pageEntity.currentPage = 0;
			pageEntity.total = 1;
		}
		return pageEntity;
	}

	/**
	 * 试读的图书title 列表
	 * 
	 * @return
	 */
	private static String KEY_CONTENT = "content";

	public static final PageEntity parsereadEpubChapter(JSONObject jsonObject,
			String path) {
		PageEntity pageEntity = null;
		ArrayList<Object> list = null;
		try {
			pageEntity = new PageEntity();
			pageEntity.code = getString(jsonObject, KEY_CODE);
			if (pageEntity.code.equals("0")) {
				// list = new ArrayList<Object>();
				StreamToolBox.saveStringToFile(
						jsonObject.getString(KEY_CONTENT), path);
				// list.add(path);
				pageEntity.obj = path;
				pageEntity.total = 1;
				pageEntity.currentPage = 0;
				pageEntity.list = list;
				pageEntity.isSuccess = true;
			} else {
				pageEntity.isSuccess = false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pageEntity;
	}

	/**
	 * 图书资讯详情页面数据
	 * 
	 * @return
	 */
	private static String KEY_MSGTITLE = "msgTitle";
	private static String KEY_MSGCONTENT = "msgContent";

	public static final PageEntity parseshowBookShortMsg(JSONObject jsonObject) {
		PageEntity pageEntity = null;
		ArrayList<Object> list = null;
		try {
			pageEntity = new PageEntity();
			pageEntity.code = getString(jsonObject, KEY_CODE);
			if (pageEntity.code.equals("0")) {
				list = new ArrayList<Object>();
				Bean bean = new Bean();
				bean.setTitle(jsonObject.getString(KEY_MSGTITLE));
				bean.setContent(jsonObject.getString(KEY_MSGCONTENT));
				list.add(bean);
				pageEntity.total = 1;
				pageEntity.currentPage = 0;
				pageEntity.list = list;
				pageEntity.isSuccess = true;
			} else {
				pageEntity.isSuccess = false;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pageEntity;
	}

	/**
	 * 解析获取列表时的数据，
	 * 
	 * @return
	 */
	public static final PageEntity parseCateEntityList(JSONObject jsonObj) {
		ParserObjectHanlde ParserHanlde = new ParserObjectHanlde() {
			@Override
			public Object praserObject(JSONObject jSONObject) {
				return CateEntity.parse(jSONObject);
			}
		};

		PageEntity pageEntity = parsePageEntity(jsonObj, KEY_CATELIST,
				ParserHanlde);
		if (pageEntity.isSuccess) {
			pageEntity.currentPage = 0;
			pageEntity.total = 1;
		}
		return pageEntity;
	}

	/**
	 * 解析获取列表时的数据，
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final PageEntity parsePageEntity(JSONObject jsonObject,
			String listkey, ParserObjectHanlde ParserHanlde) {
		PageEntity pageEntity = null;
		ArrayList<Object> list = null;
		try {
			pageEntity = new PageEntity();
			pageEntity.code = getString(jsonObject, KEY_CODE);
			if (pageEntity.code.equals("0")) {
				pageEntity.currentPage = getInt(jsonObject, KEY_CURRENT_PAGE);
				int totalCount = getInt(jsonObject, KEY_TOTAL_PAGE);
				// int totalPage = totalCount/PAGE_COUNT;
				pageEntity.total = totalCount;
				pageEntity.resultCount = getInt(jsonObject, KEY_RESULT_COUNT);
//				Log.i("zhoubo",
//						"jsonObject.toString()===" + jsonObject.toString());
//				Log.i("zhoubo", "pageEntity.total===" + pageEntity.total);
				JSONArray jsonList = jsonObject.getJSONArray(listkey);
				list = parseList(jsonList, ParserHanlde);
				pageEntity.list = list;
				pageEntity.isSuccess = true;
			} else {
				pageEntity.isSuccess = false;
				pageEntity.list = new ArrayList<Object>();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pageEntity;
	}

	/**
	 * 解析获取列表时的数据，
	 * 
	 * @return
	 */
	public static final ArrayList parseList(JSONArray jsonList,
			ParserObjectHanlde hanlde) {
		// String result = getStringFromInputStream(is);
		ArrayList list = new ArrayList();
		if (jsonList != null) {
			try {
				// JSONObject jsonObject = new JSONObject(result);
				// JSONArray jsonList = jsonObject.getJSONArray(KEY_BOOKLIST);
				int length = 0;
				if (jsonList != null && (length = jsonList.length()) > 0) {
					JSONObject jsonObj = null;
					Object object;
					for (int index = 0; index < length; index++) {
						jsonObj = jsonList.getJSONObject(index);
						if (jsonObj != null) {
							object = hanlde.praserObject(jsonObj);
							if (object != null) {
								list.add(object);
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	//
	// /**
	// * 解析获取列表时的数据，
	// *
	// * @return
	// */
	// public static final ArrayList<CateEntity> parseCateEntityList(InputStream
	// is) {
	// ArrayList<CateEntity> list = null;
	// final String result = getStringFromInputStream(is);
	// if(!TextUtils.isEmpty(result)){
	// JSONObject jsonObject;
	// try {
	// jsonObject = new JSONObject(result);
	//
	// JSONArray jsonList = jsonObject.getJSONArray(KEY_BOOKLIST);
	// ParserObjectHanlde ParserHanlde =new ParserObjectHanlde(){
	// @Override
	// public Object praserObject(JSONObject jSONObject) {
	// return CateEntity.parse(jSONObject);
	// }
	// };
	// list= (ArrayList<CateEntity>)parseList(jsonList,ParserHanlde);
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// return list;
	// }

	// /**
	// * 解析获取列表时的数据，
	// *
	// * @return
	// */
	// public static final ArrayList<BookInfor> parseCateEntityList(InputStream
	// is) {
	// ParserHanlde ParserHanlde =new ParserHanlde(){
	// @Override
	// public Object praserObject(JSONObject jSONObject) {
	// return parseOneCateEntity(jSONObject);
	// }
	// };
	// return parseList(is,ParserHanlde);
	// }
	//
	// private static final CateEntity parseOneCateEntity(JSONObject jSONObject)
	// {
	// CateEntity entity = null;
	// if (jSONObject != null) {
	// try {
	// entity = new CateEntity();
	// entity.catId = getString(jSONObject, CateEntity.KEY_CAT_ID);
	// entity.catName = getString(jSONObject, CateEntity.KEY_CATNAME);
	// entity.isLeaf = getBoolean(jSONObject, CateEntity.KEY_IS_LEAF);
	// entity.logoUrl = getString(jSONObject, CateEntity.KEY_LOGO_URL);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// return entity;
	// }

	public static String getString(JSONObject data, String key) {
//		Log.i("zhoubo", "data====" + data);
//		Log.i("zhoubo", "key====" + key);
		if (!data.isNull(key)) {
			try {
				String str = data.getString(key);
				if (str.equals("null")) {
					str = "";
				}
				return str;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public static JSONObject getJSONObject(JSONObject data, String key) {
		if (!data.isNull(key)) {
			try {
				return data.getJSONObject(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return new JSONObject();
	}

	public static JSONArray getJSONArray(JSONObject data, String key) {
		if (!data.isNull(key)) {
			try {
				return data.getJSONArray(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return new JSONArray();
	}

	public static String getStringFromArray(JSONObject data, String key) {
		if (!data.isNull(key)) {
			try {
				return data.getJSONArray(key).getString(0);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public static int getInt(JSONObject data, String key) {
		if (!data.isNull(key)) {
			try {
				return data.getInt(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	public static double getDouble(JSONObject data, String key) {
		if (!data.isNull(key)) {
			try {
				return data.getDouble(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	public static boolean getBoolean(JSONObject data, String key) {
		if (!data.isNull(key)) {
			try {
				return data.getBoolean(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static long getLong(JSONObject data, String key) {
		if (!data.isNull(key)) {
			try {
				return data.getLong(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	interface ParserObjectHanlde {
		public Object praserObject(JSONObject jSONObject);

	}

}
