package com.jingdong.app.reader.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.mzbook.sortview.model.BookShelfModel;
import com.android.mzbook.sortview.model.Folder;
import com.android.mzbook.sortview.model.SelectedModel;
import com.android.mzbook.sortview.optimized.DragGridLayout;
import com.android.mzbook.sortview.optimized.DragGridLayout.OnItemDragAndDropListener;
import com.baidu.mobstat.StatService;
import com.android.mzbook.sortview.optimized.DragItem;
import com.android.mzbook.sortview.optimized.DragItemAdapter;
import com.android.mzbook.sortview.optimized.FolderView;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.book.EBook;
import com.jingdong.app.reader.client.DownloadHelper;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.io.StoragePath;
import com.jingdong.app.reader.timeline.actiivity.TimelinePostTweetActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineSearchPeopleActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.FileGuider;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.ShareDocHelper;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.CustomProgreeDialog;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;


public class OrderingBookCaseActivity extends BaseActivityWithTopBar implements OnItemDragAndDropListener{

	private static List<DragItem> mItems = new LinkedList<DragItem>();
	private DragItemAdapter mItemAdapter;
	private DragGridLayout mDragGridLayout;

	private List<BookShelfModel> lsit = new ArrayList<BookShelfModel>();
	private int actionbarHeight;
	private FolderView folView;

	private List<SelectedModel> selectedBooks = new ArrayList<SelectedModel>();
	
	private IWXAPI weChatApi;
	private String privateMsgReceiver ="";
	private Document sharedDocument;
	private String sharedMsg;
	private static final int USERLIST_ACTIVITY = 101;
	private static final int MESSAGE_ACTIVITY = 102;
	public static final int UPLOAD_ACTIVITY = 103;

	private ProgressDialog mProgressDialog;
	private List<Folder> folders = new ArrayList<Folder>();
	private  Dialog dialog1=null;
	
	private LinearLayout deleteLayout=null;
	private LinearLayout shareLayout=null;
	private LinearLayout movetoLayout=null;
	private RelativeLayout bottomLayout =null;
	
