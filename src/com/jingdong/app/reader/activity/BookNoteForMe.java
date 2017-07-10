package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.plugin.pdf.outline.OutlineItem;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ThemeUtils;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.ViewHolder;

public class BookNoteForMe extends CommonFragment {
	
	public static final String ACTION_RELOAD_READNOTE = "com.jingdong.app.reader.reload.readnote";
	
	private ExpandableStickyListHeadersListView bookNotesList = null;
	private BookNotesAdapter adapter = null;
	private View nullLayout = null;
	private View bottomEditContainer = null;
	private View bottomContainer = null;
	private TextView editButton = null;
	private TextView deleteButton = null;
	private List<Integer> position = new ArrayList<Integer>();
	private List<ReadNote> listData = new ArrayList<ReadNote>();
	private List<ReadNote> deleteReadNotes = new ArrayList<ReadNote>();
	private ArrayList<OutlineItem> outlineList;
	private boolean isDeleteMode;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ThemeUtils.prepareTheme(getActivity());
		Intent intent = getActivity().getIntent();
		outlineList = intent.getParcelableArrayListExtra(BookNoteActivity.TOCLabelListKey);
		long ebookid = intent.getLongExtra(BookNoteActivity.EBOOK_ID, 0);
		int docid = intent.getIntExtra(BookNoteActivity.DOCUMENT_ID, 0);
		String userId = LoginUser.getpin();
		
		List<ReadNote> readNotes;
		if (docid != 0) {
			readNotes = MZBookDatabase.instance.listDocReadNote(
					userId, docid);
		} else {
			readNotes = MZBookDatabase.instance.listEBookReadNote(
					userId, ebookid);
		}

		LayoutInflater localInflater=ThemeUtils.getThemeInflater(getActivity(), inflater);
		LinearLayout layout = (LinearLayout) localInflater.inflate(
				R.layout.activity_read_note, null);
		
		nullLayout = layout.findViewById(R.id.readnote_null_layout);
		bottomContainer = layout.findViewById(R.id.bottomContainer);
		bottomEditContainer = layout.findViewById(R.id.bottomEditContainer);
		editButton = (TextView) bottomContainer.findViewById(R.id.edit);
		deleteButton = (TextView) bottomEditContainer.findViewById(R.id.delete);
		bookNotesList = (ExpandableStickyListHeadersListView) layout.findViewById(R.id.booknotesList);
		if (readNotes.size() == 0) {
			nullLayout.setVisibility(View.VISIBLE);
			bookNotesList.setVisibility(View.GONE);
			setupEditButtonDisableUI();
		}
		markChapterId(readNotes);
		listData = createData(readNotes);

