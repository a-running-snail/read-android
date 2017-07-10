package com.jingdong.app.reader.bookmark;

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
import com.jingdong.app.reader.activity.BookNoteActivity;
import com.jingdong.app.reader.activity.CatalogActivity;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.plugin.pdf.outline.OutlineItem;
import com.jingdong.app.reader.reading.BookMark;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ThemeUtils;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.ViewHolder;

public class BookMarksFragment extends CommonFragment {

    public static final String ACTION_RELOAD_BOOKMARK = "com.jingdong.app.reader.reload.bookmark";
    public static final String CHAPTER_ITEM_REF = "chapterItemRef";

	private ExpandableStickyListHeadersListView bookmarksList = null;
	private View nullLayout = null;
	private View bottomContainer = null;
	private View bottomEditContainer = null;
	private TextView editButton = null;
	private TextView deleteButton = null;
	private BookMarksAdapter adapter = null;
	private List<BookMark> deleteBookMarks = new ArrayList<BookMark>();
	private List<BookMark> listData = new ArrayList<BookMark>();
	private List<Integer> position = new ArrayList<Integer>();
	private ArrayList<OutlineItem> outlineList;
	private boolean isDeleteMode = false;
	
	public BookMarksFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentTag = "BookMarksFragment";

		LayoutInflater localInflater=ThemeUtils.getThemeInflater(getActivity(), inflater);
		LinearLayout layout = (LinearLayout) localInflater.inflate(
				R.layout.fragment_bookmarks, null);
		nullLayout = layout.findViewById(R.id.bookmark_null_layout);
		bottomContainer = layout.findViewById(R.id.bottomContainer);
		bottomEditContainer = layout.findViewById(R.id.bottomEditContainer);
		editButton = (TextView) bottomContainer.findViewById(R.id.edit);
		deleteButton = (TextView) bottomEditContainer.findViewById(R.id.delete);
		bookmarksList = (ExpandableStickyListHeadersListView) layout.findViewById(R.id.bookmarksList);

		outlineList = getArguments().getParcelableArrayList(CatalogActivity.TOCLabelListKey);
		long ebookid = getArguments().getLong("ebookid");
		int docid = getArguments().getInt("docid");
		List<BookMark> bookMarks = MZBookDatabase.instance.getAllBookMarksOfBook(
				LoginUser.getpin(), ebookid, docid);
		if (bookMarks.size() == 0) {
			nullLayout.setVisibility(View.VISIBLE);
			bookmarksList.setVisibility(View.GONE);
			setupEditButtonDisableUI();
		}
		markChapterId(bookMarks);
		listData = createData(bookMarks);

		adapter = new BookMarksAdapter();
		bookmarksList.setAdapter(adapter);
		