	private TextView deleteTv;
	private TextView moveTv;
	
	
	public void updateData() {
		mItems.clear();
		lsit = MZBookDatabase.instance.listBookShelf(LoginUser.getpin(),0);

		folders.clear();
		folders = MZBookDatabase.instance
				.getAllFolder(LoginUser.getpin());

		Collections.sort(lsit, new TimeComparator());
		for (int r = 0; r < lsit.size(); r++) {
			if (lsit.get(r).getBookType().equals("folder")) {
				int count = -1;
				boolean isSelected = false;

				for (int i = 0; i < selectedBooks.size(); i++) {
					if (selectedBooks.get(i).getId() == lsit.get(r).getBookid()
							&& selectedBooks.get(i).getType().equals("folder")) {

						if (selectedBooks.get(i).getList().size() > 0) {
							isSelected = true;
							count = selectedBooks.get(i).getList().size();
							break;
						}

					}
				}
				mItems.add(new DragItem(lsit.get(r), true, isSelected, count,true));
			} else {
				int count = -1;
				boolean isSelected = false;
				if (lsit.get(r).getBookName() == null) {
					continue;
				}
				for (int i = 0; i < selectedBooks.size(); i++) {
					if (selectedBooks.get(i).getId() == lsit.get(r).getId()
							&& selectedBooks.get(i).getType().equals("book")) {
						isSelected = true;
						break;
					}
				}
				
				mItems.add(new DragItem( lsit.get(r), false, isSelected, count,true));
			}
		}
		
		updateBottomBarStatus();

		//refreshActionbarTitle();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dragview_activity_layout);

		mProgressDialog = CustomProgreeDialog.instance(OrderingBookCaseActivity.this,
				getString(R.string.string_dialog_delete_info));

		weChatApi = WXAPIFactory.createWXAPI(this, com.jingdong.app.reader.view.SharePopupWindow.WXAPP_ID);

		mDragGridLayout = (DragGridLayout) findViewById(R.id.draglayout);
		mItemAdapter = new DragItemAdapter(this, mItems);
		mDragGridLayout.setDragAndDropEnable(false);
		mDragGridLayout.setAdapter(mItemAdapter);

		mDragGridLayout.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
					long arg3) {
				
				MZLog.d("wangguodong", "on item click");

				actionbarHeight = TopBarView.getTopBarHeightPix(OrderingBookCaseActivity.this);
				ImageView imageView = (ImageView) arg1.findViewById(R.id.book_selected_cover);
				RelativeLayout mSelectedLayout = (RelativeLayout) arg1.findViewById(R.id.mSelectedLayout);
				if (mItems.get(arg2).isFolder()) {

					List<BookShelfModel> oldSelectedData = new ArrayList<BookShelfModel>();
					for (int i = 0; i < selectedBooks.size(); i++) {
						MZLog.d("wangguodong", "数据" + selectedBooks.get(i).getId());
						if (selectedBooks.get(i).getId() == mItems.get(arg2).getMo()
								.getBookid()
								&& selectedBooks.get(i).getType().equals("folder")) {
							MZLog.d("wangguodong", "找到指定文件夹数据");
							oldSelectedData = selectedBooks.get(i).getList();
							break;
						}
					}

					folView = FolderView.OpenFolder(actionbarHeight, arg1,
							mItems.get(arg2).getMo().getBookid(),
							OrderingBookCaseActivity.this, getWindow().getDecorView(),
							true, oldSelectedData);

					folView.setmOnFolderClosedListener(new FolderView.OnFolderClosedListener() {
						@Override
						public void onClosed(List<BookShelfModel> list) {

							// 获取选中的书籍
							SelectedModel model = new SelectedModel();
							model.setId(mItems.get(arg2).getMo().getBookid());
							model.setType("folder");
							model.setList(list);

							if (list.size() > 0)
								updateSelectedModel(model);
							else
								removeSelectedModel(model);

							updateData();
							mItemAdapter.notifyDataSetChanged();

						}
					});
				} else {

				//	MZLog.d("wangguodong", "点击部分不是文件夹吗？"+mItems.get(arg2).isFolder());
					SelectedModel model = new SelectedModel();
					model.setId(mItems.get(arg2).getMo().getId());
					model.setType("book");
					List<BookShelfModel> temp = new ArrayList<BookShelfModel>();
					temp.add(mItems.get(arg2).getMo());
					model.setList(temp);

//					if (imageView.getVisibility() != View.VISIBLE) {
//						mItems.get(arg2).setSelected(true);
//						imageView.setVisibility(View.VISIBLE);
//						updateSelectedModel(model);
//						//refreshActionbarTitle();
//
//					} else {
//						mItems.get(arg2).setSelected(false);
//						imageView.setVisibility(View.INVISIBLE);
//						removeSelectedModel(model);
//						//refreshActionbarTitle();
//					}
					
					if (mSelectedLayout.getVisibility() != View.VISIBLE) {
						mItems.get(arg2).setSelected(true);
						imageView.setVisibility(View.VISIBLE);
						mSelectedLayout.setVisibility(View.VISIBLE);
						updateSelectedModel(model);
						//refreshActionbarTitle();

					} else {
						mItems.get(arg2).setSelected(false);
						imageView.setVisibility(View.INVISIBLE);
						mSelectedLayout.setVisibility(View.INVISIBLE);
						removeSelectedModel(model);
						//refreshActionbarTitle();
					}

				}			
			}
		});
		
		
		
		MZLog.d("wangguodong", "清理无用的空文件夹");
		MZBookDatabase.instance.clearFolder();
		
		//2015-2-2 添加底部状态栏 wangguoddong
		
		deleteLayout=(LinearLayout) findViewById(R.id.delete);
		shareLayout=(LinearLayout) findViewById(R.id.share);
		deleteTv = (TextView) findViewById(R.id.delete_tv);
		moveTv  = (TextView) findViewById(R.id.move_tv);
		shareLayout.setVisibility(View.GONE);
		movetoLayout=(LinearLayout) findViewById(R.id.moveto);
		bottomLayout =(RelativeLayout) findViewById(R.id.bottom_layout);
		
		bottomLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//防止后面View响应点击事件 
				
			}
		});
		
		deleteLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(selectedBooks.size()>0){
					DialogManager.showCommonDialog(OrderingBookCaseActivity.this,"提示",getString(R.string.warings_content), "确定", "取消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								mProgressDialog.show();
								new MyDeleteTask().execute();
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								
								break;
							default:
								break;
							}
							dialog.dismiss();
						}
					}); 
					
				}
					
				else {
			
					Toast.makeText(OrderingBookCaseActivity.this, getString(R.string.no_delete_selected), Toast.LENGTH_LONG).show();
				}	
			}
		});
		
		shareLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (selectedBooks != null && selectedBooks.size() > 0) {
					final List<BookShelfModel> list = getAllBookShelfModels();
					if (list.size() > 1)
						Toast.makeText(OrderingBookCaseActivity.this,
								getString(R.string.string_upload_error),
								Toast.LENGTH_SHORT).show();
					else if (list.size() == 1
							&& list.get(0).getBookType().equals("ebook")) {
						Toast.makeText(OrderingBookCaseActivity.this,
								getString(R.string.string_selected_3rdbook),
								Toast.LENGTH_SHORT).show();
					} else if (list.size() == 1
							&& list.get(0).getBookType().equals("document")) {

						  PopupWindow  menuWindow = new SharePopupWindow(OrderingBookCaseActivity.this, new OnShareItemClickedListener() {
								
								@Override
								public void onShareItemClicked(int type) {
								
									switch (type) {
									case 101://wechat
										
										Document doc = MZBookDatabase.instance.getDocument(list
												.get(0).getBookid());

										final WXAppExtendObject appdata = new WXAppExtendObject();
										
										String booksource= doc.bookSource;
										
										if(TextUtils.isEmpty(booksource))
										{
											Toast.makeText(OrderingBookCaseActivity.this, "书籍路径有问题，无法分享，请重新下载！", Toast.LENGTH_LONG).show();
											return ;
										}
									
										File fileDir = new File(booksource);
										MZLog.d("wangguodong", booksource);
										
										if (fileDir.exists()&&fileDir.isFile() && (fileDir.getName().endsWith(".epub")|| fileDir.getName().endsWith(".pdf"))) {
					
											MZLog.d("wangguodong", "22222222");
											if (fileDir != null) {
												appdata.filePath = fileDir.getPath();
												MZLog.d("wangguodong", fileDir.getPath());
												try {
													JSONObject o = new JSONObject();
													o.put("File Name", doc.title + ".epub");
													o.put("File Extension", "EPUB");
													appdata.extInfo = o.toString();
													final WXMediaMessage msg = new WXMediaMessage();
													if (!TextUtils.isEmpty(doc.coverPath)) {
														msg.setThumbImage(IOUtil.extractThumbNail(
																doc.coverPath, 75, 113, true));
													}
													msg.title = doc.title;
													msg.description = getString(R.string.can_open_by_mzread);
													msg.mediaObject = appdata;
													MZLog.d("wangguodong", "44444444");
													SendMessageToWX.Req req = new SendMessageToWX.Req();
													req.transaction = buildTransaction("appdata");
													req.message = msg;
													req.scene = SendMessageToWX.Req.WXSceneSession;
													weChatApi.sendReq(req);
												} catch (JSONException e) {
													e.printStackTrace();
													MZLog.d("wangguodong", "333333");
												}

											}
											
										}
										else {
											Toast.makeText(OrderingBookCaseActivity.this, "您要分享的文件不存在！", Toast.LENGTH_LONG).show();
										}
					
										break;

									case 102://upload

												Document doc1 = MZBookDatabase.instance.getDocument(list
														.get(0).getBookid());

												Intent intent = new Intent(OrderingBookCaseActivity.this,
														UploadActivity.class);

												if(TextUtils.isEmpty(doc1.opfMD5))
													{
													Toast.makeText(OrderingBookCaseActivity.this, "此书无法上传...", Toast.LENGTH_LONG).show();
													return ;
													}
												
												intent.putExtra("document", doc1);

												OrderingBookCaseActivity.this.startActivity(intent);
										
										break;
									}
									
								}
							},true); 
				            menuWindow.showAtLocation(OrderingBookCaseActivity.this.findViewById(R.id.main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
						
					} else {
						Toast.makeText(OrderingBookCaseActivity.this,
								getString(R.string.selectbooksfirst),
								Toast.LENGTH_SHORT).show();
					}
				
				}}
		});
		movetoLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (selectedBooks == null || selectedBooks.size() == 0) {
					Toast.makeText(OrderingBookCaseActivity.this,
							getString(R.string.selectbooksfirst),
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				showDialog();
				
				
			}
		});
		
		updateBottomBarStatus();
	}

	public void showDialog() {

		FrameLayout layout = (FrameLayout) LayoutInflater.from(
				OrderingBookCaseActivity.this).inflate(
				R.layout.bookshelf_order_dialog, null);
		ScrollView scrollView=(ScrollView) layout.findViewById(R.id.scrollView);
		if(folders.size()>=4)
		{
			LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			params.height=ScreenUtils.dip2px(OrderingBookCaseActivity.this, 300);
			scrollView.setLayoutParams(params);
		}

		LinearLayout contentLayout = (LinearLayout) layout
				.findViewById(R.id.view_content);
		for (int i = 0; i < folders.size() + 2; i++) {
			
			LinearLayout childlayout = (LinearLayout) LayoutInflater.from(
					OrderingBookCaseActivity.this).inflate(
					R.layout.item_bookshelf_ordering, null);

			ImageView view = (ImageView) childlayout
					.findViewById(R.id.item_icon);
			TextView nameTextView = (TextView) childlayout
					.findViewById(R.id.item_name);
			if (i == 0) {
				
				
				view.setImageResource(R.drawable.bookshelf_arrangebook_bookshelf);
				nameTextView
						.setText(getString(R.string.bookshelf_ordering_move_to_desktop));
				
				childlayout.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						List<BookShelfModel> list=getAllBookShelfModels();

						if (list != null && list.size() > 0) {
							for (int i = 0; i < list.size(); i++) {
								BookShelfModel model = list.get(i);
								if (model.getBelongDirId() != -1) {
									model.setBelongDirId(-1);
									model.setModifiedTime(System.currentTimeMillis());
									MZBookDatabase.instance
											.updateBookshelfFolder(model);
								}
							}
						}
						selectedBooks.clear();
						//refreshActionbarTitle();
						updateData();
						mItemAdapter.notifyDataSetChanged();
						dialog1.dismiss();
						
					}
				});
				
			} else if (i == 1) {
				view.setImageResource(R.drawable.bookshelf_arrangebook_addfolder);
				nameTextView
						.setText(getString(R.string.bookshelf_ordering_move_to_new_folder));
				
				childlayout.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {

						final LinearLayout layout = (LinearLayout) getLayoutInflater()
								.inflate(R.layout.dialog_inner_view, null);
						final EditText text = (EditText) layout.findViewById(R.id.confirm_book_name);
						text.setText(getString(R.string.bookshelf_folder_default_name));
						
						text.setOnClickListener((android.view.View.OnClickListener) new android.view.View.OnClickListener(){
							
							@Override
							public void onClick(android.view.View arg0) {
								text.setText("");
							}
						});
						new AlertDialog.Builder(OrderingBookCaseActivity.this)
						.setView(layout)
						.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog1.dismiss();
							}
						})
						.setPositiveButton(getString(R.string.ok), new OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								String foldername = text.getText().toString();
								if (foldername.replace(" ", "").equals("")) {
									
									Toast.makeText(
											OrderingBookCaseActivity.this,
											getString(R.string.string_input_error),
											Toast.LENGTH_LONG).show();
									return;
								}
								int id =MZBookDatabase.instance.createFolder(foldername,
										System.currentTimeMillis(),LoginUser.getpin());
								List<BookShelfModel> list=getAllBookShelfModels();
								
								for (int i = 0; i < list.size(); i++) {
									BookShelfModel model = list.get(i);
									if (model.getBelongDirId() != id) {
										model.setBelongDirId(id);
										model.setModifiedTime(System.currentTimeMillis());
										MZBookDatabase.instance
												.updateBookshelfFolder(model);
									}
								}
								
								selectedBooks.clear();
							//	refreshActionbarTitle();
								updateData();
								mItemAdapter.notifyDataSetChanged();
								dialog1.dismiss();
								
							}
						})
						.setTitle("新文件夹名称:")
						.create().show();
						
					}
				});
				
				
			}

			else {
				final int pos=i;
				view.setImageResource(R.drawable.bookshelf_arrangebook_folder);
				nameTextView.setText(folders.get(i - 2).getFolderName());
				childlayout.setOnClickListener(new View.OnClickListener(){
					
					@Override
					public void onClick(View v) {

						int id = folders.get(pos-2).getFolderId();
						List<BookShelfModel> list=getAllBookShelfModels();
						
						for (int i = 0; i < list.size(); i++) {
							BookShelfModel model = list.get(i);
							if (model.getBelongDirId() != id) {
								model.setBelongDirId(id);
								model.setModifiedTime(System.currentTimeMillis());
								MZBookDatabase.instance
										.updateBookshelfFolder(model);
							}
						}
						
						selectedBooks.clear();
						//refreshActionbarTitle();
						updateData();
						mItemAdapter.notifyDataSetChanged();
						dialog1.dismiss();
						
					}
					
				});
				
				
			}

			if(i==0)
			{
				List<BookShelfModel> list11=getAllBookShelfModels();

				boolean justAbook=true;
				
				if (list11 != null && list11.size() > 0) {
					for (int k = 0; k < list11.size(); k++) {
						BookShelfModel model = list11.get(i);
						if (model.getBelongDirId() != -1) {
							justAbook=false;
							break;
						}
					}
				}
		
				if(!justAbook)
					contentLayout.addView(childlayout);
				
			}
			
			else {
				contentLayout.addView(childlayout);
			}
			
		}

		dialog1 = new AlertDialog.Builder(OrderingBookCaseActivity.this).setView(
				layout).create();
		

		dialog1.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		selectedBooks.clear();
		//refreshActionbarTitle();
		updateData();
		mItemAdapter.notifyDataSetChanged();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_zhengli));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_zhengli));
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		mItemAdapter.notifyDataSetChanged();
		super.onPostCreate(savedInstanceState);
	}

