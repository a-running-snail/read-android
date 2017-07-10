package com.jingdong.app.reader.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jingdong.app.reader.common.MZReadCommonActivityWithActionBar;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.R;

public class BookTOCActivity extends MZReadCommonActivityWithActionBar {

    public static final String TOCLabelListKey = "TOCLabelListKey";
    public static final String TOCPageListKey = "TOCPageListKey";
    public static final String BookNameKey = "BookNameKey";
    public static final String AuthorNameKey = "AuthorNameKey";
    public static final String ChapterIndexKey = "ChapterIndexKey";
    private String[] labels;
    private String[] pages;
    private String bookName;
    private String author;
    private int chapterIndex;
    private int textColor;
    private boolean isPageDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toc);
        
        int style = LocalUserSetting.readStyle;
        switch (style) {
        case ReadOverlayActivity.READ_STYLE_WHITE:
        	findViewById(R.id.toc_layout).setBackgroundColor(0xfff2f2f2);
        	findViewById(R.id.back_border).setBackgroundColor(0xffe4e4e4);
        	textColor = 0xFF333333;
            break;
        case ReadOverlayActivity.READ_STYLE_SOFT:
        	findViewById(R.id.toc_layout).setBackgroundColor(0xfff5efdc);
        	findViewById(R.id.back_border).setBackgroundColor(0xffd8d8d8);
        	textColor = 0xFF333333;
            break;
        case ReadOverlayActivity.READ_STYLE_NIGHT:
        	findViewById(R.id.toc_layout).setBackgroundColor(0xff3f3f3f);
        	findViewById(R.id.back_border).setBackgroundColor(0xff2d2d2d);
        	textColor = 0xFFB8B8B8;
            break;
        }

        Intent intent = getIntent();
        labels = intent.getStringArrayExtra(TOCLabelListKey);
        pages = intent.getStringArrayExtra(TOCPageListKey);
        bookName = intent.getStringExtra(BookNameKey);
        author = intent.getStringExtra(AuthorNameKey);
        chapterIndex = intent.getIntExtra(ChapterIndexKey, 0) + 1;//第0个是书名，从第1个开始是章节
        if (pages != null && pages.length > 0) {
        	isPageDone = true;
        }
        ListView listView = (ListView) findViewById(R.id.tocList);
        listView.setAdapter(new TOCListAdapter(this));
        listView.setSelection(chapterIndex);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent data = new Intent();
                data.putExtra(CatalogActivity.TOCSelectedIndexKey, position - 1);
                setResult(RESULT_OK, data);
                finish();
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }

        });

        View cancelView = findViewById(R.id.tocBack);
        cancelView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }

        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    private class TOCListAdapter extends BaseAdapter {
        private Context context;

        TOCListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            if (position == 0) {
                return false;
            } else {
                return true;
            }
        }
        
        @Override
        public int getCount() {
            if (labels == null) {
                return 0;
            }
            return labels.length + 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.book_toc_item, parent, false);
			}
			if (position == 0) {
				convertView.findViewById(R.id.book_name).setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.chapter_name).setVisibility(View.GONE);
				TextView firstLine = (TextView) convertView.findViewById(R.id.firstLine);
				TextView secondLine = (TextView) convertView.findViewById(R.id.secondLine);
				firstLine.setTextColor(textColor);
				secondLine.setTextColor(textColor);
				firstLine.setText(bookName);
				secondLine.setText(author);
			} else {
				convertView.findViewById(R.id.book_name).setVisibility(View.GONE);
				convertView.findViewById(R.id.chapter_name).setVisibility(View.VISIBLE);
				TextView chapter = (TextView) convertView.findViewById(R.id.name);
				chapter.setText(labels[position - 1]);
				if (position == chapterIndex) {
					chapter.setTextColor(0xFFED7057);
				} else {
					chapter.setTextColor(textColor);
				}
				if (isPageDone) {
					TextView page = (TextView) convertView.findViewById(R.id.pagenumber);
					page.setText(pages[position - 1]);
				}
			}

			return convertView;
		}
    }
}
