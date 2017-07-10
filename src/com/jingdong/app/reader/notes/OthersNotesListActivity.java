package com.jingdong.app.reader.notes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.R;

public class OthersNotesListActivity extends MZReadCommonActivity {
	private List<ReadNote> readNotes;
	private List<ReadNote> listData = new ArrayList<ReadNote>();
	private long ebookid = 0;
	private int docid = 0;
	private String userid = "";
	private BookNotesAdapter adapter = null;
	private List<Integer> position = new ArrayList<Integer>();
	private ListView bookNotesList = null;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_others_notes);
		bookNotesList = (ListView) findViewById(R.id.booknotesList);
		ebookid = getIntent().getLongExtra("ebookid", 0);
		docid = getIntent().getIntExtra("docid", 0);
		userid = getIntent().getStringExtra("userid");

		if (docid != 0)
			readNotes = MZBookDatabase.instance.listDocReadNote(userid, docid);
		else if (ebookid != 0)
			readNotes = MZBookDatabase.instance.listEBookReadNote(userid,
					ebookid);

		listData = createData(readNotes);

		adapter = new BookNotesAdapter();
		bookNotesList.setAdapter(adapter);
		bookNotesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				/*
				 * Intent data = new Intent();
				 * data.putExtra(BookNoteActivity.CHAPTER_ITEM_REF,
				 * listData.get(position).spineIdRef);
				 * data.putExtra(BookNoteActivity.PARA_INDEX,
				 * listData.get(position).fromParaIndex);
				 * data.putExtra(BookNoteActivity
				 * .OFFSET_IN_PARA,listData.get(position).toOffsetInPara);
				 * setResult
				 * (BookPageViewActivity.RESULT_CHANGE_PAGE_NOTES,data);
				 * finish();
				 * overridePendingTransition(R.anim.right_in,R.anim.left_out);
				 */
			}
		});

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

			Collections.sort(tempList, new BookNoteTimeComparator());
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

	class BookNotesAdapter extends BaseAdapter {

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
				convertView = LayoutInflater.from(OthersNotesListActivity.this)
						.inflate(R.layout.item_notes_import, null);
			}
			LinearLayout topLayout = ViewHolder.get(convertView,
					R.id.top_layout);
			TextView chapterView = ViewHolder.get(convertView,
					R.id.chapter_layout);
			TextView timeView = ViewHolder.get(convertView, R.id.time_layout);
			TextView digestView = ViewHolder.get(convertView,
					R.id.digest_layout);
			TextView contentView = ViewHolder.get(convertView,
					R.id.content_layout);
			View bottomLayout = ViewHolder.get(convertView, R.id.bottom_layout);
			View divider = ViewHolder.get(convertView, R.id.divider);

			chapterView.setText(listData.get(position).chapterName);
			timeView.setText(TimeFormat.formatTime(getResources(),
					listData.get(position).updateTime / 1000));

			digestView.setText(listData.get(position).quoteText);

			String contentString = listData.get(position).contentText;
			contentView.setText(contentString);
			if (!UiStaticMethod.isEmpty(contentString)) {
				contentView.setVisibility(View.VISIBLE);
				divider.setVisibility(View.GONE);
			} else {
				contentView.setVisibility(View.GONE);
				divider.setVisibility(View.VISIBLE);
			}

			int pos = initRange(position);
			if (pos == -1) {
				topLayout.setVisibility(View.VISIBLE);
				bottomLayout.setVisibility(View.VISIBLE);
			} else if (pos == 0) {
				topLayout.setVisibility(View.GONE);
				bottomLayout.setVisibility(View.VISIBLE);
			} else if (pos == 1) {
				topLayout.setVisibility(View.GONE);
				bottomLayout.setVisibility(View.GONE);
			} else {
				topLayout.setVisibility(View.VISIBLE);
				bottomLayout.setVisibility(View.GONE);
			}

			return convertView;
		}

	}
}
