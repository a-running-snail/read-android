package com.jingdong.app.reader.reading;

import com.jingdong.app.reader.epub.paging.Page;

public class FinishPage extends Page {

    Page prevPage;
    
    public FinishPage(Page prevPage) {
        super();
        this.prevPage = prevPage;
    }
    
    public Page getPrevPage() {
        return prevPage;
    }


}
