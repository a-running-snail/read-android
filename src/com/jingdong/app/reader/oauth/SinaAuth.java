package com.jingdong.app.reader.oauth;

import android.app.Activity;
import android.content.Intent;

import com.jingdong.app.reader.net.WebRequest;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.auth.AuthInfo;

public class SinaAuth {
    
	/** 第三方应用应该使用自己的 APP_KEY 替换该 APP_KEY */
    static String SINA_APP_KEY = "2263235970";
    static String SINA_APP_SECRET = "38b57722954bc7c4fd6b55a53a1d9fde";
    /** 
     * 第三方应用可以使用自己的回调页。
     * 
     * <p>
     * 注：关于授权回调页对移动客户端应用来说对用户是不可见的，所以定义为何种形式都将不影响，
     * 但是没有定义将无法使用 SDK 认证登录。
     * 建议使用默认回调页：https://api.weibo.com/oauth2/default.html
     * </p>
     */
    static String SINA_REDIRECT_URL = "http://www.weibo.com";
    
    /**
     * Scope 是 OAuth2.0 授权机制中 authorize 接口的一个参数。通过 Scope，平台将开放更多的微博
     * 核心功能给开发者，同时也加强用户隐私保护，提升了用户体验，用户在新 OAuth2.0 授权页中有权利
     * 选择赋予应用的功能。
     * 
     * 我们通过新浪微博开放平台-->管理中心-->我的应用-->接口管理处，能看到我们目前已有哪些接口的
     * 使用权限，高级权限需要进行申请。
     * 
     * 目前 Scope 支持传入多个 Scope 权限，用逗号分隔。
     * 
     * 有关哪些 OpenAPI 需要权限申请，请查看：http://open.weibo.com/wiki/%E5%BE%AE%E5%8D%9AAPI
     * 关于 Scope 概念及注意事项，请查看：http://open.weibo.com/wiki/Scope
     */
    static final String SINA_SCOPE = 
            "email,friendships_groups_read,follow_app_official_microblog";
    
    /** 通过 code 获取 Token 的 URL */
    static final String OAUTH2_ACCESS_TOKEN_URL = "https://open.weibo.cn/oauth2/access_token";

    private AuthInfo weibo;

    private SsoHandler ssoHandler;

    private WeiboAuthListener weiboAuthListener;
    
    private Activity activity;
    
    public SinaAuth(Activity activity, WeiboAuthListener l) {
        weibo = new AuthInfo(activity, SINA_APP_KEY, SINA_REDIRECT_URL, SINA_SCOPE);
        this.activity = activity;
        weiboAuthListener = l;
    }

    public void auth() {
        // SSO授权
    	ssoHandler = new SsoHandler(activity, weibo);
        ssoHandler.authorize(weiboAuthListener);
    }
    
    public String getUserData(String accessToken, String uid) { 
        String urlText = "https://api.weibo.com/2/users/show.json?access_token=" + accessToken + "&uid=" + uid;
        return WebRequest.requestWebData(urlText, null, WebRequest.httpGet);
    }

    public void authCallBack(int requestCode, int resultCode, Intent data) {
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}
