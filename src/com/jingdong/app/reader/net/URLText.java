package com.jingdong.app.reader.net;

public interface URLText {
	//预发布环境需将https修改成http
//	public static final String JD_BASE_URL = "http://gw.e.jd.com/client.action";
//	public static final String JD_BOOK_STORE_URL = "http://gw.e.jd.com/client.action";
//	public static final String uploadImage = "http://gw.e.jd.com/uploadImage/upload.action";
//	public static final String JD_BOOK_UPLOAD_URL ="http://gw.e.jd.com/upload/uploadBook.action";
//	public static final String JD_BOOK_CHANGDU_URL = "http://cread.e.jd.com/client.action";
//	public static final String JD_BOOK_DOWNLOAD_URL = "http://rights.e.jd.com/client.action";
//	//解绑
//	public static final String BIND_URL = "http://rights.e.jd.com/client.action?";
//	public static final String JD_BOOK_ORDER_URL = "http://order.e.jd.com/client.action";
//	public static final String JD_BOOK_READ_URL = "http://notes.e.jd.com/client.action";
//	public static final String baseUrl = "http://community.e.jd.com";
	
	
	public static final String JD_BASE_URL = "https://gw-e.jd.com/client.action";
	public static final String JD_BOOK_STORE_URL = "https://gw-e.jd.com/client.action";
	/** 图片上传到云盘地址 */
	public static final String uploadImage = "https://gw-e.jd.com/uploadImage/upload.action";
	public static final String JD_BOOK_UPLOAD_URL ="https://gw-e.jd.com/upload/uploadBook.action";
	public static final String JD_BOOK_CHANGDU_URL = "https://cread.jd.com/client.action";
	public static final String JD_BOOK_DOWNLOAD_URL = "https://rights-e.jd.com/client.action";
	//解绑
	public static final String BIND_URL = "https://rights-e.jd.com/client.action?";
	public static final String JD_BOOK_ORDER_URL = "https://order-e.jd.com/client.action";
	public static final String JD_BOOK_READ_URL = "https://notes-e.jd.com/client.action";
	public static final String baseUrl = "https://sns-e.jd.com";
	
	
	//京东后台API接口 开始
	public static final String JD_GET_RSA_PUBLIC_KEY=JD_BASE_URL+"?functionId=rsaPublicKey";
	
	public static final String JD_HOT_KEY_URL = JD_BASE_URL + "?functionId=keywordByRand";
	public static final String JD_USER_SEARCH_URL = "http://gw.e.jd.com/client.action?";

	public static final String JD_UNION_LOGIN_URL = "http://gw.m.jd.com/client.action?";
	
	public static final String JD_WEBVIEW__URL="http://gw.m.jd.com/client.action";
	public static final String JD_PAY_URL="http://pay.m.jd.com/index.action";
	public static final String JD_PAPER_BOOK_SHARE_URL = "http://item.m.jd.com/ware/view.action?";

	//云盘列表
	public static final String MZ_BOOK_YUNPAN_URL = baseUrl+"/user_document_files";
	public static final String MZ_BOOK_YUNPAN_DOWNLOAD_URL = baseUrl+"/user_document_files/download";
	public static final String MZ_BOOK_YUNPAN_SEARCH_URL = baseUrl+"/user_document_files/search_mine";

	public static final String synServerId = baseUrl+"/documents.json";
	public static final String synCloudDiskBook = baseUrl+"/user_document_files/sync.json";
	public static final String uploadedBookReBack = baseUrl+"/user_document_files/bind.json";

	public static final String bookComment = baseUrl+"/book_comments";
	public static final String wishStatus = baseUrl+"/books/status";
	public static final String unwishBook = baseUrl+"/mark_books/unwish";
	public static final String wishBook = baseUrl+"/mark_books/wish";

	public static final String readStatistic = baseUrl+"/mark_books/stat";
	
	//京东后台API接口 结束
	
