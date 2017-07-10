package com.jingdong.app.reader.me.activity;

import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jingdong.app.reader.common.BaseFragmentActivityWithTopBar;
import com.jingdong.app.reader.common.MZReadCommonFragmentActivity;
import com.jingdong.app.reader.me.fragment.BookListFragment;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.me.model.BookListModel;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.SearchTopBarView.TopBarViewListener;
import com.jingdong.app.reader.R;

public class BookListActivity extends BaseFragmentActivityWithTopBar {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.booklist);
		if (savedInstanceState == null)
			initFragment();
	}

	/**
	 * 初始化待加载的Fragment。
	 */
	private void initFragment() {
		Intent intent = getIntent();
		String userId;
		int type;
		if ((type = intent.getIntExtra(BookListFragment.BOOKLIST_TYPE, 1)) == 1) {
			Uri intentData = intent.getData();
			if (intentData != null) {
				List<String> pathList = intentData.getPathSegments();
				if (pathList != null && pathList.size() == 1) {
					String uId = pathList.get(0);
					if (!TextUtils.isEmpty(uId)) {
						userId = uId;
						type = BookListModel.IMPORT_BOOKS;
						initNormalPart(intent, type, userId);
						return;
					}
				}
			}
		} else {
			userId = intent.getStringExtra(UserFragment.USER_ID);
			initNormalPart(intent, type, userId);
			return;
		}
		Toast.makeText(this, R.string.user_not_found, Toast.LENGTH_SHORT)
				.show();
	}

	private void initNormalPart(Intent intent, int type, String userId) {

		if (type == BookListModel.NOTES_BOOKS) {
			getTopBarView().setTitle("笔记");
		} else if (type == BookListModel.IMPORT_BOOKS) {
			getTopBarView().setTitle("外部导入");
		} else if (type == BookListModel.READ_BOOKS) {
			getTopBarView().setTitle("已读");
		}
		
		Fragment fragment = new BookListFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(BookListFragment.BOOKLIST_TYPE, type);
		bundle.putString(UserFragment.USER_ID, userId);
		bundle.putBoolean(BookListFragment.JUMP_READING_DATA, intent
				.getBooleanExtra(BookListFragment.JUMP_READING_DATA, true));
		if (type == BookListModel.IMPORT_BOOKS) {
			bundle.putBoolean(BookListFragment.JUMP_READING_DATA, intent
					.getBooleanExtra(BookListFragment.JUMP_READING_DATA, false));
		}
		bundle.putString(UserFragment.USER_NAME,
				intent.getStringExtra(UserFragment.USER_NAME));
		fragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.user_container, fragment).commit();
	}

}
