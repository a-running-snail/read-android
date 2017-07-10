package com.jingdong.app.reader.community;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.bookstore.search.BookStoreSearchActivity;
import com.jingdong.app.reader.bookstore.search.adapter.SearchPageAdapter;
import com.jingdong.app.reader.entity.extra.SearchKeyWord;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.net.url.PagedBasedUrlGetter;
import com.jingdong.app.reader.net.url.QueryUrlGetter;
import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.timeline.fragment.UserListFragment;
import com.jingdong.app.reader.timeline.model.UserModel.Note;
import com.jingdong.app.reader.ui.ViewPagerTabBarHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.Base64;
import com.jingdong.app.reader.util.Contants;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.CustomProgreeDialog;
import com.jingdong.app.reader.view.SearchTopBarView;
import com.jingdong.app.reader.view.SearchTopBarView.TopBarViewListener;

@SuppressWarnings("deprecation")
public class CommunitySearchActivity extends FragmentActivity implements TopBarViewListener {
	
	private final String COMMUNITY_SEARCH_HISTORY = "community_history";
	private static int DYNAMIC_PER_PAGE = 5;//动态搜索一页数据
	private int currentPage = 1;//当前页码
	private Context mContext;
	private ListView historyListview;
	private ListView resultListview;
	private ArrayList<String> historyList = new ArrayList<String>();// 搜索历史记录
	private ArrayList<DynamicResult> dynamicList = new ArrayList<DynamicResult>();// 动态搜索结果title
//	private List<UserInfo> usersList = new ArrayList<UserInfo>();// 搜索结果title
	private SearchHistoryAdapter searchHistoryAdapter;
	private SearchResultAdapter searchResultAdapter;
	private LinearLayout searchResultLinearLayout;
	SearchResultFragment searchResultFragment;
	private LinearLayout hotwordGroup1,hotwordGroup2;
	private FrameLayout frameLayout;
	private View footerView;
	private LinearLayout searchHistoryLinearLayout;
	private static String HOTWORD_TYPE="communityHotword";
	
	

	private SearchTopBarView topBarView = null;
	public static EditText edittext_serach;// 搜索关键字输入框
	private int selected = 0;
	public static boolean isHistoryKey = false;
	public static boolean isHistoryListShow = false;
	public static String TYPE_USER="user";
	public static String TYPE_TIMELINE="timeline";


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community_search);
		mContext=this;
		initView();
		topBarView = (SearchTopBarView) findViewById(R.id.topbar);
		initTopbarView();
		
		edittext_serach = (EditText) findViewById(R.id.edittext_serach);
		edittext_serach.setCursorVisible(true);
		edittext_serach.setFocusable(true);
		edittext_serach.requestFocus();
		edittext_serach.setHint("搜索用户昵称、动态");
		edittext_serach.setOnKeyListener(new View.OnKeyListener() {

			@SuppressWarnings("static-access")
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == event.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					closeKeyBroad();
					String query=edittext_serach.getText().toString();
					startSearch(query);
					
					
//					if (selected == 0) {
//						if (userlistener != null) {
//							userlistener.onSearchUser(edittext_serach.getText()
//									.toString(), true);
//						}
//					} else {
//						if (listener != null) {
//							listener.onSearchTimeline(edittext_serach.getText()
//									.toString(), true);
//						}
//					}
					return true;
				}
				return false;
			}
		});

		edittext_serach.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String content=edittext_serach.getText().toString();
				if (!TextUtils.isEmpty(content)) {
					WebRequestHelper.cancleRequest(mContext);
					dynamicList.clear();
					searchUsers(mContext, content,Contants.START_PAGE, DYNAMIC_PER_PAGE,true);
				}else{
					WebRequestHelper.cancleRequest(mContext);
					searchResultLinearLayout.setVisibility(View.GONE);
				}
			}
		});
	}

	private void initView() {
		searchHistoryLinearLayout= (LinearLayout) findViewById(R.id.search_history_ll);
		historyListview=(ListView) findViewById(R.id.history_listview);
		historyList=readSearchHistory();
		searchHistoryAdapter=new SearchHistoryAdapter(mContext, historyList);
		historyListview.setAdapter(searchHistoryAdapter);
		historyListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				edittext_serach.setText(historyList.get(position));
				edittext_serach.setSelection(edittext_serach.getText().length());
				startSearch(historyList.get(position));
			}
		});
		if(historyList==null || historyList.size()==0){
			searchHistoryLinearLayout.setVisibility(View.GONE);
		}
		
		footerView  = LayoutInflater.from(mContext).inflate(R.layout.community_search_history_footer, null);
		if(historyList.size()>0)
			historyListview.addFooterView(footerView);
		footerView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				clearHistory();
			}
		});
		
		
		searchResultLinearLayout=(LinearLayout) findViewById(R.id.search_result_ll);
		resultListview=(ListView) findViewById(R.id.search_result_listview);
		searchResultAdapter=new SearchResultAdapter(mContext, dynamicList);
		resultListview.setAdapter(searchResultAdapter);
		resultListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//				edittext_serach.setText(dynamicList.get(position));
