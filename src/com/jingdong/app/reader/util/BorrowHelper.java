package com.jingdong.app.reader.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jingdong.app.reader.message.activity.ChatActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.request.Error;
import com.jingdong.app.reader.request.Success;
import com.jingdong.app.reader.service.OpdsBookDownloadService;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.loopj.android.http.RequestParams;
import com.jingdong.app.reader.R;

public class BorrowHelper {
	private final static String DOCUMENT_ID = "document_id";

	
	public void task(){
	    final Dialog dialog = ProgressDialog.show(context, title, message, true, true, initCancelListener());
	    Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("auth_token", LocalUserSetting.getToken(context));
        paramMap.put(DOCUMENT_ID, String.valueOf(documentId));
        String baseUrl = URLText.borrowRequest;
        RequestParams params=new RequestParams(paramMap);
        WebRequestHelper.post(baseUrl, params, new MyAsyncHttpResponseHandler(context) {
            
            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                Toast.makeText(context, context.getResources().getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onResponse(int statusCode,
                    Header[] headers, byte[] responseBody) {
                dialog.dismiss();
                Error error;
                Success success;
                String result=new String(responseBody);
                if ((error = Error.fromJson(result)) != null) {
                    if (error.isNotUpload()) {
                        startChatActivity();
                    } else if (error.isNotBorrowable()) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else if ((success = Success.fromJson(result)) != null) {
                    if (success.getDownloadLink() == null || success.getDownloadLink().equals("")) {
                        startChatActivity();
                    } else {
                        downloadBook(success.getDownloadLink(), bookName);
                    }
                }
                
            }
        });
        
	}

	private final Context context;
	private final String title;
	private final String message;
	private final String bookName;
	private final long documentId;
	private final String userId;

	public BorrowHelper(Context context, String bookName, long documentId, String userId) {
		this.context = context;
		this.bookName = bookName;
		this.documentId = documentId;
		this.userId = userId;
		Resources resources = context.getResources();
		title = resources.getString(R.string.borrow);
		message = resources.getString(R.string.borrowing);
	}

	public void show() {
		task();
	}

	private OnCancelListener initCancelListener() {
		return new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				WebRequestHelper.cancleRequest(context);
			}
		};
	}

	private void downloadBook(String downloadLink, String bookName) {
		Toast.makeText(
				context,
				context.getString(R.string.start_download) + "\"" + bookName + ".epub\""
						+ context.getString(R.string.to_3rd_bookcase), Toast.LENGTH_LONG).show();
		Intent intent = new Intent(context, OpdsBookDownloadService.class);
		intent.putExtra(OpdsBookDownloadService.OpdsBookUrlPathKey, downloadLink);
		intent.putExtra(OpdsBookDownloadService.OpdsBookNameKey, bookName);
		context.startService(intent);
	}

	private void startChatActivity() {
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(ChatActivity.USER_ID, userId);
		context.startActivity(intent);
	}
}
