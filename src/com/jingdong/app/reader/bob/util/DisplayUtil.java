package com.jingdong.app.reader.bob.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.jingdong.app.reader.application.MZBookApplication;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.view.Display;
import android.view.WindowManager;


public final class DisplayUtil {

	/**
	 * Information you can retrieve about a particular application. This
	 * corresponds to information collected from the AndroidManifest.xml's
	 * &lt;application&gt; tag.
	 */
	public static final class ApplicationInfo {

		/**
		 * Value for {@link #flags}: true when the application knows how to
		 * adjust its UI for different screen sizes. Corresponds to
		 * {@link android.R.styleable#AndroidManifestSupportsScreens_resizeable
		 * android:resizeable}.
		 */
		public static final int FLAG_RESIZEABLE_FOR_SCREENS = 1 << 12;

		/**
		 * Value for {@link #flags}: true when the application's window can be
		 * increased in size for larger screens. Corresponds to
		 * {@link android.R.styleable#AndroidManifestSupportsScreens_largeScreens
		 * android:largeScreens}.
		 */
		public static final int FLAG_SUPPORTS_LARGE_SCREENS = 1 << 11;

		/**
		 * Value for {@link #flags}: true when the application knows how to
		 * accomodate different screen densities. Corresponds to
		 * {@link android.R.styleable#AndroidManifestSupportsScreens_anyDensity
		 * android:anyDensity}.
		 */
		public static final int FLAG_SUPPORTS_SCREEN_DENSITIES = 1 << 13;

		/**
		 * Value for {@link #flags}: true when the application's window can be
		 * increased in size for extra large screens. Corresponds to
		 * {@link android.R.styleable#AndroidManifestSupportsScreens_xlargeScreens
		 * android:xlargeScreens}.
		 * 
		 * @hide
		 */
		public static final int FLAG_SUPPORTS_XLARGE_SCREENS = 1 << 19;
	}

	/**
	 * CompatibilityInfo class keeps the information about compatibility mode
	 * that the application is running under. {@hide}
	 */
	public static final class CompatibilityInfo {

		/**
		 * A flag mask to tell if the application is configured to be
		 * expandable. This differs from EXPANDABLE in that the application that
		 * is not expandable will be marked as expandable if
		 * Configuration.SCREENLAYOUT_COMPAT_NEEDED is not set.
		 */
		private static final int CONFIGURED_EXPANDABLE = 4;

		/**
		 * A flag mask to tell if the application supports large screens. This
		 * differs from LARGE_SCREENS in that the application that does not
		 * support large screens will be marked as supporting them if the
		 * current screen is not large.
		 */
		private static final int CONFIGURED_LARGE_SCREENS = 16;

		/**
		 * A flag mask to tell if the application supports xlarge screens. This
		 * differs from XLARGE_SCREENS in that the application that does not
		 * support xlarge screens will be marked as supporting them if the
		 * current screen is not xlarge.
		 */
		private static final int CONFIGURED_XLARGE_SCREENS = 64;

		/**
		 * A flag mask to indicates that the application can expand over the
		 * original size. The flag is set to true if 1) Application declares its
		 * expandable in manifest file using <supports-screens> or 2)
		 * Configuration.SCREENLAYOUT_COMPAT_NEEDED is not set {@see
		 * compatibilityFlag}
		 */
		private static final int EXPANDABLE = 2;

		/**
		 * A flag mask to indicates that the application supports large screens.
		 * The flag is set to true if 1) Application declares it supports large
		 * screens in manifest file using <supports-screens> or 2) The screen
		 * size is not large {@see compatibilityFlag}
		 */
		private static final int LARGE_SCREENS = 8;

		/**
		 * A flag mask to tell if the application needs scaling (when
		 * mApplicationScale != 1.0f) {@see compatibilityFlag}
		 */
		private static final int SCALING_REQUIRED = 1;

		/**
		 * A flag mask to indicates that the application supports xlarge
		 * screens. The flag is set to true if 1) Application declares it
		 * supports xlarge screens in manifest file using <supports-screens> or
		 * 2) The screen size is not xlarge {@see compatibilityFlag}
		 */
		private static final int XLARGE_SCREENS = 32;

