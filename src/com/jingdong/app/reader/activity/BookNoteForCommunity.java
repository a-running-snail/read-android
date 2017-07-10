package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.notes.AllNotesActivity;
import com.jingdong.app.reader.notes.AlreadyAddedNotesActivity;
import com.jingdong.app.reader.notes.NotesModel;
import com.jingdong.app.reader.plugin.pdf.outline.OutlineItem;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ThemeUtils;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BookNoteForCommunity extends CommonFragment {

	private View switchOnDot;
	private View switchOffDot;
	private View importedText;
	private TextView editButton;
	private ImageView switchLine;
	private String userId;
	private String booksign;
	private long ebookid;
	private int docid;
	private boolean isShowAllNotes;
	private UserNotesAdapter adapter;
	private ArrayList<OutlineItem> outlineList;
	private List<NotesModel> list = new ArrayList<NotesModel>();
	
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(AllNotesActivity.IMPORT_NOTES_ACTION_DONE)) {
				updateView();
			} else if (intent.getAction().equals(AlreadyAddedNotesActivity.REMOVE_NOTES_ACTION_DONE)) {
				updateView();
			}
		}
		
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ThemeUtils.prepareTheme(getActivity());
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.activity_community_note, null);
		LinearLayout header = (LinearLayout) inflater.inflate(
				R.layout.view_community_note_listview_header, null);
		userId = LoginUser.getpin();
		Intent intent = getActivity().getIntent();
		outlineList = intent.getParcelableArrayListExtra(BookNoteListActivity.TOCLabelListKey);
		booksign = intent.getStringExtra(BookNoteActivity.DOCUMENT_SIGN);
		ebookid = intent.getLongExtra(BookNoteActivity.EBOOK_ID, 0);
		docid = intent.getIntExtra(BookNoteActivity.DOCUMENT_ID, 0);
		isShowAllNotes = MZBookDatabase.instance.isShowAllNotes(userId, ebookid, docid);
		ListView noteList = (ListView) layout.findViewById(R.id.community_note_list);
		noteList.addHeaderView(header);
		adapter = new UserNotesAdapter();
		noteList.setAdapter(adapter);
		importedText = header.findViewById(R.id.imported_text);
		LinearLayout showNotes = (LinearLayout) header.findViewById(R.id.show_all_notes);
		LinearLayout importNotes = (LinearLayout) header.findViewById(R.id.import_notes);
		switchOnDot = showNotes.findViewById(R.id.switchOn_dot);
		switchOffDot = showNotes.findViewById(R.id.switchOff_dot);
		switchLine = (ImageView) showNotes.findViewById(R.id.switch_line);
		editButton = (TextView) layout.findViewById(R.id.edit);
		View backButton = layout.findViewById(R.id.back);
		
		setupSwitchImage();
		updateView();
		
		noteList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				NotesModel model = (NotesModel) parent.getAdapter().getItem(position);
				if(null == model) {
					return;
				}
				Intent intent = new Intent(getActivity(), BookNoteListActivity.class);
				if (ebookid != 0) {
					intent.putExtra(BookNoteListActivity.EBOOK_ID, ebookid);
				} else if (docid != 0) {
					intent.putExtra(BookNoteListActivity.DOCUMENT_ID, docid);
				} else {
					return;
				}
				intent.putExtra(BookNoteListActivity.TOCLabelListKey, outlineList);
				intent.putExtra(BookNoteListActivity.USER_ID, model.userid);
				getActivity().startActivityForResult(intent, BookNoteActivity.ShowNoteListRequest);
			}
		});
		showNotes.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isShowAllNotes = !isShowAllNotes;
				setupSwitchImage();
				MZBookDatabase.instance.updateShowAllNotes(userId, ebookid, docid, isShowAllNotes);
			}
		});
		
		importNotes.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent itIntent = new Intent(getActivity(),
						AllNotesActivity.class);
				if (ebookid != 0) {
					itIntent.putExtra(AlreadyAddedNotesActivity.BOOK_ID_KEY, ebookid);
				} else if (booksign != null) {
					itIntent.putExtra(AlreadyAddedNotesActivity.DOC_SIGN_KEY, booksign);
					itIntent.putExtra(AlreadyAddedNotesActivity.DOC_ID_KEY, docid);
				}
				getActivity().startActivity(itIntent);
			}
		});
		
		editButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent itIntent = new Intent(getActivity(),
						AlreadyAddedNotesActivity.class);
				if (ebookid != 0) {
					itIntent.putExtra(AlreadyAddedNotesActivity.BOOK_ID_KEY, ebookid);
				} else if (booksign != null) {
					itIntent.putExtra(AlreadyAddedNotesActivity.DOC_SIGN_KEY, booksign);
					itIntent.putExtra(AlreadyAddedNotesActivity.DOC_ID_KEY, docid);
				}
				getActivity().startActivity(itIntent);
			}
		});
		
		backButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		registerReceiver();
		return layout;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unregisterReceiver();
	}
	
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
        filter.addAction(AllNotesActivity.IMPORT_NOTES_ACTION_DONE);
        filter.addAction(AlreadyAddedNotesActivity.REMOVE_NOTES_ACTION_DONE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(myReceiver, filter);
	}
	
	private void unregisterReceiver() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(myReceiver);
	}
	
	private void setupSwitchImage() {
		TypedArray a = getActivity().obtainStyledAttributes(new int[] {
				R.attr.read_switch_on_line_img,
				R.attr.read_switch_off_line_img});
		if (isShowAllNotes) {
			switchOnDot.setVisibility(View.VISIBLE);
			switchOffDot.setVisibility(View.GONE);
			switchLine.setImageResource(a.getResourceId(0,
					R.drawable.switchon_line_standard));
		} else {
			switchOnDot.setVisibility(View.GONE);
			switchOffDot.setVisibility(View.VISIBLE);
			switchLine.setImageResource(a.getResourceId(1,
					R.drawable.switchoff_line_standard));
		}
	}
	
	public void updateView() {
		list = MZBookDatabase.instance.listAllNotesModel(LoginUser.getpin(),
				ebookid, docid);
		adapter.notifyDataSetChanged();

		if (list.size() <= 0) {
			importedText.setVisibility(View.GONE);
			setupDeleteButtonDisableUI();
		} else {
			importedText.setVisibility(View.VISIBLE);
			setupDeleteButtonEnableUI();
		}
	}
	
	private void setupDeleteButtonEnableUI() {
		TypedArray a = getActivity().obtainStyledAttributes(
				new int[] { R.attr.r_text_main });
		editButton.setTextColor(a.getColor(0, this
				.getResources().getColor(R.color.r_text_main)));
		editButton.setEnabled(true);
	}
	
	private void setupDeleteButtonDisableUI() {
		TypedArray a = getActivity().obtainStyledAttributes(
				new int[] { R.attr.r_text_disable });
		editButton.setTextColor(a.getColor(0, this
				.getResources().getColor(R.color.r_text_disable)));
		editButton.setEnabled(false);
	}
	
	class UserNotesAdapter extends BaseAdapter {

		private LayoutInflater localInflater;

		public UserNotesAdapter() {
			localInflater = ThemeUtils.getThemeInflater(getActivity(), null);
		}
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = localInflater.inflate(
						R.layout.item_community_notes_import, null);
			}
			RoundNetworkImageView avatar = ViewHolder.get(convertView, R.id.thumb_nail);
			ImageView label = ViewHolder.get(convertView, R.id.avatar_label);
			TextView userName = ViewHolder.get(convertView, R.id.userName);
			TextView notesText = ViewHolder.get(convertView, R.id.notesText);
			View bottomDivider = ViewHolder.get(convertView, R.id.bottom_divider);
			
			NotesModel model = list.get(position);

            ImageLoader.getInstance().displayImage(model.avatarUrl, avatar, GlobalVarable.getDefaultAvatarDisplayOptions(false));
            userName.setText(model.userName);

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
            if (position == list.size() - 1) {
            	bottomDivider.setVisibility(View.VISIBLE);
            } else {
            	bottomDivider.setVisibility(View.GONE);
            }
            notesText.setText(String.format(getString(R.string.item_notes_description), model.noteCount));
            
            return convertView;
		}
		
	}
}
