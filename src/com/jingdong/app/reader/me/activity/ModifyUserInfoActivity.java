package com.jingdong.app.reader.me.activity;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.UserInfo;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.security.RequestSecurityKeyTask;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.Base64;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ModifyUserInfoActivity extends BaseActivityWithTopBar implements
		OnClickListener {

	private String userName;
	private String nickName;
	private String imgUrl;
	private RoundNetworkImageView avatar;
	private String usex;
	private String uremark;
	private TextView modify;
	private EditText usernameEdit;
	private EditText nicknameEdit;
	private RelativeLayout manLayout;
	private RelativeLayout womenLayout;
	private ImageView man_select;
	private ImageView women_select;
	private EditText content;
	private EditText emailEditText;
	private int sex = 2;
	public final static int SUBMIT = 1000;
	private final static int CAMERA = 101;
	private final static int CHOOSE_IMAGE = 102;
	private final static int PIC_SIZE = 400;
	private Uri thumbnailUri;
	private byte[] imagebyte;
	private boolean isupdateImg = false;
	private byte[] imgupdate = null;
	private boolean isupdateInfo = false;
	private int infocode = -1;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.modify_info);
		initView();
	}

	private void initView() {
		getTopBarView().setRightMenuOneVisiable(true, "保存", R.color.red_main,
				false);
		avatar = (RoundNetworkImageView) findViewById(R.id.thumb_nail);
		modify = (TextView) findViewById(R.id.modify);
		nicknameEdit = (EditText) findViewById(R.id.nickname);
		usernameEdit = (EditText) findViewById(R.id.username);
		InputFilter[] filters = {new NameLengthFilter(20)};  
		nicknameEdit.setFilters(filters);  
		manLayout = (RelativeLayout) findViewById(R.id.man);
		manTv = (TextView) findViewById(R.id.man_tv);
		womenTv = (TextView) findViewById(R.id.woman_tv);
		
		womenLayout = (RelativeLayout) findViewById(R.id.women);
		man_select = (ImageView) findViewById(R.id.man_select);
		women_select = (ImageView) findViewById(R.id.women_select);
		content = (EditText) findViewById(R.id.content);

		manLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				man_select.setVisibility(View.VISIBLE);
				women_select.setVisibility(View.GONE);
				sex = 0;
			}
		});

		womenLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				man_select.setVisibility(View.GONE);
				women_select.setVisibility(View.VISIBLE);
				sex = 1;
			}
		});
