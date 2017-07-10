package com.android.mzbook.sortview.optimized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.mzbook.sortview.model.BookShelfModel;
import com.android.mzbook.sortview.optimized.DragGridLayout.OnItemDragAndDropListener;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.android.mzbook.sortview.optimized.DragGridLayout;;
/**
 * 书架拖动布局
 * @author WANGGUODONG 
 * time:2014 -7 17
 */
public class FolderView extends View  implements OnItemDragAndDropListener {

	private Context mContext;
	private ViewGroup mPopupLayout;
	private FolderViewContainer mParentView;
	private WindowManager mWinManager;
	private WindowManager.LayoutParams mParams;
	private final int ANIMALTION_TIME = 500;
	private final int HALF_ANIMALTION_TIME = 500;
	private int mSrceenwidth;
	private int mSrceenheigh;

	private ImageView barView;
	private ImageView topView;  
	private ImageView bottomView;
	private View mBackgroundView;
	private ImageView barTopView;
	private ImageView topbgView;
	private EditText foldername;
	private ImageView cover_image;
	private LinearLayout editContent;
	private LinearLayout middleview;

	private int offsety = 0;
	private int actionbarHeight = 0;
	private int statusBarHeight = 0;
	private int DEFAULT_FOLDER_HEIGHT = 0; // 文件夹高度为屏幕的2/3
	private boolean mIsOpened = false;

	private int[] childViewLocation = new int[2];
	private int folderIds;
	private List<BookShelfModel> models;
	// 以下处理拖拽view
	private DragGridLayout mDragGridLayout;
	private ScrollView mDragGridLayoutScrollView;
	private static List<DragItem> mItems = new LinkedList<DragItem>();
	private DragItemAdapter mItemAdapter;

	private boolean isNeedSorted = true;
	private int dismissCount = 0;
	
	private List<BookShelfModel> selectedBooks = new ArrayList<BookShelfModel>();//选中的书籍

	public interface OnFolderClosedListener {
		public void onClosed(List<BookShelfModel> list);
	}

	private OnFolderClosedListener mOnFolderClosedListener;

	// 文件夹关闭回调接口
	public void setmOnFolderClosedListener(
			OnFolderClosedListener mOnFolderClosedListener) {
		this.mOnFolderClosedListener = mOnFolderClosedListener;
	}

	private static FolderView folderView;

	public static FolderView OpenFolder(int actionbarheight, View childView,
			int folderid, Context context, View backgroundView,
			boolean isNeedSorted,List<BookShelfModel> list) {
		if (folderView == null) {
			folderView = new FolderView(actionbarheight, childView, folderid,
					context, backgroundView, isNeedSorted,list);
		}
		return folderView;
	}

	public FolderView(int actionbarheight, View childView, int folderid,
			Context context, View backgroundView, boolean isneedSorted,List<BookShelfModel> list) {
		super(context);
		// mParentView.removeAllViews();
		mContext = context;
		mSrceenwidth = backgroundView.getWidth();
		mSrceenheigh = backgroundView.getHeight();
		mBackgroundView = backgroundView;
		folderIds = folderid;
		childView.getLocationInWindow(childViewLocation);
		isNeedSorted = isneedSorted;
		selectedBooks=list;

		// offsety = childView.getHeight() + childViewLocation[1];//
		// childViewLocation[1]表示子view的左上角顶点y坐标
		offsety = mSrceenheigh / 3;
		MZLog.d("wangguodong", "+++++++++++++当前child点击的下部坐标" + offsety);
		actionbarHeight = actionbarheight;
		statusBarHeight = getStatusBarHeight();
		MZLog.d("wangguodong", "statusBarHeight"+statusBarHeight+"%%%%%%%%%%%^%");
		DEFAULT_FOLDER_HEIGHT = 2 * mSrceenheigh / 3;
		prepareLayout(backgroundView);

	}
	
	public void getData(){
		mItems.clear();
		models = MZBookDatabase.instance.getBooksInFolder(folderIds, -1,LoginUser.getpin());
		Collections.sort(models, new TimeComparator());
		for (int r = 0; r < models.size(); r++) {
			
			boolean isSelected=false;
			
			for(int i=0;i<selectedBooks.size();i++)
			{
				if(selectedBooks.get(i).getId()==models.get(r).getId())
				{	isSelected=true;
					break;
				}
			}
			mItems.add(new DragItem( models.get(r), false,isSelected,-1,true));
		}
	}

