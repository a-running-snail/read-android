package com.jingdong.app.reader.epub.paging;

import com.jingdong.app.reader.notes.NotesModel;
import com.jingdong.app.reader.reading.BookMark;
import com.jingdong.app.reader.reading.BookBackCoverView;
import com.jingdong.app.reader.reading.ReadNote;

import android.graphics.Bitmap;
import android.view.View;


public interface IReadFunction {
    public boolean playAudio(String id);
    public boolean isPlaying(String id);
    public boolean isShowSearchBar();
    public boolean isFirstPage(Page page);
    public boolean isFinishPage(Page page);
    public boolean isPageAnimationSlide();
    public boolean isPageAnimationTurning();
    public String getDownloadFontText();
    public String getSearchKeywords();
    public float getPageWidth();
    public void nextPage(boolean smoothScroll);
    public void prevPage(boolean smoothScroll);
    public void forceGotoNextPageInAnimation();
    public void forceGotoPrevPageInAnimation();
    public void savePageAnimationTime();
    public long getPageAnimationTime();
    public void preparePageTurning(boolean isNext);
    public void openSetting();
    public void openSearch();
    public void hideSearchBar();
    public void showBackCover();
    public void startDownloadFont();
    public void hideDownloadFontBar();
    public void beginNoteSelection();
    public void endNoteSelection();
    public void stopAndClearSearch();
    public void gotoSearchPage(int chapterIndex, int paraIndex, int offsetInPara);
    public void asyncRequestCreateNote(String quote, Kit42View kit42View);
    public void asyncRequestModifyNote(ReadNote note, Kit42View kit42View);
    public void asyncRequestTranslate(String words, Kit42View kit42View);
    public ReadNote createEmptyNote();
    public BookBackCoverView getLastPageView();
    public View getLoadingView();
    public void refreshNotes();
    public BookMark createEmptyMark();
    public NotesModel findMatchNoteUser(String userId);
    public void requestAllNotesModel();
    public void commentNote(String guid, long noteId);
    public void doJumpChapterAction(String url, String basePath);
    public void doJumpAnchorAction(String id);
    public void showLoading();
    public void shareReadNoteViaSinaWeibo(ReadNote note, Bitmap bitmap);
    public void shareReadNoteViaWeixin(ReadNote note, Bitmap bitmap,int type);
    public int getBatteryPercent();
    public Chapter getNextChapter(Chapter chapter);
    public Chapter getPrevChapter(Chapter chapter);
}
