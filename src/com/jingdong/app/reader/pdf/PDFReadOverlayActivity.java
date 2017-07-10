package com.jingdong.app.reader.pdf;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.jingdong.app.reader.R;
//import com.artifex.mupdfdemo.OutlineItem;
//import com.jingdong.app.reader.plugin.pdf.outline.OutlineItem;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.ScreenUtils;

public class PDFReadOverlayActivity extends Activity {
	public static final String ACTION_PAGENUMBER_CHANGE = "com.jingdong.app.reader.pdf.pagenumberchange";
	public static final String ACTION_READFONT_CHANGE_DONE = "com.jingdong.app.reader.pdf.readfontchangedone";
    public static final String ACTION_SETTING_FONT_CHANGE = "com.jingdong.app.reader.pdf.settingfontchange";
    public static final String ACTION_GO_BACK_PROGRESS = "com.jingdong.app.reader.pdf.gobackprogress";
    public static final String ACTION_GO_FORWARD_PROGRESS = "com.jingdong.app.reader.pdf.goforwardprogress";
    public static final String ACTION_BACK_PROGRESS_DONE = "com.jingdong.app.reader.pdf.backprogressdone";
    public static final String ACTION_CHANGE_PROGRESS_DONE = "com.jingdong.app.reader.pdf.changeprogressdone";


	public static final String ShowPurchaseButtonKey = "ShowPurchaseButtonKey";
    public static final String ChapterPageIndexListKey = "ChapterPageIndexListKey";
    public static final String CurrentPageIndexKey = "CurrentPageIndexKey";
    public static final String IsBackProgressKey = "IsBackProgressKey";
    public static final String BookMarkStateKey = "BookMarkStateKey";
    public static final String LandMinZoomKey = "LandMinZoomKey";
    public static final String DocumentIdKey = "DocumentIdKey";
    public static final String PageCountKey = "PageCountKey";
    public static final String ZoomLevelKey = "ZoomLevelKey";
    
    private View readSettingPanel;
    private View pageIndicator;
    private View readSettingMore;
    private View readSettingFont;
    private View bottomContainer;
    private View bottomShadow;
    private View progressBackForward;
    private View modifyBrightnessView;
    private View volumePageView;
    private View verticalPageView;
    private View lockScreenView;
    private ImageView markImage;
    private ImageView progressBackForwardImage;
    private TextView chapterNameView;
    private TextView pageNumber;
    private SeekBar readSeekBar;
    private SeekBar brightnessSeek;
    private ProgressBar pageProgress;
    private PopupWindow popWindow;
    private int pageCount;
    private int lockScreenOrientation = LocalUserSetting.SCREEN_DONT_LOCK;
    private float zoomLevel;
    private float landMinZoom;
    private float systemBrightness;
    private boolean isMarked = false;
    private boolean isBackProgress = false;
    private boolean isSyncBrightness = false;
    private boolean isVolumePageEnabled = false;
    private boolean isVerticalPageEnabled = false;
    private boolean isLockScreenEnabled = false;
    private boolean isLandscape = false;
    private ArrayList<com.jingdong.app.reader.plugin.pdf.outline.OutlineItem> outlineList;
    private ImageView fontSizeImage1,fontSizeImage2;
    TypedValue typedValue1,typedValue2,typedValueFont;
    View fontSize1,fontSize2;
    
    private OnClickListener zoomDownListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
        	if (isLandscape) {
        		if (zoomLevel <= landMinZoom) {
//            		ToastUtil.showToastInThread(getString(R.string.read_zoom_is_min), Toast.LENGTH_SHORT);
                	return;
                }
        	} else {
	        	if (zoomLevel <= PDFBookViewActivity.BOOK_PDF_FONTSIZE_MIN) {
//	        		ToastUtil.showToastInThread(getString(R.string.read_zoom_is_min), Toast.LENGTH_SHORT);
	            	return;
	            }
        	}
        	zoomLevel --;
        	if (isLandscape) {
        		if (zoomLevel < landMinZoom) {
        			zoomLevel = landMinZoom;
        		}
        	} else {
        		if (zoomLevel < PDFBookViewActivity.BOOK_PDF_FONTSIZE_MIN) {
        			zoomLevel = PDFBookViewActivity.BOOK_PDF_FONTSIZE_MIN;
        		}
        	}
        	