	public void prepareLayout(View backgroundView) {

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPopupLayout = (LinearLayout) inflater.inflate(
				R.layout.dragview_folder_grid, null);
		mWinManager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		// 以下处理拖拽view
		
		getData();
		mDragGridLayout = (DragGridLayout) mPopupLayout
				.findViewById(R.id.folderview);
		mDragGridLayoutScrollView = (ScrollView) mPopupLayout
				.findViewById(R.id.scrollView);
		mItemAdapter = new DragItemAdapter(mContext, mItems);
		mDragGridLayout.setAdapter(mItemAdapter);
		mDragGridLayout.setDragAndDropEnable(false);
		mDragGridLayout.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
		
				RelativeLayout mSelectedLayout = (RelativeLayout) view.findViewById(R.id.mSelectedLayout);
				ImageView imageView=(ImageView) view.findViewById(R.id.book_selected_cover);
					
				if (mSelectedLayout.getVisibility() != View.VISIBLE) {			
					imageView.setVisibility(View.VISIBLE);
					mSelectedLayout.setVisibility(View.VISIBLE);
					selectedBooks.add(mItems.get(position).getMo());
				} else {
					imageView.setVisibility(View.INVISIBLE);
					mSelectedLayout.setVisibility(View.INVISIBLE);
					removeSelectedBooks(mItems.get(position).getMo());
				}
			}
				
		});


		// 以上处理拖拽view

		barView = (ImageView) mPopupLayout.findViewById(R.id.barview);
		topView = (ImageView) mPopupLayout.findViewById(R.id.topview);
		bottomView = (ImageView) mPopupLayout.findViewById(R.id.bottomview);
		// middleview=(LinearLayout) mPopupLayout.findViewById(R.id.middleView);
		foldername = (EditText) mPopupLayout.findViewById(R.id.foldername);
		cover_image=(ImageView) mPopupLayout.findViewById(R.id.cover_image);
		editContent=(LinearLayout) mPopupLayout.findViewById(R.id.edit_content);
		String folderNameString = MZBookDatabase.instance.getFolder(folderIds)
				.getFolderName();
		foldername.setText(folderNameString);
		foldername.setSelection(folderNameString.length());
		
		foldername.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				if(!hasFocus)
				{
					saveFolderName(folderIds, foldername.getText().toString());
				}
				
			}
		});
		
		foldername.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				if (arg1 == EditorInfo.IME_ACTION_DONE) {
					saveFolderName(folderIds, foldername.getText().toString());		
					return true;
				}

				return false;
			}
		});

		// 截当前view背景图
		backgroundView.setDrawingCacheEnabled(true);
		Bitmap srceen = backgroundView.getDrawingCache();

		// 截图控件状态栏和actionbar
		Bitmap bar = Bitmap.createBitmap(srceen, 0, statusBarHeight,
				mSrceenwidth, actionbarHeight);
		barView.setImageBitmap(bar);

		// top
		Bitmap top = Bitmap.createBitmap(srceen, 0, statusBarHeight
				+ actionbarHeight, mSrceenwidth, offsety - statusBarHeight
				- actionbarHeight);
		MZLog.d("wangguodong", "+++++++++++++当前上半部分图片高度statusBarHeight:"
				+ statusBarHeight + "actionbarHeight:" + actionbarHeight);
		topView.setImageBitmap(top);
		// 截图控件以下部分
		if (UiStaticMethod.hasSmartBar()) {
			Bitmap bottom = Bitmap
					.createBitmap(srceen, 0, offsety, mSrceenwidth,
							mSrceenheigh - offsety - dip2px(mContext, 48));
			bottomView.setImageBitmap(bottom);
		} else {
			Bitmap bottom = Bitmap.createBitmap(srceen, 0, offsety,
					mSrceenwidth, mSrceenheigh - offsety);
			bottomView.setImageBitmap(bottom);
		}

		mParams = new WindowManager.LayoutParams();
		mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
		mParams.format = PixelFormat.RGBA_8888;
		// mParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
		mParams.gravity = Gravity.CENTER;
		mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		mParams.height = WindowManager.LayoutParams.MATCH_PARENT;

		// 最底层的top部分
		mParentView = new FolderViewContainer(mContext);
		mParentView.setBackgroundColor(Color.WHITE);
		mParentView.addView(mPopupLayout);
		mWinManager.addView(mParentView, mParams);

		// 顶部ActionBar层
		barTopView = new ImageView(mContext);
		barTopView.setImageBitmap(bar);
		// addNewBarView();

		startOpenAnimation();

	}

	public void saveFolderName(int id, String name) {
		MZBookDatabase.instance.updateFolder(id, name);

	}

	public void addNewBarView() {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
		params.format = PixelFormat.RGBA_8888;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		params.gravity = Gravity.TOP;
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWinManager.addView(barTopView, params);

	}

	private void startOpenAnimation() {

//		Animation animation = new TranslateAnimation(0, 0, 0,
//				-(offsety - mSrceenheigh / 3));
//		animation.setDuration(ANIMALTION_TIME);
//		animation.setInterpolator(new DecelerateInterpolator());
//		animation.setFillAfter(true);
//		// animation.setAnimationListener(mOpenAnimationListener);
//		// topView.startAnimation(animation);
//
//		Animation anim1 = new TranslateAnimation(0, 0, 0,
//				-(offsety - mSrceenheigh / 3));
//		anim1.setDuration(ANIMALTION_TIME);
//		anim1.setInterpolator(new DecelerateInterpolator());
//		anim1.setFillAfter(true);
		// mDragGridLayout.startAnimation(anim1);
		
		Animation fadein = new AlphaAnimation(0, 1.0f);
		fadein.setDuration(HALF_ANIMALTION_TIME);
		fadein.setInterpolator(new DecelerateInterpolator());
		fadein.setFillAfter(true);
		// mDragGridLayout.startAnimation(anim1);
		cover_image.startAnimation(fadein);
		editContent.startAnimation(fadein);
		

		Animation anim = new TranslateAnimation(0, 0, 0, mSrceenheigh - offsety+statusBarHeight);
		anim.setDuration(HALF_ANIMALTION_TIME);
		anim.setInterpolator(new DecelerateInterpolator());
		anim.setFillAfter(true);
		anim.setAnimationListener(mOpenAnimationListener);
		bottomView.startAnimation(anim);
	}

	// 获得状态栏高度
	public int getStatusBarHeight() {
		Class<?> c = null;
		Object obj = null;
		java.lang.reflect.Field field = null;
		int x = 0;
		int statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = mContext.getResources().getDimensionPixelSize(x);
			return statusBarHeight;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusBarHeight;
	}

	// dip to px
	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public boolean removeFloderView() {
		if (mWinManager != null) {
			// mWinManager.removeView(barTopView);
			mWinManager.removeView(mParentView);
			return true;
		} else {
			return false;
		}
	}

	public void dismiss() {

		if (!mIsOpened) {
			return;
		}
		
		foldername.setFocusable(false);

//		Animation animation = new TranslateAnimation(0, 0,
//				-(offsety - mSrceenheigh / 3), 0);
//		animation.setDuration(ANIMALTION_TIME);
//		animation.setInterpolator(new DecelerateInterpolator());
//		animation.setFillAfter(true);
//
//		// topView.startAnimation(animation);
//
//		Animation anim1 = new TranslateAnimation(0, 0,
//				-(offsety - mSrceenheigh / 3), 0);
//		anim1.setDuration(ANIMALTION_TIME);
//		anim1.setInterpolator(new DecelerateInterpolator());
//		anim1.setFillAfter(true);
//
//		// middleview.startAnimation(anim1);
		
		Animation fadeout = new AlphaAnimation(1.0f, 0);
		fadeout.setDuration(HALF_ANIMALTION_TIME);
		fadeout.setInterpolator(new DecelerateInterpolator());
		fadeout.setFillAfter(true);
		cover_image.startAnimation(fadeout);
		editContent.startAnimation(fadeout);

		Animation anim = new TranslateAnimation(0, 0, mSrceenheigh - offsety, 0);
		anim.setDuration(ANIMALTION_TIME);
		anim.setInterpolator(new DecelerateInterpolator());
		anim.setFillAfter(true);
		anim.setAnimationListener(mClosedAnimationListener);
		bottomView.startAnimation(anim);

	}

	class FolderViewContainer extends FrameLayout {

		long lasttime = 0;
		boolean isvalid = false;

		public FolderViewContainer(Context context) {
			super(context);
		}


		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
					&& event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getRepeatCount() == 0) {
				dismiss();
				return true;
			}
			return super.dispatchKeyEvent(event);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			final int y = (int) event.getY();
			final int x = (int) event.getX();

			if (System.currentTimeMillis() - lasttime > ANIMALTION_TIME) {
				isvalid = true;
			} else {
				isvalid = false;
			}
			lasttime = System.currentTimeMillis();
			if ((event.getAction() == MotionEvent.ACTION_DOWN) && isvalid
					&& y < offsety) {
				dismiss();
				return true;
			} else {
				return super.onTouchEvent(event);
			}

		}

	}

	private Animation.AnimationListener mOpenAnimationListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {

			mIsOpened = true;
			dismissCount = 0;
		}
	};

	private Animation.AnimationListener mClosedAnimationListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
			if (mOnFolderClosedListener != null) {
				mOnFolderClosedListener.onClosed(selectedBooks);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {

			removeFloderView();
			mBackgroundView.setDrawingCacheEnabled(false);
			mIsOpened = false;
			folderView = null;


		}
	};


	public void removeSelectedBooks(BookShelfModel model){
		
		for(int i=0;i<selectedBooks.size();i++)
		{
			
			if(model.getId()==selectedBooks.get(i).getId())
			{
				selectedBooks.remove(i);
				break;
			}
		}
	}
	


