package com.jingdong.app.reader.bookshelf.inf;

import com.jingdong.app.reader.entity.extra.ChangduEbook;

public interface FetchChangduListListener {

	public void onSuccess(ChangduEbook changduEntity);
	public void onFailure(String errorInfo);
	
}