/*
	public void refreshActionbarTitle() {
		String countString = getResources().getString(R.string.selectedcount);
		String title = getString(R.string.reorganize);
		if (selectedBooks != null && selectedBooks.size() > 0) {
			title = String.format(countString, getAllBookShelfModels().size());
		}
	//	ActionBarHelper.customActionBarBack(this, title);
	}
*/
	
	public void deleteBooks() {
		if (selectedBooks != null && selectedBooks.size() > 0) {

			String userId = LoginUser.getpin();
			List<BookShelfModel> list = getAllBookShelfModels();
			List<EBook> ebookidList = new ArrayList<EBook>();
			List<Integer> documentidList = new ArrayList<Integer>();
			
			for (int itemPosition = 0; itemPosition < list.size(); itemPosition++) {
				if (list.get(itemPosition).getBookType().equals("ebook")) {
					
					EBook ebook = MZBookDatabase.instance.getEBook(list.get(
							itemPosition).getBookid());
	
					// 先暂停下载 然后继续删除
					LocalBook localBook = LocalBook.getLocalBookByIndex(list.get( itemPosition).getBookid());
					if (null!=localBook&&localBook.state == LocalBook.STATE_LOADING) {
						MZLog.d("wangguodong", "暂停正在下载的任务");
						DownloadHelper.stopDownload(OrderingBookCaseActivity.this, BookShelfModel.EBOOK,localBook);
					}
					
					
					ebookidList.add(ebook);
					try {
						FileGuider savePath = new FileGuider(
								FileGuider.SPACE_PRIORITY_EXTERNAL);
						savePath.setImmutable(true);
						savePath.setChildDirName("/epub/" + ebook.bookId);

						File fileDir = new File(savePath.getParentPath());

						MZLog.d("wangguodong", fileDir.getAbsolutePath());

						if (fileDir.exists()) {
							IOUtil.deleteFile(fileDir);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (list.get(itemPosition).getBookType().equals("document")) {
					Document doc = MZBookDatabase.instance.getDocument(list
							.get(itemPosition).getBookid());
					
					LocalDocument localDocument = LocalDocument.getLocalDocument(list.get( itemPosition).getBookid(), LoginUser.getpin());

					if (null!=localDocument&&localDocument.state == LocalBook.STATE_LOADING) {
						MZLog.d("wangguodong", "暂停正在下载的任务");

						// 先暂停下载 然后继续删除
						DownloadHelper.stopDownload(OrderingBookCaseActivity.this, BookShelfModel.DOCUMENT, localDocument);
						
					}
					
					
					
					documentidList.add(doc.documentId);
				
						File fileDir = new File(
								StoragePath
										.getDocumentDir(OrderingBookCaseActivity.this),
								String.valueOf(doc.documentId));
						if (fileDir.exists()) {
							IOUtil.deleteFile(fileDir);
						}
					

				}

			}

			if (ebookidList.size() > 0) {
				Long[] ebooks = new Long[ebookidList.size()];
				for (int i = 0; i < ebookidList.size(); i++)

				{
					if(ebookidList.get(i)!=null)
						ebooks[i] = ebookidList.get(i).bookId;
				}
				
				Integer[] index = new Integer[ebookidList.size()];
				for (int i = 0; i < ebookidList.size(); i++)

				{
					if(ebookidList.get(i)!=null)
						index[i] = ebookidList.get(i).ebookId;
				}

				MZBookDatabase.instance.deleteEbook(userId,index, ebooks);
		
			}

			if (documentidList.size() > 0) {
				Integer[] documents = new Integer[documentidList.size()];
				for (int i = 0; i < documentidList.size(); i++)

				{
					documents[i] = Integer.parseInt(documentidList.get(i)
							.toString());
				}

				MZBookDatabase.instance.deleteDocumentRecord(documents, userId);
			
			}

		}

		selectedBooks.clear();

	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case USERLIST_ACTIVITY:
			if (resultCode == TimelineSearchPeopleActivity.CLICK_USER_NAME) {
				UserInfo userInfo = data
						.getParcelableExtra(TimelineSearchPeopleActivity.USER_PARCELABLE);
				privateMsgReceiver = userInfo.getId();
				Resources resources = getResources();
				Intent intent = new Intent(OrderingBookCaseActivity.this,
						TimelinePostTweetActivity.class);
				intent.putExtra(TimelinePostTweetActivity.SHOW_AT, false);
				intent.putExtra(TimelinePostTweetActivity.TITLE,
						resources.getString(R.string.private_msg_share_book));
				startActivityForResult(intent, MESSAGE_ACTIVITY);
			}
			break;
		case MESSAGE_ACTIVITY:
			if (resultCode == TimelinePostTweetActivity.POST_TWEET_WORDS) {
				sharedMsg = data
						.getStringExtra(TimelinePostTweetActivity.TWEET_CONTENT);
				ShareDocHelper shareDocHelper = new ShareDocHelper(this,
						privateMsgReceiver, sharedDocument, sharedMsg);
				shareDocHelper.shareDoc();
			}
			break;
		case UPLOAD_ACTIVITY:
			if (resultCode == Activity.RESULT_OK) {
				ShareDocHelper shareDocHelper = new ShareDocHelper(this,
						privateMsgReceiver, sharedDocument, sharedMsg);
				shareDocHelper.shareDoc();
			} else {
				Toast.makeText(this, R.string.upload_fail, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void startActivitySelectUser(Document sharedDocument) {
		this.sharedDocument = sharedDocument;
		Intent intent = new Intent(this, TimelineSearchPeopleActivity.class);
		startActivityForResult(intent, USERLIST_ACTIVITY);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.dropdown_button:
			//actionBarHelper.createArrangePopupMenu(this,
			//		this.findViewById(R.id.actionbar_overlay));
			return true;
		case R.id.delete:
			if(selectedBooks.size()>0)
				new AlertDialog.Builder(OrderingBookCaseActivity.this)
					.setTitle(getString(R.string.warings))
					.setMessage(getString(R.string.warings_content))
					.setPositiveButton(getString(R.string.ok),
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									mProgressDialog.show();
									new MyDeleteTask().execute();
								}
							})
					.setNegativeButton(getString(R.string.cancel), null)
					.create().show();
			else {
				new AlertDialog.Builder(OrderingBookCaseActivity.this)
				.setTitle(getString(R.string.info))
				.setMessage(getString(R.string.no_delete_selected))
				.create().show();
			}	
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	class MyDeleteTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			deleteBooks();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			super.onPostExecute(result);
			updateData();
			mItemAdapter.notifyDataSetChanged();
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
		}
	}


	public int getHeight() {
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		return metric.heightPixels;
	}

	

	public boolean isItemSelected(int id) {
		for (int i = 0; i < selectedBooks.size(); i++) {
			if (selectedBooks.get(i).getId() == id
					&& selectedBooks.get(i).getType().equals("book")) {
				return true;
			}
		}
		return false;
	}

	public int isContainsSelectedModel(SelectedModel mo) {

		for (int i = 0; i < selectedBooks.size(); i++) {
			if (selectedBooks.get(i).getId() == mo.getId()
					&& ((mo.getType().equals("folder") && selectedBooks.get(i)
							.getType().equals("folder")) || (!mo.getType()
							.equals("folder") && selectedBooks.get(i).getType()
							.equals("book")))) {
				return i;
			}
		}

		return -1;
	}

	public void removeSelectedModel(SelectedModel mo) {

		int position = isContainsSelectedModel(mo);

		if (position > -1) {
			selectedBooks.remove(position);
		}
		
		if(shareLayout!=null&&selectedBooks!=null&&selectedBooks.size()==1)
		{
			//TODO 隐藏分享入口
			shareLayout.setVisibility(View.GONE);
		}
		else {
			shareLayout.setVisibility(View.GONE);
		}
		
		updateBottomBarStatus();
	}

	public void updateSelectedModel(SelectedModel mo) {

		
		int position = isContainsSelectedModel(mo);
		if (position > -1) {
			selectedBooks.remove(position);
			selectedBooks.add(mo);
		}

		else {
			selectedBooks.add(mo);
		}
		
		if(shareLayout!=null&&selectedBooks!=null && selectedBooks.size()==1)
		{
			//TODO 隐藏分享入口
			shareLayout.setVisibility(View.GONE);
		}
		else {
			shareLayout.setVisibility(View.GONE);
		}
		
		updateBottomBarStatus();
	}

	public List<BookShelfModel> getSelectedModel(int id, String type) {

		for (int i = 0; i < selectedBooks.size(); i++) {
			if (selectedBooks.get(i).getId() == id
					&& selectedBooks.get(i).getType().equals(type)) {
				return selectedBooks.get(i).getList();
			}
		}
		return new ArrayList<BookShelfModel>();

	}

	public List<BookShelfModel> getAllBookShelfModels() {
		List<BookShelfModel> list = new ArrayList<BookShelfModel>();
		for (int i = 0; i < selectedBooks.size(); i++) {
			list.addAll(selectedBooks.get(i).getList());
		}

		return list;
	}

/*
	@Override
	public void onDropItem(int from, int to, boolean isSorted,
			boolean isNotMoveOut) {
		

		if (from != to) {

			if (!mItems.get(from).isFolder() && mItems.get(to).isFolder()
					&& !isSorted && isNotMoveOut) {
				MZLog.d("wangguodong", "目标是文件夹，未排序，变成文件夹 删除拖动文件");
				BookShelfModel fBookShelfModel = mItems.get(from).getMo();
				boolean isSelected = isItemSelected(mItems.get(from).getMo()
						.getId());
				mItems.remove(from);
				if (lsit.get(to).getBookType().equals("folder")) {
					int folderid = lsit.get(to).getBookid();
					BookShelfModel fromModel = lsit.get(from);
					fromModel.setBelongDirId(folderid);
					MZBookDatabase.instance.updateBookshelfFolder(fromModel);
					if (isSelected) {
						// 处理选中状态 合并
						SelectedModel fModel = new SelectedModel();
						fModel.setId(fBookShelfModel.getId());
						fModel.setType("book");
						List<BookShelfModel> temp1 = new ArrayList<BookShelfModel>();
						temp1.add(fBookShelfModel);
						fModel.setList(temp1);

						removeSelectedModel(fModel);

						SelectedModel folderModel = new SelectedModel();
						folderModel.setId(folderid);
						folderModel.setType("folder");
						List<BookShelfModel> temp3 = getSelectedModel(folderid,
								"folder");
						temp3.add(fBookShelfModel);
						folderModel.setList(temp3);
						updateSelectedModel(folderModel);
					}
					// 处理选中状态 合并
				}

			} else if (!mItems.get(from).isFolder()
					&& !mItems.get(to).isFolder() && !isSorted && isNotMoveOut) {
				MZLog.d("wangguodong", "目标不是文件夹，未排序，变成文件夹 删除拖动文件");

				int folderid = MZBookDatabase.instance.createFolder(lsit
						.get(to).getModifiedTime(), LoginUser.getpin());
				BookShelfModel fromModel = lsit.get(from);
				fromModel.setBelongDirId(folderid);
				MZBookDatabase.instance.updateBookshelfFolder(fromModel);

				BookShelfModel toModel = lsit.get(to);
				toModel.setBelongDirId(folderid);
				MZBookDatabase.instance.updateBookshelfFolder(toModel);

				boolean isFromSelected = isItemSelected(mItems.get(from)
						.getMo().getId());
				boolean isToSelected = isItemSelected(mItems.get(to).getMo()
						.getId());
				mItems.remove(from);
				// 修改目标为文件夹
				if (isFromSelected || isToSelected) {

					// 处理选中状态 合并
					SelectedModel fModel = new SelectedModel();
					fModel.setId(fromModel.getId());
					fModel.setType("book");
					List<BookShelfModel> temp1 = new ArrayList<BookShelfModel>();
					temp1.add(fromModel);
					fModel.setList(temp1);

					SelectedModel tooModel = new SelectedModel();
					tooModel.setId(toModel.getId());
					tooModel.setType("book");
					List<BookShelfModel> temp2 = new ArrayList<BookShelfModel>();
					temp2.add(toModel);
					tooModel.setList(temp2);

					removeSelectedModel(fModel);
					removeSelectedModel(tooModel);

					SelectedModel folderModel = new SelectedModel();
					folderModel.setId(folderid);
					folderModel.setType("folder");
					List<BookShelfModel> temp3 = new ArrayList<BookShelfModel>();
					if (isFromSelected)
						temp3.add(fromModel);
					if (isToSelected)
						temp3.add(toModel);
					folderModel.setList(temp3);
					updateSelectedModel(folderModel);

					// 处理选中状态 合并

				}

			} else if (mItems.get(from).isFolder() || isSorted || !isNotMoveOut) {
				MZLog.d("wangguodong", "拖动的是文件夹，或者被排序了 移动位置");

				Item from_item = mItems.get(from);
				Item target_item = mItems.get(to);

				if (to == 0) {
					BookShelfModel froModel = from_item.getMo();
					froModel.setModifiedTime(target_item.getMo()
							.getModifiedTime() + 1);
					lsit.set(from, froModel);
					Collections.sort(lsit, new TimeComparator());
					MZBookDatabase.instance.updateBookshelfTime(froModel);
				} else if (to == mItems.size() - 1) {

					BookShelfModel froModel = from_item.getMo();
					froModel.setModifiedTime(target_item.getMo()
							.getModifiedTime() - 1);
					lsit.set(from, froModel);
					Collections.sort(lsit, new TimeComparator());
					MZBookDatabase.instance.updateBookshelfTime(froModel);
				} else {

					Item befour_item = mItems.get(to - 1);
					Item after_item = mItems.get(to);

					BookShelfModel targetBefour = befour_item.getMo();
					BookShelfModel targetAfter = after_item.getMo();

					double centerTime = (targetBefour.getModifiedTime() + targetAfter
							.getModifiedTime()) / 2.0f;

					BookShelfModel froModel = from_item.getMo();
					froModel.setModifiedTime(centerTime);

					lsit.set(from, froModel);
					Collections.sort(lsit, new TimeComparator());
					MZBookDatabase.instance.updateBookshelfTime(froModel);
				}

				mItems.add(to, mItems.remove(from));

			}
			updateData();
			mItemAdapter.notifyDataSetChanged();
		}

	}
*/
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


    
    public interface OnShareItemClickedListener{
		 void onShareItemClicked(int type);
}
    public class SharePopupWindow extends PopupWindow {  
    	  
    	  
        private View mMenuView;  
        

      
        public SharePopupWindow(Activity context,final OnShareItemClickedListener itemsOnClick,boolean showCloudDisk) {  
            super(context);  
            LayoutInflater inflater = (LayoutInflater) context  
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            mMenuView = inflater.inflate(R.layout.activity_ordering_popupwindow, null);  
  
            FrameLayout micromsg =(FrameLayout) mMenuView.findViewById(R.id.micromsg);
            FrameLayout uploadToCloud =(FrameLayout) mMenuView.findViewById(R.id.uploadCloud);
            
            if(!showCloudDisk)
            		uploadToCloud.setVisibility(View.GONE);
            
            micromsg.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(itemsOnClick!=null)
						itemsOnClick.onShareItemClicked(101);
					dismiss();
					
				}
			});
            
            uploadToCloud.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(itemsOnClick!=null)
						itemsOnClick.onShareItemClicked(102);
					dismiss();
				}
			});
            
            
            this.setContentView(mMenuView);  
            this.setWidth(LayoutParams.MATCH_PARENT);  
            this.setHeight(LayoutParams.WRAP_CONTENT);  
            this.setFocusable(true);  
            this.setTouchable(true);
            this.setOutsideTouchable(true);
            this.setBackgroundDrawable(new ColorDrawable(OrderingBookCaseActivity.this.getResources().getColor(R.color.bg_menu_shadow)));

      
        }  
      
    }

    /**
     * 改变底部操作栏状态
     */
    private void updateBottomBarStatus(){
    	if(selectedBooks!=null && selectedBooks.size()!=0){
			deleteLayout.setClickable(true);
			deleteTv.setTextColor(getResources().getColor(R.color.text_main));
			movetoLayout.setClickable(true);
			moveTv.setTextColor(getResources().getColor(R.color.text_main));
		}else{
			deleteLayout.setClickable(false);
			deleteTv.setTextColor(getResources().getColor(R.color.text_sub));
			movetoLayout.setClickable(false);
			moveTv.setTextColor(getResources().getColor(R.color.text_sub));
		}
    }
    
    
    
	@Override
	public boolean isFolder(int position) {
		DragItem item = mItems.get(position);
		if (item.isFolder())
			return true;
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
