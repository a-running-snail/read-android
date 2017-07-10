package com.jingdong.app.reader.privateMsg;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class DocumentRequest implements Parcelable {
	public static final Creator<DocumentRequest> CREATOR = new Creator<DocumentRequest>() {

		@Override
		public DocumentRequest createFromParcel(Parcel source) {
			return new DocumentRequest(source);
		}

		@Override
		public DocumentRequest[] newArray(int size) {
			return new DocumentRequest[size];
		}
	};

	public static enum BorrowStatus {
		REQUEST, ACCEPT, DENY
	}

	private final static String ID = "id";
	private final static String SENDER_ID = "sender_id";
	private final static String RECEIVER_ID = "receiver_id";
	private final static String DOCUMENT_ID = "document_id";
	private final static String REQUEST_STATUS = "status";
	private final static String DOCUMENT_UPLOAD = "document_upload";
	private long id;
	private long senderId;
	private long receiverId;
	private long documentId;
	private BorrowStatus status;
	private boolean documentUpload;

	private DocumentRequest() {

	}

	private DocumentRequest(Parcel source) {
		id = source.readLong();
		senderId = source.readLong();
		receiverId = source.readLong();
		documentId = source.readLong();
		int status = source.readInt();
		if (status != -1) {
			BorrowStatus[] statuses = BorrowStatus.values();
			this.status = statuses[status];
		}
		documentUpload = (source.readByte() == 1) ? true : false;
	}

	public static final DocumentRequest fromJson(JSONObject jsonObject) {
		DocumentRequest documentRequest = new DocumentRequest();
		documentRequest.setId(jsonObject.optLong(ID));
		documentRequest.setSenderId(jsonObject.optLong(SENDER_ID));
		documentRequest.setReceiverId(jsonObject.optLong(RECEIVER_ID));
		documentRequest.setDocumentId(jsonObject.optLong(DOCUMENT_ID));
		BorrowStatus[] status = BorrowStatus.values();
		documentRequest.setStatus(status[jsonObject.optInt(REQUEST_STATUS)]);
		documentRequest.setDocumentUpload(jsonObject.optBoolean(DOCUMENT_UPLOAD));
		return documentRequest;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(senderId);
		dest.writeLong(receiverId);
		dest.writeLong(documentId);
		if (status != null)
			dest.writeInt(status.ordinal());
		else
			dest.writeInt(-1);
		dest.writeByte((byte) (documentUpload ? 1 : 0));
	}

	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof DocumentRequest && ((DocumentRequest) o).getId() == id;
	}

	@Override
	public String toString() {
		return status.toString();
	}
	
	public long getId() {
		return id;
	}

	void setId(long id) {
		this.id = id;
	}

	public long getSenderId() {
		return senderId;
	}

	void setSenderId(long sender_id) {
		this.senderId = sender_id;
	}

	public long getReceiverId() {
		return receiverId;
	}

	void setReceiverId(long receiver_id) {
		this.receiverId = receiver_id;
	}

	public long getDocumentId() {
		return documentId;
	}

	void setDocumentId(long documentId) {
		this.documentId = documentId;
	}

	public BorrowStatus getStatus() {
		return status;
	}

	public void setStatus(BorrowStatus status) {
		this.status = status;
	}

	public boolean isDocumentUpload() {
		return documentUpload;
	}

	void setDocumentUpload(boolean document_upload) {
		this.documentUpload = document_upload;
	}
}
