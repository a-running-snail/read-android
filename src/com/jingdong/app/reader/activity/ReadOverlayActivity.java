package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.CommonFragmentActivity;
import com.jingdong.app.reader.entity.BaseEvent;
import com.jingdong.app.reader.entity.ChangeNightModeEvent;
import com.jingdong.app.reader.entity.ReLoginEvent;
import com.jingdong.app.reader.eventbus.de.greenrobot.event.EventBus;
import com.jingdong.app.reader.eventbus.event.MessageEvent;
import com.jingdong.app.reader.reading.ChapterPageIndex;
import com.jingdong.app.reader.reading.readingsetting.ReadingBgFirstFragment;
import com.jingdong.app.reader.reading.readingsetting.ReadingBgSecondFragment;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ThemeUtils;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.view.ColorPickView;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.ColorPickView.OnColorChangedListener;

public class ReadOverlayActivity extends CommonFragmentActivity {

	public static final String ACTION_READFONT_CHANGE_DONE = "com.jingdong.app.reader.readoverlayactivity.readfontchangedone";
    public static final String ACTION_READSTYLE_CHANGE = "com.jingdong.app.reader.readoverlayactivity.readstylechange";
    public static final String ACTION_PAGENUMBER_CHANGE = "com.jingdong.app.reader.readoverlayactivity.pagenumberchange";
    public static final String ACTION_PAGENUMBER_CHANGE_DONE = "com.jingdong.app.reader.readoverlayactivity.pagenumberchangedone";
    public static final String ACTION_SETTING_FONT_CHANGE = "com.jingdong.app.reader.readoverlayactivity.settingfontchange";
    public static final String ACTION_PAGECOUNT_DONE = "com.jingdong.app.reader.readoverlayactivity.pagecountdone";
    public static final String ACTION_FONT_CHANGE_DONE = "com.jingdong.app.reader.readoverlayactivity.fontchangedone";
    public static final String ACTION_GO_BACK_PROGRESS = "com.jingdong.app.reader.readoverlayactivity.gobackprogress";
    public static final String ACTION_GO_FORWARD_PROGRESS = "com.jingdong.app.reader.readoverlayactivity.goforwardprogress";
    public static final String ACTION_BACK_PROGRESS_DONE = "com.jingdong.app.reader.readoverlayactivity.backprogressdone";
    public static final String ACTION_CHANGE_PROGRESS_DONE = "com.jingdong.app.reader.readoverlayactivity.changeprogressdone";
    public static final String ACTION_CHANGE_READ_SPACE = "com.jingdong.app.reader.readoverlayactivity.changereadspace";
    public static final String ACTION_CHANGE_READ_SPACE_DONE = "com.jingdong.app.reader.readoverlayactivity.changereadspacedone";
    public static final String ACTION_READ_SPACE_DONE = "com.jingdong.app.reader.readoverlayactivity.readspacedone";
    public static final String ACTION_SIMPLIFIED_TO_TRADITIONAL = "com.jingdong.app.reader.readoverlayactivity.simplifiedtotraditional";


    public static final String BookMarkStateKey = "BookMarkStateKey";
    public static final String ShowPurchaseButtonKey = "ShowPurchaseButtonKey";
    public static final String ShowPlayListButtonKey = "ShowPlayListButtonKey";
    public static final String ChapterPageIndexListKey = "ChapterPageIndexListKey";
    public static final String CurrentPageIndexKey = "CurrentPageIndexKey";
    public static final String PageFontSizeKey = "PageFontSizeKey";
    public static final String PageCountKey = "PageCountKey";
    public static final String PageNumberKey = "PageNumberKey";
    public static final String IsOpenPanelKey = "IsOpenPanelKey";
    public static final String IsBackProgressKey = "IsBackProgressKey";
    public static final int READ_STYLE_WHITE = 0;
    public static final int READ_STYLE_SOFT = 1;
    public static final int READ_STYLE_MINT = 2;
    public static final int READ_STYLE_NIGHT = 3;
    public static final int WHITE_STYLE_FONT = 0xFF333333;
    public static final int SOFT_STYLE_FONT = 0xFF4D4841;
    public static final int MINT_STYLE_FONT = 0xFF484D41;
    public static final int NIGHT_STYLE_FONT = 0xFF8C8C8C;

    private static final int FONT_SIZE_MIN_LEVEL = 0;
    private static final int FONT_SIZE_MAX_LEVEL = 9;
    private View readSettingView;
    private View readSettingPanel;
    private View readSettingShadow;
    private SeekBar brightnessSeek;
    private View fontSize1;
    private View fontSize2;
    private View fontTraditional;
    private ImageView markImage;
    private ImageView switchLine;
    private ImageView switchOnDot;
    private ImageView switchOffDot;
    private ImageView nightModeImage;
    private ImageView progressBackForwardImage;
    private View progressBackForward;
    private View purchaseFullBook;
    private int textSizeLevel;
    private int pageCount;
    private int lineSpaceLevel;
    private int blockSpaceLevel;
    private int pageEdgeSpaceLevel;
    private View readSettingWhite;
    private View readSettingSoft;
    private View readSettingMint;
    private View readSettingMore;
    private View pageIndicator;
    private TextView chapterNameView;
    private TextView pageNumber;
    private SeekBar readSeekBar;
    private ProgressBar pageProgress;
    private ArrayList<ChapterPageIndex> chapterPageIndexList;
    private float systemBrightness;
    private int brightnessProgress;
    private int localProgress;
    private boolean isSyncBrightness;
    private boolean isBackProgress;
    private boolean isTraditional;
    private boolean isMarked;
    private LinearLayout clolr_select;
//    private View select_allwhite;
//    private View allwhiteTheme;
//    private View beigeTheme;
//    private View whitepaperTheme;
//    private View pinkTheme;
//    private View paper_yellow_Theme;
//    private View blueTheme;
//    private View greyTheme;
//    private View caramelTheme;
//    private View sepiaTheme;
//    private View select_colors;
    private View custom_colors;
    private View clolr_pick;
    private LinearLayout bg_layout;
    private LinearLayout text_layout;
    private TextView bg_text;
    private ImageView bg_dot;
    private TextView text_color;
    private ImageView text_dot;
    private ImageView playListImg;
	private ColorPickView bgView;
	private ColorPickView tView;
	private boolean night_model = false;
	private View topContainer;
	private View tocView;
	private View readBackView;
	private View searchBookView;
	private View readNoteView;
	private View settingMoreView;
	private View modifyTextView;
	private View moreView;
	private View largeWidth;
    private View smallWidth;
    private View mediumWidth;
    private View lineSpaceUp;
    private View lineSpaceDown;
    private View blockSpaceUp;
    private View blockSpaceDown;
    private View adjustRootLayout;
	private LinearLayout bg_colorpickerLayout;
	private LinearLayout t_colorpickerLayout;
	private LinearLayout adjust_layout;
	private LinearLayout adjust;
	private int bg_color;
	private ImageView fontSizeImage1,fontSizeImage2;
	
	private ViewPager vPager;
	private BookstorePagerAdapter pagerAdapter;
    
    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.prepareTheme(this);
        setContentView(R.layout.activity_read_overlay);
        topContainer = findViewById(R.id.top_container);
        topContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// do nothing
			}
		});
        tocView = findViewById(R.id.navToc);
        tocView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	setResult(BookPageViewActivity.RESULT_OPEN_TOC);
            	finish();
            }

        });
        
        searchBookView = findViewById(R.id.searchBook);
		searchBookView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(BookPageViewActivity.RESULT_SEARCH_BOOK);
            	finish();
			}

		});
		
		//朗读tmj
		View speechBookView = findViewById(R.id.speechBook);
		speechBookView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				TTSManager.getInstance().init();
