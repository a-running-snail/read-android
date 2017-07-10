package com.jingdong.app.reader.me.model;

public interface BookNoteModelInterface {
	public enum TYPE {
		LOAD, MAKE_PUBLIC, MAKE_PRIVATE, DELETE,MORE
	}

	/**
	 * 返回列表的行数
	 * @return 列表的行数
	 */
	public abstract int getRowNumber();

	/**
	 * 返回指定行的内容，可能为String或者BookNoteInterface
	 * @param position 行序号
	 * @return 某行的内容。
	 */
	public abstract Object getRowAt(int position);

}