		/**
		 * The effective screen density we have selected for this application.
		 */
		public final int applicationDensity;

		/**
		 * Application's inverted scale.
		 */
		public final float applicationInvertedScale;

		/**
		 * Application's scale.
		 */
		public final float applicationScale;

		/**
		 * A compatibility flags
		 */
		public int compatibilityFlags;

		public CompatibilityInfo(android.content.pm.ApplicationInfo appInfo) {

			if ((appInfo.flags & DisplayUtil.ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS) != 0) {
				compatibilityFlags |= LARGE_SCREENS | CONFIGURED_LARGE_SCREENS;
			}
			if ((appInfo.flags & DisplayUtil.ApplicationInfo.FLAG_SUPPORTS_XLARGE_SCREENS) != 0) {
				compatibilityFlags |= XLARGE_SCREENS
						| CONFIGURED_XLARGE_SCREENS;
			}
			if ((appInfo.flags & DisplayUtil.ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS) != 0) {
				compatibilityFlags |= EXPANDABLE | CONFIGURED_EXPANDABLE;
			}

			if ((appInfo.flags & DisplayUtil.ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES) != 0) {
				applicationDensity = DisplayMetrics.DENSITY_DEVICE;
				applicationScale = 1.0f;
				applicationInvertedScale = 1.0f;
			} else {
				applicationDensity = DisplayMetrics.DENSITY_DEFAULT;
				applicationScale = DisplayMetrics.DENSITY_DEVICE
						/ (float) DisplayMetrics.DENSITY_DEFAULT;
				applicationInvertedScale = 1.0f / applicationScale;
				compatibilityFlags |= SCALING_REQUIRED;
			}
		}
	}

	/**
	 * A structure describing general information about a display, such as its
	 * size, density, and font scaling.
	 * <p>
	 * To access the DisplayMetrics members, initialize an object like this:
	 * </p>
	 * 
	 * <pre>
	 * DisplayMetrics metrics = new DisplayMetrics();
	 * getWindowManager().getDefaultDisplay().getMetrics(metrics);
	 * </pre>
	 */
	public static final class DisplayMetrics {

		/**
		 * Standard quantized DPI for high-density screens.
		 */
		public static final int DENSITY_HIGH = 240;

		/**
		 * Standard quantized DPI for low-density screens.
		 */
		public static final int DENSITY_LOW = 120;

		/**
		 * Standard quantized DPI for medium-density screens.
		 */
		public static final int DENSITY_MEDIUM = 160;

		/**
		 * Standard quantized DPI for extra-high-density screens.
		 */
		public static final int DENSITY_XHIGH = 320;

		/**
		 * The reference density used throughout the system.
		 */
		public static final int DENSITY_DEFAULT = DENSITY_MEDIUM;

		/**
		 * The device's density.
		 * 
		 * @hide becase eventually this should be able to change while running,
		 *       so shouldn't be a constant.
		 */
		public static final int DENSITY_DEVICE = getDeviceDensity();

		private static int getDeviceDensity() {
			// qemu.sf.lcd_density can be used to override ro.sf.lcd_density
			// when running in the emulator, allowing for dynamic
			// configurations.
			// The reason for this is that ro.sf.lcd_density is write-once and
			// is
			// set by the init process when it parses build.prop before anything
			// else.

			int deviceDensity = DENSITY_DEFAULT;

			try {
				final Class<?> theClass = Class
						.forName("android.os.SystemProperties");
				final Method[] methods = theClass.getDeclaredMethods();
				Method getInt = null;
				final int length = methods.length;
				for (int i = 0; i < length; i++) {
					final Method method = methods[i];
					final String name = method.getName();
					if (name.equals("getInt")) {
						getInt = method;
					}
				}
				if (getInt != null) {
					getInt.setAccessible(true);
					// final Class<?>[] paramsTypes =
					// getInt.getParameterTypes();
					// for (Class<?> paramClass : paramsTypes) {
					// Log.d("getIMSI","method getInt : " +
					// paramClass.getName());
					// }
					final String keyLcdDensity = new String("ro.sf.lcd_density");
					final Integer lcdDensity = (Integer) getInt.invoke(null,
							new Object[] { keyLcdDensity, DENSITY_DEFAULT });
					final String keyQemuLcdDensity = new String(
							"qemu.sf.lcd_density");
					final Integer qemuLcdDensity = (Integer) getInt.invoke(
							null,
							new Object[] { keyQemuLcdDensity, lcdDensity });
					deviceDensity = qemuLcdDensity;
				}
			} catch (SecurityException e) {
			} catch (IllegalArgumentException e) {
			} catch (ClassNotFoundException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			} catch (Exception e) {
			}

			/*
			 * return SystemProperties.getInt("qemu.sf.lcd_density",
			 * SystemProperties.getInt("ro.sf.lcd_density", DENSITY_DEFAULT));
			 */
			return deviceDensity;
		}
	}

