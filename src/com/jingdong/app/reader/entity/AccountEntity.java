package com.jingdong.app.reader.entity;
import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;
public class AccountEntity{
	public final static String KEY_BALANCE = "balance";
	public final static String KEY_LOCKBALANCE = "lockBalance";
	public final static String KEY_STATUS = "status";
    public final static String KEY_CODE="code";
	public double balance;
	public double lockBalance;
	public String status;
    public String code;

	public final static  AccountEntity parse(JSONObject jsonObject) {
		AccountEntity account = null;
		if (jsonObject != null) {
			       try {
			    	   account = new AccountEntity();
			    	   account.balance = DataParser.getDouble(jsonObject, AccountEntity.KEY_BALANCE);
			    	   account.lockBalance = DataParser.getDouble(jsonObject, AccountEntity.KEY_LOCKBALANCE);
			    	   account.status = DataParser.getInt(jsonObject, AccountEntity.KEY_STATUS)==1?"不可用":"可用";
                       account.code=DataParser.getString(jsonObject, AccountEntity.KEY_CODE);
					} catch (Exception e) {
						e.printStackTrace();
					}
        }
		return account;
         }
	}