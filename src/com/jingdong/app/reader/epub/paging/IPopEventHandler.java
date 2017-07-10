package com.jingdong.app.reader.epub.paging;

import com.jingdong.app.reader.notes.NotesModel;

public interface IPopEventHandler {

    public void onNoteShareCommunity();
    
    public void onNoteShareSinaWeibo();
    
    public void onNoteShareWeChat(int type);
    
    public void onNoteCopy();
    
    public void onDigestCreate(boolean isHideNoteActionBar);
    
    public void onNoteCreate();
    
    public void onNoteModify();
    
    public void onNoteDictionary();
    
    public void onNoteBaike();
    
    public void onNoteDestory();
    
    public void onNoteComment(String guid, long noteId);
    
    public NotesModel findUserNotesModel(String userId);
    
    public void refreshUserNotesModel();
}
