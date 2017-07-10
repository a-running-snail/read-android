package com.jingdong.app.reader.notes;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ThemeUtils;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.TopBarView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AlreadyAddedNotesActivity extends BaseActivityWithTopBar {

	public static final String REMOVE_NOTES_ACTION_DONE = "remove_notes_action_done";
	public static final String BOOK_ID_KEY = "ebookid";
	public static final String DOC_ID_KEY = "documentid";
	public static final String DOC_SIGN_KEY = "bookSign";

	private long ebookid = 0;
	private int docid = 0;
	private String bookSign = null;

	private List<NotesModel> list = new ArrayList<NotesModel>();
	private ListView listView = null;
	private NotesAdapter adapter = null;
	private TextView removeButton;
	private TextView backButton;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		ThemeUtils.prepareTheme(this);
		setContentView(R.layout.activity_already_added_notes);
		ebookid = getIntent().getLongExtra(BOOK_ID_KEY, 0);
		docid = getIntent().getIntExtra(DOC_ID_KEY, 0);
		bookSign = getIntent().getStringExtra(DOC_SIGN_KEY);
		TopBarView topbar = this.getTopBarView();
        topbar.setTopBarTheme(ThemeUtils.getTopbarTheme());
        topbar.setTitle(getString(R.string.select_notes_for_remove));
        topbar.setLeftMenuVisiable(false, 0);
        removeButton = (TextView) findViewById(R.id.remove);
        backButton = (TextView) findViewById(R.id.cancel);
		listView = (ListView) findViewById(R.id.all_notes);
		adapter = new NotesAdapter();
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				NotesModel model = list.get(position);
				if (model.state == NotesModel.UNSELECTED) {
					updateItemData(position, NotesModel.SELECTED);
				} else if (model.state == NotesModel.SELECTED) {
					updateItemData(position, NotesModel.UNSELECTED);
				}
				setupRemoveButtonState();
			}
		});

		removeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteModel();
			}
		});

		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void deleteModel() {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).state == NotesModel.SELECTED) {
				MZBookDatabase.instance.deletImportNotes(LoginUser.getpin(),
						ebookid, docid, list.get(i).userid);
			}
		}
		updateView();
		Intent it = new Intent();
		it.setAction(REMOVE_NOTES_ACTION_DONE);
		LocalBroadcastManager.getInstance(AlreadyAddedNotesActivity.this)
				.sendBroadcast(it);
	}

	public void updateView(){
		list = MZBookDatabase.instance.listAllNotesModel(
				LoginUser.getpin(), ebookid, docid);
		adapter.notifyDataSetChanged();
		if (list.size() <= 0) {
			listView.setVisibility(View.GONE);
		} else {
			listView.setVisibility(View.VISIBLE);
		}
		setupRemoveButtonState();
	}
	
	private void setupRemoveButtonState() {
		boolean isRemoveButtonDisable = true;
		for (NotesModel m : list) {
			if (m.state == NotesModel.SELECTED) {
				isRemoveButtonDisable = false;
				break;
			}
		}
		if (isRemoveButtonDisable) {
			setupRemoveButtonDisableUI();
		} else {
			setupRemoveButtonEnableUI();
		}
	}
	
	private void setupRemoveButtonEnableUI() {
		TypedArray a = this.obtainStyledAttributes(
				new int[] { R.attr.r_theme });
		removeButton.setTextColor(a.getColor(0, this
				.getResources().getColor(R.color.r_theme)));
		removeButton.setEnabled(true);
	}
	
	private void setupRemoveButtonDisableUI() {
		TypedArray a = this.obtainStyledAttributes(
				new int[] { R.attr.r_text_disable });
		removeButton.setTextColor(a.getColor(0, this
				.getResources().getColor(R.color.r_text_disable)));
		removeButton.setEnabled(false);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateView();
	}

	public void updateItemData(int position, int state) {
		if (list != null && list.size() > position) {

			NotesModel model = list.get(position);
			model.state = state;
			list.set(position, model);
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}

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

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(
						AlreadyAddedNotesActivity.this).inflate(
						R.layout.item_community_notes_remove, null);
			}

			RoundNetworkImageView avatar = ViewHolder.get(convertView, R.id.thumb_nail);
			ImageView itemSelect = ViewHolder.get(convertView, R.id.item_select_image);
			ImageView label = ViewHolder.get(convertView, R.id.avatar_label);
			TextView userName = ViewHolder.get(convertView, R.id.userName);
			TextView notesText = ViewHolder.get(convertView, R.id.notesText);
			View bottomDivider = ViewHolder.get(convertView, R.id.bottom_divider);

			NotesModel model = list.get(position);

			ImageLoader.getInstance().displayImage(model.avatarUrl,avatar, GlobalVarable.getDefaultAvatarDisplayOptions(false));
             

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

			notesText.setText(String.format(
					getString(R.string.item_local_notes_description),
					model.noteCount));

			TypedArray a = AlreadyAddedNotesActivity.this.obtainStyledAttributes(
					new int[] { R.attr.read_list_item_unselected_img,
							R.attr.read_list_item_selected_img});
			switch (model.state) {
			case NotesModel.UNSELECTED:
				itemSelect.setImageResource(a.getResourceId(0,
						R.drawable.reader_icon_list_unselected_standard));
				break;
			case NotesModel.SELECTED:
				itemSelect.setImageResource(a.getResourceId(1,
						R.drawable.reader_icon_list_selected_standard));
				break;
			default:
				itemSelect.setImageResource(a.getResourceId(0,
						R.drawable.reader_icon_list_unselected_standard));
				break;
			}

			return convertView;
		}

	}
}