		bookmarksList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				BookMark bookmark = listData.get(position);
				if (isDeleteMode) {
					ImageView selectView = ViewHolder.get(view, R.id.item_select_image);
					TypedArray a = getActivity().obtainStyledAttributes(new int[] {
							R.attr.read_list_item_unselected_img,
							R.attr.read_list_item_selected_img});
					if (deleteBookMarks.contains(bookmark)) {
						deleteBookMarks.remove(bookmark);
						selectView.setImageResource(a.getResourceId(0, R.drawable.reader_icon_list_unselected_standard));
					} else {
						deleteBookMarks.add(bookmark);
						selectView.setImageResource(a.getResourceId(1, R.drawable.reader_icon_list_selected_standard));
					}
					
					if (deleteBookMarks.size() > 0) {
						setupDeleteButtonEnableUI();
					} else {
						setupDeleteButtonDisableUI();
					}
					return;
				}
				Intent data = new Intent();
				data.putExtra(BookNoteActivity.CHAPTER_ITEM_REF, bookmark.chapter_itemref);
				data.putExtra(BookNoteActivity.PARA_INDEX, bookmark.para_index);
				data.putExtra(BookNoteActivity.OFFSET_IN_PARA, bookmark.offset_in_para);
				data.putExtra(CatalogActivity.PAGE_INDEX, bookmark.pdf_page);
				getActivity().setResult(CatalogActivity.RESULT_CHANGE_PAGE, data);
				getActivity().finish();
				getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
			}
		});
		
		bookmarksList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				if(!isDeleteMode) {
					isDeleteMode = true;
					deleteBookMarks.clear();
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
				deleteBookMarks.clear();
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
				listData.removeAll(deleteBookMarks);
				removeBookMarks(deleteBookMarks);
				setupDeleteButtonDisableUI();
				deleteBookMarks.clear();
				adapter.refresh();
				if (listData.size() == 0) {
					setupEditButtonDisableUI();
					nullLayout.setVisibility(View.VISIBLE);
					bookmarksList.setVisibility(View.GONE);
				}
			}
		});
		bottomEditContainer.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				isDeleteMode = false;
				deleteBookMarks.clear();
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
				getActivity().getResources().getColor(
						R.color.r_text_disable)));
	}
	
	private void setupDeleteButtonEnableUI() {
		TypedArray a = getActivity().obtainStyledAttributes(
				new int[] { R.attr.r_theme });
		deleteButton.setTextColor(a.getColor(0, getActivity()
				.getResources().getColor(R.color.r_theme)));
	}
	
	private void setupDeleteButtonDisableUI() {
		TypedArray a = getActivity().obtainStyledAttributes(
				new int[] { R.attr.r_text_disable });
		deleteButton.setTextColor(a.getColor(0, getActivity()
				.getResources().getColor(R.color.r_text_disable)));
	}
	
	private void markChapterId(List<BookMark> bookMarks) {
		HashMap<String, Integer> chapterIdMap = new HashMap<String, Integer>();
		for (BookMark mark : bookMarks) {
			if (chapterIdMap.containsKey(mark.chapter_title)) {
				int chapterId = chapterIdMap.get(mark.chapter_title);
				mark.chapterId = chapterId;
			} else {
				int chapterId = findChapterId(mark.chapter_title);
				chapterIdMap.put(mark.chapter_title, chapterId);
				mark.chapterId = chapterId;
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

	@Override
	public void onPause() {
		super.onPause();
	}
	
	private void removeBookMarks(List<BookMark> bookMarks) {
		if (bookMarks != null && bookMarks.size() > 0) {
			ArrayList<String> markChapterList = new ArrayList<String>();
			for (BookMark mark : bookMarks) {
				if (!markChapterList.contains(mark.chapter_itemref)) {
					markChapterList.add(mark.chapter_itemref);
				}
				MZBookDatabase.instance.deleteBookMarkByUpdate(mark.id);
			}
			Intent intent = new Intent(ACTION_RELOAD_BOOKMARK);
			intent.putExtra(CHAPTER_ITEM_REF, markChapterList);
			LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
		}
	}
	
	class BookMarkComparator implements Comparator<BookMark> {

		@Override
		public int compare(BookMark lhs, BookMark rhs) {

			if (lhs.bookType == LocalBook.FORMAT_PDF) {
				
				if (lhs.pdf_page < rhs.pdf_page) {
					return -1;
				} else if (lhs.pdf_page > rhs.pdf_page) {
					return 1;
				} else {
					return 0;
				}
				
			} else if (lhs.bookType == LocalBook.FORMAT_EPUB) {
				if (lhs.chapterId < rhs.chapterId)
					return -1;
				else if (lhs.chapterId > rhs.chapterId) {
					return 1;
				} else {
					if (lhs.para_index < rhs.para_index) {
						return -1;
					} else if (lhs.para_index > rhs.para_index) {
						return 1;
					} else {
						if (lhs.offset_in_para < rhs.offset_in_para) {
							return -1;
						} else if (lhs.offset_in_para > rhs.offset_in_para) {
							return 1;
						} else {
							return 0;
						}
					}
				}

			} else {
				return 0;
			}
		}

	}

	class BookMarkTimeComparator implements Comparator<BookMark> {

		@Override
		public int compare(BookMark lhs, BookMark rhs) {

			if (lhs.updated_at < rhs.updated_at)
				return 1;
			else if (lhs.updated_at > rhs.updated_at)
				return -1;

			else

				return 0;
		}

	}

	public List<BookMark> createData(List<BookMark> list) {

		Collections.sort(list, new BookMarkComparator());
		List<BookMark> tempList = new ArrayList<BookMark>();
		List<BookMark> result = new ArrayList<BookMark>();
		position.clear();
		for (int i = 0; i < list.size(); i++) {

			BookMark temp = list.get(i);
			tempList.clear();
			tempList.add(temp);

			int j = i + 1;
			for (; j < list.size(); j++) {
				if (list.get(i).chapter_title.equals(list.get(j).chapter_title)) {
					tempList.add(list.get(j));
				} else {
					i = --j;
					break;
				}
			}
			if (j >= list.size())
				i = j;

			//Collections.sort(tempList, new BookMarkTimeComparator());//不要根据更新时间排序
			position.add(tempList.size());

			result.addAll(tempList);
		}
		return result;
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

	class BookMarksAdapter extends BaseAdapter implements
			StickyListHeadersAdapter, SectionIndexer {

		private List<Integer> sectionIndices;
		private List<String> sectionHeaders;

		public BookMarksAdapter() {
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
						.add(listData.get(sectionIndices.get(i)).chapter_title);
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
						R.layout.item_bookmark, null);
			}
			ImageView selectView = ViewHolder.get(convertView, R.id.item_select_image);
			TextView timeView = ViewHolder.get(convertView, R.id.time_layout);
			TextView pageView = ViewHolder.get(convertView, R.id.page_layout);
			TextView digestView = ViewHolder.get(convertView,
					R.id.digest_layout);
			View itemLine = ViewHolder.get(convertView, R.id.item_line);
			View bottomLine = ViewHolder.get(convertView, R.id.bottom_line);

			BookMark mark = listData.get(position);
			timeView.setText(TimeFormat.formatTime(getResources(),
					mark.updated_at));
			pageView.setText("");
			digestView.setText(mark.digest);
			
			selectView.setVisibility(isDeleteMode?View.VISIBLE:View.GONE);
			if (isDeleteMode) {
				TypedArray a = getActivity().obtainStyledAttributes(
						new int[] { R.attr.read_list_item_unselected_img,
								R.attr.read_list_item_selected_img });
				if (deleteBookMarks.contains(mark)) {
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
			sectionView.setText(listData.get(position).chapter_title);
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