//				TTSManager.getInstance().ttsPlay("习近平在讲话中指出，党中央历来高度重视西藏工作。");
				
				setResult(BookPageViewActivity.RESULT_SETTING_TTS);
            	finish();
			}

		});
		

        readBackView = findViewById(R.id.navBack);
        readBackView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	setResult(BookPageViewActivity.RESULT_VIEW_BACK);
            	finish();
            	overridePendingTransition(0, 0);
            }

        });

//        select_colors = findViewById(R.id.color_select_layout);
        custom_colors = findViewById(R.id.custom_colors);
        clolr_pick = findViewById(R.id.color_pick);
        bg_colorpickerLayout = (LinearLayout) findViewById(R.id.bg_colorpickerLayout);
        t_colorpickerLayout = (LinearLayout) findViewById(R.id.t_colorpickerLayout);
        bgView = (ColorPickView) findViewById(R.id.bgcolor_picker_view);
        tView = (ColorPickView) findViewById(R.id.tcolor_picker_view);
        bgView.initView(true);
        tView.initView(false);
        bgView.setOnColorChangedListener(new OnColorChangedListener() {

			@Override
			public void onColorChange(int color) {
				LocalUserSetting.saveReading_Background_Color(ReadOverlayActivity.this,color);
				LocalUserSetting.saveReading_Background_Texture(ReadOverlayActivity.this,-1);
		        Intent intent = new Intent(ACTION_READSTYLE_CHANGE);
		        LocalBroadcastManager.getInstance(ReadOverlayActivity.this).sendBroadcast(intent);
			}
		});
        
        tView.setOnColorChangedListener(new OnColorChangedListener() {
			
			@Override
			public void onColorChange(int color) {
				LocalUserSetting.saveReading_Text_Color(ReadOverlayActivity.this, color);
				LocalUserSetting.saveIgnoreCssTextColor(ReadOverlayActivity.this, true);
		        Intent intent = new Intent(ACTION_READSTYLE_CHANGE);
		        LocalBroadcastManager.getInstance(ReadOverlayActivity.this).sendBroadcast(intent);
			}
		});
        
        custom_colors.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				changeNightModel();
		        Intent intent = new Intent(ACTION_READSTYLE_CHANGE);
		        LocalBroadcastManager.getInstance(ReadOverlayActivity.this).sendBroadcast(intent);
				readSettingShadow.setVisibility(View.GONE);
				readSettingPanel.setVisibility(View.GONE);
				topContainer.setVisibility(View.GONE);
				clolr_select.setVisibility(View.GONE);
				clolr_pick.setVisibility(View.VISIBLE);
				t_colorpickerLayout.setVisibility(View.GONE);
				bg_colorpickerLayout.setVisibility(View.VISIBLE);
			}
		});
        
        readSettingView = findViewById(R.id.navReadSetting);
        readSettingShadow = findViewById(R.id.settingBottomBorder);
        readSettingPanel = findViewById(R.id.readSetting);
        
        readSettingView.setOnClickListener(new OnClickListener() {

            @Override
			public void onClick(View v) {
            	findViewById(R.id.nightMode).setVisibility(View.GONE);
            	findViewById(R.id.bottom_shadow).setVisibility(View.GONE);
            	findViewById(R.id.bottom_container).setVisibility(View.GONE);
				readSettingShadow.setVisibility(View.VISIBLE);
				readSettingPanel.setVisibility(View.VISIBLE);
			}

        });
        
        readNoteView = findViewById(R.id.navReadNote);
        readNoteView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setResult(BookPageViewActivity.RESULT_VIEW_NOTE);
            	finish();
			}
		});
        
        boolean isChangeTheme = getIntent().getBooleanExtra(IsOpenPanelKey, false);
        if (isChangeTheme) {
			readSettingShadow.setVisibility(View.VISIBLE);
			readSettingPanel.setVisibility(View.VISIBLE);
        }

        isMarked = getIntent().getBooleanExtra(BookMarkStateKey, false);
        View markBookView = findViewById(R.id.markBook);
        markImage = (ImageView) markBookView.findViewById(R.id.markBookImage);
        markBookView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	isMarked = !isMarked;
            	setupBookMarkImage();
            	setResult(BookPageViewActivity.RESULT_TOGGLE_BOOK_MARK);
            	finish();
            }

        });
        View backGroundView = findViewById(R.id.readOverlayBg);
        backGroundView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	finish();
            }

        });
        
        settingMoreView = findViewById(R.id.settingMore);
        settingMoreView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	Intent intent = new Intent(ReadOverlayActivity.this, ReaderSettingActivity.class);
    			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			startActivity(intent);
            	finish();
            }

        });
        
        switchLine = (ImageView) findViewById(R.id.switch_line);
        switchOnDot = (ImageView) findViewById(R.id.switchOn_dot);
        switchOffDot = (ImageView) findViewById(R.id.switchOff_dot);
        isSyncBrightness = LocalUserSetting.isSyncBrightness(ReadOverlayActivity.this);
        modifyTextView = findViewById(R.id.modifySystemLight);
        modifyTextView.setOnClickListener(new OnClickListener() {

            @Override
			public void onClick(View v) {
            	isSyncBrightness = !isSyncBrightness;
            	setupSwitchImage();
				LocalUserSetting.saveSyncBrightness(ReadOverlayActivity.this, isSyncBrightness);
				if (isSyncBrightness) {
					int mode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
					try {
						mode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
					} catch (SettingNotFoundException e) {
						e.printStackTrace();
					}
					if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
						systemBrightness = -1 / 255f;
					} else {
						int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
						systemBrightness = brightness / 255.0f;
					}
				} else {
					systemBrightness = LocalUserSetting.getReadBrightness(ReadOverlayActivity.this);
				}
				WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = systemBrightness;
                getWindow().setAttributes(layout);
                int brightnessProgress = (int) (systemBrightness * 100);
                brightnessSeek.setProgress(brightnessProgress);
			}

        });
        

        brightnessSeek = (SeekBar) findViewById(R.id.lightSeek);
        if (isSyncBrightness) {
        	int mode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
			try {
				mode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
			} catch (SettingNotFoundException e) {
				e.printStackTrace();
			}
			if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
				systemBrightness = -1 / 255f;
			} else {
	        	int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
	        	systemBrightness = brightness / 255.0f;
			}
        } else {
        	systemBrightness = LocalUserSetting.getReadBrightness(this);
        }
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = systemBrightness;
        getWindow().setAttributes(layout);
        brightnessProgress = (int) (systemBrightness * 100);
        brightnessSeek.setProgress(brightnessProgress);
        setupBrightness(brightnessProgress);
        brightnessSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                	brightnessProgress = progress;
                    WindowManager.LayoutParams layout = getWindow().getAttributes();
                    layout.screenBrightness = progress / 100.0f;
                    if (layout.screenBrightness <= 0) {
                        layout.screenBrightness = 0.01f;
                    }
                    getWindow().setAttributes(layout);
                    systemBrightness = layout.screenBrightness;
                    if (isSyncBrightness) {
                    	isSyncBrightness = false;
                    	setupSwitchImage();
                    	LocalUserSetting.saveSyncBrightness(ReadOverlayActivity.this, isSyncBrightness);
                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            	setupBrightness(brightnessProgress);
                LocalUserSetting.saveReadBrightness(ReadOverlayActivity.this, systemBrightness);
            }

        });
