package com.jingdong.app.reader.book;

import java.io.Serializable;

public class DocBind implements Serializable{
    
    public int documentId;
    public String userId;
    public long serverId = 0;
    public long bookId = 0;
    public int bind;
    public String serverTitle = "";
    public String serverAuthor = "";
    public String serverCover = "";
    
    

}
