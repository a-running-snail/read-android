package com.jingdong.app.reader.activity;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadThreadQueue.RefreshAble;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.config.ITransKey;
import com.jingdong.app.reader.data.db.DBHelper;
import com.jingdong.app.reader.plugin.FontItem;
import com.jingdong.app.reader.plugin.FontParser;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.DataIntent;
import com.jingdong.app.reader.util.FileUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ThemeUtils;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.view.dialog.DialogManager;

public class ReaderSettingActivity extends BaseActivityWithTopBar implements RefreshAble, ITransKey {

	public static final String ACTION_CHANGE_FONTFACE = "action_change_fontface";
	public static final String ACTION_DOWNLOAD_FONT_DONE = "action_download_font_done";
	public static final String ACTION_IMPORT_FONT_DONE = "action_import_font_done";
	public static final String FontListKey = "FontListKey";
	public static final String FontPathKey = "FontPathKey";
	public static final String SystemFontKey = "SystemFontKey";
	
    private ImageView switchLine;
    private ImageView switchOnDot;
    private ImageView switchOffDot;
    private LinearLayout settingFontFace;
    private List<FontItem> fontList = new ArrayList<FontItem>();
    private List<View> fontViewList = new ArrayList<View>();
	private boolean isVolumePageEnabled = false;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		registerReceiver();
		FontItem fontItem = DBHelper.queryEnabledFontItem();
		FontItem fontFZSS = DBHelper.queryFontItemByName(FontItem.FOUNDER_SS);
		if (TextUtils.isEmpty(fontItem.getFilePath())) {
			DBHelper.initDefautDbData();
		} else if (TextUtils.isEmpty(fontFZSS.getUrl())) {
			//XXX 如果没有方正仿宋执行初始化
			DBHelper.initDefautDbData();
		}
		ThemeUtils.prepareTheme(this);
		this.setContentView(R.layout.activity_reader_setting);
		this.getTopBarView().setTopBarTheme(ThemeUtils.getTopbarTheme());
		this.getTopBarView().setTitle(getString(R.string.settings_more));
		TypedArray a = this.obtainStyledAttributes(new int[] {
				R.attr.read_back_img});
		this.getTopBarView().setLeftMenuVisiable(true, a.getResourceId(0,
				R.drawable.reader_btn_back_standard));
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_PLUG);
		View volumeChangePageLayout = findViewById(R.id.volumeChangePageLayout);
		switchLine = (ImageView) volumeChangePageLayout.findViewById(R.id.switch_line);
        switchOnDot = (ImageView) volumeChangePageLayout.findViewById(R.id.switchOn_dot);
        switchOffDot = (ImageView) volumeChangePageLayout.findViewById(R.id.switchOff_dot);
        isVolumePageEnabled = LocalUserSetting.useVolumePage(this);
        setupSwitchImage();
        initPageAnimation();
		volumeChangePageLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				isVolumePageEnabled = !isVolumePageEnabled;
				LocalUserSetting.saveVolumePage(ReaderSettingActivity.this,
						isVolumePageEnabled);
				setupSwitchImage();
			}
		});
		
		View importFont = findViewById(R.id.setting_find_fontface);
		importFont.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				String fontpath = LocalUserSetting.getTextFontPath(getApplicationContext());
				MZLog.d("cj", "fontpath==========>>"+fontpath);
				Intent intent = new Intent(
						FileBrowserActivity.INTENT_ACTION_SELECT_FILE, null,
						ReaderSettingActivity.this, FileBrowserActivity.class);
				intent.putExtra(FileBrowserActivity.filterExtension, new String[]{"ttf", "otf"});
				intent.putExtra(FileBrowserActivity.startDirectoryParameter, fontpath);
				intent.putExtra(FileBrowserActivity.TITLE, "导入字体");
				ReaderSettingActivity.this.startActivity(intent);
				ReaderSettingActivity.this.overridePendingTransition(R.anim.fade,
						R.anim.hold);
			}
		});

		
	}
	
	private void initSetFontView() {
		fontList.clear();
		fontViewList.clear();
		settingFontFace = (LinearLayout) findViewById(R.id.setting_fontface);
		settingFontFace.removeAllViews();
		ArrayList<FontItem> list = DBHelper.queryPluginItemList(FontItem.KEY_PLUGIN_FONT);
		for (int i = 0; i < list.size(); i++) {
			Object obj = list.get(i);
			FontItem fontItem = (FontItem) obj;
			View child = createFontItemView(fontItem);
			if (child != null) {
				child.setTag(Integer.valueOf(i));
				fontList.add(fontItem);
				fontViewList.add(child);
				if (FontItem.FOUNDER_SS.equals(fontItem.getName())) {
					settingFontFace.addView(child, 1);//XXX 方正仿宋放在顺序第二个
				} else {
					settingFontFace.addView(child);
				}
			}
		}
		View lastFontView = settingFontFace.getChildAt(settingFontFace.getChildCount()-1);
		lastFontView.findViewById(R.id.itemSubLine).setVisibility(View.GONE);
	}
	
	private View createFontItemView(final FontItem fontItem) {
		if (fontItem.getPlugin_src() == FontItem.KEY_FONT_SRC_IMPORT) {
			if (!FileUtils.isExist(fontItem.getFilePath())) {
				return null;
			}
		} else if (fontItem.getPlugin_src() == FontItem.KEY_FONT_SRC_INTERNAL) {
			if (!FileUtils.isExist(fontItem.getFilePath())) {
				fontItem.setDownloadStatus(FontItem.STATE_UNLOAD);
			}
		}
		View child = View.inflate(this, R.layout.item_font_reader_setting,
				null);
		TextView fontNameView = (TextView) child.findViewById(R.id.label);
		fontNameView.setText(fontItem.getName());
		TextView fontSize = (TextView) child.findViewById(R.id.fileSize);
		if (fontItem.getDownloadStatus() == FontItem.STATE_UNLOAD 
				|| fontItem.getDownloadStatus() == FontItem.STATE_LOADING) {
			fontSize.setVisibility(View.VISIBLE);
			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			fontSize.setText(decimalFormat.format((float) fontItem
					.getInitShowTotalSize() / (1024 * 1024))
					+ "MB");
		}else {
			fontSize.setVisibility(View.GONE);
		}
		if (fontItem.getPlugin_enable() == FontItem.KEY_PLUGIN_ENABLE) {
			child.findViewById(R.id.progress).setVisibility(View.GONE);
			child.findViewById(R.id.fontEnabled).setVisibility(View.VISIBLE);
		}
		
		if (fontItem.getDownloadStatus() == FontItem.STATE_LOADING) {
			setupDownloadingUI(child, fontItem);
		} else if (fontItem.getDownloadStatus() == FontItem.STATE_LOADED) {
			child.findViewById(R.id.progress).setVisibility(View.GONE);
		} else if (fontItem.getDownloadStatus() == FontItem.STATE_LOADING
				&& fontItem.getCurrentSize() == fontItem.getTotalSize()
				&& fontItem.getTotalSize() > 0) {
			child.findViewById(R.id.progress).setVisibility(View.GONE);
		}
		
		ImageView fontImage = (ImageView) child.findViewById(R.id.labelImage);
		fontImage.setVisibility(View.GONE);
		if (fontItem.getName().equals(FontItem.FOUNDER_SS)) {
			fontNameView.setVisibility(View.GONE);
			fontImage.setVisibility(View.VISIBLE);
			fontImage.setBackgroundResource(R.drawable.founder_ss);
		} else if (fontItem.getName().equals(FontItem.FOUNDER_KAITI)) {
			fontNameView.setVisibility(View.GONE);
			fontImage.setVisibility(View.VISIBLE);
			fontImage.setBackgroundResource(R.drawable.founder_kaiti);
		} else if (fontItem.getName().equals(FontItem.FOUNDER_LANTINGHEI)) {
			fontNameView.setVisibility(View.GONE);
			fontImage.setVisibility(View.VISIBLE);
			fontImage.setBackgroundResource(R.drawable.founder_lantinghei);
		} else if (fontItem.getName().equals(FontItem.FOUNDER_MIAOWUHEI)) {
			fontNameView.setVisibility(View.GONE);
			fontImage.setVisibility(View.VISIBLE);
			fontImage.setBackgroundResource(R.drawable.founder_miaowuhei);
		}
		
		child.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Integer index = (Integer) view.getTag();
				handleDownload(index.intValue());
			}
		});
		child.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				if (fontItem.getName().equals(FontItem.FOUNDER_SS)
						|| fontItem.getName().equals(FontItem.FOUNDER_KAITI)
						|| fontItem.getName().equals(FontItem.FOUNDER_LANTINGHEI)
						|| fontItem.getName().equals(FontItem.FOUNDER_MIAOWUHEI)
						|| fontItem.getName().startsWith("系统字体 ")) {
					return false;
				}
				showDialog(fontItem);
				return true;
			}

		
		});
		return child;
	}
	private void showDialog(final FontItem fontItem) {
		
	DialogManager.showCommonDialog(this,"提示" ,"您确定删除字体", "确定", "取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					DBHelper.fontDelete(fontItem.getFilePath());
					ToastUtil.showToastInThread("删除字体成功",Toast.LENGTH_SHORT);
					initSetFontView();
					dialog.dismiss();
				}else
					dialog.dismiss();
			}
		}); 
	}
	private void setupSwitchImage() {
		TypedArray a = this.obtainStyledAttributes(new int[] {
				R.attr.read_switch_on_line_img,
				R.attr.read_switch_off_line_img});
		if (isVolumePageEnabled) {
			switchOnDot.setVisibility(View.VISIBLE);
			switchOffDot.setVisibility(View.GONE);
			switchLine.setImageResource(a.getResourceId(0,
					R.drawable.switchon_line_standard));
		} else {
			switchOnDot.setVisibility(View.GONE);
			switchOffDot.setVisibility(View.VISIBLE);
			switchLine.setImageResource(a.getResourceId(1,
					R.drawable.switchoff_line_standard));
		}
		a.recycle();
	}
	
	private void initPageAnimation() {
		int animation = LocalUserSetting.getPageAnimation(this);
		LinearLayout settingPageAnimation = (LinearLayout) findViewById(R.id.setting_pageAnimation);
		String[] pageAnimation = this.getResources().getStringArray(R.array.page_animation);
		for (int i = 0; i < pageAnimation.length; i++) {
			View child = createAnimation(pageAnimation[i], i, animation);
			settingPageAnimation.addView(child);
		}
		View lastFontView = settingPageAnimation.getChildAt(settingPageAnimation.getChildCount()-1);
		lastFontView.findViewById(R.id.itemSubLine).setVisibility(View.GONE);
	}
	
	private View createAnimation(String name, int index, int animation) {
		boolean isEnable = animation == index;
		View child = View.inflate(this, R.layout.item_font_reader_setting, null);
		child.setTag(Integer.valueOf(index));
		TextView fontNameView = (TextView) child.findViewById(R.id.label);
		fontNameView.setText(name);
		child.findViewById(R.id.fileSize).setVisibility(View.GONE);
		child.findViewById(R.id.progress).setVisibility(View.GONE);
		if (isEnable) {
			child.findViewById(R.id.fontEnabled).setVisibility(View.VISIBLE);
		} else {
			child.findViewById(R.id.fontEnabled).setVisibility(View.GONE);
		}
		child.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Integer viewIndex = (Integer) view.getTag();
				LocalUserSetting.savePageAnimation(ReaderSettingActivity.this, viewIndex.intValue());
				resetAnimation(viewIndex.intValue());
			}
		});
		return child;
	}
	
	private void resetAnimation(int index) {
		LinearLayout pageAnimation = (LinearLayout) findViewById(R.id.setting_pageAnimation);
		for (int i = 0; i < pageAnimation.getChildCount(); i++) {
			View child = pageAnimation.getChildAt(i);
			if (index == i) {
				child.findViewById(R.id.fontEnabled).setVisibility(View.VISIBLE);
			} else {
				child.findViewById(R.id.fontEnabled).setVisibility(View.GONE);
			}
		}
	}
	
	class FontSettingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_IMPORT_FONT_DONE)) {
				View lastFontView = fontViewList.get(fontViewList.size()-1);
				lastFontView.findViewById(R.id.itemSubLine).setVisibility(View.VISIBLE);
				ArrayList<String> fontPathList = intent.getStringArrayListExtra(FontListKey);
				int totalSize = fontViewList.size();
				int index = totalSize;
				for (String fontPath : fontPathList) {
					if (FileUtils.isExist(fontPath)
							&& !DBHelper.fontIsImported(fontPath)) {
						if (fontPath.endsWith(".ttf")
								&& !FontParser.getInstance().parse(fontPath)) {
							continue;
						}
						File file = new File(fontPath);
						FontItem fontItem = new FontItem(file.getName());
						fontItem.setFilePath(fontPath);
						fontItem.setCurrentSize(file.length());
						fontItem.setPlugin_src(FontItem.KEY_FONT_SRC_IMPORT);
						fontItem.setPlugin_type(FontItem.KEY_PLUGIN_FONT);
						fontItem.setInitShowTotalSize(file.length());
						fontItem.setTotalSize(file.length());
						fontItem.setDownloadStatus(FontItem.STATE_LOADED);
						fontItem.saveLoadTime(System.currentTimeMillis());
						fontItem.save();
						View child = createFontItemView(fontItem);
						child.setTag(Integer.valueOf(index));
						fontList.add(fontItem);
						fontViewList.add(child);
						settingFontFace.addView(child);
						index++;
					}else if (FileUtils.isExist(fontPath)
							&& DBHelper.fontIsImported(fontPath)){
						File file = new File(fontPath);
						ToastUtil.showToastInThread(file.getName()+getString(R.string.exitImport), Toast.LENGTH_SHORT);
						break;
					}
				}
				lastFontView = fontViewList.get(fontViewList.size()-1);
				lastFontView.findViewById(R.id.itemSubLine).setVisibility(View.GONE);
				if (totalSize < fontViewList.size()) {
					ToastUtil.showToastInThread(getString(R.string.alreadyImport), Toast.LENGTH_SHORT);
				}
			}
		}
	}
	
	private FontSettingReceiver receiver = new FontSettingReceiver();
	
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_IMPORT_FONT_DONE);
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
	
	private void handleDownload(int index) {
		FontItem fontItem = fontList.get(index);
		if (fontItem.getPlugin_enable() == FontItem.KEY_PLUGIN_ENABLE)
			return;
		if (fontItem.getPlugin_src() == FontItem.KEY_FONT_SRC_IMPORT
				|| fontItem.getDownloadStatus() == FontItem.STATE_LOADED) {
			if (fontItem.getPlugin_enable() != FontItem.KEY_PLUGIN_ENABLE) {
				fontItem.setPlugin_enable(FontItem.KEY_PLUGIN_ENABLE);

				fontEnablerUI(fontItem);
			}

		} else if (fontItem.getDownloadStatus() == FontItem.STATE_UNLOAD
				|| fontItem.getDownloadStatus() == FontItem.STATE_LOAD_FAILED
				|| fontItem.getDownloadStatus() == FontItem.STATE_LOAD_PAUSED
				|| fontItem.getDownloadStatus() == FontItem.STATE_LOAD_READY) {
			View view = fontViewList.get(index);
			setupDownloadingUI(view, fontItem);
			Intent intent = new Intent(this, DownloadService.class);
			String dataKey = DataIntent.creatKey();
			fontItem.setMenualStop(false);
			fontItem.setTotalSize(0);
			DataIntent.put(dataKey, fontItem);
			intent.putExtra(KEY1, dataKey);
			this.startService(intent);
		} else if (fontItem.getDownloadStatus() == FontItem.STATE_LOADING) {
//			View view = fontViewList.get(index);
//			setupPauseUI(view);
//			DownloadService.stop(fontItem);
		}
	}
	
	/*******
	 * 用户点击字体是执行，是已激活的置为未激活，点击的置为已激活，保存，刷新Ui
	 * 
	 * @author yfxiawei
	 * @since version 1.2.4
	 * @param tobEnableFontItem
	 *            用户新点击的字体，待激活字体
	 * ****************/
	private void fontEnablerUI(FontItem tobEnableFontItem) {
		int index = 0;
		for (FontItem temp : fontList) {
			if (temp.getFilePath().equals(tobEnableFontItem.getFilePath())) {
				temp.setPlugin_enable(FontItem.KEY_PLUGIN_ENABLE);
				View view = fontViewList.get(index);
				view.findViewById(R.id.fontEnabled).setVisibility(View.VISIBLE);
			} else {
				temp.setPlugin_enable(FontItem.KEY_PLUGIN_DISABLE);
				View view = fontViewList.get(index);
				view.findViewById(R.id.fontEnabled).setVisibility(View.GONE);
			}
			index ++;
		}
		DBHelper.updateFontStatus(tobEnableFontItem);
		Intent intent = new Intent(ACTION_CHANGE_FONTFACE);
		intent.putExtra(FontPathKey, tobEnableFontItem.getFilePath());
		intent.putExtra(SystemFontKey, tobEnableFontItem.getPlugin_src() == FontItem.KEY_FONT_SRC_System);
        LocalBroadcastManager.getInstance(ReaderSettingActivity.this).sendBroadcast(intent);
	}

	@Override
	public void refresh(DownloadedAble downloadAble) {
		if (!(downloadAble instanceof FontItem)) {
			return;
		}
		int position = 0;
		final FontItem fontItem = (FontItem) downloadAble;
		for (FontItem item : fontList) {
			if (fontItem.getPlugin_src() == FontItem.KEY_FONT_SRC_INTERNAL
					&& fontItem.getName().equals(item.getName())
					&& fontItem.getUrl().equals(item.getUrl())) {
////				long currentSize=fontList.get(position).getCurrentSize();
////				if(item.getCurrentSize()==0&&currentSize!=0){
////					item.setCurrentSize(currentSize);
////				}
//				fontList.set(position, item);
				break;
			}
			position ++;
		}
		int state = downloadAble.getDownloadStatus();
		if (position >= fontViewList.size()) {
			return;
		}
		View view = fontViewList.get(position);
		final int index=position;
		if (state == DownloadedAble.STATE_LOADED) {
			
			fontList.get(position).setDownloadStatus(DownloadedAble.STATE_LOADED);
			setupSuccessUI(view);
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					handleDownload(index);
				}
			});
			
			Intent intent = new Intent(ACTION_DOWNLOAD_FONT_DONE);
	        LocalBroadcastManager.getInstance(ReaderSettingActivity.this).sendBroadcast(intent);
			ToastUtil.showToastInThread(getString(R.string.download_finish), Toast.LENGTH_SHORT);
		} else if (state == DownloadedAble.STATE_LOADING) {
			setupDownloadingUI(view, fontItem);
			float progress = fontItem.getCurrentSize();
			fontList.get(position).setDownloadStatus(DownloadedAble.STATE_LOADING);
			if(progress>0&&fontItem.getTotalSize() ==0){
				ToastUtil.showToastInThread(getString(R.string.download_error), Toast.LENGTH_SHORT);
				return;
			}
			DBHelper.updateFontStatus(fontItem);
			if (progress > 0) {
				progress = progress / fontItem.getTotalSize() * 100;
				setupProgressUI(view, (int)progress);
			}
		}else if (state == DownloadedAble.STATE_LOAD_READY) {
			setupProgressUI(view, 0);
		} else {
			// download fail
			setupPauseUI(view);
			ToastUtil.showToastInThread(getString(R.string.download_error), Toast.LENGTH_SHORT);
		}
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void refreshDownloadCache() {
		// TODO Auto-generated method stub
		
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setupDownloadingUI(final View view, final FontItem fontItem) {
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				float progress = fontItem.getCurrentSize();
				if(progress>0&&fontItem.getTotalSize()==0){//做个异常处理
					return;
				}
				TypedArray a = ReaderSettingActivity.this.obtainStyledAttributes(new int[] {
						R.attr.read_round_corner_disable_bg,
						R.attr.r_text_disable});
				view.findViewById(R.id.progress).setBackgroundDrawable(a.getDrawable(0));
				TextView textView = (TextView) view.findViewById(R.id.progressText);
				textView.setTextColor(a.getColor(1, R.color.r_text_disable));
				if(fontItem.getTotalSize()==0){
					fontItem.setTotalSize(fontItem.getInitShowTotalSize());
				}
				if (progress > 0) {
					progress = progress / fontItem.getTotalSize() * 100;
				}
				MZLog.d("quda", "progress="+progress);
				textView.setText(progressToString((int)progress));
				a.recycle();
			}
			
		});
	}
	
	private void setupSuccessUI(final View view) {
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				view.findViewById(R.id.progress).setVisibility(View.GONE);
				view.findViewById(R.id.fileSize).setVisibility(View.GONE);
				
			}
		});
	}
	
	private void setupProgressUI(final View view, final int progress) {
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				TextView textView = (TextView) view.findViewById(R.id.progressText);
				textView.setText(progressToString(progress));
			}
		});
	}
	
	@SuppressLint("NewApi")
	private void setupPauseUI(final View view) {
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				TypedArray a = ReaderSettingActivity.this.obtainStyledAttributes(new int[] {
						R.attr.read_round_corner_enable_bg,
						R.attr.r_text_main});
				view.findViewById(R.id.progress).setBackgroundDrawable(a.getDrawable(0));
				TextView textView = (TextView) view.findViewById(R.id.progressText);
				textView.setTextColor(a.getColor(1, R.color.r_text_main));
				textView.setText(R.string.go_on);
			}
		});
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_PLUG);
		initSetFontView();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yuedu_font_setting));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yuedu_font_setting));
	}
	
	private String progressToString(int progress) {
		if (progress < 10) {
			return " "+progress+"%";
		} else {
			return progress+"%";
		}
	}
}
