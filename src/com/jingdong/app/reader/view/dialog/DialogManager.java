package com.jingdong.app.reader.view.dialog;

import android.content.Context;
import android.content.DialogInterface;

public class DialogManager {
	
	public static void showCommonDialog(Context ctx, String content,String ok,String cancel,DialogInterface.OnClickListener listener) {
		CommonDialog.Builder commonBuilder = new CommonDialog.Builder(ctx,false);
		commonBuilder.setMessage(content)
            .setNegativeButton(cancel, listener)
            .setPositiveButton(ok, listener);
        CommonDialog dialog = commonBuilder.create();
        dialog.show();
	}
	
	public static void showCommonDialog(Context ctx,String title,String content,String ok,String cancel,DialogInterface.OnClickListener listener) {
		CommonDialog.Builder commonBuilder = new CommonDialog.Builder(ctx,false);
        commonBuilder.setTitle(title)
            .setMessage(content)
            .setNegativeButton(cancel, listener)
            .setPositiveButton(ok, listener);
        CommonDialog dialog = commonBuilder.create();
        dialog.show();
	}
	/**
	 * 对话框
	 * @param ctx
	 * @param title
	 * @param content
	 * @param ok
	 * @param cancel
	 * @param listener
	 * @param showPositiveButton 是否显示第二个按钮
	 */
	public static void showCommonDialog(Context ctx,String title,String content,String ok,String cancel,DialogInterface.OnClickListener listener,boolean showNegativeButton) {
		CommonDialog.Builder commonBuilder = new CommonDialog.Builder(ctx,false);
        commonBuilder.setTitle(title)
            .setMessage(content)
            .setPositiveButton(ok, listener);
        if(showNegativeButton)
        	commonBuilder.setNegativeButton(cancel, listener);
        else
        	commonBuilder.setNegativeButton("取消", listener);
        CommonDialog dialog = commonBuilder.create();
        dialog.show();
	}
	
	public static void showToastDialog(Context ctx,String title,String content){
		
		CommonDialog.Builder commonBuilder = new CommonDialog.Builder(ctx,true);
        commonBuilder.setTitle(title).setMessage(content);
        CommonDialog dialog = commonBuilder.create();
        dialog.show();
	}
	
	public static CommonDialog getCommonDialog(Context ctx,String title,String content,String ok,String cancel,DialogInterface.OnClickListener listener) {
		CommonDialog.Builder commonBuilder = new CommonDialog.Builder(ctx,false);
        commonBuilder.setTitle(title)
            .setMessage(content)
            .setNegativeButton(cancel, listener)
            .setPositiveButton(ok, listener);
        CommonDialog dialog = commonBuilder.create();
        return dialog;
	}
	
	
}
