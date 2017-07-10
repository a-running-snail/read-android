package com.jingdong.app.reader.reading.readingsetting;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.activity.ReadOverlayActivity;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.entity.BaseEvent;
import com.jingdong.app.reader.entity.ChangeNightModeEvent;
import com.jingdong.app.reader.entity.extra.OrderEntity;
import com.jingdong.app.reader.entity.extra.OrderList;
import com.jingdong.app.reader.eventbus.de.greenrobot.event.EventBus;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.pay.OnlinePayActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.OnLinePayTools;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.tendcloud.tenddata.TCAgent;

public class ReadingBgSecondFragment extends Fragment implements OnClickListener{

	private ViewGroup rootView;
	
    private RoundNetworkImageView wl1ImageView;
    private RoundNetworkImageView wl2ImageView;
    private RoundNetworkImageView wl3ImageView;
    private RoundNetworkImageView wl4ImageView;
    private RoundNetworkImageView wl5ImageView;
    private RoundNetworkImageView wl6ImageView;
    private RoundNetworkImageView wl7ImageView;
    private RoundNetworkImageView wl8ImageView;
    private RoundNetworkImageView wl9ImageView;
    
    private LinearLayout layout1;
    private LinearLayout layout2;
    private LinearLayout layout3;
    private LinearLayout layout4;
    private LinearLayout layout5;
    private LinearLayout layout6;
    private LinearLayout layout7;
    private LinearLayout layout8;
    private LinearLayout layout9;
    
    private RoundNetworkImageView[] imageViews = new RoundNetworkImageView[9];
    private LinearLayout[] layouts = new LinearLayout[9];
    private int[] textColors = new int[9];
    
    private Context context;

	public ReadingBgSecondFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		rootView = (ViewGroup) inflater.inflate(
				R.layout.reading_texture_select, null);
		return rootView;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context = getActivity();
		
		wl1ImageView = (RoundNetworkImageView) rootView.findViewById(R.id.wl1_imageview);
		wl2ImageView = (RoundNetworkImageView) rootView.findViewById(R.id.wl2_imageview);
		wl3ImageView = (RoundNetworkImageView) rootView.findViewById(R.id.wl3_imageview);
		wl4ImageView = (RoundNetworkImageView) rootView.findViewById(R.id.wl4_imageview);
		wl5ImageView = (RoundNetworkImageView) rootView.findViewById(R.id.wl5_imageview);
		wl6ImageView = (RoundNetworkImageView) rootView.findViewById(R.id.wl6_imageview);
		wl7ImageView = (RoundNetworkImageView) rootView.findViewById(R.id.wl7_imageview);
		wl8ImageView = (RoundNetworkImageView) rootView.findViewById(R.id.wl8_imageview);
		wl9ImageView = (RoundNetworkImageView) rootView.findViewById(R.id.wl9_imageview);
		
		layout1 = (LinearLayout) rootView.findViewById(R.id.layout1);
		layout2 = (LinearLayout) rootView.findViewById(R.id.layout2);
		layout3 = (LinearLayout) rootView.findViewById(R.id.layout3);
		layout4 = (LinearLayout) rootView.findViewById(R.id.layout4);
		layout5 = (LinearLayout) rootView.findViewById(R.id.layout5);
		layout6 = (LinearLayout) rootView.findViewById(R.id.layout6);
		layout7 = (LinearLayout) rootView.findViewById(R.id.layout7);
		layout8 = (LinearLayout) rootView.findViewById(R.id.layout8);
		layout9 = (LinearLayout) rootView.findViewById(R.id.layout9);
        
		if(imageViews==null)
			imageViews = new RoundNetworkImageView[9]; 
		imageViews[0] = wl1ImageView;
		imageViews[1] = wl2ImageView;
		imageViews[2] = wl3ImageView;
		imageViews[3] = wl4ImageView;
		imageViews[4] = wl5ImageView;
		imageViews[5] = wl6ImageView;
		imageViews[6] = wl7ImageView;
		imageViews[7] = wl8ImageView;
		imageViews[8] = wl9ImageView;
		
		if(layouts==null)
			layouts = new LinearLayout[9]; 
		layouts[0] = layout1;
		layouts[1] = layout2;
		layouts[2] = layout3;
		layouts[3] = layout4;
		layouts[4] = layout5;
		layouts[5] = layout6;
		layouts[6] = layout7;
		layouts[7] = layout8;
		layouts[8] = layout9;
		
