package com.jingdong.app.reader.entity.extra;

import java.util.ArrayList;
import java.util.List;

public class BorrowBook {
	public int amount=0;
	public int code=0;
	public int currentPage=0;
	public int pageSize=0;
	public int totalPage=0;
	public List<JDBookDetail> resultList = new ArrayList<JDBookDetail>();
}
