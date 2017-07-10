package com.jingdong.app.reader.activity;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.BookInforEDetail;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OlineCard;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.extra.ChangduCard;
import com.jingdong.app.reader.entity.extra.OpenTaskDownloadEntity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OnlineReadManager;
import com.jingdong.app.reader.opentask.InterfaceBroadcastReceiver;
import com.jingdong.app.reader.opentask.InterfaceBroadcastReceiver.Command;
import com.jingdong.app.reader.util.DataIntent;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.loopj.android.http.RequestParams;

/**
 * 执行额外任务<br />
 * 1、根据传递过来的参数进行额外任务运行<br />
 * 2、请求畅读相关信息
 *
 */
public class ExtraTaskOnLoginSuccess {

	private Context mcContext = null;
	final static public String KEY_CARDS = "cards";
	private Intent itIntent = null;
	private boolean isNeedToDoOtherTask = false;

	public ExtraTaskOnLoginSuccess(Context context, Intent it,
			boolean isNeedToDoOtherTask) {
		this.mcContext = context;
		this.itIntent = it;
		this.isNeedToDoOtherTask = isNeedToDoOtherTask;
	}

	public void execute() {

		if (isNeedToDoOtherTask)
			handleIntent(itIntent);
		// 处理畅读卡信息
		processChangDuTask();

	}

	public void processChangDuTask() {

		ArrayList<OlineCard> arrayList = OlineCard.readOlineCardsFromDb();
		JSONObject jsonObject = cardList2Josn(arrayList);

		WebRequestHelper.get(URLText.JD_BOOK_CHANGDU_URL,
				RequestParamsPool.getCanUseOlineCardsHttpSetting(jsonObject),
				true, new MyAsyncHttpResponseHandler(mcContext) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						MZLog.d("wangguodong", "畅读卡数据请求失败!");
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,byte[] responseBody) {
						MZLog.d("wangguodong", "畅读卡数据请求成功!");

						try {
							JSONObject jsonObj = new JSONObject(new String(responseBody));
							JSONArray jsonArray = null;

							jsonArray = (JSONArray) jsonObj.getJSONArray("cardList");
							int length = jsonArray.length();
							for (int index = 0; index < length; index++) {

								MZLog.d("wangguodong", "批量更新本地畅读卡状态");
								jsonObj = (JSONObject) jsonArray.get(index);
								OlineCard olineCard = OlineCard.parserOLineCard(jsonObj);
								if (!TextUtils.isEmpty(olineCard.cardNum)) {
									olineCard.save();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

	}

	public static JSONObject cardList2Josn(ArrayList<OlineCard> arrayList) {
		JSONObject cardBuildObj = new JSONObject();
		try {
			long orderid = 0;
			cardBuildObj.put("orderId", orderid);
			JSONArray list = new JSONArray();
			for (OlineCard olineCard : arrayList) {
				JSONObject bookMarkObject = new JSONObject();
				bookMarkObject.put("card", olineCard.cardNum);
				list.put(bookMarkObject);
			}
			cardBuildObj.put(KEY_CARDS, list);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return cardBuildObj;
	}

	public void handleIntent(Intent intent) {
		if (intent == null)
			return;
		String key = intent.getStringExtra("key");
		if (TextUtils.isEmpty(key)) {
			return;
		}
		Object obj = DataIntent.get(key);
		if (obj != null && obj instanceof Command) {
			final Command command = (Command) obj;
			MZLog.d("wangguodong", "开始执行外部任务...");
			toTargetActivity(command);
		}
	}

	public void toTargetActivity(Command command) {

		int moduleId = command.getModuleId();
		Bundle bundle = command.getOutBundle();

		switch (moduleId) {

		case InterfaceBroadcastReceiver.MODULE_ID_DOWNLOAD_BUYED:

			BookInforEDetail bookE = new BookInforEDetail();
			bookE.bookid = bundle.getLong("bookId");
			bookE.author = bundle.getString("author");

			if (bookE.author != null) {
				if (TextUtils.isEmpty(bookE.author.trim())) {
					bookE.author = "";
				}
			} else {
				bookE.author = "";
			}
			bookE.picUrl = bundle.getString("picUrl");
			bookE.bookType = bundle.getInt("bookType");
			String formatName = bundle.getString("format");
			bookE.formatName = formatName;

			bookE.largeSizeImgUrl = bundle.getString("bigPicUrl");
			bookE.bookName = bundle.getString("bookName");
			bookE.size = bundle.getString("bookSize");
			OrderEntity order = null;
			if (bundle.getBoolean("isFreeBook", false)) {
				order = DownloadTool.creatOrderByBook(bookE);

			} else {
				order = DownloadTool.creatOrderByBook(bookE,
						bundle.getLong("orderId"));

				MZLog.d("wangguodong", bundle.getLong("orderId") + "");
			}
			if (order != null) {
				DownloadTool.downBook((Activity) mcContext, order, null, true,
						LocalBook.SOURCE_BUYED_BOOK, 0, null,true);
				MZLog.d("MainActivity", "开始下载外部任务书籍");
				Toast.makeText(mcContext, "开始下载已购书籍", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(mcContext, "下载参数有错误", Toast.LENGTH_LONG).show();
			}

			break;
		case InterfaceBroadcastReceiver.MODULE_ID_DOWNLOAD_CHANGDU:

			OpenTaskDownloadEntity book1 = new OpenTaskDownloadEntity();

			book1.bookId = bundle.getLong("bookId");
			book1.orderId = book1.bookId;
			book1.author = bundle.getString("author");
			book1.picUrl = bundle.getString("picUrl");
			book1.bookType = bundle.getInt("bookType");
			String formatName1 = bundle.getString("format");

			if (!TextUtils.isEmpty(formatName1)) {
				if (formatName1.equals(LocalBook.FORMATNAME_PDF)) {
					book1.format = LocalBook.FORMAT_PDF;
				}
				if (formatName1.equals(LocalBook.FORMATNAME_EPUB)) {
					book1.format = LocalBook.FORMAT_EPUB;
				}
			}

			book1.bigPicUrl = bundle.getString("bigPicUrl");
			book1.bookName = bundle.getString("bookName");

			if (book1 == null)
				return;
			MZLog.d("wangguodong", "下载畅读...");
			Toast.makeText(mcContext, "开始下载畅读书籍", Toast.LENGTH_LONG).show();
			OrderEntity orderEntity1 = OrderEntity
					.FromOpenTaskDownloadEntity2OrderEntity(book1);
			DownloadTool.download((Activity) mcContext, orderEntity1, null,
					false, LocalBook.SOURCE_ONLINE_BOOK, 0, true, null,true);

			break;

		}

	}
}