//        final TypedValue typedValue1 = new TypedValue();
//        getTheme().resolveAttribute(R.attr.read_fontsize_down_img, typedValue1, true);
//        final TypedValue typedValue2 = new TypedValue();
//        getTheme().resolveAttribute(R.attr.read_fontsize_up_img, typedValue2, true);
//        
//        final TypedValue typedValueFont = new TypedValue();
//        getTheme().resolveAttribute(R.attr.read_pressed_bg, typedValueFont, true);
        
        fontSizeImage1 = (ImageView) findViewById(R.id.fontSizeImage1);
        fontSizeImage2 = (ImageView) findViewById(R.id.fontSizeImage2);
        fontSize1 = findViewById(R.id.fontSize1);
        fontSize2 = findViewById(R.id.fontSize2);
        
        textSizeLevel = LocalUserSetting.getTextSizeLevel(this);
        fontSize1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	if (textSizeLevel <= FONT_SIZE_MIN_LEVEL) {
//            		ToastUtil.showToastInThread(getString(R.string.read_font_is_min), Toast.LENGTH_SHORT);
                	return;
                }
                textSizeLevel --;
                if(textSizeLevel == FONT_SIZE_MIN_LEVEL){
                	fontSizeImage1.setImageResource(R.drawable.reader_btn_fontsize_down_night);
                }else{
                	fontSizeImage1.setImageResource(R.drawable.reader_btn_fontsize_down_standard);
                }
                
                if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
                	fontSizeImage1.setImageResource(R.drawable.reader_btn_fontsize_down_night);
                	fontSizeImage2.setImageResource(R.drawable.reader_btn_fontsize_up_night);
                	fontSize1.setBackgroundResource(R.drawable.read_normal_bg_night);
                	fontSize2.setBackgroundResource(R.drawable.read_normal_bg_night);
                } else {
                	fontSizeImage2.setImageResource(R.drawable.reader_btn_fontsize_up_standard);
                	fontSize1.setBackgroundResource(R.drawable.read_normal_bg_standard);
                	fontSize2.setBackgroundResource(R.drawable.read_normal_bg_standard);
                }
                changeSettingFontSize();
            }

        });
       
        fontSize2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	if (textSizeLevel >= FONT_SIZE_MAX_LEVEL) {
//            		ToastUtil.showToastInThread(getString(R.string.read_font_is_max), Toast.LENGTH_SHORT);
                	return;
                }
                textSizeLevel ++;
                if(textSizeLevel == FONT_SIZE_MAX_LEVEL){
                	fontSizeImage2.setImageResource(R.drawable.reader_btn_fontsize_up_night);
                }else{
                	fontSizeImage2.setImageResource(R.drawable.reader_btn_fontsize_up_standard);
                }
                
                if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
                	fontSizeImage1.setImageResource(R.drawable.reader_btn_fontsize_down_night);
                	fontSizeImage2.setImageResource(R.drawable.reader_btn_fontsize_up_night);
                	fontSize1.setBackgroundResource(R.drawable.read_normal_bg_night);
                	fontSize2.setBackgroundResource(R.drawable.read_normal_bg_night);
                } else {
                	fontSizeImage1.setImageResource(R.drawable.reader_btn_fontsize_down_standard);
                	fontSize1.setBackgroundResource(R.drawable.read_normal_bg_standard);
                	fontSize2.setBackgroundResource(R.drawable.read_normal_bg_standard);
                }
                changeSettingFontSize();
            }

        });
        fontSize1.setEnabled(true);
    	fontSize2.setEnabled(true);
    	isTraditional = LocalUserSetting.isTraditional(this);
    	fontTraditional = findViewById(R.id.fontTraditional);
    	setupTraditionalUI();
    	fontTraditional.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				isTraditional = !isTraditional;
				LocalUserSetting.saveTraditional(ReadOverlayActivity.this, isTraditional);
				setupTraditionalUI();
				changeSimplifiedToTraditional();
			}
		});
    	

        readSettingWhite = findViewById(R.id.readSettingWhite);
        readSettingSoft = findViewById(R.id.readSettingSoft);
        readSettingMint = findViewById(R.id.readSettingMint);
        readSettingMore = findViewById(R.id.readSettingMore);
        adjust_layout = (LinearLayout) findViewById(R.id.adjust_layout);
        moreView = findViewById(R.id.moreView);
        
        //TODO 
//        select_allwhite = findViewById(R.id.select_allwhite);
//        allwhiteTheme = findViewById(R.id.allwhiteTheme);
//        beigeTheme = findViewById(R.id.beigeTheme);
//        whitepaperTheme = findViewById(R.id.whitepaperTheme);
//        pinkTheme = findViewById(R.id.pinkTheme);
//        paper_yellow_Theme = findViewById(R.id.paper_yellow_Theme);
//        blueTheme = findViewById(R.id.blueTheme);
//        greyTheme = findViewById(R.id.greyTheme);
//        caramelTheme = findViewById(R.id.caramelTheme);
//        sepiaTheme = findViewById(R.id.sepiaTheme);
        
        clolr_select = (LinearLayout)findViewById(R.id.color_select);
        
        vPager = (ViewPager) findViewById(R.id.vPager);
        
        adjust = (LinearLayout) findViewById(R.id.adjust);
        adjustRootLayout = adjust.findViewById(R.id.adjust_root_layout);
        
        adjust_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				readSettingShadow.setVisibility(View.GONE);
				readSettingPanel.setVisibility(View.GONE);
				clolr_select.setVisibility(View.GONE);
				clolr_pick.setVisibility(View.GONE);
				adjust.setVisibility(View.VISIBLE);
			}
		});
        
        clolr_select.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// do nothing
			}
		});
        
        bg_layout = (LinearLayout) findViewById(R.id.bg_layout);
        text_layout = (LinearLayout) findViewById(R.id.text_layout);
        
        bg_text = (TextView) findViewById(R.id.bg_text);
        bg_dot = (ImageView) findViewById(R.id.bg_dot);
        text_color = (TextView) findViewById(R.id.text_color);
        text_dot = (ImageView) findViewById(R.id.text_dot);
        
        bg_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bg_dot.setVisibility(View.VISIBLE);
				bg_text.setTextColor(getResources().getColor(R.color.text_main));
				text_dot.setVisibility(View.INVISIBLE);
				text_color.setTextColor(getResources().getColor(R.color.text_sub));
				t_colorpickerLayout.setVisibility(View.GONE);
				bg_colorpickerLayout.setVisibility(View.VISIBLE);
			}
		});
        
        text_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				text_dot.setVisibility(View.VISIBLE);
				text_color.setTextColor(getResources().getColor(R.color.text_main));
				bg_dot.setVisibility(View.INVISIBLE);
				bg_text.setTextColor(getResources().getColor(R.color.text_sub));
				t_colorpickerLayout.setVisibility(View.VISIBLE);
				bg_colorpickerLayout.setVisibility(View.GONE);
			}
		});
        
        //TODO 
//        allwhiteTheme.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				changeNightModel();
//				loadAllWhiteStyle();
//			}
//		});
//        
//        beigeTheme.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				changeNightModel();
//				loadBeigeStyle();
//			}
//		});
//        
//        whitepaperTheme.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				changeNightModel();
//				loadWhitePaperStyle();
//			}
//		});
//        
//        pinkTheme.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				changeNightModel();
//				loadPinkStyle();
//			}
//		});
//        
//        paper_yellow_Theme.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				changeNightModel();
//				loadPaperYellowStyle();
//			}
//		});
//        
//        blueTheme.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				changeNightModel();
//				loadBlueStyle();
//			}
//		});
//        
//        greyTheme.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				changeNightModel();
//				loadGreyStyle();
//			}
//		});
//        
//        caramelTheme.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				changeNightModel();
//				loadCaramelStyle();
//			}
//		});
//        
//        sepiaTheme.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				changeNightModel();
//				loadSepiaStyle();
//			}
//		});

        readSettingWhite.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	changeNightModel();
            	loadWhiteStyle();
            }

        });
        
        readSettingSoft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	changeNightModel();
            	loadSoftStyle();
            }

        });

        readSettingMint.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	changeNightModel();
            	loadMintStyle();
            }
        });
        
        pagerAdapter = new BookstorePagerAdapter(getSupportFragmentManager());
        vPager.setAdapter(pagerAdapter);
        vPager.setOffscreenPageLimit(2);
        vPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				dotMoveToPosition(position);
			}
		});
