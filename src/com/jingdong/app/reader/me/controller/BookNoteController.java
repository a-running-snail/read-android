package com.jingdong.app.reader.me.controller;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Future;

import android.R.integer;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jingdong.app.reader.me.model.BookNoteModelInterface;
import com.jingdong.app.reader.me.model.RemoteBookNoteModel;
import com.jingdong.app.reader.me.model.BookNoteModelInterface.TYPE;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.core.BookNoteInterface;
import com.jingdong.app.reader.R;

public class BookNoteController extends Observable implements OnScrollListener, Observer {
	
	public class Task implements Runnable {
		private TYPE type;
		private long noteId;
		private boolean isPrivate;

		public Task(TYPE type) {
			this.type = type;
		}

		public Task(TYPE type, long noteId) {
			this(type);
			this.noteId = noteId;
		}

		public Task(TYPE type, long noteId, boolean isPrivate) {
			this(type, noteId);
			this.isPrivate = isPrivate;
		}

		@Override
		public void run() {
			switch (type) {
			case LOAD:
				page = 1;
				model.loadBookComments(page);
				break;
			case DELETE:
				model.deleteNote(noteId);
				break;
			case MAKE_PRIVATE:
			case MAKE_PUBLIC:
				model.updatePrivacy(noteId, isPrivate, type);
				break;
			case MORE:
				page++;
				model.loadBookComments(page);
				break;
			}
		}
	}

	private static class MyHandler extends Handler {
		private WeakReference<BookNoteController> reference;

		public MyHandler(BookNoteController controller) {
			reference = new WeakReference<BookNoteController>(controller);
		}

		@Override
		public void handleMessage(Message msg) {
			BookNoteController controller = reference.get();
			if (controller != null) {
				TYPE[] types = BookNoteModelInterface.TYPE.values();
				switch (types[msg.what]) {
				case LOAD:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
							controller.model.refreshList();
							controller.adapter.notifyDataSetChanged();
							controller.view.setOnItemClickListener(controller.itemClick);
							controller.view.setOnScrollListener(controller);
						}
					} else {
						Toast.makeText(controller.view.getContext(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
					}
					break;
				case DELETE:
					controller.dialog.dismiss();
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						controller.model.deleteNoteCallBack();
						controller.adapter.notifyDataSetChanged();
					}
					break;
				case MAKE_PRIVATE:
				case MAKE_PUBLIC:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						int index = controller.model.getIndexById(msg.arg2);
						BookNoteInterface bookNoteInterface = controller.model.getBookNoteAt(index);
						bookNoteInterface.setPrivate(!bookNoteInterface.isPrivate());
						controller.adapter.notifyDataSetChanged();
						
					}
					break;
				}
			}
		}
	}

	private int lastItemIndex;
	private int scrollState;
	private ProgressDialog dialog;
	private ListView view;
	private RemoteBookNoteModel model;
	private OnItemClickListener itemClick;
	private BaseAdapter adapter;
	private Handler handler;
	private Future<?> future;
	private int page;

	public BookNoteController(RemoteBookNoteModel model, ListView view, BaseAdapter adapter, OnItemClickListener itemClick) {
		this.model = model;
		this.view = view;
		this.adapter = adapter;
		this.itemClick = itemClick;
		handler = new MyHandler(this);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		lastItemIndex = firstVisibleItem + visibleItemCount - 1 - 1;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		notifyScrollStateChanged(scrollState);
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && lastItemIndex == adapter.getCount() - 1) {
			if (future == null || future.isDone())
				future = NotificationService.getExecutorService().submit(new Task(TYPE.MORE));
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		handler.sendMessage((Message) data);
	}

	public void loadInitBookComment() {
		NotificationService.getExecutorService().submit(new Task(TYPE.LOAD));
	}

	public void setProgressDialog(ProgressDialog dialog) {
		this.dialog = dialog;
	}

	public void removeCallbacksAndMessages(){
		handler.removeCallbacksAndMessages(null);
	}
	
	private void notifyScrollStateChanged(int scrollState) {
		if (this.scrollState != scrollState) {
			this.scrollState = scrollState;
			setChanged();
			notifyObservers(this.scrollState);
		}
	}

}
