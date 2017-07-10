package com.jingdong.app.reader.entity;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.data.db.DataProvider;
import com.jingdong.app.reader.util.Log;

public class BookCategory {
    public final static String DEFAULT_CATEGORY = "全部";
    public final static String MYBUYED_CATEGORY="已购";
    public final static String UNCLASSIFIED_CATEGORY = "未分类";
    public final static String ONLINEREAD_CATEGORY = "我的畅读";
    public final static int BUUTTON_STATUS_DEL = 1;
    public final static int BUUTTON_STATUS_DONE = 0;
    public static Uri uri_category = DataProvider.CONTENT_URI_NAME_BOOKCATEGORY;    
    
    public static ArrayList<BookCategory> getBookCategoryList() {
        ArrayList<BookCategory> categoryList = new ArrayList<BookCategory>();
        String[] projection = {"category","isActive","count"};
        String sortOrder = "id ASC";

        Cursor cur = MZBookApplication.getInstance().getContentResolver()
                .query(uri_category, projection, null, null, sortOrder);
        if (cur == null || cur.getCount() == 0) {
        	/*
            BookCategory defaultCategory = new BookCategory();
            defaultCategory.name = BookCategory.DEFAULT_CATEGORY;
            defaultCategory.isActive = true;
            BookCategory.addBookCategory(defaultCategory);
            categoryList.add(defaultCategory);
            
          //我的畅读列表。
            /*
            BookCategory onlineReadCategory = new BookCategory();
            onlineReadCategory.name = BookCategory.ONLINEREAD_CATEGORY;
            onlineReadCategory.isActive = false;
            BookCategory.addBookCategory(onlineReadCategory);
            categoryList.add(onlineReadCategory);
           */
        	/*
            BookCategory unclassifiedCategory = new BookCategory();
            unclassifiedCategory.name = BookCategory.UNCLASSIFIED_CATEGORY;
            unclassifiedCategory.isActive = false;
            BookCategory.addBookCategory(unclassifiedCategory);
            categoryList.add(unclassifiedCategory);
            
            BookCategory.setActiveCategory(defaultCategory);
             */
        } else {
            while(cur.moveToNext()) {
                BookCategory category = new BookCategory();
                category.name = cur.getString(0);
                category.isActive = cur.getInt(1) == 1 ? true : false;
                category.count=cur.getInt(2);
                categoryList.add(category);
            }
        }
        if (cur != null)
            cur.close();     
        return categoryList;
    }
    
    public static void addBookCategory(BookCategory category) {
        ContentValues values = new ContentValues();
        values.put("category", category.name);
        values.put("isActive", 0);
        MZBookApplication.getInstance().getContentResolver().insert(uri_category, values);
    }
    
    public static void deleteBookCategory(BookCategory category) {
        boolean deleteActiveCategory = category.name.equals(getActiveCategory());
        MZBookApplication.getInstance().getContentResolver().delete(uri_category, "category=?", new String[]{category.name});
        
        changeCategoryName4LocalBookList(category.name, ""/*BookCategory.UNCLASSIFIED_CATEGORY*/);//将该分类中的图书全部标记为未分类。
        updateCategoryCount(BookCategory.UNCLASSIFIED_CATEGORY, category.count, true);
        if (deleteActiveCategory) {
            setActiveCategory(BookCategory.DEFAULT_CATEGORY);
        }
    }
    
    public static void renameBookCategory(BookCategory category, String newCategoryName) {
        ContentValues values = new ContentValues();
        values.put("category", newCategoryName);
        String where ="category=?";
        String[] selectionArgs = {category.name};
        MZBookApplication.getInstance().getContentResolver().update(uri_category, values, where, selectionArgs);
        
        changeCategoryName4LocalBookList(category.name, newCategoryName);
    }
    
    public static void changeCategoryName4LocalBookList(String oldCategoryName, String newCategoryName) {
        ContentValues values = new ContentValues();
        values.put("category", newCategoryName);
        String where ="category=?";
        String[] selectionArgs = {oldCategoryName};
       // MZBookApplication.getInstance().getContentResolver().update(DBHelper.uri, values, where, selectionArgs);
    }
    
