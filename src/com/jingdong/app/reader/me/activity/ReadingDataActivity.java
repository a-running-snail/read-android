package com.jingdong.app.reader.me.activity;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.me.adapter.BookNoteAdapter;
import com.jingdong.app.reader.me.controller.BookNoteController;
import com.jingdong.app.reader.me.controller.ItemClickCallback;
import com.jingdong.app.reader.me.model.BookNoteModelInterface;
import com.jingdong.app.reader.me.model.BookNoteModelInterface.TYPE;
import com.jingdong.app.reader.me.model.ReadingDataModel;
import com.jingdong.app.reader.me.model.ReadingDataModel.Event;
import com.jingdong.app.reader.me.model.RemoteBookNoteModel;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.core.BookNoteInterface;
import com.jingdong.app.reader.timeline.model.core.RenderBody;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.BorrowHelper;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ReadingDataActivity extends BaseActivityWithTopBar implements
		Observer, OnClickListener {

	private static class MyHandler extends Handler {
		private WeakReference<ReadingDataActivity> reference;

		public MyHandler(ReadingDataActivity activity) {
			reference = new WeakReference<ReadingDataActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			ReadingDataActivity activity = reference.get();
			if (activity != null) {
				Event[] events = ReadingDataModel.Event.values();
				switch (events[msg.what]) {
				case INIT_LOAD:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						activity.setViews();
						activity.header.setVisibility(View.VISIBLE);
						activity.bookNoteM.setBookId(activity.readingDataM
								.getBookId());
						activity.bookNoteM.setUserId(activity.readingDataM
								.getUserId());
						activity.callback.setBook(activity.readingDataM
								.getBook());
						activity.callback.setUser(activity.readingDataM
								.getUser());
						activity.controller.loadInitBookComment();
					} else if (msg.arg1 == ObservableModel.FAIL_INT)
						Toast.makeText(activity, R.string.loading_fail,
								Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}

	}

	private class Task implements Runnable {
		private ReadingDataModel.Event type;

		public Task(ReadingDataModel.Event type) {
			this.type = type;
		}

		@Override
		public void run() {
			switch (type) {
			case INIT_LOAD:
				readingDataM.loadReadingData(userId, book_id, documentId + "");
				break;
			}
		}
	}

	public final static String READING_DATA_URL = "readingData";
	private int scrollState;
	private String title;
	// private String readingDataUrl;
	private ListView list;
	private View header;
	private View bookBar;
	// private TextView readTime;
	// private TextView timeFactor;
	// private TextView noteNumber;
	private TextView bookName;
	private TextView bookAuthor;
	// private Button borrow;
	private ImageView bookCover;
	private RatingBar ratingBar;
	private ReadingDataModel readingDataM;
	private RemoteBookNoteModel bookNoteM;
	private BookNoteController controller;
	private ItemClickCallback callback;
	private BaseAdapter adapter;
	private Handler myHandler;
	private Executor executor;
	private boolean isDocument = false;

	private long documentId = -1;
	private String userId = "";
	private String book_id;
	private RatingBar starBar;
	private TextView bookNote;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reading_data);
		initField();
		initViews();
		this.registerForContextMenu(list);
		// getActionBar().setTitle(title);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		executor.execute(new Task(ReadingDataModel.Event.INIT_LOAD));
		initBookNote();
	}

	@Override
	protected void onDestroy() {
		readingDataM.deleteObserver(this);
		bookNoteM.deleteObserver(controller);
		controller.deleteObserver(this);
		controller.removeCallbacksAndMessages();
		myHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		WrapperListAdapter myAdapter = (WrapperListAdapter) list.getAdapter();
		if (myAdapter.getItemViewType(info.position) == BookNoteAdapter.NOTE) {
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				Object item = myAdapter.getItem(info.position);
				if (((RenderBody) item).getUserID().equals(
						LocalUserSetting.getUser_id(ReadingDataActivity.this))) {
					inflater.inflate(R.menu.reading_data, menu);
					if (((BookNoteInterface) item).isPrivate())
						menu.removeItem(R.id.make_private);
					else
						menu.removeItem(R.id.make_public);
				}
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		WrapperListAdapter myAdapter = (WrapperListAdapter) list.getAdapter();
		final RenderBody renderBody = (RenderBody) myAdapter
				.getItem(info.position);
		switch (item.getItemId()) {
		case R.id.delete:
			popDialog(renderBody);
			return true;
		case R.id.make_public:
			executor.execute(controller.new Task(TYPE.MAKE_PUBLIC, renderBody
					.getId(), renderBody.isPrivate()));
			executor.execute(new Task(ReadingDataModel.Event.INIT_LOAD));
			initBookNote();
			return true;
		case R.id.make_private:
			executor.execute(controller.new Task(TYPE.MAKE_PRIVATE, renderBody
					.getId(), renderBody.isPrivate()));
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.borrow:
			BorrowHelper borrowHelper = new BorrowHelper(this,
					readingDataM.getBookName(), readingDataM.getDocumentId(),
					readingDataM.getUserId());
			borrowHelper.show();
			break;
		case R.id.book_bar:
			if (!isDocument) {
				Intent intent = new Intent();
				intent.setClass(this, BookInfoNewUIActivity.class);
				intent.putExtra(BookInfoNewUIActivity.BookIdKey,
						readingDataM.getBookId());
				startActivity(intent);
			}
			break;
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof Message)
			myHandler.sendMessage((Message) data);
		else if (data instanceof Integer)
			scrollState = (Integer) data;
	}

	private void popDialog(final RenderBody renderBody) {
		UiStaticMethod
				.createConfirmDialog(this, R.string.delete_entity,
						R.string.delete_note_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								executor.execute(controller.new Task(
										BookNoteModelInterface.TYPE.DELETE,
										renderBody.getId()));
								Resources resources = getResources();
								ProgressDialog progressDialog = ProgressDialog
										.show(ReadingDataActivity.this,
												resources
														.getString(R.string.delete),
												resources
														.getString(R.string.deleting),
												true, false);
								controller.setProgressDialog(progressDialog);
							}
						}).create().show();
	}

	private void initField() {
		Intent intent = getIntent();
		userId = intent.getStringExtra("user_id");
		book_id = intent.getStringExtra("book_id");
		isDocument = intent.getBooleanExtra("is_document", false);
		if (book_id == null) {
			book_id = "";
		}
		if (isDocument) {
			documentId = intent.getLongExtra("documentId", -1);
		}

		list = (ListView) findViewById(R.id.list);
		readingDataM = new ReadingDataModel(this);
		myHandler = new MyHandler(this);
		executor = NotificationService.getExecutorService();
		readingDataM.addObserver(this);
	}

	private void initBookNote() {
		if (isDocument) {
			bookNoteM = new RemoteBookNoteModel(this, userId, documentId, true);
		} else {
			bookNoteM = new RemoteBookNoteModel(this);
		}

		adapter = new BookNoteAdapter(this, bookNoteM);
		callback = new ItemClickCallback(readingDataM.getUser(),
				readingDataM.getBook());
		controller = new BookNoteController(bookNoteM, list, adapter, callback);
		controller.addObserver(this);
		bookNoteM.addObserver(controller);
		list.setAdapter(adapter);
	}

	private void initViews() {
		findViews();
		bookCover.setImageResource(R.drawable.bg_default_cover);
		list.addHeaderView(UiStaticMethod.getFooterParent(this, header));
		list.setVisibility(View.VISIBLE);
		header.setVisibility(View.GONE);
	}

	private void findViews() {
		header = View
				.inflate(this, R.layout.header_activity_reading_data, null);
		bookBar = header.findViewById(R.id.book_bar);
		starBar = (RatingBar) header.findViewById(R.id.rating);
		bookNote = (TextView) header.findViewById(R.id.bookNote);
		// borrow = (Button) header.findViewById(R.id.borrow);
		// readTime = (TextView) header.findViewById(R.id.readTime);
		// timeFactor = (TextView) header.findViewById(R.id.timeFactor);
		// noteNumber = (TextView) header.findViewById(R.id.noteNbumber);
		bookName = (TextView) header.findViewById(R.id.bookName);
		bookAuthor = (TextView) header.findViewById(R.id.bookAuthor);
		ratingBar = (RatingBar) header.findViewById(R.id.rating);
		bookCover = (ImageView) header.findViewById(R.id.bookCover);
	}

	private void setViews() {
		// if (readingDataM.getBook().isBorrowable()) {
		// borrow.setVisibility(View.VISIBLE);
		// borrow.setOnClickListener(this);
		// } else {
		// borrow.setVisibility(View.GONE);
		// }

		setTime();
		// bookBar.setOnClickListener(this);
		if (!isDocument)
			bookName.setText(readingDataM.getBookName());
		else {
			bookName.setText(readingDataM.getDocument().getTitle());
		}
		String author = ("null".equals(readingDataM.getBookAuthor())
				|| "".equals(readingDataM.getBookAuthor()) || readingDataM
				.getBookAuthor() == null) ? getString(R.string.author_unknown)
				: readingDataM.getBookAuthor();
		bookAuthor.setText(author);
		UiStaticMethod.setRatingBar(starBar,null, (float) readingDataM.getRating());
		// noteNumber.setText(String.valueOf(readingDataM.getNoteCounts()));
		if (!UiStaticMethod.isEmpty(readingDataM.getBookCover())) {
			ImageLoader.getInstance().displayImage(readingDataM.getBookCover(),
					bookCover, GlobalVarable.getCutBookDisplayOptions());
		}

		bookBar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(ReadingDataActivity.this,
						BookInfoNewUIActivity.class);
				intent.putExtra(BookInfoNewUIActivity.BookIdKey,
						readingDataM.getBookId());
				startActivity(intent);
			}
		});
	}

	private void setTime() {
		long timeInSec = readingDataM.getReadingSeconds();
		if (timeInSec >= 3600) {
			float time = timeInSec / 3600f;
			String timeText = String.format(Locale.getDefault(), "%.1f", time);
			bookNote.setText("阅读" + timeText + "小时,笔记"
					+ readingDataM.getNoteCounts() + "条");
			// timeFactor.setText(getResources().getString(R.string.hour));
			// readTime.setText(timeText);
		} else if (timeInSec >= 60 && timeInSec < 3600) {
			long time = timeInSec / 60;
			bookNote.setText("阅读" + String.valueOf(time) + "分钟,笔记"
					+ readingDataM.getNoteCounts() + "条");
			// timeFactor.setText(getResources().getString(R.string.minute));
			// readTime.setText(String.valueOf(time));
		} else {
			bookNote.setText("阅读" + String.valueOf(timeInSec) + "秒,笔记"
					+ readingDataM.getNoteCounts() + "条");
			// timeFactor.setText(getResources().getString(R.string.second));
			// readTime.setText(String.valueOf(timeInSec));
		}
	}

}
