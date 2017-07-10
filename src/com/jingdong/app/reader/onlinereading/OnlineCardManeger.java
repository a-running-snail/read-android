package com.jingdong.app.reader.onlinereading;

import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.database.ContentObserver;
import android.os.Handler;
import android.text.TextUtils;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.config.ITransKey;
import com.jingdong.app.reader.entity.OlineCard;


public class OnlineCardManeger implements ITransKey {
	public static String KEY = "yc2JffcREheFQlYFIAY5f9sY7uflgBTo";
	public final static String TESt_KEY = "1hB6xFDiOzJLrU5ShSl4qgbfMGc0WOUY";
	public final static int  KEY_ADD_NEW_CARD_BATCH=1;//批量加入畅读书。
	final static public String KEY_CARDS = "cards";
	private static Hashtable<String,OlineCard>  hashTable= null;
	private static String PREFIX="10.";
	static{
		boolean  test = Configuration.getBooleanProperty(Configuration.TEST_MODE,
					false);
		String hostString=Configuration.getProperty(Configuration.DBOOK_HOST);
		if(test && hostString.startsWith(PREFIX)){
			KEY	=  TESt_KEY;
		}
	}
/*
	
	public static void doSynOlineCards() {
		try {
			FileGuider savePath = new FileGuider(
					FileGuider.SPACE_PRIORITY_EXTERNAL);
			savePath.setChildDirName(Constant.DIR_TEMP_NAME);
			savePath.setFileName(Constant.FILE_TEMP_NAME_BOOKSYN);
			ArrayList<OlineCard> arrayList = OlineCard.readOlineCardsFromDb();
			JSONObject jsonObject = cardList2Josn(arrayList);
			HttpSetting httpSetting = ServiceProtocol.getCanUseOlineCardsHttpSetting(jsonObject);
			httpSetting.setNotifyUser(false);
			httpSetting.setPost(true);
			httpSetting.setEffect(HttpSetting.EFFECT_NO);
			httpSetting.setListener(new HttpGroup.OnAllListener() {
				@Override
				public void onProgress(long max, long progress) {
				}

				@Override
				public void onError(HttpError error) {
//					Log.i(error.toString());
//					if (synRunnable != null) {
//						synRunnable.synComplete(false,"");
//					}
				}

				@Override
				public void onEnd(HttpResponse httpResponse) {
					// TODO Auto-generated method stub
					JSONObject jsonObj = httpResponse.getJSONObject();
					JSONArray jsonArray = null;
					try {
						jsonArray = (JSONArray)jsonObj.getJSONArray("cardList");
					int length = jsonArray.length();
					for(int index = 0; index < length; index++){
//						OlineCard  olineCard= new OlineCard();
//						olineCard.cardNum = DataParser.getString(jsonObj, "cardNO");
////						olineCard.end = DataParser.getLong(jsonObj, "cardEndTime");
//						String strEnd = DataParser.getString(jsonObj, "cardEndTime");
//						if(!TextUtils.isEmpty(strEnd)){
//						olineCard.end = Long.valueOf(DesUtil.decrypt(strEnd,OlineCard.KEY));
//						}
//						olineCard.start = DataParser.getLong(jsonObj, "cardStartTime");
					   jsonObj = (JSONObject)jsonArray.get(index);
						OlineCard  olineCard = OlineCard.parserOLineCard(jsonObj);
						if(!TextUtils.isEmpty(olineCard.cardNum)){
//							olineCard.availab = 1;
							olineCard.save();
							}
						OnlineReadManager.freshLocalBookOLineState(olineCard);
					}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					String code = DataParser.getString(jsonObj, "code");
//								OlineCard  olineCard= new OlineCard();
//								olineCard.cardNum = DataParser.getString(jsonObj, "cardNO");
//								olineCard.end = DataParser.getLong(jsonObj, "cardEndTime");
//								olineCard.start = DataParser.getLong(jsonObj, "cardStartTime");
//								olineCard.save();
								
				
						}

				@Override
				public void onStart() {
					// TODO Auto-generated method stub
				}
			});
			HttpGroup httpGroup =  HttpGroup.getHttpGroup(null);
			httpGroup.add(httpSetting);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Log.e("xiawei", "doSynToLocal>>" + e.getMessage());
		}
	}
	
	*/
	

