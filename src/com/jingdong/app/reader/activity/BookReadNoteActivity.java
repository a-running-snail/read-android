package com.jingdong.app.reader.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.timeline.actiivity.TimelineSearchPeopleActivity;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.CustomToast;

public class BookReadNoteActivity extends BaseActivityWithTopBar {

    public static final String BookNoteQuoteKey = "BookNoteQuoteKey";
    public static final String BookNoteContentKey = "BookNoteContentKey";
    public static final String BookNoteIsPrivateKey = "BookNoteIsPrivateKey";
    
    private static final int AtRequest = 0;
    private EditText contentEditText;
    private TextView noteQuoteTextView;
    private CheckBox shareToMZReadView;
    private String quote;
    private long bookId;
    private int documentId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookreadnote);
        this.getTopBarView().setTitle(getString(R.string.write_note));
        getTopBarView().setLeftMenuVisiable(true, "取消", R.color.red_main);
		this.getTopBarView().setRightMenuOneVisiable(true, "发布",
				R.color.red_main, false);
        findViewById(R.id.bookNoteAt).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookReadNoteActivity.this, TimelineSearchPeopleActivity.class);
                startActivityForResult(intent, AtRequest);
            }
        });
        
        bookId = getIntent().getLongExtra("bookId", 0);
        documentId = getIntent().getIntExtra("documentId", 0);
        quote = getIntent().getStringExtra(BookNoteQuoteKey);
        boolean isPrivate = getIntent().getBooleanExtra(BookNoteIsPrivateKey, true);
        String content = getIntent().getStringExtra(BookNoteContentKey);

        contentEditText = (EditText) findViewById(R.id.bookNoteText);
        if (!TextUtils.isEmpty(content)) {
            contentEditText.setText(content);
        }
        contentEditText.setFocusable(true);
        contentEditText.setFocusableInTouchMode(true);
        contentEditText.requestFocus();
        
        noteQuoteTextView = (TextView) findViewById(R.id.bookNoteQuote);
        if (!TextUtils.isEmpty(quote)) {
            noteQuoteTextView.setText(quote);
        }
        
        shareToMZReadView = (CheckBox) findViewById(R.id.shareToMZReadView);
        shareToMZReadView.setChecked(!isPrivate);
		if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			shareToMZReadView.setPadding(shareToMZReadView.getPaddingLeft(), shareToMZReadView.getPaddingTop(), shareToMZReadView.getPaddingRight(),
					shareToMZReadView.getPaddingBottom());
		} else {
			shareToMZReadView.setPadding(shareToMZReadView.getPaddingLeft()
					+ getResources().getDrawable(R.drawable.post_tweet_checkbox).getIntrinsicWidth(),
					shareToMZReadView.getPaddingTop(), shareToMZReadView.getPaddingRight(), shareToMZReadView.getPaddingBottom());
		}
    }
    
    @Override
	public void onLeftMenuClick() {
		finish();
	}
    
	@Override
	public void onRightMenuOneClick() {
		long id = bookId;
		if (bookId == 0) {
			id = documentId;
		}
		IntegrationAPI.notesGetScore(this, id, new  IntegrationAPI.GrandScoreListener() {
			
			@Override
			public void onGrandSuccess(SignScore score) {
//				CustomToast.showToast(BookReadNoteActivity.this, "恭喜你添加笔记获得"+score+"积分");
				String scoreInfo = "恭喜你添加笔记获得"+score.getGetScore()+"积分";
				SpannableString span = new SpannableString(scoreInfo);
				int start1 = 9;
				int end1 = start1 + String.valueOf(score.getGetScore()).length();
				span.setSpan(new ForegroundColorSpan(Color.RED), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				CustomToast.showToast(BookReadNoteActivity.this, span);
				backToread();
			}

			@Override
			public void onGrandFail() {
				MZLog.e("J", "添加笔记获取积分失败");
				backToread();
			}
		});
	}
	
	private void backToread() {
		Intent data = new Intent();
		data.putExtra(BookNoteQuoteKey, quote == null ? "" : quote);
		data.putExtra(BookNoteContentKey, contentEditText.getText().toString().trim());
		data.putExtra(BookNoteIsPrivateKey, !shareToMZReadView.isChecked());
		setResult(RESULT_OK, data);
		finish();
	}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case AtRequest:
            if (resultCode == TimelineSearchPeopleActivity.CLICK_USER_NAME) {
                String userName = data.getStringExtra(TimelineSearchPeopleActivity.USER_NAME);
                contentEditText.append("@");
                contentEditText.append(userName);
                contentEditText.append(" ");
            }
            break;
        }
    }

}
