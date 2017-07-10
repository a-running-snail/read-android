package com.jingdong.app.reader.util.unionlogin;

/**
 * 
 * @ClassName: LoginData
 * @Description: 封装第三方登录成功后的模型数据
 * @author J.Beyond
 * @date 2015-3-13 下午5:44:12
 *
 */
public class LoginData {

	private String uid;
	private String accessToken;
	private long expiresIn;
	private String refreshToken;
	private String scope;
	public LoginData() {
		// TODO Auto-generated constructor stub
	}
	public LoginData(String uid, String accessToken, long expiresIn,
			String refreshToken, String scope) {
		super();
		this.uid = uid;
		this.accessToken = accessToken;
		this.expiresIn = expiresIn;
		this.refreshToken = refreshToken;
		this.scope = scope;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public long getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	@Override
	public String toString() {
		return "LoginData [uid=" + uid + ", accessToken=" + accessToken
				+ ", expiresIn=" + expiresIn + ", refreshToken=" + refreshToken
				+ ", scope=" + scope + "]";
	}
	
}
