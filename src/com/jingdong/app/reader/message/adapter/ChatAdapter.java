package com.jingdong.app.reader.message.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.message.model.ApproveBorrowInterface;
import com.jingdong.app.reader.privateMsg.DocumentRequest;
import com.jingdong.app.reader.privateMsg.DocumentRequest.BorrowStatus;
import com.jingdong.app.reader.privateMsg.PrivateMessage;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.ListInterface;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ChatAdapter extends BaseAdapter implements RecyclerListener {

	private static class ViewHolder {
		RoundNetworkImageView avatar;
		TextView time;
		TextView message;
		RelativeLayout book;
		ImageView bookCover;
		TextView bookName;
		TextView bookAuthor;
		View boundary;
		TextView confirm;
	}

	private static enum BubbleType {
		Me, Friends
	};

	private ListInterface<PrivateMessage> list;
	private ApproveBorrowInterface approveBorrow;
	private Context context;
	private UserInfo user;
	private UserInfo me;

	public ChatAdapter(ListInterface<PrivateMessage> list, Context context,
			ApproveBorrowInterface approveBorrowInterface, UserInfo user) {
		this.list = list;
		this.user = user;
		this.context = context;
		this.approveBorrow = approveBorrowInterface;
		me = LocalUserSetting.getUserInfo(context);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public int getItemViewType(int position) {
		PrivateMessage message = (PrivateMessage) getItem(position);
		if (message.isFromMe(context))
			return BubbleType.Me.ordinal();
		else
			return BubbleType.Friends.ordinal();
	}

	@Override
	public int getViewTypeCount() {
		return BubbleType.values().length;
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			if (getItemViewType(position) == BubbleType.Me.ordinal()){
				convertView = View.inflate(context, R.layout.item_chat_me, null);
			}
			else if (getItemViewType(position) == BubbleType.Friends.ordinal()){
				convertView = View.inflate(context, R.layout.item_chat_friend, null);
			}
			initViewHolder(convertView);
		}
		fillContent(convertView, position);
		return convertView;
	}

	@Override
	public void onMovedToScrapHeap(View view) {
		UiStaticMethod.onMovedToScrapHeap(view, RelativeLayout.class, R.id.bookCover);
		ViewHolder viewHolder = (ViewHolder) view.getTag();
		viewHolder.confirm.setOnClickListener(null);
		ImageLoader.getInstance().cancelDisplayTask(viewHolder.bookCover);
	}

	private static void initViewHolder(View convertView) {
		ViewHolder viewHolder = new ViewHolder();
//		viewHolder.avatar = (RoundNetworkImageView) convertView.findViewById(R.id.thumb_nail);
		viewHolder.time = (TextView) convertView.findViewById(R.id.time);
		viewHolder.message = (TextView) convertView.findViewById(R.id.message);
		viewHolder.book = (RelativeLayout) convertView.findViewById(R.id.book);
		viewHolder.bookCover = (ImageView) convertView.findViewById(R.id.bookCover);
		viewHolder.bookName = (TextView) convertView.findViewById(R.id.bookName);
		viewHolder.bookAuthor = (TextView) convertView.findViewById(R.id.bookAuthor);
		viewHolder.boundary = convertView.findViewById(R.id.boundary);
		viewHolder.confirm = (TextView) convertView.findViewById(R.id.confirm);
		convertView.setTag(viewHolder);
	}

	private void fillContent(View convertView, int position) {
		ViewHolder viewHolder = (ViewHolder) convertView.getTag();
		PrivateMessage privateMessage = (PrivateMessage) getItem(position);
//		if (privateMessage.isFromMe(context)) {
//			ArticleHelper.initAvatar(context, viewHolder.avatar, me);
//		} else {
//			ArticleHelper.initAvatar(context, viewHolder.avatar, user);
//		}
		
		viewHolder.time.setText(TimeFormat.formatTime(context.getResources(), privateMessage.getTimeStamp()));
		UiStaticMethod.setUrlClickable(context, viewHolder.message, privateMessage.getBody());
		boolean visibility = (privateMessage.getDocument() != null);
		changeContentVisibility(viewHolder, visibility);
		initDocument(viewHolder, privateMessage, visibility);
	}

	/**
	 * @param viewHolder
	 * @param privateMessage
	 * @param visibility
	 */
	private void initDocument(ViewHolder viewHolder, PrivateMessage privateMessage, boolean visibility) {
		if (visibility) {
			viewHolder.bookName.setText(privateMessage.getDocument().getTitle());
			Book book = privateMessage.getDocument().getBook();
			if (book != null) {
				if (book.getCover() != null)
				{

				    
				    ImageLoader.getInstance().displayImage(book.getCover() + "!w100h150",viewHolder.bookCover, GlobalVarable.getDefaultBookDisplayOptions());
		             
				
				}
				else
				    ImageLoader.getInstance().displayImage("",viewHolder.bookCover, GlobalVarable.getDefaultBookDisplayOptions());
                
				String authorName=book.getAuthorName();
				if(TextUtils.isEmpty(authorName))
					viewHolder.bookAuthor.setText("");
				else
					viewHolder.bookAuthor.setText(book.getAuthorName());
				viewHolder.bookName.setText(book.getTitle());
			} else {
			    ImageLoader.getInstance().displayImage("",viewHolder.bookCover, GlobalVarable.getDefaultBookDisplayOptions());
                
				viewHolder.bookAuthor.setText("");
			}
			setConfirmText(viewHolder.confirm, privateMessage,
					privateMessage.getSenderId() == LocalUserSetting.getUserId(context));
		}
	}

	/**
	 * @param viewHolder
	 * @param visibility
	 */
	private void changeContentVisibility(ViewHolder viewHolder, boolean visibility) {
		if (visibility) {
			viewHolder.book.setVisibility(View.VISIBLE);
			viewHolder.boundary.setVisibility(View.VISIBLE);
			viewHolder.confirm.setVisibility(View.VISIBLE);
		} else {
			viewHolder.book.setVisibility(View.GONE);
			viewHolder.boundary.setVisibility(View.GONE);
			viewHolder.confirm.setVisibility(View.GONE);
		}
	}

	/**
	 * 
	 * @param confirmText
	 * @param privateMessage
	 * @param fromMe
	 */
	private void setConfirmText(TextView confirmText, final PrivateMessage privateMessage, final boolean fromMe) {
		final DocumentRequest documentRequest = privateMessage.getDocumentRequest();
		if (documentRequest != null) {
			initTextWithDocRequest(confirmText, privateMessage, fromMe, documentRequest);
		} else {
			initTextWithoutDocRequest(confirmText, privateMessage);
		}
	}

	private void initTextWithoutDocRequest(TextView confirmText, final PrivateMessage privateMessage) {
		final boolean sender = (privateMessage.getDocument().getUserId().equals(LoginUser.getpin()));
		int resId;
		if (sender)
			resId = R.string.private_msg_share_success;
		else
			resId = R.string.click_download;
		confirmText.setText(resId);
		confirmText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!sender) {
					approveBorrow.getDownloadLink(privateMessage.getId(), privateMessage.getDocument().getTitle());
				}
			}
		});
	}

	private void initTextWithDocRequest(TextView confirmText, final PrivateMessage privateMessage,
			final boolean fromMe, final DocumentRequest documentRequest) {
		final BorrowStatus status = documentRequest.getStatus();
		confirmText.setText(getConfirmStrings(privateMessage, fromMe)[status.ordinal()]);
		confirmText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (status == BorrowStatus.REQUEST && !fromMe) {
					approveBorrow.approveBorrow(privateMessage.getDocument(), documentRequest);
				} else if (status == BorrowStatus.ACCEPT && !fromMe) {
					approveBorrow.getDownloadLink(privateMessage.getId(), privateMessage.getDocument().getTitle());
				}
			}
		});
	}

	private String[] getConfirmStrings(PrivateMessage privateMessage, boolean fromMe) {
		String[] array = new String[3];
		boolean downloadLink = showDownloadString(privateMessage);
		if (fromMe) {
			array[0] = context.getString(R.string.waiting_accept);
			array[2] = context.getString(R.string.rejected);
		} else {
			array[0] = context.getString(R.string.accept);
			array[2] = context.getString(R.string.reject);
		}
		if (downloadLink)
			array[1] = context.getString(R.string.click_download);
		else
			array[1] = context.getString(R.string.accepted);
		return array;
	}

	private boolean showDownloadString(PrivateMessage privateMessage) {
		return (LoginUser.getpin() .equals(privateMessage.getDocumentRequest().getSenderId()))
				&& (privateMessage.getSenderId().equals(privateMessage.getDocumentRequest().getReceiverId()));
	}

}