    public static boolean isHasTheCategory(String category){
         String[] projection = {"category","isActive"};
    	  String where ="category=?";
          String[] selectionArgs = {category};
          Cursor cursor=MZBookApplication.getInstance().getContentResolver().query(uri_category,projection, where, selectionArgs,null);
          if(cursor.getCount()>0){
        	  return true;
          }
    	return false;
    }
    public static void setActiveCategory(String category) {
        ContentValues values = new ContentValues();
        values.put("isActive", 0);
        MZBookApplication.getInstance().getContentResolver().update(uri_category, values, null, null);
        
        values.clear();
        values.put("isActive", 1);
        String where ="category=?";
        String[] selectionArgs = {category};
        MZBookApplication.getInstance().getContentResolver().update(uri_category, values, where, selectionArgs);
    }
    
    public static void setActiveCategory(BookCategory category) {
        ContentValues values = new ContentValues();
        values.put("isActive", 0);
        MZBookApplication.getInstance().getContentResolver().update(uri_category, values, null, null);
        
        values.clear();
        values.put("isActive", 1);
        String where ="category=?";
        String[] selectionArgs = {category.name};
        MZBookApplication.getInstance().getContentResolver().update(uri_category, values, where, selectionArgs);
    }
    
    public static int getActivityCategoryCount(){
    	int count=0;
        Cursor cursor = MZBookApplication.getInstance().getContentResolver().query(uri_category, new String[]{"count"}, "isActive=?", new String[] {Integer.toString(1)}, null);
        if (cursor != null && cursor.getCount() >= 0 && cursor.moveToNext()) {
        	count = cursor.getInt(0);
            cursor.close();
            cursor=null;
        }
        return count;
    }
    
    public static void setActivityCategoryCount(int count,String category){
    	ContentValues values = new ContentValues();
		values.put("count", count);
		MZBookApplication
				.getInstance()
				.getContentResolver()
				.update(uri_category, values, "category=?",
						new String[] { category });
    }
    
    
    public static String getActiveCategory() {
        String activeCategory = BookCategory.DEFAULT_CATEGORY;
        Cursor cursor=null;
        if(MZBookApplication.getInstance()!=null)
        	 cursor = MZBookApplication.getInstance().getContentResolver().query(uri_category, null, "isActive=?", new String[] {Integer.toString(1)}, null);
        if (cursor != null && cursor.getCount() >= 0 && cursor.moveToNext()) {
            activeCategory = cursor.getString(cursor.getColumnIndex("category"));
            cursor.close();
            cursor=null;
        }
        
        return activeCategory;
    }
    
    /**
     * 修改分类的数量。
     * @param category
     * @param num
     * @param isPlus
     */
    public static void updateAllCategoryCount(String category,int num,boolean isPlus){
    	Log.i("category------------->"+category+"=="+num+"==="+isPlus);
//    	if(category.equals("")){
//    		category=BookCategory.UNCLASSIFIED_CATEGORY;
//    	}
    	updateCategoryCount(category, num, isPlus);
    	if(!category.equals(BookCategory.DEFAULT_CATEGORY)){//如果不是全部分类，需要同时对全部的数量进行调整。
    		updateCategoryCount(BookCategory.DEFAULT_CATEGORY, num, isPlus);
    	} 
    }

	public static synchronized void updateCategoryCount(String category, int num,
			boolean isPlus) {
		Cursor cursor = MZBookApplication
				.getInstance()
				.getContentResolver()
				.query(uri_category, new String[] { "count" }, "category=?",
						new String[] { category }, null);
		if (cursor == null || cursor.getCount() == 0) {
			Log.i("", "cur==null");
		}
		int count = 0;
		if (cursor.moveToNext()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		if (isPlus) {
			count += num;
		} else {
			count -= num;
		}
		if (count < 0) {
			count = 0;
			Log.i("error", "category's count is less than 0");
		}
		ContentValues values = new ContentValues();
		values.put("count", count);
		MZBookApplication
				.getInstance()
				.getContentResolver()
				.update(uri_category, values, "category=?",
						new String[] { category });
	}
    public String name;
    private boolean isActive;
    boolean isModify=false;
    boolean openInput=false;
    int buttonStatus=BUUTTON_STATUS_DEL;
    public int count;

	public boolean isModify() {
		return isModify;
	}

	public void setModify(boolean isModify) {
		this.isModify = isModify;
	}

	public int getButtonStatus() {
		return buttonStatus;
	}

	public void setButtonStatus(int buttonStatus) {
		this.buttonStatus = buttonStatus;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isOpenInput() {
		return openInput;
	}

	public void setOpenInput(boolean openInput) {
		this.openInput = openInput;
	}
}
