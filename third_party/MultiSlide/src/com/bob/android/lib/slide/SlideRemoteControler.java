package com.bob.android.lib.slide;

import android.view.MotionEvent;

public interface SlideRemoteControler {
	  public boolean isIntercepted();
	    public void handleTouchEventUp(MotionEvent event);
}
