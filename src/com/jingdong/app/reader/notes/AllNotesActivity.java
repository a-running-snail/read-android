package com.jingdong.app.reader.notes;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ThemeUtils;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.TopBarView;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AllNotesActivity extends BaseActivityWithTopBar {

	public static final String IMPORT_NOTES_ACTION_DONE = "import_notes_action_done";
	public static final String IMPORT_NOTES_SUCCESS = "import_notes_success";
	private static final int DEFAULT_NOTES_COUNT = 20;
    private List<NotesModel>  list          = new ArrayList<NotesModel>();
    private ListView          listView      = null;
    private NotesAdapter      adapter       = null;
    private long              ebookid       = 0;
    private int               docid         = 0;
    private String            bookSign      = null;

    private final int         INVILID_VALUE = -1;
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(IMPORT_NOTES_SUCCESS)) {

				String identity = intent.getStringExtra("identity");
				if (adapter != null) {
					int index = adapter.getIndexByIdentity(identity);
					if (index != INVILID_VALUE) {
						adapter.updateItemView(index);
					}
				}
				Intent it = new Intent();
				it.setAction(IMPORT_NOTES_ACTION_DONE);
				LocalBroadcastManager.getInstance(AllNotesActivity.this)
						.sendBroadcast(it);
			}

		}
	};

    public void updateItemData(int position, int state) {
        if (list != null && list.size() > position) {
            NotesModel model = list.get(position);
            model.state = state;
            list.set(position, model);
        }
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        ThemeUtils.prepareTheme(this);
        setContentView(R.layout.activity_all_notes);

        ebookid = getIntent().getLongExtra(AlreadyAddedNotesActivity.BOOK_ID_KEY, 0);
        bookSign = getIntent().getStringExtra(AlreadyAddedNotesActivity.DOC_SIGN_KEY);
        docid = getIntent().getIntExtra(AlreadyAddedNotesActivity.DOC_ID_KEY, 0);
        
        if (ebookid == 0 && docid == 0 && TextUtils.isEmpty(bookSign)) {
        	finish();
        	return;
        }

        TopBarView topbar = this.getTopBarView();
        topbar.setTopBarTheme(ThemeUtils.getTopbarTheme());
        topbar.setTitle(getString(R.string.import_others_notes));
        TypedArray a = this.obtainStyledAttributes(new int[] {
				R.attr.read_back_img});
        topbar.setLeftMenuVisiable(true, a.getResourceId(0,
				R.drawable.reader_btn_back_standard));
        listView = (ListView) findViewById(R.id.all_notes);
        adapter = new NotesAdapter();
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(IMPORT_NOTES_SUCCESS);
        LocalBroadcastManager.getInstance(AllNotesActivity.this).registerReceiver(myReceiver, filter);
        requestAllNotes();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(AllNotesActivity.this).unregisterReceiver(myReceiver);
    }

    public void requestAllNotes() {
        String url = URLText.getAllNotesAuthorsOfBook;
        if (ebookid != 0) {
        } else if (!TextUtils.isEmpty(bookSign)) {
        } else {
            Toast.makeText(AllNotesActivity.this, getString(R.string.other_notes_notfound), Toast.LENGTH_SHORT).show();
            return;
        }
        RequestParams request = RequestParamsPool.getAllNotesAuthors(ebookid, bookSign);
        WebRequestHelper.get(url, request, true, new MyAsyncHttpResponseHandler(AllNotesActivity.this) {

			@Override
			public void onResponse(int statusCode, Header[] headers,
					byte[] responseBody) {
				String result = new String(responseBody);
                if (!UiStaticMethod.isEmpty(result)) {
                    try {
                        List<NotesModel> data = new ArrayList<NotesModel>();
                        JSONArray array = new JSONArray(result);
                        if (array != null && array.length() != 0) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                NotesModel model = NotesModel.parseFromJson(object);
                                if (MZBookDatabase.instance.isNotesImported(LoginUser.getpin(), ebookid, docid, model.userid))
                                    model.state = NotesModel.DOWNLOADED;
                                else
                                    model.state = NotesModel.DOWNLOAD;
                                data.add(model);
                            }
                            list = data;
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AllNotesActivity.this, getString(R.string.other_notes_notfound), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(AllNotesActivity.this, getString(R.string.other_notes_notfound), Toast.LENGTH_SHORT).show();
                    }
                }
			}
        	
        });
    }

    class NotesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public int getIndexByIdentity(String identity) {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).userid.equals(identity)) {
                        return i;
                    }
                }
                return INVILID_VALUE;
            } else {
                return INVILID_VALUE;
            }
        }

        public void updateItemView(int index) {
            updateItemData(index, NotesModel.DOWNLOADED);
            int visiblePos = listView.getFirstVisiblePosition();
            int offset = index - visiblePos;
            if (offset < 0)
                return;
            View view = listView.getChildAt(offset);
            if (view != null) {
                View syncContainer = ViewHolder.get(view, R.id.syncContainer);
                syncContainer.setVisibility(View.GONE);
            }
        }

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(AllNotesActivity.this).inflate(R.layout.item_community_notes_sync, null);
            }

            RoundNetworkImageView avatar = ViewHolder.get(convertView, R.id.thumb_nail);
            ImageView label = ViewHolder.get(convertView, R.id.avatar_label);
            TextView username = ViewHolder.get(convertView, R.id.timeline_user_name);
            TextView count = ViewHolder.get(convertView, R.id.timeline_user_summary);
            View syncContainer = ViewHolder.get(convertView, R.id.syncContainer);
            View syncLayout = ViewHolder.get(convertView, R.id.syncLayout);
            View divider = ViewHolder.get(convertView, R.id.divider);
            View bottomDivider = ViewHolder.get(convertView, R.id.bottom_divider);

            NotesModel model = list.get(position);

            ImageLoader.getInstance().displayImage(model.avatarUrl, avatar, GlobalVarable.getDefaultAvatarDisplayOptions(false));

            username.setText(model.userName);

            if (model.role == 1 || model.role == 2) {
                label.setVisibility(View.VISIBLE);
                if (model.role == 1) {
                    label.setImageResource(R.drawable.profile_verify_person);
                } else {
                    label.setImageResource(R.drawable.profile_verify_organization);
                }
            } else {
                label.setVisibility(View.INVISIBLE);
            }
            count.setText(String.format(getString(R.string.item_notes_description), model.noteCount));

            switch (model.state) {
            case NotesModel.DOWNLOAD:
            	setupDownloadState(syncLayout);
                break;
            case NotesModel.DOWNLOADING:
            	syncContainer.setVisibility(View.GONE);
                break;
            case NotesModel.DOWNLOADED:
            	setupSyncState(syncLayout);
                break;
            default:
            	setupDownloadState(syncLayout);
                break;
            }
            
            syncContainer.setOnClickListener(new OnClickListener() {

                @Override
				public void onClick(View v) {
                	v.setEnabled(false);
                	v.setVisibility(View.GONE);
					NotesModel model = list.get(position);
					ImportNotesTask task = new ImportNotesTask(model, ebookid, docid, bookSign);
					task.requestSyncNotes();
				}
            });

            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // do nothing
                }
            });

            if (position == list.size() - 1) {
                divider.setVisibility(View.GONE);
                bottomDivider.setVisibility(View.VISIBLE);
            } else {
                divider.setVisibility(View.VISIBLE);
                bottomDivider.setVisibility(View.GONE);
            }
            return convertView;
        }

        private void setupDownloadState(View layout) {
        	TypedArray a = AllNotesActivity.this.obtainStyledAttributes(new int[] {
    				R.attr.read_round_corner_bg,
    				R.attr.r_theme});
        	layout.setBackgroundDrawable(a.getDrawable(0));
    		TextView textView = (TextView) layout.findViewById(R.id.syncText);
    		textView.setTextColor(a.getColor(1, R.color.r_theme));
    		textView.setText(R.string.importText);
        }
        
        private void setupSyncState(View layout) {
        	TypedArray a = AllNotesActivity.this.obtainStyledAttributes(new int[] {
    				R.attr.read_round_corner_enable_bg,
    				R.attr.r_text_main});
        	layout.setBackgroundDrawable(a.getDrawable(0));
    		TextView textView = (TextView) layout.findViewById(R.id.syncText);
    		textView.setTextColor(a.getColor(1, R.color.r_text_main));
    		textView.setText(R.string.syncText);
        }
    }
    
    class ImportNotesTask {
    	
    	private int pageIndex = 0;
    	private int noteCount = 0;
    	private int documentId = 0;
    	private long ebookId = 0;
    	private long lastSyncTime = 0;
    	private String usersId;
    	private String documentSign;
    	private NotesModel model;
    	
    	ImportNotesTask(NotesModel model, long ebookId, int docId, String docSign) {
    		this.usersId = model.userid;
    		this.ebookId = ebookId;
    		this.documentId = docId;
    		this.documentSign = docSign;
    		this.pageIndex = 1;
    		this.noteCount = 0;
    		this.model = model;
    	}
		
		private void requestSyncNotes() {
			if (!NetWorkUtils.isNetworkConnected(AllNotesActivity.this)
					|| TextUtils.isEmpty(usersId)
					|| (ebookId == 0 && TextUtils.isEmpty(documentSign)))
				return;

			String url = URLText.getSomeoneAllNotes;
			RequestParams request = RequestParamsPool.getSomeoneAllNotes(usersId,
					ebookId, documentSign, pageIndex, DEFAULT_NOTES_COUNT);
			WebRequestHelper.get(url, request, true,
					new MyAsyncHttpResponseHandler(MZBookApplication.getContext()) {

						@Override
						public void onResponse(int statusCode, Header[] headers,
								byte[] responseBody) {
							String result = new String(responseBody);
							List<ReadNote> notes = new ArrayList<ReadNote>();
							try {
								JSONObject resultObject = new JSONObject(result);
								JSONArray notesArr = resultObject
										.optJSONArray("notes");

								if (notesArr != null && notesArr.length() > 0) {
									for (int i = 0; i < notesArr.length(); i++) {
										JSONObject notesObject = notesArr
												.optJSONObject(i);
										ReadNote note = ReadNote.parseFromJson(
												notesObject, documentId);
										note.modified = false;
										lastSyncTime = Math.max(lastSyncTime,
												note.updateTime);
										notes.add(note);
									}

								}

							} catch (JSONException e) {
								e.printStackTrace();
							}

							for (ReadNote nt : notes) {
								MZBookDatabase.instance.insertOrUpdateEbookNote(nt);
							}
							pageIndex++;
							noteCount += notes.size();
							
							model.noteCount = noteCount;
							MZBookDatabase.instance.insertOrUpdateNoteSyncTime(
									LoginUser.getpin(), ebookid, docid, model);

							if (notes.size() > 0) {
								requestSyncNotes();
							} else {
								Intent it = new Intent();
								it.setAction(IMPORT_NOTES_SUCCESS);
								it.putExtra("identity", usersId);
								LocalBroadcastManager.getInstance(
										AllNotesActivity.this)
										.sendBroadcast(it);
							}
						}
					});
		}
    	
    }

}