		adapter = new BookNotesAdapter();
		bookNotesList.setAdapter(adapter);
		bookNotesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ReadNote readnote = listData.get(position);
				if (isDeleteMode) {
					ImageView selectView = ViewHolder.get(view, R.id.item_select_image);
					TypedArray a = getActivity().obtainStyledAttributes(new int[] {
							R.attr.read_list_item_unselected_img,
							R.attr.read_list_item_selected_img});
					if (deleteReadNotes.contains(readnote)) {
						deleteReadNotes.remove(readnote);
						selectView.setImageResource(a.getResourceId(0, R.drawable.reader_icon_list_unselected_standard));
					} else {
						deleteReadNotes.add(readnote);
						selectView.setImageResource(a.getResourceId(1, R.drawable.reader_icon_list_selected_standard));
					}
					
					if (deleteReadNotes.size() > 0) {
						setupDeleteButtonEnableUI();
					} else {
						setupDeleteButtonDisableUI();
					}
					return;
				}
				Intent data = new Intent();
				data.putExtra(BookNoteActivity.CHAPTER_ITEM_REF,
						readnote.spineIdRef);
				data.putExtra(BookNoteActivity.PARA_INDEX,
						readnote.fromParaIndex);
				data.putExtra(BookNoteActivity.OFFSET_IN_PARA,
						readnote.toOffsetInPara);
				getActivity().setResult(
						BookPageViewActivity.RESULT_OK, data);
				getActivity().finish();
				getActivity().overridePendingTransition(
						R.anim.right_in, R.anim.left_out);
			}
		});
		
		bookNotesList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(!isDeleteMode) {
					isDeleteMode = true;
					deleteReadNotes.clear();
					bottomContainer.setVisibility(View.GONE);
					bottomEditContainer.setVisibility(View.VISIBLE);
					adapter.notifyDataSetChanged();
					setupDeleteButtonDisableUI();
				}
				return false;
			}
		});
		
		editButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				if (listData.size() == 0) {
					return;
				}
				isDeleteMode = true;
				deleteReadNotes.clear();
				bottomContainer.setVisibility(View.GONE);
				bottomEditContainer.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
				setupDeleteButtonDisableUI();
			}
		});
		bottomContainer.findViewById(R.id.back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				getActivity().finish();
			}
		});
		deleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				listData.removeAll(deleteReadNotes);
				removeReadNotes(deleteReadNotes);
				setupDeleteButtonDisableUI();
				deleteReadNotes.clear();
				adapter.refresh();
				if (listData.size() == 0) {
					setupEditButtonDisableUI();
					nullLayout.setVisibility(View.VISIBLE);
					bookNotesList.setVisibility(View.GONE);
				}
			}
		});
		bottomEditContainer.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				isDeleteMode = false;
				deleteReadNotes.clear();
				bottomContainer.setVisibility(View.VISIBLE);
				bottomEditContainer.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
			}
		});
		
		return layout;
	}
	
	private void setupEditButtonDisableUI() {
		TypedArray a = getActivity().obtainStyledAttributes(
				new int[] { R.attr.r_text_disable });
		editButton.setTextColor(a.getColor(
				0,
				this.getResources().getColor(
						R.color.r_text_disable)));
	}
	
	private void setupDeleteButtonEnableUI() {
		TypedArray a = getActivity().obtainStyledAttributes(
				new int[] { R.attr.r_theme });
		deleteButton.setTextColor(a.getColor(0, this
				.getResources().getColor(R.color.r_theme)));
	}
	
	private void setupDeleteButtonDisableUI() {
		TypedArray a = getActivity().obtainStyledAttributes(
				new int[] { R.attr.r_text_disable });
		deleteButton.setTextColor(a.getColor(0, this
				.getResources().getColor(R.color.r_text_disable)));
	}
	
	private void markChapterId(List<ReadNote> readNotes) {
		HashMap<String, Integer> chapterIdMap = new HashMap<String, Integer>();
		for (ReadNote note : readNotes) {
			if (chapterIdMap.containsKey(note.chapterName)) {
				int chapterId = chapterIdMap.get(note.chapterName);
				note.chapterId = chapterId;
			} else {
				int chapterId = findChapterId(note.chapterName);
				chapterIdMap.put(note.chapterName, chapterId);
				note.chapterId = chapterId;
			}
		}
	}
	
	private int findChapterId(String chapterTitle) {
		int chapterId = 0;
		for (OutlineItem item : outlineList) {
			if (chapterTitle.equals(item.title)) {
				return chapterId;
			}
			chapterId++;
		}
		return chapterId;
	}

	public List<ReadNote> createData(List<ReadNote> list) {

		Collections.sort(list, new BookNoteComparator());
		List<ReadNote> tempList = new ArrayList<ReadNote>();
		List<ReadNote> result = new ArrayList<ReadNote>();
		position.clear();
		for (int i = 0; i < list.size(); i++) {

			ReadNote temp = list.get(i);
			tempList.clear();
			tempList.add(temp);

			int j = i + 1;
			for (; j < list.size(); j++) {
				if (list.get(i).chapterName.equals(list.get(j).chapterName)) {
					tempList.add(list.get(j));
				} else {
					i = --j;
					break;
				}
			}
			if (j >= list.size())
				i = j;

			//Collections.sort(tempList, new BookNoteTimeComparator());//不要根据时间排序
			position.add(tempList.size());

			result.addAll(tempList);
		}
		return result;
	}
	
	private void removeReadNotes(List<ReadNote> readNotes) {
		if (readNotes != null && readNotes.size() > 0) {
			for (ReadNote note : readNotes) {
				note.deleted = true;
				note.modified = true;
				note.updateTime = System.currentTimeMillis();
				MZBookDatabase.instance.insertOrUpdateEbookNote(note);
			}
			Intent intent = new Intent(ACTION_RELOAD_READNOTE);
			LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
		}
	}

	public int initRange(int k) {

		int oldRange = 0;
		int maxRange = 0;
		for (int i = 0; i < position.size(); i++) {
			maxRange += position.get(i);
			if (k > oldRange && k < maxRange - 1)
				return 0;// 中间
			else if (k == oldRange && maxRange - oldRange > 1) {
				return -1;// 首(有多个元素)
			} else if (k == oldRange && maxRange - oldRange == 1) {
				return -2;// 首(单个元素)
			} else if (k == maxRange - 1) {
				return 1;// 尾
			}

			else {
				oldRange = maxRange;
			}
		}

		return -1;
	}

	class BookNoteComparator implements Comparator<ReadNote> {

		@Override
		public int compare(ReadNote lhs, ReadNote rhs) {

			if (lhs.chapterId < rhs.chapterId)
				return -1;
			else if (lhs.chapterId > rhs.chapterId) {
				return 1;
			} else {

				if (lhs.fromParaIndex < rhs.fromParaIndex)
					return -1;
				else if (lhs.fromParaIndex > rhs.fromParaIndex)
					return 1;

				else
					return 0;

			}

		}

	}

	class BookNoteTimeComparator implements Comparator<ReadNote> {

		@Override
		public int compare(ReadNote lhs, ReadNote rhs) {

			if (lhs.updateTime < rhs.updateTime)
				return 1;
			else if (lhs.updateTime > rhs.updateTime)
				return -1;

			else

				return 0;
		}

	}

	class BookNotesAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {
		
		private List<Integer> sectionIndices;
		private List<String> sectionHeaders;

		public BookNotesAdapter() {
			sectionIndices = getSectionIndices();
			sectionHeaders = getSectionHeaders();
		}
		
		public void refresh() {
			sectionIndices = getSectionIndices();
			sectionHeaders = getSectionHeaders();
			notifyDataSetChanged();
		}

		private List<Integer> getSectionIndices() {
			List<Integer> sectionIndices = new ArrayList<Integer>();
			if (listData == null || listData.size() <= 0) {
				return sectionIndices;
			}
			int lastChapterId = listData.get(0).chapterId;
			sectionIndices.add(0);
			for (int i = 1; i < listData.size(); i++) {
				int chapterId = listData.get(i).chapterId;
				if (chapterId != lastChapterId) {
					lastChapterId = chapterId;
					sectionIndices.add(i);
				}
			}
			return sectionIndices;
		}

		private List<String> getSectionHeaders() {
			List<String> sectionHeaders = new ArrayList<String>();
			for (int i = 0, n = sectionIndices.size(); i < n; i++) {
				sectionHeaders
						.add(listData.get(sectionIndices.get(i)).chapterName);
			}
			return sectionHeaders;
		}
		
		@Override
		public int getCount() {
			return listData == null ? 0 : listData.size();
		}

		@Override
		public Object getItem(int position) {
			return listData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(
						R.layout.item_notes, null);
			}
			ImageView selectView = ViewHolder.get(convertView, R.id.item_select_image);
			TextView timeView = ViewHolder.get(convertView, R.id.time_layout);
			TextView digestView = ViewHolder.get(convertView,
					R.id.digest_layout);
			TextView contentView = ViewHolder.get(convertView,
					R.id.content_layout);
			View itemLine = ViewHolder.get(convertView,
					R.id.item_line);
			View bottomLine = ViewHolder.get(convertView,
					R.id.bottom_line);
			
			ReadNote note = listData.get(position);
			timeView.setText(TimeFormat.formatTime(getResources(),
					note.updateTime / 1000));
			digestView.setText(note.quoteText);

			String contentString = note.contentText;
			if (TextUtils.isEmpty(contentString)) {
				contentView.setVisibility(View.GONE);
			} else {
				contentView.setVisibility(View.VISIBLE);
				contentView.setText(contentString);
			}
			selectView.setVisibility(isDeleteMode?View.VISIBLE:View.GONE);
			if (isDeleteMode) {
				TypedArray a = getActivity().obtainStyledAttributes(
						new int[] { R.attr.read_list_item_unselected_img,
								R.attr.read_list_item_selected_img});
				if (deleteReadNotes.contains(note)) {
					selectView.setImageResource(a.getResourceId(1,
							R.drawable.reader_icon_list_selected_standard));
				} else {
					selectView.setImageResource(a.getResourceId(0,
							R.drawable.reader_icon_list_unselected_standard));
				}
			}

			int pos = initRange(position);
			if (pos == -1) {
				bottomLine.setVisibility(View.GONE);
				itemLine.setVisibility(View.VISIBLE);
			} else if (pos == 0) {
				bottomLine.setVisibility(View.GONE);
				itemLine.setVisibility(View.VISIBLE);
			} else if (pos == 1) {
				itemLine.setVisibility(View.GONE);
				bottomLine.setVisibility(View.GONE);
			} else {
				itemLine.setVisibility(View.GONE);
				bottomLine.setVisibility(View.GONE);
			}
			if (position == listData.size()-1) {
				bottomLine.setVisibility(View.VISIBLE);
			}

			return convertView;
		}
		
		@Override
		public View getHeaderView(int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(
						R.layout.item_section_bookmark_note, null);
			}
			TextView sectionView = ViewHolder.get(convertView,
					R.id.sectionTextView);
			sectionView.setText(listData.get(position).chapterName);
			return convertView;
		}

		@Override
		public long getHeaderId(int position) {
			for (int i = 0, n = sectionIndices.size(); i < n; i++) {
				if (position < sectionIndices.get(i)) {
					return i - 1;
				}
			}
			return sectionIndices.size() - 1;
		}

		@Override
		public int getPositionForSection(int sectionIndex) {
			if (sectionIndex >= sectionIndices.size()) {
				sectionIndex = sectionIndices.size() - 1;
			} else if (sectionIndex < 0) {
				sectionIndex = 0;
			}
			return sectionIndices.get(sectionIndex);
		}

		@Override
		public int getSectionForPosition(int position) {
			for (int i = 0, n = sectionIndices.size(); i < n; i++) {
				if (position < sectionIndices.get(i)) {
					return i - 1;
				}
			}
			return sectionIndices.size() - 1;
		}

		@Override
		public Object[] getSections() {
			return sectionHeaders.toArray();
		}

	}

}