	public static JSONObject cardList2Josn(ArrayList<OlineCard> arrayList){
		JSONObject cardBuildObj = new JSONObject();
		try {
			long orderid = 0;
			cardBuildObj.put("orderId", orderid);
			JSONArray list = new JSONArray();
//			ArrayList bookmarkList = new ArrayList(){};
			for (OlineCard olineCard: arrayList) {
				JSONObject bookMarkObject = new JSONObject();
				bookMarkObject.put("card", olineCard.cardNum);	
				list.put(bookMarkObject);
			}
			cardBuildObj.put(KEY_CARDS, list);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cardBuildObj;
	}
	
	private static boolean isInited = false;
	private static boolean reReadDB = true;	
	
	public static boolean judgeShowValid(String cardNum,Activity activity){
		if(TextUtils.isEmpty(cardNum)){
			return true;
		}
		
         return  judgeValid( cardNum,activity);
	} 
	
	
	public static boolean judgeValid(String cardNum,Activity activity){
		if(TextUtils.isEmpty(cardNum)){
			return false;
		}
		if(!isInited){
			isInited = true;
			 hashTable = new Hashtable<String,OlineCard>();
			 activity.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					ContentObserver mContentObserver = new ContentObserver(new Handler()) {
				         @Override
				         public boolean deliverSelfNotifications() {
				              return super.deliverSelfNotifications();
				         }

				         @Override
				         public void onChange(boolean selfChange) {
				        		reReadDB = true;
				         }
				    };
					MZBookApplication.getContext().getContentResolver().
					registerContentObserver(OlineCard.uri, true, mContentObserver);					
				} 
			 });
		}
		if(reReadDB){
			reReadDB = false;
		ArrayList<OlineCard> arrayList = OlineCard.readOlineCardsFromDb();
		hashTable.clear();
		for(OlineCard olineCard:arrayList){
			hashTable.put(olineCard.cardNum, olineCard);	
		}
		}
		OlineCard olineCard = hashTable.get(cardNum);
		if(olineCard==null){
			return false;
		}
		long time = System.currentTimeMillis();
		if(olineCard.availab==1&&time<olineCard.end){
			return true;
		}else{
			if(olineCard.availab==1&&time>olineCard.end){
				olineCard.availab = 0;
				olineCard.save();
			}
			return false;
		}
	} 
	
	/**
	 * 批量绑定畅读卡，
	 *
	public static void requestServer2BindCardBatch(final ArrayList<LocalBook> books,final MyActivity activity,final Runnable runnable){
		HttpSetting httpSetting = ServiceProtocol
				.getBindCardBatchHttpSetting(books);
		httpSetting.setNotifyUser(true);
		httpSetting.setListener(new OnAllListener() {
			@Override
			public void onProgress(long max, long progress) {
			}

			@Override
			public void onError(HttpError error) {
 				  ShowTools.toastInThread("网络连接失败！请检查网络。");
			}
			@Override
			public void onEnd(HttpResponse httpResponse) { 
				final JSONObject jsonObj = httpResponse.getJSONObject();
				Log.i("zhang",
						"goToReadOnline json------------->"
								+ jsonObj.toString());
				String code = DataParser.getString(jsonObj, "code");
				if(code.equals("0")){
					  try {
						  ArrayList<LocalBook>  mSuccessBooks=new ArrayList<LocalBook>();//绑定成功的图书。
						  ArrayList<LocalBook>  mFailBooks=new ArrayList<LocalBook>();//绑定失败的图书。
						JSONArray ebooks=jsonObj.getJSONArray("ebookList");
						String cardNum=jsonObj.getString("cardNO");
						if(ebooks!=null && ebooks.length()>0){
							 for(int i=0;i<ebooks.length();i++){
								JSONObject  mJsonObject=ebooks.getJSONObject(i);
								String ebookId=mJsonObject.getString("ebookId");
								String status=mJsonObject.getString("status");//1是失败，11是成功。
								//判断对应id是否成功。
								for(LocalBook book:books){
									if(book.id==Long.valueOf(ebookId)){
										if(status.equals("11")){
											mSuccessBooks.add(book);
											if(!TextUtils.isEmpty(cardNum)){
												book.isOnline=true;
												book.isShowOlineRead=true;
												LocalBook.saveCardInBook(book.id,
														cardNum);
											}
										}else{
											mFailBooks.add(book);
										}
									}
								}
							 }
							 ShowTools.toastInThread("共有"+mSuccessBooks.size()+"本书成功加入到畅读卡！");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}finally{
						activity.post(runnable);
					}
				}else if(code.equals("3")){
					// 未登录
					ShowTools.toastInThread("请登录后重试");
					activity.post(new Runnable() {
						@Override
						public void run() {
							Runnable sucessRunnable = new Runnable() {
								@Override
								public void run() {
									requestServer2BindCardBatch(books, activity,runnable);
								}
							};
							Runnable failedRunnable = new Runnable() {
								@Override
								public void run() {
								}
							};
							activity.startLoginActivity(sucessRunnable,
									failedRunnable);
						}
					});
				}
			}
			@Override
			public void onStart() {
			}
		});
		HttpGroup httpGroup = activity.getHttpGroupaAsynPool();
		httpGroup.add(httpSetting);
	}
*/}