        	if(zoomLevel == landMinZoom || zoomLevel == PDFBookViewActivity.BOOK_PDF_FONTSIZE_MIN){
            	fontSizeImage1.setImageResource(R.drawable.reader_btn_fontsize_down_night);
            	fontSize1.setBackgroundResource(R.drawable.read_normal_bg_standard);
            }else{
            	 fontSizeImage1.setImageResource(typedValue1.resourceId);
            	 fontSize1.setBackgroundResource(typedValueFont.resourceId);
            }
            fontSizeImage2.setImageResource(typedValue2.resourceId);
            fontSize2.setBackgroundResource(typedValueFont.resourceId);
        	
            changeSettingFontSize();
        }

    };
    private OnClickListener zoomUpListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
        	if (zoomLevel >= PDFBookViewActivity.BOOK_PDF_FONTSIZE_MAX) {
//        		ToastUtil.showToastInThread(getString(R.string.read_zoom_is_max), Toast.LENGTH_SHORT);
            	return;
            }
        	zoomLevel ++;
        	if (zoomLevel > PDFBookViewActivity.BOOK_PDF_FONTSIZE_MAX) {
        		zoomLevel = PDFBookViewActivity.BOOK_PDF_FONTSIZE_MAX;
        	}
        	if(zoomLevel == PDFBookViewActivity.BOOK_PDF_FONTSIZE_MAX){
            	fontSizeImage2.setImageResource(R.drawable.reader_btn_fontsize_up_night);
            	fontSize2.setBackgroundResource(R.drawable.read_normal_bg_standard);
            }else{
            	fontSizeImage2.setImageResource(typedValue2.resourceId);
            	fontSize2.setBackgroundResource(typedValueFont.resourceId);
            }
            fontSizeImage1.setImageResource(typedValue1.resourceId);
            fontSize1.setBackgroundResource(typedValueFont.resourceId);
            changeSettingFontSize();
        }

    };
    
    private OnSeekBarChangeListener brightnessListener = new OnSeekBarChangeListener() {

    	private int brightnessProgress;
    	
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
                	setupSwitchImage(isSyncBrightness, modifyBrightnessView);
                	LocalUserSetting.saveSyncBrightness(PDFReadOverlayActivity.this, isSyncBrightness);
                }
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        	setupBrightness(seekBar, brightnessProgress);
            LocalUserSetting.saveReadBrightness(PDFReadOverlayActivity.this, systemBrightness);
        }

    };
    
    private OnClickListener syncListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			isSyncBrightness = !isSyncBrightness;
			setupSwitchImage(isSyncBrightness, modifyBrightnessView);
			LocalUserSetting.saveSyncBrightness(
					PDFReadOverlayActivity.this, isSyncBrightness);
			if (isSyncBrightness) {
				int mode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
				try {
					mode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
				} catch (SettingNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
					systemBrightness = -1 / 255f;
				} else {
					int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
					systemBrightness = brightness / 255.0f;
				}
			} else {
				systemBrightness = LocalUserSetting.getReadBrightness(PDFReadOverlayActivity.this);
			}
			WindowManager.LayoutParams layout = getWindow().getAttributes();
            layout.screenBrightness = systemBrightness;
            getWindow().setAttributes(layout);
            int brightnessProgress = (int) (systemBrightness * 100);
            brightnessSeek.setProgress(brightnessProgress);
		}
	};
	
	private OnClickListener volumeListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			isVolumePageEnabled = !isVolumePageEnabled;
			setupSwitchImage(isVolumePageEnabled, volumePageView);
			LocalUserSetting.saveVolumePage(PDFReadOverlayActivity.this,
					isVolumePageEnabled);
		}
	};
	
	private OnClickListener verticalListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			isVerticalPageEnabled = !isVerticalPageEnabled;
			setupSwitchImage(isVerticalPageEnabled, verticalPageView);
			LocalUserSetting.saveVerticalPage(PDFReadOverlayActivity.this,
					isVerticalPageEnabled);
			Intent intent = new Intent(PDFBookViewActivity.ACTION_PDF_PAGE_TURNING_MODE);
		    LocalBroadcastManager.getInstance(PDFReadOverlayActivity.this).sendBroadcast(intent);
		}
	};
	
	private OnClickListener lockScreenListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			isLockScreenEnabled = !isLockScreenEnabled;
			setupLockImage(isLockScreenEnabled, lockScreenView);
			if (isLockScreenEnabled) {
				if (isLandscape) {
					lockScreenOrientation = LocalUserSetting.SCREEN_LANDSCAPE;
				} else {
					lockScreenOrientation = LocalUserSetting.SCREEN_PORTRAIT;
				}
			} else {
				lockScreenOrientation = LocalUserSetting.SCREEN_DONT_LOCK;
			}
			LocalUserSetting.saveLockScreenOrientation(PDFReadOverlayActivity.this,
					lockScreenOrientation);
			int rotation = Surface.ROTATION_0;
			if (lockScreenOrientation != LocalUserSetting.SCREEN_DONT_LOCK) {
				rotation = getWindowManager().getDefaultDisplay().getRotation();
			}
			LocalUserSetting.saveDisplayRotation(PDFReadOverlayActivity.this, rotation);
			Intent intent = new Intent(PDFBookViewActivity.ACTION_PDF_LOCK_SCREEN_MODE);
		    LocalBroadcastManager.getInstance(PDFReadOverlayActivity.this).sendBroadcast(intent);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTheme(R.style.WhiteTheme);
		registerReceiver();
		setContentView(R.layout.activity_read_overlay_pdf);
		landMinZoom = getIntent().getFloatExtra(LandMinZoomKey, 1.0f);
		Display d = getWindowManager().getDefaultDisplay();
		Point s = new Point();
		d.getSize(s);
		if (s.x > s.y) {
			isLandscape = true;
		}
		bottomContainer = findViewById(R.id.bottom_container);
		bottomShadow = findViewById(R.id.bottom_shadow);
		View topContainer = findViewById(R.id.top_container);
        topContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// do nothing
			}
		});
		View tocView = findViewById(R.id.navToc);
        tocView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	setResult(PDFBookViewActivity.RESULT_OPEN_TOC);
            	finish();
            }

        });
        
        View readBackView = findViewById(R.id.navBack);
        readBackView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	setResult(PDFBookViewActivity.RESULT_VIEW_BACK);
            	finish();
            }

        });
        
        isMarked = getIntent().getBooleanExtra(BookMarkStateKey, false);
        View markBookView = findViewById(R.id.markBook);
        markImage = (ImageView) markBookView.findViewById(R.id.markBookImage);
        setupBookMarkImage();
        markBookView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	isMarked = !isMarked;
            	setupBookMarkImage();
            	setResult(PDFBookViewActivity.RESULT_TOGGLE_BOOK_MARK);
            	finish();
            }

        });
        
        View searchBookView = findViewById(R.id.searchBook);
		searchBookView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(PDFBookViewActivity.RESULT_SEARCH_BOOK);
            	finish();
			}

		});
		
		boolean showPurchaseButton = getIntent().getBooleanExtra(ShowPurchaseButtonKey, false);
        View purchaseFullBook = findViewById(R.id.navPurchase);
        if (showPurchaseButton) {
            purchaseFullBook.setVisibility(View.VISIBLE);
            purchaseFullBook.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	setResult(PDFBookViewActivity.RESULT_PURCHASE_FULL_BOOK);
                	finish();
                }

            });
        } else {
            purchaseFullBook.setVisibility(View.GONE);
        }
        
        View backGroundView = findViewById(R.id.readOverlayBg);
        backGroundView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	finish();
            }

        });
        
        readSettingFont = findViewById(R.id.readSettingFont);
        View readZoomView = findViewById(R.id.navReadZoom);
		readZoomView.setVisibility(View.GONE);
        readZoomView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isLandscape) {
					createSettingFontPop(v);
					return;
				}
				readSettingPanel.setVisibility(View.VISIBLE);
				readSettingFont.setVisibility(View.VISIBLE);
				readSettingMore.setVisibility(View.GONE);
				bottomContainer.setVisibility(View.GONE);
				pageIndicator.setVisibility(View.GONE);
				bottomShadow.setVisibility(View.GONE);
			}
		});
        
        readSettingMore = findViewById(R.id.readSettingMore);
        View settingMoreView = findViewById(R.id.navSettingMore);
		settingMoreView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isLandscape) {
					createSettingMorePop(v);
					return;
				} else {
					setupBrightnessSeekBar();
				}
				readSettingPanel.setVisibility(View.VISIBLE);
				readSettingMore.setVisibility(View.VISIBLE);
				readSettingFont.setVisibility(View.GONE);
				bottomContainer.setVisibility(View.GONE);
				pageIndicator.setVisibility(View.GONE);
				bottomShadow.setVisibility(View.GONE);
			}
		});
        
        
        readSettingPanel = findViewById(R.id.readSetting);
		pageCount = getIntent().getIntExtra(PageCountKey, 0);
		int currentPosition = getIntent().getIntExtra(CurrentPageIndexKey, 0);
		outlineList = getIntent().getParcelableArrayListExtra(ChapterPageIndexListKey);
		
		
        readSeekBar = (SeekBar) findViewById(R.id.readSeek);
        pageProgress = (ProgressBar) findViewById(R.id.pageProgress);
        pageIndicator = findViewById(R.id.pageIndicator);
        chapterNameView = (TextView) findViewById(R.id.chapterName);
        pageNumber = (TextView) findViewById(R.id.pageNumber);
        readSeekBar.setEnabled(true);
        readSeekBar.setMax(pageCount - 1);
        readSeekBar.setProgress(currentPosition);
        setupReadProgress(currentPosition);
        pageProgress.setVisibility(View.GONE);
		if (outlineList != null) {
			for (int i = 0; i < outlineList.size(); i++) {
				if (currentPosition >= outlineList.get(i).page) {
					String chapterName = outlineList.get(i).title;
					chapterNameView.setText(chapterName);
				} else {
					break;
				}
			}
			pageNumber.setText((currentPosition + 1) + "/" + (pageCount));
		} else {
			chapterNameView.setText((currentPosition + 1) + "/" + (pageCount));
			pageNumber.setVisibility(View.GONE);
		}
        readSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
				if (outlineList != null) {
					String chapterName = "";
					for (int i = 0; i < outlineList.size(); i++) {
						if (progress >= outlineList.get(i).page) {
							chapterName = outlineList.get(i).title;
						} else {
							break;
						}
					}
					chapterNameView.setText(chapterName);
					pageNumber.setText((progress + 1) + "/" + (pageCount));
				} else {
					chapterNameView.setText((progress + 1) + "/" + (pageCount));
				}
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int page = seekBar.getProgress();
                setupReadProgress(page);
                Intent intent = new Intent(ACTION_PAGENUMBER_CHANGE);
                intent.putExtra(PDFBookViewActivity.ChangePageKey, page);
                LocalBroadcastManager.getInstance(PDFReadOverlayActivity.this).sendBroadcast(intent);
            }
            
        });
        
        zoomLevel = getIntent().getFloatExtra(ZoomLevelKey, PDFBookViewActivity.BOOK_PDF_FONTSIZE_MIN);
        fontSizeImage1 = (ImageView) findViewById(R.id.fontSizeImage1);
        fontSizeImage2 = (ImageView) findViewById(R.id.fontSizeImage2);
        
        fontSize1 = findViewById(R.id.fontSize1);
        fontSize1.setOnClickListener(zoomDownListener);
        fontSize2 = findViewById(R.id.fontSize2);
        fontSize2.setOnClickListener(zoomUpListener);
        
        if(zoomLevel <= landMinZoom || zoomLevel <= PDFBookViewActivity.BOOK_PDF_FONTSIZE_MIN){
        	fontSizeImage1.setImageResource(R.drawable.reader_btn_fontsize_down_night);
        	fontSize1.setBackgroundResource(R.drawable.read_normal_bg_standard);
        }else if(zoomLevel >= PDFBookViewActivity.BOOK_PDF_FONTSIZE_MAX){
        	fontSizeImage2.setImageResource(R.drawable.reader_btn_fontsize_up_night);
        	fontSize2.setBackgroundResource(R.drawable.read_normal_bg_standard);
        }
        
        typedValue1 = new TypedValue();
        getTheme().resolveAttribute(R.attr.read_fontsize_down_img, typedValue1, true);
        typedValue2 = new TypedValue();
        getTheme().resolveAttribute(R.attr.read_fontsize_up_img, typedValue2, true);
        
        typedValueFont = new TypedValue();
        getTheme().resolveAttribute(R.attr.read_pressed_bg, typedValueFont, true);
        
        
        isSyncBrightness = LocalUserSetting.isSyncBrightness(this);
        modifyBrightnessView = findViewById(R.id.modifySystemLight);
        setupSwitchImage(isSyncBrightness, modifyBrightnessView);
        modifyBrightnessView.setOnClickListener(syncListener);
        
        isVolumePageEnabled = LocalUserSetting.useVolumePage(this);
        volumePageView = findViewById(R.id.volumePage);
        setupSwitchImage(isVolumePageEnabled, volumePageView);
        volumePageView.setOnClickListener(volumeListener);
        
        isVerticalPageEnabled = LocalUserSetting.useVerticalPage(this);
        verticalPageView = findViewById(R.id.verticalPageing);
        setupSwitchImage(isVerticalPageEnabled, verticalPageView);
        verticalPageView.setOnClickListener(verticalListener);
        verticalPageView.setVisibility(View.GONE);
        
		lockScreenOrientation = LocalUserSetting.lockScreenOrientation(this);
		if (lockScreenOrientation == LocalUserSetting.SCREEN_DONT_LOCK) {
			isLockScreenEnabled = false;
		} else {
			isLockScreenEnabled = true;
		}
        lockScreenView = findViewById(R.id.lockScreen);
        setupLockImage(isLockScreenEnabled, lockScreenView);
        lockScreenView.setOnClickListener(lockScreenListener);
        
        isBackProgress = getIntent().getBooleanExtra(IsBackProgressKey, false);
        progressBackForward = findViewById(R.id.progress_back_forward);
        progressBackForwardImage = (ImageView) findViewById(R.id.progress_back_forward_image);
        progressBackForward.setEnabled(isBackProgress);
        progressBackForward.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isBackProgress) {
					Intent intent = new Intent(ACTION_GO_BACK_PROGRESS);
	                LocalBroadcastManager.getInstance(PDFReadOverlayActivity.this).sendBroadcast(intent);
				} else {
					Intent intent = new Intent(ACTION_GO_FORWARD_PROGRESS);
	                LocalBroadcastManager.getInstance(PDFReadOverlayActivity.this).sendBroadcast(intent);
				}
				isBackProgress = !isBackProgress;
				TypedArray a = PDFReadOverlayActivity.this.obtainStyledAttributes(new int[] {
						R.attr.read_progress_back_img,
						R.attr.read_progress_forward_img });
				if (isBackProgress) {
					progressBackForwardImage.setImageResource(a.getResourceId(0,
							R.drawable.reader_icon_progress_back_standard));
				} else {
					progressBackForwardImage.setImageResource(a.getResourceId(1,
							R.drawable.reader_icon_progress_forward_standard));
				}
			}
		});
	}
	
	private void setupBrightnessSeekBar() {
		brightnessSeek = (SeekBar) findViewById(R.id.lightSeek);
		if (isSyncBrightness) {
			int mode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
			try {
				mode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
			} catch (SettingNotFoundException e) {
				// TODO Auto-generated catch block
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
        int brightnessProgress = (int) (systemBrightness * 100);
        brightnessSeek.setProgress(brightnessProgress);
        setupBrightness(brightnessSeek, brightnessProgress);
        brightnessSeek.setOnSeekBarChangeListener(brightnessListener);
	}
	
	private void setupBrightness(SeekBar seekbar, int brightnessProgress) {
    	TypedArray a = this.obtainStyledAttributes(new int[] {
				R.attr.read_slider_tune_zero_img,
				R.attr.read_slider_tune_img});
    	seekbar.setProgress(brightnessProgress);
    	if (brightnessProgress <= 0) {
    		seekbar.setThumb(a.getDrawable(0));
    	} else {
    		seekbar.setThumb(a.getDrawable(1));
    	}
    }
	
	private void setupBookMarkImage() {
		TypedArray a = this.obtainStyledAttributes(new int[] {
				R.attr.read_bookmark_marked_img,
				R.attr.read_bookmark_unmark_img });
		if (isMarked) {
			markImage.setImageResource(a.getResourceId(0,
					R.drawable.reader_btn_bookmark_marked_standard));
		} else {
			markImage.setImageResource(a.getResourceId(1,
					R.drawable.reader_btn_bookmark_unmark_standard));
		}
	}
	
	private void setupSwitchImage(boolean isEnable, View parentView) {
		TypedArray a = this.obtainStyledAttributes(new int[] {
				R.attr.read_switch_on_line_img,
				R.attr.read_switch_off_line_img});
		if (isEnable) {
			parentView.findViewById(R.id.switchOn_dot).setVisibility(View.VISIBLE);
			parentView.findViewById(R.id.switchOff_dot).setVisibility(View.GONE);
			ImageView line = (ImageView) parentView.findViewById(R.id.switch_line);
			line.setImageResource(a.getResourceId(0,
					R.drawable.switchon_line_standard));
		} else {
			parentView.findViewById(R.id.switchOn_dot).setVisibility(View.GONE);
			parentView.findViewById(R.id.switchOff_dot).setVisibility(View.VISIBLE);
			ImageView line = (ImageView) parentView.findViewById(R.id.switch_line);
			line.setImageResource(a.getResourceId(1,
					R.drawable.switchoff_line_standard));
		}
	}
	
	private void setupLockImage(boolean isLock, View parentView) {
		if (isLock) {
			ImageView line = (ImageView) parentView.findViewById(R.id.lockImage);
			line.setImageResource(R.drawable.lock);
		} else {
			ImageView line = (ImageView) parentView.findViewById(R.id.lockImage);
			line.setImageResource(R.drawable.unlock);
		}
	}
	
	private void setupReadProgress(int localProgress) {
    	TypedArray a = this.obtainStyledAttributes(new int[] {
				R.attr.read_slider_tune_zero_img,
				R.attr.read_slider_tune_img});
    	if (localProgress <= 0) {
    		readSeekBar.setThumb(a.getDrawable(0));
    	} else {
    		readSeekBar.setThumb(a.getDrawable(1));
    	}
    }
	
	private void changeSettingFontSize() {
    	Intent intent = new Intent(ACTION_SETTING_FONT_CHANGE);
        intent.putExtra(ZoomLevelKey, zoomLevel);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
	
	@Override
	protected void onPause() {
		super.onPause();
		if (popWindow != null && popWindow.isShowing()) {
			popWindow.dismiss();
		}
	}
	
	PopupWindow createPopupWindow() {
		PopupWindow popupWindow = new PopupWindow(this);
		popupWindow.setWidth(LayoutParams.WRAP_CONTENT);
		popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
		popupWindow.setTouchable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable(this
				.getResources(), (Bitmap) null));
		
		return popupWindow;
	}
	
	private void createSettingFontPop(View button) {
		if (popWindow != null && popWindow.isShowing()) {
			popWindow.dismiss();
		}
		popWindow = createPopupWindow();
		View rootView = View.inflate(this,
				R.layout.pop_setting_font_read_overlay_pdf, null);
		popWindow.setContentView(rootView);
		View fontSize1 = rootView.findViewById(R.id.fontSize1);
        fontSize1.setOnClickListener(zoomDownListener);
        View fontSize2 = rootView.findViewById(R.id.fontSize2);
        fontSize2.setOnClickListener(zoomUpListener);

		rootView.setLayoutParams(new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));
		int w = View.MeasureSpec.makeMeasureSpec((int)ScreenUtils.getWidthJust(this)/2,
				View.MeasureSpec.EXACTLY);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		rootView.measure(w, h);
		int height = rootView.getMeasuredHeight();
		int[] location = new int[2];
		button.getLocationOnScreen(location);
		int offsetX = (rootView.getMeasuredWidth() - button.getWidth())/2;
		popWindow.showAtLocation(button, Gravity.NO_GRAVITY, location[0] - offsetX, location[1] - height);
	}
	
	private void createSettingMorePop(View button) {
		if (popWindow != null && popWindow.isShowing()) {
			popWindow.dismiss();
		}
		popWindow = createPopupWindow();
		View rootView = View.inflate(this,
				R.layout.pop_setting_more_read_overlay_pdf, null);
		popWindow.setContentView(rootView);
		
		brightnessSeek = (SeekBar) rootView.findViewById(R.id.lightSeek);
        if (isSyncBrightness) {
        	int mode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
			try {
				mode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
			} catch (SettingNotFoundException e) {
				// TODO Auto-generated catch block
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
        int brightnessProgress = (int) (systemBrightness * 100);
        brightnessSeek.setProgress(brightnessProgress);
        setupBrightness(brightnessSeek, brightnessProgress);
        brightnessSeek.setOnSeekBarChangeListener(brightnessListener);
        
        isSyncBrightness = LocalUserSetting.isSyncBrightness(this);
        modifyBrightnessView = rootView.findViewById(R.id.modifySystemLight);
        setupSwitchImage(isSyncBrightness, modifyBrightnessView);
        modifyBrightnessView.setOnClickListener(syncListener);
        
        isVolumePageEnabled = LocalUserSetting.useVolumePage(this);
        volumePageView = rootView.findViewById(R.id.volumePage);
        setupSwitchImage(isVolumePageEnabled, volumePageView);
        volumePageView.setOnClickListener(volumeListener);
        
        isVerticalPageEnabled = LocalUserSetting.useVerticalPage(this);
        verticalPageView = rootView.findViewById(R.id.verticalPageing);
        setupSwitchImage(isVerticalPageEnabled, verticalPageView);
        verticalPageView.setOnClickListener(verticalListener);
        
		rootView.setLayoutParams(new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));
		int w = View.MeasureSpec.makeMeasureSpec((int)ScreenUtils.getWidthJust(this) * 3/5,
				View.MeasureSpec.EXACTLY);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		rootView.measure(w, h);
		popWindow.showAtLocation(button, Gravity.RIGHT|Gravity.BOTTOM, ScreenUtils.dip2px(10), button.getHeight());
	}
	
	class PDFReadingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_BACK_PROGRESS_DONE)) {
				isBackProgress = true;
				progressBackForward.setEnabled(true);
				isMarked = intent.getBooleanExtra(BookMarkStateKey, false);
				setupBookMarkImage();
			} else if (intent.getAction().equals(ACTION_CHANGE_PROGRESS_DONE)) {
				int currentPosition = intent.getIntExtra(CurrentPageIndexKey, -1);
				if (currentPosition >= 0) {
					if(outlineList!=null ) {//outlineList is null
						for (int i = 0; i < outlineList.size(); i++) {
							if (currentPosition >= outlineList.get(i).page) {
								String chapterName = outlineList.get(i).title;
								chapterNameView.setText(chapterName);
							} else {
								break;
							}
						}
					}
					pageNumber.setText((currentPosition + 1) + "/"
							+ (pageCount));
					readSeekBar.setProgress(currentPosition);
				}
				isMarked = intent.getBooleanExtra(BookMarkStateKey, false);
				setupBookMarkImage();
			}
		}
	}

	private PDFReadingReceiver receiver = new PDFReadingReceiver();

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_BACK_PROGRESS_DONE);
		filter.addAction(ACTION_CHANGE_PROGRESS_DONE);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
	}
	
	private void unregisterReceiver() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver();
    }
}
