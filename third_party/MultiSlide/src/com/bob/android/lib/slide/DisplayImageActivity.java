//package com.bob.android.lib.slide;
//
//import java.io.InputStream;
//
//
//
//
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.jingdong.app.lib.slide.R;
//
//import android.app.Activity;
//import android.content.res.Configuration;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.Display;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.view.View.OnClickListener;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.TextView;
//
//public final class DisplayImageActivity extends Activity {
//	public final static int ACTIVITY_TYPE_FRIEND_DETAIL = 0;
//	public final static int ACTIVITY_TYPE_MY_DETAIL = 1;
//	Workspace pager;
//    int off_width = 10;//照片的边距
//
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.layout_workspace_demo);
//		   ListView listView1 = new ListView(this);
//	        listView1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,getData1()));
//		initSlideView(listView1);
//	}
//	
//	private void initSlideView(View ContentView){
//		WindowManager windowManager = this.getWindowManager();
//		Display display = windowManager.getDefaultDisplay();
//		int screenWidth = display.getWidth();
//		//control = (Indicator) findViewById(R.id.indicator);
//		pager = (Workspace) findViewById(R.id.workspace);
//		pager.setCount(2);
//		pager.setPageWidth(screenWidth+off_width);
//		pager.setBufferCount(3);
//		pager.setSpaceWidth(off_width);
//		pager.setOnItemListener(new OnItemListener() {
//			@Override
//			public void OnItemSelected(AdapterView<?> parent, Item item,
//					int position, long id) {
//				item.setSelected();
//			View view =	DisplayImageActivity.this.findViewById(R.id.textNum);
//		   ((TextView)(view)).setText(""+(position%3+1));
//			}
//
//			@Override
//			public void OnItemLoading(AdapterView<?> parent, Item item,
//					int position, long id){
//				item.load();
//			}
//		});
//
//		pager.initIndex(1);
//		pager.reInitItemLoadData();
//		   ListView listView1 = new ListView(this);
//	        listView1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,getData1()));
//	        pager.initAllItemView(new View[]{listView1,ContentView});
//	}
//
//
//
//	    private List<String> getData1(){
//	        List<String> data = new ArrayList<String>();
//
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//
//	        data.add("测试数据1");
//
//	        return data;
//
//	    }
//
//	public void reInit(){
//		pager.setCount(10);
//		//control.setNumPages(pager.getCount());
//		Log.i("zhoubo", "pager.getCurrentScreen()==="+pager.getCurrentScreen());
//		pager.initIndex(pager.getCurrentScreen());
////		pager.itemLoaded(pager.getCurrentScreen());
//		pager.reInitItemLoadData();
//	}
//
//
//
//
//
//
//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//		final Workspace pager = (Workspace) findViewById(R.id.workspace);
//		WindowManager windowManager = this.getWindowManager();
//		Display display = windowManager.getDefaultDisplay();
//		int screenWidth = display.getWidth();
//		pager.setPageWidth(screenWidth+off_width);
//		pager.relayout();
//	}
// }