//		content.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
//					int arg3) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence arg0, int arg1,
//					int arg2, int arg3) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void afterTextChanged(Editable arg0) {
//				// TODO Auto-generated method stub
//				if (arg0.length() > 50) {
//					ToastUtil.showToastInThread("个人简介不能超过50个字符!",
//							Toast.LENGTH_SHORT);
//				}
//			}
//		});
		avatar.setOnClickListener(this);
		modify.setOnClickListener(this);
		getUserInfo(LoginUser.getpin());
	}

	private void getUserInfo(String pin) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getUserInfoParams(pin), true,
				new MyAsyncHttpResponseHandler(ModifyUserInfoActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(ModifyUserInfoActivity.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);

						UserInfo userinfo = GsonUtils.fromJson(result,
								UserInfo.class);
						if (userinfo != null) {
							if(null == userinfo.getList() || (null != userinfo.getList() && userinfo.getList().size() <=0)) {
								return;
							}
							userName = userinfo.getList().get(0).getPin();
							nickName = userinfo.getList().get(0).getNickName();
							uremark = userinfo.getList().get(0).getUremark();
							imgUrl = userinfo.getList().get(0)
									.getYunBigImageUrl();
							usex = userinfo.getList().get(0).getUsex();
							mHandler.sendMessage(mHandler.obtainMessage(0));
						} else
							Toast.makeText(ModifyUserInfoActivity.this,
									getString(R.string.network_connect_error),
									Toast.LENGTH_LONG).show();

					}

				});

	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				ImageLoader.getInstance().displayImage(imgUrl, avatar,
						GlobalVarable.getDefaultAvatarDisplayOptions(false));
				usernameEdit.setText(userName);
				nicknameEdit.setText(nickName);
				// 切换后将EditText光标置于末尾
				nicknameEdit.postInvalidate();
				CharSequence charSequence = nicknameEdit.getText();
				if (charSequence instanceof Spannable) {
					Spannable spanText = (Spannable) charSequence;
					Selection.setSelection(spanText, charSequence.length());
				}
				content.setHint("这家伙很懒什么都没留下");
				content.setText(uremark);
				int usex_num = -1;
				try {
					if(!TextUtils.isEmpty(usex)) {	
						usex_num = Integer.parseInt(usex);
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if (usex_num == 0) {
					man_select.setVisibility(View.VISIBLE);
					women_select.setVisibility(View.GONE);
					womenTv.setTextColor(getResources().getColor(R.color.text_sub));
					sex = 0;
					womenLayout.setFocusable(false);
					womenLayout.setEnabled(false);
				} else if (usex_num == 1) {
					man_select.setVisibility(View.GONE);
					women_select.setVisibility(View.VISIBLE);
					manTv.setTextColor(getResources().getColor(R.color.text_sub));
					sex = 1;
					manLayout.setFocusable(false);
					manLayout.setEnabled(false);
				} else if (usex_num == 2) {
					man_select.setVisibility(View.GONE);
					women_select.setVisibility(View.GONE);
					sex = 2;
				}

				break;
			case 1:
				finish();
				break;
			default:
				break;
			}
		}

	};
	private TextView manTv;
	private TextView womenTv;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CAMERA:
			if (resultCode == RESULT_OK) {
				// startPhotoZoom(thumbnailUri);
				// resizePicture(thumbnailUri);
				// getContentResolver().delete(thumbnailUri, null, null);
				File temp = new File(Environment.getExternalStorageDirectory()
						+ "/temp1.jpg");
				startPhotoZoom(Uri.fromFile(temp));
			}
			break;
		case CHOOSE_IMAGE:
			if (resultCode == RESULT_OK) {
				startPhotoZoom(data.getData());
				// resizePicture(data.getData());
			}
			break;
		// 取得裁剪后的图片
		case 3:
			/**
			 * 非空判断大家一定要验证，如果不验证的话， 在剪裁之后如果发现不满意，要重新裁剪，丢弃
			 * 当前功能时，会报NullException，小马只 在这个地方加下，大家可以根据不同情况在合适的 地方做判断处理类似情况
			 * 
			 */
			if (data != null) {
				resizePicture(data);
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		/*
		 * 至于下面这个Intent的ACTION是怎么知道的，大家可以看下自己路径下的如下网页
		 * yourself_sdk_path/docs/reference/android/content/Intent.html
		 * 直接在里面Ctrl+F搜：CROP ，之前小马没仔细看过，其实安卓系统早已经有自带图片裁剪功能, 是直接调本地库的，小马不懂C C++
		 * 这个不做详细了解去了，有轮子就用轮子，不再研究轮子是怎么 制做的了...吼吼
		 */
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 3);
	}

	@Override
	public void onClick(View v) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(R.string.change_thumbnail);
		dialogBuilder.setItems(R.array.change_thumbnail,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (which) {
						case 0:
							Intent intent = new Intent();
							thumbnailUri = generateUri();
							intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
							// intent.putExtra(MediaStore.EXTRA_OUTPUT,
							// thumbnailUri);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
									.fromFile(new File(Environment
											.getExternalStorageDirectory(),
											"temp1.jpg")));
							startActivity(intent, CAMERA, R.string.no_camera);
							break;
						case 1:
							Intent intent2 = new Intent();
							intent2.setAction(Intent.ACTION_PICK);
							intent2.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							startActivity(intent2, CHOOSE_IMAGE,
									R.string.no_image_chooser);
							break;
						}
					}

					private Uri generateUri() {
						ContentValues values = new ContentValues(0);
						Uri uri = getContentResolver().insert(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								values);
						return uri;
					}

				});
		dialogBuilder.create().show();
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
			Toast.makeText(ModifyUserInfoActivity.this, id, Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * 重新设置用户头像的大小，截掉多余的部分
	 * 
	 * @param uri
	 *            头像所在的Uri
	 */
	private void resizePicture(Intent picdata) {
		// Bitmap bitmap = ImageUtils.getBitmapFromStream(uri,
		// getContentResolver(), PIC_SIZE, PIC_SIZE);
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			Bitmap bitmap = extras.getParcelable("data");
			if (bitmap != null) {
				isupdateImg = true;
				avatar.setImageBitmap(bitmap);
				imagebyte = UiStaticMethod.bitmapToByteArray(bitmap);
			}
		}
	}

	@Override
	public void onRightMenuOneClick() {
		// TODO Auto-generated method stub
		super.onRightMenuOneClick();
		String nickNameStr = null;
		String userMarkString = null;

		nickNameStr = nicknameEdit.getText().toString();
		userMarkString = content.getText().toString();
		if (TextUtils.isEmpty(userMarkString)) {
			userMarkString = "这家伙很懒什么都没有留下";
		}
		if (!TextUtils.isEmpty(nickNameStr) && nickNameStr.length() >= 4
				&& nickNameStr.length() < 21) {
			if (!TextUtils.isEmpty(userMarkString)
					&& userMarkString.length() <= 50) {
				if (sex == 2) {
					Toast.makeText(ModifyUserInfoActivity.this, "性别不能为空！",
							Toast.LENGTH_LONG).show();
					return;
				}
				
				if(nickNameStr.equals(nickName) && userMarkString.equals(uremark) &&
						Integer.parseInt(usex) == sex && !isupdateImg) {
					finish();
				}else {
					if (isNicknameAvalid(nickNameStr)) {
						isupdateInfo = true;
						submit(nickNameStr, userMarkString);
					}
				}
				
			} else {
				Toast.makeText(ModifyUserInfoActivity.this, "个人简介超过50个字符!",
						Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(ModifyUserInfoActivity.this, "昵称不能为空且长度为4—20字符!",
					Toast.LENGTH_LONG).show();
		}
		if (isupdateImg) {
			String imgStr = new String(Base64.encodeBytes(imagebyte));
			submitImg(imgStr);
		}
	}

	/**
	 * 
	 * @prama: str 要判断是否包含特殊字符的目标字符串
	 */

	private boolean isNicknameAvalid(String str) {
		String limitEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Pattern pattern = Pattern.compile(limitEx);
		Matcher m = pattern.matcher(str);

		if (m.find()) {
			Toast.makeText(ModifyUserInfoActivity.this, "昵称中不能包含特殊字符！",
					Toast.LENGTH_LONG).show();
			return false;
		} else {
			return true;
		}

	}

	private void submitImg(final String imgStr) {
		RequestSecurityKeyTask task = new RequestSecurityKeyTask(
				new RequestSecurityKeyTask.OnGetSessionKeyListener() {

					@Override
					public void onGetSessionKeySucceed() {
						WebRequestHelper.post(URLText.JD_BASE_URL,
								RequestParamsPool.putUserImage(imgStr), true,
								new MyAsyncHttpResponseHandler(
										ModifyUserInfoActivity.this) {

									@Override
									public void onFailure(int arg0,
											Header[] arg1, byte[] arg2,
											Throwable arg3) {
										// TODO Auto-generated method
										// stub
										Toast.makeText(
												ModifyUserInfoActivity.this,
												getString(R.string.network_connect_error),
												Toast.LENGTH_SHORT).show();
									}

									@Override
									public void onResponse(int statusCode,
											Header[] headers,
											byte[] responseBody) {
										// TODO Auto-generated method
										// stub

										String result = new String(responseBody);
										String code = null;
										String message = null;
										JSONObject jsonObj;
										try {
											jsonObj = new JSONObject(
													new String(responseBody));
											if (jsonObj != null) {
												JSONObject desJsonObj = null;
												code = jsonObj
														.optString("code");
												message = jsonObj
														.optString("message");
											}
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										if (Integer.parseInt(code) == 0) {
											if (isupdateImg
													&& isupdateInfo == false) {
												mHandler.sendMessage(mHandler
														.obtainMessage(1));
												Toast.makeText(
														ModifyUserInfoActivity.this,
														"头像上传成功!",
														Toast.LENGTH_LONG)
														.show();
											}

											if (isupdateImg && isupdateInfo
													&& infocode == 0) {
												mHandler.sendMessage(mHandler
														.obtainMessage(1));
												Toast.makeText(
														ModifyUserInfoActivity.this,
														"提交成功!",
														Toast.LENGTH_LONG)
														.show();
											}
										} else {
											if (!TextUtils.isEmpty(message)) {
												Toast.makeText(
														ModifyUserInfoActivity.this,
														message,
														Toast.LENGTH_LONG)
														.show();

											}
										}
									}

								});
					}

					@Override
					public void onGetSessionKeyFailed() {
						Toast.makeText(ModifyUserInfoActivity.this,
								getString(R.string.unknown_error),
								Toast.LENGTH_LONG).show();

					}
				});

		task.excute();
	}

	private void submit(final String username, final String userMarkString) {
		RequestSecurityKeyTask task = new RequestSecurityKeyTask(
				new RequestSecurityKeyTask.OnGetSessionKeyListener() {

					@Override
					public void onGetSessionKeySucceed() {
						WebRequestHelper.get(URLText.JD_BASE_URL,
								RequestParamsPool.putUserInfo(sex + "",
										username, userMarkString), true,
								new MyAsyncHttpResponseHandler(
										ModifyUserInfoActivity.this) {

									@Override
									public void onFailure(int arg0,
											Header[] arg1, byte[] arg2,
											Throwable arg3) {
										// TODO Auto-generated method
										// stub
										Toast.makeText(
												ModifyUserInfoActivity.this,
												getString(R.string.network_connect_error),
												Toast.LENGTH_SHORT).show();
									}

									@Override
									public void onResponse(int statusCode,
											Header[] headers,
											byte[] responseBody) {
										// TODO Auto-generated method
										// stub

										String result = new String(responseBody);
										String code = null;
										String message = null;
										JSONObject jsonObj;
										try {
											jsonObj = new JSONObject(
													new String(responseBody));
											if (jsonObj != null) {
												JSONObject desJsonObj = null;
												code = jsonObj
														.optString("code");
												message = jsonObj
														.optString("message");
											}
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										if (Integer.parseInt(code) == 0) {
											infocode = 0;
											if (isupdateImg == false
													&& isupdateInfo) {
												mHandler.sendMessage(mHandler
														.obtainMessage(1));
												Toast.makeText(
														getApplicationContext(),
														"提交成功!", 1).show();
											}
										} else {
											if ("ERROR_USER_NICK_ONLY"
													.equals(message)) {
												Toast.makeText(
														ModifyUserInfoActivity.this,
														"昵称已存在！",
														Toast.LENGTH_LONG)
														.show();
											} else {
												if("2".equals(code)) {
													message = "此昵称重复，请修改";
												}else {
													message = "提交失败！";
												}
												System.out.println("TTTTT=======code="+code+"========message="+message);
												Toast.makeText(
														ModifyUserInfoActivity.this,
														message,
														Toast.LENGTH_LONG)
														.show();
											}
										}
									}

								});
					}

					@Override
					public void onGetSessionKeyFailed() {
						Toast.makeText(ModifyUserInfoActivity.this,
								getString(R.string.unknown_error),
								Toast.LENGTH_LONG).show();

					}
				});

		task.excute();
	}
	
	
	private class NameLengthFilter implements InputFilter {  
        int MAX_EN;// 最大英文/数字长度 一个汉字算两个字母  
        String regEx = "[\\u4e00-\\u9fa5]"; // unicode编码，判断是否为汉字  
  
        public NameLengthFilter(int mAX_EN) {  
            super();  
            MAX_EN = mAX_EN;  
        }  
  
        @Override  
        public CharSequence filter(CharSequence source, int start, int end,  
                Spanned dest, int dstart, int dend) {  
            int destCount = dest.toString().length()  
                    + getChineseCount(dest.toString());  
            int sourceCount = source.toString().length()  
                    + getChineseCount(source.toString());  
            if (destCount + sourceCount > MAX_EN) {  
            	Toast.makeText(ModifyUserInfoActivity.this, "已达到最大字数限制",
						 Toast.LENGTH_SHORT).show();
                return "";  
  
            } else {  
                return source;  
            }  
        }  
  
        private int getChineseCount(String str) {  
            int count = 0;  
            Pattern p = Pattern.compile(regEx);  
            Matcher m = p.matcher(str);  
            while (m.find()) {  
                for (int i = 0; i <= m.groupCount(); i++) {  
                    count = count + 1;  
                }  
            }  
            return count;  
        }  
    }  
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_modify_userinfo));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_modify_userinfo));
	}

}
