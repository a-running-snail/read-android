package com.jingdong.app.reader.privateMsg;

import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.Log;

public class PrivateMessage extends LastMessage implements Parcelable {
	public final static Creator<PrivateMessage> CREATOR = new Creator<PrivateMessage>() {

		@Override
		public PrivateMessage createFromParcel(Parcel source) {
			return new PrivateMessage(source);
		}

		@Override
		public PrivateMessage[] newArray(int size) {
			return new PrivateMessage[size];
		}
	};
	private final static String ID = "id";
	private final static String SENDER_ID = "sender_id";
	private final static String RECEIVER_ID = "receiver_id";
	private final static String DOCUMENT_REQUEST = "document_request";
	private final static String DOCUMENT = "document";
	private long id;
	private String senderId;
	private String receiverId;
	private DocumentRequest documentRequest;
	private Document document;

	private PrivateMessage(Parcel source) {
		super(source);
		id = source.readLong();
		senderId = source.readString();
		receiverId = source.readString();
		if (source.readByte() == 1)
			documentRequest = source.readParcelable(DocumentRequest.class.getClassLoader());
		if (source.readByte() == 1)
			document = source.readParcelable(Document.class.getClassLoader());
	}

	private PrivateMessage() {

	}

	public final static PrivateMessage fromJson(JSONObject jsonObject) {
		PrivateMessage privateMessage = new PrivateMessage();
		privateMessage.parseJson(jsonObject);
		privateMessage.setId(jsonObject.optLong(ID));
		privateMessage.setSenderId(jsonObject.optString(SENDER_ID));
		privateMessage.setReceiverId(jsonObject.optString(RECEIVER_ID));
		JSONObject requestJson = jsonObject.optJSONObject(DOCUMENT_REQUEST);
		if (requestJson != null) {
			DocumentRequest documentRequestJson = DocumentRequest.fromJson(requestJson);
			privateMessage.setDocumentRequest(documentRequestJson);
		}
		JSONObject documentJson = jsonObject.optJSONObject(DOCUMENT);
		if (documentJson != null) {
			Document document = Document.fromJson(documentJson);
			privateMessage.setDocument(document);
		}
		return privateMessage;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof PrivateMessage && ((PrivateMessage) o).id == id;
	}

	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(id);
		dest.writeString(senderId);
		dest.writeString(receiverId);
		dest.writeByte((byte) (documentRequest != null ? 1 : 0));
		if (documentRequest != null)
			dest.writeParcelable(documentRequest, flags);
		dest.writeByte((byte) (document != null ? 1 : 0));
		if (document != null)
			dest.writeParcelable(document, flags);
	}

	public long getId() {
		return id;
	}

	public String getSenderId() {
		return senderId;
	}

	public String getReceiverId() {
		return receiverId;
	}

	public DocumentRequest getDocumentRequest() {
		return documentRequest;
	}

	public Document getDocument() {
		return document;
	}

	/**
	 * 判断这条信息是不是当前用户发出的
	 * 
	 * @param Context
	 *            数据上下文，通过数据上下文才能取得当前用户id
	 * @return true表示这条信息是当前用户发出的
	 */
	public boolean isFromMe(Context context) {
		if (this.senderId.equals(LocalUserSetting.getUser_id(context))) {
			return true;
		}
		return false;
	}

	void setId(long id) {
		this.id = id;
	}

	void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}

	void setDocumentRequest(DocumentRequest documentRequest) {
		this.documentRequest = documentRequest;
	}

	void setDocument(Document document) {
		this.document = document;
	}
}