		for (int i = 0; i < 9; i++) {
			layouts[i].setOnClickListener(this);
		}
		
		
		initView();
		setupNightMode();
		EventBus.getDefault().register(this);
	}
	
	//TODO 初始化选择
    private void initView(){
    	clearStyle();
    	int bgTexture = LocalUserSetting.getReading_Background_Texture(context);
    	textColors = getActivity().getResources().getIntArray(R.array.bookPageViewTextColor);
    	if(bgTexture != -1 && !LocalUserSetting.getReading_Night_Model(context)){
    		int textColor;
    		try {
    			textColor = textColors[bgTexture];
    		} catch (Exception e) {
    			e.printStackTrace();
    			textColor = 0xFF111111;
    		}
    		loadTextureStyle(bgTexture, textColor, true);
    	}
    }
    
    
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
    	super.setUserVisibleHint(isVisibleToUser);
    	try {
			initView();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
    private void changeNightModel(){
		LocalUserSetting.saveReading_Night_Model(getActivity(), false);
		setupNightMode();
		EventBus.getDefault().post(new ChangeNightModeEvent());
    }
    /**
     * 改变黑夜白天设置时背景改变
     * @param baseEvent
     */
    public void onEventMainThread(BaseEvent baseEvent) {
		if (baseEvent instanceof ChangeNightModeEvent) {
			setupNightMode();
		}
	}
	
    /**
     * 重置所有的纹理选择框的背景
     */
	private void clearStyle() {
	  wl1ImageView.setBorderWidth(0);
	  wl2ImageView.setBorderWidth(0);
	  wl3ImageView.setBorderWidth(0);
	  wl4ImageView.setBorderWidth(0);
	  wl5ImageView.setBorderWidth(0);
	  wl6ImageView.setBorderWidth(0);
      wl7ImageView.setBorderWidth(0);
      wl8ImageView.setBorderWidth(0);
      wl9ImageView.setBorderWidth(0);
  }
  /**
   * 加载选中的纹理背景，通知页面更换背景
   * @param bgColor
   * @param textColor
   * @param ignoreCssTextColor
   */
  private void loadTextureStyle(int bgTexture,int textColor,boolean ignoreCssTextColor) {
	  if(bgTexture != -1 ){
		  clearStyle();
		  imageViews[bgTexture].setBorderWidth(ScreenUtils.dip2px(2));
		  changeNightModel();
	      LocalUserSetting.saveReading_Background_Color(context,0xFFFFFFFF);
	      LocalUserSetting.saveReading_Background_Texture(context,bgTexture);//设置纹理
	      LocalUserSetting.saveReading_Text_Color(context, textColor);
	      LocalUserSetting.saveIgnoreCssTextColor(context, ignoreCssTextColor);
	      Intent intent = new Intent(ReadOverlayActivity.ACTION_READSTYLE_CHANGE);
	      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	  }
  }
  
  /**
   * 黑夜、白天模式文字颜色设置
   */
  private void setupNightMode() {
		if (LocalUserSetting.getReading_Night_Model(context)) {
			rootView.setBackgroundColor(getResources().getColor(R.color.n_bg_main));
			((TextView)rootView.findViewById(R.id.wl1_text)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.wl2_text)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.wl3_text)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.wl4_text)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.wl5_text)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.wl6_text)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.wl7_text)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.wl8_text)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.wl9_text)).setTextColor(getResources().getColor(R.color.n_text_main));
			rootView.findViewById(R.id.color_divider1).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			rootView.findViewById(R.id.color_divider2).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			rootView.findViewById(R.id.color_space1).setBackgroundColor(getResources().getColor(R.color.n_bg_sub));
		} else {
			rootView.setBackgroundColor(getResources().getColor(R.color.r_bg_main));
			((TextView)rootView.findViewById(R.id.wl1_text)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.wl2_text)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.wl3_text)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.wl4_text)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.wl5_text)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.wl6_text)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.wl7_text)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.wl8_text)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.wl9_text)).setTextColor(getResources().getColor(R.color.r_text_main));
			rootView.findViewById(R.id.color_divider1).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			rootView.findViewById(R.id.color_divider2).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			rootView.findViewById(R.id.color_space1).setBackgroundColor(getResources().getColor(R.color.r_bg_sub));
			

		}
	}
	
  
  
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout1:
			loadTextureStyle(0, textColors[0], true);
			break;
		case R.id.layout2:
			loadTextureStyle(1, textColors[1], true);
			break;
		case R.id.layout3:
			loadTextureStyle(2, textColors[2], true);
			break;
		case R.id.layout4:
			loadTextureStyle(3, textColors[3], true);
			break;
		case R.id.layout5:
			loadTextureStyle(4, textColors[4], true);
			break;
		case R.id.layout6:
			loadTextureStyle(5, textColors[5], true);
			break;
		case R.id.layout7:
			loadTextureStyle(6, textColors[6], true);
			break;
		case R.id.layout8:
			loadTextureStyle(7, textColors[7], true);
			break;
		case R.id.layout9:
			loadTextureStyle(8, textColors[8], true);
			break;
		default:
			break;
		}
		
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

}
