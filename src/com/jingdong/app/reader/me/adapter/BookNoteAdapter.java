package com.jingdong.app.reader.me.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jingdong.app.reader.me.model.BookNoteModelInterface;
import com.jingdong.app.reader.timeline.model.core.BookNoteInterface;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.TextArea;
import com.jingdong.app.reader.R;

public class BookNoteAdapter extends BaseAdapter {
	public static final int CHAPTER = 0;
	public static final int NOTE = 1;
	private Context context;
	private BookNoteModelInterface model;

	public BookNoteAdapter(Context context, BookNoteModelInterface model) {
		this.context = context;
		this.model = model;
	}

	@Override
	public int getCount() {
		return model.getRowNumber();
	}

	@Override
	public Object getItem(int position) {
		return model.getRowAt(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return (getItem(position) instanceof String) ? CHAPTER : NOTE;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int type = getItemViewType(position);
		switch (type) {
		case CHAPTER:
			if (convertView == null) {
				convertView = View.inflate(context, R.layout.view_chaper, null);
			}
			((TextView) convertView.findViewById(R.id.chapter))
					.setText((String) getItem(position));
			break;
		case NOTE:
			if (convertView == null) {
				convertView = View.inflate(context, R.layout.item_book_note,
						null);
			}
			BookNoteInterface bookNoteInterface = (BookNoteInterface) getItem(position);
			String content = bookNoteInterface.getContent();
			String quote = bookNoteInterface.getQuote();
			bookNoteInterface
					.setContent(UiStaticMethod.formatListItem(content));
			bookNoteInterface.setQuote(UiStaticMethod.formatListItem(quote));
			TextView time = (TextView)convertView.findViewById(R.id.time);
			time.setText(TimeFormat.formatTime(context.getResources(), bookNoteInterface.getWrittenTime()));
			TextView is_priavte  = (TextView) convertView.findViewById(R.id.is_private);
			is_priavte.setVisibility(bookNoteInterface.isPrivate()?View.VISIBLE:View.GONE);
			TextArea noteContent = (TextArea)convertView.findViewById(R.id.note_content);
			noteContent.parseBookNote(bookNoteInterface, false);
			bookNoteInterface.setContent(content);
			bookNoteInterface.setQuote(quote);
			break;
		}
		return convertView;
	}

	public static void merge(List<Object> row, List<String> chapterNames, List<BookNoteInterface> bookNotes) {
		row.clear();
		String chapterName;
		BookNoteInterface bookNote;
		int notePosition = 0;
		for (int i = 0; i < chapterNames.size(); i++) {
			chapterName = chapterNames.get(i);
			row.add(chapterName);
			while (notePosition < bookNotes.size()
					&& chapterName.equals((bookNote = bookNotes.get(notePosition)).getChapterName())) {
				notePosition++;
				row.add(bookNote);
			}
		}
	}

	public static void resetChapterNames(List<String> chapterNames, List<BookNoteInterface> booksNotes) {
		if (!booksNotes.isEmpty()) {
			chapterNames.clear();
			String currentChapter = booksNotes.get(0).getChapterName();
			chapterNames.add(currentChapter);
			for (BookNoteInterface note : booksNotes) {
				if (!currentChapter.equals(note.getChapterName())) {
					currentChapter = note.getChapterName();
					chapterNames.add(currentChapter);
				}
			}
		}
	}
}
