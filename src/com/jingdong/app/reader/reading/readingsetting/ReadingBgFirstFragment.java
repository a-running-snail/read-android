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
import com.tendcloud.tenddata.TCAgent;

public class ReadingBgFirstFragment extends Fragment {

	private ViewGroup rootView;
	/** 纯白 */
    private View allwhiteTheme;
    /** 米黄 */
    private View beigeTheme;
    /** 白纸 */
    private View whitepaperTheme;
    /** 粉色 */
    private View pinkTheme;
    /** 纸黄色 */
    private View paper_yellow_Theme;
    /** 海军蓝 */
    private View blueTheme;
    /** 暗绿 */
    private View greyTheme;
    /** 酱色 */
    private View caramelTheme;
    /** 深褐 */
    private View sepiaTheme;
    
    private Context context;

	public ReadingBgFirstFragment() {
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
				R.layout.reading_color_select, null);
		return rootView;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context = getActivity();
		
		allwhiteTheme = rootView.findViewById(R.id.allwhiteTheme);
        beigeTheme = rootView.findViewById(R.id.beigeTheme);
        whitepaperTheme = rootView.findViewById(R.id.whitepaperTheme);
        pinkTheme = rootView.findViewById(R.id.pinkTheme);
        paper_yellow_Theme = rootView.findViewById(R.id.paper_yellow_Theme);
        blueTheme = rootView.findViewById(R.id.blueTheme);
        greyTheme = rootView.findViewById(R.id.greyTheme);
        caramelTheme = rootView.findViewById(R.id.caramelTheme);
        sepiaTheme = rootView.findViewById(R.id.sepiaTheme);
        
