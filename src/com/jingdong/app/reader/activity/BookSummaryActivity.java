package com.jingdong.app.reader.activity;

import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.R;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

public class BookSummaryActivity extends MZReadCommonActivity {

    public static final String BookSummaryKey = "BookSummaryKey";
    public static final String BookNameKey = "BookNameKey";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_summary);
        String summary = getIntent().getStringExtra(BookSummaryKey);
        String bookName = getIntent().getStringExtra(BookNameKey);
        if (!TextUtils.isEmpty(bookName)) {
            setTitle(bookName);
        }
        if (!TextUtils.isEmpty(summary)) {
            TextView tv = (TextView) findViewById(R.id.bookSummary);
            tv.setText(summary);
        }
    }

}
