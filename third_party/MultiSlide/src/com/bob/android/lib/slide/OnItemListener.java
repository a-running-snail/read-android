package com.bob.android.lib.slide;
import android.widget.AdapterView;

public interface OnItemListener {

	public void OnItemSelected(AdapterView<?> parent, Item item,
			int position, long id);

	public void OnItemLoading(AdapterView<?> parent, Item item,
			int position, long id);
}
