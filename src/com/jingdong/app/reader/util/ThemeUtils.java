package com.jingdong.app.reader.util;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.ReadOverlayActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.view.TopBarView;

public class ThemeUtils {
	
	public static void prepareTheme(Context context) {
		if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_NIGHT) {
			context.setTheme(R.style.NightTheme);
		} else if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_MINT) {
			context.setTheme(R.style.MintTheme);
		} else if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_SOFT) {
			context.setTheme(R.style.SoftTheme);
		} else {
			context.setTheme(R.style.WhiteTheme);
		}
	}
	
	public static String getTopbarTheme() {
		if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_NIGHT) {
			return TopBarView.THEME_NIGHT;
		} else if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_MINT) {
			return TopBarView.THEME_MINT;
		} else if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_SOFT) {
			return TopBarView.THEME_SOFT;
		} else {
			return TopBarView.THEME_WHITE;
		}
	}

	public static LayoutInflater getThemeInflater(Context context,LayoutInflater inflater)
	{
		
		Context contextThemeWrapper = null;
		if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_NIGHT) {
			contextThemeWrapper = new ContextThemeWrapper(context,
					R.style.NightTheme);
		} else if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_MINT) {
			contextThemeWrapper = new ContextThemeWrapper(context,
					R.style.MintTheme);
		} else if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_SOFT) {
			contextThemeWrapper = new ContextThemeWrapper(context,
					R.style.SoftTheme);
		} else {
			contextThemeWrapper = new ContextThemeWrapper(context,
					R.style.WhiteTheme);
		}

		if (inflater == null) {
			return LayoutInflater.from(context).cloneInContext(context);
		} else {
			return inflater.cloneInContext(contextThemeWrapper);
		}
		
	}
	
}
