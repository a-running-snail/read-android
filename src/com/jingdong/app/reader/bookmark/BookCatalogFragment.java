package com.jingdong.app.reader.bookmark;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.CatalogActivity;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.plugin.pdf.outline.OutlineItem;
import com.jingdong.app.reader.util.ThemeUtils;
import com.jingdong.app.reader.util.ViewHolder;

public class BookCatalogFragment extends CommonFragment {

	private ArrayList<OutlineItem> outlineList;
	private String bookName;
	private String author;
	private int documentId;
	private int chapterIndex;
	private int paddingLeft;
	private int paddingRight;
	private boolean isPageZero = false;
	private boolean isPageFinish = false;

	public BookCatalogFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentTag = "BookCatalogFragment";

		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.fragment_catalog, null);

		LinearLayout header = (LinearLayout) inflater.inflate(
				R.layout.fragment_catalog_header, null);

		outlineList = getArguments().getParcelableArrayList(
				CatalogActivity.TOCLabelListKey);
		isPageFinish = getArguments().getBoolean("isPageFinish");
		chapterIndex = getArguments().getInt("chapterIndex");
		bookName = getArguments().getString("bookname", "");
		author = getArguments().getString("author", "");
		documentId = getArguments().getInt("docid", 0);
		if (outlineList.size() > 0) {
			OutlineItem item = outlineList.get(0);
			isPageZero = item.page == 0;
		}

		float density = getActivity().getResources().getDisplayMetrics().density;
		paddingLeft = (int) (16 * density);
		paddingRight = (int) (16 * density);

		// labels=new String[]{"第一章","第二章","第三章","第四章"};
		// pages=new String[]{"1","2","3","4"};
		// chapterIndex=1;

		TextView bookNames = (TextView) header.findViewById(R.id.book_name);
		TextView bookAuthors = (TextView) header.findViewById(R.id.book_author);
		bookNames.setText(bookName);
		bookAuthors.setText(author);

		ListView listView = (ListView) layout.findViewById(R.id.tocList);
		listView.addHeaderView(header);
		listView.setAdapter(new TOCListAdapter());
		listView.setSelectionFromTop(chapterIndex, 250);// 设置选中部分和顶部有一定距离
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 因为listview有header的时候，position是从header开始计算的，所以parent.getAdapter()
				// 参考--> http://www.cnblogs.com/tt_mc/p/3618000.html
				OutlineItem item = (OutlineItem) parent.getAdapter().getItem(
						position);
				if (item != null) {
					position = outlineList.indexOf(item);
					Intent data = new Intent();
					data.putExtra(CatalogActivity.TOCSelectedIndexKey, position);
					data.putExtra(CatalogActivity.TOCSelectedPageKey,
							String.valueOf(item.page));
					getActivity().setResult(Activity.RESULT_OK, data);
					getActivity().finish();
					getActivity().overridePendingTransition(R.anim.right_in,
							R.anim.left_out);
				}
			}

		});

		/*
		 * View docBindButton = layout.findViewById(R.id.bindBook); DocBind
		 * docBind = MZBookDatabase.instance.getDocBind(documentId,
		 * LoginUser.getpin()); docBindButton.setVisibility(View.GONE); if
		 * (docBind != null && docBind.bind == 0 && docBind.bookId == 0) {
		 * docBindButton.setVisibility(View.VISIBLE); }
		 * docBindButton.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View view) { Document document =
		 * MZBookDatabase.instance.getDocument(documentId); Intent intent = new
		 * Intent(getActivity(), Bookcase3rdBindActivity.class);
		 * intent.putExtra(Bookcase3rdBindActivity.DocumentKey, document);
		 * intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		 * startActivity(intent); } });
		 */
		View backButton = layout.findViewById(R.id.back);
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				getActivity().finish();
			}
		});
		return layout;
	}

	private class TOCListAdapter extends BaseAdapter {

		public TOCListAdapter() {
		}

		@Override
		public int getCount() {
			if (outlineList == null) {
				return 0;
			}
			return outlineList.size();
		}

		@Override
		public Object getItem(int position) {
			return outlineList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			TypedArray a = getActivity().obtainStyledAttributes(
					new int[] { R.attr.r_theme, R.attr.r_text_main,
							R.attr.r_text_sub });
			LayoutInflater localInflater = ThemeUtils.getThemeInflater(
					getActivity(), null);
			OutlineItem item = outlineList.get(position);
			if (convertView == null) {
				convertView = localInflater.inflate(
						R.layout.item_catalogactivity_catalog, null);
			}
			convertView.setPadding(paddingLeft * (item.level + 1), 0,
					paddingRight, 0);

			TextView parentChapterName = ViewHolder.get(convertView,
					R.id.parent_chapter_name);
			TextView parentChapterPosition = ViewHolder.get(convertView,
					R.id.parent_chapter_position);

			TextPaint tp = parentChapterName.getPaint();
			tp.setFakeBoldText(item.level == 0);
			parentChapterName.setText(item.title);

			if (position == chapterIndex) {
				parentChapterName.setTextColor(a.getColor(0, getResources()
						.getColor(R.color.r_theme)));
				parentChapterPosition.setTextColor(a.getColor(0, getResources()
						.getColor(R.color.r_theme)));
			} else {
				parentChapterName.setTextColor(a.getColor(1, getResources()
						.getColor(R.color.r_text_main)));
				parentChapterPosition.setTextColor(a.getColor(2, getResources()
						.getColor(R.color.r_text_sub)));
			}
			if (isPageFinish) {
				int pageNumber = isPageZero ? item.page + 1 : item.page;
				parentChapterPosition.setText(String.valueOf(pageNumber));
				parentChapterPosition.setVisibility(View.VISIBLE);
				if (pageNumber == 0) {
					parentChapterPosition.setVisibility(View.INVISIBLE);
					parentChapterName.setTextColor(a.getColor(2, getResources()
							.getColor(R.color.r_text_sub)));
				}
			} else {
				parentChapterPosition.setText("...");
			}

			return convertView;
		}
	}
}