      //TODO 
		allwhiteTheme.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeNightModel();
				loadAllWhiteStyle();
			}
		});

		beigeTheme.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeNightModel();
				loadBeigeStyle();
			}
		});

		whitepaperTheme.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeNightModel();
				loadWhitePaperStyle();
			}
		});

		pinkTheme.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeNightModel();
				loadPinkStyle();
			}
		});

		paper_yellow_Theme.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeNightModel();
				loadPaperYellowStyle();
			}
		});

		blueTheme.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeNightModel();
				loadBlueStyle();
			}
		});

		greyTheme.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeNightModel();
				loadGreyStyle();
			}
		});

		caramelTheme.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeNightModel();
				loadCaramelStyle();
			}
		});

		sepiaTheme.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeNightModel();
				loadSepiaStyle();
			}
		});
		
		initView();
		setupNightMode();
		EventBus.getDefault().register(this);
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
	
    private void initView(){
    	clearSetupThemeStyle();
    	int bg_color = LocalUserSetting.getReading_Background_Color(context);
    	
		if(bg_color == 0xFFFFFFFF && LocalUserSetting.getReading_Background_Texture(context) == -1){
	    	setupAllWhiteStyle();
		}else if (bg_color == 0xFFF7E7DE) {
	    	setupBeigeStyle();
		}else if (bg_color == 0xFFF7E3D6) {
	    	setupWhitePaperStyle();
		}else if (bg_color == 0xFFFFE7EF) {
	    	setupPinkPaperStyle();
		}else if (bg_color == 0xFFDEC79C) {
			setupPaperYellowStyle();
		}else if (bg_color == 0xFF08598C) {
		  	setupBlueStyle();
		}else if (bg_color == 0xFF104139) {
	    	setupGreyStyle();
		}else if (bg_color == 0xFF21495A) {
		   	setupCaramelStyle();
		}else if (bg_color == 0xFF212421) {
		 	setupSepiaamelStyle();
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
     * 清除选择状态
     */
    private void clearSetupThemeStyle(){
    	allwhiteTheme.setBackgroundResource(R.drawable.read_allwhite_theme);
        beigeTheme.setBackgroundResource(R.drawable.read_beige_theme);
        whitepaperTheme.setBackgroundResource(R.drawable.read_whitepaper_theme);
        pinkTheme.setBackgroundResource(R.drawable.read_pink_theme);
        paper_yellow_Theme.setBackgroundResource(R.drawable.read_paper_yellow_theme);
        blueTheme.setBackgroundResource(R.drawable.read_blue_theme);
        greyTheme.setBackgroundResource(R.drawable.read_grey_theme);
        caramelTheme.setBackgroundResource(R.drawable.read_caramel_theme);
        sepiaTheme.setBackgroundResource(R.drawable.read_sepia_theme);
    }
	
	private void setupAllWhiteStyle() {
      allwhiteTheme.setBackgroundResource(R.drawable.read_allwhite_theme_highlight);
      beigeTheme.setBackgroundResource(R.drawable.read_beige_theme);
      whitepaperTheme.setBackgroundResource(R.drawable.read_whitepaper_theme);
      pinkTheme.setBackgroundResource(R.drawable.read_pink_theme);
      paper_yellow_Theme.setBackgroundResource(R.drawable.read_paper_yellow_theme);
      blueTheme.setBackgroundResource(R.drawable.read_blue_theme);
      greyTheme.setBackgroundResource(R.drawable.read_grey_theme);
      caramelTheme.setBackgroundResource(R.drawable.read_caramel_theme);
      sepiaTheme.setBackgroundResource(R.drawable.read_sepia_theme);
  }
  
  private void loadAllWhiteStyle() {
  	setupAllWhiteStyle();
      // save color to user setting
      LocalUserSetting.saveReading_Background_Color(context,0xFFFFFFFF);
      LocalUserSetting.saveReading_Background_Texture(context,-1);
      LocalUserSetting.saveReading_Text_Color(context, 0xFF000000);
      LocalUserSetting.saveIgnoreCssTextColor(context, false);
      Intent intent = new Intent(ReadOverlayActivity.ACTION_READSTYLE_CHANGE);
      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
  
  private void setupBeigeStyle() {
      allwhiteTheme.setBackgroundResource(R.drawable.read_allwhite_theme);
      beigeTheme.setBackgroundResource(R.drawable.read_beige_theme_highlight);
      whitepaperTheme.setBackgroundResource(R.drawable.read_whitepaper_theme);
      pinkTheme.setBackgroundResource(R.drawable.read_pink_theme);
      paper_yellow_Theme.setBackgroundResource(R.drawable.read_paper_yellow_theme);
      blueTheme.setBackgroundResource(R.drawable.read_blue_theme);
      greyTheme.setBackgroundResource(R.drawable.read_grey_theme);
      caramelTheme.setBackgroundResource(R.drawable.read_caramel_theme);
      sepiaTheme.setBackgroundResource(R.drawable.read_sepia_theme);
  }
  
  private void loadBeigeStyle() {
  	setupBeigeStyle();
      LocalUserSetting.saveReading_Background_Color(context,0xFFF7E7DE);
      LocalUserSetting.saveReading_Background_Texture(context,-1);
      LocalUserSetting.saveReading_Text_Color(context, 0xFF313031);
      LocalUserSetting.saveIgnoreCssTextColor(context, false);
      Intent intent = new Intent(ReadOverlayActivity.ACTION_READSTYLE_CHANGE);
      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
  
  private void setupWhitePaperStyle() {
      allwhiteTheme.setBackgroundResource(R.drawable.read_allwhite_theme);
      beigeTheme.setBackgroundResource(R.drawable.read_beige_theme);
      whitepaperTheme.setBackgroundResource(R.drawable.read_whitepaper_theme_highlight);
      pinkTheme.setBackgroundResource(R.drawable.read_pink_theme);
      paper_yellow_Theme.setBackgroundResource(R.drawable.read_paper_yellow_theme);
      blueTheme.setBackgroundResource(R.drawable.read_blue_theme);
      greyTheme.setBackgroundResource(R.drawable.read_grey_theme);
      caramelTheme.setBackgroundResource(R.drawable.read_caramel_theme);
      sepiaTheme.setBackgroundResource(R.drawable.read_sepia_theme);
  }
  
  private void loadWhitePaperStyle() {
  	setupWhitePaperStyle();
      // save color to user setting
      LocalUserSetting.saveReading_Background_Color(context,0xFFF7E3D6);
      LocalUserSetting.saveReading_Background_Texture(context,-1);
      LocalUserSetting.saveReading_Text_Color(context, 0xFF312C29);
      LocalUserSetting.saveIgnoreCssTextColor(context, false);
      Intent intent = new Intent(ReadOverlayActivity.ACTION_READSTYLE_CHANGE);
      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
  
  private void setupPinkPaperStyle() {
      allwhiteTheme.setBackgroundResource(R.drawable.read_allwhite_theme);
      beigeTheme.setBackgroundResource(R.drawable.read_beige_theme);
      whitepaperTheme.setBackgroundResource(R.drawable.read_whitepaper_theme);
      pinkTheme.setBackgroundResource(R.drawable.read_pink_theme_highlight);
      paper_yellow_Theme.setBackgroundResource(R.drawable.read_paper_yellow_theme);
      blueTheme.setBackgroundResource(R.drawable.read_blue_theme);
      greyTheme.setBackgroundResource(R.drawable.read_grey_theme);
      caramelTheme.setBackgroundResource(R.drawable.read_caramel_theme);
      sepiaTheme.setBackgroundResource(R.drawable.read_sepia_theme);
  }
  
  private void loadPinkStyle() {
  	setupPinkPaperStyle();
      // save color to user setting
      LocalUserSetting.saveReading_Background_Color(context,0xFFFFE7EF);
      LocalUserSetting.saveReading_Background_Texture(context,-1);
      LocalUserSetting.saveReading_Text_Color(context, 0xFF312C2A);
      LocalUserSetting.saveIgnoreCssTextColor(context, false);
      Intent intent = new Intent(ReadOverlayActivity.ACTION_READSTYLE_CHANGE);
      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
  
  private void setupPaperYellowStyle() {
      allwhiteTheme.setBackgroundResource(R.drawable.read_allwhite_theme);
      beigeTheme.setBackgroundResource(R.drawable.read_beige_theme);
      whitepaperTheme.setBackgroundResource(R.drawable.read_whitepaper_theme);
      pinkTheme.setBackgroundResource(R.drawable.read_pink_theme);
      paper_yellow_Theme.setBackgroundResource(R.drawable.read_paperyellow_theme_highlight);
      blueTheme.setBackgroundResource(R.drawable.read_blue_theme);
      greyTheme.setBackgroundResource(R.drawable.read_grey_theme);
      caramelTheme.setBackgroundResource(R.drawable.read_caramel_theme);
      sepiaTheme.setBackgroundResource(R.drawable.read_sepia_theme);
  }
  
  private void loadPaperYellowStyle() {
  	setupPaperYellowStyle();
      // save color to user setting
      LocalUserSetting.saveReading_Background_Color(context,0xFFDEC79C);
      LocalUserSetting.saveReading_Background_Texture(context,-1);
      LocalUserSetting.saveReading_Text_Color(context, 0xFF311400);
      LocalUserSetting.saveIgnoreCssTextColor(context, false);
      Intent intent = new Intent(ReadOverlayActivity.ACTION_READSTYLE_CHANGE);
      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
  
  private void setupBlueStyle() {
      allwhiteTheme.setBackgroundResource(R.drawable.read_allwhite_theme);
      beigeTheme.setBackgroundResource(R.drawable.read_beige_theme);
      whitepaperTheme.setBackgroundResource(R.drawable.read_whitepaper_theme);
      pinkTheme.setBackgroundResource(R.drawable.read_pink_theme);
      paper_yellow_Theme.setBackgroundResource(R.drawable.read_paper_yellow_theme);
      blueTheme.setBackgroundResource(R.drawable.read_blue_theme_highlight);
      greyTheme.setBackgroundResource(R.drawable.read_grey_theme);
      caramelTheme.setBackgroundResource(R.drawable.read_caramel_theme);
      sepiaTheme.setBackgroundResource(R.drawable.read_sepia_theme);
  }
  
  private void loadBlueStyle() {
  	setupBlueStyle();
      // save color to user setting
      LocalUserSetting.saveReading_Background_Color(context,0xFF08598C);
      LocalUserSetting.saveReading_Background_Texture(context,-1);
      LocalUserSetting.saveReading_Text_Color(context, 0xFFCEE7F7);
      LocalUserSetting.saveIgnoreCssTextColor(context, true);
      Intent intent = new Intent(ReadOverlayActivity.ACTION_READSTYLE_CHANGE);
      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
  
  private void setupGreyStyle() {
      allwhiteTheme.setBackgroundResource(R.drawable.read_allwhite_theme);
      beigeTheme.setBackgroundResource(R.drawable.read_beige_theme);
      whitepaperTheme.setBackgroundResource(R.drawable.read_whitepaper_theme);
      pinkTheme.setBackgroundResource(R.drawable.read_pink_theme);
      paper_yellow_Theme.setBackgroundResource(R.drawable.read_paper_yellow_theme);
      blueTheme.setBackgroundResource(R.drawable.read_blue_theme);
      greyTheme.setBackgroundResource(R.drawable.read_grey_theme_highlight);
      caramelTheme.setBackgroundResource(R.drawable.read_caramel_theme);
      sepiaTheme.setBackgroundResource(R.drawable.read_sepia_theme);
  }
  
  private void loadGreyStyle() {
  	setupGreyStyle();
      // save color to user setting
      LocalUserSetting.saveReading_Background_Color(context,0xFF104139);
      LocalUserSetting.saveReading_Background_Texture(context,-1);
      LocalUserSetting.saveReading_Text_Color(context, 0xFFEFEBD6);
      LocalUserSetting.saveIgnoreCssTextColor(context, true);
      Intent intent = new Intent(ReadOverlayActivity.ACTION_READSTYLE_CHANGE);
      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
  
  private void setupCaramelStyle() {
      allwhiteTheme.setBackgroundResource(R.drawable.read_allwhite_theme);
      beigeTheme.setBackgroundResource(R.drawable.read_beige_theme);
      whitepaperTheme.setBackgroundResource(R.drawable.read_whitepaper_theme);
      pinkTheme.setBackgroundResource(R.drawable.read_pink_theme);
      paper_yellow_Theme.setBackgroundResource(R.drawable.read_paper_yellow_theme);
      blueTheme.setBackgroundResource(R.drawable.read_blue_theme);
      greyTheme.setBackgroundResource(R.drawable.read_grey_theme);
      caramelTheme.setBackgroundResource(R.drawable.read_caramel_theme_highlight);
      sepiaTheme.setBackgroundResource(R.drawable.read_sepia_theme);
  }
  
  private void loadCaramelStyle() {
  	setupCaramelStyle();
      // save color to user setting
      LocalUserSetting.saveReading_Background_Color(context,0xFF21495A);
      LocalUserSetting.saveReading_Background_Texture(context,-1);
      LocalUserSetting.saveReading_Text_Color(context, 0xFFEFE3E7);
      LocalUserSetting.saveIgnoreCssTextColor(context, true);
      Intent intent = new Intent(ReadOverlayActivity.ACTION_READSTYLE_CHANGE);
      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
  
  private void setupSepiaamelStyle() {
      allwhiteTheme.setBackgroundResource(R.drawable.read_allwhite_theme);
      beigeTheme.setBackgroundResource(R.drawable.read_beige_theme);
      whitepaperTheme.setBackgroundResource(R.drawable.read_whitepaper_theme);
      pinkTheme.setBackgroundResource(R.drawable.read_pink_theme);
      paper_yellow_Theme.setBackgroundResource(R.drawable.read_paper_yellow_theme);
      blueTheme.setBackgroundResource(R.drawable.read_blue_theme);
      greyTheme.setBackgroundResource(R.drawable.read_grey_theme);
      caramelTheme.setBackgroundResource(R.drawable.read_caramel_theme);
      sepiaTheme.setBackgroundResource(R.drawable.read_sepia_theme_highlight);
  }
  
  private void loadSepiaStyle() {
  	setupSepiaamelStyle();
      // save color to user setting
      LocalUserSetting.saveReading_Background_Color(context,0xFF212421);
      LocalUserSetting.saveReading_Background_Texture(context,-1);
      LocalUserSetting.saveReading_Text_Color(context, 0xFFEFE3E8);
      LocalUserSetting.saveIgnoreCssTextColor(context, true);
      Intent intent = new Intent(ReadOverlayActivity.ACTION_READSTYLE_CHANGE);
      LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
	
  
  
  private void setupNightMode() {
		if (LocalUserSetting.getReading_Night_Model(context)) {
			rootView.setBackgroundColor(getResources().getColor(R.color.n_bg_main));
			((TextView)rootView.findViewById(R.id.allwhiteThemeText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.beigeThemeText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.whitepaperThemeText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.pinkThemeText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.paper_yellow_ThemeText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.blueThemeText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.greyThemeText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.caramelThemeText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)rootView.findViewById(R.id.sepiaThemeText)).setTextColor(getResources().getColor(R.color.n_text_main));
			rootView.findViewById(R.id.color_divider1).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			rootView.findViewById(R.id.color_divider2).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			rootView.findViewById(R.id.color_space1).setBackgroundColor(getResources().getColor(R.color.n_bg_sub));
		} else {
			rootView.setBackgroundColor(getResources().getColor(R.color.r_bg_main));
			((TextView)rootView.findViewById(R.id.allwhiteThemeText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.beigeThemeText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.whitepaperThemeText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.pinkThemeText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.paper_yellow_ThemeText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.blueThemeText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.greyThemeText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.caramelThemeText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)rootView.findViewById(R.id.sepiaThemeText)).setTextColor(getResources().getColor(R.color.r_text_main));
			rootView.findViewById(R.id.color_divider1).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			rootView.findViewById(R.id.color_divider2).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			rootView.findViewById(R.id.color_space1).setBackgroundColor(getResources().getColor(R.color.r_bg_sub));
			
		}
	}
	
	
	
	
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

}
