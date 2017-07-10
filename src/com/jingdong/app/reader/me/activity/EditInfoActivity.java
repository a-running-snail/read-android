package com.jingdong.app.reader.me.activity;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.me.model.EditInfoModel;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.R;

public class EditInfoActivity extends MZReadCommonActivity implements OnClickListener, Observer,
		OnCheckedChangeListener {

	private class TextChangeListener implements TextWatcher {
		private int id;

		private TextChangeListener(int id) {
			this.id = id;
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (id == userName.getId()) {
				userNameString = s.toString();
			} else if (id == summary.getId()) {
				summaryString = s.toString();
			} else if (id == contactEmail.getId()) {
				contactEmailString = s.toString();
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

	}

	private class Task implements Runnable {
		private int type;

		public Task(int type) {
			this.type = type;
		}

		@Override
		public void run() {
			switch (type) {
			case SUBMIT:
				model.upload(userNameString, summaryString, contactEmailString, sex, avatar);
				break;
			}
		}
	}

	private static class MyHandler extends Handler {
		private WeakReference<EditInfoActivity> reference;

		public MyHandler(EditInfoActivity activity) {
			this.reference = new WeakReference<EditInfoActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			EditInfoActivity activity = reference.get();
			if (activity != null) {
				switch (msg.what) {
				case SUBMIT:
					activity.dialog.dismiss();
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						String profile = msg.getData().getString(EditInfoModel.PROFILE);
						LocalUserSetting.saveTokenAndUserInfo(activity, LocalUserSetting.getToken(activity), profile);
						activity.setResult(RESULT_OK, getReturnBundle(activity));
						activity.finish();
					} else if (msg.arg1 == ObservableModel.FAIL_INT) {
						if (msg.obj instanceof String) {
							if (!TextUtils.isEmpty((String) msg.obj)) {
								try {
									JSONObject jsonObject = new JSONObject((String) msg.obj);
									String message = jsonObject.getJSONArray("errors").getString(0);
									Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
									break;
								} catch (JSONException e) {
									MZLog.e("EditInfo", Log.getStackTraceString(e));
								}
							}
						}
						Toast.makeText(activity, R.string.updating_fail, Toast.LENGTH_SHORT).show();
					}
					break;
				}
			}
		}

		/**
		 * 为setResult设置Intent
		 * 
		 * @param activity
		 *            待设置的Activity
		 * @return setResult中将使用的Intent
		 */
		private Intent getReturnBundle(EditInfoActivity activity) {
			Intent intent = new Intent();
			if (activity.userNameString != null && !activity.userNameString.equals(""))
				intent.putExtra(EditInfoModel.NAME, activity.userNameString);
			if (activity.sex != -1)
				intent.putExtra(EditInfoModel.SEX, activity.sex);
			if (activity.summaryString != null)
				intent.putExtra(EditInfoModel.SUMMARY, activity.summaryString);
			if (activity.contactEmailString != null)
				intent.putExtra(EditInfoModel.CONTECT_EMAIL, activity.contactEmailString);
			if (activity.avatar != null)
				intent.putExtra(EditInfoModel.AVATAR, activity.avatar);
			return intent;
		}
	}

	public final static int SUBMIT = 1000;
	private final static int CAMERA = 101;
	private final static int CHOOSE_IMAGE = 102;
	private final static int PIC_SIZE = 400;
	private ProgressDialog dialog;
	private ImageView imageView;
	private EditText userName;
	private EditText summary;
	private EditText contactEmail;
	private RadioGroup radioGroup;
	private EditInfoModel model;
	private Handler myHandler;
	private Uri thumbnailUri;
	private int sex = -1;
	private UserInfo userInfo;
	private String userNameString;
	private String summaryString;
	private String contactEmailString;
	private byte[] avatar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_info);
		myHandler = new MyHandler(this);
		model = new EditInfoModel(this);
		model.addObserver(this);
		userInfo = (UserInfo) getIntent().getParcelableExtra(MoreInfoActivity.USER_DETAIL);
		if (userInfo == null) {
			userInfo = LocalUserSetting.getUserInfo(this);
		}
		initView(getIntent().getByteArrayExtra(EditInfoModel.AVATAR));
	}

	@Override
	protected void onDestroy() {
		model.deleteObserver(this);
		myHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.finish, menu);
		MenuItem Item = menu.findItem(R.id.finish);
		View actionView = Item.getActionView();
		TextView view = (TextView) actionView.findViewById(R.id.finish_action);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (userName.equals("")) {
					Toast.makeText(EditInfoActivity.this, R.string.no_user_name, Toast.LENGTH_SHORT).show();
				} else if (userNameString == null && summaryString == null && contactEmailString == null
						&& avatar == null && sex == -1) {
					setResult(RESULT_CANCELED);
					finish();
				} else {
					Resources resources = getResources();
					String title = resources.getString(R.string.update);
					String message = resources.getString(R.string.updating);
					dialog = ProgressDialog.show(EditInfoActivity.this, title, message, true, false);
					NotificationService.getExecutorService().execute(new Task(SUBMIT));
				}
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CAMERA:
			if (resultCode == RESULT_OK) {
				resizePicture(thumbnailUri);
				getContentResolver().delete(thumbnailUri, null, null);
			}
			break;
		case CHOOSE_IMAGE:
			if (resultCode == RESULT_OK) {
				resizePicture(data.getData());
			}
			break;
		}
	}

	@Override
	public void onClick(View v) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(R.string.change_thumbnail);
		dialogBuilder.setItems(R.array.change_thumbnail, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent();
				switch (which) {
				case 0:
					thumbnailUri = generateUri();
					intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, thumbnailUri);
					startActivity(intent, CAMERA, R.string.no_camera);
					break;
				case 1:
					intent.setAction(Intent.ACTION_PICK);
					intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivity(intent, CHOOSE_IMAGE, R.string.no_image_chooser);
					break;
				}
			}

			private Uri generateUri() {
				ContentValues values = new ContentValues(0);
				Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				return uri;
			}

		});
		dialogBuilder.create().show();
	}

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {
		switch (arg1) {
		case R.id.male:
			sex = UserInfo.MALE;
			break;
		case R.id.female:
			sex = UserInfo.FEMALE;
			break;
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		myHandler.sendMessage((Message) data);
	}

	/**
	 * 初始化view
	 * 
	 * @param origin
	 *            用户头像
	 */
	private void initView(byte[] origin) {
		getViewsByIds();
		setText();
		setThumbnail(origin);
		if (userInfo.isFemale())
			radioGroup.check(R.id.female);
		else
			radioGroup.check(R.id.male);
		setTextChangeListener();
		findViewById(R.id.change_thumbnail).setOnClickListener(this);
		radioGroup.setOnCheckedChangeListener(this);
	}

	/**
	 * 设置views的Id
	 */
	private void getViewsByIds() {
		userName = (EditText) findViewById(R.id.edit_name);
		summary = (EditText) findViewById(R.id.edit_summary);
		contactEmail = (EditText) findViewById(R.id.edit_email);
		imageView = (ImageView) findViewById(R.id.edit_thumbnail);
		radioGroup = (RadioGroup) findViewById(R.id.sex_radio_group);
	}

	/**
	 * 设置各个TextView的Text
	 */
	private void setText() {
		userName.setText(userInfo.getName());
		String summa = userInfo.getSummary();
		if (!UiStaticMethod.isEmpty(summa)) {
			summary.setText(summa);
		}
		String email = userInfo.getContactEmail();
		if (!UiStaticMethod.isEmpty(email)) {
			contactEmail.setText(email);
		}
	}

	/**
	 * 设置Text改变的监听事件
	 */
	private void setTextChangeListener() {
		userName.addTextChangedListener(new TextChangeListener(userName.getId()));
		summary.addTextChangedListener(new TextChangeListener(summary.getId()));
		contactEmail.addTextChangedListener(new TextChangeListener(contactEmail.getId()));
	}

	/**
	 * 设置用户头像
	 * 
	 * @param origin
	 *            保存有用户头像的数组，可能为空
	 */
	private void setThumbnail(byte[] origin) {
		if (origin != null)
		    imageView.setImageBitmap(BitmapFactory.decodeByteArray(origin, 0, origin.length));
		else
			UiStaticMethod.loadThumbnail(this, imageView, userInfo.getThumbNail() + "!w200h200",
					userInfo.isFemale());
	}

	/**
	 * 启动一个新的Activity用来拍照或者选择本地图片文件爱你
	 * 
	 * @param intent
	 *            包含有启动信息的Intent
	 * @param requestCode
	 *            待启动的Activity的类型
	 * @param id
	 *            如果启动失败，会显示的字符串的ID
	 */
	private void startActivity(Intent intent, int requestCode, int id) {
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(intent, requestCode);
		} else {
			Toast.makeText(EditInfoActivity.this, id, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 重新设置用户头像的大小，截掉多余的部分
	 * 
	 * @param uri
	 *            头像所在的Uri
	 */
	private void resizePicture(Uri uri) {
		Bitmap bitmap = ImageUtils.getBitmapFromStream(uri, getContentResolver(), PIC_SIZE, PIC_SIZE);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			avatar = UiStaticMethod.bitmapToByteArray(bitmap);
		}
	}
}