	//想读
	public static final String WANT_URL = baseUrl+"/mark_books";
	//timeline
	public static final String TimeLine_URL = baseUrl+"/entities";
	//动态搜索
	public static final String TimeLine_Search_URL = baseUrl+"/entities/search";
	//动态详情
	public static final String TimeLine_detail_URL = baseUrl+"/entities/";
	//分享回调
	public static final String SHARE_URL = baseUrl+"/shares";
	//关注
	public static final String Focus_URL = baseUrl+"/follows/followings";
	//粉丝
	public static final String Fans_URL = baseUrl+"/follows/followers";
	//关注统计
	public static final String Follows_URL = baseUrl+"/follows/follow_number";
	//关注某人
	public static final String Follow_SomeOne_URL = baseUrl+"/follows/follow";
	//取消关注
	public static final String Follow_Cancle = baseUrl+"/follows/unfollow";
	//个人主页
	public static final String Personal_homepage = baseUrl+"/users/public";
	//提醒
	public static final String Alerts_URL = baseUrl+"/alerts";
	//会话
	public static final String Message_URL = baseUrl+"/conversations";
	//删除会话
	public static final String Delete_Message_URL = baseUrl+"/conversations/";
	//消息数量
	public static final String Notification_num_URL = baseUrl+ "/entities/notifications";
	//发送消息
	public static final String Send_Message_URL = baseUrl+"/messages";
	//提到我
	public static final String Atme_URL = baseUrl+"/at_users";
	//评论
	public static final String Comment_URL = baseUrl+"/users/comments";
	//用户说说
	public static final String User_tweets = baseUrl+"/users/user_tweets";
	//笔记
	public static final String Books_Notes = baseUrl+"/users/books_notes";
	//书评
	public static final String Book_Comment_URL = baseUrl+"/users/public_bookcomments";
	//收藏
	public static final String Favourites_URL = baseUrl+"/favourites";
	//外部导入
	public static final String Import_book = baseUrl+"/documents/document_shelf";
	//阅读记录
	public static final String Reading_data = baseUrl+"/books/reading_data";
	//某人某本书的笔记列表
	public static final String User_book_notes = baseUrl+"/notes/user_book_notes";
	//笔记动态
	public static final String BookNote_Time = baseUrl+"/notes/show_as_entity";
	//用户搜索
	public static final String searchPeople = baseUrl + "/users/search";
	//推荐用户
	public static final String Recommend_URL = baseUrl+"/users/recommend_user";
	//批量关注
	public static final String FollowALL_URL = baseUrl+"/follows/follow";
	//清除消息
	public static final String Clear_Message = "http://test.mzread.com:8080/entities/reset_notification";
	
	//拇指后台接口 开始
	
	public static final String getBaikeWords="http://wapbaike.baidu.com/item/%s?adapt=1";
	public static final String getTranslatedWords="http://fanyi.youdao.com/openapi.do?keyfrom=mzread&key=1058223933&type=data&doctype=json&version=1.1&q=:keyword" ;
 	public static final String getChapterAllNotes=baseUrl + "/notes/notes_by_chapter";
	public static final String getSomeoneAllNotes=baseUrl + "/notes/user_book_notes";
 	public static final String getAllNotesAuthorsOfBook = baseUrl + "/notes/users_list";
	public static final String syncBookMarks=baseUrl + "/bookmarks/sync.json";
	public static final String searchPurchasedUrl=baseUrl + "/api/v1/mine/ebooks_users/search";
	public static final String getOthersReadingUrl=baseUrl + "/explore/recent_books.json";
	public static final String getOthersReadingUrlNew=baseUrl + "/books/recent_books";
	
	
	public static final String getFreeGiftsUrl=baseUrl + "/api/v1/mine/ebooks_gift";
	public static final String getDocumentNotesUrl=baseUrl + "/documents/:document_id/users/:user_id/reading_data.json";
	public static final String getAllNotesNewUrl=baseUrl + "/api/v1/users/:id/notes/books";
	public static final String getAuthorBookUrl=baseUrl + "/api/v1/users/:d/author_books.json";
	public static final String userHomepageUrl=baseUrl + "/api/v1/users/:d.json";
	public static final String userHomepageByNickNameUrl=baseUrl + "/api/v1/users/n/:s.json";
	public static final String wishUrl=baseUrl + "/books/:id/wish.json";
	public static final String unwishUrl=baseUrl + "/books/:id/unwish.json";
	public static final String getEbookListUrl=baseUrl + "/api/v1/ebook_tags/book_list/";
	public static final String shareCloudDiskUrl =  baseUrl + "/document_shelf/%d";
	public final static String shareBookUrl = baseUrl + "/books/";
	public final static String shareTweetURL = baseUrl + "/entities/s/";
	public static final String getSplashData = baseUrl + "/splash.json";
	public static final String joinBooksBars = baseUrl + "/bars/:id/join.json";
	public static final String leaveBooksBars = baseUrl + "/bars/:id/leave.json";
	public static final String searchBooksBars = baseUrl + "/bars/search/:query.json";
	public static final String getAllServerBooksBarMenmberslist = baseUrl + "/bars/:id/users.json";
	public static final String getAllServerBooksBarBookslist = baseUrl + "/bars/:id/books.json";
	public static final String getAllServerBooksBar = baseUrl + "/bars/all.json";
	public static final String getServerBooksBarInfo = baseUrl + "/bars/:id/detail.json";
	public static final String getServerBooksBarTimeline = baseUrl + "/bars/:id/entities.json";
	public static final String getServerBooksBar = baseUrl + "/mine/bars.json";
	public static final String getServerDiskBook = baseUrl + "/mine/user_document_files.json";

	
	