//				edittext_serach.setSelection(edittext_serach.getText().length());
//				startSearch(dynamicList.get(position));
				
				DynamicResult result= dynamicList.get(position);
				if(result.type.equals(TYPE_USER)){
//					edittext_serach.setText(result.userName);
//					edittext_serach.setSelection(edittext_serach.getText().length());
					Intent intent = new Intent();
					intent.setClass(mContext, UserActivity.class);
					intent.putExtra("user_id", result.id);
					intent.putExtra(UserActivity.USER_NAME, result.userName);
					mContext.startActivity(intent);
					
				}else{
//					edittext_serach.setText(result.content);
//					edittext_serach.setSelection(edittext_serach.getText().length());
					Intent intent = new Intent();
					intent.putExtra(TimelineTweetActivity.TWEET_GUID, result.id);
					intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, result.userName);
					intent.setClass(mContext, TimelineTweetActivity.class);
					mContext.startActivity(intent);
				}
				
			}
		});
		
		hotwordGroup1=(LinearLayout) findViewById(R.id.hotword_group1);
		hotwordGroup2=(LinearLayout) findViewById(R.id.hotword_group2);
		getHotKeyWords();
		
		frameLayout=(FrameLayout) findViewById(R.id.container);
		
		
	}

	private void initTopbarView() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setRightMenuVisiable(false);
		topBarView.setListener(this);
		topBarView.updateTopBarView(true);
	}
	
	/**
	 * 执行查询操作
	 */
	private void startSearch(String query){
		if(query!=null && !((query=query.trim()).equals(""))){
			searchWordReport(query);
			storeHistory(query, false);//保存搜索历史
//			currentPage=Contants.START_PAGE;
//			searchUsers(mContext, query,currentPage, Contants.PER_PAGE_NUMBER,false);
			
			WebRequestHelper.cancleRequest(mContext);
			closeKeyBroad();
			
			searchResultLinearLayout.setVisibility(View.GONE);
			frameLayout.setVisibility(View.VISIBLE);
			searchResultFragment = new SearchResultFragment();
			Bundle bundle=new Bundle();
			bundle.putString("query", query);
			searchResultFragment.setArguments(bundle);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, searchResultFragment).commit();
		}
	}
	
	/**
	* @Description: 搜索关键字日志记录信息
	* @param String keyWord 搜索关键字
	* @author xuhongwei1
	* @date 2015年11月19日 下午3:31:40 
	* @throws 
	*/ 
	private void searchWordReport(String keyWord) {
		WebRequestHelper.get(URLText.JD_USER_SEARCH_URL, RequestParamsPool.getSearchWordReportParams(keyWord, "2"),
				true, new MyAsyncHttpResponseHandler(CommunitySearchActivity.this) {
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						
			}
					
			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
//				String result = new String(responseBody);
//		System.out.println("BBBBB==8888==searchWordReport===="+result);
			}
		});
	}
	
	/**
	 * 读取搜索历史，在加载页面的第一次读取
	 **/
	private ArrayList<String> readSearchHistory() {
		
		String history = LocalUserSetting.getStringValueByKey(mContext,getCommunityHistoryKey());
		String[] historyArray = history.split(" ");
		for (int i = 0; i < historyArray.length; i++) {
			try {
				String string = new String(Base64.decode(historyArray[i]));
				if (!TextUtils.isEmpty(string)) {
					historyList.add(string);
				}
			} catch (IOException e) {
			}
		}
		return historyList;
	};
	
	
	private String getCommunityHistoryKey(){
		if(!LoginUser.isLogin())
			return "noUserLogin"+"_"+ COMMUNITY_SEARCH_HISTORY;
		return LoginUser.getpin()+"_"+ COMMUNITY_SEARCH_HISTORY;
	}

	/******
	 * 保存历史搜索，按照先进先出的原则保存10条
	 * 
	 * @param keyWord可以为null
	 *            ，为null是强制保存
	 * ********/
	public void storeHistory(String keyWord, boolean isRemoveKW) {
		historyList.trimToSize();
		if (isRemoveKW) {
			historyList.remove(keyWord);
		} else {
			if (historyList.contains(keyWord)) {
				String tmpKeyWordString = keyWord;
				historyList.remove(keyWord);
				historyList.add(0, tmpKeyWordString);
			} else if (historyList.size() < 10) {
				historyList.add(0, keyWord);
			} else {
				historyList.remove(historyList.size() - 1);
				historyList.add(0, keyWord);
			}
		}

		String historyString = "";
		for (int i = 0; i < historyList.size(); i++) {
			historyString = historyString + " " + Base64.encodeBytes(historyList.get(i).getBytes());
		}
		
		LocalUserSetting.saveStringValueByKey(mContext, getCommunityHistoryKey(), historyString);
		if (historyList.isEmpty()) {
			historyListview.setVisibility(View.GONE);
			searchHistoryLinearLayout.setVisibility(View.GONE);
		}else{
			historyListview.setVisibility(View.VISIBLE);
			searchHistoryLinearLayout.setVisibility(View.VISIBLE);
		}
	}
	

	/**
	 * 清除历史记录
	 */
	public void clearHistory() {
		historyList.clear();
		searchHistoryAdapter.notifyDataSetChanged();
		LocalUserSetting.saveStringValueByKey(mContext, getCommunityHistoryKey(), "");
		historyListview.setVisibility(View.GONE);
		searchHistoryLinearLayout.setVisibility(View.GONE);
	}

	/**
	 *  获取搜索热词
	 */
	private void getHotKeyWords() {
		WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool.getHotKeywordParams("6",HOTWORD_TYPE),
				new MyAsyncHttpResponseHandler(mContext) {

					@Override
					public void onStart() {
						super.onStart();
					}
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String result = new String(responseBody);
						try {
							JSONObject jsonObj = new JSONObject(result);
							TextView textView;
							if (jsonObj != null) {
								String code = jsonObj.optString("code");
								if (code.equals("0")) {
									final JSONArray array = jsonObj.getJSONArray("keywords");
									int count = array.length();
									for (int i = 0; i < 6; i++) {
										if (i < 3) {
											textView = (TextView) hotwordGroup1.getChildAt(i);
											if(count > i)
												textView.setText(array.getString(i));
											else
												textView.setVisibility(View.GONE);
										} else{
											textView = (TextView) hotwordGroup2.getChildAt(i-3);
											if(count > i)
												textView.setText(array.getString(i));
											else
												textView.setVisibility(View.GONE);
										}
										final int index=i;
										textView.setOnClickListener(new OnClickListener() {
											
											@Override
											public void onClick(View arg0) {
												try {
													edittext_serach.setText(array.getString(index));
													edittext_serach.setSelection(edittext_serach.getText().length());
													startSearch(array.getString(index));
												} catch (JSONException e) {
													e.printStackTrace();
												}
											}
										});
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onLeftMenuClick() {
		finish();
	}

	@Override
	public void onRightMenu_leftClick() {
	}

	@Override
	public void onRightMenu_rightClick() {

	}

	@Override
	public void onCenterMenuItemClick(int position) {

	}

	public void setEditText(String str) {
		edittext_serach.setText(str);
		// 切换后将EditText光标置于末尾
		edittext_serach.postInvalidate();
		CharSequence charSequence = edittext_serach.getText();
		if (charSequence instanceof Spannable) {
			Spannable spanText = (Spannable) charSequence;
			Selection.setSelection(spanText, charSequence.length());
		}
	}

	public static EditText getEdittext_serach() {
		return edittext_serach;
	}

	public static void setEdittext_serach(EditText edittext_serach) {
		CommunitySearchActivity.edittext_serach = edittext_serach;
	}

	private void closeKeyBroad(){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();
		if (isOpen) {
			imm.hideSoftInputFromWindow(edittext_serach.getWindowToken(), 0); // 强制隐藏键盘
		}
	}
	
	/**
	 * 动态搜索用户
	 * @param context
	 * @param query
	 * @param currentpage
	 * @param pagecount
	 * @param isDynamicSearch
	 */
	public void searchUsers(final Context context, final String query,final int currentpage,final int pagecount,final boolean isDynamicSearch) {
				WebRequestHelper.getWithContext(context,URLText.searchPeople,
						RequestParamsPool
								.searchPeopleParams(currentpage,pagecount, query), true,
						new MyAsyncHttpResponseHandler(mContext) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String jsonString = new String(responseBody);
								getUserList(jsonString,isDynamicSearch);
								searchTimeline(mContext, query, Contants.START_PAGE, DYNAMIC_PER_PAGE, true);
							}
						});
	}
	/**
	 * 解析json字符串，并把解析结果填充到一个List中
	 * 
	 * @param json
	 *            json字符串
	 * @return User的列表
	 */
	private void getUserList(String json,boolean isDynamicSearch) {
		try {
			JSONArray userArray = new JSONArray(json);
			DynamicResult result;
			if (userArray != null) {
				JSONObject jsonObject;
				if(isDynamicSearch)
					dynamicList.clear();
//				else
//					usersList = new ArrayList<UserInfo>(userArray.length());
				UserInfo user;
				for (int i = 0; i < userArray.length(); i++) {
					jsonObject = userArray.getJSONObject(i);
					if (!jsonObject.getString(UserInfo.ID).equals(
							LoginUser.getpin())) {
						user = new UserInfo();
						user.parseJson(jsonObject);
						
						result=new DynamicResult();
						result.type=TYPE_USER;
						result.id=user.getId();
						result.userName=user.getName();
						if(isDynamicSearch)
							dynamicList.add(result);
//						else
//							usersList.add(user);
					}
				}
			} 
		} catch (JSONException e) {
		}
	}
	
	
	public void searchTimeline(final Context context, final String query,final int  currentpage, final int pagecount,final boolean isDynamicSearch) {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				WebRequestHelper.get(URLText.TimeLine_Search_URL,
						RequestParamsPool
								.getTimelineSearchParams(currentpage+"",pagecount+"",query), true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String jsonString = new String(responseBody);
								getTimelineList(jsonString,isDynamicSearch);
								if (isDynamicSearch) {
									searchResultLinearLayout.setVisibility(View.VISIBLE);
									searchResultAdapter.notifyDataSetChanged();
								}else{
									searchResultLinearLayout.setVisibility(View.GONE);
								}
							}
						});
			}
		});
	}
	
	private void getTimelineList(String json,boolean isDynamicSearch) {
		try {
			JSONObject jsonObject = new JSONObject(json);
			if(jsonObject!=null){
				JSONArray array = jsonObject.getJSONArray("entities");
				DynamicResult result;
				if (array != null) {
					JSONObject jObject,tempObject,renderBody;
					UserInfo user;
					for (int i = 0; i < array.length(); i++) {
						jsonObject = array.getJSONObject(i);
						if(jsonObject.optInt("deleted")==0)
						{
							result=new DynamicResult();
							result.type=TYPE_TIMELINE;
							String guid=jsonObject.optString("guid");
							renderBody=jsonObject.getJSONObject("render_body");
							if (UiStaticMethod.isNullString(guid)) {
								if(renderBody!=null){
									tempObject=renderBody.getJSONObject("entity");
									if(tempObject!=null)
										guid=tempObject.optString("guid");
								}
							}
							result.id=guid;
							
							tempObject=jsonObject.getJSONObject("user");
							if(tempObject!=null)
								result.userName=tempObject.optString("name");
							
							String head="";
							String renderType=jsonObject.optString("render_type");
							if (TextUtils.isEmpty(renderType)) {
								head = "";
							} else if (renderType.equals("UserTweet")) {
								head = "随便说说 | ";
							} else if (renderType.equals("BookComment")) {
								int rating=renderBody.optInt("rating");
								if(rating>0)
									head = "书评 | ";
								else
									head = "书籍 | ";
							} else if (renderType.equals("Note")) {
								String content= renderBody.optString("content");
								if (UiStaticMethod.isNullString(content)) {
									head = "划线 | ";
								}else
									head = "笔记 | ";
							} else if (renderType.equals("EntityComment")) {
								head = "评论 | ";
							}

							String content=renderBody.optString("content");
							if (UiStaticMethod.isNullString(content)) 
								content=renderBody.optString("quote_text");
							content=head+content;
							result.content = content;
							dynamicList.add(result);
						}
						
					}
				} 
			}
		} catch (JSONException e) {
		}
	}
	
	
	/**
	 * 搜索历史adapter
	 *
	 */
	public class SearchHistoryAdapter extends BaseAdapter{

		private List<String> list = null ;
		private Context context= null ; 
		
		class ViewHolder {
			private LinearLayout deleteLinearLayout;
			private TextView content;
		}
		
		public SearchHistoryAdapter(Context context, List<String> list){
			this.context = context;
			this.list = list;
		}
		
		@Override
		public int getCount() {
			return list==null?0:list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if(convertView==null){ 
				viewHolder = new ViewHolder(); 
				convertView = LayoutInflater.from(context).inflate(R.layout.community_search_historylist_item, null); 
				viewHolder.content= (TextView) convertView.findViewById(R.id.content);
				viewHolder.deleteLinearLayout = (LinearLayout) convertView.findViewById(R.id.delete_linearLayout);
				convertView.setTag(viewHolder); 
			}else{		
				viewHolder=(ViewHolder) convertView.getTag();
			}
			
			viewHolder.content.setText(list.get(position));
			viewHolder.deleteLinearLayout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					String keyword = list.get(position);
					list.remove(position);
					notifyDataSetChanged();
					storeHistory(keyword, true);
				}
			});
			
			return convertView;
		}
	}
	

	/**
	 * 搜索结果adapter
	 *
	 */
	public class SearchResultAdapter extends BaseAdapter{

		private List<DynamicResult> list = null ;
		private Context context= null ; 
		
		class ViewHolder {
			private TextView content;
		}
		
		public SearchResultAdapter(Context context, List<DynamicResult> list){
			this.context = context;
			this.list = list;
		}
		
		@Override
		public int getCount() {
			return list==null?0:list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if(convertView==null){ 
				viewHolder = new ViewHolder(); 
				convertView = LayoutInflater.from(context).inflate(R.layout.community_search_resultlist_item, null); 
				viewHolder.content= (TextView) convertView.findViewById(R.id.content);
				convertView.setTag(viewHolder); 
			}else{		
				viewHolder=(ViewHolder) convertView.getTag();
			}
			
			DynamicResult result= list.get(position);
			
			if(result.type.equals(TYPE_USER))
				viewHolder.content.setText(result.userName);
			else
				viewHolder.content.setText(result.content);
			return convertView;
		}
	}
	
	
	private class DynamicResult{
		public String id;
		public String userName;
		public String content;
		public String type;
	}
	
	
	
	
	
	
}