//	@Override
//	public void onDropItem(int from, int to, boolean isSorted,
//			boolean isNotMoveOut) {
//		if (from != to) {
//			if (mItems.get(from).isFolder() || isSorted || !isNotMoveOut) {
//				MZLog.d("wangguodong", "拖动的是文件夹，或者被排序了 移动位置");
//
//				Item from_item = mItems.get(from);
//				Item target_item = mItems.get(to);
//
//				if (to == 0) {
//					BookShelfModel froModel = from_item.getMo();
//					froModel.setModifiedTime(target_item.getMo()
//							.getModifiedTime() + 1);
//					models.set(from, froModel);
//					Collections.sort(models, new TimeComparator());
//					MZBookDatabase.instance.updateBookshelfTime(froModel);
//				} else if (to == mItems.size() - 1) {
//
//					BookShelfModel froModel = from_item.getMo();
//					froModel.setModifiedTime(target_item.getMo()
//							.getModifiedTime() - 1);
//					models.set(from, froModel);
//					Collections.sort(models, new TimeComparator());
//					MZBookDatabase.instance.updateBookshelfTime(froModel);
//				} else {
//
//					Item befour_item = mItems.get(to - 1);
//					Item after_item = mItems.get(to);
//
//					BookShelfModel targetBefour = befour_item.getMo();
//					BookShelfModel targetAfter = after_item.getMo();
//
//					double centerTime = (targetBefour.getModifiedTime() + targetAfter
//							.getModifiedTime()) / 2.0f;
//
//					MZLog.d("wangguodong", "排序后的中间时间" + centerTime);
//
//					BookShelfModel froModel = from_item.getMo();
//					froModel.setModifiedTime(centerTime);
//
//					models.set(from, froModel);
//					Collections.sort(models, new TimeComparator());
//					MZBookDatabase.instance.updateBookshelfTime(froModel);
//				}
//
//				mItems.add(to, mItems.remove(from));
//
//			}
//			getData();
//			mItemAdapter.notifyDataSetChanged();
//
//		}
//	}

	class TimeComparator implements Comparator<BookShelfModel> {

		@Override
		public int compare(BookShelfModel lhs, BookShelfModel rhs) {
			double time1 = (double) lhs.getModifiedTime();
			double time2 = (double) rhs.getModifiedTime();
			// 降序排列
			if (time1 < time2)
				return 1;
			if (time1 > time2)
				return -1;
			return 0;
		}

	}

	
//	@Override
//	public void onDragOutLayout(int from) {
//
//		dismissCount++;
//		if (dismissCount < 2) {
//			
//			removeSelectedBooks(mItems.get(from).getMo());
//			dismiss();
//			MZLog.d("wangguodong", "移出文件，重新添加到书架");
//			BookShelfModel fromModel = models.get(from);
//			fromModel.setBelongDirId(-1);
//			fromModel.setModifiedTime(System.currentTimeMillis());
//			MZBookDatabase.instance.updateBookshelfFolder(fromModel);
//			
//		}
//
//	}

	

	@Override
	public boolean isFolder(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onItemSwap(int from, int to) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemMergeToFolder(int from, int to) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemMoveToFolder(int from, int to) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public int getRightPosition(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	

	@Override
	public void onDragOutLayout(int from) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragToDelBookView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragOutDelBookView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemMoveEnd(int from) {
		// TODO Auto-generated method stub
		
	}


}