	public static final String searchBookOnCloud = baseUrl+"/user_document_files/mine/search.json";
	public static final String downloadCloudBook = baseUrl+"/user_document_files/download/";
	public static final String recentlyReading = baseUrl+"/mine/reading_books.json";
	public static final String getBooksSelectedTimeline = baseUrl+"/explore/banners.json";
	public static final String getBooksSelectedTopTimeline = baseUrl+"/explore/banners/";
	public static final String bindBookUrl = baseUrl + "/documents/bind/candidate/";
	public static final String purchaseUrl = baseUrl + "/purchase.json";
	public static final String freeBooksUrl = baseUrl + "/ebook_tags/free.json";
	public static final String freeNewBooksUrl = baseUrl + "/api/v1/ebook_tags/free_book_list/free";
	
	public static final String downloadSuccessUrl = baseUrl + "/download_success.json";
	public static final String loginUrl = baseUrl + "/api/tokens.json";
	public static final String saloginUrl = baseUrl + "/sa_register.json";
	public static final String registerUrl = baseUrl + "/users.json";
	public static final String eBookProgressUrl = baseUrl + "/ebooks/percent/";
	public static final String uploadReadingDataUrl = baseUrl + "/reading_data";
	public static final String documentProgressUrl = baseUrl + "/documents/percent/";
	public static final String bookReadingDataUrl = baseUrl + "/books/reading_data";
	public static final String bookCaseUrl = baseUrl + "/mine/ebooks.json";
	public static final String purchasedBooksUrl = baseUrl + "/mine/ebooks_purchased.json";
	
	public static final String newPurchasedBooksUrl = baseUrl + "/api/v1/mine/ebooks_purchased.json";
	
	public static final String docBindUrl = baseUrl + "/documents.json";
	public static final String bookInfoUrl = baseUrl + "/books/";
	public static final String bookInfoUrlNew = baseUrl + "/books/more/";
	
	public static final String newApi_bookInfoUrl = baseUrl + "/api/v1/books/";
	
	public static final String bookCommentUrl = baseUrl + "/book_comments";
	public static final String allBookCommentUrl = baseUrl + "/books/comments/entities/%d.json";
	public static final String allBookTweetUrl = baseUrl + "/books/entities/%d.json";
	public static final String allBookReadingUserUrl = baseUrl + "/books/reading_users/%d.json";
	public static final String logoutUrl = baseUrl + "/api/tokens/";
	public static final String checkVersionUrl = baseUrl + "/home/latest_android.json";
	public static final String userInfoUrl = baseUrl + "/users/detail/";
	
	public static final String storeTagTypeUrl = baseUrl + "/api/v1/ebook_tags/categories";
	public static final String storeNewTagTypeUrl = baseUrl + "/api/v1/ebook_tags/new_categories";
	public static final String storeBookListUrl = baseUrl + "/api/v1/ebook_tags/lights";	
	
	public static final String oldStoreBookListUrl = baseUrl + "/ebook_tags/";
	public static final String storeBookHomeUrl = baseUrl + "/api/v1/ebook_tags/store";
	public static final String storeHomeBannerUrl = baseUrl + "/banners/for_home.json";
	public static final String storePublisherBannerurl = baseUrl + "/banners/for_publisher.json";
	public static final String createbookOrderUrl = baseUrl + "/orders.json";
	public static final String queryBookOrderUrl = baseUrl + "/orders/";
	public static final String timelineUrl = baseUrl + "/mine/users/timeline.json";
	
	public static final String timelineTweet = baseUrl + "/entities/";
	
	public static final String recommandUrl = baseUrl + "/recommends.json";
	
	public static final String likeEntityUrl = baseUrl + "/recommends/like";
	public static final String unlikeEntityUrl = baseUrl + "/recommends/unlike";
	
