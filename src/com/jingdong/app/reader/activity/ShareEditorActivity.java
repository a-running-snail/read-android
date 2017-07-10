package com.jingdong.app.reader.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.R;
//import com.jingdong.app.reader.util.SocialShareHelper;

public class ShareEditorActivity extends MZReadCommonActivity {
	public static final String TITLE = "title";
	private static final int WEIBO_MAX = 140;
	private EditText editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_editor);
		String shareText, title;
		Intent intent = getIntent();
		title = intent.getExtras().getString(TITLE, getTitle().toString());
		//shareText = intent.getStringExtra(SocialShareHelper.TEXT);
		getActionBar().setTitle(title);
		//initViews(shareText);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = new MenuInflater(this);
		menuInflater.inflate(R.menu.finish, menu);
		MenuItem Item = menu.findItem(R.id.finish);
		View actionView = Item.getActionView();
		TextView view = (TextView) actionView.findViewById(R.id.finish_action);
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (editor != null) {
					if (editor.length() > 0) {
						//String text = chopText(editor.getText().toString(),
						//		getIntent().getStringExtra(SocialShareHelper.URL));
						//String imgFile = getIntent().getStringExtra(SocialShareHelper.IMG_FILE);
						//Intent intent = new Intent();
						//intent.putExtra(SocialShareHelper.TEXT, text);
						//intent.putExtra(SocialShareHelper.SOCIAL_TYPE,
						//		getIntent().getIntExtra(SocialShareHelper.SOCIAL_TYPE, (int) UiStaticMethod.ILLEGAL_INDEX));
						//if (imgFile != null) {
						///	intent.putExtra(SocialShareHelper.IMG_FILE, imgFile);
						//}
						//setResult(RESULT_OK, intent);
						finish();
					} else {
						Toast.makeText(ShareEditorActivity.this, R.string.post_without_word, Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	private void initViews(String shareText) {
		//String filePath = getIntent().getStringExtra(SocialShareHelper.IMG_FILE);
		//editor = (EditText) findViewById(R.id.shareText);
		/*TextView number = (TextView) findViewById(R.id.text_number);
		ImageView imageView = (ImageView) findViewById(R.id.bookList);
		if (TextUtils.isEmpty(filePath))
			imageView.setVisibility(View.GONE);
		else {
			imageView.setVisibility(View.VISIBLE);
			Bitmap bitmap = ImageUtils
					.getBitmapFromNamePath(filePath, 0, UiStaticMethod.getScreenHeight(this));
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			}
		}
		if (TextUtils.isEmpty(shareText))
			number.setText(String.valueOf(0));
		else
			number.setText(getNumberText(shareText));
		editor.setText(shareText);
		editor.addTextChangedListener(getTextWatcher(number));*/
	}

	private String getNumberText(CharSequence s) {
		int length = s.length();
		if (length <= WEIBO_MAX)
			return String.valueOf(length);
		else
			return String.valueOf(WEIBO_MAX - length);
	}

	private static String chopText(String text, String url) {
		String soure = text;
		if (!TextUtils.isEmpty(url)) {
			soure = soure + url;
		}
		if (soure.length() > WEIBO_MAX) {
			if (TextUtils.isEmpty(url))
				soure = soure.substring(0, WEIBO_MAX);
			else
				soure = text.subSequence(0, WEIBO_MAX - url.length()) + url;
		}
		return soure;
	}

	private TextWatcher getTextWatcher(final TextView number) {
		return new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				number.setText(getNumberText(s));
			}
		};
	}
}
