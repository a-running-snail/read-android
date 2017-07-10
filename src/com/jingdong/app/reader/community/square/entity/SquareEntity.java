package com.jingdong.app.reader.community.square.entity;

public class SquareEntity {
	/** 商品id */
	public long bookId;
	/** 评论数量 */
	public int commentCount;
	/** 评论的时间 */
	public String commentdate;
	/** 评论的内容 */
	public String content;
	/** 唯一标识 */
	public String guid;
	/** 动态信息id */
	public long id;
	/** 商品信息 */
	public BookInfoEntity mBookInfoEntity;
	/** 评价星级 */
	public int rating;
	/** 当前登录用户是否点了赞 false 否 true 是 */
	public boolean recommend;
	/** 点赞的数量 */
	public int recommendsCount;
	public long renderId;
	public String renderType;
	/** 用户信息，主要是要用户的头像 */
	public UserBaseInfoEntity mUserInfoEntity;
	public long userId;
	public UsersEntity mUsersEntity;
	/** 总数据条数 */
	public int totalCount;
}
