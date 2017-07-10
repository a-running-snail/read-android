package com.jingdong.app.reader.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.data.DrmTools;
import com.jingdong.app.reader.entity.LocalBook;

public class APKUtil {
	/****
	 * @author yfxiawei
	 * @since 2-11-12-09 还没使用
	 */

	public static boolean checkApkFile(String apkName) {
		File installFile = new File(apkName);
		if (installFile.exists() && (apkName.toLowerCase().endsWith(".jeb")||apkName.toLowerCase().endsWith(".JEB"))) {

			return true;
		}
		return false;

	}

	
	public static void startApp(Context context, String ApkPackage) {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo;
		try {
			packageInfo = packageManager.getPackageInfo(ApkPackage, 0);
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			 resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(packageInfo.packageName);

			List<ResolveInfo> apps = packageManager.queryIntentActivities(
					resolveIntent, 0);

			ResolveInfo ri = apps.iterator().next();
			if (ri != null) {
				ApkPackage = ri.activityInfo.packageName;
				String className = ri.activityInfo.name;

				Intent intent = new Intent(Intent.ACTION_MAIN);
				// intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ComponentName cn = new ComponentName(ApkPackage, className);

				intent.setComponent(cn);
				context.startActivity(intent);
			}
		} catch (NameNotFoundException e) {
		}

	}
	
	
	/**
	 * @author yfxiawei
	 * @see 运行app，接受包名做参数，运行，如果无法运行说明，app未安装，则调用安装方法，把apk路径穿进去安装
	 * @since 2011-12-11
	 * */
	public static void RunApp(Context context, String ApkPackage, LocalBook localBook) {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo;
		try {
			packageInfo = packageManager.getPackageInfo(ApkPackage, 0);
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			// resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(packageInfo.packageName);

			List<ResolveInfo> apps = packageManager.queryIntentActivities(
					resolveIntent, 0);

			ResolveInfo ri = apps.iterator().next();
			if (ri != null) {
				ApkPackage = ri.activityInfo.packageName;
				String className = ri.activityInfo.name;

				Intent intent = new Intent(Intent.ACTION_MAIN);
				// intent.addCategory(Intent.CATEGORY_LAUNCHER);

				ComponentName cn = new ComponentName(ApkPackage, className);

				intent.setComponent(cn);
				context.startActivity(intent);
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			// 此处应该检测apk文件，正确后执行安装
			String apkPath = null;
			try {
			apkPath = APKUtil.ParseJebToAPK(localBook);
			}catch (Exception erro) {
			}
			if (!TextUtils.isEmpty(apkPath)&&checkApkFile(apkPath)) {
				ApkInstall(context, apkPath);
			} else {
				Toast.makeText(context, "APK文件解析出错", Toast.LENGTH_SHORT).show();
			}
			// e.printStackTrace();
		}

	}

	public static void ApkRemove(Context context, String ApkPackage) {

		try {
			Uri packageURI = Uri.parse("package:" + ApkPackage);
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
					packageURI);
			context.startActivity(uninstallIntent);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/** @author yfxiawei 运行apk文件 */
	public static boolean ApkpkgExist(Context context, String ApkPackage) {
		PackageInfo exist;// 若为空，说明apk没有安装，会抛异常
		PackageManager pm = context.getPackageManager();
		try {
			exist = pm.getPackageInfo(ApkPackage, 0);
			if (exist == null) {
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;

	}
/*******
 * APk安装 方法 支持非sd卡安装  非sd卡自动设置权限
 * ***********/
	public static void ApkInstall(Context context, String ApkFile) {
		// 安装apk文件
		Intent intent = new Intent(Intent.ACTION_VIEW);

		intent.setDataAndType(Uri.fromFile(new File(ApkFile)),
				"application/vnd.android.package-archive");
		setPermissionExecute(new File(ApkFile));
		context.startActivity(intent);

	}
/***
 * 设置apk可执行权限 
 * @param targetFile apk路径
 * 
 * **/
	private static boolean setPermissionExecute(File targetFile) {
		if (!targetFile.getAbsolutePath().startsWith("/data/")) {
			return true;
		}
		String permission = "755";
		// 下载的文件夹要具有RX权限(可以进入文件夹的前提)
		String subDir = "chmod " + permission + " "
				+ targetFile.getParentFile().getAbsolutePath();
		// 下载的文件apk为android的应用程序 需要具有x的权限，也就是可执行的权限，才能安装
		String rootDir = "chmod " + permission + " "
				+ targetFile.getParentFile().getParentFile().getAbsolutePath();
		
		String apkComm = "chmod " + permission + " "
				+ targetFile.getAbsolutePath();
		// String apkComm = "chmod -R " + permission + " " + apkPath + "/"
		// + filename;//不支持 -R选项
		Runtime runtime = Runtime.getRuntime();
	
		try {
			//分别设置 目录 子目录 文件三个权限
			runtime.exec(rootDir);
			runtime.exec(subDir);
			runtime.exec(apkComm);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	public static String ParseApkPackage(Context context, String apkFile) {
		PackageManager packageManager = context.getPackageManager();

		// File file = new File("/mnt/sdcard/ApiDemos.apk");
		String packageName = null;
		PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkFile,
				PackageManager.GET_ACTIVITIES);

		if (packageInfo != null) {
			ApplicationInfo appInfo = packageInfo.applicationInfo;

			packageName = appInfo.packageName; // 得到安装包名称

			return packageName;

		}
		return packageName;

	}

	
	public static String ParseJebPackage(LocalBook localBook) {
//		Log.i("zhoubo", "ParseJebPackage....is start");
//		String packageName = null;
//		File file = MyApplication.getInstance().getDir("jeb",Context.MODE_PRIVATE);
//		//File file = Environment.getExternalStorageDirectory();
//		String path = file.getAbsolutePath()+"/"+LocalBook.TEMP_PAth_ZIP;
//		 CopyFile.delFolder(path);
//		 Log.i("zhoubo", "localBook.book_path==="+localBook.book_path);
//		 DrmTools.decryptApk(localBook.cert, localBook.random, localBook.book_path, path);
//		 String newPath = file.getAbsolutePath()+"/"+LocalBook.TEMP_PAth_APK;
//		 try {
//		  Log.i("zhoubo", "unZipFile....is start");
//			Log.i("zhoubo", "path===="+path);
//			Log.i("zhoubo", "newPath===="+newPath);
//			UnzipFile.unInflaterFile(path, newPath);
//			Log.i("zhoubo", "unZipFile....is ok");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		String packageName = null;
		 String newPath = ParseJebToAPK(localBook);
		 packageName = ParseApkPackage(MZBookApplication.getInstance().getApplicationContext(),  newPath);
//		 CopyFile.delFolder(path);
		 FileUtils.delFolder(newPath);
		 return packageName;
	}
	
	
	public static String ParseJebToAPK(LocalBook localBook) {
		Log.i("zhoubo", "ParseJebPackage....is start");
		//String packageName = null;
		FileGuider savePath = new FileGuider(FileGuider.SPACE_PRIORITY_EXTERNAL);
//		savePath.setSpace(FileGuider.SPACE_ONLY_EXTERNAL);
		savePath.setImmutable(true);
		savePath.setChildDirName("/apk/"+"temp");
		savePath.setFileName(LocalBook.TEMP_PAth_ZIP);
		String temPath = savePath.getFilePath();
		savePath.setFileName(LocalBook.TEMP_PAth_APK);
		String newPath = savePath.getFilePath();
		 FileUtils.delFolder(temPath);
		 Log.i("zhoubo", "localBook.book_path==="+localBook.book_path);
		 DrmTools.decryptApk(localBook.cert, localBook.random, localBook.book_path, temPath);
		 try {
		  Log.i("zhoubo", "unZipFile....is start");
			Log.i("zhoubo", "path===="+temPath);
			Log.i("zhoubo", "newPath===="+newPath);
			UnzipFile.unInflaterFile(temPath, newPath);
			Log.i("zhoubo", "unZipFile....is ok");
		}catch (Exception e) {
			e.printStackTrace();
		}
		 FileUtils.delFolder(temPath);
		 return newPath;
	}
	// public static ApkInfos getApkFileInfo(Context ctx, String apkPath) {
	// /** @author yfxiawei 此类检测apk文件的包名 label 和获取apk文件图标 */
	// File apkFile = new File(apkPath);
	// if (!apkFile.exists() || !apkPath.toLowerCase().endsWith(".apk")) {
	// // System.out.println("文件路径不正确");
	// // 此处记得要打印Log
	// return null;
	// }
	// ApkInfos apkInfos;
	// String PATH_PackageParser = "android.content.pm.PackageParser";
	// String PATH_AssetManager = "android.content.res.AssetManager";
	// try {
	// // 反射得到pkgParserCls对象并实例化,有参数
	// Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
	// Class<?>[] typeArgs = { String.class };
	// Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
	// Object[] valueArgs = { apkPath };
	// Object pkgParser = pkgParserCt.newInstance(valueArgs);
	//
	// // 从pkgParserCls类得到parsePackage方法
	// DisplayMetrics metrics = new DisplayMetrics();
	// metrics.setToDefaults();// 这个是与显示有关的, 这边使用默认
	// typeArgs = new Class<?>[] { File.class, String.class,
	// DisplayMetrics.class, int.class };
	// Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
	// "parsePackage", typeArgs);
	//
	// valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };
	//
	// // 执行pkgParser_parsePackageMtd方法并返回
	// Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
	// valueArgs);
	//
	// // 从返回的对象得到名为"applicationInfo"的字段对象
	// if (pkgParserPkg == null) {
	// return null;
	// }
	// Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(
	// "applicationInfo");
	//
	// // 从对象"pkgParserPkg"得到字段"appInfoFld"的值
	// if (appInfoFld.get(pkgParserPkg) == null) {
	// return null;
	// }
	// ApplicationInfo info = (ApplicationInfo) appInfoFld
	// .get(pkgParserPkg);
	//
	// // 反射得到assetMagCls对象并实例化,无参
	// Class<?> assetMagCls = Class.forName(PATH_AssetManager);
	// Object assetMag = assetMagCls.newInstance();
	// // 从assetMagCls类得到addAssetPath方法
	// typeArgs = new Class[1];
	// typeArgs[0] = String.class;
	// Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
	// "addAssetPath", typeArgs);
	// valueArgs = new Object[1];
	// valueArgs[0] = apkPath;
	// // 执行assetMag_addAssetPathMtd方法
	// assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
	//
	// // 得到Resources对象并实例化,有参数
	// Resources res = ctx.getResources();
	// typeArgs = new Class[3];
	// typeArgs[0] = assetMag.getClass();
	// typeArgs[1] = res.getDisplayMetrics().getClass();
	// typeArgs[2] = res.getConfiguration().getClass();
	// Constructor<Resources> resCt = Resources.class
	// .getConstructor(typeArgs);
	// valueArgs = new Object[3];
	// valueArgs[0] = assetMag;
	// valueArgs[1] = res.getDisplayMetrics();
	// valueArgs[2] = res.getConfiguration();
	// res = resCt.newInstance(valueArgs);
	//
	// // 读取apk文件的信息
	// apkInfos = new ApkInfos();
	// if (info != null) {
	// if (info.icon != 0) {// 图片存在，则读取相关信息
	// Drawable icon = res.getDrawable(info.icon);// 图标
	// apkInfos.setIcon(icon);
	// }
	// if (info.labelRes != 0) {
	// String name = (String) res.getText(info.labelRes);// 名字
	// apkInfos.setAppName(name);
	// } else {
	// String apkName = apkFile.getName();
	// apkInfos.setAppName(apkName.substring(0,
	// apkName.lastIndexOf(".")));
	// }
	// String pkgName = info.packageName;// 包名
	// // appInfoData.setApppackage(pkgName);
	// apkInfos.setPackageName(pkgName);
	// } else {
	// return null;
	// }
	// PackageManager pm = ctx.getPackageManager();
	// PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath,
	// PackageManager.GET_ACTIVITIES);
	// // System.out.println(packageInfo.signatures);
	// // if (packageInfo != null) {
	//
	// // appInfoData.setAppversion(packageInfo.versionName);// 版本号
	// // appInfoData.setAppversionCode(packageInfo.versionCode +
	// // "");//
	// // 版本码
	// // }
	// return apkInfos;
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return null;
	// }
}