//        
        readSettingMore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				readSettingShadow.setVisibility(View.GONE);
				readSettingPanel.setVisibility(View.GONE);
				
				//TODO test vpager
				clolr_select.setVisibility(View.VISIBLE);
				vPager.setVisibility(View.VISIBLE);
				topContainer.setVisibility(View.GONE);
			}
		});
        
        lineSpaceLevel = LocalUserSetting.getLineSpaceLevel(this);
        lineSpaceUp = adjust.findViewById(R.id.lineSpaceUp);
        lineSpaceDown = adjust.findViewById(R.id.lineSpaceDown);
        lineSpaceDown.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				int level = LocalUserSetting.getLineSpaceLevel(ReadOverlayActivity.this);
				if (level <= 0) {
					level = 0;
					ToastUtil.showToastInThread(R.string.line_space_min);
					return;
				} else {
					level --;
				}
				LocalUserSetting.saveLineSpaceLevel(ReadOverlayActivity.this, level);
				changeSettingPageSpace();
			}
		});
        lineSpaceUp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				int level = LocalUserSetting.getLineSpaceLevel(ReadOverlayActivity.this);
				if (level >= 4) {
					level = 4;
					ToastUtil.showToastInThread(R.string.line_space_max);
					return;
				} else {
					level ++;
				}
				LocalUserSetting.saveLineSpaceLevel(ReadOverlayActivity.this, level);
				changeSettingPageSpace();
			}
		});
        
        blockSpaceLevel = LocalUserSetting.getBlockSpaceLevel(this);
        blockSpaceUp = adjust.findViewById(R.id.blockSpaceUp);
        blockSpaceDown = adjust.findViewById(R.id.blockSpaceDown);
        blockSpaceDown.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				int level = LocalUserSetting.getBlockSpaceLevel(ReadOverlayActivity.this);
				if (level <= 0) {
					level = 0;
					ToastUtil.showToastInThread(R.string.block_space_min);
					return;
				} else {
					level --;
				}
				LocalUserSetting.saveBlockSpaceLevel(ReadOverlayActivity.this, level);
				changeSettingPageSpace();
			}
		});
        blockSpaceUp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				int level = LocalUserSetting.getBlockSpaceLevel(ReadOverlayActivity.this);
				if (level >= 4) {
					level = 4;
					ToastUtil.showToastInThread(R.string.block_space_max);
					return;
				} else {
					level ++;
				}
				LocalUserSetting.saveBlockSpaceLevel(ReadOverlayActivity.this, level);
				changeSettingPageSpace();
			}
		});
        pageEdgeSpaceLevel = LocalUserSetting.getPageEdgeSpaceLevel(this);
        smallWidth = adjust.findViewById(R.id.smallWidth);
        smallWidth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				pageEdgeSpaceLevel = LocalUserSetting.getPageEdgeSpaceLevel(ReadOverlayActivity.this);
				if (pageEdgeSpaceLevel == 0) {
					return;
				} else {
					pageEdgeSpaceLevel = 0;
				}
				LocalUserSetting.savePageEdgeSpaceLevel(ReadOverlayActivity.this, pageEdgeSpaceLevel);
				setupPageEdgeSpaceStatus(pageEdgeSpaceLevel);
				changeSettingPageSpace();
			}
		});
        mediumWidth = adjust.findViewById(R.id.mediumWidth);
        mediumWidth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				pageEdgeSpaceLevel = LocalUserSetting.getPageEdgeSpaceLevel(ReadOverlayActivity.this);
				if (pageEdgeSpaceLevel == 1) {
					return;
				} else {
					pageEdgeSpaceLevel = 1;
				}
				LocalUserSetting.savePageEdgeSpaceLevel(ReadOverlayActivity.this, pageEdgeSpaceLevel);
				setupPageEdgeSpaceStatus(pageEdgeSpaceLevel);
				changeSettingPageSpace();
			}
		});
        largeWidth = adjust.findViewById(R.id.largeWidth);
        largeWidth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				pageEdgeSpaceLevel = LocalUserSetting.getPageEdgeSpaceLevel(ReadOverlayActivity.this);
				if (pageEdgeSpaceLevel == 2) {
					return;
				} else {
					pageEdgeSpaceLevel = 2;
				}
				LocalUserSetting.savePageEdgeSpaceLevel(ReadOverlayActivity.this, pageEdgeSpaceLevel);
				setupPageEdgeSpaceStatus(pageEdgeSpaceLevel);
				changeSettingPageSpace();
			}
		});

        boolean showPurchaseButton = getIntent().getBooleanExtra(ShowPurchaseButtonKey, false);
        purchaseFullBook = findViewById(R.id.navPurchase);
        if (showPurchaseButton) {
            purchaseFullBook.setVisibility(View.VISIBLE);
            purchaseFullBook.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	setResult(BookPageViewActivity.RESULT_PURCHASE_FULL_BOOK);
                	finish();
                }

            });
        } else {
            purchaseFullBook.setVisibility(View.GONE);
        }
        
        boolean showPlaylistButton = getIntent().getBooleanExtra(ShowPlayListButtonKey, false);
        View playlist = findViewById(R.id.gotoPlaylist);
        playListImg = (ImageView) playlist.findViewById(R.id.gotoPlaylistImg);
        if (showPlaylistButton) {
        	playlist.setVisibility(View.VISIBLE);
        	playlist.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	setResult(BookPageViewActivity.RESULT_OPEN_PLAYLIST);
                	finish();
                }

            });
        } else {
        	playlist.setVisibility(View.GONE);
        }
        
        View nightModeView = findViewById(R.id.nightMode);
        nightModeImage = (ImageView) nightModeView.findViewById(R.id.nightModeImage);
        nightModeView.setOnClickListener(new OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		night_model = LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this);
        		LocalUserSetting.saveReading_Night_Model(ReadOverlayActivity.this, !night_model);
                Intent intent = new Intent(ACTION_READSTYLE_CHANGE);
                LocalBroadcastManager.getInstance(ReadOverlayActivity.this).sendBroadcast(intent);
				setupNightMode();
        	}
        	
        });
       
        
        isBackProgress = getIntent().getBooleanExtra(IsBackProgressKey, false);
        progressBackForward = findViewById(R.id.progress_back_forward);
        progressBackForwardImage = (ImageView) findViewById(R.id.progress_back_forward_image);
        progressBackForward.setEnabled(isBackProgress);
        progressBackForward.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isBackProgress) {
					Intent intent = new Intent(ACTION_GO_BACK_PROGRESS);
	                LocalBroadcastManager.getInstance(ReadOverlayActivity.this).sendBroadcast(intent);
				} else {
					Intent intent = new Intent(ACTION_GO_FORWARD_PROGRESS);
	                LocalBroadcastManager.getInstance(ReadOverlayActivity.this).sendBroadcast(intent);
				}
				isBackProgress = !isBackProgress;
				setupBackForward(isBackProgress);
			}
		});
        readSeekBar = (SeekBar) findViewById(R.id.readSeek);
        pageProgress = (ProgressBar) findViewById(R.id.pageProgress);
        pageIndicator = findViewById(R.id.pageIndicator);
        chapterNameView = (TextView) findViewById(R.id.chapterName);
        pageNumber = (TextView) findViewById(R.id.pageNumber);
        
        pageCount = 0;
        chapterPageIndexList = getIntent().getParcelableArrayListExtra(ChapterPageIndexListKey);
        if (chapterPageIndexList != null) {
        	localProgress = getIntent().getIntExtra(CurrentPageIndexKey, 0);
            for (ChapterPageIndex index : chapterPageIndexList) {
                pageCount += (index.pageEnd - index.pageStart);
            }
            readSeekBar.setEnabled(true);
            readSeekBar.setMax(pageCount - 1);
            readSeekBar.setProgress(localProgress);
            pageProgress.setVisibility(View.GONE);
        } else {
        	localProgress = 0;
            readSeekBar.setEnabled(false);
            pageProgress.setVisibility(View.VISIBLE);
        }
        readSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
            	localProgress = progress;
				if (chapterPageIndexList != null) {
					for (ChapterPageIndex index : chapterPageIndexList) {
						if (progress < index.pageEnd) {
							chapterNameView.setText(index.title);
							pageNumber.setText((progress + 1) + "/"
									+ (pageCount));
							break;
						}
					}
				}
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            	pageIndicator.setVisibility(View.VISIBLE);
            	nightModeImage.setVisibility(View.GONE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            	readSeekBar.setEnabled(false);
            	setupReadProgress(localProgress);
            	pageIndicator.setVisibility(View.GONE);
            	nightModeImage.setVisibility(View.VISIBLE);
                int page = seekBar.getProgress();
                Intent intent = new Intent(ACTION_PAGENUMBER_CHANGE);
                intent.putExtra(BookPageViewActivity.ChangePageKey, page);
                LocalBroadcastManager.getInstance(ReadOverlayActivity.this).sendBroadcast(intent);
            }
            
        });
        initView();
        setupNightMode();
        registerReceiver();
        
        EventBus.getDefault().register(this);
    }
    
    
    private void initView(){
    	bg_color = LocalUserSetting.getReading_Background_Color(ReadOverlayActivity.this);
    	if (bg_color == 0xFFF2F2F2) {
    		setupWhiteStyle();
		}else if (bg_color == 0xFFFAF5ED) {
		   	setupSoftStyle();
		}else if (bg_color == 0xFFCEEBCE) {
			setupMintStyle();
		}
//		else if(bg_color == 0xFFFFFFFF){
//	    	setupAllWhiteStyle();
//		}else if (bg_color == 0xFFF7E7DE) {
//	    	setupBeigeStyle();
//		}else if (bg_color == 0xFFF7E3D6) {
//	    	setupWhitePaperStyle();
//		}else if (bg_color == 0xFFFFE7EF) {
//	    	setupPinkPaperStyle();
//		}else if (bg_color == 0xFFDEC79C) {
//			setupPaperYellowStyle();
//		}else if (bg_color == 0xFF08598C) {
//		  	setupBlueStyle();
//		}else if (bg_color == 0xFF104139) {
//	    	setupGreyStyle();
//		}else if (bg_color == 0xFF21495A) {
//		   	setupCaramelStyle();
//		}else if (bg_color == 0xFF212421) {
//		 	setupSepiaamelStyle();
//		}
    }
    
    private void setupBrightness(int brightnessProgress) {
    	if (brightnessProgress <= 0) {
    		if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
    			brightnessSeek.setThumb(getResources().getDrawable(R.drawable.progress_tune_zero_night));
    		} else {
    			brightnessSeek.setThumb(getResources().getDrawable(R.drawable.progress_tune_zero_standard));
    		}
    	} else {
    		if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
    			brightnessSeek.setThumb(getResources().getDrawable(R.drawable.progress_tune_night));
    		} else {
    			brightnessSeek.setThumb(getResources().getDrawable(R.drawable.progress_tune_standard));
    		}
    	}
    }
    
    private void setupReadProgress(int localProgress) {
    	if (localProgress <= 0) {
    		if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
    			readSeekBar.setThumb(getResources().getDrawable(R.drawable.progress_tune_zero_night));
    		} else {
    			readSeekBar.setThumb(getResources().getDrawable(R.drawable.progress_tune_zero_standard));
    		}
    	} else {
    		if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
    			readSeekBar.setThumb(getResources().getDrawable(R.drawable.progress_tune_night));
    		} else {
    			readSeekBar.setThumb(getResources().getDrawable(R.drawable.progress_tune_standard));
    		}
    	}
    }
    
    private void setupBackForward(boolean isBackProgress) {
		if (isBackProgress) {
			if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
				progressBackForwardImage.setImageResource(R.drawable.reader_icon_progress_back_night);
			} else {
				progressBackForwardImage.setImageResource(R.drawable.reader_icon_progress_back_standard);
			}
		} else {
			if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
				progressBackForwardImage.setImageResource(R.drawable.reader_icon_progress_forward_night);
			} else {
				progressBackForwardImage.setImageResource(R.drawable.reader_icon_progress_forward_standard);
			}
		}
    }
    
	private void setupBookMarkImage() {
		if (isMarked) {
			if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
				markImage.setImageResource(R.drawable.reader_btn_bookmark_marked_night);
			} else {
				markImage.setImageResource(R.drawable.reader_btn_bookmark_marked_standard);
			}
		} else {
			if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
				markImage.setImageResource(R.drawable.reader_btn_bookmark_unmark_night);
			} else {
				markImage.setImageResource(R.drawable.reader_btn_bookmark_unmark_standard);
			}
		}
	}
	
	private void setupNightMode() {
		setupSwitchImage();
		setupBookMarkImage();
		setupTraditionalUI();
		setupBackForward(isBackProgress);
		setupReadProgress(localProgress);
		setupBrightness(brightnessProgress);
		setupPageEdgeSpaceStatus(pageEdgeSpaceLevel);
		if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
			topContainer.setBackgroundColor(getResources().getColor(R.color.n_bg_main));
			findViewById(R.id.bottom_container).setBackgroundColor(getResources().getColor(R.color.n_bg_main));
			readSettingPanel.setBackgroundColor(getResources().getColor(R.color.n_bg_main));
			adjustRootLayout.setBackgroundColor(getResources().getColor(R.color.n_bg_main));
			settingMoreView.setBackgroundColor(getResources().getColor(R.color.n_bg_main));
			modifyTextView.setBackgroundColor(getResources().getColor(R.color.n_bg_main));
//			select_colors.setBackgroundColor(getResources().getColor(R.color.n_bg_main));
			tocView.setBackgroundResource(R.drawable.selector_bg_night);
			readNoteView.setBackgroundResource(R.drawable.selector_bg_night);
			readSettingView.setBackgroundResource(R.drawable.selector_bg_night);
			adjust_layout.setBackgroundResource(R.drawable.selector_bg_night);
			custom_colors.setBackgroundResource(R.drawable.read_normal_bg_night);
			readSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bg_night));
			brightnessSeek.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bg_night));
			playListImg.setImageResource(R.drawable.reader_btn_audio_night);
			fontSize1.setBackgroundResource(R.drawable.selector_bg_night);
			fontSize2.setBackgroundResource(R.drawable.selector_bg_night);
			fontTraditional.setBackgroundResource(R.drawable.selector_bg_night);
			fontSizeImage1.setImageResource(R.drawable.reader_btn_fontsize_down_night);
			fontSizeImage2.setImageResource(R.drawable.reader_btn_fontsize_up_night);
			((ImageView)tocView.findViewById(R.id.navTocImg)).setImageResource(R.drawable.reader_btn_toc_night);
			((TextView)tocView.findViewById(R.id.navTocText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((ImageView)searchBookView.findViewById(R.id.searchBookImg)).setImageResource(R.drawable.reader_btn_search_night);
			((ImageView)readBackView.findViewById(R.id.navBackImg)).setImageResource(R.drawable.reader_btn_back_night);
			((ImageView)readSettingView.findViewById(R.id.navReadSettingImg)).setImageResource(R.drawable.reader_btn_setting_night);
			((TextView)readSettingView.findViewById(R.id.navReadSettingText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((ImageView)readNoteView.findViewById(R.id.navReadNoteImg)).setImageResource(R.drawable.reader_btn_note_night);
			((TextView)readNoteView.findViewById(R.id.navReadNoteText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)settingMoreView.findViewById(R.id.settingMoreText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)modifyTextView.findViewById(R.id.modifyText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((ImageView)findViewById(R.id.imageView2)).setImageResource(R.drawable.reader_icon_brightness_down_night);
			((ImageView)findViewById(R.id.imageView3)).setImageResource(R.drawable.reader_icon_brightness_up_night);
			((TextView)readSettingWhite.findViewById(R.id.readSettingWhiteText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)readSettingSoft.findViewById(R.id.readSettingSoftText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)readSettingMint.findViewById(R.id.readSettingMintText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)readSettingMore.findViewById(R.id.readSettingMoreText)).setTextColor(getResources().getColor(R.color.n_text_main));
			((TextView)adjust_layout.findViewById(R.id.adjust_text)).setTextColor(getResources().getColor(R.color.n_text_main));
			moreView.setBackgroundResource(R.drawable.reader_btn_morecolor_night);
			nightModeImage.setImageResource(R.drawable.reader_btn_theme_daylight_standard);
			findViewById(R.id.space1).setBackgroundColor(getResources().getColor(R.color.n_bg_sub));
			findViewById(R.id.space2).setBackgroundColor(getResources().getColor(R.color.n_bg_sub));
			findViewById(R.id.divider1).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			findViewById(R.id.divider2).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			findViewById(R.id.divider3).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			findViewById(R.id.divider4).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			findViewById(R.id.divider5).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			findViewById(R.id.divider6).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			findViewById(R.id.divider7).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			findViewById(R.id.divider8).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			findViewById(R.id.divider9).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			
			((TextView)custom_colors.findViewById(R.id.custom_colors_text)).setTextColor(getResources().getColor(R.color.n_text_main));
//			select_colors.findViewById(R.id.color_divider1).setBackgroundColor(getResources().getColor(R.color.n_hariline));
//			select_colors.findViewById(R.id.color_divider2).setBackgroundColor(getResources().getColor(R.color.n_hariline));
//			select_colors.findViewById(R.id.color_space1).setBackgroundColor(getResources().getColor(R.color.n_bg_sub));
			findViewById(R.id.color_divider2).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			findViewById(R.id.dot_area).setBackgroundColor(getResources().getColor(R.color.n_bg_main));
			
			largeWidth.setBackgroundResource(R.drawable.selector_bg_night);
	    	smallWidth.setBackgroundResource(R.drawable.selector_bg_night);
	    	mediumWidth.setBackgroundResource(R.drawable.selector_bg_night);
	    	lineSpaceUp.setBackgroundResource(R.drawable.selector_bg_night);
	    	lineSpaceDown.setBackgroundResource(R.drawable.selector_bg_night);
	    	blockSpaceUp.setBackgroundResource(R.drawable.selector_bg_night);
	    	blockSpaceDown.setBackgroundResource(R.drawable.selector_bg_night);
			((ImageView)lineSpaceUp.findViewById(R.id.lineSpaceUpImg)).setImageResource(R.drawable.reader_btn_lineheight_up_night);
			((ImageView)lineSpaceDown.findViewById(R.id.lineSpaceDownImg)).setImageResource(R.drawable.reader_btn_lineheight_down_night);
			((ImageView)blockSpaceUp.findViewById(R.id.blockSpaceUpImg)).setImageResource(R.drawable.reader_btn_paramagin_up_night);
			((ImageView)blockSpaceDown.findViewById(R.id.blockSpaceDownImg)).setImageResource(R.drawable.reader_btn_paramagin_down_night);
			adjustRootLayout.findViewById(R.id.adjust_divider1).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			adjustRootLayout.findViewById(R.id.adjust_divider2).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			adjustRootLayout.findViewById(R.id.adjust_divider3).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			adjustRootLayout.findViewById(R.id.adjust_divider4).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			adjustRootLayout.findViewById(R.id.adjust_divider5).setBackgroundColor(getResources().getColor(R.color.n_hariline));
			adjustRootLayout.findViewById(R.id.adjust_space1).setBackgroundColor(getResources().getColor(R.color.n_bg_sub));

		} else {
			topContainer.setBackgroundColor(getResources().getColor(R.color.r_bg_main));
			findViewById(R.id.bottom_container).setBackgroundColor(getResources().getColor(R.color.r_bg_main));
			readSettingPanel.setBackgroundColor(getResources().getColor(R.color.r_bg_main));
			adjustRootLayout.setBackgroundColor(getResources().getColor(R.color.r_bg_main));
			settingMoreView.setBackgroundColor(getResources().getColor(R.color.r_bg_main));
			modifyTextView.setBackgroundColor(getResources().getColor(R.color.r_bg_main));
//			select_colors.setBackgroundColor(getResources().getColor(R.color.r_bg_main));
			tocView.setBackgroundResource(R.drawable.selector_bg_standard);
			readNoteView.setBackgroundResource(R.drawable.selector_bg_standard);
			readSettingView.setBackgroundResource(R.drawable.selector_bg_standard);
			adjust_layout.setBackgroundResource(R.drawable.selector_bg_standard);
			custom_colors.setBackgroundResource(R.drawable.read_normal_bg_standard);
			readSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bg_standard));
			brightnessSeek.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bg_standard));
			playListImg.setImageResource(R.drawable.reader_btn_audio_standard);
			fontSize1.setBackgroundResource(R.drawable.selector_bg_standard);
			fontSize2.setBackgroundResource(R.drawable.selector_bg_standard);
			fontTraditional.setBackgroundResource(R.drawable.selector_bg_standard);
			fontSizeImage1.setImageResource(R.drawable.reader_btn_fontsize_down_standard);
			fontSizeImage2.setImageResource(R.drawable.reader_btn_fontsize_up_standard);
			if (textSizeLevel == FONT_SIZE_MIN_LEVEL) {
				fontSizeImage1.setImageResource(R.drawable.reader_btn_fontsize_down_night);
			}
			if (textSizeLevel == FONT_SIZE_MAX_LEVEL) {
				fontSizeImage2.setImageResource(R.drawable.reader_btn_fontsize_up_night);
			}
			((ImageView)tocView.findViewById(R.id.navTocImg)).setImageResource(R.drawable.reader_btn_toc_night);
			((TextView)tocView.findViewById(R.id.navTocText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((ImageView)searchBookView.findViewById(R.id.searchBookImg)).setImageResource(R.drawable.reader_btn_search_standard);
			((ImageView)readBackView.findViewById(R.id.navBackImg)).setImageResource(R.drawable.reader_btn_back_standard);
			((ImageView)readSettingView.findViewById(R.id.navReadSettingImg)).setImageResource(R.drawable.reader_btn_setting_standard);
			((TextView)readSettingView.findViewById(R.id.navReadSettingText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((ImageView)readNoteView.findViewById(R.id.navReadNoteImg)).setImageResource(R.drawable.reader_btn_note_standard);
			((TextView)readNoteView.findViewById(R.id.navReadNoteText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)settingMoreView.findViewById(R.id.settingMoreText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)modifyTextView.findViewById(R.id.modifyText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((ImageView)findViewById(R.id.imageView2)).setImageResource(R.drawable.reader_icon_brightness_down_standard);
			((ImageView)findViewById(R.id.imageView3)).setImageResource(R.drawable.reader_icon_brightness_up_standard);
			((TextView)readSettingWhite.findViewById(R.id.readSettingWhiteText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)readSettingSoft.findViewById(R.id.readSettingSoftText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)readSettingMint.findViewById(R.id.readSettingMintText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)readSettingMore.findViewById(R.id.readSettingMoreText)).setTextColor(getResources().getColor(R.color.r_text_main));
			((TextView)adjust_layout.findViewById(R.id.adjust_text)).setTextColor(getResources().getColor(R.color.r_text_main));
			moreView.setBackgroundResource(R.drawable.reader_btn_morecolor_standard);
			nightModeImage.setImageResource(R.drawable.reader_btn_theme_night_standard);
			findViewById(R.id.space1).setBackgroundColor(getResources().getColor(R.color.r_bg_sub));
			findViewById(R.id.space2).setBackgroundColor(getResources().getColor(R.color.r_bg_sub));
			findViewById(R.id.divider1).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			findViewById(R.id.divider2).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			findViewById(R.id.divider3).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			findViewById(R.id.divider4).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			findViewById(R.id.divider5).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			findViewById(R.id.divider6).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			findViewById(R.id.divider7).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			findViewById(R.id.divider8).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			findViewById(R.id.divider9).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			
			((TextView)custom_colors.findViewById(R.id.custom_colors_text)).setTextColor(getResources().getColor(R.color.r_text_main));
			findViewById(R.id.color_divider2).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			findViewById(R.id.dot_area).setBackgroundColor(getResources().getColor(R.color.r_bg_main));
			
			largeWidth.setBackgroundResource(R.drawable.selector_bg_standard);
	    	smallWidth.setBackgroundResource(R.drawable.selector_bg_standard);
	    	mediumWidth.setBackgroundResource(R.drawable.selector_bg_standard);
	    	lineSpaceUp.setBackgroundResource(R.drawable.selector_bg_standard);
	    	lineSpaceDown.setBackgroundResource(R.drawable.selector_bg_standard);
	    	blockSpaceUp.setBackgroundResource(R.drawable.selector_bg_standard);
	    	blockSpaceDown.setBackgroundResource(R.drawable.selector_bg_standard);
			((ImageView)lineSpaceUp.findViewById(R.id.lineSpaceUpImg)).setImageResource(R.drawable.reader_btn_lineheight_up_standard);
			((ImageView)lineSpaceDown.findViewById(R.id.lineSpaceDownImg)).setImageResource(R.drawable.reader_btn_lineheight_down_standard);
			((ImageView)blockSpaceUp.findViewById(R.id.blockSpaceUpImg)).setImageResource(R.drawable.reader_btn_paramagin_up_standard);
			((ImageView)blockSpaceDown.findViewById(R.id.blockSpaceDownImg)).setImageResource(R.drawable.reader_btn_paramagin_down_standard);
			adjustRootLayout.findViewById(R.id.adjust_divider1).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			adjustRootLayout.findViewById(R.id.adjust_divider2).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			adjustRootLayout.findViewById(R.id.adjust_divider3).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			adjustRootLayout.findViewById(R.id.adjust_divider4).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			adjustRootLayout.findViewById(R.id.adjust_divider5).setBackgroundColor(getResources().getColor(R.color.r_hariline));
			adjustRootLayout.findViewById(R.id.adjust_space1).setBackgroundColor(getResources().getColor(R.color.r_bg_sub));
		}
	}
	
	private void setupSwitchImage() {
		if (isSyncBrightness) {
			switchOnDot.setVisibility(View.VISIBLE);
			switchOffDot.setVisibility(View.GONE);
			if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
				switchOnDot.setImageResource(R.drawable.switchon_dot_night);
				switchLine.setImageResource(R.drawable.switchon_line_night);
			} else {
				switchOnDot.setImageResource(R.drawable.switchon_dot_standard);
				switchLine.setImageResource(R.drawable.switchon_line_standard);
			}
		} else {
			switchOnDot.setVisibility(View.GONE);
			switchOffDot.setVisibility(View.VISIBLE);
			if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
				switchOffDot.setImageResource(R.drawable.switchoff_dot_night);
				switchLine.setImageResource(R.drawable.switchoff_line_night);
			} else {
				switchOffDot.setImageResource(R.drawable.switchoff_dot_standard);
				switchLine.setImageResource(R.drawable.switchoff_line_standard);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yuedu_setting));
	}
	    
    @Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yuedu_setting));
		int userFont = LocalUserSetting.getTextSizeLevel(this);
		if (userFont != textSizeLevel) {
			LocalUserSetting.saveTextSizeLevel(this, textSizeLevel);
			Intent intent = new Intent(ACTION_READFONT_CHANGE_DONE);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		}
		int lineSpace = LocalUserSetting.getLineSpaceLevel(this);
		int blockSpace = LocalUserSetting.getBlockSpaceLevel(this);
		int pageEdge = LocalUserSetting.getPageEdgeSpaceLevel(this);
		if (lineSpace != lineSpaceLevel || blockSpace != blockSpaceLevel
				|| pageEdge != pageEdgeSpaceLevel) {
			Intent intent = new Intent(ACTION_READ_SPACE_DONE);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		}
	}
    
    private void changeNightModel(){
		LocalUserSetting.saveReading_Night_Model(ReadOverlayActivity.this, false);
		setupNightMode();
    }
    
    private void changeSettingFontSize() {
    	fontSize1.setEnabled(false);
    	fontSize2.setEnabled(false);
    	Intent intent = new Intent(ACTION_SETTING_FONT_CHANGE);
        intent.putExtra(PageFontSizeKey, textSizeLevel);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private void changeSimplifiedToTraditional() {
    	int userFont = LocalUserSetting.getTextSizeLevel(this);
		if (userFont != textSizeLevel) {
			LocalUserSetting.saveTextSizeLevel(this, textSizeLevel);
		}
    	Intent intent = new Intent(ACTION_SIMPLIFIED_TO_TRADITIONAL);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
	private void setupTraditionalUI() {
		ImageView image = (ImageView) fontTraditional.findViewById(R.id.fontTraditionalImage);
		if (isTraditional) {
			if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
				image.setImageResource(R.drawable.reader_btn_font_unsimplified);
			} else {
				image.setImageResource(R.drawable.reader_btn_font_unsimplified);
			}
		} else {
			if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
				image.setImageResource(R.drawable.reader_btn_font_simplified);
			} else {
				image.setImageResource(R.drawable.reader_btn_font_simplified);
			}
		}
	}
    
    private void changeSettingPageSpace() {
    	largeWidth.setEnabled(false);
    	smallWidth.setEnabled(false);
    	mediumWidth.setEnabled(false);
    	lineSpaceUp.setEnabled(false);
    	lineSpaceDown.setEnabled(false);
    	blockSpaceUp.setEnabled(false);
    	blockSpaceDown.setEnabled(false);
    	Intent intent = new Intent(ACTION_CHANGE_READ_SPACE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
	private void setupPageEdgeSpaceStatus(int spaceLevel) {
		ImageView smallImage = (ImageView) smallWidth.findViewById(R.id.smallImage);
		TextView smallText = (TextView) smallWidth.findViewById(R.id.smallText);
		ImageView mediumImage = (ImageView) mediumWidth.findViewById(R.id.mediumImage);
		TextView mediumText = (TextView) mediumWidth.findViewById(R.id.mediumText);
		ImageView largeImage = (ImageView) largeWidth.findViewById(R.id.largeImage);
		TextView largeText = (TextView) largeWidth.findViewById(R.id.largeText);
		
		if (LocalUserSetting.getReading_Night_Model(ReadOverlayActivity.this)) {
			smallImage.setImageResource(R.drawable.reader_btn_textblock_small_night);
			smallText.setTextColor(getResources().getColor(R.color.n_text_main));
			mediumImage.setImageResource(R.drawable.reader_btn_textblock_medium_night);
			mediumText.setTextColor(getResources().getColor(R.color.n_text_main));
			largeImage.setImageResource(R.drawable.reader_btn_textblock_large_night);
			largeText.setTextColor(getResources().getColor(R.color.n_text_main));
			
			switch (spaceLevel) {
			case 0:
				smallImage.setImageResource(R.drawable.reader_btn_textblock_small_hl_night);
				smallText.setTextColor(getResources().getColor(R.color.r_theme));
				break;
			case 1:
				mediumImage.setImageResource(R.drawable.reader_btn_textblock_medium_hl_night);
				mediumText.setTextColor(getResources().getColor(R.color.r_theme));
				break;
			case 2:
				largeImage.setImageResource(R.drawable.reader_btn_textblock_large_hl_night);
				largeText.setTextColor(getResources().getColor(R.color.r_theme));
				break;
			default:
				break;
			}
		} else {
			smallImage.setImageResource(R.drawable.reader_btn_textblock_small_standard);
			smallText.setTextColor(getResources().getColor(R.color.r_text_main));
			mediumImage.setImageResource(R.drawable.reader_btn_textblock_medium_standard);
			mediumText.setTextColor(getResources().getColor(R.color.r_text_main));
			largeImage.setImageResource(R.drawable.reader_btn_textblock_large_standard);
			largeText.setTextColor(getResources().getColor(R.color.r_text_main));
			
			switch (spaceLevel) {
			case 0:
				smallImage.setImageResource(R.drawable.reader_btn_textblock_small_hl_standard);
				smallText.setTextColor(getResources().getColor(R.color.r_theme));
				break;
			case 1:
				mediumImage.setImageResource(R.drawable.reader_btn_textblock_medium_hl_standard);
				mediumText.setTextColor(getResources().getColor(R.color.r_theme));
				break;
			case 2:
				largeImage.setImageResource(R.drawable.reader_btn_textblock_large_hl_standard);
				largeText.setTextColor(getResources().getColor(R.color.r_theme));
				break;
			default:
				break;
			}
		}
	}
    
    private void setupWhiteStyle() {
    	moreView.setBackgroundResource(R.drawable.reader_btn_morecolor_standard);
    	readSettingWhite.findViewById(R.id.whiteTheme).setBackgroundResource(R.drawable.read_white_theme_highlight);
        readSettingSoft.findViewById(R.id.softTheme).setBackgroundResource(R.drawable.read_soft_theme);
        readSettingMint.findViewById(R.id.mintTheme).setBackgroundResource(R.drawable.read_mint_theme);
    }
    
    private void setupSoftStyle() {
    	moreView.setBackgroundResource(R.drawable.reader_btn_morecolor_soft);
        readSettingWhite.findViewById(R.id.whiteTheme).setBackgroundResource(R.drawable.read_white_theme);
        readSettingSoft.findViewById(R.id.softTheme).setBackgroundResource(R.drawable.read_soft_theme_highlight);
        readSettingMint.findViewById(R.id.mintTheme).setBackgroundResource(R.drawable.read_mint_theme);
    }
    
    private void setupMintStyle() {
    	moreView.setBackgroundResource(R.drawable.reader_btn_morecolor_mint);
    	readSettingWhite.findViewById(R.id.whiteTheme).setBackgroundResource(R.drawable.read_white_theme);
        readSettingSoft.findViewById(R.id.softTheme).setBackgroundResource(R.drawable.read_soft_theme);
        readSettingMint.findViewById(R.id.mintTheme).setBackgroundResource(R.drawable.read_mint_theme_highlight);
    }
    
    private void loadWhiteStyle() {
    	setupWhiteStyle();
        // save color to user setting
        LocalUserSetting.saveReading_Background_Color(this, 0xFFF2F2F2);
        LocalUserSetting.saveReading_Background_Texture(ReadOverlayActivity.this,-1);
        LocalUserSetting.saveReading_Text_Color(this, WHITE_STYLE_FONT);
        LocalUserSetting.saveIgnoreCssTextColor(this, false);
        Intent intent = new Intent(ACTION_READSTYLE_CHANGE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private void loadSoftStyle() {
    	setupSoftStyle();
        
        LocalUserSetting.saveReading_Background_Color(this, 0xFFFAF5ED);
        LocalUserSetting.saveReading_Background_Texture(ReadOverlayActivity.this,-1);
        LocalUserSetting.saveReading_Text_Color(this, SOFT_STYLE_FONT);
        LocalUserSetting.saveIgnoreCssTextColor(this, false);
        Intent intent = new Intent(ACTION_READSTYLE_CHANGE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private void loadMintStyle() {
    	setupMintStyle();
        
        LocalUserSetting.saveReading_Background_Color(this, 0xFFCEEBCE);
        LocalUserSetting.saveReading_Background_Texture(ReadOverlayActivity.this,-1);
        LocalUserSetting.saveReading_Text_Color(this, MINT_STYLE_FONT);
        LocalUserSetting.saveIgnoreCssTextColor(this, false);
        Intent intent = new Intent(ACTION_READSTYLE_CHANGE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
//    private void loadNightStyle() {
//        LocalUserSetting.saveReading_Background_Color(this, 0xFF000000);
//        LocalUserSetting.saveReading_Text_Color(this, NIGHT_STYLE_FONT);
//        Intent intent = new Intent(ACTION_READSTYLE_CHANGE);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//    }
    
    class ReadingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_PAGECOUNT_DONE)) {
				pageCount = intent.getIntExtra(PageCountKey, 0);
				final int currentPosition = intent.getIntExtra(CurrentPageIndexKey, 0);
				chapterPageIndexList = intent.getParcelableArrayListExtra(ChapterPageIndexListKey);
				ReadOverlayActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						readSeekBar.setEnabled(true);
						readSeekBar.setMax(pageCount - 1);
						readSeekBar.setProgress(currentPosition);
						pageProgress.setVisibility(View.GONE);
					}
				});
			} else if (intent.getAction().equals(ACTION_FONT_CHANGE_DONE)) {
				fontSize1.setEnabled(true);
				fontSize2.setEnabled(true);
			} else if (intent.getAction().equals(ACTION_BACK_PROGRESS_DONE)) {
				isBackProgress = true;
				progressBackForward.setEnabled(true);
				isMarked = intent.getBooleanExtra(BookMarkStateKey, false);
				setupBookMarkImage();
			} else if (intent.getAction().equals(ACTION_CHANGE_PROGRESS_DONE)) {
				int pageNumber = intent.getIntExtra(PageNumberKey, 0);
				readSeekBar.setProgress(pageNumber);
				isMarked = intent.getBooleanExtra(BookMarkStateKey, false);
				setupBookMarkImage();
			} else if (intent.getAction().equals(ACTION_PAGENUMBER_CHANGE_DONE)) {
				readSeekBar.setEnabled(true);
				isMarked = intent.getBooleanExtra(BookMarkStateKey, false);
				setupBookMarkImage();
			} else if (intent.getAction().equals(ACTION_CHANGE_READ_SPACE_DONE)) {
				lineSpaceUp.setEnabled(true);
		    	lineSpaceDown.setEnabled(true);
		    	blockSpaceUp.setEnabled(true);
		    	blockSpaceDown.setEnabled(true);
		    	mediumWidth.setEnabled(true);
				smallWidth.setEnabled(true);
				largeWidth.setEnabled(true);
			}
		}
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

	private ReadingReceiver receiver = new ReadingReceiver();

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_PAGECOUNT_DONE);
		filter.addAction(ACTION_FONT_CHANGE_DONE);
		filter.addAction(ACTION_BACK_PROGRESS_DONE);
		filter.addAction(ACTION_CHANGE_PROGRESS_DONE);
		filter.addAction(ACTION_PAGENUMBER_CHANGE_DONE);
		filter.addAction(ACTION_CHANGE_READ_SPACE_DONE);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
	}
	
	private void unregisterReceiver() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		EventBus.getDefault().unregister(this);
	}

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver();
    }

    /**
     * 选择背景色底部的小点移动
     * @param position
     */
    private void dotMoveToPosition(int position){
    	if(position == 0){
    		((ImageView)findViewById(R.id.dot1)).setImageDrawable(getResources().getDrawable(R.drawable.viewpager_dot_selected));
    		((ImageView)findViewById(R.id.dot2)).setImageDrawable(getResources().getDrawable(R.drawable.viewpager_dot_no_select));
    	}else{
    		((ImageView)findViewById(R.id.dot1)).setImageDrawable(getResources().getDrawable(R.drawable.viewpager_dot_no_select));
    		((ImageView)findViewById(R.id.dot2)).setImageDrawable(getResources().getDrawable(R.drawable.viewpager_dot_selected));
    	}
    }
    
    /**
     * 背景色选择的两个view
     *
     */
    public class BookstorePagerAdapter extends FragmentPagerAdapter {

		public BookstorePagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0: {// 第一页
				ReadingBgFirstFragment fragment = new ReadingBgFirstFragment();
				return fragment;
			}
			case 1: {// 第二页
				ReadingBgSecondFragment fragment = new ReadingBgSecondFragment();
				return fragment;
			}
			default:
				return new ReadingBgFirstFragment();
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

	}
    
   
}