	public enum ScreenOrientation {
		ORIENTATION_LANDSCAPE, ORIENTATION_PORTRAIT, ORIENTATION_SQUARE, ORIENTATION_UNDEFINED
	}

	public static final class ScreenSize {
		public int height = 480;

		public int width = 320;
	}

	public static float getApplicationScale(Context context)
			throws NameNotFoundException {
		return new DisplayUtil.CompatibilityInfo(context.getPackageManager()
				.getApplicationInfo(context.getPackageName(),
						PackageManager.GET_META_DATA)).applicationScale;
	}

	public static ScreenOrientation getScreenOrientation(Context context) {

		ScreenOrientation orientation = ScreenOrientation.ORIENTATION_PORTRAIT;

		final Configuration configuration = context.getResources()
				.getConfiguration();
		switch (configuration.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			orientation = ScreenOrientation.ORIENTATION_LANDSCAPE;
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			orientation = ScreenOrientation.ORIENTATION_PORTRAIT;
			break;
		case Configuration.ORIENTATION_SQUARE:
			orientation = ScreenOrientation.ORIENTATION_SQUARE;
			break;
		case Configuration.ORIENTATION_UNDEFINED:
		default:
			orientation = ScreenOrientation.ORIENTATION_UNDEFINED;
			break;
		}

		return orientation;
	}

	public static ScreenSize getScreenSize(Context context,
			ScreenOrientation screenOrientation) {

		final ScreenSize screenSize = new ScreenSize();
		float screenScale = 1.0f;
		try {
			screenScale = getApplicationScale(context);
		} catch (NameNotFoundException e) {
		}

		final android.util.DisplayMetrics displayMetrics = context
				.getResources().getDisplayMetrics();

		/*
		 * 默认，宽度小，高度大，例如:320x480 360x640 480x800 480x854
		 */
		final int w = displayMetrics.widthPixels;
		final int h = displayMetrics.heightPixels;

		switch (screenOrientation) {
		case ORIENTATION_LANDSCAPE:
			screenSize.width = Math.round((w > h ? w : h) * screenScale);
			screenSize.height = Math.round((w < h ? w : h) * screenScale);
			break;
		case ORIENTATION_PORTRAIT:
		case ORIENTATION_SQUARE:
		case ORIENTATION_UNDEFINED:
			screenSize.width = Math.round(w * screenScale);
			screenSize.height = Math.round(h * screenScale);
		default:
			screenSize.width = Math.round((w < h ? w : h) * screenScale);
			screenSize.height = Math.round((w > h ? w : h) * screenScale);
			break;
		}

		return screenSize;
	}

	private static ScreenSize screenSize;

	public static int getHeight() {
		if (screenSize == null) {
			screenSize = getScreenSize(MZBookApplication.getInstance(),
					ScreenOrientation.ORIENTATION_UNDEFINED);
		}
		return screenSize.height;
	}

	public static int getCurrentWindosWidth(Activity activty) {
		WindowManager windowManager = activty.getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		int screenWidth = display.getWidth();
		return screenWidth;
	}

	public static int getWidth() {
		if (screenSize == null) {
			screenSize = getScreenSize(MZBookApplication.getInstance(),
					ScreenOrientation.ORIENTATION_UNDEFINED);
		}
		return screenSize.width;
	}

	private DisplayUtil() {
	}
	
  public static boolean isHDDisplay(){
		if(getWidth()*getHeight()>1024*600){
		return true;	
		}
		return false;	
	}
}
