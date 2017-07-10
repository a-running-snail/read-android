package com.jingdong.app.reader.entity;


public class BodyEncodeEntity{
	   public String strEnvelope = "";  //信物
	   public String sourcePublicKey = ""; //第一次获取的公钥
	   public String sessionKey = ""; // 第二次请求获取的密钥
	   public String desSessionKey = ""; // 解密后的sessionKey
	   public boolean isSuccess =true;

	   
}