	public static final String unrecommandUrl = baseUrl + "/recommends/disrecommend.json";
	public static final String favouriteUrl = baseUrl + "/favourites";
	public static final String unFavouriteUrl = baseUrl + "/favourites/delete";
	public static final String commmentsPostUrl = baseUrl + "/entity_comments";
	public static final String commentListUrl = baseUrl + "/entity_comments";
	public static final String forwardListUrl = baseUrl + "/entity_forwards/fetch/";
	public static final String recommandListUrl = baseUrl + "/entities/recommends";
	public static final String deleteEntityUrl = timelineTweet;
	public static final String deleteCommentUrl = baseUrl + "/entity_comments/";
	public static final String postTweetUrl = baseUrl + "/user_tweets";
	public static final String searchUsersUrl = baseUrl + "/search/users.json";
	public static final String atMeUrl = baseUrl + "/mine/atme.json";
	public static final String myCommentsUrl = baseUrl + "/mine/comments.json";
	public static final String generalAlertsUrl = baseUrl + "/mine/general_alerts.json";
	public static final String notificationUrl = baseUrl + "/mine/notifications.json";
	public static final String usersPublicUrl = baseUrl + "/users/";
	public static final String userNameUrl = baseUrl + "/users/n/";
	public static final String userEntitiesUrl = baseUrl + "/users/entities/";
	public static final String userBookCommentUrl = baseUrl + "/users/bookcomments/";
	public static final String userNotesUrl = baseUrl + "/users/notes/entities/";
	public static final String userFavouriteUrl = baseUrl + "/favourites/mine.json";
	public static final String boughtBookListUrl = baseUrl + "/book_shelf/";
	public static final String importBookListUrl = baseUrl + "/document_shelf/";
	public static final String followingListUrl = baseUrl + "/following/users/";
	public static final String followerListUrl = baseUrl + "/follower/users/";
	public static final String followCertainUser = baseUrl + "/v2/users/follow/";
	public static final String unFollowCertainUser = baseUrl + "/v2/users/unfollow/";
	public static final String searchEntityUrl = baseUrl + "/search/entities.json";
	public static final String searchBookUrl = baseUrl + "/search/books.json";
	public static final String searchEbookUrl = baseUrl + "/search/ebooks/";
	public static final String recommandUsers = baseUrl + "/recommend_users.json";
	public static final String thirdPartyPeople = baseUrl + "/people.json";
	public static final String batchFollow = baseUrl + "/v2/users/batch_follow.json";
	public static final String settingUrl = baseUrl + "/settings.json";
	public static final String deleteNotesUrl = baseUrl + "/notes/";
	public static final String updatePublicUrl = baseUrl + "/notes/update_public";
	public static final String showAsEntityUrl = baseUrl + "/notes/show_as_entity/";
	public static final String noteListUrl = baseUrl + "/books/%d/notes/users_list.json";
	public static final String sinaWeibo = "http://weibo.com/";
	public static final String pushNotesUrl = baseUrl + "/notes/sync";
	public static final String pullNotesUrl = baseUrl + "/notes/user_book_notes";
	public static final String privateMessageList = baseUrl + "/mine/conversations.json";
	public static final String messageWithUser = baseUrl + "/messages/history/%d.json";
	public static final String messageHistory = baseUrl + "/messages/history/%d.json";
	public static final String sendMessage = baseUrl + "/messages.json";
	public static final String deleteConversation = baseUrl + "/conversations/delete.json";
	public static final String borrowList = baseUrl + "/books/borrow_users/%d.json";
	public static final String borrowRequest = baseUrl + "/document_requests.json";
	public static final String borrowApprove = baseUrl + "/document_requests/approve.json";
	public static final String borrowDeny = baseUrl + "/document_requests/deny.json";
	public static final String downloadDocument = baseUrl + "/messages/download_document/%d.json";
	public static final String hideDocument = baseUrl + "/documents/hide/%d.json";
	public static final String unhideDocument = baseUrl + "/documents/unhide/%d.json";
	public static final String shareByMessage = baseUrl + "/documents/share_by_message.json";
	public static final String followedUserReading = baseUrl + "/explore/follower_books.json";
	public static final String bindSina = baseUrl + "/callback_msina.json";
	public static final String unBind = baseUrl + "/social_accounts/%d.json";
	public static final String updateRegister = baseUrl + "/update_sa_register.json";
	public static final String baiduPushLogin = baseUrl + "/api/v1/baidu_push_users/user_log_in";
	public static final String baiduPushLogout = baseUrl + "/api/v1/baidu_push_users/user_cancel";
	public static final String batchPurchaseBookKey = baseUrl + "/batch_purchase.json";
	public static final String isNotesRecommanded = baseUrl + "/notes/recommended";
	
	//拇指后台接口 结束
	
	

}
